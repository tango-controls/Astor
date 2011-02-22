//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for main swing class.
//
// $Author$
//
// $Revision$
// $Log$
// Revision 1.1  2006/09/19 13:06:47  pascal_verdier
// Access control manager added.
//
//
//
// Copyleft 2005 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================


package admin.astor.access;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.DbClass;
import fr.esrf.TangoApi.DbDatum;



//=======================================================
/**
 *	Class Description: A DeviceProxy class on AccessControl device
 *
 * @author  Pascal Verdier
 */
//=======================================================
public class AccessProxy extends DeviceProxy
{
	//=======================================================
	//=======================================================
	public AccessProxy(String devname) throws DevFailed
	{
		super(devname);
	}
	//=======================================================
	//=======================================================
	public String[] getUsers() throws DevFailed
	{
		DeviceData	argout = command_inout("GetUsers");
		return argout.extractStringArray();
	}
	//=======================================================
	//=======================================================
	public String[] getAddressesByUser(String user) throws DevFailed
	{
        DeviceData  argin  = new DeviceData();
        argin.insert(user);
        DeviceData	argout = command_inout("GetAddressByUser", argin);
		return argout.extractStringArray();
	}
	//=======================================================
	//=======================================================
	public String[] getDevicesByUser(String user) throws DevFailed
	{
        DeviceData  argin  = new DeviceData();
        argin.insert(user);
        DeviceData	argout = command_inout("GetDeviceByUser", argin);
		return argout.extractStringArray();
	}
	//=======================================================
	//=======================================================
	public void removeUser(String user) throws DevFailed
	{
		DeviceData  argin  = new DeviceData();
        argin.insert(user);
        command_inout("RemoveUser", argin);
	}
	//=======================================================
	//=======================================================
	public void cloneUser(String src_user, String new_user) throws DevFailed
	{
        String[]	array = { src_user, new_user };
		DeviceData  argin  = new DeviceData();
        argin.insert(array);
        command_inout("CloneUser", argin);
	}
	//=======================================================
	//=======================================================
	public void removeAddress(String user, String address) throws DevFailed
	{
        String[]	array = { user, address };
		DeviceData  argin  = new DeviceData();
        argin.insert(array);
        command_inout("RemoveAddressForUser", argin);
	}
	//=======================================================
	//=======================================================
	public void addAddress(String user, String address) throws DevFailed
	{
        String[]	array = { user, address };
		DeviceData  argin  = new DeviceData();
        argin.insert(array);
        command_inout("AddAddressForUser", argin);
	}
	//=======================================================
	//=======================================================
	public void removeDevice(String user, String devname, String val) throws DevFailed
	{
        String[]	array = { user, devname, val };
		DeviceData  argin  = new DeviceData();
        argin.insert(array);
        command_inout("RemoveDeviceForUser", argin);
	}
	//=======================================================
	//=======================================================
	public void addDevice(String user, String devname, String val) throws DevFailed
	{
        String[]	array = { user, devname, val };
		DeviceData  argin  = new DeviceData();
        argin.insert(array);
        command_inout("AddDeviceForUser", argin);
	}
	//=======================================================
	//=======================================================
    public String getAccess(String[] inputs) throws DevFailed
    {
        DeviceData  argin = new DeviceData();
        argin.insert(inputs);
        DeviceData  argout = command_inout("GetAccess", argin);
        return argout.extractString();
    }
    //=======================================================
	//=======================================================
    public void registerService(boolean b) throws DevFailed
    {
        String  cmd = (b)? "RegisterService": "UnregisterService";
        DeviceData  argout = command_inout(cmd);
    }
    //=======================================================
	//=======================================================
    public void addAllowedCommand(ClassAllowed class_allowed) throws DevFailed
	{
		DbClass	db_class = new DbClass(class_allowed.name);
		DbDatum	datum = new DbDatum("AllowedAccessCmd");
		datum.insert(class_allowed.getAllowedCmdProperty());
		db_class.put_property(new DbDatum[] { datum });
	}
    //=======================================================
	//=======================================================
}
