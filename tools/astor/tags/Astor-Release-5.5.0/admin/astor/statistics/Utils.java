//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for the StarterStat class definition .
//
// $Author: verdier $
//
// $Revision: $
//
// $Log: StarterStat.java,v $
//
// Copyleft 2010 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor.statistics;


/** 
 *	This class is able to
 *
 * @author  verdier
 */

import admin.astor.AstorUtil;
import com.braju.format.Format;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;



public class  Utils
{
    private static final String	percentageFormat = "%7.4f";    
	//===============================================================
	//===============================================================
	public static String formatDuration(long ms)
    {
        double nbSec = (double) ms/1000.0;
        return formatDuration(nbSec);
    }
	//===============================================================
	//===============================================================
	public static String formatDuration(double nbSeconds)
	{
		if (nbSeconds<10.0) {
			String	str = "" + nbSeconds;
			int		idx = str.indexOf('.');
			if (idx>0)
				if (str.length()>idx+3)
					str = str.substring(0, idx+3);
			return str + " sec.";
		}
		else {
			int	intDuration = (int)nbSeconds;
			if (nbSeconds<60.0)
				return "" + intDuration + " sec.";
			else
			if (intDuration<3600) {
				int	mn  = intDuration/60;
				int	sec = intDuration - 60*mn;
				return "" + mn + " mn " + ((sec<10)? "0":"") + sec + " sec.";
			}
			else
			if (intDuration<24*3600) {
				int h   = intDuration/3600;
				intDuration -= h*3600;
				int	mn  = intDuration/60;
				int	sec = intDuration - 60*mn;
				return "" + h + " h " + ((mn<10)? "0":"")+ mn + " mn " +
							((sec<10)? "0":"") + sec + " sec.";
			}
			else {
				int	days = intDuration/(24*3600);
				return "" + days + " day" + ((days>1)? "s " : " ") +
					formatDuration((double)intDuration - (days*24*3600));
			}
		}
	}
    //===============================================================
    //===============================================================
    public static String formatPercentage(double ratio)
    {
        Object[] o	= { 100.0*ratio };
        return Format.sprintf(percentageFormat, o) + " %";
     }
    //===============================================================
    //===============================================================
    public static String formatDate(long ms)
    {
        StringTokenizer st = new StringTokenizer(new Date(ms).toString());
        Vector<String>	v = new Vector<String>();
        while (st.hasMoreTokens())
            v.add(st.nextToken());

        String  month = v.get(1);
        String  day   = v.get(2);
        String  time  = v.get(3);
        String  year  = v.get(v.size()-1);
        if (year.indexOf(')')>0)    year = year.substring(0, year.indexOf(')'));

        return day + " " + month + " " + year + "  " + time;
    }
	//===============================================================
	//===============================================================



