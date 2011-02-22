//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the Pogo class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.32  2010/06/17 08:48:11  pascal_verdier
// Pb on display startup level dialog in case of many devices fixed.
//
// Revision 3.31  2010/06/04 14:12:55  pascal_verdier
// Global command to change startup level added.
//
// Revision 3.30  2009/04/06 14:27:47  pascal_verdier
// Using MySqlUtil feature.
//
// Revision 3.29  2009/02/18 09:47:57  pascal_verdier
// Device dependencies (sub-devices) tool added.
//
// Revision 3.28  2009/01/30 09:31:50  pascal_verdier
// Black box management added for database.
// Black box management tool improved.
// Find TANGO object by filter added.
//
// Revision 3.27  2009/01/16 14:46:58  pascal_verdier
// Black box management added for host and Server.
// Starter logging display added for host and server.
// Splash screen use ATK one.
//
// Revision 3.26  2008/11/19 09:59:56  pascal_verdier
// New tests done on Access control.
// Pool Threads management added.
// Size added as preferences.
//
// Revision 3.25  2008/09/12 11:51:23  pascal_verdier
// Minor changes
//
// Revision 3.24  2008/05/29 13:14:55  pascal_verdier
// Bug on standard error index fixed
//
// Revision 3.23  2008/05/26 11:49:12  pascal_verdier
// Host info dialog servers are managed in a jtree.
//
// Revision 3.22  2008/03/27 08:07:15  pascal_verdier
// Compatibility with Starter 4.0 and after only !
// Better management of server list.
// Server state MOVING managed.
// Hard kill added on servers.
// New features on polling profiler.
//
// Revision 3.21  2008/03/03 14:55:21  pascal_verdier
// Starter Release_4 management.
//
// Revision 3.20  2007/09/11 09:23:29  pascal_verdier
// Db attribute polling panel added.
//
// Revision 3.19  2007/04/04 13:00:27  pascal_verdier
// Database attribute properties editor added.
//
// Revision 3.18  2007/01/08 08:21:07  pascal_verdier
// Disable Start Server button if Starter is MOVING.
//
// Revision 3.17  2006/04/19 12:08:12  pascal_verdier
// Host info dialog modified to use icons to display server states.
//
// Revision 3.16  2006/01/11 08:46:14  pascal_verdier
// PollingProfiler added.
//
// Revision 3.15  2005/11/24 12:24:57  pascal_verdier
// DevBrowser utility added.
// MkStarter utility added.
//
// Revision 3.14  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.13  2005/10/14 14:29:34  pascal_verdier
// Edit memorized attribute value added.
//
// Revision 3.12  2005/09/15 08:26:36  pascal_verdier
// Server architecture display addded.
//
// Revision 3.11  2005/09/13 14:28:01  pascal_verdier
// Wizard management added.
//
// Revision 3.10  2005/04/22 09:30:45  pascal_verdier
// Use events management in starter properies dialog added.
//
// Revision 3.9  2005/03/11 14:07:54  pascal_verdier
// Pathes have been modified.
//
// Revision 3.8  2005/01/18 08:48:19  pascal_verdier
// Tools menu added.
// Not controlled servers list added.
//
// Revision 3.7  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.6  2004/03/03 08:31:04  pascal_verdier
// The server restart command has been replaced by a stop and start command in a thread.
// The delete startup level info has been added.
//
// Revision 3.5  2004/02/04 14:37:43  pascal_verdier
// Starter logging added
// Database info added on CtrlServersDialog.
//
// Revision 3.4  2003/11/25 15:56:46  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
// Revision 3.3  2003/11/07 09:58:46  pascal_verdier
// Host info dialog automatic resize implemented.
//
// Revision 3.2  2003/09/08 11:05:28  pascal_verdier
// *** empty log message ***
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
// Revision 3.0  2003/06/04 12:37:53  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.1  2003/06/04 12:33:12  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;
 


/** 
 *	This class display a JPopupMenu.for server commands
 *
 * @author  verdier
 */


import admin.astor.tools.PollingProfiler;
import admin.astor.tools.DeviceHierarchyDialog;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;

//===============================================================
/**
 *	A class to display a popup menu.
 */
//===============================================================
@SuppressWarnings({"PointlessArithmeticExpression"})
public class ServerPopupMenu extends JPopupMenu implements AstorDefs {

