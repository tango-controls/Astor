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

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author verdier
 */
//===============================================================
//===============================================================
public class PropListDialog extends javax.swing.JDialog {
    private JFrame parent;
    private String selectedItem = null;
    private JTextArea pathText = null;

    private String[] props;
    private JList<String> jList;
    //======================================================
    //======================================================
    private void setList() {
        jList.setListData(props);
    }

    //======================================================
    /*
      *	Creates new form PropListDialog
      */
    //======================================================
    public PropListDialog(JFrame parent, String[] props) {
        super(parent, true);
        this.parent = parent;
        this.props = props;
        initComponents();

        buildList();
    }

    //======================================================
    /*
      *	Creates new form PropListDialog
      */
    //======================================================
    public PropListDialog(JFrame parent, List<String> propertyList) {
        super(parent, true);
        this.parent = parent;
        this.props = new String[propertyList.size()];
        for (int i = 0; i < propertyList.size(); i++)
            this.props[i] = propertyList.get(i);
        initComponents();

        buildList();
    }

    //======================================================
    /*
      *	Creates new form PropListDialog
      */
    //======================================================
    public PropListDialog(JFrame parent, JTextArea pathText, TangoHost[] hosts) {

        super(parent, true);
        this.parent = parent;
        this.pathText = pathText;
        initComponents();

        hosts2path(hosts);
        buildList();
    }

    //======================================================
    //======================================================
    private boolean alreadyIn(List<String> stringList, String s) {
        for (String str : stringList)
            if (str.equals(s))
                return true;
        return false;
    }

    //======================================================
    //======================================================
    private void hosts2path(TangoHost[] hosts) {
        try {
            List<String> pathList = new ArrayList<>();
            for (TangoHost host : hosts) {
                String deviceName = AstorUtil.getStarterDeviceHeader() + host.getName();
                DeviceProxy dev = new DeviceProxy(deviceName);
                DbDatum data = dev.get_property("StartDsPath");
                if (!data.is_empty()) {
                    String[] path = data.extractStringArray();
                    for (String aPath : path)
                        if (!alreadyIn(pathList, aPath))
                            pathList.add(aPath);
                }
            }
            props = new String[pathList.size()];
            for (int i = 0; i < pathList.size(); i++)
                props[i] = pathList.get(i);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }

    //======================================================
    //======================================================
    private void buildList() {
        //	Add a mouse listener on list
        //---------------------------------------------------
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                listSelectionPerformed(e);
            }
        };
        jList.addMouseListener(mouseListener);

        pack();
    }

    //======================================================
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    //======================================================
    private void initComponents() {//GEN-BEGIN:initComponents
        JPanel jPanel1 = new JPanel();
        JButton addBtn = new JButton();
        JButton dismissBtn = new JButton();
        JScrollPane jScrollPane1 = new JScrollPane();
        jList = new javax.swing.JList<>();
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        addBtn.setText("Add");
        addBtn.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        addBtn.setFont(new java.awt.Font("Dialog", 0, 12));
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });

        jPanel1.add(addBtn);

        dismissBtn.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        if (pathText == null)
            dismissBtn.setText("Cancel");
        else
            dismissBtn.setText("Dismiss");
        dismissBtn.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        dismissBtn.setFont(new java.awt.Font("Dialog", 0, 12));
        dismissBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dismissBtnActionPerformed(evt);
            }
        });
        jPanel1.add(dismissBtn);
        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        jList.setFont(new java.awt.Font("Courier", Font.BOLD, 12));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(450, 300));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(450, 300));
        jScrollPane1.setViewportView(jList);
        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    //======================================================
    //======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void dismissBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dismissBtnActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_dismissBtnActionPerformed

    //======================================================
    //======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        retrieveSelectedItem();
    }//GEN-LAST:event_addBtnActionPerformed

    //======================================================
    //======================================================
    private void listSelectionPerformed(MouseEvent evt) {
        //	save selected item to set selection  later.
        //selectedItem = new String((String) jList.getSelectedValue());

        //	Check if double click
        if (evt.getClickCount() == 2)
            retrieveSelectedItem();
    }

    //======================================================
    //======================================================
    private void retrieveSelectedItem() {
        //	At first try if already running
        selectedItem = jList.getSelectedValue();
        if (pathText != null)
            pathText.append(selectedItem + "\n");
        else {
            setVisible(false);
            dispose();
        }
    }

    //======================================================
    //======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    //======================================================
    //======================================================
    public void showDialog() {
        setList();

        //	Center windon and move a bit
        Point p = parent.getLocationOnScreen();
        p.x += ((parent.getWidth() - getWidth()) / 2) + 50;
        p.y += ((parent.getHeight() - getHeight()) / 2) + 50;
        setLocation(p);

        setVisible(true);
    }

    //======================================================
    //======================================================
    public String getSelectedItem() {
        return selectedItem;
    }
    //======================================================
    //======================================================
}
