package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

public class UserDetails extends EtlguiServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Log log = LogFactory.getLog(UserDetails.class);

	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {

		final String page = "UserDetails.vm";
		ctx.put("userNames", passwordDetails());
		return getTemplate(page);
	}

	private List<MonitorInformation> passwordDetails(){
		final List<MonitorInformation> monitorInformations = new ArrayList<>();
		BufferedReader reader = null;
		Stream<String> lines = null ;
		try {
			reader = new BufferedReader(new FileReader("/eniq/sw/conf/queryUserConf.cfg"));
			lines = reader.lines();
			Iterator<String> itr = lines.iterator();
			itr.next();
			HashMap<String,Integer> users=new HashMap<>();
			while (itr.hasNext()) {
				int alreadyExist = 0;
				String line = itr.next().trim();
				if (!(line == null || (" ").equals(line) || ("").equals(line))) {
					MonitorInformation monitorInformation = new MonitorInformation();
					String[] details = line.split("::");
					String userName = details[0].trim();
					alreadyExist = checkUserAlreadyExists(users, userName);
					setMonitorInformation(monitorInformations,monitorInformation, alreadyExist,details, userName);
				}
			}
			log.debug("Customized Database users: "+users);
		} catch (FileNotFoundException e) {
			log.warn("Database User information file not available: " +e);
		} finally {
			try {
				if(lines != null)
					lines.close();
				if(reader != null)
					reader.close();
			} catch (IOException e) {
				log.warn("Unable to close the Buffered Reader: "+ e);
			}
		}
		return monitorInformations;
	}
	private int checkUserAlreadyExists(HashMap<String, Integer> users, String userName) {
		int alreadyExist = 0;
		if (users.get(userName) == null) {
			users.put(userName, 1);
		} else {
			alreadyExist = 1;
		}
		return alreadyExist;
	}

	private void setMonitorInformation(List<MonitorInformation> monitorInformations, MonitorInformation monitorInformation, int alreadyExist, String[] details, String userName){
		if (alreadyExist == 0) {
			String expiry = details[6].trim();
			if (("0").equalsIgnoreCase(expiry)) {
				monitorInformation.setUsername(userName);
				monitorInformation.setDaysToExpire(-1);
			} else {
				int year = Integer.parseInt(expiry.split("-")[0]);
				int month = Integer.parseInt(expiry.split("-")[1]);
				int days = Integer.parseInt(expiry.split("-")[2]);
				LocalDate actualExpiryDate = LocalDate.of(year, month, days).plusDays(7);
				LocalDate today = LocalDate.now();
				int daysToExpire = (int) Duration.between(today.atStartOfDay(), actualExpiryDate.atStartOfDay()).toDays();
				if (daysToExpire < 0) {
					daysToExpire = 0;
				}
				monitorInformation.setUsername(userName);
				monitorInformation.setDaysToExpire(daysToExpire);
			}
			monitorInformations.add(monitorInformation);
		}
	}
}


