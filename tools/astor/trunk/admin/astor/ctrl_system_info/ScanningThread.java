//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009
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
// $Revision:  $
//
// $Log:  $
//
//-======================================================================

package admin.astor.ctrl_system_info;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;


//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================





//===============================================================
/*
 * A thread to do the job.
 */
//===============================================================
public class ScanningThread extends Thread {

    private ArrayList<String> hostNames;
    private ResultsStructure   results = new ResultsStructure();
    private Monitor monitor;
    private static final int nbHostByJVM = 10;
    private static final String fileNameHeader = "CtrlSystemResults_";
    //===========================================================
    //===========================================================
    public ScanningThread(ArrayList<String> hostNames, Monitor monitor) {
        this.hostNames = hostNames;
        this.monitor = monitor;
        monitor.setProgressValue(0.05, "Starting to scan control system...");
    }
    //===========================================================
    //===========================================================
    public String getResults() {
        return results.toString() + "\n\n" +
                "Measurements done " + new Date();
    }
    //===========================================================
    //===========================================================
    private void startInAnotherJvm(String parameters)
            throws DevFailed, InterruptedException, IOException {
        String command = "java -DTANGO_HOST="+ApiUtil.getTangoHost() +
                "  admin.astor.ctrl_system_info.ScanningControlSystem " + parameters;
        executeShellCommand(command);
    }
    //===============================================================
    /**
     * Open a file and return text read.
     *
     * @param filename file to be read.
     * @return the file content read.
     * @throws fr.esrf.Tango.DevFailed in case of failure during read file.
     */
    //===============================================================
    public static String readFile(String filename) throws DevFailed {
        String str = "";
        try {
            FileInputStream fid = new FileInputStream(filename);
            int nb = fid.available();
            byte[] inStr = new byte[nb];
            nb = fid.read(inStr);
            fid.close();
            if (nb>0)
                str = new String(inStr);
        } catch (Exception e) {
            Except.throw_exception("READ_FAILED",
                    e.toString(), "readFile()");
        }
        return str;
    }
    //===========================================================
    //===========================================================
    public void run() {

        try {
            int fileCounter = 0;
            StringBuilder   sb = new StringBuilder(fileNameHeader+fileCounter++);
            for (int i=0 ; i<hostNames.size() ; i++) {
                String hostName = hostNames.get(i);
                if (i!=0 && i%nbHostByJVM==0) {
                    startInAnotherJvm(sb.toString());
                    sb = new StringBuilder(fileNameHeader+fileCounter++);
                }
                sb.append(" ").append(hostName);
            }
            if (sb.length()>0) {
                //  Do it for last one if any
                startInAnotherJvm(sb.toString());
            }
            monitor.stop();

            //  Then, collect results
            String tmpDir = System.getProperty("java.io.tmpdir");
            for (int i=0 ; i< fileCounter ; i++) {
                String  fileName = tmpDir + "/" + fileNameHeader+i;
                ResultsStructure    structure = new ResultsStructure(readFile(fileName));
                results.add(structure);

                System.out.println(structure);
                if (!new File(fileName).delete())
                    System.err.println("Cannot delete " + fileName);
            }
        }
        catch (Exception e) {
            ErrorPane.showErrorMessage(new JFrame(), null, e);
        }
    }

    //===============================================================
    //===============================================================
    private class ResultsStructure {
        int nbHosts = 0;
        int nbSevers = 0;
        int nbServerInstances = 0;
        int nbDevices = 0;
        int nbControlPoints = 0;
        int nbAttributes = 0;
        int nbCommands = 0;
        int nbServerStopped = 0;

        //===========================================================
        private ResultsStructure() {

        }
        //===========================================================
        private ResultsStructure(String code) {
            code = code.toLowerCase();
            StringTokenizer stk = new StringTokenizer(code, "\n");
            while (stk.hasMoreTokens()) {
                String line = stk.nextToken();
                if (line.contains("host"))
                    nbHosts = getValue(line);
                else
                if (line.contains("server types"))
                    nbSevers = getValue(line);
                else
                if (line.contains("instance"))
                    nbServerInstances = getValue(line);
                else
                if (line.contains("device"))
                    nbDevices = getValue(line);
                else
                if (line.contains("points"))
                    nbControlPoints = getValue(line);
                else
                if (line.contains("attributes"))
                    nbAttributes = getValue(line);
                else
                if (line.contains("commands"))
                    nbCommands = getValue(line);
                else
                if (line.contains("cannot be checked"))
                    nbServerStopped = getValue(line);
            }
        }
        //===========================================================
        private int getValue(String line) {
            StringTokenizer stk = new StringTokenizer(line, ":");
            try {
                stk.nextToken();    //  name
                String  strValue = stk.nextToken().trim();
                return Integer.parseInt(strValue);
            } catch (NumberFormatException e) {
                System.err.println(e.toString());
                return -1;
            }
        }
        //===========================================================
        private void add(ResultsStructure structure) {
            this.nbHosts           += structure.nbHosts;
            this.nbSevers          += structure.nbSevers;
            this.nbServerInstances += structure.nbServerInstances;
            this.nbDevices         += structure.nbDevices;
            this.nbControlPoints   += structure.nbControlPoints;
            this.nbAttributes      += structure.nbAttributes;
            this.nbCommands        += structure.nbCommands;
            this.nbServerStopped   += structure.nbServerStopped;
        }
        //===========================================================
        public String toString() {
            String str =
                    nbHosts + " hosts\n"+
                    //nbSevers + " servers\n" + //  Cannot be compared between JVMs
                    nbServerInstances + " Server instances\n" +
                    nbDevices + " devices\n" +
                    nbControlPoints + " control points\n" +
                    nbAttributes + "  attributes\n" +
                    nbCommands   + " commands";
            if (nbServerStopped>0) {
                str += "\n\n" + nbServerStopped + " server(s) cannot be checked.";
            }
            return str;
        }
        //===========================================================
    }
    //===============================================================
    /**
     *	Execute a shell command and throw exception if command failed.
     *
     *	@param cmd	shell command to be executed.
     */
    //===============================================================
    public String executeShellCommand(String cmd)
            throws IOException, InterruptedException, DevFailed {
        Process process = Runtime.getRuntime().exec(cmd);

        // get command's output stream and
        // put a buffered reader input stream on it.
        //-------------------------------------------
        InputStream inputStream = process.getInputStream();
        BufferedReader br =
                new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();

        // read output lines from command
        //-------------------------------------------
        String str;
        while ((str = br.readLine()) != null) {
            monitor.increaseProgressValue(str);
            sb.append(str).append("\n");
        }

        // wait for end of command
        //---------------------------------------
        process.waitFor();

        // check its exit value
        //------------------------
        int retVal;
        if ((retVal = process.exitValue()) != 0) {
            //	An error occured try to read it
            InputStream errorStream = process.getErrorStream();
            br = new BufferedReader(new InputStreamReader(errorStream));
            while ((str = br.readLine()) != null) {
                monitor.increaseProgressValue(str);
                sb.append(str).append("\n");
            }
            Except.throw_exception("ExecCommnandFailed",
                    "the shell command\n" + cmd + "\nreturns : " + retVal + " !\n\n" + sb,
                    "executeShellCommand()");
        }
        System.out.println(sb);
        return sb.toString();
    }
}