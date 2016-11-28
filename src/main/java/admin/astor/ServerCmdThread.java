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
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 *	This class is a thread to send command to all servers.
 *
 * @author verdier
 */
public class ServerCmdThread extends Thread implements AstorDefs {
    private Component parent;
    private TangoHost[] hosts;
    private int cmd;
    private Monitor monitor;
    private boolean[] levelUsed;
    private short nbStartupLevels;
    private String monitor_title;
    private boolean confirm = true;
    private boolean fromList;
    private List<Integer> levels;

    //=======================================================
    /**
     * Thread Constructor for many hosts.
     *
     * @param    parent The application parent used as parent
     *                          for ProgressMonitor.
     * @param    hosts  The controlled hosts.
     * @param    cmd    command to be executed on all hosts.
     */
    //=======================================================
    public ServerCmdThread(JFrame parent, TangoHost[] hosts, int cmd) {
        this.parent = parent;
        this.hosts = hosts;
        this.cmd = cmd;
        monitor_title = " on all controlled hosts   ";

        nbStartupLevels = AstorUtil.getStarterNbStartupLevels();
        levelUsed = new boolean[nbStartupLevels];
        for (int i = 0; i < nbStartupLevels; i++)
            levelUsed[i] = true;
        fromList = false;
    }
    //=======================================================
    /**
     * Thread Constructor for one host.
     *
     * @param    parent The application parent used as parent
     *                          for ProgressMonitor.
     * @param    host   The controlled host.
     * @param    cmd        command to be executed on all hosts.
     * @param    levels list of levels
     */
    //=======================================================
    public ServerCmdThread(JDialog parent, TangoHost host, int cmd, List<Integer> levels) {
        this(parent, host, cmd, levels, true);
    }
    //=======================================================
    /**
     * Thread Constructor for one host.
     *
     * @param    parent The application parent used as parent
     *                          for ProgressMonitor.
     * @param    host   The controlled host.
     * @param    cmd        command to be executed on all hosts.
     * @param    levels list of levels
     */
    //=======================================================
    public ServerCmdThread(JDialog parent, TangoHost host, int cmd, List<Integer> levels, boolean confirm) {
        this.parent = parent;

        this.hosts = new TangoHost[1];
        this.hosts[0] = host;
        this.cmd = cmd;
        this.levels = levels;
        this.confirm = confirm;
        monitor_title = " on " + host + "   ";
        nbStartupLevels = AstorUtil.getStarterNbStartupLevels();
        fromList = true;
    }
    //=======================================================
    /*
     * Update the ProgressMonitor
     */
    //=======================================================
    private void updateProgressMonitor(int level, int hostIndex, double ratio) {
        String message;
        if (monitor == null) {
            message = cmdStr[cmd] + monitor_title;
            if (parent instanceof JDialog)
                monitor = new Monitor((JDialog) parent, message, cmdStr[cmd]);
            else if (parent instanceof JFrame)
                monitor = new Monitor((JFrame) parent, message, cmdStr[cmd]);
        }

        message = cmdStr[cmd] + "Servers on " +
                hosts[hostIndex].getName() + " for level " + level;

        //System.out.println(hostIndex + " -> " + ratio);
        monitor.setProgressValue(ratio, message);
    }
    //=======================================================
    /**
     * Execute the servers commands.
     */
    //=======================================================
    public void run() {
        //	Initialize from properties
        AstorUtil.getStarterNbStartupLevels();

        //	Start progress monitor
        updateProgressMonitor(0, 0, 0.05);

        //  Build the confirm dialog
        StartStopDialog startStopDialog;
        if (parent instanceof JDialog)
            startStopDialog = new StartStopDialog((JDialog)parent);
        else
            startStopDialog = new StartStopDialog((JFrame) parent);
        startStopDialog.setForAllLevels(!confirm);

        //	For each startup level
        //	(Increase for start or decrease for stop)
        if (fromList) {
            for (int level : levels) {
                if (startStopDialog.doItForAllLevels()) {
                    executeCommand(hosts, level);
                } else {
                    switch (startStopDialog.showDialog(cmdStr[cmd] + " for level " + level + " ?  ")) {
                        case JOptionPane.CANCEL_OPTION:
                            monitor.setProgressValue(100.0);
                            return;
                        case JOptionPane.OK_OPTION:
                            executeCommand(hosts, level);
                            break;
                        case JOptionPane.NO_OPTION:
                            break;
                    }
                }
            }
        }
        else { //   For all levels
            switch (cmd) {
                case StartAllServers:
                    for (int level=1 ; !monitor.isCanceled() && level<=nbStartupLevels; level++) {
                        if (levelUsed[level - 1]) {
                            if (startStopDialog.doItForAllLevels()) {
                                executeCommand(hosts, level);
                            }
                            else {
                                switch (startStopDialog.showDialog(cmdStr[cmd] + " for level " + level + " ?  ")) {
                                    case JOptionPane.CANCEL_OPTION :
                                        level = nbStartupLevels;
                                        break;
                                    case JOptionPane.OK_OPTION:
                                        executeCommand(hosts, level);
                                        break;
                                    case JOptionPane.NO_OPTION:
                                        break;
                                }
                            }
                        }
                    }
                    break;

                case StopAllServers:
                    for (int level = nbStartupLevels; !monitor.isCanceled() && level>0 ; level--) {
                        if (levelUsed[level - 1]) {
                            if (startStopDialog.doItForAllLevels()) {
                                executeCommand(hosts, level);
                            }
                            else {
                                switch (startStopDialog.showDialog(cmdStr[cmd] + " for level " + level)) {
                                    case JOptionPane.CANCEL_OPTION:
                                        level = 0;
                                        break;
                                    case JOptionPane.OK_OPTION:
                                        executeCommand(hosts, level);
                                        break;
                                    case JOptionPane.NO_OPTION:
                                        break;
                                }
                            }
                        }
                    }
                    break;
            }
        }
        monitor.setProgressValue(100.0);
    }

