/*------------------------------------------------------------------------
 *
 *
 *      COPYRIGHT (C) ERICSSON RADIO SYSTEMS AB, Sweden
 *
 *      The  copyright  to  the document(s) herein  is  the property of
 *      Ericsson Radio Systems AB, Sweden.
 *
 *      The document(s) may be used  and/or copied only with the written
 *      permission from Ericsson Radio Systems AB  or in accordance with
 *      the terms  and conditions  stipulated in the  agreement/contract
 *      under which the document(s) have been supplied.
 *
 *------------------------------------------------------------------------
 */

package com.distocraft.dc5000.etl.gui.aggregation;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;import org.apache.velocity.exception.VelocityException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.etl.ShowSet;
import com.distocraft.dc5000.etl.gui.monitor.Util;
import com.distocraft.dc5000.etl.gui.util.Helper;

public class ReAggregationServletEniqEvents extends EtlguiServlet {

  private final Log log = LogFactory.getLog(this.getClass());

  private final static String REAGGDURATIONNAME = "level";

  private final static String REAGGSTARTTIME = "startTime";

  private final static String REAGGENDTIME = "endTime";

  private final static String REAGGTECHPACKNAME = "batch_name";

  private final static String REAGGNOWORLATER = "ReAggregateNowOrLater";

  private static ITransferEngineRMI trRMI = null;

  protected static final String RE_AGG_SCOPES = "reaggregation_scopes";

  protected static final String RE_AGG_NAME_FOR_DAY = "DAY";

  protected static final String RE_AGG_NAME_FOR_15_MIN = "15MIN";

  protected static final String RE_AGG_NAME_FOR_1_MIN = "1MIN";

  protected static final int MILLI_SECONDS_IN_A_DAY = 24 * 60 * 60 * 1000;

  private static final int SECONDS_IN_A_DAY = 24 * 60 * 60;

  private static final int SECONDS_IN_FIFTEEN_MINUTES = 60 * 15;

  private static final int SECONDS_IN_A_MINUTE = 60;

  private static final int MILLI_SECONDS_IN_A_MINUTE = 60 * 1000;

