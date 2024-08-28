package com.distocraft.dc5000.etl.gui.etl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

import ssc.rockfactory.RockFactory;

public class FRHLoggingConfig extends EtlguiServlet {
	private final Log log = LogFactory.getLog(this.getClass()); // general
																// logger

	private transient RockFactory etlrepRockFactory = null;

	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {

		Template outty = null;

		final String page = "frh_logging_level.vm";
		String ctrlLevel = null;
		String flowLevel = null;
		String mName = null;

		final String save = request.getParameter("save");
		etlrepRockFactory = (RockFactory) ctx.get("rockEtlRep");
		if (save != null) {
			ctrlLevel = request.getParameter("CONTROLLER");
			flowLevel = request.getParameter("FLOW");
			Enumeration<String> module = request.getParameterNames();
			while (module.hasMoreElements()) {
				mName = (String) module.nextElement();
				//log.info("calling saveLoggingLevel with module name= "+mName);
				if(mName.equalsIgnoreCase("controller") || mName.equalsIgnoreCase("flow"))
				{
					log.info("calling saveLoggingLevel with module name= "+mName);
				final boolean saveSuccess = saveLoggingLevel(request.getParameter(mName), etlrepRockFactory.getConnection(),
						mName);

				if (!saveSuccess) {
					ctx.put("errorMessage", "Error saving modified log levels");
				}
				}
			}
		}
		ctx.put("logLevels", getLoggingLevels());
		ctx.put("ctrlLevel", ctrlLevel);
		ctx.put("flowLevel", flowLevel);
		outty = getTemplate(page);

		return outty;
	}

	private boolean saveLoggingLevel(String logLevel, Connection conn, String mouduleName) {

		boolean isSucc = false;
		int count = -1;

		ResultSet rset = null;

		Statement st1 = null;
		Statement st2 = null;
		Statement st4 = null;

		try {

			final String sql1 = "SELECT COUNT (*) FROM META_FRH_LOG_LEVEL WHERE MODULE_NAME LIKE '"+mouduleName+"'";
			log.info(sql1);
			st1 = conn.createStatement();
			rset = st1.executeQuery(sql1);
			while (rset.next()) {
				count = rset.getInt(1);
			}
			if (count == 0) {
				st4 = conn.createStatement();
					final String sql4 = "INSERT INTO META_FRH_LOG_LEVEL VALUES('"+mouduleName+"','" + logLevel + "')";
					log.info(sql4);
					st4 = conn.createStatement();
					st4.executeUpdate(sql4);
				isSucc = true;
			} else {
				st2 = conn.createStatement();
				//st3 = conn.createStatement();
				
					final String sql2 = "UPDATE META_FRH_LOG_LEVEL SET LOG_LEVEL='" + logLevel
							+ "' WHERE MODULE_NAME LIKE '"+mouduleName+"'";
				log.info(sql2);
				st2.executeUpdate(sql2);
				conn.commit();
				isSucc = true;
			}
		} catch (final Exception e) {
			log.error("Exception: ", e);
			isSucc = false;
		} finally {
			try {
				if (st1 != null) {
					st1.close();
				}
				if (st2 != null) {
					st2.close();
				}
				if (st4 != null) {
					st4.close();
				}
				if (st4 != null) {
					st4.close();
				}
			} catch (final Exception e) {
				log.error("Exception: ", e);
				isSucc = false;
			}
		}
		return isSucc;

	}

	private List<String> getLoggingLevels() {

		final ArrayList<String> lgl = new ArrayList<String>();
		lgl.add("INFO");
		lgl.add("DEBUG");
		lgl.add("WARN");
		lgl.add("ERROR");

		return lgl;
	}

}
