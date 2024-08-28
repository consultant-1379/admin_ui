package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.File;
import java.sql.Connection;

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
import com.distocraft.dc5000.etl.gui.util.Helper;
import com.distocraft.dc5000.etl.gui.util.ParamValue;

/**
 * This class shows a list of licensing logs.
 * 
 * @author ejannbe & ecarbjo
 * 
 */
public class LicenseLogsViewer extends EtlguiServlet {

  private static final long serialVersionUID = -6243390922786462014L;
  private final static String printLogPage = "show_license_logs.vm";
  private final static String printLogErrorPage = "show_license_logs.vm";
  private Log log = LogFactory.getLog(this.getClass());
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response,
      final Context ctx) {
	  boolean isDBError = false ;
	  
	  try{
		  prepareDateChooser(request, ctx);
	  }catch(Exception e){
		  log.error("LicenseLogsViewer:: Exception while getting database information : ",e);
		  isDBError = true ;
	  }
    
    
    Template page = null;
    try {
    	if(isDBError){
    		ctx.put("errorSet", true);
  		  	if(ENIQServiceStatusInfo.isEtlDBOffline()){
  		  		ctx.put("errorText", " Failed to initialize connection to database: " + ENIQServiceStatusInfo.getEtlDBName());	
  		  	}else if(ENIQServiceStatusInfo.isRepDBOffline()){
  		  		ctx.put("errorText", " Failed to initialize connection to database: " + ENIQServiceStatusInfo.getRepDBName());
  		  	}else if(ENIQServiceStatusInfo.isDwhDBOffline()){
  		  		ctx.put("errorText", " Failed to initialize connection to database: " + ENIQServiceStatusInfo.getDwhDBName());
  		  	}		
			return getTemplate(EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU);
    	}
    	page = getTemplate(printLogPage);
    } catch (ResourceNotFoundException e) {
        log.error("ResourceNotFoundException: ",e);
    } catch (ParseErrorException e) {
    	log.error("ParseErrorException: ",e);
    }catch (Exception e) {
    	log.error("Exception: ",e);
    }
    return page;
  }
  
  public static void prepareDateChooser(final HttpServletRequest request, final Context ctx) {
    DbCalendar calendar = new DbCalendar();
    
    final HttpSession session = request.getSession(false);
    
    String year_1 = request.getParameter("year_1");
    String month_1 = request.getParameter("month_1");
    String day_1 = request.getParameter("day_1");

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
    
    if (year_1 == null) {
      year_1 = calendar.getYearString();
      month_1 = calendar.getMonthString();
      day_1 = calendar.getDayString();
    }
    
    //This sends a vector of valid years from DIM_DATE Table.
    //This is used by cal_select_1.vm
    Connection conn = ((RockFactory) ctx.get("rockDwh")).getConnection();
    CalSelect calSelect = new CalSelect(conn);
    ctx.put("validYearRange", calSelect.getYearRange());

    ctx.put("year_1", year_1);
    ctx.put("month_1", month_1);
    ctx.put("day_1", day_1);
  }
}
