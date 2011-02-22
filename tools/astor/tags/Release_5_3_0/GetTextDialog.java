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
//
//
// Copyleft 2007 by European Synchrotron Radiation Facility, Grenoble, France
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
import fr.esrf.tangoatk.widget.util.*;


//===============================================================
/**
 *	Class Description: Basic Dialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


public class GetTextDialog extends JDialog
{
	private int		retVal = JOptionPane.OK_OPTION;

	//===============================================================
	/**
	 *	Creates new form GetTextDialog
	 */
	//===============================================================
	public GetTextDialog(JDialog parent, String title, String tip, String[] lines)
	{
		super(parent, true);
		initComponents();

		initialize(title, tip, lines);
	}
	//===============================================================
	/**
	 *	Creates new form GetTextDialog
	 */
	//===============================================================
	public GetTextDialog(JFrame parent, String title, String tip, String[] lines)
	{
		super(parent, true);
		initComponents();

		initialize(title, tip, lines);
	}
	//===============================================================
	//===============================================================
	private void initialize( String title, String tip, String[] lines)
	{
		//	Set title and help
		titleLabel.setText(title);
		textArea.setToolTipText(tip);

		//	Set default text
		if (lines!=null)
		{
			String	text = "";
			for (int i=0 ; i<lines.length ; i++)
				text += lines[i] + "\n";
			text.trim();
			textArea.setText(text);
		}
		//	Set Size and Position
		pack();
        ATKGraphicsUtils.centerDialog(this);
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
        jPanel1 = new javax.swing.JPanel();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();

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

        jScrollPane1.setPreferredSize(new java.awt.Dimension(500, 300));
        textArea.setColumns(60);
        textArea.setFont(new java.awt.Font("Courier", 1, 14));
        textArea.setRows(5);
        jScrollPane1.setViewportView(textArea);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
	private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed
		retVal = JOptionPane.OK_OPTION;
		doClose();
	}//GEN-LAST:event_okBtnActionPerformed

	//===============================================================
	//===============================================================
	private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		retVal = JOptionPane.CANCEL_OPTION;
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed

	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		retVal = JOptionPane.CANCEL_OPTION;
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
	public String getText()
	{
		return textArea.getText().trim();
	}
	//===============================================================
	//===============================================================
	public String[] getTextLines()
	{
		String	str = getText();
		Vector	v = new Vector();
		StringTokenizer st = new StringTokenizer(str, "\n");
		while(st.hasMoreTokens())
			v.add(st.nextToken());
		String[]	lines = new String[v.size()];
		for (int i=0 ; i<v.size() ; i++)
			lines[i] = (String)v.get(i);
		return lines;
	}
	//===============================================================
	//===============================================================
	public int showDialog()
	{
		setVisible(true);
		return retVal;
	}

	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okBtn;
    private javax.swing.JTextArea textArea;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//===============================================================
}
