//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the PingHosts class definition .
//
// $Author$
//
// $Revision$
//
// $Log$
//
// Copyleft 2010 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package admin.astor.tools;


/** 
 *	This class is able to
 *
 * @author  verdier
 */

import admin.astor.AstorUtil;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.Except;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;



public class  PingHosts
{
    private Vector<DevState>	states = new Vector<DevState>();
    private String[]            hosts;
	//===============================================================
	//===============================================================
	public PingHosts(String[] hosts) throws DevFailed
	{
        this.hosts = hosts;
		//	Start a thread to ping each host
        Vector<PingThread> threads = new Vector<PingThread>();
        for (String host : hosts) {
			threads.add(new PingThread(host));
		}
		for (PingThread thread : threads) {
			thread.start();
		}
		
		//	Wait a bit
		try {
            if (AstorUtil.osIsUnix())
                Thread.sleep(2000);
            else
                Thread.sleep(5000);
        } catch	(InterruptedException e) { /* */ }
		
		//	Check results
		for (PingThread thread : threads) {
			if (thread.hostAlive())
				states.add(DevState.ON);
			else
				states.add(DevState.FAULT);
            thread.interrupt();
		}
	}
	//===============================================================
	//===============================================================
	public Vector<DevState> getStates() throws DevFailed
	{
		return states;
	}
	//===============================================================
	//===============================================================
	public Vector<String> getRunning() throws DevFailed
	{
        Vector<String>  v = new Vector<String>();
        for (int i=0 ; i<hosts.length && i<states.size() ; i++) {
            if (states.get(i)==DevState.ON) {
                v.add(hosts[i]);
            }
        }
		return v;
	}
	//===============================================================
	//===============================================================
	public Vector<String> getStopped() throws DevFailed
	{
        Vector<String>  v = new Vector<String>();
        for (int i=0 ; i<hosts.length && i<states.size() ; i++) {
            if (states.get(i)==DevState.FAULT) {
                v.add(hosts[i]);
            }
        }
		return v;
	}
	//===============================================================
	//===============================================================
	public static void main (String[] args)
	{
        try  {
		    String[]	hosts = AstorUtil.getInstance().getHostControlledList();
            /*
                { "l-pinj-1", "l-pinj-2", "l-pinj-3", "deneb",
				 //"l-c10-4", "l-c10-5", "l-c10-6" , "orion",
				};
                */
            int alives = 0;
            int deads  = 0;
            long	t0 = System.currentTimeMillis();
			PingHosts	client = new PingHosts(hosts);
			Vector<DevState> states = client.getStates();
			for (int i=0 ; i<hosts.length && i<states.size() ; i++) {
                if (states.get(i)==DevState.FAULT)
                    deads++;
                else
                    alives++;
				System.out.println(hosts[i] + ":	" + 
					((states.get(i)==DevState.FAULT)? "NOT " :"") + " alive");
            }
			long	t1 = System.currentTimeMillis();
			System.out.println("elapsed time: " + (t1-t0) + " ms");
            System.out.println(alives + " hosts alive   and  " + deads + " hosts dead");
		}
		catch(DevFailed e) {
			Except.print_exception(e);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
        System.exit(0);
	}
	//===============================================================
    //===============================================================
	
	
	
	
	
	
	
	
	//===============================================================
    //===============================================================
	private class PingThread extends Thread
	{
		private String	hostName;
		private boolean	alive = false;
    	//===============================================================
    	//===============================================================
		private PingThread(String hostName)
		{
			this.hostName = hostName;
		}
    	//===============================================================
    	//===============================================================
		private boolean hostAlive()
		{
			return alive;
		}
    	//===============================================================
    	//===============================================================
		public void run()
		{
			try {
				String	str = executeShellCmdOneLine("ping " + hostName);
                if (AstorUtil.osIsUnix())
    				alive = (str.toLowerCase().indexOf("unreachable")<0);
                else
    				alive = (str.toLowerCase().indexOf("unreachable")<0 &&
                             str.length()>0 && str.indexOf("timed out")<0);
				
			}
			catch (DevFailed e) {
				Except.print_exception(e);
			}
		}
    	//===============================================================
    	/**
    	 *	Execute a shell command and throw exception if command failed.
    	 *
    	 *	@param cmd	shell command to be executed.
    	 *	@return output of executed command if display is false
    	 *	@throws fr.esrf.Tango.DevFailed if executed command has failed.
    	 */
    	//===============================================================
    	public String executeShellCmdOneLine(String cmd) throws DevFailed
    	{
       		StringBuffer	sb = new StringBuffer();
			try {
        		Process proc = Runtime.getRuntime().exec(cmd);

        		// get command's output stream and
        		// put a buffered reader input stream on it.
        		//-------------------------------------------
        		InputStream istr = proc.getInputStream();
        		BufferedReader br =
                		new BufferedReader(new InputStreamReader(istr));

        		// read output lines from command
                //  Ping result is in second line
        		//-------------------------------------------
        		String str;
        		for (int cnt=0 ; cnt<2 && (str=br.readLine())!=null ; ){
                    str = str.trim();
                    if (str.length()>0) {
                		sb.append(str.trim()).append("\n");
                        cnt++;
                    }
         		}
                proc.destroy();
			}
			catch (Exception e) {
					Except.throw_exception(e.toString(),
                		"The shell command\n" + cmd + "\nHas failed",
						"Utils.executeShellCmd()");
			}
        	//System.out.println(sb);
        	return sb.toString();
    	}
	}
}
