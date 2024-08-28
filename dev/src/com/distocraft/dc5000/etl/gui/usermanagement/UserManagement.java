/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.distocraft.dc5000.etl.gui.usermanagement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import ssc.rockfactory.RockFactory;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.SecurityAdminConstants;
import com.distocraft.dc5000.etl.gui.common.SecurityAdminHelper;
import com.distocraft.dc5000.etl.gui.login.LoginForm;
import com.distocraft.dc5000.etl.gui.util.UnusedUserExport;
import com.ericsson.eniq.ldap.handler.IHandler;
import com.ericsson.eniq.ldap.handler.RoleHandler;
import com.ericsson.eniq.ldap.handler.UserHandler;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.util.LDAPConstants;
import com.ericsson.eniq.ldap.util.LDAPUtil;
import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.UserVO;

/**
 * 
 * This controller servlet for user management ldap operations
 * 
 */
public class UserManagement extends EtlguiServlet {

	private static final long serialVersionUID = 1L;

	// /
	// Request parameters
	// /
	private static final String USER_ID_PARAM = "uid"; // for edit/delete/etc.

	// /
	// Actions verbs
	// /
	private static final String VIEW_USERS_ACTION = "viewUsers";
	private static final String ADD_USER_ACTION = "addUser";
	private static final String EDIT_USER_ACTION = "editUser";
	private static final String DELETE_USER_ACTION = "deleteUser";
	private static final String UNLOCK_USER_ACTION = "unlockUser";
	private static final String VIEW_USER_DETAILS_ACTION = "viewUserDetails";
	private static final String EXPORT_UNUSED_USERS = "exportUnused";

	// /
	// VM files for action verbs
	// /
	private static final String VIEW_USERS_TEMPLATE = "aac_view_users.vm";
	private static final String EDIT_USER_TEMPLATE = "aac_edit_user.vm";
	private static final String VIEW_USER_DETAILS_TEMPLATE = "aac_view_user_details.vm";
	private static final String LOGOUT_TEMPLATE = "logout.vm";

	// /
	// VM context variables
	// /
	private static final String ALL_USERS_VAR = "allUsers";
	private static final String USER_DATA_VAR = "userData";
	private static final String AVAIL_ROLES_VAR = "availRoles";
	private static final String LOGGED_USER_PREDEF_VAR = "loggedUserPredef";

	// /
	// HTML Form IDs
	// /
	private static final String I_USER_ID = "userId";
	private static final String I_PASSWORD = "password";
	private static final String I_FIRST_NAME = "firstName";
	private static final String I_LAST_NAME = "lastName";
	private static final String I_EMAIL = "email";
	private static final String I_PHONE = "phone";
	private static final String I_ORGANIZATION = "organization";
	private static final String I_USER_ROLES = "userRoles";
	private static final String I_FORM_VALID = "formValid"; // if 'YES' set form
															// data is valid in
															// req.

	private static final Long DEFAULT_LAST_LOGIN_THRESHOLD = 90L;
	private static final String LAST_LOGIN_THRESHOLD_VAR = "lastLoginThreshold";
	private static final String SQL_THRESHOLD_SELECT = "SELECT * FROM ENIQ_EVENTS_ADMIN_PROPERTIES WHERE PARAM_NAME='ENIQ_EVENTS_LAST_LOGIN_THRESHOLD'";

	// /
	// AUX
	// /
	private final Log log = LogFactory.getLog(this.getClass());

	protected final IHandler userHandler = new UserHandler(); // protected for
																// testing

	private static Long lastLoginThreshold = null;

	private static Connection getDatabaseConnection(final Context ctx) {
		return ((RockFactory) ctx.get("rockDwhRep")).getConnection();
	}

