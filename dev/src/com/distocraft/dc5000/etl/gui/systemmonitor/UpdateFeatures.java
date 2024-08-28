package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.CommandRunner;
import com.distocraft.dc5000.etl.gui.common.FfuEtlGuiServlet;
import com.ericsson.eniq.common.DatabaseConnections;
import com.ericsson.eniq.common.RemoteExecutor;
import com.jcraft.jsch.JSchException;

/**
 * 
 * 
 * @author xarjsin/xdivykn
 *
 */
public class UpdateFeatures extends FfuEtlGuiServlet {

	//private String mwsServerIP = "";
	
	private String upgradePath = "";
	
	private String defaultPath = "";
	
	private String upgradeResult = "";
		
	private Connection dwhConn;
	
	private Connection repConn;
	
	private FileReader stream;

	private BufferedReader buf;
	
	private boolean empty = false;

	private static boolean back_param = false;
	
	private Map<String, String> featMap = new HashMap<String,String>();
	
	final Log log = LogFactory.getLog(this.getClass());
	
	Map<String, Map<String, Map<String, String>>> installedFeatures;
	
	private static final String FEATURE_AVAILABILITY_UPDATE_TEMPLATE = "feature_availability_update.vm";
	private static final String CMD = "sudo /usr/bin/chmod 777 ";
	private FeatureAvailability featureAvailability = new FeatureAvailability();
	
	//XARJSIN
	//Files to write list of features
	private static final String GIVE_WRITE_PATH = "sudo /usr/bin/chmod 770 /eniq/home/dcuser";
	private static final String TAKE_WRITE_PATH = "sudo /usr/bin/chmod 750 /eniq/home/dcuser";
	protected static final String FEATURES_FOR_UPDATE = "/eniq/installation/core_install/etc/features_to_be_managed";
	protected static final String BACKUP_FEAT_LIST = "/var/tmp/bkupFeatList";	//backup file because features file deleted on completion
	protected static final String SNAP_LIST = "/var/tmp/rollback_conf_adminUI";
	private static final String UPGRADE_PATH = "/var/tmp/FFU_upgrade_path_file";
	//CHMOD commands to give permissions as required by FFU
	private static final String GIVE_WRITE = CMD + FEATURES_FOR_UPDATE;
	private static final String GIVE_EXECUTE = "sudo /usr/bin/chmod 777 /eniq/installation/core_install/bin/upgrade_eniq_sw.bsh";
	private static final String GIVE_WRITE_DIR = CMD + FEATURES_FOR_UPDATE.substring(0, FEATURES_FOR_UPDATE.lastIndexOf("/"));
	private static final String GIVE_WRITE_RLBK = CMD + SNAP_LIST;
	private static final String TAKE_WRITE = "sudo /usr/bin/chmod 644 " + FEATURES_FOR_UPDATE;
	private static final String TAKE_EXECUTE = "sudo /usr/bin/chmod 644 /eniq/installation/core_install/bin/upgrade_eniq_sw.bsh";
	private static final String TAKE_WRITE_DIR = "sudo /usr/bin/chmod 755 " + FEATURES_FOR_UPDATE.substring(0, FEATURES_FOR_UPDATE.lastIndexOf("/"));
	private static final String TAKE_WRITE_RLBK = "sudo /usr/bin/chmod 644 " + SNAP_LIST;
	//Commands to run on RemoteExecutor
	private static final String CLEAN_STAGE_UPGRADE = "/usr/bin/rm /eniq/installation/core_install/etc/current_upgrade_feature_only_stage";
	private static final String LIST_SNAP = "/bin/bash /eniq/bkup_sw/bin/prep_eniq_snapshots.bsh -u -N > /eniq/home/dcuser/snapList";
	private static final String LIST_SNAP_ADMIN = "sudo " + LIST_SNAP;
	//Constants used in execution
	private static final String ROOT_USER = "root";
	protected static final String HOST_ADD = "webserver";
	private static final String ADMIN_ROLE_EXIST_CMD = "cat /etc/group | grep ENIQ_ADMIN_ROLE";
	//Flags and stage files
	protected static final File PID_FLAG = new File("/var/tmp/FFU_FLAG_PID");
	protected static final File COMMIT_FLAG = new File("/var/tmp/FFU_COMMIT_PID");
	protected static final File ROLLBACK_FLAG = new File("/var/tmp/FFU_ROLLBACK_PID");
	//private static final File FEAT_INSTALLED_LIST = new File("/tmp/featInstall");
	private static final File TP_STAGE = new File("/eniq/sw/installer/tp_stage_file");
	private static final File UPGRADE_PATH_FILE = new File(UPGRADE_PATH);
	private static final File FEAT_INSTALLED_LIST = new File("/var/tmp/FVM_FeatProgress");
	private static final File COMMIT_SUCCESS_FLAG =  new File("/var/tmp/COMMIT_SUCCESSFUL");
	private static final File ROLLBACK_SUCCESS_FLAG =  new File("/var/tmp/ROLLBACK_SUCCESSFUL");
	private static final File LOCK_INFO_FILE =  new File("/eniq/local_logs/UserLock.txt");
	//Variables used in execution
	private long REFRESH_PERIOD;
	private static boolean rollbackOngoing;
	private static boolean inProgress;
	private static boolean initiated;
	private static boolean commitOngoing;
	private static boolean resumed;
	
	@Override
	public synchronized Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		//response.setHeader("Cache-Control","private");
		if(InstallFeatures.PID_FLAG.exists() || InstallFeatures.ROLLBACK_FLAG.exists() || InstallFeatures.COMMIT_FLAG.exists())
		{
			ctx.put("install_alert", true);
		}
		HttpSession session = request.getSession(false);
		
