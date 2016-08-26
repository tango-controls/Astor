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


package admin.astor.access;

import fr.esrf.TangoApi.CommandInfo;

import java.util.ArrayList;
import java.util.List;

/**
 *	This class is able to define an allowed command
 *
 * @author verdier
 */
public class ClassAllowed extends ArrayList<String> {
    String name;

    //===============================================================
    //===============================================================
    ClassAllowed(String name) {
        this.name = name;
        //	State and Status always allowed
        add("State");
        add("Status");
    }

    //===============================================================
    //===============================================================
    ClassAllowed(String name, String[] cmd) {
        this.name = name;
        for (String str : cmd)
            add(str);
    }

    //===============================================================
    //===============================================================
    String[] getNotAllowed(CommandInfo[] info_array) {
        List<String> notAllowedList = new ArrayList<>();
        for (CommandInfo info : info_array) {
            boolean found = false;
            String cmd = info.cmd_name.toLowerCase();
            for (int i = 0; !found && i < size(); i++) {
                String allowed_cmd = getCommandAt(i).toLowerCase();
                found = (cmd.equals(allowed_cmd));
            }
            if (!found)
                notAllowedList.add(info.cmd_name);
        }
        return notAllowedList.toArray(new String[notAllowedList.size()]);
    }

    //===============================================================
    //===============================================================
    public String getCommandAt(int i) {
        return get(i);
    }

    //===============================================================
    //===============================================================
    public String[] getAllowedCmdProperty() {
        admin.astor.AstorUtil.getInstance().sort(this);
        //	Do not return State and Status
        String[] str = new String[size() - 2];
        for (int i = 0, j = 0; i < size(); i++) {
            String cmd = getCommandAt(i);
            if (!cmd.toLowerCase().equals("state") &&
                !cmd.toLowerCase().equals("status"))
                str[j++] = getCommandAt(i);
        }
        return str;
    }

    //===============================================================
    //===============================================================
    public String toString() {
        return name;
    }
    //===============================================================
    //===============================================================
}
