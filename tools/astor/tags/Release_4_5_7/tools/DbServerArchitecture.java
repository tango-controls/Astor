//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author$
//
// $Revision$
// $Log$
// Revision 1.1  2007/04/04 13:08:39  pascal_verdier
// *** empty log message ***
//
//
//
// Copyleft 2007 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor.tools;

import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

import admin.astor.*;


//===============================================================
/**
 *	Class Description: Basic Dialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


public class DbServerArchitecture extends JDialog
{
	private Component		parent;
	private Server		server;
	private ServerTree	tree;

    static final Dimension		dimension = new Dimension(290,400);
	//===============================================================
	/**
	 *	Creates new form DbServerArchitecture
	 */
	//===============================================================
	public DbServerArchitecture(JFrame parent, String servname) throws DevFailed
	{
		super(parent, true);
		this.parent = parent;
		createDialog(servname);
	}
	//===============================================================
	public DbServerArchitecture(JDialog parent, String servname) throws DevFailed
	{
		super(parent, true);
		this.parent = parent;
		createDialog(servname);
	}
	//===============================================================
	//===============================================================
	private void createDialog(String servname) throws DevFailed
	{
		//	Check if exists
		new DeviceProxy("dserver/" + servname);
		
		initComponents();

		setTitle("Server in Database");
		titleLabel.setText(servname + " architecture");
		server = new Server(servname);

		//	Add tree to scroll pane
		JScrollPane		spane = new JScrollPane();
        spane.setPreferredSize(dimension);
		tree = new ServerTree(this);
		spane.add(tree);
		spane.setViewportView (tree);
        getContentPane().add(spane, BorderLayout.CENTER);

		okBtn.setVisible(false);
		cancelBtn.setText("Dismiss");
		pack();
		ATKGraphicsUtils.centerDialog(this);
	}

	//===============================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//===============================================================
    private void initComponents() {//GEN-BEGIN:initComponents
          jPanel1 = new javax.swing.JPanel();
          okBtn = new javax.swing.JButton();
          cancelBtn = new javax.swing.JButton();
          jPanel2 = new javax.swing.JPanel();
          titleLabel = new javax.swing.JLabel();
          
          addWindowListener(new java.awt.event.WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent evt) {
                  closeDialog(evt);
              }
          });
          
          okBtn.setText("OK");
          okBtn.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent evt) {
                  okBtnActionPerformed(evt);
              }
          });
          
          jPanel1.add(okBtn);
          
          cancelBtn.setText("Cancel");
          cancelBtn.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent evt) {
                  cancelBtnActionPerformed(evt);
              }
          });
          
          jPanel1.add(cancelBtn);
          
          getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
          
          titleLabel.setFont(new java.awt.Font("Dialog", 1, 18));
          titleLabel.setText("Dialog Title");
          jPanel2.add(titleLabel);
          
          getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);
        
        pack();
    }//GEN-END:initComponents

	//===============================================================
	//===============================================================
	private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed
		doClose();
	}//GEN-LAST:event_okBtnActionPerformed

	//===============================================================
	//===============================================================
	private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed

	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		doClose();
	}//GEN-LAST:event_closeDialog

	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void doClose()
	{
		setVisible(false);
		dispose();

		//	Check if from shell or  from appli
		if (parent.getWidth()==0)
			System.exit(0);
	}
	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okBtn;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//===============================================================




	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String args[]) {
	
		String	servname = null;
		
		if (args.length>0)
			servname = args[0];
		try
		{
			if (servname==null)
				Except.throw_exception("ServerName_Unknown",
					"No Server name ?????", "DbServerArchitecture()");
			new DbServerArchitecture(new javax.swing.JFrame(), servname).show();
		}
		catch (DevFailed e)
		{
			ErrorPane.showErrorMessage(new JFrame(), "DbServerArchitecture", e);
			System.exit(0);
		}
	}







	
	//===============================================================
	//===============================================================
	class Server extends DbServer
	{
		String			name;
		TangoClass[]	classes;
		public Server(String name) throws DevFailed
		{
			super(name);
			this.name = name;
			
			String[]	classnames = get_class_list();
			classes = new TangoClass[classnames.length];
			for (int i=0 ; i<classnames.length ; i++)
			{
				String[]	devnames = get_device_name(classnames[i]);
				classes[i] = new TangoClass(classnames[i], devnames);
			}
		}
		//===============================================================
		public String toString()
		{
			String	str =  name + "\n";
			for (int i=0 ; i<classes.length ; i++)
				str += classes[i] +"\n";
			return str.trim();
		}
	}

	//===============================================================
	//===============================================================
	class TangoClass
	{
		String		name;
		TangoDevice[]	devices;
		//===============================================================
		public TangoClass(String classname, String[] devnames)throws DevFailed
		{
			name = classname;
			devices =new TangoDevice[devnames.length];
			for (int i=0 ; i<devnames.length ; i++)
				devices[i] = new TangoDevice(devnames[i]);
		}
		//===============================================================
		public String toString()
		{
			return name;
		}
	}
	//===============================================================
	//===============================================================
	class TangoDevice extends DbDevice
	{
		String			name;
		DbDatum[]		properties;
		TangoAtt[]		attributes;
		//===============================================================
		public TangoDevice(String name) throws DevFailed
		{
			super(name);
			this.name = name;
			properties = get_property(get_property_list("*"));
			
			Database	db = ApiUtil.get_db_obj();
			String[]	attnames;
			
			//	Get list of attributes
			try
			{
				attnames = get_attribute_list();
			}
			catch(NoSuchMethodError e)
			{
				//	Method is missing in TangORB to get attribute list
				System.out.println(e);
				System.out.println("get_device_attribute_list() not found in Database class");
				DeviceData	argin = new DeviceData();
				argin.insert(new String[] { name, "*"});
			
				DeviceData	argout =
						db.command_inout("DbGetDeviceAttributeList", argin);
				attnames = argout.extractStringArray();
			}
			//	Get attribute Properties
			DbAttribute[] db_att = db.get_device_attribute_property(name, attnames);



			attributes = new TangoAtt[attnames.length];
			for (int i=0 ; i<attnames.length ; i++)
				attributes[i] = new TangoAtt(db_att[i]);
							//get_attribute_property(attnames[i]));
		}
		//===============================================================
		public String toString()
		{
			return name;
		}
		//===============================================================
	}
	//===============================================================
	//===============================================================
	class TangoAtt
	{
		String			name;
		DbAttribute		att;
		TangoAttProp[]	prop;
		
		//===============================================================
		public TangoAtt(DbAttribute att) throws DevFailed
		{
			this.name = att.name;
			this.att  = att;
			String[]	attprop = att.get_property_list();
			prop = new TangoAttProp[attprop.length];
			for (int j=0 ; j<attprop.length ; j++)
				prop[j] = new TangoAttProp(attprop[j],
							att.get_string_value(attprop[j]));
		}
		//===============================================================
		public String toString()
		{
			return name;
		}
		//===============================================================
	}
	//===============================================================
	//===============================================================
	class TangoAttProp
	{
		String	name;
		String	strval;
		//===============================================================
		public TangoAttProp(String name, String strval)
		{
			this.name   = name;
			this.strval = strval;
		}
		//===============================================================
		public String toString()
		{
			return name;
		}
		//===============================================================
	}

	//===============================================================
	//===============================================================
	class ServerTree extends JTree
	{
		private JDialog	dialog;
		private DefaultTreeModel	treeModel;
		//===============================================================
		public ServerTree(JDialog dialog)
		{
			this.dialog = dialog;
			DefaultMutableTreeNode root = new DefaultMutableTreeNode(server.name);

			for (int i=0 ; i<server.classes.length ; i++)
			{
				DefaultMutableTreeNode	cn = 
					new DefaultMutableTreeNode(server.classes[i]);
				craateDeviceNodes(cn, server.classes[i]);
				root.add(cn);
			}

			getSelectionModel().setSelectionMode
        			(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        			//(TreeSelectionModel.SINGLE_TREE_SELECTION);

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
			setCellRenderer(new ServerRenderer());

			//	Add Action listener
			addMouseListener (new java.awt.event.MouseAdapter () {
				public void mouseClicked (java.awt.event.MouseEvent evt) {
					treeMouseClicked (evt);
				}
			});
		}
		//===============================================================
		private void craateDeviceNodes(DefaultMutableTreeNode p_node, TangoClass _class)
		{
			for (int i=0 ; i<_class.devices.length ; i++)
			{
				DefaultMutableTreeNode	node = 
					new DefaultMutableTreeNode(_class.devices[i]);
				craateAttributeNodes(node, _class.devices[i]);
				p_node.add(node);
			}
		}
		//===============================================================
		private void craateAttributeNodes(DefaultMutableTreeNode p_node, TangoDevice dev)
		{
			DefaultMutableTreeNode	n = 
				new DefaultMutableTreeNode("Attributes");
			p_node.add(n);
			
			for (int i=0 ; i<dev.attributes.length ; i++)
			{
				DefaultMutableTreeNode	node = 
					new DefaultMutableTreeNode(dev.attributes[i]);
				n.add(node);
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

			TreePath	selectedPath = tree.getPathForLocation(evt.getX(), evt.getY());
			DefaultMutableTreeNode	node =
			(DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount()-1);
			Object	o = node.getUserObject();
			int mask = evt.getModifiers();

			//	Display History if double click
			if(evt.getClickCount() == 2)
			{
				//	Check if btn1
				//------------------
				if ((mask & MouseEvent.BUTTON1_MASK)!=0)
				{
					if (o instanceof TangoAtt)
					{
						TangoAtt	att = (TangoAtt)o;
						
						//	Reteive device instance
						int			idx = selectedPath.getPathCount()-2;
						TangoDevice	dev = null;
						for ( ; dev==null && idx>0 ; idx--)
						{
							DefaultMutableTreeNode	n =
							(DefaultMutableTreeNode) selectedPath.getPathComponent(idx);
							Object	obj = n.getUserObject();
							if (obj instanceof TangoDevice)
								dev = (TangoDevice)obj;
						}

						//	If found edit attribute properties
						if (dev==null)
							System.out.println("TangoDevice not found");
						else
						{
							DevPropertyDialog	dlg = 
								new DevPropertyDialog(dialog, dev, att);
							dlg.setVisible(true);
						}
					}
				}
			}
		}
		//===============================================================
	}
//===============================================================
/**
 *	Renderer Class
 */
//===============================================================
    private class ServerRenderer extends DefaultTreeCellRenderer
	{
		private ImageIcon	root_icon;
		private ImageIcon	server_icon;
		private ImageIcon	class_icon;
		private ImageIcon	device_icon;
		private ImageIcon	list_icon;
		private ImageIcon	empty_icon;
		private ImageIcon	att_icon;
		private	Font[]		fonts;
		
		private	static final int ROOT   = 0;
		private	static final int SERVER = 1;
		private	static final int CLASS  = 2;
		private	static final int DEVICE = 2;
		private	static final int ATTR   = 3;
		//===============================================================
		//===============================================================
		public ServerRenderer()
		{
			String	img_path = AstorDefs.img_path;
			root_icon   = new ImageIcon(getClass().getResource(img_path + "server.gif"));
			class_icon  = new ImageIcon(getClass().getResource(img_path + "class.gif"));
			device_icon = new ImageIcon(getClass().getResource(img_path + "device.gif"));
			list_icon   = new ImageIcon(getClass().getResource(img_path + "greenbal.gif"));
			empty_icon  = new ImageIcon(getClass().getResource(img_path + "greyball.gif"));
			att_icon    = new ImageIcon(getClass().getResource(img_path + "uleaf.gif"));

            fonts = new Font[4];
			fonts[0] = new Font("Dialog", Font.BOLD,  18);
			fonts[1] = new Font("Dialog", Font.BOLD,  12);
			fonts[2] = new Font("Dialog", Font.PLAIN, 12);
			fonts[3] = new Font("Dialog", Font.PLAIN, 10);
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

 			setBackgroundNonSelectionColor(Color.white);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
            Object o = node.getUserObject();
			if (row==0)
			{
				//	ROOT
				setIcon(root_icon);
 				setBackgroundSelectionColor(Color.white);
 				setBackgroundNonSelectionColor(Color.white);
				setFont(fonts[ROOT]);
			}
			else
			if (o instanceof Server)
			{
				setIcon(server_icon);
				setFont(fonts[SERVER]);
			}
			else
			if (o instanceof TangoClass)
			{
				setIcon(class_icon);
				setFont(fonts[CLASS]);
			}
			else
			if (o instanceof TangoDevice)
			{
				setIcon(device_icon);
				setFont(fonts[DEVICE]);
			}
			else
			if (o instanceof String)
			{
				if (node.isLeaf())
					setIcon(empty_icon);
				else
					setIcon(list_icon);
				setFont(fonts[ATTR]);
			}
			else
			if (o instanceof TangoAtt)
			{
				setIcon(att_icon);
				setFont(fonts[ATTR]);
			}
            return this;
        }
	}
}
