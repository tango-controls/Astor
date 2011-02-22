//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the Pogo class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.6  2006/01/11 08:46:40  pascal_verdier
// PollingProfiler added.
//
// Revision 3.5  2005/12/01 10:00:23  pascal_verdier
// Change TANGO_HOST added (needs TangORB-4.7.7 or later).
//
// Revision 3.4  2005/11/24 12:24:57  pascal_verdier
// DevBrowser utility added.
// MkStarter utility added.
//
// Revision 3.3  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.2  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
// Revision 2.0  2003/01/16 15:22:35  verdier
// Last ci before CVS usage
//
// Revision 1.1  2002/09/13 08:43:07  verdier
// Initial revision
//

//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;

/**
 *	This class is a dialog to manage polling device commands.
 *
 * @author  verdier
  */


import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.TangoConst;

import javax.swing.*;
import java.awt.*;

public class ManagePollingDialog extends javax.swing.JDialog implements TangoConst{

    private Component       parent;
	private String[]		commands;
	private String[]		attributes;
	private TangoServer		server;

	//====================================================
	//====================================================
	private void setCmdAttrBox()
	{
		//	Check selected items
		String[]	strArray;
    	if (cmdBtn.getSelectedObjects()!=null)
		{
			cmdLabel.setText("Command Selection");
			strArray = commands;
		}
		else
		{
			cmdLabel.setText("Attribute Selection");
			strArray = attributes;
		}
		//	And add to combo box
		cmdBox.removeAllItems();
		for (int i=0 ; i<strArray.length ; i++)
			cmdBox.addItem(strArray[i]);
		pack();
	}
	//====================================================
	/**
	 *	Init Components from Devices read.
	 */
	//====================================================
	private void initComponentsFromDevices()
	{
		try
		{
			//	Get device list on admin device
			String[]	devices = server.queryDevice();
			for (int i=0 ; i<devices.length ; i++)
				devicesBox.addItem(devices[i]);

			devicesBox.addItem("*");
			devicesBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					devSelectionActionPerformed(evt);
				}});

			cmdBtn.setSelected(false);
			attrBtn.setSelected(true);
			getAttrCmdLists(devices[0]);
		}
		catch (DevFailed e){
			app_util.PopupError.show(this, e);
		}

	}
	//====================================================
	//====================================================
	private void getAttrCmdLists(String devname)
	{
		try
		{
			//	get the command list on device itself
			DeviceProxy		dev = new DeviceProxy(devname);
			CommandInfo[]	cmd_list = dev.command_list_query();
			//	keep only command without argin and not Init
			int	nb=0;
			for (int i=0 ; i<cmd_list.length ; i++)
				if (cmd_list[i].in_type==Tango_DEV_VOID)
					if (cmd_list[i].cmd_name.equals("Init")==false)
						nb++;
			//	Allocate and fill array
			commands = new String[nb];
			for (int i=0, j=0 ; i<cmd_list.length ; i++)
				if (cmd_list[i].in_type==Tango_DEV_VOID)
					if (cmd_list[i].cmd_name.equals("Init")==false)
						commands[j++] = cmd_list[i].cmd_name;
			//	get attribute list
			attributes = dev.get_attribute_list();
			setCmdAttrBox();
		}
		catch(DevFailed e)
		{
			app_util.PopupError.show(this, e);
			return;
		}
	}
	//====================================================
	//====================================================
	private void devSelectionActionPerformed(java.awt.event.ActionEvent evt)
	{
		String		devname = devicesBox.getSelectedItem().toString();
		if (devname.equals("*")==false)
			getAttrCmdLists(devname);
	}
	//====================================================
	/**
	 *	Creates new form ManagePollingDialog
	 */
	//====================================================
    public ManagePollingDialog(JDialog parent, TangoServer server, Point p)
	{
        super(parent, false);
		this.server = server;
        this.parent = parent;
		setTitle("Device Polling Management Window");
        initComponents();
		initComponentsFromDevices();

		setLocation(p);
        displayStatus();
		pack();
    }

	//====================================================
	/**
	 *	Creates new form ManagePollingDialog
	 */
	//====================================================
    public ManagePollingDialog(JFrame parent, String devname, String attname, Point p) throws DevFailed
	{
        super(parent, false);
		this.server = new TangoServer(new DeviceProxy(devname).adm_name());
        this.parent = parent;
		setTitle("Device Polling Management Window");
        initComponents();
		initComponentsFromDevices();
		devicesBox.setSelectedItem(devname);
		cmdBox.setSelectedItem(attname);

		setLocation(p);
        displayStatus();
		pack();
    }
	//====================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//====================================================
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        updateBtn = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        profilerBtn = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        dismissBtn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        devicesBox = new javax.swing.JComboBox();
        cmdBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        cmdLabel = new javax.swing.JLabel();
        addBtn = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        removeBtn = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmdBtn = new javax.swing.JRadioButton();
        attrBtn = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        textScrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        updateBtn.setText("Update Status");
        updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBtnActionPerformed(evt);
            }
        });

        jPanel2.add(updateBtn);

        jLabel8.setText("        ");
        jPanel2.add(jLabel8);

        profilerBtn.setText("Show Profiler");
        profilerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profilerBtnActionPerformed(evt);
            }
        });

        jPanel2.add(profilerBtn);

        jLabel9.setText("        ");
        jPanel2.add(jLabel9);

        dismissBtn.setText("Dismiss");
        dismissBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dismissBtnActionPerformed(evt);
            }
        });

        jPanel2.add(dismissBtn);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        devicesBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                devicesBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(devicesBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(cmdBox, gridBagConstraints);

        jLabel2.setText("Device Selection");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        jPanel3.add(jLabel2, gridBagConstraints);

        cmdLabel.setText("command :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel3.add(cmdLabel, gridBagConstraints);

        addBtn.setText("Add / Update");
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(addBtn, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 36));
        jLabel3.setText("  ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        jPanel3.add(jLabel3, gridBagConstraints);

        removeBtn.setText("Remove");
        removeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(removeBtn, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 36));
        jLabel4.setText("  ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        jPanel3.add(jLabel4, gridBagConstraints);

        jLabel5.setText("   ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        jPanel3.add(jLabel5, gridBagConstraints);

        jLabel6.setText("   ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        jPanel3.add(jLabel6, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Polling  on   ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jLabel1, gridBagConstraints);

        cmdBtn.setText("Command");
        cmdBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(cmdBtn, gridBagConstraints);

        attrBtn.setText("Attribute");
        attrBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attrBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(attrBtn, gridBagConstraints);

        jLabel7.setText("   ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        jPanel1.add(jLabel7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jPanel1, gridBagConstraints);

        getContentPane().add(jPanel3, java.awt.BorderLayout.WEST);

        textScrollPane.setPreferredSize(new java.awt.Dimension(450, 400));
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Courier New", 1, 12));
        textScrollPane.setViewportView(textArea);

        getContentPane().add(textScrollPane, java.awt.BorderLayout.CENTER);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    //=========================================================
    //=========================================================
    private void profilerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profilerBtnActionPerformed
        String[]    devnames = selectedDevices();
        try
        {
            if (parent instanceof JFrame)
                new admin.astor.tools.PollingProfiler((JFrame)parent, devnames).setVisible(true);
            else
                new admin.astor.tools.PollingProfiler((JDialog)parent, devnames).setVisible(true);
        }
        catch(DevFailed e)
        {
            app_util.PopupError.show(this, e);
        }
    }//GEN-LAST:event_profilerBtnActionPerformed

	//=========================================================
	/**
	 *	If '*' is selected returns all device names
	 *	else returns only selected device name.
	 */
	//=========================================================
	private String[] selectedDevices()
	{
		String		item = devicesBox.getSelectedItem().toString();
		String[]	names;
		if (item.equals("*"))
		{
			int	nb = devicesBox.getItemCount()-1;	//	-1 because dont take '*'
			names = new String[nb];
			for (int i =0 ; i<nb ; i++)
				names[i] = devicesBox.getItemAt(i).toString();
		}
		else
		{
			names = new String[1];
			names[0] = item;
		}
		return names;
	}
	//=========================================================
	//=========================================================
    private void displayStatus()
    {
        //	Get selected device name(s).
        String[]	devnames = selectedDevices();
        String  	message = "        Polling Status\n\n";
        for (int i=0 ; i<devnames.length ; i++)
        {
            try
            {
                message += "=============================================\n";
                message += devnames[i] + " :\n\n";
                //	import device and send command
                DeviceProxy		dev = new DeviceProxy(devnames[i]);
                String[]		status = dev.polling_status();
                //	Add separators
                for (int st=0 ; st<status.length ; st++)
                {
                    message += status[st]+"\n";
                    if (st<status.length-1)
                        message += "   ---------------------------------\n";
                }
                if (status.length>0)
                    message += "\n\n";
              }
            catch(DevFailed e)
            {
                app_util.PopupError.show(this, e);
                return;
            }
        }
        //	Display result
        textArea.setText(message);
        textArea.setCaretPosition(0);
    }
	//=========================================================
	//=========================================================
	private void removeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBtnActionPerformed

		//	Get selected device name(s).
		String[]	devnames = selectedDevices();
		String		polled_obj_name = cmdBox.getSelectedItem().toString();
		String	message = "Remove polling on " + polled_obj_name + " for:\n";
        for (int i=0 ; i<devnames.length ; i++)
		    message += devnames[i] + ",  ";
        if (JOptionPane.showConfirmDialog(this,
                            message,
                            "Question ?",
                            JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
        {
            message = polled_obj_name + " polling has been removed for:\n";
            for (int i=0 ; i<devnames.length ; i++)
            {
                try
                {
                    //	import device and send command
                    DeviceProxy	dev = new DeviceProxy(devnames[i]);

                    if (cmdBtn.getSelectedObjects()!=null)
                        dev.stop_poll_command(polled_obj_name);
                    else
                        dev.stop_poll_attribute(polled_obj_name);
                    message += devnames[i] + "\n";
                }
                catch(DevFailed e)
                {
                    app_util.PopupError.show(this, e);
                    return;
                }
            }
            app_util.PopupMessage.show(this, message);
            displayStatus();
        }
	}//GEN-LAST:event_removeBtnActionPerformed

	//=========================================================
	//=========================================================
    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed

		//	Get selected device name(s).
		String[]	devnames = selectedDevices();
		boolean ok = false;
		String	strval = "";
		int	polling_period = 0;


		//	Get polling period while not ok
		while (ok==false)
		{
			//	Get polling period as string
			strval =(String) JOptionPane.showInputDialog(this,
									"Polling period (ms)  ?",
									"Polling period",
									JOptionPane.INFORMATION_MESSAGE,
									null, null, strval);
			if (strval==null)
				return;
			//	Convert to int
			try {
				polling_period = Integer.parseInt(strval);
				//	Check value
				if (polling_period<20)
					app_util.PopupError.show(this,
								"The polling period minimum value is 20ms");
				else
					ok = true;
			}
			catch(NumberFormatException e)
			{
				app_util.PopupError.show(this, e.toString() +
									"\n\nBad Value in Polling period field !");
			}
		}
		String		polled_obj_name = cmdBox.getSelectedItem().toString();
		StringBuffer	message = new StringBuffer(polled_obj_name +
									" polling has been added for:\n");
		for (int i=0 ; i<devnames.length ; i++)
		{
			try
			{
				//	import device and send command
				DeviceProxy	dev = new DeviceProxy(devnames[i]);

	    		if (cmdBtn.getSelectedObjects()!=null)
					dev.poll_command(polled_obj_name, polling_period);
				else
					dev.poll_attribute(polled_obj_name, polling_period);
				message.append(devnames[i] + "\n");
 			}
			catch(DevFailed e)
			{
				app_util.PopupError.show(this, e);
			}
		}
 		app_util.PopupMessage.show(this, message.toString());
        displayStatus();
  }//GEN-LAST:event_addBtnActionPerformed

	//=========================================================
	//=========================================================
    private void attrBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attrBtnActionPerformed
    	if (attrBtn.getSelectedObjects()!=null)
			cmdBtn.setSelected(false);
		setCmdAttrBox();
    }//GEN-LAST:event_attrBtnActionPerformed

	//=========================================================
	//=========================================================
    private void cmdBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdBtnActionPerformed
    	if (cmdBtn.getSelectedObjects()!=null)
			attrBtn.setSelected(false);
		setCmdAttrBox();
    }//GEN-LAST:event_cmdBtnActionPerformed

    //=========================================================
    //=========================================================
    private void devicesBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_devicesBoxActionPerformed
        displayStatus();
    }//GEN-LAST:event_devicesBoxActionPerformed

 	//=========================================================
 	//=========================================================
    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        displayStatus();
     }//GEN-LAST:event_updateBtnActionPerformed

    //=========================================================
    //=========================================================
     private void dismissBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dismissBtnActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_dismissBtnActionPerformed

	//=========================================================
 	//=========================================================
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JRadioButton attrBtn;
    private javax.swing.JComboBox cmdBox;
    private javax.swing.JRadioButton cmdBtn;
    private javax.swing.JLabel cmdLabel;
    private javax.swing.JComboBox devicesBox;
    private javax.swing.JButton dismissBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton profilerBtn;
    private javax.swing.JButton removeBtn;
    private javax.swing.JTextArea textArea;
    private javax.swing.JScrollPane textScrollPane;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables

}
