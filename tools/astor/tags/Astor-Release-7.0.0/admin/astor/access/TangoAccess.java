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

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;

/**
 * Class Description: Basic JFrame Class to display info
 *
 * @author Pascal Verdier
 */
public class TangoAccess extends JFrame {
    String accessDeviceName;
    UsersTree usersTree;
    /**
     * Access proxy instance
     */
    private AccessProxy accessProxy;

    /**
     * Dialog to check access
     */
    private EditDialog checkDialog = null;

    private JFrame parent;

    static private final Dimension pane_size = new Dimension(350, 500);
    //=======================================================
    /**
     * Creates new form TangoAccess
     *
     * @param parent JFrame parent instance
     * @throws fr.esrf.Tango.DevFailed If no AccessControl service or connection failed on it.
     */
    //=======================================================
    public TangoAccess(JFrame parent) throws DevFailed {
        this.parent = parent;
        try {
            AstorUtil.startSplash("TangoAccess ");
            AstorUtil.increaseSplashProgress(5, "Reading database");
            String test = System.getenv("AccessControl");
            if (test == null)
                getTangoService();
            else
                accessDeviceName = test;

            initComponents();
            initOwnComponents();
            AstorUtil.increaseSplashProgress(5, "Finalize GUI");
            ImageIcon icon = Utils.getInstance().getIcon("TangoClass.gif");
            setIconImage(icon.getImage());
            this.setTitle("Tango Access Control Manager");

            if (isSuperUser())
                superUserLabel.setVisible(true);
            else {
                if (accessProxy.getAccessControl() == TangoConst.ACCESS_READ) {
                    superUserLabel.setVisible(true);
                    superUserLabel.setText("Read Only Mode !");
                    superUserLabel.setForeground(Color.red);
                } else
                    superUserLabel.setVisible(false);
            }

            pack();
            if (parent.getWidth() > 0)    //	has parent
            {
                //	cascade window
                Point p = parent.getLocationOnScreen();
                p.x += 100;
                p.y += 100;
                setLocation(p);
            } else
                ATKGraphicsUtils.centerFrameOnScreen(this);
            AstorUtil.stopSplash();
        } catch (DevFailed e) {
            AstorUtil.stopSplash();
            throw e;
        }
    }

    //===========================================================
    //===========================================================
    private static boolean isSuperUser() {
        boolean super_tango = false;
        String str = System.getProperty("SUPER_TANGO");
        if (str != null) {
            if (str.toLowerCase().equals("true"))
                super_tango = true;
        } else {
            str = System.getenv("SUPER_TANGO");
            if (str != null) {
                if (str.toLowerCase().equals("true"))
                    super_tango = true;
            }
        }
        return super_tango;
    }

    //===========================================================
    //===========================================================
    private void getTangoService() throws DevFailed {
        //  Get TangoAccess service and check if exist
        String[] services =
                ApiUtil.get_db_obj().getServices("AccessControl", "tango");
        if (services.length == 0)
            Except.throw_communication_failed("Service_DoesNotExist",
                    "There is no AccessControl service defined !",
                    "TangoAccess.TangoAccess()");
        accessDeviceName = services[0];
    }

