package com.distocraft.dc5000.etl.gui.systemmonitor;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.util.NodeVersionComparator;
import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.distocraft.dc5000.etl.rock.Meta_databasesFactory;
import com.ericsson.eniq.repository.ETLCServerProperties;

public class FeatureInformation {

	private final Log log = LogFactory.getLog(this.getClass());

	protected final static String fileDelimiter = "::";

	private File file;

	private FileReader stream;

	private BufferedReader buf;

	private Connection dwhdbConnection;

	private Connection repdbConnection;

	private String upgradePath;

	private String cxc;

	private static final String NOTAVAILABLE = "NA";

	private boolean installFlag = false;
	
	private ETLCServerProperties etlcserverprops;
	
	private String bufferErrorMsg = "Error in closing BufferedReader";

	private String filereaderErrorMsg = "Error in closing FileReader";
    
	private String closeStmtErrorMsg = "Could not close statement ";
	/**
	 * Gets called when feature needs update/add additional features
	 */
	public FeatureInformation(Connection dwhConn, Connection repConn, boolean b) {
		this.dwhdbConnection = dwhConn;
		this.repdbConnection = repConn;
		this.installFlag = b;
	}

	
	public FeatureInformation() {

	}


	/**
	 * Returns a datastructure that contains the mapping between featureName and
	 * nodeType
	 * 
	 * @return Map<String, ArrayList<String>>
	 * @throws FileNotFoundException
	 */
	protected Map<String, Map<String, Map<String, String>>> getFeatureInformation(final String featureUpgradePath,
			Map<String, String> featMap) throws FileNotFoundException {

		final Map<String, Map<String, Map<String, String>>> featureInfo = new TreeMap<String, Map<String, Map<String, String>>>();
		this.upgradePath = featureUpgradePath+"/eniq_techpacks";
		validatePath();
		
		//Check for the proper feature description
		File file = new File(this.upgradePath + File.separator + "install_features");
		featMap = checkFeatureDescription(featMap.keySet(), file);
		
		// Iterate the key set
		for (final String cxc : featMap.keySet()) {
			this.cxc = cxc;
		
			File cxcFile = new File(this.upgradePath + File.separator + "." + this.cxc); //NOSONAR
			
			if (cxcFile.exists()) {
				//Verify the features before proceeding
				String strLine = "";
				try {
					FileReader stream = new FileReader(file);
					BufferedReader buf = new BufferedReader(stream);
					while ((strLine = buf.readLine()) != null) {
						if(strLine.contains(this.cxc)){
							final Map<String, Map<String, String>> nodeDetails = getNodeDetailsForFeature();
							featureInfo.put(featMap.get(cxc), nodeDetails);	
						}
					} 
				}catch (IOException e) {
						log.warn("IOException while reading feature names " + e.getMessage());
					}
					finally{
						if(buf != null){
							try {
								buf.close();
							} catch (IOException e) {
								log.warn(bufferErrorMsg + e.getMessage());
							}
						}
						if(stream != null){
							try {
								stream.close();
							} catch (IOException e) {
								log.warn(filereaderErrorMsg + e.getMessage());
							}
						}
					}
				}else {
					log.info("The license for " + this.cxc + " is installed in the server but the respective Feature package is not available in the MWS path." +
				" This Feature won't be available for upgrade.");
				}
			}
		return featureInfo;
	}
	
	/**
	 * Constructs new feature map by considering the Feature descriptions from the install_features files
	 * 
	 */
	private Map<String, String> checkFeatureDescription(Set<String> cxc, File installFeaturesFile) {
		Map<String,String> featureMap = new HashMap<String,String>();
		String strLine = "";
		try {
			FileReader stream = new FileReader(installFeaturesFile);
			BufferedReader buf = new BufferedReader(stream);
			while ((strLine = buf.readLine()) != null) {
			String fileCxc = strLine.substring(0,strLine.indexOf(":"));
			String fileCxcDescription = strLine.substring(strLine.indexOf(":")+2,strLine.lastIndexOf(":")-1);
			if(cxc.contains(fileCxc)){
				featureMap.put(fileCxc, fileCxcDescription);
				}
			} 
		}catch (IOException e) {
				log.warn("IOException while reading feature names " + e.getMessage());
		}catch (Exception e){
			log.warn("Exception while obtaining feature description " + e.getMessage());
		}
			finally{
				if(buf != null){
					try {
						buf.close();
					} catch (IOException e) {
						log.warn(bufferErrorMsg + e.getMessage());
					}
				}
				if(stream != null){
					try {
						stream.close();
					} catch (IOException e) {
						log.warn(filereaderErrorMsg + e.getMessage());
					}
				}
			}
		return featureMap;
	}


