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

import admin.astor.AstorUtil;
import admin.astor.ServArchitectureDialog;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;


public class DevBrowserTree extends JTree implements TangoConst {
    private static final int INSTANCE = 3;
    private static final int CLASS = 4;
    private static final int DEVICE = 5;
    private static final int ATTRIB = 6;

    private static final int DOMAIN = 3;
    private static final int FAMILY = 4;
    private static final int MEMBER = 5;

    private DefaultTreeModel treeModel;

    private DevBrowser browser;
    private TreePopupMenu attributeMenu = null;
    private TreePopupMenu deviceMenu = null;
    private TreePopupMenu serverMenu = null;
    private DefaultMutableTreeNode root;

    //===============================================================
    //===============================================================
    public DevBrowserTree(DevBrowser parent) throws DevFailed {
        super();
        this.browser = parent;

        //	Get TANGO HOST as title
        String tango_host = ApiUtil.get_db_obj().get_tango_host();
        initComponent(tango_host);
        attributeMenu = new TreePopupMenu(this, TreePopupMenu.MODE_ATTR);
        deviceMenu = new TreePopupMenu(this, TreePopupMenu.MODE_DEVICE);
        serverMenu = new TreePopupMenu(this, TreePopupMenu.MODE_SERVER);
    }

    //===============================================================
    //===============================================================
    private void initComponent(String title) throws DevFailed {

        //  Create the nodes.
        root = new DefaultMutableTreeNode(title);

        createServerNodes(root);
        createDeviceNodes(root);
        createAliasNodes(root);

        //	Create the tree that allows one selection at a time.
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //	Create Tree and Tree model
        //------------------------------------
        treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);

        // Set the icon for leaf nodes.
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

    //===============================================================
    //===============================================================
    private boolean createChildNodes(DefaultMutableTreeNode node, String[] str) {
        boolean create = false;
        if (node.getChildCount() != str.length)
            create = true;
        else
            for (int i = 0; i < str.length; i++)
                if (!node.getChildAt(i).toString().equals(str[i]))
                    create = true;
        return create;
    }

    //===============================================================
    //===============================================================
    private void createAliasNodes(DefaultMutableTreeNode root) throws DevFailed {
        //	Create a node for Device
        DefaultMutableTreeNode c_node = new DefaultMutableTreeNode("Aliases");
        root.add(c_node);

        DefaultMutableTreeNode al_node;

        Database db = ApiUtil.get_db_obj();
        String[] aliases = db.get_device_alias_list("*");
        for (String alias : aliases) {
            //	Create a node for domain
            try {
                String deviceName = ApiUtil.get_db_obj().get_device_from_alias(alias);
                al_node = new DefaultMutableTreeNode(new BrowserDevice(deviceName, alias));
                al_node.add(new DefaultMutableTreeNode("dummy"));
                c_node.add(al_node);
            } catch (DevFailed e) {/* Do Nothing */}
        }
    }

    //===============================================================
    //===============================================================
    private void createServerNodes(DefaultMutableTreeNode root) throws DevFailed {
        //	Create a node for Device
        DefaultMutableTreeNode c_node = new DefaultMutableTreeNode("Servers");
        root.add(c_node);

        DefaultMutableTreeNode d_node;

        Database db = ApiUtil.get_db_obj();
        String[] servers = db.get_server_name_list();
        for (String server : servers) {
            //	Create a node for domain
            d_node = new DefaultMutableTreeNode(server);
            d_node.add(new DefaultMutableTreeNode("dummy"));
            c_node.add(d_node);
        }
    }

    //===============================================================
    //===============================================================
    private void createInstanceNodes(DefaultMutableTreeNode node) {
        try {
            DefaultMutableTreeNode s_node;
            String binaryFile = (String) node.getUserObject();
            String[] instances =
                    ApiUtil.get_db_obj().get_instance_name_list(binaryFile);

            //  Check if something has changed.
            if (!createChildNodes(node, instances))
                return;

            for (int i = 0; i < instances.length; i++) {
                //	Create a node for family
                s_node = new DefaultMutableTreeNode(new BrowserServer(binaryFile, instances[i]));
                s_node.add(new DefaultMutableTreeNode("Dummy"));
                treeModel.insertNodeInto(s_node, node, i);
            }
            removePreviousNode(node, instances.length);
        } catch (DevFailed e) {
            displayException(e);
        }
    }

