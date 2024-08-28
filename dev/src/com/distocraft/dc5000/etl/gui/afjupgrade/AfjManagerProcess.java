/**
 * 
 */
package com.distocraft.dc5000.etl.gui.afjupgrade;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ericsson.eniq.afj.AFJManager;
import com.ericsson.eniq.afj.AfjManagerFactory;
import com.ericsson.eniq.afj.common.AFJDelta;
import com.ericsson.eniq.afj.common.AFJTechPack;
import com.ericsson.eniq.exception.AFJConfiguationException;
import com.ericsson.eniq.exception.AFJException;

/**
 * @author eheijun
 */
public class AfjManagerProcess implements Runnable {

  private final transient Log log = LogFactory.getLog(this.getClass());

  public static final String AFJ_MANAGER_PROCESS = "afjManagerProcess";

  public enum State {
    NONE, TECHPACK_ERROR, GENERATE_DELTA, UPGRADE_TECHPACK, GENERATE_READY, UPGRADE_READY, GENERATE_ERROR, UPGRADE_ERROR, GENERAL_ERROR
  };

  private State processState;

  private String statusMessage;

  private AFJManager afjManager;

  private List<AFJTechPack> afjTechPackList;

  private AFJTechPack selectedTechPack;

  private AFJDelta afjDelta;

  private Boolean running;

  private ExecutorService threadPool;

  /**
   * Constructor
   */
  public AfjManagerProcess(final ExecutorService threadPool) {
    try {
      // start debug only
      // AfjManagerFactory.setInstance(new MockAfjManager());
      // end debug only
      this.afjManager = AfjManagerFactory.getInstance();
      initializeStatus();
      this.selectedTechPack = null;
      this.afjDelta = null;
      this.threadPool = threadPool; 
      log.debug("AFJManager initialization done.");
    } catch (AFJException e) {
      this.processState = State.GENERAL_ERROR;
      this.statusMessage = e.getMessage();
      log.error("AFJManager initialization failed.", e);
    }
  }

