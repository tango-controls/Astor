//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,
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
// $Revision$
//
//-======================================================================


package admin.astor;

import admin.astor.tools.PopupTable;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;


/**
 * Display a Server/Device tree to find host where running.
 *
 * @author verdier
 */
public class DeviceTreeDialog extends javax.swing.JDialog {

    private DeviceTree tree;
    private Astor parent;
    private DeviceTreeDialog dialog;
    private JScrollPane jScrollPane1;
    private JTextArea csInfoLabel;
    private JTextArea infoLabel;
    //========================================================
    /**
     * Creates new form DeviceTreeDialog
     *
     * @param parent the Astor parent instance
     */
    //========================================================
    public DeviceTreeDialog(Astor parent) {
        super(parent, false);
        this.parent = parent;
        this.dialog = this;

        initComponents();

        //	Start a Thread to update monitor during database browsing
        new DisplayMonitor().start();

        pack();
        AstorUtil.rightShiftDialog(this, parent);
    }

    //========================================================
    //========================================================
    private void initComponents() {//GEN-BEGIN:initComponents
        JPanel buttonPanel = new javax.swing.JPanel();
        JButton ctrlServersButton = new javax.swing.JButton();
        JButton cancelButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        csInfoLabel = new javax.swing.JTextArea();
        infoLabel = new javax.swing.JTextArea("   ");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        }
        );

        infoLabel.setEditable(false);
        infoLabel.setColumns(30);
        infoLabel.setRows(10);
        csInfoLabel.setEditable(false);
        JPanel panel = new JPanel(new java.awt.BorderLayout());

        JLabel title = new JLabel("TANGO Control System");
        title.setFont(new Font("helvetica", Font.BOLD, 18));
        panel.add(title, java.awt.BorderLayout.NORTH);

        csInfoLabel.setFont(new Font("helvetica", Font.BOLD, 14));
        panel.add(csInfoLabel, java.awt.BorderLayout.CENTER);
        panel.add(infoLabel, java.awt.BorderLayout.SOUTH);
        getContentPane().add(panel, java.awt.BorderLayout.EAST);

        buttonPanel.setLayout(new java.awt.FlowLayout(FlowLayout.CENTER, 50, 5));

        ctrlServersButton.setText("Not Controlled Servers");
        ctrlServersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ctrlServersButtonActionPerformed(evt);
            }
        }
        );
        buttonPanel.add(ctrlServersButton);
        //	buttonPanel.add (new JLabel("   "));

        cancelButton.setText("Dismiss");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        }
        );
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    //============================================================
    /**
     * Display NOT controlled servers
     */
    //============================================================
    private PopupTable tableDialog = null;

    @SuppressWarnings("UnusedParameters")
    private void ctrlServersButtonActionPerformed(java.awt.event.ActionEvent evt) {

        if (tableDialog == null) {
            try {
                String[][] servers = tree.getNotCtrlServers();
                String title =
                        "" + servers.length + "  NOT controlled servers";
                String[] columns = {"Server Name", "Last exported date"};
                tableDialog = new PopupTable(this, title, columns, servers);
            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
            }
        }
        tableDialog.setVisible(true);
    }

    //============================================================
    //============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    //============================================================
    //============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    //============================================================

    /**
     * Closes the dialog
     */
    //============================================================
    void doClose() {
        setVisible(false);
        dispose();
    }
    //======================================================
    //======================================================



    //===============================================================
    //===============================================================
    class DisplayMonitor extends Thread {

        private Monitor monitor;

        //=======================================================
        //=======================================================
        DisplayMonitor() {
            monitor = new Monitor(parent,
                    "Browsing TANGO database for Servers and Devices....", "");
        }
        //=======================================================

        /**
         * Execute the servers commands.
         */
        //=======================================================
        public void run() {
            //	Build the tree (updates the monitor)
            tree = new DeviceTree(parent, monitor,
                    infoLabel, AstorUtil.getTangoHost());
            if (tree.canceled)
                doClose();
            else {
                //	Show the result
                jScrollPane1.setPreferredSize(new Dimension(280, 400));
                jScrollPane1.setViewportView(tree);
                setTitle("Tree Dialog");
                pack();
                AstorUtil.centerDialog(dialog, parent);
                monitor.setProgressValue(1.1, "Done");
                csInfoLabel.setText(tree.csInfo());
                setVisible(true);
            }
        }
    }//	End of DisplayMonitor thread
}
