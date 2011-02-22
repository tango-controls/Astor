//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the TempClass class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.3  2004/05/04 07:05:27  pascal_verdier
// Bug on notify daemon fixed.
// server reconection transparency added.
//
// Revision 3.2  2004/04/13 12:17:29  pascal_verdier
// DeviceTree class uses the new browsing database commands.
//
// Revision 3.1  2003/11/25 15:56:45  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
//
// Copyright 1995 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;

import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;


import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class DeviceTree extends JScrollPane  implements AstorDefs
{
	private Astor				appli;
	private JTree				tree;
	private JTextArea			infoLabel;
	private DefaultTreeModel	treeModel;
	private final String[]		collections = { "Servers", "Devices" };

	private final int	SERVERS = 0;
	private final int	DEVICES = 1;

	private Monitor	monitor;

 	//===============================================================
	//===============================================================
	public DeviceTree(Astor appli, Monitor monitor, JTextArea lbl, String title)
	{
		this.appli   = appli;
		this.monitor = monitor;
		infoLabel    = lbl;

		//	Build panel and its tree
		initComponent(title);
	}
	//===============================================================
	//===============================================================
	private void initComponent(String title)
	{
	
		//Create the nodes.
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(title);

		createNodes(root);

		//Create a tree that allows one selection at a time.
		tree = new JTree(root);
		tree.getSelectionModel().setSelectionMode
        		(TreeSelectionModel.SINGLE_TREE_SELECTION);

		//	Create Tree and Tree model
		//------------------------------------
		treeModel = new DefaultTreeModel(root);
		tree.setModel(treeModel);

		//Enable tool tips.
		ToolTipManager.sharedInstance().registerComponent(tree);

		/*
		 * Set the icon for leaf nodes.
		 */
		tree.setCellRenderer(new TangoRenderer());

		//	Add Action listener
		//------------------------------------
		tree.addMouseListener (new java.awt.event.MouseAdapter () {
			public void mouseClicked (java.awt.event.MouseEvent evt) {
				treeMouseClicked (evt);
			}
		});
		//	Add tree to scroll pane
		add(tree);
		setPreferredSize(new Dimension(280, 400));
		setViewportView (tree);
		setVisible(true);
	}
	//===============================================================
	/**
	 * Create nodes for Servers and devices
	 */
	//===============================================================
	private void createNodes(DefaultMutableTreeNode root)
	{
		DefaultMutableTreeNode[] collnodes =
					new DefaultMutableTreeNode[collections.length];

		for (int i=0 ; i<collections.length ; i++)
		{
			collnodes[i] = new DefaultMutableTreeNode(collections[i]);
			root.add(collnodes[i]);
		}

		try
		{
			//----------------------------
			//	Builds Servers Tree
			//----------------------------
			DefaultMutableTreeNode	s_node = null;
			double	ratio = 0.0;
			String[] servers = ApiUtil.get_db_obj().get_server_name_list();
			for (int i=0 ; i<servers.length ; i++)
			{
				ratio = (double)i/servers.length/2;
				monitor.setProgressValue(ratio,
					"Building for Server " + servers[i]);
				//	Add a server node
				s_node = new DefaultMutableTreeNode(servers[i]);
				collnodes[SERVERS].add(s_node);
				//	get instances
				String[] instances =
					ApiUtil.get_db_obj().get_instance_name_list(servers[i]);
				//	Then add instance nodes
				for (int j=0 ; j<instances.length ; j++)
					s_node.add(new DefaultMutableTreeNode(instances[j]));

				try { Thread.sleep(10);}catch(Exception e){}
			}





			//----------------------------
			//	Builds Devices Tree
			//----------------------------
			String[]	domain;
			String[]	family;
			String[]	member;
			//	Query database for devices list and build tree
			//----------------------------------
			Database	dbase = ApiUtil.get_db_obj();
			//	Query database for Domains list
			domain = dbase.get_device_domain("*");
			for (int i=0 ; i<domain.length ; i++)
			{
				ratio = (double)i/domain.length/2 + 0.5;
				monitor.setProgressValue(ratio,
					"Building for Device domain " + domain[i]);
				DefaultMutableTreeNode	d_node =
						new DefaultMutableTreeNode(domain[i]);
				collnodes[DEVICES].add(d_node);

				//	Query database for Families list
				String	wildcard = domain[i] + "/*";
				family = dbase.get_device_family(wildcard);
				for (int f=0 ; f<family.length ; f++)
				{
					DefaultMutableTreeNode	f_node =
							new DefaultMutableTreeNode(family[f]);
					d_node.add(f_node);

					//	Query database for members list
					wildcard = domain[i] + "/" + family[f] + "/*";
					member = dbase.get_device_member(wildcard);
					for (int m=0 ; m<member.length ; m++)
					{
						DefaultMutableTreeNode	m_node =
								new DefaultMutableTreeNode(member[m]);
						f_node.add(m_node);
					}
				}
				try { Thread.sleep(10);}catch(Exception e){}
			}
		}
		catch(DevFailed e)
		{
			app_util.PopupError.show(appli, e);
		}
    }

	//======================================================
	/**
	 *	Manage event on clicked mouse on PogoTree object.
	 */
	//======================================================
	private void treeMouseClicked (java.awt.event.MouseEvent evt)
	{
		//	Check if click is on a node
		if (tree.getRowForLocation(evt.getX(), evt.getY())<1)
			return;

		TreePath	path = tree.getPathForLocation(evt.getX(), evt.getY());
		int			mask = evt.getModifiers();
		//	Check if btn1
		//-------------------------------------
		if ((mask & evt.BUTTON1_MASK)!=0)
		{
			String	devname = null;
			switch (path.getPathCount())
			{
			//	If device
			case 5:
				//	Get the device name from path
				devname =	getDeviceName(path);
				if (devname==null)	return;
				break;
			//	if server
			case 4:
				String	servname =	getServerName(path);
				if (servname==null)	return;
				devname = "dserver/" + servname;
				break;
			default:
				return;
			}
			try {
				//	Display import Info
				DbDevImportInfo	info =
					ApiUtil.get_db_obj().import_device(devname);
				infoLabel.setText(info.toString());
			}
			catch (DevFailed e) {
				infoLabel.setText(" ");
			}

			//	Popup Host Info Dialog only if double click
			//--------------------------------------------------
			if(evt.getClickCount() == 2)
			{
				showHostInfoDialogForDevice(devname, evt);
			}
		}
	}
	//======================================================
	//======================================================
	private void showHostInfoDialogForDevice(String devname, MouseEvent evt)
	{
		try
		{
			//	Get host name from device
			String	hostname = new IORdump(devname).get_host();
			if (hostname==null)
				Except.throw_exception("UNKNOWN_HOST",
					"May be this device has never been exported !", "");
			//	Take off IP address if exists
			StringTokenizer st = new StringTokenizer(hostname);
			hostname = st.nextToken();
			//	Take off name extention (e.g. .esrf.fr) if exists
			st = new StringTokenizer(hostname, ".");
			hostname = st.nextToken();

			//	Select Host on main Tree and Popup host panel
			appli.tree.setSelectionPath(hostname);
			appli.tree.displayHostInfo(getLocationOnScreen());
		}
		catch (DevFailed e)
		{
			app_util.PopupError.show(this, e);
		}
	}
	//======================================================
	//======================================================
	private String getDeviceName(TreePath path)
	{
		//	Check if Device
		String	collec = path.getPathComponent(1).toString();
		if (collec.equals(collections[DEVICES])==false)
			return null;

		String	devname = 
			(String)path.getPathComponent(2).toString() + "/" +
			(String)path.getPathComponent(3).toString() + "/" +
			(String)path.getPathComponent(4).toString();

		return devname;
	}
	//======================================================
	//======================================================
	private String getServerName(TreePath path)
	{
		//	Check if Server
		String	collec = path.getPathComponent(1).toString();
		if (collec.equals(collections[SERVERS])==false)
			return null;

		String	servname = 
			(String)path.getPathComponent(2).toString() + "/" +
			(String)path.getPathComponent(3).toString();

		return servname;
	}


//===============================================================
/**
 *	Renderer Class
 */
//===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer
	{
		private ImageIcon	tangoIcon;
		private String		tango_host = AstorUtil.getTangoHost();		
		private ImageIcon	serv_icon;
		private ImageIcon	dev_icon;
		private Font[]		fonts;

		private final int	TITLE = 0;
		private final int	LEAF  = 1;

		//===============================================================
		//===============================================================
		public TangoRenderer()
		{
			//tangoIcon = new ImageIcon(getClass().getResource(img_path + "tango_icon.jpg"));
			tangoIcon = new ImageIcon(getClass().getResource(img_path + "network5.gif"));

			serv_icon = new ImageIcon(getClass().getResource(img_path + "server.gif"));
			dev_icon = new ImageIcon(getClass().getResource(img_path + "device.gif"));
			fonts = new Font[2];
			fonts[TITLE] = new Font("helvetica", Font.BOLD, 18);
			fonts[LEAF]  = new Font("helvetica", Font.PLAIN, 12);
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
			if (row==0)
			{
				//	ROOT
 				setBackgroundSelectionColor(java.awt.Color.white);
				setIcon(tangoIcon);
				setFont(fonts[TITLE]);
				setToolTipText(tango_host);
			}
			else
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
				Object user_obj = node.getUserObject();
				//setIcon(node_icon);
				switch (node.getLevel())
				{
				case 1:
 					setBackgroundSelectionColor(java.awt.Color.white);
					setToolTipText("Tango " + user_obj.toString());
					setFont(fonts[TITLE]);
					break;
				default:
 					setBackgroundSelectionColor(java.awt.Color.lightGray);
					if (leaf)
					{
						//	Check if collection is device or server
						//	to display associated icon
						TreeNode[]	path = node.getPath();
						DefaultMutableTreeNode c_node =
									(DefaultMutableTreeNode)path[1];
						String collec = (c_node.getUserObject()).toString();
						if (collec.equals(collections[SERVERS]))
							setIcon(serv_icon);
						else
							setIcon(dev_icon);
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

