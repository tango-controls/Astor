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

public class UsersTreePopupMenu extends JPopupMenu {
    private UsersTree parent;
    static private final int ADD_USER = 0;
    static private final int COPY = 1;
    static private final int PASTE = 2;
    static private final int ADD_ITEM = 3;
    static private final int EDIT = 4;
    static private final int CLONE = 5;
    static private final int REMOVE = 6;
    static private final int CHANGE_GROUP = 7;
    static private final int TOGGLE_RIGHTS = 8;

    static private String[] menuLabels = {
            "Add User",
            "Copy",
            "Paste",
            "Add Item",
            "Edit",
            "Clone",
            "Remove",
            "Change Group",
            "Toggle rights",
    };


    static private final int OFFSET = 2;        //	Label And separator
    private JLabel title;

    //===============================================================
    //===============================================================
    public UsersTreePopupMenu(UsersTree parent) {
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
                        userActionPerformed(evt);
                    }
                });
                add(btn);
            }
        }
    }
    //======================================================
    /**
     * Show menu on root
     *
     * @param evt  the mouse event
     * @param name TANGO_HOST name
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

        //  And set visible for used items
        getComponent(OFFSET /*+ADD_USER*/).setVisible(true);
        show(parent, evt.getX(), evt.getY());
    }

    //======================================================
    //======================================================
    public void showMenu(MouseEvent evt, int type, Object obj) {
        //	Set selection at mouse position
        TreePath selectedPath =
                parent.getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath == null)
            return;
        parent.setSelectionPath(selectedPath);

        title.setText(obj.toString());

        //	Reset all items
        for (int i = 0; i < menuLabels.length; i++)
            getComponent(OFFSET + i).setVisible(false);

        //  And set visible for used items
        switch (type) {
            case UsersTree.USER_NODE:
                getComponent(OFFSET + CLONE).setVisible(true);
                if (!obj.toString().equals("All Users"))
                    getComponent(OFFSET + REMOVE).setVisible(true);
                    getComponent(OFFSET + CHANGE_GROUP).setVisible(true);
                break;
            case UsersTree.COLLECTION:
                getComponent(OFFSET + ADD_ITEM).setVisible(true);
                getComponent(OFFSET + PASTE).setVisible(true);
                getComponent(OFFSET + PASTE).setEnabled(false);
                if (obj.toString().equals(UsersTree.collecStr[UsersTree.ADDRESS]))
                    if (parent.copied_addresses.size() > 0)
                        getComponent(OFFSET + PASTE).setEnabled(true);
                if (obj.toString().equals(UsersTree.collecStr[UsersTree.DEVICE]))
                    if (parent.copied_devices.size() > 0)
                        getComponent(OFFSET + PASTE).setEnabled(true);
                break;
            case UsersTree.ADDRESS:
                getComponent(OFFSET + COPY).setVisible(true);
                getComponent(OFFSET + EDIT).setVisible(true);
                getComponent(OFFSET + REMOVE).setVisible(true);
                break;
            case UsersTree.DEVICE:
                getComponent(OFFSET + COPY).setVisible(true);
                getComponent(OFFSET + EDIT).setVisible(true);
                getComponent(OFFSET + REMOVE).setVisible(true);
                getComponent(OFFSET + TOGGLE_RIGHTS).setVisible(true);
                break;
        }
        show(parent, evt.getX(), evt.getY());
    }

    //===============================================================
    //===============================================================
    private void userActionPerformed(ActionEvent evt) {
        //	Check component source
        Object obj = evt.getSource();
        int cmdidx = 0;
        for (int i = 0; i < menuLabels.length; i++)
            if (getComponent(OFFSET + i) == obj)
                cmdidx = i;

        switch (cmdidx) {
            case ADD_USER:
                parent.addUser();
                break;
            case COPY:
                parent.copyItem();
                break;
            case PASTE:
                parent.pasteItem();
                break;
            case ADD_ITEM:
                parent.addItem();
                break;
            case EDIT:
                parent.editItem();
                break;
            case CLONE:
                parent.cloneUser();
                break;
            case REMOVE:
                parent.removeItem();
                break;
            case CHANGE_GROUP:
                parent.changeGroup();
                break;
            case TOGGLE_RIGHTS:
                parent.toggleRight();
                break;
        }
    }
    //======================================================
    //======================================================
}

