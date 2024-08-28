package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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

import com.distocraft.dc5000.etl.gui.common.CommandRunner;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.DwhMonitoring;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.util.Helper;
import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.ericsson.eniq.common.RemoteExecutor;
import com.ericsson.eniq.repository.DBUsersGet;
import com.jcraft.jsch.JSchException;
import com.distocraft.dc5000.etl.gui.common.CalSelect;

public class EniqMonitoring extends EtlguiServlet {
	 private static final long serialVersionUID = 1L;

	  private static final Map<String, String> commands = new TreeMap<String, String>();

	  private static boolean initDone = false;

	  private final Log log = LogFactory.getLog(this.getClass());
	  
	  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	  /*
	   * (non-Javadoc)
	   * 
	   * @see org.apache.velocity.servlet.VelocityServlet#handleRequest(javax.servlet.http.HttpServletRequest,
	   *      javax.servlet.http.HttpServletResponse,
	   *      org.apache.velocity.context.Context)
	   */
	  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, final Context ctx) throws  ParseException, SQLException {
	    Template outty = null;
	    DbCalendar calendar = new DbCalendar();
	    
	    // tablename
	    String startYearStr = request.getParameter("year_1"); // from date
	    String startMonthStr = request.getParameter("month_1");
	    String startDayStr = request.getParameter("day_1");
	    String endYearStr = request.getParameter("year_2"); // to date
	    String endMonthStr = request.getParameter("month_2");
	    String endDayStr = request.getParameter("day_2");
	    String searchDone = request.getParameter("search_done");
	    String rls = request.getParameter("row_limit"); // row limiting count
	    String runCommand = request.getParameter("command");
	    String pattern =  "^[a-zA-Z0-9_]*$";
	    String search = "yes";
	    search=	request.getParameter("timelevel_changed");
	    String search_direction = "day";
	    search_direction =	request.getParameter("search_direction");
	    log.info("search:"+search);
	    log.info("search_direction:"+search_direction);
	    	
	    if(search == null){
	    	search = "yes";
	    }
	    
	    if(search_direction == null){
	    	search_direction = "range";
	    }
	    
	    if(runCommand == null){
	    	runCommand = "-";
	    }
	    if(runCommand.matches(pattern)){
	    	runCommand = StringEscapeUtils.escapeHtml(runCommand);
	    }else{
	    	runCommand = "-";
	    }
	    
	 String pattern2 = "^[0-9]*$";
	    
	    if(startYearStr == null || startYearStr.equals("")){
	    	startYearStr = "-";
	    }
	    
	    if(startYearStr.matches(pattern2)){
	    	startYearStr = StringEscapeUtils.escapeHtml(startYearStr);
	    }else{
	    	startYearStr = null;
	    }
	    
	    if(startMonthStr == null || startMonthStr.equals("")){
	    	startMonthStr = "-";
	    }
	    
	    
	    if(startMonthStr.matches(pattern2)){
	    	startMonthStr = StringEscapeUtils.escapeHtml(startMonthStr);
	    }else{
	    	startMonthStr = null;
	    }
	    
	    if(startDayStr == null || startDayStr.equals("")){
	    	startDayStr = "-";
	    }
	    
	    
	    if(startDayStr.matches(pattern2)){
	    	startDayStr = StringEscapeUtils.escapeHtml(startDayStr);
	    }else{
	    	startDayStr = null;
	    }
	    
	    if(endYearStr == null || endYearStr.equals("")){
	    	endYearStr = "-";
	    }
	    
	    if(endYearStr.matches(pattern2)){
	    	endYearStr = StringEscapeUtils.escapeHtml(endYearStr);
	    }else{
	    	endYearStr = null;
	    }
	    
	    if(endMonthStr == null || endMonthStr.equals("")){
	    	endMonthStr = "-";
	    }
	    
	    
	    if(endMonthStr.matches(pattern2)){
	    	endMonthStr = StringEscapeUtils.escapeHtml(endMonthStr);
	    }else{
	    	endMonthStr = null;
	    }
	    
	    if(endDayStr == null || endDayStr.equals("")){
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

	    Connection connDwh = null;
	    if(connDwh==null){
	    	  connDwh=((RockFactory)ctx.get("rockDwh")).getConnection();     
	      }
	    
	    if (null == rls || rls.equals("")) {
	        rls = "5";
	      }
	    
	    CalSelect calSelect = new CalSelect(connDwh);
	    ctx.put("validYearRange", calSelect.getYearRange());
	    // get users input
	    
	    String getDate = request.getParameter("date");
	    String result = null;
	    String startStamp = null;	    
	    if(runCommand == null){
	    	runCommand = "-";
	    }
	    if(runCommand.matches(pattern)){
	    	runCommand = StringEscapeUtils.escapeHtml(runCommand);
	    }else{
	    	runCommand = null;
	    }

	    if(getDate == null){
	    	long dateValue = System.currentTimeMillis();
	    	getDate = sdf.format(new Date(dateValue));
	    }
	    HttpSession session = request.getSession(false);
	    
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
	      	
	      
	      log.info("DateTime"+startYearStr + "-" + startMonthStr + "-" + startDayStr);
	      log.info("search_direction"+search_direction);
	    // see if we recon the command what user requested
	    // if so run the command otherwise don't
	     if(!rls.equals("0")){
	    if (runCommand == null) {
	      runCommand = "-";
	    } else {
	    	String cmdLine= (String) commands.get(runCommand);
	    	if (cmdLine == null) {
	    		cmdLine = "SchedulerHeap";
	    	} else if (cmdLine.equalsIgnoreCase("SchedulerHeap")) {
	    		 if(search_direction.equalsIgnoreCase("day")){
	    			 String sql;
	 	    		Statement statement = null;
	 	    	    ResultSet result1 = null;
	 	    	    Map<String, Integer> map = getMap();
	 	    	    log.info("Selected Date is:"+getDate);
	 	    	    sql = "select ROP,SCHEDULER_HEAP_USAGE from DC.Monitor_Heap where DATETIMEID = '"+startYearStr + "-" + startMonthStr + "-" + startDayStr+"'";
	 	    	    try    {
	 	    	      statement = connDwh.createStatement();
	 	    	      log.info("Query Executed");
	 	    	      result1 = statement.executeQuery(sql);  
	 	    	      log.info("Result set");
	 	    	      if(result1 != null){
	 	    	    	  log.info("ResultSet not null"+result1.getFetchSize());
	 	    	      while (result1.next()) {  
	 	    	        map.put(result1.getString("ROP").trim(), result1.getInt("SCHEDULER_HEAP_USAGE"));
	 	    	      }
	 	    	      }
	 	 	    		ctx.put("keys",new Vector<String>(map.keySet()));
	 	 	    		ctx.put("heap",map);
	 	 	    	    ctx.put("command", "SchedulerHeap");
	 	 	    	    ctx.put("max", getValue("SchedulerHeap"));
	 	    	    }catch (SQLException e) {
	 	    	    	ctx.put("keys",new Vector<String>(map.keySet()));
	 	 	    		ctx.put("heap",map);
	 	 	    	    ctx.put("command", "SchedulerHeap");
	 	 	    	    ctx.put("max", getValue("SchedulerHeap"));
	 					e.printStackTrace();
	 				}finally{
	 					statement.close();
	 	    	    	connDwh.close();
	 	    	    	result1.close();
	 	    	    }
	    		 }else{
	    	     String sql;
	    	  	 Statement statement = null;
	    	     ResultSet result1 = null;
	    	    // Map<String, Integer> map = getMap();
	    	     Map<String, Integer> map = new TreeMap<String, Integer>();
	    	     String endDate = endYearStr + "-" + endMonthStr + "-" + endDayStr;
	    		 String startDate=startYearStr + "-" + startMonthStr + "-" + startDayStr;
	    			log.info("Selected Start date is:"+startDate);
	    			log.info("Selected End date is:"+endDate);
	    		    Date d1 = null;
	    			Date d2 = null;
	    			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	    		    d1 = format.parse(startDate);
	    			d2 = format.parse(endDate);
	    			long diff = d2.getTime() - d1.getTime();
	    			long diffDays = diff / (24 * 60 * 60 * 1000);
	    			//System.out.println(diffDays);
	    			if(diffDays <= 0){
	    				diffDays = 1;
	    			}
	    			log.info("Difference in days"+diffDays);
	    			log.info("rows:"+rls);
	    			for(int i =0;i<=diffDays;i++){
	    			//	System.out.println(t.getNextDate(startDate));
	    				 sql = "select top "+rls +" * from monitor_heap where datetimeid = '"+startDate+"' order by scheduler_heap_usage desc ";
	    				 try    {
	    		    	    	statement = connDwh.createStatement();
	    		    	    	result1 = statement.executeQuery(sql);  
	    		    	      if(result1 != null){
	    		    	    	while (result1.next()) {  
	    		    	    		map.put(startDate+" "+result1.getString("ROP").trim(), result1.getInt("SCHEDULER_HEAP_USAGE"));
	    		    	    	}
	    		    	      }
	    		    	      	}
	    		    	    catch (SQLException e) {
	    						e.printStackTrace();
	    					}finally{
	    						statement.close();
	    		    	    	//connDwh.close();
	    		    	    	result1.close();
	    		    	    }
	    				 startDate=getNextDate(startDate);
	    			}
	    	     log.info("map contains"+map);
	    	      		ctx.put("keys",new Vector<String>(map.keySet()));
	    	      		ctx.put("heap",map);
	    	      		ctx.put("command", "SchedulerHeap");
	    	      		ctx.put("max", getValue("SchedulerHeap"));
	    		 }
	    	}else if (cmdLine.equalsIgnoreCase("EngineHeap")) {
	    		if(search_direction.equalsIgnoreCase("day")){
	    		String sql;
	    		Statement statement = null;
	    	    ResultSet result1 = null;
	    	    Map<String, Integer> map = getMap();
	    	    log.info("Selected Date is:"+getDate);
	    	    sql = "select ROP,ENGINE_HEAP_USAGE from DC.Monitor_Heap where DATETIMEID = '"+startYearStr + "-" + startMonthStr + "-" + startDayStr+"'";
	    	    try    {
	    	      statement = connDwh.createStatement();
	    	      log.info("Query Executed");
	    	      result1 = statement.executeQuery(sql);  
	    	      log.info("Result set");
	    	      if(result1 != null){
	    	    	  log.info("ResultSet not null"+result1.getFetchSize());
	    	      while (result1.next()) {  
	    	        map.put(result1.getString("ROP").trim(), result1.getInt("ENGINE_HEAP_USAGE"));
	    	      }
	    	      }
	 	    		ctx.put("keys",new Vector<String>(map.keySet()));
	 	    		ctx.put("heap",map);
	 	    	    ctx.put("command", "EngineHeap");
	 	    	    ctx.put("max", getValue("EngineHeap"));
	    	      
	    	    }catch (SQLException e) {
	    	    	ctx.put("keys",new Vector<String>(map.keySet()));
	 	    		ctx.put("heap",map);
	 	    	    ctx.put("command", "EngineHeap");
	 	    	   ctx.put("max", getValue("EngineHeap"));
					e.printStackTrace();
				}finally{
					statement.close();
	    	    	connDwh.close();
	    	    	result1.close();
	    	    }
	    		}else{
		    		
		    	     String sql;
		    	  	 Statement statement = null;
		    	     ResultSet result1 = null;
		    	    // Map<String, Integer> map = getMap();
		    	     Map<String, Integer> map = new TreeMap<String, Integer>();
		    	     String endDate = endYearStr + "-" + endMonthStr + "-" + endDayStr;
		    		 String startDate=startYearStr + "-" + startMonthStr + "-" + startDayStr;
		    			log.info("Selected Start date is:"+startDate);
		    			log.info("Selected End date is:"+endDate);
		    		    Date d1 = null;
		    			Date d2 = null;
		    			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		    		    d1 = format.parse(startDate);
		    			d2 = format.parse(endDate);
		    			long diff = d2.getTime() - d1.getTime();
		    			long diffDays = diff / (24 * 60 * 60 * 1000);
		    			//System.out.println(diffDays);
		    			if(diffDays <= 0){
		    				diffDays = 1;
		    			}
		    			log.info("Difference in days"+diffDays);
		    			log.info("rows:"+rls);
		    			for(int i =0;i<=diffDays;i++){
		    			//	System.out.println(t.getNextDate(startDate));
		    				 sql = "select top "+rls +" * from monitor_heap where datetimeid = '"+startDate+"' order by engine_heap_usage desc ";
		    				 try    {
		    		    	    	statement = connDwh.createStatement();
		    		    	    	result1 = statement.executeQuery(sql);  
		    		    	      if(result1 != null){
		    		    	    	while (result1.next()) {  
		    		    	    		map.put(startDate+" "+result1.getString("ROP").trim(), result1.getInt("ENGINE_HEAP_USAGE"));
		    		    	    	}
		    		    	      }
		    		    	      	}
		    		    	    catch (SQLException e) {
		    						e.printStackTrace();
		    					}finally{
		    						statement.close();
		    		    	    	//connDwh.close();
		    		    	    	result1.close();
		    		    	    }
		    				 startDate=getNextDate(startDate);
		    			}
		    	     log.info("map contains"+map);
		    	      		ctx.put("keys",new Vector<String>(map.keySet()));
		    	      		ctx.put("heap",map);
		    	      		ctx.put("command", "EngineHeap");
		    	      	    ctx.put("max", getValue("EngineHeap"));
		    		 }
	    		
	    	}else if (cmdLine.equalsIgnoreCase("MainCache")) {
	    		if(search_direction.equalsIgnoreCase("day")){
	    		String sql;
	    		Statement statement = null;
	    	    ResultSet result1 = null;
	    	    Map<String, Integer> map = getMap();
	    	    log.info("Date is:"+ getDate);
	    	    sql = "select ROP,MAIN_BUFFER_USAGE from DC.Monitor_db where DATETIMEID = '"+startYearStr + "-" + startMonthStr + "-" + startDayStr+"'";
	    	    try    {
	    	      statement = connDwh.createStatement();
	    	      result1 = statement.executeQuery(sql);  
	    	      if(result1 != null){
	    	      while (result1.next()) {  
	    	        map.put(result1.getString("ROP").trim(), result1.getInt("MAIN_BUFFER_USAGE"));
	    	      }
	    	      }
	 	    		ctx.put("keys",new Vector<String>(map.keySet()));
	 	    		ctx.put("heap",map);
	 	    	    ctx.put("command", "MainCache");
	    	      
	    	    }catch (Exception e) {
	    	    	ctx.put("keys",new Vector<String>(map.keySet()));
	 	    		ctx.put("heap",map);
	 	    	    ctx.put("command", "MainCache");
					e.printStackTrace();
				}finally{
					statement.close();
	    	    	connDwh.close();
	    	    	result1.close();
	    	    }
	    		}else{
		    		
		    	     String sql;
		    	  	 Statement statement = null;
		    	     ResultSet result1 = null;
		    	    // Map<String, Integer> map = getMap();
		    	     Map<String, Integer> map = new TreeMap<String, Integer>();
		    	     String endDate = endYearStr + "-" + endMonthStr + "-" + endDayStr;
		    		 String startDate=startYearStr + "-" + startMonthStr + "-" + startDayStr;
		    			log.info("Selected Start date is:"+startDate);
		    			log.info("Selected End date is:"+endDate);
		    		    Date d1 = null;
		    			Date d2 = null;
		    			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		    		    d1 = format.parse(startDate);
		    			d2 = format.parse(endDate);
		    			long diff = d2.getTime() - d1.getTime();
		    			long diffDays = diff / (24 * 60 * 60 * 1000);
		    			//System.out.println(diffDays);
		    			if(diffDays <= 0){
		    				diffDays = 1;
		    			}
		    			log.info("Difference in days"+diffDays);
		    			log.info("rows:"+rls);
		    		
		    			for(int i =0;i<=diffDays;i++){
		    			//	System.out.println(t.getNextDate(startDate));
		    				 sql = "select top "+rls +" * from DC.Monitor_db where datetimeid = '"+startDate+"' order by MAIN_BUFFER_USAGE desc ";
		    				 try    {
		    		    	    	statement = connDwh.createStatement();
		    		    	    	result1 = statement.executeQuery(sql);  
		    		    	      if(result1 != null){
		    		    	    	while (result1.next()) {  
		    		    	    		map.put(startDate+" "+result1.getString("ROP").trim(), result1.getInt("MAIN_BUFFER_USAGE"));
		    		    	    	}
		    		    	      }
		    		    	      	}
		    		    	    catch (SQLException e) {
		    						e.printStackTrace();
		    					}finally{
		    						statement.close();
		    		    	    	//connDwh.close();
		    		    	    	result1.close();
		    		    	    }
		    				 startDate=getNextDate(startDate);
		    			}
		    	     log.info("map contains"+map);
		    	      		ctx.put("keys",new Vector<String>(map.keySet()));
		    	      		ctx.put("heap",map);
		    	      		ctx.put("command", "MainCache");
		    		 }
	      }else if (cmdLine.equalsIgnoreCase("TempCache")) {
	    	  if(search_direction.equalsIgnoreCase("day")){
	    	  String sql;
	    	  Statement statement = null;
	    	  ResultSet result1 = null;
	    	  Map<String, Integer> map = getMap();
	    	  sql = "select ROP,TEMP_BUFFER_USAGE from DC.Monitor_db where DATETIMEID = '"+startYearStr + "-" + startMonthStr + "-" + startDayStr+"'";
	    	    try    {
	    	      statement = connDwh.createStatement();
	    	      result1 = statement.executeQuery(sql);  
	    	      if(result1 != null){
	    	      while (result1.next()) {  
	    	        map.put(result1.getString("ROP").trim(), result1.getInt("TEMP_BUFFER_USAGE"));
	    	      }
	    	      }
	 	    		ctx.put("keys",new Vector<String>(map.keySet()));
	 	    		ctx.put("heap",map);
	 	    	    ctx.put("command", "TempCache");
	    	    }catch (Exception e) {
	    	    	ctx.put("keys",new Vector<String>(map.keySet()));
	 	    		ctx.put("heap",map);
	 	    	    ctx.put("command", "TempCache");
					e.printStackTrace();
				}finally{
					statement.close();
	    	    	connDwh.close();
	    	    	result1.close();
	    	    }
	    	  }else{
		    		
		    	     String sql;
		    	  	 Statement statement = null;
		    	     ResultSet result1 = null;
		    	    // Map<String, Integer> map = getMap();
		    	     Map<String, Integer> map = new TreeMap<String, Integer>();
		    	     String endDate = endYearStr + "-" + endMonthStr + "-" + endDayStr;
		    		 String startDate=startYearStr + "-" + startMonthStr + "-" + startDayStr;
		    			log.info("Selected Start date is:"+startDate);
		    			log.info("Selected End date is:"+endDate);
		    		    Date d1 = null;
		    			Date d2 = null;
		    			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		    		    d1 = format.parse(startDate);
		    			d2 = format.parse(endDate);
		    			long diff = d2.getTime() - d1.getTime();
		    			long diffDays = diff / (24 * 60 * 60 * 1000);
		    			//System.out.println(diffDays);
		    			if(diffDays <= 0){
		    				diffDays = 1;
		    			}
		    			log.info("Difference in days"+diffDays);
		    			log.info("rows:"+rls);
		    			
		    			for(int i =0;i<=diffDays;i++){
		    			//	System.out.println(t.getNextDate(startDate));
		    				 sql = "select top "+rls +" * from DC.Monitor_db  where datetimeid = '"+startDate+"' order by TEMP_BUFFER_USAGE desc ";
		    				 try    {
		    		    	    	statement = connDwh.createStatement();
		    		    	    	result1 = statement.executeQuery(sql);  
		    		    	      if(result1 != null){
		    		    	    	while (result1.next()) {  
		    		    	    		map.put(startDate+" "+result1.getString("ROP").trim(), result1.getInt("TEMP_BUFFER_USAGE"));
		    		    	    	}
		    		    	      }
		    		    	      	}
		    		    	    catch (SQLException e) {
		    						e.printStackTrace();
		    					}finally{
		    						statement.close();
		    		    	    	//connDwh.close();
		    		    	    	result1.close();
		    		    	    }
		    				 startDate=getNextDate(startDate);
		    			}
		    	     log.info("map contains"+map);
		    	      		ctx.put("keys",new Vector<String>(map.keySet()));
		    	      		ctx.put("heap",map);
		    	      		ctx.put("command", "TempCache");
		    		 }
	      }else if (cmdLine.equalsIgnoreCase("RepDBConn")) {
	    	  if(search_direction.equalsIgnoreCase("day")){
	    	  String sql;
	    	  Statement statement = null;
	    	    ResultSet result1 = null;
	    	    Map<String, Integer> map = getMap();
	    	  sql = "select execution_time,no_of_connection from  dwh_repdb_count where  execution_date= '"+startYearStr + "-" + startMonthStr + "-" + startDayStr+"' and rep_dwh_name='rep'";
	    	    try    {
	    	      statement = connDwh.createStatement();
	    	      result1 = statement.executeQuery(sql);  
	    	      if(result1 != null){
	    	      while (result1.next()) {  
	    	    	 String temp=result1.getString("execution_time");
	    	    	 String time = getDateTime(temp);
	    	    	 map.put(time.trim(), result1.getInt("no_of_connection"));
		    	      }
	    	      }
	 	    		ctx.put("keys",new Vector<String>(map.keySet()));
	 	    		ctx.put("heap",map);
	 	    		ctx.put("command", "RepDBConn");
	    	    }catch (Exception e) {
	    	    	ctx.put("keys",new Vector<String>(map.keySet()));
	 	    		ctx.put("heap",map);
	 	    		ctx.put("command", "RepDBConn");
					e.printStackTrace();
				}finally{
					statement.close();
	    	    	connDwh.close();
	    	    	result1.close();
	    	    }
	        }else{
	    		
	    	     String sql;
	    	  	 Statement statement = null;
	    	     ResultSet result1 = null;
	    	    // Map<String, Integer> map = getMap();
	    	     Map<String, Integer> map = new TreeMap<String, Integer>();
	    	     String endDate = endYearStr + "-" + endMonthStr + "-" + endDayStr;
	    		 String startDate=startYearStr + "-" + startMonthStr + "-" + startDayStr;
	    			log.info("Selected Start date is:"+startDate);
	    			log.info("Selected End date is:"+endDate);
	    		    Date d1 = null;
	    			Date d2 = null;
	    			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	    		    d1 = format.parse(startDate);
	    			d2 = format.parse(endDate);
	    			long diff = d2.getTime() - d1.getTime();
	    			long diffDays = diff / (24 * 60 * 60 * 1000);
	    			//System.out.println(diffDays);
	    			if(diffDays <= 0){
	    				diffDays = 1;
	    			}
	    			log.info("Difference in days"+diffDays);
	    			log.info("rows:"+rls);
	    			
	    			for(int i =0;i<=diffDays;i++){
	    			//	System.out.println(t.getNextDate(startDate));
	    				 sql = "select top "+rls +" * from dwh_repdb_count where execution_date = '"+startDate+"' and rep_dwh_name='rep' order by no_of_connection desc ";
	    				 try    {
	    		    	    	statement = connDwh.createStatement();
	    		    	    	result1 = statement.executeQuery(sql);  
	    		    	      if(result1 != null){
	    		    	    	while (result1.next()) {  
	    		    	    		 String temp=result1.getString("execution_time");
	    			    	    	 String time = getDateTime(temp);
	    			    	    	 map.put(startDate+" "+time.trim(), result1.getInt("no_of_connection"));
	    		    	    	}
	    		    	      }
	    		    	      	}
	    		    	    catch (SQLException e) {
	    						e.printStackTrace();
	    					}finally{
	    						statement.close();
	    		    	    	//connDwh.close();
	    		    	    	result1.close();
	    		    	    }
	    				 startDate=getNextDate(startDate);
	    			}
	    	     log.info("map contains"+map);
	    	      		ctx.put("keys",new Vector<String>(map.keySet()));
	    	      		ctx.put("heap",map);
	    	      		ctx.put("command", "RepDBConn");
	    		 }
	      }else if (cmdLine.equalsIgnoreCase("DWHDBConn")) {
	    	  if(search_direction.equalsIgnoreCase("day")){
	    	  String sql;
	    	  Statement statement = null;
	    	    ResultSet result1 = null;
	    	    Map<String, Integer> map = getMap();
	    	  sql = "select execution_time,no_of_connection from  dwh_repdb_count where  execution_date= '"+startYearStr + "-" + startMonthStr + "-" + startDayStr+"' and rep_dwh_name='dwh'";
	    	    try    {
	    	      statement = connDwh.createStatement();
	    	      result1 = statement.executeQuery(sql);  
	    	      if(result1 != null){
	    	      while (result1.next()) {  
	    	    	 String temp=result1.getString("execution_time");
	    	    	 String time = getDateTime(temp);
	    	    	 map.put(time.trim(), result1.getInt("no_of_connection"));
	    	      }
	    	      }
	 	    		ctx.put("keys",new Vector<String>(map.keySet()));
	 	    		ctx.put("heap",map);
	 	    		ctx.put("command", "DWHDBConn");
	    	    }catch (Exception e) {
	    	    	ctx.put("keys",new Vector<String>(map.keySet()));
	 	    		ctx.put("heap",map);
	 	    		ctx.put("command", "DWHDBConn");
					e.printStackTrace();
				}finally{
					statement.close();
	    	    	connDwh.close();
	    	    	result1.close();
	    	    }
	      }else{
	    		
	    	     String sql;
	    	  	 Statement statement = null;
	    	     ResultSet result1 = null;
	    	    // Map<String, Integer> map = getMap();
	    	     Map<String, Integer> map = new TreeMap<String, Integer>();
	    	     String endDate = endYearStr + "-" + endMonthStr + "-" + endDayStr;
	    		 String startDate=startYearStr + "-" + startMonthStr + "-" + startDayStr;
	    			log.info("Selected Start date is:"+startDate);
	    			log.info("Selected End date is:"+endDate);
	    		    Date d1 = null;
	    			Date d2 = null;
	    			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	    		    d1 = format.parse(startDate);
	    			d2 = format.parse(endDate);
	    			long diff = d2.getTime() - d1.getTime();
	    			long diffDays = diff / (24 * 60 * 60 * 1000);
	    			//System.out.println(diffDays);
	    			if(diffDays <= 0){
	    				diffDays = 1;
	    			}
	    			log.info("Difference in days"+diffDays);
	    			log.info("rows:"+rls);
	    			
	    			for(int i =0;i<=diffDays;i++){
	    			//	System.out.println(t.getNextDate(startDate));
	    				 sql = "select top "+rls +" * from dwh_repdb_count where execution_date = '"+startDate+"' and rep_dwh_name='dwh' order by no_of_connection desc ";
	    				 try    {
	    		    	    	statement = connDwh.createStatement();
	    		    	    	result1 = statement.executeQuery(sql);  
	    		    	      if(result1 != null){
	    		    	    	while (result1.next()) {  
	    		    	    		String temp=result1.getString("execution_time");
	    			    	    	 String time = getDateTime(temp);
	    			    	    	 map.put(startDate+" "+time.trim(), result1.getInt("no_of_connection"));
	    		    	    	}
	    		    	      }
	    		    	      	}
	    		    	    catch (SQLException e) {
	    						e.printStackTrace();
	    					}finally{
	    						statement.close();
	    		    	    	//connDwh.close();
	    		    	    	result1.close();
	    		    	    }
	    				 startDate=getNextDate(startDate);
	    			}
	    	     log.info("map contains"+map);
	    	      		ctx.put("keys",new Vector<String>(map.keySet()));
	    	      		ctx.put("heap",map);
	    	      		ctx.put("command", "DWHDBConn");
	    		 }
	      }else {
	        log.debug("Running cmd: '" + cmdLine + "'");
	        try {
	          result = CommandRunner.runCmd(cmdLine, log);
	        } catch (IOException e) {
	          log.error("SQLException",e);
	          result = "";
	        }
	      }
	    }
	  }
	    log.info("search_direction near"+search_direction);
	    
	    ctx.put("day", ((search_direction != null) && (search_direction.equalsIgnoreCase("day"))) ? "checked" : " ");
	     ctx.put("range", ((search_direction != null) && (search_direction.equalsIgnoreCase("range"))) ? "checked" : " ");
	    // put result into the context, which is read by the velocity engine
	    // and rendered to page with template called
	    ctx.put("commands", new Vector<String>(commands.keySet()));
	    ctx.put("theCommand", runCommand);
	    ctx.put("theResult", result);
	    ctx.put("search", search);
	    ctx.put("search_direction", search_direction);
	    ctx.put("row_limit", rls);
	    ctx.put("year_1", startYearStr);
        ctx.put("month_1", startMonthStr);
        ctx.put("day_1", startDayStr);
        ctx.put("year_2", endYearStr);
        ctx.put("month_2", endMonthStr);
        ctx.put("day_2", endDayStr);
        
	    try {
	      outty = getTemplate("eniq_monitoring.vm");
	    } catch (Exception e) {
	      throw new VelocityException(e);
	    }
	    
	    /*
	     * } catch (ParseErrorException pee) { log.error("Parse error for template " +
	     * pee); } catch (ResourceNotFoundException rnfe) { log.error("Template not
	     * found " + rnfe); } catch (Exception e) { log.error("Error " +
	     * e.getMessage()); }
	     */
	    return outty;
	  }
	  
	  

	  /**
	   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
	   */
	  public synchronized void init(final ServletConfig config) throws ServletException {
	    super.init(config);

	    if (initDone) {
	      return;
	    }

	    final String rtDir = System.getProperty("RT_DIR");

	    // read the commandline txt file ... hidden to this JAR
	    try {

	      final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(config.getServletContext()
	          .getRealPath(Helper.PATHSEPERATOR)
	          + Helper.getEnvEntryString("conffiles") + Helper.PATHSEPERATOR + "commands_monitoring.txt")));

	      String line = null;

	      while ((line = in.readLine()) != null) {
	        final String[] cmdDef = line.split("=");

	        if (cmdDef.length != 2) {
	          continue;
	        }

	        // log.debug("Found command: '"+cmdDef[0]+"' = '"+cmdDef[1]+"'");
	        final String commandValue = cmdDef[1].replaceAll("\\$\\{RT_DIR\\}",rtDir);
	        commands.put(cmdDef[0], commandValue);
	      }

	      in.close();
	    } catch (IOException e) {
	      // what we can do except printout the problem
	      log.debug("Error when reading commands from JAR file.");
	      log.debug("Error was: " + e.getMessage());
	      log.debug("IOException", e);
	      
	    }

	    initDone = true;
	  }
	public String getDateTime(String currentTime){
		  String[] temp1 = currentTime.split(" ");
		  log.info("Date is:"+ temp1[1]);
		  String[] temp2=temp1[1].split(":");
 	 	   String value = temp2[0]+":"+temp2[1];
 	      
	  	return   value;
	  }	
	public Map<String,Integer> getMap(){
		Map<String, Integer> map = new TreeMap<String, Integer>();
	     for(int i=00;i<24;i++){
	    	 for(int j=00;j<60;j=j+15){
	    		 if(i<10&&j<1){
					map.put("0"+i+":0"+j,0);
	    		 }else if(i>10 && j>1){
	    			map.put(""+i+":"+j,0);
	    		 }else if(i<10 && j>1){
		    		map.put("0"+i+":"+j,0);
		    	 }else if(i>10 && j<1){
			    	map.put(""+i+":0"+j,0);
			     }else{
	    			map.put(""+i+":"+j,0);
	    		 }
	    	 }
	     }
     	return map;
	}
	public  String getNextDate(String curDate) {
        String nextDate = "";
        try {
            Calendar today = Calendar.getInstance();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(curDate);
            today.setTime(date);
            today.add(Calendar.DAY_OF_YEAR, 1);
            nextDate = format.format(today.getTime());
        } catch (Exception e) {
            return nextDate;
        }
        return nextDate;
    }
	
	public String getValue(String command){
		String output = "";
		if(command.contains("Engine")){
			output = "15000";
		}else{
			output = "74";
		}
    	try {
    		
			String systemCommandString = "";
			final String user = "dcuser";

			final String service_name = "scheduler";
			List<Meta_databases> mdList = DBUsersGet
					.getMetaDatabases(user,
							service_name);
			if (mdList.isEmpty()) {
				mdList = DBUsersGet.getMetaDatabases(user,service_name);
				if (mdList.isEmpty()) {
				throw new Exception("Could not find an entry for "
						+ user+ ":"
						+ service_name + " in engine! (was is added?)");
				}
			}
			final String password = mdList.get(0).getPassword();
			systemCommandString = ". /eniq/home/dcuser; . ~/.profile; "+"cat /eniq/sw/conf/niq.ini |grep -i "+command;
			 output = RemoteExecutor.executeComand(user,password, service_name, systemCommandString );
			 String[] out2 = output.split("=");
			 output = out2[1];
			return output;
	}catch (final JSchException e) {
		e.printStackTrace();
	} catch (final Exception e) {
		e.printStackTrace();
	}
		return output;	
    }
	
}
