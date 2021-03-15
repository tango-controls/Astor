//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// $Revision$
//
//-======================================================================


package admin.astor;

import admin.astor.access.TangoAccess;
import admin.astor.statistics.ResetStatistics;
import admin.astor.tools.PopupText;
import admin.astor.tools.Utils;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.IORdump;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.Splash;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.StringTokenizer;


public class AstorTree extends JTree implements AstorDefs {
    private Astor parent;
    public TangoHost[] hosts;
    private TangoHost selectedHost = null;
    private DatabaseObject selected_db = null;
    private DatabaseObject[] dbase;
    private DefaultTreeModel treeModel;
    private javax.swing.Timer watchDogTimer;
    private TacObject accessControl;
    private TangoAccess tangoAccessPanel = null;

    /**
     * A jive instance to be displayed.
     */
    private jive3.MainPanel jive3 = null;

    /**
     * Popup menu to be used on right button clicked.
     */
    private TreePopupMenu pMenu;
    private DbPopupMenu dbMenu;
    private TacPopupMenu tacMenu;
    /**
     * Class to store many HostInfoDialog
     */
    HostInfoDialogVector hostDialogs = new HostInfoDialogVector();

    private static List<String> collecNames = new ArrayList<>();
    private static DefaultMutableTreeNode root;
    private static int hostSubscribed = 0;
    private static final Color background = new Color(0xf0, 0xf0, 0xf0);

