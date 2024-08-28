package com.distocraft.dc5000.etl.gui.systemmonitor;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.naming.Context;

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
import com.ericsson.eniq.common.testutilities.UnitDatabaseTestCase;

import ssc.rockfactory.RockFactory;


public class StatusDetailsTest extends UnitDatabaseTestCase {
	
  public static final String TEST_APPLICATION = StatusDetailsTest.class.getName();
  
  private static final String TEST_VIEW_VM = "monitoring_iq_status_detail.vm";

  private static MockHttpServletRequest webmockRequest;

  private static MockHttpSession webmockSession;

  private MockHttpServletResponse webmockResponse;

  private VelocityContext velocityContext;
  
  private Mockery context = new Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  //public static RockFactory rockEtlRepDba;
  
  public static RockFactory rockDwhDba;

  private Context mockContext;

  private Context mockSubContext;

  static {
    System.setProperty("integration_host", "atrcx892zone3.athtem.eei.ericsson.se");
  }
  
  @BeforeClass
  public final static void setUpBeforeClass() throws java.lang.Exception {
    
    setup(TestType.integration);

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

    rockDwhDba = getSaConnection_DWHDB();
    
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

  @After
  public final void tearDown() throws Exception {
    Helper.setContext(null);
  }

  @AfterClass
  public final static void tearDownAfterClass() throws java.lang.Exception {

    final File f = new File(TEST_VIEW_VM);
    if (!f.delete()) {
      fail("failed to remove test template file");
    }
    
	try {

//		MemoryDatabaseUtility.shutdown(rockEtlRepDba);
//		MemoryDatabaseUtility.shutdown(rockDwhDba);
		
	}
	
	catch(Exception e){
		fail(e.getMessage());
	}
	
  System.clearProperty("CONF_DIR");

  }

  @Test
  public void testDoHandleRequestForDwh() throws Exception {

    context.checking(new Expectations() {

      {
        allowing(mockContext).lookup(with(any(String.class)));
        will(returnValue(mockSubContext));
        allowing(mockSubContext).lookup("conffiles");
        will(returnValue("."));
        allowing(mockSubContext).lookup("intOtherVersions");
        will(returnValue("10"));
        allowing(mockSubContext).lookup("intDbSize");
        will(returnValue("80"));


      }
    });
    
    webmockRequest = new MockHttpServletRequest("GET", "/servlet/StatusDetails");
    webmockRequest.setParameter("ds", "rockDwhDba");
    velocityContext.put("rockDwhDba",rockDwhDba);
    try {
       
      StatusDetails servlet = new StatusDetails();
      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
    }
    catch (Exception e) {
    	String actual = null;
      assertEquals(e.getMessage(), actual);
    }
    
  }

  @Test
  public void testDoHandleRequestForEtlrep() throws Exception {

    context.checking(new Expectations() {

      {
        allowing(mockContext).lookup(with(any(String.class)));
        will(returnValue(mockSubContext));
        allowing(mockSubContext).lookup("conffiles");
        will(returnValue("."));
        allowing(mockSubContext).lookup("intOtherVersions");
        will(returnValue("10"));
        allowing(mockSubContext).lookup("intDbSize");
        will(returnValue("80"));


      }
    });
    
    webmockRequest = new MockHttpServletRequest("GET", "/servlet/StatusDetails");
    webmockRequest.setParameter("ds", "rockEtlRepDba");
    velocityContext.put("rockEtlRepDba",rockDwhDba);
    try {
       
      StatusDetails servlet = new StatusDetails();
      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
    }
    catch (Exception e) {
      String actual = null;
      assertEquals(e.getMessage(), actual);
    }
    
  }

}
