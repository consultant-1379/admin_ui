package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.distocraft.dc5000.etl.gui.common.ENIQServiceStatusInfo;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.ericsson.eniq.licensing.cache.LicenseInformation;
import com.ericsson.eniq.licensing.cache.LicensingCache;
import com.ericsson.eniq.common.RemoteExecutor;

public class FeatureAvailability {

	final Log log = LogFactory.getLog(this.getClass());

	final static String installedFeatures = "/eniq/admin/managed_oss/total_feature_install_list";

	final static String mwsServerAddress = "/eniq/installation/config/INSTALL_SERVER";
	
	static String swLocateAddress = "/eniq/installation/config/eniq_sw_locate";
	
	final static String installFeaturesFile="/eniq/sw/conf/install_features";
	
	protected final static String fileDelimiter = "::";
	
	static String eniqTrack = null;
	
	final String conf_dir = System.getProperty("CONF_DIR", "/eniq/sw/conf");
	
	private String DCUSERPWD;
	
	private String reset_dcuserpwd;

	private File file;

	private FileReader stream;

	private BufferedReader buf;
	
	private List<String> installFeatures;
	
	//XARJSIN
	private static final File NMI_PID = new File("/var/tmp/pid_of_upgrade_process");
	private static final String DCUSER = "dcuser";
	protected static final File UPGRADE_STAGE_FILE = new File("/var/tmp/current_upgrade_feature_only_ui_stage");
	protected static final File UPGRADE_STAGE_CLEANUP_FILE = new File("/eniq/installation/core_install/etc/current_upgrade_feature_only_stage");
	protected static final File INSTALL_STAGE_FILE = new File("/var/tmp/current_add_features_ui_stage");
	protected static final File COMMIT_STAGE_FILE = new File("/var/tmp/current_post_upgrade_ui_stage");
	protected static final File COMMIT_STAGE_CLEANUP_FILE = new File("/eniq/installation/core_install/etc/current_post_upgrade_feature_only_stage");
	protected static final File ROLLBACK_STAGE_FILE = new File("/var/tmp/current_rollback_ui_stage");
	protected static final File ROLLBACK_STAGE_CLEANUP_FILE = new File("/var/tmp/current_rollback_feature_only_stage");
	protected static final File UPGRADE_STAGE_LIST = new File("/eniq/installation/core_install/etc/eniq_feature_upgrade_admin_ui_stagelist");
	protected static final File INSTALL_STAGE_LIST = new File("/eniq/installation/core_install/etc/eniq_add_features_admin_ui_stagelist");
	protected static final File COMMIT_STAGE_LIST = new File("/eniq/installation/core_install/etc/eniq_post_upgrade_admin_ui_stagelist");
	protected static final File ROLLBACK_STAGE_LIST = new File("/eniq/installation/core_install/etc/eniq_rollback_admin_ui_stagelist");
	
	/**
	 *  Method to display eniq oss
	 * 
	 * @return FeatureName for each cxcsss
	 */
	 List<String> getOSSAlias() throws FileNotFoundException {
		List<String> ossList = new ArrayList<String>();

		file = new File(conf_dir + File.separator + ".oss_ref_name_file");

		stream = new FileReader(file);

		buf = new BufferedReader(stream);
		
		String strLine = "";

		try {

			while ((strLine = buf.readLine()) != null) {

				String[] line = strLine.split(" ");
	
				ossList.add(line[0]);
			}
		} catch (IOException e) {

			log.warn("IOException while obtaining the OSS Alias " + e.getMessage());
		}catch (Exception e) {

			log.warn("OSS Alias could not be obtained due to  " + e.getMessage());
		}
		finally{
			if(buf != null){
				try {
					buf.close();
				} catch (IOException e) {
					log.warn("Error in closing BufferedReader" + e.getMessage());
				}
			}
			if(stream != null){
				try {
					stream.close();
				} catch (IOException e) {
					log.warn("Error in closing FileReader" + e.getMessage());
				}
			}
		}
		return ossList;
	}

