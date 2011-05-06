//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011
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


/** 
 *	This class is able to start and stop servers from shell command line.
 *
 * @author  verdier
 */
 
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.DbServer;
import fr.esrf.TangoApi.DbDatum;

import java.util.Vector;
import java.io.*;


public class  AstorCmdLine
{
	private int			action = NOT_INITIALIZED;
	private TangoHost[]	hosts = null;
	private AstorUtil	util;
	
	private static final int		NOT_INITIALIZED = -1;
	private static final int		START_SERVERS   =  0;
	private static final int		STOP_SERVERS    =  1;
	private static final String[]	known_actions = { "start", "stop" };
	//===============================================================
	//===============================================================
	public AstorCmdLine(String[] args) throws DevFailed, Exception
	{
		util = AstorUtil.getInstance();

		//	Check command line
		manageArgs(args);
		
		doAction();
	}
	//===============================================================
	/**
	 *	`Do the action for all levels
	 */
	//===============================================================
	private void doAction()
	{
		int		nb_levels = AstorUtil.getStarterNbStartupLevels();
		switch(action)
		{
		case START_SERVERS:
			for (int level=1 ; level<=nb_levels ; level++)
				doAction(level);
			break;
		case STOP_SERVERS:
			for (int level=nb_levels ; level>=1 ; level--)
				doAction(level);
			break;
		}
	}
	//===============================================================
	/**
	 *	`Do the action for one level
	 */
	//===============================================================
	private void doAction(int level)
	{
		if (getConfirm(level)==false)
		{ 
			System.out.println("Skip level " + level);
			return;
		}
		//	Start for all host at this level
		for (int h=0 ; h<hosts.length ; h++)
		{
			try
			{
				switch(action)
				{
				case STOP_SERVERS:
					if (hosts[h].getName().startsWith("crate")==false)
						hosts[h].stopServers(level);
					break;

				case START_SERVERS:
					if (hosts[h].getName().startsWith("crate")==false)
						hosts[h].startServers(level);
					break;
				}
				System.out.println("	done on " + hosts[h].getName());
			}
			catch (DevFailed e)
			{
				System.out.println("	failed on " + hosts[h].getName());
				System.out.println(e.errors[0].desc);
			}
		}
	}
	//===============================================================
	/**
	 *	Get a confirmation for commands
	 */
	//===============================================================
	private boolean getConfirm(int level)
	{
		String	resp;
		System.out.println("\n");
		do 
		{
			System.out.print(known_actions[action] +
						" all TANGO ds for level " + level + " ?  (y/n) ");
			byte[]	b = new byte[80];
			try {
				System.in.read(b);
				resp = new String(b).toLowerCase().trim();
			}
			catch(java.io.IOException e) {
				resp = "no";
			}
		}
		while(resp.startsWith("n")==false && resp.startsWith("y")==false);

		return (resp.startsWith("y"));
	}
	//===============================================================
	//===============================================================
	private void manageArgs(String[] args) throws DevFailed, Exception
	{
		//	Check args number
		if (args.length<3)
		{
			displaySyntax();
			System.exit(1);
		}


		//	Check args syntax
		for (int i=0 ; i<args.length ; i++)
		{
			//	Check all arguments
			if (args[i].equals("-h"))
			{
				String	s = args[++i];
				if (s.toLowerCase().equals("all"))
				{
					hosts = util.getTangoHostList();
				}
				else
				{
					hosts = new TangoHost[1];
					hosts[0] = new TangoHost(s, true);
				}
			}
			else
			//	Search if action
			for (int j=0 ; j<known_actions.length ; j++)
				if (args[i].equals(known_actions[j]))
					action = j;
		}
		
		//	Check if correctly initialized.
		if (action==NOT_INITIALIZED	||
			hosts==null)
		{
			displaySyntax();
			throw new Exception("Astor Exception");
		}
	}
	//===============================================================
	//===============================================================
	private void displaySyntax()
	{
		System.out.println("Syntax:");
		System.out.println("astor <action> <-h hostname>");
		System.out.println();
		System.out.println("Actions:");
		System.out.println("	start: will start all servers");
		System.out.println("	stop : will stop  all servers");
		System.out.println();
		System.out.println("hostname: host to do it \n");
		System.out.println("i.e.: astor start -h alpha    (starts all servers on host alpha)");
		System.out.println("or    astor start -h all      (starts all servers on all controled hosts)");
	}
	//===============================================================
	//===============================================================





