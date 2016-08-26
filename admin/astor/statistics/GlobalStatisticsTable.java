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

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;

/**
 * JLChart Class to display statistics
 *
 * @author Pascal Verdier
 */
public class GlobalStatisticsTable extends JTable {
    private JFrame parent;
    private List<ServerStat> serverStatistics;
    private List<ServerStat> filteredServerStatistics = new ArrayList<>();
    private DataTableModel model;
    private TablePopupMenu menu;


    //	Column Definitions
    private static final int SERVER_NAME = 0;
    private static final int HOST_NAME = 1;
    private static final int NB_FAILURES = 2;
    private static final int TIME_FAILURE = 3;
    private static final int AVAILABILITY = 4;
    private static final int LAST_FAILURE = 5;

    //private static final    int TIME_RUNNING = 4;
    class Column {
        String name;
        int width;

        Column(String name, int width) {
            this.name = name;
            this.width = width;
        }
    }

    private final Column[] columns = {
            new Column("Server Name", 200),
            new Column("Host Name", 100),
            new Column("Failures", 50),
            new Column("Failure Duration", 150),
            new Column("Availability", 80),
            new Column("Last Failure", 150),
            //new Column("Running Duration", 150),
    };
    //=======================================================

    /**
     * Creates new JTable to display statistics
     *
     * @param parent JFrame parent instance
     */
    //=======================================================
    public GlobalStatisticsTable(JFrame parent) {
        this.parent = parent;

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

        menu = new TablePopupMenu(this);
    }

