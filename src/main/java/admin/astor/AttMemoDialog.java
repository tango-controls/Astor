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

import admin.astor.tools.Utils;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Class Description:
 * Basic Dialog Class to edit memorized attribute value
 *
 * @author root
 */
public class AttMemoDialog extends JDialog {
    private JDialog parent;
    private DbServer server;
    private boolean from_appli = true;
    private Memorized[] memorized;
    private static final String unknown = " ? ";
    //===============================================================
    /*
     *	Creates new form AttMemoDialog
     */
    //===============================================================
    public AttMemoDialog(JDialog parent, DbServer server) throws DevFailed {
        super(parent, true);
        this.parent = parent;
        this.server = server;
        initComponents();
        buildMemorizedPanel();

        titleLabel.setText("Memorized attributes found for " + server.name());
        pack();
        //	Check if from an appli or from an empty JDialog
        if (parent.getWidth() == 0)
            from_appli = false;

        if (from_appli)
            AstorUtil.centerDialog(this, parent);
    }
    //===============================================================
    //===============================================================
    private JLabel[] attLbl;
    private JTextField[] attVal;

    private void buildMemorizedPanel() throws DevFailed {
        readAttributes();
        attLbl = new JLabel[memorized.length];
        attVal = new JTextField[memorized.length];
        for (int i = 0; i < memorized.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            int x = 0;
            int y = i + 1;
            attLbl[i] = new JLabel(memorized[i].attname);
            gbc.gridx = x++;
            gbc.gridy = y;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            valuePanel.add(attLbl[i], gbc);

            gbc.gridx = x++;
            gbc.gridy = y;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            valuePanel.add(new JLabel("  :    "), gbc);

            attVal[i] = new JTextField(memorized[i].value);
            attVal[i].setColumns(15);
            gbc.gridx = x++;
            gbc.gridy = y;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            valuePanel.add(attVal[i], gbc);


            gbc.gridx = x;
            gbc.gridy = y;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            valuePanel.add(new JLabel("     " +
                    memorized[i].min + " < .... < " +
                    memorized[i].max), gbc);

        }
    }

    //===============================================================
    //===============================================================
    private void readAttributes() throws DevFailed {
        DeviceProxy[] devices = getDevices();
        List<String> stringList = new ArrayList<>();
        for (DeviceProxy device : devices) {
            String deviceName = device.get_name();
            //	Get Attribute known by Database
            DeviceData argin = new DeviceData();
            String[] args = new String[2];
            args[0] = deviceName;
            args[1] = "*";
            argin.insert(args);
            DeviceData argOut =
                    ApiUtil.get_db_obj().command_inout("DbGetDeviceAttributeList", argin);
            String[] attlist = argOut.extractStringArray();
            //	Get only witch have memorized value set
            for (String att : attlist) {
                DbAttribute db_att = device.get_attribute_property(att);
                String[] propertyList = db_att.get_property_list();
                for (int j = 0; j < propertyList.length; j++) {
                    if (propertyList[j].equals("__value")) {
                        String min = unknown;
                        if (!db_att.is_empty("min_value"))
                            min = db_att.get_value("min_value")[0];
                        String max = unknown;
                        if (!db_att.is_empty("max_value"))
                            max = db_att.get_value("max_value")[0];
                        stringList.add(deviceName + "/" + att);
                        stringList.add(db_att.get_string_value(j));
                        stringList.add(min);
                        stringList.add(max);
                    }
                }
            }
        }
        //	build memorized array
        memorized = new Memorized[stringList.size() / 4];
        for (int i = 0; i < stringList.size(); i += 4)
            memorized[i / 4] = new Memorized(stringList.get(i),
                    stringList.get(i + 1),
                    stringList.get(i + 2),
                    stringList.get(i + 3));
    }

