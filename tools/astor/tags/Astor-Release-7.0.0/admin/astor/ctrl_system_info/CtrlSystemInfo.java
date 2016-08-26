//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009
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
// $Log:  $
//
//-======================================================================

package admin.astor.ctrl_system_info;

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;


//===============================================================
/**
 *	JDialog Class to display control system info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class CtrlSystemInfo extends JDialog {

    private JFrame	parent;
    private List<JRadioButton> buttons = new ArrayList<>();
    private List<HostCollection> collectionList;
    private ScanningThread  scanningThread;
    private Monitor monitor;
    private String controlSystemName;
    //===============================================================
    /**
     *	Creates new form CtrlSystemInfo
     */
    //===============================================================
    public CtrlSystemInfo(JFrame parent) throws DevFailed {
        super(parent, true);
        this.parent = parent;
        initComponents();
        controlSystemName = Utils.getControlSystemName();
        if (controlSystemName ==null)
            controlSystemName = Utils.getTangoHost();
        titleLabel.setText(controlSystemName + " Control System");
        textScrollPane.setVisible(false);
        
        buildCollectionButtons();

        pack();
        ATKGraphicsUtils.centerDialog(this);
        setVisible(true);
    }

    //===============================================================
    //===============================================================
    private void buildCollectionButtons() throws DevFailed {
        collectionList = getCollectionList();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        int y = 5;
        for (HostCollection collection : collectionList) {
            gbc.gridy = y++;
            JRadioButton button = new JRadioButton(collection.getName());
            button.setSelected(true);
            collectionPanel.add(button, gbc);
            buttons.add(button);
        }
        gbc.gridy = y;
        collectionPanel.add(new JLabel("   "), gbc);
    }
    //===============================================================
    //===============================================================
    private List<HostCollection> getCollectionList() throws DevFailed {
        String[]    hostNames = Utils.getHostControlledList();
        List<HostCollection>   list = new ArrayList<>();
        for (String hostName : hostNames) {
            DbDatum datum = new DeviceProxy(
                    AstorUtil.getStarterDeviceHeader()+hostName).get_property("HostCollection");
            String name;
            if (datum.is_empty())
                name = "Miscellaneous";
            else
                name = datum.extractString();
            //  Check if already exists
            HostCollection hostCollection = null;
            for (HostCollection collection : list) {
                if (collection.getName().equals(name)) {
                    hostCollection = collection;
                }
            }
            if (hostCollection==null) { //  Does not exist
                hostCollection = new HostCollection(name);
                list.add(hostCollection);
            }
            hostCollection.add(hostName);
        }
        Collections.sort(list, new HostCollectionComparator());

        String[] lastNames = Utils.getLastCollectionList();
        if (lastNames!=null && lastNames.length>0) {
            List<HostCollection> lastCollections = new ArrayList<>();
            for (HostCollection hostCollection : list) {
                for (String lastName : lastNames) {
                    if (hostCollection.getName().equals(lastName)) {
                        lastCollections.add(hostCollection);
                    }
                }
            }
            for (HostCollection hostCollection : lastCollections) {
                list.remove(hostCollection);
                list.add(hostCollection);
            }
        }
        return list;
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
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        separatorLabel = new javax.swing.JLabel();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        javax.swing.JPanel centerPanel = new javax.swing.JPanel();
        textScrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        collectionPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Dialog Title");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        startButton.setText("Start ");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        bottomPanel.add(startButton);

        separatorLabel.setText("              ");
        bottomPanel.add(separatorLabel);

        cancelBtn.setText("Dissmiss");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        centerPanel.setLayout(new java.awt.BorderLayout());

        textScrollPane.setPreferredSize(new java.awt.Dimension(400, 260));

        textArea.setEditable(false);
        textArea.setColumns(20);
        textArea.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        textArea.setRows(5);
        textArea.setTabSize(2);
        textScrollPane.setViewportView(textArea);

        centerPanel.add(textScrollPane, java.awt.BorderLayout.CENTER);

        collectionPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Collections to be checked");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        collectionPanel.add(jLabel1, gridBagConstraints);

        jLabel2.setText("    ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        collectionPanel.add(jLabel2, gridBagConstraints);

        jLabel3.setText("    ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        collectionPanel.add(jLabel3, gridBagConstraints);

        centerPanel.add(collectionPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

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
    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        // TODO add your handling code here:
        ArrayList<String>   hostList = new ArrayList<>();
        for (JRadioButton button : buttons) {
            if (button.isSelected()) {
                String  collectionName = button.getText();
                for (HostCollection hostCollection : collectionList) {
                    if (hostCollection.getName().equals(collectionName)) {
                        for (String hostName : hostCollection)
                            hostList.add(hostName);
                    }
                }
            }
        }

        monitor = new Monitor(this, "Browsing " + controlSystemName + " database for Servers....");
        scanningThread = new ScanningThread(hostList, monitor);
        scanningThread.start();
        new DisplayResults().start();
    }//GEN-LAST:event_startButtonActionPerformed

    //===============================================================
    /**
     *	Closes the dialog
     */
    //===============================================================
    private void doClose() {

        if (parent==null)
            System.exit(0);
        else {
            setVisible(false);
            dispose();
        }
    }

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel collectionPanel;
    private javax.swing.JLabel separatorLabel;
    private javax.swing.JButton startButton;
    private javax.swing.JTextArea textArea;
    private javax.swing.JScrollPane textScrollPane;
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
            new CtrlSystemInfo(null);
        }
        catch(DevFailed e) {
            ErrorPane.showErrorMessage(new Frame(), null, e);
        }
    }
    //======================================================
    //======================================================




    //======================================================
    /**
     * Results will be displayed by this thread
     */
    //======================================================
    private class DisplayResults extends Thread {
        public void run() {

            //  Close dialog and wait for end of thread job
            setVisible(false);
            try { scanningThread.join(); } catch (InterruptedException e) { System.err.println(e.getMessage()); }

            if (!monitor.isCanceled()) {
                //  Close collection part
                collectionPanel.setVisible(false);
                separatorLabel.setVisible(false);
                startButton.setVisible(false);

                //  Get and display thread results
                textArea.setText(scanningThread.getResults());
                textScrollPane.setVisible(true);
                pack();
            }
            setVisible(true);
        }
    }
    //======================================================
    //======================================================




    //======================================================
    /**
     * Comparators class to sort collection
     */
    //======================================================
    class HostCollectionComparator implements Comparator<HostCollection> {
        public int compare(HostCollection hostCollection1, HostCollection hostCollection2) {

            return hostCollection1.getName().compareTo(hostCollection2.getName());
        }
    }

}