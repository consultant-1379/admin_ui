/*package com.distocraft.dc5000.etl.gui.monitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.monitor.Util;

public class CounterFilteringDetails extends EtlguiServlet {

	private transient RockFactory dwhrepRockFactory = null;
	private transient RockFactory etlRepRockFactory = null;
	private final Log log = LogFactory.getLog(this.getClass());

	public Template doHandleRequest(final HttpServletRequest request,
			final HttpServletResponse response, final Context ctx)
			throws SQLException, RockException, ServletException {

		Template outty = null;

		dwhrepRockFactory = (RockFactory) ctx.get("rockDwhRep");
		etlRepRockFactory = (RockFactory) ctx.get("rockEtlRep");
		
		final String activate = StringEscapeUtils.escapeHtml(request.getParameter("activate"));
		final String typeName = request.getParameter("typeName");
		ctx.put("typeName", typeName);
		final String s = request.getParameter("st");
		ctx.put("st", s);
		final String search = request.getParameter("search");
		final String techPack = request.getParameter("package");

		if (dwhrepRockFactory == null || etlRepRockFactory == null) {
			ctx.put("errorMessage", "Ensure all databases are online.");
		} else {

			final Map<String, String> params = new HashMap<String, String>();

			final Enumeration<?> parameters = request.getParameterNames();

			while (parameters.hasMoreElements()) {
				final String par = (String) parameters.nextElement();
				params.put(par, request.getParameter(par));
			}

			if (activate != null) {
				if (params.size() > 0) {
					deletecountertable(etlRepRockFactory.getConnection(), s,typeName);
					final Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
					while (iterator.hasNext()) {
						final Entry<String, String> entry = iterator.next();
						if (entry.getKey().startsWith("chk:")) {
							final String[] temp = entry.getKey().split(":");
							final String tn = temp[1];
							if (params.get(entry.getKey()).toString().equalsIgnoreCase("on")) {
							if (activate != null) {
									ctx.put("typeName", typeName);
									ctx.put("st", s);
									updateCountertable(etlRepRockFactory.getConnection(),dwhrepRockFactory.getConnection(),typeName, s, tn);
									DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
									Date date = new Date();
									Util.updateFRHSynctable(etlRepRockFactory.getConnection(),dateFormat.format(date));
								}

							}
						}
					}

				}
			}
			
			ctx.put("st", s);
			ctx.put("package", techPack);
			ctx.put("search", search);
			
			final String t = request.getParameter("package");
			final List<String> counter = Util.getCounters(typeName,search,t,dwhrepRockFactory.getConnection());
			final List<String> cfcounter = Util.getCFCounters(typeName, s, etlRepRockFactory.getConnection());

			counter.removeAll(cfcounter);
			ctx.put("cfcounter", cfcounter);
			ctx.put("distinctcounters", counter);

		}

		try {
			outty = getTemplate("CounterFilteringDetails.vm");
		} catch (Exception e) {
			throw new VelocityException(e);
		}

		return outty;
	}

	private void updateCountertable(final Connection etlrepcon,
			final Connection repCon, final String typeName,
			final String netype, final String tn) throws SQLException {

		Statement statement = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			
			final String sql1 = "SELECT DISTINCT TYPEID FROM MEASUREMENTCOUNTER WHERE DATANAME LIKE '"
					+ tn + "' AND TYPEID LIKE '%" + typeName + "' ";
			statement = repCon.createStatement();
			rs = statement.executeQuery(sql1);
			while (rs.next()) {
				String result1 = rs.getString("typeid");
				final String[] temp = result1.split(":");
				String t1 = temp[0];
				String t3 = temp[2];
				
				try {
					st = etlrepcon.createStatement();
					final String sql3 = "INSERT INTO META_FRH_COUNTERFILTERING(NE_TYPE,TECHPACK_NAME,MANAGED_OBJECTS,COUNTER)VALUES('"+ netype+ "','"+ t1+ "',"
							+"'"+ t3+ "','"+ tn+ "')";
					st.executeUpdate(sql3);					
					etlrepcon.commit();
				} catch (SQLException e) {
					log.error("SQLException ", e);
				}
			}
			repCon.commit();

		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				log.error("Exception ", e);
			}
		}
	}

	private void deletecountertable(final Connection repCon,
			final String netype, final String typeName) throws SQLException {

		Statement statement = null;

		try {
			final String sql1 = "DELETE FROM META_FRH_COUNTERFILTERING WHERE NE_TYPE='"
					+ netype + "' AND MANAGED_OBJECTS ='" + typeName + "' ";
			statement = repCon.createStatement();
			statement.executeUpdate(sql1);
			repCon.commit();
		} catch (SQLException e) {
			log.error("SQLException ", e);
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				log.error("Exception ", e);
			}
		}
	}

}
*/