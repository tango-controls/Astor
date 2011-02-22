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
// Revision 3.17  2005/06/06 09:04:08  pascal_verdier
// Case of no Satretr defined bug fixed.
//
// Revision 3.16  2005/06/02 09:02:36  pascal_verdier
// Minor changes.
//
// Revision 3.15  2005/03/11 14:07:53  pascal_verdier
// Pathes have been modified.
//
// Revision 3.14  2005/02/16 13:41:05  pascal_verdier
// Add controlled servers info in DeviceTree class.
//
// Revision 3.13  2005/02/10 15:38:19  pascal_verdier
// Event subscritions have been serialized.
//
// Revision 3.12  2005/02/03 13:31:58  pascal_verdier
// Display message if subscribe event failed.
// Display hosts using events (Starter/Astor).
//
// Revision 3.11  2005/01/18 08:48:20  pascal_verdier
// Tools menu added.
// Not controlled servers list added.
//
// Revision 3.10  2004/11/23 14:05:57  pascal_verdier
// Minor changes.
//
// Revision 3.9  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.8  2004/06/17 09:19:58  pascal_verdier
// Refresh performence problem solved by removing tool tips on JTree.
//
// Revision 3.7  2003/11/25 15:56:45  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
// Revision 3.6  2003/11/05 10:34:57  pascal_verdier
// Main Panel screen centering.
// Starter multi path added.
// little bugs fixed.
//
// Revision 3.5  2003/10/20 08:55:15  pascal_verdier
// Bug on tree popup menu position fixed.
//
// Revision 3.4  2003/09/08 12:21:36  pascal_verdier
// *** empty log message ***
//
// Revision 3.3  2003/09/08 11:05:28  pascal_verdier
// *** empty log message ***
//
// Revision 3.2  2003/07/22 14:35:20  pascal_verdier
// Minor bugs fixed.
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
// Revision 3.0  2003/06/04 12:37:52  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.1  2003/06/04 12:33:11  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
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

public class AstorTree extends JTree  implements AstorDefs
{
	private Astor				parent;
	public  TangoHost[]			hosts;
	private TangoHost			selected_host = null;
	private	DbaseObject			selected_db = null;
	private DbaseObject			dbase;
	private DefaultTreeModel	treeModel;

	/**
	 *	A jive instance to be displayed.
	 */
	private jive.MainPanel jive = null;

	/**
	 *	Popup menu to be used on right button clicked.
	 */
	private TreePopupMenu	pMenu;
	/**
	 *	Class to store many HostInfoDialog
	 */
	HostInfoDialogVector	hostDialogs = null;


	static String[]	collec_names;	

