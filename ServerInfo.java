//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the Pogo class definition .
//
// $Author$
//
// $Version: $
//
// $Log$
// Revision 3.3  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;
 


/** 
 *	This class inherite from TangApi.DbServInfo class
 *	just to override toString method
 *
 * @author  verdier
 */


import fr.esrf.TangoApi.*;


//===============================================================
/**
 *	A Dialog Class to get the State parameters.
 */
//===============================================================
public class ServerInfo extends DbServInfo
{

	//===============================================================
	//===============================================================
	public ServerInfo(DbServInfo info)
	{
		super(info.name, info.host, info.controlled, info.startup_level);
	}
	//===============================================================
	//===============================================================
	public String toString()
	{
		return name.substring(name.indexOf('/')+1) + "  (" + host+")";
	}
}

