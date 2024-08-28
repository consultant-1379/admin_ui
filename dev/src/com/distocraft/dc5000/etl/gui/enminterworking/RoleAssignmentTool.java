package com.distocraft.dc5000.etl.gui.enminterworking;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.ericsson.eniq.enminterworking.rat.RoleAssignment;

import ssc.rockfactory.RockFactory;

/**
 * Copyright &copy; ERICSSON. All rights reserved.<br>
 * Servlet with Role Assingment Tool implementation. <br>
 *
 * @author Arjun Sinha [XARJSIN]
 **/

public class RoleAssignmentTool extends EtlguiServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Log log = LogFactory.getLog(this.getClass());
	private String role = null;
	private String roleNew = null;
	private List roleSnap = null;
	private String result = null;
	
	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		Template outty = null;
		Connection connDRep = ((RockFactory) ctx.get("rockDwhRep")).getConnection();
		connDRep.createStatement().execute("set temporary option blocking='on';set temporary option blocking_timeout=60000;");
	    HttpSession session = request.getSession(false);
	    String slaveIP = request.getParameter("slaveIP");
	    String getSlave = StringEscapeUtils.escapeHtml(request.getParameter("ipaddress"));
		
		if (getSlave != null){
			log.debug("IP Address entered - " + slaveIP);
			if(EnmInterUtils.validateIPAdd(slaveIP)){
				result = RoleAssignment.makeMasterSlave(slaveIP);
				ctx.put("RESULT", result);
			}
			else{
				log.warn("IP Address is invalid");
				ctx.put("invalidIP", true);
			}
		}
		
		try {
			role = EnmInterUtils.getSelfRole(connDRep);
			if(role.equalsIgnoreCase("MASTER")){
				roleNew="PRIMARY";
			}else {
				roleNew="SECONDARY";
			}
			log.debug("Present role of server is " + roleNew);
			if (role.equals("MASTER")){
				roleSnap = EnmInterUtils.getRoleTable(connDRep);
			}
			else if (role.equals("UNASSIGNED")){
				roleSnap = unassignedTable();
			}
			else {
				roleSnap = new Vector();
			}
			ctx.put("ROLE", role);
			ctx.put("roleTable", roleSnap);
		}
		catch(Exception se){
			log.error("Cannot retrieve data from RoleTable becase of exception:" + se);
			ctx.put("roleTable", new Vector());
		}
		
		if (role.equals("SLAVE")){
			ctx.put("ERROR", true);
			ctx.put("servername", EnmInterUtils.getFullyQualifiedHostname());
		}
		try {
			outty = getTemplate("role_assignment_tool.vm");
		}
		catch (ResourceNotFoundException e) {
			log.debug("ResourceNotFoundException (getTemplate):", e);
		}
		catch (ParseErrorException e){
			log.debug("ParseErrorException (getTemplate): " + e);
		}
		catch (Exception e) {
			log.debug("Exception (getTemplate): " + e);
		}
		return outty;
	}

	private List<String> unassignedTable() {
		List<String> unAssigned = new ArrayList<String>();
		unAssigned.add(EnmInterUtils.getEngineHostname());
		unAssigned.add(EnmInterUtils.getEngineIP());
		unAssigned.add("UNASSIGNED");
		return unAssigned;
	}

}