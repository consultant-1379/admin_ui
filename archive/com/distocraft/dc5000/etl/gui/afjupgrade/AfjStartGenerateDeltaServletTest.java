/**
 * 
 */
package com.distocraft.dc5000.etl.gui.afjupgrade;

import static org.junit.Assert.*;

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

import com.ericsson.eniq.afj.AfjManagerFactory;
import com.ericsson.eniq.afj.common.AFJTechPack;


/**
 * @author eheijun
 *
 */
public class AfjStartGenerateDeltaServletTest {

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

    Velocity.setProperty(Velocity.RESOURCE_LOADER, "class");
    Velocity.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
    Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogSystem.class.getName());
    Velocity.init();

  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {

    mockAfjManager = new MockAfjManager();

    mockAfjManagerProcess = context.mock(AfjManagerProcess.class);

    webmockSession = new MockHttpSession(null);

    webmockRequest = new MockHttpServletRequest("POST", "/servlet/AfjStartGenerateDelta");

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
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjStartGenerateDeltaServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestBasic() {

    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(false));
        allowing(mockAfjManagerProcess).getSelectedTechPack();
        will(returnValue(mockAFJTechPack));
        allowing(mockAfjManagerProcess).generateAFJTechPackDelta();

        allowing(mockAFJTechPack).getTechPackName();
        will(returnValue(SOME_TECH_PACK));

      }
    });

    webmockSession.setAttribute(AfjStartGenerateDeltaServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    
    final AfjStartGenerateDeltaServlet servlet = new AfjStartGenerateDeltaServlet();
    try {
      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
      final String redirectURL = webmockResponse.getRedirectedUrl();
      assertTrue(View.AFJ_SHOW_PROCESS_STATUS.equals(redirectURL));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjStartGenerateDeltaServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestWhenProcessAlreadyRunning() {

    context.checking(new Expectations() {

      {
        allowing(mockAfjManagerProcess).isRunning();
        will(returnValue(true));
        allowing(mockAfjManagerProcess).getSelectedTechPack();
        will(returnValue(mockAFJTechPack));
        allowing(mockAFJTechPack).getTechPackName();
        will(returnValue(SOME_TECH_PACK));

      }
    });

    webmockSession.setAttribute(AfjStartGenerateDeltaServlet.AFJ_MANAGER_PROCESS, mockAfjManagerProcess);

    
    final AfjStartGenerateDeltaServlet servlet = new AfjStartGenerateDeltaServlet();
    try {
      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
      final String redirectURL = webmockResponse.getRedirectedUrl();
      assertTrue(View.AFJ_SHOW_AFJ_TECH_PACKS.equals(redirectURL));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjStartGenerateDeltaServlet#doHandleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)}.
   */
  @Test
  public void testDoHandleRequestWithInvalidProcess() {

    webmockSession.setAttribute(AfjStartGenerateDeltaServlet.AFJ_MANAGER_PROCESS, null);
    
    final AfjStartGenerateDeltaServlet servlet = new AfjStartGenerateDeltaServlet();
    try {
      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
      fail("Expected Exception did not happen.");
    } catch (Exception e) {
      // this should happen
    }
  }

}
