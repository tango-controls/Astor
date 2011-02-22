//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  Dialog Class to display Starter logging info
//
// $Author$
//
// $Revision$
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================


package admin.astor;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.Except;
import fr.esrf.logviewer.DetailPanel;
import fr.esrf.logviewer.MyTableModel;
import fr.esrf.logviewer.XMLFileHandler;
import org.apache.log4j.Level;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Enumeration;
import admin.astor.tools.Utils;



//===============================================================
/**
 *	Class Description: Dialog Class to display Starter logging info.
 *	This class uses the LogViever classes.
 *
 *	@author  root
 */
//===============================================================


public class LoggingDialog extends JDialog {
	private TangoHost	host;

    /** the content handler **/
	private XMLFileHandler mHandler;
    /** parser to read XML files **/
	private XMLReader mParser;

    //===============================================================
	/*
	 *	Creates new form LoggingDialog
	 */
	//===============================================================
	public LoggingDialog(JFrame parent, TangoHost host) throws DevFailed
	{
		super(parent, true);
		this.host = host;

		initComponents();
		initMyComponents();

		titleLabel.setText("Starter logging for  " + host.getName());		
		pack();
		AstorUtil.centerDialog(this, parent);
	}

	//===============================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//===============================================================
	private void initComponents() {//GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel();
		cancelBtn = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		titleLabel = new javax.swing.JLabel();

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		cancelBtn.setText("Dismiss");
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

		pack();
	}//GEN-END:initComponents

