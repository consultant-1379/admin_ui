package com.distocraft.dc5000.etl.gui.enminterworking;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

import ssc.rockfactory.RockFactory;

/**
 * Copyright &copy; ERICSSON. All rights reserved.<br>
 * Servlet with Assigned nodes implementation. <br>
 *
 **/

public class AssignedNodes extends EtlguiServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Log log = LogFactory.getLog(this.getClass());
	private Template outty = null;

	@SuppressWarnings("deprecation")
	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		
		Connection connDwh = ((RockFactory) ctx.get("rockDwhRep")).getConnection();
		connDwh.createStatement().execute("set temporary option blocking='on';set temporary option blocking_timeout=60000;");
		String NEType = request.getParameter("NEType");
		
		ArrayList<ArrayList<String>> nodeSnap = new ArrayList<ArrayList<String>>();
		List<String> NE_Types = new ArrayList<String>();
		

		if (NEType == null) {
			
			NEType = "-";
			
		} else {
			
			nodeSnap = getNodeTable(NEType, connDwh);
			
		}
		// put result into the context, which is read by the velocity engine
		// and rendered to page with template called
		NE_Types = fetchNEtypes(connDwh);
		ctx.put("NETypes", NE_Types);
		ctx.put("NEType", NEType);
		ctx.put("nodeSnap", nodeSnap);

		try {
			
			outty = getTemplate("assigned_nodes.vm");
			
		} catch (ResourceNotFoundException e) {
			
			log.debug("ResourceNotFoundException (getTemplate):", e);
			
		} catch (ParseErrorException e) {
			
			log.debug("ParseErrorException (getTemplate): " + e);
			
		} catch (Exception e) {
			
			log.debug("Exception (getTemplate): " + e);
			
		}
		finally{
			connDwh.close();
		}

		return outty;

	}

	protected ArrayList<ArrayList<String>> getNodeTable(String NEType, Connection dwhrep) {
		ArrayList<ArrayList<String>> nTable = new ArrayList<ArrayList<String>>();
		if (NEType != null && NEType != "-") {
			
			String sql = "Select ENIQ_IDENTIFIER, FDN, NETYPE, ENM_HOSTNAME from ENIQS_Node_Assignment"
					+ " where NETYPE = '" + NEType + "';";
			
			try (Statement stmt = dwhrep.createStatement();
				 ResultSet rSet = stmt.executeQuery(sql);){
				
				while (rSet.next()) {					
					ArrayList<String> list = new ArrayList<String>();
					if (!rSet.getString("ENIQ_IDENTIFIER").equals("")) {
						list.add(rSet.getString("ENIQ_IDENTIFIER"));
						list.add(rSet.getString("FDN"));
						list.add(rSet.getString("NETYPE"));
						list.add(rSet.getString("ENM_HOSTNAME"));
						nTable.add(list);
					}
				}
			} catch (Exception e) {
				log.error("Exception while querying ENIQS_Node_Assignment: ", e);
			} 
		}
		return nTable;
	}

	protected List<String> fetchNEtypes(Connection connDwh) {
		List<String> nTable = new ArrayList<String>();
		String sql = "Select Distinct NETYPE from ENIQS_Node_Assignment where ENIQ_IDENTIFIER != ''";
		
		try(Statement stmt = connDwh.createStatement();
			ResultSet rSet = stmt.executeQuery(sql);) {
			while (rSet.next()) {
				nTable.add(rSet.getString("NETYPE"));
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
		}
		return nTable;
	}
}