    //===========================================================
    //===========================================================
    private void initOwnComponents() throws DevFailed {
        //	File menu
        fileMenu.setMnemonic('F');
        checkAccessBtn.setMnemonic('T');
        checkAccessBtn.setAccelerator(KeyStroke.getKeyStroke('T', Event.CTRL_MASK));
        exitBtn.setMnemonic('E');
        exitBtn.setAccelerator(KeyStroke.getKeyStroke('Q', Event.CTRL_MASK));

        //	Action menu
        actionMenu.setMnemonic('A');
        registerItem.setMnemonic('R');
        findItem.setMnemonic('R');
        findItem.setAccelerator(KeyStroke.getKeyStroke('F', Event.CTRL_MASK));

        //	Help menu
        helpMenu.setMnemonic('H');
        principleItem.setMnemonic('P');
        principleItem.setAccelerator(KeyStroke.getKeyStroke('P', Event.CTRL_MASK));

        //	Check if write allowed
        accessProxy = new AccessProxy(accessDeviceName);
        if (accessProxy.getAccessControl() == TangoConst.ACCESS_READ) {
            checkAccessBtn.setEnabled(false);
            actionMenu.setEnabled(false);
        }

        //	Build tabbed pane title
        tabbedPane.setTitleAt(0, "Users");
        tabbedPane.setTitleAt(1, "Allowed Cmd");

        //	Build users_tree to display users rights
        usersTree = new UsersTree(this, accessProxy);
        JScrollPane scrowllPane = new JScrollPane();
        scrowllPane.setViewportView(usersTree);
        usersPanel.add(scrowllPane, BorderLayout.CENTER);

        //	Build users_tree to display users rights
        AllowedCmdTree cmd_tree = new AllowedCmdTree(this, accessProxy);
        scrowllPane = new JScrollPane();
        scrowllPane.setViewportView(cmd_tree);
        cmdClassPanel.add(scrowllPane, BorderLayout.CENTER);

        //	Add a panel to display icons.
        JLabel lbl = new JLabel("Devices: ");
        JPanel panel = new JPanel();
        panel.add(lbl);

        lbl = new JLabel("Read/Write");
        lbl.setIcon(Utils.getInstance().getIcon("greenbal.gif"));
        panel.add(lbl);

        lbl = new JLabel("Read Only");
        lbl.setIcon(Utils.getInstance().getIcon("redball.gif"));
        panel.add(lbl);
        usersPanel.add(panel, BorderLayout.SOUTH);

        tabbedPane.setPreferredSize(pane_size);
    }
    //=======================================================

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    //=======================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        usersPanel = new javax.swing.JPanel();
        cmdClassPanel = new javax.swing.JPanel();
        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        superUserLabel = new javax.swing.JLabel();
        javax.swing.JMenuBar jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        checkAccessBtn = new javax.swing.JMenuItem();
        javax.swing.JMenuItem passwordItem = new javax.swing.JMenuItem();
        exitBtn = new javax.swing.JMenuItem();
        actionMenu = new javax.swing.JMenu();
        findItem = new javax.swing.JMenuItem();
        registerItem = new javax.swing.JRadioButtonMenuItem();
        helpMenu = new javax.swing.JMenu();
        principleItem = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        usersPanel.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("tab1", usersPanel);

        cmdClassPanel.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("tab2", cmdClassPanel);

        getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

        superUserLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        superUserLabel.setText("You are Super User !");
        topPanel.add(superUserLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.PAGE_START);

        fileMenu.setText("File");

