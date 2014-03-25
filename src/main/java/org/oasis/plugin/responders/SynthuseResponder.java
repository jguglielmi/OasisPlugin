//Name: SynthuseResponder
//Author: Edward Jakubowski ejakubowski@qed-sys.com
//Last update: 01/28/2014
//Description: This responder add's back-end support for the Synthuse library.
//Requirements: synthuse.jar, and ResponderFactory.java has: addResponder("synth", SynthuseResponder.class);
//Examples:
//


package org.oasis.plugin.responders;



import java.awt.Dimension;
import java.awt.Toolkit;

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

import org.synthuse.SynthuseDlg;

public class SynthuseResponder implements SecureResponder {


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

public static SynthuseDlg SYNTHUSE_DIALOG = null;

public SynthuseResponder() {
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
	else if(request.hasInput("actions")) {
		 getActions(response);
	}
	else {
		 response.setContent("not valid input var");
	}
	//captureScreen();
	//response.setContent(html);
	response.setMaxAge(0);
	
	return response;
}

@SuppressWarnings("serial")
private void displayDialog(SimpleResponse response) {
	String result = "";
	try {
		//JFrame top = createOnTopJFrameParent();

		SYNTHUSE_DIALOG = new SynthuseDlg(){
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
		SYNTHUSE_DIALOG.showDialog();
		//System.out.println("***************************Synthuse closed.");
	} catch (Exception ex) {
		ex.printStackTrace();
		result += "Exception displaying Dialog: " + ex.getMessage();
 }
	response.setContent(result);
}

private void getActions(SimpleResponse response) {
	String result = "";
	if (SYNTHUSE_DIALOG == null) {
		//do nothing
		result = "";
	}
	else {
		try {
			while (SynthuseDlg.actionListQueue.size() > 0) {
				result += SynthuseDlg.actionListQueue.get(0) + "\n";
				SynthuseDlg.actionListQueue.remove(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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


