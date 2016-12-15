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
import admin.astor.tools.PopupTable;
import admin.astor.tools.PopupText;
import admin.astor.tools.Utils;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoApi.events.TangoEventsAdapter;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;


/**
 * Class Description:
 * Host object containing servers list.
 * This class inherit from device proxy.
 * It is seen as the Starter device running on this host.
 *
 * @author verdier
 */


@SuppressWarnings({"NestedTryStatement"})
public class TangoHost extends DeviceProxy {
    private TangoServer starter = null;
    private String name;
    private List<TangoServer> servers = new ArrayList<>();
    public String usage = null;
    public int state;
    public DevFailed except;
    public boolean poll_serv_lists = false;
    public String collection = null;
    public HostStateThread thread = null;
    public int notifydState;
    public boolean onEvents = true;
    public boolean manageNotifd;
    public HostInfoDialog info_dialog = null;

    public TangoEventsAdapter supplier = null;
    public String   eventSource = "";   //  notifd, ZMQ or empty

    private String adm_name;

    //==============================================================
    //==============================================================
    public TangoHost(String name, boolean get_prop) throws DevFailed {
        //	Initialize device proxy class objects.
        super(AstorUtil.getStarterDeviceHeader() + name);
        adm_name = "dserver/starter/" + name;
        set_transparency_reconnection(true);
        notifydState = AstorDefs.unknown;

        //	Check if name contain sub network added, then cut it.
        int i;
        if ((i = name.indexOf(".")) < 0)
            this.name = name;
        else
            this.name = name.substring(0, i);

        if (get_prop) {
            //	Get host collection from property
            DbDatum data = get_property(AstorDefs.collec_property);
            if (!data.is_empty())
                collection = data.extractString();

            //	Get if Host usage is dedefined in database.
            DbDatum prop = get_property("HostUsage");
            if (!prop.is_empty()) {
                usage = prop.extractString();
                if (usage.length() == 0)
                    usage = null;
            }
            //	Check if notify daemon is used by the Starter ds
            manageNotifd = false;
            try {
                data = get_property("UseEvents");
                if (!data.is_empty())
                    manageNotifd = (data.extractShort() != 0);
            } catch (DevFailed e) {
                /*	Nothing */
            }
        }
        //	Else
        //		at statup it is done on one call for all hosts
    }

    //==============================================================
    //==============================================================
    static String controlMethod(boolean onEvt) {
        return (onEvt)? "on events" : "on polling";
    }
    //==============================================================
    //==============================================================
    //  Event info managed from MySqlUtil removed
    //  Because is unused in case ZMQ events.
    /*
    public TangoHost(DbDevImportInfo deviceInfo,
                     DbDevImportInfo adminInfo,
                     DbEventImportInfo evtinfo) throws DevFailed {
        //	Initialize device proxy class objects.
        super(deviceInfo);
        try {

            if (deviceInfo.exported) {
                import_admin_device(adminInfo);
                if (evtinfo != null)
                    this.getAdm_dev().set_evt_import_info(evtinfo);
                else
                    onEvents = false;
            } else
                onEvents = false;
        } catch (DevFailed e) {
            Except.print_exception(e);
            onEvents = false;
        }
        adm_name = adminInfo.name;
        set_transparency_reconnection(true);

        servers = new ArrayList<TangoServer>();
        notifydState = AstorDefs.unknown;

        //	Check if name contain sub network added, then cut it.
        int i;
        if ((i = deviceInfo.name.indexOf(".")) < 0)
            this.name = deviceInfo.name;
        else
            this.name = deviceInfo.name.substring(0, i);
        //	Get only member as name
        int idx = name.indexOf('/');
        if (idx > 0)
            idx = name.indexOf('/', idx + 1);
        if (idx > 0)
            name = name.substring(idx + 1);
    }
    */
    //==============================================================
    //==============================================================
    public TangoHost(DbDevImportInfo deviceInfo, DbDevImportInfo adminInfo) throws DevFailed {
        //	Initialize device proxy class objects.
        super(deviceInfo);
        adm_name = adminInfo.name;
        set_transparency_reconnection(true);
        notifydState = AstorDefs.unknown;

        //	Check if name contain sub network added, then cut it.
        int i;
        if ((i = deviceInfo.name.indexOf(".")) < 0)
            this.name = deviceInfo.name;
        else
            this.name = deviceInfo.name.substring(0, i);
        //	Get only member as name
        int idx = name.indexOf('/');
        if (idx > 0)
            idx = name.indexOf('/', idx + 1);
        if (idx > 0)
            name = name.substring(idx + 1);
    }

