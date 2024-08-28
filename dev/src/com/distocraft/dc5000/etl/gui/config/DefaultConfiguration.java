/**
 * 
 */
package com.distocraft.dc5000.etl.gui.config;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.distocraft.dc5000.etl.gui.util.Helper;
import com.ericsson.eniq.repository.ETLCServerProperties;



/**
 * @author eheijun
 *
 */
public class DefaultConfiguration implements Configuration {

  private static final String CONF_DIR = "CONF_DIR";
  
  private static final String ENIQ_SW_CONF = "/eniq/sw/conf";

  static final String HOSTS_FILE = "eniqhosts";

  private static final String ETLC_SERVER_PROPERTIES = "ETLCServer.properties";

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Configuration#getServiceNames()
   */
  @Override
  public ServiceNames getServiceNames() {
    String serviceNamesFile = System.getProperty(CONF_DIR, ENIQ_SW_CONF);
    if (!serviceNamesFile.endsWith(File.separator)) {
      serviceNamesFile += File.separator;
    }
    serviceNamesFile += Helper.getEnvEntryString(HOSTS_FILE);
    final ServiceNames serviceNames = new ServiceNames(serviceNamesFile); 
    return serviceNames;
  }

  /*
   * (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.config.Configuration#getETLCServerProperties()
   */
  @Override
  public Properties getETLCServerProperties() throws IOException {
    String etlcServerPropertyFile = System.getProperty(CONF_DIR, ENIQ_SW_CONF);
    if (!etlcServerPropertyFile.endsWith(File.separator)) {
      etlcServerPropertyFile += File.separator;
    }
    etlcServerPropertyFile += ETLC_SERVER_PROPERTIES;
    Properties etlcServerProperties = new ETLCServerProperties(etlcServerPropertyFile);
    return etlcServerProperties;
  }

}
