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

import javax.swing.*;
import java.awt.*;

class Utils {
    static private Utils instance = null;

    //===============================================================
    //===============================================================
    private Utils() {
    }

    //===============================================================
    //===============================================================
    static Utils getInstance() {
        if (instance == null)
            instance = new Utils();
        return instance;
    }

    //===============================================================
    //===============================================================
    ImageIcon getIcon(String filename) {
        return admin.astor.tools.Utils.getInstance().getIcon(filename);
    }

    //===============================================================
    //===============================================================
    public ImageIcon getIcon(String filename, double ratio) {
        ImageIcon icon = getIcon(filename);
        return getIcon(icon, ratio);
    }

    //===============================================================
    //===============================================================
    public ImageIcon getIcon(ImageIcon icon, double ratio) {
        if (icon != null) {
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();

            width = (int) (ratio * width);
            height = (int) (ratio * height);

            icon = new ImageIcon(
                    icon.getImage().getScaledInstance(
                            width, height, Image.SCALE_SMOOTH));
        }
        return icon;
    }
    //===============================================================
    //===============================================================
}
