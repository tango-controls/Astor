//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author: verdier $
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
// $Revision:  $
//
//-======================================================================


package admin.astor.statistics;

import admin.astor.AstorUtil;
import admin.astor.tools.PopupText;
import admin.astor.tools.PopupTable;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

//=======================================================
/**
 * JFrame Class to display info
 *
 * @author Pascal Verdier
 */
//=======================================================
public class StatisticsPanel extends JFrame {
    static private JFileChooser chooser = null;
    private static final StatisticsFileFilter fileFilter =
            new StatisticsFileFilter("xml", "Statistics Files");
    private JFrame parent = null;
    private GlobalStatistics globalStatistics;
    private JScrollPane tableScrollPane = null;
    private GlobalStatisticsTable statisticsTable;


    //=======================================================
    /**
     * Creates new form StatisticsPanel
     *
     * @param parent   JFrame parent instance (if null, exit at exitBtn clicked)
     * @param fileName file's name to load statistics.
     * @throws DevFailed if read or load statistices from file failed.
     */
    //=======================================================
    public StatisticsPanel(JFrame parent, String fileName) throws DevFailed {
        this.parent = parent;
        initComponents();
        customizeMenus();

        globalStatistics = new GlobalStatistics(fileName);
        displayGlobalStatistics();

        pack();
        ATKGraphicsUtils.centerFrameOnScreen(this);
    }
    //=======================================================
    /**
     * Creates new form StatisticsPanel
     */
    //=======================================================
    public StatisticsPanel() {
        this(null);
    }
    //=======================================================
    /**
     * Creates new form StatisticsPanel
     *
     * @param parent JFrame parent instance (if null, exit at exitBtn clicked)
     */
    //=======================================================
    public StatisticsPanel(JFrame parent) {
        this.parent = parent;
        AstorUtil.startSplash("Statistics ");
        AstorUtil.increaseSplashProgress(5, "Initializing....");
        initComponents();
        customizeMenus();

        titleLabel.setText("No Statistics Read");
        pack();
        ATKGraphicsUtils.centerFrameOnScreen(this);
        AstorUtil.stopSplash();
    }
    //=======================================================
    /**
     * @param hostList host names to be checked, if empty, check all Astor controlled hosts.
     */
    //=======================================================
    public void readAndDisplayStatistics(ArrayList<String> hostList) {
        titleLabel.setText("Reading and Computing Statistics");
        new ReadThread(hostList).start();
    }

    //=======================================================
    //=======================================================
    private void displayGlobalStatistics() {
        //  Build the server failed list and display it in a table
        List<ServerStat> failedServers =
                getServerFailedList(globalStatistics.getStarterStatistics());
        statisticsTable = new GlobalStatisticsTable(this);
        statisticsTable.setStatistics(failedServers);
        globalStatTextArea.setText(globalStatistics.toString());

        //  Put it in a scrolled pane.
        if (tableScrollPane != null)
            getContentPane().remove(tableScrollPane);
        tableScrollPane = new JScrollPane();
        tableScrollPane.setPreferredSize(new Dimension(
                statisticsTable.getDefaultWidth(), statisticsTable.getDefaultHeight()));
        tableScrollPane.setViewportView(statisticsTable);
        getContentPane().add(tableScrollPane, BorderLayout.CENTER);

        //  Build title
        String title = "During  " + Utils.formatDuration(globalStatistics.getDuration()) +
                "      " + failedServers.size();
        if (failedServers.size() <= 1)
            title += "  server has failed";
        else
            title += "  servers have failed";
        titleLabel.setText(title);
        pack();
    }
    //=======================================================
    //=======================================================

    //=======================================================
    //=======================================================
    private void customizeMenus() {
        fileMenu.setMnemonic('F');
        readItem.setMnemonic('R');
        readItem.setAccelerator(KeyStroke.getKeyStroke('R', Event.CTRL_MASK));
        openItem.setMnemonic('O');
        openItem.setAccelerator(KeyStroke.getKeyStroke('O', Event.CTRL_MASK));
        saveItem.setMnemonic('S');
        saveItem.setAccelerator(KeyStroke.getKeyStroke('S', Event.CTRL_MASK));

        String superTango = System.getenv("SUPER_TANGO");
        if (superTango != null && superTango.toLowerCase().equals("true")) {
            resetItem.setMnemonic('R');
            resetItem.setAccelerator(KeyStroke.getKeyStroke('R', Event.ALT_MASK));
        } else
            resetItem.setVisible(false);

        exitItem.setMnemonic('E');
        exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', Event.CTRL_MASK));

        editMenu.setMnemonic('E');
        showMenu.setMnemonic('S');
        bottomPanel.setVisible(false);
    }
    //=======================================================

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    //=======================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        javax.swing.JScrollPane globalStatScrollPane = new javax.swing.JScrollPane();
        globalStatTextArea = new javax.swing.JTextArea();
        bottomPanel = new javax.swing.JPanel();
        javax.swing.JLabel filterLabel = new javax.swing.JLabel();
        filterText = new javax.swing.JTextField();
        javax.swing.JMenuBar jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        readItem = new javax.swing.JMenuItem();
        openItem = new javax.swing.JMenuItem();
        saveItem = new javax.swing.JMenuItem();
        resetItem = new javax.swing.JMenuItem();
        exitItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem filterItem = new javax.swing.JMenuItem();
        showMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem datesItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem errorItem = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        topPanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        titleLabel.setText("Title");
        topPanel.add(titleLabel, java.awt.BorderLayout.PAGE_END);

