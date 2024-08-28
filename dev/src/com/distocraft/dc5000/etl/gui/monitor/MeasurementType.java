package com.distocraft.dc5000.etl.gui.monitor;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Value class for measurement types.
 * 
 * @author Raatikainen
 * 
 */
public class MeasurementType {
  String date;

  String timelevel;

  String source;

  String typename;

  int rowcount;

  int datatimes;

  int rowsum;

  /**
   * @param date
   * @param timelevel
   * @param source
   * @param typename
   * @param rowcount
   * @param datatimes
   * @param rowsum
   */
  public MeasurementType(String date, String timelevel, String source,
      String typename, int rowcount, int datatimes, int rowsum) {
    this.date = date;
    this.timelevel = timelevel;
    this.source = source;
    this.typename = typename;
    this.rowcount = rowcount;
    this.datatimes = datatimes;
    this.rowsum = rowsum;
  }

  /**
   * @return date
   */
  public String getDate() {
    return date;
  }

  /**
   * 
   * @return timelevel
   */
  public String getTimelevel() {
    return timelevel;
  }

  /**
   * @return source
   */
  public String getSource() {
    return source;
  }

  /**
   * @return typename
   */
  public String getTypename() {
    return typename;
  }

  /**
   * @return rowcount
   */
  public int getRowcount() {
    return rowcount;
  }

  /**
   * @return rowcount
   */
  public int getDatatimes() {
    return datatimes;
  }

  /**
   * @return rowcount
   */
  public int getRowsum() {
    return rowsum;
  }
}