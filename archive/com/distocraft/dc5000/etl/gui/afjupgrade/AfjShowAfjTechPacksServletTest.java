package com.distocraft.dc5000.etl.gui.afjupgrade;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogSystem;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.jmock.Expectations;
import org.jmock.Mockery;
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

public class AfjShowAfjTechPacksServletTest {

  private static final String TEST_VIEW_VM = "afj_show_afjtechpacks_view.vm";

  private static final String SOME_STATUS_MESSAGE = "SOME STATUS MESSAGE";

  private static final String SOME_ERROR_MESSAGE = "SOME ERROR MESSAGE";

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

  /**
   * 
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
   * 
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {

    mockAfjManager = new MockAfjManager();

    mockAfjManagerProcess = context.mock(AfjManagerProcess.class);

    webmockSession = new MockHttpSession(null);

    webmockRequest = new MockHttpServletRequest("GET", "/servlet/AfjShowAfjTechPacks");

    webmockResponse = new MockHttpServletResponse();

    AfjManagerFactory.setInstance(mockAfjManager);

    webmockRequest.setSession(webmockSession);

    velocityContext = new VelocityContext();

  }

  /**
   * tests basic call to the servlet
   */
  @Test
  public void testDoHandleRequest() {
    final AfjShowAfjTechPacksServlet servlet = new AfjShowAfjTechPacksServlet();
    try {

      // "install" AFJTechPack
      mockAfjManager.installAFJTechPack();

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final AfjManagerProcess amp = (AfjManagerProcess) webmockSession
          .getAttribute(AfjShowAfjTechPacksServlet.AFJ_MANAGER_PROCESS);
      assertTrue(amp != null);

      final Object messageListObj = velocityContext.get(AfjShowAfjTechPacksServlet.MESSAGES);
      if (messageListObj instanceof List<?>) {
        final List<?> messageList = ((List<?>) messageListObj);
        assertTrue(messageList.size() == 0);
      }

      final Object techpackListObj = velocityContext.get(AfjShowAfjTechPacksServlet.AFJTECHPACKLIST);
      if (techpackListObj instanceof List<?>) {
        final List<?> techpackList = ((List<?>) techpackListObj);
        assertTrue(techpackList.size() > 0);
        final Object listItem0 = techpackList.get(0);
        if (listItem0 instanceof AFJTechPack) {
          final AFJTechPack tp = (AFJTechPack) listItem0;
          assertTrue(tp.getTechPackName().equals("DC_E_STN"));
          if (tp.isMomFilePresent()) {
            assertTrue(tp.getFileName().equals("NEW_STN_COUNTERS.xml"));
          } else {
            assertTrue(tp.getFileName().equals(""));
          }
          //assertFalse(tp.getStatus().equals(""));
        }
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * tests basic call to the servlet when there is no afjtechpack in system
   */
  @Test
  public void testDoHandleRequestWithNoAFJTechPack() {
    final AfjShowAfjTechPacksServlet servlet = new AfjShowAfjTechPacksServlet();
    try {
      
      // "remove" AFJTechPack
      mockAfjManager.uninstallAFJTechPack();

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final AfjManagerProcess amp = (AfjManagerProcess) webmockSession
          .getAttribute(AfjShowAfjTechPacksServlet.AFJ_MANAGER_PROCESS);
      assertTrue(amp != null);

      final Object messageListObj = velocityContext.get(AfjShowAfjTechPacksServlet.MESSAGES);
      if (messageListObj instanceof List<?>) {
        final List<?> messageList = ((List<?>) messageListObj);
        assertTrue(messageList.size() == 1);
      }

      final Object techpackListObj = velocityContext.get(AfjShowAfjTechPacksServlet.AFJTECHPACKLIST);
      if (techpackListObj instanceof List<?>) {
        final List<?> techpackList = ((List<?>) techpackListObj);
        assertTrue(techpackList.size() == 0);
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * tests call to the servlet when user wants to start delta generation
   */
  @Test
  public void testDoHandleRequestForDelta() {

    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_STATUS_MESSAGE));
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getProcessState();
        will(returnValue(State.NONE));
        allowing(mockAfjManagerProcess).setSelectedTechPack(with(any(String.class)));

      }
    });

    webmockSession.setAttribute(AfjShowAfjTechPacksServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    webmockRequest.setParameter("showdelta." + JUST_SOME_TECHPACK, "Show Delta");

    final AfjShowAfjTechPacksServlet servlet = new AfjShowAfjTechPacksServlet();
    try {

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final String forwardURL = webmockResponse.getForwardedUrl();
      assertTrue(View.AFJ_START_GENERATE_DELTA.equals(forwardURL));

    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * tests call to the servlet when delta calculation is still running
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

    webmockSession.setAttribute(AfjShowAfjTechPacksServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    final AfjShowAfjTechPacksServlet servlet = new AfjShowAfjTechPacksServlet();
    try {

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final String redirectURL = webmockResponse.getRedirectedUrl();
      assertTrue(View.AFJ_SHOW_PROCESS_STATUS.equals(redirectURL));

    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * tests call to the servlet when tech pack upgrade is still running
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

    webmockSession.setAttribute(AfjShowAfjTechPacksServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    final AfjShowAfjTechPacksServlet servlet = new AfjShowAfjTechPacksServlet();
    try {

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final String redirectURL = webmockResponse.getRedirectedUrl();
      assertTrue(View.AFJ_SHOW_PROCESS_STATUS.equals(redirectURL));

    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * tests call to the servlet when delta calculation is ready but results are
   * not yet shown
   */
  @Test
  public void testDoHandleRequestWhenDeltaIsReady() {

    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_STATUS_MESSAGE));
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getProcessState();
        will(returnValue(State.GENERATE_READY));

      }
    });

    webmockSession.setAttribute(AfjShowAfjTechPacksServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    final AfjShowAfjTechPacksServlet servlet = new AfjShowAfjTechPacksServlet();
    try {

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
      
      final String redirectURL = webmockResponse.getRedirectedUrl();
      assertTrue(View.AFJ_SHOW_DELTA_RESULTS.equals(redirectURL));

    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * tests call to the servlet when upgrade is ready but results are not yet
   * shown
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
        allowing(mockAfjManagerProcess).isErrorState();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getAFJTechPackList();
        will(returnValue(new ArrayList<AFJTechPack>()));
        allowing(mockAfjManagerProcess).initializeStatus();

      }
    });

    webmockSession.setAttribute(AfjShowAfjTechPacksServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    final AfjShowAfjTechPacksServlet servlet = new AfjShowAfjTechPacksServlet();
    try {

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final Object messageListObj = velocityContext.get(AfjShowAfjTechPacksServlet.MESSAGES);
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

    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * tests call to the servlet when some error has happened
   */
  @Test
  public void testDoHandleRequestWhenErrorInProcess() {
    
    final List<AFJTechPack> emptyList = new ArrayList<AFJTechPack>();

    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).getStatusMessage();
        will(returnValue(SOME_ERROR_MESSAGE));
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getProcessState();
        will(returnValue(State.UPGRADE_ERROR));
        allowing(mockAfjManagerProcess).isErrorState();
        will(returnValue(true));
        allowing(mockAfjManagerProcess).getAFJTechPackList();
        will(returnValue(emptyList));
        allowing(mockAfjManagerProcess).initializeStatus();

      }
    });

    webmockSession.setAttribute(AfjShowAfjTechPacksServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    final AfjShowAfjTechPacksServlet servlet = new AfjShowAfjTechPacksServlet();
    try {

      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);

      final Object messageListObj = velocityContext.get(AfjShowAfjTechPacksServlet.MESSAGES);
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

}
