package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.rock.Meta_servers;
import com.distocraft.dc5000.etl.rock.Meta_serversFactory;
import com.distocraft.dc5000.etl.rock.Meta_system_monitors;
import com.distocraft.dc5000.etl.rock.Meta_system_monitorsFactory;

public class SystemMonitors extends EtlguiServlet {

  private Log log = LogFactory.getLog(this.getClass());

  private Vector metaServers = new Vector();

  private Context context = null;

  Template template = new Template();

  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, final Context context)
      throws Exception {
    this.context = context;

    final String action = request.getParameter("action");

    //Template template = new Template();
    String message = "";
    final Hashtable metaServerSystemMonitors = new Hashtable();
    final Hashtable metaServerStatusHtmlSources = new Hashtable();

    final String serverMonitorsBulpColor = getSystemMonitorsState();

    if (action != null && action.equalsIgnoreCase("addMetaServer")) {
      // Show the form which adds MetaServer.
      template = getTemplate("edit_meta_server.vm");
    } else if (action != null && action.equalsIgnoreCase("editMetaServer")) {
      // Show the form which edits the selected MetaServer.
      template = getTemplate("edit_meta_server.vm");
      final String hostName = request.getParameter("hostName");
      this.context.put("hostName", hostName);
      final RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");
      final Meta_servers whereMetaServer = new Meta_servers(etlRepRockFactory);
      whereMetaServer.setHostname(hostName);
      final Meta_serversFactory metaServersFactory = new Meta_serversFactory(etlRepRockFactory, whereMetaServer);
      final Vector targetMetaServerVector = metaServersFactory.get();
      Meta_servers targetMetaServer = new Meta_servers(etlRepRockFactory);
      if (targetMetaServerVector.size() == 1) {
        targetMetaServer = (Meta_servers) targetMetaServerVector.get(0);
        this.context.put("type", targetMetaServer.getType());
        this.context.put("statusUrl", targetMetaServer.getStatus_url());
      } else {
        message = "Error editing server.";
      }

    } else if (action != null && action.equalsIgnoreCase("editMetaSystemMonitor")) {
      // Show the form which edits the selected SystemMonitor.
      template = getTemplate("edit_meta_system_monitor.vm");
      final String hostName = request.getParameter("hostName");
      this.context.put("hostName", hostName);
      final String monitor = request.getParameter("monitor");
      this.context.put("monitor", monitor);

      final RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");
      final Meta_system_monitors whereMetaSystemMonitor = new Meta_system_monitors(etlRepRockFactory);

      whereMetaSystemMonitor.setHostname(hostName);
      whereMetaSystemMonitor.setMonitor(monitor);
      final Meta_system_monitorsFactory metaSystemMonitorFactory = new Meta_system_monitorsFactory(etlRepRockFactory,
          whereMetaSystemMonitor);
      final Vector targetMetaSystemMonitorVector = metaSystemMonitorFactory.get();
      Meta_system_monitors targetMetaSystemMonitor = new Meta_system_monitors(etlRepRockFactory);
      if (targetMetaSystemMonitorVector.size() == 1) {
        targetMetaSystemMonitor = (Meta_system_monitors) targetMetaSystemMonitorVector.get(0);
        this.context.put("type", targetMetaSystemMonitor.getType());
        this.context.put("configuration", targetMetaSystemMonitor.getConfiguration());
        this.context.put("executed", targetMetaSystemMonitor.getExecuted().toString());
        this.context.put("status", targetMetaSystemMonitor.getStatus());
      } else {
        message = "Error editing server.";
      }

    } else if (action != null && action.equalsIgnoreCase("saveMetaServer")) {
      // Save the changes to the MetaServer
      template = getTemplate("system_monitors.vm");

      String hostName = request.getParameter("hostName");
      String type = request.getParameter("type");
      String statusUrl = request.getParameter("statusUrl");

      if (hostName == null) {
        hostName = new String();
      }
      if (type == null) {
        type = new String();
      }
      if (statusUrl == null) {
        statusUrl = new String();
      }

      final String addingNewServer = request.getParameter("addingNewServer");

      if (addingNewServer != null && addingNewServer.equalsIgnoreCase("true")) {
        // Check for the primary key constraint.
        if (hostNameExists(hostName)) {
          message = "Host name already exists.";
        } else {
          if (saveMetaServer(hostName, type, statusUrl)) {
            message = "Server saved succesfully.";
          } else {
            message = "Server saving failed. See log for details.";
          }
        }
      } else {
        if (saveMetaServer(hostName, type, statusUrl)) {
          message = "Server updated succesfully.";
        } else {
          message = "Server update failed. See log for details.";
        }
      }

      this.metaServers = loadAllMetaServers();
      final Iterator metaServersIterator = this.metaServers.iterator();

      // Get the html sources from specified servers.
      while (metaServersIterator.hasNext()) {
        final Meta_servers currentMetaServer = (Meta_servers) metaServersIterator.next();

        final String monitorsQueryString = getServerMonitorsQueryString(currentMetaServer);

        final String metaServerStatusHtmlSource = getMetaServerStatusHtmlSource(currentMetaServer.getStatus_url()
            + "?hostName=" + currentMetaServer.getHostname() + "&hostType=" + currentMetaServer.getType()
            + monitorsQueryString, currentMetaServer.getHostname(), currentMetaServer.getType());

        this.log.debug("metaServerStatusHtmlSource = " + metaServerStatusHtmlSource);
        metaServerStatusHtmlSources.put(currentMetaServer.getHostname(), metaServerStatusHtmlSource);

      }

      this.context.put("metaServers", this.metaServers);
      if (metaServerStatusHtmlSources.size() > 0) {
        this.context.put("metaServerStatusHtmlSources", metaServerStatusHtmlSources);
      }

    } else if (action != null && action.equalsIgnoreCase("removeServer")) {
      template = getTemplate("system_monitors.vm");
      final String hostName = request.getParameter("hostName");
      if (removeServer(hostName)) {
        message = "Server removed succesfully.";
      } else {
        message = "Server remove failed.";
      }
      this.metaServers = loadAllMetaServers();
      final Iterator metaServersIterator = this.metaServers.iterator();

      // Get the html sources from specified servers.
      while (metaServersIterator.hasNext()) {
        final Meta_servers currentMetaServer = (Meta_servers) metaServersIterator.next();

        final String monitorsQueryString = getServerMonitorsQueryString(currentMetaServer);

        final String metaServerStatusHtmlSource = getMetaServerStatusHtmlSource(currentMetaServer.getStatus_url()
            + "?hostName=" + currentMetaServer.getHostname() + "&hostType=" + currentMetaServer.getType()
            + monitorsQueryString, currentMetaServer.getHostname(), currentMetaServer.getType());

        this.log.debug("metaServerStatusHtmlSource = " + metaServerStatusHtmlSource);
        metaServerStatusHtmlSources.put(currentMetaServer.getHostname(), metaServerStatusHtmlSource);

      }

      this.context.put("metaServers", this.metaServers);
      if (metaServerStatusHtmlSources.size() > 0) {
        this.context.put("metaServerStatusHtmlSources", metaServerStatusHtmlSources);
      }

    } else if (action != null && action.equalsIgnoreCase("removeMetaSystemMonitor")) {
      template = getTemplate("system_monitors.vm");
      final String hostName = request.getParameter("hostName");
      final String monitor = request.getParameter("monitor");
      if (removeSystemMonitor(hostName, monitor)) {
        message = "System monitor removed succesfully.";
      } else {
        message = "System monitor remove failed.";
      }
      this.metaServers = loadAllMetaServers();
      final Iterator metaServersIterator = this.metaServers.iterator();

      // Get the html sources from specified servers.
      while (metaServersIterator.hasNext()) {
    	final Meta_servers currentMetaServer = (Meta_servers) metaServersIterator.next();

        final String monitorsQueryString = getServerMonitorsQueryString(currentMetaServer);

        final String metaServerStatusHtmlSource = getMetaServerStatusHtmlSource(currentMetaServer.getStatus_url()
            + "?hostName=" + currentMetaServer.getHostname() + "&hostType=" + currentMetaServer.getType()
            + monitorsQueryString, currentMetaServer.getHostname(), currentMetaServer.getType());

        this.log.debug("metaServerStatusHtmlSource = " + metaServerStatusHtmlSource);
        metaServerStatusHtmlSources.put(currentMetaServer.getHostname(), metaServerStatusHtmlSource);

      }

      this.context.put("metaServers", this.metaServers);
      if (metaServerStatusHtmlSources.size() > 0) {
        this.context.put("metaServerStatusHtmlSources", metaServerStatusHtmlSources);
      }

    } else if (action != null && action.equalsIgnoreCase("addMetaSystemMonitor")) {
      // Creating a new META_SYSTEM_MONITOR ENTRY for the selected META_SERVER.
      template = getTemplate("edit_meta_system_monitor.vm");
      final String hostName = request.getParameter("hostName");
      this.context.put("hostName", hostName);
    } else if (action != null && action.equalsIgnoreCase("saveSystemMonitor")) {
      template = getTemplate("system_monitors.vm");
      final String monitor = request.getParameter("monitor");
      final String hostName = request.getParameter("hostName");
      final String type = request.getParameter("type");
      final String configuration = request.getParameter("configuration");

      final String addingNewSystemMonitor = request.getParameter("addingNewSystemMonitor");

      if (addingNewSystemMonitor != null && addingNewSystemMonitor.equalsIgnoreCase("true")) {
        final Timestamp timestamp = new Timestamp(0);
        final String status = "";
        // Check for the primary key constraint.
        if (systemMonitorExists(monitor, hostName)) {
          message = "System monitor already exists.";
        } else {
          if (saveSystemMonitor(monitor, hostName, type, configuration, timestamp, status)) {
            message = "System monitor saved succesfully.";
          } else {
            message = "Saving system monitor failed.";
          }
        }
      } else {
        // Call the saveSystemMonitor with timestamp and status parameters as null when saving existing system monitor.
        if (saveSystemMonitor(monitor, hostName, type, configuration, null, null)) {
          message = "System monitor saved succesfully.";
        } else {
          message = "Saving system monitor failed.";
        }

      }
      this.metaServers = loadAllMetaServers();
      final Iterator metaServersIterator = this.metaServers.iterator();

      // Get the html sources from specified servers.
      while (metaServersIterator.hasNext()) {
        final Meta_servers currentMetaServer = (Meta_servers) metaServersIterator.next();

        final String monitorsQueryString = getServerMonitorsQueryString(currentMetaServer);

        final String metaServerStatusHtmlSource = getMetaServerStatusHtmlSource(currentMetaServer.getStatus_url()
            + "?hostName=" + currentMetaServer.getHostname() + "&hostType=" + currentMetaServer.getType()
            + monitorsQueryString, currentMetaServer.getHostname(), currentMetaServer.getType());

        this.log.debug("metaServerStatusHtmlSource = " + metaServerStatusHtmlSource);
        metaServerStatusHtmlSources.put(currentMetaServer.getHostname(), metaServerStatusHtmlSource);

      }

      this.context.put("metaServers", this.metaServers);
      if (metaServerStatusHtmlSources.size() > 0) {
        this.context.put("metaServerStatusHtmlSources", metaServerStatusHtmlSources);
      }

    } else if (action != null && action.equalsIgnoreCase("editSystemMonitors")) {
      template = getTemplate("edit_system_monitors.vm");
      this.metaServers = loadAllMetaServers();
      final Iterator metaServersIterator = metaServers.iterator();
      while (metaServersIterator.hasNext()) {
        final Meta_servers currentMetaServer = (Meta_servers) metaServersIterator.next();
        final String host = currentMetaServer.getHostname();
        final Vector systemMonitors = getMetaServerSystemMonitors(host);
        metaServerSystemMonitors.put(host, systemMonitors);
      }

      String selectedMetaServer = "-";

      if (request.getParameter("selectedMetaServer") != null) {
        selectedMetaServer = request.getParameter("selectedMetaServer");
      }

      this.context.put("selectedMetaServer", selectedMetaServer);
      this.context.put("metaServerSystemMonitors", metaServerSystemMonitors);

      this.context.put("metaServers", this.metaServers);

    }

    else {
      // Show servers and system monitors.
      template = getTemplate("system_monitors.vm");

      this.metaServers = loadAllMetaServers();
      final Iterator metaServersIterator = this.metaServers.iterator();

      // Get the html sources from specified servers.
      while (metaServersIterator.hasNext()) {
        final Meta_servers currentMetaServer = (Meta_servers) metaServersIterator.next();

        final String monitorsQueryString = getServerMonitorsQueryString(currentMetaServer);

        final String metaServerStatusHtmlSource = getMetaServerStatusHtmlSource(currentMetaServer.getStatus_url()
            + "?hostName=" + currentMetaServer.getHostname() + "&hostType=" + currentMetaServer.getType()
            + monitorsQueryString, currentMetaServer.getHostname(), currentMetaServer.getType());

        this.log.debug("metaServerStatusHtmlSource = " + metaServerStatusHtmlSource);
        metaServerStatusHtmlSources.put(currentMetaServer.getHostname(), metaServerStatusHtmlSource);

      }

      this.context.put("metaServers", this.metaServers);
      if (metaServerStatusHtmlSources.size() > 0) {
        this.context.put("metaServerStatusHtmlSources", metaServerStatusHtmlSources);
      }
    }

    if (!message.equalsIgnoreCase("")) {
      this.context.put("message", message);
    }

    this.context.put("serverMonitorsBulpColor", serverMonitorsBulpColor);

    log.debug("Template used is " + template.getName());

    return template;
  }

  /**
   * This function loads all meta servers from the database table META_SERVERS.
   * @return Returns a Vector full of Metaserver instances.
   */
  private Vector loadAllMetaServers() {
    try {
      Vector metaServers = new Vector();
      final RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");

      final Meta_servers whereMetaServer = new Meta_servers(etlRepRockFactory);
      final Meta_serversFactory metaServerFactory = new Meta_serversFactory(etlRepRockFactory, whereMetaServer);
      metaServers = metaServerFactory.get();

      return metaServers;

    } catch (Exception e) {
      log.error("Error in SystemMonitors.loadAllMetaServers", e);
      return new Vector();
    }
  }

  /**
   * This method checks if the host name already exists in the META_SERVERS table.
   * @return Returns true if the host name already exists. Also returns true in case of an exception. Otherwise returns false.
   */
  private boolean hostNameExists(final String hostName) {
    try {
      final RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");
      final Meta_servers whereMetaServer = new Meta_servers(etlRepRockFactory);
      whereMetaServer.setHostname(hostName);
      final Meta_serversFactory metaServerFactory = new Meta_serversFactory(etlRepRockFactory, whereMetaServer);
      final Vector metaServers = metaServerFactory.get();

      return metaServers.size() > 0;

    } catch (Exception e) {
      log.error("Error in SystemMonitors.hostNameExists", e);
      return true;
    }
  }

  /**
   * This function checks if a system monitor already exists in the database table META_SYSTEM_MONITORS.
   * @param monitor Value of monitor.
   * @param hostName Value of host name.
   * @return Returns true if the system monitor already exists in database. Also returns true in case of an exception. Otherwise returns false.
   */
  private boolean systemMonitorExists(final String monitor, final String hostName) {
    try {
      final RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");
      final Meta_system_monitors whereMetaSystemMonitor = new Meta_system_monitors(etlRepRockFactory);
      whereMetaSystemMonitor.setHostname(hostName);
      whereMetaSystemMonitor.setMonitor(monitor);
      final Meta_system_monitorsFactory metaSystemMonitorFactory = new Meta_system_monitorsFactory(etlRepRockFactory,
          whereMetaSystemMonitor);
      final Vector metaSystemMonitors = metaSystemMonitorFactory.get();

      return metaSystemMonitors.size() > 0;

    } catch (Exception e) {
      log.error("Error in SystemMonitors.systemMonitorExists", e);
      return true;
    }
  }

  /**
   * Saves the data of a Meta_server to database.
   * @param hostName Name of the server.
   * @param type Type of the server.
   * @param statusUrl StatusUrl of the server.
   * @return Returns true if the saving was succesful. Otherwise returns false.
   */
  private boolean saveMetaServer(final String hostName, final String type, final String statusUrl) {
    try {
      final RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");
      final Meta_servers targetMetaServer = new Meta_servers(etlRepRockFactory);
      targetMetaServer.setHostname(hostName);
      targetMetaServer.setType(type);
      targetMetaServer.setStatus_url(statusUrl);

      if (hostNameExists(hostName)) {
        targetMetaServer.updateDB();
      } else {
        targetMetaServer.insertDB();
      }

      return true;
    } catch (Exception e) {
      log.error("Error in SystemMonitors.saveMetaServer", e);
      return false;
    }
  }

  /**
   * This function saves a system monitor entry to the database table META_SYSTEM_MONITORS. 
   * @param monitor Value of monitor.
   * @param hostName Value of host name.
   * @param type Value of type.
   * @param configuration Value of configuration.
   * @param executed Value of executed. If given as null, the value is loaded from an existing system monitor.
   * @param status Value of status. If given as null, the value is loaded from an existing system monitor.
   * @return Returns true if the save was succesful. Returns false if exception is thrown or saving fails.
   */
  private boolean saveSystemMonitor(final String monitor, final String hostName, final String type, final String configuration,
      final Timestamp executed, final String status) {
    try {
      final RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");
      final Meta_system_monitors targetMetaSystemMonitor = new Meta_system_monitors(etlRepRockFactory);
      targetMetaSystemMonitor.setHostname(hostName);
      targetMetaSystemMonitor.setMonitor(monitor);
      targetMetaSystemMonitor.setType(type);
      targetMetaSystemMonitor.setConfiguration(configuration);

      // Check if the executed value is wanted to load from existing system monitor.
      if (executed == null) {
        final Meta_system_monitors whereMetaSystemMonitor = new Meta_system_monitors(etlRepRockFactory);
        whereMetaSystemMonitor.setMonitor(monitor);
        whereMetaSystemMonitor.setHostname(hostName);
        final Meta_system_monitorsFactory metaSystemMonitorFactory = new Meta_system_monitorsFactory(etlRepRockFactory,
            whereMetaSystemMonitor);
        final Vector targetMetaSystemMonitorVector = metaSystemMonitorFactory.get();

        if (targetMetaSystemMonitorVector.size() == 1) {
          final Meta_system_monitors existingMetaSystemMonitor = (Meta_system_monitors) targetMetaSystemMonitorVector.get(0);
          targetMetaSystemMonitor.setExecuted(existingMetaSystemMonitor.getExecuted());
        }
      } else {
        targetMetaSystemMonitor.setExecuted(executed);
      }

      // Check if the status value is wanted to load from existing system monitor.
      if (status == null) {
        final Meta_system_monitors whereMetaSystemMonitor = new Meta_system_monitors(etlRepRockFactory);
        whereMetaSystemMonitor.setMonitor(monitor);
        whereMetaSystemMonitor.setHostname(hostName);
        final Meta_system_monitorsFactory metaSystemMonitorFactory = new Meta_system_monitorsFactory(etlRepRockFactory,
            whereMetaSystemMonitor);
        final Vector targetMetaSystemMonitorVector = metaSystemMonitorFactory.get();

        if (targetMetaSystemMonitorVector.size() == 1) {
          final Meta_system_monitors existingMetaSystemMonitor = (Meta_system_monitors) targetMetaSystemMonitorVector.get(0);
          targetMetaSystemMonitor.setStatus(existingMetaSystemMonitor.getStatus());
        }
      } else {
        targetMetaSystemMonitor.setStatus(status);
      }

      if (systemMonitorExists(monitor, hostName)) {
        targetMetaSystemMonitor.updateDB();
      } else {
        targetMetaSystemMonitor.insertDB();
      }

      return true;
    } catch (Exception e) {
      log.error("Error in SystemMonitors.saveSystemMonitor", e);
      return false;
    }

  }

  /**
   * This function removes a server from the database table META_SERVERS. This function also removes the related entries in the table META_SYSTEM_MONITORS.
   * @param hostName Primary key of the table, which is hostname.
   */
  private boolean removeServer(final String hostName) {
    try {
      final RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");
      final Meta_servers targetMetaServer = new Meta_servers(etlRepRockFactory);
      targetMetaServer.setHostname(hostName);

      // Before the server can be deleted, the entries related at META_SYSTEM_MONITORS must be removed.
      final Meta_system_monitors whereMetaSystemMonitor = new Meta_system_monitors(etlRepRockFactory);
      whereMetaSystemMonitor.setHostname(hostName);
      final Meta_system_monitorsFactory metaSystemMonitorFactory = new Meta_system_monitorsFactory(etlRepRockFactory,
          whereMetaSystemMonitor);
      final Vector targetMetaSystemMonitors = metaSystemMonitorFactory.get();
      final Iterator targetMetaSystemMonitorsIterator = targetMetaSystemMonitors.iterator();

      while (targetMetaSystemMonitorsIterator.hasNext()) {
        final Meta_system_monitors targetMetaSystemMonitor = (Meta_system_monitors) targetMetaSystemMonitorsIterator.next();
        targetMetaSystemMonitor.deleteDB();
      }

      targetMetaServer.deleteDB();
      return true;
    } catch (Exception e) {
      log.error("Error in SystemMonitors.removeServer", e);
      return false;
    }
  }

  /**
   * This function removes a system monitor from the database table META_SYSTEM_MONITORS.
   * @param hostName Primary key of the table, which is hostname.
   * @param monitor Primary key of the table, which is monitor.
   */
  private boolean removeSystemMonitor(final String hostName, final String monitor) {
    try {
      final RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");
      final Meta_system_monitors targetMetaSystemMonitor = new Meta_system_monitors(etlRepRockFactory);
      targetMetaSystemMonitor.setHostname(hostName);
      targetMetaSystemMonitor.setMonitor(monitor);
      targetMetaSystemMonitor.deleteDB();
      return true;
    } catch (Exception e) {
      log.error("Error in SystemMonitors.removeSystemMonitor", e);
      return false;
    }
  }

  /**
   * This function gets the system monitors of a certain meta server.
   * @param hostName Hostname of the metaserver, which is needed to load system monitors.
   * @return Returns a Vector containing loaded system monitors.
   */
  private Vector getMetaServerSystemMonitors(final String hostName) {
    try {
      Vector systemMonitors = new Vector();
      final RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");

      final Meta_system_monitors whereSystemMonitor = new Meta_system_monitors(etlRepRockFactory);

      whereSystemMonitor.setHostname(hostName);
      final Meta_system_monitorsFactory metaSystemMonitorFactory = new Meta_system_monitorsFactory(etlRepRockFactory,
          whereSystemMonitor);
      systemMonitors = metaSystemMonitorFactory.get();

      return systemMonitors;

    } catch (Exception e) {
      log.error("Error in SystemMonitors.getMetaServerSystemMonitors", e);
      return new Vector();
    }
  }

  /**
   * This function returns the state of the system monitors that exist in META_SYSTEM_MONITORS database table.
   * @return Returns the state of the system monitos in string form. Value can be one of these: green, yellow, red.
   */
  private String getSystemMonitorsState() {
    try {
      final RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");
      String systemMonitorsState = "green";

      final Meta_system_monitors whereSystemMonitor = new Meta_system_monitors(etlRepRockFactory);
      final Meta_system_monitorsFactory metaSystemMonitorFactory = new Meta_system_monitorsFactory(etlRepRockFactory,
          whereSystemMonitor);
      final Vector systemMonitors = metaSystemMonitorFactory.get();
      final Iterator systemMonitorsIterator = systemMonitors.iterator();
      while (systemMonitorsIterator.hasNext()) {
        final Meta_system_monitors currentSystemMonitor = (Meta_system_monitors) systemMonitorsIterator.next();
        if (!currentSystemMonitor.getStatus().equalsIgnoreCase("ACTIVE")) {
          systemMonitorsState = "red";
        }
        // TODO: Also check for other statuses? Like yellow?
      }

      return systemMonitorsState;

    } catch (Exception e) {
      log.error("Error in SystemMonitors.getSystemMonitorsState", e);
      return "red";
    }
  }

  /**
   * This function get's the status of the server through the http-protocol.
   * @param statusUrl is the url to get the status html-source.
   * @param hostName is the name of the remote server.
   * @param hostType is the type of the remote host.
   * @return Returns the status servlet's generated html-source.
   */
  private String getMetaServerStatusHtmlSource(final String statusUrl, final String hostName, final String hostType) {

    try {
      final URL url = new URL(statusUrl);
      log.debug("Getting status of server from status URL: " + statusUrl);

      // Read all the text returned by the server
      final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
      String line = "";
      String content = "";
      while ((line = in.readLine()) != null) {
        content += line;
      }
      in.close();
      return content;
    } catch (Exception e) {
      log.error("Error in SystemMonitors.getMetaServerStatusHtmlSource", e);

      this.context.put("errorString", "Status URL " + statusUrl + " failed to response.");
      try {
        //this.template = getTemplate("adminuiErrorPage.vm");
        final VelocityContext context = new VelocityContext();
        context.put("errorString", "Status URL " + statusUrl + " failed to response");
        context.put("hostName", hostName);
        context.put("hostType", hostType);
        final Template template = getTemplate("server_status_failed.vm");
        final StringWriter writer = new StringWriter();

        template.merge(context, writer);
        return writer.toString();
        /* show the World */

        //System.out.println( writer.toString() );     
        //this.template = getTemplate("server_status_failed.vm");
      } catch (Exception ex) {
        log.debug("Parsing of template adminuiErrorPage.vm failed.", ex);
      }

      return "";
    }

  }

  /**
   * This function creates the end of the query string to be sent to the ServerStatus servlet (ServerStatus.java).
   * @param targetMetaServer The server where monitors query is sent.
   * @return Returns the string to add to the query string.
   */
  private String getServerMonitorsQueryString(final Meta_servers targetMetaServer) {
    String serverMonitorsQueryString = "";
    try {
      final RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");
      final Meta_system_monitors whereMetaSystemMonitor = new Meta_system_monitors(etlRepRockFactory);
      whereMetaSystemMonitor.setHostname(targetMetaServer.getHostname());
      final Meta_system_monitorsFactory metaSystemMonitorsFactory = new Meta_system_monitorsFactory(etlRepRockFactory,
          whereMetaSystemMonitor);
      final Vector metaSystemMonitors = metaSystemMonitorsFactory.get();
      final Iterator metaSystemMonitorsIterator = metaSystemMonitors.iterator();
      while (metaSystemMonitorsIterator.hasNext()) {
        final Meta_system_monitors currentSystemMonitor = (Meta_system_monitors) metaSystemMonitorsIterator.next();
        final Properties systemMonitorConfiguration = getSystemMonitorConfiguration(currentSystemMonitor);
        final Enumeration systemMonitorConfigurations = systemMonitorConfiguration.propertyNames();
        while (systemMonitorConfigurations.hasMoreElements()) {
          final String propertyName = (String) systemMonitorConfigurations.nextElement();
          // Check for the type of the system monitor.
          if (currentSystemMonitor.getType().equalsIgnoreCase("DISK")) {
            serverMonitorsQueryString += "&d."; // d = DISK
            serverMonitorsQueryString += currentSystemMonitor.getMonitor();
            serverMonitorsQueryString += "." + propertyName + "=" + systemMonitorConfiguration.getProperty(propertyName);
          }
        }
      }
      log.debug("serverMonitorsQueryString = " + serverMonitorsQueryString);
      return serverMonitorsQueryString;
    } catch (Exception e) {
      log.error("Error in SystemMonitors.getServerMonitorsQueryString", e);
      return "";
    }
  }

  /**
   * This function gets the configuration of a system monitor from the database.
   * @param targetSystemMonitor is the system monitor of which configuration is to be loaded.
   */
  private Properties getSystemMonitorConfiguration(final Meta_system_monitors targetSystemMonitor) {
    final Properties configuration = new Properties();

    final String configurationString = targetSystemMonitor.getConfiguration();

    if (configurationString != null && configurationString.length() > 0) {
      try {
        final ByteArrayInputStream bais = new ByteArrayInputStream(configurationString.getBytes());
        configuration.load(bais);
        bais.close();
        log.info("Configuration read");
      } catch (Exception e) {
        log.error("SystemMonitors.getSystemMonitorConfiguration. Error reading configuration.");
        return new Properties();
      }

    }

    return configuration;

  }

}
