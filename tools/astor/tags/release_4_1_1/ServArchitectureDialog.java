//+======================================================================
// $Source$
//
// Project:   Tango Manager (Astor)
//
// Description:   Dialog Class to display server architecture.
//
// $Author$
//
// $Revision$
// $Log$
// Revision 3.7  2005/11/24 12:24:57  pascal_verdier
// DevBrowser utility added.
// MkStarter utility added.
//
// Revision 3.6  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.5  2005/10/14 14:28:28  pascal_verdier
// Edit property added to server achitecture tree.
//
// Revision 3.4  2005/10/06 06:45:14  pascal_verdier
// Bug in default/DB value fixed.
//
// Revision 3.3  2005/09/27 12:45:59  pascal_verdier
// Expand button added.
//
// Revision 3.2  2005/09/15 13:44:04  pascal_verdier
// jive.MultiLineToolTipUI.initialize() call added.
//
// Revision 3.1  2005/09/15 08:26:36  pascal_verdier
// Server architecture display addded.
//
//
//
// Copyleft 2005 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.StringTokenizer;
import java.util.Vector;


//===============================================================
/**
 *	Class Description: Basic Dialog Class to display info
 *
 *	@author  root
 */
//===============================================================


public class ServArchitectureDialog extends JDialog {

	private String	servname;
	private boolean	from_appli = true;
	private boolean	modified = false;
	
	private ServInfoTree	tree;
	static public final boolean EXPAND_NOT_FULL = false;
	static public final boolean EXPAND_FULL     = true;

    //===============================================================
    /**
     *	Creates new form ServArchitectureDialog
     */
    //===============================================================
    public ServArchitectureDialog(JDialog parent, String servname) throws DevFailed
    {
        super(parent, false);
        this.servname = servname;
        initComponents();

        titleLabel.setText(servname + "  Architecture");

        //	Show the result
        tree = new ServInfoTree(this);
        jScrollPane1.setViewportView (tree);

        //	Check if from an appli or from an empty JDialog
        if (parent.getWidth()==0)
            from_appli = false;

        jive.MultiLineToolTipUI.initialize();
        pack();
        tree.expandTree(EXPAND_NOT_FULL);
    }
    //===============================================================
    /**
     *	Creates new form ServArchitectureDialog
     */
    //===============================================================
    public ServArchitectureDialog(JFrame parent, String servname) throws DevFailed
    {
        super(parent, false);
        this.servname = servname;
        initComponents();

        titleLabel.setText(servname + "  Architecture");

        //	Show the result
        tree = new ServInfoTree(this);
        jScrollPane1.setViewportView (tree);

        //	Check if from an appli or from an empty JDialog
        if (parent.getWidth()==0)
            from_appli = false;

        jive.MultiLineToolTipUI.initialize();
        pack();
        tree.expandTree(EXPAND_NOT_FULL);
    }