	/**
	 * Returns a list containing all the installed features on the ENIQ server
	 * 
	 * @return list of features
	 */
	 Map<String, String> installedFeatures() {

		Map<String,String> featureMap = new HashMap<String,String>();
		try {
			file = new File(installedFeatures);
			stream = new FileReader(file);
			buf = new BufferedReader(stream);
			String strLine = "";
			while ((strLine = buf.readLine()) != null) {

				String[] line = strLine.split(fileDelimiter);
				//Add the CXC number and Feature Description into the Map
				featureMap.put(line[0],line[1]);
			}
		}catch (IOException e) {

			e.printStackTrace();
		}finally{
			if(buf != null){
				try {
					buf.close();
				} catch (IOException e) {
					log.warn("Error in closing BufferedReader" + e.getMessage());
				}
			}
			if(stream != null){
				try {
					stream.close();
				} catch (IOException e) {
					log.warn("Error in closing FileReader" + e.getMessage());
				}
			}
		}
		return featureMap;
	}
	 /**
		 * This method provides all the features that needs updation
		 * 
		 * @return list of licensed features
		 * @throws FileNotFoundException 
		 */
		 protected Map<String,String> getFeaturesToBeUpdated() throws FileNotFoundException {
			 Map<String,String> featureMap = installedFeatures();
				
				Map<String,String> licensedMap = getLicensedFeatures();
			//Retain the contents of featMap from the licensedMap
				featureMap.keySet().retainAll(licensedMap.keySet());
			return featureMap;
		} 
	/**
	 * This method provides all the features that needs installation
	 * 
	 * @return list of licensed features
	 * @throws FileNotFoundException 
	 */
	 protected Map<String,String> getFeaturesToBeInstalled() throws FileNotFoundException {
		 Map<String,String> featureMap = installedFeatures();
			
			Map<String,String> licensedMap = getLicensedFeatures();
		//Remove the contents of featMap from the licensedFeaturesMap
			//licensedMap.entrySet().removeAll(featureMap.entrySet());
		//This Map should now only contain the features that are licensed but not installed
			Iterator<String> iterator = licensedMap.keySet().iterator();
			while(iterator.hasNext()){
				String nextKey = iterator.next();
				if(featureMap.containsKey(nextKey)){
					iterator.remove();
				}
			}
		return licensedMap;
	}
	 
	 /**
		 * This method interfaces with licensing cache through RMI
		 * 
		 * @return list of licensed features
		 */
	protected Map<String,String> getLicensedFeatures() {
			LicensingCache cache = null;

			try {
				// contact the registry and get the cache instance.
				cache = (LicensingCache) Naming.lookup(RmiUrlFactory.getInstance().getLicmgrRmiUrl());

			} catch (Exception e) {
				log.error("Failed to get info from license manager. Exception is " + e.getMessage());
			}

			if (cache == null) {
				log.error(
						"Could not get licenseinformation from license manager. Please check that license manager process is running.");
				return new HashMap<String,String>();
			}
			Vector<LicenseInformation> licInfo = null;

			try {
				licInfo = cache.getLicenseInformation();
			} catch (Exception e) {
				log.error("Failed to get licenses from license manager. Exception is " + e.getMessage());
			}

			if (licInfo == null) {
				log.error("Returned licenses were null. Failed to get license information.");
				return new HashMap<String,String>();
			}
			Map<String, String> licensedFeaturesMap =new HashMap<String,String>();
			for (int i = 0; i < licInfo.size(); i++) {

				String cxc = licInfo.get(i).getFeatureName();
				String FeatureDescription = licInfo.get(i).getDescription();
				//Get the description of each feature using install_features
				//Add the CXC number and Feature Description into the Map
				//EQEV-46180
				//file = new File(installFeaturesFile);
				//String strLine = "";
				//try {
					//stream = new FileReader(file);
					//buf = new BufferedReader(stream);
					//while ((strLine = buf.readLine()) != null) {
						//if(strLine.contains(cxc)){
							//String FeatureDescription = strLine.substring(strLine.indexOf("::")+2,strLine.lastIndexOf("::"));
							licensedFeaturesMap.put(cxc,FeatureDescription);
						/*}
					}
				} catch (IOException e) {
					log.warn("IOException while reading feature names " + e.getMessage());
				}catch (Exception e) {
					log.warn("Exception while reading feature names" + e.getMessage());
				}
				finally{
					if(buf != null){
						try {
							buf.close();
						} catch (IOException e) {
							log.warn("Error in closing BufferedReader" + e.getMessage());
						}
					}
					if(stream != null){
						try {
							stream.close();
						} catch (IOException e) {
							log.warn("Error in closing FileReader" + e.getMessage());
						}
					}
				}*/
			}

			return licensedFeaturesMap;
		}