    //============================================================
    //============================================================
    @SuppressWarnings({"NestedTryStatement"})
    private void executeCommand(TangoHost[] hosts, int level) {
        //	For each host
        for (int i=0 ; !monitor.isCanceled() && i<hosts.length ; i++) {
            TangoHost host = hosts[i];
            double ratio;

            //----------------------------
            //	And Execute the command
            //----------------------------
            try {
                switch (cmd) {
                    case StartAllServers:
                        //	Update the Progress Monitor depends on start/stop
                        ratio = ((double) (level + 1) * hosts.length + i) /
                                (hosts.length * (nbStartupLevels + 2));
                        updateProgressMonitor(level, i, ratio);

                        //	Do command
                        host.startServers(level);
                        //	wait a bit just to display bar graph
                        try { sleep(500); } catch (Exception e) { /* */ }
                        break;

                    case StopAllServers:
                        //	Update the Progress Monitor depends on start/stop
                        ratio = ((double) (nbStartupLevels - level + 1) * hosts.length + i) /
                                (hosts.length * (nbStartupLevels + 2));
                        updateProgressMonitor(level, i, ratio);

                        //	Do command
                        host.stopServers(level);
                        //	wait a bit just to display bar graph
                        try { sleep(50); } catch (Exception e) { /* */ }
                        break;
                }
            } catch (DevFailed e) { /* */ }
            host.updateData();
        }
    }


    //===============================================================
    /**
     *	JDialog Class to ask start/stop at each level
     *  Do not use JOptionPane any more to have a radio button for all levels
     */
    //===============================================================
    private class StartStopDialog extends JDialog {

        private JPanel centerPanel;
        private JLabel titleLabel;
        private JRadioButton forAllLevelsBtn;
        private int returnValue = JOptionPane.OK_OPTION;
        //===============================================================
        private StartStopDialog(JDialog parent) {
            super(parent, true);
            initComponents();
        }
        //===============================================================
        private StartStopDialog(JFrame parent) {
                super(parent, true);
                initComponents();
        }
        //===============================================================
        private boolean doItForAllLevels() {
            return forAllLevelsBtn.isSelected();
        }
        //===============================================================
        public void setForAllLevels(boolean b) {
            forAllLevelsBtn.setSelected(b);
        }
        //===============================================================
        private void initComponents() {
            java.awt.GridBagConstraints gridBagConstraints;

            javax.swing.JPanel topPanel = new javax.swing.JPanel();
            titleLabel = new javax.swing.JLabel();
            centerPanel = new javax.swing.JPanel();
            javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
            forAllLevelsBtn = new javax.swing.JRadioButton();
            javax.swing.JButton yesBtn = new javax.swing.JButton();
            javax.swing.JButton noBtn = new javax.swing.JButton();
            javax.swing.JButton cancelBtn = new javax.swing.JButton();

            addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    closeDialog();
                }
            });

            titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
            titleLabel.setText("Dialog Title");
            titleLabel.setIcon(Utils.getInstance().getIcon("TangoClass.gif", 0.25));
            topPanel.add(titleLabel);

            getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

            centerPanel.setLayout(new java.awt.GridBagLayout());
            getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

            bottomPanel.setLayout(new java.awt.GridBagLayout());

            forAllLevelsBtn.setText("Do it for all levels");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
            bottomPanel.add(forAllLevelsBtn, gridBagConstraints);

            yesBtn.setText("Yes");
            yesBtn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    yesBtnActionPerformed();
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
            bottomPanel.add(yesBtn, gridBagConstraints);

            noBtn.setText("No");
            noBtn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    noBtnActionPerformed();
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
            bottomPanel.add(noBtn, gridBagConstraints);

            cancelBtn.setText("Cancel");
            cancelBtn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cancelBtnActionPerformed();
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
            bottomPanel.add(cancelBtn, gridBagConstraints);

            getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

            pack();
            ATKGraphicsUtils.centerDialog(this);
        }
        //===============================================================
        private void yesBtnActionPerformed() {
            returnValue = JOptionPane.YES_OPTION;
            doClose();
        }
        //===============================================================
        private void noBtnActionPerformed() {
            returnValue = JOptionPane.NO_OPTION;
            doClose();
        }
        //===============================================================
        private void cancelBtnActionPerformed() {
            returnValue = JOptionPane.CANCEL_OPTION;
            doClose();
        }
        //===============================================================
        private void closeDialog() {
            returnValue = JOptionPane.CANCEL_OPTION;
            doClose();
        }
        //===============================================================
        private void doClose() {
            setVisible(false);
            dispose();
        }
        //===============================================================
        public int showDialog(String title) {
            titleLabel.setText(title);
            setVisible(true);
            return returnValue;
        }
        //===============================================================
    }
}
