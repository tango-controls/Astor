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

    //===============================================================
    /**
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
        Vector  v = new Vector();
        StringTokenizer stk = new StringTokenizer(dev, "/");
        while (stk.hasMoreTokens())
            v.add(stk.nextToken());
        if (v.size()>3)
        {
            app_util.PopupError.show(this, "Incorrect device name  (too many members)");
            return false;
        }
        if (v.size()<3)
        {
            app_util.PopupError.show(this, "Incorrect device name  (not enough members)");
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
        StringTokenizer stk = new StringTokenizer(add, ".");
        Vector	v = new Vector();
        while (stk.hasMoreTokens())
            v.add(stk.nextToken());
        if (v.size()>4)
        {
            app_util.PopupError.show(this, "Incorrect IP address  (Too many members)");
            return false;
        }
        else
        if (v.size()<4)
        {
            app_util.PopupError.show(this, "Incorrect IP address  (not enougth members)");
            return false;
        }
        for (int i=0 ; i<v.size() ; i++)
        {
            //  Check if numbers
            try
            {
                Short.parseShort((String)v.get(i));
            }
            catch(NumberFormatException e)
            {
                //  Or if wildcard
                if (!v.get(i).equals("*"))
                {
                    app_util.PopupError.show(this, "Incorrect IP address  (member #" +
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
        r.width  = 200;
        r.height += 2;
        r.x += TXT_OFFSET;
        return r;
    }
}
