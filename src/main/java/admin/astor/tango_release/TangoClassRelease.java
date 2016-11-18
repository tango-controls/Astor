//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for the ClassRelease class definition .
//
// $Author: verdier $
//
// $Revision: $
//
// $Log: ClassRelease.java,v $
//
//-======================================================================

package admin.astor.tango_release;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbServer;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;

/**
 *	This class define a Tango class object with its
 *	Tango and IDL release number.
 *
 * @author  verdier
 */
public class  TangoClassRelease {

    String  className;
    String  serverName;
    int     idl = 0;
    String  error = "";
    //===============================================================
    //===============================================================
    public TangoClassRelease(DbServer dbServer, String className) {
        this.className = className;
        this.serverName = dbServer.name();
        try {
            String[]  deviceNames = getDevices(dbServer, className);
            if (deviceNames.length>0)
                idl = new DeviceProxy(deviceNames[0]).get_idl_version();
            else
                error = "No device found for class " + className + " on server " + dbServer.name();
        }
        catch (DevFailed e) {
            error = e.errors[0].desc;
        }
    }
    //===============================================================
    //===============================================================
    public String[] getDevices(DbServer dbServer, String className) throws DevFailed {
        return dbServer.get_device_name(className);
    }
	//===============================================================
	//===============================================================
    public String toString() {
        StringBuilder   sb = new StringBuilder();
        sb.append("\t");
        sb.append(className).append(" (");
        if (idl>0) {
            sb.append("idl=").append(idl);
        }
        else
            sb.append(error);
        sb.append(")");

        return sb.toString();
    }
	//===============================================================
	//===============================================================
	public static void main (String[] args) {

		String		serverName = "VacGaugeServer/sr_c1-pen";
		String		className = "VacGauge";

		if (args.length>0)
			className = args[0];
		try {
			TangoClassRelease	client =
                    new TangoClassRelease(new DbServer(serverName), className);
            System.out.println(client);
		}
		catch(DevFailed e) {
			Except.print_exception(e);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
