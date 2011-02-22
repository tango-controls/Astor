//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the TempClass class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.9  2004/07/08 11:22:58  pascal_verdier
// First revision able to use events.
//
// Revision 3.8  2004/04/13 12:17:29  pascal_verdier
// DeviceTree class uses the new browsing database commands.
//
// Revision 3.7  2004/03/03 08:31:05  pascal_verdier
// The server restart command has been replaced by a stop and start command in a thread.
// The delete startup level info has been added.
//
// Revision 3.6  2004/02/04 14:37:43  pascal_verdier
// Starter logging added
// Database info added on CtrlServersDialog.
//
// Revision 3.5  2003/11/25 15:56:46  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
// Revision 3.4  2003/11/05 10:34:57  pascal_verdier
// Main Panel screen centering.
// Starter multi path added.
// little bugs fixed.
//
// Revision 3.3  2003/10/20 08:55:15  pascal_verdier
// Bug on tree popup menu position fixed.
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

import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;


import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class TreePopupMenu extends JPopupMenu  implements AstorDefs
{
	private DefaultMutableTreeNode	node_cut    = null;
	private Astor		astor;
	private AstorTree	parent;
	private TangoHost	host;
	private Point		evt_position;
	private	String		collec_name;
	private TangoHost[]	collec_hosts;

	static private String[]	pMenuLabels = {
									"Open control Panel",
									"Disable control",
									"Enable  control",
									"Remote Login",
									"Starter info",
									"Starter test",
									"Starter Logging",
									"Unexport Starter device",
									"Start all Servers",
									"Stop  all Servers",
									"Clone",
									"Cut",
									"Paste",
									"Edit Properties",
									"Remove",
									"Change Name"
									};
	
	static private final int	OFFSET = 2;		//	Label And separator
	static private final int	OPEN_PANEL   = 0;
	static private final int	DISABLE      = 1;
	static private final int	ENABLE       = 2;

	//	Host menu specific
	static private final int	REM_LOGIN    = 3;
	static private final int	STARTER_INFO = 4;
	static private final int	STARTER_TEST = 5;
	static private final int	STARTER_LOGG = 6;
	static private final int	UNEXPORT_STARTER = 7;

	//	Collec menu specific
	static private final int	START_SERVERS= 8;
	static private final int	STOP_SERVERS = 9;
	
	//	Edit options
	static private final int	CLONE_HOST   = 10;
	static private final int	CUT_HOST     = 11;
	static private final int	PASTE_HOST   = 12;
	static private final int	EDIT_PROP    = 13;
	static private final int	REMOVE_HOST  = 14;
	static private final int	CHANGE_NAME  = 15;

 	//===============================================================
	//===============================================================
	public TreePopupMenu(Astor astor, AstorTree parent)
	{
		super();
		this.astor  = astor;
		this.parent = parent;

		buildBtnPopupMenu();
	}
	//===============================================================
	/**
	 *	Create a Popup menu for host control
	 */
	//===============================================================
	private void buildBtnPopupMenu()
	{
		JLabel	title = new JLabel("Host Control :");
        title.setFont(new java.awt.Font("Dialog", 1, 16));
		add(title);
		add(new JPopupMenu.Separator());

		for (int i=0 ; i<pMenuLabels.length ; i++)
		{
			JMenuItem	btn = new JMenuItem(pMenuLabels[i]);
   			btn.addActionListener (new java.awt.event.ActionListener () {
				public void actionPerformed (ActionEvent evt) {
            		hostActionPerformed(evt);
				}
			});
			add(btn);
		}
	}
	//======================================================
	//======================================================
	private boolean getSelectedObject()
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                   parent.getLastSelectedPathComponent();
		if (node==null)
			return false;

		Object obj = node.getUserObject();

		if (obj instanceof DbaseObject)
			return false;

		//	Check if a Tango Host
		if (obj instanceof TangoHost)
		{
			host = (TangoHost)obj;
			collec_name = null;
		}
		else
		{
			host = null;
			//	Get collection children
			int	nb = node.getChildCount();
			collec_hosts = new TangoHost[nb];
			for (int i=0 ; i<nb ; i++)
			{
				node = node.getNextNode();
				//	Check if it is really a TangoHost object
				Object o = node.getUserObject();
				if (o instanceof TangoHost)
					collec_hosts[i] = (TangoHost)o;
				else
					return false;
			}
			collec_name = (String) obj;
		}
		return true;
	}
	//======================================================
	//======================================================
	public void showMenu(MouseEvent evt)
	{
		//	Set selection at mouse position
		TreePath	selectedPath =
			parent.getPathForLocation(evt.getX(), evt.getY());

		if (selectedPath==null)
			return;
		Object[]	path  = selectedPath.getPath();
		parent.setSelectionPath(selectedPath);

		//	Get the selected object
		if (getSelectedObject()==false)
			return;

		//	Set all item viesible
		for (int i=OFFSET+DISABLE ; i<getComponentCount() ; i++)
			((JMenuItem)getComponent(i)).setVisible(true);

		//	if selection is host
		if (host!=null)
		{
			//	Add host name in menu label title
			JLabel	lbl = (JLabel)getComponent(0);
			lbl.setText("  " + host.getName() + "  :");

			//	Modify button text
			boolean	do_open = (host.do_polling && host.state!=faulty);

			((JMenuItem)getComponent(OFFSET+OPEN_PANEL)).setEnabled(do_open);
			if (host.use_events)
			{
				((JMenuItem)getComponent(OFFSET+DISABLE)).setVisible(false);
				((JMenuItem)getComponent(OFFSET+ENABLE)).setVisible(false);
			}
			else
			{
				((JMenuItem)getComponent(OFFSET+DISABLE)).setEnabled(host.do_polling);
				((JMenuItem)getComponent(OFFSET+ENABLE)).setEnabled(!host.do_polling);
			}
			((JMenuItem)getComponent(OFFSET+START_SERVERS)).setVisible(false);
			((JMenuItem)getComponent(OFFSET+STOP_SERVERS)).setVisible(false);

			((JMenuItem)getComponent(OFFSET+CUT_HOST)).setEnabled(true);
			((JMenuItem)getComponent(OFFSET+PASTE_HOST)).setEnabled(false);
			((JMenuItem)getComponent(OFFSET+EDIT_PROP)).setEnabled(true);
			((JMenuItem)getComponent(OFFSET+CHANGE_NAME)).setVisible(false);
			((JMenuItem)getComponent(OFFSET+CLONE_HOST)).setEnabled(true);

			boolean can_test = (host.state==all_ok  ||  host.state==alarm);
			((JMenuItem)getComponent(OFFSET+STARTER_TEST)).setEnabled(can_test);
			((JMenuItem)getComponent(OFFSET+STARTER_LOGG)).setEnabled(can_test);
			((JMenuItem)getComponent(OFFSET+REMOVE_HOST)).setEnabled(!can_test);

			boolean can_unexport = (host.state==unknown);
			((JMenuItem)getComponent(OFFSET+UNEXPORT_STARTER)).setVisible(can_unexport);
		}
		else
		//	if selection is collection
		if (collec_name!=null)
		{
			//	Add collec name in menu label title
			JLabel	lbl = (JLabel)getComponent(0);
			lbl.setText("  " + collec_name + "  :");

			//	Modify visibility
			((JMenuItem)getComponent(OFFSET+OPEN_PANEL)).setEnabled(false);
			((JMenuItem)getComponent(OFFSET+DISABLE)).setEnabled(true);
			((JMenuItem)getComponent(OFFSET+ENABLE)).setEnabled(true);
			((JMenuItem)getComponent(OFFSET+REM_LOGIN)).setVisible(false);
			((JMenuItem)getComponent(OFFSET+STARTER_INFO)).setVisible(false);
			((JMenuItem)getComponent(OFFSET+STARTER_TEST)).setVisible(false);
			((JMenuItem)getComponent(OFFSET+STARTER_LOGG)).setVisible(false);

			((JMenuItem)getComponent(OFFSET+CLONE_HOST)).setEnabled(false);
			((JMenuItem)getComponent(OFFSET+CUT_HOST)).setEnabled(false);
			((JMenuItem)getComponent(OFFSET+EDIT_PROP)).setEnabled(false);
			((JMenuItem)getComponent(OFFSET+PASTE_HOST)).setEnabled(node_cut!=null);
			((JMenuItem)getComponent(OFFSET+REMOVE_HOST)).setEnabled(false);
			((JMenuItem)getComponent(OFFSET+UNEXPORT_STARTER)).setVisible(false);
		}
		
		//	Do not do it if Windows
		//---------------------------------------
		if (AstorUtil.osIsUnix()==false)
			((JMenuItem)getComponent(OFFSET+REM_LOGIN)).setVisible(false);

		evt_position = new Point(evt.getX(), evt.getY());
		show(parent, evt.getX(), evt.getY());
	}
	//===============================================================
	//===============================================================
	private void hostActionPerformed(ActionEvent evt)
	{
 		String  cmd = evt.getActionCommand();
		if (cmd.equals(pMenuLabels[OPEN_PANEL]))
			parent.displayHostInfo(evt_position);
		else
		if (cmd.equals(pMenuLabels[UNEXPORT_STARTER]))
			host.unexportStarter(astor);
		else
		if (cmd.equals(pMenuLabels[STARTER_TEST]))
			host.testStarter(astor);
		else
		if (cmd.equals(pMenuLabels[STARTER_LOGG]))
			host.displayLogging(astor);
		else
		if (cmd.equals(pMenuLabels[STARTER_INFO]))
			host.displayInfo(parent);
		else
		if (cmd.equals(pMenuLabels[REM_LOGIN]))
			new RemoteLoginThread(host.getName(), parent).start();
		else
		if (cmd.equals(pMenuLabels[ENABLE]))
			enablePolling(true);
		else
		if (cmd.equals(pMenuLabels[DISABLE]))
			enablePolling(false);
		else
		if (cmd.equals(pMenuLabels[CLONE_HOST]))
			astor.addNewHost(host);
		else
		if (cmd.equals(pMenuLabels[EDIT_PROP]))
			astor.editHostProperties(host);
		else
		if (cmd.equals(pMenuLabels[CUT_HOST]))
			node_cut = (DefaultMutableTreeNode)
                                   parent.getLastSelectedPathComponent();
		else
		if (cmd.equals(pMenuLabels[PASTE_HOST]))
		{
			parent.moveNode(node_cut);
			node_cut = null;
		}
		else
		if (cmd.equals(pMenuLabels[REMOVE_HOST]))
			astor.removeHost(host.getName());
		else
		if (cmd.equals(pMenuLabels[CHANGE_NAME]))
			parent.changeNodeName();
		else
		if (cmd.equals(pMenuLabels[START_SERVERS]))
			new ServerCmdThread(astor, collec_hosts, StartAllServers).start();
		else
		if (cmd.equals(pMenuLabels[STOP_SERVERS]))
			new ServerCmdThread(astor, collec_hosts, StopAllServers).start();
	}

	//===============================================================
	//===============================================================
	private void enablePolling(boolean enable)
	{
		if (host!=null)
		{
			host.do_polling = enable;
			if (enable==false)
				parent.hostDialogs.close(host);
		}
		else
		for (int i=0 ; i<collec_hosts.length ; i++)
		{
			collec_hosts[i].do_polling = enable;
			if (enable==false)
				parent.hostDialogs.close(collec_hosts[i]);
		}
	}
	//===============================================================
	//===============================================================
}

