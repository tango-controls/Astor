//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the LevelTree class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.4  2008/11/19 09:59:56  pascal_verdier
// New tests done on Access control.
// Pool Threads management added.
// Size added as preferences.
//
// Revision 3.3  2008/06/16 11:50:39  pascal_verdier
// Level trees are now displayed on 2 rows.
//
// Revision 3.2  2008/05/28 13:19:20  pascal_verdier
// Start/Stop All do not ask for not concerned levels.
//
// Revision 3.1  2008/05/26 11:49:12  pascal_verdier
// Host info dialog servers are managed in a jtree.
//
//
// Copyleft 2008 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;

import fr.esrf.Tango.DevState;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Vector;

//===============================================================
/**
 *	This class is a JTree to display architecture.
 *
 * @author  Pascal Verdier
 */
//===============================================================

public class LevelTree extends JTree implements AstorDefs
{
	private HostInfoDialog		parent;
	private DefaultTreeModel	treeModel;
	private ServerPopupMenu		server_menu;
	private ServerPopupMenu		level_menu;
	private TangoHost			host;
	private Color				bg;
	private Level				level;
	private	DefaultMutableTreeNode	root;

    //===============================================================
	//===============================================================
	public LevelTree(JFrame frame, HostInfoDialog parent, TangoHost host, int level_row)
	{
		this.parent = parent;
		this.host   = host;
		//this.level  = level;

		bg = parent.getBackgroundColor();
		setBackground(bg);
		server_menu = new ServerPopupMenu(frame, parent, host, ServerPopupMenu.SERVERS);
		level_menu  = new ServerPopupMenu(frame, parent, host, ServerPopupMenu.LEVELS);
		
		level = new Level(level_row);
        initComponent();

		manageVisiblity();
	}
	//===============================================================
	//===============================================================
	public boolean hasRunningServer()
	{
		return level.hasRunningServer();
	}
	//===============================================================
	//===============================================================
	public DevState getState()
	{
		return level.getState();
	}
    //===============================================================
	//===============================================================
	public int getNbServers()
	{
		return level.size();
	}
    //===============================================================
	//===============================================================
	public void manageVisiblity()
	{
		setVisible(level.size()>0);	//	Display only if servers exist
	}
    //===============================================================
	//===============================================================
	public TangoServer getServer(String servname)
	{
		return level.getServer(servname);
	}
    //===============================================================
	//===============================================================
	public int getLevelRow()
	{
		return level.row;
	}
	//===============================================================
	//===============================================================
	private void initComponent()
	{

		//Create the nodes.
		root = new DefaultMutableTreeNode(level);
		
		for (int i=0 ; i<level.size() ; i++)
			root.add(new DefaultMutableTreeNode(level.getServer(i)));

		//Create a tree that allows multi selection at a time.
		getSelectionModel().setSelectionMode
        		(TreeSelectionModel.SINGLE_TREE_SELECTION);

		//	Create Tree and Tree model
		//------------------------------------
		treeModel = new DefaultTreeModel(root);
		setModel(treeModel);

		//Enable tool tips.
		ToolTipManager.sharedInstance().registerComponent(this);

		/*
		 * Set the icon for leaf nodes.
		 * Note: In the Swing 1.0.x release, we used
		 * swing.plaf.basic.BasicTreeCellRenderer.
		 */
		setCellRenderer(new TangoRenderer());

		//	Add Action listener
		addMouseListener (new java.awt.event.MouseAdapter () {
			public void mouseClicked (java.awt.event.MouseEvent evt) {
				treeMouseClicked (evt);
			}
		});
		//	Listen for collapse tree
		addTreeExpansionListener(new TreeExpansionListener () {
			public void treeCollapsed(TreeExpansionEvent evt) {
				parent.packTheDialog();
			}
			public void treeExpanded(TreeExpansionEvent evt) {
				parent.packTheDialog();
			}
		});

		addTreeSelectionListener(new TreeSelectionListener () {
			public void valueChanged(TreeSelectionEvent evt) {
				selectionChanged(evt);
			}
		});

		//	Collapse
		if (level.row==LEVEL_NOT_CTRL)
			collapseTree();
	}
	//======================================================
	//======================================================
	public void selectionChanged(TreeSelectionEvent evt)
	{
		//parent.fireNewTreeSelection(this);
	}
	//======================================================
	//======================================================
	public void checkUpdate()
	{
		level.updateServerList();
		manageVisiblity();

		//	check if new server
		for (int i=0 ; i<level.size() ; i++)
		{
			TangoServer	server = level.getServer(i);
			DefaultMutableTreeNode	node = root;
			int	nb = root.getChildCount();
			boolean	found = false;
			for (int j=0 ; !found && j<nb ; j++)
			{
				node = node.getNextNode();
				TangoServer	ts = (TangoServer)node.getUserObject();
				found = (ts == server);
			}
			if (!found)
			{
				//	Add a new node
				node = new DefaultMutableTreeNode(server);
				treeModel.insertNodeInto(node, root, i);
				setSelectionPath(new TreePath(node.getPath()));
				expandRow(i);
			}
		}

		//	check if some have desapeared
		DefaultMutableTreeNode	node;
		for (int i=0 ; i<root.getChildCount() ; i++)
		{
			node = (DefaultMutableTreeNode)root.getChildAt(i);
			TangoServer	server = (TangoServer)node.getUserObject();

			boolean	found = false;
			for (int j=0 ; !found && j<level.size() ; j++)
				found = (server==level.getServer(j));

			if (!found)
			{
				//	Remove node
				treeModel.removeNodeFromParent(node);
				i--;
			}
		}
	}
	//======================================================
	//======================================================
	public void resetSelection()
	{
		manageVisiblity();
		setSelectionPath(new TreePath(root.getPath()));
	}
	//======================================================
	//======================================================
	public void setSelection(TangoServer server)
	{
		manageVisiblity();
		DefaultMutableTreeNode	node;
		for (int i=0 ; i<root.getChildCount() ; i++)
		{
			node = (DefaultMutableTreeNode)root.getChildAt(i);
			TangoServer	ts = (TangoServer)node.getUserObject();
			if (ts==server)
				setSelectionPath(new TreePath(node.getPath()));
		}
	}
	//======================================================
	//======================================================
	void expandTree()
	{
		expandRow(0);
	}

