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
// Revision 3.11  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.10  2004/07/09 08:12:49  pascal_verdier
// HostInfoDialog is now awaken only on servers change.
//
// Revision 3.9  2004/07/08 11:22:58  pascal_verdier
// First revision able to use events.
//
// Revision 3.8  2004/05/04 07:05:27  pascal_verdier
// Bug on notify daemon fixed.
// server reconection transparency added.
//
// Revision 3.7  2004/03/03 08:31:04  pascal_verdier
// The server restart command has been replaced by a stop and start command in a thread.
// The delete startup level info has been added.
//
// Revision 3.6  2004/02/04 14:37:43  pascal_verdier
// Starter logging added
// Database info added on CtrlServersDialog.
//
// Revision 3.5  2003/11/25 15:56:46  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
// Revision 3.4  2003/11/05 10:34:57  pascal_verdier
// Main Panel screen centering.
// Starter multi path added.
// little bugs fixed.
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
 
//import admin.astor.TangoServer;
//import admin.astor.AstorDefs;
//import admin.astor.AstorUtil;


/** 
 *	This class is a thread reading servers states and displaying these
 *	states on synopsis.
 *
 * @author  verdier
 * @Revision 
 */
 


import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoApi.events.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;


public class HostStateThread extends Thread implements AstorDefs
{
	private AstorTree	parent;
	private TangoHost	host;
	private	DevState	devstate = DevState.UNKNOWN;
	private int			readInfoPeriod;
	boolean	stop_it = false;

	//======================================================================
	/**
	 *	Thread constructor.
	 *
	 *	@param	dbBtn	database button tu update.
	 *	@param	btn		hoat buttons to update.
	 *	@param	hosts	os=bjects to be read (servers and device).
	 */
	//======================================================================
	public HostStateThread(AstorTree parent, TangoHost host)
	{
		this.parent = parent;
		this.host   = host;

		readInfoPeriod = AstorUtil.getStarterReadPeriod()/2;
	}

