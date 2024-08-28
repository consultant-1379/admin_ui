package com.distocraft.dc5000.etl.gui.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.UserVO;

public class UnusedUserExport {

	private static final String DELIM = "\t";
	private static final String EOL = "\n";
	private static final String HEADER_LINE = "UserId\tFirst Name\tLast Name\tEmail\tPhone\tOrg\tStatus\tPredefined\tRoles\tDays Since Last Login";
	private static final String DEFAULT_EXPORT_PATH = "/eniq/home/dcuser/unused_users.tab";
	private String exportPath = getExportPath();
	private final Log log = LogFactory.getLog(this.getClass());

	public String getExportPath() {
		if (exportPath == null) {
			exportPath = PropertyLoader.getProperty("user_management.unused_users_export_path", 
		   			DEFAULT_EXPORT_PATH);
		}
		return exportPath;
	}
	
	public void setExportPath(final String exportPath) {
		this.exportPath = exportPath;
	}

	public String export(final List<IValueObject> testUserList, final long lastLoginThreshold) {

		String message = "";
		boolean createdNewExportFile = false;
		boolean exportFileWriteable = true;
		FileWriter fw = null;
		BufferedWriter bw = null;
		int exportCount = 0;
		
		// ensure export path is defined
		getExportPath();
		log.info("Exporting to file: " + exportPath);
		
		// delete existing file (if exists) and create new file
		final File exportFile = new File(exportPath);
		
		if (exportFile.exists()) 
		{
			if (exportFile.canWrite()) {
				exportFile.delete();
			} else {
				exportFileWriteable = false;
				message = "Export aborted! Export file: " + exportPath + " is read only.";
			}
		}

		if (exportFileWriteable) {
			try {
				exportFile.createNewFile();
				createdNewExportFile=true;
			} catch (IOException e) {
				log.error("Cannot create new export file: " + exportPath + "\n" + e.getMessage());
				message = "Cannot create new export file: " + exportPath + "\nSee logs for details.";
			}
		}
			
		// if new export file was successfully created, go ahead with the export
		if (createdNewExportFile) {
			try {
				fw = new FileWriter(exportFile);
				bw = new BufferedWriter(fw);
				bw.write(HEADER_LINE + EOL);
				
				// export all relevant users
				for (IValueObject user : testUserList) {
					final UserVO userVo = (UserVO)user;
					int daysSinceLastLogin = 0;
				    if (!userVo.getDaysSinceLastLogin().equals("-")) {
				    	daysSinceLastLogin = Integer.parseInt(userVo.getDaysSinceLastLogin());
				    }
				    
				    // export user if last login beyond threshold 
					if (daysSinceLastLogin >= lastLoginThreshold) {
						bw.write(buildExportRow(userVo));
						exportCount++;
					}
				}

				bw.flush();
				message = "Exported " + exportCount + " user(s) to file: " + exportPath;
				log.info(message);

			} catch (IOException e) {
				log.error("Problem exporting unused users to file: " + exportPath + "\n" + e.getMessage());
				message = "Problem exporting unused users to file: " + exportPath + "\nSee logs for details.";
			} finally {
				try {
					bw.close();
					fw.close();
				} catch (IOException e) {
					log.error("Problem closing file: " + exportPath + "\n" + e.getMessage());
					message = "Problem closing file: " + exportPath + "\nSee logs for details.";
				}
			}
		}
		
		return message;
	}
	
	private static String buildExportRow(final UserVO user) {
		final StringBuilder sb = new StringBuilder();

		sb.append(user.getUserId()).append(DELIM);
		sb.append(user.getFname()).append(DELIM);
		sb.append(user.getLname()).append(DELIM);
		sb.append(user.getEmail()).append(DELIM);
		sb.append(user.getPhone()).append(DELIM);
		sb.append(user.getOrg()).append(DELIM);
		sb.append(user.getUserState().getMessage()).append(DELIM);
		sb.append(user.isPredefined()).append(DELIM);
		sb.append(user.getRoles().toString()).append(DELIM);
		sb.append(user.getDaysSinceLastLogin());
		sb.append(EOL);
		
		return sb.toString();
	}
	
}
