package com.distocraft.dc5000.etl.gui.permissiongroupmanagement;


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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.distocraft.dc5000.etl.gui.common.Environment;
import com.distocraft.dc5000.etl.gui.common.EnvironmentEvents;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.SecurityAdminConstants;
import com.distocraft.dc5000.etl.gui.login.LoginForm;
import com.ericsson.eniq.common.testutilities.RockDatabaseHelper;
import com.ericsson.eniq.ldap.handler.PermissionGroupHandler;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.PermissionGroupVO;
import com.sun.security.auth.UserPrincipal;



@RunWith(JMock.class)
public class PermissionGroupManagementTest   {

    private Mockery context;

    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
      RockDatabaseHelper.setUpBeforeClass();
    } 
    @Before
    public void setUp() throws Exception {
        context = new JUnit4Mockery();
        context.setImposteriser(ClassImposteriser.INSTANCE);        
    }    
    
    
    /**
     * Test case : 
     */
    @Test
    public void testHandleRequest_ViewAllPermGroups() throws Exception {
    
        final HttpServletRequest        servletRequest     = context.mock(HttpServletRequest.class);
        final Context                   velocityContext    = context.mock(Context.class);
        final HttpSession               mockedSession      = context.mock(HttpSession.class);        
        final PermissionGroupHandler    mockedPermGroupHandler  = context.mock(PermissionGroupHandler.class);
        
        final StubbedPermGrpManagement stubbedPermGrpManagement= new StubbedPermGrpManagement();
        stubbedPermGrpManagement.setGroupHandler(mockedPermGroupHandler);
        
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
                    
                    one(servletRequest).getParameter( "gid") ;
                    will(returnValue("allpermissions"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));
                    
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
                    
                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue(null) );    // return 'null'->sets default action to 'view'->allows more code coverage
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));

                    one(mockedPermGroupHandler).findAll(with(any(LoginVO.class)));
                    
                    allowing(mockedSession).getAttribute("environment");
                    will(returnValue(new EnvironmentEvents()));
                } } );        
        
        
        final HttpServletResponse servletResponse = null;
        
        final Template  result = stubbedPermGrpManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_view_permission_groups.vm! VM="+result.getName(), "aac_view_permission_groups.vm".equals ( result.getName() ) );        
        
    }// func

    
    
    /**
     * Test case : add perm. group 1. -> show edit template
     */
    @Test
    public void testHandleRequest_AddPermGroupShowEditPage() throws Exception {
    
        final HttpServletRequest        servletRequest     = context.mock(HttpServletRequest.class);
        final Context                   velocityContext    = context.mock(Context.class);
        final HttpSession               mockedSession      = context.mock(HttpSession.class);        
        final PermissionGroupHandler    mockedPermGroupHandler  = context.mock(PermissionGroupHandler.class);
//        final PermissionGroupHandler    mockedGroupHandler = context.mock(PermissionGroupHandler.class);        
        
        final StubbedPermGrpManagement stubbedPermGrpManagement= new StubbedPermGrpManagement();
        stubbedPermGrpManagement.setGroupHandler(mockedPermGroupHandler);

        
        //
        // login data
        //
        final LoginForm loginForm = new LoginForm();
        loginForm.setUserName("admin");
        loginForm.setUserPassword("admin");
        
        context.checking( new Expectations() {
                {
                    one(velocityContext).put( with("permGroupData"), with(any(PermissionGroupVO.class)) ) ;
                    
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "gid") ;
                    will(returnValue("allpermissions"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("addPermissionGroup") );

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
        
        final Template  result = stubbedPermGrpManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_edit_permission_group.vm! VM="+result.getName(), "aac_edit_permission_group.vm".equals ( result.getName() ) );                
        
    }// func

    
    /**
     * Test case : add perm. group 2. -> edit template is shown and SAVE is pressed; will use mocked form data
     */
    @Test
    public void testHandleRequest_AddPermGroupSaveForm() throws Exception {

        final HttpServletRequest servletRequest     = context.mock(HttpServletRequest.class);
        final Context            velocityContext    = context.mock(Context.class);
        final HttpSession        mockedSession      = context.mock(HttpSession.class);        
        final PermissionGroupHandler    mockedPermGroupHandler  = context.mock(PermissionGroupHandler.class);
        
        final StubbedPermGrpManagement stubbedPermGrpManagement= new StubbedPermGrpManagement();
        stubbedPermGrpManagement.setGroupHandler(mockedPermGroupHandler);
        
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
                    
                    one(servletRequest).getParameter( "gid") ;
                    will(returnValue("allpermissions"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("addPermissionGroup") );

                    one(servletRequest).getParameter("formValid");
                    will(returnValue("yes") );
                    
                    //==== FORM DATA:START
                    one(servletRequest).getParameter( "permGroupName") ;
                    will(returnValue( "_permGroupName" ) );
                    
                    one(servletRequest).getParameter( "permGroupTitle") ;
                    will(returnValue( "_permGroupTitle" ) );
                    
                    one(servletRequest).getParameter( "permGroupDescription") ;
                    will(returnValue( "_permGroupDescription" ) );

                    one(servletRequest).getParameterValues( "permissions") ;
                    will(returnValue( new String[]{"dummy"}) );
                    //==== FORM DATA:END                    
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    one(mockedPermGroupHandler).create(with(any(LoginVO.class)), with(any(PermissionGroupVO.class)));                    
                    
                    one(mockedPermGroupHandler).findAll(with(any(LoginVO.class)));   
                    
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
        
        final Template  result = stubbedPermGrpManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_view_permission_groups.vm! VM="+result.getName(), "aac_view_permission_groups.vm".equals ( result.getName() ) );        
    }    

    
    /**
     * Test case : add perm. group 3. -> edit template is shown and SAVE is pressed with a duplicated item
     */
    @Test
    public void testHandleRequest_AddPermGroupSaveFormDuplicatedPermGroup() throws Exception {
        
        final HttpServletRequest servletRequest     = context.mock(HttpServletRequest.class);
        final Context            velocityContext    = context.mock(Context.class);
        final HttpSession        mockedSession      = context.mock(HttpSession.class);        
        final PermissionGroupHandler    mockedPermGroupHandler  = context.mock(PermissionGroupHandler.class);
        
        final StubbedPermGrpManagement stubbedPermGrpManagement= new StubbedPermGrpManagement();
        stubbedPermGrpManagement.setGroupHandler(mockedPermGroupHandler);
        
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
                    
                    one(velocityContext).put( with("permGroupData"), with(any(PermissionGroupVO.class)) ) ;
                    
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "gid") ;
                    will(returnValue("allpermissions"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("addPermissionGroup") );

                    one(servletRequest).getParameter("formValid");
                    will(returnValue("yes") );
                    
                    //==== FORM DATA:START
                    one(servletRequest).getParameter( "permGroupName") ;
                    will(returnValue( "_permGroupName" ) );
                    
                    one(servletRequest).getParameter( "permGroupTitle") ;
                    will(returnValue( "_permGroupTitle" ) );
                    
                    one(servletRequest).getParameter( "permGroupDescription") ;
                    will(returnValue( "_permGroupDescription" ) );

                    one(servletRequest).getParameterValues( "permissions") ;
                    will(returnValue( new String[]{"dummy"}) );
                    //==== FORM DATA:END                    
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    one(mockedPermGroupHandler).create(with(any(LoginVO.class)), with(any(PermissionGroupVO.class)));                    
                    will( throwException( new LDAPException("Group already exists")) );
                    
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
        
        final Template  result = stubbedPermGrpManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_edit_permission_group.vm! VM="+result.getName(), "aac_edit_permission_group.vm".equals ( result.getName() ) );                
    }     // func


    /**
     * Test case :  edit perm. group -> show edit template
     */
    @Test
    public void testHandleRequest_EditPermGroupShowEditPage() throws Exception {
    
        final HttpServletRequest        servletRequest     = context.mock(HttpServletRequest.class);
        final Context                   velocityContext    = context.mock(Context.class);
        final HttpSession               mockedSession      = context.mock(HttpSession.class);        
        final PermissionGroupHandler    mockedPermGroupHandler  = context.mock(PermissionGroupHandler.class);
//        final PermissionGroupHandler    mockedGroupHandler = context.mock(PermissionGroupHandler.class);        
        
        final StubbedPermGrpManagement stubbedPermGrpManagement= new StubbedPermGrpManagement();
        stubbedPermGrpManagement.setGroupHandler(mockedPermGroupHandler);

        
        //
        // login data
        //
        final LoginForm loginForm = new LoginForm();
        loginForm.setUserName("admin");
        loginForm.setUserPassword("admin");
        
        context.checking( new Expectations() {
                {
                    one(velocityContext).put( with("permGroupData"), with(any(PermissionGroupVO.class)) ) ;
                    
                    allowing(servletRequest).getSession();
                    will(returnValue(mockedSession));
                    
                    one(servletRequest).getParameter( "gid") ;
                    will(returnValue("allpermissions"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("editPermissionGroup") );

                    one(servletRequest).getParameter("formValid");
                    will(returnValue("") );
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    one(mockedPermGroupHandler).findById(with(any(LoginVO.class)), with(any(PermissionGroupVO.class)));   
                    will(returnValue(new PermissionGroupVO()) );
                    
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
        
        final Template  result = stubbedPermGrpManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_edit_permission_group.vm! VM="+result.getName(), "aac_edit_permission_group.vm".equals ( result.getName() ) );                
        
    }// func


    
    /**
     * Test case : edit template is shown and SAVE is pressed; will use mocked form data
     */
    @Test
    public void testHandleRequest_EditPermGroupSaveForm() throws Exception {

        final HttpServletRequest servletRequest     = context.mock(HttpServletRequest.class);
        final Context            velocityContext    = context.mock(Context.class);
        final HttpSession        mockedSession      = context.mock(HttpSession.class);        
        final PermissionGroupHandler    mockedPermGroupHandler  = context.mock(PermissionGroupHandler.class);
        
        final StubbedPermGrpManagement stubbedPermGrpManagement= new StubbedPermGrpManagement();
        stubbedPermGrpManagement.setGroupHandler(mockedPermGroupHandler);
        
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
                    
                    one(servletRequest).getParameter( "gid") ;
                    will(returnValue("allpermissions"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("editPermissionGroup") );

                    one(servletRequest).getParameter("formValid");
                    will(returnValue("yes") );
                    
                    //==== FORM DATA:START
                    one(servletRequest).getParameter( "permGroupName") ;
                    will(returnValue( "_permGroupName" ) );
                    
                    one(servletRequest).getParameter( "permGroupTitle") ;
                    will(returnValue( "_permGroupTitle" ) );
                    
                    one(servletRequest).getParameter( "permGroupDescription") ;
                    will(returnValue( "_permGroupDescription" ) );

                    one(servletRequest).getParameterValues( "permissions") ;
                    will(returnValue( new String[]{"dummy"}) );
                    //==== FORM DATA:END                    
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    one(mockedPermGroupHandler).modify(with(any(LoginVO.class)), with(any(PermissionGroupVO.class)));                    
                    
                    one(mockedPermGroupHandler).findAll(with(any(LoginVO.class)));   
                    
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
        
        final Template  result = stubbedPermGrpManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_view_permission_groups.vm! VM="+result.getName(), "aac_view_permission_groups.vm".equals ( result.getName() ) );        
    }    
    
    
    
    /**
     * Test case : 
     */
    @Test
    public void testHandleRequest_ViewPermGroupsPermissions() throws Exception {
    
        final HttpServletRequest        servletRequest     = context.mock(HttpServletRequest.class);
        final Context                   velocityContext    = context.mock(Context.class);
        final HttpSession               mockedSession      = context.mock(HttpSession.class);        
        final PermissionGroupHandler    mockedPermGroupHandler  = context.mock(PermissionGroupHandler.class);
        
        final StubbedPermGrpManagement stubbedPermGrpManagement= new StubbedPermGrpManagement();
        stubbedPermGrpManagement.setGroupHandler(mockedPermGroupHandler);
        
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
                    
                    one(servletRequest).getParameter( "gid") ;
                    will(returnValue("allpermissions"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("viewPermissions") );
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));

                    one(mockedPermGroupHandler).findById(with(any(LoginVO.class)), with(any(PermissionGroupVO.class)));   
                    will(returnValue(new PermissionGroupVO()) );

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
        
        final Template  result = stubbedPermGrpManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_view_permgroups_perms.vm! VM="+result.getName(), "aac_view_permgroups_perms.vm".equals ( result.getName() ) );        
    }// func
    
    
    
    /**
     * Test case : Delete permission group
     */
    @Test
    public void testHandleRequest_DeletePermGroup() throws Exception {
    
        final HttpServletRequest        servletRequest     = context.mock(HttpServletRequest.class);
        final Context                   velocityContext    = context.mock(Context.class);
        final HttpSession               mockedSession      = context.mock(HttpSession.class);        
        final PermissionGroupHandler    mockedPermGroupHandler  = context.mock(PermissionGroupHandler.class);
        
        final StubbedPermGrpManagement stubbedPermGrpManagement= new StubbedPermGrpManagement();
        stubbedPermGrpManagement.setGroupHandler(mockedPermGroupHandler);
        
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
                    
                    one(servletRequest).getParameter( "gid") ;
                    will(returnValue("allpermissions"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("deletePermissionGroup") );    
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));

                    one(mockedPermGroupHandler).delete ( with(any(LoginVO.class)), with(any(LoginVO.class)));   

                    one(mockedPermGroupHandler).findAll(with(any(LoginVO.class)));   

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
        
        final Template  result = stubbedPermGrpManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_view_permission_groups.vm! VM="+result.getName(), "aac_view_permission_groups.vm".equals ( result.getName() ) );        
        
    }// func

    /**
     * Test case : Delete permission group with error
     */
    @Test
    public void testHandleRequest_DeletePermGroupError () throws Exception {
    
        final HttpServletRequest        servletRequest     = context.mock(HttpServletRequest.class);
        final Context                   velocityContext    = context.mock(Context.class);
        final HttpSession               mockedSession      = context.mock(HttpSession.class);        
        final PermissionGroupHandler    mockedPermGroupHandler  = context.mock(PermissionGroupHandler.class);
        
        final StubbedPermGrpManagement stubbedPermGrpManagement= new StubbedPermGrpManagement();
        stubbedPermGrpManagement.setGroupHandler(mockedPermGroupHandler);
        
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
                    
                    one(servletRequest).getParameter( "gid") ;
                    will(returnValue("allpermissions"));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(loginForm));

                    atLeast(1).of(velocityContext).put( with(any(String.class)), with(any(Object.class)))  ;
                    
                    one(servletRequest).getParameter("action");
                    will(returnValue("deletePermissionGroup") );    
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));

                    one(mockedPermGroupHandler).delete ( with(any(LoginVO.class)), with(any(LoginVO.class)));   
                    will( throwException( new LDAPException("Expected Exception")) );

                    one(mockedPermGroupHandler).findAll(with(any(LoginVO.class)));                       

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
        
        final Template  result = stubbedPermGrpManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not aac_view_permission_groups.vm! VM="+result.getName(), "aac_view_permission_groups.vm".equals ( result.getName() ) );        
        
    }// func
    
    
    /**
     * Test case : 
     */
    @Test
    public void testHandleRequest_FatalError () throws Exception {
    
        final HttpServletRequest servletRequest     = context.mock(HttpServletRequest.class);
        final Context            velocityContext    = context.mock(Context.class);
        final HttpSession               mockedSession      = context.mock(HttpSession.class);        
        final PermissionGroupHandler    mockedPermGroupHandler  = context.mock(PermissionGroupHandler.class);
        
        final StubbedPermGrpManagement stubbedPermGrpManagement= new StubbedPermGrpManagement();
        stubbedPermGrpManagement.setGroupHandler(mockedPermGroupHandler);
        
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
                    
                    one(servletRequest).getParameter( "gid") ;
                    will(returnValue("allpermissions"));
                    
                    allowing(servletRequest).getUserPrincipal();
                    will(returnValue(new UserPrincipal("admin")));
                    
                    one(mockedSession).getAttribute("loginForm");
                    will(returnValue(null));
                    
                    //one(velocityContext).put( with("tr"), with(any(String.class)) ) ;                    
                    
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
        
        System.out.println("[PermissionGroupManagementTest:testHandleRequest_FatalError] testing for fatal error...");         
        final Template  result = stubbedPermGrpManagement.handleRequest(servletRequest, servletResponse, velocityContext);
        
        Assert.assertNotNull(result);
        Assert.assertTrue( "VM template is not "+EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU, EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU.equals ( result.getName() ) );        
    }// func
    

    
    @SuppressWarnings("serial")
    private class StubbedPermGrpManagement extends PermissionGroupManagement {
        
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
        protected void putAvailPermissions(final Context velocityContext, final LoginVO loginVo) throws LDAPException {
            // do nothing; only prevent call to LDAP
        }
        
        
        public void setGroupHandler (final PermissionGroupHandler handler ) throws Exception {
            //We use reflection to access final field
            final Field field = getClass().getSuperclass().getDeclaredField("permGrpHandler");
            field.setAccessible(true);
            field.set(this, handler);
        }

        
    }//class

    
}//class
