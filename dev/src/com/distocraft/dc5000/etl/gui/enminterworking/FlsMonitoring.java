package com.distocraft.dc5000.etl.gui.enminterworking;

import java.rmi.Naming;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.common.CalSelect;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.ericsson.eniq.enminterworking.IEnmInterworkingRMI;


import ssc.rockfactory.RockFactory;

public class FlsMonitoring extends EtlguiServlet{
	private static boolean isValidDate(String input) {
	    String formatString = "yyyy-MM-dd HH:mm:ss";

	    try {
	        SimpleDateFormat format = new SimpleDateFormat(formatString);
	        format.setLenient(false);
	        format.parse(input);
	    } catch (ParseException e) {
	        return false;
	    } catch (IllegalArgumentException e) {
	        return false;
	    }

	    return true;
	}
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	private final Log log = LogFactory.getLog(this.getClass());
	private List<String> allENMHostnames = new ArrayList<>();
			
	@SuppressWarnings("deprecation")
	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) {
		
		Template outty = null;
		
		String enmHostname = request.getParameter("enmHostname");
		String year_1 = request.getParameter("year_1");
	    String month_1 = request.getParameter("month_1");
	    String day_1 = request.getParameter("day_1");
	    String hour_1 = request.getParameter("hour_1");
	    String min_1 = request.getParameter("min_1");
	    String sec_1 = request.getParameter("sec_1");
	    String date_1=" ";
	    
	    Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date_now = (sdf.format(cal.getTime()));
		
		 //This sends a vector of valid years from DIM_DATE Table.
	    //This is used by cal_select_1.vm
		try(Connection conn = ((RockFactory) ctx.get("rockDwh")).getConnection()){
		    CalSelect calSelect = new CalSelect(conn);
		    ctx.put("validYearRange", calSelect.getYearRange());
		    
		    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	    	Date Date_1 = new Date(System.currentTimeMillis());
	    	cal.setTime(Date_1);
//	    	cal.add(Calendar.DATE, -4);
//	    	String past_date = dateFormat.format(cal.getTime());
//	    	log.debug("Past 3 days is " + past_date);		  
	
		    if(year_1==null){
		    	year_1="-";
		    }
		    if(month_1==null){
		    	month_1="-";
		    }
		    if(day_1==null){
		    	day_1="-";
		    }
		    if(hour_1==null){
		    	hour_1="-";
		    }
		    if(min_1==null){
		    	min_1="-";
		    }
		    if(sec_1==null){
		    	sec_1="-";
		    }
		    if ((year_1 != "-") && (month_1 != "-") && (day_1 != "-") && (hour_1 != "-") && (min_1 != "-") && (sec_1 != "-")) {
		        date_1 = year_1 + "-" + month_1 + "-" + day_1 + " " + hour_1 + ":" + min_1 + ":" + sec_1;
		        String date_2 = year_1 + "-" + month_1 + "-" + day_1;
		        if (isValidDate(date_1))
		        {
		          if ((date_1.matches("\\d+-\\d+-\\d+ \\d+:\\d+:\\d+")) && (date_2.compareToIgnoreCase(date_now) > 0))
		          {
		            log.info("Given date is future date");
		            ctx.put("resultpage", true);
		            ctx.put("future_date", true);
		          }
		          
		          else if ((date_1.matches("\\d+-\\d+-\\d+ \\d+:\\d+:\\d+"))) {
		            IEnmInterworkingRMI multiEs = (IEnmInterworkingRMI)Naming.lookup(RmiUrlFactory.getInstance().getMultiESRmiUrl(EnmInterUtils.getEngineIP()));
		            multiEs.adminuiFlsQuery(getENMAlias(enmHostname), date_1);
		            
		            ctx.put("resultpage", false);
		          }
		          else
		          {
		        	ctx.put("past_date", true);
		            ctx.put("resultpage", true);
		          }
		        }
		        else
		        {
		          ctx.put("invalid_date", true);
		          ctx.put("resultpage", true);
		        }
		      }
		      else
		      {
		        ctx.put("resultpage", true);
		      }
		    
		    //Removes all preExisting entries
		    allENMHostnames.clear();
		    //Gets the list of enmServers for which Persisted_<enmAlias>.ser  file is not present
		    allENMHostnames = EnmInterUtils.getEnmWithoutPersisterFile();	    
		    log.debug("FlsMonitoring, persisted_*.ser file not present for : " + allENMHostnames);
		    
		    ctx.put("enmHostname", enmHostname);
		    ctx.put("allENMHostnames", allENMHostnames);
		    ctx.put("year_1", year_1);
		    ctx.put("month_1", month_1);
		    ctx.put("day_1", day_1);
		    ctx.put("hour_1", hour_1);
		    ctx.put("min_1", min_1);
		    ctx.put("sec_1", sec_1);
		    ctx.put("date_1", date_1);
		    ctx.put("enmAlias", getENMAlias(enmHostname));
		    ctx.put("servername", EnmInterUtils.getFullyQualifiedHostname());
		    
		} catch(Exception e){
			log.warn("Exception caught ", e);
		}

		if (allENMHostnames.isEmpty()){
			ctx.put("ERROR", true);
		}
		
		try {
			outty = getTemplate("fls_monitoring.vm");
		} catch (Exception e) {
			log.debug("Exception in (getTemplate):", e);
		}
		return outty;
	}
	
	/**
	 * 
	 * @param enmHostname
	 * @return enmHostsnameAlias
	 */
	private String getENMAlias(String enmHostname) {		
		for(Map.Entry<String, String> entry : EnmInterUtils.getOssIdToHostNameMap().entrySet()) {
			if(Objects.equals(enmHostname, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
}
