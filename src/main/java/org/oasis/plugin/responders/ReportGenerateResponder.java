package org.oasis.plugin.responders;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

public class ReportGenerateResponder implements SecureResponder {

	public String fileName="";
	public String resultsPageName="";
	public String saveLocation="";
	public static String defaultFileDir = "files/testResults";
	private static String rootPagePath = "./FitNesseRoot";
	protected WikiPage page;
	protected WikiPage root;
	protected PageData pageData;
	protected Request request;
	protected String content;

	public ReportGenerateResponder(){}


	@SuppressWarnings("serial")
	public class GetLatestReport extends JPanel {
		JFrame parentFrame;
		SimpleResponse response;
		public GetLatestReport(){
			setPage();
		}
		public void setPage(){
			parentFrame = createOnTopJFrameParent();
			resultsPageName = (String)JOptionPane.showInputDialog(parentFrame, "Please enter the page name:", "");
			parentFrame.dispose();
		} 
	}

	@SuppressWarnings("serial")
	public class ResultsChooser extends JFileChooser {
		JFrame parentFrame;

		public ResultsChooser(JFrame jFrame){
			parentFrame = jFrame;
			createFileChooser();
		}

		public String createFileChooser(){
			setAcceptAllFileFilterUsed(false);
			setMultiSelectionEnabled(false);
			setAcceptAllFileFilterUsed(false);
			setDialogTitle("Choose the XML Report");
			setCurrentDirectory(new File(getCurrentFileDirectory()));
			FileFilter xmlFilter = new FileNameExtensionFilter("XML file (*.xml)", "xml");
			addChoosableFileFilter(xmlFilter);
			setFileFilter(xmlFilter);
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

	public void createHtmlReport(String path, String file, String parent) throws IOException, InterruptedException{
		String resultsDir = path;
		resultsDir = resultsDir + "\\htmlResults";
		String url = "http://localhost:8000/" + parent + "?pageHistory&resultDate=" + file;
		Runtime run = Runtime.getRuntime();
		run.exec("cmd.exe /c cd \""+path+"\" & start cmd.exe /c \"rm -rf htmlResults\"");
		Thread.sleep(60000);
		run.exec("cmd.exe /c cd \""+path+"\" & start cmd.exe /c mkdir htmlResults");
		Thread.sleep(8000);
		run.exec("cmd.exe /c cd \""+path+"\" & start cmd.exe /c wget -P " + "\"" + resultsDir + "\"" + " -E -H -k -p " + "\"" + url + ".html" + "\"");		
		//System.out.println("cmd.exe /c cd \""+path+"\" & start cmd.exe /c wget -P " + "\"" + resultsDir + "\"" + " -E -H -k -p " + "\"" + url + ".html" + "\"");		

	}

	public void createLatestHtmlReport(String path, String url) throws IOException, InterruptedException{
		Runtime run = Runtime.getRuntime();
		run.exec("cmd.exe /c start cmd.exe /c rm -rf " + path);
		Thread.sleep(60000);
		run.exec("cmd.exe /c start cmd.exe /c mkdir " + path);
		Thread.sleep(8000);
		run.exec("cmd.exe /c cd \""+path+"\" & start cmd.exe /c wget -P " + "\"" + path + "\"" + " -E -H -k -p " + "\"" + url + "\"");		

	}

	public void displayMessage(String message){
		JFrame frame = createOnTopJFrameParent();
		JOptionPane.showMessageDialog(frame, message);
		frame.dispose();
	}

	public Response makeResponse(FitNesseContext context, Request request) throws IOException, InterruptedException {
		boolean nonExistent = request.hasInput("nonExistent");
		return doMakeResponse(context, request, nonExistent);
	}

	public Response makeResponseForNonExistentPage(FitNesseContext context, Request request) throws IOException, InterruptedException {
		return doMakeResponse(context, request, true);
	}

	protected Response doMakeResponse(FitNesseContext context, Request request, boolean firstTimeForNewPage) throws IOException, InterruptedException {
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
		String loadedFile = (String) request.getInput("reportGenerate");
		if (request.hasInput("select")) { 
			JFrame jFrame =  createOnTopJFrameParent();
			ResultsChooser resultsChooser = new ResultsChooser(jFrame);
			jFrame.dispose();
			if(fileName=="" || fileName==null){
				displayMessage("No file was selected or field was blank.");
				response.setContent("no file selected");
			}
			else{
				displayMessage("Your report will take roughly 3 minutes to generate.  A pop up box will notify you when your report is ready.");
				String filePath = fileName.substring(0,fileName.lastIndexOf('\\'));
				String parentDirectory = filePath.substring(filePath.lastIndexOf('\\')+1,filePath.length());
				saveLocation = "/FitNesseRoot/files/testResults/" + parentDirectory + "/htmlResults";
				fileName = fileName.substring(fileName.lastIndexOf('\\')+1, fileName.lastIndexOf('.'));
				createHtmlReport(filePath, fileName, parentDirectory);
				Thread.sleep(120000);
				displayMessage("HTML file has been generated to: " + saveLocation);
			}
			//runCommand("wget -E -H -k -p " + "\"http://localhost:8000/" + ); //OasisExamples.SimpleSikuliTest1?pageHistory&resultDate=20140329055229
			//htmlReportDir
			//html += "get image " + img + " ";
		}
		else if (request.hasInput("latest")){
			GetLatestReport latestReport = new GetLatestReport();
			File resultsFile = new File("./FitNesseRoot/files/testResults/" + resultsPageName);
			if(!resultsFile.exists() || resultsFile==null){
				displayMessage("No file was selected or field was blank.");
				response.setContent("Page does not exist");
			}
			else{
				displayMessage("Your report will take roughly 3 minutes to generate.  A pop up box will notify you when your report is ready.");
				String resultsPageUrl = "http://localhost:8000/"+resultsPageName + "?pageHistory&resultDate=latest";
				String fullPath = "./FitNesseRoot/files/testResults/" + resultsPageName+ "/latest/";
				createLatestHtmlReport(fullPath, resultsPageUrl);
				Thread.sleep(120000);
				saveLocation = fullPath.substring(1,fullPath.length());
				displayMessage("HTML file has been generated to: " + saveLocation);
			}
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