	private static Monitor	monitor;
	private static int		host_subscribed = 0;
	private static Vector	hosts_using_evt;
	private static DefaultMutableTreeNode[]	leaf;
	private static DefaultMutableTreeNode[]	collection;
 	//===============================================================
	//===============================================================
	public AstorTree(Astor parent, boolean polling) throws DevFailed
	{
		this.parent = parent;

		//	Build panel and its tree
		initComponent();

		//	Start reading devices.
		dbase.start();
		
		String	message = "";
		if (hosts.length>0)
			message = hosts[0].getName();
		monitor = new Monitor(parent, "Subscribing events",	message);
		hosts_using_evt = new Vector();
		for (int i=0 ; i<hosts.length ; i++)
		{
			//	Fix the polling status
			hosts[i].do_polling = false;
			if (polling==true)
				hosts[i].setPolling(null);

			if (hosts[i].use_events)
				hosts_using_evt.add(hosts[i]);

			//	And start the control thread
			hosts[i].thread = new HostStateThread(this, hosts[i]);
			hosts[i].thread.start();
		}
		//	Start a thread to subscribe events and a monitor to display
		new subscribeThread().start();
		updateMonitor(null);
		pMenu = new TreePopupMenu(parent, this);
		hostDialogs = new HostInfoDialogVector();

		//	Expend for database
		expandRow(1);
	}
	//===============================================================
	//===============================================================
	private static String	start_host_err = "";
	void updateMonitor(String	strerror)
	{
		if (strerror!=null)
			start_host_err += strerror + "\n\n";
		//	Do not do it at last time -> all host subscribe
		if (host_subscribed >= hosts_using_evt.size())
		{
			monitor.setProgressValue(1.0, "");
			if (start_host_err.length()>0)
				app_util.PopupMessage.show(this, start_host_err);
			return;
		}

		TangoHost	host = (TangoHost)hosts_using_evt.elementAt(host_subscribed);
		String		message = "For  " + host.getName() + "  (" +
				(host_subscribed+1) + "/" + hosts_using_evt.size() + ")";

		double		ratio = (double)(host_subscribed+1)  /
							(double)(hosts_using_evt.size()+1);
		
		//	Display only if main window displayed to be sure to be on top
		if (parent.displayed)
			monitor.setProgressValue(ratio, message);

		host_subscribed++;
	}
	//===============================================================
	//===============================================================
	void stopThreads()
	{
		for (int i=0 ; i<hosts.length ; i++)
			hosts[i].stopThread();
	}
	//===============================================================
	//===============================================================
	void expand(boolean expand)
	{
		if (expand)
			//	Expand
			for (int i=0 ; i<(hosts.length + collec_names.length+1) ; i++)
				expandRow(i);
		else
			//	Collaps
			for (int i=1 ; i<=collec_names.length; i++)
				collapseRow(i);
	}
	//===============================================================
	//===============================================================
	private void initComponent() throws DevFailed
	{
	
		//Create the nodes.
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(
                                        		 "TANGO Control System");

		initTangoObjects();
		createNodes(root);

		//Create a tree that allows one selection at a time.
		getSelectionModel().setSelectionMode
        		(TreeSelectionModel.SINGLE_TREE_SELECTION);

		//	Create Tree and Tree model
		//------------------------------------
		treeModel = new DefaultTreeModel(root);
		setModel(treeModel);

		//Enable tool tips.
		ToolTipManager.sharedInstance().registerComponent(this);

		/*
		 * Create a Renderer to set the icon for leaf nodes.
		 */
		setCellRenderer(new TangoRenderer());

		//	Listen for when the selection changes.
		addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				hostSelectionPerformed(e);
			}
		});

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
		setExpandsSelectedPaths(true);
	}
	//===============================================================
	//===============================================================
	private void initTangoObjects() throws DevFailed
	{
		//	Build Database object
		dbase = new DbaseObject(this);

		//	Build Host objects
		AstorUtil	au = AstorUtil.getInstance();
		hosts = au.getTangoHostList();
		collec_names = au.getCollectionList(hosts);
	}
	//===============================================================
	//===============================================================
	private void createNodes(DefaultMutableTreeNode root)
	{

		collection = new DefaultMutableTreeNode[collec_names.length];
		for (int i=0 ; i<collec_names.length ; i++)
		{
			collection[i] = new DefaultMutableTreeNode(collec_names[i]);
			root.add(collection[i]);
		}
		//	First create database node
		collection[0].add(new DefaultMutableTreeNode(dbase));

        //	Add Host nodes
		leaf = new DefaultMutableTreeNode[hosts.length];
		for (int i=0 ; i<hosts.length ; i++)
		{
			DefaultMutableTreeNode	host = 
				new DefaultMutableTreeNode(hosts[i]);
			hosts[i].state = unknown;
			int	idx = getHostCollection(hosts[i]);
			collection[idx].add(host);
			leaf[i] = host;
		}
    }
 	//===============================================================
	//===============================================================
	int getHostCollection(TangoHost host)
	{
		for (int i=0 ; i<collec_names.length ; i++)
			if (host.collection==null)
				return collec_names.length-1;	//	The last one
			else
				if (host.collection.equals(collec_names[i]))
					return i;
		return collec_names.length-1;	//	The last one.
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
		//	do not collapse if Root or database node
		if (path.getPathCount()==1 || node==collection[0])
		{
			//	Cancel collapse tree
			DefaultMutableTreeNode	leaf = 
					(DefaultMutableTreeNode)node.getChildAt(0);
			TreeNode[]	leaf_path = leaf.getPath();
			setExpandedState(new TreePath(leaf_path), true);

			//	If root Display TANGO information
			if (path.getPathCount()==1)
			{
				String	message  = "TANGO Control System\n\n";
				message += hosts.length + " hosts controled.\n";
				int	nb_on_events = 0;
				for (int i=0 ; i<hosts.length ; i++)
					if (hosts[i].use_events)
						nb_on_events++;
				if (nb_on_events==hosts.length)
					message += "All are controled on events.";
				else
				if (nb_on_events>0)
					message += nb_on_events + " are controled on events.";
				app_util.PopupMessage.showImage(parent, message, img_path + "tango_icon.jpg");
			}
		}
	}




