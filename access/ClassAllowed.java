//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the ClassAllowed class definition .
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
 
import java.util.*;
import fr.esrf.TangoApi.CommandInfo;



public class  ClassAllowed extends Vector
{
	String		name;
	//===============================================================
	//===============================================================
	ClassAllowed(String name)
	{
		this.name = name;
		//	State and Status always allowed
		add("State");
		add("Status");
	}
	//===============================================================
	//===============================================================
	ClassAllowed(String name, String[] cmd)
	{
		this.name = name;
		for (String str : cmd)
			add(str);
	}
	//===============================================================
	//===============================================================
	String[] getNotAllowed(CommandInfo[] info_array)
	{
		Vector	v = new Vector();
		for (CommandInfo info : info_array)
		{
			boolean found = false;
			String	cmd = info.cmd_name.toLowerCase();
			for (int i=0 ; !found && i<size() ; i++)
			{
				String	allowed_cmd = getCommandAt(i).toLowerCase();
				found = (cmd.equals(allowed_cmd));
			}
			if (!found)
				v.add(info.cmd_name);
		}
		String[]	list = new String[v.size()];
		for (int i=0 ; i<v.size() ; i++)
			list[i] = (String)v.get(i);
		return list;
	}
	//===============================================================
	//===============================================================
	public String getCommandAt(int i)
	{
		return (String)get(i);
	}
	//===============================================================
	//===============================================================
	public String[] getAllowedCmdProperty()
	{
		admin.astor.AstorUtil.getInstance().sort(this);
		//	Do not return State and Status
		String[]	str = new String[size()-2];
		for (int i=0, j=0 ; i<size() ; i++)
		{
			String	cmd = getCommandAt(i);
			if (cmd.toLowerCase().equals("state")==false &&
				cmd.toLowerCase().equals("status")==false)
				str[j++] = getCommandAt(i);
		}
		return str;
	}
	//===============================================================
	//===============================================================
	public String toString()
	{
		return name;
	}
	//===============================================================
	//===============================================================
}
