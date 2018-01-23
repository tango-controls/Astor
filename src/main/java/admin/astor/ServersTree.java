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

import admin.astor.tools.Utils;
import fr.esrf.TangoApi.DbServInfo;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ServersTree extends JScrollPane implements AstorDefs {
    private Astor parent;
    public JTree tree;
    public TangoHost[] hosts;

    //===============================================================
    //===============================================================
    public ServersTree(Astor parent, String title, ArrayList servnames, ArrayList<DbServInfo[]> serverInfoList) {
        this.parent = parent;

        //	Build panel and its tree
        initComponent(title, servnames, serverInfoList);

    }

    //===============================================================
    //===============================================================
    private void initComponent(String title, ArrayList servnames, ArrayList<DbServInfo[]> serverInfoList) {

        //Create the nodes.
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(title);

        createNodes(root, servnames, serverInfoList);

        //Create a tree that allows one selection at a time.
        tree = new JTree(root);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //	Create Tree and Tree model
        //------------------------------------
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        tree.setModel(treeModel);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(tree);

        /*
           * Set the icon for leaf nodes.
           * Note: In the Swing 1.0.x release, we used
           * swing.plaf.basic.BasicTreeCellRenderer.
           */
        tree.setCellRenderer(new TangoRenderer());

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                serverSelectionPerformed(e);
            }
        });

        //	Add Action listener
        //------------------------------------
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);
            }
        });

        //	Add tree to scroll pane
        add(tree);
        setPreferredSize(new Dimension(280, 400));
        setViewportView(tree);
        setVisible(true);
    }

    private DbServInfo selection;

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    public void serverSelectionPerformed(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();

        if (node == null) return;
        Object obj = node.getUserObject();
        if (node.isLeaf()) {
            if (obj instanceof DbServInfo)
                selection = (DbServInfo) obj;
        }
    }
    //======================================================
    /*
     * Manage event on clicked mouse on PogoTree object.
     */
    //======================================================
    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
        //	Check if click is on a node
        if (tree.getRowForLocation(evt.getX(), evt.getY()) < 1)
            return;

        TreePath path = tree.getPathForLocation(evt.getX(), evt.getY());
        if (path==null) return;
        int mask = evt.getModifiers();
        //	Do something only if double click
        //-------------------------------------
        if (evt.getClickCount() == 2) {
            //	Check if btn1
            //------------------
            if ((mask & MouseEvent.BUTTON1_MASK) != 0)
                if (selection != null &&
                        path.getPathCount() - 2 == LEAF)
                    parent.tree.displayHostInfo();
        }
    }

    //===============================================================
    //===============================================================
    private void createNodes(DefaultMutableTreeNode root, ArrayList servnames, ArrayList<DbServInfo[]> serverInfoList) {
        DefaultMutableTreeNode[] collection =
                new DefaultMutableTreeNode[servnames.size()];

        for (int i = 0; i < servnames.size(); i++) {
            collection[i] = new DefaultMutableTreeNode(servnames.get(i));
            root.add(collection[i]);
        }

        //	Add instance nodes
        for (int i = 0; i < servnames.size(); i++) {
            for (DbServInfo[] serverInfoArray: serverInfoList) {
                for (DbServInfo info : serverInfoArray) {
                    String sname = (String) servnames.get(i);
                    if (info.name.contains(sname)) {
                        ServerInfo s = new ServerInfo(info);
                        DefaultMutableTreeNode instance =
                                new DefaultMutableTreeNode(s);
                        collection[i].add(instance);
                    }
                }
            }
        }
    }


    //===============================================================
    /**
     * Renderer Class
     */
    //===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer {
        ImageIcon tangoIcon;
        String tango_host = AstorUtil.getTangoHost();
        ImageIcon serverIcon;
        ImageIcon deviceIcon;
        Font[] fonts;

        private final int TITLE = 0;
        private final int LEAF = 1;

        //===============================================================
        //===============================================================
        public TangoRenderer() {

            tangoIcon = Utils.getInstance().getIcon("TransparentTango.gif", 0.15);
            serverIcon = Utils.getInstance().getIcon("server.gif");
            deviceIcon = Utils.getInstance().getIcon("device.gif");

            fonts = new Font[2];
            fonts[TITLE] = new Font("helvetica", Font.BOLD, 18);
            fonts[LEAF] = new Font("helvetica", Font.PLAIN, 12);
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

            setBackgroundNonSelectionColor(java.awt.Color.white);
            if (row == 0) {
                //	ROOT
                setBackgroundSelectionColor(java.awt.Color.white);
                setIcon(tangoIcon);
                setFont(fonts[TITLE]);
                setToolTipText(tango_host);
            } else {
                if (leaf) {
                    //	Instance object
                    setBackgroundSelectionColor(java.awt.Color.lightGray);
                    setIcon(deviceIcon);
                    setFont(fonts[LEAF]);
                    setToolTipText("Double click to popup host panel.");
                } else {
                    //	server collection
                    setBackgroundSelectionColor(java.awt.Color.white);
                    setIcon(serverIcon);
                    setFont(fonts[LEAF]);
                    setToolTipText("Server");
                }
            }
            return this;
        }
    }
}

