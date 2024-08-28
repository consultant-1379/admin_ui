package com.distocraft.dc5000.etl.gui.systemmonitor;


import java.rmi.Naming;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.util.Helper;
import com.distocraft.dc5000.etl.scheduler.ISchedulerRMI;
import com.ericsson.eniq.common.RemoteExecutor;
import com.ericsson.eniq.repository.DBUsersGet;

/**
 * This class is mainly used by loginError.jsp file to get the status of ENIQ services in case of LDAP is down.
 * @author eanguan
 *
 */
public class ServicesStatusStore {
	private static final Log log = LogFactory.getLog(ServicesStatusStore.class);
	public static final String ONLINE = "Online" ;
	public static final String OFFLINE = "Offline" ;
	public static final String GRAY = "StatusGray";
	public static final String YELLOW = "StatusYellow";
	
	/**
	 * Function to check if Engine is online or offline
	 * @return
	 */
	private static String getEngineStatusPrivate(){
		log.info("Starts getting Engine Status");
		String engStatus = OFFLINE;
		ITransferEngineRMI transferEngineRmi;
		try{
			//String rmiURL = "//" + Helper.getEnvEntryString("rmiurl") + "/" + "TransferEngine";
			//log.info("RMI Refrence for engine = " + rmiURL);
			transferEngineRmi = (ITransferEngineRMI) Naming.lookup(RmiUrlFactory.getInstance().getEngineRmiUrl());
			final List<String> status = transferEngineRmi.status();
			engStatus = ONLINE ;
			for(String s : status){
				if(s.contains("is not running")){
					engStatus = OFFLINE ;
				}
			}
		}catch(final Exception e){
			log.error("Exception comes while getting engine status : " + e.getMessage());
			log.error(e);
			engStatus = OFFLINE ;
		}
		log.info("Engine Status fetched:  " + engStatus);
		log.info("Stops getting Engine Status");
		return engStatus ;
	}
	
	/**
	 * Function to get Engine status publicly
	 * @return
	 */
	public static String getEngineStatus(){
		return getEngineStatusPrivate();
	}
	
	/**
	 * Function to check if Scheduler is online or offline
	 * @return
	 */
	private static String getSchedulerStatusPrivate(){
		log.info("Starts getting Scheduler Status");
		String schedulerStatus = OFFLINE;
		ISchedulerRMI schedulerRmi;
		try{
			//String rmiURL = "//" + Helper.getEnvEntryString("rmiurl") + "/" + "Scheduler";
			//log.info("RMI Refrence for scheduler = " + rmiURL);
			schedulerRmi = (ISchedulerRMI) Naming.lookup(RmiUrlFactory.getInstance().getSchedulerRmiUrl());
			final List<String> status = schedulerRmi.status();
			schedulerStatus = ONLINE ;
			for(String s : status){
				if(s.contains("is not running")){
					schedulerStatus = OFFLINE ;
				}
			}
		}catch(final Exception e){
			log.error("Exception comes while getting Scheduler status : " + e.getMessage());
			log.error(e);
			schedulerStatus = OFFLINE ;
		}
		log.info("Scheduler Status fetched:  " + schedulerStatus);
		log.info("Stops getting Scheduler Status");
		return schedulerStatus ;
	}
	
	/**
	 * Function to get Scheduler status publicly
	 * @return
	 */
	public static String getSchedulerStatus(){
		return getSchedulerStatusPrivate();
	}
	
	/**
	 * Function to check if Lic Srv is online or offline
	 * @return
	 */
	private static String getLicSrvStatusPrivate(){
		String licSrvStatus = OFFLINE ;
		log.info("Starts getting License Server Status");
		try{
			SystemCommand systemCommand = new SystemCommand();
			String statusOutput = systemCommand.runCmdPlain(MonitorInformation.CMD_LICSERV_STATUS);
			if (statusOutput.contains("is online")) {
				licSrvStatus = ONLINE ;
			}else{
				licSrvStatus = OFFLINE ;
			}
		}catch(final Exception e){
			log.error("Exception comes while getting License Server status : " + e.getMessage());
			log.error(e);
			licSrvStatus = OFFLINE ;
		}
		log.info("License Server Status fetched:  " + licSrvStatus);
		log.info("Stops getting License Server Status");
		return licSrvStatus ;
	}
	
