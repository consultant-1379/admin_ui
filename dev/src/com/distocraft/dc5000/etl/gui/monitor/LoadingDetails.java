package com.distocraft.dc5000.etl.gui.monitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Class is pojo for restoring data for user. 
 * @author Jani Vesterinen
 */
public class LoadingDetails {
  
  private String timelevel = "";
  private String meatype = "";
  private boolean problematic = false;
  private final List<String> al = new ArrayList<String>();
  private int rowCount = 0;
  private int duration = 24;
  private String status ="";

  public String getStatus() {
	return this.status;
  }
  
  public void setStatus(String status) {
	this.status = status;
  }
  /**
   * Set measurement type name
   * 
   * @param meatype
   */
  
  public void setMeatypeName(final String meatype) {
    this.meatype = meatype;
  }
  
  /**
   * @param value
   */
  public void setTimelevel (final String timelevel) {
    this.timelevel = timelevel;
  }
  
  /**
   * @param value
   */
  public void setRowCount (final int rowCount) {
    this.rowCount = rowCount;
  }
  
  /**
   * @param value
   */
  public int getRowCount () {
    return rowCount;
  }
  
  /**
   * @param value
   */
  
  public int getDuration () {
    return duration;
  }
  
  /**
   * @param value
   */
  public void setProblematic (final boolean problematic) {
    this.problematic = problematic;
  }
  
  /**
   * @param value
   */
  public void updateStatus (final String status) {
    final int sz = this.al.size();
    al.remove(sz-1);
    this.al.add(status);
    setStatus(status);
  }
  
  public void setDuration (final int duration) {
    this.duration = duration;
  }
  
  /**
   * @param value
   */
  public void addStatus (final String status) {
    this.al.add(status);
    setStatus(status);
  }
  
  /**
   * @return index
   */
  public String getMeatypeName() {
    return meatype;
  }
  
  /**
   * @return index
   */
  public String getTimelevel() {
    return timelevel;
  }
  
  /**
   * @return index
   */
  public boolean getProblematic() {
    return problematic;
  }
  
  /**
   * @return index
   */
  public List<String> getStatuses() {
    return al;
  }

}

