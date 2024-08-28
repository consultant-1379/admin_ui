package com.distocraft.dc5000.etl.gui.common;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * This class is used by the DataRowSummary -servlet.<br>
 * Class is value-class.
 * 
 * @author Mark Stenback
 * 
 */
public class MeasResultSet {

  // =============================================================== GLOBAL VARIABLES
  /** This array contains the line count for each searched day. */
  private final List<Integer> linesPerDay;

   /** The BASETABLENAME of given measurement type. */
  private String measBaseTableName;
  private final Log log = LogFactory.getLog(super.getClass());
  
  /** This contains the line count for each day with the measurement type **/
  private final HashMap<String , HashMap<String,ArrayList<Integer>> > linesMap;
  private final ArrayList<String> measList ;
  
  // =============================================================== CONSTRUCTOR
  /**
	 * 
	 *
	 */
  public MeasResultSet() {
    linesPerDay = new ArrayList<Integer>();
    linesMap = new HashMap<String , HashMap<String,ArrayList<Integer>> >();
    measList = new ArrayList<String>();
  }

  /**
   * 
   * @param size
   * @param measBaseTableName
   */
  public MeasResultSet(final int size, final String measBaseTableName) {
    linesPerDay = new  ArrayList<Integer>(size);
    linesMap = new HashMap<String , HashMap<String,ArrayList<Integer>> >(size);
    measList = new ArrayList<String>(size);
    this.measBaseTableName = measBaseTableName;
  }
  
  public MeasResultSet(final int size) {
	    linesPerDay = new  ArrayList<Integer>(size);
	    linesMap = new HashMap<String , HashMap<String,ArrayList<Integer>> >(size);
	    measList = new ArrayList<String>(size);
	  }

  // =============================================================== PUBLIC METHODS
  /**
   * This method adds a single int value into the ArrayList as an Integer -object.
   * 
   * @param value
   */
  public void addIntegerValue(final String key ,final Integer value) {
    final Integer count = new Integer(value);

   linesPerDay.add(count);
 
  }
   /**
   * This method returns an Integer value from the ArrayList's specified row.
   * 
   * @param row
   * @return
   */
  public Integer getIntegerValue(final int row) {
    return (Integer) linesPerDay.get(row);
  }

  public String getMeasBaseTableName() {
    return measBaseTableName;
  }

  /**
   * This method returns the ArrayList's size.
   * 
   * @return
   */
  public int searchSize() {
    return linesPerDay.size();
  }
 /**
  * This method adds all the measurement types with appropriate rowcount & ropcount values to the linesMap
  */
 public void addMapValue(String meastype , HashMap<String, ArrayList<Integer>> mapvalues) {
	linesMap.put(meastype, new HashMap<String,ArrayList<Integer>>(mapvalues) );
 }
/**
 *This method returns the size of the linesMap(tells the total number of measurement types) 
 */
 public int getSize() {
    return linesMap.size();
  }
/**
 * This method adds all the measurement types to a list
 */
 public void addMeasType(String Meas) {
	measList.add(Meas);
	
  }
 /**
  * Retrieve the measuretype based on the index value
  */
 public String getMeasType(int index) {
	return (String) measList.get(index);
  }
 /*
  * This method returns the RowCount value for a specified measurement type
  */
 public ArrayList<Integer> getMapValue(String measType) {
    ArrayList<Integer> rowCount = null ;
    HashMap<String,ArrayList<Integer>> rowMap = new HashMap<String,ArrayList<Integer>>();
    if(linesMap.containsKey(measType)) 
    	rowMap = linesMap.get(measType);
	for (int i = 0;i<rowMap.size();i++ ) {
	rowCount = rowMap.get("RowCountValue");	
	}
  return rowCount;
 }
 /*
  * This method returns the RopCount value for a specified measurement type
  */
 public ArrayList<Integer> getRopValue(String measType) {
	ArrayList<Integer> ropCount = null;	
    HashMap<String,ArrayList<Integer>> ropMap = new HashMap<String,ArrayList<Integer>>();
    if(linesMap.containsKey(measType)) 
    	ropMap = linesMap.get(measType);
	 for (int i = 0;i<ropMap.size();i++ ) {
		ropCount = ropMap.get("RopCountValue");
	}	
  return ropCount;
 }

}