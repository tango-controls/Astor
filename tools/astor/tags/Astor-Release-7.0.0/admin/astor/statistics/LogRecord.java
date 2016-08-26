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


/**
 *	This class is able to manage a stater log
 *
 * @author verdier
 */

import fr.esrf.Tango.DevState;

import java.util.StringTokenizer;


public class LogRecord {
    String name;
    DevState newState;
    long startedTime = -1;
    long failedTime = -1;
    long failedDuration = 0;
    long runDuration = 0;
    int autoRestart = ServerRecord.START_UNKNOWN;

    //===============================================================
    //===============================================================
    public LogRecord(String line) {
        StringTokenizer stk = new StringTokenizer(line, "\t");
        if (stk.countTokens()<3) {
            if (stk.hasMoreTokens())
                System.err.println(stk.nextElement() + ": ");
            System.err.println("Log cannot be parsed ! ");
            return;
        }
        name = stk.nextToken();
        newState = getState(stk.nextToken());
        startedTime = getValue(stk.nextToken()) * 1000;
        if (stk.hasMoreTokens())
            failedTime = getValue(stk.nextToken()) * 1000;
        else
            failedTime = -1;
        if (failedTime > 0)
            if (startedTime > failedTime)
                failedDuration = startedTime - failedTime;
            else if (newState == DevState.ON)
                runDuration = failedTime - startedTime;

        if (newState == DevState.ON) {
            if (stk.hasMoreTokens()) {
                String str = stk.nextToken();
                if (str.equals("true"))
                    autoRestart = ServerRecord.START_AUTO;
                else
                    autoRestart = ServerRecord.START_REQUEST;
            }
            //  else unknown (for backward compatibility)
        }
        //  else unknown (not started)
    }

    //===============================================================
    //===============================================================
    private long getValue(String str) {
        long val = -1;
        try {
            val = Long.parseLong(str);
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
        }
        return val;
    }

    //===============================================================
    //===============================================================
    private DevState getState(String str) {
        if (str.equals("ON"))
            return DevState.ON;
        else
            return DevState.FAULT;
    }

    //===============================================================
    //===============================================================
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\t");
        if (newState == DevState.FAULT) {
            sb.append("Failed at   ").append(Utils.formatDate(failedTime));
        } else {
            sb.append("Start  at   ").append(Utils.formatDate(startedTime));
        }

        if (failedDuration > 0) {
            sb.append("\tFailure ").append(Utils.formatDuration(failedDuration));
        }
        if (runDuration > 0) {
            sb.append("\tAvailable ").append(Utils.formatDuration(runDuration));
        }

        return sb.toString();
    }
    //===============================================================
    //===============================================================
}
