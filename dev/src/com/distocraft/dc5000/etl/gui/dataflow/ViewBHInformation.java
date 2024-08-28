package com.distocraft.dc5000.etl.gui.dataflow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.BHResultSet;
import com.distocraft.dc5000.etl.gui.common.CalSelect;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Class uses database dc5000rep for busyhour information.
 * 
 * @author Mark Stenback
 */
public class ViewBHInformation extends EtlguiServlet {

  private Log log = LogFactory.getLog(this.getClass());
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * @param request
   * @param response
   * @param context
   * @return template
   */
  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
    // ........................................................... declare
    // variables
    Template outty = null;
    // Date currentTime = new Date();
    DbCalendar calendar = new DbCalendar();
    String searchStr = null;
    String initialSqlStr = null;
    String searchSqlStr = null;
    String searchDone = null;
    String startYearStr = null;
    String startMonthStr = null;
    String startDayStr = null;
    String endYearStr = null;
    String endMonthStr = null;
    String endDayStr = null;
    Connection repCon = null;
    Connection dataCon = null;
    PreparedStatement statement = null;
    ResultSet initialResult = null;
    ResultSet searchResult = null;
    BHResultSet bhResult = null;
    ArrayList initialArray = new ArrayList(500);
    ArrayList resultArray = new ArrayList(5);

    // ........................................................... get request
    // parameters
    // calendar.setTime(currentTime);

    searchStr = request.getParameter("search_string");
    searchDone = StringEscapeUtils.escapeHtml(request.getParameter("search_done"));
    startYearStr = request.getParameter("year_1");
    startMonthStr = request.getParameter("month_1");
    startDayStr = request.getParameter("day_1");
    endYearStr = request.getParameter("year_2");
    endMonthStr = request.getParameter("month_2");
    endDayStr = request.getParameter("day_2");
    
    
 String pattern =  "^[a-zA-Z0-9-_]*$";
    
    if(searchStr == null){
    	searchStr = "-";
    }
    if(searchStr.matches(pattern)){
    	searchStr = StringEscapeUtils.escapeHtml(searchStr);
    }else{
    	searchStr = "-";
    }
    
 String pattern2 = "^[0-9]*$";
    
    if(startYearStr == null){
    	startYearStr = "-";
    }
    
    if(startYearStr.matches(pattern2)){
    	startYearStr = StringEscapeUtils.escapeHtml(startYearStr);
    }else{
    	startYearStr = null;
    }
    
    if(startMonthStr == null){
    	startMonthStr = "-";
    }
    
    
    if(startMonthStr.matches(pattern2)){
    	startMonthStr = StringEscapeUtils.escapeHtml(startMonthStr);
    }else{
    	startMonthStr = null;
    }
    
    if(startDayStr == null){
    	startDayStr = "-";
    }
    
    
    if(startDayStr.matches(pattern2)){
    	startDayStr = StringEscapeUtils.escapeHtml(startDayStr);
    }else{
    	startDayStr = null;
    }
    
    if(endYearStr == null){
    	endYearStr = "-";
    }
    
    if(endYearStr.matches(pattern2)){
    	endYearStr = StringEscapeUtils.escapeHtml(endYearStr);
    }else{
    	endYearStr = null;
    }
    
    if(endMonthStr == null){
    	endMonthStr = "-";
    }
    
    
    if(endMonthStr.matches(pattern2)){
    	endMonthStr = StringEscapeUtils.escapeHtml(endMonthStr);
    }else{
    	endMonthStr = null;
    }
    
    if(endDayStr == null){
    	endDayStr = "-";
    }
    
    
    if(endDayStr.matches(pattern2)){
    	endDayStr = StringEscapeUtils.escapeHtml(endDayStr);
    }else{
    	endDayStr = null;
    }
    
    
   

    repCon = ((RockFactory) ctx.get("rockDwhRep")).getConnection();
    dataCon = ((RockFactory) ctx.get("rockDwh")).getConnection();
    HttpSession session = request.getSession(false);

    
      // ....................................................... set dates

      if (startYearStr != null) {
        session.setAttribute("year", startYearStr);
      } else if (session.getAttribute("year") != null) {
        startYearStr = session.getAttribute("year").toString();
      } else {
        session.setAttribute("year", calendar.getYearString());
        startYearStr = calendar.getYearString();
      }

      if (startMonthStr != null) {
        session.setAttribute("month", startMonthStr);
      } else if (session.getAttribute("month") != null) {
        startMonthStr = session.getAttribute("month").toString();
      } else {
        session.setAttribute("month", calendar.getMonthString());
        startMonthStr = calendar.getMonthString();
      }

