/**
 * 
 */
package com.distocraft.dc5000.etl.gui;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContextEvent;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;


/**
 * @author eheijun
 *
 */
public class EniqAdminuiListenerTest {

  private final Mockery context = new JUnit4Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };
  
  private ServletContextEvent mockEvent;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    mockEvent = context.mock(ServletContextEvent.class);
    final MockServletContext mockServletContext = new MockServletContext();

    final DbConnectionCloser mockConnectionCloser = context.mock(DbConnectionCloser.class);
    DbConnectionCloser.setInstance(mockConnectionCloser);
    
    context.checking(new Expectations() {

      {
        allowing(mockEvent).getServletContext();
        will(returnValue(mockServletContext));
        allowing(mockConnectionCloser).stop();
      }
    });
  }
  
  @Test
  public void testEniqAdminuiListener() {
    new EniqAdminuiListener();
    assertThat(DbConnectionCloser.getInstance(), notNullValue());
  }
  
  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.EniqAdminuiListener#contextInitialized(javax.servlet.ServletContextEvent)}.
   */
  @Test
  public void testContextInitialized() {
    final EniqAdminuiListener listener = new EniqAdminuiListener();
    try {
      listener.contextInitialized(mockEvent);
      ExecutorService pool = (ExecutorService) mockEvent.getServletContext().getAttribute(EniqAdminuiListener.EXECUTOR_SERVICE);
      assertFalse(pool == null);
      pool.execute(new Runnable() {

        @Override
        public void run() {
          // do nothing
        }
        
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.EniqAdminuiListener#contextDestroyed(javax.servlet.ServletContextEvent)}.
   */
  @Test
  public void testContextDestroyed() {
    final EniqAdminuiListener listener = new EniqAdminuiListener();
    try {
      listener.contextDestroyed(mockEvent);
      assertTrue(mockEvent.getServletContext().getAttribute(EniqAdminuiListener.EXECUTOR_SERVICE) == null);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

}
