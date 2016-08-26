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


package admin.astor.access;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class AllowedCmdTreePopupMenu extends JPopupMenu {
    private AllowedCmdTree parent;
    static private final int ADD_CLASS = 0;
    static private final int ADD_CMD = 1;
    static private final int REMOVE_CMD = 2;

    static private String[] menuLabels = {
            "Add Class for Allowed Commands",
            "Add Allowed Command",
            "Remove",
    };


    static private final int OFFSET = 2;        //	Label And separator
    private JLabel title;

    //===============================================================
    //===============================================================
    public AllowedCmdTreePopupMenu(AllowedCmdTree parent) {
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
        title = new JLabel();
        title.setFont(new java.awt.Font("Dialog", 1, 16));
        add(title);
        add(new JPopupMenu.Separator());

        for (String menuLabel : menuLabels) {
            if (menuLabel == null)
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

    /**
     * Sho menu on root
     */
    //======================================================
    public void showMenu(MouseEvent evt, String name) {
        //	Set selection at mouse position
        TreePath selectedPath =
                parent.getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath == null)
            return;
        parent.setSelectionPath(selectedPath);

        title.setText(name);

        //	Reset all items
        for (int i = 0; i < menuLabels.length; i++)
            getComponent(OFFSET + i).setVisible(false);

        getComponent(OFFSET + ADD_CLASS).setVisible(true);
        show(parent, evt.getX(), evt.getY());
    }
    //======================================================

    /**
     * Sho menu on Command
     */
    //======================================================
    public void showMenu(MouseEvent evt, String parent_name, String name) {
        //	Set selection at mouse position
        TreePath selectedPath =
                parent.getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath == null)
            return;
        parent.setSelectionPath(selectedPath);

        title.setText(parent_name);

        //	Reset all items
        for (int i = 0; i < menuLabels.length; i++)
            getComponent(OFFSET + i).setVisible(false);

        getComponent(OFFSET + REMOVE_CMD).setVisible(true);
        ((JMenuItem) getComponent(OFFSET + REMOVE_CMD)).setText(
                menuLabels[REMOVE_CMD] + "   " + name);
        show(parent, evt.getX(), evt.getY());
    }
    //======================================================

    /**
     * Sho menu on Class
     */
    //======================================================
    public void showMenu(MouseEvent evt, ClassAllowed class_allowed) {
        //	Set selection at mouse position
        TreePath selectedPath =
                parent.getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath == null)
            return;
        parent.setSelectionPath(selectedPath);

        title.setText(class_allowed.toString());

        //	Reset all items
        for (int i = 0; i < menuLabels.length; i++)
            getComponent(OFFSET + i).setVisible(false);

        //  And set visible for used items
        getComponent(OFFSET + ADD_CMD).setVisible(true);
        show(parent, evt.getX(), evt.getY());
    }

    //===============================================================
    //===============================================================
    private void hostActionPerformed(ActionEvent evt) {
        //	Check component source
        Object obj = evt.getSource();
        int commandIndex = 0;
        for (int i = 0; i < menuLabels.length; i++)
            if (getComponent(OFFSET + i) == obj)
                commandIndex = i;

        switch (commandIndex) {
            case ADD_CLASS:
                parent.addClass();
                break;
            case ADD_CMD:
                parent.addCommand();
                break;
            case REMOVE_CMD:
                parent.removeCommand();
                break;
        }
    }
    //======================================================
    //======================================================
}

