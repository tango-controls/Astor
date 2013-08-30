//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
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
// $Revision$
//
//-======================================================================


package admin.astor.access;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import java.util.StringTokenizer;
import java.util.ArrayList;


//===============================================================

/**
 * JDialog Class to display info
 *
 * @author Pascal Verdier
 */
//===============================================================


public class PasswordDialog extends JDialog {
    private byte[] bytePassword;
    private int retVal = JOptionPane.OK_OPTION;
    private int mode = GET_PASSWORD;

    private static final int GET_PASSWORD = 0;
    private static final int CHANGE_PASSWORD = 1;
    private static final String defaultPasssword = "SUPER_TANGO";

    //===============================================================
    /*
      *	Creates new form PasswordDialog to change password
      */
    //===============================================================
    public PasswordDialog(JFrame parent, String title) {
        this(parent, null, title);
        this.mode = CHANGE_PASSWORD;
    }

    //===============================================================
    /*
      *	Creates new form PasswordDialog to get password
      */
    //===============================================================
    public PasswordDialog(JFrame parent, byte[] bytePassword) {
        this(parent, bytePassword, null);
    }

    //===============================================================
    /*
      *	Creates new form PasswordDialog to get password
      */
    //===============================================================
    public PasswordDialog(JFrame parent, byte[] bytePassword, String title) {
        super(parent, true);
        this.bytePassword = bytePassword;
        initComponents();

        if (title != null)
            titleLabel.setText(title);
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
        javax.swing.JPanel centerPanel = new javax.swing.JPanel();
        passwordField = new javax.swing.JPasswordField();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton okBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18));
        titleLabel.setText("Password ?");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        passwordField.setColumns(20);
        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                passwordFieldKeyPressed(evt);
            }
        });
        centerPanel.add(passwordField);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

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
    @SuppressWarnings({"UnusedDeclaration"})
    private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed
        checkPassword();
    }//GEN-LAST:event_okBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        doClose(JOptionPane.CANCEL_OPTION);
    }//GEN-LAST:event_cancelBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(JOptionPane.CANCEL_OPTION);
    }//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    private void passwordFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordFieldKeyPressed

        int code = evt.getKeyCode();
        if (code == 27) {
            doClose(JOptionPane.CANCEL_OPTION);
        } else if (code == 10) {
            checkPassword();
        }
    }//GEN-LAST:event_passwordFieldKeyPressed

    //===============================================================
    //===============================================================
    private void checkPassword() {
        if (mode == CHANGE_PASSWORD) {    //  no check
            doClose(JOptionPane.OK_OPTION);
            return;
        }
        String str = new String(passwordField.getPassword());
        byte[] text = str.getBytes();
        byte[] superTango = defaultPasssword.getBytes();

        if (comparePassword(text, bytePassword))
            doClose(JOptionPane.OK_OPTION);
        else if (comparePassword(text, superTango))
            doClose(JOptionPane.OK_OPTION);
        else {
            TangoAccess.popupError(this, "Invalid Password !");
            passwordField.setText("");
        }
    }

    //===============================================================
    //===============================================================
    private boolean comparePassword(byte[] text, byte[] password) {
        if (text.length != password.length)
            return false;

        for (int i = 0; i < text.length && i < password.length; i++) {
            if (text[i] != password[i])
                return false;
        }
        return true;
    }

    //===============================================================
    //===============================================================
    private void doClose(int ret) {
        retVal = ret;
        setVisible(false);
        dispose();
    }

    //===============================================================
    //===============================================================
    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    //===============================================================
    //===============================================================
    public int showDialog() {
        setVisible(true);
        return retVal;
    }

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    //===============================================================

    //===============================================================

    /**
     * @param args the command line arguments
     */
    //===============================================================
    public static void main(String args[]) {

        PasswordDialog dlg =
                new PasswordDialog(new JFrame(), "dserver".getBytes());
        dlg.setVisible(true);
    }

    //===============================================================
    //===============================================================
    private static String bytes2str(byte[] bytes) {
        String str = "";
        for (byte b : bytes)
            str += " " + b;
        return str.trim();
    }

    //===============================================================
    //===============================================================
    private static byte[] str2bytes(String str) {
        StringTokenizer stk = new StringTokenizer(str);
        ArrayList<Integer> v = new ArrayList<Integer>();
        while (stk.hasMoreTokens()) {
            try {
                v.add(Integer.parseInt(stk.nextToken()));
            } catch (NumberFormatException e) {
                v.add(0);
            }
        }
        byte[] b = new byte[v.size()];
        for (int i = 0; i < v.size(); i++)
            b[i] = v.get(i).byteValue();
        return b;
    }

    //===============================================================
    //===============================================================
    private static String privateKey = "TangoControl";

    public static String cryptPassword(String inStr) {
        byte[] mask = privateKey.getBytes();
        byte[] in = inStr.getBytes();
        int len = (in.length < mask.length) ? in.length : mask.length;
        byte[] out = new byte[len];

        for (int i = 0; i < in.length && i < privateKey.length(); i++) {
            out[i] = (byte) (in[i] ^ mask[i]);
        }

        return bytes2str(out);
    }

    //===============================================================
    //===============================================================
    public static String decryptPassword(String inStr) {
        byte[] mask = privateKey.getBytes();
        byte[] in = str2bytes(inStr);
        int len = (in.length < mask.length) ? in.length : mask.length;
        byte[] out = new byte[len];

        for (int i = 0; i < in.length && i < privateKey.length(); i++) {
            out[i] = (byte) (in[i] ^ mask[i]);
        }

        return new String(out);
    }
}
