// Name: SikuliResponder
// Author: Edward Jakubowski ejakubowski@qed-sys.com
// Last update: 11/07/2013
// Description: This responder add's back-end support for both capturing and displaying pictures using 
//              the sikuli-api library.
// Requirements: sikuli-api.jar, and ResponderFactory.java has: addResponder("sik", SikuliResponder.class);
// Examples:
//   take screenshot - http://localhost:8000/?sik&capture
//   display picture - http://localhost:8000/?sik&img=test.png
//   show loaded properties - http://localhost:8000/?sik&props
//   show image path - http://localhost:8000/?sik&path
//
//   <input id="sikuliCapture" type="button" value="Sikuli Capture" />
//   <script type="text/javascript">
//   $('#sikuliCapture').click(function () {
//     $.get("/?sik", function( data ) {
//       var tbox = $("#pageContent");
//       tbox.val(tbox.val() + '!-<img src="' + data + '" />-!\n');
//     });
//   });
//   </script>
//
//
//
//        // Add to FitNesse to initialize some sikuli stuff
//        SikuliResponder.loadProperties();
//        SikuliResponder.startupCommand();
//        SikuliResponder.setHotKey();

//        // to cleanup sikuli 
//        SikuliResponder.shutdown();


package org.oasis.plugin.responders;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URLConnection;
import java.util.Date;
import java.util.Properties;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

import org.oasis.plugin.Util;
import org.sikuli.basics.*;
import org.sikuli.script.*;

