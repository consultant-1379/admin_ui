package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.etl.LogLineDetail;
import com.distocraft.dc5000.etl.gui.util.LogBrowser;

/**
 * This class reads and prints the license logfile given as a parameter.
 * 
 * @author ejannbe & ecarbjo
 * 
 */
public class ReadLicenseLog extends EtlguiServlet {

  private static final long serialVersionUID = 1095086235920748805L;
  private final static String selectLogPage = "show_license_logs.vm";
  private final static String printLogPage = "print_license_log.vm";

  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, final Context ctx) {
    Template page = null;

    final Log log = LogFactory.getLog(this.getClass());
    try {

      final String day = StringEscapeUtils.escapeHtml(request.getParameter("day_1"));
      final String year = StringEscapeUtils.escapeHtml(request.getParameter("year_1"));
      final String month = StringEscapeUtils.escapeHtml(request.getParameter("month_1"));

      ctx.put("sdf", new SimpleDateFormat("HH:mm:ss"));

      String logfile;
      if (!day.equals("") && !month.equals("") && !year.equals("") && logExists(day, month, year)) {
        logfile = "licensemanager-" + year + "_" + month + "_" + day + ".log";
        ctx.put("licenselog_rows", readLicenseLog(logfile, log));
        page = getTemplate(printLogPage);
      } else {
        LicenseLogsViewer.prepareDateChooser(request, ctx);
        ctx.put("fault_message", "No logs available for this date.");
        page = getTemplate(selectLogPage);
      }
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
   * This function parses through the license logfile and
   * 
   * @return List containing the rows of license logs.
   */
  @SuppressWarnings("unchecked")
  private List<LogLineDetail> readLicenseLog(final String logfile, final Log log) throws Exception {
    final File targetFile = new File(System.getProperty("LOG_DIR") + "/licensemanager/" + logfile);

    if (targetFile == null) {
      log.error("Directory " + System.getProperty("LOG_DIR") + "/licensemanager/ not found!");
      return new Vector<LogLineDetail>();
    }

    try {
      // get the contents of the log file.
      final LogBrowser browser = new LogBrowser(System.getProperty("LOG_DIR") + "/licensemanager/");
      final List<LogLineDetail> logdetails = browser.parseLicenseLogFile(targetFile);
      return logdetails;

    } catch (Exception e) {
      // could be for example a file not found exception, we need to do nothing.
      return new Vector<LogLineDetail>();
    }

  }

  /**
   * Checks if the log file for the given parameters exists.
   * @param day the day in the form [0-9]{2}
   * @param month the month in the form [0-9]{2}
   * @param year the year in the form [0-9]{4}
   * @return true if the log file for the given date exists, false otherwize.
   */
  public static boolean logExists(String day, String month, String year) {
    if (!day.equals("") && !month.equals("") && !year.equals("")) {
     final String logfile = "licensemanager-" + year + "_" + month + "_" + day + ".log";
     final File targetFile = new File(System.getProperty("LOG_DIR") + "/licensemanager/" + logfile);
     return targetFile.canRead();
    } else {
      return false;
    }
  }
}
