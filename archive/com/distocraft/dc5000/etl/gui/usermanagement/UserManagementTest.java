package com.distocraft.dc5000.etl.gui.usermanagement;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;

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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;
import utils.MemoryDatabaseUtility;

import com.distocraft.dc5000.etl.gui.common.EnvironmentEvents;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.SecurityAdminConstants;
import com.distocraft.dc5000.etl.gui.etl.ShowHistorySetTest;
import com.distocraft.dc5000.etl.gui.login.LoginForm;
import com.ericsson.eniq.ldap.handler.UserHandler;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.UserVO;
import com.sun.security.auth.UserPrincipal;

@RunWith(JMock.class)
public class UserManagementTest {

  private Mockery context;
  public static RockFactory dwhrepRock;
  public static final String TEST_APPLICATION = UserManagementTest.class.getName();
  private static final Long DEFAULT_LAST_LOGIN_THRESHOLD = 90L;
  private static final Long TEST_LAST_LOGIN_THRESHOLD = 30L;

  @BeforeClass
  public static void setUpBeforeClass() throws java.lang.Exception {
	    dwhrepRock = new RockFactory(MemoryDatabaseUtility.TEST_DWHREP_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
	            MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
	    final URL dwhrepsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWHREP_BASIC);
	    if (dwhrepsqlurl == null) {
	        System.out.println("dwh script can not be loaded!");
	    } else {
	        MemoryDatabaseUtility.loadSetup(dwhrepRock, dwhrepsqlurl);
	    }
}  
  
  @Before
  public void setUp() throws Exception {

	resetMockDwhrepConnection();
	  
	context = new JUnit4Mockery();

    context.setImposteriser(ClassImposteriser.INSTANCE);
    
    // reset last login threshold for each test
    UserManagement.setLastLoginThreshold(null);

  }

  private void resetMockDwhrepConnection() throws Exception {
      dwhrepRock = new RockFactory(MemoryDatabaseUtility.TEST_DWHREP_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
            MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
  }

  @Test 
  public void testGetLastLoginThresholdFromDB() {
	  final Context velocityContext = context.mock(Context.class);

	    context.checking(new Expectations() {
	        {
	          one(velocityContext).get("rockDwhRep");
	          will(returnValue(dwhrepRock));
	        }
	    });
	    
	    UserManagement umTest = new UserManagement();
	    
	    umTest.getLastLoginThresholdFromDB(velocityContext);
	    
	    assertEquals("Default last login threshold is incorrect.", DEFAULT_LAST_LOGIN_THRESHOLD, UserManagement.getLastLoginThreshold());
  }
  
  @Test 
  public void testSetLastLoginThreshold() {
	  final Context velocityContext = context.mock(Context.class);

	    context.checking(new Expectations() {
	        {
	          one(velocityContext).get("rockDwhRep");
	          will(returnValue(dwhrepRock));
	        }
	    });
	    
	    UserManagement umTest = new UserManagement();
	    
	    umTest.getLastLoginThresholdFromDB(velocityContext);
	    
	    assertEquals("Default last login threshold is incorrect.", DEFAULT_LAST_LOGIN_THRESHOLD, UserManagement.getLastLoginThreshold());
	    
	    UserManagement.setLastLoginThreshold(TEST_LAST_LOGIN_THRESHOLD);
	    
	    assertEquals("Last login threshold not set to correct value.", TEST_LAST_LOGIN_THRESHOLD, UserManagement.getLastLoginThreshold());
  }
  
  
  /**
   * 
   * Test case : view all users -> show all users template by calling a mocked
   * userHandler.findAll function
   */

  @Test
  public void testHandleRequest_ViewAllUsers() throws Exception {

    //

    // create mock objects that will do nothing on our behalf, but allows us

    // to call their methods

    //

    final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);

    final Context velocityContext = context.mock(Context.class);

    final HttpSession mockedSession = context.mock(HttpSession.class);

    final UserHandler mockedUserHandler = context.mock(UserHandler.class);

    final StubbedUserManagement stubbedUserManagement = new StubbedUserManagement();

    stubbedUserManagement.setUserHandler(mockedUserHandler);

    //

    // login data

    //

    final LoginForm loginForm = new LoginForm();

    loginForm.setUserName("admin");

    loginForm.setUserPassword("admin");

    context.checking(new Expectations() {

      {

	    one(velocityContext).get("rockDwhRep");
	    will(returnValue(dwhrepRock));
	      
        one(velocityContext).put(with(SecurityAdminConstants.NOTIFICATION_VAR), with(any(String.class)));

        allowing(servletRequest).getSession();

        will(returnValue(mockedSession));

        one(servletRequest).getParameter("uid");

        will(returnValue("admin"));

        one(mockedSession).getAttribute("loginForm");

        will(returnValue(loginForm));

        atLeast(1).of(velocityContext).put(with(any(String.class)), with(any(Object.class)));

        one(servletRequest).getParameter("action");

        will(returnValue(null)); // return 'null'->sets default action to
                                 // 'view'->allows more code coverage

        one(mockedUserHandler).findAll(with(any(LoginVO.class)));

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
      }
    });

    final HttpServletResponse servletResponse = null;

    final Template result = stubbedUserManagement.handleRequest(servletRequest, servletResponse, velocityContext);

    Assert.assertNotNull(result);

    Assert.assertTrue("VM template is not aac_view_users.vm! VM=" + result.getName(),
        "aac_view_users.vm".equals(result.getName()));
    
  }// func

