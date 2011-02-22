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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


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
										"Manage Polling",
										"Configure (Wizard)",
										"Memorized Attributes",
										"Server Info",
										"Class  Info",
										"Test   Device",
										"Check  States",
										"Start daemon",
										"Standard Error",
									 };
	static private final int	OFFSET = 2;		//	Label And separator
	static private final int	START_STOP    =  0;
	static private final int	RESTART       =  1;
	static private final int	STARTUP_LEVEL =  2;
	static private final int	MANAGE_POLLING=  3;
	static private final int	CONFIGURE     =  4;
	static private final int	MEMO_ATTRIB   =  5;
	static private final int	SERVER_INFO   =  6;
	static private final int	CLASS_INFO    =  7;
	static private final int	TEST_DEVICE   =  8;
	static private final int	CHECK_STATES  =  9;
	static private final int	START_DAEMON  = 10;
	static private final int	STD_ERROR     = 11;

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
			boolean		running = server.isRunning();
			if (running)
				mi.setText("Kill  Server");
			else
				mi.setText("Start Server");
            mi.setEnabled(host_state!=moving);

            //	And set if enable or not
			getComponent(RESTART+OFFSET).setEnabled(running);
			getComponent(MANAGE_POLLING+OFFSET).setEnabled(running);
			getComponent(TEST_DEVICE+OFFSET).setEnabled(running);
			getComponent(CHECK_STATES+OFFSET).setEnabled(running);
			getComponent(CONFIGURE+OFFSET).setEnabled(running);
			getComponent(MEMO_ATTRIB+OFFSET).setVisible(!running);
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
			if (server.startupLevel(parent, host.getName(), location))
				parent.updateData();
				//parent.checkUpdatePanel(true);
		}
		else
		if (cmd.equals(pMenuLabels[MANAGE_POLLING]))
			new ManagePollingDialog(parent, server, location).setVisible(true);
		else
		if (cmd.equals(pMenuLabels[TEST_DEVICE]))
			server.testDevice(parent, location);
		else
		if (cmd.equals(pMenuLabels[CHECK_STATES]))
			server.checkStates(parent, location);
		else
		if (cmd.equals(pMenuLabels[CONFIGURE]))
			server.configureWithWizard(parent);
		else
		if (cmd.equals(pMenuLabels[SERVER_INFO]))
			server.displayServerInfo(parent);
		else
		if (cmd.equals(pMenuLabels[MEMO_ATTRIB]))
			server.manageMemorizedAttributes(parent);
		else
		if (cmd.equals(pMenuLabels[CLASS_INFO]))
			server.displayClassInfo(frame);
		else
		if (cmd.equals(pMenuLabels[STD_ERROR]))
			if (server!=null)
				host.readStdErrorFile(frame, server.getName());
			else
				host.readStdErrorFile(frame,notifyd_prg + "/" + host.getName());
		else
		if (cmd.equals(pMenuLabels[START_DAEMON]))
			host.startServer(parent,notifyd_prg + "/" + host.getName());
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
