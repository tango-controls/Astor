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

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;


//===============================================================
/**
 * Class Description:
 * Dialog Class to display data in a JTable inside a JDialog.
 */
//===============================================================


public class PopupTable extends JDialog {
    /**
     * Events Table
     */
    private DataTableModel model;

    /**
     * Names of the columns in the table
     */
    private static String[] col_names;

    /**
     * An array of String array for data to be displayed
     */
    private String[][] data;

    private JLabel titleLabel;
    private Window parent;
    private boolean fromApplication = true;
    private boolean sortAvailable = true;
    //===============================================================
    //===============================================================
    public PopupTable(JFrame parent, String filename)
            throws SecurityException,
            IOException,
            DevFailed {
        this(parent, filename, (Dimension) null);
    }

    //===============================================================
    /*
     *	Creates new form PopupTable
     *
     *	@param	parent		parent component.
     *	@param	filename	File's name to read data
     */
    //===============================================================
    public PopupTable(JFrame parent, String filename, Dimension dim)
            throws SecurityException,
            IOException,
            DevFailed {
        super(parent, false);
        this.parent = parent;

        readDataFile(filename);
        buildObject(filename, col_names, data, dim);

        //	Check if from an appli or from an empty JFrame
        if (parent.getWidth() == 0)
            fromApplication = false;
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings("unused")
    public PopupTable(JFrame parent, String title, String filename)
            throws SecurityException,
            IOException,
            DevFailed {
        this(parent, title, filename, null);
    }

    //===============================================================
    /*
     *	Creates new form PopupTable
     *
     *	@param	parent		parent component.
     *	@param	filename	File's name to read data
     */
    //===============================================================
    public PopupTable(JFrame parent, String title, String filename, Dimension dim)
            throws SecurityException,
            IOException,
            DevFailed {
        super(parent, false);
        this.parent = parent;

        readDataFile(filename);
        buildObject(title, col_names, data, dim);

        //	Check if from an appli or from an empty JFrame
        if (parent.getWidth() == 0)
            fromApplication = false;
    }

    //===============================================================
    //===============================================================
    public PopupTable(JFrame parent, String title, String[] col, List<String[]> list) throws DevFailed {
        this(parent, title, col, list, null);
    }
    //===============================================================
    //===============================================================
    public PopupTable(JDialog parent, String title, String[] col, List<String[]> list) throws DevFailed {
        this(parent, title, col, list, null);
    }
    //===============================================================
    //===============================================================
    public PopupTable(JDialog parent, String title, String[] col, String[][] array) throws DevFailed {
        this(parent, title, col, array, null);
    }
    //===============================================================
    /**
     * Creates new form PopupTable
     *
     * @param dim default size
     * @throws fr.esrf.Tango.DevFailed in case of problem to display in table.
     * @param    parent    parent component.
     * @param    title    Widow title.
     * @param    col      columns title.
     * @param    list    list of String arrays (lines).
     */
    //===============================================================
    public PopupTable(JDialog parent, String title, String[] col, List<String[]> list, Dimension dim) throws DevFailed {
        super(parent, false);
        this.parent = parent;
        buildObject(title, col, list.toArray(new String[list.size()][]), dim);
    }
    //===============================================================
    /**
     * Creates new form PopupTable
     *
     * @param dim default size
     * @throws fr.esrf.Tango.DevFailed in case of problem to display in table.
     * @param    parent    parent component.
     * @param    title    Widow title.
     * @param    col        columns title.
     * @param    array    array of String arrays.
     */
    //===============================================================
    public PopupTable(JDialog parent, String title, String[] col, String[][] array, Dimension dim) throws DevFailed {
        super(parent, false);
        this.parent = parent;
        buildObject(title, col, array, dim);
    }

    //===============================================================
    //===============================================================
    public PopupTable(JFrame parent, String title, String[] col, String[][] array) throws DevFailed {
        this(parent, title, col, array, null);
    }
    //===============================================================
    /**
     * Creates new form PopupTable
     *
     * @param dim default size
     * @throws fr.esrf.Tango.DevFailed in case of problem to display in table.
     * @param    parent    parent component.
     * @param    title    Widow title.
     * @param    col      columns title.
     * @param    list    list of String arrays (lines).
     */
    //===============================================================
    public PopupTable(JFrame parent, String title, String[] col, List<String[]> list, Dimension dim) throws DevFailed {
        super(parent, false);
        this.parent = parent;
        buildObject(title, col, list.toArray(new String[list.size()][]), dim);
    }
    //===============================================================
    /**
     * Creates new form PopupTable
     *
     * @param dim default size
     * @throws fr.esrf.Tango.DevFailed in case of problem to display in table.
     * @param    parent    parent component.
     * @param    title    Widow title.
     * @param    col        columns title.
     * @param    array    array of String arrays.
     */
    //===============================================================
    public PopupTable(JFrame parent, String title, String[] col, String[][] array, Dimension dim) throws DevFailed {
        super(parent, false);
        this.parent = parent;
        buildObject(title, col, array, dim);

        //	Check if from an appli or from an empty JFrame
        if (parent.getWidth() == 0)
            fromApplication = false;
    }

    //===============================================================
    //===============================================================
    private void readDataFile(String filename)
            throws SecurityException,
            IOException,
            DevFailed {
        FileInputStream fid = new FileInputStream(filename);
        int nb = fid.available();
        byte[] inStr = new byte[nb];
        int nb1 = fid.read(inStr);
        if (nb1 == 0)
            return;
        String str = new String(inStr);
        fid.close();

        //	Get all lines
        List<String> tokens = new ArrayList<>();
        StringTokenizer stk = new StringTokenizer(str, "\n");
        while (stk.hasMoreTokens())
            tokens.add(stk.nextToken());
        System.out.println(tokens.size() + " lines");

        //	Split each line
        data = new String[tokens.size() - 1][];
        for (int i = 0; i < tokens.size(); i++) {
            String line = tokens.get(i);
            stk = new StringTokenizer(line, "\t");

            //	First line is column's title
            if (i == 0) {
                col_names = new String[stk.countTokens()];
                for (int j = 0; stk.hasMoreTokens(); j++)
                    col_names[j] = stk.nextToken();
            } else {
                data[i - 1] = new String[stk.countTokens()];
                for (int j = 0; stk.hasMoreTokens(); j++)
                    data[i - 1][j] = stk.nextToken();
            }
        }
    }

    //===============================================================
    //===============================================================
    private void buildObject(String title, String[] col, String[][] array, Dimension dim) throws DevFailed {
        col_names = col;
        initComponents();
        data = array;
        initMyComponents(dim);
        titleLabel.setText(title);

        if (parent.getWidth() > 0) {
            //	Put on top left corner
            Point p = parent.getLocationOnScreen();
            p.x += 10;
            p.y += 10;
            setLocation(p);
        }
        pack();
    }
    //===============================================================
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    //===============================================================
    private void initComponents() {//GEN-BEGIN:initComponents
        JPanel  jPanel1 = new javax.swing.JPanel();
        JButton cancelBtn = new javax.swing.JButton();
        JPanel  jPanel2 = new javax.swing.JPanel();
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
    }//GEN-END:initComponents

    //===============================================================
    //===============================================================
    private JTable my_table;
    private JScrollPane scrollPane;

    private void initMyComponents(Dimension dim) throws DevFailed {
        try {
            model = new DataTableModel();

            // Create the table
            final JTable table = new JTable(model);
            table.setRowSelectionAllowed(true);
            table.setColumnSelectionAllowed(true);
            table.setDragEnabled(true);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setFont(new java.awt.Font("Dialog", 1, 14));
            table.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    tableActionPerformed(evt);
                }
            });

