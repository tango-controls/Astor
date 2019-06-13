//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
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
// $Revision: 19878 $
//
//-======================================================================


package admin.astor.access;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.StringTokenizer;


class UserGroup extends ArrayList <String> {
    private String name;
    private static final Font   font = new Font("Dialog", Font.BOLD, 16);
    public static final String unsorted =  "Unsorted Users";
    //===============================================================
    //===============================================================
    public UserGroup(String name) {
        this.name = name;
    }
    //===============================================================
    //===============================================================
    public UserGroup(String name, String grpNames, String[] users) {
        this.name = name;
        StringTokenizer stk = new StringTokenizer(grpNames, ",");
        while (stk.hasMoreTokens()) {
            String member = stk.nextToken().trim();
            //  Check if exists as user
            for (String user : users) {
                if (member.equals(user)) {
                    add(member);
                }
            }
        }
        sortMembers();
    }
    //===============================================================
    //===============================================================
    public Font getFont() {
        return  font;
    }
    //===============================================================
    //===============================================================
    public void sortMembers() {
        this.sort(new UserComparator());
    }
    //===============================================================
    //===============================================================
    public String getName() {
        return name;
    }
    //===============================================================
    //===============================================================
    public String toString() {
        return name;
    }
    //===============================================================
    //===============================================================
    public static void setUserGroupsToDatabase(Component component, List<UserGroup> groups) {
        //  Build the property string
        List<String>   arrayList = new ArrayList<>();
        for (UserGroup group : groups) {
            if (group.size()>0 && !group.getName().equals(unsorted)) {
                StringBuilder   sb = new StringBuilder();
                sb.append(group.getName()).append(':');
                for (int i=0 ; i<group.size() ; i++) {
                    sb.append(group.get(i));
                    if (i<group.size()-1)
                        sb.append(',');
                }
                arrayList.add(sb.toString());
            }
        }
        if (arrayList.isEmpty())
            return;

        //  Write it in database
        String[]    array = new String[arrayList.size()];
        for (int i=0 ; i<arrayList.size() ; i++)
            array[i] = arrayList.get(i);
        try {
            DbDatum datum = new DbDatum("UserGroups");
            datum.insert(array);
            ApiUtil.get_db_obj().put_property("CtrlSystem", new DbDatum[] { datum });
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(component, null, e);
        }
    }
    //===============================================================
    //===============================================================
    public static List<UserGroup> getUserGroupsFromDatabase(String[] users) {
        List<UserGroup>    groups = new ArrayList<>();
        try {
            DbDatum datum = ApiUtil.get_db_obj().get_property("CtrlSystem", "UserGroups");
            if (!datum.is_empty()) {
                //  Get property as array of String
                String[] lines = datum.extractStringArray();

                //  For each line (each group)
                for (String line : lines) {
                    //  Separate name and users and check syntax
                    StringTokenizer stk = new StringTokenizer(line, ":");
                    if (stk.countTokens()!=2) {
                        Except.throw_exception("BadSyntax",
                                "Bad syntax property in \'" + line + "\'",
                                "UserTree.getUserGroupsFromDatabase()");
                    }
                    //  Split group members and buid groups
                    String grpName = stk.nextToken().trim();
                    String grpNames = stk.nextToken().trim();

                    UserGroup   group = new UserGroup(grpName, grpNames, users);
                    if (group.size()>0) {
                        groups.add(group);
                    }
                }
            }
        }
        catch (DevFailed e) {
            System.err.println(e.errors[0].desc);
        }
        return groups;
    }
    //===============================================================
    //===============================================================


    //===============================================================
    //===============================================================
    private class UserComparator implements Comparator<String> {
        public int compare(String s1, String s2) {

            if (s1 == null)
                return 1;
            else if (s2 == null)
                return -1;
            else
                return s1.compareTo(s2);
        }
    }
}