	//======================================================================
	//======================================================================
	public synchronized void updateData()
	{
		notify();
	}
	//======================================================================
	//======================================================================
	private void readState()
	{
		devstate = DevState.UNKNOWN;
		//	Use synchron calls
		try
		{
			//	Get Starter state
			DeviceData argout = host.command_inout("State");
			host.except = null;
			devstate = argout.extractDevState();
		}
		catch (DevFailed e)
		{
			host.except = e;
			devstate = DevState.FAULT;
		}
		updateHost(devstate);
	}
	//======================================================================
	//======================================================================
	//======================================================================
	/**
	 *	Running thread method.
	 */
	//======================================================================
	public void run()
	{
		if (false && AstorUtil.getDebug())
			System.out.println("Thread started for " + host.getName() + " ->	"
								+ readInfoPeriod + " millisec.");

		//	Wait a bit befor starting (Wait GUI creation end)
		//---------------------------------------------
		if (host.use_events)
			try { sleep(500); }catch(Exception e){}

		//	Starting infinite loop
		//----------------------------		
		while (stop_it==false)
		{
			long		t0 = System.currentTimeMillis();
			if (host.do_polling)
			{
				//	Check if events notify is OK.
				if (host.use_events)
				{
					//readNotifydState();
					//if (event_supplier==null)
					if (state_listener==null)
						subscribeChangeStateEvent();
				}
				else
					readState();

				//	check if Starter DS is OK
				//-----------------------------------------
				if (host.poll_serv_lists)
				{
					if (host.use_events)
						subscribeChangeServerEvent();
					else
						controlServers();
				}
			}
			else
			{
				if (host.use_events==false)
					updateHost(DevState.UNKNOWN);
			}

			//	Wait a bit befor next update
			//	or to be awaken by another process.
			//---------------------------------------------
			wait_next_loop(t0);

		}	//	End of While(true)
		
		unsubscribeStateEvent();
		System.out.println(host + " thread stopped !");
	}
	//======================================================================
	//======================================================================
	public synchronized void wait_next_loop(long t0)
	{
		try
		{
			long	t1 = System.currentTimeMillis();
			long	time_to_sleep = readInfoPeriod - (t1-t0);
			
			//System.out.println("sleep : " + time_to_sleep);
			if (time_to_sleep<=0)
				time_to_sleep = 100;
			wait(time_to_sleep);
			//System.out.println((t1-t0) + " ms");
		} 
		catch(InterruptedException e){System.out.println(e); }
	}
	//======================================================================
	//======================================================================
	private DevState	previous_state = DevState.UNKNOWN;
	public synchronized void updateHost(DevState state)
	{
		if (state == previous_state)
			return;
		previous_state = state;

		//	If state has changed, then update host object
		//--------------------------------------------------
		if (state==DevState.ON)
			host.state = all_ok;
		else
		if (state==DevState.ALARM)
			host.state = alarm;
		else
		if (state==DevState.FAULT)
			host.state = faulty;
		else
			host.state = unknown;
		parent.updateState();
	}
	//======================================================================
	//======================================================================
	public synchronized void updateHost(int notifyd_state)
	{
		if (host.notifyd_state == notifyd_state)
			return;

		host.notifyd_state = notifyd_state;
		parent.updateState();
		if (host.info_dialog!=null)
			host.info_dialog.updateData();
	}
	//======================================================================
	//======================================================================
	private String[]	prev_running = null;
	private String[]	prev_stopped = null;
	public synchronized void updateHost(String[] running, String[] stopped)
	{
		boolean	changed = false;
		
		//	First time, search if something has changed
		if (prev_running==null || prev_stopped==null)
			changed = true;
		else
		if ((running!=null && prev_running.length != running.length) ||
			(stopped!=null && prev_stopped.length != stopped.length) )
			changed = true;
		else
		if (running==null || stopped==null)
			changed = true;
		else
		{

			for (int i=0 ; i<running.length && !changed ; i++)
				if (prev_running[i].equals(running[i])==false)
					changed = true;
			for (int i=0 ; i<stopped.length && !changed ; i++)
				if (prev_stopped[i].equals(stopped[i])==false)
					changed = true;
		}
		// if nothing has changed do nothing.
		if (!changed)
			return;

		//	Else, update tangoHost object

		//	Check if some of them have disappeared
		for (int i=host.nbServers()-1 ; i>=0; i--)
		{
			TangoServer	server = host.getServer(i);
			boolean		found = false;
			
			//	If server running cannot have disepeared
			if (server.isRunning())
				found = true;
			else
			if (stopped!=null)
				for(int j=0 ; !found && j<stopped.length ; j++)
					found = server.getName().equals(stopped[j]);

			//	If yes, remove them
			if (!found)
				host.removeServer(i);
		}
		//	update TangoHost object state (running)
		if (running==null)
			running = new String[0];
		for(int i=0 ; i<running.length ; i++)
			try {
				TangoServer	server = host.getServer(running[i]);
				if (server==null)
					host.addServer(new TangoServer(running[i], true));
				else
					server.setRunning(true);
			}
			catch(DevFailed e) { }
	
		//	update TangoHost object state (stopped)
		if (stopped==null)
			stopped = new String[0];
		for(int i=0 ; i<stopped.length ; i++)
			try {
				TangoServer	server = host.getServer(stopped[i]);
				if (server==null)
					host.addServer(new TangoServer(stopped[i], false));
				else
					server.setRunning(false);
			}
			catch(DevFailed e) { }
		

		//	And update saved data
		prev_running = new String[running.length];
		prev_stopped = new String[stopped.length];
		for(int i=0 ; i<running.length ; i++)
			prev_running[i] = running[i];
		
		//	update TangoHost object
		for(int i=0 ; i<stopped.length ; i++)
			prev_stopped[i] = stopped[i];

		if (host.info_dialog!=null)
			host.info_dialog.updateData();
	}
	//======================================================================
	/**
	 *	Test, through Starter DS, for a host if the device servers
	 *	controlled are running or not.
	 */
	//======================================================================
	private synchronized void controlServers()
	{
		String[]	runningServers = new String[0];
		String[]	stoppedServers = new String[0];
		try
		{
			DeviceData	din = new DeviceData();
			//	Get the running servers list
			din.insert(host.all_servers);
			DeviceData	dout = 
				host.command_inout("DevGetRunningServers", din);
			runningServers = dout.extractStringArray();

			//	Get the stopped servers list
			DeviceData	dout2 = host.command_inout("DevGetStopServers", din);
			stoppedServers = dout2.extractStringArray();
		}
		catch(DevFailed e)
		{
			host.except = e;
			return;
		}
		catch(Exception e)
		{
			try
			{
				Except.throw_exception(e.toString(),
					"Cannont extract Data", "HostStateThread");
			}
			catch(DevFailed df)
			{
				host.except = df;
				return;
			}
		}
		updateHost(runningServers, stoppedServers);
	}






