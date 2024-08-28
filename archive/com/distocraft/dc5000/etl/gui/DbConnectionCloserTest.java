/**
 * 
 */
package com.distocraft.dc5000.etl.gui;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.DbConnectionCloser;


/**
 * @author eheijun
 *
 */
public class DbConnectionCloserTest {

  private final Mockery context = new JUnit4Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };
  
  private MockServletContext webmockServletContext;
  
  private MockHttpSession webmockSession;

  private Connection mockConnection;
  
  private RockFactory mockRockFactory;

  private DbConnectionCloser connectionCloser;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    webmockServletContext = new MockServletContext(); 
    webmockSession = new MockHttpSession(webmockServletContext);
    mockRockFactory = context.mock(RockFactory.class);
    mockConnection = context.mock(Connection.class);
    webmockSession.setAttribute("rockDwh", mockRockFactory);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    DbConnectionCloser.setInstance(null);
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.DbConnectionCloser}.
   * @throws SQLException 
   * @throws InterruptedException 
   */
  @Test
  public void testCreateAndCloseConnection() throws SQLException, InterruptedException {
    
    connectionCloser = DbConnectionCloser.getInstance(90000, 30000);
    context.checking(new Expectations() {
      {
        allowing(mockRockFactory).getConnection();
        will(returnValue(mockConnection));
        // before close return false 
        one(mockConnection).isClosed();
        will(returnValue(false));
        allowing(mockConnection).close();
        // after close return true 
        one(mockConnection).isClosed();
        will(returnValue(true));
      }
    });
    connectionCloser.addRockFactory(webmockSession, mockRockFactory, "rockDwh");
    connectionCloser.stop();
    RockFactory result = (RockFactory) webmockSession.getAttribute("rockDwh");
    assertThat(result, is(mockRockFactory));
    assertThat(result.getConnection().isClosed(), is(true));
  }
  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.DbConnectionCloser#getInstance(int, int)}.
   * @throws SQLException 
   * @throws InterruptedException 
   */
  @Test
  public void testCreateConnectionAndLetTimerDestroyIt() throws SQLException, InterruptedException {
    
    connectionCloser = DbConnectionCloser.getInstance(500, 250);
    context.checking(new Expectations() {
      {
        allowing(mockRockFactory).getConnection();
        will(returnValue(mockConnection));
        // before close return false 
        one(mockConnection).isClosed();
        will(returnValue(false));
        allowing(mockConnection).close();
        // after close return true 
        one(mockConnection).isClosed();
        will(returnValue(true));
      }
    });
    connectionCloser.addRockFactory(webmockSession, mockRockFactory, "rockDwh");
    // wait just second that connection closer task has had time to perform action
    Thread.sleep(1000);
    RockFactory result = (RockFactory) webmockSession.getAttribute("rockDwh");
    assertThat(result, nullValue());
  }

}
