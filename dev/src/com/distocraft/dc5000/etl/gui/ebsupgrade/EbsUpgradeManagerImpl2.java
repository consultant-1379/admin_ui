/**
 * Implementation of IEbsUpgradeManager-interface.
 * This class can start a set in engine and keep basic
 * status info.
 */
package com.distocraft.dc5000.etl.gui.ebsupgrade;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.engine.system.SetStatusTO;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;

/**
 * @author epetrmi
 * 
 */
public class EbsUpgradeManagerImpl2 implements IEbsUpgradeManager {

  private Logger log = Logger.getLogger(this.getClass());

  private Properties props;
  
//  private List<Upgrade> upgrades;
//  private String lastListenerid = "notExecutedYet";

  // RMI connection params
  private String engineHost;
  private String enginePort;
  private String engineServiceName;
  private ITransferEngineRMI trRMI;

  // Other properties
  private Vector<String> dirNameList = new Vector<String>();
  private String collectionSetName = "test";
  private String setName = "test";

  // Manager state
  private int managerStatus = 0;
  public final static int STATUS_MANAGER_AVAILABLE = 2;
  public final static int STATUS_MANAGER_NOT_AVAILABLE = 3;

  //Search these values from properties
  private static final String EBS_DIRECTORIES = "ebsDirs";
  private static final String ETL_ENGINE_HOST = "etl_engine_host";
  private static final String ETL_ENGINE_SERVICE = "etl_engine_service";
  private static final String ETL_ENGINE_PORT = "etl_engine_port";
  private static final String EBSUPGRADEFILES_DIR = "ebsupgradefiles.dir";
  private static final String COLLECTION_SET_NAME = "collectionSetName";
  private static final String SET_NAME = "setName";

  // Use EbsUpgradeManagerFactory to get object instead of calling this
  public EbsUpgradeManagerImpl2(Properties props) {
    this.props=props;
    initialize(props);
  }

  /**
   * Run the initialization routines
   * 
   * @param props
   *          - some properties
   *          (etl_engine_host,etl_engine_port,etl_engine_service,dirName)
   */
  public void initialize(Properties props) {
    // Check pre-conditions
    parseInitParams(props);
    if (createRMI()) {
      setManagerStatus(STATUS_MANAGER_AVAILABLE);
    } else {
      log.error("EbsUpgrademanager RMI init failed");
      setManagerStatus(STATUS_MANAGER_NOT_AVAILABLE);
    }
  }

  /**
   * Checks if an new upgrade exists
   */
  public boolean isNewUpgradeAvailable() {

    for (int i = 0; i < dirNameList.size(); i++) {
      if (filesExistInDirectory(dirNameList.get(i))) {
        return true;
      }
    }
    return false;
  }

//  public SetStatusTO getStatus(int beginIndex, int count) throws Exception {
//    return getSetStatusTOFromServer(this.lastListenerid, beginIndex, count);
//  }

  /**
   * Checks if there are files in given directory path
   * 
   * @param dirPath
   * @return True, if one or more files exist. False, if there aren't any files
   *         given dirPath is not referencing to directory or something goes
   *         wrong while reading.
   */
  private boolean filesExistInDirectory(String dirPath) {
    boolean ret = false;
    try {
      File dir = new File(dirPath);
      if (dir != null && dir.exists() && dir.isDirectory()) {
        File[] files = dir.listFiles();

        // Check if files exist in given directory
        if (files != null && files.length > 0) {
          ret = true;
          log.info("New ebs upgrade exist.");
        }
      } else {
        log.error("Could not search ebs upgrade files from " + dirPath
            + "-named directory!");
      }
    } catch (Exception e) {
      log.error("Could not search ebs upgrade files from " + dirPath
          + " -named directory!");
    }
    return ret;

  }

