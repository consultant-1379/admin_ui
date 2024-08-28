package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.util.Date;

public class CertificateInformation {
	
	private String alias = "";
	
	private String purpose = "";
	
	private String expiryDate = "";
	
	private String expiryDays;
	
	private String colour = "";
	
	public static final String WEBSERVER = "AdminUI/AlarmUI";
	
	public static final String FLSorAlarms = "FLS/Alarms";
	
	public static final String KEYSTORE = "/eniq/sw/runtime/tomcat/ssl/keystore.jks";
			
	public static final String TRUSTSTORE = "/eniq/sw/runtime/jdk/jre/lib/security/truststore.ts";
	
    public static final String KEYSTORE_PASSWORD = "/eniq/sw/installer/getPassword.bsh -u keystore";

    public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getExpiryDays() {
		return expiryDays;
	}

	public void setExpiryDays(String expiryDays) {
		this.expiryDays = expiryDays;
	}

	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

}