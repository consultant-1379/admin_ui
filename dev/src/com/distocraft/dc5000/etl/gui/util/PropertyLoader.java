/**
 * Class for getting general.properties
 */
package com.distocraft.dc5000.etl.gui.util;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author epetrmi
 *
 */
public abstract class PropertyLoader {

  private static Logger log = Logger.getLogger(PropertyLoader.class);
  private static final String IS_RESETABLE_PROPERTY = "propertyloader.resetable";
  private static boolean isResetable = true;
  private static Properties props = null;
  
  //Loading the properties first time
  {
    loadProperties();
  }
  
  /**
   * Gets properties file
   * 
   * @return - Properties file or null
   */
  public static Properties getProperties(){
    if(props==null){
      loadProperties();
    }
    return props;
  }
  
  /**
   * Resets Properties so that the next Property call
   * will cause re-reading of properties. In general,
   * you should know what your doing when using this.
   * 
   * @return true, if reset is done. False, if not done.
   */
  public static boolean reset(){
    if(isResetable){
      props = null;
      return true;//Reset ok
    }else{
      return false;//Reset not ok (cannot be reseted)
    }
  }
 
  /**
   * Gets property from loaded Properties
   * 
   * @param key
   * @param defaultValue
   * @return - value if it exist. Otherwise defaultValue
   */
  public static String getProperty(final String key, final String defaultValue){
    String ret = defaultValue;
    try{
      ret = getProperties().getProperty(key, defaultValue);
      
    }catch(Exception e){
      log.equals("Failed to getProperty(" +
      		"key="+key+", " +
      				"defaultValue="+defaultValue+") " +
      						"from Properties="+props.toString());
     }
    return ret; 
  }
  
  /**
   * Does the actual loading of properties
   * Current implementation reads it from 
   * general.properties -file
   * 
   */
  private static void loadProperties() {
    log.debug("Loading properties...");
    // Read properties file.
    final Properties properties = new Properties();
    try {
        // Load properties
       // properties.load(new FileInputStream("general.properties"));
        //properties.load(PropertyLoader.class.getResourceAsStream("general.properties"));
      final ClassLoader cl = PropertyLoader.class.getClassLoader();
       properties.load(cl.getResourceAsStream("general.properties"));
      
       props = properties;
       
        if(props!=null){
          //If there's propertyloader.resetable=false in properties
          //then the properties cannot be re-initialized. Otherwise calling
          //reset() method sets properties null (and its re-initialized)
          if("false".equalsIgnoreCase( getProperty(IS_RESETABLE_PROPERTY, "true") )){
            isResetable = false;
            log.debug("PropertyLoader isResetable=false");
          }
        }
        log.info("Properties loaded succesfully!");
    } catch (Exception e) {
      log.error("Failed to load general.properties. " +
      		"You should put general.properties file to classes-root-dir. " +
      		"Otherwise some dependend classes cant get required properties", e);
    }
  }
  
}
