//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the CheckServer class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
//
// Copyleft 2005 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;


/** 
 *	This class is able to
 *
 * @author  verdier
 * @Revision 
 */
 
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;

import java.awt.*;
import java.awt.event.*;



public class  CheckServer
{
	private static final String[]	actions = {
					"start",
					"stop",
					"restart",
					"ping"
				};
	private static String		starter_name;
	private static String		servname;
	private static String		action;
	private static DeviceProxy	starter;
	//===============================================================
	//===============================================================
	public CheckServer(String[] args) throws DevFailed
	{
		if (checkSyntax(args)==false)
			System.exit(0);
		starter = new DeviceProxy(starter_name);
	}
	//===============================================================
	//===============================================================
	public void doAction() throws DevFailed
	{
		if (action.equals("start"))
		{
			execute("DevStart");
			System.out.println(servname + " started");
		}
		else
		if (action.equals("stop"))
		{
			execute("DevStop");
			System.out.println(servname + " stopped");
		}
		else
		if (action.equals("restart"))
		{
			restart();
			System.out.println(servname + " restarted");
		}
		else
		if (action.equals("ping"))
		{
			new DeviceProxy("dserver/"+servname).ping();
			System.out.println(servname + " is alive");
		}
	}
	//===============================================================
	//===============================================================
	private void execute(String cmd) throws DevFailed
	{
		DeviceData	argin = new DeviceData();
		argin.insert(servname);
		starter.command_inout(cmd, argin);
	}
	//===============================================================
	//===============================================================
	private void restart() throws DevFailed
	{
		//	Stop it (do no throw exception if already stopped)
		try {
			execute("DevStop");
			System.out.println(servname + " stopped");
		}
		catch(DevFailed e)
		{
			if (e.errors[0].reason.indexOf("NOT running !")<0)
				throw e;
		}

		//	Wait to be sure it is stopped
		int	nb_retries = 5;
		for (int i=0 ; i<nb_retries ; i ++)
		{
			sleep(1000);
			DeviceData	argin = new DeviceData();
			argin.insert(true);
			DeviceData	argout = starter.command_inout("DevGetStopServers", argin);
			String[]	servers = argout.extractStringArray();
			for (int j=0 ; j<servers.length ; j++)
			{
				if (servers[j].equals(servname))
					i = nb_retries;
			}
		}
		
		//	Restart it
		execute("DevStart");
	}
	//===============================================================
	//===============================================================
	private synchronized void sleep(long ms)
	{
		try
		{
			wait(ms);
		}
		catch(InterruptedException e) {}
	}



	//===============================================================
	//===============================================================
	private void displaySyntax(String script_name)
	{
		//	remove script path
		int	start;
		if ((start=script_name.lastIndexOf("/"))>0)
			script_name = script_name.substring(start+1);
		System.out.println("Syntax :"); 
		System.out.println(script_name + "  hosname  servname  action");
		System.out.println("	hosname  : Host name where server running");
		System.out.println("	servname : Server name (e.g. VacGauge/sr_c02)");
		System.out.print("	action   : ");
		for (int i=0 ; i<actions.length ; i++)
		{
			if (i>0)
				System.out.print(" / ");
			System.out.print(actions[i]);
		}
		System.out.println();
	}
	//===============================================================
	//===============================================================
	private boolean checkSyntax(String[] args)
	{
		if (args.length < 4)
		{
			displaySyntax(args[0]);
			return false;
		}
		starter_name  = "tango/admin/" + args[1];
		servname = args[2];
		action = args[3].toLowerCase();

		//	Check server name
		if (servname.indexOf("/")<0)
		{
			System.out.println("Server name syntax error !");
			displaySyntax(args[0]);
			return false;
		}
		
		//	Check action name
		boolean found = false;
		for (int i=0 ; !found && i<actions.length ; i++)
			found = action.equals(actions[i]);
		if (!found)
		{
			System.out.println("action name syntax error !");
			displaySyntax(args[0]);
			return false;
		}
		return true;
	}
	//===============================================================
	//===============================================================
	public static void main (String[] args)
	{
		CheckServer	client = null;
		try
		{
			client = new CheckServer(args);
			client.doAction();
		}
		catch(DevFailed e)
		{
			Except.print_exception(e);
		}
	}
}
