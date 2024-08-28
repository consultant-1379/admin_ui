/**
 * 
 */
package com.distocraft.dc5000.etl.gui;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.ENIQServiceStatusInfo;
import com.distocraft.dc5000.etl.gui.config.Configuration;
import com.distocraft.dc5000.etl.gui.config.ConfigurationFactory;
import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.distocraft.dc5000.etl.rock.Meta_databasesFactory;

/**
 * 
 * Database connection initialisation code moved from EtlguiServlet
 * 
 * @author eheijun
 * 
 */
public class DbConnectionFactory {

  private final Log log = LogFactory.getLog(this.getClass());

  public static final String ETL_REP_URL_SESSION_KEY = "etlRepUrl";

  public static final String ETL_SCHEDULER_REFNAME_SESSION_KEY = "etl_scheduler_refname";

  public static final String ETL_SCHEDULER_PORT_SESSION_KEY = "etl_scheduler_port";

  public static final String ETL_SCHEDULER_HOST_SESSION_KEY = "etl_scheduler_host";

  public static final String ETL_ENGINE_REFNAME_SESSION_KEY = "etl_engine_refname";

  public static final String ETL_ENGINE_PORT_SESSION_KEY = "etl_engine_port";

  public static final String ETL_ENGINE_HOST_SESSION_KEY = "etl_engine_host";

  public static final String ETL_REP_DRIVERNAME_SESSION_KEY = "etlRepDrivername";

  public static final String ETL_REP_PASSWORD_SESSION_KEY = "etlRepPassword";

  public static final String ETL_REP_USERNAME_SESSION_KEY = "etlRepUsername";

  private static String statusQuery = "SELECT COUNT(*) FROM META_DATABASES;";

  private static DbConnectionFactory instance;

  private static final String ADMIN_UI_CONN_ID = "AdminUI";

  private static final boolean AUTO_COMMIT_TRUE = true;

  private static final int DEFAULT_ISOLATION_LEVEL = -1;

  private static final int ISOLATION_LEVEL_0 = 0;

  private static final int DBCONNECTION_THREAD_WAIT_MS = 10000;

  private final AtomicInteger connectionsOpened = new AtomicInteger(0);

  private RockFactoryType[] rockFactoryTypes;

  private DbConnectionFactory() {
  }

  public static DbConnectionFactory getInstance() {
    if (instance == null) {
      instance = new DbConnectionFactory();
    }
    return instance;
  }

  public static void setInstance(final DbConnectionFactory _instance) {
    instance = _instance;
  }

  /**
   * Initialises rockFactory connection objects. First ETL repository database is initialised. From here a map of
   * database meta data objects is created of all the configured connection types. If *all* connection objects are
   * initialised successfully then the connection objects are added to the velocity context. If all could not be
   * successfully initialised then the connections are closed and any objects still in session are removed.
   * 
   * @param session
   *          HTTP session object
   * @param context
   *          velocity context
   * @param environment
   *          Stats, Events or some other
   * @return 
   *          array of RockFactoryTypes if all were initialised successfully, otherwise null
   * @throws IOException
   *           if connection object could not be initialised
   * @throws RockException
   *           if connection object could not be initialised
   * @throws SQLException
   *           if connection object could not be initialised
   */
  public RockFactoryType[] initialiseConnections(final HttpSession session) throws SQLException, RockException,
      IOException {

    final RockFactory etlrepRockFactory = initialiseEtlRep(session);
    final RockFactoryType rockEtlRepType = RockFactoryType.ROCK_ETL_REP;
    rockEtlRepType.setRockFactory(etlrepRockFactory);
    DbConnectionCloser.getInstance().addRockFactory(session, rockEtlRepType.getRockFactory(), rockEtlRepType.getName());

    final Map<RockFactoryType, Meta_databases> mdbMap = getMetaDatabaseMap(rockEtlRepType.getRockFactory());

    for (final RockFactoryType type : mdbMap.keySet()) {
      final Meta_databases mdb = mdbMap.get(type);
      final RockFactory rockFactory = initialiseConnection(mdb, type.getName(), session);
      type.setRockFactory(rockFactory);
      DbConnectionCloser.getInstance().addRockFactory(session, type.getRockFactory(), type.getName());
    }

    final boolean allInitialisedSuccessfully = RockFactoryType.allInitialised();

    rockFactoryTypes = RockFactoryType.getTypes();

    if (allInitialisedSuccessfully) {
      log.debug("Databases initialized successfully.");
      ENIQServiceStatusInfo.setRepDBHealth(ENIQServiceStatusInfo.ServiceHealth.Online);
      ENIQServiceStatusInfo.setdwhDBHealth(ENIQServiceStatusInfo.ServiceHealth.Online);
      ENIQServiceStatusInfo.setEtlDBHealth(ENIQServiceStatusInfo.ServiceHealth.Online);
    } else {
      for (final RockFactoryType type : rockFactoryTypes) {
    	  if(type.getRockFactory() == null){
    		  if(type.getName().equals("rockEtlRep") || type.getName().equals("rockEtlRepDba")){
    			  ENIQServiceStatusInfo.setEtlDBHealth(ENIQServiceStatusInfo.ServiceHealth.Offline);
    		  }else if(type.getName().equals("rockDwh") || type.getName().equals("rockDwhDba")){
    			  ENIQServiceStatusInfo.setdwhDBHealth(ENIQServiceStatusInfo.ServiceHealth.Offline);
    		  }else if(type.getName().equals("rockDwhRep")){
    			  ENIQServiceStatusInfo.setRepDBHealth(ENIQServiceStatusInfo.ServiceHealth.Offline);
    		  }
    		  log.debug("Failed to initialise database connection to: " + type.getName() + ". Closing connection and removing it from session.");
    		  session.removeAttribute(type.getName());
    		  closeConnection(type.getRockFactory());
    	  }
      }
    }
    return rockFactoryTypes;
  }

