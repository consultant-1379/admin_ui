package com.distocraft.dc5000.etl.gui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ssc.rockfactory.RockFactory;

public class DbConnectionCloser {

  private final Log log = LogFactory.getLog(this.getClass()); 

  private static DbConnectionCloser instance;

  private Timer timer;

  private Map<String, HttpSession> sessions = new HashMap<String, HttpSession>();

  private Map<String, RockFactory> sessionRocks = new HashMap<String, RockFactory>();

  private Object lock = new Object();

  private DbConnectionCloser(final int timeout, final int period) {
    timer = new Timer();
    timer.schedule(new DbConnectionCloseTask(timeout), period, period);
  }

  static DbConnectionCloser getInstance(final int timeout, final int period) {
    if (instance == null) {
      instance = new DbConnectionCloser(timeout, period);
    }
    return instance;
  }
  
  public static DbConnectionCloser getInstance() {
    return instance;
  }
  
  public static void setInstance(DbConnectionCloser _instance) {
    instance = _instance;
  }

  public void addRockFactory(final HttpSession session, final RockFactory rockFactory, final String rockFactoryId) {
	  synchronized (lock) {
    if (rockFactory != null) {
      if (!sessions.containsKey(session.getId())) {
        sessions.put(session.getId(), session);
      }
      sessionRocks.put(session.getId() + rockFactoryId, rockFactory);
    }
  }
  }
  
  private void closeAllConnections() {
    log.debug("System shutting down - closing all db connections");
    final Iterator<String> it = sessionRocks.keySet().iterator();
    while (it.hasNext()) {
      final String id = it.next();
      RockFactory rf = (RockFactory) sessionRocks.get(id);
      try {
        if (rf != null) {
          if (rf.getConnection() == null || rf.getConnection().isClosed()) {
            rf = null;
          } else {
            try {
              rf.getConnection().close();
            } catch (Exception e) {
              log.info("Problems with db connection closing", e);
            }
          }
        }
      } catch (SQLException se) {
        log.info("Could not close database connection", se);
      }
    }
  }

  public void stop() {

    synchronized (lock) {
    if (timer != null) 
	{   
    timer.cancel();
	}
    timer = null;

    if ((sessions != null ) && (sessionRocks != null ))
    {
    closeAllConnections();
    }
    sessions = null;
    sessionRocks = null;

	}
  }

  private final class DbConnectionCloseTask extends TimerTask {
    
    final int timeout;
    
    public DbConnectionCloseTask(final int timeout) {
      this.timeout = timeout;
    }

    public void run() {
      synchronized (lock) {
      final ArrayList<String> removableSessions = new ArrayList<String>();
      Iterator<String> it = sessions.keySet().iterator();
      while (it.hasNext()) {
        final long currentTime = System.currentTimeMillis();
        final String id = it.next();
        final HttpSession ses = sessions.get(id);
        boolean closeRocks = false;
        if (ses == null) {
          closeRocks = true;
          removableSessions.add(id);
        } else {
          try {
            if (ses.getLastAccessedTime() + timeout < currentTime) {
              doConnectClose(ses);
              removableSessions.add(id);
            }
          } catch (IllegalStateException se) {
            closeRocks = true;
            removableSessions.add(id);
          }
        }
        if (closeRocks) {
          it.remove();
          RockFactory rockDwh = null;
          RockFactory rockDwhRep = null;
          RockFactory rockEtlRep = null;
          RockFactory rockEtlRepDba = null;
          RockFactory rockDwhDba = null;
          if (sessionRocks.get(id + "rockDwh") != null) {
            rockDwh = sessionRocks.get(id + "rockDwh");
          }
          if (sessionRocks.get(id + "rockDwhRep") != null) {
            rockDwhRep = sessionRocks.get(id + "rockDwhRep");
          }
          if (sessionRocks.get(id + "rockEtlRep") != null) {
            rockEtlRep = sessionRocks.get(id + "rockEtlRep");
          }
          if (sessionRocks.get(id + "rockEtlRepDba") != null) {
            rockEtlRepDba = sessionRocks.get(id + "rockEtlRepDba");
          }
          if (sessionRocks.get(id + "rockDwhDba") != null) {
            rockDwhDba = sessionRocks.get(id + "rockDwhDba");
          }
          /*
           * Iterator it2 = sessionRocks.keySet().iterator(); while (it2.hasNext()) { String id2 = (String)it2.next();
           * System.out.println("Session rocks (on remove):"+id2); }
           */
          closeAllConnections(rockDwh, rockDwhRep, rockEtlRep, rockEtlRepDba, rockDwhDba);
          sessionRocks.remove(id + "rockDwh");
          sessionRocks.remove(id + "rockDwhRep");
          sessionRocks.remove(id + "rockEtlRep");
          sessionRocks.remove(id + "rockEtlRepDba");
          sessionRocks.remove(id + "rockDwhDba");
        }
      }
      it = removableSessions.iterator();
      while (it.hasNext()) {
        final String id2 = (String) it.next();
        sessions.remove(id2);
      }
      }
    }

