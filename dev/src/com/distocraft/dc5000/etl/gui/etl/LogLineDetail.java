package com.distocraft.dc5000.etl.gui.etl;

import java.util.Date;

/**
 * Copyright &copy; Ericsson All rights reserved.<br> 
 * @author Jani Vesterinen
 * @author Tuomas Lemminkainen
 */
public class LogLineDetail {
  
  private Date logTime;
  private String logLevel = "";
  private String techPack = "";
  private String setType = "";
  private String setName = "";
  private String typeDetail = "";
  private String msg = "";
  
  public Date getLogTime() {
    return logTime;
  }
  
  public void setLogTime(Date logTime) {
    this.logTime = logTime;
  }
  
  public String getLogLevel() {
    return logLevel;
  }
  
  public void setLogLevel(String logLevel) {
    this.logLevel = logLevel;
  }
 
  public String getMessage() {
    return msg;
  }
  
  public void setMessage(String msg) {
    this.msg = msg;
  }
  
  public String getTypeDetails() {
    return typeDetail;
  }
  
  public void setTypeDetails(String typeDetail) {
    this.typeDetail = typeDetail;
  }
  
  public String getTechPack() {
    return techPack;
  }
  
  public void setTechPack(String techPack) {
    this.techPack = techPack;
  }
  
  public String getSetType() {
    return setType;
  }

  public void setSetType(String setType) {
    this.setType = setType;
  }
  
  public String getSetName() {
    return setName;
  }
  
  public void setSetName(String setName) {
    this.setName = setName;
  }
  
}
