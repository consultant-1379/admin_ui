/**
 * 
 */
package com.distocraft.dc5000.etl.gui.afjupgrade;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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
import com.ericsson.eniq.afj.common.AFJTechPack;


/**
 * @author eheijun
 *
 */
public class AfjShowProcessStatusServletTest {

  private static final String TEST_VIEW_VM = "afj_show_process_status_view.vm";

  private static final String SOME_STATUS_MESSAGE = "SOME STATUS MESSAGE";

  private static final String SOME_TECH_PACK = "SOME_TECH_PACK";

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

    mockAfjManager = new MockAfjManager();

    mockAfjManagerProcess = context.mock(AfjManagerProcess.class);

    webmockSession = new MockHttpSession(null);

    webmockRequest = new MockHttpServletRequest("GET", "/servlet/AfjShowProcessStatus");

    webmockResponse = new MockHttpServletResponse();

    AfjManagerFactory.setInstance(mockAfjManager);

    webmockRequest.setSession(webmockSession);

    velocityContext = new VelocityContext();

    mockAFJTechPack = context.mock(AFJTechPack.class);

  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjShowProcessStatusServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestWhenAFJManagerIsRunning() {
    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(true));
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_STATUS_MESSAGE));
        allowing(mockAfjManagerProcess).getSelectedTechPack();
        will(returnValue(mockAFJTechPack));

        allowing(mockAFJTechPack).getTechPackName();
        will(returnValue(SOME_TECH_PACK));

      }
    });

    webmockSession.setAttribute(AfjShowProcessStatusServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);
    
    final AfjShowProcessStatusServlet servlet = new AfjShowProcessStatusServlet();
    try {
      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
      final Object messageListObj = velocityContext.get(AfjShowProcessStatusServlet.MESSAGES);
      if (messageListObj instanceof List<?>) {
        final List<?> messageList = ((List<?>) messageListObj);
        assertTrue(messageList.size() > 0);
        final Object listItem0 = messageList.get(0);
        if (listItem0 instanceof MessageBean) {
          final MessageBean mb = (MessageBean) listItem0;
          assertTrue(mb.getType() == MessageType.RUNNING);
          assertTrue(mb.getText().equals(SOME_STATUS_MESSAGE));
        }
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjShowProcessStatusServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestWhenAFJManagerDeltaIsReady() {
    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_STATUS_MESSAGE));
        allowing(mockAfjManagerProcess).getProcessState();
        will(returnValue(State.GENERATE_READY));

      }
    });

    webmockSession.setAttribute(AfjShowProcessStatusServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);
    
    final AfjShowProcessStatusServlet servlet = new AfjShowProcessStatusServlet();
    try {
      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
      final String redirectURL = webmockResponse.getRedirectedUrl();
      assertTrue(View.AFJ_SHOW_DELTA_RESULTS.equals(redirectURL));
    } catch (Exception e) {
      fail(e.getMessage());
    }
    
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjShowProcessStatusServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestWhenAFJManagerUpgradeIsReady() {
    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_STATUS_MESSAGE));
        allowing(mockAfjManagerProcess).getProcessState();
        will(returnValue(State.UPGRADE_READY));

      }
    });

    webmockSession.setAttribute(AfjShowProcessStatusServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);
    
    final AfjShowProcessStatusServlet servlet = new AfjShowProcessStatusServlet();
    try {
      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
      final String redirectURL = webmockResponse.getRedirectedUrl();
      assertTrue(View.AFJ_SHOW_AFJ_TECH_PACKS.equals(redirectURL));
    } catch (Exception e) {
      fail(e.getMessage());
    }
    
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjShowProcessStatusServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestWhenAFJManagerIsNotInitialized() {

    webmockSession.setAttribute(AfjShowProcessStatusServlet.AFJ_MANAGER_PROCESS, null);
    
    final AfjShowProcessStatusServlet servlet = new AfjShowProcessStatusServlet();
    try {
      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
      final String redirectURL = webmockResponse.getRedirectedUrl();
      assertTrue(View.AFJ_SHOW_AFJ_TECH_PACKS.equals(redirectURL));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

}
