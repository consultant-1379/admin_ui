package com.distocraft.dc5000.etl.gui.etl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import com.distocraft.dc5000.etl.gui.common.CalSelect;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.util.Helper;
import com.distocraft.dc5000.etl.gui.util.ParamValue;

import ssc.rockfactory.RockFactory;

public class FRHHistorySet extends EtlguiServlet {
	private final Log log = LogFactory.getLog(this.getClass());

	// @Override
	public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response,
			final Context ctx) throws Exception {

		final List<List<String>> etlSets;
		final List<String> etlSetTypes;
		final List<Integer> fileCountList;

		Template outty = new Template();
		final String page = "show_frh_history.vm";
		
		final HttpSession session = request.getSession(false);
		final RockFactory rockEtlRep = (RockFactory) ctx.get("rockEtlRep");

		// get the current date (is used at the UI, if none given)
		final DbCalendar calendar = new DbCalendar();

		String selectedtechpack = request.getParameter(Helper.PARAM_SELECTED_TECHPACK);

		String year_1 = request.getParameter("year_1");
		String month_1 = request.getParameter("month_1");
		String day_1 = request.getParameter("day_1");

		String pattern = "^[a-zA-Z0-9_-]*$";

		if (selectedtechpack == null) {
			selectedtechpack = "*";
		}
		if (selectedtechpack.matches(pattern)) {
			selectedtechpack = StringEscapeUtils.escapeHtml(selectedtechpack);
		} else {
			selectedtechpack = null;
		}

		String pattern2 = "^[0-9]*$";

		if (year_1 == null) {
			year_1 = "-";
		}

		if (year_1.matches(pattern2)) {
			year_1 = StringEscapeUtils.escapeHtml(year_1);
		} else {
			year_1 = null;
		}

		if (month_1 == null) {
			month_1 = "-";
		}

		if (month_1.matches(pattern2)) {
			month_1 = StringEscapeUtils.escapeHtml(month_1);
		} else {
			month_1 = null;
		}

		if (day_1 == null) {
			day_1 = "-";
		}

		if (day_1.matches(pattern2)) {
			day_1 = StringEscapeUtils.escapeHtml(day_1);
		} else {
			day_1 = null;
		}

		if (year_1 != null) {
			session.setAttribute("year", year_1);
		} else if (session.getAttribute("year") != null) {
			year_1 = session.getAttribute("year").toString();
		} else {
			session.setAttribute("year", calendar.getYearString());
		}

		if (month_1 != null) {
			session.setAttribute("month", month_1);
		} else if (session.getAttribute("month") != null) {
			month_1 = session.getAttribute("month").toString();
		} else {
			session.setAttribute("month", calendar.getMonthString());
		}

		if (day_1 != null) {
			session.setAttribute("day", day_1);
		} else if (session.getAttribute("day") != null) {
			day_1 = session.getAttribute("day").toString();
		} else {
			session.setAttribute("day", calendar.getDayString());
		}

		if (selectedtechpack != null) {
			session.setAttribute("packageName", selectedtechpack);
		} else if (session.getAttribute("packageName") != null) {
			selectedtechpack = session.getAttribute("packageName").toString();
		} else {
			session.setAttribute("packageName", "-");
		}

		final ParamValue tpack = new ParamValue();
		if (Helper.isNotEmpty(year_1)) {
			tpack.setParam(Helper.PARAM_SELECTED_TECHPACK);
			tpack.setValue(selectedtechpack);
			tpack.setSelectedTag("selected");
			tpack.setSelected(true);
		}
		if (year_1 == null) {
			year_1 = calendar.getYearString();
			month_1 = calendar.getMonthString();
			day_1 = calendar.getDayString();
		}

		// This sends a vector of valid years from DIM_DATE Table.
		// This is used by cal_select_1.vm
		final RockFactory rockDwh = ((RockFactory) ctx.get("rockDwh"));
		final CalSelect calSelect = new CalSelect(rockDwh.getConnection());
		ctx.put("validYearRange", calSelect.getYearRange());

		ctx.put("year_1", year_1);
		ctx.put("month_1", month_1);
		ctx.put("day_1", day_1);
		ctx.put("selectedpack", selectedtechpack);

		// find all possible set types
		etlSetTypes = fetchHierarchy(rockEtlRep.getConnection());
		etlSets = callTogetCount(rockEtlRep.getConnection(), selectedtechpack, day_1, month_1, year_1);
		fileCountList=getFileCount(rockEtlRep.getConnection(), selectedtechpack, day_1, month_1, year_1);
		boolean checkDate=checkDate(year_1, month_1, day_1);
		if(checkDate)
		{
			ctx.put("isold", "true");
		}
		else
		{
			ctx.put("isols", "false");
		}

		// put the sets to context
		ctx.put("settypes", etlSetTypes);
		ctx.put("etlsets", etlSets);
		ctx.put("selectedpack", selectedtechpack);
		ctx.put("fileCountList", fileCountList);
		// return the page that should be shown
		outty = getTemplate(page);
		return outty;
	}

	public List<String> fetchHierarchy(final Connection conn) {
		final List<String> retval = new ArrayList<String>();

		//
		// Sql statement as a string
		// get all unique tech pack names (COLLECTION_NAME)
		final String sqlStmt = "SELECT DISTINCT TECHPACK_NAME FROM META_FRH_NODEASSIGNMENT ORDER BY TECHPACK_NAME";

		Statement stmt = null;
		ResultSet rset = null;
		//
		try {
			log.debug("Executing query: " + sqlStmt);

			stmt = conn.createStatement();
			rset = stmt.executeQuery(sqlStmt);

			// loop and collect all names
			while (rset.next()) {
				retval.add(rset.getString(1));
			}
			conn.commit();
		} catch (SQLException sqle) {
			// problems?
			log.error("SQLException when executing SQL: '" + sqlStmt + "'");
			log.error("Exception: ", sqle);
		} finally {
			// finally close result set and statement
			try {
				if (rset != null) {
					rset.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				log.error("Exception", e);
			}

		}

		// return with result (tech packs)
		return retval;
	}

	public int getCount(final Connection conn, final String techpack, final String dataTimeBegin,
			final String dataTimeEnd) {

		int count = -1;
		if (techpack != null && !techpack.equals("") && !techpack.equals("-")) {
			String countStmt = "SELECT COUNT (*) FROM META_FRH_STATUS WHERE JOBSTARTTIME BETWEEN '" + dataTimeBegin
					+ "' AND '" + dataTimeEnd + "' AND INTERFACE_NAME LIKE '%" + techpack + "-%'";

			ResultSet rset1 = null;

			Statement stmt1 = null;

			try {
				log.debug("Executing query: " + countStmt);
				stmt1 = conn.createStatement();
				rset1 = stmt1.executeQuery(countStmt);

				while (rset1.next()) {
					count = rset1.getInt(1);
				}
				conn.commit();

			} catch (SQLException e) {
				log.error("SQLException: " + e);
			}
			finally
			{
				try {
					if (rset1 != null) {
						rset1.close();
					}
					if (stmt1 != null) {
						stmt1.close();
					}
				} catch (Exception e) {
					log.error("Exception", e);
				}

			}
		}
		return count;
	}
	
	public List<List<String>> callTogetCount(final Connection conn, final String techpack, final String day,
			final String month, final String year) {
		final List<List<String>> retVal = new ArrayList<List<String>>();

		final String dataTime = year + "-" + month + "-" + day;
		String dataTimeBegin = dataTime + " 00:00:00.000";
		String dataTimeEnd = dataTime + " 00:15:00.000";

		for (int i = 0; i < 96; i++) {
			int count = getCount(conn, techpack, dataTimeBegin, dataTimeEnd);
			if (count < 0) {
				break;
			}
			List<String> list = new ArrayList<String>();
			list.add(dataTimeBegin);
			list.add(dataTimeEnd);
			list.add(techpack);
			list.add(String.valueOf(count));

			retVal.add(list);

			Date dtb;
			Date dte;
			try {
				dtb = StringtoDate(dataTimeBegin);
				dte = StringtoDate(dataTimeEnd);

				dtb = add15Min(dtb);
				dte = add15Min(dte);

				dataTimeBegin = DatetoString(dtb);
				dataTimeEnd = DatetoString(dte);
			} catch (ParseException e) {
				log.error("ParseException: " + e);
			}
		}
		return retVal;
	}
	
	public List<Integer> getFileCount(final Connection conn, final String techpack, final String day,
			final String month, final String year)
	{
		final String dataTime = year + "-" + month + "-" + day;
		String dataTimeBegin = dataTime + " 00:00:00.000";
		String dataTimeEnd = dataTime + " 00:15:00.000";

		List<Integer> list = new ArrayList<Integer>();
		try{
		for (int i = 0; i < 96; i++) {
			int count = getCount(conn, techpack, dataTimeBegin, dataTimeEnd);
			list.add(count);
		}
		}catch(NumberFormatException e)
		{
			log.error("NumberFormatException: "+e);
		}
		return list;
	}

	private Date StringtoDate(String date) throws ParseException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date d = df.parse(date);
		return d;
	}

	public String DatetoString(Date date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String dts = df.format(date);
		return dts;
	}

	private Date add15Min(Date date) {
		Calendar cd = new GregorianCalendar();
		cd.setTime(date);
		cd.add(Calendar.MINUTE, 15);
		Date d2 = cd.getTime();
		return d2;
	}
	
	private boolean checkDate(String year, String month, String day) throws ParseException
	{
		final String dataTime = year + "-" + month + "-" + day+ " 00:00:00.000";
		Date selectedDate=StringtoDate(dataTime);
		long currentMillis=new Date().getTime();
		long millisIn3Days=3 * 24 * 60 * 60 * 1000;
		boolean result = selectedDate.getTime() < (currentMillis - millisIn3Days);
		return result;
	}
	
}