	//=========================================================================
	//
	//	Events management part
	//
	//=========================================================================
	private static String			stateAttr = "HostState";
	private static String[]			serversAttr =
						{ "RunningServers", "StoppedServers" };
	private static String[]			filters = new String[0];
	private TangoEventsAdapter		event_supplier = null;
	private	StateEventListener		state_listener = null;
	private	ServerEventListener[]	server_listener = null;
	//======================================================================
	//======================================================================
	private void subscribeChangeStateEvent()
	{
		long	t0 = System.currentTimeMillis();
		try 
		{
			event_supplier = new TangoEventsAdapter(host);

			//	if not already weel done, add listener for state_event
			if (state_listener==null)
			{
				state_listener = new StateEventListener();

				event_supplier.addTangoChangeListener(
							state_listener, stateAttr, filters);
			}
		}
		catch(DevFailed e)
		{
			state_listener = null;
			readState();
			host.use_events = false;

			//	Display exception 
			System.out.println("subscribeChangeStateEvent() for " +
					host.name() + " FAILED !");
			fr.esrf.TangoDs.Except.print_exception(e);
		}
		catch(Exception e)
		{
			state_listener = null;
			//	Display excetion 
			System.out.println("subscribeChangeStateEvent() for " +
					host.name() + " FAILED !");
			System.out.println(e);
			e.printStackTrace();
		}
		long	t1 = System.currentTimeMillis();
		System.out.println("subscribeChangeStateEvent() " + host.name() + " : "+ (t1-t0) + " ms");
		parent.updateMonitor();
	}
	//======================================================================
	//======================================================================
	private void subscribeChangeServerEvent()
	{
		//	Allocate Array of listeners
		if (server_listener==null)
		{
			server_listener = new ServerEventListener[serversAttr.length];
			for (int i=0 ; i<serversAttr.length ; i++)
				server_listener[i] = null;
		}

		for (int i=0 ; i<serversAttr.length ; i++)
		{
			//	if not already weel done, add listener for servers list
			if (server_listener[i]==null)
			{
				try 
				{
					//	add listener for double_event and server_event
					server_listener[i] = new ServerEventListener();

					event_supplier.addTangoChangeListener(
								server_listener[i], serversAttr[i], filters);
					System.out.println("subscribeChangeServerEvent() for " +
							host.name() + "/" + serversAttr[i] + " OK!");
				}
				catch(DevFailed e)
				{
					server_listener[i] = null;
					//	Display excetion 
					System.out.println("subscribeChangeServerEvent() for " +
							host.name() + " FAILED !");
					fr.esrf.TangoDs.Except.print_exception(e);
				}
			}
		}
	}
	//======================================================================
	/**
	 *  Unsubscribe events
	 */
	//======================================================================
	private void unsubscribeStateEvent()
	{
		if (event_supplier!=null && state_listener!=null)
			try
			{
				event_supplier.removeTangoChangeListener(state_listener, stateAttr);
				System.out.println("unsubscribe event for " + host + "/" + stateAttr);
			}
			catch(DevFailed e)
			{
				System.out.println("Failed to unsubscribe event for " + stateAttr);
				fr.esrf.TangoDs.Except.print_exception(e);
			}
	}
	//======================================================================
	//======================================================================
	private int readNotifydState()
	{
		int	notifyd_state;

		if (host.use_events==false)
			return all_ok;

		//	Get the notify daemon state
		//--------------------------------------
		notifyd_state = unknown;
		try
		{
			//	Get the notify daemon state
			DeviceData	dout = host.command_inout("NotifyDaemonState");
			boolean		running = (dout.extractDevState()==DevState.ON);
/***
if (host.getName().equals("wow"))
System.out.println("Checking notifyd state - running : " + running +
					ApiUtil.stateName(dout.extractDevState()));
*****/
			if (running)
				notifyd_state = all_ok;
			else
				notifyd_state = faulty;
		}
		catch(DevFailed e)
		{
			if (e.errors[0].reason.equals("NOTIFY_NOT_AVAILABLE") ||
				e.errors[0].reason.equals("API_CommandNotFound")    )
				notifyd_state = unknown;
			else
			{
				notifyd_state = unknown;
				Except.print_exception(e);
			}
		}
		catch(Exception e)
		{
			try
			{
				notifyd_state = unknown;
				Except.throw_exception(e.toString(),
					"Cannont extract Data", "HostStateThread");
			}
			catch(DevFailed df)
			{
				Except.print_exception(df);
				//return;
			}
		}
/****			
if (host.getName().equals("wow"))
System.out.println(" = " + notifyd_state);
/*****/
		return notifyd_state;
	}
	//=========================================================================
	//=========================================================================









