package com.distocraft.dc5000.etl.gui.dataflow;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat; 

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;

import javax.naming.Context;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogSystem;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jmock.*;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.distocraft.dc5000.etl.gui.util.Helper;

import ssc.rockfactory.RockFactory;
import utils.MemoryDatabaseUtility;

public class DataRowInfoTest {

  public static final String TEST_APPLICATION = DataRowInfoTest.class.getName();

  private static final String TEST_VIEW_VM = "datarowinfo.vm";

  private static MockHttpServletRequest webmockRequest;

  private static MockHttpSession webmockSession;

  private MockHttpServletResponse webmockResponse;

  private VelocityContext velocityContext;

  final private Mockery context = new Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  private static RockFactory dwhrepRock;

  private static RockFactory rockDwh;

  private Context mockContext;

  @BeforeClass
  public final static void setUpBeforeClass() throws java.lang.Exception {

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

    final File ETLCConfFile = new File(System.getProperty("user.dir"), "ETLCServer.properties");
    ETLCConfFile.deleteOnExit();
    try {
      final PrintWriter pw = new PrintWriter(new FileWriter(ETLCConfFile));
      pw.println("ENGINE_DB_URL=" + MemoryDatabaseUtility.TEST_ETLREP_URL);
      pw.println("ENGINE_DB_USERNAME=" + MemoryDatabaseUtility.TESTDB_USERNAME);
      pw.println("ENGINE_DB_PASSWORD=" + MemoryDatabaseUtility.TESTDB_PASSWORD);
      pw.println("ENGINE_DB_DRIVERNAME=" + MemoryDatabaseUtility.TESTDB_DRIVER);
      pw.println("ENGINE_HOSTNAME=localhost");
      pw.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.setProperty("CONF_DIR", System.getProperty("user.dir"));

    Velocity.setProperty(Velocity.RESOURCE_LOADER, "file");
    Velocity.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
    Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogSystem.class.getName());
    Velocity.init();

    dwhrepRock = new RockFactory(MemoryDatabaseUtility.TEST_DWHREP_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
        MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL dwhrepsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWHREP_BASIC);
    if (dwhrepsqlurl == null) {
      System.out.println("dwhrep script can not be loaded!");
    } else {
      MemoryDatabaseUtility.loadSetup(dwhrepRock, dwhrepsqlurl);
    }

    rockDwh = new RockFactory(MemoryDatabaseUtility.TEST_DWH_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
        MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL dwhsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_ETLREP_BASIC);
    if (dwhsqlurl == null) {
      System.out.println("dwh script can not be loaded!");
    } else {
      MemoryDatabaseUtility.loadSetup(rockDwh, dwhsqlurl);
    }
  }

  @Before
  public final void setUp() throws Exception {

    webmockSession = new MockHttpSession(null);

    webmockRequest = new MockHttpServletRequest();

    webmockRequest.setSession(webmockSession);

    webmockResponse = new MockHttpServletResponse();

    velocityContext = new VelocityContext();

    mockContext = context.mock(Context.class, "context");

    Helper.setContext(mockContext);

  }

  @AfterClass
  public final static void tearDownAfterClass() throws java.lang.Exception {

    final File f = new File(TEST_VIEW_VM);
    if (!f.delete()) {
      fail("failed to remove test template file");
    }

    try {
      MemoryDatabaseUtility.shutdown(dwhrepRock);
      MemoryDatabaseUtility.shutdown(rockDwh);
    }

    catch (Exception e) {
      fail(e.getMessage());
    }

  }

  @Test
  public void testDoHandleRequest1() throws Exception {

    webmockRequest = new MockHttpServletRequest("GET", "/servlet/DataRowInfo");
    webmockRequest.setParameter("year_1", "2011");
    webmockRequest.setParameter("month_1", "04");
    webmockRequest.setParameter("day_1", "05");
    webmockRequest.setParameter("tp", "DC_E_BSS");

    webmockRequest.setSession(webmockSession);

    velocityContext.put("rockDwhRep", dwhrepRock);
    velocityContext.put("rockDwh", rockDwh);
    final DataRowInfo servlet = new DataRowInfo();

    final Template template = servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
    assertNotNull(template);

  }
  
  @Test
  public void testDoHandleRequest2() throws Exception {

    webmockRequest = new MockHttpServletRequest("GET", "/servlet/DataRowInfo");
    webmockRequest.setParameter("year_1", "2011");
    webmockRequest.setParameter("month_1", "04");
    webmockRequest.setParameter("day_1", "05");
    webmockRequest.setParameter("tp", "DC_E_BSS");
    webmockRequest.setParameter("search_direction", "forward");
    webmockRequest.setParameter("dtype", "Measurement");

    webmockRequest.setSession(webmockSession);

    velocityContext.put("rockDwhRep", dwhrepRock);
    velocityContext.put("rockDwh", rockDwh);
    final DataRowInfo servlet = new DataRowInfo();

    final Template template = servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
    assertNotNull(template);

  }
  
  @Test
  public void testDoHandleRequest3() throws Exception {

    webmockRequest = new MockHttpServletRequest("GET", "/servlet/DataRowInfo");
    webmockRequest.setSession(webmockSession);

    velocityContext.put("rockDwhRep", dwhrepRock);
    velocityContext.put("rockDwh", rockDwh);
    final DataRowInfo servlet = new DataRowInfo();

    final Template template = servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
    assertNotNull(template);

  }

@Test
  public void testSetContextOnForwardSearchDirection() throws Exception {

   final String searchdirection1 = "forward";
   final DataRowInfo servlet1 = new DataRowInfo();
   servlet1.setContextOnSearchDirection(velocityContext, searchdirection1);
   String testStr=(String) velocityContext.get("forward");
   assertThat(testStr,is( "checked"));
  }
  
  @Test
  public void testSetContextOnBackwardSearchDirection() throws Exception {

   final String searchdirection1 = "backward";
   final DataRowInfo servlet1 = new DataRowInfo();
   servlet1.setContextOnSearchDirection(velocityContext, searchdirection1);
   String testStr=(String) velocityContext.get("backward");
   assertThat(testStr,is( "checked"));
  }
  
  
}

