//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013
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
// $Revision:  $
//
//-======================================================================

package admin.astor.tools;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.util.ArrayList;


//===============================================================
/**
 *	JDialog Class to display server structure from database
 *		as a JTree and gives the possibility to export
 *		this structure to another database.
 *
 *	@author  Pascal Verdier
 */
//===============================================================


public class Server2TangoHost extends JDialog {

	private Component	parent;
    private DbServerStructure   server;
	//===============================================================
	/*
	 *	Creates new form Server2TangoHost
	 */
	//===============================================================
	public Server2TangoHost(JFrame parent, String serverName) throws DevFailed {
		super(parent, true);
		this.parent = parent;
        buildTheForm(serverName);
    }
	//===============================================================
	/*
	 *	Creates new form Server2TangoHost
	 */
	//===============================================================
	public Server2TangoHost(JDialog parent, String serverName) throws DevFailed {
		super(parent, true);
		this.parent = parent;
        buildTheForm(serverName);
    }
    //===============================================================
	/*
	 *	Creates new form Server2TangoHost
	 */
    //===============================================================
    private void buildTheForm(String serverName) throws DevFailed {
		initComponents();

        server = new DbServerStructure(serverName);
		initOwnComponents(server);

		titleLabel.setText("Server " + serverName + " on " + ApiUtil.get_db_obj().get_tango_host());
		pack();
 		ATKGraphicsUtils.centerDialog(this);
	}
	//===============================================================
	//===============================================================
	private void initOwnComponents(DbServerStructure server) throws DevFailed
	{
         //	Build users_tree to display info
		DbServerTree	tree = new DbServerTree(server);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(tree);
		scrollPane.setPreferredSize(new Dimension(700, 600));
		getContentPane().add(scrollPane, BorderLayout.CENTER);
	}

	//===============================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//===============================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton exportBtn = new javax.swing.JButton();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Dialog Title");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        exportBtn.setText("Export Server");
        exportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(exportBtn);

        jLabel1.setText("         ");
        bottomPanel.add(jLabel1);

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
	@SuppressWarnings("UnusedParameters")
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed

