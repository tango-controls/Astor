//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011
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


import java.awt.*;


public interface AstorDefs {

    public final String notifyd_prg = "notify_daemon";

    public final String starterDeviceHeader = "tango/admin/";
    public final String img_path = "/admin/astor/images/";
    public final String collec_property = "HostCollection";
    public final String usage_property = "HostUsage";
    public final String[] logging_properties = {
            "logging_level",
            "logging_target",
            "logging_rft"
    };

    public final int do_not_close = 0;
    public final int do_close = -1;
    static final int COLLECTION = 0;
    static final int LEAF = 1;

    public final int ALL_SERVERS = 0;
    public final int RUNNING_SERVERS = 1;
    public final int StartAllServers = 0;
    public final int StopAllServers = 1;
    public final int StartNewServer = 2;
    public final String[] cmdStr = {
            "Starting ",
            "Stopping "
    };

    public final int LEVEL_NOT_CTRL = 0;
    public final int NO_STATE = 0;
    public final int SERVER_STATES = 1;
    public final int STARTER_STATE = 2;
    public final int ALL_STATES = 3;

    public final String DocLocationUnknown = "Doc location unknown....";
    public final String DerfaultDocLocation =
            "http://www.esrf.fr/tango/tango_doc/index.html";

    public final int PollPeriod = 2000;
    //======================================================================
    //	States colors definitions
    //======================================================================
    public static final int unavailable = -1;
    public static final int unknown = 0;
    public static final int faulty = 1;
    public static final int alarm = 2;
    public static final int all_ok = 3;
    public static final int moving = 4;
    public static final int failed = 5;
    public static final int NbStates = 6;
    public static Color[] bg = {
            Color.gray,
            Color.red,
            Color.orange,
            Color.green,
            Color.blue,
            Color.white
    };

    public static Color[] fg = {
            Color.white,
            Color.white,
            Color.black,
            Color.black,
            Color.white,
            Color.black
    };
    //======================================================================
    static final String HtmlHeader =
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

    static final String HtmlFooter = "\n</Body>\n</Html>\n";
}
