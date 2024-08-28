package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.CommandRunner;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.ericsson.eniq.common.RemoteExecutor;
import com.ericsson.eniq.repository.DBUsersGet;


public class Precheck extends EtlguiServlet { 

final Log log = LogFactory.getLog(this.getClass());

protected static final File PRECHECK_FLAG = new File("/var/tmp/PRECHECK_PID");
protected static final File NMI_SUCCESS_FLAG = new File("/var/tmp/precheck_success");

private static final String DCUSER = "dcuser";

private static final String ROOT_USER = "root";
private static final String HOST_ADD ="webserver";
private static final String CONSOLE_LOG_DIR = "/eniq/log/precheck/logs";
private static final String SUMMARY_LOG_DIR = "/eniq/log/precheck/summary";
private static final String TRIGGER_SCRIPT = "/eniq/sw/installer/FFU_trigger_upgrade.bsh";
private static final String GIVE_EXECUTE = "/usr/bin/chmod 777 " + TRIGGER_SCRIPT;
private static final String TAKE_EXECUTE = "/usr/bin/chmod 644 " + TRIGGER_SCRIPT;
private static final String PRECHECK_TEMPLATE = "Precheck.vm";
private static String result = "START";
private static boolean dir_found = false;
private static boolean pid_alive = false;
private long REFRESH_PERIOD;
private static final String ADMIN_ROLE_EXIST_CMD = "cat /etc/group | grep ENIQ_ADMIN_ROLE";



@Override
public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
		throws Exception {
	//response.setHeader("Cache-Control","private");
	
	String precheck_response = null;
	String finish_response = null;
	String cancel_response = null;
	String rootPwd = null;
	String userName = null;
	String runPreCheckCmd = "sudo /bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -p";
	
	if(!PRECHECK_FLAG.exists()){
		//Take response only if PRECHECK_FLAG doesn't exist(This prevents manual refresh).
		result = "START";
		precheck_response = StringEscapeUtils.escapeHtml(request.getParameter("precheck"));
	}	
	
	if(PRECHECK_FLAG.exists()){	
		long pid = getPIDFromPrecheckFile();
		if(!checkPIDAlive(pid)){
				//Take response only if PRECHECK_FLAG exists and PID not alive(This prevents manual refresh).
				finish_response = StringEscapeUtils.escapeHtml(request.getParameter("finish"));
				cancel_response = StringEscapeUtils.escapeHtml(request.getParameter("cancel"));
		}
	}
	
	 if(null != request.getParameter("userName"))
		 userName = request.getParameter("userName");
	 else
		 userName = ROOT_USER;
	rootPwd = request.getParameter("rootPassword");
	ctx.put("isAdminExist", isAdminExist());
	//Added check as part of EQEV-111845
	if(userName.equals(ROOT_USER)) {
		runPreCheckCmd = "/bin/bash /eniq/sw/installer/FFU_trigger_upgrade.bsh -p";
	}
	
	try {
		REFRESH_PERIOD = getRefreshPeriod();
	} catch(Exception e){
		REFRESH_PERIOD = 3000;
	}
	
	if(PRECHECK_FLAG.exists()){
		if(finish_response == null && cancel_response == null) {
			long pid = getPIDFromPrecheckFile();
			if(checkPIDAlive(pid)) {	
				pid_alive = true;
				ctx.put("pid_alive", pid_alive);
				// Check for existence of temp_summary folder -- This is needed. Do not remove this part of code.
				File f = new File("/eniq/log/precheck/temp_summary"); 
				List<String> consoleLog = new ArrayList<String>();
				if(f.isDirectory()){
					dir_found = true;
					File consoleLogFile = lastFileModified(CONSOLE_LOG_DIR); //Taking last modified file as there are multiple ones in same directory.
					consoleLog = Files.readAllLines(Paths.get(consoleLogFile.getAbsolutePath()),StandardCharsets.US_ASCII);
					result = "IN PROGRESS";
					ctx.put("consoleLog",consoleLog);	
				}
		 	} else {
		 		pid_alive = false;
		 		ctx.put("pid_alive", pid_alive);
		 		if(!dir_found){
		 			ctx.put("NoConsoleLog", true);
		 		}

		 		if(NMI_SUCCESS_FLAG.exists()){
		 			File summaryFile = lastFileModified(SUMMARY_LOG_DIR);
		 			String summary_report = summaryFile.toString();

		 			int start_index_date = summary_report.indexOf("_")+1;
		 			int end_index_date = summary_report.lastIndexOf("_");
		 			String summary_report_date = summary_report.substring(start_index_date, end_index_date);
		 			String summary_report_time = summary_report.substring(end_index_date+1, summary_report.lastIndexOf("."));
		 			
		 			List<String> precheckSummary = Files.readAllLines(Paths.get(summaryFile.getAbsolutePath()),StandardCharsets.US_ASCII);
		 			result = "SUCCESS";
		 			ctx.put("report_executed_date", summary_report_date);
		 			ctx.put("report_executed_time", summary_report_time);
		 			ctx.put("precheckSummary",precheckSummary);
		 			ctx.put("success-finish", true);
		 			log.info("PreCheck completed successfully");
		 		} else{
		 			result = "FAILED"; 
		 			ctx.put("failed-cancel", true);
		 		}
		 	}
		} else {
			if(cancel_response!=null) {
				if(PRECHECK_FLAG.exists()) {
					try {
						Files.delete(Paths.get(PRECHECK_FLAG.getAbsolutePath()));
					} catch(Exception e) {
						log.warn("Cannot delete PRECHECK_PID File");
					}
				}
				result = "START";
			}
		
			if(finish_response!=null) {
				if(PRECHECK_FLAG.exists()) {
					try {
						Files.delete(Paths.get(PRECHECK_FLAG.getAbsolutePath()));
					} catch(Exception e) {
						log.warn("Cannot delete PRECHECK_PID File");
					}
				}
				result = "START";
			}	
		
		}
	}else {  
		if(precheck_response!=null) { 
			if(RemoteExecutor.validatePwd(userName, rootPwd, HOST_ADD)) {				
				try {
					log.info("PreCheck trigerred");
					RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, GIVE_EXECUTE);
					RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, runPreCheckCmd); // Triggering eniq_checks.bsh 
				} catch(Exception e) {
					log.warn("Could not execute precheck " + e);
					result = "ABORTED";
					ctx.put("result", result); 
				}
				RemoteExecutor.executeComand(userName, rootPwd, HOST_ADD, TAKE_EXECUTE);
				result = "IN PROGRESS";
			} else {
					result = "START";
					ctx.put("RESULT", result);
					ctx.put("wrongPwd", true);
			}
		}
	}

	ctx.put("result",result);
	ctx.put("REFRESH_PERIOD",REFRESH_PERIOD);
	return getTemplate(PRECHECK_TEMPLATE);
}


