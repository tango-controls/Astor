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
import fr.esrf.TangoApi.DeviceProxy;

import javax.swing.*;

/**
 * This class models a TANGO device
 *
 * @author verdier
 */

public class TangoDevice {
    private String name;
    private StateCell stateCell;
    //===============================================================
    //===============================================================
    public TangoDevice(String name) {
        this.name = name;
    }
    //===============================================================
    //===============================================================
    public void createStateCell(JTable table) throws DevFailed {
        stateCell = new StateCell(name+"/State", table);
    }
    //===============================================================
    //===============================================================
    public String getStatus() throws DevFailed {
        return new DeviceProxy(name).read_attribute("Status").extractString();
    }
    //===============================================================
    //===============================================================
    public StateCell getStateCell() {
        return stateCell;
    }
    //===============================================================
    //===============================================================
    public String getName() {
        return name;
    }
    //===============================================================
    //===============================================================
    @Override
    public String toString() {
        return "        " + name;
    }
    //===============================================================
    //===============================================================
}
