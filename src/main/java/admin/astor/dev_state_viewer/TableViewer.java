//+======================================================================
// :  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// Pascal Verdier: pascal_verdier $
//
// Copyright (C) :      2004,2005,...................,2018,2019
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
// :  $
//
//-======================================================================

package admin.astor.dev_state_viewer;

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;


/**
 * This class is able to display data on a table
 *
 * @author verdier
 */

public class TableViewer extends JTable {
    private TangoClass tangoClass;
    private int tableWidth = 0;
    private int selectedRow = -1;
    private TablePopupMenu popupMenu = new TablePopupMenu();

    private static final int[] COLUMN_WIDTH = { 350, 90 };
    private static final String[] COLUMN_HEADERS = {
            "Device Names", "States"
    };
    private static final Color firstColumnColor = new Color(0xdd, 0xdd, 0xdd);
    //===============================================================
    //===============================================================
    public TableViewer(TangoClass tangoClass) throws DevFailed {
        this.tangoClass = tangoClass;
        for (TangoDevice device : tangoClass)
            device.createStateCell(this);
        ScalarViewer.attributeList.startRefresher();
        // Create the table
        DataTableModel model = new DataTableModel();
        setModel(model);
        setRowSelectionAllowed(true);
        setDefaultRenderer(String.class, new LabelCellRenderer());
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableActionPerformed(event);
            }
        });
        //  Column header management
        getTableHeader().setFont(new Font("Dialog", Font.BOLD, 14));
        setRowHeight(15);

        //  Set column width
        final Enumeration columnEnum = getColumnModel().getColumns();
        int i = 0;
        TableColumn tableColumn;
        while (columnEnum.hasMoreElements()) {
            tableWidth += COLUMN_WIDTH[i];
            tableColumn = (TableColumn) columnEnum.nextElement();
            tableColumn.setPreferredWidth(COLUMN_WIDTH[i++]);
        }
    }
    //===============================================================
    //===============================================================
    private void tableActionPerformed(MouseEvent event) {
        selectedRow = rowAtPoint(new Point(event.getX(), event.getY()));
        if (event.getClickCount() == 2 && event.getButton()==1) {
            displayStatus();
        }
        else
        if (event.getButton()==3) {
            popupMenu.showMenu(event, tangoClass.get(selectedRow).getName());
        }
        repaint();
    }
    //===============================================================
    //===============================================================
    public int getTableWidth() {
        return tableWidth;
    }
    //===============================================================
    //===============================================================
    private void displayStatus() {
        TangoDevice device = tangoClass.get(selectedRow);
        try {
            JOptionPane.showMessageDialog(this, device.getName() + ":\n"+device.getStatus());
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, device.getName(), e);
        }
    }
    //===============================================================
    //===============================================================
    private void monitorDevice() {
        TangoDevice device = tangoClass.get(selectedRow);
        new atkpanel.MainPanel(device.getName());
    }
    //===============================================================
    //===============================================================
    private void testDevice() {
        TangoDevice device = tangoClass.get(selectedRow);
        AstorUtil.testDevice(this, device.getName());
    }
    //===============================================================
    //===============================================================


    //==============================================================
    /**
     * The Table model
     */
    //==============================================================
    public class DataTableModel extends AbstractTableModel {
        //==========================================================
        @Override
        public int getColumnCount() {
            return COLUMN_HEADERS.length;
        }
        //==========================================================
        @Override
        public int getRowCount() {
            return tangoClass.size();
        }
        //==========================================================
        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex >= getColumnCount())
                return COLUMN_HEADERS[getColumnCount() - 1];
            else
                return COLUMN_HEADERS[columnIndex];
        }
        //==========================================================
        @Override
        public Object getValueAt(int row, int column) {
            //  Done by renderer
            return "";  //rowList.get(row)[column];
        }
        //==========================================================
        @Override
        public void setValueAt(Object value, int row, int column) {
        }
        //==========================================================
        /**
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         *
         * @param column the specified co;umn number
         * @return the cell class at first row for specified column.
         */
        //==========================================================
        @Override
        public Class getColumnClass(int column) {
            if (isVisible()) {
                return getValueAt(0, column).getClass();
            } else
                return null;
        }
        //==========================================================
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        //==========================================================
    }
    //==============================================================
    //==============================================================


    //==============================================================
    /**
     * Renderer to set cell color
     */
    //==============================================================
    public class LabelCellRenderer extends JLabel implements TableCellRenderer {
        //==========================================================
        public LabelCellRenderer() {
            setFont(new Font("Dialog", Font.BOLD, 12));
            setOpaque(true); //MUST do this for background to show up.
        }
        //==========================================================
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            TangoDevice device = tangoClass.get(row);
            setIcon(null);

            setForeground(Color.black);
            if (column==0) {
                setText(" " + device.getName());
                if (row==selectedRow)
                    setBackground(selectionBackground);
                else
                    setBackground(firstColumnColor);
            }
            else {
                StateCell stateCell = device.getStateCell();
                setText("  " + stateCell.getStatus());
                setBackground(stateCell.getBackground());
                setForeground(stateCell.getForeground());
            }
            return this;
        }
    }
    //==============================================================
    //==============================================================



    //===========================================================
    //===========================================================
    private static final int STATUS = 0;
    private static final int TEST = 1;
    private static final int MONITOR = 2;
    private static final int OFFSET = 2;    //  Label + separator

    private static String[] menuLabels = {
            "Device status",
            "Test device",
            "Monitor device",
    };

    private class TablePopupMenu extends JPopupMenu {
        //=======================================================
        //=======================================================
        private TablePopupMenu() {
            JLabel title = new JLabel("");
            title.setFont(new Font("Dialog", Font.BOLD, 14));
            add(title);
            add(new Separator());

            for (String menuLabel : menuLabels) {
                JMenuItem btn = new JMenuItem(menuLabel);
                btn.addActionListener(this::menuActionPerformed);
                add(btn);
            }
        }
        //======================================================
        public void showMenu(MouseEvent event, String name) {
            ((JLabel)getComponent(0)).setText(name);
            show((Component) event.getSource(), event.getX(), event.getY());
        }
        //======================================================
        private void menuActionPerformed(ActionEvent event) {
            //  Check component source
            Object obj = event.getSource();
            int commandIndex = 0;
            for (int i=0 ; i<menuLabels.length ; i++)
                if (getComponent(OFFSET + i) == obj)
                    commandIndex = i;

            switch (commandIndex) {
                case STATUS:
                    displayStatus();
                    break;
                case TEST:
                    testDevice();
                    break;
                case MONITOR:
                    monitorDevice();
                    break;
            }
        }
    }
    //===============================================================
    //===============================================================
}
