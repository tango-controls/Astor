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
// Revision 3.10  2006/06/13 13:52:14  pascal_verdier
// During StartAll command, sleep(500) added between 2 hosts.
// MOVING states added for collection.
//
// Revision 3.9  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.8  2005/08/11 15:01:34  pascal_verdier
// Bug on Start/Stop all command fixed.
//
// Revision 3.7  2005/07/07 12:40:13  pascal_verdier
// Do not start notify daemon at 'start all' button clicked.
//
// Revision 3.6  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.5  2004/07/08 11:22:58  pascal_verdier
// First revision able to use events.
//
// Revision 3.4  2004/05/04 07:05:27  pascal_verdier
// Bug on notify daemon fixed.
// server reconection transparency added.
//
// Revision 3.3  2004/02/04 14:37:43  pascal_verdier
// Starter logging added
// Database info added on CtrlServersDialog.
//
// Revision 3.2  2003/11/25 15:56:45  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
// Revision 3.0  2003/06/04 12:37:52  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.1  2003/06/04 12:33:12  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.0  2003/01/16 15:22:35  verdier
// Last ci before CVS usage
//
// Revision 1.6  2002/09/13 08:43:07  verdier
// Use IDL 2 Starter version (polling thread, State from Starter, ...).
// Host info window not modal.
// Host info window resizable for display all servers option.
// And many features.
//
// Revision 1.5  2001/05/30 15:13:29  verdier
// Start/Stop host control added
// Jive statup aded
// and many app_util added...
//
// Revision 1.4  2001/01/09 14:58:33  verdier
// Start and stop all servers added.
// Progress Monitor added.
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;
 

/** 
 *	This class is a thread to send command to all servers.
 *
 * @author  verdier
 */
 
 
import fr.esrf.Tango.DevFailed;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;


public class ServerCmdThread extends Thread implements AstorDefs
{
	private Component	parent;
	private TangoHost[]	hosts;
	private int			cmd;
	private Monitor		monitor;
	private	boolean[]	levelUsed;
	private	short		nbStartupLevels;
	private String		monitor_title;

	private boolean		from_array = true;
	private Vector		levels;

	//=======================================================
	/**
	 *	Thread Constructor for many hosts.
	 *	@param	parent	The application parent used as parent
	 *			for ProgressMinitor.
	 *	@param	hosts	The controlled hosts.
	 *	@param	cmd	command to be executed on all hosts.
	 */
	//=======================================================
	public ServerCmdThread(Component parent, TangoHost[] hosts, int cmd)
	{
		this.parent = parent;
		this.hosts  = hosts;
		this.cmd    = cmd;
		monitor_title = " on all controlled hosts   ";
		
		nbStartupLevels = AstorUtil.getStarterNbStartupLevels();
		levelUsed = new boolean[nbStartupLevels];
		for (int i=0 ; i<nbStartupLevels ; i++)
			levelUsed[i] = true;
	}
	//=======================================================
	/**
	 *	Thread Constructor for one host.
	 *	@param	parent	The application parent used as parent
	 *			for ProgressMinitor.
	 *	@param	host	The controlled host.
	 *	@param	cmd		command to be executed on all hosts.
	 *	@param	levelUsed	true if level is used by server on this host.
	 */
	//=======================================================
	public ServerCmdThread(Component parent, TangoHost host, int cmd, boolean[] levelUsed)
	{
		this.parent = parent;
		
		this.hosts     = new TangoHost[1];
		this.hosts[0]  = host;
		this.cmd       = cmd;
		this.levelUsed = levelUsed;
		monitor_title = " on " + host + "   ";
		nbStartupLevels = AstorUtil.getStarterNbStartupLevels();
	}
	//=======================================================
	/**
	 *	Thread Constructor for one host.
	 *	@param	parent	The application parent used as parent
	 *			for ProgressMinitor.
	 *	@param	host	The controlled host.
	 *	@param	cmd		command to be executed on all hosts.
	 *	@param	levels	list of levels
	 */
	//=======================================================
	public ServerCmdThread(Component parent, TangoHost host, int cmd, Vector levels)
	{
		this.parent = parent;
		
		this.hosts     = new TangoHost[1];
		this.hosts[0]  = host;
		this.cmd       = cmd;
		this.levels    = levels;
		monitor_title = " on " + host + "   ";
		nbStartupLevels = AstorUtil.getStarterNbStartupLevels();
		from_array = false;
	}
	//=======================================================
	/**
	 *	Update the PrograessMonitor
	 */
	//=======================================================
	private void updateProgressMonitor(int level, int hostnum, double ratio)
	{
		String	message;
		if (monitor==null)
		{
			message = new String(cmdStr[cmd] + monitor_title);
			if (parent instanceof JDialog)
				monitor = new Monitor((JDialog)parent, message,	cmdStr[cmd]);
			else
			if (parent instanceof JFrame)
				monitor = new Monitor((JFrame)parent, message,	cmdStr[cmd]);
		}
		
		message = cmdStr[cmd] + "Servers on " + hosts[hostnum].getName() +
				" for level " + level;

		//System.out.println(hostnum + " -> " + ratio);
		monitor.setProgressValue(ratio, message);
	}
	
