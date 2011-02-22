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
// Revision 3.27  2008/03/03 14:55:21  pascal_verdier
// Starter Release_4 management.
//
// Revision 3.26  2007/09/11 09:23:29  pascal_verdier
// Db attribute polling panel added.
//
// Revision 3.25  2007/08/20 14:06:11  pascal_verdier
// ServStatePanel added on HostInfoDialog (Check states option).
//
// Revision 3.24  2007/04/04 13:00:27  pascal_verdier
// Database attribute properties editor added.
//
// Revision 3.23  2006/01/11 08:46:14  pascal_verdier
// PollingProfiler added.
//
// Revision 3.22  2005/11/25 08:30:20  pascal_verdier
// Bug in new Device Panel startup fixed.
//
// Revision 3.21  2005/11/24 12:24:57  pascal_verdier
// DevBrowser utility added.
// MkStarter utility added.
//
// Revision 3.20  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.19  2005/10/26 10:32:44  pascal_verdier
// Change test device dialog screen position.
//
// Revision 3.18  2005/10/14 14:29:34  pascal_verdier
// Edit memorized attribute value added.
//
// Revision 3.17  2005/10/03 09:32:06  pascal_verdier
// Check if at least one device is registred in chooseDevice() method.
// New problem since Wizard usage.
//
// Revision 3.16  2005/09/15 08:26:36  pascal_verdier
// Server architecture display addded.
//
// Revision 3.15  2005/09/13 14:28:01  pascal_verdier
// Wizard management added.
//
// Revision 3.14  2005/06/02 09:02:36  pascal_verdier
// Minor changes.
//
// Revision 3.13  2005/03/11 14:07:54  pascal_verdier
// Pathes have been modified.
//
// Revision 3.12  2005/02/10 15:38:19  pascal_verdier
// Event subscritions have been serialized.
//
// Revision 3.11  2004/12/08 09:55:55  pascal_verdier
// Tango 5 feature (classname::devname) compatibility.
//
// Revision 3.10  2004/12/02 10:05:25  pascal_verdier
// Delete html temporary file.
//
// Revision 3.9  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.8  2004/07/08 11:22:58  pascal_verdier
// First revision able to use events.
//
// Revision 3.7  2004/05/04 07:05:27  pascal_verdier
// Bug on notify daemon fixed.
// server reconection transparency added.
//
// Revision 3.6  2004/03/03 08:31:04  pascal_verdier
// The server restart command has been replaced by a stop and start command in a thread.
// The delete startup level info has been added.
//
// Revision 3.5  2004/02/04 14:37:43  pascal_verdier
// Starter logging added
// Database info added on CtrlServersDialog.
//
// Revision 3.4  2003/11/25 15:56:45  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
// Revision 3.3  2003/09/08 11:05:28  pascal_verdier
// *** empty log message ***
//
// Revision 3.2  2003/07/22 14:35:20  pascal_verdier
// Minor bugs fixed.
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
// Revision 3.0  2003/06/04 12:37:52  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.1  2003/06/04 12:33:12  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.0  2003/01/16 15:22:35  verdier
// Last ci before CVS usage
//
// Revision 1.6  2002/09/13 08:43:07  verdier
// Use IDL 2 Starter version (polling thread, State from Starter, ...).
// Host info window not modal.
// Host info window resizable for display all servers option.
// And many features.
//
// Revision 1.5  2001/05/30 15:13:29  verdier
// Start/Stop host control added
// Jive statup aded
// and many app_util added...
//
// Revision 1.4  2001/01/09 14:58:33  verdier
// Start and stop all servers added.
// Progress Monitor added.
//
// Revision 1.3  2000/12/20 09:32:51  verdier
// Compatible with TangoApi package first revision.
//
// Revision 1.2  2000/10/12 08:50:52  verdier
// Hosts and servers are now controlled by threads.
// Commnds could be send to dserver/class server.
//
// Revision 1.1  2000/10/04 14:35:56  verdier
// Initial revision
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;
 
/** 
 *	Server object containing devices list.
 *	This class inherit from device proxy.
 *	It is seen as the administrative device of this server.
 *
 * @author  verdier
 */

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import admin.astor.tools.DbServerArchitecture;
import fr.esrf.tangoatk.widget.util.ErrorPane;


