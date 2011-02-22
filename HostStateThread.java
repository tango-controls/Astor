
package admin.astor;


/** 
 *	This class is a thread reading servers states and displaying these
 *	states on synopsis.
 *
 * @author  verdier
 */
 


import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevError;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.ApiUtil;
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


	private String[]	attributes = { "state", "NotifdState" };

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
		while (!stop_it)
		{
				long		t0 = System.currentTimeMillis();
			if (!host.use_events)
			{
				if (host.do_polling)
					manageSynchronousAttributes();
			}
			wait_next_loop(t0);
			//if (host.getName().equals("esrflinux1-2"))
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

		//System.out.println(host.name() + " is " + ApiUtil.stateName(state));

		if (host.info_dialog!=null)
			host.info_dialog.updateHostState();
	}
	//======================================================================
	//======================================================================
	public synchronized void updateNotifdHost(DevState notifd_state)
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
	private void manageSynchronousAttributes()
	{
		DevState	host_state;
		DevState	notifd_state;
		String[]	starting = new String[0];
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
			if (e instanceof DevFailed)
			{
				//Except.print_exception(e);
				host.except = e;
			}
			else
			{
				DevError[] errors = new DevError[1];
				errors[0].reason = "manageSynchronousAttributes_Failed";
				errors[0].desc   = e.toString();
				errors[0].origin = "HostStateThread(" +
					host.name() + ").manageSynchronousAttributes()";
				host.except = new DevFailed(errors);
				e.printStackTrace();
			}
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
		long	t0 = System.currentTimeMillis();
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
			strerror = "subscribeChangeStateEvent() for " +
							host.name() + " FAILED !\n" + e.errors[0].desc ;
			fr.esrf.TangoDs.Except.print_exception(e);
		}
		catch(Exception e)
		{
			state_listener = null;
			host.use_events = false;
			//	Display excetion 
			strerror = "subscribeChangeStateEvent() for " +
							host.name() + " FAILED !" + e.toString();
			e.printStackTrace();
		}
		long	t1 = System.currentTimeMillis();
		if (strerror!=null)
			System.out.println(strerror);
		//else
		//	System.out.println("subscribeChangeStateEvent() " + host.name() + " : "+ (t1-t0) + " ms");
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
			String		devname = tc.getEventSupplier().name();
			DevState	host_state   = DevState.UNKNOWN;
			DevState	notifd_state = DevState.UNKNOWN;

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
				host_state = DevState.ALARM;
				notifd_state = DevState.UNKNOWN;
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
				notifd_state = DevState.UNKNOWN;
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
