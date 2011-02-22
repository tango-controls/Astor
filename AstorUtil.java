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
// Revision 3.38  2009/02/18 09:47:57  pascal_verdier
// Device dependencies (sub-devices) tool added.
//
// Revision 3.37  2009/01/16 14:46:58  pascal_verdier
// Black box management added for host and Server.
// Starter logging display added for host and server.
// Splash screen use ATK one.
//
// Revision 3.36  2008/12/16 15:17:16  pascal_verdier
// Add a scroll pane in HostInfoDialog in case of too big dialog.
//
// Revision 3.35  2008/11/19 09:59:56  pascal_verdier
// New tests done on Access control.
// Pool Threads management added.
// Size added as preferences.
//
// Revision 3.34  2008/09/12 11:52:02  pascal_verdier
// Minor changes
//
// Revision 3.33  2007/09/21 09:25:18  pascal_verdier
// Refresh Astor properties when TANGO_HOST changed.
//
// Revision 3.32  2007/08/20 14:06:08  pascal_verdier
// ServStatePanel added on HostInfoDialog (Check states option).
//
// Revision 3.31  2007/04/27 08:07:28  pascal_verdier
// Bug on LastCollection not set fixed.
//
// Revision 3.30  2007/03/27 08:56:11  pascal_verdier
// Preferences added.
//
// Revision 3.29  2007/03/08 13:44:32  pascal_verdier
// LastCollections property added.
//
// Revision 3.28  2007/01/17 10:11:27  pascal_verdier
// Html helps added.
// Startup error message added in view menu.
//
// Revision 3.27  2006/06/26 12:24:08  pascal_verdier
// Bug fixed in miscellaneous host collection.
//
// Revision 3.26  2006/06/13 13:52:14  pascal_verdier
// During StartAll command, sleep(500) added between 2 hosts.
// MOVING states added for collection.
//
// Revision 3.25  2006/01/13 14:24:40  pascal_verdier
// Family Miscellaneous management modified.
//
// Revision 3.24  2006/01/11 08:46:13  pascal_verdier
// PollingProfiler added.
//
// Revision 3.23  2005/12/01 10:00:23  pascal_verdier
// Change TANGO_HOST added (needs TangORB-4.7.7 or later).
//
// Revision 3.22  2005/11/24 12:24:57  pascal_verdier
// DevBrowser utility added.
// MkStarter utility added.
//
// Revision 3.21  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.20  2005/09/27 12:43:18  pascal_verdier
// RloginCmd property added.
//
// Revision 3.19  2005/09/15 08:26:36  pascal_verdier
// Server architecture display addded.
//
// Revision 3.18  2005/08/30 08:05:25  pascal_verdier
// Management of two TANGO HOST added.
//
// Revision 3.17  2005/08/02 12:01:51  pascal_verdier
// Minor changes.
//
// Revision 3.16  2005/06/02 09:02:36  pascal_verdier
// Minor changes.
//
// Revision 3.15  2005/04/25 08:55:36  pascal_verdier
// Start/Stop servers from shell command line added.
//
// Revision 3.14  2005/03/15 10:22:30  pascal_verdier
// Sort servers before creating panel buttons.
//
// Revision 3.13  2005/01/18 08:48:20  pascal_verdier
// Tools menu added.
// Not controlled servers list added.
//
// Revision 3.12  2004/11/29 11:43:56  pascal_verdier
// OsIsUnix method modified.
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================


package admin.astor;


/**
 *	This class group many info and methods used By Astor.
 *
 * @author  verdier
 */