            //	Put it in a scrolled pane
            scrollPane = new JScrollPane(table);
            if (dim == null) {
                int heith = 18 + 18 * data.length;
                if (heith > 400) heith = 400;
                scrollPane.setPreferredSize(new Dimension(650, heith));
            } else
                scrollPane.setPreferredSize(dim);

            getContentPane().add(scrollPane, BorderLayout.CENTER);
            my_table = table;
        } catch (Exception e) {
            e.printStackTrace();
            Except.throw_exception("INIT_ERROR",
                    e.toString(),
                    "PopupTable.initMyComponents()");
        }
        //model.setTable(data);
        model.fireTableDataChanged();
    }

    //===============================================================
    //===============================================================
    public void setSortAvailable(boolean b) {
        sortAvailable = b;
    }

    //===============================================================
    //===============================================================
    private void tableActionPerformed(java.awt.event.MouseEvent evt) {

        int column = my_table.getTableHeader().columnAtPoint(
                new Point(evt.getX(), evt.getY()));
        if (sortAvailable)
            new UsedData().sort(column);

        model.fireTableDataChanged();
    }

    //===============================================================
    //===============================================================
    public void setColumnWidth(int[] width) {
        final Enumeration cenum = my_table.getColumnModel().getColumns();
        TableColumn tc;
        int sp_width = 0;
        for (int i = 0; cenum.hasMoreElements(); i++) {
            tc = (TableColumn) cenum.nextElement();
            tc.setPreferredWidth(width[i]);
            sp_width += width[i];
        }

        scrollPane.setPreferredSize(new Dimension(sp_width, 450));
        pack();
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
        if (!fromApplication)
            System.exit(0);
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
            Collections.sort(this, new StringArrayComparator());

            for (int i = 0; i < size(); i++)
                data[i] = get(i);
        }

        private int column;
        //======================================================
        /**
         * StringCompare class to sort collection
         */
        //======================================================
        class StringArrayComparator implements Comparator<String[]> {
            public int compare(String[] a1, String[] a2) {

                String s1 = a1[column];
                String s2 = a2[column];

                //	Check if number
                try {
                    double d1 = Double.parseDouble(s1);
                    double d2 = Double.parseDouble(s2);
                    if (d1==d2) return 0;
                    return ((d1 > d2) ? 1 : -1);
                } catch (NumberFormatException e) { /* */ }

                //	Sort as String
                return s1.compareToIgnoreCase(s2);
            }
        }
    }
    //=========================================================================
    //=========================================================================






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
                return col_names[getColumnCount() - 1];
            else
                return col_names[aCol];
        }

        //==========================================================
        //==========================================================
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
        //==========================================================
        //==========================================================
    }


    //=========================================================================
    //=========================================================================
    public static void main(String args[]) {
        if (args.length == 0) {
            System.out.println("File's name to find data ?");
            System.exit(0);
        }

        try {
            new PopupTable(new JFrame(), args[0]).setVisible(true);
        } catch (Exception e) {
            ErrorPane.showErrorMessage(new JFrame(), null, e);
            e.printStackTrace();
            System.exit(0);
        }
    }
}
