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
// Revision 1.1  2008/11/19 10:05:16  pascal_verdier
// Initial revision.
//
//
// copyleft 2008 by European Synchrotron Radiation Facility, Grenoble, France
//							 All Rights Reversed
//-======================================================================

package admin.astor.tools;

import admin.astor.AstorUtil;
import admin.astor.TangoServer;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
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


public class  PoolThreadsTree  extends JTree implements TangoConst
{
	static ImageIcon	tango_icon;
	static ImageIcon	class_icon;
	static ImageIcon	cmd_icon;

	private DefaultTreeModel	    treeModel;
	private DefaultMutableTreeNode  root;
	private JDialog 	                parent;
	private	PoolThreadsInfo	pool_info;
	private TangoRenderer	renderer;

	private TangoServer	server;
	private static final Color	background = admin.astor.AstorTree.background;
	//===============================================================
	//===============================================================
	public PoolThreadsTree(JDialog parent, TangoServer server) throws DevFailed
	{
		super();
		this.parent = parent;
		this.server = server;
		setBackground(background);

		pool_info = new PoolThreadsInfo();
		buildTree();
		expandChildren(root);
		setSelectionPath(null);

		//	Enable Drag and Drop
		this.setDragEnabled(true);
		setTransferHandler(new TransferHandler("Text"));
	 }
	//===============================================================
	//===============================================================
	private void buildTree()
	{
		//  Create the nodes.
		root = new DefaultMutableTreeNode(server);
		createThreadsNodes();

		//	Create the tree that allows one selection at a time.
		getSelectionModel().setSelectionMode
				(TreeSelectionModel.SINGLE_TREE_SELECTION);

		//	Create Tree and Tree model
		treeModel = new DefaultTreeModel(root);
		setModel(treeModel);

		//Enable tool tips.
		ToolTipManager.sharedInstance().registerComponent(this);

		//  Set the icon for leaf nodes.
		renderer = new TangoRenderer();
		setCellRenderer(renderer);

		 //	Listen for collapse tree
		addTreeExpansionListener(new TreeExpansionListener () {
			public void treeCollapsed(TreeExpansionEvent e) {
				//collapsedPerfomed(e);
			}
			public void treeExpanded(TreeExpansionEvent e) {
				//expandedPerfomed(e);
			}
		});
		//	Add Action listener
		addMouseListener (new java.awt.event.MouseAdapter () {

			public void mousePressed (java.awt.event.MouseEvent evt) {
				treeMousePressed (evt);
			}
			public void mouseReleased (java.awt.event.MouseEvent evt) {
				treeMouseReleased (evt);
			}
			public void mouseClicked (java.awt.event.MouseEvent evt) {
				treeMouseClicked (evt);
			}
		});
	}
	//======================================================
	/*
	 *	Manage event on clicked mouse on JTree object.
	 */
	//======================================================
	private void treeMouseClicked (java.awt.event.MouseEvent evt)
	{
        //	Set selection at mouse position
        TreePath	selectedPath = getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath==null)
			return;
        int mask = evt.getModifiers();

