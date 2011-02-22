//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the DevPollStatus class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 1.3  2008/03/27 08:08:26  pascal_verdier
// Compatibility with Starter 4.0 and after only !
// Better management of server list.
// Server state MOVING managed.
// Hard kill added on servers.
// New features on polling profiler.
//
// Revision 1.2  2006/06/13 13:58:17  pascal_verdier
// Minor changes.
//
// Revision 1.1.1.1  2006/01/11 08:34:49  pascal_verdier
// Imported using TkCVS
//
//
// Copyleft 2006 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor.tools;


/** 
 *	This class is able to
 *
 * @author  verdier
 */
 
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;

import java.util.Vector;



public class  DevPollStatus extends Vector
{
	private DeviceProxy	dev;
    private static final boolean    FULL_NAME = true;
    private static final boolean    ATTR_NAME = false;
    //===============================================================
    //===============================================================
    public DevPollStatus(String devname) throws DevFailed
    {
        dev = new DeviceProxy(devname).get_adm_dev();
        readData(devname, ATTR_NAME);
    }
    //===============================================================
    //===============================================================
    public DevPollStatus(String[] devnames) throws DevFailed
    {
        //  full name (device and attribute) if more than one device
        boolean full_name = FULL_NAME;
        if (devnames.length<2)
            full_name = ATTR_NAME;
		for (String devname : devnames)
		{
			dev = new DeviceProxy(devname).get_adm_dev();
			readData(devname, full_name);
		}
    }
	//===============================================================
	//===============================================================
	private void readData(String devname, boolean full_name) throws DevFailed
	{
		//long	t0 = System.currentTimeMillis();
		DeviceData  argin = new DeviceData();
		argin.insert(devname);
		DeviceData	argout = dev.command_inout("DevPollStatus", argin);
		String[]  str = argout.extractStringArray();
		for (String s : str)
		{
			PolledElement pe = new PolledElement(s);
			//  Check if already exists (special case for state and status att/cmd)
			boolean found = false;
			for (int j = 0; !found && j < size(); j++)
			{
				if (full_name)
					found = (polledElementAt(j).name.toLowerCase().equals(devname + "/" + pe.name.toLowerCase()));
				else
					found = (polledElementAt(j).name.toLowerCase().equals(pe.name.toLowerCase()));
			}
			if (!found)
			{
				if (full_name)
					pe.name = devname + "/" + pe.name;
				add(pe);
			}
		}

		//long	t1 = System.currentTimeMillis();
		//System.out.println("elapsed time : " + (t1-t0) + " ms");

	}
	//===============================================================
	//===============================================================
	public PolledElement polledElementAt(int i)
	{
		return (PolledElement)get(i);
	}
	//===============================================================
	//===============================================================
	public int polledCount()
	{
		int	cnt = 0;
		for (int i=0 ; i<size() ; i++)
			if (polledElementAt(i).polled)
				cnt++;
		return cnt;
	}
	//===============================================================
	//===============================================================
	public int triggeredCount()
	{
		int	cnt = 0;
		for (int i=0 ; i<size() ; i++)
			if (!polledElementAt(i).polled)
				cnt++;
		return cnt;
	}
	//===============================================================
	//===============================================================
	static void displaySyntax()
	{
		System.out.println("device name ?");
		System.exit(1);
	}
	//===============================================================
	//===============================================================
	public static void main (String[] args)
	{
		String			devname = null;
		DevPollStatus	client;

		if (args.length>0)
			devname = args[0];
		else
			displaySyntax();

		try
		{
			client = new DevPollStatus(devname);
			for (int i=0 ; i<client.size() ; i++)
			{
				String[]	info = client.polledElementAt(i).getInfo();
				for (String anInfo : info)
					System.out.println(anInfo);
				System.out.println();
			}
		}
		catch(DevFailed e)
		{
			Except.print_exception(e);
			System.exit(1);
		}
		System.exit(0);
	}
}
