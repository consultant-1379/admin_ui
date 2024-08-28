package com.distocraft.dc5000.etl.gui.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.util.Vector;

import javax.naming.Context;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogSystem;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.distocraft.dc5000.etl.gui.util.Helper;

import ssc.rockfactory.RockFactory;

import utils.MemoryDatabaseUtility;


public class DwhMonitoringTest {

  public static final String TEST_APPLICATION = DwhMonitoringTest.class.getName();

  private static RockFactory dwhRock;

  private static RockFactory etlrepRock;

  private static RockFactory dwhrepRock;

  private VelocityContext velocityContext;

  final private Mockery context = new Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  private Context mockContext;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    dwhRock = new RockFactory(MemoryDatabaseUtility.TEST_DWH_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
        MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL dwhsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWH_BASIC);
    if (dwhsqlurl == null) {
      System.out.println("dwh script can not be loaded!");
    } else {
      MemoryDatabaseUtility.loadSetup(dwhRock, dwhsqlurl);
    }
    
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
    if (dwhrepsqlurl == null) {
      System.out.println("dwhrep script can not be loaded!");
    } else {
      MemoryDatabaseUtility.loadSetup(dwhrepRock, dwhrepsqlurl);
    }

    Velocity.setProperty(Velocity.RESOURCE_LOADER, "file");
    Velocity.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
    Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogSystem.class.getName());
    Velocity.init();
  }

  @Before
  public final void setUp() throws Exception {

    velocityContext = new VelocityContext();

    mockContext = context.mock(Context.class, "context");

    Helper.setContext(mockContext);

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    MemoryDatabaseUtility.shutdown(dwhRock);
    MemoryDatabaseUtility.shutdown(etlrepRock);
    MemoryDatabaseUtility.shutdown(dwhrepRock);
  }
  
  @Test
  public void testgetDataRowUseNOkey() throws Exception {
    final Method m = DwhMonitoring.class.getDeclaredMethod("getDataRowUseNOkey", String.class, String.class,
        String.class, Connection.class, Connection.class, boolean.class);
    m.setAccessible(true);
    final Vector<String> data = (Vector<String>) m.invoke(new DwhMonitoring(), "DC_E_RAN_RNC", "RAW", "2011-04-20",
        dwhRock.getConnection(),dwhrepRock.getConnection(), true);
    assertThat(data.size(), is(1));
  }

  @Test
  public void testgetDataRowUseNOkeyEventDirectionTrue() throws Exception {
    final Method m = DwhMonitoring.class.getDeclaredMethod("getDataRowUseNOkey", String.class, String.class,
        String.class, Connection.class,Connection.class, boolean.class);
    m.setAccessible(true);
    final Vector<String> data = (Vector<String>) m.invoke(new DwhMonitoring(), "EVENT_E_USER_PLANE_CLASSIFICATION", "RAW", "2011-04-20 06:00:00.0",
        dwhRock.getConnection(),dwhrepRock.getConnection(),true);
    assertThat(data.size(), is(1));
  }
  
  @Test
  public void testgetDataRowUseNOkeyEventDirectionFalse() throws Exception {
    final Method m = DwhMonitoring.class.getDeclaredMethod("getDataRowUseNOkey", String.class, String.class,
        String.class, Connection.class,Connection.class);
    m.setAccessible(true);
    final Vector<String> data = (Vector<String>) m.invoke(new DwhMonitoring(), "EVENT_E_USER_PLANE_CLASSIFICATION", "RAW", "2011-04-20 06:00:00.0",
        dwhRock.getConnection(),dwhrepRock.getConnection());
    assertThat(data.size(), is(1));
  }

  @Test
  public void testgetDataRowDate() throws Exception {
    final Method m = DwhMonitoring.class.getDeclaredMethod("getDataRowDate", String.class, String.class,
        Connection.class);
    m.setAccessible(true);
    final Vector<String> data = (Vector<String>) m.invoke(new DwhMonitoring(), "DC_E_RAN_RNC_RAW_01", "2011-04-20", 
        dwhRock.getConnection());
    assertThat(data.size(), is(1));
  }

  @Test
  public void testgetDataRowAndKey() throws Exception {

    velocityContext.put("rockDwhRep", dwhrepRock);
    velocityContext.put("rockDwh", dwhRock);
    try {

      final DwhMonitoring Moni = new DwhMonitoring();
      Moni.getDataRowAndKey(velocityContext, "DC_E_RAN_RNC", "DC_E_RAN_RNC", "RAW", "2011-04-05 22:41:55.0", true,
          true, "100");
    } catch (Exception e) {
      final String actual = null;
      assertEquals(e.getMessage(), actual);
    }

  }

  @Test
  public void testgetDataRowAndKeyEvent() throws Exception {

    velocityContext.put("rockDwhRep", dwhrepRock);
    velocityContext.put("rockDwh", dwhRock);
    try {

      final DwhMonitoring Moni = new DwhMonitoring();
      Moni.getDataRowAndKey(velocityContext, "EVENT_E_USER_PLANE", "EVENT_E_USER_PLANE_CLASSIFICATION", "RAW", "2011-04-05 22:41:55.0", true,
          true, "100");
    } catch (Exception e) {
      final String actual = null;
      assertEquals(e.getMessage(), actual);
    }

  }
  
  
  @Test
  public void testgetDateColumn() throws Exception {
      try{
          final DwhMonitoring Moni = new DwhMonitoring();
   Moni.getDateColumn( "DC_E_RAN_RNC", "RAW",
           dwhrepRock.getConnection());
          }catch(Exception e){
              final String actual = null;
              assertEquals(e.getMessage(), actual);
          }
  }

  @Test
  public void testgetDateColumnEvent() throws Exception {
      try{
      final DwhMonitoring Moni = new DwhMonitoring();
   Moni.getDateColumn( "EVENT_E_USER_PLANE_CLASSIFICATION", "RAW",
       dwhrepRock.getConnection());
      }catch(Exception e){
          final String actual = null;
          assertEquals(e.getMessage(), actual);
      }
  }
  
  

  @Test
  public void testgetDataRowDateExtendedInternal() throws Exception {
    try {
      final DwhMonitoring Moni = new DwhMonitoring();
      Moni.getDataRowDateExtendedRange
      ("DC_E_RAN_RNC", "RAW", "2011-04-05 22:41:55.0","2011-04-06 10:00:00.0",
             dwhRock.getConnection(),dwhrepRock.getConnection(),true,"1");
    } catch (Exception e) {
      final String actual = null;
      assertEquals(e.getMessage(), actual);
    }

  }
  
  @Test
  public void testgetDataRowDateExtendedInternalEventDirectionTrue() throws Exception {
    try {
      final DwhMonitoring Moni = new DwhMonitoring();
      Moni.getDataRowDateExtendedRange
      ("EVENT_E_USER_PLANE_CLASSIFICATION", "RAW", "2011-04-05 22:41:55.0","2011-04-20 10:00:00.0",
             dwhRock.getConnection(),dwhrepRock.getConnection(),true,"100");
    } catch (Exception e) {
      final String actual = null;
      assertEquals(e.getMessage(), actual);
    }

  }
  
  @Test
  public void testgetDataRowDateExtendedInternalEventDirectionFalse() throws Exception {
    try {
      final DwhMonitoring Moni = new DwhMonitoring();
      Moni.getDataRowDateExtendedRange
      ("EVENT_E_USER_PLANE_CLASSIFICATION", "RAW", "2011-04-05 22:41:55.0","2011-04-20 10:00:00.0",
             dwhRock.getConnection(),dwhrepRock.getConnection(),false,"100");
    } catch (Exception e) {
      final String actual = null;
      assertEquals(e.getMessage(), actual);
    }

  }
  
  @Test
  public void testgetDataRowDateExtendedRange() throws Exception {
    try {
      final DwhMonitoring Moni = new DwhMonitoring();
      Moni.getDataRowDateExtendedRange
      ("DC_E_RAN_RNC", "RAW", "2011-04-05 22:41:55.0","2011-04-20 10:00:00.0",
             dwhRock.getConnection(),dwhrepRock.getConnection(),true,"100");
    } catch (Exception e) {
      final String actual = null;
      assertEquals(e.getMessage(), actual);
    }

  }
 
  @Test
  public void testgetDataRowDateExtendedRangeEventDirectionTrue() throws Exception {
    try {
      final DwhMonitoring Moni = new DwhMonitoring();
      Moni.getDataRowDateExtendedRange
      ("EVENT_E_USER_PLANE_CLASSIFICATION", "RAW", "2011-04-05 22:41:55.0","2011-04-20 10:00:00.0",
             dwhRock.getConnection(),dwhrepRock.getConnection(),true,"100");
    } catch (Exception e) {
      final String actual = null;
      assertEquals(e.getMessage(), actual);
    }

  }

  @Test
  public void testgetDataRowDateExtendedRangeEventDirectionFalse() throws Exception {
    try {
      final DwhMonitoring Moni = new DwhMonitoring();
      Moni.getDataRowDateExtendedRange
      ("EVENT_E_USER_PLANE_CLASSIFICATION", "RAW", "2011-04-05 22:41:55.0","2011-04-20 10:00:00.0",
             dwhRock.getConnection(),dwhrepRock.getConnection(),false,"100");
    } catch (Exception e) {
      final String actual = null;
      assertEquals(e.getMessage(), actual);
    }

  }
  
  @Test
  public void testgetTypeIdForMeasType() throws Exception {
      velocityContext.put("rockDwhRep", dwhrepRock);
    try {
      final DwhMonitoring Moni = new DwhMonitoring();
      Moni.getTypeIdForMeasType
      (velocityContext, "RAW");
    } catch (Exception e) {
      final String actual = null;
      assertEquals(e.getMessage(), actual);
    }

  }

  
}
