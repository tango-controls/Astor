//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code a popup menu .
//
// $Author$
//
// $Revision$
//
// $Log$
//
// Copyleft 2006 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor.tools;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

public class TreePopupMenu extends JPopupMenu
{
	private DevBrowserTree  	parent;
    static private final int	CHANGE   = 0;
	static private final int	PERIODIC = 1;
	static private final int	ARCHIVE  = 2;

    static public final int	MODE_DEVICE = 0;
    static public final int	MODE_ATTR   = 1;
    static public final int	MODE_SERVER = 2;

	static private String[]	attLabels = {
                                    "Manage Polling",
                                    null,
									"Subscribe on Change  Event",
									"Subscribe on Periodic Event",
									"Subscribe on Archive  Event",
									null,
									null,
									"Edit Change  Event Properties",
									"Edit Periodic Event Properties",
									"Edit Archive  Event Properties",
    };

    static private String[]	devLabels = {
                                    "Test Device",
                                    "MonitorDevice",
                                    "Host Panel",
                                    "Manage Polling",
                                    "Polling Profiler",
                                    "Go To Server Node",
  									};

    static private String[]	servLabels = {
                                    "Test Admin Device",
                                    "Host Panel",
                                    "Server Architecture",
                                    "Polling Profiler",
 									};

	static private final int	OFFSET       = 2;		//	Label And separator
    static private final int	ATT_POLLING      = 0;

    static private final int	ATT_ADD_CHANGE   = 2;
	static private final int	ATT_ADD_PERIODIC = 3;
	static private final int	ATT_ADD_ARCHIVE  = 4;

	static private final int	ATT_ED_CHANGE    = 7;
	static private final int	ATT_ED_PERIODIC  = 8;
	static private final int	ATT_ED_ARCHIVE   = 9;

    static private final int	DEV_TEST         = 0;
    static private final int	DEV_MONITOR      = 1;
    static private final int	DEV_HOST_PANEL   = 2;
    static private final int	DEV_POLLING      = 3;
    static private final int	DEV_PROFILER     = 4;
    static private final int	DEV_GOTO_SERVER  = 5;

    static private final int	SERV_TEST        = 0;
    static private final int	SERV_HOST_PANEL  = 1;
    static private final int	SERV_ARCHI       = 2;
    static private final int	SERV_PROFILER       = 3;

    private int mode;
    private JLabel	title;
 	//===============================================================
	//===============================================================
	public TreePopupMenu(DevBrowserTree parent, int mode)
	{
		super();
		this.parent = parent;
        this.mode   = mode;

		buildBtnPopupMenu();
	}
	//===============================================================
	/**
	 *	Create a Popup menu for host control
	 */
	//===============================================================
	private void buildBtnPopupMenu()
	{
		title = new JLabel("Attribute :");
        title.setFont(new java.awt.Font("Dialog", 1, 16));
		add(title);
		add(new JPopupMenu.Separator());
        String[]    menuLabels;
        if (mode==MODE_ATTR)
            menuLabels = attLabels;
        else
        if (mode==MODE_DEVICE)
            menuLabels = devLabels;
        else
            menuLabels = servLabels;

		for (int i=0 ; i<menuLabels.length ; i++)
		{
			if (menuLabels[i]==null)
				add(new JPopupMenu.Separator());
			else
			{
				JMenuItem	btn = new JMenuItem(menuLabels[i]);
   				btn.addActionListener (new java.awt.event.ActionListener () {
					public void actionPerformed (ActionEvent evt) {
            			hostActionPerformed(evt);
					}
				});
				add(btn);
			}
		}
	}
	//======================================================
	//======================================================
	public void showMenu(MouseEvent evt)
	{
        showMenu(evt, false);
    }
	//======================================================
	//======================================================
	public void showMenu(MouseEvent evt, boolean obj_has_polling)
	{
		//	Set selection at mouse position
		TreePath	selectedPath =
			parent.getPathForLocation(evt.getX(), evt.getY());

		if (selectedPath==null)
			return;
		parent.setSelectionPath(selectedPath);
        if (mode==MODE_ATTR)
            title.setText("Attribute: " + parent.getSelectedName());
        else
        if (mode==MODE_DEVICE)
        {
            title.setText("Device: " +  parent.getSelectedName());
            String  collec = parent.getCollection();
            getComponent(OFFSET+DEV_PROFILER).setEnabled(obj_has_polling);
            getComponent(OFFSET+DEV_GOTO_SERVER).setVisible(collec.equals("Devices") ||
                                                            collec.equals("Aliases"));
        }
        else
        if (mode==MODE_SERVER)
        {
            title.setText("Server: " +  parent.getSelectedName());
            getComponent(OFFSET+SERV_PROFILER).setEnabled(obj_has_polling);
        }

 		show(parent, evt.getX(), evt.getY());
	}
	//===============================================================
	//===============================================================
	private void hostActionPerformed(ActionEvent evt)
	{
 		String  cmd = evt.getActionCommand();
        if (mode==MODE_ATTR)
        {
            if (cmd.equals(attLabels[ATT_POLLING]))
                parent.managePolling();
            else
            if (cmd.equals(attLabels[ATT_ADD_CHANGE]))
                parent.add(CHANGE);
            else
            if (cmd.equals(attLabels[ATT_ADD_PERIODIC]))
                parent.add(PERIODIC);
            else
            if (cmd.equals(attLabels[ATT_ADD_ARCHIVE]))
                parent.add(ARCHIVE);
            else
            if (cmd.equals(attLabels[ATT_ED_CHANGE]))
                parent.editProperties(CHANGE);
            else
            if (cmd.equals(attLabels[ATT_ED_PERIODIC]))
                parent.editProperties(PERIODIC);
            else
            if (cmd.equals(attLabels[ATT_ED_ARCHIVE]))
                parent.editProperties(ARCHIVE);
        }
        else
        if (mode==MODE_DEVICE)
        {
            if (cmd.equals(devLabels[DEV_TEST]))
                parent.deviceTest();
            else
            if (cmd.equals(devLabels[DEV_MONITOR]))
                parent.deviceMonitor();
            else
            if (cmd.equals(devLabels[DEV_HOST_PANEL]))
                parent.displayHostPanel();
            else
            if (cmd.equals(devLabels[DEV_POLLING]))
                parent.managePolling();
            else
            if (cmd.startsWith(devLabels[DEV_PROFILER]))
                parent.showProfiler();
            else
            if (cmd.startsWith(devLabels[DEV_GOTO_SERVER]))
                parent.gotoServer();
        }
        else
        if (mode==MODE_SERVER)
        {
            if (cmd.equals(servLabels[SERV_TEST]))
                parent.deviceTest();
            else
            if (cmd.equals(servLabels[SERV_HOST_PANEL]))
                parent.displayHostPanel();
            else
            if (cmd.equals(servLabels[SERV_ARCHI]))
                parent.serverArchitecture();
            else
            if (cmd.equals(servLabels[SERV_PROFILER]))
                parent.showProfiler();
        }
	}
    //======================================================
    //======================================================
}

