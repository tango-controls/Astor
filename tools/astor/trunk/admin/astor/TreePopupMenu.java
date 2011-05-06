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
// Revision 3.22  2009/06/02 15:19:05  pascal_verdier
// Remove serialization between HostStateThread and HostInfoDialogVector.
//
// Revision 3.21  2009/01/30 09:31:50  pascal_verdier
// Black box management added for database.
// Black box management tool improved.
// Find TANGO object by filter added.
//
// Revision 3.20  2009/01/16 14:46:58  pascal_verdier
// Black box management added for host and Server.
// Starter logging display added for host and server.
// Splash screen use ATK one.
//
// Revision 3.19  2008/11/19 09:59:56  pascal_verdier
// New tests done on Access control.
// Pool Threads management added.
// Size added as preferences.
//
// Revision 3.18  2008/03/03 14:55:21  pascal_verdier
// Starter Release_4 management.
//
// Revision 3.17  2008/01/21 21:25:54  pascal_verdier
// Bug in index fixed.
//
// Revision 3.16  2008/01/18 10:11:22  pascal_verdier
// OSManager management removed
//
// Revision 3.15  2007/11/07 09:05:38  pascal_verdier
// Display host info if OSManage DS  is running on host.
// Display host's state on HotInfoDialog.
//
// Revision 3.14  2006/04/12 13:07:16  pascal_verdier
// updateServerList command added.
//
// Revision 3.13  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.12  2005/10/20 13:24:49  pascal_verdier
// Screen position management has been changed.
//
// Revision 3.11  2005/06/02 09:02:36  pascal_verdier
// Minor changes.
//
// Revision 3.10  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
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

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;

public class TreePopupMenu extends JPopupMenu  implements AstorDefs
{
	private Astor		astor;
	private AstorTree	parent;
	private TangoHost	host;
	private	String		collec_name;
	private TangoHost[]	collec_hosts;

	static private String[]	menuLabels = {
									"Open control Panel",
									"Disable control",
									"Enable  control",
									"Remote Login",
									"Starter info",
									"Starter test",
									"Unexport Starter device",
									"Branch  info",
									"Start all Servers",
									"Stop  all Servers",
									"Reset Statistics",
									"Clone",
									"Change branch",
									"Edit Properties",
									"Remove",
									"Black Box",
									"Starter Logs",
                                    "Uptime for Servers",
									"Force Update",
									"Change Name",
						};

	static private final int	OFFSET = 2;		//	Label And separator
	static private final int	OPEN_PANEL   = 0;
	static private final int	DISABLE      = 1;
	static private final int	ENABLE       = 2;

	//	Host menu specific
	static private final int	REM_LOGIN    = 3;
	static private final int	STARTER_INFO = 4;
	static private final int	STARTER_TEST = 5;
	static private final int	UNEXPORT_STARTER = 6;

	//	Collection menu specific
	static private final int	COLLEC_INFO    = 7;
	static private final int	START_SERVERS  = 8;
	static private final int	STOP_SERVERS   = 9;
	static private final int	RESET_STAT     = 10;

