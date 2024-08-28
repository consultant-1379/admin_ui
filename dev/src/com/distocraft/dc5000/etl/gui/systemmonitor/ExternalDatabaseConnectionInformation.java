package com.distocraft.dc5000.etl.gui.systemmonitor;

public class ExternalDatabaseConnectionInformation {

  public static final String RED_BULB_IMAGE = "red_bulp.gif";

  public static final String YELLOW_BULB_IMAGE = "yellow_bulp.gif";

  public static final String GREEN_BULB_IMAGE = "green_bulp.gif";

  private String bulbImage = "RED_BULB_IMAGE";

  private String externalDatabaseName = "";
  
  private String externalDatabaseDescription = "";

  public String getBulbImage() {
    return bulbImage;
  }

  public void setBulbImage(String bulbImage) {
    this.bulbImage = bulbImage;
  }

  public String getExternalDatabaseName() {
    return externalDatabaseName;
  }

  public void setExternalDatabaseName(String externalDatabaseName) {
    this.externalDatabaseName = externalDatabaseName;
  }

  
  public String getExternalDatabaseDescription() {
    return externalDatabaseDescription;
  }

  
  public void setExternalDatabaseDescription(String externalDatabaseDescription) {
    this.externalDatabaseDescription = externalDatabaseDescription;
  }

}
