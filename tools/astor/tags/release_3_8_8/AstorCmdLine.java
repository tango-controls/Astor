//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the AstorCmdLine class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.1  2005/04/25 08:55:36  pascal_verdier
// Start/Stop servers from shell command line added.
//
//
// Copyleft 2005 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;


/** 
 *	This class is able to start and stop servers from shell command line.
 *
 * @author  verdier
 * @Revision 
 */
 
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;



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
	private void doAction() throws DevFailed, Exception
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
			throw new Exception("Astor Exception");
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
					hosts[0] = new TangoHost(s);
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
		System.out.println("hostname: host to do it (or -h all for all hosts)");
	}
}
