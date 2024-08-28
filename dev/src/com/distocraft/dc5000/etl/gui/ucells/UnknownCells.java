/*



 * ---------------------------------------------------------------------------------------



 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.



 * ---------------------------------------------------------------------------------------



 */

package com.distocraft.dc5000.etl.gui.ucells;

import java.io.IOException;

import java.security.NoSuchAlgorithmException;

import java.sql.Connection;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Statement;

import java.sql.Timestamp;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.Iterator;

import java.util.List;

import java.util.Map;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import org.apache.commons.logging.LogFactory;

import org.apache.velocity.Template;

import org.apache.velocity.context.Context;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

import com.distocraft.dc5000.etl.gui.util.PropertyLoader;

import com.ericsson.eniq.common.HashIdCreator;

public class UnknownCells extends EtlguiServlet {

	private static final String NULL_STRING = "null";

	private static final String ACTIVE = "ACTIVE";

	private static final String ERBS_ID = "ERBS_ID";

	private static final String WHERE = " where ";

	protected enum NetworkType {

		_2G, _3G, _3G_RAN, _4G;

		public int getRat() {

			int rat;

			if (this == _2G) {

				rat = 0;

			} else if (this == _3G || this == _3G_RAN) {

				rat = 1;

			} else {

				rat = 2;

			}

			return rat;

		}

	}

	private static final String PIPE = "|";

	private static final String EMPTY_STRING = "";

	private static final String HIER3_CELL_ID = "HIER3_CELL_ID";

	private static final String HIER321_ID = "HIER321_ID";

	private static final String HIER32_ID = "HIER32_ID";

	private static final String HIER3_ID = "HIER3_ID";

	private static final String REQ_PARAM_VENDOR = "vendor";

	private static final String REQ_PARAM_OSS_ID = "ossId";

	private static final String REQ_PARAM_PARENT_SELECTION = "parentSelection";

	private static final String REQ_PARAM_CELLS_TO_REPARENT = "cellsToReparent";

	private static final String REQ_PARAM_EXPORT = "export";

	private static final String REQ_PARAM_CONNECT = "connect";

	private static final String CTX_DIM_EXPORT = "dim_export";

	private static final String CTX_REPARENT_RESULTS = "reparent_results";

	private static final String CTX_ERRORMSG = "errormsg";

	private static final String CTX_REPARENT_CELL_DATA = "reparentCellData";

	private static final String CTX_NETWORK_TYPE_PARAM = "network_type";

	private static final String CTX_EXPORT_DATA = "export_data";

	private static final String CTX_UNMATCHED_TOPOLOGY_ITEMS = "unmatched_topology_items";

	private static final String CTX_CONTROLLER_LIST = "parent_list";

	private static final String CTX_OSSID_LIST = "ossIds";

	private static final String ERICSSON = "ericsson";

	private static final String UNKNOWNCELLS_MAIN_VM = "unknowncells_main.vm";

	private static final String UNKNOWNCELLS_REPARENT_VM = "unknowncells_reparent.vm";

	private static final String UNKNOWNCELLS_SUMMARY_VM = "unknowncells_summary.vm";

	private static final String UNMATCHED_TOPOLOGY_EXPORT_VM = "unmatched_topology_export.vm";

	private static final String ERICSSON_TOPOLOGY_TABLE = "DIM_E_SGEH_HIER321";

	private static final String ERICSSON_HIER321_CELL_TOPOLOGY_TABLE = "DIM_E_SGEH_HIER321_CELL";

	private static final String NON_ERICSSON_TOPOLOGY_TABLE = "DIM_Z_SGEH_HIER321";
	
	private static final String NON_ERICSSON_HIER321_CELL_TOPOLOGY_TABLE = "DIM_Z_SGEH_HIER321_CELL";

	private static final String UNKNOWN_TOPOLOGY_TABLE = "DIM_E_SGEH_UNKNOWN_HIER321";

	private static final String ERICSSON_TOPOLOGY_TABLE_COLUMNS = " ("
			+ "OSS_ID" + ",RAT" + ",HIERARCHY_3"

			+ ",HIERARCHY_2" + ",HIERARCHY_1" + ",RAC" + ",MCC" + ",MNC"
			+ ",LAC" + ",CELL_ID" + ",ACCESS_AREA_ID"

			+ ",GLOBAL_CELL_ID" + ",START_TIME" + ",END_TIME" + ",SITE_NAME"
			+ ",CELL_TYPE" + ",CELL_BAND"

			+ ",CELL_LAYER" + ",HIER3_ID" + ",HIER32_ID" + ",HIER321_ID"
			+ ",VENDOR" + ",STATUS" + ",CREATED"

			+ ",MODIFIED" + ",MODIFIER) ";

	private static final String ERICSSON_HIER321_CELL_TOPOLOGY_TABLE_COLUMNS = " ("
			+ "RAT"
			+ ",HIERARCHY_3"
			+ ",HIERARCHY_2"
			+ ",HIERARCHY_1"
			+ ",CELL_ID"
			+ ",CID"
			+ ",CELL_NAME"
			+ ",SITE_NAME"
			+ ",START_TIME"
			+ ",END_TIME"
			+ ",HIER3_ID"
			+ ",HIER32_ID"
			+ ",HIER321_ID"
			+ ",HIER3_CELL_ID"
			+ ",VENDOR" + ",STATUS" + ",CREATED" + ",MODIFIED" + ",MODIFIER) ";

	private static final String NON_ERICSSON_TOPOLOGY_TABLE_COLUMNS = " ("
			+ "OSS_ID" + ",RAT" + ",HIERARCHY_3"

			+ ",HIERARCHY_2" + ",HIERARCHY_1" + ",RAC" + ",MCC" + ",MNC"
			+ ",LAC" + ",CELL_ID" + ",ACCESS_AREA_ID"

			+ ",GLOBAL_CELL_ID" + ",START_TIME" + ",END_TIME" + ",HIER3_ID"
			+ ",HIER32_ID" + ",HIER321_ID" + ",VENDOR"

			+ ",STATUS" + ",CREATED" + ",MODIFIED" + ",MODIFIER) ";
	
	private static final String NON_ERICSSON_HIER321_CELL_TOPOLOGY_TABLE_COLUMNS = " ("
			+ "RAT"
			+ ",HIERARCHY_3"
			+ ",HIERARCHY_2"
			+ ",HIERARCHY_1"
			+ ",CELL_ID"
			+ ",CELL_NAME"
			+ ",SITE_NAME"
			+ ",START_TIME"
			+ ",END_TIME"
			+ ",HIER3_ID"
			+ ",HIER32_ID"
			+ ",HIER321_ID"
			+ ",HIER3_CELL_ID"
			+ ",VENDOR" + ",STATUS" + ",CREATED" + ",MODIFIED" + ",MODIFIER) ";
	

	private static final String COL_HIERARCHY_3 = "HIERARCHY_3";

	private static final String COL_HIERARCHY_1 = "HIERARCHY_1";

	private static final String COL_ACCESS_AREA_ID = "ACCESS_AREA_ID";

	private static final String COL_RAT = "RAT";

	private static final String COL_MCC = "MCC";

	private static final String COL_MNC = "MNC";

	private static final String COL_LAC = "LAC";

	private static final String COL_RAC = "RAC";

	private static final String COL_EVENT_SOURCE_NAME = "EVENT_SOURCE_NAME";

	private static final String COL_FIRST_SEEN = "FIRST_SEEN";

	// Unmatched topology data parameters

	//

	private static final int ACCESS_AREA_ID_INDEX = 0;

	private static final int MCC_INDEX = 1;

	private static final int MNC_INDEX = 2;

	private static final int LAC_INDEX = 3;

	private static final int RAC_INDEX = 4;

	private static final int EVENT_SOURCE_NAME_INDEX = 5;

	private static final int FIRST_SEEN_INDEX = 6;

	private static final int UNMATCHED_TOPOLOGY_ITEM_SIZE = FIRST_SEEN_INDEX + 1;

	// 4G INDEXES & COLUMN NAME

	private static final String COL_4G_CELLID = "CELL_ID";

	private static final String COL_4G_MCC = "MCC";

	private static final String COL_4G_MNC = "MNC";

	private static final String COL_4G_TAC = "tac";

	private static final String COL_4G_EVENT_SOURCE_NAME = "EVENT_SOURCE_NAME";

	private static final String COL_4G_FIRST_SEEN = "FIRST_SEEN";

	// Unmatched topology data parameters

	//

	private static final int CELLID_INDEX_4G = 0;

	private static final int MCC_INDEX_4G = 1;

	private static final int MNC_INDEX_4G = 2;

