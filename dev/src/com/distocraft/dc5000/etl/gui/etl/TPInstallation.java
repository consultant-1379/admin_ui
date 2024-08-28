package com.distocraft.dc5000.etl.gui.etl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import ssc.rockfactory.RockFactory;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.gui.common.CommandRunner;
import com.distocraft.dc5000.etl.gui.common.DwhMonitoring;
import com.distocraft.dc5000.etl.gui.common.ENIQServiceStatusInfo;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.util.DateFormatter;
import com.distocraft.dc5000.etl.gui.util.Helper;
import com.ericsson.eniq.licensing.cache.DefaultMappingDescriptor;
import com.ericsson.eniq.licensing.cache.FeatureMapping;
import com.ericsson.eniq.licensing.cache.LicenseInformation;
import com.ericsson.eniq.licensing.cache.LicensingCache;
import com.ericsson.eniq.licensing.cache.MappingDescriptor;
import com.ericsson.eniq.licensing.cache.MappingDescriptor.MappingType;

/**
 * Copyright &copy; ERICSSON. All rights reserved.<br>
 * Shows techpack installation progress information.<br>
 *
 * @author Arjun Sinha [XARJSIN]
 **/

public class TPInstallation extends EtlguiServlet {
	// general logger
	private final Log log = LogFactory.getLog(this.getClass());
	private static boolean isTPInstall = false;
	private static boolean isStageStart = false;
	private final File stageFile = new File("/eniq/sw/installer/tp_stage_file");
	private final File tpListFile = new File("/tmp/tplist");
	private final File tpInstalledList = new File("/tmp/tpInstall");
	private final File featInstalledList = new File("/tmp/featInstall");
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private boolean isCurrFeatDone = false;
	private boolean isCurrTPDone = false;
	private List<String> featInstall = new ArrayList<String>();
	private List<String> tpInstall = new ArrayList<String>();
	private List<String> tpList = new ArrayList<String>();
	private List<String> featureList = new ArrayList<String>();
	private List<List<String>> featInstallQueue = new ArrayList<List<String>>();
	private List<List<String>> featInstallDone = new ArrayList<List<String>>();
	private List<List<String>> tpInstallQueue = new ArrayList<List<String>>();
	private List<List<String>> tpInstallDone = new ArrayList<List<String>>();
	String tpName = null;
	String rState = null;
	List<String> featureDetailsCurrent = new ArrayList<String>();
	
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
		// Default page
		Template outty = null;
	    String page = "tpinstall.vm";
	    String result = null;
	    String getDate;
	    List<String> emptyList = new ArrayList<String>();
	    emptyList.add(" ");
	    emptyList.add(" ");
	    long dateValue = System.currentTimeMillis();
    	getDate = sdf.format(new Date(dateValue));
    	try
    	{
    	if (stageFile.exists())
	    {
	    	isTPInstall = true;
	    	log.debug("TP Installation in progress");
	    	final BufferedReader stage = new BufferedReader(new InputStreamReader(new FileInputStream(stageFile)));
	       	result = stage.readLine();
	       	stage.close();
	       	if(stageFile.length() == 0)
	       	{
	       		isStageStart = false;
	       	}
	       	else
	       	{
	       		isStageStart = true;
	       		
		        if(tpInstalledList.exists())
		        {
		           	tpInstall = getListFromFile(tpInstalledList);
		        }
		        else
		        {
		        	tpInstall.add(" ");
		        }
		        if(featInstalledList.exists())
		        {
		           	featInstall = getListFromFile(featInstalledList);
		        }
		        else
		        {
		        	featInstall = emptyList;
		        }
		        String[] parts = result.split(" ");
		        
		       	final File featureListFile = new File(parts[3]);
		       	if (featureListFile.exists())
		       	{
		       		featureList = getListFromFile(featureListFile);
		       		featureList = getListEdit(featureList, featInstall, parts[2]);
		       	}
		       	else
		       	{
		       		featureList = emptyList;
		       	}
		     
		       
		       	featInstallQueue = getLicDetails(featureList);		       
		        featInstallDone = getLicDetails(featInstall);
//		        String progress = parts[0];
		        isCurrTPDone = isCurrDone(tpInstall,parts[1]);
		        isCurrFeatDone = isCurrDone(featInstall,parts[2]);
		        if (tpListFile.exists() && !isCurrFeatDone)
	       		{ 
	       			tpList = getListFromFile(tpListFile);
	       		}
	       		else
	       		{
	       			tpList = emptyList;
	       		}
		        
		        tpList = getListEdit(tpList,tpInstall,parts[1]);
		        if (!isCurrTPDone && !isCurrFeatDone)
		        {
		           	tpName = extractSubString(parts[1], "(.+)_.+_.+");
		           	rState = extractSubString(parts[1], ".+_(.+_.+)");
		           	rState = rState.substring(0, rState.lastIndexOf("."));
		        }
		        else
		        {
		           	tpName = " ";
		           	rState = " ";
		        }
		        if (!isCurrFeatDone)
		        {
		           	featureDetailsCurrent = getLicDetails(parts[2]);
		        }
		        else
		        {
		           	featureDetailsCurrent = emptyList;
		        }
		     
		        featInstallQueue = getLicDetails(featureList);
		        
		        featInstallDone = getLicDetails(featInstall);
		        tpInstallQueue = getTPParts(tpList);
		        tpInstallDone = getTPParts(tpInstall);
		        
		        if(featureDetailsCurrent.size() == 0 || featureDetailsCurrent == null){
		    		featureDetailsCurrent = emptyList;
		    	}
		    	if(featInstallQueue.size() == 0 || featInstallQueue == null){
		    		featInstallQueue.add(emptyList);
		    	}
		    	
		        ctx.put("techpackName", tpName);
		        ctx.put("rState", rState);
//		  		ctx.put("progress", progress);
		    	ctx.put("tpList", tpInstallQueue);
		    	ctx.put("tpInstall", tpInstallDone);		    	
		    	ctx.put("currentFeat", featureDetailsCurrent);		    	
		    	ctx.put("featList", featInstallQueue);
		    	ctx.put("featInstall", featInstallDone);
	        }
	    }
	    else
	    {
	    	isTPInstall = false;
	    	log.debug("TP Installation not in progress");
	    	log.info("TP Installation not in progress");
		   	final RockFactory rockDwhRep = (RockFactory) ctx.get("rockDwhRep");
		   	final List<List<String>> activeTps = DwhMonitoring.getActiveInstalledTechpacks(rockDwhRep.getConnection());
		   	ctx.put("activeTps",activeTps);
	    }
    	}
    	catch (Exception e) 
    	{
	    	log.debug("Error was: " + e.getMessage());
	    	log.info("Error was: " + e.getMessage());
	    	List<List<String>> emptyListList = new ArrayList<List<String>>();
		    emptyListList.add(emptyList);
	    	tpName = " ";
        	rState = " ";
	    	ctx.put("techpackName", tpName);
            ctx.put("rState", rState);
            ctx.put("tpList", emptyListList);
            ctx.put("tpInstall", emptyListList);
            ctx.put("currentFeat", emptyList);
            ctx.put("featList", featInstallQueue);
	    	ctx.put("featInstall", featInstallDone);
	    }
        ctx.put("isTPInstall", isTPInstall);
        ctx.put("isStageStart", isStageStart);
        ctx.put("currenttime", getDate);

// 		return the page that should be shown
        outty = getTemplate(page);
		return outty;
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
	
