package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.CommandRunner;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

public class ServerStatus extends EtlguiServlet {

  private Log log = LogFactory.getLog(this.getClass());

  private Context context = null;

  private Hashtable requestedSystemMonitorNames = new Hashtable();

  private Hashtable requestedSystemMonitorStatutes = new Hashtable();

  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context context)
      throws Exception {
    try {
      this.context = context;

      //String action = request.getParameter("action");
      Map requestParameters = request.getParameterMap();
      Set requestParameterNames = requestParameters.keySet();
      Iterator requestParameterNamesIterator = requestParameterNames.iterator();

      // First get the different system monitors requested by the server.
      while (requestParameterNamesIterator.hasNext()) {
        String parameterName = (String) requestParameterNamesIterator.next();
        log.debug(parameterName + " = " + request.getParameter(parameterName));
        String[] splittedParameterName = parameterName.split("\\.");

        log.debug("splittedParameterName.length = " + splittedParameterName.length);

        if (splittedParameterName.length == 3) {
          if (splittedParameterName.length > 1) {
            String systemMonitorName = splittedParameterName[1];
            requestedSystemMonitorNames.put(systemMonitorName, new Properties());
            requestedSystemMonitorStatutes.put(systemMonitorName, new String());
          }
        } else {
          log.debug("Parameter name was not splitted to three parts.");
        }
      }

      // Iterate through every system monitor name and get related parameters from the query string.
      Set requestedSystemMonitorNamesSet = requestedSystemMonitorNames.keySet();
      Iterator requestedSystemMonitorNamesIterator = requestedSystemMonitorNamesSet.iterator();
      while (requestedSystemMonitorNamesIterator.hasNext()) {
        String currentSystemMonitorName = (String) requestedSystemMonitorNamesIterator.next();
        requestParameterNamesIterator = requestParameterNames.iterator();
        String systemMonitorType = "";
        while (requestParameterNamesIterator.hasNext()) {
          String parameterName = (String) requestParameterNamesIterator.next();
          String[] splittedParameterName = parameterName.split("\\."); // parameterName can be for example d.system_monitor_name.configuration_name
          // Check if this parameter is related to this system monitor.
          if (splittedParameterName.length == 3
              && currentSystemMonitorName.equalsIgnoreCase(splittedParameterName[1])) {
            systemMonitorType = splittedParameterName[0];
            Properties systemMonitorProperties = (Properties) requestedSystemMonitorNames.get(currentSystemMonitorName);
            String systemMonitorConfigurationName = splittedParameterName[2];
            String systemMonitorConfifurationValue = request.getParameter(parameterName);
            log.debug("Found systemMonitorConfigurationName = " + systemMonitorConfigurationName);
            log.debug("Found systemMonitorConfifurationValue = " + systemMonitorConfifurationValue);

            systemMonitorProperties.setProperty(systemMonitorConfigurationName, systemMonitorConfifurationValue);
            requestedSystemMonitorNames.put(currentSystemMonitorName, systemMonitorProperties);
          }
        }

        String monitorTypeString = "";
        if (systemMonitorType.equalsIgnoreCase("d")) {
          monitorTypeString = "DISK";
        } else {
          log.info("ServerStatus.doHandleRequest: Invalid monitor type in parameters. Monitor type not DISK (\"d\")");
        }
        // Do the actual monitor checking for this system monitor.
        String systemMonitorsStatusColor = getSystemMonitorStatus(monitorTypeString,
            (Properties) requestedSystemMonitorNames.get(currentSystemMonitorName));

        requestedSystemMonitorStatutes.put(currentSystemMonitorName, systemMonitorsStatusColor);

        log.debug("Monitor " + currentSystemMonitorName + " returned color " + systemMonitorsStatusColor);

      }

      String hostName = request.getParameter("hostName");
      String hostType = request.getParameter("hostType");
      if (hostName == null) {
        hostName = "";
      }
      if (hostType == null) {
        hostType = "";
      }

      Template template = new Template();
      template = getTemplate("server_status.vm");

      String serverStatusColor = "GREEN";
      this.context.put("serverStatusColor", serverStatusColor);
      this.context.put("serverMonitorsBulpColor", serverStatusColor.toLowerCase());
      this.context.put("hostType", hostType);
      this.context.put("hostName", hostName);
      this.context.put("systemMonitors", requestedSystemMonitorStatutes);
      return template;
    } catch (Exception e) {
      log.error("ServerStatus.doHandleRequest failed", e);
      return getTemplate("adminuiErrorPage.vm");
    }

  }

  /**
   * This function performs the actual checking of the system monitor. The status color of the system monitor is returned.
   * @param systemMonitorType Type of the system monitor.
   * @param systemMonitorConfiguration Properties object containing the configuration of this system monitor.
   * @return Returns the status color of this system monitor.
   */
  private String getSystemMonitorStatus(String systemMonitorType, Properties systemMonitorConfiguration) {
    try {
      String systemMonitorStatusColor = "red";
      log.debug("Starting check for " + systemMonitorType + " type system monitor.");
      // Do the checking according to the type of the system monitor.
      if (systemMonitorType.equalsIgnoreCase("DISK")) {
        String path = systemMonitorConfiguration.getProperty("path");
        log.debug("path = " + path);
        if (path == null) {
          log.error("Configuration variable path was null. Exiting...");
          return "red";
        }

        Integer threshold = new Integer(systemMonitorConfiguration.getProperty("threshold"));

        String command = "df -h " + path;
        String result = CommandRunner.runCmd(command, log);

        log.debug("Result for command \"" + command + "\":");
        log.debug(result);
        String tempResultString = result.substring(0, result.indexOf("%"));
        log.debug("tempResultString = " + tempResultString);
        String usedSpacePercentageString = tempResultString.substring(tempResultString.lastIndexOf(" ") + 1,
            tempResultString.length());
        log.debug("usedSpacePercentageString = " + usedSpacePercentageString);
        Integer usedSpacePercentage = new Integer(usedSpacePercentageString);
        log.debug("usedSpacePercentage = " + usedSpacePercentage);
        if (usedSpacePercentage.intValue() > threshold.intValue()) {
          systemMonitorStatusColor = "red";
        } else {
          systemMonitorStatusColor = "green";
        }
        log.debug("System monitor got status " + systemMonitorStatusColor);

      } else {
        log.debug("Unknown system monitor type. Returning system monitor status color \"red\".");
        systemMonitorStatusColor = "red";
      }
      return systemMonitorStatusColor;

    } catch (Exception e) {
      log.debug("ServerStatus.getSystemMonitorStatus failed.", e);
      return "red";
    }

  }

}
