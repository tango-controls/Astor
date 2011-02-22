//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the Pogo class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.16  2005/06/02 09:02:36  pascal_verdier
// Minor changes.
//
// Revision 3.15  2005/05/19 11:28:59  pascal_verdier
// Minor changes.
//
// Revision 3.14  2005/04/22 09:30:44  pascal_verdier
// Use events management in starter properies dialog added.
//
// Revision 3.13  2005/03/15 10:22:30  pascal_verdier
// Sort servers before creating panel buttons.
//
// Revision 3.12  2005/02/10 15:38:19  pascal_verdier
// Event subscritions have been serialized.
//
// Revision 3.11  2005/01/24 09:35:58  pascal_verdier
// export/unexport new server before stating to be known by starter in case of startup failed.
//
// Revision 3.10  2004/11/23 14:05:57  pascal_verdier
// Minor changes.
//
// Revision 3.9  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.8  2004/07/09 08:12:49  pascal_verdier
// HostInfoDialog is now awaken only on servers change.
//
// Revision 3.7  2004/07/08 11:22:58  pascal_verdier
// First revision able to use events.
//
// Revision 3.6  2004/05/04 07:05:27  pascal_verdier
// Bug on notify daemon fixed.
// server reconection transparency added.
//
// Revision 3.5  2004/03/03 08:31:04  pascal_verdier
// The server restart command has been replaced by a stop and start command in a thread.
// The delete startup level info has been added.
//
// Revision 3.4  2004/02/04 14:37:42  pascal_verdier
// Starter logging added
// Database info added on CtrlServersDialog.
//
// Revision 3.3  2003/11/25 15:56:45  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
// Revision 3.2  2003/11/07 09:58:46  pascal_verdier
// Host info dialog automatic resize implemented.
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
// Revision 3.0  2003/06/04 12:37:52  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.1  2003/06/04 12:33:11  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;
 


/** 
 *	This class display a dialog with a list of servers running, stopped,
 *	and buttons to start or stop servers.
 *
 * @author  verdier
 * @Revision 
 */


import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;


//===============================================================
/**
 *	A Dialog Class to get the State parameters.
 */
//===============================================================
public class HostInfoDialog extends JDialog implements AstorDefs {

	String	name;
	private Astor		astor;
	private TangoHost	host;
	private	static String		servname;
	private	JButton		notifydBtn;
	private	JButton[]	btn;
	private short		nbStartupLevels = 0;
	private JPanel[]	levelPanel;
	private Vector[] 	serverLevel;
	
	private	ServerThread	thread = null;
	private static int	returnStatus = 0;
	
