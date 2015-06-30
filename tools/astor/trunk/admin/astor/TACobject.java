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

import admin.astor.tools.BlackBoxTable;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevInfo;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;

public class TACobject extends DeviceProxy implements AstorDefs {
    private AstorTree parent;
    private String deviceName;
    private StateThread state_thread;

    int state = unknown;
    DevFailed except = null;

    //======================================================
    //======================================================
    public TACobject(AstorTree parent, String deviceName) throws DevFailed {
        super(deviceName);
        //	initialize data members
        this.parent = parent;
        this.deviceName = deviceName;

        //	Start update thread.
        state_thread = new StateThread();
        state_thread.start();
    }

    //======================================================
    //======================================================
    String getCvsTag() throws DevFailed {
        String tagName = null;
        DevInfo info = info();
        String servinfo = info.doc_url;
        String tag = "CVS Tag = ";
        int start = servinfo.indexOf(tag);
        if (start > 0) {
            start += tag.length();
            int end = servinfo.indexOf('\n', start);
            if (end > start)
                tagName = servinfo.substring(start, end);
        }
        if (tagName == null)
            return "";
        else
            return "CVS Tag:   " + tagName + "\n";
    }

    //======================================================
    //======================================================
    String getServerInfo() throws DevFailed {
        String str = "Tango Access Control:\n\n";

        str += get_info() + "\n\n";
        str += getCvsTag();
        str += "\n\n";

        return str;
    }

    //===============================================================
    //===============================================================
    void start() {
        //	Start update thread.
        state_thread.start();
    }

    //======================================================
    //======================================================
    void blackbox(JFrame parent) {
        try {
            new BlackBoxTable(parent, deviceName).setVisible(true);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }

    //======================================================
    //======================================================
    public String toString() {
        return "Access Control";
    }

    //======================================================
    /**
     * A thread class to control device.
     */
    //======================================================
    private class StateThread extends Thread {
        //===============================================================
        //===============================================================
        private StateThread() {
            setName("TAC State Thread");
        }
        //===============================================================
        //===============================================================
        private synchronized void wait_next_loop() {
            try {
                wait(AstorDefs.PollPeriod);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //===============================================================
        //===============================================================
        //private synchronized void updateParent(int tmp_state, DevFailed tmp_except)
        private void updateParent(int tmp_state, DevFailed tmp_except) {
            if (state != tmp_state || except != tmp_except) {
                state = tmp_state;
                except = tmp_except;
                parent.updateState();
            }
        }

        //===============================================================
        //===============================================================
        private void manageState() {
            int tmp_state;
            DevFailed tmp_except;

            //	Try to ping TAC
            try {
                ping();

                tmp_state = all_ok;
                tmp_except = null;
            } catch (DevFailed e) {
                //	If exception caught, save it
                tmp_state = faulty;
                tmp_except = e;
            }
            updateParent(tmp_state, tmp_except);
        }

        //===============================================================
        //===============================================================
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                manageState();
                wait_next_loop();
            }
        }
    }
}