		String featurePath = request.getParameter("featurepath");
		
		//mwsServerIP = featureAvailability.getMWSIP();
		
		//XARJSIN
		//Button response
		String update = null;
		String rootPwd = null;
		//String updateMore = null;
		String rollBack = null;
		String resume = null;
		String commit = null;
		String submit = null;
		String cancel = null;
		String continue_response = null;
		String back = null;
		String userName = null;
		String deleteFileCmd = "sudo /bin/bash /eniq/sw/installer/cleanup_FFU.bsh -delete "; //Note:DO NOT remove space after delete argument
		String runUpgradeCmd = "sudo /bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -u " + getUpgradePath();
		String runCommitCmd = "sudo /bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -c";
		String runRollBackCmd = "sudo /bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -r";
		
		
		if(null != request.getParameter("userName"))
			 userName = request.getParameter("userName");
		 else
			 userName = ROOT_USER;
		
		ctx.put("isAdminExist", isAdminExist());
		//Added check as part of EQEV-111845
		if(userName.equals(ROOT_USER)) {
			
			deleteFileCmd = "/bin/bash /eniq/sw/installer/cleanup_FFU.bsh -delete ";
			runUpgradeCmd = "/bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -u " + getUpgradePath();
			runCommitCmd = "/bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -c";
			runRollBackCmd = "/bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -r";
		}
		
		if(featureAvailability.checkValidSubmit()){
			update = StringEscapeUtils.escapeHtml(request.getParameter("Update"));
			rootPwd = request.getParameter("rootPassword");
			if(rootPwd != null && !rootPwd.isEmpty()) {
				ConfirmFeatures.getInst().setRoot_pwd(rootPwd);
			}
			//updateMore = StringEscapeUtils.escapeHtml(request.getParameter("UpdateMore"));
			if(!ROLLBACK_SUCCESS_FLAG.exists()){
			rollBack = StringEscapeUtils.escapeHtml(request.getParameter("Rollback"));
			}
			resume = StringEscapeUtils.escapeHtml(request.getParameter("Resume"));
			 if(!COMMIT_SUCCESS_FLAG.exists())
			 {
			 commit = StringEscapeUtils.escapeHtml(request.getParameter("Commit"));
			 }
			submit = StringEscapeUtils.escapeHtml(request.getParameter("submitPath"));
			cancel = StringEscapeUtils.escapeHtml(request.getParameter("Cancel"));
			continue_response = StringEscapeUtils.escapeHtml(request.getParameter("Continue"));
			back = StringEscapeUtils.escapeHtml(request.getParameter("back"));
		}

		//Variables used in flow
		//TODO user defined refresh period
		try{
			REFRESH_PERIOD = featureAvailability.getRefreshPeriod();
		}
		catch(Exception e){
			REFRESH_PERIOD = 10000;
		}
		log.debug("Refresh period is - " + REFRESH_PERIOD);
		
		if(submit != null){
			back_param=false;
		}
	
		
		//Back button on confirmation screen
		 
		if(back != null){
			
			back_param = true;
			ctx.put("back_param", true);
			log.debug("Discarding selected features and reselecting new features for update");
			
			List<String> final_back_list = ConfirmFeatures.getInst().getFinal_backlist();
			Map<String, Map<String, Map<String, String>>> installed_feat = ConfirmFeatures.getInst().getInstalled_features();
			
			List<String> selected_feat_remove = new ArrayList<String>();
			Map<String, Map<String, String>> installed_feat_value = new HashMap<String, Map<String,String>>();
			Map<String, Map<String, Map<String, String>>>  installed_feat_selected_add = new HashMap<String, Map<String,Map<String,String>>>();
			Map<String, Map<String, Map<String, String>>>  installed_feat_selected_remove = new HashMap<String, Map<String,Map<String,String>>>();
			
			 
			//installed_feat map contains original set of features, final_back_list list contains first set of features selected   
			for(String inst_feat:installed_feat.keySet()){
				for(String back_list_final:final_back_list){	
					
					int index = back_list_final.indexOf("::");
					String back_final_list_feat = back_list_final.substring(0, index);
					
					if(inst_feat.equalsIgnoreCase(back_final_list_feat)) {
						
						String modified_key=back_list_final+"::C";	
						 
						//Adding only modified selected features to installed_feat_selected_add map
						installed_feat_value = installed_feat.get(inst_feat);
						installed_feat_selected_add.put(modified_key, installed_feat_value); 
						
						// Adding only selected features to selected_feat_remove map
						selected_feat_remove.add(inst_feat);
						installed_feat_selected_remove.put(inst_feat, installed_feat_value);
						
					}
				}
			}
		
			//selected features are removed from installed_feat map
			installed_feat.keySet().removeAll(selected_feat_remove); 
			
			ConfirmFeatures.getInst().setInstalled_feat_selected_add_size(installed_feat_selected_add);
			
			//modified selected features are added to installed_feat map
			installed_feat.putAll(installed_feat_selected_add);
			
			//Here, installed_feat contains the final map of features(to be displayed on clicking back)
			ConfirmFeatures.getInst().setNew_feat_list(installed_feat);
			
			
			ctx.put("installed_featurelist",installed_feat);
			upgradeResult = "NO UPGRADE";
			ctx.put("UPGRADE_RESULT", upgradeResult);
		}
				
