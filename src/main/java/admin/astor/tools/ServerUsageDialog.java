//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
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
// $Log:  $
//
//-======================================================================

package admin.astor.tools;

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DbServer;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


//===============================================================

/**
 * JDialog Class to display info
 *
 * @author Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class ServerUsageDialog extends JDialog {

    private JFrame parent;
    private List<TangoClass> tangoClasses = new ArrayList<>();
    private List<Domain> domains = new ArrayList<>();
    private int nbServers;
    private String wildcard;
    private String urlFile;
    private int serversCounter = 0;

    private static final String header =
            "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">\n" +
                    "<HTML>\n" +
                    "<HEAD>\n" +
                    "<Title> title </Title>\n" +
                    "</HEAD>\n" +
                    "<BODY TEXT=\"#000000\" BGCOLOR=\"#FFFFFF\" LINK=\"#0000FF\" VLINK=\"#7F00FF\" ALINK=\"#FF0000\">\n" +
                    "<P><!-------TITLE------></P>\n";
    private static final String footer =
            "</Body>\n" +
                    "</Html>\n";
    //===============================================================
    /**
     * Creates new form ServerUsageDialog
     *
     * @param parent the parent instance
     * @throws DevFailed if Database commands fail
     */
    //===============================================================
    public ServerUsageDialog(JFrame parent) throws DevFailed {
        super(parent, true);
        this.parent = parent;
        initComponents(null);
    }
    //===============================================================
    /**
     * Creates new form ServerUsageDialog
     *
     * @param parent the parent instance
     * @throws DevFailed if Database commands fail
     */
    //===============================================================
    @SuppressWarnings("WeakerAccess")
    public ServerUsageDialog(JFrame parent, String server) throws DevFailed {
        super(parent, true);
        this.parent = parent;
        initComponents(server);
    }
    //===============================================================
    //===============================================================
    private void initComponents(String server) throws DevFailed {
        initComponents();
        displayServerUsage(server);
        ATKGraphicsUtils.centerDialog(this);
    }
    //===============================================================
    //===============================================================
    private void displayServerUsage(String inputServer) throws DevFailed {
        List<String> serverList;
        if (inputServer!=null) {
            serverList = new ArrayList<>();
            serverList.add(inputServer);
        }
        else {
            //  Get a server list from database for selection
            String[] servers = ApiUtil.get_db_obj().get_server_name_list();
            ListDialog dialog = new ListDialog(parent,
                    "Servers in " + ApiUtil.getTangoHost(), servers);
            serverList = dialog.showDialog();
            if (serverList==null || serverList.isEmpty()) {
                doClose();
                return;
            }
        }
        try {
            AstorUtil.startSplash(serverList.get(0));
            for (String server : serverList) {
                wildcard = server;

                //  Convert to a wildcard and clear previous computation results
                wildcard += "/*";
                titleLabel.setText(wildcard);
                tangoClasses.clear();
                domains.clear();

                //  Get existing server/instances list
                String[] serverNames = ApiUtil.get_db_obj().get_server_list(wildcard);
                nbServers = serverNames.length;

                //  And for each one, distribute by class
                for (String serverName : serverNames) {
                    AstorUtil.increaseSplashProgress(1, serverName);
                    fillTangoClasses(serverName);

                    //  Sleep a bit to do not overload database
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) { /* */ }
                }
                //  Build tml code and url and put it in edito pane
                URL url = new URL(buildTmpFile(toHtml()));
                JEditorPane editorPane = new JEditorPane();
                editorPane.setEditable(false);
                editorPane.setPage(url);

                //  Then pane scrollable pane and in a tabbed pane
                JScrollPane scrollPane = new JScrollPane(editorPane);
                scrollPane.setPreferredSize(new Dimension(480, 440));
                tabbedPane.add(scrollPane);
                tabbedPane.setTitleAt(serversCounter++, server);
                tabbedPane.setSelectedComponent(scrollPane);
            }
            AstorUtil.stopSplash();

        }
        catch (IOException e) {
            Except.throw_exception("URL failed", e.getMessage());
        }
        //new PopupHtml(parent, false).show(toHtml());
    }
    //======================================================
    //======================================================
    private String buildTmpFile(String code) {
        String urlStr = null;
        try {
            int random_value = new java.util.Random().nextInt(30000);
            String tmpDir = System.getProperty("java.io.tmpdir");
            urlFile = tmpDir + "/html." + random_value;
            FileOutputStream outputStream = new FileOutputStream(urlFile);
            outputStream.write((header + code + footer).getBytes());
            outputStream.close();

            urlStr = "file:" + urlFile;
        } catch (Exception e) {
            ErrorPane.showErrorMessage(parent, null, e);
            e.printStackTrace();
        }
        return urlStr;
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
        titleLabel = new javax.swing.JLabel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton anotherBtn = new javax.swing.JButton();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Dialog Title");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        anotherBtn.setText("Another Server");
        anotherBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anotherBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(anotherBtn);

        jLabel1.setText("                              ");
        bottomPanel.add(jLabel1);

        cancelBtn.setText("Dismiss");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);
        getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        doClose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    //===============================================================
    //===============================================================
    @SuppressWarnings({"UnusedParameters"})
    private void anotherBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anotherBtnActionPerformed
        try {
            displayServerUsage(null);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }
    }//GEN-LAST:event_anotherBtnActionPerformed

    //===============================================================
    /**
     * Closes the dialog
     */
    //===============================================================
    private void doClose() {

        //  Remove url file if exists
        try {
            if (urlFile!=null)
                if (!new File(urlFile).delete())
                    System.err.println("Cannot delete " + urlFile);
        } catch (Exception e) {
            ErrorPane.showErrorMessage(parent, null, e);
        }

        if (parent == null)
            System.exit(0);
        else {
            setVisible(false);
            dispose();
        }
    }

    //===============================================================
    //===============================================================
    public void showDialog() {
        setVisible(true);
    }
    //===============================================================
    //===============================================================

    //===============================================================
    //===============================================================
    private void fillTangoClasses(String serverName) throws DevFailed {
        DbServer server = new DbServer(serverName);
        String[] class_dev = server.get_device_class_list();
        boolean doneForServer = false;


        for (int i=0 ; i<class_dev.length ; i += 2) {
            String deviceName = class_dev[i];
            String className  = class_dev[i + 1];
            if (!className.equals("DServer")) {
                //  Check if class already created
                TangoClass _class = null;
                for (TangoClass tangoClass : tangoClasses) {
                    if (tangoClass.name.toLowerCase().equals(className.toLowerCase()))
                        _class = tangoClass;
                }
                //  If not found, create it
                if (_class == null) {
                    _class = new TangoClass(className);
                    tangoClasses.add(_class);
                }
                //  and add device
                _class.add(deviceName);
                Domain domain = getDomain(deviceName);
                domain.add(deviceName);

                if (!doneForServer) {
                    domain.addServer();
                    doneForServer = true;
                }
            }
        }
    }

    //===============================================================
    //===============================================================
    private Domain getDomain(String deviceName) throws DevFailed {
        Domain domain = null;
        String domainName = deviceName.substring(0, deviceName.indexOf("/")).toLowerCase();

        //  Check if domain already created
        for (Domain d : domains) {
            if (d.name.equalsIgnoreCase(domainName))
                domain = d;
        }
        if (domain == null) {
            domain = new Domain(domainName);
            domains.add(domain);
        }
        return domain;
    }
    //===============================================================
    //===============================================================
    private String toHtml() {
        int nbDevices = 0;
        for (TangoClass tangoClass : tangoClasses) {
            nbDevices += tangoClass.size();
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<font size+=1><b>").append(nbDevices).append(" devices for ").
                append(nbServers).append(" server instances </u></b></font>\n");
        sb.append("<br><br>\n");

        //  On table containing 2 tables
        sb.append("<table border=0> <td>\n");

        //  One table by domain with instances
        sb.append("<font size+=1><b><u>By Domain:</u></b></font>\n");
        sb.append("<table border=1 cellSpacing=0>\n");
        sb.append("<td><b>Domain</b></td> <td><b>Instances</b></td> <td><b>Devices</b></td>\n");
        for (Domain domain : domains) {
            sb.append("<tr> <td> ").append(domain.name).append(" </td> <td>")
                    .append(domain.nbServers).append(" </td> <td>")
                    .append(domain.size()).append(" </td> </tr>\n");
        }
        sb.append("</table>\n");
        sb.append("</td> <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td> <td>\n");

        //  One table devices / class
        sb.append("<font size+=1><b><u>Devices by Class:</u></b></font>\n");
        sb.append("<table border=1 cellSpacing=0>\n");
        sb.append("<td><b>Class name</b></td> <td><b>Devices</b></td>\n");
        for (TangoClass tangoClass : tangoClasses) {
            sb.append("<tr> <td> ").append(tangoClass.name).append(" </td> <td>")
                    .append(tangoClass.size()).append(" </td> </tr>\n");
        }
        sb.append("</table>\n");
        sb.append("</td></table>\n");

        return sb.toString();
    }
    //===============================================================
    //===============================================================
    public String toString() {
        int nbDevices = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(wildcard).append("\n");
        sb.append("\n=====================================\n");
        for (TangoClass tangoClass : tangoClasses) {
            sb.append(tangoClass);
            nbDevices += tangoClass.size();
        }
        sb.append("\n=====================================\n");
        for (Domain domain : domains) {
            sb.append("Domain ").append(domain.name).append(":\t");
            sb.append(domain.size()).append(" devices\t");
            sb.append(domain.nbServers).append(" servers\n");
        }
        sb.append("\n=====================================\n");
        sb.append(nbServers).append("\tservers\n");
        sb.append(nbDevices).append("\tdevices\n");
        return sb.toString();
    }
    //===============================================================
    //===============================================================

    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    //===============================================================


    //===============================================================
    /**
     * @param args the command line arguments
     */
    //===============================================================
    public static void main(String args[]) {
        try {
            //String serverName = "VacGaugeServer";
            new ServerUsageDialog(null, null).setVisible(true);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(new JFrame(), null, e);
        }
    }
    //===============================================================
    //===============================================================


    //===============================================================
    //===============================================================
    private class Domain extends ArrayList<String> {
        private String name;
        private int nbServers = 0;

        //===========================================================
        private Domain(String name) {
            this.name = name;
        }

        //===========================================================
        private void addServer() {
            nbServers++;
        }
        //===========================================================
        public String toString() {
            return "Domain " + name + ":\t" + size() + " devices\n";
        }
        //===========================================================
    }

    //===============================================================
    //===============================================================
    private class TangoClass extends ArrayList<String> {
        private String name;

        //===========================================================
        private TangoClass(String name) {
            this.name = name;
        }

        //===========================================================
        public String toString() {
            /*
            for (String deviceName : this) {
                sb.append("  -  ").append(deviceName).append("\n");
            }
            */
            return "Class " + name + ":\t" + size() + " devices\n";
        }
        //===========================================================
    }
    //===============================================================
    //===============================================================
}
