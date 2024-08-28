package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.rmi.Naming;
import java.rmi.Remote;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.util.Helper;

/**
 * Copyright &copy; Ericsson ltd. All rights reserved.<br>
 * This server shows details of the engine status.<br>
 * @author Janne Berggren
 */
public class EngineStatusDetails extends EtlguiServlet {

  private Log log = LogFactory.getLog(this.getClass());
  
  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) {
    Template page = null;
    final String usePage = "engine_status_details.vm"; 

    ctx.put("engineStatusDetails", getEngineStatusDetails());
    // finally generate page
    try {
      page = getTemplate(usePage);
    } catch (ResourceNotFoundException e) {
      log.error("ResourceNotException",e);
    } catch (ParseErrorException e) {
      log.error("ParseErrorException",e);
    } catch (Exception e) {
      log.error("Exception",e);
    }

    // and return with the template
    return page;
  }

  public MonitorInformation getEngineStatusDetails() {
    MonitorInformation monitorInformation = new MonitorInformation();
    ITransferEngineRMI transferEngineRmi;
    try {
      transferEngineRmi = (ITransferEngineRMI) Naming.lookup(RmiUrlFactory.getInstance().getEngineRmiUrl());
      List status = (List) transferEngineRmi.status();

      monitorInformation.setFieldName(MonitorInformation.TITLE_ETLENGINE);
      monitorInformation.setCurrProfile(((String) status.get(9)));
      
      if ("NoLoads".equalsIgnoreCase(monitorInformation.getCurrProfileName())){
        monitorInformation.setStatus(MonitorInformation.BULB_YELLOW);
      } else if (((String) status.get(4)).trim().equals(MonitorInformation.STATUS_ENGINE_STRING_OK)) {
        monitorInformation.setStatus(MonitorInformation.BULB_GREEN);
      } else {
        monitorInformation.setStatus(MonitorInformation.BULB_YELLOW);
      }
      monitorInformation.setUptime(((String) status.get(1)));
      monitorInformation.setTotMem("Total Memory: "
          + MonitorInformation.transformBytesToMegas((String) status.get(14)) + " Mb.");
      monitorInformation.setSize("Priority queue " + ((String) status.get(5)).toLowerCase());

      final Iterator iterator = status.iterator();

      while (iterator.hasNext()) {
        monitorInformation.setMessage(((String) iterator.next()) + "<br />");
      }

    } catch (Exception e) {
      monitorInformation.setStatus(MonitorInformation.BULB_RED);
      monitorInformation.setFieldName(MonitorInformation.TITLE_ETLENGINE);
      log.error("Exception ", e);
    }
    return monitorInformation;
    
  }

 
}
