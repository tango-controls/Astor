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
import fr.esrf.TangoApi.DbClass;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;

/**
 * Class Description: A DeviceProxy class on AccessControl device
 *
 * @author Pascal Verdier
 */
public class AccessProxy extends DeviceProxy {
    //=======================================================
    //=======================================================
    public AccessProxy(String devname) throws DevFailed {
        super(devname);
    }

    //=======================================================
    //=======================================================
    public String[] getUsers() throws DevFailed {
        DeviceData argout = command_inout("GetUsers");
        return argout.extractStringArray();
    }

    //=======================================================
    //=======================================================
    public String[] getAddressesByUser(String user) throws DevFailed {
        DeviceData argin = new DeviceData();
        argin.insert(user);
        DeviceData argout = command_inout("GetAddressByUser", argin);
        return argout.extractStringArray();
    }

    //=======================================================
    //=======================================================
    public String[] getDevicesByUser(String user) throws DevFailed {
        DeviceData argin = new DeviceData();
        argin.insert(user);
        DeviceData argout = command_inout("GetDeviceByUser", argin);
        return argout.extractStringArray();
    }

    //=======================================================
    //=======================================================
    public void removeUser(String user) throws DevFailed {
        DeviceData argin = new DeviceData();
        argin.insert(user);
        command_inout("RemoveUser", argin);
    }

    //=======================================================
    //=======================================================
    public void cloneUser(String src_user, String new_user) throws DevFailed {
        String[] array = {src_user, new_user};
        DeviceData argin = new DeviceData();
        argin.insert(array);
        command_inout("CloneUser", argin);
    }

    //=======================================================
    //=======================================================
    public void removeAddress(String user, String address) throws DevFailed {
        if (user.equals("All Users"))
            user = "*";
        String[] array = {user, address};
        DeviceData argin = new DeviceData();
        argin.insert(array);
        command_inout("RemoveAddressForUser", argin);
    }

    //=======================================================
    //=======================================================
    public void addAddress(String user, String address) throws DevFailed {
        if (user.equals("All Users"))
            user = "*";
        String[] array = {user, address};
        DeviceData argin = new DeviceData();
        argin.insert(array);
        command_inout("AddAddressForUser", argin);
    }

    //=======================================================
    //=======================================================
    public void removeDevice(String user, String devname, String val) throws DevFailed {
        if (user.equals("All Users"))
            user = "*";
        String[] array = {user, devname, val};
        DeviceData argin = new DeviceData();
        argin.insert(array);
        command_inout("RemoveDeviceForUser", argin);
    }

    //=======================================================
    //=======================================================
    public void addDevice(String user, String devname, String val) throws DevFailed {
        if (user.equals("All Users"))
            user = "*";
        String[] array = {user, devname, val};
        DeviceData argin = new DeviceData();
        argin.insert(array);
        command_inout("AddDeviceForUser", argin);
    }

    //=======================================================
    //=======================================================
    public String getAccess(String[] inputs) throws DevFailed {
        DeviceData argin = new DeviceData();
        argin.insert(inputs);
        DeviceData argout = command_inout("GetAccess", argin);
        return argout.extractString();
    }

    //=======================================================
    //=======================================================
    public void registerService(boolean b) throws DevFailed {
        String cmd = (b) ? "RegisterService" : "UnregisterService";
        command_inout(cmd);
    }

    //=======================================================
    //=======================================================
    public void addAllowedCommand(ClassAllowed class_allowed) throws DevFailed {
        DbClass db_class = new DbClass(class_allowed.name);
        DbDatum datum = new DbDatum("AllowedAccessCmd");
        datum.insert(class_allowed.getAllowedCmdProperty());
        db_class.put_property(new DbDatum[]{datum});
    }
    //=======================================================
    //=======================================================
}
