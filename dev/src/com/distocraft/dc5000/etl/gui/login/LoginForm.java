/**
  * -----------------------------------------------------------------------
  *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
  * -----------------------------------------------------------------------
  */
 package com.distocraft.dc5000.etl.gui.login;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.crypto.NoSuchPaddingException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.ericsson.catalina.users.Encryption;
 
 /**
  * @author ericker
  * @since 2010
  *
  */
 public class LoginForm implements Serializable {
         
         
     /*  The properties */
     private String userName = "";
     
         private static int failureCount = 0;
         
         private static int lockOutTime; 
         
         private String defaultMessage;
         
         private static final String CRYPT_STAMP = "{AsciiCrypter}";
         
         private static final Log log = LogFactory.getLog(LoginForm.class);
         
 
         // This map has username as key and failureCount as value
     private static HashMap<String, Integer> user_map = new HashMap<>();
     
  // This map has username as key and time(in mills) at the 3rd failed attempt as value
     private static HashMap<String, Long> user_timeMap = new HashMap<>();
     
     private Properties props = new Properties();
     private static final String MESSAGE_PROPERTIES_FILE = "/eniq/sw/runtime/tomcat/webapps/adminui/conf/message.properties";
     
     // To get the failure Count of a user
     public int getFailureCount(String username) {
                 try{
                         // To read the lockOutTime from static.properties file
                         final File staticprops = new File("/eniq/sw/conf/static.properties");
                         props.load(new FileInputStream(staticprops));
                         lockOutTime = Integer.parseInt(props.getProperty("LOCK_OUT_TIME"));
                 }
                 catch(Exception e){
                         e.printStackTrace();
                 }
         if(user_map.containsKey(username)) {
                 user_map.put(username, user_map.get(username)+1);
         }else {
                 user_map.put(username, 1);
         }
         failureCount = (int)user_map.get(username);
         if(failureCount == 3) 
                 user_timeMap.put(username, System.currentTimeMillis()); 
         
         return failureCount;
     }
     
     // Returns the LockOut Time
     public static int getLockOutTime() {
                 return lockOutTime;
         }
     
     // Removes a user entry if login is successful
         public static void removeUser(String username) {
         if(user_map.containsKey(username)) 
                 user_map.remove(username);
         if(user_timeMap.containsKey(username))
                 user_timeMap.remove(username);
     }
         
         // Removes a user entry after the lockOut Time period 
     public void removeUserAfterLockout() {
         if(!user_timeMap.isEmpty()) {
                 Iterator<Map.Entry<String, Long>> itr = user_timeMap.entrySet().iterator();
                 while(itr.hasNext()) {
                         Map.Entry<String, Long> pair = itr.next();
                         if(user_timeMap.containsKey(pair.getKey())) {
                                 if(System.currentTimeMillis()-user_timeMap.get(pair.getKey()) > (lockOutTime *1000)) {
                                         user_map.remove(pair.getKey());
                                         itr.remove();
                                 }
                         }
                 }
         }
     }
     
     private String userPassword = "";
 
     public String getUserName() {
         return userName;
     }
 
     public void setUserName(final String userName) {
         this.userName = userName == null ? "" : userName.trim();
         if(this.userName != ""){
                 this.userName = htmlFilter(this.userName);
         }
     }
     
     private static String htmlFilter(String message) {
         if (message == null) return null;
         int len = message.length();
         StringBuffer result = new StringBuffer(len + 20);
         char aChar;
    
         for (int i = 0; i < len; ++i) {
            aChar = message.charAt(i);
            switch (aChar) {
                case '<': result.append("&lt;"); break;
                case '>': result.append("&gt;"); break;
                case '&': result.append("&amp;"); break;
                case '"': result.append("&quot;"); break;
                case '=': result.append("&equals;"); break;
                            case '(': result.append("&lpar;"); break;
                            case ')': result.append("&rpar;"); break;
                            case '\'': result.append("&apos;"); break;
                            case '.': result.append("&period;"); break;
                            case ';': result.append("&semi;"); break;
                default: result.append(aChar);
            }
         }
         return (result.toString());
      }
 
     public String getUserPassword() {
         return userPassword;
     }
 
     public void setUserPassword(final String userPassword) {
         this.userPassword = userPassword == null ? "" : userPassword.trim();
         if(this.userPassword != ""){
                 this.userPassword = htmlFilter(this.userPassword);
         }
     }
 
     public boolean process() {
 
         boolean canContinue = false;
         if (this.getUserName().compareTo("") != 0 && this.getUserPassword().compareTo("") != 0) {
                 canContinue = true;
         }
         return canContinue;
     }

		public boolean checkDefaults() throws IOException, InvalidKeyException, NoSuchAlgorithmException,
				NoSuchPaddingException, ParserConfigurationException {
			boolean isdefault = false;
			String stored_pw = "";
			File defaultuserfile = new File("/eniq/sw/conf/default_platform.ini");
			try {
				props.load(new FileInputStream(defaultuserfile));
			} catch (IOException e) {
				e.printStackTrace();
			}
			String def_password = props.getProperty("Default_Value");
			String encryptionFlag = props.getProperty("Encryption_Flag");
			String dec_password = "";
			if (encryptionFlag.equalsIgnoreCase("Y")) {
				dec_password = com.ericsson.eniq.repository.AsciiCrypter.getInstance().decrypt(def_password);
			} else {
				dec_password = Encryption.decrypt(def_password);
			}
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			try {
				Document document = db.parse(new File("/eniq/sw/runtime/tomcat/conf/tomcat-users.xml"));
				NodeList nodeList = document.getElementsByTagName("user");
				for (int x = 0, size = nodeList.getLength(); x < size; x++) {
					if (nodeList.item(x).getAttributes().getNamedItem("username").getNodeValue().equals(dec_password)) {
						String pass = nodeList.item(x).getAttributes().getNamedItem("password").getNodeValue();
						if (pass.contains(CRYPT_STAMP)) {
							
							stored_pw = com.ericsson.eniq.repository.AsciiCrypter.getInstance().decrypt(pass.replace(CRYPT_STAMP, ""));
							
						} else {
							stored_pw = Encryption.decrypt(pass.replace(Encryption.CRYPT_STAMP, ""));							
						}
						
					}
				}
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (stored_pw.equals(dec_password) && this.getUserPassword().compareTo(dec_password) == 0) {
				isdefault = true;
			}
			return isdefault;
		}
 
     public void clearDetails() {
         // Clear the form
         userName = "";
         userPassword = "";
     }
 
         public String getFooterMessage() {
                 File messageFile = new File(MESSAGE_PROPERTIES_FILE);
                 try {
                         boolean createNewFile = messageFile.createNewFile();
                         if (createNewFile) {
                                 log.info("Default Legal warning message file was created successfully");
                         } else {
                                 log.info("File already exists");
                         }
                 } catch (IOException e) {
                         log.error("Error while creating Legal message file");
                 }
                 defaultMessage = "IF YOU ARE NOT AN AUTHORIZED USER STOP ANY ACTIVITY.YOU ARE PERFORMING ON THIS SYSTEM AND EXIT IMMEDIATELY.\n\n"
                                 + "This system is provided for authorized and official use only.\n"
                                 + "The usage of this system is monitored and audited.\n"
                                 + "Unauthorized or improper usage may result in disciplinary actions, civil or criminal penalties.\n";
                 try {
                         props.load(new FileInputStream(messageFile));
                 } catch (IOException e) {
                         log.info("As user has not updated Legal message so default message will be shown");
                 }
                 return props.getProperty("CUSTOM_MESSAGE", defaultMessage).replaceAll("(\r\n|\n)", "<br>");
         }
 
 }