package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

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
import com.distocraft.dc5000.etl.gui.util.Helper;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Basically this servlet is for receiving all the status details of a selected IQ database.<br>
 * dc5000rep and dc5000dwh databases are used.
 * 
 * @author Jani Vesterinen
 */
public class StatusDetails extends EtlguiServlet { // NOPMD by eheijun on 03/06/11 08:32

  private static final long serialVersionUID = 1L;

  private boolean isDBError ;
  private final Log log = LogFactory.getLog(this.getClass());

  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response,
      final Context ctx) {
    Template page = null;
    final String usePage = "monitoring_iq_status_detail.vm";

    ctx.put("db", getDatabaseInfo(ctx, StringEscapeUtils.escapeHtml(request.getParameter("ds"))));
    // finally generate page
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
    	}else{
    		page = getTemplate(usePage);
    	}
    } catch (ResourceNotFoundException e) {
      log.error("ResourceNotException", e);
    } catch (ParseErrorException e) {
      log.error("ParseErrorException", e);
    } catch (Exception e) {
      log.error("Exception", e);
    }

    // and return with the template
    return page;
  }

  /**
   * Gets database info for selected database. Parameter is for deciding which database is connected.
   * 
   * @return DatabaseInfo
   * @param param
   *          selected database
   */
  public DatabaseInfo getDatabaseInfo(final Context ctx, final String param) {

    // Properties props = readPropertiesFile(Helper.getEnvEntryString("torque"));
    // Enumeration en = props.keys();
    final DatabaseInfo db = new DatabaseInfo(param);

    if (param.equals(MonitorInformation.IQ_NAME_STATUS_DWH)) {
      getDsInfoDetails(ctx, db, MonitorInformation.IQ_NAME_STATUS_DWH);
      db.setIsDetails(true);
    } else if (param.equals(MonitorInformation.IQ_NAME_STATUS_REP)) {
      getDsInfoDetails(ctx, db, MonitorInformation.IQ_NAME_STATUS_REP);
      db.setIsDetails(true);
    }
    return db;

  }

  /**
   * Gets database info details for selected database. Parameter is for deciding which database is connected.
   * 
   * @return DatabaseInfo
   * @param dsName
   *          selected database
   * @param dbi
   *          Databaseinfo object for injection.
   */
  public boolean getDsInfoDetails(final Context ctx, final DatabaseInfo dbi, final String dsName) { // NOPMD by eheijun
                                                                                                    // on 03/06/11 08:32
    boolean retval = false;
    final RockFactory ds = (RockFactory) ctx.get(dsName);
    final StringBuffer return_string = new StringBuffer();
    String mainIq = "";
    String totalSpace = "";
    String freeSpace = "";
    String page = "";
    String version = "";
    String otherVersionSize = "";
    String iqMain = "";

    try {

      final Statement stmt = ds.getConnection().createStatement();

      try {
        if (dsName.equalsIgnoreCase(MonitorInformation.IQ_NAME_STATUS_REP)) {
          // Get all the details from the SQL Anywhere Database...
          // command to determine the: free size, total space and version of DB...
          final ResultSet productInfoRs = stmt
              .executeQuery("select PropName, PropDescription, Value from sa_eng_properties() where PropName like 'Product%' or PropName='LegalCopyright' ");
          try {

            while (productInfoRs.next()) {
              if (productInfoRs.getString("PropName").equalsIgnoreCase("ProductVersion")) {
                version = productInfoRs.getString("Value");
              }
              return_string.append("<tr>");
              return_string.append("<td class='basic' width='250'>" + productInfoRs.getString("PropDescription") + " " + "</td>");
              return_string.append("<td class='basic' width='450'>" + productInfoRs.getString("Value") + " " + "</td>");
              return_string.append("</tr>");
            }
          } catch (SQLException e) {
            log.error("Exception", e);
          } finally {
            productInfoRs.close();
          }
          
          final ResultSet resultsetdbspace = stmt
              .executeQuery("SELECT free_space, total_space FROM sa_disk_free_space() WHERE dbspace_name = 'system';");
          try {
            if (resultsetdbspace.next()) {
              freeSpace = resultsetdbspace.getString("free_space");
              totalSpace = resultsetdbspace.getString("total_space");
            }
            return_string.append("<tr>");
            return_string.append("<td class='basic' width='250'>Free Space: " + "</td>");
            return_string.append("<td class='basic' width='450'>" + freeSpace + " bytes " + "</td>");
            return_string.append("</tr>");
            return_string.append("<tr>");
            return_string.append("<td class='basic' width='250'>Total Space: " + "</td>");
            return_string.append("<td class='basic' width='450'>" + totalSpace + " bytes " + "</td>");
            return_string.append("</tr>");
          } finally {
            resultsetdbspace.close();
          }
          
        } else {
          final ResultSet iqstatusRs = stmt.executeQuery("sp_iqstatus");

          try {
            while (iqstatusRs.next()) {
              if (iqstatusRs.getString(1).indexOf("Version") != -1
                  && iqstatusRs.getString(1).indexOf("Versions") == -1) {
                version = parseDbversion(iqstatusRs.getString(2));
              } else if (iqstatusRs.getString(1).indexOf("Main IQ Blocks") != -1
                  && iqstatusRs.getString(1).indexOf("Versions") == -1) {
                mainIq = iqstatusRs.getString(1) + iqstatusRs.getString(2);
              } else if (iqstatusRs.getString(1).indexOf("Page Size") != -1) {
                page = iqstatusRs.getString(1) + iqstatusRs.getString(2);
              } else if (iqstatusRs.getString(1).indexOf("Other Versions") != -1) {
                otherVersionSize = iqstatusRs.getString(2);
              }

              return_string.append("<tr>");
              return_string.append("<td class='basic' width='250'>" + iqstatusRs.getString(1) + " " + "</td>");
              return_string.append("<td class='basic' width='450'>" + iqstatusRs.getString(2) + " " + "</td>");
              return_string.append("</tr>");
            }
          } catch (SQLException e) {
            log.error("Exception", e);
          } finally {
            iqstatusRs.close();
          }
          final ResultSet iqdbspaceRs = stmt.executeQuery("select Usage,TotalSize from sp_iqdbspace() where DBSpaceName='IQ_MAIN'");
			try {
				while(iqdbspaceRs.next()) {
					iqMain = iqdbspaceRs.getInt(1) + " " + iqdbspaceRs.getString(2);
					
				}
					return_string.append("<tr>");
		            return_string.append("<td class='basic' width='250'>" + iqdbspaceRs.getString(1) + " " + "</td>");
		            return_string.append("<td class='basic' width='450'>" + iqdbspaceRs.getString(2) + " " + "</td>");
		            return_string.append("</tr>");
				
			}
			catch (SQLException e) {
	            log.error("Exception", e);
	        } finally{
				iqdbspaceRs.close();
			}
        }
      } finally {
        stmt.close();
      }

    } catch (Exception e) {
      log.error("Exception", e);
      retval = true;
    }
    
    if (dsName.equalsIgnoreCase(MonitorInformation.IQ_NAME_STATUS_REP)) {
      dbi.parseSQLAnywhereSize(version, totalSpace, freeSpace, "");
    } else {
      dbi.parseSize(mainIq, page, version, otherVersionSize, "", iqMain);
    }
    checkWarnings(dbi);

    if (!retval && !dbi.isWarning()) {
      dbi.setStatus(MonitorInformation.BULB_GREEN);
    } else if (dbi.isWarning()) {
      dbi.setStatus(MonitorInformation.BULB_YELLOW);
    } else {
      dbi.setStatus(MonitorInformation.BULB_RED);
    }

    if (return_string.toString() != null && return_string.toString().trim().equals("")) {
    	isDBError = true ;
      dbi.setAllInfo("<p>Unable to connect database " + dsName.substring(dsName.indexOf("_") + 1, dsName.length())
          + ". Please try again later.</p>", true);
    } else {
    	isDBError = false ;
      dbi.setAllInfo(return_string.toString(), true);
    }
    return retval;
  }

  /**
   * Checks if warnings exists and adds warning text based on warning.
   * 
   * @param dbi
   */
  private void checkWarnings(final DatabaseInfo dbi) {
    final int versionHigh = Helper.getEnvEntryInt("intOtherVersions");
    final int dbsize = Helper.getEnvEntryInt("intDbSize");
    if (versionHigh < (dbi.getOtherVersionMultip() * dbi.getOtherVersionSize())) {
      dbi.setWarning(true, "- Other versions exceeded limit -");
    }
    if (dbsize < dbi.getUsedSpace()) {
      dbi.setWarning(true, "-Database low on disk space -");
    }
  }

  public static String parseDbversion(final String string) {
    final StringTokenizer st = new StringTokenizer(string, "/");
    return st.nextToken().toString();
  }
}
