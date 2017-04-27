//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the TANGO client/server API.
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// $Revision: 25896 $
//
//-======================================================================


package admin.astor.tango_release;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class JTangoVersion {

    private List<ManifestModule>   modules = new ArrayList<>();

    private static String jarFileName;
    private static int    jarFileType;
    private static final String nameHeader    = "artifactId=";
    private static final String versionHeader = "version=";
    private static final String packageHeader = "/META-INF/maven/";
    private static final String manifestName  = "pom.properties";
    private static final String[] packages = {
            "org.tango/JavaTangoIDL",
            "org.tango/TangORB",
            "org.tango/JTangoServer",
            "org.zeromq/jeromq",
            //"JacORB",
            "org.slf4j/slf4j-api",
    };

    public static final int ERR = 0;
    public static final int JTANGO = 0;
    public static final int TANGORB = 1;
    public static final String[] JarUsed = { "JTango", "TangORB" };
    private static JTangoVersion instance = null;
    //========================================================================
    /*
     *	Constructor analysing  JTango jar file manifest modules.
     */
    //========================================================================
    private JTangoVersion() {
        jarFileType = getTangoJarUsed();
        if(jarFileName != null) {
            jarFileName = getRealFileName(jarFileName);

            if (jarFileType == JTANGO) {
                for (String pack : packages) {
                    String packageName = packageHeader + pack + "/" + manifestName;
                    ManifestModule module = getManifestModule(packageName);
                    if (module != null)
                        modules.add(module);
                }
            }
        }
    }
    //========================================================================
    //========================================================================
    public static JTangoVersion getInstance() {
        if (instance==null)
            instance = new JTangoVersion();
        return instance;
    }
    //========================================================================
    //========================================================================
    public String getJarFileName() {
        return jarFileName;
    }

    //========================================================================
    //========================================================================
    public int getJarFileType() {
        return jarFileType;
    }
    //========================================================================
    //========================================================================
    private String getRealFileName(String jarName) {
        try {
            return new File(jarName).getCanonicalPath();
        }
        catch (IOException e) {
            return jarName;
        }
    }
    //========================================================================
    //========================================================================
    private ManifestModule getManifestModule(String packageName) {
        ManifestModule manifestModule = null;
        try {
            InputStream inputStream = getClass().getResourceAsStream(packageName);
            if (inputStream==null) {
                System.err.println("Failed to get the inputStream for file resource :\t  " + manifestName);
                return null;
            }
            InputStreamReader reader = new InputStreamReader(inputStream);
            char[]  buff = new char[256];
            int nb = reader.read(buff);
            if (nb>0) {
                String code = new String(buff);
                StringTokenizer stk = new StringTokenizer(code, "\n");
                String version = null;
                String name = null;
                String date = null;
                while (stk.hasMoreTokens()) {
                    String line = stk.nextToken().trim();   // remove \r
                    if (line.startsWith(nameHeader))
                        name = line.substring(nameHeader.length());
                    else if (line.startsWith(versionHeader))
                        version = line.substring(versionHeader.length());
                    else
                    if (line.startsWith("#") && !line.startsWith("#Gen"))
                        date = line.substring(1);   // remove '#'
                }
                if (name!=null && version!=null)
                    manifestModule = new ManifestModule(name, version, date);
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return manifestModule;
    }
    //========================================================================
    //========================================================================
    private int getTangoJarUsed() {
        String classpath = System.getProperty("java.class.path");
        String separator = System.getProperty("path.separator");
        //  Split classpath to check jar file used.
        List<String> paths = new ArrayList<>();
        StringTokenizer stk = new StringTokenizer(classpath, separator);
        while (stk.hasMoreTokens()) {
            paths.add(stk.nextToken());
        }

        //  Check which one is used.
        if (isJarUsed(paths, JarUsed[JTANGO]))
            return JTANGO;
        else
        if (isJarUsed(paths, JarUsed[TANGORB]))
            return TANGORB;
        else
            return ERR;
    }
    //========================================================================
    //========================================================================
    private static boolean isJarUsed(List<String> paths, String jarName) {
        for (String path : paths) {
            if (path.contains(jarName+".jar")) { //  Generic jac (link)
                jarFileName = path;
                return true;
            }
            if (path.contains(jarName+"-")) { //  Followed by revision number
                jarFileName = path;
                return true;
            }
        }
        return false;
    }
    //========================================================================
    //========================================================================
    private String addTabulation(String str, int start, int maxLen) {
        StringBuilder   sb = new StringBuilder();
        for (int i=start ; i<maxLen+2 ; i++)
            sb.append(" ");
        sb.append(str);
        return sb.toString();
    }
    //========================================================================
    //========================================================================
    public String toString() {
        if (jarFileType==TANGORB)
            return getJarFileName();

        //  JTango --> build a table with name, version and date
        StringBuilder   sb = new StringBuilder(getJarFileName()+"\n\n");
        int maxNameLen = 0;
        int maxVersionLen = 0;
        for (ManifestModule module : modules) {
            if (module.name.length()>maxNameLen)
                maxNameLen = module.name.length();
            if (module.version.length()>maxVersionLen)
                maxVersionLen = module.version.length();
        }

        for (ManifestModule module : modules) {
            sb.append(module.name).append(":")
                    .append(addTabulation(module.version, module.name.length(), maxNameLen))
                    .append(addTabulation(module.date, module.version.length(), maxVersionLen))
                    .append('\n');
        }
        return sb.toString().trim();
    }

    //========================================================================
    //========================================================================
    public static void main(String[] args) {
        JTangoVersion tangoVersion = new JTangoVersion();
        System.out.println(tangoVersion);
    }
    //========================================================================
    //========================================================================




    //========================================================================
    //========================================================================
    private class ManifestModule {
        String name;
        String version;
        String date = "";
        //====================================================================
        private ManifestModule(String name, String version, String date) {
            this.name = name;
            this.version = version;
            if (date!=null)
                this.date = splitDate(date);
        }
        //====================================================================
        private String  splitDate(String date) {
            StringTokenizer stk = new StringTokenizer(date);
            List<String> tokens = new ArrayList<>();
            while (stk.hasMoreTokens())
                tokens.add(stk.nextToken());
            return tokens.get(2) + " " + tokens.get(1) + " " + tokens.get(5);
        }
        //====================================================================
        public String toString() {
            return name + ":  " + version;
        }
    }
    //========================================================================
    //========================================================================
}
