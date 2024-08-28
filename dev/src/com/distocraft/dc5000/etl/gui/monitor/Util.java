package com.distocraft.dc5000.etl.gui.monitor;

import java.io.ByteArrayInputStream;
//Related to FRH
/*import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;*/
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author melantie Copyright Distocraft 2006 $id$
 */

public class Util { //NOPMD

  private static final SimpleDateFormat sdf_secs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  // private static final SimpleDateFormat sdf_date = new
  // SimpleDateFormat("yyyy-MM-dd");

  private static final SimpleDateFormat pickYear = new SimpleDateFormat("yyyy");
  private static final SimpleDateFormat pickMonth = new SimpleDateFormat("MM");
  private static final SimpleDateFormat pickDay = new SimpleDateFormat("dd");

  private static Log log = LogFactory.getLog("com.distocraft.dc5000.etl.gui.monitor.Util");

  /**
   * Get list of different raw timelevels
   *
   * @return Vector
   */

  public static List<String> getRawTimelevels(final Connection conn) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();
      result = statement.executeQuery("SELECT timelevel FROM dim_timelevel WHERE tablelevel = 'RAW'");

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return ret;
  }

  /**
   * Get list of different tablelevels
   *
   * @return Vector
   */

  public static List<String> getTablelevels(final Connection conn) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();
      result = statement.executeQuery("SELECT distinct(tablelevel) FROM TypeActivation ORDER BY tablelevel");

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return ret;
  }
  
  /**
   * Get list of monitored types for tech pack
   *
   * @param Tech
   *          pack name
   * @return List
   */

  public static List<List<String>> getMonitoredTypes(final String techPack, final Connection conn) {

    final List<List<String>> retval = new ArrayList<List<String>>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();

      final String sql = "SELECT typename,timelevel,status,modified,activationday FROM LOG_MonitoredTypes WHERE techpack_name = '"
          + techPack + "' ORDER BY typename";

      // log.debug(sql);

      result = statement.executeQuery(sql);

      // parse search result
      while (result.next()) {
        final List<String> l = new ArrayList<String>(); // NOPMD by eheijun on 02/06/11 15:47
        l.add(result.getString("TYPENAME"));
        l.add(result.getString("TIMELEVEL"));
        l.add(result.getString("STATUS"));
        final java.util.Date mod = new java.util.Date(result.getTimestamp("MODIFIED").getTime()); // NOPMD by eheijun on 02/06/11 15:47
        if (mod != null) {
          l.add(sdf_secs.format(mod));
        }
        final java.util.Date date = new java.util.Date(result.getDate("ACTIVATIONDAY").getTime()); // NOPMD by eheijun on 02/06/11 15:47
        if (date != null) {
          l.add(pickYear.format(date));
          l.add(pickMonth.format(date));
          l.add(pickDay.format(date));
        }
        retval.add(l);
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return retval;
  }

 
  /**
   * Get active tech pack names
   *
   * @return Vector
   */

  public static List<String> getTechPacks(final Connection conn) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();

      // Added support for CM tech packs also 6-10-2009 eheijun
      //String sql = "SELECT techpack_name FROM TPActivation WHERE (type = 'PM' OR type='CUSTOM' OR type='EVENT') AND STATUS = 'active' ORDER BY techpack_name";
      final String sql = "SELECT techpack_name FROM TPActivation WHERE (type = 'PM' OR type='CUSTOM' OR type='EVENT' OR type='CM' OR type='ENIQ_EVENT' ) AND STATUS = 'active' ORDER BY techpack_name";

      log.debug(sql);

      result = statement.executeQuery(sql);

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }
    return ret;
  }

  /**
   * Get active non events tech pack names
   *
   * @return Vector
   */

  public static List<String> getActiveNonEventsTechPacks(final Connection conn) {
    final Vector<String> ret = new Vector<String>();
    try {
      Statement statement = conn.createStatement();
      try {
        final String sql = "SELECT techpack_name FROM TPActivation WHERE (type = 'PM' OR type='CUSTOM' OR type='EVENT' OR type='CM') AND STATUS = 'active' ORDER BY techpack_name";
        log.debug(sql);
        ResultSet result = statement.executeQuery(sql);
        try {
          while (result.next()) {
            ret.add(result.getString(1));
          }
        } finally {
          result.close();
        }
        conn.commit();
      } finally {
        statement.close();
      }
    } catch (Exception e) {
      log.error("Exception: ", e);
    }
    return ret;
  }

  /**
   * Get all tech pack names
   *
   * @return Vector
   */

  public static List<String> getAllTechPacks(final Connection conn) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();

      final String sql = "SELECT techpack_name FROM TPActivation WHERE status = 'ACTIVE'";

      // log.debug(sql);

      result = statement.executeQuery(sql);

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }
    return ret;
  }

  /**
   * Get list of different measurement types and typeid for tech pack
   *
   * @param tech
   *          pack name
   * @return Vector
   */
