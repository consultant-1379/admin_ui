package com.distocraft.dc5000.etl.gui.monitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.lang.String;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.commons.lang.StringEscapeUtils;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.CalSelect;
import com.ericsson.eniq.common.CalcFirstDayOfWeek;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.util.DateFormatter;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved. <br>
 * Class uses dc5000dwh database
 * 
 * @author Jani Vesterinen
 */
public class ShowAggregations extends EtlguiServlet {

  private Log log = LogFactory.getLog(this.getClass());

  private static final SimpleDateFormat sdf_nosecs = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  
  private int firstDayOfTheWeek = CalcFirstDayOfWeek.calcFirstDayOfWeek();

  //private static final SimpleDateFormat sdf_secs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  /**
   * @see org.apache.velocity.servlet.VelocityServlet#handleRequest(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.apache.velocity.context.Context)
   */
  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) {
    Template outty = null;

    Connection connDRep = ((RockFactory) ctx.get("rockDwhRep")).getConnection();

    HttpSession session = request.getSession(false);

    // get the current date (is used at the UI, if none given)
    DbCalendar calendar = new DbCalendar();
    calendar.setFirstDayOfWeek(firstDayOfTheWeek);
    calendar.setMinimalDaysInFirstWeek(4);

    // calendar.setTime(new Date());

    // check if user has given any parameters
    // day parameters
    String year_1 = request.getParameter("year_1");
    String month_1 = request.getParameter("month_1");
    String day_1 = request.getParameter("day_1");
    String subType = request.getParameter("subtype");
    String type = request.getParameter("type");
    String details = request.getParameter("details");
    String disabled = request.getParameter("disabled");
    final String aggregateinfo = request.getParameter("aggregateinfo");

    final String button = StringEscapeUtils.escapeHtml(request.getParameter("value"));
    
    
   String pattern =  "^[a-zA-Z0-9_-]*$";
    
    if(type == null){
    	type = "-";
    }
    if(type.matches(pattern)){
    	type = StringEscapeUtils.escapeHtml(type);
    }else{
    	type = null;
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
    
    if (type != null) {
      session.setAttribute("tpName", type);
    } else if (session.getAttribute("tpName") != null) {
      type = session.getAttribute("tpName").toString();
    } else {
      session.setAttribute("tpName", "-");
    }
    
    if (details == null) {
        details = "";
    }
    
    if (disabled == null) {
    	disabled = "";
    }
    ctx.put("disabled", disabled);

    if (subType == null) {
      subType = "";
    }
    ctx.put("subtype", subType);

    final String meas_group = type;

    if (meas_group == null) {
      ctx.put("theGroup", "-");
    } else {
      ctx.put("theGroup", meas_group);
    }
    
    List<String> tps = Util.getActiveNonEventsTechPacks(connDRep);
    ctx.put("distinctTechPacks", tps);

    List<String> meaTypes = Util.getMeasurementTypes(type, connDRep);

    ctx.put("twoLevelSelectionBuilder", meaTypes);

    // if user has given some parameter's set them to screen
    // if not - set default ones
    if (type == null) {
      ctx.put("type", "-");
      type = "";
    } else if (type.equals("-")) {
      ctx.put("type", "-");
      type = "";
    } else {
      ctx.put("type", type);
    }
    
    // sqlCommands = makeSqlCommands("'" + year_1 + "-" + month_1 + "-" + day_1
    // + "'");

    if (year_1 == null) {
      year_1 = calendar.getYearString();
      month_1 = calendar.getMonthString();
      day_1 = calendar.getDayString();
    } else if (button != null) {
     
      Vector lateRaw = findHoles(year_1, month_1, day_1, type, ctx);
      String max = "unknown";
      if (lateRaw.size() > 0) {
        max = (String) lateRaw.remove(0);
      }
      ctx.put("maxtime", max);
    }
   
    //This sends a vector of valid years from DIM_DATE Table.
    //This is used by cal_select_1.vm
    Connection conn = ((RockFactory) ctx.get("rockDwh")).getConnection();

    CalSelect calSelect = new CalSelect(conn);
    ctx.put("validYearRange", calSelect.getYearRange());

    ctx.put("year_1", year_1);
    ctx.put("month_1", month_1);
    ctx.put("day_1", day_1);
    ctx.put("aggregateinfo", aggregateinfo);
    // check this out when further developing functionality..
    try {
      outty = getTemplate("show_aggregations.vm");
    } catch (ResourceNotFoundException e) {
      log.debug("ResourceNotFoundException (getTemplate):", e);
    } catch (ParseErrorException e) {
      log.debug("ParseErrorException (getTemplate): " + e);
    } catch (Exception e) {
      log.debug("Exception (getTemplate): " + e);
    } 
    
    return outty;
  }


  /**
   * This method uses log_aggregationstatus.
   * 
   * @param year
   * @param month
   * @param day
   * @param typeName
   * @param problematic
   * @param timeLevel
   * @param details
   * @param conn
   * @param ctx
   * @return dataholes
   */
  private Vector findHoles(String year, String month, String day, String typeName, Context ctx) {

    Connection connDRep = ((RockFactory) ctx.get("rockDwhRep")).getConnection();
    Connection conn = ((RockFactory) ctx.get("rockDwh")).getConnection();
    
    boolean showDisabled = false;
    final String paramDisabled = (String)ctx.get("disabled");
    if(paramDisabled!=null && paramDisabled.equalsIgnoreCase("disabled")) {
    	showDisabled = true;
    }
    
    Vector result = new Vector();
    Vector validatedTypes = new Vector();
    String maxSql = "SELECT DATEFORMAT(MAX(modified),'yyyy-mm-dd hh:nn:ss') AS modified FROM LOG_LOADSTATUS";
  
    String dataTime = year + "-" + month + "-" + day;

    log.debug("Type :" + typeName);
    String meaTypeList = "AND typename IN (";

    List ll = new ArrayList();

    log.debug("Typename length:" + typeName.length());
    
    // if tech pack is selected get all tech packs active meatypes
    // else get all measurement types which
    
    if (typeName != null && !typeName.equals("") && typeName.length() > 1) {
      ll = Util.getActiveDayMeasurementTypes(typeName, connDRep);
    } else {
      ll = Util.getAllActiveDayMeasurementTypes(connDRep);
    }

    int listLength = ll.size();

    log.debug("List size: " + ll.size());

    if (listLength > 0) {

      ListIterator li = ll.listIterator();

      for (int i = 0; i < ll.size(); i++) {
        meaTypeList += "'" + li.next().toString() + "'";
        meaTypeList += ",";
      }
      meaTypeList = meaTypeList.substring(0, meaTypeList.length() - 1); 
      meaTypeList += ")";
    } else {
      meaTypeList = "";
    }

    /* Checking if tech pack typename is given but no meatypes are found */
    if ((typeName != null && !typeName.equals("") && typeName.length() > 1) && meaTypeList.equals("")) {
      
      /* In that case, no data should be shown, returning empty vectors */
      ctx.put("day", new Vector());
      ctx.put("week", new Vector());
      ctx.put("month", new Vector());
      
      return validatedTypes;
    }
    
    String date = "yyyy-MM-dd";
    DateFormatter df = new DateFormatter(date);
    df.setCalendar(dataTime);
    
   // HU42331
    String date1 = "yyyy-MM-dd";
    boolean diff_flag = false;
    Date parse_date = null;
    
    SimpleDateFormat sdd = new SimpleDateFormat(date1);
        
    try {
    	Date System_date = new Date();
    	parse_date = sdd.parse(dataTime); 
    	long diff = parse_date.getTime()-System_date.getTime();
        long diffInDays = diff/(24*60*60*1000);
    	if((diffInDays+1) > 2){
    		diff_flag = true;
    	}
    	log.debug("Future Data : diffInDays : " + diffInDays + " - diff_flag : " +diff_flag);
    	ctx.put("future_date",diff_flag);
    }catch(Exception e){
    	e.printStackTrace();
    }
    //DateFormatter df2 = new DateFormatter(date + "-HH:mm");
    //df2.setCalendar(dataTime); // + "-11:00");

    //DateFormatter df3 = new DateFormatter(date);
    //df3.setCalendar(dataTime);
    List<String> v = df.reverseTimeDay(14);
    df.setCalendar(dataTime);
    List<String> week = df.reverseTimeDateYear(56);
    //DateFormatter df4 = new DateFormatter(date);
    //df4.setCalendar(dataTime);
    df.setCalendar(dataTime);
    List<String> monthr = df.reverseTimeMonth(df.getTime());
    ctx.put("days", v);

    //DateFormatter df5 = new DateFormatter(date);
    //df5.setCalendar(dataTime);
    df.setCalendar(dataTime);
    List<String> v_week = df.reverseTimeWeek(8);

    //DateFormatter df6 = new DateFormatter(date);
    //df6.setCalendar(dataTime);
    df.setCalendar(dataTime);
    List<String> v_month = df.reverseTimeMonth(df.getTime());

   
    // gets only the week timelevels
    String sql_week = "SELECT typename, timelevel, aggregation, datadate, status, description "
        + "FROM LOG_AggregationStatus WHERE (DataDate between date('" + dataTime + "') - 55 and date('"
        + dataTime + "') " + " " + meaTypeList + ") AND aggregation LIKE '%WEEK%'" +
                " ORDER BY aggregation, typename, timelevel, datadate desc";

    // gets only the month timelevels
    // Calendar tmp = Calendar.getInstance();
    DateFormatter dft = new DateFormatter("yyyy-MM-dd");
    dft.setCalendar(dataTime);
    dft.getTime().set(Calendar.DAY_OF_MONTH, 1);
    // int startdate = dft.getTime().get(Calendar.DAY_OF_YEAR);
    dft.getTime().add(Calendar.MONTH, -(4));
    dft.getTime().set(Calendar.DAY_OF_MONTH, 1);
    // int minusdays = startdate - dft.getTime().get(Calendar.DAY_OF_YEAR);

    String sql_month = "SELECT typename, timelevel, aggregation, datadate, status, description "
        + "FROM LOG_AggregationStatus WHERE (DataDate between date('" + dataTime + "') - 122" + " and date('"
        + dataTime + "') " + " " + meaTypeList + ") AND aggregation LIKE '%MONTH%'" +
                " ORDER BY aggregation, typename, timelevel, datadate desc";

    // select maximum modifed value
    lastModificationTime(conn, result, maxSql);

    // gets all the timelevel rows
    String sql = "SELECT typename, timelevel, aggregation, datadate, status, description "
        + "FROM LOG_AggregationStatus WHERE (DataDate between date('" + dataTime + "') - 13 and date('"
        + dataTime + "') " + " " + meaTypeList + ")"
        + " ORDER BY aggregation, typename, timelevel, datadate desc";
    
    

    Statement stmt = null;
    ResultSet rset = null;

    try {
      // make the actual "status" query to the monitoring tables
      stmt = conn.createStatement();
      log.debug("Executing query: " + sql);
      rset = stmt.executeQuery(sql);

      Vector typeNameVector = null;
      //String lastTypeName = "";
      String lastTimeLevel = "";
      String lastaggregation = "";
      while (rset.next()) {
        // what is the typename - group them into the vectors by type names
        String tmpTypeName = rset.getString(1);
        String aggregation = rset.getString(3);
        String tmpTimeLevel = rset.getString(2);
        boolean isEnabled = true;
        // check if we have a new typename
        // if (!tmpTypeName.equals(lastTypeName) ||
        // !tmpTimeLevel.equals(lastTimeLevel)) {
        if (!aggregation.equals(lastaggregation) || !tmpTimeLevel.equals(lastTimeLevel)) {
          if (typeNameVector != null) {
            result.add(typeNameVector);
          }
          typeNameVector = new Vector();
          // log.debug("Created new vector, values lastagg: '"+aggregation+"'
          // timelevel: '"+tmpTimeLevel+"'" );
        }
        

        // this is actual data container
        Vector tmpVect = new Vector();
       
        //check set is enabled
        //String value = Util.getEnabledSet(aggregation,conn);        	
        // tmpVect.add(tmpTypeName);
        tmpVect.add(aggregation);
        //lastTypeName = tmpTypeName;
        lastaggregation = aggregation;
        tmpVect.add(tmpTypeName);

        lastTimeLevel = tmpTimeLevel;// typename, timelevel, aggregation,
        // datadate, datatime, status, description
        tmpVect.add(rset.getString("aggregation"));
        tmpVect.add(rset.getDate("datadate").toString());
        tmpVect.add(sdf_nosecs.format(new Date(rset.getDate("datadate").getTime())));
        tmpVect.add(rset.getString("status"));
        tmpVect.add(rset.getString("description"));
        tmpVect.add("enable");
    	isEnabled = true;
        /*if (value.length() > 0){
        	tmpVect.add("enable");
        	isEnabled = true;
        }else{
        	tmpVect.add("disable");
        	isEnabled = false;
        }*/
        tmpVect.add(rset.getString("status"));
        tmpVect.add("n/a");
        if(isEnabled || showDisabled) {
        	// same type as previous or the first one
        	typeNameVector.add(tmpVect);
        }

      }
      result.add(typeNameVector);
      if (typeNameVector == null) {
        ctx.put("day", new Vector());
      } else {
        validatedTypes = validateDates(v, result);
        // builds different timelevels. 3 different context attributes are set,
        // day, week, month
        buildDifferentTimelevels(ctx, validatedTypes);
      }
      
      // SQL for getting weekly aggregations
      // make the actual "status" query to the monitoring tables
      Statement stmt_week = null;
      ResultSet rset_week = null;
      try {
        stmt_week = conn.createStatement();
        log.debug("Executing query: " + sql_week);
        rset_week = stmt_week.executeQuery(sql_week);

        typeNameVector = null;
        //lastTypeName = "";
        lastTimeLevel = "";
        lastaggregation = "";
        result = new Vector();
        while (rset_week.next()) {
          // what is the typename - group them into the vectors by type names
          String tmpTypeName = rset_week.getString(1);
          String aggregation = rset_week.getString(3);
          String tmpTimeLevel = rset_week.getString(2);
          boolean isEnabled = true;
          // check if we have a new typename
          // if (!tmpTypeName.equals(lastTypeName) ||
          // !tmpTimeLevel.equals(lastTimeLevel)) {
          if (!aggregation.equals(lastaggregation) || !tmpTimeLevel.equals(lastTimeLevel)) {
            if (typeNameVector != null) {
              result.add(typeNameVector);
            }
            typeNameVector = new Vector();
          }

          // this is actual data container
          Vector tmpVect = new Vector();
          //check set is enabled
          //String value = Util.getEnabledSet(aggregation,conn);
          // tmpVect.add(tmpTypeName);
          tmpVect.add(aggregation);
          //lastTypeName = tmpTypeName;
          lastaggregation = aggregation;
          tmpVect.add(tmpTypeName);

          lastTimeLevel = tmpTimeLevel;// typename, timelevel, aggregation,
          // datadate, datatime, status, description
          tmpVect.add(rset_week.getString("aggregation"));
          tmpVect.add(rset_week.getDate("datadate").toString());
          tmpVect.add(sdf_nosecs.format(new Date(rset_week.getDate("datadate").getTime())));
          tmpVect.add(rset_week.getString("status"));
          tmpVect.add(rset_week.getString("description"));
          tmpVect.add("enable");
          isEnabled = true;
          /*if (value.length() > 0){
            tmpVect.add("enable");
            isEnabled = true;
          }else{
          	tmpVect.add("disable");
          	isEnabled = false;
          }*/
          tmpVect.add(rset_week.getString("status"));
          tmpVect.add("n/a");
          if(isEnabled || showDisabled) {
        	  // same type as previous or the first one
        	  typeNameVector.add(tmpVect);
          }
        }
      } catch (SQLException se) {
        throw se;
      } finally {
        rset_week.close();
        stmt_week.close();
      }
      result.add(typeNameVector);
      if (typeNameVector == null) {
        ctx.put("week", new Vector());
      } else {
        Vector validatedTypes_week = validateWeeks(week, result, year);
        log.debug("All sqls processed.");
        ctx.put("v_week", v_week);
        ctx.put("week", validatedTypes_week);
      }
      
      
      // SQL for getting monthly aggregations
      // make the actual "status" query to the monitoring tables
      Statement stmt_month = null;
      ResultSet rset_month = null;
      try {
      stmt_month = conn.createStatement();
      log.debug("Executing month query: " + sql_month);
      rset_month = stmt_month.executeQuery(sql_month);

      typeNameVector = null;
      //lastTypeName = "";
      lastTimeLevel = "";
      lastaggregation = "";
      //log.debug("Creating new vector");
      
      result = new Vector();
      
      while (rset_month.next()) {
        
        log.debug("Month result > 1"); 
        // what is the typename - group them into the vectors by type names
        String tmpTypeName = rset_month.getString(1);
        String aggregation = rset_month.getString(3);
        String tmpTimeLevel = rset_month.getString(2);
        boolean isEnabled = true;
        // check if we have a new typename
        // if (!tmpTypeName.equals(lastTypeName) ||
        // !tmpTimeLevel.equals(lastTimeLevel)) {
        if (!aggregation.equals(lastaggregation) || !tmpTimeLevel.equals(lastTimeLevel)) {
          if (typeNameVector != null) {
            result.add(typeNameVector);
          }
          typeNameVector = new Vector();
        }

        // this is actual data container
        Vector tmpVect = new Vector();
        //check set is enabled
        //String value = Util.getEnabledSet(aggregation,conn);
        // tmpVect.add(tmpTypeName);
        tmpVect.add(aggregation);
        //lastTypeName = tmpTypeName;
        lastaggregation = aggregation;
        tmpVect.add(tmpTypeName);

        lastTimeLevel = tmpTimeLevel;// typename, timelevel, aggregation,
        // datadate, datatime, status, description
        tmpVect.add(rset_month.getString("aggregation"));
        tmpVect.add(rset_month.getDate("datadate").toString());
        tmpVect.add(sdf_nosecs.format(new Date(rset_month.getDate("datadate").getTime())));
        tmpVect.add(rset_month.getString("status"));
        tmpVect.add(rset_month.getString("description"));
        tmpVect.add("enable");
      	isEnabled = true;
        /*if (value.length() > 0){
          	log.debug("Enabled Sets : " + aggregation );
          	tmpVect.add("enable");
          	isEnabled = true;
          }else{
          	log.debug("Disabled sets: " + aggregation );
          	tmpVect.add("disable");
          	isEnabled = false;
          }*/
        tmpVect.add(rset_month.getString("status"));
        tmpVect.add("n/a");
        if(isEnabled || showDisabled) {
        	// same type as previous or the first one
        	typeNameVector.add(tmpVect);
        }
      }
      } catch (SQLException se) {
        throw se;
      } finally {
        rset_month.close();
        stmt_month.close();
      }
      result.add(typeNameVector);

      //log.debug("TypeNameVector size: " +typeNameVector.size());
      
      if (typeNameVector == null) {
        ctx.put("month", new Vector());
      } else {
        Vector validatedTypes_month = validateMonths(monthr, result, year);
        log.debug("All sqls processed.");
        ctx.put("v_month", v_month);
        ctx.put("month", validatedTypes_month);
      }
    } catch (SQLException ex) {
      log.error("SQLException: " + ex.getMessage(), ex);
      log.error("Excepted SQL statement was: " + sql);
    } catch (Exception ex) {
      log.error("Exception: " + ex.getMessage(), ex);
    } finally {
      try {
        if (rset != null) {
          rset.close();
        }
        if (stmt != null) {
          stmt.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
    }

    return validatedTypes;
  }

  /**
   * This method will retrive the maximum last modification time.
   * 
   * @param conn
   * @param result
   * @param maxSql
   */
  
  private void lastModificationTime(Connection conn, Vector result, String maxSql) {
    Statement stmtMax = null;
    ResultSet rsetMax = null;
    try {
      stmtMax = conn.createStatement();
      log.debug("Executing query: " + maxSql);
      rsetMax = stmtMax.executeQuery(maxSql);

      // see if we have value at the DB - if not set "last update" as unknown
      if (rsetMax.next()) {
        //Date dd = rsetMax.getTimestamp(1);
        //if (dd != null) {
        //  result.add(sdf_secs.format(dd));
        String maxModified = rsetMax.getString(1);
        if (maxModified != null) {
          result.add(maxModified);
        } else {
          result.add("unknown");
        }
      } else {
        result.add("unknown");
      }
      // max query done
    } catch (SQLException e) {
      log.error("SQLException: ", e);
      log.error("Excepted SQL statement was: " + maxSql);
    } finally {
      try {
        rsetMax.close();
        stmtMax.close();
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
      
    }
  }
  
  private synchronized Vector validateWeeks(List<String> week, Vector typeNameVector, String year) {
	    Vector validated = new Vector();
	    String curr_agg_status=null;
	    String temp_agg_status=null;
	    String temp_date=null;
	    String curr_date=null;
	    DateFormatter tmpDf = new DateFormatter("yyyy-MM-dd");// hh:mm:ss.mm");
	    // Typenamevector is vector that holds all the rows
	    // First typenamevector is filled with blanks if necessary
	    
	    for (int i = 0; i < typeNameVector.size(); i++) {
	      Vector tmptypename = (Vector) typeNameVector.get(i);
	      Vector validatedTypename = new Vector();
	      temp_agg_status=null;
	      temp_date=null;
	      int y = 0;
	      for (int x = 0; x < tmptypename.size(); x++) {
	        
	        // typenamevalues is one unit of information in row
	        Vector typenamevalues = (Vector) tmptypename.get(x);
	        
	        // iterates all the dates, and if the date doesnt exist, puts blank
	        // information vector
	        tmpDf.setCalendar((String) typenamevalues.get(3));

	        
	        tmpDf.getTime().set(Calendar.HOUR_OF_DAY, 11);
	        tmpDf.getTime().setFirstDayOfWeek(firstDayOfTheWeek);
	        tmpDf.getTime().setMinimalDaysInFirstWeek(4);
	        tmpDf.getTime().set(Calendar.DAY_OF_WEEK, firstDayOfTheWeek);
	        DateFormatter counterCal = new DateFormatter("yyyy-MM-dd");
	        if (y == 56) {
	          y = 55;
	        }
	        counterCal.getTime().setFirstDayOfWeek(firstDayOfTheWeek);
	        counterCal.getTime().setMinimalDaysInFirstWeek(4);
	        try{
	        counterCal.setCalendar((String)week.get(y));
	        }
	        catch(Exception ex){
	        	log.error("Exception: " + ex.getMessage(), ex);
	        }
	        
	        /*if (((String) week.get(y)).equals("01-01")) {
	          counterCal.setCalendar((String) week.get(y) + "-" + (tmpDf.getTime().get(Calendar.YEAR) + 1));
	        } else {
	          counterCal.setCalendar((String) week.get(y) + "-" + (tmpDf.getTime().get(Calendar.YEAR)));
	        }*/
	        
	        counterCal.getTime().set(Calendar.DAY_OF_WEEK, firstDayOfTheWeek);

	        curr_date=tmpDf.getTime().getTime().toString();
	        curr_agg_status=(String) typenamevalues.get(5);
	        
	        	        
	        log.debug("DAY/WEEK aggregation " + tmpDf.getTime().getTime().toString());
	        log.debug("DAY/WEEK date  " + counterCal.getTime().getTime().toString());
	        
	        boolean isWeek = false;
	        
	        if(!curr_date.equalsIgnoreCase( temp_date)){
	        		        
		        if (tmpDf.getFormattedDate(tmpDf.getTime()).equals(counterCal.getFormattedDate(counterCal.getTime()))) {
		        	if(validatedTypename.size()<8){
		        	validatedTypename.add(typenamevalues);
		        	}
		          y = y + 7;
		          isWeek = true;
		        }
		
		        if (!isWeek) {
		          Vector blank = new Vector();
		          blank.add((String) typenamevalues.get(0));
		          blank.add((String) typenamevalues.get(1));
		          for (int z = 0; z < 7; z++) {
		            blank.add("blank");
		          }
				  blank.add("n/a");
		          // log.debug("not valid: " +
		          // tmpDf.getFormattedDate(tmpDf.getTime()));
				  if(validatedTypename.size()<8){
		          validatedTypename.add(blank);
				  }
		          y = y + 7;
		          x--;
		        }
		        temp_date=curr_date;
		        temp_agg_status=curr_agg_status;
	        }
	        else{
	        	    if((temp_agg_status.equalsIgnoreCase( "AGGREGATED")) && !(curr_agg_status.equalsIgnoreCase( "AGGREGATED")))
	        		{
	        	    	validatedTypename.set((validatedTypename.size()-1),typenamevalues);
	        		}
	        	        		
	        	continue;	
	         }
	      }
	      validated.add(validatedTypename);
	    } 
	    return validated;
	  }
  
  public String isYearChange(DateFormatter tmpDf, String year) {
    if (tmpDf.getTime().get(Calendar.YEAR) < Integer.parseInt(year)) {
      return String.valueOf(tmpDf.getTime().get(Calendar.YEAR));
    } else {
      return year;
    }
  }

  private Vector validateMonths(List<String> week, Vector typeNameVector, String year) {
    Vector validated = new Vector();
    DateFormatter tmpDf = new DateFormatter("yyyy-MM-dd");// hh:mm:ss.mm");
    // Typenamevector is vector that holds all the rows
    // First typenamevector is filled with blanks if necessary

    for (int i = 0; i < typeNameVector.size(); i++) {
      Vector tmptypename = (Vector) typeNameVector.get(i);
      Vector validatedTypename = new Vector();
      int y = 0;
      for (int x = 0; x < tmptypename.size() && y < 4; x++) {
        // typenamevalues is one unit of information in row
        Vector typenamevalues = (Vector) tmptypename.get(x);
        // iterates all the dates, and if the date doesnt exist, puts blank
        // information vector
        tmpDf.setCalendar((String) typenamevalues.get(3));
        tmpDf.getTime().set(Calendar.DATE, 1);
        DateFormatter counterCal = new DateFormatter("yyyy/MM");
        counterCal.setCalendar((String) week.get(y));
        counterCal.getTime().set(Calendar.DATE, 1);
        log.debug("month aggregointi " + tmpDf.getTime().getTime().toString());
        log.debug("month pvm " + counterCal.getTime().getTime().toString());
        boolean isWeek = false;
        if (tmpDf.getFormattedDate(tmpDf.getTime()).equals(counterCal.getFormattedDate(counterCal.getTime()))) {
          validatedTypename.add(typenamevalues);
          y = y + 1;
          isWeek = true;
        }

        if (!isWeek) {
          Vector blank = new Vector();
          blank.add((String) typenamevalues.get(0));
          blank.add((String) typenamevalues.get(1));
          for (int z = 0; z < 6; z++) {
            blank.add("blank");
          }
          // log.debug("not valid: " +
          // tmpDf.getFormattedDate(tmpDf.getTime()));
          validatedTypename.add(blank);
          y = y + 1;
          x--;
        }
      }
      validated.add(validatedTypename);
    }
    return validated;
  }

  /**
   * Builds different timelevels. 3 different context attributes are set, day,
   * week, month
   * 
   * @param ctx
   * @param validatedTypes
   */
  public void buildDifferentTimelevels(Context ctx, Vector validatedTypes) {
    Vector day = new Vector();
    // Vector week = new Vector();
    // Vector month = new Vector();

    if (validatedTypes != null) {
      for (int i = 1; i < validatedTypes.size(); i++) {
        Vector tmptypename = (Vector) validatedTypes.get(i);
        for (int x = 0; x < tmptypename.size(); x++) {
          // typenamevalues is one unit of information in row
          Vector typenamevalues = (Vector) tmptypename.get(x);
          // iterates all the dates, and if the date doesnt exist, puts blank
          // information vector
          if (((String) typenamevalues.get(0)).indexOf("DAY") != -1
              || (((String) typenamevalues.get(0)).indexOf("COUNT") != -1)
              || ((((String) typenamevalues.get(0)).indexOf("RANKBH") != -1)
                  && (((String) typenamevalues.get(0)).indexOf("WEEKRANKBH") == -1) && (((String) typenamevalues.get(0))
                  .indexOf("MONTHRANKBH") == -1))) {
            day.add(tmptypename);
          }// Only day aggregations are sort this way.
          /*
           * else if (((String)typenamevalues.get(0)).indexOf("WEEK") != -1){
           * week.add(tmptypename); }else if
           * (((String)typenamevalues.get(0)).indexOf("MONTH") != -1){
           * month.add(tmptypename); }
           */
          break;
        }
      }
      ctx.put("day", day);
    } else {
      ctx.put("day", new Vector());
    }
    // ctx.put("week", week);
    // ctx.put("month", month);

    /*
     * for (int i = 0; i < day.size(); i++){ log.debug(day.get(i) + " : " + i); }
     */
  }

  /**
   * Validate days so that there isnt any blank spots in typename days.
   * 
   * @param dates
   * @param typeNameVector
   * @return valid days
   */
  public Vector validateDates(List<String> dates, Vector typeNameVector) {
    Vector validated = new Vector();
    DateFormatter tmpDf = new DateFormatter("yyyy-MM-dd");// hh:mm:ss.mm");

    if (typeNameVector.get(0) instanceof String) {

      log.debug("typeName was String " + dates.size());

      Iterator it = dates.iterator();
      int ix = 0;
      while (it.hasNext()) {
        String date = (String) it.next();
        log.debug("    " + (ix++) + " " + date);
      }

      validated.add((String) typeNameVector.get(0));

      // Typenamevector is vector that holds all the rows
      // First typenamevector is filled with blanks if necessary
      for (int i = 1; i < typeNameVector.size(); i++) { // OK

        Vector tmptypename = (Vector) typeNameVector.get(i);

        log.debug("TypeName " + i + " size " + tmptypename.size());

        Vector validatedTypename = new Vector();

        int y = 0;
        for (int x = 0; x < tmptypename.size(); x++) {

          // typenamevalues is one unit of information in row
          Vector typenamevalues = (Vector) tmptypename.get(x);

          log.debug("TypeNameValues " + x + " size " + typenamevalues + " y=" + y + " (" + dates.size() + ")");

          // iterates all the dates, and if the date doesnt exist, puts blank
          // information vector
          tmpDf.setCalendar((String) typenamevalues.get(3));

          if (tmpDf.getFormattedDate(tmpDf.getTime()).equals((String) dates.get(y))) {
            validatedTypename.add(typenamevalues);
            y++;
          } else {
            Vector blank = new Vector();
            blank.add((String) typenamevalues.get(0));
            blank.add((String) typenamevalues.get(1));
            for (int z = 0; z < 6; z++) {
              blank.add("blank");
            }
            log.debug("not valid: " + tmpDf.getFormattedDate(tmpDf.getTime()));
            validatedTypename.add(blank);
            y++;
            x--;
          }

        } // for temptypename

        validated.add(validatedTypename);
      }

    } else {

      // Typenamevector is vector that holds all the rows
      // First typenamevector is filled with blanks if necessary
      for (int i = 0; i < typeNameVector.size(); i++) {

        Vector tmptypename = (Vector) typeNameVector.get(i);
        Vector validatedTypename = new Vector();
        int y = 0;
        for (int x = 0; x < tmptypename.size(); x++) {

          // typenamevalues is one unit of information in row
          Vector typenamevalues = (Vector) tmptypename.get(x);
          // iterates all the dates, and if the date doesnt exist, puts blank
          // information vector
          tmpDf.setCalendar((String) typenamevalues.get(3));
          if (tmpDf.getFormattedDate(tmpDf.getTime()).equals((String) dates.get(y))) {
            validatedTypename.add(typenamevalues);
            y++;
          } else {
            Vector blank = new Vector();
            blank.add((String) typenamevalues.get(0));
            blank.add((String) typenamevalues.get(1));
            for (int z = 0; z < 6; z++) {
              blank.add("blank");
            }
            // log.debug("not valid: " +
            // tmpDf.getFormattedDate(tmpDf.getTime()));
            validatedTypename.add(blank);
            y++;
            x--;
          }
        }
        validated.add(validatedTypename);
      }
    }

    return validated;
  }

  /**
   * Validate days so that there isnt any blank spots in typename days.
   * 
   * @param dates
   * @param typeNameVector
   * @return valid days
   */
  public Vector validateDatesBeta(Vector dates, Vector typeNameVector) {
    Vector validated = new Vector();
    DateFormatter tmpDf = new DateFormatter("yyyy-MM-dd hh:mm:ss.mm");

    validated.add((String) typeNameVector.get(0));

    // Typenamevector is vector that holds all the rows
    // First typenamevector is filled with blanks if necessary
    for (int i = 1; i < typeNameVector.size(); i++) {
      Vector tmptypename = (Vector) typeNameVector.get(i);
      Vector validatedTypename = new Vector();
      int y = 0;
      for (int x = 0; x < tmptypename.size(); x++) {
        // typenamevalues is one unit of information in row
        Vector typenamevalues = (Vector) tmptypename.get(x);
        // iterates all the dates, and if the date doesnt exist, puts blank
        // information vector
        if (((String) typenamevalues.get(0)).indexOf("WEEK") == -1) {
          tmpDf.setCalendar((String) typenamevalues.get(3));
          if (tmpDf.getFormattedDate(tmpDf.getTime()).equals((String) dates.get(y))) {
            validatedTypename.add(typenamevalues);
            y++;
          } else {
            Vector blank = new Vector();
            blank.add((String) typenamevalues.get(0));
            blank.add((String) typenamevalues.get(1));
            for (int z = 0; z < 6; z++) {
              blank.add("blank");
            }
            // log.debug("not valid: " +
            // tmpDf.getFormattedDate(tmpDf.getTime()));
            validatedTypename.add(blank);
            y++;
            x--;
          }
        } else {
          validatedTypename.add(typenamevalues);
        }
      }

      validated.add(validatedTypename);
    }

    return validated;
  }

  /**
   * @param dateString
   * @return sqlcommands
   */
  public static Vector makeSqlCommands(String dateString) {
    Vector sqlCommands = new Vector();

    sqlCommands.add("monitor_day(" + dateString + ")");
    sqlCommands.add("monitor_aggregation_day(" + dateString + ")");
    sqlCommands.add("monitor_aggregation_week(" + dateString + ")");
    sqlCommands.add("monitor_aggregation_month(" + dateString + ")");

    return sqlCommands;
  }

  public static void main(String[] args) {
    String date = "yyyy-MM-dd";

    DateFormatter df = new DateFormatter(date);
    df.setCalendar("2005-09-06");
    //Vector v = df.reverseTimeDay(14);

  }

}