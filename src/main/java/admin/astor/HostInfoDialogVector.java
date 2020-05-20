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

import java.awt.*;
import java.util.ArrayList;


public class HostInfoDialogVector extends ArrayList<HostInfoDialog> {
    private Point position = null;

    //===============================================================
    //===============================================================
    HostInfoDialogVector() {
        super();
    }
    //===============================================================
    //===============================================================
    void setDialogPreferredSize(Dimension d) {
        for (Object o : this)
            ((HostInfoDialog) o).setDialogPreferredSize(d);
    }
    //===============================================================
    //===============================================================
    HostInfoDialog getByHostName(TangoHost host) {
        HostInfoDialog hid = null;
        for (HostInfoDialog hostInfoDialog : this) {
            if (host.getName().equals(hostInfoDialog.getHostName()))
                hid = hostInfoDialog;
        }
        return hid;
    }

    //===============================================================
    //===============================================================
    HostInfoDialog add(Astor parent, TangoHost host) {
        if (position == null)
            position = parent.getLocationOnScreen();
        //	Set the servers polling and Notify to awake the thread.
        host.poll_serv_lists = true;
        host.updateData();
        //	And wait a bit before re-build panel
        try { Thread.sleep(500); } catch (Exception e) { /* */ }

        //	Search if already exists
        host.info_dialog = getByHostName(host);
        //	If does not exists, create a new one and add it in vector
        if (host.info_dialog == null) {
            host.info_dialog = new HostInfoDialog(parent, host);
            add(host.info_dialog);

            //	Set position to display
            position.translate(10, 10);
            host.info_dialog.setLocation(position);
        } else
            host.info_dialog.updatePanel(true);
        host.info_dialog.setVisible(true);

        return host.info_dialog;
    }

    //===============================================================
    //===============================================================
    void close(TangoHost host) {
        //	Search if already exists
        HostInfoDialog hid = getByHostName(host);
        //	If do exists, close it
        if (hid != null)
            hid.doClose();
    }
    //===============================================================
    //===============================================================
}
