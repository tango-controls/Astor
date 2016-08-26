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

import admin.astor.Astor;
import admin.astor.AstorUtil;
import admin.astor.HostInfoDialog;
import admin.astor.RemoteLoginThread;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.DeviceProxyFactory;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;

/**
 * JDialog Class to display info
 *
 * @author Pascal Verdier
 */
@SuppressWarnings("MagicConstant")
public class BlackBoxTable extends JDialog {
    private String deviceName;
    private Component parent;
    private DataTableModel model;
    private BlackBox blackbox = null;
    private UpdateThread thread;
    private JDialog table_dialog;
    private FilterDialog filter_dlg = null;
    private TopDialog top_dialog = null;
    private boolean auto_update = false;
    private boolean clearBlackBox = false;
    private int upd_period = 1000;
    private TablePopupMenu menu;

    /**
     * Defining table columns
     */
    private class Column {
        String name;
        int width;
        //==========================
        Column(String n, int w) {
            name = n;
            width = w;
        }
        //==========================
    }

    private final Column[] columns = {
            new Column("Date", 170),
            new Column("Operation", 150),
            new Column("Name", 130),
            new Column("Source", 100),
            new Column("Host", 130),
            new Column("Process", 220),
    };
    static final int DATES = 0;
    static final int OPERATION = 1;
    //static final int NAME = 2;
    //static final int SOURCE = 3;
    static final int HOST = 4;
    static final int PROCESS = 5;


    private static final int height = 400;

    //===============================================================
    /**
     * Creates new form BlackBoxTable
     */
    //===============================================================
    public BlackBoxTable(JFrame parent, String deviceName) throws DevFailed {
        super(parent, false);
        this.parent = parent;
        initDialog(deviceName);
    }
    //===============================================================
    /**
     * Creates new form BlackBoxTable
     */
    //===============================================================
    public BlackBoxTable(JDialog parent, String deviceName) throws DevFailed {
        super(parent, false);
        this.parent = parent;
        initDialog(deviceName);
    }