	/**
	 * Returns default MWS Path from which feature upgrade needs to be done
	 * 
	 * @return string
	 */
	 public String getDefaultMWSPath() {

			file = new File(swLocateAddress);

			try {
				String defaultPath = "";
				
				stream = new FileReader(file);

				buf = new BufferedReader(stream);
				
				String line = "";
				
				while ((line = buf.readLine()) != null) {
					
					String str[] = line.split("/");
					
					String mwsServerIP = str[0].substring(0, (str[0].length() - 1));
					String cmd = "ls -1t /net/"+mwsServerIP+"/JUMP/ENIQ_STATS/ENIQ_STATS/ | grep Features_ | head -1 ";
					DCUSERPWD = ConfirmFeatures.getInst().getDcuser_pwd();
				 	String featpath = RemoteExecutor.executeComand(DCUSER, DCUSERPWD, UpdateFeatures.HOST_ADD, cmd );
					defaultPath = "/net/" + mwsServerIP + "/JUMP/ENIQ_STATS/ENIQ_STATS/" + featpath.trim();		
				}
				return defaultPath;

			} catch (FileNotFoundException e) {
				log.warn("File at " + swLocateAddress + "not found " + e.getMessage() );
				return null;
			} catch (IOException e) {
				log.warn("Unable to read "+ swLocateAddress + e.getMessage() );
				return null;
			} catch (Exception e){
				log.warn("Exception while determining the track " + e.getMessage());
				return null;
			}
			
		}
	
	 public String getDefaultMWSPathWithRelease() {
		 return getDefaultMWSPath();
	 }
	 
