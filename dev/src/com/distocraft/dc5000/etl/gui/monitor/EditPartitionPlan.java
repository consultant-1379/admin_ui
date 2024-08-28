package com.distocraft.dc5000.etl.gui.monitor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.repository.dwhrep.Partitionplan;
import com.distocraft.dc5000.repository.dwhrep.PartitionplanFactory;
import com.distocraft.dc5000.repository.dwhrep.Typeactivation;
import com.distocraft.dc5000.repository.dwhrep.TypeactivationFactory;
import com.distocraft.etl.gui.info.AdminuiInfo;

/**
 * With this servlet user can edit one of the partition plans. Page also shows
 * the TypeActivation's that use this specific PartitionPlan.
 * 
 * @author Janne Berggren
 */
public class EditPartitionPlan extends EtlguiServlet {

  private static final long serialVersionUID = 1L;
  
  private final Log log = LogFactory.getLog(this.getClass()); // general logger

  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, final Context ctx)
      throws Exception { // NOPMD by eheijun on 02/06/11 15:44

    Template outty = null;

    final String page = "editPartitionPlan.vm";

    final RockFactory dwhRepRockFactory = (RockFactory) ctx.get("rockDwhRep");
    final HttpSession session = request.getSession();
    String username =  (String) session.getAttribute("username");
	log.info("user in EditPartitionPlan="+username);
	
	String pathInfo =request.getRequestURI();
	log.info("path Info of  EditPartitionPlann="+pathInfo);
    String ipAddress = request.getRemoteAddr();
    log.info("IpAddress in EditPartitionPlan="+ipAddress);

    if (request.getParameterMap().containsKey("action") && request.getParameter("action").equalsIgnoreCase("save")) {
      // Update the values of this partitionplan.
      final String defaultStorageTime = request.getParameter("defaultStorageTime");
      log.info("Storagetime changed="+defaultStorageTime);
      final String partitionPlan = request.getParameter("partitionPlan");
      log.info("Storagetime changed="+defaultStorageTime+"--"+"partitionPlan="+partitionPlan);
      AdminuiInfo.logEditPartionPlan( username, ipAddress, pathInfo,defaultStorageTime , partitionPlan);

      if (partitionPlan == null) {
        this.log.warn("Parameter partitionPlan was null in EditPartitionPlan.");
      }

      
      if (defaultStorageTime == null) {
        this.log.warn("Parameter defaultStorageTime was null in EditPartitionPlan.");
      }
	  final Pattern p = Pattern.compile("^[0-9]+$");
      final Matcher match = p.matcher(defaultStorageTime);
      if (match.find()) {
    	  log.info("Parameter defaultStorageTime is matching with the regex. ");
      }else {
    	  log.warn("Parameter defaultStorageTime is not matching with the regex, redirecting to Logout");
    	  request.getRequestDispatcher("/servlet/Logout").forward(request, response);
      }
      final Partitionplan wherePartitionPlan = new Partitionplan(dwhRepRockFactory);
      wherePartitionPlan.setPartitionplan(partitionPlan);
      final PartitionplanFactory partitionPlanFactory = new PartitionplanFactory(dwhRepRockFactory, wherePartitionPlan);
      dwhRepRockFactory.getConnection().commit();

      final List<Partitionplan> partitionPlanVector = partitionPlanFactory.get();

      if (partitionPlanVector.size() > 0) {

        final Partitionplan targetPartitionPlan = (Partitionplan) partitionPlanVector.get(0);
        targetPartitionPlan.setDefaultstoragetime(new Long(defaultStorageTime));
        targetPartitionPlan.updateDB();
        
        // now jump to "DWH Configuration" servlet
        final ShowPartitionPlan spp = new ShowPartitionPlan();
        try {
          spp.init(this.getServletConfig());
          ctx.put("pp_saved", partitionPlan);
          outty = spp.doHandleRequest(request, response, ctx);
          return outty;
        } catch (ServletException e) {
          log.warn("Exception: ", e);
        }
      } else {
        ctx.put("errorMessage", "Saving partitionplan " + partitionPlan
            + " failed. No partitionplan could not be found.");
      }
    } else {
      // Show the edit form for partitionplan. Also show the TypeActivations
      // that uses this partition plan.
      final String partitionPlanName = request.getParameter("pp");
	  final Pattern p = Pattern.compile("^[A-Za-z0-9_]*$");
      final Matcher match = p.matcher(partitionPlanName);
      if (match.find()) {
    	  log.info("Parameter pp is matching with the regex. ");
      }else {
    	  log.warn("Parameter pp is not matching with the regex, redirecting to Logout ");
    	  request.getRequestDispatcher("/servlet/Logout").forward(request, response);
      }

      final Partitionplan targetPartitionPlan = getPartitionPlan(partitionPlanName, dwhRepRockFactory);

      if (targetPartitionPlan == null) {
        ctx.put("errorMessage", "Could not find Partition Plan named " + partitionPlanName + ".");
      } else {
        
        String maxStorageTime = "Not defined";
        maxStorageTime = getMaxStorageTime(targetPartitionPlan.getPartitionplan(), dwhRepRockFactory);
        
        ctx.put("maxStorageTime", maxStorageTime);
        log.info("maxStorageTime="+ maxStorageTime);
        ctx.put("defaultStorageTime", targetPartitionPlan.getDefaultstoragetime());
        ctx.put("defaultPartitionSize", targetPartitionPlan.getDefaultpartitionsize());
        ctx.put("partitionPlanName", targetPartitionPlan.getPartitionplan());
        ctx.put("partitionType", targetPartitionPlan.getPartitiontype());
      }

      final Typeactivation whereTypeActivation = new Typeactivation(dwhRepRockFactory);
      whereTypeActivation.setPartitionplan(partitionPlanName);
      final TypeactivationFactory typeActivationFactory = new TypeactivationFactory(dwhRepRockFactory, whereTypeActivation,
          " ORDER BY techpack_name, typename;");

      final List<Typeactivation> typeActivations = typeActivationFactory.get();

      ctx.put("typeActivations", typeActivations);
    }

    outty = getTemplate(page);

    return outty;
  }

  /**
   * This function returns the PartitionPlan object identified by parameter
   * partitionPlanName.
   * 
   * @param partitionPlanName
   *          is the name of the PartitionPlan.
   * @param dwhRepRockFactory
   *          Dwhrep database RockFactory to use.
   * @return Returns Loaded Partitionplan object. In case of error returns null.
   */
  private Partitionplan getPartitionPlan(final String partitionPlanName, final RockFactory dwhRepRockFactory) {

    try {
      final Partitionplan wherePartitionPlan = new Partitionplan(dwhRepRockFactory);
      wherePartitionPlan.setPartitionplan(partitionPlanName);

      final PartitionplanFactory partitionPlanFactory = new PartitionplanFactory(dwhRepRockFactory, wherePartitionPlan);
      final List<?> partitionPlanVector = partitionPlanFactory.get();

      if (partitionPlanVector.size() == 0) {
        this.log.warn("Could not find partitionplan named " + partitionPlanName + ".");
        return null;
      } else {
        return (Partitionplan) partitionPlanVector.get(0);
      }

    } catch (Exception e) {
      this.log.warn("Getting partitionplan failed for partitionplan named " + partitionPlanName + ".");
      return null;
    }

  }
  
  /**
   * This function returns the default maxstoragetime of the partitionplan given as a parameter.
   * 
   * @param partitionPlan Name of the PartitionPlan.
   * @param dwhrepRockFactory Rockfactory to use in database queries.
   * @return Returns the value of maxstoragetime.
   */
  private String getMaxStorageTime(final String partitionPlan, final RockFactory dwhrepRockFactory) {

    try {
      final Partitionplan wherePartitionPlan = new Partitionplan(dwhrepRockFactory);
      wherePartitionPlan.setPartitionplan(partitionPlan);
      final PartitionplanFactory partitionPlanFactory = new PartitionplanFactory(dwhrepRockFactory, wherePartitionPlan);
      final List<Partitionplan> partitionPlanVect = partitionPlanFactory.get();
      
      if(partitionPlanVect.size() == 0) {
        this.log.warn("No maxstoragetime found for partitionplan " + partitionPlan + ".");
        return "";
      } else {
        final Partitionplan targetPartitionPlan = (Partitionplan) partitionPlanVect.get(0);
        
        return targetPartitionPlan.getMaxstoragetime().toString();
      }
    } catch(Exception e) {
      this.log.warn("Failed to get maxstoragetime.", e);
      return "";
    }
  }


}



