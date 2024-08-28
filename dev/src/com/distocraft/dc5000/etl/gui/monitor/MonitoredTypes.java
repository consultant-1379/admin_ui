package com.distocraft.dc5000.etl.gui.monitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.CalSelect;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

/**
 * @author melantie Copyright Distocraft 2005 $id$
 */
public class MonitoredTypes extends EtlguiServlet {

  private static final long serialVersionUID = 1L;

  private final transient Log log = LogFactory.getLog(this.getClass());

  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response,
      final Context ctx) throws SQLException {

    Template outty = null;
    List<List<String>> monitoredTypes = null;

    final HttpSession session = request.getSession(false);

    // get the current date (is used at the UI, if none given)
    final DbCalendar calendar = new DbCalendar();

    final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh");
    final RockFactory rockDwhRep = (RockFactory) ctx.get("rockDwhRep");

    String selectedTechpack = request.getParameter("tp");
    final String update = StringEscapeUtils.escapeHtml(request.getParameter("update"));
    final String deleteSelected = StringEscapeUtils.escapeHtml(request.getParameter("deleteSelected"));

    String year_1 = request.getParameter("year_1");
    String month_1 = request.getParameter("month_1");
    String day_1 = request.getParameter("day_1");
    
    String pattern =  "^[a-zA-Z0-9-_]*$";
  
    if(selectedTechpack == null){
    	selectedTechpack = "-";
    }
    
    if(selectedTechpack.matches(pattern)){
    	selectedTechpack = StringEscapeUtils.escapeHtml(selectedTechpack);
    }else{
    	selectedTechpack = null;
    }
    
    String pattern2 = "^[0-9]*$";
    
    if(year_1 == null){
    	year_1 = "-";
    }
    
    if(year_1.matches(pattern2)){
    	year_1 = StringEscapeUtils.escapeHtml(year_1);
    }else{
    	year_1 = null;
    }
    
    if(month_1 == null){
    	month_1 = "-";
    }
    
    
    if(month_1.matches(pattern2)){
    	month_1 = StringEscapeUtils.escapeHtml(month_1);
    }else{
    	month_1 = null;
    }
    
    if(day_1 == null){
    	day_1 = "-";
    }
    
    
    if(day_1.matches(pattern2)){
    	day_1 = StringEscapeUtils.escapeHtml(day_1);
    }else{
    	day_1 = null;
    }
    
    

    // year session info
    if (year_1 != null) {
      session.setAttribute("year", year_1);
    } else if (session.getAttribute("year") != null) {
      year_1 = session.getAttribute("year").toString();
    } else {
      session.setAttribute("year", calendar.getYearString());
      year_1 = calendar.getYearString();
    }

    // month session info
    if (month_1 != null) {
      session.setAttribute("month", month_1);
    } else if (session.getAttribute("month") != null) {
      month_1 = session.getAttribute("month").toString();
    } else {
      session.setAttribute("month", calendar.getMonthString());
      month_1 = calendar.getMonthString();
    }

    // day session info
    if (day_1 != null) {
      session.setAttribute("day", day_1);
    } else if (session.getAttribute("day") != null) {
      day_1 = session.getAttribute("day").toString();
    } else {
      session.setAttribute("day", calendar.getDayString());
      day_1 = calendar.getDayString();
    }

    // techpack name session info
    if (selectedTechpack != null) {
      session.setAttribute("tpName", selectedTechpack);
    } else if (session.getAttribute("tpName") != null) {
      selectedTechpack = session.getAttribute("tpName").toString();
    } else {
      session.setAttribute("tpName", "-");
      selectedTechpack = "-";
    }

    // *end* session info

    final String status = request.getParameter("status");

    final String actDay = year_1 + "-" + month_1 + "-" + day_1 + " 00:00";

    final Map<String, String> params = new HashMap<String, String>();

    final Enumeration<?> parameters = request.getParameterNames();

    // get all parameters to map
    while (parameters.hasMoreElements()) {
      final String par = (String) parameters.nextElement();
      params.put(par, request.getParameter(par));
    }

    // if update button is pressed
    if (update != null) {

      if (params.size() > 0) {

        final Iterator<String> it = params.keySet().iterator();

        while (it.hasNext()) {

          final String key = it.next();

          if (key.startsWith("chk:")) {

            final String[] temp = key.split(":");
            final String tn = temp[1];
            final String tl = temp[2];

            if (params.get(key).toString().equalsIgnoreCase("on")) {
              updateMonitoredType(rockDwh.getConnection(), tn, tl, status, actDay);
            }
          }
        }
      }
    }

    /*
     * If delete button pressed
     */

    if (deleteSelected != null) {

      if (params.size() > 0) {

        final Iterator<String> it = params.keySet().iterator();

        while (it.hasNext()) {

          final String key = it.next();

          if (key.startsWith("chk:")) {

            final String[] temp = key.split(":");
            final String tn = temp[1];
            final String tl = temp[2];

            if (params.get(key).toString().equalsIgnoreCase("on")) {
              deleteMonitoredType(rockDwh.getConnection(), rockDwhRep.getConnection(), tn, tl);

            }
          }
        }
      }

    }

    final List<String> timelevels = Util.getRawTimelevels(rockDwh.getConnection());
    ctx.put("distinctTimelevels", timelevels);

    final List<String> tps = Util.getTechPacks(rockDwhRep.getConnection());
    // add '-' selection to drop down menu
    tps.add("-");
    ctx.put("distinctTechPacks", tps);
    ctx.put("year_1", year_1);
    ctx.put("month_1", month_1);
    ctx.put("day_1", day_1);

    // This sends a vector of valid years from DIM_DATE Table.
    // This is used by cal_select_1.vm
    final CalSelect calSelect = new CalSelect(rockDwh.getConnection());
    ctx.put("validYearRange", calSelect.getYearRange());

    final String page = "monitored_types.vm";

    if (selectedTechpack != " ") {

      monitoredTypes = Util.getMonitoredTypes(selectedTechpack, rockDwh.getConnection());
      ctx.put("monitoredTypes", monitoredTypes);
      ctx.put("tp", selectedTechpack);
    }

    // Exception handling is done in EtlguiServlet in handleRequest method
    try {
      outty = getTemplate(page);
    } catch (Exception e) {
      throw new VelocityException(e);
    }
    return outty;
  }

  private void updateMonitoredType(final Connection connDwh, final String tn, final String tl, final String status,
      final String actDay) throws SQLException {

    Statement statement = null; // sql statement

    try {
      statement = connDwh.createStatement();

      final String sql = "UPDATE LOG_MonitoredTypes SET status = '" + status + "', activationday = '" + actDay
          + "', modified = now() " + " WHERE typename = '" + tn + "' AND timelevel = '" + tl + "'";

      log.debug(sql);

      statement.executeUpdate(sql);

    } finally {
      // commit
      if (connDwh != null) {
        connDwh.commit();
      }
      if (statement != null) {
        statement.close();
      }
    }

  }

  /**
   * Deletes monitored type from database
   * 
   * @param Typename
   * @param Timelevel
   */

  public void deleteMonitoredType(final Connection conn, final Connection connDRep, final String tn, final String tl)
      throws SQLException {

    Statement statement = null; // sql statement

    try {
      statement = conn.createStatement();

      final String sql = "DELETE from LOG_MonitoredTypes WHERE typename = '" + tn + "' AND timelevel = '" + tl + "'";

      log.debug(sql);

      statement.executeUpdate(sql);

      final int count = getpartionCountForLog_LoadStatus(connDRep);
      String sqlLoadStatus = null;
      for (int i = 1; i <= count; i++) {
        if (i > 0 && i < 10) {
          sqlLoadStatus = "DELETE from LOG_LoadStatus_0" + i + " WHERE typename = '" + tn + "' AND timelevel = '" + tl + "'";
        } else {
          sqlLoadStatus = "DELETE from LOG_LoadStatus_" + i + " WHERE typename = '" + tn + "' AND timelevel = '" + tl + "'";
        }
        log.debug(sqlLoadStatus);
        statement.executeUpdate(sqlLoadStatus);
      }
    } finally {

      // commit
      if (conn != null) {
        conn.commit();
      }
      if (statement != null) {
        statement.close();
      }
    }
  }

  /**
   * Update monitored type status to ACTIVE or INACTIVE
   * 
   * @param Typename
   * @param Timelevel
   * @param Status
   *          ACTIVE/INACTIVE
   */

  public void updateMonitoredTypeStatus(final Connection conn, final String tn, final String tl, final String status)
      throws SQLException {

    Statement statement = null; // sql statement

    try {
      statement = conn.createStatement();

      final String sql = "UPDATE LOG_MonitoredTypes SET status = '" + status
          + "', activationday = now(), modified = now() " + " WHERE typename = '" + tn + "' AND timelevel = '" + tl
          + "'";

      log.debug(sql);

      statement.executeUpdate(sql);

    } finally {
      // commit
      if (conn != null) {
        conn.commit();
      }
      if (statement != null) {
        statement.close();
      }
    }
  }

  private int getpartionCountForLog_LoadStatus(final Connection dwhrep) throws SQLException {

    int count = 0;

    final Statement stmt = dwhrep.createStatement();
    try {
      final String sqlPartitionCount = "select partitioncount from DWHType where typename='Log_LoadStatus'";
      final ResultSet rs = stmt.executeQuery(sqlPartitionCount);
      try {
        if (rs.next()) {
          count = rs.getInt(1);
        }
      } finally {
        rs.close();
      }
    } finally {
      stmt.close();
    }

    return count;
  }

}