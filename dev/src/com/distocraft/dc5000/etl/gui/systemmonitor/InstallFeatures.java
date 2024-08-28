package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

public class InstallFeatures extends FfuEtlGuiServlet {


	private String upgradePath = "";

	private String defaultPath = "";

	private Connection dwhConn;

	private Connection repConn;

	private FileReader stream;

	private BufferedReader buf;

	private boolean empty = false;

	private Map<String, String> featMap = new HashMap<String, String>();

	private Map<String, Map<String, Map<String, String>>> installFeatures;

	final Log log = LogFactory.getLog(this.getClass());
	private static final String CMD = "sudo /usr/bin/chmod 777 ";
	

	private static final String FEATURE_AVAILABILITY_INSTALL_TEMPLATE = "feature_availability_install.vm";

	private FeatureAvailability featureAvailability = new FeatureAvailability();

	// Files to write list of features
	private static final String GIVE_WRITE_PATH = "sudo /usr/bin/chmod 770 /eniq/home/dcuser";
	private static final String TAKE_WRITE_PATH = "sudo /usr/bin/chmod 750 /eniq/home/dcuser";
	protected static final String FEATURES_FOR_INSTALL = "/eniq/installation/core_install/etc/feature_output_list_";
	// backupFeatInstallList file's path to be changed to /eniq/sw/conf
	protected static final String BACKUP_FEAT_LIST = "/var/tmp/bkupFeatInstallList";
	protected static final String SNAP_LIST = "/var/tmp/rollback_conf_adminUI";
	private static final String INSTALL_PATH = "/var/tmp/FFU_install_path_file";
	// CHMOD commands to give permissions as required by FFU
	private static final String GIVE_WRITE = CMD + FEATURES_FOR_INSTALL;
	private static final String GIVE_EXECUTE = "sudo /usr/bin/chmod 777 /eniq/installation/core_install/bin/upgrade_eniq_sw.bsh";
	private static final String GIVE_WRITE_DIR = CMD
			+ FEATURES_FOR_INSTALL.substring(0, FEATURES_FOR_INSTALL.lastIndexOf("/"));
	private static final String GIVE_WRITE_RLBK = CMD + SNAP_LIST;
	private static final String TAKE_WRITE = "sudo /usr/bin/chmod 644 " + FEATURES_FOR_INSTALL;
	private static final String TAKE_EXECUTE = "sudo /usr/bin/chmod 644 /eniq/installation/core_install/bin/upgrade_eniq_sw.bsh";
	private static final String TAKE_WRITE_DIR = "sudo /usr/bin/chmod 755 "
			+ FEATURES_FOR_INSTALL.substring(0, FEATURES_FOR_INSTALL.lastIndexOf("/"));
	private static final String TAKE_WRITE_RLBK = "sudo /usr/bin/chmod 644 " + SNAP_LIST;
	// Commands to run on RemoteExecutor
	private static final String CLEAN_STAGE_UPGRADE = "/usr/bin/rm /eniq/installation/core_install/etc/current_upgrade_feature_only_stage";
	private static final String LIST_SNAP = "/bin/bash /eniq/bkup_sw/bin/prep_eniq_snapshots.bsh -u -N > /eniq/home/dcuser/snapList";
	private static final String LIST_SNAP_ADMIN = "sudo " + LIST_SNAP;
	// Constants used in execution
	private static final String ROOT_USER = "root";
	protected static final String HOST_ADD = "webserver";
	// Flags and stage files
	protected static final File PID_FLAG = new File("/var/tmp/INSTALL_FLAG_PID");
	protected static final File COMMIT_FLAG = new File("/var/tmp/INSTALL_COMMIT_PID");
	protected static final File ROLLBACK_FLAG = new File("/var/tmp/INSTALL_ROLLBACK_PID");
	private static final File FEAT_INSTALLED_LIST = new File("/var/tmp/FVM_FeatProgress");
	private static final File TP_STAGE = new File("/eniq/sw/installer/tp_stage_file");
	private static final File INSTALL_PATH_FILE = new File(INSTALL_PATH);
	private static final File COMMIT_SUCCESS_FLAG = new File("/var/tmp/COMMIT_SUCCESSFUL");
	private static final File ROLLBACK_SUCCESS_FLAG = new File("/var/tmp/ROLLBACK_SUCCESSFUL");
	private static final File INTF_Activated = new File("/var/tmp/FVM_OSSProgress");
	private static final String ADMIN_ROLE_EXIST_CMD = "cat /etc/group | grep ENIQ_ADMIN_ROLE";
	
	// Variables used in execution
	private long REFRESH_PERIOD;
	private static boolean rollbackOngoing;
	private static boolean inProgress;
	private static boolean initiated;
	private static boolean commitOngoing;
	private static boolean resumed;

	@Override
	public synchronized Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		//response.setHeader("Cache-Control", "private");
	    
