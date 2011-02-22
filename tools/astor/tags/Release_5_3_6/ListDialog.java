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
// Revision 3.5  2008/03/03 14:55:21  pascal_verdier
// Starter Release_4 management.
//
// Revision 3.4  2006/01/11 08:46:13  pascal_verdier
// PollingProfiler added.
//
// Revision 3.3  2005/11/17 12:30:33  pascal_verdier
// Analysed with IntelliJidea.
//
// Revision 3.2  2004/09/28 07:01:51  pascal_verdier
// Problem on two events server list fixed.
//
// Revision 3.1  2003/06/19 12:57:57  pascal_verdier
// Add a new host option.
// Controlled servers list option.
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;

/**
 *
 * @author  verdier
 */



import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DeviceProxy;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

//===============================================================
public class ListDialog extends javax.swing.JDialog {
	private static String	str_filter = "*";
	private static String	previous_item = null;
	private HostInfoDialog	parent;
	private	Vector<String>	selectedItems = null;


	//======================================================
	//======================================================
	private void setList()	throws DevFailed
	{
		str_filter = filterTxt.getText();
		String[] servlist = ApiUtil.get_db_obj().get_server_list(str_filter);
		jList.setListData(servlist);

		//	Search if previous selection exists
		//----------------------------------------
		for (int i=0 ; i<servlist.length ; i++)
			if (servlist[i].equals(previous_item))
				jList.setSelectedIndex(i);
	}

	//======================================================
	/**
	 *	Creates new form ListDialog
	 */
	//======================================================
	public ListDialog(HostInfoDialog parent)
    {
		super (parent, true);
		this.parent = parent;
		initComponents ();

		//	fix str filter and add a mouse listener on list
		//---------------------------------------------------
		filterTxt.setText(str_filter);
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				listSelectionPerformed(e);
			}
		};
		jList.addMouseListener(mouseListener);

        pack ();
	}

 	//======================================================
	/** This method is called from within the constructor to
	* initialize the form.
	* WARNING: Do NOT modify this code. The content of this method is
	* always regenerated by the FormEditor.
	*/
 	//======================================================
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        startBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        filterTxt = new javax.swing.JTextField();
        createBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        startBtn.setText("Start Server");
        startBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBtnActionPerformed(evt);
            }
        });

        jPanel1.add(startBtn);

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        jPanel1.add(cancelBtn);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 300));
        jScrollPane1.setViewportView(jList);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel2.setText("Filter :  ");
        jPanel2.add(jLabel2);

        jPanel2.add(jLabel1);

        filterTxt.setColumns(20);
        filterTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterTxtActionPerformed(evt);
            }
        });

        jPanel2.add(filterTxt);

        createBtn.setText("Create New Server");
        createBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createBtnActionPerformed(evt);
            }
        });

        jPanel2.add(createBtn);

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents

    //======================================================
    //======================================================
    private void createBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createBtnActionPerformed
        try
        {
            jive.DevWizard wdlg = new jive.DevWizard(parent, parent.host);
            wdlg.showWizard(null);

            String	servname = wdlg.lastServStarted;
            if (servname!=null)
            {
                //	Search Btn position to set dialog location
                Point	p   = getLocationOnScreen();
                p.translate(50,50);
                try
                {
                    //	OK to start get the Startup control params.
                    //--------------------------------------------------
                    if (new TangoServer(servname, DevState.OFF).startupLevel(parent, parent.host.getName(), p))
                        parent.updateData();
                }
                catch (DevFailed e)
                {
                    app_util.PopupError.show(parent, e);
                }
            }
        }
        catch(NoSuchMethodError ex)
        {
            app_util.PopupError.show(parent, "This server is too old !\nUse Jive to create it.");
        }
        setVisible (false);
        dispose ();
    }//GEN-LAST:event_createBtnActionPerformed

	//======================================================
	//======================================================
	private void filterTxtActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterTxtActionPerformed
		try {
			setList();
		}
		catch(DevFailed e)
		{
			app_util.PopupError.show(parent, e);
		}
	}//GEN-LAST:event_filterTxtActionPerformed

	//======================================================
	//======================================================
	private void cancelBtnActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		setVisible (false);
		dispose ();
	}//GEN-LAST:event_cancelBtnActionPerformed

	//======================================================
	//======================================================
	private void startBtnActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBtnActionPerformed
		retreiveSelectedItem();
	}//GEN-LAST:event_startBtnActionPerformed
	//======================================================
	//======================================================
	private void listSelectionPerformed(MouseEvent evt)
	{
		//	save selected item to set selection  later.
		//----------------------------------------------------
		previous_item = new String((String) jList.getSelectedValue());

		//	Check if double click
		//-----------------------------
		if (evt.getClickCount() == 2)
			retreiveSelectedItem();
	}
	//======================================================
	//======================================================
	private void retreiveSelectedItem()
	{
		selectedItems = new Vector<String>();

		//	At first try if already running
		Object[]	selections = jList.getSelectedValues();
		for (int i=0 ; i<selections.length ; i++)
		{
			String	servname = (String)selections[i];
			try {
				String		devname = new String("dserver/" + servname);
				DeviceProxy	dev = new DeviceProxy(devname);
				try {
					dev.ping();
					//	ping works  --> already running -> throw exception
					app_util.PopupError.show(parent,
						new String(servname + "  is Already Running  on " +
    						new fr.esrf.TangoApi.IORdump(devname).get_host() + " !"));
					return;
				}
				catch(DevFailed e) { /* OK not running -> can be started */}
			}
			catch (DevFailed e)
			{
				app_util.PopupError.show(parent, e);
				return;
			}

			selectedItems.add(servname);
		}
		setVisible (false);
		dispose ();
	}
	//======================================================
	//======================================================
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		setVisible (false);
		dispose ();
	}//GEN-LAST:event_closeDialog
	//======================================================
	//======================================================
	public void showDialog()
	{
		try {
			setList();
		}
		catch(DevFailed e)
		{
			app_util.PopupError.show(parent, e);
		}
		setVisible(true);
	}
	//======================================================
	//======================================================
	public Vector<String> getSelectedItems()
	{
		return selectedItems;
	}

 	//======================================================
	//======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton createBtn;
    private javax.swing.JTextField filterTxt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList jList;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton startBtn;
    // End of variables declaration//GEN-END:variables

  /**
  * @param args the command line arguments
  public static void main (String args[]) {
    new ListDialog (new javax.swing.JFrame (), true).show ();
  }
  */
}
