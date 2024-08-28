package com.distocraft.dc5000.etl.gui.activation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
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

public class TpActivationTest {

  public static final String TEST_APPLICATION = TpActivationTest.class.getName();

  private static RockFactory dwhrepRock;

  private static RockFactory dwhRock;

  @BeforeClass
  public static void setUpBeforeClass() throws java.lang.Exception {

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
  public static void tearDownAfterClass() throws java.lang.Exception {

    try {
      MemoryDatabaseUtility.shutdown(dwhrepRock);
      MemoryDatabaseUtility.shutdown(dwhRock);
    }

    catch (Exception e) {
      fail(e.getMessage());
    }

  }

  @Test
  public void testupdateTypeActivationStatus() throws Exception {

    final Method m = TPActivation.class.getDeclaredMethod("updateTypeActivationStatus", Connection.class, String.class,
        String.class, String.class);
    m.setAccessible(true);
    final String data = (String) m.invoke(new TPActivation(), dwhrepRock.getConnection(), "LOG_SESSION_ADAPTER","PLAIN", "INACTIVE");
    assertEquals(data,null);
  }

   @Test
   public void testgetTablelevelTypeActivation() throws Exception {
	
	 final Method m = TPActivation.class.getDeclaredMethod("getTablelevelTypeActivation", RockFactory.class, String.class);
	 m.setAccessible(true);
	 final List<String> data = (List<String>) m.invoke(new TPActivation(), dwhrepRock, "PLAIN");
	 assertThat(data.size(), is(2));
   }
  
  @Test
  public void testgetTechPackTablelevelTypeActivation() throws Exception {
  
	final Method m = TPActivation.class.getDeclaredMethod("getTechPackTablelevelTypeActivation", RockFactory.class, String.class, String.class);
	m.setAccessible(true);
	final List<String> data = (List<String>) m.invoke(new TPActivation(), dwhrepRock, "DWH_MONITOR", "PLAIN");
	assertThat(data.size(), is(2));
   }
  
  @Test
   public void testgetTPActivation() throws Exception {
  
	final Method m = TPActivation.class.getDeclaredMethod("getTPActivation", RockFactory.class, String.class);
	m.setAccessible(true);
	final Vector<String> data = (Vector<String>)m.invoke(new TPActivation(), dwhrepRock, "DWH_MONITOR");
	assertEquals(data,null);
  }
  
  @Test
  public void testgetTypeActivation() throws Exception {
 
	final Method m = TPActivation.class.getDeclaredMethod("getTypeActivation", RockFactory.class, String.class);
	m.setAccessible(true);
	final List<String> data = (List<String>)m.invoke(new TPActivation(), dwhrepRock, "DWH_MONITOR");
	assertThat(data.size(), is(2));
 } 
  
   @Test
   public void testgetPartitionplan() throws Exception {
	   
	  final Method m = TPActivation.class.getDeclaredMethod("getPartitionplan", RockFactory.class, String.class);
	  m.setAccessible(true);
	  m.invoke(new TPActivation(), dwhrepRock, "extralarge_plain");
  }
   
   @Test
   public void testgetPartitionplanElse() throws Exception {
	   
	  final Method m = TPActivation.class.getDeclaredMethod("getPartitionplan", RockFactory.class, String.class);
	  m.setAccessible(true);
	  m.invoke(new TPActivation(), dwhrepRock, "extralarge");
  }

}