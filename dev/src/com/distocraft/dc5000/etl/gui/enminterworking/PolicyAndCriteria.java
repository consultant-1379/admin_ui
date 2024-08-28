package com.distocraft.dc5000.etl.gui.enminterworking;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.distocraft.dc5000.common.RmiUrlFactory;
import com.ericsson.eniq.enminterworking.EnmInterCommonUtils;
import com.ericsson.eniq.enminterworking.IEnmInterworkingRMI;

public class PolicyAndCriteria {

	private final Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * 
	 * @param dwhrep
	 * @param technology
	 * @param regexString
	 * @param identifier
	 * @param enmHostname
	 * @return true if policy is inserted successfully in table ENIQS_Policy_Criteria.
	 */
	public boolean insertPolicy(Connection dwhrep, String technology, 
			String regexString, String identifier, String enmHostname) {				

		boolean isInserted = false;
		boolean isRmi = false;
		final String roleSql = "Select IP_ADDRESS from RoleTable where ROLE='SLAVE'";
		final String sql = "Insert into ENIQS_Policy_Criteria values ('" + technology + "','" + regexString + "','"
				+ identifier + "','" + enmHostname + "')";
		
		try(Statement stmt = dwhrep.createStatement()) {
			stmt.execute(sql);
			log.info("Inserted policy successfully with TECHNOLOGY = " + technology
					+ " NAMINGCONVENTION = " + regexString + " ENIQ_IDENTIFIER = " + identifier
					+ " ENM_HOSTNAME = " + enmHostname);
			isInserted = true;
			//insert the same into ENIQ-S slaves (if present)
			try(ResultSet rs = stmt.executeQuery(roleSql)) {
				while (rs.next()) {
					String IPAddress = rs.getString("IP_ADDRESS");
					log.debug("The ipaddress is " + IPAddress);
					IEnmInterworkingRMI multiEs =  (IEnmInterworkingRMI) Naming.lookup(RmiUrlFactory.getInstance().getMultiESRmiUrl(IPAddress));
					isRmi = multiEs.policyCriteriaInsert(technology, regexString, identifier, enmHostname);
					if(isRmi){
						log.info("Inserted the row successfully in " + rs.getString("IP_ADDRESS"));
					}
				} 
			}
		} catch (SQLException e) {
			log.error("SQLException: ", e);
		} catch (MalformedURLException e) {
			log.warn("MalformedURLException at insertPolicy: "+e);
		} catch (RemoteException e) {
			log.warn("RemoteException at insertPolicy: "+e);
		} catch (NotBoundException e) {
			log.warn("NotBoundException at insertPolicy: "+e);
		}
		return isInserted;
	}
	
	/**
	 * @param name
	 * @return
	 */
	protected boolean isPatternMatching(String name) {
		try{
			@SuppressWarnings("unused")
			Pattern pattern = Pattern.compile(name);
			return true;
		}
		catch(Exception e){
			log.debug("pattern :"+name+ " is not valid");
			return false;
		}
	}

	protected ArrayList<ArrayList<String>> getAllPolicies(Connection dwhrep) {
		ArrayList<ArrayList<String>>  pList = new ArrayList<ArrayList<String>>();
		
		try(Statement stmt = dwhrep.createStatement();
			ResultSet rSet = stmt.executeQuery("Select * from ENIQS_Policy_Criteria");) {			
			while (rSet.next()) {
				ArrayList<String> list = new ArrayList<String>();
				list.add(rSet.getString("TECHNOLOGY"));
				list.add(rSet.getString("NAMINGCONVENTION"));
				list.add(rSet.getString("ENM_HOSTNAME"));
				list.add(rSet.getString("ENIQ_IDENTIFIER"));
				pList.add(list);
			}
		} catch (Exception e) {
			log.warn("Exception caught in getAllPolicies method", e);
		}
		return pList;
	}

	// refines the policy
	public String[] refinePolicy(String index, Connection dwhrep) {
		String[] out1 = null;
		HashMap<Integer, String[]> tablesContents = new HashMap<Integer, String[]>();
		String sqltableContent = "select * from ENIQS_Policy_Criteria ";
		log.debug("sql  :  " + sqltableContent);
		
		try(Statement stmt = dwhrep.createStatement();
			ResultSet tableContentRS = stmt.executeQuery(sqltableContent);) {
			ResultSetMetaData meta = tableContentRS.getMetaData();
			int col = meta.getColumnCount();
			out1 = new String[col];
			while (tableContentRS.next()) {
				log.debug("row :: " + tableContentRS.getRow());
				String[] values = new String[col];
				for (int column = 1; column <= col; column++) {
					Object value = tableContentRS.getObject(column);
					values[column - 1] = String.valueOf(value);
				}
				tablesContents.put(tableContentRS.getRow(), values);
			}
			int index1 = Integer.parseInt(index) + 1;
			out1 = tablesContents.get(index1);
			for (int i = 1; i <= 4; i++) {
				log.debug(tablesContents.get(i));
			}
			log.debug(out1);

		} catch (SQLException e) {
			log.warn("Exception caught at refinePolicy method", e);
		}
		return out1;
	}
	
