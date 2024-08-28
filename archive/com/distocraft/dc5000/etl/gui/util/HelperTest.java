/**
 * 
 */
package com.distocraft.dc5000.etl.gui.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;


/**
 * @author eheijun
 *
 */
public class HelperTest {

  private Mockery context = new Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };
  
  private Context mockContext;
  private Context mockSubContext;
  
  @Before
  public final void setUp() throws Exception {
    mockContext = context.mock(Context.class, "context");
    mockSubContext = context.mock(Context.class, "subcontext");
    Helper.setContext(mockContext);    
  }
  
  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.util.Helper#getEnvEntryString(java.lang.String)}.
   * @throws NamingException 
   */
  @Test
  public void testGetEnvEntryString() throws NamingException {
    context.checking(new Expectations() {

      {
        allowing(mockContext).lookup(with(any(String.class)));
        will(returnValue(mockSubContext));
        allowing(mockSubContext).lookup("x");
        will(returnValue("y"));
      }
    });
    String res = Helper.getEnvEntryString("x");
    assertThat(res, is("y"));
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.util.Helper#getEnvEntryInt(java.lang.String)}.
   * @throws NamingException 
   */
  @Test
  public void testGetEnvEntryInt() throws NamingException {
    context.checking(new Expectations() {

      {
        allowing(mockContext).lookup(with(any(String.class)));
        will(returnValue(mockSubContext));
        allowing(mockSubContext).lookup("x");
        will(returnValue("1"));
      }
    });
    Integer res = Helper.getEnvEntryInt("x");
    assertThat(res, is(1));
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.util.Helper#isNotEmpty(java.lang.String)}.
   */
  @Test
  public void testIsNotEmpty() {
    boolean res = Helper.isNotEmpty("");
    assertThat(res, is(false));
  }

}
