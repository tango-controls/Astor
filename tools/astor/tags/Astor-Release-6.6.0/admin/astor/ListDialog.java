//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,
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

/**
 *  A dialog with list used to select servers to be started
 * @author verdier
 */


import admin.astor.tools.Utils;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.IORdump;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import jive.DevWizard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;


//===============================================================
public class ListDialog extends javax.swing.JDialog {
    private static String str_filter = "*";
    private static String previous_item = null;
    private HostInfoDialog hostInfoDialog;
    private ArrayList<String> selectedItems = null;


    //======================================================
    /**
     * Creates new form ListDialog
     * @param hostInfoDialog1 hostInfoDialog dialog instance
     */
    //======================================================
    public ListDialog(HostInfoDialog hostInfoDialog1) {
        super(hostInfoDialog1, true);
        this.hostInfoDialog = hostInfoDialog1;
        initComponents();

        //	fix str filter and add a mouse listener on list
        //---------------------------------------------------
        filterTxt.setText(str_filter);
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                listSelectionPerformed(e);
            }
        };
        jList.addMouseListener(mouseListener);

        pack();
    }
    //======================================================
    //======================================================
    private void setList() throws DevFailed {
        str_filter = filterTxt.getText();
        String[] servlist = ApiUtil.get_db_obj().get_server_list(str_filter);
        jList.setListData(servlist);

        //	Search if previous selection exists
        //----------------------------------------
        for (int i = 0; i < servlist.length; i++)
            if (servlist[i].equals(previous_item))
                jList.setSelectedIndex(i);
    }


    //======================================================
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    //======================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        jList = new javax.swing.JList();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton fromAnotherCrateBtn = new javax.swing.JButton();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        javax.swing.JPanel flowPanel = new javax.swing.JPanel();
        javax.swing.JButton startBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        javax.swing.JPanel titlePanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        filterPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        filterTxt = new javax.swing.JTextField();
        javax.swing.JButton createBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 300));
        jScrollPane1.setViewportView(jList);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        bottomPanel.setLayout(new java.awt.BorderLayout());

        fromAnotherCrateBtn.setText("Get Server List from Another host");
        fromAnotherCrateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromAnotherCrateBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(fromAnotherCrateBtn, java.awt.BorderLayout.NORTH);

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel3.setText("  ");
        bottomPanel.add(jLabel3, java.awt.BorderLayout.CENTER);

        startBtn.setText("Start Server");
        startBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBtnActionPerformed(evt);
            }
        });
        flowPanel.add(startBtn);

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        flowPanel.add(cancelBtn);

        bottomPanel.add(flowPanel, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        topPanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        titleLabel.setText("Server list");
        titlePanel.add(titleLabel);

        topPanel.add(titlePanel, java.awt.BorderLayout.PAGE_START);

        jLabel2.setText("Filter :  ");
        filterPanel.add(jLabel2);

        filterTxt.setColumns(20);
        filterTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterTxtActionPerformed(evt);
            }
        });
        filterPanel.add(filterTxt);

        createBtn.setText("Create New Server");
        createBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createBtnActionPerformed(evt);
            }
        });
        filterPanel.add(createBtn);

        topPanel.add(filterPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    //======================================================
    //======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void createBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createBtnActionPerformed
        try {
            jive.DevWizard wdlg = new jive.DevWizard(hostInfoDialog, hostInfoDialog.host);
            wdlg.showWizard(null);

            String servname = DevWizard.lastServStarted;
            if (servname != null) {
                //	Search Btn position to set dialog location
                Point p = getLocationOnScreen();
                p.translate(50, 50);
                try {
                    //	OK to start get the Startup control params.
                    //--------------------------------------------------
                    if (new TangoServer(servname, DevState.OFF).startupLevel(hostInfoDialog, hostInfoDialog.host.getName(), p))
                        hostInfoDialog.updateData();
                } catch (DevFailed e) {
                    Utils.popupError(hostInfoDialog, null, e);
                }
            }
        } catch (NoSuchMethodError ex) {
            Utils.popupError(hostInfoDialog, "This server is too old !\nUse Jive to create it.");
        }
        setVisible(false);
        dispose();
    }//GEN-LAST:event_createBtnActionPerformed

    //======================================================
    //======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void filterTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterTxtActionPerformed
        try {
            setList();
        } catch (DevFailed e) {
            Utils.popupError(hostInfoDialog, null, e);
        }
    }//GEN-LAST:event_filterTxtActionPerformed

    //======================================================
    //======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    //======================================================
    //======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void startBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBtnActionPerformed
        startSelectedItems();
    }//GEN-LAST:event_startBtnActionPerformed

    //======================================================
    //======================================================
    private void listSelectionPerformed(MouseEvent evt) {
        //	save selected item to set selection  later.
        //----------------------------------------------------
        previous_item = (String) jList.getSelectedValue();

        //	Check if double click
        //-----------------------------
        if (evt.getClickCount() == 2)
            startSelectedItems();
    }

    //======================================================
    //======================================================
    private void startSelectedItems() {
        selectedItems = new ArrayList<String>();

        //	At first try if already running
        Object[] selections = jList.getSelectedValues();
        if (selections.length>0) {
            for (Object selection : selections) {
                String serverName = (String) selection;
                try {
                    String deviceName = "dserver/" + serverName;
                    DeviceProxy dev = new DeviceProxy(deviceName);
                    try {
                        dev.ping();
                        //	ping works  --> already running -> throw exception
                        ErrorPane.showErrorMessage(hostInfoDialog, null,
                                new Exception(serverName + "  is Already Running  on " +
                                        new IORdump(deviceName).get_host() + " !"));

                    } catch (DevFailed e) {
                        // OK not running -> can be started */
                        selectedItems.add(serverName);
                    }
                } catch (DevFailed e) {
                    Utils.popupError(hostInfoDialog, null, e);
                }

            }
            setVisible(false);
            dispose();
        }
        else
            ErrorPane.showErrorMessage(this, null, new Exception("No server selected !"));
    }

    //======================================================
    //======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    //======================================================
    //======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void fromAnotherCrateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromAnotherCrateBtnActionPerformed
        try {
            HostList    hostListDialog = new HostList(this);
            if (hostListDialog.showDialog()== JOptionPane.OK_OPTION) {
                ArrayList<String>   serverList = hostListDialog.getServerList();
                String[]    array = new String[serverList.size()];
                for (int i=0 ; i<serverList.size() ; i++)
                    array[i] = serverList.get(i);
                jList.setListData(array);
                filterPanel.setVisible(false);
                titleLabel.setText("Start servers from " + hostListDialog.getSelectedHostName());
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_fromAnotherCrateBtnActionPerformed

    //======================================================
    //======================================================
    public void showDialog() {
        try {
            setList();
        } catch (DevFailed e) {
            Utils.popupError(hostInfoDialog, null, e);
        }
        setVisible(true);
    }

    //======================================================
    //======================================================
    public ArrayList<String> getSelectedItems() {
        return selectedItems;
    }

    //======================================================
    //======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel filterPanel;
    private javax.swing.JTextField filterTxt;
    private javax.swing.JList jList;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

}
