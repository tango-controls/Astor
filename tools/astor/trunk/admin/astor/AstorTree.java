//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011
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

import admin.astor.access.TangoAccess;
import admin.astor.statistics.ResetStatistics;
import admin.astor.tools.PopupText;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.Splash;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.IORdump;
import fr.esrf.TangoDs.Except;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.StringTokenizer;
import java.util.Vector;
import admin.astor.tools.Utils;


public class AstorTree extends JTree  implements AstorDefs
{
	private JFrame				parent;
	public  TangoHost[]			hosts;
	private TangoHost			selected_host = null;
	private	DbaseObject			selected_db = null;
	private DbaseObject[]		dbase;
	private DefaultTreeModel	treeModel;
    private javax.swing.Timer   watchDogTimer;
    private TACobject           accessControl;
    private TangoAccess         tangoAccessPanel = null;

	/**
	 *	A jive instance to be displayed.
	 */
	private jive3.MainPanel	jive3 = null;

	/**
	 *	Popup menu to be used on right button clicked.
	 */
	private TreePopupMenu	pMenu;
	private DbPopupMenu		dbMenu;
	private TacPopupMenu	tacMenu;
	/**
	 *	Class to store many HostInfoDialog
	 */
	HostInfoDialogVector	hostDialogs = null;

