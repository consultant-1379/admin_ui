/**
 * Servlet to allow user to modify the poll delay time per interface.
 * For example if user changes the poll delay of Interface A from 0 to 5 then 
 * that interface adapter will run at 5, 20, 35 and 50th minute.
 * @author xankigu
 * @date 20120723
 */
package com.distocraft.dc5000.etl.gui.etl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import ssc.rockfactory.RockFactory;


import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.monitor.Util;
import com.distocraft.dc5000.etl.scheduler.ISchedulerRMI;
import com.distocraft.etl.gui.info.AdminuiInfo;

public class INTFPollingDelayConf extends EtlguiServlet {

	private static final long serialVersionUID = 1L;

	//private static final Log log = LogFactory.getLog(INTFPollingDelayConf.class.getClass());
	private final Log log = LogFactory.getLog(this.getClass());

	Map<Long, ArrayList<String>> mapOfInterfaceList;
	
	@Override
	public Template doHandleRequest(HttpServletRequest request,
			HttpServletResponse response, Context ctx) throws IOException,
			ServletException {
		Connection connERep = null;
		Template outty = null;
		final String templateName = "interfacepollingdelayConf.vm";
		inititialiseMembers(ctx);
               log.info(" Inside doHansdleRequest method of INTLPolling class");
               
               final HttpSession session = request.getSession();
               String username =  (String) session.getAttribute("username");
       		log.info("user in INTFPollingDelayConfig"+username);
       		
       		String pathInfo =request.getRequestURI();
       		log.info("path Info of INTFPollingDelayConfig="+pathInfo);
       	    String ipAddress = request.getRemoteAddr();
       	    log.info("IpAddress in INTFPollingDelayConfig="+ipAddress);

		try {
			connERep = ((RockFactory) ctx.get("rockEtlRep"))
					.getConnection();
			boolean isSavedSucc = false;
			
			final Map<String, String> params = new HashMap<String, String>();
			final Enumeration<String> parameters = request.getParameterNames();
			final String save = request.getParameter("save");
			Map<String, Integer> mapOfNameAndSchMin = new HashMap<String, Integer>();
			if (save != null) {
				log.info("Saving INTF POLL DELAY properties...");
				mapOfNameAndSchMin = Util
						.getActiveInterfaceAndSchMinMap(connERep);
				if (mapOfNameAndSchMin == null) {
					final String err = "Error comes while getting SCHEDULING_MIN values for each Inetrfaces.";
					log.error(err);
					ctx.put("errorMessage", err);
					isSavedSucc = false;
				} else {
					// get all parameters to map
					while (parameters.hasMoreElements()) {
						final String par = (String) parameters.nextElement();
						params.put(par, request.getParameter(par));
					}
					if (params.size() > 0) {
						log.debug("Parameters Map : " + params.toString());
						final Iterator<String> iter = params.keySet()
								.iterator();

						// Iterating over each key
						while (iter.hasNext()) {
							
							try {
								final String key = (String) iter.next();
								if (key.startsWith("intfName:")) {
									final String value = request.getParameter(
											key).toString();
									final int pollDelay = Integer
											.parseInt(value);
									final String effectedIntfName = key
											.substring(key.indexOf(":") + 1);
									int oldPollVal = mapOfNameAndSchMin.get(
											effectedIntfName).intValue();
									
									log.info("oldPollVal=" +oldPollVal);
									
									
								    log.debug("For Interface: "
											+ effectedIntfName
											+ " Old Poll Delay Value: "
											+ oldPollVal
										 	+ " New Changed Poll Delay Value: "
											+ pollDelay);
								    
								    
								
									log.info("Going to update POLL DELAY in database");
										if (!Util.setPollDelayForInterface(
												connERep, effectedIntfName,
												pollDelay)) {
												
											final String err = "Error while updating the saved value to database.";
											log.error(err);
											ctx.put("errorMessage", err);
											

										} 
										
										
										else {
											log.info("Successfully saved the changed values to database for interface: "
													+ effectedIntfName
													+ " with polldelay: "
													+ pollDelay);
											
											log.debug("No need to update, as already the same: ");
										}
										log.info("Poll Delay Vlaue=" +pollDelay);
										  AdminuiInfo.logINTFpoolingDelay(username,ipAddress, pathInfo,oldPollVal, pollDelay);
									  
								}
								
							} catch (final Exception e) {
								final String err = "Exception come while parsing the parameter: "
										+ e.getMessage();
								log.error(err);
								ctx.put("errorMessage", err);
								isSavedSucc = false;
							}
							
						}// while
						
					}// if
				}// else
				log.info("Saving completed. Now again get all values from database...");
				
				
				

					log.info("Saved the changes successfully.");
					final String succMess = "Saved Successfully.";
					ctx.put("savedSuccess", succMess);
					if (reloadScheduler()) {
						log.info("Scheduler reloaded successfully.");
					} else {
						log.error("Scheduler reload failed.");
					}
				


			}// if save != null
			log.info("Creating table with Interfcace Name and its poll delay values.");
			showInterfaceAndPollDelayTable(ctx, connERep);
		} catch (final Exception e) {
			ctx.put("errorMessage", e.getMessage());
		}
		try {
			outty = getTemplate(templateName);
		} catch (Exception e) {
			// throw new VelocityException(e);
			log.error("Error comes while returing template.", e);
		}finally {
			closeConn(connERep);
		}
		
		return outty;
	}
	
	private void closeConn(Connection connERep) {

		log.info("Closing Connection");
			try {
				if(connERep!=null) {
					connERep.close();
				}
				log.info("Connection closed successfully.");
			}catch(Exception e) {
				log.error("Error while closing connection.", e);
			}
			
		}

	/**
	 * Function to show Interface Name and its Current POLL DELAY value and also provide drop down menu to user to change it.
	 * @param context Current Velocity context
	 * @param conn Connection to ETLREP database
	 */
	private void showInterfaceAndPollDelayTable(final Context context,
			final Connection conn) {
		mapOfInterfaceList = Util.getActiveInterfaceIntrvalMap(conn);
		if (mapOfInterfaceList != null) {
			log.debug("Map content: " + mapOfInterfaceList.toString());
			// mapOfInterfaceList.get(0).get(0).indexOf(arg0)
		} else {
			log.error("Map mapOfInterfaceList is null.....");
		}
		context.put("mapOfInterfaceWithInterval", mapOfInterfaceList);
	}

	/**
	 * Function to reload scheduler
	 * @return True if scheduler reloaded successfully, false otherwise
	 */
	private boolean reloadScheduler() {
		boolean reloaded = false;
		// START SOME SET
		try {
			// create RMI object
			log.info("Reloading of scheduler starts.");
			final ISchedulerRMI trSch = (ISchedulerRMI) Naming
					.lookup(RmiUrlFactory.getInstance().getSchedulerRmiUrl());
			// reload properties
			trSch.reload();
			reloaded = true;
			log.debug("reloadScheduler executed successfully.");
		} catch (MalformedURLException murle) {
			log.error("MalformedURLException error:", murle);
		} catch (RemoteException re) {
			log.error("RemoteException error:", re);
		} catch (NotBoundException nbe) {
			log.error("NotBoundException error:", nbe);
		} catch (Exception e) {
			log.error("Exception caught: ", e);
		}
		return reloaded;
	}

	/**
	 * Function to initialize the common variables
	 * @param context Velocity context
	 */
	private void inititialiseMembers(final Context context) {
		context.put("errorMessage", "");
		context.put("savedSuccess", "");
	}
	
}//End of class
