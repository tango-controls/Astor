//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for the TangoServerReleaseList class definition .
//
// $Author: verdier $
//
// $Revision: $
//
// $Log: TangoServerReleaseList.java,v $
//
//-======================================================================

package admin.astor.tango_release;

import admin.astor.AstorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/**
 *	This class Manage a list Tango server objects with their
 *	Tango and IDL release number.
 *
 * @author  verdier
 */
public class  TangoServerReleaseList  extends ArrayList<TangoServerRelease> {

    private List<TangoServerRelease> onErrors = new ArrayList<>();
    private static final int idlMin = 2;
    private static final int idlMax = 10;
	//===============================================================
	//===============================================================
	public TangoServerReleaseList(List<String> serverNames) {

        Collections.sort(serverNames, new StringComparator());
        int counter = 0;
        for (String serverName : serverNames) {
            double ratio = (double) counter / serverNames.size(); counter++;
            AstorUtil.increaseSplashProgress(ratio, "Check " + counter + "/" +
                    serverNames.size() + " - " + serverName);
            TangoServerRelease  serverRelease = new TangoServerRelease(serverName);
            if (serverRelease.releaseNumber>0)
                add(serverRelease);
            else
                onErrors.add(serverRelease);

            if (AstorUtil.osIsUnix()) {
                //  If Linux, needs to wait 3 minutes
                //  for file systems release
                if ((counter%920)==0) {
                    for (int i=180 ; i>0 ; i--) {
                        AstorUtil.setSplashMessage("Waiting " + i + " sec.");
                        try { Thread.sleep(1000); } catch (InterruptedException e) { /* */ }
                    }
                }
            }
        }
    }
	//===============================================================
	//===============================================================
    public int nbClasses() {
        int nb = 0;
        for (int idl=idlMin ; idl<=idlMax ; idl++) {
            List<TangoClassRelease> classReleases = getClassesForIdlRelease(idl);
            nb += classReleases.size();
        }
        return nb;
    }
	//===============================================================
	//===============================================================
    public List<TangoServerRelease> getServersForTangoRelease(double tangoRelease) {
        List<TangoServerRelease>   list = new ArrayList<>();
        for (TangoServerRelease serverRelease : this) {
            //  Check only first decimal value
            int r1 = (int) (10*serverRelease.releaseNumber);
            int r2 = (int) (10*tangoRelease);
            if (r1==r2) {
                list.add(serverRelease);
            }
        }
        return list;
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedDeclaration")
    public List<TangoServerRelease> getServersForIdlRelease(int idl) {
        List<TangoServerRelease>   list = new ArrayList<>();
        for (TangoServerRelease serverRelease : this) {
            if (serverRelease.hasIDL(idl)) {
                list.add(serverRelease);
            }
        }
        return list;
    }
    //===============================================================
    //===============================================================
    public List<TangoClassRelease> getClassesForIdlRelease(int idl) {
        List<TangoClassRelease>   list = new ArrayList<>();
        for (TangoServerRelease serverRelease : this) {
            List<TangoClassRelease>    classes = serverRelease.getIdlClasses(idl);
            for (TangoClassRelease class_ : classes) {
                //  Check if already in list
                boolean found = false;
                for (TangoClassRelease classRelease : list) {
                    if (classRelease.className.equals(class_.className))
                        found = true;
                }
                if (!found)
                    list.add(class_);
            }
        }
        Collections.sort(list, new ClassComparator());
        return list;
    }
	//===============================================================
	//===============================================================
    public List<TangoServerRelease> getServersOnError() {
       return onErrors;
    }
	//===============================================================
	//===============================================================
    public String toString(List<TangoServerRelease>   list) {

        StringBuilder   sb = new StringBuilder();
        for (TangoServerRelease serverRelease : list) {
            sb.append(serverRelease.toStringFull()).append("\n");
        }
        return sb.toString();
    }
	//===============================================================
	//===============================================================
    public String toString(double tangoRelease) {
        return toString(getServersForTangoRelease(tangoRelease));
    }
	//===============================================================
	//===============================================================
    public String toString(int idl) {
        StringBuilder   sb = new StringBuilder();
        List<TangoClassRelease> classReleases = getClassesForIdlRelease(idl);
        for (TangoClassRelease classRelease : classReleases) {
            sb.append(classRelease).append("\n");
        }

        return sb.toString();

        //return toString(getServersForIdlRelease(idl));

    }
	//===============================================================
	//===============================================================
    public String toString() {
        return toString(this);
    }
    //===============================================================
	//===============================================================
	public static void main (String[] args) {

        List<String>   list = new ArrayList<>();
        list.add("VacGaugeServer/sr_c1-pen");
        list.add("VacGaugeServer/sr_c1-ip");
        list.add("PLCvacuumValve/sr_c01");
        list.add("Starter/l-c01-1");


        try {
            TangoServerReleaseList	client = new TangoServerReleaseList(list);
            System.out.println(client.toString(4));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
	}
    //======================================================
    //======================================================











    //======================================================
    /**
     * Comparators class to sort TangoServerRelease collection
     */
    //======================================================
    @SuppressWarnings("UnusedDeclaration")
    class ServerComparator implements Comparator<TangoServerRelease> {
        public int compare(TangoServerRelease server1, TangoServerRelease server2) {
            return server1.name.compareTo(server2.name);
        }
    }
    //======================================================
    /**
     * Comparators class to sort TangoClassRelease collection
     */
    //======================================================
    class ClassComparator implements Comparator<TangoClassRelease> {
        public int compare(TangoClassRelease class1, TangoClassRelease class2) {
            return class1.className.compareTo(class2.className);
        }
    }
    //======================================================
    /**
     * Comparators class to sort String collection
     */
    //======================================================
    class StringComparator implements Comparator<String> {
        public int compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }
}