	private static final int TAC_INDEX_4G = 3;

	private static final int EVENT_SOURCE_NAME_INDEX_4G = 4;

	private static final int FIRST_SEEN_INDEX_4G = 5;

	private static final int UNMATCHED_TOPOLOGY_ITEM_SIZE_4G = FIRST_SEEN_INDEX_4G + 1;

	private final Log log = LogFactory.getLog(this.getClass());

	@Override
	public Template doHandleRequest(final HttpServletRequest request,
			final HttpServletResponse response,

			final Context ctx) {

		// public final Template handleRequest(final HttpServletRequest request,

		// final HttpServletResponse response, final Context ctx) {

		try {

			if (request.getParameter(REQ_PARAM_EXPORT) != null) {

				ctx.put(CTX_NETWORK_TYPE_PARAM, request.getSession()
						.getAttribute(CTX_NETWORK_TYPE_PARAM));

				return formatForExport(request.getParameter(CTX_EXPORT_DATA),
						ctx);

			}

			if (request.getParameter(REQ_PARAM_CONNECT) != null) {

				ctx.put(CTX_NETWORK_TYPE_PARAM, request.getSession()
						.getAttribute(CTX_NETWORK_TYPE_PARAM));

				return connectToController(request.getParameterMap(), ctx);

			}

			final String networkTypeParam = request
					.getParameter(CTX_NETWORK_TYPE_PARAM);

			ctx.put(CTX_NETWORK_TYPE_PARAM, networkTypeParam);

			if (networkTypeParam == null) {

				return getTemplate(UNKNOWNCELLS_MAIN_VM);

			} else {

				final NetworkType networkType = NetworkType
						.valueOf(networkTypeParam);

				String selectedController = null;

				if (!networkType.equals(NetworkType._4G)
						&& !networkType.equals(NetworkType._3G_RAN)) {

					selectedController = getSelectedController(request);

				}

				log.debug("Selected Controller: " + selectedController);

				request.getSession().setAttribute(CTX_NETWORK_TYPE_PARAM,
						networkTypeParam);

				if (selectedController != null
						&& !networkType.equals(NetworkType._4G)
						&& !networkType.equals(NetworkType._3G_RAN)) {

					return getConnectParameters(selectedController,
							request.getParameterMap(), ctx, networkType);

				} else if ((networkType.equals(NetworkType._4G) || networkType
						.equals(NetworkType._3G_RAN))
						&& (request.getParameter(REQ_PARAM_CELLS_TO_REPARENT) != null && !(request
								.getParameter(REQ_PARAM_CELLS_TO_REPARENT)
								.equals(""))))		{
					return getConnectParameters(request.getParameterMap(), ctx,
							networkType);

				} else {

					return getUnmatchedTopologyForNetworkType(networkType, ctx);

				}

			}

		} catch (final Exception e) {

			ctx.put(CTX_ERRORMSG, e.getMessage());

			e.printStackTrace();

			try {

				return getTemplate(UNKNOWNCELLS_MAIN_VM);

			} catch (final Exception e1) {

				e1.printStackTrace();

				return null;

			}

		}

	}

	private String getSelectedController(final HttpServletRequest request) {

		final String parentSelection = request
				.getParameter(REQ_PARAM_PARENT_SELECTION);

		final String parentSelectionText = request
				.getParameter("parentSelectionText");

		String selectedParent = null;

		if (parentSelectionText != null && parentSelectionText != "") {

			selectedParent = parentSelectionText;

		}

		if (parentSelection != null && parentSelection != "") {

			selectedParent = parentSelection;

		}

		return selectedParent;

	}

	private Template formatForExport(final String exportData, final Context ctx)
			throws Exception {

		ctx.put(CTX_DIM_EXPORT, exportData);

		final NetworkType networkType = NetworkType.valueOf((String) ctx
				.get(CTX_NETWORK_TYPE_PARAM));

		final StringTokenizer mapEntry = new StringTokenizer(exportData, "{}");

		// only here while debugging....

		PropertyLoader.reset();

		// only here while debugging....

		if (networkType.equals(NetworkType._4G)) {

			final StringBuilder topologyExportQuery = new StringBuilder();

			final String _colsToExport = "OSS_ID,ERBS_FDN,ERBS_NAME,ERBS_ID,VENDOR,STATUS,CREATED,MODIFIED,MODIFIER";

			final String[] columnsToExport = _colsToExport.split(",");

			final String colSeperator = PropertyLoader.getProperty(
					"topology_export.colseperator", "|");

			topologyExportQuery.append("SELECT ");

			final int exportColumnCount = columnsToExport.length;

			for (int i = 0; i < exportColumnCount; i++) {

				topologyExportQuery.append(columnsToExport[i]);

				if (i < exportColumnCount - 1) {

					topologyExportQuery.append(", ");

				}

			}

			topologyExportQuery.append(" FROM DIM_E_LTE_ERBS  WHERE (");

			while (mapEntry.hasMoreTokens()) {

				final String entry = mapEntry.nextToken();

				final int splitIndex = entry.indexOf('=');

				final String nodeName = entry.substring(0, splitIndex).trim();

				topologyExportQuery.append(ERBS_ID).append(" = '")
						.append(nodeName).append("'");

				if (mapEntry.hasMoreTokens()) {

					topologyExportQuery.append(") and (");

				}

			}

			topologyExportQuery.append(")");

			Statement stmt = null;

			ResultSet rs = null;

			final StringBuilder dimTopology = new StringBuilder();

			try {

				stmt = getDatabaseConnection(ctx).createStatement();

				rs = stmt.executeQuery(topologyExportQuery.toString());

				for (int i = 0; i < exportColumnCount; i++) {

					final String colName = columnsToExport[i];

					dimTopology.append(colName);

					if (i < exportColumnCount - 1) {

						dimTopology.append(colSeperator);

					}

				}

				dimTopology.append("\n");

				while (rs.next()) {

					for (int i = 0; i < exportColumnCount; i++) {

						final String value = rs.getString(columnsToExport[i]);

						if (value == null
								|| NULL_STRING.equalsIgnoreCase(value)) {

							dimTopology.append("");

						} else {

							dimTopology.append(value);

						}

						if (i < exportColumnCount - 1) {

							dimTopology.append(colSeperator);

						}

					}

					dimTopology.append("\n");

				}

				ctx.put("dim_topology", dimTopology.toString());

			} catch (final SQLException e) {

				ctx.put(CTX_ERRORMSG, getErrorMessage(e));

			} catch (final Throwable t) {

				ctx.put(CTX_ERRORMSG, t.toString());

			} finally {

				if (rs != null) {

					try {

						rs.close();

					} catch (final SQLException e) {

						log.warn("Error closing resultSet");

					}

				}

				if (stmt != null) {

					try {

						stmt.close();

					} catch (final SQLException e) {

						log.warn("Error closing statement");

					}

				}

			}

		} else {

			final String _colsToExport = PropertyLoader.getProperty(
					"topology_export.columns", null);

			final String[] columnsToExport = _colsToExport.split(",");

			final String colSeperator = PropertyLoader.getProperty(
					"topology_export.colseperator", "|");

			final StringBuilder topologyExportQuery = new StringBuilder();

			topologyExportQuery.append("select ");

			final int exportColumnCount = columnsToExport.length;

			for (int i = 0; i < exportColumnCount; i++) {

				topologyExportQuery.append(columnsToExport[i]);

				if (i < exportColumnCount - 1) {

					topologyExportQuery.append(", ");

				}

			}

			topologyExportQuery.append(" from ")
					.append(ERICSSON_TOPOLOGY_TABLE).append(" where (");

			while (mapEntry.hasMoreTokens()) {

				final String entry = mapEntry.nextToken();

				final int splitIndex = entry.indexOf('=');

				final String nodeName = entry.substring(0, splitIndex).trim();

				final String mValues = entry.substring(splitIndex + 2,
						entry.length() - 1); // knock off the wrapping brackets
												// []

				final StringTokenizer cellTokeniser = new StringTokenizer(
						mValues, ",");

				topologyExportQuery.append(COL_HIERARCHY_3).append(" = '")
						.append(nodeName).append("' and ")

						.append(COL_HIERARCHY_1).append(" in (");

				while (cellTokeniser.hasMoreTokens()) {

					topologyExportQuery.append("'")
							.append(cellTokeniser.nextToken().trim())
							.append("'");

					if (cellTokeniser.hasMoreTokens()) {

						topologyExportQuery.append(", ");

					}

				}

				topologyExportQuery.append(")");

				if (mapEntry.hasMoreTokens()) {

					topologyExportQuery.append(" and (");

				}

			}

			topologyExportQuery.append(")");

			Statement stmt = null;

			ResultSet rs = null;

			final StringBuilder dimTopology = new StringBuilder();

			try {

				stmt = getDatabaseConnection(ctx).createStatement();

				rs = stmt.executeQuery(topologyExportQuery.toString());

				for (int i = 0; i < exportColumnCount; i++) {

					final String colName = columnsToExport[i];

					dimTopology.append(colName);

					if (i < exportColumnCount - 1) {

						dimTopology.append(colSeperator);

					}

				}

				dimTopology.append("\n");

				while (rs.next()) {

					for (int i = 0; i < exportColumnCount; i++) {

						final String value = rs.getString(columnsToExport[i]);

						if (value == null
								|| NULL_STRING.equalsIgnoreCase(value)) {

							dimTopology.append("");

						} else {

							dimTopology.append(value);

						}

						if (i < exportColumnCount - 1) {

							dimTopology.append(colSeperator);

						}

					}

					dimTopology.append("\n");

				}

				ctx.put("dim_topology", dimTopology.toString());

			} catch (final SQLException e) {

				ctx.put(CTX_ERRORMSG, getErrorMessage(e));

			} catch (final Throwable t) {

				ctx.put(CTX_ERRORMSG, t.toString());

			} finally {

				if (rs != null) {

					try {

						rs.close();

					} catch (final SQLException e) {

						log.warn("Error closing resultSet");

					}

				}

				if (stmt != null) {

					try {

						stmt.close();

					} catch (final SQLException e) {

						log.warn("Error closing statement");

					}

				}

			}

		}

		return getTemplate(UNMATCHED_TOPOLOGY_EXPORT_VM);

	}

