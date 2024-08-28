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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import ssc.rockfactory.RockFactory;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.monitor.Util;

public class NodeAssignment extends EtlguiServlet {

	private transient RockFactory etlRepRockFactory = null;
	private transient RockFactory dwhrepRockFactory = null;
	private final Log log = LogFactory.getLog(this.getClass());

	public Template doHandleRequest(final HttpServletRequest request,
			final HttpServletResponse response, final Context ctx)
			throws Exception {

		Template template = null;

		etlRepRockFactory = (RockFactory) ctx.get("rockEtlRep");
		dwhrepRockFactory = (RockFactory) ctx.get("rockDwhRep");

		final String selectnetype = request.getParameter("selectnetype");

		final Map<String, String> params = new HashMap<String, String>();
		final Enumeration<?> parameters = request.getParameterNames();

		while (parameters.hasMoreElements()) {
			final String par = (String) parameters.nextElement();
			params.put(par, request.getParameter(par));
		}
		if (etlRepRockFactory == null) {
			ctx.put("errorMessage", "Ensure all databases are online.");
			log.debug("etlRepRockFactory Connection is " + etlRepRockFactory);
		} else {
			List<String> NETypes = new ArrayList<String>();
			NETypes = getNETypes(etlRepRockFactory.getConnection());
			while (parameters.hasMoreElements()) {
				final String par = (String) parameters.nextElement();
				params.put(par, request.getParameter(par));
			}

			if (selectnetype != null) {
				updateEnabledFlag(etlRepRockFactory.getConnection());
				if (params.size() > 0) {
					final Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
					while (iterator.hasNext()) {
						final Entry<String, String> entry = iterator.next();
						if (entry.getKey().startsWith("chk:")) {
							final String[] value = entry.getKey().split(":");
							final String ne = value[2];
							if (params.get(entry.getKey()).toString().equalsIgnoreCase("on")) {
								if (selectnetype != null) {
									updateMapTable(etlRepRockFactory.getConnection(),ne);
									DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
									Date date = new Date();
									Util.updateFRHSynctable(etlRepRockFactory.getConnection(),dateFormat.format(date));

								}
							}
						}
					}
				}

			}

			deleteDisabledNA(etlRepRockFactory.getConnection());
			deleteDisabledMO(etlRepRockFactory.getConnection());
			deleteDisabledCF(etlRepRockFactory.getConnection());

			final List<String> enabled_ne = getEnabledNEType(etlRepRockFactory.getConnection());
			ctx.put("enablednetype", enabled_ne);

			NETypes.removeAll(enabled_ne);
			ctx.put("NETYPES", NETypes);

		}

		try {
			template = getTemplate("nodeassignment.vm");
		} catch (Exception e) {
			throw new VelocityException(e);
		}

		return template;
	}

	public List<String> getNETypes(final Connection etlrepconn)throws SQLException {
		Statement statement = null;
		ResultSet result = null;
		List<String> neType = new ArrayList<String>();
		try {
			final String netype = "SELECT DISTINCT NE_TYPE FROM META_FRH_NODE_MAPPING";
			statement = etlrepconn.createStatement();
			result = statement.executeQuery(netype);
			while (result.next()) {
				neType.add(result.getString(1));
			}
			etlrepconn.commit();
		}catch(SQLException e){
			log.error("Exception ", e);
		}finally {

			try {
				if (result != null) {
					result.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				log.error("Exception ", e);
			}

		}
		return neType;
	}
	
	public void updateEnabledFlag(final Connection etlrepconn)throws SQLException {
		Statement statement = null;
		try {
			final String updateData = "UPDATE META_FRH_NODE_MAPPING SET FRH_ENABLED_FLAG=null";
			statement = etlrepconn.createStatement();
			statement.executeUpdate(updateData);
			etlrepconn.commit();
		}catch(SQLException e){
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

	public void updateMapTable(final Connection etlrepconn, final String ne)throws SQLException {
		Statement statement = null;
		try {
			final String updateData = "UPDATE META_FRH_NODE_MAPPING SET FRH_ENABLED_FLAG='Y' WHERE NE_TYPE='"
					+ ne + "' ";
			statement = etlrepconn.createStatement();
			statement.executeUpdate(updateData);
			etlrepconn.commit();
		} catch(SQLException e){
			log.error("SQLException ", e);
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}
				
			} catch (Exception e) {
				log.error("Exception ", e);
			}

		}
	}

	public List<String> getEnabledNEType(final Connection etlrepconn)throws SQLException {
		Statement statement = null;
		ResultSet result = null;
		List<String> netype = new ArrayList<String>();
		try {
			final String updateData = "select distinct ne_type from META_FRH_NODE_MAPPING where FRH_ENABLED_FLAG='Y' ";
			statement = etlrepconn.createStatement();
			result = statement.executeQuery(updateData);
			while (result.next()) {
				netype.add(result.getString(1));
			}
			etlrepconn.commit();
		}catch (SQLException e) {
			log.error("SQLException ", e);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
				if (statement != null) {
					statement.close();
				}
				
			} catch (Exception e) {
				log.error("Exception ", e);
			}
		}
		return netype;
	}

	public void deleteDisabledNA(final Connection etlrepconn)throws SQLException {
		Statement statement = null;
		try {
			
			final String deleteDisabledData = "delete meta_frh_nodeassignment from meta_frh_nodeassignment join META_FRH_NODE_MAPPING on META_FRH_NODE_MAPPING.ne_type = meta_frh_nodeassignment.ne_type where frh_enabled_flag=null";
			statement = etlrepconn.createStatement();
			statement.executeUpdate(deleteDisabledData);
			etlrepconn.commit();
		}catch (SQLException e) {
			log.error("SQLException ", e);
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}

			} catch (Exception e) {
				log.error("Exception ", e);
			}
		}
	}

	public void deleteDisabledMO(final Connection etlrepconn)throws SQLException {
		Statement statement = null;
		try {
			final String deleteDisabledData = "delete meta_frh_moaggregation from meta_frh_moaggregation join META_FRH_NODE_MAPPING on META_FRH_NODE_MAPPING.ne_type = meta_frh_moaggregation.ne_type where frh_enabled_flag=null";
			statement = etlrepconn.createStatement();
			statement.executeUpdate(deleteDisabledData);
			etlrepconn.commit();
		}catch (SQLException e) {
			log.error("SQLException ", e);
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}

			} catch (Exception e) {
				log.error("Exception ", e);
			}
		}
	}

	public void deleteDisabledCF(final Connection etlrepconn)throws SQLException {
		Statement statement = null;
		try {
			final String deleteDisabledData = "delete meta_frh_counterfiltering from meta_frh_counterfiltering join META_FRH_NODE_MAPPING on META_FRH_NODE_MAPPING.ne_type = meta_frh_counterfiltering.ne_type where frh_enabled_flag=null";
			statement = etlrepconn.createStatement();
			statement.executeUpdate(deleteDisabledData);
			etlrepconn.commit();
		}catch (SQLException e) {
			log.error("SQLException ", e);
		}
		finally {
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