package com.distocraft.dc5000.etl.gui.enminterworking;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.ericsson.eniq.common.RemoteExecutor;
import com.jcraft.jsch.JSchException;

public class DisplayCustomNodes extends EtlguiServlet {// NOSONAR

	/**
	 * 
	 */
	private static final long serialVersionUID = 4793701546197423233L;

	private static final Log log = LogFactory.getLog(DisplayCustomNodes.class);

	private static final File customEniqConfigurationXml = new File(
			AddCustomNodes.CUSTOM_ENIQ_CONFIG_DIR + "/custom_eniq.xml");

	private static final File customNodeTypeDataTypeProperties = new File(
			AddCustomNodes.CUSTOM_ENIQ_CONFIG_DIR + "/custom_NodeTypeDataTypeMapping.properties");

	private static final File standardNodeTypeDataTypeProperties = new File(
			AddCustomNodes.ENIQ_INSTALLER_DIR + "/NodeTypeDataTypeMapping.properties");

	private static final File standardNodeTechnologyProperties = new File(
			AddCustomNodes.STANDARD_ENIQ_CONFIG_DIR + "/NodeTechnologyMapping.properties");
	
	private static final String BASE_PATH_FOR_FLS = "/eniq/sw/bin";//NOSONAR
	
	private final File flsRestartFile = new File(BASE_PATH_FOR_FLS + "/fls");
	
	private static final String APPLICATION_USER = "dcuser";

	private static final String HOST_ADD = "scheduler";
	
	private final File customNodeTechnologyProperties = new File(AddCustomNodes.CUSTOM_ENIQ_CONFIG_DIR + "/custom_NodeTechnologyMapping.properties");

	private static Set<String> dataTypes = new HashSet<>();
	
	private static final String FLS_STARTED = "ifFLSSucessfullyRestarted";
	private static final String FLS_RESTART = "FLSRestart";
	private static final String TOPO = "TOPOLOGY";

