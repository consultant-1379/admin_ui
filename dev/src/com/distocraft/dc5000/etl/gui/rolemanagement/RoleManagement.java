/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.distocraft.dc5000.etl.gui.rolemanagement;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
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
import com.distocraft.dc5000.etl.gui.common.GuiServlet;
import com.distocraft.dc5000.etl.gui.common.SecurityAdminConstants;
import com.distocraft.dc5000.etl.gui.common.SecurityAdminHelper;
import com.distocraft.dc5000.etl.gui.login.LoginForm;
import com.ericsson.eniq.ldap.handler.IHandler;
import com.ericsson.eniq.ldap.handler.PermissionGroupHandler;
import com.ericsson.eniq.ldap.handler.RoleHandler;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.util.LDAPConstants;
import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.RoleVO;


/**
 *
 *
 *
 */

public class RoleManagement extends EtlguiServlet {
 

    private static final long serialVersionUID = 1L;

    ///
    //  Request parameters
    ///
    private static final String ROLE_ID_PARAM       = "rid";            // for edit/delete/etc. | rid=role name

    ///
    //  Actions verbs
    ///
    private static final String VIEW_ROLES_ACTION           = "viewRoles";
    private static final String ADD_ROLE_ACTION             = "addRole";
    private static final String EDIT_ROLE_ACTION            = "editRole";
    private static final String DELETE_ROLE_ACTION          = "deleteRole";
    private static final String VIEW_ROLE_PERM_GRPS_ACTION  = "viewRolePermGroups";

    ///
    //  VM files for action verbs
    ///
    private static final String VIEW_ROLES_TEMPLATE         = "aac_view_roles.vm";
    private static final String EDIT_ROLE_TEMPLATE          = "aac_edit_role.vm";
    private static final String VIEW_ROLE_PERM_GRPS_TEMPLATE= "aac_view_role_perm_groups.vm";
    private static final String LOGOUT_TEMPLATE = "logout.vm";

    ///
    //  VM context variables
    ///
    private static final String ALL_ROLES_VAR           = "allRoles";
    private static final String ROLE_DATA_VAR           = "roleData";
    private static final String AVAIL_PERM_GROUPS_VAR   = "availPermGroups";

    ///
    //  HTML Form IDs
    ///
    private static final String I_ROLE_NAME      = "roleName";
    private static final String I_ROLE_TITLE     = "roleTitle";
    private static final String I_ROLE_DESC      = "roleDesc";
    private static final String I_ROLE_PERM_GRPS = "rolePermGroups";
    private static final String I_FORM_VALID     = "formValid";  // if 'YES' set form data is valid in req.

    ///
    //  AUX
    ///
    private final Log           log             = LogFactory.getLog(this.getClass());
    protected final IHandler      roleHandler     = new RoleHandler();    // protected for testing