  /**
   * Try to initialise ETL repository database connection. - read properties and push to session - check if ETL
   * repository rock factory is initialised or has closed connections, if so try to initialise it.
   * 
   * TODO: if properties are updated shouldn't the connection be reinitialised ???
   * 
   * @param session
   * @return initialised rockEtlRep or null
   * @throws Exception
   * @throws RockException
   * @throws SQLException
   */
  private RockFactory initialiseEtlRep(final HttpSession session) throws SQLException, RockException, IOException {
    if (!containsEtlRepDatabaseConnectionConfig(session)) {
      log.debug("Initializing etlrep database properties...");
      final Configuration configuration = ConfigurationFactory.getConfiguration();
      updateEtlRepPropertiesInSession(session, configuration.getETLCServerProperties());
    }

    final String rockEtlRepKey = RockFactoryType.ROCK_ETL_REP.getName();
    RockFactory rockEtlRep = (RockFactory) session.getAttribute(rockEtlRepKey);
    if (isNullOrConnectionClosed(rockEtlRep)) {
      final String url = session.getAttribute(ETL_REP_URL_SESSION_KEY).toString();
      final String userName = session.getAttribute(ETL_REP_USERNAME_SESSION_KEY).toString();
      final String password = session.getAttribute(ETL_REP_PASSWORD_SESSION_KEY).toString();
      final String driverName = session.getAttribute(ETL_REP_DRIVERNAME_SESSION_KEY).toString();

      log.debug("Testing connection to etlrep with {" + url + "}  {" + userName + "/" + password + "} {" + driverName
          + "}");
      if (connectionSuccessful(url, userName, password, driverName, statusQuery)) {
        log.debug("Connection successful.");
        rockEtlRep = initialiseRockFactory(url, userName, password, driverName, ISOLATION_LEVEL_0);
        log.debug("Connections opened" + connectionsOpened.incrementAndGet());
        session.setAttribute(rockEtlRepKey, rockEtlRep);
      } else {
        throw new IllegalStateException("Cannot initialise connection to ETL repository database as user '" + userName
            + "' with url '" + url + "'. Please see logs for detailed information.");
      }
    }

    return rockEtlRep;
  }

  /**
   * Checks for the presence of DB connection related config in the specified HttpSession
   * 
   * @see EtlGuiServlet
   * @param session
   * @return true if parameters present otherwise false
   */
  private static boolean containsEtlRepDatabaseConnectionConfig(final HttpSession session) {
    return (session != null && (session.getAttribute(ETL_REP_URL_SESSION_KEY) != null
        && session.getAttribute(ETL_REP_USERNAME_SESSION_KEY) != null
        && session.getAttribute(ETL_REP_PASSWORD_SESSION_KEY) != null && session
          .getAttribute(ETL_REP_DRIVERNAME_SESSION_KEY) != null));
  }

