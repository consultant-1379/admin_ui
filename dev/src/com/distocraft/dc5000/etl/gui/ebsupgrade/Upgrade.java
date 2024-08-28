/**
 * 
 */
package com.distocraft.dc5000.etl.gui.ebsupgrade;

/**
 * @author epetrmi
 *
 */
public class Upgrade {
  private String id;
  private String status;
  private boolean available;
  private boolean running = false;
  
  public Upgrade(String id, boolean isAvailable, boolean isRunning){
    this.id = id;
    this.available = isAvailable;
    this.running = isRunning;
  }
  
  public String getId() {
    if(id==null){
      id = "";
    }
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getStatus() {
    if(status==null){
      status = "EMPTY";
    }
    return status;
  }
  
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * @param available the available to set
   */
  public void setAvailable(boolean available) {
    this.available = available;
  }

  /**
   * @return the available
   */
  public boolean isAvailable() {
    return available;
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }
  
  
  @Override
  public String toString() {
    return "Upgrade: id="+id+", status="+status+", available="+available;
  }
  
  
  
}