    //==============================================================
    //==============================================================
    public void addServer(TangoServer ts) {
        servers.add(ts);
    }

    //==============================================================
    //==============================================================
    public TangoServer getServer(String servname) {
        servname = servname.trim();
        for (int i = 0; i < nbServers(); i++) {
            TangoServer server = getServer(i);
            if (server.getName().equals(servname))
                return server;
        }
        return null;
    }

    //==============================================================
    //==============================================================
    public TangoServer getServer(int idx) {
        return servers.get(idx);
    }
    //==============================================================
    //==============================================================
    public List<String> getServerNames() {
        List<String>  controlledServers = new ArrayList<>();
        try {
            DeviceAttribute attribute = read_attribute("Servers");
            String[]  lines = attribute.extractStringArray();
            for (String line : lines) {
                StringTokenizer stringTokenizer = new StringTokenizer(line);
                String serverName = stringTokenizer.nextToken();
                controlledServers.add(serverName);
            }
        }
        catch(DevFailed e) { /* */ }
        return controlledServers;
    }

    //==============================================================
    //==============================================================
    public void removeServer(int idx) {
        servers.remove(idx);
    }

    //==============================================================
    //==============================================================
    public void removeServer(String servname) {
        for (int i = 0; i < nbServers(); i++) {
            TangoServer server = getServer(i);
            if (server.getName().equals(servname)) {
                removeServer(i);
                return;
            }
        }
    }

    //==============================================================
    //==============================================================
    public int nbServers() {
        if (servers == null)
            return 0;
        else
            return servers.size();
    }

    //==============================================================
    //==============================================================
    public String[] getServerAttribute() {
        try {
            DeviceAttribute att = read_attribute("Servers");
            return att.extractStringArray();
        } catch (DevFailed e) {
			System.out.println(name);
            Except.print_exception(e);
            return new String[0];
        }
    }

    //==============================================================
    //==============================================================
    public String readLogFile(String servname) throws DevFailed {
        DeviceData argin = new DeviceData();
        argin.insert(servname);
        DeviceData argout = command_inout("DevReadLog", argin);
        //System.out.println(argout.extractString());
        return argout.extractString();
    }
    //==============================================================

    /**
     * Register server (export/unexport admin device )
     * to be known by starter.
     *
     * @param servname servers name
     * @throws DevFailed in case of server already running
     */
    //==============================================================
    public void registerServer(String servname) throws DevFailed {
        //	Check before if already running
        String devname = "dserver/" + servname;
        boolean running = false;
        DeviceProxy dev;
        try {
            dev = new DeviceProxy(devname);
            dev.ping();
            running = true;
        } catch (DevFailed e) {  /* */}

        if (running) {
            IORdump d = new IORdump(devname);
            Except.throw_exception("StartServerFailed",
                    servname + " is already running on " + d.get_host(),
                    "DevWizard.startServer()");
        }
        DbDevExportInfo info =
                new DbDevExportInfo(devname, "null", name, "null");
        ApiUtil.get_db_obj().export_device(info);
        ApiUtil.get_db_obj().unexport_device(devname);
    }

    //==============================================================
    //==============================================================
    public void startOneServer(String servname) throws DevFailed {
        DeviceData argin = new DeviceData();
        argin.insert(servname);
        System.out.println("command_inout(DevStart, " + servname + ") on " + get_name());
        command_inout("DevStart", argin);
    }

    //==============================================================
    //==============================================================
    public void startServer(String servname) throws DevFailed {
        DeviceData argin = new DeviceData();
        argin.insert(servname);
        command_inout("DevStart", argin);
    }

    //==============================================================
    //==============================================================
    public void stopServer(String servname) throws DevFailed {
        DeviceData argin = new DeviceData();
        argin.insert(servname);
        command_inout("DevStop", argin);
    }

    //==============================================================
    //==============================================================
    public void hardKillServer(String servname) throws DevFailed {
        DeviceData argin = new DeviceData();
        argin.insert(servname);
        command_inout("HardKillServer", argin);
    }

    //==============================================================
    //==============================================================
    public void startServers(int level) throws DevFailed {
        DeviceData argin = new DeviceData();
        argin.insert((short) level);
        command_inout("DevStartAll", argin);
    }

    //==============================================================
    //==============================================================
    public void stopServers(int level) throws DevFailed {
        DeviceData argin = new DeviceData();
        argin.insert((short) level);
        command_inout("DevStopAll", argin);
    }