	//======================================================
	//======================================================
	void collapseTree()
	{
		collapseRow(0);
	}
	//======================================================
	//======================================================
	void toggleExpandCollapse()
	{
		if (isExpanded(0))
			collapseTree();
		else
			expandTree();
	}

//======================================================
//
//	Mouse event managment.
//
//======================================================
	//======================================================
	/**
	 *	Manage event on clicked mouse on JTree object.
	 */
	//======================================================
	private void treeMouseClicked (java.awt.event.MouseEvent evt)
	{
		//	Check if click is on a node
		if (getRowForLocation(evt.getX(), evt.getY())<0)
			return;

		TreePath	selectedPath = getPathForLocation(evt.getX(), evt.getY());
		DefaultMutableTreeNode	node =
		(DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount()-1);
		Object	uo = node.getUserObject();
		int mask = evt.getModifiers();
		parent.fireNewTreeSelection(this);

		//	Display History if double click
		if(evt.getClickCount() == 2)
		{
			//	Check if btn1
			//------------------
			if ((mask & MouseEvent.BUTTON1_MASK)!=0)
			{
			}
		}
		else
        if ((mask & MouseEvent.BUTTON3_MASK)!=0)
		{
			if (uo instanceof TangoServer)
				server_menu.showMenu(evt, this, (TangoServer)uo);
			else
				level_menu.showMenu(evt, this, isExpanded(0));
		}
	}
	//===============================================================
	//===============================================================
	public String toString()
	{
		return level.toString();
	}
	//===============================================================
	//===============================================================



    //===============================================================
	//===============================================================
	class Level extends Vector
	{
		public int	row;
		//==========================================================
		public Level(int row)
		{
			this.row = row;
			updateServerList();
		}
		//===========================================================
		private void updateServerList()
		{
			clear();
			for (int i=0 ; i<host.nbServers() ; i++)
			{
				//	Check if in this level
				TangoServer	server = host.getServer(i);
				if (server.startup_level==row)
					add(server);
			}
			//	Alphabetical order
			AstorUtil.getInstance().sort(this);
		}
		//===========================================================
		TangoServer getServer(int idx)
		{
			return (TangoServer)get(idx);
		}
		//===========================================================
		TangoServer getServer(String servname)
		{
			for (int i=0 ; i<size() ; i++)
			{
				TangoServer	server = getServer(i);
				if (server.getName().equals(servname))
					return server;
			}
			return null;
		}
		//===========================================================
		boolean hasRunningServer()
		{
 			for (int i=0 ; i<size() ; i++)
			{
				TangoServer	server = getServer(i);
				if (server.getState() == DevState.ON)
					return true;
			}
			return false;
			
		}
		//===========================================================
		DevState getState()
		{
			boolean		is_faulty  = false;
			boolean 	is_alarm   = false;
            boolean 	is_moving  = false;

 			for (int i=0 ; i<size() ; i++)
			{
				TangoServer	server = getServer(i);

				//	At least one unknown -> branch is unknown
				if (server.getState() == DevState.UNKNOWN)
					return DevState.UNKNOWN;
				else
				if (server.getState() == DevState.FAULT)
					is_faulty = true;
				else
				if (server.getState() == DevState.ALARM)
					is_alarm = true;
				else
				if (server.getState() == DevState.MOVING)
					is_moving = true;
			}
			//	Calculate branch state
			if (is_faulty)
				return DevState.FAULT;
			else
			if (is_moving)
				return DevState.MOVING;
			else
			if (is_alarm)
				return DevState.ALARM;
			else
				return DevState.ON;
		}
		//===========================================================
		public String toString()
		{
			String	str;
			if (row!=LEVEL_NOT_CTRL)
				str = "Level " + row;
			else
				str = "Not Controlled";
			return str;// + " ("+size()+")";
		}
		//===========================================================
	}




//===============================================================
/**
 *	Renderer Class
 */
//===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer
	{
		private	Font[]		fonts;
		//===============================================================
		//===============================================================
		public TangoRenderer()
		{
            fonts = new Font[3];
			fonts[0] = new Font("Dialog", Font.BOLD,  16);
			fonts[1] = new Font("Dialog", Font.PLAIN,  12);
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

 			setBackgroundNonSelectionColor(bg);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
            Object uo = node.getUserObject();

			setFont(fonts[node.getLevel()]);
			
			if (row==0)
			{
				//	ROOT (Level number)
				setIcon( getStateIcon(level.getState()) );
 				setBackgroundSelectionColor(bg);
			}
			else
			if (uo instanceof TangoServer)
			{
				TangoServer	server = (TangoServer)uo;
				setIcon(getStateIcon(server.getState()));
 				setBackgroundSelectionColor(Color.lightGray);
			}
            return this;
        }
		//===============================================================
		//===============================================================
		private ImageIcon getStateIcon(DevState state)
		{
			int	idx;
			if (state==DevState.MOVING)
				idx= moving;
			else
			if (state==DevState.ON)
				idx = all_ok;
			else
			if (state==DevState.ALARM)
				idx = alarm;
			else
				idx = faulty;
			return AstorUtil.state_icons[idx];
		}
		//===============================================================
		//===============================================================
	}
}