public class TangoServer extends DeviceProxy implements AstorDefs, TangoConst
{
	private String		name;
	private	DbServer	server = null;
	private String		devname;
	private	DevState	state;
	

	public  DbServInfo	info;
	public	JLabel		label;
	//	Replacing info
	public boolean	controlled = false;
	public int		startup_level = 0;

	private static final long	RestartTimeout = 10000;
	//=============================================================
	//=============================================================
	public TangoServer(String name, DevState state) throws DevFailed
	{
		//	Create object
		//---------------------------------
		super("dserver/" + name);
		set_transparency_reconnection(true);
		this.name    = name;
		this.devname = name();	//	Not the server name but
								// the administrative device name
		this.state   = state;
	}
	//=============================================================
	//=============================================================
	public TangoServer(String name) throws DevFailed
	{
		//	Create object
		//---------------------------------
		super(name);
		set_transparency_reconnection(true);
		this.name    = name;
		this.devname = name();	//	Not the server name but
								// the administrative device name
		this.state = DevState.ON;
	}
	
	//=============================================================
	//=============================================================
	public void putStartupInfo(DbServInfo si) throws DevFailed
	{
		if (server==null)
			server = new DbServer(name);
		info = si;
		server.put_info(info);
	}
	//=============================================================
	//=============================================================
	public void updateStartupInfo() throws DevFailed
	{
		if (server==null)
			server = new DbServer(name);
		info = server.get_info();
	}
	//=============================================================
	//=============================================================
	public void setState(DevState state)
	{
		this.state = state;
	}
	//=============================================================
	//=============================================================
	public DevState getState()
	{
		return state;
	}
	//=============================================================
	//=============================================================
	//=============================================================
	public String getName()
	{
		return name;
	}
	//=============================================================
	//=============================================================
	//=============================================================
	public String[] queryClass() throws DevFailed
	{
		DeviceData	argout = command_inout("QueryClass");
		return argout.extractStringArray();
	}
	//=============================================================
	//=============================================================
	public DbDevImportInfo getImportInfo() throws DevFailed
	{
		return import_device();
	}
	//=============================================================
	//=============================================================
	public String[] queryDevice() throws DevFailed
	{
		//	Query the device list 
		//	(Check two times for backward compatibility)
		//-------------------------------------------------
		DeviceData	argout;
		try {
			argout = command_inout("QueryDevice");
		} 
		catch (DevFailed e)
		{
			//	If not "command not found" re-throw exception
			if (e.errors[0].reason.equals("API_CommandNotFound")==false)
				throw e;
			else
				argout = command_inout("DevQueryDevice");
		}
		//	remove class name at begining of each device name added in Tango-5
		String		separator = "::";
		String[]	devices = argout.extractStringArray();
		for (int i=0, start=0 ; i<devices.length ; i++)
			if ((start=devices[i].indexOf(separator))>0)
				devices[i] = devices[i].substring(start+separator.length());
		return devices;
	}
	//=============================================================
	//=============================================================
	public String toString()
	{
		return name;
	}