    //===============================================================
    //===============================================================
    private void createClassNodes(DefaultMutableTreeNode node) {
        try {
            DefaultMutableTreeNode i_node;
            BrowserServer server = (BrowserServer) node.getUserObject();
            String[] classes = ApiUtil.get_db_obj().get_server_class_list(server.name);

            //  Check if something has changed.
            if (!createChildNodes(node, classes))
                return;

            for (int i = 0; i < classes.length; i++) {
                //	Create a node for family
                i_node = new DefaultMutableTreeNode(classes[i]);
                i_node.add(new DefaultMutableTreeNode("Dummy"));
                treeModel.insertNodeInto(i_node, node, i);
            }
            removePreviousNode(node, classes.length);
        } catch (DevFailed e) {
            displayException(e);
        }
    }

    //===============================================================
    //===============================================================
    private void createDeviceNodesFromServer(DefaultMutableTreeNode node) {
        try {
            DefaultMutableTreeNode d_node;
            DefaultMutableTreeNode s_node = (DefaultMutableTreeNode) node.getParent();

            BrowserServer server = (BrowserServer) s_node.getUserObject();
            String serverName = server.name;
            String className = (String) node.getUserObject();
            String[] devices =
                    ApiUtil.get_db_obj().get_device_name(serverName, className);

            //  Check if something has changed.
            if (!createChildNodes(node, devices))
                return;

            for (int i = 0; i < devices.length; i++) {
                //	Create a node for family
                d_node = new DefaultMutableTreeNode(new BrowserDevice_2(devices[i]));
                d_node.add(new DefaultMutableTreeNode("dummy"));
                treeModel.insertNodeInto(d_node, node, i);
            }
            removePreviousNode(node, devices.length);
        } catch (DevFailed e) {
            displayException(e);
        }
    }
    //===============================================================
    //===============================================================


    //===============================================================
    //===============================================================
    private void createDeviceNodes(DefaultMutableTreeNode root) throws DevFailed {
        //	Create a node for Device
        DefaultMutableTreeNode c_node = new DefaultMutableTreeNode("Devices");
        root.add(c_node);

        DefaultMutableTreeNode d_node;

        Database db = ApiUtil.get_db_obj();
        String[] domains = db.get_device_domain("*");
        for (String domain : domains) {
            //	Create a node for domain
            d_node = new DefaultMutableTreeNode(domain);
            d_node.add(new DefaultMutableTreeNode("dummy"));
            c_node.add(d_node);
        }
    }

    //===============================================================
    //===============================================================
    private void createFamilyNodes(DefaultMutableTreeNode node) {
        try {
            DefaultMutableTreeNode f_node;
            String domain = (String) node.getUserObject();
            String[] families =
                    ApiUtil.get_db_obj().get_device_family(domain + "/*");

            //  Check if something has changed.
            if (!createChildNodes(node, families))
                return;

            for (int f = 0; f < families.length; f++) {
                //	Create a node for family
                f_node = new DefaultMutableTreeNode(families[f]);
                f_node.add(new DefaultMutableTreeNode("Dummy"));
                treeModel.insertNodeInto(f_node, node, f);
            }
            removePreviousNode(node, families.length);
        } catch (DevFailed e) {
            displayException(e);
        }
    }

    //===============================================================
    //===============================================================
    private void createMemberNodes(String tango_path, DefaultMutableTreeNode node) {
        try {
            DefaultMutableTreeNode m_node;
            tango_path += (String) node.getUserObject();
            String[] members =
                    ApiUtil.get_db_obj().get_device_member(tango_path + "/*");

            //  Check if something has changed.
            if (!createChildNodes(node, members))
                return;

            for (int m = 0; m < members.length; m++) {
                //	Create a node for family
                //m_node = new DefaultMutableTreeNode(members[m]);
                m_node = new DefaultMutableTreeNode(new BrowserDevice(tango_path + "/" + members[m]));
                m_node.add(new DefaultMutableTreeNode("Dummy"));
                treeModel.insertNodeInto(m_node, node, m);
            }
            removePreviousNode(node, members.length);
        } catch (DevFailed e) {
            displayException(e);
        }
    }