public class SikuliResponder implements SecureResponder {
	
//public static final String defaultScriptDir = "files/sikuliScripts";
public static String defaultImageDir = "files/sikuliScripts";
public static String saveImagePath = defaultImageDir;
public static boolean defaultPromptImageFilename = true;
public static boolean promptImageFilename = defaultPromptImageFilename;
public static boolean defaultAppendTestPage = true;
public static boolean appendTestPage = defaultAppendTestPage;
public static int captureTimeout = 0;
public static String defaultImageExtension = ".png";
public static String defaultTableHeader = "";
public static String tableHeader = defaultTableHeader;
public static boolean defaultShowToolbar = true;
public static boolean showToolbar = defaultShowToolbar;
public static String defaultCaptureHotkey = "2";
public static String captureHotkey = defaultCaptureHotkey;
public static int captureHotkeyModifier = KeyModifier.CTRL + KeyModifier.SHIFT;
public static String startupCommand = "";
public static final String defaultPropertiesFilename = "sikuli.properties";


/* Example sikuli.properties file:

saveImagePath=files/sikuliScripts/images
appendTestPage=true
captureTimeout=1000
promptImageFilename=true

 */


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
private static String rootPagePath = "./FitNesseRoot";
private static String pomXmlFile = "./pom.xml";
private static String pomXmlStr = "";

public static String CAPTURE_FILENAME = "";
public static boolean CAPTURE_STATUS = false;

public SikuliResponder() {
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
 loadProperties();

 //String html = doMakeHtml(resource, context, firstTimeForNewPage);
 //String html = "";
 //html += "resources: " + resource + "\r\n "; 
 String img = (String) request.getInput("img");
 if (request.hasInput("img")) { 
 loadImage(img, response);
 //html += "get image " + img + " ";
 }
 else if (request.hasInput("props")) {
	 displayProperties(response);
 }
 else if (request.hasInput("path")) {
	 response.setContent(getCurrentImageDirectory());
 }
 else if (request.hasInput("toffset")) {
	 setTargetOffset(response);
 }
 else if (request.hasInput("sense")) {
	 setSensitivity(response);
 }
 else if (request.hasInput("table")) {
	 response.setContent(tableHeader);
 }
 else if (request.hasInput("tools")) {
	 if (!showToolbar)
		 response.setContent("false");
	 else 
		 response.setContent("");
 }
 else if (request.hasInput("hotkey")) {
	 setHotKey();
 	 response.setContent("capture hotkey set: " + captureHotkey + "," + captureHotkeyModifier + ".");
 }
 else if (request.hasInput("capture")) {
	 //html += "take image ";
	 captureScreenResponder(response);
 }
 else if (request.hasInput("list")) {
	 listImages(response);
 }
 else if (request.hasInput("encrypt")) {
	 encryptString(response);
 }
 else if (request.hasInput("version")) {
	 getVersion(response);
 }
 else if (request.hasInput("shutdown")) {
	 System.exit(0);
 }
 else {
	 response.setContent("not valid input var");
 }
 //captureScreen();
 //response.setContent(html);
 response.setMaxAge(0);

 return response;
}

public static void loadProperties(){
	File propFile = new File(rootPagePath, defaultPropertiesFilename);
	if (propFile.exists() == false)
		return;
	Properties prop = new Properties();	 
	try {
           //load a properties file
		prop.load(new FileInputStream(propFile));
		saveImagePath = prop.getProperty("saveImagePath",defaultImageDir);
		appendTestPage = new Boolean(prop.getProperty("appendTestPage",defaultAppendTestPage + ""));
		promptImageFilename = new Boolean(prop.getProperty("promptImageFilename",defaultPromptImageFilename + ""));
		captureTimeout = Integer.parseInt(prop.getProperty("captureTimeout", "0"));
		tableHeader = prop.getProperty("tableHeader",defaultTableHeader);
		showToolbar = new Boolean(prop.getProperty("showToolbar",defaultShowToolbar + ""));
		captureHotkey = prop.getProperty("captureHotkey", defaultCaptureHotkey);
		startupCommand = prop.getProperty("startupCommand", "");
	} catch (Exception ex) {
		ex.printStackTrace();
    }
}

public static void startupCommand() {
	if (startupCommand == "")
		return;
	String executionPath = System.getProperty("user.dir");
    System.out.println("startupCommand at =>"+executionPath.replace("\\", "/") + " " + startupCommand);
	try {
		final Runtime rt = Runtime.getRuntime();
		rt.exec(startupCommand);
	} catch (IOException e) {
		e.printStackTrace();
	}
}

public static void shutdown() {
	if (captureHotkey != "") {
		HotkeyManager.getInstance().removeHotkey(captureHotkey, captureHotkeyModifier);
		HotkeyManager.getInstance().cleanUp();
	}
}

public static void setHotKey() {
	if (captureHotkey == "")
		return;
	 //int mod = KeyModifier.CTRL + KeyModifier.SHIFT;
	 HotkeyManager.getInstance().addHotkey(captureHotkey, captureHotkeyModifier, new HotkeyListener() {
		 public void hotkeyPressed(HotkeyEvent e) {
			 //System.out.println("hotkey pushed");
			 captureScreenGlobalKeyPress();
		 }
	 });
	 HotkeyManager.getInstance().addHotkey("0", captureHotkeyModifier, new HotkeyListener() {
		 public void hotkeyPressed(HotkeyEvent e) {
			 System.exit(0);
		 }
	 });
}

public static void getVersion(SimpleResponse response) {
	String val = "x";
	try {
		if (pomXmlStr == "")
			pomXmlStr = Util.fileToString(pomXmlFile);
		val = Util.evaluateXpathGetValue(pomXmlStr, "//*[local-name()='testframework.version']/text()");
	} catch (Exception ex) {
		ex.printStackTrace();
	}
	response.setContent("version: " + val);
}

private void displayProperties(SimpleResponse response) {
	String propStr = "SikuliResponder Properties: <br />\n";
	Properties prop = new Properties();	 
	try {
           //load a properties file
		prop.load(new FileInputStream(new File(rootPagePath, defaultPropertiesFilename)));
        for(Object k : prop.keySet()){
            String key = (String)k;
            propStr += key + " = " + prop.getProperty(key) + "<br />\n";
        }

	} catch (Exception ex) {
		ex.printStackTrace();
		propStr += "Exception Reading Properties: " + ex.getMessage();
    }
	response.setContent(propStr);
}

private String getSikuliPageName(boolean includePostfix) {
	if (includePostfix)
		return page.getName() + ".sikuli";
	else
		return page.getName();
}

private String getCurrentImageDirectory() {
	File imgFile = new File(rootPagePath, saveImagePath);
	if (appendTestPage)
		imgFile = new File(imgFile, getSikuliPageName(true));
	try {
		return imgFile.getCanonicalPath();
	} catch (Exception e) {
		e.printStackTrace();
		return imgFile.getAbsolutePath();
	}
}

public String buildImageUrl(String imageFilename, boolean includeTestPage) {
	String html = "";
	
	String extension = defaultImageExtension;
	if (imageFilename.endsWith(defaultImageExtension))
		extension = "";
	
	if (appendTestPage && includeTestPage)
		html = getSikuliPageName(false) + "?sik&img=" + imageFilename + extension;
	else
		html = "/?sik&img=" + imageFilename + extension; 
	return html;
}

private String buildImageList(String imageDirectory, boolean includeTestPage) {
	File imgDir = new File(imageDirectory);
	File [] list = imgDir.listFiles();
	StringBuilder sb = new StringBuilder("");
	for (File f : list) {
		if (f.toString().toUpperCase().endsWith(".PNG")) {
			sb.append("<li><a id='sikuliCapturedItem' href='#img" + f.getName() + "'>" + f.getName() + " <img src='" + buildImageUrl(f.getName(), includeTestPage) + "' width='20' height='20' /></a></li>\n");
		}
	}
	return sb.toString();
}

private void listImages(SimpleResponse response) {
	String html = "";
	html += buildImageList(new File(rootPagePath, saveImagePath).getAbsolutePath(), false);
	html += buildImageList(getCurrentImageDirectory(), true);
	response.setContent(html);
}

//private static Cipher aes;

public static String bytesToHex(byte[] bytes) {
	final char[] hexArray = "0123456789ABCDEF".toCharArray();
    char[] hexChars = new char[bytes.length * 2];
    int v;
    for ( int j = 0; j < bytes.length; j++ ) {
        v = bytes[j] & 0xFF;
        hexChars[j * 2] = hexArray[v >>> 4];
        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
}

public static SecretKeySpec generateKey() {
	String passStr="XebiumIsConnectedToTheSikuliBone";
	SecretKey tmp = null;
	try{
		String saltStr = "fijsd@#saltr9jsfizxnv";
		byte[] salt = saltStr.getBytes();
		int iterations = 43210;
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		tmp = factory.generateSecret(new PBEKeySpec(passStr.toCharArray(), salt, iterations, 128));
	}
	catch(Exception e){
		e.printStackTrace();
	}
	return new SecretKeySpec(tmp.getEncoded(), "AES");
}

private void encryptString(SimpleResponse response) {
	String html = "";
	JFrame frame = createOnTopJFrameParent();
	frame.setFocusableWindowState(true);
	//CAPTURE_FILENAME =JOptionPane.showInputDialog("Please input a image name", CAPTURE_FILENAME);
	String clearTxt = JOptionPane.showInputDialog(frame, "Please enter string to encrypt:", "");
	frame.dispose();
	
	if (clearTxt != null) {
		Cipher aes = null;
		try {
			aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aes.init(Cipher.ENCRYPT_MODE, generateKey());
			byte[] ciphertext = aes.doFinal(clearTxt.getBytes());
			html = "decrypt:" + bytesToHex(ciphertext);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	response.setContent(html);
}

private void loadImage(String imageFilename, SimpleResponse response) {
	//FileUtil.getDirectoryListing
	File imgFile = new File(getCurrentImageDirectory(), imageFilename);
	if (!imgFile.exists())//if image doesn't exist check if different path was provided
		imgFile = new File(new File(rootPagePath, saveImagePath), imageFilename);
	String imgPath = imgFile.getAbsolutePath();
	String mimeType= URLConnection.guessContentTypeFromName(imgPath);
	//response.addHeader("Content-Type", mimeType);
	response.setContentType(mimeType);
	response.addHeader("Content-Length", String.valueOf(imgFile.length()));
	response.addHeader("Content-Disposition", "inline; filename=\"" + imageFilename + "\"");

	try {
		byte [] fileData = new byte[(int)imgFile.length()];
		DataInputStream dis = new DataInputStream(new FileInputStream(imgFile));
		dis.readFully(fileData);
		dis.close();
		response.setContent(fileData);
		fileData = null;
	} catch (Exception ex) {
		ex.printStackTrace();
	}
}

public static String captureScreen(final String imageDirectory) {
	//if (CAPTURE_STATUS)
	//	return; //only let one capture through at a time
	//System.out.println("Capturing screen image dir: " + imageDirectory);
	try { Thread.sleep(captureTimeout); } catch (InterruptedException e) {}
	final String returnImageFilename;
	CAPTURE_STATUS = true;
	CAPTURE_FILENAME = (new Date()).getTime() + "";
	Screen s = new Screen();
	final OverlayCapturePrompt cp = new OverlayCapturePrompt(s, null);
	//final CapturePrompt cp = new CapturePrompt(s);
	cp.prompt("Select an image");
	cp.addObserver(new EventObserver() {
		public void update(EventSubject s){
			ScreenImage img = cp.getSelection();
			cp.close();
			if (promptImageFilename)
				CAPTURE_FILENAME = JOptionPane.showInputDialog("Please input a image name", CAPTURE_FILENAME);
				if (CAPTURE_FILENAME == null) {
					CAPTURE_STATUS = false;
					return;
				}
			try {
				File newImgDir = new File(imageDirectory);
				newImgDir.mkdirs();
				File newImgFile = new File(newImgDir, CAPTURE_FILENAME + defaultImageExtension);
				if (newImgFile.exists()) { //if image file already exists ask user if you would like to replace it
					int dialogResult = JOptionPane.showConfirmDialog (null, "Would you like to replace the already existing image file '" + CAPTURE_FILENAME + "' ?", "Warning", JOptionPane.YES_NO_OPTION);
					if(dialogResult == JOptionPane.NO_OPTION){
						CAPTURE_STATUS = false;
						CAPTURE_FILENAME = null;
						return;
					}
				}
				FileOutputStream fos = new FileOutputStream(newImgFile);
				ImageIO.write(img.getImage(), "png", fos);
				fos.flush();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
				CAPTURE_FILENAME = null;
			}
			img = null;
			//cp.close();
			CAPTURE_STATUS = false;
		}		
	});
	while (CAPTURE_STATUS) {
		try { Thread.sleep(500); } catch (InterruptedException e) {}
	}
	if (CAPTURE_FILENAME == null) //user canceled or there was an error saving
		return null;
	
	File newImgFile = new File(imageDirectory, CAPTURE_FILENAME + defaultImageExtension);
	try {
		returnImageFilename = newImgFile.getCanonicalPath();
	} catch (IOException e) {
		e.printStackTrace();
		return null;
	}
	return returnImageFilename;
}

private void captureScreenResponder(final SimpleResponse response) {
	
	String capImageFilename = captureScreen(getCurrentImageDirectory());
	if (capImageFilename == null)
		return;
	File imgFile = new File(getCurrentImageDirectory(), CAPTURE_FILENAME + defaultImageExtension);
	String html = imgFile.toString();
	if (html.startsWith(rootPagePath))
		html = html.substring(rootPagePath.length());
	//override the above string to just have the small link path
	html = buildImageUrl(CAPTURE_FILENAME, appendTestPage);
	response.setContent(html);
}

public static void captureScreenGlobalKeyPress() {
	String imgPath = new File(rootPagePath, saveImagePath).toString();
	String targetPath = "";
	String capImageFilename = captureScreen(imgPath);
	if (capImageFilename == null)
		return;
	
	try {
		JFrame frame = createOnTopJFrameParent();
		JFileChooser chooser = new JFileChooser();
	    chooser.setCurrentDirectory(new File(imgPath));
	    chooser.setDialogTitle("Choose Directory For Screen Capture");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    // disable the "All files" option.
	    chooser.setAcceptAllFileFilterUsed(false);
	    if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) { 
	    	targetPath = chooser.getSelectedFile().getCanonicalPath();
	    	//System.out.println("targetPath : " +  chooser.getSelectedFile());
	    }
	    frame.dispose();
	    // move image from base sikuli script directory to the selected directory
	    if (targetPath != "") {
	    	File imgFile = new File(imgPath, CAPTURE_FILENAME + defaultImageExtension);
	    	imgFile.renameTo(new File(targetPath, CAPTURE_FILENAME + defaultImageExtension));
	    }
	} catch (Exception e) {
		e.printStackTrace();
	}

	
}

private void setSensitivity(SimpleResponse response) {
	JFrame frame = createOnTopJFrameParent();
	final JOptionPane optionPane = new JOptionPane();
	JSlider slider = new JSlider();
	slider.setMajorTickSpacing(10);
	slider.setPaintTicks(true);
	slider.setPaintLabels(true);
	ChangeListener changeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent changeEvent) {
			JSlider theSlider = (JSlider) changeEvent.getSource();
			if (!theSlider.getValueIsAdjusting()) {
			optionPane.setInputValue(new Integer(theSlider.getValue()));
			}
		}
	};
	slider.addChangeListener(changeListener);
	slider.setValue(75);
	Object[] message = new Object[] { "Set Sikuli Image Sensitivity: ", slider };
	Object[] options = { "OK", "Cancel" };
	int n = JOptionPane.showOptionDialog(frame, message, "", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[1]);
	if (optionPane.getInputValue() != null && n == JOptionPane.OK_OPTION) {
		response.setContent(((Double.parseDouble(optionPane.getInputValue()+"")) / 100.0) + "");
	}
	else
		response.setContent("");
	frame.dispose();
	//System.out.println("Input: " + optionPane.getInputValue());
}

private void setTargetOffset(SimpleResponse response) {
	try {
		JFrame frame = createOnTopJFrameParent();
		JFileChooser chooser = new JFileChooser();
		ImagePreviewPanel preview = new ImagePreviewPanel();
		//PatternPaneTargetOffset preview = new PatternPaneTargetOffset();
		chooser.setAccessory(preview);
		chooser.addPropertyChangeListener(preview);
		chooser.setDialogTitle("Image Target Offset");
		chooser.setCurrentDirectory(new File(getCurrentImageDirectory()));
		//chooser.showSaveDialog(null);
		int returnVal = chooser.showDialog(frame, "Set Target Offset");
		frame.dispose();
		// 1 = cancel
		// 0 = set target
		if (returnVal == JFileChooser.APPROVE_OPTION ) {
			response.setContent(chooser.getSelectedFile().getName() + "|" + preview.target.x + "," + preview.target.y );
		}
		
	} catch (Exception e) {
		e.printStackTrace();
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


@SuppressWarnings("serial")
class ImagePreviewPanel extends JPanel implements PropertyChangeListener, MouseListener {
	
	public Location target = new Location(0,0);
	private int width, height;
	double ratio = 1.0;
	private ImageIcon icon;
	private Image image = null;
	private static final int ACCSIZE = 300;
	private Color bg;
	
	
	public ImagePreviewPanel() {
		setPreferredSize(new Dimension(ACCSIZE, -1));
		bg = getBackground();
		addMouseListener(this);
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		String propertyName = e.getPropertyName();
		
		// Make sure we are responding to the right event.
		if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
			File selection = (File)e.getNewValue();
			String name;
			
			if (selection == null)
			    return;
			else
			    name = selection.getAbsolutePath();
			
			/*
			 * Make reasonably sure we have an image format that AWT can
			 * handle so we don't try to draw something silly.
			 */
			if ((name != null) && name.toLowerCase().endsWith(".jpg") ||
			name.toLowerCase().endsWith(".jpeg") ||	name.toLowerCase().endsWith(".gif") ||
			name.toLowerCase().endsWith(".png")) {
				icon = new ImageIcon(name);
				image = icon.getImage();
				target = new Location(0,0);
				scaleImage();
				repaint();
			}
		}
	}
	
	private void scaleImage() {
		width = image.getWidth(this);
		height = image.getHeight(this);
		ratio = 1.0;
		/* 
		 * Determine how to scale the image. Since the accessory can expand
		 * vertically make sure we don't go larger than 150 when scaling
		 * vertically.
		 */
		if (width >= height) {
			ratio = (double)(ACCSIZE-5) / width;
			width = ACCSIZE-5;
			height = (int)(height * ratio);
		}
		else {
			if (getHeight() > (ACCSIZE)) {
				ratio = (double)(ACCSIZE-5) / height;
				height = ACCSIZE-5;
				width = (int)(width * ratio);
			}
			else {
				ratio = (double)getHeight() / height;
				height = getHeight();
				width = (int)(width * ratio);
			}
		}
		image = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
	}
	
	Point convertScreenToView(Location loc) {
		Point ret = new Point();

		ret.x = (int) (getWidth() / 2 + loc.x * ratio);
		ret.y = (int) (getHeight() / 2 + loc.y * ratio);

		return ret;
	}
	
	Location convertViewToScreen(Point p) {
		Location ret = new Location(0, 0);

		//ret.x = (int) ((p.x - image.getWidth(this) / 2) / ratio);
		//ret.y = (int) ((p.y - image.getHeight(this) / 2) / ratio);
		ret.x = (int) ((p.x - this.getWidth() / 2) / ratio);
		ret.y = (int) ((p.y - this.getHeight() / 2) / ratio);
		return ret;
	}
	
	void paintTarget(Graphics2D g2d) {
		final int CROSS_LEN = 20 / 2;
		Point l = convertScreenToView(target);
		g2d.setColor(Color.BLACK);
		g2d.drawLine(l.x - CROSS_LEN, l.y + 1, l.x + CROSS_LEN, l.y + 1);
		g2d.drawLine(l.x + 1, l.y - CROSS_LEN, l.x + 1, l.y + CROSS_LEN);
		g2d.setColor(Color.WHITE);
		g2d.drawLine(l.x - CROSS_LEN, l.y, l.x + CROSS_LEN, l.y);
		g2d.drawLine(l.x, l.y - CROSS_LEN, l.x, l.y + CROSS_LEN);
	}
	
	public void paintComponent(Graphics g) {
		g.setColor(bg);
		
		/*
		 * If we don't do this, we will end up with garbage from previous
		 * images if they have larger sizes than the one we are currently
		 * drawing. Also, it seems that the file list can paint outside
		 * of its rectangle, and will cause odd behavior if we don't clear
		 * or fill the rectangle for the accessory before drawing. This might
		 * be a bug in JFileChooser.
		 */
		g.fillRect(0, 0, ACCSIZE, getHeight());
		g.drawImage(image, getWidth() / 2 - width / 2 + 5, getHeight() / 2 - height / 2, this);
		if (image != null)
			paintTarget((Graphics2D) g);
	}

	@Override
	public void mousePressed(MouseEvent me) {
		target = convertViewToScreen(me.getPoint());
		//System.out.println("click: " + me.getPoint() + " -> " + target.toStringShort());
		//setTarget(tar.x, tar.y);
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent me) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
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

