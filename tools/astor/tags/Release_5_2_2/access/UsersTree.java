//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:	java source code for display JTree
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 1.3  2008/11/19 10:01:34  pascal_verdier
// New tests done on Access control.
// Allowed commands tree added.
//
// Revision 1.2  2006/10/02 14:09:02  pascal_verdier
// Minor changes.
//
// Revision 1.1  2006/09/19 13:06:47  pascal_verdier
// Access control manager added.
//
//
// copyleft 2006 by European Synchrotron Radiation Facility, Grenoble, France
//							 All Rights Reversed
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
import java.util.StringTokenizer;
import java.util.Vector;


public class  UsersTree  extends JTree implements TangoConst
{
	static final int			USER_NODE  = -2;
	static final int			COLLECTION = -1;
	static final int			ADDRESS    = 0;
	static final int			DEVICE     = 1;
	static final String[]		collecStr = { "Allowed Addresses", "Devices" };


	static final int			WRITE   = 0;
	static final int			READ    = 1;
	static final String[]		rightsStr = { "write", "read" };

	static ImageIcon	tango_icon;
	static ImageIcon	all_users_icon;
	static ImageIcon	user_icon;
	static ImageIcon	add_icon;
	static ImageIcon	dev_icon;
	static ImageIcon	write_icon;
	static ImageIcon	read_icon;



	private DefaultTreeModel	    treeModel;
	private DefaultMutableTreeNode  root;
	private UsersTreePopupMenu	    menu;
	private JFrame 	                parent;

