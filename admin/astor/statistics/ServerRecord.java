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
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.Except;

public class ServerRecord {
    DevState state;
    String stateName;
    long startTime = -1;
    long endTime = -1;
    long duration = 0;
    int autoRestart = START_UNKNOWN;

    public static final int START_UNKNOWN = 0;
    public static final int START_REQUEST = 1;
    public static final int START_AUTO = 2;

    //  Saving file definitions
    public static final String className = "ServerRecord";
    private static final String stateStr = "state";
    private static final String startTimeStr = "startTime";
    private static final String endTimeStr = "endTime";
    private static final String durationStr = "duration";
    private static final String autoStartStr = "started";
    private static final String description =
            "<" + className + " " +
                    stateStr + "=\"STATE\" " +
                    startTimeStr + "=\"START_TIME\" " +
                    endTimeStr + "=\"END_TIME\" " +
                    durationStr + "=\"DURATION\"" +
                    autoStartStr + "=\"STARTED\"" +
                    ">";
    private static final String tab = "\t\t\t\t";

    //===============================================================
    //===============================================================
    public ServerRecord(String line) throws DevFailed {
        stateName = Utils.parseXmlProperty(line, stateStr);
        if (stateName.equals("Start"))
            state = DevState.ON;
        else
            state = DevState.FAULT;
        try {
            startTime = Long.parseLong(Utils.parseXmlProperty(line, startTimeStr));
            endTime = Long.parseLong(Utils.parseXmlProperty(line, endTimeStr));
            duration = endTime - startTime;
        } catch (NumberFormatException e) {
            Except.throw_exception("SYNTAX_ERROR", e.toString(), "ServerRecord.ServerRecord()");
        }
        String str = Utils.parseXmlProperty(line, autoStartStr);
        switch (str) {
            case "auto":
                autoRestart = START_AUTO;
                break;
            case "request":
                autoRestart = START_REQUEST;
                break;
            default:
                autoRestart = START_UNKNOWN;
                break;
        }
    }

    //===============================================================
    //===============================================================
    public ServerRecord(DevState state, long startTime, long endTime, int autoRestart) {
        this.state = state;
        if (state == DevState.ON)
            stateName = "Start";
        else
            stateName = "Failed";

        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = endTime - startTime;
        this.autoRestart = autoRestart;
    }

    //===============================================================
    //===============================================================
    public String toXml() {
        String str = description;
        str = Utils.strReplace(str, "STATE", stateName);
        str = Utils.strReplace(str, "START_TIME", Long.toString(startTime));
        str = Utils.strReplace(str, "END_TIME", Long.toString(endTime));
        str = Utils.strReplace(str, "DURATION", Long.toString(duration));
        if (state == DevState.ON) {
            if (autoRestart == ServerRecord.START_AUTO)
                str = Utils.strReplace(str, "STARTED", "auto");
            else if (autoRestart == ServerRecord.START_REQUEST)
                str = Utils.strReplace(str, "STARTED", "request");
            else
                str = Utils.strReplace(str, "STARTED", "");// Unknown
        } else
            str = Utils.strReplace(str, "STARTED", "");
        return tab + str;
    }

    //===============================================================
    //===============================================================
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (state == DevState.ON)
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
