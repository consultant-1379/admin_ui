package com.distocraft.dc5000.etl.gui.rolemanagement;


import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.distocraft.dc5000.etl.gui.common.Environment;
import com.distocraft.dc5000.etl.gui.common.EnvironmentEvents;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.SecurityAdminConstants;
import com.distocraft.dc5000.etl.gui.login.LoginForm;
import com.ericsson.eniq.ldap.handler.RoleHandler;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.RoleVO;
import com.sun.security.auth.UserPrincipal;





@RunWith(JMock.class)
public class RoleManagementTest   {

    private Mockery context;

    
    @Before
    public void setUp() {
        context = new JUnit4Mockery();
        context.setImposteriser(ClassImposteriser.INSTANCE);        
    }    
    
    
    /**
     * Test case : view all roles -> show all roles template by calling a mocked roleHandler.findAll function
     */
    @Test
    public void testHandleRequest_ViewAllRoles() throws Exception {
    
        final HttpServletRequest servletRequest     = context.mock(HttpServletRequest.class);
        final Context            velocityContext    = context.mock(Context.class);
        final HttpSession        mockedSession      = context.mock(HttpSession.class);        
        final RoleHandler        mockedRoleHandler  = context.mock(RoleHandler.class);
        
        final StubbedRoleManagement stubbedRoleManagement= new StubbedRoleManagement();
        stubbedRoleManagement.setRoleHandler(mockedRoleHandler);
        
        //
        // login data
        //
        final LoginForm loginForm = new LoginForm();
        loginForm.setUserName("admin");
        loginForm.setUserPassword("admin");
        
        context.checking( new Expectations() {
                {
                    one(velocityContext).put( with(SecurityAdminConstants.NOTIFICATION_VAR), with(any(String.class)) ) ;
                    
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "rid") ;
                    will(returnValue("sysadmin"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue(null) );    // return 'null'->sets default action to 'view'->allows more code coverage

                    one(mockedRoleHandler).findAll(with(any(LoginVO.class)));
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    allowing(mockedSession).getAttribute("environment");
                    will(returnValue(new EnvironmentEvents()));
                    
                    one(mockedSession).setMaxInactiveInterval(10800);
                    
                    one(mockedSession).getAttribute("etlRepUrl");
                    will(returnValue("jdbc:hsqldb:mem:etlrep"));
                    
                    one(mockedSession).getAttribute("etlRepUsername");
                    will(returnValue("SA"));
                    
                    one(mockedSession).getAttribute("etlRepPassword");
                    will(returnValue(""));

                    one(mockedSession).getAttribute("etlRepDrivername");
                    will(returnValue("org.hsqldb.jdbcDriver"));

                    one(mockedSession).getAttribute("rockEtlRep");
                    will(returnValue("org.hsqldb.jdbcDriver"));
                } } );        
        
        
        final HttpServletResponse servletResponse = null;
        
        final Template  result = stubbedRoleManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_view_roles.vm! VM="+result.getName(), "aac_view_roles.vm".equals ( result.getName() ) );        
        
    }// func

    
    
