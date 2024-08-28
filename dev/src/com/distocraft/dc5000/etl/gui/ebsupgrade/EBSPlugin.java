/**
 * Simple class that checks if EBS is enabled
 * 
 * You have to have properties file as in fileName -variable
 * to enable EBS
 *
 */
package com.distocraft.dc5000.etl.gui.ebsupgrade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author epetrmi
 * 
 */
 public class EBSPlugin {

  private static EBSPlugin instance = new EBSPlugin();
  
  private Log log = LogFactory.getLog(EBSPlugin.class);
  private Properties props = null;
  private String fileName = "/eniq/sw/conf/EBSManager.properties";
  
  //Constructor
  EBSPlugin(){
    props = loadProperties();
  }
  
  /**
   * Checks if EBSPlugin is enabled
   * @return true/false
   */
  public static boolean isEnabled() {
    if (instance.getProps() != null) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Gets properties
   * @return Properties if they exist, null if retrieving
   *          properties fail (dont exist etc.)
   */
  public static Properties getProperties(){
    return instance.getProps();
  }
  
  
  /**
   * Loads properties from file. If file does not
   * exist or something goes wrong null is returned.
   * 
   * @return Properties-object or null
   */
  private Properties loadProperties() {
    log.info("Trying to read properties for ebs");
    Properties props = null;
    FileInputStream fis = null;
    File file = null;
    boolean isError = true;
    try {
      props = new Properties();
      file = new File(fileName);
      fis = new FileInputStream(file);
      props.load(fis);
      fis.close();
      isError = false;
      log.info("Successfully loaded propertyfile:"+fileName+
          ". This means that EBSManager IS ENABLED");
    } catch (FileNotFoundException e) {
      log.error("File=" + fileName
          + " could not be found. This means that EBSManager IS DISABLED");
    } catch (IOException e) {
      log.error("IO Error : File=" + fileName
          + " could not be found. This means that EBSManager IS DISABLED");
    } finally {
      if(isError){
        props = null;
      }
    }
    return props;
  }
  
  //Package-visibility for testing purposes...
  
  void setProps(Properties props){
    instance.props = props;
  }
  
  Properties getProps(){
    return props;
  }
 
  static void setEBSPlugin(EBSPlugin e){
    
  }
  
  static EBSPlugin getEBSPlugin(){
    return instance;
  }
  
}
