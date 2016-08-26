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

import fr.esrf.TangoApi.DbServInfo;

/**
 *	This class inherite from TangApi.DbServInfo class
 *	just to override toString method
 *
 * @author verdier
 */
public class ServerInfo extends DbServInfo {

    //===============================================================
    //===============================================================
    public ServerInfo(DbServInfo info) {
        super(info.name, info.host, info.controlled, info.startup_level);
    }

    //===============================================================
    //===============================================================
    public String toString() {
        return name.substring(name.indexOf('/') + 1) + "  (" + host + ")";
    }
}