		List<String> listFeat = new ArrayList<String>();
		HashMap<String, String> featStatus = new HashMap<String, String> ();
		//clean flags when cancel is clicked
				if(cancel != null){
					List<String> ossAlias = featureAvailability.getOSSAlias();
					log.debug("Cleaning up as per user entry");
					try {
						if (PID_FLAG.exists()) {
							try {
								Files.delete(Paths.get(PID_FLAG.getAbsolutePath()));
							} catch (Exception e) {
								log.warn("Cannot delete Upgrade Flag File");
							}
						}
						if (COMMIT_FLAG.exists()) {
							try {
								Files.delete(Paths.get(COMMIT_FLAG.getAbsolutePath()));
							} catch (Exception e) {
								log.warn("Cannot delete Commit Flag File");
							}
						}
						if (ROLLBACK_FLAG.exists()) {
							try {
								Files.delete(Paths.get(ROLLBACK_FLAG.getAbsolutePath()));
							} catch (Exception e) {
								log.warn("Cannot delete Rollback Flag File");
							}
						}
						File FEATURES_FOR_UPDATE = new File(UpdateFeatures.FEATURES_FOR_UPDATE);
						if (FEATURES_FOR_UPDATE.exists()) {
							String root_pwd = ConfirmFeatures.getInst().getRoot_pwd();
							if (RemoteExecutor.validatePwd(userName, root_pwd, HOST_ADD)) {	
								String deleteFeaturesForUpdate = deleteFileCmd + UpdateFeatures.FEATURES_FOR_UPDATE;
								RemoteExecutor.executeComand(userName, root_pwd, HOST_ADD, deleteFeaturesForUpdate);
							} else{
								log.warn("Cannot delete FEATURES_FOR_UPDATE File");
							}
						}
						for (String oss : ossAlias) {
							File FEATURES_INSTALL = new File(InstallFeatures.FEATURES_FOR_INSTALL + oss);
							if (FEATURES_INSTALL.exists()) {
								String root_pwd = ConfirmFeatures.getInst().getRoot_pwd();
									if (RemoteExecutor.validatePwd(userName, root_pwd, HOST_ADD)) {
										String deleteFeaturesForInstall = deleteFileCmd + InstallFeatures.FEATURES_FOR_INSTALL + oss;
										RemoteExecutor.executeComand(userName, root_pwd, HOST_ADD, deleteFeaturesForInstall);
									} else{
										log.warn("Cannot delete FEATURES_FOR_INSTALL File");
									}
							}
						}
						
							if(inProgress){
								inProgress=false;
							}
						}
						catch(Exception e){
							log.warn("Could not cancel upgrade" + e);
						}
					cancel = null;	
				}
		String upgradeResult = "NO UPGRADE";
		/*if(updateMore != null){
			log.info("Continuing without commit or rollback");
			if(PID_FLAG.exists()){
				try
				{
					Files.delete(Paths.get(PID_FLAG.getAbsolutePath()));
				}catch(Exception e)
				{
					log.warn("Cannot delete Upgrade Flag File");
				}
			}
			inProgress = false;
			initiated = false;
		}*/
		
		// confirmation screen
		final Map<String, String> params = new HashMap<String, String>();

		final Enumeration<?> parameters = request.getParameterNames();

		while (parameters.hasMoreElements()) {
			final String par = (String) parameters.nextElement();
			params.put(par, request.getParameter(par));
		}
		
		List<String> finalList = new ArrayList<String>();
		List<String> final_back_list = new ArrayList<String>();
		List<String> featList = new ArrayList<String>();
		List<String> final_featlist = new ArrayList<String>();
		List<String> listFeatures = new ArrayList<String>();
				
		if(continue_response != null) {
			if(InstallFeatures.PID_FLAG.exists() || InstallFeatures.ROLLBACK_FLAG.exists() || InstallFeatures.COMMIT_FLAG.exists())
			{
				ctx.put("install_alert", true);
			}
			else if(params.size() > 2) {
				featList = Arrays.asList(request.getParameterValues("featurechecked"));
				for(String feat:featList) {
					if(feat.contains("::C")) {
						int last_index = feat.indexOf("::C");
						String str2 = feat.substring(0, last_index);
						final_featlist.add(str2);
					} else {
						final_featlist.add(feat);
					}
				}
				
				listFeatures = featureAvailability.parseFeatList(final_featlist);
				ConfirmFeatures.getInst().setFeatures(final_featlist);
				
				if(listFeatures!= null) {
					
					for (String s : listFeatures) {
						int start_index = s.indexOf("::", 0);
						int second_index = s.indexOf("::", start_index+1);
						String final_feature = s.substring(start_index+2, second_index);
						finalList.add(final_feature);
						int feat_index = listFeatures.indexOf(s)+1;
						String final_feature_back_list = final_feature+"::"+feat_index;
						final_back_list.add(final_feature_back_list);
					}
					
					upgradeResult="CONFIRM UPDATE";
					ctx.put("UPGRADE_RESULT",upgradeResult);
					
					//final_back_list list contains feature name::update priority
					ConfirmFeatures.getInst().setFinal_backlist(final_back_list);  
					ctx.put("FEATURES_LIST",finalList);
					
				}
			} else {
				upgradeResult="NO UPGRADE";
				ctx.put("UPGRADE_RESULT",upgradeResult);
				ctx.put("empty_feat", true);
			}
		}
		