	private String getErrorMessage(final SQLException exception) {

		String errmsg;

		if (exception.getNextException() == null) {

			errmsg = exception.getMessage();

		} else {

			errmsg = exception.getNextException().getMessage();

		}

		return errmsg;

	}

	private Template getConnectParameters(final String selectedController,
			final Map paramMap, final Context ctx,

			final NetworkType networkType) throws Exception {

		ctx.put(REQ_PARAM_PARENT_SELECTION, selectedController);

		final Object serializedString = paramMap
				.get(REQ_PARAM_CELLS_TO_REPARENT);

		if (serializedString == null) {

			return getTemplate(UNKNOWNCELLS_REPARENT_VM);

		}

		final String topologyData = ((String[]) serializedString)[0];

		final Connection conn = getDatabaseConnection(ctx);

		ctx.put(REQ_PARAM_CELLS_TO_REPARENT, topologyData);

		ctx.put(CTX_OSSID_LIST, getOssIds(conn, networkType));

		if (topologyData != null && !topologyData.isEmpty()) {

			final List<String[]> topologyItems = convertFromTokenizedString(topologyData);

			final String activeCheckResults = getTopologyActiveStatus(
					topologyItems, networkType, conn);

			if (activeCheckResults.isEmpty()) {

				final List<String[]> connectData = new ArrayList<String[]>(
						topologyItems.size());

				for (final String[] topologyItem : topologyItems) {

					final String[] rowData = new String[6];

					rowData[0] = selectedController;

					rowData[1] = topologyItem[ACCESS_AREA_ID_INDEX];

					rowData[2] = topologyItem[MCC_INDEX];

					rowData[3] = topologyItem[MNC_INDEX];

					rowData[4] = topologyItem[LAC_INDEX];

					rowData[5] = topologyItem[RAC_INDEX];

					connectData.add(rowData);

				}

				ctx.put(CTX_REPARENT_CELL_DATA,
						convertToJavaScriptString(connectData));

				return getTemplate(UNKNOWNCELLS_REPARENT_VM);

			} else {

				ctx.put(CTX_ERRORMSG, activeCheckResults);

				return getTemplate(UNKNOWNCELLS_MAIN_VM);

			}

		} else {

			// no cells selected....

			return getTemplate(UNKNOWNCELLS_MAIN_VM);

		}

	}

	private Template getConnectParameters(final Map paramMap,
			final Context ctx, final NetworkType networkType) throws Exception {
		final Object serializedString = paramMap
				.get(REQ_PARAM_CELLS_TO_REPARENT);
		if (serializedString == null) {
			return getTemplate(UNKNOWNCELLS_REPARENT_VM);
		}
		final String topologyData = ((String[]) serializedString)[0];
		final Connection conn = getDatabaseConnection(ctx);
		ctx.put(REQ_PARAM_CELLS_TO_REPARENT, topologyData);
		ctx.put(CTX_OSSID_LIST, getOssIds(conn, networkType));

		if (topologyData != null && !topologyData.isEmpty()) {
			final List<String[]> topologyItems = convertFromTokenizedString(topologyData);
			final String activeCheckResults = getTopologyActiveStatus(
					topologyItems, networkType, conn);

			if (activeCheckResults.isEmpty()) {

				final List<String[]> connectData = new ArrayList<String[]>(
						topologyItems.size());

				if (networkType.equals(NetworkType._4G)) {
					for (final String[] topologyItem : topologyItems) {

						final String[] rowData = new String[5];

						rowData[0] = "eNodeB"
								+ getENodeB(topologyItem[CELLID_INDEX_4G]);

						rowData[1] = topologyItem[CELLID_INDEX_4G];

						rowData[2] = topologyItem[MCC_INDEX_4G];

						rowData[3] = topologyItem[MNC_INDEX_4G];

						rowData[4] = topologyItem[TAC_INDEX_4G];
						connectData.add(rowData);
					}

				} else {
					for (final String[] topologyItem : topologyItems) {

						final String[] rowData = new String[4];

						rowData[0] = getController(conn, topologyItem[1]);

						rowData[1] = "RNC" + topologyItem[1] + "-"
								+ topologyItem[0];

						rowData[2] = topologyItem[2];

						rowData[3] = topologyItem[3];
						connectData.add(rowData);
					}

				}

				ctx.put(CTX_REPARENT_CELL_DATA,
						convertToJavaScriptString(connectData));

				return getTemplate(UNKNOWNCELLS_REPARENT_VM);

			} else {

				ctx.put(CTX_ERRORMSG, activeCheckResults);

				return getTemplate(UNKNOWNCELLS_MAIN_VM);

			}

		} else {

			// no cells selected....

			return getTemplate(UNKNOWNCELLS_MAIN_VM);

		}

	}

	private String getController(final Connection conn, final String rncId)
			throws SQLException {
		String controllerQuery = "select ALTERNATIVE_FDN from DIM_E_RAN_RNC,DIM_E_RAN_RNCFUNCTION where DIM_E_RAN_RNC.RNC_ID = DIM_E_RAN_RNCFUNCTION.RNC_ID and DIM_E_RAN_RNCFUNCTION.rncId ="
				+ rncId;
		final Statement stmt = conn.createStatement();

		final ResultSet rs = stmt.executeQuery(controllerQuery);
		String rnc_id;

		try {

			rs.next();
			rnc_id = rs.getString(1);

		} finally {

			rs.close();
			stmt.close();
		}
		return rnc_id;
	}

	private String getENodeB(final String eci) {

		final int i = Integer.parseInt(eci);

		final int enodeBID = (i >> 8);

		return "" + enodeBID;

	}

	private String getCellId(final String eci) {

		final int i = Integer.parseInt(eci);

		final int cellID = (i & 0xFF);

		return "" + cellID;

	}

