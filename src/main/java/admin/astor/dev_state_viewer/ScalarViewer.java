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
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.core.*;
import fr.esrf.tangoatk.widget.attribute.StateViewer;
import fr.esrf.tangoatk.widget.util.ATKConstant;

import javax.swing.*;
import java.awt.*;


/**
 * A ATK Scalar viewer to manage events
 *
 * @author verdier
 */

class ScalarViewer extends StateViewer implements IDevStateScalarListener {
    private StateCell stateCell;
    private JTable table;
    static AttributeList attributeList = new AttributeList();
    //==============================================================
    ScalarViewer(StateCell stateCell, String attributeName, JTable table) throws DevFailed {
        this.stateCell = stateCell;
        this.table = table;
        //System.out.println("Connect to " + stateCell.getName());
        try {
            //  create a state Viewer to manage events
            IDevStateScalar stateScalar =
                    (IDevStateScalar) attributeList.add(attributeName);
            setModel(stateScalar);
            stateScalar.addDevStateScalarListener(this);
        }
        catch (ConnectionException e) {
            Except.throw_exception("ConnectionFailed", e.getDescription());
        }
    }
    //==============================================================
    @Override
    public void devStateScalarChange(DevStateScalarEvent devStateScalarEvent) {
        stateCell.setStatus(devStateScalarEvent.getValue());
        stateCell.setErrorMessage(null);
        //  Manage state colors
        if (stateCell.getStatus().equals("FAULT") || stateCell.getStatus().equals("UNKNOWN"))
            stateCell.setForeground(Color.white);
        else
            stateCell.setForeground(Color.black);
        stateCell.setBackground(ATKConstant.getColor4State(stateCell.getStatus()));
        //System.out.println(stateCell.name + ":  " + stateCell.status);
        if (table!=null)
            table.repaint();
    }
    //==============================================================
    @Override
    public void errorChange(ErrorEvent errorEvent) {
        stateCell.setBackground(ATKConstant.getColor4State("UNKNOWN"));
        stateCell.setForeground(Color.white);
        stateCell.setErrorMessage(errorEvent.getError().getMessage());
        //System.out.println(stateCell.name+":  " + errorEvent.getError().getMessage());
    }
    //==============================================================
}