	/**
	 * 
	 * @param dwhrep
	 * @param editPolicy
	 * @param technology
	 * @param regexString
	 * @param identifier
	 * @param enmHostname
	 * @return true if the combination is already present in db.
	 */
	public boolean isCombinationExists(Connection dwhrep, boolean editPolicy, 
			String technology, String regexString, String identifier, String enmHostname) { 
		
		String sqlCheck;
		if(editPolicy) {
			//To edit a policy first check if the given combination of values exist in db.
			sqlCheck = " select * from  ENIQS_Policy_Criteria where TECHNOLOGY='" + technology
				+ "' and NAMINGCONVENTION='" + regexString + "' and ENIQ_IDENTIFIER='" 
				+ identifier + "' and ENM_HOSTNAME='" + enmHostname + "'";
		} else {
			//To add a new Policy check if the given tech, NamingConvention and ENM combination exist in db.
			sqlCheck = " select * from  ENIQS_Policy_Criteria where TECHNOLOGY='" + technology
				+ "' and NAMINGCONVENTION='" + regexString + "' and ENM_HOSTNAME='" +enmHostname +"'" ;
		}			
		log.debug("Checking for existing Combination, sql  :  " + sqlCheck);
		
		try(Statement stmt = dwhrep.createStatement();
			ResultSet rs = stmt.executeQuery(sqlCheck);) {	
			while(rs.next()){
				if (rs.getString("NAMINGCONVENTION").equals(regexString)) {
					// policy already exists
					log.info("Policy Already exists!! with TECHNOLOGY = " + technology
					+ " NAMINGCONVENTION = " + regexString + " ENIQ_IDENTIFIER = " + identifier
					+ " ENMHostanem = " + enmHostname);
					return true;
				} 
			}
		} catch (SQLException e) {
			log.error("Exception occurred while updating the Policy and Criteria!!", e);
		}
		return false;
	}
	
	/**
	 * Updates the Policy with the given details.
	 * 
	 * @param dwhrep
	 * 			connection object
	 * @param column
	 * 			A String array of technology, regexString, identifier, enmHostname,
	 * 						oldTechnology, oldNaming, oldIdentifier, oldENMHostname
	 */
	public void updatePolicy(Connection dwhrep, String... column) {
		
		ResultSet rs = null;
		boolean isRmi = false;
		String roleSql="Select IP_ADDRESS from RoleTable where ROLE='SLAVE'";
		String sqlUpdate = " update ENIQS_Policy_criteria set " + " TECHNOLOGY=\'" + column[0] + "' , "
				+ " NAMINGCONVENTION=\'" + column[1] + "' , " + " ENIQ_IDENTIFIER=\'" + column[2]
				+  "' , " + " ENM_HOSTNAME=\'" + column[3] + "' where "
				+ " TECHNOLOGY=\'" + column[4] + "' and " + " NAMINGCONVENTION=\'" + column[5] + "' and "
				+ " ENIQ_IDENTIFIER=\'" + column[6] + "' and" + " ENM_HOSTNAME=\'" + column[7] + "'";
		log.debug("UpdatePolicy sql command: " + sqlUpdate);
		
		try(Statement stmt = dwhrep.createStatement();) {
			
			stmt.execute(sqlUpdate);
			log.info("Succesfully Updated the Policy and Criteria!! with TECHNOLOGY = " + column[0]
					+ " NAMINGCONVENTION = " + column[1] + " ENIQ_IDENTIFIER = " + column[2]
					+ " ENMHostname = " + column[3]);
			
			//update the same into ENIQ-S slaves (if present)
			rs = stmt.executeQuery(roleSql);
			while (rs.next()) {
				String IPAddress = rs.getString("IP_ADDRESS");	
				log.debug("The ipaddress is " + IPAddress);
				IEnmInterworkingRMI multiEs =  (IEnmInterworkingRMI) Naming.lookup(RmiUrlFactory.getInstance().getMultiESRmiUrl(IPAddress));
				isRmi = multiEs.policyCriteriaUpdate(column[0], column[1], column[2], column[3], 
						column[4], column[5], column[6], column[7]);
				if(isRmi){
					log.info("Updated the row successfully in " + rs.getString("IP_ADDRESS"));
				}
			}
		} catch (SQLException e) {
			log.error("Exception occured while updating the Policy and Criteria!!", e);
		}catch (MalformedURLException e) {
			log.warn("MalformedURLException at updatePolicy: "+ e);
		} catch (RemoteException e) {
			log.warn("RemoteException at updatePolicy: "+ e);
		} catch (NotBoundException e) {
			log.warn("NotBoundException at updatePolicy: "+ e);
		}
	}
	
