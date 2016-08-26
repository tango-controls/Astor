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


package admin.astor.access;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDialog Class to display info
 *
 * @author Pascal Verdier
 */
public class ListSelectionDialog extends JDialog {
    private int retVal = JOptionPane.OK_OPTION;
    private ClassAllowed def_class = null;
    private String[] cmdExist;

    private int mode;
    private static final int CLASS_MODE = 0;
    private static final int COMMAND_MODE = 1;
    //===============================================================
    /*
     * Creates new form ListSelectionDialog for classes
     */
    //===============================================================
    public ListSelectionDialog(JFrame parent, String[] exist) throws DevFailed {
        super(parent, true);
        cmdExist = exist;
        mode = CLASS_MODE;
        initComponents();
        initOwnComponents();

        titleLabel.setText("Classes found in database");
        pack();
        ATKGraphicsUtils.centerDialog(this);
    }
    //===============================================================
    /*
     * Creates new form ListSelectionDialog for commands
     */
    //===============================================================
    public ListSelectionDialog(JFrame parent, ClassAllowed def_class) throws DevFailed {
        super(parent, true);
        this.def_class = def_class;
        mode = COMMAND_MODE;
        initComponents();
        initOwnComponents();

        titleLabel.setText("Commands not allowed for " + def_class.name);
        pack();

        //	cascade window
        Point p = parent.getLocationOnScreen();
        p.x += 50;
        p.y += 50;
        setLocation(p);
        //ATKGraphicsUtils.centerDialog(this);
    }

    //===============================================================
    //===============================================================
    private String[] doesNotExist(String[] array1, String[] array2) {
        List<String> list = new ArrayList<>();
        for (String s1 : array1) {
            String cmd = null;
            for (String s2 : array2)
                if (s1.toLowerCase().equals(s2.toLowerCase()))
                    cmd = s1;
            if (cmd == null)    //	Not found
                list.add(s1);
        }
        return list.toArray(new String[list.size()]);
    }

    //===============================================================
    //===============================================================
    private void initOwnComponents() throws DevFailed {
        //	Get exported devices for class
        Database dbase = ApiUtil.get_db_obj();

        jScrollPane1.setPreferredSize(new Dimension(300, 400));
        if (mode == CLASS_MODE) {
            String[] classes = dbase.get_class_list("*");
            //	keep only not_existing
            classes = doesNotExist(classes, cmdExist);
            cmdList.setListData(classes);
        } else {
            //	Commands mode
            String[] devices = dbase.get_device_exported_for_class(def_class.name);
            String[] commands = null;
            for (String devname : devices) {
                try {
                    //	Try to read all commands.
                    System.out.println("try to read " + devname);
                    CommandInfo[] info =
                            new DeviceProxy(devname).command_list_query();
                    //	And keep only not already allowed
                    commands = def_class.getNotAllowed(info);
                    break;
                } catch (DevFailed e) {
                    //ErrorPane.showErrorMessage(this, null, e);
                }
            }
            if (commands != null) {
                cmdList.setListData(commands);
            } else
                Except.throw_exception("NoDeviceExported",
                        "There is no device exported for class " + def_class.name +
                                " to get the command list !",
                        "ListSelectionDialog.initOwnComponents()");
        }
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
        jScrollPane1 = new javax.swing.JScrollPane();
        cmdList = new javax.swing.JList<>();

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

        cmdList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cmdListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(cmdList);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    private String selection = null;

    public String getSelection() {
        return selection;
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed

        selection = cmdList.getSelectedValue();
        retVal = JOptionPane.OK_OPTION;
        doClose();
    }//GEN-LAST:event_okBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        retVal = JOptionPane.CANCEL_OPTION;
        doClose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        retVal = JOptionPane.CANCEL_OPTION;
        doClose();
    }//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    private void cmdListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmdListMouseClicked

        //	Check if double click
        if (evt.getClickCount() == 2) {
            selection = cmdList.getSelectedValue();
            retVal = JOptionPane.OK_OPTION;
            doClose();
        }
    }//GEN-LAST:event_cmdListMouseClicked

    //===============================================================

    /**
     * Closes the dialog
     */
    //===============================================================
    private void doClose() {
        setVisible(false);
        dispose();
    }

    //===============================================================
    //===============================================================
    public int showDialog() {
        setVisible(true);
        return retVal;
    }

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> cmdList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    //===============================================================

}
