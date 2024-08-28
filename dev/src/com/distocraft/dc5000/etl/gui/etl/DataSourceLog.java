/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.distocraft.dc5000.etl.gui.etl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.util.Helper;

/**
 * 
 * Servlet is for browsing missing network elements.
 * 
 * @author etonnee
 * 
 */
public class DataSourceLog extends EtlguiServlet {

  private static final Log LOG = LogFactory.getLog(DataSourceLog.class);

  private static final String SDF_FORMAT = "yyyy-MM-dd";

  private static final String SDFTIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  private static final String VIEW_TEMPLATE = "displaymissingNE.vm";

  private static final String NOTIFICATION = "An Error Occured while processing this request. Please check adminui.log for more details.";

  private static final String NOTIFICATION_KEY = "notification";

  private static final String EMPTY = " ";

  private static final String YEAR = "year";

  private static final String MONTH = "month";

  private static final String DAY = "day";

  private static final String YEAR2 = "year2";

  private static final String MONTH2 = "month2";

  private static final String DAY2 = "day2";

  private static final String START_HOUR = "start_hour";

  private static final String START_MIN = "start_min";

  private static final String END_HOUR = "end_hour";

  private static final String END_MIN = "end_min";

  TreeMap<String, List<TimeRange>> missingDataMap = null;

  String toDateUser = null;

  String fromDateUser = null;

