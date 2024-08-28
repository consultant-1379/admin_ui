package com.distocraft.dc5000.etl.gui.monitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

/**
 * @author melantie Copyright Distocraft 2005 $id$
 */
public class MonitoringRules extends EtlguiServlet {

  private static final long serialVersionUID = 1L;

  private final transient Log log = LogFactory.getLog(this.getClass());

  private static final SimpleDateFormat sdf_secs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, final Context ctx) throws SQLException  {

    Template outty = null;
    List<List<String>> monitoringRules = null;

    final HttpSession session = request.getSession(false);
    
    final RockFactory rockDwhRep = (RockFactory) ctx.get("rockDwhRep");
    
    String selectedTechpack = StringEscapeUtils.escapeHtml(request.getParameter("tp"));
    
    String pattern =  "^[a-zA-Z0-9-_]*$";
    
    if(selectedTechpack == null){
    	selectedTechpack = "-";
    }
    
    if(selectedTechpack.matches(pattern)){
    	selectedTechpack = StringEscapeUtils.escapeHtml(selectedTechpack);
    }else{
    	selectedTechpack = null;
    }

//  techpack name session info
    if (selectedTechpack != null) {
      session.setAttribute("tpName", selectedTechpack);
    } else if (session.getAttribute("tpName") != null) {
      selectedTechpack = session.getAttribute("tpName").toString();
    } else {
      session.setAttribute("tpName", "-");
      selectedTechpack = "-";
    }
    
    if (selectedTechpack != " " || selectedTechpack != null) {
      monitoringRules = getMonitoringRules(ctx, selectedTechpack);
      ctx.put("monitoringRules", monitoringRules);
      ctx.put("tp", selectedTechpack);
    }

    final List<String> tps = Util.getTechPacks(rockDwhRep.getConnection());
    //  add '-' selection to drop down menu
    tps.add("-");
    ctx.put("distinctTechPacks", tps);

    final String page = "monitoring_rules.vm";

    try {
      outty = getTemplate(page);
    } catch (Exception e) {
      throw new VelocityException(e);
    }
    return outty;
  }

  /**
   * @param editType
   * @param origTimelevel
   * @param newTimelevel
   * @param status
   * @param activationday
   */

  public void updateMonitoringRule(final Context ctx, final String editType, final String origTimelevel, final String newTimelevel,
      final String status, final String activationday) throws SQLException {

    Statement statement = null; // sql statement

    final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh"); 
    try {
      rockDwh.getConnection().setAutoCommit(false);
      statement = rockDwh.getConnection().createStatement();

      final String sql = "UPDATE LOG_MonitoredTypes SET status = '" + status + "', timelevel = '" + newTimelevel
      + "', activationday = '" + activationday + "', modified = now() WHERE typename = '" + editType
      + "' AND timelevel = '" + origTimelevel + "'";

      log.debug(sql);

      statement.executeUpdate(sql);
      rockDwh.getConnection().commit();

    } catch (Exception e) { 
      rockDwh.getConnection().rollback();
      log.error("Exception: ",e); 
    } finally { 
      try { //commit
        if (statement != null) {
          statement.close(); 
        }
      } catch (Exception e) { 
        log.error("Exception: ",e); 
      } 
    }
  }

  /**
   * Get list of monitoring rules for tech pack
   * 
   * @param Tech
   *          pack name
   * @return List
   */

  public List<List<String>> getMonitoringRules(final Context ctx, final String techPack) throws SQLException {

    final List<List<String>> retval = new ArrayList<List<String>>();
    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh"); 

    try {
      statement = rockDwh.getConnection().createStatement();

      final String sql = "SELECT TYPENAME, TIMELEVEL, max(MODIFIED) as MODIFIED FROM LOG_MonitoringRules WHERE techpack_name = '"
          + techPack + "' group by typename, timelevel";

      log.debug(sql);

      result = statement.executeQuery(sql);

      // parse search result
      while (result.next()) {
        final List<String> l = new ArrayList<String>();
        l.add(result.getString("TYPENAME"));
        l.add(result.getString("TIMELEVEL"));

        if (result.getString("MODIFIED") == null) {
          l.add(result.getString("MODIFIED"));
        } else {
          final java.util.Date mod = new java.util.Date(result.getTimestamp("MODIFIED").getTime());
          l.add(sdf_secs.format(mod));
        }

        retval.add(l);
      }

    } finally {
      // commit
      if (rockDwh.getConnection() != null) {
        rockDwh.getConnection().commit();
      }
      if (result != null) {
        result.close();
      }
      if (statement != null) {
        statement.close();
      }
    }
    return retval;
  }

}