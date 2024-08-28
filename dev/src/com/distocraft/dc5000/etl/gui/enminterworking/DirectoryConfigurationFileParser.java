package com.distocraft.dc5000.etl.gui.enminterworking;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class DirectoryConfigurationFileParser extends DefaultHandler {

	/**
	 * An object which represents all details from the eniq.xml file for a single
	 * neType
	 */
	private SymbolicLinkSubDirConfiguration symbolicLinkSubDirConfiguration = null;

	/**
	 * This entire map represents the whole XML file. A map where NeType is the key
	 * and SymbolLinkSubDirConfiguration for that NeType/configurationName as the
	 * value for easy and fast retrieval
	 */
	private ConcurrentHashMap<String, Map<String, SymbolicLinkSubDirConfiguration>> symbolicLinkSubDirConfigurations = new ConcurrentHashMap<>();

	/**
	 * To hold the current XML tag @SupportedTags object
	 */
	private SupportedTags currentTag = SupportedTags.noValue;

	/**
	 * Value of the current tag
	 */
	private String currentTagValue = "";

	private boolean isInitSuccessful;

	Log log;

	/**
	 * Supported XML tags by this parser
	 */
	private enum SupportedTags {
		// EQEV-60726
		InterfaceData, Interface, neType, maxNumLinks, nodeTypeDir, subdir, noValue, fileFilter;//NOSONAR

		public static SupportedTags getTag(final String str) {
			try {
				return valueOf(str);
			} catch (final IllegalArgumentException e) {
				return noValue;
			}
		}
	}

	/**
	 * Default constructor which parses the eniq.xml file
	 * 
	 * @param file
	 */
	protected DirectoryConfigurationFileParser(Log log2, File file) {
		try {
			this.log = log2;
			SAXParserFactory.newInstance().newSAXParser().parse(file, this);
			isInitSuccessful = true;
			log2.debug("Eniq.xml successfully parsed, result : " + symbolicLinkSubDirConfigurations);
		} catch (final FileNotFoundException e) {
			handleException(e,
					".ENIQ-M might not be installed on the server, ENIQ-S will not create the symbolic links");
		} catch (final Exception e) {
			handleException(e, e.getMessage());
		}
	}

	/**
	 * Return the map of @SymbolicLinkSubDirConfiguration stored per node type which
	 * is populated from the eniq.xml file
	 * 
	 * @return - the symbolicLinkSubDirConfigurations
	 */
	public ConcurrentMap<String, Map<String, SymbolicLinkSubDirConfiguration>> getSymbolicLinkSubDirConfigurations() {
		return symbolicLinkSubDirConfigurations;
	}

	/**
	 * Called when starting to parse an element. If the element is an interface,
	 * i.e. the main object being parsed, then create a new instance and add it to
	 * the list.
	 * 
	 * @param - qName String the XML tag for this element
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(final String uri, final String localName, final String qName,
			final Attributes inAttributes) {
		currentTagValue = "";
		currentTag = SupportedTags.getTag(qName);
		if (currentTag.equals(SupportedTags.Interface)) {
			// data for new NeType
			symbolicLinkSubDirConfiguration = new SymbolicLinkSubDirConfiguration();
		}
	}

	/**
	 * Called when finished parsing an element, i.e. the value of the element was
	 * read by the characters() method. Set the appropriate attribute in the holder
	 * to the value read by the characters() method.
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(final String namespaceURL, final String lName, final String qName) {
		try {
			log.debug("Current tag value: "+currentTagValue + "curent tag: "+currentTag);
			switch (currentTag) {
			case neType:
				symbolicLinkSubDirConfiguration.setName(currentTagValue);
				break;
			case maxNumLinks:
				symbolicLinkSubDirConfiguration.setMaxNumLinks(Integer.valueOf(currentTagValue));
				break;
			case fileFilter:
				symbolicLinkSubDirConfiguration.setFileFilter(currentTagValue);
				break;
			case nodeTypeDir:
				symbolicLinkSubDirConfiguration.setNodeTypeDir(currentTagValue);
				Map<String, SymbolicLinkSubDirConfiguration> ntdList = symbolicLinkSubDirConfigurations
						.get(symbolicLinkSubDirConfiguration.getName());
				if (ntdList == null) {
					ntdList = new ConcurrentHashMap<>();
				}
				ntdList.put(currentTagValue, symbolicLinkSubDirConfiguration);
				symbolicLinkSubDirConfigurations.put(symbolicLinkSubDirConfiguration.getName(), ntdList);
				break;

			case subdir:
				symbolicLinkSubDirConfiguration.addSubDir(currentTagValue);
				break;
			default:
				break;
			}
			currentTag = SupportedTags.noValue;
		} catch (Exception e) {
			log.warn("Exception at endElement " + e.getMessage());
		}
	}

	/**
	 * Read the value from the XML file. This value is then set in the holder object
	 * by the endElement() method.
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(final char[] buf, final int offset, final int len) {
		final StringBuilder charBuffer = new StringBuilder(len);
		for (int i = offset; i < (offset + len); i++) {
			// If no control char
			if ((buf[i] != '\\') && (buf[i] != '\n') && (buf[i] != '\r') && (buf[i] != '\t')) {
				charBuffer.append(buf[i]);
			}
		}
		currentTagValue += charBuffer.toString();
	}

	/**
	 * Finished parsing the XML.
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	@Override
	public void endDocument() {
		symbolicLinkSubDirConfiguration = null;
	}

	/**
	 * Handles the exceptions in this class
	 * 
	 * @param e       - @Throwable object
	 * @param type    - "error" in case of major error
	 * @param message - error message to be logged
	 */
	private void handleException(final Throwable e, final String message) {
		log.warn("DirectoryConfigurationFileParser  : " + message, e);
		symbolicLinkSubDirConfigurations.clear();
		symbolicLinkSubDirConfiguration = null;
		currentTagValue = "";
		currentTag = SupportedTags.noValue;
		isInitSuccessful = false;
	}

	public boolean isInitSuccessful() {
		return isInitSuccessful;
	}
}
