//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the Pogo class definition .
//
// $Author$
//
// $Version: $
//
// $Log$
// Revision 3.9  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.8  2004/07/09 08:12:49  pascal_verdier
// HostInfoDialog is now awaken only on servers change.
//
// Revision 3.7  2004/07/08 11:22:58  pascal_verdier
// First revision able to use events.
//
// Revision 3.6  2004/03/03 08:31:04  pascal_verdier
// The server restart command has been replaced by a stop and start command in a thread.
// The delete startup level info has been added.
//
// Revision 3.5  2004/02/04 14:37:43  pascal_verdier
// Starter logging added
// Database info added on CtrlServersDialog.
//
// Revision 3.4  2003/11/25 15:56:46  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
// Revision 3.3  2003/09/08 11:05:28  pascal_verdier
// *** empty log message ***
//
// Revision 3.2  2003/07/22 14:35:20  pascal_verdier
// Minor bugs fixed.
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
// Revision 3.0  2003/06/04 12:37:53  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.1  2003/06/04 12:33:12  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;
 
import org.omg.CORBA.*;
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;
import java.io.*;
import java.util.*;


/** 
 *	Class Description:
 *	Host object containing servers list.
 *	This class inherit from device proxy.
 *	It is seen as the Starter device running on this host.
 *
 * @author  verdier
 * @Revision 
 */


public class TangoHost extends DeviceProxy
{
	private TangoServer	starter = null;
	private String		name;
	private Vector		servers;
	public  String		usage = null;
	public	int			state;
	public  DevFailed	except;
	public  boolean		do_polling  = false;
	public	boolean		poll_serv_lists = false;
	public	boolean		all_servers = false;
	public	String		collection = null;
	public	HostStateThread	thread = null;
	public  int			notifyd_state;
	public  boolean		use_events;
	public	HostInfoDialog	info_dialog = null;

	/**
	 *	Starter IDL version
	 */
	public	int			idl_version = 0;

