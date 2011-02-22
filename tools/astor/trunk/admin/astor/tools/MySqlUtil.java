//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the Pogo class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 1.2  2009/06/02 15:16:22  pascal_verdier
// Bug on host.check_notifyd fixed.
//
// Revision 1.1  2009/04/06 14:25:16  pascal_verdier
// *** empty log message ***
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================


package admin.astor.tools;


/**
 *	This class group many info and methods used By Astor.
 *
 * @author  verdier
 */


import admin.astor.TangoHost;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DbDevImportInfo;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.events.DbEventImportInfo;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.util.Vector;

public class MySqlUtil
{

	private	static MySqlUtil	instance = null;

	//===============================================================
	//===============================================================
	private MySqlUtil()
	{

	}
	//===============================================================
	//===============================================================
	public static MySqlUtil getInstance()
	{
		if (instance==null)
			instance = new MySqlUtil();
		return instance;
	}


	//===============================================================
	/**
	 *	Execute a SELECT command on TANGO databse.
	 * @param cmd	the command to be excuted
	 * @return	the data found in TANGO database
	 * @throws DevFailed in case of database server is not running
	 * 			or in case of syntax error in cmd parameter.
	 */
	//===============================================================
	public MySqlData executeMySqlSelect(String cmd) throws DevFailed
	{
		//long	t0 = System.currentTimeMillis();

		DeviceData	argin = new DeviceData();
		argin.insert(cmd);
		DeviceData	argout = ApiUtil.get_db_obj().command_inout("DbMySqlSelect", argin);
		//long	t1 = System.currentTimeMillis();
		//System.out.println("elapsed time : " + (t1-t0) + " ms");

		return new MySqlData(argout.extractLongStringArray());
	}
	//===============================================================
	/**
	 *	Get the EventImport info for specified devices using low level MySql command.
	 * @param devname	specified device names (using % as wildcard)
	 * @return	the EventImport info for specified devices.
	 * @throws DevFailed case of database server is not running
	 */
	//===============================================================
	public DbEventImportInfo[] getMultipleEventImportInfo(String devname) throws DevFailed
	{
		String		table    = "event";
		String[]	fields   = { "name", "host", "exported", "ior" };

		String	cmd = "";
		for (int i=0; i<fields.length ; i++)
		{
			cmd += fields[i];
			if (i<fields.length-1)
				cmd += ",";
		}
		cmd += " FROM "                + table;
		cmd += " WHERE name LIKE \"" + devname + "\"";

		MySqlData	result = executeMySqlSelect(cmd);
		DbEventImportInfo[]	info = new DbEventImportInfo[result.size()];
		int	idx = 0;
		for (MySqlRow row : result)
			if (!row.hasNull())
				info[idx++] = new DbEventImportInfo(
							row.get(0).toLowerCase(),
							row.get(1),
							row.get(2).equals("1"),
							row.get(3) );
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
	 *	Get the DevImport info for specified devices using low level MySql command.
	 * @param devname	specified device names (using % as wildcard)
	 * @return	the DevImport info for specified devices.
	 * @throws DevFailed case of database server is not running
	 */
	//===============================================================
	public DbDevImportInfo[] getHostDevImportInfo(String devname) throws DevFailed
	{
		String		table    = "device";
		String[]	fields   = { "name", "exported", "version", "ior",
								"server", "host", "class" };

		String	cmd = "";
		for (int i=0; i<fields.length ; i++)
		{
			cmd += fields[i];
			if (i<fields.length-1)
				cmd += ",";
		}
		cmd += " FROM "                + table;
		cmd += " WHERE name LIKE \"" + devname + "\" ORDER BY name";

		MySqlData	result = executeMySqlSelect(cmd);

		DbDevImportInfo[]	info = new DbDevImportInfo[result.size()];
		int	idx = 0;
		for (MySqlRow row : result)
			if (!row.hasNull())
				info[idx++] = new DbDevImportInfo(
							row.get(0).toLowerCase(),
							row.get(1).equals("1"),
							row.get(2),
							row.get(3),
							row.get(4),
							row.get(5),
							row.get(6) );
		return info;
	}
	//===============================================================
	/**
	 *	Get the property value for specified devices using low level MySql command.
	 * @param devname	specified device names (using % as wildcard)
	 * @param propname	specified device property name
	 * @return	a vector of MySql result rows.
	 * @throws DevFailed case of database server is not running
	 */
	//===============================================================
	public Vector<String[]> getHostProperty(String devname, String propname) throws DevFailed
	{
		String		table    = "property_device";
		String[]	fields   = { "device", "value" };

		String	cmd = "";
		for (int i=0; i<fields.length ; i++)
		{
			cmd += fields[i];
			if (i<fields.length-1)
				cmd += ",";
		}
		cmd += " FROM "                + table;
		cmd += " WHERE device LIKE \"" + devname + "\"";
		cmd += " And name = \""     + propname + "\"";

		MySqlData	result = executeMySqlSelect(cmd);
		Vector<String[]>	v = new Vector<String[]>();
		for (MySqlRow row : result)
			if (!row.hasNull())
				v.add(new String[] { row.get(0), row.get(1) });
		return v;
	}
	//===============================================================
	/**
	 * 	Read the Starter properties for all Hosts and
	 *	set them in TangoHost objects
	 * @param hosts	the TangoHost objects to set teh property values found
	 */
	//===============================================================
	public void manageTangoHostProperties(TangoHost[] hosts)
	{
		try
		{
			String		starters  = "tango/admin/%";
			MySqlUtil	mysql = MySqlUtil.getInstance();
			Vector<String[]>	collections  = mysql.getHostProperty(starters, "HostCollection");
			Vector<String[]>	host_usage   = mysql.getHostProperty(starters, "HostUsage");
			Vector<String[]>	use_evt      = mysql.getHostProperty(starters, "UseEvents");
			for (TangoHost host : hosts)
			{
				String	devname = host.get_name();
				for (String[] collection : collections)
					if (devname.equals(collection[0]))
						host.collection = collection[1];
				for (String[] usage : host_usage)
					if (devname.equals(usage[0]))
						host.usage = usage[1];
				for (String[] use : use_evt)
					if (devname.equals(use[0]))
						host.use_events = (use[1].equals("true") ||
											use[1].equals("1"));
				host.check_notifd = host.use_events;
			}
		}
		catch(DevFailed e)
		{
			ErrorPane.showErrorMessage(new JFrame(), null, e);
		}
	}
	//===============================================================
	//===============================================================



	//===============================================================
	//===============================================================
	private class MySqlRow extends Vector<String>
	{
		//===========================================================
		private MySqlRow(Vector<String> vs)
		{
			super();
			for (String s : vs)
				add(s);
		}
		//===========================================================
		private boolean hasNull()
		{
			for (String s : this)
				if (s==null)
					return true;
			return false;
		}
		//===========================================================
	}
	//===============================================================
	//===============================================================
	private class MySqlData extends Vector<MySqlRow>
	{

		//===========================================================
		private MySqlData(DevVarLongStringArray	lsa)
		{
			super();
			int	nb_rows   = lsa.lvalue[lsa.lvalue.length-2];
			int	nb_fields = lsa.lvalue[lsa.lvalue.length-1];
			int	idx = 0;
			for (int i=0 ; i<nb_rows ; i++)
			{
				Vector<String>	vs = new Vector<String>();
				for (int j=0 ; j<nb_fields ; j++, idx++)
					if (lsa.lvalue[idx]!=0)	//	is valid
						vs.add(lsa.svalue[idx]);
					else
						vs.add(null);
				add(new MySqlRow(vs));
			}
		}
		//===========================================================

	}
	//===============================================================
	//===============================================================
}
