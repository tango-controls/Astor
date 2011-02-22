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
//
// $Log$
// Revision 3.32  2008/04/07 10:53:35  pascal_verdier
// Branch info modified.
//
// Revision 3.31  2008/03/03 14:55:21  pascal_verdier
// Starter Release_4 management.
//
//
// Copyleft 2007 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;


/**
 *	This class display a dialog with a list of servers running, stopped,
 *	and buttons to start or stop servers.
 *
 * @author  verdier
 * @version $Revision$
 */
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoApi.events.*;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.ATKConstant;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;
import javax.swing.*;


//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


public class HostInfoDialog extends JDialog implements AstorDefs, TangoConst
{
	public  TangoHost		host;
	public	String			name;
	private Astor			astor;
	private UpdateThread	thread;
	private Color			bg = null;
	
	private String	attribute = "Servers";

	private	static final int	NO_CHANGE     = 0;
	private	static final int	LIST_CHANGED  = 1;
	private static final int	STATE_CHANGED = 2;

	private LevelTree[]			trees = null;
	private ServerPopupMenu		notifd_menu;
	//===============================================================
	/**
	 *	Creates new form HostInfoDialog
	 */
	//===============================================================
	public HostInfoDialog(Astor parent, TangoHost host)
	{
		super(parent, false);
		this.astor  = parent;
		this.host   = host;
		this.name   = host.getName();
		initComponents();
		setTitle(host + "  Control");
		displayAllBtn.setSelected(true);

		thread = new UpdateThread();
		thread.start();

		bg = titlePanel.getBackground();
		titleLabel.setText("Controlled Servers on " + name);
		notifd_menu = new ServerPopupMenu(astor, this, host, ServerPopupMenu.NOTIFD);
		pack();
 		ATKGraphicsUtils.centerDialog(this);
	}
	//===============================================================
	//===============================================================
	void updatePanel()
	{
		//	Build panel
		if (trees==null)
		{
			int	nb_levels = AstorUtil.getStarterNbStartupLevels();
			nb_levels++; //	for not controlled

			JPanel	serverPanel = new JPanel();
        	serverPanel.setLayout(new GridBagLayout());
        	centerPanel.add(serverPanel, java.awt.BorderLayout.CENTER);

			//	First time build notifd label
			if (host.check_notifd)
			{
				JPanel	horiz_panel = new JPanel();
				host.notifd_label = new JLabel("Event Notify Daemon");
            	host.notifd_label.setFont(new Font("Dialog", 1, 12));
				GridBagConstraints gbc = new GridBagConstraints ();
				gbc.gridx  = 0;
				gbc.gridy  = 0;
				gbc.gridwidth = nb_levels;
				gbc.fill  = GridBagConstraints.HORIZONTAL;
				horiz_panel.add (host.notifd_label, gbc);
				serverPanel.add (horiz_panel, gbc);

				//	Add Action listener
				host.notifd_label.addMouseListener (new java.awt.event.MouseAdapter () {
					public void mouseClicked (java.awt.event.MouseEvent evt) {
						serverBtnMouseClicked (evt);
					}
				});
			}

			trees = new LevelTree[nb_levels];

			int	x = 0;
			int	y = 1;
			for (int i=1 ; i<nb_levels ; i++)
			{
				trees[i] = new LevelTree(astor, this, host, i);
				GridBagConstraints gbc = new GridBagConstraints ();
				gbc.gridx  = x++;
				gbc.gridy  = y;
				gbc.insets = new Insets(5, 10, 0, 0);
				gbc.fill   = GridBagConstraints.VERTICAL;
				serverPanel.add (trees[i], gbc);
			}

			//	Add not controlled level
			trees[LEVEL_NOT_CTRL] = new LevelTree(astor, this, host, LEVEL_NOT_CTRL);
			GridBagConstraints gbc = new GridBagConstraints ();
			gbc.gridx  = x;
			gbc.gridy  = y;
			gbc.insets = new Insets(5, 10, 0, 0);
			gbc.fill   = GridBagConstraints.VERTICAL;
			serverPanel.add (trees[LEVEL_NOT_CTRL], gbc);
		}
		else
		for (int i=0 ; i<trees.length ; i++)
			trees[i].checkUpdate();
		
		updateHostState();
		pack();
	}
	//===============================================================
	//===============================================================
	Color getBackgroundColor()
	{
		//	under Win32 the color is from button
		//	From dialog it is another one (Why ?)
		return startNewBtn.getBackground();
	}
	//===============================================================
	//===============================================================
	void updateHostState()
	{
		//	Manage Stater state
		if (host.state==moving)
		{
			String	str_state = ApiUtil.stateName(DevState.MOVING);
			titlePanel.setBackground(ATKConstant.getColor4State(str_state));
		}
		else
		if (host.state==alarm)
		{
			String	str_state = ApiUtil.stateName(DevState.ALARM);
			titlePanel.setBackground(ATKConstant.getColor4State(str_state));
		}
		else
			titlePanel.setBackground(bg);

		//	Update  notifd state
		if (host.check_notifd)
			host.notifd_label.setIcon(Astor.state_icons[host.notifyd_state]);
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
        btnPanel = new javax.swing.JPanel();
        cancelBtn = new javax.swing.JButton();
        centerPanel = new javax.swing.JPanel();
        titlePanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        startNewBtn = new javax.swing.JButton();
        StartAllBtn = new javax.swing.JButton();
        stopAllBtn = new javax.swing.JButton();
        displayAllBtn = new javax.swing.JRadioButton();

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

        btnPanel.add(cancelBtn);

        getContentPane().add(btnPanel, java.awt.BorderLayout.SOUTH);

        centerPanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18));
        titleLabel.setText("Dialog Title");
        titlePanel.add(titleLabel);

        centerPanel.add(titlePanel, java.awt.BorderLayout.NORTH);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        startNewBtn.setText("Start New");
        startNewBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startNewBtnActionPerformed(evt);
            }
        });

        jPanel1.add(startNewBtn);

        StartAllBtn.setText("Start All");
        StartAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StartAllBtnActionPerformed(evt);
            }
        });

        jPanel1.add(StartAllBtn);

        stopAllBtn.setText("Stop All");
        stopAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopAllBtnActionPerformed(evt);
            }
        });

        jPanel1.add(stopAllBtn);

        displayAllBtn.setText("Display All");
        displayAllBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        displayAllBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        displayAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayAllBtnActionPerformed(evt);
            }
        });

        jPanel1.add(displayAllBtn);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//======================================================
	/**
	 *	Manage event on clicked mouse on PogoTree object.
	 */
	//======================================================
	private void serverBtnMouseClicked (java.awt.event.MouseEvent evt)
	{
		notifd_menu.showMenu(evt, host.state);
	}
	//===============================================================
	//===============================================================
    private void displayAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayAllBtnActionPerformed

		boolean b = (displayAllBtn.getSelectedObjects()!=null);
		for (int i=0 ; i<trees.length ; i++)
			if (b)
			{
				if (trees[i].getLevelRow()!=LEVEL_NOT_CTRL)
					trees[i].expandTree();
			}
			else
				trees[i].collapseTree();

    }//GEN-LAST:event_displayAllBtnActionPerformed

	//===============================================================
	//===============================================================
    private void stopAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAllBtnActionPerformed

		//	Check levels used by servers
		Vector	used = new Vector();
		for (int i=trees.length-1 ; i>=0 ; i--)
		{
			int	level = trees[i].getLevelRow();
			if (level!=LEVEL_NOT_CTRL)	//	is controlled
				used.add(new Integer(level));
		}
		//	And stop them
		new ServerCmdThread(this, host, StopAllServers, used).start();

    }//GEN-LAST:event_stopAllBtnActionPerformed

 	//===============================================================
	//===============================================================
    private void StartAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartAllBtnActionPerformed

		//	Check levels used by servers
		Vector	used = new Vector();
		for (int i=0 ; i<trees.length ; i++)
		{
			int	level = trees[i].getLevelRow();
			if (level!=LEVEL_NOT_CTRL)	//	is controlled
				used.add(new Integer(level));
		}
		//	And start them
		new ServerCmdThread(this, host, StartAllServers, used).start();

    }//GEN-LAST:event_StartAllBtnActionPerformed

	//===============================================================
	//===============================================================
	private	static String	servname;
    private void startNewBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startNewBtnActionPerformed

 		//	Get Servername
		ListDialog	jlist = new ListDialog(this);
		//	Search Btn position to set dialog location
		Point	p   = getLocationOnScreen();
		p.translate(50,50);
		jlist.setLocation(p);
		jlist.showDialog();
		servname = jlist.getSelectedItem();
		if (servname!=null)
		{
			try
			{
				//	OK to start, do it.
				//------------------------------------------------------------
				host.registerServer(servname);
				host.startOneServer(servname);

				//	OK to start get the Startup control params.
				//--------------------------------------------------
				TangoServer	ts = new TangoServer(servname, DevState.OFF);
				ts.startupLevel(this, host.getName(), p);
			}
			catch (DevFailed e)
			{
            	ErrorPane.showErrorMessage(astor, null, e);
			}
		}

	}//GEN-LAST:event_startNewBtnActionPerformed

	
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
	void doClose()
	{
		if (astor.getWidth()==0)	//	in test
			System.exit(0);
		setVisible(false);
		dispose();
	}
	//===============================================================
	//===============================================================
    void stopLevel(int level)
	{
		Vector	v = new Vector();
		v.add(new Integer(level));
		new ServerCmdThread(this, host, StopAllServers, v).start();
	}
	//===============================================================
	//===============================================================
    void startLevel(int level)
	{
		Vector	v = new Vector();
		v.add(new Integer(level));
		new ServerCmdThread(this, host, StartAllServers, v).start();
	}
	//=============================================================
	//=============================================================
	private void manageServersAttribute(DeviceAttribute	att)
	{
		//	
		Vector	servers = new Vector();
		try
		{
			if (!att.hasFailed())
			{
				String[]	list = att.extractStringArray();
				for (int i=0 ; i<list.length ; i++)
					servers.add(new Server(list[i]));
			}
		}
		catch(DevFailed e)
		{
			Except.print_exception(e);
		}

		//	Check if something has changed
		switch(updateHost(servers))
		{
		case LIST_CHANGED:
			updatePanel();
			break;
		case NO_CHANGE:
			return;
		}
		for (int i=0 ; i<trees.length ; i++)
			trees[i].repaint();
	}
	//=============================================================
	/**
	 *	Update TangoHost objects and check what has changed.
	 */
	//=============================================================
	public synchronized int updateHost(Vector new_servers)
	{
		boolean	state_changed = false;
		boolean	list_changed = false;

		//	check if new one
		for (int i=0 ; i<new_servers.size() ; i++)
		{
			Server		new_serv = (Server)new_servers.get(i);
			TangoServer	server   = host.getServer(new_serv.name);
			if (server==null)
			{
				//	create it
				try
				{
					server = new TangoServer(new_serv.name, new_serv.state);
				}
				catch(DevFailed e) { Except.print_exception(e);}
				host.addServer(server);
				list_changed = true;
			}

			//	Check state
			if (new_serv.state != server.getState())
			{
				server.setState(new_serv.state);
				state_changed = true;
			}
			//	Check control
			if (new_serv.controlled != server.controlled |
				new_serv.level      != server.startup_level)
			{
				server.controlled    = new_serv.controlled;
				server.startup_level = new_serv.level;
				list_changed = true;
			}
		}

		//	Check if some have been removed
		for (int i=0 ; i<host.nbServers() ; i++)
		{
			TangoServer	server   = host.getServer(i);
			boolean	found = false;
			for (int j=0 ; !found && j<new_servers.size() ; j++)
			{
				Server	new_serv = (Server)new_servers.get(j);
				found = (new_serv.name.equals(server.getName()));
			}
			if (!found)
			{
				host.removeServer(server.getName());
				list_changed = true;
			}
		}

		if(list_changed)
			return LIST_CHANGED;
		else
		if(state_changed)
			return STATE_CHANGED;
		else
			return NO_CHANGE;
	}
	//=============================================================
	//=============================================================
	private DevState string2state(String str)
	{
		for (int i=0 ; i<Tango_DevStateName.length ; i++)
			if (str.equals(Tango_DevStateName[i]))
				return DevState.from_int(i);
		return DevState.UNKNOWN;
	}
	//=============================================================
	//=============================================================
	class Server
	{
		String		name;
		DevState	state;
		boolean		controlled = false;
		int			level = 0;

		//=========================================================
		public Server(String line)
		{
			//	Parse line
        	StringTokenizer stk = new StringTokenizer(line);
			Vector	v = new Vector();
		    while (stk.hasMoreTokens())
        		v.add(stk.nextToken());
			if (v.size()>0)
				this.name = (String)v.get(0);

			if (v.size()>1)
				this.state = string2state((String)v.get(1));

			if (v.size()>2)
				this.controlled = ( ((String)v.get(2)).equals("1") );

			if (v.size()>3)
			{
				String	s = (String)v.get(3);
				try {
					this.level = Integer.parseInt(s);
				} catch(NumberFormatException e){}
			}
		}
		//=========================================================
		public String toString()
		{
			return name + " -> " + ApiUtil.stateName(state) + "	- " +
					((controlled)? "" : "not ") + "Controlled 	level " + level;
		}
		//=========================================================
	}

	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton StartAllBtn;
    private javax.swing.JPanel btnPanel;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JRadioButton displayAllBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton startNewBtn;
    private javax.swing.JButton stopAllBtn;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel titlePanel;
    // End of variables declaration//GEN-END:variables
	//===============================================================


	//=========================================================
	//=========================================================
	void setSelection(String servname)
	{
		for (int i=0 ; i<trees.length ; i++)
		{
			TangoServer	server = trees[i].getServer(servname);
			if (server !=null)
			{
				trees[i].expandTree();
				trees[i].setSelection(server);
			}
			else
				trees[i].resetSelection();
		}
	}
	//=========================================================
	//=========================================================
	void updateData()
	{
		updatePanel();
	}
	//=========================================================
	//=========================================================




















	//===============================================================
	/**
	 * A thread to read and update server lists
	 */
	//===============================================================
	class UpdateThread extends Thread
	{
		private int 	readInfoPeriod = 1000;
		private boolean	stop_it = false;
		//===========================================================
		public UpdateThread()
		{
		}
		//===========================================================
		public void run()
		{
			if (host.use_events)
				subscribeChangeEvent();

			
			//	Manage polling on synchronous calls
			while (!stop_it)
			{
				long		t0 = System.currentTimeMillis();

				if (!host.use_events)
				{
					if (host.do_polling)
					{
						//System.out.println("Reading " + host.name() +
						//		"  on events: " + host.use_events);
						manageSynchronous();
					}
				}
				wait_next_loop(t0);
			}
		}
		//=============================================================
		/**
		 *	Compute time to sleep before next loop
		 */
		//=============================================================
		public synchronized void wait_next_loop(long t0)
		{
			try
			{
				long	t1 = System.currentTimeMillis();
				long	time_to_sleep = readInfoPeriod - (t1-t0);

				if (time_to_sleep<=0)
					time_to_sleep = 100;
				wait(time_to_sleep);
			} 
			catch(InterruptedException e) { System.out.println(e); }
		}
		//=============================================================
		/**
		 *	Read servers list attributes in synchronous mode.
		 */
		//=============================================================
		private void manageSynchronous()
		{
			try
			{
				DeviceAttribute	att = host.read_attribute(attribute);
				manageServersAttribute(att);
			}
			catch(DevFailed e) {
				// Except.print_exception(e);
			}
		}
	}




	private static String[]		filters = new String[0];
	private	ServerEventListener	server_listener = null;
	//======================================================================
	//======================================================================
	private void subscribeChangeEvent()
	{
		try
		{
			if (host.supplier==null)
				host.supplier = new TangoEventsAdapter(host);
		}
		catch(DevFailed e)
		{
			host.use_events = false;
			//	Display exception 
			System.out.println("subscribeChangeServerEvent() for " +
							host.name() + " FAILED !");
			fr.esrf.TangoDs.Except.print_exception(e);
			return;
		}
		catch(Exception e)
		{
			host.use_events = false;
			//	Display exception 
			System.out.println("subscribeChangeServerEvent() for " +
							host.name() + " FAILED !");
			System.out.println(e);
			return;
		}

		try
		{
			//	add listener for double_event and server_event
			server_listener = new ServerEventListener();

			host.supplier.addTangoChangeListener(
						server_listener, attribute, filters);
			//if (AstorUtil.getDebug())
				System.out.println("subscribeChangeServerEvent() for " +
					host.name() + "/" + attribute + " OK!");
		}
		catch(DevFailed e)
		{
			//	Display exception
			host.use_events = false;
			System.out.println("subscribeChangeServerEvent() for " +
					host.name() + " FAILED !");
			fr.esrf.TangoDs.Except.print_exception(e);
			return;
		}
		catch(Exception e)
		{
			//	Display exception 
			host.use_events = false;
			System.out.println("subscribeChangeServerEvent() for " +
							host.name() + " FAILED !");
			System.out.println(e);
			return;
		}
	}
	//======================================================================
	//======================================================================
	public void unsubscribeServersEvent()
	{
		if (host.supplier!=null && server_listener!=null)
			try
			{
			    host.supplier.removeTangoChangeListener(server_listener, attribute);
  			    System.out.println("unsubscribe event for " + host.getName() + "/" + attribute);
			}
			catch(DevFailed e)
			{
				System.out.println("Failed to unsubscribe event for " + attribute);
				fr.esrf.TangoDs.Except.print_exception(e);
			}
	}

	//=========================================================================
	/**
	 *	Server event listener
	 */
	//=========================================================================
	class ServerEventListener implements ITangoChangeListener
	{
		//=====================================================================
		//=====================================================================
    	public void change(TangoChangeEvent event)
		{
System.out.println(" change(TangoChangeEvent event)");
			TangoChange	tc = (TangoChange)event.getSource();
			String		devname = tc.getEventSupplier().name();
			String[]	servers = new String[0];
			String		attname = null;
			
			try
			{
			 	DeviceAttribute	att = event.getValue();
				manageServersAttribute(att);
				//if (AstorUtil.getDebug())
				//	System.out.println(devname + "/" + att.getName() + " changed " + " : ");
	    	}
			catch (DevFailed e)
			{
				if (e.errors[0].reason.equals("API_EventTimeout"))
				{
					System.out.println("HostStataThread.ServerEventListener" +
										devname + " : API_EventTimeout");
					//fr.esrf.TangoDs.Except.print_exception(e);
				}
				else
					fr.esrf.TangoDs.Except.print_exception(e);
			}
        	catch (Exception e)
        	{
				System.out.println("AstorEvent." + devname);
 				System.out.println(e);
            	System.out.println("HostStateThread.ServerEventListener : could not extract data!");
        	}
		}
	}
    //======================================================
    //======================================================
      class Blink
      {
          private JComponent  obj;
          private int     cnt;
          private Timer   timer;
          private Color   color;
          private long    t0 = 0;
          private int     duration;
          private String  text = null;

        //======================================================
        /**
         * Constructor for blicking object
         *
         * @param obj JComponent to start blinking
         */
        //======================================================
        Blink(JComponent obj)
        {
            this.obj = obj;
            cnt      = 0;
            color    = obj.getBackground();
        }
       //======================================================
       /**
        * Constructor for blicking object
        *
        * @param obj      JComponent to start blinking
        * @param duration Duration to blink in second
        */
        //======================================================
       Blink(JComponent obj, int duration)
       {
           this(obj);
           this.duration = duration;
           if (obj instanceof JLabel)
               text = ((JLabel)obj).getText();
           else
               color = obj.getBackground();
           t0 = System.currentTimeMillis();
        }
        //======================================================
        //======================================================
        void blinkPerformer(ActionEvent evt)
        {
            cnt++;
            if (cnt%2==0)
            {
                if (obj instanceof JLabel)
                    ((JLabel)obj).setText("-> " + text);
                else
                    obj.setBackground(color);
            }
            else
            {
                if (obj instanceof JLabel)
                    ((JLabel)obj).setText(text);
                else
                   obj.setBackground(Color.lightGray);
            }
            //    Check if terminated.
            if (duration!=0)
            {
                long  t1 = System.currentTimeMillis();
                if (t1-t0 >= duration*1000)
                {
                    timer.stop();
                    if (obj instanceof JLabel)
                        ((JLabel)obj).setText(text);
                    else
                        obj.setBackground(color);
                }
            }
        }
        //======================================================
        //======================================================
        private void start()
        {
            //	Fire a timer every once in a while make blink the button
            ActionListener taskPerformer = new ActionListener() {
               public void actionPerformed(ActionEvent evt) {
                   blinkPerformer(evt);
               }
            };
            timer = new Timer(200, taskPerformer);
            timer.start();
        }
      }

}
