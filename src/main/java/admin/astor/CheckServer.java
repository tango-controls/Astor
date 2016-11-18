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
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;

/**
 *
 * @author verdier
 */
public class CheckServer {
    private static final String[] actions = {
            "start",
            "stop",
            "restart",
            "ping"
    };
    private static String starter_name;
    private static String servname;
    private static String action;
    private static DeviceProxy starter;

    //===============================================================
    //===============================================================
    public CheckServer(String[] args) throws DevFailed {
        if (!checkSyntax(args))
            System.exit(0);
        starter = new DeviceProxy(starter_name);
    }

    //===============================================================
    //===============================================================
    public void doAction() throws DevFailed {
        switch (action) {
            case "start":
                execute("DevStart");
                System.out.println(servname + " started");
                break;
            case "stop":
                execute("DevStop");
                System.out.println(servname + " stopped");
                break;
            case "restart":
                restart();
                System.out.println(servname + " restarted");
                break;
            case "ping":
                new DeviceProxy("dserver/" + servname).ping();
                System.out.println(servname + " is alive");
                break;
        }
    }

    //===============================================================
    //===============================================================
    private void execute(String cmd) throws DevFailed {
        DeviceData argin = new DeviceData();
        argin.insert(servname);
        starter.command_inout(cmd, argin);
    }

    //===============================================================
    //===============================================================
    private void restart() throws DevFailed {
        //	Stop it (do no throw exception if already stopped)
        try {
            execute("DevStop");
            System.out.println(servname + " stopped");
        } catch (DevFailed e) {
            if (!e.errors[0].reason.contains("NOT running !"))
                throw e;
        }

        //	Wait to be sure it is stopped
        int nb_retries = 5;
        for (int i = 0; i < nb_retries; i++) {
            sleep(1000);
            DeviceData argin = new DeviceData();
            argin.insert(true);
            DeviceData argout = starter.command_inout("DevGetStopServers", argin);
            String[] servers = argout.extractStringArray();
            for (String server : servers) {
                if (server.equals(servname))
                    i = nb_retries;
            }
        }

        //	Restart it
        execute("DevStart");
    }

    //===============================================================
    //===============================================================
    private synchronized void sleep(long ms) {
        try {
            wait(ms);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }


    //===============================================================
    //===============================================================
    private void displaySyntax(String script_name) {
        //	remove script path
        int start;
        if ((start = script_name.lastIndexOf("/")) > 0)
            script_name = script_name.substring(start + 1);
        System.out.println("Syntax :");
        System.out.println(script_name + "  hosname  servname  action");
        System.out.println("	hosname  : Host name where server running");
        System.out.println("	servname : Server name (e.g. VacGauge/sr_c02)");
        System.out.print("	action   : ");
        for (int i = 0; i < actions.length; i++) {
            if (i > 0)
                System.out.print(" / ");
            System.out.print(actions[i]);
        }
        System.out.println();
    }

    //===============================================================
    //===============================================================
    private boolean checkSyntax(String[] args) {
        if (args.length < 4) {
            displaySyntax(args[0]);
            return false;
        }
        starter_name = AstorUtil.getStarterDeviceHeader() + args[1];
        servname = args[2];
        action = args[3].toLowerCase();

        //	Check server name
        if (!servname.contains("/")) {
            System.out.println("Server name syntax error !");
            displaySyntax(args[0]);
            return false;
        }

        //	Check action name
        boolean found = false;
        for (int i = 0; !found && i < actions.length; i++)
            found = action.equals(actions[i]);
        if (!found) {
            System.out.println("action name syntax error !");
            displaySyntax(args[0]);
            return false;
        }
        return true;
    }

    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        try {
            CheckServer client = new CheckServer(args);
            client.doAction();
        } catch (DevFailed e) {
            Except.print_exception(e);
        }
    }
}
