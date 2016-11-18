//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:	java source code for display JTree
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
// $Revision: 1.2 $
//
// $Log:  $
//
//-======================================================================

package admin.astor.tango_release;

import admin.astor.Astor;
import admin.astor.AstorUtil;
import admin.astor.tools.Utils;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;



public class TangoReleaseTree extends JTree implements TangoConst {
    private JFrame appli;
    private DefaultMutableTreeNode root;
    private TangoReleaseTreePopupMenu menu;
    private int mode;
    private TangoServerReleaseList  serverReleaseList;
    private static JFileChooser fileChooser = null;

    //  ToDo add here new release to be checked.
    private static final double[]   tangoReleases = { 1.0, 2.0, 5.0, 5.2, 7.0, 8.0, 8.1 };
    private static final int[]      idlReleases   = { 1, 2, 3, 4, 5 };

    private static ImageIcon networkIcon;
    private static ImageIcon tangoIcon;
    private static ImageIcon serverIcon;
    private static ImageIcon classIcon;
    //===============================================================
    //===============================================================
    public TangoReleaseTree(JFrame frame, String rootName,
                            TangoServerReleaseList serverReleaseList, int mode) {
        this.appli = frame;
        this.mode   = mode;
        this.serverReleaseList = serverReleaseList;

        networkIcon = Utils.getInstance().getIcon("TangoClass.gif", 0.33);
        tangoIcon = Utils.getInstance().getIcon("TangoClass.gif", 0.125);
        serverIcon = Utils.getInstance().getIcon("server.gif");
        classIcon = Utils.getInstance().getIcon("class.gif");
        buildTree(rootName);
        menu = new TangoReleaseTreePopupMenu(this);
        //expandChildren(root);
        setSelectionPath(null);

        fileChooser = new JFileChooser(new File("").getAbsolutePath());
    }

    //===============================================================
    //===============================================================
    private void buildTree(String rootName) {
        //  Create the nodes.
        root = new DefaultMutableTreeNode(rootName);
        createCollectionClassNodes();

        //	Create the tree that allows one selection at a time.
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //	Create Tree and Tree model
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);

        //  Set the icon for leaf nodes.
        TangoRenderer renderer = new TangoRenderer();
        setCellRenderer(renderer);

