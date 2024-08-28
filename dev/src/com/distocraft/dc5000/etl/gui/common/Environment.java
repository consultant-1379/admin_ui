/**
 * 
 */
package com.distocraft.dc5000.etl.gui.common;


/**
 * @author eheijun
 *
 */
public interface Environment {
  
  /**
   * Currently three different environment are supported:
   * ENIQ Statistics,
   * ENIQ Events &
   * Mixed 
   */
  enum Type {
    STATS,
    EVENTS,
    MIXED,
    NONE
  }
  
  /**
   * Returns the current environment type
   */
  Type getType();
  
  /**
   * Returns DWH database name in the current environment 
   * @return database name
   */
  String getDwhName();
  
  /**
   * True if environment has Show Loadings feature 
   */
  Boolean showLoadings();

  /**
   * True if environment has Show Aggregations feature 
   */
  Boolean showAggregations();

  /**
   * True if environment uses old Reaggregations feature 
   */
  Boolean showOldReAggregations();

  /**
   * True if environment uses new Reaggregations feature 
   */
  Boolean showNewReAggregations();

  /**
   * True if environment has Datasource Log feature 
   */
  Boolean showDatasourceLog();

  /**
   * True if environment has Busyhour feature 
   */
  Boolean showBusyhourInfo();

  /**
   * True if environment has Monitoring Rules feature 
   */
  Boolean showMonitoringRules();

  /**
   * True if environment has Unmatched topology feature 
   */
  Boolean showUnmatchedTopology();

  /**
   * True if environment has Admin configuration feature 
   */
  Boolean showAdminConfiguration();

  /**
   * True if environment has EBS feature 
   */
  Boolean showEBSUpgrader();

  

  /**
   * True if environment has User administration feature 
   */
  Boolean showUserAdministration();

  /**
   * True if environment has Online CPI documentation 
   */
  Boolean showManual();
  /**
   * True if environment uses FM Alarm feature 
   */
  Boolean showFMAlarm();
  /**
   * True if environment uses Eniq Monitoring Services 
   */
  Boolean showEniqMonitoring();
  
  
  /**
   * True only if UNASSIGNED or MASTER ENIQ-S
   */
  Boolean showRAT();
  
  /**
   * True only if MASTER ENIQ-S
   */
  Boolean showNAT();  
  
  /**
   * True only if User has to enter Date and Time
   */
  Boolean showFLS();  
}