		//first page
				if(!inProgress){
					try{
						dwhConn = DatabaseConnections.getDwhDBConnection().getConnection();
						repConn = DatabaseConnections.getDwhRepConnection().getConnection();
						featMap = featureAvailability.getFeaturesToBeUpdated();
						FeatureInformation finfo = new FeatureInformation(dwhConn,repConn,false);
						constructDefaultPath(finfo,featMap);
						//Gets called only when the user has configured the software path
						if(featurePath != null && !featurePath.isEmpty()){
							if(!featurePath.isEmpty()){
								//Get the data structure if MWS path is accessible
								try{
									installedFeatures = finfo.getFeatureInformation(featurePath, featMap);
									writeUserPath(featurePath);
									ConfirmFeatures.getInst().setConfigured_path(featurePath);
									setUpgradePath(featurePath);
								}catch(FileNotFoundException e){
									log.warn("Entered path not found. Considering the default path " + getUpgradePath());
									ConfirmFeatures.getInst().setConfigured_path(ConfirmFeatures.getInst().getDefault_path());
									if(!defaultPath.equals("NA")){
										ctx.put("undetermined_path", true);
										empty = false;
									}
									else{
										ctx.put("undeterminedPathWithNoDefault", true);
										if(UPGRADE_PATH_FILE.exists()){
											readUpgradePath();
											empty = false;
										}else{
											empty = true;
										}
										if(!getUpgradePath().equals("NA")){
											try{
												installedFeatures = finfo.getFeatureInformation(getUpgradePath(), featMap);
												}catch(FileNotFoundException fe){
													defaultPath="NA";
													ConfirmFeatures.getInst().setDefault_path(defaultPath);
													ConfirmFeatures.getInst().setConfigured_path("NA");
													setUpgradePath("NA");
													empty = true;
													if(UPGRADE_PATH_FILE.exists()){
														UPGRADE_PATH_FILE.delete();
													}
													installedFeatures.clear();
													ctx.put("mws_unreachable",true);
												}
												}else{
													empty = true;
											}
									}
								}
							}else{
									ctx.put("undeterminedPathWithNoDefault", true);
								}
							}else{
								/*if(updateMore!=null)
								{
								if(UPGRADE_PATH_FILE.exists()){
									readUpgradePath();						
								}
								upgradeResult = "NO UPGRADE";
								}
								else
								{	*/
									if(UPGRADE_PATH_FILE.exists()){
										readUpgradePath();
									}
									if(!getUpgradePath().equals("NA")){
									try{
										installedFeatures = finfo.getFeatureInformation(getUpgradePath(), featMap);
										if(submit!=null) {
											ctx.put("emptyConfigPath", true);
										} else
											ctx.put("emptyConfigPath", false);
										}catch(FileNotFoundException e){
											defaultPath="NA";
											ConfirmFeatures.getInst().setDefault_path(defaultPath);
											ConfirmFeatures.getInst().setConfigured_path("NA");
											setUpgradePath("NA");
											empty = true;
											if(UPGRADE_PATH_FILE.exists()){
												UPGRADE_PATH_FILE.delete();
											}
											installedFeatures.clear();
											ctx.put("mws_unreachable",true);
										}
										}else{
											empty = true;
									}
								}
							
							if(continue_response!=null){
								back_param=false;
								ConfirmFeatures.getInst().setInstalled_features(installedFeatures);
							}
							
							if(back_param){
								
								ctx.put("retain", "yes");
								int feat_list_size = ConfirmFeatures.getInst().getInstalled_feat_selected_add_size().size();
								ctx.put("feat_list_size", feat_list_size);
								ctx.put("installed_featurelist", ConfirmFeatures.getInst().getNew_feat_list());
								back_param=false;
								
							}else{
								ctx.put("installed_featurelist", installedFeatures);
							}
							
							ctx.put("defaultPath", ConfirmFeatures.getInst().getDefault_path());
							ctx.put("upgradePath", ConfirmFeatures.getInst().getConfigured_path());
							} catch(Exception e){			
								log.warn("Exception in obtaining the feature information. " + e.getMessage());	
						} finally {
							if(dwhConn != null) {
								try{
									dwhConn.close();
								} catch(SQLException se){
								log.warn("Error in closing dwhdb connection. " + se);
								}
							}
							if(repConn != null) {
								try {
									repConn.close();
								} catch(SQLException se) {
									log.warn("Error in closing repdb connection. " + se);
								}
							}
						}
				}
				
