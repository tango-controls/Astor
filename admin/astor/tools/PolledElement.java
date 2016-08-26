//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
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
// $Revision$
//
//-======================================================================


package admin.astor.tools;

import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoDs.TangoConst;

import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 *	This class is able to defined a polled element
 *
 * @author verdier
 */
public class PolledElement {
    String deviceName = "unknown";
    String name = "unknown";
    String type;
    int period = -1;
    int buffer_depth = -1;
    double reading_time = -1;
    int last_update = -1;
    int[] realPeriods = new int[0];
    DevState state;
    String status;
    String last_update_str = "";
    private static final String equal = " = ";
    private static final String since = " since ";

    //===============================================================
    //===============================================================
    public PolledElement(String deviceName, String status) {
        this.deviceName = deviceName;
        this.status = status;
        parsePollingStatus(status);
        if (period < 0 || buffer_depth < 0 || reading_time < 0 || realPeriods.length == 0)
            state = DevState.FAULT;
        else if (realPeriods[0] > 1.5 * period || reading_time > period)
            state = DevState.FAULT;
        else
            state = DevState.ON;
    }

    //===============================================================
    //===============================================================
    private static final String externalyStr = "Polling externally triggered";
    boolean polled = true;

    private void parsePeriod(String line) {
        if (line.startsWith(externalyStr)) {
            polled = false;
            return;
        }
        int idx;
        if ((idx = line.indexOf(equal)) > 0) {
            String str = line.substring(idx + equal.length());
            try {
                period = Integer.parseInt(str);
            } catch (NumberFormatException e) { /* do nothing */ }
        }
    }

    //===============================================================
    //===============================================================
    private void parseBufferDepth(String line) {
        int idx;
        if ((idx = line.indexOf(equal)) > 0) {
            String str = line.substring(idx + equal.length());
            try {
                buffer_depth = Integer.parseInt(str);
            } catch (NumberFormatException e) { /* Do nothing */}
        }
    }

    //===============================================================
    //===============================================================
    private void parseName(String line) {
        int idx;
        if ((idx = line.indexOf(equal)) > 0)
            name = line.substring(idx + equal.length());
        if (line.indexOf(TangoConst.Tango_PollAttribute) > 0)
            type = TangoConst.Tango_PollAttribute;
        else
            type = TangoConst.Tango_PollCommand;
    }

    //===============================================================
    //===============================================================
    private void parseDuration(String line) {
        int idx;
        if ((idx = line.indexOf(equal)) > 0) {
            String str = line.substring(idx + equal.length());
            try {
                reading_time = Double.parseDouble(str);
            } catch (NumberFormatException e) { /* do nothing */}
        } else
            reading_time = 0;
    }

    //===============================================================
    //===============================================================
    private void parseRealPeriod(String line) {
        int idx;
        if ((idx = line.indexOf(equal)) > 0) {
            String str = line.substring(idx + equal.length());
            StringTokenizer stk = new StringTokenizer(str, ", ");
            List<String> tokens = new ArrayList<>();
            while (stk.hasMoreTokens())
                tokens.add(stk.nextToken());
            realPeriods = new int[tokens.size()];
            int i=0;
            for (String token : tokens) {
                try {
                    realPeriods[i++] = Integer.parseInt(token);
                } catch (NumberFormatException e) { /* Do nothing */}
            }
        }
    }

    //===============================================================
    //===============================================================
    private void parseLastUpdate(String line) {
        int idx;
        if ((idx = line.indexOf(since)) > 0) {
            String str = line.substring(idx + since.length());
            last_update_str = str;

            str = str.substring(0, str.indexOf("mS"));
            StringTokenizer stk = new StringTokenizer(str, " S and ");
            List<String> tokens = new ArrayList<>();
            while (stk.hasMoreTokens()) {
                tokens.add(stk.nextToken());
            }
            switch (tokens.size()) {
                case 1:
                    //	Get ms
                    try {
                        last_update = Integer.parseInt(tokens.get(0));
                    } catch (NumberFormatException e) {
                        last_update = 1000;
                    }
                    break;
                case 2:
                    //	Get seconds and ms
                    try {
                        last_update = Integer.parseInt(tokens.get(1));
                        last_update +=
                                1000 * Integer.parseInt(tokens.get(0));
                    } catch (NumberFormatException e) {
                        //	more
                        last_update += 60000;
                    }
                    break;
                default:
                    if (tokens.size() > 1) {
                        try {
                            last_update =
                                    60000 * Integer.parseInt(tokens.get(0));
                            last_update += Integer.parseInt(tokens.get(3));
                            last_update +=
                                    1000 * Integer.parseInt(tokens.get(2));
                        } catch (NumberFormatException e) {
                            //	more
                            last_update += 60000;
                        }
                    }
            }
        }
    }

    //===============================================================
    //===============================================================
    private String[] info;

    private void parsePollingStatus(String status) {
        List<String> lines = new ArrayList<>();
        StringTokenizer stk = new StringTokenizer(status, "\n");
        for (int i = 0; stk.hasMoreTokens(); i++) {
            String line = stk.nextToken();
            lines.add(line);
            switch (i) {
                case 0:
                    parseName(line);
                    break;

                case 1:
                    parsePeriod(line);
                    break;

                case 2:
                    parseBufferDepth(line);
                    break;

                case 3:
                    parseDuration(line);
                    break;

                case 4:
                    parseLastUpdate(line);
                    break;

                case 5:
                    parseRealPeriod(line);
                    break;
            }
        }
        info = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++)
            info[i] = lines.get(i);
    }

    //===============================================================
    //===============================================================
    public String toString() {
        return name;
    }

    //===============================================================
    //===============================================================
    public String info() {
        String str = type + " " + name +
                ":	" + period + " -> " + reading_time;
        str += "  (since " + last_update + ")	";
        for (int realPeriod : realPeriods)
            str += realPeriod + ", ";
        str += "	" + ApiUtil.stateName(state);
        return str;
    }

    //===============================================================
    //===============================================================
    public String[] getInfo() {
        List<String> lines = new ArrayList<>();

        if (polled) {
            lines.add(type + "  " + name);
            lines.add("Polling period = " + period + "ms");
            lines.add("Last record takes " + reading_time + " ms");

            if (info.length > 4) lines.add(info[4]);
            if (info.length > 5) lines.add(info[5]);

            lines.add("");
            String str = "Drifts (ms):   ";
            for (int realPeriod : realPeriods)
                str += "" + (realPeriod - period) + ", ";
            lines.add(str);
        } else {
            //	If triggered -> return status
            StringTokenizer stk = new StringTokenizer(status, "\n");
            while (stk.hasMoreTokens())
                lines.add(stk.nextToken());
        }
        return lines.toArray(new String[lines.size()]);
    }
}
