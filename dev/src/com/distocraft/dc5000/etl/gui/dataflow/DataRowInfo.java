package com.distocraft.dc5000.etl.gui.dataflow;

import java.sql.SQLException;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.commons.lang.StringEscapeUtils;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.CalSelect;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.DwhMonitoring;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.monitor.Util;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved. <br>
 * This servlet will fetch for certain measurementtype the row count for certain date range and details for the selected
 * day.
 * 
 * @author Antti Laurila, Mark Stenback, Jaakko Melantie
 * 
 */
public class DataRowInfo extends EtlguiServlet { 

  private static final long serialVersionUID = 1L;

  private final Log log = LogFactory.getLog(this.getClass());

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.velocity.servlet.VelocityServlet#handleRequest(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)
   */
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response,
      final Context ctx) 
      throws Exception { 
    Template outty = null;
    boolean searchbackward = false;

    final RockFactory rockDwhRep = (RockFactory) ctx.get("rockDwhRep");
    final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh");
    final HttpSession session = request.getSession(false);

    final String searchdirection = StringEscapeUtils.escapeHtml(request.getParameter("search_direction"));

    String tp = request.getParameter("tp");

    final List<String> twb = Util.getTechPacks(rockDwhRep.getConnection());

    /*
     * For HO46878, We'll not consider DIM_ and GROUP_ techpacks. [ This is required only for
     * Eniq Events in the merged platform. ]
     */

    Vector<String> filteredTwb = new Vector<String>();
    Iterator<String> checkDIMTables = twb.iterator();

    String techPack = "";
    while (checkDIMTables.hasNext()) {

      techPack = checkDIMTables.next();
      if (techPack != null && !techPack.startsWith("DIM") && !techPack.startsWith("GROUP")) {
        // Load Only EVENT Techpacks
        filteredTwb.add(techPack);
      }
    }

    ctx.put("techPacks", filteredTwb);

    //
    // next get the current date (is used at the UI if none given)

    final DbCalendar calendar = new DbCalendar(); // defaults to current date

    //
    // check if user has given any parameters
    String year_1 = request.getParameter("year_1");
    String month_1 = request.getParameter("month_1");
    String day_1 = request.getParameter("day_1");
    String meas_group = request.getParameter("dgroup");
    String meas_type = request.getParameter("dtype");
    String meas_level = request.getParameter("dlevel");
    String search_days = request.getParameter("search_days");
     
    
String pattern =  "^[a-zA-Z0-9_]*$";
    
    if(tp == null){
    	tp = "-";
    }
    if(tp.matches(pattern)){
    	tp = StringEscapeUtils.escapeHtml(tp);
    }else{
    	tp = null;
    }
    
    if(meas_group == null){
    	meas_group = "-";
    }
    if(meas_group.matches(pattern)){
    	meas_group = StringEscapeUtils.escapeHtml(meas_group);
    }else{
    	meas_group = "-";
    }
    
    if(meas_type == null){
    	meas_type = "-";
    }
    if(meas_type.matches(pattern)){
    	meas_type = StringEscapeUtils.escapeHtml(meas_type);
    }else{
    	meas_type = null;
    }
    
    if(meas_level == null){
    	meas_level = "-";
    }
    if(meas_level.matches(pattern)){
    	meas_level = StringEscapeUtils.escapeHtml(meas_level);
    }else{
    	meas_level = null;
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
    
    if(search_days == null){
    	search_days = "-";
    }
    
    
    if(search_days.matches(pattern2)){
    	search_days = StringEscapeUtils.escapeHtml(search_days);
    }else{
    	search_days = null;
    }
    
    
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

    if (tp != null) {
      session.setAttribute("tpName", tp);
    } else if (session.getAttribute("tpName") != null) {
      tp = session.getAttribute("tpName").toString();
    } else {
      session.setAttribute("tpName", "-");
      tp = "-";
    }

    //
    if (tp == null || tp == "") {
      tp = twb.get(0).toString();
    }

    final List<String> mtv = Util.getMeasurementTypes(tp, rockDwhRep.getConnection());

    if (tp != null && tp != "-") {
      ctx.put("meaTypesForTechPack", mtv);
      ctx.put("tp", tp);
    }

    if (mtv.size() > 0) {

      // Vector table_levels = TablelevelsPeer.getTableLevels();
      final Vector<String> table_levels = new Vector<String>();
      table_levels.add("RAW");
      table_levels.add("DAY");
      table_levels.add("DAYBH");
      table_levels.add("COUNT");
      // table_levels.removeElement("PLAIN");
      ctx.put("theLevels", table_levels);

      try {

        //
        // first load all measurement's and table_level's

        // Vector link_list = getMeasurementTypes();
        final List<String> link_list = Util.getAllMeasurementTypes(rockDwhRep.getConnection());
        ctx.put("theLinks", link_list);

        ctx.put("p_day", day_1);
        ctx.put("p_year", year_1);
        ctx.put("p_month", month_1);
        ctx.put("p_dgroup", meas_group);
        ctx.put("p_dtype", meas_type);
        ctx.put("p_dlevel", meas_level);
        ctx.put("direction", searchdirection != null && searchdirection.equals("forward") ? "+" : "-");

        setContextOnSearchDirection(ctx, searchdirection);

        if (search_days == null || search_days.equals("")
            || Integer.parseInt(search_days) > 31) {
          search_days = "31";
        }
      
        ctx.put("search_days", search_days);
        search_days = String.valueOf(Integer.parseInt(search_days)-1);
        
        if (search_days.equals("0")){
              search_days = "1";
        } 
        //
        // if user has given some parameter's set them to screen
        // if not - set default ones

        if (meas_type != null) {
          ctx.put("theType", meas_type);
        } else {
          if (mtv.size() > 0) {
            ctx.put("theType", mtv.get(0));
          }
        }

        if (meas_level != null) {
          ctx.put("theLevel", meas_level);
        } else {
          ctx.put("theLevel", "RAW");
          meas_level = "RAW";
        }

        //
        // if user has given the date, dont change it, but if
        // not set the date as current one on the UI-screen
        String check_time = "";
        if (year_1 != null) {
          check_time = year_1 + "-" + month_1 + "-" + day_1;
        } else {
          year_1 = calendar.getYearString();
          month_1 = calendar.getMonthString();
          day_1 = calendar.getDayString();
          check_time = calendar.getDbDate();
        }

        //This sends a vector of valid years from DIM_DATE Table.
        //This is used by cal_select_1.vm
        final CalSelect calSelect = new CalSelect(rockDwh.getConnection());
        ctx.put("validYearRange", calSelect.getYearRange());

        ctx.put("year_1", year_1);
        ctx.put("month_1", month_1);
        ctx.put("day_1", day_1);
        ctx.put("theDate", check_time);

        final DbCalendar cal = new DbCalendar(year_1, month_1, day_1);
        cal.correctByDays(Integer.parseInt(search_days));
        final String end_time = cal.getDbDate();

        //
        // if user has selected measurement type too, we have a winner
        // we can fetch the data from the table
        if (searchdirection == null) {
          searchbackward = true;
        } else if (searchdirection.equals("backward")) {
          searchbackward = true;
        } else if (searchdirection.equals("forward")) {
          searchbackward = false;
        }

        if (meas_type != null && !meas_type.equals("-")) {

          try {
            final List<List<Object>> genRowInfo = DwhMonitoring.getDataRowDateExtendedRange(meas_type, meas_level,
                check_time, end_time, rockDwh.getConnection(),rockDwhRep.getConnection(), searchbackward, search_days);
            ctx.put("genRowInfo", genRowInfo);
          } catch (SQLException e) {
            ctx.put("errormsg", e.getMessage());
          }

          // if (meas_level.equals("RAW")) {

            try {
            final String type_id = DwhMonitoring.getTypeIdForMeasType(ctx, meas_type);

            final List<List<Object>> detailRowInfo = DwhMonitoring.getDataRowAndKey(ctx, type_id, meas_type,
                meas_level, check_time, true, searchbackward, search_days);
              ctx.put("detailRowInfo", detailRowInfo);
            } catch (SQLException e) {
              ctx.put("errormsg", e.getMessage());
            }
          // }
        }

        //
      } catch (Exception e) {
        log.error("Error occurred in DataRowInfo", e);
      }

    }
    // try {
    outty = getTemplate("datarowinfo.vm");
    /*
     * } catch (ResourceNotFoundException e1) { log.error("Template datarowinfo.vm not found: " + e1); } catch
     * (ParseErrorException e1) { log.error("Parse error for template datarowinfo.vm: " + e1); } catch (Exception e1) {
     * log.error("When creating template datarowinfo.vm error occurred: " + e1); }
     */

    return outty;
  }
 
  protected void setContextOnSearchDirection(final Context ctx,
          final String searchdirection) {
	   if (searchdirection == null || searchdirection.equals("backward")) {
		   ctx.put("backward", "checked");
	   }
	   else
	   {
		   ctx.put("forward","checked");
	   }
   }


}
