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
// Revision 3.30  2011/01/04 14:33:03  pascal_verdier
// Password added for access control dialog.
// Do not try to subscribe on Starter events if starter device not exported.
//
// Revision 3.29  2010/11/29 13:54:45  pascal_verdier
// Multi servers command added.
// Uptime for servers added.
//
// Revision 3.28  2010/04/08 10:41:47  pascal_verdier
// Minor changes.
//
// Revision 3.27  2009/04/06 14:27:47  pascal_verdier
// Using MySqlUtil feature.
//
// Revision 3.26  2009/01/30 09:31:50  pascal_verdier
// Black box management added for database.
// Black box management tool improved.
// Find TANGO object by filter added.
//
// Revision 3.25  2009/01/16 14:46:58  pascal_verdier
// Black box management added for host and Server.
// Starter logging display added for host and server.
// Splash screen use ATK one.
//
// Revision 3.24  2008/11/19 09:59:56  pascal_verdier
// New tests done on Access control.
// Pool Threads management added.
// Size added as preferences.
//
// Revision 3.23  2008/09/12 11:51:23  pascal_verdier
// Minor changes
//
// Revision 3.22  2008/05/26 11:49:12  pascal_verdier
// Host info dialog servers are managed in a jtree.
//
// Revision 3.21  2008/04/07 10:53:36  pascal_verdier
// Branch info modified.
//
// Revision 3.20  2008/03/03 14:55:21  pascal_verdier
// Starter Release_4 management.
//
// Revision 3.19  2008/01/18 10:11:22  pascal_verdier
// OSManager management removed
//
// Revision 3.18  2007/11/07 09:05:38  pascal_verdier
// Display host info if OSManage DS  is running on host.
// Display host's state on HotInfoDialog.
//
// Revision 3.17  2006/06/13 13:52:14  pascal_verdier
// During StartAll command, sleep(500) added between 2 hosts.
// MOVING states added for collection.
//
// Revision 3.16  2006/04/12 13:06:43  pascal_verdier
// updateServerList(0 method added.
//
// Revision 3.15  2006/01/11 08:46:14  pascal_verdier
// PollingProfiler added.
//
// Revision 3.14  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.13  2005/09/13 14:28:01  pascal_verdier
// Wizard management added.
//
// Revision 3.12  2005/05/19 11:28:59  pascal_verdier
// Minor changes.
//
// Revision 3.11  2005/03/11 14:07:54  pascal_verdier
// Pathes have been modified.
//
// Revision 3.10  2005/01/24 09:35:58  pascal_verdier
// export/unexport new server before stating to be known by starter in case of startup failed.
//
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

import admin.astor.tools.BlackBoxTable;
import admin.astor.tools.PopupTable;
import admin.astor.tools.PopupText;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevInfo;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoApi.events.TangoEventsAdapter;
import fr.esrf.TangoApi.events.DbEventImportInfo;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.*;
import admin.astor.tools.Utils;


/**
 *	Class Description:
 *	Host object containing servers list.
 *	This class inherit from device proxy.
 *	It is seen as the Starter device running on this host.
 *
 * @author  verdier
 */


@SuppressWarnings({"NestedTryStatement"})
public class TangoHost extends DeviceProxy
{
	private TangoServer	starter = null;
	private String		name;
	private Vector<TangoServer>
						servers;
	public  String		usage = null;
	public	int			state;
	public  DevFailed	except;
	public  boolean		do_polling  = false;
	public	boolean		poll_serv_lists = false;
	public	String		collection = null;
	public	HostStateThread	thread = null;
	public  int			notifyd_state;
	public	JLabel		notifd_label;
	public  boolean		use_events;
	public  boolean		check_notifd;
	public	HostInfoDialog	info_dialog = null;

	public	TangoEventsAdapter	supplier = null;

	private String		adm_name;

