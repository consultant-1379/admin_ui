package com.distocraft.dc5000.etl.gui.enminterworking;

import java.rmi.Naming;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.distocraft.dc5000.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.ericsson.eniq.enminterworking.EnmInterCommonUtils;
import com.ericsson.eniq.enminterworking.IEnmInterworkingRMI;

import ssc.rockfactory.RockFactory;

/**
 * With this servlet user can edit one of the policies.
 * 
 * @author xdivykn
 */
public class PolicyReassignment extends EtlguiServlet {

	private static final long serialVersionUID = 1L;

	private final Log log = LogFactory.getLog(this.getClass());

	private String indexOfTable = null;

	private String[] policyRows;

	List<String> allRoles = new ArrayList<String>();

	List<String> allTechnologies = new ArrayList<String>();
	
	List<String> allENMHostnames = new ArrayList<String>();

	private String regexString;

	private String technology;

	private String identifier;
	
	private String enmHostname;

	private String oldTechnology;

	private String oldNaming;

	private String oldIdentifier;
	
	private String oldENMHostname;

	@SuppressWarnings("deprecation")
	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		
		Template outty = null;

		final String page = "policy_reassignment.vm";

		Connection dwhrep = ((RockFactory) ctx.get("rockDwhRep")).getConnection();
		dwhrep.createStatement().execute("set temporary option blocking='on';set temporary option blocking_timeout=60000;");
		PolicyAndCriteria pc = new PolicyAndCriteria();

		HttpSession session = request.getSession(false);

		if (request.getParameterMap().containsKey("action")
				&& request.getParameter("action").equalsIgnoreCase("save")) {

			technology = request.getParameter("technology");

			regexString = request.getParameter("inputstring");

			identifier = request.getParameter("identifier");
			
			enmHostname = request.getParameter("enmHostname");

			if (!regexString.isEmpty()) {
				// check if pattern is matching
				if (pc.isPatternMatching(regexString) || regexString.equals("*")) {
					log.debug("Pattern Matched. Checking for combination.");

					if (!pc.isCombinationExists(dwhrep, true, technology, regexString, identifier, enmHostname)) {
						log.info("Updating the policies with tech = " + technology + " regex = "
								+ regexString + " ENIQId = " + identifier +" enmHostname = " + enmHostname);
						pc.updatePolicy(dwhrep, technology, regexString, identifier, enmHostname,
								oldTechnology, oldNaming, oldIdentifier, oldENMHostname);
						IEnmInterworkingRMI multiEs = (IEnmInterworkingRMI) Naming
								.lookup(RmiUrlFactory.getInstance().getMultiESRmiUrl(EnmInterCommonUtils.getEngineIP()));
						multiEs.refreshNodeAssignmentCache();
					} else {
						log.warn("This Policy is Already defined!!");
						ctx.put("invalidCombination", true);
					}

				} else {
					log.warn("Invalid Naming Convention");
					ctx.put("invalidNaming", true);
				}
			}
			// now jump to "Policy Refinement Tool" servlet
			final PolicyRefinementTool prt = new PolicyRefinementTool();
			try {
				prt.init(this.getServletConfig());
				ctx.put("prt_saved", true);
				outty = prt.doHandleRequest(request, response, ctx);
				
			} catch (ServletException e) {
				log.warn("Exception: ", e);
			}
			finally{
				dwhrep.close();
			}
			return outty;
		} else {
			indexOfTable = request.getParameter("pc");

			allRoles = EnmInterUtils.getAllServers(dwhrep);
			allENMHostnames = EnmInterUtils.getAllENMHostnames();

			if (indexOfTable != null) {

				policyRows = pc.refinePolicy(indexOfTable, dwhrep);

				oldTechnology = policyRows[0];
				oldNaming = policyRows[1];
				oldIdentifier = policyRows[2];
				oldENMHostname = policyRows[3];				

				ctx.put("pc", indexOfTable);
				ctx.put("technology", oldTechnology);
				ctx.put("identifier", oldIdentifier);
				ctx.put("enmHostname", oldENMHostname);
				ctx.put("allPolicies", pc.getAllPolicies(dwhrep));
				ctx.put("allTechnologies", new PolicyRefinementTool().getTechnologies());
				ctx.put("allRoles", allRoles);
				ctx.put("allENMHostnames", allENMHostnames);
				ctx.put("inputstring", oldNaming);

			}
			try {

				outty = getTemplate(page);

			} catch (ResourceNotFoundException e) {

				log.debug("ResourceNotFoundException (getTemplate):", e);

			} catch (ParseErrorException e) {

				log.debug("ParseErrorException (getTemplate): " + e);
			} catch (Exception e) {

				log.debug("Exception (getTemplate): " + e);
			}
			finally{
				dwhrep.close();
			}
			return outty;
		}
	}
}