	/**
	 * Function to get Lic Srv status publicly
	 * @return
	 */
	public static String getLicSrvStatus(){
		return getLicSrvStatusPrivate();
	}
	
	/**
	 * Function to check if Lic Mgr is online or offline
	 * @return
	 */
	private static String getLicMgrStatusPrivate(){
		log.info("Starts getting License Manager Status");
		String licMgrStatus = OFFLINE ;
		try{
			SystemCommand systemCommand = new SystemCommand();
			String statusOutput = systemCommand.runCmdPlain(MonitorInformation.CMD_LICMGR_STATUS);
			if (statusOutput.contains("is running")) {
				licMgrStatus = ONLINE ;
			}else{
				licMgrStatus = OFFLINE ;
			}
		}catch(final Exception e){
			log.error("Exception comes while getting License Manger status : " + e.getMessage());
			log.error(e);
			licMgrStatus = OFFLINE ;
		}
		log.info("License Manager Status fetched:  " + licMgrStatus);
		log.info("Stops getting License Manager Status");
		return licMgrStatus ;
	}
	
	/**
	 * Function to get Lic Mgr status publicly
	 * @return
	 */
	public static String getLicMgrStatus(){
		return getLicMgrStatusPrivate();
	}
	
	
	/**
	 * Function to check if Glassfish is online or offline
	 * @return
	 */
	private static String getGlassFishStatusPrivate(){
		log.info("Starts getting Glassfish Status");
		String gfStatus = OFFLINE ;
		try{
		    final String user = MonitorInformation.DC_USER_NAME;
		    final String glassfsihcommand = Helper.getEnvEntryString(MonitorInformation.GLASSFISH_STATUS_COMMAND);
		    final String password = DBUsersGet.getMetaDatabases(MonitorInformation.DC_USER_NAME, MonitorInformation.MZ_HOST_ID).get(0).getPassword();
		    final String statusOutput = RemoteExecutor.executeComand(user, password, MonitorInformation.GLASSFISH_HOST_ID,
		          glassfsihcommand).trim();
		    if (statusOutput.contains(MonitorInformation.STATUS_RUNNING)) {
		    	gfStatus = ONLINE ;
		    }else{
		    	gfStatus = OFFLINE ;
		    }
		}catch(final Exception e){
			log.error("Exception comes while getting Glassfish status : " + e.getMessage());
			log.error(e);
			gfStatus = OFFLINE ;
		}
		log.info("Glassfish Status fetched:  " + gfStatus);
		log.info("Stops getting Glassfish Status");
		return gfStatus ;    
	}
	
	/**
	 * Function to get Glassfish status publicly
	 * @return
	 */
	public static String getGlassFishStatus(){
		return getGlassFishStatusPrivate();
	}
	
	/**
	 * Function to check if Mediation gateway is online or offline
	 * @return
	 */
	private static String getMGStatusPrivate(){
		log.info("Starts getting Mediation Gateway Status");
		String mgStatus = OFFLINE ;
		
		try {
			final String user = MonitorInformation.DC_USER_NAME;
		    final String mzcommand = Helper.getEnvEntryString(MonitorInformation.MZ_MEDIATION_STATUS_COMMAND);
		    final String password = DBUsersGet.getMetaDatabases(MonitorInformation.DC_USER_NAME, MonitorInformation.MZ_HOST_ID).get(0).getPassword();
		    String statusOutput = RemoteExecutor.executeComand(user, password, MonitorInformation.MZ_HOST_ID, mzcommand)
		          .trim();
		    
		    if (statusOutput.contains(MonitorInformation.MZ_STATUS_RUNNING_KEY)) {
		    	//Online
		    	mgStatus = ONLINE ;
		    } else {
		    	//Offline
		    	mgStatus = OFFLINE ;
		    }
		}catch(final Exception e){
			log.error("Exception comes while getting Mediation Gateway status : " + e.getMessage());
			log.error(e);
			mgStatus = OFFLINE ;
		}
		log.info("Mediation Gateway Status fetched:  " + mgStatus);
		log.info("Stops getting Mediation Gateway Status");
		return mgStatus ;
	}
	
	/**
	 * Function to get Mediation gateway status publicly
	 * @return
	 */
	public static String getMGStatus(){
		return getMGStatusPrivate() ;
	}

}
