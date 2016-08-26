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

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DbDatum;

/**
 *	This class is a set of tools
 *
 * @author  verdier
 */
 
public class  Utils {
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
    static String getControlSystemName() throws DevFailed {
        DbDatum datum = ApiUtil.get_db_obj().get_property("CtrlSystem", "Name");
        if (datum.is_empty())
            return "";
        else
            return  datum.extractString();
    }
    //===============================================================
    //===============================================================
    static String[] getLastCollectionList() throws  DevFailed {
        DbDatum datum = ApiUtil.get_db_obj().get_property("Astor", "LastCollections");
        if (datum.is_empty()) {
            return new String[] {};
        }
        else
            return datum.extractStringArray();
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
    static String[] getHostControlledList() throws DevFailed {
        return ApiUtil.get_db_obj().get_device_member(AstorUtil.getStarterDeviceHeader()+"*");
    }

}
