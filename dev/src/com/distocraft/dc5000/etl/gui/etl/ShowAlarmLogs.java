/*
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package com.distocraft.dc5000.etl.gui.etl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.CalSelect;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved. <br>
 * Shows alarm interface history. Uses dc5000etlrep and dc5000dwhrep databases.
 * 
 * @author Jaakko Melantie
 */

public class ShowAlarmLogs extends EtlguiServlet {

  private Log log = LogFactory.getLog(this.getClass()); // general logger

  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) {

    List AlarmInterfaces = null; // contains all alarm interfaces
    Template outty = null;

    final String page = "alarmHistory.vm"; // show this page if nothing else is
    // stated
    HttpSession session = request.getSession(false);

    Connection connErep = ((RockFactory) ctx.get("rockEtlRep")).getConnection();
    Connection connDrep = ((RockFactory) ctx.get("rockDwhRep")).getConnection();

    // get the current date (is used at the UI, if none given)
    DbCalendar calendar = new DbCalendar();

    // date parameters
    String year_1 = request.getParameter("year_1");
    String month_1 = request.getParameter("month_1");
    String day_1 = request.getParameter("day_1");

    // other parameters
    String alarmInterfaceName = request.getParameter("interfaceid");
    String status = request.getParameter("status");
    final String button = request.getParameter("Search");

    // session details
    if (year_1 != null) {
      session.setAttribute("year", year_1);
    } else if (session.getAttribute("year") != null) {
      year_1 = session.getAttribute("year").toString();
    } else {
      session.setAttribute("year", calendar.getYearString());
      year_1 = calendar.getYearString();
    }

    if (month_1 != null) {
      session.setAttribute("month", month_1);
    } else if (session.getAttribute("month") != null) {
      month_1 = session.getAttribute("month").toString();
    } else {
      session.setAttribute("month", calendar.getMonthString());
      month_1 = calendar.getMonthString();
    }

    if (day_1 != null) {
      session.setAttribute("day", day_1);
    } else if (session.getAttribute("day") != null) {
      day_1 = session.getAttribute("day").toString();
    } else {
      session.setAttribute("day", calendar.getDayString());
      day_1 = calendar.getDayString();
    }

    if (alarmInterfaceName != null) {
      session.setAttribute("interfaceid", alarmInterfaceName);
    } else if (session.getAttribute("interfaceid") != null) {
      alarmInterfaceName = session.getAttribute("interfaceid").toString();
    } else {
      session.setAttribute("interfaceid", "-");
    }
    
    if (status != null) {
      session.setAttribute("status", status);
    } else if (session.getAttribute("status") != null) {
      status = session.getAttribute("status").toString();
    } else {
      session.setAttribute("status", "-");
      status = "-";
    }

    // find all possible alarm interfaces
    AlarmInterfaces = fetchAlarmInterfaces(connDrep);

    Vector execSets = new Vector();

    if (button != null) {
      log.debug("Fetching executed alarms...");
      if (alarmInterfaceName.equals("-")) {

        ListIterator it = AlarmInterfaces.listIterator();

        while (it.hasNext()) {
          List allAlarms = new ArrayList();
          allAlarms = fetchExecutedAlarmSets(connErep, connDrep, it.next().toString(), status, day_1, month_1, year_1,
              "100");

          if (!allAlarms.isEmpty()) {
            execSets.add(allAlarms);
          }

        }

      } else {
        execSets.add(fetchExecutedAlarmSets(connErep, connDrep, alarmInterfaceName, status, day_1, month_1, year_1,
            "100"));
      }
      ctx.put("executedAlarms", execSets);
    }
    //This sends a vector of valid years from DIM_DATE Table.
    //This is used by cal_select_1.vm
    Connection conn = ((RockFactory) ctx.get("rockDwh")).getConnection();
    CalSelect calSelect = new CalSelect(conn);
    ctx.put("validYearRange", calSelect.getYearRange());

    ctx.put("year_1", year_1);
    ctx.put("month_1", month_1);
    ctx.put("day_1", day_1);

    ctx.put("selectedInterface", alarmInterfaceName);
    ctx.put("selectedStatus", status);

    // put the sets to context
    ctx.put("alarmInterfaces", AlarmInterfaces);

    try {
      // return the page that should be shown
      outty = getTemplate(page);
    } catch (Exception e) {

    }

    return outty;
  }

  /**
   * Fecth the list of different alarm interfaces. <br>
   * AlarmInterface table is used.
   * 
   * @return list
   */

  public List fetchAlarmInterfaces(Connection conn) {

    List retval = new ArrayList();

    //
    // Sql statement as a string
    String sqlStmt = "SELECT INTERFACEID FROM AlarmInterface";

    Statement stmt = null;
    ResultSet rset = null;
    //
    try {
      log.debug("Executing query: " + sqlStmt);

      stmt = conn.createStatement();
      rset = stmt.executeQuery(sqlStmt);

      // loop and collect all names
      while (rset.next()) {
        retval.add(rset.getString(1));
      }
    } catch (SQLException sqle) {
      // problems?
      log.error("SQLException when executing SQL: '" + sqlStmt + "'");
      log.error("Exception: ", sqle);
    } finally {
      // finally close result set and statement
      try {
        if (rset != null) {
          rset.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
      try {
        if (stmt != null) {
          stmt.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
    }

    // return with result (alarmInterfaces)
    return retval;
  }

  private List fetchExecutedAlarmSets(Connection conn, Connection connDwhRep, String interfaceName, String status,
      String day, String month, String year, String rowcount) {

    List retval = new ArrayList();
    List setId = new ArrayList();

    String dataTime = year + "-" + month + "-" + day;
    String dataTimeBegin = dataTime + " 00:00:00";
    String dataTimeEnd = dataTime + " 23:59:59";
    
    String where = "";

    String collectionSetId = "";
    String collectionId = "";

    setId = getCollectionId(connDwhRep, interfaceName);

    ListIterator it = setId.listIterator();

    while (it.hasNext()) {
      collectionSetId = it.next().toString();
      collectionId = it.next().toString();
    }

    where = "B.START_DATE BETWEEN '" +dataTimeBegin +"' AND '" + dataTimeEnd + "' AND "
    +"B.COLLECTION_SET_ID = '" + collectionSetId + "' AND B.COLLECTION_ID = '" + collectionId + "' AND ";

    if (status != null && !status.equals("-")) {

      where += " B.status = '" + status + "' AND ";

    }

    //
    // Sql statement as a string
    String sqlStmt = "SELECT TOP " + rowcount + " B.ID, B.START_DATE, B.END_DATE, B.FAIL_FLAG, B.STATUS, "
        + "B.VERSION_NUMBER, B.META_COLLECTION_SET_NAME, B.META_COLLECTION_NAME, B.SETTYPE, B.SERVICE_NODE "
        + "FROM META_TRANSFER_BATCHES B WHERE " + where + "B.SETTYPE = 'alarm' ORDER BY 3 DESC, 2";

    
    // make query and build up executed list
    Statement stmt = null;
    ResultSet rset = null;
    //
    try {
      log.debug("Executing query: " + sqlStmt);

      stmt = conn.createStatement();
      rset = stmt.executeQuery(sqlStmt);

      while (rset.next()) {
        List l = new ArrayList();

        l.add(rset.getString("META_COLLECTION_SET_NAME")); // collection name
        l.add(rset.getString("SETTYPE")); // set type
        l.add(rset.getString("START_DATE")); // start time
        l.add(rset.getString("END_DATE")); // end time
        l.add(rset.getString("STATUS")); // status
        l.add(rset.getString("VERSION_NUMBER")); // version
        l.add(rset.getString("META_COLLECTION_NAME")); // version

        l.add(interfaceName);
        
        //20111129 EANGUAN :: To add service node column in adminui :: HSS/SMF IP
        if (rset.getString("SERVICE_NODE") != null) {
      	  if (rset.getString("SERVICE_NODE").isEmpty()){
      		l.add("");
      	  }else{
      		l.add(rset.getString("SERVICE_NODE")); // Service Node
      	  }
        }else{
        	l.add("");
        }
        
        retval.add(l);
      }

    } catch (SQLException ex) {
      log.error("SQLException: " + ex);
    } finally {
      try {
        if (rset != null) {
          rset.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
      try {
        if (stmt != null) {
          stmt.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
    }

    // return the executed sets
    return retval;
  }

  private List getCollectionId(Connection conn, String interfaceId) {

    List retval = new ArrayList();

    // Sql statement as a string
    String sqlStmt = "SELECT collection_set_id, collection_id FROM AlarmInterface WHERE interfaceid = '" + interfaceId
        + "'";

    Statement stmt = null;
    ResultSet rset = null;
    //
    try {
      log.debug("Executing query: " + sqlStmt);

      stmt = conn.createStatement();
      rset = stmt.executeQuery(sqlStmt);

      // loop and collect all names
      while (rset.next()) {
        retval.add(rset.getString("collection_set_id"));
        retval.add(rset.getString("collection_id"));
      }
    } catch (SQLException sqle) {
      // problems?
      log.error("SQLException when executing SQL: '" + sqlStmt + "'");
      log.error("Exception: ", sqle);
    } finally {
      // finally close result set and statement
      try {
        if (rset != null) {
          rset.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
      try {
        if (stmt != null) {
          stmt.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
    }

    return retval;
  }

}