    //=======================================================
	//=======================================================
	public static Vector<StarterStat>  readHostStatistics()
    {
        Vector<String>  vs = null;
        return readHostStatistics(vs);
    }
    //=======================================================
	//=======================================================
	public static StarterStat  readHostStatistics(String hostName)
    {
        Vector<String>  vs = new Vector<String>();
        vs.add(hostName);
        Vector<StarterStat> stat = readHostStatistics(vs);
        if (stat.size()>0)
            return stat.get(0);
        return null;
    }
    //=======================================================
	//=======================================================
	public static Vector<StarterStat>  readHostStatistics(Vector<String> ctrlHosts)
    {
        //  If host list is empty, get controlled host list
        if (ctrlHosts==null || ctrlHosts.size()==0) {
            ctrlHosts = getHostControlledList(true);
        }

        int increment = 80/ctrlHosts.size();
        Vector<StarterStat> vStat = new Vector<StarterStat>();
        for (String host : ctrlHosts) {
            AstorUtil.increaseSplashProgress(increment, "Get statistics for " + host);
            //System.out.println(host);
            vStat.add(new StarterStat(host));
        }
        //System.out.println(new GlobalStatistics(vStat));
        return vStat;
    }
	//=======================================================
	//=======================================================
	public static Vector<String> getHostControlledList(boolean display)
    {
        if (display)
            AstorUtil.increaseSplashProgress(5, "Get Controlled host list....");
        Vector<String> ctrlHosts = new Vector<String>();
        String dbg = System.getProperty("ebugStat");
        if (dbg!=null && dbg.equals("true")) {
            ctrlHosts.add("coral");
            ctrlHosts.add("esrflinux1-2");
            return ctrlHosts;
        }

        try {
            //  get host list
            String[]	hosts = AstorUtil.getInstance().getHostControlledList();

            //	get ExcludedCollectionsForStatistics Astor Property
            Database dbase = ApiUtil.get_db_obj();
            String[]    excludedCollections = new String[0];
            DbDatum data = dbase.get_property("Astor", "ExcludedCollectionsForStatistics");
            if (!data.is_empty())
                excludedCollections = data.extractStringArray();

            for (String host : hosts) {
                //  Get host collection from Starter property
                DbDatum datum = new DeviceProxy("tango/admin/"+host).get_property("HostCollection");
                if (!datum.is_empty()) {
                    String collec = datum.extractString().toLowerCase();

                    //  Check if collection is excluded.
                    boolean found = false;
                    for (String s : excludedCollections){
                        if (s.toLowerCase().equals(collec))
                            found = true;
                    }
                    if (!found)
                        ctrlHosts.add(host);
                }
            }
        }
        catch(DevFailed e) {
            if (display)
                ErrorPane.showErrorMessage(new JFrame(), null, e);
            else
                Except.print_exception(e);
        }
        return ctrlHosts;
    }
    //===============================================================
    /**
     *	Open a file and return text read.
     * @param filename file to be read.
     * @return the file content read.
     * @throws fr.esrf.Tango.DevFailed in case of failure during read file.
     */
    //===============================================================
    public static String readFile(String filename) throws DevFailed
    {
        String str = "";
		try
		{
        	FileInputStream fid = new FileInputStream(filename);
        	int nb = fid.available();
        	byte[]	inStr  = new byte[nb];
        	nb = fid.read(inStr);
        	fid.close();

        	if (nb>0)
            	str = takeOffWindowsChar(inStr);
        }
		catch (Exception e)
		{
			Except.throw_exception("READ_FAILED",
						e.toString(), "ParserTool.readFile()");
		}
		return str;
    }
    //===============================================================
    /**
     *  Check if OS is Unix
     * @return true if OS is not windows
     */
    //===============================================================
    public static boolean osIsUnix()
    {
        String	os = System.getProperty("os.name");
        //System.out.println("Running under " + os);
        return ! os.toLowerCase().startsWith("windows");
    }
	//===============================================================
	/**
	 *	Take off Cr eventually added by Windows editor.
     * @param b_in  specified byte array to be modified.
     * @return the modified byte array as String.
	 */
	//===============================================================
	private static String takeOffWindowsChar(byte[] b_in)
	{
		//	Take off Cr (0x0d) eventually added by Windows editor
        int	nb = 0;
        for (byte b : b_in)
            if (b != 13)
                nb++;
		byte[]	b_out = new byte[nb];
		for (int i=0, j=0 ; i<b_in.length ; i++)
			if (b_in[i]!=13)
				b_out[j++] = b_in[i];
		return new String(b_out);
	}
    //===============================================================
    //===============================================================
    private static String checkOsFormat(String code)
    {
        if (!osIsUnix())
            return setWindowsFileFormat(code);
        else
            return code;
    }

    //===============================================================
    //===============================================================
    public static String setWindowsFileFormat(String code)
    {
        //	Convert default Unix format to Windows format
        byte[]  b = { 0xd, 0xa };
        String	lsp = new String(b); //System.getProperty("line.separator");
        code = code.replaceAll("\n", lsp);
        return code;
    }
    //===============================================================
    //===============================================================
    public static void writeFile(String fileName, String code)  throws	DevFailed
    {
 		try
		{
        	code = checkOsFormat(code);
        	FileOutputStream	fidout = new FileOutputStream(fileName);
        	fidout.write(code.getBytes());
        	fidout.close();
            System.out.println(fileName + "  written");
    	}
		catch (Exception e)
		{
			Except.throw_exception("WRITE_FAILED",
						e.toString(), "ParserTool.readFile()");
		}
	}
    //=======================================================
    //=======================================================
    public static String parseXmlProperty(String code, String tag) throws DevFailed
    {
        String  tmpTag = tag+"=\"";
        int start = code.indexOf(tmpTag);
        if (start<0)
            Except.throw_exception("SYNTAX_ERROR",
                    "Cannot parse \'"+tag+"\'  in:\n" + code,
                    "Utils.parseProperty()");
        start += tmpTag.length();
        int end = code.indexOf('\"', start);
        if (end<0)
            Except.throw_exception("SYNTAX_ERROR",
                    "Cannot parse \'"+tag+"\'  in:\n" + code,
                    "Utils.parseProperty()");
        return code.substring(start, end);
    }
    //===================================================================
    //===================================================================
    public static String strReplace(String text, String old_str, String new_str)
    {
        if (text==null)	return "";
        for (int pos=0 ; (pos=text.indexOf(old_str, pos))>=0 ; pos+=new_str.length())
            text = text.substring(0, pos) + new_str +
                    text.substring(pos+old_str.length());
        return text;
    }
    //=======================================================
    //=======================================================
}
