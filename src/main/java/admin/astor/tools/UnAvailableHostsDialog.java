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


package admin.astor.tools;

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


//===============================================================
/**
 * JDialog Class to display info
 *
 * @author Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class UnAvailableHostsDialog extends JDialog {
    private JFrame parent;
    private List<StoppedHost> stoppedHosts;
    private String[] lastCollections;

    //===============================================================
    //===============================================================
    private class StoppedHost {
        String name;
        String collection = "Not Defined";
        boolean inLastCollections = true;
        //===========================================================
        private StoppedHost(String name) {
            this.name = name;
            try {
                DbDatum datum = new DeviceProxy(
                        AstorUtil.getStarterDeviceHeader()+name).get_property("HostCollection");
                if (!datum.is_empty()) {
                    collection = datum.extractString();
                    boolean found = false;
                    for (String s: lastCollections) {
                        if (s.equals(collection)) {
                            found = true;
                        }
                    }
                    inLastCollections = found;
                }
            }
            catch (DevFailed e) { /* */ }
        }
        //===========================================================
    }


    //===============================================================
    /*
     *	Creates new form UnAvailableHostsDialog
     */
    //===============================================================
    public UnAvailableHostsDialog(JFrame parent) throws DevFailed {
        super(parent, true);
        this.parent = parent;
        initComponents();
        notCriticalBtn.setBackground(Color.orange);
        criticalBtn.setBackground(Color.red);
        criticalBtn.setForeground(Color.white);
        AstorUtil.startSplash("Pinging crates.....");
        AstorUtil.increaseSplashProgress(0.3, "Get host list from database");
        String[] ctrlHosts = MySqlUtil.getInstance().getHostControlledList();
        lastCollections = AstorUtil.getInstance().getLastCollectionList();
        AstorUtil.increaseSplashProgress(0.6, "Checking " + ctrlHosts.length + " hosts");

        PingHosts pg = new PingHosts(ctrlHosts);
        AstorUtil.increaseSplashProgress(0.8, "Building stopped host list");
        stoppedHosts = buildStoppedHosts(pg.getStoppedList());
        int x = 0;
        int y = 0;
        int modulo = (int)Math.sqrt(stoppedHosts.size());
        for (StoppedHost host : stoppedHosts) {
            JButton btn = new JButton(host.name);
            if (host.inLastCollections) {
                btn.setBackground(Color.orange);
                btn.setForeground(Color.black);
            }
            else {
                btn.setBackground(Color.red);
                btn.setForeground(Color.white);
            }
            btn.setToolTipText(host.collection);

            btn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    hostBtnActionPerformed(evt);
                }
            });
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = x++;
            gbc.gridy = y;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new java.awt.Insets(10, 10, 10, 10);
            centerPanel.add(btn, gbc);

            if (x == modulo) {
                x = 0;
                y++;
            }
        }
        titleLabel.setText(stoppedHosts.size() + " Unreachable Hosts");
        pack();
        ATKGraphicsUtils.centerDialog(this);
        AstorUtil.stopSplash();
    }

    //===============================================================
    //===============================================================
    private List<StoppedHost> buildStoppedHosts(List<String> hostNames) {
        List<StoppedHost>  list = new ArrayList<>();
        for (String hostName : hostNames) {
            list.add(new StoppedHost(hostName));
        }
        return list;
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

        centerPanel = new javax.swing.JPanel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton updateBtn = new javax.swing.JButton();
        javax.swing.JButton unexportAllBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        javax.swing.JPanel topTopPanel = new javax.swing.JPanel();
        criticalBtn = new javax.swing.JButton();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        notCriticalBtn = new javax.swing.JButton();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        centerPanel.setLayout(new java.awt.GridBagLayout());
        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        bottomPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 50, 5));

        updateBtn.setText("Update List");
        updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(updateBtn);

        unexportAllBtn.setText("Unexport All ");
        unexportAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unExportAllBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(unexportAllBtn);

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        topPanel.setLayout(new java.awt.BorderLayout());

        criticalBtn.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        criticalBtn.setText("   Critical   ");
        topTopPanel.add(criticalBtn);

        jLabel2.setText("           ");
        topTopPanel.add(jLabel2);

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Unreachable Hosts");
        topTopPanel.add(titleLabel);

        jLabel1.setText("           ");
        topTopPanel.add(jLabel1);

        notCriticalBtn.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        notCriticalBtn.setText(" Not Critical ");
        topTopPanel.add(notCriticalBtn);

        topPanel.add(topTopPanel, java.awt.BorderLayout.NORTH);
        topPanel.add(jSeparator1, java.awt.BorderLayout.CENTER);

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel3.setText("           ");
        topPanel.add(jLabel3, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    private void hostBtnActionPerformed(java.awt.event.ActionEvent evt) {
        JButton btn = (JButton) evt.getSource();
        String hostName = btn.getText();
        try {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            OneHost host = new OneHost(hostName);
            new PopupText(this, host).setVisible(true);
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        } catch (DevFailed e) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            ErrorPane.showErrorMessage(this, null, e);
        }
    }

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
    private void unExportAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unexportAllBtnActionPerformed

        if (stoppedHosts.size()>0) {
            if (JOptionPane.showConfirmDialog(parent,
                    "Unexport all devices registered on " + stoppedHosts.size() + " hosts ?",
                    "Confirm Dialog",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {

                //  OK Un export
                AstorUtil.startSplash("Un export");
                int ratio = 100 / stoppedHosts.size();
                for (StoppedHost host : stoppedHosts) {
                    AstorUtil.increaseSplashProgress(ratio, "un export devices for " + host.name);
                    try {
                        setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        OneHost oneHost = new OneHost(host.name);
                        oneHost.unExportDevices();
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    } catch (DevFailed e) {
                        AstorUtil.stopSplash();
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        ErrorPane.showErrorMessage(this, null, e);
                    }
                }
                AstorUtil.stopSplash();
            }
        }
    }//GEN-LAST:event_unexportAllBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed

        setVisible(false);
        dispose();
        try {
            new UnAvailableHostsDialog(parent).setVisible(true);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
            doClose();
        }
    }//GEN-LAST:event_updateBtnActionPerformed

    //===============================================================
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
    //===============================================================

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JButton criticalBtn;
    private javax.swing.JButton notCriticalBtn;
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
            new UnAvailableHostsDialog(null).setVisible(true);
        } catch (DevFailed e) {
            AstorUtil.stopSplash();
            ErrorPane.showErrorMessage(new Frame(), null, e);
        }
    }
    //===============================================================
    //===============================================================








    //===============================================================
    //===============================================================
    private class OneHost extends ArrayList<OneServer> {
        private String name;

        //===========================================================
        private OneHost(String name) throws DevFailed {
            this.name = name;
            //  Get server list
            DeviceProxy dbDev = new DeviceProxy(ApiUtil.get_db_obj().get_name());
            DeviceData argIn = new DeviceData();
            argIn.insert(name);
            DeviceData argOut = dbDev.command_inout("DbGetHostServersInfo", argIn);
            String[] servers = argOut.extractStringArray();
            for (int i=0 ; i<servers.length ; i++) {
                if ((i % 3) == 0)   //  1- controlled, 2 - level
                    add(new OneServer(servers[i]));
            }
        }

        //===========================================================
        private void unExportDevices() throws DevFailed {
            for (OneServer server : this) {
                server.unExportDeices();
            }
        }

        //===========================================================
        public String toString() {
            StringBuilder sb = new StringBuilder(name + ":\n");
            for (OneServer server : this) {
                sb.append(server).append("\n");
            }
            return sb.toString();
        }
        //===========================================================
    }

    //===============================================================
    //===============================================================
    private class OneServer extends ArrayList<DeviceProxy> // devices
    {
        private String name;

        //===========================================================
        private OneServer(String name) throws DevFailed {
            this.name = name;
            //  Get device list
            DbServer dbServer = new DbServer(name);
            String[] devClasses = dbServer.get_device_class_list();
            for (int i=0 ; i<devClasses.length ; i++) {
                if ((i % 2) == 0) { //  1- class name
                    add(new DeviceProxy(devClasses[i]));
                }
            }
        }
        //===========================================================
        private void unExportDeices() throws DevFailed {
            for (DeviceProxy deviceProxy : this) {
                deviceProxy.unexport_device();
            }
        }
        //===========================================================
        public String toString() {
            StringBuilder sb = new StringBuilder(name + ":\n");
            for (DeviceProxy deviceProxy : this) {
                sb.append("\t").append(deviceProxy.name()).append("\n");
            }
            return sb.toString();
        }
        //===========================================================
    }


    //===============================================================
    //===============================================================
    public class PopupText extends JDialog {
        private OneHost host;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JTextArea textArea;

        //======================================================
        /*
         *	Creates new form PopupText
         */
        //======================================================
        public PopupText(JDialog parent, OneHost host) {
            super(parent, true);
            this.host = host;
            initComponents();
            textArea.setText(host.toString());
            pack();
            ATKGraphicsUtils.centerDialog(this);
        }
        //======================================================
        //======================================================
        private void initComponents() {
            jScrollPane1 = new javax.swing.JScrollPane();
            textArea = new javax.swing.JTextArea();
            textArea.setFont(new java.awt.Font("monospaced", 1, 12));
            addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    closeDialog(evt);
                }
            }
            );


            JButton unexportButton = new JButton("UnExport devices");
            unexportButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    unexportButtonActionPerformed(evt);
                }
            }
            );
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cancelButtonActionPerformed(evt);
                }
            }
            );

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new java.awt.FlowLayout(2, 5, 5));
            buttonPanel.add(unexportButton);
            buttonPanel.add(cancelButton);

            getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
            jScrollPane1.setViewportView(textArea);
            getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
        }
        //============================================================
        //============================================================
        public void setFont(java.awt.Font font) {
            textArea.setFont(font);
        }
        //============================================================
        //============================================================
        @SuppressWarnings({"UnusedDeclaration"})
        private void unexportButtonActionPerformed(java.awt.event.ActionEvent evt) {
            System.out.println("UnExport devices for " + host.name);
            try {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                host.unExportDevices();
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } catch (DevFailed e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                ErrorPane.showErrorMessage(this, null, e);
            }
            doClose();
        }
        //============================================================
        //============================================================
        @SuppressWarnings({"UnusedDeclaration"})
        private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
            doClose();
        }
        //============================================================
        //============================================================
        @SuppressWarnings({"UnusedDeclaration"})
        private void closeDialog(java.awt.event.WindowEvent evt) {
            doClose();
        }
        //============================================================
        //============================================================
        private void doClose() {
            setVisible(false);
            dispose();
        }
    }
}
