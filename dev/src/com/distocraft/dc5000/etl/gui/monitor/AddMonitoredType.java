package com.distocraft.dc5000.etl.gui.monitor;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import org.apache.commons.lang.StringEscapeUtils;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

/**
 * TODO intro TODO usage TODO used databases/tables TODO used properties
 * 
 * @author melantie Copyright Distocraft 2006 $id$
 */
public class AddMonitoredType extends EtlguiServlet {

  private static final long serialVersionUID = 1L;

  private final transient Log log = LogFactory.getLog(this.getClass());
  
  private static final SimpleDateFormat pickYear = new SimpleDateFormat("yyyy");
  private static final SimpleDateFormat pickMonth = new SimpleDateFormat("MM");
  private static final SimpleDateFormat pickDay = new SimpleDateFormat("dd");
  

  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, final Context ctx) throws SQLException {
 
    Template outty = null;
    List<List<String>> monitoredTypes = null;
    
    final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh");
    final RockFactory rockDwhRep = (RockFactory) ctx.get("rockDwhRep");
    
    //final String selectedTechpack = request.getParameter("tp");
     String addMonitoredType = request.getParameter("type");
     String timelevel = request.getParameter("timelevel");
     String status = request.getParameter("status");
     String selectedTechPack = request.getParameter("tp");

     String actYear = request.getParameter("year_1");
     String actMonth = request.getParameter("month_1");
     String actDay = request.getParameter("day_1");
    
    String pattern =  "^[a-zA-Z0-9-_]*$";
    
    if(addMonitoredType == null){
    	addMonitoredType = "-";
    }
    
    if(addMonitoredType.matches(pattern)){
    	addMonitoredType = StringEscapeUtils.escapeHtml(addMonitoredType);
    }else{
    	addMonitoredType = "-";
    }
    
    if(timelevel == null){
    	timelevel = "-";
    }
    
    if(timelevel.matches(pattern)){
    	timelevel = StringEscapeUtils.escapeHtml(timelevel);
    }else{
    	timelevel = "1MIN";
    }
    
    if(status == null){
    	status = "-";
    }
    
    if(status.matches(pattern)){
    	status = StringEscapeUtils.escapeHtml(status);
    }else{
    	status = "INACTIVE";
    }
    
    if(selectedTechPack == null){
    	selectedTechPack = "-";
    }
    
    if(selectedTechPack.matches(pattern)){
    	selectedTechPack = StringEscapeUtils.escapeHtml(selectedTechPack);
    }else{
    	selectedTechPack = "-";
    }
    
 String pattern2 = "^[0-9]*$";
    
    if(actYear == null){
    	actYear = "-";
    }
    
    if(actYear.matches(pattern2)){
    	actYear = StringEscapeUtils.escapeHtml(actYear);
    }else{
    	actYear = null;
    }
    
    if(actMonth == null){
    	actMonth = "-";
    }
    
    if(actMonth.matches(pattern2)){
    	actMonth = StringEscapeUtils.escapeHtml(actMonth);
    }else{
    	actMonth = null;
    }
    
    if(actDay == null){
    	actDay = "-";
    }
    
    if(actDay.matches(pattern2)){
    	actDay = StringEscapeUtils.escapeHtml(actDay);
    }else{
    	actDay = null;
    }
    

    final String actDate = actYear + "-" + actMonth + "-" + actDay;

    String page = "add_monitored_type.vm";

    final String save = request.getParameter("save");

    if (save != null && addMonitoredType != null && timelevel != null) {

      if (Util.checkIfMonitoredExists(rockDwh.getConnection(), addMonitoredType, timelevel)) {

        ctx.put("errorMsg","Could not add " + addMonitoredType + " and timelevel " + timelevel+ " there can be only one same typename and timelevel pair.");

      } else {

        addMonitoredType(ctx, addMonitoredType, timelevel, status, actDate, selectedTechPack);

        monitoredTypes = Util.getMonitoredTypes(selectedTechPack, rockDwh.getConnection());
        ctx.put("monitoredTypes", monitoredTypes);
     
        page = "monitored_types.vm";
      }

    }

    final Date today = new Date();

    final String year_1 = pickYear.format(today);
    final String month_1 = pickMonth.format(today);
    final String day_1 = pickDay.format(today);
    
    final List<String> futureYearRange = Util.getFutureRange(year_1);
    
    ctx.put("year_1", year_1);
    ctx.put("month_1", month_1);
    ctx.put("day_1", day_1);
    ctx.put("futureYearRange", futureYearRange);
    
    final List<String> meaTypes = Util.getMeasurementTypes(selectedTechPack, rockDwhRep.getConnection());
    ctx.put("distinctMeaTypes", meaTypes);

    final List<String> timelevels = Util.getRawTimelevels(rockDwh.getConnection());
    ctx.put("distinctTimelevels", timelevels);

    ctx.put("tp", selectedTechPack);

    final List<String> tps = Util.getTechPacks(rockDwhRep.getConnection());
    ctx.put("distinctTechPacks", tps);

    try {
      outty = getTemplate(page);
    } catch (Exception e) {
      throw new VelocityException(e);
    }
    return outty;
  }

  

  


  /**
   * Add monitored type
   * 
   * @param Typename
   * @param Timelevel
   * @param Status
   *          ACTIVE/INACTIVE
   * @param Activation
   *          day
   * @param Tech
   *          pack name
   * @throws SQLException 
   */

  private void addMonitoredType(final Context ctx, final String tn, final String tl, final String status, final String actDay, final String tp) throws SQLException {

    Statement statement = null; // sql statement
    
    final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh");
    //final Connection conn = rockDwh.getConnection();

   try {
      statement = rockDwh.getConnection().createStatement();
      
      final String sql = "INSERT INTO LOG_MonitoredTypes (typename, timelevel, status, modified, activationday, techpack_name) VALUES ('"
          + tn + "','" + tl + "','" + status + "', now(), '" + actDay + "', '" + tp + "')";

      log.debug(sql);
      
      statement.executeUpdate(sql);
     
    }finally {
      if (rockDwh.getConnection() != null) {
        rockDwh.getConnection().commit();
      }
      if (statement != null) {
          statement.close();
      }
    
    }
  }
}
