//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the Astor class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.3  2005/04/26 14:19:28  pascal_verdier
// Bug on close dialog fixed.
//
// Revision 3.2  2005/03/11 14:07:54  pascal_verdier
// Pathes have been modified.
//
//
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author  pons
 */
 
public class RenameDialog extends JDialog {

	private JTextField theText;
	private boolean    ret_code;
	String  value;

 	//===============================================================
	/**
	 *	Construction without predefined values
	 */
 	//===============================================================
	public RenameDialog(Frame parent,String value,Rectangle bounds) {
		super(parent,true);
		getContentPane().setLayout(null);
		theText = new JTextField();

		theText.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {

				if( e.getKeyCode() == KeyEvent.VK_ENTER ) {
					ret_code=true;
					closeDlg();
				}

				if( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
					ret_code=false;
					closeDlg();
				}
			}

			public void keyTyped(KeyEvent e) {
			}

		});

		getContentPane().add(theText);
		theText.setBounds(0,0,(int)bounds.getWidth(),(int)bounds.getHeight());     
		theText.setText(value);
		theText.setBorder( BorderFactory.createLineBorder(Color.black) );
		theText.selectAll();
		setBounds(bounds);
		setUndecorated(true);
		ret_code = false;
	}
  
  	//===============================================================
	//===============================================================
	public void closeDlg() {
		value = theText.getText();
		setVisible(false);
	}  
  
 	//===============================================================
 	//===============================================================
	public boolean showDlg() {
		setVisible(true);
		return ret_code;
	}
  
  	//===============================================================
	//===============================================================
	public String getNewName() {
		return value;
	}
  
  	//===============================================================
	//===============================================================
	public void moveToLocation(int x,int y) {
		Rectangle r = getBounds();
		r.setLocation(x,y);
		setBounds(r);
	}
}
