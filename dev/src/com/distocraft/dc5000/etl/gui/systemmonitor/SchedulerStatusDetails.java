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

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.util.Helper;
import com.distocraft.dc5000.etl.scheduler.ISchedulerRMI;

/**
 * Copyright &copy; Ericsson ltd. All rights reserved.<br>
 * This server shows details of the status status.<br>
 * 
 * @author Janne Berggren
 */
public class SchedulerStatusDetails extends EtlguiServlet {

  private Log log = LogFactory.getLog(this.getClass());

  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) {
    Template page = null;
    final String usePage = "scheduler_status_details.vm";

    ctx.put("schedulerStatusDetails", getSchedulerStatusDetails());
    // finally generate page
    try {
      page = getTemplate(usePage);
    } catch (ResourceNotFoundException e) {
      log.error("ResourceNotException", e);
    } catch (ParseErrorException e) {
      log.error("ParseErrorException", e);
    } catch (Exception e) {
      log.error("Exception", e);
    }

    // and return with the template
    return page;
  }

  public MonitorInformation getSchedulerStatusDetails() {
    MonitorInformation monitorInformation = new MonitorInformation();
    ISchedulerRMI schedulerRmi;
    try {
      schedulerRmi = (ISchedulerRMI) Naming.lookup(RmiUrlFactory.getInstance().getSchedulerRmiUrl());
      List status = (List) schedulerRmi.status();
      monitorInformation.setFieldName(MonitorInformation.TITLE_ETLSCHEDULER);
      if (((String) status.get(1)).trim().equals(MonitorInformation.STATUS_SCHEDULER_STRING_OK)) {
        monitorInformation.setStatus(MonitorInformation.BULB_GREEN);
      } else {
        monitorInformation.setStatus(MonitorInformation.BULB_YELLOW);
      }
      
      monitorInformation.setPollInterval(((String) status.get(2)));
      Iterator iterator = status.iterator();
      while (iterator.hasNext()) {
        monitorInformation.setMessage(((String) iterator.next()) + "<br />");
      }
    } catch (Exception e) {
      monitorInformation.setStatus(MonitorInformation.BULB_RED);
      monitorInformation.setFieldName(MonitorInformation.TITLE_ETLSCHEDULER);
      log.error("Exception ", e);
    }
    return monitorInformation;

  }

}
