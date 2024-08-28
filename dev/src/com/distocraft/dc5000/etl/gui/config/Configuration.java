/**
 * 
 */
package com.distocraft.dc5000.etl.gui.config;

import java.io.IOException;
import java.util.Properties;


/**
 * @author eheijun
 *
 */
public interface Configuration {
  
  /*
   * Getter for property file ETLCServer.properties 
   */
  Properties getETLCServerProperties() throws IOException;
  
  /*
   * Getter for configuration file service_names
   */
  ServiceNames getServiceNames();

}
