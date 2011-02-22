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
// Revision 3.2  2004/02/11 08:49:37  pascal_verdier
// A user name for rlogin as been added as Astor property.
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
// Revision 3.0  2003/06/04 12:37:52  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.1  2003/06/04 12:33:11  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;


/** 
 *	This class is a thread to open a window with a
 *	remote login to a remote host.
 *
 * @author  verdier
 * @Revision 
 */
 
 
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

public class RemoteLoginThread extends Thread implements AstorDefs
{
	private Component	parent;
	private String	hostname;

	//======================================================================
	/**
	 *	Thread constructor.
	 *
	 *	@param	hostname	Host to do the remote login.
	 *	@param	parent		parent component used to display error message.
	 */
	//======================================================================
	public RemoteLoginThread(String hostname, Component parent)
	{
		this.hostname = hostname;
		this.parent   = parent;
	}


	//======================================================================
	/**
	 *	Running thread method.
	 */
	//======================================================================
	public void run()
	{
		String	cmd = new String("xterm -sb -title " + hostname);
		//	Check if rlogin user is defined
		String	remlog = AstorUtil.getRloginCmd();
		String	user   = AstorUtil.getRloginUser();

		if (remlog==null)
		{
			if (user==null)
				remlog = "telnet";
			else
				remlog = "rlogin";
		}

		//	if user -> add user name in title
		if (user!=null)
			cmd += "_" + user;

		//	build command
		cmd += " -e " + remlog + " " + hostname;
		if (user!=null)
			cmd += " -l " + user;
		
		try
		{
			Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
		}
		catch(Exception ex)
		{
			System.out.println(ex);
			JOptionPane.showMessageDialog(parent,
										ex.toString(),
										"Error Window",
										JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
