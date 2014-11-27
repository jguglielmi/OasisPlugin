package org.oasis.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import fitnesseMain.FitNesseMain;

public class GlobalEnv {

	public static AtomicInteger FITNESSE_PORT = new AtomicInteger(80);
	public static AtomicBoolean IS_FITNESSE_PROC = new AtomicBoolean(false);
	public static String FITNESSE_ROOT = ".";
	
	public static String getOasisRoot() {
		String rootPath = ".";
		rootPath = FitNesseMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		System.out.println("getOasisRoot: " + rootPath);
		if (rootPath.contains("/"))
			rootPath = rootPath.substring(0, rootPath.lastIndexOf("/"));
		else if (rootPath.contains("\\"))
			rootPath = rootPath.substring(0, rootPath.lastIndexOf("\\"));
		return rootPath;
	}
	
	public static Properties getOasisProperties()
	{
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(getOasisRoot() + File.separator + "oasis.properties");
			prop.load(input);
			// get the property value and print it out
			//System.out.println(prop.getProperty("oasis_version"));
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return prop;
	}

	//searches command line args for the port passed
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
		
	public static String getOasisVersion()
	{
		return getOasisProperties().getProperty("oasis_version");
	}
	
	public static String getOasisBuildDate()
	{
		return getOasisProperties().getProperty("build_date");
	}
}
