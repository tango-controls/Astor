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
// Copyright 1995 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;
 


/** 
 *	This class display a JPopupMenu.for server commands
 *
 * @author  verdier
 * @Revision 
 */


import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;


//===============================================================
/**
 *	A Dialog Class to get the State parameters.
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
										"Manage Polling",
										"Server Info",
										"Class  Info",
										"Test   Device",
										"Check  States",
										"Start daemon",
										"Standard Error",
									 };
	static private final int	OFFSET = 2;		//	Label And separator
	static private final int	START_STOP    = 0;
	static private final int	RESTART       = 1;
	static private final int	STARTUP_LEVEL = 2;
	static private final int	MANAGE_POLLING= 3;
	static private final int	SERVER_INFO   = 4;
	static private final int	CLASS_INFO    = 5;
	static private final int	TEST_DEVICE   = 6;
	static private final int	CHECK_STATES  = 7;
	static private final int	START_DAEMON  = 8;
	static private final int	STD_ERROR     = 9;

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
	public void showMenu(java.awt.event.MouseEvent evt)
	{
		int mask = evt.getModifiers();
		Object	o = evt.getSource();

		if ((o instanceof JButton)==false || isVisible())
		{
			setVisible(false);
			return;
		}

		//	Get the selected server

		JButton btn = (JButton)evt.getSource();
		String	name = btn.getText();

		//	Add host name in menu label title
		JLabel	lbl = (JLabel)getComponent(0);
		lbl.setText("  " + name + "  :");

		if (name.startsWith("Events"))
		{
			server = null;
			//	Manage menu for notify daemon
			for (int i=0 ; i<pMenuLabels.length ; i++)
				((JMenuItem)getComponent(i+OFFSET)).setVisible(false);
			((JMenuItem)getComponent(START_DAEMON+OFFSET)).setVisible(true);
			((JMenuItem)getComponent(STD_ERROR+OFFSET)).setVisible(true);

			boolean		running = (host.notifyd_state==all_ok);
			((JMenuItem)getComponent(START_DAEMON+OFFSET)).setEnabled(!running);
		}
		else
		{
			server = host.getServer(btn.getText());

			//	Manage menu for Tango device server
			for (int i=0 ; i<pMenuLabels.length ; i++)
				((JMenuItem)getComponent(i+OFFSET)).setVisible(true);
			((JMenuItem)getComponent(START_DAEMON+OFFSET)).setVisible(false);


			//	Set label (depends on server runninig or not)
			JMenuItem	mi = (JMenuItem)getComponent(START_STOP+OFFSET);
			boolean		running = server.isRunning();
			if (running)
				mi.setText("Kill  Server");
			else
				mi.setText("Start Server");

			//	And set if enable or not
			((JMenuItem)getComponent(RESTART+OFFSET)).setEnabled(running);
			((JMenuItem)getComponent(MANAGE_POLLING+OFFSET)).setEnabled(running);
			((JMenuItem)getComponent(TEST_DEVICE+OFFSET)).setEnabled(running);
			((JMenuItem)getComponent(CHECK_STATES+OFFSET)).setEnabled(running);
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
 		String  cmd = evt.getActionCommand();
		if (cmd.equals(pMenuLabels[STARTUP_LEVEL]))
		{
			if (server.startupLevel(parent, host.getName(), location)==true)
				parent.checkUpdatePanel(true);
		}
		else
		if (cmd.equals(pMenuLabels[MANAGE_POLLING]))
			new ManagePollingDialog(frame, parent, server, location).show();
		else
		if (cmd.equals(pMenuLabels[TEST_DEVICE]))
			server.testDevice(parent, location);
		else
		if (cmd.equals(pMenuLabels[CHECK_STATES]))
			server.checkStates(parent, location);
		else
		if (cmd.equals(pMenuLabels[SERVER_INFO]))
			server.displayServerInfo(parent);
		else
		if (cmd.equals(pMenuLabels[CLASS_INFO]))
			server.displayClassInfo(frame);
		else
		if (cmd.equals(pMenuLabels[STD_ERROR]))
			if (server!=null)
				host.readStdErrorFile(frame, server.getName());
			else
				host.readStdErrorFile(frame, 
					new String(notifyd_prg + "/" + host.getName()));
		else
		if (cmd.equals(pMenuLabels[START_DAEMON]))
			host.startServer(parent, 
					new String(notifyd_prg + "/" + host.getName()));
		else
		if (cmd.equals(pMenuLabels[RESTART]))
		{
			server.restart(parent, host);
		}
		else
		if (server.isRunning())
		{
			try
			{
				//	Ask to confirm
				if (JOptionPane.showConfirmDialog(parent,
						"Are you sure to want to kill " + server.getName(),
						"Confirm Dialog",
						JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
				{
					host.stopServer(server.getName());
				}
			}
			catch (DevFailed e) {
				app_util.PopupError.show(parent, e);
			}
		}
		else
			host.startServer(parent, server.getName());
	}
}
			//server.stopServer(parent);