    //===============================================================
    //===============================================================
    private void createAttributeNodes(DefaultMutableTreeNode node) {
        try {
            DefaultMutableTreeNode a_node;

            BrowserDevice dev = (BrowserDevice) node.getUserObject();
            String[] attnames = dev.get_attribute_list();

            //  Check if something has changed.
            if (!createChildNodes(node, attnames))
                return;

            for (int a = 0; a < attnames.length; a++) {
                //	Create a node for attribute
                BrowserAttribute attr = new BrowserAttribute(attnames[a], dev);
                a_node = new DefaultMutableTreeNode(attr);
                treeModel.insertNodeInto(a_node, node, a);
            }
            removePreviousNode(node, attnames.length);
        } catch (DevFailed e) {
            removePreviousNode(node, 0);
            displayException(e);
        }
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
    private String tangoPath(TreePath path, int nb) {
        String p = "";
        for (int i = DOMAIN; i < DOMAIN + nb; i++) {
            p += path.getPathComponent(i - 1).toString();
            p += "/";
        }
        return p;
    }

    //===============================================================
    //===============================================================
    public void expandedPerformed(TreeExpansionEvent evt) {
        //	Get path
        TreePath tp = evt.getPath();
        Object[] path = tp.getPath();
        if (path.length < 2)
            return;
        //	Get concerned node
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) tp.getPathComponent(path.length - 1);

        //	and create new ones
        String tango_path;
        if (path[1].toString().equals("Servers")) {
            switch (path.length) {
                case INSTANCE:
                    createInstanceNodes(node);
                    break;
                case CLASS:
                    createClassNodes(node);
                    break;
                case DEVICE:
                    createDeviceNodesFromServer(node);
                    break;
                case ATTRIB:
                    createAttributeNodes(node);
                    break;
            }
        } else if (path[1].toString().equals("Devices")) {
            switch (path.length) {
                case DOMAIN:
                    createFamilyNodes(node);
                    break;
                case FAMILY:
                    tango_path = tangoPath(tp, 1);
                    createMemberNodes(tango_path, node);
                    break;
                case MEMBER:
                    createAttributeNodes(node);
                    break;
            }
        } else if (path[1].toString().equals("Aliases")) {
            if (path.length == 3)
                createAttributeNodes(node);
        }
    }

    //======================================================
    /**
     * Manage event on clicked mouse on JTree object.
     */
    //======================================================
    private boolean obj_has_polling;

    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
        //	Check if click is on a node
        if (getRowForLocation(evt.getX(), evt.getY()) < 1)
            return;

