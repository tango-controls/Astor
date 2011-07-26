//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// $Revision$
//
//-======================================================================


package admin.astor;
 
/** 
 *	Server object containing devices list.
 *	This class inherit from device proxy.
 *	It is seen as the administrative device of this server.
 *
 * @author  verdier
 */

import admin.astor.tools.*;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.Splash;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Vector;


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
		this.devname = get_name();	//	Not the server name but
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
		this.devname = get_name();	//	Not the server name but
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
	public String[] queryDeviceFromDb() throws DevFailed
	{
		if (server==null) {
			String servname = name.substring(name.indexOf('/')+1);
			server = new DbServer(servname);
		}
		Vector<String>	v = new Vector<String>();
		String[]	class_list = server.get_class_list();
		for (String cl : class_list) {
			String[]	devnames = server.get_device_name(cl);
            v.addAll(Arrays.asList(devnames));
		}
		String[]	devnames = new String[v.size()];
		for (int i=0 ; i<v.size() ; i++)
			devnames[i] = v.get(i);
		return devnames;
	}
	//=============================================================
	//=============================================================
	public String[] queryDevice() throws DevFailed
	{
		return queryDevice(false);
	}
	//=============================================================
	//=============================================================
	public String[] queryDevice(boolean add_dserver) throws DevFailed
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
			if (!e.errors[0].reason.equals("API_CommandNotFound"))
				throw e;
			else
				argout = command_inout("DevQueryDevice");
		}

		//	remove class name at begining of each device name added in Tango-5
		String		separator = "::";
		String[]	tmp = argout.extractStringArray();
		String[]	devices = new String[(add_dserver)? tmp.length+1 : tmp.length];
		for (int i=0, start ; i<tmp.length ; i++)
			if ((start=tmp[i].indexOf(separator))>0)
				devices[i] = tmp[i].substring(start+separator.length());
			else
				devices[i] = tmp[i];

		//	Add the admin dev name if requested.
		if (add_dserver)
			devices[devices.length-1] = devname;
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
						Utils.popupMessage(parent,
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
			ErrorPane.showErrorMessage(parent, null, e);
		}
		return modif;
	}
	//===============================================================
	//===============================================================
	public String chooseDevice(Component parent) throws DevFailed
	{
		return chooseDevice(parent, false);
	}
	//===============================================================
	//===============================================================
	public String chooseDevice(Component parent, boolean add_dserver) throws DevFailed
	{
		//	Query the device list to run jive
		String[] devices = queryDevice(add_dserver);
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
	public void displayBlackBox(Component parent)
	{
		try
		{
			//	Query the device to test
			String	choice = chooseDevice(parent, true);
			if (choice==null)
				return;
			//	Start Test black box on selected device
			BlackBoxTable	bb;
			if (parent instanceof JFrame)
				bb = new BlackBoxTable((JFrame) parent, choice);
			else
				bb = new BlackBoxTable((JDialog) parent, choice);
			bb.setVisible(true);
		}
		catch(DevFailed e)
		{
			ErrorPane.showErrorMessage(parent, null, e);
		}
	}
	//===============================================================
	//===============================================================
	public void testDevice(Component parent)
	{
		try
		{
			//	Query the device to test
			String	choice = chooseDevice(parent);
			if (choice==null)
				return;
			//	Start Test Device panel  on selected device
			AstorUtil.testDevice(parent, choice);
		}
		catch(DevFailed e)
		{
			ErrorPane.showErrorMessage(parent, null, e);
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
			ErrorPane.showErrorMessage(parent, null, e);
			return "";
		}
		if (ds_present)
		{
			try
			{
				//	Query info from server if running
				String[]	devices  = queryDevice();
				servinfo +=  "\n\n----------- Device(s) Served -----------\n\n";
				for (String devname : devices)
					servinfo += devname + "\n";
			}
			catch(DevFailed e) { /* */ }
		}
		return servinfo;
	}
	//===============================================================
	//===============================================================
	public void configureWithWizard(JDialog parent)
	{
		jive.DevWizard wdlg = new jive.DevWizard(parent);
		wdlg.showClassesWizard(name);
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
			ErrorPane.showErrorMessage(parent, null, e);
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
				ErrorPane.showErrorMessage(parent, null, e);
		}
		return true;
	}
	//===============================================================
	//===============================================================
	public String[] getServerUptime() throws DevFailed
	{
        String[]  retStr = new String[0];
        String	  servinfo = get_info().toString();
        int start = servinfo.indexOf("last_exported:");
        if (start>0) {
            start += "last_exported:".length();
            int end = servinfo.indexOf("\n", start);
            if (end>0) {
                //  Get exported date
                retStr = new String[2];
                retStr[0] = servinfo.substring(start, end).trim();

                //  Get unexported
                start = servinfo.indexOf(":", end);
                if (start>0){
                    retStr[1] = servinfo.substring(start+1).trim();
                } else {
                    retStr[1] = "???";
                }
            }
        }

        return retStr;
    }
	//===============================================================
	//===============================================================
	public void displayServerInfo(JDialog parent)
	{
		//	Dispplay info from database and from server if any.
		boolean done = false;
		if (state==DevState.ON)
			done = displayArchitecture(parent);
		if(!done)
		{
			String		servinfo = getServerInfo(parent, (state==DevState.ON));
			if (servinfo.length()>0)
				Utils.popupMessage(parent, servinfo);
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
			for (String classname : classes)
			{
				sb.append("<li> <b> Class ").append(classname).append(":</b>\n");
				sb.append("<ul>\n");

				String[]	prop = AstorUtil.getServerClassProperties(classname);
				//	append URL doc location
				if (prop[2].equals(DocLocationUnknown))
					sb.append(DocLocationUnknown + "<Br><Br>\n");
					//{
					//	sb.append("<a href=\"" + DerfaultDocLocation + "\">\n	");
					//	sb.append(DerfaultDocLocation + "</a>\n<Br><Br>\n");
					//}
				else
				{
					sb.append("<a href=\"").append(prop[2]).append("\">\n	");
					sb.append(prop[2]).append("</a>\n<Br><Br>\n");
				}
				//	append class description
				if (prop[0] != null) sb.append(prop[0]).append("<Br>\n");
				if (prop[1] != null) sb.append(prop[1]).append("<Br>\n");

				sb.append("</ul>\n<Br><Br>");
			}
			sb.append("</ul>\n");

			//	And write it in a tmp file
			//---------------------------------
			int		random_value = new java.util.Random().nextInt(30000);
			FileOutputStream	fidout;
			String	filename = "/tmp/astor." + random_value;
			//	Try a first time
			try {
				fidout = new FileOutputStream(filename);
			}
			catch(FileNotFoundException e)
			{
				//	May be it is window and /tmp does not exist....
				filename = "c:/temp/astor." + random_value;
				fidout = new FileOutputStream(filename);
			}
			fidout.write(HtmlHeader.getBytes());
			fidout.write(sb.toString().getBytes());
			fidout.write(HtmlFooter.getBytes());
			fidout.close();

			String	urlstr = "file:"+filename;
			new PopupHtml(parent, true).show(urlstr);
			if (!new File(filename).delete())
                System.err.println("Failed to delete " + filename);
		}
		catch (Exception e) {
			ErrorPane.showErrorMessage(parent, null, e);
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
		int		end;
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
	public void checkStates(JDialog parent)
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
            System.arraycopy(_devices, 0, devices, 1, _devices.length);
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
				Utils.popupMessage(parent, servinfo);
			else
				new PopupText(parent,  true).show(servinfo);
		}
		catch(DevFailed e)
		{
			ErrorPane.showErrorMessage(parent, null, e);
		}
	}
	//===============================================================
	//===============================================================
	public void restart(Component parent, TangoHost host, boolean check_stop)
	{
		String	message = (check_stop)? "Are you sure to" : "Do you";
		message += " want to restart "+ getName();
		if (JOptionPane.showConfirmDialog(parent,
				message,
				"Confirm Dialog",
				JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
		{
			new restartThread(parent, host, check_stop).start();
		}
	}
	//===============================================================
	//===============================================================
	private PoolThreadsManager	pool_thread_man = null;
	void poolThreadManager(JDialog parent, TangoHost host)
	{
		try
		{
			if (pool_thread_man==null)
				pool_thread_man = new PoolThreadsManager(parent, host, this);
			else
				pool_thread_man.initializeTree();
			pool_thread_man.setVisible(true);
		}
		catch(DevFailed e)
		{
			ErrorPane.showErrorMessage(parent, null, e);
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
		private boolean 	check_stop;

		//===============================================================
		//===============================================================
		restartThread(Component parent, TangoHost host, boolean check_stop)
		{
			this.host   = host;
			this.parent = parent;
			this.check_stop = check_stop;
		}
		//===============================================================
		//===============================================================
		public void run()
		{
			try {
				//	Stop it
				try {
					host.stopServer(name);
				}
				catch (DevFailed e) {
					if (check_stop)
						throw e;
				}

				//	Wait a bit to be sure it is stopped
				long	t0 = System.currentTimeMillis();
				long	t1 = t0;
				while (state==DevState.ON ||
						state==DevState.MOVING && (t1-t0)<RestartTimeout) {
					host.updateData();
					try { Thread.sleep(1000); } catch(InterruptedException e){ /* */ }
					t1 = System.currentTimeMillis();
				}
				if (state==DevState.ON) {
					Except.throw_exception("STOP_TIMOUT",
									"Stopping server " + name + " timeout\n"+
									"may be, it cannot be stopped.",
									"TangoServer.restartThread.run()");
				}
				//	And restart.
				host.startServer(parent, name);
			}
			catch (DevFailed e) {
				ErrorPane.showErrorMessage(parent, null, e);
			}
		}
	}
}
