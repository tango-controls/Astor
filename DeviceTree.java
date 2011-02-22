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
// Revision 3.12  2005/11/24 12:24:57  pascal_verdier
// DevBrowser utility added.
// MkStarter utility added.
//
// Revision 3.11  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.10  2005/10/20 13:24:49  pascal_verdier
// Screen position management has been changed.
//
// Revision 3.9  2005/03/11 14:07:53  pascal_verdier
// Pathes have been modified.
//
// Revision 3.8  2005/02/21 13:44:16  pascal_verdier
// Display not controled servers in a JTable.
//
// Revision 3.7  2005/02/16 13:41:02  pascal_verdier
// Add controlled servers info in DeviceTree class.
//
// Revision 3.6  2005/01/18 08:48:20  pascal_verdier
// Tools menu added.
// Not controlled servers list added.
//
// Revision 3.5  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.4  2004/07/08 11:22:58  pascal_verdier
// First revision able to use events.
//
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
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;
import java.util.Vector;

public class DeviceTree extends JTree  implements AstorDefs
{
	private	CtrlSystem			cs;
	private Astor				appli;
	private JTextArea			infoLabel;
	private DefaultTreeModel	treeModel;
	private final String[]		collections = { "Controlled Servers", "Not Controlled servers", "Devices" };

	private final int	CTRL_SERVERS   = 0;
	private final int	N_CTRL_SERVERS = 1;
	private final int	DEVICES        = 2;

	private Monitor	monitor;

