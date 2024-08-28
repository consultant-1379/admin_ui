/**
 * This class runs the Ebs Upgrade in Thread and waits until it is finished
 */
package com.distocraft.dc5000.etl.gui.ebsupgrade;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;

/**
 * @author epetrmi
 * 
 */

public class UpgradeExecuterRunnable implements Runnable {
  private Logger log = Logger.getLogger(this.getClass());
  private ITransferEngineRMI trRMI;
  private String collectionSetName;
  private String collectionName;
  private int status = STATUS_SET_NOT_STARTED;

  public final static int STATUS_SET_NOT_STARTED = 0;
  public final static int STATUS_SET_RUNNING = 2;
  public final static int STATUS_SET_FINISHED_SUCCESFULLY = 3;
  public final static int STATUS_SET_FAILED_TO_RUN = 4;

  // Constructor
  public UpgradeExecuterRunnable(ITransferEngineRMI transferEngineRMI,
      String colSetName, String colName) {
    trRMI = transferEngineRMI;
    collectionSetName = colSetName;
    collectionName = colName;
  }

  /* Run
   * Set's this object state to STATUS_SET_RUNNING and
   * executes a given set via RMI. If execution is success, this.status
   * is set to STATUS_SET_FINISHED_SUCCESSFULLY. If execution fails, 
   * this.status is set to STATUS_SET_FAILED_TO RUN.
   */
  public void run() {
    status = STATUS_SET_RUNNING;
    try {
      log.debug("RUN STARTING status=" + status);
      // This method executes the set and waits returns
      // when the execution is finished
      trRMI.executeAndWait(collectionSetName, collectionName, "");
      status = STATUS_SET_FINISHED_SUCCESFULLY;
      log.debug("RUN FINISHED status=" + status);
    } catch (RemoteException e) {
      status = STATUS_SET_FAILED_TO_RUN;
      log.error("RUN FAILED status=" + status);
      e.printStackTrace();
    }
  }

  /**
   * @return status of executing the given set
   */
  public int getStatus() {
    return status;
  }

}