		//XARJSIN
		if(!empty){
			
		if(PID_FLAG.exists()){	//Should not trigger if it comes back from webserver restart
			long pid = featureAvailability.getPIDFromFile("upgrade");
			if(featureAvailability.checkPIDAlive(pid)){
				inProgress = true;
			}
		}
		
		List<String> confirmList = new ArrayList<String>();
		List<String> parsedConfirmList = new ArrayList<String>();
		List<String> finalFeatureList = new ArrayList<String>();
		if(update != null && rootPwd !=null && !inProgress){
			
			try{
				confirmList = ConfirmFeatures.getInst().getFeatures();
				parsedConfirmList = featureAvailability.parseFeatList(confirmList);
				if(parsedConfirmList!= null) {
					for (String feat : parsedConfirmList) {
							int start_index = feat.indexOf("::", 0);
							int second_index = feat.indexOf("::", start_index+1);
							String final_feature = feat.substring(start_index+2, second_index);
							finalFeatureList.add(final_feature);
					}
				}
			}
			catch(NullPointerException e){
				log.warn("No valid features were selected");
			}
			
			if(!finalFeatureList.isEmpty()) {
				
				log.info("Starting to trigger FFU upgrade process with command - " + runUpgradeCmd);
			if(RemoteExecutor.validatePwd(userName,rootPwd,HOST_ADD)){
				if(COMMIT_SUCCESS_FLAG.exists() || ROLLBACK_SUCCESS_FLAG.exists()){
					try{
					String GIVE_WRITE_FILE = "/usr/bin/chmod 777 ";
						if(FeatureAvailability.UPGRADE_STAGE_FILE.exists()){	
							RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_WRITE_FILE+FeatureAvailability.UPGRADE_STAGE_FILE);
						Files.delete(Paths.get(FeatureAvailability.UPGRADE_STAGE_FILE.getAbsolutePath()));
						}
						if(FeatureAvailability.COMMIT_STAGE_FILE.exists()){
							RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_WRITE_FILE+FeatureAvailability.COMMIT_STAGE_FILE);
						Files.delete(Paths.get(FeatureAvailability.COMMIT_STAGE_FILE.getAbsolutePath()));
						}
						if(FeatureAvailability.ROLLBACK_STAGE_FILE.exists()){
							RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_WRITE_FILE+FeatureAvailability.ROLLBACK_STAGE_FILE);
						Files.delete(Paths.get(FeatureAvailability.ROLLBACK_STAGE_FILE.getAbsolutePath()));
						}
					}catch(Exception e)
						{
							log.warn("Cannot delete previous stage files");
						}
						}
				boolean writeFile = true;
				Path featListFile = Paths.get(FEATURES_FOR_UPDATE);
				Path backupFeatList = Paths.get(BACKUP_FEAT_LIST);
				try{
					if(featListFile.toFile().exists()){
						RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_WRITE);
					}
					else{
						log.debug("Features file does not exist, running command - " + GIVE_WRITE_DIR);
						RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_WRITE_DIR);
					}
					Files.write(backupFeatList, parsedConfirmList, Charset.forName("UTF-8"));
					Files.write(featListFile, parsedConfirmList, Charset.forName("UTF-8"));
				}
				catch(Exception e){
					log.warn("Could not write to file " + e);
					writeFile = false;
				}
				RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_WRITE);
				RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_WRITE_DIR);
				if(writeFile){
					try{
						RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_EXECUTE);
						RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, runUpgradeCmd);
					}
					catch(Exception e){
						log.warn("Could not execute upgrade " + e);
					}
					RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_EXECUTE);
				}
			}
			else{
				log.warn("Could not connect with given root password");
				upgradeResult="CONFIRM UPDATE";
				ctx.put("FEATURES_LIST", finalFeatureList);
				ctx.put("UPGRADE_RESULT", upgradeResult);
				ctx.put("wrong_pwd", true);
			}
			}
			else{
				ctx.put("empty_feat", true);
			}
			update = null;
			rootPwd = null;
		}
		if(resume != null && rootPwd != null && !resumed){
			log.info("Triggering upgrade again");
			resumed = true;
			if(RemoteExecutor.validatePwd(userName, rootPwd, HOST_ADD)){
				try{
					RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_EXECUTE);
					RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, runUpgradeCmd);
				}
				catch(Exception e){
					log.warn("Could not re-initiate upgrade" + e);
				}
				RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_EXECUTE);
			}
			else{
				log.warn("Could not connect with given root password");
				ctx.put("wrong_pwd", true);
			}
			resume = null;
			rootPwd = null;
		}
		if(PID_FLAG.exists()){
			inProgress = true;
			log.debug("AdminUI initiated upgrade");
			//GET FEATURE LIST FROM FILE
			String currentStage = featureAvailability.getStageFromFile("upgrade").trim();
			String currentStage_cleanUp = featureAvailability.getStageFromFile("upgrade_cleanup").trim();
			List<String> stageList = featureAvailability.getStageListFromFile("upgrade");
			HashMap<String, String> stageMap = featureAvailability.getStageStatus(currentStage, stageList);
			List<String> upgradeFeatures = featureAvailability.readFeatFromFile("upgrade");
			List<List<String>> userLockFile = featureAvailability.getLockingInfo(LOCK_INFO_FILE);
			featStatus = getDefaultStatus(upgradeFeatures, "init");
			long pid = featureAvailability.getPIDFromFile("upgrade");
			log.debug("PID of upgrade process is - " + pid);
			
			if(featureAvailability.checkPIDAlive(pid)){
				log.debug("Upgrade is in progress. Process PID - " + pid);
				if(currentStage_cleanUp.equalsIgnoreCase("cleanup")){
					currentStage = "Initialising";
				}
				if(TP_STAGE.exists()){
					log.debug("TP upgrade has started");
					initiated = true;
					if(TP_STAGE.length() == 0){
						currentStage = "TP Upgrade Initiated";
					}
					else{
						currentStage = "TP Upgrade In Progress";
					}
					featStatus = getFeatStatus(featureAvailability.readFromFile("upgrade"));
				}
				else{
					if(initiated){
						log.debug("TP installations done");
						currentStage = "Upgrade Processes Running";
						featStatus = getFeatStatus(featureAvailability.readFromFile("upgrade"));
					}
				}
				upgradeResult = "IN PROGRESS";
			}
			else{
				//IF DONE - SUCCESS
				//IF MANAGE_FEATURE - FAILURE NO RESUME - feature_upgrade
				//ELSE - FAILURE WITH RESUME
				if(currentStage_cleanUp.equalsIgnoreCase("cleanup")){
					upgradeFeatures = featureAvailability.readFeatFromFile("success");
					featStatus = getDefaultStatus(upgradeFeatures, "success");
					stageMap = featureAvailability.getStageStatus(currentStage,stageList, 0);
					upgradeResult = "UPDATE SUCCESSFUL";
				}
				else if(currentStage_cleanUp.equalsIgnoreCase("nofile")){
					stageMap = featureAvailability.getStageStatus(currentStage,stageList, 2);
					upgradeResult = "FAILED DURING CREATE SNAPSHOT";
					resumed = false;
				}
				else if (currentStage.equalsIgnoreCase("Parsers Update")
						|| currentStage.equalsIgnoreCase("Engine, dwhdb Restart")
						|| currentStage.equalsIgnoreCase("dwhdb Restart")) {
					upgradeResult = "FAILED DURING " + currentStage.toUpperCase();
					stageMap = featureAvailability.getStageStatus(currentStage, stageList, 1);

				}else{
					upgradeResult = "FAILED DURING FEATURE UPDATE";
					stageMap = featureAvailability.getStageStatus(currentStage,stageList, 1);
					List<String> selectedFeatures = featureAvailability.readFromFile("upgrade");
					String featFailed = "";
					if(FEAT_INSTALLED_LIST.exists())
					{
						List<String> featInstalled = getListFromFile(FEAT_INSTALLED_LIST);
						featFailed = selectedFeatures.get(featInstalled.size());
					}
					else
					{
						featFailed = selectedFeatures.get(0);
					}
					for (String feat : selectedFeatures) {
						String cxc = feat.substring(0, feat.indexOf(":"));
						String featName = feat.substring(feat.indexOf(":") + 2,feat.lastIndexOf("::"));
						if (feat.equals(featFailed)) {
							featStatus.put(featName, "FAILED");
						} else if (status(cxc) == 1) {
							featStatus.put(featName, "COMPLETED");
						} else {
							featStatus.put(featName, "QUEUED");
						}
					}
				}
				initiated = false;
			}
//			ctx.put("Current_Stage", currentStage);
			ctx.put("stageMap", stageMap);
			ctx.put("stageList", stageList);
			ctx.put("statusMap", featStatus);
			ctx.put("featureListFile", upgradeFeatures);
			ctx.put("userLockFile", userLockFile);
		}
		else{
			inProgress = false;
			initiated = false;
			resumed = false;
		}
		if(rollBack != null && rootPwd != null && !rollbackOngoing){
			log.info("Triggering rollback");
			if(RemoteExecutor.validatePwd(userName, rootPwd, HOST_ADD)){
				if(!selectSnapshot(userName,rootPwd).isEmpty()){
					try{
						RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_EXECUTE);
						RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, runRollBackCmd);
					}
					catch(Exception e){
						log.warn("Could not perform rollback " + e);
					}
					RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_EXECUTE);
					if(PID_FLAG.exists()){
						PID_FLAG.delete();
					}
				}
				else{
					ctx.put("rlbk_invalid", true);
					log.warn("Couldn't write to rollback input");
				}
			}
			else{
				log.warn("Could not connect with given root password");
				ctx.put("wrong_pwd", true);
			}
			rollBack = null;
			rootPwd = null;
		}
		if(ROLLBACK_FLAG.exists()){
			if(PID_FLAG.exists()){
				PID_FLAG.delete();
			}
			rollbackOngoing = true;
			String rlbkStage = featureAvailability.getStageFromFile("rollback").trim();
			String rlbkStage_cleanUp = featureAvailability.getStageFromFile("rollback_cleanup").trim();
			List<String> rlbkStageList = featureAvailability.getStageListFromFile("rollback");
			HashMap<String, String> rlbkStageMap = featureAvailability.getStageStatus(rlbkStage, rlbkStageList);
			//TODO display snapshot
			HashMap<String, String> snapMap = featureAvailability.getSnapFromFile();
			long r_pid = featureAvailability.getPIDFromFile("rollback");
			
			if(featureAvailability.checkPIDAlive(r_pid)){
				upgradeResult = "FEATURE ROLLBACK IN PROGRESS";
			}
			else{
				if(rlbkStage_cleanUp.equalsIgnoreCase("cleanup") || rlbkStage_cleanUp.equalsIgnoreCase("nofile")){
					upgradeResult = "FEATURE ROLLBACK SUCCESSFUL";
					rlbkStageMap = featureAvailability.getStageStatus(rlbkStage, rlbkStageList, 0);
					rollbackOngoing = false;
					//Robustness - in case NMI cleanup fails
					if(ROLLBACK_FLAG.exists()){
						ROLLBACK_FLAG.delete();
					}
				}
				else{
					upgradeResult = "FEATURE ROLLBACK FAILED";
					rlbkStageMap = featureAvailability.getStageStatus(rlbkStage, rlbkStageList, 1);
					log.info("Rollback failed");
					rollbackOngoing = false;
				}
			}
			ctx.put("stageMap", rlbkStageMap);
			ctx.put("stageList", rlbkStageList);
		}
		else{
			rollbackOngoing = false;
		}
		if(commit != null && rootPwd != null && !commitOngoing){
			log.info("Triggering post upgrade");
			if(RemoteExecutor.validatePwd(userName, rootPwd, HOST_ADD)){
				try{
					RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_EXECUTE);
					RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, runCommitCmd);
				}
				catch(Exception e){
					log.warn("Could not perform post-upgrade " + e);
				}
				RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_EXECUTE);
				if(PID_FLAG.exists()){
					PID_FLAG.delete();
				}
			}
			else{
				log.warn("Could not connect with given root password");
				ctx.put("wrong_pwd", true);
			}
			commit = null;
			rootPwd = null;
		}
		if(COMMIT_FLAG.exists()){
			commitOngoing = true;
			String commitStage = featureAvailability.getStageFromFile("commit").trim();
			String commitStage_cleanUp = featureAvailability.getStageFromFile("commit_cleanup").trim();
			List<String> commitStageList = featureAvailability.getStageListFromFile("commit");
			HashMap<String, String> commitStageMap = featureAvailability.getStageStatus(commitStage, commitStageList);
//			List<String> upgradeFeatures = featureAvailability.readFeatFromFile("success");
//			featStatus = getDefaultStatus(upgradeFeatures, "success");
			long com_pid = featureAvailability.getPIDFromFile("commit");
			
			if(featureAvailability.checkPIDAlive(com_pid)){
				upgradeResult = "FEATURE COMMIT IN PROGRESS";
			}
			else{
				if(commitStage_cleanUp.equalsIgnoreCase("cleanup") || commitStage_cleanUp.equalsIgnoreCase("nofile")){
					upgradeResult = "FEATURE COMMIT SUCCESSFUL";
					commitStageMap = featureAvailability.getStageStatus(commitStage, commitStageList, 0);
					commitOngoing = false;
					//Robustness - in case NMI cleanup fails
					if(COMMIT_FLAG.exists()){
						COMMIT_FLAG.delete();
					}
				}
				else{
					upgradeResult = "FEATURE COMMIT FAILED";
					log.info("Post upgrade failed");
					commitStageMap = featureAvailability.getStageStatus(commitStage, commitStageList, 1);
					commitOngoing = false;
				}
			}
//			ctx.put("Current_Stage", commitStage);
			ctx.put("stageMap", commitStageMap);
			ctx.put("stageList", commitStageList);
//			ctx.put("statusMap", featStatus);
//			ctx.put("featureListFile", upgradeFeatures);
		}
		else{
			commitOngoing = false;
		}
		
		ctx.put("UPGRADE_RESULT", upgradeResult);
		ctx.put("REFRESH_PERIOD", REFRESH_PERIOD);
		}else{
			ctx.put("UPGRADE_RESULT", "NO UPGRADE");
		}
		return getTemplate(FEATURE_AVAILABILITY_UPDATE_TEMPLATE);
	}
	private void readUpgradePath() {
		try {
			stream = new FileReader(UPGRADE_PATH_FILE);
			buf = new BufferedReader(stream);
			String line = "";
			while ((line = buf.readLine()) != null) {
				buf = new BufferedReader(stream);
				ConfirmFeatures.getInst().setConfigured_path(line);
				setUpgradePath(line);	//setting config path here
			}
		} catch (FileNotFoundException fe )  {
			log.warn("Could not find file " + UPGRADE_PATH_FILE);
		} catch(IOException ie){
			log.warn("Could not read file " + UPGRADE_PATH_FILE + " due to " + ie.getMessage());
		}catch(Exception e){
			log.warn("Exception in reading file " + UPGRADE_PATH_FILE + " due to " + e.getMessage());
		}
	}
	private void constructDefaultPath(FeatureInformation finfo, Map<String, String> featMap2) {
		defaultPath = featureAvailability.getDefaultMWSPath();
		ConfirmFeatures.getInst().setDefault_path(defaultPath);
		if (defaultPath!=null && !defaultPath.isEmpty()) {
			try{
				installedFeatures = finfo.getFeatureInformation(defaultPath, featMap);
				ConfirmFeatures.getInst().setConfigured_path(defaultPath);
				setUpgradePath(defaultPath);
			}catch(FileNotFoundException e){	
				defaultPath = featureAvailability.getDefaultMWSPathWithRelease();
				ConfirmFeatures.getInst().setDefault_path(defaultPath);
				try{
					installedFeatures = finfo.getFeatureInformation(defaultPath, featMap);
					ConfirmFeatures.getInst().setConfigured_path(defaultPath);
					setUpgradePath(defaultPath);
				}catch(FileNotFoundException fe){
					defaultPath = "NA";
					ConfirmFeatures.getInst().setDefault_path(defaultPath);
					ConfirmFeatures.getInst().setConfigured_path("NA");
					setUpgradePath("NA");
					log.warn("Unable to reach default MWS Server ");
				}
			}
			
		}
		
	}
	/**
	 * Method to write the upgrade path in a file
	 * @param featurePath 
	 */
	private void writeUserPath(String featurePath) {
		try{
		    PrintWriter writer = new PrintWriter(UPGRADE_PATH, "UTF-8");
		    writer.println(featurePath);
		    writer.close();
		} catch (IOException e) {
		   log.warn("Exception in writing user entered path to " + UPGRADE_PATH + " due to " + e.getMessage());
		}
	}

	public String getUpgradePath() {
		return upgradePath;
	}

	public void setUpgradePath(String upgradePath) {
		if(!upgradePath.equals("NA")){
			empty = false;
		}
		this.upgradePath = upgradePath;
	}

	/**
	 * Method to write to snapshot file
	 * @param rootPwd 
	 */
	private List<String> selectSnapshot(String userName,String rootPwd) {
		List<String> snapList = new ArrayList<String>();
		List<String> serverTypes = getServerTypes();
		if(serverTypes.isEmpty()){
			log.warn("Could not read server list");
			return new ArrayList<String>();
		}
		Path snapListFile = Paths.get(SNAP_LIST);
		try{
			if (userName.equals(ROOT_USER)) {
			RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, LIST_SNAP);
			} else {
				RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_WRITE_PATH);
				RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, LIST_SNAP_ADMIN);
			}
		}
		catch(Exception e){
			log.warn("Could not list snapshots " + e);
			return new ArrayList<String>();
		} finally {
			if (!userName.equals(ROOT_USER)) {
				try {
					RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_WRITE_PATH);
				} catch (Exception e) {
					log.warn("Could not revert permission " + e);
				}
			}
		}
		snapList = getSnapShots(serverTypes);
		if(snapList.isEmpty()){
			log.warn("No valid snapshots ");
			return new ArrayList<String>();
		}
		try {
			RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_WRITE_RLBK);
			Files.write(snapListFile, snapList, Charset.forName("UTF-8"));
			RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_WRITE_RLBK);
		} catch (IOException e) {
			log.warn("Could not write to snapshot file");
			return new ArrayList<String>();
		} catch (JSchException e) {
			log.warn("Could not give proper permissions");
			return new ArrayList<String>();
		}
		// TODO To create list of possible snapshots
		return snapList;
	}

	private List<String> getSnapShots(List<String> serverTypes) {
		List<String> snapList = new ArrayList<String>();
		BufferedReader snapOut;
		try {
			snapOut = new BufferedReader(new InputStreamReader(new FileInputStream("/eniq/home/dcuser/snapList")));
			String str3;
	    	while((str3 = snapOut.readLine()) != null)
	    	{
	    		for(String blade : serverTypes){
	    			if(str3.trim().startsWith(blade)){
	    				snapList.add(str3.trim());
	    			}
	    		}
	    	}
	    	snapOut.close();
		} catch (FileNotFoundException e) {
			log.warn("Could not open file");
		} catch (IOException e) {
			log.warn("Could not read file");
		}
		return snapList;
	}

	private List<String> getServerTypes() {
		List<String> servList = new ArrayList<String>();
		BufferedReader serverHost;
		try {
			serverHost = new BufferedReader(new InputStreamReader(new FileInputStream("/eniq/sw/conf/server_types")));
			String str3;
	    	while((str3 = serverHost.readLine()) != null)
	    	{
	    		str3 = str3.substring(str3.indexOf("::") + 2, str3.lastIndexOf("::"));
	    		servList.add(str3);
	    	}
	    	serverHost.close();
		} catch (FileNotFoundException e) {
			log.warn("Could not open file");
		} catch (IOException e) {
			log.warn("Could not read file");
		}
		return servList;
	}

	/**
	 * Get the default status for when stage files are cleaned-up
	 * @param upgradeFeatures
	 * @param action
	 * @return
	 */
	private HashMap<String, String> getDefaultStatus(List<String> upgradeFeatures, String action) {
		HashMap<String, String> defStat = new HashMap<String, String>();
		for(String feat : upgradeFeatures){
			if(action.equalsIgnoreCase("init")){
				defStat.put(feat, "QUEUED");
			}
			else if(action.equalsIgnoreCase("success")){
				defStat.put(feat, "COMPLETED");
			}
			else if(action.equalsIgnoreCase("fail")){
				defStat.put(feat, "FAILED");
			}
		}
		return defStat;
	}

	/**
	 * Get the current status for list of features
	 * @param upgradeFeatures
	 * @return
	 */
	private HashMap<String, String> getFeatStatus(List<String> upgradeFeatures) {
		HashMap<String, String> featStatus = new HashMap<String, String>();
		for(String feature : upgradeFeatures){
			String cxcFeat = feature.substring(0,feature.indexOf(":"));
			String featName = feature.substring(feature.indexOf("::") + 2, feature.lastIndexOf("::"));
			if(status(cxcFeat) == 1){
				featStatus.put(featName, "COMPLETED");
			}
			else if(status(cxcFeat) == 2){
				featStatus.put(featName, "IN PROGRESS");
			}
			else{
				featStatus.put(featName, "QUEUED");
			}
		}
		return featStatus;
	}

	/**
	 * Get status from CXC
	 * @param cxcFeat
	 * @return
	 */
	private int status(String cxcFeat) {
		if(FEAT_INSTALLED_LIST.exists()){
				try {
					if(getListFromFile(FEAT_INSTALLED_LIST).contains(cxcFeat)){
						return 1;
					}
				} catch (Exception e) {
					log.warn("Exception in reading - " + FEAT_INSTALLED_LIST + " file " + e);
				}
		}
		if(TP_STAGE.exists()){
		if(TP_STAGE.length() != 0){
			BufferedReader stage;
			try {
				stage = new BufferedReader(new InputStreamReader(new FileInputStream(TP_STAGE)));
				if(stage != null){
					String[] parts = stage.readLine().split(" ");
		       		stage.close();
		       		if(parts[2].equalsIgnoreCase(cxcFeat)){
		       			return 2;
		       		}
		       	}
			} catch (FileNotFoundException e) {
				log.warn("File not found - " + TP_STAGE + " file " + e);
			} catch (IOException e) {
				log.warn("Could not read - " + TP_STAGE + " file " + e);
			} catch (Exception e){
				log.warn("Exception in reading - " + TP_STAGE + " file " + e);
			}
		}
		}
		return 0;
	}
	
	/**
	 * Extracts features/techpacks from list in given file
	 * @throws Exception 
	 * 
	 */
	public List<String> getListFromFile(File installFile) throws Exception {

		List<String> instList = new ArrayList<String>();
		final BufferedReader instL = new BufferedReader(new InputStreamReader(new FileInputStream(installFile)));
    	String str3;
    	while((str3 = instL.readLine()) != null)
    	{
    		instList.add(str3);
    	}
    	instL.close();
    	return instList;
	}
	private boolean isAdminExist() {
		String adminRole;
		try {
			adminRole = CommandRunner.runCmd(ADMIN_ROLE_EXIST_CMD, log);
			log.debug(adminRole);
			return adminRole.contains("ENIQ_ADMIN_ROLE") ;	
		} 
		catch (IOException e) {
		      log.debug("Admin Role Execution command failed.", e);
		      return false;
		    }
	  	}
	}