        checkAccessBtn.setText("Test Tango Acces");
        checkAccessBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAccessBtnActionPerformed(evt);
            }
        });
        fileMenu.add(checkAccessBtn);

        passwordItem.setText("Change Password");
        passwordItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordItemActionPerformed(evt);
            }
        });
        fileMenu.add(passwordItem);

        exitBtn.setText("Exit");
        exitBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitBtnActionPerformed(evt);
            }
        });
        fileMenu.add(exitBtn);

        jMenuBar1.add(fileMenu);

        actionMenu.setText("Action");
        actionMenu.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                actionMenuItemStateChanged(evt);
            }
        });

        findItem.setText("Find User");
        findItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findItemActionPerformed(evt);
            }
        });
        actionMenu.add(findItem);

        registerItem.setText("Register Service");
        registerItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerItemActionPerformed(evt);
            }
        });
        actionMenu.add(registerItem);

        jMenuBar1.add(actionMenu);

        helpMenu.setText("help");

        principleItem.setText("On Principle");
        principleItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                principleItemActionPerformed(evt);
            }
        });
        helpMenu.add(principleItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void actionMenuItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_actionMenuItemStateChanged
        if (!actionMenu.isSelected())
            return;
        try {
            String[] services =
                    ApiUtil.get_db_obj().getServices(TangoConst.ACCESS_SERVICE, "tango");
            registerItem.setSelected(services.length != 0);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this,
                    "Cannot start TangoAccess class", e);
        }
    }//GEN-LAST:event_actionMenuItemStateChanged

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void registerItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerItemActionPerformed
        try {
            boolean b = (registerItem.getSelectedObjects() != null);
            accessProxy.registerService(b);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this,
                    "Cannot start TangoAccess class", e);
        }
    }//GEN-LAST:event_registerItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void checkAccessBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAccessBtnActionPerformed
        if (checkDialog == null) {
            checkDialog = new EditDialog(this, accessProxy);
            checkDialog.showDialog();
        } else
            checkDialog.setVisible(true);
    }//GEN-LAST:event_checkAccessBtnActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void exitBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitBtnActionPerformed
        doClose();
    }//GEN-LAST:event_exitBtnActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        doClose();
    }//GEN-LAST:event_exitForm

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void principleItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_principleItemActionPerformed
        admin.astor.tools.Utils.popupMessage(this,
                "This access management is necessary only if the\n" +
                        "    \"AccessControl/tango\"\n" +
                        "    Tango service has been installed.\n\n" +
                        "By default all devices are forbiden for all users.\n" +
                        "And the rights will be opened for [user, address, device].\n\n" +
                        "This tool is able to define WRITE access \n" +
                        "    on devices for a TANGO control system\n\n" +
                        "You can define for a specified user:\n" +
                        "    - Allowed addresses to write devices\n" +
                        "    - Set devices acces to  READ_WRITE or READ_ONLY");
    }//GEN-LAST:event_principleItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void passwordItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordItemActionPerformed
        boolean ok = false;
        while (!ok) {
            //  Get a new password
            PasswordDialog dialog = new PasswordDialog(this, "New Password ?");
            if (dialog.showDialog() == JOptionPane.OK_OPTION) {
                String password = dialog.getPassword();
                //  Get it a second time
                dialog = new PasswordDialog(this, "Confirm New Passsword:");
                if (dialog.showDialog() == JOptionPane.OK_OPTION) {
                    String password2 = dialog.getPassword();
                    //  Check them (they must be equals
                    if (password.equals(password2)) {
                        try {
                            savePassword(password);
                        } catch (DevFailed e) {
                            ErrorPane.showErrorMessage(this, null, e);
                        }
                        ok = true;
                    } else
                        popupError(this, "Passwords are not equals !");
                } else
                    return;
            } else
                return;
        }
    }//GEN-LAST:event_passwordItemActionPerformed

    //=======================================================
    //=======================================================
    private String userName;

    @SuppressWarnings({"UnusedDeclaration"})
    private void findItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findItemActionPerformed
        //	Ask for user name
        String str = (String) JOptionPane.showInputDialog(this,
                "Host Name ?",
                "Input Dialog",
                JOptionPane.INFORMATION_MESSAGE,
                null, null, userName);
        if (str != null) {
            userName = str;
            usersTree.findUser(userName);
        }
    }//GEN-LAST:event_findItemActionPerformed

    //=======================================================
    //=======================================================
    private static String getPasswordFromDatabase() throws DevFailed {
        //   If not super user check access with password
        DbDatum datum = ApiUtil.get_db_obj().get_property("Astor", "access");
        String encoded = "???";
        if (!datum.is_empty())
            encoded = datum.extractString();
        return PasswordDialog.decryptPassword(encoded);
    }

    //=======================================================
    //=======================================================
    private static void savePassword(String password) throws DevFailed {
        DbDatum datum = new DbDatum("access");
        datum.insert(PasswordDialog.cryptPassword(password));
        ApiUtil.get_db_obj().put_property("Astor", new DbDatum[]{datum});
    }

    //=======================================================
    //=======================================================
    public static int checkPassword(JFrame parent) throws DevFailed {
        if (isSuperUser())
            return JOptionPane.OK_OPTION;

        String password = getPasswordFromDatabase();
        PasswordDialog dialog = new PasswordDialog(parent, password.getBytes());
        return dialog.showDialog();
    }

    //=======================================================
    //=======================================================
    private void doClose() {
        if (parent.getWidth() > 0)
            setVisible(false);
        else
            System.exit(0);
    }

    //=======================================================

    /**
     * @param args the command line arguments
     */
    //=======================================================
    public static void main(String args[]) {
        try {
            //  Check if display password
            if (args.length > 0) {
                if (args[0].equals("-???")) {
                    System.out.println(getPasswordFromDatabase());
                    System.exit(0);
                }
            }

            //  Get the password and display tool
            if (TangoAccess.checkPassword(new JFrame()) == JOptionPane.OK_OPTION) {
                new TangoAccess(new JFrame()).setVisible(true);
            } else
                System.exit(0);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(new JFrame(),
                    "Cannot start TangoAccess class", e);
            System.exit(0);
        } catch (InternalError | HeadlessException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    //===============================================================
    //===============================================================
    public static void popupError(Component component, String message) {
        try {
            throw new Exception(message);
        } catch (Exception e) {
            ErrorPane.showErrorMessage(component, null, e);
        }
    }


    //=======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu actionMenu;
    private javax.swing.JMenuItem checkAccessBtn;
    private javax.swing.JPanel cmdClassPanel;
    private javax.swing.JMenuItem exitBtn;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem findItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem principleItem;
    private javax.swing.JRadioButtonMenuItem registerItem;
    private javax.swing.JLabel superUserLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel usersPanel;
    // End of variables declaration//GEN-END:variables
    //=======================================================

}