        globalStatScrollPane.setPreferredSize(new java.awt.Dimension(250, 110));

        globalStatTextArea.setColumns(20);
        globalStatTextArea.setEditable(false);
        globalStatTextArea.setFont(new java.awt.Font("Monospaced", 1, 12)); // NOI18N
        globalStatTextArea.setRows(5);
        globalStatScrollPane.setViewportView(globalStatTextArea);

        topPanel.add(globalStatScrollPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        filterLabel.setText("Filter :  ");
        bottomPanel.add(filterLabel);

        filterText.setColumns(20);
        filterText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                filterTextKeyPressed(evt);
            }
        });
        bottomPanel.add(filterText);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        fileMenu.setText("File");

        readItem.setText("Read Whole Statistics");
        readItem.setActionCommand("read");
        readItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readItemActionPerformed(evt);
            }
        });
        fileMenu.add(readItem);

        openItem.setText("Open");
        openItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openItemActionPerformed(evt);
            }
        });
        fileMenu.add(openItem);

        saveItem.setText("Save");
        saveItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveItem);

        resetItem.setText("Reset Statistics");
        resetItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetItemActionPerformed(evt);
            }
        });
        fileMenu.add(resetItem);

        exitItem.setText("Exit");
        exitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitItem);

        jMenuBar1.add(fileMenu);

        editMenu.setText("Edit");

        filterItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        filterItem.setText("Find Server");
        filterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterItemActionPerformed(evt);
            }
        });
        editMenu.add(filterItem);

        jMenuBar1.add(editMenu);

        showMenu.setText("Show");

        datesItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        datesItem.setText("Show Reset Dates");
        datesItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datesItemActionPerformed(evt);
            }
        });
        showMenu.add(datesItem);

        errorItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        errorItem.setText("Show Errors");
        errorItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                errorItemActionPerformed(evt);
            }
        });
        showMenu.add(errorItem);

        jMenuBar1.add(showMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void openItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openItemActionPerformed
        initChooser("Open");
        int retval = chooser.showOpenDialog(this);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                if (!file.isDirectory()) {
                    try {
                        String filename = file.getAbsolutePath();
                        globalStatistics = new GlobalStatistics(filename);
                        displayGlobalStatistics();
                    } catch (DevFailed e) {
                        ErrorPane.showErrorMessage(this, null, e);
                    }
                }
            }
        }
    }//GEN-LAST:event_openItemActionPerformed

    //=======================================================
    //=======================================================
    private void initChooser(String str) {
        if (chooser == null) {
            String path = System.getProperty("FILES");
            if (path == null)
                path = "";
            chooser = new JFileChooser(new File(path).getAbsolutePath());
            chooser.setFileFilter(fileFilter);
        }
        chooser.setApproveButtonText(str);
    }

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void saveItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveItemActionPerformed

        if (globalStatistics == null)
            return;
        initChooser("Save");
        int retval = chooser.showOpenDialog(this);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                if (!file.isDirectory()) {
                    //  Get target file name
                    String fileName = file.getAbsolutePath();
                    if (!fileName.endsWith(".xml"))
                        fileName += ".xml";

                    //  Check if already exists
                    if (new File(fileName).exists()) {
                        if (JOptionPane.showConfirmDialog(this,
                                fileName + "   File already exists\n\n     Overwrite it ?",
                                "Confirm Dialog",
                                JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION) {
                            return;
                        }

                    }

                    //  Save data
                    try {
                        globalStatistics.saveStatistics(fileName);
                    } catch (DevFailed e) {
                        ErrorPane.showErrorMessage(this, null, e);
                    }
                }
            }
        }
    }//GEN-LAST:event_saveItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void exitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitItemActionPerformed
        if (parent == null)
            System.exit(0);
        else
            setVisible(false);
    }//GEN-LAST:event_exitItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        if (parent == null)
            System.exit(0);
        else
            setVisible(false);
    }//GEN-LAST:event_exitForm

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void readItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readItemActionPerformed

        readAndDisplayStatistics(null);
    }//GEN-LAST:event_readItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void errorItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorItemActionPerformed

        //  Build error list if global stat have been read
        StringBuilder sb = new StringBuilder();
        if (globalStatistics != null) {
            for (StarterStat starterStat : globalStatistics.getStarterStatistics()) {
                if (!starterStat.readOK) {
                    sb.append(starterStat.name).append(":\t").append(starterStat.error).append("\n");
                }
            }
        }

        //  if nothing, special message
        if (sb.length() == 0)
            sb.append("No Eror.");
        new PopupText(this, true).show(sb.toString());
    }//GEN-LAST:event_errorItemActionPerformed

    //=======================================================
    //=======================================================
    private void filterTextKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterTextKeyPressed
        if (evt.getKeyChar() == 27) { //    Escape
            resetFilter();
        } else {
            //  Delayed a bit to be able to read text
            new DelayedDisplay(evt).start();
        }
    }//GEN-LAST:event_filterTextKeyPressed

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void filterItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterItemActionPerformed
        if (statisticsTable != null) {
            bottomPanel.setVisible(true);
            pack();
            new DelayedDisplay().start();
        }
    }//GEN-LAST:event_filterItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void resetItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetItemActionPerformed

        new ResetStatistics(this);
    }//GEN-LAST:event_resetItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void datesItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datesItemActionPerformed

        if (globalStatistics!=null) {
            try {
                new PopupTable(this, null,
                        new String[] { "Host", "Statistics starting Date"},
                        globalStatistics.getStarterResetDates()).setVisible(true);
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
            }
        }
    }//GEN-LAST:event_datesItemActionPerformed

    //=======================================================
    //=======================================================
    private void resetFilter() {
        if (statisticsTable != null) {
            statisticsTable.resetFilter();
            filterText.setText("");
            bottomPanel.setVisible(false);
            pack();
        }
    }

    //=======================================================
    //=======================================================
    private List<ServerStat> getServerFailedList(List<StarterStat> starterStats) {
        List<ServerStat> serverStats = new ArrayList<>();
        for (StarterStat starterStat : starterStats) {
            for (ServerStat server : starterStat) {
                if (server.nbFailures > 0) {
                    serverStats.add(server);
                }
            }
        }
        return serverStats;
    }
    //=======================================================
    /**
     * @param args the command line arguments
     */
    //=======================================================
    public static void main(String args[]) {
        try {
            if (args.length > 0)
                new StatisticsPanel(null, args[0]).setVisible(true);
            else
                new StatisticsPanel().setVisible(true);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(new JFrame(), null, e);
        }
    }


    //=======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JTextField filterText;
    private javax.swing.JTextArea globalStatTextArea;
    private javax.swing.JMenuItem openItem;
    private javax.swing.JMenuItem readItem;
    private javax.swing.JMenuItem resetItem;
    private javax.swing.JMenuItem saveItem;
    private javax.swing.JMenu showMenu;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    //=======================================================













    //=======================================================
    //=======================================================
    private class ReadThread extends Thread {
        private ArrayList<String> hostList;

        //===================================================
        private ReadThread(ArrayList<String> hostList) {
            this.hostList = hostList;
        }
        //===================================================
        public void run() {
            AstorUtil.startSplash("Statistics ");
            AstorUtil.increaseSplashProgress(5, "Reading....");
            setCursor(new Cursor(Cursor.WAIT_CURSOR));

            //  Read Statistics for all controlled starters
            List<StarterStat> starterStatistics = Utils.readHostStatistics(hostList);
            Collections.sort(starterStatistics, new CompareStarterResetTime());
            globalStatistics = new GlobalStatistics(starterStatistics);

            displayGlobalStatistics();

            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            AstorUtil.stopSplash();
        }
        //===================================================
    }
    //===============================================================
    //===============================================================




    //======================================================
    /**
     * Compare class to sort collection by reset time
     */
    //======================================================
    class CompareStarterResetTime implements Comparator<StarterStat> {
        public int compare(StarterStat stat1, StarterStat stat2) {

            if  (stat1.resetTime==stat2.resetTime)   return 0;
            return (stat1.resetTime>stat2.resetTime)? 1 : -1;
        }
    }







    private String filter = "";
    //===============================================================
    /**
     * A Thread class to manage JTextField a bit later.
     * After main loop update.
     */
    //===============================================================
    private class DelayedDisplay extends Thread {
        private KeyEvent evt = null;

        //===============================================================
        private DelayedDisplay() {
            //
        }

        private DelayedDisplay(KeyEvent evt) {
            this.evt = evt;
        }

        //===============================================================
        public void run() {
            try {
                sleep(10);
            } catch (InterruptedException e) {/*  */}

            if (evt == null) {
                filterText.requestFocus();
            } else {
                char c = evt.getKeyChar();
                if ((c & 0x8000) == 0) { //  not Ctrl, Shift,...
                    String s = filterText.getText();
                    //System.out.println(c+ "  " + ((int)c));
                    if (!filter.equals(s)) { // Has changed
                        if (s.length() > 0) {
                            statisticsTable.setFilter(s);
                        } else
                            statisticsTable.resetFilter();
                    }
                    filter = s;
                }
            }
        }
    }
}
