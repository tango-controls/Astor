//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the HostInfoDialogVector class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.3  2003/11/07 09:58:46  pascal_verdier
// Host info dialog automatic resize implemented.
//
// Revision 3.2  2003/10/20 08:55:15  pascal_verdier
// Bug on tree popup menu position fixed.
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
// Revision 3.0  2003/06/04 12:37:53  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.1  2003/06/04 12:33:12  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.0  2003/01/16 15:22:35  verdier
// Last ci before CVS usage
//
//
// Copyright 1995 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;
 
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;



class  HostInfoDialogVector extends Vector
{
	//===============================================================
	//===============================================================
	HostInfoDialogVector()
	{
		super();
	}
	//===============================================================
	//===============================================================
	HostInfoDialog getByHostName(TangoHost host)
	{
		HostInfoDialog	hid = null;
		for (int i=0 ; i<size() ; i++)
		{
			HostInfoDialog	tmp = (HostInfoDialog) elementAt(i);
			if (host.getName().equals(tmp.name))
				hid = tmp;
		}
		return hid;
	}
	//===============================================================
	//===============================================================
	HostInfoDialog add(Astor parent, TangoHost host, Point p)
	{
		//	Set the servers polling and Notify to awake the thread.
		host.poll_serv_lists = true;
		host.updateData();
		//	And wait a bit before re-build panel
		try { Thread.sleep(500); } catch(Exception e){}

		//	Search if already exists
		HostInfoDialog	hid = getByHostName(host);
		//	If does not exists, create a new one and add it in vector
		if (hid==null)
		{
			hid = new HostInfoDialog(parent, host);
			add(hid);

			if (p!=null)
			{
				//	Set position to display
				p.translate(5,5);
				hid.setLocation(p);
			}
		}
		else
			hid.updatePanel();
		hid.show();

		return hid;
	}
	//===============================================================
	//===============================================================
	void close(TangoHost host)
	{
		//	Search if already exists
		HostInfoDialog	hid = getByHostName(host);
		//	If do exists, close it
		if (hid!=null)
			hid.doClose(-1);
	}
	//===============================================================
	//===============================================================
}
