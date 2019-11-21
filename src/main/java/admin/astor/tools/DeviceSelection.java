//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,......,2018
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
//-======================================================================

package admin.astor.tools;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;

//===============================================================

/**
 *	JDialog Class to display devices
 *	sorted by class and to select one of them.
 *
 *	@author  Pascal Verdier
 */
//===============================================================

public class DeviceSelection extends JDialog {
	private Component parent;
	private ServerTree serverTree;
	private String selectedDevice = null;
	private String serverName;
	private boolean withDServer;
	private ImageIcon serverIcon;
	private ImageIcon classIcon;
	private ImageIcon deviceIcon;
	//===============================================================
	//===============================================================
	public DeviceSelection(JFrame parent,
						   String serverName,
						   String[] classDeviceNames, boolean withDServer) throws DevFailed {
		super(parent, true);
		this.parent = parent;
		this.serverName = serverName;
		this.withDServer = withDServer;
		buildTheForm(classDeviceNames);
	}
	//===============================================================
	//===============================================================
	public DeviceSelection(JDialog parent,
						   String serverName,
						   String[] classDeviceNames, boolean withDServer) throws DevFailed {
		super(parent, true);
		this.parent = parent;
		this.serverName = serverName;
		this.withDServer = withDServer;
		buildTheForm(classDeviceNames);
	}
	//===============================================================
	//===============================================================
	private void buildTheForm(String[] classDeviceNames) throws DevFailed {
		initComponents();
		serverIcon = Utils.getInstance().getIcon("server.gif");
		classIcon = Utils.getInstance().getIcon("class.gif");
		deviceIcon = Utils.getInstance().getIcon("device.gif");

		if (classDeviceNames == null)
			classDeviceNames = getClassDevices(serverName);

		//	Build users_tree to display info
		getContentPane().remove(centerPanel);
		serverTree = new ServerTree(serverName, classDeviceNames);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(serverTree);
		scrollPane.setPreferredSize(new Dimension(350, 400));
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		titleLabel.setText("Device Selection by class");
		pack();
		ATKGraphicsUtils.centerDialog(this);
	}
	//===============================================================
	//===============================================================

	//===============================================================
	//===============================================================
	public static String[] getClassDevices(String serverName) throws DevFailed {
		//	Check if device name
		StringTokenizer stk = new StringTokenizer(serverName, "/");
		String deviceName = null;
		switch (stk.countTokens()) {
			case 2:
				deviceName = "dserver/" + serverName;
				break;
			case 3:
				deviceName = serverName;
				break;
			default:
				Except.throw_exception("BadParam", serverName + " not a server name");
		}

		DeviceData argOut = new DeviceProxy(deviceName).command_inout("QueryDevice");
		return argOut.extractStringArray();
	}
	//===============================================================
	//===============================================================


	//===============================================================
	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	//===============================================================
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		JPanel topPanel = new JPanel();
		titleLabel = new JLabel();
		centerPanel = new JPanel();
		JPanel bottomPanel = new JPanel();
		JButton okBtn = new JButton();
		JButton cancelBtn = new JButton();

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		titleLabel.setFont(new Font("Dialog", 1, 18));
		titleLabel.setText("Dialog Title");
		topPanel.add(titleLabel);

		getContentPane().add(topPanel, BorderLayout.NORTH);

		centerPanel.setLayout(new GridBagLayout());
		getContentPane().add(centerPanel, BorderLayout.CENTER);

