//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author$
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
// $Revision$
//
//-======================================================================


package admin.astor;



import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.events.ITangoChangeListener;
import fr.esrf.TangoApi.events.TangoChange;
import fr.esrf.TangoApi.events.TangoChangeEvent;
import fr.esrf.TangoApi.events.TangoEventsAdapter;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ATKConstant;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 *	This class display a dialog with a list of servers running, stopped,
 *	and buttons to start or stop servers.
 *
 * @author verdier
 * @version $Revision$
 */


@SuppressWarnings("MagicConstant")
public class HostInfoDialog extends JDialog implements AstorDefs, TangoConst {
    public TangoHost host;
    public String name;
    private JFrame jFrame;
    private Color bg = null;

    private String attribute = "Servers";

    private static final int NO_CHANGE = 0;
    private static final int LIST_CHANGED = 1;
    private static final int STATE_CHANGED = 2;
    private static Dimension preferredSize;

    private JScrollPane scrollPane = null;
    private LevelTree[] trees = null;
    private JPanel levelsPanel = null;
    private JPanel[] treePanels;
    private JPanel notifdPanel;
    private JLabel notifdLabel;
    private ServerPopupMenu notifdMenu;
    private boolean standAlone = false;
    private UpdateThread    updateThread;
    //===============================================================
    /**
     * Creates new form HostInfoDialog
     *
     * @param parent        the Astor parent instance
     * @param hostName      host name to display info
     * @param standAlone    true if stand alone
     * @throws DevFailed    if starter connection failed
     */
    //===============================================================
    public HostInfoDialog(JFrame parent, String hostName, boolean standAlone) throws DevFailed{
        this(parent, new TangoHost(hostName, true));
        this.standAlone = standAlone;
        AstorUtil.getInstance().initIcons();
        //new HostStateThread(host).start();
    }
    //===============================================================
    /**
     * Creates new form HostInfoDialog
     *
     * @param jFrame the Astor parent instance
     * @param host   host to display info
     */
    //===============================================================
    public HostInfoDialog(JFrame jFrame, TangoHost host) {
        super(jFrame, false);
        this.jFrame = jFrame;
        this.host = host;
        this.name = host.getName();
        initComponents();
        setTitle(host + "  Control");
        displayAllBtn.setSelected(true);
        preferredSize = AstorUtil.getHostDialogPreferredSize();
        scrollPane = new JScrollPane();

        updateThread = new UpdateThread();
        updateThread.start();

        bg = titlePanel.getBackground();
        titleLabel.setText("Controlled Servers on " + name);
        notifdMenu = new ServerPopupMenu(jFrame, this, host, ServerPopupMenu.NOTIFD);

        //  Manage for READ_ONLY mode
        if (Astor.rwMode==AstorDefs.READ_ONLY) {
            startNewBtn.setVisible(false);
            startAllBtn.setVisible(false);
            stopAllBtn.setVisible(false);
        }

        pack();
        ATKGraphicsUtils.centerDialog(this);
    }

    //===============================================================
    //===============================================================
    public Astor getAstorObject() {
        if (jFrame instanceof Astor)
            return (Astor) jFrame;
        else
            return null;
    }

    //===============================================================
    //===============================================================
    public void displayHostInfoDialog(String hostname) {
        if (jFrame instanceof Astor)
            ((Astor)jFrame).tree.displayHostInfoDialog(hostname);
    }

    //===============================================================
    //===============================================================
    void setDialogPreferredSize(Dimension d) {
        preferredSize = d;
        packTheDialog();
    }