      if (startDayStr != null) {
        session.setAttribute("day", startDayStr);
      } else if (session.getAttribute("day") != null) {
        startDayStr = session.getAttribute("day").toString();
      } else {
        session.setAttribute("day", calendar.getDayString());
        startDayStr = calendar.getDayString();
      }

      if (endYearStr != null) {
        session.setAttribute("year2", endYearStr);
      } else if (session.getAttribute("year2") != null) {
        endYearStr = session.getAttribute("year2").toString();
      } else {
        session.setAttribute("year2", calendar.getYearString());
        endYearStr = calendar.getYearString();
      }

      if (endMonthStr != null) {
        session.setAttribute("month2", endMonthStr);
      } else if (session.getAttribute("month2") != null) {
        endMonthStr = session.getAttribute("month2").toString();
      } else {
        session.setAttribute("month2", calendar.getMonthString());
        endMonthStr = calendar.getMonthString();
      }

      if (endDayStr != null) {
        session.setAttribute("day2", endDayStr);
      } else if (session.getAttribute("day2") != null) {
        endDayStr = session.getAttribute("day2").toString();
      } else {
        session.setAttribute("day2", calendar.getDayString());
        endDayStr = calendar.getDayString();
      }
   
      final Date date_1 = sdf.parse (startYearStr + "-" + startMonthStr + "-" + startDayStr);
      final Date date_2 = sdf.parse (endYearStr + "-" + endMonthStr + "-" + endDayStr);
      
      if (date_1.after(date_2)) {
        endYearStr = startYearStr;
        endMonthStr = startMonthStr;
        endDayStr = startDayStr;
        session.setAttribute("year2", startYearStr);
        session.setAttribute("month2", startMonthStr);
        session.setAttribute("day2", startDayStr);
      }
      
    try {
     
      initialSqlStr = "SELECT typename FROM TypeActivation WHERE tablelevel='DAYBH' and status = 'ACTIVE' ORDER BY typename";

      repCon.commit();

      statement = repCon.prepareStatement(initialSqlStr);

      // ................................................... perform query
      initialResult = statement.executeQuery();

      // ................................................... parse query result
      while (initialResult.next()) {
        initialArray.add(initialResult.getString("typename"));
      }

      ctx.put("initialArray", initialArray);
      ctx.put("search_done", "false");

      // ....................................................... search request
      if (searchDone != null && !searchStr.equals("-")) {

        // ................................................... perform first
        // search query
        
        
        searchSqlStr = "SELECT COUNT(" + searchStr + "_DAYBH.date_id) AS counted, " + searchStr
            + "_DAYBH.date_id AS date_id, dim_bhclass.description AS description FROM " + searchStr + "_DAYBH, dim_bhclass WHERE "
            + searchStr + "_DAYBH.bhclass = dim_bhclass.bhclass AND date_id >= '" + startYearStr + "-" + startMonthStr + "-"
            + startDayStr + "%' AND date_id <= '" + endYearStr + "-" + endMonthStr + "-" + endDayStr
            + "%' GROUP BY date_id, description ORDER BY date_id DESC";

        log.debug(searchSqlStr);
        
        dataCon.commit();

        statement = dataCon.prepareStatement(searchSqlStr);

        searchResult = statement.executeQuery();

        log.debug("busyhour " + searchSqlStr);

        // ................................................... parse query
        // result
        while (searchResult.next()) {
          bhResult = new BHResultSet(searchResult.getString("counted"), searchResult.getString("date_id"), searchResult
              .getString("description"));

          resultArray.add(bhResult);
        }

        // ................................................... prepare context
        ctx.put("resultArray", resultArray);
        ctx.put("search_string", searchStr);
        ctx.put("search_done", "true");

        searchResult.close();
        searchResult = null;
      }

      //This sends a vector of valid years from DIM_DATE Table.
      //This is used by cal_select_1.vm
      CalSelect calSelect = new CalSelect(dataCon);
      ctx.put("validYearRange", calSelect.getYearRange());

      // ....................................................... default actions
      ctx.put("year_1", startYearStr);
      ctx.put("month_1", startMonthStr);
      ctx.put("day_1", startDayStr);
      ctx.put("year_2", endYearStr);
      ctx.put("month_2", endMonthStr);
      ctx.put("day_2", endDayStr);
      
      outty = getTemplate("viewbhinformation.vm");
    } finally {
      if (searchResult != null) {
        searchResult.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (initialResult != null) {
        initialResult.close();
      }
    }
    return outty;
  }
}