	private AccessProxy	access_dev;
	CopiedAddresses copied_addresses = new CopiedAddresses();
	CopiedDevices   copied_devices   = new CopiedDevices();
	private static final Color	background = admin.astor.AstorTree.background;
	//===============================================================
	//===============================================================
	public UsersTree(JFrame parent, AccessProxy access_dev) throws DevFailed
	{
		super();
		this.parent     = parent;
		this.access_dev = access_dev;
		setBackground(background);
		buildTree();
		menu = new UsersTreePopupMenu(this);
	 }
	//===============================================================
	//===============================================================
	private void buildTree() throws DevFailed
	{
		String  str_root = "Tango Control Access";
		try
		{
				str_root = "Access to  " +
						ApiUtil.get_db_obj().get_tango_host();
		}
		catch(DevFailed e) { /** Nothing to do */}

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
		addTreeExpansionListener(new TreeExpansionListener () {
			public void treeCollapsed(TreeExpansionEvent e) {
				//collapsedPerfomed(e);
			}
			public void treeExpanded(TreeExpansionEvent e) {
				expandedPerfomed(e);
			}
		});
		//	Add Action listener
		addMouseListener (new java.awt.event.MouseAdapter () {
			public void mouseClicked (java.awt.event.MouseEvent evt) {
				treeMouseClicked (evt);
			}
		});
	}
	//======================================================
	/**
	 *	Manage event on clicked mouse on JTree object.
	 */
	//======================================================
	private void treeMouseClicked (java.awt.event.MouseEvent evt)
	{
		if (access_dev.getAccessControl()==TangoConst.ACCESS_READ)
			return;

		//	Set selection at mouse position
		TreePath	selectedPath = getPathForLocation(evt.getX(), evt.getY());
		if (selectedPath==null) return;

		DefaultMutableTreeNode	node =
		(DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount()-1);
		Object	o = node.getUserObject();
		int mask = evt.getModifiers();

		//  Check button clicked
		if(evt.getClickCount()==2 && (mask & MouseEvent.BUTTON1_MASK)!=0)
		{
			if (o instanceof AccessAddress ||
				o instanceof AccessDevice)
			editItem();
		}
		else
		if ((mask & MouseEvent.BUTTON3_MASK)!=0)
		{
			if (node == root)
				 menu.showMenu(evt, (String)o);
			else
			 if (o instanceof AccessAddress)
				menu.showMenu(evt, ADDRESS, o);
			else
			if (o instanceof AccessDevice)
				menu.showMenu(evt, DEVICE, o);
			else
			if (o instanceof AccessUser)
				menu.showMenu(evt, USER_NODE, o);
			else
			if (o instanceof String)
				menu.showMenu(evt, COLLECTION, o);
		}
	}
	//===============================================================
	//===============================================================
	public void expandedPerfomed(TreeExpansionEvent evt)
	{
		if (!manage_expand)
			return;
		//	Get path
		TreePath	tp = evt.getPath();
		Object[]    path = tp.getPath();
		if (path.length<2)
			return;

		//	Get concerned node
		DefaultMutableTreeNode	node =
			(DefaultMutableTreeNode)tp.getPathComponent(path.length-1);

		switch (path.length)
		{
		case 2:
			expendUserNode(node.toString());
			break;

		case 3:
			if (node.toString().equals(collecStr[ADDRESS]))
				createAddressNodes(node);
			else
				createDeviceNodes(node);
			 break;
	   }
	}
	//===============================================================
	//===============================================================
	private void createDeviceNodes(DefaultMutableTreeNode node)
	{
		try
		{
			DefaultMutableTreeNode	d_node;
			Object		obj =
				((DefaultMutableTreeNode)node.getParent()).getUserObject();
			String		user = ((AccessUser)obj).getName();
			AccessDevice[]	devices = getDevices(user);
			for (int i=0 ; i<devices.length ; i++)
			{
				//	Create a node for devices
				d_node = new DefaultMutableTreeNode(devices[i]);
				treeModel.insertNodeInto(d_node, node, i);
			}
			removePreviousNode(node, devices.length);
		}
		catch (DevFailed e)
		{
			removePreviousNode(node, 0);
			ErrorPane.showErrorMessage(parent,
				"Cannot read devices", e);
		}
	}
	 //===============================================================
	//===============================================================
	private void createAddressNodes(DefaultMutableTreeNode node)
	{
		try
		{
			DefaultMutableTreeNode	a_node;
			Object		obj =
				((DefaultMutableTreeNode)node.getParent()).getUserObject();
			String		user = ((AccessUser)obj).getName();
			AccessAddress[] addresses = getAddresses(user);

			//  Check if something has changed.
			if (!createChildNodes(node, addresses))
				return;

			for (int i=0 ; i<addresses.length ; i++)
			{
				//	Create a node for addresses
				a_node = new DefaultMutableTreeNode(addresses[i]);
				treeModel.insertNodeInto(a_node, node, i);
			}
			removePreviousNode(node, addresses.length);
		}
		catch (DevFailed e)
		{
			removePreviousNode(node, 0);
			ErrorPane.showErrorMessage(parent,
				"Cannot read addresses", e);
		}
	}
	//===============================================================
	//===============================================================
	private void createUserNode(String name)
	{
		DefaultMutableTreeNode	u_node;
		DefaultMutableTreeNode	a_node;
		DefaultMutableTreeNode	d_node;

		u_node =  new DefaultMutableTreeNode(new AccessUser(name));
		a_node = new DefaultMutableTreeNode(collecStr[ADDRESS]);
		d_node = new DefaultMutableTreeNode(collecStr[DEVICE]);
		a_node.add(new DefaultMutableTreeNode(new Dummy()));
		d_node.add(new DefaultMutableTreeNode(new Dummy()));
		u_node.add(a_node);
		u_node.add(d_node);
		root.add(u_node);
	}
	//===============================================================
	//===============================================================
	private void createUserNodes() throws DevFailed
	{
		String[]	users = access_dev.getUsers();
		int	ratio = 80/users.length;
		for (String user : users)
		{
			AstorUtil.increaseSplashProgress(
					ratio, "building tree for " + user);
			createUserNode(user);
		}
	}
	//===============================================================
	//===============================================================
	private boolean createChildNodes(DefaultMutableTreeNode node, AccessAddress[] address)
	{
		boolean create = false;
		if (node.getChildCount() != address.length)
			create = true;
		else
		for (int i=0 ; i<address.length ; i++)
			if (!node.getChildAt(i).toString().equals(address[i].toString()))
				create = true;
		return create;
	}
	//===============================================================
	//===============================================================
	private void removePreviousNode(DefaultMutableTreeNode node, int offset)
	{
		while (node.getChildCount()>offset)
		{
			DefaultMutableTreeNode	leaf =
					(DefaultMutableTreeNode)node.getChildAt(offset);
			treeModel.removeNodeFromParent(leaf);
		}
	}
	//===============================================================
	//===============================================================
	private AccessDevice[] getDevices(String user) throws DevFailed
	{
		String[]    result = access_dev.getDevicesByUser(user);

		AccessDevice[]	ret = new AccessDevice[result.length/2];
		for (int i=0 ; i<result.length/2 ; i++)
			ret[i] = new AccessDevice(result[2*i], result[2*i+1]);
		return ret;
	}
	//===============================================================
	//===============================================================
	private AccessAddress[] getAddresses(String user) throws DevFailed
	{
		String[]    result = access_dev.getAddressesByUser(user);

		AccessAddress[]	ret = new AccessAddress[result.length];
		for (int i=0 ; i<result.length ; i++)
			ret[i] = new AccessAddress(result[i]);
		return ret;
	}
	//======================================================
	//======================================================
	DefaultMutableTreeNode getSelectedNode()
	{
		return (DefaultMutableTreeNode) getLastSelectedPathComponent();
	}

