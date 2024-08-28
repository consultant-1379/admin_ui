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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.monitor.Util;

public class TechPackSelection extends EtlguiServlet {

	private final Log log = LogFactory.getLog(this.getClass());
	private transient RockFactory etlRepRockFactory = null;

	public Template doHandleRequest(final HttpServletRequest request,
			final HttpServletResponse response, final Context ctx)
			throws SQLException, ServletException, RockException {

		Template outty = null;
		etlRepRockFactory = (RockFactory) ctx.get("rockEtlRep");

		String save = request.getParameter("save");
		String set = request.getParameter("netype");

		ctx.put("ne", set);

		final String set1 = request.getParameter("ne");

		final List<String> techPack_Name;
		final List<String> techpackName;
		final List<String> techPackOption;

		techPack_Name = getTechPackListDefault(
				etlRepRockFactory.getConnection(), set);
		ctx.put("techPackDefault", techPack_Name);
		techpackName = getTechPackListMandatory(
				etlRepRockFactory.getConnection(), set);

		ctx.put("techPackMandatory", techpackName);
		techPackOption = getTechPackListOptional(
				etlRepRockFactory.getConnection(), set);

		final Map<String, String> params = new HashMap<String, String>();
		final Enumeration<?> parameters = request.getParameterNames();

		while (parameters.hasMoreElements()) {
			final String par = (String) parameters.nextElement();
			params.put(par, request.getParameter(par));
		}

		if (save != null) {
			deleteNodeAssignment(etlRepRockFactory.getConnection(), set1);

			if (params.size() > 0) {
				final Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
				while (iterator.hasNext()) {
					final Entry<String, String> entry = iterator.next();
					if (entry.getKey().startsWith("chk:")) {
						final String[] value = entry.getKey().split(":");
						final String techPack = value[1];
						if (params.get(entry.getKey()).toString().equalsIgnoreCase("on")) {
						if (save != null) {
								insertNodeAssignment(
										etlRepRockFactory.getConnection(),
										set1, techPack);
								DateFormat dateFormat = new SimpleDateFormat(
										"yyyy/MM/dd HH:mm:ss");
								Date date = new Date();
								Util.updateFRHSynctable(
										etlRepRockFactory.getConnection(),
										dateFormat.format(date));
							}
						}
					}
				}
			}
		}

		final List<String> enabled_ne = getEnabledNEType(etlRepRockFactory
				.getConnection());
		ctx.put("enablednetype", enabled_ne);

		final List<String> selectedTechPack = getSelectedTechPack(
				etlRepRockFactory.getConnection(), set);

		ctx.put("selectedOptionalTechPack", selectedTechPack);

		techPackOption.removeAll(selectedTechPack);

		ctx.put("techPackOptional", techPackOption);
		ctx.put("selectedTechPack", selectedTechPack);

		deleteTechPackMO(etlRepRockFactory.getConnection());
		deleteTechPackCF(etlRepRockFactory.getConnection());

		try {
			outty = getTemplate("techPackSelection.vm");
		} catch (Exception e) {
			throw new VelocityException(e);
		}

		return outty;
	}

