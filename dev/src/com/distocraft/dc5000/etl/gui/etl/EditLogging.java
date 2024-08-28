package com.distocraft.dc5000.etl.gui.etl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;

import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.config.Source;
import com.distocraft.dc5000.etl.gui.monitor.Util;
import com.distocraft.dc5000.etl.scheduler.ISchedulerRMI;
import com.distocraft.etl.gui.info.AdminuiInfo;
import com.ericsson.eniq.common.CommonUtils;
import com.ericsson.eniq.licensing.cache.LicensingCache;


import ssc.rockfactory.RockFactory;

/**
* Copyright &copy; Distocraft ltd. All rights reserved.<br>
* This class is for viewing detailed information from log files.<br>
* 
 * @author Jaakko Melantie
*/
public class EditLogging extends EtlguiServlet { // NOPMD by eheijun on 02/06/11 14:48

  private static final long serialVersionUID = 1L;

  private final Log log = LogFactory.getLog(this.getClass()); // general logger

  private static String engineHost = null;

  private static String enginePort = null;

  private static String engineServiceName = null;

  private static String licensingHost = null;

  private static String licensingPort = null;

  private static String licensingServiceName = null;

  private static String schedulerHost = null;

  private static String schedulerPort = null;

  private static String schedulerServiceName = null;
  
  private static final String CONF_DIR_DEFAULT = "/eniq/sw/conf";

  private static final String DWH_INI_FILENAME = "dwh.ini";

  private static final String NIQ_INI_FILENAME = "niq.ini";
  