	//==============================================================
	//==============================================================
	public TangoHost(String name, boolean get_prop) throws DevFailed
	{
		//	Initialize device proxy class objects.
		super("tango/admin/" + name);
		adm_name = "dserver/starter/" + name;
		set_transparency_reconnection(true);

		servers       = new Vector<TangoServer>();
		notifyd_state = AstorDefs.unknown;

		//	Check if name contain sub network added, then cut it.
		int	i;
		if ((i=name.indexOf("."))<0)
			this.name  = name;
		else
			this.name  = name.substring(0, i);

		if (get_prop)
		{
			//	Get host collection from property
			DbDatum	data = get_property(AstorDefs.collec_property);
			if (!data.is_empty())
				collection = data.extractString();

			//	Get if Host usage is dedefined in database.
			DbDatum	prop =get_property("HostUsage");
			if (!prop.is_empty())
			{
				usage = prop.extractString();
				if (usage.length()==0)
					usage = null;
			}
			//	Check if notify daemon is used by the Starter ds
			use_events = false;
			try {
				data = get_property("UseEvents");
				if (!data.is_empty())
					use_events = (data.extractShort()!=0);
			}
			catch(DevFailed e){
				/*	Nothing */
			}
			check_notifd = use_events;
		}
		//	Else
		//		at statup it is done on one call for all hosts
	}
	//==============================================================
	//==============================================================
	public TangoHost(DbDevImportInfo devinfo,
					 DbDevImportInfo adminfo,
					 DbEventImportInfo evtinfo) throws DevFailed
	{
		//	Initialize device proxy class objects.
		super(devinfo);
		try {
		
			if (devinfo.exported) {
				import_admin_device(adminfo);
				if (evtinfo!=null)
					this.getAdm_dev().set_evt_import_info(evtinfo);
				else
					use_events = false;
			}
			else
				use_events = false;
		} catch (DevFailed e) {
			Except.print_exception(e);
			use_events = false;
		}
		adm_name = adminfo.name;
		set_transparency_reconnection(true);

		servers       = new Vector<TangoServer>();
		notifyd_state = AstorDefs.unknown;

		//	Check if name contain sub network added, then cut it.
		int	i;
		if ((i=devinfo.name.indexOf("."))<0)
			this.name  = devinfo.name;
		else
			this.name  = devinfo.name.substring(0, i);
		//	Get only member as name
		int idx = name.indexOf('/');
		if (idx>0)
			idx = name.indexOf('/', idx+1);
		if (idx>0)
			name = name.substring(idx+1);
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
		return servers.get(idx);
	}
	//==============================================================
	//==============================================================
	public void removeServer(int idx)
	{
		servers.removeElementAt(idx);
	}
	//==============================================================
	//==============================================================
	public void removeServer(String servname)
	{
		for (int i=0 ; i<nbServers() ; i++)
		{
			TangoServer	server = getServer(i);
			if (server.getName().equals(servname))
			{
				removeServer(i);
				return;
			}
		}
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
	public String[] getServerAttribute()
	{
		try
		{
			DeviceAttribute	att = read_attribute("Servers");
			return att.extractStringArray();
		}
		catch (DevFailed e)
		{
			Except.print_exception(e);
			return new String[0];
		}
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
     * @param servname servers name
     * @throws DevFailed in case of server already running
	 */
	//==============================================================
	public void registerServer(String servname) throws DevFailed
	{
		//	Check before if already running
		String	devname = "dserver/" + servname;
		boolean running = false;
		DeviceProxy	dev;
		try {
			dev = new DeviceProxy(devname);
			dev.ping();
			running = true;
		}
		catch(DevFailed e) {  /** */ }

		if (running)
		{
			IORdump	d = new IORdump(devname);
			Except.throw_exception("StartServerFailed",
					servname + " is already running on " + d.get_host(),
					"DevWizard.startServer()");
		}
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
		System.out.println("command_inout(DevStart, "+servname+") on "+ get_name());
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
	public void hardKillServer(String servname) throws  DevFailed
	{
		DeviceData	argin = new DeviceData();
		argin.insert(servname);
		command_inout("HardKillServer", argin);
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
    //======================================================
    //======================================================
    public void displayUptimes(JFrame parent)
    {
        Vector<String[]>    v = new Vector<String[]>();
        try {
            Database	db = ApiUtil.get_db_obj();
            DeviceData	argin = new DeviceData();
            argin.insert(name);
            DeviceData	argout = db.command_inout("DbGetHostServerList", argin);
            String[]	servnames = argout.extractStringArray();
            for (String sevname : servnames) {

                String[]    exportedStr = new TangoServer("dserver/"+sevname).getServerUptime();
                v.add(new String[] {
                        sevname, exportedStr[0], exportedStr[1] } );
            }

            String[]    columns = new String[] { "Server", "Last   exported", "Last unexported" };
            String[][]  table = new String[v.size()][];
            for (int i=0 ; i<v.size() ; i++) {
                table[i] = v.get(i);
                System.out.println(table[i][0] + ":\t" + table[i][1]);
            }
            PopupTable ppt = new PopupTable(parent, name,
                        columns, table, new Dimension(650, 250));
            ppt.setVisible(true);
        }
        catch(DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }
	//==============================================================
	//==============================================================
	public void displayLogging(JFrame parent)
	{
		displayLogging(parent, null);
	}
	//==============================================================
	//==============================================================
	public void displayLogging(Component parent, String filter)
	{
		try
		{
			////	new LoggingDialog(parent, this).setVisible(true);

			DeviceData	argin = new DeviceData();
			argin.insert("Starter");
			DeviceData	argout = command_inout("DevReadLog", argin);
			String	str = argout.extractString();
			String[]	array = AstorUtil.string2array(str, "\n");
			Vector<String[]>	v = new Vector<String[]>();
			String	prev_date = null;
			for (String line : array) {
				String[]	words = AstorUtil.string2array(line);
				//	Swap servers and action
				String	server = words[2];
				words[2] = words[3];
				words[3] = server;
				//	filter with server name if any
				if (filter==null || server.equals(filter)) {
					//	Check if date changed
					if (prev_date!=null) {
						if (!words[0].equals(prev_date))
							v.add(new String[]{ "-", "-", "-", "-" } );
					}
					prev_date = words[0];
					v.add(words);
				}
			}
			if (v.size()>0) {
				String[][]	lines = new String[v.size()][];
				for (int i=0 ; i<v.size() ; i++)
					lines[i] = v.get(i);
				String[]	colnames = { "Date", "Time", "Action", "Server" };
				PopupTable	table;
				if (parent instanceof JFrame)
					table = new PopupTable((JFrame) parent, "Starter on " + name, colnames, lines);
				else
					table = new PopupTable((JDialog) parent, "Starter on " + name, colnames, lines);
				table.setColumnWidth(new int[] {70, 70,70, 250});
				table.setSortAvailable(false);
				table.setVisible(true);
			}
			else {
				String	desc = "no record found";
				if (filter!=null)
					desc += "  for  " + filter;
				Except.throw_exception("", desc,"");
			}
		}
		catch(DevFailed e) {
			Utils.popupError(parent, e.errors[0].desc);//"Cannot read Starter logging...");
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
				starter = new TangoServer(adm_name);
			str += starter.getServerInfo(parent, (state==AstorDefs.all_ok));
			str += "\n\n----------- Controlled servers -----------\n";

			Database	db = ApiUtil.get_db_obj();
			DeviceData	argin = new DeviceData();
			argin.insert(name);
			DeviceData	argout = db.command_inout("DbGetHostServerList", argin);
			String[]	servnames = argout.extractStringArray();

			//	Query database for control mode.
			for (String servname : servnames)
			{
				DbServInfo s = db.get_server_info(servname);
				//	store only controlled servers
				if (s.controlled)
					str += s.name + "\n";
			}

			String  tagName = "";
			try
			{
				DevInfo info = info();
				String  servinfo = info.doc_url;
				String  tag = "CVS Tag = ";
				int start = servinfo.indexOf(tag);
				if (start>0)
				{
					start += tag.length();
					int end = servinfo.indexOf('\n', start);
					if (end>start)
						tagName = servinfo.substring(start, end);
					str += "\n----------- Tag Release -----------\n" +
						   "        " + tagName;
				}
			}
			catch(DevFailed e) { /** Nothing to do */}

		}
		catch(DevFailed e) {
			str += e.errors[0].desc;
			Utils.popupError(parent, str, e);
			return;
		}
		str +="\n\n";
		Utils.popupMessage(parent, str);
	}
	//==============================================================
	//==============================================================
	public void testStarter(java.awt.Component parent)
	{
		try {
			if (starter==null)
				starter = new TangoServer(adm_name);
			starter.testDevice(parent);
		}
		catch(DevFailed e) {
			ErrorPane.showErrorMessage(parent, "", e);
		}
	}
	//==============================================================
	//==============================================================
	public void unexportStarter(java.awt.Component parent)
	{
		try {
			//	Check if exported
			DbDevImportInfo	info = import_device();
			if (!info.exported) {
				Utils.popupError(parent,
							get_name() + "  NOT  exported !");
				return;
			}

			//	Unexport device
			unexport_device();
			//	And administrative device
			String	adm = "dserver/Starter/" + name;
			new DeviceProxy(adm).unexport_device();

			//	Stop polling because it is not exported
			do_polling = false;
			Utils.popupMessage(parent,
							adm + "   and    " + get_name() +
							"\n\n       have been unexported !");
		}
		catch(DevFailed e) {
			ErrorPane.showErrorMessage(parent, "", e);
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
			for (String enabled_host : enabled_hosts)
				if (name.equals(enabled_host))
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
			ErrorPane.showErrorMessage(parent, "", e);
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
			PopupText	dialog = new PopupText(parent, true);
			dialog.show(logStr, 700, 500);
		}
		catch(DevFailed e){
			ErrorPane.showErrorMessage(parent, "", e);
		}
		catch(Exception e){
			ErrorPane.showErrorMessage(parent, "", e);
			  e.printStackTrace();
		}
	}
	//==============================================================
	//==============================================================
	void updateServersList(JFrame parent)
	{
		try
		{
			command_inout("UpdateServersInfo");
		}
		catch(DevFailed e)
		{
			ErrorPane.showErrorMessage(parent, "", e);
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
			if (!datum.is_empty())
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
			if (datum.is_empty())
				family = datum.extractString();
		}
		catch(DevFailed e){
			Except.print_exception(e);
		}
		return family;
	}
	//==============================================================
	//==============================================================
	public void displayBlackBox(JFrame parent)
	{
		String[] devices = { this.get_name(), this.adm_name };
		String	choice;
		if ((choice=(String)JOptionPane.showInputDialog(parent,
								"Device selection :", "",
								JOptionPane.INFORMATION_MESSAGE, null,
								devices, devices[0])) != null) {
			try {
				new BlackBoxTable(parent, choice).setVisible(true);
			}
			catch(DevFailed e) {
				Utils.popupError(parent, null, e);
			}
		}
	}
	//==============================================================
	//==============================================================
	public String hostStatus()
	{
		String	str = name + ":";
		try {
			if (state==AstorDefs.faulty)
				str += "     is faulty\n";
			else {
				DeviceAttribute	att = read_attribute("Servers");
				if (att.hasFailed())
					str += "     " + att.getErrStack()[0].desc + "\n";
				else {
					str +="\n";
					Vector<String>	running = new Vector<String>();
					Vector<String>	moving  = new Vector<String>();
					Vector<String>	stopped = new Vector<String>();
					String[]	list = att.extractStringArray();
					for (String line : list) {
						StringTokenizer stk = new StringTokenizer(line);
						String name = stk.nextToken();
						String st = stk.nextToken();
						String str_ctrl = stk.nextToken();
						if (str_ctrl.equals("1")) {
							if (st.equals("FAULT"))
								stopped.add(name);
							else if (st.equals("MOVING"))
								moving.add(name);
							else
								running.add(name);
						}
					}
					if (stopped.size()>0)
						str += "     " + stopped.size() + "  servers stopped\n";
					if (moving.size()>0)
						str += "     " + moving.size()  + " servers moving\n";
					if (running.size()>0)
						str += "     " + running.size() + " servers running\n";

				}
			}
		}
		catch(DevFailed e) {
			str += "     " + e.errors[0].desc;
		}
		return str;
	}
	//==============================================================
	//==============================================================
    public String hostName()
    {
        return name;
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
