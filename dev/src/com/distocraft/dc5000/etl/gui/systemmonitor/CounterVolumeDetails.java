package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
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


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.taskdefs.Length;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

public class CounterVolumeDetails extends EtlguiServlet {
	  private final Log log = LogFactory.getLog(this.getClass());
	  private boolean counter5 = false;
	  private boolean counter30 = false;
	  private boolean counter60 = false;
	  
	@Override
	public Template doHandleRequest(HttpServletRequest request,
			HttpServletResponse response, Context ctx) throws Exception {
	    Template outty = null;
		Map map = getCounterVolume(ctx);
		 String search_direction = "15min";
		 search_direction =	request.getParameter("search_direction");
		 if(search_direction == null){
			 search_direction = "15min";
		 }
		ctx.put("keys",new Vector<String>(map.keySet()));
		ctx.put("counterVolume", map);
		// TODO Auto-generated method stub
		try {
		      outty = getTemplate("counterVolume_details.vm");
		      ctx.put("search_direction",search_direction);
		      ctx.put("rop15", ((search_direction != null) && (search_direction.equalsIgnoreCase("15min"))) ? "checked" : " ");
		      ctx.put("rop30", ((search_direction != null) && (search_direction.equalsIgnoreCase("30min"))) ? "checked" : " ");
			   ctx.put("rop5", ((search_direction != null) && (search_direction.equalsIgnoreCase("5min"))) ? "checked" : " ");
			   ctx.put("rop60", ((search_direction != null) && (search_direction.equalsIgnoreCase("60min"))) ? "checked" : " ");
		    } catch (Exception e) {
		      throw new VelocityException(e);
		    }
		    
		return outty;
	} 

	private Map getCounterVolume(Context ctx) throws SQLException {
		Connection connDwh = null;
	    if(connDwh==null){
	    	  connDwh=((RockFactory)ctx.get("rockDwh")).getConnection();     
	      }
	    Map map = new LinkedHashMap();
	  String counterVolume="0";
	  String sql;
  	  Statement statement = null;
  	  ResultSet result = null;
  	  DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd ");
  	  Date date = new Date();
  
  	  try {
		statement = connDwh.createStatement();
	    	  
	    	  String[] values = getDateTime();
	    	  log.info("Values length:"+values.length);
	    	  for(int i=0;i<values.length;i++){
	    		  sql = "select sum(NUM_OF_COUNTERS) from LOG_SESSION_ADAPTER where rop_starttime ='"+values[i]+"' and SOURCE != 'INTF_DC_E_BULK_CM'";
	    		  result = statement.executeQuery(sql);  
	    		  while (result.next()) { 
	 	    	 	String volume = result.getString(1);
	 	    	 	log.info("Rop"+i+":"+volume);
	 	    	 	 if(volume==null){
	 	    	 		volume = "0";
			 	    	 	 }
	 	    		double val = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(volume)/1000000));
	 	    	 	map.put(values[i], val);
	 	    	  }
	    		
	    	  }
	    	  Map counter5Map = get5MinCounterVolume(ctx,connDwh);
	    	  if(counter5Map.size() >= 1){
	    		  log.info("Map size"+counter5Map.size());
	    		  counter5 =true;
	    		  ctx.put("counter5min", "true");
	    			ctx.put("keys5",new Vector<String>(counter5Map.keySet()));
	 	    	 	ctx.put("counterVolume5", counter5Map);
	    	  }else{
	    		  ctx.put("counter5min", "false");
	    		  log.info("Map size is zero");
	    	  }
	    	  Map counter30Map = get30MinCounterVolume(ctx,connDwh);
	    	  if(counter30Map.size() >= 1){
	    		  log.info("Map size"+counter30Map.size());
	    		  counter30 =true;
	    		  ctx.put("counter30min", "true");
	    			ctx.put("keys30",new Vector<String>(counter30Map.keySet()));
	 	    	 	ctx.put("counterVolume30", counter30Map);
	 	    	 	log.info("getCounterVolume");
	    	  }else{
	    		  ctx.put("counter30min", "false");
	    		  log.info("counter30 :Map size is zero");
	    	  }
	    	 
