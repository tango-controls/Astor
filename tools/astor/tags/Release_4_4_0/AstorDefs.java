//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the Astor class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.13  2006/04/19 12:07:21  pascal_verdier
// Host info dialog modified to use icons to display server states.
//
// Revision 3.12  2005/12/01 10:00:23  pascal_verdier
// Change TANGO_HOST added (needs TangORB-4.7.7 or later).
//
// Revision 3.11  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.10  2005/08/30 08:05:25  pascal_verdier
// Management of two TANGO HOST added.
//
// Revision 3.9  2005/03/11 14:07:53  pascal_verdier
// Pathes have been modified.
//
// Revision 3.8  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.7  2004/07/08 11:22:58  pascal_verdier
// First revision able to use events.
//
// Revision 3.6  2004/05/04 07:05:27  pascal_verdier
// Bug on notify daemon fixed.
// server reconection transparency added.
//
// Revision 3.5  2004/04/13 12:17:28  pascal_verdier
// DeviceTree class uses the new browsing database commands.
//
// Revision 3.4  2004/02/04 14:37:42  pascal_verdier
// Starter logging added
// Database info added on CtrlServersDialog.
//
// Revision 3.3  2003/11/25 15:56:45  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
// Revision 3.2  2003/09/08 12:21:36  pascal_verdier
// *** empty log message ***
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
// Revision 3.0  2003/06/04 12:37:52  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.1  2003/06/04 12:33:11  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.0  2003/01/16 15:22:35  verdier
// Last ci before CVS usage
//
// Revision 1.6  2002/09/13 08:43:07  verdier
// Use IDL 2 Starter version (polling thread, State from Starter, ...).
// Host info window not modal.
// Host info window resizable for display all servers option.
// And many features.
//
// Revision 1.5  2001/05/30 15:13:29  verdier
// Start/Stop host control added
// Jive statup aded
// and many app_util added...
//
// Revision 1.4  2001/01/09 14:58:33  verdier
// Start and stop all servers added.
// Progress Monitor added.
//
// Revision 1.3  2000/12/20 09:32:51  verdier
// Compatible with TangoApi package first revision.
//
// Revision 1.2  2000/10/12 08:50:52  verdier
// Hosts and servers are now controlled by threads.
// Commnds could be send to dserver/class server.
//
// Revision 1.1  2000/10/04 14:35:56  verdier
// Initial revision
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;
 


/** 
 *	Constant definition interface for Astor package.
 *
 * @author  verdier
 */


import java.awt.*;


public interface AstorDefs {

	public final String		notifyd_prg = "notify_daemon";

	public final String		starterDeviceHeader = "tango/admin/";
	public final String		img_path = "/app_util/img/";
	public final String		collec_property = "HostCollection";
	public final String		usage_property = "HostUsage";
	public final String[]	logging_properties = {
										"logging_level", 
										"logging_target",
										"logging_rft"
									};

	public final int		do_not_close = 0;
	public final int		do_close     = -1;
	static final int		COLLECTION = 0;
	static final int		LEAF       = 1;

	public final int		ALL_SERVERS     = 0;
	public final int		RUNNING_SERVERS = 1;
	public final int		StartAllServers = 0;
	public final int		StopAllServers  = 1;
	public final int		StartNewServer  = 2;
	public final String[]	cmdStr = {
							"Starting ",
							"Stopping "
							};

	public final String		DocLocationUnknown = "Doc location unknown....";
	public final String		DerfaultDocLocation =
		"http://www.esrf.fr/tango/tango_doc/index.html";

	public final int		PollPeriod  = 2000;
	//======================================================================
	//	States colors definitions
	//======================================================================
	public static final int unavailable = -1;
	public static final int unknown     =  0;
	public static final int faulty      =  1;
	public static final int alarm       =  2;
	public static final int all_ok      =  3;
	public static final int moving      =  4;
	public static final int failed      =  5;
	public static final int NbStates    =  6;
	public static Color[]	bg = {
			Color.gray,
			Color.red,   
			Color.orange,
			Color.green,
			Color.blue,
			Color.white
			};

	public static Color[]	fg = {	
			Color.white,
			Color.white,
			Color.black,
			Color.black,
			Color.white,
			Color.black
			};
	//======================================================================
	static final String	HtmlHeader = 
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

	static final String	HtmlFooter = "\n</Body>\n</Html>\n";
}
