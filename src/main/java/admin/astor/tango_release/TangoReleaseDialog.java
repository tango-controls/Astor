//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009
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
// $Revision:  $
//
// $Log:  $
//
//-======================================================================

package admin.astor.tango_release;

import admin.astor.AstorUtil;
import admin.astor.tools.MySqlUtil;
import admin.astor.tools.PopupHtml;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *	JDialog Class to display display a tree
 *  with servers sorted by Tango or IDL versions.
 *
 *	@author  Pascal Verdier
 */
@SuppressWarnings("MagicConstant")
public class TangoReleaseDialog extends JDialog {

	private JFrame	parent;
	private int		retVal = JOptionPane.OK_OPTION;
    static final int byTango = 0;
    static final int byIDL   = 1;
	//===============================================================
	/**
	 *	Creates new form TangoReleaseDialog to display a tree
     * after have got the list of whole control system server list.
     *  with servers sorted by Tango or IDL versions.
     *  @param parent the frame parent object
     *  @throws DevFailed if cannot read host list from database
	 */
	//===============================================================
	public TangoReleaseDialog(JFrame parent) throws DevFailed{
        super(parent, false);
        this.parent = parent;

        //  Get host list
        String[] hostList = MySqlUtil.getInstance().getHostControlledList();
        List<String>   serverList = new ArrayList<>();

        //  For each host
        for (String  hostName : hostList) {
            System.out.println("Reading "+ hostName);
            //  Get server list
            List<String>   serverNames = getControlledServers(hostName);
            serverList.addAll(serverNames);
        }
        System.out.println(serverList.size());
        String  rootName = ApiUtil.getTangoHost();
        buildDialog(rootName, serverList);
    }
	//===============================================================
	/**
	 *	Creates new form TangoReleaseDialog to display a tree
     *  with servers sorted by Tango or IDL versions.
     *  @param parent the frame parent object
     *  @param hostName Host to be checked
	 */
	//===============================================================
	public TangoReleaseDialog(JFrame parent, String hostName){
        super(parent, false);
        this.parent = parent;
        buildDialog(hostName, getControlledServers(hostName));
    }
	//===============================================================
	/**
	 *	Creates new form TangoReleaseDialog to display a tree
     *  with servers sorted by Tango or IDL versions.
     *  @param parent the frame parent object
     *  @param rootName String to dsiplay in JTree root node
     *  @param serverNames  list of server to be analyzed
	 */
	//===============================================================
	public TangoReleaseDialog(JFrame parent, String rootName,  List<String> serverNames) {
		super(parent, false);
		this.parent = parent;
        buildDialog(rootName, serverNames);
	}

	//===============================================================
	//===============================================================
    private void buildDialog(String rootName, List<String> serverNames) {

        AstorUtil.startSplash("Server Tango Releases");
        AstorUtil.increaseSplashProgress(5, "Initializing....");
        initComponents();
        initOwnComponents(rootName, serverNames);

        titleLabel.setText("Tango Release for Servers");
        pack();
        ATKGraphicsUtils.centerDialog(this);
        AstorUtil.stopSplash();
    }
    //===============================================================
    //===============================================================
    private List<String>  getControlledServers(String hostName) {
        List<String>  serverList = new ArrayList<>();
        try {
            Database database = ApiUtil.get_db_obj();
            DeviceData argIn = new DeviceData();
            argIn.insert(hostName);
            DeviceData argOut = database.command_inout("DbGetHostServerList", argIn);
            String[] serverNames = argOut.extractStringArray();
            Collections.addAll(serverList, serverNames);
        }
        catch (DevFailed e) {
            //  Do nothing
        }
        return serverList;
    }
	//===============================================================
	//===============================================================
    private void initOwnComponents(String rootName, List<String> serverNames) {

        //  Get the server release list
        AstorUtil.increaseSplashProgress(1, "Check servers....");
        TangoServerReleaseList  serverReleaseList = new TangoServerReleaseList(serverNames);
        
        //  Root text
        rootName += "  (" + serverReleaseList.size() + " servers - " +
                        serverReleaseList.nbClasses() +  " classes)";

        //	Build tree to display info
        AstorUtil.increaseSplashProgress(5, "Build trees....");
        TangoReleaseTree    tangoTree = new TangoReleaseTree(parent,
                rootName, serverReleaseList, byTango);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(tangoTree);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        tangoPanel.add(scrollPane, BorderLayout.CENTER);

        TangoReleaseTree    idlTree = new TangoReleaseTree(parent,
                rootName, serverReleaseList, byIDL);
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(idlTree);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        idlPanel.add(scrollPane, BorderLayout.CENTER);
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

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton helpBtn = new javax.swing.JButton();
        javax.swing.JLabel dummyLbl = new javax.swing.JLabel();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        javax.swing.JTabbedPane jTabbedPane1 = new javax.swing.JTabbedPane();
        tangoPanel = new javax.swing.JPanel();
        idlPanel = new javax.swing.JPanel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Dialog Title");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        helpBtn.setText("Help");
        helpBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(helpBtn);

        dummyLbl.setText("                       ");
        bottomPanel.add(dummyLbl);

        cancelBtn.setText("Dismiss");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        tangoPanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Tango Releases", tangoPanel);

        idlPanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("IDL Releases", idlPanel);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
	@SuppressWarnings("UnusedParameters")
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		retVal = JOptionPane.CANCEL_OPTION;
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed

	//===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		retVal = JOptionPane.CANCEL_OPTION;
		doClose();
	}//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void helpBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpBtnActionPerformed
        URL url = getClass().getResource("/admin/astor/TangoRelease.html");
        PopupHtml   dialog = new PopupHtml(null);
        dialog.setLocation(getLocation());
        dialog.show(url, 900, 600);
    }//GEN-LAST:event_helpBtnActionPerformed

	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void doClose() {
	
		if (parent==null)
			System.exit(0);
		else {
			setVisible(false);
			dispose();
		}
	}
	//===============================================================
	//===============================================================
	public int showDialog() {
		setVisible(true);
		return retVal;
	}

	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel idlPanel;
    private javax.swing.JPanel tangoPanel;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//===============================================================




	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String args[]) {

        try {
            if (args.length>0) {
                if (args[0].equals("-test")) {
                    //  Do it for a list
                    List<String>   list = new ArrayList<>();
                    list.add("HostInfo/draco3");
                     list.add("Starter/draco3");
                   new TangoReleaseDialog(null, "My Test", list).setVisible(true);
                }
                else {
                    //  Do it for a host
                    TangoReleaseDialog dialog = new TangoReleaseDialog(null, args[0]);
                    dialog.setVisible(true);
                }
            }
            else {
                //  Do it for the who;e control system
                new TangoReleaseDialog(null).setVisible(true);
            }
        }
        catch(DevFailed e) {
            ErrorPane.showErrorMessage(new Frame(), null, e);
        }
	}
    //===============================================================
    //===============================================================

}
