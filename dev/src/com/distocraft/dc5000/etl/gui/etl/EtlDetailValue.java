package com.distocraft.dc5000.etl.gui.etl;
/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Class is pojo for restoring data for user. 
 * @author Jani Vesterinen
 */
public class EtlDetailValue {
  private int index = 0;
  private String value = "";
  
  /**
   * @return value
   */
  public String getValue() {
    return value;
  }
  
  /**
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
  }
  
  /**
   * @return index
   */
  public int getIndex() {
    return index;
  }
  
  /**
   * @param index
   */
  public void setIndex(int index) {
    this.index = index;
  }
  
  /**
   * @return tabcolor
   */
  public String getTabColor() {
    return index % 2 == 0 ? "#AAAAAA" : "#FFFFFF";
  }

}