	//===============================================================
	//===============================================================
	private void initMyComponents() throws DevFailed
	{
		try
		{
 			//	Initialise the final XML objects
            MyTableModel model = new MyTableModel();
			mHandler = new XMLFileHandler(model);
			mParser  = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			mParser.setContentHandler(mHandler);
   	  		 // Create the table
        	final JTable table = new JTable(model);
        	table.setRowSelectionAllowed(true);
        	table.setColumnSelectionAllowed(true);
        	table.setDragEnabled(true); 
        	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        	final Enumeration cenum = table.getColumnModel().getColumns();
        	LogTableRowRenderer dtcr = new LogTableRowRenderer();
        	int i = 0;
        	TableColumn tc;
        	//int col_width[] = {45, 155, 75, 155, 500};  
        	int col_width[] = {0, 155, 50, 155, 300};  
        	while (cenum.hasMoreElements()) { 
        	  tc = (TableColumn)cenum.nextElement();
        	  tc.setCellRenderer(dtcr);
        	  tc.setPreferredWidth(col_width[i++]);
        	}
        	JScrollPane scrollPane = new JScrollPane(table);
        	scrollPane.setBorder(BorderFactory.createTitledBorder("Logs"));
        	scrollPane.setMinimumSize(new Dimension(150, 150));
        	//scrollPane.setPreferredSize(new Dimension(790, 450));
        	scrollPane.setPreferredSize(new Dimension(600, 350));

        	// Create the details
        	final JPanel details = new DetailPanel(table, model);
        	details.setMinimumSize(new Dimension(0, 0));
        	details.setPreferredSize(new Dimension(600, 0));

        	getContentPane().add(scrollPane, BorderLayout.CENTER);

			//	Start thread to initialize table
			new LoadFile(this).start();
		}
		catch(Exception e)
		{
			Except.throw_exception("INIT_ERROR",
							e.toString(),
							"LoggingDialog.initMyComponents()");
		}
	}
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
		dispose();
	}
	//===============================================================
	//===============================================================



	//===============================================================
        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JPanel jPanel1;
        private javax.swing.JButton cancelBtn;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JLabel titleLabel;
        // End of variables declaration//GEN-END:variables
	//===============================================================





	//=========================================================================
	//=========================================================================
	class LoadFile extends Thread
	{
		private JDialog	parent;
		//======================================================================
		/**
		 *	Thread constructor.
         * @param parent JDialog parent instance
         */
		//======================================================================
		LoadFile(JDialog parent)
		{
			this.parent   = parent;
		}
		//======================================================================
		/**
		 *	Running thread method.
		 */
		//======================================================================
		public void run()
		{
			String	filename = "";
			try {
				//	Check if tmp dir exists
				String	tmp = "/tmp";
				if (!AstorUtil.osIsUnix())
					tmp = "c:/temp";

				File	f = new File(tmp);
				System.out.println("Check " + tmp);
				if (!f.exists()) {
					if (!f.mkdir())
                        System.out.println("Failed to create " + tmp);
				}
				int		random_value = new java.util.Random().nextInt(30000);
				filename = tmp + "/astor." + random_value;
				//	Read remote login file and write a local one
				//System.out.println("Query starter for " + host.getName() + " logging file");
				String	logs = host.readLogFile("starter/" + host.getName());
				FileOutputStream	fidout = new FileOutputStream(filename);
				fidout.write(logs.getBytes());
				fidout.close();
				
				//	Then parse this file to read XML logs
     	  		synchronized (mParser) {
            		// Create a dummy document to parse the file
            		final StringBuffer buf = new StringBuffer();
            		buf.append("<?xml version=\"1.0\" standalone=\"yes\"?>\n");
            		buf.append("<!DOCTYPE log4j:eventSet ");
            		buf.append("[<!ENTITY data SYSTEM \"file:///");
            		buf.append(filename);
            		buf.append("\">]>\n");
            		buf.append("<log4j:eventSet xmlns:log4j=\"Claira\">\n");
            		buf.append("&data;\n");
            		buf.append("</log4j:eventSet>\n");

					StringReader		sr = new StringReader(buf.toString());
            		final InputSource	is = new InputSource(sr);
            		mParser.parse(is);
					
					String	message =
                            Integer.toString(mHandler.getNumEvents()) + " events found !";
					Utils.popupMessage(parent, message);
				}
			}
			catch(DevFailed e) {
				Utils.popupError(parent, null, e);
			}
			catch(Exception e) {
				Utils.popupError(parent, null, e);
				e.printStackTrace();
				
			}
			//	Remove the temporary file
			try {
				File	f = new File(filename);
				if (f.exists())
					if (!f.delete())
                        System.err.println("Failed to delete " + filename);
			}
			catch(Exception e) {
				Utils.popupError(parent, null, e);				
			}
		}
	}

	//=========================================================================
	/**
	 *	A renderer class to update table
	 */
	//=========================================================================
	public class LogTableRowRenderer extends DefaultTableCellRenderer {
        
		private final Color _scolor = new Color(204, 204, 255);
		private final Color _color  = new Color(230, 230, 230);
		private final JCheckBox _true  = new JCheckBox("", true);
		private final JCheckBox _false = new JCheckBox("", false);
        
    	//=========================================================================
    	//=========================================================================
        LogTableRowRenderer ()
		{
			setHorizontalAlignment(javax.swing.SwingConstants.CENTER); 
			_true.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			_false.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		}
        
    	//=========================================================================
    	//=========================================================================
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int col)
        {
            String col_header = (String)table.getColumnModel().getColumn(col).getHeaderValue();
            //-- Set back and fore colors
            if (isSelected) {    
                setBackground(_scolor);
            } 
            else if (col_header.equals("Level")) {
               Level level = (Level)value;
               if (level == Level.FATAL) {
                 setBackground(Color.black);  
                 setForeground(Color.white);  
               }
               else if (level == Level.ERROR) {
                 setBackground(Color.red);  
                 setForeground(Color.black);  
               }
               else if (level == Level.WARN) {
                 setBackground(Color.orange);  
                 setForeground(Color.black);  
               }
               else if (level == Level.INFO) {
                 setBackground(Color.green);  
                 setForeground(Color.black);  
               }
               else if (level == Level.DEBUG) {
                 setBackground(Color.cyan);  
                 setForeground(Color.black);  
               }
            }
            else {
                if ((row % 2) == 0) {
                    setBackground(_color);
                } else {
                    setBackground(Color.white);
                }
                setForeground(Color.black);  
            }
            //-- Set cell content and height
            if (col_header.equals("Trace")) {
                return  (value == Boolean.TRUE) ? _true : _false;
            }

            return super.getTableCellRendererComponent(table,
                                                       value,
                                                       isSelected,
                                                       hasFocus,
                                                       row, 
                                                       col);
        }
    }
}
