//+======================================================================
// :  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// : pascal_verdier $
//
// Copyright (C) :      2004,2005,...................,2018,2019
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
// :  $
//
//-======================================================================

package admin.astor.dev_state_viewer;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbServer;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import java.util.ArrayList;


/**
 * This class models a TANGO server
 *
 * @author verdier
 */

public class TangoServer extends ArrayList<TangoClass> {
    private String name;
    //===============================================================
    //===============================================================
    public TangoServer(String name) throws DevFailed {
        this.name = name;
        DbServer server = new DbServer(name);
        String[] classNames = server.get_class_list();
        for (String className : classNames) {
            TangoClass tangoClass = new TangoClass(className);
            String[] deviceNames = server.get_device_name(className);
            for (String deviceName : deviceNames)
                tangoClass.add(new TangoDevice(deviceName));
            add(tangoClass);
        }
    }
    //===============================================================
    //===============================================================
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name + ":\n");
        for (TangoClass tangoClass : this)
            sb.append(tangoClass).append('\n');
        return sb.toString();
    }
    //===============================================================
    //===============================================================




    //=======================================================
    /**
     * @param args the command line arguments
     */
    //=======================================================
    public static void main(String[] args) {
        try {
            if (args.length==0) {
                ErrorPane.showErrorMessage(null, null, new Exception("Server name ?"));
                System.exit(0);
            }
            System.out.println(new TangoServer(args[0]));
        }
        catch(DevFailed e) {
            ErrorPane.showErrorMessage(null, null, e);
            System.exit(0);
        }
    }
    //=======================================================
    //=======================================================

}
