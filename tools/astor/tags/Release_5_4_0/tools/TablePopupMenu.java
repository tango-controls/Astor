//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the Pogo class definition .
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

package	admin.astor.tools;



//import admin.astor.tools.SubscribedSignal;

import javax.swing.*;
import java.awt.event.ActionEvent;


//===============================================================
/**
 *	A class to display a popup menu.
 */
//===============================================================
public class TablePopupMenu extends JPopupMenu
{

	private EventsTable		parent;
	private JLabel		titleLbl;
	/**
	 *	Popup menu to be used on right button clicked.
	 */
	static private	 String[]	pMenuLabels = { 
										"Signal Info",
                                        "Signal History",
										"Remove",
										"Move Up",
                                        "Move Down"
									};
	static private final int	OFFSET = 2;		//	Label And separator
	static private final int	INFO      = 0;
	static private final int	HISTORY   = 1;
	static private final int	REMOVE    = 2;
	static private final int	MOVE_UP   = 3;
    static private final int	MOVE_DOWN = 4;

    static public final int	UP   = 0;
    static public final int	DOWN = 1;
	//==========================================================
	/**
	 *	Class Constructor
	 */
	//==========================================================
	public TablePopupMenu(EventsTable parent) {
		super ();
		this.parent  = parent;
		buildBtnPopupMenu();
	}
	//===============================================================
	/**
	 *	Create a Popup menu for host control
	 */
	//===============================================================
	private void buildBtnPopupMenu()
	{
        titleLbl = new JLabel();
        titleLbl.setFont(new java.awt.Font("Dialog", 1, 16));
		add(titleLbl);
		add(new JPopupMenu.Separator());
		for (int i=0 ; i<pMenuLabels.length ; i++)
		{
            JMenuItem	btn = new JMenuItem(pMenuLabels[i]);
            btn.addActionListener (new java.awt.event.ActionListener () {
                public void actionPerformed (ActionEvent evt) {
                    executeActionPerformed(evt);
                }
            });
            add(btn);
		}
	}
	//======================================================
	/**
	 *	Manage event on clicked mouse on PogoTree object.
	 */
	//======================================================
    private SubscribedSignal signal;
	public void showMenu(java.awt.event.MouseEvent evt, SubscribedSignal signal)
	{
        this.signal = signal;
        getComponent(OFFSET+HISTORY).setVisible(signal.histo.size()>0);
    	titleLbl.setText(signal.toString());

		show(parent.table, evt.getX(), evt.getY());
	}
	//======================================================
	/**
	 *	Called when popup menu item selected
	 */
	//======================================================
	private void executeActionPerformed(ActionEvent evt)
	{
 		String  cmd = evt.getActionCommand();

		if (cmd.equals(pMenuLabels[INFO]))
			parent.displayInfo(signal);
		else
		if (cmd.equals(pMenuLabels[HISTORY]))
			parent.displayHistory(signal);
		else
		if (cmd.equals(pMenuLabels[REMOVE]))
		{
			if (JOptionPane.showConfirmDialog(parent,
								"OK to remove " + signal + " ?",
								"Confim Dialog",
								JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
			{
				//	Remove signal
				parent.remove(signal);
			}
		}
        else
        if (cmd.equals(pMenuLabels[MOVE_UP]))
            parent.move(signal, UP);
        else
        if (cmd.equals(pMenuLabels[MOVE_DOWN]))
            parent.move(signal, DOWN);
	}
}
