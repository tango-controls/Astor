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
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DbServer;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *	This class is able to start and stop servers from shell command line.
 *
 * @author verdier
 */
public class AstorCmdLine {
    private int action = NOT_INITIALIZED;
    private TangoHost[] hosts = null;
    private AstorUtil util;

    private static final int NOT_INITIALIZED = -1;
    private static final int START_SERVERS = 0;
    private static final int STOP_SERVERS = 1;
    private static final String[] known_actions = {"start", "stop"};

    //===============================================================
    //===============================================================
    public AstorCmdLine(String[] args) throws Exception {
        util = AstorUtil.getInstance();

        //	Check command line
        manageArgs(args);

        doAction();
    }
    //===============================================================

    /**
     * `Do the action for all levels
     */
    //===============================================================
    private void doAction() {
        int nb_levels = AstorUtil.getStarterNbStartupLevels();
        switch (action) {
            case START_SERVERS:
                for (int level = 1; level <= nb_levels; level++)
                    doAction(level);
                break;
            case STOP_SERVERS:
                for (int level = nb_levels; level >= 1; level--)
                    doAction(level);
                break;
        }
    }
    //===============================================================
    /**
     * Do the action for one level
     * @param level specified level
     */
    //===============================================================
    private void doAction(int level) {
        if (!getConfirm(level)) {
            System.out.println("Skip level " + level);
            return;
        }
        //	Start for all host at this level
        for (TangoHost host : hosts) {
            try {
                switch (action) {
                    case STOP_SERVERS:
                        if (!host.getName().startsWith("crate"))
                            host.stopServers(level);
                        break;

                    case START_SERVERS:
                        if (!host.getName().startsWith("crate"))
                            host.startServers(level);
                        break;
                }
                System.out.println("	done on " + host.getName());
            } catch (DevFailed e) {
                System.out.println("	failed on " + host.getName());
                System.out.println(e.errors[0].desc);
            }
        }
    }
    //===============================================================
    /**
     * Get a confirmation for commands
     * @param level specified level
     * @return true if confirmation is y(es)
     */
    //===============================================================
    private boolean getConfirm(int level) {
        String resp = "";
        System.out.println("\n");
        do {
            System.out.print(known_actions[action] +
                    " all TANGO ds for level " + level + " ?  (y/n) ");
            byte[] b = new byte[80];
            try {
                int nb = System.in.read(b);
                if (nb>0)
                    resp = new String(b).toLowerCase().trim();
            } catch (java.io.IOException e) {
                resp = "no";
            }
        }
        while (!resp.startsWith("n") && !resp.startsWith("y"));

        return (resp.startsWith("y"));
    }

    //===============================================================
    //===============================================================
    private void manageArgs(String[] args) throws Exception {
        //	Check args number
        if (args.length < 3) {
            displaySyntax();
            System.exit(1);
        }


        //	Check args syntax
        for (int i = 0; i < args.length; i++) {
            //	Check all arguments
            if (args[i].equals("-h")) {
                String s = args[++i];
                if (s.toLowerCase().equals("all")) {
                    hosts = util.getTangoHostList();
                } else {
                    hosts = new TangoHost[1];
                    hosts[0] = new TangoHost(s, true);
                }
            } else
                //	Search if action
                for (int j = 0; j < known_actions.length; j++)
                    if (args[i].equals(known_actions[j]))
                        action = j;
        }

        //	Check if correctly initialized.
        if (action == NOT_INITIALIZED ||
                hosts == null) {
            displaySyntax();
            throw new Exception("Astor Exception");
        }
    }

    //===============================================================
    //===============================================================
    private void displaySyntax() {
        System.out.println("Syntax:");
        System.out.println("astor <mode>");
        System.out.println("mode:");
        System.out.println("    -rw    : Astor will be in Read/Write mode");
        System.out.println("    -db_ro : Database will be in Read Only mode");
        System.out.println("    -ro    : Astor will be in read only mode");
        System.out.println("\nor");
        System.out.println("astor <action> <-h hostname>");
        System.out.println();
        System.out.println("Actions:");
        System.out.println("	start: will start all servers");
        System.out.println("	stop : will stop  all servers");
        System.out.println();
        System.out.println("hostname: host to do it \n");
        System.out.println("i.e.: astor start -h alpha    (starts all servers on host alpha)");
        System.out.println("or    astor start -h all      (starts all servers on all controled hosts)");
    }
    //===============================================================
    //===============================================================