 	//===============================================================
	//===============================================================
	public DeviceTree(Astor appli, Monitor monitor, JTextArea lbl, String title)
	{
		super();
		this.appli   = appli;
		this.monitor = monitor;
		infoLabel    = lbl;
		cs = new CtrlSystem(appli.tree.hosts.length);

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

		//	Create the tree that allows one selection at a time.
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		//	Create Tree and Tree model
		//------------------------------------
		treeModel = new DefaultTreeModel(root);
		setModel(treeModel);

		//Enable tool tips.
		ToolTipManager.sharedInstance().registerComponent(this);

         // Set the icon for leaf nodes.
		setCellRenderer(new TangoRenderer());

		//	Listen for collapse tree
		addTreeExpansionListener(new TreeExpansionListener () {
			public void treeCollapsed(TreeExpansionEvent e) {
				collapsedPerfomed(e);
			}
			public void treeExpanded(TreeExpansionEvent e) {
				//expandedPerfomed(e);
			}
		});
		//	Add Action listener
		//------------------------------------
		addMouseListener (new java.awt.event.MouseAdapter () {
			public void mouseClicked (java.awt.event.MouseEvent evt) {
				treeMouseClicked (evt);
			}
		});
	}
	//===============================================================
	/**
	 * Create nodes for Servers and devices
	 */
	//===============================================================
	private	DefaultMutableTreeNode[] collnodes;
	private void createNodes(DefaultMutableTreeNode root)
	{
		collnodes = new DefaultMutableTreeNode[collections.length];
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
			DefaultMutableTreeNode[]	s_node = new DefaultMutableTreeNode[2];
			double	ratio = 0.0;
			String[] servers = ApiUtil.get_db_obj().get_server_name_list();
			for (int i=0 ; i<servers.length ; i++)
			{
				ratio = (double)i/servers.length/2;

				//	Add a server node
				s_node[0] = new DefaultMutableTreeNode(servers[i]);
				s_node[1] = new DefaultMutableTreeNode(servers[i]);
				//	get instances
				String[] instances =
					ApiUtil.get_db_obj().get_instance_name_list(servers[i]);

				//	Then add instance nodes
				for (int j=0 ; j<instances.length ; j++)
				{
					String	servname = servers[i] + "/" + instances[j];
					monitor.setProgressValue(ratio, "Building for Server " + servname);
					//	Check if controlled and add to server node
					DbServInfo	info = new DbServer(servname).get_info();
					if (info.controlled)
					{
						//	Update ContolSystem info
						cs.nb_instances++;
						DbServer	dbs = new DbServer(servname);
						String[]	classes = dbs.get_class_list();
						cs.nb_classes += classes.length;
						for (int c=0 ; c<classes.length ; c++)
						{
							//	get device list
							String[]	dn = dbs.get_device_name(classes[c]);
							cs.nb_devices += dn.length;
						}
						
						s_node[CTRL_SERVERS].add(new DefaultMutableTreeNode(instances[j]));
					}
					else
					{
						cs.not_c_instances++;
						s_node[N_CTRL_SERVERS].add(new DefaultMutableTreeNode(instances[j]));
					}
				}
				//	Add server node if has at least one child
				if (s_node[CTRL_SERVERS].getChildCount()>0)
				{
					cs.nb_servers++;
					collnodes[CTRL_SERVERS].add(s_node[CTRL_SERVERS]);
				}
				if (s_node[N_CTRL_SERVERS].getChildCount()>0)
					collnodes[N_CTRL_SERVERS].add(s_node[N_CTRL_SERVERS]);

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
				DefaultMutableTreeNode	d_node =
						new DefaultMutableTreeNode(domain[i]);
				collnodes[DEVICES].add(d_node);

				//	Query database for Families list
				String	wildcard = domain[i] + "/*";
				family = dbase.get_device_family(wildcard);
				for (int f=0 ; f<family.length ; f++)
				{
					monitor.setProgressValue(ratio,
							"Building for Device family " + domain[i] + "/" + family[f]);

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
		if (getRowForLocation(evt.getX(), evt.getY())<1)
			return;

		TreePath	path = getPathForLocation(evt.getX(), evt.getY());
		int			mask = evt.getModifiers();
		//	Check if btn1
		//-------------------------------------
		if ((mask & MouseEvent.BUTTON1_MASK)!=0)
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
				//	Display device Info
				DeviceInfo	info =
					ApiUtil.get_db_obj().get_device_info(devname);
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
	public void collapsedPerfomed(TreeExpansionEvent e)
	{
		//	Get path
		TreePath	path = e.getPath();
		if (path.getPathCount()>2)
			return;
		//	Get concerned node
		DefaultMutableTreeNode	node =
			(DefaultMutableTreeNode)path.getPathComponent(path.getPathCount()-1);
		//	do not collapse if Root
		if (path.getPathCount()==1)
		{
			setExpandedState(new TreePath(node.getPath()), true);
			app_util.PopupMessage.showImage(appli, cs.toString(), img_path + "tango_icon.jpg");
		}
	}
	//======================================================
	//======================================================
	String[][] getNotCtrlServers()
	{
		DefaultMutableTreeNode	node = collnodes[N_CTRL_SERVERS];
		int	nb_serv = node.getChildCount();
		Vector	v_serv = new Vector();
		//	Search all Classes
		for (int i=0 ; i<nb_serv ; i++)
		{
			node = node.getNextNode();
			String	servname = node.toString();
			int	nb_inst = node.getChildCount();
			try
			{
				//	For all instances
				for (int j=0 ; j<nb_inst ; j++)
				{
					node = node.getNextNode();
					String		server = servname + "/" + node;
					DbServInfo	info = new DbServer(server).get_info();
					//	Store in vector if controlled
					if (!info.controlled)
						//	Starter cannot controle itself.
						if (!servname.equals("Starter"))
							v_serv.add(server);
				}
			}
			catch(DevFailed e){
				v_serv.add(e.errors[0].desc);
			}
		}

		//	Add last exported date
		String[][]	result = new String[v_serv.size()][];
		for (int i=0 ; i<v_serv.size() ; i++)
		{
			String	server = (String)v_serv.elementAt(i); 
			result[i]    = new String[2];
			result[i][0] = server;
			//	Get last started date
			try
			{
				DeviceInfo	info2 = new DbDevice("dserver/"+server).get_info();
				result[i][1] = info2.last_exported;
			}
			catch (DevFailed e){
				result[i][1] = e.errors[0].desc;
			}
		}

		return result;
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
			appli.tree.displayHostInfo();
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
		if (!collec.equals(collections[DEVICES]))
			return null;

		return
			path.getPathComponent(2).toString() + "/" +
			path.getPathComponent(3).toString() + "/" +
			path.getPathComponent(4).toString();
	}
	//======================================================
	//======================================================
	private String getServerName(TreePath path)
	{
		//	Check if Server
		String	collec = path.getPathComponent(1).toString();
		//if (collec.equals(collections[CTRL_SERVERS])==false)
		if (collec!=collections[CTRL_SERVERS] && collec!=collections[N_CTRL_SERVERS])
			return null;

		return
			path.getPathComponent(2).toString() + "/" +
			path.getPathComponent(3).toString();
	}
	//======================================================
	//======================================================
	String csInfo()
	{
		return cs.toString();
	}
//===============================================================
/**
 *	Control System info class
 */
//===============================================================
	class CtrlSystem
	{
		int	nb_hosts     = 0;
		int	nb_servers   = 0;
		int	nb_instances = 0;
		int	nb_classes   = 0;
		int	nb_devices   = 0;
		int	not_c_instances= 0;

		public CtrlSystem(int nb_hosts)
		{
			this.nb_hosts = nb_hosts;
		}
		public String toString()
		{
			String  str = "";
			str +=  nb_hosts     + "  Hosts controlled.\n";
			str +=  nb_servers   + "  Different controlled servers.\n";
			str +=  nb_instances + "  Controlled servers/instances.\n";
			str +=  nb_classes   + "  Controlled classes.\n";
			str +=  nb_devices   + "  Controlled devices.\n\n";
			str +=  not_c_instances+ "  NOT controlled servers/instances.";
			return str;
		}
	}
	//===============================================================

//===============================================================
/**
 *	Renderer Class
 */
//===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer
	{
		private ImageIcon	tangoIcon;
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
				setToolTipText("Double click to popup info");
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

