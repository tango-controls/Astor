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
        for (int i=0 ; i<devnames.length ; i++)
        {
            dev = new DeviceProxy(devnames[i]).get_adm_dev();
            readData(devnames[i], full_name);
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
		String[]  s = argout.extractStringArray();
		for (int i=0 ; i<s.length ; i++)
        {
            PolledElement   pe = new PolledElement(s[i]);
            //  Check if already exists (special case for state and status att/cmd)
            boolean found = false;
            for (int j=0 ; !found && j<size() ; j++)
            {
                if (full_name)
                    found =(polledElementAt(j).name.toLowerCase().equals(devname + "/" + pe.name.toLowerCase()));
                else
                    found =(polledElementAt(j).name.toLowerCase().equals(pe.name.toLowerCase()));
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
		DevPollStatus	client = null;

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
				for(int j=0 ; j<info.length ; j++)
					System.out.println(info[j]);
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