    //===============================================================
    /**
     * Another usage of this class
     */
    //===============================================================
    static final int REMOVE_POLLING = 0;
    static final int REMOVE_POLLING_FORCED = 1;
    private static final String PollAttProp = "polled_attr";

    @SuppressWarnings("unused")
    public AstorCmdLine(int doWhat, String serverName) {
        boolean forced = true;
        try {
            switch (doWhat) {
                case REMOVE_POLLING:
                    forced = false;
                case REMOVE_POLLING_FORCED:
                    DeviceProxy[] devices = getDeviceList(serverName);

                    if (devices.length == 0)
                        Except.throw_exception("NO_DEVICES",
                                "No device found for " + serverName,
                                "DbPollPanel.CmdLineSolution(" + serverName + ")");
                    System.out.println("Polled Attributes For " + serverName);
                    displayAndConfirm(devices, forced);
            }
        } catch (Exception e) {
            Except.print_exception(e);
        }
    }

    //===============================================================
    //===============================================================
    private void displayAndConfirm(DeviceProxy[] devices, boolean forced) throws DevFailed, IOException {
        byte[] b = new byte[80];
        for (DeviceProxy device : devices) {
            System.out.println(device.get_name());
            PolledAttr[] attlist = getPolledAttributes(device);
            boolean[] remove_it = new boolean[attlist.length];
            for (int a = 0; a < attlist.length; a++) {
                remove_it[a] = forced;
                if (!forced) {
                    System.out.print("   - " + attlist[a]);
                    System.out.print("  -  Remove polling (y/n) ? ");
                    int nb = System.in.read(b);
                    if (nb>0)
                        remove_it[a] = (b[0] == 'y' || b[0] == 'Y');
                }
            }
            removePolling(device, attlist, remove_it);
        }
    }

    //===============================================================
    //===============================================================
    private void removePolling(DeviceProxy dev, PolledAttr[] attr, boolean[] remove_it)
            throws DevFailed {
        List<String> stringList = new ArrayList<>();
        for (int i=0 ; i<attr.length ; i++)
            if (!remove_it[i]) {
                stringList.add(attr[i].name);
                stringList.add(attr[i].period);
            }
        DbDatum argIn = new DbDatum(PollAttProp);
        argIn.insert(stringList.toArray(new String[stringList.size()]));
        dev.put_property(new DbDatum[]{argIn});

        for (int i=0 ; i<attr.length ; i++)
            if (remove_it[i])
                System.out.println(attr[i].name + " ..... Polling Removed");
    }

    //===============================================================
    //===============================================================
    private DeviceProxy[] getDeviceList(String servname) throws DevFailed {
        List<String> stringList = new ArrayList<>();
        DbServer server = new DbServer(servname);
        String[] classes = server.get_class_list();
        for (String classname : classes) {
            String[] deviceNames = server.get_device_name(classname);
            stringList.addAll(Arrays.asList(deviceNames));
        }
        //	Create device proxy in reverse order
        DeviceProxy[] dp = new DeviceProxy[stringList.size()];
        for (int i=0 ; i<stringList.size() ; i++)
            dp[i] = new DeviceProxy(stringList.get(i));
        return dp;
    }

    //===============================================================
    //===============================================================
    public PolledAttr[] getPolledAttributes(DeviceProxy dev) throws DevFailed {
        DbDatum argout = dev.get_property(PollAttProp);
        String[] data = argout.extractStringArray();
        if (data == null)
            return new PolledAttr[0];

        ArrayList<PolledAttr> polledAttrList = new ArrayList<>();
        for (int i = 0; i < data.length; i += 2)
            polledAttrList.add(new PolledAttr(data[i], data[i + 1]));
        return polledAttrList.toArray(new PolledAttr[polledAttrList.size()]);
    }

    //===============================================================
    //===============================================================
    class PolledAttr {
        String name;
        String period;

        public PolledAttr(String name, String period) {
            this.name = name;
            this.period = period;
        }

        public String toString() {
            return name + "   (" + period + " ms)";
        }
    }
}
