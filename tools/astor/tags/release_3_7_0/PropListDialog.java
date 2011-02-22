//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the TempClass class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.2  2003/07/22 14:35:20  pascal_verdier
// Minor bugs fixed.
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
// Revision 1.1  2003/06/19 12:27:03  pascal_verdier
// Add host option.
// Controlled servers list option.
//
//
// Copyright 1995 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================
package admin.astor;

/** 
 *
 * @author  verdier
 * @version 
 */
 
 
 
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;

//===============================================================
public class PropListDialog extends javax.swing.JDialog {
	private JFrame		parent;
	private	String		selectedItem = null;
	private JTextArea	pathText = null;

	private String[]	props;
	//======================================================
	//======================================================
	private void setList()	throws DevFailed
	{
		jList.setListData(props);
	}
	//======================================================
	/**
	 *	Creates new form PropListDialog
	 */
	//======================================================
	public PropListDialog(JFrame parent, String[] props) {
		super ((Frame)parent, true);
		this.parent = parent;
		this.props  = props;
		initComponents ();
		
		buildlist();
	}
	//======================================================
	/**
	 *	Creates new form PropListDialog
	 */
	//======================================================
	public PropListDialog(JFrame parent, JTextArea pathText, TangoHost[] hosts) {

		super ((Frame)parent, true);
		this.parent   = parent;
		this.pathText = pathText;
		initComponents ();

		hosts2path(hosts);
		buildlist();
	}
	//======================================================
	//======================================================
	private boolean alreadyIn(Vector vect, String s)
	{
		for (int i=0 ; i<vect.size() ; i++)
			if (vect.elementAt(i).equals(s))
				return true;
		return false;
	}
	//======================================================
	//======================================================
	private void hosts2path(TangoHost[] hosts)
	{
		try
		{
			Vector	vect = new Vector();
			for (int i=0 ; i<hosts.length ; i++)
			{
				String	devname = AstorDefs.starterDeviceHeader + hosts[i].getName();
				DeviceProxy	dev = new DeviceProxy(devname);
				DbDatum 	data = dev.get_property("StartDsPath");
				if (data.is_empty()==false)
				{
					String[]	path = data.extractStringArray();
					for (int p=0 ; p<path.length ; p++)
						if (alreadyIn(vect, path[p])==false)
							vect.add(path[p]);
				}
			}
			props = new String[vect.size()];
			for (int i=0 ; i<vect.size() ; i++)
				props[i] = (String) vect.elementAt(i);
		}
		catch (DevFailed e)
		{
			app_util.PopupError.show(parent, e);
		}
	}
	//======================================================
	//======================================================
	private void buildlist()
	{
		//	Add a mouse listener on list
		//---------------------------------------------------
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				listSelectionPerformed(e);
			}
		};
		jList.addMouseListener(mouseListener);

		pack ();
	}

 	//======================================================
	/** This method is called from within the constructor to
	* initialize the form.
	* WARNING: Do NOT modify this code. The content of this method is
	* always regenerated by the FormEditor.
	*/
 	//======================================================
	private void initComponents () {//GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel ();
		addBtn = new javax.swing.JButton ();
		dismissBtn = new javax.swing.JButton ();
		jScrollPane1 = new javax.swing.JScrollPane ();
		jList = new javax.swing.JList ();
		addWindowListener (new java.awt.event.WindowAdapter () {
			public void windowClosing (java.awt.event.WindowEvent evt) {
				closeDialog (evt);
			}
		});

		addBtn.setText ("Add");
		addBtn.setHorizontalAlignment (javax.swing.SwingConstants.RIGHT);
		addBtn.setFont (new java.awt.Font ("Dialog", 0, 12));
		addBtn.addActionListener (new java.awt.event.ActionListener () {
			public void actionPerformed (java.awt.event.ActionEvent evt) {
				addBtnActionPerformed (evt);
			}
		});

		jPanel1.add (addBtn);
  
		dismissBtn.setHorizontalTextPosition (javax.swing.SwingConstants.RIGHT);
		if (pathText==null)
			dismissBtn.setText ("Cancel");
		else
			dismissBtn.setText ("Dismiss");
		dismissBtn.setHorizontalAlignment (javax.swing.SwingConstants.RIGHT);
		dismissBtn.setFont (new java.awt.Font ("Dialog", 0, 12));
		dismissBtn.addActionListener (new java.awt.event.ActionListener () {
			public void actionPerformed (java.awt.event.ActionEvent evt) {
				dismissBtnActionPerformed (evt);
			}
		});
		jPanel1.add (dismissBtn);
		getContentPane ().add (jPanel1, java.awt.BorderLayout.SOUTH);

		jList.setFont (new java.awt.Font ("Courier", 1, 12));

		jScrollPane1.setPreferredSize (new java.awt.Dimension(450, 300));
		jScrollPane1.setMinimumSize (new java.awt.Dimension(450, 300));
		jScrollPane1.setViewportView (jList);
		getContentPane ().add (jScrollPane1, java.awt.BorderLayout.CENTER);

	}//GEN-END:initComponents

	//======================================================
	//======================================================
	private void dismissBtnActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dismissBtnActionPerformed
		setVisible (false);
		dispose ();
	}//GEN-LAST:event_dismissBtnActionPerformed

	//======================================================
	//======================================================
	private void addBtnActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
		retreiveSelectedItem();
	}//GEN-LAST:event_addBtnActionPerformed
	//======================================================
	//======================================================
	private void listSelectionPerformed(MouseEvent evt)
	{
		//	save selected item to set selection  later.
		//----------------------------------------------------
		//selectedItem = new String((String) jList.getSelectedValue());

		//	Check if double click
		//-----------------------------
		if (evt.getClickCount() == 2)
			retreiveSelectedItem();
	}
	//======================================================
	//======================================================
	private void retreiveSelectedItem()
	{
		//	At first try if already running
		//------------------------------------
		selectedItem = (String) jList.getSelectedValue();
		if (pathText!=null)
			pathText.append(selectedItem + "\n");
		else
		{
			setVisible (false);
			dispose ();
		}
	}
	//======================================================
	//======================================================
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		setVisible (false);
		dispose ();
	}//GEN-LAST:event_closeDialog
	//======================================================
	//======================================================
	public void showDialog()
	{
		try {
			setList();
		}
		catch(DevFailed e)
		{
			app_util.PopupError.show(parent, e);
		}
		//	Center windon and move a bit
		Point	p = parent.getLocationOnScreen();
		p.x += ((parent.getWidth()  - getWidth())   / 2) + 50;
		p.y += ((parent.getHeight() - getHeight())  / 2) + 50;
		setLocation(p);

		setVisible(true);
	}
	//======================================================
	//======================================================
	public String getSelectedItem()
	{
		return selectedItem;
	}

 	//======================================================
	//======================================================
        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JPanel jPanel1;
        private javax.swing.JButton addBtn;
        private javax.swing.JButton dismissBtn;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JList jList;
        // End of variables declaration//GEN-END:variables

	//======================================================
	//======================================================
 }
