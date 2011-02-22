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
import java.awt.event.MouseEvent;

public class AllowedCmdTreePopupMenu extends JPopupMenu
{
	private AllowedCmdTree  	parent;
	static private final int	ADD_CLASS     = 0;
	static private final int	ADD_CMD       = 1;
	static private final int	REMOVE_CMD    = 2;

	static private String[]	menuLabels = {
                                    "Add Class for Allowed Commands",
                                    "Add Allowed Command",
                                    "Remove",
                            };


	static private final int	OFFSET       = 2;		//	Label And separator
    private JLabel	title;
 	//===============================================================
	//===============================================================
	public AllowedCmdTreePopupMenu(AllowedCmdTree parent)
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
	/**
	 *	Sho menu on root
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

		getComponent(OFFSET+ADD_CLASS).setVisible(true);
 		show(parent, evt.getX(), evt.getY());
	}
	//======================================================
	/**
	 *	Sho menu on Command
	 */
	//======================================================
	public void showMenu(MouseEvent evt, String parent_name, String name)
	{
		//	Set selection at mouse position
		TreePath	selectedPath =
			parent.getPathForLocation(evt.getX(), evt.getY());
		if (selectedPath==null)
			return;
		parent.setSelectionPath(selectedPath);

        title.setText(parent_name);

        //	Reset all items
		for (int i=0 ; i<menuLabels.length ; i++)
			getComponent(OFFSET+i).setVisible(false);

		getComponent(OFFSET+REMOVE_CMD).setVisible(true);
		((JMenuItem)getComponent(OFFSET+REMOVE_CMD)).setText(
					menuLabels[REMOVE_CMD] + "   " + name);
 		show(parent, evt.getX(), evt.getY());
	}
	//======================================================
	/**
	 *	Sho menu on Class
	 */
	//======================================================
	public void showMenu(MouseEvent evt, ClassAllowed class_allowed)
	{
		//	Set selection at mouse position
		TreePath	selectedPath =
			parent.getPathForLocation(evt.getX(), evt.getY());
		if (selectedPath==null)
			return;
		parent.setSelectionPath(selectedPath);

        title.setText(class_allowed.toString());

        //	Reset all items
		for (int i=0 ; i<menuLabels.length ; i++)
			getComponent(OFFSET+i).setVisible(false);

        //  And set visible for used items
		getComponent(OFFSET+ADD_CMD).setVisible(true);
 		show(parent, evt.getX(), evt.getY());
	}
	//===============================================================
	//===============================================================
	private void hostActionPerformed(ActionEvent evt)
	{
 		String  cmd = evt.getActionCommand();
		//	Check component source
		Object	obj = evt.getSource();
        int     cmdidx = 0;
        for (int i=0 ; i<menuLabels.length ; i++)
            if (getComponent(OFFSET+i)==obj)
                cmdidx = i;

		switch (cmdidx)
		{
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

