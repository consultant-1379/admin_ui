/**
 * 
 */
package com.distocraft.dc5000.etl.gui.common;


/**
 * @author eheijun
 *
 */
public class EnvironmentNone implements Environment {

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#getType()
   */
  @Override
  public Type getType() {
    return Type.NONE;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#getDwhName()
   */
  @Override
  public String getDwhName() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showLoadings()
   */
  @Override
  public Boolean showLoadings() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showAggregations()
   */
  @Override
  public Boolean showAggregations() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showOldReAggregations()
   */
  @Override
  public Boolean showOldReAggregations() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showNewReAggregations()
   */
  @Override
  public Boolean showNewReAggregations() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showDatasourceLog()
   */
  @Override
  public Boolean showDatasourceLog() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showBusyhourInfo()
   */
  @Override
  public Boolean showBusyhourInfo() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showMonitoringRules()
   */
  @Override
  public Boolean showMonitoringRules() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showUnmatchedTopology()
   */
  @Override
  public Boolean showUnmatchedTopology() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showAdminConfiguration()
   */
  @Override
  public Boolean showAdminConfiguration() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showEBSUpgrader()
   */
  @Override
  public Boolean showEBSUpgrader() {
    return null;
  }

 

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showUserAdministration()
   */
  @Override
  public Boolean showUserAdministration() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showManual()
   */
  @Override
  public Boolean showManual() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showFMAlarm()
   */
  @Override
public Boolean showFMAlarm() {
	return null;
}
  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.common.Environment#showEniqMonitoring()
   */
  @Override
  public Boolean showEniqMonitoring(){
	return null;  
  } 

@Override
public Boolean showRAT() {
	return null;
}

@Override
public Boolean showNAT() {
	return null;
}
@Override
public Boolean showFLS() {
	return null;
}
}
