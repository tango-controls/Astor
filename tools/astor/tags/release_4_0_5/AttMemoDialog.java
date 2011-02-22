//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author$
//
// $Revision$
// $Log$
// Revision 3.1  2005/10/14 14:29:34  pascal_verdier
// Edit memorized attribute value added.
//
//
//
// Copyleft 2005 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;

import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


//===============================================================
/**
 *	Class Description: Basic Dialog Class to display info
 *
 *	@author  root
 */
//===============================================================


public class AttMemoDialog extends JDialog {
	private JDialog		parent;
	private DbServer	server;
	private boolean		from_appli = true;
	private Memorized[]	memorized;
	
	//===============================================================
	/**
	 *	Creates new form AttMemoDialog
	 */
	//===============================================================
	public AttMemoDialog(JDialog parent, DbServer server) throws DevFailed
	{
		super(parent, true);
		this.parent = parent;
		this.server = server;	   
		initComponents();
		buildMemorizedPanel();

		titleLabel.setText("Memorized attributes found for " + server.name());
		pack();
		//	Check if from an appli or from an empty JDialog
		if (parent.getWidth()==0)
			from_appli = false;

		if (from_appli)
			AstorUtil.centerDialog(this, parent);
	}

 	//===============================================================
 	//===============================================================
	private JLabel[]		attLbl;
	private JTextField[]	attVal;  
	private void buildMemorizedPanel() throws DevFailed
	{
		readAttributes();
		attLbl  = new JLabel[memorized.length];
		attVal   = new JTextField[memorized.length];
		for (int i=0 ; i<memorized.length ; i++)
		{
			GridBagConstraints	gbc= new GridBagConstraints();
			int	x = 0;
			int	y = i+1;
			attLbl[i] = new JLabel(memorized[i].attname);
			gbc.gridx = x++;
			gbc.gridy = y;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			valuePanel.add(attLbl[i], gbc);

			gbc.gridx = x++;
			gbc.gridy = y;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			valuePanel.add(new JLabel("  :    "), gbc);

			attVal[i] = new JTextField(memorized[i].value);
			attVal[i].setColumns(15);
			gbc.gridx = x++;
			gbc.gridy = y;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			valuePanel.add(attVal[i], gbc);


			gbc.gridx = x++;
			gbc.gridy = y;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			valuePanel.add(new JLabel("     " + 
						memorized[i].min + " < .... < " +
						memorized[i].max), gbc);

		}
	}
 	//===============================================================
 	//===============================================================
	private static final String	unknown = " ? ";
	private void readAttributes() throws DevFailed
	{
		DeviceProxy[]	dev = getDevices();
		Vector	v = new Vector();
		for (int d=0 ; d<dev.length ; d++)
		{
			String	devname = dev[d].name();
			//	Get Attribute known by Database
			DeviceData	argin = new DeviceData();
			String[]	args = new String[2];
			args[0] = devname;
			args[1] = "*";
			argin.insert(args);
			DeviceData	argout = 
				ApiUtil.get_db_obj().command_inout("DbGetDeviceAttributeList", argin);
			String[]	attlist = argout.extractStringArray();
			//	Get only witch have memorized value set
			for (int i=0 ; i<attlist.length ; i++)
			{
				DbAttribute	db_att   = dev[d].get_attribute_property(attlist[i]);
				String[]	proplist = db_att.get_property_list();
				for (int j=0 ; j<proplist.length ; j++)
				{
					if (proplist[j].equals("__value"))
					{
						String	min = unknown;
						if (db_att.is_empty("min_value")==false)
							min = db_att.get_value("min_value")[0];
						String	max = unknown;
						if (db_att.is_empty("max_value")==false)
							max = db_att.get_value("max_value")[0];
						v.add(devname+"/"+attlist[i]);
						v.add(db_att.get_string_value(j));
						v.add(min);
						v.add(max);
					}
				}
			}
		}		
		//	build memorized array
		memorized = new Memorized[v.size()/4];
		for (int i=0 ; i<v.size() ; i+=4)
			memorized[i/4] = new Memorized((String) v.elementAt(i),
										(String) v.elementAt(i+1),
										(String) v.elementAt(i+2),
										(String) v.elementAt(i+3));
	}
 	//===============================================================
 	//===============================================================
	private DeviceProxy[] getDevices() throws DevFailed
	{
		Vector	v = new Vector();
		String[] classes = server.get_class_list();

		for (int c=0 ; c<classes.length ; c++)
		{
			String[]	devices = server.get_device_name(classes[c]);
			for (int d=0 ; d<devices.length ; d++)
				v.add(devices[d]);
		}
		DeviceProxy[]	dev = new DeviceProxy[v.size()];
		for (int i=0 ; i<v.size() ; i++)
			dev[i] = new DeviceProxy((String)v.elementAt(i));
		
		return dev;
	}
 	//===============================================================
   /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//===============================================================
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        valuePanel = new javax.swing.JPanel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okBtn.setText("OK");
        okBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okBtnActionPerformed(evt);
            }
        });

        jPanel1.add(okBtn);

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        jPanel1.add(cancelBtn);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18));
        titleLabel.setText("Dialog Title");
        jPanel2.add(titleLabel);

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        valuePanel.setLayout(new java.awt.GridBagLayout());

        getContentPane().add(valuePanel, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

	//===============================================================
	//===============================================================
	private boolean writeValues()
	{
		String	message = "";
		for (int i=0 ; i<attVal.length ; i++)
		{
			String		val_str = attVal[i].getText();
			Memorized	memo = memorized[i];
			double		val;
			
			if (val_str.equals(memo.value)==false)
			{
				//	Check if number
				try {
					val = Double.parseDouble(val_str);
				}
				catch(NumberFormatException e) {
					app_util.PopupError.show(parent, val_str + " is not a number !");
					return false;
				}

				//	Check if out of bounds
				if (memo.min.equals(unknown)==false)
				{
					double min;
					try {
						min = Double.parseDouble(memo.min);
					}
					catch(NumberFormatException e) {
						app_util.PopupError.show(parent, memo.min + " is not a number !");
						return false;
					}
					if (val<min) {
						app_util.PopupError.show(parent, "Incorrect value:\n" +
									val_str +  " is less than " + memo.min);
						return false;
					}
				}
				if (memo.max.equals(unknown)==false)
				{
					double max;
					try {
						max = Double.parseDouble(memo.max);
					}
					catch(NumberFormatException e) {
						app_util.PopupError.show(parent, memo.max + " is not a number !");
						return false;
					}
					if (val>max) {
						app_util.PopupError.show(parent, "Incorrect value:\n" +
									val_str +  " is greater than " + memo.max);
						return false;
					}
				}
				
				//	write to database
				try
				{
					DbDatum	data = new DbDatum("__value");
					data.insert(val_str);
					AttributeProxy	att = new AttributeProxy(attLbl[i].getText());
					att.put_property(data);
					memo.value = val_str;
					message += attLbl[i].getText() + "  set to  " + val_str + "\n";
				}
				catch (DevFailed e)
				{
					app_util.PopupError.show(parent, e);
					return false;
				}
			}
		}
		if (message.length()>0)
			app_util.PopupMessage.show(parent, message);
		return true;
	}
	//===============================================================
	//===============================================================
	private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed
		if (writeValues())
			doClose();
	}//GEN-LAST:event_okBtnActionPerformed

	//===============================================================
	//===============================================================
	private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed

	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		doClose();
	}//GEN-LAST:event_closeDialog

	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void doClose()
	{
		setVisible(false);
		dispose();
		if (from_appli==false)
			System.exit(0);
	}
	//===============================================================
	//===============================================================
	public void showDialog()
	{
		//	Check if memorized attribute not found
		if (memorized.length==0)
		{
			app_util.PopupError.show(parent,
				"No memorized attribute found for " + server.name());
		}
		else
			setVisible(true);
	}
	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okBtn;
    private javax.swing.JPanel valuePanel;
    private javax.swing.JButton cancelBtn;
    // End of variables declaration//GEN-END:variables
	//===============================================================




	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String args[]) {
	
		try
		{
			AttMemoDialog	d = new AttMemoDialog(new JDialog(),
									new DbServer("PowerSupply/pv"));
			d.showDialog();
		}
		catch(DevFailed e)
		{
			Except.print_exception(e);
		}
	}



	//===============================================================
	//===============================================================
	class Memorized
	{
		String	attname;
		String	value;
		String	min;
		String	max;
		Memorized(String attname, String value, String min, String max)
		{
			this.attname = attname;
			this.value   = value;
			this.min     = min;
			this.max     = max;
		}
	}
}
