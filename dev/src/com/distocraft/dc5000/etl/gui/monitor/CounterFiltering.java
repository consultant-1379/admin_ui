/*package com.distocraft.dc5000.etl.gui.monitor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.monitor.Util;

public class CounterFiltering extends EtlguiServlet {

	private transient RockFactory etlRepRockFactory = null;
	private transient RockFactory dwhrepRockFactory = null;
	
	public Template doHandleRequest(final HttpServletRequest request,
			final HttpServletResponse response, final Context ctx)
			throws SQLException, ServletException, RockException, IOException {

		Template outty = null;

		etlRepRockFactory = (RockFactory) ctx.get("rockEtlRep");
		dwhrepRockFactory = (RockFactory) ctx.get("rockDwhRep");

		String neType = request.getParameter("st");
		String techpack = request.getParameter("package");
		String type = request.getParameter("type");
		String search = StringEscapeUtils.escapeHtml(request.getParameter("search"));
		
		ctx.put("selectedTechPack", type);

		String pattern = "^[a-zA-Z0-9---_]*$";
		if (techpack == null) {
			techpack = "-";
		}

		if (techpack.matches(pattern)) {
			techpack = StringEscapeUtils.escapeHtml(techpack);
		} else {
			techpack = null;
		}

		final Map<String, String> params = new HashMap<String, String>();

		final Enumeration<?> parameters = request.getParameterNames();

		while (parameters.hasMoreElements()) {
			final String par = (String) parameters.nextElement();
			params.put(par, request.getParameter(par));
		}

		ctx.put("st", neType);
		ctx.put("package", techpack);
		ctx.put("search", search);
		
		if (etlRepRockFactory == null || dwhrepRockFactory == null) {
			ctx.put("errorMessage", "Ensure all databases are online.");

		} else {

			final List<String> netype = Util.getFRHNETypes(etlRepRockFactory.getConnection());
			ctx.put("distinctnetype", netype);

			final List<String> tps = Util.getMOType(etlRepRockFactory.getConnection(),dwhrepRockFactory.getConnection(), neType);
			ctx.put("distinctTechPacks", tps);

			if (search != null) {
				List<String> typenameinfo = new ArrayList<String>();
				typenameinfo = Util.getTypeNameinfo(techpack, search,dwhrepRockFactory.getConnection());
				ctx.put("volumeBasedMeasTypes", typenameinfo);
			}
		}

		try {
			outty = getTemplate("CounterFiltering.vm");
		} catch (Exception e) {
			throw new VelocityException(e);
		}

		return outty;

	}

}*/