    //===============================================================
    //===============================================================
    void updatePanel(final boolean resizePanel) {
        //	Build panel
        if (trees == null) {
            int nbLevels = AstorUtil.getStarterNbStartupLevels();
            nbLevels++; //	for not controlled

            levelsPanel = new JPanel();
            levelsPanel.setLayout(new GridBagLayout());
            centerPanel.add(levelsPanel, BorderLayout.CENTER);

            //	First time build notifd label
            if (host.manageNotifd) {
                notifdPanel = new JPanel();
                notifdLabel = new JLabel("Event Notify Daemon");
                notifdLabel.setFont(new Font("Dialog", Font.BOLD, 12));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = nbLevels;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                notifdPanel.add(notifdLabel, gbc);
                levelsPanel.add(notifdPanel, gbc);

                //	Add Action listener
                notifdLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        serverBtnMouseClicked(evt);
                    }
                });
            }
            treePanels = new JPanel[nbLevels];
            trees = new LevelTree[nbLevels];

            for (int i = 0; i < nbLevels; i++) {
                trees[i] = new LevelTree(jFrame, this, host, i);
                treePanels[i] = new JPanel();
                treePanels[i].add(trees[i]);
            }
        } else
            for (LevelTree tree : trees)
                tree.checkUpdate();

        int nb = 0;
        for (int i = 1; i < trees.length; i++)
            nb += trees[i].getNbServers();
        titleLabel.setText("" + nb + " Controlled Servers on " + name);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                checkActiveLevels();
                updateHostState();
                if (resizePanel)
                    packTheDialog();
            }
        });
    }
    //===============================================================
    /**
     * Pack the window and check if it needs scroll bars or not.
     * If very big, needs scroll bars.
     * But if not so big, do not needs to have a good resize.
     */
    //===============================================================
    void packTheDialog() {
        if (!isVisible())
            return;

        Point p = getLocation();
        pack();
        int width = levelsPanel.getWidth();
        int height = levelsPanel.getHeight();
        //System.out.println(width + ", " + height);

        if (width > preferredSize.width || height > preferredSize.height) {
            //	set bar width (cannot get, not active yet)
            int bw = 20;
            //	Put the tree panel in a scroll pane
            centerPanel.remove(levelsPanel);
            Dimension d = new Dimension(preferredSize);
            //	Check to resize only in one way
            if (width < preferredSize.width - bw)
                d.width = width + bw;
            if (height < preferredSize.height - bw)
                d.height = height + bw;
            scrollPane.setPreferredSize(d);
            scrollPane.add(levelsPanel);
            scrollPane.setViewportView(levelsPanel);
            centerPanel.add(scrollPane, java.awt.BorderLayout.CENTER);
        } else {
            //	Put the tree panel directly in center panel.
            centerPanel.remove(scrollPane);
            scrollPane.remove(levelsPanel);
            centerPanel.add(levelsPanel, java.awt.BorderLayout.CENTER);
        }
        pack();
        setLocation(p);
   }

    //===============================================================
    //===============================================================
    private void checkActiveLevels() {
        levelsPanel.removeAll();

        //	Check how many levels are active
        List<JPanel> v = new ArrayList<>();
        for (int i = 1; i < trees.length; i++)
            if (trees[i].getNbServers() > 0)
                v.add(treePanels[i]);
        if (trees[LEVEL_NOT_CTRL].getNbServers() > 0)
            v.add(treePanels[LEVEL_NOT_CTRL]);

        //	Compute horizontal size and dispach
        int x = 0;
        int y = 1;
        int x_size = v.size() / 2 - 1;
        if (x_size < 2) x_size = 2;

        for (int i = 0; i < v.size(); i++) {
            JPanel panel = v.get(i);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = x++;
            gbc.gridy = y;
            gbc.insets = new Insets(5, 10, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.fill = GridBagConstraints.VERTICAL;
            gbc.anchor = GridBagConstraints.WEST;
            levelsPanel.add(panel, gbc);
            if (i == x_size) {
                x = 0;
                y += 2;
            }
        }
        //	Re-add notifd panel
        if (host.manageNotifd) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = x_size + 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            levelsPanel.add(notifdPanel, gbc);
        }
    }

    //===============================================================
    //===============================================================
    Color getBackgroundColor() {
        //	under Win32 the color is from button
        //	From dialog it is another one (Why ?)
        return startNewBtn.getBackground();
    }

    //===============================================================
    //===============================================================
    void updateHostState() {
        //	Manage Stater state
        if (host.state == moving) {
            String str_state = ApiUtil.stateName(DevState.MOVING);
            titlePanel.setBackground(ATKConstant.getColor4State(str_state));
        } else if (host.state == alarm) {
            String str_state = ApiUtil.stateName(DevState.ALARM);
            titlePanel.setBackground(ATKConstant.getColor4State(str_state));
        } else
            titlePanel.setBackground(bg);

        //	Update  notifd state
        if (host.manageNotifd && notifdLabel!=null)
            notifdLabel.setIcon(AstorUtil.state_icons[host.notifydState]);
    }


    //===============================================================
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    //===============================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        startNewBtn = new javax.swing.JButton();
        startAllBtn = new javax.swing.JButton();
        stopAllBtn = new javax.swing.JButton();
        displayAllBtn = new javax.swing.JRadioButton();
        centerPanel = new javax.swing.JPanel();
        titlePanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        startNewBtn.setText("Start New");
        startNewBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startNewBtnActionPerformed(evt);
            }
        });
        topPanel.add(startNewBtn);

        startAllBtn.setText("Start All");
        startAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startAllBtnActionPerformed(evt);
            }
        });
        topPanel.add(startAllBtn);

        stopAllBtn.setText("Stop All");
        stopAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopAllBtnActionPerformed(evt);
            }
        });
        topPanel.add(stopAllBtn);

        displayAllBtn.setText("Display All");
        displayAllBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        displayAllBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        displayAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayAllBtnActionPerformed(evt);
            }
        });
        topPanel.add(displayAllBtn);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        centerPanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Dialog Title");
        titlePanel.add(titleLabel);

        centerPanel.add(titlePanel, java.awt.BorderLayout.NORTH);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        cancelBtn.setText("Dismiss");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //======================================================
    /**
     * Manage event on clicked mouse on PogoTree object.
     *
     * @param evt the mouse event
     */
    //======================================================
    private void serverBtnMouseClicked(java.awt.event.MouseEvent evt) {
        notifdMenu.showMenu(evt);
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void displayAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayAllBtnActionPerformed

        boolean b = (displayAllBtn.getSelectedObjects() != null);
        if (trees != null) {
            for (LevelTree tree : trees)
                if (b) {
                    if (tree.getLevelRow() != LEVEL_NOT_CTRL)
                        tree.expandTree();
                } else
                    tree.collapseTree();
        }
    }//GEN-LAST:event_displayAllBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void stopAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAllBtnActionPerformed

        //	Check levels used by servers
        List<Integer> used = new ArrayList<>();
        for (int i = trees.length - 1; i >= 0; i--) {
            int level = trees[i].getLevelRow();
            if (level != LEVEL_NOT_CTRL) {    //	is controlled
                if (trees[i].getNbServers()>0 && trees[i].hasRunningServer())
                    used.add(level);
                else {
                    //  Send DevStopAll it to stop a DevStartAll if running
                    try { host.stopServers(level); }
                    catch (DevFailed e) { /* */ }
                }
            }
        }
        //	And stop them
        new ServerCmdThread(this, host, StopAllServers, used).start();

    }//GEN-LAST:event_stopAllBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void startAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startAllBtnActionPerformed

        //	Check levels used by servers
        List<Integer> used = new ArrayList<>();
        for (LevelTree tree : trees) {
            int level = tree.getLevelRow();
            if (level != LEVEL_NOT_CTRL) { //	is controlled
                if(tree.getNbServers() > 0 && tree.getState() != DevState.ON)
                    used.add(level);
            }
        }
        //	And start them
        new ServerCmdThread(this, host, StartAllServers, used).start();

    }//GEN-LAST:event_startAllBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void startNewBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startNewBtnActionPerformed

        //	Get Server name
        ListDialog jlist = new ListDialog(this);
        //	Search Btn position to set dialog location
        Point p = getLocationOnScreen();
        p.translate(50, 50);
        jlist.setLocation(p);
        jlist.showDialog();
        List<String> serverNames = jlist.getSelectedItems();
        if (serverNames == null)
            return;
        for (String serverName : serverNames) {
            if (serverName != null) {
                try {
                    //	OK to start, do it.
                    host.registerServer(serverName);
                    host.startOneServer(serverName);

                    //	OK to start get the Startup control params.
                    TangoServer ts = new TangoServer(serverName, DevState.OFF);
                    ts.startupLevel(this, host.getName(), p);
                } catch (DevFailed e) {
                    ErrorPane.showErrorMessage(jFrame, null, e);
                }
            }
        }
        //  Force a Starter update
        host.updateServersList(jFrame);
        try {
            //  And force asynchronous update
            DeviceAttribute attribute = host.read_attribute("Servers");
            manageServersAttribute(attribute);
        }
        catch(DevFailed e) {
            // Nothing
        }
    }//GEN-LAST:event_startNewBtnActionPerformed


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
     * Closes the dialog
     */
    //===============================================================
    void doClose() {
        if ((jFrame instanceof Astor) ||
            (!standAlone) ) {
            setVisible(false);
            dispose();
        }
        else {
            updateThread.stopThread();
            System.exit(0);
        }
    }

    //===============================================================
    //===============================================================
    void fireNewTreeSelection(LevelTree tree) {
        for (LevelTree tree1 : trees)
            if (tree1 != tree)
                tree1.clearSelection();
    }

    //===============================================================
    //===============================================================
    void stopLevel(int level) {
        List<Integer> levels = new ArrayList<>();
        levels.add(level);
        new ServerCmdThread(this, host, StopAllServers, levels).start();
    }

    //===============================================================
    //===============================================================
    void startLevel(int level) {
        List<Integer> levels = new ArrayList<>();
        levels.add(level);
        new ServerCmdThread(this, host, StartAllServers, levels, false).start();
    }

    //=============================================================
    //=============================================================
    private void manageServersAttribute(DeviceAttribute att) {
        //
        List<Server> servers = new ArrayList<>();
        try {
            if (!att.hasFailed()) {
                String[] lines = att.extractStringArray();
                for (String line : lines) {
                    Server server = new Server(line);
                    servers.add(server);
                }
            }
        } catch (DevFailed e) {
            System.err.println(name);
            Except.print_exception(e);
        }

        //	Check if something has changed
        switch (updateHost(servers)) {
            case STATE_CHANGED:
                updatePanel(false);
                break;
            case LIST_CHANGED:
                updatePanel(true);
                break;
            case NO_CHANGE:
                return;
        }
        for (LevelTree tree : trees)
            tree.repaint();
    }

    //=============================================================
    /**
     *	Update TangoHost objects and check what has changed.
     */
    //=============================================================
    private int updateHost(List<Server> newServers) {
        boolean state_changed = false;
        boolean list_changed = false;

        //	check if new one
        for (Server newServer : newServers) {
            TangoServer server = host.getServer(newServer.name);
            if (server == null) {
                //	create it
                try {
                    server = new TangoServer(newServer.name, newServer.state);
                } catch (DevFailed e) {
                    System.err.println(name);
                    Except.print_exception(e);
                }
                host.addServer(server);
                list_changed = true;
            }

            if (server != null) {
                //	Check state
                if (newServer.state != server.getState()) {
                    server.setState(newServer.state);
                    state_changed = true;
                }
                //	Check control
                if (newServer.controlled != server.controlled |
                        newServer.level != server.startup_level) {
                    server.controlled = newServer.controlled;
                    server.startup_level = newServer.level;
                    list_changed = true;
                }
            }
        }

        //	Check if some have been removed
        for (int i=0 ; i<host.nbServers() ; i++) {
            TangoServer server = host.getServer(i);
            boolean found = false;
            for (int j=0 ; !found && j<newServers.size() ; j++) {
                Server newServer = newServers.get(j);
                found = (newServer.name.equals(server.getName()));
            }
            if (!found) {
                //System.out.println(host + " removing " + server.getName());
                host.removeServer(server.getName());
                list_changed = true;
            }
        }

        if (list_changed)
            return LIST_CHANGED;
        else if (state_changed)
            return STATE_CHANGED;
        else
            return NO_CHANGE;
    }

    //=============================================================
    //=============================================================
    private DevState string2state(String str) {
        for (int i = 0; i < Tango_DevStateName.length; i++)
            if (str.equals(Tango_DevStateName[i]))
                return DevState.from_int(i);
        return DevState.UNKNOWN;
    }

    //=========================================================
    //=========================================================
    void setSelection(String serverName) {
        if (trees != null)
            for (LevelTree tree : trees) {
                TangoServer server = tree.getServer(serverName);
                if (server != null) {
                    tree.expandTree();
                    tree.setSelection(server);
                } else
                    tree.resetSelection();
            }
    }

    //=========================================================
    //=========================================================
    void updateData() {
        updatePanel(true);
    }
    //=========================================================
    //=========================================================

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JRadioButton displayAllBtn;
    private javax.swing.JButton startAllBtn;
    private javax.swing.JButton startNewBtn;
    private javax.swing.JButton stopAllBtn;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel titlePanel;
    // End of variables declaration//GEN-END:variables
    //===============================================================

    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        try {
			String hostname;
			
            if (args.length>0) {
				hostname = args[0];
			}
			else {
        		if ((hostname=(String) JOptionPane.showInputDialog(
                        new JFrame(), "Host Name ?", "Input Dialog",
                		JOptionPane.INFORMATION_MESSAGE, null, null, "")) ==null)
            		System.exit(0);
			}

            new HostInfoDialog(new JFrame(), hostname, true).setVisible(true);
        }
        catch(DevFailed e) {
            ErrorPane.showErrorMessage(new JFrame(), null, e);
       }
    }
    //===============================================================
    //===============================================================







    //=============================================================
    /*
     *  A class defining a server
     */
    //=============================================================
    class Server {
        String name;
        DevState state;
        boolean controlled = false;
        int level = 0;

        //=========================================================
        public Server(String line) {
            //	Parse line
            StringTokenizer stk = new StringTokenizer(line);
            List<String> items = new ArrayList<>();
            while (stk.hasMoreTokens())
                items.add(stk.nextToken());

            if (items.size() > 0)
                this.name = items.get(0);

            if (items.size() > 1)
                this.state = string2state(items.get(1));

            if (items.size() > 2)
                this.controlled = ((items.get(2)).equals("1"));

            if (items.size() > 3) {
                String s = items.get(3);
                try {
                    this.level = Integer.parseInt(s);
                } catch (NumberFormatException e) { /* */}
            }
            //System.out.println(this);
        }

        //=========================================================
        public String toString() {
            return name + " -> " + ApiUtil.stateName(state) + "	- " +
                    ((controlled) ? "" : "not ") + "Controlled 	level " + level;
        }
        //=========================================================
    }
    //===============================================================
    //===============================================================












    //===============================================================
    /**
     * A thread to read and update server lists
     */
    //===============================================================
    private class UpdateThread extends Thread {
        private int readInfoPeriod = 1000;
        private boolean stopIt = false;

        //===========================================================
        private synchronized void stopThread() {
            stopIt = true;
            notify();
        }
        //===========================================================
        public void run() {
            if (host.onEvents)
                subscribeChangeEvent();

            //	Manage polling on synchronous calls
            while (!stopIt) {
                long t0 = System.currentTimeMillis();

                waitNextLoop(t0);
                if (!stopIt) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (!host.onEvents) {
                                manageSynchronous();
                            }
                            if (!(jFrame instanceof Astor)) {
                                manageNotifd();
                                updateHostState();
                            }
                        }
                    });
                }
            }
        }
        //=============================================================
        //=============================================================
        private void manageNotifd() {
            try {
                DeviceAttribute deviceAttribute = host.read_attribute("NotifdState");
                DevState    devState = deviceAttribute.extractDevState();
                if (devState==DevState.ON)
                    host.notifydState = all_ok;
                else
                if (devState==DevState.FAULT)
                    host.notifydState = faulty;
            }
            catch(DevFailed e) {
                host.notifydState = unknown;
            }
        }
        //=============================================================
        /**
         * Compute time to sleep before next loop
         *
         * @param t0 time reference to compute time to wait
         */
        //=============================================================
        private synchronized void waitNextLoop(long t0) {
            try {
                long t1 = System.currentTimeMillis();
                long time_to_sleep = readInfoPeriod - (t1 - t0);

                if (time_to_sleep <= 0)
                    time_to_sleep = 100;
                wait(time_to_sleep);
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
        }
        //=============================================================
        /**
         * Read servers list attributes in synchronous mode.
         */
        //=============================================================
        private void manageSynchronous() {
            try {
                DeviceAttribute att = host.read_attribute(attribute);
                manageServersAttribute(att);
            } catch (DevFailed e) {
                // Except.print_exception(e);
            }
        }
    }


    //======================================================================
    //======================================================================
    private void subscribeChangeEvent() {
        try {
            if (host.supplier == null)
                host.supplier = new TangoEventsAdapter(host);
        } catch (DevFailed e) {
            host.onEvents = false;
            //	Display exception
            System.err.println("subscribeChangeServerEvent() for " +
                    host.get_name() + " FAILED !");
            fr.esrf.TangoDs.Except.print_exception(e);
            return;
        } catch (Exception e) {
            host.onEvents = false;
            //	Display exception
            System.err.println("subscribeChangeServerEvent() for " +
                    host.get_name() + " FAILED !");
            System.err.println(e.toString());
            return;
        }

        try {
            //	add listener for double_event and server_event
            ServerEventListener serverListener = new ServerEventListener();
            host.supplier.addTangoChangeListener(serverListener, attribute, false);

            System.out.println("subscribeChangeServerEvent() for " +
                    host.get_name() + "/" + attribute + " OK!");
        } catch (DevFailed e) {
            //	Display exception
            host.onEvents = false;
            System.err.println("subscribeChangeServerEvent() for " +
                    host.get_name() + " FAILED !");
            fr.esrf.TangoDs.Except.print_exception(e);
        } catch (Exception e) {
            //	Display exception
            host.onEvents = false;
            System.err.println("subscribeChangeServerEvent() for " +
                    host.get_name() + " FAILED !");
            System.err.println(e.toString());
        }
    }
    //======================================================================
    //======================================================================











    //=========================================================================
    /**
     * Server event listener
     */
    //=========================================================================
    private class ServerEventListener implements ITangoChangeListener {
        //=====================================================================
        //=====================================================================
        public void change(TangoChangeEvent event) {
            TangoChange tc = (TangoChange) event.getSource();
            String deviceName = tc.getEventSupplier().get_name();

            try {
                DeviceAttribute att = event.getValue();
                manageServersAttribute(att);
                //if (AstorUtil.getDebug())
                // 	System.out.println(host + "/" + att.getName() + " changed " + " : ");
            } catch (DevFailed e) {
                System.out.println(name);
                if (e.errors[0].reason.equals("API_EventTimeout")) {
                    System.err.println("HostStataThread.ServerEventListener" +
                            deviceName + " : API_EventTimeout");
                    //fr.esrf.TangoDs.Except.print_exception(e);
                } else
                    fr.esrf.TangoDs.Except.print_exception(e);
            } catch (Exception e) {
                System.err.println(name);
                System.err.println("AstorEvent." + deviceName);
                System.err.println(e.toString());
                System.err.println("HostStateThread.ServerEventListener : could not extract data!");
            }
        }
    }
}
