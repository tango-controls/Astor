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


package admin.astor.access;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class AllowedCmdTree extends JTree implements TangoConst {
    static ImageIcon tango_icon;
    static ImageIcon class_icon;
    static ImageIcon cmd_icon;

    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;
    private AllowedCmdTreePopupMenu menu;
    private JFrame parent;

    private AccessProxy access_dev;
    private static final Color background = Color.WHITE;

    //===============================================================
    //===============================================================
    public AllowedCmdTree(JFrame parent, AccessProxy access_dev) throws DevFailed {
        super();
        this.parent = parent;
        this.access_dev = access_dev;
        setBackground(background);
        buildTree();
        menu = new AllowedCmdTreePopupMenu(this);
    }

    //===============================================================
    //===============================================================
    private void buildTree() throws DevFailed {
        String str_root = "Tango Control Access";
        try {
            str_root = "Access to  " +
                    ApiUtil.get_db_obj().get_tango_host();
        } catch (DevFailed e) { /* Nothing to do */}

        //  Create the nodes.
        root = new DefaultMutableTreeNode(str_root);
        createClassNodes();

        //	Create the tree that allows one selection at a time.
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //	Create Tree and Tree model
        treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);

        //  Set the icon for leaf nodes.
        setCellRenderer(new TangoRenderer());

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
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);
            }
        });
    }
    //======================================================
    /*
     * Manage event on clicked mouse on JTree object.
     */
    //======================================================
    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
        if (access_dev.getAccessControl() == TangoConst.ACCESS_READ)
            return;

        //	Set selection at mouse position
        TreePath selectedPath = getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath == null)
            return;

        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount() - 1);
        Object o = node.getUserObject();
        int mask = evt.getModifiers();

        //  Check button clicked
        if ((mask & MouseEvent.BUTTON3_MASK) != 0) {
            if (node == root) {
                menu.showMenu(evt, (String) o);
            } else if (o instanceof String) {
                String parent_name = node.getParent().toString();
                String cmd = (String) o;

                if (!cmd.toLowerCase().equals("state") &&
                    !cmd.toLowerCase().equals("status"))
                    menu.showMenu(evt, parent_name, cmd);
            } else
                menu.showMenu(evt, (ClassAllowed) o);
        }
    }


    //===============================================================
    //===============================================================
    private void createClassNodes() throws DevFailed {
        try {
            //	Get classes where AllowedCommands property is defined
            DeviceData argout =
                    access_dev.command_inout("GetAllowedCommandClassList");
            String[] classlist = argout.extractStringArray();
            for (String classname : classlist) {
                //	Get each class allowed commands
                DeviceData argin = new DeviceData();
                argin.insert(classname);
                argout = access_dev.command_inout("GetAllowedCommands", argin);
                ClassAllowed cmd_class =
                        new ClassAllowed(classname, argout.extractStringArray());

                //	Build node for class with all commansdds as leaf
                DefaultMutableTreeNode node =
                        new DefaultMutableTreeNode(cmd_class);
                root.add(node);
                for (Object obj : cmd_class)
                    node.add(new DefaultMutableTreeNode(obj));
            }
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }

    //======================================================
    //======================================================
    DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) getLastSelectedPathComponent();
    }

    //======================================================
    //======================================================
    Object getSelectedObject() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null)
            return null;
        return node.getUserObject();
    }

    //===============================================================
    //===============================================================
    private String[] getDefinedClasses() {
        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < root.getChildCount(); i++)
            stringList.add(root.getChildAt(i).toString());
        return stringList.toArray(new String[stringList.size()]);
    }
    //===============================================================
    //===============================================================


    //===============================================================
