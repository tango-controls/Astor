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
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;

/**
 *	This class is able to declare
 *	a new Starter Tango device server in database.
 *
 * @author verdier
 */
public class MkStarter {
    private final String[] polled_obj_names = {
            "HostState", "RunningServers", "StoppedServers"
    };
    private final String[] logging_properties = {
            "logging_level",
            "logging_target",
            "logging_rft"
    };
    private String hostName;
    private String[] ds_path;

    private String classname = "Starter";
    private String serverName;
    private String deviceName;
    private DeviceProxy dev;

    private static final int polling_period = 1000;
    private static final String[] unexpectedChars = {
            "!", "@", "#", "$", "%", "^", "&", "*",
            "(", ")", "/", "/", ",", "<", ">", ";",
            ":", "{", "}", "[", "]", "=", "+"
    };
    //===============================================================
    //===============================================================
    public MkStarter() throws DevFailed {
        getEnvironment();
        checkHostName(hostName);
        serverName = classname + "/" + hostName;
        deviceName = AstorUtil.getStarterDeviceHeader() + hostName;
    }
    //===============================================================
    //===============================================================
    public MkStarter(String hostName, String[] ds_path)
            throws DevFailed {
        this.hostName = hostName;
        this.ds_path = ds_path;
        checkHostName(hostName);

        serverName = classname + "/" + hostName;
        deviceName = AstorUtil.getStarterDeviceHeader() + hostName;
    }
    //======================================================================
    //======================================================================
    private void checkHostName(String hostName) throws DevFailed {
        for (String unexpected : unexpectedChars)
            if (hostName.contains(unexpected))
                Except.throw_exception("SyntaxError", "Char \'" + unexpected + "\' unexpected !");
    }
    //===============================================================
    //===============================================================
    public void create() throws DevFailed {

        //	Check if does not already exist
        boolean exists = false;
        try {
            new DeviceProxy(deviceName);
            exists = true;
        } catch (DevFailed e) {
            //  nothing
        }
        if (exists)
            Except.throw_exception("DeviceAlreadyExists",
                    serverName + " already exits in database.",
                    "MkStarter.MkStarter()");

        //	create the new Starter server
        Database db = ApiUtil.get_db_obj();
        db.add_device(deviceName, classname, serverName);
    }
    //===============================================================
    //===============================================================
    public void setProperties() throws DevFailed {
        //	Set PATH property as String array
        dev = new DeviceProxy(deviceName);
        dev.put_property(new DbDatum("StartDsPath", ds_path));

        //	Set logging properties (will be done at creation only later)
        String[] valStr = new String[] {
                "WARNING",
                "file::/tmp/ds.log/starter_" + hostName + ".log",
                Integer.toString(500) };
        DbDatum[] datum = new DbDatum[logging_properties.length];
        for (int i = 0; i < logging_properties.length; i++)
            datum[i] = new DbDatum(logging_properties[i], valStr[i]);
        dev.put_property(datum);

        //	Manage Attribute Polling
        setPollProperty();
    }
    //===============================================================
    //===============================================================
    private void setPollProperty() throws DevFailed {
        String str_period = "" + polling_period;

        String[] pollProp = new String[2 * polled_obj_names.length];
        for (int i = 0; i < polled_obj_names.length; i++) {
            pollProp[2 * i] = polled_obj_names[i].toLowerCase();
            pollProp[2 * i + 1] = str_period;
        }
        DbDatum data = new DbDatum("polled_attr");
        data.insert(pollProp);
        dev.put_property(data);
    }
    //===============================================================
    //===============================================================
    public void setAdditionalProperties(String propertyName, String propertyValue, boolean create) throws DevFailed {
        //	Set  property
        if (propertyValue.length() > 0)
            dev.put_property(new DbDatum(propertyName, propertyValue));
        else
        if (!create)
            dev.delete_property(propertyName);
    }
    //===============================================================
    //===============================================================
    private void getEnvironment() throws DevFailed {
        if ((hostName = System.getProperty("HOST_NAME")) == null)
            Except.throw_exception("EnvironmentException",
                    "HOST_NAME is not defined.",
                    "MkStarter.getEnvironment()");

        String dp = System.getProperty("DS_PATH");
        if (dp == null)
            Except.throw_exception("EnvironmentException",
                    "DS_PATH is not defined.",
                    "MkStarter.getEnvironment()");
        ds_path = new String[1];
        ds_path[0] = dp;
    }
    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        try {
            MkStarter starter = new MkStarter();
            starter.create();
            //	Set the default properties
            starter.setProperties();
        } catch (DevFailed e) {
            System.out.println();
            Except.print_exception(e);
            System.exit(-1);
        }
        System.exit(0);
    }
}