  @Override
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response,
      final Context velocityContext) {

    final HttpSession session = request.getSession(false);
    Template viewTemplate = null;
    try {
      processRequestData(velocityContext, session, request, response);
      velocityContext.put(NOTIFICATION_KEY, EMPTY);
    } catch (Exception e) {
      LOG.error("Exception", e);
      velocityContext.put(NOTIFICATION_KEY, NOTIFICATION);

    } finally {
      try {
        viewTemplate = getTemplate(VIEW_TEMPLATE);
      } catch (ResourceNotFoundException e) {
        LOG.error("ResourceNotFoundException", e);
      } catch (ParseErrorException e) {
        LOG.error("ParseErrorException", e);
      } catch (Exception e) {
        LOG.error("Exception", e);
      }
    }

    return viewTemplate;

  }

  /**
   * process request parameters and process display data
   * 
   * @param ctx
   * @param session
   * @param request
   * @param response
   */
  private void processRequestData(final Context velocityContext, final HttpSession session,
      final HttpServletRequest request, final HttpServletResponse response) {

    // get the current date (is used at the UI, if none given)
    final DbCalendar calendar = new DbCalendar();
    final SimpleDateFormat sdfDate = new SimpleDateFormat(SDF_FORMAT);
    String submitted = request.getParameter("submitted");

    String year_1 = request.getParameter("year_1");
    String month_1 = request.getParameter("month_1");
    String day_1 = request.getParameter("day_1");
    String year_2 = request.getParameter("year_2");
    String month_2 = request.getParameter("month_2");
    String day_2 = request.getParameter("day_2");

    String start_hour = request.getParameter("start_hour");
    String start_min = request.getParameter("start_min");

    String end_hour = request.getParameter("end_hour");
    String end_min = request.getParameter("end_min");

    if (year_1 != null) {
      session.setAttribute(YEAR, year_1);
    } else if (session.getAttribute(YEAR) != null) {
      year_1 = session.getAttribute(YEAR).toString();
    } else {
      session.setAttribute(YEAR, calendar.getYearString());
      year_1 = calendar.getYearString();
    }

    if (month_1 != null) {
      session.setAttribute(MONTH, month_1);
    } else if (session.getAttribute(MONTH) != null) {
      month_1 = session.getAttribute(MONTH).toString();
    } else {
      session.setAttribute(MONTH, calendar.getMonthString());
      month_1 = calendar.getMonthString();
    }

    if (day_1 != null) {
      session.setAttribute(DAY, day_1);
    } else if (session.getAttribute(DAY) != null) {
      day_1 = session.getAttribute(DAY).toString();
    } else {
      session.setAttribute(DAY, calendar.getDayString());
      day_1 = calendar.getDayString();
    }

    if (start_hour != null) {
      session.setAttribute(START_HOUR, start_hour);
    } else if (session.getAttribute(START_HOUR) != null) {
      start_hour = session.getAttribute(START_HOUR).toString();
    } else {
      session.setAttribute(START_HOUR, "0");
      start_hour = "0";
    }

    if (start_min != null) {
      session.setAttribute(START_MIN, start_min);
    } else if (session.getAttribute(START_MIN) != null) {
      start_min = session.getAttribute(START_MIN).toString();
    } else {
      session.setAttribute(START_MIN, "00");
      start_min = "00";
    }

    if (year_2 != null) {
      session.setAttribute(YEAR2, year_2);
    } else if (session.getAttribute(YEAR2) != null) {
      year_2 = session.getAttribute(YEAR2).toString();
    } else {
      session.setAttribute(YEAR2, calendar.getYearString());
      year_2 = calendar.getYearString();
    }

    if (month_2 != null) {
      session.setAttribute(MONTH2, month_2);
    } else if (session.getAttribute(MONTH2) != null) {
      month_2 = session.getAttribute(MONTH2).toString();
    } else {
      session.setAttribute(MONTH2, calendar.getMonthString());
      month_2 = calendar.getMonthString();
    }

    if (day_2 != null) {
      session.setAttribute(DAY2, day_2);
    } else if (session.getAttribute(DAY2) != null) {
      day_2 = session.getAttribute(DAY2).toString();
    } else {
      session.setAttribute(DAY2, calendar.getDayString());
      day_2 = calendar.getDayString();
    }

    if (end_hour != null) {
      session.setAttribute(END_HOUR, end_hour);
    } else if (session.getAttribute(END_HOUR) != null) {
      end_hour = session.getAttribute(END_HOUR).toString();
    } else {
      session.setAttribute(END_HOUR, "23");
      end_hour = "23";
    }

    if (end_min != null) {
      session.setAttribute(END_MIN, end_min);
    } else if (session.getAttribute(END_MIN) != null) {
      end_min = session.getAttribute(END_MIN).toString();
    } else {
      session.setAttribute(END_MIN, "00");
      end_min = "00";
    }

    try {

      Date date_1 = sdfDate.parse(year_1 + "-" + month_1 + "-" + day_1);
      Date date_2 = sdfDate.parse(year_2 + "-" + month_2 + "-" + day_2);

      if (date_1.after(date_2)) {
        year_2 = year_1;
        month_2 = month_1;
        day_2 = day_1;
        session.setAttribute("year2", year_1);
        session.setAttribute("month2", month_1);
        session.setAttribute("day2", day_1);
      }
      if ("true".equalsIgnoreCase(submitted)) {
        velocityContext.put("submitted", true);
        final String query = buildQuery(formatDateTime(year_1, month_1, day_1, start_hour, start_min),
            formatDateTime(year_2, month_2, day_2, end_hour, end_min));
        int maxNErows = Helper.getEnvEntryInt("maxNERows");
        Connection conn = null;
        conn = ((RockFactory) velocityContext.get("rockDwh")).getConnection();
        long maxRecordCount = processDBRecords(conn, query);
        velocityContext.put("results", missingDataMap);
        velocityContext.put("toomany", maxRecordCount >= maxNErows ? maxNErows : 0);
        LOG.debug("toomany :" + (maxRecordCount >= maxNErows));
        LOG.debug(missingDataMap.keySet());
      }

    } catch (Exception e) {
      LOG.error("Exception", e);
      velocityContext.put(NOTIFICATION_KEY, NOTIFICATION);
    }
    velocityContext.put("year_1", year_1);
    velocityContext.put("month_1", month_1);
    velocityContext.put("day_1", day_1);
    velocityContext.put("year_2", year_2);
    velocityContext.put("month_2", month_2);
    velocityContext.put("day_2", day_2);
    velocityContext.put("start_hour", start_hour);
    velocityContext.put("start_min", start_min);
    velocityContext.put("end_hour", end_hour);
    velocityContext.put("end_min", end_min);

  }

  /**
   * Format date time string with seconds defaulted to 0.
   * 
   * @param year
   * @param month
   * @param day
   * @param hours
   * @param minutes
   * @return formatted date time string
   */
  private String formatDateTime(final String year, final String month, final String day, final String hours,
      final String minutes) {
    final StringBuilder sb = new StringBuilder();
    sb.append(year).append("-").append(month).append("-").append(day).append(" ").append(hours).append(":")
        .append(minutes).append(":00");

    return sb.toString();
  }

  /**
   * Format and build SQL Query
   * 
   * @param flag
   * @param dateTime1
   * @param dateTime2
   * @return formatted Query String
   * 
   */
  private String buildQuery(String dateTime1, String dateTime2) {
    final StringBuilder query = new StringBuilder();
    toDateUser = dateTime2;
    fromDateUser = dateTime1;

    query.append("SELECT NE.SOURCE, NE.DATETIME_ID");
    query.append(" FROM LOG_SESSION_COLLECTED_DATA NE, DIM_E_SGEH_SGSN DIM");
    query.append(" WHERE NE.SOURCE=DIM.SGSN_NAME  AND NE.COLLECTED=0");
    query.append(" AND NE.DATETIME_ID between datetime('" + fromDateUser + "') AND datetime('" + toDateUser + "')");
    query.append(" order by NE.SOURCE, NE.DATETIME_ID");

    LOG.debug("NEQuery--" + query.toString());

    return query.toString();
  }

  /**
   * 
   * process database Records and create list of datasource elements
   * 
   * @param ctx
   * @param query
   * @return
   */
  private long processDBRecords(Connection conn, String query) throws Exception {

    Statement statement = null;
    ResultSet resultSet = null;
    List<String> sourceNames = new ArrayList<String>();
    missingDataMap = new TreeMap<String, List<TimeRange>>();
    try {
      SimpleDateFormat SDFTIME = new SimpleDateFormat(SDFTIME_FORMAT);
      statement = conn.createStatement();
      resultSet = statement.executeQuery(query);
      String strDateTime = null;
      String sourceName = null;
      Date lastCollectedDate = null;
      while (resultSet.next()) {
        sourceName = resultSet.getString("source");

        if (!sourceNames.contains(sourceName)) {
          sourceNames.add(sourceName);
        }

        strDateTime = resultSet.getString("datetime_id");
        if (strDateTime != null) {

          lastCollectedDate = SDFTIME.parse(strDateTime);

        } else {
          lastCollectedDate = null;
        }

        mapMissingDataRange(sourceName, lastCollectedDate);
      }

    } catch (ParseException e) {
      LOG.error("Date ParseException : " + e);
      throw e;
    } catch (SQLException e) {
      LOG.error("SQLException: " + e);
      throw e;
    } finally {
      try {
        if (conn != null) {
          conn.commit();
        }
        if (resultSet != null) {
          resultSet.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (Exception e) {
        LOG.error("Exception: ", e);
        throw e;
      }
    }

    LOG.info("hmMissingNE : " + missingDataMap.size());

    return sourceNames.size();
  }

  public static class TimeRange {

    SimpleDateFormat SDFTIME = new SimpleDateFormat(SDFTIME_FORMAT);

    private Date start;

    private Date end;

    GregorianCalendar cal = new DbCalendar();

    TimeRange(Date start) {
      if (start != null) {
        this.start = start;
        this.end = addMinutes(start, 1);
      }

    }

    public boolean updateEndOfRange(Date date) {
      if (date == null) {
        return false;
      } else if (this.end == null) {
        return true;
      } else {
        // LOG.debug(SDFTIME.format(date.getTime()) +" -- "+ SDFTIME.format(this.end.getTime()));
        return ((date.getTime() - this.end.getTime()) / (60 * 1000) == 0);
      }

    }

    public void setStart(Date start) {
      this.start = start;
    }

    public Date getStart() {
      return start;
    }

    public Date getEnd() {
      return end;
    }

    public void setEnd(Date end) {
      this.end = addMinutes(end, 1);
    }

    public String getEndForDisplay() {

      return SDFTIME.format(end);
    }

    public String getStartForDisplay() {
      return SDFTIME.format(start);
    }

    /**
     * Get time interval in minutes
     * 
     * @return time interval in minutes or null
     * @throws IllegalStateException
     *           if start date is before end date
     */
    public Long lengthInMinutes() {
      if (this.start == null || this.end == null) {
        return null;
      }
      if (this.start.after(this.end)) {
        throw new IllegalStateException("start date (" + this.start + ") after end date: (" + this.end + ")");
      }

      return (this.end.getTime() - this.start.getTime()) / (60 * 1000);

    }

    private Date addMinutes(Date dateChange, int min) {

      GregorianCalendar cal = new DbCalendar();
      cal.setTime(dateChange);
      cal.add(Calendar.MINUTE, min);

      return cal.getTime();

    }

  }

  /**
   * Update missing data time range.
   * <p/>
   * Caveat: depends on time range ordered in ascending fashion externally.
   * <p/>
   * TODO: reorg logic to take into account times coming in any order .
   * 
   * @param sourceName
   * @param lastUncollectedDataTime
   * @return map of data sources and time range for missing data
   */
  public Map<String, List<TimeRange>> mapMissingDataRange(final String sourceName, final Date lastUncollectedDataTime)
      throws ParseException, Exception {
    List<TimeRange> timeRanges = missingDataMap.get(sourceName);

    if (timeRanges == null) {
      timeRanges = new ArrayList<TimeRange>();

      if (lastUncollectedDataTime != null) {
        timeRanges.add(new TimeRange(lastUncollectedDataTime));
      } else {
        // add start time range
        SimpleDateFormat SDFTIME = new SimpleDateFormat(SDFTIME_FORMAT);
        TimeRange newTimeRange = new TimeRange(SDFTIME.parse(fromDateUser));
        timeRanges.add(newTimeRange);
        newTimeRange.setEnd(SDFTIME.parse(toDateUser));
      }
      missingDataMap.put(sourceName, timeRanges);
    } else {
      TimeRange lastTimeRange = timeRanges.get(timeRanges.size() - 1);
      if (lastTimeRange.updateEndOfRange(lastUncollectedDataTime)) {
        lastTimeRange.setEnd(lastUncollectedDataTime);
      } else {
        timeRanges.add(new TimeRange(lastUncollectedDataTime));
      }

    }

    return this.missingDataMap;
  }

}
