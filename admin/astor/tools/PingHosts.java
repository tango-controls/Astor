//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011
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


/**
 *	This class is able to
 *
 * @author verdier
 */

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.Except;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class PingHosts {
    private ArrayList<DevState> states = new ArrayList<DevState>();
    private String[] hosts;

    //===============================================================
    //===============================================================
    public PingHosts(String[] hosts) throws DevFailed {
        this.hosts = hosts;
        //	Start a thread to ping each host
        ArrayList<PingThread> threads = new ArrayList<PingThread>();
        for (String host : hosts) {
            threads.add(new PingThread(host));
        }
        for (PingThread thread : threads) {
            thread.start();
        }

        //	Wait a bit
        try {
            if (AstorUtil.osIsUnix())
                Thread.sleep(2000);
            else
                Thread.sleep(5000);
        } catch (InterruptedException e) { /* */ }

        //	Check results
        for (PingThread thread : threads) {
            if (thread.hostAlive())
                states.add(DevState.ON);
            else
                states.add(DevState.FAULT);
            thread.interrupt();
        }
    }

    //===============================================================
    //===============================================================
    public ArrayList<DevState> getStates() throws DevFailed {
        return states;
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    public ArrayList<String> getRunning() throws DevFailed {
        ArrayList<String> v = new ArrayList<String>();
        for (int i = 0; i < hosts.length && i < states.size(); i++) {
            if (states.get(i) == DevState.ON) {
                v.add(hosts[i]);
            }
        }
        return v;
    }

    //===============================================================
    //===============================================================
    public ArrayList<String> getStopped() throws DevFailed {
        ArrayList<String> v = new ArrayList<String>();
        for (int i = 0; i < hosts.length && i < states.size(); i++) {
            if (states.get(i) == DevState.FAULT) {
                v.add(hosts[i]);
            }
        }
        return v;
    }

    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        try {
            String[] hosts = AstorUtil.getInstance().getHostControlledList();
            /*
                { "l-pinj-1", "l-pinj-2", "l-pinj-3", "deneb",
				 //"l-c10-4", "l-c10-5", "l-c10-6" , "orion",
				};
                */
            int alives = 0;
            int deads = 0;
            long t0 = System.currentTimeMillis();
            PingHosts client = new PingHosts(hosts);
            ArrayList<DevState> states = client.getStates();
            for (int i = 0; i < hosts.length && i < states.size(); i++) {
                if (states.get(i) == DevState.FAULT)
                    deads++;
                else
                    alives++;
                System.out.println(hosts[i] + ":	" +
                        ((states.get(i) == DevState.FAULT) ? "NOT " : "") + " alive");
            }
            long t1 = System.currentTimeMillis();
            System.out.println("elapsed time: " + (t1 - t0) + " ms");
            System.out.println(alives + " hosts alive   and  " + deads + " hosts dead");
        } catch (DevFailed e) {
            Except.print_exception(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
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
                String str = executeShellCmdOneLine("ping " + hostName);
                if (AstorUtil.osIsUnix())
                    alive = (str.toLowerCase().indexOf("unreachable") < 0);
                else
                    alive = (str.toLowerCase().indexOf("unreachable") < 0 &&
                            str.length() > 0 && str.indexOf("timed out") < 0);

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
        public String executeShellCmdOneLine(String cmd) throws DevFailed {
            StringBuffer sb = new StringBuffer();
            try {
                Process proc = Runtime.getRuntime().exec(cmd);

                // get command's output stream and
                // put a buffered reader input stream on it.
                //-------------------------------------------
                InputStream istr = proc.getInputStream();
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(istr));

                // read output lines from command
                //  Ping result is in second line
                //-------------------------------------------
                String str;
                for (int cnt = 0; cnt < 2 && (str = br.readLine()) != null;) {
                    str = str.trim();
                    if (str.length() > 0) {
                        sb.append(str.trim()).append("\n");
                        cnt++;
                    }
                }
                proc.destroy();
            } catch (Exception e) {
                Except.throw_exception(e.toString(),
                        "The shell command\n" + cmd + "\nHas failed",
                        "Utils.executeShellCmd()");
            }
            //System.out.println(sb);
            return sb.toString();
        }
    }
}