  /**
   * Set given properties to this
   * 
   * @param props
   */
  private void parseInitParams(Properties props) {
    if (props == null) {
      throw new IllegalArgumentException();
    }

    // Read RMI params
    this.engineHost = getPropertyOrDefault(props, ETL_ENGINE_HOST,
        "localhost");
    this.enginePort = getPropertyOrDefault(props, ETL_ENGINE_PORT, "1200");
    this.engineServiceName = getPropertyOrDefault(props, ETL_ENGINE_SERVICE,
        "TransferEngine");

    String dirTmp = getPropertyOrDefault(props, EBSUPGRADEFILES_DIR,
        "c:/local/ebs-upgrade-temp");

    String[] dirs = dirTmp.split(",");
    for (int i = 0; i < dirs.length; i++) {
      dirNameList.add(dirs[i]);
    }

    this.collectionSetName = getPropertyOrDefault(props, COLLECTION_SET_NAME,
        "test");
    this.setName = getPropertyOrDefault(props, SET_NAME, "test");
  }

  /**
   * Creates RMI connection by using instance parameters that where set during
   * initialization.
   */
  private boolean createRMI() {
    boolean ret = false;
    String rmiconnect = "";
    try {
//      rmiconnect = "rmi://" + this.engineHost + ":" + this.enginePort
//          + "/" + this.engineServiceName;
//      log.debug("Starting to create RMI-connection (" + rmiconnect + ")...");
      // create RMI object
      long startTime = System.currentTimeMillis();
      trRMI = (ITransferEngineRMI) Naming.lookup(RmiUrlFactory.getInstance().getEngineRmiUrl());
      log.debug("Finished creating RMI-connection (it took "
          + (System.currentTimeMillis() - startTime) + " millis)");
      ret = true;
    } catch (MalformedURLException murle) {
      log.error("Failed to use URL="+rmiconnect, murle);
    } catch (RemoteException re) {
      log.error("Failed to create RMI with URL="+rmiconnect, re);
    } catch (NotBoundException nbe) {
      log.error("NotBoundException error:", nbe);
    } catch (Exception e) {
      log.error("Failed to create RMI", e);
    }
    return ret;
  }

  
  private ITransferEngineRMI getRMI(){
    if(this.trRMI==null){
      if(createRMI()){
        log.info("RMI connection re-created succcesfully");
      }else{
        log.info("RMI connection re-creation failed");
      }
      
    }
    return trRMI;
  }
  
  /**
   * Gets value from properties object. If not found, return defaultValue
   * 
   * @param props
   * @param key
   * @param defaultValue
   * @return value or defaultValue
   */
  String getPropertyOrDefault(Properties props, String key, String defaultValue) {
    String value = defaultValue;
    ;
    try {
      value = props.getProperty(key);
      if (value == null || "".equals(value)) {
        value = defaultValue;
      }
    } catch (Exception e) {
      log.error("Failed to get property (props=" + props + ", key=" + key
          + "). Using default=" + defaultValue);
    }
    log.debug("Property read (props=" + props + ", key=" + key + ", value="
        + value + ")");
    return value;
  }


//  /**
//   * Executes upgrade
//   * 
//   * Status: alreadyrunning started failed
//   * 
//   * @return - status
//   */
//  public String executeUpgrade() {
//    String ret = executeWithParams(this.collectionSetName,this.setName);
//    return ret;
//  }
//
//  /**
//   * @return
//   */
//  private String executeWithParams(String collectionSetName, String setName) {
//    String ret = "alreadyrunning";
//    String status = getStatusFromServer(this.lastListenerid);
//    if ("".equals(status)) {
//      // Is running
//    } else {
//      // Is not running
//      try {
//        // change engine profile to No loads
//        log.info("Changing engine status to No loads...");
//        trRMI.setAndWaitActiveExecutionProfile("Normal");
//        log.info("Engine status changed to No loads.");
//        
//        log.debug("Executing with params: colSetName="+collectionSetName+", setName="+setName);
//        this.lastListenerid = trRMI.executeWithSetListener(
//            collectionSetName, setName, "");
//        ret = "started";
//      } catch (RemoteException e) {
//        ret = "failed";
//        log.error("Failed to execute set with listener. " +
//        		"ColSetName="+collectionSetName+
//        		", setName="+setName+" and lastParam=emptystring", e);
//      }
//    }
//    return ret;
//  }
//
//  private String getStatusFromServer(String listenerId) {
//    String ret = "no_status";
//    if (listenerId != null && !"".equals(listenerId)
//        && !"notExecutedYet".equals(listenerId)) {
//      SetStatusTO lastStatusTO = null;
//      try {
//        lastStatusTO = trRMI.getStatusEventsWithId(listenerId, 0, 0);
//      } catch (RemoteException e) {
//        log.error("Failed to get statusEventsWithId="+listenerId, e);
//      }
//      if (lastStatusTO != null) {
//        ret = lastStatusTO.getSetStatus();
//      }
//    }
//    return ret;
//  }

//  private SetStatusTO getSetStatusTOFromServer(String listenerId,
//      int beginIndex, int count) {
//    SetStatusTO ret = null;
//    log.debug("Starting to get events with listenerId=" + listenerId);
//    try {
//      if (listenerId != null && !"".equals(listenerId)
//          && !"notExecutedYet".equals(listenerId)) {
//        ret = trRMI.getStatusEventsWithId(listenerId, beginIndex, count);
//      }
//    } catch (RemoteException e) {
//      log.error("Failed to get status information with params : "
//          + "listenerId=" + listenerId + "beginIndex=" + beginIndex + "count="
//          + count, e);
//    }
//    return ret;
//  }


