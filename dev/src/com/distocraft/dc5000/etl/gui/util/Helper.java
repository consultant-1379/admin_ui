package com.distocraft.dc5000.etl.gui.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * 
 * Helper class is for storing constants, and to recieve env entrys, and so on...
 * @author Jani Vesterinen
 */
public class Helper {
  
  private Helper() {
    // do not allow initialisation
  }

  private static Log log = LogFactory.getLog("com.distocraft.dc5000.etl.gui.util.Helper");
  
  public static final String PARAM_TECHPACKNAME = "techpak";
  public static final String PARAM_SET_NAME = "setname";
  public static final String PARAM_TYPE = "type";
  public static final String PARAM_START_TIME = "starttime";
  public static final String PARAM_END_TIME = "endtime";
  public static final String DATE_URL_FORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final String DATE_LOG_FORMAT = "yyyy.dd.MM HH:mm:ss";
  public static final String PARAM_PAGE_FROM = "pagefrom";
  public static final String TITLE_PAGE_HISTORY = "History Browsing";
  public static final String TITLE_PAGE_MONITORING = "Realtime Monitoring";
  public static final String PARAM_PAGE_MONITORING = "monitoring";
  public static final String PARAM_PAGE_HISTORY = "historybrowsing";
  public static final String PARAM_SELECTED_TYPE = "selectedtype";
  public static final String PARAM_SEARCH_STRING = "searchstring";
  public static final String PARAM_SELECTED_TABLE = "selectedtable";
  public static final String PARAM_SELECTED_TECHPACK = "selectedpack";
  public static final String PARAM_SELECTED_SET_TYPE = "selectedsettype";
  public static final String PARAM_PAGE_SESLOG = "sessionlog";
  public static final String TITLE_PAGE_SESSION = "Session Log";
  public static final String MZ_PROCESSING = "PROCESSING";
  public static final String MZ_PREPROCESSING = "PREPROCESSING";
  public static final String SDFTIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final String PATHSEPERATOR = "/";
  public static final String REALPATH_CONF = "";
  public static String adminui4 = null;

  private static Context context;
  
  public static Context getContext() {
    return context;
  }

  public static void setContext(final Context context) {
    Helper.context = context;
  }

  /**
   * gets env entry String value from DD
   * @param entry
   * @return
   */
  public static synchronized String getEnvEntryString(final String entry){
    String value = "";
    
    try {
      if (context == null) {
        context = new InitialContext();
      }
      final Context envCtx = (Context) context.lookup("java:comp/env");
      value = (String) envCtx.lookup(entry);
    } catch (NamingException e) {
      log.error("NamingException",e);
    }

    return value;
  }

  /**
   * gets env entry int value from DD
   * @param entry
   * @return
   */
  public static synchronized int getEnvEntryInt(final String entry){
    int value = 0;
    
    try {
      if (context == null) {
        context = new InitialContext();
      }
      final Context envCtx = (Context)context.lookup("java:comp/env");
      value = Integer.parseInt((String) envCtx.lookup(entry));
    } catch (NamingException e) {
      log.error("NamingException",e);
    }

    return value;
  }

  public static boolean isNotEmpty(final String s){
    return s != null && !s.trim().equals("");
  }
  
 /*
  public static String getAdminui4(){
    if (adminui4 == null)
      adminui4  = Helper.getEnvEntryString("adminui4").equals("true") ? "true" : "false";
    return adminui4;
  }
 */
}
