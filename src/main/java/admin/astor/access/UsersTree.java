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

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
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
import java.util.Comparator;
import java.util.StringTokenizer;


public class UsersTree extends JTree implements TangoConst {
    static final int USER_NODE = -2;
    static final int COLLECTION = -1;
    static final int ADDRESS = 0;
    static final int DEVICE = 1;
    static final String[] collecStr = {"Allowed Addresses", "Devices"};


    static final int WRITE = 0;
    static final int READ = 1;
    static final String[] rightsStr = {"write", "read"};

    static ImageIcon tango_icon;
    static ImageIcon all_users_icon;
    static ImageIcon group_icon;
    static ImageIcon user_icon;
    static ImageIcon add_icon;
    static ImageIcon dev_icon;
    static ImageIcon write_icon;
    static ImageIcon read_icon;


    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;
    private UsersTreePopupMenu menu;
    private JFrame parent;

    private AccessProxy accessProxy;
    List<AccessAddress> copiedAddresses = new ArrayList<>();
    List<AccessDevice> copiedDevices = new ArrayList<>();
    private static final Color background = Color.WHITE;


    private List<UserGroup>  groups = new ArrayList<>();
    //===============================================================
    //===============================================================
    public UsersTree(JFrame parent, AccessProxy access_proxy) throws DevFailed {
        super();
        this.parent = parent;
        this.accessProxy = access_proxy;
        setBackground(background);
        buildTree();
        menu = new UsersTreePopupMenu(this);
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
        createUserNodes();

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
                expandedPerformed(e);
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
    /**
     * Manage event on clicked mouse on JTree object.
     *
     * @param evt the mouse event
     */
    //======================================================
    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
        if (accessProxy.getAccessControl() == TangoConst.ACCESS_READ)
            return;

        //	Set selection at mouse position
        TreePath selectedPath = getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath == null) return;

        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount() - 1);
        Object o = node.getUserObject();
        int mask = evt.getModifiers();

        //  Check button clicked
        if (evt.getClickCount() == 2 && (mask & MouseEvent.BUTTON1_MASK) != 0) {
            if (o instanceof AccessAddress ||
                    o instanceof AccessDevice)
                editItem();
        } else if ((mask & MouseEvent.BUTTON3_MASK) != 0) {
            if (node == root)
                menu.showMenu(evt, o.toString());
            else if (o instanceof UserGroup)
                menu.showMenu(evt, o.toString());
            else if (o instanceof AccessAddress)
                menu.showMenu(evt, ADDRESS, o);
            else if (o instanceof AccessDevice)
                menu.showMenu(evt, DEVICE, o);
            else if (o instanceof AccessUser)
                menu.showMenu(evt, USER_NODE, o);
            else if (o instanceof String)
                menu.showMenu(evt, COLLECTION, o);
        }
    }

    //===============================================================
    //===============================================================
    public void expandedPerformed(TreeExpansionEvent evt) {
        if (!manage_expand)
            return;
        //	Get path
        TreePath tp = evt.getPath();
        Object[] path = tp.getPath();
        if (path.length < 2)
            return;

        //	Get concerned node
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) tp.getPathComponent(path.length - 1);

        if (node.getUserObject() instanceof AccessUser)
            expendUserNode(node.toString());
        else {
            if (node.toString().equals(collecStr[ADDRESS]))
                createAddressNodes(node);
            else
            if (node.toString().equals(collecStr[DEVICE]))
                createDeviceNodes(node);
        }
    }

    //===============================================================
    //===============================================================
    private void createDeviceNodes(DefaultMutableTreeNode node) {
        try {
            DefaultMutableTreeNode d_node;
            Object obj =
                    ((DefaultMutableTreeNode) node.getParent()).getUserObject();
            String user = ((AccessUser) obj).getName();
            AccessDevice[] devices = getDevices(user);
            for (int i = 0; i < devices.length; i++) {
                //	Create a node for devices
                d_node = new DefaultMutableTreeNode(devices[i]);
                treeModel.insertNodeInto(d_node, node, i);
            }
            removePreviousNode(node, devices.length);
        } catch (DevFailed e) {
            removePreviousNode(node, 0);
            ErrorPane.showErrorMessage(parent,
                    "Cannot read devices", e);
        }
    }

    //===============================================================
    //===============================================================
    private void createAddressNodes(DefaultMutableTreeNode node) {
        try {
            DefaultMutableTreeNode a_node;
            Object obj =
                    ((DefaultMutableTreeNode) node.getParent()).getUserObject();
            String user = ((AccessUser) obj).getName();
            AccessAddress[] addresses = getAddresses(user);

            //  Check if something has changed.
            if (!createChildNodes(node, addresses))
                return;

            for (int i = 0; i < addresses.length; i++) {
                //	Create a node for addresses
                a_node = new DefaultMutableTreeNode(addresses[i]);
                treeModel.insertNodeInto(a_node, node, i);
            }
            removePreviousNode(node, addresses.length);
        } catch (DevFailed e) {
            removePreviousNode(node, 0);
            ErrorPane.showErrorMessage(parent,
                    "Cannot read addresses", e);
        }
    }

    //===============================================================
    //===============================================================
    private void createUserNode(String name, DefaultMutableTreeNode groupNode) {
        DefaultMutableTreeNode userNode;
        DefaultMutableTreeNode addrNode;
        DefaultMutableTreeNode dummyNode;

        userNode = new DefaultMutableTreeNode(new AccessUser(name));
        addrNode = new DefaultMutableTreeNode(collecStr[ADDRESS]);
        dummyNode = new DefaultMutableTreeNode(collecStr[DEVICE]);
        addrNode.add(new DefaultMutableTreeNode(new Dummy()));
        dummyNode.add(new DefaultMutableTreeNode(new Dummy()));
        userNode.add(addrNode);
        userNode.add(dummyNode);
        if (groupNode==null)
            root.add(userNode);
        else
            groupNode.add(userNode);
    }

    //===============================================================
    //===============================================================
    private void createUserNodes() throws DevFailed {
        String[] users = accessProxy.getUsers();
        buildGroups(users);
        int ratio = 80 / users.length;

        //  Do the first one ("*")
        createUserNode(users[0], null);
        //  Do groups
        for (UserGroup group : groups) {
            DefaultMutableTreeNode  groupNode = new DefaultMutableTreeNode(group);
            for (String user : group) {
                AstorUtil.increaseSplashProgress(
                        ratio, "building tree for " + user);
                createUserNode(user, groupNode);
            }
            root.add(groupNode);
        }
    }
    //===============================================================
    //===============================================================
    private void buildGroups(String[] users) {
        //  Get defined group/users from DB
        groups = UserGroup.getUserGroupsFromDatabase(users);

        //  Build a dummy group for non defined users
        UserGroup   unsorted = new UserGroup(UserGroup.unsorted);
        for (String user : users) {
            boolean found = false;
            if (!user.equals("*")) {
                for (UserGroup group : groups) {
                    for (String member : group) {
                        if (user.equals(member)) {
                            found = true;
                        }
                    }
                }
                if (!found)
                    unsorted.add(user);
            }
        }
        if (unsorted.size()>0) {
            unsorted.sortMembers();
            groups.add(unsorted);
        }
    }
    //===============================================================
    //===============================================================
    private boolean createChildNodes(DefaultMutableTreeNode node, AccessAddress[] address) {
        boolean create = false;
        if (node.getChildCount() != address.length)
            create = true;
        else
            for (int i = 0; i < address.length; i++)
                if (!node.getChildAt(i).toString().equals(address[i].toString()))
                    create = true;
        return create;
    }

    //===============================================================
    //===============================================================
    private void removePreviousNode(DefaultMutableTreeNode node, int offset) {
        while (node.getChildCount() > offset) {
            DefaultMutableTreeNode leaf =
                    (DefaultMutableTreeNode) node.getChildAt(offset);
            treeModel.removeNodeFromParent(leaf);
        }
    }

    //===============================================================
    //===============================================================
    private AccessDevice[] getDevices(String user) throws DevFailed {
        String[] result = accessProxy.getDevicesByUser(user);

        AccessDevice[] ret = new AccessDevice[result.length / 2];
        for (int i = 0; i < result.length / 2; i++)
            ret[i] = new AccessDevice(result[2 * i], result[2 * i + 1]);
        return ret;
    }

    //===============================================================
    //===============================================================
    private AccessAddress[] getAddresses(String user) throws DevFailed {
        String[] result = accessProxy.getAddressesByUser(user);

        AccessAddress[] ret = new AccessAddress[result.length];
        for (int i = 0; i < result.length; i++)
            ret[i] = new AccessAddress(result[i]);
        return ret;
    }

    //======================================================
    //======================================================
    DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) getLastSelectedPathComponent();
    }

    //======================================================
    //======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    Object getSelectedObject() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null)
            return null;
        return node.getUserObject();
    }


    //===============================================================
    //
    //	Editing Tree (Add, remove...)
    //
    //===============================================================
    private boolean checkUserName(String user) {
        boolean ok = true;
        //  Check if user OK
        if (user.length() > 0) {
            StringTokenizer stk = new StringTokenizer(user.toLowerCase());
            StringBuilder sb = new StringBuilder();
            while (stk.hasMoreTokens())
                sb.append(stk.nextToken());
            if (sb.toString().equals("allusers")) {
                TangoAccess.popupError(this, user + " is reserved !");
                ok = false;
            }
        } else
            ok = false;

        //  Check if user already exists
        if (!user.isEmpty()) {
            if (userExists(user)) {
                ok = false;
                expendUserNode(user);
                TangoAccess.popupError(this, "User  " + user + "  Already exists !");
            }
        } else
            ok = false;
        return ok;
    }

    //===============================================================
    //===============================================================
    void addUser() {
        String userName = "";
        String address = "";
        String devname = "*/*/*";

        //  Get default group if any
        Object  obj = getSelectedObject();
        UserGroup   userGroup = null;
        if (obj instanceof UserGroup)
            userGroup = (UserGroup) obj;

        //  Display dialog to get user and address
        boolean ok = false;
        while (!ok) {
            EditDialog dlg = new EditDialog(parent, userName, address, groups, userGroup);
            if (dlg.showDialog() != JOptionPane.OK_OPTION)
                return;

            userGroup = dlg.getUserGroup();
            if (userGroup!=null) {
                String[] str = dlg.getInputs();

                userName = str[EditDialog.USER];
                address = str[EditDialog.ADDRESS];
                ok = checkUserName(userName);
            }
        }

        //  If does not already exist, create it
        try {
            accessProxy.addAddress(userName, address);
            accessProxy.addDevice(userName, devname, rightsStr[WRITE]);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
            return;
        }

        //  Get the group node
        DefaultMutableTreeNode groupNode = getGroupNode(userGroup.getName());
        if (groupNode==null) {
            //  It is a new group
            groups.add(userGroup);
            groups.sort(new GroupComparator());
            groupNode = new DefaultMutableTreeNode(userGroup);
            treeModel.insertNodeInto(groupNode, root, root.getChildCount());
        }
        userGroup.add(userName);


        //  Update group list and put in DB
        UserGroup.setUserGroupsToDatabase(this, groups);

        //  Build nodes
        DefaultMutableTreeNode new_user_node =
                new DefaultMutableTreeNode(new AccessUser(userName));
        DefaultMutableTreeNode new_str_add_node =
                new DefaultMutableTreeNode(collecStr[ADDRESS]);
        DefaultMutableTreeNode new_str_dev_node =
                new DefaultMutableTreeNode(collecStr[DEVICE]);
        DefaultMutableTreeNode new_add_node =
                new DefaultMutableTreeNode(new AccessAddress(address));
        DefaultMutableTreeNode new_dev_node =
                new DefaultMutableTreeNode(new AccessDevice(devname, WRITE));

        //  And insert in tree
        treeModel.insertNodeInto(new_user_node, groupNode, groupNode.getChildCount());
        treeModel.insertNodeInto(new_str_add_node, new_user_node, 0);
        treeModel.insertNodeInto(new_str_dev_node, new_user_node, 1);
        treeModel.insertNodeInto(new_add_node, new_str_add_node, 0);
        treeModel.insertNodeInto(new_dev_node, new_str_dev_node, 0);

        //  Expend to show new user
        TreeNode[] path = new TreeNode[5];
        path[0] = root;
        path[1] = groupNode;
        path[2] = new_user_node;
        path[3] = new_str_add_node;
        path[4] = new_add_node;
        TreePath tp = new TreePath(path);
        setSelectionPath(tp);

        path[3] = new_str_dev_node;
        path[4] = new_dev_node;
        tp = new TreePath(path);
        setSelectionPath(tp);
        scrollPathToVisible(tp);
    }

    //===============================================================
    //===============================================================
    private DefaultMutableTreeNode getGroupNode(String groupName) {

        for (int i=0 ; i<root.getChildCount() ; i++) {
            if (root.getChildAt(i).toString().equals(groupName))
                return (DefaultMutableTreeNode) root.getChildAt(i);
        }
        return null;
    }
    //===============================================================
    //===============================================================
    private boolean manage_expand = true;

    void addItem() {
        DefaultMutableTreeNode node = getSelectedNode();
        Object o = node.getUserObject();
        int obj_type;
        if (o.toString().equals(collecStr[ADDRESS]))
            obj_type = ADDRESS;
        else if (o.toString().equals(collecStr[DEVICE]))
            obj_type = DEVICE;
        else
            return;

        DefaultMutableTreeNode new_node;
        int i = 0;
        TreeNode[] path = new DefaultMutableTreeNode[5];
        TreePath tp;
        path[i++] = root;
        path[i++] = node.getParent().getParent();   //  Group
        path[i++] = node.getParent();               //  User
        path[i++] = node;                           //  Address/Device

        //  Expand to see additional node.
        DefaultMutableTreeNode dummy_node = null;
        if (node.getChildCount() > 0)
            path[i] = node.getChildAt(0);
        else {
            dummy_node = new DefaultMutableTreeNode(new Dummy());
            treeModel.insertNodeInto(dummy_node, node, node.getChildCount());
            path[i] = dummy_node;
            manage_expand = false;
        }
        tp = new TreePath(path);
        setSelectionPath(tp);
        scrollPathToVisible(tp);

        switch (obj_type) {
            case ADDRESS:
                //	Create a node for devices
                new_node = new DefaultMutableTreeNode(new AccessAddress("*.*.*.*"));
                treeModel.insertNodeInto(new_node, node, node.getChildCount());
                path[i] = new_node;
                tp = new TreePath(path);
                setSelectionPath(tp);
                scrollPathToVisible(tp);
                break;
            case DEVICE:
                //	Create a node for devices
                new_node = new DefaultMutableTreeNode(new AccessDevice("*/*/*", WRITE));
                treeModel.insertNodeInto(new_node, node, node.getChildCount());
                path[i] = new_node;
                tp = new TreePath(path);
                setSelectionPath(tp);
                scrollPathToVisible(tp);
                break;
            default:
                return;
        }
        if (dummy_node != null)
            treeModel.removeNodeFromParent(dummy_node);
        manage_expand = true;
        if (!editItem())
            treeModel.removeNodeFromParent(new_node);
    }

    //===============================================================
    //===============================================================
    EditTreeItem edit_item;

    boolean editItem() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null)
            return true;
        Object o = node.getUserObject();
        int obj_type;
        String value;
        if (o instanceof AccessDevice) {
            obj_type = DEVICE;
            value = ((AccessDevice) o).name;
        } else if (o instanceof AccessAddress) {
            obj_type = ADDRESS;
            value = ((AccessAddress) o).name;
        } else
            return false;

        //	Build the inside dialog
        edit_item = new EditTreeItem(parent, this, value, obj_type);
        if (edit_item.showDlg()) {
            String user = node.getParent().getParent().toString();
            String new_name = edit_item.getInputs();
            try {
                switch (obj_type) {
                    case ADDRESS:
                        AccessAddress add = (AccessAddress) o;
                        accessProxy.removeAddress(user, add.name);
                        accessProxy.addAddress(user, new_name);
                        add.setName(new_name);
                        rebuildNode(node, add);
                        break;
                    case DEVICE:
                        AccessDevice dev = (AccessDevice) o;
                        accessProxy.removeDevice(user, dev.name, rightsStr[dev.right]);
                        accessProxy.addDevice(user, new_name, rightsStr[dev.right]);
                        dev.name = new_name;
                        rebuildNode(node, dev);
                        break;
                }
                return true;
            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(parent,
                        "Error during Database access", e);
            }
        }
        return false;
    }

    //===============================================================
    //===============================================================
    private void rebuildNode(DefaultMutableTreeNode node, Object obj) {
        //  Set new user object to resize
        DefaultMutableTreeNode parent_node = (DefaultMutableTreeNode) node.getParent();
        DefaultMutableTreeNode new_node = new DefaultMutableTreeNode(obj);
        int idx = parent_node.getIndex(node);
        treeModel.insertNodeInto(new_node, parent_node, idx);
        treeModel.removeNodeFromParent(node);

    }

    //===============================================================
    //===============================================================
    private boolean isAllUsersNode(DefaultMutableTreeNode node) {
        Object o = node.getUserObject();
        if (o instanceof AccessUser) {
            String user = ((AccessUser) o).getName();
            return user.equals("*");
        }
        return false;
    }

    //===============================================================
    //===============================================================
    private boolean isLastOneForAllUsers(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode
                collec_node = (DefaultMutableTreeNode) node.getParent();
        DefaultMutableTreeNode
                user_node = (DefaultMutableTreeNode) collec_node.getParent();
        return isAllUsersNode(user_node) && (collec_node.getChildCount() == 1);
    }

    //===============================================================
    //===============================================================
    void removeItem() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null)
            return;
        Object o = node.getUserObject();
        int obj_type;
        String message;
        String user;
        if (o instanceof AccessDevice) {
            //	Check if not the last one for all users
            if (isLastOneForAllUsers(node)) {
                admin.astor.tools.Utils.popupError(parent,
                        "Cannot remove last device for all users");
                return;
            }
            user = node.getParent().getParent().toString();
            message = "Are you sure to want to remove :  " + o;
            obj_type = DEVICE;
        } else if (o instanceof AccessAddress) {
            //	Check if not the last one for all users
            if (isLastOneForAllUsers(node)) {
                admin.astor.tools.Utils.popupError(parent,
                        "Cannot remove last address for all users");
                return;
            }
            user = node.getParent().getParent().toString();
            message = "Are you sure to want to remove :  " + o;
            obj_type = ADDRESS;
        } else if (o instanceof AccessUser) {
            //	Check if not for all users
            if (isAllUsersNode(node)) {
                admin.astor.tools.Utils.popupError(parent,
                        "Cannot remove rights for all users");
                return;
            }
            user = ((AccessUser) o).getName();
            obj_type = USER_NODE;
            message = "Are you sure to want to remove all records for " + o;
        } else
            return;

        if (JOptionPane.showConfirmDialog(this,
                message,
                "Confirm Dialog",
                JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION)
            return;


        try {
            switch (obj_type) {
                case ADDRESS:
                    AccessAddress add = (AccessAddress) o;
                    accessProxy.removeAddress(user, add.name);
                    treeModel.removeNodeFromParent(node);
                    break;
                case DEVICE:
                    AccessDevice dev = (AccessDevice) o;
                    accessProxy.removeDevice(user, dev.name, rightsStr[dev.right]);
                    treeModel.removeNodeFromParent(node);
                    break;
                case USER_NODE:
                    accessProxy.removeUser(user);
                    DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) node.getParent();
                    for (UserGroup userGroup : groups) {
                        if (userGroup.getName().equals(groupNode.toString())) {
                            userGroup.remove(user);
                            if (userGroup.isEmpty()) {
                                groups.remove(userGroup);
                                treeModel.removeNodeFromParent(groupNode);
                            }
                            else
                                treeModel.removeNodeFromParent(node);
                            //  Update db
                            UserGroup.setUserGroupsToDatabase(this, groups);
                            return;
                        }
                    }
                    break;
            }

        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent,
                    "Error during Database access", e);
        }
    }

    //===============================================================
    //===============================================================
    private String[] getDefinedUsers() {
        List<String> userList = new ArrayList<>();
        for (UserGroup group : groups) {
            userList.addAll(group);
        }
        return userList.toArray(new String[0]);
    }

    //===============================================================
    //===============================================================
    private void expendUserNode(String name) {
        //  If  already exist, show it
        DefaultMutableTreeNode userNode = getUserNode(name);
        int depth = userNode.getPath().length;

        //  expand address nodes
        TreeNode[] path = new TreeNode[depth+2];
        int idx = 0;
        path[idx++] = root;
        if (depth>2)
            path[idx++] = userNode.getParent();
        path[idx++] = userNode;

        DefaultMutableTreeNode  addressNode = (DefaultMutableTreeNode) userNode.getChildAt(ADDRESS);
        if (addressNode.getChildCount() > 0) {
            path[idx] = addressNode;
            path[idx+1]   = addressNode.getChildAt(0);
            TreePath tp = new TreePath(path);
            setSelectionPath(tp);
        }
        //  expand device nodes if exists
        DefaultMutableTreeNode deviceNode = (DefaultMutableTreeNode) userNode.getChildAt(DEVICE);
        if (addressNode.getChildCount() > 0) {
            path[idx] = deviceNode;
            path[idx+1] = deviceNode.getChildAt(0);
            TreePath tp = new TreePath(path);
            setSelectionPath(tp);
        }

        //  Select user node.
        path = new TreeNode[depth];
        idx = 0;
        path[idx++] = root;
        if (depth>2)
            path[idx++] = userNode.getParent();
        path[idx] = userNode;
        TreePath tp = new TreePath(path);
        setSelectionPath(tp);
    }

    //===============================================================
    //===============================================================
    private DefaultMutableTreeNode getUserNode(String name) {
        for (int i=0 ; i<root.getChildCount() ; i++) {
            //  Check group node
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) root.getChildAt(i);
            Object userObject = node.getUserObject();
            if (userObject.toString().equals(name))
                return node;

            //  Check user nodes
            for (int j=0 ; j<node.getChildCount() ; j++) {
                DefaultMutableTreeNode userNode =
                        (DefaultMutableTreeNode) node.getChildAt(j);
                userObject = userNode.getUserObject();
                if (userObject.toString().equals(name))
                    return userNode;
            }
        }
        return new DefaultMutableTreeNode(name);
    }

    //===============================================================
    //===============================================================
    private boolean userExists(String name) {
        String[] users = getDefinedUsers();
        for (String user : users)
            if (user.equals(name))
                return true;
        return false;
    }

    //===============================================================
    //===============================================================
    void changeGroup() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null)
            return;
        DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) node.getParent();
        UserGroup   srcGroup;
        Object  object = groupNode.getUserObject();
        if (object instanceof UserGroup)
            srcGroup = (UserGroup) object;
        else
            return;

        Object o = node.getUserObject();
        if (o instanceof AccessUser) {
            String user = ((AccessUser) o).getName();
            //  Get target group
            String title = "Move " + user + " from " + srcGroup + "  to ";
            ChooseGroupDialog   dialog = new ChooseGroupDialog(parent, title, groups);
            if (dialog.showDialog()!=JOptionPane.CANCEL_OPTION) {
                UserGroup   targetGroup = dialog.getUserGroup();
                if (targetGroup==srcGroup)
                    return;

                //  Update the JTree
                treeModel.removeNodeFromParent(node);
                groupNode = getGroupNode(targetGroup.getName());
                if (groupNode==null) {
                    groupNode = new DefaultMutableTreeNode(targetGroup);
                    treeModel.insertNodeInto(groupNode, root, root.getChildCount());
                    groups.add(targetGroup);
                }
                treeModel.insertNodeInto(node, groupNode, groupNode.getChildCount());
                srcGroup.remove(user);
                targetGroup.add(user);

                //  Update group list and put in DB
                UserGroup.setUserGroupsToDatabase(this, groups);

                //  Expend to show new user
                TreeNode[] path = new TreeNode[3];
                path[0] = root;
                path[1] = groupNode;
                path[2] = node;
                TreePath tp = new TreePath(path);
                setSelectionPath(tp);

                tp = new TreePath(path);
                setSelectionPath(tp);
                scrollPathToVisible(tp);
            }
        }
    }
    //===============================================================
    //===============================================================
    void cloneUser() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null)
            return;
        DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) node.getParent();
        UserGroup   userGroup = null;
        Object  object = groupNode.getUserObject();
        if (object instanceof UserGroup)
            userGroup = (UserGroup) object;

        Object o = node.getUserObject();
        if (o instanceof AccessUser) {
            String srcUser = ((AccessUser) o).getName();
            String newUser;
            String[]    inputs = {""};
            boolean ok = false;
            while (!ok) {
                //	Get new user name and check if already exists.
                EditDialog dlg = new EditDialog(parent, "", null, groups, userGroup);
                if (dlg.showDialog() != JOptionPane.OK_OPTION)
                    return;

                inputs = dlg.getInputs();
                ok = checkUserName(inputs[0]);
                userGroup = dlg.getUserGroup();
            }
            newUser = inputs[0];
            groupNode = getGroupNode(userGroup.getName());
            if (groupNode==null) { //   Not already exist
                groupNode = new DefaultMutableTreeNode(userGroup);
                treeModel.insertNodeInto(groupNode, root, root.getChildCount());
                groups.add(userGroup);
                groups.sort(new GroupComparator());
            }

            //  Update Access proxy
            try {
                accessProxy.cloneUser(srcUser, newUser);
            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(parent,
                        "Error during Database access", e);
                return;
            }
            userGroup.add(newUser);

            //  Update JTree
            DefaultMutableTreeNode u_node =
                    new DefaultMutableTreeNode(new AccessUser(newUser));
            DefaultMutableTreeNode a_node = new DefaultMutableTreeNode(collecStr[ADDRESS]);
            DefaultMutableTreeNode d_node = new DefaultMutableTreeNode(collecStr[DEVICE]);
            treeModel.insertNodeInto(a_node, u_node, 0);
            treeModel.insertNodeInto(d_node, u_node, 1);
            treeModel.insertNodeInto(u_node, groupNode, groupNode.getChildCount());

            //  Add dummy nodes
            treeModel.insertNodeInto(new DefaultMutableTreeNode(new Dummy()), a_node, 0);
            treeModel.insertNodeInto(new DefaultMutableTreeNode(new Dummy()), d_node, 0);

            //  update database
            UserGroup.setUserGroupsToDatabase(this,groups);

            //  And expand
            TreeNode[] path = new DefaultMutableTreeNode[3];
            TreePath tp;
            path[0] = root;
            path[1] = u_node;
            path[2] = a_node;
            tp = new TreePath(path);
            setSelectionPath(tp);
            scrollPathToVisible(tp);

        }
    }

    //===============================================================
    //===============================================================
    void pasteItem() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null)
            return;
        Object o = node.getUserObject();
        int obj_type;
        if (o instanceof String) {
            if (o.toString().equals(collecStr[DEVICE]))
                obj_type = DEVICE;
            else if (o.toString().equals(collecStr[ADDRESS]))
                obj_type = ADDRESS;
            else
                return;
        } else
            return;
        String user = node.getParent().toString();
        try {

            DefaultMutableTreeNode new_node = null;
            switch (obj_type) {
                case ADDRESS:
                    String address = copiedAddresses.get(0).name;
                    accessProxy.addAddress(user, address);
                    new_node = new DefaultMutableTreeNode(new AccessAddress(address));
                    treeModel.insertNodeInto(new_node, node, node.getChildCount());
                    break;
                case DEVICE:
                    String deviceName = copiedDevices.get(0).name;
                    int right = copiedDevices.get(0).right;
                    accessProxy.addDevice(user, deviceName, rightsStr[right]);
                    new_node = new DefaultMutableTreeNode(new AccessDevice(deviceName, right));
                    treeModel.insertNodeInto(new_node, node, node.getChildCount());
                    break;
            }
            TreeNode[] path = new DefaultMutableTreeNode[4];
            path[0] = root;
            path[1] = node.getParent(); // user
            path[2] = node;
            path[3] = new_node;
            TreePath tp = new TreePath(path);
            setSelectionPath(tp);
            scrollPathToVisible(tp);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent,
                    "Error during Database access", e);
        }
    }

    //===============================================================
    //===============================================================
    void copyItem() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null)
            return;
        Object o = node.getUserObject();
        int obj_type;
        if (o instanceof AccessDevice)
            obj_type = DEVICE;
        else
            obj_type = ADDRESS;

        switch (obj_type) {
            case ADDRESS:
                copiedAddresses.clear();
                copiedAddresses.add((AccessAddress) o);
                break;
            case DEVICE:
                copiedDevices.clear();
                copiedDevices.add((AccessDevice) o);
                break;
        }
    }

    //===============================================================
    //===============================================================
    void toggleRight() {
        //	Get object itself
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null)
            return;
        Object o = node.getUserObject();

        //	Get user object
        node = (DefaultMutableTreeNode) node.getParent().getParent();
        Object uo = node.getUserObject();
        String user = ((AccessUser) uo).getName();

        try {
            AccessDevice dev = (AccessDevice) o;
            int new_right;
            if (dev.right == READ)
                new_right = WRITE;
            else
                new_right = READ;

            accessProxy.removeDevice(user, dev.name, rightsStr[dev.right]);
            accessProxy.addDevice(user, dev.name, rightsStr[new_right]);
            dev.right = new_right;
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent,
                    "Error during Database access", e);
        }
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private List<String> getUserList() {
        List<String> users = new ArrayList<>();
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode childNode =
                    (DefaultMutableTreeNode) root.getChildAt(i);
            Object obj = childNode.getUserObject();
            if (obj instanceof AccessUser) {
                AccessUser user = (AccessUser) obj;
                users.add(user.toString());
            }
        }
        return users;
    }

    //===============================================================
    //===============================================================
    public void findUser(String userName) {
        for (int i=0 ; i<root.getChildCount() ; i++) {
            findUser(userName, (DefaultMutableTreeNode) root.getChildAt(i));
        }
    }
    //===============================================================
    //===============================================================
    public void findUser(String userName, DefaultMutableTreeNode node) {
        for (int i=0 ; i<node.getChildCount() ; i++) {
            DefaultMutableTreeNode childNode =
                    (DefaultMutableTreeNode) node.getChildAt(i);
            Object obj = childNode.getUserObject();
            if (obj instanceof AccessUser) {
                AccessUser user = (AccessUser) obj;
                if (user.toString().equals(userName))
                    expandChildren(childNode);
            }
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
        TreeNode[] tn = nodeList.toArray(new TreeNode[0]);
        TreePath tp = new TreePath(tn);
        setSelectionPath(tp);
        scrollPathToVisible(tp);
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


    //===============================================================
    /**
     * Renderer Class
     */
    //===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer {
        private Font[] fonts;

        private final int TITLE = 0;
        private final int ALL_USERS = 1;
        private final int USER = 2;
        private final int COLLEC = 3;
        private final int LEAF = 4;

        //===============================================================
        //===============================================================
        public TangoRenderer() {
            Utils utils = Utils.getInstance();
            tango_icon = utils.getIcon("TangoClass.gif", 0.33);
            all_users_icon = utils.getIcon("user.gif", 1.0);
            group_icon = utils.getIcon("user.gif", 0.8);
            user_icon = utils.getIcon("user.gif", 0.6);
            add_icon = utils.getIcon("server.gif");
            dev_icon = utils.getIcon("device.gif");
            write_icon = utils.getIcon("greenbal.gif");
            read_icon = utils.getIcon("redball.gif");

            fonts = new Font[LEAF + 1];
            fonts[TITLE] = new Font("Dialog", Font.BOLD, 18);
            //	width fixed font
            fonts[ALL_USERS] = new Font("Dialog", Font.BOLD, 16);
            fonts[USER] = new Font("Dialog", Font.BOLD, 12);
            fonts[COLLEC] = new Font("Dialog", Font.BOLD, 12);
            fonts[LEAF] = new Font("Monospaced", Font.PLAIN, 12);
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

                if (node.getUserObject() instanceof UserGroup) {
                    UserGroup group = (UserGroup) node.getUserObject();
                    setFont(group.getFont());
                    setIcon(group_icon);
                }
                else
                if (node.getUserObject() instanceof AccessUser) {
                    String user = ((AccessUser) node.getUserObject()).getName();
                    if (user.equals("*")) {
                        setFont(fonts[ALL_USERS]);
                        setIcon(all_users_icon);
                    } else {
                        setFont(fonts[USER]);
                        setIcon(user_icon);
                    }
                } else if (node.getUserObject() instanceof String) {
                    setFont(fonts[COLLEC]);
                    if (obj.toString().equals(collecStr[ADDRESS]))
                        setIcon(add_icon);
                    else
                        setIcon(dev_icon);
                } else if (node.getUserObject() instanceof AccessAddress) {
                    setFont(fonts[LEAF]);
                    setIcon(add_icon);
                } else if (node.getUserObject() instanceof AccessDevice) {
                    setFont(fonts[LEAF]);
                    AccessDevice dev = (AccessDevice) node.getUserObject();
                    if (dev.right == WRITE)
                        setIcon(write_icon);
                    else
                        setIcon(read_icon);
                }
            }
            return this;
        }
    }//	End of Renderer Class


    //===============================================================
    /*
     *	Classes defining structures used in tree
     */
    //===============================================================
    class AccessUser {
        private String name;

        //===========================================================
        private AccessUser(String name) {
            this.name = name;
        }

        //===========================================================
        public String getName() {
            return name;
        }

        //===========================================================
        public String toString() {
            return (name.equals("*") ? "All Users" : name);
        }
        //===========================================================
    }

    //===============================================================
    //===============================================================
    class AccessAddress {
        String name;
        private String hostname = null;

        //===========================================================
        private AccessAddress(String add) {
            name = add;
            checkHostname(add);
        }

        //===========================================================
        private void checkHostname(String add) {
            //	Split the address
            StringTokenizer stk = new StringTokenizer(add, ".");
            List<String> tokens = new ArrayList<>();
            while (stk.hasMoreTokens())
                tokens.add(stk.nextToken());
            byte[] bytes = new byte[4];
            for (int i = 0; i < 4 && i < tokens.size(); i++)
                try {
                    bytes[i] = (byte) Integer.parseInt(tokens.get(i));
                } catch (NumberFormatException e) {
                    hostname = null;
                    return;
                }

            //	Check if host name
            try {
                java.net.InetAddress inetAddress =
                        java.net.InetAddress.getByAddress(bytes);
                hostname = inetAddress.getHostName();
                //	remove fqdn if any
                int pos = hostname.indexOf('.');
                if (pos > 0)
                    hostname = hostname.substring(0, pos);
            } catch (Exception e) {
                hostname = null;
            }
        }

        //===========================================================
        private void setName(String add) {
            name = add;
            checkHostname(add);
        }

        //===========================================================
        public String toString() {
            if (hostname == null)
                return name;
            else
                return name + "  (" + hostname + ")";
        }
        //===========================================================
    }

    //===============================================================
    class AccessDevice {
        String name;
        int right = READ;

        //===========================================================
        private AccessDevice(String add, String r) {
            name = add;
            for (int i = 0; i < rightsStr.length; i++)
                if (rightsStr[i].equals(r))
                    right = i;
        }

        //===========================================================
        private AccessDevice(String add, int r) {
            name = add;
            right = r;
        }

        //===========================================================
        public String toString() {
            return name;
        }
        //===========================================================
    }
    //===============================================================
    private class Dummy {
        //	nothing
        public String toString() {
            return "";
        }
    }
    //===============================================================
    //===============================================================
    private class GroupComparator implements Comparator<UserGroup> {
        public int compare(UserGroup group1, UserGroup group2) {
            if (group1.getName().equals(UserGroup.unsorted))
                return 1;
            if (group2.getName().equals(UserGroup.unsorted))
                return -1;
            return group1.getName().compareTo(group2.getName());
        }
    }
}