	private TangoHost		host;
	private TangoServer		server;
	private LevelTree		tree;
	private JFrame			frame;
	private HostInfoDialog	parent;
	private int				mode;
	
	static final int	SERVERS = 0;
	static final int	LEVELS  = 1;
	static final int	NOTIFD  = 2;

	static private	 String[]	serverMenuLabels = {
										"Start server",
										"Restart server",
										"Set startup level",
                                        "Uptime",
										"Polling Manager",
										"Polling Profiler",
										"Pool Threads Manager",
										"Configure (Wizard)",
										"DB Attribute Properties",
										"Server Info",
										"Class  Info",
										"Device Dependencies",
										"Test   Device",
										"Check  States",
										"Black Box",
										"Starter logs",
										"Standard Error",
									 };
	static private	 String[]	levelMenuLabels = { 
										"Start servers",
										"Stop  servers",
										"Change level number",
										"Set startup level for each server",
										"Uptime",
										"Expand Tree",
									 };
	static private	 String[]	notifdMenuLabels = { 
										"Start daemon",
									 };
	static private final int	OFFSET = 2;		//	Label And separator
	static private final int	START_STOP      =  0;
	static private final int	RESTART         =  1;
	static private final int	STARTUP_LEVEL   =  2;
	static private final int	UPTIME          =  3;
	static private final int	POLLING_MANAGER =  4;
	static private final int	POLLING_PROFILER=  5;
	static private final int	POOL_THREAD_MAN =  6;
	static private final int	CONFIGURE       =  7;
	static private final int	DB_ATTRIBUTES   =  8;
	static private final int	SERVER_INFO     =  9;
	static private final int	CLASS_INFO      = 10;
	static private final int	DEPENDENCIES    = 11;
	static private final int	TEST_DEVICE     = 12;
	static private final int	CHECK_STATES    = 13;
	static private final int	BLACK_BOX       = 14;
	static private final int	STARTER_LOGS    = 15;
	static private final int	STD_ERROR       = 16;

	static private final int	START         =  0;
	static private final int	STOP          =  1;
	static private final int	CHANGE_LEVEL  =  2;
	static private final int	SERVER_LEVELS =  3;
	static private final int	UPTIME_LEVEL  =  4;
	static private final int	EXPAND        =  5;

