//+===========================================================================
// $Source$
//
// Project:   Tango API
//
// Description:  Java source for device proxy connection
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.4  2003/11/25 15:56:46  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
// Revision 3.3  2003/11/05 10:34:57  pascal_verdier
// Main Panel screen centering.
// Starter multi path added.
// little bugs fixed.
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
//-===========================================================================
//         (c) - Software Engineering Group - ESRF
//============================================================================

package admin.astor;

import org.omg.CORBA.*;
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;

import java.util.*;

class DbaseObject implements AstorDefs
{
	private AstorTree	parent;
	private String		tango_host;
	private DbaseState	state_thread;

	       int			state = unknown;
	       DevFailed	except = null;

	//======================================================
	//======================================================
	public DbaseObject(AstorTree parent)
	{
		//	initialize data members
		this.parent = parent;
		int period = AstorUtil.getStarterReadPeriod()/2;
		tango_host = AstorUtil.getTangoHost();

		//	Start update thread.
		state_thread = new DbaseState(period);
	}
	//===============================================================
	//===============================================================
	void start()
	{
		//	Start update thread.
		state_thread.start();
	}
	//======================================================
	//======================================================
	public String toString()
	{
		return "Database (" + tango_host + ")";
	}


	//======================================================
	/**
	 *	A thread class to control device.
	 */
	//======================================================
	class  DbaseState extends Thread
	{
		private long		period;
		private Database	dbase = null;
		private	DeviceProxy	db_dev = null;
		//===============================================================
		//===============================================================
		public DbaseState(long  period)
		{
			this.period = period;
		}


		//===============================================================
		//===============================================================
		public synchronized void wait_next_loop()
		{
			try {
				wait(period);
			}
			catch(InterruptedException e) {}
		}
		//===============================================================
		//===============================================================
		private synchronized void updateParent(int tmp_state, DevFailed tmp_except)
		{
			if (state!=tmp_state || except!=tmp_except)
			{
				state  = tmp_state;
				except = tmp_except;
				parent.updateState();
			}
		}
		//===============================================================
		//===============================================================
		private void manageState()
		{
			int			tmp_state;
			DevFailed	tmp_except;
			
				//	Try to ping database
				try
				{
					//	Check if database server is running
					if (dbase==null)
						dbase = ApiUtil.get_db_obj(tango_host);
					dbase.ping();

					//	Check if database device is OK
					//if (db_dev==null)
					//	db_dev = new DeviceProxy(dbase.get_name());
					//db_dev.ping();

					tmp_state = all_ok;
					tmp_except = null;
				}
				catch (DevFailed e)
				{
					//	If exception catched, save it
					tmp_state = faulty;
					tmp_except = e;
				}
				updateParent(tmp_state, tmp_except);
		}
		//===============================================================
		//===============================================================
		public void run()
		{
			while (true)
			{
				manageState();
				wait_next_loop();
			}
		}
	}
}