	private Template connectToController(final Map paramMap, final Context ctx)
			throws Exception {

		final NetworkType networkType = NetworkType.valueOf((String) ctx
				.get(CTX_NETWORK_TYPE_PARAM));

		final String cellsToReparent = ((String[]) paramMap
				.get("controllerSelection"))[0];

		String selectedController = "";

		if (!networkType.equals(NetworkType._4G)
				&& !networkType.equals(NetworkType._3G_RAN)) {

			selectedController = ((String[]) paramMap
					.get(REQ_PARAM_PARENT_SELECTION))[0];

		}

		final String ossId = ((String[]) paramMap.get(REQ_PARAM_OSS_ID))[0];

		final String vendor = ((String[]) paramMap.get(REQ_PARAM_VENDOR))[0];

		final List<String[]> connectParameters = convertFromTokenizedString(cellsToReparent);

		final List<String> insertStatements = new ArrayList<String>();

		final List<String> deleteStatements = new ArrayList<String>();

		final Map<String, List<String>> controllerAccessAreaMap = buildConnectSQL(
				insertStatements, deleteStatements,

				selectedController, ossId, vendor, connectParameters,
				networkType);

		final Connection dbConnection = getDatabaseConnection(ctx);

		boolean allWentOk = false;

		final boolean restoreAutoCommit = dbConnection.getAutoCommit();

		Statement dbStmt = null;

		ResultSet checkSet = null;

		try {

			dbConnection.setAutoCommit(false);

			dbConnection.rollback();

			dbStmt = dbConnection.createStatement();

			if (!networkType.equals(NetworkType._4G)) {

				for (final String controllerName : controllerAccessAreaMap
						.keySet()) {

					final List<String> checkCells = controllerAccessAreaMap
							.get(controllerName);

					String checkCellNameNotAlreadyUsed = "select distinct("
							+ COL_HIERARCHY_1 + ") from "

							+ ERICSSON_TOPOLOGY_TABLE + WHERE + COL_HIERARCHY_3
							+ " = '" + controllerName + "' and "

							+ COL_HIERARCHY_1 + " in (";

					final Iterator<String> iterator = checkCells.iterator();

					while (iterator.hasNext()) {

						checkCellNameNotAlreadyUsed += "'" + iterator.next()
								+ "'";

						if (iterator.hasNext()) {

							checkCellNameNotAlreadyUsed += ", ";

						}

					}

					checkCellNameNotAlreadyUsed += ")";

					checkSet = dbStmt.executeQuery(checkCellNameNotAlreadyUsed);

					String errormsg = "";

					while (checkSet.next()) {

						errormsg += "Cell Name '"
								+ checkSet.getString(COL_HIERARCHY_1)

								+ "' is already in connected to controller "
								+ controllerName + "<br/>";

					}

					if (errormsg.length() > 0) {

						ctx.put(CTX_ERRORMSG, errormsg);

						ctx.put(CTX_NETWORK_TYPE_PARAM,
								paramMap.get(CTX_NETWORK_TYPE_PARAM));

						return getTemplate(UNKNOWNCELLS_MAIN_VM);

					}

				}

			}

			// call insert statements

			// delete cells from UNKNOWN table

			for (final String add : insertStatements) {

				dbStmt.executeUpdate(add);

			}

			for (final String delete : deleteStatements) {

				dbStmt.executeUpdate(delete);

			}

			dbConnection.commit();

			allWentOk = true;

		} catch (final SQLException e) {

			e.printStackTrace();

			ctx.put(CTX_ERRORMSG, getErrorMessage(e));

		} catch (final Throwable t) {

			t.printStackTrace();

			ctx.put(CTX_ERRORMSG, t.toString());

		} finally {

			if (checkSet != null) {

				try {

					checkSet.close();

				} catch (final SQLException e) {

					log.warn("Error closing resultSet");

				}

			}

			if (dbStmt != null) {

				try {

					dbStmt.close();

				} catch (final SQLException e) {

					log.warn("Error closing statement");

				}

			}

			if (!allWentOk) {

				dbConnection.rollback();

			}

			dbConnection.setAutoCommit(restoreAutoCommit);

		}

		ctx.put(CTX_REPARENT_RESULTS, controllerAccessAreaMap);

		ctx.put(CTX_DIM_EXPORT, connectParameters);

		return getTemplate(UNKNOWNCELLS_SUMMARY_VM);

	}

	private void build4GConnectSQL(final List<String> insertStatements,
			final List<String> deleteStatements,
			final String selectedController, final String ossId,
			final String vendor, final List<String[]> reparentData,
			final Map<String, List<String>> accessAreaControllerMap) {

		final Timestamp startTime = new Timestamp(System.currentTimeMillis());
		for (final String[] dataToInsert : reparentData) {
			final String controllerName = dataToInsert[0];
			final String eci = dataToInsert[1];
			final String mcc = dataToInsert[2];
			final String mnc = dataToInsert[3];
			final String tac = dataToInsert[4];

			deleteStatements
					.add("delete from DIM_E_LTE_UNKNOWN_EUCELL where CELL_ID = "
							+ eci
							+ " and MCC = '"
							+ mcc
							+ "' and MNC = '"
							+ mnc + "' and tac = " + tac);

			final List<String> accessAreaList;
			if (accessAreaControllerMap.containsKey(controllerName)) {
				accessAreaList = accessAreaControllerMap.get(controllerName);
			} else {
				accessAreaList = new ArrayList<String>();
				accessAreaControllerMap.put(controllerName, accessAreaList);
			}

			if (!accessAreaList.contains(eci)) {
				accessAreaList.add(eci);
			}

			StringBuilder insertTopologyStatement = new StringBuilder();
			insertTopologyStatement.append("INSERT INTO DIM_E_LTE_ERBS");
			insertTopologyStatement
					.append("(OSS_ID,ERBS_FDN,ERBS_NAME,ERBS_ID,VENDOR,STATUS,CREATED,MODIFIED,MODIFIER)");
			insertTopologyStatement.append(" values (");
			append(insertTopologyStatement, ossId);
			append(insertTopologyStatement, controllerName);
			append(insertTopologyStatement, controllerName);
			append(insertTopologyStatement, controllerName);
			append(insertTopologyStatement, vendor);
			append(insertTopologyStatement, ACTIVE);
			append(insertTopologyStatement, startTime.toString());
			append(insertTopologyStatement, startTime.toString());
			append(insertTopologyStatement, "ENIQ", true);
			insertTopologyStatement.append(")");

			insertStatements.add(insertTopologyStatement.toString());

			insertTopologyStatement = new StringBuilder();
			insertTopologyStatement
					.append("INSERT INTO DIM_E_LTE_ENODEBFUNCTION");
			insertTopologyStatement
					.append("(OSS_ID,ENODEB_FDN,ERBS_ID,ENodeBFunction,ENODEB_ID,MCC,MNC,VENDOR,STATUS,CREATED,MODIFIED,MODIFIER)");
			insertTopologyStatement.append(" values (");
			append(insertTopologyStatement, ossId);
			append(insertTopologyStatement, controllerName);
			append(insertTopologyStatement, controllerName);
			append(insertTopologyStatement, controllerName);
			append(insertTopologyStatement, getENodeB(eci));
			append(insertTopologyStatement, mcc);
			append(insertTopologyStatement, mnc);
			append(insertTopologyStatement, vendor);
			append(insertTopologyStatement, ACTIVE);
			append(insertTopologyStatement, startTime.toString());
			append(insertTopologyStatement, startTime.toString());
			append(insertTopologyStatement, "ENIQ", true);
			insertTopologyStatement.append(")");

			insertStatements.add(insertTopologyStatement.toString());

			insertTopologyStatement = new StringBuilder();
			insertTopologyStatement.append("INSERT INTO DIM_E_LTE_EUCELL");
			insertTopologyStatement
					.append("(OSS_ID, CELL_TYPE, ERBS_ID, ENodeBFunction, EUtranCellId, CELL_ID, bPlmnList, "
							+ "MCC, MNC,tac, "
							+ "VENDOR, STATUS, CREATED, MODIFIED, MODIFIER )");

			insertTopologyStatement.append(" values (");
			append(insertTopologyStatement, ossId);
			append(insertTopologyStatement, "TDD");
			append(insertTopologyStatement, controllerName);
			append(insertTopologyStatement, controllerName);
			append(insertTopologyStatement, controllerName + "-"
					+ getCellId(eci));
			append(insertTopologyStatement, getCellId(eci));
			append(insertTopologyStatement, mcc + "_" + mnc + "_2");
			append(insertTopologyStatement, mcc);
			append(insertTopologyStatement, mnc);
			append(insertTopologyStatement, tac);
			append(insertTopologyStatement, vendor);
			append(insertTopologyStatement, ACTIVE);
			append(insertTopologyStatement, startTime.toString());
			append(insertTopologyStatement, startTime.toString());
			append(insertTopologyStatement, "ENIQ", true);
			insertTopologyStatement.append(")");

			insertStatements.add(insertTopologyStatement.toString());

		}

	}