  private static final String CONF_DIR = "CONF_DIR";
  

  
   @Override
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, // NOPMD by eheijun on 02/06/11 14:49
      final Context ctx) throws IOException, ServletException {  

    Template outty = null;
    
    //final String server_Type = getServerType();
    final String server_Type = CommonUtils.getServerType();
    
    String serverType = "stats";

    final String page = "editLoggingProperties.vm";
    final String confDir = System.getProperty("CONF_DIR");    
    final File engineConf = new File(confDir, "engineLogging.properties");
    final File schedulerConf = new File(confDir, "schedulerLogging.properties");
    final File licensingConf = new File(confDir, "licensingLogging.properties");
    final File staticConf = new File(confDir, "static.properties");   
    final Properties engProp = getProperties(engineConf);
    final Properties schProp = getProperties(schedulerConf);
    final Properties licProp = getProperties(licensingConf);
    final Properties staticProp = getProperties(staticConf);   
    // HttpSession session = request.getSession(false);
    final RockFactory rockEtlRep = ((RockFactory) ctx.get("rockEtlRep"));

    final List<String> etlPackages = Util.getEtlPackages(rockEtlRep.getConnection());

    final String save = request.getParameter("save");
   
    // Populate it... Then later, iterate over its elements
    final Iterator<String> it = etlPackages.iterator();

    final Vector<ArrayList<String>> meatypeLevels = new Vector<ArrayList<String>>();

    String logEntry = "";
    String propValue = "";
    
    final HttpSession session = request.getSession();
    String username =  (String) session.getAttribute("username");
	log.info("user in EditLogging"+username);
	
	String pathInfo =request.getRequestURI();
	log.info("path Info of  EditLogging="+pathInfo);
    String ipAddress = request.getRemoteAddr();
    log.info("IpAddress in EditLogging="+ipAddress);
    
    if (save != null) {

      log.debug("Saving properties...");
      String val = "";
      final Map<String, String> params = new HashMap<String, String>();
      final Enumeration<String> parameters = request.getParameterNames();
      
    
     // get all parameters to map
      while (parameters.hasMoreElements()) {
        final String par = parameters.nextElement();
        params.put(par, request.getParameter(par));        
      }
      if (params.size() > 0) {

        final Iterator<String> iter = params.keySet().iterator();      
        String[] command = { "/bin/sh",
                               "-c",
                               "df -h | grep -i '/eniq/log'| awk '{ print $5 }'"
                             };
           Process p = Runtime.getRuntime().exec(command);           
           InputStream is = p.getInputStream();       
           Scanner s = new Scanner(is).useDelimiter("\\A");  
           if (s.hasNext()) {
               val = s.next();             
           }
           else {
               val = "";              
           }
        while (iter.hasNext()) {        	
                String [] val1=val.split("%");
                Integer i=Integer.parseInt(val1[0]);
                final String key = iter.next();
                String value = request.getParameter(key).toString();           
                final String finestlogvalue = staticProp.getProperty("FINEST.limit");
                final String finerlogvalue = staticProp.getProperty("FINER.limit");
                final String finelogvalue = staticProp.getProperty("FINE.limit");
                final String maxloglimit = staticProp.getProperty("Logging.MaxLimit");               

               
                if( i<=Integer.parseInt(maxloglimit)){               
                	log.debug(i+ " :: < "+ maxloglimit);
                	changeLogginglevel(key, value, engProp, licProp, schProp,i,finelogvalue,finerlogvalue,finestlogvalue,ctx, username,ipAddress,pathInfo);                         
               
                }else{
                	ctx.put("errorMessage","Log filesystem [/eniq/log/sw_log/] utilization is greater than "+ maxloglimit + "% , Logging cannot be changed ");
                	log.info("Log filesystem [/eniq/log/sw_log/] utilization is greater than "+ maxloglimit + "% , Logging cannot be changed ");
                }
        
        }
        // save engineLogging.Properties
         final boolean saveSuccesEng = savePropertyFile(engProp, engineConf);
         
         
        
        if (!saveSuccesEng) {
          ctx.put("errorMessage", "Error saving engineLogging.properties file");
        }

        // save schedulerLogging.Properties
        final boolean saveSuccesSch = savePropertyFile(schProp, schedulerConf);

        if (!saveSuccesSch) {
          ctx.put("errorMessage", "Error saving schedulerLogging.properties file");
        }

        // save licensingLogging.properties
        final boolean saveSuccessLic = savePropertyFile(licProp, licensingConf);

        if (!saveSuccessLic) {
          ctx.put("errorMessage", "Error saving licensingLogging.properties file");
        }
      
        // after property files are succesfully saved we need to reload config
        // for engine and scheduler
        if (saveSuccesSch && saveSuccesEng && saveSuccessLic) {
        	init(this.getServletConfig());
          final boolean isEngSuccess = reloadEngineConfig();
          final boolean isSchSuccess = reloadSchedulerConfig();
          final boolean isLicSuccess = reloadLicensingConfig();          
          if (!isEngSuccess || !isSchSuccess || !isLicSuccess) {
            ctx.put("errorMessage",
                "Error executing reloading configuration.(Check that Engine, Sheduler and Licence are online)");
         
        
        }
        }
        }
      
    
}
    
          
    

    while (it.hasNext()) {
      final ArrayList<String> l = new ArrayList<String>(); // NOPMD by eheijun on 02/06/11 14:49
      logEntry = it.next().toString();
      l.add(logEntry);
      log.debug("Getting property : etl." + logEntry + ".level");
      propValue = engProp.getProperty("etl." + logEntry + ".level");
      log.debug("Property value: " + propValue);
      l.add(propValue);

      meatypeLevels.add(l);
    }
    
    
    ctx.put("server_Type", server_Type);
    ctx.put("meatypeLevels", meatypeLevels);
    ctx.put("logLevels", getLoggingLevels());
    ctx.put("defLogLevels", getDefLoggingLevels());
    ctx.put("defLevel", engProp.getProperty(".level"));
    ctx.put("engLevel", engProp.getProperty("etlengine.level"));
    ctx.put("priorityQueueLevel", engProp.getProperty("etlengine.priorityqueue.level"));
    ctx.put("perfLevel", engProp.getProperty("performance.level"));
    ctx.put("schLevel", schProp.getProperty(".level"));
    ctx.put("licLevel", licProp.getProperty("licensing.level"));
   
   
    try {
      outty = getTemplate(page);
    } catch (Exception e) {
      throw new VelocityException(e);
    }
    

    return outty;
  }

  /**
   * Read properties
   * 
   */

  private Properties getProperties(final File propFile) {

    final Properties prop = new Properties();
    try {
      prop.load(new FileInputStream(propFile));
    } catch (Exception e) {
      // e.printStackTrace();
      log.info("Property file error reading file " + propFile, e);
    }
    return prop;
  }

  /**
   * Save properties file
   * 
   * @throws IOException
   * 
   */

  private boolean savePropertyFile(final Properties prop, final File propFile) throws IOException {

    final FileOutputStream out = new FileOutputStream(propFile);

    try {
      prop.store(out, "Saved by AdminUi");
    } catch (IOException e) {
      log.info("Property file error writing file " + propFile, e);
      return false;
    } finally {
      out.close();
    }

    return true;
  }

  /**
   * Reload engine config
   * 
   * @return isSuccess
   */

  private boolean reloadEngineConfig() {
    boolean reloaded = false;

    // START SOME SET
    try {
      // create RMI object
      log.debug("reloadEngineConfig starts.");

      //log.debug("rmi://" + engineHost + ":" + enginePort + "/" + engineServiceName);
      final ITransferEngineRMI trRMI = (ITransferEngineRMI) Naming.lookup(RmiUrlFactory.getInstance().getEngineRmiUrl());

      // start the set
      trRMI.reloadProperties();

      // aaaaaand we are done....
      reloaded = true;

      log.debug("reloadEngineConfig executed successfully.");

    } catch (MalformedURLException murle) {
      log.error("MalformedURLException error:", murle);
    } catch (RemoteException re) {
      log.error("RemoteException error:", re);
    } catch (NotBoundException nbe) {
      log.error("NotBoundException error:", nbe);
    } catch (Exception e) {
      log.error("Exception caught: ", e);
    }

    return reloaded;
  }

  /**
   * Reload scheduler config
   * 
   * @return isSuccess
   */
  private boolean reloadSchedulerConfig() {
    boolean reloaded = false;

    // START SOME SET
    try {
      // create RMI object
      log.debug("reloadSchedulerConfig starts.");

      //log.debug("rmi://" + schedulerHost + ":" + schedulerPort + "/" + schedulerServiceName);
      final ISchedulerRMI trSch = (ISchedulerRMI) Naming.lookup(RmiUrlFactory.getInstance().getSchedulerRmiUrl());

      // reload properties
      trSch.reloadLoggingProperties();

      reloaded = true;

      log.debug("reloadSchedulerConfig executed successfully.");

    } catch (MalformedURLException murle) {
      log.error("MalformedURLException error:", murle);
    } catch (RemoteException re) {
      log.error("RemoteException error:", re);
    } catch (NotBoundException nbe) {
      log.error("NotBoundException error:", nbe);
    } catch (Exception e) {
      log.error("Exception caught: ", e);
    }

    return reloaded;
  }

  /**
   * Reload the licensing config
   * 
   * @return isSuccess
   */
  private boolean reloadLicensingConfig() {
    boolean reloaded = false;

    // START SOME SET
    try {
      // create RMI object
      log.debug("reloadLicensingConfig starts.");

      //log.debug("rmi://" + licensingHost + ":" + licensingPort + "/" + licensingServiceName);
      final LicensingCache cache = (LicensingCache) Naming.lookup(RmiUrlFactory.getInstance().getLicmgrRmiUrl());

      // reload properties
      cache.reloadLogging();

      reloaded = true;

      log.debug("reloadSchedulerConfig executed successfully.");

    } catch (MalformedURLException murle) {
      log.error("MalformedURLException error:", murle);
    } catch (RemoteException re) {
      log.error("RemoteException error:", re);
    } catch (NotBoundException nbe) {
      log.error("NotBoundException error:", nbe);
    } catch (Exception e) {
      log.error("Exception caught: ", e);
    }

    return reloaded;
  }

  /**
   * Get different package logging levels
   * 
   * @return List
   */

  private List<String> getLoggingLevels() {

    final ArrayList<String> lgl = new ArrayList<String>();

    lgl.add("SEVERE");
    lgl.add("WARNING");
    lgl.add("INFO");
    lgl.add("CONFIG");
    lgl.add("FINE");
    lgl.add("FINER");
    lgl.add("FINEST");

    return lgl;
  }

  
  public void changeLogginglevel(String key,String value,Properties engProp,Properties licProp,Properties schProp,int i,String finelogvalue,String finerlogvalue,String finestlogvalue,Context ctx,String username,String ipAddress,String pathInfo ){               
      String oldValue;
      boolean a=true;
      final String meatype = key.substring(key.indexOf(":") + 1);                  
      log.debug("value :"+value);
       if (key.startsWith("logLevel:")) {
    	   if (value != null && value != "" && value != " " && value.length() > 2) {
    		   oldValue = engProp.getProperty("etl." + meatype + ".level");
    		  		
    		   log.debug("oldvalue engProp :"+oldValue);
    		   if (!(value.equalsIgnoreCase (oldValue)))
    		   {
    			   a = checkloglevel(value, i, finelogvalue, finerlogvalue,finestlogvalue,ctx);           	  
    			   	if(a){
    			   		engProp.setProperty("etl." + meatype + ".level", value);
    			   		//log.info("oldValue=" +oldValue);
    			   		log.info("Changing property entry: etl." + meatype + ".level to " + value);
    			   		AdminuiInfo.logEditLog(username,ipAddress,pathInfo,key,oldValue,value);
    			   		}
    			   } 
    	   		}else {
        // if level is empty delete property entry
        log.info("Deleting property entry: etl." + meatype + ".level");
        engProp.remove("etl." + meatype + ".level");
      }
       }
      

    if (key.equalsIgnoreCase("defLevel") && value.length() > 2) {
    		oldValue = engProp.getProperty(".level");    	
    		log.info("oldValue=" +oldValue);
    		if (!(value.equalsIgnoreCase (oldValue)))
    		{
    			a=checkloglevel(value, i, finelogvalue, finerlogvalue,finestlogvalue,ctx);   
    			if(a)  {
    				engProp.setProperty(".level", value);
    				log.info("newValue=" +value);
    				log.info("Changing default .level to " + value);
    				AdminuiInfo.logEditLog(username,ipAddress,pathInfo,key,oldValue,value);
    				}
    	}
    }
    if (key.equalsIgnoreCase("engLevel") && value.length() > 2) {
    		oldValue = engProp.getProperty("etlengine.level");   
    		log.info("oldValue=" +oldValue);
    		if (!(value.equalsIgnoreCase (oldValue)))
    		{
    			a=checkloglevel(value, i, finelogvalue, finerlogvalue,finestlogvalue,ctx);    			
    			if(a){
    				engProp.setProperty("etlengine.level", value);
    				log.info("newValue=" +value);
    				log.info("Changing etlengine.level to " + value);
    				AdminuiInfo.logEditLog(username,ipAddress,pathInfo,key,oldValue,value);
    			}
    		}
    	}
    
    if (key.equalsIgnoreCase("priorityQueueLevel") && value.length() > 2) {
    		oldValue = engProp.getProperty("etlengine.priorityqueue.level"); 
    		log.info("oldValue=" +oldValue);
    		if (!(value.equalsIgnoreCase (oldValue)))
    		{
    			a=checkloglevel(value, i, finelogvalue, finerlogvalue,finestlogvalue,ctx);    
    			if(a){
    				engProp.setProperty("etlengine.priorityqueue.level", value);
    				log.info("newValue=" +value);
    				log.info("Changing etlengine.priorityqueue.level to " + value);
    				AdminuiInfo.logEditLog(username,ipAddress,pathInfo,key,oldValue,value);
    			}
    		}
    	}

    if (key.equalsIgnoreCase("perfLevel") && value.length() > 2) {
    		oldValue = engProp.getProperty("performance.level");  
    		log.info("oldValue=" +oldValue);
    		if (!(value.equalsIgnoreCase (oldValue)))
    		{
    			a=checkloglevel(value, i, finelogvalue, finerlogvalue,finestlogvalue,ctx);    
    			if(a){
    				engProp.setProperty("performance.level", value);
    				log.info("newValue=" +value);
    				log.info("Changing performance.level to " + value);
    				AdminuiInfo.logEditLog(username,ipAddress,pathInfo,key,oldValue,value);
    				}
    		}
    }else if (key.equalsIgnoreCase("perfLevel") && value.length() < 2) {
      // if level is empty delete property entry
    	log.info("Deleting property entry: performance.level");
    	engProp.remove("performance.level");
    }

    if (key.equalsIgnoreCase("schLevel") && value.length() > 2) {
    		oldValue = schProp.getProperty(".level");
    		log.info("oldValue=" +oldValue);
    		if (!(value.equalsIgnoreCase (oldValue)))
    		{
    			a=checkloglevel(value, i, finelogvalue, finerlogvalue,finestlogvalue,ctx);     	 
    			if(a){
    				schProp.setProperty(".level", value);
    				log.info("newValue=" +value);
    				log.info("Changing scheduler default .level to " + value);
    				AdminuiInfo.logEditLog(username,ipAddress,pathInfo,key,oldValue,value);
    			}
    		}
    	}

    if (key.equalsIgnoreCase("licLevel") && value.length() > 2) {
    		oldValue = licProp.getProperty("licensing.level");
    		log.info("oldValue=" +oldValue);         
    		if (!(value.equalsIgnoreCase (oldValue)))
    		{
    			a=checkloglevel(value, i, finelogvalue, finerlogvalue,finestlogvalue,ctx);     	 
    			if(a){
    				licProp.setProperty("licensing.level", value);
    				log.info("newValue=" +value);
    				log.info("Changing licensing default .level to " + value);
    				AdminuiInfo.logEditLog(username,ipAddress,pathInfo,key,oldValue,value);
    			}
    		}
    	}
   
}
                
  
  
                
  /**
   * Get different default engine logging levels
   * 
   * @return List
   */

  private List<String> getDefLoggingLevels() {

    final ArrayList<String> lgl = new ArrayList<String>();

    lgl.add("SEVERE");
    lgl.add("WARNING");
    lgl.add("INFO");
    lgl.add("CONFIG");

    return lgl;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
   */

  @Override
  public void init(final ServletConfig config) throws ServletException { // NOPMD by eheijun on 02/06/11 14:49
    super.init(config);

    final Source configFileSource = new Source("etlgui", config.getServletContext().getRealPath("/"));

    licensingHost = configFileSource.getProperty("etl_licensing_host");
    if (licensingHost == null) {
      licensingHost = "localhost";
    }

    licensingPort = configFileSource.getProperty("etl_licensing_port");
    if (licensingPort == null) {
      licensingPort = "1200";
    }

    licensingServiceName = configFileSource.getProperty("etl_licensing_service");
    if (licensingServiceName == null) {
      licensingServiceName = "LicensingCache";
    }

    engineHost = configFileSource.getProperty("etl_engine_host");
    if (engineHost == null) {
      engineHost = "localhost";
    }
    enginePort = configFileSource.getProperty("etl_engine_port");
    if (enginePort == null) {
      enginePort = "1200";
    }
    engineServiceName = configFileSource.getProperty("etl_engine_service");
    if (engineServiceName == null) {
      engineServiceName = "TransferEngine";
    }

    schedulerHost = configFileSource.getProperty("etl_scheduler_host");
    if (schedulerHost == null) {
      schedulerHost = "localhost";
    }
    schedulerPort = configFileSource.getProperty("etl_scheduler_port");
    if (schedulerPort == null) {
      schedulerPort = "1200";
    }
    schedulerServiceName = configFileSource.getProperty("etl_scheduler_service");
    if (schedulerServiceName == null) {
      schedulerServiceName = "Scheduler";
    }

  }
  private boolean checkloglevel(String value,int i,String finelogvalue,String finerlogvalue,String finestlogvalue,Context ctx){
	  
	  if(i>Integer.parseInt(finestlogvalue)&& value.equalsIgnoreCase("FINEST")){
		  log.info("Log filesystem [/eniq/log/sw_log/] utilization is greater than "+finestlogvalue+"%  , Logging level cannot be set to  FINEST")  ; 
		  ctx.put("errorMessage","Log filesystem [/eniq/log/sw_log/] utilization is greater than "+finestlogvalue+" % , Logging level cannot be set to  FINEST");
		  return false;
	  	}
	  else if(i>Integer.parseInt(finerlogvalue) && (value.equalsIgnoreCase("FINEST") || value.equalsIgnoreCase("FINER"))){
		  ctx.put("errorMessage","Log filesystem [/eniq/log/sw_log/] utilization is greater than "+finerlogvalue+" %, Logging level cannot be set to FINER/FINEST");
		  log.info("Log filesystem [/eniq/log/sw_log/] utilization is greater than "+finerlogvalue+"% , Logging level cannot be set to FINER/FINEST");
		  return false;
	  }else if(i>Integer.parseInt(finelogvalue) && (value.equalsIgnoreCase("FINEST") || value.equalsIgnoreCase("FINER") || value.equalsIgnoreCase("FINE"))){
		  ctx.put("errorMessage","Log filesystem [/eniq/log/sw_log/] utilization is greater than "+finelogvalue+" %, Logging level cannot be set to FINE/FINER/FINEST");
		  log.info("Log filesystem [/eniq/log/sw_log/] utilization is greater than "+finelogvalue+"% , Logging level cannot be set to FINE/FINER/FINEST");
		  return false;
	  }else{
		  return true;
	  }
	  
  }
}
	