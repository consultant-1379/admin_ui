package com.distocraft.dc5000.etl.gui.dataflow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved. <br>
 * Class for viewing rankbh information. <br>
 * dc5000rep database is used from this class.
 * 
 * @author Mark Stenback
 */
public class ViewRankBH extends EtlguiServlet {

  private static Log log = LogFactory.getLog(ViewRankBH.class);
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  
  /**
   * 
   * 
   */
  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
    //Template outty = null;
    // Date currentTime = new Date();
    DbCalendar calendar = new DbCalendar();
    String initialSqlStr = null;
    String searchSqlStr = null;
    int rowLimit = 0;
    ArrayList initialArray = new ArrayList(500);
    ArrayList resultArray = new ArrayList(500);
    ArrayList columnArray = new ArrayList(20);
    Connection repCon = null;
    Connection dataCon = null;
    PreparedStatement statement = null;
    ResultSet initialResult = null;
    ResultSet searchResult = null;

    // ........................................................... get request
    // parameters
    // calendar.setTime(currentTime);

    // read user given parameters
     String searchStr = request.getParameter("search_string"); // possible
    // tablename
    String startYearStr = request.getParameter("year_1"); // from date
    String startMonthStr = request.getParameter("month_1");
    String startDayStr = request.getParameter("day_1");
    String endYearStr = request.getParameter("year_2"); // to date
    String endMonthStr = request.getParameter("month_2");
    String endDayStr = request.getParameter("day_2");
    String searchDone = request.getParameter("search_done");
    String rls = request.getParameter("row_limit"); // row limiting count
    
    String pattern =  "^[a-zA-Z0-9_]*$";
    
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
    
    
    if(rls == null){
    	rls = "-";
    }
    
    
    if(rls.matches(pattern2)){
    	rls = StringEscapeUtils.escapeHtml(rls);
    }else{
    	rls = null;
    }
    
    
    HttpSession session = request.getSession(false);
    
    repCon = ((RockFactory) ctx.get("rockDwhRep")).getConnection();
    dataCon = ((RockFactory) ctx.get("rockDwh")).getConnection();

    if (null == rls || rls.equals("")) {
      rls = "500";
    }

    rowLimit = Integer.parseInt(rls);
    if (rowLimit < 0) {
      rowLimit = 0;
    }
    ctx.put("row_limit", String.valueOf(rowLimit));

    
     
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
      
      Date date_1 = sdf.parse(startYearStr + "-" + startMonthStr + "-" + startDayStr);
      Date date_2 = sdf.parse (endYearStr + "-" + endMonthStr + "-" + endDayStr);
      
      if (date_1.after(date_2)) {
        endYearStr = startYearStr;
        endMonthStr = startMonthStr;
        endDayStr = startDayStr;
        session.setAttribute("year2", startYearStr);
        session.setAttribute("month2", startMonthStr);
        session.setAttribute("day2", startDayStr);
      }
      
     
   try {
        
      // ....................................................... retrieve DAYBH
      // tables
      initialSqlStr = "SELECT typename FROM TypeActivation WHERE tablelevel='RANKBH' and status = 'ACTIVE' ORDER BY typename";

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
        // ................................................... convert string to
        // int
        /*
        startYearInt = Integer.parseInt(startYearStr);
        startMonthInt = Integer.parseInt(startMonthStr);
        startDayInt = Integer.parseInt(startDayStr);

        endYearInt = Integer.parseInt(endYearStr);
        endMonthInt = Integer.parseInt(endMonthStr);
        endDayInt = Integer.parseInt(endDayStr);
        */
        // ................................................... perform search
        // query
        searchSqlStr = "SELECT * FROM " + searchStr + "_RANKBH WHERE date_id >= '" + startYearStr + "-" + startMonthStr + "-"
            + startDayStr + "' AND date_id <= '" + endYearStr + "-" + endMonthStr + "-" + endDayStr
            + "' ORDER BY date_id DESC";

        dataCon.commit();

        statement = dataCon.prepareStatement(searchSqlStr);

        statement.setMaxRows(rowLimit); // PRa 17.11.2004

        searchResult = statement.executeQuery();
        log.debug(searchSqlStr);
        // ................................................... retrieve result
        // data
        ResultSetMetaData resultMetaData = searchResult.getMetaData();

        int columnCount = resultMetaData.getColumnCount();

        for (int i = 1; i < columnCount; i++) {
          columnArray.add(resultMetaData.getColumnName(i));
        }

        resultArray.add(columnArray);

        while (searchResult.next()) {
          ArrayList tmpArray = new ArrayList();

          for (int i = 1; i < columnCount; i++) {
            String value = searchResult.getString(i);
            if( value == null || value.equalsIgnoreCase("null") ) {
              value = "null";
            }
            
            tmpArray.add(value);
          }

          resultArray.add(tmpArray);
        }

        Integer rowLimitEx = new Integer(resultArray.size() - rowLimit);

        ctx.put("rowLimitEx", rowLimitEx);

        // ................................................... prepare context
        ctx.put("resultArray", resultArray);
        ctx.put("search_string", searchStr);
        ctx.put("search_done", "true");

        // ................................................... close data
        // connection
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
    
    return getTemplate("viewrankbh.vm");
  }
}