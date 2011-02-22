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
// Copyright 1995 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;

/**
 *	This class is the Astor main panel
 *	containing the Jtree used to display hosts.
 *
 * @author  root
 */

import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;


import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class Astor extends JFrame implements AstorDefs
{

	/**
	 *  Initialized by rcs unix utility and used to display title.
	 */
	private static String rcsString
     = "$Header$";

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


	//======================================================================
    /**
	 *	Creates new form Astor
	 */
	//======================================================================
	public Astor()
	{
		
		initComponents();
		customizeMenu();

		setTitle ("Tango Management  -  " +
				  			new app_util.RcsId(rcsString).toString());
		buildTree();
		ImageIcon icon = new ImageIcon(
			getClass().getResource("/app_util/img/tango_icon.jpg"));
		setIconImage(icon.getImage());
		
		changeTgHostBtn.setVisible(false);
		centerWindow();
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
			scrowllPane.setPreferredSize(new Dimension(320, 600));
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

		//	View menu
		viewMenu.setMnemonic ('V');
		newBranchBtn.setMnemonic ('N');
		newBranchBtn.setAccelerator(KeyStroke.getKeyStroke('N', Event.CTRL_MASK));

		refreshBtn.setMnemonic ('U');
		refreshBtn.setAccelerator(KeyStroke.getKeyStroke('U', Event.CTRL_MASK));

		changeTgHostBtn.setMnemonic ('C');
		changeTgHostBtn.setAccelerator(KeyStroke.getKeyStroke('C', Event.CTRL_MASK));

		expandBtn.setMnemonic ('E');
		expandBtn.setAccelerator(KeyStroke.getKeyStroke('E', Event.CTRL_MASK));

		//	Searc menu
		searchMenu.setMnemonic ('S');
		findByDeviceBtn.setAccelerator(KeyStroke.getKeyStroke('F', Event.CTRL_MASK));

		//	Command menu
		cmdMenu.setMnemonic ('C');

		newHostBtn.setAccelerator(KeyStroke.getKeyStroke('H', Event.CTRL_MASK));
	}

	//======================================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//======================================================================
        private void initComponents() {//GEN-BEGIN:initComponents
                jMenuBar1 = new javax.swing.JMenuBar();
                fileMenu = new javax.swing.JMenu();
                exitBtn = new javax.swing.JMenuItem();
                viewMenu = new javax.swing.JMenu();
                refreshBtn = new javax.swing.JMenuItem();
                changeTgHostBtn = new javax.swing.JMenuItem();
                expandBtn = new javax.swing.JMenuItem();
                serversItem = new javax.swing.JMenuItem();
                searchMenu = new javax.swing.JMenu();
                findByDeviceBtn = new javax.swing.JMenuItem();
                cmdMenu = new javax.swing.JMenu();
                startServersBtn = new javax.swing.JMenuItem();
                stopServersBtn = new javax.swing.JMenuItem();
                newHostBtn = new javax.swing.JMenuItem();
                newBranchBtn = new javax.swing.JMenuItem();
                helpMenu = new javax.swing.JMenu();
                principleBtn = new javax.swing.JMenuItem();
                helpAppliBtn = new javax.swing.JMenuItem();
                helpStarterBtn = new javax.swing.JMenuItem();
                stateIconsBtn = new javax.swing.JMenuItem();
                aboutBtn = new javax.swing.JMenuItem();
                jPanel1 = new javax.swing.JPanel();
                jLabel1 = new javax.swing.JLabel();
                allHostsControledBtn = new javax.swing.JToggleButton();
                stopHostsControledBtn = new javax.swing.JToggleButton();
                
                fileMenu.setText("File");
                exitBtn.setText("Exit");
                exitBtn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        exitBtnActionPerformed(evt);
                    }
                });
                
                fileMenu.add(exitBtn);
                jMenuBar1.add(fileMenu);
              viewMenu.setText("View");
                refreshBtn.setText("Update Tree from database");
                refreshBtn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        refreshBtnActionPerformed(evt);
                    }
                });
                
                viewMenu.add(refreshBtn);
                changeTgHostBtn.setText("Change Tango Host");
                changeTgHostBtn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        changeTgHostBtnActionPerformed(evt);
                    }
                });
                
                viewMenu.add(changeTgHostBtn);
                expandBtn.setText("Expand Tree");
                expandBtn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        expandBtnActionPerformed(evt);
                    }
                });
                
                viewMenu.add(expandBtn);
                serversItem.setText("Controlled Servers");
                serversItem.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        serversItemActionPerformed(evt);
                    }
                });
                
                viewMenu.add(serversItem);
                jMenuBar1.add(viewMenu);
              searchMenu.setText("Search");
                findByDeviceBtn.setText("Find by Device");
                findByDeviceBtn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        findByDeviceBtnActionPerformed(evt);
                    }
                });
                
                searchMenu.add(findByDeviceBtn);
                jMenuBar1.add(searchMenu);
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
                aboutBtn.setText("About");
                aboutBtn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        helpActionPerformed(evt);
                    }
                });
                
                helpMenu.add(aboutBtn);
                jMenuBar1.add(helpMenu);
              
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
            
            setJMenuBar(jMenuBar1);
            pack();
        }//GEN-END:initComponents

	//======================================================================
	//======================================================================
	private void findByDeviceBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findByDeviceBtnActionPerformed
		new DeviceTreeDialog(this, tree.hosts);
	}//GEN-LAST:event_findByDeviceBtnActionPerformed

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
			
			//	Remove devices and server
			Database	db = ApiUtil.get_db_obj(AstorUtil.getTangoHost());
			db.delete_device(devname);
			db.delete_device(devadmin);
			db.delete_server(servname);

			JOptionPane.showMessageDialog(this,
								hostname + " has been removed !",
								"Command Done",
								JOptionPane.INFORMATION_MESSAGE);
			buildTree();
		}
		catch (DevFailed e)
		{
			app_util.PopupError.show(this, e);
		}
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
		dialog.show();
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
		dialog.show();
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
						(String)null, "/admin/astor/img/principle.gif");
		else
		if (item.equals(helpAppliBtn.getText()))
			new app_util.PopupHtml(this).show(AstorUtil.getAppliHelpURL());
		else
		if (item.equals(helpStarterBtn.getText()))
			new app_util.PopupHtml(this).show(AstorUtil.getStarterHelpURL());
		else
		if (item.equals(stateIconsBtn.getText()))
			app_util.PopupMessage.showImage(this,
						(String)null, "/admin/astor/img/astor_state_icons.jpg");
		else
		if (item.equals(aboutBtn.getText()))
		{
			String	message = "Astor  (Tango Management) \n\n"+
						"This programme is used to control, start and stop\n"+
						"           the TANGO device servers. \n\n\n" +
						new app_util.RcsId(rcsString).toString();
			app_util.PopupMessage.show(this, message);
		}
	}//GEN-LAST:event_helpActionPerformed

	//======================================================================
	//======================================================================
	private void stopHostsControledBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopHostsControledBtnActionPerformed
		for (int i=0 ; i<tree.hosts.length ; i++)
		{
			tree.hosts[i].do_polling = false;
			tree.hostDialogs.close(tree.hosts[i]);
		}
		stopHostsControledBtn.setSelected(false);

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
	private void changeTgHostBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeTgHostBtnActionPerformed

		String	tango_host =  AstorUtil.getTangoHost();

		tango_host = (String)JOptionPane.showInputDialog(this,
								"New Tango Host (e.g.  hal:20000)", 
								"Input Dialog",
								JOptionPane.INFORMATION_MESSAGE,
								null, null, tango_host);
		if (tango_host!=null)
		{
			AstorUtil.setTangoHost(tango_host);
			buildTree();
			System.out.println("TANGO_HOST= " + AstorUtil.getInstance().getTangoHost());
		}

	}//GEN-LAST:event_changeTgHostBtnActionPerformed
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
	private void doExit()
	{
		//	Stop all host controle
		for (int i=0 ; i<tree.hosts.length ; i++)
		{
			if (tree.hosts[i].use_events)
				tree.hosts[i].stopThread();
			try { Thread.sleep(50); } catch (Exception e){}
		}
        System.exit(0);
	}

	//======================================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem exitBtn;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem refreshBtn;
    private javax.swing.JMenuItem changeTgHostBtn;
    private javax.swing.JMenuItem expandBtn;
    private javax.swing.JMenuItem serversItem;
    private javax.swing.JMenu searchMenu;
    private javax.swing.JMenuItem findByDeviceBtn;
    private javax.swing.JMenu cmdMenu;
    private javax.swing.JMenuItem startServersBtn;
    private javax.swing.JMenuItem stopServersBtn;
    private javax.swing.JMenuItem newHostBtn;
    private javax.swing.JMenuItem newBranchBtn;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem principleBtn;
    private javax.swing.JMenuItem helpAppliBtn;
    private javax.swing.JMenuItem helpStarterBtn;
    private javax.swing.JMenuItem stateIconsBtn;
    private javax.swing.JMenuItem aboutBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JToggleButton allHostsControledBtn;
    private javax.swing.JToggleButton stopHostsControledBtn;
    // End of variables declaration//GEN-END:variables
	//======================================================================

	//======================================================================
	/**
	 * @param args the command line arguments
	 */
	//======================================================================
    public static void main(String args[]) {
	
		try
		{
			//	Firat time, Open a simple Tango window
			app_util.TangoWindow	tw =
					new app_util.TangoWindow("ASTOR  Tango Manager");
			tw.setVisible(true);

			new Astor().show();

			//	Close Tango Window
			tw.setVisible(false);
		}
		catch(java.lang.InternalError e)
		{
			System.out.println(e);
		}
	}


}