//===============================================================
//	
//	Editing Tree (Rename, move...)
//
//===============================================================
	//===============================================================
	/**
	 *	Compute bound rectangle for a node
	 */
	//===============================================================
	private Rectangle computeBounds(TreePath selPath)
	{
		scrollPathToVisible(selPath);
		Rectangle r  = getPathBounds(selPath);
		Point p = r.getLocation();
		SwingUtilities.convertPointToScreen(p, this);
		r.setLocation(p);
		r.width  += 20;
		r.height += 2;
		return r;
	}
 	//===============================================================
	//===============================================================
	void changeNodeName()
	{
		Rectangle r = computeBounds(getSelectionPath());
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                   getLastSelectedPathComponent();
		//	Build the inside dialog
	  	RenameDialog dlg = new RenameDialog(parent, node.toString(), r);
		if (dlg.showDlg())
		{
			String new_name = dlg.getNewName();

			//	Change property for all Starter devices
			try
			{
				DefaultMutableTreeNode	n2 = node;
				int	nb = n2.getChildCount();
				for (int i=0 ; i<nb ; i++)
				{
					n2 = n2.getNextNode();
					TangoHost th = (TangoHost)n2.getUserObject();
					th.setCollection(new_name);
				}
			}
			catch(DevFailed e) {
				app_util.PopupError.show(parent, e);
				return;
			}
			node.setUserObject(new_name);
		}
	}
 	//===============================================================
	//===============================================================
	void addBranch(String name)
	{
		//	Create node for new branch
		DefaultMutableTreeNode	node = new DefaultMutableTreeNode(name);

		//	Add new branch and root node to add new branch
		DefaultTreeModel	model = (DefaultTreeModel)getModel();
		DefaultMutableTreeNode	root = 
				(DefaultMutableTreeNode)model.getRoot();
		model.insertNodeInto(node, root, root.getChildCount());
	}
 	//===============================================================
	//===============================================================
	void moveNode(DefaultMutableTreeNode host_node)
	{
		//	Get the old collection node
		DefaultMutableTreeNode	old_collec = 
				(DefaultMutableTreeNode) host_node.getParent();
		
		//	get the new collection
		DefaultMutableTreeNode	new_collec =
			(DefaultMutableTreeNode) getLastSelectedPathComponent();

		//	Change the Starter property
		try
		{
			TangoHost	host = (TangoHost)host_node.getUserObject();
			host.setCollection((String)new_collec.getUserObject());
		}
		catch(DevFailed e) {
			app_util.PopupError.show(parent, e);
			return;
		}

		//	Get the default model used to move node
		DefaultTreeModel	model = (DefaultTreeModel)getModel();
		model.removeNodeFromParent(host_node);
		model.insertNodeInto(host_node, new_collec, 0);
		//	Check if previous collection has still children
		if (old_collec.getChildCount()==0)
			model.removeNodeFromParent(old_collec);

		//	Ensure that the new node is visible
		scrollPathToVisible(new TreePath(host_node.getPath()));
	}
	//===============================================================
	//===============================================================
	public void hostSelectionPerformed(TreeSelectionEvent e) 
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                   getLastSelectedPathComponent();

		if (node == null) return;

		Object obj = node.getUserObject();
		if (node.isLeaf())
		{
			if (obj instanceof TangoHost)
			{
				selected_host = (TangoHost)obj;
				selected_db  = null;
			}
			else
			if (obj instanceof DbaseObject)
			{
				selected_host = null;
				selected_db  = (DbaseObject)obj;
			}
			else
			{
				selected_host = null;
				selected_db  = null;
			}
		}
		else
		{
			selected_host = null;
			selected_db  = null;
		}
	}




