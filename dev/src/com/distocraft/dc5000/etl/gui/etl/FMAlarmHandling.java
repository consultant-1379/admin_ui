package com.distocraft.dc5000.etl.gui.etl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

import ssc.rockfactory.RockFactory;

public class FMAlarmHandling extends EtlguiServlet{
	private final Log log = LogFactory.getLog(this.getClass());
	
	
	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		Template outty = null;
		outty = getTemplate("FMAlarmHandle.vm");
		ctx.put("FMAlarmHandling", getAlarmTable(ctx));
		return outty;			
	}


	private Vector getAlarmTable(Context ctx) throws SQLException {
		Connection connDwh = null;
	    if(connDwh==null){
	    	  connDwh=((RockFactory)ctx.get("rockDwh")).getConnection();     
	      }
	    final Vector<List<Object>> v = new Vector<List<Object>>();
	    String sql = null;
	    Statement statement = null;
	  	ResultSet result = null;
	  	try {
			statement = connDwh.createStatement();
			sql = "select * from DC_Z_ALARM_ERROR";  
  		  result = statement.executeQuery(sql);
  		
  		  while (result.next()){ 
  			final List<Object> col = new Vector<Object>();
  			col.add(result.getString("ENMHostname"));
            col.add(result.getString("ErrorDetail"));
            col.add(result.getString("AlarmName"));
            col.add(result.getString("ManagedObjectInstance"));
            col.add(result.getString("ObjectOfReference"));
            col.add(result.getString("OssName"));
            col.add(result.getString("ReportTitle"));
            col.add(result.getString("EventTime"));
            v.add(col);
            log.debug("Iterating the DC_A_ALARM_ERROR" + col);
  		  }
	  	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			connDwh.close();
		}
	      return v;
		}
}
