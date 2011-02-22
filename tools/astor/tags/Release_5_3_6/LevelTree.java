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
// Revision 3.7  2010/10/08 07:41:27  pascal_verdier
// Minor changes.
//
// Revision 3.6  2010/06/04 14:12:55  pascal_verdier
// Global command to change startup level added.
//
// Revision 3.5  2008/12/16 15:17:16  pascal_verdier
// Add a scroll pane in HostInfoDialog in case of too big dialog.
//
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
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbServer;
import fr.esrf.TangoApi.DbServInfo;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Vector;
import fr.esrf.tangoatk.widget.util.ErrorPane;

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

        for (TangoServer  server : level)
            root.add(new DefaultMutableTreeNode(server));

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
    @SuppressWarnings({"UnusedDeclaration"})
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
			TangoServer	server = level.get(i);
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
				found = (server==level.get(j));

			if (!found)
			{
				//	Remove node
				treeModel.removeNodeFromParent(node);
				i--;
			}
		}
	}
	//===============================================================
	//===============================================================
    void changeChangeLevel(int level)
	{
		try
		{
			//	Get statup info for first server
			DefaultMutableTreeNode	node =
				(DefaultMutableTreeNode)root.getChildAt(0);
			TangoServer	server = (TangoServer)node.getUserObject();
			DbServer	s1 = new DbServer(server.getName());
			DbServInfo	info = s1.get_info();

			PutServerInfoDialog	dialog = new PutServerInfoDialog(parent, true);
			dialog.setLocation(getLocationOnScreen());
			String	hostname = info.host;

			//	if OK put the new info to database
			if (dialog.showDialog(info, level)==PutServerInfoDialog.RET_OK)
			{
				System.out.println("Do it !");
				//	Apply
				info = dialog.getSelection();
				info.host = hostname;
				if (info!=null)
				{
					for (int i=0 ; i<root.getChildCount() ; i++)
					{
						node = (DefaultMutableTreeNode)root.getChildAt(i);
						server = (TangoServer)node.getUserObject();

						info.name = server.getName();
						server.putStartupInfo(info);
						try { Thread.sleep(20); } catch(Exception e){ /** */ }
					}
					parent.updateData();
				}
			}
		}
        catch(DevFailed e)
        {
            ErrorPane.showErrorMessage(this, null, e);
        }
	}
	//===============================================================
	//===============================================================
    void changeServerLevels()
	{
		Vector<TangoServer>	servers = new Vector<TangoServer>();
		DefaultMutableTreeNode	node;
		for (int i=0 ; i<root.getChildCount() ; i++) {
			node = (DefaultMutableTreeNode)root.getChildAt(i);
			servers.add((TangoServer)node.getUserObject());
		}
		
		for (TangoServer server : servers) {
			server.startupLevel(parent, host.getName(), getLocationOnScreen());
		}
		parent.updateData();
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
		for (int i=0 ; i<root.getChildCount() ; i++) {
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
    //======================================================
    void displayUptime()
    {
        Vector<String[]>    v = new Vector<String[]>();
        try {
            for (int i=0 ; i<root.getChildCount() ; i++)
            {
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode)root.getChildAt(i);
                TangoServer	server = (TangoServer)node.getUserObject();
                String[]    exportedStr = server.getServerUptime();
                v.add(new String[] {
                        server.getName(), exportedStr[0], exportedStr[1] } );
            }


            String[]    columns = new String[] { "Server", "Last   exported", "Last unexported" };
            String[][]  table = new String[v.size()][];
            for (int i=0 ; i<v.size() ; i++)
                table[i] = v.get(i);
            app_util.PopupTable ppt =
                    new app_util.PopupTable(parent, level.toString(),
                        columns, table, new Dimension(650, 250));
            ppt.setVisible(true);
        }
        catch(DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }
    //======================================================
    //======================================================






//======================================================
//
//	Mouse event managment.
//
//======================================================
	//======================================================
	/**
	 *	Manage event on clicked mouse on JTree object.
     * @param evt mouse event.
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
	class Level extends Vector<TangoServer>
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
				try {
					//	Check if in this level
					TangoServer	server = host.getServer(i);
					if (server.startup_level==row)
						add(server);
        		}
       			catch(NullPointerException e) {
						ErrorPane.showErrorMessage(parent, null, e);
        		}
 			}
			//	Alphabetical order
			AstorUtil.getInstance().sort(this);
		}
		//===========================================================
		TangoServer getServer(String servname)
		{
			for (TangoServer server : this) {
				if (server.getName().equals(servname))
					return server;
			}
			return null;
		}
		//===========================================================
		boolean hasRunningServer()
		{
            for (TangoServer server : this) {
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

            for (TangoServer server : this) {

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
