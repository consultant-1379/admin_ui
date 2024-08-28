package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.util.HashMap;
import java.util.Map;

public class ECStatus {

	private static final String CONTROL_ZONE_REPLACE = "controlzone";
	static final String CONTROL_ZONE = "Control Zone";

	private static final Map<String, String> ecStatusMap = new HashMap<String, String>(); 
	private static final Map<String, Boolean> ecIsRunning = new HashMap<String, Boolean>(); 

	private ECStatus() {}
	
	/**
	 * Splits message into an ec context name and its status.
	 * Message is taken from mediation zone status and will be like
	 * "EC1 is running" or "EC1 is " (when called from LoaderStatus
	 * which splits the returned message text using "running" as the
	 * delimiter).
	 *  
	 * Adds a mapping to ecStatus for the ec name, with the value 
	 * equal to the status. Also adds a mapping to ecIsRunning which 
	 * is true if the service is running, otherwise false.
	 * 
	 * ec context name is the first "word" of item (or first two 
	 * for "Control Zone". The standardised context name is used
	 * as the key.
	 * 
	 * @param message String to process
	 */
	public static void add(final String message) {
		if (message!=null) {
			String ecName = message.trim().toLowerCase().replaceAll(CONTROL_ZONE.toLowerCase(), CONTROL_ZONE_REPLACE);
			String ecExtra = "";
			final int idx = ecName.indexOf(" ");
			if (idx>0) {
				ecExtra = ecName.substring(idx+1);
				
				// if message doesn't include "running", add it back in.
				if (ecExtra!=null && !ecExtra.contains(MonitorInformation.MZ_STATUS_RUNNING_KEY)) {
					ecExtra += " " + MonitorInformation.MZ_STATUS_RUNNING;
				}
				ecName = ecName.substring(0, idx);
			}
			ecName = standardiseName(ecName);
			ecStatusMap.put(ecName, ecExtra);
			
			if (ecExtra!=null && ecExtra.contains(MonitorInformation.MZ_STATUS_RUNNING_KEY)) {
				ecIsRunning.put(ecName, true);
			} else {
				ecIsRunning.put(ecName, false);
			}
		}
	}
	
	/** 
	 * Standardise ec name to make checking simpler and avoid issues with
	 * differences between name in service and output of mzsh status reports
	 * 
	 * Name is set to lowercase, ec1 and ec2 converted to ec_1 and ec_2
	 * and "control zone" to "controlzone".
	 * 
	 * @param ecName
	 * @return
	 */
	private static String standardiseName(final String ecName) {
		String stdName = ecName;
		
		if (stdName != null) {
			stdName = stdName.trim().toLowerCase().replaceAll(CONTROL_ZONE.toLowerCase(), CONTROL_ZONE_REPLACE);
			
			if (stdName.matches("ec"+"[1-9]")) {
				stdName = "ec_" + stdName.substring(2);
			}
		}
		
		return stdName;
	}
	
	/** 
	 * Checks if service name is in the ec status map
	 * 
	 * @param serviceName
	 * @return true if service name is in the ec status map, otherwise false
	 */
	public static boolean isLoggedService(final String serviceName) {
		boolean isLogged = false;
		
		if (ecStatusMap.containsKey(standardiseName(serviceName))) {
			isLogged = true;
		}
		
		return isLogged;
	}
	
	
	/**
	 * Check if specified ec is running
	 * 
	 * @param ecServiceName service name of ec
	 * @return true if ec is running, otherwise false
	 */
	public static boolean ecIsRunning(final String ecServiceName) {
		boolean isRunning = false;
		
		final String stdEcServiceName = standardiseName(ecServiceName);
		if (ecStatusMap.containsKey(stdEcServiceName)) {
			isRunning=ecIsRunning.get(stdEcServiceName);
		}
		
		return isRunning;
	}

	/** 
	 * Get the ec status map. Included for testing purposes
	 * 
	 * @return
	 */
	public static Map<String, String> getECStatusMap() {
		return ecStatusMap;
	}
	
	/** 
	 * Clear the ec status map. Included mainly for testing purposes
	 * 
	 * @return
	 */
	public static void clear() {
		ecStatusMap.clear();
	}
}
