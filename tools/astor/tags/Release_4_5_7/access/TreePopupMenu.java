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

package admin.astor.access;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

public class TreePopupMenu extends JPopupMenu
{
	private UsersTree  	parent;
    static private int  x = 0;
    static private final int	COPY          = x++;
	static private final int	PASTE         = x++;
	static private final int	ADD_ITEM      = x++;
    static private final int	EDIT          = x++;
    static private final int	CLONE         = x++;
	static private final int	REMOVE        = x++;
	static private final int	TOGGLE_RIGHTS = x++;

	static private String[]	menuLabels = {
                                    "Copy",
                                    "Paste",
                                    "Add Item",
                                    "Edit",
                                    "Clone",
                                    "Remove",
                                    "Toggle rights"
                            };


	static private final int	OFFSET       = 2;		//	Label And separator
    private JLabel	title;
 	//===============================================================
	//===============================================================
	public TreePopupMenu(UsersTree parent)
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
        case UsersTree.USER :
            getComponent(OFFSET+CLONE).setVisible(true);
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
	private void hostActionPerformed(ActionEvent evt)
	{
 		String  cmd = evt.getActionCommand();

        if (cmd.equals(menuLabels[COPY]))
            parent.copyItem();
		else
        if (cmd.equals(menuLabels[PASTE]))
            parent.pasteItem();
		else
        if (cmd.equals(menuLabels[ADD_ITEM]))
            parent.addItem();
		else
        if (cmd.equals(menuLabels[EDIT]))
            parent.editItem();
		else
        if (cmd.equals(menuLabels[CLONE]))
            parent.cloneUser();
		else
        if (cmd.equals(menuLabels[REMOVE]))
            parent.removeItem();
		else
        if (cmd.equals(menuLabels[TOGGLE_RIGHTS]))
            parent.toggleRight();
	}
    //======================================================
    //======================================================
}

