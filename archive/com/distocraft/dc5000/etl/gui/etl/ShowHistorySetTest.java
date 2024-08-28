package com.distocraft.dc5000.etl.gui.etl;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.*;


import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;

import javax.naming.Context;
import org.apache.velocity.Template;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogSystem;
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

public class ShowHistorySetTest {

	
	public static final String TEST_APPLICATION = ShowHistorySetTest.class.getName();
	
	private static RockFactory etlrepRock;
	
	  private static final String TEST_VIEW_VM = "etlShowHistory.vm";

	  private static MockHttpServletRequest webmockRequest;

	  private static MockHttpSession webmockSession;

	  private MockHttpServletResponse webmockResponse;

	  private VelocityContext velocityContext;
	  
	  private Mockery context = new Mockery() {

	    {
	      setImposteriser(ClassImposteriser.INSTANCE);
	    }
	  };

  
	  public static RockFactory rockEtlRep;
	  
	  public static RockFactory rockDwh;
	  
	  private Context mockContext;

	  private Context mockSubContext;

	
	
@BeforeClass
public static void setUpBeforeClass() throws java.lang.Exception {
	
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

   rockEtlRep = new RockFactory(MemoryDatabaseUtility.TEST_ETLREP_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
        MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL etlrepsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_ETLREP_BASIC);
    if (etlrepsqlurl == null) {
      System.out.println("etlrep script can not be loaded!");
    } else {
      MemoryDatabaseUtility.loadSetup(rockEtlRep, etlrepsqlurl);
    }
    
    rockDwh = new RockFactory(MemoryDatabaseUtility.TEST_DWH_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
            MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
   	final URL dwhsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWH_BASIC);
   	if (dwhsqlurl == null) {
   	  System.out.println("dwh script can not be loaded!");
   	  } else {
   	    MemoryDatabaseUtility.loadSetup(rockDwh, dwhsqlurl);
   	}
      
}

@AfterClass
public static void tearDownAfterClass() throws java.lang.Exception {
	
 
  MemoryDatabaseUtility.shutdown(rockEtlRep);
  MemoryDatabaseUtility.shutdown(rockDwh);
  
  final File f = new File(TEST_VIEW_VM);
  if (!f.delete()) {
    fail("failed to remove test template");
  }
  
  
}

@Before
public final void setUp()  throws Exception {
	  
  
  webmockSession = new MockHttpSession(null);

  webmockRequest = new MockHttpServletRequest();

  webmockResponse = new MockHttpServletResponse();

  velocityContext = new VelocityContext();

  mockContext = context.mock(Context.class, "context");

  mockSubContext = context.mock(Context.class, "subcontext");
  
  Helper.setContext(mockContext);
 
}

@Test
public void testfetchHierarchy() throws Exception {
	final Method m = ShowHistorySet.class.getDeclaredMethod("fetchHierarchy", Connection.class);
	m.setAccessible(true);
	final ArrayList<String> data = (ArrayList<String>) m.invoke(new ShowHistorySet(), rockEtlRep.getConnection());
	assertThat(data.size(), is(3));
}

@Test
public void testfetchSets() throws Exception {
	final Method m = ShowHistorySet.class.getDeclaredMethod("fetchSets", Connection.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class);
	m.setAccessible(true);
	final ArrayList<String> data = (ArrayList<String>) m.invoke(new ShowHistorySet(), rockEtlRep.getConnection(), "DWH_MONITOR", "15", "04", "2011", "ALL", "DWH_MONITOR", "10" );
	assertThat(data.size(), is(1));
}

@Test
public void testDoHandleRequest() throws Exception {

  context.checking(new Expectations() {

    {
      allowing(mockContext).lookup(with(any(String.class)));
      will(returnValue(mockSubContext));
  	
      allowing(mockSubContext).lookup("conffiles");
      will(returnValue("."));
      allowing(mockSubContext).lookup("maxHistoryRows");
      will(returnValue("10"));

    }
  });
  
  webmockRequest = new MockHttpServletRequest("GET", "/servlet/ShowHistorySet");
  webmockRequest.setParameter("Helper.PARAM_SEARCH_STRING", "");
  webmockRequest.setParameter("Helper.PARAM_SELECTED_SET_TYPE", "");
  webmockRequest.setParameter("Helper.PARAM_SELECTED_TECHPACK", "");
  webmockRequest.setParameter("year_1", "2011");
  webmockRequest.setParameter("month_1", "04");
  webmockRequest.setParameter("day_1", "05");
  
  webmockRequest.setSession(webmockSession);
  
  velocityContext.put("rockEtlRep",rockEtlRep);
  velocityContext.put("rockDwh",rockDwh);
 
    ShowHistorySet servlet = new ShowHistorySet();
    Template template = servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
    assertNotNull(template);
   
}

@Test
public void testDoHandleRequest1() throws Exception {

  context.checking(new Expectations() {

    {
      allowing(mockContext).lookup(with(any(String.class)));
      will(returnValue(mockSubContext));
  	
      allowing(mockSubContext).lookup("conffiles");
      will(returnValue("."));
      allowing(mockSubContext).lookup("maxHistoryRows");
      will(returnValue("10"));

    }
  });
  
  webmockRequest = new MockHttpServletRequest("GET", "/servlet/ShowHistorySet");
  webmockRequest.setParameter("Helper.PARAM_SEARCH_STRING", "");
  webmockRequest.setParameter("Helper.PARAM_SELECTED_SET_TYPE", "");
  webmockRequest.setParameter("Helper.PARAM_SELECTED_TECHPACK", "");
  
  webmockRequest.setSession(webmockSession);
  
  velocityContext.put("rockEtlRep",rockEtlRep);
  velocityContext.put("rockDwh",rockDwh);
 
    ShowHistorySet servlet = new ShowHistorySet();
    Template template = servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
    assertNotNull(template);
   
}

}