	private void build3GRANConnectSQL(final List<String> insertStatements,
			final List<String> deleteStatements,
			final String selectedController, final String ossId,
			final String vendor, final List<String[]> reparentData,
			final Map<String, List<String>> accessAreaControllerMap,
			final NetworkType networkType) throws NoSuchAlgorithmException, IOException {

		final Timestamp startTime = new Timestamp(System.currentTimeMillis());
		final List<String> check = new ArrayList<String>();
		for (final String[] dataToInsert : reparentData) {
			final String controllerName = dataToInsert[0];
			final String accessAreaId = dataToInsert[1];
			final String mcc = dataToInsert[2];
			final String mnc = dataToInsert[3];
			//final String lac = dataToInsert[4];
			//final String rac = dataToInsert[5];

			String[] rncId_cid = accessAreaId.split("-");// RNC1-2
			final String CID = rncId_cid[1];// 2
			final String rncId = rncId_cid[0].substring(3);// RNC1

			deleteStatements
					.add("delete from DIM_E_RAN_UNKNOWN_HIER3_CELL_ID where CID = "
							+ CID
							+ " and rncId = "
							+ rncId
							+ " and MCC = '"
							+ mcc
							+ "' and MNC = '"
							+ mnc
							+ "'");

			final String globalAccessAreaId = buildGlobalAccessAreaId(mcc, mnc,rncId, CID);
			
			final String accessAreaName = buildTemporaryAccessAreaName(networkType, globalAccessAreaId);
			
			final String rat = String.valueOf(networkType.getRat());

			final String hier3IdValue = hashIdGenerator(rat, controllerName,
					EMPTY_STRING, vendor, HIER3_ID);

			final String hier32IdValue = hashIdGenerator(rat, controllerName,
					EMPTY_STRING, vendor, HIER32_ID);

			final String hier321IdValue = hashIdGenerator(rat, controllerName,
					accessAreaName, vendor, HIER321_ID);

			final String hier3_Cell_IdValue = hashIdGenerator(rat,
					controllerName, accessAreaId, vendor, HIER3_CELL_ID);

			final List<String> accessAreaList;

			if (accessAreaControllerMap.containsKey(controllerName)) {
				accessAreaList = accessAreaControllerMap.get(controllerName);
			} else {
				accessAreaList = new ArrayList<String>();
				accessAreaControllerMap.put(controllerName, accessAreaList);
			}

			if (!accessAreaList.contains(accessAreaId)) {
				accessAreaList.add(accessAreaId);
			}

			StringBuilder insertTopologyStatement = new StringBuilder();

			if (ERICSSON.equalsIgnoreCase(vendor)) {
				insertTopologyStatement.append("INSERT INTO ");
				insertTopologyStatement.append(ERICSSON_TOPOLOGY_TABLE);
				insertTopologyStatement.append(ERICSSON_TOPOLOGY_TABLE_COLUMNS);
				insertTopologyStatement.append(" values (");
				append(insertTopologyStatement, ossId);
				append(insertTopologyStatement,
						String.valueOf(networkType.getRat()));
				append(insertTopologyStatement, controllerName);
				append(insertTopologyStatement, "");
				append(insertTopologyStatement, accessAreaName);
				append(insertTopologyStatement, ""); // RAC
				append(insertTopologyStatement, mcc);
				append(insertTopologyStatement, mnc);
				append(insertTopologyStatement, "0"); //lac
				append(insertTopologyStatement, null);
				append(insertTopologyStatement, accessAreaId);
				append(insertTopologyStatement, globalAccessAreaId);
				append(insertTopologyStatement, startTime.toString());
				append(insertTopologyStatement, null);
				append(insertTopologyStatement, "");
				append(insertTopologyStatement, "1"); // Cell type
				append(insertTopologyStatement, ""); // Cell band
				append(insertTopologyStatement, ""); // Cell layer
				append(insertTopologyStatement, hier3IdValue);
				append(insertTopologyStatement, hier32IdValue);
				append(insertTopologyStatement, hier321IdValue);
				append(insertTopologyStatement, vendor);
				append(insertTopologyStatement, ACTIVE);
				append(insertTopologyStatement, startTime.toString());
				append(insertTopologyStatement, startTime.toString());
				append(insertTopologyStatement, "ENIQ_EVENTS", true);
				insertTopologyStatement.append(")");

				insertStatements.add(insertTopologyStatement.toString());
				
				if(!check.contains(hier3_Cell_IdValue)){
					insertTopologyStatement = new StringBuilder();
					insertTopologyStatement.append("INSERT INTO ");
					insertTopologyStatement
							.append(ERICSSON_HIER321_CELL_TOPOLOGY_TABLE);
					insertTopologyStatement
							.append(ERICSSON_HIER321_CELL_TOPOLOGY_TABLE_COLUMNS);
					insertTopologyStatement.append(" values (");
					append(insertTopologyStatement,
							String.valueOf(networkType.getRat()));
					append(insertTopologyStatement, controllerName);
					append(insertTopologyStatement, "");
					append(insertTopologyStatement, accessAreaName);
					append(insertTopologyStatement, accessAreaId);
					append(insertTopologyStatement, CID);
					append(insertTopologyStatement, null);// cell name
					append(insertTopologyStatement, null);// site name
					append(insertTopologyStatement, startTime.toString());
					append(insertTopologyStatement, null);
					append(insertTopologyStatement, hier3IdValue);
					append(insertTopologyStatement, hier32IdValue);
					append(insertTopologyStatement, hier321IdValue);
					append(insertTopologyStatement, hier3_Cell_IdValue);
					append(insertTopologyStatement, vendor);
					append(insertTopologyStatement, ACTIVE);
					append(insertTopologyStatement, startTime.toString());
					append(insertTopologyStatement, startTime.toString());
					append(insertTopologyStatement, "ENIQ_EVENTS", true);
					insertTopologyStatement.append(")");
					insertStatements.add(insertTopologyStatement.toString());
					check.add(hier3_Cell_IdValue);
				}

				

			} else {

				insertTopologyStatement.append("INSERT INTO ");
				insertTopologyStatement.append(NON_ERICSSON_TOPOLOGY_TABLE);
				insertTopologyStatement
						.append(NON_ERICSSON_TOPOLOGY_TABLE_COLUMNS);
				insertTopologyStatement.append(" values (");
				append(insertTopologyStatement, ossId);
				append(insertTopologyStatement,
						String.valueOf(networkType.getRat()));
				append(insertTopologyStatement, controllerName);
				append(insertTopologyStatement, "");
				append(insertTopologyStatement, accessAreaName);
				append(insertTopologyStatement, "");//rac
				append(insertTopologyStatement, mcc);
				append(insertTopologyStatement, mnc);
				append(insertTopologyStatement, "0");//lac
				append(insertTopologyStatement, null);
				append(insertTopologyStatement, accessAreaId);
				append(insertTopologyStatement, globalAccessAreaId);
				append(insertTopologyStatement, startTime.toString());
				append(insertTopologyStatement, null);
				append(insertTopologyStatement, hier3IdValue);
				append(insertTopologyStatement, hier32IdValue);
				append(insertTopologyStatement, hier321IdValue);
				append(insertTopologyStatement, vendor);
				append(insertTopologyStatement, ACTIVE);
				append(insertTopologyStatement, startTime.toString());
				append(insertTopologyStatement, startTime.toString());
				append(insertTopologyStatement, "ENIQ_EVENTS", true);
				insertTopologyStatement.append(")");
				
				insertStatements.add(insertTopologyStatement.toString());
				
				if(!check.contains(hier3_Cell_IdValue)){
					insertTopologyStatement = new StringBuilder();
					insertTopologyStatement.append("INSERT INTO ");
					insertTopologyStatement
							.append(NON_ERICSSON_HIER321_CELL_TOPOLOGY_TABLE);
					insertTopologyStatement
							.append(NON_ERICSSON_HIER321_CELL_TOPOLOGY_TABLE_COLUMNS);
					insertTopologyStatement.append(" values (");
					append(insertTopologyStatement,
							String.valueOf(networkType.getRat()));
					append(insertTopologyStatement, controllerName);
					append(insertTopologyStatement, "");
					append(insertTopologyStatement, accessAreaName);
					append(insertTopologyStatement, accessAreaId);
					append(insertTopologyStatement, null);// cell name
					append(insertTopologyStatement, null);// site name
					append(insertTopologyStatement, startTime.toString());
					append(insertTopologyStatement, null);
					append(insertTopologyStatement, hier3IdValue);
					append(insertTopologyStatement, hier32IdValue);
					append(insertTopologyStatement, hier321IdValue);
					append(insertTopologyStatement, hier3_Cell_IdValue);
					append(insertTopologyStatement, vendor);
					append(insertTopologyStatement, ACTIVE);
					append(insertTopologyStatement, startTime.toString());
					append(insertTopologyStatement, startTime.toString());
					append(insertTopologyStatement, "ENIQ_EVENTS", true);
					insertTopologyStatement.append(")");
					insertStatements.add(insertTopologyStatement.toString());
					check.add(hier3_Cell_IdValue);	
				}
				
				
			}

		}
	}

