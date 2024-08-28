/**
 * 
 */
package com.distocraft.dc5000.etl.gui;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import ssc.rockfactory.RockFactory;


/**
 * @author eheijun
 *
 */
public class RockFactoryTypeTest {
  
  private final Mockery context = new JUnit4Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };
  
  private RockFactoryType[] types;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    types = RockFactoryType.getTypes();
    for (RockFactoryType type : types) {
      RockFactory mockRockFactory = context.mock(RockFactory.class, type.getName());
      type.setRockFactory(mockRockFactory);
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.RockFactoryType#allInitialised()}.
   */
  @Test
  public void testAllInitialised() {
    assertThat(RockFactoryType.allInitialised(), is(true));
  }

}
