package com.distocraft.dc5000.etl.gui.aggregation;

import com.distocraft.dc5000.etl.gui.util.ParamValue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.Vector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ssc.rockfactory.RockFactory;
import utils.MemoryDatabaseUtility;

import com.distocraft.dc5000.etl.gui.monitor.UtilTest;

public class ReAggregationServletTest {

  public static final String TEST_APPLICATION = UtilTest.class.getName();

  private static RockFactory dwhrepRock;

  private static RockFactory dwhRock;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    try {

      dwhrepRock = new RockFactory(MemoryDatabaseUtility.TEST_DWHREP_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
          MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
      final URL dwhrepsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWHREP_BASIC);
      if (dwhrepsqlurl == null) {
        System.out.println("dwhrep script can not be loaded!");
      } else {
        MemoryDatabaseUtility.loadSetup(dwhrepRock, dwhrepsqlurl);
      }

      dwhRock = new RockFactory(MemoryDatabaseUtility.TEST_DWH_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
          MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
      final URL dwhsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWH_BASIC);
      if (dwhsqlurl == null) {
        System.out.println("dwh script can not be loaded!");
      } else {
        MemoryDatabaseUtility.loadSetup(dwhRock, dwhsqlurl);
      }

    }

    catch (Exception e) {
      fail(e.getMessage());
    }

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {

    try {
      MemoryDatabaseUtility.shutdown(dwhRock);
      MemoryDatabaseUtility.shutdown(dwhrepRock);
    }

    catch (Exception e) {
      fail(e.getMessage());
    }

  }

  @Test
  public void testparseValue() throws Exception {

    final Method m = ReAggregationServlet.class.getDeclaredMethod("parseValue", boolean.class, String.class);
    m.setAccessible(true);
    final String expected = "sentence";
    final String data = (String) m.invoke(new ReAggregationServlet(), false, "Test&sentence");
    assertEquals(expected, data);
  }

  @Test
  public void testgetAggregationScope() throws Exception {
    final ReAggregationServlet reserv = new ReAggregationServlet();
    final Vector<String> data = (Vector<String>) reserv.getAggregationScope(dwhrepRock.getConnection());
    assertThat(data.size(), is(2));
  }

  @Test
  public void testgetTechPacks() throws Exception {

    final Method m = ReAggregationServlet.class.getDeclaredMethod("getTechPacks", Connection.class, String.class);
    m.setAccessible(true);
    final Vector<String> data = (Vector<String>) m
        .invoke(new ReAggregationServlet(), dwhrepRock.getConnection(), "DAY");
    assertThat(data.size(), is(0));
  }

  @Test
  public void testgetAggregationNames() throws Exception {
    List<ParamValue> agg_names = new Vector<ParamValue>();
    final ReAggregationServlet reserv = new ReAggregationServlet();
    agg_names = reserv.getAggregationNames(dwhrepRock.getConnection(), "DAY", "DC_E_BSS:((18))");
    assertThat(agg_names.size(), is(1));
  }

  /*
   * @Test public void testgetAggregations() throws Exception { String Value1 = "DC_E_BSS_ATERTRANS_DAY"; String Value2
   * = "DC_E_BSS_LAPD_DAY"; Vector<ParamValue> pv = new Vector<ParamValue>(); ParamValue v = new ParamValue();
   * v.setParam(Value1.toString()); v.setValue(Value2.toString()); pv.add(v);
   * 
   * DbCalendar db = null; String start_date = "2011-03-01"; String end_date = "2011-04-21";
   * 
   * Vector<ParamValue> agg_names = new Vector<ParamValue>(); ReAggregationServlet reserv = new ReAggregationServlet();
   * agg_names = reserv.getAggregations(dwhRock.getConnection(), pv, db = new DbCalendar(start_date), db = new
   * DbCalendar(end_date)); //assertThat(agg_names.size(), is(1)); }
   */

  @Test
  public void testcreateZeroTimeCalendar() throws Exception {
    final Method m = ReAggregationServlet.class.getDeclaredMethod("createZeroTimeCalendar", String.class, String.class,
        String.class);
    m.setAccessible(true);
    m.invoke(new ReAggregationServlet(), "4", "4", "2011");
  }

}
