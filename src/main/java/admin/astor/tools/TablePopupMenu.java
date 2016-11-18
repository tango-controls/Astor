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


//import admin.astor.tools.SubscribedSignal;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


//===============================================================

/**
 * A class to display a popup menu.
 */
//===============================================================
public class TablePopupMenu extends JPopupMenu {

    private EventsTable parent;
    private JLabel titleLbl;
    /**
     * Popup menu to be used on right button clicked.
     */
    static private String[] menuLabels = {
            "Signal Info",
            "Signal History",
            "Remove",
            "Move Up",
            "Move Down"
    };
    static private final int OFFSET = 2;        //	Label And separator
    static private final int INFO = 0;
    static private final int HISTORY = 1;
    static private final int REMOVE = 2;
    static private final int MOVE_UP = 3;
    static private final int MOVE_DOWN = 4;

    static public final int UP = 0;
    static public final int DOWN = 1;
    //==========================================================
    /**
     * Class Constructor
     */
    //==========================================================
    public TablePopupMenu(EventsTable parent) {
        super();
        this.parent = parent;
        buildBtnPopupMenu();
    }
    //===============================================================

    /**
     * Create a Popup menu for host control
     */
    //===============================================================
    private void buildBtnPopupMenu() {
        titleLbl = new JLabel();
        titleLbl.setFont(new java.awt.Font("Dialog", 1, 16));
        add(titleLbl);
        add(new JPopupMenu.Separator());
        for (String label : menuLabels) {
            JMenuItem btn = new JMenuItem(label);
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    executeActionPerformed(evt);
                }
            });
            add(btn);
        }
    }
    //======================================================
    /**
     * Manage event on clicked mouse on PogoTree object.
     */
    //======================================================
    private SubscribedSignal signal;

    public void showMenu(java.awt.event.MouseEvent evt, SubscribedSignal signal) {
        this.signal = signal;
        getComponent(OFFSET + HISTORY).setVisible(signal.histo.size() > 0);
        titleLbl.setText(signal.toString());

        show(parent.table, evt.getX(), evt.getY());
    }
    //======================================================

    /**
     * Called when popup menu item selected
     */
    //======================================================
    private void executeActionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();

        if (cmd.equals(menuLabels[INFO]))
            parent.displayInfo(signal);
        else if (cmd.equals(menuLabels[HISTORY]))
            parent.displayHistory(signal);
        else if (cmd.equals(menuLabels[REMOVE])) {
            if (JOptionPane.showConfirmDialog(parent,
                    "OK to remove " + signal + " ?",
                    "Confim Dialog",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                //	Remove signal
                parent.remove(signal);
            }
        } else if (cmd.equals(menuLabels[MOVE_UP]))
            parent.move(signal, UP);
        else if (cmd.equals(menuLabels[MOVE_DOWN]))
            parent.move(signal, DOWN);
    }
}
