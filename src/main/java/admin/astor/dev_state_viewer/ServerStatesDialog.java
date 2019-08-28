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

package admin.astor.dev_state_viewer;

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================

public class ServerStatesDialog extends JDialog {
	private Component parent;
	//===============================================================
	//===============================================================
	public ServerStatesDialog(JFrame parent, String serverName) throws DevFailed {
		super(parent, true);
		buildTheForm(parent, serverName);
	}
	//===============================================================
	//===============================================================
	public ServerStatesDialog(JDialog parent, String serverName) throws DevFailed {
		super(parent, true);
		buildTheForm(parent, serverName);
	}
	//===============================================================
	//===============================================================
	private void buildTheForm(Component parent, String serverName) throws DevFailed {
		try {
			this.parent = parent;
			AstorUtil.startSplash("Build GUI");
			initComponents();

			AstorUtil.increaseSplashProgress(5, "Query database for " + serverName);
			//noinspection MismatchedQueryAndUpdateOfCollection
			TangoServer tangoServer = new TangoServer(serverName);
			if (tangoServer.isEmpty())
				Except.throw_exception("", "No class/device defined for specified server: " + serverName);

			//  Build a tabbed pane to display each class
			JTabbedPane tabbedPane = new JTabbedPane();
			getContentPane().add(tabbedPane, BorderLayout.CENTER);

			AstorUtil.increaseSplashProgress(25, "Building the state viewers");
			//  For each class, display a table with devices (name/state)
			TableViewer classTable = null;
			int nbDevices = 0;
			int index = 0;
			int i = 0;
			List<JScrollPane> scrollPanes = new ArrayList<>();
			for (TangoClass tangoClass : tangoServer) {
				classTable = new TableViewer(tangoClass);
				JScrollPane scrollPane = new JScrollPane(classTable);
				scrollPanes.add(scrollPane);

				tabbedPane.add(scrollPane, tangoClass.getName());
				if (tangoClass.size()>nbDevices) {
					nbDevices = tangoClass.size();
					index = i;
				}
				i++;
			}
			//  Set size
			int height = nbDevices * 15 + 30;
			if (height>800) height = 800;
			for (JScrollPane scrollPane : scrollPanes) {
				scrollPane.setPreferredSize(new Dimension(classTable.getTableWidth(), height));
				tabbedPane.setSelectedIndex(index);
			}

			titleLabel.setText(serverName);
			pack();
			ATKGraphicsUtils.centerDialog(this);
			AstorUtil.stopSplash();
		}
		catch (DevFailed e) {
			AstorUtil.stopSplash();
			throw e;
		}
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

        cancelBtn.setText("Dismiss");
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
	private void doClose() {
		ScalarViewer.attributeList.stopRefresher();
		ScalarViewer.attributeList.clear();
		if (parent==null)
			System.exit(0);
		else {
			setVisible(false);
			dispose();
		}
	}
	//===============================================================
	//===============================================================




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
	public static void main(String[] args) {
		try {
			if (args.length==0) {
				ErrorPane.showErrorMessage(new JFrame(), null, new Exception("Server name ?"));
				System.exit(0);
			}
			new ServerStatesDialog((JFrame) null, args[0]).setVisible(true);
		}
		catch(DevFailed e) {
			ErrorPane.showErrorMessage(new JFrame(), null, e);
			System.exit(0);
		}
	}

}
