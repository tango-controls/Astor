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
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import java.awt.*;

/**
 *	This class is a thread to open a window with a
 *	remote login to a remote host.
 *
 * @author verdier
 */
public class RemoteLoginThread extends Thread implements AstorDefs {
    private String hostname;

    //======================================================================
    /**
     * Thread constructor.
     *
     * @param hostname Host to do the remote login.
     */
    //======================================================================
    public RemoteLoginThread(String hostname) {
        this.hostname = hostname;
    }
    //======================================================================
    /**
     * Running thread method.
     */
    //======================================================================
    public void run() {
        JSSHTerminal.MainPanel terminal;
        String defaultUser = "dserver";
        String defaultPassword = "dev-server";
        try {
            DbDatum[] data = ApiUtil.get_db_obj().get_property("Astor", new String[]{"RloginUser", "RloginPassword"});
            if (!data[0].is_empty()) defaultUser = data[0].extractString();
            if (!data[1].is_empty()) defaultPassword = data[0].extractString();
        } catch (DevFailed e) { /* */ }
        terminal = new JSSHTerminal.MainPanel(hostname, defaultUser, defaultPassword, 80, 24, 1024);
        terminal.setX11Forwarding(true);
        terminal.setExitOnClose(false);
        ATKGraphicsUtils.centerFrameOnScreen(terminal);
        terminal.setVisible(true);

    }
}