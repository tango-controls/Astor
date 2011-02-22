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
// Revision 3.10  2009/04/17 19:12:51  pascal_verdier
// Best display on DB server info.
//
// Revision 3.9  2009/01/30 09:31:50  pascal_verdier
// Black box management added for database.
// Black box management tool improved.
// Find TANGO object by filter added.
//
// Revision 3.8  2008/03/27 08:07:15  pascal_verdier
// Compatibility with Starter 4.0 and after only !
// Better management of server list.
// Server state MOVING managed.
// Hard kill added on servers.
// New features on polling profiler.
//
// Revision 3.7  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.6  2005/08/30 08:05:25  pascal_verdier
// Management of two TANGO HOST added.
//
// Revision 3.5  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
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

import admin.astor.tools.BlackBoxTable;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevInfo;
import fr.esrf.TangoApi.*;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;

public class DbaseObject implements AstorDefs
{
	private AstorTree	parent;
	private String		tango_host;
	private DbaseState	state_thread;

		   int			state = unknown;
		   DevFailed	except = null;

	//======================================================
	//======================================================
	public DbaseObject(AstorTree parent, String tango_host)
	{
		//	initialize data members
		this.parent     = parent;
		this.tango_host = tango_host;

		//	Start update thread.
		state_thread = new DbaseState();
		state_thread.start();
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
	String	getCvsTag(DeviceProxy dev) throws DevFailed
	{
		String	tagName = null;
		DevInfo	info = dev.info();
		String  servinfo = info.doc_url;
		String  tag = "CVS Tag = ";
		int start = servinfo.indexOf(tag);
		if (start>0)
		{
			start += tag.length();
			int end = servinfo.indexOf('\n', start);
			if (end>start)
				tagName = servinfo.substring(start, end);
		}
		if (tagName==null)
			return"";
		else
			return "CVS Tag:   " + tagName + "\n";
	}
	//======================================================
	//======================================================
	String getServerInfo() throws DevFailed
	{
		String		devname = state_thread.db.get_name();
		DeviceProxy	dev = new DeviceProxy(devname);
		String		str = "TANGO_HOST:    " +tango_host + "\n\n";

		str += dev.get_info() + "\n\n";
		str += getCvsTag(dev);

		try {
			DeviceAttribute	att = dev.read_attribute("StoredProcedureRelease");
			str += "Stored Procedure: " + att.extractString();
		} catch(DevFailed e) {
			//	Attribute not found
		}

		str += "\n\n";

		return str;
	}
	//======================================================
	//======================================================
	String getInfo() throws DevFailed
	{
		Database	db = ApiUtil.get_db_obj(tango_host);
		return db.get_info();
	}
	//======================================================
	//======================================================
	private BlackBoxTable	blackbox = null;
	void blackbox(JFrame parent)
	{
		try
		{
			if (blackbox==null)
				blackbox = new BlackBoxTable(parent,
					state_thread.db.getDeviceName());
			blackbox.setVisible(true);
		}
		catch(DevFailed e)
		{
			ErrorPane.showErrorMessage(parent, null, e);
		}
	}
	//======================================================
	//======================================================
	public String toString()
	{
		return tango_host;
	}


	//======================================================
	//======================================================
	private class DbConnection extends Connection
	{
		private String	devname;
		private DbConnection(String host, String port) throws DevFailed
		{
			super(host, port, false);
			devname = get_name();
		}
		private String getDeviceName()
		{
			return devname;
		}
	}
	//======================================================
	/**
	 *	A thread class to control device.
	 */
	//======================================================
	private class  DbaseState extends Thread
	{
		private String	host;
		private String	port;
		private DbConnection	db = null;
		//===============================================================
		//===============================================================
		private DbaseState()
		{
			int	idx = tango_host.indexOf(":");
			host = tango_host.substring(0, idx);
			port = tango_host.substring(idx+1);
		}


		//===============================================================
		//===============================================================
		private synchronized void wait_next_loop()
		{
			try {
				wait(AstorDefs.PollPeriod);
			}
			catch(InterruptedException e) {}
		}
		//===============================================================
		//===============================================================
		//private synchronized void updateParent(int tmp_state, DevFailed tmp_except)
		private void updateParent(int tmp_state, DevFailed tmp_except)
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
			int				tmp_state;
			DevFailed		tmp_except;

				//	Try to ping database
				try
				{
					//	Build connection if not done
					//	Do not use ApiUtil.get_db_obj() to be sure
					//	on which database the connection is done
					if (db==null)
						db = new DbConnection(host, port);
					db.ping();

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
			//noinspection InfiniteLoopStatement
			while (true)
			{
				manageState();
				wait_next_loop();
			}
		}
	}
}
