/**
 * 
 */
package com.distocraft.dc5000.etl.gui.config;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.distocraft.dc5000.etl.gui.util.Helper;


/**
 * @author eheijun
 *
 */
public class DefaultConfigurationTest {

  private final Mockery context = new JUnit4Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  private Context mockContext;
  private Context mockSubContext;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    File homeDir = new File(System.getProperty("user.dir"));
    /* Creating static property file */
    File sp = new File(homeDir, "service_names");
    try {
      PrintWriter pw = new PrintWriter(new FileWriter(sp));
      pw.print("159.107.220.37::eniqserver::dwhdb");
      pw.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    /* Setting the system property for static property file */
    System.setProperty("CONF_DIR", homeDir.getPath());
    
    mockContext = context.mock(Context.class, "context");
    mockSubContext = context.mock(Context.class, "subcontext");
    Helper.setContext(mockContext);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.config.DefaultConfiguration#getServiceNames()}.
   * @throws NamingException 
   */
  @Test
  public void testGetServiceNames() throws NamingException {
    
    context.checking(new Expectations() {

      {
        allowing(mockContext).lookup(with(any(String.class)));
        will(returnValue(mockSubContext));
        allowing(mockSubContext).lookup(DefaultConfiguration.HOSTS_FILE);
        will(returnValue("service_names"));
      }
    });
    
    DefaultConfiguration configuration = new DefaultConfiguration();
    ServiceNames serviceNames = configuration.getServiceNames();
    assertThat(serviceNames, notNullValue());
    Set<String> hosts = serviceNames.getHosts();
    assertTrue(hosts.contains("eniqserver"));
  }

}
