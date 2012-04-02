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


package admin.astor;


/**
 *	This class group many info and methods used By Astor.
 *
 * @author verdier
 */


import admin.astor.tools.MySqlUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.ErrSeverity;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoApi.events.DbEventImportInfo;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.JSmoothProgressBar;
import fr.esrf.tangoatk.widget.util.Splash;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class AstorUtil implements AstorDefs {

    private static DbClass _class = null;
    private static AstorUtil instance = null;

    //	Variables will be set by properties
    //---------------------------------------------
    private static boolean superTango = false;
    private static short readInfoPeriod = 5;
    private static short nbStartupLevels = 5;
    private static String rloginCmd = null;
    private static String rloginUser = null;
    private static String[] tools = null;
    private static String[] known_tango_hosts = null;
    private static Dimension preferred_size = new Dimension(400, 600);
    private static Dimension host_dlg_preferred_size = new Dimension(800, 500);
    private static String[] last_collec = null;
    private static boolean jiveReadOnly = false;
    private static boolean starterStartup = true;
    private static String serverHelpURL = "http://www.esrf.fr/computing/cs/tango/";
    private static String appliHelpURL = "http://www.esrf.fr/computing/cs/tango/";
    private static boolean properties_read = false;
    private static boolean debug = false;
    private static String[] helps;

    MyCompare compare;

    private static final String starterStartupPropName = "StartServersAtStartup";
    private static final String[] astor_propnames = {
            "Debug",
            "LastCollections",
            "RloginCmd",
            "RloginUser",
            "JiveReadOnly",
            "KnownTangoHosts",
            "PreferredSize",
            "HostDialogPreferredSize",
            "Tools",
            "HtmlHelps"
    };
    public static ImageIcon[] state_icons = new ImageIcon[NbStates];

    //===============================================================
    //===============================================================
    private AstorUtil() {
        compare = new MyCompare();
        String str = System.getenv("SUPER_TANGO");
        if (str != null) {
            superTango = str.equals("true");
        } else {
            str = System.getProperty("SUPER_TANGO");
            if (str != null)
                superTango = str.equals("true");
        }
    }

    //===============================================================
    //===============================================================
    public static AstorUtil getInstance() {
        if (instance == null)
            instance = new AstorUtil();
        return instance;
    }

    //===============================================================
    //===============================================================
    public void initIcons() {
        state_icons[unknown] = new ImageIcon(getClass().getResource(img_path + "greyball.gif"));
        state_icons[faulty] = new ImageIcon(getClass().getResource(img_path + "redball.gif"));
        state_icons[alarm] = new ImageIcon(getClass().getResource(img_path + "orangebal.gif"));
        state_icons[all_ok] = new ImageIcon(getClass().getResource(img_path + "greenbal.gif"));
        state_icons[moving] = new ImageIcon(getClass().getResource(img_path + "blueball.gif"));
        state_icons[failed] = new ImageIcon(getClass().getResource(img_path + "failed.gif"));
    }

    //===============================================================
    //===============================================================
    public boolean isSuperTango() {
        return superTango;
    }

    //===============================================================
    //===============================================================
    static String[] string2array(String str) {
        return string2array(str, null);
    }

    //===============================================================
    //===============================================================
    static String[] string2array(String str, String separ) {
        StringTokenizer stk;
        if (separ == null)
            stk = new StringTokenizer(str);
        else
            stk = new StringTokenizer(str, separ);
        ArrayList<String> v = new ArrayList<String>();
        while (stk.hasMoreTokens())
            v.add(stk.nextToken());
        String[] array = new String[v.size()];
        for (int i = 0; i < v.size(); i++)
            array[i] = v.get(i);
        return array;
    }

    //===============================================================
    //===============================================================
    static boolean getDebug() {
        if (!properties_read)
            readAstorProperties();
        return debug;
    }

    //===============================================================
    //===============================================================
    public static String getRloginCmd() {
        if (!properties_read)
            readAstorProperties();
        return rloginCmd;
    }

    //===============================================================
    //===============================================================
    public static void setRloginCmd(String s) {
        rloginCmd = s;
    }

    //===============================================================
    //===============================================================
    public static String getRloginUser() {
        if (!properties_read)
            readAstorProperties();
        return rloginUser;
    }

    //===============================================================
    //===============================================================
    public static void setRloginUser(String s) {
        rloginUser = s;
    }

    //===============================================================
    //===============================================================
    public static void setTools(String[] t) {
        tools = t;
    }

    //===============================================================
    //===============================================================
    public static String[] getTools() {
        if (!properties_read)
            readAstorProperties();
        return tools;
    }

    //===============================================================
    //===============================================================
    public static void setHtmlHelps(String[] h) {
        helps = h;
    }

    //===============================================================
    //===============================================================
    public static String[] getHtmlHelps() {
        if (!properties_read)
            readAstorProperties();
        return helps;
    }

    //===============================================================
    //===============================================================
    public static String[] getKnownTangoHosts() {
        if (!properties_read)
            readAstorProperties();
        return known_tango_hosts;
    }

    //===============================================================
    //===============================================================
    public static Dimension getPreferredSize() {
        if (!properties_read)
            readAstorProperties();
        return preferred_size;
    }

    //===============================================================
    //===============================================================
    public static void setPreferredSize(Dimension d) {
        preferred_size = d;
    }

    //===============================================================
    //===============================================================
    public static Dimension getHostDialogPreferredSize() {
        if (!properties_read)
            readAstorProperties();
        return host_dlg_preferred_size;
    }

    //===============================================================
    //===============================================================
    public static void setHostDialogPreferredSize(Dimension d) {
        host_dlg_preferred_size = d;
    }

    //===============================================================
    //===============================================================
    public static void setKnownTangoHosts(String[] kth) {
        known_tango_hosts = kth;
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"NestedTryStatement"})
    static public void readAstorProperties() {
        try {
            //	Get database instance
            //----------------------------------
            Database dbase = ApiUtil.get_db_obj();
            //	get Astor Property
            //----------------------------------
            DbDatum[] data = dbase.get_property("Astor", astor_propnames);
            int i = -1;
            if (!data[++i].is_empty())
                debug = data[i].extractBoolean();
            if (!data[++i].is_empty())
                last_collec = data[i].extractStringArray();
            if (!data[++i].is_empty())
                rloginCmd = data[i].extractString();
            if (!data[++i].is_empty())
                rloginUser = data[i].extractString();
            if (!data[++i].is_empty())
                jiveReadOnly = data[i].extractBoolean();
            if (!data[++i].is_empty())
                known_tango_hosts = data[i].extractStringArray();
            if (!data[++i].is_empty()) {
                String[] s = data[i].extractStringArray();
                try {
                    int width = Integer.parseInt(s[0]);
                    int height = Integer.parseInt(s[1]);
                    preferred_size = new Dimension(width, height);
                } catch (NumberFormatException e) { /* */ }
            }
            if (!data[++i].is_empty()) {
                String[] s = data[i].extractStringArray();
                try {
                    int width = Integer.parseInt(s[0]);
                    int height = Integer.parseInt(s[1]);
                    host_dlg_preferred_size = new Dimension(width, height);
                } catch (NumberFormatException e) { /* */ }
            }
            if (!data[++i].is_empty())
                tools = data[i].extractStringArray();
            if (!data[++i].is_empty())
                helps = data[i].extractStringArray();


            //  Get Starter startup mode property
            DbClass dbClass = new DbClass("Starter");
            DbDatum datum = dbClass.get_property(starterStartupPropName);
            if (!datum.is_empty())
                starterStartup = datum.extractBoolean();

            properties_read = true;
        } catch (DevFailed e) { /** Do Nothing */}
    }

    //===============================================================
    //===============================================================
    static void putAstorProperties() throws DevFailed {
        //	Get database instance
        //----------------------------------
        Database dbase = ApiUtil.get_db_obj();
        //	get Astor Property
        //----------------------------------
        DbDatum[] data = new DbDatum[astor_propnames.length];
        int i = 0;
        data[i] = new DbDatum(astor_propnames[i]);
        data[i++].insert(debug);

        data[i] = new DbDatum(astor_propnames[i]);
        data[i++].insert(last_collec);

        data[i] = new DbDatum(astor_propnames[i]);
        data[i++].insert(rloginCmd);

        data[i] = new DbDatum(astor_propnames[i]);
        data[i++].insert(rloginUser);

        data[i] = new DbDatum(astor_propnames[i]);
        data[i++].insert(jiveReadOnly);

        data[i] = new DbDatum(astor_propnames[i]);
        data[i++].insert(known_tango_hosts);

        data[i] = new DbDatum(astor_propnames[i]);
        String[] size_str = {
                Integer.toString(preferred_size.width),
                Integer.toString(preferred_size.height),
        };
        data[i++].insert(size_str);

        data[i] = new DbDatum(astor_propnames[i]);
        String[] host_dlg_size_str = {
                Integer.toString(host_dlg_preferred_size.width),
                Integer.toString(host_dlg_preferred_size.height),
        };
        data[i++].insert(host_dlg_size_str);

        data[i] = new DbDatum(astor_propnames[i]);
        data[i++].insert(tools);

        data[i] = new DbDatum(astor_propnames[i]);
        data[i].insert(helps);

        dbase.put_property("Astor", data);


        //  Add Starter startup mode property
        DbDatum datum = new DbDatum(starterStartupPropName);
        datum.insert(starterStartup);
        DbClass dbClass = new DbClass("Starter");
        dbClass.put_property(new DbDatum[]{datum});
    }

    //===============================================================
    //===============================================================
    void setJiveReadOnly(boolean b) {
        jiveReadOnly = b;
    }

    //===============================================================
    //===============================================================
    void setStarterStartup(boolean b) {
        starterStartup = b;
    }

    //===============================================================
    //===============================================================
    boolean jiveIsReadOnly() {
        if (!properties_read)
            readAstorProperties();
        return jiveReadOnly;
    }

    //===============================================================
    //===============================================================
    boolean getStarterStartup() {
        if (!properties_read)
            readAstorProperties();
        return starterStartup;
    }

    //===============================================================
    //===============================================================
    public static String getStarterPathHome() {
        String path = System.getenv("StarterPathHome");
        if (path == null)
            path = System.getProperty("StarterPathHome");
        if (path != null)
            return path;
        else
            return ".";
    }

    //===============================================================
    //===============================================================
    String[] getLastCollectionList() {
        if (!properties_read)
            readAstorProperties();
        return last_collec;
    }

    //===============================================================
    //===============================================================
    void setLastCollectionList(String[] lcl) {
        last_collec = lcl;
    }

    //===============================================================
    //===============================================================
    ArrayList<String> getCollectionList(TangoHost[] hosts) {
        ArrayList<String> vect = new ArrayList<String>();
        for (TangoHost host : hosts) {
            //  Check if collection property is defined.
            if (host.collection == null)
                host.collection = "Miscellaneous";

            //  Check if this collection already exists
            boolean found = false;
            for (int j = 0; j < vect.size() && !found; j++)
                found = (host.collection.equals(vect.get(j)));

            //	If not already exists add it
            if (!found)
                vect.add(host.collection);
        }

        //	Sort for alphabetical order
        //noinspection unchecked
        Collections.sort(vect, compare);

        //	Add database as first element
        vect.add(0, "Tango Database");

        //	Check if default Bottom collection
        String[] lasts = getLastCollectionList();
        //	Check if collections exist
        if (lasts != null)
            for (String last : lasts) {
                boolean found = false;
                for (int j = 0; !found && j < vect.size(); j++) {
                    //	 put it at end of vector
                    if (found = last.equals(vect.get(j))) {
                        String s = vect.get(j);
                        vect.remove(j);
                        vect.add(s);
                    }
                }
            }
        return vect;
    }

    //===============================================================
    //===============================================================
    public TangoHost[] getTangoHostList() throws DevFailed {
        //	Get hosts list from database
        String[] hostNames = getHostControlledList();

        //	If IDL 4 or greater, read database for all hosts import info
        boolean db_server_idl_4 =
                ApiUtil.get_db_obj().get_idl_version() >= 4;
        DbDevImportInfo[] devinfo = null;
        DbDevImportInfo[] adminfo = null;
        if (db_server_idl_4) {
            MySqlUtil mysql = MySqlUtil.getInstance();
            devinfo = mysql.getHostDevImportInfo("tango/admin/%");
            adminfo = mysql.getHostDevImportInfo("dserver/starter/%");
        }

        //	And create TangoHost array object
        TangoHost[] hosts = new TangoHost[hostNames.length];
        int idx = 0;
        for (String hostName : hostNames) {
            if (db_server_idl_4) {
                //	Check to be sure the admin correspond to the device
                DbDevImportInfo deviceInfo = getDevImportInfo(hostName, devinfo);
                DbDevImportInfo adminInfo  = getDevImportInfo(hostName, adminfo);
                if (!deviceInfo.exported || adminInfo == null)
                    hosts[idx++] = new TangoHost(hostName, !db_server_idl_4);
                else {
                     //	Create the device proxies with 2 info
                    //  Because event info is unused in case ZMQ events.
                  hosts[idx++] = new TangoHost(deviceInfo, adminInfo);
                }
            }
            else
                hosts[idx++] = new TangoHost(hostName, !db_server_idl_4);
        }
        if (db_server_idl_4)
            MySqlUtil.getInstance().manageTangoHostProperties(hosts);
        return hosts;
    }

    //===============================================================
    //===============================================================
    private DbDevImportInfo getDevImportInfo(String hostName, DbDevImportInfo[] importInfos) {
        for (DbDevImportInfo importInfo : importInfos) {
            //	get member name
            int idx = importInfo.name.lastIndexOf('/');
            String  member = importInfo.name.substring(idx+1);

            //  Remove FQDN if any
            idx = member.indexOf('.');
            if (idx>0)
                member = member.substring(0, idx);
            //  Check if specified one
            if (member.toLowerCase().equals(hostName.toLowerCase())) {
                return importInfo;
            }
        }
        return null; //	not found
    }

    //===============================================================
    //===============================================================
    private DbEventImportInfo getEventImportInfo(String admname, DbEventImportInfo[] evtinfo) {
        if (evtinfo != null)
            for (DbEventImportInfo info : evtinfo)
                if (info.name.equals(admname))
                    return info;
        //	not found
        return null;
    }
    //===============================================================

    /**
     * Get the devices controlled by Starter DS
     * and return the hosts list.
     *
     * @return controlled host list
     * @throws fr.esrf.Tango.DevFailed in case of database connection failed.
     */
    //===============================================================
    public String[] getHostControlledList() throws DevFailed {
        //	Get database instance and read host list
        String  debugHosts = System.getenv("DebugHosts");
        if (debugHosts==null) {
            Database dbase = ApiUtil.get_db_obj();
            return dbase.get_device_member("tango/admin/*");
        }
        else {
            StringTokenizer stk = new StringTokenizer(debugHosts, ",");
            ArrayList<String>   list = new ArrayList<String>();
            while (stk.hasMoreElements())
                list.add(stk.nextToken());
            String[]    array = new String[list.size()];
            for (int i=0 ; i<list.size() ; i++) {
                array[i] = list.get(i);
            }
            return array;
        }
    }

    //===============================================================
    //===============================================================
    public static String getTangoHost() {
        String th;
        try {
            th = ApiUtil.getTangoHost();
        } catch (DevFailed e) {
            return null;
        } catch (NoSuchMethodError e) {
            th = System.getProperty("TANGO_HOST");
            if (th == null)
                th = System.getenv("TANGO_HOST");
        }
        return th;
    }

    //===============================================================
    //===============================================================
    public static void setTangoHost(String tango_host) {
        Properties props = System.getProperties();
        props.put("TANGO_HOST", tango_host);
        System.setProperties(props);
        _class = null;
    }

    //===============================================================
    //===============================================================
    public static short getStarterReadPeriod() {
        if (_class == null) {
            getStarterClassProperties();
        }
        return readInfoPeriod;
    }

    //===============================================================
    //===============================================================
    public static short getStarterNbStartupLevels() {
        if (_class == null) {
            getStarterClassProperties();
        }
        return nbStartupLevels;
    }

    //===============================================================
    //===============================================================
    public static String getStarterHelpURL() {
        if (_class == null) {
            getStarterClassProperties();
        }
        return serverHelpURL;
    }

    //===============================================================
    //===============================================================
    public static String getAppliHelpURL() {
        if (_class == null) {
            getStarterClassProperties();
        }
        return appliHelpURL;
    }

    //===============================================================
    //===============================================================
    private static void getStarterClassProperties() {
        try {
            _class = new DbClass("Starter");

            String[] propnames = {
                    "NbStartupLevels",
                    "ReadInfoDbPeriod",
                    "doc_url",
                    "appli_doc_url"
            };
            DbDatum[] properties = _class.get_property(propnames);
            int i = 0;
            if (!properties[i].is_empty())
                nbStartupLevels = properties[i].extractShort();
            i++;
            if (!properties[i].is_empty())
                readInfoPeriod = properties[i].extractShort();
            readInfoPeriod *= 1000;    //	sec -> ms

            i++;
            if (!properties[i].is_empty())
                serverHelpURL = properties[i].extractString();

            i++;
            if (!properties[i].is_empty())
                appliHelpURL = properties[i].extractString();
        } catch (DevFailed e) {
            Except.print_exception(e);
        }
        if (getDebug()) {
            System.out.println("NbStartupLevels:  " + nbStartupLevels);
            System.out.println("ReadInfoDbPeriod: " + readInfoPeriod);
            System.out.println("server doc_url:   " + serverHelpURL);
            System.out.println("appli_doc_url:    " + appliHelpURL);
        }
    }

    //===============================================================
    //===============================================================
    static String[] getServerClassProperties(String classname) {
        String[] result = new String[3];
        try {
            DbClass dbclass = new DbClass(classname);
            String[] propnames = {"ProjectTitle",
                    "Description",
                    "doc_url"};
            String[] desc;
            DbDatum[] prop = dbclass.get_property(propnames);
            if (!prop[0].is_empty())
                result[0] = prop[0].extractString();
            if (!prop[1].is_empty()) {
                //	Get description as string array and convert to string
                desc = prop[1].extractStringArray();
                result[1] = "";
                for (int i = 0; i < desc.length; i++) {
                    result[1] += desc[i];
                    if (i < desc.length - 1)
                        result[1] += "\n";
                }
            }
            if (prop[2].is_empty())
                result[2] = DocLocationUnknown;
            else
                result[2] = prop[2].extractString();
        } catch (DevFailed e) {
            result[0] = result[1] = result[2] = null;
        }
        return result;
    }

    //================================================================
    //================================================================
    public static String strException(Exception except) {
        String str = "";

        if (except instanceof ConnectionFailed)
            str += ((ConnectionFailed) (except)).getStack();
        else if (except instanceof CommunicationFailed)
            str += ((CommunicationFailed) (except)).getStack();
        else if (except instanceof WrongNameSyntax)
            str += ((WrongNameSyntax) (except)).getStack();
        else if (except instanceof WrongData)
            str += ((WrongData) (except)).getStack();
        else if (except instanceof NonDbDevice)
            str += ((NonDbDevice) (except)).getStack();
        else if (except instanceof NonSupportedFeature)
            str += ((NonSupportedFeature) (except)).getStack();
        else if (except instanceof EventSystemFailed)
            str += ((EventSystemFailed) (except)).getStack();
        else if (except instanceof AsynReplyNotArrived)
            str += ((AsynReplyNotArrived) (except)).getStack();
        else if (except instanceof DevFailed) {
            DevFailed df = (DevFailed) except;
            //	True DevFailed
            str += "Tango exception  " + df.toString() + "\n";
            for (int i = 0; i < df.errors.length; i++) {
                str += "Severity -> ";
                switch (df.errors[i].severity.value()) {
                    case ErrSeverity._WARN:
                        str += "WARNING \n";
                        break;

                    case ErrSeverity._ERR:
                        str += "ERROR \n";
                        break;

                    case ErrSeverity._PANIC:
                        str += "PANIC \n";
                        break;

                    default:
                        str += "Unknown severity code";
                        break;
                }
                str += "Desc   -> " + df.errors[i].desc + "\n";
                str += "Reason -> " + df.errors[i].reason + "\n";
                str += "Origin -> " + df.errors[i].origin + "\n";

                if (i < df.errors.length - 1)
                    str += "-------------------------------------------------------------\n";
            }
        } else
            str = except.toString();
        return str;
    }

    //======================================================
    //======================================================
    static public void centerDialog(JDialog dialog, JFrame parent) {
        Point p = parent.getLocationOnScreen();
        p.x += ((parent.getWidth() - dialog.getWidth()) / 2);
        p.y += ((parent.getHeight() - dialog.getHeight()) / 2);
        if (p.y <= 0) p.y = 20;
        if (p.x <= 0) p.x = 20;
        dialog.setLocation(p);
    }

    //======================================================
    //======================================================
    static public void centerDialog(JDialog dialog, JDialog parent) {
        Point p = parent.getLocationOnScreen();
        p.x += ((parent.getWidth() - dialog.getWidth()) / 2);
        p.y += ((parent.getHeight() - dialog.getHeight()) / 2);
        if (p.y <= 0) p.y = 20;
        if (p.x <= 0) p.x = 20;
        dialog.setLocation(p);
    }

    //======================================================
    //======================================================
    static public void rightShiftDialog(JDialog dialog, JFrame parent) {
        Point p = parent.getLocationOnScreen();
        p.x += parent.getWidth();
        p.y += ((parent.getHeight() - dialog.getHeight()) / 2);
        if (p.y <= 0) p.y = 20;
        if (p.x <= 0) p.x = 20;
        dialog.setLocation(p);
    }

    //======================================================
    //======================================================
    static public void rightShiftDialog(JDialog dialog, JDialog parent) {
        Point p = parent.getLocationOnScreen();
        p.x += parent.getWidth();
        p.y += ((parent.getHeight() - dialog.getHeight()) / 2);
        if (p.y <= 0) p.y = 20;
        if (p.x <= 0) p.x = 20;
        dialog.setLocation(p);
    }

    //======================================================
    //======================================================
    static public void cascadeDialog(JDialog dialog, JFrame parent) {
        Point p = parent.getLocationOnScreen();
        p.x += 20;
        p.y += 20;
        dialog.setLocation(p);
    }

    //======================================================
    //======================================================
    static public void cascadeDialog(JDialog dialog, JDialog parent) {
        Point p = parent.getLocationOnScreen();
        p.x += 20;
        p.y += 20;
        dialog.setLocation(p);
    }

    //======================================================
    //======================================================
    static String[] string2StringArray(String str) {
        int idx;
        ArrayList<String> v = new ArrayList<String>();
        while ((idx = str.indexOf("\n")) > 0) {
            v.add(str.substring(0, idx));
            str = str.substring(idx + 1);
        }
        v.add(str);
        String[] result = new String[v.size()];
        for (int i = 0; i < v.size(); i++)
            result[i] = v.get(i);
        return result;
    }
    //===============================================================

    /**
     * Execute a shell command and throw exception if command failed.
     *
     * @param cmd shell command to be executed.
     * @throws java.io.IOException in case of execution failed
     */
    //===============================================================
    public static void executeShellCmdAndReturn(String cmd)
            throws IOException {
        Process proc = Runtime.getRuntime().exec(cmd);

        // get command's output stream and
        // put a buffered reader input stream on it.
        //-------------------------------------------
        InputStream istr = proc.getInputStream();
        new BufferedReader(new InputStreamReader(istr));

        // do not read output lines from command
        // Do not check its exit value
    }
    //===============================================================

    /**
     * Execute a shell command and throw exception if command failed.
     *
     * @param cmd shell command to be executed.
     * @return command execution standard out
     * @throws fr.esrf.Tango.DevFailed        command execution standard error if any
     * @throws java.io.IOException            In case of exec failed
     * @throws java.lang.InterruptedException In case of exec wait failed
     */
    //===============================================================
    public static String executeShellCmd(String cmd)
            throws IOException, InterruptedException, DevFailed {
        Process proc = Runtime.getRuntime().exec(cmd);

        // get command's output stream and
        // put a buffered reader input stream on it.
        //-------------------------------------------
        InputStream istr = proc.getInputStream();
        BufferedReader br =
                new BufferedReader(new InputStreamReader(istr));
        String sb = "";

        // read output lines from command
        //-------------------------------------------
        String str;
        while ((str = br.readLine()) != null) {
            //System.out.println(str);
            sb += str + "\n";
        }

        // wait for end of command
        //---------------------------------------
        proc.waitFor();

        // check its exit value
        //------------------------
        int retVal;
        if ((retVal = proc.exitValue()) != 0) {
            //	An error occured try to read it
            InputStream errstr = proc.getErrorStream();
            br = new BufferedReader(new InputStreamReader(errstr));
            while ((str = br.readLine()) != null) {
                System.out.println(str);
                sb += str + "\n";
            }
            Except.throw_exception("SHELL_CMD_FAILED",
                    "the shell command\n" + cmd + "\nreturns : " + retVal
                            + " !\n\n" + sb,
                    "AstorUtil.executeShellCmd()");
        }
        return sb;
    }

    //===============================================================
    //===============================================================
    static private boolean _osIsUnix = true;
    static private boolean _osIsUnixTested = false;

    static public boolean osIsUnix() {
        if (!_osIsUnixTested) {
            try {
                String os = System.getProperty("os.name");
                //System.out.println("Running under " + os);
                _osIsUnix = !os.toLowerCase().startsWith("windows");
                _osIsUnixTested = true;
            } catch (Exception e) {
                //System.out.println(e);
                _osIsUnix = false;
            }
        }
        return _osIsUnix;
    }

    //===============================================================
    //===============================================================
    public static void testDevice(Component parent, String devname) {
        JDialog d;
        if (parent instanceof JDialog)
            d = new JDialog((JDialog) parent, false);
        else
            d = new JDialog((JFrame) parent, false);
        d.setTitle(devname + " Device Panel");
        try {
            d.setContentPane(new jive.ExecDev(devname));
            ATKGraphicsUtils.centerDialog(d);
            d.setVisible(true);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }

    //===============================================================
    //===============================================================
    public static void showInHtmBrowser(String url) {
        //  Check for browser
        String browser;
        if ((browser = System.getProperty("BROWSER")) == null) {
            if (AstorUtil.osIsUnix())
                browser = "firefox - turbo";
            else
                browser = "explorer";
        }
        String cmd = browser + " " + url;
        try {
            executeShellCmdAndReturn(cmd);
        } catch (Exception e) {
            ErrorPane.showErrorMessage(new JFrame(), null, e);
        }
    }

    //===============================================================
    //===============================================================
    public static String getAccessControlDeviceName() {
        String name;
        try {
            String[] services =
                    ApiUtil.get_db_obj().getServices("AccessControl", "*");

            if (services.length > 0) {
                name = services[0];
            } else {   //  Service does not exist !
                name = null;
            }
        } catch (DevFailed e) {
            name = null;
        }
        return name;
    }
    //===============================================================
    //===============================================================


    //===============================================================
    //===============================================================
    private static Splash splash;
    private static int splash_progress;
    private static ImageIcon tango_icon = null;

    public static void startSplash(String title) {
        //	Create a splash window.
        JSmoothProgressBar myBar = new JSmoothProgressBar();
        myBar.setStringPainted(true);
        myBar.setBackground(Color.lightGray);
        myBar.setProgressBarColors(Color.gray, Color.lightGray, Color.darkGray);

        if (tango_icon == null)
            tango_icon = new ImageIcon(
                    getInstance().getClass().getResource(img_path + "TangoCollaboration.jpg"));
        splash = new Splash(tango_icon, Color.black, myBar);
        splash.setTitle(title);
        splash.setMessage("Starting....");
        splash_progress = 0;
        splash.setVisible(true);
        splash.repaint();
    }

    //=======================================================
    //=======================================================
    public static void stopSplash() {
        if (splash != null) {
            splash_progress = 100;
            splash.progress(splash_progress);
            splash.setVisible(false);
        }
    }

    //===============================================================
    //===============================================================
    public static void increaseSplashProgress(int i, String message) {
        if (splash != null) {
            splash_progress += i;
            if (splash_progress > 99)
                splash_progress = 99;
            splash.progress(splash_progress);
            splash.setMessage(message);
        }
    }


    //===============================================================
    //===============================================================


    //===============================================================
    //===============================================================
    static private RGB rgb = null;

    public void initColors(int nb) {
        if (rgb == null)
            rgb = new RGB(nb);
        else
            rgb.initColor(nb);

    }

    //===============================================================
    public Color getNewColor() {
        if (rgb == null)
            rgb = new RGB();
        return rgb.getNewColor();
    }

    //===============================================================
    //===============================================================
    class RGB {
        int r = 0;
        int g = 0;
        int b = 0;
        int step = 10;

        //===============================================
        RGB() {
        }

        //===============================================
        RGB(int nb) {
            initColor(nb);
        }

        //===============================================
        void initColor(int nb) {
            step = (4 * 0xFF) / nb;
            if (step == 0xFF) step = 0x80;
            red = true;
            green = false;
            blue = false;
            r =
                    g =
                            b = 0;
            //System.out.println("Nb = " + nb + "    step = " +step);
        }

        private boolean red = true;
        private boolean green = false;
        private boolean blue = false;

        //===============================================
        void increase() {

            if (red) {
                if ((r + step) < 0xFF)
                    r += step;
                else {
                    r = 0xFF;
                    red = false;
                    green = true;
                }
            } else if (green) {
                if ((g + step) < 0xFF) {
                    if ((r - step) > 0)
                        r -= step;
                    g += step;
                } else {
                    g = 0xFF;
                    green = false;
                    blue = true;
                }
            } else if (blue) {
                if ((b + step) < 0xFF) {
                    if ((g - step) > 0)
                        g -= step;
                    b += step;
                } else {
                    b = 0xFF;
                    r = 0xFF;
                    blue = false;
                }
            } else if ((r - step) > 0) {
                r -= step;
                b -= step;
            }
            //System.out.println("step = " + step + "  rgb = " + r + " - " + g + " - " + b);
        }

        //===============================================
        Color getNewColor() {
            increase();
            return new Color(r, g, b);
        }
    }


    //===============================================================
    //===============================================================
    @SuppressWarnings({"unchecked"})
    public void sort(ArrayList[] array) {
        for (ArrayList v : array)
            Collections.sort(v, compare);
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"unchecked"})
    public void sort(ArrayList v) {
        Collections.sort(v, compare);
    }
    //===============================================================
    //===============================================================


    //======================================================

    /**
     * MyCompare class to sort collection
     */
    //======================================================
    class MyCompare implements Comparator {
        public int compare(Object o1, Object o2) {
            String s1 = o1.toString().toLowerCase();
            String s2 = o2.toString().toLowerCase();
            if (s1 == null)
                return 1;
            else if (s2 == null)
                return -1;
            else
                return s1.compareTo(s2);
        }
    }
}