	    	  Map counter60Map = get60MinCounterVolume(ctx,connDwh);
	    	  if(counter60Map.size() >= 1){
	    		  counter60 =true;
	    		  ctx.put("counter60min", "true");
	    			ctx.put("keys60",new Vector<String>(counter60Map.keySet()));
	 	    	 	ctx.put("counterVolume60", counter60Map);
	    	  }else{
	    		  ctx.put("counter60min", "false");
	    	  }
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}finally{
		connDwh.close();
	}
    
      return map;
	}
	
	private Map get5MinCounterVolume(Context ctx, Connection connDwh) throws SQLException {
	    if(connDwh==null){
	    	  connDwh=((RockFactory)ctx.get("rockDwh")).getConnection();     
	      }
	    Map map = new LinkedHashMap();
	  String counterVolume="0";
	  String sql;
  	  Statement statement = null;
  	  ResultSet result = null;
  	  DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd ");
  	  Date date = new Date();
  	  try {
		statement = connDwh.createStatement();
	    	  String[] values = getDateTime();
	    	  log.info("Values length:"+values.length);
	    		  sql = "select distinct ROP_STARTTIME, sum(NUM_OF_COUNTERS) from LOG_SESSION_ADAPTER where ROP_STARTTIME >='"+values[95] +"' and ROP_STARTTIME < '"+values[0] +"' and SOURCE != 'INTF_DC_E_BULK_CM' and DATEDIFF( MINUTE, rop_starttime, rop_endtime) =5 group by ROP_STARTTIME order by ROP_STARTTIME desc";
	    		  result = statement.executeQuery(sql);  
	    		  while (result.next()) { 
	    			String rop =  result.getString(1);
	 	    	 	String volume = result.getString(2);
	 	    	 	log.info("volume of 5 : "+volume);
	 	    	 	 if(volume==null){
	 	    	 		volume = "0";
			 	    	 	 }
	 	    		double val = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(volume)/1000000));
	 	    	 	map.put(rop, val);
	 	    	  }
	    	} catch (SQLException e) {
		// TODO Auto-generated catch block
	    		log.info("Caught an SQL exception"+ e.getMessage());
		e.printStackTrace();
	}
    
      return map;
	}
	
	private Map get30MinCounterVolume(Context ctx, Connection connDwh) throws SQLException {
	    if(connDwh==null){
	    	  connDwh=((RockFactory)ctx.get("rockDwh")).getConnection();     
	    }
	    Map map = new LinkedHashMap();
	  String counterVolume="0";
	  String sql;
  	  Statement statement = null;
  	  ResultSet result = null;
  	  DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd ");
  	  Date date = new Date();
  	  try {
		statement = connDwh.createStatement();
	    	  String[] values = getDateTime();
	    	  log.info("Values length:"+values.length);
	    	  sql = "select distinct ROP_STARTTIME, sum(NUM_OF_COUNTERS) from LOG_SESSION_ADAPTER where ROP_STARTTIME >='"+values[95] +"' and ROP_STARTTIME < '"+values[0] +"' and SOURCE != 'INTF_DC_E_BULK_CM' and DATEDIFF( MINUTE, rop_starttime, rop_endtime) =30 group by ROP_STARTTIME order by ROP_STARTTIME desc";
	    		  result = statement.executeQuery(sql);  
	    		  while (result.next()) { 
	    			String rop =  result.getString(1);
	 	    	 	String volume = result.getString(2);
	 	    	 	log.info("volume of 30 : "+volume);
	 	    	 	 if(volume==null){
	 	    	 		volume = "0";
	 	    	 		log.info("volume is null");
			 	    	 	 }
	 	    		double val = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(volume)/1000000));
	 	    	 	map.put(rop, val);
	 	    	  }
	    		
	      
	} catch (SQLException e) {
		e.printStackTrace();
		log.info("Caught an SQL exception"+ e.getMessage());
	}catch (Exception e) { 
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      return map;
	}
	
	private Map get60MinCounterVolume(Context ctx, Connection connDwh) throws SQLException {
	    if(connDwh==null){
	    	  connDwh=((RockFactory)ctx.get("rockDwh")).getConnection();     
	      }
	    Map map = new LinkedHashMap();
	  String counterVolume="0";
	  String sql;
  	  Statement statement = null;
  	  ResultSet result = null;
  	  DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd ");
  	  Date date = new Date();

  	  try {
		statement = connDwh.createStatement();
	    	  
	    	  String[] values = getDateTime();
	    	  log.info("Values length:"+values.length);
	    		  sql = "select distinct ROP_STARTTIME, sum(NUM_OF_COUNTERS) from LOG_SESSION_ADAPTER where ROP_STARTTIME >='"+values[95] +"' and ROP_STARTTIME < '"+values[0] +"' and SOURCE != 'INTF_DC_E_BULK_CM' and DATEDIFF( MINUTE, rop_starttime, rop_endtime) =60 group by ROP_STARTTIME order by ROP_STARTTIME desc";
	    		  result = statement.executeQuery(sql);  
	    		  while (result.next()) { 
	    			String rop =  result.getString(1);
	 	    	 	String volume = result.getString(2);
	 	    	 	 if(volume==null){
	 	    	 		volume = "0";
			 	    	 	 }
	 	    		double val = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(volume)/1000000));
	 	    	 	map.put(rop, val);
	 	    	  }
	    		
	    //	  }
	      
	} catch (SQLException e) {
		log.info("Caught an SQL exception"+ e.getMessage());
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return map;
	}
	
	public String[] getDateTime(){
		   Calendar now = Calendar.getInstance();
		   int hour = now.get(Calendar.HOUR_OF_DAY);
		   int minute = now.get(Calendar.MINUTE);
		   int value = now.get(Calendar.MINUTE);;
		   if(minute > 0 && minute < 15){
			   minute = 00;
		   }else if(minute > 15 && minute <30){
			   minute=15;
		   }else if(minute > 30 && minute <45){
			   minute=30;
		   }else{
			   minute = 45;
		   }
		   
		   int newValue = value - minute;
		   	newValue = newValue+15;
		    now = Calendar.getInstance();
		    now.add(Calendar.MINUTE, -newValue);
		    String a[] = new String[96];
		    String h=null;
		    String m = null;
		    String d,mo = null;
		    for(int i=0;i<96;i++){
		    	now.add(Calendar.MINUTE, -15);
		    	//a[i]=now.get(Calendar.YEAR)+":"+now.get(Calendar.MONTH)+":"+now.get(Calendar.DATE)+" "+now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE)+":00";
		    	int hours = now.get(Calendar.HOUR_OF_DAY);
		    	if(hours < 10){
		    		h="0"+hours;
		    	}else{
		    		h= String.valueOf(hours);
		    	}
		    	
		    	int minutes = now.get(Calendar.MINUTE);
		    	if(minutes < 10){
		    		m="0"+minutes;
		    	}else{
		    		m= String.valueOf(minutes);
		    	}
		    	 int days = now.get(Calendar.DATE);
		    	 if(days <10){
		    		 d="0"+days;
		    	 }else{
		    		 d= String.valueOf(days);
		    	 }
		    
		    	 int months = now.get(Calendar.MONTH)+1;
		    	 if(months < 10){
		    		 mo="0"+months;
		    	 }else{
		    		 mo=String.valueOf(months);
		    	 }
		    	
		    	a[i]=now.get(Calendar.YEAR)+"-"+mo+"-"+d+" "+h + ":" + m+":00";
		    }
			return a;
		    
	  }	
	
}
