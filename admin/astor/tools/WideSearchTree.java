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

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class WideSearchTree extends JTree implements TangoConst {

    private DefaultMutableTreeNode root;
    private WideSearchTreePopupMenu menu;
    private JDialog parent;

    @SuppressWarnings("InspectionUsingGrayColors")
    private static final Color background = new Color(0xf0, 0xf0, 0xf0);

    //===============================================================
    //===============================================================
    public WideSearchTree(JDialog parent, String wildcard) throws DevFailed {
        super();
        this.parent = parent;
        setBackground(background);
        buildTree(wildcard);
        menu = new WideSearchTreePopupMenu(this);
        expandChildren(root);
        setSelectionPath(null);
    }

    //===============================================================
    //===============================================================
    private void buildTree(String wildcard) throws DevFailed {
        //  Create the nodes.
        root = new DefaultMutableTreeNode("Objects found for  " + wildcard);
        createCollectionClassNodes(wildcard);

        //	Create the tree that allows one selection at a time.
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //	Create Tree and Tree model
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);

        //  Set the icon for leaf nodes.
        TangoRenderer renderer = new TangoRenderer();
        setCellRenderer(renderer);

        //	Add Action listener
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);    //	for tree clicked, menu,...
            }
        });
    }

    //======================================================
    /*
      *	Manage event on clicked mouse on JTree object.
      */
    //======================================================
    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
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
            if (!(o instanceof TangoClass))
                menu.showMenu(evt, (LeafClass) o);
        }
    }

    //===============================================================
    //===============================================================
    private List<CollectionClass> initGlobalObject(String wildcard) throws DevFailed {
        List<CollectionClass> collectionClasses = new ArrayList<>();
        Database db = ApiUtil.get_db_obj();
        String[] classes = db.get_class_list(wildcard);
        if (classes.length > 0) {
            CollectionClass cc = new CollectionClass("Classes");
            for (String name : classes)
                cc.add(new TangoClass(name));
            collectionClasses.add(cc);
        }
        String[] servers = db.get_server_list(wildcard);
        if (servers.length > 0) {
            CollectionClass cc = new CollectionClass("Servers");
            for (String name : servers)
                cc.add(new TangoServer(name));
            collectionClasses.add(cc);
        }
        try {
            String[] devices = db.get_device_list(wildcard);
            if (devices.length > 0) {
                CollectionClass cc = new CollectionClass("Devices");
                for (String name : devices)
                    cc.add(new TangoDevice(name));
                collectionClasses.add(cc);
            }
        } catch (NoSuchMethodError e) {
            ((WideSearchDialog) parent).setWarning();
        } catch (DevFailed e) {
            if (e.errors[0].reason.equals("API_CommandNotFound"))
                ((WideSearchDialog) parent).setWarning();
            else
                throw e;
        }
        String[] aliases = db.get_device_alias_list(wildcard);
        if (aliases.length > 0) {
            CollectionClass cc = new CollectionClass("Aliases");
            for (String name : aliases)
                cc.add(new TangoAlias(name));
            collectionClasses.add(cc);
        }
        return collectionClasses;
    }

    //===============================================================
    //===============================================================
    private void createCollectionClassNodes(String wildcard) throws DevFailed {
        List<CollectionClass> collectionClasses = initGlobalObject(wildcard);
        if (collectionClasses.size() == 0)
            root.setUserObject("No Object Found for  " + wildcard);
        else
            for (CollectionClass aCollec : collectionClasses) {
                DefaultMutableTreeNode node =
                        new DefaultMutableTreeNode(aCollec);
                root.add(node);

                for (Object obj : aCollec)
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
    private DeviceInfo getDevInfo(String devname) throws DevFailed {
        DbDevice dev = new DbDevice(devname);
        return dev.get_info();
    }

    //===============================================================
    //===============================================================
    private String getAliasInfo(TangoAlias alias) throws DevFailed {
        StringBuilder sb = new StringBuilder("------------ ");
        sb.append(alias.name).append(" Info ------------\n\n");
        String deviceName = ApiUtil.get_db_obj().get_device_from_alias(alias.name);
        sb.append("Alias for device :   ").append(deviceName).append("\n\n");
        sb.append(getDeviceInfo(deviceName));
        return sb.toString();
    }

    //===============================================================
    //===============================================================
    private String getDeviceInfo(TangoDevice device) throws DevFailed {
        return getDeviceInfo(device.name);
    }

    //===============================================================
    //===============================================================
    private String getDeviceInfo(String devname) throws DevFailed {
        StringBuilder sb = new StringBuilder("------------ ");
        sb.append(devname).append("  Info ------------\n\n");
        DeviceInfo info = getDevInfo(devname);
        sb.append(info);
        return sb.toString();
    }

    //===============================================================
    //===============================================================
    private String getServerInfo(TangoServer server) throws DevFailed {
        StringBuilder sb = new StringBuilder("------------ ");
        sb.append(server.name).append("  Info ------------\n\n");
        DeviceInfo info = getDevInfo("dserver/" + server.name);
        sb.append(info);

        //	Query for served class(es) and device(s)
        DbServer db_serv = new DbServer(server.name);
        String[] class_list = db_serv.get_class_list();

        sb.append("\n\n----------- Device(s) Served -----------\n\n");
        for (String cl : class_list) {
            sb.append("\n").append(cl).append(":\n");
            String[] devnames = db_serv.get_device_name(cl);
            for (String devname : devnames)
                sb.append("   ").append(devname).append("\n");
        }
        return sb.toString();
    }

    //===============================================================
    //===============================================================
    private void displayInfo() {
        try {
            Object obj = getSelectedObject();
            if (obj instanceof TangoServer)
                new PopupText(parent, true).show(
                        getServerInfo((TangoServer) obj));
            else if (obj instanceof TangoDevice)
                new PopupText(parent, true).show(
                        getDeviceInfo((TangoDevice) obj));
            else if (obj instanceof TangoAlias)
                new PopupText(parent, true).show(
                        getAliasInfo((TangoAlias) obj));
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, "", e);
        }
    }

    //===============================================================
    //===============================================================
    private void displayHostPanel() {
        try {
            Object obj = getSelectedObject();
            String deviceName;
            if (obj instanceof TangoServer)
                deviceName = "dserver/" + ((TangoServer) obj).name;
            else if (obj instanceof TangoAlias)
                deviceName = ApiUtil.get_db_obj().get_device_from_alias(
                        ((TangoAlias) obj).name);
            else
            if (obj instanceof  TangoDevice)
                deviceName = ((TangoDevice) obj).name;
            else
                return;

            DeviceInfo info = getDevInfo(deviceName);
            String hostname = info.hostname;
            ((WideSearchDialog) parent).displayHostPanel(hostname);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, "", e);
        }
    }
    //===============================================================
    //===============================================================


    //===============================================================
    /*
      *	LeafClass object definition
      */
    //===============================================================
    private class LeafClass extends ArrayList {
        String name;

        //===========================================================
        private LeafClass(String name) {
            this.name = name;
        }

        //===========================================================
        public String toString() {
            return name;
        }
        //===========================================================
    }

    private class TangoClass extends LeafClass {
        private TangoClass(String name) {
            super(name);
        }
    }

    private class TangoServer extends LeafClass {
        private TangoServer(String name) {
            super(name);
        }
    }

    private class TangoDevice extends LeafClass {
        private TangoDevice(String name) {
            super(name);
        }
    }

    private class TangoAlias extends LeafClass {
        private TangoAlias(String name) {
            super(name);
        }
    }

    //===============================================================
    /*
      *	CollectionClass object definition
      */
    //===============================================================
    private class CollectionClass extends ArrayList<LeafClass> {
        String name;

        //===========================================================
        private CollectionClass(String name) {
            this.name = name;
        }

        //===========================================================
        public String toString() {
            return name;
        }
        //===========================================================
    }
    //===============================================================

    /**
     * Renderer Class
     */
    //===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer {
        private Font[] fonts;

        private final int TITLE = 0;
        private final int COLLEC = 1;
        private final int LEAF = 2;

        //===============================================================
        //===============================================================
        public TangoRenderer() {
            fonts = new Font[LEAF + 1];
            fonts[TITLE] = new Font("Dialog", Font.BOLD, 18);
            fonts[COLLEC] = new Font("Dialog", Font.BOLD, 12);
            fonts[LEAF] = new Font("Dialog", Font.PLAIN, 12);
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
                //setIcon(tango_icon);
            } else {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;

                if (node.getUserObject() instanceof CollectionClass) {
                    setFont(fonts[COLLEC]);
                    //setIcon(class_icon);
                } else {
                    setFont(fonts[LEAF]);
                    //setIcon(cmd_icon);
                }
            }
            return this;
        }
    }//	End of Renderer Class

    //==============================================================================
