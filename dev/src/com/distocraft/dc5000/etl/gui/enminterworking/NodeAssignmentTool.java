package com.distocraft.dc5000.etl.gui.enminterworking;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


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
 * Servlet with Node Assignment Tool implementation. <br>
 *
 **/

public class NodeAssignmentTool extends EtlguiServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(NodeAssignmentTool.class.getClass());
	private Template outty = null;
	private String role = null;
	
	@SuppressWarnings("deprecation")
	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		Connection connDwh = ((RockFactory) ctx.get("rockDwhRep")).getConnection();
		Connection connDRep = ((RockFactory) ctx.get("rockDwhRep")).getConnection();
		connDwh.createStatement().execute("set temporary option blocking='on';set temporary option blocking_timeout=60000;");
		int assignedNodes = getAssignedNodes(connDwh);
	    int unassignedNodes = getUnassignedNodes(connDwh);
	    
	    ctx.put("assignedNodes",assignedNodes);
	    ctx.put("unassignedNodes", unassignedNodes);
	    
	    role = EnmInterUtils.getSelfRole(connDRep);
	    if (role.equals("SLAVE")){
			ctx.put("ERROR", true);
		}

	    try 
		{
			outty = getTemplate("node_assignment_tool.vm");
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

			closeConn(connDRep);
		}
		return outty;
		
	}
	

	private void closeConn(Connection conn) {

		log.info("Closing Connection");
		try {
			if(conn!=null) {
				conn.close();
			}
			log.info("Connection closed successfully.");
		}catch(Exception e) {
			log.error("Error while closing connection.", e);
		}
	}
	
	private int getUnassignedNodes(Connection connDwh) {
		int unassignedNodes = 0;
		Statement stmt = null;
		ResultSet rSet = null;
		try
		{
			stmt = connDwh.createStatement();
			rSet = stmt.executeQuery("Select count(*) from ENIQS_Node_Assignment where ENIQ_IDENTIFIER = ''");
			
			while(rSet.next())
			{
				unassignedNodes = rSet.getInt(1);
				log.debug("The unassigned nodes are " + unassignedNodes );
			}
		}
		catch (Exception e)
		{
			log.error("Exception: ", e);
		}
		finally {
		      // finally clean up
		      try {
		        if (rSet != null) {
		        	rSet.close();
		        }
		        if (stmt != null) {
		          stmt.close();
		        }
		      } catch (SQLException e) {
		        log.error("SQLException: ", e);
		      }
		}
		return unassignedNodes;
	}

	public int getAssignedNodes (Connection dwhdb)
	{
		int assignedNodes = 0;
		Statement stmt = null;
		ResultSet rSet = null;
		try
		{
			stmt = dwhdb.createStatement();
			rSet = stmt.executeQuery("Select count(*) from ENIQS_Node_Assignment where ENIQ_IDENTIFIER is not null and ENIQ_IDENTIFIER != ''");
			while(rSet.next())
			{
				assignedNodes = rSet.getInt(1);
				log.debug("The assigned nodes are " + assignedNodes );
			}
		}
		catch (Exception e)
		{
			log.error("Exception: ", e);
		}
		finally {
		      // finally clean up
		      try {
		        if (rSet != null) {
		        	rSet.close();
		        }
		        if (stmt != null) {
		          stmt.close();
		        }
		      } catch (SQLException e) {
		        log.error("SQLException: ", e);
		      }
		}
		return assignedNodes;
	}
	}