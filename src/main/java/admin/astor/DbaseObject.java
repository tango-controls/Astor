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
import fr.esrf.TangoApi.*;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;

public class DbaseObject implements AstorDefs {
    private AstorTree parent;
    private String tango_host;
    private DbaseState state_thread;

    int state = unknown;
    DevFailed except = null;
    //======================================================
    //======================================================
    public DbaseObject(AstorTree parent, String tango_host) {
        //	initialize data members
        this.parent = parent;
        this.tango_host = tango_host;

        //	Start update thread.
        state_thread = new DbaseState();
        state_thread.start();
    }
    //===============================================================
    //===============================================================
    void start() {
        //	Start update thread.
        state_thread.start();
    }
    //======================================================
    //======================================================
    String getServerInfo() throws DevFailed {
        String deviceName = state_thread.db.get_name();
        DeviceProxy dev = new DeviceProxy(deviceName);
        String str = "TANGO_HOST:    " + tango_host + "\n\n";

        str += dev.get_info() + "\n\n";
        try {
            DeviceAttribute att = dev.read_attribute("StoredProcedureRelease");
            str += "Stored Procedure: " + att.extractString();
        } catch (DevFailed e) {
            //	Attribute not found
        }
        return str + "\n\n";
    }

    //======================================================
    //======================================================
    String getInfo() throws DevFailed {
        Database db = ApiUtil.get_db_obj(tango_host);
        return db.get_info();
    }

    //======================================================
    //======================================================
    void showBlackBox(JFrame parent) {
        try {
            new BlackBoxTable(parent,
                    state_thread.db.getDeviceName()).setVisible(true);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }
    //======================================================
    //======================================================
    void monitor() {
        dbbench.DBBench dbBench = new dbbench.DBBench(state_thread.db.getDeviceName(), false);
        ATKGraphicsUtils.centerFrameOnScreen(dbBench);
        dbBench.setVisible(true);
    }
    //======================================================
    //======================================================
    public String toString() {
        return tango_host;
    }


    //======================================================
    //======================================================
    private class DbConnection extends Connection {
        private String deviceName;

        private DbConnection(String host, String port) throws DevFailed {
            super(host, port, false);
            deviceName = get_name();
        }
        private String getDeviceName() {
            return deviceName;
        }
    }
    //======================================================
    /**
     * A thread class to control device.
     */
    //======================================================
    private class DbaseState extends Thread {
        private String host;
        private String port;
        private DbConnection db = null;

        //===============================================================
        //===============================================================
        private DbaseState() {
            setName("Database State Thread");
            int idx = tango_host.indexOf(":");
            host = tango_host.substring(0, idx);
            port = tango_host.substring(idx + 1);
        }


        //===============================================================
        //===============================================================
        private synchronized void wait_next_loop() {
            try {
                wait(AstorDefs.PollPeriod);
            } catch (InterruptedException e) {
                System.err.println(e.toString());
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

            //	Try to ping database
            try {
                //	Build connection if not done
                //	Do not use ApiUtil.get_db_obj() to be sure
                //	on which database the connection is done
                if (db == null)
                    db = new DbConnection(host, port);
                db.ping();

                tmp_state = all_ok;
                tmp_except = null;
            } catch (DevFailed e) {
                //	If exception catched, save it
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