    //======================================================
    //======================================================
    public void displayUptimes(JFrame parent) {
        try {
            Database db = ApiUtil.get_db_obj();
            DeviceData argIn = new DeviceData();
            argIn.insert(name);
            DeviceData argOut = db.command_inout("DbGetHostServerList", argIn);
            String[] serverNames = argOut.extractStringArray();
            List<String[]> lines = new ArrayList<>();
            for (String severName : serverNames) {

                String[] exportedStr = new TangoServer("dserver/" + severName).getServerUptime();
                lines.add(new String[]{
                        severName, exportedStr[0], exportedStr[1]});
            }

            String[] columns = new String[]{"Server", "Last   exported", "Last unexported"};
            PopupTable ppt = new PopupTable(parent, name, columns, lines, new Dimension(650, 250));
            ppt.setVisible(true);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }

    //==============================================================
    //==============================================================
    public void displayLogging(JFrame parent) {
        displayLogging(parent, null);
    }

    //==============================================================
    //==============================================================
    public void displayLogging(Component parent, String filter) {
        try {
            ////	new LoggingDialog(parent, this).setVisible(true);

            DeviceData argIn = new DeviceData();
            argIn.insert("Starter");
            DeviceData argOut = command_inout("DevReadLog", argIn);
            String str = argOut.extractString();
            String[] array = AstorUtil.string2array(str, "\n");
            List<String[]> lines = new ArrayList<>();
            String prev_date = null;
            for (String line : array) {
                String[] words = AstorUtil.string2array(line);
                //	Swap servers and action
                String server = words[2];
                words[2] = words[3];
                words[3] = server;
                //	filter with server name if any
                if (filter == null || server.equals(filter)) {
                    //	Check if date changed
                    if (prev_date != null) {
                        if (!words[0].equals(prev_date))
                            lines.add(new String[]{"-", "-", "-", "-"});
                    }
                    prev_date = words[0];
                    lines.add(words);
                }
            }
            if (lines.size() > 0) {
                String[] columnNames = {"Date", "Time", "Action", "Server"};
                PopupTable table;
                if (parent instanceof JFrame)
                    table = new PopupTable((JFrame) parent, "Starter on " + name, columnNames, lines);
                else
                    table = new PopupTable((JDialog) parent, "Starter on " + name, columnNames, lines);
                table.setColumnWidth(new int[]{70, 70, 70, 250});
                table.setSortAvailable(false);
                table.setVisible(true);
            } else {
                String desc = "no record found";
                if (filter != null)
                    desc += "  for  " + filter;
                Except.throw_exception("", desc, "");
            }
        } catch (DevFailed e) {
            Utils.popupError(parent, e.errors[0].desc);//"Cannot read Starter logging...");
        }
    }

    //==============================================================
    //==============================================================
    public void displayInfo(java.awt.Component parent) {
        String str = "";
        //	Query database for Controlled servers list
        try {
            if (starter == null)
                starter = new TangoServer(adm_name);
            str += starter.getServerInfo(parent, (state == AstorDefs.all_ok));
            str += "\n\n----------- Controlled servers -----------\n";

            Database db = ApiUtil.get_db_obj();
            DeviceData argin = new DeviceData();
            argin.insert(name);
            DeviceData argout = db.command_inout("DbGetHostServerList", argin);
            String[] servnames = argout.extractStringArray();

            //	Query database for control mode.
            for (String servname : servnames) {
                DbServInfo s = db.get_server_info(servname);
                //	store only controlled servers
                if (s.controlled)
                    str += s.name + "\n";
            }
        } catch (DevFailed e) {
            str += e.errors[0].desc;
            ErrorPane.showErrorMessage(parent, str, e);
            return;
        }
        str += "\n\n";
        Utils.popupMessage(parent, str);
    }

    //==============================================================
    //==============================================================
    public void testStarter(java.awt.Component parent) {
        try {
            if (starter == null)
                starter = new TangoServer(adm_name);
            starter.testDevice(parent);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, "", e);
        }
    }