  /**
   * Helper function to store ETLC properties in a HttpSession object. TODO: all property keys should be defined in
   * ETLCServerProperties
   * 
   * @param session
   *          HTTP session
   */
  private static void updateEtlRepPropertiesInSession(final HttpSession session, final Properties etlRepProperties) {
    if (session == null) {
      throw new IllegalArgumentException("trying to update a null session object");
    }
    session.setAttribute(ETL_REP_URL_SESSION_KEY, etlRepProperties.getProperty("ENGINE_DB_URL").trim());
    session.setAttribute(ETL_REP_USERNAME_SESSION_KEY, etlRepProperties.getProperty("ENGINE_DB_USERNAME").trim());
    session.setAttribute(ETL_REP_PASSWORD_SESSION_KEY, etlRepProperties.getProperty("ENGINE_DB_PASSWORD").trim());
    session.setAttribute(ETL_REP_DRIVERNAME_SESSION_KEY, etlRepProperties.getProperty("ENGINE_DB_DRIVERNAME").trim());
    session.setAttribute(ETL_ENGINE_HOST_SESSION_KEY, etlRepProperties.getProperty("ENGINE_HOSTNAME"));
    session.setAttribute(ETL_ENGINE_PORT_SESSION_KEY, etlRepProperties.getProperty("ENGINE_PORT"));
    session.setAttribute(ETL_ENGINE_REFNAME_SESSION_KEY, etlRepProperties.getProperty("ENGINE_REFNAME"));
    session.setAttribute(ETL_SCHEDULER_HOST_SESSION_KEY, etlRepProperties.getProperty("SCHEDULER_HOSTNAME"));
    session.setAttribute(ETL_SCHEDULER_PORT_SESSION_KEY, etlRepProperties.getProperty("SCHEDULER_PORT"));
    session.setAttribute(ETL_SCHEDULER_REFNAME_SESSION_KEY, etlRepProperties.getProperty("SCHEDULER_REFNAME"));
  }

  private boolean isNullOrConnectionClosed(final RockFactory rockFactory) throws SQLException {
    return rockFactory == null || rockFactory.getConnection() == null || rockFactory.getConnection().isClosed();
  }

  /**
   * Tests database connection in a separate thread. Waits a maximum of 15 seconds. Wait time is now 10sec.
   * 
   * @param url
   * @param username
   * @param password
   * @param driverName
   * @return true if connection succeeded, false otherwise.
   */
  private boolean connectionSuccessful(final String url, final String username, final String password,
      final String driverName, final String testQuery) {
    try {
      final StringBuffer statusText = new StringBuffer();
      new DBConnectionThread(statusText, url, username, password, driverName, Thread.currentThread(), testQuery)
          .start();

      synchronized (statusText) {
        try {
          statusText.wait(DBCONNECTION_THREAD_WAIT_MS);
        } catch (final InterruptedException e) {

        }
      }
      return statusText.length() > 0;
    } catch (final Exception e) {
      log.error(e);
      return false;
    }
  }

  /**
   * Initialise rock factory from meta database info object.
   * 
   * @param mdb
   * @param isolationLevel
   *          TODO
   * @return initialised rock factory or null
   * @throws RockException
   * @throws SQLException
   * @throws Exception
   * @throws IllegalArgumentException
   *           if null Meta_database object is passed
   */
  private RockFactory initialiseRockFactory(final Meta_databases mdb, final int isolationLevel) throws SQLException,
      RockException {
    if (mdb == null) {
      throw new IllegalArgumentException("Meta database object may not be null");
    }
    return initialiseRockFactory(mdb.getConnection_string(), mdb.getUsername(), mdb.getPassword(),
        mdb.getDriver_name(), isolationLevel);
  }

