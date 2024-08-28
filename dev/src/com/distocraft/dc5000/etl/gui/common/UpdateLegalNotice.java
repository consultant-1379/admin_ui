package com.distocraft.dc5000.etl.gui.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

public class UpdateLegalNotice extends EtlguiServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String MESSAGE_PROPERTIES_FILE = "/eniq/sw/runtime/tomcat/webapps/adminui/conf/message.properties";
	private  Properties props = new Properties();
	private static final String CUSTOM_MESSAGE = "CUSTOM_MESSAGE";
	private static final String CURRENT_MESSAGE = "currentMessage";
	private static final String AVOID_SPECIAL_CHARACTER = "avoidSpecialCharacter";
	private String defaultMessage;
	@SuppressWarnings("unused")
	private boolean result = false;

	@SuppressWarnings("deprecation")
	@Override
	
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception
			{
		 defaultMessage ="IF YOU ARE NOT AN AUTHORIZED USER STOP ANY ACTIVITY.YOU ARE PERFORMING ON THIS SYSTEM AND EXIT IMMEDIATELY.\n\n"+
				"This system is provided for authorized and official use only.\n"+
				"The usage of this system is monitored and audited.\n"+
				"Unauthorized or improper usage may result in disciplinary actions, civil or criminal penalties.\n";
		
		String legalNoticeMessage = request.getParameter("legalnoticemessage");
		try {
			props.load(new FileInputStream(MESSAGE_PROPERTIES_FILE));
			ctx.put(CURRENT_MESSAGE, props.getProperty(CUSTOM_MESSAGE,defaultMessage));
		} 
		 catch (Exception e) {
			ctx.put(CURRENT_MESSAGE, defaultMessage);
			ctx.put("propertiesFileNotFound", true);
			ctx.put(AVOID_SPECIAL_CHARACTER, false);
		 }
		if (legalNoticeMessage != null) {
			boolean isStringContainsSpecialCharacter = legalWarningMessageValidation("[!@#$%&*()_+=|<>?{}\\[\\]~-]", legalNoticeMessage);
			boolean isStringContainsUrl = legalWarningMessageValidation("((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\\\\\))+[\\\\w\\\\d:#@%/;$()~_?\\\\+-=\\\\\\\\\\\\.&]*)", legalNoticeMessage);
			if(isStringContainsSpecialCharacter || isStringContainsUrl) {
				ctx.put(AVOID_SPECIAL_CHARACTER, true);
	        } else {
	        	updateLegalNoticeMessage(request, legalNoticeMessage.trim(), true, ctx);
			}
		}
		
		return getTemplate("update_legalnotice.vm");
	}

	private void updateLegalNoticeMessage(HttpServletRequest request, String legalNoticeMessage, boolean value, Context ctx) throws IOException  {
		boolean flag = true;
		File messageProperties = new File(MESSAGE_PROPERTIES_FILE);
		messageProperties.createNewFile();
		OutputStream outputStream = new FileOutputStream(messageProperties,false);
		
		try{  
			props.setProperty(CUSTOM_MESSAGE, legalNoticeMessage);
		    props.store(outputStream, null);
		} catch (Exception e) {
			flag=false;
			 ctx.put(CURRENT_MESSAGE, defaultMessage);
		} 
		if (value) {
			ctx.put("legalNoticeUpdatedSuccessfully", flag);
			ctx.put(CURRENT_MESSAGE, props.getProperty(CUSTOM_MESSAGE,defaultMessage));
			ctx.put("propertiesFileNotFound", false);
			ctx.put(AVOID_SPECIAL_CHARACTER, false);
			this.result = flag;
		}
		
	}
	
	private boolean legalWarningMessageValidation(String pattern, String legalNoticeMessage) {
		return Pattern.compile(pattern).matcher(legalNoticeMessage).find();
	}
}