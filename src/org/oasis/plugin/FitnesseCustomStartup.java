package org.oasis.plugin;

import java.io.IOException;
import java.net.ServerSocket;

public class FitnesseCustomStartup implements Runnable {
	
	public int fitnessePort = 80;
	
	public static interface fitnesseEvents {
		void startup(int port);
	}
	public fitnesseEvents events = new fitnesseEvents() {
		public void startup(int port) { //default event, does nothing
			
		}
	};
	
	public FitnesseCustomStartup(int fitnessePort, fitnesseEvents events) {
		this.fitnessePort = fitnessePort;
		this.events = events;
	}

	public static void addCustomFitnesseStartup(int fitnessePort, fitnesseEvents events) {
		Thread t = new Thread(new FitnesseCustomStartup(fitnessePort, events));
        t.start();
	}

	@Override
	public void run() {
		// loop until port is accessible
		while(checkPort(fitnessePort)) {
			try {Thread.sleep(500);} catch (Exception e) {e.printStackTrace();}
		}
		events.startup(fitnessePort);
	}
	
	public static boolean checkPort(int port)
	{
		//System.out.println("checking port: " + port);
	    ServerSocket ss = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ss.close();
	    } catch (IOException e) {
	    	return false;
	    } finally {
	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }
	    return true;
	}

}
