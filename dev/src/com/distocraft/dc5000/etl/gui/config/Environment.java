package com.distocraft.dc5000.etl.gui.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.distocraft.dc5000.etl.gui.util.Helper;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * 
 * Environment initialize Torque (and logs ...). Changed to use dd, that no
 * hardcoding is needed.
 * @author Jukka Karvanen, Jani Vesterinen
 */

public class Environment {

  /**
   * Static environment entry, only one instance per application is used to handle environment entrys.
   */
  static protected Environment env;

  private String propertyFile;

  private Properties prop = new Properties();
  /**
   * if Torque has been initialized or not.
   */
  //static private boolean torqueInitialized = false;

  /**
   * Receives information from dc5000 properties. (Actually only logger properties are needed.)
   * @param initTorque
   * @param realpath
   * @throws Exception
   */
  protected Environment(boolean initTorque, String realpath) throws Exception {
    try {
      propertyFile = System.getProperty("Dc5000.properties", realpath +  Helper.getEnvEntryString("conffiles") + Helper.PATHSEPERATOR  + "adminui.properties");
      prop.load(new FileInputStream(propertyFile));
    } catch (FileNotFoundException e1) {
     // System.out.println("Dc5000.properties failed:" + e1.getMessage());
      throw new Exception();

    } catch (IOException e1) {
      //System.out.println("Dc5000.properties failed:" + e1.getMessage());
    }
    
    /*
    if (initTorque)
      initTorque();
    */
    // Log4j configuration in dc5000.properties file
    PropertyConfigurator.configure(prop);

  }

  /**
   * Initializes Torque.
   * @throws Exception
   */
  /*
  private void initTorque() throws Exception {
    // Read first from dc5000 properties and then System properties
    String torqueIni = getProperty("Torque.properties");
    if (torqueIni == null)
      torqueIni = System.getProperty("Torque.properties");

    // Do not init if not set
    if (torqueIni != null) {
      try {
        // configuration
        Torque.init(torqueIni);
        // Log init
      } catch (TorqueException e) {
        System.out.println("Torque.init failed:" + e.getMessage());
        throw new Exception(e.getMessage());
      }
    }
    torqueInitialized = true;
  }
*/
  /**
   * Gets environment entry values.
   * @param realpath
   * @return environment
   */
  public static Environment getEnv(String realpath) {
    if (env == null) {
      try {
        env = new Environment(true, realpath);
      } catch (Exception e) {
        return null;
      }
    } 
    /*
    else {
      if (!torqueInitialized) {
        try {
          env.initTorque();
        } catch (Exception e) {
          return null;
        }
      }
    } */
    return env;
    
  }

  /**
   * @deprecated
   * @param realpath
   * @return environment
   */
  public static Environment getEnvWithoutDb(String realpath) {
    if (env == null) {
      try {
        env = new Environment(false, realpath);
      } catch (Exception e) {
        //System.out.println(e.getMessage());
        return null;
      }
    }
    return env;
  }

  /**
   * @param key
   * @param defaultValue
   * @return property
   */
  public String getProperty(String key, String defaultValue) {
    return prop.getProperty(key, defaultValue);
  }

  /**
   * @param key
   * @return property
   */
  public String getProperty(String key) {
    return prop.getProperty(key);
  }
}