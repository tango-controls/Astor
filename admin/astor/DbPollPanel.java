//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011
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
 *	A panel to manage polling for a non running device.
 *
 * @author verdier
 */

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DbServer;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;


//===============================================================

/**
 * JDialog Class to display info
 *
 * @author Pascal Verdier
 */
//===============================================================


public class DbPollPanel extends JDialog {
    private Component parent;
    private DeviceProxy selected_dev;
    private DeviceProxy[] devices;
    private PolledAttr[] attlist;
    private static final String PollAttProp = "polled_attr";

    //===============================================================
    /**
     * Creates new form PollPanel with a button for each polled attribute
     */
    //===============================================================
    public DbPollPanel(JFrame parent, String servname) throws DevFailed {
        super(parent, true);
        this.parent = parent;
        initialize(servname);
    }

    //===============================================================
    public DbPollPanel(JDialog parent, String servname) throws DevFailed {
        super(parent, true);
        this.parent = parent;
        initialize(servname);
    }

    //===============================================================
    //===============================================================
    private void initialize(String servname) throws DevFailed {
        this.parent = parent;
        initComponents();
        titleLabel.setText("Polled Attributes For ");
        devices = getDeviceList(servname);

        if (devices.length == 0)
            Except.throw_exception("NO_DEVICES",
                    "No device found for " + servname,
                    "DbPollPanel.initialize(" + servname + ")");
        buildPanel(devices.length - 1);

        okBtn.setText("Apply");
        cancelBtn.setText("Dismiss");
    }

    //===============================================================
    //===============================================================
    private DeviceProxy[] getDeviceList(String servname) {
        try {
            ArrayList<String> v = new ArrayList<String>();
            DbServer serv = new DbServer(servname);
            String[] classes = serv.get_class_list();
            for (String classname : classes) {
                String[] devnames = serv.get_device_name(classname);
                v.addAll(Arrays.asList(devnames));
            }
            //	Create device proxy in reverse order
            DeviceProxy[] dp = new DeviceProxy[v.size()];
            for (int i = 0; i < v.size(); i++) {
                devComboBox.addItem(v.get(i));
                dp[i] = new DeviceProxy(v.get(i));
            }
            devComboBox.setSelectedIndex(v.size() - 1);
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
            for (PolledAttr att : attlist) {
                gbc.gridx = 0;
                gbc.gridy = y;
                gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
                panel.add(att.btn, gbc);

                gbc.gridx = 1;
                gbc.gridy = y;
                gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
                panel.add(att.txt, gbc);

                gbc.gridx = 2;
                gbc.gridy = y++;
                gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
                panel.add(new JLabel("ms"), gbc);
            }
            getContentPane().add(panel, BorderLayout.CENTER);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
        pack();
    }
    //===============================================================
    /**
     * Retreive polled attribute list
     */
    //===============================================================
    public PolledAttr[] getPolledAttributes(DeviceProxy dev) throws DevFailed {
        DbDatum argout = dev.get_property(PollAttProp);
        String[] data = argout.extractStringArray();
        if (data == null)
            return new PolledAttr[0];

        ArrayList<PolledAttr> v = new ArrayList<PolledAttr>();
        for (int i = 0; i < data.length; i += 2)
            v.add(new PolledAttr(data[i], data[i + 1]));

        PolledAttr[] pa = new PolledAttr[v.size()];
        for (int i = 0; i < v.size(); i++)
            pa[i] = v.get(i);
        return pa;
    }
    //===============================================================

    /**
     * Object defining polled attribute
     */
    //===============================================================
    class PolledAttr {
        String name;
        int period;
        JRadioButton btn;
        JTextField txt;

        public PolledAttr(String name, String strperiod) {
            this.name = name;
            try {
                this.period = Integer.parseInt(strperiod);
            } catch (NumberFormatException e) {
                this.period = -1;
            }
            btn = new JRadioButton(name);
            btn.setSelected(true);
            txt = new JTextField("" + period);
            txt.setColumns(6);
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        devComboBox = new javax.swing.JComboBox();

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

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18));
        titleLabel.setText("Dialog Title");
        jPanel2.add(titleLabel);

        devComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                devComboBoxActionPerformed(evt);
            }
        });

        jPanel2.add(devComboBox);

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    private void devComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_devComboBoxActionPerformed

        if (isVisible())
            buildPanel(devComboBox.getSelectedIndex());
    }//GEN-LAST:event_devComboBoxActionPerformed

    //===============================================================
    //===============================================================
    private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed

        //	Check selected attributes
        String message = "Remove polling for :\n";
        ArrayList<String> v = new ArrayList<String>();
        for (PolledAttr att : attlist)
            if (att.btn.getSelectedObjects() == null)
                message += att.name + "\n";
            else {
                v.add(att.name);
                try {
                    //	Check if period is coherent
                    att.period = Integer.parseInt(att.txt.getText());
                    v.add(att.txt.getText());
                } catch (NumberFormatException e) {
                    ErrorPane.showErrorMessage(this,
                            "NumberFormatException on  attribute " + att.name, e);
                    return;
                }
            }

        //	if some are not, ask to confirm to remove polling on them
        System.out.println(v.size());
        if (v.size() < attlist.length * 2)
            if (JOptionPane.showConfirmDialog(this,
                    message,
                    "Question",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION)
                return;

        //	OK prepeare String array
        String[] str = new String[v.size()];
        for (int i = 0; i < v.size(); i++)
            str[i] = v.get(i);

        //	And update database
        try {
            DbDatum data = new DbDatum(PollAttProp);
            data.insert(str);
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
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        doClose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    //===============================================================

    /**
     * Closes the dialog
     */
    //===============================================================
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    //===============================================================

    /**
     * Closes the dialog
     */
    //===============================================================
    private void doClose() {
        if (parent.getWidth() == 0)
            System.exit(0);

        setVisible(false);
        dispose();
    }

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelBtn;
    private javax.swing.JComboBox devComboBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton okBtn;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    //===============================================================


    //===============================================================

    /**
     * @param args the command line arguments
     */
    //===============================================================
    public static void main(String args[]) {

        String servname = null;
        if (args.length > 0)
            servname = args[0];

        try {
            if (servname == null)
                Except.throw_exception("NO_SERVER_NAME",
                        "Serrver's name ????",
                        "PollPanel.main()");
            new DbPollPanel(new JFrame(), servname).setVisible(true);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(new JFrame(), null, e);
            System.exit(0);
        } catch (java.lang.InternalError ie) {
            String x11_pb = "Can't connect to X11 window server";
            if (ie.toString().indexOf(x11_pb) > 0) {
                System.out.println(x11_pb);
                int action = AstorCmdLine.REMOVE_POLLING;
                if (args.length > 1 && args[1].equals("-f"))
                    action = AstorCmdLine.REMOVE_POLLING_FORCED;
                new AstorCmdLine(action, servname);

                System.exit(0);
            } else {
                System.exit(0);
            }
        }

    }

}
