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


package admin.astor.tools;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.AttributeProxy;
import fr.esrf.TangoDs.TangoConst;

import javax.swing.*;
import java.awt.*;


//===============================================================
/**
 *	Class Description: Basic Dialog Class to display info
 *
 *	@author  root
 */
//===============================================================


public class PropertyDialog extends JDialog implements TangoConst
{
	private JFrame	parent;
	private String	signame;

	static int		ADD_OPTION    = 0;
	static int		UPDATE_OPTION = 1;
	static int		CANCEL_OPTION = 2;

	private int		mode;

	//===============================================================
	/**
	 *	Creates new form PropertyDialog
	 */
	//===============================================================
	public PropertyDialog(JFrame parent, String signame, int mode) {
		super(parent, true);
		this.parent  = parent;
		this.signame = signame;
		this.mode    = mode;
		initComponents();

		titleLabel.setText(EventsTable.strMode[mode] + " Event for " + signame);

		switch (mode)
		{
		case EventsTable.SUBSCRIBE_CHANGE:
			periodLbl.setVisible(false);
			periodTxt.setVisible(false);
			resetPerBtn.setVisible(false);
			break;
		case EventsTable.SUBSCRIBE_PERIODIC:
			absLbl.setVisible(false);
			absTxt.setVisible(false);
			resetAbsBtn.setVisible(false);
			relLbl.setVisible(false);
			relTxt.setVisible(false);
			resetRelBtn.setVisible(false);
			break;
		}

		pack();
		Point	p = parent.getLocationOnScreen();
		p.x += 50;
		p.y += 50;
		setLocation(p);
	}
	//===============================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//===============================================================
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        updateBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        absLbl = new javax.swing.JLabel();
        relLbl = new javax.swing.JLabel();
        periodLbl = new javax.swing.JLabel();
        absTxt = new javax.swing.JTextField();
        relTxt = new javax.swing.JTextField();
        periodTxt = new javax.swing.JTextField();
        resetAbsBtn = new javax.swing.JButton();
        resetRelBtn = new javax.swing.JButton();
        resetPerBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        updateBtn.setText("Update");
        updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBtnActionPerformed(evt);
            }
        });

        jPanel1.add(updateBtn);

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

        jPanel3.setLayout(new java.awt.GridBagLayout());

        absLbl.setText("abs_change");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel3.add(absLbl, gridBagConstraints);

        relLbl.setText("rel_change");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel3.add(relLbl, gridBagConstraints);

        periodLbl.setText("period");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel3.add(periodLbl, gridBagConstraints);

        absTxt.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel3.add(absTxt, gridBagConstraints);

        relTxt.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel3.add(relTxt, gridBagConstraints);

        periodTxt.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel3.add(periodTxt, gridBagConstraints);

        resetAbsBtn.setText("Reset");
        resetAbsBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        resetAbsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAbsBtnActionPerformed(evt);
            }
        });

        jPanel3.add(resetAbsBtn, new java.awt.GridBagConstraints());

        resetRelBtn.setText("Reset");
        resetRelBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        resetRelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetRelBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel3.add(resetRelBtn, gridBagConstraints);

        resetPerBtn.setText("Reset");
        resetPerBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        resetPerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetPerBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        jPanel3.add(resetPerBtn, gridBagConstraints);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
    private void resetPerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetPerBtnActionPerformed
		periodTxt.setText(Tango_AlrmValueNotSpec);
    }//GEN-LAST:event_resetPerBtnActionPerformed

	//===============================================================
	//===============================================================
    private void resetRelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetRelBtnActionPerformed
		relTxt.setText(Tango_AlrmValueNotSpec);
    }//GEN-LAST:event_resetRelBtnActionPerformed

	//===============================================================
	//===============================================================
    private void resetAbsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAbsBtnActionPerformed
		absTxt.setText(Tango_AlrmValueNotSpec);
    }//GEN-LAST:event_resetAbsBtnActionPerformed

	//===============================================================
	/**
	 *	Verify if value set are coherent and if at least one is set.
	 */
	//===============================================================
	private boolean checkValues()
	{
		try {
			String	s;
			s = getAbs();
			if (!s.equals(Tango_AlrmValueNotSpec) &&
				!s.equals("NaN")) {
				Double.parseDouble(s);
			}
			s = getRel();
			if (!s.equals(Tango_AlrmValueNotSpec) &&
				!s.equals("NaN")) {
				Double.parseDouble(s);
			}
			s = getPeriod();
			if (!s.equals(Tango_AlrmValueNotSpec) &&
				!s.equals("NaN")) {
				Integer.parseInt(s);
			}
		}
		catch(Exception e) {
			Utils.popupError(this, null, e);
			return false;
		}
		return true;
	}
	//===============================================================
	//===============================================================
	private boolean writeValues()
	{
		try {
			//	Retreive properties (check before if have changed
			boolean	changed = false;
			if (!abs_change.equals(getAbs())) {
				changed = true;
				if (mode==EventsTable.SUBSCRIBE_CHANGE)
					info.events.ch_event.abs_change = getAbs();
				else
				if (mode==EventsTable.SUBSCRIBE_ARCHIVE)
					info.events.arch_event.abs_change = getAbs();
			}
			if (!rel_change.equals(getRel())) {
				changed = true;
				if (mode==EventsTable.SUBSCRIBE_CHANGE)
					info.events.ch_event.rel_change = getRel();
				else
				if (mode==EventsTable.SUBSCRIBE_ARCHIVE)
					info.events.arch_event.rel_change = getRel();
			}
			if (!period.equals(getPeriod())) {
				changed = true;
				if (mode==EventsTable.SUBSCRIBE_PERIODIC)
					info.events.per_event.period = getPeriod();
				else
				if (mode==EventsTable.SUBSCRIBE_ARCHIVE)
					info.events.arch_event.period = getPeriod();
			}

			//	And set them if have changed
			if (changed) {
				AttributeInfoEx[]	aie = new AttributeInfoEx[1];
				aie[0] = info;
				att.set_info(aie);
			}
			return true;
		}
		catch(DevFailed e){
			Utils.popupError(parent, null, e);
			return false;
		}
	}
	//===============================================================
	//===============================================================
    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
		if (checkValues()) {
			if (writeValues())
				doClose();
		}
    }//GEN-LAST:event_updateBtnActionPerformed
	//===============================================================
	//===============================================================
	private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed

	//===============================================================
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
	}
	//===============================================================
	//===============================================================
	String getAbs()
	{
		String	s = absTxt.getText().trim();
		if (s.equals(Tango_AlrmValueNotSpec))
			return "NaN";
		else
			return s;
	}
	//===============================================================
	//===============================================================
	String getRel()
	{
		String	s =  relTxt.getText().trim();
		if (s.equals(Tango_AlrmValueNotSpec))
			return "NaN";
		else
			return s;
	}
	//===============================================================
	//===============================================================
	String getPeriod()
	{
		String	s =  periodTxt.getText().trim();
		if (s.equals(Tango_AlrmValueNotSpec))
			return "NaN";
		else
			return s;
	}
	//===============================================================
	//===============================================================
	private AttributeProxy	att = null;
	private AttributeInfoEx	info;
	private String			abs_change, rel_change, period;
	private void displayProperty() throws DevFailed
	{
		if (att==null)
			att = new AttributeProxy(signame);
		info = att.get_info_ex();

		switch (mode)
		{
		case EventsTable.SUBSCRIBE_CHANGE:
			if (info.events!=null && info.events.ch_event!=null)
			{
				abs_change = info.events.ch_event.abs_change;
				rel_change = info.events.ch_event.rel_change;
				period     = Tango_AlrmValueNotSpec;
			}
			else
			{
				abs_change = Tango_AlrmValueNotSpec;
				rel_change = Tango_AlrmValueNotSpec;
				period     = Tango_AlrmValueNotSpec;
			}
			break;

		case EventsTable.SUBSCRIBE_ARCHIVE:
			if (info.events!=null && info.events.arch_event!=null)
			{
				abs_change = info.events.arch_event.abs_change;
				rel_change = info.events.arch_event.rel_change;
				period     = info.events.arch_event.period;
			}
			else
			{
				abs_change = Tango_AlrmValueNotSpec;
				rel_change = Tango_AlrmValueNotSpec;
				period     = Tango_AlrmValueNotSpec;
			}
			break;

		case EventsTable.SUBSCRIBE_PERIODIC:
			if (info.events!=null && info.events.per_event!=null)
			{
				abs_change = Tango_AlrmValueNotSpec;
				rel_change = Tango_AlrmValueNotSpec;
				period     = info.events.per_event.period;
			}
			else
			{
				abs_change = Tango_AlrmValueNotSpec;
				rel_change = Tango_AlrmValueNotSpec;
				period     = Tango_AlrmValueNotSpec;
			}
			break;
		}
		absTxt.setText(abs_change);
		relTxt.setText(rel_change);
		periodTxt.setText(period);
	}
	//===============================================================
	//===============================================================
	public void showDialog()
	{
		try {
			displayProperty();
			setVisible(true);
		}
		catch(DevFailed e) {
			Utils.popupError(parent, null, e);
		}
	}

	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel absLbl;
    private javax.swing.JTextField absTxt;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel periodLbl;
    private javax.swing.JTextField periodTxt;
    private javax.swing.JLabel relLbl;
    private javax.swing.JTextField relTxt;
    private javax.swing.JButton resetAbsBtn;
    private javax.swing.JButton resetPerBtn;
    private javax.swing.JButton resetRelBtn;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
	//===============================================================



}
