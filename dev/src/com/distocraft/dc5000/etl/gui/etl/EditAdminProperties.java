


/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.distocraft.dc5000.etl.gui.etl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.usermanagement.UserManagement;

public class EditAdminProperties extends EtlguiServlet {

  private final Log log = LogFactory.getLog(this.getClass()); // general logger

  private static String VELOCITY_TEMPLATE = "viewadminparams.vm";

  private static final String ACTION_PARAM = "action";
  private static final String VIEW_PARAM = "viewparam";
  private static final String MODIFY_PARAM = "modifyparam";
  private static final String THE_USER = "theuser";
  
  private static final String ENIQ_EVENTS_LOCK_USERS="ENIQ_EVENTS_LOCK_USERS";
  private static final String ENIQ_EVENTS_MAX_USER_SESSIONS="ENIQ_EVENTS_MAX_USER_SESSIONS";
  private static final String ENIQ_EVENTS_BANNER_MESSAGE="ENIQ_EVENTS_BANNER_MESSAGE";
  private static final String ENIQ_EVENTS_LAST_LOGIN_THRESHOLD="ENIQ_EVENTS_LAST_LOGIN_THRESHOLD";
  
  private static final String SQL_SELECT = "select * from dwhrep.ENIQ_EVENTS_ADMIN_PROPERTIES";
  private static final String SQL_MODIFY = "UPDATE  ENIQ_EVENTS_ADMIN_PROPERTIES SET PARAM_VALUE=? ,MODIFIED_BY=? WHERE PARAM_NAME=?";
  
  
  
	private static Connection getDatabaseConnection(final Context ctx) {
		return ((RockFactory) ctx.get("rockDwhRep")).getConnection();
	}


  @Override
  public Template doHandleRequest(
		  final HttpServletRequest request, 
		  final HttpServletResponse response,
          final Context velocityContext) {

   String action = request.getParameter(ACTION_PARAM);
   Vector<Vector<String>> retval = new Vector<Vector<String>>();
   boolean result=false;
   final String lockUsers= request.getParameter(ENIQ_EVENTS_LOCK_USERS);
   final String maxLogins = request.getParameter(ENIQ_EVENTS_MAX_USER_SESSIONS);
   final String bannerMessage = request.getParameter(ENIQ_EVENTS_BANNER_MESSAGE);
   final String threshold = request.getParameter(ENIQ_EVENTS_LAST_LOGIN_THRESHOLD);
   
   
   final String[] paramsToModify=new String[4];
   Template viewTemplate = null;
   
  try {
      
      String userName = " ";
      if (request.getUserPrincipal() == null) {
          log.debug(THE_USER + " is null");
      } else {
          userName = request.getUserPrincipal().getName();
          log.debug(THE_USER + " = " + userName);
      }
      velocityContext.put(THE_USER, userName);
      Connection repCon = getDatabaseConnection(velocityContext);
      if(MODIFY_PARAM.equalsIgnoreCase(action))  {
          log.debug(MODIFY_PARAM);
           
          paramsToModify[0]=lockUsers;
           paramsToModify[1]=maxLogins;
           paramsToModify[2]=bannerMessage;
           paramsToModify[3]=threshold;
           
           result=modifyAdminParam(velocityContext,paramsToModify,repCon);
           
           UserManagement.setLastLoginThreshold(Long.parseLong(threshold));
           
           velocityContext.put("paramupdate",result);
           if(result){
               velocityContext.put("notification","<font color=blue>modified system properties</font>");
           }else{
               
               velocityContext.put("notification","<font color=red>failed to modify system properties</font>");
           }    
               
       } else {
          log.debug(VIEW_PARAM);
          velocityContext.put("notification","");
       }    

      retval=getAdminParams(repCon);
      velocityContext.put("adminparams",retval);
      viewTemplate = getTemplate(VELOCITY_TEMPLATE);
      
  }  catch (ResourceNotFoundException e) {
      log.error("ResourceNotFoundException", e);
  } catch (ParseErrorException e) {
      log.error("ParseErrorException", e);
  } catch (Exception e) {
      log.error("Exception", e);
  }
   
  
  return viewTemplate;
  }

  private Vector<Vector<String>> getAdminParams(Connection repCon) {
      

      log.debug("getAdminParams ="+repCon);
      Vector<Vector<String>> retval = new Vector<Vector<String>>();
      Statement statement = null;
      ResultSet result = null;
      try {
        statement = repCon.createStatement();
        result = statement.executeQuery(SQL_SELECT);
        while (result.next()) {
          Vector<String> detail = new Vector<String>();
          detail.add(result.getString("PARAM_NAME"));
          detail.add(result.getString("PARAM_VALUE"));
          retval.add(detail);
        }
        
      } catch (SQLException e) {
        log.info("SQLException: " + e);
        log.info("When executing statement: '" + SQL_SELECT + "'");
      } finally {
        try {
          if (repCon != null) {
              repCon.commit();
          }
          if (result != null) {
            result.close();
          }
          if (statement != null) {
            statement.close();
          }
        } catch (Exception e) {
          log.error("Exception: ",e);
        }
      }
      return retval;
  }

private boolean modifyAdminParam(Context velocityContext,String[] paramsToModify,Connection repCon) {
      
      
      boolean updateSuccess=false;
      PreparedStatement statement = null;
      int index = 0;
      try {
        statement = repCon.prepareStatement(SQL_MODIFY);
        for(String item:paramsToModify){
            
            if(index==0){
                statement.setString(3, ENIQ_EVENTS_LOCK_USERS);
            }
            if(index==1){
                statement.setString(3, ENIQ_EVENTS_MAX_USER_SESSIONS);
            }
            if(index==2){
                statement.setString(3, ENIQ_EVENTS_BANNER_MESSAGE);
            }
            if(index==3){
                statement.setString(3, ENIQ_EVENTS_LAST_LOGIN_THRESHOLD);
            }
            
            statement.setString(1, item);
            statement.setString(2, (String)velocityContext.get(THE_USER));
            statement.executeUpdate();
            index+=1;
        }   
        updateSuccess=true;
        log.debug("modifyAdminParam updateSuccess="+updateSuccess);
      } catch (SQLException e) {
        log.info("SQLException: " + e);
        log.info("When executing statement: '" + SQL_MODIFY + "'");
      } finally {
        try {
          if (repCon != null) {
              repCon.commit();
          }
          if (statement != null) {
            statement.close();
          }
        } catch (Exception e) {
          log.error("Exception: ",e);
        }
      }
      return updateSuccess;
  }
}
