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

import java.util.ArrayList;


/**
 * This class models a TANGO class
 *
 * @author verdier
 */

public class TangoClass extends ArrayList<TangoDevice> {
    private String name;
    //===============================================================
    //===============================================================
    public TangoClass(String name) {
        this.name = name;
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
        StringBuilder sb = new StringBuilder("    " + name + ":\n");
        for (TangoDevice device : this)
            sb.append(device).append('\n');
        return sb.toString();
    }
    //===============================================================
    //===============================================================
}
