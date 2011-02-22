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
 
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoDs.TangoConst;

import java.util.StringTokenizer;
import java.util.Vector;



public class  PolledElement
{
	String	name = "unknown";
	String	type;
	int		period       = -1;
	int		buffer_depth = -1;
	double	reading_time = -1;
	int		last_update  = -1;
	int[]	real_periods = new int[0];
	DevState	state;
    String      status;
	private static final String	equal = " = ";
	private static final String	since = " since ";

	//===============================================================
	//===============================================================
	public PolledElement(String status)
	{
        this.status = status;
		parsePollingStatus(status);
		if (period<0 || buffer_depth<0 || reading_time<0 || real_periods.length==0)
			state = DevState.FAULT;
		else
		if (real_periods[0]> 1.5*period || reading_time>period)
			state = DevState.FAULT;
		else
			state = DevState.ON;
	}
	//===============================================================
	//===============================================================
    private String[]    info;
	private void parsePollingStatus(String status)
	{
        Vector  lines = new Vector();
		StringTokenizer	stk = new StringTokenizer(status, "\n");
		for (int i=0 ; stk.hasMoreTokens() ; i++)
		{
			String	line = stk.nextToken();
            lines.add(line);
			int		idx;
			switch(i)
			{
			case 0:
				if ((idx=line.indexOf(equal))>0)
					name = line.substring(idx+equal.length());
				if (line.indexOf(TangoConst.Tango_PollAttribute)>0)
					type = TangoConst.Tango_PollAttribute;
				else
					type = TangoConst.Tango_PollCommand;
				break;

			case 1:
				if ((idx=line.indexOf(equal))>0)
				{
					String	str = line.substring(idx+equal.length());
					try {
						period = Integer.parseInt(str);
					} catch(NumberFormatException e) { /* do nothing */ }
				}
				break;

			case 2:
				if ((idx=line.indexOf(equal))>0)
				{
					String	str = line.substring(idx+equal.length());
					try {
						buffer_depth = Integer.parseInt(str);
					} catch(NumberFormatException e) { /* Do nothing */}
				}
				break;

			case 3:
				if ((idx=line.indexOf(equal))>0)
				{
					String	str = line.substring(idx+equal.length());
					try {
						reading_time = Double.parseDouble(str);
					} catch(NumberFormatException e) { /* do nothing */}
				}
				break;

			case 4:
				if ((idx=line.indexOf(since))>0)
				{
					String	str = line.substring(idx+since.length());
					str = str.substring(0, str.indexOf("mS"));
					StringTokenizer	stk2 = new StringTokenizer(str, " S and ");
					Vector	v = new Vector();
					while(stk2.hasMoreTokens())
                    {
                        String  s = stk2.nextToken();
						v.add(s);
                    }
                    switch (v.size())
                    {
                    case 1:
                        //	Get ms
                        try {
                            last_update = Integer.parseInt((String)v.get(0));
                        } catch(NumberFormatException e) {
                            last_update = 1000;
                        }
                        break;
					case 2:
                        //	Get seconds and ms
						try {
							last_update = Integer.parseInt((String)v.get(1));
							last_update +=
								1000*Integer.parseInt((String)v.get(0));
						} catch(NumberFormatException e) {
							//	more
							last_update += 60000;
						}
                        break;
                    default:
                        if (v.size()>1)
                        {
                            try {
                                last_update =
                                    60000*Integer.parseInt((String)v.get(0));
                                last_update += Integer.parseInt((String)v.get(3));
                                last_update +=
                                    1000*Integer.parseInt((String)v.get(2));
                           } catch(NumberFormatException e) {
                                //	more
                                last_update += 60000;
                            }
                        }
                    }
				}
				break;

			case 5:
				if ((idx=line.indexOf(equal))>0)
				{
					String	str = line.substring(idx+equal.length());
					StringTokenizer	stk2 = new StringTokenizer(str, ", ");
					Vector	v = new Vector();
					while(stk2.hasMoreTokens())
						v.add(stk2.nextToken());
					real_periods = new int[v.size()];
					for (int j=0 ; j<v.size() ; j++)
						try {
							real_periods[j] =
								Integer.parseInt((String)v.get(j));
						} catch(NumberFormatException e) { /* Do nothing */}
				}
				break;
			}
		}
        info = new String[lines.size()];
        for (int i=0 ; i<lines.size() ; i++)
            info[i] = (String) lines.get(i);
	}
	
	//===============================================================
	//===============================================================
	public String toString()
	{
		return name;
	}
	//===============================================================
	//===============================================================
	public String info()
	{
		String	str =  type + " " + name +
							":	" + period + " -> " + reading_time;
		str += "  (since " + last_update + ")	";
		for (int i=0 ; i<real_periods.length ; i++)
			str += real_periods[i] + ", ";
		str += "	" + ApiUtil.stateName(state);
		return str;
	}
	//===============================================================
	//===============================================================
    //===============================================================
    //===============================================================
    public String[] getInfo()
    {
        String[]    retStr = new String[5];
        int i=0;
        retStr[i++] = type + "  " + name;
        retStr[i++] = "Polling period = " + period + "ms";
        retStr[i++] = "Last record takes " + reading_time + " ms";
        if (info.length>3)  retStr[i++] = info[4];
        if (info.length>4)  retStr[i] = info[5];
        return retStr;
    }
}
