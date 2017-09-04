//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author: verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// $Revision:  $
//
//-======================================================================


package admin.astor.statistics;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.Except;

import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * Compute global statistics for a multi StarterStat object
 *
 * @author Pascal Verdier
 */
public class GlobalStatistics {
    public long readAt = 0;
    public int nbHosts = 0;
    public int nbHostRead = 0;
    public int nbServers = 0;
    public int nbServersRead = 0;
    public long runDuration = 0;
    public long failedDuration = 0;
    public int nbFailures = 0;
    private List<StarterStat> starterStats;
    private String fileName = null;

    //  Saving file definitions
    private static final String className = "GlobalStat";
    private static final String dateStr = "date";
    private static final String timeStr = "time";
    private static final String failuresStr = "failures";
    private static final String description =
            "<" + className +
                    dateStr + "=\"DATE\" " +
                    timeStr + "=\"TIME\" " +
                    failuresStr + "=\"FAILURES\" " +
                    ">";

    public static final String system = "ctrlStatDsl:CtrlStatSystem";
    public static final String header = "<?xml version=\"1.0\" encoding=\"ASCII\"?>\n" +
            "<" + system + " xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ctrlStatDsl=\"http://www.esrf.fr/tango/starter/CtrlStatDslDsl\">";
    public static final String footer = "</" + system + ">";

    //=======================================================
    //=======================================================
    public GlobalStatistics(String fileName) throws DevFailed {
        this.fileName = fileName;
        starterStats = new ArrayList<>();
        String code = Utils.readFile(fileName);
        if (!code.startsWith(header))
            Except.throw_exception("FILE_NOT_VALID",
                    fileName + " is not a TANGO statistics file",
                    "GlobalStatistics.GlobalStatistics()");
        parseStatistics(code);
        computeStatistics();
    }

    //=======================================================
    //=======================================================
    public GlobalStatistics(List<StarterStat> starterStats) {
        this.starterStats = starterStats;
        readAt = System.currentTimeMillis();
        computeStatistics();
    }

    //=======================================================
    //=======================================================
    public String[][] getStarterResetDates() {
        String[][] dates = new String[starterStats.size()][];
        int i=0;
        for (StarterStat starterStat : starterStats) {
            String[]    array = new String[2];
            array[0] = starterStat.name;
            long t = starterStat.resetTime;
            if (t<0)
                array[1] = " ? ? ";
            else {
                array[1] = Utils.formatDate(t);
            }
            dates[i++] = array;
        }
        return dates;
    }
    //=======================================================
    //=======================================================
    public String getServersRunningTwice() {
        StringBuilder sb = new StringBuilder();
        for (StarterStat starterStat : starterStats) {
            if (starterStat.runningTwiceList.size()>0) {
                sb.append(starterStat.name).append(":\n");
                for (String runningTwice : starterStat.runningTwiceList) {
                    sb.append("  ").append(runningTwice).append('\n');
                }
            }
        }
        return sb.toString();
    }
    //=======================================================
    //=======================================================
    public List<StarterStat> getStarterStatistics() {
        return starterStats;
    }

    //=======================================================
    //=======================================================
    private void computeStatistics() {
        nbHosts = starterStats.size();
        for (StarterStat starterStat : starterStats) {
            nbServers += starterStat.size();
            if (starterStat.readOK) {
                nbHostRead++;
                for (ServerStat serverStat : starterStat) {
                    nbServersRead++;
                    runDuration += serverStat.runDuration / 1000;
                    if (serverStat.nbFailures > 0) {
                        nbFailures += serverStat.nbFailures;
                        failedDuration += serverStat.failedDuration / 1000;
                    }
                }
            }
        }
        runDuration *= 1000;
        failedDuration *= 1000;
    }

    //=======================================================
    //=======================================================
    public long getDuration() {
        return readAt - getOldestTime();
    }

