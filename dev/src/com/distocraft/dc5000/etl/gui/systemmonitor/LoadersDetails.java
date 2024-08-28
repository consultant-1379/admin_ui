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

public class LoadersDetails extends EtlguiServlet {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(LoadersDetails.class);

	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		Template Loaders = null;
		final String page = "LoadersDetails.vm";
		
		String date = request.getParameter("date");
		final RockFactory rockDwh = ((RockFactory) ctx.get("rockDwh"));
		final CalSelect calSelect = new CalSelect(rockDwh.getConnection());
		
		Loaders = getTemplate(page);
		ctx.put("LoaderDetailsinfo", LoaderDetailsInfo(date));
		return Loaders;
	}
	
	public ArrayList<String> LoaderDetailsInfo(String date) {
		Connection connDwhdb = null;
		connDwhdb = DatabaseConnections.getDwhDBConnection().getConnection();
		String sqlLoaderDetails="select typename as Loader,count(*) as count from LOG_SESSION_LOADER where date_id ='"+date+"'group by typename order by typename";
		Statement stmtDwhdb = null;
		ResultSet rsCount = null;
		ArrayList<String> LoaderDetailsInfo = new ArrayList<String>();
		try {
			stmtDwhdb = connDwhdb.createStatement();
			rsCount = stmtDwhdb.executeQuery(sqlLoaderDetails);
			while (rsCount.next()) {
				String typename = rsCount.getString(1);
				String loaderCount = rsCount.getString(2);
				String typename_count=typename+ "::" + loaderCount;
				LoaderDetailsInfo.add(typename_count );
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
		return LoaderDetailsInfo;

	}

}
