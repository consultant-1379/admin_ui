package com.distocraft.dc5000.etl.gui.common;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;

import javax.naming.Context;

import javax.servlet.http.HttpSession;

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
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;

import com.distocraft.dc5000.etl.gui.DbConnectionCloser;
import com.distocraft.dc5000.etl.gui.util.Helper;
import com.sun.security.auth.UserPrincipal;

import ssc.rockfactory.RockFactory;
import utils.MemoryDatabaseUtility;

@Ignore
public abstract class EtlguiServletTest {

  public static final String TEST_APPLICATION = EtlguiServletTest.class.getName();

  static final String TEST_VIEW_VM = "etlguiservlet.vm";

  private static final String ERROR_PAGE_VIEW_VM = "adminuiErrorPage.vm";

  private static MockHttpServletRequest webmockRequest;

  private static MockHttpSession webmockSession;

  private MockHttpServletResponse webmockResponse;

  private MockServletConfig webmockConfig;

  private VelocityContext velocityContext;

  public DbConnectionCloser mockDbConnectionCloser;

  private Mockery context = new Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  abstract EtlguiServlet getServlet();

  private static RockFactory etlrepRock;

  private Context mockContext;

  private Context mockSubContext;

  private UserPrincipal userPrincipal;
  
  @BeforeClass
  public final static void setUpBeforeClass() throws java.lang.Exception {

    final FileWriter fwriter = new FileWriter(TEST_VIEW_VM);
    try {
      final PrintWriter out = new PrintWriter(fwriter);
      try {
        out.println("<html>$currenttime</html>");
      } finally {
        out.close();
      }
    } finally {
      fwriter.close();
    }

    final FileWriter fw = new FileWriter(ERROR_PAGE_VIEW_VM);
    try {
      final PrintWriter out = new PrintWriter(fw);
      try {
        out.println("<html></html>");
      } finally {
        out.close();
      }
    } finally {
      fwriter.close();
    }

    /* Creating ETLC property file */
    // final String folderName = System.getProperty("user.dir") + File.separator;
    // final String fileName = folderName + "ETLCServer.properties";
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

    /* Creating ETLC property file */
    // final String folderName = System.getProperty("user.dir") + File.separator;
    // final String fileName = folderName + "ETLCServer.properties";
    final File adminuiPropertiesFile = new File(System.getProperty("user.dir"), "adminui.properties");
    adminuiPropertiesFile.deleteOnExit();
    try {
      final PrintWriter pw = new PrintWriter(new FileWriter(adminuiPropertiesFile));
      pw.println("log4j.rootLogger=DEBUG, A1");
      pw.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.setProperty("CONF_DIR", System.getProperty("user.dir"));

    Velocity.setProperty(Velocity.RESOURCE_LOADER, "file");
    Velocity.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
    Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogSystem.class.getName());
    Velocity.init();

    etlrepRock = new RockFactory(MemoryDatabaseUtility.TEST_ETLREP_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
        MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL etlrepsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_ETLREP_BASIC);
    if (etlrepsqlurl == null) {
      System.out.println("etlrep script can not be loaded!");
    } else {
      MemoryDatabaseUtility.loadSetup(etlrepRock, etlrepsqlurl);
    }
  }

  @Before
  public final void setUp() throws Exception {

    webmockSession = new MockHttpSession(null);
    
    userPrincipal = new UserPrincipal("eniq");
    

    // webmockRequest = new MockHttpServletRequest("GET", "/servlet/EtlguiServlet");
    webmockRequest = new MockHttpServletRequest();

    webmockRequest.setSession(webmockSession);

    webmockRequest.setUserPrincipal(userPrincipal);
    
    webmockResponse = new MockHttpServletResponse();

    velocityContext = new VelocityContext();

    webmockConfig = new MockServletConfig();

    mockDbConnectionCloser = context.mock(DbConnectionCloser.class);

    mockContext = context.mock(Context.class, "context");

    mockSubContext = context.mock(Context.class, "subcontext");

    Helper.setContext(mockContext);

    DbConnectionCloser.setInstance(mockDbConnectionCloser);

    /* EtlguiServlet.statusQuery = "CALL CURDATE()"; */

  }

  @After
  public final void tearDown() throws Exception {

    DbConnectionCloser.setInstance(null);

  }

  @AfterClass
  public final static void tearDownAfterClass() throws java.lang.Exception {

    final File file = new File(TEST_VIEW_VM);
    if (!file.delete()) {
      fail("failed to remove test template file");
    }

    final File file2 = new File(ERROR_PAGE_VIEW_VM);
    if (!file2.delete()) {
      fail("failed to remove test template file");
    }

    try {
      MemoryDatabaseUtility.shutdown(etlrepRock);
    } catch (Exception e) {
      fail(e.getMessage());
    }

  }

  @Test
  public void testDoHandleRequest() throws Exception {

    context.checking(new Expectations() {

      {
        allowing(mockDbConnectionCloser).addRockFactory(with(any(HttpSession.class)), with(any(RockFactory.class)), with(any(String.class)));
        allowing(mockContext).lookup(with(any(String.class)));
        will(returnValue(mockSubContext));
        allowing(mockSubContext).lookup("conffiles");
        will(returnValue("."));
        allowing(mockSubContext).lookup("dbConnectionTimeout");
        will(returnValue("900000"));
        allowing(mockSubContext).lookup("dbConnectionTestPeriod");
        will(returnValue("300000"));

      }
    });
    EtlguiServlet servlet = getServlet();
    servlet.loadConfiguration(webmockConfig);
    Template template = servlet.handleRequest(webmockRequest, webmockResponse, velocityContext);
    assertThat(template, notNullValue());
    assertThat(velocityContext.get("environment"), notNullValue());
    assertThat(velocityContext.get("currenttime"), notNullValue());
    assertThat(velocityContext.get("theuser"), notNullValue());

  }

}
