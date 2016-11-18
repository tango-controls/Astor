//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author: verdier $
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
// $Revision:  $
//
//-======================================================================


package admin.astor.statistics;

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

/**
 * Execute a ResetStatistics on all Controlled starter
 *
 * @author Pascal Verdier
 */
public class ResetStatistics {
    private int nbHosts = 0;
    private int done = 0;
    private JFrame parent;

    //=======================================================
    //=======================================================
    public ResetStatistics(JFrame parent) {
        this.parent = parent;

        List<String> hosts = Utils.getHostControlledList(true, false);
        StringBuffer failed = new StringBuffer();
        if (getConfirm(hosts)) {
            nbHosts = hosts.size();

            if (parent != null)
                AstorUtil.startSplash("Statistics ");
            for (String host : hosts) {
                if (parent != null) {
                    int ratio = 100 / nbHosts;
                    if (ratio < 1)
                        ratio = 1;
                    AstorUtil.increaseSplashProgress(ratio, "Resetting " + host);
                }
                try {
                    //  Check if host or starter name
                    String devName = host;
                    if (host.indexOf('/') < 0)
                        devName = AstorUtil.getStarterDeviceHeader() + host;
                    System.out.println("Resetting " + host);
                    DeviceProxy dev = new DeviceProxy(devName);
                    dev.command_inout("ResetStatistics");
                    done++;

                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {/* */}
                } catch (DevFailed e) {
                    failed.append(host).append(":    ").append(e.errors[0].desc).append("\n");
                }
            }
        }
        if (parent != null)
            AstorUtil.stopSplash();

        //  Display error if any
        if (failed.length() > 0) {
            if (parent == null)
                System.err.println(failed);
            else {
                JOptionPane.showMessageDialog(parent,
                        failed.toString(),
                        "error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }


        //  Display results
        if (parent != null) {
            JOptionPane.showMessageDialog(parent,
                    this,
                    "Command done",
                    JOptionPane.INFORMATION_MESSAGE);
        } else
            System.out.println(this);
    }

    //=======================================================
    //=======================================================
    private boolean getConfirm(List<String> hosts) {
        if (parent != null) {

            return JOptionPane.showConfirmDialog(parent,
                    "Reset Statistics on " + hosts.size() + " hosts ?",
                    "Confirm Dialog",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION;

        } else {
            try {
                System.out.println("OK to reset statistics on " + hosts.size() + " (y/n) ?");
                byte[] b = new byte[100];
                if (System.in.read(b) > 0)
                    if (b[0] == 'y')
                        return true;
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        return false;
    }

    //=======================================================
    //=======================================================
    public String toString() {
        return "ResetStatistics done for " + done + " hosts / " + nbHosts;

    }

    //=======================================================
    //=======================================================
    public static void main(String[] args) {
        new ResetStatistics(null);
    }
    //=======================================================
    //=======================================================
}