/*
  public static Vector getMeasurementTypesAndTypeId(String techPack, Connection conn) {

    Vector ret = new Vector();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();
      result = statement.executeQuery("SELECT distinct(objectname), typeid FROM MeasurementType WHERE objectid like '"
          + techPack + "%' ORDER by objectname");

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
        ret.add(result.getString(2));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return ret;
  }
*/
  /**
   * Get list of different measurement types for tech pack
   *
   * @param tech
   *          pack name
   * @return Vector
   */

  public static List<String> getMeasurementTypes(final String techPack, final Connection conn) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    final String sql = "SELECT typename FROM TypeActivation WHERE techpack_name = '" + techPack
    + "' and status = 'ACTIVE' and tablelevel = 'RAW' ORDER by typename";

    try {
      statement = conn.createStatement();
      result = statement.executeQuery(sql);

      log.debug(sql);

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return ret;
  }

  /**
   * Get list of different active day measurement types for tech pack
   *
   * @param tech
   *          pack name
   * @return Vector
   */

  public static List<String> getActiveDayMeasurementTypes(final String techPack, final Connection conn) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    final String sql = "SELECT typename FROM TypeActivation WHERE techpack_name = '" + techPack
    + "' and status = 'ACTIVE' and tablelevel IN ('DAY','COUNT','DAYBH','RANKBH') ORDER by typename";

    try {
      statement = conn.createStatement();
      result = statement.executeQuery(sql);

      log.debug(sql);

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return ret;
  }


  /**
   * Get list of all active different measurement types
   *
   * @return Vector
   */

  public static List<String> getAllActiveDayMeasurementTypes(final Connection conn) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    final String sql = "SELECT typename FROM TypeActivation WHERE status = 'ACTIVE' and tablelevel IN ('DAY','COUNT','DAYBH','RANKBH')  ORDER BY typename";

    try {
      statement = conn.createStatement();

      log.debug(sql);

      result = statement.executeQuery(sql);

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return ret;
  }



  /**
   * Get list of all different measurement types
   *
   * @return Vector
   */

  public static List<String> getAllMeasurementTypes(final Connection conn) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    final String sql = "SELECT typename FROM TypeActivation WHERE status = 'ACTIVE' and tablelevel = 'RAW' ORDER BY typename";


    try {
      statement = conn.createStatement();

      log.debug(sql);

      result = statement.executeQuery(sql);

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return ret;
  }

  /**
   * Check if in there is allready this typename and timelevel pair in monitored
   * types
   *
   * @param Typename
   * @param Timelevel
   * @return boolean
   */

  public static boolean checkIfMonitoredExists(final Connection conn, final String tn, final String tl) {

    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    boolean exists = false;

    try {
      statement = conn.createStatement();

      final String sql = "SELECT count(*) as rowcount FROM LOG_MonitoredTypes WHERE typename = '" + tn
          + "' AND timelevel = '" + tl + "'";

      // log.debug(sql);

      result = statement.executeQuery(sql);

      result.next();

      final int rowCount = result.getInt("rowcount");

      if (rowCount > 0) {
        exists = true;
      }
      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return exists;
  }

  /**
   * Get list of all different set types
   *
   * @return Vector
   */

  public static List<String> getSetTypes(final Connection conn) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();
      result = statement.executeQuery("SELECT distinct(type) FROM META_COLLECTION_SETS order by type");

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return ret;
  }

  /**
   * Get list of packages for set type
   *
   * @return Vector
   */

  public static List<String> getEtlPackages(final Connection conn, final String setType) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();
      result = statement.executeQuery("select distinct(COLLECTION_SET_NAME) from META_COLLECTION_SETS"
          + " where TYPE = '" + setType + "' order by COLLECTION_SET_NAME");

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return ret;
  }

  /**
   * Get list of active packages for set type
   *
   * @return Vector
   */

  public static List<String> getActiveEtlPackages(final Connection conn, final String setType) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();
      result = statement.executeQuery("SELECT DISTINCT(COLLECTION_SET_NAME) FROM META_COLLECTION_SETS"
          + " WHERE TYPE = '" + setType + "' AND ENABLED_FLAG = 'Y' ORDER BY COLLECTION_SET_NAME");

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return ret;
  }

  /**
   * Get list of active packages for set type
   *
   * @return Vector
   */

  public static List<String> getActiveEtlPackages(final Connection conn) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();
      result = statement.executeQuery("SELECT DISTINCT(COLLECTION_SET_NAME) FROM META_COLLECTION_SETS"
          + " WHERE ENABLED_FLAG = 'Y' ORDER BY COLLECTION_SET_NAME");

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return ret;
  }

  /**
   * Function to set the POll delay for Interface in META_SCHEDULINGS table
   * @author xankigu
   * @param conn Connection to etlrep database
   * @param intf Interface name
   * @param pollDelay value of poll delay from adminui page ( From  INTFPollingDelayConf page )
   * @return
   */
  public static boolean setPollDelayForInterface(final Connection conn, final String intf, final int pollDelay){
	  Statement stmtToGetIntfID = null; // sql statement
	  Statement stmtToComaprePollDelay = null;
	  ResultSet resultToGetIntfID = null; // resultset
	  ResultSet resultToComaprePollDelay = null; // resultset
	  Statement stmtToUpdatePollDelay = null ;
	  long collSetID = 0 ;
	  boolean isSucc = false ;
	  try{
		  stmtToGetIntfID = conn.createStatement();
		  resultToGetIntfID = stmtToGetIntfID.executeQuery("select DISTINCT(COLLECTION_SET_ID) FROM META_COLLECTION_SETS"
		          + " WHERE TYPE = 'Interface' AND ENABLED_FLAG = 'Y' AND COLLECTION_SET_NAME = '" + intf + "'");
		  while(resultToGetIntfID.next()){
			  collSetID = resultToGetIntfID.getLong(1);
		  }

		  //Now update for this collSetID in table META_SCHEDULINGS
		  stmtToUpdatePollDelay = conn.createStatement();


		  //Set also the LAST_EXECUTION_TIME=null & LAST_EXEC_TIME_MS=0


		  final String updateQuery = "update META_SCHEDULINGS set LAST_EXECUTION_TIME=null, LAST_EXEC_TIME_MS=0, SCHEDULING_MIN = "
				  + pollDelay + " where COLLECTION_SET_ID = '" + collSetID
				  + "' AND EXECUTION_TYPE='Interval' AND NAME like 'TriggerAdapter_%'" ;

		  stmtToUpdatePollDelay.executeUpdate(updateQuery);

		  conn.commit();
		  isSucc = true ;
	  }catch(final Exception e){
		  log.error("Exception: ", e);
		  isSucc = false ;
	  }finally{
		  try{
			  if(stmtToGetIntfID != null){
				  stmtToGetIntfID.close();
			  }
			  if(stmtToUpdatePollDelay != null){
				  stmtToUpdatePollDelay.close();
			  }
			  if(resultToGetIntfID != null){
				  resultToGetIntfID.close();
			  }
		  }catch(final Exception e){
			  log.error("Exception: ", e);
			  isSucc = false ;
		  }
	  }
	  return  isSucc ;
  }

  /**
   * @author xankigu
   * @param conn connection to ETLREP database
   * @return Map of the list of interfaces with the interval as their key and InterfaceName:PollDelay as its corresponding value
   */
  public static Map<Long, ArrayList<String>> getActiveInterfaceIntrvalMap(final Connection conn) {
	    final Vector<String> ret = new Vector<String>();
	    Statement stmtToGetIntfID = null; // sql statement
	    Statement stmtToGetIntfIntrval = null; // sql statement
	    ResultSet resultToGetIntfID = null; // resultset
	    ResultSet resultToGetIntfIntrval = null; // resultset
	    Map<Long, ArrayList<String>> mapIntervalToInterfaceList = new HashMap<Long, ArrayList<String>>() ;

	    try {
	    	stmtToGetIntfID = conn.createStatement();
	    	stmtToGetIntfIntrval = conn.createStatement();
	      resultToGetIntfID = stmtToGetIntfID.executeQuery("select DISTINCT(COLLECTION_SET_ID), COLLECTION_SET_NAME FROM META_COLLECTION_SETS"
	          + " WHERE TYPE = 'Interface' AND ENABLED_FLAG = 'Y' ORDER BY COLLECTION_SET_NAME");

	      // parse search result
	      while (resultToGetIntfID.next()){
	        final long collSetID = resultToGetIntfID.getLong(1);
	        String interfaceName = resultToGetIntfID.getString(2);

	        
	        final String str = "select interval_hour, interval_min, SCHEDULING_MIN from META_SCHEDULINGS where "
	        		+ "COLLECTION_SET_ID='" + collSetID + "' AND EXECUTION_TYPE='Interval' AND NAME like 'TriggerAdapter_%'" ;
	        resultToGetIntfIntrval = stmtToGetIntfIntrval.executeQuery(str);

	        while(resultToGetIntfIntrval.next()){
        		long hours = resultToGetIntfIntrval.getLong(1);
        		long min = resultToGetIntfIntrval.getLong(2);

        		long totalMins = (hours * (long)60) + min ;

        		int pollDelay = resultToGetIntfIntrval.getInt(3);
        		interfaceName = interfaceName + ":" + pollDelay ;

        		Long minLongObj = Long.valueOf(totalMins);
        		
        		if (mapIntervalToInterfaceList.containsKey(minLongObj)) {
        			ArrayList<String> listOfInterface = mapIntervalToInterfaceList.get(minLongObj);
        			listOfInterface.add(interfaceName);
        		} else {
        			ArrayList<String> listOfInterface = new ArrayList<String>();
        			listOfInterface.add(interfaceName);
        			mapIntervalToInterfaceList.put(minLongObj, listOfInterface);
        		}
        		
	        }//while(resultToGetIntfIntrval.next()){
	      }//while (resultToGetIntfID.next()){

	      // commit
	      conn.commit();

	    } catch (Exception e) {
	      log.error("Exception: ", e);
	      return null ;
	    } finally {
	      // finally clean up
	      try {
	        if (resultToGetIntfID != null) {
	          resultToGetIntfID.close();
	        }
	        if (resultToGetIntfIntrval != null) {
		          resultToGetIntfIntrval.close();
	        }
	        if (stmtToGetIntfID != null) {
	          stmtToGetIntfIntrval.close();
	        }
	        if(stmtToGetIntfIntrval != null){
	        	stmtToGetIntfIntrval.close();
	        }
	      } catch (SQLException e) {
	        log.error("SQLException: ", e);
	      }
	    }
	    return mapIntervalToInterfaceList;
	  }


  /**
   * Function to get the Map with key as Interface name and value as Scheduling_min. Useful for checking the changed value of poll_delay by user in adminui page
   * @author xankigu
   * @param conn connection to ETLREP database
   * @return Map with key as Interface name and value as Scheduling_min. Useful for checking the changed value of poll_delay by user in adminui page
   */
  public static Map<String, Integer> getActiveInterfaceAndSchMinMap(final Connection conn) {
	    final Vector<String> ret = new Vector<String>();
	    Statement stmtToGetIntfID = null; // sql statement
	    Statement stmtToGetIntfIntrval = null; // sql statement
	    ResultSet resultToGetIntfID = null; // resultset
	    ResultSet resultToGetIntfIntrval = null; // resultset
	    Map<String, Integer> mapIntervalToInterfaceList = new HashMap<String, Integer>() ;

	    ArrayList<String> listOfInterface = null ;
	    try {
	    	stmtToGetIntfID = conn.createStatement();
	    	stmtToGetIntfIntrval = conn.createStatement();
	      resultToGetIntfID = stmtToGetIntfID.executeQuery("select DISTINCT(COLLECTION_SET_ID), COLLECTION_SET_NAME FROM META_COLLECTION_SETS"
	          + " WHERE TYPE = 'Interface' AND ENABLED_FLAG = 'Y' ORDER BY COLLECTION_SET_NAME");

	      mapIntervalToInterfaceList.clear();

	      // parse search result
	      while (resultToGetIntfID.next()){
	        final long collSetID = resultToGetIntfID.getLong(1);
	        String interfaceName = resultToGetIntfID.getString(2);

	        
	        final String str = "select SCHEDULING_MIN from META_SCHEDULINGS where "
	        		+ "COLLECTION_SET_ID='" + collSetID + "' AND EXECUTION_TYPE='Interval' AND NAME like 'TriggerAdapter_%'" ;
	        resultToGetIntfIntrval = stmtToGetIntfIntrval.executeQuery(str);

	        while(resultToGetIntfIntrval.next()){
	        		int schMin = resultToGetIntfIntrval.getInt(1);
	        		Integer schMinInt = Integer.valueOf(schMin);
	        		mapIntervalToInterfaceList.put(interfaceName, schMinInt);
	        }//while(resultToGetIntfIntrval.next()){
	      }//while (resultToGetIntfID.next()){

	      // commit
	      conn.commit();

	    } catch (Exception e) {
	      log.error("Exception: ", e);
	      return null ;
	    } finally {
	      // finally clean up
	      try {
	        if (resultToGetIntfID != null) {
	          resultToGetIntfID.close();
	        }
	        if (resultToGetIntfIntrval != null) {
		          resultToGetIntfIntrval.close();
	        }
	        if (stmtToGetIntfID != null) {
	          stmtToGetIntfIntrval.close();
	        }
	        if(stmtToGetIntfIntrval != null){
	        	stmtToGetIntfIntrval.close();
	        }
	      } catch (SQLException e) {
	        log.error("SQLException: ", e);
	      }
	    }
	    return mapIntervalToInterfaceList;
  }

  /**
   * Get list of all packages
   *
   * @return Vector
   */

  public static List<String> getEtlPackages(final Connection conn) {

    final Vector<String> ret = new Vector<String>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();
      result = statement.executeQuery("select distinct(COLLECTION_SET_NAME) from META_COLLECTION_SETS"
          + " order by COLLECTION_SET_NAME");

      // parse search result
      while (result.next()) {
        ret.add(result.getString(1));
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return ret;
  }

  /**
   * Get all sets of package. Only enabled are taken.
   *
   * @return all sets that are enabled
   */

  public static Map<String, List<List<String>>> getSetsForPackage(final Connection conn, final String packageName) { // NOPMD by eheijun on 02/06/11 15:47

    final Map<String, List<List<String>>> retval = new HashMap<String, List<List<String>>>();

    // select all sets that are possible to execute
    // see below more meanings of the fields
   /* String sqlStmt = "select S.COLLECTION_SET_ID,S.COLLECTION_SET_NAME,S.VERSION_NUMBER,S.ENABLED_FLAG,C.COLLECTION_ID,"
        + "C.COLLECTION_NAME, C.SETTYPE, C.HOLD_FLAG, C.ENABLED_FLAG, H.EXECUTION_TYPE, H.LAST_EXECUTION_TIME from "
        + "etlrep.META_COLLECTION_SETS S, etlrep.META_COLLECTIONS C left outer join etlrep.META_SCHEDULINGS H on "
        + "H.COLLECTION_SET_ID = c.COLLECTION_SET_ID and  H.COLLECTION_ID=C.COLLECTION_ID where S.ENABLED_FLAG='y' "
        + "AND C.ENABLED_FLAG='y' and C.COLLECTION_SET_ID = S.COLLECTION_SET_ID and C.VERSION_NUMBER = S.VERSION_NUMBER "
        + "and S.COLLECTION_SET_NAME = '" + packageName + "' and C.SETTYPE <> 'Aggregator' order by COLLECTION_NAME";
*/
    final String sqlStmt = "select S.COLLECTION_SET_ID,S.COLLECTION_SET_NAME,S.VERSION_NUMBER,S.ENABLED_FLAG,C.COLLECTION_ID,"
        + "C.COLLECTION_NAME, C.SETTYPE, C.HOLD_FLAG, C.ENABLED_FLAG, H.SCHEDULING_MONTH, H.SCHEDULING_DAY, H.SCHEDULING_HOUR,"
        + "H.SCHEDULING_MIN, MON_FLAG, TUE_FLAG,WED_FLAG,THU_FLAG,FRI_FLAG,SAT_FLAG,SUN_FLAG,"
        + "H.INTERVAL_HOUR, H.INTERVAL_MIN,H.TRIGGER_COMMAND, H.OS_COMMAND, H.SCHEDULING_YEAR,H.EXECUTION_TYPE, H.LAST_EXECUTION_TIME from "
        + "META_COLLECTION_SETS S, META_COLLECTIONS C left outer join META_SCHEDULINGS H on "
        + "H.COLLECTION_SET_ID = c.COLLECTION_SET_ID and  H.COLLECTION_ID=C.COLLECTION_ID where S.ENABLED_FLAG='y' "
        + "AND C.ENABLED_FLAG='y' and C.COLLECTION_SET_ID = S.COLLECTION_SET_ID and C.VERSION_NUMBER = S.VERSION_NUMBER "
        + "and S.COLLECTION_SET_NAME = '" + packageName + "' and C.SETTYPE <> 'Aggregator' order by COLLECTION_NAME";

    // make query and build up hierarchial list
    List<List<String>> collectionSet = null;
    long oldSetID = -1;
    String collectionSetName = null;
    String oldCollectionName = null;
    Statement stmt = null;
    ResultSet rset = null;
    //TR HL89284: description is added to give details about execution of set
     String execTypeDescription = null;
    String execType = null;
    HashMap<String, String> tempSchedulingtype = null;
    //
    try {
      // log.debug("Executing query: " + sqlStmt);

      stmt = conn.createStatement();
      rset = stmt.executeQuery(sqlStmt);
      // log.debug(sqlStmt);

      // as long as the collection_set_id remains same we have same
      // collection set (measurementtype), when it changes create a
      // new "set"
      while (rset.next()) {
    	  
        final long setID = rset.getLong(1); // S.COLLECTION_SET_ID
        if (setID != oldSetID) {
          if (collectionSet != null) {
            retval.put(collectionSetName, collectionSet);
          }

          // old one saved (or was unexistent, first loop) - create new one
          collectionSet = new ArrayList<List<String>>(); // NOPMD by eheijun on 02/06/11 15:47
          tempSchedulingtype = new HashMap<String, String>(); // NOPMD by eheijun on 02/06/11 15:47
        }
        // save set name - for the hashmap
        collectionSetName = rset.getString(2);

        // create one item which is saved into the collectionSet
        String collectionName = rset.getString(6);
        if (collectionName.equalsIgnoreCase(oldCollectionName) ){
           continue;
        }
        final List<String> item = new ArrayList<String>(); // NOPMD by eheijun on 02/06/11 15:47
        item.add("" + setID);
        item.add(collectionSetName); // E_BSS,N_BSS, etc...
        item.add(rset.getString(3)); // S.VERSION_NUMBER
        
        item.add(rset.getString(5)); // C.COLLECTION_ID
        item.add(collectionName); // C.COLLECTION_NAME, loader1, loader2,
        // blabla_loader, etc...
        item.add(rset.getString(7)); // C.SETTYPE - parser, adapter,
        item.add(rset.getString(8)); // C.HOLD_FLAG
        
        //TR HL89284: description is added to give details about execution of set
        execType = rset.getString(26);
        item.add(execType); // H.EXEcution_TYPE

        tempSchedulingtype.put("schMonth", rset.getString(10));
        tempSchedulingtype.put("schDay", rset.getString(11));
        tempSchedulingtype.put("schHour", rset.getString(12));
        tempSchedulingtype.put("schMin", rset.getString(13));
        tempSchedulingtype.put("monFlag", rset.getString(14));
        tempSchedulingtype.put("tueFlag", rset.getString(15));
        tempSchedulingtype.put("wedFlag", rset.getString(16));
        tempSchedulingtype.put("thuFlag", rset.getString(17));
        tempSchedulingtype.put("friFlag", rset.getString(18));
        tempSchedulingtype.put("satFlag", rset.getString(19));
        tempSchedulingtype.put("sunFlag", rset.getString(20));
        tempSchedulingtype.put("intervalHour", rset.getString(21));
        tempSchedulingtype.put("intervalMin", rset.getString(22));
        tempSchedulingtype.put("triggerComamnd", rset.getString(23));
        tempSchedulingtype.put("osCommand", rset.getString(24));
        tempSchedulingtype.put("schYear", rset.getString(25));
        execTypeDescription = getSchedulingDescription(execType, tempSchedulingtype);
        //TR HL89284: end
        item.add(execTypeDescription); // execTypeDescription
        item.add(rset.getString(27)); // H.LAST_EXECUTION_TIME

        // add item to collection set
        if (collectionSet != null) {
          collectionSet.add(item);
        }
        // and save id - next loop might belong to same measurementtype
        oldSetID = setID;
        oldCollectionName =  collectionName ;
      }

      // and add the last collection too :)
      if (collectionSet != null) {
        retval.put(collectionSetName, collectionSet);
      }

    } catch (Exception ex) {
      // log.error("SQLException: " + ex);
      ex.printStackTrace();
    } finally {
      try {
        if (rset != null) {
          rset.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
      try {
        if (stmt != null) {
          stmt.close();
        }
      } catch (SQLException e) {
        log.error("Exception", e);
      }
    }

    // return hierarchy list
    return retval;
   
  }

	/**
	 * This Method returns a valid range, starting from the current year
	 * and ending 5 years in the future.
	 * @param startYear
	 * @return
	 */
	public static List<String> getFutureRange(final String startYear) {
	  final int start = Integer.parseInt(startYear);
	  final List<String> result = new ArrayList<String>(6);
		for(int i = 0; i < 6; i++){
			result.add(Integer.toString(start + i));
		}
		return result;
	}

	/**
	 * Used to create description for each scheduling type
	 * @param typ
	 * @param tempSchedulingType
	 * @return
	 */
	private static String getSchedulingDescription(final String typ, final Map<String, String> tempSchedulingType) { // NOPMD by eheijun on 02/06/11 15:47
    // Description
    if (typ != null) {
      if (typ.equals("interval") || typ.equals("intervall")) {
        return "Occurs once every " + tempSchedulingType.get("intervalHour") + " hours " + tempSchedulingType.get("intervalMin") + " minutes";
      } else if (typ.equals("timeDirCheck")) {
        final String checkIfEmpty = (tempSchedulingType.get("triggerCommand") == null) ? "true" : tempSchedulingType.get("triggerCommand").split(";")[0].trim();
        final String dirs = (tempSchedulingType.get("triggerCommand") == null) ? "" : tempSchedulingType.get("triggerCommand").split(";")[1].trim();
        return "Occurs once every " + tempSchedulingType.get("intervalHour") + " hours " + tempSchedulingType.get("intervalMin") + " minutes" +
               ", check for empty directories = " + checkIfEmpty + ", directories to check: " + dirs;
      } else if (typ.equals("wait")) {
        return "Waiting trigger";
      } else if (typ.equals("fileExists")) {
        return "Waiting file " + tempSchedulingType.get("triggerCommand");
      } else if (typ.equals("weekly")) {
        final StringBuffer sb = new StringBuffer("");
        if ("Y".equals(tempSchedulingType.get("monFlag"))) {
          sb.append("Mon");
        }
        if ("Y".equals(tempSchedulingType.get("tueFlag"))) {
          if (sb.length() > 0) {
            sb.append(",");
          }
          sb.append("Tue");
        }
        if ("Y".equals(tempSchedulingType.get("wedFlag"))) {
          if (sb.length() > 0) {
            sb.append(",");
          }
          sb.append("Wed");
        }
        if ("Y".equals(tempSchedulingType.get("thuFlag"))) {
          if (sb.length() > 0) {
            sb.append(",");
          }
          sb.append("Thu");
        }
        if ("Y".equals(tempSchedulingType.get("friFlag"))) {
          if (sb.length() > 0) {
            sb.append(",");
          }
          sb.append("Fri");
        }
        if ("Y".equals(tempSchedulingType.get("satFlag"))) {
          if (sb.length() > 0) {
            sb.append(",");
          }
          sb.append("Sat");
        }
        if ("Y".equals(tempSchedulingType.get("sunFlag"))) {
          if (sb.length() > 0) {
            sb.append(",");
          }
          sb.append("Sun");
        }
        sb.insert(0, "Every ");
        sb.append(" at ");
        if (String.valueOf(tempSchedulingType.get("schHour")).length() <= 1) {
          sb.append("0");
        }
        sb.append(tempSchedulingType.get("schHour")).append(":");
        if (String.valueOf(tempSchedulingType.get("schMin")).length() <= 1) {
          sb.append("0");
        }
        sb.append(tempSchedulingType.get("schMin"));

        return sb.toString();
      } else if (typ.equals("monthly")) {
        final int day = Integer.parseInt((String) tempSchedulingType.get("schMon"));
        if (day <= 0) {
          final StringBuffer sb = new StringBuffer("Occures last day of month at ");
          if (String.valueOf(tempSchedulingType.get("schHour")).length() <= 1) {
            sb.append("0");
          }
          sb.append(tempSchedulingType.get("schHour")).append(":");
          if (String.valueOf(tempSchedulingType.get("schMin")).length() <= 1) {
            sb.append("0");
          }
          sb.append(tempSchedulingType.get("schMin"));
          return sb.toString();
        } else {
          final StringBuffer sb = new StringBuffer("Occures ");
          sb.append(tempSchedulingType.get("schDay")).append(". day of month at ");
          if (String.valueOf(tempSchedulingType.get("schHour")).length() <= 1) {
            sb.append("0");
          }
          sb.append(tempSchedulingType.get("schHour")).append(":");
          if (String.valueOf(tempSchedulingType.get("schMin")).length() <= 1) {
            sb.append("0");
          }
          sb.append(tempSchedulingType.get("schMin"));

          return sb.toString();
        }

      } else if (typ.equals("once")) {
        final StringBuffer sb = new StringBuffer("Occures ");
        sb.append(tempSchedulingType.get("schDay")).append(".").append(Integer.parseInt((String) tempSchedulingType.get("schMonth")) + 1);
        sb.append(".").append(tempSchedulingType.get("schYear")).append(" at ");
        if (String.valueOf(tempSchedulingType.get("schHour")).length() <= 1) {
          sb.append("0");
        }
        sb.append(tempSchedulingType.get("schHour")).append(":");
        if (String.valueOf(tempSchedulingType.get("schMin")).length() <= 1) {
          sb.append("0");
        }
        sb.append(tempSchedulingType.get("schMin"));

        return sb.toString();
        } else if (typ.equals("weeklyinterval")) {
          StringBuffer sb = new StringBuffer("Occures every ");

          if ("Y".equals(tempSchedulingType.get("monFlag"))) {
            sb.append("Mon");
          }
          if ("Y".equals(tempSchedulingType.get("tueFlag"))) {
            if (sb.length() > 0) {
              sb.append(",");
            }
            sb.append("Tue");
          }
          if ("Y".equals(tempSchedulingType.get("wedFlag"))) {
            if (sb.length() > 0) {
              sb.append(",");
            }
            sb.append("Wed");
          }
          if ("Y".equals(tempSchedulingType.get("thuFlag"))) {
            if (sb.length() > 0) {
              sb.append(",");
            }
            sb.append("Thu");
          }
          if ("Y".equals(tempSchedulingType.get("friFlag"))) {
            if (sb.length() > 0) {
              sb.append(",");
            }
            sb.append("Fri");
          }
          if ("Y".equals(tempSchedulingType.get("satFlag"))) {
            if (sb.length() > 0) {
              sb.append(",");
            }
            sb.append("Sat");
          }
          if ("Y".equals(tempSchedulingType.get("sunFlag"))) {
            if (sb.length() > 0) {
              sb.append(",");
            }
            sb.append("Sun");
          }

          // Drop the comma, if it is the first character before the weekdays in
          // info string.
          if (sb.charAt(14) == ',') {
            sb.deleteCharAt(14);
          }

          // Drop the comma, if it is the last character in info string.
          if (sb.charAt(sb.length() - 1) == ',') {
            sb = new StringBuffer(sb.substring(0, sb.length() - 1));
          }

          // Get/parse the interval values from database to these variables from
          // OSCommand.
          final String serializedIntervalString = (String) tempSchedulingType.get("osCommand");

          final Properties intervalProps = new Properties();

          if (serializedIntervalString != null && serializedIntervalString.length() > 0) {

            try {
              final ByteArrayInputStream bais = new ByteArrayInputStream(serializedIntervalString.getBytes());
              intervalProps.load(bais);
              bais.close();
              log.debug("Interval Properties-object read in view");
            } catch (Exception e) {
              log.debug("Interval Properties-object error in view");
            }

            final Integer intervalStartHour = new Integer(intervalProps.getProperty("intervalStartHour"));
            final Integer intervalStartMinute = new Integer(intervalProps.getProperty("intervalStartMinute"));
            final Integer intervalEndHour = new Integer(intervalProps.getProperty("intervalEndHour"));
            final Integer intervalEndMinute = new Integer(intervalProps.getProperty("intervalEndMinute"));

            String intervalStartHourString = new String(intervalStartHour.toString());
            String intervalStartMinuteString = new String(intervalStartMinute.toString());
            String intervalEndHourString = new String(intervalEndHour.toString());
            String intervalEndMinuteString = new String(intervalEndMinute.toString());

            if (intervalStartHour.intValue() < 10 && intervalStartHour.intValue() >= 0) {
              intervalStartHourString = new String("0" + intervalStartHour.toString());
            }
            if (intervalStartMinute.intValue() < 10 && intervalStartMinute.intValue() >= 0) {
              intervalStartMinuteString = new String("0" + intervalStartMinute.toString());
            }
            if (intervalEndHour.intValue() < 10 && intervalEndHour.intValue() >= 0) {
              intervalEndHourString = new String("0" + intervalEndHour.toString());
            }
            if (intervalEndMinute.intValue() < 10 && intervalEndMinute.intValue() >= 0) {
              intervalEndMinuteString = new String("0" + intervalEndMinute.toString());
            }

            sb.append(" from ");
            sb.append(intervalStartHourString + ":" + intervalStartMinuteString);

            sb.append(" to ");
            sb.append(intervalEndHourString + ":" + intervalEndMinuteString);
          }
          sb.append(" every " + tempSchedulingType.get("schHour") + " hours and " + tempSchedulingType.get("schMin") + " minutes");
          return sb.toString();

        } else if (typ.equals("onStartup")) {
        return "Executed on ETLC startup";
      } else {
        return "unknown schedule type";
      }
    }
    return "No Scheduling Type Defined";
  }


     /**
	   * Get the enabled set
	   * @return String
	   * ejohabd ,20100909 , HL76673
	   */
	  public static String getEnabledSet(final String aggregation, final Connection conn){
		  Statement stmt_disableSql = null;
		  ResultSet rset_disableSql = null;
		  String value="";
		  final String disableSql = "SELECT aggregation FROM LOG_AggregationRules where AGGREGATION = '" + aggregation + "'";
		  try{
		  	stmt_disableSql=conn.createStatement();
		  	log.debug("Executing query: " + disableSql);
		  	rset_disableSql=stmt_disableSql.executeQuery(disableSql);
		  	while (rset_disableSql.next()){
		  		value=rset_disableSql.getString(1);
		  		log.debug("Show Resultset value: " + value + " and set name : " + aggregation);
		  	}

		  }catch (Exception e) {
		      log.error("Exception: ", e);
		  } finally {
		    // finally clean up
		    try {
		      if (rset_disableSql != null) {
		    	  rset_disableSql.close();
		      }
		      if (stmt_disableSql != null) {
		    	  stmt_disableSql.close();
		      }
		    } catch (SQLException e) {
		      log.error("SQLException: ", e);
		    }
		  }
		  // Check that case is correct, as dB select can be case-insetive.
		  // eeoidiv, 20110601, HO31913:AdminUI showing old BusyHours causing confusion
		  if(!value.equals(aggregation)) {
			  value = "";
		  }
		  return value;
		}
	  
	    
	  //FROP changes

	  /*
	  public static void updateFRHSynctable(Connection conn, String format)
				throws SQLException {

			Statement statement = null;
			try {
				final String sql = "UPDATE META_FRH_SYNC SET LAST_MODIFIED_TIME= '"
						+ format + "' ";
				statement = conn.createStatement();
				statement.executeUpdate(sql);
				conn.commit();
			} catch (SQLException e) {
				log.error("SQLException ", e);
			}
			finally {
				try {
					if (statement != null) {
						statement.close();
					}
				} catch (Exception e) {
					log.error("Exception ", e);
				}
			}
		}
		
	public static List<String> getTypeNameinfo(final String techpack_name,
				final String search, final Connection conn) {

			final Vector<String> ret = new Vector<String>();

			Statement statement = null;
			ResultSet rs = null;

			try {	
				final String sql = "select distinct substr(typeid,CHARINDEX(')', typeid)+3) from measurementcounter where typeid like '"+ techpack_name+":%' and typeid like '%" + search + "%' order by substr(typeid,CHARINDEX(')', typeid)+3)";

				statement = conn.createStatement();
				rs = statement.executeQuery(sql);

				while (rs.next()) {
					String mo = rs.getString(1);
					ret.add(mo);
				}		
				conn.commit();
			}catch (Exception e) {
				log.error("Exception: ", e);
			} finally {

				try {
					if (rs != null) {
						rs.close();
					}
					if (statement != null) {
						statement.close();
					}
				} catch (SQLException e) {
					log.error("SQLException: ", e);

				}
			}
			return ret;

		}
		
	public static List<String> getMOType(final Connection etlrepconn,
				final Connection dwhrepconn, final String neType) {

			final Vector<String> tpname = new Vector<String>();
			final Vector<String> techPackName = new Vector<String>();

			Statement statement = null;
			ResultSet result1 = null;
			ResultSet result2 = null;

			try {

				statement = dwhrepconn.createStatement();

				final String sql1 = "SELECT TECHPACK_NAME FROM TPACTIVATION WHERE STATUS='ACTIVE' ORDER BY TECHPACK_NAME";
				result1 = statement.executeQuery(sql1);

				while (result1.next()) {
					tpname.add(result1.getString(1));

				}
				dwhrepconn.commit();

				statement = etlrepconn.createStatement();

				final String sql2 = "SELECT TECHPACK_NAME FROM META_FRH_NODEASSIGNMENT WHERE NE_TYPE='"
						+ neType + "'";

				result2 = statement.executeQuery(sql2);
				while (result2.next()) {
					techPackName.add(result2.getString(1));

				}

				etlrepconn.commit();

				techPackName.retainAll(tpname);

			} catch (SQLException e) {
				log.error("SQLException: ", e);
			} finally {

				try {
					if (result1 != null) {
						result1.close();
					}
					if (result2 != null) {
						result2.close();
					}
					if (statement != null) {
						statement.close();
					}
				} catch (Exception e) {
					log.error("Exception: ", e);
				}
			}
			return techPackName;
		}
		
	public static List<String> getCounters(final String typeName,final String search,final String techPack,
				final Connection conn) {

			
			final Vector<String> ret = new Vector<String>();
			Statement statement = null;
			ResultSet result = null;
			
			try {			
				final String sql = "SELECT DISTINCT DATANAME FROM MEASUREMENTCOUNTER WHERE TYPEID LIKE '%:((%)):"+typeName+"' ";
				statement = conn.createStatement();
				result = statement.executeQuery(sql);
				while (result.next()) {
					ret.add(result.getString(1));

				}	
				conn.commit();
			}  catch (SQLException e) {
				log.error("SQLException: ", e);
			} finally {

				try {
					if (result != null) {
						result.close();
					}
					if (statement != null) {
						statement.close();
					}
				} catch (Exception e) {
					log.error("Exception: ", e);

				}
			}
			return ret;

		}

		 public static List<String> getFRHNETypes(final Connection conn) {
		
		 final List<String> ret = new ArrayList<String>();
		 Statement statement = null;
		 ResultSet result = null;
		
		 try {
			 	 
		 final String sql1 ="SELECT DISTINCT NE_TYPE FROM META_FRH_NODEASSIGNMENT";	
		 
		 statement = conn.createStatement();
		
		 result = statement.executeQuery(sql1);
		 
		 while (result.next()) {
		 ret.add(result.getString(1));
		 }
		
		 conn.commit();
		
		 } catch (SQLException e) {
		 log.error("SQLException: ", e);
		 } finally {
		
		 try {
		   if (result != null) {
			   result.close();
		    }
		   if (statement != null) {
		      statement.close();
		    }
		 } catch (Exception e) {
		     log.error("Exception: ", e);
		 	}
		 }
		 return ret;
		
		 }
		

		public static List<String> getCFCounters(final String typeName,
				final String netype, final Connection conn) {

			final Vector<String> ret = new Vector<String>();
			Statement statement = null;
			ResultSet result = null;

			try {
				statement = conn.createStatement();
				final String sql = "select distinct counter from META_FRH_CounterFiltering where ne_type = '"
						+ netype + "' and managed_objects ='" + typeName + "' ";

				result = statement.executeQuery(sql);

				while (result.next()) {
					ret.add(result.getString(1));
				}
				conn.commit();

			} catch (SQLException e) {
				log.error("SQLException: ", e);
			} finally {

				try {
					if (result != null) {
						result.close();
					}
					if (statement != null) {
						statement.close();
					}

				} catch (Exception e) {
					log.error("Exception: ", e);

				}
			}
			return ret;
		}

	*/  
}