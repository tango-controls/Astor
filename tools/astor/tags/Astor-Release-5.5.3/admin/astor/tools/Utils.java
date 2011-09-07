//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011
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


/** 
 *	This class is able to
 *
 * @author  verdier
 */
 
import fr.esrf.tangoatk.core.ATKException;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;


public class  Utils
{
	static private Utils	instance = null;
    static public final String img_path = "/admin/astor/images/";
	//===============================================================
	//===============================================================
	private Utils()
	{
	}
	//===============================================================
	//===============================================================
	static public Utils getInstance()
	{
		if (instance==null)
			instance = new Utils();
		return instance;
	}
	//===============================================================
	//===============================================================
	public ImageIcon getIcon(String filename)
	{
		java.net.URL	url =
			getClass().getResource(img_path+filename);
		if (url==null) {
			System.err.println("WARNING:  " + img_path + filename + " : File not found");
			return new ImageIcon();
		}
		return new ImageIcon(url);
	}
    //===============================================================
	//===============================================================
    static public void popupMessage(Component c, String message, String filename)
    {
        ImageIcon	icon = new ImageIcon(c.getClass().getResource(filename));
        JOptionPane.showMessageDialog(c, message, "Info Window", JOptionPane.INFORMATION_MESSAGE, icon);
    }
    //===============================================================
	//===============================================================
    static public void popupMessage(Component c, String message)
    {
        JOptionPane.showMessageDialog(c, message, "Info Window", JOptionPane.INFORMATION_MESSAGE);
    }
    //===============================================================
	//===============================================================
    static public void popupError(Component c, String message, Exception e)
    {
        ErrorPane.showErrorMessage(c, message, e);
    }
    //===============================================================
	//===============================================================
    static public void popupError(Component c, String message)
    {
        try {
            throw new ATKException(message);
        }
        catch (ATKException e) {
            ErrorPane.showErrorMessage(c, null, e);
        }
    }
    //===============================================================
	//===============================================================
}