	//	Edit options
	static private final int	CLONE_HOST     = 11;
	static private final int	CHANGE_BRANCHE = 12;
	static private final int	EDIT_PROP      = 13;
	static private final int	REMOVE_HOST    = 14;
	static private final int	BLACK_BOX      = 15;
	static private final int	STARTER_LOGS   = 16;
    static private final int	UPTIME_SERVERS = 17;
	static private final int	UPDATE         = 18;
	static private final int	CHANGE_NAME    = 19;

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
	//===============================================================
	public TreePopupMenu(AstorTree parent)
	{
		super();
		this.astor  = null;
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

		for (String menuLabel : menuLabels)
		{
			JMenuItem btn = new JMenuItem(menuLabel);
			btn.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
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
	@SuppressWarnings({"PointlessArithmeticExpression"})
	public void showMenu(MouseEvent evt)
	{
		//	Set selection at mouse position
		TreePath	selectedPath =
			parent.getPathForLocation(evt.getX(), evt.getY());

		if (selectedPath==null)
			return;
		parent.setSelectionPath(selectedPath);

		//	Get the selected object
		if (!getSelectedObject())
			return;

		//	Set all item viesible
		for (int i=OFFSET+DISABLE ; i<getComponentCount() ; i++)
			getComponent(i).setVisible(true);

		//	if selection is host
		if (host!=null)
		{
			//	Add host name in menu label title
			JLabel	lbl = (JLabel)getComponent(0);
			lbl.setText("  " + host.getName() + "  :");

			//	Modify button text
			boolean	do_open = (host.do_polling && host.state!=faulty);

			getComponent(OFFSET+OPEN_PANEL).setEnabled(do_open);
			if (host.use_events || !AstorUtil.getCtrlBtn())
			{
				getComponent(OFFSET+DISABLE).setVisible(false);
				getComponent(OFFSET+ENABLE).setVisible(false);
			}
			else
			{
				getComponent(OFFSET+DISABLE).setEnabled(host.do_polling);
				getComponent(OFFSET+ENABLE).setEnabled(!host.do_polling);
			}
			getComponent(OFFSET+COLLEC_INFO).setVisible(false);
			getComponent(OFFSET+START_SERVERS).setVisible(false);
			getComponent(OFFSET+STOP_SERVERS).setVisible(false);
			getComponent(OFFSET+RESET_STAT).setVisible(false);
			getComponent(OFFSET+CHANGE_NAME).setVisible(false);

			getComponent(OFFSET+CHANGE_BRANCHE).setEnabled(true);
			getComponent(OFFSET+EDIT_PROP).setEnabled(true);
			getComponent(OFFSET+CLONE_HOST).setEnabled(true);

			boolean can_test = (host.state==all_ok  ||
						  host.state==alarm || host.state==moving);
			getComponent(OFFSET+STARTER_TEST).setEnabled(can_test);
			getComponent(OFFSET+STARTER_LOGS).setEnabled(can_test);
            getComponent(OFFSET+UPTIME_SERVERS).setVisible(true);
			getComponent(OFFSET+REMOVE_HOST).setEnabled(!can_test);
			getComponent(OFFSET+UPDATE).setEnabled(can_test);
			getComponent(OFFSET+BLACK_BOX).setVisible(host.state!=faulty);

			boolean can_unexport = (host.state==unknown);
			getComponent(OFFSET+UNEXPORT_STARTER).setVisible(can_unexport);
		}
		else
		//	if selection is collection
		if (collec_name!=null)
		{
			//	Add collec name in menu label title
			JLabel	lbl = (JLabel)getComponent(0);
			lbl.setText("  " + collec_name + "  :");

			//	Modify visibility
			getComponent(OFFSET+OPEN_PANEL).setEnabled(false);
			getComponent(OFFSET+DISABLE).setVisible(AstorUtil.getCtrlBtn());
			getComponent(OFFSET+ENABLE).setVisible(AstorUtil.getCtrlBtn());
			getComponent(OFFSET+DISABLE).setEnabled(true);
			getComponent(OFFSET+ENABLE).setEnabled(true);
			getComponent(OFFSET+REM_LOGIN).setVisible(false);
			getComponent(OFFSET+STARTER_INFO).setVisible(false);
			getComponent(OFFSET+STARTER_TEST).setVisible(false);
			getComponent(OFFSET+STARTER_LOGS).setVisible(false);
			getComponent(OFFSET+UPTIME_SERVERS).setVisible(false);
			getComponent(OFFSET+UPDATE).setVisible(false);

			getComponent(OFFSET+CLONE_HOST).setEnabled(false);
			getComponent(OFFSET+CHANGE_BRANCHE).setEnabled(false);
			getComponent(OFFSET+EDIT_PROP).setEnabled(false);
			getComponent(OFFSET+REMOVE_HOST).setEnabled(false);
			getComponent(OFFSET+BLACK_BOX).setVisible(false);
			getComponent(OFFSET+UNEXPORT_STARTER).setVisible(false);

			getComponent(OFFSET+RESET_STAT).setVisible(AstorUtil.getInstance().isSuperTango());
        }

		//	Do not do it if Windows
		//---------------------------------------
		if (!AstorUtil.osIsUnix())
			getComponent(OFFSET+REM_LOGIN).setVisible(false);

		show(parent, evt.getX(), evt.getY());
	}
	//===============================================================
	//===============================================================
	private void hostActionPerformed(ActionEvent evt)
	{
		//	Check component source
		Object	obj = evt.getSource();
		int     cmdidx = 0;
		for (int i=0 ; i<menuLabels.length ; i++)
			if (getComponent(OFFSET+i)==obj)
				cmdidx = i;

		switch (cmdidx)
		{
		case OPEN_PANEL:
			parent.displayHostInfo();
			break;
		case UNEXPORT_STARTER:
			host.unexportStarter(astor);
			break;
		case STARTER_TEST:
			host.testStarter(astor);
			break;
		case STARTER_LOGS:
			host.displayLogging(astor);
			break;
		case UPTIME_SERVERS:
			host.displayUptimes(astor);
			break;
		case STARTER_INFO:
			host.displayInfo(parent);
			break;
		case REM_LOGIN:
			new RemoteLoginThread(host.getName(), parent).start();
			break;
		case ENABLE:
			enablePolling(true);
			break;
		case DISABLE:
			enablePolling(false);
			break;
		case CLONE_HOST:
			astor.addNewHost(host);
			break;
		case EDIT_PROP:
			astor.editHostProperties(host);
			break;
		case CHANGE_BRANCHE:
			parent.moveNode();
			break;
		case REMOVE_HOST:
			astor.removeHost(host.getName());
			break;
		case BLACK_BOX:
			host.displayBlackBox(astor);
			break;
		case COLLEC_INFO:
			parent.displayBranchInfo();
			break;
		case START_SERVERS:
			new ServerCmdThread(astor, collec_hosts, StartAllServers).start();
			break;
		case STOP_SERVERS:
			new ServerCmdThread(astor, collec_hosts, StopAllServers).start();
			break;
		case RESET_STAT:
			parent.resetCollectionStatistics();
			break;
		case UPDATE:
			host.updateServersList(astor);
			break;
		case CHANGE_NAME:
			parent.changeNodeName();
			break;
		}
	}

	//===============================================================
	//===============================================================
	private void enablePolling(boolean enable)
	{
		if (host!=null)
		{
			host.do_polling = enable;
			if (!enable)
				parent.hostDialogs.close(host);
		}
		else
			for (TangoHost host : collec_hosts)
			{
				host.do_polling = enable;
				if (!enable)
					parent.hostDialogs.close(host);
			}
	}
	//===============================================================
	//===============================================================
}

