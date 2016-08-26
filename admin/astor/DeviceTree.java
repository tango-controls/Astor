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
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
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


public class DeviceTree extends JTree implements AstorDefs {
    private CtrlSystem cs;
    private Astor astor;
    private JTextArea infoLabel;
    private final String[] collections = {"Controlled Servers", "Not Controlled servers", "Devices"};

    private final int CTRL_SERVERS = 0;
    private final int N_CTRL_SERVERS = 1;
    private final int DEVICES = 2;

    private Monitor monitor;
    boolean canceled = false;

    //===============================================================
    //===============================================================
    public DeviceTree(Astor astor, Monitor monitor, JTextArea lbl, String title) {
        super();
        this.astor = astor;
        this.monitor = monitor;
        infoLabel = lbl;
        cs = new CtrlSystem(astor.tree.hosts.length);

        //	Build panel and its tree
        initComponent(title);
    }

    //===============================================================
    //===============================================================
    private void initComponent(String title) {
        //Create the nodes.
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(title);
        createNodes(root);

        //	Create the tree that allows one selection at a time.
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //	Create Tree and Tree model
        //------------------------------------
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);

        // Set the icon for leaf nodes.
        setCellRenderer(new TangoRenderer());

        //	Listen for collapse tree
        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
                collapsedPerfomed(e);
            }

            public void treeExpanded(TreeExpansionEvent e) {
                //expandedPerformed(e);
            }
        });
        //	Add Action listener
        //------------------------------------
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);
            }
        });
    }
    //===============================================================
    /**
     * Create nodes for Servers and devices
     */
    //===============================================================
    private DefaultMutableTreeNode[] collnodes;

    private void createNodes(DefaultMutableTreeNode root) {
        collnodes = new DefaultMutableTreeNode[collections.length];
        for (int i = 0; i < collections.length; i++) {
            collnodes[i] = new DefaultMutableTreeNode(collections[i]);
            root.add(collnodes[i]);
        }

        try {
            //----------------------------
            //	Builds Servers Tree
            //----------------------------
            DefaultMutableTreeNode[] s_node = new DefaultMutableTreeNode[2];
            double ratio;
            String[] servers = ApiUtil.get_db_obj().get_server_name_list();
            for (int i = 0; i < servers.length; i++) {
                ratio = (double) i / servers.length / 2;

                //	Add a server node
                s_node[0] = new DefaultMutableTreeNode(servers[i]);
                s_node[1] = new DefaultMutableTreeNode(servers[i]);
                //	get instances
                String[] instances =
                        ApiUtil.get_db_obj().get_instance_name_list(servers[i]);

                //	Then add instance nodes
                for (String instance : instances) {
                    String servname = servers[i] + "/" + instance;
                    if ((canceled = monitor.isCanceled()))
                        return;
                    monitor.setProgressValue(ratio, "Building for Server " + servname);
                    //	Check if controlled and add to server node
                    DbServInfo info = new DbServer(servname).get_info();
                    if (info.controlled) {
                        //	Update ContolSystem info
                        cs.nb_instances++;
                        DbServer dbs = new DbServer(servname);
                        String[] classes = dbs.get_class_list();
                        cs.nb_classes += classes.length;
                        for (String classname : classes) {
                            //	get device list
                            String[] dn = dbs.get_device_name(classname);
                            cs.nb_devices += dn.length;
                        }

                        s_node[CTRL_SERVERS].add(new DefaultMutableTreeNode(instance));
                    } else {
                        cs.not_c_instances++;
                        s_node[N_CTRL_SERVERS].add(new DefaultMutableTreeNode(instance));
                    }
                }
                //	Add server node if has at least one child
                if (s_node[CTRL_SERVERS].getChildCount() > 0) {
                    cs.nb_servers++;
                    collnodes[CTRL_SERVERS].add(s_node[CTRL_SERVERS]);
                }
                if (s_node[N_CTRL_SERVERS].getChildCount() > 0)
                    collnodes[N_CTRL_SERVERS].add(s_node[N_CTRL_SERVERS]);

                Thread.sleep(10);
            }


            //----------------------------
            //	Builds Devices Tree
            //----------------------------
            String[] domain;
            String[] family;
            String[] member;
            //	Query database for devices list and build tree
            //----------------------------------
            Database dbase = ApiUtil.get_db_obj();
            //	Query database for Domains list
            domain = dbase.get_device_domain("*");
            for (int i = 0; i < domain.length; i++) {
                ratio = (double) i / domain.length / 2 + 0.5;
                DefaultMutableTreeNode d_node =
                        new DefaultMutableTreeNode(domain[i]);
                collnodes[DEVICES].add(d_node);

                //	Query database for Families list
                String wildcard = domain[i] + "/*";
                family = dbase.get_device_family(wildcard);
                for (String aFamily : family) {
                    if ((canceled = monitor.isCanceled()))
                        return;
                    monitor.setProgressValue(ratio,
                            "Building for Device family " + domain[i] + "/" + aFamily);

                    DefaultMutableTreeNode f_node =
                            new DefaultMutableTreeNode(aFamily);
                    d_node.add(f_node);

                    //	Query database for members list
                    wildcard = domain[i] + "/" + aFamily + "/*";
                    member = dbase.get_device_member(wildcard);
                    for (String aMember : member) {
                        DefaultMutableTreeNode m_node =
                                new DefaultMutableTreeNode(aMember);
                        f_node.add(m_node);
                    }
                }
                Thread.sleep(10);
            }
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(astor, null, e);
        } catch (InterruptedException e) { /* */ }
    }

    //======================================================

    /**
     * Manage event on clicked mouse on PogoTree object.
     *
     * @param evt the mouse event
     */
    //======================================================
    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
        //	Check if click is on a node
        if (getRowForLocation(evt.getX(), evt.getY()) < 1)
            return;

        TreePath path = getPathForLocation(evt.getX(), evt.getY());
        if (path==null)
            return;
        int mask = evt.getModifiers();
        //	Check if btn1
        //-------------------------------------
        if ((mask & MouseEvent.BUTTON1_MASK) != 0) {
            String deviceName;
            switch (path.getPathCount()) {
                //	If device
                case 5:
                    //	Get the device name from path
                    deviceName = getDeviceName(path);
                    if (deviceName == null) return;
                    break;
                //	if server
                case 4:
                    String serverName = getServerName(path);
                    if (serverName == null) return;
                    deviceName = "dserver/" + serverName;
                    break;
                default:
                    return;
            }
            try {
                //	Display device Info
                DeviceInfo info =
                        ApiUtil.get_db_obj().get_device_info(deviceName);
                infoLabel.setText(info.toString());
            } catch (DevFailed e) {
                infoLabel.setText(" ");
            }

            //	Popup Host Info Dialog only if double click
            //--------------------------------------------------
            if (evt.getClickCount() == 2) {
                showHostInfoDialogForDevice(deviceName, evt);
            }
        }
    }

    //======================================================
    //======================================================
    public void collapsedPerfomed(TreeExpansionEvent e) {
        //	Get path
        TreePath path = e.getPath();
        if (path.getPathCount() > 2)
            return;
        //	Get concerned node
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) path.getPathComponent(path.getPathCount() - 1);
        //	do not collapse if Root
        if (path.getPathCount() == 1) {
            setExpandedState(new TreePath(node.getPath()), true);
            Utils.popupMessage(astor, cs.toString(), "TangoClass.gif");
        }
    }

    //======================================================
    //======================================================
    String[][] getNotCtrlServers() {
        DefaultMutableTreeNode node = collnodes[N_CTRL_SERVERS];
        int nbServers = node.getChildCount();
        List<String> serverNames = new ArrayList<>();
        //	Search all Classes
        for (int i = 0; i < nbServers; i++) {
            node = node.getNextNode();
            String serverName = node.toString();
            int nb_inst = node.getChildCount();
            try {
                //	For all instances
                for (int j = 0; j < nb_inst; j++) {
                    node = node.getNextNode();
                    String server = serverName + "/" + node;
                    DbServInfo info = new DbServer(server).get_info();
                    //	Store in vector if controlled
                    if (!info.controlled)
                        //	Starter cannot controle itself.
                        if (!serverName.equals("Starter"))
                            serverNames.add(server);
                }
            } catch (DevFailed e) {
                serverNames.add(e.errors[0].desc);
            }
        }

        //	Add last exported date
        String[][] result = new String[serverNames.size()][];
        for (int i = 0; i < serverNames.size(); i++) {
            String server = serverNames.get(i);
            result[i] = new String[2];
            result[i][0] = server;
            //	Get last started date
            try {
                DeviceInfo info2 = new DbDevice("dserver/" + server).get_info();
                result[i][1] = info2.last_exported;
            } catch (DevFailed e) {
                result[i][1] = e.errors[0].desc;
            }
        }

        return result;
    }

    //======================================================
    //======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void showHostInfoDialogForDevice(String devname, MouseEvent evt) {
        try {
            //	Get host name from device
            String hostname = new IORdump(devname).get_host();
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
                astor.tree.setSelectionPath(hostname);
                astor.tree.displayHostInfo();
            }
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }

    //======================================================
    //======================================================
    private String getDeviceName(TreePath path) {
        //	Check if Device
        String collec = path.getPathComponent(1).toString();
        if (!collec.equals(collections[DEVICES]))
            return null;

        return
                path.getPathComponent(2).toString() + "/" +
                        path.getPathComponent(3).toString() + "/" +
                        path.getPathComponent(4).toString();
    }

    //======================================================
    //======================================================
    private String getServerName(TreePath path) {
        //	Check if Server
        String collec = path.getPathComponent(1).toString();
        //if (collec.equals(collections[CTRL_SERVERS])==false)
        if (collec.equals(collections[CTRL_SERVERS]) && collec.equals(collections[N_CTRL_SERVERS]))
            return null;

        return
                path.getPathComponent(2).toString() + "/" +
                        path.getPathComponent(3).toString();
    }

    //======================================================
    //======================================================
    String csInfo() {
        return cs.toString();
    }
