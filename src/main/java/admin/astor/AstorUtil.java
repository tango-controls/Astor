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

import admin.astor.tools.MySqlUtil;
import admin.astor.tools.Utils;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.ErrSeverity;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.JSmoothProgressBar;
import fr.esrf.tangoatk.widget.util.Splash;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.*;
import java.util.List;

/**
 *	This class group many info and methods used By Astor.
 *
 * @author verdier
 */
@SuppressWarnings("WeakerAccess")
public class AstorUtil implements AstorDefs {

    private static DbClass _class = null;
    private static AstorUtil instance = null;

    //	Variables will be set by properties
    private static String starterDeviceHeader = "tango/admin/";
    private static boolean superTango = false;
    private static short readInfoPeriod = 5;
    private static short nbStartupLevels = 5;
    private static String rloginCmd = null;
    private static String rloginUser = null;
    private static String[] tools = null;
    private static String[] known_tango_hosts = null;
    private static Dimension preferred_size = new Dimension(400, 600);
    private static Dimension host_dlg_preferred_size = new Dimension(800, 500);
    private static String[] lastCollections = null;
    private static boolean jiveReadOnly = false;
    private static boolean starterStartup = true;
    private static boolean properties_read = false;
    private static String[] helps;

    private static final String starterStartupPropName = "StartServersAtStartup";
    private static final String[] astorPropertyNames = {
            "RloginCmd",
            "RloginUser",
            "JiveReadOnly",
            "LastCollections",
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
        String str = System.getenv("SUPER_TANGO");
        if (str != null) {
            superTango = str.equals("true");
        } else {
            str = System.getProperty("SUPER_TANGO");
            if (str != null)
                superTango = str.equals("true");
        }
        try {
            DbDatum datum = ApiUtil.get_db_obj().get_class_property("Starter", "Domain");
            if (!datum.is_empty())
                starterDeviceHeader = datum.extractString()+"/admin/";
        }
        catch (DevFailed e) {
            //  Just print error, Astor will not start
            System.err.println(e.errors[0].desc);
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
    public static String getStarterDeviceHeader() {
        return starterDeviceHeader;
    }
    //===============================================================
    //===============================================================
    public static String getControlSystemName() throws DevFailed {
        DbDatum datum = ApiUtil.get_db_obj().get_property("CtrlSystem", "Name");
        if (!datum.is_empty())
            return  datum.extractString();
        return null;
    }
    //===============================================================
    //===============================================================
    public static void setControlSystemName(String name) throws DevFailed {
        DbDatum datum = new DbDatum("Name");
        datum.insert(name);
        ApiUtil.get_db_obj().put_property("CtrlSystem", new DbDatum[] { datum });
    }
    //===============================================================
    //===============================================================
    public void initIcons() {
        state_icons[unknown] = Utils.getInstance().getIcon("greyball.gif");
        state_icons[faulty]  = Utils.getInstance().getIcon("redball.gif");
        state_icons[alarm]   = Utils.getInstance().getIcon("orangebal.gif");
        state_icons[all_ok]  = Utils.getInstance().getIcon("greenbal.gif");
        state_icons[all_off] = Utils.getInstance().getIcon("whiteball.gif");
        state_icons[moving]  = Utils.getInstance().getIcon("blueball.gif");
        state_icons[long_moving]  = Utils.getInstance().getIcon("orangeTriangle.png", 0.36);
        state_icons[failed]  = Utils.getInstance().getIcon("failed.gif");
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
        List<String> list = new ArrayList<>();
        while (stk.hasMoreTokens())
            list.add(stk.nextToken());
        return list.toArray(new String[list.size()]);
    }

    //===============================================================
    //===============================================================
    /*
    static boolean getDebug() {
        if (!properties_read)
            readAstorProperties();
        return debug;
    }
    */
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
    public static String[] getDbaseKnownTangoHosts() {
        if (!properties_read)
            readAstorProperties();
        return known_tango_hosts;
    }
    //===============================================================
    //===============================================================
    public static List<String> getUserKnownTangoHosts() {
        String astorRC = getAstorRC();
        return getFromEnvFile("KnownTangoHosts", astorRC);
    }
    //===============================================================
    //===============================================================
    public static List<String> getAllKnownTangoHosts() {

        //  Get tango hosts from database
        String[]        csTangoHosts = getDbaseKnownTangoHosts();
        List<String>    list = new ArrayList<>();
        if (csTangoHosts!=null)
            Collections.addAll(list, csTangoHosts);

        //  Get tango hosts from user file and merge
        List<String>    userTangoHosts = getUserKnownTangoHosts();
        for (String userTH : userTangoHosts) {
            boolean exists = false;
            if (csTangoHosts!=null) {
                //  Check if already exists from database
                for (String csTH : csTangoHosts)
                    if (csTH.equals(userTH))
                        exists = true;
            }
            if (!exists)
                list.add(userTH);
        }

        return list;
    }
    //===============================================================
    //===============================================================
    private static String getAstorRC()  {
        String  astorRC;
        String home = System.getProperty("user.home");
        if (osIsUnix())  {
            astorRC = home+"/."+rcFileName;
        }
        else {	//	WIN 32
            astorRC = home+"/"+rcFileName;
        }
        return astorRC;
    }
    //===============================================================
    //===============================================================
    public static void saveUserKnownTangoHost(List<String> list) throws DevFailed {

        //  Build the line
        final String tag = "KnownTangoHosts:";
        StringBuilder  sb = new StringBuilder(tag+"  ");
        for (String host : list) {
            sb.append(host).append(", ");
        }
        //  Remove last ','
        String  tangoHosts = sb.substring(0, sb.length()-2) + '\n';

        //  Get existing file content
        String  astorRC = getAstorRC();
        String  code;
        try {
            code = readFile(astorRC);
        }
        catch (DevFailed e) {
            code = "#\n#  Astor (TANGO Manager) configuration file\n#\n#\n";
        }

        //  And insert line
        int start = code.indexOf(tag);
        if (start<0) {
            code +=tangoHosts;
        }
        else {
            int end = code.indexOf('\n', start);
            if (end<0) {
                code = code.substring(0, start) + tangoHosts;
            }
            else {
                code = code.substring(0, start) + tangoHosts + code.substring(end);
            }
        }
        writeFile(astorRC, code);
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings("SameParameterValue")
    private static List<String> getFromEnvFile(String propertyName, String fileName) {
        List<String>    list = new ArrayList<>();
        try {
            //  Get file content
            String	code = readFile(fileName);
            StringTokenizer stk = new StringTokenizer(code, "\n");
            List<String>    lines = new ArrayList<>();
            while (stk.hasMoreTokens())  {
                String line = stk.nextToken().trim();
                if (!line.startsWith("#"))  {
                    lines.add(line);
                }
            }

            //  Get property value
            for (String line : lines) {
                if (line.startsWith(propertyName + ":")) {
                    //  Get value part
                    String s = line.substring(propertyName.length() + 1).trim();
                    stk = new StringTokenizer(s, ",");
                    while (stk.hasMoreTokens())
                        list.add(stk.nextToken().trim());
                    //  OK, -> can return now
                    return list;
                }
            }
        }
        catch(DevFailed e) {
            //System.err.println(e);
        }
        return list;
    }
    //===============================================================
    //===============================================================
    private static String hostInfoClassName = null;
    public static String getHostInfoClassName() {
        if (hostInfoClassName==null) {
            hostInfoClassName = "";
            try {
                DbDatum datum = ApiUtil.get_db_obj().get_property("Astor", "_HostInfo");
                if (!datum.is_empty())
                    hostInfoClassName = datum.extractString();
            }
            catch (DevFailed e) {/* */}
        }
        return hostInfoClassName;
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

            if (nb > 0)
                str = new String(inStr);
        } catch (Exception e) {
            Except.throw_exception("READ_FAILED", e.toString());
        }
        return str;
    }
    //===============================================================
    /**
     * Open a file and return text read as lines.
     *
     * @param filename file to be read.
     * @return the file content read as lines.
     * @throws fr.esrf.Tango.DevFailed in case of failure during read file.
     */
    //===============================================================
    @SuppressWarnings("UnusedDeclaration")
    public static List<String> readFileLines(String filename) throws DevFailed {
        List<String>   lines = new ArrayList<>();
        try {
            String str = readFile(filename);
            StringTokenizer stk = new StringTokenizer(str, "\n");
            while (stk.hasMoreTokens())
                lines.add(stk.nextToken());
        } catch (Exception e) {
            Except.throw_exception("READ_FAILED", e.toString());
        }
        return lines;
    }
    //===============================================================
    //===============================================================
    public static void writeFile(String filename, String code) throws DevFailed {
        try {
            FileOutputStream fid = new FileOutputStream(filename);
            fid.write(code.getBytes());
            fid.close();
        } catch (Exception e) {
            Except.throw_exception("WRITE_FAILED", e.toString());
        }
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
    private static String getStringProperty(DbDatum datum) {
        if (datum.is_empty())
            return null;
        else
            return datum.extractString();
    }
    //===============================================================
    //===============================================================
    private static String[] getStringArrayProperty(DbDatum datum) {
        if (datum.is_empty())
            return null;
        else
            return datum.extractStringArray();
    }
    //===============================================================
    //===============================================================
    static public void readAstorProperties() {
        try {
            //	get Astor Property
            DbDatum[] data = ApiUtil.get_db_obj().get_property("Astor", astorPropertyNames);
            String   s;
            String[] array;
            int i = 0;
            rloginCmd = getStringProperty(data[i++]);
            rloginUser = getStringProperty(data[i++]);
            s = getStringProperty(data[i++]);
            if (s!=null)
                jiveReadOnly = (s.equals("true")||s.equals("1"));

            lastCollections = getStringArrayProperty(data[i++]);
            known_tango_hosts = getStringArrayProperty(data[i++]);
            array = getStringArrayProperty(data[i++]);
            try {
                if (array!=null && array.length>=2) {
                    int width = Integer.parseInt(array[0]);
                    int height = Integer.parseInt(array[1]);
                    preferred_size = new Dimension(width, height);
                }
            } catch (Exception e) { /* */ }

            array = getStringArrayProperty(data[i++]);

            try {
                if (array!=null && array.length>=2) {
                    int width = Integer.parseInt(array[0]);
                    int height = Integer.parseInt(array[1]);
                    host_dlg_preferred_size = new Dimension(width, height);
                }
            } catch (Exception e) { /* */ }

            tools = getStringArrayProperty(data[i++]);
            helps = getStringArrayProperty(data[i]);

            //  Get Starter startup mode property
            DbClass dbClass = new DbClass("Starter");
            DbDatum datum = dbClass.get_property(starterStartupPropName);
            if (!datum.is_empty())
                starterStartup = datum.extractBoolean();

            properties_read = true;
        } catch (DevFailed e) { /* Do Nothing */}
    }

    //===============================================================
    //===============================================================
    static void putAstorProperties() throws DevFailed {
        //	get Astor Property
        DbDatum[] data = new DbDatum[astorPropertyNames.length];
        int i = 0;
        data[i] = new DbDatum(astorPropertyNames[i], rloginCmd); i++;
        data[i] = new DbDatum(astorPropertyNames[i], rloginUser); i++;
        data[i] = new DbDatum(astorPropertyNames[i], jiveReadOnly); i++;
        data[i] = new DbDatum(astorPropertyNames[i], lastCollections); i++;
        data[i] = new DbDatum(astorPropertyNames[i], known_tango_hosts); i++;
        data[i] = new DbDatum(astorPropertyNames[i], new String[]  {
                Integer.toString(preferred_size.width),
                Integer.toString(preferred_size.height),
        }); i++;

        data[i] = new DbDatum(astorPropertyNames[i], new String[] {
                Integer.toString(host_dlg_preferred_size.width),
                Integer.toString(host_dlg_preferred_size.height),
        }); i++;
        data[i] = new DbDatum(astorPropertyNames[i], tools); i++;
        data[i] = new DbDatum(astorPropertyNames[i], helps);

        ApiUtil.get_db_obj().put_property("Astor", data);

        //  Add Starter startup mode class property
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
    public String[] getLastCollectionList() {
        if (!properties_read)
            readAstorProperties();
        return lastCollections;
    }

    //===============================================================
    //===============================================================
    public void setLastCollectionList(String[] lcl) {
        lastCollections = lcl;
    }

    //===============================================================
    //===============================================================
    public List<String> getCollectionList() throws DevFailed {
        return getCollectionList(getTangoHostList());
    }
    //===============================================================
    //===============================================================
    public List<String> getCollectionList(TangoHost[] hosts) {
        List<String> list = new ArrayList<>();
        for (TangoHost host : hosts) {
            //  Check if collection property is defined.
            if (host.collection == null)
                host.collection = "Miscellaneous";

            //  Check if this collection already exists
            boolean found = false;
            for (int j = 0; j < list.size() && !found; j++)
                found = (host.collection.equals(list.get(j)));

            //	If not already exists add it
            if (!found)
                list.add(host.collection);
        }

        //	Sort for alphabetical order
        Collections.sort(list, new StringComparator());

        //	Add database as first element
        list.add(0, "Tango Database");

        //	Check if default Bottom collection
        String[] lasts = getLastCollectionList();

        //	Check if collections exist
        if (lasts != null) {
            for (String last : lasts) {
                boolean found = false;
                for (int j = 0; !found && j < list.size(); j++) {
                    //	 put it at end of vector
                    String collection = list.get(j);
                    if (last!=null && collection!=null){
                        if (found=last.equals(list.get(j))) {
                            list.remove(j);
                            list.add(collection);
                        }
                    }
                }
            }
        }
        return list;
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
            devinfo = mysql.getHostDevImportInfo(starterDeviceHeader+"%");
            adminfo = mysql.getHostDevImportInfo("dserver/starter/%");
        }

        //	And create TangoHost array object
        List<TangoHost> hosts = new ArrayList<>();
        int idx = 0;
        for (String hostName : hostNames) {
            if (db_server_idl_4) {
                //	Check to be sure the admin correspond to the device
                DbDevImportInfo deviceInfo = getDevImportInfo(hostName, devinfo);
                DbDevImportInfo adminInfo  = getDevImportInfo(hostName, adminfo);
                if (deviceInfo!=null) {
                    if (!deviceInfo.exported || adminInfo==null)
                        hosts.add(new TangoHost(hostName, false));
                    else {
                        //	Create the device proxies with 2 info
                        //  Because event info is unused in case ZMQ events.
                        hosts.add(new TangoHost(deviceInfo, adminInfo));
                    }
                }
                else {
                    System.err.println("----------> " + hostName);
                }

            }
            else
                hosts.add(new TangoHost(hostName, true));
        }
        if (db_server_idl_4)
            MySqlUtil.getInstance().manageTangoHostProperties(hosts);
        return hosts.toArray(new TangoHost[hosts.size()]);
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
    /**
     * Get the devices controlled by Starter DS
     * and return the hosts list.
     *
     * @return controlled host list
     * @throws fr.esrf.Tango.DevFailed in case of database connection failed.
     */
    //===============================================================
    public String[] getHostControlledList() throws DevFailed {
        return ApiUtil.get_db_obj().get_device_member(starterDeviceHeader+"*");
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
    private static void getStarterClassProperties() {
        try {
            _class = new DbClass("Starter");

            String[] propNames = {
                    "NbStartupLevels",
                    "ReadInfoDbPeriod",
                    "doc_url",
                    "appli_doc_url"
            };
            DbDatum[] properties = _class.get_property(propNames);
            int i = 0;
            if (!properties[i].is_empty())
                nbStartupLevels = properties[i].extractShort();
            i++;
            if (!properties[i].is_empty())
                readInfoPeriod = properties[i].extractShort();
            readInfoPeriod *= 1000;    //	sec -> ms
        } catch (DevFailed e) {
            Except.print_exception(e);
        }
    }

    //===============================================================
    //===============================================================
    static String[] getServerClassProperties(String classname) {
        String[] result = new String[3];
        try {
            DbClass dbclass = new DbClass(classname);
            String[] propNames = {"ProjectTitle",
                    "Description",
                    "doc_url"};
            String[] desc;
            DbDatum[] prop = dbclass.get_property(propNames);
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
        List<String> list = new ArrayList<>();
        while ((idx = str.indexOf("\n")) > 0) {
            list.add(str.substring(0, idx));
            str = str.substring(idx + 1);
        }
        list.add(str);
        return list.toArray(new String[list.size()]);
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
        System.out.println(cmd);
        Process process = Runtime.getRuntime().exec(cmd);

        // get command output stream and
        // put a buffered reader input stream on it.
        InputStream inputStream = process.getInputStream();
        new BufferedReader(new InputStreamReader(inputStream));

        // do not read output lines from command
        // Do not check its exit value
    }
    //===============================================================
    /**
     *	Execute a shell command and throw exception if command failed.
     *
     *	@param cmd	shell command to be executed.
     */
    //===============================================================
    public static String executeShellCmd(String cmd)
            throws IOException, InterruptedException, DevFailed  {
        Process process = Runtime.getRuntime().exec(cmd);

        // get command output stream and
        // put a buffered reader input stream on it.
        InputStream inputStream = process.getInputStream();
        BufferedReader br =
                new BufferedReader(new InputStreamReader(inputStream));
        String	sb = "";

        // read output lines from command
        String str;
        while ((str = br.readLine()) != null) {
            //System.out.println(str);
            sb += str+"\n";
        }

        // wait for end of command
        process.waitFor();

        // check its exit value
        int retVal;
        if ((retVal=process.exitValue()) != 0) {
            //	An error occurs try to read it
            InputStream errorStream = process.getErrorStream();
            br = new BufferedReader(new InputStreamReader(errorStream));
            while ((str = br.readLine()) != null) {
                System.out.println(str);
                sb += str+"\n";
            }
            Except.throw_exception("ExecFailed",
                    "the shell command\n" + cmd + "\nreturns : " + retVal + " !\n\n" + sb);
        }
        //System.out.println(sb);
        return sb;
    }
    //===============================================================
    //===============================================================



    //===============================================================
    //===============================================================
    static public boolean osIsUnix() {
        return !System.getProperty("os.name").toLowerCase().startsWith("windows");
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

        // Verify if class Desktop is supported :
        if (Desktop.isDesktopSupported()) {
            // get desktop instance
            Desktop desktop = Desktop.getDesktop();
            // Verify if browse feature is supported
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    // launch associated application
                    desktop.browse(new URI(url));
                } catch (Exception e) {
                    ErrorPane.showErrorMessage(new JFrame(), null, e);
                }
            }
        }
        else {
            //  Check for browser
            String browser;
            if (AstorUtil.osIsUnix())
                browser = "firefox - turbo";
            else
                browser = "explorer";
            String cmd = browser + " " + url;
            try {
                executeShellCmdAndReturn(cmd);
            } catch (Exception e) {
                ErrorPane.showErrorMessage(new JFrame(), null, e);
            }
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
        String s = System.getenv("UseSplash");
        if (s!=null && s.equals("false")) return;

        //	Create a splash window.
        JSmoothProgressBar myBar = new JSmoothProgressBar();
        myBar.setStringPainted(true);
        myBar.setBackground(Color.lightGray);
        myBar.setProgressBarColors(Color.gray, Color.lightGray, Color.darkGray);

        if (tango_icon == null)
            tango_icon =  Utils.getInstance().getIcon("TangoLogo.gif");
        splash = new Splash(tango_icon, Color.black, myBar);
        splash.setTitle(title);
        splash.setMessage("Starting....");
        splash_progress = 0;
        splash.setAlwaysOnTop(true);
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
                splash_progress = 10;
            splash.progress(splash_progress);
            splash.setMessage(message);
        }
    }
    //===============================================================
    //===============================================================
    public static void increaseSplashProgress(double ratio, String message) {
        if (splash != null) {
            splash_progress = (int)(100*ratio);
            if (splash_progress > 99)
                splash_progress = 10;
            splash.progress(splash_progress);
            splash.setMessage(message);
        }
    }
        //===============================================================
        //===============================================================
    public static void setSplashMessage(String message) {
        if (splash!=null)
            splash.setMessage(message);
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
    public void sortTangoServer(ArrayList<TangoServer> list) {
        Collections.sort(list, new TangoServerComparator());
    }

    //===============================================================
    //===============================================================
    public void sort(ArrayList<String> arrayList) {
        Collections.sort(arrayList, new StringComparator());
    }
    //===============================================================
    //===============================================================
    public void startExternalApplication(String className, String stringParameter) throws DevFailed {
        try {
            //	Retrieve class name
            Class	_class = Class.forName(className);
            boolean found = false;

            //	And build object
            Constructor[] constructors = _class.getDeclaredConstructors();
            for (Constructor constructor : constructors) {
                Class[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length==2 &&
                        parameterTypes[0]==JFrame.class && parameterTypes[1]==String.class) {
                    ((Component) constructor.newInstance(new JFrame(), stringParameter)).setVisible(true);
                    found = true;
                }
            }
            if (!found)
                throw new Exception("Cannot find constructor for " + className);
        }
        catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                InvocationTargetException   ite = (InvocationTargetException) e;
                Throwable   throwable = ite.getTargetException();
                System.out.println(throwable.getMessage());
                if (throwable instanceof DevFailed)
                    throw (DevFailed) throwable;
            }
            Except.throw_exception(e.toString(), e.toString(), "AstorUtil.startExternalApplication()");
        }
    }
    //===============================================================
    //===============================================================
    public void startExternalApplication(String className, String[] stringParameters) throws DevFailed {
        try {
            //	Retrieve class name
            Class	_class = Class.forName(className);
            boolean found = false;

            //	And build object
            Constructor[] constructors = _class.getDeclaredConstructors();
            for (Constructor constructor : constructors) {
                Class[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length==2 &&
                        parameterTypes[0]==JFrame.class && parameterTypes[1]==String[].class) {
                    ((Component) constructor.newInstance(new JFrame(), stringParameters)).setVisible(true);
                    found = true;
                }
            }
            if (!found)
                throw new Exception("Cannot find constructor for " + className);
        }
        catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                InvocationTargetException   ite = (InvocationTargetException) e;
                Throwable   throwable = ite.getTargetException();
                System.out.println(throwable.getMessage());
                if (throwable instanceof DevFailed)
                    throw (DevFailed) throwable;
            }
            Except.throw_exception(e.toString(), e.toString(), "AstorUtil.startExternalApplication()");
        }
    }
    //===============================================================
    //===============================================================




    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        //AstorUtil.getAllKnownTangoHosts();
        /*
        try {
            AstorUtil.getInstance().startExternalApplication("host_info.HostStatus", "l-c01-1");
        }
        catch (DevFailed e) {
            Except.print_exception(e);
        }
        */
    }
    //===============================================================
    //===============================================================


    //======================================================
    /**
     * Comparators class to sort collection
     */
    //======================================================
    class StringComparator implements Comparator<String> {
        public int compare(String s1, String s2) {

            if (s1 == null)
                return 1;
            else if (s2 == null)
                return -1;
            else
                return s1.compareTo(s2);
        }
    }
    //======================================================
    class TangoServerComparator implements Comparator<TangoServer> {
        public int compare(TangoServer s1, TangoServer s2) {

            if (s1 == null)
                return 1;
            else if (s2 == null)
                return -1;
            else
                return s1.getName().compareTo(s2.getName());
        }
    }
}
