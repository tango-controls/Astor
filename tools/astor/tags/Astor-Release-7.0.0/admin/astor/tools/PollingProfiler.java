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

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.chart.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

//===============================================================
/**
 * Class Description: Dialog to display events history.
 *
 * @author root
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class PollingProfiler extends JDialog implements IJLChartListener, ComponentListener {
    /**
     * The chart to display data.
     */
    private JLChart chart = new JLChart();
    private JLAxis y_axis;
    private JLAxis x_axis;
    private JLDataView[] data = null;
    private long now;

    private String[] deviceNames;
    private DevPollStatus poll_status;
    private Component parent = null;
    private Timer timer = null;
    private int timer_period = 2;
    private boolean update_chart = true;
    private JLabel late_label;
    private JLabel early_label;
    private PollingInfo poll_info;

    private String title;
    private int display_mode = DURATION;
    private JCheckBoxMenuItem[] check_box;
    private static final int HISTORY = 0;
    private static final int POLL_DRIFT = 1;
    private static final int DURATION = 2;
    private static final int NB_CHECK_BOX = 3;
    private static final Dimension preferredDimension = new Dimension(1024, 500);
    //===============================================================
    /**
     * Creates new form PollingProfiler
     *
     * @param parent  The parent dialog
     * @param deviceName the device name to display polling
     */
    //===============================================================
    public PollingProfiler(JDialog parent, String deviceName) {
        super(parent, false);
        this.parent = parent;
        realConstructor(new String[]{deviceName});
    }
    //===============================================================
    /**
     * Creates new form PollingProfiler
     *
     * @param parent  The parent frame
     * @param deviceName the device name to display polling
     */
    //===============================================================
    public PollingProfiler(JFrame parent, String deviceName) {
        super(parent, false);
        this.parent = parent;
        realConstructor(new String[]{deviceName});
    }
    //===============================================================
    /**
     * Creates new form PollingProfiler
     *
     * @param parent   The parent dialog
     * @param deviceNames the device names to display polling
     */
    //===============================================================
    public PollingProfiler(JDialog parent, String[] deviceNames) {
        super(parent, false);
        this.parent = parent;
        realConstructor(deviceNames);
    }
    //===============================================================
    /**
     * Creates new form PollingProfiler
     *
     * @param parent   The parent frame
     * @param deviceNames the device names to display polling
     */
    //===============================================================
    public PollingProfiler(JFrame parent, String[] deviceNames) {
        super(parent, false);
        this.parent = parent;
        realConstructor(deviceNames);
    }
    //===============================================================
    /**
     * The real constructor
     *
     * @param deviceNames the device names to display polling
     */
    //===============================================================
    private void realConstructor(String[] deviceNames) {
        this.deviceNames = deviceNames;
        initComponents();
        initOwnComponents();
        buildPopupMenu();
        customizeAxis();
        updateData();
        checkTangoRelease();

        pack();
        ATKGraphicsUtils.centerDialog(this);
    }

    //===============================================================
    //===============================================================
    private void checkTangoRelease() {
        try {
            if (deviceNames.length==0)
                return;
            //  Check device proxy release.
            DeviceProxy proxy = new DeviceProxy(deviceNames[0]);
            DeviceProxy adminDevice = proxy.get_adm_dev();
            int tangoRelease = adminDevice.getTangoVersion();

            //  Check if property set the polling model
            DbDatum datum = adminDevice.get_property("polling_before_9");
            boolean beforeNine;
            beforeNine = !datum.is_empty() && datum.extractBoolean();
            //System.out.println("beforeNine=" + beforeNine + "   Tango release=" + tangoRelease);

            //  If Tango 9 or more and not old model --> add warning
            if (tangoRelease<910 || beforeNine) {
                warningBtn.setVisible(false);
            } else {
                //  Check if several attributes are polled at same time
                if (poll_status.isPollingSeveralAttributes()) {
                    warningBtn.setIcon(Utils.getInstance().getIcon("clock.gif"));
                    warningBtn.setText("");
                }
                else
                    warningBtn.setVisible(false);
            }
        }
        catch (DevFailed e) {
            System.err.println("Cannot check Tango release: " + e.errors[0].desc);
        }

    }
    //===============================================================
    //===============================================================
    private void buildPopupMenu() {
        chart.addMenuItem(new JMenuItem("-------------------------"));

        check_box = new JCheckBoxMenuItem[NB_CHECK_BOX];
        check_box[HISTORY] = new JCheckBoxMenuItem("History");
        check_box[HISTORY].addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuActionPerformed(evt);
            }
        });
        chart.addMenuItem(check_box[HISTORY]);

        check_box[POLL_DRIFT] = new JCheckBoxMenuItem("Polling Drift");
        check_box[POLL_DRIFT].addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuActionPerformed(evt);
            }
        });
        chart.addMenuItem(check_box[POLL_DRIFT]);

        check_box[DURATION] = new JCheckBoxMenuItem("Duration");
        check_box[DURATION].addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuActionPerformed(evt);
            }
        });
        chart.addMenuItem(check_box[DURATION]);

        JMenuItem jmi = new JMenuItem("Polling Info");
        jmi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuActionPerformed(evt);
            }
        });
        chart.addMenuItem(jmi);

        check_box[display_mode].setSelected(true);
    }
    //===============================================================
    /*
     * Added JMenuItem listeners management
     */
    //===============================================================
    private void menuActionPerformed(ActionEvent evt) {
        Object obj = evt.getSource();
        int cmdidx = 0;

        if (obj instanceof JCheckBoxMenuItem) {
            //	Check selected mode
            for (int i = 0; i < check_box.length; i++)
                if (check_box[i] == obj) {
                    cmdidx = i;
                    check_box[i].setSelected(true);
                } else
                    check_box[i].setSelected(false);

            //	And display it
            display_mode = cmdidx;
            customizeAxis();
            updateCurves();
            manageLabels();
        } else
            poll_info.setVisible(true);

    }

    //===============================================================
    //===============================================================
    private void customizeAxis() {
        x_axis.setPosition(JLAxis.HORIZONTAL_ORG2);
        if (display_mode == HISTORY) {
            titleLabel.setText(title + " (History)");
            x_axis.setAutoScale(false);
            x_axis.setName("Time");
            x_axis.setAnnotation(JLAxis.TIME_ANNO);
            x_axis.setLabelFormat(JLAxis.AUTO_FORMAT);
            y_axis.setAutoScale(false);
            y_axis.setName("T0");
            late_label.setVisible(false);
            early_label.setVisible(false);
        } else {
            x_axis.setAutoScale(false);
            x_axis.setName("Attributes");
            x_axis.setAnnotation(JLAxis.VALUE_ANNO);
            x_axis.setLabelFormat(JLAxis.DECINT_FORMAT);
            if (display_mode == POLL_DRIFT) {
                titleLabel.setText(title + " (Drift)");
                y_axis.setName("Polling Drift (ms)");
                late_label.setVisible(true);
                early_label.setVisible(true);
            } else if (display_mode == DURATION) {
                titleLabel.setText(title + " (Duration)");
                y_axis.setName("Duration (ms)");
                late_label.setVisible(false);
                early_label.setVisible(false);
                y_axis.setAutoScale(true);
            }
        }
    }

    //===============================================================
    //===============================================================
    private void manageLabels() {
        switch (display_mode) {
            case HISTORY:
                late_label.setVisible(false);
                early_label.setVisible(false);
                break;
            case POLL_DRIFT:
                int w = early_label.getPreferredSize().width;
                int h = early_label.getPreferredSize().height;
                int h_chart = chart.getHeight();
                early_label.setBounds(24, h_chart - 20, w, h);
                late_label.setVisible(true);
                early_label.setVisible(true);
                break;
            case DURATION:
                late_label.setVisible(false);
                early_label.setVisible(false);
                break;
        }
    }

    //===============================================================
    //===============================================================
    private void initOwnComponents() {

        y_axis = chart.getY2Axis();
        x_axis = chart.getXAxis();

        //	Add a JLChart to display Loads
        chart.setBackground(Color.white);
        chart.setChartBackground(Color.lightGray);
        chart.setLabelVisible(true);
        chart.setLabelFont(new Font("Dialog", Font.BOLD, 12));

        x_axis.setGridVisible(true);

        chart.setPreferredSize(preferredDimension);
        chart.setJLChartListener(this);
        chart.setLabelPlacement(JLChart.LABEL_RIGHT);
        getContentPane().add(chart, java.awt.BorderLayout.CENTER);

        chart.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                myMouseReleased(evt);
            }
        });

        //  Build title.
        title = "Polling on:  ";
        if (deviceNames.length == 1)
            title += deviceNames[0];
        else {
            try {
                String adm = new DeviceProxy(deviceNames[0]).adm_name();
                title += adm.substring(adm.indexOf('/') + 1);
            } catch (DevFailed e) {
                title += deviceNames[0];
                ErrorPane.showErrorMessage(this, null, e);
            }
        }
        titleLabel.setText(title);

        //	Add two labels to show direction for late and early (drift)
        late_label = new JLabel("Polling Late");
        late_label.setFont(new Font("Dialog", Font.BOLD, 14));
        late_label.setIcon(Utils.getInstance().getIcon("up.gif"));
        late_label.setVisible(false);
        chart.add(late_label);
        int w = late_label.getPreferredSize().width;
        int h = late_label.getPreferredSize().height;
        late_label.setBounds(24, 10, w, h);

        early_label = new JLabel("Polling Early");
        early_label.setFont(new Font("Dialog", Font.BOLD, 14));
        early_label.setIcon(Utils.getInstance().getIcon("down.gif"));
        early_label.setVisible(false);
        chart.add(early_label);


        poll_info = new PollingInfo(this);
        chart.addComponentListener(this);
    }

    //===============================================================
    //===============================================================
    private void myMouseReleased(MouseEvent evt) {
        if ((evt.getModifiers() & java.awt.event.MouseEvent.BUTTON1_MASK) != 0) {
            update_chart = true;
            doRepaint();
        }
    }

    //===============================================================
    //===============================================================
    private void displayTitle(int nbAttributes) throws DevFailed {

        int nbThreads;
        String adminDevice = new DeviceProxy(deviceNames[0]).adm_name();
        nbThreads = new PoolThreadsTree(adminDevice).getNbThreads();

        setTitle(Integer.toString(nbAttributes) + " elements polled by " + nbThreads + " threads");
        titleDisplayed = true;
}
    //===============================================================
    //===============================================================
    private boolean titleDisplayed = false;
    private void updateData() {
        now = System.currentTimeMillis();
        try {
            //ToDo
            poll_status = new DevPollStatus(deviceNames);
            if (!titleDisplayed)
                displayTitle(poll_status.size());
            updateCurves();
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }

    //===============================================================
    //===============================================================
    private void manageDataViews(int nb) {
        //  Check if nb curves has changed
        if (data != null && nb != data.length) {
            for (JLDataView datum : data) {
                datum.reset();
                y_axis.removeDataView(datum);
            }
            data = null;
            initOwnComponents();
        }

        //	Allocate JDataView object
        if (data != null) {
            for (JLDataView datum : data)
                datum.reset();
        } else {
            data = new JLDataView[nb];
            AstorUtil util = AstorUtil.getInstance();
            util.initColors(nb);

            for (int i = 0; i < nb; i++) {
                data[i] = new JLDataView();
                data[i].setColor(util.getNewColor());
                data[i].setName(poll_status.get(i).name);
                data[i].setLabelVisible(true);
                y_axis.addDataView(data[i]);
            }
        }
        if (display_mode == HISTORY) {
            for (int i = 0; i < nb; i++) {
                data[i].setViewType(JLDataView.TYPE_LINE);
                //data[i].setBarWidth(1);
            }
        } else {
            for (int i = 0; i < nb; i++) {
                data[i].setViewType(JLDataView.TYPE_BAR);
                data[i].setFillMethod(JLDataView.METHOD_FILL_FROM_ZERO);
                data[i].setFill(true);
                data[i].setFillStyle(JLDataView.FILL_STYLE_SOLID);
                data[i].setFillColor(data[i].getColor());
            }
        }
    }

    //===============================================================
    //===============================================================
    private void updateCurves() {
        //  Get nb curves
        int nb = poll_status.size();
        if (nb == 0)
            return;

        manageDataViews(nb);

        //  Update curves
        double xMin = now;
        double y;

        double[] yMinMax =  new double[] { 0, 0 };
        int i = 0;
        for (PolledElement polledElement : poll_status) {
            switch (display_mode) {
                case HISTORY:
                    xMin = updateHistory(polledElement, i, xMin);
                    break;
                case POLL_DRIFT:
                    yMinMax = updateDrift(polledElement, i, yMinMax);
                    break;
                case DURATION:
                    if (polledElement.polled)
                        y = polledElement.reading_time;
                    else
                        y = 0.0;    //	External triggered
                    data[i].add((double) i, y);
                    break;
            }
            i++;
        }
        manageMinMax(yMinMax);
        poll_info.display();
        doRepaint();
    }

    //===============================================================
    //===============================================================
    private double[] updateDrift(PolledElement polledElement, int i, double[] yMinMax) {
        double y;
        if (polledElement.polled)
            if (polledElement.realPeriods.length == 0) {
                System.out.println("pe.realPeriods.length=0");
                y = 0.0;
            } else {
                y = polledElement.realPeriods[0] - polledElement.period;
            }
        else
            y = 0.0;    //	External triggered
        data[i].add((double) i, y);
        if (y < yMinMax[0]) yMinMax[0] = y;
        if (y > yMinMax[1]) yMinMax[1] = y;

        return yMinMax;
    }

    //===============================================================
    //===============================================================
    private double updateHistory(PolledElement polledElement, int i, double xMin) {

        double x = now - polledElement.last_update;
        double y = 1 + 0.1 * i;
        data[i].add(x, 0);
        data[i].add(x, y);
        data[i].add(x - polledElement.reading_time, y);
        data[i].add(x - polledElement.reading_time, 0);

        if (x < xMin) xMin = x;
        for (int real_period : polledElement.realPeriods) {
            x -= real_period;
            data[i].add(x, 0);
            data[i].add(x, y);
            data[i].add(x - polledElement.reading_time, y);
            data[i].add(x - polledElement.reading_time, 0);
            if (x < xMin) xMin = x;
        }
        return xMin;
    }
    //===============================================================
    //===============================================================
    private void manageMinMax(double[] yMinMax) {
        int nb = poll_status.size();
        double xmin = now;
        switch (display_mode) {
            case HISTORY:
                xmin -= 1000.0;
                x_axis.setMinimum(xmin);
                x_axis.setMaximum((double) now);
                y_axis.setMinimum(0);
                y_axis.setMaximum(0.1 * nb + 1.5);
                break;
            case POLL_DRIFT:
                x_axis.setMinimum(-.05);
                x_axis.setMaximum((double) nb - 0.5);
                if (yMinMax[0] > -10.0 &&  yMinMax[1] < 10.0) {
                    y_axis.setAutoScale(false);
                    y_axis.setMinimum(-10);
                    y_axis.setMaximum(10);
                } else
                    y_axis.setAutoScale(true);

                break;
            case DURATION:
                x_axis.setMinimum(-.05);
                x_axis.setMaximum((double) nb - 0.5);
                break;
        }
    }
    //===============================================================
    //===============================================================
    private void doRepaint() {
        if (update_chart)
            chart.repaint();
    }
    //===============================================================
    /**
     * Received on a click in graph
     */
    //===============================================================
    public String[] clickOnChart(JLChartEvent event) {

        update_chart = false;
        JLDataView dv = event.getDataView();
        PolledElement pe = null;
        for (int i = 0; pe == null && i < poll_status.size(); i++)
            if (dv.getName().equals(poll_status.get(i).name))
                pe = poll_status.get(i);
        int idx = event.getDataViewIndex();
        DataList dl = dv.getData();
        //  get the clicked index.
        for (int i = 0; i < idx; i++)
            dl = dl.next;

        if (pe == null)
            return new String[0];
        String[] s1 = pe.getInfo();
        int nblines = s1.length;
        String[] retVal;
        if (display_mode == HISTORY) {
            retVal = new String[nblines + 2];
            System.arraycopy(s1, 0, retVal, 0, nblines);
            retVal[nblines] = "";
            retVal[nblines + 1] = "T0 - " + (int) (now - dl.x) + " ms";
        } else
            retVal = s1;
        return retVal;
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
        warningBtn = new javax.swing.JButton();
        javax.swing.JLabel separatorLabel = new javax.swing.JLabel();
        autoBtn = new javax.swing.JRadioButton();
        updateBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        warningBtn.setText("...");
        warningBtn.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        warningBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                warningBtnActionPerformed(evt);
            }
        });
        jPanel1.add(warningBtn);

        separatorLabel.setText("                 ");
        jPanel1.add(separatorLabel);

        autoBtn.setText("Auto Update");
        autoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoBtnActionPerformed(evt);
            }
        });
        jPanel1.add(autoBtn);

        updateBtn.setText("Update");
        updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBtnActionPerformed(evt);
            }
        });
        jPanel1.add(updateBtn);

        cancelBtn.setText("Dismiss");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        jPanel1.add(cancelBtn);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Dialog Title");
        jPanel2.add(titleLabel);

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void autoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoBtnActionPerformed
        if (autoBtn.getSelectedObjects() != null) {
            String strValue = "" + timer_period;
            //	Get polling period as string
            strValue = (String) JOptionPane.showInputDialog(this,
                    "Reading period (seconds)  ?",
                    "Reading period",
                    JOptionPane.INFORMATION_MESSAGE,
                    null, null, strValue);
            if (strValue == null) {
                autoBtn.setSelected(false);
                return;
            }
            //	Convert to int
            try {
                timer_period = Integer.parseInt(strValue);
            } catch (NumberFormatException e) {
                ErrorPane.showErrorMessage(new JFrame(), null, e);
                ErrorPane.showErrorMessage(this, null, e);
                autoBtn.setSelected(false);
                return;
            }
            updateData();
            //	Start a timer to update display
            if (timer == null) {
                ActionListener taskPerformer = new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        updateData();
                    }
                };
                timer = new Timer(1000 * timer_period, taskPerformer);
            } else
                timer.setDelay(1000 * timer_period);
            timer.start();
            updateBtn.setEnabled(false);
        } else if (timer != null) {
            timer.stop();
            updateBtn.setEnabled(true);
        }
    }//GEN-LAST:event_autoBtnActionPerformed
    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        updateData();
    }//GEN-LAST:event_updateBtnActionPerformed
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
    private void warningBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_warningBtnActionPerformed
        JOptionPane.showMessageDialog(this, "WARNING:\n"+
                "  Since Tango-9, the polling duration\n"+
                "  could be a sum of several attribute polling durations.\n\n"+
                "  To have previous behaviour, add an admin device property:\n"+
                "       polling_before_9:  true");
    }//GEN-LAST:event_warningBtnActionPerformed

    //===============================================================
    /**
     * Closes the dialog
     */
    //===============================================================
    private void doClose() {
        if (timer != null)
            timer.stop();
        if (parent != null && parent.isVisible()) {
            setVisible(false);
            dispose();
        } else
            System.exit(0);
    }

    //===============================================================
    //===============================================================
    public void componentMoved(ComponentEvent evt) {
        manageLabels();
    }

    //===============================================================
    //===============================================================
    public void componentShown(ComponentEvent evt) {
        manageLabels();
    }

    //===============================================================
    //===============================================================
    public void componentResized(ComponentEvent evt) {
        manageLabels();
    }

    //===============================================================
    //===============================================================
    public void componentHidden(ComponentEvent evt) {
    }

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton autoBtn;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JButton updateBtn;
    private javax.swing.JButton warningBtn;
    // End of variables declaration//GEN-END:variables
    //===============================================================


    //===============================================================
    /*
     * Check nb fields (2 for server - 3 for device)
     */
    //===============================================================
    static int getNbFields(String[] args) {
        if (args.length == 0)
            return 0;

        StringTokenizer stk = new StringTokenizer(args[0], "/");
        return stk.countTokens();
    }

    //===============================================================
    //===============================================================
    private static String[] getDeviceNames(String serverName) throws DevFailed {
        DeviceProxy adminDevice = new DeviceProxy("dserver/" + serverName);
        DeviceData argOut = adminDevice.command_inout("QueryDevice");
        String[] deviceNames = argOut.extractStringArray();
        for (int i = 0; i < deviceNames.length; i++) {
            //	Remove Class name
            deviceNames[i] = deviceNames[i].substring(deviceNames[i].indexOf("::") + 2);
            //System.out.println(deviceNames[i]);
        }
        return deviceNames;
    }
    //===============================================================
    //===============================================================


    //===============================================================
    /**
     * @param args the command line arguments
     */
    //===============================================================
    public static void main(String[] args) {

        try {
            switch (getNbFields(args)) {
                case 3:    //	Device name
                    new PollingProfiler(new JDialog(), args[0]).setVisible(true);
                    break;
                case 2:    //	Server name
                    String[] deviceNames = getDeviceNames(args[0]);
                    new PollingProfiler(new JDialog(), deviceNames).setVisible(true);
                    break;
                default:
                    Except.throw_exception("BAD_ARGUMENT",
                            "Server or Device name  ?", "PollingProfiler.main()");
            }
        } catch (DevFailed e) {
            Except.print_exception(e);
        }
    }


    //===============================================================
    /**
     * A dialog class to display info
     */
    //===============================================================
    public class PollingInfo extends JDialog {
        private JTextArea textArea;

        //==========================================================
        public PollingInfo(JDialog parent) {
            super(parent, false);

            JScrollPane jsp = new JScrollPane();
            textArea = new JTextArea();
            textArea.setFont(new Font("Dialog", Font.BOLD, 14));
            textArea.setEditable(false);
            jsp.setViewportView(textArea);
            jsp.setPreferredSize(new Dimension(500, 250));

            getContentPane().add(jsp, BorderLayout.CENTER);
            pack();
            ATKGraphicsUtils.centerDialog(this);
        }

        //==========================================================
        private String formatValue(double val, int nb_dec) {
            String str = "" + val;
            int idx = str.indexOf('.');
            if (idx > 0)
                if (str.substring(idx + 1).length() > nb_dec)
                    str = str.substring(0, nb_dec + idx + 1);
            return str;
        }

        //==========================================================
        private void display() {
            //	First time check how many are polled
            int nb_polled = poll_status.polledCount();
            int nb_triggered = poll_status.triggeredCount();

            StringBuilder sb = new StringBuilder("  ");
            if (nb_triggered == 0)
                sb.append(nb_polled).append(" polled attributes.\n\n");
            else if (nb_polled == 0)
                sb.append(nb_triggered).append(" triggered attributes.\n\n");
            else {
                sb.append(nb_polled).append(" polled attributes  and  ");
                sb.append(nb_triggered).append(" triggered attributes.\n\n");
            }
            sb.append(getDriftStatus());
            sb.append(getDurationStatus());
            sb.append(getLastUpdate());

            //	Display results
            textArea.setText(sb.toString());
        }
        //==========================================================
        private String getDurationStatus() {
            double sum = 0;
            double max = 0;
            String maxName = null;
            for (PolledElement polledElement : poll_status) {
                if (polledElement.realPeriods.length > 0) {
                    if (polledElement.polled) {
                        //	Check duration
                        if (polledElement.reading_time > max) {
                            max = polledElement.reading_time;
                            maxName = polledElement.name;
                        }
                        sum += polledElement.reading_time;
                    }
                }
            }
            //	Build Duration
            StringBuilder   sb = new StringBuilder();
            sb.append("\n");
            sb.append("Duration :\n");
            sb.append("    Maxi : ").append(formatValue(max, 2)).append(" ms");
            if (maxName != null)
                sb.append("	on ").append(maxName);
            sb.append("\n");
            sb.append("    Total: ").append(formatValue(sum, 2)).append(" ms \n");
            return sb.toString();
        }
        //==========================================================
        private String getDriftStatus() {
            boolean available = false;
            int late  = 0;
            int early = 0;
            String lateName = null;
            String earlyName = null;

            for (PolledElement polledElement : poll_status) {
                if (polledElement.realPeriods.length > 0) {
                    if (polledElement.polled) {
                        //	Check drifts
                        int drift = polledElement.realPeriods[0] - polledElement.period;
                        if (drift > 0) {
                            if (drift > late) {
                                late = drift;
                                lateName = polledElement.name;
                            }
                        } else if (drift < early) {
                            early = drift;
                            earlyName = polledElement.name;
                        }
                        available = true;
                    }
                }
            }

            //	Build Drift result
            StringBuilder   sb = new StringBuilder();
            if (available) {
                sb.append("Drift maxi :\n");
                sb.append("    - Late : ").append(late).append(" ms ");
                if (lateName != null)
                    sb.append("	on ").append(lateName);
                sb.append("\n");
                sb.append("    - Early: ").append(early).append(" ms ");
                if (earlyName != null)
                    sb.append("	on ").append(earlyName);
                sb.append("\n");
            }
            return sb.toString();
        }
        //==========================================================
        private String getLastUpdate() {

            int max = 0;
            String time = "";    //	formatted time
            String maxName = null;

            for (PolledElement polledElement : poll_status) {
                if (polledElement.realPeriods.length > 0) {
                    //	Check last update
                    if (polledElement.last_update > max) {
                        max = polledElement.last_update;
                        time = polledElement.last_update_str;
                        maxName = polledElement.name;
                    }
                }
            }

            //	Build last update
            StringBuilder   sb = new StringBuilder();
            if (maxName != null) {
                sb.append("\n");
                sb.append("Last update max	on ").append(maxName);
                sb.append("\n    since ").append(time);
            }
            return sb.toString();
        }
        //==========================================================
    }
}
