/**
 * 
 */
package com.distocraft.dc5000.etl.gui.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author eheijun
 *
 */
public class ConfigurationFactoryTest {

  private final Mockery context = new JUnit4Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  private Configuration mockInstance;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    mockInstance = context.mock(Configuration.class);
    ConfigurationFactory.setConfiguration(mockInstance);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    ConfigurationFactory.setConfiguration(null);
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.config.ConfigurationFactory#getConfiguration()}.
   */
  @Test
  public void testGetConfiguration() {
    Configuration conf = ConfigurationFactory.getConfiguration();
    assertThat(conf, notNullValue());
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.config.ConfigurationFactory#setConfiguration(com.distocraft.dc5000.etl.gui.config.Configuration)}.
   */
  @Test
  public void testSetConfiguration() {
    Configuration conf = ConfigurationFactory.getConfiguration();
    assertThat(conf, is(mockInstance));
  }

}
