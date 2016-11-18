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

package admin.astor.tools;

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.Except;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *	This class is able to ping hosts and check alive/stopped list
 *
 * @author verdier
 */

class PingHosts {
    private List<String> aliveList = new ArrayList<>();
    private List<String> stoppedList = new ArrayList<>();

    private static final String PingCommand =
            AstorUtil.osIsUnix()? "ping  -c 1 -W 1 " : "ping  -n 1 -w 1 ";
    private static final String AliveResponse =
            AstorUtil.osIsUnix()? "1 received" : "Received = 1";
    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    public PingHosts(String host) {
        this(new String[] { host });
    }
    //===============================================================
    //===============================================================
    PingHosts(String[] hosts) {
        //	Start a thread to ping each host
        List<PingThread> threads = new ArrayList<>();
        for (String host : hosts) {
            PingThread thread = new PingThread(host);
            thread.start();
            threads.add(thread);
        }

        //	Check results
        for (PingThread thread : threads) {
            try { thread.join(); } catch (InterruptedException e) { /* */ }
            if (thread.hostAlive())
                aliveList.add(thread.hostName);
            else
                stoppedList.add(thread.hostName);
            thread.interrupt();
        }
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    public boolean noStopped() throws DevFailed {
        return stoppedList.isEmpty();
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    public List<String> getAliveList() throws DevFailed {
        return aliveList;
    }
    //===============================================================
    //===============================================================
    List<String> getStoppedList() throws DevFailed {
        return stoppedList;
    }
    //===============================================================
    //===============================================================










    //===============================================================
    //===============================================================
    private class PingThread extends Thread {
        private String hostName;
        private boolean alive = false;
        //===============================================================
        //===============================================================
        private PingThread(String hostName) {
            this.hostName = hostName;
        }
        //===============================================================
        //===============================================================
        private boolean hostAlive() {
            return alive;
        }
        //===============================================================
        //===============================================================
        public void run() {
            try {
                String str = executeShellCmdOneLine(PingCommand + hostName);
                alive = str.contains(AliveResponse);
                if (!alive) System.out.println(str);
                //if (hostName.contains("fofb-dev")) System.out.println(str);

            } catch (DevFailed e) {
                Except.print_exception(e);
            }
        }
        //===============================================================
        /**
         * Execute a shell command and throw exception if command failed.
         *
         * @param cmd shell command to be executed.
         * @return output of executed command if display is false
         * @throws fr.esrf.Tango.DevFailed if executed command has failed.
         */
        //===============================================================
        String executeShellCmdOneLine(String cmd) throws DevFailed {
            StringBuilder sb = new StringBuilder();
            try {
                Process process = Runtime.getRuntime().exec(cmd);

                // get command output stream and
                // put a buffered reader input stream on it.
                InputStream inputStream = process.getInputStream();
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(inputStream));

                // read output lines from command
                String str;
                while ((str = br.readLine()) != null) {
                    sb.append(str).append("\n");
                }

                // wait for end of command
                //process.waitFor();
            } catch (Exception e) {
                Except.throw_exception(e.toString(),
                        "The shell command\n" + cmd + "\nHas failed");
            }
            //System.out.println(sb);
            return sb.toString();
        }
        //===============================================================
        //===============================================================
    }
}
