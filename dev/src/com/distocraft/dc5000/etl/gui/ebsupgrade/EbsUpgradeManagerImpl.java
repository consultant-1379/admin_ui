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
public class EbsUpgradeManagerImpl implements IEbsUpgradeManager {

  private Logger log = Logger.getLogger(this.getClass());

  //##NOT NEEDED In new implementation
  private Thread t;
  private UpgradeExecuterRunnable ue;

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
  public final static int STATUS_MANAGER_NOT_INITIALIZED = 0;
  public final static int STATUS_MANAGER_INITIALIZED = 1;

  // Status
  // private int setStatus = 0;
  public final static int STATUS_SET_NOT_STARTED = 0;
  public final static int STATUS_SET_STARTED = 1;
  public final static int STATUS_SET_RUNNING = 2;
  public final static int STATUS_SET_FINISHED = 3;
  public final static int STATUS_SET_FAILED_TO_RUN = 4;

  // Use EbsUpgradeManagerFactory to get object instead of calling this
  public EbsUpgradeManagerImpl(Properties props) {
    initialize(props);
  }

  /**
   * Run the initialization routines
   * 
   * @param props - some properties
   * (etl_engine_host,etl_engine_port,etl_engine_service,dirName)
   * @throws IllegalStateException
   */
  public void initialize(Properties props) throws IllegalStateException {
    // Check pre-conditions
    if (managerStatus == STATUS_MANAGER_NOT_INITIALIZED) {
      parseInitParams(props);
      createRMI();
    } else {
      throw new IllegalStateException(
          "Manager must be in NOT_INITIALIZED state");
    }// ...if (pre-conditions)
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
    this.engineHost = getPropertyOrDefault(props, "etl_engine_host",
        "localhost");
    
    this.enginePort = getPropertyOrDefault(props, "etl_engine_port", "1200");
    
    this.engineServiceName = getPropertyOrDefault(props, "etl_engine_service",
        "TransferEngine");

    String dirTmp = getPropertyOrDefault(props, "ebsDirs",
        "c:/local/ebs-upgrade-temp");
    
    String[] dirs = dirTmp.split(",");
    for(int i = 0 ; i < dirs.length ; i++){
    	
    	String[] splits = dirs[i].split("=");
    	dirNameList.add(splits[1]);
    }
    
    this.collectionSetName = getPropertyOrDefault(props, "collectionSetName",
        "test");
    this.setName = getPropertyOrDefault(props, "setName", "test");
  }

  /**
   * Creates RMI connection by using instance parameters that where set during
   * initialization.
   */
  private void createRMI() {
    try {
      //String rmiconnect = "rmi://" + this.engineHost + ":" + this.enginePort
          //+ "/" + this.engineServiceName;
     // log.debug("Starting to create RMI-connection (" + rmiconnect + ")...");
      // create RMI object
      long startTime = System.currentTimeMillis();
      trRMI = (ITransferEngineRMI) Naming.lookup(RmiUrlFactory.getInstance().getEngineRmiUrl());
      log.debug("Finished creating RMI-connection (it took "
          + (System.currentTimeMillis() - startTime) + " millis)");

    } catch (MalformedURLException murle) {
      log.error("MalformedURLException error:", murle);
    } catch (RemoteException re) {
      log.error("RemoteException error:", re);
    } catch (NotBoundException nbe) {
      log.error("NotBoundException error:", nbe);
    } catch (Exception e) {
      log.error("Exception caught: ", e);
    }

  }

  /**
   * Executes upgrade
   * 
   * pre-conditions: upgrade cannot be running (ue.status!=STATUS_SET_RUNNING)
   * 
   * @return String - "success" or "failure"
   */
  // ##TODO## Check the return type and values
  @Deprecated
  public String executeUpgrade() {
    // Check pre-conditions
    if (t == null || ue == null
        || (ue != null && ue.getStatus() != STATUS_SET_RUNNING)) {
      executeSetInThread(collectionSetName, setName);
      return "success";
    }
    return "failure";
  }

  /**
   * Tells the status of upgrade
   * 
   */
  @Deprecated
  public String getUpgradeStatus() {
    int status = STATUS_SET_NOT_STARTED;
    if (t != null) {
      if (t.isAlive() == true) {
        log.debug("Ebs-thread is still alive");
      }
      status = ue.getStatus();
    }
    return String.valueOf(status);
  }

  /**
   * Checks if an new upgrade exists
   */
  public boolean isNewUpgradeAvailable() {

	  for (int i = 0 ; i < dirNameList.size() ; i++){
		  if (filesExistInDirectory(dirNameList.get(i))){
			  return true;
		  }
	  }
	  
    return false;
  }

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
   * Starts selected set in thread. This is now used because Engine does not
   * currently offer an interface for retrieving set status information.
   * Instead, there is a method that allows you to run a set and return not
   * until the execution is complete (success,failed). This Engine- method is
   * now run in a thread so that we can keep at least some kind of status info,
   * while giving other objects such as GUI chance to poll status info.
   * 
   * @param collectionSetName
   *          - collection set name
   * @param collectionName
   *          - collection name
   * @return isStarted - true, if thread started without problems. Otherwise
   *         false.
   */
  @Deprecated
  private boolean executeSetInThread(String collectionSetName,
      String collectionName) {
    boolean started = false;
    ue = new UpgradeExecuterRunnable(trRMI, collectionSetName, collectionName);
    t = new Thread(ue);
    t.start();
    started = true;
    return started;
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

  public String executeWithListener() {
    return startSetWithSetListener(this.collectionSetName, this.setName);
     
  }

  public SetStatusTO getExecuteStatusEventsWithListenerId(String listenerId,
      int beginIndex, int count) {
    log.debug("Starting to get events with listenerId="+listenerId);
    SetStatusTO setStatusTO = null;
    try {
      setStatusTO = trRMI.getStatusEventsWithId(listenerId, beginIndex, count);
    } catch (RemoteException e) {
      log.error("Failed to get status information with params : "
          + "listenerId=" + listenerId + "beginIndex=" + beginIndex + "count="
          + count, e);
    }
    
    return setStatusTO;
  }
  
  
  private String startSetWithSetListener(String collectionSetName,
      String collectionName) {

    String listenerId = "-1";
    try {
    	
    	// change engine profile to No loads
    	log.info("Changing engine status to No loads...");
    	trRMI.setAndWaitActiveExecutionProfile("NoLoads");
    	log.info("Engine status changed to No loads.");
    	
      log
          .info("Starting now set " + collectionSetName + " : "
              + collectionName);
      // try to start the set
      listenerId = trRMI.executeWithSetListener(collectionSetName,
          collectionName, "");
    } catch (RemoteException e) {
      log.error("Failed to startSetWithSetListener with params : "
          + "collectionSetName=" + collectionSetName + "collectionName="
          + collectionName);

    }
    return listenerId;
  }

  public SetStatusTO getStatus(int beginIndex, int count)
      throws Exception {
    // TODO Auto-generated method stub
    return null;
  }



  public List<Upgrade> getUpgrades() {
    // TODO Auto-generated method stub
    return null;
  }

  public SetStatusTO getStatus(String id, int beginIndex, int count)
      throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public SetStatusTO executeUpgrade(String id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


}
