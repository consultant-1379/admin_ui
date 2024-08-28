package com.distocraft.dc5000.etl.gui.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.DbConnectionFactory;
import com.distocraft.dc5000.etl.gui.RockFactoryType;
import com.distocraft.dc5000.etl.gui.systemmonitor.LoaderStatus;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * This is the base class for all servlets that don't need a DB connection.
 *
 * Initialises database connection objects and places them in context for sub
 * classes.
 *
 * @author Antti Laurila
 * @author Matti Koljonen
 * @author Jaakko Melantie
 * @author edeccox - refactored
 */

@SuppressWarnings("deprecation")
public abstract class EtlguiServlet extends GuiServlet {

	
	public static final String SESSION_HANDLING_TEMPLATE = "logout.vm";
	private static final long serialVersionUID = 1L;

	private static final String ERROR_PAGE_MESSAGE_KEY = "tr";
	
	private static  int SESSION_TIMEOUT; 

	
	public static int getSessionTimeout() {
		return SESSION_TIMEOUT;
	}

	private static boolean session_check;

	public static boolean isSession_check() {
		return session_check;
	}

	public static void setSession_check(boolean session_check) {
		EtlguiServlet.session_check = session_check;
	}

	public static final String ADMINUI_ERROR_PAGE_TEMPLATE = "adminuiErrorPage.vm";

	public static final String ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU = "adminui_general_errorpage_with_menu.vm";

	public static final String ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHOUTMENU = "adminui_general_errorpage_without_menu.vm";

	public static final String LOGOUT_TEMPLATE = "logout.vm";

	private static final String RUNTIME_LOG_KEY = "runtime.log";

	private static final String VELOCITY_LOG = "WEB-INF/logs/velocity.log";

	private static final String WEB_INF_TEMPLATES = "WEB-INF/templates";

	private final Log log = LogFactory.getLog(this.getClass());
	

	/**
	 *
	 *
	 * @see org.apache.velocity.servlet.VelocityServlet#loadConfiguration(javax.servlet.ServletConfig)
	 */
	@Override
	protected Properties loadConfiguration(final ServletConfig config)
			throws IOException, FileNotFoundException {
		final Properties p = new Properties();
		p.setProperty(Velocity.RESOURCE_LOADER, "file, class");
		p.setProperty("file.resource.loader.description",
				"Velocity File Resource Loader");
		p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,
				getWebRootPath(config) + WEB_INF_TEMPLATES);
		p.setProperty("class.resource.loader.description",
				"Velocity Classpath Resource Loader");
		p.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		p.setProperty(RUNTIME_LOG_KEY, getWebRootPath(config) + VELOCITY_LOG);
		return p;
	}

	@Override
	public final Template handleRequest(final HttpServletRequest request,
			final HttpServletResponse response, final Context context)
			throws Exception {
		
		//To get maximum session time out
		String param_value = getServletContext()
				.getInitParameter("session_timeout_in_minute");
		int session_timeout_in_minute = Integer.parseInt(param_value);
		SESSION_TIMEOUT = session_timeout_in_minute*60;
		
		
		Template viewTemplate = super.handleRequest(request, response, context);
		
		final HttpSession session = request.getSession();
		session.setMaxInactiveInterval(SESSION_TIMEOUT);

		
		
		if (EtlguiServlet.isSession_check()) {
			EtlguiServlet.setSession_check(false);
			request.getSession().invalidate();
			context.put("session_value", true);
			viewTemplate = getTemplate(SESSION_HANDLING_TEMPLATE);

		} else {

			try {
				log.debug("Initializing connections ...");
				RockFactoryType[] rockFactoryTypeArray = null;
				try {
					rockFactoryTypeArray = DbConnectionFactory.getInstance()
							.initialiseConnections(session);
				} catch (Exception e) {
					ENIQServiceStatusInfo
							.setEtlDBHealth(ENIQServiceStatusInfo.ServiceHealth.Offline);
					ENIQServiceStatusInfo
							.setdwhDBHealth(ENIQServiceStatusInfo.ServiceHealth.Offline);
					ENIQServiceStatusInfo
							.setRepDBHealth(ENIQServiceStatusInfo.ServiceHealth.Offline);
					log.debug("Failed to initialise database connection to: rockEtlRep. ");
				}

				if (rockFactoryTypeArray != null) {
					log.debug("Adding databases to context.");
					for (final RockFactoryType type : rockFactoryTypeArray) {
						context.put(type.getName(), type.getRockFactory());
					}
				}

				log.debug("Starting doHandleRequest...");
				
				viewTemplate = doHandleRequest(request, response, context);
				

				log.debug("doHandleRequest done.");
			} catch (final Exception e) {
				try {
					context.put("errorSet", true);
					if (ENIQServiceStatusInfo.isEtlDBOffline()) {
						context.put("errorText",
								" Failed to initialize connection to database: "
										+ ENIQServiceStatusInfo.getEtlDBName());
					} else if (ENIQServiceStatusInfo.isRepDBOffline()) {
						context.put("errorText",
								" Failed to initialize connection to database: "
										+ ENIQServiceStatusInfo.getRepDBName());
					} else if (ENIQServiceStatusInfo.isDwhDBOffline()) {
						context.put("errorText",
								" Failed to initialize connection to database: "
										+ ENIQServiceStatusInfo.getDwhDBName());
					} else {
						final String message = getErrorMessage(e);
						log.error("Error message:" + message);
						log.error("Exception", e);
						context.put("errorText", message);
					}
					viewTemplate = getTemplate(ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU);

				} catch (final Exception e1) {
					log.error("Exception getting velocity template: "
							+ ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU + " ",
							e1);
				}
			}
		}
		return viewTemplate;
	}

	/**
	 * Template method pattern. Subclasses of this servlet implement this
	 * method.
	 *
	 * @param request
	 * @param response
	 * @param ctx
	 *            velocity context
	 * @return view velocity template object
	 */
	public abstract Template doHandleRequest(HttpServletRequest request,
			HttpServletResponse response, Context ctx) throws Exception; // NOPMD
																			// by
																			// eheijun
																			// on
																			// 02/06/11
																			// 14:27

	private static String getErrorMessage(final Throwable tr) {
		String msg = tr.getMessage();
		if (msg != null && msg.contains("JZ00L")) {
			msg = "Max connections to etlrep database reached.";
		} else {
			msg = "Error: " + tr.getLocalizedMessage();
		}
		return msg;
	}

	private static String getWebRootPath(final ServletConfig config) {
		String webRootPath = config.getServletContext().getRealPath("/");
		if (webRootPath == null) {
			webRootPath = "/";
		}
		return webRootPath;
	}

}
