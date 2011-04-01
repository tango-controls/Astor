//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for the StarterStat class definition .
//
// $Author: verdier $
//
// $Revision: $
//
// $Log: StarterStat.java,v $
//
// Copyleft 2010 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor.statistics;


/** 
 *	This class is able to
 *
 * @author  verdier
 */

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.Except;


public class  ServerRecord
{
	DevState	state;
    String      stateName;
	long		startTime    = -1;
	long		endTime      = -1;
	long		duration     = 0;
    public static final String className = "ServerRecord";
    private static final String stateStr     = "state";
    private static final String startTimeStr = "startTime";
    private static final String endTimeStr   = "endTime";
    private static final String durationStr  = "duration";
     private static final String description =
            "<" + className   + " " +
                    stateStr     + "=\"STATE\" " +
                    startTimeStr + "=\"START_TIME\" "+
                    endTimeStr   + "=\"END_TIME\" " +
                    durationStr  + "=\"DURATION\"" +
                    ">";
    private static final String tab = "\t\t\t\t";
	//===============================================================
	//===============================================================
	public ServerRecord(String line) throws DevFailed
	{
        stateName = Utils.parseXmlProperty(line, stateStr);
        if (stateName.equals("Start"))
            state = DevState.ON;
        else
            state = DevState.FAULT;
        try {
            startTime = Long.parseLong(Utils.parseXmlProperty(line, startTimeStr));
            endTime   = Long.parseLong(Utils.parseXmlProperty(line, endTimeStr));
        duration       = endTime - startTime;
        }
        catch (NumberFormatException e ) {
            Except.throw_exception("SYNTAX_ERROR", e.toString(), "ServerRecord.ServerRecord()");
        }
	}
	//===============================================================
	//===============================================================
	public ServerRecord(DevState state, long startTime, long endTime)
	{
        this.state = state;
        if (state==DevState.ON)
            stateName = "Start";
        else
            stateName = "Failed";

        this.startTime = startTime;
        this.endTime   = endTime;
        duration       = endTime - startTime;
	}
	//===============================================================
	//===============================================================
    public String toXml()
    {
        String  str = description;
        str = Utils.strReplace(str, "STATE", stateName);
        str = Utils.strReplace(str, "START_TIME", Long.toString(startTime));
        str = Utils.strReplace(str, "END_TIME", Long.toString(endTime));
        str = Utils.strReplace(str, "DURATION", Long.toString(duration));
        return tab + str;
    }
	//===============================================================
	//===============================================================
	public String toString()
	{
		StringBuffer	sb = new StringBuffer();
		if (state==DevState.ON)
            sb.append("Start");
        else
            sb.append("Failed");
        sb.append("\t").append(Utils.formatDate(startTime)).append("\t")
                .append(Utils.formatDate(endTime)).append("\t").
                append(Utils.formatDuration(duration));

		return sb.toString();
	}
	//===============================================================
	//===============================================================
}
