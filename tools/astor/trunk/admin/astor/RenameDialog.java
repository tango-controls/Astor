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


package admin.astor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author Pascal Verdier
 */
public class RenameDialog extends JDialog {

    private JTextField theText;
    private boolean ret_code;
    String value;

    //===============================================================
    /**
     * Construction without predefined values
     */
    //===============================================================
    public RenameDialog(Frame parent, String value, Rectangle bounds) {
        super(parent, true);
        getContentPane().setLayout(null);
        theText = new JTextField();

        theText.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ret_code = true;
                    closeDlg();
                }

                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    ret_code = false;
                    closeDlg();
                }
            }

            public void keyTyped(KeyEvent e) {
            }

        });

        getContentPane().add(theText);
        theText.setBounds(0, 0, (int) bounds.getWidth(), (int) bounds.getHeight());
        theText.setText(value);
        theText.setBorder(BorderFactory.createLineBorder(Color.black));
        theText.selectAll();
        setBounds(bounds);
        setUndecorated(true);
        ret_code = false;
    }

    //===============================================================
    //===============================================================
    public void closeDlg() {
        value = theText.getText();
        setVisible(false);
    }

    //===============================================================
    //===============================================================
    public boolean showDlg() {
        setVisible(true);
        return ret_code;
    }

    //===============================================================
    //===============================================================
    public String getNewName() {
        return value;
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings("unused")
    public void moveToLocation(int x, int y) {
        Rectangle r = getBounds();
        r.setLocation(x, y);
        setBounds(r);
    }
}
