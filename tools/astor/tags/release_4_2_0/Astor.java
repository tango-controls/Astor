//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the TempClass class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.31  2005/12/08 12:39:27  pascal_verdier
// Add release note display in help menu.
//
// Revision 3.30  2005/12/01 10:00:23  pascal_verdier
// Change TANGO_HOST added (needs TangORB-4.7.7 or later).
//
// Revision 3.29  2005/11/24 12:24:57  pascal_verdier
// DevBrowser utility added.
// MkStarter utility added.
//
// Revision 3.28  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.27  2005/10/20 13:24:49  pascal_verdier
// Screen position management has been changed.
//
// Revision 3.26  2005/10/17 14:14:24  pascal_verdier
// Search host by name added.
//
// Revision 3.25  2005/09/15 08:26:36  pascal_verdier
// Server architecture display addded.
//
// Revision 3.24  2005/08/30 08:05:25  pascal_verdier
// Management of two TANGO HOST added.
//
// Revision 3.23  2005/06/02 09:02:36  pascal_verdier
// Minor changes.
//
// Revision 3.22  2005/04/25 08:55:35  pascal_verdier
// Start/Stop servers from shell command line added.
//
// Revision 3.21  2005/04/22 09:30:45  pascal_verdier
// Use events management in starter properies dialog added.
//
// Revision 3.20  2005/03/15 10:22:30  pascal_verdier
// Sort servers before creating panel buttons.
//
// Revision 3.19  2005/03/11 14:07:53  pascal_verdier
// Pathes have been modified.
//
// Revision 3.18  2005/02/16 13:41:05  pascal_verdier
// Add controlled servers info in DeviceTree class.
//
// Revision 3.17  2005/02/10 15:38:18  pascal_verdier
// Event subscritions have been serialized.
//
// Revision 3.16  2005/02/03 13:31:58  pascal_verdier
// Display message if subscribe event failed.
// Display hosts using events (Starter/Astor).
//
// Revision 3.15  2005/01/18 08:48:20  pascal_verdier
// Tools menu added.
// Not controlled servers list added.
//
// Revision 3.14  2004/11/23 14:05:56  pascal_verdier
// Minor changes.
//
// Revision 3.13  2004/09/28 07:01:50  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.12  2004/07/09 07:21:25  pascal_verdier
// JAR revision and date management added in appli.
//
// Revision 3.11  2004/07/08 11:22:58  pascal_verdier
// First revision able to use events.
//
// Revision 3.10  2004/06/17 09:19:58  pascal_verdier
// Refresh performence problem solved by removing tool tips on JTree.
//
// Revision 3.9  2004/05/04 07:05:27  pascal_verdier
// Bug on notify daemon fixed.
// server reconection transparency added.
//
// Revision 3.8  2004/04/13 12:17:27  pascal_verdier
// DeviceTree class uses the new browsing database commands.
//
// Revision 3.7  2003/11/25 15:56:45  pascal_verdier
// Label on hosts added.
// Notifyd begin to be controled.
//
// Revision 3.6  2003/11/05 10:34:57  pascal_verdier
// Main Panel screen centering.
// Starter multi path added.
// little bugs fixed.
//
// Revision 3.5  2003/10/20 08:55:15  pascal_verdier
// Bug on tree popup menu position fixed.
//
// Revision 3.4  2003/09/08 12:21:36  pascal_verdier
// *** empty log message ***
//
// Revision 3.3  2003/09/08 11:05:28  pascal_verdier
// *** empty log message ***
//
// Revision 3.2  2003/07/22 14:35:20  pascal_verdier
// Minor bugs fixed.
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
// Revision 3.0  2003/06/04 12:37:52  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
// Revision 2.1  2003/06/04 12:33:12  pascal_verdier
// Main window uses now a Jtree to display hosts.
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;