  /**
   * @see com.distocraft.dc5000.etl.gui.common.EtlguiServlet#doHandleRequest(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.apache.velocity.context.Context)
   */
  @Override
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response,
      final Context ctx) {

    String page = "aggregationEniqEvents.vm";
    final Connection connDwhRep = ((RockFactory) ctx.get("rockDwhRep")).getConnection();
    try {
    	String engRMIUrl = RmiUrlFactory.getInstance().getEngineRmiUrl() ;
      trRMI = (ITransferEngineRMI) Naming
          .lookup(engRMIUrl);
      log.info("Engine RMI url :" + engRMIUrl);
      if (trRMI != null) {

        ctx.put("scopes", readInfoFromFile(RE_AGG_SCOPES));
        ctx.put("techpacks", Util.getTechPacks(connDwhRep));
        ctx.put("showDurationAndTime", "false");

        final String reAggDurationName = request.getParameter(REAGGDURATIONNAME);
        final String reAggStartTime = request.getParameter(REAGGSTARTTIME);
        final String reAggEndTime = request.getParameter(REAGGENDTIME);
        final String reAggTechPackName = request.getParameter(REAGGTECHPACKNAME);
        final String reAggNowOrLater = request.getParameter(REAGGNOWORLATER);

        ctx.put(REAGGDURATIONNAME, reAggDurationName);
        ctx.put(REAGGSTARTTIME, reAggStartTime);
        ctx.put(REAGGENDTIME, reAggEndTime);
        ctx.put(REAGGTECHPACKNAME, reAggTechPackName);
        ctx.put(REAGGNOWORLATER, reAggNowOrLater);

        if (reAggTechPackName != null && reAggDurationName != null) {
          ctx.put("showDurationAndTime", "true");
          // return value of -1 from
          // trRMI.getOldestReAggTimeInMs(reAggTechPackName)
          // would indicate earliestTime in Raw tables + bufferTime is
          // greater than current time . Handling for oldestTime = -1 is in
          // getDuration .
          final long oldestTime = trRMI.getOldestReAggTimeInMs(reAggTechPackName);
          final long lastImmediateTimeStamp = getNearestStartDate(reAggDurationName);
          final long durationInSeconds = getDuration(oldestTime, lastImmediateTimeStamp);

          log.info("Duration between nearest timestamp and the oldest timestamp available : " + durationInSeconds);
          if (durationInSeconds == -1) {
            ctx.put("NoDataAvailableForReAggregation", "Reaggregation could not proceed. Reasons can be \n"
                + "1. No data in Raw Tables \n"
                + "2. Oldest time in Raw Tables + Buffer time is more than Current Date .");
            page = "aggregationShowErrors.vm";
          } else {
            // duration starts from 0
            final int intervals = getIntervals(durationInSeconds, reAggDurationName);
            ctx.put("intervals", (intervals + 1));
            ctx.put("startTimes", getDateObjects(reAggDurationName, lastImmediateTimeStamp, intervals));
          }
        }
        // If the Do Reaggregate button is clicked
        if (reAggStartTime != null && reAggEndTime != null) {

          page = startReAggSets(reAggTechPackName, reAggStartTime, reAggEndTime, reAggDurationName, reAggNowOrLater,
              ctx);
          // Adding of the set has been done . Now, Show the results
          final ShowSet showSet = new ShowSet();
          showSet.init(this.getServletConfig());
          showSet.doHandleRequest(request, response, ctx);
        }
      }
    } catch (ServerException se) {
      page = showErrorPageandPrintToLog("ServerException in doHandleRequest :",se,ctx,"ServerException","ServerException");
    } catch (RemoteException re) {
      page = showErrorPageandPrintToLog("Remote Exception in doHandleRequest :",re,ctx,"rmiUrlFailure", "rmi://" + Helper.getEnvEntryString("rmiurl") + "/" + "TransferEngine");
    } catch (ParseException pe) {
      page = showErrorPageandPrintToLog("Parse Exception in doHandleRequest :",pe,ctx,"ParseException", "Dates Could not be parsed");
    } catch (ServletException se) {
      page = showErrorPageandPrintToLog("ServletException in doHandleRequest :",se,ctx,"ServletException", "ServletException");
    } catch (Exception e) {
      page = showErrorPageandPrintToLog("UnexpectedException in doHandleRequest :",e,ctx,"UnexpectedException",e.toString());
    }
    finally {
      try {
        connDwhRep.close();
      } catch (SQLException se) {
      page = showErrorPageandPrintToLog("SQLException while closing database connection", se,ctx,"SQLException", "SQLException while closing database connection");
      }
    }
    try {
      return getTemplate(page);    } catch (Exception e) {      throw new VelocityException(e);    }
  }

  /**
   * Reads the value or values of a variable from web.xml file.
   * 
   * @param variableName
   *          The name of the variable whose value has to be read from the
   *          web.xml file .
   * 
   * @return A Vector containing the value or the values of the variable .
   */
  protected List<String> readInfoFromFile(final String variableName) {

    final String informationInFile = getEnvEntryFromHelper(variableName);

    final Vector<String> vec1 = new Vector<String>();

    if (informationInFile.contains(",")) {
      final StringTokenizer st = new StringTokenizer(informationInFile, ",");
      while (st.hasMoreTokens()) {
        vec1.add(st.nextToken());
      }
    } else {
      vec1.add(informationInFile);
    }

    return vec1;
  }

  /**
   * Extracted out for Junit
   * 
   * @param variableName
   * @return
   */
  protected String getEnvEntryFromHelper(final String variableName) {
    return Helper.getEnvEntryString(variableName);
  }

  /**
   * Extracted out for Junit
   * 
   * @param variableName
   * @return
   */
  protected DbCalendar getCalendarWithSecondAndMilliSecondSetToZero() {

    final DbCalendar calendar = new DbCalendar();

    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar;
  }

  /**
   * Returns the nearest timestamp [long] in the past, for a particular Re
   * Aggregation . Eg: For DAY level ReAggregation, the Most recent day from
   * Current date. For 15 MIN = Most recent 15th minute from the current date
   * for 1 MIN = Most recent 1 minute from the current time.
   * 
   * @param aggType
   *          DAY or 15 Minute or 1 Minute Re Aggregation .
   * @return Most recent time stamp .
   */
  protected long getNearestStartDate(final String aggType) {

    final DbCalendar calendar = getCalendarWithSecondAndMilliSecondSetToZero();

    if (aggType.equalsIgnoreCase(RE_AGG_NAME_FOR_15_MIN)) {
      final int minutes = calendar.get(Calendar.MINUTE);
      calendar.set(Calendar.MINUTE, (minutes - 15) - (minutes % 15));
    } else if (aggType.equalsIgnoreCase(RE_AGG_NAME_FOR_1_MIN)) {
      calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 1);
    } else if (aggType.equalsIgnoreCase(RE_AGG_NAME_FOR_DAY)) {
      // Should be Hour or Hour_Of_Day for 24 hour time format.
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
    }
    return calendar.getTimeInMillis();
  }

  /**
   * Takes oldestTime from Engine,lastImmediateTimeStamp calculated and gives
   * the difference between them in seconds. This will return -1 for error
   * conditions .
   * 
   * @param oldestTime
   *          The timestamp [long] returned by Engine . This is earliest time in
   *          RAW tables + a Buffer time of 2 days.
   * @param lastImmediateTimeStamp
   *          The last immediate time stamp in milliseconds for a particular
   *          reAggName .
   * 
   * @return The difference between lastImmediateTimeStamp oldestTime in seconds
   *         or -1 in case of Errors.
   */
  protected long getDuration(final long oldestTime, final long lastImmediateTimeStamp) {
    long duration = -1;
    // We need not consider the last three digits . Because the first second
    // will occur after 999 milliseconds.
    // For duration calculation we can use milliseconds .
    log.info("Inside getDuration():oldestTime passed [Date] is " + new Date(oldestTime));
    log.info("Inside getDuration():lastImmediateTimeStamp passed [Date] is " + new Date(lastImmediateTimeStamp));
    if (oldestTime > 0) {
      // Calculate, using only till seconds .
      final long old = oldestTime / 1000;
      final long immediatePrevious = lastImmediateTimeStamp / 1000;
      if (immediatePrevious == old) {
        log.info("Inside getDuration():lastImmediateTimeStamp calculated is same as oldestTime got from Engine : Setting duration = 0 ");
        // yesterday is equal to oldestTime . Only one date should be there .
        duration = 0;
      } else if (immediatePrevious > old) {
        duration = (immediatePrevious - old);
      }
    }
    return duration;
  }

  /**
   * Returns the number of days or number of 15 Minute Intervals or number of 1
   * Minute intervals from lastImmediateTimeStamp to oldestTime . <br>
   * <br>
   * 
   * @param duration
   *          duration in seconds between two time stamps.
   * @param AggregationName
   *          DAY , 15 MIN or 1 MIN Re Aggregation .
   * @return The number of Re Aggregation intervals or units.
   */
  protected int getIntervals(final long duration, final String AggregationName) {
    int intervals = 0;
    if (duration > 0) {
    if (AggregationName.equalsIgnoreCase(RE_AGG_NAME_FOR_DAY)) {
      intervals = (int) (duration / SECONDS_IN_A_DAY);
      log.info("Inside getDuration():DAY : lastImmediateTimeStamp > oldestTime got from Engine : Setting duration = "
          + duration);
    } else if (AggregationName.equalsIgnoreCase(RE_AGG_NAME_FOR_15_MIN)) {
      intervals = (int) (duration / SECONDS_IN_FIFTEEN_MINUTES);

      log.info("Inside getDuration():15 MIN: lastImmediateTimeStamp > oldestTime got from Engine : Setting duration = "
          + duration);
    } else if (AggregationName.equalsIgnoreCase(RE_AGG_NAME_FOR_1_MIN)) {

      intervals = (int) (duration / SECONDS_IN_A_MINUTE);
      log.info("Inside getDuration():1 MIN: lastImmediateTimeStamp > oldestTime got from Engine : Setting duration = "
          + duration);
    }
    }
    return intervals;

  }
  /**
   * Returns a Vector containing all the Date objects for a particular duration
   * from lastImmediateTimeStamp . <br>
   * <br>
   * 
   * @param reAggName
   *          DAY or 15 Minute or 1 Minute Re Aggregation .
   * @param lastImmediateTimeStamp
   *          The last immediate time stamp in milliseconds for a particular
   *          reAggName .
   * @param duration
   *          The number of DAY level units or number of 15 Min Units or number
   *          of 1 Min Units from lastImmediateTimeStamp to the time Re
   *          Aggregation is supported i.e . oldestTime in RAW tables + Buffer
   *          time
   * @return A Vector of Date objects .
   */
  protected List<Date> getDateObjects(final String reAggName, final long lastImmediateTimeStamp, final int intervals) {
    final Vector<Date> startTimes = new Vector<Date>();

    if (reAggName.equalsIgnoreCase(RE_AGG_NAME_FOR_DAY)) {
      log.info("DAY: Creating " + (intervals + 1) + " date objects");
      for (long i = 0; i <= intervals; i++) {
        startTimes.add(createDateObject((lastImmediateTimeStamp - (i * MILLI_SECONDS_IN_A_DAY))));
      }
    } else if (reAggName.equalsIgnoreCase(RE_AGG_NAME_FOR_15_MIN)) {
      log.info("15 MIN: Creating " + (intervals + 1) + " date objects");
      for (long i = 0; i <= intervals; i++) {
        startTimes.add(createDateObject(lastImmediateTimeStamp - i * (MILLI_SECONDS_IN_A_MINUTE * 15)));
      }

    } else if (reAggName.equalsIgnoreCase(RE_AGG_NAME_FOR_1_MIN)) {
      log.info("1 MIN: Creating " + (intervals + 1) + " date objects");
      for (long i = 0; i <= intervals; i++) {
        startTimes.add(createDateObject(lastImmediateTimeStamp - i * MILLI_SECONDS_IN_A_MINUTE));
      }
    }

    return startTimes;
  }

  /**
   * Creates a new Date object . This is used for the start times and End times
   * drop down box in adminUI <br>
   * <br>
   * 
   * @param timestamp
   *          The time in MilliSeconds .
   * @return New Date Object.
   */
  private Date createDateObject(final long timestamp) {
    return new Date(timestamp);
  }

  /**
   * Starts ReAgg Set to run now or later . <br>
   * <br>
   * 
   * @param techPack
   *          name , start Time , end Time , re Aggregation scope and boolean
   *          flag to run set now or later The name of the techpack for which Re
   *          Aggregation has to start .
   * @param starttime
   *          The Start Time for the Re Aggregation Set
   * @param enddate
   *          The End Time for the Re Aggregation Set .
   * @param reAggType
   *          This can be DAY level , 15 Minute or 1 Minute . The names for
   *          these aggregations can be defined in the web.xml file .
   * @param trueorfalse
   *          value = true will run the Re Aggregation Set straight away without
   *          any scheduling Value = false will run the Re aggregation Set for
   *          the End of the Day .
   * @return Nothing .
   */
  protected String startReAggSets(final String techPack, final String starttime, final String endTime,
      final String reAggType, final String reAggNowOrLater, final Context ctx) throws ParseException, ServerException,
      RemoteException {

    String page = "etlShowManySets_dummy.vm";
    final SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.getDefault());

    try {
      final Timestamp startTimeStamp = new Timestamp(sdf1.parse(starttime).getTime());
      final Timestamp endTimeStamp = new Timestamp(sdf1.parse(endTime).getTime());
      if (endTimeStamp.before(startTimeStamp)) {
        ctx.put("endDateLessThanStartDate", "Start Time :" + startTimeStamp + " End Time :" + endTimeStamp);
        page = "aggregationShowErrors.vm";
      } else {
        log.debug("startTimeStamp" + startTimeStamp);
        log.debug("endTimeStamp" + endTimeStamp);
        if (reAggNowOrLater.equalsIgnoreCase("ReAggregateNow")) {
          trRMI.manualCountReAgg(techPack, startTimeStamp, endTimeStamp, reAggType, false);
          log.info("ReAggregation requested without scheduling");
        } else {
          trRMI.manualCountReAgg(techPack, startTimeStamp, endTimeStamp, reAggType, true);
          log.info("ReAggregation requested with scheduling");
        }
      }
    } catch (ServerException Se) {
      log.error("ServerException in startReAggSets :");
      throw Se;
    } catch (RemoteException re) {
      log.error("RemoteException in startReAggSets :");
      throw re;
    } catch (ParseException pe) {
      log.error("ParseException in startReAggSets :");
      // timeOkFlag = false;
      throw pe;
    }

    // Adding of the set has been done . Now, Show the results
    return page;
  }
  /**
   * Prints a message to logfile and populates variables in Context. <br>
   * <br>
   *
   * @param message
   *          The message to print in log file.
   * @param e
   *          The exception that has occured
   * @param ctx
   *          The context to populate.
   * @param argument
   *          The argument to populate in ctx.
   * @param MessageToShowOnGui
   *          The value to set for argument.
   * @return aggregationShowErrors.vm i.e the page to show the errors.
   */
  protected String showErrorPageandPrintToLog(final String message,final Exception e,final Context ctx, final String argument, final String MessageToShowOnGui){
    log.error(message,e.fillInStackTrace());
    ctx.put(argument,MessageToShowOnGui);
    return "aggregationShowErrors.vm" ;
  }
}
