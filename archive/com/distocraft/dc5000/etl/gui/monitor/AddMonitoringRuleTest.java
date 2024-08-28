package com.distocraft.dc5000.etl.gui.monitor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;

import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.naming.Context;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
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

public class AddMonitoringRuleTest {
	
	public static final String TEST_APPLICATION = AddMonitoringRule.class.getName();
	
	private static final String TEST_VIEW_VM = "add_monitoring_rule.vm";

	private static RockFactory dwhrepRock;
	
	private static MockHttpServletRequest webmockRequest;

	private static MockHttpSession webmockSession;

	private MockHttpServletResponse webmockResponse;

	private VelocityContext velocityContext;
	  
	final private Mockery context = new Mockery() {

	    {
	      setImposteriser(ClassImposteriser.INSTANCE);
	    }
	  };

	private Context mockContext;

	private Context mockSubContext;

	public static Connection con =null;
	
	public static Statement stmt;
	
	private static RockFactory dwhRock;
	
@BeforeClass
public static void setUpBeforeClass() throws Exception {
	
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
    final String folderName = System.getProperty("user.home") + File.separator;
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
    Velocity.init();
	
	final String TEST_APPLICATION = AddMonitoringRule.class.getName();
	
	dwhRock = new RockFactory(MemoryDatabaseUtility.TEST_DWH_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
            MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL dwhsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWH_BASIC);
    if (dwhsqlurl == null) {
        System.out.println("dwh script can not be loaded!");
    } else {
        MemoryDatabaseUtility.loadSetup(dwhRock, dwhsqlurl);
    }
    
    dwhrepRock = new RockFactory(MemoryDatabaseUtility.TEST_DWHREP_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
            MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL dwhrepsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWHREP_BASIC);
    if (dwhrepsqlurl == null) {
        System.out.println("dwh script can not be loaded!");
    } else {
        MemoryDatabaseUtility.loadSetup(dwhrepRock, dwhrepsqlurl);
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

  mockSubContext = context.mock(Context.class, "subcontext");
  
  Helper.setContext(mockContext);
 
}

@AfterClass
public static void tearDownAfterClass() throws Exception {
	
	final File f = new File(TEST_VIEW_VM);
    if (!f.delete()) {
      fail("failed to remove test template file");
    }
	
	try {
		MemoryDatabaseUtility.shutdown(dwhRock);
		MemoryDatabaseUtility.shutdown(dwhrepRock);
		}
	catch(Exception e){
		fail(e.getMessage());
	}
}

@Test
public void testcheckIfmonitoringRuleExists() throws Exception {
  final Method m = AddMonitoringRule.class.getDeclaredMethod("checkIfmonitoringRuleExists", Connection.class, String.class, String.class);
  m.setAccessible(true);
  final Boolean actual = (Boolean)m.invoke(new AddMonitoringRule(), dwhRock.getConnection(), "DC_E_BSS_ATERTRANS", "15MIN");
  assertEquals(true, actual);
}

@Test
public void testgetMonitoringRule() throws Exception {
  final Method m = AddMonitoringRule.class.getDeclaredMethod("getMonitoringRule", Connection.class, String.class, String.class);
  m.setAccessible(true);
  final Map data = (Map) m.invoke(new AddMonitoringRule(), dwhRock.getConnection(), "DC_E_BSS_ATERTRANS", "15MIN");
  assertThat(data.size(), is(4));
}

@Test
public void testgetMonitoringRules() throws Exception {
  final Method m = AddMonitoringRule.class.getDeclaredMethod("getMonitoringRules", Connection.class, String.class);
  m.setAccessible(true);
  final List<String> data = (List<String>) m.invoke(new AddMonitoringRule(), dwhRock.getConnection(), "DC_E_BSS");
  assertThat(data.size(), is(2));
}

@Test
public void testdeleteMonitoringRule() throws Exception {
  try {
  final Method m = AddMonitoringRule.class.getDeclaredMethod("deleteMonitoringRule", Connection.class, String.class, String.class, String.class);
  m.setAccessible(true);
  m.invoke(new AddMonitoringRule(), dwhRock.getConnection(), "DC_E_SGSN", "DC_E_SGSN_ATM", "15MIN");
  }
  catch (Exception e) {
	  assertEquals(e.getMessage(), null);
  }
}

@Test
public void testDoHandleRequest() throws Exception {

  context.checking(new Expectations() {
    {
      allowing(mockContext).lookup(with(any(String.class)));
      will(returnValue(mockSubContext));
      allowing(mockSubContext).lookup("conffiles");
      will(returnValue("."));
    }
  });
  
  webmockRequest = new MockHttpServletRequest("GET", "/servlet/MonitoringRule");
  webmockRequest.setParameter("tp", "DC_E_BSS");
  webmockRequest.setParameter("type", "DC_E_BSS_ATERTRANS");
  webmockRequest.setParameter("add", "add");
  webmockRequest.setParameter("timelevel", "15MIN");
  velocityContext.put("rockDwhRep",dwhrepRock);
  velocityContext.put("rockDwh",dwhRock);
  try {
     
    final AddMonitoringRule servlet = new AddMonitoringRule();
    servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
  }
  catch (Exception e) {
  	final String actual = null;
    assertEquals(e.getMessage(), actual);
  }
  
}

@Test
public void testDoHandleRequest1() throws Exception {

  context.checking(new Expectations() {
    {
      allowing(mockContext).lookup(with(any(String.class)));
      will(returnValue(mockSubContext));
      allowing(mockSubContext).lookup("conffiles");
      will(returnValue("."));
    }
  });
  
  webmockRequest = new MockHttpServletRequest("GET", "/servlet/AddMonitoringRule");
  webmockRequest.setParameter("tp", "DC_E_BSS");
  webmockRequest.setParameter("add", "add");
  webmockRequest.setParameter("save", "save");
  webmockRequest.setParameter("delete", "delete");
  velocityContext.put("rockDwhRep",dwhrepRock);
  velocityContext.put("rockDwh",dwhRock);
  try {
     
    final AddMonitoringRule servlet = new AddMonitoringRule();
    servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
  }
  catch (Exception e) {
  	final String actual = null;
    assertEquals(e.getMessage(), actual);
  }
  
}

}