    // startup objects
    private List<String> subscribeError = new ArrayList<>();
    private UpdateSplashThread updateSplashThread;
    PopupText subscribeErrWindow = null;
    private long startSubscribeTime;
    private JProgressBar splashProgess;
    private JLabel splashLabel;
    //===============================================================
    //===============================================================
    public AstorTree(Astor parent, JProgressBar splash,JLabel label) throws DevFailed {
        this.parent = parent;
        this.splashProgess = splash;
        this.splashLabel = label;

        //	Build panel and its tree
        initComponent();
        setBackground(background);

        //	Check hosts using or not events.
        //  since ZMQ event system, all hosts are tried to be controlled on events
        hostSubscribed = 0;
        updateSplashThread = new UpdateSplashThread(hosts.length);
        updateSplashThread.start();
        int cnt = 0;
        for (TangoHost host : hosts) {

            //	And start the control thread
            updateSplashThread.wakeUp(cnt++, "Creating  " + host + "  object");
            host.thread = new HostStateThread(this, host);
            host.thread.start();
        }
        updateSplashThread.stopThread();


        //	Start a thread to subscribe events and a monitor to display
        if (hostSubscribed == 0) {   //	init size
            updateSplashThread = new UpdateSplashThread(hosts.length);
            updateSplashThread.start();

            startSubscribeTime = System.currentTimeMillis();
            new subscribeThread().start();
            updateMonitor(null);
        }

        //	Build menus
        if (parent instanceof Astor)
            pMenu = new TreePopupMenu((Astor) parent, this);
        else
            pMenu = new TreePopupMenu(this);
        dbMenu = new DbPopupMenu(this);
        if (isAccessControlled())
            tacMenu = new TacPopupMenu(this);

        //	Expend for database
        expandRow(1);

        //	Start a timer to have a watch dog on host thread
        int delay = 10000; //   2*timeout on first connection in milliseconds
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //watchDog(evt);
            }
        };
        watchDogTimer = new javax.swing.Timer(delay, taskPerformer);
        watchDogTimer.start();
    }


    //===============================================================
    /**
     * A little thread to update splash screen
     * because it gets too much time in loop.
     */
    //===============================================================
    private class UpdateSplashThread extends Thread {
        private String message = "";
        private int ratio;
        private boolean stop = false;

        //===========================================================
        private UpdateSplashThread(int maxValue) {
            splashLabel.setVisible(true);
            splashProgess.setVisible(true);
            splashProgess.setMaximum(maxValue);
        }

        //===========================================================
        public void run() {
            while (!stop) {
                splashProgess.setValue(ratio);
                splashLabel.setText(message);
                doSleep();
            }
        }

        //===========================================================
        private synchronized void doSleep() {
            try {
                wait(500);
            } catch (InterruptedException e) { /* */ }
        }

        //===========================================================
        private synchronized void wakeUp(int ratio, String message) {
            this.ratio = ratio;
            this.message = message;
            notify();
        }

        //===========================================================
        private synchronized void stopThread() {
            stop = true;
            notify();
        }
        //===========================================================
    }

    //===============================================================
    //===============================================================
    void updateMonitor(String strError) {
        //	add error startup message
        if (strError != null) {
            subscribeError.add(strError);
        }

        //	Check if startup is terminated
        if (hostSubscribed < hosts.length) {
            //	NOT TERMINATED
            if (updateSplashThread != null) {
                TangoHost host = hosts[hostSubscribed];
                String message = "Subscribing for  " + host.getName() + "  (" +
                        (hostSubscribed + 1) + "/" + hosts.length + ")";
                updateSplashThread.wakeUp(hostSubscribed, message);    //  Wake up thread to update splash screen
                hostSubscribed++;
            }
        }
        else {
            //	All host subscribed or failed

            //  Display time spent.
            long t1 = System.currentTimeMillis();
            System.out.println("Total time to subscribe on " +
                    hostSubscribed + " hosts : " + (t1 - startSubscribeTime) + " ms");
            System.out.println("Total time to start Astor " + (t1 - Astor.t0) + " ms");

            if (updateSplashThread != null) {
                updateSplashThread.stopThread();
            }

            //	Concat. error messages at startup if any
            splashLabel.setVisible(false);
            splashProgess.setVisible(false);
            if (subscribeError.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < subscribeError.size(); i++) {
                    sb.append(subscribeError.get(i));
                    if (i < subscribeError.size() - 1)
                        sb.append("\n\n");
                }
                String title = new Date().toString() +
                        " - Subscribe events on " + subscribeError.size() + "/" +
                        hostSubscribed;
                if (subscribeError.size() == 1)
                    title += " host has failed";
                else
                    title += " hosts have failed";
                int height = 50 + subscribeError.size() * 75;
                if (height > 560)
                    height = 560;

                subscribeErrWindow = new PopupText(parent, true);
                subscribeErrWindow.setTitle(title);
                subscribeErrWindow.addText(sb.toString());
                subscribeErrWindow.setSize(900, height);
            }
        }
    }

    //===============================================================
    //===============================================================
    void stopThreads() {
        watchDogTimer.stop();
        for (TangoHost host : hosts)
            host.stopThread();
    }

    //===============================================================
    //===============================================================
    void expand(boolean expand) {
        if (expand)
            //	Expand
            for (int i = 0; i < (hosts.length + collecNames.size() + 1); i++)
                expandRow(i);
        else
            //	Collapse
            for (int i = 1; i <= collecNames.size(); i++)
                collapseRow(i);
    }

    //===============================================================
    //===============================================================
    boolean isAccessControlled() {
        return accessControl != null;
    }

    //===============================================================
    //===============================================================
    private void initComponent() throws DevFailed {

        // Create the nodes.
        root = new DefaultMutableTreeNode("Control System");
        initTangoObjects();
        createNodes(root);

        //Create a tree that allows one selection at a time.
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //	Create Tree and Tree model
        treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);

        /*
         * Create a Renderer to set the icon for leaf nodes.
         */
        setCellRenderer(new TangoRenderer());

        //	Listen for when the selection changes.
        addTreeSelectionListener(e -> hostSelectionPerformed());

        //	Listen for collapse tree
        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
                collapsedPerformed(e);
            }

            public void treeExpanded(TreeExpansionEvent e) {
                //expandedPerformed(e);
            }
        });
        //	Add Action listener
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);
            }
        });
        setExpandsSelectedPaths(true);
    }

    //===============================================================
    //===============================================================
    private void initTangoObjects() throws DevFailed {
        //	Build Database objects
        String tango_hosts = AstorUtil.getTangoHost();
        if (tango_hosts == null || tango_hosts.length() == 0)
            Except.throw_connection_failed("TangoApi_TANGO_HOST_NOT_SET",
                    "TANGO_HOST is not set",
                    "AstorTree.initTangoObjects()");
        else {
            StringTokenizer stk;
            if (tango_hosts.contains(","))
                stk = new StringTokenizer(tango_hosts, ",");
            else
                stk = new StringTokenizer(tango_hosts);

            List<String> list = new ArrayList<>();
            while (stk.hasMoreTokens())
                list.add(stk.nextToken());
            dbase = new DatabaseObject[list.size()];
            for (int i = 0; i < list.size(); i++)
                dbase[i] = new DatabaseObject(this, list.get(i));

            //	Build Host objects
            AstorUtil au = AstorUtil.getInstance();
            hosts = au.getTangoHostList();
            collecNames = au.getCollectionList(hosts);

            String accessControlDeviceName = AstorUtil.getAccessControlDeviceName();
            if (accessControlDeviceName != null)
                accessControl = new TacObject(this, accessControlDeviceName);
        }
    }

    //===============================================================
    //===============================================================
    private void createNodes(DefaultMutableTreeNode root) {

        updateSplashThread = new UpdateSplashThread(hosts.length);
        updateSplashThread.start();
        List<DefaultMutableTreeNode> collections = new ArrayList<>();
        for (String collectionName : collecNames) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(collectionName);
            collections.add(node);
            root.add(node);
        }
        //	First create database node
        for (DatabaseObject db : dbase) {
            collections.get(0).add(new DefaultMutableTreeNode(db));
        }

        //  Add TAC if any
        if (accessControl != null) {
            collections.get(0).add(new DefaultMutableTreeNode(accessControl));
        }

        //	Add Host nodes
        int cnt = 0;
        for (TangoHost host : hosts) {
            updateSplashThread.wakeUp(cnt++, "Creating " + host + " node");
            DefaultMutableTreeNode host_node =
                    new DefaultMutableTreeNode(host);
            host.state = unknown;
            int idx = getHostCollection(host);
            collections.get(idx).add(host_node);
        }
        updateSplashThread.stopThread();

    }

    //===============================================================
    //===============================================================
    List<String> getCollectionList() {
        return collecNames;
    }

    //===============================================================
    //===============================================================
    private int getHostCollection(TangoHost host) {
        for (int i = 0; i < collecNames.size(); i++)
            if (host.collection == null)
                return collecNames.size() - 1;    //	The last one
            else if (host.collection.equals(collecNames.get(i)))
                return i;
        return collecNames.size() - 1;    //	The last one.
    }

    //======================================================
    //======================================================
    private void collapsedPerformed(TreeExpansionEvent e) {
        //	Get path
        TreePath path = e.getPath();
        if (path.getPathCount() > 2)
            return;
        //	Get concerned node
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) path.getPathComponent(path.getPathCount() - 1);

        boolean is_db_collec =
                ((DefaultMutableTreeNode) node.getChildAt(0)).getUserObject() instanceof DatabaseObject;
        //	do not collapse if Root or database node
        if (path.getPathCount() == 1 || is_db_collec) {
            //	Cancel collapse tree
            DefaultMutableTreeNode leaf =
                    (DefaultMutableTreeNode) node.getChildAt(0);
            TreeNode[] leaf_path = leaf.getPath();
            setExpandedState(new TreePath(leaf_path), true);

            //	If root Display TANGO information
            if (path.getPathCount() == 1) {
                String message = "TANGO Control System\n\n";
                message += hosts.length + " hosts controlled.\n";
                int nb_on_events = 0;
                for (TangoHost host : hosts)
                    if (host.onEvents)
                        nb_on_events++;
                if (nb_on_events == hosts.length)
                    message += "All are controlled on events.";
                else if (nb_on_events > 0)
                    message += nb_on_events + " are controlled on events.";
                Utils.popupMessage(parent, message, "TangoClass.png");
            }
        }
    }


