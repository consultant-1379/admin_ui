package com.distocraft.dc5000.etl.gui.enminterworking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.distocraft.dc5000.common.RmiUrlFactory;
import com.ericsson.eniq.enminterworking.IEnmInterworkingRMI;
import com.ericsson.eniq.repository.ETLCServerProperties;

public class EnmInterUtils {
	
	private static Log log = LogFactory.getLog("com.distocraft.dc5000.etl.gui.enminterworking.EnmInterUtils");
	
	public static List<List<String>> getRoleTable (Connection dwhrep)
	{
		List<List<String>> rTable = new ArrayList<List<String>>();
		String sql = "Select ENIQ_ID, IP_ADDRESS, ROLE from RoleTable";
		try(ResultSet rSet = dwhrep.createStatement().executeQuery(sql);)
		{
			while(rSet.next())
			{
				List<String> row = new ArrayList<String>();
				row.add(rSet.getString("ENIQ_ID"));
				row.add(rSet.getString("IP_ADDRESS"));
				row.add(rSet.getString("ROLE"));
				log.debug("RoleTable row - " + rSet.getString("ENIQ_ID") + " - " + rSet.getString("IP_ADDRESS") + " - " + rSet.getString("ROLE"));
				rTable.add(row);
			}
		}
		catch (Exception e)
		{
			log.error("Exception: ", e);
		}

		return rTable;
	}
	
	public static String getSelfRole (Connection dwhrep)
	{
		String role = null;
		ResultSet result = null;
		ResultSet count = null;
		String countSql = "Select count(*) from RoleTable";
		String roleSql = "Select ROLE from RoleTable where ENIQ_ID = '" + getEngineHostname() 
		+ "' and IP_ADDRESS = '" + getEngineIP() + "'";
		log.debug("Role SQL is - " + roleSql);
		try
		{
			count = dwhrep.createStatement().executeQuery(countSql);
			if(count.next()){
				log.debug("Number of rows in Role Table- " + count.getString(1));			
				if(count.getString(1).equals("0"))
				{
					role = "UNASSIGNED";
				}
				else
				{
					result = dwhrep.createStatement().executeQuery(roleSql);
					if(result.next()){
						role = result.getString("ROLE");
					}
					else {
						role = "UNDEFINED OR COULD NOT RETRIEVE ROLE";
					}
				}
			}
			else {
				role = "UNDEFINED OR COULD NOT RETRIEVE ROLE";
			}
		}
		catch (Exception e)
		{
			role = "UNDEFINED OR COULD NOT RETRIEVE ROLE";
			log.error("Exception: ", e);
		}
		finally {
		      // finally clean up
		      try {
		        if (result != null) {
		        	result.close();
		        }
		        if (count != null) {
		        	count.close();
		        }
		      } catch (SQLException e) {
		        log.error("SQLException: ", e);
		      }
		    }
		return role;
	}
	
	public static String getEngineHostname() {
		String hostname = null;
		String engineHostname = null;
		try {
			Scanner scan = new Scanner(new FileReader(System.getProperty(ETLCServerProperties.CONFIG_DIR_PROPERTY_NAME)+"/service_names"));
			while(scan.hasNext()){
				engineHostname = scan.next();
				if(engineHostname.contains("engine")){
					hostname = parsePattern(engineHostname,".+::(.+)::.+");
				}
			}
			scan.close();
		} catch (FileNotFoundException e1) {
			log.warn("Cannot read file to get engine name, treating as standalone");
			return System.getenv("hostname");
		}
		return hostname;
	}
	
	/**
	 * 
	 * @return string Webserver Hostname
	 */
	public static String getWebserverHostname() {
		String hostname = null;
		String service_name = null;
		try {
			Scanner scan = new Scanner(new FileReader(System.getProperty(ETLCServerProperties.CONFIG_DIR_PROPERTY_NAME)+"/service_names"));
			while(scan.hasNext()){
				service_name = scan.next();
				if(service_name.contains("webserver")){
					hostname = parsePattern(service_name,".+::(.+)::.+");
				}
			}
			scan.close();
		} catch (FileNotFoundException e1) {
			log.warn("Cannot read file to get webserver, treating as standalone");
			return hostname;
		}
		return hostname;
	}
	