    //===============================================================
    //===============================================================
    private void initDialog(String devname) throws DevFailed {
        this.deviceName = devname;
        table_dialog = this;
        blackbox = new BlackBox();

        initComponents();

        //	Compute with as sum of column
        int width = 0;
        for (Column col : columns)
            width += col.width;

        //	Initialise the table
        try {
            model = new DataTableModel();

            // Create the table
            final JTable table = new JTable(model) {
                //	Implements table cell tool tip
                public String getToolTipText(MouseEvent e) {
                    String tip = null;
                    if (isVisible()) {
                        Point p = e.getPoint();
                        int row = rowAtPoint(p);
                        if (blackbox != null)
                            tip = blackbox.getCodeLine(row);
                    }
                    return tip;
                }
            };
            table.setRowSelectionAllowed(true);
            table.setColumnSelectionAllowed(true);
            table.setDragEnabled(false);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setFont(new java.awt.Font("Dialog", 1, 14));
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    tableActionPerformed(evt);
                }
            });
            menu = new TablePopupMenu(table);

            //	Put it in a scrolled pane
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(width, height));
            setColumnWidth(table);

            getContentPane().add(scrollPane, BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
            Except.throw_exception("INIT_ERROR",
                    e.toString(),
                    "PopupTable.initMyComponents()");
        }

        thread = new UpdateThread();
        thread.start();

        titleLabel.setText("Black box on  " + devname);
        updPeriodTxt.setText(Integer.toString(upd_period));
        warningLbl.setVisible(false);
        pack();
        ATKGraphicsUtils.centerDialog(this);
    }

    //===============================================================
    //===============================================================
    private void tableActionPerformed(java.awt.event.MouseEvent evt) {

        JTable table = (JTable) evt.getSource();
        int column = table.columnAtPoint(new Point(evt.getX(), evt.getY()));
        int row = table.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (column != HOST)
            return;
        String hostname = blackbox.toString(row, column);
        if (!hostname.equals("polling"))
            menu.showMenu(evt, hostname);
        //new admin.astor.RemoteLoginThread(hostname, this).start();
    }

    //===============================================================
    //===============================================================
    private void setColumnWidth(JTable table) {
        final Enumeration cenum = table.getColumnModel().getColumns();
        TableColumn tc;
        for (int i = 0; i < columns.length && cenum.hasMoreElements(); i++) {
            tc = (TableColumn) cenum.nextElement();
            tc.setPreferredWidth(columns[i].width);
        }
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
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JPanel westPanel = new javax.swing.JPanel();
        autoUpdateBtn = new javax.swing.JRadioButton();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        updPeriodTxt = new javax.swing.JTextField();
        javax.swing.JButton filterBtn = new javax.swing.JButton();
        javax.swing.JSeparator separator = new javax.swing.JSeparator();
        filterLabel = new javax.swing.JLabel();
        javax.swing.JButton topBtn = new javax.swing.JButton();
        javax.swing.JButton clearBtn = new javax.swing.JButton();
        warningLbl = new javax.swing.JLabel();

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
        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18));
        titleLabel.setText("Dialog Title");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        westPanel.setLayout(new java.awt.GridBagLayout());

        autoUpdateBtn.setText("Update");
        autoUpdateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoUpdateBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        westPanel.add(autoUpdateBtn, gridBagConstraints);

        jLabel1.setText("Period (milli sec):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        westPanel.add(jLabel1, gridBagConstraints);

        updPeriodTxt.setColumns(8);
        updPeriodTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updPeriodTxtActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        westPanel.add(updPeriodTxt, gridBagConstraints);

        filterBtn.setText("Filter");
        filterBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        westPanel.add(filterBtn, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        westPanel.add(separator, gridBagConstraints);

        filterLabel.setText("none");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        westPanel.add(filterLabel, gridBagConstraints);

        topBtn.setText("Top");
        topBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 0);
        westPanel.add(topBtn, gridBagConstraints);

        clearBtn.setText("Clear");
        clearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        westPanel.add(clearBtn, gridBagConstraints);

        warningLbl.setForeground(new java.awt.Color(255, 51, 0));
        warningLbl.setText("!!!  Warning  !!!");
        warningLbl.setToolTipText("Lines could have been lost !!!  Check update period.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        westPanel.add(warningLbl, gridBagConstraints);

        getContentPane().add(westPanel, java.awt.BorderLayout.WEST);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        doClose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void autoUpdateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoUpdateBtnActionPerformed
        auto_update = (autoUpdateBtn.getSelectedObjects() != null);

        //	if Starte, check the period
        if (auto_update)
            checkUpdatePeriode();
    }//GEN-LAST:event_autoUpdateBtnActionPerformed

    //===============================================================
    //===============================================================
    private void manageWarningButton(boolean recover) {
        //	If no recover found display warning
        if (blackbox.size() > 0)
            warningLbl.setVisible(!recover);
        else
            warningLbl.setVisible(false);
    }

    //===============================================================
    //===============================================================
    private void checkUpdatePeriode() {
        String strval = updPeriodTxt.getText().trim();
        try {
            int tmp = Integer.parseInt(strval);
            if (tmp < 100)
                tmp = 100;
            updPeriodTxt.setText(Integer.toString(tmp));
            if (tmp != upd_period) {
                upd_period = tmp;
                thread.wakeUp();
            }
        } catch (NumberFormatException e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void updPeriodTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updPeriodTxtActionPerformed
        checkUpdatePeriode();
    }//GEN-LAST:event_updPeriodTxtActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void filterBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterBtnActionPerformed

        if (filter_dlg == null)
            filter_dlg = new FilterDialog(this);

        if (filter_dlg.showDialog() == JOptionPane.OK_OPTION) {
            Filter filter = filter_dlg.getFilter();
            blackbox.setFilter(filter);
            if (filter != null) {
                filterLabel.setText(filter.toString());
            } else
                filterLabel.setText("none");

            //	Update the top if visible
            if (top_dialog != null)
                if (top_dialog.isVisible())
                    top_dialog.displayTop();
        }

    }//GEN-LAST:event_filterBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void topBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topBtnActionPerformed
        if (top_dialog == null)
            top_dialog = new TopDialog(this, deviceName, blackbox);
        top_dialog.showDialog();
    }//GEN-LAST:event_topBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed
        clearBlackBox = true;
        thread.wakeUp();
    }//GEN-LAST:event_clearBtnActionPerformed

    //===============================================================
    /**
     * Closes the dialog
     */
    //===============================================================
    private void doClose() {
        thread.stopThread();
        if (parent==null) {
            System.exit(0);
        } else {
            auto_update = false;
            autoUpdateBtn.setSelected(false);
            setVisible(false);
            dispose();
        }
    }

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton autoUpdateBtn;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField updPeriodTxt;
    private javax.swing.JLabel warningLbl;
    // End of variables declaration//GEN-END:variables
    //===============================================================


    //===============================================================

    /**
     * @param args the command line arguments
     */
    //===============================================================
    public static void main(String args[]) {

        String deviceName = "sys/database/3";
        if (args.length > 0)
            deviceName = args[0];
        try {
            new BlackBoxTable((JFrame)null, deviceName).setVisible(true);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(new Frame(), "", e);
        }
    }
    //===============================================================
    //===============================================================


    //===============================================================
    /**
     * The blacbox class (to parse string array)
     */
    //===============================================================
    class BlackBox extends ArrayList<ArrayList<String>>    //	Data parsed.
    {
        private List<ArrayList<String>> filtered;        //	the data filtered
        private Filter filter = null;    //	The filter requirement
        private List<String> code;            //	Original lines
        private List<String> filtered_code;    //	filtered original lines
        private double delta_time = 0.0;//	time between first and last record.

        //===========================================================
        private BlackBox() {
            code = new ArrayList<>();
            filtered = new ArrayList<>();
            filtered_code = new ArrayList<>();

            //	Parse the string array from the device black box
            //addRecords(parseContent(array), array);
        }

        //===========================================================
        private void setFilter(Filter filter) {
            this.filter = filter;
            if (filter != null)
                filterContent();
            model.fireTableDataChanged();
        }

        //===========================================================
        private void filterContent() {
            filtered.clear();
            filtered_code.clear();
            for (int i = 0; i < size(); i++) {
                ArrayList<String> line = get(i);
                String topic = line.get(filter.col_idx);
                if (topic.contains(filter.str)) {
                    filtered.add(line);
                    filtered_code.add(code.get(i));
                }
            }
        }

        //===========================================================
        ArrayList<String> getLine(int idx) {
            if (filter == null)
                return get(idx);
            else
                return filtered.get(idx);
        }

        //===========================================================
        int nbRecords() {
            if (filter == null)
                return size();
            else
                return filtered.size();
        }

        //===========================================================
        String getDeltaTimeStr() {
            int dt = (int) delta_time; //	integer part
            StringBuilder sb = new StringBuilder("");
            int h = 0;
            int mn = 0;
            int sec;
            int millis = (int) (1000.0 * (delta_time - dt));

            if (dt > 3600) {
                h = dt / 3600;
                if (h < 10) sb.append("0");
                sb.append(Integer.toString(h)).append(":");
                dt -= 3600 * h;
            }

            if (dt >= 60) {
                mn = dt / 60;
                if (mn < 10) sb.append("0");
                sb.append(Integer.toString(mn)).append(":");
                dt -= 60 * mn;
            } else if (h > 0)
                sb.append("00:");

            if (dt > 0) {
                sec = dt;
                if (sec < 10) sb.append("0");
                sb.append(Integer.toString(sec)).append(".");
            } else
                sb.append("00.");

            sb.append(Integer.toString(millis));
            if (h == 0 && mn == 0)
                sb.append(" sec.");

            return sb.toString();
        }

        //===========================================================
        double getDeltaTime() {
            String[] dates = getDates();
            double start = parseTime(dates[0]);
            double stop = parseTime(dates[1]);

            while (stop < start)
                stop += 24 * 3600;
            return stop - start;
        }

        //===========================================================
        private double parseTime(String str) {
            if (str.length() == 0)
                return 0.0;

            //	Build time
            List<String> tokens = new ArrayList<>();
            StringTokenizer stk = new StringTokenizer(str, ":");
            while (stk.hasMoreTokens())
                tokens.add(stk.nextToken());
            if (tokens.size() < 4)
                return 0.0;

            double t = 0.0;
            try {
                t = 3600.0 * Integer.parseInt(tokens.get(0));
                t += 60.0 * Integer.parseInt(tokens.get(1));
                t += 1.0 * Integer.parseInt(tokens.get(2));
                t += 1.0 * Integer.parseInt(tokens.get(3)) / 100;
                //System.out.println(str + " -> " + t);
            } catch (NumberFormatException e) {
                System.out.println(e.toString());
            }
            return t;
        }

        //===========================================================
        String[] getDates() {
            if (size() == 0)
                return new String[]{"", ""};
            String start = get(size() - 1).get(DATES);
            String stop = get(0).get(DATES);    //	Last record is on top

            int pos;
            if ((pos = start.indexOf(' ')) > 0) start = start.substring(pos).trim();
            if ((pos = stop.indexOf(' ')) > 0) stop = stop.substring(pos).trim();

            return new String[]{start, stop};
        }

        //===========================================================
        private void update(String[] array) {
            List<ArrayList<String>> tmp = parseContent(array);
            boolean found = false;

            if (size() > 0) {
                //	Get own last record (first element)
                //	and search if some news in tmp object
                ArrayList<String> last_line = get(0);
                for (int i = tmp.size() - 1; i >= 0; i--) {
                    ArrayList<String> new_line = tmp.get(i);
                    if (found)    //	do it next time and after found
                    {
                        add(0, new_line);
                        code.add(0, array[i]);
                    } else
                    //	Check vector fields
                    {
                        boolean diff = false;
                        for (int j = 0; !diff &&
                                j < new_line.size() && j < last_line.size(); j++)
                            diff = !new_line.get(j).equals(last_line.get(j));

                        //	If no diff, it is the last record displayed
                        found = !diff;
                    }
                }
            } else
                found = true;    //	no warning.

            //	if not found or no record yet, copy all
            if (!found || size() == 0)
                addRecords(tmp, array);

            //	Show warning if no recover found
            manageWarningButton(found);

            //	Filter result if requested
            if (filter != null)
                filterContent();

            //	Update the top if visible
            if (top_dialog != null)
                if (top_dialog.isVisible())
                    top_dialog.displayTop();

            //	Update the delta time
            delta_time = getDeltaTime();
        }

        //===========================================================
        private void addRecords(List<ArrayList<String>> tmp, String[] array) {
            int idx = array.length - 1;
            if (tmp.size() > 0)
                for (int i = tmp.size() - 1; i >= 0; i--) {
                    add(0, tmp.get(i));
                    while (idx >= 0 &&
                            array[idx].indexOf("Operation blackbox") > 0)    //	Don't keep balckbox trace
                        idx--;
                    code.add(0, array[idx--]);

                }
        }

        //===========================================================
        private ArrayList<ArrayList<String>> parseContent(String[] array) {
            ArrayList<ArrayList<String>> info = new ArrayList<>();
            for (String line : array) {
                //	Get date
                ArrayList<String> vs = new ArrayList<>(columns.length);
                String date = getDate(line);
                int offset = date.length() + 3;

                //	Get operation type
                String op_type = getNextWord(line, offset);
                offset += op_type.length() + 1;

                if (op_type.equals("Attribute"))
                    op_type = "CORBA " + op_type;

                //	Get Operation call
                String operation;
                if (op_type.equals("Operation")) {
                    operation = getNextWord(line, offset);
                    offset += operation.length() + 1;
                } else
                    operation = op_type;
                //	Get operation parameter
                String op_param = "";
                if (!operation.startsWith("command_list_query") &&
                        !operation.startsWith("get_attribute_config") &&
                        !operation.startsWith("ping") &&
                        !operation.startsWith("adm_name")) {
                    //	Do it in loop in case of more than one attributes
                    String tmp;
                    do {
                        tmp = getNextWord(line, offset);
                        offset += tmp.length() + 1;
                        op_param += " " + tmp;
                    }
                    while (tmp.endsWith(","));
                    op_param = op_param.trim();

                    //	If it is a command, another syntax !!! :-)
                    if (op_param.equals("cmd"))
                        for (int i = 0; i < 2; i++)    //	one for "="
                        {
                            op_param = getNextWord(line, offset);
                            offset += op_param.length() + 1;
                        }
                }
                //	Get source
                String source = "device";
                if (line.indexOf("from cache_device") > 0)
                    source = "cache_device";
                else if (line.indexOf("from cache") > 0)
                    source = "cache ";

                //	Get host (or polling)
                String host = "";
                offset = line.indexOf("requested from ");
                if (offset > 0) {
                    if (line.indexOf("requested form polling") > 0)
                        host = "polling";
                    else {
                        host = getNextWord(line, offset + "requested from ".length());
                        //	take off FQDN
                        int pos = host.indexOf('.');
                        if (pos > 0)
                            host = host.substring(0, pos);
                    }
                }

                //	Get process if any
                String process = getProcess(line);

                //	Build table row.
                if (!operation.equals("blackbox")) {
                    vs.add(date);
                    vs.add(operation);
                    vs.add(op_param);
                    vs.add(source);
                    vs.add(host);
                    vs.add(process);
                    info.add(vs);
                }
            }
            return info;
        }

        //===========================================================
        private String getProcess(String line) {
            int offset;
            String process = "";
            String javaClient = "java client with main class";
            String cppClient = "cpp/python client with pid";
            if ((offset = line.toLowerCase().indexOf(javaClient)) > 0) {
                process = line.substring(offset + javaClient.length());
                if ((offset = process.indexOf(')')) > 0)
                    process = process.substring(0, offset);
            } else if ((offset = line.toLowerCase().indexOf(cppClient)) > 0)
                process = "PID=" + getNextWord(line, offset + cppClient.length());
            return process.trim();
        }

        //===========================================================
        private String getNextWord(String line, int offset) {
            line = line.substring(offset);
            StringTokenizer stk = new StringTokenizer(line);
            if (stk.hasMoreTokens()) {
                String word = stk.nextToken();
                //	take off parentehesis if any
                if (word.startsWith("("))
                    word = word.substring(1);
                int i = word.indexOf(')');
                if (i > 0)
                    word = word.substring(0, i);
                return word;

            } else
                return "";
        }

        //===========================================================
        private String getDate(String line) {
            int pos = 0;
            for (int i = 0; pos >= 0 && i < 4; i++)
                pos = line.indexOf(':', pos + 1);
            if (pos < 0)
                return "";
            return line.substring(0, pos).trim();
        }

        //===========================================================
        private String getCodeLine(int rownum) {
            if (filter == null)
                return code.get(rownum);
            else if (rownum < 0 || rownum > filtered_code.size() - 1)
                return "row = " + rownum + "???";
            else
                return filtered_code.get(rownum);
        }

        //===========================================================
        public String toString(int rownum, int wordnum) {
            if (rownum < 0 || wordnum < 0)
                return "";
            else if (filter == null)
                return get(rownum).get(wordnum);
            else
                return filtered.get(rownum).get(wordnum);
        }

        //===========================================================
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (ArrayList<String> line : this) {
                for (String word : line)
                    sb.append(word).append('\t');
                sb.append('\n');
            }
            return sb.toString();
        }
        //===========================================================
    }
    //===============================================================
    //	End of  BlackBox class
    //===============================================================


    //===============================================================

    /**
     * A little class to define a filter
     */
    //===============================================================
    private class Filter {
        private int col_idx;
        private String str;

        private Filter(int idx, String str) {
            col_idx = idx;
            this.str = str;
        }

        public String toString() {
            return columns[col_idx].name + ":  " + str;
        }
    }
    //===============================================================
    // End of Filter class
    //===============================================================


    //===============================================================

    /**
     * DataTableModel class.
     */
    //===============================================================
    public class DataTableModel extends AbstractTableModel {
        //==========================================================
        //==========================================================
        public int getColumnCount() {
            return columns.length;
        }

        //==========================================================
        //==========================================================
        public String getColumnName(int colnum) {
            if (colnum >= getColumnCount())
                return columns[getColumnCount() - 1].name;
            else
                return columns[colnum].name;
        }

        //==========================================================
        //==========================================================
        public int getRowCount() {
            if (blackbox == null)
                return 0;
            else
                return blackbox.nbRecords();
        }

        //==========================================================
        //==========================================================
        public Object getValueAt(int row, int col) {
            return blackbox.toString(row, col);
        }
        //==========================================================
        //==========================================================
    }
    //===============================================================
    //	End of DataTableModel class
    //===============================================================


    //===============================================================
    /**
     *	A Thread class to update black box content
     */
    //===============================================================
    private class UpdateThread extends Thread {
        private boolean stop = false;

        //==========================================================
        private void stopThread() {
            stop = true;
            wakeUp();
        }

        //==========================================================
        private synchronized void wakeUp() {
            notify();
        }

        //==========================================================
        private synchronized void waitNextLoop(long millis) {
            try {  wait(millis); } catch (InterruptedException e) { /* */ }
        }

        //==========================================================
        public void run() {
            DeviceProxy dev = null;
            boolean first = true;
            while (!stop) {
                //	Do it only the first time or if auto update is true
                if (first || auto_update) {
                    first = false;
                    try {
                        //	Read black box
                        if (dev == null)
                            dev = DeviceProxyFactory.get(deviceName);
                        String[] array = dev.black_box(100);
                        //	And update black box and table
                        blackbox.update(array);
                        model.fireTableDataChanged();
                        titleLabel.setText(deviceName + " - " + blackbox.getDeltaTimeStr());
                    } catch (DevFailed e) {
                        if (!e.errors[0].reason.equals("API_BlackBoxEmpty"))
                            ErrorPane.showErrorMessage(table_dialog, "", e);
                    }
                }
                waitNextLoop(upd_period);
                if (isVisible()) {
                    if (clearBlackBox) {
                        blackbox.clear();
                        clearBlackBox = false;
                        blackbox.update(new String[0]);
                        model.fireTableDataChanged();
                        titleLabel.setText("Black box on  " + deviceName);
                    }
                }
            }
            //System.out.println("Thread has been stopped !");
        }
        //==========================================================
    }
    //===============================================================
    //	End of UpdateThread class
    //===============================================================


    //===============================================================
    /*
      *	A Table Popup menu
      */
    //===============================================================
    class TablePopupMenu extends JPopupMenu {
        private JTable table;
        private String hostname = null;
        private final String[] menuLabels = {
                "Host Panel",
                "Remote Login",
        };
        private final int OFFSET = 2;        //	Label And separator
        private final int HOST_PANEL = 0;
        private final int REMOTE_LOG = 1;

        //===========================================================
        private TablePopupMenu(JTable table) {
            this.table = table;
            //	Build menu
            JLabel title = new JLabel("Datbase Server :");
            title.setFont(new java.awt.Font("Dialog", Font.BOLD, 16));
            add(title);
            add(new JPopupMenu.Separator());
            for (String menuLabel : menuLabels) {
                JMenuItem btn = new JMenuItem(menuLabel);
                btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        popupActionPerformed(evt);
                    }
                });
                add(btn);
            }
            //	Do not do it if Windows
            getComponent(OFFSET + REMOTE_LOG).setEnabled(AstorUtil.osIsUnix());
        }

        //===========================================================
        public void showMenu(java.awt.event.MouseEvent evt, String hostname) {
            this.hostname = hostname;
            //	Add host name in menu label title
            JLabel lbl = (JLabel) getComponent(0);
            lbl.setText(hostname + "  :");
            show(table, evt.getX(), evt.getY());
        }

        //===========================================================
        private void popupActionPerformed(ActionEvent evt) {
            Object src = evt.getSource();
            int cmdidx = 0;
            for (int i = 0; i < menuLabels.length; i++)
                if (getComponent(OFFSET + i) == src)
                    cmdidx = i;

            switch (cmdidx) {
                case HOST_PANEL:
                    if (parent instanceof Astor)
                        ((Astor) parent).tree.displayHostInfoDialog(hostname);
                    else if (parent instanceof HostInfoDialog)
                        ((HostInfoDialog) parent).displayHostInfoDialog(hostname);
                    break;

                case REMOTE_LOG:
                    new RemoteLoginThread(hostname, parent).start();
                    break;
            }
        }
        //===========================================================
    }
    //===============================================================
    //	End of TablePopupMenu class
    //===============================================================


    //===============================================================

    /**
     * JDialog Class to get filter
     */
    //===============================================================
    public class FilterDialog extends JDialog {
        private List<JRadioButton> buttons = new ArrayList<>();
        private JPanel txtPanel;
        private JTextField strFilterTxt;
        private JLabel titleLabel;
        private int retVal = JOptionPane.OK_OPTION;
        //===============================================================
        /*
         * Creates new form FilterDialog
         */
        //===============================================================
        public FilterDialog(JDialog parent) {
            super(parent, true);
            initComponents();
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new GridBagLayout());
            getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

            //	Build topic button
            GridBagConstraints gbc;
            int line = 0;
            for (int i = 1; i < columns.length; i++) {
                JRadioButton btn = new JRadioButton(columns[i].name);
                gbc = new GridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = line++;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                centerPanel.add(btn, gbc);
                btn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnActionPerformed(evt);
                    }
                });
                buttons.add(btn);
            }

            //	Add a text field to get filter.
            txtPanel = new JPanel();
            txtPanel.add(new JLabel("Filter: "));
            strFilterTxt = new JTextField();
            strFilterTxt.setColumns(15);
            txtPanel.add(strFilterTxt);
            strFilterTxt.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    strFilterTxtActionPerformed(evt);
                }
            });

            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = line;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            centerPanel.add(txtPanel, gbc);
            txtPanel.setVisible(false);

            titleLabel.setText("Filtering  Black  Box Content ");
            pack();
            ATKGraphicsUtils.centerDialog(this);
        }

        //===============================================================
        //===============================================================
        private void initComponents() {
            JPanel jPanel1 = new JPanel();
            JButton cancelBtn = new JButton();
            JButton okBtn = new JButton();
            JPanel jPanel2 = new JPanel();
            titleLabel = new JLabel();

            addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    closeDialog(evt);
                }
            });

            okBtn.setText("OK");
            okBtn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    okBtnActionPerformed(evt);
                }
            });
            jPanel1.add(okBtn);

            cancelBtn.setText("Dismiss");
            cancelBtn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cancelBtnActionPerformed(evt);
                }
            });
            jPanel1.add(cancelBtn);
            getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

            titleLabel.setFont(new java.awt.Font("Dialog", Font.BOLD, 18));
            titleLabel.setText("Dialog Title");
            jPanel2.add(titleLabel);
            getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);
        }

        //===============================================================
        //===============================================================
        private void strFilterTxtActionPerformed(java.awt.event.ActionEvent evt) {
            okBtnActionPerformed(evt);
        }

        //===============================================================
        //===============================================================
        Filter getFilter() {
            int idx = -1;
            for (int i = 0; i < buttons.size(); i++)
                if (buttons.get(i).getSelectedObjects() != null)
                    idx = i;
            if (idx == -1)
                return null;
            String str = strFilterTxt.getText().trim();
            if (str.length() > 0)
                return new Filter(idx + 1, str); //	+1 because date is not used.
            else
                return null;
        }

        //===============================================================
        //===============================================================
        private void btnActionPerformed(java.awt.event.ActionEvent evt) {
            JRadioButton btn = (JRadioButton) evt.getSource();
            boolean isSet = (btn.getSelectedObjects() != null);
            //	If is set, reset all others
            if (isSet)
                for (JRadioButton b : buttons)
                    if (b != btn)
                        b.setSelected(false);

            //	Check if at least one is selected
            isSet = false;
            for (JRadioButton b : buttons)
                if (b.getSelectedObjects() != null)
                    isSet = true;
            txtPanel.setVisible(isSet);
            if (isSet) {
                strFilterTxt.requestFocus();
                strFilterTxt.selectAll();
            }
            pack();
        }

        //===============================================================
        //===============================================================
        @SuppressWarnings({"UnusedParameters"})
        private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {
            retVal = JOptionPane.OK_OPTION;
            doClose();
        }

        //===============================================================
        //===============================================================
        @SuppressWarnings({"UnusedParameters"})
        private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {
            retVal = JOptionPane.CANCEL_OPTION;
            doClose();
        }

        //===============================================================
        //===============================================================
        @SuppressWarnings({"UnusedParameters"})
        private void closeDialog(java.awt.event.WindowEvent evt) {
            retVal = JOptionPane.CANCEL_OPTION;
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
        }

        //===============================================================
        //===============================================================
        public int showDialog() {
            strFilterTxt.requestFocus();
            strFilterTxt.selectAll();
            setVisible(true);
            return retVal;
        }
    }
}
