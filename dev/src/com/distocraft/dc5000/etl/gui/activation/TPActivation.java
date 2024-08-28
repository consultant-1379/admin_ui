package com.distocraft.dc5000.etl.gui.activation;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

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
import org.apache.velocity.exception.VelocityException;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.config.Source;
import com.distocraft.dc5000.etl.gui.monitor.Util;
import com.distocraft.dc5000.repository.dwhrep.Partitionplan;
import com.distocraft.dc5000.repository.dwhrep.PartitionplanFactory;
import com.distocraft.dc5000.repository.dwhrep.Tpactivation;
import com.distocraft.dc5000.repository.dwhrep.TpactivationFactory;
import com.distocraft.dc5000.repository.dwhrep.Typeactivation;
import com.distocraft.dc5000.repository.dwhrep.TypeactivationFactory;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * dc5000rep database is used from this class.
 * 
 * @author Jaakko Melantie
 */
public class TPActivation extends EtlguiServlet {  // NOPMD by eheijun on 02/06/11 14:21

  private static final long serialVersionUID = 1L;

  private transient final Log log = LogFactory.getLog(this.getClass());

  private String engineHost = null;

  private String enginePort = null;

  private String engineServiceName = null;

  private transient RockFactory dwhrepRockFactory = null;

  /**
   * @param request
   * @param response
   * @param context
   * @return template
   * @throws SQLException
   * @throws ServletException
   * @throws RockException
   */
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, // NOPMD by eheijun on 02/06/11 14:21
      final Context ctx) throws SQLException, ServletException, RockException {

    Template outty = null;
    final HttpSession session = request.getSession(false);

    dwhrepRockFactory = (RockFactory) ctx.get("rockDwhRep");

     String meas_group = request.getParameter("tp");
    String level = request.getParameter("level");
    String pattern = "^[a-zA-Z0-9---_]*$";;
    
    if(meas_group == null){
		  meas_group = "-";
	  }
	    if(level == null){
	    	level = "-";
	    }

	    if(level.matches(pattern)){
	       	level = StringEscapeUtils.escapeHtml(level);
	       }else{
	       	level = "-";
	       }
	       


	   if(meas_group.matches(pattern)){
	       	meas_group = StringEscapeUtils.escapeHtml(meas_group);
	       }else{
	       	meas_group = null;
	       }
	      
    
    final String activateSelected = StringEscapeUtils.escapeHtml(request.getParameter("activateSelected"));
    final String inActivateSelected = StringEscapeUtils.escapeHtml(request.getParameter("inActivateSelected"));

    final Map<String, String> params = new HashMap<String, String>();

    final Enumeration<?> parameters = request.getParameterNames();

    // get all parameters to map

    while (parameters.hasMoreElements()) {
      final String par = (String) parameters.nextElement();
      params.put(par, request.getParameter(par));
    }

    if (meas_group == null) {
      ctx.put("tp", "-");
    } else if (meas_group.equals("-")) {
      ctx.put("tp", "-");
    } else {
      ctx.put("tp", meas_group);
    }
    


    if (dwhrepRockFactory == null) {
      ctx.put("errorMessage", "Please ensure all databases are online.");
      log.debug("DwhrepRockFactory Connection is " + dwhrepRockFactory);
    } else {
      List<String> tps = Util.getAllTechPacks(dwhrepRockFactory.getConnection());
      Collections.sort(tps);
      ctx.put("distinctTechPacks", tps);

      log.debug("meas_group" + meas_group);
      log.debug("level" + level);

      if (meas_group != null || level != null) {

        // If activating or inactivate button pressed
        if (activateSelected != null || inActivateSelected != null) {

          if (params.size() > 0) {

            final Iterator<Entry<String, String>> it = params.entrySet().iterator();

            while (it.hasNext()) {

              final Entry<String, String> entry = it.next();

              if (entry.getKey().startsWith("chk:")) {

                final String[] temp = entry.getKey().split(":");
                final String tn = temp[1];
                final String tl = temp[2];

                if (params.get(entry.getKey()).toString().equalsIgnoreCase("on")) {
                  if (activateSelected != null) {
                    updateTypeActivationStatus(dwhrepRockFactory.getConnection(), tn, tl, "ACTIVE");
                  }
                  if (inActivateSelected != null) {
                    updateTypeActivationStatus(dwhrepRockFactory.getConnection(), tn, tl, "INACTIVE");
                  }
                }
              }
            }
            init(this.getServletConfig());
            final boolean success = reloadEngineConfig();

            if (!success) {
              ctx.put("errorMessage", "Engine configuration reload failed.");
            }
          }

        }

        Tpactivation techPackInfo;
        List<List<Object>> measTypes;

        if (meas_group != null && !meas_group.equals("-") && level != null && !level.equals("-") && level != "") {
          log.debug("meas_group and level selected.");
          techPackInfo = getTPActivation(dwhrepRockFactory, meas_group);
          measTypes = getTechPackTablelevelTypeActivation(dwhrepRockFactory, meas_group, level);
        } else if ((meas_group == null || meas_group.equals("-")) && level != null && !level.equals("-") && level != "") {
          log.debug("only level selected.");
          techPackInfo = null;
          measTypes = getTablelevelTypeActivation(dwhrepRockFactory, level);
        } else {
          log.debug("only meas group selected.");
          techPackInfo = getTPActivation(dwhrepRockFactory, meas_group);
          measTypes = getTypeActivation(dwhrepRockFactory, meas_group);
        }

        final List<List<Object>> timeBasedMeasTypes = new ArrayList<List<Object>>();
        final List<List<Object>> volumeBasedMeasTypes = new ArrayList<List<Object>>();
        for (List<Object> measType : measTypes) {
          if (measType.get(6) instanceof Short) {
            final Short pt = (Short) measType.get(6);
            if (pt.equals(1)) {
              volumeBasedMeasTypes.add(measType);
            } else {
              timeBasedMeasTypes.add(measType);
            }
          }
        }
        ctx.put("timeBasedMeasTypes", timeBasedMeasTypes);
        ctx.put("volumeBasedMeasTypes", volumeBasedMeasTypes);
        ctx.put("tpInfo", techPackInfo);
      }

    }

    final List<String> TableLevels = Util.getTablelevels(dwhrepRockFactory.getConnection());

    log.debug("Tablelevel size: " + TableLevels.size());

    // List MeasTypes = getTypeActivation (repCon, meas_group);
    ctx.put("selectedlevel", level);
    ctx.put("tablelevels", TableLevels);

    ctx.put("typeSaved", session.getAttribute("typeActivationSaved"));

    log.debug("Type Saved session attribute: " + session.getAttribute("typeActivationSaved"));

    try {
      outty = getTemplate("typeActivation.vm");
    } catch (Exception e) {
      throw new VelocityException(e);
    }

    return outty;
  }

  private void updateTypeActivationStatus(final Connection repCon, final String tn, final String tl, final String status)
      throws SQLException {

    Statement statement = null; // sql statement

    try {

      final String sql = "UPDATE TypeActivation SET status = '" + status + "' WHERE typename = '" + tn
          + "' AND tablelevel = '" + tl + "'";

      log.debug(sql);

      statement = repCon.createStatement();

      statement.executeUpdate(sql);

      repCon.commit();

    } finally {

      try {
        statement.close();
      } catch (Exception e) {
        log.error("Exception ", e);
      }
    }
  }

  /**
   * 
   * List all types which has this tablelevel
   * 
   * @param rockFact
   * @param tablelevel
   * @return
   * @throws SQLException
   * @throws RockException
   */
  private List<List<Object>> getTablelevelTypeActivation(final RockFactory rockFact, final String tablelevel)
      throws SQLException, RockException {

    final List<List<Object>> retval = new ArrayList<List<Object>>();

    final Typeactivation whereRef = new Typeactivation(rockFact);
    whereRef.setTablelevel(tablelevel);
    final TypeactivationFactory tpa = new TypeactivationFactory(rockFact, whereRef, "ORDER BY typename");

    final Vector<Typeactivation> dbVec = tpa.get();

    for (int i = 0; i < dbVec.size(); i++) {

      final List<Object> l = new ArrayList<Object>(); // NOPMD by eheijun on 02/06/11 14:21
      final Typeactivation tp = (Typeactivation) dbVec.elementAt(i);

      boolean useDefaultStorageTime = false;
      if (tp.getStoragetime() == null || tp.getStoragetime().longValue() == -1) {
        useDefaultStorageTime = true;
      }

      l.add(tp.getStatus());
      l.add(tp.getTypename());
      l.add(tp.getTablelevel());

      final Partitionplan pp = getPartitionplan(rockFact, tp.getPartitionplan());
      Long defaultStorageTime = -1L;
      Short partitionType = 0;
      if (pp != null) {
        defaultStorageTime = pp.getDefaultstoragetime();
        partitionType = pp.getPartitiontype();
      }
      if (useDefaultStorageTime) {
        l.add(defaultStorageTime);
      } else {
        l.add(tp.getStoragetime());
      }

      l.add(tp.getType());

      if (useDefaultStorageTime) {
        l.add("true");
      } else {
        l.add("false");
      }

      l.add(partitionType);

      retval.add(l);
    }
    return retval;
  }

  /**
   * 
   * List all types which has this tablelevel in this tech pack
   * 
   * @param rockFact
   * @param tablelevel
   * @return
   * @throws SQLException
   * @throws RockException
   */
  private List<List<Object>> getTechPackTablelevelTypeActivation(final RockFactory rockFact,
      final String techpack_name, final String tablelevel) throws SQLException, RockException {

    final List<List<Object>> retval = new ArrayList<List<Object>>();

    final Typeactivation whereRef = new Typeactivation(rockFact);
    whereRef.setTablelevel(tablelevel);
    whereRef.setTechpack_name(techpack_name);
    final TypeactivationFactory tpa = new TypeactivationFactory(rockFact, whereRef, "ORDER BY typename");

    final Vector<Typeactivation> dbVec = tpa.get();

    for (int i = 0; i < dbVec.size(); i++) {

      final List<Object> l = new ArrayList<Object>(); // NOPMD by eheijun on 02/06/11 14:22
      final Typeactivation tp = (Typeactivation) dbVec.elementAt(i);

      boolean useDefaultStorageTime = false;
      if (tp.getStoragetime() == null || tp.getStoragetime().longValue() == -1) {
        useDefaultStorageTime = true;
      }

      l.add(tp.getStatus());
      l.add(tp.getTypename());
      l.add(tp.getTablelevel());

      final Partitionplan pp = getPartitionplan(rockFact, tp.getPartitionplan());
      Long defaultStorageTime = -1L;
      Short partitionType = 0;
      if (pp != null) {
        defaultStorageTime = pp.getDefaultstoragetime();
        partitionType = pp.getPartitiontype();
        ;
      }
      if (useDefaultStorageTime) {
        l.add(defaultStorageTime);
      } else {
        l.add(tp.getStoragetime());
      }

      l.add(tp.getType());

      if (useDefaultStorageTime) {
        l.add("true");
      } else {
        l.add("false");
      }

      l.add(partitionType);

      retval.add(l);
    }
    return retval;
  }

  /**
   * 
   * List all types for this tech pack
   * 
   * @param rockFact
   * @param tech
   *          pack name
   * @return
   * @throws SQLException
   * @throws RockException
   */
  private List<List<Object>> getTypeActivation(final RockFactory rockFact, final String techpack_name) throws SQLException, RockException {

    final List<List<Object>> retval = new ArrayList<List<Object>>();

    final Typeactivation whereRef = new Typeactivation(rockFact);
    whereRef.setTechpack_name(techpack_name);
    final TypeactivationFactory tpa = new TypeactivationFactory(rockFact, whereRef, "ORDER BY typename");

    final Vector<Typeactivation> dbVec = tpa.get();

    for (int i = 0; i < dbVec.size(); i++) {

      final List<Object> l = new ArrayList<Object>(); // NOPMD by eheijun on 02/06/11 14:22
      final Typeactivation tp = (Typeactivation) dbVec.elementAt(i);

      boolean useDefaultStorageTime = false;
      if (tp.getStoragetime() == null || tp.getStoragetime().longValue() == -1) {
        useDefaultStorageTime = true;
      }

      l.add(tp.getStatus());
      l.add(tp.getTypename());
      l.add(tp.getTablelevel());

      final Partitionplan pp = getPartitionplan(rockFact, tp.getPartitionplan());
      Long defaultStorageTime = -1L;
      Short partitionType = 0;
      if (pp != null) {
        defaultStorageTime = pp.getDefaultstoragetime();
        partitionType = pp.getPartitiontype();
        ;
      }
      if (useDefaultStorageTime) {
        l.add(defaultStorageTime);
      } else {
        l.add(tp.getStoragetime());
      }

      l.add(tp.getType());

      if (useDefaultStorageTime) {
        l.add("true");
      } else {
        l.add("false");
      }

      l.add(partitionType);

      retval.add(l);
    }
    return retval;
  }

  private Tpactivation getTPActivation(final RockFactory rockFact, final String techpack_name)
      throws SQLException, RockException {

    final Tpactivation whereRef = new Tpactivation(rockFact);
    whereRef.setTechpack_name(techpack_name);
    final TpactivationFactory tpa = new TpactivationFactory(rockFact, whereRef, "");

    final Vector<Tpactivation> dbVec = tpa.get();

    if (!dbVec.isEmpty()) {
      return dbVec.firstElement();
    }
    return null;
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

      //log.debug("rmi://" + engineHost + ":" + enginePort + "/" + engineServiceName);
      final ITransferEngineRMI trRMI = (ITransferEngineRMI) Naming.lookup(RmiUrlFactory.getInstance().getEngineRmiUrl());

      // start the set
      trRMI.reloadProperties();

      // aaaaaand we are done....
      reloaded = true;

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

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
   */

  public void init(final ServletConfig arg0) throws ServletException {
    super.init(arg0);

    final Source configFileSource = new Source("etlgui", arg0.getServletContext().getRealPath("/"));
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
  }

  /**
   * This function returns the PartitionPlan object.
   * 
   * @param partitionPlan
   *          Name of the Partitionplan.
   * @return Returns the object by Partitionplan name. If no plan is found, null is returned.
   */
  private Partitionplan getPartitionplan(final RockFactory dwhrepRockFactory, final String partitionPlan) {

    final Partitionplan wherePartitionPlan = new Partitionplan(dwhrepRockFactory);
    wherePartitionPlan.setPartitionplan(partitionPlan);

    try {
      final PartitionplanFactory partPlanFact = new PartitionplanFactory(dwhrepRockFactory, wherePartitionPlan);
      final Vector<Partitionplan> partPlanVect = partPlanFact.get();

      if (partPlanVect.size() > 0) {
        final Partitionplan targetPartitionPlan = (Partitionplan) partPlanVect.get(0);

        return targetPartitionPlan;

      } else {
        log.info("Failed to get default storagetime.");
        return null;
      }

    } catch (Exception e) {
      log.info("Failed to get default storagetime.", e);
      return null;
    }

  }

}