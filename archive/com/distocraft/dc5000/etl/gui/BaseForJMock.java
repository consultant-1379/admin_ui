/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.distocraft.dc5000.etl.gui;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 * @author EEMECOY
 *
 */
public class BaseForJMock {

  protected Mockery mockery = new JUnit4Mockery();

  {
    // we need to mock classes, not just interfaces.
    mockery.setImposteriser(ClassImposteriser.INSTANCE);
  }

}
