/**
 * 
 */
package com.distocraft.dc5000.etl.gui.afjupgrade;

import static org.junit.Assert.*;

import org.junit.Test;

import com.distocraft.dc5000.etl.gui.afjupgrade.MessageBean.MessageType;


/**
 * @author eheijun
 *
 */
public class MessageBeanTest {

  private static final String ONELINETEST = "THIS MESSAGE HAS JUST ONE LINE";
  private static final String TWOLINETEST = "THIS MESSAGE" + "\n" + "HAS TWO LINES";
  private static final String TWOLINETEST_EXPECTED = "THIS MESSAGE<br/>HAS TWO LINES";
  private static final String MULTILINETEST = "THIS MESSAGE" + "\n" + "HAS MORE THAN" + "\n" + "TWO LINES";
  private static final String MULTILINETEST_EXPECTED = "THIS MESSAGE<ul><li>HAS MORE THAN<li>TWO LINES</ul>";

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.MessageBean#toString()}.
   */
  @Test
  public void testToString() {
    final MessageBean mb = new MessageBean(ONELINETEST, MessageType.INFO);
    assertTrue(mb.toString().equals(ONELINETEST));
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.MessageBean#isError()}.
   */
  @Test
  public void testIsError() {
    final MessageBean mb = new MessageBean(ONELINETEST, MessageType.ERROR);
    assertTrue(mb.isError());
    assertFalse(mb.isWarning());
    assertFalse(mb.isInfo());
    assertFalse(mb.isRunning());
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.MessageBean#isWarning()}.
   */
  @Test
  public void testIsWarning() {
    final MessageBean mb = new MessageBean(ONELINETEST, MessageType.WARNING);
    assertFalse(mb.isError());
    assertTrue(mb.isWarning());
    assertFalse(mb.isInfo());
    assertFalse(mb.isRunning());
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.MessageBean#isInfo()}.
   */
  @Test
  public void testIsInfo() {
    final MessageBean mb = new MessageBean(ONELINETEST, MessageType.INFO);
    assertFalse(mb.isError());
    assertFalse(mb.isWarning());
    assertTrue(mb.isInfo());
    assertFalse(mb.isRunning());
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.MessageBean#isRunning()}.
   */
  @Test
  public void testIsRunning() {
    final MessageBean mb = new MessageBean(ONELINETEST, MessageType.RUNNING);
    assertFalse(mb.isError());
    assertFalse(mb.isWarning());
    assertFalse(mb.isInfo());
    assertTrue(mb.isRunning());
  }
  
  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.MessageBean#getTextAsHTML()}.
   */
  @Test
  public void testGetTextAsHTML_One() {
    final MessageBean mb = new MessageBean(ONELINETEST, MessageType.ERROR);
    final String result = mb.getTextAsHTML();
    assertTrue(result.equals(ONELINETEST));
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.MessageBean#getTextAsHTML()}.
   */
  @Test
  public void testGetTextAsHTML_Two() {
    final MessageBean mb = new MessageBean(TWOLINETEST, MessageType.ERROR);
    final String result = mb.getTextAsHTML();
    assertTrue(result.equals(TWOLINETEST_EXPECTED));
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.MessageBean#getTextAsHTML()}.
   */
  @Test
  public void testGetTextAsHTML_Three() {
    final MessageBean mb = new MessageBean(MULTILINETEST, MessageType.ERROR);
    final String result = mb.getTextAsHTML();
    assertTrue(result.equals(MULTILINETEST_EXPECTED));
  }

}
