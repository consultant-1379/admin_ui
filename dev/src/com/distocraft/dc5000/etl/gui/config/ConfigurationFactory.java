/**
 * 
 */
package com.distocraft.dc5000.etl.gui.config;



/**
 * @author eheijun
 *
 */
public class ConfigurationFactory {

  private static Configuration _instance;
  
  /**
   * Hide constructor
   */
  private ConfigurationFactory() {
    super();
  }
  
  public static Configuration getConfiguration() {
    if (_instance == null) {
      _instance = new DefaultConfiguration(); 
    }
    return _instance;
  }
  
  /**
   * Set alternative Configuration (for junit tests)
   * @param instance
   */
  public static void setConfiguration(final Configuration instance) {
    _instance = instance;
  }
}
