package com.distocraft.dc5000.etl.gui.etl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;

import ssc.rockfactory.RockFactory;
import utils.MemoryDatabaseUtility;

import java.io.File;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Properties;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author eanubda
 */
public class EditLoggingTest {

  private static RockFactory etlrepRock;

  public static final String TEST_APPLICATION = ShowHistorySetTest.class.getName();

  @BeforeClass
  public static void setUp() throws java.lang.Exception {

    etlrepRock = new RockFactory(MemoryDatabaseUtility.TEST_ETLREP_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
        MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL etlrepsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_ETLREP_BASIC);
    if (etlrepsqlurl == null) {
      System.out.println("etlrep script can not be loaded!");
    } else {
      MemoryDatabaseUtility.loadSetup(etlrepRock, etlrepsqlurl);
    }
  }

  @AfterClass
  public static void tearDownAfterClass() throws java.lang.Exception {
    MemoryDatabaseUtility.shutdown(etlrepRock);
  }

  @Test
  public void testSaveAndTestPropertyFile() {

    try {

      final Properties prop = new Properties();
      final String propFile = "C:\\Users\\Public\\engineLogging.properties";
      final File engineConf = new File(propFile);

      final Method m1 = EditLogging.class.getDeclaredMethod("savePropertyFile", Properties.class, File.class);
      m1.setAccessible(true);
      m1.invoke(new EditLogging(), prop, engineConf);

      final Method m2 = EditLogging.class.getDeclaredMethod("getProperties", File.class);
      m2.setAccessible(true);
      m2.invoke(new EditLogging(), engineConf);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /*
   * @Test public void testGetLoggingLevels() throws Exception{
   * 
   * try { ArrayList actual = new ArrayList(); final Method m = EditLogging.class.getDeclaredMethod("getLoggingLevels");
   * m.setAccessible(true); actual = (ArrayList)m.invoke(new EditLogging());
   * 
   * final ArrayList<String> expected = new ArrayList<String>(); expected.add("SEVERE"); expected.add("WARNING");
   * expected.add("INFO"); expected.add("CONFIG"); expected.add("FINE"); expected.add("FINER"); expected.add("FINEST");
   * 
   * assertEquals(expected, actual); } catch (Exception e) { e.printStackTrace(); } }
   */

  @Test
  public void testgetLoggingLevels() throws Exception {
    final Method m = EditLogging.class.getDeclaredMethod("getLoggingLevels");
    m.setAccessible(true);
    final ArrayList<String> data = (ArrayList<String>) m.invoke(new EditLogging());
    assertThat(data.size(), is(7));
  }

  /*
   * @Test public void testgetDefLoggingLevels() throws Exception{
   * 
   * try { ArrayList<String> actual = new ArrayList<String>(); final Method m =
   * EditLogging.class.getDeclaredMethod("getDefLoggingLevels"); m.setAccessible(true); actual = (ArrayList)m.invoke(new
   * EditLogging());
   * 
   * final ArrayList<String> expected = new ArrayList<String>(); expected.add("SEVERE"); expected.add("WARNING");
   * expected.add("INFO"); expected.add("CONFIG");
   * 
   * assertEquals(expected, actual); } catch (Exception e) { e.printStackTrace(); } }
   */

  @Test
  public void testgetDefLoggingLevels() throws Exception {
    final Method m = EditLogging.class.getDeclaredMethod("getDefLoggingLevels");
    m.setAccessible(true);
    final ArrayList<String> data = (ArrayList<String>) m.invoke(new EditLogging());
    assertThat(data.size(), is(4));
  }

}