	public List<String> getEnabledNEType(final Connection etlrepconn)
			throws SQLException {
		Statement statement = null;
		ResultSet result = null;
		List<String> netype = new ArrayList<String>();
		try {

			final String updateData = "SELECT DISTINCT NE_TYPE FROM META_FRH_NODE_MAPPING WHERE FRH_ENABLED_FLAG='Y' ";
			statement = etlrepconn.createStatement();
			result = statement.executeQuery(updateData);
			while (result.next()) {
				netype.add(result.getString(1));
			}
			etlrepconn.commit();
		}catch(SQLException e){
			log.error("Exception ", e);
		} 
		finally {

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

	public void deleteNodeAssignment(final Connection etlrepconn,
			final String set) throws SQLException {
		Statement statement = null;
		try {

			final String deleteData = "DELETE FROM META_FRH_NODEASSIGNMENT WHERE NE_TYPE='"
					+ set + "' ";
			statement = etlrepconn.createStatement();
			statement.executeUpdate(deleteData);
			etlrepconn.commit();
		} catch(SQLException e){
			log.error("Exception ", e);
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

	public void insertNodeAssignment(final Connection etlrepconn,
			final String set, final String techpack) throws SQLException {
		Statement statement = null;
		try {

			final String insertData = "INSERT INTO META_FRH_NODEASSIGNMENT VALUES('"
					+ set + "','" + techpack + "')";

			statement = etlrepconn.createStatement();

			statement.executeUpdate(insertData);
			etlrepconn.commit();
		} catch (SQLException e) {
			log.error("Exception ", e);
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

	public List<String> getSelectedTechPack(final Connection etlrepconn,
			final String set) throws SQLException {
		Statement statement = null;
		ResultSet result = null;
		List<String> selectedTp = new ArrayList<String>();
		try {

			final String selectData = "select distinct meta_frh_nodeassignment.techpack_name "
					+ "from meta_frh_nodeassignment join META_FRH_NODE_MAPPING "
					+ "on META_FRH_NODE_MAPPING.ne_type = meta_frh_nodeassignment.ne_type "
					+ "and META_FRH_NODE_MAPPING.TECHPACK_NAME=meta_frh_nodeassignment.TECHPACK_NAME "
					+ "where META_FRH_NODE_MAPPING.ACTIVATED=' ' and meta_frh_nodeassignment.ne_type='"
					+ set + "' ";

			statement = etlrepconn.createStatement();

			result = statement.executeQuery(selectData);
			while (result.next()) {
				selectedTp.add(result.getString(1));
			}
			etlrepconn.commit();
		} catch (SQLException e) {
			log.error("Exception ", e);
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

		return selectedTp;
	}

	public List<String> getTechPackListMandatory(final Connection etlrepconn,
			final String set) throws SQLException {

		Statement statement = null;
		ResultSet result = null;
		List<String> techPackNameMandatory = new ArrayList<String>();
		try {
			final String techPack = "SELECT TECHPACK_NAME FROM META_FRH_NODE_MAPPING WHERE NE_TYPE='"
					+ set + "'" + "AND ACTIVATED LIKE '%MANDATORY%'";
			statement = etlrepconn.createStatement();
			result = statement.executeQuery(techPack);
			while (result.next()) {
				techPackNameMandatory.add(result.getString(1));
			}

			etlrepconn.commit();
		}catch(SQLException e){
			log.error("Exception ", e);
		} 
		finally {

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
		return techPackNameMandatory;

	}

	public List<String> getTechPackListDefault(final Connection etlrepconn,
			final String set) throws SQLException {

		Statement statement = null;
		ResultSet result = null;
		List<String> techPackNameDefault = new ArrayList<String>();
		try {
			final String techPack = "SELECT TECHPACK_NAME FROM META_FRH_NODE_MAPPING WHERE NE_TYPE='"
					+ set + "'" + "AND ACTIVATED LIKE '%DEFAULT%'";
			statement = etlrepconn.createStatement();
			result = statement.executeQuery(techPack);
			while (result.next()) {
				techPackNameDefault.add(result.getString(1));
			}

			etlrepconn.commit();
		}catch(SQLException e){
			log.error("Exception ", e);
		}
		finally {

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
		return techPackNameDefault;

	}

	private List<String> getTechPackListOptional(final Connection etlrrepconn,
			final String set) throws SQLException {

		Statement statement = null;
		ResultSet result = null;
		List<String> techPackNameOption = new ArrayList<String>();
		try {

			final String techPackNameOptional = "SELECT TECHPACK_NAME FROM META_FRH_NODE_MAPPING WHERE NE_TYPE='"
					+ set + "'" + "AND ACTIVATED LIKE '% %' ";

			statement = etlrrepconn.createStatement();
			result = statement.executeQuery(techPackNameOptional);

			while (result.next()) {
				techPackNameOption.add(result.getString(1));
			}

			etlrrepconn.commit();
		}catch(SQLException e){
			log.error("Exception ", e);
		} 
		finally {

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
		return techPackNameOption;

	}

	public void deleteTechPackMO(final Connection etlrrepconn)
			throws SQLException {
		Statement statement = null;
		try {
			final String deleteTP = "delete meta_frh_moaggregation from meta_frh_moaggregation as m left join meta_frh_nodeassignment as n on m.ne_type = n.ne_type and m.techpack_name = n.techpack_name where n.ne_type = null and n.techpack_name = null";
			statement = etlrrepconn.createStatement();
			statement.executeUpdate(deleteTP);
			etlrrepconn.commit();
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

	public void deleteTechPackCF(final Connection etlrrepconn)
			throws SQLException {
		Statement statement = null;
		try {
			final String deleteTechPack = "delete meta_frh_counterfiltering from meta_frh_counterfiltering as c left join meta_frh_nodeassignment as n on c.ne_type = n.ne_type and c.techpack_name = n.techpack_name where n.ne_type = null and n.techpack_name = null";
			statement = etlrrepconn.createStatement();
			statement.executeUpdate(deleteTechPack);
			etlrrepconn.commit();
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

}*/