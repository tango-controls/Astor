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
//
// Copyleft 2008 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor.access;


/** 
 *	This class is able to
 *
 * @author  verdier
 */
 
import javax.swing.ImageIcon;



class  Utils
{
	static private Utils	instance = null;
	//===============================================================
	//===============================================================
	private Utils()
	{
	}
	//===============================================================
	//===============================================================
	static Utils getInstance()
	{
		if (instance==null)
			instance = new Utils();
		return instance;
	}
	//===============================================================
	//===============================================================
	ImageIcon getIcon(String filename)
	{
		java.net.URL	url =
			getClass().getResource("/app_util/img/"+filename);
		if (url==null)
		{
			System.err.println("WARNING:  /app_util/img/"+ filename + " : File not found");
			return new ImageIcon();
		}
		return new ImageIcon(url);
	}
	//===============================================================
	//===============================================================
}
