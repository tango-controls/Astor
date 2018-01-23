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

import admin.astor.tools.PopupTable;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DbServInfo;
import fr.esrf.TangoApi.DbServer;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

//===============================================================

/**
 * This class is a JTree to display architecture.
 *
 * @author Pascal Verdier
 */
//===============================================================

class LevelTree extends JTree implements AstorDefs {
    private HostInfoDialog parent;
    private DefaultTreeModel treeModel;
    private ServerPopupMenu serverMenu;
    private ServerPopupMenu levelMenu;
    private TangoHost host;
    private Level level;
    private DefaultMutableTreeNode root;

    private static Color treeBackground;
    private static final Color warningColor = new Color(0xffaa00);
    private static final Color selectionWarning = new Color(0xccaa66);
    //===============================================================
    //===============================================================
    LevelTree(JFrame jFrame, HostInfoDialog parent, TangoHost host, int level_row) {
        this.parent = parent;
        this.host = host;

        treeBackground = parent.getBackgroundColor();
        setBackground(treeBackground);
        serverMenu = new ServerPopupMenu(jFrame, parent, host, ServerPopupMenu.SERVERS);
        levelMenu = new ServerPopupMenu(jFrame, parent, host, ServerPopupMenu.LEVELS);

        level = new Level(level_row);
        initComponent();

        manageVisibility();
    }
    //===============================================================
    //===============================================================
    boolean hasRunningServer() {
        return level.hasRunningServer();
    }
    //===============================================================
    //===============================================================
    DevState getState() {
        return level.getState();
    }
    //===============================================================
    //===============================================================
    int getNbServers() {
        return level.size();
    }
    //===============================================================
    //===============================================================
    private void manageVisibility() {
        setVisible(level.size() > 0);    //	Display only if servers exist
    }
    //===============================================================
    //===============================================================
    TangoServer getServer(String serverName) {
        return level.getServer(serverName);
    }
    //===============================================================
    //===============================================================
    int getLevelRow() {
        return level.row;
    }
    //===============================================================
    //===============================================================
    List<TangoServer> getTangoServerList() {
        return level;
    }
    //===============================================================
    //===============================================================
    private void initComponent() {
        //Create the nodes.
        root = new DefaultMutableTreeNode(level);

        for (TangoServer server : level)
            root.add(new DefaultMutableTreeNode(server));

        //Create a tree that allows multi selection at a time.
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //	Create Tree and Tree model
        treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        // Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);
        setCellRenderer(new TangoRenderer());

        //	Add Action listener
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);
            }
        });
        //	Listen for collapse tree
        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent evt) {
                parent.packTheDialog();
            }

            public void treeExpanded(TreeExpansionEvent evt) {
                parent.packTheDialog();
            }
        });

        addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent evt) {
                selectionChanged(evt);
            }
        });

        //	Collapse
        if (level.row == LEVEL_NOT_CTRL)
            collapseTree();
    }
    //======================================================
    //======================================================
    @SuppressWarnings("UnusedParameters")
    private void selectionChanged(TreeSelectionEvent evt) {
        //parent.fireNewTreeSelection(this);
    }
    //======================================================
    //======================================================
    void checkUpdate() {
        level.updateServerList();
        manageVisibility();

        //	check if new server
        for (int i = 0; i < level.size(); i++) {
            TangoServer server = level.get(i);
            DefaultMutableTreeNode node = root;
            int nb = root.getChildCount();
            boolean found = false;
            for (int j = 0; !found && j < nb; j++) {
                node = node.getNextNode();
                TangoServer ts = (TangoServer) node.getUserObject();
                found = (ts == server);
            }
            if (!found) {
                //	Add a new node
                node = new DefaultMutableTreeNode(server);
                treeModel.insertNodeInto(node, root, i);
                setSelectionPath(new TreePath(node.getPath()));
                expandRow(i);
            }
        }

        //	check if some have disappeared
        DefaultMutableTreeNode node;
        for (int i = 0; i < root.getChildCount(); i++) {
            node = (DefaultMutableTreeNode) root.getChildAt(i);
            TangoServer server = (TangoServer) node.getUserObject();

            boolean found = false;
            for (int j = 0; !found && j < level.size(); j++)
                found = (server == level.get(j));

            if (!found) {
                //	Remove node
                treeModel.removeNodeFromParent(node);
                i--;
            }
        }
    }
    //===============================================================
    //===============================================================
    void changeChangeLevel(int level) {
        try {
            //	Get statup info for first server
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) root.getChildAt(0);
            TangoServer server = (TangoServer) node.getUserObject();
            DbServer s1 = new DbServer(server.getName());
            DbServInfo info = s1.get_info();

            PutServerInfoDialog dialog = new PutServerInfoDialog(parent, true);
            dialog.setLocation(getLocationOnScreen());
            String hostname = info.host;

            //	if OK put the new info to database
            if (dialog.showDialog(info, level) == PutServerInfoDialog.RET_OK) {
                System.out.println("Do it !");
                //	Apply
                info = dialog.getSelection();
                if (info!=null) {
                    info.host = hostname;
                    for (int i = 0; i < root.getChildCount(); i++) {
                        node = (DefaultMutableTreeNode) root.getChildAt(i);
                        server = (TangoServer) node.getUserObject();

                        info.name = server.getName();
                        server.putStartupInfo(info);
                        try {
                            Thread.sleep(20);
                        } catch (Exception e) { /* */}
                    }
                    parent.updateData();
                }
            }
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
    //===============================================================
    //===============================================================
    void changeServerLevels() {
        List<TangoServer> servers = new ArrayList<>();
        DefaultMutableTreeNode node;
        for (int i = 0; i < root.getChildCount(); i++) {
            node = (DefaultMutableTreeNode) root.getChildAt(i);
            servers.add((TangoServer) node.getUserObject());
        }

        for (TangoServer server : servers) {
            server.startupLevel(parent, host.getName(), getLocationOnScreen());
        }
        parent.updateData();
    }
    //======================================================
    //======================================================
    void resetSelection() {
        manageVisibility();
        setSelectionPath(new TreePath(root.getPath()));
    }
    //======================================================
    //======================================================
    void setSelection(TangoServer server) {
        manageVisibility();
        DefaultMutableTreeNode node;
        for (int i = 0; i < root.getChildCount(); i++) {
            node = (DefaultMutableTreeNode) root.getChildAt(i);
            TangoServer ts = (TangoServer) node.getUserObject();
            if (ts == server)
                setSelectionPath(new TreePath(node.getPath()));
        }
    }
    //======================================================
    //======================================================
    void expandTree() {
        expandRow(0);
    }
    //======================================================
    //======================================================
    void collapseTree() {
        collapseRow(0);
    }
    //======================================================
    //======================================================
    void toggleExpandCollapse() {
        if (isExpanded(0))
            collapseTree();
        else
            expandTree();
    }
    //======================================================
    //======================================================
    void displayUptime() {
        List<String[]> v = new ArrayList<>();
        try {
            for (int i = 0; i < root.getChildCount(); i++) {
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) root.getChildAt(i);
                TangoServer server = (TangoServer) node.getUserObject();
                String[] exportedStr = server.getServerUptime();
                v.add(new String[]{
                        server.getName(), exportedStr[0], exportedStr[1]});
            }


            String[] columns = new String[]{"Server", "Last   exported", "Last unexported"};
            String[][] table = new String[v.size()][];
            for (int i = 0; i < v.size(); i++)
                table[i] = v.get(i);
            PopupTable ppt = new PopupTable(parent, level.toString(),
                    columns, table, new Dimension(650, 250));
            ppt.setVisible(true);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }
    //======================================================
    /**
     * Manage event on clicked mouse on JTree object.
     *
     * @param evt mouse event.
     */
    //======================================================
    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
        //	Check if click is on a node
        if (getRowForLocation(evt.getX(), evt.getY()) < 0)
            return;

        TreePath selectedPath = getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath==null)
            return;
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount()-1);
        Object uo = node.getUserObject();
        int mask = evt.getModifiers();
        parent.fireNewTreeSelection(this);

        //	Display History if double click
        if ((mask & MouseEvent.BUTTON3_MASK) != 0) {
            if (uo instanceof TangoServer)
                serverMenu.showMenu(evt, this, (TangoServer) uo);
            else
                levelMenu.showMenu(evt, this, isExpanded(0));
        }
    }
    //===============================================================
    //===============================================================
    public String toString() {
        return level.toString();
    }
    //===============================================================
    //===============================================================






    //===============================================================
    /*
     * Startup Level class
     */
    //===============================================================
    private class Level extends ArrayList<TangoServer> {
        public int row;

        //==========================================================
        public Level(int row) {
            this.row = row;
            updateServerList();
        }
        //===========================================================
        private void updateServerList() {
            clear();
            for (int i = 0; i < host.nbServers(); i++) {
                try {
                    //	Check if in this level
                    TangoServer server = host.getServer(i);
                    if (server.startup_level == row)
                        add(server);
                } catch (NullPointerException e) {
                    ErrorPane.showErrorMessage(parent, null, e);
                }
            }
            //	Alphabetical order
            AstorUtil.getInstance().sortTangoServer(this);
        }
        //===========================================================
        TangoServer getServer(String servname) {
            for (TangoServer server : this) {
                if (server.getName().equals(servname))
                    return server;
            }
            return null;
        }
        //===========================================================
        boolean hasRunningServer() {
            for (TangoServer server : this) {
                if (server.getState() == DevState.ON)
                    return true;
            }
            return false;

        }
        //===========================================================
        DevState getState() {
            boolean is_faulty = false;
            boolean is_alarm = false;
            boolean is_moving = false;

            for (TangoServer server : this) {

                //	At least one unknown -> branch is unknown
                if (server.getState() == DevState.UNKNOWN)
                    return DevState.UNKNOWN;
                else if (server.getState() == DevState.FAULT)
                    is_faulty = true;
                else if (server.getState() == DevState.ALARM)
                    is_alarm = true;
                else if (server.getState() == DevState.MOVING)
                    is_moving = true;
            }
            //	Calculate branch state
            if (is_faulty)
                return DevState.FAULT;
            else if (is_moving)
                return DevState.MOVING;
            else if (is_alarm)
                return DevState.ALARM;
            else
                return DevState.ON;
        }
        //===========================================================
        public String toString() {
            String str;
            if (row != LEVEL_NOT_CTRL)
                str = "Level " + row;
            else
                str = "Not Controlled";
            return str;// + " ("+size()+")";
        }
        //===========================================================
    }


    //===============================================================
    /**
     * Renderer Class
     */
    //===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer {
        private final Font[] fonts = {
                new Font("Dialog", Font.BOLD, 16),
                new Font("Dialog", Font.PLAIN, 12),
        };
        //===============================================================
        public TangoRenderer() {}
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

            setBackgroundNonSelectionColor(treeBackground);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
            Object userObject = node.getUserObject();

            setFont(fonts[node.getLevel()]);

            if (row == 0) {
                //	ROOT (Level number)
                setIcon(getStateIcon(level.getState()));
                setBackgroundSelectionColor(treeBackground);
            } else if (userObject instanceof TangoServer) {
                TangoServer server = (TangoServer) userObject;
                setIcon(getStateIcon(server.getState()));
                if (server.getNbInstances()>1) {
                    setBackgroundNonSelectionColor(warningColor);
                    setBackgroundSelectionColor(selectionWarning);
                }
                else {
                    setBackgroundNonSelectionColor(treeBackground );
                    setBackgroundSelectionColor(Color.lightGray);
                }
            }
            return this;
        }
        //===============================================================
        private ImageIcon getStateIcon(DevState state) {
            int idx;
            if (state == DevState.MOVING)
                idx = moving;
            else
            if (state == DevState.STANDBY)
                idx = long_moving;
            else if (state == DevState.ON)
                idx = all_ok;
            else if (state == DevState.ALARM)
                idx = alarm;
            else
                idx = faulty;
            return AstorUtil.state_icons[idx];
        }
        //===============================================================
    }
}
