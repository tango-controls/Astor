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
// Revision 1.1  2006/09/19 13:06:47  pascal_verdier
// Access control manager added.
//
//
// copyleft 1996 by European Synchrotron Radiation Facility, Grenoble, France
//							 All Rights Reversed
//-======================================================================

package admin.astor.access;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.TangoApi.ApiUtil;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Vector;


public class  UsersTree  extends JTree implements TangoConst
{
    static final int			USER       = -2;
    static final int			COLLECTION = -1;
    static final int			ADDRESS    = 0;
    static final int			DEVICE     = 1;
    static final String[]		collecStr = { "Allowed Addresses", "Devices" };


    static final int			WRITE   = 0;
    static final int			READ    = 1;
    static final String[]		rightsStr = { "write", "read" };

    private static final String	img_path = "/app_util/img/";
    static ImageIcon	tango_icon;
    static ImageIcon	user_icon;
    static ImageIcon	add_icon;
    static ImageIcon	dev_icon;
    static ImageIcon	write_icon;
    static ImageIcon	read_icon;



    private DefaultTreeModel	    treeModel;
    private DefaultMutableTreeNode  root;
    private TreePopupMenu	        menu;
    private JFrame 	                parent;

    private AccessProxy	access_dev;
    CopiedAddresses copied_addresses = new CopiedAddresses();
    CopiedDevices   copied_devices   = new CopiedDevices();

