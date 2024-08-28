package com.distocraft.dc5000.etl.gui.activation;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.config.Source;
import com.distocraft.dc5000.etl.gui.monitor.Util;
import com.distocraft.dc5000.repository.dwhrep.Dwhpartition;
import com.distocraft.dc5000.repository.dwhrep.DwhpartitionFactory;
import com.distocraft.dc5000.repository.dwhrep.Partitionplan;
import com.distocraft.dc5000.repository.dwhrep.PartitionplanFactory;
import com.distocraft.dc5000.repository.dwhrep.Tpactivation;
import com.distocraft.dc5000.repository.dwhrep.TpactivationFactory;
import com.distocraft.dc5000.repository.dwhrep.Typeactivation;
import com.distocraft.dc5000.repository.dwhrep.TypeactivationFactory;
import com.distocraft.etl.gui.info.AdminuiInfo;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * dc5000rep database is used from this class.
 * 
 * @author Jaakko Melantie, Janne Berggren
 */
public class TypeActivationEdit extends EtlguiServlet {

  private static final long serialVersionUID = 1L;

  private final transient Log log = LogFactory.getLog(this.getClass());

  private String engineHost = null;

  private String enginePort = null;

  private String engineServiceName = null;

  /**
   * @param request
   * @param response
   * @param context
   * @return template
   * @throws RockException 
   * @throws SQLException 
   * @throws ServletException 
   */
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, final Context ctx) throws SQLException, RockException, ServletException {

    Template outty = null;
    String page = "typeActivationEdit.vm";

    final HttpSession session = request.getSession(false);

    final RockFactory rock = (RockFactory) ctx.get("rockDwhRep");

    final String tp = request.getParameter("tp");
    final String save = request.getParameter("save");

    final String typeName = request.getParameter("tpn");
    final String level = request.getParameter("level");
    final String status = request.getParameter("status");
    final String st = request.getParameter("st");
    final String type = request.getParameter("type");
    
    final HttpSession session1 = request.getSession();
    String username =  (String) session1.getAttribute("username");
	log.info("user in TypeActivatinEdit="+username);
	
	String pathInfo =request.getRequestURI();
	log.info("path Info of  TypeActivationEdit="+pathInfo);
    String ipAddress = request.getRemoteAddr();
    log.info("IpAddress in TypeActivationEdit="+ipAddress);

    if (tp == null) {
      ctx.put("tp", "-");
    } else if (tp.equals("-")) {
      ctx.put("tp", "-");
    } else {
      ctx.put("tp", tp);
    }

    final List<String> tps = Util.getAllTechPacks(rock.getConnection());
    ctx.put("distinctTechPacks", tps);

    if (save == null) {

      final List<Dwhpartition> partitionInfo = getPartitionInfo(rock, typeName, level);
      
      final String partitionPlan = getPartitionplanName(typeName, level, type, rock);
      final String usedStorageTime = getStorageTime(typeName, level, type, rock);


      if(usedStorageTime.equalsIgnoreCase("-1")) {
        ctx.put("useDefaultStorageTime", "true");
      } else {
        ctx.put("useDefaultStorageTime", "false");
      }
      
      
      String defaultStorageTime = "Not defined";
      String maxStorageTime = "Not defined";
      Short partitionType = 0;
      
      if(partitionPlan != null) {
        if(!partitionPlan.equalsIgnoreCase("")) {
          final Partitionplan pp = this.getPartitionplan(partitionPlan, rock);
          defaultStorageTime = pp.getDefaultstoragetime().toString();
          maxStorageTime = pp.getMaxstoragetime().toString();
          partitionType = pp.getPartitiontype();
        }
      }
      
      ctx.put("partitionPlan", partitionPlan);
      ctx.put("defaultStorageTime", defaultStorageTime);
      ctx.put("maxStorageTime", maxStorageTime);
      ctx.put("partitionType", partitionType);
      
      ctx.put("partInfo", partitionInfo);
      ctx.put("tpn", typeName);
      ctx.put("level", level);
      ctx.put("status", status);
      ctx.put("st", st);
      ctx.put("type", type);
      page = "typeActivationEdit.vm";
    } else {
      Long usedStorageTime = Long.valueOf(0L);
      final String defaultStorageTime = request.getParameter("defaultStorageTimeValue");
      log.info("Typename="+typeName);

      log.info("defaultStorageTimeValue="+defaultStorageTime);
      
      if(request.getParameterMap().containsKey("useDefaultStorageTime")) {
        
        try {
          usedStorageTime = Long.valueOf(-1L);;
        } catch (Exception e) {
          this.log.warn("Failed to convert storage time " + defaultStorageTime + " to type Long. Using value 0.");
        }
        
      } else {
        try {
          usedStorageTime = new Long(st);
          log.info("storage Time changed ="+ st);
          AdminuiInfo.logTypeActivation(username,ipAddress,pathInfo,defaultStorageTime,st);
        } catch (Exception e) { 
          this.log.warn("Failed to convert storage time " + st + " to type Long. Using value 0.");
        }
      }
      
      updateType(rock.getConnection(), typeName, level, status, usedStorageTime.toString());

      // reload engine config
      init(this.getServletConfig());
      final boolean success = reloadEngineConfig();

      if (!success) {
        ctx.put("errorMessage", "Engine configuration reload failed.");
      }

      final Tpactivation techPackInfo = getTPActivation(rock, tp);
      final List<List<Object>> MeasTypes = getTypeActivation(rock, tp);

      final List<String> TableLevels = Util.getTablelevels(rock.getConnection());

      // List MeasTypes = getTypeActivation (repCon, meas_group);
      ctx.put("selectedlevel", level);
      ctx.put("tablelevels", TableLevels);

      ctx.put("measTypes", MeasTypes);
      ctx.put("tpInfo", techPackInfo);
      // put flag to session that type activation is edited
      session.setAttribute("typeActivationSaved", "1");
      ctx.put("typeSaved", session.getAttribute("typeActivationSaved"));
      log.debug("Type Saved: " + session.getAttribute("typeActivationSaved"));
      page = "typeActivation.vm";

    } 
    

    try {
      outty = getTemplate(page);
    } catch (Exception e) {
      throw new VelocityException(e);
    }

    return outty;
  }

  private void updateType(final Connection conn, final String tpn, final String level, final String status, final String st) throws SQLException {

    Statement statement = null; // sql statement

    try {

      final String sql = "UPDATE TypeActivation SET status = '" + status + "', storagetime = '" + st + "' "
          + "WHERE typename = '" + tpn + "' AND tablelevel = '" + level + "'";

      log.debug(sql);

      statement = conn.createStatement();

      statement.executeUpdate(sql);

    } finally {
      conn.commit();
      try {
        statement.close();
      } catch (Exception e) {
        log.error("Exception ", e);
      }
    }
  }

  /**
   * Get all partitions info for this measurement type and level
   * 
   * @param rockFact
   * @param type_name
   * @param level
   * @return
   * @throws RockException 
   * @throws SQLException 
   */
  private List<Dwhpartition> getPartitionInfo(final RockFactory rockFact, final String type_name, final String level) throws SQLException, RockException {

    final Dwhpartition whereRef = new Dwhpartition(rockFact);
    whereRef.setStorageid(type_name + ":" + level);
    final DwhpartitionFactory dwp = new DwhpartitionFactory(rockFact, whereRef, "ORDER BY tablename");

    final Vector<Dwhpartition> dbVec = dwp.get();

    return dbVec;
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

      final List<Object> l = new ArrayList<Object>();
      final Typeactivation tp = (Typeactivation) dbVec.elementAt(i);

      boolean useDefaultStorageTime = false;
      if (tp.getStoragetime() == null || tp.getStoragetime().longValue() == -1) {
        useDefaultStorageTime = true;
      }

      l.add(tp.getStatus());
      l.add(tp.getTypename());
      l.add(tp.getTablelevel());

      final Partitionplan pp = getPartitionplan(tp.getPartitionplan(), rockFact);
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
   * This function returns the name of the partitionplan TypeActivation uses.
   * @param typeName TypeName of the TypeActivation.
   * @param level TableLevel of the TypeActivation.
   * @param type Type of the TypeActivation.
   * @return Returns the name of the partitionplan Typeactivation uses.
   */
  private String getPartitionplanName(final String typeName, final String level, final String type, final RockFactory dwhrepRockFactory) {

    try {
      final Typeactivation whereTypeActivation = new Typeactivation(dwhrepRockFactory);
      whereTypeActivation.setTypename(typeName);
      whereTypeActivation.setTablelevel(level);
      whereTypeActivation.setType(type);
      final TypeactivationFactory typeActivationFact = new TypeactivationFactory(dwhrepRockFactory, whereTypeActivation);
      final Vector<Typeactivation> typeActivationsVect = typeActivationFact.get();
      
      if(typeActivationsVect.size() == 0) {
        this.log.warn("No partitionplan found for TypeActivation with typeName " + typeName + ".");
        return "";
      } else {
        final Typeactivation targetTypeActivation = (Typeactivation)typeActivationsVect.get(0);
        final String partitionPlan = targetTypeActivation.getPartitionplan();
        
        return partitionPlan;
      }
    } catch (Exception e) {
      this.log.warn("Getting TypeActivation's partitionplan failed.", e);
      return "";
    }
  }
  
  /**
   * This function returns the value of storagetime for a TypeActivation.
   * @param typeName TypeName of the TypeActivation.
   * @param level TableLevel of the TypeActivation.
   * @param type Type of the TypeActivation.
   * @param rock Dwhrep Rockfactory to be used.
   * @return Returns the value of storagetime. In case of error, empty String is returned.
   */
  private String getStorageTime(final String typeName, final String level, final String type, final RockFactory dwhrepRockFactory) {
    try {
      final Typeactivation whereTypeActivation = new Typeactivation(dwhrepRockFactory);
      whereTypeActivation.setTypename(typeName);
      whereTypeActivation.setTablelevel(level);
      whereTypeActivation.setType(type);
      final TypeactivationFactory typeActivationFact = new TypeactivationFactory(dwhrepRockFactory, whereTypeActivation);
      final Vector<Typeactivation> typeActivationsVect = typeActivationFact.get();
      
      if(typeActivationsVect.size() == 0) {
        this.log.warn("No partitionplan found for TypeActivation with typeName " + typeName + ".");
        return "";
      } else {
        final Typeactivation targetTypeActivation = (Typeactivation)typeActivationsVect.get(0);
        final Long storageTime = targetTypeActivation.getStoragetime();
        
        return storageTime.toString();
      }
    } catch (Exception e) {
      this.log.warn("Getting TypeActivation's storagetime failed.", e);
      return "";
    }

  }
  
  
  /**
   * This function returns the PartitionPlan object.
   * 
   * @param partitionPlan
   *          Name of the Partitionplan.
   * @return Returns the object by Partitionplan name. If no plan is found, null is returned.
   */
  private Partitionplan getPartitionplan(final String partitionPlan, final RockFactory dwhrepRockFactory) {

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