	/**
	 * Extracts a substring from given string based on given regExp
	 * 
	 */
	public String extractSubString(final String str, final String regExp) {

		final Pattern pattern = Pattern.compile(regExp);
		final Matcher matcher = pattern.matcher(str);

		if (matcher.matches())
		{
			final String result = matcher.group(1);
			log.debug(" regExp (" + regExp + ") found from " + str + "  :" + result);
			return result;
		}
		else 
		{
			log.error("String " + str + " doesn't match defined regExp " + regExp);
		}

		return "";

	}
	
	/**
	 * 
	 * Queries and extracts the FAJ Number and/or Description from a given CXC number using licmgr
	 * @throws Exception 
	 * 
	 */
	
	private List<String> getLicDetails(String cxcNumber) throws Exception
	{
		final List<String> licResult = new ArrayList<String>();
		String[] cxcNum = new String[] { cxcNumber };
		MappingType typeFAJ = MappingType.FAJ;
		MappingType typeDesc = MappingType.DESCRIPTION;
		LicensingCache cache = null;
		try 
		{
		      // contact the registry and get the cache instance.
		      cache = (LicensingCache) Naming
		          .lookup(RmiUrlFactory.getInstance().getLicmgrRmiUrl());
		}
		catch (Exception e)
		{
			log.error("Failed to get info from license manager. Exception is " + e.getMessage());
		}
		final MappingDescriptor md1 = new DefaultMappingDescriptor(cxcNum, typeDesc);
		final MappingDescriptor md2 = new DefaultMappingDescriptor(cxcNum, typeFAJ);
		final Vector<String> mappings1 = cache.map(md1);
	    final Vector<String> mappings2 = cache.map(md2);
	    if (mappings1 == null || mappings1.size() < 1)
	    {
	        System.out.println("No mappings found.");
	    }
	    else
	    {
	    	
	        final Enumeration<String> elem1 = mappings1.elements();
	        while (elem1.hasMoreElements())
	        {
	        	licResult.add(elem1.nextElement());
	        }
	    }
	    if (mappings2 == null || mappings2.size() < 1)
	    {
	    	System.out.println("No mappings found.");
	    }
	    else
	    {
	        final Enumeration<String> elem2 = mappings2.elements();
	        while (elem2.hasMoreElements())
	        {
	        	licResult.add(elem2.nextElement());
	        }
	    }
	    return licResult;
	}

