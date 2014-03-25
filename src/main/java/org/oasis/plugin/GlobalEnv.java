package org.oasis.plugin;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalEnv {

	public static AtomicInteger FITNESSE_PORT = new AtomicInteger(80);
	public static AtomicBoolean IS_FITNESSE_PROC = new AtomicBoolean(false);
	public static String FITNESSE_ROOT = ".";
	
	public static int getFitnessePort()
	{
		//RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		//List<String> arguments = runtimeMxBean.getInputArguments();	
		//System.out.println("arguments count " + arguments.size());
		String args = System.getProperty("sun.java.command");
		//System.out.println("$$ command " + System.getProperty("sun.java.command"));
		if (args.contains("-p ")) {
			String ps = args.substring(args.indexOf("-p ") + 3);
			if (ps.contains(" "))
				ps = ps.substring(0, ps.indexOf(" "));
			int p = Integer.parseInt(ps);
			FITNESSE_PORT.set(p);
			return FITNESSE_PORT.get();
		}
		else
		{
			FITNESSE_PORT.set(80);
			return FITNESSE_PORT.get(); //default is port 80 if command line argument is not set
		}
	}
	
}