	protected void getLastLoginThresholdFromDB(final Context ctx) {
		final Connection repCon = getDatabaseConnection(ctx);
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = repCon.createStatement();
			result = statement.executeQuery(SQL_THRESHOLD_SELECT);
			if (result.next()) {
				final String thresholdAsText = result.getString("PARAM_VALUE");
				Long threshold = DEFAULT_LAST_LOGIN_THRESHOLD;
				try {
					threshold = Long.decode(thresholdAsText);
				} catch (Exception e) {
					log.error("The value for last login threshold stored in ENIQ_EVENTS_ADMIN_PROPERTIES is not a number: \""
							+ thresholdAsText + "\". " + e);
				}
				setLastLoginThreshold(threshold);
				log.info("Last Login Threshold set from db at " + threshold);
			}

		} catch (SQLException e) {
			log.info("SQLException: " + e);
			log.info("When executing statement: '" + SQL_THRESHOLD_SELECT + "'");
		} finally {
			try {
				if (result != null) {
					result.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (repCon != null) {
					repCon.close();
				}
			} catch (Exception e) {
				log.error("Exception: ", e);
			}
		}
	}

	public static Long getLastLoginThreshold() {
		return lastLoginThreshold;
	}

	public static void setLastLoginThreshold(final Long lastLoginThreshold) {
		UserManagement.lastLoginThreshold = lastLoginThreshold;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Template doHandleRequest(final HttpServletRequest servletRequest,
			final HttpServletResponse servletResponse,
			final Context velocityContext) throws Exception {

		Template velocityTemplate = null;

		String velocityTemplateFile = null;

		// if threshold isn't set, get it from the database
		if (getLastLoginThreshold() == null) {
			getLastLoginThresholdFromDB(velocityContext);
		}
		velocityContext.put(LAST_LOGIN_THRESHOLD_VAR, lastLoginThreshold);

		String notificationToDisplay = LDAPConstants.EMPTY_STRING;
		HttpSession session = null;
		String reqUserId = null;

		try {

			session = servletRequest.getSession();

			// final Principal userPrincipal =
			// servletRequest.getUserPrincipal();
			reqUserId = servletRequest.getParameter(USER_ID_PARAM); // edit/delete/etc.
																	// or
																	// null

			// //
			// get logged user data
			// //
			final LoginForm loginForm = (LoginForm) session
					.getAttribute(SecurityAdminConstants.LOGIN_FORM_NAME);
			if (loginForm == null) {
				throw new Exception("Session login details are null!");
			}

			final String loggedUserName = loginForm.getUserName();
			final String loggedUserPwd = loginForm.getUserPassword();

			// LDAP login object
			final LoginVO loginVo = new LoginVO();
			loginVo.setLoginId(loggedUserName);
			loginVo.setPassword(loggedUserPwd);

			// action
			String action = servletRequest
					.getParameter(SecurityAdminConstants.ACTION_PARAM);

			if (action == null) {
				// default action
				action = VIEW_USERS_ACTION;
			}

			// _debug += "<br>userPrincipal.getName()="+userPrincipal.getName()+
			// " | loginForm.getUserName()="+loginForm.getUserName()+
			// " | loginForm.getUserPassword()="+loginForm.getUserPassword() +
			// " | UID_PARAM="+reqUserId+
			// " | action="+action+
			// "<br>";

			final boolean addUser = ADD_USER_ACTION.equalsIgnoreCase(action);
			final boolean editUser = EDIT_USER_ACTION.equalsIgnoreCase(action);

			if (VIEW_USERS_ACTION.equalsIgnoreCase(action)) {
				//
				// View all users
				//
				putAllUsers(velocityContext, loginVo);
				velocityTemplateFile = VIEW_USERS_TEMPLATE;

			} else if (EXPORT_UNUSED_USERS.equalsIgnoreCase(action)) {
				//
				// View all users
				//
				putAllUsers(velocityContext, loginVo);
				final UnusedUserExport unusedUserExport = new UnusedUserExport();
				notificationToDisplay = unusedUserExport.export((List<IValueObject>)velocityContext.get(ALL_USERS_VAR), lastLoginThreshold);
				velocityTemplateFile = VIEW_USERS_TEMPLATE;
				
			} else if (DELETE_USER_ACTION.equalsIgnoreCase(action)) {
				//
				// delete specified user
				//
				final UserVO delUser = new UserVO();
				delUser.setUserId(reqUserId);

				try {
					userHandler.delete(loginVo, delUser);
					notificationToDisplay = "User " + reqUserId + " is deleted";
				} catch (final LDAPException ex) {
					velocityContext.put(
							SecurityAdminConstants.ERROR_MESSAGE_VAR,
							ex.getErrorMessage() + " (user:" + reqUserId + ")");
				}

				// reload view
				putAllUsers(velocityContext, loginVo);
				velocityTemplateFile = VIEW_USERS_TEMPLATE;
			} else if (UNLOCK_USER_ACTION.equalsIgnoreCase(action)) {
				//
				// unlock specified user
				//
				final UserVO unlockUser = new UserVO();
				unlockUser.setUserId(reqUserId);

				try {
					LDAPUtil.unlockUser(loginVo, unlockUser);
					notificationToDisplay = "User " + reqUserId
							+ " is unlocked";
				} catch (final LDAPException ex) {
					velocityContext.put(
							SecurityAdminConstants.ERROR_MESSAGE_VAR,
							ex.getErrorMessage() + " (user:" + reqUserId + ")");
				}

				// reload view
				putAllUsers(velocityContext, loginVo);
				velocityTemplateFile = VIEW_USERS_TEMPLATE;

			} else if (addUser || editUser) {
				//
				// Add or Edit user
				//

				velocityContext.put(SecurityAdminConstants.ADD_MODE_VAR,
						new Boolean(addUser) /* VM needs Boolean object */);

				// need to store the logged user's predefined status as it's
				// used by VM for access control
				final boolean loggedUserPredef = findUser(loggedUserName,
						loginVo).isPredefined();
				velocityContext.put(LOGGED_USER_PREDEF_VAR, loggedUserPredef);

				if (SecurityAdminConstants.YES.equals(servletRequest
						.getParameter(I_FORM_VALID))) {
					//
					// 'edit/add' form is submitted->modify user in LDAP
					//

					final UserVO user = createUserFromRequest(servletRequest);

					// do stuff
					String op = null;
					String result = null;
					boolean success = false;
					boolean logoutUser = false;

					try {
						if (addUser) {
							userHandler.create(loginVo, user);
							op = "created";
						} else {
							userHandler.modify(loginVo, user);
							op = "modified";

							if (loggedUserName.equals(user.getUserId())
									&& !user.getPassword().equals(
											LDAPConstants.PASSWORD_PLACEHOLDER)) {
								// self modification with pwd change->need to
								// logout user
								logoutUser = true;
							}
						}

						success = true;

					} catch (final LDAPException ex) {
						result = ex.getErrorMessage();
					}

					if (success) {
						if (!logoutUser) {
							//
							// Op. is done with OK code->show info and modified
							// users
							//
							putAllUsers(velocityContext, loginVo);
							notificationToDisplay = "User " + user.getUserId()
									+ " is " + op;
							velocityTemplateFile = VIEW_USERS_TEMPLATE;
						} else {
							//
							// Op. is done with OK code, but user changed its
							// pwd->logout user
							//
							session.invalidate();

							// remove user from the context -
							// so that it wont be shown in the screen
							velocityContext
									.remove(SecurityAdminConstants.LOGGED_USER_NAME_VAR);
							notificationToDisplay = "Your changes have been saved successfully, please login again using your new password";
							velocityTemplateFile = LOGOUT_TEMPLATE;
						} // if (success)

					} else {
						//
						// Op. returns ERROR->show user vo again and display
						// error message
						//
						putAvailRoles(velocityContext, loginVo);

						velocityContext.put(USER_DATA_VAR, user /*
																 * user that
																 * can't be
																 * saved
																 */);
						velocityContext.put(
								SecurityAdminConstants.ERROR_MESSAGE_VAR,
								result + " (user:" + user.getUserId() + ")");

						velocityTemplateFile = EDIT_USER_TEMPLATE;

					} // if
				} else {

					//
					// show velocity template for editing/adding user
					//
					// show roles
					putAvailRoles(velocityContext, loginVo);

					if (editUser) {
						// load user data if editing
						velocityContext.put(USER_DATA_VAR,
								findUser(reqUserId, loginVo));
					} else {
						// new user
						velocityContext.put(USER_DATA_VAR, createUser());
					}

					velocityTemplateFile = EDIT_USER_TEMPLATE;

				} // if

			} else if (VIEW_USER_DETAILS_ACTION.equalsIgnoreCase(action)) {
				//
				// view details of specified user
				//
				velocityContext
						.put(USER_DATA_VAR, findUser(reqUserId, loginVo));
				velocityTemplateFile = VIEW_USER_DETAILS_TEMPLATE;

			} // if VIEW_USER_ROLES_ACTION

			// velocityContext.put("_debug", _debug);

			velocityContext.put(SecurityAdminConstants.NOTIFICATION_VAR,
					notificationToDisplay);
			velocityTemplate = getTemplate(velocityTemplateFile);

		} catch (LDAPException Le) {

			final String Invalid_Credentials = "Invalid Credentials";
			final String error_mess = Le.getMessage();
			log.error("LDAP getErrorMessage :" + Le.getMessage());

			if (error_mess.contains(Invalid_Credentials)) {
				try {
					session.invalidate();
					// remove user from the context -
					velocityContext
							.remove(SecurityAdminConstants.LOGGED_USER_NAME_VAR);
					notificationToDisplay = "account locked. Please contact the system administrator.";
					velocityTemplateFile = LOGOUT_TEMPLATE;
					velocityContext.put(
							SecurityAdminConstants.NOTIFICATION_VAR,
							notificationToDisplay);
					velocityTemplate = getTemplate(velocityTemplateFile);
					log.error(Le);
				} catch (final ResourceNotFoundException e) {
					log.error("Resource Not Found", e);
					throw e;
				} catch (final ParseErrorException e) {
					log.error("Parse error", e);
					throw e;
				} catch (final SQLException e) {
					log.error("SQL exception", e);
					throw e;
				} catch (final Exception e) { // catch all
					log.error("Exception handling request ", e);
					throw e;
				}
			}
		} catch (final Throwable tr) {

			try {

				// 20111206 EANGUAN:: TR HO96578 :: Logout if account locked
				// using EventsUi
				if (tr != null && tr.getMessage() != null
						&& tr.getMessage().contains("error code 49")) {
					// User account gets locked
					session.invalidate();
					// remove user from the context -
					// so that it wont be shown in the screen
					velocityContext
							.remove(SecurityAdminConstants.LOGGED_USER_NAME_VAR);
					notificationToDisplay = "User credentials are not correct or account gets locked. Please contact admin.";
					velocityContext.put(
							SecurityAdminConstants.NOTIFICATION_VAR,
							notificationToDisplay);
					log.error(tr);
					velocityTemplate = getTemplate(EtlguiServlet.LOGOUT_TEMPLATE);
				} else {
					String errorMsg = null;
					if (tr != null) {
						errorMsg = tr.getLocalizedMessage();
					}
					velocityContext.put("errorSet", true);
					if (errorMsg != null && errorMsg.length() > 0) {
						velocityContext.put("errorText", errorMsg);
					} else {
						velocityContext.put("errorText",
								"Unkown Exception occured: ");
					}
					log.error(tr);
					// get error page template
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
			} catch (final Exception e) { // catch all
				log.error("Exception handling request ", e);
			}
		} // catch

		return velocityTemplate;

	}

	/**
	 * Returns a hashmap containing user data
	 * 
	 * @param request
	 * @return
	 */
	private Map<String, String> extractUserData(final HttpServletRequest request) {
		final Map<String, String> userData = new HashMap<String, String>();

		userData.put(I_USER_ID,
				SecurityAdminHelper.getReqParam(request, I_USER_ID));
		userData.put(I_PASSWORD,
				SecurityAdminHelper.getReqParam(request, I_PASSWORD));
		userData.put(I_FIRST_NAME,
				SecurityAdminHelper.getReqParam(request, I_FIRST_NAME));
		userData.put(I_LAST_NAME,
				SecurityAdminHelper.getReqParam(request, I_LAST_NAME));
		userData.put(I_EMAIL, SecurityAdminHelper.getReqParam(request, I_EMAIL));
		userData.put(I_PHONE, SecurityAdminHelper.getReqParam(request, I_PHONE));
		userData.put(I_ORGANIZATION,
				SecurityAdminHelper.getReqParam(request, I_ORGANIZATION));

		return userData;
	}

	/**
	 * Puts all available roles into context
	 * 
	 * @param velocityContext
	 * @throws LDAPException
	 */
	protected void putAvailRoles(final Context velocityContext,
			final LoginVO loginVo) throws LDAPException {
		final IHandler roleHandler = new RoleHandler();
		final List<IValueObject> availRoles = roleHandler.findAll(loginVo);

		velocityContext.put(AVAIL_ROLES_VAR, availRoles);
	}

	/**
	 * Puts all users into context
	 * 
	 * @param velocityContext
	 * @param loginVo
	 */

	private void putAllUsers(final Context velocityContext,
			final LoginVO loginVo) throws LDAPException {
		final List<IValueObject> allUsers = userHandler.findAll(loginVo);
		velocityContext.put(ALL_USERS_VAR, allUsers);
	}

	/**
	 * Finds specified user and replaces NULLs with empty strings
	 * 
	 */
	private UserVO findUser(final String userId, final LoginVO loginVo)
			throws LDAPException {
		final UserVO reqUser = new UserVO(); // user to find
		reqUser.setUserId(userId);
		final UserVO user = (UserVO) userHandler.findById(loginVo, reqUser);

		// set defaults for optional fields
		if (user.getOrg() == null) {
			user.setOrg(LDAPConstants.EMPTY_STRING);
		}

		return user;
	} // func

	/**
	 * Creates a new user
	 */

	private UserVO createUser() {
		final UserVO newUser = new UserVO();

		newUser.setUserId(LDAPConstants.EMPTY_STRING);
		newUser.setPassword(LDAPConstants.EMPTY_STRING);
		newUser.setFname(LDAPConstants.EMPTY_STRING);
		newUser.setLname(LDAPConstants.EMPTY_STRING);
		newUser.setEmail(LDAPConstants.EMPTY_STRING);
		newUser.setPhone(LDAPConstants.EMPTY_STRING);
		newUser.setOrg(LDAPConstants.EMPTY_STRING);
		newUser.setRoles(new HashSet<String>());
		newUser.setPredefined(false);

		return newUser;
	}

	/**
	 * Creates user from http request /FORM/
	 * 
	 * @param servletRequest
	 * @return
	 */
	private UserVO createUserFromRequest(final HttpServletRequest servletRequest) {

		final Map<String, String> newUserData = extractUserData(servletRequest);
		final UserVO newUser = new UserVO(); //

		newUser.setUserId(newUserData.get(I_USER_ID));
		newUser.setPassword(newUserData.get(I_PASSWORD));
		newUser.setFname(newUserData.get(I_FIRST_NAME));
		newUser.setLname(newUserData.get(I_LAST_NAME));
		newUser.setEmail(newUserData.get(I_EMAIL));
		newUser.setPhone(newUserData.get(I_PHONE));
		newUser.setOrg(newUserData.get(I_ORGANIZATION));
		newUser.setPredefined(false);

		// roles assigned to user
		final String[] userRoles = servletRequest
				.getParameterValues(I_USER_ROLES);
		final Set<String> usrRoles = new HashSet<String>(
				Arrays.asList(userRoles));
		newUser.setRoles(usrRoles);

		return newUser;
	} // func

}// class