	private static Vector<String>			collecNames = new Vector<String>();
	private static DefaultMutableTreeNode	root;
	private static int						host_subscribed = 0;
	private static Vector<TangoHost>		hosts_using_evt;
	public  static final Color	background = new Color(0xf0, 0xf0, 0xf0);
 	//===============================================================
	//===============================================================
	public AstorTree(JFrame parent, boolean polling, Splash splash) throws DevFailed
	{
		this.parent = parent;
		this.splash = splash;
		splash.setMessage("Initializing from Database....");
		int	cnt = 0;
		splash.progress(cnt);

		//	Build panel and its tree
		initComponent();

		setBackground(background);

		//	Check hosts using or not events.
		host_subscribed = 0;
		hosts_using_evt = new Vector<TangoHost>();
		splash.setMaxProgress(hosts.length);
		for (TangoHost host : hosts)
		{
			splash.progress(++cnt);
			splash.setMessage("Creating  " + host + "  object");

			//	Fix the polling status
			host.do_polling = false;
			if (polling)
				host.setPolling(null);

			if (host.use_events)
				hosts_using_evt.add(host);

			//	And start the control thread
			host.thread = new HostStateThread(this, host);
			host.thread.start();
		}
		hostDialogs = new HostInfoDialogVector();

		//	Start a thread to subscribe events and a monitor to display
		if (host_subscribed == 0)	//	init size
			splash.setMaxProgress(hosts_using_evt.size());
		new subscribeThread().start();
		updateMonitor(null);

		//	Build menus
		if (parent instanceof Astor)
			pMenu  = new TreePopupMenu((Astor)parent, this);
		else
			pMenu  = new TreePopupMenu(this);
		dbMenu = new DbPopupMenu(this);
        if (isAccessControlled())
           tacMenu = new TacPopupMenu(this);

		//	Expend for database
		expandRow(1);

        //	Start a timer to have a watch dog on host thread
        int delay = 10000; //   2*timeout on first connection in milliseconds
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //watchDog(evt);
            }
        };
        watchDogTimer = new javax.swing.Timer(delay, taskPerformer);
        watchDogTimer.start();
	}
	//===============================================================
	//===============================================================
	private Splash	splash;
	private Vector<String>	subscrib_error = new Vector<String>();
	PopupText subscribeErrWindow = null;
	void updateMonitor(String strerror)
	{
		//	Concat. error messages at startup
		if (strerror!=null)
			subscrib_error.add(strerror);

		//	Check if startup is terminated
		if (host_subscribed < hosts_using_evt.size())
		{
			//	NOT TERMINATED
			//	Update the monitor window
			TangoHost	host = hosts_using_evt.get(host_subscribed);
			String		message = "Subscribing for  " + host.getName() + "  (" +
					(host_subscribed+1) + "/" + hosts_using_evt.size() + ")";

			//	Display only if main window displayed to be sure to be on top
			if (Astor.displayed)
				splash.setVisible(true);

			splash.progress(++host_subscribed);
			splash.setMessage(message);
		}
		else
		{
			//	All host subscribed or failed
			splash.setVisible(false);
			if (subscrib_error.size()>0)
			{
				StringBuffer	sb = new StringBuffer();
				for (int i=0 ; i<subscrib_error.size() ; i++)
				{
					sb.append(subscrib_error.get(i));
					if (i < subscrib_error.size()-1)
						sb.append("\n\n");
				}
				String title = "Subcribe events on " + subscrib_error.size() + "/" + 
						host_subscribed;
				if (subscrib_error.size()==1)
					title += " host has failed";
				else
					title += " hosts have failed";
				int	height = 50 + subscrib_error.size()*75;
				if (height>560)
					height = 560;

				subscribeErrWindow = new PopupText(parent, true);
				subscribeErrWindow.show(title,
					new String[] { sb.toString() }, 
					900, height);
			}				
		}
	}
	//===============================================================
	//===============================================================
	void stopThreads()
	{
        watchDogTimer.stop();
		for (TangoHost host : hosts)
			host.stopThread();
	}
	//===============================================================
	//===============================================================
	void expand(boolean expand)
	{
		if (expand)
			//	Expand
			for (int i=0 ; i<(hosts.length + collecNames.size()+1) ; i++)
				expandRow(i);
		else
			//	Collapse
			for (int i=1 ; i<=collecNames.size(); i++)
				collapseRow(i);
	}
	//===============================================================
	//===============================================================
    public boolean isAccessControlled()
    {
        return accessControl != null;
    }
	//===============================================================
	//===============================================================
	private void initComponent() throws DevFailed
	{
	
		//Create the nodes.
		root = new DefaultMutableTreeNode("TANGO Control System");

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
		//	Build Database objects
		String		tango_hosts = AstorUtil.getTangoHost();
		if (tango_hosts==null ||
			tango_hosts.length()==0)
			Except.throw_connection_failed("TangoApi_TANGO_HOST_NOT_SET",
											"TANGO_HOST is not set",
											"AstorTree.initTangoObjects()");

		StringTokenizer stk;
		if (tango_hosts!=null && tango_hosts.indexOf(",")>0)
			stk = new StringTokenizer(tango_hosts, ",");
		else
			stk = new StringTokenizer(tango_hosts);

		Vector<String>	vector = new Vector<String>();
		while(stk.hasMoreTokens())
			vector.add(stk.nextToken());
		dbase = new DbaseObject[vector.size()];
		for (int i=0 ; i<vector.size() ; i++)
			dbase[i] = new DbaseObject(this, vector.get(i));

		//	Build Host objects
		AstorUtil	au = AstorUtil.getInstance();
		hosts = au.getTangoHostList();
		collecNames = au.getCollectionList(hosts);

        String accessControlDeviceName = AstorUtil.getAccessControlDeviceName();
        if (accessControlDeviceName!=null)
            accessControl = new TACobject(this, accessControlDeviceName);
	}
	//===============================================================
	//===============================================================
	private void createNodes(DefaultMutableTreeNode root)
	{

		int	cnt = 0;
		splash.progress(cnt);
		Vector<DefaultMutableTreeNode>	collections = new Vector<DefaultMutableTreeNode>();
		for (String collec_name : collecNames) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(collec_name);
			collections.add(node);
			root.add(node);
		}
		//	First create database node
		for (DbaseObject db : dbase) {
			collections.get(0).add(new DefaultMutableTreeNode(db));
        }

        //  Add TAC if any
        if (accessControl!=null) {
            collections.get(0).add(new DefaultMutableTreeNode(accessControl));
        }

        //	Add Host nodes
		splash.setMaxProgress(hosts.length);
		for (TangoHost host : hosts) {
			splash.progress(++cnt);
			splash.setMessage("Creating  " + host + "  node");
			DefaultMutableTreeNode host_node =
					new DefaultMutableTreeNode(host);
			host.state = unknown;
			int idx = getHostCollection(host);
			collections.get(idx).add(host_node);
		}
    }
 	//===============================================================
	//===============================================================
	Vector<String> getCollectionList()
	 {
		 return collecNames;
	 }
	//===============================================================
	//===============================================================
	private int getHostCollection(TangoHost host)
	{
		for (int i=0 ; i<collecNames.size() ; i++)
			if (host.collection==null)
				return collecNames.size()-1;	//	The last one
			else
				if (host.collection.equals(collecNames.get(i)))
					return i;
		return collecNames.size()-1;	//	The last one.
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

		boolean	is_db_collec =
				((DefaultMutableTreeNode)node.getChildAt(0)).getUserObject()instanceof DbaseObject;
		//	do not collapse if Root or database node
		if (path.getPathCount()==1 || is_db_collec)
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
				for (TangoHost host : hosts)
					if (host.use_events)
						nb_on_events++;
				if (nb_on_events==hosts.length)
					message += "All are controled on events.";
				else
				if (nb_on_events>0)
					message += nb_on_events + " are controled on events.";
				Utils.popupMessage(parent, message, img_path + "tango_icon.jpg");
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
     * @param selPath selected tree path
     * @return the rectangle to get inputs
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
	Object getSelectedObject()
	{
		DefaultMutableTreeNode node =
			(DefaultMutableTreeNode)getLastSelectedPathComponent();
		return node.getUserObject();
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
				ErrorPane.showErrorMessage(parent, null, e);
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
		collecNames.add(name);
	}
 	//===============================================================
	//===============================================================
	void moveNode()
	{
		//	Get selected host
		DefaultMutableTreeNode	hostNode =
			(DefaultMutableTreeNode) getLastSelectedPathComponent();
		DefaultMutableTreeNode	parentNode =
			(DefaultMutableTreeNode) hostNode.getParent();

		//	Get collection without Database and its own collection
		String[]	targetNames = new String[collecNames.size()-1];
		int	idx = 0;
		for (int i=1 ; i<collecNames.size() ; i++) {
			if (! collecNames.get(i).equals(parentNode.toString()))
				targetNames[idx++] = collecNames.get(i);
		}
		targetNames[idx] = "New Branch";

		//	And choose new collection
		String		targetName =
				 (String) JOptionPane.showInputDialog(this,
								"Move " + hostNode + "  to :", "",
								JOptionPane.INFORMATION_MESSAGE, null,
								targetNames, targetNames[0]);
		if (targetName != null) {

			//	Is it a new one ?
			if (targetName.equals(targetNames[idx])) {	//	"New Branch"

				//	Get the new branch name and add it
				targetName =
					(String)JOptionPane.showInputDialog(this,
								"New Branch Name",
								"Input Dialog",
								JOptionPane.INFORMATION_MESSAGE,
								null, null, "");
				if (targetName==null)
					return;

				addBranch(targetName);
			}

			//	Get target collection node
			for (int i=0 ; i<root.getChildCount() ; i++) {

				DefaultMutableTreeNode	collecNode =
					(DefaultMutableTreeNode) root.getChildAt(i);
				if (collecNode.toString().equals(targetName)) {

					//	Change the Starter property
					try {
						TangoHost	host = (TangoHost)hostNode.getUserObject();
						host.setCollection(targetName);
					}
					catch(DevFailed e) {
						ErrorPane.showErrorMessage(parent, null, e);
						return;
					}

					//	Get the default model used to move node
					DefaultTreeModel	model = (DefaultTreeModel)getModel();
					model.removeNodeFromParent(hostNode);
					model.insertNodeInto(hostNode, collecNode, 0);

					//	Check if previous collection has still children
					if (parentNode.getChildCount()==0) {
						model.removeNodeFromParent(parentNode);
						collecNames.remove(parentNode.toString());
					}

					//	Ensure that the new node is visible and select it
					scrollPathToVisible(new TreePath(hostNode.getPath()));

					TreeNode[]	path = hostNode.getPath();
					setSelectionPath(new TreePath(path));
				}
			}
		}
	}
	//===============================================================
	//===============================================================
	@SuppressWarnings({"UnusedDeclaration"})
    public void hostSelectionPerformed(TreeSelectionEvent e)
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                   getLastSelectedPathComponent();

		if (node == null) return;

		Object obj = node.getUserObject();
		if (node.isLeaf()) {
			if (obj instanceof TangoHost) {
				selected_host = (TangoHost)obj;
				selected_db  = null;
			}
			else
			if (obj instanceof DbaseObject) {
				selected_host = null;
				selected_db  = (DbaseObject)obj;
			}
			else {
				selected_host = null;
				selected_db  = null;
			}
		}
		else {
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
     * @param evt the mouse event
     */
	//======================================================
	private void treeMouseClicked (java.awt.event.MouseEvent evt)
	{
		//	Check if click is on a node
		if (getRowForLocation(evt.getX(), evt.getY())<1)
			return;

		//	Set selection at mouse position
		TreePath	selectedPath =
			getPathForLocation(evt.getX(), evt.getY());

		if (selectedPath==null)
			return;
		setSelectionPath(selectedPath);

		//	Do something only if double click
		int mask = evt.getModifiers();
		if(evt.getClickCount() == 2) {
			//	Check if btn1
			if ((mask & InputEvent.BUTTON1_MASK)!=0) {
				if (selected_host!=null) {
					//	Display dialog
					displayHostInfo();
				}
				else {
					DefaultMutableTreeNode	node =
						(DefaultMutableTreeNode) getLastSelectedPathComponent();
					Object o = node.getUserObject();
					if (selected_db!=null ||
						o.toString().equals(collecNames.get(0)))
						displayJiveAppli();
				}
			}
		}
		else {
			DefaultMutableTreeNode	node =
				(DefaultMutableTreeNode) getLastSelectedPathComponent();
			Object obj = node.getUserObject();
			//	Check if btn3
			if ((mask & InputEvent.BUTTON3_MASK)!=0) {
				if (obj instanceof DbaseObject)
					dbMenu.showMenu(evt);
				else
				if (obj instanceof TACobject)
					tacMenu.showMenu(evt);
				else
					pMenu.showMenu(evt);
            }
		}
	}
	//======================================================
	//======================================================
	public void setSelectionPath(String hostname)
	{
		DefaultMutableTreeNode	collec;
		DefaultMutableTreeNode	node;
		for (int i=0 ; i<root.getChildCount() ; i++)
		{
			collec = (DefaultMutableTreeNode)root.getChildAt(i);
			for (int j=0 ; j<collec.getChildCount() ; j++)
			{
				node = (DefaultMutableTreeNode) collec.getChildAt(j);
				Object	obj = node.getUserObject();
				if (obj instanceof TangoHost)
				{
					String		name = node.toString();
					int			idx = name.indexOf('(');
					if (idx>0)	//	remove description
						name = name.substring(0, idx).trim();
					if (name.equals(hostname))
						setSelectionPath(new TreePath(node.getPath()));
				}
			}
		}
	}
	//======================================================
	//======================================================
	private boolean	jive_is_read_only  = false;
	void displayJiveAppli()
	{
		if (selected_db!=null && selected_db.state==faulty)
		{
			ErrorPane.showErrorMessage(parent, null, selected_db.except);
		}
		else
		{
			boolean	from_shell = false;
			boolean	read_only  = AstorUtil.getInstance().jiveIsReadOnly();

			//	Check if it has changed
			//	or not already Started
			if (jive_is_read_only!=read_only || jive3==null)
				jive3 = new jive3.MainPanel(from_shell, read_only);
			jive3.setVisible(true);
			jive3.toFront();
			jive_is_read_only = read_only;
		}
	}
	//===============================================================
	/**
	 *	Replace an old leaf by a new one containing the new object.
	 *	This method is mainly used to re size node (when usage changed)
     * @param h the new tango host object
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
	public void displayHostInfoDialog(String hostname)
    {
        //	Take off IP address if exists
        StringTokenizer st = new StringTokenizer(hostname);
        hostname = st.nextToken();
        //	Take off name extention (e.g. .esrf.fr) if exists
        st = new StringTokenizer(hostname, ".");
        hostname = st.nextToken();

        //	Select Host on main Tree and Popup host panel
        parent.setVisible(true);
 		setSelectionPath(new TreePath(root.getPath()));	// remove previous selection
  		setSelectionPath(hostname);
        displayHostInfo();
    }
    //======================================================
    //======================================================
	public void displayHostInfo(String devname)
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
            parent.setVisible(true);
 			setSelectionPath(new TreePath(root.getPath()));	// remove previous selection
 			setSelectionPath(hostname);
            displayHostInfo();
            String  servname = new DeviceProxy(devname).adm_name();
            servname = servname.substring(servname.indexOf('/')+1);
			HostInfoDialog	dlg = hostDialogs.getByHostName(selected_host);
			if (dlg!=null)
				dlg.setSelection(servname);
        }
        catch (DevFailed e)
        {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }
    //======================================================
    //======================================================
    public void displayHostInfo()
    {
		//	Check if a host is selected
		if (selected_host==null)
		{
			Utils.popupError(this, 
					"this Host is not controlled by Astor !");
			return;
		}
		
		//	if host is not polled start polling on it
		if (!selected_host.do_polling)
		{
			selected_host.do_polling = true;
			selected_host.updateData();
			try { Thread.sleep(1000); } catch (InterruptedException e) { /* */ }
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
				if (AstorUtil.osIsUnix())
				{
					if (JOptionPane.showConfirmDialog(parent,
							"Starter is not running on " + selected_host + "\n\n\n"+
							"Do you want a remote login to start it ?",
							"Dialog",
							JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
					{
						new RemoteLoginThread(selected_host.getName(), parent).start();
					}
				}
				else
					Utils.popupError(parent,
							"Starter is not running on " +
							selected_host + " !!!");
			}
			else
				//	Display exception
				Utils.popupError(parent,
								"Starter on " + selected_host,
								selected_host.except);
		}
		else
		if (selected_host.state==unknown)
			Utils.popupMessage(parent,
				"Connection with Starter device server is blocked !");
		else
			//	Popup a host info dialog and add it in vector
			if  (parent instanceof Astor)
				hostDialogs.add((Astor) parent, selected_host);
	}
	//===============================================================
	//===============================================================
	void resetCollectionStatistics()
	{
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                   getLastSelectedPathComponent();
        Vector<String>  hostList = new Vector<String>();
        int nb = node.getChildCount();
        for (int i=0 ; i<nb ; i++) {
            node = node.getNextNode();
            TangoHost	host = (TangoHost)node.getUserObject();
            hostList.add(host.name());
        }

        if (JOptionPane.showConfirmDialog(parent,
                "Do you want to reset statistics on " +
                        hostList.size() + " hosts  ?",
                "Dialog",
                JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION) {

            new ResetStatistics(hostList);
        }

    }
	//===============================================================
	//===============================================================
	void displayBranchInfo()
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                   getLastSelectedPathComponent();

		String	str = node + ":\n\n";
		//	Get collection children
		int	nb = node.getChildCount();
		for (int i=0 ; i<nb ; i++)
		{
			node = node.getNextNode();
			TangoHost	host = (TangoHost)node.getUserObject();
			str += host.hostStatus();
		}
		Utils.popupMessage(parent, str);
	}
	//===============================================================
	//===============================================================
    public void startTACpanel()
    {
        try  {
            if (tangoAccessPanel==null || !tangoAccessPanel.isVisible()) {
                if (TangoAccess.checkPassword(parent)==JOptionPane.OK_OPTION) {
                    tangoAccessPanel = new TangoAccess(parent);
                }
                else
                    return;
            }
            tangoAccessPanel.setVisible(true);
        }
        catch(DevFailed e)  {
            ErrorPane.showErrorMessage(this,
        				"Cannot start TangoAccess class", e);
        }
    }
	//===============================================================
	//===============================================================
	public void updateState()
	{
		repaint();
		if (hostDialogs!=null)
		{
			//	Close host dialogs if exists
			for (TangoHost host : hosts)
				if (host.state == faulty)
					hostDialogs.close(host);
		}
	}
	//======================================================
	//======================================================










