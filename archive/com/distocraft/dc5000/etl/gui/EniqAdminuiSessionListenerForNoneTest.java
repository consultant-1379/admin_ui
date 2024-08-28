/**
 * 
 */
package com.distocraft.dc5000.etl.gui;

import static org.junit.Assert.*;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.Environment;
import com.distocraft.dc5000.etl.gui.common.EnvironmentNone;
import com.ericsson.eniq.common.testutilities.UnitDatabaseTestCase;


/**
 * @author eheijun
 *
 */
public class EniqAdminuiSessionListenerForNoneTest extends UnitDatabaseTestCase {

  private final Mockery context = new JUnit4Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };
  
  private HttpSessionEvent mockEvent;

  private MockHttpSession mockSession;
  
  
  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    System.setProperty(CONF_DIR, System.getProperty(USER_DIR));
    setup(TestType.unit);
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
    final DbConnectionCloser mockConnectionCloser = context.mock(DbConnectionCloser.class);
    
    mockEvent = context.mock(HttpSessionEvent.class);
    
    mockSession = new MockHttpSession();
    
    context.checking(new Expectations() {

      {
        allowing(mockConnectionCloser).addRockFactory(with(any(HttpSession.class)), with(any(RockFactory.class)), with(any(String.class)));

        allowing(mockEvent).getSession();
        will(returnValue(mockSession));
        
      }
    });
    
    DbConnectionCloser.setInstance(mockConnectionCloser);    
    
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    DbConnectionCloser.setInstance(null);    
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.EniqAdminuiSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)}.
   */
  @Test
  public void testSessionCreated() {
    EniqAdminuiSessionListener listener = new EniqAdminuiSessionListener();
    listener.sessionCreated(mockEvent);
    Environment environment = (Environment) mockSession.getAttribute("environment");
    assertTrue(environment instanceof EnvironmentNone);
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.EniqAdminuiSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)}.
   */
  @Test
  public void testSessionDestroyed() {
    EniqAdminuiSessionListener listener = new EniqAdminuiSessionListener();
    listener.sessionDestroyed(mockEvent);
  }

}
