package com.distocraft.dc5000.etl.gui.etl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Vector;

import org.apache.velocity.context.Context;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogSystem;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import org.jmock.lib.legacy.ClassImposteriser;
import org.jmock.Mockery;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ssc.rockfactory.RockFactory;
import utils.MemoryDatabaseUtility;

public class ETLSessionLogTest {

  public static final String TEST_APPLICATION = ETLSessionLogTest.class.getName();

  private static RockFactory rockDwh;

  private static final String TEST_VIEW_VM = "C:\\Users\\Public\\etl_little_logs_adapter.vm";

  Mockery context = new Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  private VelocityContext velocityContext = new VelocityContext();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    rockDwh = new RockFactory(MemoryDatabaseUtility.TEST_DWH_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
        MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL dwhsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWH_BASIC);
    if (dwhsqlurl == null) {
      System.out.println("dwh script can not be loaded!");
    } else {
      MemoryDatabaseUtility.loadSetup(rockDwh, dwhsqlurl);
    }
    // Template outty = null;
    // outty = getTemplate("etl_little_logs_adapter.vm");

    final FileWriter fwriter = new FileWriter(TEST_VIEW_VM);
    try {
      final PrintWriter out = new PrintWriter(fwriter);
      try {
        out.println("<html></html>");
      } finally {
        out.close();
      }
    } finally {
      fwriter.close();
    }

    Velocity.setProperty(Velocity.RESOURCE_LOADER, "file");
    Velocity.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
    Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogSystem.class.getName());
    Velocity.init();

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    final File f = new File(TEST_VIEW_VM);
    if (!f.delete()) {
      fail("failed to remove test template file");
    }
    MemoryDatabaseUtility.shutdown(rockDwh);
  }

  @Test
  public void testgetEtlAdapterRowDetails() throws Exception {
    try {
      Method m = ETLSessionLog.class.getDeclaredMethod("getEtlAdapterRowDetails", Context.class, String.class,
          String.class);
      m.setAccessible(true);
      velocityContext.put("rockDwh", rockDwh);
      Vector<String> data = (Vector<String>) m.invoke(new ETLSessionLog(), velocityContext, "3", "2");
      assertThat(data.size(), is(1));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testgetMZLoggerRowDetails() throws Exception {
    try {
      Method m = ETLSessionLog.class.getDeclaredMethod("getMZLoggerRowDetails", Context.class, String.class,
          String.class, String.class, String.class);
      m.setAccessible(true);
      velocityContext.put("rockDwh", rockDwh);
      Vector<String> data = (Vector<String>) m.invoke(new ETLSessionLog(), velocityContext, "2", "1", "3", "WF02_Processing");
      assertThat(data.size(), is(1));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testgetEtlLoggerRowDetails() throws Exception {
    try {
      Method m = ETLSessionLog.class.getDeclaredMethod("getEtlLoggerRowDetails", Context.class, String.class,
          String.class);
      m.setAccessible(true);
      velocityContext.put("rockDwh", rockDwh);
      Vector<String> data = (Vector<String>) m.invoke(new ETLSessionLog(), velocityContext, "2", "1");
      assertThat(data.size(), is(1));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

}
