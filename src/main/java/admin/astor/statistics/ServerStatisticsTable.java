//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author: verdier $
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
// $Revision:  $
//
//-======================================================================


package admin.astor.statistics;

import fr.esrf.Tango.DevState;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

//=======================================================

/**
 * JLChart Class to display statistics
 *
 * @author Pascal Verdier
 */
//=======================================================
public class ServerStatisticsTable extends JTable {
    private ServerStat serverStatistics;
    private ServerStat filteredStatistics;
    private DataTableModel model;

    //	Column Definitions
    private static final int STATE = 0;
    private static final int START_TIME = 1;
    private static final int END_TIME = 2;
    private static final int DURATION = 3;
    private static final int AUTO_START = 4;

    class Column {
        String name;
        int width;

        Column(String name, int width) {
            this.name = name;
            this.width = width;
        }
    }

    private final Column[] columns = {
            new Column("State", 50),
            new Column("Begin", 150),
            new Column("End", 150),
            new Column("Duration", 170),
            new Column("Started", 50),
    };
    //=======================================================

    /**
     * Creates new JTable to display statistics
     *
     * @param serverStatistics specified server statistics
     */
    //=======================================================
    public ServerStatisticsTable(ServerStat serverStatistics) {
        this.serverStatistics = serverStatistics;
        copyServerStat();
        buildTableComponent();
    }

    //=======================================================
    //=======================================================
    private void copyServerStat() {
        filteredStatistics = new ServerStat(serverStatistics.name);
        filteredStatistics.starterStat = serverStatistics.starterStat;
        filteredStatistics.nbFailures = serverStatistics.nbFailures;
        filteredStatistics.failedDuration = serverStatistics.failedDuration;
        filteredStatistics.runDuration = serverStatistics.runDuration;
        filteredStatistics.oldestTime = serverStatistics.oldestTime;
        filteredStatistics.addAll(serverStatistics);
    }

    //=======================================================
    //=======================================================
    private void buildTableComponent() {
        //  Create the table.
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setDragEnabled(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableActionPerformed(evt);
            }
        });
        model = new DataTableModel();
        setModel(model);
        setDefaultRenderer(String.class, new StatCellRenderer());

        //  Manage column headers
        getTableHeader().setFont(new java.awt.Font("Dialog", 1, 12));
        getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                headerTableActionPerformed(evt);
            }
        });

        //  Fix width for columns
        final Enumeration enumeration = getColumnModel().getColumns();
        int i = 0;
        TableColumn tc;
        while (enumeration.hasMoreElements()) {
            tc = (TableColumn) enumeration.nextElement();
            tc.setPreferredWidth(columns[i++].width);
        }
        sort(START_TIME);
    }

    //===============================================================
    //===============================================================
    public int getDefaultHeight() {
        int max = 600;
        int height = 22 + 17 * filteredStatistics.size();
        if (height > max) height = max;
        return height;
    }

    //===============================================================
    //===============================================================
    public int getDefaultWidth() {
        int width = 0;
        for (Column column : columns)
            width += column.width;
        return width;
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void tableActionPerformed(MouseEvent evt) {

        /*
        if(evt.getClickCount() != 2)
            return;
        //	get selected cell
        int column = columnAtPoint(new Point(evt.getX(), evt.getY()));
        int row    = rowAtPoint(new Point(evt.getX(), evt.getY()));
        ServerRecord  rec = serverStatistics.get(row);
        */
    }

    //===============================================================
    //===============================================================
    private void headerTableActionPerformed(MouseEvent evt) {

        sort(getTableHeader().columnAtPoint(
                new Point(evt.getX(), evt.getY())));
    }

    //=======================================================
    //=======================================================
    private void sort(int column) {
        MyCompare compare = new MyCompare();
        compare.setSelectedColumn(column);
        Collections.sort(filteredStatistics, compare);

        model.fireTableDataChanged();
    }

    //==========================================================
    //==========================================================
    private String getRecordValueString(ServerRecord record, int column) {

        switch (column) {
            case STATE:
                return record.stateName;
            case START_TIME:
                return Utils.formatDate(record.startTime);
            case END_TIME:
                return Utils.formatDate(record.endTime);
            case DURATION:
                return Utils.formatDuration(record.duration);
            case AUTO_START:
                if (record.state == DevState.ON) {
                    //return Boolean.toString(record.autoRestart);
                    if (record.autoRestart == ServerRecord.START_AUTO)
                        return "Auto";
                    else if (record.autoRestart == ServerRecord.START_REQUEST)
                        return "On Req.";
                    else
                        return "";
                } else
                    return "";
        }
        return "--";
    }

    //=======================================================
    //=======================================================
    public void setFilterOnRunning(boolean b) {
        filteredStatistics.clear();
        for (ServerRecord rec : serverStatistics)
            if (b || rec.state == DevState.FAULT)
                filteredStatistics.add(rec);
        model.fireTableDataChanged();
    }
    //=======================================================
    //=======================================================


    //=========================================================================
    //=========================================================================
    public class DataTableModel extends DefaultTableModel {
        //==========================================================
        @Override
        public int getColumnCount() {
            return columns.length;
        }
        //==========================================================
        @Override
        public int getRowCount() {
            return filteredStatistics.size();
        }
        //==========================================================
        @Override
        public String getColumnName(int col) {
            return columns[col].name;
        }
        //==========================================================
        @Override
        public Object getValueAt(int row, int col) {
            ServerRecord record = filteredStatistics.get(row);
            return getRecordValueString(record, col);
        }

        //==========================================================
        /*
        * JTable uses this method to determine the default renderer/
        * editor for each cell.  If we didn't implement this method,
        * then the last column would contain text ("true"/"false"),
        * rather than a check box.
        */
        //==========================================================
        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        //==========================================================
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        //==========================================================
    }
    //=========================================================================
    //=========================================================================


    //==========================================================
    //==========================================================
    public class StatCellRenderer extends JLabel
            implements TableCellRenderer {

        public StatCellRenderer() {
            setFont(new Font("Dialog", Font.PLAIN, 12));
            setOpaque(true); //MUST do this for background to show up.
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            ServerRecord record = filteredStatistics.get(row);
            if (record.state == DevState.FAULT)
                setBackground(Color.orange);
            else
                setBackground(Color.green);
            setText(getRecordValueString(record, column));

            //  set tool tips
            if (column == AUTO_START) {
                if (record.autoRestart == ServerRecord.START_AUTO)
                    setToolTipText("Auto restarted by Starter");
                else if (record.autoRestart == ServerRecord.START_REQUEST)
                    setToolTipText("Started on Request");
                else
                    setToolTipText("");
            } else
                setToolTipText("");

            return this;
        }
    }
    //==========================================================
    //==========================================================


    //======================================================

    /**
     * MyCompare class to sort collection
     */
    //======================================================
    class MyCompare implements Comparator<ServerRecord> {
        private int column;

        private void setSelectedColumn(int column) {
            this.column = column;
        }

        public int compare(ServerRecord record1, ServerRecord record2) {
            switch (column) {
                case START_TIME:
                    return ((record1.startTime < record2.startTime) ? 1 : 0);
                case END_TIME:
                    return ((record1.endTime < record2.endTime) ? 1 : 0);
                case DURATION:
                    return ((record1.duration < record2.duration) ? 1 : 0);
                case AUTO_START:
                    return (record1.autoRestart < record2.autoRestart) ? 1 : 0;
            }
            //	default case by state string
            return ((record1.stateName.compareTo(record2.stateName) > 0) ? 1 : 0);
        }
    }
}
