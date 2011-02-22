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
// Revision 1.4  2011/01/04 14:35:45  pascal_verdier
// New constructors added (for Jive).
//
// Revision 1.3  2010/12/02 15:07:19  pascal_verdier
// Constructors added to be launched from Jive.
//
// Revision 1.2  2009/04/06 14:25:21  pascal_verdier
// *** empty log message ***
//
// Revision 1.1  2008/11/19 10:05:16  pascal_verdier
// Initial revision.
//
//
// Copyleft 2008 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor.tools;

import admin.astor.TangoServer;
import admin.astor.TangoHost;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.TangoDs.Except;

import javax.swing.*;
import java.awt.*;

//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


public class PoolThreadsManager extends JDialog
{
	private Component	parent;
	private TangoHost	host;
	private TangoServer	server;
	private PoolThreadsTree	tree;

	//===============================================================
	/*
	 *	Creates new form PoolThreadsManager
	 */
	//===============================================================
	public PoolThreadsManager(JFrame parent, TangoHost host, TangoServer server) throws DevFailed
	{
		super(parent, true);
		this.parent = parent;
		this.host   = host;
		this.server = server;
		createForm();
	}
	//===============================================================
	/*
	 *	Creates new form PoolThreadsManager
	 */
	//===============================================================
	public PoolThreadsManager(JFrame parent, TangoServer server) throws DevFailed
	{
		this(parent, null, server);
	}
	//===============================================================
	/*
	 *	Creates new form PoolThreadsManager
	 */
	//===============================================================
	public PoolThreadsManager(JDialog parent, TangoHost host, TangoServer server) throws DevFailed
	{
		super(parent, true);
		this.parent = parent;
		this.host   = host;
		this.server = server;
		createForm();
	}
	//===============================================================
	/*
	 *	Creates new form PoolThreadsManager
	 */
	//===============================================================
	public PoolThreadsManager(JDialog parent, TangoServer server) throws DevFailed
	{
		this(parent, null, server);
	}
	//===============================================================
	/*
	 *	Creates new form PoolThreadsManager
	 */
	//===============================================================
	public PoolThreadsManager(JFrame parent, TangoHost host, String servname) throws DevFailed
	{
		this(parent, host, new TangoServer(
			(servname.startsWith("dserver/"))? servname : "dserver/"+servname));
	}
	//===============================================================
	/*
	 *	Creates new form PoolThreadsManager
	 */
	//===============================================================
	public PoolThreadsManager(JDialog parent, TangoHost host, String servname) throws DevFailed
	{
		this(parent, host, new TangoServer(
			(servname.startsWith("dserver/"))? servname : "dserver/"+servname));
	}
	//===============================================================
	/*
	 *	Creates new form PoolThreadsManager
	 */
	//===============================================================
	public PoolThreadsManager(JFrame parent, String servname) throws DevFailed
	{
		this(parent, null, servname);
	}
	//===============================================================
	/*
	 *	Creates new form PoolThreadsManager
	 */
	//===============================================================
	public PoolThreadsManager(JDialog parent, String servname) throws DevFailed
	{
		this(parent, null, servname);
	}

