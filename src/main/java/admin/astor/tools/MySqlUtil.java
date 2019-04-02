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

import admin.astor.AstorUtil;
import admin.astor.TangoHost;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DbDevImportInfo;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.events.DbEventImportInfo;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *	This class group many info and methods used By Astor.
 *
 * @author verdier
 */

public class MySqlUtil {

    private static MySqlUtil instance = new MySqlUtil();
    //===============================================================
    //===============================================================
    public static MySqlUtil getInstance() {
        return instance;
    }
    //===============================================================
    /**
     * Execute a SELECT command on TANGO database.
     *
     * @param command the command to be executed
     * @throws DevFailed in case of database server is not running
     *                   or in case of syntax error in command parameter.
     * @return the data found in TANGO database
     */
    //===============================================================
    public MySqlData executeMySqlSelect(String command) throws DevFailed {
        //long	t0 = System.currentTimeMillis();

        DeviceData argIn = new DeviceData();
        argIn.insert(command);
        DeviceData argout = ApiUtil.get_db_obj().command_inout("DbMySqlSelect", argIn);
        //long	t1 = System.currentTimeMillis();
        //System.out.println("elapsed time : " + (t1-t0) + " ms");

        return new MySqlData(argout.extractLongStringArray());
    }
    //===============================================================
    //===============================================================
    public String[] getHostControlledList() throws DevFailed {
        List<String> deviceList = new ArrayList<>();
        String command = "SELECT name FROM device WHERE class=\"Starter\" ORDER BY name";
        MySqlData result = executeMySqlSelect(command);
        for (MySqlRow row : result) {
            if (!row.hasNull()) {
                deviceList.add(row.get(0));
            }
        }
        //  Get only host names
        List<String> hostList = new ArrayList<>();
        for (String deviceName : deviceList) {
            StringTokenizer stk = new StringTokenizer(deviceName, "/");
            if (stk.countTokens()==3) {
                stk.nextToken();
                stk.nextToken();
                String host = stk.nextToken();
                // Check if FQDN
                int idx = host.indexOf('.');
                if (idx>0)
                    host = host.substring(0, idx);
                if (hostList.contains(host))
                    System.err.println("WARNING: " + host + " found several times as Starter");
                else
                    hostList.add(host);
            }
        }
        return hostList.toArray(new String[0]);
    }
    //===============================================================
    /**
     * Get the EventImport info for specified devices using low level MySql command.
     *
     * @param deviceName specified device names (using % as wildcard)
     * @throws DevFailed case of database server is not running
     * @return the EventImport info for specified devices.
     */
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    public DbEventImportInfo[] getMultipleEventImportInfo(String deviceName) throws DevFailed {
        String table = "event";
        String[] fields = {"name", "host", "exported", "ior"};

        String cmd = "";
        for (int i = 0; i < fields.length; i++) {
            cmd += fields[i];
            if (i < fields.length - 1)
                cmd += ",";
        }
        cmd += " FROM " + table;
        cmd += " WHERE name LIKE \"" + deviceName + "\"";

        MySqlData result = executeMySqlSelect(cmd);
        DbEventImportInfo[] info = new DbEventImportInfo[result.size()];
        int idx = 0;
        for (MySqlRow row : result)
            if (!row.hasNull())
                info[idx++] = new DbEventImportInfo(
                        row.get(0).toLowerCase(),
                        row.get(1),
                        row.get(2).equals("1"),
                        row.get(3));
        /*
      System.out.println("getMultipleEventImportInfo()");
          for (MySqlRow row : result)
              if (row.hasNull()==false)
                  System.out.println("	"+
                              row.get(0).toLowerCase() +"\n"+
                              row.get(1) +"\n"+
                              row.get(2) +"\n"+
                              row.get(3) );

      */
        return info;
    }
    //===============================================================
    /**
     * Get the DevImport info for specified devices using low level MySql command.
     *
     * @param deviceName specified device names (using % as wildcard)
     * @throws DevFailed case of database server is not running
     * @return the DevImport info for specified devices.
     */
    //===============================================================
    public DbDevImportInfo[] getHostDevImportInfo(String deviceName) throws DevFailed {
        String table = "device";
        String[] fields = {"name", "exported", "version", "ior",
                "server", "host", "class"};

        String cmd = "";
        for (int i = 0; i < fields.length; i++) {
            cmd += fields[i];
            if (i < fields.length - 1)
                cmd += ",";
        }
        cmd += " FROM " + table;
        cmd += " WHERE name LIKE \"" + deviceName + "\" ORDER BY name";

        MySqlData result = executeMySqlSelect(cmd);

        DbDevImportInfo[] info = new DbDevImportInfo[result.size()];
        int idx = 0;
        for (MySqlRow row : result)
            if (!row.hasNull())
                info[idx++] = new DbDevImportInfo(
                        row.get(0).toLowerCase(),
                        row.get(1).equals("1"),
                        row.get(2),
                        row.get(3),
                        row.get(4),
                        row.get(5),
                        row.get(6));
        return info;
    }
    //===============================================================
    /**
     * Get the property value for specified devices using low level MySql command.
     *
     * @param deviceName  specified device names (using % as wildcard)
     * @param propertyName specified device property name
     * @throws DevFailed case of database server is not running
     * @return a vector of MySql result rows.
     */
    //===============================================================
    public List<String[]> getHostProperty(String deviceName, String propertyName) throws DevFailed {
        String table = "property_device";
        String[] fields = {"device", "value"};

        String cmd = "";
        for (int i = 0; i < fields.length; i++) {
            cmd += fields[i];
            if (i < fields.length - 1)
                cmd += ",";
        }
        cmd += " FROM " + table;
        cmd += " WHERE device LIKE \"" + deviceName + "\"";
        cmd += " And name = \"" + propertyName + "\"";

        MySqlData result = executeMySqlSelect(cmd);
        List<String[]> lines = new ArrayList<>();
        for (MySqlRow row : result)
            if (!row.hasNull())
                lines.add(new String[]{row.get(0), row.get(1)});
        return lines;
    }
    //===============================================================
    /**
     * Read the Starter properties for all Hosts and
     * set them in TangoHost objects
     *
     * @param hosts the TangoHost objects to set teh property values found
     */
    //===============================================================
    public void manageTangoHostProperties(List<TangoHost> hosts) {
        try {
            String starters = AstorUtil.getStarterDeviceHeader()+"%";
            MySqlUtil mysql = MySqlUtil.getInstance();
            List<String[]> collections = mysql.getHostProperty(starters, "HostCollection");
            List<String[]> host_usage = mysql.getHostProperty(starters, "HostUsage");
            List<String[]> use_evt = mysql.getHostProperty(starters, "UseEvents");
            for (TangoHost host : hosts) {
                if (host!=null) {
                    String deviceName=host.get_name();
                    for (String[] collection : collections)
                        if (deviceName.equals(collection[0]))
                            host.collection=collection[1];
                    for (String[] usage : host_usage)
                        if (deviceName.equals(usage[0]))
                            host.usage=usage[1];
                    for (String[] use : use_evt)
                        if (deviceName.equals(use[0]))
                            host.manageNotifd=
                                    (use[1].equals("true") || use[1].equals("1"));
                }
                else
                    System.err.println("Host is null");
            }
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(new JFrame(), null, e);
        }
    }
    //===============================================================
    //===============================================================


    //===============================================================
    //===============================================================
    private class MySqlRow extends ArrayList<String> {
        //===========================================================
        private MySqlRow(List<String> vs) {
            super();
            this.addAll(vs);
        }
        //===========================================================
        private boolean hasNull() {
            for (String s : this)
                if (s == null)
                    return true;
            return false;
        }
        //===========================================================
    }

    //===============================================================
    //===============================================================
    private class MySqlData extends ArrayList<MySqlRow> {

        //===========================================================
        private MySqlData(DevVarLongStringArray lsa) {
            super();
            int nbRows = lsa.lvalue[lsa.lvalue.length - 2];
            int nbFields = lsa.lvalue[lsa.lvalue.length - 1];
            int idx = 0;
            for (int i = 0; i < nbRows; i++) {
                List<String> row = new ArrayList<>();
                for (int j=0 ; j<nbFields ; j++, idx++)
                    if (lsa.lvalue[idx] != 0)    //	is valid
                        row.add(lsa.svalue[idx]);
                    else
                        row.add(null);
                add(new MySqlRow(row));
            }
        }
        //===========================================================

    }
    //===============================================================
    //===============================================================
}
