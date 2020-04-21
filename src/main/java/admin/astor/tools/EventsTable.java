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
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;


//===============================================================

/**
 * Class Description: Dialog Class to display subscribed events
 *
 * @author Pascal Verdier
 */
//===============================================================


@SuppressWarnings({"MagicConstant", "WeakerAccess"})
public class EventsTable extends JDialog {
    /**
     * Subscribe mode definitions
     */
    public static final int SUBSCRIBE_CHANGE = 0;
    public static final int SUBSCRIBE_PERIODIC = 1;
    public static final int SUBSCRIBE_ARCHIVE = 2;
    public static final String[] strMode = {
            "CHANGE", "PERIODIC", "ARCHIVE"
    };

    /**
     * Events Table
     */
    public JTable table;
    private DataTableModel model;
    private JScrollPane scrollPane;

    /**
     * Names of the columns in the table
     */
    private static String[] col_names = {
            "Signal names", "Read Value", "Mode",
            "Last Time", "Delta Time", "Delta Value", "Received", "Source"};
    public static final int NAME = 0;
    public static final int VALUE = 1;
    public static final int MODE = 2;
    public static final int TIME = 3;
    public static final int DT = 4;
    public static final int DV = 5;
    public static final int CNT = 6;

    private static int[] col_width = {140, 100, 45, 85, 60, 100, 50, 50};
    /**
     * An array of String array for data to be displayed
     */
    private List<SubscribedSignal> signals = new ArrayList<>();
    private boolean first = true;
    private TablePopupMenu menu = null;
    private Component parent;
    /**
     * File Chooser Object used in file menu.
     */
    static private JFileChooser chooser;
    private static final String ColWidthHeader = "Column_width:";
    private static final String FileHeader =
            "#\n#	EventTester :	event list\n#\n";

    //===============================================================
    /*
     *	Creates new form EventsTable
     */
    //===============================================================
    public EventsTable(JFrame parent) throws DevFailed {
        this(parent, false);
    }
    //===============================================================
    /*
     *	Creates new form EventsTable
     */
    //===============================================================
    public EventsTable(JFrame parent, boolean center) throws DevFailed {
        super(parent, center);
        this.parent = parent;
        initializeForm(center);
    }

    //===============================================================
    /*
     *	Creates new form EventsTable
     */
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    public EventsTable(JDialog parent) throws DevFailed {
        super(parent, false);
        this.parent = parent;
        initializeForm(false);
    }

