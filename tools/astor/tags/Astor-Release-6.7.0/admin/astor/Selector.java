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

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


//===============================================================

/**
 * Class Description: Basic Dialog Class to display info
 *
 * @author Pascal Verdier
 */
//===============================================================


public class Selector extends JDialog {
    private String retVal = "";
    private List<String>    values = new ArrayList<String>();

    //===============================================================
    /*
     * Creates new form Selector
     */
    //===============================================================
    public Selector(JFrame parent, String title, List<String> values, String def_val) {
        super(parent, true);
        initComponents();

        this.values = values;
        comboBox.addItem("");
        for (int i = 0; values != null && i < values.size(); i++) {
            comboBox.addItem(values.get(i));
            if (values.get(i).equals(def_val))
                comboBox.setSelectedIndex(i + 1);
        }
        comboBox.setEditable(true);

        titleLabel.setText(title);
        pack();
    }

    //===============================================================
    /*
     * Creates new form Selector
     */
    //===============================================================
    @SuppressWarnings("UnusedDeclaration")
    public Selector(JFrame parent, String title, String[] values, String def_val) {
        super(parent, true);
        initComponents();

        comboBox.addItem("");
        for (int i = 0; values != null && i < values.length; i++) {
            comboBox.addItem(values[i]);
            this.values.add(values[i]);
            if (values[i].equals(def_val))
                comboBox.setSelectedIndex(i + 1);
        }
        comboBox.setEditable(true);

        titleLabel.setText(title);
        pack();
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
        comboBox = new javax.swing.JComboBox<String>();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton okBtn = new javax.swing.JButton();
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

        comboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxActionPerformed(evt);
            }
        });
        getContentPane().add(comboBox, java.awt.BorderLayout.CENTER);

        okBtn.setText("OK");
        okBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(okBtn);

        cancelBtn.setText("Cancel");
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
    private void comboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxActionPerformed
        //okBtnActionPerformed(evt);
    }//GEN-LAST:event_comboBoxActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed
        retVal = (String) comboBox.getSelectedItem();

        //	Check if already in combo box or a new one
        boolean found = false;
        for (String value : values)
            if (value.equals(retVal))
                found = true;
        //	If not foun add for a next usage
        if (!found)
            comboBox.addItem(retVal);

        //	if it is the empty one, clear it
        comboBox.removeItemAt(0);
        comboBox.insertItemAt("", 0);
        doClose();
    }//GEN-LAST:event_okBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        retVal = null;
        doClose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        retVal = null;
        doClose();
    }//GEN-LAST:event_closeDialog

    //===============================================================
    /**
     * Closes the dialog
     */
    //===============================================================
    private void doClose() {
        setVisible(false);
        //dispose();
    }

    //===============================================================
    //===============================================================
    public String showDialog() {
        setVisible(true);
        return retVal;
    }

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> comboBox;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    //===============================================================


    //===============================================================

    /**
     * @param args the command line arguments
     */
    //===============================================================
    public static void main(String args[]) {
        String title = "TANGO_HOST ?";
        String[] tgh = {"corvus:10000", "orion:10000"};
        ArrayList<String> v = new ArrayList<String>();
        v.add(tgh[0]);
        v.add(tgh[1]);
        Selector sel = new Selector(new JFrame(), title, v, "orion:10000");

        String th = sel.showDialog();
        if (th != null)
            System.out.println(th);

        th = sel.showDialog();
        if (th != null)
            System.out.println(th);

        System.exit(0);
    }

}
