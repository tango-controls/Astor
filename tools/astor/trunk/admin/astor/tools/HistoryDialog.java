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
import fr.esrf.tangoatk.widget.util.chart.*;

import javax.swing.*;
import java.awt.*;
import java.util.Date;


//===============================================================

/**
 * Class Description: Dialog to display events history.
 *
 * @author root
 */
//===============================================================


public class HistoryDialog extends JDialog implements IJLChartListener {
    /**
     * The chart to display data.
     */
    private JLChart chart = new JLChart();

    private JLabel titleLabel;
    private SubscribedSignal signal;
    //===============================================================
    /**
     * Creates new form HistoryDialog
     *
     * @param parent   The parent dialog
     * @param signal  the attribute name
     */
    //===============================================================
    public HistoryDialog(JDialog parent, SubscribedSignal signal) {
        super(parent, false);
        this.signal = signal;
        initComponents();
        initOwnComponents();

        titleLabel.setText("Events History for " + signal.name);
        pack();
    }

    //===============================================================
    //===============================================================
    private void initOwnComponents() {

        //	Add a JLChart to display Loads
        chart.setBackground(Color.white);
        chart.setChartBackground(Color.lightGray);
        chart.setHeaderFont(new Font("Dialog", Font.BOLD, 18));
        chart.setHeader("History");
        chart.setLabelFont(new Font("Dialog", Font.BOLD, 12));

        chart.getY1Axis().setName("Read Value");
        chart.getY1Axis().setAutoScale(true);
        chart.getXAxis().setName("Time");
        chart.getXAxis().setGridVisible(true);
        chart.setPreferredSize(new Dimension(800, 600));
        chart.setJLChartListener(this);
        getContentPane().add(chart, java.awt.BorderLayout.CENTER);

        //  Get nb curves
        int nb = 0;
        for (int i = 0; nb == 0 && i < signal.histo.size(); i++) {
            SubscribedSignal.EventHisto histo = signal.histo.get(i);
            if (histo.values != null)
                nb = histo.values.length;
        }
        AstorUtil util = AstorUtil.getInstance();
        util.initColors(nb);
        //	Allocate JDataView object
        JLDataView[] dv = new JLDataView[nb];
        for (int i = 0; i < nb; i++) {
            dv[i] = new JLDataView();
            dv[i].setColor(util.getNewColor());
            dv[i].setName("Values[" + i + "]");
            dv[i].setLabelVisible(false);
            dv[i].setMarker(JLDataView.MARKER_CROSS);
            JLAxis axis = chart.getY1Axis();
            axis.addDataView(dv[i]);
        }

        double x;
        double y;
        for (int i = 0; i < signal.histo.size(); i++) {
            SubscribedSignal.EventHisto histo = signal.histo.get(i);
            if (histo.values != null) {
                double max = 0;
                for (int v = 0; v < histo.values.length; v++) {
                    x = histo.time;
                    y = histo.values[v];
                    if (y > max) max = y;
                    chart.addData(dv[v], x, y);
                }
            }
        }
    }
    //===============================================================

    /**
     * Received on a click in graph
     */
    //===============================================================
    public String[] clickOnChart(JLChartEvent event) {
        JLDataView dv = event.getDataView();
        int idx = event.getDataViewIndex();
        DataList data = dv.getData();
        //  get the clicked index.
        for (int i = 0; i < idx; i++)
            data = data.next;
        String[] retVal = new String[4];
        retVal[0] = new Date((long) data.x).toString();
        retVal[1] = "Value = " + SubscribedSignal.formatValue(data.y);
        SubscribedSignal.EventHisto histo = signal.histo.get(idx);
        retVal[2] = "Delta Value = " + histo.d_value;
        retVal[3] = "Delta time  = " + histo.d_time;

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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
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
    }
    // </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        doClose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    private void doClose() {
        setVisible(false);
        dispose();
    }

    //===============================================================
    /**
     * @param args the command line arguments
     */
    //===============================================================
    public static void main(String args[]) {
        //	new HistoryDialog(new javax.swing.JDialog()).show();
    }
}
