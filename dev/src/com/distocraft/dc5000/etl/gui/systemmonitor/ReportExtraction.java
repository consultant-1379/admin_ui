package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.ericsson.eniq.common.RemoteExecutor;
import com.ericsson.eniq.repository.DBUsersGet;
import com.jcraft.jsch.JSchException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.FfuEtlGuiServlet;
import com.distocraft.dc5000.etl.rock.Meta_databases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public  class ReportExtraction extends FfuEtlGuiServlet
{
	private static final String EXTRACT_REPORT_PACKAGE_TEMPLATE = "reportextraction.vm";
	private String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	private static String scriptPath = "/usr/bin/bash /eniq/sw/installer/extract_reports.bsh";
	private static String swLocateAddress = "/eniq/installation/config/eniq_sw_locate";
	private String REPORTS_LOG = "/eniq/log/sw_log/tp_installer/"+timeStamp+"_ReportExtraction.log";
	private static final String UPGRADE_PATH = "/var/tmp/upgrade_path_file";
	private static final File UPGRADE_PATH_FILE = new File(UPGRADE_PATH);
	private static final String DCUSER = "dcuser";
	protected static final String HOST_ADD = "webserver";
	final String service_name = "scheduler";
	private String systemCommandString = "";
	public String UpgradePath= "";
	public String configpath = "";	
	final Log log = LogFactory.getLog(this.getClass());
	
	
	private String defaultPath = "";
	private File file ;
	private FileReader stream;
	private BufferedReader buf;
	private String upgradePath = "";
	
	
	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		//response.setHeader("Cache-Control", "private");
		String featurePath = request.getParameter("featurepath");
		String Path = request.getParameter("submitPath");
		String extract = request.getParameter("Extract Package");

		String MWSfeaturePath = new FeatureAvailability().getDefaultMWSPath();
		this.defaultPath = MWSfeaturePath + "/eniq_reports" ;
		ctx.put("defaultPath", defaultPath);

		if (defaultPath!=null && !defaultPath.isEmpty()) 
		{
			if(featurePath != null && !featurePath.isEmpty() )  
			{
				writeUserPath(featurePath);
				setUpgradePath(featurePath);			
				boolean bln = false;
				String AbsolutePath = featurePath + "/reports";
				File Pathfile = new File(AbsolutePath);
				log.info("The Absolute Path is " +AbsolutePath);
				File[] files = Pathfile.listFiles();
				String pattern = "(Report_Package)a?";
				Pattern r = Pattern.compile(pattern);
				if(Pathfile.exists())
				{
					
			        for(File f: files)
			        	
			        {		       
			          Matcher m = r.matcher(f.getName());
			            if (m.find() == true) 
			             {
			            	bln = true;
			            	if(bln)
			            	{
			            		ctx.put("undetermined_path", false);
			            	}
			            	else
			            	{
			            		ctx.put("undetermined_path", true);
			            		log.info("Default path is undefined.");
			            		writeUserPath(defaultPath);
			    				setUpgradePath(defaultPath);
			            	}
			             }
			            else
			            {
			            	ctx.put("undetermined_path", true);
			            }
				}	
				}
				else
				{
					 ctx.put("undetermined_path",true);
					 	writeUserPath(defaultPath);
					setUpgradePath(defaultPath);
				}
					
			}
		
			else 
			{
				writeUserPath(defaultPath);
				setUpgradePath(defaultPath);
				if(Path!=null)
				{
				ctx.put("emptyConfigPath", true);
				
				}
				else
				{
					
					ctx.put("emptyConfigPath", false);
				}
			}
			
		}
			else {
				if(!UPGRADE_PATH_FILE.exists()){
					writeUserPath(getUpgradePath());
					setUpgradePath(defaultPath);
				}else{
					stream = new FileReader(UPGRADE_PATH_FILE);
					buf = new BufferedReader(stream);
					String line = "";
					while ((line = buf.readLine()) != null) {
						setUpgradePath(line);
					}
				}
		}
		
		UpgradePath = getUpgradePath();
		ctx.put("upgradePath", UpgradePath);
		log.info("The upgrade path is " + getUpgradePath());
		
		if (extract!= null) {
            executor(ctx);
            
           }
		
		return getTemplate(EXTRACT_REPORT_PACKAGE_TEMPLATE); 
}
	
	
	private void writeUserPath(String featurePath) {
		try{
		    PrintWriter writer = new PrintWriter(UPGRADE_PATH, "UTF-8");
		    writer.println(featurePath);
		    writer.close();
		} catch (IOException e) {
		   log.warn("Exception in writing user entered path to " + UPGRADE_PATH + " due to " + e.getMessage());
		}	
	}
	private String getUpgradePath() {
		return upgradePath;
		}
	private void setUpgradePath(String upgradePath) {
		this.upgradePath = upgradePath;
		
	}


	private static String getPwd(String userName, String servicename) throws Exception {
		List<Meta_databases> mdList = DBUsersGet.getMetaDatabases(userName,servicename);
		if (mdList.isEmpty()) {
			mdList = DBUsersGet.getMetaDatabases(userName, servicename);
			if (mdList.isEmpty()) {
				throw new Exception("Could not find an entry for " + userName + ":"
						+ servicename + " in repdb! (was it added?)");
			}
		}
		return mdList.get(0).getPassword();
	}
	private  void executor(Context ctx) throws Exception{
		systemCommandString = scriptPath + " "+UpgradePath;
		String getPassword = getPwd(DCUSER,ReportExtraction.HOST_ADD);
		
		try {
			//ProcessBuilder pb = new ProcessBuilder("/bin/bash /eniq/sw/installer/extract_reports.bsh",  "systemCommandString");
			 String result = RemoteExecutor.executeComand(DCUSER,getPassword,HOST_ADD,systemCommandString);
			 
			  if(result.contains("BUILD SUCCESSFUL") && result.contains("Not extracting"))
			 {
				 ctx.put("unsuccessful", true);
				 ctx.put("logFile",REPORTS_LOG);		 
				 log.info("The build is successful with some failures.");
			 }
			  else if(result.contains("BUILD SUCCESSFUL"))
			 {
				ctx.put("successful",true);
				log.info("Build is successful");
			 }
			 
			 else
			 {
				 ctx.put("unsuccessmsg", true);
				 ctx.put("logFile",REPORTS_LOG);
				 log.info("The build is failed");
			 }
			log.info("The upgraded path is " + UpgradePath);
			log.info("Executed result "+ result);
			{
			String pattern = "(Not extracting)";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(result);
			int count = 0;
	        while (m.find())
	        {
	            count++;
	        }
	        ctx.put("failurecount", count);
			}
	        
		} catch (IOException e) {
			e.printStackTrace();
			log.info("IOException: " + e);
		} catch (JSchException e) {
			e.printStackTrace();
			log.warn("JSchException: " + e);
		}
	   }
	
		
	
		   
	
}
	

	


	

	
		