    //===============================================================
    //===============================================================
    private void initializeForm(boolean center) throws DevFailed {
        initComponents();
        initMyComponents();
        titleLabel.setText("TANGO  Event Tester");

        //	Set screen position
        if (center) {
            ATKGraphicsUtils.centerDialog(this);
        }
        else
        if (parent != null && parent.isVisible()) {

            //  Set it at bottom
            Point p = parent.getLocationOnScreen();
            int h = parent.getHeight();
            p.y += h;
            //  if too low -> put it at top
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension scrsize = toolkit.getScreenSize();
            if (p.y > (scrsize.height - getHeight() - 20))
                p.y -= (h + getHeight());
            setLocation(p);
        }

        //	File menu
        fileMenu.setMnemonic('F');
        openFile.setMnemonic('O');
        openFile.setAccelerator(KeyStroke.getKeyStroke('O', MouseEvent.CTRL_MASK));

        saveFile.setMnemonic('S');
        saveFile.setAccelerator(KeyStroke.getKeyStroke('S', MouseEvent.CTRL_MASK));
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JMenuBar jMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openFile = new javax.swing.JMenuItem();
        saveFile = new javax.swing.JMenuItem();

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

        fileMenu.setText("File");

        openFile.setText("Open");
        openFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileActionPerformed(evt);
            }
        });
        fileMenu.add(openFile);

        saveFile.setText("Save");
        saveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileActionPerformed(evt);
            }
        });
        fileMenu.add(saveFile);

        jMenuBar.add(fileMenu);

        setJMenuBar(jMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void openFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileActionPerformed
        //	Fixe the input directory
        if (chooser == null) {
            String homeDir;
            if ((homeDir = System.getProperty("EVT_DATA_FILES")) == null)
                homeDir = new File("").getAbsolutePath();
            chooser = new JFileChooser(homeDir);
        }

        chooser.setDialogTitle("Open Configuration File");
        chooser.setApproveButtonText("Open");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null && !file.isDirectory())
                openEventList(file.getAbsolutePath());
        }
    }//GEN-LAST:event_openFileActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void saveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileActionPerformed

        if (signals.size() == 0) {
            Utils.popupError(this, "No subscription to save");
            return;
        }
        //	Fixe the input directory
        if (chooser == null) {
            String homeDir;
            if ((homeDir = System.getProperty("EVT_DATA_FILES")) == null)
                homeDir = new File("").getAbsolutePath();
            chooser = new JFileChooser(homeDir);
        }

        chooser.setDialogTitle("Save Configuration");
        chooser.setApproveButtonText("Save");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null && !file.isDirectory()) {
                //	If exists , ask for confirm
                if (file.exists())
                    if (JOptionPane.showConfirmDialog(this,
                            "This File Already Exists !\n\n" +
                                    "Would you like to overwrite ?",
                            "information",
                            JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;

                saveEventList(file.getAbsolutePath());
            }
        }

    }//GEN-LAST:event_saveFileActionPerformed

    //===============================================================
    //===============================================================
    private void saveEventList(String filename) {
        StringBuilder sb = new StringBuilder(FileHeader);
        //	Get signal list
        for (SubscribedSignal signal : signals) {
            sb.append(signal.toString()).append("\n");
        }
        //	Get columns width
        int[] width = getColumnWidth();
        sb.append("#\n" + ColWidthHeader + "	");
        for (int aWidth : width)
            sb.append(" ").append(aWidth);
        try {
            FileOutputStream fidout = new FileOutputStream(filename);
            fidout.write(sb.toString().getBytes());
            fidout.close();


        } catch (Exception e) {
            Utils.popupError(this, null, e);
        }
    }

    //===============================================================
    //===============================================================
    private void openEventList(String filename) {
        String str;
        try {
            FileInputStream fid = new FileInputStream(filename);
            int nb = fid.available();
            byte[] inStr = new byte[nb];
            nb = fid.read(inStr);
            if (nb == 0)
                return;
            str = new String(inStr);
            fid.close();
        } catch (Exception e) {
            Utils.popupError(this, null, e);
            return;
        }

        //	Check if it is an EventTester Configuration file
        if (!str.startsWith(FileHeader)) {
            Utils.popupError(this,
                    "This is not an EventTester Configuration file");
            return;
        }
        //	get each line
        str = str.substring(FileHeader.length());
        StringTokenizer stk = new StringTokenizer(str, "\n");
        while (stk.hasMoreTokens()) {
            String line = stk.nextToken();
            if (!line.startsWith("#"))
                if (!line.startsWith(ColWidthHeader))
                    createSignalFromLine(line);
                else
                    readColWidthDefinition(line);
        }

        setColumnWidth(col_width);
        pack();
    }

    //===============================================================
    //===============================================================
    private void readColWidthDefinition(String line) {
        StringTokenizer stk = new StringTokenizer(line);
        stk.nextToken();    //	for header

        //	get culumn width
        col_width = new int[stk.countTokens()];
        for (int i = 0; stk.hasMoreTokens(); i++) {
            try {
                String s = stk.nextToken();
                col_width[i] = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Cannot parse width for column " + i);
                col_width[i] = 50;
            }
        }
    }

    //===============================================================
    //===============================================================
    private void createSignalFromLine(String line) {
        //	Separate name and mode
        StringTokenizer stk = new StringTokenizer(line);
        String name = stk.nextToken();
        //	Get subscribe mode
        String strmode = stk.nextToken().substring(1); //	remove [
        strmode = strmode.substring(0, strmode.indexOf(']'));

        //	Convert string mode to integer
        int mode = -1;
        for (int i = 0; i < strMode.length; i++)
            if (strmode.equals(strMode[i]))
                mode = i;
        if (mode == -1) {
            Utils.popupError(this, "mode " + strmode + " is unknown !");
            return;
        }
        add(name, mode);
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        doClose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    //===============================================================

    /**
     * Closes the dialog
     */
    //===============================================================
    private void doClose() {
        if (parent != null) {
            getColumnWidth();
            setVisible(false);
            dispose();
        } else
            System.exit(0);
    }


    //===============================================================
    //===============================================================
    public void add(String name, int mode) {
        //  Check if already added
        for (int i = 0; i < signals.size(); i++) {
            SubscribedSignal sig = signals.get(i);
            if (sig.name.toLowerCase().equals(name.toLowerCase()) && sig.mode == mode) {
                setVisible(true);
                table.changeSelection(i, 0, false, false);
                Utils.popupError(this, "Event \'" + name + "\'  already subscribed");
                return;
            }
        }
        SubscribedSignal sig = new SubscribedSignal(name, mode);
        sig.subscribe(this);
        signals.add(sig);
        setVisible(true);
        updateTable();

        Dimension tableSize = table.getSize();
        tableSize.height += 40;    //  Header height
        scrollPane.setPreferredSize(new Dimension(tableSize));
        pack();
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void buildTitle(ActionEvent evt) {
        String title = "Attribute events";
        String date = SubscribedSignal.getStrDate();
        titleLabel.setText(title + " at " + date);
    }

    //===============================================================
    //===============================================================
    void updateTable() {
        model.fireTableDataChanged();
    }

    //===============================================================
    //===============================================================
    private void initMyComponents() throws DevFailed {
        try {
            //	Initialise the final JTable model
            model = new DataTableModel();

            // Create the table
            table = new JTable(model) {
                //Implement table cell tool tips.
                public String getToolTipText(MouseEvent e) {
                    String tip = null;
                    Point p = e.getPoint();
                    int row = rowAtPoint(p);
                    int col = columnAtPoint(p);
                    int realColumnIndex = convertColumnIndexToModel(col);

                    SubscribedSignal signal = signals.get(row);
                    switch (realColumnIndex) {
                        case NAME:
                            tip = signal.name;
                            break;
                        case VALUE:
                            if (signal.except == null)
                                tip = signal.value;
                            else
                                tip = signal.except_str();
                            break;
                        case DT:
                            tip = signal.getTimes();
                            break;
                    }
                    return tip;
                }
            };
            //table.setRowSelectionAllowed(true);
            //table.setColumnSelectionAllowed(true);
            //table.setDragEnabled(true);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setFont(new java.awt.Font("Dialog", 1, 14));
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    tableActionPerformed(evt);
                }
            });

            //	Put it in a scrolled pane
            scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(700, 50));

            getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

            //getContentPane().add(table, java.awt.BorderLayout.CENTER);

            //	if first time , remove dummy label and display time
            if (first) {
                //	Start a timer to display date and time
                int delay = 1000; //milliseconds
                ActionListener taskPerformer = this::buildTitle;
                new javax.swing.Timer(delay, taskPerformer).start();
                first = false;
            }
            setColumnWidth(col_width);
            pack();
        } catch (Exception e) {
            e.printStackTrace();
            Except.throw_exception("INIT_ERROR",
                    e.toString(),
                    "TestEventTable.initMyComponents()");
        }
        model.fireTableDataChanged();
    }

    //===============================================================
    //===============================================================
    private void tableActionPerformed(java.awt.event.MouseEvent evt) {

        if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
            //	Retreive data fro clicked cell
            int row = table.rowAtPoint(new Point(evt.getX(), evt.getY()));

            SubscribedSignal signal = signals.get(row);

            if (signal == null)
                return;

            //	Create menu if not already done and display
            if (menu == null)
                menu = new TablePopupMenu(this);
            menu.showMenu(evt, signal);
        }
    }

    //===============================================================
    //===============================================================
    void move(SubscribedSignal signal, int direction) {
        //  Search signal position
        int idx = 0;
        for (int i = 0; i < signals.size(); i++)
            if (signals.get(i).equals(signal))
                idx = i;
        switch (direction) {
            case TablePopupMenu.UP:
                if (idx > 0) {
                    signals.remove(signal);
                    signals.add(idx - 1, signal);
                }
                break;
            case TablePopupMenu.DOWN:
                if (idx < (signals.size() - 1)) {
                    signals.remove(signal);
                    signals.add(idx + 1, signal);
                }
                break;
        }
        updateTable();
    }

    //===============================================================
    //===============================================================
    void displayHistory(SubscribedSignal signal) {
        new HistoryDialog(this, signal).setVisible(true);
    }

    //===============================================================
    //===============================================================
    void displayInfo(SubscribedSignal signal) {
        if (signal.except != null)
            Utils.popupError(this, null, signal.except);
        else
            Utils.popupError(this, signal.status());
    }

    //===============================================================
    //===============================================================
    void remove(SubscribedSignal signal) {
        signal.unsubscribe();
        signals.remove(signal);
        updateTable();
        Dimension tableSize = table.getSize();
        tableSize.height += 40;    //  Column header height
        scrollPane.setPreferredSize(new Dimension(tableSize));
        pack();
    }

    //===============================================================
    //===============================================================
    public void setColumnWidth(int[] width) {
        final Enumeration cenum = table.getColumnModel().getColumns();
        TableColumn tc;
        for (int i = 0; cenum.hasMoreElements(); i++) {
            tc = (TableColumn) cenum.nextElement();
            tc.setPreferredWidth(width[i]);
        }
    }

    //===============================================================
    //===============================================================
    public int[] getColumnWidth() {
        final Enumeration cellNumber = table.getColumnModel().getColumns();
        List<TableColumn> list = new ArrayList<>();
        while (cellNumber.hasMoreElements())
            list.add((TableColumn) cellNumber.nextElement());

        //	Copy to array
        int[] width = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            TableColumn tc = list.get(i);
            width[i] = tc.getPreferredWidth();
        }
        return width;
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedDeclaration")
    public List<String> getSubscribedNames() {
        List<String>   list = new ArrayList<>();
        for (SubscribedSignal signal : signals)
            list.add(signal.name);
        return list;
    }
    //===============================================================
    //===============================================================


    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem openFile;
    private javax.swing.JMenuItem saveFile;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    //===============================================================


    //===============================================================
    //===============================================================
    static void displaySyntax() {
        System.out.println("Syntax:");
        System.out.println("EventsTable -a  <attribute list>");
        System.out.println("EventsTable -f  <file name>");
        System.exit(0);
    }

    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        if (args.length > 0)
            if (args[0].equals("-?"))
                EventsTable.displaySyntax();
        try {
            EventsTable table = new EventsTable(new JFrame());
            if (args.length > 1)
                if (args[0].equals("-a")) {
                    for (int i = 1; i < args.length; i++)
                        table.add(args[i], SUBSCRIBE_ARCHIVE);
                } else if (args[0].equals("-f"))
                    table.openEventList(args[1]);
            table.setVisible(true);
        } catch (DevFailed e) {
            Except.print_exception(e);
        }
    }


    //=========================================================================
    //=========================================================================
    public class DataTableModel extends AbstractTableModel {
        //==========================================================
        //==========================================================
        public int getColumnCount() {
            return col_names.length;
        }

        //==========================================================
        //==========================================================
        public int getRowCount() {
            return signals.size();
        }

        //==========================================================
        //==========================================================
        public String getColumnName(int aCol) {
            return col_names[aCol];
        }

        //==========================================================
        //==========================================================
        public Object getValueAt(int row, int col) {
            SubscribedSignal sig = signals.get(row);
            switch (col) {
                case NAME:
                    return sig.name;
                case VALUE:
                    return sig.value;
                case MODE:
                    return strMode[sig.mode];
                case TIME:
                    return sig.time;
                case DT:
                    return sig.d_time;
                case DV:
                    return sig.d_value;
                case CNT:
                    return "" + sig.cnt;
            }
            return "";
        }
        //==========================================================
        //==========================================================
    }

}
