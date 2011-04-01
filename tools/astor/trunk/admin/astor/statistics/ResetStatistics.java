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

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;

import java.io.IOException;
import java.util.Vector;

//=======================================================
/**
 *	Execute a ResetStatistics on all Controlled starter
 *
 * @author  Pascal Verdier
 */
//=======================================================
public class ResetStatistics
{
    private int nbHosts = 0;
    private int done = 0;
    //=======================================================
    //=======================================================
	public ResetStatistics(Vector<String> hosts)
	{
        nbHosts = hosts.size();
        for (String host : hosts) {
            try {
                //  Check if host or starter name
                String  devName = host;
                if (host.indexOf('/')<0)
                    devName = "tango/admin/" + host;
                DeviceProxy dev = new DeviceProxy(devName);
                //dev.command_inout("ResetStatistics");
                done++;
                System.out.println("ResetStatistics done on " + host);
            }
            catch(DevFailed e) {
                System.err.println(host + ":    " + e.errors[0].desc);
            }
        }
	}
    //=======================================================
    //=======================================================
    private static boolean getConfirm(Vector<String> hosts)
    {
        try {
             System.out.println("OK to reset statistics on " + hosts.size() + " (y/n) ?");
             byte[] b = new byte[100];
             if (System.in.read(b)>0)
                 if (b[0]=='y')
                     return true;
         }
         catch (IOException e) {
             System.err.println(e);
         }
        return false;
    }
    //=======================================================
    //=======================================================
    public String toString()
    {
        return "ResetStatistics done for " + done + " hosts / " + nbHosts;

    }
    //=======================================================
    //=======================================================
    public static void main(String[] args)
    {
        Vector<String>  hosts = Utils.getHostControlledList(false);
        if (getConfirm(hosts)) {
            ResetStatistics rs = new ResetStatistics(hosts);
            System.out.println(rs);
        }
    }
    //=======================================================
    //=======================================================
}
