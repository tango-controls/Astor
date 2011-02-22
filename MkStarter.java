//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the MkStarter class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.2  2006/04/12 13:05:39  pascal_verdier
// *** empty log message ***
//
// Revision 3.1  2005/11/24 12:24:57  pascal_verdier
// DevBrowser utility added.
// MkStarter utility added.
//
//
// Copyleft 2005 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;


/**
 *	This class is able to declare
 *	a new Starter Tango device server in database.
 *
 * @author  verdier
 */

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;



public class  MkStarter
{
	private final int		polling_period = 1000;
	private final String[]	polled_obj_names = {
						"HostState", "RunningServers", "StoppedServers"
					};
	private final String[]	logging_properties = {
										"logging_level",
										"logging_target",
										"logging_rft"
									};
	private String		hostname;
	private String[]	ds_path;
	private boolean		use_events = false;

	private	String	    classname = "Starter";
	private	String	    servname;
	private	String	    devname;
	private	String	    admindev;
    private DeviceProxy dev;
	//===============================================================
	//===============================================================
	public MkStarter() throws DevFailed
	{
		getEnvironment();

		servname = classname + "/" + hostname;
		devname  = "tango/admin/" + hostname;
		admindev = "dserver/" + classname + "/" + hostname;
	}

	//===============================================================
	//===============================================================
	public MkStarter(String hostname, String[] ds_path, boolean use_events)
		throws DevFailed
	{
		this.hostname   = hostname;
		this.ds_path    = ds_path;
		this.use_events = use_events;

		servname = classname + "/" + hostname;
		devname  = "tango/admin/" + hostname;
		admindev = "dserver/" + classname + "/" + hostname;
	}
	//===============================================================
	//===============================================================
	public void create() throws DevFailed
	{

		//	Check if does not already exist
		boolean exists = false;
		try
		{
			new DeviceProxy(devname);
			exists = true;
		}
		catch(DevFailed e) {}
		if (exists)
			Except.throw_exception("DeviceAlreadyExists",
							servname + " is already exits in database.",
							"MkStarter.MkStarter()");

		//	create server info
		System.out.println("Create server " + servname);
		DbDevInfo[]	devinfo = new DbDevInfo[2];
		devinfo[0] = new DbDevInfo(admindev, classname, servname);
		devinfo[1] = new DbDevInfo(devname, classname, servname);

		//	create the new Starter server
		Database	db = ApiUtil.get_db_obj();
		db.add_device(devname, classname, servname);
	}
	//===============================================================
	//===============================================================
	public void setProperties() throws DevFailed
	{
		//	Set PATH property as String array
		dev = new 	DeviceProxy(devname);
		dev.put_property(new DbDatum("StartDsPath", ds_path));

		//	Set logging properties (will be done at creation only later)
		String[]	valStr = new String[3];
		valStr[0] = "WARNING";
		valStr[1] = "file::/tmp/ds.log/starter_" + hostname + ".log";
		valStr[2] = new Integer(500).toString();
		DbDatum[]	datum = new DbDatum[logging_properties.length];
		for (int i=0 ; i<logging_properties.length ; i++)
			datum[i] = new DbDatum(logging_properties[i], valStr[i]);
		dev.put_property(datum);


		//	Manage Attribute Polling
		setPollProperty();


		//	Manage use events if needed.
		if (use_events)
		{
			DbDatum	data = new DbDatum("UseEvents", 1);
			dev.put_property(data);

			DbAttribute	att = new DbAttribute("HostState");
			att.add("abs_change", 1);
			dev.put_attribute_property(att);

			System.out.println("Starter will use events");
		}
		else
		{
			DbDatum	data = new DbDatum("UseEvents", 0);
			dev.put_property(data);
		}
	}
	//===============================================================
	//===============================================================
    private void setPollProperty() throws DevFailed
    {
	    String      str_period = "" + polling_period;

        String[]  pollProp = new String[2*polled_obj_names.length];
		for (int i=0 ; i<polled_obj_names.length ; i++)
		{
	        pollProp[2*i]   = polled_obj_names[i].toLowerCase();
        	pollProp[2*i+1] = str_period;
		}
        DbDatum data = new DbDatum("polled_attr");
        data.insert(pollProp);
        dev.put_property(data);
    }

	//===============================================================
	//===============================================================
    public void setAdditionalProperties(String usage, String family) throws DevFailed
    {
        //	Set usage property
        if (usage.length()>0)
            dev.put_property(new DbDatum(AstorDefs.usage_property, usage));

        //	add host family property
        if (family.length()>0)
            dev.put_property(new DbDatum(AstorDefs.collec_property, family));
    }

	//===============================================================
	//===============================================================
	private void getEnvironment() throws DevFailed
	{
		if ((hostname=System.getProperty("HOST_NAME"))==null)
			Except.throw_exception("EnvironmentException",
							"HOST_NAME is not defined.",
							"MkStarter.getEnvironment()");

		String dp = System.getProperty("DS_PATH");
		if (dp==null)
			Except.throw_exception("EnvironmentException",
							"DS_PATH is not defined.",
							"MkStarter.getEnvironment()");
		ds_path = new String[1];
		ds_path[0] = dp;

		String str = System.getProperty("USE_EVENTS");
		if (str!=null && str.toLowerCase().equals("true"))
			use_events = true;
	}
	//===============================================================
	//===============================================================
	public static void main (String[] args)
	{
		try
		{
			MkStarter	starter = new MkStarter();
			starter.create();
            //	Set the default properties
            starter.setProperties();
		}
		catch(DevFailed e)
		{
			System.out.println();
			Except.print_exception(e);
			System.exit(-1);
		}
		System.exit(0);
	}
}
