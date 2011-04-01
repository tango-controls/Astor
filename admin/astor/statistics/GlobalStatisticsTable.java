//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for main swing class.
//
// $Author: verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009
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

import admin.astor.tools.PopupTable;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ErrorPane;

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
import java.util.Vector;

//=======================================================
/**
 *	JLChart Class to display statistics
 *
 * @author  Pascal Verdier
 */
//=======================================================
public class GlobalStatisticsTable extends JTable
{
    private JFrame  parent;
    private Vector<ServerStat>  serverStatistics;
    private DataTableModel	    model;
    private TablePopupMenu      menu;

    private static final    String[]    columnNames = {
            "Server Name",  "Host Name",
            "Failures",     "Failure Duration",
            "Running Duration", "Availability",
            "Last Failure",
    };
    private static final    int[]    columnSizes = {
            200, 100, 50, 150, 150, 80, 150
    };
    private static final    int SERVER_NAME  = 0;
    private static final    int HOST_NAME    = 1;
    private static final    int NB_FAILURES  = 2;
    private static final    int TIME_FAILURE = 3;
    private static final    int TIME_RUNNING = 4;
    private static final    int AVAILABILITY = 5;
    private static final    int LAST_FAILURE = 6;
	//=======================================================
    /**
	 *	Creates new JTable to display statistics
     * @param parent JFrame parent instance
	 */
	//=======================================================
    public GlobalStatisticsTable(JFrame parent)
	{
        this.parent = parent;
        //  Create the table.
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setDragEnabled(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addMouseListener (new java.awt.event.MouseAdapter () {
            public void mouseClicked (java.awt.event.MouseEvent evt) {
                tableActionPerformed(evt);
            }
        });
        model = new DataTableModel();
        setModel(model);

        //  Manage column headers
        getTableHeader().setFont(new java.awt.Font("Dialog", 1, 12));
        getTableHeader().addMouseListener (new java.awt.event.MouseAdapter () {
            public void mouseClicked (java.awt.event.MouseEvent evt) {
                headerTableActionPerformed(evt);
            }
        });

        //  Fix width for columns
        final Enumeration enumeration = getColumnModel().getColumns();
        int i = 0;
        TableColumn tc;
        while (enumeration.hasMoreElements()) {
            tc = (TableColumn)enumeration.nextElement();
            tc.setPreferredWidth(columnSizes[i++]);
        }

        menu = new TablePopupMenu(this);
 	}
    //===============================================================
    //===============================================================
    public int getDefaultHeight()
    {
		int	max = 400;
        int height = 22+17*serverStatistics.size();
        if (height>max) height = max;
        return height;
    }
    //===============================================================
    //===============================================================
    public static int getDefaultWidth()
    {
        int width = 0;
        for (int w : columnSizes)
            width += w;
        return width;
    }
    //===============================================================
    //===============================================================
    private void tableActionPerformed(MouseEvent evt) {

        int column = columnAtPoint(new Point(evt.getX(), evt.getY()));
        int row    = rowAtPoint(new Point(evt.getX(), evt.getY()));
        //	get selected cell
        ServerStat  server = serverStatistics.get(row);
        if (evt.getButton()==MouseEvent.BUTTON3) {
            switch (column) {
                case SERVER_NAME:
                    menu.showMenu(evt, server);
                    break;
                case HOST_NAME:
                    menu.showMenu(evt, server.starterStat);
                    break;
            }
        }
        else if (evt.getButton()==MouseEvent.BUTTON1) {
            if(evt.getClickCount() == 2) {
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
        try {
            String  title = serverStat.name + "  registered on  " + serverStat.starterStat.name;
            new ServerStatisticsPanel(parent, title, serverStat).setVisible(true);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }
    //===============================================================
    //===============================================================
    private void displayStarterStat(StarterStat starterStat) {
        try {
            String  title = starterStat.name;
            new PopupTable(parent, title, StarterStat.tableHeader, starterStat.toTable()).setVisible(true);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }
    //===============================================================
    //===============================================================
    private void headerTableActionPerformed(MouseEvent evt) {

        sort (getTableHeader().columnAtPoint(
                new Point(evt.getX(), evt.getY())) );
    }
    //=======================================================
    //=======================================================
    private void sort(int column)
    {
        MyCompare   compare = new MyCompare();
        compare.setSelectedColumn(column);
        Collections.sort(serverStatistics, compare);

        model.fireTableDataChanged();
    }
    //=======================================================
    //=======================================================
    public void setStatistics(Vector<ServerStat> serverStatistics) {
        this.serverStatistics = serverStatistics;
        sort(LAST_FAILURE);
    }
	//=======================================================
	//=======================================================











    //=========================================================================
    //=========================================================================
    public class DataTableModel extends AbstractTableModel
    {
         //==========================================================
         //==========================================================
        public int getColumnCount()
        {
            return columnNames.length;
        }
         //==========================================================
         //==========================================================
        public int getRowCount()
        {
            return serverStatistics.size();
        }
         //==========================================================
         //==========================================================
        public String getColumnName(int aCol) {
            if (aCol>=getColumnCount())
                return columnNames[getColumnCount()-1];
            else
                return columnNames[aCol];
        }
         //==========================================================
         //==========================================================
        public Object getValueAt(int row, int col)
        {
            ServerStat  server = serverStatistics.get(row);
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
                case TIME_RUNNING:
                    return Utils.formatDuration(server.runDuration);
                case AVAILABILITY:
                    return Utils.formatPercentage(server.getAvailability());
                case LAST_FAILURE:
                    long t = server.getLastFailure();
                    if (t>0)
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
     *	MyCompare class to sort collection
     */
    //======================================================
    class  MyCompare implements Comparator<ServerStat>
    {
        private int column;
        private void setSelectedColumn(int column)
        {
            this.column = column;
        }
        public int compare(ServerStat server1, ServerStat server2)
        {
            switch (column) {
                case SERVER_NAME:
                    return ((server1.name.compareToIgnoreCase(server2.name)>0)? 1 : 0);
                case HOST_NAME:
                    return ((server1.starterStat.name.compareToIgnoreCase(server2.starterStat.name)>0)? 1 : 0);
                case NB_FAILURES:
                    return ((server1.nbFailures < server2.nbFailures)? 1 : 0);
                case TIME_FAILURE:
                    return ((server1.failedDuration < server2.failedDuration)? 1 : 0);
                case TIME_RUNNING:
                    return ((server1.runDuration < server2.runDuration)? 1 : 0);
                case AVAILABILITY:
                    return ((server1.getAvailability() < server2.getAvailability())? 1 : 0);
                case LAST_FAILURE:
                    return ((server1.getLastFailure() < server2.getLastFailure())? 1 : 0);
            }
            //	default case by name
            return ((server1.name.compareToIgnoreCase(server2.name)>0)? 1 : 0);
        }
    }
    //==============================================================================
    //==============================================================================




    //==============================================================================
    //==============================================================================
    static private final int	OFFSET         = 2;
    static private final int	SERVER_STAT    = 0;
	static private final int	HOST_STAT      = 1;

	static private String[]	menuLabels = {
            "Server statistics",
            "Host statistics",
        };
	private class TablePopupMenu extends JPopupMenu
	{
        private JTable      table;
        private ServerStat  serverStat;
        private StarterStat starterStat;
        private JLabel      title;
        //=======================================================
        /**
         *	Create a Popup menu for JTable object
         * @param table specified JTable instance
         */
        //=======================================================
        private TablePopupMenu(JTable table)
        {
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
            getComponent(OFFSET+SERVER_STAT).setVisible(true);
            getComponent(OFFSET+HOST_STAT).setVisible(false);
            show(table, evt.getX(), evt.getY());
        }
        //======================================================
        private void showMenu(MouseEvent evt, StarterStat starterStat) {

            title.setText(starterStat.name);
            this.starterStat = starterStat;
            //noinspection PointlessArithmeticExpression
            getComponent(OFFSET+SERVER_STAT).setVisible(false);
            getComponent(OFFSET+HOST_STAT).setVisible(true);
            show(table, evt.getX(), evt.getY());
        }
        //======================================================
        private void menuActionPerformed(ActionEvent evt)
        {
             //	Check component source
            Object	obj = evt.getSource();
            int     cmdidx = 0;
            for (int i=0 ; i<menuLabels.length ; i++)
                if (getComponent(OFFSET+i)==obj)
                    cmdidx = i;

            switch (cmdidx)
            {
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