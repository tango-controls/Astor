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


package admin.astor;


import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Create a thread to display a Progress Monitor
 * and update it during files generation.
 */
public class Monitor extends JDialog {
    static private int MAX = 100;
    private ProgressMonitor pbar = null;
    static private int cnt = 10;
    static private String actionStr = null;

    //======================================================================
    /**
     * Constructor for the dialog.
     *
     * @param    parent     parent to create dialog.
     */
    //======================================================================
    public Monitor(JFrame parent) {
        super(parent, true);
        pbar = new ProgressMonitor(this,
                "Astor is Starting up. Wait a while....", "Astor Startup", 0, MAX);
    }
    //======================================================================
    /**
     * Constructor for the dialog.
     *
     * @param    parent     parent to create dialog.
     */
    //======================================================================
    public Monitor(JFrame parent, String title, String note) {
        super(parent, true);
        pbar = new ProgressMonitor(this, title, note, 0, MAX);
    }
    //======================================================================
    /**
     * Constructor for the dialog.
     *
     * @param    parent     parent to create dialog.
     */
    //======================================================================
    public Monitor(JFrame parent, String title) {
        this(parent, title, "");
    }
    //======================================================================
    /**
     * Constructor for the dialog.
     *
     * @param    parent     parent to create dialog.
     */
    //======================================================================
    public Monitor(JDialog parent, String title, String note) {
        super(parent, true);
        pbar = new ProgressMonitor(this, title, note, 0, MAX);
    }
    //======================================================================
    /**
     * Constructor for the dialog.
     *
     * @param    parent     parent to create dialog.
     */
    //======================================================================
    public Monitor(JDialog parent, String title) {
        this(parent, title, "");
    }
    //======================================================================
    /**
     * Invoked by the timer every half second.
     * Simply place the progress monitor update in event queue.
     */
    //======================================================================
    public void actionPerformed(@SuppressWarnings("UnusedParameters") ActionEvent evt) {
        //SwingUtilities.invokeLater(new Update());
    }
    //======================================================================
    /**
     * Update the ratio value..
     */
    //======================================================================
    public void setProgressValue(double ratio) {
        cnt = (int) (ratio * MAX);
        if (cnt <= 0) cnt = 1;
        SwingUtilities.invokeLater(new Update());
        toFront();
    }
    //======================================================================
    /**
     * Update the ratio value and action string.
     */
    //======================================================================
    public void setProgressValue(double ratio, String str) {
        cnt = (int) (ratio * MAX);
        if (cnt <= 0) cnt = 1;
        actionStr = str;
        SwingUtilities.invokeLater(new Update());
        toBack();
        toFront();
        //System.out.println("   Cnt: "+cnt);
    }

    //======================================================================
    /**
     * Return true if the cancel button has been clicked.
     */
    //======================================================================
    public boolean isCanceled() {
        return pbar.isCanceled();
    }
    //======================================================================
    /**
     * Start the thread to update Process Monitor.
     */
    //======================================================================
    class Update implements Runnable {
        public synchronized void run() {
            pbar.setProgress(cnt);
            if (actionStr == null)
                pbar.setNote("Operation is " + cnt + "% complete...");
            else
                pbar.setNote(actionStr);
        }
    }
    //======================================================================
}