	private void build2G3GSQLConnect(final List<String> insertStatements,
			final List<String> deleteStatements,
			final String selectedController, final String ossId,
			final String vendor, final List<String[]> reparentData,
			final Map<String, List<String>> accessAreaControllerMap,
			final NetworkType networkType) throws NoSuchAlgorithmException, IOException {

		final Timestamp startTime = new Timestamp(System.currentTimeMillis());
		for (final String[] dataToInsert : reparentData) {
			final String controllerName = selectedController;
			final String accessAreaId = dataToInsert[1];
			final String mcc = dataToInsert[2];
			final String mnc = dataToInsert[3];
			final String lac = dataToInsert[4];
			final String rac = dataToInsert[5];

			deleteStatements.add("delete from " + UNKNOWN_TOPOLOGY_TABLE
					+ " where ACCESS_AREA_ID = " + accessAreaId + " and RAT = "
					+ networkType.getRat() + " and MCC = '" + mcc
					+ "' and MNC = '" + mnc + "' and LAC = " + lac);

			final String globalAccessAreaId = buildGlobalAccessAreaId(mcc, mnc,
					lac, accessAreaId);
			final String accessAreaName = buildTemporaryAccessAreaName(
					networkType, globalAccessAreaId);
			final String rat = String.valueOf(networkType.getRat());
			final String hier3IdValue = hashIdGenerator(rat, controllerName,
					EMPTY_STRING, vendor, HIER3_ID);
			final String hier32IdValue = hashIdGenerator(rat, controllerName,
					EMPTY_STRING, vendor, HIER32_ID);
			final String hier321IdValue = hashIdGenerator(rat, controllerName,
					accessAreaName, vendor, HIER321_ID);

			final List<String> accessAreaList;

			if (accessAreaControllerMap.containsKey(controllerName)) {
				accessAreaList = accessAreaControllerMap.get(controllerName);
			} else {
				accessAreaList = new ArrayList<String>();
				accessAreaControllerMap.put(controllerName, accessAreaList);
			}
			if (!accessAreaList.contains(accessAreaName)) {
				accessAreaList.add(accessAreaName);
			}

			final StringBuilder insertTopologyStatement = new StringBuilder();

			insertTopologyStatement.append("INSERT INTO ");

			if (ERICSSON.equalsIgnoreCase(vendor)) {

				insertTopologyStatement.append(ERICSSON_TOPOLOGY_TABLE);
				insertTopologyStatement.append(ERICSSON_TOPOLOGY_TABLE_COLUMNS);
				insertTopologyStatement.append(" values (");
				append(insertTopologyStatement, ossId);
				append(insertTopologyStatement,
						String.valueOf(networkType.getRat()));
				append(insertTopologyStatement, controllerName);
				append(insertTopologyStatement, "");
				append(insertTopologyStatement, accessAreaName);
				append(insertTopologyStatement, rac); // RAC
				append(insertTopologyStatement, mcc);
				append(insertTopologyStatement, mnc);
				append(insertTopologyStatement, lac);
				append(insertTopologyStatement, null);
				append(insertTopologyStatement, accessAreaId);
				append(insertTopologyStatement, globalAccessAreaId);
				append(insertTopologyStatement, startTime.toString());
				append(insertTopologyStatement, null);
				append(insertTopologyStatement, "");
				append(insertTopologyStatement, "1"); // Cell type
				append(insertTopologyStatement, ""); // Cell band
				append(insertTopologyStatement, ""); // Cell layer
				append(insertTopologyStatement, hier3IdValue);
				append(insertTopologyStatement, hier32IdValue);
				append(insertTopologyStatement, hier321IdValue);
				append(insertTopologyStatement, vendor);
				append(insertTopologyStatement, ACTIVE);
				append(insertTopologyStatement, startTime.toString());
				append(insertTopologyStatement, startTime.toString());
				append(insertTopologyStatement, "ENIQ_EVENTS", true);
				insertTopologyStatement.append(")");

			} else {

				insertTopologyStatement.append(NON_ERICSSON_TOPOLOGY_TABLE);
				insertTopologyStatement
						.append(NON_ERICSSON_TOPOLOGY_TABLE_COLUMNS);
				insertTopologyStatement.append(" values (");

				append(insertTopologyStatement, ossId);
				append(insertTopologyStatement,
						String.valueOf(networkType.getRat()));
				append(insertTopologyStatement, controllerName);
				append(insertTopologyStatement, "");
				append(insertTopologyStatement, accessAreaName);
				append(insertTopologyStatement, rac);
				append(insertTopologyStatement, mcc);
				append(insertTopologyStatement, mnc);
				append(insertTopologyStatement, lac);
				append(insertTopologyStatement, null);
				append(insertTopologyStatement, accessAreaId);
				append(insertTopologyStatement, globalAccessAreaId);
				append(insertTopologyStatement, startTime.toString());
				append(insertTopologyStatement, null);
				append(insertTopologyStatement, hier3IdValue);
				append(insertTopologyStatement, hier32IdValue);
				append(insertTopologyStatement, hier321IdValue);
				append(insertTopologyStatement, vendor);
				append(insertTopologyStatement, ACTIVE);
				append(insertTopologyStatement, startTime.toString());
				append(insertTopologyStatement, startTime.toString());
				append(insertTopologyStatement, "ENIQ_EVENTS", true);

				insertTopologyStatement.append(")");

			}

			insertStatements.add(insertTopologyStatement.toString());

		}
	}

	private Map<String, List<String>> buildConnectSQL(
			final List<String> insertStatements,
			final List<String> deleteStatements,
			final String selectedController, final String ossId,
			final String vendor, final List<String[]> reparentData,
			final NetworkType networkType) throws NoSuchAlgorithmException,
			IOException {

		final Map<String, List<String>> accessAreaControllerMap = new HashMap<String, List<String>>();
		if (networkType.equals(NetworkType._4G)) {
			build4GConnectSQL(insertStatements, deleteStatements,
					selectedController, ossId, vendor, reparentData,
					accessAreaControllerMap);
		} else if (networkType.equals(NetworkType._3G_RAN)) {
			build3GRANConnectSQL(insertStatements, deleteStatements,
					selectedController, ossId, vendor, reparentData,
					accessAreaControllerMap, networkType);
		} else {
			build2G3GSQLConnect(insertStatements, deleteStatements,
					selectedController, ossId, vendor, reparentData,
					accessAreaControllerMap, networkType);
		}

		if (log.isDebugEnabled()) {
			log.debug("TOPOLOGY INSERT STATEMENTS " + insertStatements);
			log.debug("TOPOLOGY DELETE STATEMENTS " + deleteStatements);
		}
		return accessAreaControllerMap;
	}
	

	protected String hashIdGenerator(final String rat,
			final String controllerName, final String accessAreaName,

			final String vendor, final String hashIdName)
			throws NoSuchAlgorithmException, IOException {

		final HashIdCreator hashIdCreator = new HashIdCreator();

		String hashIdValue = null;

		final StringBuilder hashIdString = new StringBuilder();

		hashIdString.append(rat).append(PIPE).append(controllerName);

		if (hashIdName.equals(HIER32_ID)) {

			hashIdString.append(PIPE).append(EMPTY_STRING);

		} else if (hashIdName.equals(HIER321_ID)) {

			hashIdString.append(PIPE).append(EMPTY_STRING).append(PIPE)
					.append(accessAreaName);

		} else if (hashIdName.equals(HIER3_CELL_ID)) {
			
			hashIdString.append(PIPE).append(accessAreaName);
			
		}

		hashIdString.append(PIPE).append(vendor);

		final long hashId = hashIdCreator.hashStringToLongId(hashIdString
				.toString());

		hashIdValue = Long.toString(hashId);

		return hashIdValue;

	}