        //	Listen for collapse tree
        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
                //collapsedPerformed(e);
            }

            public void treeExpanded(TreeExpansionEvent e) {
                expandedPerformed(e);
            }
        });
        //	Add Action listener
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);    //	for tree clicked, menu,...
            }
        });
    }
    //======================================================
    /*
     * Manage event on clicked mouse on JTree object.
     */
    //======================================================
    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
        //	Set selection at mouse position
        TreePath selectedPath = getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath == null)
            return;

        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount() - 1);
        Object userObject = node.getUserObject();
        int mask = evt.getModifiers();

        //  Check button clicked
        if ((mask & MouseEvent.BUTTON3_MASK) != 0) {
            /*
            if (node == root)
                menu.showMenu(evt, (String) userObject);
            else
            */
             if (userObject instanceof ServerCollectionClass)
                menu.showMenu(evt, (ServerCollectionClass) userObject);
            else
             if (userObject instanceof IdlCollectionClass)
                menu.showMenu(evt, (IdlCollectionClass) userObject);
            else
             if (userObject instanceof Executable)
                menu.showMenu(evt, (Executable) userObject);
            else
            if (userObject instanceof Instance)
                if (appli instanceof Astor)
                menu.showMenu(evt, (Instance) userObject);
        }
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    public void expandedPerformed(TreeExpansionEvent evt) {
    }

    //===============================================================
    //===============================================================
    private List<ServerCollectionClass> initCollectionsByTangoRelease() {
        List<ServerCollectionClass> serverCollectionClasses = new ArrayList<>();

        //  Do it for specific release
        for (double tangoRelease : tangoReleases) {
            List<TangoServerRelease>   servers =
                    serverReleaseList.getServersForTangoRelease(tangoRelease);
            if (servers.size()>0) {
                String text = "Tango-"+tangoRelease;
                if (tangoRelease==7.0)
                    text = "Tango-7.x";
                else
                if (tangoRelease==5.2)
                    text += " or 6.x";
                serverCollectionClasses.add(new ServerCollectionClass(text, servers));
            }
        }
        //  Do it for releases > 9.0 (the real release is now given by c++ lib)
        for (int i=90 ; i<150 ; i++) {
            double tangoRelease = 0.1*i;
            List<TangoServerRelease> servers =
                    serverReleaseList.getServersForTangoRelease(tangoRelease);
            if (servers.size()>0) {
                String text = "Tango-" + tangoRelease;
                serverCollectionClasses.add(new ServerCollectionClass(text, servers));
            }
        }
        //  Add a collection for failed ones.
        List<TangoServerRelease> onError = serverReleaseList.getServersOnError();
        if (onError.size()>0)
            serverCollectionClasses.add(new ServerCollectionClass("Failed", onError));

        return serverCollectionClasses;
    }
    //===============================================================
    //===============================================================
    private List<IdlCollectionClass> initCollectionsByIdlRelease() {

        List<IdlCollectionClass> idlCollectionClasses = new ArrayList<>();
        for (int idlRelease : idlReleases) {
            List<TangoClassRelease>   servers =
                    serverReleaseList.getClassesForIdlRelease(idlRelease);
            if (servers.size()>0)
                idlCollectionClasses.add(new IdlCollectionClass("Device_"+idlRelease+"Impl", servers));
        }
        return idlCollectionClasses;
    }

    //===============================================================
    //===============================================================
    private void createCollectionClassNodes() {
        //  Build collections (depending on mode)
        switch (mode) {
            case TangoReleaseDialog.byTango:
                List<ServerCollectionClass> serverCollectionClasses = initCollectionsByTangoRelease();

                //  Build collection nodes
                for (ServerCollectionClass serverCollectionClass : serverCollectionClasses) {
                    DefaultMutableTreeNode tangoNode =
                            new DefaultMutableTreeNode(serverCollectionClass);
                    root.add(tangoNode);

                    //  Declare a map to split server name on executable/instance
                    HashMap<String, DefaultMutableTreeNode> executableNodes = new HashMap<>();

                    //  Build server nodes
                    for (TangoServerRelease serverRelease : serverCollectionClass.servers) {

                        DefaultMutableTreeNode  exeNode = executableNodes.get(serverRelease.exeName);
                        if (exeNode==null) {
                            exeNode = new DefaultMutableTreeNode(new Executable(serverRelease.exeName));
                            executableNodes.put(serverRelease.exeName, exeNode);
                            tangoNode.add(exeNode);
                        }

                        DefaultMutableTreeNode  instanceNode = new DefaultMutableTreeNode(new Instance(serverRelease));
                        exeNode.add(instanceNode);

                        //  Build class nodes.
                        for (TangoClassRelease classRelease : serverRelease) {
                            instanceNode.add(new DefaultMutableTreeNode(classRelease));
                        }
                    }
                }
                break;
            case TangoReleaseDialog.byIDL:
                List<IdlCollectionClass> idlCollectionClasses = initCollectionsByIdlRelease();
                for (IdlCollectionClass idlCollectionClass : idlCollectionClasses) {
                    DefaultMutableTreeNode idlNode =
                            new DefaultMutableTreeNode(idlCollectionClass);
                    root.add(idlNode);
                    for (TangoClassRelease classRelease : idlCollectionClass.classes) {
                        DefaultMutableTreeNode classNode =
                                new DefaultMutableTreeNode(classRelease);
                        idlNode.add(classNode);
                        classNode.add(new DefaultMutableTreeNode(classRelease.serverName));
                    }
                }
                break;
        }
    }

    //======================================================
    //======================================================
    private DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) getLastSelectedPathComponent();
    }

    //======================================================
    //======================================================
    Object getSelectedObject() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null)
            return null;
        return node.getUserObject();
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedDeclaration")
    private void expandChildren(DefaultMutableTreeNode node) {
        boolean level_done = false;
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child =
                    (DefaultMutableTreeNode) node.getChildAt(i);
            if (child.isLeaf()) {
                if (!level_done) {
                    expandNode(child);
                    level_done = true;
                }
            } else
                expandChildren(child);
        }
    }

    //===============================================================
    //===============================================================
    private void expandNode(DefaultMutableTreeNode node) {
        List<DefaultMutableTreeNode> list = new ArrayList<>();
        list.add(node);
        while (node != root) {
            node = (DefaultMutableTreeNode) node.getParent();
            list.add(0, node);
        }
        TreeNode[] tn = list.toArray(new TreeNode[list.size()]);
        TreePath tp = new TreePath(tn);
        setSelectionPath(tp);
        scrollPathToVisible(tp);
    }
    //===============================================================
    //===============================================================
    private void saveCollection() {

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                if (!file.isDirectory()) {
                    String  fileName = file.getAbsolutePath();
                    if (new File(fileName).exists()) {
                        if (JOptionPane.showConfirmDialog(this,
                                "The File " + fileName + "  Already Exists !\n\n" +
                                "Would you like to overwrite ?",
                                "information",
                                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                            return;

                    }
                    String  title = getSelectedObject().toString();
                    String  text  = title + "\n\n" + getCollectionText();

                    try {
                        AstorUtil.writeFile(fileName, text);
                    }
                    catch (DevFailed e) {
                        ErrorPane.showErrorMessage(this, null, e);
                    }
                }
            }
        }
    }
    //===============================================================
    //===============================================================
    private void displayCollection() {
        String  title = getSelectedObject().toString();
        String  text = getCollectionText();
        admin.astor.tools.PopupText popupText =
                new admin.astor.tools.PopupText(appli, true);
        popupText.setTitle(title);
        popupText.addText(text);
        popupText.setSize(400, 500);
        ATKGraphicsUtils.centerDialog(popupText);
        popupText.setVisible(true);
    }
    //===============================================================
    //===============================================================
    private String getCollectionText() {

        DefaultMutableTreeNode  node = getSelectedNode();
        StringBuilder   sb = new StringBuilder();

        Object  object = node.getUserObject();
        if (object instanceof ServerCollectionClass) {
            for (int i=0 ; i<node.getChildCount() ; i++) {
                DefaultMutableTreeNode exeNode =
                        (DefaultMutableTreeNode) node.getChildAt(i);
                for (int j=0; j<exeNode.getChildCount() ; j++) {
                    DefaultMutableTreeNode instanceNode =
                            (DefaultMutableTreeNode) exeNode.getChildAt(j);
                    TangoServerRelease serverRelease =
                            ((Instance)instanceNode.getUserObject()).server;
                    sb.append(serverRelease.toStringFull()).append("\n");
                }
            }
        }
        else
        if (object instanceof IdlCollectionClass) {
            for (int i=0 ; i<node.getChildCount() ; i++) {
                DefaultMutableTreeNode classNode =
                        (DefaultMutableTreeNode) node.getChildAt(i);
                sb.append(classNode.toString()).append("\n");
            }
        }
        else
        if (object instanceof  Executable) {
            for (int i=0 ; i<node.getChildCount() ; i++) {
                DefaultMutableTreeNode instanceNode =
                        (DefaultMutableTreeNode) node.getChildAt(i);
                TangoServerRelease serverRelease =
                        ((Instance)instanceNode.getUserObject()).server;
                sb.append(serverRelease.toStringFull()).append("\n");
            }
        }
        return sb.toString();
    }
    //===============================================================
    //===============================================================






    //===============================================================
    /**
     *	Executable object definition
     */
    //===============================================================
    private class Executable {
        String name;

        //===========================================================
        private Executable(String name) {
            this.name = name;
        }

        //===========================================================
        public String toString() {
            return name;
        }
        //===========================================================
    }
    //===============================================================
    /**
     *	Instance object definition
     */
    //===============================================================
    private class Instance  {
        String name;
        TangoServerRelease server;

        //===========================================================
        private Instance(TangoServerRelease server) {
            this.server = server;
            this.name    = server.instanceName;
            if (server.releaseNumber>=1.0)
                name += "  (Tango-"+ String.format("%1.1f", server.releaseNumber) + ")";
            else
                name += "  (" + server.error + ")";
        }

        //===========================================================
        public String toString() {
            return name;
        }
        //===========================================================
    }

    //===============================================================
    /*
     *	ServerCollectionClass object definition
     */
    //===============================================================
    private class ServerCollectionClass {
        String name;
        List<TangoServerRelease>   servers;

        //===========================================================
        private ServerCollectionClass(String name, List<TangoServerRelease> servers) {
            this.name = name;
            this.servers = servers;
        }

        //===========================================================
        public String toString() {
            return name + "  (" + servers.size() + ")";
        }
        //===========================================================
    }
    //===============================================================
    /*
     *	IdlCollectionClass object definition
     */
    //===============================================================
    private class IdlCollectionClass {
        String name;
        List<TangoClassRelease>   classes;

        //===========================================================
        private IdlCollectionClass(String name, List<TangoClassRelease> classes) {
            this.name = name;
            this.classes = classes;
        }

        //===========================================================
        public String toString() {
            return name + "  (" + classes.size() + ")";
        }
        //===========================================================
    }
    //===============================================================
    /**
     * Renderer Class
     */
    //===============================================================
    private class TangoRenderer extends DefaultTreeCellRenderer {
        private Font[] fonts;

        private final int TITLE = 0;
        private final int COLLEC = 1;
        private final int SERVER = 2;
        private final int CLASS  = 3;

        //===============================================================
        //===============================================================
        public TangoRenderer() {

            fonts = new Font[CLASS + 1];
            fonts[TITLE]  = new Font("Dialog", Font.BOLD, 18);
            fonts[COLLEC] = new Font("Dialog", Font.BOLD, 14);
            fonts[SERVER] = new Font("Dialog", Font.BOLD, 12);
            fonts[CLASS]  = new Font("Dialog", Font.PLAIN, 12);
        }

        //===============================================================
        //===============================================================
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object obj,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, obj, sel,
                    expanded, leaf, row,
                    hasFocus);

            setBackgroundNonSelectionColor(Color.white);
            setForeground(Color.black);
            setBackgroundSelectionColor(Color.lightGray);
            if (row == 0) {
                //	ROOT
                setFont(fonts[TITLE]);
                setIcon(networkIcon);
            } else {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;

                if (node.getUserObject() instanceof ServerCollectionClass ||
                    node.getUserObject() instanceof IdlCollectionClass) {
                    setFont(fonts[COLLEC]);
                    setIcon(tangoIcon);
                }
                else
                if (node.getUserObject() instanceof Executable ||
                    node.getUserObject() instanceof Instance) {
                    setFont(fonts[SERVER]);
                    setIcon(serverIcon);
                }
                else
                if (node.getUserObject() instanceof TangoClassRelease) {
                    setFont(fonts[CLASS]);
                    setIcon(classIcon);
                }
            }
            return this;
        }
    }//	End of Renderer Class

    //==============================================================================
