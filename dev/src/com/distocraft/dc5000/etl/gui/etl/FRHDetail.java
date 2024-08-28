package com.distocraft.dc5000.etl.gui.etl;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.util.FRHLogBrowser;
import com.distocraft.dc5000.etl.gui.util.Helper;

public class FRHDetail extends EtlguiServlet {

	private final Log log = LogFactory.getLog(this.getClass());

	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		Template outty = null;
	
		final String startTime = StringEscapeUtils.escapeHtml(request.getParameter("starttime"));
	    final String endTime = StringEscapeUtils.escapeHtml(request.getParameter("endtime"));
	    final String tp = StringEscapeUtils.escapeHtml(request.getParameter("techpack"));
	    
	    final String page = "FRHLogShow.vm";
	    
	    FRHLogBrowser browser = new FRHLogBrowser("/eniq/log/controller");
	    
	    String logParsingError = "";
	    List list = new LinkedList();
	    try {
	    	log.info("sending tp: "+tp);
	      list = browser.parseFRHFolder(startTime,endTime,tp);
	    } catch (Exception e) {
	      logParsingError = e.getMessage();
	      log.debug(logParsingError);
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
	    
	    ctx.put("sdf", new SimpleDateFormat("HH:mm:ss"));
	    ctx.put("pageTitle", pageHeader);
	    ctx.put("logParsingError", logParsingError);
	    if (list.size() == 0) {
	      ctx.put("listnull", "0");
	    } else {
	      ctx.put("listnull", "1");
	    }
	    
	    ctx.put("st", startTime);
	    ctx.put("et", endTime);
	    ctx.put("tp", tp);

	    
	    outty=getTemplate(page);
		return outty;
	}
	
	public void init(ServletConfig arg0) throws ServletException {
	    super.init(arg0);
	  }

}
