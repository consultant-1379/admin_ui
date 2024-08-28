package com.distocraft.dc5000.etl.gui.systemmonitor;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;

import junit.framework.Assert;

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
import org.springframework.mock.web.MockServletConfig;

import com.distocraft.dc5000.etl.gui.DbConnectionCloser;
import com.distocraft.dc5000.etl.gui.util.Helper;
import com.ericsson.eniq.licensing.cache.DefaultLicensingCache;
import com.ericsson.eniq.licensing.cache.LicensingCache;

import ssc.rockfactory.RockFactory;
import utils.MemoryDatabaseUtility;


public class ShowInstalledLicensesTest {
	
  private static final long serialVersionUID = 1L;

  public static final String TEST_APPLICATION = ShowInstalledLicensesTest.class.getName();
  
  private static final String TEST_VIEW_VM = "show_installed_licenses.vm";

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

//  abstract EtlguiServlet getServlet();

  private static RockFactory etlrepRock;

  private Context mockContext;

  private Context mockSubContext;

  @BeforeClass
  public final static void setUpBeforeClass() throws Exception {

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

    /* Creating ETLC property file */
    String folderName = System.getProperty("user.home") + File.separator;
    final String fileName = folderName + "ETLCServer.properties";
    final File ETLCConfFile = new File(fileName);
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

    System.setProperty("CONF_DIR", folderName);

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

    // webmockRequest = new MockHttpServletRequest("GET", "/servlet/EtlguiServlet");
    webmockRequest = new MockHttpServletRequest();

    webmockRequest.setSession(webmockSession);

    webmockResponse = new MockHttpServletResponse();

    velocityContext = new VelocityContext();

    webmockConfig = new MockServletConfig();

    mockDbConnectionCloser = context.mock(DbConnectionCloser.class);

    mockContext = context.mock(Context.class, "context");

    mockSubContext = context.mock(Context.class, "subcontext");
    
    Helper.setContext(mockContext);
   
    DbConnectionCloser.setInstance(mockDbConnectionCloser);    
    


  }

  @After
  public final void tearDown() throws Exception {

    DbConnectionCloser.setInstance(null);    

  }

  @AfterClass
  public final static void tearDownAfterClass() throws Exception {

    final File f = new File(TEST_VIEW_VM);
    if (!f.delete()) {
      fail("failed to remove test template file");
    }
    
	try {
		MemoryDatabaseUtility.shutdown(etlrepRock);
		
	}
	
	catch(Exception e){
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
    
    
    //DbConnectionCloser.dbConnectionCloser = DbConnectionCloser.getInstance(Helper.getEnvEntryInt("dbConnectionTimeout"),Helper.getEnvEntryInt("dbConnectionTestPeriod"));
    ShowInstalledLicenses servlet = new ShowInstalledLicenses();
    servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
      
      
    
  }

}
