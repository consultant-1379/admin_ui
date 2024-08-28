/**
 * 
 */
package com.distocraft.dc5000.etl.gui.afjupgrade;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogSystem;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess.State;
import com.distocraft.dc5000.etl.gui.afjupgrade.MessageBean.MessageType;
import com.ericsson.eniq.afj.AfjManagerFactory;
import com.ericsson.eniq.afj.common.AFJDelta;
import com.ericsson.eniq.afj.common.AFJMeasurementCounter;
import com.ericsson.eniq.afj.common.AFJMeasurementTag;
import com.ericsson.eniq.afj.common.AFJMeasurementType;
import com.ericsson.eniq.afj.common.AFJTechPack;


/**
 * @author eheijun
 *
 */
public class AfjShowDeltaResultsServletTest {

  private static final String TEST_VIEW_VM = "afj_show_delta_results_view.vm";

  private static final String SOME_STATUS_MESSAGE = "SOME STATUS MESSAGE";

  private static final String SOME_ERROR_MESSAGE = "SOME ERROR MESSAGE";

  private static final String SOME_TECH_PACK = "SOME_TECH_PACK";

  private static final String SOME_TECH_PACK_VERSION = "SOME_TECH_PACK_VERSION";

  private static final String JUST_SOME_TECHPACK = "JUST_SOME_TECHPACK";

  private final Mockery context = new JUnit4Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  private VelocityContext velocityContext;

  private MockAfjManager mockAfjManager;

  private AfjManagerProcess mockAfjManagerProcess;

  private MockHttpSession webmockSession;

  private MockHttpServletRequest webmockRequest;

  private MockHttpServletResponse webmockResponse;

  private AFJTechPack mockAFJTechPack;

  private AFJDelta mockAfjDelta;

  private AFJMeasurementType mockAFJMeasurementType1_1;

  private AFJMeasurementType mockAFJMeasurementType1_2;

  private AFJMeasurementTag mockAFJTag1_1;

  private AFJMeasurementTag mockAFJTag1_2;

  private AFJMeasurementTag mockAFJTag2_1;

  private AFJMeasurementTag mockAFJTag2_2;

  private AFJMeasurementCounter mockAFJMeasurementCounter1_1_1;

  private AFJMeasurementCounter mockAFJMeasurementCounter1_1_2;

  private AFJMeasurementCounter mockAFJMeasurementCounter1_2_1;

  private AFJMeasurementCounter mockAFJMeasurementCounter1_2_2;

  private AFJMeasurementCounter mockAFJMeasurementCounter2_1_1;

  private AFJMeasurementCounter mockAFJMeasurementCounter2_1_2;

  private AFJMeasurementCounter mockAFJMeasurementCounter2_2_1;
  
  /**
   * @throws java.lang.Exception
   */
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

