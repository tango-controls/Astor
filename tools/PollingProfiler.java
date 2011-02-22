
//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author$
//
// $Revision$
// $Log$
//
// Copyleft 2006 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor.tools;

import admin.astor.AstorUtil;
import fr.esrf.tangoatk.widget.util.chart.*;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoApi.DeviceProxy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



//===============================================================
/**
 *	Class Description: Dialog to display events history.
 *
 *	@author  root
 */
//===============================================================


public class PollingProfiler extends JDialog implements IJLChartListener
{
    /**
     *  The chart to display data.
     */
    private JLChart		chart  = new JLChart();
    private JLAxis      y_axis;
    private JLAxis      x_axis;
    /**
     *
     */
    private String[]        devnames;
    private DevPollStatus   poll_status;
    private Component       parent = null;
    private Timer           timer = null;
    private int             timer_period = 2;
    private boolean         update_chart = true;
    //===============================================================
    /**
     *	Creates new form PollingProfiler
     *
     * @param parent    The parent dislog
     * @param devname   the device name to display polling
     */
    //===============================================================
    public PollingProfiler(JDialog parent, String  devname) throws DevFailed
    {
        super(parent, false);
        this.parent = parent;
        realConstructor(new String[] { devname });
    }
    //===============================================================
    /**
     *	Creates new form PollingProfiler
     *
     * @param parent    The parent frame
     * @param devname   the device name to display polling
     */
    //===============================================================
    public PollingProfiler(Frame parent, String  devname) throws DevFailed
    {
        super(parent, false);
        this.parent = parent;
        realConstructor(new String[] { devname });
    }
    //===============================================================
    /**
     *	Creates new form PollingProfiler
     *
     * @param parent    The parent dislog
     * @param devnames  the device names to display polling
     */
    //===============================================================
    public PollingProfiler(JDialog parent, String[]  devnames) throws DevFailed
    {
        super(parent, false);
        this.parent = parent;
        realConstructor(devnames);
    }
    //===============================================================
    /**
     *	Creates new form PollingProfiler
     *
     * @param parent    The parent frame
     * @param devnames  the device names to display polling
     */
    //===============================================================
    public PollingProfiler(Frame parent, String[]  devnames) throws DevFailed
    {
        super(parent, false);
        this.parent = parent;
        realConstructor(devnames);
    }
    //===============================================================
    /**
     *	The real constructor
     *
     * @param devname   the device name to display polling
     */
    //===============================================================
    private void realConstructor(String[] devnames) throws DevFailed
    {
        this.devnames = devnames;
        initComponents();
        initOwnComponents();
        updateData();
        titleLabel.setVisible(false);
        pack();

        //	Set screen position
        if (parent!=null && parent.isVisible())
        {
            //  Set it at buttom
            Point	p = parent.getLocationOnScreen();
            p.x += 10;
            p.y += 10;
            setLocation(p);
        }
    }
    //===============================================================
    //===============================================================
    private void initOwnComponents()
    {
        y_axis = chart.getY2Axis();
        x_axis = chart.getXAxis();

        //	Add a JLChart to display Loads
        chart.setBackground(Color.white);
        chart.setChartBackground(Color.lightGray);
        chart.setHeaderFont(new Font("Dialog", Font.BOLD, 18));
        chart.setLabelVisible(true);
        chart.setLabelFont(new Font("Dialog", Font.BOLD, 12));

        y_axis.setName("T0");
        y_axis.setAutoScale(false);
        x_axis.setAutoScale(false);
        x_axis.setName("Time");
        x_axis.setGridVisible(true);

        chart.setPreferredSize(new Dimension(850, 400));
        chart.setJLChartListener(this);
        chart.setLabelPlacement(JLChart.LABEL_RIGHT);
        getContentPane().add(chart, java.awt.BorderLayout.CENTER);

        chart.addMouseListener (new java.awt.event.MouseAdapter () {
            public void mouseReleased (java.awt.event.MouseEvent evt) {
                myMouseReleased(evt);
            }
        });

        //  Build title.
        String  title = "Polling on:  ";
        if (devnames.length==1)
            title += devnames[0];
        else
        {
            try
            {
                String  adm = new DeviceProxy(devnames[0]).adm_name();
                title += adm.substring(adm.indexOf('/')+1);
            }
            catch(DevFailed e)
            {
                title += devnames[0];
                app_util.PopupError.show(this, e);
            }
        }
        chart.setHeader(title);
    }
    //===============================================================
    //===============================================================
    private void myMouseReleased(java.awt.event.MouseEvent evt)
    {
        if ((evt.getModifiers() & evt.BUTTON1_MASK)!=0)
        {
            update_chart = true;
            doRepaint();
        }
    }
    //===============================================================
    //===============================================================
    private JLDataView[]    data = null;
    private long            now;
    private void updateData() throws DevFailed
    {
        now = System.currentTimeMillis();
        try
        {
            poll_status = new DevPollStatus(devnames);
        }
        catch(DevFailed e)
        {
            app_util.PopupError.show(this, e);
            return;
        }

        //  Get nb curves
        int nb = poll_status.size();
        if (nb==0)
        {
            Except.throw_exception("NoAttributePolled",
                                    "There is not any attribute polled !",
                                    "PollingProfiler.updateData()");
            return;
        }

        //  Check if nb curves has changed
        if (data!=null  && nb!=data.length)
        {
            for (int i=0 ; i<data.length ; i++)
            {
                data[i].reset();
                y_axis.removeDataView(data[i]);
            }
            data = null;
            initOwnComponents();
        }

        //	Allocate JDataView object
        if (data!=null)
        {
            for (int i=0 ; i<data.length ; i++)
                data[i].reset();
        }
        else
        {
            data = new JLDataView[nb];
            AstorUtil   util = AstorUtil.getInstance();
            util.initColors(nb);

            for (int i=0 ; i<nb ; i++)
            {
                data[i] = new JLDataView();
                data[i].setColor(util.getNewColor());
                data[i].setName(poll_status.polledElementAt(i).name);
                data[i].setFill(false);
                data[i].setLabelVisible(true);
                y_axis.addDataView(data[i]);
            }
        }

        //  Update curves
        double  xmin = now;
        for (int i=0 ; i<nb ; i++)
        {
            PolledElement   pe = poll_status.polledElementAt(i);
           	long t0 = now - pe.last_update;
            double  x = t0;
            double  y = 1+0.1*i;
            data[i].add(x, 0);
            data[i].add(x, y);
            data[i].add(x-pe.reading_time, y);
            data[i].add(x-pe.reading_time, 0);
            /*
            if (pe.reading_time<1)
                data[i].setBarWidth(1);
            else
                data[i].setBarWidth((int)pe.reading_time);
            */
            if (x<xmin) xmin = x;
            for (int t=0 ; t<pe.real_periods.length ; t++)
            {
                x -= pe.real_periods[t];
                data[i].add(x, 0);
                data[i].add(x, y);
                data[i].add(x-pe.reading_time, y);
                data[i].add(x-pe.reading_time, 0);
                if (x<xmin) xmin = x;
            }
            //System.out.println(pe.info());
        }
        xmin -= 1000.0;
        x_axis.setMinimum(xmin);
        x_axis.setMaximum((double)now);
        y_axis.setMinimum(0);
        y_axis.setMaximum(0.1*nb + 1.5);
        doRepaint();
   }
    //===============================================================
    //===============================================================
    private void doRepaint()
    {
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
        JLDataView	dv = event.getDataView();
        PolledElement   pe = null;
        for (int i=0 ; pe==null && i<poll_status.size() ; i++)
            if (dv.getName().equals(poll_status.polledElementAt(i).name))
                pe = poll_status.polledElementAt(i);
        int         idx = event.getDataViewIndex();
        DataList    dl = dv.getData();
        //  get the clicked index.
        for (int i=0 ; i<idx ; i++)
            dl = dl.next;

        String[]    s1 = pe.getInfo();
        int         nblines = s1.length;
        String[]    retVal = new String[nblines + 2];

        for (int i=0 ; i<nblines ; i++)
            retVal[i] = s1[i];
        retVal[nblines]   = "";
        retVal[nblines+1] = "T0 - " + (int)(now - dl.x) +  " ms";
		return retVal;
    }
 	//===============================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//===============================================================
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        autoBtn = new javax.swing.JRadioButton();
        updateBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

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

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18));
        titleLabel.setText("Dialog Title");
        jPanel2.add(titleLabel);

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    private void autoBtnActionPerformed(java.awt.event.ActionEvent
            evt) {//GEN-FIRST:event_autoBtnActionPerformed
        if (autoBtn.getSelectedObjects()!=null)
        {
            String  strval = "" + timer_period;
            //	Get polling period as string
            strval =(String) JOptionPane.showInputDialog(this,
                                        "Reading period (seconds)  ?",
                                        "Reading period",
                                        JOptionPane.INFORMATION_MESSAGE,
                                        null, null, strval);
            if (strval==null)
            {
                autoBtn.setSelected(false);
                return;
            }
            //	Convert to int
            try {
                timer_period = Integer.parseInt(strval);
            }
            catch(NumberFormatException e)
            {
                app_util.PopupError.show(this, e.toString());
                autoBtn.setSelected(false);
                return;
            }
            try { updateData(); } catch (DevFailed e){}
            //	Start a timer to update display
            if (timer==null)
            {
                ActionListener taskPerformer = new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        try { updateData(); } catch (DevFailed e){}
                   }
                };
                timer = new Timer(1000*timer_period, taskPerformer);
            }
            else
                timer.setDelay(1000*timer_period);
            timer.start();
            updateBtn.setEnabled(false);
        }
        else
        if (timer!=null)
        {
            timer.stop();
            updateBtn.setEnabled(true);
        }
    }//GEN-LAST:event_autoBtnActionPerformed

    //===============================================================
    //===============================================================
    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        try
        {
            updateData();
        }
        catch(DevFailed e)
        {
            app_util.PopupError.show(this, e);
        }
    }//GEN-LAST:event_updateBtnActionPerformed

	//===============================================================
	//===============================================================
	private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed

	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		doClose();
	}//GEN-LAST:event_closeDialog

	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void doClose()
	{
        if (timer!=null)
            timer.stop();
        if (parent!=null && parent.isVisible())
        {
            setVisible(false);
            dispose();
        }
        else
            System.exit(0);
    }
	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton autoBtn;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
	//===============================================================




	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String args[]) {
        String  devname = "pv/ps/1";
        if (args.length>0)
            devname = args[0];
        try
        {
    	    new PollingProfiler(new javax.swing.JDialog(), devname).show();
        }
        catch(DevFailed e)
        {
            Except.print_exception(e);
        }
	}


}