    private void doConnectClose(final HttpSession session) {

      RockFactory rockDwh = null;
      RockFactory rockDwhRep = null;
      RockFactory rockEtlRep = null;
      RockFactory rockEtlRepDba = null;
      RockFactory rockDwhDba = null;
      final String id = session.getId();
      if (session.getAttribute("rockDwh") != null) {
        rockDwh = (RockFactory) session.getAttribute("rockDwh");
      }
      if (session.getAttribute("rockDwhRep") != null) {
        rockDwhRep = (RockFactory) session.getAttribute("rockDwhRep");
      }
      if (session.getAttribute("rockEtlRep") != null) {
        rockEtlRep = (RockFactory) session.getAttribute("rockEtlRep");
      }
      if (session.getAttribute("rockEtlRepDba") != null) {
        rockEtlRepDba = (RockFactory) session.getAttribute("rockEtlRepDba");
      }
      if (session.getAttribute("rockDwhDba") != null) {
        rockDwhDba = (RockFactory) session.getAttribute("rockDwhDba");
      }
      log.debug("Timeout occured - db connections closing for session " + session.getId());
      // System.out.println("Timeout occured - db connections closing for session "+session.getId());
      session.removeAttribute("rockDwhRep");
      session.removeAttribute("rockDwh");
      session.removeAttribute("rockDwhDba");
      session.removeAttribute("rockEtlRep");
      session.removeAttribute("rockEtlRepDba");
      closeAllConnections(rockDwh, rockDwhRep, rockEtlRep, rockEtlRepDba, rockDwhDba);
      // sessions.remove(session.getId());
      sessionRocks.remove(id + "rockDwh");
      sessionRocks.remove(id + "rockDwhRep");
      sessionRocks.remove(id + "rockEtlRep");
      sessionRocks.remove(id + "rockEtlRepDba");
      sessionRocks.remove(id + "rockDwhDba");
    }
  }

  private void closeAllConnections(RockFactory rockDwh, RockFactory rockDwhRep, RockFactory rockEtlRep,
      RockFactory rockEtlRepDba, RockFactory rockDwhDba) {
    log.debug("Closing all connections...");
    // System.out.println("Closing all connections...");

    try {
      if (rockDwhRep != null) {
        if (rockDwhRep.getConnection() == null || rockDwhRep.getConnection().isClosed()) {
          rockDwhRep = null;
        } else {
          try {
            rockDwhRep.getConnection().close();
          } catch (Exception e) {
            log.info("Problems with dwhrep db connection closing", e);
            // System.out.println("Problems with dwhrep db connection closing"+e.toString());
          }
        }
      }
      if (rockDwh != null) {
        if (rockDwh.getConnection() == null || rockDwh.getConnection().isClosed()) {
          rockDwh = null;
        } else {
          try {
            rockDwh.getConnection().close();
          } catch (Exception e) {
            log.info("Problems with dwh db connection closing", e);
            // System.out.println("Problems with dwh db connection closing"+e.toString());
          }
        }
      }
      if (rockDwhDba != null) {
        if (rockDwhDba.getConnection() == null || rockDwhDba.getConnection().isClosed()) {
          rockDwhDba = null;
        } else {
          try {
            rockDwhDba.getConnection().close();
          } catch (Exception e) {
            log.info("Problems with dwhdba db connection closing", e);
            // System.out.println("Problems with dwhdba db connection closing"+e.toString());
          }
        }
      }

      if (rockEtlRepDba != null) {
        if (rockEtlRepDba.getConnection() == null || rockEtlRepDba.getConnection().isClosed()) {
          rockEtlRepDba = null;
        } else {
          try {
            rockEtlRepDba.getConnection().close();
          } catch (Exception e) {
            log.info("Problems with etlrepdba db connection closing", e);
            // System.out.println("Problems with etlrepdba db connection closing"+e.toString());
          }
        }
      }

      if (rockEtlRep != null) {
        if (rockEtlRep.getConnection() == null || rockEtlRep.getConnection().isClosed()) {
          rockEtlRep = null;
        } else {
          try {
            rockEtlRep.getConnection().close();
          } catch (Exception e) {
            log.info("Problems with etlrep db connection closing", e);
            // System.out.println("Problems with etlrep db connection closing"+e.toString());
          }
        }
      }
    } catch (Exception e) {
      log.info("Could not close database connection", e);
      // System.out.println("Could not close database connection"+e.toString());
    }
  }
}
