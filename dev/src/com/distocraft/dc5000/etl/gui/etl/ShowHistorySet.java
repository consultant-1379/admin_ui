/*
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package com.distocraft.dc5000.etl.gui.etl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
import com.distocraft.dc5000.etl.gui.util.Helper;
import com.distocraft.dc5000.etl.gui.util.ParamValue;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved. <br>
 * Shows history sets. Uses dc5000rep database.
 * 
 * @author Antti Laurila
 */
public class ShowHistorySet extends EtlguiServlet { // NOPMD by eheijun on 02/06/11 15:31

  private static final long serialVersionUID = 1L;
  
  private final Log log = LogFactory.getLog(this.getClass()); // general logger

  @Override
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, // NOPMD by eheijun on 02/06/11 15:31
      final Context ctx) throws Exception {  // NOPMD by eheijun on 02/06/11 15:31
    final List<List<String>> etlSets; // contains the sets of some specific type
    final List<String> etlSetTypes; // contains all possible tech packs
    Template outty = null;
    final String page = "etlShowHistory.vm"; // show this page if nothing else is
    // stated
    final HttpSession session = request.getSession(false);
    final RockFactory rockEtlRep = (RockFactory) ctx.get("rockEtlRep"); 

    // get the current date (is used at the UI, if none given)
    final DbCalendar calendar = new DbCalendar();

    // read user parameters
    final String searchstring = StringEscapeUtils.escapeHtml(request.getParameter(Helper.PARAM_SEARCH_STRING));
    String selectedsettype =  request.getParameter(Helper.PARAM_SELECTED_SET_TYPE);
    String selectedtechpack = request.getParameter(Helper.PARAM_SELECTED_TECHPACK);

    if (selectedsettype == null) {
      selectedsettype = "ALL";
    }

    String year_1 = request.getParameter("year_1");
    String month_1 =  request.getParameter("month_1");
    String day_1 =  request.getParameter("day_1");

    
    String pattern =  "^[a-zA-Z0-9_-]*$";
    
    if (selectedsettype == null) {
        selectedsettype = "ALL";
      }
    if(selectedsettype.matches(pattern)){
    	selectedsettype = StringEscapeUtils.escapeHtml(selectedsettype);
    }else{
    	selectedsettype = "ALL";
    }
    
    if(selectedtechpack == null){
    	selectedtechpack = "*";
    }
    if(selectedtechpack.matches(pattern)){
    	selectedtechpack = StringEscapeUtils.escapeHtml(selectedtechpack);
    }else{
    	selectedtechpack = null;
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

    if (selectedtechpack != null) {
      session.setAttribute("packageName", selectedtechpack);
    } else if (session.getAttribute("packageName") != null) {
      selectedtechpack = session.getAttribute("packageName").toString();
    } else {
      session.setAttribute("packageName", "-");
    }

    final ParamValue settype = new ParamValue();
    final ParamValue search = new ParamValue();
    final ParamValue tpack = new ParamValue();

    if (Helper.isNotEmpty(year_1)) {
      settype.setParam(Helper.PARAM_SELECTED_SET_TYPE);
      settype.setValue(selectedsettype);
      settype.setSelectedTag("selected");
      settype.setSelected(true);

      search.setParam(Helper.PARAM_SEARCH_STRING);
      search.setValue(searchstring);
      search.setSelectedTag("");
      search.setSelected(true);

      tpack.setParam(Helper.PARAM_SELECTED_TECHPACK);
      tpack.setValue(selectedtechpack);
      tpack.setSelectedTag("selected");
      tpack.setSelected(true);
    }
    if (year_1 == null) {
      year_1 = calendar.getYearString();
      month_1 = calendar.getMonthString();
      day_1 = calendar.getDayString();
    }

    // This sends a vector of valid years from DIM_DATE Table.
    // This is used by cal_select_1.vm
    final RockFactory rockDwh = ((RockFactory) ctx.get("rockDwh"));
    final CalSelect calSelect = new CalSelect(rockDwh.getConnection());
    ctx.put("validYearRange", calSelect.getYearRange());

    ctx.put("year_1", year_1);
    ctx.put("month_1", month_1);
    ctx.put("day_1", day_1);
    ctx.put("selectedpack", selectedtechpack);
    ctx.put("search", search);

    // find all possible set types
    etlSetTypes = fetchHierarchy(rockEtlRep.getConnection());

    // if user has selected a type then search those
    final int maxHistoryRows = Helper.getEnvEntryInt("maxHistoryRows");
    etlSets = fetchSets(rockEtlRep.getConnection(), selectedtechpack, day_1, month_1, year_1, selectedsettype, searchstring,
        String.valueOf(maxHistoryRows));
    ctx.put("toomany", etlSets.size() >= maxHistoryRows ? String.valueOf(maxHistoryRows) : " ");

    // put the sets to context
    ctx.put("settypes", etlSetTypes);
    ctx.put("etlsets", etlSets);
    ctx.put("type", settype);
    ctx.put("selectedpack", selectedtechpack);
    // return the page that should be shown
    outty = getTemplate(page);
    return outty;
  }

  /**
   * Fecth the list from that user can select the teck pack. <br>
   * META_COLLECTION_SETS table is used.
   * 
   * @return listhierarchy
   */
  public List<String> fetchHierarchy(final Connection conn) {
    final List<String> retval = new ArrayList<String>();

    //
    // Sql statement as a string
    // get all unique tech pack names (COLLECTION_NAME)
    final String sqlStmt = "SELECT DISTINCT COLLECTION_SET_NAME FROM META_COLLECTION_SETS ORDER BY COLLECTION_SET_NAME";

    PreparedStatement pstmt = null;
    ResultSet rset = null;
    //
    try {
      log.debug("Executing query: " + sqlStmt);

      pstmt = conn.prepareStatement(sqlStmt);
      rset = pstmt.executeQuery();

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
        if (pstmt != null) {
          pstmt.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
    }

    // return with result (tech packs)
    return retval;
  }

  private List<List<String>> fetchSets(final Connection conn, final String techpack, final String day, final String month, // NOPMD by eheijun on 02/06/11 15:31
      final String year, final String settype, 
      final String text, final String rowcount) {
    final List<List<String>> retval = new ArrayList<List<String>>();

    final String dataTime = year + "-" + month + "-" + day;
    final String dataTimeBegin = dataTime + " 00:00:00.000";
    final String dataTimeEnd = dataTime + " 23:59:59.999";
    String stringAll = "";

    //
    // Sql statement as a string
    // Sql statement as a string
    PreparedStatement pstmt = null;
    ResultSet rset = null;
    if (settype.equals("ALL")) {
      stringAll = " ";
    } else {
      stringAll = "B.SETTYPE = ? AND ";
    }
    //
    // Sql statement as a string
    
    final String sqlStmt = "SELECT TOP ? B.ID, B.START_DATE, B.END_DATE, B.FAIL_FLAG, B.STATUS, "
     + "B.VERSION_NUMBER, B.META_COLLECTION_SET_NAME, B.META_COLLECTION_NAME, B.SETTYPE, B.SCHEDULING_INFO, B.SERVICE_NODE "
            + "FROM META_TRANSFER_BATCHES B WHERE B.START_DATE BETWEEN ? AND ? AND "
            + "B.META_COLLECTION_SET_NAME = ? AND " + stringAll
            + "B.META_COLLECTION_NAME LIKE ? escape '\\' ORDER BY 3 DESC, 2";
    String SETTYPE=  settype;
    try {
      log.debug("Executing query: " + sqlStmt);
      if (settype.equals("ALL")) {
    	  pstmt = conn.prepareStatement(sqlStmt);
          pstmt.setInt(1, Integer.parseInt(rowcount));
          pstmt.setString(2, dataTimeBegin);
          pstmt.setString(3, dataTimeEnd);
          pstmt.setString(4, techpack);
          pstmt.setString(5, "%" + text + "%");
          
        } else {
           pstmt = conn.prepareStatement(sqlStmt);
           pstmt.setInt(1, Integer.parseInt(rowcount));
           pstmt.setString(2, dataTimeBegin);
           pstmt.setString(3, dataTimeEnd);
           pstmt.setString(4, techpack);
           pstmt.setString(5, SETTYPE);
           pstmt.setString(6, "%" + text + "%");

        }
      rset = pstmt.executeQuery();
    
      while (rset.next()) {
        final List<String> l = new ArrayList<String>(); // NOPMD by eheijun on 02/06/11 15:31

        l.add(rset.getString("META_COLLECTION_SET_NAME")); // collection name
        l.add(rset.getString("SETTYPE")); // set type
        l.add(rset.getString("START_DATE")); // start time
        l.add(rset.getString("END_DATE")); // end time
        l.add(rset.getString("STATUS")); // status
        l.add(rset.getString("VERSION_NUMBER")); // version
        l.add(rset.getString("META_COLLECTION_NAME")); // version
        
        if (rset.getString("SCHEDULING_INFO") != null) {
          if (rset.getString("SCHEDULING_INFO").isEmpty()) {
            l.add("");
          } else {
            if (rset.getString("SCHEDULING_INFO").indexOf("Date") != -1) {
              l.add(rset.getString("SCHEDULING_INFO").substring(0, rset.getString("SCHEDULING_INFO").indexOf("Date")));
            } else {
              l.add("");
            }
          }
        } else {
          l.add("");
        }
        
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
        if (pstmt != null) {
          pstmt.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
    }

    // return the executed sets
    return retval;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
   */
  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
  }

}