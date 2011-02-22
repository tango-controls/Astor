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

//import admin.astor.*;


/**
 *	This class group many info and methods used By Astor.
 *
 * @author  verdier
 */


import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.ErrSeverity;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.Vector;

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
							"Tools",
							"HtmlHelps"
						};
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
    public static void setKnownTangoHosts(String[] kth)
    {
		known_tango_hosts = kth;
    }
	//===============================================================
	//===============================================================
	static void readAstorProperties()
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
		data[i++].insert(tools);

		data[i] = new DbDatum(astor_propnames[i]);
		data[i++].insert(helps);

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
		for (int i=0 ; i<hosts.length ; i++)
		{
            //  Check if collection property is defined.
            if (hosts[i].collection==null)
                hosts[i].collection = "Miscellaneous";

            //  Check if this collection already exists
            boolean found = false;
			for (int j=0 ; j<vect.size() && !found ; j++)
				found = (hosts[i].collection.equals(vect.elementAt(j)));

			//	If not already exists add it
			if (!found)
				vect.add(hosts[i].collection);
		}

		//	Sort for alphabetical order
		Collections.sort(vect, compare);

		//	Add database as first element
		vect.add(0, "Tango Database");
		
		//	Check if default Bottom collection
        String[]	last = getLastCollectionList();;
		//	Check if collections exist
		for (int i=0 ; i<last.length ; i++)
		{
			boolean	found = false;
			for (int j=0 ; !found && j<vect.size() ; j++)
			{
				//	 put it at end of vector
				if (found=last[i].equals(vect.get(j).toString()))
				{
					String	s = vect.get(j).toString();
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
		//----------------------------------
		String []	hostnames = getHostControlledList();

		//	And create TangoHost array object
		//----------------------------------------
		TangoHost[]	hosts = new TangoHost[hostnames.length];
		for(int i=0, j=0 ; i<hostnames.length ; i++, j++)
			hosts[j] = new TangoHost(hostnames[i]);
		return hosts;
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
		return System.getProperty("TANGO_HOST");
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
	public static void executeShellCmdAndReturn(String cmd) throws Exception
	{
		Process proc = Runtime.getRuntime().exec(cmd);

		// get command's output stream and
		// put a buffered reader input stream on it.
		//-------------------------------------------
		InputStream istr = proc.getInputStream();
		BufferedReader br =
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
	public static String executeShellCmd(String cmd) throws Exception
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
		for (int i=0 ; i<array.length ; i++)
			Collections.sort(array[i], compare);
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