  /**
   * Initialises process status variables
   */
  public final void initializeStatus() {
    this.processState = State.NONE;
    this.statusMessage = "";
    this.running = Boolean.valueOf(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    if (!isRunning()) {
      String task = "Task";
      running = Boolean.valueOf(true);
      try {
        switch (processState) {
        case GENERATE_DELTA:
          try {
            task = "Delta generation for the "  + selectedTechPack.getTechPackName();
            statusMessage = task + " running.";
            log.debug(statusMessage);
            afjDelta = afjManager.getAFJDelta(selectedTechPack);
            processState = State.GENERATE_READY;
            statusMessage = task + " successfully done.";
            log.debug(statusMessage);
          } catch (Exception e) {
            processState = State.GENERATE_ERROR;
            statusMessage = task + " failed:\n" + e.getMessage().trim();
            log.error("AFJManager execution failed.", e);
          }
          break;
        case UPGRADE_TECHPACK:
          try {
            task = "Tech Pack upgrade for the " + selectedTechPack.getTechPackName(); 
            statusMessage = task + " running.";
            log.debug(statusMessage);
            final String result = afjManager.upgradeAFJTechPack(afjDelta);
            afjDelta = null;
            processState = State.UPGRADE_READY;
            statusMessage = result;
            log.info(statusMessage);
          } catch (Exception e) {
            processState = State.UPGRADE_ERROR;
            statusMessage = task + " failed:\n" + e.getMessage().trim();
            log.error("AFJManager execution failed.", e);
          }
          break;
        }
      } finally {
        running = Boolean.valueOf(false);
      }
    }
  }

  /**
   * Read only property that returns the status of the running process.
   * 
   * @return true if this process has started running.
   */
  public Boolean isRunning() {
    return this.running;
  }

  /**
   * Read only property that returns current status of the AFJManager process as
   * string
   * 
   * @return current status as text
   */
  public String getStatusMessage() {
    String task = "";
    if (isRunning()) {
      switch (processState) {
      case GENERATE_DELTA:
        task = "delta generation";
        break;
      case UPGRADE_TECHPACK:
        task = "Tech Pack upgrade";
        break;
      }
      return "Node Version Update Manager running " + task + " for " + selectedTechPack.getTechPackName() + ". Please wait...";
    }
    
    switch (processState) {
    case GENERATE_READY:
    case UPGRADE_READY:
    case UPGRADE_ERROR:
    case TECHPACK_ERROR:      
    case GENERATE_ERROR:
    case GENERAL_ERROR:
      return statusMessage;
    default:
      return "";
    }
    
  }

  /**
   * Getter for selectedTechPack
   * 
   * @return the selectedTechPack
   */
  public AFJTechPack getSelectedTechPack() {
    return selectedTechPack;
  }

  /**
   * Setter for selectedTechPack
   * 
   * @param selectedTechPack
   *          the selectedTechPack to set
   */
  public void setSelectedTechPack(final String selectedTechPack) {
    final AFJTechPack tmp = this.getAFJTechPackByName(selectedTechPack);
    if (tmp != this.selectedTechPack) {
      this.selectedTechPack = tmp;
      this.afjDelta = null;
    }
  }

  /**
   * @return the processState
   */
  public State getProcessState() {
    return processState;
  }

  /**
   * Return list of all AFJ enabled techpacks
   * 
   * @return result list
   */
  public List<AFJTechPack> getAFJTechPackList() {
    if (!isRunning()) {
      try {
        afjTechPackList = afjManager.getAFJTechPacks();
      } catch (AFJConfiguationException e) {
        processState = State.TECHPACK_ERROR;        
        statusMessage = e.getMessage();
        log.warn("Getting TechPack list from AFJManager can not be done: "  + e.getMessage());
      } catch (AFJException e) {
        processState = State.GENERAL_ERROR;
        statusMessage = e.getMessage();
        log.error("Getting TechPack list from AFJManager failed.", e);
      }
    }
    return afjTechPackList;
  }

  /**
   * Returns results of delta generation for selected TP
   * 
   * @return the AFJDeltaList
   */
  public AFJDelta getAFJDelta() {
    if (!isRunning()) {
      return afjDelta;
    }
    return null;
  }

  /**
   * Returns AFJTechPack by given name
   * 
   * @param name
   *          Search parameter
   * @return Matching AFJTechPack or null if not found
   */
  public AFJTechPack getAFJTechPackByName(final String name) {
    if (afjTechPackList == null) {
      this.getAFJTechPackList();
    }
    for (AFJTechPack AFJTechPack : afjTechPackList) {
      if (AFJTechPack.getTechPackName().equals(name)) {
        return AFJTechPack;
      }
    }
    return null;
  }

  /**
   * Starts delta generation process for selected techpack in background
   */
  public void generateAFJTechPackDelta() {
    if (!isRunning()) {
      processState = State.GENERATE_DELTA;
      statusMessage = "";
      try {
        threadPool.execute(this);
        log.info("Delta generation for the AFJTechPack " + selectedTechPack.getTechPackName() + " started.");
        // wait until new thread is running, but max 3 seconds
        final Long start = System.currentTimeMillis();
        while (!this.isRunning()) {
          final Long now = System.currentTimeMillis();
          if ((now - start) > 3000) {
            break;
          }
        }
      } catch (Exception e) {
        processState = State.GENERATE_ERROR;
        statusMessage = e.getMessage();
        log.error("Getting TechPack list from AFJManager failed.", e);
      }
    }
  }

  /**
   * Starts upgrade process for selected techpack in background
   */
  public void upgradeAFJTechPackWithDelta() {
    if (!isRunning()) {
      processState = State.UPGRADE_TECHPACK;
      statusMessage = "";
      try {
        threadPool.execute(this);
        log.info("Upgrade for the AFJTechPack " + selectedTechPack.getTechPackName() + " started.");
        // wait until new thread is running, but max 3 seconds
        final Long start = System.currentTimeMillis();
        while (!this.isRunning()) {
          final Long now = System.currentTimeMillis();
          if ((now - start) > 3000) {
            break;
          }
        }
      } catch (Exception e) {
        processState = State.UPGRADE_ERROR;
        statusMessage = e.getMessage();
        log.error("Getting TechPack list from AFJManager failed.", e);
      }
    }
  }

  public boolean isErrorState() {
    return (processState == State.TECHPACK_ERROR || processState == State.GENERAL_ERROR || processState == State.GENERATE_ERROR || processState == State.UPGRADE_ERROR);
  }

}
