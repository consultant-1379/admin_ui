package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.ENIQServiceStatusInfo;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Basically this servlet is for receiving all the details of 
 * on certain datasource iqReaderstatus
 * @author Jani Vesterinen - original
 * @author eramyag
 */
@SuppressWarnings("serial")
public class ReaderStatusDetails extends EtlguiServlet {

    private Log log = LogFactory.getLog(this.getClass());

    /**
     * 
     * This stored procedure gets connection information from the target
     * database. It is inbuilt in Sybase IQ. A separately installed 
     * procedure adapts the connection info from Sybase ASA (repository
     * database) to the same format. 
     *  
     */
    private static final String SP_IQMPXINFO = "sp_iqmpxinfo";

    private static final String MONITORING_IQ_STATUS_READER_DETAIL_TEMPLATE = "monitoring_iq_status_reader_detail.vm";
    
    private static final String MONITORING_IQ_STATUS_READER_DETAIL_ERROR_TEMPLATE = "monitoring_iq_status_reader_error_detail.vm";

    /** HTML header for connection info */
    private static final String CONNECTION_STATUS_HEADER = "<tr>" + "<td class='basic' width='100'>server_id</td>"
            + "<td class='basic' width='100'>server_name</td>" + "<td class='basic' width='100'>connection_info</td>"
            + "<td class='basic' width='100'>db_path</td>" + "<td class='basic' width='100'>role</td>"
            + "<td class='basic' width='100'>status</td>" + "<td class='basic' width='100'>mpx_mode</td>"
            + "<td class='basic' width='100'>inc_state</td>" + "<td class='basic' width='100'>coordinator_failover</td>"
            + "<td class='basic' width='100'>current_version</td>" + "<td class='basic' width='100'>active_versions</td>" + "</tr>";

    private static final String[] DATABASES = { 
        MonitorInformation.IQ_NAME_STATUS_DWH,
    };

    public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) {
    	boolean isError = false ;
        try {
            ctx.put(MonitorInformation.READER_STATUS_CONTEXT, getIQreaderDetails(ctx,StringEscapeUtils.escapeHtml(request.getParameter("ds"))));
        }catch(Exception e1){
        	log.error("ReaderStatusDetails:: Exception while getting IQ Reader details : ",e1);
        	isError = true ;
        }
        
        try{
        	if(isError){
        		ctx.put("errorSet", true);
      		  	if(ENIQServiceStatusInfo.isEtlDBOffline()){
      		  		ctx.put("errorText", " Failed to initialize connection to database: " + ENIQServiceStatusInfo.getEtlDBName());	
      		  	}else if(ENIQServiceStatusInfo.isRepDBOffline()){
      		  		ctx.put("errorText", " Failed to initialize connection to database: " + ENIQServiceStatusInfo.getRepDBName());
      		  	}else if(ENIQServiceStatusInfo.isDwhDBOffline()){
      		  		ctx.put("errorText", " Failed to initialize connection to database: " + ENIQServiceStatusInfo.getDwhDBName());
      		  	}		
    			return getTemplate(EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHOUTMENU);
        	}else{
        		return getTemplate(MONITORING_IQ_STATUS_READER_DETAIL_TEMPLATE);
        	}
        } catch (ResourceNotFoundException e) {
            log.error("ResourceNotFoundException: ", e);
        } catch (ParseErrorException e) {
            log.error("ParseErrorException: ", e);
        } catch (Exception e) {
            log.error("Exception: ", e);
        } 
        return null;
    }

    /**
     * Gets connection details for selected database. 
     * @param ctx 
     *          session context
     * @param database 
     *          selected database
     * 
     * @return DatabaseInfo populated with IQ Reader information
     */
    private DatabaseInfo getIQreaderDetails(Context ctx, final String database) {
        final DatabaseInfo dbi = new DatabaseInfo(database);
        dbi.setIsDetails(true);

        if (isKnownDatabase(database)) {
            final Connection conn = ((RockFactory) ctx.get(database)).getConnection();
            final StringBuilder readerDetails = new StringBuilder();
            Statement stmt = null;
            ResultSet rs = null;
            try {

                stmt = conn.createStatement();
                rs = stmt.executeQuery(SP_IQMPXINFO);

                readerDetails.append(CONNECTION_STATUS_HEADER);

                while (rs.next()) {
                	readerDetails.append("<tr>");
                	readerDetails.append("<td class='basic' width='100'>" + rs.getString(1) + "</td>");
                	readerDetails.append("<td class='basic' width='100'>" + rs.getString(2) + "</td>");
                    readerDetails.append("<td class='basic' width='100'>" + rs.getString(3) + "</td>");
                	readerDetails.append("<td class='basic' width='450'>" + rs.getString(4) + "</td>");
                	readerDetails.append("<td class='basic' width='450'>" + rs.getString(5) + "</td>");
                	readerDetails.append("<td class='basic' width='450'>" + rs.getString(6) + "</td>");
                	readerDetails.append("<td class='basic' width='450'>" + rs.getString(7) + "</td>");
                	readerDetails.append("<td class='basic' width='450'>" + rs.getString(8) + "</td>");
                	readerDetails.append("<td class='basic' width='450'>" + rs.getString(9) + "</td>");
                	readerDetails.append("<td class='basic' width='450'>" + rs.getString(10) + "</td>");
                	readerDetails.append("<td class='basic' width='450'>" + rs.getString(11) + "</td>");
                	readerDetails.append("</tr>");
                }
               
            } catch (Exception e) {
                log.error("Exception: ", e);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    log.error("Exception: ", e);
                }
            }
            if (readerDetails.length() == 0) {
            	readerDetails.append(getConnectionErrorMessage(database));
            }
            

            dbi.setAllInfo(readerDetails.toString(), true);

        } else {
            dbi.setAllInfo(getUnknownDatabaseErrorMessage(database), true);
        }

        
        return dbi;
    }

    private static boolean isKnownDatabase(final String database) {
        return Arrays.asList(DATABASES).contains(database);
    }
    
    private static String getConnectionErrorMessage(final String database) {
        return "<p>Unable to connect database " + database + ". Please try again later.</p>";
    }

    private static String getUnknownDatabaseErrorMessage(final String database) {
        return "<p>Unknown database " + database + "</p>";
    }

}
