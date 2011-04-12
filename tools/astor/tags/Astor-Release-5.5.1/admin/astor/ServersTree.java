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
// Revision 3.7  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.6  2005/10/20 13:24:49  pascal_verdier
// Screen position management has been changed.
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
// Revision 3.2  2003/11/25 15:56:46  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
// Revision 3.1  2003/09/08 11:08:53  pascal_verdier
// *** empty log message ***
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

import fr.esrf.TangoApi.DbServInfo;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class ServersTree extends JScrollPane  implements AstorDefs
{
	private Astor				parent;
	public  JTree				tree;
	public  TangoHost[]			hosts;
	private DefaultTreeModel	treeModel;

 	//===============================================================
	//===============================================================
	public ServersTree(Astor parent, String title, Vector servnames, Vector servers_info)
	{
		this.parent = parent;

		//	Build panel and its tree
		initComponent(title, servnames, servers_info);

	}
	//===============================================================
	//===============================================================
	private void initComponent(String title, Vector servnames, Vector servers_info)
	{
	
		//Create the nodes.
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(title);

		createNodes(root, servnames, servers_info);

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
		 * Note: In the Swing 1.0.x release, we used
		 * swing.plaf.basic.BasicTreeCellRenderer.
		 */
		tree.setCellRenderer(new TangoRenderer());

		//Listen for when the selection changes.
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				serverSelectionPerformed(e);
			}
		});

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
	private DbServInfo	selection;
	//===============================================================
	//===============================================================
	public void serverSelectionPerformed(TreeSelectionEvent e) 
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                   tree.getLastSelectedPathComponent();

		if (node == null) return;
		Object obj = node.getUserObject();
		if (node.isLeaf())
		{
			if (obj instanceof DbServInfo)
				selection = (DbServInfo)obj;
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
		int mask = evt.getModifiers();
		//	Do something only if double click
		//-------------------------------------
		if(evt.getClickCount() == 2)
		{
			//	Check if btn1
			//------------------
			if ((mask & MouseEvent.BUTTON1_MASK)!=0)
				if (selection!=null  &&
					path.getPathCount()-2==LEAF)
						parent.tree.displayHostInfo();
		}
	}
	//===============================================================
	//===============================================================
	private void createNodes(DefaultMutableTreeNode root, Vector servnames, Vector servers_info)
	{
		DefaultMutableTreeNode[] collection =
					new DefaultMutableTreeNode[servnames.size()];

		for (int i=0 ; i<servnames.size() ; i++)
		{
			collection[i] = new DefaultMutableTreeNode(servnames.elementAt(i));
			root.add(collection[i]);
		}

        //	Add instance nodes
		for (int i=0 ; i<servnames.size() ; i++)
		{
			for (int inf=0 ; inf<servers_info.size() ; inf++)
			{
				DbServInfo[]	servinfo = (DbServInfo[])servers_info.elementAt(inf);
				for (DbServInfo info : servinfo)
				{
					String sname = (String) servnames.elementAt(i);
					if (info.name.indexOf(sname) >= 0)
					{
						ServerInfo s = new ServerInfo(info);
						DefaultMutableTreeNode instance =
								new DefaultMutableTreeNode(s);
						collection[i].add(instance);
					}
				}
			}
		}
    }


//===============================================================
/**
 *	Renderer Class
 */
//===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer
	{
		ImageIcon	tangoIcon;
		String		tango_host = AstorUtil.getTangoHost();		
		ImageIcon	serv_icon;
		ImageIcon	inst_icon;
		Font[]		fonts;
		
		private final int	TITLE = 0;
		private final int	LEAF  = 1;

		//===============================================================
		//===============================================================
		public TangoRenderer()
		{
			//tangoIcon = new ImageIcon(getClass().getResource(img_path + "tango_icon.jpg"));
			tangoIcon = new ImageIcon(getClass().getResource(img_path + "network5.gif"));

			serv_icon = new ImageIcon(getClass().getResource(img_path + "server.gif"));
			inst_icon = new ImageIcon(getClass().getResource(img_path + "device.gif"));
		
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
				if (leaf)
				{
					//	Instance object
 					setBackgroundSelectionColor(java.awt.Color.lightGray);
					setIcon(inst_icon);
					setFont(fonts[LEAF]);
					setToolTipText("Double click to popup host panel.");
				}
				else
				{
					//	server collection
 					setBackgroundSelectionColor(java.awt.Color.white);
					setIcon(serv_icon);
					setFont(fonts[LEAF]);
					setToolTipText("Server");
				}
			}
            return this;
        }
	}
}

