/**
 * 
 */
package com.distocraft.dc5000.etl.gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.distocraft.dc5000.etl.gui.util.Helper;

/**
 * @author eheijun
 * 
 */
public class EniqAdminuiListener implements ServletContextListener {

  private static final String DB_CONNECTION_TEST_PERIOD = "dbConnectionTestPeriod";

  private static final String DB_CONNECTION_TIMEOUT = "dbConnectionTimeout";

  static final String EXECUTOR_SERVICE = "ExecutorService";

  private static final String CONFIG_FILE_DIR_NAME = "conffiles";
  
  private static final String ADMINUI_PROPERTIES = "adminui.properties";
  
  private static Log log = LogFactory.getLog(EniqAdminuiListener.class);

  private final DbConnectionCloser dbConnectionCloser;

  private final ExecutorService executorService;

  /**
   * This is same as Executors.DefaultThreadFactory but it creates threads as daemon
   */
  private static class AdminUIDefaultThreadFactory implements ThreadFactory {

    static final AtomicInteger poolNumber = new AtomicInteger(1);

    final ThreadGroup group;

    final AtomicInteger threadNumber = new AtomicInteger(1);

    final String namePrefix;

    private AdminUIDefaultThreadFactory() {
      final SecurityManager s = System.getSecurityManager();
      group = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
      namePrefix = "adminui-pool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread(final Runnable r) {
      final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
      // if (t.isDaemon()) {
      // t.setDaemon(false);
      // }
      if (!t.isDaemon()) {
        t.setDaemon(true);
      }
      if (t.getPriority() != Thread.NORM_PRIORITY) {
        t.setPriority(Thread.NORM_PRIORITY);
      }
      return t;
    }

  }

  /**
   * Creates threadPool which allows only one thread execution at a time. New threads are created as daemons.
   * Also creates DbConnectionCloser instance to be sure that no database connections are left running
   */
  public EniqAdminuiListener() {
    executorService = Executors.newSingleThreadExecutor(new AdminUIDefaultThreadFactory());
    dbConnectionCloser = DbConnectionCloser.getInstance(Helper.getEnvEntryInt(DB_CONNECTION_TIMEOUT),
        Helper.getEnvEntryInt(DB_CONNECTION_TEST_PERIOD));
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
   */
  @Override
  public void contextInitialized(final ServletContextEvent event) {
    
    final StringBuilder currentRootPath = new StringBuilder(event.getServletContext().getRealPath(Helper.PATHSEPERATOR));
    final StringBuilder configFilePath = currentRootPath.append(Helper.getEnvEntryString(CONFIG_FILE_DIR_NAME)).append(Helper.PATHSEPERATOR);
    final StringBuilder loggerPropertiesFile = configFilePath.append(ADMINUI_PROPERTIES);
    
    PropertyConfigurator.configure(loggerPropertiesFile.toString());
    log.info("Log4j logging initialized using \"" + loggerPropertiesFile.toString() + "\" file.");
    
    event.getServletContext().setAttribute(EXECUTOR_SERVICE, executorService);
    log.debug("Threadpool ready to be used.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
   */
  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    event.getServletContext().setAttribute(EXECUTOR_SERVICE, null);
    try {
      executorService.shutdownNow();
      log.debug("Threadpool released.");
    } catch (Exception e) {
      log.error("Failed to shutdown AdminUI threadpool.", e);
    }
    try {
      dbConnectionCloser.stop();
      log.debug("Database connections released.");
    } catch (Exception e) {
      log.error("Failed to shutdown database connections.", e);
    }
  }

}
