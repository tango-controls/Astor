//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the Utils class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 1.2  2011/02/11 10:03:06  pascal_verdier
// Pb with TAC when adding addresses on "All Users" fixed.
// No reference on app_util classes any more.
// Change splash screen image.
//
// Revision 1.1  2008/11/19 10:05:16  pascal_verdier
// Initial revision.
//
//
// Copyleft 2008 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
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