//
//	Editing Tree (Add, remove...)
//
//===============================================================
    //===============================================================
    //===============================================================
    void addClass() {
        try {
            ListSelectionDialog dialog =
                    new ListSelectionDialog(parent, getDefinedClasses());
            if (dialog.showDialog() == JOptionPane.OK_OPTION) {
                ClassAllowed class_allowed =
                        new ClassAllowed(dialog.getSelection());
                DefaultMutableTreeNode new_node =
                        new DefaultMutableTreeNode(class_allowed);
                treeModel.insertNodeInto(new_node, root, root.getChildCount());
                for (Object cmd : class_allowed)
                    treeModel.insertNodeInto(new DefaultMutableTreeNode(cmd),
                            new_node, new_node.getChildCount());
                TreeNode[] tn = new DefaultMutableTreeNode[3];
                tn[0] = root;
                tn[1] = new_node;
                tn[2] = new_node.getChildAt(0); //	State
                TreePath tp = new TreePath(tn);
                setSelectionPath(tp);
                scrollPathToVisible(tp);
            }
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }

    //===============================================================
    //===============================================================
    void addCommand() {
        Object o = getSelectedObject();
        if (o instanceof ClassAllowed) {
            ClassAllowed cmd_class = null;
            String new_cmd = null;
            boolean added = false;
            try {
                cmd_class = (ClassAllowed) o;
                ListSelectionDialog dialog =
                        new ListSelectionDialog(parent, cmd_class);
                if (dialog.showDialog() == JOptionPane.OK_OPTION) {
                    //	Get the new command name and add to database
                    new_cmd = dialog.getSelection();
                    cmd_class.add(new_cmd);
                    added = true;
                    access_dev.addAllowedCommand(cmd_class);

                    //	Add class object to the tree
                    DefaultMutableTreeNode node = getSelectedNode();
                    DefaultMutableTreeNode new_node =
                            new DefaultMutableTreeNode(new_cmd);
                    treeModel.insertNodeInto(new_node, node, node.getChildCount());

                    //	open tree to see new node.
                    TreeNode[] tn = new DefaultMutableTreeNode[3];
                    tn[0] = root;
                    tn[1] = node;
                    tn[2] = new_node;
                    TreePath tp = new TreePath(tn);
                    setSelectionPath(tp);
                    scrollPathToVisible(tp);
                }
            } catch (DevFailed e) {
                if (added)
                    cmd_class.remove(new_cmd);
                ErrorPane.showErrorMessage(this, null, e);
            }
        }
    }

    //===============================================================
    //===============================================================
    void removeCommand() {
        Object o = getSelectedObject();
        if (o instanceof String) {
            String cmd = (String) o;
            DefaultMutableTreeNode node = getSelectedNode();
            DefaultMutableTreeNode parent_node =
                    (DefaultMutableTreeNode) node.getParent();
            if (parent_node.getUserObject() instanceof ClassAllowed) {
                ClassAllowed cmd_class =
                        (ClassAllowed) parent_node.getUserObject();

                if (JOptionPane.showConfirmDialog(this,
                        "Are you sure to want to remove " + cmd +
                                "  for class " + cmd_class,
                        "Confirm Dialog",
                        JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION)
                    return;

                try {
                    cmd_class.remove(cmd);
                    access_dev.addAllowedCommand(cmd_class);
                    treeModel.removeNodeFromParent(node);
                } catch (DevFailed e) {
                    ErrorPane.showErrorMessage(this, null, e);
                }
            }
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
        private final int CLASS = 1;
        private final int COMMAND = 2;

        //===============================================================
        //===============================================================
        public TangoRenderer() {
            Utils utils = Utils.getInstance();
            tango_icon = utils.getIcon("TangoClass.gif", 0.33);
            class_icon = utils.getIcon("TangoClass.gif", 0.125);
            cmd_icon = utils.getIcon("attleaf.gif");

            fonts = new Font[3];
            fonts[TITLE] = new Font("Dialog", Font.BOLD, 18);
            //	width fixed font
            fonts[CLASS] = new Font("Dialog", Font.BOLD, 12);
            fonts[COMMAND] = new Font("Dialog", Font.PLAIN, 12);
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

                if (node.getUserObject() instanceof ClassAllowed) {
                    setFont(fonts[CLASS]);
                    setIcon(class_icon);
                } else {
                    setFont(fonts[COMMAND]);
                    setIcon(cmd_icon);
                }
            }
            return this;
        }
    }//	End of Renderer Class

}
