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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *	This class Manage a list Tango class objects with their
 *	Tango and IDL release number.
 *
 * @author  verdier
 */
public class  TangoServerRelease extends ArrayList<TangoClassRelease> {

    String      name;
    String      exeName;
    String      instanceName;
    double      releaseNumber=0.0;
    String      error = "";

    private static final String dserverClassName = "DServer";
    //===============================================================
    //===============================================================
    public TangoServerRelease(String serverName) {
        this.name = serverName;
        StringTokenizer stk = new StringTokenizer(serverName, "/");
        exeName = stk.nextToken();
        instanceName = stk.nextToken();
        try {
            //  Get Tango release number
            DbServer dbServer = new DbServer(serverName);
            int release = new DeviceProxy("dserver/"+serverName).getTangoVersion();
            //  Get class list
            String[] classNames = getClasses(dbServer);
            if (release>=100)
                releaseNumber = 0.01 * release;
            //System.out.println(name + ":    " + releaseNumber);
            if (classNames.length>0) {
                //add(new TangoClassRelease(dbServer, dserverClassName));
                for (String className : classNames) {
                    //  Add it to class list with its IDL
                    add(new TangoClassRelease(dbServer, className));
                }
            }
            else
                error = "No class found on server " + name;
        }
        catch (DevFailed e) {
            error = e.errors[0].desc;
        }
        catch (NoSuchMethodError e) {
            error = "TangORB is too old to check Tango release.";
        }
    }
    //===============================================================
    //===============================================================
    public String[] getClasses(DbServer dbServer) throws DevFailed {
        return dbServer.get_class_list();
    }
	//===============================================================
	//===============================================================
    public boolean hasIDL(int idl) {
        for (TangoClassRelease classRelease : this) {
            if (classRelease.idl==idl &&
               !classRelease.className.equals(dserverClassName))
                return true;
        }
        //  Nothing found
        return false;
    }
	//===============================================================
	//===============================================================
    public List<TangoClassRelease> getIdlClasses(int idl) {
        List<TangoClassRelease> list = new ArrayList<>();
        for (TangoClassRelease classRelease : this) {
            if (classRelease.idl==idl &&
               !classRelease.className.equals(dserverClassName))
                list.add(classRelease);
        }
        return list;
    }
	//===============================================================
	//===============================================================
    public String toStringFull() {
        StringBuilder   sb = new StringBuilder(this.toString());
        sb.append("\n");
        for (TangoClassRelease classRelease : this) {
            sb.append(classRelease).append("\n");
        }
        return sb.toString();
    }
	//===============================================================
	//===============================================================
    public String toString() {
        StringBuilder   sb = new StringBuilder(name + " (");
        if (releaseNumber>=1.0) {
            sb.append("Tango-").append(String.format("%4.2f", releaseNumber));
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

		if (args.length>0)
			serverName = args[0];
		try {
			TangoServerRelease	client =
                    new TangoServerRelease(serverName);
            System.out.println(client);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
