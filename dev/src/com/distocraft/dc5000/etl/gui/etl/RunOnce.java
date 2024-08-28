package com.distocraft.dc5000.etl.gui.etl;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.common.ServicenamesHelper;
import com.distocraft.dc5000.etl.gui.config.*;
import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.monitor.Util;


/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Runs set that is selected from GUI. <br>
 * RMI -connection is used to execute set.
 * 
 * @author Antti Laurila
 */
public class RunOnce extends EtlguiServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private Log log = LogFactory.getLog(this.getClass()); // general logger

  private static String engineHost = null;
  private static String enginePort = null;
  private static String engineServiceName = null;
  private final String rmiPort = "1200" ;
  private final String serviceRef = "TransferEngine";

  
  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
    
    Template outty = null;
    final String page = "etlRunOnce.vm"; // show this page if nothing else is state

    Connection connERep = ((RockFactory) ctx.get("rockEtlRep")).getConnection();
    HttpSession session = request.getSession(false);
    
    String setType =  request.getParameter("st");
    String packageType = request.getParameter("package");
    

    String pattern =  "^[a-zA-Z0-9_-]*$";
    
    if(setType == null){
    	setType = "*";
    }
    if(setType.matches(pattern)){
    	setType = StringEscapeUtils.escapeHtml(setType);
    }else{
    	setType = null;
    }
    
    if(packageType == null){
    	packageType = "*";
    }
    if(packageType.matches(pattern)){
    	packageType = StringEscapeUtils.escapeHtml(packageType);
    }else{
    	packageType = null;
    }
    
    log.debug("setType="+ setType);
    log.debug("packageType="+ packageType);
    
    if (setType != null) {
      if (session.getAttribute("setType") != null && !session.getAttribute("setType").toString().equalsIgnoreCase(setType)) {
        packageType = "-";
      }
      session.setAttribute("setType", setType);
    } else if (session.getAttribute("setType") != null) {
      setType = session.getAttribute("setType").toString();
    } else {
      session.setAttribute("setType", setType);
    }
   
    if (packageType != null) {
      session.setAttribute("packageName", packageType);
    } else if (session.getAttribute("packageName") != null) {
      packageType = session.getAttribute("packageName").toString();
    } else {
      session.setAttribute("packageName", packageType);
    }
    
    if (setType == null) {
      ctx.put("etlsets", Util.getActiveEtlPackages(connERep));
    } else if (setType.equalsIgnoreCase("-")) {
      ctx.put("etlsets", Util.getActiveEtlPackages(connERep));
    } else {
      // ctx.put("settypes",Util.getSetTypes());
      ctx.put("etlsets", Util.getActiveEtlPackages(connERep, setType));
    }
    
    ctx.put("st", setType);
    ctx.put("settypes", Util.getSetTypes(connERep));
    ctx.put("package", packageType);

    if (packageType != null) {
      Map<String, List<List<String>>> uneditedReturnValue = Util.getSetsForPackage(connERep, packageType);
      // Call to new method renameVersion to get consistent version names.
      Connection connDRep = ((RockFactory) ctx.get("rockDwhRep")).getConnection();
      Map<String, List<List<String>>> editedReturnValue = null;
      if(packageType.startsWith("INTF")){
    	  editedReturnValue = renameInterfaceVersion(connDRep, uneditedReturnValue, packageType);  
      }
      else{
    	  editedReturnValue = renameTechpackVersion(connDRep, uneditedReturnValue, packageType);
      }
      ctx.put("packageSets", editedReturnValue);
    }

    // see if user requested to start some set
    final String colName = request.getParameter("colName");
    final String setName = request.getParameter("setName");

    outty = getTemplate(page);
    
    // if both are defined we should start a set
    if (colName != null && setName != null) {
      final boolean success = startSet(colName, setName);

      log.debug("Started set: " + colName + " " + setName + " success bit: " + success);
      
      if (setName.startsWith("StorageTimeAction")) {
        log.debug("TypeSaved session attribute is removed.");
        session.removeAttribute("typeActivationSaved");
      }
      // now jump to "realtime monitoring" servlet    
      ShowSet st = new ShowSet();
      try {
        st.init(this.getServletConfig());
        outty = st.doHandleRequest(request, response, ctx);
        log.debug("To page: " + page);
      } catch (ServletException e) {
        log.debug("Exception: ", e);
      }
    }

    // return the page that should be shown
    return outty;
  }

  
  /*
   * Method to convert the version information which can be in one of the two forms:
   * 1. R21B_b16
   * 2. ((13)) 
   * To 
   * 1. R21B_b16
   * 2. R20D_b13
   * Consistent display in the ETLC Set Scheduling screen.
   */
  private Map<String, List<List<String>>> renameInterfaceVersion(Connection conn, Map<String, List<List<String>>> uneditedReturnValue, String packageType){	
	  
	  Set<String> keySet = uneditedReturnValue.keySet();
	  Iterator<String> iter = keySet.iterator();
	  List<List<String>> temp = null;
	  String version = null;
	  // This implies an interface case e.g INTF_DC_E_BSS_APG-eniq_oss_1. Get only the interface name.
	  if(packageType.contains("-")){
		  packageType = packageType.substring(0,packageType.indexOf("-"));
	  }  
	  
	  
	  String sqlStmt = null;
	  String tempVersion = null;
	  List<String> tempList = null;
	  Iterator<List<String>> listIterator = null;
	  
	  while(iter.hasNext()){
		  temp = uneditedReturnValue.get(iter.next());
		  listIterator = temp.iterator();
		  while(listIterator.hasNext()){
			  tempList = listIterator.next();
			  version = tempList.get(2); //Have to hard code to 2 as that is what comes in the resultset in the Util.getSetsForPackage method.  
			  
			  // If the version already has an _ then no need to bother. The one's with ((x)) - generated from the IDE have to be modified.
			  if(version.contains("_")){
				  continue;
			  }
			  
			  sqlStmt = "select RSTATE+'_'+INTERFACEVERSION from DataInterface where INTERFACENAME = '"+packageType+"'";
		    
		  	log.debug("sqlStmt"+ sqlStmt);
		    Statement stmt = null;
		    ResultSet rset = null;
		    //
		    try {
		      
		      stmt = conn.createStatement();
		      rset = stmt.executeQuery(sqlStmt);
		      
		  
		      while (rset.next()) {
		    	  tempVersion = rset.getString(1);		    	  
		    	  if(tempVersion.contains("(")){
		    		  version = tempVersion.replace("((", "b");
		    		  version= version.replace("))", "");
		    	  }
		    	  else{
		    		  version = rset.getString(1);
		    	  }
		    	  }
		      log.debug("version:"+version);
		      // Remove the old version and add the new version
		      tempList.remove(2);
	    	  tempList.add(2, version);
		    
		    } catch (SQLException ex) {
		      log.info("SQLException: " + ex);
		    } finally {
		      try {
		        if (rset != null) {
		          rset.close();
		        }
		      } catch (SQLException e) {
		        log.debug("SQLException", e);
		      }
		      try {
		        if (stmt != null) {
		          stmt.close();
		        }
		      } catch (SQLException e) {
		        log.debug("Exception", e);
		      }
		    }
		  }
		  
	  }
	  return uneditedReturnValue;
  }
  
  /*
   * Method to convert the version information which can be in one of the two forms:
   * 1. R21B_b16
   * 2. ((13)) 
   * To 
   * 1. R21B_b16
   * 2. R20D_b13
   * Consistent display in the ETLC Set Scheduling screen.
   */
  private Map<String, List<List<String>>> renameTechpackVersion(Connection conn, Map<String, List<List<String>>> uneditedReturnValue, String packageType){	
	  
	  Set<String> keySet = uneditedReturnValue.keySet();
	  Iterator<String> iter = keySet.iterator();
	  List<List<String>> temp = null;
	  String version = null;	  
	  String sqlStmt = null;
	  String tempVersion = null;
	  List<String> tempList = null;
	  Iterator<List<String>> listIterator = null;
	  
	  while(iter.hasNext()){
		  temp = uneditedReturnValue.get(iter.next());
		  listIterator = temp.iterator();
		  while(listIterator.hasNext()){
			  tempList = listIterator.next();
			  version = tempList.get(2); //Have to hard code to 2 as that is what comes in the resultset in the Util.getSetsForPackage method.  
			  
			  // If the version already has an _ then no need to bother. The one's with ((x)) - generated from the IDE have to be modified.
			  if(version.contains("_")){
				  continue;
			  }
		  
			  sqlStmt = "select techpack_version from versioning where versionid = '"+packageType+":"+version+"'";	
		  
		  	log.debug("sqlStmt"+ sqlStmt);
		    Statement stmt = null;
		    ResultSet rset = null;
		    //
		    try {
		      
		      stmt = conn.createStatement();
		      rset = stmt.executeQuery(sqlStmt);
		      String adminUiVersion = null;
		  
		      while (rset.next()) {
		    	  tempVersion = rset.getString(1);
		    	  log.debug("tempVersion:"+tempVersion);
		    	  if(!tempVersion.contains("_")){
		    		  adminUiVersion = getVersionForAdminUI(version);
		    		  if(adminUiVersion.startsWith("b")){
		    			  // Incase of DWH_BASE or DWH_Manager techpacks.
		    			  version = rset.getString(1) + "_" + adminUiVersion;  
		    		  }
		    		  else{
		    			  version = rset.getString(1) + "_b"+ adminUiVersion;
		    		  }
		    		  
		    	  }
		    	  else{
		    		  version = rset.getString(1);
		    	  }
		    	  }
		      log.debug("version:"+version);
		      // Remove the old version and add the new version
		      tempList.remove(2);
	    	  tempList.add(2, version);
		    
		    } catch (SQLException ex) {
		      log.info("SQLException: " + ex);
		    } finally {
		      try {
		        if (rset != null) {
		          rset.close();
		        }
		      } catch (SQLException e) {
		        log.debug("SQLException", e);
		      }
		      try {
		        if (stmt != null) {
		          stmt.close();
		        }
		      } catch (SQLException e) {
		        log.debug("Exception", e);
		      }
		    }
		  }
		  
	  }
	  return uneditedReturnValue;
  }
  
  private String getVersionForAdminUI(String inputVersion){
	  String resultVersion = null;
	  resultVersion = inputVersion.substring(inputVersion.indexOf(":")+1);
	  resultVersion = resultVersion.replace("((", "");
	  resultVersion = resultVersion.replace("))", "");	  
	  return resultVersion;
  }
  
  /**
   * Starts selected set.
   * 
   * @param collectionSetName
   * @param collectionName
   * @return isSuccess
   */
	private boolean startSet(String collectionSetName, String collectionName) {
		boolean started = false;
		// START SOME SET
		try {
			// create RMI object
			ITransferEngineRMI trRMI = (ITransferEngineRMI) Naming
					.lookup(RmiUrlFactory.getInstance().getEngineRmiUrl());
			
			// start the set
			if (collectionName.contains("DWHM_StorageTimeUpdate_") || collectionName.contains("DWHM_Install_")) {
				List<String> dependentList = new ArrayList<String>();
				dependentList.add(collectionName);
				log.info("Disabling TP : " +dependentList);
				trRMI.disableDependentTP(dependentList);
				log.info("Executing set : " +collectionSetName);
				trRMI.executeAndWait(collectionSetName, collectionName, "");
				log.info("Enabling TP : " +dependentList);
				trRMI.enableDependentTP(dependentList);
			} else {
				trRMI.execute(collectionSetName, collectionName, "");
			}

			started = true;
		} catch (MalformedURLException murle) {
			log.error("MalformedURLException error:", murle);
		} catch (RemoteException re) {
			log.error("RemoteException error:", re);
		} catch (NotBoundException nbe) {
			log.error("NotBoundException error:", nbe);
		} catch (Exception e) {
			log.error("Exception caught: ", e);
		}

		return started;
	}

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
   */
  
  public void init(ServletConfig arg0) throws ServletException {
    super.init(arg0);

    Source configFileSource = new Source("etlgui", arg0.getServletContext().getRealPath("/"));
    engineHost = configFileSource.getProperty("etl_engine_host");
    if (engineHost == null) {
      engineHost = "localhost";
      try{
    	  engineHost = ServicenamesHelper.getServiceHost("engine", "localhost");  
      }catch(final Exception e){    	  
    	  engineHost = "localhost" ;
      }
    }
    enginePort = configFileSource.getProperty("etl_engine_port");
    if (enginePort == null) {
      enginePort = "1200";
    }
    engineServiceName = configFileSource.getProperty("etl_engine_service");
    if (engineServiceName == null) {
      engineServiceName = "TransferEngine";
    }
  }
 
}