/**
 * Fetching last modified console log/summary report.
 * @param dir
 * @return
 */
private static File lastFileModified(String dir) {
    File fl = new File(dir);
    File[] files = fl.listFiles(new FileFilter() {          
        public boolean accept(File file) {
            return file.isFile();
        }
    });
    
    long lastMod = Long.MIN_VALUE;
   
    File choice = null;
    for (File file : files) {
        if (file.lastModified() > lastMod) {
            choice = file;
            lastMod = file.lastModified();
        }
    }
    return choice;
}


/**
 * Checks if PID of eniq_checks is alive.
 * @param pid
 * @return
 * @throws Exception
 */
private boolean checkPIDAlive(long pid) throws Exception {
	String DCUSERPWD = getPwd(DCUSER,HOST_ADD);
	String PID_ALIVE = "/usr/bin/ps -p " + pid + " | grep -v CMD | cut -d' ' -f1";
	String result = RemoteExecutor.executeComand(DCUSER, DCUSERPWD ,HOST_ADD, PID_ALIVE );
	if(result.equalsIgnoreCase("") || result == null){
		log.debug("PID not alive");
		return false;
	}
	return true;
}


/**
 * Get password of dcuser from META_DATABASES.
 * @param userName
 * @param servicename
 * @return
 * @throws Exception
 */
private static String getPwd(String userName, String servicename) throws Exception {
	List<Meta_databases> mdList = DBUsersGet.getMetaDatabases(userName,servicename);
	if (mdList.isEmpty()) {
		mdList = DBUsersGet.getMetaDatabases(userName, servicename);
		if (mdList.isEmpty()) {
			throw new Exception("Could not find an entry for " + userName + ":"
					+ servicename + " in repdb! (was is added?)");
		}
	}
	return mdList.get(0).getPassword();
}


/**
 * Get PID of eniq_checks process
 * @return
 */
private long getPIDFromPrecheckFile() {
	long result = 0;
	try {
		BufferedReader stage = new BufferedReader(new InputStreamReader(new FileInputStream(Precheck.PRECHECK_FLAG)));	
		result = Long.parseLong(stage.readLine());
		stage.close();
	} catch (FileNotFoundException e) {
		log.warn("PRECHECKPID does not exist");
	} catch (NumberFormatException e) {
		log.warn("Exception :"+e);
	} catch (IOException e) {
		log.warn("Exception :"+e);
	}
	return result;
}


/**
 * Defines refresh period, which is used when precheck is in progress.
 * @return
 */
private long getRefreshPeriod() {
	final Properties ffuProps = new Properties();
	try{
		final File ffuProp = new File("/eniq/sw/runtime/tomcat/webapps/adminui/conf/FFU.properties");
		ffuProps.load(new FileInputStream(ffuProp));
		return Long.parseLong(ffuProps.getProperty("PRECHECK_REFRESH_PERIOD", "3000"));
	}
	catch(Exception e){
		return 10000;
	}
}

private  boolean isAdminExist() {
	String adminRole;
	try {
		adminRole = CommandRunner.runCmd(ADMIN_ROLE_EXIST_CMD, log);
		log.info(adminRole);
		return adminRole.contains("ENIQ_ADMIN_ROLE") ;	
	} 
	catch (IOException e) {
	      log.debug("Admin Role Execution command failed.", e);
	      return false;
	    }
  	}
}