  /**
   * 
   * Test case : add user 1. -> show edit template
   */

  @Test
  public void testHandleRequest_AddUserShowEditPage() throws Exception {

    final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);

    final Context velocityContext = context.mock(Context.class);

    final HttpSession mockedSession = context.mock(HttpSession.class);

    final UserHandler mockedUserHandler = context.mock(UserHandler.class);

    final StubbedUserManagement stubbedUserManagement = new StubbedUserManagement();

    stubbedUserManagement.setUserHandler(mockedUserHandler);

    //

    // login data

    //

    final LoginForm loginForm = new LoginForm();

    loginForm.setUserName("admin");

    loginForm.setUserPassword("admin");

    context.checking(new Expectations() {

      {

  	    one(velocityContext).get("rockDwhRep");
  	    will(returnValue(dwhrepRock));

  	    one(velocityContext).put(with("userData"), with(any(UserVO.class)));

        allowing(servletRequest).getSession();

        will(returnValue(mockedSession));

        one(servletRequest).getParameter("uid");

        will(returnValue("admin"));

        one(mockedSession).getAttribute("loginForm");

        will(returnValue(loginForm));

        atLeast(1).of(velocityContext).put(with(any(String.class)), with(any(Object.class)));

        one(servletRequest).getParameter("action");

        will(returnValue("addUser"));

        one(mockedUserHandler).findById(with(any(LoginVO.class)), with(any(UserVO.class))); // logged
                                                                                            // user
                                                                                            // predef

        will(returnValue(new UserVO()));

        one(servletRequest).getParameter("formValid");

        will(returnValue(""));

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
      }
    });

    final HttpServletResponse servletResponse = null;

    final Template result = stubbedUserManagement.handleRequest(servletRequest, servletResponse, velocityContext);

    Assert.assertNotNull(result);

