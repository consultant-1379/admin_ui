/**
 * 
 */
package com.distocraft.dc5000.etl.gui.ebsupgrade;

import java.util.List;

import com.distocraft.dc5000.etl.engine.system.SetStatusTO;

/**
 * @author epetrmi
 *
 */
public interface IEbsUpgradeManager {
  
  
  /**
   * Checks if there are new upgrades available
   * 
   * @return true/false
   */
  boolean isNewUpgradeAvailable();


  /**
   * Executes Ebs upgrade and return listenerId which can
   * be used to get status information
   * 
   * @return statusCode
   */
//  String executeUpgrade() throws Exception;
  
  /**
   * Gets status information with listenerId.
   * 
   * @param beginIndex - index where the eventlist starts
   * @param count - number of StatusEvents wanted. 
   * @return - SetStatusTO
   */
//  SetStatusTO getStatus(int beginIndex, int count) throws Exception;
  

  /**
   * Checks if there are new upgrades available
   * 
   * @return true/false
   */
  List<Upgrade> getUpgrades();


  /**
   * Executes Ebs upgrade and return listenerId which can
   * be used to get status information
   * 
   * @return statusCode
   */
  SetStatusTO executeUpgrade(String id) throws Exception;
  
  /**
   * Gets status information with listenerId.
   * 
   * @param beginIndex - index where the eventlist starts
   * @param count - number of StatusEvents wanted. 
   * @return - SetStatusTO
   */
  SetStatusTO getStatus(String id, int beginIndex, int count) throws Exception;
  
  
  
}
