/**
 * Use this class to instantiate EbsUpgradeManager-class
 */
package com.distocraft.dc5000.etl.gui.ebsupgrade;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author epetrmi
 *
 */
abstract public class EbsUpgradeManagerFactory {

  private static Log log = LogFactory.getLog(com.distocraft.dc5000.etl.gui.ebsupgrade.EbsUpgradeManagerFactory.class);
  private static IEbsUpgradeManager manager;
  
  static IEbsUpgradeManager getInstance(){
    return getManager();
  }
  
  /**
   * Returns EbsUpgradeManagerImpl if can get properties and initialization is ok
   * Returns null if cannot get properties or initialization fails
   * 
   * @return IEbsUpgradeManager
   */
  static private IEbsUpgradeManager getManager(){
    if(manager==null){

      try{
      //      Properties props = PropertyLoader.getProperties();
      Properties props = EBSPlugin.getProperties();
      
      //EBSPlugin return null if the plugin is not enabled
      // so we can give a dummy implementation
      if(props==null){
        //DUMMY
        log.info("EbsUpgradeManagerFactory gives EbsUpgraderManagerDummyImpl");
//        manager = new EbsUpgradeManagerDummyImpl();
        manager = null;
      }else{
      //REAL

        log.debug("Loaded properties="+props.toString());
        log.info("EbsUpgradeManagerFactory creates EbsUpgraderManagerImpl2");
//        manager = new EbsUpgradeManagerImpl(props);
        
        //Use return manager only if its not null and it is available
        EbsUpgradeManagerImpl2 m = new EbsUpgradeManagerImpl2(props);
        if(m!=null && m.getManagerStatus()==EbsUpgradeManagerImpl2.STATUS_MANAGER_AVAILABLE){
          manager = m;
        }
      }
      }catch(Exception e){
        log.error("Failed to instantiate (and initialize) EbsUpgradeManager!"+e);
        manager = null;
      }
      
    }
    return manager;
  }
  
}
