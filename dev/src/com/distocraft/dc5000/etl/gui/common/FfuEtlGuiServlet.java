package com.distocraft.dc5000.etl.gui.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.systemmonitor.ConfirmFeatures;
import com.distocraft.dc5000.etl.gui.systemmonitor.FeatureAvailability;
import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.ericsson.eniq.repository.DBUsersGet;

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
public abstract class FfuEtlGuiServlet extends GuiServlet {

	private static final long serialVersionUID = 1L;

	private static final String RUNTIME_LOG_KEY = "runtime.log";

	private static final String VELOCITY_LOG = "WEB-INF/logs/velocity.log";

	private static final String WEB_INF_TEMPLATES = "WEB-INF/templates";

	private static final String DCUSER = "dcuser";

	private static final String HOST_ADD = "webserver";

	private final Log log = LogFactory.getLog(this.getClass());

	private FeatureAvailability obj = new FeatureAvailability();

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

		Template viewTemplate = super.handleRequest(request, response, context);
				
		if (EtlguiServlet.isSession_check()) {
			EtlguiServlet.setSession_check(false);
			request.getSession().invalidate();
			context.put("session_value", true);
			viewTemplate = getTemplate(EtlguiServlet.SESSION_HANDLING_TEMPLATE);	

		} else {

			try {
				if (ENIQServiceStatusInfo.isRepDBOnline()) {
					List<Meta_databases> mdList = DBUsersGet.getMetaDatabases(
							DCUSER, HOST_ADD);
					if (!mdList.isEmpty()) {
						String dcuser_pwd = mdList.get(0).getPassword();
						ConfirmFeatures.getInst().setDcuser_pwd(dcuser_pwd);
					}
				}

				viewTemplate = doHandleRequest(request, response, context);
			} catch (Exception e) {
				try {
					if (ENIQServiceStatusInfo.isEtlDBOffline()) {
						context.put("errorText",
								" Failed to initialize connection to database: "
										+ ENIQServiceStatusInfo.getEtlDBName());
					} else if (ENIQServiceStatusInfo.isDwhDBOffline()) {
						context.put("errorText",
								" Failed to initialize connection to database: "
										+ ENIQServiceStatusInfo.getDwhDBName());
					} else {
						context.put("errorSet", true);
						final String message = getErrorMessage(e);
						log.error("Error message:" + message);
						log.error("Exception", e);
						context.put("errorText", message);
					}
					viewTemplate = getTemplate("feature_availability_update.vm");
				} catch (final Exception e1) {
					log.error("Exception getting velocity template: ", e1);
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
