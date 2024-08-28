/**
 * 
 */
package com.distocraft.dc5000.etl.gui;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.EnvironmentEvents;
import com.distocraft.dc5000.etl.gui.common.EnvironmentMixed;
import com.distocraft.dc5000.etl.gui.common.EnvironmentNone;
import com.distocraft.dc5000.etl.gui.common.EnvironmentStats;
import com.distocraft.dc5000.repository.dwhrep.Tpactivation;
import com.distocraft.dc5000.repository.dwhrep.TpactivationFactory;

/**
 * @author eheijun
 * 
 */
public class EniqAdminuiSessionListener implements HttpSessionListener {

  private static final String ENVIRONMENT = "environment";

  private static Log log = LogFactory.getLog(EniqAdminuiSessionListener.class);

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
   */
  @Override
  public void sessionCreated(final HttpSessionEvent sessionEvent) {

    final HttpSession session = sessionEvent.getSession();
    session.setAttribute(ENVIRONMENT, new EnvironmentStats());
    log.info("System environment is ENIQ Stats.");

  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
   */
  @Override
  public void sessionDestroyed(final HttpSessionEvent sessionEvent) {
    try {
      DbConnectionFactory.getInstance().finalizeConnections(sessionEvent.getSession());
    } catch (SQLException e) {
      log.error("SQL Error when removing session.", e);
    }
  }

}
