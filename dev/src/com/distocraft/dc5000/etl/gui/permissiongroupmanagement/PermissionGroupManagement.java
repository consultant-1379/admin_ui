/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.distocraft.dc5000.etl.gui.permissiongroupmanagement;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;


import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.SecurityAdminConstants;
import com.distocraft.dc5000.etl.gui.common.SecurityAdminHelper;
import com.distocraft.dc5000.etl.gui.login.LoginForm;
import com.ericsson.eniq.ldap.handler.IHandler;
import com.ericsson.eniq.ldap.handler.PermissionGroupHandler;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.util.LDAPConstants;
import com.ericsson.eniq.ldap.util.LDAPUtil;
import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.PermissionGroupVO;
import com.ericsson.eniq.ldap.vo.PermissionVO;


public class PermissionGroupManagement extends EtlguiServlet {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

// /
  // Request parameters
  // /
  private static final String GROUP_ID_PARAM = "gid"; // for edit/delete/etc. |
                                                      // gid=group name

  // /
  // Actions verbs
  // /
  private static final String VIEW_PERM_GRPS_ACTION = "viewPermissionGroups";

  private static final String ADD_PERM_GRP_ACTION = "addPermissionGroup";

  private static final String EDIT_PERM_GRP_ACTION = "editPermissionGroup";

  private static final String DELETE_PERM_GRP_ACTION = "deletePermissionGroup";

  private static final String VIEW_PERM_GRP_PERM_GRPS_ACTION = "viewPermissions";

  // /
  // VM files for action verbs
  // /
  private static final String VIEW_PERM_GRPS_TEMPLATE = "aac_view_permission_groups.vm";

  private static final String EDIT_PERM_GRP_TEMPLATE = "aac_edit_permission_group.vm";

  private static final String VIEW_PERM_GRP_PERM_GRPS_TEMPLATE = "aac_view_permgroups_perms.vm";
   
  private static final String LOGOUT_TEMPLATE = "logout.vm";

  // /
  // VM context variables
  // /
  private static final String ALL_PERM_GRPS_VAR = "allPermGroups";

  private static final String PERM_GRP_DATA_VAR = "permGroupData";

  private static final String AVAIL_PERMISSIONS_VAR = "availPermissions";
  
  private static final String AVAIL_PERMISSIONS_JS_ARR_VAR = "permArray";
  
  

  // /
  // HTML Form IDs
  // /
  private static final String I_PERM_GRP_NAME = "permGroupName";

  private static final String I_PERM_GRP_TITLE = "permGroupTitle";

  private static final String I_PERM_GRP_DESC = "permGroupDescription";

  private static final String I_PERM_GRP_PERM_GRPS = "permissions";

  private static final String I_FORM_VALID = "formValid"; // if 'YES' set form
                                                          // data is valid in
                                                          // req.

  // /
  // AUX
  // /
  private final Log log = LogFactory.getLog(this.getClass());

  protected final IHandler permGrpHandler = new PermissionGroupHandler(); // protected
                                                                          // for
                                                                          // testing

