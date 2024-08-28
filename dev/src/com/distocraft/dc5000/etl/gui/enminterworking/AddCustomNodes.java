package com.distocraft.dc5000.etl.gui.enminterworking;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.ericsson.eniq.common.DatabaseConnections;

public class AddCustomNodes extends EtlguiServlet {//NOSONAR

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(AddCustomNodes.class);

	protected static final String CUSTOM_ENIQ_CONFIG_DIR = "/eniq/sw/conf/custom_fls_config";
	
	protected static final String STANDARD_ENIQ_CONFIG_DIR = "/eniq/sw/conf";
	
	protected static final String ENIQ_INSTALLER_DIR = "/eniq/sw/installer";
	
	private final File customEniqConfigurationXml = new File(CUSTOM_ENIQ_CONFIG_DIR + "/custom_eniq.xml");

	private final File customNodeTechnologyProperties = new File(
			CUSTOM_ENIQ_CONFIG_DIR + "/custom_NodeTechnologyMapping.properties");

	private final File customNodeTypeDataTypeProperties = new File(
			CUSTOM_ENIQ_CONFIG_DIR + "/custom_NodeTypeDataTypeMapping.properties");
	
	private final File standardNodeTechnologyProperties = new File(STANDARD_ENIQ_CONFIG_DIR +"/NodeTechnologyMapping.properties");

	private final File standardNodeTypeDataTypeProperties = new File(ENIQ_INSTALLER_DIR+ "/NodeTypeDataTypeMapping.properties");

	private final File standardEniqConfigurationXml = new File(STANDARD_ENIQ_CONFIG_DIR + "/eniq.xml");

	private static final int MAX_NUM_LINKS = 2000;
	
	private static final String NODE_TYPE = "nodeType";
	private static final String TECHNOLOGY = "technology";
	private static final String TOPO_DATA_TYPE= "topoDataType";
	private static final String TOPO_NODE_TYPE_DIR = "topoNodeTypeDir";
	private static final String TOPO_SUB_DIR = "topoSubDir";
	private static final String PM_DATA_TYPE_OTHERS = "pmDataTypeOthers";
	private static final String PM_DATA_TYPE= "pmDataType";
	private static final String PM_NODE_TYPE_DIR = "pmNodeTypeDir";
	private static final String FILE_FILTER = "fileFilter";
	private static final String NUM_OF_NODES = "numberOfNodes";
	private static final String OTHERS = "Others";
	private static final String PM_STATISTICAL = "PM_STATISTICAL";
	private static final String PATTERN = "^[A-Za-z].*$";
	private static final String CONTINUE_CLICKED = "continueButtonClicked";
	
	private static String nodeType = null;
	private static String techn = null;
	private static String pmDataType = null;
	private static String pmNodeTypeDir = null;
	private static String fileFilter = null;
	private static String numberOfNodes = null;
	private static String topoDataType = null;
	private static String topoNodeTypeDir = null;
	private static String topoSubDir = null;
	private static int numberOfPMSubdirs = 0;
	private static boolean fileFilterCheck = true;