	private String getTopologyActiveStatus(final List<String[]> topologyItems,
			final NetworkType networkType, final Connection conn)
			throws SQLException {
		final StringBuilder sb = new StringBuilder();
		int recordIndex = 0;

		for (final String[] topologyItem : topologyItems) {

			final StringBuilder activeCheckSql = new StringBuilder();
			if (networkType.equals(NetworkType._4G)) {
				activeCheckSql
						.append("SELECT ")
						.append(ERBS_ID)
						.append(", ")
						.append("EUtranCellId")
						.append(" from ")
						.append("DIM_E_LTE_EUCELL")
						.append(WHERE)
						.append(ERBS_ID)
						.append(" = '")
						.append("eNodeB"
								+ getENodeB(topologyItem[CELLID_INDEX_4G]))
						.append("' and ")
						.append("EUtranCellID")
						.append(" = '")
						.append("eNodeB"
								+ getENodeB(topologyItem[CELLID_INDEX_4G])
								+ "-"
								+ getCellId(topologyItem[CELLID_INDEX_4G]))
						.append("' and ").append("MCC").append(" = '")
						.append(topologyItem[MCC_INDEX_4G]).append("' and ")
						.append("MNC").append(" = '")
						.append(topologyItem[MNC_INDEX_4G])
						.append("' and STATUS = 'ACTIVE'");

			} else if (networkType.equals(NetworkType._3G_RAN)) {
				activeCheckSql.append("SELECT DISTINCT ")
				.append("DIM_E_SGEH_HIER321.HIERARCHY_3,")
				.append("DIM_E_SGEH_HIER321.HIERARCHY_1")
				//.append("DIM_E_SGEH_HIER321.MCC,")
				//.append("DIM_E_SGEH_HIER321.MNC,")
				//.append("DIM_E_SGEH_HIER321_CELL.CID,")
				//.append("DIM_E_RAN_RNCFUNCTION.rncId")
				.append(" FROM ")
				.append("DIM_E_RAN_RNC,")
				.append("DIM_E_RAN_RNCFUNCTION,")
				.append("DIM_E_SGEH_HIER321,")
				.append("DIM_E_SGEH_HIER321_CELL")
				.append(" WHERE ")
				.append("DIM_E_RAN_RNC.RNC_ID = DIM_E_RAN_RNCFUNCTION.RNC_ID ")
				.append("and DIM_E_RAN_RNC.ALTERNATIVE_FDN  = DIM_E_SGEH_HIER321.HIERARCHY_3 ")
				.append("and DIM_E_SGEH_HIER321.HIERARCHY_3 = DIM_E_SGEH_HIER321_CELL.HIERARCHY_3 ")
				.append("and DIM_E_SGEH_HIER321.HIERARCHY_3 = DIM_E_SGEH_HIER321_CELL.HIERARCHY_3 ")
				.append("and DIM_E_SGEH_HIER321_CELL.CID = ").append(topologyItem[0])
				.append(" and DIM_E_RAN_RNCFUNCTION.rncId = ").append(topologyItem[1])
				.append(" and DIM_E_SGEH_HIER321.MCC = '").append(topologyItem[2])
				.append("' and DIM_E_SGEH_HIER321.MNC = '").append(topologyItem[3])
				.append("' and DIM_E_SGEH_HIER321.STATUS = 'ACTIVE'");
			}

			else {

				activeCheckSql.append("select ").append(COL_HIERARCHY_3)
						.append(", ").append(COL_HIERARCHY_1)

						.append(" from ").append(ERICSSON_TOPOLOGY_TABLE)
						.append(WHERE).append(COL_ACCESS_AREA_ID)

						.append(" = ")
						.append(topologyItem[ACCESS_AREA_ID_INDEX])
						.append(" and ").append(COL_LAC)

						.append(" = ").append(topologyItem[LAC_INDEX])
						.append(" and ").append(COL_MCC).append(" = '")

						.append(topologyItem[MCC_INDEX]).append("' and ")
						.append(COL_MNC).append(" = '")

						.append(topologyItem[MNC_INDEX]).append("' and ")
						.append(COL_RAT).append(" = ")

						.append(networkType.getRat())
						.append(" and STATUS = 'ACTIVE'");

			}

			if (log.isDebugEnabled()) {

				log.debug(activeCheckSql.toString());

			}

			Statement stmt = conn.createStatement();

			final ResultSet rs = stmt.executeQuery(activeCheckSql.toString());

			while (rs.next()) {

				if (networkType.equals(NetworkType._4G)) {

					sb.append("Access Area with ID ")
							.append(rs.getString("EUtranCellId"))

							.append(" already active and connected to controller ")
							.append(rs.getString(ERBS_ID))

							.append("<br/>");

				} else {
					sb.append("Access Area with ID ")
							.append(rs.getString(COL_HIERARCHY_1))

							.append(" already active and connected to controller ")

							.append(rs.getString(COL_HIERARCHY_3))
							.append("<br/>");

				}
			}

			/*
			 * 
			 * This code is to fix TR HO56950.
			 * 
			 * If the record to be inserted in DIM_E_LTE_EUCELL if already
			 * exists then delete it from DIM_E_LTE_EUCELL.
			 */

			if (networkType.equals(NetworkType._4G)) {

				if (sb.toString().length() > 0) {

					// Clear the Error Message as this not considered as Error
					// as per the discussions of TR HO43435.

					sb.delete(0, sb.length());

					// Delete the Old record from DIM_E_LTE_EUCELL, so that the
					// new record will be inserted

					String delSql = " DELETE FROM DIM_E_LTE_EUCELL WHERE  ERBS_ID = 'eNodeB"
							+ getENodeB(topologyItem[CELLID_INDEX_4G])
							+

							"' AND EUtranCellID = 'eNodeB"
							+ getENodeB(topologyItem[CELLID_INDEX_4G])
							+ "-"

							+ getCellId(topologyItem[CELLID_INDEX_4G])
							+ "' AND MCC= '"
							+ topologyItem[MCC_INDEX_4G]
							+ "'"
							+

							" AND MNC = '"
							+ topologyItem[MNC_INDEX_4G]
							+ "' AND  STATUS = 'ACTIVE'";

					stmt = conn.createStatement();

					stmt.executeUpdate(delSql);

					log.debug("Deleting the record ERBS_ID = 'eNodeB"
							+ getENodeB(topologyItem[CELLID_INDEX_4G])
							+

							"' AND EUtranCellID = 'eNodeB"
							+ getENodeB(topologyItem[CELLID_INDEX_4G])
							+ "-"

							+ getCellId(topologyItem[CELLID_INDEX_4G])
							+ "' AND MCC= '"
							+ topologyItem[MCC_INDEX_4G]
							+ "'"
							+

							" AND MNC = '"
							+ topologyItem[MNC_INDEX_4G]
							+ "' AND  STATUS = 'ACTIVE'from DIM_E_LTE_EUCELL, as it already exists in DIM_E_LTE_EUCELL.");

				}

				recordIndex++;

			}

		}

		return sb.toString();

	}

	private String buildTemporaryAccessAreaName(final NetworkType networkType,
			final String globalAccessAreaId) {

		String accessAreaName;

		if (networkType == NetworkType._2G) {

			accessAreaName = "CELL-" + globalAccessAreaId;

		} else {

			accessAreaName = "SAC-" + globalAccessAreaId;

		}

		return accessAreaName;

	}

	private Template getUnmatchedTopologyForNetworkType(
			final NetworkType networkType, final Context ctx)

	throws Exception {

		try {

			final Connection conn = getDatabaseConnection(ctx);

			final List<String[]> items = getUnmatchedTopology(networkType, conn);

			if (!items.isEmpty()) {

				final String itemString = convertToJavaScriptString(items);

				if (log.isDebugEnabled()) {

					log.debug(itemString);

				}

				ctx.put(CTX_UNMATCHED_TOPOLOGY_ITEMS, itemString);

			}

			ctx.put(CTX_CONTROLLER_LIST,
					getAvailableControllers(networkType, conn));

		} catch (final SQLException e) {

			if (e.getNextException() == null) {

				ctx.put(CTX_ERRORMSG, e.getMessage());

			} else {

				ctx.put(CTX_ERRORMSG, e.getNextException().getMessage());

			}

		}

		return getTemplate(UNKNOWNCELLS_MAIN_VM);

	}

	private static List<String[]> convertFromTokenizedString(
			final String toConvert) {

		final List<String[]> dataRows = new ArrayList<String[]>();

		final StringTokenizer lt = new StringTokenizer(toConvert, "|");

		while (lt.hasMoreTokens()) {

			final String row = lt.nextToken();

			final StringTokenizer rt = new StringTokenizer(row, ",");

			final String[] rowData = new String[rt.countTokens()];

			int index = 0;

			while (rt.hasMoreTokens()) {

				String content = rt.nextToken();

				if (NULL_STRING.equalsIgnoreCase(content)) {

					content = null;

				}

				rowData[index++] = content;

			}

			dataRows.add(rowData);

		}

		return dataRows;

	}

	private static String convertToJavaScriptString(final List<String[]> rowData) {

		final StringBuilder sb = new StringBuilder("[");

		for (int j = 0; j < rowData.size(); j++) {

			final String[] row = rowData.get(j);

			sb.append("[");

			for (int i = 0; i < row.length; i++) {

				sb.append("\"").append(row[i]).append("\"");

				if (i < row.length - 1) {

					sb.append(",");

				}

			}

			sb.append("]");

			if (j < rowData.size() - 1) {

				sb.append(",");

			}

		}

		sb.append("]");

		return sb.toString();

	}

