package com.distocraft.dc5000.etl.gui.enminterworking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WriteAndReadDataFromEniqXml {

	private static final Log log = LogFactory.getLog(WriteAndReadDataFromEniqXml.class);
	private String nodeType;
	private String pmDataType;
	private String technology;
	private String topoNodeTypeDir;
	private String topoSubDir;
	private String topologyDataType;
	private String pmNodeTypeDir;
	private int numberOfPMSubdirs;
	private String fileFilter;
	
	protected static final String STANDARD_ENIQ_CONFIG_DIR = "/eniq/sw/conf";
	private static final File customEniqConfigurationXml = new File(AddCustomNodes.CUSTOM_ENIQ_CONFIG_DIR + "/custom_eniq.xml");
	private final File standardEniqConfigurationXml = new File(STANDARD_ENIQ_CONFIG_DIR + "/eniq.xml");

	private static final int MAX_NUM_LINKS = 2000;

	public WriteAndReadDataFromEniqXml() {
		super();
	}

	public WriteAndReadDataFromEniqXml(String nodeType, String pmDataType, String technology, String topoNodeTypeDir,
			String topoSubDir, String topoDataType, String pmNodeTypeDir, int numberOfPMSubdirs, String fileFilter) {
		super();
		this.nodeType = nodeType;
		this.pmDataType = pmDataType;
		this.technology = technology;
		this.topoNodeTypeDir = topoNodeTypeDir;
		this.topoSubDir = topoSubDir;
		this.pmNodeTypeDir = pmNodeTypeDir;
		this.numberOfPMSubdirs = numberOfPMSubdirs;
		this.fileFilter = fileFilter;
		this.topologyDataType = topoDataType;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getDataType() {
		return pmDataType;
	}

	public void setDataType(String pmDataType) {
		this.pmDataType = pmDataType;
	}

	public String getTechnology() {
		return technology;
	}

	public void setTechnology(String technology) {
		this.technology = technology;
	}

	public String getTopoNodeTypeDir() {
		return topoNodeTypeDir;
	}

	public void setTopoNodeTypeDir(String topoNodeTypeDir) {
		this.topoNodeTypeDir = topoNodeTypeDir;
	}

	public String getTopoSubDir() {
		return topoSubDir;
	}

	public void setTopoSubDir(String topoSubDir) {
		this.topoSubDir = topoSubDir;
	}

	public void setTopologyDataType(String topologyDataType) {
		this.topologyDataType = topologyDataType;
	}

	public String getPmNodeTypeDir() {
		return pmNodeTypeDir;
	}

	public void setPmNodeTypeDir(String pmNodeTypeDir) {
		this.pmNodeTypeDir = pmNodeTypeDir;
	}

	public int getNumberOfPMSubdirs() {
		return numberOfPMSubdirs;
	}

	public void setNumberOfPMSubdirs(int numberOfPMSubdirs) {
		this.numberOfPMSubdirs = numberOfPMSubdirs;
	}

	public String getTopologyDataType() {
		return topologyDataType;
	}

	public String getFileFilter() {
		return fileFilter;
	}

	public void setFileFilter(String fileFilter) {
		this.fileFilter = fileFilter;
	}
	
	public static ConcurrentMap<String, Map<String, SymbolicLinkSubDirConfiguration>> readEniqXmlFile(File file) {
		DirectoryConfigurationFileParser eniqXml = new DirectoryConfigurationFileParser(log,file);
		return eniqXml.getSymbolicLinkSubDirConfigurations();
	}
	 /*
	  * Writes data in custom eniq.xml file Returns boolean value
	  * True: If it added the data passed, in the hashmap successfully and wrote the data in custom eniq.xml
	  * False: If it did not
	  * */
	public boolean writeDataIntoXml() {
		ConcurrentMap<String, Map<String, SymbolicLinkSubDirConfiguration>> hm = null;
		boolean ifAlreadyPresent = true;
		if (customEniqConfigurationXml.exists() && customEniqConfigurationXml.length() > 0) {
			DirectoryConfigurationFileParser customEniqXml = new DirectoryConfigurationFileParser(log,
					customEniqConfigurationXml);
			boolean checkForSuccessfulParsing = customEniqXml.isInitSuccessful();
			if (!checkForSuccessfulParsing) {
				log.warn("Error while parsing the custom_eniq.xml file");
			} else {
				hm = customEniqXml.getSymbolicLinkSubDirConfigurations();
				log.debug("After parsing custom eniq.xml, hashmap value" + hm);
				ifAlreadyPresent = setPMAndTopoValuesInHashMap(hm);
			}
		} else if (customEniqConfigurationXml.exists() && customEniqConfigurationXml.length() == 0) {
			hm = new ConcurrentHashMap<>();
			ifAlreadyPresent = setPMAndTopoValuesInHashMap(hm);
		} else {
			log.warn("Custom_eniq.xml does not exist");
		}
		if (hm != null && hm.size() > 0 && !ifAlreadyPresent) {
			boolean result = writeDataIntoCustomXml(hm);
			if (result)
				return AddCustomNodes.addGranularityValueInRepdb(technology, nodeType);
			else
				log.warn("Unable to write the data Entered in custom eniq.xml file. Data Entered: [NodeType: "
						+ nodeType + ", PM DataType: " + pmDataType + ", technology: " + technology + ", topoNodeTypeDir: "
						+ topoNodeTypeDir + ", topoSubDir: " + topoSubDir + ", pmNodeTypeDir: " + pmNodeTypeDir + "]");
		}
		return false;
	}

	/*
	 * Calls methods to set pm and topology data in hashmap passed Returns boolean value
	 * False: If it added the data passed, in the hashmap successfully - expected
	 * True: If it did not
	 */
	private boolean setPMAndTopoValuesInHashMap(
			ConcurrentMap<String, Map<String, SymbolicLinkSubDirConfiguration>> hm) {
		setTopologyValuesInHashMap(hm);
		boolean pmResult = setPmValuesInHashMap(hm);
		return !pmResult;
	}

	/*
	 * Sets topology data in hashmap passed Returns boolean value True: If it does
	 * not match with existing data in custom eniq.xml False: If it matches
	 */
	private boolean setTopologyValuesInHashMap(
			ConcurrentMap<String, Map<String, SymbolicLinkSubDirConfiguration>> hm) {
		String topoNeType = getNeType(nodeType, topologyDataType);
		Map<String, SymbolicLinkSubDirConfiguration> neTypeValues = hm.get(topoNeType);
		if (neTypeValues == null) {
			neTypeValues = new ConcurrentHashMap<>();
			SymbolicLinkSubDirConfiguration sym = new SymbolicLinkSubDirConfiguration();
			setSymbolicLinkDirConfiguration(sym, topoNeType, topoNodeTypeDir, topoSubDir);
			neTypeValues.put(topoNodeTypeDir, sym);
			hm.put(topoNeType, neTypeValues);
		} else {
			SymbolicLinkSubDirConfiguration nodeTypeDirValue = neTypeValues.get(topoNodeTypeDir);
			if (nodeTypeDirValue == null) {
				SymbolicLinkSubDirConfiguration sym = new SymbolicLinkSubDirConfiguration();
				setSymbolicLinkDirConfiguration(sym, topoNeType, topoNodeTypeDir, topoSubDir);
				neTypeValues.put(topoNodeTypeDir, sym);
			} else {
				log.info("Same topology nodeTypeDirValue already present in custom_eniq.xml file: nodeTypeDirValue="
						+ nodeTypeDirValue);
			}
		}
		return true;
	}

	/*
	 * Sets pm data in hashmap passed Returns boolean value True: If it does not
	 * match with existing data in custom eniq.xml False: If it matches
	 */
	private boolean setPmValuesInHashMap(ConcurrentMap<String, Map<String, SymbolicLinkSubDirConfiguration>> hm) {
		String pmNeType = getNeType(nodeType, pmDataType);
		Map<String, SymbolicLinkSubDirConfiguration> neTypeValues = hm.get(pmNeType);
		if (neTypeValues == null) {
			neTypeValues = new ConcurrentHashMap<>();
			SymbolicLinkSubDirConfiguration sym = new SymbolicLinkSubDirConfiguration();
			setSymbolicLinkDirConfiguration(sym, pmNeType, pmNodeTypeDir, numberOfPMSubdirs, fileFilter);
			neTypeValues.put(pmNodeTypeDir, sym);
			hm.put(pmNeType, neTypeValues);
		} else {
			SymbolicLinkSubDirConfiguration nodeTypeDirValue = neTypeValues.get(pmNodeTypeDir);
			if (nodeTypeDirValue == null) {
				SymbolicLinkSubDirConfiguration sym = new SymbolicLinkSubDirConfiguration();
				setSymbolicLinkDirConfiguration(sym, pmNeType, pmNodeTypeDir, numberOfPMSubdirs, fileFilter);
				neTypeValues.put(pmNodeTypeDir, sym);
			} else {
				log.warn("Same PM nodeTypeDirValue already present in custom_eniq.xml file: nodeTypeDirValue="
						+ nodeTypeDirValue);
				return false;
			}
		}
		return true;
	}

	// Returns the netype tag value for Pm/Topo data
	public static String getNeType(String nodeType, String dataType) {
		StringBuilder key = new StringBuilder();
		key.append(dataType);
		key.append("_");
		key.append(nodeType);
		return key.toString();
	}

	// Set Topo data in SymbolicLinkSubDirConfiguration object
	private void setSymbolicLinkDirConfiguration(SymbolicLinkSubDirConfiguration sym, String topoNeType,
			String topoNodeTypeDir, String topoSubDir) {
		sym.setFileFilter("");
		sym.setMaxNumLinks(MAX_NUM_LINKS);
		sym.setName(topoNeType);
		sym.setNodeTypeDir(topoNodeTypeDir);
		if (topoSubDir == null)
			sym.addSubDir("");
		else
			sym.addSubDir(topoSubDir);
	}

	// Set Pm data in SymbolicLinkSubDirConfiguration object
	private void setSymbolicLinkDirConfiguration(SymbolicLinkSubDirConfiguration sym, String pmNeType,
			String pmNodeTypeDir, int numberOfPMSubdirs, String fileFilter) {
		if (fileFilter == null)
			sym.setFileFilter("");
		else
			sym.setFileFilter(fileFilter);
		sym.setMaxNumLinks(MAX_NUM_LINKS);
		sym.setName(pmNeType);
		sym.setNodeTypeDir(pmNodeTypeDir);
		for (int i = 1; i <= numberOfPMSubdirs; i++) {
			sym.addSubDir("dir" + i);
		}
	}

	/*
	 * Write data into custom eniq.xml file Returns boolean value True: If it
	 * successfully added the data in custom eniq.xml file. False: If it did not.
	 */
	protected boolean writeDataIntoCustomXml(ConcurrentMap<String, Map<String, SymbolicLinkSubDirConfiguration>> hm) {
		log.debug("Map Value to write in the custom eniq.xml file: Map=" + hm);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		boolean ifSuccessfullyWritten = true;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			Element rootElement = doc.createElement("InterfaceData");
			doc.appendChild(rootElement);
			Iterator<Entry<String, Map<String, SymbolicLinkSubDirConfiguration>>> iterator = hm.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Map<String, SymbolicLinkSubDirConfiguration>> entry = iterator.next();
				Map<String, SymbolicLinkSubDirConfiguration> value = entry.getValue();
				Iterator<String> nodeTypeDirs = value.keySet().iterator();
				while (nodeTypeDirs.hasNext()) {
					String nodeTypeDir = nodeTypeDirs.next();
					SymbolicLinkSubDirConfiguration symLink = value.get(nodeTypeDir);
					String filefilter = symLink.getFileFilter();
					String neType = symLink.getName();
					List<String> subDirs = symLink.getSubDirs();
					if (filefilter != null && filefilter.length() > 0) {
						writePMDataIntoXml(doc, rootElement, neType, nodeTypeDir, subDirs, filefilter);
					} else {
						writeDataIntoXml(doc, rootElement, neType, subDirs, nodeTypeDir);
					}
				}
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(customEniqConfigurationXml);
			transformer.transform(source, result);
			format(customEniqConfigurationXml);
		} catch (ParserConfigurationException e) {
			log.warn("Exception while creating a new document builder:" + e);
			ifSuccessfullyWritten = false;
		} catch (TransformerConfigurationException e1) {
			log.warn("Exception while creating new transformer object: " + e1);
			ifSuccessfullyWritten = false;
		} catch (TransformerException e2) {
			log.warn("Exception while transforming data into xml: " + e2);
			ifSuccessfullyWritten = false;
		}
		return ifSuccessfullyWritten;
	}

	// Write pm data into custom eniq.xml file including fileFilter tag
	private void writePMDataIntoXml(Document doc, Element rootElement, String neTypeValue, String nodeTypeDirValue,
			List<String> subDirValues, String fileFilterValue) {
		// Interface Data
		log.debug("Called PmdataintoXml method with fileFilter");

		Element interFace = doc.createElement("Interface");
		rootElement.appendChild(interFace);
		Element neType = doc.createElement("neType");
		neType.appendChild(doc.createTextNode(neTypeValue));
		interFace.appendChild(neType);
		Element filefilter = doc.createElement("fileFilter");
		filefilter.appendChild(doc.createTextNode(fileFilterValue));
		interFace.appendChild(filefilter);
		Element maxNumLink = doc.createElement("maxNumLinks");
		maxNumLink.appendChild(doc.createTextNode(MAX_NUM_LINKS + ""));
		interFace.appendChild(maxNumLink);
		Element nodeTypeDir = doc.createElement("nodeTypeDir");
		nodeTypeDir.appendChild(doc.createTextNode(nodeTypeDirValue));
		interFace.appendChild(nodeTypeDir);
		for (String subDirValue : subDirValues) {
			Element subDir = doc.createElement("subdir");
			subDir.appendChild(doc.createTextNode(subDirValue));
			interFace.appendChild(subDir);
		}
	}

	// Write topology data into custom eniq.xml file
	private void writeDataIntoXml(Document doc, Element rootElement, String neTypeValue, List<String> subDirValues,
			String nodeTypeDirValue) {
		log.debug("Called writeDataIntoXml method");
		// Interface Data
		Element interFace = doc.createElement("Interface");
		rootElement.appendChild(interFace);
		Element neType = doc.createElement("neType");
		neType.appendChild(doc.createTextNode(neTypeValue));
		interFace.appendChild(neType);
		Element maxNumLink = doc.createElement("maxNumLinks");
		maxNumLink.appendChild(doc.createTextNode(MAX_NUM_LINKS + ""));
		interFace.appendChild(maxNumLink);
		Element nodeTypeDir = doc.createElement("nodeTypeDir");
		nodeTypeDir.appendChild(doc.createTextNode(nodeTypeDirValue));
		interFace.appendChild(nodeTypeDir);
		for (String subDirValue : subDirValues) {
			Element subDir = doc.createElement("subdir");
			subDir.appendChild(doc.createTextNode(subDirValue));
			interFace.appendChild(subDir);
		}
	}

	// To format the custom eniq.xml file
	public void format(File customeniqconfigurationxml) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try (FileInputStream fStream = new FileInputStream(customeniqconfigurationxml);
				InputStreamReader iReader = new InputStreamReader(fStream);) {
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(iReader));
			// Gets a new transformer instance
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			// Sets XML formatting
			xformer.setOutputProperty(OutputKeys.METHOD, "xml");
			// Sets indent
			xformer.setOutputProperty(OutputKeys.INDENT, "yes");
			Source source = new DOMSource(document);
			Result result = new StreamResult(customeniqconfigurationxml);
			xformer.transform(source, result);
		} catch (ParserConfigurationException e) {
			log.warn("Exception while creating new document builder object: " + e);
		} catch (FileNotFoundException e1) {
			log.warn("The file to be formatted does not exist. File=" + customeniqconfigurationxml + ": " + e1);
		} catch (IOException e2) {
			log.warn("Exception while closing streams: " + e2);
		} catch (SAXException e3) {
			log.warn("Exception while parsing the custom eniq.xml file: " + e3);
		} catch (TransformerConfigurationException e4) {
			log.warn("Exception while creating a transformer factory object: " + e4);
		} catch (TransformerFactoryConfigurationError e5) {
			log.warn("Exception while getting a new instance of transformer factory object: " + e5);
		} catch (TransformerException e6) {
			log.warn("Exception while transforming the custom eniq.xml file: " + e6);
		} catch (Exception e7) {
			log.warn("Exception while formating the custom eniq.xml file: " + e7);
		}
	}
}