//==============================================================================
    static private final int ROOT_OPTION  = 0;
    static private final int IN_TEXT_AREA = 1;
    static private final int SAVE         = 2;
    static private final int OPEN_PANEL   = 3;
    static private final int OFFSET = 2;    //	Label And separator

    static private String[] menuLabels = {
            "Root Options",
            "Display in Text Area",
            "Save",
            "Open server panel",
    };

    private class TangoReleaseTreePopupMenu extends JPopupMenu {
        private JTree tree;
        private JLabel title;

        private TangoReleaseTreePopupMenu(JTree tree) {
            this.tree = tree;
            buildBtnPopupMenu();
        }
        //=======================================================

        /**
         * Create a Popup menu for host control
         */
        //=======================================================
        private void buildBtnPopupMenu() {
            title = new JLabel();
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
                            hostActionPerformed(evt);
                        }
                    });
                    add(btn);
                }
            }
        }
        //======================================================
        /*
         * Show menu on root
         */
        //======================================================
        public void showMenu(MouseEvent evt, String name) {
            //	Set selection at mouse position
            TreePath selectedPath =
                    tree.getPathForLocation(evt.getX(), evt.getY());
            if (selectedPath == null)
                return;
            tree.setSelectionPath(selectedPath);

            title.setText(name);

            //	Reset all items
            for (int i = 0; i < menuLabels.length; i++)
                getComponent(OFFSET + i).setVisible(false);

            //noinspection PointlessArithmeticExpression
            getComponent(OFFSET + ROOT_OPTION).setVisible(true);
            show(tree, evt.getX(), evt.getY());
        }
        //======================================================
        /*
         * Show menu on Collection of servers
         */
        //======================================================
        public void showMenu(MouseEvent evt, ServerCollectionClass serverCollectionClass) {
            //	Set selection at mouse position
            TreePath selectedPath =
                    tree.getPathForLocation(evt.getX(), evt.getY());
            if (selectedPath == null)
                return;
            tree.setSelectionPath(selectedPath);

            title.setText(serverCollectionClass.toString());

            //	Reset all items
            for (int i = 0; i < menuLabels.length; i++)
                getComponent(OFFSET + i).setVisible(false);

            getComponent(OFFSET + IN_TEXT_AREA).setVisible(true);
            getComponent(OFFSET + SAVE).setVisible(true);
            show(tree, evt.getX(), evt.getY());
        }
        //======================================================
        /*
         * Show menu on Collection of IDL
         */
        //======================================================
        public void showMenu(MouseEvent evt, IdlCollectionClass idlCollectionClass) {
            //	Set selection at mouse position
            TreePath selectedPath =
                    tree.getPathForLocation(evt.getX(), evt.getY());
            if (selectedPath == null)
                return;
            tree.setSelectionPath(selectedPath);

            title.setText(idlCollectionClass.toString());

            //	Reset all items
            for (int i = 0; i < menuLabels.length; i++)
                getComponent(OFFSET + i).setVisible(false);

            getComponent(OFFSET + IN_TEXT_AREA).setVisible(true);
            getComponent(OFFSET + SAVE).setVisible(true);
            show(tree, evt.getX(), evt.getY());
        }
        //======================================================
        /*
         * Show menu on Executable
         */
        //======================================================
        public void showMenu(MouseEvent evt, Executable executable) {
            //	Set selection at mouse position
            TreePath selectedPath =
                    tree.getPathForLocation(evt.getX(), evt.getY());
            if (selectedPath == null)
                return;
            tree.setSelectionPath(selectedPath);

            title.setText(executable.toString());

            //	Reset all items
            for (int i = 0; i < menuLabels.length; i++)
                getComponent(OFFSET + i).setVisible(false);

            getComponent(OFFSET + IN_TEXT_AREA).setVisible(true);
            getComponent(OFFSET + SAVE).setVisible(true);
            show(tree, evt.getX(), evt.getY());
        }
        //======================================================
        /*
         * Show menu on Device
         */
        //======================================================
        public void showMenu(MouseEvent evt, Instance instance) {
            //	Set selection at mouse position
            TreePath selectedPath =
                    tree.getPathForLocation(evt.getX(), evt.getY());
            if (selectedPath == null)
                return;
            tree.setSelectionPath(selectedPath);

            title.setText(instance.server.name);

            //	Reset all items
            for (int i = 0; i < menuLabels.length; i++)
                getComponent(OFFSET + i).setVisible(false);

            //  Only if appli is Astor
            getComponent(OFFSET + OPEN_PANEL).setVisible(true);
            show(tree, evt.getX(), evt.getY());
        }

        //======================================================
        private void hostActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int commandIndex = 0;
            for (int i = 0; i < menuLabels.length; i++)
                if (getComponent(OFFSET + i) == obj)
                    commandIndex = i;

            switch (commandIndex) {
                case ROOT_OPTION:
                    break;
                case IN_TEXT_AREA:
                    displayCollection();
                break;
                case SAVE:
                    saveCollection();
                break;
                case OPEN_PANEL:
                {
                    Object  object = getSelectedObject();
                    if (object instanceof Instance) {
                        String server = ((Instance) object).server.name;
                        if (appli instanceof Astor)
                            ((Astor)appli).tree.displayHostInfo("dserver/"+server);
                    }
                }
                break;
            }
        }
    }
}
