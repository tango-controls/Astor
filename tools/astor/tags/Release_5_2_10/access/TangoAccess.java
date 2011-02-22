//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for main swing class.
//
// $Author$
//
// $Revision$
// $Log$
// Revision 1.5  2009/02/18 09:51:46  pascal_verdier
// Splah screen management moved to AstorUtil class.
//
// Revision 1.4  2009/01/30 09:34:42  pascal_verdier
// Force access device name added.
//
// Revision 1.3  2008/11/19 10:01:34  pascal_verdier
// New tests done on Access control.
// Allowed commands tree added.
//
// Revision 1.2  2006/10/02 14:09:02  pascal_verdier
// Minor changes.
//
// Revision 1.1  2006/09/19 13:06:47  pascal_verdier
// Access control manager added.
//
//
//
// Copyleft 2005 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================


package admin.astor.access;

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;

//=======================================================
/**
 *	Class Description: Basic JFrame Class to display info
 *
 * @author  Pascal Verdier
 */
//=======================================================
public class TangoAccess extends JFrame
{
	/**
	 *  Initialized by make jar call and used to display title.
	 */
	private static String revNumber = "Release 1.0  -  Fri Sep 12 13:53:31 CEST 2008";

	String	access_devname;
    /**
     *	JTree object to display users rights
     */
    private UsersTree	users_tree = null;
    /**
     *	JTree object to display allowed commands
     */
    private AllowedCmdTree	cmd_tree = null;
	/**
	 *	Access proxy instance
	 */
	private AccessProxy	access_dev;

    /**
     *  Dialog to check access
     */
    private EditDialog  check_dlg = null;

	private JFrame	parent;

	static private final Dimension	pane_size = new Dimension(350, 500);
    //=======================================================
    /**
	 *	Creates new form TangoAccess
	 */
	//=======================================================
    public TangoAccess(JFrame parent) throws DevFailed
	{
        this.parent = parent;
		try
		{
			AstorUtil.startSplash("TangoAccess ");
        	AstorUtil.increaseSplashProgress(5, "Reading database");
			String	test = System.getenv("AccessControl");
			if (test==null)
				getTangoService();
			else
				access_devname = test;

        	initComponents();
        	initOwnComponents();
        	AstorUtil.increaseSplashProgress( 5, "Finalize GUI");
        	ImageIcon icon = Utils.getInstance().getIcon("tango_icon.jpg");
        	setIconImage(icon.getImage());
        	this.setTitle("Tango Access Control Manager");
			
			boolean	super_tango = false;
			String	str = System.getProperty("SUPER_TANGO");
			if (str !=null)
				if (str.toLowerCase().equals("true"))
					super_tango = true;
			topPanel.setVisible(super_tango);

        	pack();
			if (parent.getWidth()>0)	//	has parent
			{
				//	cascade window
				Point	p = parent.getLocationOnScreen();
				p.x += 100;
				p.y += 100;
				setLocation(p);
			}
			else
	        	ATKGraphicsUtils.centerFrameOnScreen(this);
				AstorUtil.stopSplash();
		}
		catch (DevFailed e)
		{
			AstorUtil.stopSplash();
			throw e;
		}
    }
 	//===========================================================
	//===========================================================
	private void getTangoService() throws DevFailed
    {
	    //  Get TangoAccess service and check if exist
        String[]    services =
	    	ApiUtil.get_db_obj().getServices("AccessControl", "tango");
        if (services.length==0)
            Except.throw_communication_failed("Service_DoesNotExist",
                    "There is no AccessControl service defined !",
                    "TangoAccess.TangoAccess()");
        access_devname = services[0];
	}
 	//===========================================================
	//===========================================================
	private void initOwnComponents() throws DevFailed
	{
		//	File menu
		fileMenu.setMnemonic ('F');
		checkAccessBtn.setMnemonic ('T');
		checkAccessBtn.setAccelerator(KeyStroke.getKeyStroke('T', Event.CTRL_MASK));
        exitBtn.setMnemonic ('E');
        exitBtn.setAccelerator(KeyStroke.getKeyStroke('Q', Event.CTRL_MASK));

		//	Action menu
        actionMenu.setMnemonic ('A');
        registerItem.setMnemonic ('R');

		//	Help menu
        helpMenu.setMnemonic ('H');
        principleItem.setMnemonic ('P');
		principleItem.setAccelerator(KeyStroke.getKeyStroke('P', Event.CTRL_MASK));

		//	Check if write allowed
 		access_dev = new AccessProxy(access_devname);
        if (access_dev.getAccessControl()==TangoConst.ACCESS_READ)
		{
            checkAccessBtn.setEnabled(false);
            actionMenu.setEnabled(false);
        }

		//	Build tabbed pane title
        tabbedPane.setTitleAt(0, "Users");
        tabbedPane.setTitleAt(1, "Allowed Cmd");

         //	Build users_tree to display users rights
		users_tree = new UsersTree(this, access_dev);
		JScrollPane scrowllPane = new JScrollPane();
		scrowllPane.setViewportView(users_tree);
		usersPanel.add(scrowllPane, BorderLayout.CENTER);

         //	Build users_tree to display users rights
		cmd_tree = new AllowedCmdTree(this, access_dev);
		scrowllPane = new JScrollPane();
		scrowllPane.setViewportView(cmd_tree);
		cmdClassPanel.add(scrowllPane, BorderLayout.CENTER);

		//	Add a panel to display icons.
		JLabel	lbl = new JLabel("Devices: ");
		JPanel	panel = new JPanel();
		panel.add(lbl);

		lbl = new JLabel("Read/Write");
		lbl.setIcon(Utils.getInstance().getIcon("greenbal.gif"));
		panel.add(lbl);

		lbl = new JLabel("Read Only");
		lbl.setIcon(Utils.getInstance().getIcon("redball.gif"));
		panel.add(lbl);
		usersPanel.add(panel, BorderLayout.SOUTH);

		tabbedPane.setPreferredSize(pane_size);
	}
	//=======================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//=======================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        usersPanel = new javax.swing.JPanel();
        cmdClassPanel = new javax.swing.JPanel();
        topPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        javax.swing.JMenuBar jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        checkAccessBtn = new javax.swing.JMenuItem();
        exitBtn = new javax.swing.JMenuItem();
        actionMenu = new javax.swing.JMenu();
        registerItem = new javax.swing.JRadioButtonMenuItem();
        helpMenu = new javax.swing.JMenu();
        principleItem = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        usersPanel.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("tab1", usersPanel);

