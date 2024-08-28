///**
// * -----------------------------------------------------------------------
// *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
// * -----------------------------------------------------------------------
// */
//package com.distocraft.dc5000.etl.gui.permissiongroupmanagement;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//
//import junit.framework.Assert;
//
//import org.apache.velocity.Template;
//import org.apache.velocity.context.Context;
//import org.apache.velocity.exception.ParseErrorException;
//import org.apache.velocity.exception.ResourceNotFoundException;
//import org.jmock.Expectations;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.distocraft.dc5000.etl.gui.login.LoginForm;
//import com.ericsson.eniq.common.testutilities.BaseUnitTestX;
//import com.ericsson.eniq.ldap.handler.PermissionGroupHandler;
//import com.ericsson.eniq.ldap.vo.IValueObject;
//import com.ericsson.eniq.ldap.vo.LoginVO;
//
///**
// * @author eemecoy
// * 
// */
//public class PermissionGroupManagementTestExample extends BaseUnitTestX {
//
//  private PermissionGroupManagement permissionGroupManagement;
//
//  PermissionGroupHandler mockedPermissionGroupHandler;
//
//  Template mockedTemplate;
//
//  @Before
//  public void setup() {
//    permissionGroupManagement = new StubbedPermissionGroupManagement();
//    mockedPermissionGroupHandler = context.mock(PermissionGroupHandler.class);
//    mockedTemplate = context.mock(Template.class);
//  }
//
//  @Test
//  public void testHandleRequest() throws Exception {
//    final HttpServletRequest servletRequest = context.mock(HttpServletRequest.class);
//    final LoginForm loginForm = new LoginForm();
//    final HttpSession mockedSession = context.mock(HttpSession.class);
//    final Context velocityContext = context.mock(Context.class);
//    final List<IValueObject> permissionGroups = new ArrayList<IValueObject>();
//    context.checking(new Expectations() {
//
//      {
//        one(servletRequest).getSession();
//        will(returnValue(mockedSession));
//        one(mockedSession).getAttribute("loginForm");
//        will(returnValue(loginForm));
//        one(velocityContext).put("loggedUserName", "");
//        one(servletRequest).getParameter("action");
//        one(servletRequest).getParameter("permgroup_id");
//        one(mockedPermissionGroupHandler).findAll(with(any(LoginVO.class)));
//        will(returnValue(permissionGroups));
//        one(velocityContext).put("listPermGroupsFromLdap", permissionGroups);
//      }
//    });
//    final HttpServletResponse servletResponse = null;
//    final Template result = permissionGroupManagement.handleRequest(servletRequest, servletResponse, velocityContext);
//    Assert.assertEquals(mockedTemplate, result);
//  }
//
//  class StubbedPermissionGroupManagement extends PermissionGroupManagement {
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see com.distocraft.dc5000.etl.gui.permissiongroupmanagement.
//     * PermissionGroupManagement#getPermissionGroupHandler()
//     */
//    @Override
//    PermissionGroupHandler getPermissionGroupHandler() {
//      return mockedPermissionGroupHandler;
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * org.apache.velocity.servlet.VelocityServlet#getTemplate(java.lang.String)
//     */
//    @Override
//    public Template getTemplate(final String name) throws ResourceNotFoundException, ParseErrorException, Exception {
//      return mockedTemplate;
//    }
//  }
//
// }