  /**
   * Initialise rock factory with specified JDBC connection parameters
   * 
   * @param url
   * @param userName
   * @param password
   * @param driverName
   * @param isolationLevel
   * @return initialised rock factory or null
   * @throws SQLException
   *           connection could not be initialised
   * @throws RockException
   *           wraps exceptions caused by JDBC driver initialisation failure
   */
  private RockFactory initialiseRockFactory(final String url, final String userName, final String password,
      final String driverName, final int isolationLevel) throws SQLException, RockException {
    log.debug("Initialising rockFactory with {" + url + "}  {" + userName + "/" + password + "} {" + driverName + "}");

    RockFactory rockFactory = null;
    try {
      rockFactory = new RockFactory(url, userName, password, driverName, ADMIN_UI_CONN_ID, AUTO_COMMIT_TRUE,
          isolationLevel);
      return rockFactory;
    } catch (final SQLException e) {
    	if(e.getSQLState().equals("08W03")){
    		if(url.contains("repdb:")){
    			log.error(" Connection limit exceeds for database: " + ENIQServiceStatusInfo.getRepDBName());
    			ENIQServiceStatusInfo.setRepDBConnState(ENIQServiceStatusInfo.ConnState.LIMIT_EXCEED);
    		}
    		if(url.contains("dwhdb:")){
    			log.error(" Connection limit exceeds for database: " + ENIQServiceStatusInfo.getDwhDBName());
    			ENIQServiceStatusInfo.setDwhDBConnState(ENIQServiceStatusInfo.ConnState.LIMIT_EXCEED);
    		}
    	}else{
    		ENIQServiceStatusInfo.setRepDBConnState(ENIQServiceStatusInfo.ConnState.LIMIT_OK);
    		ENIQServiceStatusInfo.setDwhDBConnState(ENIQServiceStatusInfo.ConnState.LIMIT_OK);
    	}
      log.error("Error initialising connection to:" + url, e);
      throw e;
    } catch (final RockException e) {
      log.error("Error creating RockFactory:" + url, e);
      throw e;
    }
  }

  /**
   * Create a map of metadatabase objects from the ETL repository.
   * 
   * @param rockEtlrep
   *          ETL repository conncetion object
   * @param currentEnvironment
   * @return map of metadatabase objects
   * @throws RockException
   * @throws SQLException
   */
  private Map<RockFactoryType, Meta_databases> getMetaDatabaseMap(final RockFactory rockEtlrep) throws SQLException,
      RockException {

    if (rockEtlrep == null) {
      throw new IllegalStateException("etlrep connection not initialised");
    }

    final Meta_databasesFactory mdbFactory = new Meta_databasesFactory(rockEtlrep, new Meta_databases(rockEtlrep));

    final Map<RockFactoryType, Meta_databases> mdbMap = new HashMap<RockFactoryType, Meta_databases>();

    for (final Object o : mdbFactory.get()) {
      final Meta_databases mdb = (Meta_databases) o;
      if (isTypeAndConnection(mdb, "dba", "etlrep")) {
        mdbMap.put(RockFactoryType.ROCK_ETL_REP_DBA, mdb);
      } else if (isTypeAndConnection(mdb, "dba", "dwh_coor") || isTypeAndConnection(mdb, "dba", "dwh")) {
        mdbMap.put(RockFactoryType.ROCK_DWH_DBA, mdb);
      } else if (isTypeAndConnection(mdb, "user", "dwh_coor") || isTypeAndConnection(mdb, "user", "dwh")) {
        mdbMap.put(RockFactoryType.ROCK_DWH, mdb);
      } else if (isTypeAndConnection(mdb, "user", "dwhrep")) {
        mdbMap.put(RockFactoryType.ROCK_DWH_REP, mdb);
      // confusing warning removed 9.9.2011 by eheijun  
      //} else {
      //  log.warn("Unmapped Meta_database object: '" + mdb.getType_name() + "' - '" + mdb.getConnection_name() + "'");
      }
    }
    return mdbMap;
  }

  /**
   * Checks that the meta database object represents a particular connection. TODO: this belongs as a method on
   * Meta_databases i.e., boolean isTypeAndConnection(final String theType, final String connection) { .. } usage:
   * mdb.isTypeAndConnection("user","dwh");
   * 
   * @param mdb
   * @param theType
   * @param theConnection
   * @return
   */
  private boolean isTypeAndConnection(final Meta_databases mdb, final String theType, final String theConnection) {
    if (mdb == null) {
      return false;
    }
    final String typeName = mdb.getType_name();
    final String connectionName = mdb.getConnection_name();
    return (typeName != null && connectionName != null && typeName.equalsIgnoreCase(theType) && connectionName
        .equalsIgnoreCase(theConnection));
  }

