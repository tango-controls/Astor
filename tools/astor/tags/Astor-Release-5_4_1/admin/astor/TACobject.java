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
// Revision 1.1  2011/01/10 12:49:02  pascal_verdier
// TAC is now displayed as database servers.
// StartServersAtStarteup starter class property management added.
// Display access mode in Tango Access panel.
//
// Revision 3.11  2009/06/02 15:19:05  pascal_verdier
//
// Copyleft 2011 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-===========================================================================
//         (c) - Software Engineering Group - ESRF
//============================================================================

package admin.astor;

import admin.astor.tools.BlackBoxTable;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevInfo;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;

public class TACobject extends  DeviceProxy implements AstorDefs
{
	private AstorTree	parent;
	private String		deviceName;
	private StateThread	state_thread;

		   int			state = unknown;
		   DevFailed	except = null;

	//======================================================
	//======================================================
	public TACobject(AstorTree parent, String deviceName) throws DevFailed
	{
        super(deviceName);
		//	initialize data members
		this.parent     = parent;
		this.deviceName = deviceName;

		//	Start update thread.
		state_thread = new StateThread();
		state_thread.start();
	}
    //======================================================
    //======================================================
    String	getCvsTag() throws DevFailed
    {
        String	tagName = null;
        DevInfo info = info();
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
        String		str = "Tango Access Control:\n\n";

        str += get_info() + "\n\n";
        str += getCvsTag();
        str += "\n\n";

        return str;
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
	void blackbox(JFrame parent)
	{
		try	{
			new BlackBoxTable(parent, deviceName).setVisible(true);
		}
		catch(DevFailed e) {
			ErrorPane.showErrorMessage(parent, null, e);
		}
	}
	//======================================================
	//======================================================
	public String toString()
	{
		return "Access Control";
	}

	//======================================================
	/**
	 *	A thread class to control device.
	 */
	//======================================================
	private class  StateThread extends Thread
	{
		//===============================================================
		//===============================================================
		private synchronized void wait_next_loop()
		{
			try {
				wait(AstorDefs.PollPeriod);
			}
			catch(InterruptedException e) { /* */ }
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
					ping();

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
