package com.distocraft.dc5000.etl.gui.enminterworking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.ericsson.eniq.enminterworking.EnmInterCommonUtils;
import com.ericsson.eniq.enminterworking.IEnmInterworkingRMI;

import ssc.rockfactory.RockFactory;

public class PolicyRefinementTool extends EtlguiServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Log log = LogFactory.getLog(this.getClass());

	private String Identifier;

	private String technology;

	private String regexString;
	
	private String enmHostname;

	Boolean validated = false;

	Boolean inserted = false;

	List<String> allRoles = new ArrayList<String>();

	List<String> allTechnologies = new ArrayList<String>();
	
	List<String> allENMHostnames = new ArrayList<String>();

	ArrayList<ArrayList<String>> policyTable = new ArrayList<ArrayList<String>>();

	private final String propertiesFile = System.getProperty("CONF_DIR", "/eniq/sw/conf") + "/NodeTechnologyMapping.properties";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.distocraft.dc5000.etl.gui.common.EtlguiServlet#doHandleRequest(javax.
	 * servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * org.apache.velocity.context.Context)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		
		Template outty = null;
		Connection dwhrep = ((RockFactory) ctx.get("rockDwhRep")).getConnection();
		dwhrep.createStatement().execute("set temporary option blocking='on';set temporary option blocking_timeout=60000;");
		HttpSession session = request.getSession(false);
		
		Identifier = request.getParameter("identifier");
		technology = request.getParameter("technology");
		enmHostname = request.getParameter("enmHostname");
		log.debug("Technology is " + technology);
		regexString = request.getParameter("inputstring");
		
		String indexOfTable = request.getParameter("deletePolicy");
		
		if(indexOfTable != null) {
			PolicyAndCriteria pc = new PolicyAndCriteria();
			String[] data = pc.refinePolicy(indexOfTable, dwhrep);
			log.info("Deleting policy: "+Arrays.toString(data));
			pc.deletePolicy(data, dwhrep);
			log.debug("Policy to be deleted: "+Arrays.toString(data));
		}
		
		if (ctx.containsKey("prt_saved")) {
			ctx.put("message", "Policy Updated Successfully");
			technology = "-";
		} else {
			PolicyAndCriteria pc = new PolicyAndCriteria();
			boolean serverNotSelected = Identifier == null || enmHostname == null;
			if (!serverNotSelected) {
				if(technology.equals("-")){
					technology = "*";
				}
				if (regexString.isEmpty()) {
					regexString = "*";
				}
				if(!pc.isCombinationExists(dwhrep, false, technology, regexString, Identifier, enmHostname)) {				
					
					//validate the given regular expression
					if (pc.isPatternMatching(regexString) || regexString.equals("*")) {
						inserted = pc.insertPolicy(dwhrep, technology, regexString, Identifier, enmHostname);
						if ( inserted ){
							IEnmInterworkingRMI multiEs = (IEnmInterworkingRMI) Naming
									.lookup(RmiUrlFactory.getInstance().getMultiESRmiUrl(EnmInterCommonUtils.getEngineIP()));
							multiEs.refreshNodeAssignmentCache();
						}
					} else {						

						log.debug("Invalid Naming Convention");
						ctx.put("invalidNaming", true);
					}
				} else {
					ctx.put("invalidcombination", true);
				}		
			}
		}

		allRoles = EnmInterUtils.getAllServers(dwhrep);
		
		policyTable = new PolicyAndCriteria().getAllPolicies(dwhrep);
		
		allTechnologies = getTechnologies();
		
		allENMHostnames = EnmInterUtils.getAllENMHostnames(); 
		
		ctx.put("policyTable", policyTable);
		ctx.put("technology", "-");
		ctx.put("allTechnologies", allTechnologies);
		ctx.put("allRoles", allRoles);
		ctx.put("allENMHostnames", allENMHostnames);
		ctx.put("inputstring", regexString);

		try {
			
			outty = getTemplate("policy_refinement_tool.vm");
			
		} catch (Exception e) {			
			log.warn("Exception in (getTemplate):", e);			
		}
		finally{
			dwhrep.close();
		}

		return outty;
	}

	// fetches all the technologies from the property file
	protected List<String> getTechnologies() {

		List<String> allTech = new ArrayList<String>();
		allTech.add("*");

		File propFile = new File(propertiesFile);
		//If File is not found exception is printed and * is returned as technology
		try(FileInputStream fstream = new FileInputStream(propFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));){
			String strLine = "";
			while ((strLine = br.readLine()) != null) {

				String[] line = strLine.split("-");
				allTech.add(line[0]);
			}				
		} catch(Exception e) {
			log.warn("Exception while reading properties file ", e);
		}
	
		return allTech;
	}
}