package com.distocraft.dc5000.etl.gui.aggregation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import org.apache.commons.lang.StringEscapeUtils;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.CalSelect;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.monitor.Aggregation;
import com.distocraft.dc5000.etl.gui.util.ParamValue;
import com.ericsson.eniq.common.CalcFirstDayOfWeek;
import java.util.Collections;
import com.ericsson.eniq.common.Constants;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved. <br>
 * A Servlet to issue reaggregation requests. Following condition parameters
 * applies:
 * <li>Aggregation Scope (DAY, WEEK, MONTH). Selecting scope shall affect time
 * selecting components. Scope = WEEK -> selecting starting week & end week
 * etc...</li>
 * <li>Start time & end time. Select time for aggregation. AdminUI shall isssue
 * reaggregation request for each moment of time between start and end time.
 * </li>
 * <li>TechPack. TechPack name is selected. Individual aggregations are shown
 * based on techpack selection.</li>
 * <li>Aggregation. Name of the aggregation to select from. TechPack and
 * Aggregation scope determines which aggregations are shown on selection list.
 * One or multiple aggregations can be selected from this list.</li>
 * <br>
 * Issuing aggregation request: AdminUI calls ETLCEngine for issuing
 * reaggregation requests (method reaggregate in ITransferEngineRMI).
 * Reaggregate is issued for each unit of time in selection for each selected
 * aggregation. <br>
 * <br>
 * WEEK-scope aggregations shall be issued for first day of the week (monday)
 * and MONTH-scope aggregations shall be issued for first day of the month. <br>
 * <br>
 * When selected reaggregation requests are issued adminUI shall redirect the
 * browser to Show Aggregation view for selected TechPack on selection time.
 * 
 * @author Jani Vesterinen
 */
public class ReAggregationServlet extends EtlguiServlet { // NOPMD by eheijun on 02/06/11 14:22

  private static final long serialVersionUID = 1L;
  
  private final Log log = LogFactory.getLog(this.getClass());
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  
  private final int firstDayOfTheWeek = CalcFirstDayOfWeek.calcFirstDayOfWeek();
  private final int endOfWeek = (firstDayOfTheWeek > 1)?firstDayOfTheWeek-1:firstDayOfTheWeek+6;


