package com.distocraft.dc5000.etl.gui.monitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

/**
 * @author melantie Copyright Distocraft 2006 $id$
 */

public class AddMonitoringRule extends EtlguiServlet { // NOPMD by eheijun on 02/06/11 15:39

  private static final long serialVersionUID = 1L;

  private final transient Log log = LogFactory.getLog(this.getClass());

  private static final SimpleDateFormat sdf_secs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, // NOPMD by eheijun on 02/06/11 15:39
      final Context ctx) {

    final RockFactory rockDwhRep = (RockFactory) ctx.get("rockDwhRep");
    final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh");
    // final Connection connDRep = rockDwhRep.getConnection();
    // final Connection connDwh = rockDwh.getConnection();

    Template outty = null;

     final String tp = StringEscapeUtils.escapeHtml(request.getParameter("tp"));
     final String type = StringEscapeUtils.escapeHtml(request.getParameter("type"));
     final String add = StringEscapeUtils.escapeHtml(request.getParameter("add"));
     final String timelevel = StringEscapeUtils.escapeHtml(request.getParameter("timelevel"));
    
     
    final String save = StringEscapeUtils.escapeHtml(request.getParameter("save"));
    final String delete = StringEscapeUtils.escapeHtml(request.getParameter("delete"));

    String page = "add_monitoring_rule.vm";

    ctx.put("timelevel", timelevel);
    ctx.put("tp", tp);

    final Map<String, String> params = new HashMap<String, String>();

    final Enumeration<?> parameters = request.getParameterNames();

    // get all parameters to map
    while (parameters.hasMoreElements()) {
      final String par = (String) parameters.nextElement();
      params.put(par, request.getParameter(par));
    }

    if (type == null && timelevel == null) {
      ctx.put("addNew", "true");

      ctx.put("distinctMeaTypes", Util.getMeasurementTypes(tp, rockDwhRep.getConnection()));
      ctx.put("distinctTimelevels", Util.getRawTimelevels(rockDwh.getConnection()));
    }

    if (save != null || delete != null || add != null) {

      final List<String> tps = Util.getTechPacks(rockDwhRep.getConnection());
      ctx.put("distinctTechPacks", tps);

      page = "monitoring_rules.vm";

      List<List<String>> monitorRules = null;

      if (save != null) {
        updateMonitoringRule(rockDwh.getConnection(), tp, type, timelevel, params);
        monitorRules = getMonitoringRules(rockDwh.getConnection(), tp);
        ctx.put("monitoringRules", monitorRules);
      }

      if (delete != null) {
        deleteMonitoringRule(rockDwh.getConnection(), tp, type, timelevel);
        monitorRules = getMonitoringRules(rockDwh.getConnection(), tp);
        ctx.put("monitoringRules", monitorRules);
      }

      if (add != null) {

        final boolean exists = checkIfmonitoringRuleExists(rockDwh.getConnection(), type, timelevel);

        if (exists) {
          // ctx.put("addNew","true");
          page = "add_monitoring_rule.vm";
          ctx.put("errorMsg", "Monitoring rule already exists. There can be only one typename and timelevel pair.");
        } else {
          updateMonitoringRule(rockDwh.getConnection(), tp, type, timelevel, params);
          monitorRules = getMonitoringRules(rockDwh.getConnection(), tp);
          ctx.put("monitoringRules", monitorRules);
        }
      }

    }

    ctx.put("type", type);
    ctx.put("rules", getMonitoringRule(rockDwh.getConnection(), type, timelevel));

    try {
      outty = getTemplate(page);
    } catch (Exception e) {
      throw new VelocityException(e);
    }
    return outty;
  }

  /**
   * Check if monitoring rule allready exists in monitoring rule table
   * 
   * @param tp
   * @param type
   * @param timelevel
   * @return boolean
   */

  private boolean checkIfmonitoringRuleExists(final Connection conn, final String type, final String timelevel) {
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    boolean exists = false;

    try {
      statement = conn.createStatement();

      final String sql = "SELECT count(*) as rowcount FROM LOG_MonitoringRules WHERE typename = '" + type
          + "' AND timelevel = '" + timelevel + "'";

      log.debug(sql);

      result = statement.executeQuery(sql);

      result.next();

      final int rowCount = result.getInt("rowcount");

      if (rowCount > 0) {
        exists = true;
      }

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      try {
        // commit
        if (conn != null) {
          conn.commit();
        }
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (Exception e) {
        log.error("Exception: ", e);
      }
    }
    return exists;
  }

  /**
   * Deletes measurement type from monitoring rules
   * 
   * @param tp
   * @param type
   * @param timelevel
   */
  private void deleteMonitoringRule(final Connection conn, final String tp, final String type, final String timelevel) {

    Statement statement = null; // sql statement

    try {
      statement = conn.createStatement();

      final String sql = "DELETE FROM LOG_MonitoringRules WHERE techpack_name = '" + tp + "' AND typename = '" + type
          + "' AND timelevel = '" + timelevel + "'";

      log.debug(sql);

      statement.executeUpdate(sql);

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      try {
        // commit
        if (conn != null) {
          conn.commit();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (Exception e) {
        log.error("Exception: ", e);
      }
    }

  }

  /**
   * Get monitoring rules
   * 
   * @param Mesurement
   *          type, timelevel
   * @return Vector
   */

  private Map<String, List<String>> getMonitoringRule(final Connection conn, final String mt, final String tl) { // NOPMD by eheijun on 02/06/11 15:39

    final Map<String, List<String>> retval = new HashMap<String, List<String>>();

    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();

      final String sql = "SELECT RULENAME, THRESHOLD, STATUS FROM LOG_MonitoringRules WHERE typename = '" + mt
          + "' AND timelevel = '" + tl + "'";

      log.debug(sql);

      result = statement.executeQuery(sql);

      // parse search result
      while (result.next()) {
        final List<String> item = new ArrayList<String>(); // NOPMD by eheijun on 02/06/11 15:39
        item.add(result.getString("THRESHOLD"));
        item.add(result.getString("STATUS"));
        retval.put(result.getString("RULENAME"), item);
      }

      final List<String> temp = new ArrayList<String>();
      temp.add("");
      temp.add("");

      // add missing rules to map
      if (!retval.containsKey("MINROW")) {
        retval.put("MINROW", temp);
      }
      if (!retval.containsKey("MAXROW")) {
        retval.put("MAXROW", temp);
      }
      if (!retval.containsKey("MINSOURCE")) {
        retval.put("MINSOURCE", temp);
      }
      if (!retval.containsKey("MAXSOURCE")) {
        retval.put("MAXSOURCE", temp);
      }

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      try {
        // commit
        if (conn != null) {
          conn.commit();
        }
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (Exception e) {
        log.error("Exception: ", e);
      }
    }
    return retval;
  }

  /**
   * 
   * 
   * 
   * @param tp
   *          Tech pack name
   * @param mt
   *          Measurement type
   * @param tl
   *          timelevel
   * @param ruleValues
   */

  private void updateMonitoringRule(final Connection conn, final String tp, final String mt, final String tl, // NOPMD by eheijun on 02/06/11 15:39
      final Map<String, String> ruleValues) {

    try {
      conn.setAutoCommit(false);
      try {
        final Statement statement = conn.createStatement();
        try {
          String sql = "DELETE FROM LOG_MonitoringRules WHERE typename = '" + mt + "' AND timelevel = '" + tl + "'";
          statement.executeUpdate(sql);
          final Iterator<String> it = ruleValues.keySet().iterator();
          while (it.hasNext()) {
            final String key = it.next();
            if (key.equalsIgnoreCase("maxRow") || key.equalsIgnoreCase("minRow") || key.equalsIgnoreCase("maxSource")
                || key.equalsIgnoreCase("minSource")) {
              final String tmpStatus = ruleValues.get(key + "status").toString();
              final String tmpThreshold = ruleValues.get(key + "threshold").toString();
              if (!tmpStatus.equalsIgnoreCase("") && !tmpThreshold.equalsIgnoreCase("")) {
                sql = "INSERT INTO LOG_MonitoringRules (TYPENAME,TIMELEVEL,RULENAME,THRESHOLD,STATUS,MODIFIED,TECHPACK_NAME) VALUES ('"
                    + mt + "','" + tl + "','" + key + "','" + tmpThreshold + "','" + tmpStatus + "',now(),'" + tp + "')";
                statement.executeUpdate(sql);
              }
            }
          }
          conn.commit();
        } catch (SQLException e) {
          conn.rollback();
        } finally {
          statement.close();
        }
      } finally {
        conn.setAutoCommit(true);
      }

    } catch (Exception e) {
      log.error("Exception: ", e);
    }
  }

  /**
   * Get list of monitoring rules for tech pack
   * 
   * @param Tech
   *          pack name
   * @return List
   */

  private List<List<String>> getMonitoringRules(final Connection conn, final String techPack) {

    final List<List<String>> retval = new ArrayList<List<String>>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    try {
      statement = conn.createStatement();

      final String sql = "SELECT TYPENAME, TIMELEVEL, max(MODIFIED) as MODIFIED FROM LOG_MonitoringRules WHERE techpack_name = '"
          + techPack + "' group by typename, timelevel";

      log.debug(sql);

      result = statement.executeQuery(sql);

      // parse search result
      while (result.next()) {
        final List<String> l = new ArrayList<String>(); // NOPMD by eheijun on 02/06/11 15:39
        l.add(result.getString("TYPENAME"));
        l.add(result.getString("TIMELEVEL"));

        if (result.getString("MODIFIED") == null) {
          l.add(result.getString("MODIFIED"));
        } else {
          final java.util.Date mod = new java.util.Date(result.getTimestamp("MODIFIED").getTime()); // NOPMD by eheijun on 02/06/11 15:39
          l.add(sdf_secs.format(mod));
        }

        retval.add(l);
      }

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      try {
        // commit
        if (conn != null) {
          conn.commit();
        }
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (Exception e) {
        log.error("Exception: ", e);
      }
    }
    return retval;
  }
}