//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code to control host by a thread .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.34  2009/06/02 15:19:05  pascal_verdier
// Remove serialization between HostStateThread and HostInfoDialogVector.
//
// Revision 3.33  2009/04/06 14:27:47  pascal_verdier
// Using MySqlUtil feature.
//
// Revision 3.32  2009/01/30 09:31:50  pascal_verdier
// Black box management added for database.
// Black box management tool improved.
// Find TANGO object by filter added.
//
// Revision 3.31  2008/09/12 11:51:23  pascal_verdier
// Minor changes
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;


/** 
 *	This class is a thread reading servers states and displaying these
 *	states on synopsis.
 *
 * @author  verdier
 */



import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.events.ITangoChangeListener;
import fr.esrf.TangoApi.events.TangoChange;
import fr.esrf.TangoApi.events.TangoChangeEvent;
import fr.esrf.TangoApi.events.TangoEventsAdapter;


public class HostStateThread extends Thread implements AstorDefs
{
	private AstorTree	parent;
	private TangoHost	host;
	private int			readInfoPeriod;
	boolean	stop_it = false;


	private String[]	attributes = { "State", "NotifdState" };

	private static final int	StateAtt  = 0;
	private static final int	NotifdAtt = 1;

	//======================================================================
	/**
	 *	Thread constructor.
	 *
	 *	@param	parent  Apllication.
	 *	@param	host    host object to control.
	 */
	//======================================================================
	public HostStateThread(AstorTree parent, TangoHost host)
	{
		this.parent  = parent;
		this.host      = host;
		//readInfoPeriod = 1000;
		host.thread    = this;
		readInfoPeriod = AstorUtil.getStarterReadPeriod()/2;
	}
	//======================================================================
	//======================================================================
	public synchronized void updateData()
	{
		notify();
	}



	//======================================================================
	/**
	 *	Running thread method.
	 */
	//======================================================================
	public void run()
	{
		/*	Done in AstorTree class to be serialized !
			When subscribed, monitor is updated.
		if (host.use_events)
		{
			//	If on events -> Wait.
			subscribeChangeStateEvent();
		}
		*/
		
		//	Else or failed
		//	Manage polling on synchronous calls
		long		t0 = System.currentTimeMillis();
		while (!stop_it)
		{
			long		t = System.currentTimeMillis();
			if (!host.use_events)
			{
				if (host.do_polling)
					manageSynchronousAttributes();
			}
			else
			{
				if ((t-t0)>60000)
				{
					//	Every minuts, check in synchronous
					//	event could have been lost.
					manageSynchronousAttributes();
					t0 = t;
				}
			}
			wait_next_loop(t);
			//if (host.getName().equals("orion"))
			//	System.out.println(host.use_events);
		}
	}
	//======================================================================
	/**
	 *	Compute time to sleep before next loop
	 */
	//======================================================================
	public synchronized void wait_next_loop(long t0)
	{
		try
		{
			long	t1 = System.currentTimeMillis();
			long	time_to_sleep = readInfoPeriod - (t1-t0);
			
			if (time_to_sleep<=0)
				time_to_sleep = 100;
			wait(time_to_sleep);
		} 
		catch(InterruptedException e){System.out.println(e); }
	}
	//======================================================================
	/**
	 *	Update host window if state has changed
	 */
	//======================================================================
	private DevState	previous_state = DevState.UNKNOWN;
	//public synchronized void updateHost(DevState state)
	public void updateHost(DevState state)
	{
		if (state == previous_state)
			return;
		previous_state = state;

		//	If state has changed, then update host object
		//--------------------------------------------------
		if (state==DevState.ON)
			host.state = all_ok;
		else
		if (state==DevState.MOVING)
			host.state = moving;
		else
		if (state==DevState.ALARM)
			host.state = alarm;
		else
		if (state==DevState.FAULT)
			host.state = faulty;
		else
			host.state = unknown;

		if (parent!=null)
			parent.updateState();

		//System.out.println(host.get_name() + " is " + ApiUtil.stateName(state));

		if (host.info_dialog!=null)
			host.info_dialog.updateHostState();
	}
	//======================================================================
	//======================================================================
	//public synchronized void updateNotifdHost(DevState notifd_state)
	public void updateNotifdHost(DevState notifd_state)
	{
		//	Convert to int
		int	notifyd_state = unknown;
		if (notifd_state == DevState.ON)	notifyd_state = all_ok;
		else
		if (notifd_state == DevState.FAULT)	notifyd_state = faulty;

		if (host.notifyd_state == notifyd_state)
			return;

		host.notifyd_state = notifyd_state;
		if (parent!=null)
			parent.updateState();
		if (host.info_dialog!=null)
			host.info_dialog.updateHostState();
	}
	//======================================================================
	//======================================================================
	public void manageSynchronousAttributes()
	{
		DevState	host_state;
		DevState	notifd_state;
		try
		{
			DeviceAttribute[]	att = host.read_attribute(attributes);

			if (att[StateAtt].hasFailed())
				host_state   = DevState.FAULT;
			else
				host_state   = att[StateAtt].extractState();

			if (att[NotifdAtt].hasFailed())
				notifd_state = DevState.UNKNOWN;
			else
				notifd_state = att[NotifdAtt].extractState();
		}
		catch(DevFailed e)
		{
			//Except.print_exception(e);
			host.except  = e;
			notifd_state = DevState.UNKNOWN;
			host_state   = DevState.FAULT;
		}
		updateHost(host_state);

		if (host.check_notifd)
			updateNotifdHost(notifd_state);
	}







