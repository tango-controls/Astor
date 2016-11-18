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


package admin.astor.access;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author verdier
 */

public class EditTreeItem extends JDialog {

    private JTextField theText;
    private boolean ret_code;
    private int obj_type;
    private static final int TXT_OFFSET = 20;

    private static boolean keep_deco = false;

    //===============================================================
    /*
     *	Construction without predefined values
     */
    //===============================================================
    //===============================================================
    //===============================================================
    public EditTreeItem(Frame parent, JTree tree, String value, int obj_type) {
        super(parent, true);
        this.obj_type = obj_type;
        getContentPane().setLayout(null);
        theText = new JTextField();

        //	Nicer without decoration !
        //	but OK only with java 1.4 and 1.6
        String version = System.getProperty("java.version");
        if (version != null)
            keep_deco = version.startsWith("1.5");

        //	Add keyboard listeners
        theText.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                keyReleasedListener(e);
            }

            public void keyTyped(KeyEvent e) {
            }

        });

        Rectangle bounds = computeBounds(tree);
        getContentPane().add(theText);
        theText.setBounds(0, 0, (int) bounds.getWidth(), (int) bounds.getHeight());
        theText.setText(value);
        theText.setBorder(BorderFactory.createLineBorder(Color.black));
        theText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        theText.selectAll();
        setBounds(bounds);
        if (!keep_deco)
            setUndecorated(true);
        ret_code = false;
    }


    //===============================================================
    //===============================================================
    public void keyReleasedListener(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (checkInputs()) {
                ret_code = true;
                closeDlg();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            ret_code = false;
            closeDlg();
        }
    }

    //===============================================================
    //===============================================================
    private boolean checkInputs() {
        switch (obj_type) {
            case UsersTree.ADDRESS:
                return checkAddress();
            case UsersTree.DEVICE:
                return checkDevice();
        }
        return false;
    }

    //===============================================================
    //===============================================================
    private boolean checkDevice() {
        //  Check dev name
        String dev = theText.getText().trim().toLowerCase();
        theText.setText(dev);
        List<String> tokens = new ArrayList<>();
        StringTokenizer stk = new StringTokenizer(dev, "/");
        while (stk.hasMoreTokens())
            tokens.add(stk.nextToken());
        if (tokens.size() > 3) {
            admin.astor.tools.Utils.popupError(this, "Incorrect device name  (too many members)");
            return false;
        }
        if (tokens.size() < 3) {
            admin.astor.tools.Utils.popupError(this, "Incorrect device name  (not enough members)");
            return false;
        }
        return true;
    }

    //===============================================================
    //===============================================================
    private boolean checkAddress() {
        //  check IP add name
        String add = theText.getText().trim();
        theText.setText(add);

        //	Check if host name
        try {
            java.net.InetAddress iadd =
                    java.net.InetAddress.getByName(add);

            //	If found replace by address
            add = iadd.getHostAddress();
            theText.setText(add);
        } catch (Exception e) { /* */ }

        //	Try to split with '.' separator
        StringTokenizer stk = new StringTokenizer(add, ".");
        List<String> tokens = new ArrayList<>();
        while (stk.hasMoreTokens())
            tokens.add(stk.nextToken());
        if (tokens.size() > 4) {
            admin.astor.tools.Utils.popupError(this, "Incorrect IP address  (Too many members)");
            return false;
        } else if (tokens.size() < 4) {
            admin.astor.tools.Utils.popupError(this, "Incorrect IP address  (not enougth members)");
            return false;
        }

        //	rebuild add string to be sure that there is no too much '.'
        //		like xxx.xxx....xx....xx
        add = tokens.get(0) + "." + tokens.get(1) + "." + tokens.get(2) + "." + tokens.get(3);
        theText.setText(add);

        for (int i = 0; i < tokens.size(); i++) {
            //  Check if numbers
            try {
                //noinspection ResultOfMethodCallIgnored
                Short.parseShort(tokens.get(i));
            } catch (NumberFormatException e) {
                //  if NOT wildcard
                if (!tokens.get(i).equals("*")) {
                    admin.astor.tools.Utils.popupError(this, "Incorrect IP address  (member #" +
                            (i + 1) + " (" + tokens.get(i) + ") is not a number)");
                    return false;
                }
            }
        }

        return true;
    }

    //===============================================================
    //===============================================================
    private void closeDlg() {
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
    public String getInputs() {
        return theText.getText();
    }

    //===============================================================
    /**
     * Compute bound rectangle for a node
     *
     * @param tree the tree to compute bonds
     * @return the computed bounds
     */
    //===============================================================
    private Rectangle computeBounds(JTree tree) {
        TreePath selPath = tree.getSelectionPath();
        tree.scrollPathToVisible(selPath);
        Rectangle r = tree.getPathBounds(selPath);
        if (r!=null) {
            Point p = r.getLocation();
            SwingUtilities.convertPointToScreen(p, tree);
            r.setLocation(p);
            r.width = 250;
            r.height += 2;
            r.x += TXT_OFFSET;
        }
        return r;
    }
}
