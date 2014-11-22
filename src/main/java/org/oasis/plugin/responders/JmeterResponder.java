//Name: JmeterResponder
//Author: Edward Jakubowski ejakubowski7@gmail.com
//Last update: 11/22/2014
//Description: This responder add's back-end support for the JmeterBundle library.
//Requirements: jmeterbundle.jar, and ResponderFactory.java has: addResponder("jmeter", JmeterResponder.class);
//Examples:
//To open Jmeter GUI from a link use the following address:
// http://localhost:8000/?jmeter&dialog
//
//To run a Jmeter JMX project use the following address:
// http://localhost:8000/?jmeter&jmx=jmeter/SimpleHttpTest1.jmx


package org.oasis.plugin.responders;


import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.MockingPageCrawler;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.threads.RemoteThreadsListenerTestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.util.JOrphanUtils;
import org.oasis.plugin.Util;

public class JmeterResponder implements SecureResponder {


public static final String CONTENT_INPUT_NAME = "pageContent";
public static final String TIME_STAMP = "editTime";
public static final String TICKET_ID = "ticketId";
public static final String HELP_TEXT = "helpText";
public static final String SUITES = "suites";
public static final String PAGE_TYPE = "pageType";
public static final String PAGE_NAME = "pageName";
public static final String TEMPLATE_MAP = "templateMap";

protected String content;
protected WikiPage page;
protected WikiPage root;
protected PageData pageData;
protected Request request;
@SuppressWarnings("unused")
private static String rootPagePath = "./FitNesseRoot";
//private static String pomXmlFile = "./pom.xml";
//private static String pomXmlStr = "";

public static String CAPTURE_FILENAME = "";
public static boolean CAPTURE_STATUS = false;

public JMeterEngine jEngine = null;
public HashTree tree = null;
public static Thread jmeterThread = null;
public static String jmeterHomePath = ".";
public static String jmeterPropertiesFile = "./jmeter/jmeter.properties";
public static String jmeterbundleJar = "./plugins/jmeterbundle.jar";
public static String jmeterLogPath = "./FitNesseRoot/files/testResults/";

public JmeterResponder() {
}

public Response makeResponse(FitNesseContext context, Request request) {
	boolean nonExistent = request.hasInput("nonExistent");
	return doMakeResponse(context, request, nonExistent);
}

public Response makeResponseForNonExistentPage(FitNesseContext context, Request request) {
	return doMakeResponse(context, request, true);
}

protected Response doMakeResponse(FitNesseContext context, Request request, boolean firstTimeForNewPage) {
	initializeResponder(context.root, request);
	
	final SimpleResponse response = new SimpleResponse();
	String resource = request.getResource();
	WikiPagePath path = PathParser.parse(resource);
	PageCrawler crawler = context.root.getPageCrawler();
	
	page = crawler.getPage(path, new MockingPageCrawler());
	pageData = page.getData();
	content = createPageContent();
	
	rootPagePath = context.getRootPagePath();
	//loadProperties();
	
	
	if (request.hasInput("dialog")) {
		 displayDialog(response);
	}
	else if(request.hasInput("jmx")) {
		//getActions(response);
		//this will run nongui jmeter
		String jmx = (String) request.getInput("jmx");
		String results = runTestPlan(jmx);
		response.setContent("running " + jmx + "\n<br />\n<br />" + results);
	}
	else {
		 response.setContent("not valid input var");
	}
	//captureScreen();
	//response.setContent(html);
	response.setMaxAge(0);
	
	return response;
}

public void createJmeterEngine() {
	//JMeterEngine jEngine = null;
	try {
		String currentPath = new File(jmeterHomePath).getCanonicalPath();
		System.out.println("Setting Jmeter Home: " + currentPath);
		JMeterUtils.setJMeterHome(currentPath);
	} catch (Exception e) {
		e.printStackTrace();
	}
	System.setProperty(JMeter.JMETER_NON_GUI, "true");
	jEngine = new StandardJMeterEngine();
    // jmeter.properties
    JMeterUtils.loadJMeterProperties(jmeterPropertiesFile);
	JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
	JMeterUtils.initLocale();
    //return jEngine;
}

public String runTestPlan(String testPlanFilename) {
	String results = "";
	createJmeterEngine();
    File f = new File(testPlanFilename);
    if (!f.exists() || !f.isFile()) {
    	results += "Could not open " + testPlanFilename;
        System.out.println(results);
        return results;
    }

	FileInputStream reader = null;
	try {
		reader = new FileInputStream(new File(testPlanFilename));
		tree = SaveService.loadTree(reader); 
		//store log file in ./FitNesseRoot/files/testResults/testPlanFilename.log
		String logFile = new File(jmeterLogPath, (new File(testPlanFilename).getName() + ".log")).getCanonicalPath();
		
        @SuppressWarnings("deprecation") // Deliberate use of deprecated ctor
        JMeterTreeModel treeModel = new JMeterTreeModel(new Object());// Create non-GUI version to avoid headless problems
        JMeterTreeNode root = (JMeterTreeNode) treeModel.getRoot();
        treeModel.addSubTree(tree, root);

        // Hack to resolve ModuleControllers in non GUI mode
        SearchByClass<ReplaceableController> replaceableControllers = new SearchByClass<ReplaceableController>(ReplaceableController.class);
        tree.traverse(replaceableControllers);
        Collection<ReplaceableController> replaceableControllersRes = replaceableControllers.getSearchResults();
        for (Iterator<ReplaceableController> iter = replaceableControllersRes.iterator(); iter.hasNext();) {
            ReplaceableController replaceableController = iter.next();
            replaceableController.resolveReplacementSubTree(root);
        }

        // Remove the disabled items
        // For GUI runs this is done in Start.java
        JMeter.convertSubTree(tree);

        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");//$NON-NLS-1$
        if (summariserName.length() > 0) {
            //log.info("Creating summariser <" + summariserName + ">");
            //println("Creating summariser <" + summariserName + ">");
            summer = new Summariser(summariserName);
        }

        if (logFile != null) {
            ResultCollector logger = new ResultCollector(summer);
            logger.setFilename(logFile);
            tree.add(tree.getArray()[0], logger);
        }
        else {
            // only add Summariser if it can not be shared with the ResultCollector
            if (summer != null) {
                tree.add(tree.getArray()[0], summer);
            }
        }

        // Used for remote notification of threads start/stop,see BUG 54152
        // Summariser uses this feature to compute correctly number of threads 
        // when NON GUI mode is used
        tree.add(tree.getArray()[0], new RemoteThreadsListenerTestElement());
		
        jEngine.configure(tree);
        jEngine.runTest();
		//reader.close();
        JOrphanUtils.closeQuietly(reader);
        Util.waitForFileToExists(logFile, 5); //wait up to 5 seconds for file to exist
        String logStr = Util.fileToString(logFile);
        logStr = logStr.replaceAll("\n", "<br/>\n");
        results += logStr;
		results += "Test " + testPlanFilename + " completed.";
        System.out.println("Test " + testPlanFilename + " completed.");
        
	} catch (Exception e) {
		e.printStackTrace();
		return results + "\r\nException: " +e.getMessage();
	}
	return results;
}


private void displayDialog(SimpleResponse response) {
	String result = "";
	try {
		//JFrame top = createOnTopJFrameParent();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					String currentPath = new File(jmeterHomePath).getCanonicalPath();
					System.out.println("Setting Jmeter Home: " + currentPath);
					System.setProperty("jmeter.home", currentPath); 
					//JMeterUtils.setJMeterHome(currentPath);
					System.out.println("Starting Jmeter with: " + jmeterPropertiesFile);
		    		//org.apache.jmeter.NewDriver.main(new String[]{"-p" + jmeterPropertiesFile});
					
					//if we start Jmeter in same process, when user closes jmeter it will call system.exit() and close oasis
					String jmeterbundleJarFull = new File(jmeterbundleJar).getCanonicalPath();
					//System.out.println("jmeterbundleJarFull:" + jmeterbundleJarFull); 
					String jmeterPropertiesFileFull = new File(jmeterPropertiesFile).getCanonicalPath();
					Util.forkJavaProcess("./plugins", jmeterbundleJarFull, new String[]{"-p" + jmeterPropertiesFileFull} );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		String currentUrl = request.getRequestUri();
		if (currentUrl.toLowerCase().contains("?jmeter"))
			currentUrl = currentUrl.substring(0, currentUrl.toLowerCase().indexOf("?jmeter"));
		result +="<script>window.location.replace('" + currentUrl + "');</script>";
		
	} catch (Exception ex) {
		ex.printStackTrace();
		result += "Exception displaying Dialog: " + ex.getMessage();
 }
	response.setContent(result);
}

@SuppressWarnings("unused")
private static JFrame createOnTopJFrameParent() {
	@SuppressWarnings("serial")
	//had to create a jframe parent for the JFileChooser to have foreground focus. 
	JFrame frame = new JFrame("parent"){
		@Override
		public void setVisible(final boolean visible) {
		  // make sure that frame is marked as not disposed if it is asked to be visible
		  if (visible) {
		      //setDisposed(false);
		  }
		  // let's handle visibility...
		  if (!visible || !isVisible()) { // have to check this condition simply because super.setVisible(true) invokes toFront if frame was already visible
		      super.setVisible(visible);
		  }
		  // ...and bring frame to the front.. in a strange and weird way
		  if (visible) {
		      int state = super.getExtendedState();
		      state &= ~JFrame.ICONIFIED;
		      super.setExtendedState(state);
		      super.setAlwaysOnTop(true);
		      super.toFront();
		      super.requestFocus();
		      super.setAlwaysOnTop(false);
		  }
		}
	};
	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
	frame.setVisible(true);
	return frame;
}

protected void initializeResponder(WikiPage root, Request request) {
this.root = root;
this.request = request;
}

protected String createPageContent() {
return pageData.getContent();
}


public SecureOperation getSecureOperation() {
return new SecureReadOperation();
}

}


