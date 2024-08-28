package com.distocraft.dc5000.etl.gui.manual;

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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.distocraft.dc5000.etl.gui.util.Helper;

public class ManualTest {
	
  private static final long serialVersionUID = 1L;

  public static final String TEST_APPLICATION = ManualTest.class.getName();
  
  private static final String TEST_VIEW_VM1 = "index.html";
  private static final String TEST_VIEW_VM2 = "manual.vm";
  
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
  

  @BeforeClass
  public final static void setUpBeforeClass() throws java.lang.Exception {

	    final FileWriter fwriter1 = new FileWriter(TEST_VIEW_VM1);
	    try {
	      final PrintWriter out = new PrintWriter(fwriter1);
	      try {
	        out.println("<html></html>");
	      } finally {
	        out.close();
	      }
	    } finally {
	      fwriter1.close();
	    }
	    
	    final FileWriter fwriter2 = new FileWriter(TEST_VIEW_VM2);
	    try {
	      final PrintWriter out = new PrintWriter(fwriter2);
	      try {
	        out.println("<html></html>");
	      } finally {
	        out.close();
	      }
	    } finally {
	      fwriter2.close();
	    }
	  
    Velocity.setProperty(Velocity.RESOURCE_LOADER, "file");
    Velocity.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
    Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogSystem.class.getName());
    Velocity.init();

  }
  
  @AfterClass
  public final static void tearDownAfterClass() throws java.lang.Exception {

    final File f1 = new File(TEST_VIEW_VM1);
    if (!f1.delete()) {
      fail("failed to remove test template file");
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

  @Test
  public void testDoHandleRequest1() throws Exception {

    webmockRequest = new MockHttpServletRequest("GET", "/servlet/StatusDetails");
    webmockRequest.setParameter("page", "null");
    
    try {
       
      final Manual manual = new Manual();
      manual.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
    }
    catch (Exception e) {
    	final String actual = "null";
      assertEquals(e.getMessage(), actual);
    }
    
  }
  
  @Test
  public void testDoHandleRequest2() throws Exception {

    webmockRequest = new MockHttpServletRequest("GET", "/servlet/StatusDetails");
    webmockRequest.setParameter("page", "null");
    
    final File f2 = new File(TEST_VIEW_VM2);
    if (!f2.delete()) {
      fail("failed to remove test template file");
    }
    try {
       
      final Manual manual = new Manual();
      manual.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
    }
    catch (Exception e) {
    	final String actual = "null";
      assertEquals(e.getMessage(), actual);
    }
    
  }
  
  @Test
  public void testDoHandleRequest3() throws Exception {

    webmockRequest = new MockHttpServletRequest("GET", "/servlet/StatusDetails");
    webmockRequest.setParameter("page", "page");
    
    try {
       
      final Manual manual = new Manual();
      manual.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
    }
    catch (Exception e) {
    	final String actual = "null";
      assertEquals(e.getMessage(), actual);
    }
    
  } 
  
}
