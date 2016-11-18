//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author: pascal_verdier $
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
// $Revision: 16801 $
//
//-======================================================================


package admin.astor.statistics;

import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.ArrayList;


//===============================================================
/**
 * Class Description:
 * Dialog Class to display data in a JTable inside a JDialog.
 */
//===============================================================


public class StarterStatTable extends JDialog {

    private DataTableModel model;
    private JTable jtable;
    private String[][] data;

    private JFrame parent;
    private JLabel titleLabel;
    private StarterStat starterStat;

    //===============================================================
    /**
     * Creates new form StarterStatTable
     *
     * @param    parent    parent component.
     * @param    title    Widow title.
     * @param    starterStat        specified starter statistics
     */
    //===============================================================
    public StarterStatTable(JFrame parent, String title, StarterStat starterStat) {
        super(parent, false);
        this.parent = parent;
        this.starterStat = starterStat;
        data = starterStat.toTable();
        initComponents();
        initMyComponents(title);
    }
    //===============================================================
    /**
     * Creates new form StarterStatTable
     *
     * @throws fr.esrf.Tango.DevFailed in case of problem to display in table.
     * @param    parent    parent component.
     * @param    hostname the specified host name.
     */
    //===============================================================
    public StarterStatTable(JFrame parent, String hostname) throws DevFailed {
        super(parent, false);
        this.parent = parent;
        this.starterStat = new StarterStat(hostname);
        data = starterStat.toTable();
        initComponents();
        initMyComponents(hostname);
    }

    //===============================================================
    //===============================================================
    private void initComponents() {
        JPanel jPanel1 = new javax.swing.JPanel();
        JButton cancelBtn = new javax.swing.JButton();
        JPanel jPanel2 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        cancelBtn.setText("Dismiss");
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
        pack();
    }

    //===============================================================
    //===============================================================
    private void initMyComponents(String title) {
        model = new DataTableModel();

        // Create the table
        jtable = new JTable(model);
        jtable.setRowSelectionAllowed(true);
        jtable.setColumnSelectionAllowed(true);
        jtable.setDragEnabled(true);
        jtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtable.getTableHeader().setFont(new java.awt.Font("Dialog", 1, 14));
        jtable.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                headerTableActionPerformed(evt);
            }
        });
        jtable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableActionPerformed(evt);
            }
        });

        //	Put it in a scrolled pane
        JScrollPane scrollPane = new JScrollPane(jtable);

        int height = 18 + 18 * data.length;
        if (height > 400) height = 400;

        final int[] width = {180, 180, 70, 130};
        final Enumeration enumeration = jtable.getColumnModel().getColumns();
        TableColumn tc;
        int sp_width = 0;
        for (int i = 0; enumeration.hasMoreElements(); i++) {
            tc = (TableColumn) enumeration.nextElement();
            tc.setPreferredWidth(width[i]);
            sp_width += width[i];
        }
        scrollPane.setPreferredSize(new Dimension(sp_width, height));

        getContentPane().add(scrollPane, BorderLayout.CENTER);
        model.fireTableDataChanged();

        titleLabel.setText(title);
        ATKGraphicsUtils.centerDialog(this);
        pack();
    }

    //===============================================================
    //===============================================================
    private void headerTableActionPerformed(java.awt.event.MouseEvent evt) {

        int column = jtable.getTableHeader().columnAtPoint(
                new Point(evt.getX(), evt.getY()));
        new UsedData().sort(column);
        model.fireTableDataChanged();
    }

    //===============================================================
    //===============================================================
    private void tableActionPerformed(java.awt.event.MouseEvent evt) {
        int column = jtable.columnAtPoint(new Point(evt.getX(), evt.getY()));
        int row = jtable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        //	get selected cell
        //ServerStat  server = filteredServerStatistics.get(row);
        if (evt.getButton() == MouseEvent.BUTTON1) {
            if (evt.getClickCount() == 2) {
                if (column == 0) {    //  Server name

                    String serverName = data[row][0];
                    for (ServerStat serverStat : starterStat) {
                        if (serverStat.name.equals(serverName)) {
                            if (parent != null)
                                new ServerStatisticsPanel(parent, serverName, serverStat).setVisible(true);
                            else
                                new ServerStatisticsPanel(this, serverName, serverStat).setVisible(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {
        doClose();
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void closeDialog(java.awt.event.WindowEvent evt) {
        doClose();
    }
    //===============================================================

    /**
     * Closes the dialog
     */
    //===============================================================
    private void doClose() {
        setVisible(false);
        dispose();
        if (parent == null)
            System.exit(0);
    }
    //===============================================================
    //===============================================================

    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Crate name ?");
            System.exit(0);
        }

        try {
            new StarterStatTable(null, args[0]).setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //===============================================================
    //===============================================================


    //=========================================================================
    /**
     * A class to sort table data
     */
    //=========================================================================
    class UsedData extends ArrayList<String[]> {
        //======================================================
        //======================================================
        UsedData() {
            //	fill with lines
            for (String[] datum : data)
                add(datum);
        }

        //======================================================
        //======================================================
        void sort(int column) {
            this.column = column;
            //	Sort data
            MyCompare compare = new MyCompare();
            Collections.sort(this, compare);

            for (int i = 0; i < size(); i++)
                data[i] = get(i);
        }

        private int column;
        //======================================================
        /**
         * MyCompare class to sort collection
         */
        //======================================================
        class MyCompare implements Comparator<String[]> {
            public int compare(String[] array1, String[] array2) {
                String s1 = array1[column];
                String s2 = array2[column];

                //	Check if number
                try {
                    double d1 = Double.parseDouble(s1);
                    double d2 = Double.parseDouble(s2);
                    if (d1==d2) return 0;
                    return ((d1 < d2) ? 1 : -1);
                } catch (NumberFormatException e) { /* */ }

                //	Sort as String
                return s1.compareToIgnoreCase(s2);
            }
        }
    }


    //=========================================================================
    //=========================================================================
    public class DataTableModel extends AbstractTableModel {
        //==========================================================
        //==========================================================
        public int getColumnCount() {
            return data[0].length;
        }

        //==========================================================
        //==========================================================
        public int getRowCount() {
            return data.length;
        }

        //==========================================================
        //==========================================================
        public String getColumnName(int aCol) {
            if (aCol >= getColumnCount())
                return StarterStat.tableHeader[getColumnCount() - 1];
            else
                return StarterStat.tableHeader[aCol];
        }

        //==========================================================
        //==========================================================
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
        //==========================================================
        //==========================================================
    }
}