	/**
	 * Deletes the Policy with the given details.
	 * 
	 * @param dwhrep
	 * 			connection object
	 * @param data
	 * 			A String array of technology, regexString, identifier, enmHostname,
	 */
	public boolean deletePolicy(String[] data, Connection dwhrep) {
		
		ResultSet rsDelete = null;
		ResultSet rs = null;
		boolean isRmi = false;
		String roleSql="Select IP_ADDRESS from RoleTable where ROLE='SLAVE'";
		String sqlDwhrep = "delete from ENIQS_POLICY_CRITERIA where Technology = '"+data[0]+"' AND NAMINGCONVENTION = "
				+ "'"+data[1]+"' AND ENIQ_IDENTIFIER = '"+data[2]+"' AND ENM_HOSTNAME = '"+data[3]+"'";
		Statement stmt = null;
		boolean result = true;
		String netype = null;
		String selectNatsql= null;
		IEnmInterworkingRMI multiEsMaster = null;
		Map<String, String> nodeTechMap = getNodeTechnologyMapping();
		
		if(!nodeTechMap.isEmpty() && nodeTechMap.get(data[0])!=null) {
			String[] value = nodeTechMap.get(data[0]).split(",");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < value.length-1; i++) {
				sb.append("'"+value[i]+"'"+",");
			}
			sb.append("'"+value[value.length-1]+"'");
			netype = sb.toString();
			selectNatsql="select * from eniqs_node_assignment where enm_hostname='"+data[3]+"'"
					+ "and eniq_identifier like '%"+data[2]+"%' and netype in ("+netype+")";
		}else {
			selectNatsql="select * from eniqs_node_assignment where enm_hostname='"+data[3]+"' and eniq_identifier like '%"+data[2]+"%'";
		}
		log.debug("selectNatsql: "+selectNatsql);
		try {
			stmt = dwhrep.createStatement();
			stmt.executeQuery(sqlDwhrep);
			String masterIP = EnmInterCommonUtils.getEngineIP();
			multiEsMaster = (IEnmInterworkingRMI) Naming.lookup(RmiUrlFactory.getInstance().getMultiESRmiUrl(masterIP));
			multiEsMaster.refreshNodeAssignmentCache();
			
			//delete the same into ENIQ-S slaves (if present)
			rsDelete = stmt.executeQuery(roleSql);
			while (rsDelete.next()) {
				String IPAddress = rsDelete.getString("IP_ADDRESS");	
				log.debug("The ipaddress is " + IPAddress);
				IEnmInterworkingRMI multiEs =  (IEnmInterworkingRMI) Naming.lookup(RmiUrlFactory.getInstance().getMultiESRmiUrl(IPAddress));
				isRmi = multiEs.deletePolicyCriteria(data);
				if(isRmi){
					log.info("Deleted the row successfully in " + rsDelete.getString("IP_ADDRESS"));
				}
			}
			log.info("Successfully deleted policy: "+Arrays.toString(data));
			
			// To reassign the assigned nodes
			rs = stmt.executeQuery(selectNatsql);
			while(rs.next()) {
				String neType = rs.getString("NETYPE");
				String fdn = rs.getString("FDN");
				String enmHostName = rs.getString("ENM_HOSTNAME");
				multiEsMaster.addingToBlockingQueue(neType, fdn, enmHostName, true);
			}
			multiEsMaster.refreshNodeAssignmentCache();
		} catch (SQLException e) {
			log.warn("Unable to delete policy " + e);
			result = false;
		} catch (MalformedURLException e) {
			log.warn("MalformedURLException at deletepolicy: "+ e);
		} catch (RemoteException e) {
			log.warn("RemoteException at deletepolicy: "+ e);
		} catch (NotBoundException e) {
			log.warn("NotBoundException at deletepolicy: "+ e);
		}finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rsDelete != null)
					rsDelete.close();
				if(rs != null)
					rs.close();
			} catch (SQLException e) {
				log.warn("Unable to close connections " + e);
			}
		}
		return result;
	}
	
	public Map<String, String> getNodeTechnologyMapping() {
		BufferedReader reader = null;
		Map<String, String> nodeTechnologyMap = new HashMap<String, String>();
		try {
			reader = new BufferedReader(new FileReader("/eniq/sw/conf/NodeTechnologyMapping.properties"));
			String line = reader.readLine();
			while(line != null) {
				String key = line.substring(0, line.indexOf("-"));
				String value = line.substring(line.indexOf("-")+1, line.length());
				nodeTechnologyMap.put(key, value);
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			return nodeTechnologyMap;
		} catch (IOException e) {
			log.warn("Unable to read file: "+e);
		}finally {
			try {
				reader.close();
			} catch (IOException e) {
				log.warn("Unable to close BufferedReader",e);
			}
		}
		return nodeTechnologyMap;
	}
}