	private static String[] getAvailableControllers(
			final NetworkType networkType, final Connection conn)

	throws SQLException {

		final List<String> parentList = new ArrayList<String>();

		if (networkType.equals(NetworkType._4G)) {

			final String sql = "select distinct ERBS_NAME from DIM_E_LTE_ERBS order by ERBS_NAME";

			final Statement stmt = conn.createStatement();

			final ResultSet rs = stmt.executeQuery(sql);

			try {

				while (rs.next()) {

					final String node = rs.getString("ERBS_NAME").trim();

					parentList.add(node);

				}

			} finally {

				rs.close();

				stmt.close();

			}

		} else {

			final String sql = "select distinct(" + COL_HIERARCHY_3 + ") from "
					+ ERICSSON_TOPOLOGY_TABLE + WHERE

					+ COL_RAT + " = " + networkType.getRat() + " order by "
					+ COL_HIERARCHY_3;

			final Statement stmt = conn.createStatement();

			final ResultSet rs = stmt.executeQuery(sql);

			try {

				while (rs.next()) {

					final String node = rs.getString(COL_HIERARCHY_3).trim();

					parentList.add(node);

				}

			} finally {

				rs.close();

				stmt.close();

			}

		}

		return parentList.toArray(new String[parentList.size()]);

	}

	/**
	 * 
	 * 
	 * 
	 * Get OSS ID reference data from Ericsson topology table
	 * 
	 * 
	 * 
	 * @param networkType
	 * 
	 * 
	 * 
	 * @param conn
	 * 
	 * 
	 * 
	 * @return
	 * 
	 * 
	 * 
	 * @throws SQLException
	 */

	private static String[] getOssIds(final Connection conn,
			final NetworkType networkType) throws SQLException {

		String ossIdQuery;

		if (networkType.equals(NetworkType._4G)) {

			ossIdQuery = "select distinct(OSS_ID) from DIM_E_LTE_ERBS";

		} else {

			ossIdQuery = "select distinct(OSS_ID) from "
					+ ERICSSON_TOPOLOGY_TABLE;

		}

		final Statement stmt = conn.createStatement();

		final ResultSet rs = stmt.executeQuery(ossIdQuery);

		final List<String> ossIds = new ArrayList<String>();

		try {

			while (rs.next()) {

				ossIds.add(rs.getString(1));

			}

		} finally {

			rs.close();

			stmt.close();

		}

		return ossIds.toArray(new String[ossIds.size()]);

	}

	private static List<String[]> getUnmatchedTopology(
			final NetworkType networkType, final Connection conn)
			throws SQLException {
		if (networkType.equals(NetworkType._4G)) {

			// final String UNMATCHED_TOPOLOGY_ITEMS = "SELECT " + COL_4G_CELLID
			// + ", " + COL_4G_MCC + ", " + COL_4G_MNC

			// + ", " + COL_4G_TAC + ", " + COL_4G_EVENT_SOURCE_NAME + ", " +
			// COL_4G_SGSN_IPADDRESS + ", "

			// + COL_4G_FIRST_SEEN + "FROM " + UNKNOWN_TOPOLOGY_TABLE_4G;
			final String UNMATCHED_TOPOLOGY_ITEMS = "SELECT TOP 10000 CELL_ID, MNC, MCC, tac, EVENT_SOURCE_NAME, FIRST_SEEN FROM DIM_E_LTE_UNKNOWN_EUCELL ";

			final List<String[]> unknownTopologyItems = new ArrayList<String[]>();

			final Statement stmt = conn.createStatement();

			final ResultSet rs = stmt.executeQuery(UNMATCHED_TOPOLOGY_ITEMS);

			try {

				while (rs.next()) {

					final String[] item = new String[UNMATCHED_TOPOLOGY_ITEM_SIZE_4G];

					item[CELLID_INDEX_4G] = formatValue(rs
							.getString(COL_4G_CELLID));

					item[MCC_INDEX_4G] = formatValue(rs.getString(COL_4G_MCC));

					item[MNC_INDEX_4G] = formatValue(rs.getString(COL_4G_MNC));

					item[TAC_INDEX_4G] = formatValue(rs.getString(COL_4G_TAC));

					String eventSourceName = formatValue(rs
							.getString(COL_4G_EVENT_SOURCE_NAME));

					eventSourceName = (eventSourceName == null
							|| eventSourceName.isEmpty() ? "Unknown"

					: eventSourceName.trim());

					item[EVENT_SOURCE_NAME_INDEX_4G] = eventSourceName;

					item[FIRST_SEEN_INDEX_4G] = formatValue(rs
							.getString(COL_4G_FIRST_SEEN));

					unknownTopologyItems.add(item);

				}

			} finally {

				rs.close();

				stmt.close();

			}

			return unknownTopologyItems;

		} else if (networkType.equals(NetworkType._3G_RAN)) {
			final String UNMATCHED_TOPOLOGY_ITEMS = "SELECT CID, rncId, MCC, MNC, FIRST_SEEN FROM  DIM_E_RAN_UNKNOWN_HIER3_CELL_ID ";
			final List<String[]> unknownTopologyItems = new ArrayList<String[]>();
			final Statement stmt = conn.createStatement();
			final ResultSet rs = stmt.executeQuery(UNMATCHED_TOPOLOGY_ITEMS);
			try {
				while (rs.next()) {
					final String[] item = new String[5];
					item[0] = formatValue(rs.getString("CID"));
					item[1] = formatValue(rs.getString("rncId"));
					item[2] = formatValue(rs.getString("MCC"));
					item[3] = formatValue(rs.getString("MNC"));
					item[4] = formatValue(rs.getString("FIRST_SEEN"));
					unknownTopologyItems.add(item);
				}
			} finally {
				rs.close();
				stmt.close();
			}
			return unknownTopologyItems;

		} else {

			final String UNMATCHED_TOPOLOGY_ITEMS = "SELECT TOP 10000 "
					+ COL_ACCESS_AREA_ID + ", " + COL_MCC + ", " + COL_MNC

					+ ", " + COL_LAC + ", " + COL_RAC + ", "
					+ COL_EVENT_SOURCE_NAME + ", " + COL_FIRST_SEEN + " "

					+ "FROM " + UNKNOWN_TOPOLOGY_TABLE + " " + "WHERE "
					+ COL_RAT + " = " + networkType.getRat();

			final List<String[]> unknownTopologyItems = new ArrayList<String[]>();

			final Statement stmt = conn.createStatement();

			final ResultSet rs = stmt.executeQuery(UNMATCHED_TOPOLOGY_ITEMS);

			try {

				while (rs.next()) {

					final String[] item = new String[UNMATCHED_TOPOLOGY_ITEM_SIZE];

					item[ACCESS_AREA_ID_INDEX] = formatValue(rs
							.getString(COL_ACCESS_AREA_ID));

					item[MCC_INDEX] = formatValue(rs.getString(COL_MCC));

					item[MNC_INDEX] = formatValue(rs.getString(COL_MNC));

					item[LAC_INDEX] = formatValue(rs.getString(COL_LAC));

					item[RAC_INDEX] = formatValue(rs.getString(COL_RAC));

					String eventSourceName = formatValue(rs
							.getString(COL_EVENT_SOURCE_NAME));

					eventSourceName = (eventSourceName == null
							|| eventSourceName.isEmpty() ? "Unknown"

					: eventSourceName.trim());

					item[EVENT_SOURCE_NAME_INDEX] = eventSourceName;

					item[FIRST_SEEN_INDEX] = formatValue(rs
							.getString(COL_FIRST_SEEN));

					unknownTopologyItems.add(item);

				}

			} finally {

				rs.close();

				stmt.close();

			}

			return unknownTopologyItems;

		}

	}

	private static String formatValue(final String value) {

		return (value != null ? value.trim() : value);

	}

	private static Connection getDatabaseConnection(final Context ctx) {

		final RockFactory dwhDb = (RockFactory) ctx.get("rockDwh");

		return dwhDb.getConnection();

	}

	private static void append(final StringBuilder sb, final String value) {

		append(sb, value, false);

	}

	private static void append(final StringBuilder sb, final String value,
			final boolean isLast) {

		if (value == null) {

			sb.append(NULL_STRING);

		} else {

			sb.append('\'').append(value).append('\'');

		}

		if (!isLast) {

			sb.append(", ");

		}

	}

	private static String buildGlobalAccessAreaId(final String mcc,
			final String mnc, final String lac,

			final String accessAreaId) {

		return new StringBuilder().append(mcc).append("-").append(mnc)
				.append("-").append(lac).append("-")

				.append(accessAreaId).toString();

	}

}
