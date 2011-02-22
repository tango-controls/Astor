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
	private Levels			levels;
	private UpdateThread	thread;
	private Color			bg = null;
	
	private String	attribute = "Servers";

	private	static final int	NO_CHANGE     = 0;
	private	static final int	LIST_CHANGED  = 1;
	private static final int	STATE_CHANGED = 2;

	private ServerPopupMenu	pMenu;
	private boolean			updating = false;
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

		pMenu = new ServerPopupMenu(astor, this, host);
		levels = new Levels();
		thread = new UpdateThread();
		thread.start();

		bg = titlePanel.getBackground();
		titleLabel.setText("Controlled Servers on " + name);
		pack();
 		ATKGraphicsUtils.centerDialog(this);
	}
	//===============================================================
	//===============================================================
	void updatePanel()
	{
		updating = true;

		//	Build levels vector
		updateStartupLevels();
		
		//	Build pale
		servPanel.removeAll();
		levels.buildPanels();

		//	Check if level 0 must be displayed
		boolean b = (displayAllBtn.getSelectedObjects()!=null);
		levels.setLevelVisible(0,b);

		//	Update states
		updateServerStates();

		pack();

		updating = false;
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
	//===============================================================
	private void updateServerStates()
	{
		//System.out.println("updateServerStates()");  
		if (host.check_notifd)
			host.notifd_label.setIcon(Astor.state_icons[host.notifyd_state]);

		for (int i=0 ; i<host.nbServers() ; i++)
		{
			TangoServer	server = host.getServer(i);
			int	idx = unknown;
			if (server.getState()==DevState.MOVING)
				idx= moving;
			else
			if (server.getState()==DevState.ON)
				idx = all_ok;
			else
				idx = faulty;
			server.label.setIcon(Astor.state_icons[idx]);
		}
	}
	//===============================================================
	//===============================================================
	private void updateStartupLevels()
	{
		levels.clear();
		//	Get startup info for each server
		for (int i=0 ; i<host.nbServers() ; i++)
		{
			TangoServer	server = host.getServer(i);
			//	And dispach in levels vectors.
			LevelServers	ls = levels.getLevelServers(server.startup_level);
			if (ls==null)
			{
				ls = new LevelServers(server.startup_level);
				levels.add(ls);
			}
			ls.add(server);
		}

		//	Alphabetical order in each level
		for (int i=0 ; i<levels.size() ; i++)
		{
			LevelServers	ls = levels.getServersAt(i);
			AstorUtil.getInstance().sort(ls);
		}
		
		//	set level 0 at end
		levels.sort();
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
        servPanel = new javax.swing.JPanel();
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

        servPanel.setLayout(new java.awt.GridBagLayout());

        centerPanel.add(servPanel, java.awt.BorderLayout.WEST);

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

	//===============================================================
	//===============================================================
    private void displayAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayAllBtnActionPerformed

		boolean b = (displayAllBtn.getSelectedObjects()!=null);
		
		levels.setLevelVisible(0,b);
		pack();

    }//GEN-LAST:event_displayAllBtnActionPerformed

	//===============================================================
	//===============================================================
    private void stopAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAllBtnActionPerformed

		//	Check levels used by servers
		Vector	used = new Vector();
		for (int i=levels.size()-1 ; i>=0 ; i--)
		{
			LevelServers ls = levels.getServersAt(i);
			if (ls.level!=0)	//	not controlled
				used.add(new Integer(ls.level));
		}
		//	And stop them
		new ServerCmdThread(this, host, StopAllServers, used).start();

    }//GEN-LAST:event_stopAllBtnActionPerformed

 	//===============================================================
	//===============================================================
    private void StartAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartAllBtnActionPerformed


		//	Check levels used by servers
		Vector	used = new Vector();
		for (int i=0 ; i<levels.size() ; i++)
		{
			LevelServers ls = levels.getServersAt(i);
			if (ls.level!=0)	//	not controlled
				used.add(new Integer(ls.level));
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

	//======================================================
	/**
	 *	Manage event on clicked mouse on PogoTree object.
	 */
	//======================================================
	private void serverBtnMouseClicked (java.awt.event.MouseEvent evt)
	{
		pMenu.showMenu(evt, host.state);
	}
	
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
		case STATE_CHANGED:
			updateServerStates();
			break;
		}			
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
    private javax.swing.JPanel servPanel;
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
		TangoServer	ts = levels.getServerByName(servname);
	
        //  Wait a bit for panel construction
        while(updating)
            try { Thread.sleep(100); }catch(Exception e) {/** Nothing to do */}

		if (ts!=null)
		{
	        Blink blink = new Blink(ts.label, 5);
            blink.start();
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
	 *	Implements a vector of LevelServers classes
	 */
	//===============================================================
	class Levels extends Vector
	{
		//===========================================================
		LevelServers getServersAt(int idx)
		{
			return (LevelServers)get(idx);
		}
		//===========================================================
		TangoServer getServerByName(String servname)
		{
			for (int i=0 ; i<size(); i++)
			{
				LevelServers	ls = (LevelServers)get(i);
				TangoServer		ts = ls.getServer(servname);
				if (ts!=null)
					return ts;
			}
			return null;
		}
		//===========================================================
		private int nbHoriz()
		{
			//	Compute number server on x
			int	nbmax = 0;
			for (int i=0 ; i<size(); i++)
			{
				LevelServers	ls = (LevelServers)get(i);
				if (nbmax<ls.size())
					nbmax = ls.size();
			}
    	    //   Claculate how many servers in horizontal
			int	x_servers = (int)Math.sqrt(nbmax);
			if (x_servers>2) x_servers--;
        	if (x_servers>4) x_servers = 4;
			return x_servers;
		}
		//===========================================================
		LevelServers getLevelServers(int level)
		{
			for (int i=0 ; i<size() ; i++)
			{
				LevelServers	ls = (LevelServers)get(i);
				if (ls.level==level)
					return ls;
			}
			return null;
		}
		//===========================================================
		void buildPanels()
		{
			int	x_servers = nbHoriz();
			int	y = 0;

			//	First time build notifd labael
			if (host.check_notifd)
			{
				GridBagConstraints gbc = new GridBagConstraints();
				host.notifd_label = new JLabel("Events Notify Daemon");
            	host.notifd_label.setIcon(Astor.state_icons[unknown]);
            	host.notifd_label.setFont(new Font("Dialog", 1, 12));
				gbc.gridwidth = x_servers;
				gbc.gridx = 0;
				gbc.gridy = y++;
				gbc.fill   = GridBagConstraints.HORIZONTAL;
            	gbc.anchor = GridBagConstraints.CENTER;
				gbc.insets = new Insets(5, 0, 5, 0);
				JPanel	panel = new JPanel();	//	A tmp panel to center
				panel.add(host.notifd_label);
 				servPanel.add(panel, gbc);

				//	Add Action listener
				host.notifd_label.addMouseListener (new java.awt.event.MouseAdapter () {
					public void mouseClicked (java.awt.event.MouseEvent evt) {
						serverBtnMouseClicked (evt);
					}
				});
			}
			//	And then a panel for each level
			for (int i=0 ; i<size() ; i++)
			{
				LevelServers	ls = getServersAt(i);
				y = ls.buildPanel(y, x_servers);
			}
		}
		//===========================================================
		void sort()
		{
			//	set level 0 at end
			AstorUtil.getInstance().sort(this);
 			for (int i=0 ; i<size()-1 ; i++)
			{
				LevelServers	ls = levels.getServersAt(i);
				if (ls.level==0)
				{
					levels.remove(i);
					levels.add(ls);
				}
			}
		}
		//===========================================================
		void setLevelVisible(int level, boolean b)
		{
			for (int i=0 ; i<size() ; i++)
			{
				LevelServers	ls = levels.getServersAt(i);
				if (ls.level==level)
					ls.setVisible(b);
			}
		}
		//===========================================================
		public String toString()
		{
			return host.name() + " ";
		}
		//===========================================================
	}
	//===============================================================
	/**
	 *	Implements a vector of TangoServer classes for one level
	 */
	//===============================================================
	class LevelServers extends Vector
	{
		int 	level;
		JLabel	title;
		//===========================================================
		public LevelServers(int level)
		{
			this.level = level;
			//System.out.println("Creating level "+level);
		}
		//===========================================================
		int buildPanel(int y, int x_servers)
		{
			//	Create a panel for the level
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill   = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

			//	Create a label for level title
			String	str = "Level " + level + " :";
			if (level==0)
				str = "Not Controlled :";
			title = new JLabel(str);
            title.setFont(new Font("Dialog", 1, 14));
            title.setHorizontalAlignment(SwingConstants.LEFT);
			gbc.gridx = 0;
			gbc.gridy = y++;
			gbc.insets = new Insets(10, 10, 2, 10);
 			servPanel.add(title, gbc);

			//	Create label for each server
			int	x = 0;
			for (int i=0 ; i<size() ; i++)
			{
				TangoServer	server = getServer(i);
				server.label = new JLabel(server.getName());
                server.label.setHorizontalAlignment(SwingConstants.LEFT);
                server.label.setIcon(Astor.state_icons[unknown]);
                server.label.setFont(new Font("Dialog", 1, 12));
				gbc.gridx = x;
				gbc.gridy = y;
				gbc.insets = new Insets(0, 10, 2, 10 );

				if (x<x_servers-1)
					x++;
				else
				{
					x = 0;
					y++;
				}
 				servPanel.add(server.label, gbc);

				//	Add Action listener
				server.label.addMouseListener (new java.awt.event.MouseAdapter () {
					public void mouseClicked (java.awt.event.MouseEvent evt) {
						serverBtnMouseClicked (evt);
					}
				});

			}
  			//	Add dummy labels to align
			while (x<=x_servers)
			{
				gbc.gridx = x++;
				gbc.gridy = y;
				servPanel.add(new JLabel(" "), gbc);
			}
			
			return ++y;
		}
		//===========================================================
		void setVisible(boolean b)
		{
			title.setVisible(b);
			for (int i=0 ; i<size() ; i++)
			{
				TangoServer	server = getServer(i);
				server.label.setVisible(b);
			}
		}
		//===========================================================
		TangoServer getServer(int idx)
		{
			return (TangoServer)get(idx);
		}
		//===========================================================
		TangoServer getServer(String servname)
		{
			for (int i=0 ; i<size() ; i++)
			{
				TangoServer	server = getServer(i);
				if (server.getName().equals(servname))
					return server;
			}
			return null;
		}
		//===========================================================
		int nbControlledServers()
		{
			int	nb = 0;
			for (int i=0 ; i<size() ; i++)
				if (getServer(i).controlled)
					nb++;
			return nb;
		}
		//===========================================================
		public String toString()
		{
			return "Level " + level;
		}
		//===========================================================
	}








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
