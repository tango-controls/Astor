//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,
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

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;


/**
 * Display a message in a scrolled JText
 *
 * @author verdier
 */
public class PopupText extends javax.swing.JDialog {

    private JScrollPane scrollPane;
    private JTextPane textPane;
    //======================================================
    /**
     * Creates new form PopupText
     */
    //======================================================
    public PopupText(JFrame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        pack();

        try {
            //	Put on top left corner
            Point p = parent.getLocationOnScreen();
            p.x += 10;
            p.y += 10;
            setLocation(p);
        } catch (Exception e) {
            /* is Parent is not already displayed */
        }
    }
    //======================================================
    /**
     * Creates new form PopupText
     */
    //======================================================
    public PopupText(JDialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
        pack();

        try {
            //	Put on top left corner
            Point p = parent.getLocationOnScreen();
            p.x += 10;
            p.y += 10;
            setLocation(p);
        } catch (Exception e) {
            /* is Parent is not already displayed */
        }
    }
    //======================================================
    //======================================================
    private void initComponents() {
        JPanel buttonPanel = new JPanel();
        JButton cancelButton = new JButton();
        scrollPane = new JScrollPane();
        textPane = new JTextPane();
        textPane.setFont(new Font("monospaced", Font.BOLD, 12));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        }
        );

        buttonPanel.setLayout(new java.awt.FlowLayout(FlowLayout.RIGHT, 5, 5));

        cancelButton.setText("Dismiss");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        }
        );

        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
        scrollPane.setViewportView(textPane);
        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

    }

    //============================================================
    //============================================================
    public void setFont(Font font) {
        textPane.setFont(font);
    }
    //============================================================
    //============================================================
    @SuppressWarnings("UnusedParameters")
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    //============================================================
    //============================================================
    @SuppressWarnings("UnusedParameters")
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    //============================================================
    //============================================================
    private void doClose() {
        setVisible(false);
        dispose();
    }
    //============================================================
    /**
     * Insert the message in the TextPane with its attributes
     */
    //============================================================
    private void showMsg(String text, SimpleAttributeSet attrs) {
        Document doc = textPane.getDocument();
        try {
            doc.insertString(doc.getLength(), text, attrs);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    //============================================================
    /**
     * format the text and Popup the window.
     */
    //============================================================
    @SuppressWarnings("unused")
    public void showFormatted(String text) {
        int nbLines = 0;
        int start = 0;
        int end;
        while ((end = text.indexOf('\n', start)) >= 0) {
            //	Get line after line
            end++;
            String line = text.substring(start, end);
            SimpleAttributeSet attrs = new SimpleAttributeSet();

            //	Check if title
            if (line.indexOf(':') < 0)
                StyleConstants.setBold(attrs, false);
            else
                StyleConstants.setBold(attrs, true);

            //	Check if state
            int start1;
            if ((start1 = line.indexOf("Running")) > 0) {
                showMsg(line.substring(0, start1), attrs);
                StyleConstants.setForeground(attrs, Color.green);
                showMsg(line.substring(start1), attrs);
            } else if ((start1 = line.indexOf("Stopped")) > 0) {
                showMsg(line.substring(0, start1), attrs);
                StyleConstants.setForeground(attrs, Color.red);
                showMsg(line.substring(start1), attrs);
            } else {
                StyleConstants.setForeground(attrs, Color.black);
                showMsg(line, attrs);
            }
            start = end;
            nbLines++;
        }
        textPane.setEditable(false);
        textPane.setSize(400, 300);
		scrollPane.setPreferredSize(new Dimension(450, 350));
        pack();
        setVisible(true);
    }


    //============================================================
    //============================================================
    public void setTitle(String title) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        int fs = StyleConstants.getFontSize(attrs);
        StyleConstants.setBold(attrs, true);
        StyleConstants.setUnderline(attrs, true);
        StyleConstants.setFontSize(attrs, fs + 4);
        showMsg(title + "\n\n", attrs);
    }

    //============================================================
    /**
     * Display a message in a scrolled JText dialog.
     */
    //============================================================
    public void show(String text) {
        textPane.setText(text);
        textPane.setEditable(false);
		scrollPane.setPreferredSize(new Dimension(450, 350));
        pack();
        setVisible(true);
    }
    //============================================================
    /**
     * Display a message in a scrolled JText dialog.
     */
    //============================================================
    public void show(String title, String[] array, int width, int height) {
        //	Display title
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        int fs = StyleConstants.getFontSize(attrs);
        StyleConstants.setBold(attrs, true);
        StyleConstants.setUnderline(attrs, true);
        StyleConstants.setFontSize(attrs, fs + 4);
        showMsg(title + "\n\n", attrs);

        StyleConstants.setBold(attrs, false);
        StyleConstants.setUnderline(attrs, false);
        StyleConstants.setFontSize(attrs, fs);

        //	Display lines
        for (String str : array)
            showMsg(str + "\n", attrs);
        textPane.setEditable(false);
        textPane.setPreferredSize(new Dimension(width, height));
		scrollPane.setPreferredSize(new Dimension(width, height));
        pack();
        setVisible(true);
    }
    //============================================================
    /**
     * Display a message in a scrolled JText dialog.
     */
    //============================================================
    public void show(String title, String[] array) {
        show(title, array, 800, 600);

    }
    //============================================================
    /**
     * Display a message in a scrolled JText dialog and set dimentions.
     */
    //============================================================
    public void show(String text, int sizeX, int sizeY) {
        textPane.setText(text);
        textPane.setEditable(false);
        textPane.setPreferredSize(new java.awt.Dimension(sizeX, sizeY));
        scrollPane.setPreferredSize(new java.awt.Dimension(sizeX, sizeY));
        pack();
        setVisible(true);
    }
    //============================================================
    //============================================================
    public void setSize(int width, int height) {
        textPane.setPreferredSize(new Dimension(width, height));
		scrollPane.setPreferredSize(new Dimension(width, height));
        pack();
    }
    //============================================================
    //============================================================
    public void addText(String text) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        int fs = StyleConstants.getFontSize(attrs);
        StyleConstants.setBold(attrs, false);
        StyleConstants.setUnderline(attrs, false);
        StyleConstants.setFontSize(attrs, fs);
        showMsg(text + "\n", attrs);
        textPane.setEditable(false);
    }
    //============================================================
    /**
     * @param args the command line arguments
     */
    //============================================================
    public static void main(String args[]) {
        PopupText pt = new PopupText(new javax.swing.JFrame(), true);
        pt.show("My Title", new String[]{"Line #1", "line #2", "Bla bla bla !"});
    }
}
