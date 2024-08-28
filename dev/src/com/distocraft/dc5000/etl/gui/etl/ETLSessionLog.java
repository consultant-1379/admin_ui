package com.distocraft.dc5000.etl.gui.etl;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;

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
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.ENIQServiceStatusInfo;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.config.Configuration;
import com.distocraft.dc5000.etl.gui.config.ConfigurationFactory;
import com.distocraft.dc5000.etl.gui.config.ServiceNames;
import com.distocraft.dc5000.etl.gui.systemmonitor.MonitorInformation;
import com.distocraft.dc5000.etl.gui.util.FileBrowser;
import com.distocraft.dc5000.etl.gui.util.Helper;
import com.distocraft.dc5000.etl.gui.util.ParamValue;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved. <br>
 * Servlet is for browsing session logs.
 * 
 * @author Jani Vesterinen
 */
public class ETLSessionLog extends EtlguiServlet { // NOPMD by eheijun on 02/06/11 15:09

  private static final long serialVersionUID = 1L;

  private final Log log = LogFactory.getLog(this.getClass());
  
  private Boolean isSearch =false;

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  public final static BigInteger ONE_BILLION = new BigInteger("1000000000");

  @Override
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, final Context ctx) { // NOPMD by eheijun on 02/06/11 15:09
    Template outty = null;

    final RockFactory rockEtlRep = ((RockFactory) ctx.get("rockEtlRep"));
    final HttpSession session = request.getSession(false);

    // get the current date (is used at the UI, if none given)
    final DbCalendar calendar = new DbCalendar();

    String page = "sessionLog.vm";
    // String searchtype = request.getParameter(Helper.PARAM_SEARCH_STRING);
    // techpack
    final String techpack = StringEscapeUtils.escapeHtml( request.getParameter(Helper.PARAM_SELECTED_TECHPACK));
    String logger = request.getParameter(Helper.PARAM_SELECTED_TABLE);
    final String wfName = StringEscapeUtils.escapeHtml( request.getParameter("wfname"));

    String year_1 =  request.getParameter("year_1");
    String month_1 = request.getParameter("month_1");
    String day_1 =  request.getParameter("day_1");
    String year_2 =  request.getParameter("year_2");
    String month_2 = request.getParameter("month_2");
    String day_2 =  request.getParameter("day_2");
    String start_hour = request.getParameter("start_hour");
    String end_hour = request.getParameter("end_hour");
    final String source = StringEscapeUtils.escapeHtml( request.getParameter("source"));
    final String batch_id = StringEscapeUtils.escapeHtml( request.getParameter("batch_id"));
    final String session_id = StringEscapeUtils.escapeHtml( request.getParameter("session_id"));
    final String search = StringEscapeUtils.escapeHtml( request.getParameter("search"));

    final String a_status = StringEscapeUtils.escapeHtml( request.getParameter("a_status"));
    final String a_filename = StringEscapeUtils.escapeHtml( request.getParameter("a_filename"));
    
    
    String pattern =  "^[a-zA-Z0-9_]*$";
    
    if(logger == null){
    	logger = "-";
    }
    if(logger.matches(pattern)){
    	logger = StringEscapeUtils.escapeHtml(logger);
    }else{
    	logger = " ";
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

    
    if(year_2 == null){
    	year_2 = "-";
    }
    
    if(year_2.matches(pattern2)){
    	year_2 = StringEscapeUtils.escapeHtml(year_2);
    }else{
    	year_2 = null;
    }
    
    if(month_2 == null){
    	month_2 = "-";
    }
    
    
    if(month_2.matches(pattern2)){
    	month_2 = StringEscapeUtils.escapeHtml(month_2);
    }else{
    	month_2 = null;
    }
    
    if(day_2 == null){
    	day_2 = "-";
    }
    
    
    if(day_2.matches(pattern2)){
    	day_2 = StringEscapeUtils.escapeHtml(day_2);
    }else{
    	day_2 = null;
    }
    
    if(start_hour == null){
    	start_hour = "-";
    }
    
    
    if(start_hour.matches(pattern2)){
    	start_hour = StringEscapeUtils.escapeHtml(start_hour);
    }else{
    	start_hour = null;
    }
    
    if(end_hour == null){
    	end_hour = "-";
    }
    
    
    if(end_hour.matches(pattern2)){
    	end_hour = StringEscapeUtils.escapeHtml(end_hour);
    }else{
    	end_hour = null;
    }
    
    
    final ParamValue select = new ParamValue();
    final ParamValue loggerui = new ParamValue();

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

    if (start_hour != null) {
      session.setAttribute("start_hour", start_hour);
    } else if (session.getAttribute("start_hour") != null) {
      start_hour = session.getAttribute("start_hour").toString();
    } else {
      session.setAttribute("start_hour", "0");
      start_hour = "0";
    }

    if (year_2 != null) {
      session.setAttribute("year2", year_2);
    } else if (session.getAttribute("year2") != null) {
      year_2 = session.getAttribute("year2").toString();
    } else {
      session.setAttribute("year2", calendar.getYearString());
      year_2 = calendar.getYearString();
    }

    if (month_2 != null) {
      session.setAttribute("month2", month_2);
    } else if (session.getAttribute("month2") != null) {
      month_2 = session.getAttribute("month2").toString();
    } else {
      session.setAttribute("month2", calendar.getMonthString());
      month_2 = calendar.getMonthString();
    }

    if (day_2 != null) {
      session.setAttribute("day2", day_2);
    } else if (session.getAttribute("day2") != null) {
      day_2 = session.getAttribute("day2").toString();
    } else {
      session.setAttribute("day2", calendar.getDayString());
      day_2 = calendar.getDayString();
    }

    if (end_hour != null) {
      session.setAttribute("end_hour", end_hour);
    } else if (session.getAttribute("end_hour") != null) {
      end_hour = session.getAttribute("end_hour").toString();
    } else {
      session.setAttribute("end_hour", "23");
      end_hour = "23";
    }

    try {

      final Date date_1 = sdf.parse(year_1 + "-" + month_1 + "-" + day_1);
      final Date date_2 = sdf.parse(year_2 + "-" + month_2 + "-" + day_2);

      if (date_1.after(date_2)) {
        year_2 = year_1;
        month_2 = month_1;
        day_2 = day_1;
        session.setAttribute("year2", year_1);
        session.setAttribute("month2", month_1);
        session.setAttribute("day2", day_1);
      }

      if (search == null)
      {
    	  ctx.put("toomany", " ");
    	  ctx.put("Search", isSearch);
      }




      if (search != null) {
        select.setParam(Helper.PARAM_SELECTED_TECHPACK);
        select.setValue(techpack);
        select.setSelectedTag("selected");
        select.setSelected(true);

        loggerui.setParam(Helper.PARAM_SELECTED_TABLE);
        loggerui.setValue(logger);
        loggerui.setSelectedTag("selectedtable");
        loggerui.setSelected(true);

        // getInformation(ctx, year_1, month_1, day_1, logger, searchtype,
        // selectedtype);
        final int maxrows = Helper.getEnvEntryInt("maxSessionRows");
        if (logger != null && logger.equals("1")) {
          getInformation(ctx, year_1, month_1, day_1, logger, techpack, start_hour, end_hour, source, null, null, null,
              year_2, month_2, day_2, String.valueOf(maxrows));
        } else if (logger != null && logger.equals("0")) {
          getInformation(ctx, year_1, month_1, day_1, logger, techpack, start_hour, end_hour, null, source, a_status,
              a_filename, year_2, month_2, day_2, String.valueOf(maxrows));
        } else if (logger != null && logger.equals("2")) {
          getInformation(ctx, year_1, month_1, day_1, logger, techpack, start_hour, end_hour, null, null, null, null,
              year_2, month_2, day_2, String.valueOf(maxrows));
        } else if (logger != null && (logger.equals("3") || logger.equals("4"))) {
          getInformation(ctx, year_1, month_1, day_1, logger, techpack, start_hour, end_hour, null, source, a_status,
              a_filename, year_2, month_2, day_2, String.valueOf(maxrows));
        }

        // browseLogFolder(request, ctx);
      }

      // This sends a vector of valid years from DIM_DATE Table.
      // This is used by cal_select_1.vm
      try{
    	  final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh"); 
          final CalSelect calSelect = new CalSelect(rockDwh.getConnection());
          ctx.put("validYearRange", calSelect.getYearRange());
      }catch(final Exception e){
    	  ctx.put("errorSet", true);
		  if(ENIQServiceStatusInfo.isEtlDBOffline()){
			  ctx.put("errorText", " Failed to initialize connection to database: " + ENIQServiceStatusInfo.getEtlDBName());	
		  }else if(ENIQServiceStatusInfo.isRepDBOffline()){
			  ctx.put("errorText", " Failed to initialize connection to database: " + ENIQServiceStatusInfo.getRepDBName());
		  }else if(ENIQServiceStatusInfo.isDwhDBOffline()){
			  ctx.put("errorText", " Failed to initialize connection to database: " + ENIQServiceStatusInfo.getDwhDBName());
		  }else{
			  final String message = getErrorMessage(e);
			  log.error("Error message:" + message);
    		  log.error("Exception", e);
    		  ctx.put("errorText",message);
		  }		
    	  return getTemplate(EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU);
      }
      

      ctx.put("techpack", techpack);
      ctx.put("selectedpack", techpack == null ? "" : techpack);
      ctx.put("select", select);
      ctx.put("loggerui", loggerui);
      ctx.put("source", source == null ? "" : source);
      ctx.put("a_filename", a_filename == null ? "" : a_filename);
      ctx.put("a_status", a_status == null ? "" : a_status);
      
      final Configuration configuration = ConfigurationFactory.getConfiguration();
      ServiceNames servicenames = configuration.getServiceNames();
      Set<String> hosts = servicenames.getServices();

      if (hosts.contains(MonitorInformation.MZ_HOST_ID)) {
        ctx.put("mzInformation", Boolean.TRUE);
      }
      
      
      final ShowHistorySet set = new ShowHistorySet();

      try{
    	  ctx.put("techpacks", set.fetchHierarchy(rockEtlRep.getConnection()));
      }catch(Exception e){
    	  ctx.put("errorSet", true);
		  if(ENIQServiceStatusInfo.isEtlDBOffline()){
			  ctx.put("errorText", " Failed to initialize connection to database: " + ENIQServiceStatusInfo.getEtlDBName());	
		  }else if(ENIQServiceStatusInfo.isRepDBOffline()){
			  ctx.put("errorText", " Failed to initialize connection to database: " + ENIQServiceStatusInfo.getRepDBName());
		  }else if(ENIQServiceStatusInfo.isDwhDBOffline()){
			  ctx.put("errorText", " Failed to initialize connection to database: " + ENIQServiceStatusInfo.getDwhDBName());
		  }else{
			  final String message = getErrorMessage(e);
			  log.error("Error message:" + message);
    		  log.error("Exception", e);
    		  ctx.put("errorText",message);
		  }		
    	  return getTemplate(EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU);
      }
      
      final String logs = request.getParameter("logger");
      if (logs != null) {
        if (logs.equals("loader")) {
          page = "etl_little_logs.vm";
          ctx.put("logger", getEtlLoggerRowDetails(ctx, batch_id, session_id));
        } else if (logs.equals("adapter")) {
          page = "etl_little_logs_adapter.vm";
          ctx.put("logger", getEtlAdapterRowDetails(ctx, batch_id, session_id));
        } else if (logs.equals("mz")) {
          page = "etl_little_logs_mz.vm";
          log.debug(logger);
          ctx.put("logger", getMZLoggerRowDetails(ctx, batch_id, session_id, logger, wfName));
        }
      }
      outty = getTemplate(page);
    } catch (ResourceNotFoundException e) {
      log.error("ResourceNotFoundException", e);
    } catch (ParseErrorException e) {
      log.error("ParseErrorException", e);
    } catch (Exception e) {
      log.error("Exception", e);
    }

    ctx.put("year_1", year_1);
    ctx.put("month_1", month_1);
    ctx.put("day_1", day_1);
    ctx.put("year_2", year_2);
    ctx.put("month_2", month_2);
    ctx.put("day_2", day_2);
    ctx.put("start_hour", start_hour);
    ctx.put("end_hour", end_hour);

    return outty;

  }

  private List<List<String>> getEtlAdapterRowDetails(final Context ctx, final String batch_id, final String session_id) {
	  final String sql = "select * from log_session_loader where session_id= ? and batch_id= ?";
		log.debug(sql);
		// create connection to PM DATA
		final RockFactory rockDwh = ((RockFactory) ctx.get("rockDwh"));

		final List<List<String>> retval = new Vector<List<String>>();
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = rockDwh.getConnection().prepareStatement(sql);
			statement.setString(1, session_id);
			statement.setString(2, batch_id);
			result = statement.executeQuery();
      //
      // result
      while (result.next()) {
        final List<String> detail = new Vector<String>(); // NOPMD by eheijun on 02/06/11 15:09
        detail.add(result.getString("TypeName"));
        detail.add(result.getString("DataTime"));
        detail.add(result.getString("TimeLevel"));
        detail.add(result.getString("RowCount"));
        detail.add(result.getString("SessionStartTime"));
        detail.add(result.getString("SessionEndTime"));
        detail.add(result.getString("Source"));
        detail.add(result.getString("Status"));
        detail.add(result.getString("batch_id"));
        detail.add(result.getString("session_id"));
        retval.add(detail);
      }

    } catch (SQLException e) {
      log.error("When executing statement: '" + sql + "'");
      log.error("SQLException: ", e);
    } finally {
      try {
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
      } catch (Exception e) {
        log.error("Exception: ", e);
      }
    }
    return retval;

  }

  private List<List<String>> getMZLoggerRowDetails(final Context ctx, final String batch_id, final String session_id, String loggerx,
      final String wfName) {

    log.debug(loggerx);
    if (loggerx.equals("3")) {
      loggerx = "PROCESSING";
    } else if (loggerx.equals("4")) {
      loggerx = "PREPROCESSING";
    }
    final String selCols = "SESSION_ID,SOURCE,BATCH_ID,DATE_ID,FILENAME,SESSIONSTARTTIME "
			+ ",SESSIONENDTIME,TYPENAME,ROP_STARTTIME,NUM_OF_ROWS,WORKFLOW_TYPE "
			+ ",WORKFLOW_NAME,NUM_OF_CORRUPTED_ROWS,ROP_ENDTIME ";

	final String sql = "select " + selCols
			+ "  from log_session_adapter where session_id= ? and batch_id= ? and WORKFLOW_TYPE = ? and WORKFLOW_NAME LIKE ?";
	log.debug(sql);
	// create connection to PM DATA
	final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh");
	final List<List<String>> retval = new Vector<List<String>>();
	PreparedStatement statement = null;
	ResultSet result = null;
	try {
		statement = rockDwh.getConnection().prepareStatement(sql);
		statement.setString(1, session_id);
		statement.setString(2, batch_id);
		statement.setString(3, loggerx);
		statement.setString(4, "%" + wfName + "%");
		result = statement.executeQuery();

      // ................................................... parse search
      // result
      while (result.next()) {
        final List<String> detail = new Vector<String>(); // NOPMD by eheijun on 02/06/11 15:09
        detail.add(result.getString("SESSION_ID"));
        detail.add(result.getString("SOURCE"));
        detail.add(result.getString("BATCH_ID"));
        detail.add(result.getString("DATE_ID"));
        detail.add(result.getString("FILENAME"));
        detail.add(result.getString("SESSIONSTARTTIME"));
        detail.add(result.getString("SESSIONENDTIME"));
        detail.add(result.getString("TYPENAME"));
        detail.add(result.getString("ROP_STARTTIME"));
        detail.add(result.getString("NUM_OF_ROWS"));
        detail.add(result.getString("WORKFLOW_TYPE"));
        detail.add(result.getString("WORKFLOW_NAME"));
        detail.add(result.getString("NUM_OF_CORRUPTED_ROWS"));
        detail.add(result.getString("ROP_ENDTIME"));
        retval.add(detail);
      }

    } catch (SQLException e) {
      log.info("SQLException: " + e);
      log.info("When executing statement: '" + sql + "'");
    } finally {
      try {
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
      } catch (Exception e) {
        log.error("Exception: ", e);
      }
    }
    return retval;
  }

  private List<List<String>> getEtlLoggerRowDetails(final Context ctx, final String batch_id, final String session_id) {

	  final String sql = "select distinct FileName, SessionEndTime, SessionStartTime, Source, Status from log_session_adapter where session_id= ? and batch_id= ?";
		log.debug(sql);
//create connection to PM DATA
final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh");
final List<List<String>> retval = new Vector<List<String>>();
PreparedStatement statement = null;
ResultSet result = null;
try {
	statement = rockDwh.getConnection().prepareStatement(sql);
	statement.setString(1, session_id);
	statement.setString(2, batch_id);
	result = statement.executeQuery();

      // ................................................... parse search
      // result
      while (result.next()) {
        final List<String> detail = new Vector<String>(); // NOPMD by eheijun on 02/06/11 15:09
        detail.add(result.getString("FileName"));
        detail.add(result.getString("SessionEndTime"));
        detail.add(result.getString("SessionStartTime"));
        detail.add(result.getString("Source"));
        detail.add(result.getString("Status"));
        retval.add(detail);
      }

    } catch (SQLException e) {
      log.info("SQLException: " + e);
      log.info("When executing statement: '" + sql + "'");
    } finally {
      try {
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
      } catch (Exception e) {
        log.error("Exception: ", e);
      }
    }
    return retval;
  }

  private static long getTimeNoMillis(final Timestamp t) {
    return t.getTime() - (t.getNanos() / 1000000);
  }

  /**
   * @param ctx
   * @param year_1
   * @param month_1
   * @param day_1
   * @param logger
   * @param tpack
   * @param starthour
   * @param endhour
   * @param source
   * @param a_source
   * @param a_status
   * @param a_filename
   * @param day_2
   * @param month_2
   * @param year_2
   */

  private void getInformation(final Context ctx, final String year_1, final String month_1, final String day_1, final String logger, final String tpack, // NOPMD by eheijun on 02/06/11 15:09
      final String starthour, final String endhour, final String source, final String a_source, final String a_status, final String a_filename,
      final String year_2, final String month_2, final String day_2, final String maxrows) {

    // tpack parameter is not used at the moment.

    final String dataTime = year_1 + "-" + month_1 + "-" + day_1;
    final String dataTime2 = year_2 + "-" + month_2 + "-" + day_2;
    ctx.put("datatime", dataTime);
    String table = "";
    String tpackfilter = "";

    String stime = "";
    if (logger.equals("0")) {
        table = "LOG_SESSION_ADAPTER";
        tpackfilter = " and filename LIKE ? and source LIKE ? and status=?";
        stime = "(datetime(sessionendtime) between datetime(?) and datetime(?))";
       // select * from LOG_SESSION_LOADER where datetime(sessionendtime) between
        // datetime('2005-11-16 00:00:00') and datetime('2005-11-16 01:00:00')
     
      } else if (logger.equals("1")) {
        table = "LOG_SESSION_LOADER";
      tpackfilter = " and typename LIKE ? and source LIKE ?";
      stime = "(datetime(sessionendtime) between datetime(?) and datetime(?))";
      } else if (logger.equals("2")) {
        table = "LOG_SESSION_AGGREGATOR";
        tpackfilter = " and typename LIKE ?";
        stime = "(datetime(sessionendtime) between datetime(?) and datetime(?))";	
      } else if (logger.equals("3")) {
        table = "LOG_SESSION_ADAPTER";
        tpackfilter = " and WORKFLOW_TYPE = ?";
   
        if (tpack != null) {
        	tpackfilter += " and typename LIKE ?";
          log.debug("3-tpack=" + tpack);
        }
        if (a_filename != null) {
        	tpackfilter += " and filename LIKE ?";
          log.debug("3-a_filename=" + a_filename);
        }
        if (a_source != null) {
        	tpackfilter += "and source LIKE ?";
          log.debug("3-a_source=" + a_source);
        }
        if (a_status != null) {
        	tpackfilter += " and status= ? ";
          log.debug("3-a_status=" + a_status);
        }
         stime = "(datetime(sessionendtime) between datetime(?) and datetime(?))";
        log.debug("3-stime=" + stime);
      } else if (logger.equals("4")) {
        table = "LOG_SESSION_ADAPTER";
       tpackfilter = " and WORKFLOW_TYPE = ?";

        if (tpack != null) {
        	tpackfilter += " and typename LIKE ?";
          log.debug("4-tpack=" + tpack);
        }
        if (a_filename != null) {
          tpackfilter += " and filename LIKE ?";
          log.debug("4-a_filename=" + a_filename);
        }
        if (a_source != null) {
        tpackfilter += "and source LIKE ?";
        log.debug("4-a_source=" + a_source);
        }
        if (a_status != null) {
        	tpackfilter += " and status= ? ";
          log.debug("4-a_status=" + a_status);
        }
         stime = "(datetime(sessionendtime) between datetime(?) and datetime(?))";
        log.debug("4-stime=" + stime);
      }

      String sqlStr = "select top ? * from " + table + " where " + stime + tpackfilter;
  	
      // Changes for Counter Volume Visualisation IP.
      if (table.equalsIgnoreCase("LOG_SESSION_ADAPTER")) {
        final String selectColumns = " FileName, SessionStartTime, SessionEndTime, Source, Status, batch_id, session_id ";
        sqlStr = "select distinct top ?" + selectColumns + "from " + table + " where " + stime + tpackfilter;
      }

      if ((logger.equals("3") || logger.equals("4")) && table.equalsIgnoreCase("LOG_SESSION_ADAPTER")) {
        final String selectColumns = " FileName, CONVERT(VARCHAR(23), SessionStartTime, 121), CONVERT(VARCHAR(23), SessionEndTime, 121), Source, Status, batch_id, session_id,workflow_name ";
        sqlStr = "select distinct top ?" + selectColumns + "from " + table + " where " + stime + tpackfilter;
      }
       log.debug("logger SQL -->" + sqlStr);
      // create connection to PM DATA
       final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh");
	  	PreparedStatement statement = null;
	  	ResultSet result = null;
      java.sql.Timestamp timestampEnd = null;
      java.sql.Timestamp timestampStart = null;

      // DecimalFormat df = new DecimalFormat("#.000");

      long firstTime = 0l;
      long secondTime = 0l;
      Long diffTime = 0l;
String datatimestart=dataTime + " " + starthour + ":00:00";
String datatimeend=dataTime2 + " " + endhour + ":00:00";
String typename= "%" + tpack + "%";
String source1= "%" + source + "%";
String WORKFLOW_TYPE = " Helper.MZ_PROCESSING ";
String filename= "%" + a_filename + "%";
String status=  a_status ;
String source2= "%" + a_source + "%";

   	try {
  		if (logger.equals("0")) {
	        table = "LOG_SESSION_ADAPTER";
	        statement = rockDwh.getConnection().prepareStatement(sqlStr);
	  		statement.setInt(1, Integer.parseInt(maxrows));
  		  statement.setString(2, datatimestart);
	  	statement.setString(3,datatimeend); 
		statement.setString(4, filename);
	  	statement.setString(5, source2);
		statement.setString(6, status);
  		} else if (logger.equals("1")) {
	        table = "LOG_SESSION_LOADER";
	        statement = rockDwh.getConnection().prepareStatement(sqlStr);
	  		statement.setInt(1, Integer.parseInt(maxrows));
	        statement.setString(2, datatimestart);
		  	statement.setString(3,datatimeend); 
			statement.setString(4, typename);
		  	statement.setString(5, source1);		  	
		  	
  		 } else if (logger.equals("2")) {
 	        table = "LOG_SESSION_AGGREGATOR";
 	        statement = rockDwh.getConnection().prepareStatement(sqlStr);
	  		statement.setInt(1, Integer.parseInt(maxrows));
 	        statement.setString(2, datatimestart);
		  	statement.setString(3,datatimeend); 
			statement.setString(4, typename);
  		 } else if (logger.equals("3")) {
 	        table = "LOG_SESSION_ADAPTER";
 	  	    statement = rockDwh.getConnection().prepareStatement(sqlStr);
	  		statement.setInt(1, Integer.parseInt(maxrows));
 	        statement.setString(2, datatimestart);
		  	statement.setString(3,datatimeend); 
			statement.setString(4, WORKFLOW_TYPE);
			if (tpack != null) {
				 statement = rockDwh.getConnection().prepareStatement(sqlStr);
			  		statement.setInt(1, Integer.parseInt(maxrows));
		 	       statement.setString(2, typename);		
		        		        }
		        if (a_filename != null) {
		        	 statement = rockDwh.getConnection().prepareStatement(sqlStr);
				  		statement.setInt(1, Integer.parseInt(maxrows));
			 	       statement.setString(2, filename);
			        		        }
		        if (a_source != null) {
		        	 statement = rockDwh.getConnection().prepareStatement(sqlStr);
				  		statement.setInt(1, Integer.parseInt(maxrows));
			 	       statement.setString(2, source2);
			       
		        }
		        if (a_status != null) {
		        	 statement = rockDwh.getConnection().prepareStatement(sqlStr);
				  		statement.setInt(1, Integer.parseInt(maxrows));
			 	       statement.setString(2, status);
		         
		        }
  		 }else if (logger.equals("4")) {
  			System.out.println("logger.equals( 4 -PS:"+sqlStr);
  	        table = "LOG_SESSION_ADAPTER";
  	      statement = rockDwh.getConnection().prepareStatement(sqlStr);
	  		statement.setInt(1, Integer.parseInt(maxrows));
	       statement.setString(2, WORKFLOW_TYPE);

  	        if (tpack != null) {
  	        	 statement = rockDwh.getConnection().prepareStatement(sqlStr);
			  		statement.setInt(1, Integer.parseInt(maxrows));
		 	       statement.setString(2, typename);
  	        }
  	        if (a_filename != null) {
  	        	 statement = rockDwh.getConnection().prepareStatement(sqlStr);
			  		statement.setInt(1, Integer.parseInt(maxrows));
		 	       statement.setString(2, filename);
  		       
  	        }
  	        if (a_source != null) {
  	        	 statement = rockDwh.getConnection().prepareStatement(sqlStr);
			  		statement.setInt(1, Integer.parseInt(maxrows));
		 	       statement.setString(2, source2);
  	        
  	        }
  	        if (a_status != null) {
  	        	 statement = rockDwh.getConnection().prepareStatement(sqlStr);
			  		statement.setInt(1, Integer.parseInt(maxrows));
		 	       statement.setString(2, status);
  	        }    
  	        }
  		result = statement.executeQuery();
  		result = statement.executeQuery();
        final Vector<List<Object>> v = new Vector<List<Object>>();
        // ................................................... parse search
        // result
        int resultCount = 0;
      // ................................................... parse search
      // result
      while (result.next()) {
    	  resultCount++;
        final List<Object> detail = new Vector<Object>(); // NOPMD by eheijun on 02/06/11 15:09
        if (table.equals("LOG_SESSION_ADAPTER")) {
          /*
           * FileName SessionStartTime SessionEndTime Source Status
           */
          detail.add(result.getString("FileName"));
          detail.add(result.getString("SessionStartTime"));
          detail.add(result.getString("SessionEndTime"));
          detail.add(result.getString("Source"));
          detail.add(result.getString("Status"));
          detail.add(result.getString("batch_id"));
          detail.add(result.getString("session_id"));
          if (logger.equals("3") || logger.equals("4")) {
        		System.out.println("logger.equals 3 and 4");
            try {
              timestampEnd = result.getTimestamp("SessionEndTime");
              timestampStart = result.getTimestamp("SessionStartTime");
              firstTime = (getTimeNoMillis(timestampStart) * 1000000) + timestampStart.getNanos();
              secondTime = (getTimeNoMillis(timestampEnd) * 1000000) + timestampEnd.getNanos();
              diffTime = Math.abs(secondTime - firstTime); // diff is in nanos
              diffTime = Math.abs(diffTime / 1000000); // diff is in millis
              // dblSecs = Double.valueOf(diffTime/1000); // diff is in secs

            } catch (Exception e) {
              log.error("Exception: ", e);
            }
            detail.add(result.getString("workflow_name"));
            detail.add(diffTime);
          }
          /*
           * Filename - Name of source file SessionStartTime - Start time of adapter session SessionEndTime - End time
           * of adapter session Interface (Source in database) - Name of interface performed adaptation Status - Status
           * of adapter session. OK or ERROR
           */

        } else if (table.equals("LOG_SESSION_AGGREGATOR") || table.equals("LOG_SESSION_LOADER")) {
          /*
           * Typename Datatime Timelevel Rowcount SessionStartTime SessionEndTime Source Status
           */
          detail.add(result.getString("TypeName"));
          detail.add(result.getString("DataTime"));
          detail.add(result.getString("TimeLevel"));
          detail.add(result.getString("RowCount"));
          detail.add(result.getString("SessionStartTime"));
          detail.add(result.getString("SessionEndTime"));
          detail.add(result.getString("Source"));
          detail.add(result.getString("Status"));
          detail.add(result.getString("batch_id"));
          detail.add(result.getString("session_id"));
        }
        v.add(detail);
      }
       isSearch = true;
      ctx.put("results", v);
      ctx.put("Search", isSearch);
      ctx.put("vsize", resultCount);
      ctx.put("toomany",resultCount >= Integer.parseInt(maxrows) ? maxrows : " ");
    } catch (SQLException e) {
      log.info("SQLException: " + e);
      log.info("When executing statement: '" + sqlStr + "'");
    } finally {
      try {
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
      } catch (Exception e) {
        log.error("Exception: ", e);
      }
    }
  }
  /**
   * Browses through log folder.
   * 
   * @param request
   * @param ctx
   */
  public void browseLogFolder(final HttpServletRequest request, final Context ctx) {
    // Map ETLSets = new HashMap(); // contains the sets (in the end)
    // String page = "etlDetailShow.vm"; // show this page if nothing else is
    // stated
    // FileBrowser browser = new FileBrowser(Helper.getEnvEntryString("logfile"));
    final FileBrowser browser = new FileBrowser(System.getProperty("LOG_DIR") + "/engine");

    final List<EtlDetailValue> list = browser.parseFolder(request.getParameterMap());
    final String pageFrom = request.getParameter(Helper.PARAM_PAGE_FROM);
    // The page title is decided.
    final String pageHeader = (pageFrom != null && pageFrom.equals(Helper.PARAM_PAGE_HISTORY)) ? Helper.TITLE_PAGE_HISTORY
        : Helper.TITLE_PAGE_MONITORING;
    ctx.put("listofvalues", list);
    ctx.put("pageTitle", pageHeader);
    if (list.size() == 0) {
      ctx.put("listnull", "0");
    } else {
      ctx.put("listnull", "1");
    }
  }
  
  private static String getErrorMessage(final Throwable tr) {
	    String msg = tr.getMessage();
	    if (msg != null && msg.contains("JZ00L")) {
	      msg = "Max connections to etlrep database reached.";
	    } else {
	      msg = "Error: " + tr.getLocalizedMessage();
	    }
	    return msg;
	  }

}