  /**
   * Initialise a specific database connection object (rockFactory) from meta data if needed.
   * 
   * Only initialise if not already initialised in session or connection is closed.
   * 
   * @param mdb
   *          meta data for the connection
   * @param sessionKey
   * @param session
   *          http session
   * @return initialised rock factory object
   * @throws SQLException
   */
  private RockFactory initialiseConnection(final Meta_databases mdb, final String sessionKey, final HttpSession session)
      throws SQLException {

    // If in session and connection closed then re-initialise
    RockFactory rockFactory = (RockFactory) session.getAttribute(sessionKey);

    if (isNullOrConnectionClosed(rockFactory)) {
      try {
        session.removeAttribute(sessionKey);
        rockFactory = initialiseRockFactory(mdb, DEFAULT_ISOLATION_LEVEL);
        log.debug(sessionKey + " - connections opened " + (connectionsOpened.incrementAndGet()));
        session.setAttribute(sessionKey, rockFactory);
      } catch (final Exception ex) {
        log.info("Could not create database connection to '" + sessionKey + "' database.", ex);
        session.setAttribute("dbName", sessionKey);
      }
    }
    return rockFactory;
  }

  private void closeAllConnections(final RockFactoryType[] rockFactoryTypes) {
    log.debug("Closing all connections...");
    for (final RockFactoryType type : rockFactoryTypes) {
      closeConnection(type.getRockFactory());
    }
  }

  private void closeConnection(final RockFactory rockFactory) {
    if (rockFactory != null) {
      try {
        if (rockFactory.getConnection() != null) {
          rockFactory.getConnection().close();
          connectionsOpened.decrementAndGet();
        }
      } catch (final SQLException e) {
        log.warn("Error closing connection: " + rockFactory.getDbURL());
      }
    }
  }
  
  /**
   * Closes all open connections of the given session
   * @param session
   * @throws SQLException
   */
  public void finalizeConnections(final HttpSession session) throws SQLException {
    final Enumeration<String> attributeNames = session.getAttributeNames();
    while (attributeNames.hasMoreElements()) {
      final String attributeName = attributeNames.nextElement();
      final Object attribute = session.getAttribute(attributeName);
      if (attribute instanceof RockFactory) {
        final RockFactory rockFactory = (RockFactory) attribute;
        if (!isNullOrConnectionClosed(rockFactory)) {
          closeConnection(rockFactory);
        }
      }
    }
  }
  

  /**
   * Used to test a connection to the specified database. A connection is opened to the specified database and a test
   * query is run the output of which is appended to a string buffer.
   */
  private class DBConnectionThread extends Thread { 

    // private static final String DB_CONNECTION_THREAD_TEST_QUERY = "select getdate();";

    private final StringBuffer statusText;

    private final String url;

    private final String username;

    private final String password;

    private final String driverName;

    private final Thread parentThread;

    private final String testQuery;

    DBConnectionThread(final StringBuffer statusText, final String url, final String username, final String password,
        final String driverName, final Thread parentThread, final String testQuery) {
      this.statusText = statusText;
      this.url = url;
      this.username = username;
      this.password = password;
      this.driverName = driverName;
      this.parentThread = parentThread;
      this.testQuery = testQuery;
    }

    @Override
    public void run() { 

      try {
        final Class<?> clazz = Class.forName(driverName);
        if (clazz != null) {
          final Properties p = new Properties();
          p.put("user", username);
          p.put("password", password);
          p.put("REMOTEPWD", ",,CON=AdminUICheck");
          final Connection connection = DriverManager.getConnection(url, p);
          try {
            connection.setAutoCommit(false);
            final Statement stmt = connection.createStatement();
            try {
              final ResultSet rs = stmt.executeQuery(testQuery);
              try {
                if (rs.next()) {
                  statusText.append(rs.getString(1));
                }
              } finally {
                rs.close();
              }
            } finally {
              stmt.close();
            }
            connection.commit();
            parentThread.interrupt();
          } catch (final SQLException e) {
            connection.rollback();
            log.error(e);
          } finally {
            connection.setAutoCommit(true);
            connection.close();
          }
        }
      } catch (ClassNotFoundException e) {
        log.error(e);
      } catch (SQLException e) {
        log.error(e);
      } catch (Exception e) {
        log.error(e);
      }
    }
  }

}
