package com.distocraft.dc5000.etl.gui.monitor;

import java.io.IOException;
import java.rmi.Naming;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.util.Helper;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved. <br>
 * Class executes manual aggregation request to ETL. Request is made via RMI-
 * call.
 * 
 * @author Jani Vesterinen
 */
public class Aggregation extends EtlguiServlet {

  private Log log = LogFactory.getLog(this.getClass());

  /*
   * (non-Javadoc)
   * 
   * @see com.distocraft.dc5000.etl.gui.common.EtlguiServlet#doHandleRequest(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.apache.velocity.context.Context)
   */
  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) {

    Template page = null;

    HttpSession session = request.getSession(false);

    Connection connDwh = ((RockFactory) ctx.get("rockDwh")).getConnection();

    String aggregateInfo = "";
    final String doaggregate = request.getParameter("doaggregate");

    if (request.getParameter("doaggregate") == null || request.getParameter("doaggregate").equals("")) {
      ctx.put("datadate", request.getParameter("datadate"));
      ctx.put("aggregation", request.getParameter("aggregation"));
      ctx.put("tname", request.getParameter("tname"));
      ctx.put("timestamp", request.getParameter("timestamp"));

      Vector fields = getFields(connDwh, request.getParameter("datadate"), request.getParameter("aggregation"), request
          .getParameter("tname"), request.getParameter("timestamp"));

      ctx.put("fieldvalues", fields);

      if (fields.get(4).toString().equalsIgnoreCase("NOT_LOADED")) { // 4th
                                                                      // field
                                                                      // in
                                                                      // vector
                                                                      // is
                                                                      // status
        ctx.put("disableAggregateButtonStatus", "true");
      } else {
        ctx.put("disableAggregateButtonStatus", "false");
      }

      // check if user has given any parameters
      // day parameters
      String year_1 = request.getParameter("year_1");
      String month_1 = request.getParameter("month_1");
      String day_1 = request.getParameter("day_1");
      String type = request.getParameter("type");

      // get the current date (is used at the UI, if none given)
      DbCalendar calendar = new DbCalendar();

      if (year_1 != null) {
        session.setAttribute("year", year_1);
      } else if (session.getAttribute("year") != null) {
        year_1 = session.getAttribute("year").toString();
      } else {
        session.setAttribute("year", calendar.getYearString());
      }
      
      if (month_1 != null) {
        session.setAttribute("month", month_1);
      } else if (session.getAttribute("month") != null) {
        month_1 = session.getAttribute("month").toString();
      } else {
        session.setAttribute("month", calendar.getMonthString());
      }
      
      if (day_1 != null) {
        session.setAttribute("day", day_1);
      } else if (session.getAttribute("day") != null) {
        day_1 = session.getAttribute("day").toString();
      } else {
        session.setAttribute("day", calendar.getDayString());
      }
      
      if (type != null) {
        session.setAttribute("tpName", type);
      } else if (session.getAttribute("tpName") != null) {
        type = session.getAttribute("tpName").toString();
      } else {
        session.setAttribute("tpName", "-");
      }
      
      ctx.put("year_1", year_1);
      ctx.put("month_1", month_1);
      ctx.put("day_1", day_1);
      ctx.put("type", type);

    }

    if ("aggregate".equalsIgnoreCase(doaggregate)) {
      final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      sdf.setCalendar(Calendar.getInstance());
      try {
        sdf.parse(request.getParameter("datadate"));
      } catch (Exception e) {
        log.error(e);
      }

      boolean ok = aggregate(request.getParameter("aggregation"), sdf.getCalendar().getTimeInMillis());

      ctx.put("datadate", request.getParameter("datadate"));
      ctx.put("aggregation", request.getParameter("aggregation"));

      Vector fields = getFields(connDwh, request.getParameter("datadate"), request.getParameter("aggregation"), request
          .getParameter("tname"), request.getParameter("timestamp"));

      ctx.put("fieldvalues", fields);

      if (fields.get(4).toString().equalsIgnoreCase("NOT_LOADED")) { // 4th
                                                                      // field
                                                                      // in
                                                                      // vector
                                                                      // is
                                                                      // status
        ctx.put("disableAggregateButtonStatus", "true");
      } else {
        ctx.put("disableAggregateButtonStatus", "false");
      }

      if (!ok) {
        aggregateInfo = "Manual Aggregation requested.";
      } else {
        aggregateInfo = "Manual aggregation request failed. For further information, please contact Ericsson Network IQ administrator.";
      }

      if (!ok) {
        try {

          request.getRequestDispatcher(
              "ShowAggregations?aggregateinfo=" + aggregateInfo + "&date_1=" + request.getParameter("date_1")
                  + "&month_1=" + request.getParameter("month_1") + "&year_1=" + request.getParameter("year_1")
                  + "&type=" + request.getParameter("type")).forward(request, response);
        } catch (ServletException e) {
          log.error("ServletException: ", e);
        } catch (IOException e) {
          log.error("IOException: ", e);
        }
      }
    }

    else if ("ignore".equalsIgnoreCase(doaggregate)) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      sdf.setCalendar(Calendar.getInstance());
      try {
        sdf.parse(request.getParameter("datadate"));
      } catch (Exception e) {
        log.error("Exception: ", e);
      }

      boolean ok = ignore(request.getParameter("aggregation"), sdf.getCalendar().getTimeInMillis());

      ctx.put("datadate", request.getParameter("datadate"));
      ctx.put("aggregation", request.getParameter("aggregation"));

      Vector fields = getFields(connDwh, request.getParameter("datadate"), request.getParameter("aggregation"), request
          .getParameter("tname"), request.getParameter("timestamp"));

      ctx.put("fieldvalues", fields);

      if (fields.get(4).toString().equalsIgnoreCase("NOT_LOADED")) { // 4th
                                                                      // field
                                                                      // in
                                                                      // vector
                                                                      // is
                                                                      // status
        ctx.put("disableAggregateButtonStatus", "true");
      } else {
        ctx.put("disableAggregateButtonStatus", "false");
      }

      if (!ok) {
        aggregateInfo = "Ignoring requested.";
      } else {
        aggregateInfo = "Ignoring request failed. For further information, please contact Ericsson Network IQ administrator.";
      }
      if (!ok) {
        try {

          request.getRequestDispatcher(
              "ShowAggregations?aggregateinfo=" + aggregateInfo + "&date_1=" + request.getParameter("date_1")
                  + "&month_1=" + request.getParameter("month_1") + "&year_1=" + request.getParameter("year_1")
                  + "&type=" + request.getParameter("type")).forward(request, response);
        } catch (ServletException e) {
          log.error("Exception: ", e);
        } catch (IOException e) {
          log.error("Exception: ", e);
        }
      }

    }
    ctx.put("aggregateinfo", aggregateInfo);
    ctx.put("day_1", request.getParameter("day_1"));
    ctx.put("month_1", request.getParameter("month_1"));
    ctx.put("year_1", request.getParameter("year_1"));
    ctx.put("type", request.getParameter("type"));

    try {
      page = getTemplate("little_aggregation.vm");
    } catch (ResourceNotFoundException e) {
      log.error("ResourceNotFoundException", e);
    } catch (ParseErrorException e) {
      log.error("ParseErrorException", e);
    } catch (Exception e) {
      log.error("Exception", e);
    }
    return page;
  }

  /**
   * Gets fields for little aggregation info. One row is returned with all the
   * columns.
   * 
   * @param datadate
   * @param aggregation
   * @param tname
   * @param timestamp
   * @return fields
   */
  private Vector getFields(Connection conn, String datadate, String aggregation, String tname, String timestamp) {
    Vector fields = new Vector();
    ResultSet rs = null;
    PreparedStatement ps = null;
    
    //datadate is date by it's type so no need for using DATE
    String sql = "SELECT typename, timelevel, datadate, status, description, aggregation, rowcount, initial_aggregation, last_aggregation "
        + "FROM LOG_AGGREGATIONSTATUS a "
        + "WHERE datadate =  '" + datadate + "' "
        + "AND aggregation = '"
        + aggregation
        + "' AND typename = '" + tname
        // + "' AND datadate = '" + datadate
        + "' ORDER BY typename, timelevel, datadate";

    log.debug("executed tmp: " + sql);

    try {
      ps = conn.prepareStatement(sql);
      rs = ps.executeQuery();

      while (rs.next()) {
        fields.add(rs.getString("aggregation"));
        fields.add(rs.getString("typename"));
        fields.add(rs.getString("timelevel"));
        fields.add(rs.getString("datadate"));
        fields.add(rs.getString("status"));
        fields
            .add((rs.getString("initial_aggregation") == null || rs.getString("initial_aggregation").equals("null")) ? "n/a"
                : rs.getString("initial_aggregation"));
        fields
            .add((rs.getString("last_aggregation") == null || rs.getString("last_aggregation").equals("null")) ? "n/a"
                : rs.getString("last_aggregation"));
        fields.add((rs.getString("description") == null || rs.getString("description").equals("null")) ? "n/a" : rs
            .getString("description"));
        fields.add((rs.getString("rowcount") == null || rs.getString("rowcount").equals("null")) ? "n/a" : rs
            .getString("rowcount"));
      }

    } catch (SQLException e) {
      log.trace(e);
    } finally {
      try {
        // commit
        if (conn != null) {
          conn.commit();
        }
        if (rs != null) {
          rs.close();
        }
        if (ps != null) {
          ps.close();
        }
      } catch (Exception e) {
        log.error("Exception: ", e);
      }

    }

    return fields;
  }

  /**
   * Aggregates selected data. Aggregation is done through RMI.
   * 
   * @param aggregationname
   * @param datadate
   * @return iserror - if exception is thrown true -value is returned.
   */
  public boolean aggregate(String aggregationname, long datadate) {
    boolean error = false;
    ITransferEngineRMI termi;

    Date newDate = new Date(datadate);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    try {
      // public void reaggregate(String aggregation, long datadate) throws
      // RemoteException
      // termi = (ITransferEngineRMI) connect("Aggregate");
      log.debug("Aggregating: " + aggregationname);
      log.debug("Date: " + sdf.format(newDate));
      log.debug(Helper.getEnvEntryString("rmiurl"));
      termi = (ITransferEngineRMI) Naming.lookup(RmiUrlFactory.getInstance().getEngineRmiUrl());
      termi.changeAggregationStatus("MANUAL", aggregationname, datadate);
    } catch (Exception e) {
      log.error("Exception", e);
      error = true;
    }
    return error;
  }

  /**
   * Ignores selected data. Ignore is done through RMI.
   * 
   * @param aggregationname
   * @param datadate
   * @return iserror - if exception is thrown true -value is returned.
   */
  public boolean ignore(String aggregationname, long datadate) {
    boolean error = false;
    ITransferEngineRMI termi;

    Date newDate = new Date(datadate);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    try {
      // termi = (ITransferEngineRMI) connect("Aggregate");
      log.debug("Ignoring: " + aggregationname);
      log.debug("Date: " + sdf.format(newDate));
      log.debug(Helper.getEnvEntryString("rmiurl"));
      termi = (ITransferEngineRMI) Naming.lookup(RmiUrlFactory.getInstance().getEngineRmiUrl());
      termi.changeAggregationStatus("IGNORED", aggregationname, datadate);
    } catch (Exception e) {
      log.trace(e);
      error = true;
    }
    return error;
  }

}

class TimerHelper {

  Timer timer;

  public TimerHelper(int seconds, Object object) {
    timer = new Timer();
    timer.schedule(new RemindTask(), seconds * 1000);
  }

  class RemindTask extends TimerTask {

    public void run() {
      // log.debug("Time's up!");
      timer.cancel(); // Terminate the timer thread
    }
  }

}
