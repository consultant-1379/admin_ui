package com.distocraft.dc5000.etl.gui.etl;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.commons.lang.StringEscapeUtils;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.util.Helper;
import com.distocraft.dc5000.etl.gui.util.LogBrowser;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * This class is for viewing detailed information from log files.<br>
 * 
 * @author Jani Vesterinen
 */
public class EtlDetail extends EtlguiServlet {

  private Log log = LogFactory.getLog(this.getClass()); // general logger
  
  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
    
    Template outty = null;
   
   // String page = "etlDetailShow.vm"; // show this page if nothing else is
                                      // stated
    
    final String startTime = StringEscapeUtils.escapeHtml(request.getParameter("starttime"));
    final String endTime = StringEscapeUtils.escapeHtml(request.getParameter("endtime"));
    final String type =StringEscapeUtils.escapeHtml( request.getParameter("type"));
    final String tp = StringEscapeUtils.escapeHtml(request.getParameter("techpak"));
    final String sn = StringEscapeUtils.escapeHtml(request.getParameter("setname"));
    
    final String page = "logDetailShow.vm"; // show this page if nothing else is
    // stated
    
    // hardcoded now, but it is changed as a env-entry..
    LogBrowser browser = new LogBrowser(System.getProperty("LOG_DIR") + "/engine/" +StringEscapeUtils.escapeHtml( request.getParameter("techpak")));
    
    String logParsingError = "";
    List list = new LinkedList();
    try {
      list = browser.parseFolder(startTime,endTime,sn,tp,type);
    } catch (Exception e) {
      logParsingError = e.getMessage();
    }
    
    String pageFrom = request.getParameter(Helper.PARAM_PAGE_FROM);
    // The page title is decided.
    String pageHeader = "";
    if (pageFrom != null && pageFrom.equals(Helper.PARAM_PAGE_HISTORY)) {
      pageHeader = Helper.TITLE_PAGE_HISTORY;
    } else if (pageFrom != null && pageFrom.equals(Helper.PARAM_PAGE_SESLOG)) {
      pageHeader = Helper.TITLE_PAGE_SESSION;
    } else {
      pageHeader = "Log details";
    }
    ctx.put("listofvalues", list);
     
    //Vector msgs = detailParser(list);
    //ctx.put("listofvalues", msgs);
    
    ctx.put("sdf", new SimpleDateFormat("HH:mm:ss"));
    ctx.put("pageTitle", pageHeader);
    ctx.put("logParsingError", logParsingError);
    if (list.size() == 0) {
      ctx.put("listnull", "0");
    } else {
      ctx.put("listnull", "1");
    }
    // return the page that should be shown
    
    ctx.put("st", startTime);
    ctx.put("et", endTime);
    ctx.put("type", type);
    ctx.put("tp", tp);
    ctx.put("sn", sn);
    
    outty = getTemplate(page);
    
    return outty;
  }

  
  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig arg0) throws ServletException {
    super.init(arg0);
  }

}