		//XARJSIN
		protected List<String> parseFeatList(List<String> featList) {
			log.debug("Raw list - " + featList);
			List<String> featListArranged = new ArrayList<String>();
			Comparator<String> sortPriority = new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					int n1 = Integer.parseInt(o1.substring(o1.lastIndexOf(":") + 1));
					int n2 = Integer.parseInt(o2.substring(o2.lastIndexOf(":") + 1));
					return (n1 - n2);
				}
			};
			Collections.sort(featList, sortPriority );
			log.debug("Sorted list - " + featList);
			for(String feat : featList){
				feat = feat.substring(0, feat.lastIndexOf(":") + 1);
				featListArranged.add(feat);
			}
			return featListArranged;
		}

		public boolean checkPIDAlive(long pid) throws Exception {
			try{
			//String DCUSERPWD = getPwd(DCUSER,UpdateFeatures.HOST_ADD);
			if(ConfirmFeatures.getInst().getDcuser_pwd().isEmpty()){
				DCUSERPWD = " ";
			}
			else{
				DCUSERPWD = ConfirmFeatures.getInst().getDcuser_pwd();
			}
			if(RemoteExecutor.validatePwd(DCUSER,DCUSERPWD,UpdateFeatures.HOST_ADD)){
				String PID_ALIVE = "/usr/bin/ps -p " + pid + " | grep -v CMD";
				String result = RemoteExecutor.executeComand(DCUSER, DCUSERPWD, UpdateFeatures.HOST_ADD, PID_ALIVE );	
				if(result.equalsIgnoreCase("") || result == null){
					return false;
				}
				return true;
			} else {	
				Thread.sleep(getRefreshPeriod());
				if(ENIQServiceStatusInfo.isRepDBOnline()){	
					FeatureInformation obj = new FeatureInformation();
					List<String> pwdlist = new ArrayList<String>();
					pwdlist = obj.pwdDbConnect();
					
					if(!pwdlist.isEmpty()){
						String curr_pwd = pwdlist.get(0);
						
					    ConfirmFeatures.getInst().setDcuser_pwd(curr_pwd);
					    
					    reset_dcuserpwd = ConfirmFeatures.getInst().getDcuser_pwd();
					   
					    if(RemoteExecutor.validatePwd(DCUSER,reset_dcuserpwd,UpdateFeatures.HOST_ADD)){
					    	String PID_ALIVE = "/usr/bin/ps -p " + pid + " | grep -v CMD";
					    	
					    	String result = RemoteExecutor.executeComand(DCUSER,reset_dcuserpwd , UpdateFeatures.HOST_ADD, PID_ALIVE );
					    	
					    	if(result.equalsIgnoreCase("") || result == null){
							   return false;
					    	}
					    	return true;
					    }
					}
				} else{
					log.warn("Repdb is not online.Could not run PID_ALIVE command with updated dcuser password");
				}
			}
			} catch(Exception e){
				throw new Exception("Could not find an entry in repdb! (was is added?)");
			}
			return true;						
		}
		

		public List<String> readFeatFromFile(String action) {
			if(action.equalsIgnoreCase("install")){
				return getFeatFromFile(new File(InstallFeatures.BACKUP_FEAT_LIST), true);
			}
			else if(action.equalsIgnoreCase("installsuccess")){
				return getFeatFromFile(new File(InstallFeatures.BACKUP_FEAT_LIST), true);
			}
			else if(action.equalsIgnoreCase("upgrade")){
				return getFeatFromFile(new File(UpdateFeatures.BACKUP_FEAT_LIST), true);
			}
			else if(action.equalsIgnoreCase("success")){
				return getFeatFromFile(new File(UpdateFeatures.BACKUP_FEAT_LIST), true);
			}
			return null;

}
		
		/**
		 * Extracts feature name from upgrade file to list 
		 * @param featureName 
		 * @throws Exception 
		 * 
		 */
		public List<String> getFeatFromFile(File installFile, boolean featureName){

			List<String> instList = new ArrayList<String>();
			BufferedReader instL;
			try {
				instL = new BufferedReader(new InputStreamReader(new FileInputStream(installFile)));
				String str3;
		    	while((str3 = instL.readLine()) != null)
		    	{
		    		if(featureName){
		    			str3 = str3.substring(str3.indexOf("::") + 2, str3.lastIndexOf("::"));
		    		}
		    		instList.add(str3);
		    	}
		    	instL.close();
			} catch (FileNotFoundException e) {
				log.warn("Could not open file for list of features to be updated");
			} catch (IOException e) {
				log.warn("Could not read file");
			}
			return instList;
		}

		public long getPIDFromFile(String action) {
			BufferedReader stage;
			long result = 0;
			try {
				if(action.equalsIgnoreCase("upgrade")){
					stage = new BufferedReader(new InputStreamReader(new FileInputStream(UpdateFeatures.PID_FLAG)));
				}
				else if(action.equalsIgnoreCase("commit")){
					stage = new BufferedReader(new InputStreamReader(new FileInputStream(UpdateFeatures.COMMIT_FLAG)));
				}
				else if(action.equalsIgnoreCase("rollback")){
					stage = new BufferedReader(new InputStreamReader(new FileInputStream(UpdateFeatures.ROLLBACK_FLAG)));
				}
				else if(action.equalsIgnoreCase("install")){
					stage = new BufferedReader(new InputStreamReader(new FileInputStream(InstallFeatures.PID_FLAG)));
				}
				else if(action.equalsIgnoreCase("rollback_install")){
					stage = new BufferedReader(new InputStreamReader(new FileInputStream(InstallFeatures.ROLLBACK_FLAG)));
				}
				else if(action.equalsIgnoreCase("commit_install")){
					stage = new BufferedReader(new InputStreamReader(new FileInputStream(InstallFeatures.COMMIT_FLAG)));
				}
				/*else if(action.equalsIgnoreCase("precheck")){
					stage = new BufferedReader(new InputStreamReader(new FileInputStream(Precheck.PRECHECK_FLAG)));
				}*/
				else{
					stage = new BufferedReader(new InputStreamReader(new FileInputStream(NMI_PID)));
				}
				result = Long.parseLong(stage.readLine());
				stage.close();
			} catch (FileNotFoundException e) {
				log.warn("PID file does not exist, will trigger script");
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}

		public String getStageFromFile(String action) {
			BufferedReader stage;
			String result = null;
			File STAGE = null;
			if(action.equalsIgnoreCase("upgrade_cleanup"))
			{
				STAGE = UPGRADE_STAGE_CLEANUP_FILE;
			}
			else if(action.equalsIgnoreCase("upgrade")){
				STAGE = UPGRADE_STAGE_FILE;
			}
			else if(action.equalsIgnoreCase("install")){
				STAGE = INSTALL_STAGE_FILE;
			}
			else if(action.equalsIgnoreCase("commit_cleanup")){
				STAGE = COMMIT_STAGE_CLEANUP_FILE;
			}
			else if(action.equalsIgnoreCase("commit")){
				STAGE = COMMIT_STAGE_FILE;
			}
			else if(action.equalsIgnoreCase("rollback_cleanup")){
				STAGE = ROLLBACK_STAGE_CLEANUP_FILE;
			}
			else if(action.equalsIgnoreCase("rollback")){
				STAGE = ROLLBACK_STAGE_FILE;
			}
			try {
				stage = new BufferedReader(new InputStreamReader(new FileInputStream(STAGE)));
				result = stage.readLine();
				if(result.startsWith("#")){
					result = stage.readLine();
				}
		       	stage.close();
			} catch (FileNotFoundException e) {
				log.debug("File not found" + e);
				result = "nofile";
			} catch (NumberFormatException e) {
				log.warn("Format exception" + e);
			} catch (IOException e) {
				log.warn("IO Exception" + e);
			}
			catch(NullPointerException e)
			{
				log.debug("File is empty" + e);
				result = "nofile";
			}
			return result;	
		}

		public List<String> readFromFile(String action) {
			if(action.equalsIgnoreCase("upgrade")){
				return getFeatFromFile(new File(UpdateFeatures.BACKUP_FEAT_LIST), false);
			}
			else if(action.equalsIgnoreCase("success")){
				return getFeatFromFile(new File(UpdateFeatures.BACKUP_FEAT_LIST), false);
			}
			else if(action.equalsIgnoreCase("install")){
				return getFeatFromFile(new File(InstallFeatures.BACKUP_FEAT_LIST), false);
			}
			else if(action.equalsIgnoreCase("installsuccess")){
				return getFeatFromFile(new File(InstallFeatures.BACKUP_FEAT_LIST), false);
			}
			return null;
		}
		
		public HashMap<String, String> getStageStatus(String currentStage, List<String> stages) {
			HashMap<String, String> stageResult = new HashMap<>();
			int index = stages.indexOf(currentStage);
	    	for(String eachStage : stages){
	    		int thisStage = stages.indexOf(eachStage);
	    		if(index == -1 || currentStage.equalsIgnoreCase("nofile")){
	    			if(thisStage == 0){
	    				stageResult.put(eachStage, "IN PROGRESS");
	    			}
	    			else{
	    				stageResult.put(eachStage, "QUEUED");
	    			}
	    		}
	    		else{
	    			if(index > thisStage){
		    			stageResult.put(eachStage, "COMPLETED");
		    		}
		    		else if(index == thisStage){
		    			stageResult.put(eachStage, "IN PROGRESS");
		    		}
		    		else if (index < thisStage){
		    			stageResult.put(eachStage, "QUEUED");
		    		}
	    		}
	    	}
			return stageResult;
		}

		public List<String> getStageListFromFile(String action) {
			File stageList = null;
			List<String> stages = new ArrayList<>();
			BufferedReader stageReader;
			if(action.equalsIgnoreCase("install")){
				stageList = INSTALL_STAGE_LIST;
			}
			else if(action.equalsIgnoreCase("upgrade")){
				stageList = UPGRADE_STAGE_LIST;
			}
			else if(action.equalsIgnoreCase("commit")){
				stageList = COMMIT_STAGE_LIST;
			}
			else if(action.equalsIgnoreCase("rollback")){
				stageList = ROLLBACK_STAGE_LIST;
			}
			try {
				stageReader = new BufferedReader(new InputStreamReader(new FileInputStream(stageList)));
				String str3;
		    	while((str3 = stageReader.readLine()) != null)
		    	{
		    		stages.add(str3);
		    	}
		    	stageReader.close();
			} catch (FileNotFoundException e) {
				log.warn("Could not open stagelist file");
			} catch (IOException e) {
				log.warn("Could not read file");
			}
			return stages;
		}

		protected long getRefreshPeriod() {
			final Properties ffuProps = new Properties();
			try{
				final File ffuProp = new File("/eniq/sw/runtime/tomcat/webapps/adminui/conf/FFU.properties");
				ffuProps.load(new FileInputStream(ffuProp));
				return Long.parseLong(ffuProps.getProperty("REFRESH_PERIOD", "10000"));
			}
			catch(Exception e){
				return 10000;
			}
		}

		public HashMap<String, String> getStageStatus(String currentStage, List<String> stageList, int scenario) {
			HashMap<String, String> stageResult = getStageStatus(currentStage, stageList);
			if(scenario == 0){
				log.debug("All stages completed successfully");
				stageResult.put(currentStage, "COMPLETED");
			}
			else if(scenario == 1){
				log.debug("Failed at feature update");
				stageResult.put(currentStage, "FAILED");
			}
			else if(scenario == 2){
				log.debug("Failed at first stage");
				stageResult.put(stageList.get(0), "FAILED");
			}
			return stageResult;
		}

		protected HashMap<String, String> getSnapFromFile() {
			// TODO Auto-generated method stub
			return null;
		}
		
		protected boolean checkValidSubmit() throws Exception {
			if(UpdateFeatures.PID_FLAG.exists()){
				if(checkPIDAlive(getPIDFromFile("upgrade"))){
					return false;
				}
			}
			if(UpdateFeatures.COMMIT_FLAG.exists()){
				if(checkPIDAlive(getPIDFromFile("commit"))){
					return false;
				}
			}
			if(UpdateFeatures.ROLLBACK_FLAG.exists()){
				if(checkPIDAlive(getPIDFromFile("rollback"))){
					return false;
				}
			}
			if(InstallFeatures.PID_FLAG.exists()){
				if(checkPIDAlive(getPIDFromFile("install"))){
					return false;
				}
			}
			if(InstallFeatures.COMMIT_FLAG.exists()){
				if(checkPIDAlive(getPIDFromFile("commit_install"))){
					return false;
				}
			}
			if(InstallFeatures.ROLLBACK_FLAG.exists()){
				if(checkPIDAlive(getPIDFromFile("rollback_install"))){
					return false;
				}
			}
			return true;
		}
	public List<List<String>> getLockingInfo(File lockInfoFile) throws IOException{
		if(!lockInfoFile.exists())
			return null;
		BufferedReader br = null;
		List<List<String>> userLockFile = null;
		try{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(lockInfoFile)));
			
			userLockFile = new ArrayList<>();
			ArrayList<String> userlockdetail;
			String line;
			while((line = br.readLine()) != null)
			{
				if(line.isEmpty())
				{
					continue;
				}
				String[] data = line.split(" ");
				userlockdetail = new ArrayList<>();
				userlockdetail.add(data[0]);
				userlockdetail.add(data[1]);
				if(data.length==4)
				{
					userlockdetail.add(data[2]);
					userlockdetail.add(data[3]);
				}
	
				userLockFile.add(userlockdetail);
			}
		}
		catch(Exception e)
		{
			log.error("Exception Occurred :"+e);
			return null;
		}
		br.close();
		return userLockFile;
	}
}
