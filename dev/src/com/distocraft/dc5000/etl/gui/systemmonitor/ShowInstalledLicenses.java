package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.common.ENIQServiceStatusInfo;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.config.Configuration;
import com.distocraft.dc5000.etl.gui.config.ConfigurationFactory;
import com.distocraft.dc5000.repository.dwhrep.Interfacetechpacks;
import com.distocraft.dc5000.repository.dwhrep.InterfacetechpacksFactory;
import com.ericsson.eniq.licensing.cache.DefaultMappingDescriptor;
import com.ericsson.eniq.licensing.cache.LicenseInformation;
import com.ericsson.eniq.licensing.cache.LicensingCache;
import com.ericsson.eniq.licensing.cache.MappingDescriptor.MappingType;

/**
 * This class shows installed licenses in a html table.
 * 
 * @author ejannbe
 * 
 */
public class ShowInstalledLicenses extends EtlguiServlet { // NOPMD by eheijun on 03/06/11 08:29

  private static final long serialVersionUID = 1L;

  String serverHostName = "";

  Integer serverPort = 0;
  
  protected FileInputStream fstream;
	
  protected BufferedReader br;

  private RockFactory dwhrepRockFactory = null;

  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response,
      final Context ctx) {
    Template page = null;
    final Log log = LogFactory.getLog(this.getClass());
    
    if(ENIQServiceStatusInfo.isLicManagerOffline()){
    	ctx.put("errorSet", true);
  		ctx.put("errorText", " Failed to get license information from license manager.<br/>Please check that license manager process is running.");
  		try{
  			return getTemplate(EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU);
  		}catch(final Exception e){
  			log.error("ShowInstalledLicenses:: Exception getting velocity template: " + EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU + " ", e);
  		}
    }
    
    try {

      this.dwhrepRockFactory = (RockFactory) ctx.get("rockDwhRep");

      final Configuration configuration = ConfigurationFactory.getConfiguration();
      final java.util.Properties appProps = configuration.getETLCServerProperties();

      this.serverHostName = appProps.getProperty("ENGINE_HOSTNAME", null);
      if (this.serverHostName == null) { // trying to determine hostname
        this.serverHostName = "localhost";

        try {
          this.serverHostName = InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException ex) {
          log.info("getHostName failed", ex);
        }
      }

      this.serverPort = 1200;
      final String sporttmp = appProps.getProperty("ENGINE_PORT", "1200");
      try {
        this.serverPort = Integer.parseInt(sporttmp);
      } catch (NumberFormatException nfe) {
        log.info("Value of property ENGINE_PORT \"" + sporttmp + "\" is invalid. Using default.");
      }

      final String usePage = "show_installed_licenses.vm";

      final Vector<Vector<String>> licenses = getLicenseInfo(log);
      
       HashMap<String,Integer> rowCount = new HashMap<String,Integer>();
      
      for(Vector<String> vectElement : licenses){
    	  if(!rowCount.containsKey(vectElement.get(2))){
    	  String TempString = vectElement.get(2);
          int SuiteCount = 0;
          for(Vector<String> v2 : licenses){
        	  if(v2.get(2).equals(TempString)){
        		  SuiteCount=SuiteCount+1;
        	  }else{
        	  continue;
        	  }

        	  }
          rowCount.put(vectElement.get(2), SuiteCount);
      } else{
    	  continue;
    	  }
 
      }

      List<String> packs = new ArrayList<String>();
      for(Vector<String> vectElement : licenses){
    	  if(packs.contains(vectElement.get(1))){
    		  continue;
    	  }else{
    		  packs.add(vectElement.get(1));
    	  }
      }
      //DDP-113 :: eanguan:: 20120417 :: Filter the licenses that don't have any "Feature Identity" and "Description" text.
      Vector<Vector<String>> filteredLicenses = new Vector<Vector<String>>();
      for(Vector<String> vec : licenses){
    	  String tmpstring = vec.toString();
    	  if(vec.toString().contains("&nbsp;&nbsp;")){
    		  tmpstring = tmpstring.replaceAll("&nbsp;&nbsp;", "");
    	  }if(vec.toString().contains("<br />")){
    		  tmpstring = tmpstring.replaceAll("<br />", ",");
    	  }
    	
    	  log.info("validating License Feature Info: " + tmpstring);
    	  final String featureIdentity = vec.get(0);
    	  final String featureDesc = vec.get(1);
    	  final String licInfo = vec.get(2);
    	  if((featureIdentity != null && featureIdentity.length() > 0) && (featureDesc != null && featureDesc.length() > 0) && (licInfo != null && licInfo.length() > 0)){
    		  log.info("License Feature Info is correct. Adding this feature info to list. ");
    		  filteredLicenses.add(vec);
    	  }else{
    		  log.info("License Feature Info is not correct. Some info is missing. Skipping this feature. Will not show this in Adminui. ");
    	  }
      }//for
      
      // Comparator comparator = Collections.reverseOrder();
      // Collections.sort(logs, comparator);
      ctx.put("Packs", packs);
      ctx.put("rowcount", rowCount);
      ctx.put("licenses", filteredLicenses);
      // finally generate page

      page = getTemplate(usePage);

    } catch (ResourceNotFoundException e) {
      log.error("ResourceNotException", e);
    } catch (ParseErrorException e) {
      log.error("ParseErrorException", e);
    } catch (Exception e) {
      log.error("Exception", e);
    }

    return page;
  }

  /**
   * Get license information available from each server in LSHOST.
   * 
   * @param log
   *          Log object where logging should be done.
   * @return Returns a Vector full of Vector objects containing data about the installed licenses.
   */
  private Vector<Vector<String>> getLicenseInfo(final Log log) { // NOPMD by eheijun on 03/06/11 08:29

    LicensingCache cache = null;

    try {
      // contact the registry and get the cache instance.
      cache = (LicensingCache) Naming
          .lookup(RmiUrlFactory.getInstance().getLicmgrRmiUrl());

    } catch (Exception e) {
      log.error("Failed to get info from license manager. Exception is " + e.getMessage());
    }

    if (cache == null) {
      log.error("Could not get licenseinformation from license manager. Please check that license manager process is running.");
      return new Vector<Vector<String>>();
    }

    Vector<LicenseInformation> licInfo = null;
    final Vector<Vector<String>> formattedLicInfo = new Vector<Vector<String>>();

    try {
      licInfo = cache.getLicenseInformation();
    } catch (Exception e) {
      log.error("Failed to get licenses from license manager. Exception is " + e.getMessage());
    }

    if (licInfo == null) {
      log.error("Returned licenses was null. Failed to get license information.");
      return new Vector<Vector<String>>();
    }

    try {
    	final String propertiesFile = System.getProperty("CONF_DIR", "/eniq/sw/conf") + "/ESM_Mapping";
    	final File propFile = new File(propertiesFile);
		if(!propFile.exists()){
			log.info("file doesnt exist");
			throw new Exception("Failed to load " + propertiesFile);
		}else{
			log.info("file found");
		}
		fstream = new FileInputStream(propFile);
		 br = new BufferedReader(new InputStreamReader(fstream));
		String strLine = "";			  
			 while ((strLine = br.readLine()) != null)  {
				 String[] line = strLine.split(";");
				 String Suite = line[0];
				 String Pack = line[1];
				 String SuiteFAJ = line[2];
				 String[] CXCs = line[3].split(",");
				 for (int c = 0; c < CXCs.length; c++) {
					 for (int i = 0; i < licInfo.size(); i++) {
					 final LicenseInformation currLicInfo = licInfo.get(i);

        final Vector<String> currLicInfoVector = new Vector<String>(); // NOPMD by eheijun on 03/06/11 08:29
        // index 0 of Vector is FAJ
        currLicInfoVector.add(Suite);
        currLicInfoVector.add(Pack);
        currLicInfoVector.add(SuiteFAJ);
        currLicInfoVector.add(currLicInfo.getFajNumber());
        currLicInfoVector.add(currLicInfo.getDescription()); // index 1 of
        // Vector 1 of Vector is license Feature Description

        String featureCxc = currLicInfo.getFeatureName();
        if (CXCs[c].equals(featureCxc)){
        	if (currLicInfo.getCapacity() > -1) {
          featureCxc += ", Capacity: " + currLicInfo.getCapacity();
        }
        currLicInfoVector.add(featureCxc); // index 2
        log.info("Iterating at CXC " + featureCxc);

        final DefaultMappingDescriptor mapDesc = new DefaultMappingDescriptor(new String[] { featureCxc }, // NOPMD by eheijun on 03/06/11 08:29
            MappingType.INTERFACE);
        Vector<String> interfaces = cache.map(mapDesc);

        if (interfaces == null) {
          log.info("No interface found for CXC number " + featureCxc + ".");
          interfaces = new Vector<String>(); // NOPMD by eheijun on 03/06/11 08:29
        }

        String intfString = "";

        for (int j = 0; j < interfaces.size(); j++) {
          final String currIntf = interfaces.get(j);
          log.info("Iterating at interface " + currIntf);
          intfString += "&nbsp;&nbsp;" + currIntf + "<br />";
        }
        currLicInfoVector.add(intfString); // index 3

        String tpString = "";
        Vector<String> allTps = new Vector<String>(); // NOPMD by eheijun on 03/06/11 08:29

        // Get the techpacks related to the interface
        for (int j = 0; j < interfaces.size(); j++) {
          final String currIntf = interfaces.get(j);
          final List<String> techpacks = getTechpacks(currIntf, log);

          for (int k = 0; k < techpacks.size(); k++) {
            final String currTechpack = techpacks.get(k);

            if (allTps.contains(currTechpack)) {
              // No need to do anything here. Techpack already exists on the
              // list.
            } else {
              log.info("Iterating at techpack " + currTechpack);
              //EQEV-31649
              if(!(currLicInfo.getDescription().equals("Ericsson GSM BSS PM Tech Pack") && currTechpack.contains("RBSG2"))){
              tpString += "&nbsp;&nbsp;" + currTechpack + "<br />";
              allTps.add(currTechpack);
            }
           }
          }
        }
 
        // index 4 of Vector is license Additional Information
        currLicInfoVector.add(tpString);
        formattedLicInfo.add(currLicInfoVector);
        break;
        }
					 }	
				 }		
	}
    } catch (Exception e) {
      log.error("Failed to get interface and techpack information for a license. Exception was " + e.getMessage());
      return new Vector<Vector<String>>();
    }

    return formattedLicInfo;
  }

  /**
   * This function returns the techpacks related to this interface. The information is read from
   * dwhrep.InterfaceTechpacks table.
   * 
   * @param interfaceName
   * @return Returns a Vector full of techpack names.
   */
  private List<String> getTechpacks(final String interfaceName, final Log log) {
    final List<String> techpacknames = new Vector<String>();

    try {
      final Interfacetechpacks whereIntfTechpacks = new Interfacetechpacks(this.dwhrepRockFactory);
      whereIntfTechpacks.setInterfacename(interfaceName);
      final InterfacetechpacksFactory intfTechpacksFactory = new InterfacetechpacksFactory(this.dwhrepRockFactory,
          whereIntfTechpacks);
      final Vector<Interfacetechpacks> intfTechpacks = intfTechpacksFactory.get();
      if (intfTechpacks == null) {
        log.info("No techpack information found for interface " + interfaceName);
        return new Vector<String>();
      }
      final Iterator<Interfacetechpacks> intfTechpacksIter = intfTechpacks.iterator();
      while (intfTechpacksIter.hasNext()) {
        final Interfacetechpacks currentTechPack = intfTechpacksIter.next();
        final String techpackName = currentTechPack.getTechpackname();
        techpacknames.add(techpackName);
      }
    } catch (Exception e) {
      log.info("Exception occurred while accessing database. Exception was " + e.getMessage());
      return new Vector<String>();
    }
    return techpacknames;
  }

}
