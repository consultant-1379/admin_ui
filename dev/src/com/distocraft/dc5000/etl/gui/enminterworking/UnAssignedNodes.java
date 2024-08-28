package com.distocraft.dc5000.etl.gui.enminterworking;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

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
 * Servlet with Unassigned nodes implementation. <br>
 *
 **/

public class UnAssignedNodes extends EtlguiServlet{

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
		ArrayList<ArrayList<String>> un_nodes = new ArrayList<ArrayList<String>>();
	    
	    // put result into the context, which is read by the velocity engine
	    // and rendered to page with template called
	    
	    un_nodes = getUnassignedNodes(connDwh);
	    ctx.put("nodeSnap",un_nodes);

	    try 
		{
			outty = getTemplate("unassigned_nodes.vm");
		}
		catch (ResourceNotFoundException e) 
		{
			log.debug("ResourceNotFoundException (getTemplate):", e);
		}
		catch (ParseErrorException e)
		{
			log.debug("ParseErrorException (getTemplate): " + e);
		}
		catch (Exception e) 
		{
			log.debug("Exception (getTemplate): " + e);
		}
	    finally{
	    	connDwh.close();
		}
		return outty;		
	}
	
	private ArrayList<ArrayList<String>> getUnassignedNodes(Connection connDwh) {
		ArrayList<ArrayList<String>> nTable = new ArrayList<ArrayList<String>>();
		String sql = "Select FDN, NETYPE, ENM_HOSTNAME from ENIQS_Node_Assignment where ENIQ_IDENTIFIER=''";
		
		try(Statement stmt = connDwh.createStatement(); 
			ResultSet rSet = stmt.executeQuery(sql);) {
			while(rSet.next())
			{
				ArrayList<String> list = new ArrayList<String>();
				list.add(rSet.getString("FDN"));
				list.add(rSet.getString("NETYPE"));
				list.add(rSet.getString("ENM_HOSTNAME"));
				nTable.add(list);
			}
		}
		catch (Exception e)
		{
			log.error("Exception: ", e);
		}		
		return nTable;
	}
}