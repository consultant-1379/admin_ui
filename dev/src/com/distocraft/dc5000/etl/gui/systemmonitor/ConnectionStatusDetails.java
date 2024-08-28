package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.commons.lang.StringEscapeUtils;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.ENIQServiceStatusInfo;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Basically this servlet is for receiving all the details of on certain datasource iqstatus
 * @author Jani Vesterinen
 */
public class ConnectionStatusDetails extends EtlguiServlet {

  private Context ctx = null;
  private Log log = LogFactory.getLog(this.getClass());
  
  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) {
    Template page = null;
    boolean isDBError = false ;
    this.ctx = ctx;
    final String usePage = "monitoring_iq_connection_status_detail.vm";
    final String useErrorDBPage = "monitoring_iq_connection_status_detail_dberror.vm"; 
    try{
    	ctx.put("connection", getDatabaseInfo(StringEscapeUtils.escapeHtml(request.getParameter("connection"))));
    }catch(Exception e){
    	log.error("ConnectionStatusDetails:: Exception while getting database information : ",e);
    	isDBError = true ;
    }
     
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
			return getTemplate(EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHOUTMENU);
    		//page = getTemplate(useErrorDBPage);
    	}else{
    		page = getTemplate(usePage);
    	}
    } catch (ResourceNotFoundException e) {
      log.error("ResourceNotFoundException: ",e);
    } catch (ParseErrorException e) {
      log.error("ParseErrorException: ",e);
    } catch (Exception e) {
      log.error("Exception: ",e);
    }

    // and return with the template
    return page;
  }
  /**
   * Gets database info for selected database. Parameter is for deciding which database is connected.
   * @return DatabaseInfo
   * @param param selected database
   */
  private DatabaseInfo getDatabaseInfo(String param) {
   // Properties props = readPropertiesFile(Helper.getEnvEntryString("torque"));
    DatabaseInfo db = new DatabaseInfo(param);

    if (param.equals(MonitorInformation.IQ_NAME_STATUS_DWH) ){
      getDsInfoDetails(db, MonitorInformation.IQ_NAME_STATUS_DWH);
      db.setIsDetails(true);
    }else if (param.equals(MonitorInformation.IQ_NAME_STATUS_REP)){
      getDsInfoDetails(db, MonitorInformation.IQ_NAME_STATUS_REP);
      db.setIsDetails(true);
    }
    return db;
    
  }

  /**
   * Gets database info details for selected database. Parameter is for deciding which database is connected.
   * @return DatabaseInfo
   * @param dsName selected database
   * @param dbi Databaseinfo object for injection.
   */
  public boolean getDsInfoDetails(DatabaseInfo dbi, String dsName){
	  boolean retval = false;
	  Connection conn = ((RockFactory)ctx.get(dsName)).getConnection();

	  StringBuffer return_string = new StringBuffer();

	  Statement stmt = null;
	  ResultSet rest = null;
	  try {

		  stmt = conn.createStatement();
		  if(dsName.equalsIgnoreCase(MonitorInformation.IQ_NAME_STATUS_REP)){
			  rest = stmt.executeQuery("select UserId, LastReqTime, ReqType, CommLink, NodeAddr from sa_conn_info();");

			  return_string.append("<tr>");
			  return_string.append("<td class='basic' width='100'>UserId</td>"); 
			  return_string.append("<td class='basic' width='100'>LastReqTime</td>");
			  return_string.append("<td class='basic' width='100'>ReqType</td>"); 
			  return_string.append("<td class='basic' width='100'>CommLink</td>");        
			  return_string.append("<td class='basic' width='100'>ReqType</td>"); 
			  return_string.append("<td class='basic' width='100'>NodeAddr</td>"); 
			  return_string.append("</tr>");
			  while (rest.next()) {
				  return_string.append("<tr>");
				  return_string.append("<td class='basic' width='100'>"+ rest.getString("UserId") + "</td>"); 
				  return_string.append("<td class='basic' width='100'>"+ rest.getString("LastReqTime")+ "</td>");
				  return_string.append("<td class='basic' width='100'>" + rest.getString("ReqType") + "</td>"); 
				  return_string.append("<td class='basic' width='450'>" + rest.getString("CommLink") + "</td>");
				  return_string.append("<td class='basic' width='450'>" + rest.getString("ReqType") + "</td>");
				  return_string.append("<td class='basic' width='450'>" + rest.getString("NodeAddr") + "</td>");
				  return_string.append("</tr>");
			  }
		  }else{
			  rest = stmt.executeQuery("sp_iqconnection");

			  return_string.append("<tr>");
			  return_string.append("<td class='basic' width='100'>IqConnId</td>"); 
			  return_string.append("<td class='basic' width='100'>IqConnName</td>");
			  return_string.append("<td class='basic' width='100'>Userid</td>"); 
			  return_string.append("<td class='basic' width='100'>LastReqTime</td>");        
			  return_string.append("<td class='basic' width='100'>ReqType</td>"); 
			  return_string.append("<td class='basic' width='100'>IQCmdType</td>"); 
			  return_string.append("<td class='basic' width='100'>LastIQCmdTime</td>"); 
			  return_string.append("<td class='basic' width='100'>ConnCreateTime</td>"); 
			  return_string.append("<td class='basic' width='100'>IQconnHandle</td>"); 
			  return_string.append("<td class='basic' width='100'>CommLink</td>"); 
			  return_string.append("<td class='basic' width='100'>NodeAddr</td>"); 
			  return_string.append("</tr>");
			  while (rest.next()) {
				  return_string.append("<tr>");
				  return_string.append("<td class='basic' width='100'>"+ rest.getString(15) + "</td>"); 
				  return_string.append("<td class='basic' width='100'>"+ (rest.getString("name").trim().equals("") ? "n/a" : rest.getString("name")) + "</td>");
				  return_string.append("<td class='basic' width='100'>" + rest.getString(3) + "</td>"); 
				  return_string.append("<td class='basic' width='450'>" + rest.getString(4) + "</td>");
				  return_string.append("<td class='basic' width='450'>" + rest.getString(5) + "</td>");
				  return_string.append("<td class='basic' width='450'>" + rest.getString(6) + "</td>");
				  return_string.append("<td class='basic' width='450'>" + rest.getString(7) + "</td>");  
				  return_string.append("<td class='basic' width='450'>" + rest.getString(12) + "</td>");  
				  return_string.append("<td class='basic' width='450'>" + rest.getString(1) + "</td>"); 
				  return_string.append("<td class='basic' width='450'>" + rest.getString(18) + "</td>");  
				  return_string.append("<td class='basic' width='450'>" + rest.getString("NodeAddr") + "</td>"); 
				  return_string.append("</tr>");
			  }
		  }

    } catch (Exception e) {
      log.error("Exception: ",e);
      retval = true;
    } finally {
      try {
        rest.close();
        stmt.close();
      } catch (SQLException e) {
        log.error("Exception: ",e);
      }
      
    }

    if (return_string.toString() != null && return_string.toString().trim().equals("")) {
      dbi.setAllInfo("<p>Unable to connect database " + dsName.substring(dsName.indexOf("_")+1, dsName.length()) + ". Please try again later.</p>", true);
    } else {
      dbi.setAllInfo(return_string.toString(), true);
    }
    return retval;
  }
  /**
   * Reads properties file where Torque.properties stands for. 
   * @param path
   * @return properties
   */  
  public Properties readPropertiesFile(String path){
    InputStream propsFile;
    Properties tempProp = new Properties();
    try {
        propsFile = new FileInputStream(path);
        tempProp.load(propsFile);
        propsFile.close();
    } catch (IOException ioe) {
      log.error("IOException: ",ioe);
    }
    return tempProp;
  }  

}
