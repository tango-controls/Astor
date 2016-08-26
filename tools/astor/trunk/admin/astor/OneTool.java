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

import javax.swing.*;

/**
 *	This class is able to define a tool object to be instancied
 *	using introspection classes.
 *
 * @author verdier
 */
public class OneTool {
    String name;
    String classname;
    JFrame jframe = null;

    //===============================================================
    //===============================================================
    public OneTool(String name, String classname) {
        this.name = name;
        this.classname = classname;
    }

    //===============================================================
    //===============================================================
    public void setJFrame(JFrame jframe) {
        this.jframe = jframe;
    }
    //===============================================================
    //===============================================================

}