    //===============================================================
    //===============================================================
    public int getDefaultHeight() {
        int max = 400;
        int height = 22 + 17 * filteredServerStatistics.size();
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
    private void tableActionPerformed(MouseEvent evt) {

        int column = columnAtPoint(new Point(evt.getX(), evt.getY()));
        int row = rowAtPoint(new Point(evt.getX(), evt.getY()));
        //	get selected cell
        ServerStat server = filteredServerStatistics.get(row);
        if (evt.getButton() == MouseEvent.BUTTON3) {
            switch (column) {
                case SERVER_NAME:
                    menu.showMenu(evt, server);
                    break;
                case HOST_NAME:
                    menu.showMenu(evt, server.starterStat);
                    break;
            }
        } else if (evt.getButton() == MouseEvent.BUTTON1) {
            if (evt.getClickCount() == 2) {
                switch (column) {
                    case SERVER_NAME:
                        displayServerStat(server);
                        break;
                    case HOST_NAME:
                        displayStarterStat(server.starterStat);
                        break;
                }
            }
        }
    }

    //===============================================================
    //===============================================================
    private void displayServerStat(ServerStat serverStat) {
        String title = serverStat.name + "  registered on  " + serverStat.starterStat.name;
        new ServerStatisticsPanel(parent, title, serverStat).setVisible(true);
    }

    //===============================================================
    //===============================================================
    private void displayStarterStat(StarterStat starterStat) {

        String title = starterStat.name;
        new StarterStatTable(parent, title, starterStat).setVisible(true);
    }

    //===============================================================
    //===============================================================
    private void headerTableActionPerformed(MouseEvent evt) {

		System.out.println("headerTableActionPerformed(evt); called");
        sort(getTableHeader().columnAtPoint(
                new Point(evt.getX(), evt.getY())));
    }

    //=======================================================
    //=======================================================
    private void sort(int column) {
        for (ServerStat serverStat : filteredServerStatistics) {
            System.out.println(serverStat.name);
        }
        Collections.sort(filteredServerStatistics, new ServersComparator(column));
        System.out.println("==============================================================================");
        for (ServerStat serverStat : filteredServerStatistics) {
            System.out.println(serverStat.name);
        }

        model.fireTableDataChanged();
    }

    //=======================================================
    //=======================================================
    public void setStatistics(List<ServerStat> serverStatistics) {
        this.serverStatistics = serverStatistics;
        copyServerStatistics();
        sort(LAST_FAILURE);
        model.fireTableDataChanged();
    }

    //=======================================================
    //=======================================================
    public void setFilter(String startWith) {
        filteredServerStatistics.clear();
        for (ServerStat serverStat : serverStatistics) {
            if (serverStat.name.startsWith(startWith)) {
                filteredServerStatistics.add(serverStat);
            }
        }
        model.fireTableDataChanged();
    }

    //=======================================================
    //=======================================================
    public void resetFilter() {
        copyServerStatistics();
        model.fireTableDataChanged();
    }

    //=======================================================
    //=======================================================
    private void copyServerStatistics() {
        filteredServerStatistics.clear();
        for (ServerStat serverStat : serverStatistics) {
            filteredServerStatistics.add(serverStat);
        }
    }
    //=======================================================
    //=======================================================


    //=========================================================================
    //=========================================================================
    public class DataTableModel extends AbstractTableModel {
        //==========================================================
        //==========================================================
        public int getColumnCount() {
            return columns.length;
        }

        //==========================================================
        //==========================================================
        public int getRowCount() {
            return filteredServerStatistics.size();
        }

        //==========================================================
        //==========================================================
        public String getColumnName(int col) {
            return columns[col].name;
        }

        //==========================================================
        //==========================================================
        public Object getValueAt(int row, int col) {
            ServerStat server = filteredServerStatistics.get(row);
            return getServerValueString(server, col);
        }

        //==========================================================
        //==========================================================
        private String getServerValueString(ServerStat server, int column) {

            switch (column) {
                case SERVER_NAME:
                    return server.name;
                case HOST_NAME:
                    return server.starterStat.name;
                case NB_FAILURES:
                    return Integer.toString(server.nbFailures);
                case TIME_FAILURE:
                    return Utils.formatDuration(server.failedDuration);
                //case TIME_RUNNING:
                //    return Utils.formatDuration(server.runDuration);
                case AVAILABILITY:
                    return Utils.formatPercentage(server.getAvailability());
                case LAST_FAILURE:
                    long t = server.getLastFailure();
                    if (t > 0)
                        return Utils.formatDate(t);
                    else
                        return " ? ? ";
            }
            return "--";
        }
        //==========================================================
        //==========================================================
    }


    //======================================================

    /**
     * MyCompare class to sort collection
     */
    //======================================================
    class ServersComparator implements Comparator<ServerStat> {
        private int column;

        private ServersComparator(int column) {
            this.column = column;
        }

        public int compare(ServerStat server1, ServerStat server2) {
            switch (column) {
                case SERVER_NAME:
                    return server1.name.compareToIgnoreCase(server2.name);
                case HOST_NAME:
                    return server1.starterStat.name.compareToIgnoreCase(server2.starterStat.name);
                case NB_FAILURES:
                    if (server1.nbFailures == server2.nbFailures) return 0;
                    return ((server1.nbFailures < server2.nbFailures) ? 1 : -1);
                case TIME_FAILURE:
                    if (server1.failedDuration < server2.failedDuration) return 0;
                    return ((server1.failedDuration < server2.failedDuration) ? 1 : -1);
                case AVAILABILITY:
                    if (server1.getAvailability() < server2.getAvailability()) return 0;
                    return ((server1.getAvailability() < server2.getAvailability()) ? 1 : -1);
                case LAST_FAILURE:
                    if (server1.getLastFailure() < server2.getLastFailure()) return 0;
                    return ((server1.getLastFailure() < server2.getLastFailure()) ? 1 : -1);
            }
            //	default case by name
            return server1.name.compareToIgnoreCase(server2.name);
        }
    }
    //==============================================================================
    //==============================================================================


    //==============================================================================
    //==============================================================================
    static private final int OFFSET = 2;
    static private final int SERVER_STAT = 0;
    static private final int HOST_STAT = 1;

    static private String[] menuLabels = {
            "Server statistics",
            "Host statistics",
    };

    private class TablePopupMenu extends JPopupMenu {
        private JTable table;
        private ServerStat serverStat;
        private StarterStat starterStat;
        private JLabel title;
        //=======================================================

        /**
         * Create a Popup menu for JTable object
         *
         * @param table specified JTable instance
         */
        //=======================================================
        private TablePopupMenu(JTable table) {
            this.table = table;
            title = new JLabel();
            title.setFont(new java.awt.Font("Dialog", 1, 16));
            add(title);
            add(new JPopupMenu.Separator());

            for (String menuLabel : menuLabels) {
                if (menuLabel == null)
                    add(new Separator());
                else {
                    JMenuItem btn = new JMenuItem(menuLabel);
                    btn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            menuActionPerformed(evt);
                        }
                    });
                    add(btn);
                }
            }
        }

        //======================================================
        private void showMenu(MouseEvent evt, ServerStat serverStat) {

            title.setText(serverStat.name);
            this.serverStat = serverStat;
            //noinspection PointlessArithmeticExpression
            getComponent(OFFSET + SERVER_STAT).setVisible(true);
            getComponent(OFFSET + HOST_STAT).setVisible(false);
            show(table, evt.getX(), evt.getY());
        }

        //======================================================
        private void showMenu(MouseEvent evt, StarterStat starterStat) {

            title.setText(starterStat.name);
            this.starterStat = starterStat;
            //noinspection PointlessArithmeticExpression
            getComponent(OFFSET + SERVER_STAT).setVisible(false);
            getComponent(OFFSET + HOST_STAT).setVisible(true);
            show(table, evt.getX(), evt.getY());
        }

        //======================================================
        private void menuActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int cmdidx = 0;
            for (int i = 0; i < menuLabels.length; i++)
                if (getComponent(OFFSET + i) == obj)
                    cmdidx = i;

            switch (cmdidx) {
                case SERVER_STAT:
                    displayServerStat(serverStat);
                    break;
                case HOST_STAT:
                    displayStarterStat(starterStat);
                    break;
            }
        }
    }
    //======================================================
}
