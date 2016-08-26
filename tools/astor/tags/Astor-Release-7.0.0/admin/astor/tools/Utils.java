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


package admin.astor.tools;

import fr.esrf.tangoatk.core.ATKException;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;

/**
 *	This class is a set of tools
 *
 * @author verdier
 */
public class Utils {
    static private Utils instance = null;
    static public final String img_path = "/admin/astor/images/";

    //===============================================================
    //===============================================================
    private Utils() {
    }

    //===============================================================
    //===============================================================
    static public Utils getInstance() {
        if (instance == null)
            instance = new Utils();
        return instance;
    }

    //===============================================================
    //===============================================================
    public ImageIcon getIcon(String fileName) {
        java.net.URL url = getImageUrl(fileName);
        if (url == null) {
            System.err.println("WARNING:  " + img_path + fileName + " : File not found");
            return new ImageIcon();
        }
        return new ImageIcon(url);
    }

    //===============================================================
    //===============================================================
    public Cursor getCursor(String fileName) {
        java.net.URL url = getImageUrl(fileName);
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        return Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), fileName);
    }
    //===============================================================
    //===============================================================
    public java.net.URL getImageUrl(String filename) {
        return getClass().getResource(img_path + filename);
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
    static public void popupMessage(Component c, String message, String filename) {
        ImageIcon icon = getInstance().getIcon(filename);
        JOptionPane.showMessageDialog(c, message, "Info Window", JOptionPane.INFORMATION_MESSAGE, icon);
    }

    //===============================================================
    //===============================================================
    static public void popupMessage(Component c, String message) {
        JOptionPane.showMessageDialog(c, message, "Info Window", JOptionPane.INFORMATION_MESSAGE);
    }

    //===============================================================
    //===============================================================
    static public void popupError(Component c, String message, Exception e) {
        ErrorPane.showErrorMessage(c, message, e);
    }

    //===============================================================
    //===============================================================
    static public void popupError(Component c, String message) {
        ErrorPane.showErrorMessage(c, null, new ATKException(message));
    }
    //===============================================================
    //===============================================================
}
