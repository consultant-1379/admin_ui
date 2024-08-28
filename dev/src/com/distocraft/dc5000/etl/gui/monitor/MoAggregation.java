/*package com.distocraft.dc5000.etl.gui.monitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class MoAggregation extends EtlguiServlet {

	private transient RockFactory dwhrepRockFactory = null;
	private transient RockFactory etlRepRockFactory = null;
	private final Log log = LogFactory.getLog(this.getClass());

	public Template doHandleRequest(final HttpServletRequest request,
			final HttpServletResponse response, final Context ctx)
			throws SQLException, ServletException, RockException {

		Template outty = null;

		dwhrepRockFactory = (RockFactory) ctx.get("rockDwhRep");
		etlRepRockFactory = (RockFactory) ctx.get("rockEtlRep");
		
		String neType = request.getParameter("st");

		String techpack = request.getParameter("pack");

		String search = StringEscapeUtils.escapeHtml(request
				.getParameter("search"));

		String pattern = "^[a-zA-Z0-9---_]*$";

		if (techpack == null) {
			techpack = "-";
		}

		if (techpack.matches(pattern)) {
			techpack = StringEscapeUtils.escapeHtml(techpack);
		} else {
			techpack = null;
		}

		final String activateSelected = StringEscapeUtils.escapeHtml(request
				.getParameter("activateSelected"));

		final Map<String, String> params = new HashMap<String, String>();

		final Enumeration<?> parameters = request.getParameterNames();

		while (parameters.hasMoreElements()) {
			final String par = (String) parameters.nextElement();
			params.put(par, request.getParameter(par));
		}

		ctx.put("st", neType);
		ctx.put("pack", techpack);
		
		if (etlRepRockFactory == null || dwhrepRockFactory == null) {
			ctx.put("errorMessage", "Ensure all databases are online.");

		} else {

			final List<String> netype = Util.getFRHNETypes(etlRepRockFactory
					.getConnection());
			ctx.put("distinctnetype", netype);

			final List<String> tps = Util.getMOType(etlRepRockFactory.getConnection(),dwhrepRockFactory.getConnection(), neType);
			ctx.put("distinctTechPacks", tps);

			if (techpack != null) {
				if (activateSelected != null) {

					deleteMOAggregationtable(etlRepRockFactory.getConnection(),neType, techpack);
			
					if (params.size() > 0) {
						final Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
						while (iterator.hasNext()) {
							final Entry<String, String> entry = iterator.next();
							if (entry.getKey().startsWith("chk:")) {
								final String[] value = entry.getKey().split(":");
								final String managedObject = value[1];
								if (params.get(entry.getKey()).toString().equalsIgnoreCase("on")) {
									if (activateSelected != null) {
										insertAggregationtable(etlRepRockFactory.getConnection(),neType, techpack, managedObject);
										DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
										Date date = new Date();
										Util.updateFRHSynctable(etlRepRockFactory.getConnection(),dateFormat.format(date));
									}

								}

							}

						}
					}

				}

			}
			if (search != null) {

				final List<String> motype = getMOData(techpack, search,dwhrepRockFactory.getConnection());
				final List<String> mo_type = getMO_Data(techpack,search,neType, etlRepRockFactory.getConnection());
				if (techpack != null && !techpack.equals("-")) {
					motype.removeAll(mo_type);
					ctx.put("timeBasedMeasTypes", mo_type);
					ctx.put("volumeBasedMeasTypes", motype);

				}
			}

		}

		try {
			outty = getTemplate("frop.vm");
		} catch (Exception e) {
			throw new VelocityException(e);
		}

		return outty;
	}

	private void insertAggregationtable(final Connection etlrepconn,
			final String netype, final String techpack,
			final String managedObject) throws SQLException {

		Statement statement = null;

		try {
			final String sql = "INSERT INTO META_FRH_MOAGGREGATION(NE_TYPE,TECHPACK_NAME,MANAGED_OBJECTS)VALUES('"
					+ netype + "','" + techpack + "','" + managedObject + "') ";
			statement = etlrepconn.createStatement();
			statement.executeUpdate(sql);
			etlrepconn.commit();
		}catch(SQLException e){
			log.error("SQLException ", e);
		}finally {
			try{
				if (statement != null) {
					statement.close();
				}
			}catch (Exception e) {
				log.error("Exception ", e);
			}
		}

	}

	private void deleteMOAggregationtable(final Connection repCon,
			String netype, String techpack) throws SQLException {

		Statement statement = null;

		try {
			final String sql1 = "DELETE FROM META_FRH_MOAggregation WHERE NE_TYPE='"
					+ netype + "' and TECHPACK_NAME='" + techpack + "' ";
			statement = repCon.createStatement();
			statement.executeUpdate(sql1);
			repCon.commit();
		} catch(SQLException e ){
			log.error("SQLException ", e);
		}finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				log.error("Exception ", e);
			}
		}
	}

	public List<String> getMOData(final String techpack, final String search,
			final Connection conn) {

		final List<String> ret = new ArrayList<String>();
		Statement statement = null;
		ResultSet result = null;

		try {
			statement = conn.createStatement();
			final String sql = "SELECT DISTINCT TYPENAME FROM TYPEACTIVATION WHERE TECHPACK_NAME ='"
					+ techpack
					+ "' AND TYPENAME LIKE 'DC_%' AND TYPENAME LIKE '%"
					+ search + "%' ";
			result = statement.executeQuery(sql);
			while (result.next()) {
				ret.add(result.getString(1));
			}
			conn.commit();
		} catch (SQLException e) {
			log.error("SQLException: ", e);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				log.error("Exception: ", e);

			}
		}
		return ret;
	}

	public List<String> getMO_Data(final String techpack, final String search,
			final String netype, final Connection conn) {

		final List<String> ret = new ArrayList<String>();
		Statement statement = null;
		ResultSet result = null;

		try {
			statement = conn.createStatement();
			final String sql = "SELECT DISTINCT MANAGED_OBJECTS FROM META_FRH_MOAGGREGATION WHERE NE_TYPE = '"
					+ netype
					+ "' AND TECHPACK_NAME ='"
					+ techpack
					+ "' AND MANAGED_OBJECTS like 'DC_%' AND MANAGED_OBJECTS LIKE '%"
					+ search + "%' ";			
			result = statement.executeQuery(sql);
			while (result.next()) {
				ret.add(result.getString(1));
			}
			conn.commit();
		} catch (SQLException e) {
			log.error("SQLException: ", e);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				log.error("Exception: ", e);
			}
		}
		return ret;
	}
	
}*/