package com.distocraft.dc5000.etl.gui.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.repository.dwhrep.Partitionplan;
import com.distocraft.dc5000.repository.dwhrep.PartitionplanFactory;

/**
 * This servlet shows the contents of the dwhrep database table PartitionPlan.
 * 
 * @author Janne Berggren
 */
public class ShowPartitionPlan extends EtlguiServlet {

  private Log log = LogFactory.getLog(this.getClass()); // general logger

  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
      throws Exception {

    Template outty = null;

    final String page = "showPartitionPlan.vm";

    RockFactory dwhRepRockFactory = (RockFactory) ctx.get("rockDwhRep");

    // Get all contents of PartitionPlan table.
    Partitionplan wherePartitionPlan = new Partitionplan(dwhRepRockFactory);
    PartitionplanFactory partitionPlanFactory = new PartitionplanFactory(dwhRepRockFactory, wherePartitionPlan, " ORDER BY PARTITIONPLAN;");
    
    Vector partitionPlans = partitionPlanFactory.get();
    
    if(ctx.containsKey("pp_saved")) {
      ctx.put("message", "Partition plan " + ctx.get("pp_saved") + " saved succesfully.");
      //ctx.put("pp_saved", request.getParameter("pp_saved"));
    }
    
    ctx.put("partitionPlans", partitionPlans);
    outty = getTemplate(page);

    return outty;
  }

  /**
   * Read properties
   * 
   */
  private Properties getProperties(File propFile) {

    Properties prop = new Properties();
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

  private boolean savePropertyFile(Properties prop, File propFile) throws IOException {

    FileOutputStream out = new FileOutputStream(propFile);

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


}
