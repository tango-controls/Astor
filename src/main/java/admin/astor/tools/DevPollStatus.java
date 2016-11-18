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


package admin.astor.tools;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;

import java.util.ArrayList;

/**
 *	This class is able to
 *
 * @author verdier
 */
public class DevPollStatus extends ArrayList<PolledElement> {
    private DeviceProxy deviceProxy;
    private boolean pollSeveralAttributes = false;

    private static final boolean FULL_NAME = true;
    private static final boolean ATTR_NAME = false;
    //===============================================================
    //===============================================================
    public DevPollStatus(String deviceName) throws DevFailed {
        deviceProxy = new DeviceProxy(deviceName).get_adm_dev();
        readData(deviceName, ATTR_NAME);
    }

    //===============================================================
    //===============================================================
    public DevPollStatus(String[] deviceNames) throws DevFailed {
        //  full name (device and attribute) if more than one device
        boolean full_name = FULL_NAME;
        if (deviceNames.length < 2)
            full_name = ATTR_NAME;
        for (String deviceName : deviceNames) {
            deviceProxy = new DeviceProxy(deviceName).get_adm_dev();
            readData(removeTangoHostIfAny(deviceName), full_name);
        }
    }

    //===============================================================
    //===============================================================
    public boolean isPollingSeveralAttributes() {
        return pollSeveralAttributes;
    }
    //===============================================================
    //===============================================================
	private String removeTangoHostIfAny(String deviceName) {
		String	tangoHeader = "tango://";
		if (deviceName.startsWith(tangoHeader)) {
			deviceName = deviceName.substring(tangoHeader.length());
			deviceName = deviceName.substring(deviceName.indexOf('/')+1);
		}
		return deviceName;
	}
    //===============================================================
    //===============================================================
   private void readData(String deviceName, boolean full_name) throws DevFailed {
        //long	t0 = System.currentTimeMillis();
        DeviceData argin = new DeviceData();
        argin.insert(deviceName);
        DeviceData argOut = deviceProxy.command_inout("DevPollStatus", argin);
        String[] lines = argOut.extractStringArray();
        for (String line : lines) {
            //  ToDo
            if (line.contains("Time needed for the last attribute"))
                if (line.contains("Time needed for the last attributes")) {
                    pollSeveralAttributes = true;
                }

            PolledElement polledElement = new PolledElement(deviceName, line);
            //  Check if already exists (special case for state and status att/cmd)
            boolean found = false;
            for (int j=0 ; !found && j<size() ; j++) {
                if (full_name)
                    found = (get(j).name.toLowerCase().equals(deviceName + "/" + polledElement.name.toLowerCase()));
                else
                    found = (get(j).name.toLowerCase().equals(polledElement.name.toLowerCase()));
            }
            if (!found) {
                if (full_name)
                    polledElement.name = deviceName + "/" + polledElement.name;
                add(polledElement);
            }
        }

        //long	t1 = System.currentTimeMillis();
        //System.out.println("elapsed time : " + (t1-t0) + " ms");

    }

    //===============================================================
    //===============================================================
    public int polledCount() {
        int cnt = 0;
        for (PolledElement polledElement : this)
            if (polledElement.polled)
                cnt++;
        return cnt;
    }

    //===============================================================
    //===============================================================
    public int triggeredCount() {
        int cnt = 0;
        for (PolledElement polledElement : this)
            if (!polledElement.polled)
                cnt++;
        return cnt;
    }

    //===============================================================
    //===============================================================
    static void displaySyntax() {
        System.out.println("device name ?");
        System.exit(1);
    }

    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        String deviceName = null;
        DevPollStatus clients;

        if (args.length > 0)
            deviceName = args[0];
        else
            displaySyntax();

        try {
            clients = new DevPollStatus(deviceName);
            for (PolledElement client : clients) {
                String[] infoArray = client.getInfo();
                for (String info : infoArray)
                    System.out.println(info);
                System.out.println();
            }
        } catch (DevFailed e) {
            Except.print_exception(e);
            System.exit(1);
        }
        System.exit(0);
    }
}