//===============================================================

    /**
     * Control System info class
     */
//===============================================================
    class CtrlSystem {
        int nb_hosts = 0;
        int nb_servers = 0;
        int nb_instances = 0;
        int nb_classes = 0;
        int nb_devices = 0;
        int not_c_instances = 0;

        public CtrlSystem(int nb_hosts) {
            this.nb_hosts = nb_hosts;
        }

        public String toString() {
            String str = "";
            str += nb_hosts + "  Hosts controlled.\n";
            str += nb_servers + "  Different controlled servers.\n";
            str += nb_instances + "  Controlled servers/instances.\n";
            str += nb_classes + "  Controlled classes.\n";
            str += nb_devices + "  Controlled devices.\n\n";
            str += not_c_instances + "  NOT controlled servers/instances.";
            return str;
        }
    }
    //===============================================================

//===============================================================

    /**
     * Renderer Class
     */
//===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer {
        private ImageIcon tangoIcon;
        private ImageIcon serv_icon;
        private ImageIcon dev_icon;
        private Font[] fonts;

        private final int TITLE = 0;
        private final int LEAF = 1;

        //===============================================================
        //===============================================================
        public TangoRenderer() {
            tangoIcon = Utils.getInstance().getIcon("TransparentTango.gif", 0.15);
            serv_icon = Utils.getInstance().getIcon("server.gif");
            dev_icon = Utils.getInstance().getIcon("device.gif");
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
                setToolTipText("Double click to popup info");
            } else {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
                Object user_obj = node.getUserObject();
                //setIcon(node_icon);
                switch (node.getLevel()) {
                    case 1:
                        setBackgroundSelectionColor(java.awt.Color.white);
                        setToolTipText("Tango " + user_obj.toString());
                        setFont(fonts[TITLE]);
                        break;
                    default:
                        setBackgroundSelectionColor(java.awt.Color.lightGray);
                        if (leaf) {
                            //	Check if collection is device or server
                            //	to display associated icon
                            TreeNode[] path = node.getPath();
                            DefaultMutableTreeNode c_node =
                                    (DefaultMutableTreeNode) path[1];
                            String collec = (c_node.getUserObject()).toString();
                            if (collec.equals(collections[DEVICES]))
                                setIcon(dev_icon);
                            else
                                setIcon(serv_icon);
                            setToolTipText("Double click to popup host window.");
                        }
                        setFont(fonts[LEAF]);
                        break;
                }
            }
            return this;
        }
    }//	End of Renderer Class
}