    //===============================================================
    //===============================================================
    public UsersTree(JFrame parent, AccessProxy access_dev) throws DevFailed
    {
        super();
        this.parent     = parent;
        this.access_dev = access_dev;
        buildTree();
        menu = new TreePopupMenu(this);
     }
    //===============================================================
    //===============================================================
    private void buildTree() throws DevFailed
    {
        String  str_root = "Tango Control Access";
        try
        {
                str_root = ApiUtil.get_db_obj().get_tango_host() + "  Control Access";
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
        //	Check if click is on a node
        if (getRowForLocation(evt.getX(), evt.getY())<1)
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
            if (o instanceof AcAddress ||
                o instanceof AcDevice)
            editItem();
        }
        else
        if ((mask & MouseEvent.BUTTON3_MASK)!=0)
        {
             if (o instanceof AcAddress)
                menu.showMenu(evt, ADDRESS, o);
            else
            if (o instanceof AcDevice)
                menu.showMenu(evt, DEVICE, o);
            else
            if (o instanceof String)
            {
                switch(selectedPath.getPath().length)
                {
    //        if (o.toString().equals(collecStr[ADDRESS])  ||
    //            o.toString().equals(collecStr[DEVICE]) )
                case 2:
                    menu.showMenu(evt, USER, o);
                    break;
                case 3:
                    menu.showMenu(evt, COLLECTION, o);
                    break;
                }
            }
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
        if (path.length<3)
            return;

        //	Get concerned node
        DefaultMutableTreeNode	node =
            (DefaultMutableTreeNode)tp.getPathComponent(path.length-1);

        switch (path.length)
        {
        case 3:
            if (node.toString().equals(collecStr[ADDRESS]))
                createAddressNodes(node);
            else
                createDeviceNodes(node);
        }
    }
    //===============================================================
    //===============================================================
    private void createDeviceNodes(DefaultMutableTreeNode node)
    {
        try
        {
            DefaultMutableTreeNode	d_node;
            String		user = node.getParent().toString();
            AcDevice[]	devices = getDevices(user);

            //  Check if something has changed.
            //if (!createChildNodes(node, devices))
            //    return;

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
            String		user = node.getParent().toString();
            String[]	addresses =
                    access_dev.getAddressesByUser(user);

            //  Check if something has changed.
            if (!createChildNodes(node, addresses))
                return;

            for (int i=0 ; i<addresses.length ; i++)
            {
                //	Create a node for addresses
                a_node = new DefaultMutableTreeNode(new AcAddress(addresses[i]));
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

        u_node =  new DefaultMutableTreeNode(name);
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
    private Vector  v_users = new Vector();
    private void createUserNodes() throws DevFailed
    {
        //  Get users list
        //  and store it in a vector for later usage
        String[]	users = access_dev.getUsers();
        for (int i=0 ; i<users.length ; i++)
            v_users.add(users[i]);

        for (int i=0 ; i<users.length ; i++)
            createUserNode(users[i]);
    }
    //===============================================================
    //===============================================================
    private boolean createChildNodes(DefaultMutableTreeNode node, String[] str)
    {
        boolean create = false;
        if (node.getChildCount() != str.length)
            create = true;
        else
        for (int i=0 ; i<str.length ; i++)
            if (!node.getChildAt(i).toString().equals(str[i]))
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
    private AcDevice[] getDevices(String user) throws DevFailed
    {
        String[]    result = access_dev.getDevicesByUser(user);

        UsersTree.AcDevice[]	ret = new UsersTree.AcDevice[result.length/2];
        for (int i=0 ; i<result.length/2 ; i++)
            ret[i] = new UsersTree.AcDevice(result[2*i], result[2*i+1]);
        return ret;
    }
    //======================================================
    //======================================================
    DefaultMutableTreeNode getSelectedNode()
    {
        return (DefaultMutableTreeNode) getLastSelectedPathComponent();
    }



//===============================================================
//
//	Editing Tree (Add, remove...)
//
//===============================================================
    //===============================================================
    //===============================================================
    void addUserNode(String[] str)
    {
        String  user    = str[EditDialog.USER];
        String  address = str[EditDialog.ADDRESS];
        String  devname = "*/*/*";

        //  Check if user already exists
        TreePath    tp = null;
        boolean already_exists = false;
        for (int i=0 ; !already_exists && i<v_users.size() ; i++)
            if ((v_users.get(i)).equals(user))
            {
                already_exists = true;
                DefaultMutableTreeNode  user_node =
                            (DefaultMutableTreeNode)root.getChildAt(i);
                //  expand address nodes
                TreeNode[]  path = new TreeNode[4];
                path[0] = root;
                path[1] = user_node;
                path[2] = user_node.getChildAt(0);
                path[3] = user_node.getChildAt(0).getChildAt(0);
                tp = new TreePath(path);
                setSelectionPath(tp);

                //  Select user node.
                path = new TreeNode[2];
                path[0] = root;
                path[1] = user_node;
                tp = new TreePath(path);
                setSelectionPath(tp);
                app_util.PopupError.show(parent, "Üser  "+ user + "  Already exists !");
            }


        //  If does not already exist, create it
        if (!already_exists)
        {
            try
            {
                access_dev.addAddress(user, address);
                access_dev.addDevice(user, devname, rightsStr[WRITE]);
            }
            catch(DevFailed e)
            {
                    ErrorPane.showErrorMessage(parent,
                        "Error during Database access", e);
            }

            TreeNode[]  path = new TreeNode[4];

            DefaultMutableTreeNode  new_user_node =
                      new DefaultMutableTreeNode(user);
            DefaultMutableTreeNode  new_str_add_node =
                      new DefaultMutableTreeNode(collecStr[ADDRESS]);
            DefaultMutableTreeNode  new_str_dev_node =
                      new DefaultMutableTreeNode(collecStr[DEVICE]);
            DefaultMutableTreeNode  new_add_node =
                      new DefaultMutableTreeNode(new AcAddress(address));
            DefaultMutableTreeNode  new_dev_node =
                      new DefaultMutableTreeNode(new AcDevice(devname, WRITE));

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
            new_node = new DefaultMutableTreeNode(new AcAddress("*.*.*.*"));
            treeModel.insertNodeInto(new_node, node, node.getChildCount());
            path[3] = new_node;
            tp = new TreePath(path);
            setSelectionPath(tp);
            scrollPathToVisible(tp);
            break;
        case DEVICE:
            //	Create a node for devices
            new_node = new DefaultMutableTreeNode(new AcDevice("*/*/*", WRITE));
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
        if (o instanceof AcDevice)
            obj_type = DEVICE;
        else
            obj_type = ADDRESS;

        //	Build the inside dialog
        edit_item = new EditTreeItem(parent, this, node.toString(), obj_type);
        if (edit_item.showDlg())
        {
            String	user = node.getParent().getParent().toString();
            String	new_name = edit_item.getInputs();
            try
            {
                switch(obj_type)
                {
                case ADDRESS:
                    AcAddress	add = (AcAddress)o;
                    access_dev.removeAddress(user, add.name);
                    access_dev.addAddress(user, new_name);
                    add.name = new_name;
                    rebuildNode(node, add);
                    break;
                case DEVICE:
                    AcDevice	dev = (AcDevice)o;
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
    void removeItem()
    {
        DefaultMutableTreeNode	node = getSelectedNode();
        Object		o = node.getUserObject();
        int			obj_type;
        String      message;
        String      user;
        if (o instanceof AcDevice)
        {
            user = node.getParent().getParent().toString();
            message = "Are you sure to want to remove this record";
            obj_type = DEVICE;
        }
        else
        if (o instanceof AcAddress)
        {
            user = node.getParent().getParent().toString();
            message = "Are you sure to want to remove this record";
            obj_type = ADDRESS;
        }
        else
        {
            user = o.toString();
            obj_type = USER;
            message = "Are you sure to want to remove all records for " + o;
        }

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
                AcAddress	add = (AcAddress)o;
                access_dev.removeAddress(user, add.name);
                break;
            case DEVICE:
                AcDevice	dev = (AcDevice)o;
                access_dev.removeDevice(user, dev.name, rightsStr[dev.right]);
                break;
            case USER:
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
    void cloneUser()
    {
        DefaultMutableTreeNode	node = getSelectedNode();
        Object		o = node.getUserObject();
        if (o instanceof String)
        {
            String  src_user = o.toString();
            String  new_user =(String) JOptionPane.showInputDialog(parent,
                                    "New User name  ?",
                                    "Clone " + src_user + "  ?",
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null, null, "");
            if (new_user==null)
                return;
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
            DefaultMutableTreeNode  u_node = new DefaultMutableTreeNode(new_user);
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
                 new_node = new DefaultMutableTreeNode(new AcAddress(address));
                treeModel.insertNodeInto(new_node, node,  node.getChildCount());
                break;
            case DEVICE:
                String  devname = copied_devices.deviceAt(0).name;
                int     right   = copied_devices.deviceAt(0).right;
                access_dev.addDevice(user, devname, rightsStr[right]);
                new_node = new DefaultMutableTreeNode(new AcDevice(devname, right));
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
        Object		o = node.getUserObject();
        int			obj_type;
        if (o instanceof AcDevice)
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
        DefaultMutableTreeNode	node = getSelectedNode();
        Object		o = node.getUserObject();

        String	user = node.getParent().getParent().toString();
        try
        {
            AcDevice	dev = (AcDevice)o;
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
        private final int	USER      = 1;
        private final int	COLLEC    = 2;
        private final int	LEAF      = 3;

        //===============================================================
        //===============================================================
        public TangoRenderer()
        {
            tango_icon = new ImageIcon(getClass().getResource(img_path + "network5.gif"));
            user_icon  = new ImageIcon(getClass().getResource(img_path + "user.gif"));
            add_icon   = new ImageIcon(getClass().getResource(img_path + "server.gif"));
            dev_icon   = new ImageIcon(getClass().getResource(img_path + "device.gif"));
            write_icon = new ImageIcon(getClass().getResource(img_path + "greenbal.gif"));
            read_icon  = new ImageIcon(getClass().getResource(img_path + "redball.gif"));

            fonts = new Font[LEAF+1];
            fonts[TITLE]   = new Font("courrier", Font.BOLD, 18);
            //	width fixed font
            fonts[USER ]   = new Font("Monospaced", Font.BOLD, 14);
            fonts[COLLEC]  = new Font("Monospaced", Font.BOLD, 12);
            fonts[LEAF]    = new Font("Monospaced", Font.PLAIN, 12);
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

            if (row==0)
            {
                //	ROOT
                setFont(fonts[TITLE]);
                setIcon(tango_icon);
            }
            else
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;

                if (node.getUserObject() instanceof String)
                {
                    if (obj.toString().equals(collecStr[ADDRESS]))
                    {
                        setFont(fonts[COLLEC]);
                        setIcon(add_icon);
                    }
                    else
                    if (obj.toString().equals(collecStr[DEVICE]))
                    {
                        setFont(fonts[COLLEC]);
                        setIcon(dev_icon);
                    }
                    else
                    {
                        setIcon(user_icon);
                        setFont(fonts[USER]);
                    }
                }
                else
                if (node.getUserObject() instanceof AcAddress)
                {
                    setFont(fonts[LEAF]);
                    setIcon(add_icon);
                }
                else
                if (node.getUserObject() instanceof AcDevice)
                {
                    setFont(fonts[LEAF]);
                    AcDevice	dev = (AcDevice)node.getUserObject();
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
    class AcAddress
    {
        String	name;
        public AcAddress(String add) {
            name  = add;
        }
        public String toString() { return name; }
    }
    //===============================================================
    public class AcDevice
    {
        String	name;
        int 	right = READ;
        public AcDevice(String add, String r) {
            name  = add;
            for (int i=0 ; i<rightsStr.length ; i++)
                if (rightsStr[i].equals(r))
                    right = i;
        }
        public AcDevice(String add, int r) {
            name  = add;
            right = r;
        }
        public String toString() { return name; }
    }
    //===============================================================
    class Dummy
    {
        //	notthing
        public String toString() { return ""; }
    }

    //===============================================================
    class CopiedDevices extends Vector
    {
        public AcDevice deviceAt(int i)
        {
            return (AcDevice)get(i);
        }
    }
    //===============================================================
    class CopiedAddresses extends Vector
    {
        public AcAddress addressAt(int i)
        {
            return (AcAddress)get(i);
        }
    }

}
