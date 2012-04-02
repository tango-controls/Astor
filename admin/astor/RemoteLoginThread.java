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
 *	This class is a thread to open a window with a
 *	remote login to a remote host.
 *
 * @author verdier
 */


import javax.swing.*;
import java.awt.*;

public class RemoteLoginThread extends Thread implements AstorDefs {
    private Component parent;
    private String hostname;

    //======================================================================

    /**
     * Thread constructor.
     *
     * @param    hostname    Host to do the remote login.
     * @param    parent        parent component used to display error message.
     */
    //======================================================================
    public RemoteLoginThread(String hostname, Component parent) {
        this.hostname = hostname;
        this.parent = parent;
    }


    //======================================================================

    /**
     * Running thread method.
     */
    //======================================================================
    public void run() {
        String cmd = "xterm -sb -title " + hostname + "";


        //	Check if rlogin user is defined
        String remlog = AstorUtil.getRloginCmd();
        String user = AstorUtil.getRloginUser();
        if (remlog == null || remlog.length() == 0) {
            if (user == null || user.length() == 0)
                cmd += "  -e telnet " + hostname;
            else
                remlog = "  -e rlogin  " + hostname;
        } else {
            //	Check if rlogin command (with or without user)
            if (remlog.equals("rlogin")) {
                if (user == null || user.length() == 0)
                    cmd += "  -e  rlogin " + hostname;
                else
                    cmd += "_" + user + "  -e  rlogin -l " + user + "  " + hostname;
            } else
                //	Check if ssh command (with or without user)
                if (remlog.startsWith("ssh")) {
                    if (user == null || user.length() == 0)
                        cmd += "  -e  ssh -X " + hostname;
                    else
                        cmd += "_" + user + "  -e  ssh -X " + user + "@" + hostname;
                } else {
                    JOptionPane.showMessageDialog(parent,
                            "Command : " + remlog + " Not Managed !",
                            "Error Window",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

        }

        try {
            //	Execute
            Process proc = Runtime.getRuntime().exec(cmd);
            proc.waitFor();
        } catch (Exception ex) {
            System.out.println(ex);
            JOptionPane.showMessageDialog(parent,
                    ex.toString(),
                    "Error Window",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