/**
 *	This class is the Astor main panel
 *	containing the Jtree used to display hosts.
 *
 * @author  root
 */

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import admin.astor.tools.DevBrowser;

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
	private static String revNumber = "Release 4.2.0  -  Wed Jan 11 09:37:35 CET 2006";

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
	private JScrollPane	scrowllPane;
	/**
	 *	Dialog to display controlled servers tree.
	 */
	static CtrlServersDialog	ctrl_serv_d = null;
	/**
	 *	true if Astor is fully built and displayed.
	 */
	static boolean displayed = false;

    public static DevBrowser   dev_browser = null;
    private String tango_host = "";
	//======================================================================
    /**
	 *	Creates new form Astor
	 */
	//======================================================================
	public Astor()
	{

		initComponents();
		customizeMenu();

		setTitle("TANGO Manager - " + revNumber);
		buildTree();
		ImageIcon icon = new ImageIcon(
			getClass().getResource("/app_util/img/tango_icon.jpg"));
		setIconImage(icon.getImage());

		jPanel1.setVisible(AstorUtil.getCtrlBtn());
		centerWindow();
		checkTangORB();

        try {
            tango_host = ApiUtil.get_db_obj().get_tango_host();
        }
        catch(DevFailed e){}
	}
	//======================================================================
	//======================================================================
	private void checkTangORB()
	{
		//	Check the TangORB revision
		try
		{
			TangORBversion	tangorb = new TangORBversion();
			String	rev = "";
			rev += tangorb.api.charAt(0);
			rev += tangorb.api.charAt(2);
			rev += tangorb.api.charAt(4);
			int	revnum = Integer.parseInt(rev);


			if (revnum>500)
				//	A bug in TangORBversion (api and tango fields swapped)
				changeTgHostBtn.setVisible(false);  //  Revision with bug
			else
			if (revnum>476)
				changeTgHostBtn.setVisible(true);
			else
				changeTgHostBtn.setVisible(false);  //  too old
		}
		catch(DevFailed e)
		{
			Except.print_exception(e);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
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
	private void buildTree()
	{
		try
		{
			//	Stop threads if already started (updating tree)
			if (tree!=null)
			{
				tree.stopThreads();
				scrowllPane.remove(tree);
				remove(scrowllPane);
			}
 	 		//	Build tree and start threads to update tree
			tree = new AstorTree(this, true);
			scrowllPane = new JScrollPane();
			scrowllPane.setPreferredSize(new Dimension(340, 600));
			scrowllPane.setViewportView (tree);
			getContentPane().add(scrowllPane, BorderLayout.CENTER);
		}
		catch (DevFailed e)
		{
			app_util.PopupError.show(this, e);
		}
		expanded = false;
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
		//tangoStatBtn.setAccelerator(KeyStroke.getKeyStroke('I', Event.CTRL_MASK));
		jiveMenuItem.setAccelerator(KeyStroke.getKeyStroke('J', Event.CTRL_MASK));
		logviewerMenuItem.setAccelerator(KeyStroke.getKeyStroke('L', Event.CTRL_MASK));

		//	Command menu
		cmdMenu.setMnemonic ('C');

		newHostBtn.setAccelerator(KeyStroke.getKeyStroke('H', Event.CTRL_MASK));

		//	Not used any more
		serversItem.setVisible(false);
		serversItem.setAccelerator(KeyStroke.getKeyStroke('S', Event.CTRL_MASK));

		//	Add a button to search a host
		searchHostItem = new javax.swing.JMenuItem();
		searchHostItem.setText("Find Host by Name");
		searchHostItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				searchHostItemActionPerformed(evt);
			}
		});
		toolsMenu.add(searchHostItem, 0);

        nb_def_tools = toolsMenu.getItemCount();
        buildToolsItems();
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
            mi.removeActionListener((ActionListener)tools_al.get(i-nb_def_tools-1));
            toolsMenu.remove(i-1);
        }
        app_tools.clear();
        tools_al.clear();

		//	Add JMenuItem for tools
		String[]	str_tools= AstorUtil.getTools();
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
	private Vector  app_tools = new Vector();
    private Vector  tools_al  = new Vector();
	//======================================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//======================================================================
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        allHostsControledBtn = new javax.swing.JToggleButton();
        stopHostsControledBtn = new javax.swing.JToggleButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        changeTgHostBtn = new javax.swing.JMenuItem();
        exitBtn = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        deviceBrowserBtn = new javax.swing.JMenuItem();
        refreshBtn = new javax.swing.JMenuItem();
        expandBtn = new javax.swing.JMenuItem();
        cmdMenu = new javax.swing.JMenu();
        startServersBtn = new javax.swing.JMenuItem();
        stopServersBtn = new javax.swing.JMenuItem();
        newHostBtn = new javax.swing.JMenuItem();
        newBranchBtn = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        serversItem = new javax.swing.JMenuItem();
        tangoStatBtn = new javax.swing.JMenuItem();
        jiveMenuItem = new javax.swing.JMenuItem();
        logviewerMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        principleBtn = new javax.swing.JMenuItem();
        helpAppliBtn = new javax.swing.JMenuItem();
        helpStarterBtn = new javax.swing.JMenuItem();
        stateIconsBtn = new javax.swing.JMenuItem();
        tangorbBtn = new javax.swing.JMenuItem();
        starterEventsItem = new javax.swing.JMenuItem();
        releaseNoteBtn = new javax.swing.JMenuItem();
        aboutBtn = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jLabel1.setText("Control  :   ");
        jPanel1.add(jLabel1);

        allHostsControledBtn.setFont(new java.awt.Font("Dialog", 1, 10));
        allHostsControledBtn.setText("  All  hosts  ");
        allHostsControledBtn.setToolTipText("Start Control on All Hosts");
        allHostsControledBtn.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
        allHostsControledBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        allHostsControledBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allHostsControledBtnActionPerformed(evt);
            }
        });

        jPanel1.add(allHostsControledBtn);

        stopHostsControledBtn.setFont(new java.awt.Font("Dialog", 1, 10));
        stopHostsControledBtn.setText("    None   ");
        stopHostsControledBtn.setToolTipText("Stop Control on All Hosts");
        stopHostsControledBtn.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
        stopHostsControledBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        stopHostsControledBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopHostsControledBtnActionPerformed(evt);
            }
        });

        jPanel1.add(stopHostsControledBtn);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        fileMenu.setText("File");
        changeTgHostBtn.setText("Change Tango Host");
        changeTgHostBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeTgHostBtnActionPerformed(evt);
            }
        });

        fileMenu.add(changeTgHostBtn);

        exitBtn.setText("Exit");
        exitBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitBtnActionPerformed(evt);
            }
        });

        fileMenu.add(exitBtn);

        jMenuBar1.add(fileMenu);

        viewMenu.setText("View");
        deviceBrowserBtn.setText("Device Browser");
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

        jMenuBar1.add(viewMenu);

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

        jMenuBar1.add(cmdMenu);

        toolsMenu.setText("Tools");
        serversItem.setText("Controlled Servers");
        serversItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serversItemActionPerformed(evt);
            }
        });

        toolsMenu.add(serversItem);

        tangoStatBtn.setText("Ctrl System Info");
        tangoStatBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tangoStatBtnActionPerformed(evt);
            }
        });

        toolsMenu.add(tangoStatBtn);

        jiveMenuItem.setText("Jive");
        jiveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jiveMenuItemActionPerformed(evt);
            }
        });

        toolsMenu.add(jiveMenuItem);

        logviewerMenuItem.setText("LogViewer");
        logviewerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logviewerMenuItemActionPerformed(evt);
            }
        });

        toolsMenu.add(logviewerMenuItem);

        jMenuBar1.add(toolsMenu);

        helpMenu.setText("Help");
        principleBtn.setText("Principle");
        principleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });

        helpMenu.add(principleBtn);

        helpAppliBtn.setText("Application");
        helpAppliBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });

        helpMenu.add(helpAppliBtn);

        helpStarterBtn.setText("Starter Server");
        helpStarterBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });

        helpMenu.add(helpStarterBtn);

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

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    //======================================================================
    //======================================================================
    private Selector    tango_host_selector = null;
    private void changeTgHostBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeTgHostBtnActionPerformed
        try{
            String      tmp_tgh;
            String[]    known_tgh = null;
            if (tango_host_selector==null)
            {
                known_tgh = AstorUtil.getKnownTangoHosts();
                tango_host_selector = new Selector(this, "Tango Host  (e.g.  hal:2001)", known_tgh, tango_host);
                ATKGraphicsUtils.centerDialog(tango_host_selector);
            }
            tmp_tgh = tango_host_selector.showDialog();

            if (tmp_tgh==null || tmp_tgh.length()==0)
                return;

            //	Check if connection OK
            String[]	tgh_arr = tmp_tgh.split(":");
            if (tgh_arr.length!=2)
            {
                app_util.PopupError.show(this, "Input syntax error\n" + tmp_tgh + "\n is not a valid TANGO_HOST");
                return;
            }
            ApiUtil.get_db_obj(tgh_arr[0], tgh_arr[1]);

            if (tango_host.equals(tmp_tgh))
                return;

            tango_host = tmp_tgh;
            //	Close all host info dialogs
            tree.hostDialogs.close();
            tree.hostDialogs.clear();
            if (dev_browser!=null)
            {
                dev_browser.setVisible(false);
                dev_browser = null;
            }
            //  Change the Tango host and rebuid tree
            AstorUtil.setTangoHost(tango_host);
            ApiUtil.change_db_obj(tgh_arr[0], tgh_arr[1]);
            fr.esrf.TangoApi.events.EventConsumer.create().updateDatabaseObject();
            buildTree();

            //  Re-build tools MenuItems
            buildToolsItems();
        }
        catch(DevFailed e) {
            app_util.PopupError.show(this, e);
        }
    }//GEN-LAST:event_changeTgHostBtnActionPerformed

	//======================================================================
	//======================================================================
	private fr.esrf.logviewer.Main	logviewer = null;
	private void logviewerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logviewerMenuItemActionPerformed
		if (logviewer==null)
			logviewer = new fr.esrf.logviewer.Main(this);
		logviewer.setVisible(true);
		logviewer.toFront();
	}//GEN-LAST:event_logviewerMenuItemActionPerformed

	//======================================================================
	//======================================================================
	private void jiveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jiveMenuItemActionPerformed
		tree.displayDbaseInfo();
	}//GEN-LAST:event_jiveMenuItemActionPerformed

	//======================================================================
	//======================================================================
	static private String	searched_host = "";
	private void searchHostItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchHostItemActionPerformed

		//	Ask for host's name
		String	hostname = (String) JOptionPane.showInputDialog(this,
												"Host's Name ?",
												"Input Dialog",
												JOptionPane.INFORMATION_MESSAGE,
												null, null, searched_host);
		//	if host has been typed,
		//	select it on tree and open control panel
		if (hostname!=null)
		{
			tree.setSelectionPath(hostname);
			tree.displayHostInfo();
			searched_host = hostname;
		}

	}//GEN-LAST:event_searchHostItemActionPerformed
	//======================================================================
	//======================================================================
        private void tangoStatBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tangoStatBtnActionPerformed
		new DeviceTreeDialog(this);
        }//GEN-LAST:event_tangoStatBtnActionPerformed

	//======================================================================
	//======================================================================
	private void serversItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serversItemActionPerformed
		//	if already exist -> close it before new one.
		//	To do not have more than one active.
		if (ctrl_serv_d!=null)
			ctrl_serv_d.doClose();
		ctrl_serv_d = new CtrlServersDialog(this, tree.hosts);
	}//GEN-LAST:event_serversItemActionPerformed

	//======================================================================
	/**
	 *	Start tools found in Astor/Tools property
	 */
	//======================================================================
	private void toolsItemActionPerformed(java.awt.event.ActionEvent evt) {
 		String  name = evt.getActionCommand();
		for (int i=0 ; i<app_tools.size() ; i++)
		{
			OneTool	t = (OneTool)app_tools.elementAt(i);
			if (t.name.equals(name))
			{
				try
				{
					System.out.println("Starting " + t.classname);

					//	Check if tool is already instancied.
					if (t.jframe==null)
					{
						//	Retrieve class name
						Class		cl = Class.forName(t.classname);

						//	And build object
						Class[]		param =  new Class[1];
						param[0] = new JFrame().getClass();
						Constructor	contructor = cl.getConstructor(param);
						JFrame[]	argin = new JFrame[1];
						argin[0] = this;
						Object obj = contructor.newInstance(argin);
						JFrame	jframe = (JFrame)obj;
						t.setJFrame(jframe);
						jframe.setVisible(true);
					}
					else
						t.jframe.setVisible(true);
				}
				catch(Exception e)
				{
					app_util.PopupError.show(this, e);
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
            dev.delete_property("�seEvents");

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
			app_util.PopupError.show(this, e);
		}
        buildTree();
	}
	//======================================================================
	//======================================================================
	private void newHostBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newHostBtnActionPerformed
		addNewHost(null);
	}//GEN-LAST:event_newHostBtnActionPerformed
	//======================================================================
	//======================================================================
	void addNewHost(TangoHost h)
	{
		NewStarterDialog	dialog
			= new NewStarterDialog(this, h, tree.collec_names, tree.hosts, true);
		dialog.setVisible(true);
		if (dialog.getValue()==JOptionPane.OK_OPTION)
		{
			buildTree();
		}
	}
	//======================================================================
	//======================================================================
	void editHostProperties(TangoHost h)
	{
		NewStarterDialog	dialog
			= new NewStarterDialog(this, h, tree.collec_names, tree.hosts, false);
		dialog.setVisible(true);
	}
	//======================================================================
	//======================================================================
	private void stopServersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopServersBtnActionPerformed
		new ServerCmdThread(this, tree.hosts, StopAllServers).start();
	}//GEN-LAST:event_stopServersBtnActionPerformed

	//======================================================================
	//======================================================================
	private void startServersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startServersBtnActionPerformed
		new ServerCmdThread(this, tree.hosts, StartAllServers).start();
	}//GEN-LAST:event_startServersBtnActionPerformed

	//======================================================================
	//======================================================================
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
	private void helpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpActionPerformed
 		String  item = evt.getActionCommand();

		if (item.equals(principleBtn.getText()))
			app_util.PopupMessage.showImage(this,
						(String)null, img_path + "principle.gif");
		else
		if (item.equals(helpAppliBtn.getText()))
			new app_util.PopupHtml(this).show(AstorUtil.getAppliHelpURL());
		else
		if (item.equals(helpStarterBtn.getText()))
			new app_util.PopupHtml(this).show(AstorUtil.getStarterHelpURL());
		else
		if (item.equals(stateIconsBtn.getText()))
			app_util.PopupMessage.showImage(this,
						(String)null, img_path + "astor_state_icons.jpg");
		else
		if (item.equals(releaseNoteBtn.getText()))
		{
            new app_util.PopupHtml(this).show(ReleaseNote.str);
        }
		else
		if (item.equals(tangorbBtn.getText()))
		{
			TangORBversion	tangorb = null;
			try {
				tangorb = new TangORBversion();
			}
			catch(Exception e) {}
			String	message = tangorb.jarfile + ":\n\n" + tangorb;
			app_util.PopupText	txt = new app_util.PopupText(this, true);
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
			app_util.PopupMessage.showImage(this, message, img_path + "tango_icon.jpg");
		}
		else
		if (item.equals(starterEventsItem.getText()))
		{
			Vector	v = new Vector();
			for (int i=0 ; i<tree.hosts.length ; i++)
				if (tree.hosts[i].use_events)
					v.add(tree.hosts[i].getName());

			String		title = null;
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
					hostnames[i] = (String)v.elementAt(i);
			}
			if (hostnames==null)
				app_util.PopupMessage.show(this, title);
			else
				new app_util.PopupText(this, true).show(title, hostnames, 300, 400);
		}
		else
			app_util.PopupMessage.show(this, "Not implemented yet !");
	}//GEN-LAST:event_helpActionPerformed

	//======================================================================
	//======================================================================
	private void stopHostsControledBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopHostsControledBtnActionPerformed
		if (tree!=null)
		{
			for (int i=0 ; i<tree.hosts.length ; i++)
			{
				tree.hosts[i].do_polling = false;
				tree.hostDialogs.close(tree.hosts[i]);
			}
			stopHostsControledBtn.setSelected(false);
		}
	}//GEN-LAST:event_stopHostsControledBtnActionPerformed

	//======================================================================
	//======================================================================
	private void allHostsControledBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allHostsControledBtnActionPerformed
		for (int i=0 ; i<tree.hosts.length ; i++)
			tree.hosts[i].do_polling = true;
		allHostsControledBtn.setSelected(false);
	}//GEN-LAST:event_allHostsControledBtnActionPerformed

	//======================================================================
	//======================================================================
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
        private void deviceBrowserBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deviceBrowserBtnActionPerformed
            if (dev_browser==null)
                dev_browser = new DevBrowser(this);
            dev_browser.setVisible(true);
        }//GEN-LAST:event_deviceBrowserBtnActionPerformed
	//======================================================================
	//======================================================================
    private void refreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
		buildTree();
    }//GEN-LAST:event_refreshBtnActionPerformed

	//======================================================================
    /**
	 *	Exit the Application
	 */
	//======================================================================
    private void exitBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitBtnActionPerformed
		doExit();
    }//GEN-LAST:event_exitBtnActionPerformed

	//======================================================================
    /**
	 *	Exit the Application
	 */
	//======================================================================
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
		doExit();
	}//GEN-LAST:event_exitForm
	//======================================================================
	//======================================================================
	private void stopThreads()
	{
        System.out.println("Astor exiting....");
        //	Stop all host controled
        if (tree!=null && tree.hosts!=null)
        {
            for (int i=0 ; i<tree.hosts.length ; i++)
            {
                //	Display a little timer during unsubscribe
                if ((i%2)==0)
                    System.out.print("\\" + (char)13);
                else
                    System.out.print("/" + (char)13);

                tree.hosts[i].stopThread();
                if (tree.hosts[i].use_events)
                {
                    try { Thread.sleep(20); } catch (Exception e){}
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
    private javax.swing.JToggleButton allHostsControledBtn;
    private javax.swing.JMenuItem changeTgHostBtn;
    private javax.swing.JMenu cmdMenu;
    private javax.swing.JMenuItem deviceBrowserBtn;
    private javax.swing.JMenuItem exitBtn;
    private javax.swing.JMenuItem expandBtn;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem helpAppliBtn;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpStarterBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JMenuItem jiveMenuItem;
    private javax.swing.JMenuItem logviewerMenuItem;
    private javax.swing.JMenuItem newBranchBtn;
    private javax.swing.JMenuItem newHostBtn;
    private javax.swing.JMenuItem principleBtn;
    private javax.swing.JMenuItem refreshBtn;
    private javax.swing.JMenuItem releaseNoteBtn;
    private javax.swing.JMenuItem serversItem;
    private javax.swing.JMenuItem startServersBtn;
    private javax.swing.JMenuItem starterEventsItem;
    private javax.swing.JMenuItem stateIconsBtn;
    private javax.swing.JToggleButton stopHostsControledBtn;
    private javax.swing.JMenuItem stopServersBtn;
    private javax.swing.JMenuItem tangoStatBtn;
    private javax.swing.JMenuItem tangorbBtn;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
        private javax.swing.JMenuItem searchHostItem;
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
		try
		{
			//	Firat time, Open a simple Tango window
			app_util.TangoWindow	tw =
					new app_util.TangoWindow("ASTOR  Tango Manager");
			tw.setVisible(true);

			Astor	astor = new Astor();
			astor.setVisible(true);
			astor.displayed = true;

			//	Close Tango Window
			tw.setVisible(false);
		}
		catch(java.lang.InternalError e)
		{
			System.out.println(e);
		}
		long	t1 = System.currentTimeMillis();
		System.out.println("Build  GUI :" + (t1-t0) + " ms");
	}


}