    //===============================================================
    /**
     *	Creates new form ServArchitectureDialog
     */
    //===============================================================
    public ServArchitectureDialog(JDialog parent, DeviceProxy dev) throws DevFailed
    {
        super(parent, false);
        this.servname = dev.name().substring("dserver/".length());
        initComponents();

        titleLabel.setText(servname + "  Architecture");

        //	Show the result
        tree = new ServInfoTree(this);
        jScrollPane1.setViewportView (tree);

        //	Check if from an appli or from an empty JDialog
        if (parent.getWidth()==0)
            from_appli = false;

        jive.MultiLineToolTipUI.initialize();
        pack();
        tree.expandTree(EXPAND_NOT_FULL);
    }
    //===============================================================
    /**
     *	Creates new form ServArchitectureDialog
     */
    //===============================================================
    public ServArchitectureDialog(JFrame parent, DeviceProxy dev) throws DevFailed
    {
        super(parent, false);
        this.servname = dev.name().substring("dserver/".length());
        initComponents();

        titleLabel.setText(servname + "  Architecture");

        //	Show the result
        tree = new ServInfoTree(this);
        jScrollPane1.setViewportView (tree);

        //	Check if from an appli or from an empty JDialog
        if (parent.getWidth()==0)
            from_appli = false;

        jive.MultiLineToolTipUI.initialize();
        pack();
        tree.expandTree(EXPAND_NOT_FULL);
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
        expandBtn = new javax.swing.JRadioButton();
        cancelBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        expandBtn.setText("Expand all properties");
        expandBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expandBtnActionPerformed(evt);
            }
        });

        jPanel1.add(expandBtn);

        cancelBtn.setText("Dismiss");
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

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

	//===============================================================
	//===============================================================
	private void expandBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandBtnActionPerformed

		if (expandBtn.getSelectedObjects()!=null)
			tree.expandTree(EXPAND_FULL);
		else
			tree.expandTree(EXPAND_NOT_FULL);

	}//GEN-LAST:event_expandBtnActionPerformed

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
		//	If modified -->  propose to restart devices
		if (modified)
		{
			if (JOptionPane.showConfirmDialog(this,
					"Some properties have been modified !\n\n" +
						"Do you want a restart devices ?",
						"Dialog",
						JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
			{
				try
				{
					new DeviceProxy("dserver/" + servname).command_inout("init");
				}
				catch (DevFailed e)
				{
					app_util.PopupError.show(this, e);
				}
			}	
		}
		setVisible(false);
		dispose();
		if (from_appli==false)
			System.exit(0);
	}
	//===============================================================
	//===============================================================
	public void showDialog()
	{
		setVisible(true);
	}
	//===============================================================
	//===============================================================
	static private String	separator = ", ";
	static public String multiLine2OneLine(String str)
	{
		if (str==null)	return str;
		//	Take of '\n'
		int	idx;
		while ((idx=str.indexOf('\n'))>=0)
			str = str.substring(0, idx) + separator +
				str.substring(idx + 1);
		return str;
	}
	//===============================================================
	//===============================================================
	static public String OneLine2multiLine(String str)
	{
		if (str==null)	return str;
		//	replace ", " by '\n'
		int	idx;
		while ((idx=str.indexOf(separator))>=0)
			str = str.substring(0, idx) + "\n" +
				str.substring(idx + separator.length());
		return str;
	}
	//===============================================================
	//===============================================================
	static public String[] string2array(String str)
	{
		Vector	v = new Vector();
		StringTokenizer stk= new StringTokenizer(str, separator);
		while (stk.hasMoreTokens())
			v.add(stk.nextToken());

		String[]	array = new String[v.size()];
		for (int i=0 ; i<v.size() ; i++)
			array[i] = (String)v.elementAt(i);
		return array;
	}

	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton expandBtn;
    private javax.swing.JButton cancelBtn;
    // End of variables declaration//GEN-END:variables
	//===============================================================







	//===============================================================
	/**
	 *	JTree Class
	 */
	//===============================================================
	class ServInfoTree extends JTree 
	{
		private Component   parent;
		private DeviceProxy	dev     = null;
		private String[]	devlist = null;
		private DefaultTreeModel	treeModel;
		private DefaultMutableTreeNode root;
		//===============================================================
		//===============================================================
		public ServInfoTree(Component parent) throws DevFailed
		{
			super();
			this.parent = parent;
			initComponent();
		}
		//===============================================================
		//===============================================================
		public ServInfoTree(DeviceProxy dev) throws DevFailed
		{
			super();
			this.dev = dev;
			initComponent();
		}
		//===============================================================
		//===============================================================
		void expandTree(boolean expand)
		{
			expandTree(root, expand);
		}
		//===============================================================
		/**
		 *	Expend tree from node (re-entring method)
		 *
		 *	@param node origin to start expanding.
		 */
		//===============================================================
		private void expandTree(DefaultMutableTreeNode node, boolean expand)
		{
			int	nb = node.getChildCount();
			for (int i=0 ; i<nb ; i++)
			{
				DefaultMutableTreeNode	child = 
						(DefaultMutableTreeNode)node.getChildAt(i);

				Object obj = child.getUserObject();
				//	Check if all must be expanded
				if (expand==EXPAND_FULL ||
					((obj instanceof TgProperty) == false) )
				{
					TreePath	path = new TreePath(child.getPath());
					expandPath(path);
					expandTree(child, expand);
				}

				//	Check if something must be collapsed
				if (expand==EXPAND_NOT_FULL &&
					((obj instanceof TgProperty) == true) )
				{
					TreePath	path = new TreePath(child.getPath());
					collapsePath(path);
				}
			}
		}
		//===============================================================
		//===============================================================
		private void initComponent() throws DevFailed
		{
			//	Create the nodes (root is the server).
			root = new DefaultMutableTreeNode(new TgServer(servname));

			createNodes(root);
			//	Create the tree that allows one selection at a time.
			getSelectionModel().setSelectionMode
        			(TreeSelectionModel.SINGLE_TREE_SELECTION);

			//	Create Tree and Tree model
			//------------------------------------
			treeModel = new DefaultTreeModel(root);
			setModel(treeModel);

			//	Enable tool tips.
			ToolTipManager.sharedInstance().registerComponent(this);

			//	Set the icon for leaf nodes.
			setCellRenderer(new TangoRenderer());
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
		 *	Create the server tree
		 */
		//===============================================================
		private void createNodes(DefaultMutableTreeNode root) throws DevFailed
		{
			if (dev==null)
				dev = new DeviceProxy("dserver/"+servname);

			TgClass[]	classes = getClasses();
			DefaultMutableTreeNode[] classnodes =
				new DefaultMutableTreeNode[classes.length];
			for (int c=0 ; c<classes.length ; c++)
			{
				//	Display class part
				classnodes[c] = new DefaultMutableTreeNode(classes[c]);
				root.add(classnodes[c]);

				for (int p=0 ; p<classes[c].properties.length ; p++)
				{
					TgProperty	prop = classes[c].properties[p];
					DefaultMutableTreeNode	node = new DefaultMutableTreeNode(prop);
					classnodes[c].add(node);
					node.add(new DefaultMutableTreeNode(prop.getValue()));
				}

				//	Display device part
				TgDevice[]	devices  = getDevices(classes[c].name);
				DefaultMutableTreeNode[] devnodes =
					new DefaultMutableTreeNode[devices.length];

				//	Build for each device
				for (int d=0 ; d<devices.length ; d++)
				{
					devnodes[d] = new DefaultMutableTreeNode(devices[d]);
					classnodes[c].add(devnodes[d]);
					for (int p=0 ; p<devices[d].properties.length ; p++)
					{
						TgProperty	prop = devices[d].properties[p];
						DefaultMutableTreeNode	node = new DefaultMutableTreeNode(prop);
						devnodes[d].add(node);
						node.add(new DefaultMutableTreeNode(prop.getValue()));
					}
				}
			}
		}
		//===============================================================
		//===============================================================
		private TgDevice[] getDevices(String classname) throws DevFailed
		{
			if (devlist==null)
			{
				DeviceData	argout  = dev.command_inout("QueryDevice");
				devlist = argout.extractStringArray();
			}
			//	get only the device name for specified class
			Vector	v = new Vector();
			String	str = classname + "::";
			for (int i=0 ; i<devlist.length ; i++)
				if (devlist[i].startsWith(str))
					v.add(devlist[i].substring(str.length()));
			String[]	devnames = new String[v.size()];
			for (int i=0 ; i<v.size() ; i++)
				devnames[i] = (String) v.elementAt(i);

			//	Build properties for each device
			TgProperty[]	dev_prop = getProperties(classname, "Dev");
			TgDevice[]		devices = new TgDevice[devnames.length];
			for (int i=0 ; i<devnames.length ; i++)
				devices[i] = new TgDevice(devnames[i], dev_prop);
			return devices;
		}
		//===============================================================
		//===============================================================
		private TgClass[] getClasses() throws DevFailed
		{
			//	Get the class list
			DeviceData	argout     = dev.command_inout("QueryClass");
			String[]	classnames = argout.extractStringArray();

			//	Build properties for each class
			TgClass[]		classes = new TgClass[classnames.length];
			for (int i=0 ; i<classnames.length ; i++)
			{
				TgProperty[]	prop    = getProperties(classnames[i], "Class");
				classes[i] = new TgClass(classnames[i], prop);
			}
			return classes;
		}
		//===============================================================
		//===============================================================
		private TgProperty[] getProperties(String classname, String source) throws DevFailed
		{
			DeviceData		argin  = new DeviceData();
			argin.insert(classname);
			String			cmd    = "QueryWizard" + source + "Property";
			DeviceData		argout = dev.command_inout(cmd, argin);
			String[]		str    = argout.extractStringArray();
			TgProperty[]	prop   = new TgProperty[str.length/3];
			for (int i=0, n=0 ; i<str.length ; n++, i+=3)
				prop[n] = new TgProperty(classname, source, str[i], str[i+1], str[i+2]);

			return prop;
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

			int mask = evt.getModifiers();
			//	Do something only if double click
			//-------------------------------------
			if(evt.getClickCount() == 2)
			{
				//	Check if btn1
				//------------------
				if ((mask & evt.BUTTON1_MASK)!=0)
				{
					//	Check if on a property value
					DefaultMutableTreeNode	node =
						(DefaultMutableTreeNode) getLastSelectedPathComponent();
					Object	o = node.getUserObject();
					if (node.isLeaf() && o instanceof String)
					{
						editProperty(node);
					}
				}
			}
		}
		//===============================================================
		//===============================================================
		private void editProperty(DefaultMutableTreeNode node)
		{
			DefaultMutableTreeNode	prop_node = 
						(DefaultMutableTreeNode)node.getParent();
			TgProperty	property = (TgProperty)prop_node.getUserObject();
			EditPropertyDialog	dialog;
            if (parent instanceof JDialog)
               dialog = new EditPropertyDialog((JDialog)parent, property);
            else
               dialog = new EditPropertyDialog((JFrame)parent, property);
			if ((property=dialog.showDialog())!=null)
			{
				//	Get the Class or device parent
				DefaultMutableTreeNode	tg_node = 
						(DefaultMutableTreeNode)prop_node.getParent();
				Object	o = tg_node.getUserObject();
				try
				{
					if (o instanceof TgClass)
					{
						TgClass	_class = (TgClass)o;
						_class.put_property(property);
						//	Re-create node to resize.
						replaceNode(node, property.getValue());
						modified = true;
					}
					else
					if (o instanceof TgDevice)
					{
						TgDevice	dev = (TgDevice)o;
						dev.put_property(property);
						//	Re-create node to resize.
						replaceNode(node, property.getValue());
						modified = true;
					}
					else
						System.out.println("object " + o + "  not implemented !");
				}
				catch(DevFailed e)
				{
					app_util.PopupError.show(this,e);
				}
			}
		}
		//===============================================================
		//===============================================================
		private void replaceNode(DefaultMutableTreeNode node, String str)
		{
			//	Get parent node and node position.
			DefaultMutableTreeNode	parent_node =
								(DefaultMutableTreeNode)node.getParent();
			int	pos =0;
			for (int i=0 ; i<parent_node.getChildCount() ; i++)
				if (parent_node.getChildAt(i).equals(node))
					pos = i;

			//	Build ne node and insert
			DefaultMutableTreeNode	new_node = new DefaultMutableTreeNode(str);
			treeModel.insertNodeInto(new_node, parent_node, pos);

			//	Remove old one
			treeModel.removeNodeFromParent(node);
		}
		//===============================================================
		//===============================================================
	}





	//===============================================================
	/**
	 *	Class to define TANGO Server object
	 */
	//===============================================================
	class TgServer
	{
		String	name;
		String	desc;
		
		//===============================================================
		public TgServer(String name)
		{
			this.name  = name;
			this.desc  = "";
			
			try
			{
				String		admin = "dserver/" + name;
				IORdump 	id = new IORdump(admin);
				DeviceInfo	info = new DbDevice(admin).get_info();
				desc = id.toString() + "\nsince " + info.last_exported;
			}
			catch(DevFailed e){}
		}
		//===============================================================
		public String toString()
		{
			return name;
		}
	}
	//===============================================================
	/**
	 *	Class to define TANGO Device object
	 */
	//===============================================================
	class TgDevice extends DeviceProxy
	{
		String			name;
		TgProperty[]	properties;
		public TgDevice(String name, TgProperty[] properties) throws DevFailed
		{
			super(name);
			this.name = name;

			//	Copy the default properties
			this.properties = new TgProperty[properties.length];
			for (int i=0 ; i<properties.length ; i++)
			{
				this.properties[i] = new TgProperty (name,
													properties[i].src,
													properties[i].name,
													properties[i].desc,
													properties[i].def_value);
				//	And Check the database
				try
				{
					DbDatum	data = get_property(properties[i].name);
					if (data.is_empty()==false)
						this.properties[i].setDbValue(data.extractStringArray());
				}
				catch(DevFailed e){
					Except.print_exception(e);
				}
			}
		}
		//===============================================================
		//===============================================================
		void put_property(TgProperty prop) throws DevFailed
		{
			if (prop.db_value==null)
			{
				//	remove property in database
				delete_property(prop.name);
			}
			else
			{
				String[]	value = string2array(prop.db_value);
				DbDatum[]	data = { new DbDatum(prop.name) };
				data[0].insert(value);
				put_property(data);
			}
		}
		//===============================================================
		//===============================================================
		public String toString()
		{
			return "Device: " + name;
		}
	}
	//===============================================================
	/**
	 *	Class to define TANGO Class Object
	 */
	//===============================================================
	class TgClass extends DbClass
	{
		String	name;
		String	desc;
		TgProperty[]	properties;
		//===============================================================
		public TgClass(String name, TgProperty[] properties) throws DevFailed
		{
			super(name);
			this.name  = name;
			this.desc  = "No Description Found in Database";
			try
			{
				//	Try to get description
				DbDatum data = get_property("Description");
				if (data.is_empty()==false)
				{
					String[]	array = data.extractStringArray();					
					this.desc = "";
					for (int i=0 ; i<array.length ; i++)
						this.desc += array[i] + "\n";
				}
				
				//	Copy the default properties
				this.properties = new TgProperty[properties.length];
				for (int i=0 ; i<properties.length ; i++)
				{
					this.properties[i] = new TgProperty (name,
														properties[i].src,
														properties[i].name,
														properties[i].desc,
														properties[i].def_value);
					//	And Check the database
					data = get_property(properties[i].name);
					if (data.is_empty()==false)
						this.properties[i].setDbValue(data.extractStringArray());
				}
			}
			catch(DevFailed e) {}
		}
		//===============================================================
		//===============================================================
		void put_property(TgProperty prop) throws DevFailed
		{
			if (prop.db_value==null)
			{
				//	remove property in database
				delete_property(prop.name);
			}
			else
			{
				String[]	value = string2array(prop.db_value);
				DbDatum[]	data = { new DbDatum(prop.name) };
				data[0].insert(value);
				put_property(data);
			}
		}
		//===============================================================
		//===============================================================
		public String toString()
		{
			return "Class: " + name;
		}
	}
		
	//===============================================================
	/**
	 *	Class to define TANGO property object
	 */
	//===============================================================
	public class TgProperty
	{
		public String	objname;
		public String	src;
		public String	name;
		public String	desc;
		public String	def_value;
		public String	db_value = null;;
		
		//===============================================================
		public TgProperty(String objname, String src, String name, String desc, String def_value)
		{
			this.objname = objname;
			this.src     = src;
			this.name    = name;
			this.desc    = desc;
			this.def_value   = "";

			this.def_value = ServArchitectureDialog.multiLine2OneLine(def_value);
		}
		//===============================================================
		//===============================================================
		public void setDbValue(String[] values)
		{
			db_value = "";
			for (int i=0 ; i<values.length ; i++)
			{
				db_value += values[i];
				if (i<(values.length-1))
					db_value += ", ";
			}
		}

		//===============================================================
		//===============================================================
		public String getValue()
		{
			if (db_value==null)
				return def_value;
			else
				return db_value;
		}
		//===============================================================
		//===============================================================
		public String toString(boolean verbose)
		{
			if (verbose)
				return src + ": " + objname + "/" + name + " : \n" +
						desc   + "\n"+
						"    default  value:  " + def_value + "\n" +
						"    database value:  " + db_value;
			else
				return toString();
		}
		//===============================================================
		//===============================================================
		public String toString()
		{
			return name;
		}
	}














	//===============================================================
	/**
	 *	Renderer Class
	 */
	//===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer
	{
			private ImageIcon	root_icon;
			private ImageIcon	class_icon;
			private ImageIcon	prop_icon;
			private ImageIcon	leaf_icon;
			private Font[]		fonts;

			private final int	TITLE      = 0;
			private final int	CLASS_DEV  = 1;
			private final int	PROP_NAME  = 2;
			private final int	PROP_DESC  = 3;

		//===============================================================
		//===============================================================
		public TangoRenderer()
		{
			root_icon = new ImageIcon(getClass().getResource(AstorDefs.img_path + "network5.gif"));

			class_icon = new ImageIcon(getClass().getResource(AstorDefs.img_path + "class.gif"));
			prop_icon  = new ImageIcon(getClass().getResource(AstorDefs.img_path + "attleaf.gif"));
			leaf_icon  = new ImageIcon(getClass().getResource(AstorDefs.img_path + "uleaf.gif"));

			fonts = new Font[4];
			fonts[TITLE]     = new Font("helvetica", Font.BOLD, 18);
			fonts[CLASS_DEV] = new Font("helvetica", Font.BOLD, 16);
			fonts[PROP_NAME] = new Font("helvetica", Font.PLAIN, 12);
			fonts[PROP_DESC] = new Font("helvetica", Font.PLAIN, 10);
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
			String	tip = null;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
			Object	user_obj = node.getUserObject();
			if (row==0)
			{
				//	ROOT
				setIcon(root_icon);
				setFont(fonts[TITLE]);
				tip = ((TgServer)user_obj).desc;
			}
			else
			{
				if (user_obj instanceof TgClass)
				{
					setIcon(class_icon);
					setFont(fonts[CLASS_DEV]);
					tip = ((TgClass)user_obj).desc;
				}
				else
				if (user_obj instanceof TgDevice)
				{
					setIcon(class_icon);
					setFont(fonts[CLASS_DEV]);
				}
				else
				if (user_obj instanceof TgProperty)
				{
					//	Property name
					setIcon(prop_icon);
					setFont(fonts[PROP_NAME]);
					tip = ((TgProperty)user_obj).desc;
				}
				else
				if (user_obj instanceof String)
				{
					//	Property desc and value
					setIcon(leaf_icon);
					setFont(fonts[PROP_DESC]);
				}
			}
			setToolTipText(tip);
            return this;
		}
	}
	//===============================================================
	//===============================================================










	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String args[]) {
		String		servname = "PowerSupply/pv";
		if (args.length>0)
			servname = args[0];
		try
		{
			//new ServArchitectureDialog(new javax.swing.JDialog(), servname).show();
			new ServArchitectureDialog(new javax.swing.JDialog(), "dserver/"+servname).setVisible(true);
		}
		catch(DevFailed e)
		{
			app_util.PopupError.show(new javax.swing.JDialog(), e);
			System.exit(0);
		}

	}
}
