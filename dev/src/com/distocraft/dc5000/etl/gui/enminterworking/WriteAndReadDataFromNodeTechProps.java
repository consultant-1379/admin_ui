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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WriteAndReadDataFromNodeTechProps {
	
	private String nodeType;
	private String technology;

	private static final Log log = LogFactory.getLog(WriteAndReadDataFromNodeTechProps.class);
	
	private final File customNodeTechnologyProperties = new File(AddCustomNodes.CUSTOM_ENIQ_CONFIG_DIR + "/custom_NodeTechnologyMapping.properties");
	
	public WriteAndReadDataFromNodeTechProps(String nodeType, String technology) {
		super();
		this.nodeType = nodeType;
		this.technology = technology;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getTechnology() {
		return technology;
	}

	public void setTechnology(String technology) {
		this.technology = technology;
	}
	
	/*
	 * Reads the NodeTechnology Mapping properties and returns a Map:
	 * Map: <Technology, nodeTypes Separated by commas in a string>
	 * Example: <'NR', 'RadioNode,5GRadioNode,vTIF,vPP'>
	 * */
	public static Map<String, String> getNodeTechnologyMapping(File file) {

		Map<String, String> nodeTechnologyMap = new HashMap<>();
		try (FileReader fReader = new FileReader(file); BufferedReader reader = new BufferedReader(fReader);) {
			String line = reader.readLine();
			while (line != null) {
				String key = line.substring(0, line.indexOf("-"));
				String value = line.substring(line.indexOf("-") + 1, line.length());
				nodeTechnologyMap.put(key, value);
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			log.warn("File not found: File=" + file + " " + e);
		} catch (IOException e1) {
			log.warn("Unable to read file: " + e1);
		}
		return nodeTechnologyMap;
	}
	
	/*
	 * Returns boolean value
	 * True: If it successfully wrote the custom nodeType and technology in the
	 * custom NodeTech properties file - expected 
	 * False: If it didn't
	 */
	public boolean writeDataIntoNodeTechProperties() {
		boolean result = true;
		if(customNodeTechnologyProperties.exists()) {
			Path path = Paths.get(customNodeTechnologyProperties.getAbsolutePath());
			List<String> lines = setListForNodeTechProperties(path);
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

	/* 
	 * Returns a list of lines to be written in the custom NodeTech properties file
	 * by adding the new custom NodeType and Technology if not present 
	 */
	private List<String> setListForNodeTechProperties(Path path){
		List<String> lines = new ArrayList<>();
		try {
			lines = Files.readAllLines(path);
			log.debug("Before modifying lines of Node tech properties: "+lines);
			Optional<String> ifTechPresent = lines.stream().filter( line -> {
				String tech = line.substring(0, line.indexOf("-"));
				return tech.equals(technology);
			}).findAny();
			if(ifTechPresent.isPresent()) {
				String nodeTypes = ifTechPresent.get();
				String[] value = nodeTypes.substring(nodeTypes.indexOf("-") + 1, nodeTypes.length()).split(",");
				Predicate<String> p = t -> t.equals(nodeType);
				if(Arrays.stream(value).anyMatch(p)) {
					log.info("Technology: "+technology+" ,Node Type: "+nodeType+" is already present in custom NodeTechnologyMapping file");
				}else {
					int indexToAppendTheNewNodeType =lines.indexOf(nodeTypes);
					String newNodeTypes = nodeTypes+","+nodeType;
					lines.set(indexToAppendTheNewNodeType, newNodeTypes);
				}
			}else{
				lines.add(technology+"-"+nodeType);
			}
		} catch (IOException e) {
			log.warn("Exception while reading the custom NodeTechnologyProperties: "+e);
		}
		log.debug("After modifying lines of Node tech properties:"+lines);
		return lines;
	}
}
