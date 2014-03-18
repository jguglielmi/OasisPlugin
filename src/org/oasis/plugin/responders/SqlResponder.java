package org.oasis.plugin.responders;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
public class SqlResponder implements SecureResponder{
    protected PageData pageData;
    protected WikiPage page;
    protected WikiPagePath path;
    protected PageCrawler crawler;
    protected WikiPage root;
    protected Request request;
    protected String content;


    public SqlResponder(){
    }

    public class GetJDBC extends JPanel {
        private String driver = null;
        private String url = null;
        private String username = null;
        private String password = null;
        private String query = null;
        JFrame parentFrame;
        SimpleResponse response;
        public GetJDBC(SimpleResponse response){
            this.response=response;
            setDriver();
            setUrl();
            setUserName();
            setPassword();
            setQuery();
            String encryptedPassword = encryptString(password);
            if(driver==null || driver=="" || url==null || url=="" || username==null || username=="" || password==null || password=="" || query==null || query=="")
                response.setContent("Parameter is missing.");
            else
            response.setContent("| set driver | " + driver + " | " + "set url | " + url + " | " + "set username | " + username + " | " + " set password | " + encryptedPassword + " | " + " set query | " + query + " | ");
        }

        public void setDriver(){
            parentFrame = createOnTopJFrameParent();
            Object[] options = {"SQLServer", "MySQL", "Oracle"};
            driver = (String)JOptionPane.showInputDialog(parentFrame, "Select the Driver", "Select the Driver", JOptionPane.PLAIN_MESSAGE, null, options, "SQLServer");
            parentFrame.dispose();
        }

        public void setUrl(){
            if(driver == "MySQL"){
                url = "jdbc:mysql://";
                driver = "com.mysql.jdbc.Driver";
            }
            else if(driver == "SQLServer"){
                url = "jdbc:sqlserver://";
                driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            }
            else{
                url = "jdbc:oracle:thin:@";
                driver = "oracle.jdbc.driver.OracleDriver";
            }
            parentFrame = createOnTopJFrameParent();
            url = url + (String)JOptionPane.showInputDialog(parentFrame, "Please enter the database URL:", "");
            parentFrame.dispose();
        }

        public void setUserName(){
            parentFrame = createOnTopJFrameParent();
            username = (String)JOptionPane.showInputDialog(parentFrame, "Please enter the username:", "");
            parentFrame.dispose();
        }

        public void setPassword(){
            parentFrame = createOnTopJFrameParent();
            password = (String)JOptionPane.showInputDialog(parentFrame, "Please enter the password:", "");
            parentFrame.dispose();
        }

        public void setQuery(){
            parentFrame = createOnTopJFrameParent();
            query = (String)JOptionPane.showInputDialog(parentFrame, "Please enter the query:", "");
            parentFrame.dispose();
        }
       
        private String encryptString(String password) {
           
            if (password != null) {
                Cipher aes = null;
                try {
                    aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    aes.init(Cipher.ENCRYPT_MODE, generateKey());
                    byte[] ciphertext = aes.doFinal(password.getBytes());
                    password = "decrypt:" + bytesToHex(ciphertext);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return password;
        }
       
        public String bytesToHex(byte[] bytes) {
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

        public SecretKeySpec generateKey() {
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

        SimpleResponse response = new SimpleResponse();
        String resource = request.getResource();
        WikiPagePath path = PathParser.parse(resource);
        PageCrawler crawler = context.root.getPageCrawler();

        page = crawler.getPage(path, new MockingPageCrawler());
        pageData = page.getData();
        content = createPageContent();


        String loadedFile = (String) request.getInput("sql");
        if (request.hasInput("jdbc")) {
            GetJDBC getJDBC = new GetJDBC(response);
            //response.setContent(jdbc);
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
        frame.setSize(frame.WIDTH+300, 130);
        frame.setVisible(true);
        return frame;
    }

}