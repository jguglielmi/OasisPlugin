package org.oasis.plugin;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.oasis.plugin.responders.SikuliResponder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Util {
	
	public static String pomXmlFile = "./pom.xml";
	public static String pomXmlStr = "";
	
	public static boolean waitForFileToExists(String filename, int waitSeconds) {
		int totalSeconds = 0;
		while (new File(filename).exists() == false && totalSeconds < waitSeconds) {
			++totalSeconds;
			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		}
		return new File(filename).exists();
	}
	
	public static String fileToString(String filename) {
		String result = null;
	    DataInputStream in = null;
	    try {
	        byte[] buffer = new byte[(int) new File(filename).length()];
	        in = new DataInputStream(new FileInputStream(filename));
	        in.readFully(buffer);
	        result = new String(buffer);
	        in.close();
	    } 
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}
	
	public static String evaluateXpathGetValue(String xml, String xpathExpr) {
		String result = "";
		try {
			InputSource inSource = new InputSource(new StringReader(xml));
			XPath xpath = XPathFactory.newInstance().newXPath();
			Node node = (Node) xpath.evaluate(xpathExpr, inSource, XPathConstants.NODE);
			if (node != null)
				result = node.getNodeValue();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<String> evaluateXpathGetValues(String xml, String xpathExpr) {
		List<String> resultLst = new ArrayList<String>();
		try {
			InputSource inSource = new InputSource(new StringReader(xml));
			XPathFactory factory = XPathFactory.newInstance();
		    XPath xpath = factory.newXPath();
		    XPathExpression expr = xpath.compile(xpathExpr);

		    Object result = expr.evaluate(inSource, XPathConstants.NODESET);
		    NodeList nodes = (NodeList) result;
		    for (int i = 0; i < nodes.getLength(); i++) {
		    	resultLst.add(nodes.item(i).getNodeValue());
		        //System.out.println(nodes.item(i).getNodeValue()); 
		    }

		} catch(Exception e) {
			e.printStackTrace();
		}
		return resultLst;
	}

	
	public static boolean isShutdownPressed(){
		//<fitnesse.port>8000</fitnesse.port>
		int port = 8000;
		try
		{
			if (new File(pomXmlFile).exists())
			{
				if (pomXmlStr == "")
					pomXmlStr = fileToString(pomXmlFile);
				String val = evaluateXpathGetValue(pomXmlStr, "//*[local-name()='fitnesse.port']/text()");
				port = Integer.parseInt(val);
			}
			//else
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (org.oasis.plugin.GlobalEnv.IS_FITNESSE_PROC.get())
			return isShutdownPressed(org.oasis.plugin.GlobalEnv.FITNESSE_PORT.get());
		else
			return isShutdownPressed(port);
	}

	public static boolean isShutdownPressed(int port){
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
	
	public static  byte[] hexToBytes(String hex) {
		int len = hex.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i+1), 16));
		}
		return data;
	}
	
	public static String processDecryptionString(String val) {
		if (val.startsWith("decrypt:") ) {
			try {
				val = val.substring(val.indexOf(":") + 1);
				SecretKeySpec key = SikuliResponder.generateKey();
				Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
				aes.init(Cipher.DECRYPT_MODE, key);
				String cleartext = new String(aes.doFinal(hexToBytes(val)));
				return cleartext;
			}
			catch (Exception e) {
				//LOG.info("Exception processDecryptionString:" + e.getMessage());
				e.printStackTrace();
				return val;
			}
		}
		else
			return val;
	}
	
	public static Process forkJavaProcess(String startPath, String jarFilename, String[] args) throws IOException {

		String sep = System.getProperty("file.separator");
		String javaBin = "java"; //System.getProperty("java.home")  + sep + "bin" + sep + "java.exe";
		//System.out.println("javaBin: " + javaBin);
		List<String> argumentsList = new ArrayList<String>();
		if (System.getProperty("os.name").startsWith("Win"))
		{
			//if using windows use different method of starting java process, which lets it start on top
			argumentsList.add("cmd.exe");
			argumentsList.add("/c");
			String argStr = "";
			for (String arg : args) {
				argStr += arg + " ";
			}
			argumentsList.add("\"start " + javaBin + " -jar " + jarFilename + " " + argStr + "\"");
		}
		else
		{
			//for linux, osx and other os
			argumentsList.add(javaBin);
			//argumentsList.add("-classpath");
			//argumentsList.add(getClasspath());
			argumentsList.add("-jar");
			argumentsList.add(jarFilename);
			for (String arg : args) {
				argumentsList.add(arg);
			}
		}

		String printCmd = "";
		for (String arg : argumentsList) {
			printCmd += arg + " ";
		}
		System.out.println(printCmd);
		
		ProcessBuilder processBuilder = new ProcessBuilder(argumentsList.toArray(new String[argumentsList.size()]));
		processBuilder.redirectErrorStream(true);
		processBuilder.directory(new File(startPath));
		return processBuilder.start();
	}

}
