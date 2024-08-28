package com.distocraft.dc5000.etl.gui.enminterworking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WriteAndReadDataFromNodeTypeDataTypeFile {

	private static final Log log = LogFactory.getLog(WriteAndReadDataFromNodeTypeDataTypeFile.class);

	private final File customNodeTypeDataTypeProperties = new File(
			AddCustomNodes.CUSTOM_ENIQ_CONFIG_DIR + "/custom_NodeTypeDataTypeMapping.properties");

	private String nodeType;
	private String dataType;
	private String technology;

	public WriteAndReadDataFromNodeTypeDataTypeFile(String nodeType, String dataType, String technology) {
		super();
		this.nodeType = nodeType;
		this.dataType = dataType;
		this.technology = technology;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getTechnology() {
		return technology;
	}

	public void setTechnology(String technology) {
		this.technology = technology;
	}
	
	/*
	 * Reads the NodeTypeDataTypeMapping properties and returns the in a Map
	 * Map Format: Map<NodeType, Map<TechnologyListSeparatedByComma, Set<Data Types>>>
	 * Example: 
	 * 1) Map<'RadioNode', Map<'NR,LTE,WCDMA,GRAN', Set<'PM_STATISTICAL'>>>
	 * 2) Map<'RadioNode', Map<'Events', Set<'PM_EBSN_DU, PM_EBSN_CUCP, PM_EBSN_CUUP'>>>
	 * */
	
	public static Map<String, Map<String, Set<String>>> readNodeTypeDataTypeFile(File nodeDataTypeMapping) {
		String line;
		Map<String, Map<String, Set<String>>> builderMap = new HashMap<>();
		try (FileReader fReader = new FileReader(nodeDataTypeMapping);
				BufferedReader br = new BufferedReader(fReader)) {
			while ((line = br.readLine()) != null) {
				if (line.contains(":")) {
					String[] tokens = line.split(":", 3);
					String nodeType = tokens[0];
					String dataType = tokens[1];
					String technology = tokens[2];
					Map<String, Set<String>> techDatatypeMapping = builderMap.get(nodeType);
					if (techDatatypeMapping == null) {
						techDatatypeMapping = new HashMap<>();
						Set<String> datatypes = new HashSet<>();
						datatypes.add(dataType);
						techDatatypeMapping.put(technology, datatypes);
					}else {
						Set<String> datatypes = techDatatypeMapping.get(technology);
						if(datatypes == null) {
							datatypes = new HashSet<>();
						}
						datatypes.add(dataType);
						techDatatypeMapping.put(technology, datatypes);
					}
					builderMap.put(nodeType, techDatatypeMapping);
				}
			}
		} catch (FileNotFoundException e) {
			log.warn("File not found while reading file="+nodeDataTypeMapping+": "+e);
		} catch (IOException e) {
			log.warn("Exception while reading file="+nodeDataTypeMapping+": "+e);
		}
		return builderMap;
	}
	
	public boolean writeDataInNodeTypeDataTypeFile() {
		boolean result = true;
		if(customNodeTypeDataTypeProperties.exists()) {
			Path path = Paths.get(customNodeTypeDataTypeProperties.getAbsolutePath());
			List<String> lines = setListForNodeTypeDataTypeProperties(path);
			if(!lines.isEmpty() && lines.stream().anyMatch( line -> line.contains(nodeType))) {
				result = AddCustomNodes.writeToCustomFiles(path, lines);
			}else {
				result = false;
			}
		}else {
			result = false;
		}
		return result;
	}

	private List<String> setListForNodeTypeDataTypeProperties(Path path) {
		List<String> lines = new ArrayList<>();
		try {
			lines = Files.readAllLines(path);
			log.debug("Before modifying lines of Node Type Data Type: "+lines);
			List<String> allNodeTypes = lines.stream().filter( line -> {
				String[] tokens = line.split(":",3);
				return tokens[0].equals(nodeType);
				}).collect(Collectors.toList());
			if(!allNodeTypes.isEmpty()) {
				Optional<String> datatypes = allNodeTypes.stream().filter( line -> {
					String[] tokens = line.split(":",3);
					return tokens[1].equals(dataType);
				}).findFirst();
				if(datatypes.isPresent()) {
					String line = datatypes.get();
					String[] lines2 = line.split(":", 3);
					Predicate<String> p = t -> t.equals(technology);
					if(Arrays.stream(lines2[2].split(",")).anyMatch(p)) {
						log.info("NodeType:"+nodeType+", DataType:"+dataType+" and technology:"+technology+" already present in custom NodeTypeDataTypeMapping properties file");
					}else {
						String newLine = line+","+technology;
						int indexOfOldLine = allNodeTypes.indexOf(line);
						lines.set(indexOfOldLine, newLine);
					}
				}
				else {
					String newLine = nodeType+":"+dataType+":"+technology;
					lines.add(newLine);
				}
			} else {
				String newLine = nodeType+":"+dataType+":"+technology;
				lines.add(newLine);
			}
		} catch (IOException e) {
			log.warn("Exception while reading the custom NodeTechnologyProperties: "+e);
		}
		log.debug("After modifying lines of Node Type Data Type: "+lines);
		return lines;
	}
}