		if (UpdateFeatures.PID_FLAG.exists() || UpdateFeatures.ROLLBACK_FLAG.exists()
				|| UpdateFeatures.COMMIT_FLAG.exists()) {
			ctx.put("update_alert", true);
		}
		HttpSession session = request.getSession(false);
		String featurePath = request.getParameter("featurepath");
		// Button response
		String update = null;
		String rootPwd = null;
		String rollBack = null;
		String resume = null;
		String commit = null;
		String submit = null;
		String cancel = null;
		String userName = null;
		String deleteFileCmd = "sudo /bin/bash /eniq/sw/installer/cleanup_FFU.bsh -delete "; //Note:DO NOT remove space after delete argument
		String runCommitCmd = "sudo /bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -k";
		String runRollBackCmd = "sudo /bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -z";
		String runUpgradeCmd = "sudo /bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -i " + getUpgradePath();
		 if(null != request.getParameter("userName"))
			 userName = request.getParameter("userName");
		 else
			 userName = ROOT_USER;
		 ctx.put("isAdminExist", isAdminExist());
		 //Added check as part of EQEV-111845
		 if(userName.equals(ROOT_USER)) {
			 deleteFileCmd = "/bin/bash /eniq/sw/installer/cleanup_FFU.bsh -delete ";
			 runCommitCmd = "/bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -k";
			 runRollBackCmd = "/bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -z";
			 runUpgradeCmd = "/bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -i " + getUpgradePath();
			 
		 }
		 
		if (featureAvailability.checkValidSubmit()) {
			update = StringEscapeUtils.escapeHtml(request.getParameter("Install"));
			rootPwd = request.getParameter("rootPassword");
			if(rootPwd != null && !rootPwd.isEmpty()) {
				ConfirmFeatures.getInst().setRoot_pwd(rootPwd);
			}
			// String updateMore =
			// StringEscapeUtils.escapeHtml(request.getParameter("updateMore"));
			if (!ROLLBACK_SUCCESS_FLAG.exists()) {
				rollBack = StringEscapeUtils.escapeHtml(request.getParameter("Rollback"));
			}
			resume = StringEscapeUtils.escapeHtml(request.getParameter("Resume"));
			if (!COMMIT_SUCCESS_FLAG.exists()) {
				commit = StringEscapeUtils.escapeHtml(request.getParameter("Commit"));
			}
			submit = StringEscapeUtils.escapeHtml(request.getParameter("submitPath"));
			cancel = StringEscapeUtils.escapeHtml(request.getParameter("Cancel"));
		}
		// Variables used in flow

