/**
 * 
 */
package com.distocraft.dc5000.etl.gui;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import com.ericsson.eniq.common.testutilities.UnitDatabaseTestCase;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;


/**
 * @author eheijun
 *
 */
public class DbConnectionFactoryTest extends UnitDatabaseTestCase {
  
  private Mockery context = new Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  private static final String CONF_DIR = "CONF_DIR";

  private static final String USER_DIR = "user.dir";

  private MockServletContext webmockServletContext;
  
  private MockHttpSession webmockSession;

  public DbConnectionCloser mockDbConnectionCloser;
  
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
    System.clearProperty(CONF_DIR);
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    webmockServletContext = new MockServletContext(); 
    webmockSession = new MockHttpSession(webmockServletContext);

    mockDbConnectionCloser = context.mock(DbConnectionCloser.class);
    
    DbConnectionCloser.setInstance(mockDbConnectionCloser);    
    
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.DbConnectionFactory#getInstance()}.
   * @throws IOException 
   * @throws RockException 
   * @throws SQLException 
   */
  @Test
  public void test() throws SQLException, RockException, IOException {
    
    context.checking(new Expectations() {

      {
        allowing(mockDbConnectionCloser).addRockFactory(with(any(HttpSession.class)), with(any(RockFactory.class)), with(any(String.class)));
      }
    });
    
    RockFactoryType[] rockFactoryTypes = DbConnectionFactory.getInstance().initialiseConnections(webmockSession);
    for (RockFactoryType rockFactoryType : rockFactoryTypes) {
      assertThat(rockFactoryType.getRockFactory().getConnection().isClosed(), is(false));
    }
    DbConnectionFactory.getInstance().finalizeConnections(webmockSession);    
    for (RockFactoryType rockFactoryType : rockFactoryTypes) {
      assertThat(rockFactoryType.getRockFactory().getConnection().isClosed(), is(true));
    }
  }

}
