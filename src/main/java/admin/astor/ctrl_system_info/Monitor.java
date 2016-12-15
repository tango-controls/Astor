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

package admin.astor.ctrl_system_info;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//===========================================================
/**
 * Create a thread to display a Progress Monitor
 * and update it during database browsing
 */
//===========================================================
public class Monitor extends JDialog implements ActionListener {
    private ProgressMonitor progressBar = null;
    private int cnt = 0;
    private String actionStr = null;

    private static final int Maximum = 100;
    //======================================================================
    /**
     * Constructor for the dialog.
     * @param component parent component
     * @param text Main text to display
     */
    //======================================================================
    public Monitor(JDialog component, String text) {
        super(component, true);
        progressBar = new ProgressMonitor(this, text, "Startup", 0, Maximum);
    }
    //======================================================================
    /**
     * Invoked by the timer every half second.
     * Simply place the progress monitor update in event queue.
     */
    //======================================================================
    public void actionPerformed(ActionEvent evt) {
        //SwingUtilities.invokeLater(new Update());
    }
    //======================================================================
    /**
     * Update the ratio value..
     * @param text  text to display
     */
    //======================================================================
    public void increaseProgressValue(String text) {
        cnt++;
        if (cnt >= 95) cnt = 5;
        actionStr = text;
        SwingUtilities.invokeLater(new Update());
    }
    //======================================================================
    /**
     * Update the ratio value..
     * @param ratio value between 0 and 1
     */
    //======================================================================
    @SuppressWarnings("UnusedDeclaration")
    public void setProgressValue(double ratio) {
        cnt = (int) (ratio * Maximum);
        if (cnt <= 0) cnt = 1;
        SwingUtilities.invokeLater(new Update());
    }

    //======================================================================
    /**
     * Update the ratio value and action string.
     * @param ratio value between 0 and 1
     * @param text  text to display
     */
    //======================================================================
    public void setProgressValue(double ratio, String text) {
        cnt = (int) (ratio * Maximum);
        if (cnt <= 0) cnt = 1;
        actionStr = text;
        SwingUtilities.invokeLater(new Update());
    }

    //======================================================================
    /**
     * Stop the monitor
     */
    //======================================================================
    public void stop() {
        cnt = Maximum +1;
        SwingUtilities.invokeLater(new Update());
    }
    //======================================================================
    /**
     * Return true if the cancel button has been clicked.
     */
    //======================================================================
    public boolean isCanceled() {
        return progressBar.isCanceled();
    }
    //======================================================================
    /**
     * Start the thread to update Process Monitor.
     */
    //======================================================================
    class Update implements Runnable {
        public synchronized void run() {
            progressBar.setProgress(cnt);
            if (actionStr == null)
                progressBar.setNote("Operation is " + cnt + "% complete...");
            else
                progressBar.setNote(actionStr);
        }
    }
    //======================================================================
}