        //  Check button clicked
        if(evt.getClickCount()==2 && (mask & MouseEvent.BUTTON1_MASK)!=0)
        {
        }
        else
        if ((mask & MouseEvent.BUTTON3_MASK)!=0)
        {
        }
	}
	//===============================================================
	//===============================================================
	private void createThreadsNodes()
	{
		for (int i=0 ; i<pool_info.size() ; i++)
		{
			PollThread	thread = pool_info.threadAt(i);
			//	Build node for class with all commansdds as leaf
			DefaultMutableTreeNode	node =
				new DefaultMutableTreeNode(thread);
			root.add(node);
			for (Object obj : thread)
				node.add(new DefaultMutableTreeNode(obj));
		}
	}
	//======================================================
	//======================================================
	private DefaultMutableTreeNode getSelectedNode()
	{
		return (DefaultMutableTreeNode) getLastSelectedPathComponent();
	}

	//======================================================
	//======================================================
	private Object getSelectedObject()
	{
		DefaultMutableTreeNode	node = getSelectedNode();
		if (node==null)
			return null;
		return node.getUserObject();
	}
	//===============================================================
	//===============================================================
	boolean selectedObjectIsThread()
	{
		return (getSelectedObject() instanceof PollThread);
	}
	//===============================================================
	//===============================================================
	private void expandChildren(DefaultMutableTreeNode node)
	{
		boolean	level_done = false;
		for (int i=0 ; i<node.getChildCount() ; i++)
		{
			DefaultMutableTreeNode child =
					(DefaultMutableTreeNode) node.getChildAt(i);
			if (child.isLeaf())
			{
				if (!level_done)
				{
					expandNode(child);
					level_done = true;
				}
			}
			else
				expandChildren(child);
		}
	}
	//===============================================================
	//===============================================================
	private void expandNode(DefaultMutableTreeNode node)
	{
		Vector<DefaultMutableTreeNode>	v = new Vector<DefaultMutableTreeNode>();
		v.add(node);
		while (node!=root) {
			node = (DefaultMutableTreeNode) node.getParent();
			v.insertElementAt(node, 0);
		}
		TreeNode[]    tn = new DefaultMutableTreeNode[v.size()];
		for (int i=0 ; i<v.size() ; i++)
			tn[i] = v.get(i);
		TreePath      tp = new TreePath(tn);
		setSelectionPath(tp);
		scrollPathToVisible(tp);
	}
	//===============================================================
	//===============================================================
	private DefaultMutableTreeNode getFutureSelectedNode(DefaultMutableTreeNode node)
	{
		//	Get the future selectd node, after remove.
		DefaultMutableTreeNode	parent_node =
				(DefaultMutableTreeNode) node.getParent();
		DefaultMutableTreeNode	ret_node = parent_node;
		for (int i=0 ; i<parent_node.getChildCount() ; i++)
		{
			DefaultMutableTreeNode	child_node =
					(DefaultMutableTreeNode) parent_node.getChildAt(i);
			if (child_node==node)
			{
				if (i==parent_node.getChildCount()-1)
				{
					if (i>0)
						ret_node = (DefaultMutableTreeNode) parent_node.getChildAt(i-1);
				}
				else
					ret_node = (DefaultMutableTreeNode) parent_node.getChildAt(i+1);
			}
		}
		return ret_node;
	}
	//===============================================================
	//===============================================================
	void removeThread()
	{
		DefaultMutableTreeNode	node = getSelectedNode();

		if (node!=null)
		{
			Object	obj = node.getUserObject();
			if (obj instanceof PollThread)
			{
				//	Check if device(s) associated.
				if (node.getChildCount()==0)
				{
					//	get future selected node
					DefaultMutableTreeNode	next_node = getFutureSelectedNode(node);
					//	Remove selected one
					treeModel.removeNodeFromParent(node);
                    PollThread  pt = (PollThread) obj;
					pool_info.remove(pt);
					//	And select the found node
					TreeNode[]	tree_node = next_node.getPath();
					TreePath	path = new TreePath(tree_node);
					setSelectionPath(path);
					scrollPathToVisible(path);
				}
				else
					Utils.popupError(parent, "Cannot remove a not empty thread !");
			}
		}
	}
	//===============================================================
	//===============================================================
	DefaultMutableTreeNode addThreadNode()
	{

		PollThread	new_thread = new PollThread(getNextThreadNum());
		DefaultMutableTreeNode	node =
				new DefaultMutableTreeNode(new_thread);
		treeModel.insertNodeInto(node, root, root.getChildCount());
		return node;
	}
	//===============================================================
	//===============================================================
	private int getNextThreadNum()
	{
		int	num = 0;
		for (int i=0 ; i<root.getChildCount() ; i++)
		{
			DefaultMutableTreeNode	th_node =
					(DefaultMutableTreeNode) root.getChildAt(i);
			num = ((PollThread)th_node.getUserObject()).num;
		}
		return ++num;
	}
	//===============================================================
	//===============================================================
	void putPoolThreadInfo()
	{
		//	Convert tree to device(admin) property.
		int	nb_thread = root.getChildCount();
		Vector<String> v = new Vector<String>();
		for (int i=0 ; i<root.getChildCount() ; i++) {
			DefaultMutableTreeNode	th_node =
					(DefaultMutableTreeNode) root.getChildAt(i);
			int	nb_dev = th_node.getChildCount();
			if (nb_dev>0) {
				String	s = "";
				for (int j=0 ; j<nb_dev ; j++) {
					s += th_node.getChildAt(j).toString();
					if (j<nb_dev-1)
						s += ",";
				}
				v.add(s);
			}
		}
		String[]	conf = new String[v.size()];
		for (int i=0; i<v.size() ; i++)
			conf[i] = v.get(i);
		//	And send it to database.
		try {
			DbDatum[]	argin = new DbDatum[2];
			argin[0] = new DbDatum("polling_threads_pool_size", nb_thread);
			argin[1] = new DbDatum("polling_threads_pool_conf", conf);
			server.put_property(argin);
		}
		catch(DevFailed e) {
			ErrorPane.showErrorMessage(parent, null, e);
		}
	}
	//===============================================================
	//===============================================================

	//===============================================================
	/***
	 *	Drag and Drop management
	 */
	//===============================================================
	private DefaultMutableTreeNode	dragged_node = null;
	//======================================================
	//======================================================
	private TreePath getUpperPath(int x, int y)
	{
		TreePath	selectedPath = null;
		while (selectedPath==null && y>10)
		{
			selectedPath = getPathForLocation(x, y);
			y -= 10;
		}
		return selectedPath;
	}
	//======================================================
	//======================================================
	private void treeMouseReleased (java.awt.event.MouseEvent evt)
	{
		int mask = evt.getModifiers();
		if ((mask & MouseEvent.BUTTON1_MASK)!=0)
		{
			if (dragged_node==null)
				return;
			TreePath	selectedPath = getPathForLocation(evt.getX(), evt.getY());
			if (selectedPath==null)
				if ((selectedPath=getUpperPath(evt.getX(), evt.getY()))==null)
					 return;
			DefaultMutableTreeNode	node =
			(DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount()-1);
			Object	o = node.getUserObject();
			int	pos = 0;
			if (o instanceof String)
			{
				DefaultMutableTreeNode p_node
						= (DefaultMutableTreeNode) node.getParent();
				pos = p_node.getIndex(node);
				node = p_node;
			}
			moveLeaf(node, dragged_node, pos);
			dragged_node = null;
			Cursor	cursor = new Cursor(Cursor.DEFAULT_CURSOR);
			parent.setCursor(cursor);
		}
	}
	//======================================================
	//======================================================
	private void treeMousePressed (java.awt.event.MouseEvent evt)
	{
		int mask = evt.getModifiers();
		if ((mask & MouseEvent.BUTTON1_MASK)!=0)
		{
			TreePath	selectedPath = getPathForLocation(evt.getX(), evt.getY());
			if (selectedPath==null)
				 return;
			DefaultMutableTreeNode	node =
			(DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount()-1);
			Object	o = node.getUserObject();
			if (o instanceof String)
			{
				TransferHandler	transfer = this.getTransferHandler();
				transfer.exportAsDrag(this, evt, TransferHandler.COPY);
				dragged_node = node;
				parent.setCursor(renderer.getNodeCursor(node));
			}
		}
	}
	//===============================================================
	//===============================================================
	private void moveLeaf(DefaultMutableTreeNode collec_node, DefaultMutableTreeNode leaf_node, int pos)
	{
		Object	obj = collec_node.getUserObject();
		if (obj instanceof PollThread)
		{
			treeModel.removeNodeFromParent(leaf_node);
			if (pos<0)
				treeModel.insertNodeInto(leaf_node, collec_node, collec_node.getChildCount());
			else
				treeModel.insertNodeInto(leaf_node, collec_node, pos);

			expandNode(leaf_node);
		}
	}
	//===============================================================
	//===============================================================







	//===============================================================
	/*
	 *	Polling thread object
	 */
	//===============================================================
	private class PollThread extends Vector<String>
	{
		String	name;
		int		num;
		//===========================================================
		private PollThread(int num)
		{
			this.num = num;
			this.name = "Thread " + (num+1);
		}
		//===========================================================
		public String toString() {
			return name;
		}
		//===========================================================
	}
	//===============================================================
	/*
	 *	Pool of polling threads info object
	 */
	//===============================================================
	private class PoolThreadsInfo extends Vector<PollThread>
	{
		int	size = 1;
		//===========================================================
		private PoolThreadsInfo() throws DevFailed
		{
			DbDatum[]	argin = new DbDatum[2];
			argin[0] = new DbDatum("polling_threads_pool_size");
			argin[1] = new DbDatum("polling_threads_pool_conf");
			DbDatum[]	argout = server.get_property(argin);
			String[] conf = new String[0];
			if (argout[0].is_empty() && argout[1].is_empty()) {
				//	If no property --> get device list from db
				String[]	s = server.queryDeviceFromDb();
				//	and set all for on thread
				StringBuffer	sb = new StringBuffer();
				for (int i=0 ; i<s.length ; i++)
				{
					sb.append(s[i]);
					if (i<s.length-1)
						sb.append(',');
				}
				conf = new String[1];
				conf[0] = sb.toString();
			} {
				if (!argout[0].is_empty())
					size = argout[0].extractLong();
				if (!argout[1].is_empty())
					conf = argout[1].extractStringArray();
			}
			buidConfig(conf);
		}
		//===========================================================
		private void buidConfig(String[] conf)
		{
			for (int i=0 ; i<conf.length ; i++) {
				StringTokenizer	stk = new StringTokenizer(conf[i], ",");
				PollThread	thread = new PollThread(i);
				while (stk.hasMoreTokens())
					thread.add(stk.nextToken());
				add(thread);
			}
			for (int i=conf.length ; i<size ; i++)
				add(new PollThread(i));
		}
		//===========================================================
		private PollThread threadAt(int i)
		{
			return get(i);
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

		private final int	TITLE  = 0;
		private final int	THREAD = 1;
		private final int	DEVICE = 2;
		private Cursor	    dd_cursor;

		//===============================================================
		//===============================================================
		public TangoRenderer()
		{
			Utils	utils = Utils.getInstance();
			tango_icon      = utils.getIcon("network5.gif");
			class_icon      = utils.getIcon("class.gif");
			cmd_icon        = utils.getIcon("attleaf.gif");
			dd_cursor       = getNodeCursor("drg-drp.gif");

			fonts = new Font[3];
			fonts[TITLE]  = new Font("Dialog", Font.BOLD, 18);
			//	width fixed font
			fonts[THREAD] = new Font("Dialog", Font.BOLD, 12);
			fonts[DEVICE] = new Font("Dialog", Font.PLAIN, 12);
		}

		//===============================================================
		//===============================================================
		Cursor getNodeCursor(DefaultMutableTreeNode node)
		{
			Object	o = node.getUserObject();
			if (o instanceof String)
				return dd_cursor;
			return new Cursor(Cursor.DEFAULT_CURSOR);
		}
		//===============================================================
		//===============================================================
		Cursor getNodeCursor(String filename)
		{
			java.net.URL	url =
				getClass().getResource(Utils.img_path + filename);
			Image	image = Toolkit.getDefaultToolkit().getImage(url);
			return Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), filename);
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

				if (node.getUserObject() instanceof PollThread)
				{
					setFont(fonts[THREAD]);
					setIcon(class_icon);
				}
				else
				{
					setFont(fonts[DEVICE]);
					setIcon(cmd_icon);
				}
			}
			return this;
		}
	}//	End of Renderer Class
//==============================================================================
//==============================================================================
}
