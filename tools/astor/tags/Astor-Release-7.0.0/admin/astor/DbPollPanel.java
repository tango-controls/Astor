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

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DbServer;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * JDialog Class to display info
 *
 * @author Pascal Verdier
 */
@SuppressWarnings("MagicConstant")
public class DbPollPanel extends JDialog {
    private Component parent;
    private DeviceProxy selected_dev;
    private DeviceProxy[] devices;
    private PolledAttribute[] attlist;
    private static final String PollAttProp = "polled_attr";

    //===============================================================
    /**
     * Creates new form PollPanel with a button for each polled attribute
     */
    //===============================================================
    public DbPollPanel(JFrame parent, String serverName) throws DevFailed {
        super(parent, true);
        this.parent = parent;
        initialize(serverName);
    }

    //===============================================================
    //===============================================================
    public DbPollPanel(JDialog parent, String serverName) throws DevFailed {
        super(parent, true);
        this.parent = parent;
        initialize(serverName);
    }

    //===============================================================
    //===============================================================
    private void initialize(String serverName) throws DevFailed {
        initComponents();
        titleLabel.setText("Polled Attributes For ");
        devices = getDeviceList(serverName);

        if (devices.length == 0)
            Except.throw_exception("NO_DEVICES",
                    "No device found for " + serverName,
                    "DbPollPanel.initialize(" + serverName + ")");
        buildPanel(devices.length - 1);

        ATKGraphicsUtils.centerDialog(this);
    }

    //===============================================================
    //===============================================================
    private DeviceProxy[] getDeviceList(String serverName) {
        try {
            List<String> deviceList = new ArrayList<>();
            DbServer dbServer = new DbServer(serverName);
            String[] classes = dbServer.get_class_list();
            for (String className : classes) {
                String[] deviceNames = dbServer.get_device_name(className);
                deviceList.addAll(Arrays.asList(deviceNames));
            }
            //	Create device proxy in reverse order
            DeviceProxy[] dp = new DeviceProxy[deviceList.size()];
            for (int i = 0; i < deviceList.size(); i++) {
                devComboBox.addItem(deviceList.get(i));
                dp[i] = new DeviceProxy(deviceList.get(i));
            }
            devComboBox.setSelectedIndex(deviceList.size() - 1);
            return dp;
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
            return new DeviceProxy[0];
        }
    }

    //===============================================================
    //===============================================================
    private JPanel panel = null;

    private void buildPanel(int dev_idx) {
        selected_dev = devices[dev_idx];
        if (panel != null)
            getContentPane().remove(panel);

        try {
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            int y = 0;

            attlist = getPolledAttributes(selected_dev);
            for (PolledAttribute att : attlist) {
                gbc.gridx = 0;
                gbc.gridy = y;
                gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
                panel.add(att.radioButton, gbc);

                gbc.gridx = 1;
                gbc.gridy = y;
                gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
                panel.add(att.textField, gbc);

                gbc.gridx = 2;
                gbc.gridy = y++;
                gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
                panel.add(new JLabel("ms"), gbc);
            }
            scrollPane.setViewportView(panel);
            getContentPane().add(scrollPane, BorderLayout.CENTER);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
        pack();
        if (scrollPane.getHeight()>800)
            scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth(), 800));
    }
    //===============================================================
    /**
     * Retrieve polled attribute list
     */
    //===============================================================
    public PolledAttribute[] getPolledAttributes(DeviceProxy dev) throws DevFailed {
        DbDatum argOut = dev.get_property(PollAttProp);
        String[] data = argOut.extractStringArray();
        if (data==null || data.length<2)
            return new PolledAttribute[0];

        List<PolledAttribute> polledAttributeList = new ArrayList<>();
        for (int i=0 ; i<data.length ; i+=2)
            polledAttributeList.add(new PolledAttribute(data[i], data[i + 1]));

        PolledAttribute[] polledAttributes = new PolledAttribute[polledAttributeList.size()];
        for (int i=0 ; i<polledAttributeList.size() ; i++)
            polledAttributes[i] = polledAttributeList.get(i);
        return polledAttributes;
    }
    //===============================================================
    /**
     * Object defining polled attribute
     */
    //===============================================================
    class PolledAttribute {
        String name;
        int period;
        JRadioButton radioButton;
        JTextField textField;

        public PolledAttribute(String name, String strperiod) {
            this.name = name;
            try {
                this.period = Integer.parseInt(strperiod);
            } catch (NumberFormatException e) {
                this.period = -1;
            }
            radioButton = new JRadioButton(name);
            radioButton.setSelected(true);
            textField = new JTextField(Integer.toString(period));
            textField.setColumns(6);
        }
    }
    //===============================================================
    /**
     * This method is called from within the constructor to
     * buildPanel the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    //===============================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        devComboBox = new javax.swing.JComboBox<>();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton okBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Dialog Title");
        topPanel.add(titleLabel);

        devComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                devComboBoxActionPerformed(evt);
            }
        });
        topPanel.add(devComboBox);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        okBtn.setText("Apply");
        okBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(okBtn);

        cancelBtn.setText("Dismiss");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);
        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void devComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_devComboBoxActionPerformed

        if (isVisible())
            buildPanel(devComboBox.getSelectedIndex());
    }//GEN-LAST:event_devComboBoxActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed

        //	Check selected attributes
        String message = "Remove polling for :\n";
        List<String> attributeList = new ArrayList<>();
        for (PolledAttribute att : attlist)
            if (att.radioButton.getSelectedObjects() == null)
                message += att.name + "\n";
            else {
                attributeList.add(att.name);
                try {
                    //	Check if period is coherent
                    att.period = Integer.parseInt(att.textField.getText());
                    attributeList.add(att.textField.getText());
                } catch (NumberFormatException e) {
                    ErrorPane.showErrorMessage(this,
                            "NumberFormatException on  attribute " + att.name, e);
                    return;
                }
            }

        //	if some are not, ask to confirm to remove polling on them
        System.out.println(attributeList.size());
        if (attributeList.size() < attlist.length * 2)
            if (JOptionPane.showConfirmDialog(this,
                    message,
                    "Question",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION)
                return;

        //	OK prepare String array
        String[] AttributeNames = new String[attributeList.size()];
        for (int i = 0; i < attributeList.size(); i++)
            AttributeNames[i] = attributeList.get(i);

        //	And update database
        try {
            DbDatum data = new DbDatum(PollAttProp);
            data.insert(AttributeNames);
            selected_dev.put_property(data);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }

        JOptionPane.showMessageDialog(this,
                "Database Updated",
                "",
                JOptionPane.INFORMATION_MESSAGE);
        //doClose();
    }//GEN-LAST:event_okBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        doClose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    //===============================================================

    /**
     * Closes the dialog
     */
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    //===============================================================

    /**
     * Closes the dialog
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
    private javax.swing.JComboBox<String> devComboBox;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    //===============================================================


    //===============================================================

    /**
     * @param args the command line arguments
     */
    //===============================================================
    public static void main(String args[]) {

        String serverName = null;
        if (args.length > 0)
            serverName = args[0];

        try {
            if (serverName == null)
                Except.throw_exception("NO_SERVER_NAME",
                        "Server's name ????",
                        "PollPanel.main()");
            new DbPollPanel((JFrame)null, serverName).setVisible(true);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(new JFrame(), null, e);
            System.exit(0);
        }
    }
}
