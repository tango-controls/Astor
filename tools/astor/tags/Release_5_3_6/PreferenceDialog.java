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
// Revision 1.5  2010/10/08 07:41:27  pascal_verdier
// Minor changes.
//
// Revision 1.4  2009/01/16 14:46:58  pascal_verdier
// Black box management added for host and Server.
// Starter logging display added for host and server.
// Splash screen use ATK one.
//
// Revision 1.3  2008/12/16 15:17:16  pascal_verdier
// Add a scroll pane in HostInfoDialog in case of too big dialog.
//
// Revision 1.2  2008/11/19 09:59:56  pascal_verdier
// New tests done on Access control.
// Pool Threads management added.
// Size added as preferences.
//
// Revision 1.1  2008/01/18 10:13:03  pascal_verdier
// *** empty log message ***
//
//
//
// Copyleft 2007 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;

import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;

//===============================================================
/**
 *	Class Description: Basic Dialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


public class PreferenceDialog extends JDialog
{
	private JFrame	parent;

	//===============================================================
	/**
	 *	Creates new form PreferenceDialog
     * @param parent parent instance
     */
	//===============================================================
	public PreferenceDialog(JFrame parent)
	{
		super(parent, true);
		this.parent = parent;
		initComponents();
		initialize();

		titleLabel.setText(AstorUtil.getTangoHost() + "  preferences");
		pack();
        ATKGraphicsUtils.centerDialog(this);
	}
	//===============================================================
	//===============================================================
	private void storePreferences()
	{
		//	Get velues
		String	rl_user  = rshUserTxt.getText();
		String	rl_cmd   = rshCmdTxt.getText();

		//	Check them
		if (rl_user==null)              rl_user           = "";
		if (rl_cmd==null)               rl_cmd            = "";
		if (tools==null)                tools             = new String[0];
		if (last_collection==null)      last_collection   = new String[0];
		if (known_tango_hosts==null)    known_tango_hosts = new String[0];
		if (pages==null)                pages             = new String[0];

		//	Set them
		AstorUtil	util = AstorUtil.getInstance();
		util.setJiveReadOnly(jiveReadOnly);
		util.setStarterStartup(starterStart);
		AstorUtil.setRloginUser(rl_user);
		AstorUtil.setRloginCmd(rl_cmd);
		util.setLastCollectionList(last_collection);
		AstorUtil.setKnownTangoHosts(known_tango_hosts);
		AstorUtil.setTools(tools);
		AstorUtil.setHtmlHelps(pages);
		if (parent instanceof Astor)
		{
			try
			{
				int	w = Integer.parseInt(treeWidthtTxt.getText());
				int	h = Integer.parseInt(treeHeighttTxt.getText());
				Dimension	d = new Dimension(w, h);
				((Astor)parent).setTreeSize(d);
				AstorUtil.setPreferredSize(d);

				w = Integer.parseInt(hostDlgWidthtTxt.getText());
				h = Integer.parseInt(hostDlgHeighttTxt.getText());
				d = new Dimension(w, h);
				AstorUtil.setHostDialogPreferredSize(d);
				((Astor)parent).tree.hostDialogs.setDialogPreferredSize(d);		
			}
			catch(NumberFormatException e)
			{
				app_util.PopupError.show(this, e);
			}
		}
		try
		{
			//	And write it in database
			AstorUtil.putAstorProperties();
			app_util.PopupMessage.show(this,
				"The preferences have been saved for " + AstorUtil.getTangoHost());
		}
		catch (DevFailed e)
		{
			ErrorPane.showErrorMessage(this, "Cannot put Astor properties", e);
		}
	}
	//===============================================================
	//===============================================================
	private String[]	last_collection = new String[0];
	private String[]	known_tango_hosts= new String[0];
	private String[]	tools = new String[0];
	private String[]	pages = {
			"Device Servers",
			"http://www.esrf.fr/computing/cs/tango/tango_doc/ds_doc/index.html"
		};
	private boolean		jiveReadOnly = false;
	private boolean		starterStart = true;
	private void initialize()
	{
		AstorUtil	util = AstorUtil.getInstance();
		
		//	Get last collections
		last_collection = util.getLastCollectionList();

		//	Get known TANGO_HOST
		known_tango_hosts = AstorUtil.getKnownTangoHosts();
		
		//	Get additianal tools
		tools = AstorUtil.getTools();

		//	Get help pages
		pages = AstorUtil.getHtmlHelps();

		//	Get Jive startup mode
		jiveReadOnly = util.jiveIsReadOnly();
		manageToggleBtn(jiveRObtn, jiveReadOnly);

		//	Get starter startup mode
		starterStart = util.getStarterStartup();
		manageToggleBtn(starterStartupBtn, starterStart);

		//	Get rlogin info
		String	rl_user = AstorUtil.getRloginUser();
		if (rl_user!=null)
			rshUserTxt.setText(rl_user);
		String	rl_cmd  = AstorUtil.getRloginCmd();
		if (rl_cmd!=null)
			rshCmdTxt.setText(rl_cmd);

		if (parent instanceof Astor)
		{
			Dimension	d = ((Astor)parent).getTreeSize();
			treeWidthtTxt.setText(Integer.toString(d.width));
			treeHeighttTxt.setText(Integer.toString(d.height));

			d = AstorUtil.getHostDialogPreferredSize();
			hostDlgWidthtTxt.setText(Integer.toString(d.width));
			hostDlgHeighttTxt.setText(Integer.toString(d.height));
		}
	}

	//===============================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//===============================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JButton okBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JPanel prefPanel = new javax.swing.JPanel();
        javax.swing.JLabel treeWidthLbl = new javax.swing.JLabel();
        treeWidthtTxt = new javax.swing.JTextField();
        javax.swing.JLabel treeHeightLbl = new javax.swing.JLabel();
        treeHeighttTxt = new javax.swing.JTextField();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        rshUserTxt = new javax.swing.JTextField();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        rshCmdTxt = new javax.swing.JTextField();
        javax.swing.JSeparator jSeparator2 = new javax.swing.JSeparator();
        javax.swing.JSeparator jSeparator3 = new javax.swing.JSeparator();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        jiveRObtn = new javax.swing.JRadioButton();
        javax.swing.JSeparator jSeparator5 = new javax.swing.JSeparator();
        javax.swing.JSeparator jSeparator7 = new javax.swing.JSeparator();
        javax.swing.JButton lastCollectionBtn = new javax.swing.JButton();
        javax.swing.JButton tangoHostsBtn = new javax.swing.JButton();
        javax.swing.JButton toolsBtn = new javax.swing.JButton();
        javax.swing.JButton helpPagesBtn = new javax.swing.JButton();
        javax.swing.JLabel hostDlgWidthLbl = new javax.swing.JLabel();
        javax.swing.JLabel hostDlgHeightLbl = new javax.swing.JLabel();
        hostDlgWidthtTxt = new javax.swing.JTextField();
        hostDlgHeighttTxt = new javax.swing.JTextField();
        javax.swing.JLabel jLabel6 = new javax.swing.JLabel();
        starterStartupBtn = new javax.swing.JRadioButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okBtn.setText("OK");
        okBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okBtnActionPerformed(evt);
            }
        });
        jPanel1.add(okBtn);

        cancelBtn.setText("Cancel");
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

        prefPanel.setLayout(new java.awt.GridBagLayout());

        treeWidthLbl.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        treeWidthLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        treeWidthLbl.setText("Hosts Tree Width:  ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(treeWidthLbl, gridBagConstraints);

        treeWidthtTxt.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(treeWidthtTxt, gridBagConstraints);

        treeHeightLbl.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        treeHeightLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        treeHeightLbl.setText("Hosts Tree Height:  ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(treeHeightLbl, gridBagConstraints);

        treeHeighttTxt.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(treeHeighttTxt, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        prefPanel.add(jSeparator1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Remote login user :  ");
        jLabel1.setToolTipText("Default user name used for remote login on a host.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(jLabel1, gridBagConstraints);

        rshUserTxt.setColumns(12);
        rshUserTxt.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        rshUserTxt.setToolTipText("Default user name used for remote login on a host.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(rshUserTxt, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Remote login command :  ");
        jLabel2.setToolTipText("Command used for remote login on a host (rlogin, ssh, ...).");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(jLabel2, gridBagConstraints);

        rshCmdTxt.setColumns(12);
        rshCmdTxt.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        rshCmdTxt.setText("rlogin");
        rshCmdTxt.setToolTipText("Command used for remote login on a host (rlogin, ssh, ...).");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(rshCmdTxt, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        prefPanel.add(jSeparator2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        prefPanel.add(jSeparator3, gridBagConstraints);

        jLabel5.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        jLabel5.setText("Start Jive in READ_ONLY mode :");
        jLabel5.setToolTipText("Mode to start jive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        prefPanel.add(jLabel5, gridBagConstraints);

        jiveRObtn.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        jiveRObtn.setText("false");
        jiveRObtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jiveRObtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jiveRObtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jiveRObtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        prefPanel.add(jiveRObtn, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        prefPanel.add(jSeparator5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        prefPanel.add(jSeparator7, gridBagConstraints);

        lastCollectionBtn.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        lastCollectionBtn.setText("Last Collections :");
        lastCollectionBtn.setToolTipText("List of collections (families) displayed at the end of the control system tree.");
        lastCollectionBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lastCollectionBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(lastCollectionBtn, gridBagConstraints);

        tangoHostsBtn.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        tangoHostsBtn.setText("Known Tango Hosts");
        tangoHostsBtn.setToolTipText("List of   TANGO_HOST known (used to change during execution).");
        tangoHostsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tangoHostsBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(tangoHostsBtn, gridBagConstraints);

        toolsBtn.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        toolsBtn.setText("Additional tools :");
        toolsBtn.setToolTipText("List of  additiannal tools (see  Astor pages).");
        toolsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolsBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(toolsBtn, gridBagConstraints);

        helpPagesBtn.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        helpPagesBtn.setText("Help pages");
        helpPagesBtn.setToolTipText("List of   help  pages (as tools see Astor pages).");
        helpPagesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpPagesBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(helpPagesBtn, gridBagConstraints);

        hostDlgWidthLbl.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        hostDlgWidthLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        hostDlgWidthLbl.setText("Host Panel Width:  ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(hostDlgWidthLbl, gridBagConstraints);

        hostDlgHeightLbl.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        hostDlgHeightLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        hostDlgHeightLbl.setText("Host Panel Height:  ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(hostDlgHeightLbl, gridBagConstraints);

        hostDlgWidthtTxt.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(hostDlgWidthtTxt, gridBagConstraints);

        hostDlgHeighttTxt.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        prefPanel.add(hostDlgHeighttTxt, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        jLabel6.setText("Starter starts servers at startup:");
        jLabel6.setToolTipText("Mode to start jive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        prefPanel.add(jLabel6, gridBagConstraints);

        starterStartupBtn.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12));
        starterStartupBtn.setSelected(true);
        starterStartupBtn.setText("true");
        starterStartupBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        starterStartupBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        starterStartupBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                starterStartupBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        prefPanel.add(starterStartupBtn, gridBagConstraints);

        getContentPane().add(prefPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
    private void helpPagesBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpPagesBtnActionPerformed
		JButton	btn = (JButton)evt.getSource();
		GetTextDialog	dlg = new GetTextDialog(this,
				btn.getText(), btn.getToolTipText(), pages);
		if (dlg.showDialog()==JOptionPane.OK_OPTION)
			pages = dlg.getTextLines();
    }//GEN-LAST:event_helpPagesBtnActionPerformed

	//===============================================================
	//===============================================================
    private void toolsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolsBtnActionPerformed
		JButton	btn = (JButton)evt.getSource();
		GetTextDialog	dlg = new GetTextDialog(this,
				btn.getText(), btn.getToolTipText(), tools);
		if (dlg.showDialog()==JOptionPane.OK_OPTION)
			tools = dlg.getTextLines();

     }//GEN-LAST:event_toolsBtnActionPerformed

	//===============================================================
	//===============================================================
    private void tangoHostsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tangoHostsBtnActionPerformed
		JButton	btn = (JButton)evt.getSource();
		GetTextDialog	dlg = new GetTextDialog(this,
				btn.getText(), btn.getToolTipText(), known_tango_hosts);
		if (dlg.showDialog()==JOptionPane.OK_OPTION)
			known_tango_hosts = dlg.getTextLines();
    }//GEN-LAST:event_tangoHostsBtnActionPerformed

	//===============================================================
	//===============================================================
    private void lastCollectionBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastCollectionBtnActionPerformed

		JButton	btn = (JButton)evt.getSource();
		GetTextDialog	dlg = new GetTextDialog(this,
				btn.getText(), btn.getToolTipText(), last_collection);
		if (dlg.showDialog()==JOptionPane.OK_OPTION)
			last_collection = dlg.getTextLines();
    }//GEN-LAST:event_lastCollectionBtnActionPerformed

	//===============================================================
	//===============================================================
	private void manageToggleBtn(JToggleButton btn, boolean b)
	{
		btn.setSelected(b);
		btn.setText(""+b);
	}
	//===============================================================
	//===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void jiveRObtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jiveRObtnActionPerformed
		jiveReadOnly = (jiveRObtn.getSelectedObjects()!=null);
		manageToggleBtn(jiveRObtn, jiveReadOnly);
    }//GEN-LAST:event_jiveRObtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void starterStartupBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_starterStartupBtnActionPerformed
        starterStart = (starterStartupBtn.getSelectedObjects()!=null);
        manageToggleBtn(starterStartupBtn, starterStart);
    }//GEN-LAST:event_starterStartupBtnActionPerformed
	//===============================================================
	//===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
	private void okBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBtnActionPerformed

		storePreferences();
		doClose();
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
		setVisible(false);
		if (parent instanceof Astor)
			dispose();
		else
			System.exit(0);
	}
	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField hostDlgHeighttTxt;
    private javax.swing.JTextField hostDlgWidthtTxt;
    private javax.swing.JRadioButton jiveRObtn;
    private javax.swing.JTextField rshCmdTxt;
    private javax.swing.JTextField rshUserTxt;
    private javax.swing.JRadioButton starterStartupBtn;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField treeHeighttTxt;
    private javax.swing.JTextField treeWidthtTxt;
    // End of variables declaration//GEN-END:variables
	//===============================================================




	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String args[]) {
		new PreferenceDialog(new javax.swing.JFrame()).setVisible(true);
	}

}