//===============================================================
//	
//	Editing Tree (Rename, move...)
//
//===============================================================
    //===============================================================
    /**
     * Compute bound rectangle for a node
     *
     * @param selPath selected tree path
     * @return the rectangle to get inputs
     */
    //===============================================================
    private Rectangle computeBounds(TreePath selPath) {
        scrollPathToVisible(selPath);
        Rectangle r = getPathBounds(selPath);
        if (r!=null) {
            Point p = r.getLocation();
            SwingUtilities.convertPointToScreen(p, this);
            r.setLocation(p);
            r.width += 20;
            r.height += 2;
        }
        return r;
    }

    //===============================================================
    //===============================================================
    private Object getSelectedObject() {
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) getLastSelectedPathComponent();
        return node.getUserObject();
    }

    //===============================================================
    //===============================================================
    void startHostInfo() {
        System.out.println("Astor: " + parent);
        Object  object = getSelectedObject();
        if (object instanceof TangoHost) {
            TangoHost   host = (TangoHost) object;
            String className = AstorUtil.getHostInfoClassName();
            try {
                AstorUtil.getInstance().startExternalApplication(parent, className, host.hostName());
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(parent, null, e);
            }
        }
        if (object instanceof String) { //  A branch
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    getLastSelectedPathComponent();

            //	Get collection children
            int nbHosts = node.getChildCount();
            String[]   hostNames = new String[nbHosts];
            for (int i=0 ; i<nbHosts ; i++) {
                node = node.getNextNode();
                hostNames[i] = ((TangoHost) node.getUserObject()).hostName();
            }
            String className = AstorUtil.getHostInfoClassName();
            try {
                AstorUtil.getInstance().startExternalApplication(parent, className, hostNames);
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(parent, null, e);
            }
        }
    }
    //===============================================================
    //===============================================================
    void changeNodeName() {
        Rectangle r = computeBounds(getSelectionPath());
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                getLastSelectedPathComponent();
        //	Build the inside dialog
        RenameDialog dlg = new RenameDialog(parent, node.toString(), r);
        if (dlg.showDlg()) {
            String new_name = dlg.getNewName();

            //	Change property for all Starter devices
            try {
                DefaultMutableTreeNode n2 = node;
                int nb = n2.getChildCount();
                for (int i = 0; i < nb; i++) {
                    n2 = n2.getNextNode();
                    TangoHost th = (TangoHost) n2.getUserObject();
                    th.setCollection(new_name);
                }
            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(parent, null, e);
                return;
            }
            node.setUserObject(new_name);
        }
    }

    //===============================================================
    //===============================================================
    void addBranch(String name) {
        //	Create node for new branch
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);

        //	Add new branch and root node to add new branch
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        DefaultMutableTreeNode root =
                (DefaultMutableTreeNode) model.getRoot();
        model.insertNodeInto(node, root, root.getChildCount());
        collecNames.add(name);
    }

    //===============================================================
    //===============================================================
    void moveNode() {
        //	Get selected host
        DefaultMutableTreeNode hostNode =
                (DefaultMutableTreeNode) getLastSelectedPathComponent();
        DefaultMutableTreeNode parentNode =
                (DefaultMutableTreeNode) hostNode.getParent();

        //	Get collection without Database and its own collection
        String[] targetNames = new String[collecNames.size() - 1];
        int idx = 0;
        for (int i = 1; i < collecNames.size(); i++) {
            if (!collecNames.get(i).equals(parentNode.toString()))
                targetNames[idx++] = collecNames.get(i);
        }
        targetNames[idx] = "New Branch";

        //	And choose new collection
        String targetName = (String) JOptionPane.showInputDialog(this,
                        "Move " + hostNode + "  to :", "",
                        JOptionPane.INFORMATION_MESSAGE, null,
                        targetNames, targetNames[0]);
        if (targetName != null) {
            //	Is it a new one ?
            if (targetName.equals(targetNames[idx])) {    //	"New Branch"

                //	Get the new branch name and add it
                targetName =
                        (String) JOptionPane.showInputDialog(this,
                                "New Branch Name",
                                "Input Dialog",
                                JOptionPane.INFORMATION_MESSAGE,
                                null, null, "");
                if (targetName == null)
                    return;

                addBranch(targetName);
            }

            //	Get target collection node
            for (int i = 0; i < root.getChildCount(); i++) {

                DefaultMutableTreeNode collecNode =
                        (DefaultMutableTreeNode) root.getChildAt(i);
                if (collecNode.toString().equals(targetName)) {

                    //	Change the Starter property
                    try {
                        TangoHost host = (TangoHost) hostNode.getUserObject();
                        host.setCollection(targetName);
                    } catch (DevFailed e) {
                        ErrorPane.showErrorMessage(parent, null, e);
                        return;
                    }

                    //	Get the default model used to move node
                    DefaultTreeModel model = (DefaultTreeModel) getModel();
                    model.removeNodeFromParent(hostNode);
                    model.insertNodeInto(hostNode, collecNode, 0);

                    //	Check if previous collection has still children
                    if (parentNode.getChildCount() == 0) {
                        model.removeNodeFromParent(parentNode);
                        collecNames.remove(parentNode.toString());
                    }

                    //	Ensure that the new node is visible and select it
                    scrollPathToVisible(new TreePath(hostNode.getPath()));

                    TreeNode[] path = hostNode.getPath();
                    setSelectionPath(new TreePath(path));
                }
            }
        }
    }

    //===============================================================
    //===============================================================
    private void hostSelectionPerformed() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                getLastSelectedPathComponent();

        if (node == null) return;

        Object obj = node.getUserObject();
        if (node.isLeaf()) {
            if (obj instanceof TangoHost) {
                selectedHost = (TangoHost) obj;
                selected_db = null;
            } else if (obj instanceof DatabaseObject) {
                selectedHost = null;
                selected_db = (DatabaseObject) obj;
            } else {
                selectedHost = null;
                selected_db = null;
            }
        } else {
            selectedHost = null;
            selected_db = null;
        }
    }


