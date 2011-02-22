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
// Revision 1.1  2008/11/19 10:01:34  pascal_verdier
// New tests done on Access control.
// Allowed commands tree added.
//
// Revision 1.1  2006/09/19 13:06:47  pascal_verdier
// Access control manager added.
//
//
// Copyleft 2006 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor.access;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class UsersTreePopupMenu extends JPopupMenu
{
	private UsersTree  	parent;
    static private final int	ADD_USER      = 0;
    static private final int	COPY          = 1;
	static private final int	PASTE         = 2;
	static private final int	ADD_ITEM      = 3;
    static private final int	EDIT          = 4;
    static private final int	CLONE         = 5;
	static private final int	REMOVE        = 6;
	static private final int	TOGGLE_RIGHTS = 7;

	static private String[]	menuLabels = {
                                    "Add User",
                                    "Copy",
                                    "Paste",
                                    "Add Item",
                                    "Edit",
                                    "Clone",
                                    "Remove",
                                    "Toggle rights",
                            };


	static private final int	OFFSET       = 2;		//	Label And separator
    private JLabel	title;
 	//===============================================================
	//===============================================================
	public UsersTreePopupMenu(UsersTree parent)
	{
		super();
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
     * @param evt the mouse event
     * @param name TANGO_HOST name
     */
	//======================================================
	public void showMenu(MouseEvent evt, String name)
	{
		//	Set selection at mouse position
		TreePath	selectedPath =
			parent.getPathForLocation(evt.getX(), evt.getY());
		if (selectedPath==null)
			return;
		parent.setSelectionPath(selectedPath);

        title.setText(name);

        //	Reset all items
		for (int i=0 ; i<menuLabels.length ; i++)
			getComponent(OFFSET+i).setVisible(false);

        //  And set visible for used items
        getComponent(OFFSET /*+ADD_USER*/).setVisible(true);
 		show(parent, evt.getX(), evt.getY());
	}
	//======================================================
	//======================================================
	public void showMenu(MouseEvent evt, int type, Object obj)
	{
		//	Set selection at mouse position
		TreePath	selectedPath =
			parent.getPathForLocation(evt.getX(), evt.getY());
		if (selectedPath==null)
			return;
		parent.setSelectionPath(selectedPath);

        title.setText(obj.toString());

        //	Reset all items
		for (int i=0 ; i<menuLabels.length ; i++)
			getComponent(OFFSET+i).setVisible(false);

        //  And set visible for used items
        switch(type)
        {
        case UsersTree.USER_NODE :
            getComponent(OFFSET+CLONE).setVisible(true);
			if (!obj.toString().equals("All Users"))
	            getComponent(OFFSET+REMOVE).setVisible(true);
            break;
        case UsersTree.COLLECTION :
            getComponent(OFFSET+ADD_ITEM).setVisible(true);
            getComponent(OFFSET+PASTE).setVisible(true);
            getComponent(OFFSET+PASTE).setEnabled(false);
            if (obj.toString().equals(UsersTree.collecStr[UsersTree.ADDRESS]))
                if (parent.copied_addresses.size()>0)
                    getComponent(OFFSET+PASTE).setEnabled(true);
            if (obj.toString().equals(UsersTree.collecStr[UsersTree.DEVICE]))
                if (parent.copied_devices.size()>0)
                    getComponent(OFFSET+PASTE).setEnabled(true);
            break;
 		case UsersTree.ADDRESS:
			getComponent(OFFSET+COPY).setVisible(true);
			getComponent(OFFSET+EDIT).setVisible(true);
			getComponent(OFFSET+REMOVE).setVisible(true);
            break;
		case UsersTree.DEVICE:
			getComponent(OFFSET+COPY).setVisible(true);
			getComponent(OFFSET+EDIT).setVisible(true);
			getComponent(OFFSET+REMOVE).setVisible(true);
			getComponent(OFFSET+TOGGLE_RIGHTS).setVisible(true);
            break;
        }
 		show(parent, evt.getX(), evt.getY());
	}
	//===============================================================
	//===============================================================
	private void userActionPerformed(ActionEvent evt)
	{
		//	Check component source
		Object	obj = evt.getSource();
        int     cmdidx = 0;
        for (int i=0 ; i<menuLabels.length ; i++)
            if (getComponent(OFFSET+i)==obj)
                cmdidx = i;

		switch (cmdidx)
		{
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
		case TOGGLE_RIGHTS:
			parent.toggleRight();
			break;
		}
	}
    //======================================================
    //======================================================
}

