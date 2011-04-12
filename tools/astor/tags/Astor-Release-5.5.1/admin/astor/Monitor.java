//+======================================================================
// $Source$
//
// Project:   Astor
//
// Description:  java source code for Astor statup progress monitor. .
//
// $Author$
//
// $Version$
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;


import javax.swing.*;
import java.awt.event.ActionEvent;

//===========================================================
/**
 *	Create a thread to display a Progress Monitor
 *	and update it during files generation.
 */
//===========================================================
public class Monitor extends JDialog
{
	static private int		MAX  = 100;
	private ProgressMonitor	pbar = null;
	static private int		cnt  = 10;
	static private String	actionStr = null;

//======================================================================
/**
 *	Constructor for the dialog.
 *
 *	@param	parent	 parent to create dialog.
 */
//======================================================================
	public Monitor(JFrame parent)
	{
		super(parent, true);
		pbar = new ProgressMonitor(this, "Astor is Starting up. Wait a while....",
									"Astor Startup", 0, MAX);
	}
//======================================================================
/**
 *	Constructor for the dialog.
 *
 *	@param	parent	 parent to create dialog.
 */
//======================================================================
	public Monitor(JFrame parent, String title, String note)
	{
		super(parent, true);
		pbar = new ProgressMonitor(this, title, note, 0, MAX);
	}
//======================================================================
/**
 *	Constructor for the dialog.
 *
 *	@param	parent	 parent to create dialog.
 */
//======================================================================
	public Monitor(JFrame parent, String title)
	{
		this(parent, title, "");
	}
//======================================================================
/**
 *	Constructor for the dialog.
 *
 *	@param	parent	 parent to create dialog.
 */
//======================================================================
	public Monitor(JDialog parent, String title, String note)
	{
		super(parent, true);
		pbar = new ProgressMonitor(this, title, note, 0, MAX);
	}
//======================================================================
/**
 *	Constructor for the dialog.
 *
 *	@param	parent	 parent to create dialog.
 */
//======================================================================
	public Monitor(JDialog parent, String title)
	{
		this(parent, title, "");
	}
//======================================================================
/**
 *	Invoked by the timer every half second.
 *	Simply place the progress monitor update in event queue.
 */
//======================================================================
	public void actionPerformed(ActionEvent	evt)
	{
		//SwingUtilities.invokeLater(new Update());
	}
//======================================================================
/**
 *	Update the ratio value..
 */
//======================================================================
	public void setProgressValue(double ratio)
	{
		cnt = (int)(ratio*MAX);
		if (cnt<=0) cnt = 1;
		SwingUtilities.invokeLater(new Update());
		toFront();
	}
//======================================================================
/**
 *	Update the ratio value and action string.
 */
//======================================================================
	public void setProgressValue(double ratio, String str)
	{
		cnt = (int)(ratio*MAX);
		if (cnt<=0) cnt = 1;
		actionStr = str;
		SwingUtilities.invokeLater(new Update());
		toBack();
		toFront();
	//System.out.println("   Cnt: "+cnt);
	}

//======================================================================
/**
 *	Return true if the cancel button has been clicked.
 */
//======================================================================
	public boolean isCanceled()
	{
		return pbar.isCanceled();
	}
//======================================================================
/**
 *	Start the thread to update Process Monitor.
 */
//======================================================================
	class Update implements Runnable
	{
		public synchronized void run()
		{
			pbar.setProgress(cnt);
			if (actionStr==null)
				pbar.setNote("Operation is " + cnt + "% complete...");
			else
				pbar.setNote(actionStr);
		}
	}
 //======================================================================
}
