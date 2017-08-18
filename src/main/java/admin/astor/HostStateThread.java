//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,
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

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.events.ITangoChangeListener;
import fr.esrf.TangoApi.events.TangoChange;
import fr.esrf.TangoApi.events.TangoChangeEvent;
import fr.esrf.TangoApi.events.TangoEventsAdapter;

import javax.swing.*;
import java.util.Date;

/**
 *	This class is a thread reading servers states and displaying these
 *	states on synopsis.
 *
 * @author verdier
 */
public class HostStateThread extends Thread implements AstorDefs {
    private AstorTree parent;
    private TangoHost host;
    private int readInfoPeriod;
    boolean stop_it = false;


    private String[] attributes = {"State", "NotifdState"};

    private static final int StateAtt = 0;
    private static final int NotifdAtt = 1;

    //======================================================================
    /**
     * Thread constructor.
     *
     * @param host   host object to control.
     */
    //======================================================================
    public HostStateThread(TangoHost host) {
        this(null, host);
    }
    //======================================================================
    /**
     * Thread constructor.
     *
     * @param parent Apllication.
     * @param host   host object to control.
     */
    //======================================================================
    public HostStateThread(AstorTree parent, TangoHost host) {
        this.parent = parent;
        this.host = host;
        host.thread = this;
        readInfoPeriod = AstorUtil.getStarterReadPeriod() / 2;
    }

    //======================================================================
    //======================================================================
    public synchronized void updateData() {
        notify();
    }


    //======================================================================
    /**
     * Running thread method.
     */
    //======================================================================
    public void run() {
        /*
         *	Done in AstorTree class to be serialized !
         *    When subscribed, monitor is updated.
         *     Do it only if not from astor
         */
          if (parent==null && host.onEvents)  {
              subscribeChangeStateEvent();
          }

        //	Else or failed
        //	Manage polling on synchronous calls
        long t0 = System.currentTimeMillis();
        while (!stop_it) {
            long t = System.currentTimeMillis();
            if (!host.onEvents) {
                manageSynchronousAttributes();
            } else {
                if ((t - t0) > 60000) {
                    //	Every minute, check in synchronous
                    //	event could have been lost.
                    manageSynchronousAttributes();
                    t0 = t;
                }
            }
            wait_next_loop(t);
            //if (host.getName().equals("orion"))
            //	System.out.println(host.onEvents);
        }
    }
    //======================================================================
    /**
     * Compute time to sleep before next loop, and sleep it.
     *
     * @param t0 time (in ms) when loop have started.
     */
    //======================================================================
    public synchronized void wait_next_loop(long t0) {
        try {
            long t1 = System.currentTimeMillis();
            long time_to_sleep = readInfoPeriod - (t1 - t0);

            if (time_to_sleep <= 0)
                time_to_sleep = 100;
            wait(time_to_sleep);
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        }
    }
    //======================================================================
    /**
     * Update host window if state has changed
     */
    //======================================================================
    private DevState previous_state = DevState.UNKNOWN;

