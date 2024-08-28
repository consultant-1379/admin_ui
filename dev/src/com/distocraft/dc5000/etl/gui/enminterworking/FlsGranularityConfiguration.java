package com.distocraft.dc5000.etl.gui.enminterworking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.etl.gui.info.AdminuiInfo;
import com.ericsson.eniq.common.DatabaseConnections;
import com.ericsson.eniq.common.RemoteExecutor;
import com.jcraft.jsch.JSchException;

public class FlsGranularityConfiguration extends EtlguiServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(FlsGranularityConfiguration.class);

	private LinkedHashSet<String> technologyList = new LinkedHashSet<>();

	private LinkedHashMap<String, String> hm = new LinkedHashMap<String, String>();
	
	private static final String APPLICATION_USER = "dcuser";

	protected static final String HOST_ADD = "scheduler";

	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws ResourceNotFoundException, ParseErrorException{

		//response.setHeader("Cache-Control", "private");
		
		

		LinkedHashMap<String, ArrayList<TableList>> newGranularityList = new LinkedHashMap<String, ArrayList<TableList>>();
		
		final LinkedHashMap<String, ArrayList<TableList>> granularityList = new LinkedHashMap<>();

		final List<String> timeList = new ArrayList<String>();

		String continue_response = StringEscapeUtils.escapeHtml(request.getParameter("continue"));

		String submit_response = StringEscapeUtils.escapeHtml(request.getParameter("submit"));

		String back_response = StringEscapeUtils.escapeHtml(request.getParameter("back"));
		
		String script_response = StringEscapeUtils.escapeHtml(request.getParameter("flsScript"));
		
		boolean continue_flag = false;

		boolean back_flag = false;
		
		Template template = null;

		final String page = "granularityConfig.vm";

		// Flag when db is down and not able to fetch the table values
		boolean errorMessage = false;
		
		final HttpSession session = request.getSession();
		String username =  (String) session.getAttribute("username");
	    	log.info("user in FLSGranularityConfiguration="+username);
	
	    		String pathInfo =request.getRequestURI();
		log.info("path Info of  FLSGranularityConfiguration="+pathInfo);
		String ipAddress = request.getRemoteAddr();
    log.info("IpAddress in FLSGranularityConfiguration="+ipAddress);
		
		// To populate NodeTypeGranularity table
		if (script_response != null) {
			File f = new File("/eniq/sw/installer/node_type_granularity.bsh");
			if (f.exists()) {
				String populateTable = "bash /eniq/sw/installer/node_type_granularity.bsh -p /eniq/sw/installer";
				String a = null;
				try {
					a = RemoteExecutor.executeComandSshKey(APPLICATION_USER, HOST_ADD, populateTable);
				} catch (JSchException | IOException e) {
					log.warn("Error while populating nodetypegranularity table " + e);
				}
			} else {
				ctx.put("scriptError", true);
			}
		}
		// To display the node types whose configured granularity has been changed.
		if (continue_response != null) {
			continue_flag = true;
			final LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();

			final List<String> parameters = new ArrayList<String>(request.getParameterMap().keySet());

			Iterator<String> itr = parameters.iterator();
			if (!hm.isEmpty())
				hm.clear();
			// get all parameters to map
			while (itr.hasNext()) {
				String par = itr.next();
				if (!par.equalsIgnoreCase("continue")) {
					String value = request.getParameter(par);
					params.put(par, value);
					hm.put(par, value);
				}
			}
			log.debug("Parameters fetched on continue button click : " + params);
			HashMap<String, ArrayList<String>> fileMap = nodeTypesToBlock();
			LinkedHashMap<String, ArrayList<TableList>> list = displayChangedNodes(params,fileMap,  ctx,username,pathInfo,ipAddress);
			log.debug("Node type list before submitting : " + list);
			if (list.isEmpty())
				continue_flag = false;
			ctx.put("flag", continue_flag);
			log.info("Displaying the changed granularity of different node types");
			continue_response = null;
		}

		// To retain the changed granularity and show it on the main page.
		// Updating hm on continue and using hm to populate newGranularityList
		if (back_response != null) {
			if (!(hm.isEmpty() && technologyList.isEmpty())) {
				back_flag = true;
				Iterator<String> techItr = technologyList.iterator();
				while (techItr.hasNext()) {
					String tech = techItr.next();
					Iterator<String> mapItr = hm.keySet().iterator();
					ArrayList<TableList> list = new ArrayList<TableList>();
					while (mapItr.hasNext()) {
						String node = mapItr.next();
						String[] str = node.split("::");
						if (tech.equalsIgnoreCase(str[0])) {
							String newValue = hm.get(node);
							TableList t = new TableList();
							t.setNodeType(str[1]);
							t.setDefGranularity(str[2]);
							t.setConfGranularity(newValue);
							list.add(t);
						}
					}
					if (!list.isEmpty())
						newGranularityList.put(tech, list);
				}
				ctx.put("back_flag", back_flag);
				log.debug("New List after retention of configured granularity : " + newGranularityList);
				log.info("Retaining the changed granularities and displaying changed list");
			}
			back_response = null;
		}

		// To update the NodeTypeGranularity table
		if (submit_response != null) {
			final LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();

			final List<String> parameters = new ArrayList<String>(request.getParameterMap().keySet());

			Iterator<String> itr = parameters.iterator();

			while (itr.hasNext()) {
				final String par = itr.next();
				if (!par.equalsIgnoreCase("submit"))
					params.put(par, request.getParameter(par));
			}
			log.debug("Parameters fetched on submit button click : " + params);
			log.info("Updating the NodeTypeGranularity Table.");
			updateGranularityTable(params, ctx);
			submit_response = null;
		}

		Connection connDwhrep = null;
		Connection connDwhdb = null;
		connDwhdb = DatabaseConnections.getDwhDBConnection().getConnection();
		connDwhrep = DatabaseConnections.getDwhRepConnection().getConnection();

		String sqlTechnology = "select technology from NodeTypeGranularity ORDER BY TECHNOLOGY";
		String sqlTime = "select DURATIONMIN from DIM_TIMELEVEL where TABLELEVEL='RAW' and DESCRIPTION not in('10 min','6 Hours')";
		
		Statement stmtDwhrep = null;
		Statement stmtDwhdb = null;
		ResultSet rsTime = null;
		ResultSet rsTech = null;
		
		// To display the drop-down values in adminui
		try {
			stmtDwhdb = connDwhdb.createStatement();
			rsTime = stmtDwhdb.executeQuery(sqlTime);
			while (rsTime.next()) {
				int time = rsTime.getInt(1);
				if (time < 60)
					timeList.add(time + "MIN");
				else {
					time = time / 60;
					timeList.add(time + "HOUR");
				}
			}
			ctx.put("TimeData", timeList);
			log.debug("DropDown list values: " + timeList);
		} catch (SQLException e) {
			errorMessage = true;
			ctx.put("errorMessage", errorMessage);
			log.warn(("Unable to query DIM_TIMELEVEL table" + e));
		} finally {
			try {
				if (connDwhdb != null)
					connDwhdb.close();
				if (stmtDwhdb != null)
					stmtDwhdb.close();
				if (rsTime != null)
					rsTime.close();
			} catch (SQLException e) {
				log.warn("Unable to close database connections " + e);
			}
		}
		
		// To display NodeTypeGranularity Table
		try {
			boolean result = false;
			stmtDwhrep = connDwhrep.createStatement();
			rsTech = stmtDwhrep.executeQuery(sqlTechnology);
			while (rsTech.next()) {
				technologyList.add(rsTech.getString(1));
				result = true;
			}
			log.debug("Technology List inside NodeTypeGranularity table: " + technologyList);
			Iterator<String> techItr = technologyList.iterator();
			while (techItr.hasNext()) {
				ResultSet rsGranularity = null;
				ArrayList<TableList> list = new ArrayList<TableList>();
				String tech = techItr.next();
				String sqlGranularity = "select Node_Type,Default_Granularity,Configured_Granularity from NodeTypeGranularity where Technology='"
						+ tech + "' ORDER BY Node_Type ASC";
				try {
					rsGranularity = stmtDwhrep.executeQuery(sqlGranularity);
					while (rsGranularity.next()) {
						TableList t = new TableList();
						t.setNodeType(rsGranularity.getString(1));
						t.setDefGranularity(rsGranularity.getString(2));
						t.setConfGranularity(rsGranularity.getString(3));
						list.add(t);
					}
					granularityList.put(tech, list);
				} catch (SQLException e) {
					log.warn("Error while fetching the NodeTypeGranularity Table " + e);
				} finally {
					if (rsGranularity != null)
						rsGranularity.close();
				}
			}
			ctx.put("dataNotFoundError", result);
			if (!back_flag)
				ctx.put("TableData", granularityList);
			else {
				ctx.put("TableData", newGranularityList);
				back_flag = false;
			}

		} catch (SQLException e) {
			errorMessage = true;
			ctx.put("errorMessage", errorMessage);
			log.warn("Error while fetching the NodeTypeGranularity Table " + e);
		} finally {
			try {
				if (connDwhrep != null)
					connDwhrep.close();
				if (stmtDwhrep != null)
					stmtDwhrep.close();
				if (rsTech != null)
					rsTech.close();
			} catch (SQLException e2) {
				log.warn("Unable to close dwhrep connection " + e2);
			}
		}

		try {
			template = getTemplate(page);
		} catch (Exception e) {
			log.warn("Granularity config velocity template not found " + e);
		}
		return template;
	}

	// Reading file to block some node_types to have different granularity
	protected HashMap<String, ArrayList<String>> nodeTypesToBlock() {
		HashMap<String, ArrayList<String>> fileMap = new LinkedHashMap<String, ArrayList<String>>();
		BufferedReader reader = null;
		Connection connDwhrep = null;
		connDwhrep = DatabaseConnections.getDwhRepConnection().getConnection();
		Statement stmtDwhrep = null;
		try {
			reader = new BufferedReader(new FileReader("/eniq/sw/installer/NodeDataMapping.properties"));
			String line = reader.readLine();
			int value = 1;
			String result = line.replace(" ", "").split("=")[1];
			if(result.equalsIgnoreCase("false")) {
				reader.close();
				return fileMap;
			}
			while (line != null) {
				String[] arr = line.replace(" ", "").split("=");
				List<String> nodeTypes = Arrays.asList(arr[1].replace(" ", "").split(","));
				Iterator<String> itr = nodeTypes.iterator();
				while (itr.hasNext()) {
					String nodeType = itr.next();
					ArrayList<String> techPlusAlias = new ArrayList<String>();
					techPlusAlias.add(value + "");
					if (nodeType.contains(":")) {
						String[] arr2 = nodeType.split(":");
						String alias = "";
						ArrayList<String> techPlusAlias1 = new ArrayList<String>();
						if (fileMap.containsKey(arr2[0]) && fileMap.containsKey(arr2[1])) {
						} else if (fileMap.containsKey(arr2[0])) {
							alias = fileMap.get(arr2[0]).get(0);
							techPlusAlias1.add(alias + "");
							fileMap.put(arr2[1], techPlusAlias1);
						} else if (fileMap.containsKey(arr2[1])) {
							alias = fileMap.get(arr2[1]).get(0);
							techPlusAlias1.add(alias + "");
							fileMap.put(arr2[0], techPlusAlias1);
						} else {
							fileMap.put(arr2[0], techPlusAlias);
							techPlusAlias1.add(value + "");
							fileMap.put(arr2[1], techPlusAlias1);
						}
						value++;
						
					} else {
						if (fileMap.containsKey(nodeType)) {
							String alias = fileMap.get(nodeType).get(0);
							Iterator<String> itr1 = nodeTypes.iterator();
							while (itr1.hasNext()) {
								ArrayList<String> techPlusAlias1 = new ArrayList<String>();
								techPlusAlias1.add(alias + "");
								fileMap.put(itr1.next(), techPlusAlias1);
							}
							break;
						}
						else {
							fileMap.put(nodeType, techPlusAlias);
						}
					}
				}
				value++;
				line = reader.readLine();
			}
		} catch (FileNotFoundException e1) {
			log.debug("File not found" + e1);
		} catch (IOException e) {
			log.warn("Unable to read file: " + e);
		}
		Iterator<Map.Entry<String, ArrayList<String>>> fileItr = fileMap.entrySet().iterator();
		try {
			stmtDwhrep = connDwhrep.createStatement();
			while(fileItr.hasNext()) {
				Map.Entry<String, ArrayList<String>> pair = fileItr.next();
				String sqlTech = "select technology from nodetypegranularity where node_type='" + pair.getKey()
						+ "' order by technology";
				String technology = "";
				ResultSet rsTech = null;
				try {
					rsTech = stmtDwhrep.executeQuery(sqlTech);
					while(rsTech.next()) {
						technology += rsTech.getString(1)+",";
					}
					if(technology == "")
						fileItr.remove();
					else {
						technology = technology.substring(0, technology.length()-1);
						ArrayList<String> value = pair.getValue();
						value.add(1, technology);
					}
				} finally {
					try {
						if (rsTech != null)
							rsTech.close();
					} catch (SQLException e) {
						log.warn("Unable to close resultSet: " + e);
					}
				}
			}
		} catch (SQLException e) {
			log.warn("Unable to create stmtDwhrep connection: " + e);
		} finally {
			try {
				if (connDwhrep != null)
					connDwhrep.close();
				if (stmtDwhrep != null)
					stmtDwhrep.close();
				if (reader != null)
					reader.close();
			} catch (SQLException | IOException e) {
				log.warn("Unable to close dwhrep connection: " + e);
			}
		}
		log.info("Node types to block: " + fileMap);
		log.debug("Node types to block: " + fileMap);
		return fileMap;
	}
	// To check the changed granularity and displaying it after continue is clicked
	// for confirmation
	protected LinkedHashMap<String, ArrayList<TableList>> displayChangedNodes(LinkedHashMap<String, String> newList,
			HashMap<String, ArrayList<String>> fileMap, Context ctx,String username,String pathInfo,String ipAddress ) {
		Connection connDwhrep = null;
		connDwhrep = DatabaseConnections.getDwhRepConnection().getConnection();
		final File file = new File("/eniq/sw/installer/NodeDataMapping.properties");
		boolean exist = false;
		if(file.exists() && !fileMap.isEmpty())
			exist = true;
		LinkedHashMap<String, String> changedMap = new LinkedHashMap<>();
		Iterator<String> techItr = technologyList.iterator();
		LinkedHashMap<String, ArrayList<TableList>> updatedList = new LinkedHashMap<String, ArrayList<TableList>>();
		Statement stmtDwhrep = null;
		try {
			stmtDwhrep = connDwhrep.createStatement();
		} catch (SQLException e1) {
			log.warn("Unable to create dwhrep connection " + e1);
		}

		try {
			Iterator<String> itr = newList.keySet().iterator();
			while (itr.hasNext()) {
				ResultSet rsGranularity = null;
				String node = itr.next();
				String[] str = node.split("::");
				String newValue = newList.get(node);
				String sqlGranularity = "select Configured_Granularity FROM NodeTypeGranularity WHERE Technology='"
						+ str[0] + "' AND Node_Type='" + str[1] + "'";
				try {
					rsGranularity = stmtDwhrep.executeQuery(sqlGranularity);
					while (rsGranularity.next()) {
						String oldValue = rsGranularity.getString(1);
						if (!(oldValue.equals(newValue))) {
							newValueandoldValue(fileMap, exist, changedMap, str, newValue, oldValue);
							AdminuiInfo.logFlsGranularity(username, ipAddress,pathInfo,oldValue, newValue);		
						
				            }
				}
					}
				
				catch (SQLException e) {
					log.warn("Unable to fetch NodeTypeGranularity table: " + e);
				} finally {
					if (rsGranularity != null)
						rsGranularity.close();
				}
			}
		} catch (SQLException e) {
			log.warn("Unable to fetch NodeTypeGranularity table: " + e);
		} finally {
			extracted(connDwhrep, stmtDwhrep);
		}
		log.info("Confirmation list of node types with changed granularity  : " + changedMap);
	
		log.debug("Confirmation list of node types with changed granularity: "+changedMap);
		return linkedHashmap(ctx, changedMap, techItr, updatedList);
	}

	private void newValueandoldValue(HashMap<String, ArrayList<String>> fileMap, boolean exist,
			LinkedHashMap<String, String> changedMap, String[] str, String newValue, String oldValue) {
		if(fileMap.containsKey(str[1]) && exist ) {
			String alias = fileMap.get(str[1]).get(0);
			Iterator<String> fileItr = fileMap.keySet().iterator();
			while(fileItr.hasNext()) {
				String nodeType = fileItr.next();
				ArrayList<String> techPlusAlias = fileMap.get(nodeType);
				chnagedMap(changedMap, str, newValue, oldValue, alias, nodeType, techPlusAlias);
			}
		}else {
			String key = str[0]+"::"+str[1]+"::"+oldValue;
			changedMap.put(key, newValue);
		
		}
	}

	private void chnagedMap(LinkedHashMap<String, String> changedMap, String[] str, String newValue, String oldValue,
			String alias, String nodeType, ArrayList<String> techPlusAlias) {
		if(techPlusAlias.get(0).equalsIgnoreCase(alias)) {
			String[] s = techPlusAlias.get(1).split(",");
			for(int i=0;i<s.length;i++) {
				String key = s[i]+"::"+nodeType+"::"+oldValue;
		
				String key1 = s[i]+"::"+nodeType+"::"+str[2];
					changedMap.computeIfAbsent(key, k ->{
					changedMap.put(key, newValue);
					return newValue;
				});
				changedMap.computeIfAbsent(key, k ->{
				hm.put(key1, newValue);
		         return newValue;
			
				});
			
			}
		}
	}

	private void extracted(Connection connDwhrep, Statement stmtDwhrep) {
		try {
			if (connDwhrep != null)
				connDwhrep.close();
			if (stmtDwhrep != null)
				stmtDwhrep.close();
		} catch (SQLException e) {
			log.warn("Unable to close Database connections " + e);
		}
	}

	private LinkedHashMap<String, ArrayList<TableList>> linkedHashmap(Context ctx,
			LinkedHashMap<String, String> changedMap, Iterator<String> techItr,
			LinkedHashMap<String, ArrayList<TableList>> updatedList) {
		if (changedMap.size() != 0) {
			while (techItr.hasNext()) {
				String tech = techItr.next();
				Iterator<String> itrChangedMap = changedMap.keySet().iterator();
				ArrayList<TableList> l = new ArrayList<TableList>();
				while (itrChangedMap.hasNext()) {
					String node = itrChangedMap.next();
					String[] str = node.split("::");
					if (tech.equalsIgnoreCase(str[0])) {
						TableList t = new TableList();
						t.setNodeType((str[1]));
						t.setDefGranularity(str[2]);
						t.setConfGranularity(changedMap.get(node));
						l.add(t);
					}
				}
				if (l.size() != 0)
					updatedList.put(tech, l);
			}
		}
		if (!updatedList.isEmpty())
			ctx.put("changedList", updatedList);
		else {
			ctx.put("NoChange", true);
		}
		return updatedList;
	}
	// To update the NodeTypeGranularity Table.
	protected void updateGranularityTable(LinkedHashMap<String, String> updatedTable, Context ctx) {
		Connection connDwhrep = null;
		connDwhrep = DatabaseConnections.getDwhRepConnection().getConnection();
		Iterator<String> itr = updatedTable.keySet().iterator();
		Statement stmtDwhrep = null;
		boolean status = true;
		try {
			stmtDwhrep = connDwhrep.createStatement();
		} catch (SQLException e1) {
			log.warn("Unable to create dwhrep connection " + e1);
		}
		try {
			while (itr.hasNext()) {
				String node = itr.next();
				String[] str = node.split("::");
				String sqlGranularity = "update NodeTypeGranularity set CONFIGURED_GRANULARITY = '"
						+ updatedTable.get(node) + "' where Technology='" + str[0] + "' and NODE_TYPE='" + str[1] + "'";
				ResultSet rsGranularity = null;
				try {
					rsGranularity = stmtDwhrep.executeQuery(sqlGranularity);
				} catch (SQLException e) {
					status = false;
					log.warn("Unable to update NodeTypeGranularity Table " + e);
				} finally {
					try {
						if (rsGranularity != null)
							rsGranularity.close();
					} catch (SQLException e) {
						log.warn("Unable to close result set " + e);
					}
				}
			}
		} 
		finally {
			ctx.put("updateStatus", status);
			extracted(connDwhrep, stmtDwhrep);
		}
		log.info("The NodeTypeGranularity table has been updated with the changed granularities.");
	}
}


