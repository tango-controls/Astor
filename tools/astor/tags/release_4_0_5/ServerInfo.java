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
 * @Revision 
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
	//===============================================================
	//===============================================================
	/*
	public String toString()
	{
		return name + "  (" + host+")";
	}
	*/
	//===============================================================
	//===============================================================
}

