//+======================================================================
// :  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// : pascal_verdier $
//
// Copyright (C) :      2004,2005,...................,2017,2018
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
// :  $
//
//-======================================================================

package admin.astor.dev_state_viewer;

import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.attribute.StateViewer;

import javax.swing.*;
import java.awt.*;


/**
 * A class defining state table cell
 *
 * @author verdier
 */

public class StateCell {
    private String name;
    private Color background = new Color(0xdddddd);
    private Color foreground = Color.black;
    private String status = "UNKNOWN";
    private String errorMessage;
    @SuppressWarnings("unused")
    private StateViewer stateViewer; // used only for scalar
    //==============================================================
    public StateCell(String attributeName, JTable table) throws DevFailed {
        name = " " + attributeName;
        stateViewer = new ScalarViewer(this, attributeName, table);
    }
    //==============================================================
    public String getName() {
        return name;
    }
    //==============================================================
    public Color getBackground() {
        return background;
    }
    //==============================================================
    public Color getForeground() {
        return foreground;
    }
    //==============================================================
    public String getStatusToolTip() {
        if (status.equals("ON"))
            return name + " is OK";
        else
            return name + " is " + status;
    }
    //==============================================================
    public String getStatus() {
        return status;
    }
    //==============================================================
    public String getErrorMessage() {
        return errorMessage;
    }
    //==============================================================
    public void setBackground(Color background) {
        this.background = background;
    }
    //==============================================================
    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }
    //==============================================================
    public void setStatus(String status) {
        this.status = status;
    }
    //==============================================================
    public void setErrorMessage(String errorMessage) {
        if (errorMessage!=null) {
            int idx = errorMessage.indexOf("Description:");
            if (idx<0)
                this.errorMessage = errorMessage;
            else
                this.errorMessage = errorMessage.substring(idx, errorMessage.indexOf("Reason"));
        }
        else
            this.errorMessage = null;
    }
    //==============================================================
    public String toString() {
        return name;
    }
    //==============================================================
}
