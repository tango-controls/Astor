package admin.astor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

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
		hide();
	}  
  
 	//===============================================================
 	//===============================================================
	public boolean showDlg() {
		show();
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
