//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011
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


package admin.astor;

/**
 *	This class is the Astor main panel
 *	containing the Jtree used to display hosts.
 *
 * @author  verdier
 */

import admin.astor.statistics.StatisticsPanel;
import admin.astor.tools.*;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.Splash;
import fr.esrf.tangoatk.widget.util.JSmoothProgressBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.Vector;


public class Astor extends JFrame implements AstorDefs
{

	/**
	 *  Initialized by make jar call and used to display title.
	 */
    private static String revNumber =
            "Release 5.5.1  -  Tue Apr 12 14:12:12 CEST 2011";
	/**
	 *	JTree object to display control system.
	 */
	public AstorTree	tree = null;
	/**
	 *	JTree state
	 */
	private boolean		expanded = false;

	/**
	 *	JTree Container
	 */
	private JScrollPane	scrollPane;
	/**
	 *	true if Astor is fully built and displayed.
	 */
	static boolean displayed = false;

    public static DevBrowser   dev_browser = null;
    private String tango_host = "";

    private MultiServerCommand  multiServerCommand = null;

	static long	t0;
	//======================================================================
    /**
	 *	Creates new form Astor
     * @throws DevFailed in case of database connection failed
	 */
	//======================================================================
	public Astor() throws DevFailed
	{
		t0 = System.currentTimeMillis();

		initComponents();
		AstorUtil.getInstance().initIcons();
		customizeMenu();

		setTitle("TANGO Manager - " + revNumber);
		buildTree();
		ImageIcon icon = new ImageIcon(
			getClass().getResource(img_path+"tango_icon.jpg"));
		setIconImage(icon.getImage());

		bottomPanel.setVisible(AstorUtil.getCtrlBtn());
		centerWindow();

        try {
            tango_host = ApiUtil.get_db_obj().get_tango_host();
        }
        catch(DevFailed e){ /* do nothing */ }
		
		//	There is some problem between environement and change
		changeTgHostBtn.setVisible(false);
	}
//===========================================================
/**
 *	Move the window to the center of the screen
 */
//===========================================================
	public void centerWindow() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension scrsize = toolkit.getScreenSize();
		Dimension appsize = getSize();
		Point	p = new Point();
		p.x = (scrsize.width  - appsize.width)/2;
		p.y = (scrsize.height - appsize.height)/2;
		setLocation(p);
	}
	//======================================================================
	//======================================================================
	private void buildTree() throws DevFailed
	{
		//	Build Splash Screen
		String	title = "Astor (TANGO Manager)";
		int end = revNumber.indexOf("-");
		if (end>0)
			title += " - " + revNumber.substring(0, end).trim();

		//	Create a splash window.
		JSmoothProgressBar myBar = new JSmoothProgressBar();
		myBar.setStringPainted(true);
		myBar.setBackground(Color.lightGray);
		myBar.setProgressBarColors(Color.gray, Color.gray, Color.gray);

		ImageIcon icon = new ImageIcon(
			getClass().getResource(img_path+"TangoCollaboration.jpg"));
		Splash	splash = new Splash(icon, Color.black, myBar);
		splash.setTitle(title);
		splash.setMessage("Starting....");
		splash.setVisible(true);

		try {
			//	Stop threads if already started (updating tree)
			if (tree!=null) {
				tree.stopThreads();
				scrollPane.remove(tree);
				remove(scrollPane);
			}

 	 		//	Build tree and start threads to update tree
			tree = new AstorTree(this, true, splash);
			scrollPane = new JScrollPane();
			scrollPane.setPreferredSize(AstorUtil.getPreferredSize());
			scrollPane.setViewportView (tree);
			getContentPane().add(scrollPane, BorderLayout.CENTER);
			expanded = false;
			pack();

            //  Access control management
            manageAccessControlMenu(tree.isAccessControlled());
		}
		catch (DevFailed e) {
			splash.setVisible(false);
			throw e;
		}
	}
	//======================================================================
	//======================================================================
	Dimension getTreeSize()
	{
		//return scrollPane.getSize();
		return scrollPane.getPreferredSize();
	}
	//======================================================================
	//======================================================================
	void setTreeSize(Dimension d)
	{
		scrollPane.setPreferredSize(d);
		pack();
	}
	//======================================================================
	//======================================================================
	private void customizeMenu()
	{
		//	File menu
		fileMenu.setMnemonic ('F');
		exitBtn.setMnemonic ('E');
		exitBtn.setAccelerator(KeyStroke.getKeyStroke('Q', Event.CTRL_MASK));

        ctrlPreferenceBtn.setMnemonic ('P');
		ctrlPreferenceBtn.setAccelerator(KeyStroke.getKeyStroke('P', Event.CTRL_MASK));
		String	s = System.getProperty("NO_PREF");
		if (s!=null && s.toLowerCase().equals("true"))
			ctrlPreferenceBtn.setEnabled(false);

        changeTgHostBtn.setMnemonic ('T');
		changeTgHostBtn.setAccelerator(KeyStroke.getKeyStroke('T', Event.CTRL_MASK));

        //	View menu
		viewMenu.setMnemonic ('V');
		newBranchBtn.setMnemonic ('N');
		newBranchBtn.setAccelerator(KeyStroke.getKeyStroke('N', Event.CTRL_MASK));

		refreshBtn.setMnemonic ('U');
		refreshBtn.setAccelerator(KeyStroke.getKeyStroke('U', Event.CTRL_MASK));

		deviceBrowserBtn.setMnemonic ('B');
		deviceBrowserBtn.setAccelerator(KeyStroke.getKeyStroke('B', Event.CTRL_MASK));

		expandBtn.setMnemonic ('E');
		expandBtn.setAccelerator(KeyStroke.getKeyStroke('E', Event.CTRL_MASK));

		//	Search menu
		toolsMenu.setMnemonic ('T');
		multiServersCmdItem.setAccelerator(KeyStroke.getKeyStroke('M', Event.CTRL_MASK));
		jiveMenuItem.setAccelerator(KeyStroke.getKeyStroke('J',        Event.CTRL_MASK));
		logviewerMenuItem.setAccelerator(KeyStroke.getKeyStroke('L',   Event.CTRL_MASK));

		//	Command menu
		cmdMenu.setMnemonic ('C');

		newHostBtn.setAccelerator(KeyStroke.getKeyStroke('H', Event.CTRL_MASK));

        nb_def_tools = toolsMenu.getItemCount();
        buildToolsItems();
        buildAdditionnalHelps();

        expandBtn.setVisible(false);
    }


    //======================================================================
    //======================================================================
    private void manageAccessControlMenu(boolean isAccessControlled)
    {
        if (isAccessControlled)  {
            accessControlBtn.setMnemonic ('A');
            accessControlBtn.setAccelerator(KeyStroke.getKeyStroke('A', Event.CTRL_MASK));
            accessControlBtn.setVisible(true);
            System.out.println("AccessControl is active");
        }
        else {   //  Service does not exist !
            accessControlBtn.setVisible(false);
        }
    }
    //======================================================================
    //======================================================================
    private String[]  htmlHelps = null;
    private void buildAdditionnalHelps()
    {
        htmlHelps = AstorUtil.getHtmlHelps();
        if (htmlHelps==null)    return;
        if (htmlHelps.length==0)    return;

        helpMenu.add(new JSeparator());

        for (int i=0 ; i<htmlHelps.length/2 ; i++)
        {
            JMenuItem	mi = new JMenuItem();
            mi.setText(htmlHelps[2*i]);
            mi.addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    htmlHelpsItemActionPerformed(evt);
                }
            });
            helpMenu.add(mi);
        }
    }

    //======================================================================
    //======================================================================
    private void htmlHelpsItemActionPerformed(java.awt.event.ActionEvent evt)
    {
        String  str = evt.getActionCommand();
        String  cmd = null;
        for (int i=0 ; i<htmlHelps.length ; i++)
            if (str.equals(htmlHelps[i]))
                if (i<htmlHelps.length-1)
                    cmd = htmlHelps[i+1];
        if (cmd==null)
            Utils.popupError(this, "No command found for item  \'" + str +"\'");
        System.out.println(cmd);

		AstorUtil.showInHtmBrowser(cmd);
    }
    //======================================================================
   /**
    * Remove Optional tools if any and add new ones.
    */
    //======================================================================
    private void buildToolsItems()
    {
        //  Remove items if any
        for (int i=toolsMenu.getItemCount() ; i>nb_def_tools ; i--)
        {
            JMenuItem   mi = toolsMenu.getItem(i-1);
            mi.removeActionListener(tools_al.get(i-nb_def_tools-1));
            toolsMenu.remove(i-1);
        }
        app_tools.clear();
        tools_al.clear();

		//	Add JMenuItem for tools
		String[]	str_tools = AstorUtil.getTools();
		if (str_tools!=null)
		{
			for (int i=0 ; i<str_tools.length ; i+=2)
			{
				OneTool	t = new OneTool(str_tools[i], str_tools[i+1]);
				app_tools.add(t);
				JMenuItem	mi = new JMenuItem();
				mi.setText(t.name);
                ActionListener  al;
				mi.addActionListener(al=new ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						toolsItemActionPerformed(evt);
					}
                });
                tools_al.add(al);
				toolsMenu.add(mi);
			}
		}
	}

    private int     nb_def_tools = 1;
	private Vector<OneTool>         app_tools = new Vector<OneTool>();
    private Vector<ActionListener>  tools_al  = new Vector<ActionListener>();
	//======================================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//======================================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bottomPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        allHostsControledBtn = new javax.swing.JToggleButton();
        stopHostsControledBtn = new javax.swing.JToggleButton();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        changeTgHostBtn = new javax.swing.JMenuItem();
        ctrlPreferenceBtn = new javax.swing.JMenuItem();
        exitBtn = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        deviceBrowserBtn = new javax.swing.JMenuItem();
        refreshBtn = new javax.swing.JMenuItem();
        expandBtn = new javax.swing.JMenuItem();
        javax.swing.JMenuItem startupErrorBtn = new javax.swing.JMenuItem();
        cmdMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem startServersBtn = new javax.swing.JMenuItem();
        javax.swing.JMenuItem stopServersBtn = new javax.swing.JMenuItem();
        newHostBtn = new javax.swing.JMenuItem();
        newBranchBtn = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        javax.swing.JMenu findMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem findHostItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem findObjectByFilterItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem tangoStatBtn = new javax.swing.JMenuItem();
        multiServersCmdItem = new javax.swing.JMenuItem();
        jiveMenuItem = new javax.swing.JMenuItem();
        accessControlBtn = new javax.swing.JMenuItem();
        javax.swing.JMenuItem statisticsBtn = new javax.swing.JMenuItem();
        logviewerMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        principleBtn = new javax.swing.JMenuItem();
        distributionBtn = new javax.swing.JMenuItem();
        stateIconsBtn = new javax.swing.JMenuItem();
        tangorbBtn = new javax.swing.JMenuItem();
        starterEventsItem = new javax.swing.JMenuItem();
        starterNoEventsItem = new javax.swing.JMenuItem();
        releaseNoteBtn = new javax.swing.JMenuItem();
        aboutBtn = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jLabel1.setText("Control  :   ");
        bottomPanel.add(jLabel1);

        allHostsControledBtn.setFont(new java.awt.Font("Dialog", 1, 10));
        allHostsControledBtn.setText("  All  hosts  ");
        allHostsControledBtn.setToolTipText("Start Control on All Hosts");
        allHostsControledBtn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        allHostsControledBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        allHostsControledBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allHostsControledBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(allHostsControledBtn);

        stopHostsControledBtn.setFont(new java.awt.Font("Dialog", 1, 10));
        stopHostsControledBtn.setText("    None   ");
        stopHostsControledBtn.setToolTipText("Stop Control on All Hosts");
        stopHostsControledBtn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        stopHostsControledBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        stopHostsControledBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopHostsControledBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(stopHostsControledBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        fileMenu.setText("File");

        changeTgHostBtn.setText("Change Tango Host");
        changeTgHostBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeTgHostBtnActionPerformed(evt);
            }
        });
        fileMenu.add(changeTgHostBtn);

        ctrlPreferenceBtn.setText("Ctrl System Preferences");
        ctrlPreferenceBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ctrlPreferenceBtnActionPerformed(evt);
            }
        });
        fileMenu.add(ctrlPreferenceBtn);

        exitBtn.setText("Exit");
        exitBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitBtnActionPerformed(evt);
            }
        });
        fileMenu.add(exitBtn);

        menuBar.add(fileMenu);

        viewMenu.setText("View");

        deviceBrowserBtn.setText("Event Manager");
        deviceBrowserBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deviceBrowserBtnActionPerformed(evt);
            }
        });
        viewMenu.add(deviceBrowserBtn);

        refreshBtn.setText("Update Tree from database");
        refreshBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshBtnActionPerformed(evt);
            }
        });
        viewMenu.add(refreshBtn);

        expandBtn.setText("Expand Tree");
        expandBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expandBtnActionPerformed(evt);
            }
        });
        viewMenu.add(expandBtn);

        startupErrorBtn.setText("Startup Errors");
        startupErrorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startupErrorBtnActionPerformed(evt);
            }
        });
        viewMenu.add(startupErrorBtn);

        menuBar.add(viewMenu);

        cmdMenu.setText("Command");

        startServersBtn.setText("Start All Controlled Servers");
        startServersBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startServersBtnActionPerformed(evt);
            }
        });
        cmdMenu.add(startServersBtn);

        stopServersBtn.setText("Stop All Controlled Servers");
        stopServersBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopServersBtnActionPerformed(evt);
            }
        });
        cmdMenu.add(stopServersBtn);

        newHostBtn.setText("Add a New Host");
        newHostBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newHostBtnActionPerformed(evt);
            }
        });
        cmdMenu.add(newHostBtn);

        newBranchBtn.setText("Add a New Branch");
        newBranchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newBranchBtnActionPerformed(evt);
            }
        });
        cmdMenu.add(newBranchBtn);

        menuBar.add(cmdMenu);

        toolsMenu.setText("Tools");

        findMenu.setText("Find");

        findHostItem.setText("Host by Name");
        findHostItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findHostItemActionPerformed(evt);
            }
        });
        findMenu.add(findHostItem);

        findObjectByFilterItem.setText("Device/Server/Class by Filter");
        findObjectByFilterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findObjectByFilterItemActionPerformed(evt);
            }
        });
        findMenu.add(findObjectByFilterItem);

        toolsMenu.add(findMenu);

        tangoStatBtn.setText("Ctrl System Info");
        tangoStatBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tangoStatBtnActionPerformed(evt);
            }
        });
        toolsMenu.add(tangoStatBtn);

        multiServersCmdItem.setText("Multi Servers Command");
        multiServersCmdItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multiServersCmdItemActionPerformed(evt);
            }
        });
        toolsMenu.add(multiServersCmdItem);

        jiveMenuItem.setText("Jive");
        jiveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jiveMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(jiveMenuItem);

        accessControlBtn.setText("Access Control");
        accessControlBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accessControlBtnActionPerformed(evt);
            }
        });
        toolsMenu.add(accessControlBtn);

        statisticsBtn.setText("Server Statistics");
        statisticsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statisticsBtnActionPerformed(evt);
            }
        });
        toolsMenu.add(statisticsBtn);

        logviewerMenuItem.setText("LogViewer");
        logviewerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logviewerMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(logviewerMenuItem);

        menuBar.add(toolsMenu);

        helpMenu.setText("Help");

        principleBtn.setText("Principle");
        principleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });
        helpMenu.add(principleBtn);

        distributionBtn.setText("Servers Distribution");
        distributionBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });
        helpMenu.add(distributionBtn);

        stateIconsBtn.setText("State Icons");
        stateIconsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });
        helpMenu.add(stateIconsBtn);

        tangorbBtn.setText("TangORB revision");
        tangorbBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });
        helpMenu.add(tangorbBtn);

        starterEventsItem.setText("Hosts Controlled on Events");
        starterEventsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });
        helpMenu.add(starterEventsItem);

        starterNoEventsItem.setText("Hosts Controlled on Polling");
        starterNoEventsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });
        helpMenu.add(starterNoEventsItem);

        releaseNoteBtn.setText("Release Note");
        releaseNoteBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });
        helpMenu.add(releaseNoteBtn);

        aboutBtn.setText("About");
        aboutBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });
        helpMenu.add(aboutBtn);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //======================================================================
    //======================================================================
    @SuppressWarnings({"UnusedDeclaration"})
	private void ctrlPreferenceBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ctrlPreferenceBtnActionPerformed
		new PreferenceDialog(this).setVisible(true);

    }//GEN-LAST:event_ctrlPreferenceBtnActionPerformed

    //======================================================================
    //======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
    private void startupErrorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startupErrorBtnActionPerformed
        
		if (tree.subscribeErrWindow==null)
			Utils.popupMessage(this, "No error at startup.");
		else
			tree.subscribeErrWindow.setVisible(true);
    }//GEN-LAST:event_startupErrorBtnActionPerformed

    //======================================================================
    //======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
    private void accessControlBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accessControlBtnActionPerformed
        tree.startTACpanel();
    }//GEN-LAST:event_accessControlBtnActionPerformed

    //======================================================================
    //======================================================================
    private Selector    tango_host_selector = null;
	@SuppressWarnings({"UnusedDeclaration"})
    private void changeTgHostBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeTgHostBtnActionPerformed
        try{
            String      tmp_tgh;
            String[]    known_tgh;
            if (tango_host_selector==null)  {
                known_tgh = AstorUtil.getKnownTangoHosts();
                tango_host_selector = new Selector(this, "Tango Host  (e.g.  hal:2001)", known_tgh, tango_host);
                ATKGraphicsUtils.centerDialog(tango_host_selector);
            }
            tmp_tgh = tango_host_selector.showDialog();

            if (tmp_tgh==null || tmp_tgh.length()==0)
                return;

            //	Check if connection OK
            String[]	tgh_arr = tmp_tgh.split(":");
            if (tgh_arr.length!=2)  {
                Utils.popupError(this, "Input syntax error\n" + tmp_tgh + "\n is not a valid TANGO_HOST");
                return;
            }
            ApiUtil.get_db_obj(tgh_arr[0], tgh_arr[1]);

            if (tango_host.equals(tmp_tgh))
                return;

            tango_host = tmp_tgh;

            //	Close all host info dialogs
            tree.hostDialogs.close();
            tree.hostDialogs.clear();
            if (dev_browser!=null)  {
                dev_browser.setVisible(false);
                dev_browser = null;
            }
            //  Change the Tango host and rebuid tree
            AstorUtil.setTangoHost(tango_host);
            ApiUtil.change_db_obj(tgh_arr[0], tgh_arr[1]);
			AstorUtil.readAstorProperties();//	could have changed with new Tango Host

			//	Method has been deprecated
			//fr.esrf.TangoApi.events.EventConsumer.create().updateDatabaseObject();

            buildTree();

            //  Re-build tools MenuItems
            buildToolsItems();
        }
        catch(DevFailed e) {
            ErrorPane.showErrorMessage(this,
				"Cannot change TANGO_HOST", e);
        }
    }//GEN-LAST:event_changeTgHostBtnActionPerformed

	//======================================================================
	//======================================================================
	private fr.esrf.logviewer.Main	logviewer = null;
	@SuppressWarnings({"UnusedDeclaration"})
	private void logviewerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logviewerMenuItemActionPerformed
		if (logviewer==null)
			logviewer = new fr.esrf.logviewer.Main(this);
		logviewer.setVisible(true);
		logviewer.toFront();
	}//GEN-LAST:event_logviewerMenuItemActionPerformed

	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
	private void jiveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jiveMenuItemActionPerformed
		tree.displayJiveAppli();
	}//GEN-LAST:event_jiveMenuItemActionPerformed

	//======================================================================
	//======================================================================
	static private String	searched_host = "";
	@SuppressWarnings({"UnusedDeclaration"})
	private void findHostItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findHostItemActionPerformed

		//	Ask for host's name
		String	hostname = (String) JOptionPane.showInputDialog(this,
												"Host Name ?",
												"Input Dialog",
												JOptionPane.INFORMATION_MESSAGE,
												null, null, searched_host);
        /*
        String hostname = InputDialog.getInput(this, "Host name ?", tree.getHostList());
        */
		//	if host has been typed,
		//	select it on tree and open control panel
		if (hostname!=null)
		{
			tree.setSelectionPath(hostname);
			tree.displayHostInfo();
			searched_host = hostname;
		}

	}//GEN-LAST:event_findHostItemActionPerformed
	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
	private void tangoStatBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tangoStatBtnActionPerformed

		//	Ask to confirm
		if (JOptionPane.showConfirmDialog(this,
				"The System Information needs to browse Database\n"+
				"        and it could take a long time !\n\n" +
				"Start it any way ?",
				"Confirm Dialog",
				JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
					new DeviceTreeDialog(this);
	}//GEN-LAST:event_tangoStatBtnActionPerformed

	//======================================================================
	/**
	 *	Start tools found in Astor/Tools property
     * @param evt mouse event
	 */
	//======================================================================
	@SuppressWarnings({"ConstantConditions"})
    private void toolsItemActionPerformed(java.awt.event.ActionEvent evt) {
 		String  name = evt.getActionCommand();
        for (OneTool app : app_tools) {
            if (app.name.equals(name)) {
                try {
                    System.out.println("Starting " + app.classname);

                    //	Check if tool is already instancied.
                    if (app.jframe == null) {
                        //	Retrieve class name
                        Class cl = Class.forName(app.classname);

                        //	And build object
                        Class[] param = new Class[1];
                        param[0] = JFrame.class;
                        Constructor contructor = cl.getConstructor(param);

                        // ----------------- Java 5 -----------------
                        JFrame jf = (JFrame) contructor.newInstance(this);
                        //JFrame	jf = (JFrame)contructor.newInstance(new Object[] { this });
                        app.setJFrame(jf);
                    }
                    app.jframe.setVisible(true);
                }
                catch (Exception e) {
                    ErrorPane.showErrorMessage(this, null, e);
                }
            }
        }
	}
	//======================================================================
	//======================================================================
	void removeHost(String hostname)
	{
		if (JOptionPane.showConfirmDialog(this,
				"Are you sure to want to remove " + hostname,
				"Confirm Dialog",
				JOptionPane.YES_NO_OPTION)!=JOptionPane.OK_OPTION)
			return;

		String	servname = "Starter/" + hostname;
		String	devadmin = "dserver/" + servname;
		String	devname  = starterDeviceHeader + hostname;

		//	Ask to confirm
		try
		{
			//	Remove properties
			DeviceProxy	dev = new 	DeviceProxy(devname);
			dev.delete_property("StarteDsPath");
            dev.delete_property(collec_property);
            dev.delete_property(usage_property);
            dev.delete_property("UseEvents");

			//	Remove devices and server
			Database	db = ApiUtil.get_db_obj(AstorUtil.getTangoHost());
            db.delete_server(servname);
			db.delete_device(devname);
			db.delete_device(devadmin);

			JOptionPane.showMessageDialog(this,
								hostname + " has been removed !",
								"Command Done",
								JOptionPane.INFORMATION_MESSAGE);
		}
		catch (DevFailed e)
		{
            ErrorPane.showErrorMessage(this,
				"Cannot remove host", e);
		}
		try
		{
			buildTree();
		}
		catch (DevFailed e)
		{
            ErrorPane.showErrorMessage(this, null, e);
		}
	}
	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
	private void newHostBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newHostBtnActionPerformed
		addNewHost(null);
	}//GEN-LAST:event_newHostBtnActionPerformed
	//======================================================================
	//======================================================================
	void addNewHost(TangoHost h)
	{
		NewStarterDialog	dialog
			= new NewStarterDialog(this, h, tree.getCollectionList(), tree.hosts, true);
		dialog.setVisible(true);
		if (dialog.getValue()==JOptionPane.OK_OPTION)
		{
			try
			{
				buildTree();
			}
			catch (DevFailed e)
			{
        		ErrorPane.showErrorMessage(this, null, e);
			}
		}
	}
	//======================================================================
	//======================================================================
	void editHostProperties(TangoHost h)
	{
		NewStarterDialog	dialog
			= new NewStarterDialog(this, h, tree.getCollectionList(), tree.hosts, false);
		dialog.setVisible(true);
	}
	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
	private void stopServersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopServersBtnActionPerformed
		new ServerCmdThread(this, tree.hosts, StopAllServers).start();
	}//GEN-LAST:event_stopServersBtnActionPerformed

	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
	private void startServersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startServersBtnActionPerformed
		new ServerCmdThread(this, tree.hosts, StartAllServers).start();
	}//GEN-LAST:event_startServersBtnActionPerformed

	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
	private void newBranchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newBranchBtnActionPerformed

		String	branch_name = (String)JOptionPane.showInputDialog(this,
								"New Branch Name",
								"Input Dialog",
								JOptionPane.INFORMATION_MESSAGE,
								null, null, "");
		if (branch_name!=null)
		{
			tree.addBranch(branch_name);
		}
	}//GEN-LAST:event_newBranchBtnActionPerformed

	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
	private void helpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpActionPerformed
 		String  item = evt.getActionCommand();

		if (item.equals(principleBtn.getText()))
			Utils.popupMessage(this, "", img_path + "principle.gif");
		else
		if (item.equals(distributionBtn.getText()))
			new HostsScanThread(this, tree.hosts).start();
		else
		if (item.equals(stateIconsBtn.getText()))
			Utils.popupMessage(this, "", img_path + "astor_state_icons.jpg");
		else
		if (item.equals(releaseNoteBtn.getText()))
		{
            new PopupHtml(this).show(ReleaseNote.str);
        }
		else
		if (item.equals(tangorbBtn.getText()))
		{
			TangORBversion	tangorb;
			try {
				tangorb = new TangORBversion();
			}
			catch(Exception e) {
                ErrorPane.showErrorMessage(this,
                    "Cannot check TangORB revision", e);
                return;
            }
			String	message = tangorb.jarfile + ":\n\n" + tangorb;
			PopupText txt = new PopupText(this, true);
			txt.setFont(new java.awt.Font("Courier", 1, 14));
			AstorUtil.centerDialog(txt, this);
			txt.show(message);
		}
		else
		if (item.equals(aboutBtn.getText()))
		{
			String	message =
				"           Astor  (Tango Manager) \n\n"+
				"This programme is used to control, start and stop\n"+
				"           the TANGO device servers. \n\n" +
				revNumber +
				"\n\n"+
				"Pascal Verdier - Software Engineering Group - ESRF";
			Utils.popupMessage(this, message, img_path + "tango_icon.jpg");
		}
		else
		if (item.equals(starterEventsItem.getText()))
		{
			Vector<String>	v = new Vector<String>();
			for (TangoHost host : tree.hosts)
				if (host.use_events)
					v.add(host.getName());

			String		title;
			String[]	hostnames = null;
			if (v.size()==0)
				title = "There is no host controlled on events !";
			else
			if (v.size()==tree.hosts.length)
				title = "All hosts are controlled on events !";
			else
			{
				title = "On " + tree.hosts.length + " hosts,\n" +
						v.size() +"  are controlled on events :";
				hostnames = new String[v.size()];
				for (int i=0 ; i<v.size() ; i++)
					hostnames[i] = v.get(i);
			}
			if (hostnames==null)
				Utils.popupMessage(this, title);
			else
				new PopupText(this, true).show(title, hostnames, 300, 400);
		}
		else
		if (item.equals(starterNoEventsItem.getText()))
		{
			Vector<String>	v = new Vector<String>();
			for (TangoHost host : tree.hosts)
				if (!host.use_events)
					v.add(host.getName());

			String		title;
			String[]	hostnames = null;
			if (v.size()==0)
				title = "There is no host controlled on polling !";
			else
			if (v.size()==tree.hosts.length)
				title = "All hosts are controlled on polling !";
			else
			{
				title = "On " + tree.hosts.length + " hosts,\n" +
						v.size() +"  are controlled on polling :";
				hostnames = new String[v.size()];
				for (int i=0 ; i<v.size() ; i++)
					hostnames[i] = v.get(i);
			}
			if (hostnames==null)
				Utils.popupMessage(this, title);
			else
				new PopupText(this, true).show(title, hostnames, 300, 400);
		}
		else
			Utils.popupMessage(this, "Not implemented yet !");
	}//GEN-LAST:event_helpActionPerformed

	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
	private void stopHostsControledBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopHostsControledBtnActionPerformed
		if (tree!=null)
		{
			for (TangoHost host : tree.hosts)
			{
				host.do_polling = false;
				tree.hostDialogs.close(host);
			}
			stopHostsControledBtn.setSelected(false);
		}
	}//GEN-LAST:event_stopHostsControledBtnActionPerformed

	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
	private void allHostsControledBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allHostsControledBtnActionPerformed
		for (TangoHost host : tree.hosts)
			host.do_polling = true;
		allHostsControledBtn.setSelected(false);
	}//GEN-LAST:event_allHostsControledBtnActionPerformed

	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
	private void expandBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandBtnActionPerformed
		expanded = !expanded;
		tree.expand(expanded);
		if (expanded)
			expandBtn.setText("Collapse Tree");
		else
			expandBtn.setText("Expand Tree");
	}//GEN-LAST:event_expandBtnActionPerformed
	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
	private void deviceBrowserBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deviceBrowserBtnActionPerformed
		if (dev_browser==null)
			dev_browser = new DevBrowser(this);
		dev_browser.setVisible(true);
	}//GEN-LAST:event_deviceBrowserBtnActionPerformed
	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
    private void refreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
		try {
			buildTree();
		}
		catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
		}
    }//GEN-LAST:event_refreshBtnActionPerformed

	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
    private void exitBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitBtnActionPerformed
		doExit();
    }//GEN-LAST:event_exitBtnActionPerformed

	//======================================================================
	//======================================================================
	@SuppressWarnings({"UnusedDeclaration"})
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
		doExit();
	}//GEN-LAST:event_exitForm
	//======================================================================
	//======================================================================
	private WideSearchDialog	wide_search_dlg = null;
	@SuppressWarnings({"UnusedDeclaration"})
	private void findObjectByFilterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findObjectByFilterItemActionPerformed

		if (wide_search_dlg==null)
			wide_search_dlg = new WideSearchDialog(this);
		wide_search_dlg.setVisible(true);

	}//GEN-LAST:event_findObjectByFilterItemActionPerformed

    //======================================================================
    //======================================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void multiServersCmdItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiServersCmdItemActionPerformed
        try {
            if (multiServerCommand==null) {
                multiServerCommand = new MultiServerCommand(this);
            }
            multiServerCommand.setVisible(true);
        }
        catch(DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_multiServersCmdItemActionPerformed

    //======================================================================
    //======================================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void statisticsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statisticsBtnActionPerformed
        StatisticsPanel statisticsPanel = new StatisticsPanel(this);
        statisticsPanel.readAndDisplayStatistics(null); //  On all Servers
        statisticsPanel.setVisible(true);
    }//GEN-LAST:event_statisticsBtnActionPerformed
	//======================================================================
	//======================================================================
	@SuppressWarnings({"ConstantConditions"})
    private void stopThreads()
	{
        System.out.println("Astor exiting....");
        //	Stop all host controled
        if (tree!=null && tree.hosts!=null)  {
			for (TangoHost host : tree.hosts) {
				//	Display a little timer during unsubscribe
				host.stopThread();
				if (host.use_events) {
					try { Thread.sleep(20); }
					catch (Exception e) {/* Do nothing */}
				}
			}
        }
        System.out.println(" ");
	}
	//======================================================================
	//======================================================================
	public void doExit()
	{
        if (dev_browser!=null && dev_browser.isVisible())
            setVisible(false);
        else
        {
            setVisible(false);
			stopThreads();
            System.exit(0);
        }
	}

	//======================================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutBtn;
    private javax.swing.JMenuItem accessControlBtn;
    private javax.swing.JToggleButton allHostsControledBtn;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JMenuItem changeTgHostBtn;
    private javax.swing.JMenu cmdMenu;
    private javax.swing.JMenuItem ctrlPreferenceBtn;
    private javax.swing.JMenuItem deviceBrowserBtn;
    private javax.swing.JMenuItem distributionBtn;
    private javax.swing.JMenuItem exitBtn;
    private javax.swing.JMenuItem expandBtn;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem jiveMenuItem;
    private javax.swing.JMenuItem logviewerMenuItem;
    private javax.swing.JMenuItem multiServersCmdItem;
    private javax.swing.JMenuItem newBranchBtn;
    private javax.swing.JMenuItem newHostBtn;
    private javax.swing.JMenuItem principleBtn;
    private javax.swing.JMenuItem refreshBtn;
    private javax.swing.JMenuItem releaseNoteBtn;
    private javax.swing.JMenuItem starterEventsItem;
    private javax.swing.JMenuItem starterNoEventsItem;
    private javax.swing.JMenuItem stateIconsBtn;
    private javax.swing.JToggleButton stopHostsControledBtn;
    private javax.swing.JMenuItem tangorbBtn;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
	//======================================================================

	//======================================================================
	/**
	 * @param args the command line arguments
	 */
	//======================================================================
    public static void main(String args[]) {

		//	Check if line command
		if (args.length>0)
		{
			try
			{
				new AstorCmdLine(args);
			}
			catch(DevFailed e)
			{
				Except.print_exception(e);
			}
			catch(Exception e)
			{
				System.out.println(e);
				e.printStackTrace();
			}
			System.exit(0);
		}
		//	Else start application


		long	t0 = System.currentTimeMillis();
		//	First time, Open a simple Tango window
		//noinspection ErrorNotRethrown
		try
		{
			Astor	astor = new Astor();
			astor.setVisible(true);
			Astor.displayed = true;
		}
		catch(DevFailed e)
		{
			System.out.println(e);
			if (e.errors[0].desc.indexOf("Controlled access service defined in Db but unreachable")>0)
				e.errors[0].desc = "Controlled access service defined in Db but unreachable\n" +
						"Astor cannot be configured from database !";
			
			ErrorPane.showErrorMessage(new JFrame(), null, e);
			System.exit(-1);
		}
		catch(java.lang.InternalError e)
		{
			System.out.println(e);
		}
		 catch(java.awt.HeadlessException e)
		{
			System.out.println(e);
		}
		long	t1 = System.currentTimeMillis();
		System.out.println("Build  GUI :" + (t1-t0) + " ms");
	}








    //===============================================================
    /**
     *	A thread class to execute a hosts scan
     */
    //===============================================================
    class HostsScanThread extends Thread
    {
        private JFrame		parent;
		private TangoHost[]	hosts;
        //===============================================================
        HostsScanThread(JFrame parent, TangoHost[] hosts)
        {
            this.parent = parent;
			this.hosts  = hosts;
		}
       //===============================================================
        public void run()
        {
			String[][]	list = new String[hosts.length][];
            String  message = "Scanning hosts...";
            Monitor monitor = new Monitor(parent, message);
			double	ratio = 0.01;
            monitor.setProgressValue(ratio, "Starting...");
			try { sleep(500); } catch(InterruptedException e) { /** */}
			try
			{
				int	nb_serv = 0;
				for (int i=0 ; i<hosts.length ; i++)
				{
					ratio = (1+1.0*i)/hosts.length;
                	monitor.setProgressValue(ratio,
							"Reading " + hosts[i].getName());

					String[]	servers = hosts[i].getServerAttribute();
					
					list[i] = new String[2];
					list[i][0] = hosts[i].getName();
					list[i][1] = ""+servers.length;
					nb_serv += servers.length;
				}
				
				//	Format results
				String	title = nb_serv + " servers   on " +
								hosts.length + " hosts";
				String[] cols = new String[] { "Names", "Nb Servers" };
				
				PopupTable	table = new PopupTable(parent, title, cols, list);
				table.setColumnWidth(new int[] { 200, 100 });
				table.setVisible(true);
			}
			catch(DevFailed e)
			{
            	ErrorPane.showErrorMessage(parent, "", e);
			}
		}
	}
}
