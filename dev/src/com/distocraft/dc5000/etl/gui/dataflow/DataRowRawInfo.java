package com.distocraft.dc5000.etl.gui.dataflow;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.CalSelect;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.DwhMonitoring;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.util.DateFormatter;

// import com.distocraft.dc5000.om.dc5000rep.TablelevelsPeer;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * This servlet will fetch for certain measurementtype the details for the selected day.
 * 
 * @author Antti Laurila, Mark Stenback, Petri Raatikainen
 */
public class DataRowRawInfo extends EtlguiServlet { // NOPMD by eheijun on 02/06/11 14:41

  private static final long serialVersionUID = 1L;

  private final Log log = LogFactory.getLog(this.getClass());

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.velocity.servlet.VelocityServlet#handleRequest(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)
   */
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response,
      final Context ctx) { // NOPMD by eheijun on 02/06/11 14:41
    Template outty = null;

    try {

      //
      // first load all measurement's and table_level's
      // Connection connDRep = ((RockFactory) ctx.get("rockDwhRep")).getConnection();

      /*
       * Vector link_list = getMeasurementTypes(); ctx.put("theLinks", link_list); TwoLevelSelectionBuilder twb = new
       * TwoLevelSelectionBuilder(); twb.buildSelectionLists(link_list); ctx.put("twoLevelSelectionBuilder", twb);
       */
      /*
       * Vector tps = Util.getTechPacks(connDRep); ctx.put("distinctTechPacks", tps);
       * 
       * // Vector table_levels = TablelevelsPeer.getTableLevels(); Vector table_levels = new Vector();
       * table_levels.add("RAW"); table_levels.add("DAY"); table_levels.add("DAYBH"); table_levels.add("COUNT"); //
       * table_levels.removeElement("PLAIN"); ctx.put("theLevels", table_levels);
       */
      //
      // next get the current date (is used at the UI if none given)

      final DbCalendar calendar = new DbCalendar(); // defaults to current date

      //
      // check if user has given any parameters
      final String fulldate = request.getParameter("date");

      String year_1 = DateFormatter.parseDateToYear(fulldate);
      String month_1 = DateFormatter.parseDateToMonth(fulldate);
      ;
      String day_1 = DateFormatter.parseDateToDay(fulldate);
      final String meas_group = request.getParameter("dgroup");
      final String meas_type = request.getParameter("dtype");
      String search_days = request.getParameter("search_days");
      final String meas_level = request.getParameter("dlevel");
      ctx.put("dlevel", meas_level);

      if (search_days == null || search_days.equals("0") || search_days.equals("")
          || Integer.parseInt(search_days) > 14) {
        search_days = "0";
      }

      ctx.put("search_days", search_days);

      // if user has given some parameter's set them to screen
      // if not - set default ones
      if (meas_group == null) {
        ctx.put("theGroup", "-");
      } else {
        ctx.put("theGroup", meas_group);
      }

      if (meas_type == null) {
        ctx.put("theType", "-");
      } else {
        ctx.put("theType", meas_type);
      }

      //
      // if user has given the date, dont change it, but if
      // not set the date as current one on the UI-screen
      String check_time = "";
      if (year_1 == null) {
        year_1 = calendar.getYearString();
        month_1 = calendar.getMonthString();
        day_1 = calendar.getDayString();
        check_time = calendar.getDbDate();
      } else {
        check_time = year_1 + "-" + month_1 + "-" + day_1;
      }

      // This sends a vector of valid years from DIM_DATE Table.
      // This is used by cal_select_1.vm
      final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh");
      final CalSelect calSelect = new CalSelect(rockDwh.getConnection());
      ctx.put("validYearRange", calSelect.getYearRange());

      ctx.put("year_1", year_1);
      ctx.put("month_1", month_1);
      ctx.put("day_1", day_1);
      ctx.put("theDate", check_time);

      final DbCalendar cal = new DbCalendar(year_1, month_1, day_1);
      cal.correctByDays(Integer.parseInt(search_days));

      //
      // if user has selected measurement type too, we have a winner
      // we can fetch the data from the table
      if (meas_type != null && !meas_type.equals("-")) {

        final List<List<Object>> detailRowInfo = DwhMonitoring.getDataRowAndKey(ctx, meas_group, meas_type, meas_level,
            check_time, true, false, search_days);
        // Vector detailRowInfo = DwhMonitoring.getDataRowUseNOkey(meas_type, "RAW", search_days, connDRep, true);
        ctx.put("detailRowInfo", detailRowInfo);
      }

      //
      // done - let Velocity know the template
      outty = getTemplate("datarowrawinfo.vm");
    } catch (ParseErrorException pee) {
      log.error("Parse error for template " + pee);
    } catch (ResourceNotFoundException rnfe) {
      log.error("Template not found " + rnfe);
    } catch (Exception e) {
      log.error("Error " + e.getMessage());
    }
    return outty;
  }

}
