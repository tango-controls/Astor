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

import admin.astor.statistics.StarterStatTable;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class TreePopupMenu extends JPopupMenu implements AstorDefs {
    private Astor astor;
    private AstorTree parent;
    private TangoHost host;
    private String collec_name;
    private TangoHost[] collec_hosts;

    static private String[] menuLabels = {
            "Open control Panel",
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
            "Starter Statistics",
            "Uptime for Servers",
            "Force Update",
            "Change Name",
    };

    static private final int OFFSET = 2;        //	Label And separator
    static private final int OPEN_PANEL = 0;

    //	Host menu specific
    static private final int REM_LOGIN = 1;
    static private final int STARTER_INFO = 2;
    static private final int STARTER_TEST = 3;
    static private final int UNEXPORT_STARTER = 4;

    //	Collection menu specific
    static private final int COLLEC_INFO = 5;
    static private final int START_SERVERS = 6;
    static private final int STOP_SERVERS = 7;
    static private final int RESET_STAT = 8;

    //	Edit options
    static private final int CLONE_HOST = 9;
    static private final int CHANGE_BRANCHE = 10;
    static private final int EDIT_PROP = 11;
    static private final int REMOVE_HOST = 12;
    static private final int BLACK_BOX = 13;
    static private final int STARTER_LOGS = 14;
    static private final int STARTER_STAT = 15;
    static private final int UPTIME_SERVERS = 16;
    static private final int UPDATE = 17;
    static private final int CHANGE_NAME = 18;

    //===============================================================
    //===============================================================
    public TreePopupMenu(Astor astor, AstorTree parent) {
        super();
        this.astor = astor;
        this.parent = parent;

        buildBtnPopupMenu();
    }

    //===============================================================
    //===============================================================
    public TreePopupMenu(AstorTree parent) {
        super();
        this.astor = null;
        this.parent = parent;

        buildBtnPopupMenu();
    }
    //===============================================================

    /**
     * Create a Popup menu for host control
     */
    //===============================================================
    private void buildBtnPopupMenu() {
        JLabel title = new JLabel("Host Control :");
        title.setFont(new java.awt.Font("Dialog", 1, 16));
        add(title);
        add(new JPopupMenu.Separator());

        for (String menuLabel : menuLabels) {
            JMenuItem btn = new JMenuItem(menuLabel);
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    hostActionPerformed(evt);
                }
            });
            add(btn);
        }
    }

    //======================================================
    //======================================================
    private boolean getSelectedObject() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                parent.getLastSelectedPathComponent();
        if (node == null)
            return false;

        Object obj = node.getUserObject();

        if (obj instanceof DbaseObject)
            return false;

        //	Check if a Tango Host
        if (obj instanceof TangoHost) {
            host = (TangoHost) obj;
            collec_name = null;
        } else {
            host = null;
            //	Get collection children
            int nb = node.getChildCount();
            collec_hosts = new TangoHost[nb];
            for (int i = 0; i < nb; i++) {
                node = node.getNextNode();
                //	Check if it is really a TangoHost object
                Object o = node.getUserObject();
                if (o instanceof TangoHost)
                    collec_hosts[i] = (TangoHost) o;
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
    public void showMenu(MouseEvent evt) {
        //	Set selection at mouse position
        TreePath selectedPath =
                parent.getPathForLocation(evt.getX(), evt.getY());

        if (selectedPath == null)
            return;
        parent.setSelectionPath(selectedPath);

        //	Get the selected object
        if (!getSelectedObject())
            return;

        //	Set all item viesible
        for (int i = OFFSET ; i < getComponentCount(); i++)
            getComponent(i).setVisible(true);

        //	if selection is host
        if (host != null) {
            //	Add host name in menu label title
            JLabel lbl = (JLabel) getComponent(0);
            lbl.setText("  " + host.getName() + "  :");

            getComponent(OFFSET + COLLEC_INFO).setVisible(false);
            getComponent(OFFSET + START_SERVERS).setVisible(false);
            getComponent(OFFSET + STOP_SERVERS).setVisible(false);
            getComponent(OFFSET + RESET_STAT).setVisible(false);
            getComponent(OFFSET + CHANGE_NAME).setVisible(false);

            getComponent(OFFSET + CHANGE_BRANCHE).setEnabled(true);
            getComponent(OFFSET + EDIT_PROP).setEnabled(true);
            getComponent(OFFSET + CLONE_HOST).setEnabled(true);

            boolean can_test = (host.state == all_ok ||
                    host.state == alarm || host.state == moving);
            getComponent(OFFSET + STARTER_TEST).setEnabled(can_test);
            getComponent(OFFSET + STARTER_LOGS).setEnabled(can_test);
            getComponent(OFFSET + STARTER_STAT).setEnabled(can_test);
            getComponent(OFFSET + UPTIME_SERVERS).setVisible(true);
            getComponent(OFFSET + REMOVE_HOST).setEnabled(!can_test);
            getComponent(OFFSET + UPDATE).setEnabled(can_test);
            getComponent(OFFSET + BLACK_BOX).setVisible(host.state != faulty);

            boolean can_unexport = (host.state == unknown);
            getComponent(OFFSET + UNEXPORT_STARTER).setVisible(can_unexport);
        } else
            //	if selection is collection
            if (collec_name != null) {
                //	Add collec name in menu label title
                JLabel lbl = (JLabel) getComponent(0);
                lbl.setText("  " + collec_name + "  :");

                //	Modify visibility
                getComponent(OFFSET + OPEN_PANEL).setEnabled(false);
                getComponent(OFFSET + REM_LOGIN).setVisible(false);
                getComponent(OFFSET + STARTER_INFO).setVisible(false);
                getComponent(OFFSET + STARTER_TEST).setVisible(false);
                getComponent(OFFSET + STARTER_LOGS).setVisible(false);
                getComponent(OFFSET + STARTER_STAT).setVisible(false);
                getComponent(OFFSET + UPTIME_SERVERS).setVisible(false);
                getComponent(OFFSET + UPDATE).setVisible(false);

                getComponent(OFFSET + CLONE_HOST).setEnabled(false);
                getComponent(OFFSET + CHANGE_BRANCHE).setEnabled(false);
                getComponent(OFFSET + EDIT_PROP).setEnabled(false);
                getComponent(OFFSET + REMOVE_HOST).setEnabled(false);
                getComponent(OFFSET + BLACK_BOX).setVisible(false);
                getComponent(OFFSET + UNEXPORT_STARTER).setVisible(false);

                getComponent(OFFSET + RESET_STAT).setVisible(AstorUtil.getInstance().isSuperTango());
            }

        //	Do not do it if Windows
        //---------------------------------------
        if (!AstorUtil.osIsUnix())
            getComponent(OFFSET + REM_LOGIN).setVisible(false);

        show(parent, evt.getX(), evt.getY());
    }

    //===============================================================
    //===============================================================
    private void hostActionPerformed(ActionEvent evt) {
        //	Check component source
        Object obj = evt.getSource();
        int cmdidx = 0;
        for (int i = 0; i < menuLabels.length; i++)
            if (getComponent(OFFSET + i) == obj)
                cmdidx = i;

        switch (cmdidx) {
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
            case STARTER_STAT:
                try {
                    new StarterStatTable(astor, host.hostName()).setVisible(true);
                } catch (DevFailed e) {
                    ErrorPane.showErrorMessage(astor, null, e);
                }
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
}

