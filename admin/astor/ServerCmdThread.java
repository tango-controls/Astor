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
    private boolean from_array = true;
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
    public ServerCmdThread(Component parent, TangoHost[] hosts, int cmd) {
        this.parent = parent;
        this.hosts = hosts;
        this.cmd = cmd;
        monitor_title = " on all controlled hosts   ";

        nbStartupLevels = AstorUtil.getStarterNbStartupLevels();
        levelUsed = new boolean[nbStartupLevels];
        for (int i = 0; i < nbStartupLevels; i++)
            levelUsed[i] = true;
    }
    //=======================================================
    /**
     * Thread Constructor for one host.
     *
     * @param    parent The application parent used as parent
     *                          for ProgressMonitor.
     * @param    host   The controlled host.
     * @param    cmd    command to be executed on all hosts.
     * @param    levelUsed    true if level is used by server on this host.
     */
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    public ServerCmdThread(Component parent, TangoHost host, int cmd, boolean[] levelUsed) {
        this.parent = parent;

        this.hosts = new TangoHost[1];
        this.hosts[0] = host;
        this.cmd = cmd;
        this.levelUsed = levelUsed;
        monitor_title = " on " + host + "   ";
        nbStartupLevels = AstorUtil.getStarterNbStartupLevels();
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
    public ServerCmdThread(Component parent, TangoHost host, int cmd, List<Integer> levels) {
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
    public ServerCmdThread(Component parent, TangoHost host, int cmd, List<Integer> levels, boolean confirm) {
        this.parent = parent;

        this.hosts = new TangoHost[1];
        this.hosts[0] = host;
        this.cmd = cmd;
        this.levels = levels;
        this.confirm = confirm;
        monitor_title = " on " + host + "   ";
        nbStartupLevels = AstorUtil.getStarterNbStartupLevels();
        from_array = false;
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

        //	For each startup level
        //	(Increase for start or decrease for stop)
        if (from_array) {
            switch (cmd) {
                case StartAllServers:
                    for (int level=1 ; !monitor.isCanceled() && level<=nbStartupLevels; level++) {
                        if (levelUsed[level - 1]) {
                            if (confirm) {
                                int option = JOptionPane.showConfirmDialog(parent,
                                        cmdStr[cmd] + " for level " + level,
                                        "",
                                        JOptionPane.YES_NO_CANCEL_OPTION);

                                if (option == JOptionPane.CANCEL_OPTION)
                                    level = nbStartupLevels;
                                else {
                                    if (option == JOptionPane.OK_OPTION)
                                        executeCommand(hosts, level);
                                }
                            }
                            else
                                executeCommand(hosts, level);
                        }
                    }
                    break;

                case StopAllServers:
                    for (int level = nbStartupLevels; !monitor.isCanceled() && level>0 ; level--) {
                        if (levelUsed[level - 1]) {
                            int option = JOptionPane.showConfirmDialog(parent,
                                    cmdStr[cmd] + " for level " + level,
                                    "",
                                    JOptionPane.YES_NO_CANCEL_OPTION);
                            if (option == JOptionPane.CANCEL_OPTION)
                                level = 0;
                            else {
                                if (option == JOptionPane.OK_OPTION)
                                    executeCommand(hosts, level);
                            }
                        }
                    }
                    break;
            }
        }
        else {   //	New version from a vector

            for (int l=0 ; l<levels.size() ; l++) {
                int level = levels.get(l);
                int option = JOptionPane.showConfirmDialog(parent,
                        cmdStr[cmd] + " for level " + level,
                        "",
                        JOptionPane.YES_NO_CANCEL_OPTION);

                switch (option) {
                    case JOptionPane.CANCEL_OPTION:
                        l = levels.size();
                        break;
                    case JOptionPane.OK_OPTION:
                        executeCommand(hosts, level);
                        break;
                    case JOptionPane.NO_OPTION:
                        break;
                }
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
}