	//=========================================================================
	//
	//	Events management part
	//
	//=========================================================================
	private static String[]			filters = new String[0];
	private	StateEventListener		state_listener = null;
	//======================================================================
	/**
	 *	Subscribe on State events
	 */
	//======================================================================
	public void subscribeChangeStateEvent()
	{
		String	strerror = null;
		try 
		{
			if (host.supplier==null)
				host.supplier = new TangoEventsAdapter(host);

			//	if not already well done, add listener for state_event
			if (state_listener==null)
			{
				state_listener = new StateEventListener();

				host.supplier.addTangoChangeListener(
							state_listener, attributes[StateAtt], filters);
			}
		}
		catch(DevFailed e)
		{
			state_listener = null;
			host.use_events = false;

			//	Display exception 
			if (e.errors[0].desc.startsWith("Already connected to event")==false)
				strerror = "subscribeChangeStateEvent() for " +
							host.get_name() + " FAILED !\n" + e.errors[0].desc ;
			fr.esrf.TangoDs.Except.print_exception(e);
		}
		catch(Exception e)
		{
			state_listener = null;
			host.use_events = false;
			//	Display excetion
			strerror = "subscribeChangeStateEvent() for " +
							host.get_name() + " FAILED !" + e.toString();
			e.printStackTrace();
		}
		if (strerror!=null)
			System.out.println(strerror);
		parent.updateMonitor(strerror);
	}
	//=========================================================================
	/**
	 *	Change State eventz listener
	 */
	//=========================================================================
	class StateEventListener implements ITangoChangeListener
	{
		//=====================================================================
		//=====================================================================
    	public void change(TangoChangeEvent event) {

			//long	t0 = System.currentTimeMillis();
			TangoChange	tc = (TangoChange)event.getSource();
			String		devname = tc.getEventSupplier().get_name();
			DevState	host_state;
			DevState	notifd_state ;

System.out.println("in public void change(TangoChangeEvent event)");
			try
			{
				//	Get the host state from attribute value
			 	DeviceAttribute	attr = event.getValue();
				if (attr.hasFailed())
					host_state = DevState.UNKNOWN;
				else
					host_state = attr.extractState();

	    	}
			catch (DevFailed e)
			{
System.out.println("Recieved a DevFailed :	" + e.errors[0].desc);
				host_state = DevState.ALARM;
				if (e.errors[0].reason.equals("API_EventTimeout"))
				{
					System.out.println("HostStateThread.StateEventListener" +
										devname + " : API_EventTimeout");
					//fr.esrf.TangoDs.Except.print_exception(e);
					//	Check if Starter stopped or notifd
					try
					{
						host.ping();
					}
					catch(DevFailed e2)
					{
						host_state = DevState.FAULT;
					}
				}
				else
				if (e.errors[0].reason.equals("TangoApi_CANNOT_IMPORT_DEVICE"))
				{
					//fr.esrf.TangoDs.Except.print_exception(e);
					System.out.println("HostStateThread.StateEventListener" +
										devname + " : TangoApi_CANNOT_IMPORT_DEVICE");
					host_state = DevState.FAULT;
				}
			}
        	catch (Exception e)
        	{
				System.out.println("AstorEvent." + devname);
 				System.out.println(e);
            	System.out.println("HostStateThread.StateEventListener : could not extract data!");
				host_state = DevState.UNKNOWN;
        	}

			
			try
			{
				//	Check if notify daemon running in synchron
				DeviceAttribute	att_synch = host.read_attribute(attributes[NotifdAtt]);
				if (att_synch.hasFailed())
					notifd_state = DevState.UNKNOWN;
				else
					notifd_state = att_synch.extractState();
				//System.out.println("notifd_state=" + ApiUtil.stateName(notifd_state));
			}
        	catch (Exception e)
			{
				notifd_state = DevState.UNKNOWN;
			}
			updateNotifdHost(notifd_state);

			updateHost(host_state);
		}
	}
}
