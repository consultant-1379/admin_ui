package com.distocraft.dc5000.etl.gui.common;

/**
 * @author eanguan
 */
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.distocraft.dc5000.common.ServicenamesHelper;

public class RmiUrlFactory {
	private final static String defaultRMIPortS = "1200" ;
	private final static int defaultRMIPortI = 1200 ;
	private final static String defaultSchedulerRefName = "Scheduler" ;
	private final static String defaultEngineRefName = "TransferEngine" ;
	private final static String defaultLicRefName = "LicensingCache" ;
	private final static String defaultEniqHost = "localhost" ;
	private final static String engineServiceName = "engine";
	private final static String schedulerServiceName = "scheduler";
	private final static String licServiceName = "licenceservice";
	
	private final Log log = LogFactory.getLog(this.getClass());
	private static RmiUrlFactory _staticInstance;
	private static Properties appProps ;
	
	/**
	 * Public method to get the singleton object of class RmiUrlFactory
	 * @return RmiUrlFactory
	 */
	public static RmiUrlFactory getInstance(){
		if(_staticInstance == null){
			_staticInstance = new RmiUrlFactory();
		}
		return _staticInstance ;
	}
	
	/**
	 * private Constructor
	 */
	private RmiUrlFactory(){
		getStarted();
	}
	
	/**
	 * Method to load the properties defined in ETLC properties file
	 */
	private void getStarted() {
		String confDir = System.getProperty("dc5000.config.directory","/eniq/sw/conf/");
		if (!confDir.endsWith(File.separator)) {
			confDir += File.separator;
		}
		appProps = new Properties();
		FileInputStream file = null;
		try{
			file = new  FileInputStream(confDir + "ETLCServer.properties");
			appProps.load(file);
			
		}catch(final Exception e){
			log.error("Failed to read the ETLC properties file. ", e);
		}finally{
			try{
				file.close();
			}catch(final Exception e){
				//Don't do anything
			}
		}		
		//String serverHostName = appProps.getProperty("ENGINE_HOSTNAME");
	}


	/**
	 * To get the RMI URL for engine
	 * @return String: engine RMI URL
	 */
	public String getEngineRmiUrl(){
		log.debug("Begin: getEngineRmiUrl");
		String rmiURL = null ;
		String engineHostName = appProps.getProperty("ENGINE_HOSTNAME");
		if(engineHostName == null || engineHostName.isEmpty()){
			log.debug("Engine host name is not defined in ETLC properties file.");
			//Get from service name of engine
			try{
				log.debug("Getting Engine host name via service name: " + engineServiceName);
				engineHostName = ServicenamesHelper.getServiceHost(engineServiceName, defaultEniqHost);
			}catch(final Exception e){
				log.debug("Exception comes while getting Engine host name via service name. Setting it to default hostname: " + defaultEniqHost);
				engineHostName = defaultEniqHost ;
			}
		}
		log.debug("Engine host name set as: " + engineHostName);
		
		final String enginePortS = appProps.getProperty("ENGINE_PORT", defaultRMIPortS);
		int enginePortI = defaultRMIPortI ;
		try {
			enginePortI = Integer.parseInt(enginePortS);
		}catch (final Exception e) {
			enginePortI = defaultRMIPortI ;
		}
		log.debug("Engine RMI port set as: " + enginePortI);

		final String engineRefName = appProps.getProperty("ENGINE_REFNAME", defaultEngineRefName);
		log.debug("Engine Refrence Name set as: " + enginePortI);
		
		rmiURL = "//" + engineHostName + ":" + enginePortI + "/" + engineRefName ;
		log.info("Created RMI URL for engine : " + rmiURL);
		log.debug("End: getEngineRmiUrl");
		return rmiURL;
	}
	
	/**
	 * To get the RMI URL for scheduler
	 * @return String: scheduler RMI URL
	 */
	public String getSchedulerRmiUrl(){
		log.debug("Begin: getSchedulerRmiUrl");
		String rmiURL = null ;
		String schedulerHostName = appProps.getProperty("SCHEDULER_HOSTNAME");
		if(schedulerHostName == null || schedulerHostName.isEmpty()){
			log.debug("Scheduler host name is not defined in ETLC properties file.");
			//Get from service name of engine
			try{
				log.debug("Getting Scheduler host name via service name: " + schedulerServiceName);
				schedulerHostName = ServicenamesHelper.getServiceHost(schedulerServiceName, defaultEniqHost);
			}catch(final Exception e){
				log.debug("Exception comes while getting Scheduler host name via service name. Setting it to default hostname: " + defaultEniqHost);
				schedulerHostName = defaultEniqHost ;
			}
		}
		log.debug("Scheduler host name set as: " + schedulerHostName);
		
		final String schedulerPortS = appProps.getProperty("SCHEDULER_PORT", defaultRMIPortS);
		int schedulerPortI = defaultRMIPortI ;
		try {
			schedulerPortI = Integer.parseInt(schedulerPortS);
		}catch (final Exception e) {
			schedulerPortI = defaultRMIPortI ;
		}
		log.debug("Scheduler RMI port set as: " + schedulerPortI);

		final String schedulerRefName = appProps.getProperty("SCHEDULER_REFNAME", defaultSchedulerRefName);
		log.debug("Scheduler Refrence Name set as: " + schedulerRefName);
		
		rmiURL = "//" + schedulerHostName + ":" + schedulerPortI + "/" + schedulerRefName ;
		log.info("Created RMI URL for scheduler : " + rmiURL);
		log.debug("End: getSchedulerRmiUrl");
		return rmiURL;
	}
	
	/**
	 * To get the RMI URL for licmgr
	 * @return String: licmgr RMI URL
	 */
	public String getLicmgrRmiUrl(){
		log.debug("Begin: getLicmgrRmiUrl");
		String rmiURL = null ;
		String licHostName = appProps.getProperty("LICENSING_HOSTNAME");
		if(licHostName == null || licHostName.isEmpty()){
			log.debug("Licensing host name is not defined in ETLC properties file.");
			//Get from service name of engine
			try{
				log.debug("Getting Licensing host name via service name: " + licServiceName);
				licHostName = ServicenamesHelper.getServiceHost(licServiceName, defaultEniqHost);
			}catch(final Exception e){
				log.debug("Exception comes while getting Licensing host name via service name. Setting it to default hostname: " + defaultEniqHost);
				licHostName = defaultEniqHost ;
			}
		}
		log.debug("Licensing host name set as: " + licHostName);
		
		final String licPortS = appProps.getProperty("LICENSING_PORT", defaultRMIPortS);
		int licPortI = defaultRMIPortI ;
		try {
			licPortI = Integer.parseInt(licPortS);
		}catch (final Exception e) {
			licPortI = defaultRMIPortI ;
		}
		log.debug("Licensing RMI port set as: " + licPortI);

		final String licRefName = appProps.getProperty("LICENSING_REFNAME", defaultLicRefName);
		log.debug("Licensing Refrence Name set as: " + licRefName);
		
		rmiURL = "//" + licHostName + ":" + licPortI + "/" + licRefName ;
		log.info("Created RMI URL for Licensing : " + rmiURL);
		log.debug("End: getLicmgrRmiUrl");
		return rmiURL;
	}

}