  /**
   * @param managerStatus
   *          the managerStatus to set
   */
  public void setManagerStatus(int managerStatus) {
    this.managerStatus = managerStatus;
  }

  /**
   * @return the managerStatus
   */
  public int getManagerStatus() {
    return managerStatus;
  }

//  public String executeUpgrade(String id) throws Exception {
//    String ret = executeWithParams(id, this.setName);
//    return ret;
//  }

  
  public List<Upgrade> getUpgrades() {
    return getUpgrades(this.props);
  }

  private List<Upgrade> getUpgrades(Properties props3) {
    if(props3!=null){
    log.debug("Using props:"+props3.toString());
    if(props3!=null){
      String valuePairList = props3.getProperty(EBS_DIRECTORIES, "");
      if(valuePairList!=null && !"".equals(valuePairList))
      {
        //Parse dirs
        return parseUpgrades(valuePairList);
      }
    }
    }
    return new ArrayList<Upgrade>(0);
  }

  /**
   * Creates Upgrade-objects by using valuePairList as parameter.
   * (ex."ebsDirs=INTF_PM_E_EBSW=/local/ebs-upgrade-temp/test/,PM_E_EBSW=/local/ebs-upgrade-temp/1/" 
   * is parsed to 2 Upgrade-objects which id:s are INTF_PM_E_EBSW and PM_E_EBSW. Method also checks 
   * if there these upgrades are available and sets objects available-status true or false.
   * 
   * @param valuePairList
   */
   List<Upgrade> parseUpgrades(String valuePairList) {
    log.debug("valuePairList="+valuePairList);
     List<Upgrade> upgrades = new Vector<Upgrade>();
    String[] valuePairs = valuePairList.split(",");
    for (int i = 0; i < valuePairs.length; i++) {
      String[] valuePair = valuePairs[i].split("=");
      boolean isAvailable = false;
      if(filesExistInDirectory(valuePair[1])){
        isAvailable = true;
      }
      Upgrade u = new Upgrade(valuePair[0], isAvailable, false);
      upgrades.add(u);
      log.debug("added upgrade: "+u);
    }
    return upgrades;
  }

   public SetStatusTO executeUpgrade(String id) throws Exception{
     SetStatusTO status = null;
     try {
       log.debug("Executing with params: colSetName="+id+", setName="+setName);
       if(props==null){
         props = new Properties();
       }
       status = getRMI().executeSetViaSetManager(id, setName, "", props );
     } catch (RemoteException e) {
       log.error("Failed to execute set with listener. " +
           "ColSetName="+id+
           ", setName="+setName+" and lastParam=emptystring", e);
       trRMI = null;//Forces next time to recreate the connection
     }
     return status;
   }

  public SetStatusTO getStatus(String id, int beginIndex, int count) throws Exception {
    SetStatusTO status = null;
    try {
      log.debug("Executing with params: colSetName="+id+", setName="+setName);
      status = getRMI().getSetStatusViaSetManager(id, setName, beginIndex, count);
    } catch (RemoteException e) {
      log.error("Failed to execute set with listener. " +
          "ColSetName="+id+
          ", setName="+setName+" and lastParam=emptystring", e);
      trRMI = null;//Forces next time to recreate the connection
    }
    return status;
  }
   
   
}