//===============================================================
/**
 *	Renderer Class
 */
//===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer
	{
		private ImageIcon	tangoIcon;
		private ImageIcon	dbIcon;
		private Font[]		fonts;

		//===============================================================
		//===============================================================
		public TangoRenderer()
		{
			dbIcon = new ImageIcon(getClass().getResource(img_path + "MySql.gif"));
			//tangoIcon = new ImageIcon(getClass().getResource(img_path + "tango_icon.jpg"));
			tangoIcon = new ImageIcon(getClass().getResource(img_path + "TangoSmall.gif"));

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

            super.getTreeCellRendererComponent(
                            tree, obj, sel,
                            expanded, leaf, row,
                            hasFocus);

 			setBackgroundNonSelectionColor(background);
			setForeground(java.awt.Color.black);
			if (row==0) {
				//	ROOT
 				setBackgroundSelectionColor(background);
				setIcon(tangoIcon);
				setFont(fonts[COLLECTION]);
			}
			else
			if (isDatabase(obj)) {
				if (leaf) 	{
					//	Database object
					setBackgroundSelectionColor(java.awt.Color.lightGray);
					DbaseObject	db = getDbase(obj);
					setIcon(AstorUtil.state_icons[db.state]);
					setFont(fonts[LEAF]);
				}
				else {
					//	Database collection
 					setBackgroundSelectionColor(background);
					int	state = all_ok;
					for (DbaseObject db : dbase)
						if (db.state == faulty)
							state = faulty;
					if (state==faulty)
						setForeground(java.awt.Color.red);

					setIcon(dbIcon);
					setFont(fonts[COLLECTION]);
				}
				
			}
			else
			if(isTAC(obj)) {
                //	TAC object
                setBackgroundSelectionColor(java.awt.Color.lightGray);
                TACobject	tac = getTACobject(obj);
                setIcon(AstorUtil.state_icons[tac.state]);
                setFont(fonts[LEAF]);

            }
			else
			if(isHost(obj)) {
				//	Tango Host
 				setBackgroundSelectionColor(java.awt.Color.lightGray);
				setFont(fonts[LEAF]);
 				setBackgroundNonSelectionColor(background);

				TangoHost	host = getHost(obj);
				int state = host.state;
				if (state==unknown && host.do_polling)
					state = failed;
				setIcon(AstorUtil.state_icons[state]);
			}
			else {
				//	Collection
 				setBackgroundSelectionColor(java.awt.Color.lightGray);
				setFont(fonts[COLLECTION]);
				int	state = branchState(obj);
				setIcon(AstorUtil.state_icons[state]);
			}
			return this;
        }

		//===============================================================
		//===============================================================
		protected int branchState(Object tree_node)
		{
			int	state;
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
			boolean is_faulty  = false;
			boolean is_alarm   = false;
			boolean is_unknown = false;
            boolean is_moving  = false;
            for (int i=0 ; i<nb ; i++)
			{
				if (th[i].do_polling)
				{
					//	At least one unknown -> branch is unknown
					if (th[i].state == unknown)
						return unknown;
					else
					if (th[i].state == faulty)
						is_faulty = true;
					else
					if (th[i].state == alarm)
						is_alarm = true;
					else
					if (th[i].state == moving)
						is_moving = true;
				}
				else
					is_unknown = true;
			}
			//	Calculate branch state
			if (is_unknown)
				state = unknown;
			else
			if (is_faulty)
				state = faulty;
			else
			if (is_moving)
				state = moving;
			else
			if (is_alarm)
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
		protected DbaseObject getDbase(Object tree_node)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree_node;
			Object obj = node.getUserObject();
			if (obj instanceof DbaseObject)
				return (DbaseObject) (obj);
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
			if (obj instanceof String) {
					String	str = (String)obj;
					return (str.equals(collecNames.get(0)));
			}
			return false;
		}
        //===============================================================
        //===============================================================
        protected TACobject getTACobject(Object tree_node)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree_node;
            Object obj = node.getUserObject();
            if (obj instanceof TACobject)
                return (TACobject) (obj);
            return null;
        }
		//===============================================================
		//===============================================================
		protected boolean isTAC(Object tree_node)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree_node;
			Object obj = node.getUserObject();
			return (obj instanceof TACobject);
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
			for (TangoHost host : hosts)
			{
				if (host.use_events)
				{
					host.thread.subscribeChangeStateEvent();
					nb_subscribed++;
				}
			}
			
			long	t1 = System.currentTimeMillis();
			System.out.println("Total time to subscribe on " + 
							nb_subscribed + " hosts : "+ (t1-t0) + " ms");
			System.out.println("Total time to start Astor " + (t1-Astor.t0) + " ms");
		}
	}


	

	//===============================================================
	/*
	 *	A Database Popup menu
	 */
	//===============================================================
	class DbPopupMenu extends JPopupMenu
	{
		private JTree	tree;
		private	 final String[]	menuLabels = { 
					"Server Info",
					"Database Info",
					"Database Black Box",
					"Browse Database (Jive)",
				};
		private final int	OFFSET = 2;		//	Label And separator
		private final int	SERVER_INFO       = 0;
		private final int	DATABASE_INFO     = 1;
		private final int	DATABASE_BLACKBOX = 2;
		private final int	BROWSE_DATABASE   = 3;
		//===========================================================
		DbPopupMenu(JTree tree)
		{
			this.tree = tree;
			
			//	Build menu
			JLabel	title = new JLabel("Datbase Server :");
        	title.setFont(new java.awt.Font("Dialog", 1, 16));
			add(title);
			add(new JPopupMenu.Separator());
			for (String menuLabel : menuLabels)
			{
				JMenuItem btn = new JMenuItem(menuLabel);
				btn.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						treeActionPerformed(evt);
					}
				});
				add(btn);
			}
		}
		//===========================================================		}
		public void showMenu(java.awt.event.MouseEvent evt)
		{

			Object obj = getSelectedObject();

			//	Add host name in menu label title
			JLabel	lbl = (JLabel)getComponent(0);
			lbl.setText(obj.toString() + "  :");
			show(tree, evt.getX(), evt.getY());
		}
		//===========================================================
		private void treeActionPerformed(ActionEvent evt)
		{
			Object	src = evt.getSource();
        	int     cmdidx = 0;
        	for (int i=0 ; i<menuLabels.length ; i++)
            	if (getComponent(OFFSET+i)==src)
                	cmdidx = i;

			try
			{
				switch(cmdidx)
				{
				case BROWSE_DATABASE:
					displayJiveAppli();
					break;
				default:
					manageOneDbaseOption(cmdidx);
				}
   			}
			catch (DevFailed e)
			{
		        ErrorPane.showErrorMessage(this, null, e);
			}
		}
		//===========================================================
		private void manageOneDbaseOption(int action) throws DevFailed
		{
			//	Create a Popup text window
			DbaseObject	db = (DbaseObject)getSelectedObject();
			PopupText	ppt = new PopupText(parent, true);
			//ppt.setFont(new Font("helvetica", Font.BOLD, 14));
			switch(action)
			{
			case SERVER_INFO:
				ppt.show(db.getServerInfo());
				break;
			case DATABASE_INFO:
				ppt.show(db.getInfo());
				break;
			case DATABASE_BLACKBOX:
				db.blackbox(parent);
				break;
			}
		}
	}



	//===============================================================
	/*
	 *	A TAC Popup menu
	 */
	//===============================================================
	class TacPopupMenu extends JPopupMenu
	{
		private JTree	tree;
		private	 final String[]	menuLabels = {
					"Server Info",
					"Black  Box",
					"Manager Panel",
				};
		private final int	OFFSET = 2;		//	Label And separator
		private final int	SERVER_INFO   = 0;
		private final int	BLACKBOX      = 1;
		private final int	MANAGER_PANEL = 2;
		//===========================================================
		TacPopupMenu(JTree tree)
		{
			this.tree = tree;

			//	Build menu
			JLabel	title = new JLabel("Access Control Server :");
        	title.setFont(new java.awt.Font("Dialog", 1, 16));
			add(title);
			add(new JPopupMenu.Separator());
			for (String menuLabel : menuLabels) {
				JMenuItem btn = new JMenuItem(menuLabel);
				btn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						treeActionPerformed(evt);
					}
				});
				add(btn);
			}
		}
		//===========================================================		}
		public void showMenu(java.awt.event.MouseEvent evt)
		{

			Object obj = getSelectedObject();

			//	Add host name in menu label title
			JLabel	lbl = (JLabel)getComponent(0);
			lbl.setText(obj.toString() + "  :");
			show(tree, evt.getX(), evt.getY());
		}
		//===========================================================
		private void treeActionPerformed(ActionEvent evt)
		{
			Object	src = evt.getSource();
        	int     cmdidx = 0;
        	for (int i=0 ; i<menuLabels.length ; i++)
            	if (getComponent(OFFSET+i)==src)
                	cmdidx = i;

			//	Create a Popup text window
			TACobject	tac = (TACobject) getSelectedObject();
			PopupText	ppt = new PopupText(parent, true);
			try {
                switch(cmdidx)
                {
                case SERVER_INFO:
                    ppt.show(tac.getServerInfo());
                    break;
                case BLACKBOX:
                    tac.blackbox(parent);
                    break;
                case MANAGER_PANEL:
                    startTACpanel();
                    break;
                }
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
            }
		}
	}


}