	/**
	 *	Popup menu to be used on right button clicked.
	 */
	private ServerPopupMenu	pMenu;
	//==========================================================
	/**
	 *	Class Constructor
	 */
	//==========================================================
	public HostInfoDialog(Astor astor, TangoHost host) {
		super (astor, false);
		initComponents ();
		nbStartupLevels = AstorUtil.getStarterNbStartupLevels();
		this.astor = astor;
		this.host   = host;
		this.name   = host.getName();
		myInitComponents();

/*
		//	Arm a timer to check if panel must be updated
		int delay = 1000; //milliseconds
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				checkUpdatePanel();
    	}};
		new javax.swing.Timer(delay, taskPerformer).start();
*/
	}
	//==========================================================
	/**
	 *	Initializes the Form
	 */
	//==========================================================
	private void myInitComponents()
	{
		//	Add a button to dimiss
		JButton	btn = new JButton("Dismiss");
		btn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dismissBtnActionPerformed(evt);
			}
		});
		jPanel1.add(btn);
		

		//	Create a panel for each startup level
		createStartupLevelPanels();
		//	Build the server panel and start thread.
		buildPanel();
		pMenu = new ServerPopupMenu(astor, this, host);
		thread = new ServerThread();
		thread.start();

		setTitle(host + "  Control");

		pack ();
	}

	//==========================================================
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the FormEditor.
	 */
	//==========================================================
        private void initComponents() {//GEN-BEGIN:initComponents
              hostPanel = new javax.swing.JPanel();
              jPanel1 = new javax.swing.JPanel();
              jLabel1 = new javax.swing.JLabel();
              jPanel2 = new javax.swing.JPanel();
              title = new javax.swing.JLabel();
              jPanel3 = new javax.swing.JPanel();
              startNewBtn = new javax.swing.JButton();
              startAllBtn = new javax.swing.JButton();
              stopAllBtn = new javax.swing.JButton();
              displayAllBtn = new javax.swing.JRadioButton();
              jSeparator1 = new javax.swing.JSeparator();
              
              setTitle("Host Info Window");
              setBackground(new java.awt.Color(198, 178, 168));
              addWindowListener(new java.awt.event.WindowAdapter() {
                  public void windowClosing(java.awt.event.WindowEvent evt) {
                      closeDialog(evt);
                  }
              });
              
              hostPanel.setLayout(new java.awt.GridBagLayout());
              java.awt.GridBagConstraints gridBagConstraints1;
              
              getContentPane().add(hostPanel, java.awt.BorderLayout.CENTER);
              
              jLabel1.setText("         ");
              jPanel1.add(jLabel1);
              
              getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
              
              jPanel2.setLayout(new java.awt.BorderLayout());
              
              title.setFont(new java.awt.Font("Dialog", 1, 18));
              title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
              title.setText("jLabel2");
              jPanel2.add(title, java.awt.BorderLayout.SOUTH);
              
              startNewBtn.setFont(new java.awt.Font("Dialog", 1, 10));
                startNewBtn.setText("Start New");
                startNewBtn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        startNewBtnActionPerformed(evt);
                    }
                });
                
                jPanel3.add(startNewBtn);
                
                startAllBtn.setFont(new java.awt.Font("Dialog", 1, 10));
                startAllBtn.setText("Start All");
                startAllBtn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        startAllBtnActionPerformed(evt);
                    }
                });
                
                jPanel3.add(startAllBtn);
                
                stopAllBtn.setFont(new java.awt.Font("Dialog", 1, 10));
                stopAllBtn.setText("Stop All");
                stopAllBtn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        stopAllBtnActionPerformed(evt);
                    }
                });
                
                jPanel3.add(stopAllBtn);
                
                displayAllBtn.setFont(new java.awt.Font("Dialog", 1, 10));
                displayAllBtn.setText("Display All");
                displayAllBtn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        displayAllBtnActionPerformed(evt);
                    }
                });
                
                jPanel3.add(displayAllBtn);
                
                jPanel2.add(jPanel3, java.awt.BorderLayout.NORTH);
              
              jPanel2.add(jSeparator1, java.awt.BorderLayout.CENTER);
              
              getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);
            
        }//GEN-END:initComponents

	//======================================================
	/**
	 *	Called when display Server is clicked.
	 */
	//======================================================
	private void displayAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayAllBtnActionPerformed
		//	Get the btn state and set it for host.
		//--------------------------------------------------------
		host.all_servers = (displayAllBtn.getSelectedObjects()!=null);
		if (host.use_events)
		{
			checkUpdatePanel(true);
			thread.updateData(true);
		}
		else
			host.updateData();
	}//GEN-LAST:event_displayAllBtnActionPerformed

	//======================================================
	/**
	 *	Called when Stop all Servers is clicked.
	 */
	//======================================================
	private void stopAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAllBtnActionPerformed

		//	Check levels used by servers
		boolean[]	levelUsed = new boolean[nbStartupLevels];
		for (int level=0 ; level<nbStartupLevels ; level++)
			levelUsed[level] = (serverLevel[level].size()!=0);
		
		//	And stop them
		new ServerCmdThread(this, host, StopAllServers, levelUsed).start();

	}//GEN-LAST:event_stopAllBtnActionPerformed

 	//======================================================
	/**
	 *	Called when Start all Servers is clicked.
	 */
	//======================================================
	private void startAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startAllBtnActionPerformed

		//	Check levels used by servers
		boolean[]	levelUsed = new boolean[nbStartupLevels];
		for (int level=0 ; level<nbStartupLevels ; level++)
			levelUsed[level] = (serverLevel[level].size()!=0);
		
		//	And start them
		new ServerCmdThread(this, host, StartAllServers, levelUsed).start();

	}//GEN-LAST:event_startAllBtnActionPerformed

	//======================================================
	/**
	 *	Called when Start new Server is clicked.
	 */
	//======================================================
	private void startNewBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startNewBtnActionPerformed
		//	Get Servername
		ListDialog	jlist = new ListDialog(astor);
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
				if (new TangoServer(servname, false).startupLevel(this, host.getName(), p))
					updateData();
			}
			catch (DevFailed e)
			{
				app_util.PopupError.show(astor, e);
			}
		}
	}//GEN-LAST:event_startNewBtnActionPerformed

	//======================================================
	/**
	 *	Called when a server button is clicked.
	 */
	//======================================================
	private void serverBtnActionPerformed(ActionEvent evt)
	{
		if (host.state==faulty)
		{
			String	message = new String(
				"There is no Tango Administrator Server running on the Host '" +
				host.getName()+"'\n\n"+
				"Would you like a remote login to start it ?");
			
			if (JOptionPane.showConfirmDialog(this, 
							message,
							"Error Window",
							JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
			{
				new RemoteLoginThread(host.getName(), astor).start();
			}
		}
	}
	//======================================================
	/**
	 *	Closes the dialog
	 */
	//======================================================
	private void dismissBtnActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dismissBtnActionPerformed
		doClose(-1);
	}//GEN-LAST:event_dismissBtnActionPerformed

	//======================================================
	/**
	 *	Closes the dialog
	 */
	//======================================================
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		doClose(-1);
	}//GEN-LAST:event_closeDialog

   //======================================================
   /**
    *	Build an array (for each startup level) of
	*	vectors of servers
	*/
   //======================================================
   private synchronized Vector[] buildVectorOfStartupLevel()
   {
		int		nb = nbStartupLevels;	
		nb++;	//	Not started 
		nb++;	//	Not controlled

		try
		{
			//	Update server info from Database
			for(int i=0 ; i<host.nbServers() ; i++)
			{
				TangoServer	server = host.getServer(i);
				server.updateStartupInfo();
			}
		}
		catch (DevFailed e) {
			app_util.PopupError.show(astor, e);
		}
		//	Check if server exist for each level
		Vector[] serverLevel = new Vector[nb];
		int	idx = 0;
		for (int level=1 ; level<=nbStartupLevels ; level++, idx++)
		{
			serverLevel[idx] = new Vector();
			for(int i=0 ; i<host.nbServers() ; i++)
			{
				//	if exists, store it in vector
				TangoServer	server = host.getServer(i);
				if (server!=null)
					if (server.info.startup_level==level)
						serverLevel[idx].add(server);
			}
		}
		//	treat not started and not controlled servers
		serverLevel[nb-2] = new Vector();
		serverLevel[nb-1] = new Vector();
		for(int i=0 ; i<host.nbServers() ; i++)
		{
			//	if exists, store it in vector
			TangoServer	server = host.getServer(i);
			if (server.info.controlled)
			{
				if (server.info.startup_level==0)
					serverLevel[nb-2].add(server);
			}
			else
				serverLevel[nb-1].add(server);
		}

		//	Sort for alphabetical order
		 AstorUtil.getInstance().sort(serverLevel);
		
		return serverLevel;
   }
   
	//==========================================================
	/**
	 *	Create a panel for each startup level
	 */
	//==========================================================
	private void addLevelPanelTitles()
	{
		int		nb = nbStartupLevels + 2;	// not started and not controlled

		GridBagConstraints gbc = new GridBagConstraints();
		int	idx = 0;
		gbc.gridx  = 1;
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		for (int level=1 ; level<=nbStartupLevels ; level++, idx++)
		{
			//	Add a label to display level
			gbc.gridy = 0;
			String	text = new String("Level " + level + " : ");
			levelPanel[idx].add(new JLabel(text), gbc);

			//	Add a Dummy label as separator
			gbc.gridy = 500;
			levelPanel[idx].add(new JLabel(" "), gbc);
		}
		//	Add a label to display level
		gbc.gridy = 0;
		levelPanel[idx].add(new JLabel("Not Automaticly Started:"), gbc);
		//	Add a Dummy label as separator
		gbc.gridy = 500;
		levelPanel[idx].add(new JLabel(" "), gbc);
		idx++;
		//	Add a label to display level
		gbc.gridy = 0;
		levelPanel[idx++].add(new JLabel("Not Controlled:"), gbc);
	}
	//==========================================================
	/**
	 *	Create a panel for each startup level
	 */
	//==========================================================
	private void createStartupLevelPanels()
	{
		int		nb = nbStartupLevels;
		nb++;	//	Notify Daemon
		nb++;	//	Not started 

		levelPanel = new JPanel[nb];
		GridBagConstraints gbc = new GridBagConstraints();
		//hostPanel.setLayout (new GridBagLayout ());
		int	idx = 0;
		int	y   = 0;
		gbc.gridx  = 1;
		gbc.anchor = java.awt.GridBagConstraints.WEST;



		//	create one for Notify daemon
		gbc.gridy = y++;
		gbc.gridwidth = 2;
		gbc.fill  = GridBagConstraints.HORIZONTAL;
		notifydBtn = new JButton("Events Notify Daemon");
		notifydBtn.setVisible(host.check_notifd);
		hostPanel.add(notifydBtn, gbc);
		//	Add Action listener
		notifydBtn.addMouseListener (new java.awt.event.MouseAdapter () {
			public void mouseClicked (java.awt.event.MouseEvent evt) {
				serverBtnMouseClicked (evt);
			}
		});

		//	create a dummy separator
		gbc.gridy = y++;
		gbc.gridwidth = 1;
		gbc.fill  = GridBagConstraints.NONE;
		hostPanel.add(new JLabel("   "), gbc);

		for (int level=1 ; level<=nbStartupLevels ; level++, idx++, y++)
		{
			// put level 0 (not controlled at the end
			gbc.gridy = y;
			levelPanel[idx] = new JPanel(new GridBagLayout ());
			hostPanel.add(levelPanel[idx], gbc);
			//	Add Action listener
			levelPanel[idx].addMouseListener (new java.awt.event.MouseAdapter () {
				public void mouseClicked (java.awt.event.MouseEvent evt) {
					serverBtnMouseClicked (evt);
				}
			});

		}
		
		//	create for controlled but not started
		gbc.gridy = y++;
		levelPanel[idx] = new JPanel(new GridBagLayout ());
		hostPanel.add(levelPanel[idx], gbc);
		idx++;

		//	create for NOT controlled
		gbc.gridy = y++;
		levelPanel[idx] = new JPanel(new GridBagLayout ());
		hostPanel.add(levelPanel[idx], gbc);
	}
   //======================================================
   /**
    *	Buid or rebuild panel. That means a button for each server.
	*/
   //======================================================
	public void buildPanel()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		if (host.all_servers)
			title.setText(" All Servers Registred on "+ name + " ");
		else
			title.setText(" Servers Controlled on "+ name + " ");
	
		btn = new JButton[host.nbServers()];
		serverLevel = buildVectorOfStartupLevel();

		//	Compute number server on x
		int	nbmax = 0;
		for (int level=0 ; level<nbStartupLevels +2; level++)
		{
			//	check if display only controlled servers or all
			if ((level==nbStartupLevels+1) &&
				(displayAllBtn.getSelectedObjects()==null))
				break;
			if (serverLevel[level].size() > nbmax)
				nbmax = serverLevel[level].size();
		}
		int	x_servers = (int)Math.sqrt(nbmax);
		if (x_servers>2)
			x_servers--;
		
		int btnIdx = 0;
		for (int level=0 ; level<nbStartupLevels+2 ; level++)
		{
			//	check if display only controlled servers or all
			if ((level==nbStartupLevels+1) &&
				(displayAllBtn.getSelectedObjects()==null))
			{
				levelPanel[level].setVisible(false);
				break;
			}

			if (serverLevel[level].size()==0)
			{
				levelPanel[level].setVisible(false);
				continue;
			}
			//System.out.println(level + ": " + serverLevel[level].size());

			//	build panel for this level
			levelPanel[level].setVisible(true);
			for(int i=0, y=1, x=1 ;
					i<serverLevel[level].size() && btnIdx<btn.length ; i++)
			{
				//	Create a panel to display server info
				//-----------------------------------------
				TangoServer	server = (TangoServer)serverLevel[level].elementAt(i);

				gbc.gridx = x++;
				gbc.gridy = y;
				gbc.fill = GridBagConstraints.HORIZONTAL;
		
				btn[btnIdx] = new JButton(server.toString());
				levelPanel[level].add(btn[btnIdx], gbc);
				//System.out.println("	" +server.toString() + " in  panel #"+level);

				btn[btnIdx].addActionListener (new ActionListener () {
   			     	public void actionPerformed (ActionEvent evt) {
						serverBtnActionPerformed (evt);
					}
				});
				//	Add Action listener
				//------------------------------------
				btn[btnIdx].addMouseListener (new java.awt.event.MouseAdapter () {
					public void mouseClicked (java.awt.event.MouseEvent evt) {
						serverBtnMouseClicked (evt);
					}
				});

				//	Get project title from property
				//-----------------------------------------
				int	pos = server.getName().indexOf("/");
				if (pos>0)
				{
					String   classname = server.getName().substring(0, pos);
					String[] prop = AstorUtil.getServerClassProperties(classname);
					if (prop[0] != null)
						btn[btnIdx].setToolTipText(prop[0]);
				}

				//	Increase Y if enough servers on X
				//--------------------------------------
				if (x>x_servers)
				{
					x = 1;
					y++;
				}
				btnIdx++;
			}                  
		}
		addLevelPanelTitles();
		pack();

		//	Try to fix same width for all buttons
		if (btn.length>0)
		{
			int	height = 10;
			int	width  = 0;
			for (int i=0 ; i<btn.length ; i++)
				if (btn[i]!=null && btn[i].isVisible())
					if (width<btn[i].getWidth())
					{
						width = btn[i].getWidth();
						height = btn[i].getHeight();
					}
			for (int i=0 ; i<btn.length ; i++)
				if (btn[i]!=null && btn[i].isVisible())
					btn[i].setPreferredSize(new Dimension(width, height));
		}
		pack();
		//setVisible(true);
	}
	//======================================================
	/**
	 *	Manage event on clicked mouse on PogoTree object.
	 */
	//======================================================
	private void serverBtnMouseClicked (java.awt.event.MouseEvent evt)
	{
		pMenu.showMenu(evt);
	}
  //======================================================
  /**
   *	Re-build the panel.
   */
  //======================================================
	public void updatePanel()
	{
		//	Remove all server buttons  nbStartupLevels
		for (int level=0 ; level<levelPanel.length ; level++)
			levelPanel[level].removeAll();
		buildPanel();

		//	Awake updating thread
		thread.updateData(true);	
	}
	//==========================================================
	//==========================================================
	private void checkUpdatePanel()
	{
		if (isVisible())
		{
			//System.out.println("btn.length = " + btn.length + 
			//		"  -  host.nbServers()" + host.nbServers());
			if (btn.length != host.nbServers())
				checkUpdatePanel(true);
		}
	}
	//==========================================================
	//==========================================================
	void checkUpdatePanel(boolean forced)
	{
		if (forced)
		{
			//	Remove all server buttons  nbStartupLevels
			for (int level=0 ; level<levelPanel.length ; level++)
				levelPanel[level].removeAll();
			buildPanel();
		}
	}
  //======================================================
  /**
   *	get the selected user choice.
   */
  //======================================================
  public int getChoice()
  {
  	return returnStatus;
  }
  //======================================================
  /**
   *	return the server name to be started.
   */
  //======================================================
  public String getServerName()
  {
  	return servname;
  }
  //======================================================
  void doClose(int retStatus)
  {
	host.poll_serv_lists = false;
    returnStatus = retStatus;
    setVisible(false);
	thread.updateData(false);
    dispose ();
  }
  //======================================================
  //======================================================
  void updateData()
  {
	thread.updateData(true);
  }
  //======================================================
  //======================================================

  //======================================================
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel hostPanel;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JLabel title;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JButton startNewBtn;
  private javax.swing.JButton startAllBtn;
  private javax.swing.JButton stopAllBtn;
  private javax.swing.JRadioButton displayAllBtn;
  private javax.swing.JSeparator jSeparator1;
  // End of variables declaration//GEN-END:variables

	

	//======================================================
	/**
	 *	A thread class to control device.
	 */
	//======================================================
	class  ServerThread extends Thread
	{
		private long		period;
		private boolean		update_data = true;
		//===============================================================
		//===============================================================
		public ServerThread()
		{
			period = AstorUtil.getStarterReadPeriod()/2;
		}


		//===============================================================
		//===============================================================
		public synchronized void wait_next_update()
		{
			try {
				wait(0);
			}
			catch(InterruptedException e) {}
		}
		//===============================================================
		/**
		 *	Strat/stop updating.
		 */
		//===============================================================
		public synchronized void updateData(boolean b)
		{
			update_data = b;
			notify();
		}
		//===============================================================
		/**
		 *	Update dialog server buttons.
		 */
		//===============================================================
		private synchronized void updateParent()
		{
			int	state;

			checkUpdatePanel(true);
			for (int i=0 ; i<btn.length ; i++)
			{
				if (btn[i]!=null)
				{
					TangoServer	server = host.getServer(btn[i].getText());
					if (server==null)
						state = unknown;
					else
					if (server.isRunning())
						state = all_ok;
					else
						state = faulty;
					btn[i].setForeground(fg[state]);
					btn[i].setBackground(bg[state]);
					
				}
				//else
				//	System.out.println("btn[" + i + "]==null");
			}
			//	Manage Notify Daemon button if visible
			state = host.notifyd_state;
			if (notifydBtn.isVisible())
			{
				notifydBtn.setForeground(fg[state]);
				notifydBtn.setBackground(bg[state]);
			}
		}
		//===============================================================
		/**
		 *	The thread main loop
		 */
		//===============================================================
		public void run()
		{
			while (true)
			{
				if (update_data)
					updateParent();
				wait_next_update();
			}
		}
	}
}
