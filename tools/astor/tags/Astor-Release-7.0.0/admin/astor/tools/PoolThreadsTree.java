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


package admin.astor.tools;

import admin.astor.TangoServer;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;


public class PoolThreadsTree extends JTree implements TangoConst {
    static ImageIcon tango_icon;
    static ImageIcon class_icon;
    static ImageIcon cmd_icon;

    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;
    private JDialog parent;
    private PoolThreadsInfo threadsInfo;
    private TangoRenderer renderer;

    private TangoServer server;
    private static final Color background = Color.WHITE;
    private static final             String[]    propertyNames = {
            "polling_threads_pool_size",
            "polling_threads_pool_conf",
    };
    private static final int NB_THREADS = 0;
    private static final int THREADS_CONFIG = 1;
    private static final int LINE_MAX_LENGTH = 256;
    //===============================================================
    //===============================================================
    public PoolThreadsTree(JDialog parent, TangoServer server) throws DevFailed {
        super();
        this.parent = parent;
        this.server = server;
        setBackground(background);

        threadsInfo = new PoolThreadsInfo(server);
        buildTree();
        expandChildren(root);
        setSelectionPath(null);

        //	Enable Drag and Drop
        this.setDragEnabled(true);
        setTransferHandler(new TransferHandler("Text"));
    }
    //===============================================================
    //===============================================================
    private void buildTree() {
        //  Create the nodes.
        root = new DefaultMutableTreeNode(server);
        createThreadsNodes();

        //	Create the tree that allows one selection at a time.
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //	Create Tree and Tree model
        treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);

        //  Set the icon for leaf nodes.
        renderer = new TangoRenderer();
        setCellRenderer(renderer);

