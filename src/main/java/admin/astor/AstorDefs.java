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

package admin.astor;

/**
 *	Constant definition interface for Astor package.
 *
 * @author verdier
 */

public interface AstorDefs {

    String notifyd_prg = "notify_daemon";
    String collec_property = "HostCollection";
    String usage_property = "HostUsage";
    String  rcFileName = "astorrc";

    int READ_WRITE   = 0;
    int DB_READ_ONLY = 1;
    int READ_ONLY    = 2;
    String[] strMode = { "Read/Write mode", "Database is read only", "Read Only mode"};

    int COLLECTION = 0;
    int LEAF = 1;

    int StartAllServers = 0;
    int StopAllServers = 1;
    String[] cmdStr = {
            "Starting ",
            "Stopping "
    };

    int LEVEL_NOT_CTRL = 0;

    String DocLocationUnknown = "Doc location unknown....";

    int PollPeriod = 2000;
    //======================================================================
    //	States colors definitions
    //======================================================================
    int all_ok   = 0;
    int moving   = 1;
    int long_moving = 2;
    int alarm    = 3;
    int all_off  = 4;
    int faulty   = 5;
    int unknown  = 6;
    int failed   = 7;
    int NbStates = 8;

    String[] iconHelpForHosts = {
            "All controlled servers are running",
            "Starter is starting server(s)",
            "At least one server is blocked since a while",
            "At least one  controlled server is stopped",
            "All controlled servers are stopped",
            "Starter is not running on host",
            "Starter is may be running but the connection has failed",
            "State is not supported",
    };
    String[] iconHelpForServers = {
            "Server is running",
            "Server is running but not responding (starting ?)",
            null, null, null,
            "Server is not running"
    };
    //======================================================================
    String HtmlHeader =
            "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">\n" +
                    "<HTML>\n" +
                    "<BODY TEXT=\"#000000\" BGCOLOR=\"#FFFFFF\" LINK=\"#0000FF\" VLINK=\"#FF0000\" ALINK=\"#FF0000\">\n" +
                    "\n" +
                    "<table width=\"100%\" height=\"20%\"><tr>\n" +
                    "<td align=CENTER>\n" +
                    "<FONT COLOR=\"#0000FF\"><FONT SIZE=+4>E</FONT></FONT><FONT SIZE=+1>UROPEAN</FONT>\n" +
                    "<FONT COLOR=\"#0000FF\"><FONT SIZE=+4>S</FONT></FONT><FONT SIZE=+1>YNCHROTRON</FONT>\n" +
                    "<FONT COLOR=\"#0000FF\"><FONT SIZE=+4>R</FONT></FONT><FONT SIZE=+1>ADIATION</FONT>\n" +
                    "<FONT COLOR=\"#0000FF\"><FONT SIZE=+4>F</FONT></FONT><FONT SIZE=+1>ACILITY</FONT>\n" +
                    "</td><td>\n" +
                    "<IMG SRC=\"http://www.esrf.fr/gifs/logo/80.gif\">\n" +
                    "</td></tr></table>\n" +
                    "<P><Br>\n";

    String HtmlFooter = "\n</Body>\n</Html>\n";
}
