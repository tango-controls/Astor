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

import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/*
 * A thread to do the job.
 */
public class ScanningControlSystem  {

    private List<String> classList = new ArrayList<>();
    private List<String> binaryList = new ArrayList<>();
    private List<String> stoppedList = new ArrayList<>();
    private List<TangoCommand> dServerCommands = new ArrayList<>();

    private StringBuilder   results = new StringBuilder();
    //===========================================================
    //===========================================================
    public static void main(String[] args) {
        if (args.length<2) {
            System.out.println("File and Host names ?");
        }
        else {
            String fileName = args[0];
            List<String>   hostNames = new ArrayList<>();
            hostNames.addAll(Arrays.asList(args).subList(1, args.length));
            System.out.println("Stating scan - put results in " + fileName);
            new ScanningControlSystem(fileName, hostNames);
        }

    }
    //===========================================================
    //===========================================================
    public ScanningControlSystem(String fileName, List<String> hostNames) {
        try {

            //  Browse the Database.
            List<TangoHost>    hostList = new ArrayList<>();
            for (String hostName : hostNames) {
                hostList.add(new TangoHost(hostName));
            }

            //  make sums
            int nbServers = 0 ;
            int nbDevices = 0 ;
            int nbControlPoints = 0 ;
            int nbAttributes = 0;
            int nbCommands = 0;
            for (TangoHost tangoHost : hostList) {
                nbServers += tangoHost.size();
                for (TangoServer tangoServer : tangoHost) {
                    nbDevices += tangoServer.size();
                    for (TangoDevice tangoDevice : tangoServer) {
                        nbAttributes += tangoDevice.size();
                        nbCommands   += tangoDevice.commands.size();
                        for (TangoAttribute attribute : tangoDevice) {
                            nbControlPoints += attribute.controlPoint;
                        }
                        for (TangoCommand command : tangoDevice.commands) {
                            nbControlPoints += command.controlPoint;
                        }
                    }
                }
            }

            //  Build results
            results.append("Controlled under Starter:\n");
            results.append("    - Hosts:\t\t\t\t")        .append(hostList.size()).append("\n");
            results.append("    - Server types:\t\t")     .append(binaryList.size()).append("\n");
            results.append("    - Server/Instances:\t")   .append(nbServers).append("\n");
            results.append("    - Classes:\t\t\t")        .append(classList.size()).append("\n");
            results.append("    - Devices:\t\t\t")        .append(nbDevices).append("\n");
            results.append("    - Control Points:\t\t")   .append(nbControlPoints).append("\n");
            results.append("\t\t- attributes:\t")         .append(nbAttributes).append("\n");
            results.append("\t\t- commands:\t")           .append(nbCommands).append("\n");

            if (stoppedList.size()>0) {
                results.append("\n").append(" Server(s) cannot be checked : ")
                        .append(stoppedList.size()).append("\n");
            }
            results.append("Measurements done ").append(new Date()).append("\n");


            //  Display results
            saveResults(fileName);
            printComplementaryResults();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(new JFrame(), null, e);
        }
    }
    //===========================================================
    //===========================================================
    private void saveResults(String filename) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        writeFile(tmpDir+"/"+filename, results.toString());
    }
    //===============================================================
    //===============================================================
    public static void writeFile(String filename, String code) {
        try {
            FileOutputStream fidout = new FileOutputStream(filename);
            fidout.write(code.getBytes());
            fidout.close();
            System.out.println("Results saved in " + filename);
        } catch (Exception e) {
            System.err.println("Cannot write " + filename + "\n"+ e);
        }
    }
    //===========================================================
    //===========================================================
    private void printComplementaryResults() {
        //System.out.println(hostList.get(0));
        System.out.println(stoppedList.size() + " stopped server(s)");
        for (String stopped : stoppedList)
            System.out.println(stopped);
    }


    //===============================================================
    //===============================================================
    private class TangoCommand {
        String name;
        int controlPoint;
        private TangoCommand(CommandInfo commandInfo) {
            this.name = commandInfo.cmd_name;
            //  Check if args are In and Out (2 Cp)  or otherwise (1 CP)
            if (commandInfo.in_type!= TangoConst.Tango_DEV_VOID &&
                commandInfo.out_type!= TangoConst.Tango_DEV_VOID) {
                controlPoint = 2;
            }
            else
                controlPoint = 1;
        }
    }
    //===============================================================
    //===============================================================
    private class TangoAttribute {
        String name;
        int controlPoint;
        private TangoAttribute(AttributeInfo attributeInfo) {
            this.name = attributeInfo.name;
            //  Check if READ (1 CP)  or writable (2 CP)
            if (attributeInfo.writable== AttrWriteType.READ) {
                controlPoint = 1;
            }
            else
                controlPoint = 2;
        }
    }
    //===============================================================
    //===============================================================
    private class TangoDevice extends ArrayList<TangoAttribute> {
        private String name;
        private List<TangoCommand>   commands = new ArrayList<>();
        //===========================================================
        private TangoDevice(String name, boolean running) {
            this.name = name;
            if (name.startsWith("dserver/")) {
                manageDserverDevice(running);
            }
            else {
                if (running) {
                    try {
                        DeviceProxy proxy = new DeviceProxy(name);

                        AttributeInfo[] attributeInfoList = proxy.get_attribute_info();
                        for (AttributeInfo attributeInfo : attributeInfoList) {
                            add(new TangoAttribute(attributeInfo));
                        }

                        CommandInfo[]   commandInfoList = proxy.command_list_query();
                        for (CommandInfo commandInfo : commandInfoList) {
                            commands.add(new TangoCommand(commandInfo));
                        }
                    }
                    catch (DevFailed e) { /*   */ }
                }
            }
        }
        //===========================================================
        private void manageDserverDevice(boolean running) {
            //  Create a DServer device
            //  It is not necessary to read it, all dserver are same device
            //  Only the first dserver device will be called
            if (running && dServerCommands.isEmpty()) {
                try {
                    //int i=0;
                    DeviceProxy proxy = new DeviceProxy(name);
                    CommandInfo[]   commandInfoList = proxy.command_list_query();
                    for (CommandInfo commandInfo : commandInfoList) {
                        dServerCommands.add(new TangoCommand(commandInfo));
                        //System.out.println(i++ + "/" +
                        //        commandInfoList.length + ":\t" + commandInfo.cmd_name);
                    }
                }
                catch (DevFailed e) { /* */ }
            }
            for (TangoCommand tangoCommand : dServerCommands) {
                commands.add(tangoCommand);
            }
        }
        //===========================================================
        private int getControlPointCount() {
            int nb = 0;
            for (TangoAttribute attribute : this)
                nb += attribute.controlPoint;
            for (TangoCommand command : commands)
                nb += command.controlPoint;
            return nb;
        }
        //===========================================================
        public String toString() {
            String  tab = "        ";
            String sb = name + ":\n" + tab + size() + " attributes\n" +
                    tab + commands.size() + " commands\n" +
                    tab + tab + getControlPointCount() + " control point\n";

            return tab + sb.trim();
        }
        //===========================================================
    }
    //===============================================================
    //===============================================================



    //===============================================================
    //===============================================================
    private class TangoServer extends ArrayList<TangoDevice> {
        String name;
        String binary;
        boolean running;
        //===========================================================
        private TangoServer(String name) throws DevFailed {
            this.name = name;
            binary = name.substring(0, name.indexOf('/'));
            if (!binaryList.contains(binary)) {
                binaryList.add(binary);
            }

            running = isRunning();

            //  Check classes and devices for this server
            DbServer server = new DbServer(name);
            String[]    argOut = server.get_device_class_list();
            for (int i=0 ; i<argOut.length ; i+=2) {
                String className = argOut[i+1];
                if (!classList.contains(className)) {
                    //System.out.println(classList.size() + "\t"+className);  //  className
                    classList.add(className);
                }
                //  Do not really control DServer
                String  deviceName = argOut[i];
                 add(new TangoDevice(deviceName, running));

                //  Wait a bit to do not overload control system
                try { Thread.sleep(5); } catch (InterruptedException e) { /* */ }
            }
        }
        //===========================================================
        private boolean isRunning() {
            try {
                new DeviceProxy("dserver/"+name).ping();
                return true;
            }
            catch (DevFailed e) {
                stoppedList.add(name + ":\t" + e.errors[0].desc);
                return false;
            }
        }
        //===========================================================
        public String toString() {
            String  tab = "    ";
            StringBuilder   sb = new StringBuilder(name+":\n");
            for (TangoDevice device : this) {
                sb.append(device).append("\n");
            }
            return tab + sb.toString().trim();
        }
        //===========================================================
    }
    //===============================================================
    //===============================================================


    //===============================================================
    //===============================================================
    private class TangoHost extends ArrayList<TangoServer> {
        String name;
        //===========================================================
        private TangoHost(String name) throws DevFailed{
            this.name = name;

            //  Build Server List
            DeviceData argIn = new DeviceData();
            argIn.insert(name);
            DeviceData argOut = ApiUtil.get_db_obj().command_inout("DbGetHostServerList", argIn);
            String[] serverNames = argOut.extractStringArray();

            //  Store only controlled servers
            for (String serverName : serverNames) {
                if (serverControlled(serverName)) {
                    System.out.println("Checking on " + name + ": " + serverName);
                    add(new TangoServer(serverName));
                }
            }
        }
        //===========================================================
        private boolean serverControlled(String serverName) throws DevFailed {
            if (serverName.startsWith("Starter/") || serverName.startsWith("DataBase"))
                return true;
            //	Query database for control mode.
            DbServInfo info = ApiUtil.get_db_obj().get_server_info(serverName);
            //	store only controlled servers
            return info.controlled;
         }
        //===========================================================
        public String toString() {
            StringBuilder   sb = new StringBuilder(name+":\n");
            for (TangoServer server : this) {
                sb.append(server).append("\n");
            }
            return sb.toString();
        }
        //===========================================================
    }
    //===============================================================
    //===============================================================
}