	//=======================================================
	/**
	 *	Execute the servers commands.
	 */
	//=======================================================
	public void run()
	{
		//	Initialize from properties
		//--------------------------------
		AstorUtil.getStarterNbStartupLevels();

		//	Start progress monitor
		//-----------------------------
		updateProgressMonitor(0, 0, 0.05);

		//	For each startup level
		//	(Increase for start or decrease for stop)
		//---------------------------------------------------
		if (from_array)
		{
			switch(cmd)
			{
			case StartAllServers:
				for (int level=1 ; monitor.isCanceled()==false &&
									level<=nbStartupLevels ; level++)
				{
					if (levelUsed[level-1])
					{
						int option = JOptionPane.showConfirmDialog(parent, 
									new String(cmdStr[cmd] + " for level "+level),
									"",
									JOptionPane.YES_NO_CANCEL_OPTION);

						if (option==JOptionPane.CANCEL_OPTION)
							level = nbStartupLevels;
						else
						{
							boolean do_it = (option==JOptionPane.OK_OPTION);
							executeCommand(hosts, level, do_it);
						}
					}
				}
				break;

			case StopAllServers:
				for (int level=nbStartupLevels ; monitor.isCanceled()==false &&
									level>0 ; level--)
				{
					if (levelUsed[level-1])
					{
						int option = JOptionPane.showConfirmDialog(parent, 
									new String(cmdStr[cmd] + " for level "+level),
									"",
									JOptionPane.YES_NO_CANCEL_OPTION);
						if (option==JOptionPane.CANCEL_OPTION)
							level = 0;
						else
						{
							boolean	do_it = (option==JOptionPane.OK_OPTION);
							executeCommand(hosts, level, do_it);
						}
					}
				}
				break;
			}
		}
		else	//	New version from a vector
		{
			for (int l=0 ; l<levels.size() ; l++)
			{
				int	level = ((Integer)levels.get(l)).intValue();
				int option = JOptionPane.showConfirmDialog(parent, 
							new String(cmdStr[cmd] + " for level "+level),
							"",
							JOptionPane.YES_NO_CANCEL_OPTION);

				switch (option)
				{
				case JOptionPane.CANCEL_OPTION:
					l = levels.size();
					break;
				case JOptionPane.OK_OPTION:
					executeCommand(hosts, level, true);
					break;
				case JOptionPane.NO_OPTION:
					break;
				}
			}
		}
		monitor.setProgressValue(100.0);
	}
	
	//============================================================
	//============================================================
	private void executeCommand(TangoHost[] hosts, int level, boolean doThisLevel)
	{
		//	For each host
		//-------------------
		for (int i=0 ; doThisLevel &&
					monitor.isCanceled()==false && i<hosts.length ; i++)
		{
			TangoHost	host = hosts[i];
			double		ratio;

			//-------------------------------------
			//	And Execute the command
			//----------------------------
			try
			{
				switch(cmd)
				{
				case StartAllServers:
					//	Update the Progress Monitor depends on start/stop
					ratio = ((double)(level+1) * hosts.length + i) /
							(hosts.length * (nbStartupLevels+2));
					updateProgressMonitor(level, i, ratio);

					//	Do command
					host.startServers(level);
					//	wait a bit just to display bargraph
					try {sleep(500);} catch(Exception e){}
					break;
					
				case StopAllServers:
					//	Update the Progress Monitor depends on start/stop
					ratio = ((double)(nbStartupLevels-level+1) * hosts.length + i) /
							(hosts.length * (nbStartupLevels+2));
					updateProgressMonitor(level, i, ratio);

					//	Do command
					host.stopServers(level);
					//	wait a bit just to display bargraph
					try {sleep(50);} catch(Exception e){}
					break;
				}
			}
			catch(DevFailed e){}
			//try {sleep(500);} catch(Exception e){}
			host.updateData();
		}
	}
}
