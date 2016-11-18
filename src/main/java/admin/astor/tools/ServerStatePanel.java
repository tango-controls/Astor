//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,
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
// $Revision: 22226 $
//
//-======================================================================

package admin.astor.tools;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbServer;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.tangoatk.core.*;
import fr.esrf.tangoatk.widget.attribute.StateViewer;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorHistory;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *	This class is able to start a panel containing
 *	 a state viewer for each device of specified server
 *
 * @author Pascal Verdier
 */
public class ServerStatePanel extends JScrollPane {
    private String title;
    private AttributeList attlist = new AttributeList();
    private ErrorHistory err_history;
    private StatePopupMenu menu;

    //===============================================================
    //===============================================================
    public ServerStatePanel(String serverName) throws DevFailed {
        try {
            //	First time check if sever is running
            new DeviceProxy("dserver/" + serverName).ping();
        } catch (DevFailed e) {
            //	If not running set a more explicit message
            e.errors[0].desc = "Server " + serverName + " Is not running !";
            throw e;
        }

        title = serverName;
        //  Cet devices from admin device
        TangoClass[] classes = getClasses(serverName);
        initializeComponents(classes);
        menu = new StatePopupMenu(this);
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedDeclaration")
    public ServerStatePanel(String title, String[] deviceNames) {
        this.title = title;

        //  Set devices from deviceNames
        TangoClass _class = new TangoClass(deviceNames);
        initializeComponents(new TangoClass[]{_class});
    }

    //===============================================================
    //===============================================================
    public void stopRefresher() {
        attlist.stopRefresher();
    }

    //===============================================================
    //===============================================================
    private void initializeComponents(TangoClass[] classes) {
        err_history = new ErrorHistory();
        attlist.addErrorListener(err_history);
        attlist.startRefresher();

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        //  Add a line for each device
        int y = 2;
        for (TangoClass _class : classes) {
            //  Create a label for class name
            gbc.gridx = 0;
            gbc.gridy = y++;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gbc.insets = new java.awt.Insets(4, 0, 4, 0);
            panel.add(new JLabel(_class.name), gbc);

            //for (DeviceProxy d : _class.dev)
            for (TangoDevice dev : _class.dev) {
                if (dev != null) {
                    gbc.gridx = 1;
                    gbc.gridy = y;
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.insets = new Insets(4, 0, 4, 0);
                    panel.add(dev.label, gbc);

                    if (dev.viewer != null) {
                        gbc.gridx = 2;
                        gbc.gridy = y;
                        gbc.fill = GridBagConstraints.HORIZONTAL;
                        gbc.insets = new Insets(4, 0, 4, 0);
                        panel.add(dev.viewer, gbc);

                        gbc.gridx = 4;
                        gbc.gridy = y;
                        gbc.fill = GridBagConstraints.HORIZONTAL;
                        panel.add(dev.btn, gbc);

                        dev.setStateText();
                    }
                    y++;
                }
            }
        }

        //  Add a title label
        JLabel title_lbl = new JLabel(title);
        title_lbl.setFont(new Font("Dialog", Font.BOLD, 16));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new java.awt.Insets(4, 4, 30, 0);
        panel.add(title_lbl, gbc);

        //  Add a button to display error history
        JButton errBtn = new JButton("Errors");
        errBtn.setMargin(new java.awt.Insets(2, 5, 5, 2));
        gbc.gridwidth = 2;
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel.add(errBtn, gbc);

        //	Add Action listener
        errBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                if ((evt.getModifiers() & MouseEvent.CTRL_MASK) == 0)
                    err_history.setVisible(true);
                else
                    fr.esrf.tangoatk.widget.util.ATKDiagnostic.showDiagnostic();
            }
        });
        add(panel);
        setViewportView(panel);

        //  Check line number to resize Window
        if (y > 24) {
            this.setPreferredSize(new Dimension(300, 650));
        }
    }

    //===============================================================
    //===============================================================
    private TangoClass[] getClasses(String servname) throws DevFailed {
        DbServer serv = new DbServer(servname);
        String[] classnames = serv.get_class_list();
        TangoClass[] classes = new TangoClass[classnames.length];
        int i = 0;
        //  Create in reverse order to have high level devices in first.
        for (String name : classnames) {
            classes[classnames.length - i - 1] = new TangoClass(serv, name);
            i++;
        }
        return classes;
    }
    //===============================================================
    //===============================================================


    //===============================================================
    //===============================================================
    class TangoClass {
        String name;
        TangoDevice[] dev;

        //===============================================================
        public TangoClass(DbServer adm_dev, String name) {
            this.name = name;
            try {
                String[] deviceNames = adm_dev.get_device_name(name);
                dev = new TangoDevice[deviceNames.length];
                int i = 0;
                for (String deviceName : deviceNames)
                    dev[i++] = new TangoDevice(deviceName);

            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(new JFrame(), null, e);
            }
        }

        //===============================================================
        public TangoClass(String[] deviceNames) {
            this.name = "";
            try {
                dev = new TangoDevice[deviceNames.length];
                int i = 0;
                for (String deviceName : deviceNames)
                    dev[i++] = new TangoDevice(deviceName);

            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(new JFrame(), null, e);
            }
        }

        //===============================================================
        public String toString() {
            String str = "Class " + name + ":\n";
            for (TangoDevice d : dev)
                str += "\t" + d.name() + "\n";
            return str;
        }
        //===============================================================
    }

    //===============================================================
    //===============================================================
    class TangoDevice extends DeviceProxy implements IDevStateScalarListener {
        String name;
        JLabel label;
        StateViewer viewer;
        JButton btn;

        public TangoDevice(String name) throws DevFailed {
            super(name);
            this.name = name;
            try {
                label = new JLabel(name);
                label.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent evt) {
                        devMouseClicked(evt);
                    }
                });

                IDevStateScalar att_state = (IDevStateScalar) attlist.add(name + "/state");
                viewer = new StateViewer();
                viewer.setLabel("");
                viewer.setStatePreferredSize(new Dimension(60, 15));
                viewer.setModel(att_state);
                att_state.addDevStateScalarListener(this);

                btn = new JButton("...");
                btn.setPreferredSize(new Dimension(20, 15));
                //	Add Action listener
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent evt) {
                        devMouseClicked(evt);
                    }
                });
            } catch (ConnectionException e) {
                ErrorPane.showErrorMessage(new JFrame(), null, e);
            }
        }

        //===============================================================
        private void setStateText() {
            String str = viewer.getCurrentState();
            viewer.setStateText(str);
        }

        //===============================================================
        public void devStateScalarChange(DevStateScalarEvent evt) {
            viewer.setStateText(evt.getValue());
        }

        //===============================================================
        public void stateChange(AttributeStateEvent evt) {
            //viewer.setStateText(evt.getState());
        }

        //==================================================================
        public void errorChange(ErrorEvent evt) {
            //noinspection ThrowableResultOfMethodCallIgnored
            System.err.println(evt.getError().toString());
        }

        //===============================================================
        private void displayStatus() {
            try {
                String status = status();
                JOptionPane.showMessageDialog(new Frame(),
                        status,
                        name,
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(new JFrame(), null, e);
            }
        }

        //===============================================================
        private void devMouseClicked(MouseEvent evt) {
            int mask = evt.getModifiers();

            //  if from btn and mouse btn1--> display status
            if ((mask & InputEvent.BUTTON1_MASK) != 0 && evt.getSource() instanceof JButton)
                displayStatus();
            else if ((mask & InputEvent.BUTTON3_MASK) != 0) {
                menu.showMenu(evt, this);
            }
        }
    }
    //===============================================================
    //===============================================================


    //===============================================================

    /**
     * A popup menu to launch appli
     */
    //===============================================================
    class StatePopupMenu extends JPopupMenu {
        private ServerStatePanel parent;
        private final String[] menuLabels = {
                "Status",
                "Test Device",
                "monitor Device",
        };
        private JLabel title;
        private TangoDevice device;
        private final int OFFSET = 2;
        private final int STATUS = 0;
        private final int TEST_DEVICE = 1;
        private final int MONITOR_DEVICE = 2;

        //===============================================================
        public StatePopupMenu(ServerStatePanel parent) {
            super();
            this.parent = parent;
            buildBtnPopupMenu();
        }

        //===============================================================
        private void buildBtnPopupMenu() {
            title = new JLabel("Device :");
            title.setFont(new java.awt.Font("Dialog", Font.BOLD, 16));
            add(title);
            add(new JPopupMenu.Separator());
            for (String menuLabel : menuLabels) {
                if (menuLabel == null)
                    add(new Separator());
                else {
                    JMenuItem btn = new JMenuItem(menuLabel);
                    btn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            itemActionPerformed(evt);
                        }
                    });
                    add(btn);
                }
            }
        }

        //======================================================
        public void showMenu(MouseEvent evt, TangoDevice dev) {
            device = dev;
            title.setText("Device: " + device.name);

            JComponent c = (JComponent) evt.getSource();
            Point p = c.getLocation();
            show(parent, evt.getX() + p.x, evt.getY() + p.y);
        }

        //===============================================================
        //===============================================================
        private void itemActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int cmdidx = 0;
            for (int i = 0; i < menuLabels.length; i++)
                if (getComponent(OFFSET + i) == obj)
                    cmdidx = i;

            //	Check action
            switch (cmdidx) {
                case STATUS:
                    device.displayStatus();
                    break;
                case TEST_DEVICE:
                    try {
                        //	Start Test Device panel  on selected device
                        JDialog d = new JDialog(new JFrame(), false);
                        d.setTitle(device.name + " Device Panel");
                        d.setContentPane(new jive.ExecDev(device.name));
                        ATKGraphicsUtils.centerDialog(d);
                        d.setVisible(true);
                    } catch (DevFailed e) {
                        ErrorPane.showErrorMessage(new JFrame(), device.name, e);
                    }
                    break;
                case MONITOR_DEVICE:
                    new atkpanel.MainPanel(device.name(), false, true);
                    break;
            }
        }
    }
}
