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

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Class Description: Basic Dialog Class to display info
 *
 * @author Pascal Verdier
 */
public class EditDialog extends JDialog {
    private int retVal = JOptionPane.OK_OPTION;

    static final int CHECK_ACCESS = 0;
    static final int EDIT_USER = 1;
    static final int CLONE_USER = 2;
    private int mode = EDIT_USER;

    private static String[] titles;
    private static final String[] check_titles = {"User Name", "IP Address", "Device"};
    private static final String[] edit_titles = {"User Name", "Allowed Address", "Device"};
    private static final String[] clone_titles = {"User Name"};
    static final int USER = 0;
    static final int ADDRESS = 1;
    static final int DEVICE = 2;

    private AccessProxy accessProxy;
    private JTextField[] textFields;
    private JLabel checkResultLabel;
    private JLabel[] labels;
    private JComboBox<UserGroup>   groupBox;
    //===============================================================
    /**
     * Creates new form EditDialog
     *
     * @param parent  JFrame parent instance
     * @param user    default name for users
     * @param address default address
     * @param groups groups of user list
     * @param defaultGroup group selection
     */
    //===============================================================
    public EditDialog(JFrame parent, String user,
                      String address, List<UserGroup> groups, UserGroup defaultGroup) {
        super(parent, true);
        mode = (address==null)? CLONE_USER : EDIT_USER;
        titles = (address==null)? clone_titles : edit_titles;
        initComponents();
        initOwnComponents(titles);

        //  Add a JComboBox for groups
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        groupBox = new JComboBox<>();
        for (UserGroup group : groups) {
            groupBox.addItem(group);
        }
        groupBox.setEditable(true);
        gbc.gridx = 1;
        gbc.gridy = 0;
        centerPanel.add(groupBox, gbc);
        gbc.gridx = 0;
        centerPanel.add(new JLabel("Group:"), gbc);
        if (defaultGroup!=null)
            groupBox.setSelectedItem(defaultGroup);
        else
            groupBox.setSelectedItem("");

        //  Customize for edition
        if (mode!=CLONE_USER) {
            labels[DEVICE].setVisible(false);
            textFields[DEVICE].setVisible(false);
            textFields[USER].setText(user);
            textFields[ADDRESS].setText(address);
        }
        centerDialog(parent);
        textFields[USER].requestFocus();
    }
    //===============================================================
    /**
     * Creates new form EditDialog
     *
     * @param parent     JFrame parent instance
     * @param accessProxy access device parameters
     */
    //===============================================================
    public EditDialog(JFrame parent, AccessProxy accessProxy) {
        super(parent, true);
        mode = CHECK_ACCESS;
        titles = check_titles;
        this.accessProxy = accessProxy;
        initComponents();
        initOwnComponents(titles);
        centerDialog(parent);
    }

