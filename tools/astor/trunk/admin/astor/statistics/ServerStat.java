//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for the ServerStat class definition .
//
// $Author: verdier $
//
// $Revision: $
//
// $Log: ServerStat.java,v $
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

import java.util.Vector;



public class  ServerStat extends Vector<ServerRecord>
{
	public String	    name;
    public StarterStat  starterStat;
	public int	nbFailures     = 0;
	public long	failedDuration = 0;
	public long	runDuration    = 0;
    public long oldestTime     = 0;
    private Vector<LogRecord>   logs = new Vector<LogRecord>();
    public  static final String className = "ServerStat";
    private static final String nameStr   = "server";
    private static final String nbFailStr = "nbFailures";
    private static final String tFailStr  = "failedDuration";
    private static final String tRunStr   = "runDuration";
    private static final String resetStr  = "reset";
    private static final String description =
            "<" + className   + " " +
                    nameStr   + "=\"SERVER\" " +
                    nbFailStr + "=\"NB_FAILURES\" "+
                    tFailStr  + "=\"FAILED_DURATION\" " +
                    tRunStr   + "=\"RUN_DURATION\"" +
                    resetStr  + "=\"RESET\"" +
                    ">";
    private static final String tab = "\t\t\t";


	//===============================================================
    /**
     *  Statistics for the specified server 
     * @param lines  xml lines
     * @throws DevFailed if parsing xml lines failed.
     */
	//===============================================================
	public ServerStat(Vector<String> lines) throws DevFailed
    {
        parseXmlStatistics(lines);
    }
    //=======================================================
    //=======================================================
    private void parseXmlStatistics(Vector<String> lines) throws DevFailed
    {
        //  The first line is the Starter definition
        if (lines.size()>=0) {
            parseXmlProperties(lines.get(0));
            for (int i=1 ; i<lines.size() ; i++) {
                add(new ServerRecord(lines.get(i)));
            }
        }
    }
    //=======================================================
    //=======================================================
    private void parseXmlProperties(String line) throws DevFailed
    {
        name = Utils.parseXmlProperty(line, nameStr);
        try {
            nbFailures     = Integer.parseInt(Utils.parseXmlProperty(line, nbFailStr));
            failedDuration = Long.parseLong(Utils.parseXmlProperty(line, tFailStr));
            runDuration    = Long.parseLong(Utils.parseXmlProperty(line, tRunStr));
            oldestTime     = Long.parseLong(Utils.parseXmlProperty(line, resetStr));
        }
        catch (NumberFormatException e ) {
            Except.throw_exception("SYNTAX_ERROR", e.toString(), "ServerStat.parseLine()");
        }
    }
	//===============================================================
    /**
     *  Statistics for the specified server
     * @param name  server name
     */
	//===============================================================
	public ServerStat(String name)
	{
		this.name = name;
    }
	//===============================================================
    /**
     *  Statistics for the specified server
     * @param name  server name
     * @param starterStat   start where server is registered statistics
     */
	//===============================================================
	public ServerStat(String name, StarterStat  starterStat)
	{
		this.name = name;
        this.starterStat = starterStat;
	}
	//===============================================================
	//===============================================================
    public void addLog(LogRecord log)
    {
        logs.add(log);
    }
	//===============================================================
	//===============================================================
    public void computeStatistics()
    {
        nbFailures     = 0;
        failedDuration = 0;
        runDuration    = 0;
        oldestTime     = System.currentTimeMillis();

        //  For each Log record
        Vector<ServerRecord>    serverRecords = new Vector<ServerRecord>();
        for (int i=logs.size()-1 ; i>=0 ; i--) {
            LogRecord   log = logs.get(i);
            DevState    state = log.newState;
            long    t0;
            long    t1 = 0;
            int     idx;

            if (state==DevState.FAULT) {
                //  Check failure time and duration
                nbFailures++;
                t0 = log.failedTime;
                if (i==0)   //  Last record (until now)
                    t1 = System.currentTimeMillis();
                else {
                    idx = getNextRestartIndex(i);
                    if (idx>=0) {    //   Found
                        t1 = logs.get(idx).startedTime;
                        i = idx+1;
                    }
                    else {
                        idx = getNextFailureIndex(i);
                        if (idx>=0) {    //   Found
                            t1 = logs.get(idx).failedTime;
                            i = idx+1;
                        }
                    }
                }
                failedDuration += t1 - t0;
            }
            else {
                //  Check running time and duration
                t0 = log.startedTime;
                if (i==0)   //  Last record (until now)
                    t1 = System.currentTimeMillis();
                else {
                    idx = getNextFailureIndex(i);
                    if (idx>=0) {    //   Found
                        t1 = logs.get(idx).failedTime;
                        i = idx+1;
                    }
                    else {
                        idx = getNextRestartIndex(i);
                        if (idx>=0) {    //   Found
                            t1 = logs.get(idx).startedTime;
                            i = idx+1;
                        }
                    }
                }
                runDuration += t1-t0;
            }

            ServerRecord    serverRecord = new ServerRecord(log.newState, t0, t1);
            serverRecords.add(serverRecord);

            if (oldestTime>t0)
                oldestTime = t0; 
        }

        //  Reverse order to have last record first
        for (ServerRecord rec : serverRecords)
            insertElementAt(rec, 0);
    }
	//===============================================================
	//===============================================================
    private int getNextRestartIndex(int i)
    {
        i--;
        for ( ; i>=0 ; i--) {
            LogRecord  rec = logs.get(i);
            if (rec.newState==DevState.ON)
                return i;
        }
        return -1;
    }
	//===============================================================
	//===============================================================
    private int getNextFailureIndex(int i)
    {
        i--;
        for ( ; i>=0 ; i--) {
            LogRecord  rec = logs.get(i);
            if (rec.newState==DevState.FAULT)
                return i;
        }
        return -1;
    }
	//===============================================================
	//===============================================================
	public String recordsToString()
	{
		StringBuffer	sb = new StringBuffer();
		sb.append(name).append(":\n");
        for (ServerRecord rec : this) {
            sb.append(rec).append("\n");
        }
		return sb.toString();
	}
	//===============================================================
    /**
     * @return the server availability ina double value (e.g.: 0.98)
     */
	//===============================================================
    public double getAvailability()
    {
        return  ((double)runDuration/(runDuration+failedDuration));
    }
	//===============================================================
	//===============================================================
    public long getLastFailure()
    {
        for (ServerRecord rec : this) {
            if (rec.state==DevState.FAULT && rec.startTime>0)
                return rec.startTime;
        }
        return 0;
    }
    //=======================================================
    //=======================================================
    private String toXmlLine()
    {
        String  str = description;
        str = Utils.strReplace(str, "SERVER", name);
        str = Utils.strReplace(str, "NB_FAILURES", Integer.toString(nbFailures));
        str = Utils.strReplace(str, "FAILED_DURATION", Long.toString(failedDuration));
        str = Utils.strReplace(str, "RUN_DURATION", Long.toString(runDuration));
        str = Utils.strReplace(str, "RESET", Long.toString(oldestTime));
        return str;
    }
    //=======================================================
    //=======================================================
    public String toXml()
    {
        StringBuffer    sb = new StringBuffer();
        sb.append(tab).append("").append(toXmlLine()).append(">\n");
        for (ServerRecord   record : this)
            sb.append(record.toXml()).append("\n");
        sb.append(tab).append("</"+className+">");
        return sb.toString();
    }
	//===============================================================
	//===============================================================
	public String toString()
	{
		StringBuffer	sb = new StringBuffer();
		sb.append(name).append(":\thas run ").append(Utils.formatDuration(runDuration));
		if (nbFailures>0)
			sb.append("   has failed ").append(nbFailures).append(" times  (total time: ").
				append(Utils.formatDuration(failedDuration)).append(") - ")
                    .append(size()).append(" records");
		
		return sb.toString();
	}
	//===============================================================
	//===============================================================
}