    /**
     * Test case : add role 1. -> show edit template
     */
    @Test
    public void testHandleRequest_AddRoleShowEditPage() throws Exception {
    
        final HttpServletRequest        servletRequest     = context.mock(HttpServletRequest.class);
        final Context                   velocityContext    = context.mock(Context.class);
        final HttpSession               mockedSession      = context.mock(HttpSession.class);        
        final RoleHandler               mockedRoleHandler  = context.mock(RoleHandler.class);

        
        final StubbedRoleManagement stubbedRoleManagement= new StubbedRoleManagement();
        stubbedRoleManagement.setRoleHandler(mockedRoleHandler);

        
        //
        // login data
        //
        final LoginForm loginForm = new LoginForm();
        loginForm.setUserName("admin");
        loginForm.setUserPassword("admin");
        
        context.checking( new Expectations() {
                {
                    one(velocityContext).put( with("roleData"), with(any(RoleVO.class)) ) ;
                    
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "rid") ;
                    will(returnValue("sysadmin"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("addRole") );

                    one(servletRequest).getParameter("formValid");
                    will(returnValue("") );

                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));

                    allowing(mockedSession).getAttribute("environment");
                    will(returnValue(new EnvironmentEvents()));
                    
                    one(mockedSession).setMaxInactiveInterval(10800);
                    
                    one(mockedSession).getAttribute("etlRepUrl");
                    will(returnValue("jdbc:hsqldb:mem:etlrep"));
                    
                    one(mockedSession).getAttribute("etlRepUsername");
                    will(returnValue("SA"));
                    
                    one(mockedSession).getAttribute("etlRepPassword");
                    will(returnValue(""));

                    one(mockedSession).getAttribute("etlRepDrivername");
                    will(returnValue("org.hsqldb.jdbcDriver"));

                    one(mockedSession).getAttribute("rockEtlRep");
                    will(returnValue("org.hsqldb.jdbcDriver"));
                } } );        
        
        
        final HttpServletResponse servletResponse = null;
        
        final Template  result = stubbedRoleManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_edit_role.vm! VM="+result.getName(), "aac_edit_role.vm".equals ( result.getName() ) );                
        
    }// func

    
    /**
     * Test case : add role 2. -> edit template is shown and SAVE is pressed; will use mocked form data
     */
    @Test
    public void testHandleRequest_AddRoleSaveForm() throws Exception {

        final HttpServletRequest servletRequest     = context.mock(HttpServletRequest.class);
        final Context            velocityContext    = context.mock(Context.class);
        final HttpSession        mockedSession      = context.mock(HttpSession.class);        
        final RoleHandler        mockedRoleHandler  = context.mock(RoleHandler.class);
        
        final StubbedRoleManagement stubbedRoleManagement= new StubbedRoleManagement();
        stubbedRoleManagement.setRoleHandler(mockedRoleHandler);
        
        //
        // login data
        //
        final LoginForm loginForm = new LoginForm();
        loginForm.setUserName("admin");
        loginForm.setUserPassword("admin");
        
        context.checking( new Expectations() {
                {
                    // NEVER expect error !
                    never(velocityContext).put( with(SecurityAdminConstants.ERROR_MESSAGE_VAR), with(any(String.class)) ) ;
                    
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "rid") ;
                    will(returnValue("sysadmin"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("addRole") );

                    one(servletRequest).getParameter("formValid");
                    will(returnValue("yes") );
                    
                    //==== FORM DATA:START
                    one(servletRequest).getParameter( "roleName") ;
                    will(returnValue( "_roleName" ) );
                    
                    one(servletRequest).getParameter( "roleDesc") ;
                    will(returnValue( "_roleDesc" ) );
                    
                    one(servletRequest).getParameter( "roleTitle") ;
                    will(returnValue( "_roleTitle" ) );

                    one(servletRequest).getParameterValues( "rolePermGroups") ;
                    will(returnValue( new String[]{"dummy"}) );
                    //==== FORM DATA:END                    
                    
                    one(mockedRoleHandler).create(with(any(LoginVO.class)), with(any(RoleVO.class)));                    
                    
                    one(mockedRoleHandler).findAll(with(any(LoginVO.class)));   
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));

                    allowing(mockedSession).getAttribute("environment");
                    will(returnValue(new EnvironmentEvents()));
                    
                    one(mockedSession).setMaxInactiveInterval(10800);
                    
                    one(mockedSession).getAttribute("etlRepUrl");
                    will(returnValue("jdbc:hsqldb:mem:etlrep"));
                    
                    one(mockedSession).getAttribute("etlRepUsername");
                    will(returnValue("SA"));
                    
                    one(mockedSession).getAttribute("etlRepPassword");
                    will(returnValue(""));

                    one(mockedSession).getAttribute("etlRepDrivername");
                    will(returnValue("org.hsqldb.jdbcDriver"));

                    one(mockedSession).getAttribute("rockEtlRep");
                    will(returnValue("org.hsqldb.jdbcDriver"));
                } } );        
        
        
        final HttpServletResponse servletResponse = null;
        
        final Template  result = stubbedRoleManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_view_roles.vm! VM="+result.getName(), "aac_view_roles.vm".equals ( result.getName() ) );                
    }    

    
    /**
     * Test case : add role 3. -> edit template is shown and SAVE is pressed with a duplicated role
     */
    @Test
    public void testHandleRequest_AddRoleSaveFormDuplicatedRole() throws Exception {
        
        final HttpServletRequest servletRequest     = context.mock(HttpServletRequest.class);
        final Context            velocityContext    = context.mock(Context.class);
        final HttpSession        mockedSession      = context.mock(HttpSession.class);        
        final RoleHandler        mockedRoleHandler  = context.mock(RoleHandler.class);
        
        final StubbedRoleManagement stubbedRoleManagement= new StubbedRoleManagement();
        stubbedRoleManagement.setRoleHandler(mockedRoleHandler);
        
        //
        // login data
        //
        final LoginForm loginForm = new LoginForm();
        loginForm.setUserName("admin");
        loginForm.setUserPassword("admin");
        
        context.checking( new Expectations() {
                {
                    // expect error !
                    one(velocityContext).put( with(SecurityAdminConstants.ERROR_MESSAGE_VAR), with(any(String.class)) ) ;
                    
                    one(velocityContext).put( with("roleData"), with(any(RoleVO.class)) ) ;
                    
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "rid") ;
                    will(returnValue("sysadmin"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("addRole") );

                    one(servletRequest).getParameter("formValid");
                    will(returnValue("yes") );
                    
                    //==== FORM DATA:START
                    one(servletRequest).getParameter( "roleName") ;
                    will(returnValue( "_roleName" ) );
                    
                    one(servletRequest).getParameter( "roleDesc") ;
                    will(returnValue( "_roleDesc" ) );
                    
                    one(servletRequest).getParameter( "roleTitle") ;
                    will(returnValue( "_roleTitle" ) );

                    one(servletRequest).getParameterValues( "rolePermGroups") ;
                    will(returnValue( new String[]{"dummy"}) );
                    //==== FORM DATA:END                    
                    
                    one(mockedRoleHandler).create(with(any(LoginVO.class)), with(any(RoleVO.class)));                    
                    will( throwException( new LDAPException("Role already exists")) );
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    allowing(mockedSession).getAttribute("environment");
                    will(returnValue(new EnvironmentEvents()));
                    
                    one(mockedSession).setMaxInactiveInterval(10800);
                    
                    one(mockedSession).getAttribute("etlRepUrl");
                    will(returnValue("jdbc:hsqldb:mem:etlrep"));
                    
                    one(mockedSession).getAttribute("etlRepUsername");
                    will(returnValue("SA"));
                    
                    one(mockedSession).getAttribute("etlRepPassword");
                    will(returnValue(""));

                    one(mockedSession).getAttribute("etlRepDrivername");
                    will(returnValue("org.hsqldb.jdbcDriver"));

                    one(mockedSession).getAttribute("rockEtlRep");
                    will(returnValue("org.hsqldb.jdbcDriver"));
                } } );        
        
        
        final HttpServletResponse servletResponse = null;
        
        final Template  result = stubbedRoleManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_edit_role.vm! VM="+result.getName(), "aac_edit_role.vm".equals ( result.getName() ) );                
    }     // func

    
    /**
     * Test case : edit role -> show edit template
     */
    @Test
    public void testHandleRequest_EditRoleShowEditPage() throws Exception {
    
        final HttpServletRequest        servletRequest     = context.mock(HttpServletRequest.class);
        final Context                   velocityContext    = context.mock(Context.class);
        final HttpSession               mockedSession      = context.mock(HttpSession.class);        
        final RoleHandler               mockedRoleHandler  = context.mock(RoleHandler.class);

        
        final StubbedRoleManagement stubbedRoleManagement= new StubbedRoleManagement();
        stubbedRoleManagement.setRoleHandler(mockedRoleHandler);

        
        //
        // login data
        //
        final LoginForm loginForm = new LoginForm();
        loginForm.setUserName("admin");
        loginForm.setUserPassword("admin");
        
        context.checking( new Expectations() {
                {
                    one(velocityContext).put( with("roleData"), with(any(RoleVO.class)) ) ;
                    
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "rid") ;
                    will(returnValue("sysadmin"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("editRole") );

                    one(servletRequest).getParameter("formValid");
                    will(returnValue("") );
                    
                    one(mockedRoleHandler).findById(with(any(LoginVO.class)), with(any(RoleVO.class)));   
                    will(returnValue(new RoleVO()) );
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    allowing(mockedSession).getAttribute("environment");
                    will(returnValue(new EnvironmentEvents()));
                    
                    one(mockedSession).setMaxInactiveInterval(10800);
                    
                    one(mockedSession).getAttribute("etlRepUrl");
                    will(returnValue("jdbc:hsqldb:mem:etlrep"));
                    
                    one(mockedSession).getAttribute("etlRepUsername");
                    will(returnValue("SA"));
                    
                    one(mockedSession).getAttribute("etlRepPassword");
                    will(returnValue(""));

                    one(mockedSession).getAttribute("etlRepDrivername");
                    will(returnValue("org.hsqldb.jdbcDriver"));

                    one(mockedSession).getAttribute("rockEtlRep");
                    will(returnValue("org.hsqldb.jdbcDriver"));
                } } );        
        
        
        final HttpServletResponse servletResponse = null;
        
        final Template  result = stubbedRoleManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_edit_role.vm! VM="+result.getName(), "aac_edit_role.vm".equals ( result.getName() ) );                
        
    }// func
    
    
    /**
     * Test case : edit template is shown and SAVE is pressed; will use mocked form data
     */
    @Test
    public void testHandleRequest_EditRoleSaveForm() throws Exception {

        final HttpServletRequest servletRequest     = context.mock(HttpServletRequest.class);
        final Context            velocityContext    = context.mock(Context.class);
        final HttpSession        mockedSession      = context.mock(HttpSession.class);        
        final RoleHandler        mockedRoleHandler  = context.mock(RoleHandler.class);
        
        final StubbedRoleManagement stubbedRoleManagement= new StubbedRoleManagement();
        stubbedRoleManagement.setRoleHandler(mockedRoleHandler);
        
        //
        // login data
        //
        final LoginForm loginForm = new LoginForm();
        loginForm.setUserName("admin");
        loginForm.setUserPassword("admin");
        
        context.checking( new Expectations() {
                {
                    // NEVER expect error !
                    never(velocityContext).put( with(SecurityAdminConstants.ERROR_MESSAGE_VAR), with(any(String.class)) ) ;
                    
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "rid") ;
                    will(returnValue("sysadmin"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("editRole") );

                    one(servletRequest).getParameter("formValid");
                    will(returnValue("yes") );
                    
                    //==== FORM DATA:START
                    one(servletRequest).getParameter( "roleName") ;
                    will(returnValue( "_roleName" ) );
                    
                    one(servletRequest).getParameter( "roleDesc") ;
                    will(returnValue( "_roleDesc" ) );
                    
                    one(servletRequest).getParameter( "roleTitle") ;
                    will(returnValue( "_roleTitle" ) );

                    one(servletRequest).getParameterValues( "rolePermGroups") ;
                    will(returnValue( new String[]{"dummy"}) );
                    //==== FORM DATA:END                    
                    
                    one(mockedRoleHandler).modify(with(any(LoginVO.class)), with(any(RoleVO.class)));                    
                    
                    one(mockedRoleHandler).findAll(with(any(LoginVO.class)));
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    allowing(mockedSession).getAttribute("environment");
                    will(returnValue(new EnvironmentEvents()));
                    
                    one(mockedSession).setMaxInactiveInterval(10800);
                    
                    one(mockedSession).getAttribute("etlRepUrl");
                    will(returnValue("jdbc:hsqldb:mem:etlrep"));
                    
                    one(mockedSession).getAttribute("etlRepUsername");
                    will(returnValue("SA"));
                    
                    one(mockedSession).getAttribute("etlRepPassword");
                    will(returnValue(""));

                    one(mockedSession).getAttribute("etlRepDrivername");
                    will(returnValue("org.hsqldb.jdbcDriver"));

                    one(mockedSession).getAttribute("rockEtlRep");
                    will(returnValue("org.hsqldb.jdbcDriver"));
                } } );        
        
        
        final HttpServletResponse servletResponse = null;
        
        final Template  result = stubbedRoleManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_view_roles.vm! VM="+result.getName(), "aac_view_roles.vm".equals ( result.getName() ) );                
    }    
    
    /**
     * Test case : 
     */
    @Test
    public void testHandleRequest_ViewRolePermGroups () throws Exception {
    
        final HttpServletRequest servletRequest     = context.mock(HttpServletRequest.class);
        final Context            velocityContext    = context.mock(Context.class);
        final HttpSession        mockedSession      = context.mock(HttpSession.class);        
        final RoleHandler        mockedRoleHandler  = context.mock(RoleHandler.class);
        
        final StubbedRoleManagement stubbedRoleManagement= new StubbedRoleManagement();
        stubbedRoleManagement.setRoleHandler(mockedRoleHandler);
        
        //
        // login data
        //
        final LoginForm loginForm = new LoginForm();
        loginForm.setUserName("admin");
        loginForm.setUserPassword("admin");
        
        context.checking( new Expectations() {
                {
                    one(velocityContext).put( with(SecurityAdminConstants.NOTIFICATION_VAR), with(any(String.class)) ) ;
                    
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "rid") ;
                    will(returnValue("sysadmin"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("viewRolePermGroups") );

                    one(mockedRoleHandler).findById(with(any(LoginVO.class)), with(any(RoleVO.class)));   
                    will(returnValue(new RoleVO()) );
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    allowing(mockedSession).getAttribute("environment");
                    will(returnValue(new EnvironmentEvents()));
                    
                    one(mockedSession).setMaxInactiveInterval(10800);
                    
                    one(mockedSession).getAttribute("etlRepUrl");
                    will(returnValue("jdbc:hsqldb:mem:etlrep"));
                    
                    one(mockedSession).getAttribute("etlRepUsername");
                    will(returnValue("SA"));
                    
                    one(mockedSession).getAttribute("etlRepPassword");
                    will(returnValue(""));

                    one(mockedSession).getAttribute("etlRepDrivername");
                    will(returnValue("org.hsqldb.jdbcDriver"));

                    one(mockedSession).getAttribute("rockEtlRep");
                    will(returnValue("org.hsqldb.jdbcDriver"));
                } } );        
        
        
        final HttpServletResponse servletResponse = null;
        
        final Template  result = stubbedRoleManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_view_role_perm_groups.vm! VM="+result.getName(), "aac_view_role_perm_groups.vm".equals ( result.getName() ) );        
    }// func
    

    /**
     * Test case : Delete role
     */
    @Test
    public void testHandleRequest_DeleteRole () throws Exception {

        final HttpServletRequest servletRequest     = context.mock(HttpServletRequest.class);
        final Context            velocityContext    = context.mock(Context.class);
        final HttpSession        mockedSession      = context.mock(HttpSession.class);        
        final RoleHandler        mockedRoleHandler  = context.mock(RoleHandler.class);
        
        final StubbedRoleManagement stubbedRoleManagement= new StubbedRoleManagement();
        stubbedRoleManagement.setRoleHandler(mockedRoleHandler);
        
        //
        // login data
        //
        final LoginForm loginForm = new LoginForm();
        loginForm.setUserName("admin");
        loginForm.setUserPassword("admin");
        
        context.checking( new Expectations() {
                {
                    one(velocityContext).put( with(SecurityAdminConstants.NOTIFICATION_VAR), with(any(String.class)) ) ;
                    
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "rid") ;
                    will(returnValue("sysadmin"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("deleteRole") );

                    one(mockedRoleHandler).delete ( with(any(LoginVO.class)), with(any(LoginVO.class)));   
                    
                    one(mockedRoleHandler).findAll(with(any(LoginVO.class)));
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    allowing(mockedSession).getAttribute("environment");
                    will(returnValue(new EnvironmentEvents()));
                    
                    one(mockedSession).setMaxInactiveInterval(10800);
                    
                    one(mockedSession).getAttribute("etlRepUrl");
                    will(returnValue("jdbc:hsqldb:mem:etlrep"));
                    
                    one(mockedSession).getAttribute("etlRepUsername");
                    will(returnValue("SA"));
                    
                    one(mockedSession).getAttribute("etlRepPassword");
                    will(returnValue(""));

                    one(mockedSession).getAttribute("etlRepDrivername");
                    will(returnValue("org.hsqldb.jdbcDriver"));

                    one(mockedSession).getAttribute("rockEtlRep");
                    will(returnValue("org.hsqldb.jdbcDriver"));
                } } );        
        
        
        final HttpServletResponse servletResponse = null;
        
        final Template  result = stubbedRoleManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_view_roles.vm! VM="+result.getName(), "aac_view_roles.vm".equals ( result.getName() ) );        
        
    }// func

    
    /**
     * Test case : Delete role with error
     */
    @Test
    public void testHandleRequest_DeleteRoleError () throws Exception {

        final HttpServletRequest servletRequest     = context.mock(HttpServletRequest.class);
        final Context            velocityContext    = context.mock(Context.class);
        final HttpSession        mockedSession      = context.mock(HttpSession.class);        
        final RoleHandler        mockedRoleHandler  = context.mock(RoleHandler.class);
        
        final StubbedRoleManagement stubbedRoleManagement= new StubbedRoleManagement();
        stubbedRoleManagement.setRoleHandler(mockedRoleHandler);
        
        //
        // login data
        //
        final LoginForm loginForm = new LoginForm();
        loginForm.setUserName("admin");
        loginForm.setUserPassword("admin");
        
        context.checking( new Expectations() {
                {
                    // expect error !
                    one(velocityContext).put( with(SecurityAdminConstants.ERROR_MESSAGE_VAR), with(any(String.class)) ) ;
                    
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "rid") ;
                    will(returnValue("sysadmin"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("deleteRole") );

                    one(mockedRoleHandler).delete ( with(any(LoginVO.class)), with(any(LoginVO.class)));   
                    will( throwException( new LDAPException("Expected Exception")) );
                                        
                    one(mockedRoleHandler).findAll(with(any(LoginVO.class)));   

                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    allowing(mockedSession).getAttribute("environment");
                    will(returnValue(new EnvironmentEvents()));
                    
                    one(mockedSession).setMaxInactiveInterval(10800);
                    
                    one(mockedSession).getAttribute("etlRepUrl");
                    will(returnValue("jdbc:hsqldb:mem:etlrep"));
                    
                    one(mockedSession).getAttribute("etlRepUsername");
                    will(returnValue("SA"));
                    
                    one(mockedSession).getAttribute("etlRepPassword");
                    will(returnValue(""));

                    one(mockedSession).getAttribute("etlRepDrivername");
                    will(returnValue("org.hsqldb.jdbcDriver"));

                    one(mockedSession).getAttribute("rockEtlRep");
                    will(returnValue("org.hsqldb.jdbcDriver"));
                } } );        
        
        
        final HttpServletResponse servletResponse = null;
        
        final Template  result = stubbedRoleManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_view_roles.vm! VM="+result.getName(), "aac_view_roles.vm".equals ( result.getName() ) );        
        
    }// func

    
    /**
     * Test case : 
     */
    @Test
    public void testHandleRequest_FatalError () throws Exception {
    
        final HttpServletRequest servletRequest     = context.mock(HttpServletRequest.class);
        final Context            velocityContext    = context.mock(Context.class);
        final HttpSession        mockedSession      = context.mock(HttpSession.class);        
        final RoleHandler        mockedRoleHandler  = context.mock(RoleHandler.class);
        
        final StubbedRoleManagement stubbedRoleManagement= new StubbedRoleManagement();
        stubbedRoleManagement.setRoleHandler(mockedRoleHandler);
        
        //
        // login data
        //
        final LoginForm loginForm = new LoginForm();
        loginForm.setUserName("admin");
        loginForm.setUserPassword("admin");
        
        context.checking( new Expectations() {
                {
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "rid") ;
                    will(returnValue("sysadmin"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(null));
                    
                    //one(velocityContext).put( with("tr"), with(any(String.class)) ) ;
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    one(velocityContext).put( with("theuser"), with(any(String.class)) ) ;                    
                    
                    one(velocityContext).put( with("currenttime"), with(any(String.class)) ) ;                    
                    
                    one(velocityContext).put( with("environment"), with(any(Environment.class)) ) ;                    

                    one(velocityContext).put( with("errorSet"), with(any(Boolean.class)) ) ;
                    one(velocityContext).put( with("errorText"), with(any(String.class)) ) ;
                    
                    allowing(mockedSession).getAttribute("environment");
                    will(returnValue(new EnvironmentEvents()));
                    
                    one(mockedSession).setMaxInactiveInterval(10800);
                    
                    one(mockedSession).getAttribute("etlRepUrl");
                    will(returnValue("jdbc:hsqldb:mem:etlrep"));
                    
                    one(mockedSession).getAttribute("etlRepUsername");
                    will(returnValue("SA"));
                    
                    one(mockedSession).getAttribute("etlRepPassword");
                    will(returnValue(""));

                    one(mockedSession).getAttribute("etlRepDrivername");
                    will(returnValue("org.hsqldb.jdbcDriver"));

                    one(mockedSession).getAttribute("rockEtlRep");
                    will(returnValue("org.hsqldb.jdbcDriver"));
                } } );        
        
        
        final HttpServletResponse servletResponse = null;
        
        System.out.println("[RoleManagementTest:testHandleRequest_FatalError] testing for fatal error...");
        final Template  result = stubbedRoleManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not "+EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU, EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU.equals ( result.getName() ) );        
        
    }// func
    
    
    @SuppressWarnings("serial")
    private class StubbedRoleManagement extends RoleManagement {
        
        @Override
        public Template getTemplate(final String name)
                        throws ResourceNotFoundException, ParseErrorException,
                        Exception {
            // dummy: return a template whose name we can check against later
            final Template template = new Template();
            template.setName(name);
            return template;
        }
        

        @Override
        protected void putAvailPermGroups(final Context velocityContext, final LoginVO loginVo) throws LDAPException {
            // do nothing; only prevent call to LDAP
        }
        
        public void setRoleHandler (final RoleHandler handler ) throws Exception {
            //We use reflection to access final field
            final Field field = getClass().getSuperclass().getDeclaredField("roleHandler");
            field.setAccessible(true);
            field.set(this, handler);
        }

        
    }//class
    
}//class