	/**
	 * Checks if the Upgrade/Install path is valid 
	 * 
	 * @throws FileNotFoundException
	 */
	private void validatePath() throws FileNotFoundException {
		 boolean isValidPath = false;
		File dir = new File(upgradePath);
		if (dir.exists() && dir.isDirectory()) {
			
			File[] listOfFiles = dir.listFiles();
			for (File file : listOfFiles) {
				if (file.isFile() & file.getName().startsWith(".CXC")) {
					isValidPath=true;
					break;
				}
			}if(!isValidPath){
				log.warn(".CXC files in MWS server are not available.Please check the path.");
				throw new FileNotFoundException();
			}
		}else{
			throw new FileNotFoundException();
		}
		
	}

	/**
	 * Returns a datastructure that contains the mapping between nodeType and
	 * and its versions
	 * 
	 * @return Map<String, Map<String,String>>
	 * @throws FileNotFoundException
	 */
	private Map<String, Map<String, String>> getNodeDetailsForFeature() {
		String topologyVersion = null;
		String techpackVersion = null;
		String mwsVersion = null;

		file = new File(upgradePath + File.separator + "." + cxc);
		final Map<String, Map<String, String>> nodeInfo = new HashMap<String, Map<String, String>>();
		try {
			stream = new FileReader(file);
			buf = new BufferedReader(stream);

			// Ignore the first line of the file
			buf.readLine();

			String line = "";

			while ((line = buf.readLine()) != null) {
				Map<String, String> nodeMap = new HashMap<String, String>();

				String[] nodeValue = line.split(fileDelimiter);
				String nodeType = nodeValue[1].trim();
				//columns to be populated in II page
				if(installFlag){
					topologyVersion = NOTAVAILABLE;
					techpackVersion = " ";
					if (nodeType.equals(NOTAVAILABLE)) {
						mwsVersion = " ";
					}else{
						mwsVersion = nodeValue[5];
					}
				}else{
				//columns to be populated in Update page
				// nodetype is NA for features like bulkcm
					if (nodeType.equals(NOTAVAILABLE)) {
						topologyVersion = NOTAVAILABLE;
						techpackVersion = getTechPackVersion(nodeType);
						mwsVersion = getMWSRstate(nodeType);
					} else {
						topologyVersion = getTopologyVersion(nodeType, nodeValue[2], nodeValue[3], nodeValue[4]);
						techpackVersion = getTechPackVersion(nodeType);
						mwsVersion = nodeValue[5];
					}
					if (techpackVersion.equals(mwsVersion)) {
						if (!techpackVersion.equals("NA") & !mwsVersion.equals("NA")) {
							techpackVersion = techpackVersion.concat("(" + getInstalledTechPackBuild(nodeType) + ")");
							mwsVersion = mwsVersion.concat("(" + getMWSTechPackBuild(nodeType) + ")");
						}
					}
				}
				nodeMap.put("NODE_TYPE", nodeType);
				nodeMap.put("NODE_VERSION_NW", topologyVersion);
				nodeMap.put("NODE_VERSION_TP", techpackVersion);
				nodeMap.put("NODE_VERSION_SW_GW", mwsVersion);
				nodeMap.put("CXCNUMBER", cxc);

				nodeInfo.put(nodeType, nodeMap);
			}
		} catch (FileNotFoundException e) {
			log.warn("The .CXC file "+ cxc +" in MWS server cannot be located " + e.getMessage());
			String nodeType = "";
			Map<String, String> nodeMap = new HashMap<String, String>();
			nodeMap.put("NODE_TYPE", "");
			nodeMap.put("NODE_VERSION_NW", "");
			nodeMap.put("NODE_VERSION_TP", "");
			nodeMap.put("NODE_VERSION_SW_GW", "");
			nodeMap.put("CXCNUMBER", cxc);
			nodeInfo.put(nodeType, nodeMap);
		} catch (IOException e) {
			
			log.warn("Cannot read "+upgradePath + File.separator + "." + cxc);
		}
		finally {
			if (buf != null) {
				try {
					buf.close();
				} catch (IOException e) {
					log.warn(bufferErrorMsg + e.getMessage());
				}
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					log.warn(filereaderErrorMsg + e.getMessage());
				}
			}
		}
		return nodeInfo;
	}

	/**
	 * Connects to repdb and queries for techpack name from the tpnode version
	 * table
	 * 
	 * @param nodeType
	 * @return techpack
	 */
	private String getMWSTechPackBuild(String nodeType) {
		String techpackName = "";
		String mwsFileName = "";
		String mwsTechpackBuild = "";
		// determine the Feature techpack
		techpackName = getTechPackVersionId(nodeType);
		try {
			if (!techpackName.isEmpty() | techpackName != null) {
				techpackName = techpackName.substring(0, techpackName.indexOf(":"));
				// determine the Rstate for the installed TP in MWS
				File dir = new File(upgradePath);
				File[] listOfFiles = dir.listFiles();
				for (File file : listOfFiles) {
					if (file.isFile() & file.getName().contains(techpackName + "_R")
							& !file.getName().startsWith("INTF")) {
						mwsFileName = file.getName();
						break;
					}
				}
				mwsTechpackBuild = mwsFileName.substring(mwsFileName.lastIndexOf("_") + 2, mwsFileName.indexOf("."));
				if (mwsTechpackBuild == null) {
					mwsTechpackBuild = NOTAVAILABLE;
					log.debug("Node Version(Build) MWS is null for the feature " + cxc);
				}
			} else {
				mwsTechpackBuild = NOTAVAILABLE;
				log.debug("Node Version(Build) MWS for the feature " + cxc + " could not be determined.");
			}
		} catch (Exception e) {
			log.debug("Couldnt fetch Techpack details for the cxc " + cxc + "due to" + e.getMessage());
			mwsTechpackBuild = NOTAVAILABLE;
		}
		return mwsTechpackBuild;
	}

	private String getInstalledTechPackBuild(String nodeType) {
		String techpackName = "";
		String buildNumber = "";
		// determine the Feature techpack
		techpackName = getTechPackVersionId(nodeType);
		if (!techpackName.isEmpty() | techpackName != null) {
			buildNumber = techpackName.substring(techpackName.lastIndexOf("(")+1,techpackName.indexOf(")"));
		}
		return buildNumber;
	}

	/**
	 * determines the rstate of the TP in MWS
	 * table
	 * 
	 * @param nodeType
	 * @return techpack
	 */
	private String getMWSRstate(String nodeType) {
		String techpackName = "";
		String mwsFileName = "";
		String mwsTechpackRState = "";
		// determine the Feature techpack
			techpackName = getTechPackVersionId(nodeType);
		try {
			if (!techpackName.isEmpty() | techpackName != null) {
				techpackName = techpackName.substring(0, techpackName.indexOf(":"));
				// determine the Rstate for the installed TP in MWS
				File dir = new File(upgradePath);
				File[] listOfFiles = dir.listFiles();
				for (File file : listOfFiles) {
					if (file.isFile() & file.getName().contains(techpackName + "_R")
							& !file.getName().startsWith("INTF")) {
						mwsFileName = file.getName();
						break;
					}
				}
				mwsTechpackRState = mwsFileName.substring(mwsFileName.lastIndexOf("_R") + 1, mwsFileName.lastIndexOf("_"));
				if (mwsTechpackRState == null) {
					mwsTechpackRState = NOTAVAILABLE;
					log.debug("Node Version MWS is null for the feature " + cxc);
				}
			} else {
				mwsTechpackRState = NOTAVAILABLE;
				log.debug("Node Version MWS for the feature " + cxc + " could not be determined.");
			}
		} catch (Exception e) {
			log.info("Couldnt fetch Techpack details for" + cxc + "due to" + e.getMessage());
			mwsTechpackRState = NOTAVAILABLE;
		}
		return mwsTechpackRState;
	}

	/**
	 * Connects to repdb and queries for techpack name from the tpnode version
	 * table
	 * 
	 * @param nodeType
	 * @return techpack
	 */
	private String getTechPackVersionId(String nodeType) {
		String techpackName = "";
		try {
			if (!repdbConnection.isClosed()) {
				Statement stmt = null;
				ResultSet resultSet = null;

				String tpVersionSql = "select top 1 VERSIONID from versioning WHERE VERSIONID in "
						+ "(select distinct VERSIONID from tpActivation where STATUS='ACTIVE') and "
						+ "LICENSENAME like '%" + cxc
						+ "%' and TECHPACK_NAME in (select distinct TECHPACK_NAME from TPNodeVersion where node_Type = '"
						+ nodeType + "')";
				try {
					stmt = repdbConnection.createStatement();

					resultSet = stmt.executeQuery(tpVersionSql);

					if (resultSet != null) {
						while (resultSet.next()) {
							techpackName = resultSet.getString(1);
						}
					}
				} catch (SQLException e) {
					log.warn("SQL Exception occured while quering for tp node version " + e.getMessage());
					return NOTAVAILABLE;
				} catch (Exception e) {
					log.warn("Exception occured while quering for tp node version " + e.getMessage());
					return NOTAVAILABLE;
				} finally {
					if (resultSet != null) {
						try {
							resultSet.close();
						} catch (SQLException e) {
							log.warn("Could not close result set " + e.getMessage());
						}
					}
					if (stmt != null) {
						try {
							stmt.close();
						} catch (SQLException e) {
							log.warn(closeStmtErrorMsg + e.getMessage());
						}
					}
				}
			}
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		return techpackName;
	}

	/**
	 * Connects to repdb and queries for techpack node version from the tpnode
	 * version table
	 * 
	 * @param nodetype
	 * @return node version in techpack
	 */
	@SuppressWarnings("resource")
	private String getTechPackVersion(String nodeType) {
		String tpNodeVersion = "";

		try {
			if (!repdbConnection.isClosed()) {
				Statement stmt = null;

				ResultSet resultSet = null;

				String techpackName = null;

				String techpackVersion = null;
				String tpVersionSql = "select top 1 TECHPACK_NAME,TECHPACK_VERSION from versioning WHERE VERSIONID in "
						+ "(select distinct VERSIONID from tpActivation where STATUS='ACTIVE') and "
						+ "LICENSENAME like '%" + cxc
						+ "%' and TECHPACK_NAME in (select distinct TECHPACK_NAME from TPNodeVersion where node_Type ='"
						+ nodeType + "') order by TECHPACK_NAME";
				try {
					stmt = repdbConnection.createStatement();

					resultSet = stmt.executeQuery(tpVersionSql);

					if (resultSet != null) {
						while (resultSet.next()) {
							techpackName = resultSet.getString(1);
							techpackVersion = resultSet.getString(2);
						}
					}
					// if nodetype is NA , consider its Rstate as node version
					if (nodeType.equals(NOTAVAILABLE)) {
						tpNodeVersion = techpackVersion;
					} else {
						String tpNodeVersionSql = "select NODE_VERSION from TPNodeVersion where TECHPACK_NAME = '"
								+ techpackName + "' and TECHPACK_VERSION = '" + techpackVersion + "' and NODE_TYPE ='"
								+ nodeType + "'";
						// Get the nodeversion for the particular nodetype
						resultSet = stmt.executeQuery(tpNodeVersionSql);

						while (resultSet.next()) {
							tpNodeVersion = resultSet.getString("NODE_VERSION");
						}
					}
				} catch (SQLException e) {
					log.warn("SQL Exception occured while quering for tp node version " + e.getMessage());
					tpNodeVersion = NOTAVAILABLE;
				} catch (Exception e) {
					log.warn("Exception occured while quering for tp node version " + e.getMessage());
					tpNodeVersion = NOTAVAILABLE;
				} finally {
					if (resultSet != null) {
						try {
							resultSet.close();
						} catch (SQLException e) {
							log.warn("Could not close result set " + e.getMessage());
						}
					}
					if (stmt != null) {
						try {
							stmt.close();
						} catch (SQLException e) {
							log.warn(closeStmtErrorMsg + e.getMessage());
						}
					}
				}
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
		if (tpNodeVersion == null) {
			tpNodeVersion = NOTAVAILABLE;
			log.debug("Node Version TP is null for the feature " + cxc);
		}
		return tpNodeVersion;
	}

	/**
	 * Connects to dwhdb and queries for Node version in the topology tables
	 * 
	 * @param nodetype
	 * @param tablename
	 * @param managedelementtype
	 * @param version
	 *            column
	 * @return topologyversion
	 */
	private String getTopologyVersion(String nodeType, String tableName, String nodeColumn, String versionColumn) {

		String topologyVersion = "";

		try {
			if (!dwhdbConnection.isClosed()) {
				Statement stmt = null;
				ResultSet resultSet = null;
				List<String> topologyVersionList = new ArrayList<String>();
				if (nodeType.contains(":")) {
					nodeType = getNodeTypeAsArray(nodeType);
				} else {
					nodeType = "'" + nodeType + "'";
				}
				String sql = "select distinct " + versionColumn + " from " + tableName + " where " + nodeColumn
						+ " in (" + nodeType + ") and status = 'ACTIVE'";
				try {
					stmt = dwhdbConnection.createStatement();

					resultSet = stmt.executeQuery(sql);

					if (resultSet != null) {
						while (resultSet.next()) {
							topologyVersionList.add(resultSet.getString(1));
						}
					}
					// Pass the arraylist to the user defined comparator
					if (topologyVersionList.isEmpty()) {
						log.debug("Topology table " + tableName + " does not contain the active node version");

					} else {
						NodeVersionComparator nodeVersionComparator = new NodeVersionComparator();
						String[] topologyVersionArray = new String[topologyVersionList.size()];
						topologyVersion = nodeVersionComparator
								.highestNodeFromList(topologyVersionList.toArray(topologyVersionArray));
					}

				} catch (SQLException e) {

					log.warn("Exception in querying the " + tableName + " due to " + e.getMessage() + "for the nodetype " +nodeType + "and the feature " + cxc);

				} catch (Exception e) {

					log.warn("Topology information could not be fetched due to " + e.getMessage());

				} finally {

					if (resultSet != null) {
						try {
							resultSet.close();
						} catch (SQLException e) {
							log.warn("Could not close result set " + e.getMessage());
						}
					}
					if (stmt != null) {
						try {
							stmt.close();
						} catch (SQLException e) {
							log.warn(closeStmtErrorMsg + e.getMessage());
						}
					}
				}

			}
		} catch (Exception e) {

			log.warn("Could not establish dwhdb connection.");
		}

		return topologyVersion;
	}

	private String getNodeTypeAsArray(String nodeType) {

		String[] nodeTypeArray = nodeType.split(":");
		String multipleNodeTypes = "";
		for (String type : nodeTypeArray) {
			multipleNodeTypes = multipleNodeTypes.concat("'" + type + "',");
		}
		multipleNodeTypes = multipleNodeTypes.substring(0, multipleNodeTypes.lastIndexOf(","));

		return multipleNodeTypes;
	}
	
	
	public List<String> pwdDbConnect()  {
		
		List<String> pwdList = new ArrayList<String>();
		
		try{
			etlcserverprops =  new ETLCServerProperties(System.getProperty(ETLCServerProperties.CONFIG_DIR_PROPERTY_NAME)+"/ETLCServer.properties");
		}catch(IOException e){
			log.warn("Could not get server properties "+ e.getMessage());
		}
		
		RockFactory etlrep = null;
		Meta_databases db_prop;
		Meta_databases where_obj;
		Meta_databasesFactory md_fact;
		List<Meta_databases> dbs;
		
		try {
			if (!repdbConnection.isClosed()) {
				etlrep = new RockFactory(etlcserverprops.getProperty(ETLCServerProperties.DBURL), etlcserverprops.getProperty(ETLCServerProperties.DBUSERNAME),
						etlcserverprops.getProperty(ETLCServerProperties.DBPASSWORD), etlcserverprops.getProperty(ETLCServerProperties.DBDRIVERNAME),
						"InformationStoreParser",false);
				
				where_obj = new Meta_databases(etlrep);
					
				where_obj.setUsername("dcuser");
				md_fact = new Meta_databasesFactory(etlrep, where_obj);
				dbs = md_fact.get();
				if (dbs.size() <= 0) {
					throw new RockException("Could not extract details for dcuser.");
				}
				
				Iterator<Meta_databases> itr = dbs.iterator();
				while(itr.hasNext()){
					Meta_databases mdb = (Meta_databases)itr.next();
					if(!pwdList.contains(mdb.getPassword())){
						pwdList.add(mdb.getPassword());
					}
				}
			}
		} catch (SQLException | RockException e) {
			
			log.warn("Exception: "+e);
		} finally {
			try {
				if (etlrep != null)
					etlrep.getConnection().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return pwdList;
	}

}
