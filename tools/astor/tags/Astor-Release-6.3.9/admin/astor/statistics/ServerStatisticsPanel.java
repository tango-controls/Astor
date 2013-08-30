//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author: verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,
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


package admin.astor.statistics;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import java.awt.*;


//===============================================================

/**
 * JDialog Class to display info
 *
 * @author Pascal Verdier
 */
//===============================================================


public class ServerStatisticsPanel extends JDialog {
    private Window parent;
    private ServerStatisticsTable statisticsTable;
    //===============================================================

    /**
     * Creates new form ServerStatisticsPanel
     *
     * @param parent     JFrame parent instance
     * @param title      dialog title.
     * @param serverStat specified server statistics
     */
    //===============================================================
    public ServerStatisticsPanel(JFrame parent, String title, ServerStat serverStat) {
        super(parent, true);
        this.parent = parent;
        initComponents();
        buildForm(serverStat);
        titleLabel.setText(title);
    }
    //===============================================================

    /**
     * Creates new form ServerStatisticsPanel
     *
     * @param parent     JDialog parent instance
     * @param title      dialog title.
     * @param serverStat specified server statistics
     */
    //===============================================================
    public ServerStatisticsPanel(JDialog parent, String title, ServerStat serverStat) {
        super(parent, true);
        this.parent = parent;
        initComponents();
        buildForm(serverStat);
        titleLabel.setText(title);
    }

    //===============================================================
    //===============================================================
    private void buildForm(ServerStat serverStat) {
        //  Build the server failed list and display it in a table
        statisticsTable = new ServerStatisticsTable(serverStat);

        //  Put it in a scrolled pane.
        JScrollPane scp = new JScrollPane();
        scp.setPreferredSize(new Dimension(
                statisticsTable.getDefaultWidth(), statisticsTable.getDefaultHeight()));
        scp.setViewportView(statisticsTable);
        getContentPane().add(scp, BorderLayout.CENTER);

        pack();
        ATKGraphicsUtils.centerDialog(this);
    }

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

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        filterBtn = new javax.swing.JRadioButton();
        javax.swing.JLabel dummyLbl = new javax.swing.JLabel();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18));
        titleLabel.setText("Dialog Title");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        filterBtn.setSelected(true);
        filterBtn.setText("Display Running ones");
        filterBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(filterBtn);

        dummyLbl.setText("                                 ");
        bottomPanel.add(dummyLbl);

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
    @SuppressWarnings({"UnusedDeclaration"})
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        doClose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void filterBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterBtnActionPerformed
        boolean b = filterBtn.getSelectedObjects() != null;
        statisticsTable.setFilterOnRunning(b);
    }//GEN-LAST:event_filterBtnActionPerformed
    //===============================================================

    /**
     * Closes the dialog
     */
    //===============================================================
    private void doClose() {
        setVisible(false);
        dispose();
        if (parent == null)
            System.exit(0);
    }

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton filterBtn;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    //===============================================================
}