  @Override
  public Template doHandleRequest(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse,
      final Context velocityContext) throws Exception {

    Template velocityTemplate = null ;
    String velocityTemplateFile = null;
    String notificationToDisplay = LDAPConstants.EMPTY_STRING;
    //    String _debug = LDAPConstants.EMPTY_STRING;
	
	HttpSession session = null;
     String reqGroupId = null;
   
	try {

        session = servletRequest.getSession();
        reqGroupId = servletRequest.getParameter(GROUP_ID_PARAM); // edit/delete/etc.
                                                                             // null

      // //
      // get logged user data
      // //
      final LoginForm loginForm = (LoginForm) session.getAttribute(SecurityAdminConstants.LOGIN_FORM_NAME);
      if (loginForm == null) {
        throw new Exception("Session login details are null!");
      }

      final String loggedUserName = loginForm.getUserName();
      final String loggedUserPwd = loginForm.getUserPassword();
	
	  velocityContext.put(SecurityAdminConstants.LOGGED_USER_NAME_VAR, loggedUserName); // for
                                                                                        // footer.vm


      // login object
      final LoginVO loginVo = new LoginVO();
      loginVo.setLoginId(loggedUserName);
      loginVo.setPassword(loggedUserPwd);

      String action = servletRequest.getParameter(SecurityAdminConstants.ACTION_PARAM);
      if (action == null) {
        // default action
        action = VIEW_PERM_GRPS_ACTION;
      }

      final boolean addGroup = ADD_PERM_GRP_ACTION.equalsIgnoreCase(action);
      final boolean editGroup = EDIT_PERM_GRP_ACTION.equalsIgnoreCase(action);

      // /
      // Parse actions
      // /
      if (VIEW_PERM_GRPS_ACTION.equalsIgnoreCase(action)) {
        //
        // View all groups
        //
        putAllPermGroups(velocityContext, loginVo);
        velocityTemplateFile = VIEW_PERM_GRPS_TEMPLATE;

      } // if VIEW_
      else if (DELETE_PERM_GRP_ACTION.equalsIgnoreCase(action)) {
        //
        // delete specified group
        //
        final PermissionGroupVO delGroup = new PermissionGroupVO();
        delGroup.setPermissionGroupName(reqGroupId);
        
        try 
        {
            permGrpHandler.delete(loginVo, delGroup);
            notificationToDisplay = "Permission group " + reqGroupId + " is deleted";
        } catch (final LDAPException ex) {
            velocityContext.put(SecurityAdminConstants.ERROR_MESSAGE_VAR,  ex.getErrorMessage() + " (group:"+reqGroupId+")" );
        }

        // reload view
        putAllPermGroups(velocityContext, loginVo);
        velocityTemplateFile = VIEW_PERM_GRPS_TEMPLATE;

        
      } // if EDIT_
      else if (addGroup || editGroup) {

        //
        // Add or Edit group
        //
          
        velocityContext.put(SecurityAdminConstants.ADD_MODE_VAR, new Boolean(addGroup) /* VM needs Boolean object*/ ) ;

        if (SecurityAdminConstants.YES.equals(servletRequest.getParameter(I_FORM_VALID))) {
          //
          // 'edit/add' form is submitted->modify LDAP
          //
          final PermissionGroupVO permGrp = createPermGroupFromRequest(servletRequest);

          // do stuff
          String op = null;
          String result = null;
          boolean success = false;

          try {
            if (addGroup) {
              permGrpHandler.create(loginVo, permGrp);
              op = "created";
            } else {
              permGrpHandler.modify (loginVo, permGrp );
              op = "modified";
            }
            success = true;
          } catch (final LDAPException ex) {
            result = ex.getErrorMessage();
          }

          if (success) {
            //
            // Op. is done with OK code->show info and modified users
            //
            putAllPermGroups(velocityContext, loginVo);
            notificationToDisplay = "Permission Group " + permGrp.getPermissionGroupName() + " is " + op;
            velocityTemplateFile = VIEW_PERM_GRPS_TEMPLATE;
          } else {
            //
            // Op. returns ERROR->reload edit page with error message
            //
            putAvailPermissions(velocityContext, loginVo);

            velocityContext.put(PERM_GRP_DATA_VAR, permGrp /*
                                                            * group with invalid
                                                            * data
                                                            */);
            velocityContext.put(SecurityAdminConstants.ERROR_MESSAGE_VAR,
                result + " (permission group:" + permGrp.getPermissionGroupName() + ")");
            velocityTemplateFile = EDIT_PERM_GRP_TEMPLATE;
          } // if
        } else {
          //
          // show velocity template for editing/adding group
          //
          putAvailPermissions(velocityContext, loginVo);

          if (editGroup) {
            // load group data if editing
            velocityContext.put(PERM_GRP_DATA_VAR, findGroup(reqGroupId, loginVo));
          } else {
            // new group
            velocityContext.put(PERM_GRP_DATA_VAR, createPermGroup());
          }

          velocityTemplateFile = EDIT_PERM_GRP_TEMPLATE;
        } // if

      } // add || edit
      else if (VIEW_PERM_GRP_PERM_GRPS_ACTION.equalsIgnoreCase(action)) {
        //
        // View permissions of given group
        //
        velocityContext.put(PERM_GRP_DATA_VAR, findGroup(reqGroupId, loginVo));
        velocityTemplateFile = VIEW_PERM_GRP_PERM_GRPS_TEMPLATE;

      } // VIEW_

      //velocityContext.put("_debug", _debug);
      velocityContext.put(SecurityAdminConstants.NOTIFICATION_VAR, notificationToDisplay);
      velocityTemplate = getTemplate(velocityTemplateFile);

    }catch(LDAPException Le){
    
    String Invalid_Credentials = "Invalid Credentials";	
    String error_mess = Le.getMessage();
    log.error("LDAP getErrorMessage :"+ Le.getMessage());
    
    if (error_mess.contains(Invalid_Credentials)){	
    try{
    	session.invalidate();
    	// remove user from the context - 
    	velocityContext.remove(SecurityAdminConstants.LOGGED_USER_NAME_VAR);
    	notificationToDisplay = "account locked. Please contact the system administrator.";
    	velocityTemplateFile = LOGOUT_TEMPLATE;
    	velocityContext.put(SecurityAdminConstants.NOTIFICATION_VAR, notificationToDisplay);
    	velocityTemplate = getTemplate(velocityTemplateFile);
    	log.error(Le);
      	} catch (final ResourceNotFoundException e) {
        log.error("Resource Not Found", e);
        throw e;
      	} catch (final ParseErrorException e) { 
        log.error("Parse error", e);
        throw e;
      	} 
      	catch (final SQLException e) {
          log.error("SQL exception", e);
          throw e;
        }
      	catch (final Exception e) { // catch all
        log.error("Exception handling request ", e);
        throw e;
      }
      	}
  }    
	catch (final Throwable tr) {

      try {

    	//20111206 EANGUAN:: TR HO96578 :: Logout if account locked using EventsUi
    	  if(tr != null && tr.getMessage() != null && tr.getMessage().contains("error code 49")){
    		  // User account gets locked
              session.invalidate();
              // remove user from the context -
              // so that it wont be shown in the screen
              velocityContext.remove(SecurityAdminConstants.LOGGED_USER_NAME_VAR);
              notificationToDisplay = "User credentials are not correct or account gets locked. Please contact admin.";
              velocityContext.put(SecurityAdminConstants.NOTIFICATION_VAR, notificationToDisplay);
              log.error(tr);
              velocityTemplate = getTemplate(EtlguiServlet.LOGOUT_TEMPLATE);
    	  }else{
    		  String errorMsg = null ;
    		  if(tr != null){
    			  errorMsg = tr.getLocalizedMessage();
    		  }
    		  velocityContext.put("errorSet", true);
    		  if(errorMsg != null && errorMsg.length() > 0){
    			  velocityContext.put("errorText", errorMsg);
    		  }else{
    			  velocityContext.put("errorText", "Unkown Exception occured: ");
    		  }
    		  log.error(tr);
    		  //get error page template
    		  velocityTemplate = getTemplate(EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU);
    	  }
      } catch (final ResourceNotFoundException e) {
        log.error("Resource Not Found", e);
        throw e;
      } catch (final ParseErrorException e) {
        log.error("Parse error", e);
        throw e;
      } catch (final SQLException e) {
        log.error("SQL exception", e);
      } catch (final Exception e) {
        log.error("Exception handling request ", e);
      }

    } // catch

    return velocityTemplate;

  } // func