		try {
			REFRESH_PERIOD = featureAvailability.getRefreshPeriod();

		} catch (Exception e) {
			REFRESH_PERIOD = 10000;
		}
		log.debug("Refresh period is exception - " + REFRESH_PERIOD);
		List<String> listFeat = new ArrayList<String>();
		List<String> ossAlias = featureAvailability.getOSSAlias();
		// List<String> ossForFeatures = new ArrayList<String>();
		Map<String, String> featStatus = featureAvailability.installedFeatures();
		String upgradeResult = "NO UPGRADE";
		// clean flags when cancel is clicked
		if (cancel != null) {
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
					} else {
						log.warn("Cannot delete FEATURES_FOR_UPDATE File");
					}
				} 
				for (String oss : ossAlias) {
					File FEATURES_INSTALL = new File(FEATURES_FOR_INSTALL + oss);
					if (FEATURES_INSTALL.exists()) {
						String root_pwd = ConfirmFeatures.getInst().getRoot_pwd();
						if (RemoteExecutor.validatePwd(userName, root_pwd, HOST_ADD)) {
							String deleteFeaturesForInstall = deleteFileCmd + FEATURES_FOR_INSTALL + oss;
							RemoteExecutor.executeComand(userName, root_pwd , HOST_ADD, deleteFeaturesForInstall);
						} else {
							log.warn("Cannot delete FEATURES_FOR_INSTALL File");
						}
					}
				}
				if (inProgress) {
					inProgress = false;
				}
			} catch (Exception e) {
				log.warn("Could not cancel upgrade" + e);
			}
			cancel = null;
		}

		// To be enabled if install other features implemented on UI.
		/*
		 * if(updateMore != null){ log.info(
		 * "Continuing without commit or rollback"); if(PID_FLAG.exists()){
		 * PID_FLAG.delete(); } inProgress = false; initiated = false; }
		 */

		// first page
		if (!(PID_FLAG.exists() || COMMIT_FLAG.exists() || ROLLBACK_FLAG.exists())) {
			if (!inProgress) {
				featMap = featureAvailability.getFeaturesToBeInstalled();
				if (!featMap.isEmpty()) {
					try {
						dwhConn = DatabaseConnections.getDwhDBConnection().getConnection();
						repConn = DatabaseConnections.getDwhRepConnection().getConnection();
						FeatureInformation finfo = new FeatureInformation(dwhConn, repConn, true);
						constructDefaultPath(finfo, featMap);
						// Gets called only when the user has configured the
						// software path
						if (featurePath != null) {
							if (!featurePath.isEmpty()) {
								// Get the data structure if MWS path is
								// accessible
								try {
									installFeatures = finfo.getFeatureInformation(featurePath, featMap);
									writeUserPath(featurePath);
									ConfirmFeatures.getInst().setConfigured_path(featurePath);
									setUpgradePath(featurePath);
								} catch (FileNotFoundException e) {
									log.warn(
											"Entered path not found. Considering the default path " + getUpgradePath());
									ConfirmFeatures.getInst()
											.setConfigured_path(ConfirmFeatures.getInst().getDefault_path());
									if (!defaultPath.equals("NA")) {
										ctx.put("undetermined_path", true);
										empty = false;
									} else {
										ctx.put("undeterminedPathWithNoDefault", true);
										if (INSTALL_PATH_FILE.exists()) {
											readUpgradePath();
											empty = false;
										} else {
											empty = true;
										}
										if (!getUpgradePath().equals("NA")) {
											try {
												installFeatures = finfo.getFeatureInformation(getUpgradePath(),
														featMap);
											} catch (FileNotFoundException fe) {
												defaultPath = "NA";
												ConfirmFeatures.getInst().setDefault_path(defaultPath);
												ConfirmFeatures.getInst().setConfigured_path("NA");
												setUpgradePath("NA");
												empty = true;
												if (INSTALL_PATH_FILE.exists()) {
													INSTALL_PATH_FILE.delete();
												}
												installFeatures.clear();
												ctx.put("mws_unreachable", true);
											}
										} else {
											empty = true;
										}
									}
								}
							} else {
								ctx.put("undeterminedPathWithNoDefault", true);
							}
						} else {
							if (INSTALL_PATH_FILE.exists()) {
								readUpgradePath();
							}
							if (!getUpgradePath().equals("NA")) {
								try {
									installFeatures = finfo.getFeatureInformation(getUpgradePath(), featMap);
								} catch (FileNotFoundException fe) {
									defaultPath = "NA";
									ConfirmFeatures.getInst().setDefault_path(defaultPath);
									ConfirmFeatures.getInst().setConfigured_path("NA");
									setUpgradePath("NA");
									empty = true;
									if (INSTALL_PATH_FILE.exists()) {
										INSTALL_PATH_FILE.delete();
									}
									installFeatures.clear();
									ctx.put("mws_unreachable", true);
								}
							} else {
								empty = true;
							}
						}
						if(installFeatures.isEmpty()){
							empty = true;
							ctx.put("no_features_for_installation", true);
						}else{
							ctx.put("licensed_featurelist", installFeatures);
							ctx.put("defaultPath", ConfirmFeatures.getInst().getDefault_path());
							ctx.put("upgradePath", ConfirmFeatures.getInst().getConfigured_path());
							// ctx.put("defaultPath", defaultPath);
							// ctx.put("upgradePath", getUpgradePath());
							ctx.put("ossalias", featureAvailability.getOSSAlias());

						log.info("Install Path is " + getUpgradePath());
						}
					} catch (Exception e) {
						log.warn("Exception in obtaining the feature information. " + e.getMessage());
					} finally {
						if (dwhConn != null) {
							try {
								dwhConn.close();
							} catch (SQLException se) {
								log.warn("Error in closing dwhdb connection. " + se);
							}
						}
						if (repConn != null) {
							try {
								repConn.close();
							} catch (SQLException se) {
								log.warn("Error in closing repdb connection. " + se);
							}
						}
					}
				} else {
					empty = true;
					ctx.put("no_features_for_installation", true);
				}
			}
		}
		// XARJSIN
		if (!empty) {
			if (PID_FLAG.exists()) { // Should not trigger if it comes back from
										// webserver restart
				long pid = featureAvailability.getPIDFromFile("install");
				if (featureAvailability.checkPIDAlive(pid)) {
					inProgress = true;
				}
			}
			
			if (update != null && rootPwd != null && !inProgress) {
				if (UpdateFeatures.PID_FLAG.exists() || UpdateFeatures.ROLLBACK_FLAG.exists()
						|| UpdateFeatures.COMMIT_FLAG.exists()) {
					ctx.put("update_alert", true);
				} else {
					try {
						listFeat = (Arrays.asList(request.getParameterValues("featureOSS")));
					} catch (NullPointerException e) {
						log.warn("No valid features were selected");
					}
					if (!listFeat.isEmpty()) {
						
						log.info("Starting to trigger FFU install process with command - " + runUpgradeCmd);
						
						if (RemoteExecutor.validatePwd(userName, rootPwd, HOST_ADD)) {
							if (COMMIT_SUCCESS_FLAG.exists() || ROLLBACK_SUCCESS_FLAG.exists()) {
								try {
									String giveWriteFile = CMD;
									if (FeatureAvailability.INSTALL_STAGE_FILE.exists()) {
										RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD,
												giveWriteFile + FeatureAvailability.INSTALL_STAGE_FILE);
										Files.delete(
												Paths.get(FeatureAvailability.INSTALL_STAGE_FILE.getAbsolutePath()));
									}
									if (FeatureAvailability.COMMIT_STAGE_FILE.exists()) {
										RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD,
												giveWriteFile + FeatureAvailability.COMMIT_STAGE_FILE);
										Files.delete(
												Paths.get(FeatureAvailability.COMMIT_STAGE_FILE.getAbsolutePath()));
									}
									if (FeatureAvailability.ROLLBACK_STAGE_FILE.exists()) {
										RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD,
												giveWriteFile + FeatureAvailability.ROLLBACK_STAGE_FILE);
										Files.delete(
												Paths.get(FeatureAvailability.ROLLBACK_STAGE_FILE.getAbsolutePath()));
									}
								} catch (Exception e) {
									log.warn("Cannot delete previous stage files");
								}
							}
							FileWriter fw = null;
							BufferedWriter bw = null;
							PrintWriter out = null;
							boolean writeFile = true;
							Path featListFile = null;
							ArrayList<String> featSortedByOSS = new ArrayList<>();
							try {
								for (String oss : ossAlias) {
									for (String Features : listFeat) {
										if (Features.substring(Features.lastIndexOf(":") + 1).equals(oss)) {
											featSortedByOSS.add(Features);
											featListFile = Paths.get(FEATURES_FOR_INSTALL + oss);
											if (featListFile.toFile().exists()) {
												String GIVE_WRITE_FILE = GIVE_WRITE + oss;
												RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD,
														GIVE_WRITE_FILE);
											} else {
												log.debug("Features file does not exist, running command - "
														+ GIVE_WRITE_DIR);
												RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD,
														GIVE_WRITE_DIR);
											}
											fw = new FileWriter(featListFile.toString(), true);
											bw = new BufferedWriter(fw);
											out = new PrintWriter(bw);
											out.println(Features);
											out.close();
										}
									}
								}
								Path backupFeatList = Paths.get(BACKUP_FEAT_LIST);
								Files.write(backupFeatList, featSortedByOSS, Charset.forName("UTF-8"));

							} catch (Exception e) {
								log.warn("Could not write to file " + featListFile.toString());
								writeFile = false;

							} finally {
								if (out != null) {
									out.close();
								}
								if (bw != null) {
									try {
										bw.close();
									} catch (IOException e) {
										log.error("Error While trying to close the file: " + e.getMessage());
									}
								}
								if (fw != null) {
									try {
										fw.close();
									} catch (IOException e) {
										log.error("Error While trying to close the file: " + e.getMessage());
									}
								}
							}
							RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_WRITE);
							RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_WRITE_DIR);
							if (writeFile) {
								try {
									RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_EXECUTE);
									RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, runUpgradeCmd);
								} catch (Exception e) {
									log.warn("Could not execute installation " + e);
								}
								RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_EXECUTE);
							}
						} else {
							log.warn("Could not connect with given root password");
							ctx.put("wrong_pwd", true);
						}
					} else {
						ctx.put("empty_feat", true);
					}
					update = null;
					rootPwd = null;
				}
			}
			if (resume != null && rootPwd != null && !resumed) {
				log.info("Triggering installation again");
				resumed = true;
				if (RemoteExecutor.validatePwd(userName, rootPwd, HOST_ADD)) {
					try {
						RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_EXECUTE);
						RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, runUpgradeCmd);

					} catch (Exception e) {
						log.warn("Could not re-initiate installation" + e);

					}
					RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_EXECUTE);
				} else {
					log.warn("Could not connect with given root password");
					ctx.put("wrong_pwd", true);
				}
				resume = null;
				rootPwd = null;
			}
			if (PID_FLAG.exists()) {
				inProgress = true;
				log.debug("AdminUI initiated installation");
				// GET FEATURE LIST FROM FILE
				String currentStage = featureAvailability.getStageFromFile("install").trim();
				String currentStage_cleanUp = featureAvailability.getStageFromFile("upgrade_cleanup").trim();
				List<String> stageList = featureAvailability.getStageListFromFile("install");
				HashMap<String, String> stageMap = featureAvailability.getStageStatus(currentStage, stageList);
				List<String> cxc_oss = getCXCOSS();
				LinkedHashMap<String, String> statusMapOSS = getDefaultOSSStatus(cxc_oss, "init");
				List<String> upgradeFeatures = featureAvailability.readFeatFromFile("install");
				featStatus = getDefaultStatus(upgradeFeatures, "init");
				long pid = featureAvailability.getPIDFromFile("install");
				log.debug("PID of upgrade process is - " + pid);
				if (featureAvailability.checkPIDAlive(pid)) {
					log.debug("Installation is in progress. Process PID - " + pid);
					if (currentStage_cleanUp.equalsIgnoreCase("cleanup")) {
						currentStage = "Initialising";
					}
					if (TP_STAGE.exists()) {
						log.debug("TP upgrade has started");
						initiated = true;
						if (TP_STAGE.length() == 0) {
							currentStage = "TP Upgrade Initiated";
						} else {
							currentStage = "TP Upgrade In Progress";
						}
						featStatus = getFeatStatus(featureAvailability.readFromFile("install"));
					} else {
						log.debug("TP installations done");
						if (initiated) {

							currentStage = "Upgrade Processes Running";
							featStatus = getFeatStatus(featureAvailability.readFromFile("install"));
						}
					}
					upgradeResult = "IN PROGRESS";
					// Interface Activation tracking
					if (featureAvailability.getStageFromFile("install").trim().equals("Interface Activation")
							|| INTF_Activated.exists()) {
						statusMapOSS = getOSSStatus(cxc_oss); //get status of interface activation for each cxc::oss key
						
						//show "In Progress" on interface activation status for a particular feature
						//only when current stage is "Interface Activation" 
						if (featureAvailability.getStageFromFile("install").trim().equals("Interface Activation")) {
							
							//Take the first non completed interface activation status(Queued)
							//and change it into "In Progress" and break the loop
							for (String key : statusMapOSS.keySet()) {
								if (statusMapOSS.get(key).equals("QUEUED")) {
									if(INTF_Activated.length()>0)
									{
										String content = new String(Files.readAllBytes(INTF_Activated.toPath()));
										if (content.contains(key.substring(key.indexOf("::")+2))) {
											statusMapOSS.put(key, "IN PROGRESS");
										}
									}
									break;
								}
							}
						}
					}
				} else {
					// IF DONE - SUCCESS
					// IF MANAGE_FEATURE - FAILURE NO RESUME - feature_upgrade
					// ELSE - FAILURE WITH RESUME
					if (currentStage_cleanUp.equalsIgnoreCase("cleanup")) {
						upgradeFeatures = featureAvailability.readFeatFromFile("installsuccess");
						featStatus = getDefaultStatus(upgradeFeatures, "success");
						statusMapOSS = getOSSStatus(cxc_oss);
						stageMap = featureAvailability.getStageStatus(currentStage, stageList, 0);
						upgradeResult = "INSTALLATION SUCCESSFUL";
					} else if (currentStage_cleanUp.equalsIgnoreCase("nofile")) {
						stageMap = featureAvailability.getStageStatus(currentStage, stageList, 2);
						upgradeResult = "FAILED DURING CREATE SNAPSHOT";
						resumed = false;
					} else if (currentStage.equalsIgnoreCase("Interface Activation")
							&& !currentStage_cleanUp.equalsIgnoreCase("cleanup")) {

						upgradeResult = "FAILED DURING INTERFACE ACTIVATION";
						
						//get status for each key cxc::oss
						statusMapOSS = getOSSStatus(cxc_oss);
						
						//take the first non completed (Queued) element in 
						//interface activation status and change to "FAILED"
						//in case of failure
						for (String key : statusMapOSS.keySet()) {
							if (statusMapOSS.get(key).equals("QUEUED")) {
								statusMapOSS.put(key, "FAILED");
								break;
							}
						}
						featStatus = getFeatStatus(featureAvailability.readFromFile("install"));
						stageMap = featureAvailability.getStageStatus(currentStage, stageList, 1);
					} else if (currentStage.equalsIgnoreCase("Parsers Install")
							|| currentStage.equalsIgnoreCase("Engine, dwhdb Restart")
							|| currentStage.equalsIgnoreCase("dwhdb Restart")) {
						upgradeResult = "FAILED DURING " + currentStage.toUpperCase();
						stageMap = featureAvailability.getStageStatus(currentStage, stageList, 1);

					} else {
						upgradeResult = "FAILED DURING FEATURE INSTALLATION";
						List<String> selectedFeatures = featureAvailability.readFromFile("install");
						stageMap = featureAvailability.getStageStatus(currentStage, stageList, 1);
						String featFailed = "";
						// remove duplicate features
						LinkedHashSet<String> removeDuplicates = new LinkedHashSet<String>();
						for (String feature : selectedFeatures) {
							String cxc_feat = feature.substring(0, feature.indexOf(":")) + "::"
									+ feature.substring(feature.indexOf(":") + 2, feature.lastIndexOf("::"));
							removeDuplicates.add(cxc_feat);
						}
						ArrayList<String> cxc_featList = new ArrayList<String>(removeDuplicates);
						if (FEAT_INSTALLED_LIST.exists()) {
							List<String> featInstalled = getListFromFile(FEAT_INSTALLED_LIST);
							featFailed = cxc_featList.get(featInstalled.size());
						} else {
							featFailed = cxc_featList.get(0);
						}
						for (String feat : cxc_featList) {
							String cxc = feat.substring(0, feat.indexOf(":"));
							String featName = feat.substring(feat.indexOf(":") + 2);
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
					// intf_activated = false;
				}
				ctx.put("cxc_oss", cxc_oss);
				ctx.put("statusMapOSS", statusMapOSS);
				ctx.put("stageMap", stageMap);
				ctx.put("stageList", stageList);
				ctx.put("statusMap", featStatus);
				ctx.put("featureListFile", upgradeFeatures);
				ctx.put("ossForFeatures", getOssForFeatures());
			} else {
				inProgress = false;
				initiated = false;
				resumed = false;
			}
			if (rollBack != null && rootPwd != null && !rollbackOngoing) {
				if (RemoteExecutor.validatePwd(userName, rootPwd, HOST_ADD)) {
					log.info("Triggering rollback");

					if (INTF_Activated.exists()) {
						Files.delete(INTF_Activated.toPath());
					}

					if (!selectSnapshot(userName,rootPwd).isEmpty()) {
						try {
							RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_EXECUTE);
							RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, runRollBackCmd);
						} catch (Exception e) {
							log.warn("Could not perform rollback " + e);
						}
						RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_EXECUTE);
						if (PID_FLAG.exists()) {
							PID_FLAG.delete();
						}
					} else {

						ctx.put("rlbk_invalid", true);
						log.warn("Couldn't write to rollback input");
					}
				} else {
					log.warn("Could not connect with given root password");
					ctx.put("wrong_pwd", true);
				}
				rollBack = null;
				rootPwd = null;
			}
			if (ROLLBACK_FLAG.exists()) {
				if (PID_FLAG.exists()) {
					PID_FLAG.delete();
				}
				rollbackOngoing = true;
				String rlbkStage = featureAvailability.getStageFromFile("rollback").trim();
				String rlbkStage_cleanUp = featureAvailability.getStageFromFile("rollback_cleanup").trim();
				List<String> rlbkStageList = featureAvailability.getStageListFromFile("rollback");
				HashMap<String, String> rlbkStageMap = featureAvailability.getStageStatus(rlbkStage, rlbkStageList);
				//  display snapshot
				HashMap<String, String> snapMap = featureAvailability.getSnapFromFile();
				long r_pid = featureAvailability.getPIDFromFile("rollback_install");
				if (featureAvailability.checkPIDAlive(r_pid)) {
					upgradeResult = "FEATURE ROLLBACK IN PROGRESS";
				} else {
					if (rlbkStage_cleanUp.equalsIgnoreCase("cleanup") || rlbkStage_cleanUp.equalsIgnoreCase("nofile")) {
						upgradeResult = "FEATURE ROLLBACK SUCCESSFUL";
						rlbkStageMap = featureAvailability.getStageStatus(rlbkStage, rlbkStageList, 0);
						rollbackOngoing = false;
						// Robustness - in case NMI cleanup fails
						if (ROLLBACK_FLAG.exists()) {
							ROLLBACK_FLAG.delete();
						}
					} else {
						upgradeResult = "FEATURE ROLLBACK FAILED";
						rlbkStageMap = featureAvailability.getStageStatus(rlbkStage, rlbkStageList, 1);
						log.info("Rollback failed");
						rollbackOngoing = false;
					}
				}
				ctx.put("stageMap", rlbkStageMap);
				ctx.put("stageList", rlbkStageList);
			} else {
				rollbackOngoing = false;
			}
			if (commit != null && rootPwd != null && !commitOngoing) {
				if (RemoteExecutor.validatePwd(userName, rootPwd, HOST_ADD)) {
					log.info("Triggering post upgrade");
					if (INTF_Activated.exists()) {
						Files.delete(INTF_Activated.toPath());
					}
					try {
						RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_EXECUTE);
						RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, runCommitCmd);
					} catch (Exception e) {
						log.warn("Could not perform post-upgrade " + e);
					}
					RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_EXECUTE);
					if (PID_FLAG.exists()) {
						PID_FLAG.delete();
					}
				} else {
					log.warn("Could not connect with given root password");
					ctx.put("wrong_pwd", true);
				}
				commit = null;
				rootPwd = null;
			}
			if (COMMIT_FLAG.exists()) {
				commitOngoing = true;
				String commitStage = featureAvailability.getStageFromFile("commit").trim();
				String commitStage_cleanUp = featureAvailability.getStageFromFile("commit_cleanup").trim();
				List<String> commitStageList = featureAvailability.getStageListFromFile("commit");
				HashMap<String, String> commitStageMap = featureAvailability.getStageStatus(commitStage,
						commitStageList);
				long com_pid = featureAvailability.getPIDFromFile("commit_install");
				if (featureAvailability.checkPIDAlive(com_pid)) {
					upgradeResult = "FEATURE COMMIT IN PROGRESS";
				} else {
					if (commitStage_cleanUp.equalsIgnoreCase("cleanup")
							|| commitStage_cleanUp.equalsIgnoreCase("nofile")) {
						upgradeResult = "FEATURE COMMIT SUCCESSFUL";
						commitStageMap = featureAvailability.getStageStatus(commitStage, commitStageList, 0);
						commitOngoing = false;
						// Robustness - in case NMI cleanup fails
						if (COMMIT_FLAG.exists()) {
							COMMIT_FLAG.delete();
						}
					} else {
						upgradeResult = "FEATURE COMMIT FAILED";
						log.info("Post upgrade failed");
						commitStageMap = featureAvailability.getStageStatus(commitStage, commitStageList, 1);
						commitOngoing = false;
					}
				}
				ctx.put("stageMap", commitStageMap);
				ctx.put("stageList", commitStageList);
			} else {
				commitOngoing = false;
			}

			ctx.put("UPGRADE_RESULT", upgradeResult);
			ctx.put("REFRESH_PERIOD", REFRESH_PERIOD);
		} else {
			ctx.put("UPGRADE_RESULT", "NO UPGRADE");
		}
		return getTemplate(FEATURE_AVAILABILITY_INSTALL_TEMPLATE);
	}

	/*
	 * This function is used to get the map as:-
	 * key(String): cxc_oss
	 * value(String): status(Completed/Queued)
	 * 
	 * Get mapping of all the interfaces to the cxc by getCxcIntflist() method.
	 * After creating the map , take contents of /var/tmp/FVM_OSSProgress
	 * Get the interfaces mapped to each feature and check if it present in FVM_OSSProgress.
	 * If they are present, on another map , put they key and value as "COMPLETED".
	 * else put they key and the value of key as "QUEUED".
	 */
	private LinkedHashMap<String, String> getOSSStatus(List<String> cxc_oss) {

		LinkedHashMap<String, String> stageMapOSS = new LinkedHashMap<String, String>();
		try {
			LinkedHashMap<String, List<String>> cxc_oss_intf_oss = getCxcIntflist();
			List<String> intf_oss = new ArrayList<String>();
			final BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(INTF_Activated)));
			String str;
			while ((str = buff.readLine()) != null) {
				intf_oss.add(str);
			}
			buff.close();

			for (String key : cxc_oss) {
				if (intf_oss.containsAll(cxc_oss_intf_oss.get(key))) {
					stageMapOSS.put(key, "COMPLETED");
				} else {
					stageMapOSS.put(key, "QUEUED");
				}
			}
		} catch (FileNotFoundException e) {
			log.warn("Could not find " + INTF_Activated.toString());
			for (String key : cxc_oss) {
				stageMapOSS.put(key, "QUEUED");
			}
		} catch (IOException e) {
			log.warn("Could not read " + INTF_Activated.toString());
		}
		return stageMapOSS;
	}

	private List<String> getOssForFeatures() throws IOException {
		List<String> ossForFeat = new ArrayList<String>();
		final BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(BACKUP_FEAT_LIST)));
		String str;
		while ((str = buff.readLine()) != null) {
			ossForFeat.add(str.substring(str.lastIndexOf(":") + 1));
		}
		buff.close();
		return ossForFeat;
	}

	private void readUpgradePath() {
		try {
			stream = new FileReader(INSTALL_PATH_FILE);
			buf = new BufferedReader(stream);
			String line = "";
			while ((line = buf.readLine()) != null) {
				buf = new BufferedReader(stream);
				ConfirmFeatures.getInst().setConfigured_path(line);
				setUpgradePath(line); // setting config path here
			}
		} catch (FileNotFoundException fe) {
			log.warn("Could not find file " + INSTALL_PATH_FILE);
		} catch (IOException ie) {
			log.warn("Could not read file " + INSTALL_PATH_FILE + " due to " + ie.getMessage());
		} catch (Exception e) {
			log.warn("Exception in reading file " + INSTALL_PATH_FILE + " due to " + e.getMessage());
		}
	}

	private void constructDefaultPath(FeatureInformation finfo, Map<String, String> featMap2) {
		defaultPath = featureAvailability.getDefaultMWSPath();
		ConfirmFeatures.getInst().setDefault_path(defaultPath);
		if (defaultPath != null && !defaultPath.isEmpty()) {
			try {
				installFeatures = finfo.getFeatureInformation(defaultPath, featMap);
				ConfirmFeatures.getInst().setConfigured_path(defaultPath);
				setUpgradePath(defaultPath);
			} catch (FileNotFoundException e) {
				defaultPath = featureAvailability.getDefaultMWSPathWithRelease();
				ConfirmFeatures.getInst().setDefault_path(defaultPath);
				try {
					installFeatures = finfo.getFeatureInformation(defaultPath, featMap);
					ConfirmFeatures.getInst().setConfigured_path(defaultPath);
					setUpgradePath(defaultPath);
				} catch (FileNotFoundException fe) {
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
	 * 
	 * @param featurePath
	 */
	private void writeUserPath(String featurePath) {
		try {
			PrintWriter writer = new PrintWriter(INSTALL_PATH, "UTF-8");
			writer.println(featurePath);
			writer.close();
		} catch (IOException e) {
			log.warn("Exception in writing user entered path to " + INSTALL_PATH + " due to " + e.getMessage());
		}
	}

	public String getUpgradePath() {
		return upgradePath;
	}

	public void setUpgradePath(String upgradePath) {
		if (!upgradePath.equals("NA")) {
			empty = false;
		}
		this.upgradePath = upgradePath;
	}

	/**
	 * Method to write to snapshot file
	 * 
	 * @param rootPwd
	 */
	private List<String> selectSnapshot(String userName, String rootPwd) {
		List<String> snapList = new ArrayList<String>();
		List<String> serverTypes = getServerTypes();
		if (serverTypes.isEmpty()) {
			log.warn("Could not read server list");
			return new ArrayList<String>();
		}
		Path snapListFile = Paths.get(SNAP_LIST);
		try {
			if (userName.equals(ROOT_USER)) {
			RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, LIST_SNAP);
			} else {
				RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_WRITE_PATH);
				RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, LIST_SNAP_ADMIN);
			}
		} catch (Exception e) {
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
		if (snapList.isEmpty()) {
			log.warn("No valid snapshots ");
			return new ArrayList<String>();
		}
		try {
			RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_WRITE_RLBK);
			Files.write(snapListFile, snapList, Charset.forName("UTF-8"));
			RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_WRITE_RLBK);
		} catch (IOException e) {
			log.warn("Could not write to snapshot file");
			return new ArrayList<>();
		} catch (JSchException e) {
			log.warn("Could not give proper permissions");
			return new ArrayList<String>();
		}
		return snapList;
	}

	private List<String> getSnapShots(List<String> serverTypes) {
		List<String> snapList = new ArrayList<String>();
		BufferedReader snapOut;
		try {
			snapOut = new BufferedReader(new InputStreamReader(new FileInputStream("/eniq/home/dcuser/snapList")));
			String str3;
			while ((str3 = snapOut.readLine()) != null) {
				for (String blade : serverTypes) {
					if (str3.trim().startsWith(blade)) {
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
			while ((str3 = serverHost.readLine()) != null) {
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
	 * 
	 * @param upgradeFeatures
	 * @param action
	 * @return
	 */
	private HashMap<String, String> getDefaultStatus(List<String> upgradeFeatures, String action) {
		HashMap<String, String> defStat = new HashMap<String, String>();
		for (String feat : upgradeFeatures) {
			if (action.equalsIgnoreCase("init")) {
				defStat.put(feat, "QUEUED");
			} else if (action.equalsIgnoreCase("success")) {
				defStat.put(feat, "COMPLETED");
			} else if (action.equalsIgnoreCase("fail")) {
				defStat.put(feat, "FAILED");
			}
		}
		return defStat;
	}

	private LinkedHashMap<String, String> getDefaultOSSStatus(List<String> cxc_oss, String action) {
		LinkedHashMap<String, String> defStatOSS = new LinkedHashMap<String, String>();
		for (String line : cxc_oss) {
			if (action.equalsIgnoreCase("init")) {
				defStatOSS.put(line, "QUEUED");
			} else if (action.equalsIgnoreCase("success")) {
				defStatOSS.put(line, "COMPLETED");
			}
		}
		return defStatOSS;
	}

	private List<String> getCXCOSS() {
		List<String> cxcOSSList = new ArrayList<String>();
		BufferedReader instL;
		String str;
		try {
			instL = new BufferedReader(new InputStreamReader(new FileInputStream(BACKUP_FEAT_LIST)));
			while ((str = instL.readLine()) != null) {
				String cxc_oss = str.substring(0, str.indexOf("::")) + str.substring(str.lastIndexOf("::"));
				cxcOSSList.add(cxc_oss);
			}
		} catch (FileNotFoundException e) {
			log.warn("/var/tmp/FVM_OSSProgress not found");
		} catch (IOException e) {
			log.warn("cannot read /var/tmp/FVM_OSSProgress");
		}
		return cxcOSSList;
	}

	/**
	 * Get the current status for list of features
	 * 
	 * @param upgradeFeatures
	 * @return
	 */
	private HashMap<String, String> getFeatStatus(List<String> upgradeFeatures) {
		HashMap<String, String> featStatus = new HashMap<String, String>();
		for (String feature : upgradeFeatures) {
			String cxcFeat = feature.substring(0, feature.indexOf(":"));
			String featName = feature.substring(feature.indexOf(":") + 2, feature.lastIndexOf("::"));
			if (status(cxcFeat) == 1) {
				// we will append the featname to the new file from here (if it
				// already doesnt exist)
				featStatus.put(featName, "COMPLETED");
			} else if (status(cxcFeat) == 2) {
				featStatus.put(featName, "IN PROGRESS");
			} else {
				featStatus.put(featName, "QUEUED");
			}
		}
		return featStatus;
	}

	/**
	 * Get status from CXC
	 * 
	 * @param cxcFeat
	 * @return
	 */
	private int status(String cxcFeat) {
		if (FEAT_INSTALLED_LIST.exists()) {
			try {
				if (getListFromFile(FEAT_INSTALLED_LIST).contains(cxcFeat)) {
					return 1;
				}
			} catch (Exception e) {
				log.warn("Exception in reading - " + FEAT_INSTALLED_LIST + " file " + e);
			}
		}
		if (TP_STAGE.exists()) {
			if (TP_STAGE.length() != 0) {
				BufferedReader stage;
				try {
					stage = new BufferedReader(new InputStreamReader(new FileInputStream(TP_STAGE)));
					if (stage != null) {
						String[] parts = stage.readLine().split(" ");
						stage.close();
						if (parts[2].equalsIgnoreCase(cxcFeat)) {
							return 2;
						}
					}
				} catch (FileNotFoundException e) {
					log.warn("File not found - " + TP_STAGE + " file " + e);
				} catch (IOException e) {
					log.warn("Could not read - " + TP_STAGE + " file " + e);
				} catch (Exception e) {
					log.warn("Exception in reading - " + TP_STAGE + " file " + e);
				}
			}
		}
		return 0;
	}

	/**
	 * Extracts features/techpacks from list in given file
	 * 
	 * @throws Exception
	 * 
	 */
	public List<String> getListFromFile(File installFile) throws Exception {

		List<String> instList = new ArrayList<String>();
		final BufferedReader instL = new BufferedReader(new InputStreamReader(new FileInputStream(installFile)));
		String str3;
		while ((str3 = instL.readLine()) != null) {
			instList.add(str3);
		}
		instL.close();
		return instList;
	}

	/*
	 * Returns LinkedHashMap<String,List> of:
	 * key(String) : "cxc::oss"
	 * value(List<String>) : List of "Interface::oss"
	 *
	 */
	public LinkedHashMap<String, List<String>> getCxcIntflist() {

		LinkedHashMap<String, List<String>> cxc_oss_intf_oss = new LinkedHashMap<String, List<String>>();

		try {
			//bkupFeatInstallList contains all the selected features with cxc and oss.
			BufferedReader bkup_Feat_Install_List = new BufferedReader(
					new InputStreamReader(new FileInputStream("/var/tmp/bkupFeatInstallList")));
			String str;
			String str1;
			while ((str = bkup_Feat_Install_List.readLine()) != null) {

				int last_indexof_cxc = str.indexOf("::");
				String cxc = str.substring(0, last_indexof_cxc);
				int oss1 = str.lastIndexOf("::") + 2;
				String oss = str.substring(oss1);
				String cxc_oss = cxc + "::" + oss;

				List<String> intf = new ArrayList<String>();
				
				//run this unix command to get all the interfaces associated with each cxc.
				final String GET_INTERFACE_LIST_FOR_EACH_CXC = "/eniq/sw/bin/licmgr -map interface " + cxc;
				Runtime rt = Runtime.getRuntime();
				Process process = rt.exec(GET_INTERFACE_LIST_FOR_EACH_CXC);
				BufferedReader std_out = new BufferedReader(new InputStreamReader(process.getInputStream()));

				// stdout contains interface list for feature
				while ((str1 = std_out.readLine()) != null) {
					intf.add(str1 + "::" + oss);
				}

				std_out.close();
				process.getErrorStream().close();
				process.getOutputStream().close();
				// this will contain key - cxc::oss1 and value - intf1,oss1
				// intf2,oss1 intf3,oss1
				cxc_oss_intf_oss.put(cxc_oss, intf);

			}
			bkup_Feat_Install_List.close();

		} catch (FileNotFoundException e1) {
			log.warn("Could not find file:" + e1);
		} catch (IOException e) {
			log.warn("Could not read file:" + e);
		}
		return cxc_oss_intf_oss;
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
