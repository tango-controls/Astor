
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
// Revision 1.4  2008/11/19 10:04:38  pascal_verdier
// Minor changes
//
// Revision 1.3  2008/03/27 08:08:26  pascal_verdier
// Compatibility with Starter 4.0 and after only !
// Better management of server list.
// Server state MOVING managed.
// Hard kill added on servers.
// New features on polling profiler.
//
// Revision 1.2  2006/06/13 13:58:17  pascal_verdier
// Minor changes.
//
// Revision 1.1.1.1  2006/01/11 08:34:49  pascal_verdier
// Imported using TkCVS
//
//
// Copyleft 2006 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor.tools;

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
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
 *	Class Description: Dialog to display events history.
 *
 *	@author  root
 */
//===============================================================


public class PollingProfiler extends JDialog implements IJLChartListener, ComponentListener
{
    /**
     *  The chart to display data.
     */
    private JLChart			chart  = new JLChart();
    private JLAxis      	y_axis;
    private JLAxis      	x_axis;
    private JLDataView[]    data = null;
    private long            now;
    /**
     *
     */
    private String[]        devnames;
    private DevPollStatus   poll_status;
    private Component       parent = null;
    private Timer           timer = null;
    private int             timer_period = 2;
    private boolean         update_chart = true;
	private JLabel			late_label;
	private JLabel			early_label;
	private PollingInfo		poll_info;