	//==============================================================
	//==============================================================
	public TangoHost(String name) throws DevFailed
	{
		//	Initialize device proxy class objects.
		//-----------------------------------------------
		super(new String("tango/admin/" + name));
		set_transparency_reconnection(true);

		servers    = new Vector();
		notifyd_state = AstorDefs.unknown;
		
		//	Get host collection from property
		DbDatum	data = get_property(AstorDefs.collec_property);
		if (data.is_empty()==false)
			collection = data.extractString();

		//	Check if name contain sub network added, then cut it.
		//--------------------------------------------------------
		int	i;
		if ((i=name.indexOf("."))<0)
			this.name  = new String(name);
		else
			this.name  = new String(name.substring(0, i));

		//	Get if Host usage is dedefined in database.
		DbDatum	prop =get_property("HostUsage");
		if (prop.is_empty()==false)
		{
			usage = prop.extractString();
			if (usage.length()==0)
				usage = null;
		}
		//	Check if notify daemon is used by the Starter ds
		use_events = false;
		try {
			data = get_property("UseEvents");
			if (data.is_empty()==false)
				use_events = (data.extractShort()!=0);
		}
		catch(DevFailed e){}
	}
	//=====================================================
	public void removeAllservers()
	{
		if (servers.size()>0)
			servers.removeAllElements();
	}
	//==============================================================
	//==============================================================
	public void addServer(TangoServer ts)
	{
		servers.addElement(ts);
	}
	//==============================================================
	//==============================================================
	public TangoServer getServer(String servname)
	{
		servname = servname.trim();
		for (int i=0 ; i<nbServers() ; i++)
		{
			TangoServer	server = getServer(i);
			if (server.getName().equals(servname))
				return server;
		}
		return null;
	}
	//==============================================================
	//==============================================================
	public TangoServer getServer(int idx)
	{
		return (TangoServer)(servers.elementAt(idx));
	}
	//==============================================================
	//==============================================================
	public void removeServer(int idx)
	{
		servers.removeElementAt(idx);
	}
	//==============================================================
	//==============================================================
	public int nbServers()
	{
		if (servers==null)
			return 0;
		else
			return servers.size();
	}
	//==============================================================
	//==============================================================
	public String readLogFile(String servname) throws DevFailed
	{
		DeviceData	argin = new DeviceData();
		argin.insert(servname);
		DeviceData	argout = command_inout("DevReadLog", argin);
		//System.out.println(argout.extractString());
		return argout.extractString();
	}
	//==============================================================
	/**
	 *	Register server (export/unexport admin device )
	 *	to be known by starter.
	 */
	//==============================================================
	public void registerServer(String servname) throws DevFailed
	{
		String			devname = "dserver/" + servname;
		DbDevExportInfo	info =
			new DbDevExportInfo(devname, "null", name, "null");
		ApiUtil.get_db_obj().export_device(info);
		ApiUtil.get_db_obj().unexport_device(devname);
	}
	//==============================================================
	//==============================================================
	public void startOneServer(String servname) throws DevFailed
	{
		DeviceData	argin = new DeviceData();
		argin.insert(servname);
		System.out.println("command_inout(DevStart, "+servname+") on "+ name());
		command_inout("DevStart", argin);
	}
	//==============================================================
	//==============================================================
	public void startServer(String servname) throws  DevFailed
	{
		DeviceData	argin = new DeviceData();
		argin.insert(servname);
		command_inout("DevStart", argin);
	}
	//==============================================================
	//==============================================================
	public void stopServer(String servname) throws  DevFailed
	{
		DeviceData	argin = new DeviceData();
		argin.insert(servname);
		command_inout("DevStop", argin);
	}
	//==============================================================
	//==============================================================
	public void startServers(int level) throws  DevFailed
	{
		DeviceData	argin = new DeviceData();
		argin.insert((short)level);
		command_inout("DevStartAll", argin);
	}
	//==============================================================
	//==============================================================
	public void stopServers(int level) throws DevFailed
	{
		DeviceData	argin = new DeviceData();
		argin.insert((short)level);
		command_inout("DevStopAll", argin);
	}
	//==============================================================
	//==============================================================
	public void displayLogging(javax.swing.JFrame parent)
	{
		try 
		{
			new LoggingDialog(parent, this).show();
		}
		catch(DevFailed e)
		{
			app_util.PopupError.show(parent, e);
		}
	}
	//==============================================================
	//==============================================================
	public void displayInfo(java.awt.Component parent)
	{
		String	str = "";
		//	Query database for Controlled servers list
		try
		{
			if (starter==null)
				starter = new TangoServer(adm_name());
				//starter = new TangoServer("Starter/" + name, true);
			str += starter.getServerInfo(parent, (state==AstorDefs.all_ok));
			str += "\n----------- Controlled servers -----------\n\n";

			Database	db = ApiUtil.get_db_obj();
			DeviceData	argin = new DeviceData();
			argin.insert(name);
			DeviceData	argout = db.command_inout("DbGetHostServerList", argin);
			String[]	servnames = argout.extractStringArray();

			//	Query database for control mode.
			for (int i=0 ; i<servnames.length ; i++)
			{
				DbServInfo	s = db.get_server_info(servnames[i]);
				//	store only controlled servers
				if (s.controlled)
					str += s.name + "\n";
			}
		}
		catch(DevFailed e) {
			str += e.errors[0].desc;
			app_util.PopupError.show(parent, str, e);
			return;
		}
		str +="\n\n";
		app_util.PopupMessage.show(parent, str);
	}
	//==============================================================
	//==============================================================
	public void testStarter(java.awt.Component parent)
	{
		try
		{
			if (starter==null)
				starter = new TangoServer(adm_name());
			starter.testDevice(parent, new java.awt.Point(0,0));
		}
		catch(DevFailed e) {
			app_util.PopupError.show(parent,e);
		}
	}
	//==============================================================
	//==============================================================
	public void unexportStarter(java.awt.Component parent)
	{
		try
		{
			//	Check if exported
			DbDevImportInfo	info = import_device();
			if (info.exported==false)
			{
				app_util.PopupMessage.show(parent, 
							name() + "  NOT  exported !");
				return;
			}

			//	Unexport device
			unexport_device();
			//	And administrative device
			String	adm = "dserver/Starter/" + name;
			new DeviceProxy(adm).unexport_device();

			//	Stop polling because it is not exported
			do_polling = false;
			app_util.PopupMessage.show(parent, 
							adm + "   and    " + name() +
							"\n\n       have been unexported !");
		}
		catch(DevFailed e) {
			app_util.PopupError.show(parent, e);
		}
	}
	//==============================================================
	//==============================================================
	public void setCollection(String new_collec) throws DevFailed
	{
		DbDatum[] prop = new DbDatum[1];
		prop[0] = new DbDatum(AstorDefs.collec_property, new_collec);

		put_property(prop);
		collection = new_collec;
	}
	//==============================================================
	/**
	 *	Inform Starter device server than srver info have been modified.
	 */
	//==============================================================
	public void informStarterForInfo() throws DevFailed
	{
		command_inout("UpdateServersInfo");
	}
	//==============================================================
	//==============================================================
	public String getName()
	{
		return name;
	}
	//==============================================================
	//==============================================================
	void setPolling(String[] enabled_hosts)
	{
		if (enabled_hosts==null)
			do_polling = true;
		else
		if (enabled_hosts.length>0 && enabled_hosts[0].equals("none"))
			do_polling = false;
		else
		if (enabled_hosts.length>0 && enabled_hosts[0].equals("all"))
			do_polling = true;
		else
			//	search if this host is enable
			for (int i=0 ; i<enabled_hosts.length ; i++)
				if (name.equals(enabled_hosts[i]))
					do_polling = true;
	}
	//===============================================================
	//===============================================================
	public void startServer(java.awt.Component parent, String servname)
	{
		try
		{
			startServer(servname);
		}
		catch (DevFailed e) {
			app_util.PopupError.show(parent, e);
		}
	}
	//===============================================================
	//===============================================================
	void readStdErrorFile(java.awt.Frame parent, String servname)
	{
		try{
			String	logStr = readLogFile(servname);

			//	Get size to know if scrollable is necessary
			//------------------------------------------------
			app_util.PopupText	dialog = new app_util.PopupText(parent, true);
			dialog.show(logStr, 700, 500);
		}
		catch(DevFailed e){
			app_util.PopupError.show(parent, e);
		}
		catch(Exception e){
			app_util.PopupError.show(parent, e.toString());
		  	e.printStackTrace();
		}
	}
	//==============================================================
	/**
	 *	Awake thread to read host.
	 */
	//==============================================================
	void updateData()
	{
		thread.updateData();
	}
	//==============================================================
	//==============================================================
	void stopThread()
	{
		thread.stop_it = true;
		thread.updateData();
	}
	//==============================================================
	//==============================================================
	String[] getPath()
	{
		String[]	path = { "" };
		try
		{
			
			DbDatum	datum = get_property("StartDsPath");
			if (datum.is_empty()==false)
				path = datum.extractStringArray();
		}
		catch(DevFailed e){
			Except.print_exception(e);
		}
		return path;
	}
	//==============================================================
	//==============================================================
	String getFamily()
	{
		String	family = "";
		try
		{
			
			DbDatum	datum = get_property("HostCollection");
			if (datum.is_empty()==false)
				family = datum.extractString();
		}
		catch(DevFailed e){
			Except.print_exception(e);
		}
		return family;
	}
	//==============================================================
	//==============================================================
	public String toString()
	{
		if (usage==null || usage.length()==0)
			return name;
		else
			return name + "  ( "+ usage + " )";
	}
}