    //=======================================================
    //=======================================================
    private long getOldestTime() {
        long t = System.currentTimeMillis();
        for (StarterStat starterStat : starterStats) {
            if (starterStat.resetTime < t) {
                t = starterStat.resetTime;
                //System.out.println(starterStat.name + ":	" + Utils.formatDate(starterStat.resetTime));
            }
        }
        return t;
    }
    //=======================================================
    //=======================================================
    @SuppressWarnings("unused")
    public String getHostForOldestTime() {
        String hostName = "";
        long t = System.currentTimeMillis();
        for (StarterStat starterStat : starterStats) {
            if (starterStat.resetTime < t) {
                t = starterStat.resetTime;
                hostName = starterStat.name;
                //System.out.println(starterStat.name + ":	" + Utils.formatDate(starterStat.resetTime));
            }
        }
        return hostName + " (" + Utils.formatDate(t) + ")";
    }

    //=======================================================
    //=======================================================
    private void parseStatistics(String code) throws DevFailed {
        StringTokenizer stk = new StringTokenizer(code, "\n");
        List<String> lines = new ArrayList<>();
        boolean globalFound = false;

        //  Get global info and collect lines between begin/end for StarterStat
        while (stk.hasMoreTokens()) {
            String line = stk.nextToken().trim();
            //  Is it Global statistics beginning
            if (line.startsWith("<" + className)) {
                globalFound = true;
                parseXmlLine(line);
            } else { //    Is it the end of global statistics ?
                if (line.startsWith("</" + className)) {
                    globalFound = false;
                } else if (globalFound) {
                    lines.add(line);
                }
            }
        }

        //  Collect StarterStat lines, and create objects
        List<String> records = new ArrayList<>();
        for (String line : lines) {
            //  Is it the end of server stat ?
            if (line.startsWith("</" + StarterStat.className)) {
                StarterStat starterStat = new StarterStat(records);
                starterStats.add(starterStat);
                records.clear();    //  OK (has been used) can clear
            } else {
                records.add(line);
            }
        }
    }

    //=======================================================
    //=======================================================
    private void parseXmlLine(String line) throws DevFailed {
        try {
            readAt = Long.parseLong(Utils.parseXmlProperty(line, timeStr));
            //nbFailures = Integer.parseInt(Utils.parseXmlProperty(line, failuresStr));
        } catch (NumberFormatException e) {
            Except.throw_exception("SYNTAX_ERROR", e.toString(), "GlobalStatistics.parseLine()");
        }
        System.out.println("Written " + Utils.parseXmlProperty(line, dateStr));
        System.out.println("--> " + Utils.formatDate(readAt));
        System.out.println(nbFailures + " failures");
    }

    //=======================================================
    //=======================================================
    public void saveStatistics(String fileName) throws DevFailed {
        StringBuilder sb = new StringBuilder(header);
        sb.append("\n");

        sb.append("\t").append(toXml()).append("\n");
        for (StarterStat starterStat : starterStats)
            sb.append(starterStat.toXml()).append("\n");
        sb.append("\t</").append(className).append(">\n");

        sb.append(footer);
        Utils.writeFile(fileName, sb.toString());
    }

    //=======================================================
    //=======================================================
    private String toXml() {
        String str = description;
        str = Utils.strReplace(str, "DATE", Utils.formatDate(readAt));
        str = Utils.strReplace(str, "TIME", Long.toString(readAt));
        str = Utils.strReplace(str, "FAILURES", Integer.toString(nbFailures));
        return str;
    }

    //=======================================================
    //=======================================================
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (fileName == null)
            sb.append("Statistics from Starters\n");
        else
            sb.append("Statistics from file: ").append(fileName).append("\n");

        sb.append("Between   ").append(Utils.formatDate(getOldestTime()))
                .append("   and   ")
                .append(Utils.formatDate(readAt)).append(" on:\n  ");
        sb.append(nbHostRead).append("/").append(nbHosts).append("   Controlled hosts\n  ");
        //sb.append(nbServersRead).append("/").append(nbServers).append("  Controlled servers\n  ");
        sb.append(nbServersRead).append("  Controlled servers\n  ");
        if (nbFailures == 0)
            sb.append("Availability:    100 %");
        else {
            double availability = (double) runDuration / (runDuration + failedDuration);
            sb.append("Nb failures:   ").append(nbFailures).append("\n  ");
            sb.append("Availability:  ").append(Utils.formatPercentage(availability));
        }
        return sb.toString();
    }
    //=======================================================
    //=======================================================
}
