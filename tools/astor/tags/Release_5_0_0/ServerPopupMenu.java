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


import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import admin.astor.tools.PollingProfiler;

//===============================================================
/**
 *	A class to display a popup menu.
 */
//===============================================================
public class ServerPopupMenu extends JPopupMenu implements AstorDefs {

	private TangoHost		host;
	private TangoServer		server;
	private JFrame			frame;
	private HostInfoDialog	parent;
	
	
	/**
	 *	Popup menu to be used on right button clicked.
	 */
	static private	 String[]	pMenuLabels = { 
										"Start server",
										"Restart server",
										"Set startup level",
										"Polling Manager",
										"Polling Profiler",
										"Configure (Wizard)",
										"DB Attribute Properties",
										"Server Info",
										"Class  Info",
										"Test   Device",
										"Check  States",
										"Start daemon",
										"Standard Error",
									 };
	static private final int	OFFSET = 2;		//	Label And separator
	static private final int	START_STOP      =  0;
	static private final int	RESTART         =  1;
	static private final int	STARTUP_LEVEL   =  2;
	static private final int	POLLING_MANAGER =  3;
	static private final int	POLLING_PROFILER=  4;
	static private final int	CONFIGURE       =  5;
	static private final int	DB_ATTRIBUTES   =  6;
	static private final int	SERVER_INFO     =  7;
	static private final int	CLASS_INFO      =  8;
	static private final int	TEST_DEVICE     =  9;
	static private final int	CHECK_STATES    = 10;
	static private final int	START_DAEMON    = 11;
	static private final int	STD_ERROR       = 12;

	//==========================================================
	/**
	 *	Class Constructor
	 */
	//==========================================================
	public ServerPopupMenu(JFrame frame, HostInfoDialog parent, TangoHost host) {
		super ();
		this.frame  = frame;
		this.parent = parent;
		this.host   = host;

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
		for (int i=0 ; i<pMenuLabels.length ; i++)
		{
			JMenuItem	btn = new JMenuItem(pMenuLabels[i]);
   			btn.addActionListener (new java.awt.event.ActionListener () {
				public void actionPerformed (ActionEvent evt) {
            		serverCmdActionPerformed(evt);
				}
			});
			add(btn);
		}
	}
	//======================================================
	/**
	 *	Manage event on clicked mouse on PogoTree object.
	 */
	//======================================================
	private boolean		running;
	public void showMenu(java.awt.event.MouseEvent evt, int host_state)
	{
		Object	o = evt.getSource();
        if (!(o instanceof JLabel) || isVisible())
		{
			setVisible(false);
			return;
		}
		//	Get the selected server
		JLabel btn = (JLabel)evt.getSource();
		String	name = btn.getText();

		//	Add host name in menu label title
		JLabel	lbl = (JLabel)getComponent(0);
		lbl.setText("  " + name + "  :");

		if (name.startsWith("Events"))
		{
			server = null;
			//	Manage menu for notify daemon
			for (int i=0 ; i<pMenuLabels.length ; i++)
				getComponent(i+OFFSET).setVisible(false);
			getComponent(START_DAEMON+OFFSET).setVisible(true);
			getComponent(STD_ERROR+OFFSET).setVisible(false);

			boolean		running = (host.notifyd_state==all_ok);
			getComponent(START_DAEMON+OFFSET).setEnabled(!running);
		}
		else
		{
			server = host.getServer(btn.getText());

			//	Manage menu for Tango device server
			for (int i=0 ; i<pMenuLabels.length ; i++)
				getComponent(i+OFFSET).setVisible(true);
			getComponent(START_DAEMON+OFFSET).setVisible(false);


			//	Set label (depends on server runninig or not)
			JMenuItem	mi = (JMenuItem)getComponent(START_STOP+OFFSET);
			boolean running = (server.getState()==DevState.ON);

			if (running || server.getState()==DevState.MOVING)
				mi.setText("Kill  Server");
			else
				mi.setText("Start Server");
            //mi.setEnabled(host_state!=moving);

            //	And set if enable or not
			getComponent(RESTART+OFFSET).setEnabled(running);
			getComponent(POLLING_PROFILER+OFFSET).setEnabled(running);
			getComponent(TEST_DEVICE+OFFSET).setEnabled(running);
			getComponent(CHECK_STATES+OFFSET).setEnabled(running);
			getComponent(CONFIGURE+OFFSET).setEnabled(running);
			getComponent(DB_ATTRIBUTES+OFFSET).setVisible(!running);
		}
		//	Get position and display Popup menu
		//---------------------------------------
		location = btn.getLocationOnScreen();
		show(btn, evt.getX(), evt.getY());
	}
	//======================================================
	/**
	 *	Called when popup menu item selected
	 */
	//======================================================
	private	Point	location;
	private void serverCmdActionPerformed(ActionEvent evt)
	{
		Object	obj = evt.getSource();
        int     cmdidx = 0;
        for (int i=0 ; i<pMenuLabels.length ; i++)
            if (getComponent(OFFSET+i)==obj)
                cmdidx = i;

		switch(cmdidx)
		{
		case STARTUP_LEVEL:
			if (server.startupLevel(parent, host.getName(), location))
				parent.updateData();
			break;
		case POLLING_MANAGER:
			if (server.getState()==DevState.ON)
				new ManagePollingDialog(parent, server).setVisible(true);
			else
				new DbPollPanel(parent, server.getName()).setVisible(true);
			break;
		case POLLING_PROFILER:
			startPollingProfiler();
			break;
		case TEST_DEVICE:
			server.testDevice(parent, location);
			break;
		case CHECK_STATES:
			server.checkStates(parent, location);
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
		case STD_ERROR:
			if (server!=null)
				host.readStdErrorFile(frame, server.getName());
			else
				host.readStdErrorFile(frame,notifyd_prg + "/" + host.getName());
			break;
		case START_DAEMON:
			host.startServer(parent,notifyd_prg + "/" + host.getName());
			break;
		case RESTART:
			server.restart(parent, host);
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