    //===============================================================
    //===============================================================
    private void centerDialog(JFrame parent) {

        pack();
        //	Center dialog
        Point p = parent.getLocationOnScreen();
        p.x += ((parent.getWidth() - this.getWidth()) / 2);
        p.y += ((parent.getHeight() - this.getHeight()) / 2);
        if (p.y <= 0) p.y = 20;
        if (p.x <= 0) p.x = 20;
        this.setLocation(p);
    }
    //===============================================================
    //===============================================================
    private void initOwnComponents(String[] titles) {
        GridBagConstraints gbc = new GridBagConstraints();

        textFields = new JTextField[titles.length];
        labels = new JLabel[titles.length];
        int i;
        for (i=0; i<titles.length ; i++) {
            labels[i] = new JLabel(titles[i] + ":  ");
            gbc.gridx = 0;
            gbc.gridy = i+1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            centerPanel.add(labels[i], gbc);

            textFields[i] = new JTextField();
            textFields[i].setColumns(20);
            textFields[i].addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    textKeyPressed(evt);
                }
            });
            gbc.gridx = 1;
            gbc.gridy = i+1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            centerPanel.add(textFields[i], gbc);
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

        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        okBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        centerPanel = new javax.swing.JPanel();

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
        bottomPanel.add(okBtn);

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        centerPanel.setLayout(new java.awt.GridBagLayout());
        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    private boolean checkInputs() {
        String user = textFields[USER].getText().trim().toLowerCase();
        textFields[USER].setText(user);
        
        if (mode!=CHECK_ACCESS) {
            String grpName = groupBox.getSelectedItem().toString();
            if (grpName.startsWith("All") || grpName.endsWith("Users")) {
                admin.astor.tools.Utils.popupError(this, "Incoherent group name !");
                return false;
            }
            if (mode==EDIT_USER && grpName.length()==0) {
                admin.astor.tools.Utils.popupError(this, "Group name ?");
                return false;
            }
        }
        if (user.length()==0) {
            admin.astor.tools.Utils.popupError(this, "User name NOT defined !");
            return false;
        }

        if (mode!=CLONE_USER) {
            String address = textFields[ADDRESS].getText().trim();
            String device  = textFields[DEVICE].getText().trim().toLowerCase();
            if (address.length()==0 || (mode==CHECK_ACCESS && device.length()==0) ) {
                admin.astor.tools.Utils.popupError(this, "Please fill all fields !");
                return false;
            }
            //  set after trim
            textFields[DEVICE].setText(device);
            textFields[ADDRESS].setText(address);
            //	Check if host name as address
            try {
                java.net.InetAddress iadd =
                        java.net.InetAddress.getByName(address);

                //	If found replace by address
                address = iadd.getHostAddress();
                textFields[ADDRESS].setText(address);
            } catch (Exception e) { /* */ }

            //  Check dev name
            List<String> tokens = new ArrayList<>();
            if (mode == CHECK_ACCESS) {
                //	Try to split with '.' separator
                StringTokenizer stk = new StringTokenizer(device, "/");
                while (stk.hasMoreTokens())
                    tokens.add(stk.nextToken());
                if (tokens.size() > 3) {
                    admin.astor.tools.Utils.popupError(this, "Incorrect device name  (too many members)");
                    return false;
                }
                if (tokens.size() < 3) {
                    admin.astor.tools.Utils.popupError(this, "Incorrect device name  (not enough members)");
                    return false;
                }
            }
            //  check IP add name
            StringTokenizer stk1 = new StringTokenizer(address, ".");
            tokens.clear();
            while (stk1.hasMoreTokens())
                tokens.add(stk1.nextToken());
            if (tokens.size() > 4) {
                admin.astor.tools.Utils.popupError(this, "Incorrect IP address  (Too many members)");
                return false;
            } else if (tokens.size() < 4) {
                admin.astor.tools.Utils.popupError(this, "Incorrect IP address  (not enougth members)");
                return false;
            }
            //	rebuild add string to be sure that there is no too much '.'
            //		like xxx.xxx....xx....xx
            address = tokens.get(0) + "." + tokens.get(1) + "." + tokens.get(2) + "." + tokens.get(3);
            textFields[ADDRESS].setText(address);

            for (int i = 0; i < tokens.size(); i++) {
                //  Check if numbers
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Short.parseShort(tokens.get(i));
                } catch (NumberFormatException e) {
                    //  Or if wildcard
                    if (!tokens.get(i).equals("*")) {
                        admin.astor.tools.Utils.popupError(this,
                                "Incorrect IP address  (member #" + (i + 1) +
                                " (" + tokens.get(i) + ") is not a number)");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //===============================================================
    //===============================================================
    private void checkAccess() {
        try {
            //  Check security
            String result = accessProxy.getAccess(getInputs());
            checkResultLabel.setText(result);
            if (result.equals("read"))
                checkResultLabel.setIcon(UsersTree.read_icon);
            else
                checkResultLabel.setIcon(UsersTree.write_icon);
        } catch (DevFailed e) {
            fr.esrf.tangoatk.widget.util.ErrorPane.showErrorMessage(this, "Cannot check TANGO Access", e);
        }
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void textKeyPressed(java.awt.event.KeyEvent evt) {
        int c = (int) evt.getKeyChar();
        //System.out.println("textInputChanged() " + c);
        switch (c) {
            case 27:
                //  Equivalent to cancel
                retVal = JOptionPane.CANCEL_OPTION;
                doClose();
                break;
            case 10:
                switch (mode) {
                    case EDIT_USER:
                    case CLONE_USER:
                        //  Equivalent to okBtn
                        if (checkInputs()) {
                            retVal = JOptionPane.OK_OPTION;
                            doClose();
                        }
                        break;
                    case CHECK_ACCESS:
                        if (checkInputs())
                            checkAccess();
                        break;
                }
                break;
            default:
                if (mode == CHECK_ACCESS) {
                    checkResultLabel.setText("...");
                    checkResultLabel.setIcon(null);
                }
        }
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed
        if (checkInputs()) {
            retVal = JOptionPane.OK_OPTION;
            //  do not close if test mode
            if (mode == CHECK_ACCESS)
                checkAccess();
            else
                doClose();
        }
    }//GEN-LAST:event_okBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        retVal = JOptionPane.CANCEL_OPTION;
        doClose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        retVal = JOptionPane.CANCEL_OPTION;
        doClose();
    }//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    private void doClose() {
        setVisible(false);
        dispose();
    }

    //===============================================================
    //===============================================================
    public String[] getInputs() {
        String[] val = new String[textFields.length];
        for (int i = 0; i < textFields.length; i++)
            val[i] = textFields[i].getText().trim();
        return val;
    }
    //===============================================================
    //===============================================================
    public UserGroup getUserGroup() {
        Object  selectedItem = groupBox.getSelectedItem();
        if (selectedItem instanceof UserGroup) {
            return (UserGroup) selectedItem;
        }
        else {
            String  name = selectedItem.toString();
            if (name.length()==0)
                return null;
            return new UserGroup(name);
        }
    }
    //===============================================================
    //===============================================================
    public int showDialog() {
        if (mode == CHECK_ACCESS) {
            try {
                textFields[USER].setText(System.getProperty("user.name").toLowerCase());
                textFields[ADDRESS].setText(InetAddress.getLocalHost().getHostAddress());
                okBtn.setText("Check");

                //  Add a button to display check result
                GridBagConstraints gbc = new GridBagConstraints();
                checkResultLabel = new JLabel("...");
                gbc.gridx = 1;
                gbc.gridy = titles.length+1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                centerPanel.add(checkResultLabel, gbc);
                pack();
            } catch (UnknownHostException e) {
                admin.astor.tools.Utils.popupError(this, null, e);
            }
        }
        setVisible(true);
        return retVal;
    }


    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JButton okBtn;
    // End of variables declaration//GEN-END:variables
    //===============================================================

}
