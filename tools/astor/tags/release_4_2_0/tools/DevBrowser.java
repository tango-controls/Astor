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
//
// Copyleft 2006 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================


package admin.astor.tools;

import admin.astor.*;
import app_util.PopupError;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.IORdump;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.TangoDs.Except;

import javax.swing.*;
import java.awt.*;
import java.util.StringTokenizer;



//=======================================================
/**
 *	Class Description: JFrame extention Class to display
 *		browsing and  accessing devices
 *
 * @author  Pascal Verdier
 */
//=======================================================
public class DevBrowser extends JFrame
{
	private Astor			astor;
	private DevBrowserTree	tree;

    private static EventsTable     ev_table;
	//=======================================================
    /**
	 *	Creates new form DevBrowser
	 */
	//=======================================================
    public DevBrowser(Astor parent)
	{
 		super();
		this.astor = parent;
		initComponents();

		//  Create  Tree
		//------------------------------
		try
		{
			tree = new DevBrowserTree(this);
            treeScrollPane.setViewportView (tree);
		}
		catch (DevFailed e)
		{
			PopupError.show(parent, e);
		}
        customizeMenu();
		pack();

        ATKGraphicsUtils.centerFrameOnScreen(this);
		jive.MultiLineToolTipUI.initialize();
    }
    //======================================================================
    //======================================================================
    private void customizeMenu()
    {
        //	File menu
        fileMenu.setMnemonic ('F');
        exitBtn.setMnemonic ('E');
        exitBtn.setAccelerator(KeyStroke.getKeyStroke('Q', Event.CTRL_MASK));
    }
	//===============================================================
	//===============================================================
	void displayEventProperties(String attname)
	{
		tree.displayEventProperties(attname);
	}

	//=======================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//=======================================================
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        textScrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        treeScrollPane = new javax.swing.JScrollPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitBtn = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        eventsDlgBtn = new javax.swing.JMenuItem();
        astorBtn = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        textScrollPane.setPreferredSize(new java.awt.Dimension(400, 400));
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Courier New", 1, 12));
        textScrollPane.setViewportView(textArea);

        jPanel1.add(textScrollPane, java.awt.BorderLayout.CENTER);

        treeScrollPane.setPreferredSize(new java.awt.Dimension(300, 400));
        jPanel1.add(treeScrollPane, java.awt.BorderLayout.WEST);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

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
        eventsDlgBtn.setText("Events Panel");
        eventsDlgBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventsDlgBtnActionPerformed(evt);
            }
        });

        viewMenu.add(eventsDlgBtn);

        astorBtn.setText("Astor Panel");
        astorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                astorBtnActionPerformed(evt);
            }
        });

        viewMenu.add(astorBtn);

        jMenuBar1.add(viewMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    //=======================================================
    //=======================================================
    private void astorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_astorBtnActionPerformed
        if (astor!=null)
            astor.setVisible(true);
    }//GEN-LAST:event_astorBtnActionPerformed

    //=======================================================
    //=======================================================
    private void eventsDlgBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventsDlgBtnActionPerformed

        try
        {
            if (ev_table==null)
                ev_table = new EventsTable(this);
            ev_table.setVisible(true);
        }
        catch(DevFailed e)
        {
            app_util.PopupError.show(this,e);
            return;
        }
    }//GEN-LAST:event_eventsDlgBtnActionPerformed

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

	//===============================================================
	//===============================================================
    private void doClose()
    {
        setVisible(false);
        if (astor.isVisible())
             dispose();
        else
            astor.doExit();
    }
	//===============================================================
	//===============================================================
	void setText(String str)
	{
		textArea.setText(str);
        textArea.setCaretPosition(0);
	}
	//===============================================================
	//===============================================================
	void add(String signame, int mode)
	{
        //  Build dialog if not already done.
        if (ev_table==null)
        {
            try
            {
                ev_table = new EventsTable(this);
            }
            catch(DevFailed e)
            {
                app_util.PopupError.show(this,e);
                return;
            }
            //ATKGraphicsUtils.centerDialog(ev_table);
        }
        ev_table.add(signame, mode);
	}
    //======================================================
    //======================================================
    void displayHostPanel(String devname)
    {
        astor.tree.displayHostInfo(devname);
    }
    //======================================================
    //======================================================
    void managePolling(SubscribedSignal sig, Point p)
    {
        try
        {
            new ManagePollingDialog(this, sig.devname, sig.attname, p).setVisible(true);
        }
        catch(DevFailed e)
        {
            PopupError.show(astor, e);
        }
    }
    //======================================================
    //======================================================
    void managePolling(String devname)
    {
        try
        {
            Point   p = getLocationOnScreen();
            p.translate(10, 10);
            new ManagePollingDialog(this, devname, "", p).setVisible(true);
        }
        catch(DevFailed e)
        {
            PopupError.show(astor, e);
        }
    }
    //======================================================
    //======================================================
    void managePolling(DeviceProxy dev, String attname)
    {
        try
        {
            Point   p = getLocationOnScreen();
            p.translate(10, 10);
            new ManagePollingDialog(
                    this, dev.name(), attname, p).setVisible(true);
        }
        catch(DevFailed e)
        {
            PopupError.show(astor, e);
        }
    }
	//=======================================================
    /**
    * @param args the command line arguments
    */
	//=======================================================
    public static void main(String args[]) {
		Astor		astor = new Astor();
       	DevBrowser	db= new DevBrowser(astor);
		db.setVisible(true);
    }


	//=======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem astorBtn;
    private javax.swing.JMenuItem eventsDlgBtn;
    private javax.swing.JMenuItem exitBtn;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextArea textArea;
    private javax.swing.JScrollPane textScrollPane;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
	//=======================================================

}