	/**
	 * 
	 * @param hostname
	 * @return Fully Qualified Host Name
	 */
	public static String getFullyQualifiedHostname() {
		String command  = "/usr/bin/host " + getWebserverHostname();
		Process process = null;
		String servername = null;
		String line;
		try {
			process = Runtime.getRuntime().exec(command);
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			if ((line = br.readLine()) != null) {
				//To handle vApps where this command returns "xx not found"
				if (!line.contains("not found")) {
					servername = line.split("\\s+")[0];
				}
			}
		} catch (IOException e) {
			log.error("IOException while retrievin fully qualified host name ", e);
		} finally {
			if(process != null) {
				process.destroy();	
			}
		}
		return servername;		
	}
	
	public static String getEngineIP() {
		String ip = null;
		String engineIp = null;
		try {
			Scanner scan = new Scanner(new FileReader(System.getProperty(ETLCServerProperties.CONFIG_DIR_PROPERTY_NAME)+"/service_names"));
			while(scan.hasNext()){
				engineIp = scan.next();
				if(engineIp.contains("engine")){
					ip = parsePattern(engineIp,"(.+)::.+::.+");
				}
			}
			scan.close();
		} catch (FileNotFoundException e1) {
			log.warn("Cannot read file");
		}
		return ip;
	}
	
	private static String parsePattern(String str, String regExp) {
		final Pattern pattern = Pattern.compile(regExp);
		final Matcher matcher = pattern.matcher(str);
		String result = "";
		// Find a match between regExp and return group 1 as result.
		if (matcher.find()) {
			result = matcher.group(1);
			log.debug(" regExp (" + regExp + ") found from subset of " + str + "  :" + result);
		} else {
			log.warn("No subset of String " + str + " matchs defined regExp " + regExp);
		}
		return result;
	}
	
	public static boolean validateIPAdd(final String ip) {
		if(ip.equals(getEngineIP())){
			return false;
		}
		final Pattern PATTERN = Pattern.compile(
		        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	    return PATTERN.matcher(ip).matches();
	}
	
	public static List<String> getAllServers(Connection dwhrep) {
		List<String> allRoles = new ArrayList<String>();
	
		try(Statement stmt = dwhrep.createStatement();
			ResultSet rSet = stmt.executeQuery("Select ENIQ_ID from RoleTable");) {
			
			while(rSet.next())
			{
				allRoles.add(rSet.getString("ENIQ_ID"));
			}
		}
		catch (Exception e)
		{
			log.error("Exception while getting ENIQ_ID ", e);
		}	
		return allRoles;
	}
	
	/**
	 * 
	 * @return enmHostnames of servers which are integrated with ENIQ.
	 */
	public static List<String> getAllENMHostnames() {
		try {
			IEnmInterworkingRMI multiEs = (IEnmInterworkingRMI)Naming.lookup(RmiUrlFactory.getInstance().getMultiESRmiUrl(EnmInterUtils.getEngineIP()));
			return multiEs.getFlsEnabledEnms();
		} catch (RemoteException | MalformedURLException | NotBoundException  e) {
			log.warn("Exception while getting the list of enm hostnames ", e);
		}
		return new ArrayList<>();
	}
	
	/**
	 * @return enmAliases of servers which are integrated with ENIQ.
	 */
	public static Map<String,String> getOssIdToHostNameMap() {
		try{
			IEnmInterworkingRMI multiEs = (IEnmInterworkingRMI)Naming.lookup(RmiUrlFactory.getInstance().getMultiESRmiUrl(EnmInterUtils.getEngineIP()));
			return multiEs.getOssIdToHostNameMap();
		} catch(RemoteException | MalformedURLException | NotBoundException e) {
			log.warn("Exception while getting the list of enm aliases ", e);
			return new HashMap<String,String>();
		}
	}
	
	/**
	 * @return list of enms for which persisted file is missing.
	 */
	public static List<String> getEnmWithoutPersisterFile() {
		List<String> allENMHostnames = new ArrayList<>();
		Map<String, String> ossIdToHostnameMap = getOssIdToHostNameMap();
		for(String enm : ossIdToHostnameMap.keySet()) {
			  String file_path = System.getProperty("CONF_DIR") + File.separator + "Persisted_"+enm+".ser";
			  File config_file = new File(file_path);
			  if(!config_file.exists()) {
				  allENMHostnames.add(ossIdToHostnameMap.get(enm));
			  }
	    }	
		return allENMHostnames;
	}
}