//======================================================
//
//	Mouse event management.
//
//======================================================
    //======================================================
    /**
     * Manage event on clicked mouse on JTree object.
     *
     * @param evt the mouse event
     */
    //======================================================
    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
        //	Check if click is on a node
        if (getRowForLocation(evt.getX(), evt.getY()) < 1)
            return;

        //	Set selection at mouse position
        TreePath selectedPath =
                getPathForLocation(evt.getX(), evt.getY());

        if (selectedPath == null)
            return;
        setSelectionPath(selectedPath);

        int mask = evt.getModifiers();
        //	Do something only if double or right click
        if (evt.getClickCount() == 2) {
            //	Check if btn1
            if ((mask & MouseEvent.BUTTON1_MASK) != 0) {
                if (selectedHost != null) {
                    //	Display dialog
                    displayHostInfo();
                } else {
                    DefaultMutableTreeNode node =
                            (DefaultMutableTreeNode) getLastSelectedPathComponent();
                    Object o = node.getUserObject();
                    if (selected_db != null ||
                            o.toString().equals(collecNames.get(0)))
                        displayJiveAppli();
                }
            }
        } else {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) getLastSelectedPathComponent();
            Object obj = node.getUserObject();
            //	Check if btn3
            if ((mask & MouseEvent.BUTTON3_MASK) != 0) {
                if (obj instanceof DatabaseObject)
                    dbMenu.showMenu(evt);
                else if (obj instanceof TacObject)
                    tacMenu.showMenu(evt);
                else
                    pMenu.showMenu(evt);
            }
        }
    }

    //======================================================
    //======================================================
    void setSelectionRoot() {
        setSelectionPath(new TreePath(root.getPath()));
    }
    //======================================================
    //======================================================
    public void setSelectionPath(String hostname) throws DevFailed{
        DefaultMutableTreeNode collec;
        DefaultMutableTreeNode node;
        boolean found = false;
        for (int i = 0; i < root.getChildCount(); i++) {
            collec = (DefaultMutableTreeNode) root.getChildAt(i);
            for (int j = 0; j < collec.getChildCount(); j++) {
                node = (DefaultMutableTreeNode) collec.getChildAt(j);
                Object obj = node.getUserObject();
                if (obj instanceof TangoHost) {
                    String name = node.toString();
                    int idx = name.indexOf('(');
                    if (idx > 0)    //	remove description
                        name = name.substring(0, idx).trim();
                    if (name.equals(hostname)) {
                        setSelectionPath(new TreePath(node.getPath()));
                        found = true;
                    }
                }
            }
        }
        if (!found)
            Except.throw_exception("HOST_NOT_FOUND",
                    hostname + "  is not controlled by Astor !",
                    "AstorTree.setSelectionPath()");
    }

    //======================================================
    //======================================================
    private boolean jive_is_read_only = false;

    void displayJiveAppli() {
        if (selected_db != null && selected_db.state == faulty) {
            ErrorPane.showErrorMessage(parent, null, selected_db.except);
        } else {
            boolean read_only = AstorUtil.getInstance().jiveIsReadOnly();
            if (Astor.rwMode!=AstorDefs.READ_WRITE)
                read_only = true;

            //	Check if it has changed
            //	or not already Started
            if (jive_is_read_only != read_only || jive3 == null)
                jive3 = new jive3.MainPanel(false, read_only);
            jive3.setVisible(true);
            jive3.toFront();
            jive_is_read_only = read_only;
        }
    }
    //===============================================================
    //===============================================================
    void showJive(TangoServer server) {
        displayJiveAppli();
        jive3.goToServerNode(server.getName());
    }
    //===============================================================
    /**
     * Replace an old leaf by a new one containing the new object.
     * This method is mainly used to re size node (when usage changed)
     *
     * @param h the new tango host object
     */
    //===============================================================
    void changeHostNode(TangoHost h) {
        //	Get selected node
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) getLastSelectedPathComponent();

        //	Get parent node and node position.
        DefaultMutableTreeNode parent_node =
                (DefaultMutableTreeNode) node.getParent();
        int pos = 0;
        for (int i = 0; i < parent_node.getChildCount(); i++)
            if (parent_node.getChildAt(i).equals(node))
                pos = i;

        //	Build ne node and insert
        DefaultMutableTreeNode new_node = new DefaultMutableTreeNode(h);
        treeModel.insertNodeInto(new_node, parent_node, pos);

        //	Remove old one
        treeModel.removeNodeFromParent(node);

        //	And set selection on new
        TreeNode[] path = new_node.getPath();
        setSelectionPath(new TreePath(path));
    }

    //======================================================
    //======================================================
    public void displayHostInfoDialog(String hostname) {
        try {
            //	Take off IP address if exists
            StringTokenizer st = new StringTokenizer(hostname);
            hostname = st.nextToken();
            //	Take off name extension (e.g. .esrf.fr) if exists
            st = new StringTokenizer(hostname, ".");
            hostname = st.nextToken();

            //	Select Host on main Tree and Popup host panel
            parent.setVisible(true);
            setSelectionPath(new TreePath(root.getPath()));    // remove previous selection
            setSelectionPath(hostname);
            displayHostInfo();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }

    //======================================================
    //======================================================
    public void displayHostInfo(String deviceName) {
        try {
            //	Get host name from device
            String hostname = new IORdump(deviceName).get_host();
            if (hostname == null)
                Except.throw_exception("UNKNOWN_HOST",
                        "May be this device has never been exported !", "");
            else {
                //	Take off IP address if exists
                StringTokenizer st = new StringTokenizer(hostname);
                hostname = st.nextToken();
                //	Take off name extention (e.g. .esrf.fr) if exists
                st = new StringTokenizer(hostname, ".");
                hostname = st.nextToken();

                //	Select Host on main Tree and Popup host panel
                parent.setVisible(true);
                setSelectionPath(new TreePath(root.getPath()));    // remove previous selection
                setSelectionPath(hostname);
                displayHostInfo();
                String servname = new DeviceProxy(deviceName).adm_name();
                servname = servname.substring(servname.indexOf('/') + 1);
                HostInfoDialog dlg = hostDialogs.getByHostName(selectedHost);
                if (dlg != null)
                    dlg.setSelection(servname);
            }
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }

    //======================================================
    //======================================================
    void displayHostInfo() {
        //	Check if a host is selected
        if (selectedHost == null) {
            Utils.popupError(this, "this Host is not controlled by Astor !");
            return;
        }

        //  Check if host alive
        if (System.getenv("DEBUG")!=null && !System.getenv("DEBUG").equals("true")) {
            try {
                selectedHost.checkIfAlive();
            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
                return;
            }
        }
        //	Manage a synchronous read attributes
        selectedHost.updateData();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) { /* */ }

        //	Check if host starter is faulty
        if (selectedHost.state == faulty && selectedHost.except != null) {
            //	Check if host starter is running
            String reason = selectedHost.except.errors[0].reason;
            String desc = selectedHost.except.errors[0].desc;
            if (reason.equals("TangoApi_DEVICE_NOT_EXPORTED") ||
                    desc.indexOf("CORBA.TRANSIENT: Retries exceeded,") > 0) {
                //	Ask for remote login
                if (AstorUtil.osIsUnix()) {
                    if (JOptionPane.showConfirmDialog(parent,
                            "Starter is not running on " + selectedHost + "\n\n\n" +
                                    "Do you want a ssh login to start it ?",
                            "Dialog",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                        try {
                            new RemoteLoginThread(selectedHost.getName()).start();
                        } catch (DevFailed e) {
                            ErrorPane.showErrorMessage(this, null, e);
                        }
                    }
                } else
                    Utils.popupError(parent,
                            "Starter is not running on " + selectedHost + " !!!");
            } else
                //	Display exception
                ErrorPane.showErrorMessage(parent,
                        "Starter on " + selectedHost,
                        selectedHost.except);
        } else if (selectedHost.state == unknown)
            Utils.popupMessage(parent,
                    "Connection with Starter device server is blocked !");
        else {
            //	Popup a host info dialog and add it in vector
            if (parent instanceof Astor)
                hostDialogs.add((Astor) parent, selectedHost);
        }
    }
    //===============================================================
    //===============================================================
    void resetCollectionStatistics() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                getLastSelectedPathComponent();
        List<String> hostList = new ArrayList<>();
        int nb = node.getChildCount();
        for (int i = 0; i < nb; i++) {
            node = node.getNextNode();
            TangoHost host = (TangoHost) node.getUserObject();
            hostList.add(host.name());
        }

        if (JOptionPane.showConfirmDialog(parent,
                "Do you want to reset statistics on " +
                        hostList.size() + " hosts  ?",
                "Dialog",
                JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {

            new ResetStatistics(parent);
        }

    }

    //===============================================================
    //===============================================================
    void displayBranchInfo() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                getLastSelectedPathComponent();

        StringBuilder str = new StringBuilder(node + ":\n\n");
        //	Get collection children
        int nb = node.getChildCount();
        for (int i = 0; i < nb; i++) {
            node = node.getNextNode();
            TangoHost host = (TangoHost) node.getUserObject();
            str.append(host.hostStatus());
        }
        Utils.popupMessage(parent, str.toString());
    }

    //===============================================================
    //===============================================================
    void startTACpanel() {
        try {
            if (tangoAccessPanel == null || !tangoAccessPanel.isVisible()) {
                if (TangoAccess.checkPassword(parent) == JOptionPane.OK_OPTION) {
                    tangoAccessPanel = new TangoAccess(parent);
                } else
                    return;
            }
            tangoAccessPanel.setVisible(true);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this,
                    "Cannot start TangoAccess class", e);
        }
    }

    //===============================================================
    //===============================================================
    void updateState() {
        repaint();
        if (hostDialogs != null && hosts != null) {
            //	Close host dialogs if exists
            for (TangoHost host : hosts)
                if (host.state == faulty)
                    hostDialogs.close(host);
        }
    }
    //======================================================
    //======================================================


    //===============================================================
    /**
     * Renderer Class
     */
    //===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer {
        private ImageIcon tangoIcon;
        private ImageIcon dbIcon;
        private Font[] fonts;
        private Font rootFont;

        //===============================================================
        //===============================================================
        public TangoRenderer() {
            dbIcon = Utils.getInstance().getIcon("MySql.png");
            tangoIcon = Utils.getInstance().getIcon("TransparentTango.png", 0.25);

            fonts = new Font[2];
            rootFont = new Font("helvetica", Font.BOLD, 24);
            fonts[COLLECTION] = new Font("helvetica", Font.BOLD, 18);
            fonts[LEAF] = new Font("helvetica", Font.PLAIN, 12);
        }

        //===============================================================
        //===============================================================
        public Component getTreeCellRendererComponent(
                final JTree tree,
                final Object obj,
                final boolean sel,
                final boolean expanded,
                final boolean leaf,
                final int row,
                final boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, obj, sel,
                    expanded, leaf, row,
                    hasFocus);

            setBackgroundNonSelectionColor(background);
            setForeground(java.awt.Color.black);
            setToolTipText(null);
            if (row == 0) {
                //	ROOT
                setBackgroundSelectionColor(background);
                setIcon(tangoIcon);
                setFont(rootFont);
            } else if (isDatabase(obj)) {
                if (leaf) {
                    //	Database object
                    setBackgroundSelectionColor(java.awt.Color.lightGray);
                    DatabaseObject db = getDbase(obj);
                    setIcon(AstorUtil.state_icons[db.state]);
                    setFont(fonts[LEAF]);
                } else {
                    //	Database collection
                    setBackgroundSelectionColor(background);
                    int state = all_ok;
                    for (DatabaseObject db : dbase)
                        if (db.state == faulty)
                            state = faulty;
                    if (state == faulty)
                        setForeground(java.awt.Color.red);

                    setIcon(dbIcon);
                    setFont(fonts[COLLECTION]);
                }

            } else if (isTAC(obj)) {
                //	TAC object
                setBackgroundSelectionColor(java.awt.Color.lightGray);
                TacObject tac = getTACobject(obj);
                setIcon(AstorUtil.state_icons[tac.state]);
                setFont(fonts[LEAF]);

            } else if (isHost(obj)) {
                //	Tango Host
                setBackgroundSelectionColor(java.awt.Color.lightGray);
                setFont(fonts[LEAF]);
                setBackgroundNonSelectionColor(background);

                TangoHost host = getHost(obj);
                int state = host.state;
                if (state == unknown)
                    state = failed;
                setIcon(AstorUtil.state_icons[state]);
                setToolTipText(AstorDefs.iconHelpForHosts[state]);
            } else {
                //	Collection
                setBackgroundSelectionColor(java.awt.Color.lightGray);
                setFont(fonts[COLLECTION]);
                int state = branchState(obj);
                setIcon(AstorUtil.state_icons[state]);
            }
            return this;
        }

        //===============================================================
        //===============================================================
        int branchState(Object tree_node) {
            int state;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_node;

            //	Get collection children
            int nb = node.getChildCount();
            TangoHost[] tangoHosts = new TangoHost[nb];
            for (int i = 0; i < nb; i++) {
                node = node.getNextNode();
                tangoHosts[i] = (TangoHost) node.getUserObject();
            }

            //	Calculate how many faulty and/or alarm
            boolean is_faulty = false;
            boolean is_alarm = false;
            boolean is_moving = false;
            boolean is_long_moving = false;
            int nb_off = 0;
            int nb_ok = 0;
            for (int i = 0; i < nb; i++) {
                //	At least one unknown -> branch is unknown
                if (tangoHosts[i].state == unknown)
                    return unknown;
                else if (tangoHosts[i].state == faulty)
                    is_faulty = true;
                else if (tangoHosts[i].state == alarm)
                    is_alarm = true;
                else if (tangoHosts[i].state == moving)
                    is_moving = true;
                else if (tangoHosts[i].state == long_moving)
                    is_long_moving = true;
                else if (tangoHosts[i].state == all_off)
                    nb_off++;
                else if (tangoHosts[i].state == all_ok)
                    nb_ok++;
            }
            //	Calculate branch state
            if (is_faulty)
                state = faulty;
            else if (is_moving)
                state = moving;
            else if (is_long_moving)
                state = long_moving;
            else if (is_alarm)
                state = alarm;
            else if (nb_off==nb)
                state = all_off;
            else if (nb_ok==nb)
                state = all_ok;
            else
                state = alarm;
            return state;
        }

        //===============================================================
        //===============================================================
        boolean isHost(Object tree_node) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_node;
            Object obj = node.getUserObject();
            return (obj instanceof TangoHost);
        }

        //===============================================================
        //===============================================================
        TangoHost getHost(Object tree_node) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_node;
            Object obj = node.getUserObject();
            if (obj instanceof TangoHost)
                return (TangoHost) (obj);
            return null;
        }

        //===============================================================
        //===============================================================
        DatabaseObject getDbase(Object tree_node) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_node;
            Object obj = node.getUserObject();
            if (obj instanceof DatabaseObject)
                return (DatabaseObject) (obj);
            return null;
        }

        //===============================================================
        //===============================================================
        boolean isDatabase(Object tree_node) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_node;
            Object obj = node.getUserObject();
            if (obj instanceof DatabaseObject)
                return true;
            else if (obj instanceof String) {
                String str = (String) obj;
                return (str.equals(collecNames.get(0)));
            }
            return false;
        }

        //===============================================================
        //===============================================================
        TacObject getTACobject(Object tree_node) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_node;
            Object obj = node.getUserObject();
            if (obj instanceof TacObject)
                return (TacObject) (obj);
            return null;
        }

        //===============================================================
        //===============================================================
        boolean isTAC(Object tree_node) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_node;
            Object obj = node.getUserObject();
            return (obj instanceof TacObject);
        }
    }


    //===============================================================
    /*
      *	A thread to subscribe State events for all hosts
      */
    //===============================================================
    private class subscribeThread extends Thread {
        //===============================================================
        public void run() {
            for (TangoHost host : hosts) {
                if (host.onEvents) {
                    host.thread.subscribeChangeStateEvent();    
                }
            }
        }
        //===============================================================
    }


    //===============================================================
    /*
     *	A Database Popup menu
     */
    //===============================================================
    private class DbPopupMenu extends JPopupMenu {
        private JTree tree;
        private final String[] menuLabels = {
                "Server Info",
                "Database Info",
                "Database Black Box",
                "Database Monitoring",
                "Browse Database (Jive)",
        };
        private final int OFFSET = 2;        //	Label And separator
        private final int SERVER_INFO = 0;
        private final int DATABASE_INFO = 1;
        private final int DATABASE_BLACKBOX = 2;
        private final int DATABASE_MONITOR  = 3;
        private final int BROWSE_DATABASE = 4;
        //===========================================================
        private DbPopupMenu(JTree tree) {
            this.tree = tree;

            //	Build menu
            JLabel title = new JLabel("Database Server :");
            title.setFont(new java.awt.Font("Dialog", Font.BOLD, 16));
            add(title);
            add(new JPopupMenu.Separator());
            for (String menuLabel : menuLabels) {
                JMenuItem btn = new JMenuItem(menuLabel);
                btn.addActionListener(this::treeActionPerformed);
                add(btn);
            }
        }
        //===========================================================
        private void showMenu(java.awt.event.MouseEvent evt) {
            Object obj = getSelectedObject();

            //	Add host name in menu label title
            JLabel lbl = (JLabel) getComponent(0);
            lbl.setText(obj.toString() + "  :");
            show(tree, evt.getX(), evt.getY());
        }
        //===========================================================
        private void treeActionPerformed(ActionEvent evt) {
            Object src = evt.getSource();
            int cmdidx = 0;
            for (int i = 0; i < menuLabels.length; i++)
                if (getComponent(OFFSET + i) == src)
                    cmdidx = i;

            try {
                if (cmdidx == BROWSE_DATABASE) {
                    displayJiveAppli();
                } else {
                    manageOneDataBaseOption(cmdidx);
                }
            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
            }
        }
        //===========================================================
        private void manageOneDataBaseOption(int action) throws DevFailed {
            //	Create a Popup text window
            DatabaseObject db = (DatabaseObject) getSelectedObject();
            switch (action) {
                case SERVER_INFO:
                    new PopupText(parent, true).show(db.getServerInfo());
                    break;
                case DATABASE_INFO:
                    new PopupText(parent, true).show(db.getInfo());
                    break;
                case DATABASE_BLACKBOX:
                    db.showBlackBox(parent);
                    break;
                case DATABASE_MONITOR:
                    db.monitor();
                    break;
            }
        }
        //===========================================================
    }


    //===============================================================
    /*
     *	A TAC Popup menu
     */
    //===============================================================
    private class TacPopupMenu extends JPopupMenu {
        private JTree tree;
        private final String[] menuLabels = {
                "Server Info",
                "Black  Box",
                "Manager Panel",
        };
        private final int OFFSET = 2;        //	Label And separator
        private final int SERVER_INFO = 0;
        private final int BLACKBOX = 1;
        private final int MANAGER_PANEL = 2;

        //===========================================================
        private TacPopupMenu(JTree tree) {
            this.tree = tree;

            //	Build menu
            JLabel title = new JLabel("Access Control Server :");
            title.setFont(new java.awt.Font("Dialog", Font.BOLD, 16));
            add(title);
            add(new JPopupMenu.Separator());
            for (String menuLabel : menuLabels) {
                JMenuItem btn = new JMenuItem(menuLabel);
                btn.addActionListener(this::treeActionPerformed);
                add(btn);
            }
        }

        //===========================================================		}
        public void showMenu(java.awt.event.MouseEvent evt) {

            Object obj = getSelectedObject();

            //	Add host name in menu label title
            JLabel lbl = (JLabel) getComponent(0);
            lbl.setText(obj.toString() + "  :");
            show(tree, evt.getX(), evt.getY());
            getComponent(OFFSET + MANAGER_PANEL).setEnabled(Astor.rwMode==AstorDefs.READ_WRITE);
        }

        //===========================================================
        private void treeActionPerformed(ActionEvent evt) {
            Object src = evt.getSource();
            int cmdidx = 0;
            for (int i = 0; i < menuLabels.length; i++)
                if (getComponent(OFFSET + i) == src)
                    cmdidx = i;

            //	Create a Popup text window
            TacObject tac = (TacObject) getSelectedObject();
            PopupText ppt = new PopupText(parent, true);
            try {
                switch (cmdidx) {
                    case SERVER_INFO:
                        ppt.show(tac.getServerInfo());
                        break;
                    case BLACKBOX:
                        tac.blackbox(parent);
                        break;
                    case MANAGER_PANEL:
                        startTACpanel();
                        break;
                }
            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
            }
        }
        //===========================================================
    }
}

