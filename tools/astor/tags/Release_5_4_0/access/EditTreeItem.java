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
// Copyleft 2003 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor.access;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author  pons
 */

public class EditTreeItem extends JDialog {

    private JTextField theText;
    private boolean    ret_code;
    private int		   obj_type;
    private static final int    TXT_OFFSET = 20;

	private static boolean keep_deco = false;
    //===============================================================
    /*
     *	Construction without predefined values
     */
    //===============================================================
    //===============================================================
    //===============================================================
    public EditTreeItem(Frame parent, JTree tree, String value, int obj_type) {
        super(parent,true);
        this.obj_type = obj_type;
        getContentPane().setLayout(null);
        theText = new JTextField();
		
		//	Nicer without decoration !
		//	but OK only with java 1.4 and 1.6
		String	version = System.getProperty("java.version");
		if (version!=null)
			keep_deco = version.startsWith("1.5");

		//	Add keyboard listeners
        theText.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
            }
            public void keyReleased(KeyEvent e) {
                keyReleasedListener(e);
            }
            public void keyTyped(KeyEvent e) {
            }

        });

        Rectangle bounds = computeBounds(tree);
        getContentPane().add(theText);
        theText.setBounds(0,0,(int)bounds.getWidth(),(int)bounds.getHeight());
        theText.setText(value);
        theText.setBorder( BorderFactory.createLineBorder(Color.black) );
        theText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        theText.selectAll();
        setBounds(bounds);
		if (!keep_deco)
        	setUndecorated(true);
        ret_code = false;
    }


    //===============================================================
    //===============================================================
    public void keyReleasedListener(KeyEvent e)
    {
        if( e.getKeyCode() == KeyEvent.VK_ENTER ) {
            if (checkInputs())
            {
                ret_code=true;
                closeDlg();
            }
        }

        if( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
            ret_code=false;
            closeDlg();
        }
    }
    //===============================================================
    //===============================================================
    private boolean checkInputs()
    {
        switch (obj_type)
        {
        case UsersTree.ADDRESS:
            return checkAddress();
        case UsersTree.DEVICE:
            return checkDevice();
        }
        return false;
    }
      //===============================================================
    //===============================================================
    private boolean checkDevice()
    {
        //  Check dev name
        String	dev = theText.getText().trim().toLowerCase();
        theText.setText(dev);
        Vector<String>  v = new Vector<String>();
        StringTokenizer stk = new StringTokenizer(dev, "/");
        while (stk.hasMoreTokens())
            v.add(stk.nextToken());
        if (v.size()>3)  {
            admin.astor.tools.Utils.popupError(this, "Incorrect device name  (too many members)");
            return false;
        }
        if (v.size()<3) {
            admin.astor.tools.Utils.popupError(this, "Incorrect device name  (not enough members)");
            return false;
        }
        return true;
    }
      //===============================================================
    //===============================================================
    private boolean checkAddress()
    {
        //  check IP add name
        String	add = theText.getText().trim();
        theText.setText(add);

		//	Check if host name
		try {
	 		java.net.InetAddress	iadd = 
				java.net.InetAddress.getByName(add);

			//	If found replace by address
			add = iadd.getHostAddress();
			theText.setText(add);
		}
		catch(Exception e) { /* */ }

		//	Try to split with '.' separator
        StringTokenizer stk = new StringTokenizer(add, ".");
        Vector<String>	v = new Vector<String>();
        while (stk.hasMoreTokens())
            v.add(stk.nextToken());
        if (v.size()>4) {
            admin.astor.tools.Utils.popupError(this, "Incorrect IP address  (Too many members)");
            return false;
        }
        else
        if (v.size()<4)  {
            admin.astor.tools.Utils.popupError(this, "Incorrect IP address  (not enougth members)");
            return false;
        }

		//	rebuild add string to be sure that there is no too much '.'
		//		like xxx.xxx....xx....xx
		add = v.get(0) + "." + v.get(1) + "." + v.get(2) + "." +v.get(3);
		theText.setText(add);
		
        for (int i=0 ; i<v.size() ; i++) {
            //  Check if numbers
            try {
                Short.parseShort(v.get(i));
            }
            catch(NumberFormatException e) {
           		//  if NOT wildcard
              	if (!v.get(i).equals("*")) {
                   	admin.astor.tools.Utils.popupError(this, "Incorrect IP address  (member #" +
                               	(i+1) + " (" + v.get(i) + ") is not a number)");
                   	return false;
				}
            }
        }
		
        return true;
     }
      //===============================================================
    //===============================================================
    private void closeDlg() {
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
    public String getInputs() {
        return theText.getText();
    }

    //===============================================================
    /**
     *	Compute bound rectangle for a node
     * @param tree the tree to compute bonds
     * @return the computed boundspr
     */
    //===============================================================
    private Rectangle computeBounds(JTree tree)
    {
        TreePath    selPath = tree.getSelectionPath();
        tree.scrollPathToVisible(selPath);
        Rectangle r  = tree.getPathBounds(selPath);
        Point p = r.getLocation();
        SwingUtilities.convertPointToScreen(p, tree);
        r.setLocation(p);
        r.width  = 250;
        r.height += 2;
        r.x += TXT_OFFSET;
        return r;
    }
}