    Assert.assertTrue("VM template is not aac_edit_user.vm! VM=" + result.getName(),
        "aac_edit_user.vm".equals(result.getName()));

  }// func

  /**
   * 
   * Test case : add user 2. -> edit template is shown and SAVE is pressed; will
   * use mocked form data
   */

  @Test
  public void testHandleRequest_AddUserSaveForm() throws Exception {

    final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);

    final Context velocityContext = context.mock(Context.class);

    final HttpSession mockedSession = context.mock(HttpSession.class);

    final UserHandler mockedUserHandler = context.mock(UserHandler.class);

    final StubbedUserManagement stubbedUserManagement = new StubbedUserManagement();

    stubbedUserManagement.setUserHandler(mockedUserHandler);

    //

    // login data

    //

    final LoginForm loginForm = new LoginForm();

    loginForm.setUserName("admin");

    loginForm.setUserPassword("admin");

    context.checking(new Expectations() {

      {

  	    one(velocityContext).get("rockDwhRep");
  	    will(returnValue(dwhrepRock));

  	    allowing(servletRequest).getSession();

        will(returnValue(mockedSession));

        one(servletRequest).getParameter("uid");

        will(returnValue("admin"));

        one(mockedSession).getAttribute("loginForm");

        will(returnValue(loginForm));

        atLeast(1).of(velocityContext).put(with(any(String.class)), with(any(Object.class)));

        one(servletRequest).getParameter("action");

        will(returnValue("addUser"));

        one(mockedUserHandler).findById(with(any(LoginVO.class)), with(any(UserVO.class))); // logged
                                                                                            // user
                                                                                            // predef

        will(returnValue(new UserVO()));

        one(servletRequest).getParameter("formValid");

        will(returnValue("yes"));

        // ==== FORM DATA:START

        one(servletRequest).getParameter("userId");

        will(returnValue("_userId"));

        one(servletRequest).getParameter("password");

        will(returnValue("_password"));

        one(servletRequest).getParameter("firstName");

        will(returnValue("_firstName"));

        one(servletRequest).getParameter("lastName");

        will(returnValue("_lastName"));

        one(servletRequest).getParameter("email");

        will(returnValue("_email"));

        one(servletRequest).getParameter("phone");

        will(returnValue("_phone"));

        one(servletRequest).getParameter("organization");

        will(returnValue("_organization"));

        one(servletRequest).getParameterValues("userRoles");

        will(returnValue(new String[] { "dummy" }));

        // ==== FORM DATA:END

        one(mockedUserHandler).create(with(any(LoginVO.class)), with(any(UserVO.class)));

        one(mockedUserHandler).findAll(with(any(LoginVO.class)));

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

      }
    });

    final HttpServletResponse servletResponse = null;

    final Template result = stubbedUserManagement.handleRequest(servletRequest, servletResponse, velocityContext);

    Assert.assertNotNull(result);

    Assert.assertTrue("VM template is not aac_view_users.vm! VM=" + result.getName(),
        "aac_view_users.vm".equals(result.getName()));

  }

  /**
   * 
   * Test case : add user 3. -> edit template is shown and SAVE is pressed with
   * a duplicated user -> expect error message
   */

  @Test
  public void testHandleRequest_AddUserSaveFormDuplicatedUser() throws Exception {

    final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);

    final Context velocityContext = context.mock(Context.class);

    final HttpSession mockedSession = context.mock(HttpSession.class);

    final UserHandler mockedUserHandler = context.mock(UserHandler.class);

    final StubbedUserManagement stubbedUserManagement = new StubbedUserManagement();

    stubbedUserManagement.setUserHandler(mockedUserHandler);

    //

    // login data

    //

    final LoginForm loginForm = new LoginForm();

    loginForm.setUserName("admin");

    loginForm.setUserPassword("admin");

    context.checking(new Expectations() {

      {

  	    one(velocityContext).get("rockDwhRep");
  	    will(returnValue(dwhrepRock));

  	    one(velocityContext).put(with(SecurityAdminConstants.ERROR_MESSAGE_VAR), with(any(String.class)));

        one(velocityContext).put(with("userData"), with(any(UserVO.class)));

        allowing(servletRequest).getSession();

        will(returnValue(mockedSession));

        one(servletRequest).getParameter("uid");

        will(returnValue("admin"));

        one(mockedSession).getAttribute("loginForm");

        will(returnValue(loginForm));

        atLeast(1).of(velocityContext).put(with(any(String.class)), with(any(Object.class)));

        one(servletRequest).getParameter("action");

        will(returnValue("addUser"));

        one(mockedUserHandler).findById(with(any(LoginVO.class)), with(any(UserVO.class))); // logged
                                                                                            // user
                                                                                            // predef

        will(returnValue(new UserVO()));

        one(servletRequest).getParameter("formValid");

        will(returnValue("yes"));

        // ==== FORM DATA:START

        one(servletRequest).getParameter("userId");

        will(returnValue("_userId"));

        one(servletRequest).getParameter("password");

        will(returnValue("_password"));

        one(servletRequest).getParameter("firstName");

        will(returnValue("_firstName"));

        one(servletRequest).getParameter("lastName");

        will(returnValue("_lastName"));

        one(servletRequest).getParameter("email");

        will(returnValue("_email"));

        one(servletRequest).getParameter("phone");

        will(returnValue("_phone"));

        one(servletRequest).getParameter("organization");

        will(returnValue("_organization"));

        one(servletRequest).getParameterValues("userRoles");

        will(returnValue(new String[] { "dummy" }));

        // ==== FORM DATA:END

        one(mockedUserHandler).create(with(any(LoginVO.class)), with(any(UserVO.class)));

        will(throwException(new LDAPException("User already exists")));

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
      }
    });

    final HttpServletResponse servletResponse = null;

    final Template result = stubbedUserManagement.handleRequest(servletRequest, servletResponse, velocityContext);

    Assert.assertNotNull(result);

    Assert.assertTrue("VM template is not aac_edit_user.vm! VM=" + result.getName(),
        "aac_edit_user.vm".equals(result.getName()));

  } // func

  /**
   * 
   * Test case : edit user -> show edit template
   */

  @Test
  public void testHandleRequest_EditUserShowEditPage() throws Exception {

    final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);

    final Context velocityContext = context.mock(Context.class);

    final HttpSession mockedSession = context.mock(HttpSession.class);

    final UserHandler mockedUserHandler = context.mock(UserHandler.class);

    final StubbedUserManagement stubbedUserManagement = new StubbedUserManagement();

    stubbedUserManagement.setUserHandler(mockedUserHandler);

    //

    // login data

    //

    final LoginForm loginForm = new LoginForm();

    loginForm.setUserName("admin");

    loginForm.setUserPassword("admin");

    context.checking(new Expectations() {

      {

  	    one(velocityContext).get("rockDwhRep");
   	    will(returnValue(dwhrepRock));

        one(velocityContext).put(with("userData"), with(any(UserVO.class)));

        allowing(servletRequest).getSession();

        will(returnValue(mockedSession));

        one(servletRequest).getParameter("uid");

        will(returnValue("admin"));

        one(mockedSession).getAttribute("loginForm");

        will(returnValue(loginForm));

        atLeast(1).of(velocityContext).put(with(any(String.class)), with(any(Object.class)));

        one(servletRequest).getParameter("action");

        will(returnValue("editUser"));

        exactly(2).of(mockedUserHandler).findById(with(any(LoginVO.class)), with(any(UserVO.class))); // logged
                                                                                                      // user
                                                                                                      // predef+edited
                                                                                                      // user

        will(returnValue(new UserVO()));

        one(servletRequest).getParameter("formValid");

        will(returnValue(""));

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
      }
    });

    final HttpServletResponse servletResponse = null;

    final Template result = stubbedUserManagement.handleRequest(servletRequest, servletResponse, velocityContext);

    Assert.assertNotNull(result);

    Assert.assertTrue("VM template is not aac_edit_user.vm! VM=" + result.getName(),
        "aac_edit_user.vm".equals(result.getName()));

  }// func

  /**
   * 
   * Test case : edit template is shown and SAVE is pressed; will use mocked
   * form data
   * 
   * 
   */

  @Test
  public void testHandleRequest_EditUserSaveForm() throws Exception {

    final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);

    final Context velocityContext = context.mock(Context.class);

    final HttpSession mockedSession = context.mock(HttpSession.class);

    final UserHandler mockedUserHandler = context.mock(UserHandler.class);

    final StubbedUserManagement stubbedUserManagement = new StubbedUserManagement();

    stubbedUserManagement.setUserHandler(mockedUserHandler);

    //

    // login data

    //

    final LoginForm loginForm = new LoginForm();

    loginForm.setUserName("admin");

    loginForm.setUserPassword("admin");

    context.checking(new Expectations() {

      {

	    one(velocityContext).get("rockDwhRep");
  	    will(returnValue(dwhrepRock));

        allowing(servletRequest).getSession();

        will(returnValue(mockedSession));

        one(servletRequest).getParameter("uid");

        will(returnValue("admin"));

        one(mockedSession).getAttribute("loginForm");

        will(returnValue(loginForm));

        atLeast(1).of(velocityContext).put(with(any(String.class)), with(any(Object.class)));

        one(servletRequest).getParameter("action");

        will(returnValue("editUser"));

        one(mockedUserHandler).findById(with(any(LoginVO.class)), with(any(UserVO.class))); // logged
                                                                                            // user
                                                                                            // predef

        will(returnValue(new UserVO()));

        one(servletRequest).getParameter("formValid");

        will(returnValue("yes"));

        // ==== FORM DATA:START

        one(servletRequest).getParameter("userId");

        will(returnValue("_userId"));

        one(servletRequest).getParameter("password");

        will(returnValue("_password"));

        one(servletRequest).getParameter("firstName");

        will(returnValue("_firstName"));

        one(servletRequest).getParameter("lastName");

        will(returnValue("_lastName"));

        one(servletRequest).getParameter("email");

        will(returnValue("_email"));

        one(servletRequest).getParameter("phone");

        will(returnValue("_phone"));

        one(servletRequest).getParameter("organization");

        will(returnValue("_organization"));

        one(servletRequest).getParameterValues("userRoles");

        will(returnValue(new String[] { "dummy" }));

        // ==== FORM DATA:END

        one(mockedUserHandler).modify(with(any(LoginVO.class)), with(any(UserVO.class)));

        one(mockedUserHandler).findAll(with(any(LoginVO.class)));

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
      }
    });

    final HttpServletResponse servletResponse = null;

    final Template result = stubbedUserManagement.handleRequest(servletRequest, servletResponse, velocityContext);

    Assert.assertNotNull(result);

    Assert.assertTrue("VM template is not aac_view_users.vm! VM=" + result.getName(),
        "aac_view_users.vm".equals(result.getName()));

  } // func

  /**
   * 
   * Test case : edit template is shown and SAVE is pressed whilst user editing
   * itself;
   * 
   * 
   */

  @Test
  public void testHandleRequest_EditUserSaveFormSelfEdit() throws Exception {

    final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);

    final Context velocityContext = context.mock(Context.class);

    final HttpSession mockedSession = context.mock(HttpSession.class);

    final UserHandler mockedUserHandler = context.mock(UserHandler.class);

    final StubbedUserManagement stubbedUserManagement = new StubbedUserManagement();

    stubbedUserManagement.setUserHandler(mockedUserHandler);

    //

    // login data

    //

    final LoginForm loginForm = new LoginForm();

    loginForm.setUserName("admin");

    loginForm.setUserPassword("admin");

    context.checking(new Expectations() {

      {

  	    one(velocityContext).get("rockDwhRep");
  	    will(returnValue(dwhrepRock));

        allowing(servletRequest).getSession();

        will(returnValue(mockedSession));

        one(servletRequest).getParameter("uid");

        will(returnValue("admin"));

        one(mockedSession).getAttribute("loginForm");

        will(returnValue(loginForm));

        atLeast(1).of(velocityContext).put(with(any(String.class)), with(any(Object.class)));

        one(servletRequest).getParameter("action");

        will(returnValue("editUser"));

        one(mockedUserHandler).findById(with(any(LoginVO.class)), with(any(UserVO.class))); // logged
                                                                                            // user
                                                                                            // predef

        will(returnValue(new UserVO()));

        one(servletRequest).getParameter("formValid");

        will(returnValue("yes"));

        // ==== FORM DATA:START

        one(servletRequest).getParameter("userId");

        will(returnValue("admin"));

        one(servletRequest).getParameter("password");

        will(returnValue("_password"));

        one(servletRequest).getParameter("firstName");

        will(returnValue("_firstName"));

        one(servletRequest).getParameter("lastName");

        will(returnValue("_lastName"));

        one(servletRequest).getParameter("email");

        will(returnValue("_email"));

        one(servletRequest).getParameter("phone");

        will(returnValue("_phone"));

        one(servletRequest).getParameter("organization");

        will(returnValue("_organization"));

        one(servletRequest).getParameterValues("userRoles");

        will(returnValue(new String[] { "dummy" }));

        // ==== FORM DATA:END

        one(mockedUserHandler).modify(with(any(LoginVO.class)), with(any(UserVO.class)));

        one(mockedSession).invalidate();

        one(velocityContext).remove(SecurityAdminConstants.LOGGED_USER_NAME_VAR);

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
      }
    });

    final HttpServletResponse servletResponse = null;

    final Template result = stubbedUserManagement.handleRequest(servletRequest, servletResponse, velocityContext);

    Assert.assertNotNull(result);

    Assert.assertTrue("VM template is not logout.vm! VM=" + result.getName(), "logout.vm".equals(result.getName()));

  } // func

  /**
   * 
   * Test case :
   */

  @Test
  public void testHandleRequest_ViewUserDetails() throws Exception {

    final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);

    final Context velocityContext = context.mock(Context.class);

    final HttpSession mockedSession = context.mock(HttpSession.class);

    final UserHandler mockedUserHandler = context.mock(UserHandler.class);

    final StubbedUserManagement stubbedUserManagement = new StubbedUserManagement();

    stubbedUserManagement.setUserHandler(mockedUserHandler);

    //

    // login data

    //

    final LoginForm loginForm = new LoginForm();

    loginForm.setUserName("admin");

    loginForm.setUserPassword("admin");

    context.checking(new Expectations() {

      {

  	    one(velocityContext).get("rockDwhRep");
  	    will(returnValue(dwhrepRock));

        one(velocityContext).put(with(SecurityAdminConstants.NOTIFICATION_VAR), with(any(String.class)));

        allowing(servletRequest).getSession();

        will(returnValue(mockedSession));

        one(servletRequest).getParameter("uid");

        will(returnValue("admin"));

        one(mockedSession).getAttribute("loginForm");

        will(returnValue(loginForm));

        atLeast(1).of(velocityContext).put(with(any(String.class)), with(any(Object.class)));

        one(servletRequest).getParameter("action");

        will(returnValue("viewUserDetails"));

        one(mockedUserHandler).findById(with(any(LoginVO.class)), with(any(UserVO.class)));

        will(returnValue(new UserVO()));

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
      }
    });

    final HttpServletResponse servletResponse = null;

    final Template result = stubbedUserManagement.handleRequest(servletRequest, servletResponse, velocityContext);

    Assert.assertNotNull(result);

    Assert.assertTrue("VM template is not aac_view_user_details.vm!",
        "aac_view_user_details.vm".equals(result.getName()));

  }// func

  /**
   * 
   * Test case : Delete user
   */

  @Test
  public void testHandleRequest_DeleteUser() throws Exception {

    final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);

    final Context velocityContext = context.mock(Context.class);

    final HttpSession mockedSession = context.mock(HttpSession.class);

    final UserHandler mockedUserHandler = context.mock(UserHandler.class);

    final StubbedUserManagement stubbedUserManagement = new StubbedUserManagement();

    stubbedUserManagement.setUserHandler(mockedUserHandler);

    //

    // login data

    //

    final LoginForm loginForm = new LoginForm();

    loginForm.setUserName("admin");

    loginForm.setUserPassword("admin");

    context.checking(new Expectations() {

      {

  	    one(velocityContext).get("rockDwhRep");
  	    will(returnValue(dwhrepRock));

        one(velocityContext).put(with(SecurityAdminConstants.NOTIFICATION_VAR), with(any(String.class)));

        allowing(servletRequest).getSession();

        will(returnValue(mockedSession));

        one(servletRequest).getParameter("uid");

        will(returnValue("admin"));

        one(mockedSession).getAttribute("loginForm");

        will(returnValue(loginForm));

        atLeast(1).of(velocityContext).put(with(any(String.class)), with(any(Object.class)));

        one(servletRequest).getParameter("action");

        will(returnValue("deleteUser"));

        one(mockedUserHandler).delete(with(any(LoginVO.class)), with(any(LoginVO.class)));

        one(mockedUserHandler).findAll(with(any(LoginVO.class)));

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
      }
    });

    final HttpServletResponse servletResponse = null;

    final Template result = stubbedUserManagement.handleRequest(servletRequest, servletResponse, velocityContext);

    Assert.assertNotNull(result);

    Assert.assertTrue("VM template is not aac_view_users.vm!", "aac_view_users.vm".equals(result.getName()));

  }// func

  /**
   * 
   * Test case : Delete user with error
   */

  @Test
  public void testHandleRequest_DeleteUserError() throws Exception {

    final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);

    final Context velocityContext = context.mock(Context.class);

    final HttpSession mockedSession = context.mock(HttpSession.class);

    final UserHandler mockedUserHandler = context.mock(UserHandler.class);

    final StubbedUserManagement stubbedUserManagement = new StubbedUserManagement();

    stubbedUserManagement.setUserHandler(mockedUserHandler);

    //

    // login data

    //

    final LoginForm loginForm = new LoginForm();

    loginForm.setUserName("admin");

    loginForm.setUserPassword("admin");

    context.checking(new Expectations() {

      {

        // expect error !

        one(velocityContext).put(with(SecurityAdminConstants.ERROR_MESSAGE_VAR), with(any(String.class)));

        allowing(servletRequest).getSession();

        will(returnValue(mockedSession));

	    one(velocityContext).get("rockDwhRep");
  	    will(returnValue(dwhrepRock));

        one(servletRequest).getParameter("uid");

        will(returnValue("admin"));

        one(mockedSession).getAttribute("loginForm");

        will(returnValue(loginForm));

        atLeast(1).of(velocityContext).put(with(any(String.class)), with(any(Object.class)));

        one(servletRequest).getParameter("action");

        will(returnValue("deleteUser"));

        one(mockedUserHandler).delete(with(any(LoginVO.class)), with(any(LoginVO.class)));

        will(throwException(new LDAPException("Expected Exception")));

        one(mockedUserHandler).findAll(with(any(LoginVO.class)));

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
      }
    });

    final HttpServletResponse servletResponse = null;

    final Template result = stubbedUserManagement.handleRequest(servletRequest, servletResponse, velocityContext);

    Assert.assertNotNull(result);

    Assert.assertTrue("VM template is not aac_view_users.vm!", "aac_view_users.vm".equals(result.getName()));

  }// func

  /**
   * 
   * Test case :
   */

  @Test
  public void testHandleRequest_FatalError() throws Exception {

    final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);

    final Context velocityContext = context.mock(Context.class);

    final HttpSession mockedSession = context.mock(HttpSession.class);

    final UserHandler mockedUserHandler = context.mock(UserHandler.class);

    final StubbedUserManagement stubbedUserManagement = new StubbedUserManagement();

    stubbedUserManagement.setUserHandler(mockedUserHandler);

    //

    // login data

    //

    final LoginForm loginForm = new LoginForm();

    loginForm.setUserName("admin");

    loginForm.setUserPassword("admin");

    context.checking(new Expectations() {

      {

  	    one(velocityContext).get("rockDwhRep");
  	    will(returnValue(dwhrepRock));

        allowing(servletRequest).getSession();

        will(returnValue(mockedSession));

        one(servletRequest).getParameter("uid");

        will(returnValue("admin"));

        one(mockedSession).getAttribute("loginForm");

        will(returnValue(null));

        // one(velocityContext).put( with("tr"), with(any(String.class)) ) ;

        allowing(servletRequest).getUserPrincipal();
        will(returnValue(new UserPrincipal("admin")));

        allowing(mockedSession).getAttribute("environment");
        will(returnValue(new EnvironmentEvents()));

        one(velocityContext).put(with("lastLoginThreshold"), with(any(String.class)));

        one(velocityContext).put(with("theuser"), with(any(String.class)));

        one(velocityContext).put(with("currenttime"), with(any(String.class)));

        one(velocityContext).put(with("environment"), with(any(EnvironmentEvents.class)));

        one(velocityContext).put(with("errorSet"), with(any(Boolean.class)));
        one(velocityContext).put(with("errorText"), with(any(String.class)));
        
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
      }
    });

    final HttpServletResponse servletResponse = null;

    System.out.println("[UserManagementTest:testHandleRequest_FatalError] testing for fatal error...");

    final Template result = stubbedUserManagement.handleRequest(servletRequest, servletResponse, velocityContext);

    Assert.assertNotNull(result);

    Assert.assertTrue("VM template is not " + EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU,
        EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU.equals(result.getName()));

  }// func

  // /**

  // * Test case : Unlock user

  // */

  // @Test

  // public void testHandleRequest_UnlockUser () throws Exception {

  //

  // final HttpServletRequest servletRequest =
  // context.mock(HttpServletRequest.class);

  // final Context velocityContext = context.mock(Context.class);

  // final HttpSession mockedSession = context.mock(HttpSession.class);

  // final UserHandler mockedUserHandler = context.mock(UserHandler.class);

  //

  //

  // final StubbedUserManagement stubbedUserManagement= new
  // StubbedUserManagement();

  // stubbedUserManagement.setUserHandler(mockedUserHandler);

  //

  // //

  // // login data

  // //

  // final LoginForm loginForm = new LoginForm();

  // loginForm.setUserName("admin");

  // loginForm.setUserPassword("admin");

  //

  // context.checking( new Expectations() {

  // {

  // one(velocityContext).put( with(SecurityAdminConstants.NOTIFICATION_VAR),
  // with(any(String.class)) ) ;

  //

  // one(servletRequest).getSession();

  // will(returnValue(mockedSession));

  //

  // one(servletRequest).getParameter( "uid") ;

  // will(returnValue("admin"));

  //

  // one(mockedSession).getAttribute("loginForm");

  // will(returnValue(loginForm));

  //

  // atLeast(1).of(velocityContext).put( with(any(String.class)),
  // with(any(Object.class))) ;

  //

  // one(servletRequest).getParameter("action");

  // will(returnValue("unlockUser") );

  //

  // one(mockedLDAPUtil).unlockUser( with(any(LoginVO.class)),
  // with(any(UserVO.class)));

  //

  // one(mockedUserHandler).findAll(with(any(LoginVO.class)));

  // } } );

  //

  //

  // final HttpServletResponse servletResponse = null;

  //

  // final Template result = stubbedUserManagement.handleRequest(servletRequest,
  // servletResponse, velocityContext);

  //

  // Assert.assertNotNull(result);

  // Assert.assertTrue( "VM template is not aac_view_users.vm!",
  // "aac_view_users.vm".equals ( result.getName() ) );

  //

  // }// func

  @SuppressWarnings("serial")
  private class StubbedUserManagement extends UserManagement {

//	@Override
//	protected void getLastLoginThresholdFromDB(final Context ctx) {
//		
//	}
	  
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
    protected void putAvailRoles(final Context velocityContext, final LoginVO loginVo) throws LDAPException {

      // just to prevent LDAP call

    }

    public void setUserHandler(final UserHandler handler) throws Exception {

      // We use reflection to access final field

      final Field field = getClass().getSuperclass().getDeclaredField("userHandler");

      field.setAccessible(true);

      field.set(this, handler);

    }

  }// class

}// class