        cmdClassPanel.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("tab2", cmdClassPanel);

        getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

        jLabel1.setText("You are Super User !");
        topPanel.add(jLabel1);

        getContentPane().add(topPanel, java.awt.BorderLayout.PAGE_START);

        fileMenu.setText("File");

        checkAccessBtn.setText("Test Tango Acces");
        checkAccessBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAccessBtnActionPerformed(evt);
            }
        });
        fileMenu.add(checkAccessBtn);

        exitBtn.setText("Exit");
        exitBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitBtnActionPerformed(evt);
            }
        });
        fileMenu.add(exitBtn);

        jMenuBar1.add(fileMenu);

        actionMenu.setText("Action");
        actionMenu.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                actionMenuItemStateChanged(evt);
            }
        });

        registerItem.setText("Register Service");
        registerItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerItemActionPerformed(evt);
            }
        });
        actionMenu.add(registerItem);

        jMenuBar1.add(actionMenu);

        helpMenu.setText("help");

        principleItem.setText("On Principle");
        principleItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                principleItemActionPerformed(evt);
            }
        });
        helpMenu.add(principleItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //=======================================================
    //=======================================================
    private void actionMenuItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_actionMenuItemStateChanged
        if (!actionMenu.isSelected())
            return;
        try
        {
            String[]    services =
	    		ApiUtil.get_db_obj().getServices(TangoConst.ACCESS_SERVICE, "tango");
            registerItem.setSelected(services.length!=0);
        }
        catch(DevFailed e)
        {
            ErrorPane.showErrorMessage(this,
				"Cannot start TangoAccess class", e);
        }
    }//GEN-LAST:event_actionMenuItemStateChanged

    //=======================================================
    //=======================================================
    private void registerItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerItemActionPerformed
        try
        {
            boolean b = (registerItem.getSelectedObjects()!=null);
            access_dev.registerService(b);
        }
        catch(DevFailed e)
        {
            ErrorPane.showErrorMessage(this,
				"Cannot start TangoAccess class", e);
        }
    }//GEN-LAST:event_registerItemActionPerformed

    //=======================================================
    //=======================================================
    private void checkAccessBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAccessBtnActionPerformed
        if (check_dlg==null)
        {
            check_dlg = new EditDialog(this, access_dev);
            check_dlg.showDialog();
        }
        else
            check_dlg.setVisible(true);
    }//GEN-LAST:event_checkAccessBtnActionPerformed

	//=======================================================
	//=======================================================
    private void exitBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitBtnActionPerformed
        doClose();
    }//GEN-LAST:event_exitBtnActionPerformed

	//=======================================================
    /**
	 *	Exit the Application
	 */
	//=======================================================
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        doClose();
    }//GEN-LAST:event_exitForm

	//=======================================================
	//=======================================================
	private void principleItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_principleItemActionPerformed
		app_util.PopupMessage.show(this,
			"This access management is necessary only if the\n" +
			"    \"AccessControl/tango\"\n" +
			"    Tango service has been installed.\n\n" +
			"By default all devices are forbiden for all users.\n" +
			"And the rights will be opened for [user, address, device].\n\n" +
			"This tool is able to define WRITE access \n" +
			"    on devices for a TANGO control system\n\n"+
			"You can define for a specified user:\n" +
			"    - Allowed addresses to write devices\n"+
			"    - Set devices acces to  READ_WRITE or READ_ONLY");
	}//GEN-LAST:event_principleItemActionPerformed

	//=======================================================
	//=======================================================
    private void doClose()
    {
        if (parent.getWidth()>0)
            setVisible(false);
        else
            System.exit(0);
    }

   //=======================================================
   /**
    * @param args the command line arguments
    */
	//=======================================================
    public static void main(String args[]) {
        try
        {
            new TangoAccess(new JFrame()).setVisible(true);
        }
        catch (DevFailed e)
        {
            ErrorPane.showErrorMessage(new JFrame(),
				"Cannot start TangoAccess class", e);
            System.exit(0);
        }
 		catch(java.lang.InternalError e)
		{
			System.out.println(e);
		}
 		catch(java.awt.HeadlessException e)
		{
			System.out.println(e);
		}
   }


	//=======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu actionMenu;
    private javax.swing.JMenuItem checkAccessBtn;
    private javax.swing.JPanel cmdClassPanel;
    private javax.swing.JMenuItem exitBtn;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuItem principleItem;
    private javax.swing.JRadioButtonMenuItem registerItem;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel topPanel;
    private javax.swing.JPanel usersPanel;
    // End of variables declaration//GEN-END:variables
	//=======================================================

}