	static private final boolean	TANGO_7   =  true;
	//==========================================================
	/**
	 *	Class Constructor
	 */
	//==========================================================
	public ServerPopupMenu(JFrame frame, HostInfoDialog parent, TangoHost host, int mode) {
		super ();
		this.frame  = frame;
		this.parent = parent;
		this.host   = host;
		this.mode  = mode;

		buildBtnPopupMenu();
	}
	//===============================================================
	/**
	 *	Create a Popup menu for host control
	 */
	//===============================================================
	private void buildBtnPopupMenu()
	{
		JLabel	title = new JLabel("Server Control :");
        title.setFont(new java.awt.Font("Dialog", 1, 16));
		add(title);
		add(new JPopupMenu.Separator());
		String[] pMenuLabels;
		switch (mode)
		{
		case SERVERS:
			pMenuLabels = serverMenuLabels;
			break;
		case LEVELS:
			pMenuLabels = levelMenuLabels;
			break;
		default:
			title.setText("Event Notify Daemon");
			pMenuLabels = notifdMenuLabels;
		}


		for (String lbl : pMenuLabels)
		{
			JMenuItem btn = new JMenuItem(lbl);
			btn.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					cmdActionPerformed(evt);
				}
			});
			add(btn);
		}
	}
	//======================================================
	/**
	 *	Manage event on clicked mouse on Servere object.
	 */
	//======================================================
	//======================================================
	//======================================================
	public void showMenu(MouseEvent evt, JTree tree, TangoServer server)
	{
		//	Set selection at mouse position
		TreePath	selectedPath =
			tree.getPathForLocation(evt.getX(), evt.getY());

		if (selectedPath==null)
			return;
		tree.setSelectionPath(selectedPath);


		String	name = server.getName();

		//	Add host name in menu label title
		JLabel	lbl = (JLabel)getComponent(0);
		lbl.setText("  " + name + "  :");

		this.server = server;

		//	Set label (depends on server runninig or not)
		JMenuItem	mi = (JMenuItem)getComponent(START_STOP+OFFSET);
		boolean running = (server.getState()==DevState.ON);

		if (running || server.getState()==DevState.MOVING)
			mi.setText("Kill  Server");
		else
			mi.setText("Start Server");

        //	And set if enable or not
		getComponent(RESTART+OFFSET).setEnabled(running);
		//getComponent(UPTIME+OFFSET).setEnabled(running);
		getComponent(POLLING_PROFILER+OFFSET).setEnabled(running);
		getComponent(TEST_DEVICE+OFFSET).setEnabled(running);
		getComponent(CHECK_STATES+OFFSET).setEnabled(running);
		getComponent(BLACK_BOX+OFFSET).setEnabled(running);
		getComponent(CONFIGURE+OFFSET).setEnabled(running);
		getComponent(DB_ATTRIBUTES+OFFSET).setVisible(!running);


		getComponent(POOL_THREAD_MAN+OFFSET).setVisible(TANGO_7);
		getComponent(DEPENDENCIES+OFFSET).setVisible(TANGO_7);

		//	Get position and display Popup menu
		location = tree.getLocationOnScreen();
		location.x += evt.getX();
		location.y += evt.getY();
		show(tree, evt.getX(), evt.getY());
	}
	//======================================================
	/*
	 *	Manage event on clicked mouse on Level object.
	 */
	//======================================================
	public void showMenu(MouseEvent evt, LevelTree tree, boolean expanded)
	{
		this.tree = tree;

		//	Add host name in menu label title
		JLabel	lbl = (JLabel)getComponent(0);
		lbl.setText("  " + tree + "  :");

		
		JMenuItem	mi = (JMenuItem)getComponent(EXPAND+OFFSET);
		mi.setText((expanded)? "Collapse Tree" : levelMenuLabels[EXPAND]);

		getComponent(SERVER_LEVELS+OFFSET).setVisible(false);

		//	Get position and display Popup menu
		location = tree.getLocationOnScreen();
		location.x += evt.getX();
		location.y += evt.getY();
		show(tree, evt.getX(), evt.getY());
	}
	//======================================================
	/*
	 *	Manage event on clicked mouse on Notifd object.
	 */
	//======================================================
	public void showMenu(MouseEvent evt, int state)
	{
		JLabel lbl = (JLabel)evt.getSource();

		boolean		running = (host.notifyd_state==all_ok);
		getComponent(START+OFFSET).setEnabled(!running);

		//	Get position and display Popup menu
		location = lbl.getLocationOnScreen();
		show(lbl, evt.getX(), evt.getY());
	}
	//======================================================
	/**
	 *	Called when popup menu item selected
	 */
	//======================================================
	private	Point	location;
	private void cmdActionPerformed(ActionEvent evt)
	{
		switch (mode)
		{
		case SERVERS:
			serverCmdActionPerformed(evt);
			break;
		case LEVELS:
			levelCmdActionPerformed(evt);
			break;
		case NOTIFD:
			notifdCmdActionPerformed(evt);
			break;
		}
	}
	//======================================================
	//======================================================
	private void notifdCmdActionPerformed(ActionEvent evt)
	{
		Object	obj = evt.getSource();
        int     cmdidx = -1;
        for (int i=0 ; i<notifdMenuLabels.length ; i++)
            if (getComponent(OFFSET+i)==obj)
                cmdidx = i;

		switch(cmdidx)
		{
		case START:
			host.startServer(parent,notifyd_prg + "/" + host.getName());
			break;
		}
	}
	//======================================================
	//======================================================
	private void levelCmdActionPerformed(ActionEvent evt)
	{
		Object	obj = evt.getSource();
        int     cmdidx = -1;
        for (int i=0 ; i<levelMenuLabels.length ; i++)
            if (getComponent(OFFSET+i)==obj)
                cmdidx = i;

		switch(cmdidx)
		{
		case START:
			parent.startLevel(tree.getLevelRow());
			break;
		case STOP:
			parent.stopLevel(tree.getLevelRow());
			break;
		case CHANGE_LEVEL:
			tree.changeChangeLevel(tree.getLevelRow());
			break;
		case SERVER_LEVELS:
			tree.changeServerLevels();
			break;
		case UPTIME_LEVEL:
            tree.displayUptime();
            break;
		case EXPAND:
			tree.toggleExpandCollapse();
			break;
		}		
	}
	//======================================================
	//======================================================
	private void serverCmdActionPerformed(ActionEvent evt)
	{
		Object	obj = evt.getSource();
        int     cmdidx = -1;
        for (int i=0 ; i<serverMenuLabels.length ; i++)
            if (getComponent(OFFSET+i)==obj)
                cmdidx = i;

		switch(cmdidx)
		{
		case STARTUP_LEVEL:
			if (server.startupLevel(parent, host.getName(), location))
				parent.updateData();
			break;
		case UPTIME:
            try {
                String[]    exportedStr = server.getServerUptime();
                String[]    columns = new String[] { "Last   exported", "Last unexported" };
                app_util.PopupTable ppt =
                        new app_util.PopupTable(parent, server.getName(),
                            columns, new String[][] { exportedStr }, new Dimension(450, 50));
                ppt.setVisible(true);
            }
            catch(DevFailed e) {
                ErrorPane.showErrorMessage(parent, null, e);
            }
            break;
		case POLLING_MANAGER:
			if (server.getState()==DevState.ON)
				new ManagePollingDialog(parent, server).setVisible(true);
			else
				try {
					new DbPollPanel(parent, server.getName()).setVisible(true);
				}
				catch (DevFailed e) {
					ErrorPane.showErrorMessage(parent, null, e);
				}
			break;
		case POOL_THREAD_MAN:
			server.poolThreadManager(parent, host);
			break;
		case POLLING_PROFILER:
			startPollingProfiler();
			break;
		case TEST_DEVICE:
			server.testDevice(parent);
			break;
		case CHECK_STATES:
			server.checkStates(parent);
			break;
		case BLACK_BOX:
			server.displayBlackBox(parent);
			break;
		case STARTER_LOGS:
			host.displayLogging(parent, server.toString());
			break;
		case CONFIGURE:
			server.configureWithWizard(parent);
			break;
		case SERVER_INFO:
			server.displayServerInfo(parent);
			break;
		case DB_ATTRIBUTES:
			server.manageMemorizedAttributes(parent);
			break;
		case CLASS_INFO:
			server.displayClassInfo(frame);
			break;
		case DEPENDENCIES:
			try
			{
				new DeviceHierarchyDialog(parent,
						server.getName()).setVisible(true);
			}
			catch(DevFailed e)
			{
				ErrorPane.showErrorMessage(parent, null, e);
			}
			break;
		case STD_ERROR:
			if (server!=null)
				host.readStdErrorFile(frame, server.getName());
			else
				host.readStdErrorFile(frame,notifyd_prg + "/" + host.getName());
			break;
		case RESTART:
			server.restart(parent, host, true);
			break;

		case START_STOP:  
			if (server.getState()==DevState.ON ||
				server.getState()==DevState.MOVING)
			{
				try
				{
					//	Ask to confirm
					if (JOptionPane.showConfirmDialog(parent,
							"Are you sure to want to kill " + server.getName(),
							"Confirm Dialog",
							JOptionPane.YES_NO_OPTION)!=JOptionPane.OK_OPTION)
						return;

					host.stopServer(server.getName());
				}
				catch (DevFailed e)
				{
					//	Check if only moving
					if (e.errors[0].reason.equals("SERVER_NOT_RESPONDING"))
					{
						try
						{
							//	Ask to confirm
							if (JOptionPane.showConfirmDialog(parent,
									e.errors[0].desc + "\n" +
									"Do you even want to kill it ?",
									"Confirm Dialog",
									JOptionPane.YES_NO_OPTION)!=JOptionPane.OK_OPTION)
								return;
							host.hardKillServer(server.getName());
						}
						catch (DevFailed e2)
						{
							ErrorPane.showErrorMessage(parent, null, e2);
						}
					}
					else
						ErrorPane.showErrorMessage(parent, null, e);
				}
			}
			else
				host.startServer(parent, server.getName());
			break;
		}
	}
	//======================================================
	//======================================================
	private void startPollingProfiler()
	{
		try
		{
			String[]	devnames = server.queryDevice();
			new PollingProfiler(parent, devnames).setVisible(true);
		}
		catch (DevFailed e)
		{
            ErrorPane.showErrorMessage(parent, null, e);
		}
	}
}
			//server.stopServer(parent);
