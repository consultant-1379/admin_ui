package com.distocraft.dc5000.etl.gui.monitor;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ssc.rockfactory.RockFactory;
import utils.MemoryDatabaseUtility;

public class UtilTest {

  public static final String TEST_APPLICATION = UtilTest.class.getName();

  private static RockFactory etlrepRock;

  private static RockFactory dwhrepRock;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    etlrepRock = new RockFactory(MemoryDatabaseUtility.TEST_ETLREP_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
        MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL etlrepsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_ETLREP_BASIC);
    if (etlrepsqlurl == null) {
      System.out.println("etlrep script can not be loaded!");
    } else {
      MemoryDatabaseUtility.loadSetup(etlrepRock, etlrepsqlurl);
    }
    
    dwhrepRock = new RockFactory(MemoryDatabaseUtility.TEST_DWHREP_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
        MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL dwhrepsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWHREP_BASIC);
    if (etlrepsqlurl == null) {
      System.out.println("dwhrep script can not be loaded!");
    } else {
      MemoryDatabaseUtility.loadSetup(dwhrepRock, dwhrepsqlurl);
    }

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    MemoryDatabaseUtility.shutdown(etlrepRock);
    MemoryDatabaseUtility.shutdown(dwhrepRock);
  }

  @Test
  public void getFutureRangeFromThisYear() {
    final List<String> actual = Util.getFutureRange("2009");
    final List<String> expected = new ArrayList<String>(6);
    expected.add("2009");
    expected.add("2010");
    expected.add("2011");
    expected.add("2012");
    expected.add("2013");
    expected.add("2014");
    assertEquals(expected, actual);
  }

  @Test
  public void getSChedulingDescriptionTestForUpdateMonitoring() throws SQLException {
    Method m;
    String data = null;

    Statement stmt = etlrepRock.getConnection().createStatement();
    try {
      stmt.executeUpdate("INSERT INTO Meta_collection_sets VALUES( 0  ,'DWH_MONITOR'  ,'Monitoring'  ,'((49))'  ,'Y'  ,'Maintenance' )");
      stmt.executeUpdate("INSERT INTO Meta_collections VALUES( 0  ,'UpdateMonitoring'  ,'testCOLLECTION'  ,'testMAIL_ERROR_ADDR'  ,'testMAIL_FAIL_ADDR'  ,'testMAIL_BUG_ADDR'  ,1  ,1  ,1  ,'N'  ,'N'  ,'2000-01-01 00:00:00.0'  ,'((49))'  ,0  ,'testUSE_BATCH_ID'  ,3  ,33  ,'Y'  ,'Support'  ,'Y'  ,'testMEASTYPE'  ,'N'  ,'testSCHEDULING_INFO' )");
      stmt.executeUpdate("INSERT INTO Meta_schedulings VALUES( '((49))'  ,1  ,'wait'  ,null  ,null  ,null  ,null  ,null  ,0  ,0  ,null  ,null  ,null  ,null  ,null  ,null  ,null  ,'Executed'  ,'2000-06-27 00:00:00.0'  ,null  ,null  ,'UpdateMonitoring'  ,'N'  ,null  ,null  ,null  ,1 )");
    } finally {
      stmt.close();
    }

    try {
      m = Util.class.getDeclaredMethod("getSchedulingDescription", String.class, Map.class);
      m.setAccessible(true);

      HashMap schType = getSchedulingMap();

      String type = schType.get("execType").toString();

      try {
        data = (String) m.invoke(new Util(), type, schType);
      } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      /* Cleaning up data */
      stmt = etlrepRock.getConnection().createStatement();
      try {
        stmt.executeUpdate("DELETE FROM Meta_collection_sets WHERE VERSION_NUMBER = '((49))'");
        stmt.executeUpdate("DELETE FROM Meta_collections WHERE VERSION_NUMBER = '((49))'");
        stmt.executeUpdate("DELETE FROM Meta_schedulings WHERE VERSION_NUMBER = '((49))'");
      } finally {
        stmt.close();
      }
      

    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("Waiting trigger", data);
  }

  @Test
  public void getSChedulingDescriptionTestForSessionLoader() throws SQLException {
    Method m;
    String data = null;

    Statement stmt = etlrepRock.getConnection().createStatement();
    try {
      stmt.executeUpdate("INSERT INTO Meta_collection_sets VALUES( 0  ,'DWH_MONITOR'  ,'Monitoring'  ,'((49))'  ,'Y'  ,'Maintenance' )");
      stmt.executeUpdate("INSERT INTO Meta_collections VALUES( 1  ,'SessionLoader_Starter'  ,'testCOLLECTION'  ,'testMAIL_ERROR_ADDR'  ,'testMAIL_FAIL_ADDR'  ,'testMAIL_BUG_ADDR'  ,1  ,1  ,1  ,'N'  ,'N'  ,'2000-01-01 00:00:00.0'  ,'((49))'  ,0  ,'testUSE_BATCH_ID'  ,10  ,5  ,'Y'  ,'Support'  ,'Y'  ,'testMEASTYPE'  ,'N'  ,'testSCHEDULING_INFO' )");
      stmt.executeUpdate("INSERT INTO Meta_schedulings VALUES( '((49))'  ,2  ,'interval'  ,null  ,5  ,15  ,14  ,0  ,0  ,1  ,null  ,null  ,null  ,null  ,null  ,null  ,null  ,'Executed'  ,'2000-06-27 00:00:00.0'  ,0  ,15  ,'SessionLoader_Starter'  ,'N'  ,null  ,2006  ,null  ,1 )");
    } finally {
      stmt.close();
    }

    try {
      m = Util.class.getDeclaredMethod("getSchedulingDescription", String.class, Map.class);
      m.setAccessible(true);

      HashMap schType = getSchedulingMap();
      String type = schType.get("execType").toString();

      try {
        data = (String) m.invoke(new Util(), type, schType);
      } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      /* Cleaning up data */
      stmt = etlrepRock.getConnection().createStatement();
      try {
        stmt.executeUpdate("DELETE FROM Meta_collection_sets WHERE VERSION_NUMBER = '((49))'");
        stmt.executeUpdate("DELETE FROM Meta_collections WHERE VERSION_NUMBER = '((49))'");
        stmt.executeUpdate("DELETE FROM Meta_schedulings WHERE VERSION_NUMBER = '((49))'");
      } finally {
        stmt.close();
      }
    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("Occurs once every 0 hours 15 minutes", data);
  }

  @Test
  public void getSChedulingDescriptionTestForAutomaticAggregation() throws SQLException {
    Method m;
    String data = null;

    Statement stmt = etlrepRock.getConnection().createStatement();
    try {
      stmt.executeUpdate("INSERT INTO Meta_collection_sets VALUES( 0  ,'DWH_MONITOR'  ,'Monitoring'  ,'((49))'  ,'Y'  ,'Maintenance' )");
      stmt.executeUpdate("INSERT INTO Meta_collections VALUES( 6  ,'AutomaticAggregation'  ,'testCOLLECTION'  ,'testMAIL_ERROR_ADDR'  ,'testMAIL_FAIL_ADDR'  ,'testMAIL_BUG_ADDR'  ,1  ,1  ,1  ,'N'  ,'N'  ,'2000-01-01 00:00:00.0'  ,'((49))'  ,0  ,'testUSE_BATCH_ID'  ,3  ,33  ,'Y'  ,'Support'  ,'Y'  ,'testMEASTYPE'  ,'N'  ,'testSCHEDULING_INFO' )");
      stmt.executeUpdate("INSERT INTO Meta_schedulings VALUES( '((49))'  ,9  ,'interval'  ,null  ,10  ,20  ,18  ,45  ,0  ,6  ,null  ,null  ,null  ,null  ,null  ,null  ,null  ,'Executed'  ,'2000-06-27 00:00:00.0'  ,1  ,0  ,'Aggregate'  ,'N'  ,null  ,2006  ,null  ,1 )");
    } finally {
      stmt.close();
    }

    try {
      m = Util.class.getDeclaredMethod("getSchedulingDescription", String.class, Map.class);
      m.setAccessible(true);

      HashMap schType = getSchedulingMap();
      String type = schType.get("execType").toString();

      try {
        data = (String) m.invoke(new Util(), type, schType);
      } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      /* Cleaning up data */
      stmt = etlrepRock.getConnection().createStatement();
      try {
        stmt.executeUpdate("DELETE FROM Meta_collection_sets WHERE VERSION_NUMBER = '((49))'");
        stmt.executeUpdate("DELETE FROM Meta_collections WHERE VERSION_NUMBER = '((49))'");
        stmt.executeUpdate("DELETE FROM Meta_schedulings WHERE VERSION_NUMBER = '((49))'");
      } finally {
        stmt.close();
      }
    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("Occurs once every 1 hours 0 minutes", data);
  }

  @Test
  public void getSChedulingDescriptionTestForAutomaticReAggregation() throws SQLException {
    Method m;
    String data = null;

    Statement stmt = etlrepRock.getConnection().createStatement();
    try {
      stmt.executeUpdate("INSERT INTO Meta_collection_sets VALUES( 0  ,'DWH_MONITOR'  ,'Monitoring'  ,'((49))'  ,'Y'  ,'Maintenance' )");
      stmt.executeUpdate("INSERT INTO Meta_collections VALUES( 9  ,'AutomaticREAggregation'  ,'testCOLLECTION'  ,'testMAIL_ERROR_ADDR'  ,'testMAIL_FAIL_ADDR'  ,'testMAIL_BUG_ADDR'  ,1  ,1  ,1  ,'N'  ,'N'  ,'2000-01-01 00:00:00.0'  ,'((49))'  ,0  ,'testUSE_BATCH_ID'  ,3  ,33  ,'Y'  ,'Support'  ,'Y'  ,'testMEASTYPE'  ,'N'  ,'testSCHEDULING_INFO' )");
      stmt.executeUpdate("INSERT INTO Meta_schedulings VALUES( '((49))'  ,0  ,'weekly'  ,null  ,2  ,10  ,19  ,0  ,0  ,9 ,'Y'  ,'Y' ,'Y'  ,'Y'  ,'Y' ,'Y','Y','Executed'  ,'2000-06-27 00:00:00.0'  ,1  ,0  ,'DailyReAggregation'  ,'N'  ,null  ,2006  ,null  ,1 )");
    } finally {
      stmt.close();
    }

    try {
      m = Util.class.getDeclaredMethod("getSchedulingDescription", String.class, Map.class);
      m.setAccessible(true);

      HashMap schType = getSchedulingMap();
      String type = schType.get("execType").toString();

      try {
        data = (String) m.invoke(new Util(), type, schType);
      } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      /* Cleaning up data */
      stmt = etlrepRock.getConnection().createStatement();
      try {
        stmt.executeUpdate("DELETE FROM Meta_collection_sets WHERE VERSION_NUMBER = '((49))'");
        stmt.executeUpdate("DELETE FROM Meta_collections WHERE VERSION_NUMBER = '((49))'");
        stmt.executeUpdate("DELETE FROM Meta_schedulings WHERE VERSION_NUMBER = '((49))'");
      } finally {
        stmt.close();
      }
    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("Every Mon,Tue,Wed,Thu,Fri,Sat,Sun at 19:00", data);
  }

  @Test
  public void getSChedulingDescriptionTestForUpdateFirstLoading() throws SQLException {
    Method m;
    String data = null;

    Statement stmt = etlrepRock.getConnection().createStatement();
    try {
      stmt.executeUpdate("INSERT INTO Meta_collection_sets VALUES( 0  ,'DWH_MONITOR'  ,'Monitoring'  ,'((49))'  ,'Y'  ,'Maintenance' )");
      stmt.executeUpdate("INSERT INTO Meta_collections VALUES( 8  ,'UpdateFirstLoadings'  ,'testCOLLECTION'  ,'testMAIL_ERROR_ADDR'  ,'testMAIL_FAIL_ADDR'  ,'testMAIL_BUG_ADDR'  ,1  ,1  ,1  ,'N'  ,'N'  ,'2000-01-01 00:00:00.0'  ,'((49))'  ,0  ,'testUSE_BATCH_ID'  ,3  ,33  ,'Y'  ,'Support'  ,'Y'  ,'testMEASTYPE'  ,'N'  ,'testSCHEDULING_INFO' )");
      stmt.executeUpdate("INSERT INTO Meta_schedulings VALUES( '((49))'  ,0  ,'weekly'  ,null  ,2  ,10  ,19  ,0  ,0  ,9 ,'Y'  ,'Y' ,'Y'  ,'Y'  ,'Y' ,'Y','Y','Executed'  ,'2000-06-27 00:00:00.0'  ,1  ,0  ,'DailyReAggregation'  ,'N'  ,null  ,2006  ,null  ,1 )");
    } finally {
      stmt.close();
    }

    try {
      m = Util.class.getDeclaredMethod("getSchedulingDescription", String.class, Map.class);
      m.setAccessible(true);

      HashMap schType = getSchedulingMap();
      String type = null;
      if (schType.get("execType") != null) {
        type = schType.get("execType").toString();
      }

      try {
        data = (String) m.invoke(new Util(), type, schType);
      } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      /* Cleaning up data */
      stmt = etlrepRock.getConnection().createStatement();
      try {
        stmt.executeUpdate("DELETE FROM Meta_collection_sets WHERE VERSION_NUMBER = '((49))'");
        stmt.executeUpdate("DELETE FROM Meta_collections WHERE VERSION_NUMBER = '((49))'");
        stmt.executeUpdate("DELETE FROM Meta_schedulings WHERE VERSION_NUMBER = '((49))'");
      } finally {
        stmt.close();
      }
    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("No Scheduling Type Defined", data);
  }

  private HashMap getSchedulingMap() throws SQLException {

    HashMap tempSchedulingtype = null;
    String packageName = "DWH_MONITOR";

    /* Adding example data to table */

    String sqlStmt = "select S.COLLECTION_SET_ID,S.COLLECTION_SET_NAME,S.VERSION_NUMBER,S.ENABLED_FLAG,C.COLLECTION_ID,"
        + "C.COLLECTION_NAME, C.SETTYPE, C.HOLD_FLAG, C.ENABLED_FLAG, H.SCHEDULING_MONTH, H.SCHEDULING_DAY, H.SCHEDULING_HOUR,"
        + "H.SCHEDULING_MIN, MON_FLAG, TUE_FLAG,WED_FLAG,THU_FLAG,FRI_FLAG,SAT_FLAG,SUN_FLAG,"
        + "H.INTERVAL_HOUR, H.INTERVAL_MIN,H.TRIGGER_COMMAND, H.OS_COMMAND, H.SCHEDULING_YEAR,H.EXECUTION_TYPE, H.LAST_EXECUTION_TIME from "
        + "META_COLLECTION_SETS S, META_COLLECTIONS C left outer join META_SCHEDULINGS H on "
        + "H.COLLECTION_SET_ID = c.COLLECTION_SET_ID and  H.COLLECTION_ID=C.COLLECTION_ID where S.ENABLED_FLAG='Y' "
        + "AND C.ENABLED_FLAG='Y' and C.COLLECTION_SET_ID = S.COLLECTION_SET_ID and C.VERSION_NUMBER = S.VERSION_NUMBER "
        + "and S.COLLECTION_SET_NAME = '" + packageName + "' and C.SETTYPE <> 'Aggregator' order by COLLECTION_NAME";

    String execType = null;
    ResultSet rset = null;

      Statement stmt = etlrepRock.getConnection().createStatement();
      try {
        rset = stmt.executeQuery(sqlStmt);
        try {

          while (rset.next()) {
            tempSchedulingtype = new HashMap();
            tempSchedulingtype.put("schMonth", rset.getString("SCHEDULING_MONTH"));
            tempSchedulingtype.put("schDay", rset.getString("SCHEDULING_DAY"));
            tempSchedulingtype.put("schHour", rset.getString("SCHEDULING_HOUR"));
            tempSchedulingtype.put("schMin", rset.getString("SCHEDULING_MIN"));
            tempSchedulingtype.put("monFlag", rset.getString("MON_FLAG"));
            tempSchedulingtype.put("tueFlag", rset.getString("TUE_FLAG"));
            tempSchedulingtype.put("wedFlag", rset.getString("WED_FLAG"));
            tempSchedulingtype.put("thuFlag", rset.getString("THU_FLAG"));
            tempSchedulingtype.put("friFlag", rset.getString("FRI_FLAG"));
            tempSchedulingtype.put("satFlag", rset.getString("SAT_FLAG"));
            tempSchedulingtype.put("sunFlag", rset.getString("SUN_FLAG"));
            tempSchedulingtype.put("intervalHour", rset.getString("INTERVAL_HOUR"));
            tempSchedulingtype.put("intervalMin", rset.getString("INTERVAL_MIN"));
            tempSchedulingtype.put("triggerComamnd", rset.getString("TRIGGER_COMMAND"));
            tempSchedulingtype.put("osCommand", rset.getString("OS_COMMAND"));
            tempSchedulingtype.put("schYear", rset.getString("SCHEDULING_YEAR"));
            tempSchedulingtype.put("execType", rset.getString("EXECUTION_TYPE"));
          }
        } finally {
          rset.close();
        }
      } finally {
        stmt.close();
      }

    return tempSchedulingtype;

  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.monitor.Util#getTechPacks(java.sql.Connection)}.
   * 
   * @throws NoSuchMethodException
   * @throws SecurityException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testGetTechPacks() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {
    Method m = Util.class.getDeclaredMethod("getTechPacks", Connection.class);
    m.setAccessible(true);
    Vector<String> data = (Vector<String>) m.invoke(new Util(), dwhrepRock.getConnection());
    assertThat(data.size(), is(3));
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.monitor.Util#getActiveNonEventsTechPacks(java.sql.Connection)}.
   * 
   * @throws NoSuchMethodException
   * @throws SecurityException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testGetActiveNonEventsTechPacks() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {
    Method m = Util.class.getDeclaredMethod("getActiveNonEventsTechPacks", Connection.class);
    m.setAccessible(true);
    Vector<String> data = (Vector<String>) m.invoke(new Util(), dwhrepRock.getConnection());
    assertThat(data.size(), is(2));
  }

  /**
   * Test method for
   * {@link com.distocraft.dc5000.etl.gui.monitor.Util#getSetsForPackage(java.sql.Connection, java.lang.String)}.
   * @throws NoSuchMethodException 
   * @throws SecurityException 
   * @throws InvocationTargetException 
   * @throws IllegalAccessException 
   * @throws IllegalArgumentException 
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testGetSetsForPackage() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Method m = Util.class.getDeclaredMethod("getSetsForPackage", Connection.class, String.class);
    m.setAccessible(true);
    String packageName = "TEST_MONITOR";
    Map<String, List<List<String>>> data = (Map<String, List<List<String>>>) m.invoke(new Util(), etlrepRock.getConnection(), packageName);
    assertThat(data.size(), is(1));
  }
  
  @Test
  public void testGetEnabledSet_Day() {
    // Setup test database
    try{
      // Populate
      Statement stmt = etlrepRock.getConnection().createStatement();
      stmt.executeUpdate("insert into LOG_AggregationRules (AGGREGATION, RULEID, TARGET_TYPE, TARGET_LEVEL, SOURCE_TYPE, SOURCE_LEVEL, RULETYPE, AGGREGATIONSCOPE, STATUS, MODIFIED, BHTYPE, TARGET_TABLE, SOURCE_TABLE) values ('DC_E_BSS_CELL_CS_DAYBH_CELL', 9, 'DC_E_BSS_CELL_CS', 'DAYBH', 'DC_E_BSS_CELLBH', 'RANKBH', 'RANKSRC', 'DAY', ' ', null, ' ', 'DC_E_BSS_CELL_CS_DAYBH', 'DC_E_BSS_CELLBH_RANKBH');");
    } catch (Exception e) {
      fail("Exception:"+e.getMessage()+", "+e);
    }
    String aggregation = "DC_E_BSS_CELL_CS_DAYBH_Cell"; // lower case "ell"
    String expected = ""; //expect disabled (not found)
    String result = Util.getEnabledSet(aggregation , etlrepRock.getConnection());
    assertEquals(expected, result);
    aggregation = "DC_E_BSS_CELL_CS_DAYBH_CELL"; // all upper case, expect enabled (should be found)
    result = Util.getEnabledSet(aggregation , etlrepRock.getConnection());
    assertEquals(aggregation, result);
  } //testGetEnabledSet_Day
  
  @Test
  public void testGetEnabledSet_Week() {
    // Setup test database
    try{
      // Populate
      Statement stmt = etlrepRock.getConnection().createStatement();
      stmt.executeUpdate("insert into LOG_AggregationRules (AGGREGATION, RULEID, TARGET_TYPE, TARGET_LEVEL, SOURCE_TYPE, SOURCE_LEVEL, RULETYPE, AGGREGATIONSCOPE, STATUS, MODIFIED, BHTYPE, TARGET_TABLE, SOURCE_TABLE) values ('DC_E_BSS_CELLBH_WEEKRANKBH_CELL_PP0', 1, 'DC_E_BSS_CELLBH', 'RANKBH', 'DC_E_BSS_CELLBH', 'RANKBH', 'RANKBHCLASS', 'WEEK', ' ', null, 'CELL_PP0', 'DC_E_BSS_CELLBH_RANKBH', 'DC_E_BSS_CELLBH_RANKBH');");
    } catch (Exception e) {
      fail("Exception:"+e.getMessage()+", "+e);
    }
    String aggregation = "DC_E_BSS_CELLBH_WEEKRANKBH_Cell_CellTraffic"; // lower case "ell"
    String expected = ""; //expect disabled (not found)
    String result = Util.getEnabledSet(aggregation , etlrepRock.getConnection());
    assertEquals(expected, result);
    aggregation = "DC_E_BSS_CELLBH_WEEKRANKBH_CELL_PP0"; // all upper case, expect enabled (should be found)
    result = Util.getEnabledSet(aggregation , etlrepRock.getConnection());
    assertEquals(aggregation, result);
  } //testGetEnabledSet_Week
  
  @Test
  public void testGetEnabledSet_Month() {
    // Setup test database
    try{
      // Populate
      Statement stmt = etlrepRock.getConnection().createStatement();
      stmt.executeUpdate("insert into LOG_AggregationRules (AGGREGATION, RULEID, TARGET_TYPE, TARGET_LEVEL, SOURCE_TYPE, SOURCE_LEVEL, RULETYPE, AGGREGATIONSCOPE, STATUS, MODIFIED, BHTYPE, TARGET_TABLE, SOURCE_TABLE) values ('DC_E_BSS_CELLBH_MONTHRANKBH_CELL_PP0', 2, 'DC_E_BSS_CELLBH', 'RANKBH', 'DC_E_BSS_CELLBH', 'RANKBH', 'RANKBHCLASS', 'MONTH', ' ', null, 'CELL_PP0', 'DC_E_BSS_CELLBH_RANKBH', 'DC_E_BSS_CELLBH_RANKBH');");
    } catch (Exception e) {
      fail("Exception:"+e.getMessage()+", "+e);
    }
    String aggregation = "DC_E_BSS_CELLBH_MONTHRANKBH_Cell_CellTraffic"; // lower case "ell"
    String expected = ""; //expect disabled (not found)
    String result = Util.getEnabledSet(aggregation , etlrepRock.getConnection());
    assertEquals(expected, result);
    aggregation = "DC_E_BSS_CELLBH_MONTHRANKBH_CELL_PP0"; // all upper case, expect enabled (should be found)
    result = Util.getEnabledSet(aggregation , etlrepRock.getConnection());
    assertEquals(aggregation, result);
  } //testGetEnabledSet_Month
  

}
