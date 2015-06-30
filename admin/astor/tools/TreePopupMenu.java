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


package admin.astor.tools;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class TreePopupMenu extends JPopupMenu {
    private DevBrowserTree parent;
    static private final int CHANGE = 0;
    static private final int PERIODIC = 1;
    static private final int ARCHIVE = 2;

    static public final int MODE_DEVICE = 0;
    static public final int MODE_ATTR = 1;
    static public final int MODE_SERVER = 2;

    static private String[] attLabels = {
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

    static private String[] devLabels = {
            "Test Device",
            "MonitorDevice",
            "Host Panel",
            "Manage Polling",
            "Polling Profiler",
            "Go To Server Node",
    };

    static private String[] servLabels = {
            "Test Admin Device",
            "Host Panel",
            "Server Architecture",
            "Polling Profiler",
    };

    static private final int OFFSET = 2;        //	Label And separator
    static private final int ATT_POLLING = 0;

    static private final int ATT_ADD_CHANGE = 2;
    static private final int ATT_ADD_PERIODIC = 3;
    static private final int ATT_ADD_ARCHIVE = 4;

    static private final int ATT_ED_CHANGE = 7;
    static private final int ATT_ED_PERIODIC = 8;
    static private final int ATT_ED_ARCHIVE = 9;

    static private final int DEV_TEST = 0;
    static private final int DEV_MONITOR = 1;
    static private final int DEV_HOST_PANEL = 2;
    static private final int DEV_POLLING = 3;
    static private final int DEV_PROFILER = 4;
    static private final int DEV_GOTO_SERVER = 5;

    static private final int SERV_TEST = 0;
    static private final int SERV_HOST_PANEL = 1;
    static private final int SERV_ARCHI = 2;
    static private final int SERV_PROFILER = 3;

    private int mode;
    private JLabel title;

    //===============================================================
    //===============================================================
    public TreePopupMenu(DevBrowserTree parent, int mode) {
        super();
        this.parent = parent;
        this.mode = mode;

        buildBtnPopupMenu();
    }
    //===============================================================

    /**
     * Create a Popup menu for host control
     */
    //===============================================================
    private void buildBtnPopupMenu() {
        title = new JLabel("Attribute :");
        title.setFont(new Font("Dialog", Font.BOLD, 16));
        add(title);
        add(new JPopupMenu.Separator());
        String[] menuLabels;
        if (mode == MODE_ATTR)
            menuLabels = attLabels;
        else if (mode == MODE_DEVICE)
            menuLabels = devLabels;
        else
            menuLabels = servLabels;

        for (String menuLabel : menuLabels) {
            if (menuLabel==null)
                add(new Separator());
            else {
                JMenuItem btn = new JMenuItem(menuLabel);
                btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        hostActionPerformed(evt);
                    }
                });
                add(btn);
            }
        }
    }

    //======================================================
    //======================================================
    public void showMenu(MouseEvent evt) {
        showMenu(evt, false, true);
    }

    //======================================================
    //======================================================
    public void showMenu(MouseEvent evt, boolean obj_has_polling, boolean running) {
        //	Set selection at mouse position
        TreePath selectedPath =
                parent.getPathForLocation(evt.getX(), evt.getY());

        if (selectedPath == null)
            return;
        parent.setSelectionPath(selectedPath);
        if (mode == MODE_ATTR)
            title.setText("Attribute: " + parent.getSelectedName());
        else if (mode == MODE_DEVICE) {
            title.setText("Device: " + parent.getSelectedName());
            String collection = parent.getCollection();
            getComponent(OFFSET + DEV_PROFILER).setEnabled(obj_has_polling);
            getComponent(OFFSET + DEV_GOTO_SERVER).setVisible(collection.equals("Devices") ||
                    collection.equals("Aliases"));
            if (!running) {
                getComponent(OFFSET).setEnabled(false);
                getComponent(OFFSET + DEV_MONITOR).setEnabled(false);
                getComponent(OFFSET + DEV_POLLING).setEnabled(false);
                getComponent(OFFSET + DEV_PROFILER).setEnabled(false);
            }
        } else if (mode == MODE_SERVER) {
            title.setText("Server: " + parent.getSelectedName());
            getComponent(OFFSET + SERV_PROFILER).setEnabled(obj_has_polling);
            if (!running) {
                getComponent(OFFSET + SERV_PROFILER).setEnabled(false);
                getComponent(OFFSET + SERV_ARCHI).setEnabled(false);
                getComponent(SERV_TEST).setEnabled(false);
            }
        }

        show(parent, evt.getX(), evt.getY());
    }

    //===============================================================
    //===============================================================
    private void hostActionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        if (mode == MODE_ATTR) {
            if (cmd.equals(attLabels[ATT_POLLING]))
                parent.managePolling();
            else if (cmd.equals(attLabels[ATT_ADD_CHANGE]))
                parent.add(CHANGE);
            else if (cmd.equals(attLabels[ATT_ADD_PERIODIC]))
                parent.add(PERIODIC);
            else if (cmd.equals(attLabels[ATT_ADD_ARCHIVE]))
                parent.add(ARCHIVE);
            else if (cmd.equals(attLabels[ATT_ED_CHANGE]))
                parent.editProperties(CHANGE);
            else if (cmd.equals(attLabels[ATT_ED_PERIODIC]))
                parent.editProperties(PERIODIC);
            else if (cmd.equals(attLabels[ATT_ED_ARCHIVE]))
                parent.editProperties(ARCHIVE);
        } else
        if (mode == MODE_DEVICE) {
            if (cmd.equals(devLabels[DEV_TEST]))
                parent.deviceTest();
            else if (cmd.equals(devLabels[DEV_MONITOR]))
                parent.deviceMonitor();
            else if (cmd.equals(devLabels[DEV_HOST_PANEL]))
                parent.displayHostPanel();
            else if (cmd.equals(devLabels[DEV_POLLING]))
                parent.managePolling();
            else if (cmd.startsWith(devLabels[DEV_PROFILER]))
                parent.showProfiler();
            else if (cmd.startsWith(devLabels[DEV_GOTO_SERVER]))
                parent.gotoServer();
        } else
        if (mode == MODE_SERVER) {
            if (cmd.equals(servLabels[SERV_TEST]))
                parent.deviceTest();
            else if (cmd.equals(servLabels[SERV_HOST_PANEL]))
                parent.displayHostPanel();
            else if (cmd.equals(servLabels[SERV_ARCHI]))
                parent.serverArchitecture();
            else if (cmd.equals(servLabels[SERV_PROFILER]))
                parent.showProfiler();
        }
    }
    //======================================================
    //======================================================
}