//==============================================================================
    static private final int DISPLAY_INFO = 0;
    static private final int HOST_PANEL = 1;
    static private final int OFFSET = 2;    //	Label And separator

    static private String[] menuLabels = {
            "Display Info",
            "Host Panel",
    };

    private class WideSearchTreePopupMenu extends JPopupMenu {
        private JTree tree;
        private JLabel title;

        private WideSearchTreePopupMenu(JTree tree) {
            this.tree = tree;
            buildBtnPopupMenu();
        }
        //=======================================================

        /**
         * Create a Popup menu for host control
         */
        //=======================================================
        private void buildBtnPopupMenu() {
            title = new JLabel();
            title.setFont(new java.awt.Font("Dialog", 1, 16));
            add(title);
            add(new JPopupMenu.Separator());

            for (String menuLabel : menuLabels) {
                if (menuLabel == null)
                    add(new Separator());
                else {
                    JMenuItem btn = new JMenuItem(menuLabel);
                    btn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            hostActionPerformed(evt);
                        }
                    });
                    add(btn);
                }
            }
        }

        //======================================================
        /*
           *	Show menu on Device
           */
        //======================================================
        public void showMenu(MouseEvent evt, LeafClass leaf) {
            //	Set selection at mouse position
            TreePath selectedPath =
                    tree.getPathForLocation(evt.getX(), evt.getY());
            if (selectedPath == null)
                return;
            tree.setSelectionPath(selectedPath);

            title.setText(leaf.toString());
            show(tree, evt.getX(), evt.getY());
        }

        //======================================================
        private void hostActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int cmdidx = 0;
            for (int i = 0; i < menuLabels.length; i++)
                if (getComponent(OFFSET + i) == obj)
                    cmdidx = i;

            switch (cmdidx) {
                case DISPLAY_INFO:
                    displayInfo();
                    break;
                case HOST_PANEL:
                    displayHostPanel();
                    break;
            }
        }
    }
}