	//===============================================================
	//===============================================================
	private void createForm() throws DevFailed
	{
		initComponents();
		//	Check IDL Version
		int	idl;
		try
		{
			idl = server.get_idl_version();
		}
		catch(DevFailed e)	{
			idl = 0;
		}

		if (idl>0 &&idl<4)
			Except.throw_non_supported_exception("BAD_IDL_VERSION",
				"The server is compiled with IDL " + idl +
				"\nThis feature is allowed only with IDL 4 (TANGO 7) and above",
				"PoolThreadsManager.PoolThreadsManager()");
		warningTextArea.setVisible(idl==0);	//	visible if server not running
		initializeTree();

		pack();
 		ATKGraphicsUtils.centerDialog(this);
	}
	//===============================================================
	//===============================================================
	private JScrollPane	scrowllPane = null;
	public void initializeTree() throws DevFailed
	{
		if (scrowllPane==null)
		{
			scrowllPane = new JScrollPane();
			scrowllPane.setPreferredSize(new Dimension(350, 400));
			centerPanel.add(scrowllPane, BorderLayout.CENTER);
		}
		else
		if (tree!=null)
			scrowllPane.remove(tree);

		//	Build users_tree to display uinfo
		tree = new PoolThreadsTree(this, server);
		scrowllPane.setViewportView(tree);

		//	Customize menu
		editMenu.setMnemonic ('E');
		newThreadItem.setMnemonic ('N');
		removeThreadItem.setMnemonic ('R');

		newThreadItem.setAccelerator(KeyStroke.getKeyStroke('N', Event.CTRL_MASK));
		removeThreadItem.setAccelerator(KeyStroke.getKeyStroke(Event.DELETE, 0));
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
        bottomPanel = new javax.swing.JPanel();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        centerPanel = new javax.swing.JPanel();
        warningTextArea = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        jMenuBar = new javax.swing.JMenuBar();
        editMenu = new javax.swing.JMenu();
        newThreadItem = new javax.swing.JMenuItem();
        removeThreadItem = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okBtn.setText("Apply");
        okBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okBtnActionPerformed(evt);
            }
        });

        bottomPanel.add(okBtn);

        cancelBtn.setText("Dismiss");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        centerPanel.setLayout(new java.awt.BorderLayout());

        warningTextArea.setColumns(20);
        warningTextArea.setEditable(false);
        warningTextArea.setFont(new java.awt.Font("Dialog", 1, 12));
        warningTextArea.setRows(3);
        warningTextArea.setText("Warning:\nIDL server cannot be checked.\nThis feature is avalable only for TANGO-7 (or above) servers.");
        centerPanel.add(warningTextArea, java.awt.BorderLayout.SOUTH);

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 14));
        titleLabel.setText("Polling Threads Mangement");
        jPanel1.add(titleLabel);

        centerPanel.add(jPanel1, java.awt.BorderLayout.NORTH);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        editMenu.setText("Edit");
        newThreadItem.setText("New Thread");
        newThreadItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newThreadItemActionPerformed(evt);
            }
        });

        editMenu.add(newThreadItem);

        removeThreadItem.setText("Remove Thread");
        removeThreadItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeThreadItemActionPerformed(evt);
            }
        });

        editMenu.add(removeThreadItem);

        jMenuBar.add(editMenu);

        setJMenuBar(jMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void removeThreadItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeThreadItemActionPerformed
		if (tree.selectedObjectIsThread())
			tree.removeThread();
	}//GEN-LAST:event_removeThreadItemActionPerformed

	//===============================================================
	//===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void newThreadItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newThreadItemActionPerformed
		tree.addThreadNode();
    }//GEN-LAST:event_newThreadItemActionPerformed

	//===============================================================
	//===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
	private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed

		if (JOptionPane.showConfirmDialog(parent,
				"Apply pool thread configuration to database ?",
				"Confirm Dialog",
				JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
		{
			tree.putPoolThreadInfo();
			if (host!=null)
				server.restart(parent, host, false);
			else
				JOptionPane.showMessageDialog(this,
								"Database has been updated.\nRestart the server now.",
								"Command Done",
								JOptionPane.INFORMATION_MESSAGE);
		}
	}//GEN-LAST:event_okBtnActionPerformed

	//===============================================================
	//===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
	private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed

	//===============================================================
	//===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
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
		if (parent.getWidth()>0) {
			setVisible(false);
			dispose();
		}
		else
			System.exit(0);
	}

	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JMenuItem newThreadItem;
    private javax.swing.JButton okBtn;
    private javax.swing.JMenuItem removeThreadItem;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextArea warningTextArea;
    // End of variables declaration//GEN-END:variables
	//===============================================================




	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String args[]) {
	
		
		try
		{
			//JDialog	dlg = new JDialog(new JFrame());
			new PoolThreadsManager(new JFrame(), "PoolThreadTest/pv").setVisible(true);
//					new TangoHost("esrflinux1-2", true), "dserver/PoolThreadTest/pv").setVisible(true);
		}
		catch(DevFailed e)
		{
            ErrorPane.showErrorMessage(new Frame(), "", e);
		}
	}

}