    public void updateHost(final DevState state) {
        if (state == previous_state)
            return;
        previous_state = state;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

               //	If state has changed, then update host object
               if (state==DevState.ON)
                   host.state = all_ok;
               else if (state==DevState.MOVING)
                   host.state = moving;
               else if (state==DevState.ALARM)
                   host.state = alarm;
               else if (state==DevState.OFF)
                   host.state = all_off;
               else if (state==DevState.FAULT)
                   host.state = faulty;
               else
                   host.state = unknown;

               if (parent!=null)
                   parent.updateState();

               //System.out.println(host.get_name() + " is " + ApiUtil.stateName(state));

               if (host.info_dialog!=null)
                   host.info_dialog.updateHostState();
           }
       });
    }

    //======================================================================
    //======================================================================
    public void updateNotifdHost(final DevState notifd_state) {
        //	Convert to int
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int notifyd_state = unknown;
                if (notifd_state == DevState.ON) notifyd_state = all_ok;
                else if (notifd_state == DevState.FAULT) notifyd_state = faulty;

                if (host.notifydState == notifyd_state)
                    return;

                host.notifydState = notifyd_state;
                if (parent != null)
                    parent.updateState();
                else {
                    host.notifydState = notifyd_state;
                }
                if (host.info_dialog != null)
                    host.info_dialog.updateHostState();
            }
        });
    }

    //======================================================================
    //======================================================================
    private int timeout = -1;
    public void manageSynchronousAttributes() {
        DevState hostState;
        DevState notifdState;

        try {
            if (timeout<0) {
                timeout = host.get_timeout_millis();
            }
            host.set_timeout_millis(1000);
            DeviceAttribute[] att = host.read_attribute(attributes);

            if (att[StateAtt].hasFailed())
                hostState = DevState.FAULT;
            else
                hostState = att[StateAtt].extractState();

            if (att[NotifdAtt].hasFailed())
                notifdState = DevState.UNKNOWN;
            else
                notifdState = att[NotifdAtt].extractState();
        } catch (DevFailed e) {
            //Except.print_exception(e);
            host.except = e;
            notifdState = DevState.UNKNOWN;
            hostState = DevState.FAULT;
        }
        try { host.set_timeout_millis(timeout); } catch (DevFailed e) { /* */ }
        updateHost(hostState);

        if (host.manageNotifd)
            updateNotifdHost(notifdState);
    }


    //=========================================================================
    //
    //	Events management part
    //
    //=========================================================================
    private static String[] filters = new String[0];
    private StateEventListener state_listener = null;
    //======================================================================
    /**
     * Subscribe on State events
     */
    //======================================================================
    public void subscribeChangeStateEvent() {
        String stringError = null;
        try {

            if (host.supplier == null)
                host.supplier = new TangoEventsAdapter(host);

            //	if not already well done, add listener for state_event
            if (state_listener == null) {
                state_listener = new StateEventListener();
                host.supplier.addTangoChangeListener(
                        state_listener, attributes[StateAtt], filters);
            }
        } catch (DevFailed e) {
            state_listener = null;
            host.onEvents = false;

            //System.err.println(host.name());
            //	Display exception
            if (!e.errors[0].desc.startsWith("Already connected to event"))
                stringError = "subscribeChangeStateEvent() for " +
                        host.get_name() + " FAILED !\n" + e.errors[0].desc;
            //fr.esrf.TangoDs.Except.print_exception(e);
			//System.err.println(host.get_name() + ":	"+e.errors[0].desc);
        } catch (Exception e) {
            state_listener = null;
            host.onEvents = false;
            //	Display exception
            stringError = "subscribeChangeStateEvent() for " +
                    host.get_name() + " FAILED !" + e.toString();
            e.printStackTrace();
        }
        
		//if (stringError != null)
        //    System.err.println(stringError);
        if (parent!=null)
            parent.updateMonitor(stringError);
    }
    //=========================================================================
    /**
     * Change State events listener
     */
    //=========================================================================
    class StateEventListener implements ITangoChangeListener {
        //=====================================================================
        //=====================================================================
        public void change(TangoChangeEvent event) {

            try {
                if (event.isZmqEvent())
                    host.eventSource = "(ZMQ)";
                else
                    host.eventSource = "(notifd)";
            }
            catch (NoSuchMethodError e) {
                //  Cannot be ZMQ (too old)
                host.eventSource = "(notifd)";
            }
            //long	t0 = System.currentTimeMillis();
            TangoChange tc = (TangoChange) event.getSource();
            String deviceName = tc.getEventSupplier().get_name();
            DevState hostState;
            DevState notifdState;

            int timeout = -1;
            try {
                //	Get the host state from attribute value
                DeviceAttribute attr = event.getValue();
                if (attr.hasFailed())
                    hostState = DevState.UNKNOWN;
                else
                    hostState = attr.extractState();

            } catch (DevFailed e) {
                System.err.println(new Date());
                System.err.println(host.name() + "  has received a DevFailed :	" + e.errors[0].desc);
                hostState = DevState.ALARM;
                System.err.println("HostStateThread.StateEventListener on " + deviceName);
                try {
                    timeout = host.get_timeout_millis();
                    host.set_timeout_millis(500);
                    host.ping();
                } catch (DevFailed e2) {
                    hostState = DevState.FAULT;
                }
            } catch (Exception e) {
                System.out.println("AstorEvent." + deviceName);
                System.out.println(e.toString());
                System.out.println("HostStateThread.StateEventListener : could not extract data!");
                hostState = DevState.UNKNOWN;
            }
            try {
                if (timeout>0)
                    host.set_timeout_millis(timeout);
            }catch (DevFailed e) {
                System.err.println(e.errors[0].desc);
            }

            if (host.manageNotifd) {
                try {
                    //	Check if notify daemon running in synchronous
                    DeviceAttribute att_synch = host.read_attribute(attributes[NotifdAtt]);
                    if (att_synch.hasFailed())
                        notifdState = DevState.UNKNOWN;
                    else
                        notifdState = att_synch.extractState();
                } catch (Exception e) {
                    notifdState = DevState.UNKNOWN;
                }
                updateNotifdHost(notifdState);
            }
            updateHost(hostState);
        }
    }
}