//======================================================
//
//	Mouse event managment.
//
//======================================================
	//======================================================
	/**
	 *	Manage event on clicked mouse on PogoTree object.
	 */
	//======================================================
	private void treeMouseClicked (java.awt.event.MouseEvent evt)
	{
		/***** TRACE clicked point ***********
		Object	o1 = evt.getSource();
		System.out.println(o1 + " - " + evt.getX() + ", " + evt.getY());
		Point	p1 = ((Component)o1).getLocationOnScreen();
		System.out.println(p1);
		*****************************/

		//	Check if click is on a node
		if (getRowForLocation(evt.getX(), evt.getY())<1)
			return;

		int mask = evt.getModifiers();
		//	Do something only if double click
		//-------------------------------------
		if(evt.getClickCount() == 2)
		{
			//	Check if btn1
			//------------------
			if ((mask & evt.BUTTON1_MASK)!=0)
				if (selected_host!=null)
				{
					//	Get position to display dialog
					Object	o = evt.getSource();
					Point	p = ((Component)o).getLocationOnScreen();
					p.x += evt.getX();
					p.y += evt.getY();
					displayHostInfo(p);
				}
				else
					if (selected_db!=null)
						displayDbaseInfo();
		}
		else
		{
			//	Check if btn3
			//------------------
			if ((mask & evt.BUTTON3_MASK)!=0)
				pMenu.showMenu(evt);
		}
	}
	//======================================================
	//======================================================
	void setSelectionPath(String hostname)
	{

		//	Search host node
		for (int i=0 ;i<leaf.length ; i++)
		{
			String	leafname = (leaf[i].getUserObject()).toString();
			//	Check if leaf has additional comments then take off
			int	start;
			if ((start=leafname.indexOf("("))>0)
				leafname = leafname.substring(0, start).trim();
			if (leafname.equals(hostname))
			{
				//	Get node path to set selection
				TreePath	path = new TreePath(leaf[i].getPath());
				setSelectionPath(path);
				return;
			}
		}
		//	If not found
		setSelectionRow(0);
	}
	//======================================================
	//======================================================
	void displayDbaseInfo()
	{
		if (dbase.state==faulty)
		{
			app_util.PopupError.show(parent, dbase.except);
		}
		else
		{
			//	Start Jive
			//---------------------------
			if (jive==null)
				jive = new jive.MainPanel();

			jive.setVisible(true);
			jive.toFront();
		}
	}
	//===============================================================
	/**
	 *	Replace an old leaf by a new one containing the new object.
	 *	This method is maily used to resize node (when usage changed)
	 */
	//===============================================================
	void changeHostNode(TangoHost h)
	{
		//	Get selected node
		DefaultMutableTreeNode	node =
			(DefaultMutableTreeNode) getLastSelectedPathComponent();

		//	Get parent node and node position.
		DefaultMutableTreeNode	parent_node =
							(DefaultMutableTreeNode)node.getParent();
		int	pos =0;
		for (int i=0 ; i<parent_node.getChildCount() ; i++)
			if (parent_node.getChildAt(i).equals(node))
				pos = i;

		//	Build ne node and insert
		DefaultMutableTreeNode	new_node = new DefaultMutableTreeNode(h);
		treeModel.insertNodeInto(new_node, parent_node, pos);

		//	Remove old one
		treeModel.removeNodeFromParent(node);
		
		//	And set selection on new
		TreeNode[]	path = new_node.getPath();
		setSelectionPath(new TreePath(path));
	}
	//======================================================
	//======================================================
	void displayHostInfo(Point p)
	{
		//	Check if a host is selected
		if (selected_host==null)
		{
			app_util.PopupError.show(this, 
					"this Host is not controled by Astor !");
			return;
		}
		
		//	if host is not polled start polling on it
		if (selected_host.do_polling==false)
		{
			selected_host.do_polling = true;
			selected_host.updateData();
			sleep(1000);
			//return;
		}
		//	Check if host starter is faulty
		if (selected_host.state==faulty	&& selected_host.except!=null)
		{
			//	Check if host starter is running
			String	reason = selected_host.except.errors[0].reason;
			String	desc   = selected_host.except.errors[0].desc;
			if (reason.equals("TangoApi_DEVICE_NOT_EXPORTED") ||
				desc.indexOf("CORBA.TRANSIENT: Retries exceeded,")>0)
			{
				//	Ask for remote login
				if (JOptionPane.showConfirmDialog(parent,
						"Starter is not running on " + selected_host + "\n\n\n"+
						"Do you want a remote login tom start it ?",
						"Dialog",
						JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
				{
					new RemoteLoginThread(selected_host.getName(), parent).start();
				}
			}
			else
				//	Display exception
				app_util.PopupError.show(parent, 
								"Starter on " + selected_host,
								selected_host.except);
		}
		else
		if (selected_host.state==unknown)
			app_util.PopupMessage.show(parent,
				"Connection with Starter device server is blocked !");
		else
			//	Popup a host info dialog and add it in vector
			//----------------------------------------------------
			hostDialogs.add(parent, selected_host, p);
	}
	//===============================================================
	//===============================================================
	public void updateState()
	{
		repaint();
		if (hostDialogs!=null)
		{
			//	Close host dialogs if exists
			for (int i=0 ; i<hosts.length ; i++)
				if (hosts[i].state==faulty)
					hostDialogs.close(hosts[i]);
		}
	}
	//======================================================
	//======================================================
	private synchronized void sleep(int ms)
	{
		try { 
			wait(ms);
		}
		catch(InterruptedException e){}
	}













//===============================================================
/**
 *	Renderer Class
 */
//===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer
	{
		ImageIcon	tangoIcon;
		ImageIcon	pcIcon;
		ImageIcon	dbIcon;
		String		tango_host = AstorUtil.getTangoHost();
		String[]	tip_text = {
							"Control is disabled",
							"Starter is not running",
							"At least one server is stopped",
							"All servers are OK",
							"Connection failed on Starter"
							};
		
		ImageIcon[]	state_icons;
		Font[]		fonts;

		//===============================================================
		//===============================================================
		private static final int failed = all_ok+1;
		public TangoRenderer()
		{
			dbIcon = new ImageIcon(getClass().getResource(img_path + "MySql.jpg"));

			state_icons = new ImageIcon[failed+1];
			state_icons[unknown] = new ImageIcon(getClass().getResource(img_path + "unpolled.gif"));
			state_icons[faulty]  = new ImageIcon(getClass().getResource(img_path + "fault.gif"));
			state_icons[alarm]   = new ImageIcon(getClass().getResource(img_path + "warning.gif"));
			state_icons[all_ok]  = new ImageIcon(getClass().getResource(img_path + "smiley.gif"));
			state_icons[failed]  = new ImageIcon(getClass().getResource(img_path + "failed.gif"));

			pcIcon    = new ImageIcon(getClass().getResource(img_path + "pc04.gif"));
			tangoIcon = new ImageIcon(getClass().getResource(img_path + "tango_icon.jpg"));
		
			fonts = new Font[2];
			fonts[COLLECTION] = new Font("helvetica", Font.BOLD, 18);
			fonts[LEAF]       = new Font("helvetica", Font.PLAIN, 12);
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
                            boolean hasFocus)
		{
			boolean tooltip = false;

            super.getTreeCellRendererComponent(
                            tree, obj, sel,
                            expanded, leaf, row,
                            hasFocus);
/*
long	t0 = System.currentTimeMillis();
System.out.print("refreshing for " + obj + " hasFocus = " + hasFocus);
*/
 			setBackgroundNonSelectionColor(java.awt.Color.white);
			setForeground(java.awt.Color.black);
			if (row==0)
			{
				//	ROOT
 				setBackgroundSelectionColor(java.awt.Color.white);
				setIcon(tangoIcon);
				setFont(fonts[COLLECTION]);
				if (tooltip) setToolTipText(hosts.length + " hosts in control system");
			}
			else
			if (isDatabase(obj))
			{
				if (leaf)
				{
					//	Database object
					setBackgroundSelectionColor(java.awt.Color.white);
					if (dbase.state!=all_ok)
						setForeground(java.awt.Color.red);
					setIcon(dbIcon);
					setFont(fonts[LEAF]);
					if (tooltip) setToolTipText("Double click to popup jive.");
				}
				else
				{
					//	Database collection
 					setBackgroundSelectionColor(java.awt.Color.white);
					if (dbase.state==all_ok)
						setIcon(pcIcon);
					else
						setIcon(state_icons[dbase.state]);
					setFont(fonts[COLLECTION]);
					if (tooltip) setToolTipText(tango_host);
				}
				
			}
			else
			if(isHost(obj))
			{
				//	Tango Host
 				setBackgroundSelectionColor(java.awt.Color.lightGray);
				setFont(fonts[LEAF]);
 				setBackgroundNonSelectionColor(java.awt.Color.white);

				TangoHost	host = getHost(obj);
				int state = host.state;
				if (state==unknown && host.do_polling)
					state = failed;
				setIcon(state_icons[state]);
				if (tooltip) setToolTipText(tip_text[state]);
			}
			else
			{
				//	Collection
 				setBackgroundSelectionColor(java.awt.Color.lightGray);
				setFont(fonts[COLLECTION]);
				int	state = branchState(obj);
				setIcon(state_icons[state]);
				if (tooltip) setToolTipText(tip_text[state]);
			}
/*
long	t1 = System.currentTimeMillis();
System.out.println(" ---->  " + (t1-t0) + " ms");
*/
			return this;
        }

		//===============================================================
		//===============================================================
		protected int branchState(Object tree_node)
		{
			int	state = all_ok;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree_node;

			//	Get collection children
			int	nb = node.getChildCount();
			TangoHost[]	th = new TangoHost[nb];
			for (int i=0 ; i<nb ; i++)
			{
				node = node.getNextNode();
				th[i] = (TangoHost)node.getUserObject();
			}

			//	Calculate how many faulty and/or alarm
			int	nb_faulty = 0;
			int	nb_alarm  = 0;
			int	nb_unknown= 0;
			for (int i=0 ; i<nb ; i++)
			{
				if (th[i].do_polling)
				{
					//	At least one unknown -> branch is unknown
					if (th[i].state == unknown)
						return unknown;
					else
					if (th[i].state == faulty)
						nb_faulty++;
					else
					if (th[i].state == alarm)
						nb_alarm++;
				}
				else
					nb_unknown++;
			}
			//	Calculate branch state
			if (nb_unknown==nb)
				state = unknown;
			else
			if (nb_faulty>0)
				state = faulty;
			else
			if (nb_alarm>0)
				state = alarm;
			else
				state = all_ok;
			return state;
		}
		//===============================================================
		//===============================================================
		protected boolean isHost(Object tree_node)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree_node;
			Object obj = node.getUserObject();
			return (obj instanceof TangoHost);
    	}
		//===============================================================
		//===============================================================
		protected TangoHost getHost(Object tree_node)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree_node;
			Object obj = node.getUserObject();
			if (obj instanceof TangoHost)
				return (TangoHost) (obj);
			return null;
    	}
		//===============================================================
		//===============================================================
		protected boolean isDatabase(Object tree_node)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree_node;
			Object obj = node.getUserObject();
			if (obj instanceof DbaseObject)
				return true;
			else
			if (obj instanceof String)
			{
					String	str = (String)obj;
					return (str.equals(collec_names[0]));
			}
			return false;
		}
	}



	//===============================================================
	/*
	 *	A thread to subscribe State events for all hosts
	 */
	//===============================================================
	class subscribeThread extends Thread
	{
		subscribeThread()
		{
		}
		//===============================================================
		//===============================================================
		public void run()
		{
			long	t0 = System.currentTimeMillis();
			int		nb_subscribed = 0;
			for (int i=0 ; i<hosts.length ; i++)
			{
				if (hosts[i].use_events)
				{
					hosts[i].thread.subscribeChangeStateEvent();
					nb_subscribed++;
				}
			}
			
			long	t1 = System.currentTimeMillis();
			System.out.println("Total time to subscribe on " + 
							nb_subscribed + " hosts : "+ (t1-t0) + " ms");
		}
	}

}