    //==============================================================
    //==============================================================
    @SuppressWarnings("unused")
    public void unExportStarter(java.awt.Component parent) {
        try {
            //	Check if exported
            DbDevImportInfo info = import_device();
            if (!info.exported) {
                Utils.popupError(parent,get_name() + "  NOT  exported !");
                return;
            }

            //	Unexport device
            unexport_device();
            //	And administrative device
            String adm = "dserver/Starter/" + name;
            new DeviceProxy(adm).unexport_device();

            //	Stop polling because it is not exported
            Utils.popupMessage(parent,
                    adm + "   and    " + get_name() +
                            "\n\n       have been unexported !");
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, "", e);
        }
    }

    //==============================================================
    //==============================================================
    public void setCollection(String new_collec) throws DevFailed {
        DbDatum[] prop = new DbDatum[1];
        prop[0] = new DbDatum(AstorDefs.collec_property, new_collec);

        put_property(prop);
        collection = new_collec;
    }

    //==============================================================
    //==============================================================
    public String getName() {
        return name;
    }

    //===============================================================
    //===============================================================
    public void startServer(java.awt.Component parent, String servname) {
        try {
            startServer(servname);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, "", e);
        }
    }

    //===============================================================
    //===============================================================
    void readStdErrorFile(java.awt.Frame parent, String servname) {
        try {
            String logStr = readLogFile(servname);

            //	Get size to know if scrollable is necessary
            //------------------------------------------------
            PopupText dialog = new PopupText(parent, true);
            dialog.show(logStr, 700, 500);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, "", e);
        } catch (Exception e) {
            ErrorPane.showErrorMessage(parent, "", e);
            e.printStackTrace();
        }
    }

    //==============================================================
    //==============================================================
    void updateServersList(JFrame parent) {
        try {
            command_inout("UpdateServersInfo");
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, "", e);
        }
    }
    //==============================================================

    /**
     * Awake thread to read host.
     */
    //==============================================================
    void updateData() {
        thread.updateData();
    }

    //==============================================================
    //==============================================================
    void stopThread() {
        thread.stop_it = true;
        thread.updateData();
    }

    //==============================================================
    //==============================================================
    String[] getPath() {
        String[] path = {""};
        try {

            DbDatum datum = get_property("StartDsPath");
            if (!datum.is_empty())
                path = datum.extractStringArray();
        } catch (DevFailed e) {
            Except.print_exception(e);
        }
        return path;
    }

    //==============================================================
    //==============================================================
    String getFamily() {
        String family = "";
        try {

            DbDatum datum = get_property("HostCollection");
            if (!datum.is_empty())
                family = datum.extractString();
        } catch (DevFailed e) {
            Except.print_exception(e);
        }
        return family;
    }

    //==============================================================
    //==============================================================
    public void displayBlackBox(JFrame parent) {
        String[] devices = {this.get_name(), this.adm_name};
        String choice;
        if ((choice = (String) JOptionPane.showInputDialog(parent,
                "Device selection :", "",
                JOptionPane.INFORMATION_MESSAGE, null,
                devices, devices[0])) != null) {
            try {
                new BlackBoxTable(parent, choice).setVisible(true);
            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(parent, null, e);
            }
        }
    }

    //==============================================================
    //==============================================================
    public String hostStatus() {
        String str = name + ":";
        try {
            if (state == AstorDefs.faulty)
                str += "     is faulty\n";
            else {
                DeviceAttribute att = read_attribute("Servers");
                if (att.hasFailed())
                    str += "     " + att.getErrStack()[0].desc + "\n";
                else {
                    str += "\n";
                    List<String> running = new ArrayList<>();
                    List<String> moving = new ArrayList<>();
                    List<String> stopped = new ArrayList<>();
                    String[] list = att.extractStringArray();
                    for (String line : list) {
                        StringTokenizer stk = new StringTokenizer(line);
                        String name = stk.nextToken();
                        String st = stk.nextToken();
                        String str_ctrl = stk.nextToken();
                        if (str_ctrl.equals("1")) {
                            switch (st) {
                                case "FAULT":
                                    stopped.add(name);
                                    break;
                                case "MOVING":
                                    moving.add(name);
                                    break;
                                default:
                                    running.add(name);
                                    break;
                            }
                        }
                    }
                    if (stopped.size() > 0)
                        str += "     " + stopped.size() + "  servers stopped\n";
                    if (moving.size() > 0)
                        str += "     " + moving.size() + " servers moving\n";
                    if (running.size() > 0)
                        str += "     " + running.size() + " servers running\n";

                }
            }
        } catch (DevFailed e) {
            str += "     " + e.errors[0].desc;
        }
        return str;
    }

    //==============================================================
    //==============================================================
    public String hostName() {
        return name;
    }

    public String toString() {
        if (usage == null || usage.length() == 0)
            return name;
        else
            return name + "  ( " + usage + " )";
    }
    //==============================================================
    //==============================================================
}
