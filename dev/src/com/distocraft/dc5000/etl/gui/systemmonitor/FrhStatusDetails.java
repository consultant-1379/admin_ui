package com.distocraft.dc5000.etl.gui.systemmonitor;

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
import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.ericsson.eniq.common.RemoteExecutor;
import com.ericsson.eniq.repository.DBUsersGet;

public class FrhStatusDetails extends EtlguiServlet {
	public static final String FRH_TITLE = "FRH";
	public static final String LICENSE_NOTINSTALLED = "License is not installed";
	public final static String FRHUSER = "dcuser";
	public final static String SERVICE_NAME = "frh";
	private Log log = LogFactory.getLog(this.getClass());

	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		Template page = null;
		final String usePage = "frh_status_details.vm";
		ctx.put("frhStatusDetails", getFrhStatusDetails());
		try {
			page = getTemplate(usePage);
		} catch (ResourceNotFoundException e) {
			log.error("ResourceNotException at doHandleRequest in FrhStatusDetails", e);
		} catch (ParseErrorException e) {
			log.error("ParseErrorException at doHandleRequest in FrhStatusDetails", e);
		} catch (Exception e) {
			log.error("Exception at doHandleRequest in FrhStatusDetails", e);
		}

		return page;
	}

	private MonitorInformation getFrhStatusDetails() {
		LoaderStatus loader = new LoaderStatus();
		MonitorInformation monitorInformation = loader.getStatusMonitor("frh", "Frh Show Details", null);
/*
		if (monitorInformation.isGray()) {
			monitorInformation.setStatus(monitorInformation.BULB_GRAY);
			monitorInformation.setMessage(LICENSE_NOTINSTALLED);
		} else {
			String frhPassword = null;
			try {
				List<Meta_databases> mdList = DBUsersGet.getMetaDatabases(FRHUSER, SERVICE_NAME);
				if (mdList.isEmpty()) {
					throw new Exception("Could not find an entry for " + FRHUSER + ":" + SERVICE_NAME + " in FRH!");
				}
				frhPassword = mdList.get(0).getPassword();
				if (frhPassword != null) {
					String frhServerIp = loader.getFrhServerIP();
					
					final String frhSupervisorStatuscommand = "/ericsson/frh/controller/bin/frh-supervisor status";
					String frhSupervisorMessage = RemoteExecutor.executeComand(FRHUSER, frhPassword, frhServerIp,
							frhSupervisorStatuscommand);
					monitorInformation.setMessage(frhSupervisorMessage + "<br />");
					
					final String frhControllerStatuscommand = "/ericsson/frh/controller/bin/frh_controller.sh -a status";
					String frhControllerMessage = RemoteExecutor.executeComand(FRHUSER, frhPassword, frhServerIp,
							frhControllerStatuscommand);
					monitorInformation.setMessage(frhControllerMessage + "<br />");
					
					final String EPSStatuscommand = "/ericsson/frh/flow/bin/eps.sh -a statusall";
					String EPSMessage = RemoteExecutor.executeComand(FRHUSER, frhPassword, frhServerIp,
							EPSStatuscommand);
					String splitEPSMessage[] = EPSMessage.split("(?=EPS)");
					for (String s : splitEPSMessage) {
						monitorInformation.setMessage(s + "<br />");
					}
					monitorInformation.setFieldName(FRH_TITLE);
				}
			} catch (Exception e) {
				monitorInformation.setStatus(MonitorInformation.BULB_RED);
				monitorInformation.setFieldName(FRH_TITLE);
				log.error("Exception at getFrhStatusDetails in FrhStatusDetails", e);
			}
		}
		*/
		return monitorInformation;
	}
}