  /**
   * @throws ParseException 
   * @throws SQLException 
   * @see com.distocraft.dc5000.etl.gui.common.EtlguiServlet#doHandleRequest(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.apache.velocity.context.Context)
   */
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, final Context ctx) throws Exception { // NOPMD by eheijun on 02/06/11 14:22
    final String page = "aggregation.vm";
    Template outtemplate = null;

    String aggregationStartDay = request.getParameter("day_1"); 
    String aggregationStartWeek = request.getParameter("week_1");
    String aggregationStartMonth = request.getParameter("month_1");
    String aggregationStartYear = request.getParameter("year_1");

    String aggregationEndDay = request.getParameter("day_2"); 
    String aggregationEndWeek = request.getParameter("week_2");
    String aggregationEndMonth = request.getParameter("month_2");
    String aggregationEndYear = request.getParameter("year_2");
    
    String level = request.getParameter("level");
    final String aggregate = StringEscapeUtils.escapeHtml(request.getParameter("aggregate"));
    String batchname = request.getParameter("batch_name");
    final String list = StringEscapeUtils.escapeHtml(request.getParameter("list"));
    //String levelnversion = request.getParameter("levelnversion");

    
   String pattern =  "^[a-zA-Z0-9_]*$";
    
    if(level == null){
    	level = "-";
    }
    if(level.matches(pattern)){
    	level = StringEscapeUtils.escapeHtml(level);
    }else{
    	level = null;
    }
    
  String pattern2 = "^[0-9]*$";
    
    if(aggregationStartYear == null){
    	aggregationStartYear = "-";
    }
    
    if(aggregationStartYear.matches(pattern2)){
    	aggregationStartYear = StringEscapeUtils.escapeHtml(aggregationStartYear);
    }else{
    	aggregationStartYear = null;
    }
    
    if(aggregationStartMonth == null){
    	aggregationStartMonth = "-";
    }
    
    
    if(aggregationStartMonth.matches(pattern2)){
    	aggregationStartMonth = StringEscapeUtils.escapeHtml(aggregationStartMonth);
    }else{
    	aggregationStartMonth = null;
    }
    
    if(aggregationStartWeek == null){
    	aggregationStartWeek = "-";
    }
    
    
    if(aggregationStartWeek.matches(pattern2)){
    	aggregationStartWeek = StringEscapeUtils.escapeHtml(aggregationStartWeek);
    }else{
    	aggregationStartWeek = null;
    }
    
    if(aggregationStartDay == null){
    	aggregationStartDay = "-";
    }
    
    
    if(aggregationStartDay.matches(pattern2)){
    	aggregationStartDay = StringEscapeUtils.escapeHtml(aggregationStartDay);
    }else{
    	aggregationStartDay = null;
    }
    
    
    
    
    
    if(aggregationEndYear == null){
    	aggregationEndYear = "-";
    }
    
    if(aggregationEndYear.matches(pattern2)){
    	aggregationEndYear = StringEscapeUtils.escapeHtml(aggregationEndYear);
    }else{
    	aggregationEndYear = null;
    }
    
    if(aggregationEndMonth == null){
    	aggregationEndMonth = "-";
    }
        
    if(aggregationEndMonth.matches(pattern2)){
    	aggregationEndMonth = StringEscapeUtils.escapeHtml(aggregationEndMonth);
    }else{
    	aggregationEndMonth = null;
    }
    
    if(aggregationEndWeek == null){
    	aggregationEndWeek = "-";
    }
    
    
    if(aggregationEndWeek.matches(pattern2)){
    	aggregationEndWeek = StringEscapeUtils.escapeHtml(aggregationEndWeek);
    }else{
    	aggregationEndWeek = null;
    }
    
    if(aggregationEndDay == null){
    	aggregationEndDay = "-";
    }
    
    if(aggregationEndDay.matches(pattern2)){
    	aggregationEndDay = StringEscapeUtils.escapeHtml(aggregationEndDay);
    }else{
    	aggregationEndDay = null;
    }
    
  
    
    DbCalendar selectedStartDay = null;
    
    DbCalendar selectedEndDay = null;
    
    if (level != null && level.equals("DAY")) {
      selectedStartDay = new DbCalendar();
      selectedStartDay.setFirstDayOfWeek(firstDayOfTheWeek);
      selectedStartDay.setMinimalDaysInFirstWeek(4);
      selectedStartDay.correctByDays(-1);
      
      selectedEndDay = new DbCalendar();
      selectedEndDay.setFirstDayOfWeek(firstDayOfTheWeek);
      selectedEndDay.setMinimalDaysInFirstWeek(4);
      selectedEndDay.correctByDays(-1);

      if (aggregationStartYear != null) {
        selectedStartDay.setYear(aggregationStartYear);
      }
      if (aggregationStartMonth != null) {
        selectedStartDay.setMonth(aggregationStartMonth);
      }
      if (aggregationStartDay != null) {
        selectedStartDay.setDay(aggregationStartDay);
      }
      if (aggregationEndYear != null) {
        selectedEndDay.setYear(aggregationEndYear);
      }
      if (aggregationEndMonth != null) {
        selectedEndDay.setMonth(aggregationEndMonth);
      }
      if (aggregationEndDay != null) {
        selectedEndDay.setDay(aggregationEndDay);
      }
    } else if (level != null && level.equals("WEEK")) {
      selectedStartDay = new DbCalendar();
      selectedStartDay.setFirstDayOfWeek(firstDayOfTheWeek);
      selectedStartDay.setMinimalDaysInFirstWeek(4);
      selectedStartDay.correctByDays(-7);
      
      selectedEndDay = new DbCalendar();
      selectedEndDay.setFirstDayOfWeek(firstDayOfTheWeek);
      selectedEndDay.setMinimalDaysInFirstWeek(4);
      selectedEndDay.correctByDays(-7);

      if (aggregationStartYear != null) {
        selectedStartDay.setYear(aggregationStartYear);
      }
      if (aggregationStartMonth != null) {
        selectedStartDay.setMonth(aggregationStartMonth);
      }
      if (aggregationStartDay != null) {
        selectedStartDay.setDay(aggregationStartDay);
      }
      if (aggregationEndYear != null) {
        selectedEndDay.setYear(aggregationEndYear);
      }
      if (aggregationEndMonth != null) {
        selectedEndDay.setMonth(aggregationEndMonth);
      }
      if (aggregationEndDay != null) {
        selectedEndDay.setDay(aggregationEndDay);
      }
    } else if (level != null && level.equals("MONTH")) {
      selectedStartDay = new DbCalendar();
      selectedStartDay.setFirstDayOfWeek(firstDayOfTheWeek);
      selectedStartDay.setMinimalDaysInFirstWeek(4);
      selectedStartDay.correctByMonths(-1);
      
      selectedEndDay = new DbCalendar();
      selectedEndDay.setFirstDayOfWeek(firstDayOfTheWeek);
      selectedEndDay.setMinimalDaysInFirstWeek(4);
      
      if (aggregationStartYear != null) {
        selectedStartDay.setYear(aggregationStartYear);
      }
      if (aggregationStartMonth != null) {
        selectedStartDay.setMonth(aggregationStartMonth);
      }
      selectedStartDay.setDay(1);
      if (aggregationEndYear != null) {
        selectedEndDay.setYear(aggregationEndYear);
      }
      if (aggregationEndMonth != null) {
        selectedEndDay.setMonth(aggregationEndMonth);
      }
      selectedEndDay.setDay(1);
      selectedEndDay.correctByDays(-1);
    }

    final HttpSession session = request.getSession(false);

    final RockFactory rockDwhRep = (RockFactory) ctx.get("rockDwhRep");
    final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh");

    if (aggregationStartYear != null) {
      session.setAttribute("year", aggregationStartYear);
    } else if (session.getAttribute("year") != null) {
      aggregationStartYear = session.getAttribute("year").toString();
    } else if (selectedStartDay != null) {
      final String tmpYear = selectedStartDay.getYearString();
      session.setAttribute("year", tmpYear);
      aggregationStartYear = tmpYear;
    }

    if (aggregationEndYear != null) {
      session.setAttribute("year2", aggregationEndYear);
    } else if (session.getAttribute("year2") != null) {
      aggregationEndYear = session.getAttribute("year2").toString();
    } else if (selectedEndDay != null) {
      final String tmpYear = selectedEndDay.getYearString();
      session.setAttribute("year2", tmpYear);
      aggregationEndYear = tmpYear;
    }

    if (aggregationStartMonth != null) {
      session.setAttribute("month", aggregationStartMonth);
    } else if (session.getAttribute("month") != null) {
      aggregationStartMonth = session.getAttribute("month").toString();
    } else if (selectedStartDay != null) {
      final String tmpMonth = selectedStartDay.getMonthString();
      session.setAttribute("month", tmpMonth);
      aggregationStartMonth = tmpMonth;
    }

    if (aggregationEndMonth != null) {
      session.setAttribute("month2", aggregationEndMonth);
    } else if (session.getAttribute("month2") != null) {
      aggregationEndMonth = session.getAttribute("month2").toString();
    } else if (selectedEndDay != null) {
      final String tmpMonth = selectedEndDay.getMonthString();
      session.setAttribute("month2", tmpMonth);
      aggregationEndMonth = tmpMonth;
    }

    if (aggregationStartWeek != null) {
      session.setAttribute("week", aggregationStartWeek);
    } else if (session.getAttribute("week") != null) {
      aggregationStartWeek = session.getAttribute("week").toString();
    } else if (selectedStartDay != null) {
      final int tmpWeek = selectedStartDay.get(DbCalendar.WEEK_OF_YEAR);
      session.setAttribute("week", tmpWeek);
      aggregationStartWeek = String.valueOf(tmpWeek);
    }

    if (aggregationEndWeek != null) {
      session.setAttribute("week2", aggregationEndWeek);
    } else if (session.getAttribute("week2") != null) {
      aggregationEndWeek = session.getAttribute("week2").toString();
    } else if (selectedEndDay != null) {
      final int tmpWeek = selectedEndDay.get(DbCalendar.WEEK_OF_YEAR);
      session.setAttribute("week2", tmpWeek);
      aggregationEndWeek = String.valueOf(tmpWeek);
    }

    if (aggregationStartDay != null) {
      session.setAttribute("day", aggregationStartDay);
    } else if (session.getAttribute("day") != null) {
      aggregationStartDay = session.getAttribute("day").toString();
    } else if (selectedStartDay != null) {
      final String tmpDay = selectedStartDay.getDayString();
      session.setAttribute("day", tmpDay);
      aggregationStartDay = tmpDay;
    }

    if (aggregationEndDay != null) {
      session.setAttribute("day2", aggregationEndDay);
    } else if (session.getAttribute("day2") != null) {
      aggregationEndDay = session.getAttribute("day2").toString();
    } else if (selectedEndDay != null) {
      final String tmpDay = selectedEndDay.getDayString();
      session.setAttribute("day2", tmpDay);
      aggregationEndDay = tmpDay;
    }

    if (aggregationStartYear != null && aggregationStartMonth != null && aggregationStartDay != null 
        && aggregationEndYear != null && aggregationEndMonth != null && aggregationEndDay != null) {
      final Date date_1 = sdf.parse (aggregationStartYear + "-" + aggregationStartMonth + "-" + aggregationStartDay);
      final Date date_2 = sdf.parse (aggregationEndYear + "-" + aggregationEndMonth + "-" + aggregationEndDay);
      
      if (date_1.after(date_2)) {
        aggregationEndYear = aggregationStartYear;
        aggregationEndMonth = aggregationStartMonth;
        aggregationEndDay = aggregationStartDay;
        session.setAttribute("year2", aggregationStartYear);
        session.setAttribute("month2", aggregationStartMonth);
        session.setAttribute("day2", aggregationStartDay);
      }
    }

    //This is used by cal_select_1.vm
    /*CalSelect calSelect = new CalSelect(null);
    final Vector<String> futureYearRange = calSelect.getFutureRange(aggregationStartYear);
    ctx.put("futureYearRange", futureYearRange);*/
    
    final CalSelect calSelect = new CalSelect(rockDwh.getConnection());
    ctx.put("validYearRange", calSelect.getYearRange());

    ctx.put("year_1", aggregationStartYear);
    ctx.put("month_1", aggregationStartMonth);
    ctx.put("week_1", aggregationStartWeek);
    ctx.put("day_1", aggregationStartDay);
    
    ctx.put("year_2", aggregationEndYear);
    ctx.put("month_2", aggregationEndMonth);
    ctx.put("week_2", aggregationEndWeek);
    ctx.put("day_2", aggregationEndDay);

    
    String tpName = "-";
    
    if (batchname != null) {
    
      tpName = parseValue(false, batchname);
    
      if (tpName != null) {
        session.setAttribute("tpName", tpName);
      } else if (session.getAttribute("tpName") != null) {
        tpName = session.getAttribute("tpName").toString();
      } else {
        session.setAttribute("tpName", "-");
      }
    }
    
    ctx.put("batch_name", batchname != null ? "<option value=\"" + batchname + "\">" + tpName + "</option>" : "");
    ctx.put("level", level != null ? level : "");

    List<String> scopes = getAggregationScope(rockDwhRep.getConnection());
    Collections.replaceAll(scopes, Constants.ROPAGGSCOPE, "COUNT");
    ctx.put("scopes", scopes);

    if (level != null && !level.equals("")) {
      // Vector tps = Util.getTechPacks(connDwhRep);
      final List<ParamValue> tps = getTechPacks(rockDwhRep.getConnection(), level);
      ctx.put("techpacks", tps);
    }

    if (list != null && !list.equals("")) {

      DbCalendar start = null;
      DbCalendar end = null;

      final DbCalendar calendar = new DbCalendar();
      calendar.setFirstDayOfWeek(firstDayOfTheWeek);
      calendar.setMinimalDaysInFirstWeek(4);
      calendar.correctByDays(-1);

      if (aggregationStartWeek == null) {
        aggregationStartWeek = String.valueOf(calendar.get(DbCalendar.WEEK_OF_YEAR) - 1);
        if (aggregationStartWeek.equals("0")) {
          aggregationStartWeek = "1";
        }
      }
      if (aggregationEndWeek == null) {
        aggregationEndWeek = String.valueOf(calendar.get(DbCalendar.WEEK_OF_YEAR) - 1);
        if (aggregationEndWeek.equals("0")) {
          aggregationEndWeek = "1";
        }
      }

      if (level.equals("DAY")) {
        start = createZeroTimeCalendar(aggregationStartDay, aggregationStartMonth, aggregationStartYear);
        end = createZeroTimeCalendar(aggregationEndDay, aggregationEndMonth, aggregationEndYear);
      } else if (level.equals("WEEK")) {
        start = new DbCalendar();
        end = new DbCalendar();
        end.set(DbCalendar.YEAR, Integer.parseInt(aggregationEndYear));
        end.setFirstDayOfWeek(firstDayOfTheWeek);
        end.setMinimalDaysInFirstWeek(4);
        end.set(DbCalendar.YEAR, Integer.parseInt(aggregationEndYear));
        end.set(DbCalendar.WEEK_OF_YEAR, Integer.parseInt(aggregationEndWeek));
        end.set(DbCalendar.DAY_OF_WEEK, endOfWeek);

        start.set(DbCalendar.YEAR, Integer.parseInt(aggregationStartYear));
        start.setFirstDayOfWeek(firstDayOfTheWeek);
        start.setMinimalDaysInFirstWeek(4);
        start.set(DbCalendar.YEAR, Integer.parseInt(aggregationStartYear));
        start.set(DbCalendar.WEEK_OF_YEAR, Integer.parseInt(aggregationStartWeek));
        start.set(DbCalendar.DAY_OF_WEEK, firstDayOfTheWeek);
      } else if (level.equals("MONTH")) {
        start = new DbCalendar(aggregationStartYear, aggregationStartMonth, "1", 0, 0, 0);
        end = new DbCalendar(aggregationEndYear, aggregationEndMonth, "1", 0, 0, 0);
        end = new DbCalendar(aggregationEndYear, aggregationEndMonth, String.valueOf(end
            .getActualMaximum(DbCalendar.DAY_OF_MONTH)), 0, 0, 0);
      }

      log.debug(start.getTime().toString());
      log.debug(end.getTime().toString());
      log.debug("batchname " + batchname);

      final String p_version = parseValue(true, batchname);
      ctx.put("batch_name", batchname != null ? "<option value=\"" + batchname + "\">" + tpName + "</option>" : "");
      ctx.put("aggregations", getAggregations(rockDwh.getConnection(), getAggregationNames(rockDwhRep.getConnection(), level, p_version), tpName,start, end));

    }

    // try {
    boolean error = false;
    try {
      outtemplate = getTemplate(page);
    } catch (Exception e) {
      throw new VelocityException(e);
    }
    if (aggregate != null) {
      ctx.put("batch_name", parseValue(false, batchname));
      final String[] aggregated = request.getParameterValues("aggregated");
      final Aggregation aggregation = new Aggregation();
      for (int i = 0; i < aggregated.length; i++) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOPMD by eheijun on 02/06/11 14:22
        final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd"); // NOPMD by eheijun on 02/06/11 14:23
        sdf.setCalendar(Calendar.getInstance());
        try {
          sdf.parse(parseValue(false, aggregated[i]));
        } catch(ParseException pe) {
          try {
            sdf2.parse(parseValue(false, aggregated[i]));
            sdf = sdf2;
          } catch(Exception e) {
            log.error(e);
          }
        }        
        catch (Exception e) {
          log.error(e);
        }

        error = aggregation.aggregate(parseValue(true, aggregated[i]), sdf.getCalendar().getTimeInMillis());
        log.debug("Aggregation requested: " + parseValue(true, aggregated[i]) + " error: " + error);
        if (error) {
          break;
        }
      }

      if (error) {
        ctx.put("error", "Manual aggregation request failed.");
        DbCalendar start = null;
        DbCalendar end = null;

        if (level.equals("DAY")) {
          start = createZeroTimeCalendar(aggregationStartDay, aggregationStartMonth, aggregationStartYear);
          end = createZeroTimeCalendar(aggregationEndDay, aggregationEndMonth, aggregationEndYear);
        } else if (level.equals("WEEK")) {
          start = new DbCalendar();
          end = new DbCalendar();
          end.setFirstDayOfWeek(firstDayOfTheWeek);
          end.setMinimalDaysInFirstWeek(4);
          end.set(DbCalendar.WEEK_OF_YEAR, Integer.parseInt(aggregationEndWeek));
          end.set(DbCalendar.DAY_OF_WEEK, endOfWeek);
          start.setFirstDayOfWeek(firstDayOfTheWeek);
          start.setMinimalDaysInFirstWeek(4);
          start.set(DbCalendar.WEEK_OF_YEAR, Integer.parseInt(aggregationStartWeek));
          start.set(DbCalendar.DAY_OF_WEEK, firstDayOfTheWeek);
          ctx.put("day_1", String.valueOf(start.get(Calendar.DAY_OF_MONTH)));
        } else if (level.equals("MONTH")) {
          start = new DbCalendar(aggregationStartYear, aggregationStartMonth, "1", 0, 0, 0);
          end = new DbCalendar(aggregationEndYear, aggregationEndMonth, "1", 0, 0, 0);
          end = new DbCalendar(aggregationEndYear, aggregationEndMonth, String.valueOf(end
              .getActualMaximum(DbCalendar.DAY_OF_MONTH)), 0, 0, 0);
        }

        log.debug(start.getTime().toString());
        log.debug(end.getTime().toString());
        final String p_version = parseValue(true, batchname);
        ctx.put("batch_name", batchname != null ? "<option value=\"" + batchname + "\">" + tpName + "</option>" : "");
        ctx.put("aggregations", getAggregations(rockDwh.getConnection(), getAggregationNames(rockDwhRep.getConnection(), level, p_version), tpName, start, end));
      } else {

        String batchName = parseValue(false, batchname);
        // Remove the string "CUSTOM_" from the start of the type so that the correct techpack is selected in the "Show aggregations" page.
        // For example CUSTOM_DC_E_RAN becomes DC_E_RAN.
        batchName = batchName.replaceFirst("CUSTOM_","");
        
        ctx.put("batch_name", batchName);
        if (aggregationEndDay == null) {
          final Calendar c = Calendar.getInstance();
          c.setFirstDayOfWeek(firstDayOfTheWeek);
          c.setMinimalDaysInFirstWeek(4);
          c.set(DbCalendar.WEEK_OF_YEAR, Integer.parseInt(aggregationEndWeek));
          log.debug(String.valueOf(c.getActualMaximum(Calendar.MONTH)));
          ctx.put("day_1", String.valueOf(c.getActualMaximum(Calendar.DAY_OF_MONTH)));
        }
        if (aggregationEndMonth == null) {
          final Calendar c = Calendar.getInstance();
          c.setFirstDayOfWeek(firstDayOfTheWeek);
          c.setMinimalDaysInFirstWeek(4);
          c.set(DbCalendar.WEEK_OF_YEAR, Integer.parseInt(aggregationEndWeek));
          c.set(Calendar.DAY_OF_WEEK, endOfWeek);
          log.debug(String.valueOf(c.getActualMaximum(Calendar.MONTH)));
          ctx.put("day_1", String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
          ctx.put("month_1", String.valueOf(c.get(Calendar.MONTH) + 1));
        }
      }
      log.debug("All manual aggregations requested..");
      // year_1=$year_1&month_1=$month_1&day_1=$day_1&type=$batch_name";
      if (!error) {
        try {
          outtemplate = getTemplate("aggregation_dummy.vm");
        } catch (Exception e) {
          throw new VelocityException(e);
        }
      } else {
        try {
          outtemplate = getTemplate(page);
        } catch (Exception e) {
          throw new VelocityException(e);
        }
      }
    }
    /*
     * } catch (ParseErrorException pee) { log.error("Parse error for template " +
     * pee); } catch (ResourceNotFoundException rnfe) { log.error("Template not
     * found " + rnfe); } catch (Exception e) { log.error("Error " +
     * e.getMessage()); }
     */
    return outtemplate;
  }

  /**
   * Parses values from given String. (String has to be in foo&bar format. '&'
   * sign acts as a delimeter.)
   * 
   * @param b -
   *          if set to true, parses first part of string. if b is set to false,
   *          parses last part of String.
   * @param string
   * @return value
   */
  private String parseValue(final boolean b, final String string) {
    final StringTokenizer st = new StringTokenizer(string, "&");
    // String ret = "";
    if (b) {
      return st.nextToken();
    } else {
      st.nextToken();
      return st.nextToken();
    }

    //return ret;
  }

  /**
   * Gets aggregation scopes from database (dc5000rep). <br>
   * Table Aggregation is used.
   * 
   * @return vector that holds scope
   * @throws SQLException 
   */
  public List<String> getAggregationScope(final Connection conn) throws SQLException {
    final String sql = "SELECT DISTINCT AGGREGATIONSCOPE FROM Aggregation";
    final Vector<String> scopes = new Vector<String>();
    Statement st = null;
    ResultSet rs = null;
    try {
      st = conn.createStatement();
      rs = st.executeQuery(sql);

      while (rs.next()) {
        scopes.add(rs.getString(1));
      }
    } finally {
      try {
        rs.close();
        st.close();
      } catch(SQLException e) {
        log.error(e);
      }
    }
    return scopes;
  }

  /**
   * returns Vector that holds paramvalues. Paramvalue objects 'param' is
   * tpackname and 'value' is versionid. <br>
   * <br>
   * (dc5000rep) -database is used <br>
   * <br>
   * Aggregation and Versioning tables are used.
   * 
   * @param scope
   *          Scope for aggregation (e.g. WEEK, MONTH or DAY)
   * @return Vector holds values for tachpack names
   */

  private List<ParamValue> getTechPacks(final Connection conn, final String scope) {
    /*
    String sql = "SELECT DISTINCT v.TECHPACK_NAME, a.VERSIONID FROM Aggregation a, "
        + "Versioning v WHERE a.VERSIONID = v.VERSIONID AND a.AGGREGATIONSCOPE = '" + scope + "' AND v.status = 1";
    */
    final String sql = "SELECT DISTINCT t.TECHPACK_NAME, a.VERSIONID FROM Aggregation a, TPActivation t " +
            "WHERE a.VERSIONID = t.VERSIONID AND a.AGGREGATIONSCOPE = '" + scope + "' AND t.status='ACTIVE'";
    
    final Vector<ParamValue> packs = new Vector<ParamValue>();
    log.debug(sql);
    Statement st = null;
    ResultSet rs = null;
    try {
      st = conn.createStatement();
      rs = st.executeQuery(sql);

      while (rs.next()) {
        final ParamValue v = new ParamValue(); // NOPMD by eheijun on 02/06/11 14:23
        v.setParam(rs.getString(1));
        v.setValue(rs.getString(2));
        packs.add(v);
      }
    } catch (SQLException e) {
      log.error(e);
    } finally {
      try {
        rs.close();
        st.close();
      } catch(SQLException e) {
        log.error(e);
      }
    }
    return packs;
  }

  /**
   * (dc5000rep) -database is used <br>
   * <br>
   * Aggregation table is used.
   * 
   * @param scope -
   *          Scope for aggregation (e.g. WEEK, MONTH or DAY)
   * @param version -
   *          Version for aggregation
   * @return Vector that holds
   * @see com.distocraft.dc5000.etl.gui.util.ParamValue - objects. Return value
   *      holds values for aggregation names.
   */
  public List<ParamValue> getAggregationNames(final Connection conn, final String scope, final String version) {

    String tmp = "";

    if (scope.equalsIgnoreCase("day")) {
      tmp = "OR AGGREGATIONSCOPE='COUNT'";
    }

    final String sql = "SELECT DISTINCT a.AGGREGATION, a.AGGREGATIONTYPE FROM Aggregation a WHERE (AGGREGATIONSCOPE='"
        + scope + "' " + tmp + ") AND VERSIONID='" + version + "'";

    final Vector<ParamValue> aggnames = new Vector<ParamValue>();
    log.debug(sql);
    Statement st = null;
    ResultSet rs = null;
    try {
      st = conn.createStatement();
      rs = st.executeQuery(sql);

      while (rs.next()) {
        final ParamValue v = new ParamValue(); // NOPMD by eheijun on 02/06/11 14:23
        v.setParam(rs.getString(1));
        v.setValue(rs.getString(2));
        aggnames.add(v);
      }
    } catch (SQLException e) {
      log.error("SQLException ", e);
    } finally {
      try {
        rs.close();
        st.close();
      } catch (SQLException e) {
        log.error("SQLException ", e);
      }
      
    }
    return aggnames;
  }

  /**
   * Gets all the aggregations from database. (dc5000rep) -database is used <br>
   * <br>
   * Log_aggregationstatus table is used.
   * 
   * @param aggregations
   *          Vector that holds
   * @see com.distocraft.dc5000.etl.gui.util.ParamValue - objects.
   * @param startdate
   * @see com.distocraft.dc5000.etl.gui.common.DbCalendar aggregation start date
   * @param enddate
   * @see com.distocraft.dc5000.etl.gui.common.DbCalendar aggregation end date
   * @return Vector that holds all the aggregations with given parameter
   *         description.
   */
  public List<ParamValue> getAggregations(final Connection conn, final List<ParamValue> aggregations, final String tepackname,final DbCalendar startdate, final DbCalendar enddate) {
    if (aggregations.size() == 0) {
      return new Vector<ParamValue>();
    }
    
    final StringBuffer aggrsql = new StringBuffer();
    aggrsql.append("(");
    
    // Fix for TR HQ89725, HQ95844 & HQ94262
    /*
    for (int i = 0; i < aggregations.size(); i++) {
      final ParamValue val = aggregations.get(i);
      // (a.AGGREGATION = 'DC_E_BSS_BSC_DAY' OR a.AGGREGATION =
      // 'DC_E_BSS_BSCGPRS_DAY')
      aggrsql.append(" (a.AGGREGATION = '");
      aggrsql.append(val.getParam());
      if (i < aggregations.size() - 1) {
        aggrsql.append("') OR ");
      } else {
        aggrsql.append("')) ");
      }
    }
	*/
    
   	   final String sql = "SELECT distinct * FROM LOG_AGGREGATIONSTATUS a WHERE " 
                //+ "a.AGGREGATION like '%"  
        //+ tepackname + "%'" 
        //+ " AND 
        + "(a.datadate between date('" + startdate.getDbDate() + "') and date('" + enddate.getDbDate() 
        + "')) order by a.aggregation, a.datadate";

        
    log.debug(sql);
        
    final Vector<ParamValue> logAggregationstatus = new Vector<ParamValue>();
    Statement st = null;
    ResultSet rs = null;
        
    try {
      st = conn.createStatement();
      rs = st.executeQuery(sql);

      while (rs.next()) {
        final ParamValue v = new ParamValue(); // NOPMD by eheijun on 02/06/11 14:23
        v.setParam(rs.getString("AGGREGATION"));
        v.setValue(rs.getString("DATADATE"));
        logAggregationstatus.add(v);
      }
    } catch (SQLException e) {
      log.error("SQLException", e);
    } finally {
      try {
        rs.close();
        st.close();
      } catch (SQLException e) {
        log.error("SQLException ", e);
      }
    }
    
    int logAggregationstatusSize = logAggregationstatus.size();
    int aggregationsSize = aggregations.size();
    int outerLoop = 0;
    int innerLoop =0;
    
    ParamValue aggnamesParam;
    ParamValue aggregationsParam;
    
    while(outerLoop < logAggregationstatusSize) {
    	aggregationsParam = logAggregationstatus.get(outerLoop);
    	
    	while(innerLoop <aggregationsSize) {
    		aggnamesParam = aggregations.get(innerLoop);
    		
    		if(aggnamesParam.getParam().contentEquals(aggregationsParam.getParam())) {
    			innerLoop = 0;
    			break;
    		}
    		
    		innerLoop++;
    	}
    	
    	if(innerLoop != 0) {
    		logAggregationstatus.remove(outerLoop);
    		innerLoop = 0; 
    		logAggregationstatusSize = logAggregationstatus.size();
    	}
    	else {
    		outerLoop++;
    	}
    }

    return logAggregationstatus;
  }

  /**
   * Create a calendar from user (String) values. The time values of the
   * calendar is 00:00:00.
   * 
   * @param aggregationEndDay
   * @param aggregationEndMonth
   * @param aggregationEndYear
   * @return dbcalendar
   */
  private DbCalendar createZeroTimeCalendar(final String aggregationEndDay, final String aggregationEndMonth,
      final String aggregationEndYear) {

    final DbCalendar cal = new DbCalendar();
    cal.setFirstDayOfWeek(firstDayOfTheWeek);
    cal.setMinimalDaysInFirstWeek(4);
    cal.set(aggregationEndYear, aggregationEndMonth, aggregationEndDay, 0, 0, 0);
    cal.set(DbCalendar.MILLISECOND, 0);

    return cal;
  }
}