	//===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		doClose();
	}//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void exportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportBtnActionPerformed
        try {
            //  Get the tango host to export
            String  tgHost = getTangoHostToExport(server.getName());
            if (tgHost==null) return;

            //  TANGO_HOST is OK
            //DbServerStructure   server = serverDialog.getServer();
            int option = checkIfServerAlreadyExists(server, tgHost);
            if (option==JOptionPane.CANCEL_OPTION)
                return;
            //  IF OK remove it before
            if (option==JOptionPane.OK_OPTION) {
                server.remove(tgHost);
            }
            server.putInDatabase(tgHost);

            //	Ask to remove original one
            if (JOptionPane.showConfirmDialog(this,
                    server.getName() + "  server\nHas been exported to " + tgHost + " \n\n" +
                            "Remove it  in  " + ApiUtil.getTangoHost() + " ?\n",
                    "Confirm Dialog",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                removeServer();
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
        doClose();
    }//GEN-LAST:event_exportBtnActionPerformed

    //===============================================================
    //===============================================================
    private void removeServer() throws DevFailed {

        //  Check if running
        DeviceProxy dev = new DeviceProxy("dserver/"+server.getName());
        try {
            dev.ping();
            //	Ask to stop it
            if (JOptionPane.showConfirmDialog(this,
                    server.getName() + "  is running!\n" +
                            "Stop it before remove it  in  " + ApiUtil.getTangoHost() + " ?\n",
                    "Confirm Dialog",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                 dev.command_inout("Kill");
                 try { Thread.sleep(1000); } catch (InterruptedException e) { /* */ }
            }
            else
                return;

        }
        catch (DevFailed e) {
            if (JOptionPane.showConfirmDialog(this,
                    "Delete  " + server.getName() + "  in  " + ApiUtil.getTangoHost() + " ?\n",
                    "Confirm Dialog",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }
        }

        // Set as not controlled.
        new DbServer(server.getName()).put_info(
                new DbServInfo(server.getName(), "", false, 0));
        //Then remove it
        server.remove();
    }
    //===============================================================
    //===============================================================
    private int checkIfServerAlreadyExists(DbServerStructure server, String tgHost) throws DevFailed{

        if (server.alreadyExists(tgHost)) {
            Object[] options = {"Write " + tgHost + " nevertheless", "Cancel"};
            switch (JOptionPane.showOptionDialog(this,
                    "WARNING:\n"+
                    server.getName() + " or device(s)  already exists in "  + tgHost + " !\n\n",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, options, options[0])) {
                case 0:    //	Remove and Continue
                    return JOptionPane.OK_OPTION;

                case 1:    // Discard
                case -1:   //	escape
                    return JOptionPane.CANCEL_OPTION;
            }
        }
        //  Continue
        return JOptionPane.NO_OPTION;
    }
    //===============================================================
    //===============================================================
    private String getTangoHostToExport(String serverName) {
        String tgHost = "";
        boolean tgHostOK = false;
        while (tgHost!=null && !tgHostOK) {
            tgHost = (String) JOptionPane.showInputDialog(this,
                    "TANGO_HOST to export " + serverName + "  ?",
                    "Input Dialog",
                    JOptionPane.INFORMATION_MESSAGE,
                    null, null, tgHost);
            if (tgHost!=null) {

                try {
                    //  Check if not default Tango host
                    if (tgHost.equals(ApiUtil.getTangoHost())) {
                        Except.throw_exception("",
                                "Cannot copy server on itself",
                                "Server2TangoHost.getTangoHostToExport()");
                    }
                    //  Check tango host connection
                    ApiUtil.get_db_obj(tgHost);
                    tgHostOK=true;
                }
                catch (DevFailed e) {
                    ErrorPane.showErrorMessage(this, null, e);
                }
            }
        }
        return tgHost;
    }
	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void doClose() {

        if (parent==null)
            System.exit(0);
		setVisible(false);
		dispose();
	}

	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//===============================================================




	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String args[]) {
		try {
            if (args.length==0)
                Except.throw_exception("SyntaxError", "ServerName ?", "main()");
			new Server2TangoHost((JFrame)null, args[0]).setVisible(true);
		}
		catch(DevFailed e) {
            ErrorPane.showErrorMessage(new Frame(), null, e);
            System.exit(0);
		}
	}






    //===============================================================
    /**
     * A JTree class to display structure
     */
    //===============================================================
    public class DbServerTree extends JTree implements TangoConst {

        private DefaultMutableTreeNode root;

        //===============================================================
        //===============================================================
        public DbServerTree(DbServerStructure server) throws DevFailed {
            super();
            buildTree(server);
            expandChildren(root);
            setSelectionPath(null);
        }

        //===============================================================
        //===============================================================
        private void buildTree(DbServerStructure server) {
            //  Create the nodes.
            root = new DefaultMutableTreeNode(server);
            createCollectionClassNodes(server);

            //	Create the tree that allows one selection at a time.
            getSelectionModel().setSelectionMode
                    (TreeSelectionModel.SINGLE_TREE_SELECTION);

            //	Create Tree and Tree model
            DefaultTreeModel treeModel = new DefaultTreeModel(root);
            setModel(treeModel);

            //Enable tool tips.
            ToolTipManager.sharedInstance().registerComponent(this);

            //  Set the icon for leaf nodes.
            TangoRenderer renderer = new TangoRenderer();
            setCellRenderer(renderer);

            //	Listen for collapse tree
            addTreeExpansionListener(new TreeExpansionListener() {
                public void treeCollapsed(TreeExpansionEvent e) {
                    //collapsedPerfomed(e);
                }

                public void treeExpanded(TreeExpansionEvent e) {
                    expandedPerformed(e);
                }
            });
            //	Add Action listener
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    treeMouseClicked(evt);    //	for tree clicked, menu,...
                }
            });
        }
        //======================================================
        /*
         * Manage event on clicked mouse on JTree object.
        */
        //======================================================
        private void treeMouseClicked(@SuppressWarnings("UnusedParameters") java.awt.event.MouseEvent evt) {
        }
        //===============================================================
        public void expandedPerformed(@SuppressWarnings("UnusedParameters") TreeExpansionEvent evt) {
        }
        //===============================================================
        private void createCollectionClassNodes(DbServerStructure server) {
            ArrayList<DbServerStructure.TangoClass> classes = server.getClasses();
            //  Build class nodes
            for (DbServerStructure.TangoClass clazz : classes) {
                DefaultMutableTreeNode classNode =
                        new DefaultMutableTreeNode(clazz);
                root.add(classNode);

                // Build class property nodes
                ArrayList<DbServerStructure.TangoProperty> properties = clazz.getProperties();
                for (DbServerStructure.TangoProperty property : properties)
                    classNode.add(new DefaultMutableTreeNode(property));

                // Build class attribute nodes
                ArrayList<DbServerStructure.TangoAttribute> attributes = clazz.getAttributes();
                for (DbServerStructure.TangoAttribute attribute : attributes) {
                    DefaultMutableTreeNode attributeNode = new DefaultMutableTreeNode(attribute);
                    classNode.add(attributeNode);
                    for (DbServerStructure.TangoProperty property : attribute)
                        attributeNode.add(new DefaultMutableTreeNode(property));
                }

                //  Build device nodes
                for (DbServerStructure.TangoDevice device : clazz) {
                    DefaultMutableTreeNode deviceNode = new DefaultMutableTreeNode(device);
                    classNode.add(deviceNode);

                    // Build device property nodes
                    ArrayList<DbServerStructure.TangoProperty> deviceProperties = device.getProperties();
                    for (DbServerStructure.TangoProperty property : deviceProperties)
                        deviceNode.add(new DefaultMutableTreeNode(property));

                    // Build device attribute nodes
                    ArrayList<DbServerStructure.TangoAttribute> deviceAttributes = device.getAttributes();
                    for (DbServerStructure.TangoAttribute attribute : deviceAttributes) {
                        DefaultMutableTreeNode attributeNode = new DefaultMutableTreeNode(attribute);
                        deviceNode.add(attributeNode);
                        for (DbServerStructure.TangoProperty property : attribute)
                            attributeNode.add(new DefaultMutableTreeNode(property));
                    }
                }
            }
        }
        //===============================================================
        private void expandChildren(DefaultMutableTreeNode node) {
            boolean level_done = false;
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode child =
                        (DefaultMutableTreeNode) node.getChildAt(i);
                if (child.isLeaf()) {
                    if (!level_done) {
                        expandNode(child);
                        level_done = true;
                    }
                } else
                if (! (child.getUserObject() instanceof DbServerStructure.TangoDevice)) {
                    expandChildren(child);
                }
            }
        }
        //===============================================================
        private void expandNode(DefaultMutableTreeNode node) {
            ArrayList<DefaultMutableTreeNode> nodeList = new ArrayList<DefaultMutableTreeNode>();
            nodeList.add(node);
            while (node != root) {
                node = (DefaultMutableTreeNode) node.getParent();
                nodeList.add(0, node);
            }
            TreeNode[] tn = new DefaultMutableTreeNode[nodeList.size()];
            for (int i = 0; i < nodeList.size(); i++)
                tn[i] =  nodeList.get(i);
            TreePath tp = new TreePath(tn);
            setSelectionPath(tp);
            scrollPathToVisible(tp);
        }
        //===============================================================
    }
    //===============================================================
    //  End of DbServerTree class
    //===============================================================





    private static final Font[] fonts = new Font[] {
            new Font("Dialog", Font.BOLD, 18),
            new Font("Dialog", Font.BOLD, 14),
            new Font("Dialog", Font.BOLD, 12),
            new Font("Dialog", Font.PLAIN, 12),
            new Font("Dialog", Font.PLAIN, 11),
    };
    private static final int SERVER = 0;
    private static final int CLASS  = 1;
    private static final int DEVICE = 2;
    private static final int ATTRIB = 3;
    private static final int DATA   = 4;
    //===============================================================
    /**
     * Renderer Class
     */
    //===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer {
        private ImageIcon[] icons;
        //===============================================================
        //===============================================================
        public TangoRenderer() {
            icons = new ImageIcon[DATA+1];
            icons[SERVER] = Utils.getInstance().getIcon("server.gif");
            icons[CLASS]  = Utils.getInstance().getIcon("class.gif");
            icons[DEVICE] = Utils.getInstance().getIcon("device.gif");
            icons[ATTRIB] = Utils.getInstance().getIcon("attleaf.gif");
            icons[DATA]   = Utils.getInstance().getIcon("leaf.gif");
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

            setForeground(Color.black);
            setBackgroundSelectionColor(Color.lightGray);
            int	idx;
            Object  userObject = ((DefaultMutableTreeNode) obj).getUserObject();
            if (userObject instanceof DbServerStructure) {
                idx = SERVER;
            }
            else
            if (userObject instanceof DbServerStructure.TangoClass) {
                idx = CLASS;
            }
            else
            if (userObject instanceof DbServerStructure.TangoDevice) {
                idx = DEVICE;
            }
            else
            if (userObject instanceof DbServerStructure.TangoAttribute) {
                idx = ATTRIB;
            }
            else {
                idx = DATA;
            }
            setFont(fonts[idx]);
            setIcon(icons[idx]);
            return this;
        }
    }//	End of Renderer Class
    //==============================================================================
    //  End fo Renderer class
    //==============================================================================
}