	//======================================================
	//======================================================
	Object getSelectedObject()
	{
		DefaultMutableTreeNode	node = getSelectedNode();
		if (node==null)
			return null;
		return node.getUserObject();
	}


//===============================================================
//
//	Editing Tree (Add, remove...)
//
//===============================================================
	//===============================================================
	//===============================================================
	void addUser()
	{
		EditDialog  dlg = new EditDialog(parent);
		if (dlg.showDialog()!=JOptionPane.OK_OPTION)
			return;
		String[]	str = dlg.getInputs();
		String  	user    = str[EditDialog.USER];
		String  	address = str[EditDialog.ADDRESS];
		String  	devname = "*/*/*";

		//  Check if user already exists
		TreePath    tp = null;
		if (userExists(user))
		{
			expendUserNode(user);
			app_util.PopupError.show(parent,
				"User  "+ user + "  Already exists !");
		}
		else
		{
			 //  If does not already exist, create it
		   try
			{
				access_dev.addAddress(user, address);
				access_dev.addDevice(user, devname, rightsStr[WRITE]);
			}
			catch(DevFailed e)
			{
					ErrorPane.showErrorMessage(parent,
						"Error during Database access", e);
					return;
			}

			TreeNode[]  path = new TreeNode[4];

			DefaultMutableTreeNode  new_user_node =
					  new DefaultMutableTreeNode(new AccessUser(user));
			DefaultMutableTreeNode  new_str_add_node =
					  new DefaultMutableTreeNode(collecStr[ADDRESS]);
			DefaultMutableTreeNode  new_str_dev_node =
					  new DefaultMutableTreeNode(collecStr[DEVICE]);
			DefaultMutableTreeNode  new_add_node =
					  new DefaultMutableTreeNode(new AccessAddress(address));
			DefaultMutableTreeNode  new_dev_node =
					  new DefaultMutableTreeNode(new AccessDevice(devname, WRITE));

			treeModel.insertNodeInto(new_user_node, root, root.getChildCount());
			treeModel.insertNodeInto(new_str_add_node, new_user_node, 0);
			treeModel.insertNodeInto(new_str_dev_node, new_user_node, 1);
			treeModel.insertNodeInto(new_add_node, new_str_add_node, 0);
			treeModel.insertNodeInto(new_dev_node, new_str_dev_node, 0);

			path[0] = root;
			path[1] = new_user_node;
			path[2] = new_str_add_node;
			path[3] = new_add_node;
			tp = new TreePath(path);
			setSelectionPath(tp);

			path[2] = new_str_dev_node;
			path[3] = new_dev_node;
			tp = new TreePath(path);
			setSelectionPath(tp);
		}
		scrollPathToVisible(tp);
	}
	//===============================================================
	//===============================================================
	private boolean manage_expand =true;
	void addItem()
	{
		DefaultMutableTreeNode	node = getSelectedNode();
		Object		o = node.getUserObject();
		int			obj_type;
		if (o.toString().equals(collecStr[ADDRESS]))
			obj_type = ADDRESS;
		else
		if (o.toString().equals(collecStr[DEVICE]))
			obj_type = DEVICE;
		else
			return;

		DefaultMutableTreeNode      new_node;
		TreeNode[]    path = new DefaultMutableTreeNode[4];
		TreePath      tp;
		path[0] = root;
		path[1] = node.getParent();
		path[2] = node;

		//  Exand to see additional node.
		DefaultMutableTreeNode  dummy_node = null;
		if (node.getChildCount()>0)
			path[3] = node.getChildAt(0);
		else
		{
			dummy_node = new DefaultMutableTreeNode(new Dummy());
			treeModel.insertNodeInto(dummy_node, node, node.getChildCount());
			path[3] = dummy_node;
			manage_expand = false;
		}
		tp = new TreePath(path);
		setSelectionPath(tp);
		scrollPathToVisible(tp);

		switch(obj_type)
		{
		case ADDRESS:
			//	Create a node for devices
			new_node = new DefaultMutableTreeNode(new AccessAddress("*.*.*.*"));
			treeModel.insertNodeInto(new_node, node, node.getChildCount());
			path[3] = new_node;
			tp = new TreePath(path);
			setSelectionPath(tp);
			scrollPathToVisible(tp);
			break;
		case DEVICE:
			//	Create a node for devices
			new_node = new DefaultMutableTreeNode(new AccessDevice("*/*/*", WRITE));
			treeModel.insertNodeInto(new_node, node, node.getChildCount());
			path[3] = new_node;
			tp = new TreePath(path);
			setSelectionPath(tp);
			scrollPathToVisible(tp);
			break;
		default:
			return;
		}
		if (dummy_node!=null)
			treeModel.removeNodeFromParent(dummy_node);
		manage_expand = true;
		if (!editItem())
			treeModel.removeNodeFromParent(new_node);
	  }
	//===============================================================
	//===============================================================
	EditTreeItem    edit_item;
	boolean editItem()
	{
		DefaultMutableTreeNode	node = getSelectedNode();
		if (node==null)
			return true;
		Object		o = node.getUserObject();
		int			obj_type;
		String		value;
		if (o instanceof AccessDevice)
		{
			obj_type = DEVICE;
			value = ((AccessDevice)o).name;
		}
		else
		if (o instanceof AccessAddress)
		{
			obj_type = ADDRESS;
			value = ((AccessAddress)o).name;
		}
		else
			return false;

		//	Build the inside dialog
		edit_item = new EditTreeItem(parent, this, value, obj_type);
		if (edit_item.showDlg())
		{
			String	user = node.getParent().getParent().toString();
			String	new_name = edit_item.getInputs();
			try
			{
				switch(obj_type)
				{
				case ADDRESS:
					AccessAddress	add = (AccessAddress)o;
					access_dev.removeAddress(user, add.name);
					access_dev.addAddress(user, new_name);
					add.setName(new_name);
					rebuildNode(node, add);
					break;
				case DEVICE:
					AccessDevice	dev = (AccessDevice)o;
					access_dev.removeDevice(user, dev.name, rightsStr[dev.right]);
					access_dev.addDevice(user, new_name, rightsStr[dev.right]);
					dev.name = new_name;
					rebuildNode(node, dev);
					break;
				}
				return  true;
			}
			catch(DevFailed e)
			{
				ErrorPane.showErrorMessage(parent,
					   "Error during Database access", e);
			}
		}
		return false;
	}
	//===============================================================
	//===============================================================
	private void rebuildNode(DefaultMutableTreeNode node, Object obj)
	{
		//  Set new user object to resize
		DefaultMutableTreeNode  parent_node = (DefaultMutableTreeNode)node.getParent();
		DefaultMutableTreeNode  new_node    = new DefaultMutableTreeNode(obj);
		int idx = parent_node.getIndex(node);
		treeModel.insertNodeInto(new_node, parent_node,  idx);
		treeModel.removeNodeFromParent(node);

	}
	//===============================================================
	//===============================================================
	private boolean isAllUsersNode(DefaultMutableTreeNode node)
	{
		Object	o = node.getUserObject();
		if (o instanceof AccessUser)
		{
			String user = ((AccessUser)o).getName();
			return user.equals("*");
		}
		return false;
	}
	//===============================================================
	//===============================================================
	private boolean isLastOneForAllUsers(DefaultMutableTreeNode node)
	{
		DefaultMutableTreeNode
			collec_node = (DefaultMutableTreeNode) node.getParent();
		DefaultMutableTreeNode
			user_node = (DefaultMutableTreeNode) collec_node.getParent();
		if (isAllUsersNode(user_node))
			return (collec_node.getChildCount()==1);
		return false;
	}
	//===============================================================
	//===============================================================
	void removeItem()
	{
		DefaultMutableTreeNode	node = getSelectedNode();
		if (node==null)
			return;
		Object		o = node.getUserObject();
		int			obj_type;
		String      message;
		String      user;
		if (o instanceof AccessDevice)
		{
			//	Check if not the last one for all users
			if (isLastOneForAllUsers(node))
			{
				app_util.PopupError.show(parent,
					"Cannot remove last device for all users");
				return;
			}
			user = node.getParent().getParent().toString();
			message = "Are you sure to want to remove :  " + o;
			obj_type = DEVICE;
		}
		else
		if (o instanceof AccessAddress)
		{
			//	Check if not the last one for all users
			if (isLastOneForAllUsers(node))
			{
				app_util.PopupError.show(parent,
					"Cannot remove last address for all users");
				return;
			}
			user = node.getParent().getParent().toString();
			message = "Are you sure to want to remove :  " + o;
			obj_type = ADDRESS;
		}
		else
		if (o instanceof AccessUser)
		{
			//	Check if not for all users
			if (isAllUsersNode(node))
			{
				app_util.PopupError.show(parent,
					"Cannot remove rights for all users");
				return;
			}
			user = ((AccessUser)o).getName();
			obj_type = USER_NODE;
			message = "Are you sure to want to remove all records for " + o;
		}
		else
			return;

		if (JOptionPane.showConfirmDialog(this,
				message,
				"Confirm Dialog",
				JOptionPane.YES_NO_OPTION)!=JOptionPane.OK_OPTION)
			return;


		try
		{
			switch(obj_type)
			{
			case ADDRESS:
				AccessAddress	add = (AccessAddress)o;
				access_dev.removeAddress(user, add.name);
				break;
			case DEVICE:
				AccessDevice	dev = (AccessDevice)o;
				access_dev.removeDevice(user, dev.name, rightsStr[dev.right]);
				break;
			case USER_NODE:
				access_dev.removeUser(user);
				break;
			}
			treeModel.removeNodeFromParent(node);

		}
		catch(DevFailed e)
		{
			  ErrorPane.showErrorMessage(parent,
					"Error during Database access", e);
		}
	}
	//===============================================================
	//===============================================================
	private String[] getDefinedUsers()
	{
		Vector	v = new Vector();
		for (int i=0 ; i<root.getChildCount() ; i++)
			v.add(root.getChildAt(i).toString());
		String[]	str = new String[v.size()];
		for (int i=0 ; i<v.size() ; i++)
			str[i] = (String)v.get(i);
		return str;
	}
	//===============================================================
	//===============================================================
	private void expendUserNode(String name)
	{
		//  If  already exist, show it
		DefaultMutableTreeNode  user_node = getUserNode(name);
		DefaultMutableTreeNode	collec_node;
		TreePath	tp;
		//  expand address nodes
		TreeNode[]  path = new TreeNode[4];
		path[0] = root;
		path[1] = user_node;
		collec_node = (DefaultMutableTreeNode)user_node.getChildAt(ADDRESS);
		if (collec_node.getChildCount()>0)
		{
			path[2] = collec_node;
			path[3] = collec_node.getChildAt(0);
			tp = new TreePath(path);
			setSelectionPath(tp);
		}
		//  expand device nodes if exists
		collec_node = (DefaultMutableTreeNode)user_node.getChildAt(DEVICE);
		if (collec_node.getChildCount()>0)
		{
			path[2] = collec_node;
			path[3] = collec_node.getChildAt(0);
			tp = new TreePath(path);
			setSelectionPath(tp);
		}

		//  Select user node.
		path = new TreeNode[2];
		path[0] = root;
		path[1] = user_node;
		tp = new TreePath(path);
		setSelectionPath(tp);
	}
	//===============================================================
	//===============================================================
	private DefaultMutableTreeNode getUserNode(String name)
	{
		for (int i=0 ; i<root.getChildCount() ; i++)
		{
			DefaultMutableTreeNode	node =
					(DefaultMutableTreeNode)root.getChildAt(i);
			Object		o = node.getUserObject();
			if (o.toString().equals(name))
				return node;
		}
		return new DefaultMutableTreeNode(name);
	}
	//===============================================================
	//===============================================================
	private boolean userExists(String name)
	{
		String[]	users = getDefinedUsers();
		for (String user : users)
			if (user.equals(name))
				return true;
		return false;
	}
	//===============================================================
	//===============================================================
	void cloneUser()
	{
		DefaultMutableTreeNode	node = getSelectedNode();
		if (node==null)
			return;
		Object		o = node.getUserObject();
		if (o instanceof AccessUser)
		{
			String  src_user = ((AccessUser)o).getName();
			String  new_user = "";
			boolean ok = false;
			while (!ok)
			{
				//	Get new username and check if already exists.
				 new_user = (String) JOptionPane.showInputDialog(parent,
									"New User name  ?",
									"Clone " + src_user + "  ?",
									JOptionPane.INFORMATION_MESSAGE,
									null, null, new_user);
				//	Check inputs
				if (new_user==null || new_user.length()==0)
					return;
				if (userExists(new_user))
				{
					expendUserNode(new_user);
					app_util.PopupError.show(parent, "User already exists !");
				}
				else
					ok = true;
			}
			try
			{
				access_dev.cloneUser(src_user, new_user);
			}
			catch(DevFailed e)
			{
				  ErrorPane.showErrorMessage(parent,
						"Error during Database access", e);
				  return;
			}
			DefaultMutableTreeNode  u_node =
				new DefaultMutableTreeNode(new AccessUser(new_user));
			DefaultMutableTreeNode  a_node = new DefaultMutableTreeNode(collecStr[ADDRESS]);
			DefaultMutableTreeNode  d_node = new DefaultMutableTreeNode(collecStr[DEVICE]);
			treeModel.insertNodeInto(a_node, u_node,  0);
			treeModel.insertNodeInto(d_node, u_node,  1);
			treeModel.insertNodeInto(u_node, root,  root.getChildCount());

			//  Add dummy nodes
			treeModel.insertNodeInto(new DefaultMutableTreeNode(new Dummy()), a_node, 0);
			treeModel.insertNodeInto(new DefaultMutableTreeNode(new Dummy()), d_node, 0);

			//  And expand
			TreeNode[]    path = new DefaultMutableTreeNode[3];
			TreePath      tp;
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
	void pasteItem()
	{
		DefaultMutableTreeNode	node = getSelectedNode();
		if (node==null)
			return;
		Object		o = node.getUserObject();
		int			obj_type;
		if (o instanceof String)
		{
			if (o.toString().equals(collecStr[DEVICE]))
				obj_type = DEVICE;
			else
			if (o.toString().equals(collecStr[ADDRESS]))
				obj_type = ADDRESS;
			else
				return;
		}
		else
			return;
		String  user = node.getParent().toString();
		try
		{

			DefaultMutableTreeNode  new_node = null;
			switch(obj_type)
			{
			case ADDRESS:
				String  address = copied_addresses.addressAt(0).name;
				access_dev.addAddress(user, address);
				 new_node = new DefaultMutableTreeNode(new AccessAddress(address));
				treeModel.insertNodeInto(new_node, node,  node.getChildCount());
				break;
			case DEVICE:
				String  devname = copied_devices.deviceAt(0).name;
				int     right   = copied_devices.deviceAt(0).right;
				access_dev.addDevice(user, devname, rightsStr[right]);
				new_node = new DefaultMutableTreeNode(new AccessDevice(devname, right));
				treeModel.insertNodeInto(new_node, node,  node.getChildCount());
				break;
			}
			TreeNode[]    path = new DefaultMutableTreeNode[4];
			path[0] = root;
			path[1] = node.getParent(); // user
			path[2] = node;
			path[3] = new_node;
			TreePath      tp = new TreePath(path);
			setSelectionPath(tp);
			scrollPathToVisible(tp);
		}
		catch(DevFailed e)
		{
				ErrorPane.showErrorMessage(parent,
					"Error during Database access", e);
		}
	}
	//===============================================================
	//===============================================================
	void copyItem()
	{
		DefaultMutableTreeNode	node = getSelectedNode();
		if (node==null)
			return;
		Object		o = node.getUserObject();
		int			obj_type;
		if (o instanceof AccessDevice)
			obj_type = DEVICE;
		else
			obj_type = ADDRESS;

		switch(obj_type)
		{
		case ADDRESS:
			copied_addresses.clear();
			copied_addresses.add(o);
			break;
		case DEVICE:
			copied_devices.clear();
			copied_devices.add(o);
			break;
		}
	}
	//===============================================================
	//===============================================================
	void toggleRight()
	{
		//	Get object itself
		DefaultMutableTreeNode	node = getSelectedNode();
		if (node==null)
			return;
		Object		o = node.getUserObject();

		//	Get user object
		node = (DefaultMutableTreeNode)node.getParent().getParent();
		Object	uo = node.getUserObject();
		String  user = ((AccessUser)uo).getName();

		try
		{
			AccessDevice	dev = (AccessDevice)o;
			int			new_right;
			if (dev.right==READ)
				new_right = WRITE;
			else
				new_right = READ;

			access_dev.removeDevice(user, dev.name, rightsStr[dev.right]);
			access_dev.addDevice(user, dev.name, rightsStr[new_right]);
			dev.right = new_right;
		}
		catch(DevFailed e)
		{
			  ErrorPane.showErrorMessage(parent,
					"Error during Database access", e);
		}
	}
	//===============================================================
	//===============================================================






//===============================================================
/**
 *	Renderer Class
 */
//===============================================================
private class TangoRenderer extends DefaultTreeCellRenderer
	{
		private Font[]		fonts;

		private final int	TITLE     = 0;
		private final int	ALL_USERS = 1;
		private final int	USER      = 2;
		private final int	COLLEC    = 3;
		private final int	LEAF      = 4;

		//===============================================================
		//===============================================================
		public TangoRenderer()
		{
			Utils	utils = Utils.getInstance();
			tango_icon      = utils.getIcon("network5.gif");
			all_users_icon  = utils.getIcon("user.gif");
			user_icon       = utils.getIcon("user-2.gif");
			add_icon        = utils.getIcon("server.gif");
			dev_icon        = utils.getIcon("device.gif");
			write_icon      = utils.getIcon("greenbal.gif");
			read_icon       = utils.getIcon("redball.gif");

			fonts = new Font[LEAF+1];
			fonts[TITLE]   = new Font("Dialog", Font.BOLD, 18);
			//	width fixed font
			fonts[ALL_USERS]= new Font("Dialog", Font.BOLD, 16);
			fonts[USER ]    = new Font("Dialog", Font.BOLD, 12);
			fonts[COLLEC]   = new Font("Dialog", Font.BOLD, 12);
			fonts[LEAF]     = new Font("Monospaced", Font.PLAIN, 12);
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
			if (row==0)
			{
				//	ROOT
				setFont(fonts[TITLE]);
				setIcon(tango_icon);
			}
			else
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;

				if (node.getUserObject() instanceof AccessUser)
				{
					String	user = ((AccessUser)node.getUserObject()).getName();
					if (user.equals("*"))
					{
						setFont(fonts[ALL_USERS]);
						setIcon(all_users_icon);
					}
					else
					{
						setFont(fonts[USER]);
						setIcon(user_icon);
					}
				}
				else
				if (node.getUserObject() instanceof String)
				{
					setFont(fonts[COLLEC]);
					if (obj.toString().equals(collecStr[ADDRESS]))
						setIcon(add_icon);
					else
						setIcon(dev_icon);
				}
				else
				if (node.getUserObject() instanceof AccessAddress)
				{
					setFont(fonts[LEAF]);
					setIcon(add_icon);
				}
				else
				if (node.getUserObject() instanceof AccessDevice)
				{
					setFont(fonts[LEAF]);
					AccessDevice	dev = (AccessDevice)node.getUserObject();
					if (dev.right==WRITE)
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
		 *	Classes difining structures used in tree
		 */
	//===============================================================
	class AccessUser
	{
		private String	name;
		 //===========================================================
		private AccessUser(String name)
		{
			this.name = name;
		}
		 //===========================================================
		public String getName()
		{
			return name;
		}
		 //===========================================================
		public String toString()
		{
			return (name.equals("*")? "All Users" : name);
		}
		 //===========================================================
	}
	//===============================================================
	//===============================================================
	class AccessAddress
	{
		String	name;
		private String hostname = null;
		 //===========================================================
		private AccessAddress(String add)
		{
			name  = add;
			 checkHostname(add);
		}

		 //===========================================================
		private void checkHostname(String add)
		{
			//	Split the address
			StringTokenizer stk = new StringTokenizer(add, ".");
			Vector	v = new Vector();
			while (stk.hasMoreTokens())
				v.add(stk.nextToken());
			byte[]	bytes = new byte[4];
			for (int i=0 ; i<4 && i<v.size() ; i++)
				try {
					bytes[i] = (byte)Integer.parseInt((String)v.get(i));
				}
				catch(NumberFormatException e) {
					hostname = null;
					return;
				}

			//	Check if host name
			try
			{
				 java.net.InetAddress	iadd =
					java.net.InetAddress.getByAddress(bytes);
				hostname = iadd.getHostName();
				//	remove fqdn if any
				int	pos = hostname.indexOf('.');
				if (pos>0)
					hostname = hostname.substring(0, pos);
			}
			catch(Exception e) {
				hostname = null;
			}
		}
		 //===========================================================
		private void setName(String add)
		{
			name  = add;
			 checkHostname(add);
		}
		 //===========================================================
		public String toString()
		{
			if (hostname==null)
				return name;
			else
				return name  +  "  (" + hostname + ")";
		}
		  //===========================================================
   }
	//===============================================================
	class AccessDevice
	{
		String	name;
		int 	right = READ;
		 //===========================================================
		private AccessDevice(String add, String r)
		{
			name  = add;
			for (int i=0 ; i<rightsStr.length ; i++)
				if (rightsStr[i].equals(r))
					right = i;
		}
		 //===========================================================
		private AccessDevice(String add, int r) {
			name  = add;
			right = r;
		}
		  //===========================================================
	   public String toString() { return name; }
		  //===========================================================
	}
	//===============================================================
	private class Dummy
	{
		//	notthing
		public String toString() { return ""; }
	}

	//===============================================================
	class CopiedDevices extends Vector
	{
		AccessDevice deviceAt(int i)
		{
			return (AccessDevice)get(i);
		}
	}
	//===============================================================
	class CopiedAddresses extends Vector
	{
		AccessAddress addressAt(int i)
		{
			return (AccessAddress)get(i);
		}
	}

}
