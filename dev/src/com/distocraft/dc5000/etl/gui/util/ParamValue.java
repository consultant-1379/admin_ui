package com.distocraft.dc5000.etl.gui.util;
/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Value class for restoring key value pairs.
 *  
 * @author Jani Vesterinen
 */
public class ParamValue {
  private String param = "";
  private String value = "";
  private boolean selected = false;
  private String tag = "";
  
  public String getParam(){
    return param;
  }
  
  public String getValue(){
    return value;
  }
  
  public void setParam(final String s){
    param = s;
  }
  
  public void setValue(final String s){
    value = s;
  }
  
  public void setSelected(final boolean b){
    selected = b;
  }
  
  public String isSelected(){
    return selected ? tag : "";
  }
  
  public void setSelectedTag(final String s){
    tag = s;
  }
  
}