    Velocity.setProperty(Velocity.RESOURCE_LOADER, "file");
    Velocity.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
    Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogSystem.class.getName());
    Velocity.init();

  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    final File f = new File(TEST_VIEW_VM);
    if (!f.delete()) {
      fail("failed to remove test template file");
    }
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {

    final FileWriter fwriter = new FileWriter(TEST_VIEW_VM);
    try {
      final PrintWriter out = new PrintWriter(fwriter);
      try {
        out.println("<html></html>");
        fwriter.flush();
      } finally {
        out.close();
      }
    } finally {
      fwriter.close();
    }
    
    mockAfjManager = new MockAfjManager();

    mockAfjManager.installAFJTechPack();

    mockAfjManagerProcess = context.mock(AfjManagerProcess.class);

    webmockSession = new MockHttpSession(null);

    webmockRequest = new MockHttpServletRequest("GET", "/servlet/AfjShowDeltaResults");

    webmockResponse = new MockHttpServletResponse();

    AfjManagerFactory.setInstance(mockAfjManager);

    webmockRequest.setSession(webmockSession);

    velocityContext = new VelocityContext();

    mockAFJTechPack = context.mock(AFJTechPack.class);
    
    mockAfjDelta = context.mock(AFJDelta.class);

    mockAFJMeasurementType1_1 = context.mock(AFJMeasurementType.class, "MT1.1");
    
    mockAFJMeasurementType1_2 = context.mock(AFJMeasurementType.class, "MT1.2");

    mockAFJTag1_1 = context.mock(AFJMeasurementTag.class, "1.1");
    
    mockAFJTag1_2 = context.mock(AFJMeasurementTag.class, "1.2");

    mockAFJTag2_1 = context.mock(AFJMeasurementTag.class, "2.1");
    
    mockAFJTag2_2 = context.mock(AFJMeasurementTag.class, "2.2");

    mockAFJMeasurementCounter1_1_1 = context.mock(AFJMeasurementCounter.class, "1.1.1");
    
    mockAFJMeasurementCounter1_1_2 = context.mock(AFJMeasurementCounter.class, "1.1.2");
    
    mockAFJMeasurementCounter1_2_1 = context.mock(AFJMeasurementCounter.class, "1.2.1");
    
    mockAFJMeasurementCounter1_2_2 = context.mock(AFJMeasurementCounter.class, "1.2.2");
    
    mockAFJMeasurementCounter2_1_1 = context.mock(AFJMeasurementCounter.class, "2.1.1");
    
    mockAFJMeasurementCounter2_1_2 = context.mock(AFJMeasurementCounter.class, "2.1.2");
    
    mockAFJMeasurementCounter2_2_1 = context.mock(AFJMeasurementCounter.class, "2.2.1");
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjShowDeltaResultsServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequest() {
    
    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_STATUS_MESSAGE));
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getProcessState();
        will(returnValue(State.GENERATE_READY));
        allowing(mockAfjManagerProcess).isErrorState();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getAFJDelta();
        will(returnValue(mockAfjDelta));
        allowing(mockAfjManagerProcess).getSelectedTechPack();
        will(returnValue(mockAFJTechPack));
        allowing(mockAfjManagerProcess).initializeStatus();
        
        allowing(mockAfjDelta).getMeasurementTypes();
        will(returnValue(new ArrayList<AFJMeasurementType>(Arrays.asList(new AFJMeasurementType[] { mockAFJMeasurementType1_1, mockAFJMeasurementType1_2 }))));
        allowing(mockAfjDelta).getTechPackVersion();
        will(returnValue(SOME_TECH_PACK_VERSION));
        allowing(mockAfjDelta).getTechPackName();
        will(returnValue(SOME_TECH_PACK));
        
        allowing(mockAFJMeasurementType1_1).isTypeNew();
        will(returnValue(false));
        allowing(mockAFJMeasurementType1_1).getTags();
        will(returnValue(new ArrayList<AFJMeasurementTag>(Arrays.asList(new AFJMeasurementTag[] { mockAFJTag1_1, mockAFJTag1_2 }))));

        allowing(mockAFJMeasurementType1_2).isTypeNew();
        will(returnValue(true));
        allowing(mockAFJMeasurementType1_2).getTags();
        will(returnValue(new ArrayList<AFJMeasurementTag>(Arrays.asList(new AFJMeasurementTag[] { mockAFJTag2_1, mockAFJTag2_2 }))));

        allowing(mockAFJTag1_1).getNewCounters();
        will(returnValue(Arrays.asList(new AFJMeasurementCounter[] {mockAFJMeasurementCounter1_1_1, mockAFJMeasurementCounter1_1_2})));
        
        allowing(mockAFJTag1_2).getNewCounters();
        will(returnValue(Arrays.asList(new AFJMeasurementCounter[] {mockAFJMeasurementCounter1_2_1, mockAFJMeasurementCounter1_2_2})));
        
        allowing(mockAFJTag2_1).getNewCounters();
        will(returnValue(Arrays.asList(new AFJMeasurementCounter[] {mockAFJMeasurementCounter2_1_1, mockAFJMeasurementCounter2_1_2})));
        
        allowing(mockAFJTag2_2).getNewCounters();
        will(returnValue(Arrays.asList(new AFJMeasurementCounter[] {mockAFJMeasurementCounter2_2_1})));
        
        allowing(mockAFJTechPack).getTechPackName();
        will(returnValue(SOME_TECH_PACK));
        
        allowing(mockAFJTechPack).getMaxCounters();
        will(returnValue(100));
        
        allowing(mockAFJTechPack).getMaxMeasTypes();
        will(returnValue(20));
        
      }
    });

    webmockSession.setAttribute(AfjShowDeltaResultsServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    final AfjShowDeltaResultsServlet servlet = new AfjShowDeltaResultsServlet();
    try {
      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
      
      final Object messageListObj = velocityContext.get(AfjShowDeltaResultsServlet.MESSAGES);
      if (messageListObj instanceof List<?>) {
        final List<?> messageList = ((List<?>) messageListObj);
        assertTrue(messageList.size() > 0);
        final Object listItem0 = messageList.get(0);
        if (listItem0 instanceof MessageBean) {
          final MessageBean mb = (MessageBean) listItem0;
          assertTrue(mb.getType() == MessageType.INFO);
          assertTrue(mb.getText().equals(SOME_STATUS_MESSAGE));
        }
      }
      
      final Integer newtypecount = (Integer) velocityContext.get(AfjShowDeltaResultsServlet.NEWTYPECOUNT);
      assertTrue(newtypecount == 1);
      final Integer newtypecountercount = (Integer) velocityContext.get(AfjShowDeltaResultsServlet.NEWTYPECOUNTERCOUNT);
      assertTrue(newtypecountercount == 3);
      final Integer modtypecount = (Integer) velocityContext.get(AfjShowDeltaResultsServlet.MODTYPECOUNT);
      assertTrue(modtypecount == 1);
      final Integer modtypecountercount = (Integer) velocityContext.get(AfjShowDeltaResultsServlet.MODTYPECOUNTERCOUNT);
      assertTrue(modtypecountercount == 4);
      final Integer tottypecount = (Integer) velocityContext.get(AfjShowDeltaResultsServlet.TOTTYPECOUNT);
      assertTrue(tottypecount == 2);
      
      
      
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjShowDeltaResultsServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestForUpgrade() {

    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_STATUS_MESSAGE));
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getProcessState();
        will(returnValue(State.NONE));
        allowing(mockAfjManagerProcess).getAFJDelta();
        will(returnValue(mockAfjDelta));
        allowing(mockAfjManagerProcess).setSelectedTechPack(with(any(String.class)));

      }
    });

    webmockSession.setAttribute(AfjShowDeltaResultsServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    webmockRequest.setParameter("runupgrade." + JUST_SOME_TECHPACK, "Show Delta");
    webmockRequest.setParameter("confirm", "true");

    final AfjShowDeltaResultsServlet servlet = new AfjShowDeltaResultsServlet();
    try {

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final String forwardURL = webmockResponse.getForwardedUrl();
      assertTrue(View.AFJ_START_UPGRADE_TECH_PACK.equals(forwardURL));

    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjShowDeltaResultsServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestForUpgradeWithoutConfirm() {

    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_STATUS_MESSAGE));
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getProcessState();
        will(returnValue(State.NONE));
        allowing(mockAfjManagerProcess).isErrorState();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getAFJDelta();
        will(returnValue(mockAfjDelta));
        allowing(mockAfjManagerProcess).getSelectedTechPack();
        will(returnValue(mockAFJTechPack));
        allowing(mockAfjManagerProcess).initializeStatus();

        allowing(mockAfjDelta).getMeasurementTypes();
        will(returnValue(new ArrayList<AFJMeasurementType>(Arrays.asList(new AFJMeasurementType[] { mockAFJMeasurementType1_1, mockAFJMeasurementType1_2 }))));
        allowing(mockAfjDelta).getTechPackVersion();
        will(returnValue(SOME_TECH_PACK_VERSION));
        allowing(mockAfjDelta).getTechPackName();
        will(returnValue(SOME_TECH_PACK));
        
        allowing(mockAFJMeasurementType1_1).isTypeNew();
        will(returnValue(false));
        allowing(mockAFJMeasurementType1_1).getTags();
        will(returnValue(new ArrayList<AFJMeasurementTag>(Arrays.asList(new AFJMeasurementTag[] { mockAFJTag1_1, mockAFJTag1_2 }))));

        allowing(mockAFJMeasurementType1_2).isTypeNew();
        will(returnValue(true));
        allowing(mockAFJMeasurementType1_2).getTags();
        will(returnValue(new ArrayList<AFJMeasurementTag>(Arrays.asList(new AFJMeasurementTag[] { mockAFJTag2_1, mockAFJTag2_2 }))));

        allowing(mockAFJTag1_1).getNewCounters();
        will(returnValue(Arrays.asList(new AFJMeasurementCounter[] {mockAFJMeasurementCounter1_1_1, mockAFJMeasurementCounter1_1_2})));
        
        allowing(mockAFJTag1_2).getNewCounters();
        will(returnValue(Arrays.asList(new AFJMeasurementCounter[] {mockAFJMeasurementCounter1_2_1, mockAFJMeasurementCounter1_2_2})));
        
        allowing(mockAFJTag2_1).getNewCounters();
        will(returnValue(Arrays.asList(new AFJMeasurementCounter[] {mockAFJMeasurementCounter2_1_1, mockAFJMeasurementCounter2_1_2})));
        
        allowing(mockAFJTag2_2).getNewCounters();
        will(returnValue(Arrays.asList(new AFJMeasurementCounter[] {mockAFJMeasurementCounter2_2_1})));
        
        allowing(mockAFJTechPack).getTechPackName();
        will(returnValue(SOME_TECH_PACK));
        
        allowing(mockAFJTechPack).getMaxCounters();
        will(returnValue(100));
        
        allowing(mockAFJTechPack).getMaxMeasTypes();
        will(returnValue(20));
      }
    });

    webmockSession.setAttribute(AfjShowDeltaResultsServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    webmockRequest.setParameter("runupgrade." + JUST_SOME_TECHPACK, "Show Delta");

    final AfjShowDeltaResultsServlet servlet = new AfjShowDeltaResultsServlet();
    try {

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final Object messageListObj = velocityContext.get(AfjShowDeltaResultsServlet.MESSAGES);
      if (messageListObj instanceof List<?>) {
        final List<?> messageList = ((List<?>) messageListObj);
        assertTrue(messageList.size() > 0);
        final Object listItem0 = messageList.get(0);
        if (listItem0 instanceof MessageBean) {
          final MessageBean mb = (MessageBean) listItem0;
          assertTrue(mb.getType() == MessageType.ERROR);
          assertFalse(mb.getText().equals(""));
        }
      }

    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjShowDeltaResultsServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestWhenDeltaIsRunning() {

    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_STATUS_MESSAGE));
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(true));
        allowing(mockAfjManagerProcess).getProcessState();
        will(returnValue(State.GENERATE_DELTA));

      }
    });

    webmockSession.setAttribute(AfjShowDeltaResultsServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    final AfjShowDeltaResultsServlet servlet = new AfjShowDeltaResultsServlet();
    try {

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final String redirectURL = webmockResponse.getRedirectedUrl();
      assertTrue(View.AFJ_SHOW_PROCESS_STATUS.equals(redirectURL));

    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjShowDeltaResultsServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestWhenUpgradeIsRunning() {

    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_STATUS_MESSAGE));
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(true));
        allowing(mockAfjManagerProcess).getProcessState();
        will(returnValue(State.UPGRADE_TECHPACK));

      }
    });

    webmockSession.setAttribute(AfjShowDeltaResultsServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    final AfjShowDeltaResultsServlet servlet = new AfjShowDeltaResultsServlet();
    try {

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final String redirectURL = webmockResponse.getRedirectedUrl();
      assertTrue(View.AFJ_SHOW_PROCESS_STATUS.equals(redirectURL));

    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjShowDeltaResultsServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestWhenUpgradeIsReady() {

    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_STATUS_MESSAGE));
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getProcessState();
        will(returnValue(State.UPGRADE_READY));

      }
    });

    webmockSession.setAttribute(AfjShowDeltaResultsServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    final AfjShowDeltaResultsServlet servlet = new AfjShowDeltaResultsServlet();
    try {

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final String redirectURL = webmockResponse.getRedirectedUrl();
      assertTrue(View.AFJ_SHOW_AFJ_TECH_PACKS.equals(redirectURL));

    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjShowDeltaResultsServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestWhenErrorInProcess() {
    
    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_ERROR_MESSAGE));
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getProcessState();
        will(returnValue(State.GENERATE_ERROR));
        allowing(mockAfjManagerProcess).isErrorState();
        will(returnValue(true));
        allowing(mockAfjManagerProcess).getAFJDelta();
        will(returnValue(null));
        allowing(mockAfjManagerProcess).getSelectedTechPack();
        will(returnValue(mockAFJTechPack));
        allowing(mockAfjManagerProcess).initializeStatus();

        allowing(mockAFJTechPack).getTechPackName();
        will(returnValue(SOME_TECH_PACK));
        
        allowing(mockAFJTechPack).getMaxCounters();
        will(returnValue(100));
        
        allowing(mockAFJTechPack).getMaxMeasTypes();
        will(returnValue(20));
      }
    });

    webmockSession.setAttribute(AfjShowDeltaResultsServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    final AfjShowDeltaResultsServlet servlet = new AfjShowDeltaResultsServlet();
    try {

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final Object messageListObj = velocityContext.get(AfjShowDeltaResultsServlet.MESSAGES);
      if (messageListObj instanceof List<?>) {
        final List<?> messageList = ((List<?>) messageListObj);
        assertTrue(messageList.size() > 0);
        final Object listItem0 = messageList.get(0);
        if (listItem0 instanceof MessageBean) {
          final MessageBean mb = (MessageBean) listItem0;
          assertTrue(mb.getType() == MessageType.ERROR);
          assertTrue(mb.getText().equals(SOME_ERROR_MESSAGE));
        }
      }

    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjShowDeltaResultsServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestWhenAFJManagerProcessIsNotInitialized () {
	  
	    final AfjShowDeltaResultsServlet servlet = new AfjShowDeltaResultsServlet();
	    try {
	      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
	      final String redirectURL = webmockResponse.getRedirectedUrl();
	      assertTrue(View.AFJ_SHOW_AFJ_TECH_PACKS.equals(redirectURL));
	    } catch (Exception e) {
	        fail(e.getMessage());
	    }
  }
}