        //	Listen for collapse tree
        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
                //collapsedPerformed(e);
            }

            public void treeExpanded(TreeExpansionEvent e) {
                //expandedPerformed(e);
            }
        });
        //	Add Action listener
        addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                treeMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                treeMouseReleased(evt);
            }

            /*
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);
            }
            */
        });
    }

    //======================================================
    /*
     *	Manage event on clicked mouse on JTree object.
     */
    //======================================================
        /*
    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
        //	Set selection at mouse position
        TreePath selectedPath = getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath == null)
            return;
        int mask = evt.getModifiers();

        //  Check button clicked
        if (evt.getClickCount() == 2 && (mask & MouseEvent.BUTTON1_MASK) != 0) {
        } else if ((mask & MouseEvent.BUTTON3_MASK) != 0) {
        }
    }
        */

    //===============================================================
    //===============================================================
    private void createThreadsNodes() {
        for (int i = 0; i < threadsInfo.size(); i++) {
            PollThread thread = threadsInfo.threadAt(i);
            //	Build node for class with all commansdds as leaf
            DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode(thread);
            root.add(node);
            for (Object obj : thread)
                node.add(new DefaultMutableTreeNode(obj));
        }
    }

    //======================================================
    //======================================================
    private DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) getLastSelectedPathComponent();
    }

    //======================================================
    //======================================================
    private Object getSelectedObject() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null)
            return null;
        return node.getUserObject();
    }

    //===============================================================
    //===============================================================
    boolean selectedObjectIsThread() {
        return (getSelectedObject() instanceof PollThread);
    }

    //===============================================================
    //===============================================================
    private void expandChildren(DefaultMutableTreeNode node) {
        boolean level_done = false;
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child =
                    (DefaultMutableTreeNode) node.getChildAt(i);
            if (child.isLeaf()) {
                if (!level_done) {
                    expandNode(child);
                    level_done = true;
                }
            } else
                expandChildren(child);
        }
    }

    //===============================================================
    //===============================================================
    private void expandNode(DefaultMutableTreeNode node) {
        List<DefaultMutableTreeNode> nodeList = new ArrayList<>();
        nodeList.add(node);
        while (node != root) {
            node = (DefaultMutableTreeNode) node.getParent();
            nodeList.add(0, node);
        }
        TreeNode[] tn = new DefaultMutableTreeNode[nodeList.size()];
        for (int i = 0; i < nodeList.size(); i++)
            tn[i] = nodeList.get(i);
        TreePath tp = new TreePath(tn);
        setSelectionPath(tp);
        scrollPathToVisible(tp);
    }

    //===============================================================
    //===============================================================
    private DefaultMutableTreeNode getFutureSelectedNode(DefaultMutableTreeNode node) {
        //	Get the future selected node, after remove.
        DefaultMutableTreeNode parent_node =
                (DefaultMutableTreeNode) node.getParent();
        DefaultMutableTreeNode ret_node = parent_node;
        for (int i = 0; i < parent_node.getChildCount(); i++) {
            DefaultMutableTreeNode child_node =
                    (DefaultMutableTreeNode) parent_node.getChildAt(i);
            if (child_node == node) {
                if (i == parent_node.getChildCount() - 1) {
                    if (i > 0)
                        ret_node = (DefaultMutableTreeNode) parent_node.getChildAt(i - 1);
                } else
                    ret_node = (DefaultMutableTreeNode) parent_node.getChildAt(i + 1);
            }
        }
        return ret_node;
    }

    //===============================================================
    //===============================================================
    void removeThread() {
        DefaultMutableTreeNode node = getSelectedNode();

        if (node != null) {
            Object obj = node.getUserObject();
            if (obj instanceof PollThread) {
                //	Check if device(s) associated.
                if (node.getChildCount() == 0) {
                    //	get future selected node
                    DefaultMutableTreeNode next_node = getFutureSelectedNode(node);
                    //	Remove selected one
                    treeModel.removeNodeFromParent(node);
                    PollThread pt = (PollThread) obj;
                    threadsInfo.remove(pt);
                    //	And select the found node
                    TreeNode[] tree_node = next_node.getPath();
                    TreePath path = new TreePath(tree_node);
                    setSelectionPath(path);
                    scrollPathToVisible(path);
                } else
                    Utils.popupError(parent, "Cannot remove a not empty thread !");
            }
        }
    }

    //===============================================================
    //===============================================================
    DefaultMutableTreeNode addThreadNode() {

        PollThread new_thread = new PollThread(getNextThreadNum());
        DefaultMutableTreeNode node =
                new DefaultMutableTreeNode(new_thread);
        treeModel.insertNodeInto(node, root, root.getChildCount());
        return node;
    }

    //===============================================================
    //===============================================================
    private int getNextThreadNum() {
        int num = 0;
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode th_node =
                    (DefaultMutableTreeNode) root.getChildAt(i);
            num = ((PollThread) th_node.getUserObject()).num;
        }
        return ++num;
    }

    //===============================================================
    //===============================================================
    private List<String> manageMaxLength(List<String> lines) {
        List<String>   list = new ArrayList<>();
        for (String line : lines) {
            while (line.length()>LINE_MAX_LENGTH) {
                list.add(line.substring(0, LINE_MAX_LENGTH-1)+'\\');
                line = line.substring(LINE_MAX_LENGTH-1);
            }
            list.add(line);
        }
        return list;
    }
    //===============================================================
    //===============================================================
    void putPoolThreadInfo() {
        //  ToDo
        //	Get configuration from tree
        int nbThreads = root.getChildCount();
        List<String> lines = new ArrayList<>();
        for (int i=0 ; i<root.getChildCount() ; i++) {
            DefaultMutableTreeNode threadNode =
                    (DefaultMutableTreeNode) root.getChildAt(i);
            int deviceNumber = threadNode.getChildCount();
            if (deviceNumber > 0) {
                String s = "";
                for (int j=0 ; j<deviceNumber ; j++) {
                    s += threadNode.getChildAt(j).toString();
                    if (j < deviceNumber-1)
                        s += ",";
                }
                lines.add(s);
            }
        }
        //  Check for maximum length of lines
        lines  = manageMaxLength(lines);

        //	Convert tree to device(admin) property.
        String[] config = new String[lines.size()];
        for (int i=0 ; i<lines.size() ; i++)
            config[i] = lines.get(i);

        //	And send it to database.
        try {
            DbDatum[] argin = new DbDatum[2];
            argin[0] = new DbDatum(propertyNames[NB_THREADS], nbThreads);
            argin[1] = new DbDatum(propertyNames[THREADS_CONFIG], config);
            server.put_property(argin);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }
    //===============================================================
    //===============================================================

    //===============================================================
    /**
     * Drag and Drop management
     */
    //===============================================================
    private DefaultMutableTreeNode dragged_node = null;

    //======================================================
    //======================================================
    private TreePath getUpperPath(int x, int y) {
        TreePath selectedPath = null;
        while (selectedPath == null && y > 10) {
            selectedPath = getPathForLocation(x, y);
            y -= 10;
        }
        return selectedPath;
    }

    //======================================================
    //======================================================
    private void treeMouseReleased(java.awt.event.MouseEvent evt) {
        int mask = evt.getModifiers();
        if ((mask & MouseEvent.BUTTON1_MASK) != 0) {
            if (dragged_node == null)
                return;
            TreePath selectedPath = getPathForLocation(evt.getX(), evt.getY());
            if (selectedPath == null)
                if ((selectedPath = getUpperPath(evt.getX(), evt.getY())) == null)
                    return;
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount() - 1);
            Object o = node.getUserObject();
            int pos = 0;
            if (o instanceof String) {
                DefaultMutableTreeNode p_node
                        = (DefaultMutableTreeNode) node.getParent();
                pos = p_node.getIndex(node);
                node = p_node;
            }
            moveLeaf(node, dragged_node, pos);
            dragged_node = null;
            Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
            parent.setCursor(cursor);
        }
    }

    //======================================================
    //======================================================
    private void treeMousePressed(java.awt.event.MouseEvent evt) {
        int mask = evt.getModifiers();
        if ((mask & MouseEvent.BUTTON1_MASK) != 0) {
            TreePath selectedPath = getPathForLocation(evt.getX(), evt.getY());
            if (selectedPath == null)
                return;
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount() - 1);
            Object o = node.getUserObject();
            if (o instanceof String) {
                TransferHandler transfer = this.getTransferHandler();
                transfer.exportAsDrag(this, evt, TransferHandler.COPY);
                dragged_node = node;
                parent.setCursor(renderer.getNodeCursor(node));
            }
        }
    }

    //===============================================================
    //===============================================================
    private void moveLeaf(DefaultMutableTreeNode collec_node, DefaultMutableTreeNode leaf_node, int pos) {
        Object obj = collec_node.getUserObject();
        if (obj instanceof PollThread) {
            treeModel.removeNodeFromParent(leaf_node);
            if (pos < 0)
                treeModel.insertNodeInto(leaf_node, collec_node, collec_node.getChildCount());
            else
                treeModel.insertNodeInto(leaf_node, collec_node, pos);

            expandNode(leaf_node);
        }
    }
    //===============================================================
    //===============================================================
    PoolThreadsTree(String serverName) throws DevFailed {
        //  Just to be used for threads pool info without display
        server = new TangoServer(serverName);
        threadsInfo = new PoolThreadsInfo(new TangoServer(serverName));
    }
    //===============================================================
    //===============================================================
    public int getNbThreads() throws DevFailed {
        return threadsInfo.size();
    }
    //===============================================================
    //===============================================================


    //===============================================================
    /*
      *	Polling thread object
      */
    //===============================================================
    private class PollThread extends ArrayList<String> {
        String name;
        int num;

        //===========================================================
        private PollThread(int num) {
            this.num = num;
            this.name = "Thread " + (num + 1);
        }

        //===========================================================
        public String toString() {
            return name;
        }
        //===========================================================
    }

    //===============================================================
    /*
      *	Pool of polling threads info object
      */
    //===============================================================
    private class PoolThreadsInfo extends ArrayList<PollThread> {

        //===========================================================
        private PoolThreadsInfo(TangoServer server) throws DevFailed {
            DbDatum[] data = server.get_property(propertyNames);
            String[] config = new String[0];
            int threadsNumber = 1;
            if (data[NB_THREADS].is_empty() && data[THREADS_CONFIG].is_empty()) {
                //	If no property --> get device list from db
                String[] s = server.queryDeviceFromDb();
                //	and set all for on thread
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < s.length; i++) {
                    sb.append(s[i]);
                    if (i < s.length - 1)
                        sb.append(',');
                }
                config = new String[]{ sb.toString() };
            }
            if (!data[NB_THREADS].is_empty())
                threadsNumber = data[NB_THREADS].extractLong();
            if (!data[THREADS_CONFIG].is_empty())
                config = data[THREADS_CONFIG].extractStringArray();
            buildConfig(config, threadsNumber);
        }

        //===========================================================
        private List<String> rebuildLines(String[] config) {
            List<String>   lines = new ArrayList<>();
            String line = config[0];
            for (int i=1 ; i<config.length ; i++) {
                if (line.endsWith("\\")) {
                    //  Append before '\'
                    line = line.substring(0, line.indexOf('\\')) + config[i];
                }
                else {
                    lines.add(line);
                    line = config[i];
                }
            }
            lines.add(line);
            return lines;
        }
        //===========================================================
        private void buildConfig(String[] config, int threadsNumber) {
            if (config.length==0) //  Do nothing
                return;
            //  first time rebuild lines (ended by '\'
            List<String>   lines = rebuildLines(config);

            int threadCounter = 0;
            for (String line : lines) {
                StringTokenizer stk = new StringTokenizer(line, ",");
                PollThread thread = new PollThread(threadCounter++);
                while (stk.hasMoreTokens())
                    thread.add(stk.nextToken());
                add(thread);
            }
            for (int i=threadCounter ; i<threadsNumber ; i++)
                add(new PollThread(i));
        }

        //===========================================================
        private PollThread threadAt(int i) {
            return get(i);
        }
    }
    //===============================================================
    //===============================================================


    //===============================================================

    /**
     * Renderer Class
     */
    //===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer {
        private Font[] fonts;

        private final int TITLE = 0;
        private final int THREAD = 1;
        private final int DEVICE = 2;
        private Cursor dd_cursor;

        //===============================================================
        //===============================================================
        public TangoRenderer() {
            Utils utils = Utils.getInstance();
            tango_icon = utils.getIcon("TangoClass.gif", 0.33);
            class_icon = utils.getIcon("class.gif");
            cmd_icon = utils.getIcon("attleaf.gif");
            dd_cursor = utils.getCursor("drg-drp.gif");

            fonts = new Font[3];
            fonts[TITLE] = new Font("Dialog", Font.BOLD, 18);
            //	width fixed font
            fonts[THREAD] = new Font("Dialog", Font.BOLD, 12);
            fonts[DEVICE] = new Font("Dialog", Font.PLAIN, 12);
        }

        //===============================================================
        //===============================================================
        Cursor getNodeCursor(DefaultMutableTreeNode node) {
            Object o = node.getUserObject();
            if (o instanceof String)
                return dd_cursor;
            return new Cursor(Cursor.DEFAULT_CURSOR);
        }

        //===============================================================
        //===============================================================
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object obj,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, obj, sel,
                    expanded, leaf, row,
                    hasFocus);

            setBackgroundNonSelectionColor(background);
            setForeground(Color.black);
            setBackgroundSelectionColor(Color.lightGray);
            if (row == 0) {
                //	ROOT
                setFont(fonts[TITLE]);
                setIcon(tango_icon);
            } else {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;

                if (node.getUserObject() instanceof PollThread) {
                    setFont(fonts[THREAD]);
                    setIcon(class_icon);
                } else {
                    setFont(fonts[DEVICE]);
                    setIcon(cmd_icon);
                }
            }
            return this;
        }
    }//	End of Renderer Class
//==============================================================================
//==============================================================================
}