import admin.astor.tools.MySqlUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.ErrSeverity;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoApi.events.DbEventImportInfo;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.JSmoothProgressBar;
import fr.esrf.tangoatk.widget.util.Splash;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class AstorUtil implements AstorDefs {

	private static DbClass	_class = null;
	private	static AstorUtil	instance = null;

	//	Variables will be setted by properties
	//---------------------------------------------
	private static short	readInfoPeriod  = 5;
	private static short	nbStartupLevels	= 5;
	private static String	rloginCmd 	    = null;
	private static String	rloginUser	    = null;
	private	static String[]	tools           = null;
	private	static String[] known_tango_hosts = null;
	private	static Dimension preferred_size             = new Dimension(400, 600);
	private	static Dimension host_dlg_preferred_size    = new Dimension(800, 500);
	private	static String[] last_collec       = null;
	private	static boolean	jiveReadOnly      = false;
	private static String	serverHelpURL	  = "http://www.esrf.fr/computing/cs/tango/";
	private static String	appliHelpURL	  = "http://www.esrf.fr/computing/cs/tango/";
	private static boolean	properties_read   = false;
	private static boolean	debug             = false;
	private static boolean	no_ctrl_btn       = false;
	private static String[]	helps;

	MyCompare		compare;

	private static final	String[]	astor_propnames = {
							"Debug",
							"LastCollections",
							"RloginCmd",
							"RloginUser",
							"NoCtrlButton",
							"JiveReadOnly",
							"KnownTangoHosts",
							"PreferredSize",
							"HostDialogPreferredSize",
							"Tools",
							"HtmlHelps"
						};
	public static ImageIcon[]  state_icons = new ImageIcon[NbStates];
	//===============================================================
	//===============================================================
	private AstorUtil()
	{
		compare = new MyCompare();
	}

	//===============================================================
	//===============================================================
	public static AstorUtil getInstance()
	{
		if (instance==null)
			instance = new AstorUtil();
		return instance;
	}
	//===============================================================
	//===============================================================
	public void initIcons()
	{
		state_icons[unknown] = new ImageIcon(getClass().getResource(img_path + "greyball.gif"));
		state_icons[faulty]  = new ImageIcon(getClass().getResource(img_path + "redball.gif"));
		state_icons[alarm]   = new ImageIcon(getClass().getResource(img_path + "orangebal.gif"));
		state_icons[all_ok]  = new ImageIcon(getClass().getResource(img_path + "greenbal.gif"));
		state_icons[moving]  = new ImageIcon(getClass().getResource(img_path + "blueball.gif"));
		state_icons[failed]  = new ImageIcon(getClass().getResource(img_path + "failed.gif"));
	}
	//===============================================================
	//===============================================================
	static String[] string2array(String str)
	{
		return string2array(str, null);
	}
	//===============================================================
	//===============================================================
	static String[] string2array(String str, String separ)
	{
		StringTokenizer	stk;
		if (separ==null)
			stk = new StringTokenizer(str);
		else
			stk = new StringTokenizer(str, separ);
		Vector	v = new Vector();
		while (stk.hasMoreTokens())
			v.add(stk.nextToken());
		String []	array = new String[v.size()];
		for (int i=0 ; i<v.size() ; i++)
			array[i] = (String)v.get(i);
		return array;
	}
	//===============================================================
	//===============================================================
	static boolean getCtrlBtn()
	{
		if (!no_ctrl_btn)
			readAstorProperties();
		return !no_ctrl_btn;
	}
	//===============================================================
	//===============================================================
	static boolean getDebug()
	{
		if (!properties_read)
			readAstorProperties();
		return debug;
	}
	//===============================================================
	//===============================================================
	public static String getRloginCmd()
	{
		if (!properties_read)
			readAstorProperties();
		return rloginCmd;
	}
	//===============================================================
	//===============================================================
	public static void setRloginCmd(String s)
	{
		rloginCmd = s;
	}
	//===============================================================
	//===============================================================
	public static String getRloginUser()
	{
		if (!properties_read)
			readAstorProperties();
		return rloginUser;
	}
	//===============================================================
	//===============================================================
	public static void setRloginUser(String s)
	{
		rloginUser = s;
	}
	//===============================================================
	//===============================================================
	public static void setTools(String[] t)
	{
		tools = t;
	}
	//===============================================================
	//===============================================================
	public static String[] getTools()
	{
		if (!properties_read)
			readAstorProperties();
		return tools;
	}
	//===============================================================
	//===============================================================
	public static void setHtmlHelps(String[] h)
	{
		helps = h;
	}
	//===============================================================
	//===============================================================
	public static String[] getHtmlHelps()
	{
		if (!properties_read)
			readAstorProperties();
		return helps;
	}
	//===============================================================
	//===============================================================
	public static String[] getKnownTangoHosts()
	{
		if (!properties_read)
			readAstorProperties();
		return known_tango_hosts;
	}
	//===============================================================
	//===============================================================
	public static Dimension getPreferredSize()
	{
		if (!properties_read)
			readAstorProperties();
		return preferred_size;
	}
	//===============================================================
	//===============================================================
	public static void setPreferredSize(Dimension d)
	{
		preferred_size = d;
	}
	//===============================================================
	//===============================================================
	public static Dimension getHostDialogPreferredSize()
	{
		if (!properties_read)
			readAstorProperties();
		return host_dlg_preferred_size;
	}
	//===============================================================
	//===============================================================
	public static void setHostDialogPreferredSize(Dimension d)
	{
		host_dlg_preferred_size = d;
	}
	//===============================================================
	//===============================================================
	public static void setKnownTangoHosts(String[] kth)
	{
		known_tango_hosts = kth;
	}
	//===============================================================
	//===============================================================
	@SuppressWarnings({"NestedTryStatement"})
	static public void readAstorProperties()
	{
		try
		{
			//	Get database instance
			//----------------------------------
			Database	dbase = ApiUtil.get_db_obj();
			//	get Astor Property
			//----------------------------------
			DbDatum[]	data = dbase.get_property("Astor", astor_propnames);
			int	i = -1;
			if (!data[++i].is_empty())
				debug = data[i].extractBoolean();
			if (!data[++i].is_empty())
				last_collec = data[i].extractStringArray();
			if (!data[++i].is_empty())
				rloginCmd = data[i].extractString();
			if (!data[++i].is_empty())
				rloginUser = data[i].extractString();
			if (!data[++i].is_empty())
				no_ctrl_btn = data[i].extractBoolean();
			if (!data[++i].is_empty())
				jiveReadOnly = data[i].extractBoolean();
			if (!data[++i].is_empty())
				known_tango_hosts = data[i].extractStringArray();
			if (!data[++i].is_empty())
			{
				String[]	s = data[i].extractStringArray();
				try {
					int	width = Integer.parseInt(s[0]);
					int	height = Integer.parseInt(s[1]);
					preferred_size = new Dimension(width, height);
				} catch(NumberFormatException e) {}
			}
			if (!data[++i].is_empty())
			{
				String[]	s = data[i].extractStringArray();
				try {
					int	width = Integer.parseInt(s[0]);
					int	height = Integer.parseInt(s[1]);
					host_dlg_preferred_size = new Dimension(width, height);
				} catch(NumberFormatException e) {}
			}
			if (!data[++i].is_empty())
				tools = data[i].extractStringArray();
			if (!data[++i].is_empty())
				helps = data[i].extractStringArray();
			properties_read = true;
		}
		catch(DevFailed e) { /** Do Nothing */ }
	}
	//===============================================================
	//===============================================================
	static void putAstorProperties() throws DevFailed
	{
		//	Get database instance
		//----------------------------------
		Database	dbase = ApiUtil.get_db_obj();
		//	get Astor Property
		//----------------------------------
		DbDatum[]	data = new DbDatum[astor_propnames.length];
		int	i = 0;
		data[i] = new DbDatum(astor_propnames[i]);
		data[i++].insert(debug);

		data[i] = new DbDatum(astor_propnames[i]);
		data[i++].insert(last_collec);

		data[i] = new DbDatum(astor_propnames[i]);
		data[i++].insert(rloginCmd);

		data[i] = new DbDatum(astor_propnames[i]);
		data[i++].insert(rloginUser);

		data[i] = new DbDatum(astor_propnames[i]);
		data[i++].insert(no_ctrl_btn);

		data[i] = new DbDatum(astor_propnames[i]);
		data[i++].insert(jiveReadOnly);

		data[i] = new DbDatum(astor_propnames[i]);
		data[i++].insert(known_tango_hosts);

		data[i] = new DbDatum(astor_propnames[i]);
		String[]	size_str = {
				Integer.toString(preferred_size.width),
				Integer.toString(preferred_size.height),
			};
		data[i++].insert(size_str);

		data[i] = new DbDatum(astor_propnames[i]);
		String[]	host_dlg_size_str = {
				Integer.toString(host_dlg_preferred_size.width),
				Integer.toString(host_dlg_preferred_size.height),
			};
		data[i++].insert(host_dlg_size_str);

		data[i] = new DbDatum(astor_propnames[i]);
		data[i++].insert(tools);

		data[i] = new DbDatum(astor_propnames[i]);
		data[i].insert(helps);

		dbase.put_property("Astor", data);
	}
	//===============================================================
	//===============================================================
	void setJiveReadOnly(boolean b)
	{
		jiveReadOnly = b;
	}
	//===============================================================
	//===============================================================
	boolean jiveIsReadOnly()
	{
		if (!properties_read)
			readAstorProperties();
		return jiveReadOnly;
	}
	//===============================================================
	//===============================================================
	void setLastCollectionList(String[] lcl)
	{
		last_collec = lcl;
	}
	//===============================================================
	//===============================================================
	String[] getLastCollectionList()
	{
		if (!properties_read)
			readAstorProperties();
		return last_collec;
	}
	//===============================================================
	//===============================================================
	String[] getCollectionList(TangoHost[] hosts)
	{
		Vector	vect = new Vector();
		for (TangoHost host : hosts)
		{
			//  Check if collection property is defined.
			if (host.collection == null)
				host.collection = "Miscellaneous";

			//  Check if this collection already exists
			boolean found = false;
			for (int j = 0; j < vect.size() && !found; j++)
				found = (host.collection.equals(vect.elementAt(j)));

			//	If not already exists add it
			if (!found)
				vect.add(host.collection);
		}

		//	Sort for alphabetical order
		Collections.sort(vect, compare);

		//	Add database as first element
		vect.add(0, "Tango Database");

		//	Check if default Bottom collection
		String[]	lasts = getLastCollectionList();
		//	Check if collections exist
		if (lasts!=null)
			for (String last : lasts)
			{
				boolean found = false;
				for (int j = 0; !found && j < vect.size(); j++)
				{
					//	 put it at end of vector
					if (found = last.equals(vect.get(j).toString()))
					{
						String s = vect.get(j).toString();
						vect.remove(j);
						vect.add(s);
					}
				}
			}


		//	Copy vector to string array
		String[]	list = new String[vect.size()];
		for (int i=0 ; i<vect.size() ; i++)
			list[i] = (String)vect.elementAt(i);
		return list;
	}
	//===============================================================
	//===============================================================
	public TangoHost[] getTangoHostList() throws DevFailed
	{
		//	Get hosts list from database
		String []	hostnames = getHostControlledList();

		//	If IDL 4 or greater, read database for all hosts import info
		boolean	db_server_idl_4 =
				ApiUtil.get_db_obj().get_idl_version()>=4;
		DbDevImportInfo[] devinfo = null;
		DbDevImportInfo[] adminfo = null;
		DbEventImportInfo[]	evtinfo = null;
		if (db_server_idl_4)
		{
			MySqlUtil	mysql = MySqlUtil.getInstance();
			devinfo = mysql.getHostDevImportInfo("tango/admin/%");
			adminfo = mysql.getHostDevImportInfo("dserver/starter/%");
			evtinfo = mysql.getMultipleEventImportInfo("dserver/starter/%");
		}

		//	And create TangoHost array object
		TangoHost[]	hosts = new TangoHost[hostnames.length];
		for(int i=0 ; i<hostnames.length ; i++)
			if (db_server_idl_4)
			{
				//	Check to be sure the admin corespond to the device
				DbDevImportInfo	adm = getAdmDevImportInfo(devinfo[i], adminfo);
				if (adm==null)
					hosts[i] = new TangoHost(hostnames[i], ! db_server_idl_4);
				else
				{
					//	Create the device proxies with 3 info
					//  read in one call for all hosts
					DbEventImportInfo	evt = getEventImportInfo(adm.name, evtinfo);
					hosts[i] = new TangoHost(devinfo[i], adm, evt);
				}
			}
			else
				hosts[i] = new TangoHost(hostnames[i], ! db_server_idl_4);

		if (db_server_idl_4)
			MySqlUtil.getInstance().manageTangoHostProperties(hosts);
		return hosts;
	}
	//===============================================================
	//===============================================================
	private DbDevImportInfo getAdmDevImportInfo(DbDevImportInfo devinfo, DbDevImportInfo[] adminfo)
	{
		//	get member name
		int	idx = devinfo.name.indexOf('/');
		idx = devinfo.name.indexOf('/', idx+1);
		String	devmember = devinfo.name.substring(idx);

		for (DbDevImportInfo info : adminfo)
		{
			idx = info.name.indexOf('/');
			idx = info.name.indexOf('/', idx+1);
			String	admmember = info.name.substring(idx);
			if (admmember.equals(devmember))
				return info;
		}
		//	not found
		return null;
	}
	//===============================================================
	//===============================================================
	private DbEventImportInfo getEventImportInfo(String admname, DbEventImportInfo[] evtinfo)
	{
		if (evtinfo!=null)
			for (DbEventImportInfo info : evtinfo)
				if (info.name.equals(admname))
					return info;
		//	not found
		return null;
	}
	//===============================================================
	/**
	 *	Get the devices controlled by Starter DS
	 *	and return the hosts list.
	 */
	//===============================================================
	private String[] getHostControlledList() throws DevFailed
	{
		//	Get database instance and read host list
		//-------------------------------------------------
		Database	dbase = ApiUtil.get_db_obj();
		return  dbase.get_device_member("tango/admin/*");
	}
	//===============================================================
	//===============================================================
	public static String getTangoHost()
	{
		String	th = System.getProperty("TANGO_HOST");
		if (th==null)
			th = System.getenv("TANGO_HOST");
		return th;
	}
	//===============================================================
	//===============================================================
	public static void setTangoHost(String tango_host)
	{
		Properties props = System.getProperties();
		props.put("TANGO_HOST", tango_host);
		System.setProperties(props);
		_class = null;
	}
	//===============================================================
	//===============================================================
	public static short getStarterReadPeriod()
	{
		if (_class==null)
		{
			getStarterClassProperties();
		}
		return readInfoPeriod;
	}
	//===============================================================
	//===============================================================
	public static short getStarterNbStartupLevels()
	{
		if (_class==null)
		{
			getStarterClassProperties();
		}
		return nbStartupLevels;
	}
	//===============================================================
	//===============================================================
	public static String getStarterHelpURL()
	{
		if (_class==null)
		{
			getStarterClassProperties();
		}
		return serverHelpURL;
	}
	//===============================================================
	//===============================================================
	public static String getAppliHelpURL()
	{
		if (_class==null)
		{
			getStarterClassProperties();
		}
		return appliHelpURL;
	}
	//===============================================================
	//===============================================================
	private static void getStarterClassProperties()
	{
		try
		{
			_class = new DbClass("Starter");

			String[]	propnames = {
								"NbStartupLevels",
								"ReadInfoDbPeriod",
								"doc_url",
								"appli_doc_url"
								};
			DbDatum[]	properties = _class.get_property(propnames);
			if (!properties[0].is_empty())
				nbStartupLevels  = properties[0].extractShort();
			if (!properties[1].is_empty())
				readInfoPeriod   = properties[1].extractShort();
			readInfoPeriod  *= 1000;	//	sec -> ms

			if (!properties[2].is_empty())
				serverHelpURL = properties[2].extractString();

			if (!properties[3].is_empty())
				appliHelpURL = properties[3].extractString();
		}
		catch(DevFailed e)
		{
			 Except.print_exception(e);
		}
		if (getDebug())
		{
			System.out.println("NbStartupLevels:  " + nbStartupLevels);
			System.out.println("ReadInfoDbPeriod: " + readInfoPeriod);
			System.out.println("server doc_url:   " + serverHelpURL);
			System.out.println("appli_doc_url:    " + appliHelpURL);
		}
	}
	//===============================================================
	//===============================================================
	static String[] getServerClassProperties(String classname)
	{
		String[] result = new String[3];
		try
		{
			DbClass	dbclass  = new DbClass(classname);
			String[] propnames = {  "ProjectTitle",
									"Description",
									"doc_url" };
			String[]	desc;
			DbDatum[]	prop = dbclass.get_property(propnames);
			if (!prop[0].is_empty())
				result[0] = prop[0].extractString();
			if (!prop[1].is_empty())
			{
				//	Get description as string array and convert to string
				desc = prop[1].extractStringArray();
				result[1] = "";
				for (int i=0 ; i<desc.length ; i++)
				{
					result[1] += desc[i];
					if (i<desc.length-1)
						result[1] += "\n";
				}
			}
			if (prop[2].is_empty())
				result[2] = DocLocationUnknown;
			else
				result[2] = prop[2].extractString();
		}
		catch(DevFailed e)
		{
			result[0] = result[1] = result[2] = null;
		}
		return result;
	}
	//================================================================
	//================================================================
	public static String strException(Exception except)
	{
		String	str = "";

		if (except instanceof ConnectionFailed)
			str += ((ConnectionFailed)(except)).getStack();
		else
		if (except instanceof CommunicationFailed)
			str += ((CommunicationFailed)(except)).getStack();
		else
		if (except instanceof WrongNameSyntax)
			str += ((WrongNameSyntax)(except)).getStack();
		else
		if (except instanceof WrongData)
			str += ((WrongData)(except)).getStack();
		else
		if (except instanceof NonDbDevice)
			str += ((NonDbDevice)(except)).getStack();
		else
		if (except instanceof NonSupportedFeature)
			str += ((NonSupportedFeature)(except)).getStack();
		else
		if (except instanceof EventSystemFailed)
			str += ((EventSystemFailed)(except)).getStack();
		else
		if (except instanceof AsynReplyNotArrived)
			str += ((AsynReplyNotArrived)(except)).getStack();
		else
		if (except instanceof DevFailed)
		{
			DevFailed	df = (DevFailed)except;
			//	True DevFailed
			str += "Tango exception  " + df.toString() + "\n";
			for (int i=0 ; i<df.errors.length ; i++)
			{
				str += "Severity -> ";
				switch (df.errors[i].severity.value())
				{
				case ErrSeverity._WARN :
					str += "WARNING \n";
					break;

				case ErrSeverity._ERR :
					str += "ERROR \n";
					break;

				case ErrSeverity._PANIC :
					str += "PANIC \n";
					break;

				default :
					str += "Unknown severity code";
					break;
				}
				str += "Desc   -> " + df.errors[i].desc   + "\n";
				str += "Reason -> " + df.errors[i].reason + "\n";
				str += "Origin -> " + df.errors[i].origin + "\n";

				if (i<df.errors.length-1)
					str += "-------------------------------------------------------------\n";
			}
		}
		else
			str = except.toString();
		return str;
	}

	//======================================================
	//======================================================
	static public void centerDialog(JDialog dialog, JFrame parent)
	{
		Point	p = parent.getLocationOnScreen();
		p.x += ((parent.getWidth() - dialog.getWidth())  / 2);
		p.y += ((parent.getHeight() - dialog.getHeight())  / 2);
		if (p.y<=0) p.y=20;
		if (p.x<=0) p.x=20;
		dialog.setLocation(p);
	}
	//======================================================
	//======================================================
	static public void centerDialog(JDialog dialog, JDialog parent)
	{
		Point	p = parent.getLocationOnScreen();
		p.x += ((parent.getWidth() - dialog.getWidth())  / 2);
		p.y += ((parent.getHeight() - dialog.getHeight())  / 2);
		if (p.y<=0) p.y=20;
		if (p.x<=0) p.x=20;
		dialog.setLocation(p);
	}
	//======================================================
	//======================================================
	static public void rightShiftDialog(JDialog dialog, JFrame parent)
	{
		Point	p = parent.getLocationOnScreen();
		p.x += parent.getWidth();
		p.y += ((parent.getHeight() - dialog.getHeight())  / 2);
		if (p.y<=0) p.y=20;
		if (p.x<=0) p.x=20;
		dialog.setLocation(p);
	}
	//======================================================
	//======================================================
	static public void rightShiftDialog(JDialog dialog, JDialog parent)
	{
		Point	p = parent.getLocationOnScreen();
		p.x += parent.getWidth();
		p.y += ((parent.getHeight() - dialog.getHeight())  / 2);
		if (p.y<=0) p.y=20;
		if (p.x<=0) p.x=20;
		dialog.setLocation(p);
	}
	//======================================================
	//======================================================
	static public void cascadeDialog(JDialog dialog, JFrame parent)
	{
		Point	p = parent.getLocationOnScreen();
		p.x += 20;
		p.y += 20;
		dialog.setLocation(p);
	}
	//======================================================
	//======================================================
	static public void cascadeDialog(JDialog dialog, JDialog parent)
	{
		Point	p = parent.getLocationOnScreen();
		p.x += 20;
		p.y += 20;
		dialog.setLocation(p);
	}
	//======================================================
	//======================================================
	static String[] string2StringArray(String str)
	{
		int	idx;
		Vector	v = new Vector();
		while ((idx=str.indexOf("\n"))>0)
		{
			v.add(str.substring(0, idx));
			str = str.substring(idx+1);
		}
		v.add(str);
		String[]	result = new String[v.size()];
		for (int i=0 ; i<v.size() ; i++)
			result[i] = (String) v.elementAt(i);
		return result;
	}
	//===============================================================
	/**
	 *	Execute a shell command and throw exception if command failed.
	 *
	 *	@param cmd	shell command to be executed.
	 */
	//===============================================================
	public static void executeShellCmdAndReturn(String cmd)
			throws IOException
	{
		Process proc = Runtime.getRuntime().exec(cmd);

		// get command's output stream and
		// put a buffered reader input stream on it.
		//-------------------------------------------
		InputStream istr = proc.getInputStream();
		new BufferedReader(new InputStreamReader(istr));

		// do not read output lines from command
		// Do not check its exit value
	}
	//===============================================================
	/**
	 *	Execute a shell command and throw exception if command failed.
	 *
	 *	@param cmd	shell command to be executed.
	 */
	//===============================================================
	public static String executeShellCmd(String cmd)
			throws IOException, InterruptedException, DevFailed
	{
		Process proc = Runtime.getRuntime().exec(cmd);

		// get command's output stream and
		// put a buffered reader input stream on it.
		//-------------------------------------------
		InputStream istr = proc.getInputStream();
		BufferedReader br =
				new BufferedReader(new InputStreamReader(istr));
		String	sb = "";

		// read output lines from command
		//-------------------------------------------
		String str;
		while ((str = br.readLine()) != null)
		{
			//System.out.println(str);
			sb += str+"\n";
		}

		// wait for end of command
		//---------------------------------------
		proc.waitFor();

		// check its exit value
		//------------------------
		int retVal;
		if ((retVal=proc.exitValue()) != 0)
		{
			//	An error occured try to read it
			InputStream errstr = proc.getErrorStream();
			br = new BufferedReader(new InputStreamReader(errstr));
			while ((str = br.readLine()) != null)
			{
				System.out.println(str);
				sb += str+"\n";
			}
			Except.throw_exception("SHELL_CMD_FAILED",
				"the shell command\n" + cmd + "\nreturns : " + retVal
				+ " !\n\n"+ sb,
				"AstorUtil.executeShellCmd()");
		}
		return sb;
	}
	//===============================================================
	//===============================================================
	static private boolean	_osIsUnix = true;
	static private boolean	_osIsUnixTested = false;
	static public boolean osIsUnix()
	{
		if (!_osIsUnixTested)
		{
			try
			{
				String	os = System.getProperty("os.name");
				//System.out.println("Running under " + os);
				_osIsUnix = ! os.toLowerCase().startsWith("windows");
			}
			catch(Exception e)
			{
				//System.out.println(e);
				_osIsUnix = false;
			}
		}
		return _osIsUnix;
	}
	//===============================================================
	//===============================================================
	public static void testDevice(Component parent, String devname)
	{
		JDialog d;
		if (parent instanceof JDialog)
			d = new JDialog((JDialog)parent, false);
		else
			d = new JDialog((JFrame)parent, false);
		d.setTitle(devname + " Device Panel");
		try
		{
			d.setContentPane(new jive.ExecDev(devname));
			ATKGraphicsUtils.centerDialog(d);
			d.setVisible(true);
		}
		catch(DevFailed e)
		{
			ErrorPane.showErrorMessage(parent, null, e);
		}
	}
	//===============================================================
	//===============================================================
	public static void showInHtmBrowser(String url)
	{
	   //  Check for browser
		String  browser;
		if ((browser=System.getProperty("BROWSER"))==null)
		{
			if (AstorUtil.osIsUnix())
				browser = "firefox - turbo";
			else
				browser = "explorer";
		}
		String	cmd = browser + " " + url;
		try
		{
			executeShellCmdAndReturn(cmd);
		}
		catch(Exception e)
		{
			ErrorPane.showErrorMessage(new JFrame(), null, e);
		}
	 }
	//===============================================================
	//===============================================================

	//===============================================================
	//===============================================================
	private static Splash		splash;
	private static int			splash_progress;
	private static ImageIcon	tango_icon = null;
	public static void startSplash(String title)
	{
		//	Create a splash window.
		JSmoothProgressBar myBar = new JSmoothProgressBar();
		myBar.setStringPainted(true);
		myBar.setBackground(Color.lightGray);
		myBar.setProgressBarColors(Color.yellow, Color.yellow, Color.yellow);

		if (tango_icon==null)
			tango_icon = new ImageIcon(
				getInstance().getClass().getResource("/app_util/img/tango.jpg"));
		splash = new Splash(tango_icon, Color.yellow, myBar);
		splash.setTitle(title);
		splash.setMessage("Starting....");
		splash_progress = 0;
		splash.setVisible(true);
		splash.repaint();
	}
	//=======================================================
	//=======================================================
	public static void stopSplash()
	{
		splash_progress = 100;
		splash.progress(splash_progress);
		 splash.setVisible(false);
	}
	//===============================================================
	//===============================================================
	public static void increaseSplashProgress(int i, String message)
	{
		splash_progress += i;
		if (splash_progress>99)
			splash_progress = 99;
		splash.progress(splash_progress);
		splash.setMessage(message);
	}



	//===============================================================
	//===============================================================



	//===============================================================
	//===============================================================
	static private RGB	rgb = null;
	public void initColors(int nb)
	{
		if (rgb==null)
			rgb = new RGB(nb);
		else
			rgb.initColor(nb);

	}
	//===============================================================
	public Color getNewColor()
	{
		if (rgb==null)
			rgb = new RGB();
		return rgb.getNewColor();
	}
	//===============================================================
	//===============================================================
	class RGB
	{
		int	r = 0;
		int	g = 0;
		int	b = 0;
		int	step = 10;
		//===============================================
		RGB()
		{
		}
		//===============================================
		RGB(int nb)
		{
			initColor(nb);
		}
		//===============================================
		void initColor(int nb)
		{
			step  = (4*0xFF)/nb;
			if (step==0xFF) step = 0x80;
			red   = true;
			green = false;
			blue  = false;
			r     =
			g     =
			b     = 0;
			//System.out.println("Nb = " + nb + "    step = " +step);
		}
		private boolean	red   = true;
		private boolean	green = false;
		private boolean	blue  = false;

		//===============================================
		void increase()
		{

			if (red)
			{
				if ((r+step)<0xFF)
					r += step;
				else
				{
					r = 0xFF;
					red   = false;
					green = true;
				}
			}
			else
			if (green)
			{
				if ((g+step)<0xFF)
				{
					if ((r-step)>0)
						r -= step;
					g += step;
				}
				else
				{
					g = 0xFF;
					green = false;
					blue  = true;
				}
			}
			else
			if (blue)
			{
				if ((b+step)<0xFF)
				{
					if ((g-step)>0)
						g -= step;
					b += step;
				}
				else
				{
					b = 0xFF;
					r = 0xFF;
					blue  = false;
				}
			}
			else
				if ((r-step)>0)
				{
					r -= step;
					b -= step;
				}
			//System.out.println("step = " + step + "  rgb = " + r + " - " + g + " - " + b);
		}
		//===============================================
		Color getNewColor()
		{
			increase();
			return new Color(r,g,b);
		}
	}














	//===============================================================
	//===============================================================
	public void sort(Vector[] array)
	{
		for (Vector v : array)
			Collections.sort(v, compare);
	}
	//===============================================================
	//===============================================================
	public void sort(Vector v)
	{
		Collections.sort(v, compare);
	}
	//===============================================================
	//===============================================================




	//======================================================
	/**
	 *	MyCompare class to sort collection
	 */
	//======================================================
	class  MyCompare implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
			String	s1 = o1.toString().toLowerCase();
			String	s2 = o2.toString().toLowerCase();
			if (s1==null)
				return 1;
			else
			if (s2==null)
				return -1;
			else
				return s1.compareTo(s2);
		}
	}
}