	private String	title;
	private int 	display_mode = DURATION;
	private JCheckBoxMenuItem[]	check_box;
	private static final int	HISTORY      = 0;
	private static final int	POLL_DRIFT   = 1;
	private static final int	DURATION     = 2;
	private static final int	NB_CHECK_BOX = 3;
    //===============================================================
    /**
     *	Creates new form PollingProfiler
     *
     * @param parent    The parent dislog
     * @param devname   the device name to display polling
     */
    //===============================================================
    public PollingProfiler(JDialog parent, String  devname)
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
    public PollingProfiler(JFrame parent, String  devname)
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
    public PollingProfiler(JDialog parent, String[]  devnames)
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
    public PollingProfiler(JFrame parent, String[]  devnames)
    {
        super(parent, false);
        this.parent = parent;
        realConstructor(devnames);
    }
    //===============================================================
    /**
     *	The real constructor
     *
     * @param devnames   the device names to display polling
     */
    //===============================================================
    private void realConstructor(String[] devnames)
    {
        this.devnames = devnames;
        initComponents();
        initOwnComponents();
		buildPopupMenu();
		customizeAxis();
        updateData();

        titleLabel.setVisible(false);
        pack();

		ATKGraphicsUtils.centerDialog(this);
    }
    //===============================================================
    //===============================================================
	private void buildPopupMenu()
	{
        chart.addMenuItem(new JMenuItem("-------------------------"));

		check_box = new JCheckBoxMenuItem[NB_CHECK_BOX];
		check_box[HISTORY] = new JCheckBoxMenuItem("History");
		check_box[HISTORY].addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                menuActionPerformed(evt);
            }
        });
        chart.addMenuItem(check_box[HISTORY]);

		check_box[POLL_DRIFT] = new JCheckBoxMenuItem("Polling Drift");
		check_box[POLL_DRIFT].addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                menuActionPerformed(evt);
            }
        });
        chart.addMenuItem(check_box[POLL_DRIFT]);

		check_box[DURATION] = new JCheckBoxMenuItem("Duration");
		check_box[DURATION].addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                menuActionPerformed(evt);
            }
        });
        chart.addMenuItem(check_box[DURATION]);

		JMenuItem jmi = new JMenuItem("Polling Info");
		jmi.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                menuActionPerformed(evt);
            }
        });
        chart.addMenuItem(jmi);

		check_box[display_mode].setSelected(true);
	}
    //===============================================================
    /**
     * Added JMenuItem listeners management
     */
    //===============================================================
    private void menuActionPerformed(ActionEvent evt)
    {
		Object	obj = evt.getSource();
        int     cmdidx = 0;

		if (obj instanceof JCheckBoxMenuItem)
		{
			//	Check selected mode
        	for (int i=0 ; i<check_box.length ; i++)
            	if (check_box[i]==obj)
				{
                	cmdidx = i;
					check_box[i].setSelected(true);
				}
				else
					check_box[i].setSelected(false);

			//	And display it
			display_mode = cmdidx;
			customizeAxis();
			updateCurves();
 			manageLabels();
		}
		else
			poll_info.setVisible(true);

   }
    //===============================================================
    //===============================================================
	private void customizeAxis()
	{
		x_axis.setPosition(JLAxis.HORIZONTAL_ORG2);
		if (display_mode==HISTORY)
		{
			//	Cannot use BAR chart, to be able to display duration
			chart.setHeader(title + " (History)");
        	x_axis.setAutoScale(false);
        	x_axis.setName("Time");
	 		x_axis.setAnnotation(JLAxis.TIME_ANNO);
	 		x_axis.setLabelFormat(JLAxis.AUTO_FORMAT);
        	y_axis.setAutoScale(false);
        	y_axis.setName("T0");
			late_label.setVisible(false);				
			early_label.setVisible(false);				
		}
		else
		{
        	x_axis.setAutoScale(false);
        	x_axis.setName("Attributes");
	 		x_axis.setAnnotation(JLAxis.VALUE_ANNO);
	 		x_axis.setLabelFormat(JLAxis.DECINT_FORMAT);
			if (display_mode==POLL_DRIFT)
			{
				chart.setHeader(title + " (Drift)");
        		y_axis.setName("Polling Drift (ms)");
				late_label.setVisible(true);				
				early_label.setVisible(true);				
			}
			else
			if (display_mode==DURATION)
			{
				chart.setHeader(title + " (Duration)");
        		y_axis.setName("Duration (ms)");
				late_label.setVisible(false);				
				early_label.setVisible(false);				
        		y_axis.setAutoScale(true);
			}
		}
	}
    //===============================================================
    //===============================================================
	private void manageLabels()
	{
		switch (display_mode)
		{
		case HISTORY:
			late_label.setVisible(false);
			early_label.setVisible(false);
			break;
		case POLL_DRIFT:
			int w = early_label.getPreferredSize().width;
			int h = early_label.getPreferredSize().height;
			int	h_chart = chart.getHeight();
    		early_label.setBounds(24, h_chart-20, w, h);
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
        title = "Polling on:  ";
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
				ErrorPane.showErrorMessage(this, null, e);
            }
        }
        chart.setHeader(title);


		//	Add two labels to show direction for late and aerly (drift)
    	late_label = new JLabel("Polling Late");
    	late_label.setFont(new Font("Dialog", Font.BOLD, 14));
    	late_label.setIcon(new ImageIcon(getClass().getResource(
			admin.astor.AstorDefs.img_path + "up.gif")));
    	late_label.setVisible(false);
    	chart.add(late_label);
		int	w = late_label.getPreferredSize().width;
		int	h = late_label.getPreferredSize().height;
    	late_label.setBounds(24, 10, w, h);

    	early_label = new JLabel("Polling Early");
    	early_label.setFont(new Font("Dialog", Font.BOLD, 14));
    	early_label.setIcon(new ImageIcon(getClass().getResource(
			admin.astor.AstorDefs.img_path + "down.gif")));
    	early_label.setVisible(false);
    	chart.add(early_label);


		poll_info = new PollingInfo(this);
		chart.addComponentListener(this);
    }
    //===============================================================
    //===============================================================
    private void myMouseReleased(MouseEvent evt)
    {
        if ((evt.getModifiers() & java.awt.event.MouseEvent.BUTTON1_MASK)!=0)
        {
            update_chart = true;
            doRepaint();
        }
    }
    //===============================================================
    //===============================================================
    private void updateData()
    {
        now = System.currentTimeMillis();
        try
        {
            poll_status = new DevPollStatus(devnames);
			updateCurves();
        }
        catch(DevFailed e)
        {
			ErrorPane.showErrorMessage(this, null, e);
        }
	}
    //===============================================================
    //===============================================================
	private void manageDataViews(int nb)
	{
        //  Check if nb curves has changed
        if (data!=null  && nb!=data.length)
        {
			for (JLDataView datum : data)
			{
				datum.reset();
				y_axis.removeDataView(datum);
			}
            data = null;
            initOwnComponents();
        }

        //	Allocate JDataView object
        if (data!=null)
        {
			for (JLDataView datum : data)
				datum.reset();
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
                data[i].setLabelVisible(true);
                y_axis.addDataView(data[i]);
            }
        }
		if (display_mode==HISTORY)
		{
			for (int i=0 ; i<nb ; i++)
			{
				data[i].setViewType(JLDataView.TYPE_LINE);
				//data[i].setBarWidth(1);
			}
		}
		else
		{
			for (int i=0 ; i<nb ; i++)
			{
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
    private void updateCurves()
	{
        //  Get nb curves
        int nb = poll_status.size();
        if (nb==0)
             return;

		manageDataViews(nb);

        //  Update curves
        double  xmin = now;
		double	x;
		double	y;
		
		double	y_min = 0;
		double	y_max = 0;
        for (int i=0 ; i<nb ; i++)
        {
            PolledElement   pe = poll_status.polledElementAt(i);
			switch (display_mode)
			{
			case HISTORY:
           		x = now - pe.last_update;
            	y = 1+0.1*i;
            	data[i].add(x, 0);
            	data[i].add(x, y);
            	data[i].add(x-pe.reading_time, y);
            	data[i].add(x-pe.reading_time, 0);

            	if (x<xmin) xmin = x;
				for (int real_period : pe.real_periods)
				{
					x -= real_period;
					data[i].add(x, 0);
					data[i].add(x, y);
					data[i].add(x - pe.reading_time, y);
					data[i].add(x - pe.reading_time, 0);
					if (x < xmin) xmin = x;
				}
				break;
			case POLL_DRIFT:
				if (pe.polled)
					if (pe.real_periods.length==0) {
						System.out.println("pe.real_periods.length=0");
						y = 0.0;
					}
					else {
						y = pe.real_periods[0] - pe.period;
					}
				else
					y = 0.0;	//	External triggered
	            data[i].add((double)i, y);
				if (y>y_max)	y_max = y;
				if (y<y_min)	y_min = y;
				
				break;
			case DURATION:
				if (pe.polled)
					y = pe.reading_time;
				else
					y = 0.0;	//	External triggered
	            data[i].add((double)i, y);
				break;
			}
        }
		switch (display_mode)
		{
		case HISTORY:
			xmin -= 1000.0;
			x_axis.setMinimum(xmin);
			x_axis.setMaximum((double)now);
			y_axis.setMinimum(0);
			y_axis.setMaximum(0.1*nb + 1.5);
			break;
		case POLL_DRIFT:
			x_axis.setMinimum(-.05);
			x_axis.setMaximum((double)nb - 0.5);
			if (y_max<10.0 && y_min>-10.0)
			{
        		y_axis.setAutoScale(false);
				y_axis.setMinimum(-10);
				y_axis.setMaximum(10);
			}
			else
	        	y_axis.setAutoScale(true);
			
			break;
		case DURATION:
			x_axis.setMinimum(-.05);
			x_axis.setMaximum((double)nb - 0.5);
			break;
		}
		poll_info.display();
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

        if (pe==null)
            return new String[0];
        String[]    s1 = pe.getInfo();
        int         nblines = s1.length;
        String[]    retVal;

		if (display_mode==HISTORY)
		{
	     	retVal = new String[nblines + 2];
		    System.arraycopy(s1, 0, retVal, 0, nblines);
    	    retVal[nblines]   = "";
        	retVal[nblines+1] = "T0 - " + (int)(now - dl.x) +  " ms";
		}
		else
			retVal = s1;
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
    private void autoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoBtnActionPerformed
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
				ErrorPane.showErrorMessage(new JFrame(), null, e);
				ErrorPane.showErrorMessage(this, null, e);
                autoBtn.setSelected(false);
                return;
            }
            updateData();
            //	Start a timer to update display
            if (timer==null)
            {
                ActionListener taskPerformer = new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        updateData();
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
        updateData();
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
    //===============================================================
	public void componentMoved(ComponentEvent evt)
	{
		manageLabels();
	}
    //===============================================================
    //===============================================================
	public void componentShown(ComponentEvent evt)
	{
		manageLabels();
	}
    //===============================================================
    //===============================================================
	public void componentResized(ComponentEvent evt)
	{
		manageLabels();
	}
    //===============================================================
    //===============================================================
	public void componentHidden(ComponentEvent evt)
	{
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
	 *	Check nb fields (2 for server - 3 for device)
	 */
	//===============================================================
	static int getNbFields(String[] args)
	{
		if (args.length==0)
			return 0;
		
		StringTokenizer	stk = new StringTokenizer(args[0], "/");
		int	nb = 0;
		for (nb=0 ; stk.hasMoreTokens(); nb++)
			stk.nextToken();
		return nb;
	}
	//===============================================================
	static String[] getDeviceNames(String servname) throws DevFailed
	{
		DeviceData	argout = 
			new DeviceProxy("dserver/" + servname).command_inout("QueryDevice");
		String[]	devnames = argout.extractStringArray();
		for (int i=0 ; i<devnames.length ; i++)
		{
			//	Remove Class name
			devnames[i] = devnames[i].substring(devnames[i].indexOf("::")+2);
			System.out.println(devnames[i]);
		}
		return devnames;
	}
	//===============================================================




	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String[] args) {
	
        try
        {
			switch(getNbFields(args))
			{
			case 3:	//	Device name
    		    new PollingProfiler(new JDialog(), args[0]).setVisible(true);
				break;
 			case 2:	//	Server name
	        	String[]  devnames = getDeviceNames(args[0]);
    		    new PollingProfiler(new JDialog(), devnames).setVisible(true);
				break;
			default:
				Except.throw_exception("BAD_ARGUMENT",
						"Server or Device name  ?", "PollingProfiler.main()");
			}
        }
        catch(DevFailed e)
        {
            Except.print_exception(e);
        }
	}


	

	//===============================================================
	/**
	* A dialog class to display info
	*/
	//===============================================================
	public class PollingInfo extends JDialog 
	{
		private JTextArea	textArea;
		//==========================================================
		public PollingInfo(JDialog parent)
		{
			super(parent, false);
			
		 	JScrollPane	jsp = new JScrollPane ();
			textArea = new JTextArea();
			textArea.setFont(new Font("Dialog", 1, 14));
			textArea.setEditable(false);
			jsp.setViewportView (textArea);
			jsp.setPreferredSize(new Dimension(500, 250));

			getContentPane().add(jsp, BorderLayout.CENTER);
			pack();
			ATKGraphicsUtils.centerDialog(this);
		}
		//==========================================================
		private void setText(String text)
		{
			textArea.setText(text);
		}
		//==========================================================
		private String formatValue(double val, int nb_dec)
		{
			String	str = "" + val;
			int	idx = str.indexOf('.');
			if (idx>0)
				if (str.substring(idx+1).length()>nb_dec)
					str = str.substring(0, nb_dec+idx+1);
			return str;
		}
		//==========================================================
		private void display()
		{
			//	First time check how many are polled
			int	nb_polled    = poll_status.polledCount();
			int	nb_triggered = poll_status.triggeredCount();
		
			StringBuffer sb = new StringBuffer("  ");
			
			if (nb_triggered==0)
				sb.append(nb_polled + " polled attributes.\n\n");
			else
			if (nb_polled==0)
				sb.append(nb_triggered + " triggered attributes.\n\n");
			else
			{
				sb.append(nb_polled + " polled attributes  and  ");
				sb.append(nb_triggered + " triggered attributes.\n\n");
			}
			
			boolean	drift_available = false;
			int		late_drift = 0;
			int		early_drift = 0;
			String	late_drift_name = null;
			String	early_drift_name = null;
			
			double	sum_duration = 0;
			double	max_duration = 0;
			String	max_duration_name = null;
			
			int		last_update_max = 0;
			String	last_update_max_str = "";	//	formated time
			String	last_update_max_name = null;

        	for (int i=0 ; i<poll_status.size() ; i++)
        	{
            	PolledElement   pe = poll_status.polledElementAt(i);
				if (pe.real_periods.length>0) {
					if (pe.polled)
					{
						//	Check drifts

						int	drift = pe.real_periods[0] - pe.period;
						if (drift>0)
						{
							if (drift > late_drift)
							{
								late_drift = drift;
								late_drift_name = pe.name;
							}
						}
						else
						if (drift < early_drift)
						{
							early_drift = drift;
							early_drift_name = pe.name;
						}
						drift_available = true;
					}

					//	Check duration
					if (pe.reading_time > max_duration)
					{
						max_duration = pe.reading_time;
						max_duration_name = pe.name;
					}
					sum_duration += pe.reading_time;

					//	Check last update
					if (pe.last_update > last_update_max)
					{
						last_update_max = pe.last_update;
						last_update_max_str = pe.last_update_str;
						last_update_max_name = pe.name;
					}
				}
			}
			
			//	Build Drift result
			if (drift_available)
			{
				sb.append("Drift maxi :\n");
				sb.append("    - Late : " + late_drift + " ms ");
				if (late_drift_name!=null)
					sb.append("	on " + late_drift_name);
				sb.append("\n");
				sb.append("    - Early: " + early_drift + " ms ");
				if (early_drift_name!=null)
					sb.append("	on " + early_drift_name);
				sb.append("\n");
			}

			//	Build Duration
			sb.append("\n");
			sb.append("Duration :\n");
			sb.append("    Maxi : " + formatValue(max_duration, 2) + " ms");
			if (max_duration_name!=null)
				sb.append("	on " + max_duration_name);
			sb.append("\n");
			sb.append("    Total: " + formatValue(sum_duration,2) + " ms \n");

			//	Build last update
			if (last_update_max_name!=null)
			{
				sb.append("\n");
				sb.append("Last update max	on " + last_update_max_name);
				sb.append("\n    since " + last_update_max_str);
			}

			//	Display results
			textArea.setText(sb.toString());
		}
		//==========================================================
	}
}
