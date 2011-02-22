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
// Revision 3.11  2006/04/19 12:06:58  pascal_verdier
// Host info dialog modified to use icons to display server states.
//
// Revision 3.10  2005/12/01 10:00:23  pascal_verdier
// Change TANGO_HOST added (needs TangORB-4.7.7 or later).
//
// Revision 3.9  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.8  2005/10/20 13:24:49  pascal_verdier
// Screen position management has been changed.
//
// Revision 3.7  2005/03/11 14:07:54  pascal_verdier
// Pathes have been modified.
//
// Revision 3.6  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.5  2004/07/09 08:12:49  pascal_verdier
// HostInfoDialog is now awaken only on servers change.
//
// Revision 3.4  2004/07/08 11:22:58  pascal_verdier
// First revision able to use events.
//
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
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;
 
import java.awt.*;
import java.util.Vector;



public class  HostInfoDialogVector extends Vector
{
	private	Point		position = null;
	private static int	step = 10;
	//===============================================================
	//===============================================================
	HostInfoDialogVector()
	{
		super();
	}
	//===============================================================
	//===============================================================
	void setDialogPreferredSize(Dimension d)
	{
		for (Object o : this)
			((HostInfoDialog)o).setDialogPreferredSize(d);
	}
	//===============================================================
	//===============================================================
	void close()
	{
		for (Object o : this)
		{
			((HostInfoDialog)o).setVisible(false);
			((HostInfoDialog)o).dispose();
		}
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
	HostInfoDialog add(Astor parent, TangoHost host)
	{
		if (position==null)
			position = parent.getLocationOnScreen();
		//	Set the servers polling and Notify to awake the thread.
		host.poll_serv_lists = true;
		host.updateData();
		//	And wait a bit before re-build panel
		try { Thread.sleep(500); } catch(Exception e){}

		//	Search if already exists
		host.info_dialog = getByHostName(host);
		//	If does not exists, create a new one and add it in vector
		if (host.info_dialog==null)
		{
			host.info_dialog = new HostInfoDialog(parent, host);
			add(host.info_dialog);

			//	Set position to display
			position.translate(step,step);
			host.info_dialog.setLocation(position);
		}
		else
			host.info_dialog.updatePanel();
		host.info_dialog.setVisible(true);

		return host.info_dialog;
	}
	//===============================================================
	//===============================================================
	void close(TangoHost host)
	{
		//	Search if already exists
		HostInfoDialog	hid = getByHostName(host);
		//	If do exists, close it
		if (hid!=null)
			hid.doClose();
	}
	//===============================================================
	//===============================================================
}