	@SuppressWarnings("deprecation")
	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) {

		Template template = null;
		final String page = "addNewCustomNodes.vm";
		final Map<String, String> technologyMapping = WriteAndReadDataFromNodeTechProps.getNodeTechnologyMapping(standardNodeTechnologyProperties);

		
		String continueButton = StringEscapeUtils.escapeHtml(request.getParameter("Continue"));
		String submitButton = StringEscapeUtils.escapeHtml(request.getParameter("Submit"));

		populateTechnologyValues(ctx, technologyMapping);
		populateDataTypeValues(ctx);
		setSessionAttributes(request.getSession(false));
		setRequestParameters(request, ctx);

		if(continueButton != null) {
			ctx.put(CONTINUE_CLICKED, true);
			log.info("Values Passed= nodeType: " + nodeType + "--\n" + "PM dataType: " + pmDataType + "--\n" + "technology: "
					+ techn + "--\n" + "topoNodeTypeDir: " + topoNodeTypeDir + "--\n" + "topoSubDir: " + topoSubDir + "--\n"
					+ "numberOfNodes: " + numberOfNodes + "--\n" + "nodeDataGranularity: " + "--\n"
					+ "pmNodeTypeDir: " + pmNodeTypeDir + "--\n" + "numberOfPMSubdirs: " + numberOfPMSubdirs + "--\n" + "fileFilter: "
					+ fileFilter + "--\n");
			continueResponseHandling(ctx, request);
		}
		if(submitButton != null) {
			boolean ifSuccessfullyWroteDataInEniqXml = false;
			boolean ifSuccessfullyWroteDataInNodeTechProperties = false;
			boolean ifSuccessfullyWroteDataInNodeTypeDataTypeProperties = false;
			WriteAndReadDataFromEniqXml customEniqXmlObj = new WriteAndReadDataFromEniqXml(nodeType, pmDataType,
					techn, topoNodeTypeDir, topoSubDir, topoDataType, pmNodeTypeDir, numberOfPMSubdirs,
					fileFilter);
			ifSuccessfullyWroteDataInEniqXml = customEniqXmlObj.writeDataIntoXml();
			boolean ifDataMatchesInNodeTechProperties = checkIfValueMatchesWithStandardEniqForNodeTechProp(nodeType,
					techn, technologyMapping);
			boolean ifDataMatchesInNodeTypeDataTypeProperties = checkIfValueMatchesWithStandardEniqForNodeTypeDataTypeProp(
					nodeType, pmDataType, techn);
			if(ifSuccessfullyWroteDataInEniqXml && !ifDataMatchesInNodeTechProperties) {
				WriteAndReadDataFromNodeTechProps nodeTechObj = new WriteAndReadDataFromNodeTechProps(nodeType,
						techn);
				ifSuccessfullyWroteDataInNodeTechProperties = nodeTechObj.writeDataIntoNodeTechProperties();				
			}
			if(ifSuccessfullyWroteDataInEniqXml && !ifDataMatchesInNodeTypeDataTypeProperties) {
				WriteAndReadDataFromNodeTypeDataTypeFile nodeTypeDataTypeObj = new WriteAndReadDataFromNodeTypeDataTypeFile(
						nodeType, pmDataType, techn);
				ifSuccessfullyWroteDataInNodeTypeDataTypeProperties = nodeTypeDataTypeObj
						.writeDataInNodeTypeDataTypeFile();				
			}
			if(ifSuccessfullyWroteDataInNodeTechProperties) {
				log.info("Data Successfully written in custom NodeTechMapping properties file");
			}
			if(ifSuccessfullyWroteDataInNodeTypeDataTypeProperties) {
				log.info("Data Successfully written in custom NodeTypeDataTypeMapping properties file");
			}
			if (ifSuccessfullyWroteDataInEniqXml) {
				log.info("Data Successfully written in custom eniq.xml file");
				setSessionAttributes(request.getSession(false));
				ctx.put("dataSucessfullyEntered", true);
			}
		}
		try {
			template = getTemplate(page);
		} catch (Exception e) {
			log.warn("Custom Nodes velocity template not found " + e);
		}
		return template;
	}

	private static void setRequestParameters(HttpServletRequest request, Context ctx) {//NOSONAR
		final HttpSession session = request.getSession(false);
		nodeType = StringEscapeUtils.escapeHtml(request.getParameter(NODE_TYPE));
		techn = StringEscapeUtils.escapeHtml(request.getParameter(TECHNOLOGY));
		String pmDataTypeOthers = StringEscapeUtils.escapeHtml(request.getParameter(PM_DATA_TYPE_OTHERS));
		pmDataType = StringEscapeUtils.escapeHtml(request.getParameter(PM_DATA_TYPE));
		pmNodeTypeDir = StringEscapeUtils.escapeHtml(request.getParameter(PM_NODE_TYPE_DIR));
		fileFilter = StringEscapeUtils.escapeHtml(request.getParameter(FILE_FILTER));
		numberOfNodes = StringEscapeUtils.escapeHtml(request.getParameter(NUM_OF_NODES));
		topoDataType = StringEscapeUtils.escapeHtml(request.getParameter(TOPO_DATA_TYPE));
		topoNodeTypeDir = StringEscapeUtils.escapeHtml(request.getParameter(TOPO_NODE_TYPE_DIR));
		topoSubDir = StringEscapeUtils.escapeHtml(request.getParameter(TOPO_SUB_DIR));
		String backButton = StringEscapeUtils.escapeHtml(request.getParameter("Back"));
		
		if (nodeType != null && ("").equals(nodeType.trim())) {
			nodeType = null;
			ctx.put("emptyNodeType", true);
		} else if (nodeType != null && nodeType.matches(PATTERN)) {
			nodeType = nodeType.trim();
			session.setAttribute(NODE_TYPE, nodeType);
		} else if (nodeType != null) {
			session.setAttribute(NODE_TYPE, nodeType);
			nodeType = null;
			ctx.put("wrongNodeType", true);
		}

		if (techn != null && ("").equals(techn.trim())) {
			techn = null;
			ctx.put("wrongTechnologyValue", true);
		} else if (techn != null) {
			techn = techn.trim();
			session.setAttribute(TECHNOLOGY, techn);
		}

		if (topoDataType != null && (("").equals(topoDataType.trim()))) {
			topoDataType = null;
			ctx.put("topologyDataTypeEmpty", true);
		} else if (topoDataType != null && techn != null && topoDataType.startsWith("TOPOLOGY_")) {
			topoDataType = topoDataType.trim();
			session.setAttribute(TOPO_DATA_TYPE, topoDataType);
			String[] values = topoDataType.split("_");
			if(values.length != 2 || !values[1].equals(techn)) {
				topoDataType = null;
				ctx.put("wrongTopologyDataType", true);
			}
		} else if (topoDataType != null) {
			session.setAttribute(TOPO_DATA_TYPE, topoDataType);
			topoDataType = null;
			ctx.put("wrongTopologyDataType", true);
		}
		
		if (topoNodeTypeDir != null && ("").equals(topoNodeTypeDir.trim())) {
			topoNodeTypeDir = null;
			ctx.put("emptyTopoNodeTypeDir", true);
		} else if (topoNodeTypeDir != null && topoNodeTypeDir.matches(PATTERN)) {
			topoNodeTypeDir = topoNodeTypeDir.trim();
			session.setAttribute(TOPO_NODE_TYPE_DIR, topoNodeTypeDir);
		} else if(topoNodeTypeDir != null) {
			session.setAttribute(TOPO_NODE_TYPE_DIR, topoNodeTypeDir);
			topoNodeTypeDir = null;
			ctx.put("wrongTopoNodeTypeDir", true);
		}

		if (topoSubDir != null && ("").equals(topoSubDir.trim())) {
			topoSubDir = null;
		} else if (topoSubDir != null && topoSubDir.matches(PATTERN)) {
			topoSubDir = topoSubDir.trim();
			session.setAttribute(TOPO_SUB_DIR, topoSubDir);
		} else if(topoSubDir != null) {
			session.setAttribute(TOPO_SUB_DIR, topoSubDir);
			topoSubDir = null;
			ctx.put("wrongTopoSubDir", true);
		}

		if (pmDataTypeOthers != null && ("").equals(pmDataTypeOthers.trim())) {
			pmDataTypeOthers = null;
			ctx.put("emptyPmDataTypeOthers", true);
		} else if (pmDataTypeOthers != null && pmDataTypeOthers.matches(PATTERN)) {
			pmDataTypeOthers = pmDataTypeOthers.trim().toUpperCase();
			session.setAttribute(PM_DATA_TYPE_OTHERS, pmDataTypeOthers);
		} else if (pmDataTypeOthers != null) {
			session.setAttribute(PM_DATA_TYPE_OTHERS, pmDataTypeOthers);
			pmDataTypeOthers = null;
			ctx.put("wrongPmDataTypeOthers", true);
		} else if(pmDataTypeOthers == null && backButton != null && !(PM_STATISTICAL).equals(pmDataType)) {
			session.setAttribute(PM_DATA_TYPE_OTHERS, pmDataType);
		}

		if (pmDataType != null && (("").equals(pmDataType))) {
			pmDataType = null;
			ctx.put("emptyPmDataType", true);
		} else if ((OTHERS.equals(pmDataType) && pmDataTypeOthers == null)) {
			pmDataType = null;
			session.setAttribute(PM_DATA_TYPE, OTHERS);
		} else if ((pmDataType != null && OTHERS.equals(pmDataType))) {
			pmDataType = pmDataTypeOthers;
			session.setAttribute(PM_DATA_TYPE, OTHERS);
		} else  if (pmDataType != null) {
			pmDataType = pmDataType.trim();
			session.setAttribute(PM_DATA_TYPE, pmDataType);
		}
		
		if(backButton != null && (PM_STATISTICAL).equals(pmDataType)) {
			session.setAttribute(PM_DATA_TYPE, pmDataType);
		} else if(backButton != null){
			session.setAttribute(PM_DATA_TYPE, OTHERS);
		} 
		
		if (numberOfNodes != null) {
			try {
				numberOfPMSubdirs = Integer.parseInt(numberOfNodes) / MAX_NUM_LINKS;
				numberOfPMSubdirs = numberOfPMSubdirs == 0 ? 1 : numberOfPMSubdirs;
			} catch (NumberFormatException e) {
				log.warn("Exception while converting numerOfNodes field to integer: " + e);
			}
			session.setAttribute(NUM_OF_NODES, numberOfNodes);
		}

		if (pmNodeTypeDir != null && ("").equals(pmNodeTypeDir.trim())) {
			pmNodeTypeDir = null;
			ctx.put("emptyPmNodeTypeDir", true);
		} else if (pmNodeTypeDir != null && pmNodeTypeDir.matches(PATTERN)) {
			pmNodeTypeDir = pmNodeTypeDir.trim();
			session.setAttribute(PM_NODE_TYPE_DIR, pmNodeTypeDir);
		} else if (pmNodeTypeDir != null){
			session.setAttribute(PM_NODE_TYPE_DIR, pmNodeTypeDir);
			pmNodeTypeDir = null;
			ctx.put("wrongPmNodeTypeDir", true);
		}
		if(pmNodeTypeDir != null && topoNodeTypeDir != null && pmNodeTypeDir.equals(topoNodeTypeDir)) {
			ctx.put("sameNodeTypeDir", true);
			pmNodeTypeDir = null;
			topoNodeTypeDir = null;
		}
		if (fileFilter != null && ("").equals(fileFilter.trim())) {
			fileFilter = null;
		} else if (fileFilter != null) {
			fileFilter = fileFilter.trim();
			session.setAttribute(FILE_FILTER, fileFilter);
			try {
				Pattern.compile(fileFilter);
			} catch (PatternSyntaxException e) {
				log.warn("Invalid file filter pattern entered by user: " + e);
				ctx.put("invalidRegexPattern", true);
				fileFilter = null;
				fileFilterCheck = false;
			}
		}
	}

	private void continueResponseHandling(Context ctx, HttpServletRequest request) {
		boolean ifDataMatches = false;
		boolean ifDataMatchesInCustomEniqXml = false;
		boolean ifSuccessfullyCreatedDirectoriesAndFiles = false;
		if (nodeType != null && pmDataType != null && techn != null && topoNodeTypeDir != null && numberOfPMSubdirs > 0
				&& pmNodeTypeDir != null && topoDataType != null && fileFilterCheck) {
			ifDataMatches = checkIfValueMatchesWithStandardEniqForEniqXml(ctx);
			ifDataMatchesInCustomEniqXml = checkIfValueMatchesWithCustomEniqXml(ctx);
			ifSuccessfullyCreatedDirectoriesAndFiles = createDirandFiles();
			ctx.put("dataMatchesForEniqXML", ifDataMatches);
			ctx.put("dataMatchesForCustomEniqXml", ifDataMatchesInCustomEniqXml);
			ctx.put("ifSuccessfullyCreatedDirectoriesAndFiles", !ifSuccessfullyCreatedDirectoriesAndFiles);
		}else {
			ctx.put(CONTINUE_CLICKED, false);
		}
		if (ifDataMatches || ifDataMatchesInCustomEniqXml || !ifSuccessfullyCreatedDirectoriesAndFiles) {
			ctx.put(CONTINUE_CLICKED, false);
		} else {
			request.getSession(false).setAttribute(PM_DATA_TYPE, pmDataType);
		}
	}

	private boolean checkIfValueMatchesWithCustomEniqXml(Context ctx) {
		ConcurrentMap<String, Map<String, SymbolicLinkSubDirConfiguration>> hm = WriteAndReadDataFromEniqXml
				.readEniqXmlFile(customEniqConfigurationXml);
		String pmNeType = WriteAndReadDataFromEniqXml.getNeType(nodeType, pmDataType);
		for(Entry<String, Map<String, SymbolicLinkSubDirConfiguration>> neType: hm.entrySet()) {
			Map<String, SymbolicLinkSubDirConfiguration> nodeTypeDir = neType.getValue();
			for(Entry<String, SymbolicLinkSubDirConfiguration> node : nodeTypeDir.entrySet()) {
				SymbolicLinkSubDirConfiguration sym = node.getValue();
				if(sym.getName().equals(pmNeType) && sym.getNodeTypeDir().equals(pmNodeTypeDir)) {
					log.warn("PM Data Entered matches with custom ENIQ for NodeTypeDir: Entered Details: [NodeType: " + nodeType + ", PM DataType:" + pmDataType + ", TopoNodeTypeDir: " + topoNodeTypeDir + ",topoSubDir: " + topoSubDir
							+ ", pmNodeTypeDir:" + pmNodeTypeDir + "]");
					ctx.put("customPmNodeTypeDirMatches", true);
					return true;
				}
			}
		}
		return false;
	}

	private void setSessionAttributes(HttpSession session) {
		session.setAttribute(NODE_TYPE, "");
		session.setAttribute(TECHNOLOGY, "");
		session.setAttribute(TOPO_DATA_TYPE, "");
		session.setAttribute(TOPO_NODE_TYPE_DIR, "");
		session.setAttribute(TOPO_SUB_DIR, "");
		session.setAttribute(PM_DATA_TYPE_OTHERS, "");
		session.setAttribute(PM_DATA_TYPE, "");
		session.setAttribute(PM_NODE_TYPE_DIR, "");
		session.setAttribute(FILE_FILTER, "");
		session.setAttribute(NUM_OF_NODES, "");
	}

	private void populateDataTypeValues(Context ctx) {
		Set<String> pmDataTypes = new LinkedHashSet<>();
		pmDataTypes.add(PM_STATISTICAL);
		pmDataTypes.add(OTHERS);
		ctx.put("pmDataTypes", pmDataTypes);
	}

	/*
	 * Returns boolean value
	 * True: If values matches with the standard eniq 
	 * False: If it does not match with the standard eniq - expected
	 */
	private boolean checkIfValueMatchesWithStandardEniqForNodeTypeDataTypeProp(String nodeType, String dataType,
			String technology) {
		Map<String, Map<String, Set<String>>> nodeDatatypeMapping = WriteAndReadDataFromNodeTypeDataTypeFile.readNodeTypeDataTypeFile(
				standardNodeTypeDataTypeProperties);
		log.debug("Standard ENIQ nodeTypeDataTypeMapping properties map value after parsing: " + nodeDatatypeMapping);
		Map<String, Set<String>> techDataTypeMapping = nodeDatatypeMapping.get(nodeType);
		if (techDataTypeMapping == null) {
			return false;
		}
		log.info("techDataTypeMapping: "+techDataTypeMapping);
		List<String> technologyMatching = techDataTypeMapping.keySet().stream()
				.filter(tech -> {
					String[] techs = tech.split(",");
					for(String t: techs) {
						if(t.equals(technology))
							return true;
					}
					return false;
				}).collect(Collectors.toList());
		if (technologyMatching.isEmpty()) {
			return false;
		}
		boolean dataTypeCheck =  checkIfDataTypeMatches(techDataTypeMapping, technologyMatching, dataType);
		if(dataTypeCheck)
			log.info("Data Entered matches with standard NodeTypeDataTypeMapping properties file: data entered=[" + "NodeType: "
				+ nodeType + ", DataType: " + dataType + ", technology: " + technology + "]");
		return dataTypeCheck;
	}
	
	private boolean checkIfDataTypeMatches(Map<String, Set<String>> techDataTypeMapping,
			List<String> technologyMatching, String dataType) {
		boolean dataTypeCheck = false;
		for (String tech : technologyMatching) {
			dataTypeCheck = techDataTypeMapping.get(tech).stream().anyMatch(data -> data.equals(dataType));
			if (dataTypeCheck)
				break;
		}
		return dataTypeCheck;
	}

	/*
	 * Returns boolean value
	 * True: If values matches with the standard eniq 
	 * False: If it does not match with the standard eniq - expected
	 */
	private boolean checkIfValueMatchesWithStandardEniqForNodeTechProp(String nodeType, String technology, Map<String, String> technologyMapping) {
		log.debug("Map of Standard NodeTechMapping properties file after parsing is: "+technologyMapping);
		boolean result = true;
		if (!technologyMapping.isEmpty()) {
			String nodeTypes = technologyMapping.get(technology);
			Predicate<String> node = t -> t.equals(nodeType);
			if (nodeTypes == null || Arrays.stream(nodeTypes.split(",")).noneMatch(node)) {
				result = false;
			}
		}
		if(result)
			log.info("Data Entered matches with standard NodeTechMapping properties file: data=["+"NodeType: "+nodeType+", technology:"+technology+"]");
		return result;
	}

	/*
	 * Returns boolean value
	 * True: If values matches with the standard eniq
	 * False: If it does not match with the standard eniq - expected
	 */
	private boolean checkIfValueMatchesWithStandardEniqForEniqXml(Context ctx) {
		ConcurrentMap<String, Map<String, SymbolicLinkSubDirConfiguration>> standardEniqXmlMap = WriteAndReadDataFromEniqXml
				.readEniqXmlFile(standardEniqConfigurationXml);
		String pmNeType = WriteAndReadDataFromEniqXml.getNeType(nodeType, pmDataType);
		String topoNeType = WriteAndReadDataFromEniqXml.getNeType(nodeType, topoDataType);
		log.debug("Standard eniq.xml after parsing: hashmap value is:" + standardEniqXmlMap);
		for(Entry<String, Map<String, SymbolicLinkSubDirConfiguration>> neType: standardEniqXmlMap.entrySet()) {
			Map<String, SymbolicLinkSubDirConfiguration> nodeTypeDir = neType.getValue();
			for(Entry<String, SymbolicLinkSubDirConfiguration> node : nodeTypeDir.entrySet()) {
				SymbolicLinkSubDirConfiguration sym = node.getValue();
				if(sym.getName().equals(pmNeType) && sym.getNodeTypeDir().equals(pmNodeTypeDir)) {
					log.warn("PM Data Entered matches with standard ENIQ for NodeTypeDir: Entered Details: [NodeType: " + nodeType + ", PM DataType: " + pmDataType + ",technology: "
							+ techn + ", TopoNodeTypeDir: " + topoNodeTypeDir + ", topoSubDir: " + topoSubDir
							+ ", pmNodeTypeDir: " + pmNodeTypeDir + "]");
					ctx.put("pmNodeTypeDirMatches", true);
					return true;
				}
				if(sym.getName().equals(topoNeType) && sym.getNodeTypeDir().equals(topoNodeTypeDir) && sym.getSubDirs().get(0).equals(topoSubDir)) {
					log.warn("Topology data Entered matches with standard ENIQ:  standardEniqTopologyData" + sym
							+ "Entered details: [NodeType: " + nodeType + ", Topo DataType: " + topoDataType + ",Technology: "
							+ techn + ",topoNodeTypeDir: " + topoNodeTypeDir + ",topoSubDir: " + topoSubDir
							+ ",pmNodeTypeDir: " + pmNodeTypeDir + "]");
					ctx.put("topoNodeTypeDirMatches", true);
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * Returns boolean value
	 * True: If successfully created custom directory and files
	 * False: If it did not
	 */
	private boolean createDirandFiles() {
		File directory = new File(CUSTOM_ENIQ_CONFIG_DIR);
		if (!directory.exists()) {
			directory.mkdir();
		}
		boolean customEniqXMLCreated = false;
		boolean customNodeTechCreated = false;
		boolean customNodeTypeDataTypeCreated = false;
		if (directory.exists()) {
			try {
				if (!customEniqConfigurationXml.exists())
					customEniqXMLCreated = customEniqConfigurationXml.createNewFile();
				if (!customNodeTechnologyProperties.exists())
					customNodeTechCreated = customNodeTechnologyProperties.createNewFile();
				if (!customNodeTypeDataTypeProperties.exists())
					customNodeTypeDataTypeCreated = customNodeTypeDataTypeProperties.createNewFile();
			} catch (IOException e) {
				log.warn("Unable to create new custom files: " + e);
				return false;
			}
		} else {
			log.warn("Unable to create custom directory for custom Nodes");
			return false;
		}
		if(customEniqXMLCreated)
			log.info("Custom Eniq Xml successfully created");
		if(customNodeTechCreated)
			log.info("Custom Node Tech Properties file successfully created");
		if(customNodeTypeDataTypeCreated)
			log.info("Custom Node Type DataType Properties file successfully created");
		return true;
	}

	// To display the technology Values shown in dropdown
	private Set<String> populateTechnologyValues(Context ctx, Map<String, String> technologyMapping) {
		Set<String> technologyList = new HashSet<>();
		if (!technologyMapping.isEmpty()) {
			technologyList = technologyMapping.keySet();
			ctx.put("technologyList", technologyList);
		} else {
			ctx.put("unableToFetchTechList", true);
		}
		return technologyList;
	}
	
	/*Returns boolean value
	 * True: If Successfully wrote the custom file with the data provided - expected
	 * False: If any error comes while writing
	 */
	public static boolean writeToCustomFiles(Path path, List<String> lines) {
		try {
			Files.write(path, lines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.warn("Exception while writing to custom file="+path+" : "+e);
			return false;
		}
		return true;
	}

	/*
	 * Add NodeTypeGranularity table with the custom nodetype Returns boolean value
	 * True: If it successfully added the granularity value in NodeTypeGranularity table. - expected
	 * False: If it did not.
	 */
	public static boolean addGranularityValueInRepdb(String technology, String nodeType) {
		String sqlCheck = "SELECT * FROM NodeTypeGranularity WHERE NODE_TYPE=? AND TECHNOLOGY=?";
		String sqlInsert = "INSERT INTO NodeTypeGranularity (TECHNOLOGY, NODE_TYPE, DEFAULT_GRANULARITY, CONFIGURED_GRANULARITY) VALUES (?, ?, '15MIN', ?)";
		ResultSet rsCheck = null;
		int rsUpdate = 0;
		boolean result = true;
		try (Connection connDwhrep = DatabaseConnections.getDwhRepConnection().getConnection();
				PreparedStatement stmtCheck = connDwhrep.prepareStatement(sqlCheck);
				PreparedStatement stmtInsert = connDwhrep.prepareStatement(sqlInsert);) {
			stmtCheck.setString(1, nodeType);
			stmtCheck.setString(2, technology);
			rsCheck = stmtCheck.executeQuery();
			if (rsCheck.next()) {
				log.info("The entry with technology="+technology+" and node type="+nodeType+" in nodetypegranularity table is already present.");
			} else {
				stmtInsert.setString(1, technology);
				stmtInsert.setString(2, nodeType);
				stmtInsert.setString(3, "15MIN");
				rsUpdate = stmtInsert.executeUpdate();
				log.info("Successfully Inserted the entry in NodeTypeGranularity Tablewith technology="+technology+" and node type="+nodeType+" rs=" + rsUpdate);
			}
		} catch (SQLException e) {
			log.warn("Error while querying NodeTypeGranularity table: " + e);
			result = false;
		} finally {
			try {
				if (rsCheck != null)
					rsCheck.close();
			} catch (SQLException e) {
				log.warn("Error while closing resultset: " + e);
			}
		}
		return result;
	}
}
