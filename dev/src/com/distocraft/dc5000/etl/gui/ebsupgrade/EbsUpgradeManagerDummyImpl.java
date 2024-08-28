/**
 * 
 */
package com.distocraft.dc5000.etl.gui.ebsupgrade;

import java.util.List;
import java.util.Vector;

import com.distocraft.dc5000.etl.engine.system.SetStatusTO;
import com.distocraft.dc5000.etl.engine.system.StatusEvent;

/**
 * @author epetrmi
 *
 */
public class EbsUpgradeManagerDummyImpl implements IEbsUpgradeManager {

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.ebsupgrade.IEbsUpgradeManager#executeUpgrade()
   */
  public String executeUpgrade() {
    // TODO Auto-generated method stub
    return "";
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.ebsupgrade.IEbsUpgradeManager#executeWithListener()
   */
  public String executeWithListener() {
    return String.valueOf(123);
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.ebsupgrade.IEbsUpgradeManager#getExecuteStatusEventsWithListenerId(java.lang.String, int, int)
   */
  public SetStatusTO getExecuteStatusEventsWithListenerId(String listenerId,
      int beginIndex, int count) {
    
    List<StatusEvent> listOfEvents = new Vector<StatusEvent>(2);
    listOfEvents.add(new StatusEvent("dummyevent1"));
    listOfEvents.add(new StatusEvent("dummyevent2"));
    SetStatusTO ret = new SetStatusTO("1", listOfEvents);
    return ret;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.ebsupgrade.IEbsUpgradeManager#getUpgradeStatus()
   */
  public String getUpgradeStatus() {
    return String.valueOf(0);
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.ebsupgrade.IEbsUpgradeManager#isNewUpgradeAvailable()
   */
  public boolean isNewUpgradeAvailable() {
    return true;
  }

  public SetStatusTO getStatus(int beginIndex, int count)
      throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public SetStatusTO executeUpgrade(String id) throws Exception {
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

}