	//=========================================================================
	/**
	 *	State event listener
	 */
	//=========================================================================
	class StateEventListener implements ITangoChangeListener
	{
		//=====================================================================
		//=====================================================================
    	public void change(TangoChangeEvent event) {

			//long	t0 = System.currentTimeMillis();
			TangoChange	tc = (TangoChange)event.getSource();
			String		devname = tc.getEventSupplier().name();
			devstate = DevState.UNKNOWN;
			int	notifyd_state = all_ok;

			try
			{
				//	Get the host state from attribute value
			 	DeviceAttribute	attr = event.getValue();
				short	state_value = attr.extractShort();
				devstate = DevState.from_int(state_value);

				//	Check if notify daemon running
				notifyd_state = readNotifydState();
				
	    	}
			catch (DevFailed e)
			{
				devstate = DevState.ALARM;
				if (e.errors[0].reason.equals("API_EventTimeout"))
				{
					notifyd_state = unknown;
					System.out.println("HostStateThread.StateEventListener" +
										devname + " : API_EventTimeout");
					//fr.esrf.TangoDs.Except.print_exception(e);
				}
				else
				if (e.errors[0].reason.equals("TangoApi_CANNOT_IMPORT_DEVICE"))
				{
					//fr.esrf.TangoDs.Except.print_exception(e);
					System.out.println("HostStateThread.StateEventListener" +
										devname + " : TangoApi_CANNOT_IMPORT_DEVICE");
					devstate = DevState.FAULT;
				}
			}
        	catch (Exception e)
        	{
				System.out.println("AstorEvent." + devname);
 				System.out.println(e);
            	System.out.println("HostStateThread.StateEventListener : could not extract data!");
				devstate = DevState.UNKNOWN;
        	}
			System.out.println("State changed on " + devname + " : " +
							ApiUtil.stateName(devstate));
			updateHost(devstate);
			updateHost(notifyd_state);

			//long	t1 = System.currentTimeMillis();
			//System.out.println("StateEventListener.change() " +
			//		devname  + " : " + (t1-t0) + " ms");
		}
	}
	//=========================================================================
	/**
	 *	Server event listener
	 */
	//=========================================================================
	class ServerEventListener implements ITangoChangeListener
	{
		//=====================================================================
		//=====================================================================
    	public void change(TangoChangeEvent event) {

			TangoChange	tc = (TangoChange)event.getSource();
			String		devname = tc.getEventSupplier().name();
			String[]	servers = new String[0];
			String		attname = null;
			
			try
			{
			 	DeviceAttribute	attr = event.getValue();
				attname = attr.getName();
				servers = attr.extractStringArray();
				System.out.println(devname + "/" + attr.getName() + " changed " + " : ");
				/*
				for (int i=0 ; i<servers.length ; i++)
					System.out.println("	" + servers[i]);
				*/
	    	}
			catch (DevFailed e)
			{
				if (e.errors[0].reason.equals("API_EventTimeout"))
				{
					System.out.println("HostStataThread.ServerEventListener" +
										devname + " : API_EventTimeout");
					//fr.esrf.TangoDs.Except.print_exception(e);
				}
				//else
					//fr.esrf.TangoDs.Except.print_exception(e);
			}
        	catch (Exception e)
        	{
				System.out.println("AstorEvent." + devname);
 				System.out.println(e);
            	System.out.println("HostStateThread.ServerEventListener : could not extract data!");
        	}
			if (attname!=null)
				if (attname.equals(serversAttr[0]))
					updateHost(servers, prev_stopped);
				else
					updateHost(prev_running, servers);
		}
	}

}
