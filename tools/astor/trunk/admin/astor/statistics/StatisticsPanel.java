//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for main swing class.
//
// $Author: verdier $
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
//-======================================================================


package admin.astor.statistics;

import admin.astor.AstorUtil;
import admin.astor.tools.PopupText;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Vector;

//=======================================================
/**
 *	JFrame Class to display info
 *
 * @author  Pascal Verdier
 */
//=======================================================
public class StatisticsPanel extends JFrame
{
	static private JFileChooser  chooser = null;
    private static final StatisticsFileFilter	fileFilter =
            new StatisticsFileFilter("xml", "Statistics Files");
    private JFrame  parent = null;
    private GlobalStatistics    globalStatistics;



    //=======================================================
    /**
	 *	Creates new form StatisticsPanel
     * @param parent JFrame parent instance (if null, exit at exitBtn clicked)
     * @param fileName file's name to load statistics.
     * @throws DevFailed if read or load statistices from file failed.
	 */
	//=======================================================
    public StatisticsPanel(JFrame parent, String fileName) throws DevFailed
	{
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
	 *	Creates new form StatisticsPanel
	 */
	//=======================================================
    public StatisticsPanel()
	{
        this(null);
    }
    //=======================================================
    /**
	 *	Creates new form StatisticsPanel
     * @param parent JFrame parent instance (if null, exit at exitBtn clicked)
	 */
	//=======================================================
    public StatisticsPanel(JFrame parent)
	{
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
    public void readAndDisplayStatistics(Vector<String> hostList)
    {
        titleLabel.setText("Reading and Computing Statistics");
        new ReadThread(hostList).start();
    }
    //=======================================================
    //=======================================================
    private class ReadThread extends Thread
    {
        private Vector<String> hostList;
        //===================================================
        private ReadThread(Vector<String> hostList)
        {
            this.hostList = hostList;
        }
        //===================================================
        public void run() {
            AstorUtil.startSplash("Statistics ");
            AstorUtil.increaseSplashProgress(5, "Reading....");
            setCursor(new Cursor(Cursor.WAIT_CURSOR));

            //  Read Statistics for all controlled starters
             Vector<StarterStat> starterStatistics = Utils.readHostStatistics(hostList);
             globalStatistics = new GlobalStatistics(starterStatistics);

             displayGlobalStatistics();

            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            pack();
            AstorUtil.stopSplash();
        }

    }
    //=======================================================
    //=======================================================
    private void displayGlobalStatistics()
    {
        //  Build the server failed list and display it in a table
        Vector<ServerStat>  failedServers = getServerFailedList(
                globalStatistics.getStarterStatistics());
        GlobalStatisticsTable statisticsTable = new GlobalStatisticsTable(this);
        statisticsTable.setStatistics(failedServers);
        globalStatTextArea.setText(globalStatistics.toString());

        //  Put it in a scrolled pane.
        JScrollPane scp = new JScrollPane();
        scp.setPreferredSize(new Dimension(
                GlobalStatisticsTable.getDefaultWidth(), statisticsTable.getDefaultHeight()));
        scp.setViewportView(statisticsTable);
        getContentPane().add(scp, BorderLayout.CENTER);

        //  Build title
        String title = "During  " + Utils.formatDuration(globalStatistics.getDuration()) +
                "      " + failedServers.size();
        if (failedServers.size()<=1)
            title += "  server has failed";
        else
            title += "  servers have failed";
        titleLabel.setText(title);
	}
	//=======================================================
	//=======================================================

	//=======================================================
	//=======================================================
	private void customizeMenus()
	{
		fileMenu.setMnemonic ('F');
		readItem.setMnemonic ('R');
		readItem.setAccelerator(KeyStroke.getKeyStroke('R', Event.CTRL_MASK));
		openItem.setMnemonic ('O');
		openItem.setAccelerator(KeyStroke.getKeyStroke('O', Event.CTRL_MASK));
		saveItem.setMnemonic ('S');
		saveItem.setAccelerator(KeyStroke.getKeyStroke('S', Event.CTRL_MASK));
		exitItem.setMnemonic ('E');
		exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', Event.CTRL_MASK));

        showMenu.setMnemonic ('S');
        errorItem.setMnemonic ('E');
        errorItem.setAccelerator(KeyStroke.getKeyStroke('E', Event.CTRL_MASK));
	}
	//=======================================================
    /** This method is called from within the constructor to
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
        javax.swing.JMenuBar jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        readItem = new javax.swing.JMenuItem();
        openItem = new javax.swing.JMenuItem();
        saveItem = new javax.swing.JMenuItem();
        exitItem = new javax.swing.JMenuItem();
        showMenu = new javax.swing.JMenu();
        errorItem = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        topPanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        titleLabel.setText("Title");
        topPanel.add(titleLabel, java.awt.BorderLayout.SOUTH);

        globalStatScrollPane.setPreferredSize(new java.awt.Dimension(250, 95));

        globalStatTextArea.setColumns(20);
        globalStatTextArea.setEditable(false);
        globalStatTextArea.setFont(new java.awt.Font("Monospaced", 1, 12)); // NOI18N
        globalStatTextArea.setRows(5);
        globalStatScrollPane.setViewportView(globalStatTextArea);

        topPanel.add(globalStatScrollPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

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

        exitItem.setText("Exit");
        exitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitItem);

        jMenuBar1.add(fileMenu);

        showMenu.setText("Show");

        errorItem.setText("Errors");
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
		int	retval = chooser.showOpenDialog(this);
		if (retval==JFileChooser.APPROVE_OPTION) {
			File	file = chooser.getSelectedFile();
			if (file!=null) {
				if (!file.isDirectory()) {
					String	filename = file.getAbsolutePath();
					System.out.println(filename);
				}
			}
		}
    }//GEN-LAST:event_openItemActionPerformed
    //=======================================================
    //=======================================================
    private void initChooser(String str)
    {
        if (chooser==null) {
            String  path = System.getProperty("FILES");
            if (path==null)
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

        if (globalStatistics==null)
            return;
        initChooser("Save");
        int	retval = chooser.showOpenDialog(this);
        if (retval==JFileChooser.APPROVE_OPTION) {
            File	file = chooser.getSelectedFile();
            if (file!=null) {
                if (!file.isDirectory()) {
                    //  Get target file name
                    String	fileName = file.getAbsolutePath();
                    if (!fileName.endsWith(".xml"))
                        fileName += ".xml";

                    //  Check if already exists
                    if (new File(fileName).exists()) {
                        if (JOptionPane.showConfirmDialog(parent,
                                fileName + "   File already exists\n\n     Overwrite it ?",
                                "Confirm Dialog",
                                JOptionPane.YES_NO_OPTION)!=JOptionPane.OK_OPTION) {
                            return;
                        }

                    }

                    //  Save data
                    try {
                        globalStatistics.saveStatistics(fileName);
                    }
                    catch(DevFailed e) {
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
        if (parent==null)
            System.exit(0);
        else
            setVisible(false);
    }//GEN-LAST:event_exitItemActionPerformed

	//=======================================================
	//=======================================================
    @SuppressWarnings({"UnusedDeclaration"})
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        if (parent==null)
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
        StringBuffer    sb = new StringBuffer();
        if (globalStatistics!=null) {
            for (StarterStat starterStat : globalStatistics.getStarterStatistics()) {
                if (!starterStat.readOK) {
                    sb.append(starterStat.name).append(":\t").append(starterStat.error).append("\n");
                }
            }
        }

        //  if nothing, special message
        if (sb.length()==0)
            sb.append("No Eror.");
        new PopupText(this, true).show(sb.toString());
    }//GEN-LAST:event_errorItemActionPerformed
    //=======================================================
    //=======================================================
    private Vector<ServerStat>  getServerFailedList(Vector<StarterStat> starterStats)
    {
        Vector<ServerStat>  serverStats = new Vector<ServerStat>();
        for (StarterStat starterStat : starterStats) {
            for (ServerStat server : starterStat) {
                if (server.nbFailures>0) {
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
            if (args.length>0)
                new StatisticsPanel(null, args[0]).setVisible(true);
            else
                new StatisticsPanel().setVisible(true);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(new JFrame(), null, e);
        }
    }


	//=======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem errorItem;
    private javax.swing.JMenuItem exitItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JTextArea globalStatTextArea;
    private javax.swing.JMenuItem openItem;
    private javax.swing.JMenuItem readItem;
    private javax.swing.JMenuItem saveItem;
    private javax.swing.JMenu showMenu;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//=======================================================

}
