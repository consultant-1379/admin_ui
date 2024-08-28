package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.rock.Meta_databases;
import com.ericsson.eniq.common.RemoteExecutor;
import com.ericsson.eniq.repository.DBUsersGet;

public class CertificateExpiryDetails extends EtlguiServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(CertificateExpiryDetails.class);
	
	@SuppressWarnings("deprecation")
	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
		final String page = "CertificateExpiryDetails.vm";
		ctx.put("certificateDetails", setCertificateInformation());
		ctx.put("otherCerfiticateDetails", setOtherCerfiticateDetails());
		return getTemplate(page);
	}
	
	private String fetchKeystorePassword() throws Exception{
		List<Meta_databases> mdList = DBUsersGet.getMetaDatabases(MonitorInformation.DC_USER_NAME , MonitorInformation.TITLE_ETLSCHEDULER);
		if (mdList.isEmpty()) {
			throw new Exception("Could not find an entry for " + MonitorInformation.DC_USER_NAME + ":"
					+ MonitorInformation.TITLE_ETLSCHEDULER + " in repdb!");
		}
		final String password = mdList.get(0).getPassword();
		String keyStorePassValue = RemoteExecutor.executeComand(MonitorInformation.DC_USER_NAME , password , MonitorInformation.TITLE_ETLSCHEDULER, CertificateInformation.KEYSTORE_PASSWORD);
		keyStorePassValue = keyStorePassValue.substring(keyStorePassValue.indexOf(':') + 1).trim();
		return keyStorePassValue;
	}
	
	private List<CertificateInformation> setPFCertificateInformation(
			List<CertificateInformation> certificateInformation, String keyStorePassValue, String keystoreFile,
			String purpose) {
		try(FileInputStream file = new FileInputStream(keystoreFile)) {
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(file, keyStorePassValue.toCharArray());
			Enumeration<String> aliases = keystore.aliases();
			while (aliases.hasMoreElements()) {
				CertificateInformation information = new CertificateInformation();
				String alias = aliases.nextElement();
				Date certExpiryDate = ((X509Certificate) keystore.getCertificate(alias)).getNotAfter();
				SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy");
				Date today = new Date();
				long dateDiff = certExpiryDate.getTime() - today.getTime();
				long expiresIn = dateDiff / (24 * 60 * 60 * 1000);
				information.setColour(getColorValue(expiresIn));
				information.setAlias(alias);
				information.setExpiryDate(ft.format(certExpiryDate));
				if (expiresIn < 0) {
					information.setExpiryDays("Expired");
				} else {
					information.setExpiryDays(Long.toString(expiresIn));
				}
				information.setPurpose(purpose);
				certificateInformation.add(information);
				log.debug("Certificate: " + alias + "\tExpires On: " + certExpiryDate + "\tFormated Date: "
						+ ft.format(certExpiryDate) + "\tToday's Date: " + ft.format(today) + "\tExpires In: "
						+ expiresIn);
			}
		} catch (Exception e) {
			log.warn("Exception while fetching certificate information from " + keystoreFile + " , Exception : " + e);
		}
		return certificateInformation;
	}
	
	public List<CertificateInformation> setCertificateInformation() throws Exception{
		List<CertificateInformation> certificateInformation = new ArrayList<>();
		String keyStorePassValue = fetchKeystorePassword();
		certificateInformation = setPFCertificateInformation(certificateInformation , keyStorePassValue , CertificateInformation.KEYSTORE , CertificateInformation.WEBSERVER);
		File f = new File(CertificateInformation.TRUSTSTORE);
		if(f.exists() && !f.isDirectory()) { 
			certificateInformation = setPFCertificateInformation(certificateInformation , keyStorePassValue , CertificateInformation.TRUSTSTORE , CertificateInformation.FLSorAlarms);
		}
		return certificateInformation;
	}

	protected Map<String, List<CertificateInformation>> setOtherCerfiticateDetails() {
		Map<String, List<CertificateInformation>> certificateDetails = new HashMap<>();
		String baseDirectoryPath = "/eniq/home/dcuser/windows_certificates/";
		File[] files = new File(baseDirectoryPath).listFiles();
		if(files == null) {
			return certificateDetails;
		}
		for (File file : files) {
			List<CertificateInformation> certificateInformation = new ArrayList<>();
			String directoryName = file.getName();
			File[] certificateFileNames = file.listFiles();
			if(certificateFileNames == null)
				continue;
			else if (certificateFileNames.length > 0) {
				String filePath = certificateFileNames[0].getAbsolutePath();
				log.debug("filePath: " + filePath);
				getCertificationDetails(filePath, certificateInformation);
				if (!certificateInformation.isEmpty())
					certificateDetails.put(directoryName, certificateInformation);
			}
		}
		return certificateDetails;
	}

	protected void getCertificationDetails(String filePath, List<CertificateInformation> certificateInformation) {
		try (FileReader file = new FileReader(filePath);
				BufferedReader reader = new BufferedReader(file);
				Stream<String> lines = reader.lines();) {
			lines.forEach(line -> {
				CertificateInformation information = new CertificateInformation();
				if (!(("").equals(line) || line.contains("Purpose"))) {
					String[] details = line.split("::");
					information.setAlias(details[1].trim());
					information.setPurpose(details[2].trim());
					information.setExpiryDate(details[3].trim());
					long expiryDays = Long.parseLong(details[4].trim());
					information.setExpiryDays(getExpiresIn(expiryDays, details));
					information.setColour(getColorValue(expiryDays));
					certificateInformation.add(information);
				}
			});
		} catch (FileNotFoundException e) {
			log.info("File Not found: " + e);
		} catch (IOException e1) {
			log.warn("Unable to close the File/Buffered Reader: " + e1);
		} catch (ArrayIndexOutOfBoundsException e2) {
			log.warn("Exception while reading file: " + filePath + e2);
			certificateInformation.clear();
		} catch (NumberFormatException e3) {
			log.warn("Exception while reading the expiry days from the file: " + filePath + e3);
			certificateInformation.clear();
		}
	}

	private String getColorValue(long expiryDays) {
		if (expiryDays <= 30) {
			return "Red";
		} else if (expiryDays > 30 && expiryDays <= 180) {
			return "Yellow";
		}
		return "Green";
	}

	private String getExpiresIn(long expiryDays, String[] details) {
		if (expiryDays < 0) {
			return "Expired";
		}
		return details[4].trim();
	}
}