    //===============================================================
    //===============================================================
    private DeviceProxy[] getDevices() throws DevFailed {
        List<String> deviceNames = new ArrayList<>();
        String[] classes = server.get_class_list();
        for (String aClass : classes) {
            String[] devices = server.get_device_name(aClass);
            deviceNames.addAll(Arrays.asList(devices));
        }
        DeviceProxy[] dev = new DeviceProxy[deviceNames.size()];
        int i=0;
        for (String deviceName : deviceNames)
            dev[i++] = new DeviceProxy(deviceName);
        return dev;
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

        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JButton okBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        valuePanel = new javax.swing.JPanel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okBtn.setText("OK");
        okBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okBtnActionPerformed(evt);
            }
        });
        jPanel1.add(okBtn);

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        jPanel1.add(cancelBtn);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Dialog Title");
        jPanel2.add(titleLabel);

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        valuePanel.setLayout(new java.awt.GridBagLayout());
        getContentPane().add(valuePanel, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    private boolean writeValues() {
        String message = "";
        for (int i = 0; i < attVal.length; i++) {
            String val_str = attVal[i].getText();
            Memorized memo = memorized[i];
            double val;

            if (!val_str.equals(memo.value)) {
                //	Check if number
                try {
                    val = Double.parseDouble(val_str);
                } catch (NumberFormatException e) {
                    Utils.popupError(parent, val_str + " is not a number !");
                    return false;
                }

                //	Check if out of bounds
                if (!memo.min.equals(unknown)) {
                    double min;
                    try {
                        min = Double.parseDouble(memo.min);
                    } catch (NumberFormatException e) {
                        Utils.popupError(parent, memo.min + " is not a number !");
                        return false;
                    }
                    if (val < min) {
                        Utils.popupError(parent, "Incorrect value:\n" +
                                val_str + " is less than " + memo.min);
                        return false;
                    }
                }
                if (!memo.max.equals(unknown)) {
                    double max;
                    try {
                        max = Double.parseDouble(memo.max);
                    } catch (NumberFormatException e) {
                        Utils.popupError(parent, memo.max + " is not a number !");
                        return false;
                    }
                    if (val > max) {
                        Utils.popupError(parent, "Incorrect value:\n" +
                                val_str + " is greater than " + memo.max);
                        return false;
                    }
                }

                //	write to database
                try {
                    DbDatum data = new DbDatum("__value");
                    data.insert(val_str);
                    AttributeProxy att = new AttributeProxy(attLbl[i].getText());
                    att.put_property(data);
                    memo.value = val_str;
                    message += attLbl[i].getText() + "  set to  " + val_str + "\n";
                } catch (DevFailed e) {
                    ErrorPane.showErrorMessage(parent, null, e);
                    return false;
                }
            }
        }
        if (message.length() > 0)
            Utils.popupMessage(parent, message);
        return true;
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed
        if (writeValues())
            doClose();
    }//GEN-LAST:event_okBtnActionPerformed

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

    /**
     * Closes the dialog
     */
    //===============================================================
    private void doClose() {
        setVisible(false);
        dispose();
        if (!from_appli)
            System.exit(0);
    }

    //===============================================================
    //===============================================================
    public void showDialog() {
        //	Check if memorized attribute not found
        if (memorized.length == 0) {
            Utils.popupError(parent,
                    "No memorized attribute found for " + server.name());
        } else
            setVisible(true);
    }

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel valuePanel;
    // End of variables declaration//GEN-END:variables
    //===============================================================


    //===============================================================

    /**
     * @param args the command line arguments
     */
    //===============================================================
    public static void main(String args[]) {

        try {
            AttMemoDialog d = new AttMemoDialog(new JDialog(),
                    new DbServer("PowerSupply/pv"));
            d.showDialog();
        } catch (DevFailed e) {
            Except.print_exception(e);
        }
    }


    //===============================================================
    //===============================================================
    class Memorized {
        String attname;
        String value;
        String min;
        String max;

        Memorized(String attname, String value, String min, String max) {
            this.attname = attname;
            this.value = value;
            this.min = min;
            this.max = max;
        }
    }
}