	//===============================================================
	/**
	 *	Another usage of this class
	 */
	//===============================================================
	static final int	REMOVE_POLLING = 0;
	static final int	REMOVE_POLLING_FORCED = 1;
	private static final String	PollAttProp = "polled_attr";
	private	int doWhat;
	public AstorCmdLine(int doWhat, String servname)
	{
		boolean	forced = true;
		try
		{
			switch(doWhat)
			{
			case REMOVE_POLLING:
				forced = false;
			case REMOVE_POLLING_FORCED:
				DeviceProxy[]	devices = getDeviceList(servname);

				if (devices.length==0)
					Except.throw_exception("NO_DEVICES",
								"No device found for " + servname,
								"DbPollPanel.CmdLineSolution("+servname+")");
				System.out.println("Polled Attributes For " + servname);
				displayAndConfirm(devices, forced);
			}
		}
		catch(Exception e) {
			Except.print_exception(e);
		}
	}
	//===============================================================
	//===============================================================
	private void displayAndConfirm(DeviceProxy[] devices, boolean forced) throws DevFailed, IOException
	{
		byte[] b = new byte[80];
		for (int dev=0 ; dev<devices.length ; dev++)
		{
			System.out.println(devices[dev].get_name());
			PolledAttr[]	attlist = getPolledAttributes(devices[dev]);
			boolean[]		remove_it = new boolean[attlist.length];
			for (int a=0 ; a<attlist.length ; a++)
			{
				remove_it[a] = forced;
				if (!forced)
				{
					System.out.print("   - " + attlist[a]);
					System.out.print("  -  Remove polling (y/n) ? ");
					System.in.read(b);
					remove_it[a] = (b[0]=='y' || b[0]=='Y');
				}
			}
			removePolling(devices[dev], attlist, remove_it);
		}
	}
	//===============================================================
	//===============================================================
	private void removePolling(DeviceProxy dev, PolledAttr[] attr, boolean[] remove_it)
			 throws DevFailed
	{
		Vector	v = new Vector();
		for (int i=0 ; i<attr.length ; i++)
			if (!remove_it[i])
			{
				v.add(attr[i].name);
				v.add(attr[i].period);
			}
		String[]	str = new String[v.size()];
		for (int i=0 ; i<v.size() ; i++)
			str[i] = (String)v.get(i);

		DbDatum		argin = new DbDatum(PollAttProp);
		argin.insert(str);
		dev.put_property(new DbDatum[] { argin });

		for (int i=0 ; i<attr.length ; i++)
			if (remove_it[i])
				System.out.println(attr[i].name + " ..... Polling Removed");
	}
	//===============================================================
	//===============================================================
	private DeviceProxy[] getDeviceList(String servname) throws DevFailed
	{
		Vector		v = new Vector();
		DbServer	serv = new DbServer(servname);
		String[]	classes = serv.get_class_list();
		for (String classname : classes)
		{
			String[]	devnames = serv.get_device_name(classname);
			for (String dn  : devnames)
				v.add(dn);
		}
		//	Create device proxy in reverse order
		DeviceProxy[]	dp = new DeviceProxy[v.size()];
		for (int i=0 ; i<v.size() ; i++)
			dp[i] = new DeviceProxy((String)v.get(i));
		return dp;
	}
	//===============================================================
	//===============================================================
	public PolledAttr[] getPolledAttributes(DeviceProxy dev) throws DevFailed
	{
		DbDatum		argout = dev.get_property(PollAttProp);
		String[]	data = argout.extractStringArray();
		if (data==null)
			return new PolledAttr[0];

		Vector	v = new Vector();
		for (int i=0 ; i<data.length ; i+=2)
			v.add(new PolledAttr(data[i], data[i+1]));

		PolledAttr[]	pa = new PolledAttr[v.size()];
		for (int i=0 ; i<v.size() ; i++)
			pa[i] = (PolledAttr)v.get(i);
		return pa;
	}
	//===============================================================
	//===============================================================
	class PolledAttr
	{
		String	name;
		String	period;
		public PolledAttr(String name, String period)
		{
			this.name = name;
			this.period = period;
		}
		public String toString()
		{
			return name + "   (" + period + " ms)";
		}
	}
}