	@SuppressWarnings("deprecation")
	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		String page = "displayCustomNodes.vm";
		Template template = null;
		List<SymbolicLinkSubDirConfiguration> eniqXmlList = new ArrayList<>();
		String deleteButtonResponse = StringEscapeUtils.escapeHtml(request.getParameter("Delete"));
		if (customEniqConfigurationXml.exists() && customEniqConfigurationXml.length() > 0) {
			getAllDataTypes();
			Map<String, Set<String>> technologyMapping = consolidateMapping();
			DirectoryConfigurationFileParser parser = new DirectoryConfigurationFileParser(log,
					customEniqConfigurationXml);
			ConcurrentMap<String, Map<String, SymbolicLinkSubDirConfiguration>> eniqXmlMap = parser
					.getSymbolicLinkSubDirConfigurations();
			if (!eniqXmlMap.isEmpty()) {
				if(deleteButtonResponse != null) {
					handleDeleteButton(deleteButtonResponse, eniqXmlMap);
				}
				eniqXmlMap.keySet().stream().forEach(neType -> {
					Map<String, SymbolicLinkSubDirConfiguration> typeDir = eniqXmlMap.get(neType);
					typeDir.keySet().stream().forEach(node -> eniqXmlList.add(typeDir.get(node)));
				});
				divideNeType(eniqXmlList, technologyMapping);
				mapEmptytechnology(eniqXmlList, technologyMapping);
				addInHashMap(eniqXmlList, ctx);
			}
			String flsRestartButton = StringEscapeUtils.escapeHtml(request.getParameter(FLS_RESTART));
			if(flsRestartButton != null) {
				restartFLS(ctx);
			}
			log.debug("All symbolicLinks: " + eniqXmlList);
			if(eniqXmlList.isEmpty()) {
				ctx.put("noCustomNodesConfigured", true);
			}
		} else {
			ctx.put("noCustomNodesConfigured", true);
		}
		try {
			template = getTemplate(page);
		} catch (Exception e) {
			log.warn("Display Nodes velocity template not found " + e);
		}
		return template;
	}

	private void handleDeleteButton(String deleteButtonResponse,
			ConcurrentMap<String, Map<String, SymbolicLinkSubDirConfiguration>> eniqXmlMap) {
		String[] values = deleteButtonResponse.split(":::");
		
		String techn = values[0];
		String nodeType = values[1];
		String dataType = values[2];
		String nodeTypeDir = values[3];
		List<String> subDirs = Arrays.asList(values[4].substring(values[4].indexOf('[')+1, values[4].lastIndexOf(']')).split(","));
		String fileFilter = ("-").equals(values[5]) ? "" : values[5];
		log.info("Received values to be deleted: " + nodeType + "--\n" + "technology: " + techn + "--\n" + "dataType: "
					+ dataType + "--\n" + "nodeTypeDir: " + nodeTypeDir + "--\n" + "subDirs: " + subDirs + "--\n"
					+ "nodeDataGranularity: " + "--\n" + "fileFilter: " + fileFilter + "--\n");
		SymbolicLinkSubDirConfiguration sym = new SymbolicLinkSubDirConfiguration();
		sym.setNodeTypeDir(nodeTypeDir);
		sym.setFileFilter(fileFilter);
		sym.setName(dataType+"_"+nodeType);
		subDirs.forEach(sym::addSubDir);
		Map<String, SymbolicLinkSubDirConfiguration> nodeDirs = eniqXmlMap.get(sym.getName());
		if(nodeDirs != null) {
			SymbolicLinkSubDirConfiguration symbolic = nodeDirs.get(sym.getNodeTypeDir());
			if( symbolic != null) {
				nodeDirs.remove(sym.getNodeTypeDir());
				if(nodeDirs.size()>0)
					eniqXmlMap.put(sym.getName(), nodeDirs);
				else {
					eniqXmlMap.remove(sym.getName());
				}
			}
			log.info("Deleted from custom eniq.xml file");
		}
		WriteAndReadDataFromEniqXml customDelete = new WriteAndReadDataFromEniqXml();
		customDelete.writeDataIntoCustomXml(eniqXmlMap);
		if(dataType.startsWith(TOPO)) {
			deleteFromNodeTechnologyMapping(nodeType,techn);
		}else {
			deleteFromNodeTypeDataTypeFile(nodeType,dataType,techn);			
		}
	}

	private void mapEmptytechnology(List<SymbolicLinkSubDirConfiguration> eniqXmlList,
			Map<String, Set<String>> technologyMapping) {
		eniqXmlList.stream().forEach(sym -> {
			String technology = sym.getTechnology();
			String node = sym.getNode();
			if(("").equals(technology)) {
				Optional<Set<String>> opTechnology = technologyMapping.values().stream().filter(nodes -> nodes.contains(node)).findFirst();
				if (opTechnology.isPresent()) {
					Set<String> nodeValues = opTechnology.get();
					Optional<String> opTech = technologyMapping.keySet().stream().filter(tech -> technologyMapping.get(tech) == (nodeValues)).findFirst();
					technology = opTech.isPresent() ? opTech.get() : "";
					sym.setTechnology(technology);
				}
			}
		});
	}

	private Map<String, Set<String>> consolidateMapping() {
		
		Map<String, Set<String>> technologyNodeMapping = new HashMap<>();
		Map<String, String> standardTechnologyNodeMapping = WriteAndReadDataFromNodeTechProps.getNodeTechnologyMapping(standardNodeTechnologyProperties);
		Map<String, String> customTechnologyNodeMapping = WriteAndReadDataFromNodeTechProps.getNodeTechnologyMapping(customNodeTechnologyProperties);
		for(Entry<String, String> tech : customTechnologyNodeMapping.entrySet()) {
			Set<String> nodeTypes = technologyNodeMapping.get(tech.getKey());
			if(nodeTypes == null) {
				nodeTypes = new HashSet<>();
			}
			Arrays.stream(tech.getValue().split(",")).forEach(nodeTypes::add);
			technologyNodeMapping.put(tech.getKey(), nodeTypes);
		}
		for(Entry<String, String> tech : standardTechnologyNodeMapping.entrySet()) {
			Set<String> nodeTypes = technologyNodeMapping.get(tech.getKey());
			if(nodeTypes == null) {
				nodeTypes = new HashSet<>();
			}
			Arrays.stream(tech.getValue().split(",")).forEach(nodeTypes::add);
			technologyNodeMapping.put(tech.getKey(), nodeTypes);
		}
		return technologyNodeMapping;
	}

	private void addInHashMap(List<SymbolicLinkSubDirConfiguration> eniqXmlList, Context ctx) {
		Map<String, Map<String, Set<SymbolicLinkSubDirConfiguration>>> hm = new HashMap<>();
		for (SymbolicLinkSubDirConfiguration sym : eniqXmlList) {
			String tech = sym.getTechnology();
			String node = sym.getNode();
			if(("".equals(tech))) {
				continue;
			}
			Map<String, Set<SymbolicLinkSubDirConfiguration>> nodeWithSym = hm.get(tech);
			if (nodeWithSym == null) {
				nodeWithSym = new HashMap<>();
				Set<SymbolicLinkSubDirConfiguration> list = new HashSet<>();
				list.add(sym);
				nodeWithSym.put(node, list);
				hm.put(tech, nodeWithSym);
			} else {
				Set<SymbolicLinkSubDirConfiguration> symsList = nodeWithSym.get(node);
				if (symsList == null) {
					symsList = new HashSet<>();
					symsList.add(sym);
					nodeWithSym.put(node, symsList);
				} else {
					if (!symsList.contains(sym)) {
						symsList.add(sym);
					}
				}
			}
		}
		addRowSpanInContext(hm, ctx);
		ctx.put("eniqXmlMap", hm);
	}

	private void addRowSpanInContext(Map<String, Map<String, Set<SymbolicLinkSubDirConfiguration>>> hm, Context ctx) {
		HashMap<String, Integer> rowSpan = new HashMap<>();
		for(Entry<String, Map<String, Set<SymbolicLinkSubDirConfiguration>>> techs : hm.entrySet()) {
			Map<String, Set<SymbolicLinkSubDirConfiguration>> nodes = techs.getValue();
			int sum = 0;
			for(Entry<String, Set<SymbolicLinkSubDirConfiguration>> syms : nodes.entrySet()) {
				sum += syms.getValue().size();
			}
			rowSpan.put(techs.getKey(), sum);
		}
		log.debug("rowSpans: "+rowSpan);
		ctx.put("rowSpans", rowSpan);
	}

	private void divideNeType(List<SymbolicLinkSubDirConfiguration> eniqXmlList, Map<String, Set<String>> technologyMapping) {
		if(eniqXmlList.isEmpty()) {
			log.info("No elements present.");
			return;
		}
		log.debug("technologyMapping: "+technologyMapping);
		eniqXmlList.stream().forEach(sym -> {
			String neType = sym.getName();
			String node = "";
			String technology = "";
			String dataType = "";
			if (neType.startsWith(TOPO)) {
				dataType = TOPO;
				String techNode = neType.substring(TOPO.length() + 1);
				Optional<String> techPresent = technologyMapping.keySet().stream().filter(techNode::startsWith)
						.findFirst();
				if (techPresent.isPresent()) {
					technology = techPresent.get();
					node = techNode.substring(technology.length() + 1);
				}
			} else {
				Optional<String> dataTypePresent = dataTypes.stream().filter(neType::startsWith).findFirst();
				if (dataTypePresent.isPresent()) {
					dataType = dataTypePresent.get();
					node = neType.substring(dataType.length() + 1);
				}
			}
			sym.setNode(node);
			sym.setDataType(dataType);
			sym.setTechnology(technology);
		});
		log.debug("After divide ENIQXml list"+eniqXmlList);
	}

	private void getAllDataTypes() {
		Path customPath = null;
		Path standardPath = null;
		Consumer<String> con = line -> {
			String[] tokens = line.split(":", 3);
			dataTypes.add(tokens[1]);
		};
		if (customNodeTypeDataTypeProperties.exists()) {
			customPath = Paths.get(customNodeTypeDataTypeProperties.getAbsolutePath());
			try {
				List<String> lines = Files.readAllLines(customPath);
				if (!lines.isEmpty()) {
					lines.stream().forEach(con);
				}
			} catch (IOException e) {
				log.warn("Exception while reading custom NodeType DataType mapping properties file: " + e);
			}
		}
		if (standardNodeTypeDataTypeProperties.exists()) {
			standardPath = Paths.get(standardNodeTypeDataTypeProperties.getAbsolutePath());
			try {
				List<String> oldLines = Files.readAllLines(standardPath);
				if (!oldLines.isEmpty()) {
					oldLines.stream().forEach(con);
				}
			} catch (IOException e) {
				log.warn("Exception while reading custom NodeType DataType mapping properties file: " + e);
			}
		}
	}
	
	public void restartFLS(Context ctx) {
		log.info("Restarting FLS");
		boolean result = true;
		if (flsRestartFile.exists()) {
			String flsRestartCommand = "bash /eniq/sw/bin/fls restart";
			try {
				RemoteExecutor.executeComandSshKey(APPLICATION_USER, HOST_ADD, flsRestartCommand);
			} catch (JSchException | IOException e) {
				log.warn("Error while restarting FLS " + e);
				result= false;
			}
		} else {
			log.warn("FLS script not found");
			result = false;
		}
		ctx.put(FLS_STARTED, result);
	}
	
	public void deleteFromNodeTechnologyMapping(String nodeType, String technology) {
		List<String> lines = new ArrayList<>();
		try {
			if (customNodeTechnologyProperties.exists()) {
				Path path = Paths.get(customNodeTechnologyProperties.getAbsolutePath());
				lines = Files.readAllLines(path);
				Optional<String> ifTechPresent = lines.stream().filter(line -> {
					String tech = line.substring(0, line.indexOf("-"));
					return tech.equals(technology);
				}).findAny();
				if (ifTechPresent.isPresent()) {
					String nodeTypes = ifTechPresent.get();
					String[] value = nodeTypes.substring(nodeTypes.indexOf("-") + 1, nodeTypes.length()).split(",");
					Predicate<String> p = t -> !t.equals(nodeType);
					List<String> value1 = Arrays.stream(value).filter(p).collect(Collectors.toList());
					if (value1.size() >= 1) {
						String str = String.join(",", value1);
						String finalstr = technology + "-" + str;
						int indexToAppendTheNewNodeType = lines.indexOf(nodeTypes);
						lines.set(indexToAppendTheNewNodeType, finalstr);
					} else if (value1.size() == 0) {
						lines.remove(nodeTypes);
					}
					AddCustomNodes.writeToCustomFiles(path, lines);
				} else {
					log.warn("Exception while reading the custom NodeTechnologyProperties");
				}
			}
		} catch (IOException e) {
			log.warn("Exception while reading the custom NodeTechnologyProperties: " + e);
		}
		log.debug("After modifying lines of Node tech properties:" + lines);
	}

	public void deleteFromNodeTypeDataTypeFile(String nodeType, String dataType, String techn) {
		log.debug("Inside deleteFromNodeTypeDataTypeFile with nodeType :" + nodeType + "dataType :" + dataType
				+ "technology :" + techn);
		if (customNodeTypeDataTypeProperties.exists()) {
			Path path = Paths.get(customNodeTypeDataTypeProperties.getAbsolutePath());
			List<String> lines = new ArrayList<>();
			try {
				lines = Files.readAllLines(path);
				log.debug("Before modifying lines of Node Type Data Type: " + lines);
				Optional<String> datatypes = lines.stream().filter(line -> {
					String[] tokens = line.split(":", 3);
					return tokens[0].equals(nodeType) && tokens[1].equals(dataType);
				}).findFirst();
				if (datatypes.isPresent()) {
					String line = datatypes.get();
					String[] lines2 = line.split(":", 3);
					Predicate<String> p = t -> !t.equals(techn);
					List<String> value = Arrays.stream(lines2[2].split(",")).filter(p).collect(Collectors.toList());
					if (value.size() >= 1) {
						String str = String.join(",", value);
						String finalstr = nodeType + ":" + dataType + ":" + str;
						int indexToAppendTheNewNodeType = lines.indexOf(line);
						lines.set(indexToAppendTheNewNodeType, finalstr);
					} else if (value.size() == 0) {
						lines.remove(line);
					}
					AddCustomNodes.writeToCustomFiles(path, lines);
				} else {
					log.warn("Exception while reading the data from file");
				}
			} catch (IOException e) {
				log.warn("Exception while reading the custom NodeTechnologyProperties: " + e);
			}
		}
	}
}