	/**
	 * 
	 * Queries and extracts the FAJ Number and/or Description from a given list 
	 * containing CXC numbers using licmgr
	 * @throws Exception 
	 * 
	 */

	private List<List<String>> getLicDetails(List<String> cxcNumber) throws Exception
	{
		
		final List<List<String>> licResultList = new ArrayList<List<String>>();
		if (cxcNumber.isEmpty() || cxcNumber.contains(" "))
		{
			
			List<String> featEmpty = new ArrayList<String>();
			featEmpty.add(" ");
			featEmpty.add(" ");
			licResultList.add(featEmpty);
		}
		else
		{
		
			for (String cxcNum : cxcNumber)
			{
				List<String> licResult = new ArrayList<String>();				
				licResult = getLicDetails(cxcNum);
				licResultList.add(licResult);
			}
		}
		return licResultList;
	}
	
	/**
	 * 
	 * Breaks down .tpi filename into TP name and R-state
	 * @throws Exception 
	 * 
	 */

	private List<List<String>> getTPParts(List<String> tpi) throws Exception
	{
		final List<List<String>> tpList = new ArrayList<List<String>>();
		if (tpi.isEmpty() || tpi.contains(" "))
		{
			List<String> tpEmpty = new ArrayList<String>();
			tpEmpty.add(" ");
			tpEmpty.add(" ");
			tpList.add(tpEmpty);
		}
		else
		{
			for (String tp : tpi)
			{
				List<String> tpResult = new ArrayList<String>();
				String tpName = extractSubString(tp, "(.+)_.+_.+");
				String rState = extractSubString(tp, ".+_(.+_.+)");
				rState = rState.substring(0, rState.lastIndexOf("."));
				tpResult.add(tpName);
				tpResult.add(rState);
				tpList.add(tpResult);
			}
		}
		return tpList;
	}

	/**
	 * Extracts correct queued list
	 * 
	 */
	
	private List<String> getListEdit(List<String> fullList, List<String> editList, String inProgress)
	{
		List<String> nList = new ArrayList<String>();
		nList.addAll(editList);
		nList.add(inProgress);
		fullList.removeAll(nList);
		if(fullList.isEmpty())
		{
			fullList.add(" ");
			fullList.add(" ");
		}
		return fullList;
	}
	
	/**
	 * Checks if given feature has already been installed
	 */
	
	private boolean isCurrDone (List<String> installList, String currFeature)
	{
		boolean isCurrDone = false;
		if (installList.contains(currFeature))
		{
			isCurrDone = true;
		}
		return isCurrDone;
	}
}