    @Override
    public Template doHandleRequest(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse,
                    final Context velocityContext) throws Exception {
    
        Template    velocityTemplate        = null ;
        String      velocityTemplateFile    = null;
        String      notificationToDisplay   = LDAPConstants.EMPTY_STRING;
        //String  _debug= LDAPConstants.EMPTY_STRING;
        HttpSession   session = null ;

        String   reqRoleId = null;
        try {

            session  = servletRequest.getSession();
            reqRoleId  = servletRequest.getParameter(ROLE_ID_PARAM);       // edit/delete/etc. or null

            ////
            // get logged user data
            ////
            final LoginForm     loginForm       = (LoginForm) session.getAttribute(SecurityAdminConstants.LOGIN_FORM_NAME);
            if (loginForm == null) {
                throw new Exception ("Session login details are null!");
            }

            final String        loggedUserName  = loginForm.getUserName();
            final String        loggedUserPwd   = loginForm.getUserPassword();

            // login object
            final LoginVO       loginVo     = new LoginVO();
            loginVo.setLoginId ( loggedUserName );
            loginVo.setPassword( loggedUserPwd );


            String action = servletRequest.getParameter (SecurityAdminConstants.ACTION_PARAM);
            if (action == null) {
                // default action
                action = VIEW_ROLES_ACTION;
            }


            final boolean addRole      = ADD_ROLE_ACTION.equalsIgnoreCase(action) ;
            final boolean editRole     = EDIT_ROLE_ACTION.equalsIgnoreCase(action) ;

            ///
            // Parse actions
            ///
            if (VIEW_ROLES_ACTION.equalsIgnoreCase(action)) {
                //
                // View all roles
                //
                putAllRoles(velocityContext, loginVo);
                velocityTemplateFile = VIEW_ROLES_TEMPLATE;

            } // if VIEW_ROLES_ACTION
            else if (DELETE_ROLE_ACTION.equalsIgnoreCase(action)) {
                //
                // delete selected role
                //
                final RoleVO delRole = new RoleVO();
                delRole.setRoleName(reqRoleId);

                try
                {
                    roleHandler.delete(loginVo, delRole);
                    notificationToDisplay = "Role " + reqRoleId + " is deleted";

                } catch (final LDAPException ex) {
                    velocityContext.put(SecurityAdminConstants.ERROR_MESSAGE_VAR, ex.getErrorMessage() + " (role:"+reqRoleId+")" );
                }

                // reload view
                putAllRoles(velocityContext, loginVo);
                velocityTemplateFile = VIEW_ROLES_TEMPLATE;
            } // if EDIT_ROLE_ACTION
            else if ( addRole  || editRole ) {

                //
                // Add or Edit role
                //

                velocityContext.put(SecurityAdminConstants.ADD_MODE_VAR, new Boolean(addRole) /* VM needs Boolean object*/ ) ;

                if ( SecurityAdminConstants.YES.equals( servletRequest.getParameter (I_FORM_VALID))  ) {
                    //
                    // 'edit/add' form is submitted->modify role in LDAP
                    //
                    final RoleVO role = createRoleFromRequest(servletRequest);

                    // do stuff
                    String op       = null;
                    String result   = null;
                    boolean success = false;

                    try {
                        if ( addRole ) {
                            roleHandler.create(loginVo, role );
                            op = "created";
                        }
                        else {
                            roleHandler.modify (loginVo, role);
                            op = "modified";
                        }
                        success = true;
                    } catch (LDAPException ex) {
                        result = ex.getErrorMessage();
                    }

                    if ( success ) {
                        //
                        // Op. is done with OK code->show info and modified users
                        //
                        putAllRoles(velocityContext, loginVo);
                        notificationToDisplay = "Role " + role.getRoleName() + " is "+op;
                        velocityTemplateFile  =  VIEW_ROLES_TEMPLATE;
                    } else {
                        //
                        // Op. returns ERROR->reload edit page with error message
                        //
                        putAvailPermGroups(velocityContext, loginVo);

                        velocityContext.put(ROLE_DATA_VAR, role /* role with invalid data*/ ) ;
                        velocityContext.put(SecurityAdminConstants.ERROR_MESSAGE_VAR, result + " (role:"+role.getRoleName()+")" );

                        velocityTemplateFile = EDIT_ROLE_TEMPLATE;
                    } // if

                }
                else {
                    //
                    // show velocity template for editing/adding role
                    //
                    putAvailPermGroups (velocityContext, loginVo);

                    if ( editRole ) {
                        // load role data if editing
                        velocityContext.put(ROLE_DATA_VAR, findRole (reqRoleId, loginVo) ) ;
                    } else {
                        // new role
                        velocityContext.put(ROLE_DATA_VAR, createRole() ) ;
                    }

                    velocityTemplateFile =  EDIT_ROLE_TEMPLATE;
                } // if

            } // add || edit
            else if (VIEW_ROLE_PERM_GRPS_ACTION.equalsIgnoreCase(action)) {
                //
                // View permission groups of given role
                //
                velocityContext.put(ROLE_DATA_VAR, findRole (reqRoleId, loginVo) ) ;
                velocityTemplateFile = VIEW_ROLE_PERM_GRPS_TEMPLATE;

            } // VIEW_ROLE_PERM_GRPS_ACTION


            //velocityContext.put("_debug", _debug);
            velocityContext.put(SecurityAdminConstants.NOTIFICATION_VAR, notificationToDisplay);
            velocityTemplate = getTemplate (velocityTemplateFile);

        }
		catch(LDAPException Le){

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
        } catch (final Throwable tr) {

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
     * Puts all available permission groups into context
     *
     * @param velocityContext
     * @throws LDAPException
     */
    protected void putAvailPermGroups(final Context velocityContext, final LoginVO loginVo) throws LDAPException {
        final IHandler   permGrpHandler        = new PermissionGroupHandler();
        final List<IValueObject> availPermGrps = permGrpHandler.findAll(loginVo);

        velocityContext.put(AVAIL_PERM_GROUPS_VAR, availPermGrps);
    }


    /**
     * Puts all roles into context
     *
     * @param velocityContext
     * @param loginVo
     */
    private void putAllRoles (final Context velocityContext, final LoginVO loginVo) throws LDAPException {

        final List<IValueObject> roles = roleHandler.findAll(loginVo);

        velocityContext.put(ALL_ROLES_VAR, roles);
    }


    /**
     * Finds specified role and replaces NULLs with empty strings
     *
     */
    private RoleVO findRole (final String roleId , final LoginVO loginVo) throws LDAPException {

        final RoleVO reqRole = new RoleVO();        // role to find
        reqRole.setRoleName(roleId);

        final RoleVO role    = (RoleVO) roleHandler.findById(loginVo, reqRole);

        return role;
    } // func



    /**
     * Creates role from http request /FORM/
     *
     * @param servletRequest
     * @return
     */
    private RoleVO createRoleFromRequest (final HttpServletRequest servletRequest ) {

        final RoleVO role = new RoleVO();
        role.setRoleName    (SecurityAdminHelper.getReqParam (servletRequest, I_ROLE_NAME));
        role.setTitle       (SecurityAdminHelper.getReqParam (servletRequest, I_ROLE_TITLE));
        role.setDescription (SecurityAdminHelper.getReqParam (servletRequest, I_ROLE_DESC));

        final String[]      rolePermGrps      = servletRequest.getParameterValues(I_ROLE_PERM_GRPS);
        final Set<String>   assignedPermGrps  = new HashSet<String> ( Arrays.asList(rolePermGrps) );
        role.setPermissionGroups(assignedPermGrps);

        role.setPredefined(false);

        return role;
    } // func


    /**
     * Creates new role
     */
    private RoleVO createRole () {
        final RoleVO role = new RoleVO();

        role.setRoleName    ( LDAPConstants.EMPTY_STRING );
        role.setTitle       ( LDAPConstants.EMPTY_STRING );
        role.setDescription ( LDAPConstants.EMPTY_STRING );
        role.setPermissionGroups( new HashSet<String>() );
        role.setPredefined(false);

        return role;
    } // func


} // class