        //	Set selection at mouse position
        TreePath selectedPath = getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath == null) return;

        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount() - 1);
        Object o = node.getUserObject();
        int mask = evt.getModifiers();

        clearDisplay();
        obj_has_polling = false;

        //  Check button clicked
        if (evt.getClickCount() == 2 && node.isLeaf()) {
            //	if do not have attributes, retry to read attributes
            if (o instanceof BrowserDevice) {
                createAttributeNodes(node);
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode child =
                            (DefaultMutableTreeNode) node.getChildAt(i);
                    expandPath(new TreePath(child.getPath()));
                }
            }
        } else if ((mask & MouseEvent.BUTTON3_MASK) != 0) {
            //	Check if selection is an attribute
            if (o instanceof BrowserAttribute) {
                attributeMenu.showMenu(evt);

                //	Check if selection is an attribute
                BrowserAttribute attr = (BrowserAttribute) o;
                displayEventProperties(attr);
            } else if (o instanceof BrowserDevice) {
                boolean running = displayDeviceInfo((BrowserDevice) o);
                deviceMenu.showMenu(evt, obj_has_polling, running);
            } else if (o instanceof BrowserServer) {
                boolean running = displayDeviceInfo(((BrowserServer) o).dev);
                serverMenu.showMenu(evt, obj_has_polling, running);
            }
        } else if ((mask & MouseEvent.BUTTON1_MASK) != 0) {
            if (o instanceof BrowserServer)
                displayDeviceInfo(((BrowserServer) o).dev);
            else if (o instanceof BrowserDevice)
                displayDeviceInfo((BrowserDevice) o);
            else if (o instanceof BrowserAttribute)
                displayEventProperties((BrowserAttribute) o);
        }
    }

    //======================================================
    //======================================================
    private void clearDisplay() {
        browser.setText("");
    }

    //======================================================
    //======================================================
    private boolean displayDeviceInfo(BrowserDevice dev) {
        String message;
        try {
            DeviceInfo info = dev.get_info();
            message = info + "\n\n";
        } catch (DevFailed e) {
            browser.setText(e.toString());
            return false;
        }

        boolean ok = false;
        try {
            String str = "\n=======================================\n";
            str += "           Polling Status:\n\n";
            //  Check if admin device
            if (dev.name().startsWith("dserver/")) {
                DeviceData argout = dev.command_inout("QueryDevice");
                String[] devlist = argout.extractStringArray();
                for (String device : devlist) {
                    //  Take off class name
                    String devname = device.substring(device.indexOf("::") + 2);
                    DeviceData argin = new DeviceData();
                    argin.insert(devname);
                    argout = dev.command_inout("DevPollStatus", argin);
                    String[] s = argout.extractStringArray();
                    str += "----------------- " + devname + " ------------------\n";
                    for (String line : s)
                        str += line + "\n\n";
                    if (s.length > 0)
                        obj_has_polling = true;
                }
                ok = true;
            } else {
                String[] poll_st = dev.polling_status();
                for (String line : poll_st)
                    str += line + "\n\n";
                ok = true;
                if (poll_st.length > 0)
                    obj_has_polling = true;
            }
            message += str;
        } catch (DevFailed e) {
            try {
                dev.ping();
                ok = true;
            } catch (DevFailed ex) {
                message += "\n=======================================\n";
                message += AstorUtil.strException(ex);
            }
        }

        browser.setText(message);
        return ok;
    }

    //======================================================
    //======================================================
    void showProfiler() {
        try {
            Object o = getSelectedNode().getUserObject();
            if (o instanceof BrowserDevice)
                new PollingProfiler(browser, ((BrowserDevice) o).name).setVisible(true);
            else if (o instanceof BrowserServer) {
                DeviceData argOut = ((BrowserServer) o).dev.command_inout("QueryDevice");
                String[] deviceNames = argOut.extractStringArray();
                //  Take off class names.
                String s = "::";
                for (int i = 0; i < deviceNames.length; i++) {
                    int idx = deviceNames[i].indexOf(s);
                    if (idx > 0)
                        deviceNames[i] = deviceNames[i].substring(idx + s.length());
                }

                new PollingProfiler(browser, deviceNames).setVisible(true);
            }
        } catch (DevFailed e) {
            Utils.popupError(browser, null, e);
        }
    }

    //======================================================
    //======================================================
    String getSelectedName() {
        Object o = getSelectedNode().getUserObject();
        if (o instanceof BrowserAttribute)
            return ((BrowserAttribute) o).name;
        else if (o instanceof BrowserDevice)
            return ((BrowserDevice) o).name;
        else if (o instanceof BrowserServer)
            return ((BrowserServer) o).name;
        else
            return "??";
    }

    //======================================================
    //======================================================
    private String getAttPollingInfo() {
        Object o = getSelectedNode().getUserObject();
        if (o instanceof BrowserAttribute) {
            try {
                BrowserAttribute att = (BrowserAttribute) o;
                String header = "Polled attribute name = ";
                String[] poll_status = att.dev.polling_status();
                for (String line : poll_status) {
                    String s = line.substring(header.length(),
                            line.indexOf('\n'));
                    if (s.toLowerCase().equals(att.name.toLowerCase()))
                        return line;
                }
            } catch (DevFailed e) {
                return e.toString();
            }
        }
        return "";
    }

    //======================================================
    //======================================================
    private void displayEventProperties(BrowserAttribute attr) {
        displayEventProperties(attr.attname);
    }

    //======================================================
    //======================================================
    void displayEventProperties(String attname) {
        String str = "";
        try {
            //	Check if device support archive events
            AttributeProxy ap = new AttributeProxy(attname);
            int att_idl_version = ap.get_idl_version();
            if (att_idl_version < 3) {
                browser.setText("Device_" +
                        att_idl_version + "Impl not supported.");
                return;
            }

            //	ok. Get config
            AttributeInfoEx info = ap.get_info_ex();
            if (info.events != null) {
                str = attname;
                str += "\n\nChange event properties:\n";
                if (info.events.ch_event != null) {
                    str += "abs_change :   " + info.events.ch_event.abs_change + "\n";
                    str += "rel_change :   " + info.events.ch_event.rel_change + "\n";
                } else {
                    str += "rel_change :   " + Tango_AlrmValueNotSpec + "\n";
                    str += "abs_change :   " + Tango_AlrmValueNotSpec + "\n";
                }

                str += "\n\nPeriodic event properties:\n";
                if (info.events.per_event != null) {
                    str += "period     :   " + info.events.per_event.period;
                } else {
                    str += "period     :   " + Tango_AlrmValueNotSpec;
                }

                str += "\n\nArchive event properties:\n";
                if (info.events.arch_event != null) {
                    str += "abs_change :   " + info.events.arch_event.abs_change + "\n";
                    str += "rel_change :   " + info.events.arch_event.rel_change + "\n";
                    str += "period     :   " + info.events.arch_event.period;
                } else {
                    str += "rel_change :   " + Tango_AlrmValueNotSpec + "\n";
                    str += "abs_change :   " + Tango_AlrmValueNotSpec + "\n";
                    str += "period     :   " + Tango_AlrmValueNotSpec;
                }
            }
            str += "\n\n\n" + getAttPollingInfo();

            browser.setText(str);
        } catch (DevFailed e) {
            displayException(e);
        }
    }

    //======================================================
    //======================================================
    void deviceTest() {
        Object o = getSelectedNode().getUserObject();
        String deviceName = null;
        if (o instanceof BrowserDevice)
            deviceName = ((BrowserDevice) o).name;
        else if (o instanceof BrowserServer)
            deviceName = "dserver/" + ((BrowserServer) o).name;

        if (deviceName != null) {
            try {
                JDialog d = new JDialog(browser, false);
                d.setTitle(deviceName + " Device Panel");
                d.setContentPane(new jive.ExecDev(deviceName));
                ATKGraphicsUtils.centerDialog(d);
                d.setVisible(true);
            } catch (DevFailed e) {
                Utils.popupError(browser, null, e);
            }
        }
    }

    //======================================================
    //======================================================
    void serverArchitecture() {
        Object o = getSelectedNode().getUserObject();
        String serverName = null;
        try {
            if (o instanceof BrowserDevice) {
                BrowserDevice dev = (BrowserDevice) o;
                DeviceInfo info = dev.get_info();
                serverName = info.server;
            } else if (o instanceof BrowserServer)
                serverName = ((BrowserServer) o).name;

            if (serverName != null) {
                ServArchitectureDialog sad = new ServArchitectureDialog(browser, serverName);
                ATKGraphicsUtils.centerDialog(sad);
                sad.setVisible(true);
            }
        } catch (DevFailed e) {
            displayException(e);
        }
    }

    //======================================================
    //======================================================
    private void displayException(Exception e) {
        browser.setText(AstorUtil.strException(e));
    }

    //======================================================
    //======================================================
    void deviceMonitor() {
        Object o = getSelectedNode().getUserObject();
        if (o instanceof BrowserDevice) {
            String devname = ((BrowserDevice) o).name;
            atkpanel.MainPanel atkpanel = new atkpanel.MainPanel(devname, false, true);
            ATKGraphicsUtils.centerFrameOnScreen(atkpanel);
        }
    }

    //======================================================
    //======================================================
    DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) getLastSelectedPathComponent();
    }

    //======================================================
    //======================================================
    private BrowserAttribute getAttribute(Object o) {
        if (o == null)
            return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
        Object obj = node.getUserObject();
        if (obj instanceof BrowserAttribute)
            return (BrowserAttribute) obj;
        else
            return null;
    }

    //======================================================
    //======================================================
    void add(int mode) {
        BrowserAttribute attr = getAttribute(getSelectedNode());
        if (attr != null) {
            //editProperties(mode);
            browser.add(attr.attname, mode);
        }
    }

    //======================================================
    //======================================================
    void editProperties(int mode) {
        BrowserAttribute attr = getAttribute(getSelectedNode());
        if (attr != null) {
            new PropertyDialog(browser, attr.attname, mode).showDialog();
            displayEventProperties(attr);
        }
    }

    //======================================================
    //======================================================
    void managePolling() {
        BrowserAttribute attr = getAttribute(getSelectedNode());
        if (attr != null)
            browser.managePolling(attr.dev, attr.name);
        else {
            Object o = getSelectedNode().getUserObject();
            if (o instanceof BrowserDevice_2)
                browser.managePolling(((BrowserDevice_2) o).name);
            else if (o instanceof BrowserDevice)
                browser.managePolling(((BrowserDevice) o).name);
        }
    }

    //======================================================
    //======================================================
    void displayHostPanel() {
        Object o = getSelectedNode().getUserObject();
        String deviceName = null;
        if (o instanceof BrowserDevice)
            deviceName = ((BrowserDevice) o).name;
        else if (o instanceof BrowserServer)
            deviceName = "dserver/" + ((BrowserServer) o).name;

        if (deviceName != null)
            browser.displayHostPanel(deviceName);
    }

    //======================================================
    //======================================================
    void gotoServer() {
        Object o = getSelectedNode().getUserObject();
        if (o instanceof BrowserDevice) {
            BrowserDevice dev = ((BrowserDevice) o);
            try {
                DeviceInfo info = dev.get_info();
                String serverName = info.server;
                String binaryFile = serverName.substring(0, serverName.indexOf('/'));
                String instance = serverName.substring(serverName.indexOf('/') + 1);

                DefaultMutableTreeNode[] path = new DefaultMutableTreeNode[4];
                int idx = 0;
                path[idx] = root;
                boolean found = false;
                for (int i = 0; !found && i < path[idx].getChildCount(); i++) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path[idx].getChildAt(i);
                    if (found = node.toString().equals("Servers"))
                        path[++idx] = node;
                }

                //  Search server binary file
                found = false;
                for (int i = 0; !found && i < path[idx].getChildCount(); i++) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path[idx].getChildAt(i);
                    if (found = node.toString().equals(binaryFile))
                        path[++idx] = node;
                }
                if (!found) {
                    browser.setText(binaryFile + " Not Found !");
                    return;
                }

                //  First time check if already created
                if (path[idx].getChildCount() == 1)
                    if (path[idx].getChildAt(0).toString().equals("dummy"))
                        createInstanceNodes(path[idx]);

                //  Search server instance
                found = false;
                for (int i = 0; !found && i < path[idx].getChildCount(); i++) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path[idx].getChildAt(i);
                    Object obj = node.getUserObject();
                    if (obj instanceof BrowserServer) {
                        BrowserServer serv = ((BrowserServer) obj);
                        if (found = serv.instance.equals(instance))
                            path[++idx] = node;
                    }
                }
                if (!found) {
                    browser.setText(instance + " Not Found !");
                    return;
                }
                TreePath tp = new TreePath(path);
                setSelectionPath(tp);
                scrollPathToVisible(tp);
            } catch (DevFailed e) {
                displayException(e);
            }
        }

    }

    //======================================================
    //======================================================
    String getCollection() {
        /*
        DefaultMutableTreeNode node = getSelectedNode();
        TreeNode[]    path = node.getPath();
        return path[1].toString();
        */
        Object o = getSelectedNode().getUserObject();
        if (o instanceof BrowserDevice_2)
            return "Servers";
        else
            return "Devices";
    }


    //==========================================================
    //==========================================================
    private class BrowserServer {
        String name;
        String instance;
        BrowserDevice_2 dev;

        //==========================================================
        BrowserServer(String binfile, String instance) {
            this.name = binfile + "/" + instance;
            this.instance = instance;
            try {
                dev = new BrowserDevice_2("dserver/" + name);
            } catch (DevFailed e) { /* Do nothing */}
        }

        //==========================================================
        //==========================================================
        public String toString() {
            return instance;
        }
        //==========================================================
    }

    //==========================================================
    //==========================================================
    private class BrowserDevice extends DeviceProxy {
        String name;
        String member;

        //==========================================================
        BrowserDevice(String name) throws DevFailed {
            super(name);
            this.name = name;
            int idx = name.lastIndexOf('/');
            if (idx < 0)
                this.member = name;
            else
                this.member = name.substring(idx + 1);
        }

        //==========================================================
        BrowserDevice(String name, String aliasname) throws DevFailed {
            super(name);
            this.name = name;
            member = aliasname;
        }

        //==========================================================
        public String toString() {
            return member;
        }
    }

    //==========================================================
    //==========================================================
    private class BrowserDevice_2 extends BrowserDevice {
        //==========================================================
        BrowserDevice_2(String name) throws DevFailed {
            super(name);
        }

        //==========================================================
        public String toString() {
            return name;
        }
    }

    //==========================================================
    //==========================================================
    private class BrowserAttribute {
        BrowserDevice dev;
        String name;
        String attname;

        //==========================================================
        BrowserAttribute(String name, BrowserDevice dev) {
            this.name = name;
            this.dev = dev;
            this.attname = dev.name + "/" + name;
        }

        //==========================================================
        public String toString() {
            return name;
        }
    }


    //===============================================================
    /**
     * Renderer Class
     */
    //===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer {
        private ImageIcon tangoIcon;
        private ImageIcon serv_icon;
        private ImageIcon dev_icon;
        private ImageIcon attr_icon;
        private ImageIcon class_icon;
        private Font[] fonts;

        private final int TITLE = 0;
        private final int DEVICE = 1;
        private final int ATTR = 2;

        //===============================================================
        //===============================================================
        public TangoRenderer() {
            tangoIcon = Utils.getInstance().getIcon("TransparentTango.png", 0.15);
            serv_icon = Utils.getInstance().getIcon("server.gif");
            dev_icon = Utils.getInstance().getIcon("device.gif");
            attr_icon = Utils.getInstance().getIcon("leaf.gif");
            class_icon = Utils.getInstance().getIcon("class.gif");

            fonts = new Font[3];
            fonts[TITLE] = new Font("courrier", Font.BOLD, 18);
            //	width fixed font
            fonts[DEVICE] = new Font("Monospaced", Font.BOLD, 12);
            fonts[ATTR] = new Font("Monospaced", Font.PLAIN, 12);
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

            if (row == 0) {
                //	ROOT
                setFont(fonts[TITLE]);
                setIcon(tangoIcon);
            } else {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;

                if (node.getUserObject() instanceof String) {
                    setFont(fonts[TITLE]);
                    if (obj.toString().equals("Servers"))
                        setIcon(serv_icon);
                    else if (obj.toString().equals("Devices"))
                        setIcon(dev_icon);
                    else if (obj.toString().equals("Aliases"))
                        setIcon(dev_icon);
                    else {
                        setFont(fonts[DEVICE]);
                        setIcon(class_icon);
                    }
                } else if (node.getUserObject() instanceof BrowserAttribute)//getAttribute(node)!=null)
                {
                    setFont(fonts[ATTR]);
                    setIcon(attr_icon);
                } else if (node.getUserObject() instanceof BrowserDevice) {
                    setFont(fonts[DEVICE]);
                    setIcon(dev_icon);
                } else if (node.getUserObject() instanceof BrowserServer) {
                    setFont(fonts[DEVICE]);
                    setIcon(serv_icon);
                }
            }
            return this;
        }
    }//	End of Renderer Class
}
