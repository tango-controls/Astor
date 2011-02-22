//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the OneTool class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
//
// Copyright 1995 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;


/** 
 *	This class is able to define a tool object to be instancied 
 *	using introspection classes.
 *
 * @author  verdier
 * @Revision 
 */
 
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;

import javax.swing.JFrame;



public class  OneTool
{
	String	name;
	String	classname;
	JFrame	jframe = null;
	//===============================================================
	//===============================================================
	public OneTool(String name, String classname)
	{
		this.name      = name;
		this.classname = classname;
	}
	//===============================================================
	//===============================================================
	public void setJFrame(JFrame jframe)
	{
		this.jframe = jframe;
	}
	//===============================================================
	//===============================================================
	
}
