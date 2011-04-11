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

import fr.esrf.Tango.DevState;

import java.util.StringTokenizer;



public class  LogRecord
{
	String		name;
	DevState	newState;
	long		startedTime    = -1;
	long		failedTime     = -1;
	long		failedDuration = 0;
	long		runDuration    = 0;
    boolean     autoRestart    = false;
	//===============================================================
	//===============================================================
	public LogRecord(String line)
	{
		StringTokenizer	stk = new StringTokenizer(line, "\t");
		name  = stk.nextToken();
		newState     = getState(stk.nextToken());
		startedTime  = getValue(stk.nextToken()) * 1000;
		failedTime   = getValue(stk.nextToken()) * 1000;
		if (failedTime>0)
			if (startedTime>failedTime)
				failedDuration = startedTime - failedTime;
			else
				runDuration = failedTime - startedTime;

        if (stk.hasMoreTokens()) {
            autoRestart = stk.nextToken().equals("true");
        }
	}
	//===============================================================
	//===============================================================
	private long getValue(String str)
	{
		long	val = -1;
		try {
			val = Long.parseLong(str);
		}
		catch (NumberFormatException e) {
			System.err.println(e);
		}
		return val;
	}
	//===============================================================
	//===============================================================
	private DevState getState(String str)
	{
		if (str.equals("ON"))
			return DevState.ON;
		else
			return DevState.FAULT;
	}
	//===============================================================
	//===============================================================
	public String toString()
	{
		StringBuffer	sb = new StringBuffer();
		sb.append(name).append(":\t");
		if (newState==DevState.FAULT) {
			sb.append("Failed at   ").append(Utils.formatDate(failedTime));
		}
		else {
			sb.append("Start  at   ").append(Utils.formatDate(startedTime));
		}

		if (failedDuration>0) {
			sb.append("\tFailure ").append(Utils.formatDuration(failedDuration));
		}
		if (runDuration>0) {
			sb.append("\tAvailable ").append(Utils.formatDuration(runDuration));
		}

		return sb.toString();
	}
	//===============================================================
	//===============================================================
}
