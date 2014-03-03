package org.oasis.plugin.responders;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

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

@SuppressWarnings("serial")
public class FileReaderResponder implements SecureResponder{

	public String fileName="";
	public static String defaultFileDir = "files/testData";
	private static String rootPagePath = "./FitNesseRoot";
	protected WikiPage page;
	protected WikiPage root;
	protected PageData pageData;
	protected Request request;
	protected String content;

	public FileReaderResponder(){
	}

	public class FileReaderChooser extends JFileChooser {
		JFrame parentFrame;

		public FileReaderChooser(JFrame jFrame){
			parentFrame = jFrame;
			createFileChooser();
		}
		
		public String createFileChooser(){
			setAcceptAllFileFilterUsed(false);
			setMultiSelectionEnabled(false);
			setAcceptAllFileFilterUsed(false);
			setDialogTitle("Choose file");
			setCurrentDirectory(new File(getCurrentFileDirectory()));
			FileFilter excelFilter = new FileNameExtensionFilter("Excel spreadsheet (*.xlsx)", "xlsx");
			FileFilter textFilter = new FileNameExtensionFilter("Text file (*.txt)", "txt");
			addChoosableFileFilter(excelFilter);
			addChoosableFileFilter(textFilter);
			setFileFilter(excelFilter);
			setFileFilter(textFilter);
			int response = showOpenDialog(parentFrame);
			if(response == APPROVE_OPTION){
				try {
				fileName = getSelectedFile().getCanonicalPath();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(response == CANCEL_OPTION){
				fileName = "";
			}
			else{
				fileName = "";
			}
			return fileName;
		}
	}
	
	

	public String getFile(){
		return fileName;
	}

	public void setFile(String fileName){
		this.fileName=fileName;
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

		//String html = doMakeHtml(resource, context, firstTimeForNewPage);
		//String html = "";
		//html += "resources: " + resource + "\r\n "; 
		String loadedFile = (String) request.getInput("fileRead");
		if (request.hasInput("select")) { 
			JFrame jFrame =  createOnTopJFrameParent();
			FileReaderChooser fileReaderChooser = new FileReaderChooser(jFrame);
			response.setContent(fileName);
			jFrame.dispose();
			//html += "get image " + img + " ";
		}
		else if (request.hasInput("test")){
			response.setContent("test is successful");
		}
		else {
			response.setContent("not valid input var");
		}
		//captureScreen();
		//response.setContent(html);
		response.setMaxAge(0);

		return response;
	}


	public SecureOperation getSecureOperation() {
		return new SecureReadOperation();
	}

	protected String createPageContent() {
		return pageData.getContent();
	}

	protected void initializeResponder(WikiPage root, Request request) {
		this.root = root;
		this.request = request;
	}


	private String getCurrentFileDirectory() {
		File fileReadDirectory = new File(rootPagePath, defaultFileDir);
		try {
			return fileReadDirectory.getCanonicalPath();
		} catch (Exception e) {
			e.printStackTrace();
			return fileReadDirectory.getAbsolutePath();
		}
	}
	
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


}