	//===============================================================
	//===============================================================
	public boolean startupLevel(JDialog parent, String hostname, Point p)
	{
		boolean	modif = false;
		try
		{
			//	Get server startup info and popup dialog
			PutServerInfoDialog	dialog = new PutServerInfoDialog(parent, true);
			dialog.setLocation(p);
			updateStartupInfo();

			//	if OK put the new info to database
			if (dialog.showDialog(info)==PutServerInfoDialog.RET_OK)
			{
				info = dialog.getSelection();
				if (info!=null)
				{
					info.host = hostname;
					putStartupInfo(info);
				}
				else
				{
					//	Check if server is stopped (it must be)
					if (state==DevState.ON)
					{
						app_util.PopupMessage.show(parent,
								"Stop " + name + "  server before !");
						return modif;
					}
					//	Remove server info in database
					putStartupInfo(new DbServInfo(name, "", false, 0));

					//	Register devices on empty host and unexport.
					if (server==null)
						server = new DbServer(name);
					String[]	devname = server.get_device_class_list();
					for (int i=0 ; i<devname.length ; i+=2)
					{
						ApiUtil.get_db_obj().export_device(
							new DbDevExportInfo(devname[i], "", "", ""));
						ApiUtil.get_db_obj().unexport_device(devname[i]);
					}
				}
				modif = true;
			}
		}
		catch (DevFailed e)
		{
			e.printStackTrace();
			app_util.PopupError.show(parent, e);
		}
		return modif;
	}
	//===============================================================
	//===============================================================
	public String chooseDevice(Component parent) throws DevFailed
	{
		//	Query the device list to run jive
		String[] devices = queryDevice();
		String	choice = null;
		switch (devices.length)
		{
		case 0:
			Except.throw_exception("NO_DEVICE_REGISTRED",
						"No device registred for this derver",
						"TangoServer.chooseDevice()");
			break;
		case 1:
			choice = devices[0];
			break;
		default:
			if ((choice=(String) JOptionPane.showInputDialog(parent, 
								"Device selection :", "",
								JOptionPane.INFORMATION_MESSAGE, null,
								devices, devices[0])) == null)
				choice = null;
			break;
		}
		return choice;
	}
	//===============================================================
	//===============================================================
	public void testDevice(Component parent, Point point)
	{
    	try
		{
			//	Query the device to test
			String	choice = chooseDevice(parent);
			if (choice==null)
				return;
			//	Start Test Device panel  on selected device
            JDialog d;
            if (parent instanceof JDialog)
                d = new JDialog((JDialog)parent, false);
            else
                d = new JDialog((JFrame)parent, false);
            d.setTitle(choice + " Device Panel");
            try {
                d.setContentPane(new jive.ExecDev(choice));
            }
            catch(NoClassDefFoundError er)
            {
                app_util.PopupError.show(parent, "Jive.jar is too old\nIt needs to be upgraded.");
            }
            ATKGraphicsUtils.centerDialog(d);
            d.setVisible(true);
		}
		catch(DevFailed e)
		{
			app_util.PopupError.show(parent, e);
		}
	}
	//===============================================================
	//===============================================================
	public String getServerInfo(Component parent, boolean ds_present)
	{
		String		servinfo = "------------ Server Info ------------\n\n";
		try
		{
			//	Query info from database
			servinfo += get_info().toString();
		}
		catch(DevFailed e)
		{
			app_util.PopupError.show(parent, e);
			return "";
		}
		if (ds_present)
		{
			try
			{
				//	Query info from server if running
				String[]	devices  = queryDevice();
				servinfo +=  "\n\n----------- Device(s) Served -----------\n\n";
				for (int i=0 ; i<devices.length ; i++)
					servinfo += devices[i] + "\n";
			}
			catch(DevFailed e) {
			}
		}
		return servinfo;
	}
	//===============================================================
	//===============================================================
	public void configureWithWizard(JDialog parent)
	{
		try
		{
			jive.DevWizard wdlg = new jive.DevWizard(parent);
			wdlg.showClassesWizard(name);
		}
		catch(NoSuchMethodError e)
		{
			app_util.PopupError.show(parent, "Not supported by Jive before 2.6 revision.");
		}
	}
	//===============================================================
	//===============================================================
	public void manageMemorizedAttributes(JDialog parent)
	{
		try
		{
			/*
			if (server==null)
				server = new DbServer(name);
			new AttMemoDialog(parent, server).setVisible(true);
			*/
			//	Replaced by following  class
			new DbServerArchitecture(parent, name).setVisible(true);
		}
		catch(DevFailed e)
		{
			app_util.PopupError.show(parent, e);
		}
	}
	//===============================================================
	//===============================================================
	public boolean displayArchitecture(JDialog parent)
	{
		try
		{
			ServArchitectureDialog	dialog =
				new ServArchitectureDialog(parent, this);
			AstorUtil.centerDialog(dialog, parent);
			dialog.setVisible(true);
		}
		catch(DevFailed e)
		{
			if (e.errors[0].reason.equals("API_CommandNotFound"))
                return false;
            else
				app_util.PopupError.show(parent, e);
		}
        return true;
	}
	//===============================================================
	//===============================================================
	public void displayServerInfo(JDialog parent)
	{
		//	Dispplay info from database and from server if any.
        boolean done = false;
        if (state==DevState.ON)
            done = displayArchitecture(parent);
        if(done==false)
        {
    		String		servinfo = getServerInfo(parent, (state==DevState.ON));
	    	if (servinfo.length()>0)
		    	app_util.PopupMessage.show(parent, servinfo);
        }
	}
	//===============================================================
	//===============================================================
	public void displayClassInfo(JFrame parent)
	{
		try
		{
			if (server==null)
				server = new DbServer(name);
			//	Prepeare text
			//------------------------
			String[]		classes = server.get_class_list();
			StringBuffer	sb = new StringBuffer(
								"<FONT SIZE=+2>Tango Device Server " +
								name + " : </font>\n\n");
			sb.append("<ul>\n");
			//	Do for each class
			for (int i=0 ; i<classes.length ;i++)
			{
				sb.append("<li> <b> Class " + classes[i] + ":</b>\n");
				sb.append("<ul>\n");

				String[]	prop = AstorUtil.getServerClassProperties(classes[i]);
				//	append URL doc location
				if (prop[2].equals(DocLocationUnknown))
					sb.append (DocLocationUnknown + "<Br><Br>\n");
				//{
				//	sb.append("<a href=\"" + DerfaultDocLocation + "\">\n	");
				//	sb.append(DerfaultDocLocation + "</a>\n<Br><Br>\n");
				//}
				else
				{
					sb.append("<a href=\"" + prop[2] + "\">\n	");
					sb.append(prop[2] + "</a>\n<Br><Br>\n");
				}
				//	append class description
				if (prop[0]!=null) sb.append(prop[0] + "<Br>\n");
				if (prop[1]!=null) sb.append(prop[1] + "<Br>\n");

				sb.append("</ul>\n<Br><Br>");
			}
			sb.append("</ul>\n");

			//	And write it in a tmp file
			//---------------------------------
			int		random_value = new java.util.Random().nextInt(30000);
			FileOutputStream	fidout;
			String	filename = new String("/tmp/astor." + random_value);
			//	Try a first time
			try {
				fidout = new FileOutputStream(filename);
			}
			catch(FileNotFoundException e)
			{
				//	May be it is window and /tmp does not exist....
				filename = new String("c:/temp/astor." + random_value);
				fidout = new FileOutputStream(filename);
			}
			fidout.write(HtmlHeader.getBytes());
			fidout.write(sb.toString().getBytes());
			fidout.write(HtmlFooter.getBytes());
			fidout.close();

			String	urlstr = new String("file:"+filename);
			new app_util.PopupHtml(parent, true).show(urlstr);
			new File(filename).delete();
		}
		catch (Exception e) {
			app_util.PopupError.show(parent, e);
			e.printStackTrace();
		}
	}
	//===============================================================
	//===============================================================
	private String indent(String s)
	{
		String	ret = "";
		String	tab = "        ";
		int		start = 0;
		int		end   = 0;
		while ((end=s.indexOf("\n", start))>=0)
		{
			if (start>0)
				ret += "\n";
			ret += tab + s.substring(start, end);
			start = end+1;
		}
		if (start>0)
			ret += "\n";
		ret += tab + s.substring(start);

		if (ret.endsWith("\n"))
			ret = ret.substring(0, ret.length()-2);
		return ret;
	}
	//===============================================================
	//===============================================================
	private JDialog	stateDialog;
	private tool_panels.servstate.ServStatePanel	statePanel;
	public void checkStates(JDialog parent, Point point)
	{
		//	Try to implement state panel if found in classpath
		try
		{
			//	Start progress monitor
			//-----------------------------
			Splash	splash = new Splash();
			splash.setTitle("Forking State Panel");
			splash.setMessage("Starting");
			splash.setVisible(true);
			splash.progress(10);

			statePanel = new tool_panels.servstate.ServStatePanel(name);
			splash.progress(30);


			//	Build dialog
			stateDialog = new JDialog(parent, false);
			stateDialog.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent evt) {

					//	Stop refresher
					statePanel.stopRefresher();
					//	Close dialog
					stateDialog.setVisible(false);
					stateDialog.dispose();
				}
			});

			JButton	dismissBtn = new JButton("Dismiss");
			dismissBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {

					//	Stop refresher
					statePanel.stopRefresher();
					//	Close dialog
					stateDialog.setVisible(false);
					stateDialog.dispose();
				}
			});
			JPanel	panel2 = new JPanel();
			panel2.add(dismissBtn);
			stateDialog.getContentPane().add(panel2, BorderLayout.SOUTH);
			splash.progress(50);

			//	Add state panel in disalog and set it visible.
			stateDialog.getContentPane().add(statePanel, BorderLayout.CENTER);
			stateDialog.pack();
			AstorUtil.cascadeDialog(stateDialog, parent);
			stateDialog.setVisible(true);
			splash.setVisible(false);
			System.out.println("stateDialog created");
			return;
		}
		catch(DevFailed e)
		{
			ErrorPane.showErrorMessage(parent, null, e);
			return;
		}
		catch (NoClassDefFoundError e)
		{
			System.out.println(e);
		}

		//==================================
		//	If not found do it as before
		//==================================
		try
		{
			String		servinfo = "";
			//	Query info from server
			String[]	_devices  = queryDevice();
			//	add admin device
			String[]	devices = new String[_devices.length+1];
			devices[0] = devname;
			for (int i=0 ; i<_devices.length ; i++)
				devices[i+1] = _devices[i];
			DevState[]	states = new DevState[devices.length];

			//	Check state for each device
			for (int i=0 ; i<devices.length ; i++)
			{
				DeviceProxy	d = new DeviceProxy(devices[i]);
				states[i] = d.state();
				servinfo += devices[i] + ":    ";
				servinfo += ApiUtil.stateName(states[i]) + "\n";

				//	Check if status must be added.
				if (states[i]==DevState.FAULT  ||
					states[i]==DevState.ALARM  ||
					states[i]==DevState.UNKNOWN )
				{
					servinfo += indent(d.status())+"\n\n";
				}
			}
			//System.out.println(servinfo);
			int	nb_lines = 0;
			for (int i=0 ; i<servinfo.length() ; i++)
				if (servinfo.charAt(i)=='\n')
					nb_lines++;
			if (nb_lines<=40)
				app_util.PopupMessage.show(parent, servinfo);
			else
				new app_util.PopupText(parent,  true).show(servinfo);
		}
		catch(DevFailed e)
		{
			ErrorPane.showErrorMessage(parent, null, e);
		}
	}
	//===============================================================
	//===============================================================
	void restart(Component parent, TangoHost host)
	{
		if (JOptionPane.showConfirmDialog(parent,
				"Are you sure to want to restart " + getName(),
				"Confirm Dialog",
				JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
		{
			new restartThread(parent, host).start();
		}
	}
	//===============================================================
	//===============================================================



	//===============================================================
	/*
	 *	A thread class to staop, Wait a bit and restat server.
	 */
	//===============================================================
	private class restartThread extends Thread
	{
		private TangoHost	host;
		private Component	parent;
	
		//===============================================================
		//===============================================================
		restartThread(Component parent, TangoHost host)
		{
			this.host   = host;
			this.parent = parent;
		}
		//===============================================================
		//===============================================================
		public void run()
		{
			try
			{
				//	Stop it
				host.stopServer(name);

				//	Wait a bit to be sure it is stopped
				long	t0 = System.currentTimeMillis();
				long	t1 = t0;
				while (state==DevState.ON ||
						state==DevState.MOVING && (t1-t0)<RestartTimeout)
				{
					host.updateData();
					try { Thread.sleep(1000); } catch(Exception e){}
					t1 = System.currentTimeMillis();
				}
				if (state==DevState.ON)
				{
					Except.throw_exception("STOP_TIMOUT",
									"Stopping server " + name + " timeout\n"+
									"may be, it cannot be stopped.",
									"TangoServer.restartThread.run()");
				}
				//	And restart.
				host.startServer(parent, name);
			}
			catch (DevFailed e) {
				app_util.PopupError.show(parent, e);
			}
		}

	}
}
