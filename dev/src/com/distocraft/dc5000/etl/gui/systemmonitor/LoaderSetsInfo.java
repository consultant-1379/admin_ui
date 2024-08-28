package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.CalSelect;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.ericsson.eniq.common.DatabaseConnections;

import ssc.rockfactory.RockFactory;

public class LoaderSetsInfo extends EtlguiServlet {

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(LoaderSetsInfo.class);

	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		Template Loaders = null;
		final String page = "LoadersCount.vm";
		final DbCalendar calendar = new DbCalendar();
		String year_1 = request.getParameter("year_1");
		String month_1 = request.getParameter("month_1");
		String day_1 = request.getParameter("day_1");
		String year_2 = request.getParameter("year_2");
		String month_2 = request.getParameter("month_2");
		String day_2 = request.getParameter("day_2");
		String searchButton = StringEscapeUtils.escapeHtml(request.getParameter("Search"));

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
		if (year_1 == null) {
			year_1 = calendar.getYearString();
			month_1 = calendar.getMonthString();
			day_1 = calendar.getDayString();
		}

		if (year_2 == null) {
			year_2 = "-";
		}

		if (year_2.matches(pattern2)) {
			year_2 = StringEscapeUtils.escapeHtml(year_2);
		} else {
			year_2 = null;
		}

		if (month_2 == null) {
			month_2 = "-";
		}

		if (month_2.matches(pattern2)) {
			month_2 = StringEscapeUtils.escapeHtml(month_2);
		} else {
			month_2 = null;
		}

		if (day_2 == null) {
			day_2 = "-";
		}

		if (day_2.matches(pattern2)) {
			day_2 = StringEscapeUtils.escapeHtml(day_2);
		} else {
			day_2 = null;
		}
		if (year_2 == null) {
			year_2 = calendar.getYearString();
			month_2 = calendar.getMonthString();
			day_2 = calendar.getDayString();
		}
		final RockFactory rockDwh = ((RockFactory) ctx.get("rockDwh"));
		final CalSelect calSelect = new CalSelect(rockDwh.getConnection());
		ctx.put("validYearRange", calSelect.getYearRange());
		ctx.put("year_1", year_1);
		ctx.put("month_1", month_1);
		ctx.put("day_1", day_1);
		ctx.put("year_2", year_2);
		ctx.put("month_2", month_2);
		ctx.put("day_2", day_2);
		String selectedDate1 = year_1 + "-" + month_1 + "-" + day_1;
		String selectedDate2 = year_2 + "-" + month_2 + "-" + day_2;
		
		Loaders = getTemplate(page);
		/* if user press button then this method will be called*/
		if (searchButton != null) {
			ctx.put("LoaderDateInfo", LoaderCountInfo(selectedDate1,selectedDate2));
		}
		ctx.put("LoaderInfo", LoaderCount24Info());
		return Loaders;
	}
	/* method used for  queryng loader counts from database for last executed sets sort by date*/
	public ArrayList<String> LoaderCount24Info() {
		Connection connDwhdb = null;
		connDwhdb = DatabaseConnections.getDwhDBConnection().getConnection();

		String sqlLoaderCount = "BEGIN DECLARE @Maxdt datetime; "
				+ "SET @Maxdt=(select MAX(datatime) FROM dc.LOG_SESSION_LOADER); "
				+ "SELECT MIN(datatime) AS start_time, @Maxdt AS end_time, COUNT(DISTINCT typename) "
				+ "AS Loader_count FROM dc.LOG_SESSION_LOADER WHERE datatime >= DATEADD(hh,-24,@Maxdt);END";

		Statement stmtDwhdb = null;
		ResultSet rsCount = null;
		ArrayList<String> LoaderInfo = new ArrayList<String>();
		try {
			stmtDwhdb = connDwhdb.createStatement();
			rsCount = stmtDwhdb.executeQuery(sqlLoaderCount);
			while (rsCount.next()) {
				String startTime = rsCount.getString(1);
				String endTime = rsCount.getString(2);
				String loaderCount = rsCount.getString(3);
				LoaderInfo.add(startTime);
				LoaderInfo.add(endTime);
				LoaderInfo.add(loaderCount);
			}
		} catch (SQLException e) {
			log.warn("Unable to execute query " + e);
		} finally {
			try {
				if (connDwhdb != null)
					connDwhdb.close();
				if (stmtDwhdb != null)
					stmtDwhdb.close();
				if (rsCount != null)
					rsCount.close();
			} catch (SQLException e) {
				log.warn("Unable to close database connections " + e);
			}
		}
		return LoaderInfo;
	}
	/* method used for filtering loader set count from database in between two dates*/
	public ArrayList<String> LoaderCountInfo(String date1,String date2) {
		Connection connDwhdb = null;
		connDwhdb = DatabaseConnections.getDwhDBConnection().getConnection();
		String sqlLoaderCount1 = " select date_id,count(distinct typename) as Loader_count "
				+ "from LOG_SESSION_LOADER where date_id between '"+ date1+"'and '"+date2+"'group by date_id order by date_id";
		Statement stmtDwhdb = null;
		ResultSet rsCount = null;
		ArrayList<String> LoaderDateInfo = new ArrayList<String>();
		try {
			stmtDwhdb = connDwhdb.createStatement();
			rsCount = stmtDwhdb.executeQuery(sqlLoaderCount1);
			while (rsCount.next()) {
				String startTime = rsCount.getString(1);
				String loaderCount = rsCount.getString(2);
				String date_count=startTime+ "::" + loaderCount;
				LoaderDateInfo.add(date_count );
			}
		} catch (SQLException e) {
			log.warn("Unable to execute query " + e);
		} finally {
			try {
				if (connDwhdb != null)
					connDwhdb.close();
				if (stmtDwhdb != null)
					stmtDwhdb.close();
				if (rsCount != null)
					rsCount.close();
			} catch (SQLException e) {
				log.warn("Unable to close database connections " + e);
			}
		}
		return LoaderDateInfo;

	}

}
