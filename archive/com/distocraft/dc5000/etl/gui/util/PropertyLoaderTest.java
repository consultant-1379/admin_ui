/**
 * 
 */
package com.distocraft.dc5000.etl.gui.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.Test;


/**
 * @author eheijun
 *
 */
public class PropertyLoaderTest {

  private static final String SOME_VALUE = "value";

  private static final String SOME_KEY = "key";
  
  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.util.PropertyLoader#getProperties()}.
   */
  @Test
  public void testGetProperties() {
    Properties props = PropertyLoader.getProperties();
    assertThat(props, notNullValue());
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.util.PropertyLoader#reset()}.
   */
  @Test
  public void testReset() {
    PropertyLoader.reset();
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.util.PropertyLoader#getProperty(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testGetProperty() {
    Properties props = PropertyLoader.getProperties();
    String prop = props.getProperty(SOME_KEY);
    assertThat(prop, is(SOME_VALUE));
  }

}
