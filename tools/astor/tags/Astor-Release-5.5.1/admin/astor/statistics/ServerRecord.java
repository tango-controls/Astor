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
    int         autoRestart  = START_UNKNOWN;

    public static final int START_UNKNOWN = 0;
    public static final int START_REQUEST = 1;
    public static final int START_AUTO    = 2;

    //  Saving file definitions
    public static final String className = "ServerRecord";
    private static final String stateStr     = "state";
    private static final String startTimeStr = "startTime";
    private static final String endTimeStr   = "endTime";
    private static final String durationStr  = "duration";
    private static final String autoStartStr = "started";
    private static final String description =
            "<" + className   + " " +
                    stateStr     + "=\"STATE\" " +
                    startTimeStr + "=\"START_TIME\" "+
                    endTimeStr   + "=\"END_TIME\" " +
                    durationStr  + "=\"DURATION\"" +
                    autoStartStr + "=\"STARTED\"" +
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
            duration  = endTime - startTime;
        }
        catch (NumberFormatException e ) {
            Except.throw_exception("SYNTAX_ERROR", e.toString(), "ServerRecord.ServerRecord()");
        }
        String  str = Utils.parseXmlProperty(line, autoStartStr);
        if (str.equals("auto"))
            autoRestart = START_AUTO;
        else
        if (str.equals("request"))
            autoRestart = START_REQUEST;
        else
            autoRestart = START_UNKNOWN;
	}
	//===============================================================
	//===============================================================
	public ServerRecord(DevState state, long startTime, long endTime, int autoRestart)
	{
        this.state = state;
        if (state==DevState.ON)
            stateName = "Start";
        else
            stateName = "Failed";

        this.startTime   = startTime;
        this.endTime     = endTime;
        this.duration    = endTime - startTime;
        this.autoRestart = autoRestart;
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
        if (state==DevState.ON) {
            if (autoRestart==ServerRecord.START_AUTO)
                str = Utils.strReplace(str, "STARTED", "auto");
            else
            if (autoRestart==ServerRecord.START_REQUEST)
                str = Utils.strReplace(str, "STARTED", "request");
            else
                str = Utils.strReplace(str, "STARTED", "");// Unknown
        }
        else
            str = Utils.strReplace(str, "STARTED", "");
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
