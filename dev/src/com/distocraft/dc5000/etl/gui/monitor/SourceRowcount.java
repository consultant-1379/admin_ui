package com.distocraft.dc5000.etl.gui.monitor;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Value class for source row count.
 * 
 * @author Jani Vesterinen
 *
 */
public class SourceRowcount {
  String source;

  int rowcount;

  int datatimes;

  int rowsum;

  /**
   * @param source
   * @param rowcount
   * @param datatimes
   * @param rowsum
   */
  public SourceRowcount(String source, int rowcount, int datatimes, int rowsum) {
    this.source = source;
    this.rowcount = rowcount;
    this.datatimes = datatimes;
    this.rowsum = rowsum;
  }

  /**
   * @return source
   */
  public String getSource() {
    return source;
  }

  /**
   * @return rowcount
   */
  public int getRowcount() {
    return rowcount;
  }
  
  /**
   * @return datatimes
   */
  public int getDatatimes() {
    return datatimes;
  }
  
  /**
   * @return rowsum
   */
  public int getRowsum() {
    return rowsum;
  }
}