  /**
   * Puts all available permissions into context
   *
   * @param velocityContext
   * @throws LDAPException
   */
  protected void putAvailPermissions(final Context velocityContext, final LoginVO loginVo) throws LDAPException {
    final Set<IValueObject> availPermGrps = LDAPUtil.getAllPermissionsAsVOs(loginVo);

    velocityContext.put(AVAIL_PERMISSIONS_VAR, availPermGrps /* temporal */);

    //
    // create javascript array of permission names and descriptions
    //
    final Iterator<IValueObject> it = availPermGrps.iterator();

    final StringBuilder jsArrayCode = new StringBuilder(200);
    jsArrayCode.append("var arr = new Array(); \n ");

    while ( it.hasNext() ) {
        final PermissionVO perm = (PermissionVO)it.next();        // = arr ["perm_name"] = "perm_desc";

        jsArrayCode.append(" arr[\"" );
        jsArrayCode.append( perm.getPermissionName() );
        jsArrayCode.append("\"] = \"" );
        jsArrayCode.append( perm.getDescription() );
        jsArrayCode.append("\"; \n" );

    }// while

    velocityContext.put(AVAIL_PERMISSIONS_JS_ARR_VAR, jsArrayCode.toString() );
  }// func


  /**
   * Puts all groups into context
   *
   * @param velocityContext
   * @param loginVo
   */
  protected void putAllPermGroups(final Context velocityContext, final LoginVO loginVo) throws LDAPException {

    final List<IValueObject> groups = permGrpHandler.findAll(loginVo);

    velocityContext.put(ALL_PERM_GRPS_VAR, groups);
  }

  /**
   * Finds specified group and replaces NULLs with empty strings
   *
   */
  private PermissionGroupVO findGroup(final String groupId, final LoginVO loginVo) throws LDAPException {

    final PermissionGroupVO reqGroup = new PermissionGroupVO(); // group to find
    reqGroup.setPermissionGroupName(groupId);

    return (PermissionGroupVO) permGrpHandler.findById(loginVo, reqGroup);
  } // func

  /**
   * Creates permission group from http request /FORM/
   *
   * @param servletRequest
   * @return
   */
  private PermissionGroupVO createPermGroup() {
    final PermissionGroupVO group = new PermissionGroupVO();

    group.setPermissionGroupName(LDAPConstants.EMPTY_STRING);
    group.setTitle(LDAPConstants.EMPTY_STRING);
    group.setDescription(LDAPConstants.EMPTY_STRING);
    group.setPermissions(new HashSet<String>());
    group.setPredefined(false);

    return group;
  } // func

  /**
   * Creates permission group from http request /FORM/
   *
   * @param servletRequest
   * @return
   */
  private PermissionGroupVO createPermGroupFromRequest(final HttpServletRequest servletRequest) {

    final PermissionGroupVO group = new PermissionGroupVO();

    group.setPermissionGroupName(SecurityAdminHelper.getReqParam(servletRequest, I_PERM_GRP_NAME));
    group.setTitle(SecurityAdminHelper.getReqParam(servletRequest, I_PERM_GRP_TITLE));
    group.setDescription(SecurityAdminHelper.getReqParam(servletRequest, I_PERM_GRP_DESC));

    final String[] grpPermissions = servletRequest.getParameterValues(I_PERM_GRP_PERM_GRPS);
    final Set<String> assignedPermissions = new HashSet<String>(Arrays.asList(grpPermissions));
    group.setPermissions(assignedPermissions);

    group.setPredefined(false);

    return group;
  } // func


}// class