		okBtn.setText("OK");
		okBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okBtnActionPerformed(evt);
			}
		});
		bottomPanel.add(okBtn);

		cancelBtn.setText("Cancel");
		cancelBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelBtnActionPerformed(evt);
			}
		});
		bottomPanel.add(cancelBtn);

		getContentPane().add(bottomPanel, BorderLayout.SOUTH);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
	@SuppressWarnings("UnusedParameters")
	private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed
		selectedDevice = serverTree.getSelectedDevice();
		if (selectedDevice==null)
			JOptionPane.showMessageDialog(this, "No device selected !");
		else
			doClose();
	}//GEN-LAST:event_okBtnActionPerformed
	//===============================================================
	//===============================================================
	@SuppressWarnings("UnusedParameters")
	private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		selectedDevice = null;
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed
	//===============================================================
	//===============================================================
	@SuppressWarnings("UnusedParameters")
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		selectedDevice = null;
		doClose();
	}//GEN-LAST:event_closeDialog
	//===============================================================
	//===============================================================
	private void doClose() {
		if (parent == null)
			System.exit(0);
		else {
			setVisible(false);
			dispose();
		}
	}
	//===============================================================
	//===============================================================
	public String showDialog() {
		setVisible(true);
		return selectedDevice;
	}
	//===============================================================
	//===============================================================


	//===============================================================
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JPanel centerPanel;
	private JLabel titleLabel;
	// End of variables declaration//GEN-END:variables
	//===============================================================


	//===============================================================
	/**
	 * @param args the command line arguments
	 */
	//===============================================================
	public static void main(String[] args) {
		try {
			String serverName = "VacGaugeServer/sr_c01-pen";
			//String serverName = "Starter/l-c01-1";
			String[] classDeviceNames = DeviceSelection.getClassDevices(serverName);
			if (classDeviceNames.length>1) {
				String deviceName =
						new DeviceSelection((JDialog) null, serverName, classDeviceNames, true).showDialog();
				if (deviceName!=null) {
					System.out.println(deviceName);
				}
				System.exit(0);
			}
			else {
				String deviceName = classDeviceNames[0];
				int idx = deviceName.indexOf("::");
				if (idx>0)
					deviceName = deviceName.substring(idx+2);
				System.out.println(deviceName);
			}
		} catch (DevFailed e) {
			ErrorPane.showErrorMessage(new Frame(), null, e);
			System.exit(0);
		}
	}
	//===============================================================
	//===============================================================




	//===============================================================
	/**
	 * LeafClass object definition
	 */
	//===============================================================
	private class DeviceObject {
		String name;
		//===========================================================
		private DeviceObject(String name) {
			this.name = name;
		}
		//===========================================================
		public String toString() {
			return name;
		}
		//===========================================================
	}
	//===============================================================
	//===============================================================


	//===============================================================
	/**
	 * CollectionClass object definition
	 */
	//===============================================================
	private class ClassObject {
		String name;
		//===========================================================
		private ClassObject(String name) {
			this.name = name;
		}
		//===========================================================
		public String toString() {
			return name;
		}
		//===========================================================
	}
	//===============================================================
	//===============================================================



	//===============================================================
	//===============================================================
	private class ServerTree extends JTree implements TangoConst {
		private DefaultMutableTreeNode rootNode;
		//===========================================================
		private ServerTree(String serverName, String[] classDeviceNames) {
			super();
			//  Create the nodes.
			rootNode = new DefaultMutableTreeNode(serverName);
			createNodes(classDeviceNames);

			//	Create the tree that allows one selection at a time.
			getSelectionModel().setSelectionMode
					(TreeSelectionModel.SINGLE_TREE_SELECTION);

			//	Create Tree and Tree model
			DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
			setModel(treeModel);

			//Enable tool tips.
			ToolTipManager.sharedInstance().registerComponent(this);

			//  Set the icon for leaf nodes.
			TangoRenderer renderer = new TangoRenderer();
			setCellRenderer(renderer);
			//	Add Action listener
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent evt) {
					treeMouseClicked(evt);
				}
			});

			setSelectionPath(null);
		}
		//======================================================
		private void treeMouseClicked(MouseEvent evt) {
			//	Set selection at mouse position
			TreePath selectedPath = getPathForLocation(evt.getX(), evt.getY());
			if (selectedPath == null)
				return;

			DefaultMutableTreeNode node =
					(DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount() - 1);
			Object userObject = node.getUserObject();
			//  Check button clicked
			if (evt.getClickCount() == 2 && evt.getButton()==1) {
				if (userObject instanceof DeviceObject) {
					okBtnActionPerformed(null);
				}
			}
		}
		//===========================================================
		private void createNodes(String[] classDeviceNames) {
			ClassObject _class = null;
			DefaultMutableTreeNode classNode = null;
            for (String str : classDeviceNames) {
                StringTokenizer stk = new StringTokenizer(str, "::");
                if (stk.countTokens() == 2) {
                    String className = stk.nextToken();
                    String deviceName = stk.nextToken();
                    if (_class == null || !className.equals(_class.name)) {
                        _class = new ClassObject(className);
                        classNode = new DefaultMutableTreeNode(_class);
                        rootNode.add(classNode);
                    }
                    classNode.add(new DefaultMutableTreeNode(new DeviceObject(deviceName)));
				}
			}
			if (withDServer) {
				classNode = new DefaultMutableTreeNode(new ClassObject("DServer"));
				rootNode.add(classNode);
				classNode.add(new DefaultMutableTreeNode(
				        new DeviceObject("dserver/"+serverName.toLowerCase())));
			}
		}
		//===========================================================
		private String getSelectedDevice() {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
			if (node==null)
				return null;
			else
				return node.getUserObject().toString();
		}
		//===========================================================
		//===========================================================
	}


		//===============================================================
		/**
		 * Renderer Class
		 */
		//===============================================================
		private class TangoRenderer extends DefaultTreeCellRenderer {
			private final Font rootFont = new Font("Dialog", Font.BOLD, 14);
			private final Font collectionFont = new Font("Dialog", Font.BOLD, 12);
			private final Font leafFont = new Font("Dialog", Font.PLAIN, 12);
			//===========================================================
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
				setForeground(Color.black);
				setBackgroundSelectionColor(Color.lightGray);
				if (row == 0) {
					//	ROOT
					setFont(rootFont);
					setIcon(serverIcon);
				} else {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;

					if (node.getUserObject() instanceof ClassObject) {
						setFont(collectionFont);
						setIcon(classIcon);
					} else {
						setFont(leafFont);
						setIcon(deviceIcon);
					}
				}
				return this;
			}
			//===============================================================
		}//	End of Renderer Class

}