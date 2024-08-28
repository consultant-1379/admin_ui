package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ericsson.eniq.common.Constants;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Class is for maintaining monitor information. (Value class)
 * 
 * @author Jani Vesterinen
 */

public class MonitorInformation {

	private static final Log log = LogFactory.getLog(MonitorInformation.class);
	
	public String userName= "";
	
	public LocalDate actualExpiryDate;

	public int daysToExpire= 0;

	private String fieldName = "";
	
	private String passExpiryDate = "";

  private final StringBuffer message = new StringBuffer();

  private String detailUrl = "";

  private String uptime = "";

  private String queueStatus = "";

  private String size = "";

  private String pollperiod = "";

  private String currProfile = "";

  private String currProfileName = "";

  private String slots = "";

  private String avProcessors = "";

  private String freeMem = "";

  private String totMem = "";

  private String pollinterval = "";

  private String hostname = "";

  private String hwversion = "";

  private String osversion = "";

  private List<Map<String,String>> listOfServiceswithStatus = new ArrayList<Map<String,String>>();

  private String glassfishServerStatusMsg = "";

  private final List<String> mediationGatewayStatusMessage = new ArrayList<String>();

  private final List<String> glassfishStatusMessage = new ArrayList<String>();
  
  private final List<String> mediatorStatusMessage = new ArrayList<String>();

  private String licManagerStatusMsg = "";

  private String licServerStatusMsg = "";
  
  private String counterVolume = "0";

private int status = -1;

  private boolean warning = false;

  private String warningText = "";

  public static final int BULB_RED = -1;

  public static final int BULB_YELLOW = 0;

  public static final int BULB_GREEN = 1;
  
  public static final int BULB_GRAY = 2;

  public static final String GLASSFISH_STATUS_FIELD_NAME = "Status";

  public static final String GLASSFISH_STATUS_COMMAND = "glassfishcommand";

  public static final String MEDIATOR_STATUS_FIELD_NAME = "Status";

  public static final String MEDIATOR_STATUS_COMMAND = "/eniq/admin/bin/mediator status";
  
  public static final String DWHDB_STATUS_COMMAND = "/eniq/sw/bin/dwhdb status";
  
  public static final  String CONTROL_ZONE_NAME="controlzone";
  
  public static final  String EC="ec_";
  
  public static final  String Engine="engine";
  
  public static final  String Dwh_reader="dwh_reader_";
  
  public static final String CMD_PING6_STATUS = "/usr/sbin/ping6";
  
  public static final  String Active_dwh="active";
  
  public static final  String Glassfish="glassfish";
  
  public static final String MEDIATOR_HOST_ID = "mediator";
  
  public static final String STATUS_RUNNING = "Status: Running";

  public static final String GLASSFISH_STATUS_CHECK = "domain1 running";
  
  public static final String STATUS_NOT_RUNNING = "Status: Not running";

  public static final String STATUS_COMMAND_ERROR = "Status: could not execute command";

  public static final String MZ_MEDIATION_STATUS_COMMAND = "mzcommand";

  public static final String MZ_STATUS_FIELD_NAME = "Status";

  public static final String MZ_HOST_ID = "controlzone";

  public static final String GLASSFISH_HOST_ID = "glassfish";

  public static final String GF_COMMAND_LIST = "Command list-domains executed successfully.";

  public static final String MZ_STATUS_RUNNING = "running";

  public static final String GF_STATUS_RUNNING = "Running";
  
  public static final String DWHDB_STATUS_RUNNING = "running OK";

  public static final String MZ_PLATFORM_PORT = "mzplatformport";

  public static final String MZ_EC_PORT = "mzecport";

  public static final String ERICSSON_URL = ".athtem.eei.ericsson.se:";

  public static final String MZ_STATUS_RUNNING_KEY = "is running";

  public static final String MZ_STATUS_NOT_RUNNING = "Mediaton Gateway is not running";

  public static final String MZ_EC1_RUNNING = "EC1 is running";

  public static final String MZ_PLATFORM_RUNNING = "Platform is running";

  public static final String STATUS_ENGINE_STRING_OK = "Status: active";

  public static final String STATUS_SCHEDULER_STRING_OK = "Status: active";

  public static final String READER_STATUS_CONTEXT = "iqreader";

  public static final String TITLE_ETLENGINE = "Engine";
  
  public static final String TITLE_LWPHELPER = "LwpHelper";
  
  public static final String TITLE_FLS = "Fls";

  public static final String TITLE_ETLSCHEDULER = "Scheduler";

  public static final String TITLE_LICSERV = "License server";

  public static final String TITLE_LICMGR = "License manager";

  public static final String TITLE_DATAWARE = "PM Datawarehouse";

  public static final String TITLE_HOST = "Host Information";

  public static final String EXECUTION_CONTEXT_HREF = "EC";

  public static final String OS_VERSION = System.getProperty("os.name");

  public static final String CMD_HOSTNAME = "hostname";

  public static final String CMD_HOSTNAME_WIN = "cmd.exe /c Systeminfo | Find \"Host Name\"";

  public static final String CMD_OS_VERSION_S = "uname -s";

  public static final String CMD_OS_VERSION_R = "cat /etc/redhat-release | awk '{print $7}'"; 
  

  public static final String CMD_OS_VERSION_WIN = "cmd.exe /c Systeminfo | Find \"OS Version\"";

  public static final String CMD_OS_NAME = "cmd.exe /c Systeminfo | Find \"OS Name\"";

  public static final String CMD_SO_HW_VERSION = "uname -i";

  public static final String CMD_LICSERV_STATUS = "/eniq/sw/bin/licmgr -serverstatus";

  public static final String CMD_LICMGR_STATUS = "/eniq/sw/bin/licmgr -status";
  
  public static final String CMD_PING_STATUS = "/usr/sbin/ping";
  
  public static final String CMD_PING_STATUS_RESULT_VALID = "is alive" ;
  
  public static final String CMD_DB_STATUS_RESULT_VALID = "is running OK";
  
  public static final String STATS_STARTER_LICENSE = Constants.ENIQ_STARTER_LICENSE;

  public static final String STATS_CMD_STARTER_LICENSE_STATUS = "/eniq/sw/bin/licmgr -isvalid " + STATS_STARTER_LICENSE;

  public static final String STATS_CMD_STARTER_LICENSE_VALID = "License for feature " + STATS_STARTER_LICENSE +" is valid";

  // Following two constanst are only here until LoaderStatus servlet has been merged
  public static final String CMD_STARTER_LICENSE_STATUS = STATS_CMD_STARTER_LICENSE_STATUS;

  public static final String CMD_STARTER_LICENSE_VALID = STATS_CMD_STARTER_LICENSE_VALID;

  public static final String STATS_STARTER_LICENSE_EXPIRED = "<FONT COLOR=\"#FF0000\">ENIQ Starter license <b>" + STATS_STARTER_LICENSE + "</b> has expired.</FONT></br>";

  public static final String EVENTS_CMD_STARTER_LICENSE_STATUS = "/eniq/sw/bin/licmgr -isvalid CXC4012080";

  public static final String EVENTS_CMD_STARTER_LICENSE_VALID = "License for feature CXC4012080 is valid";

  public static final String EVENTS_STARTER_LICENSE_EXPIRED = "<FONT COLOR=\"#FF0000\">ENIQ Starter license <b>CXC4012080</b> has expired.</FONT></br>";

  public static final String CMD_HP_HW_VERSION = "model";

  public static final String CMD_WIN_HW_VERSION = "cmd.exe /c Systeminfo | Find \"System Model\"";

  public static final String CMD_UPTIME = "uptime";

  public static final String CMD_WIN_UPTIME = "cmd.exe /c Systeminfo | Find \"Up Time\"";

  public static final String IQ_NAME_STATUS_DWH = "rockDwhDba";

  public static final String IQ_NAME_STATUS_REP = "rockEtlRepDba";

  public static final String IQ_NAME_STATUS_READER = "rockDwhDbaReader";
  
  public static final String dwhDBServiceName = "dwhdb";
  
  public static final String repDBServiceName = "repdb";
  
  public static final String SERVICE_OK = "OK";
  
  public static final String SERVICE_NOT_OK = "NOT OK";

  public static final String CMD_HOST = "host_path_mz_ec";

  static final String DC_USER_NAME = "dcuser";
  
  static final String DC_USER_CONNECTION_NAME = "dcuser";
  
  public static String SERVER_TYPE = "cat /eniq/installation/config/SunOS.ini |grep -i Storage_TYPE";
  
  public static final String TITLE_ROLLING_SNAPSHOT = "Rolling Snapshot";
  
  public static final String TITLE_OMBS_BACKUP = "OMBS Backup";
  
  public  StringBuffer Alarm_Message = new StringBuffer();
  
  private boolean details = false;
  
  public static String genTypeCommand="sudo dmidecode -s system-product-name";

  //Related to FRH
/*  public static final String CONTROLLER_NOT_RUNNING = "<FONT COLOR=\"#FF0000\"><b>Controller is not running</b></FONT>";

  public static final String LICENSE_NOT_INSTALLED = "<FONT COLOR=\"#FF0000\"><b>License is not installed</b></FONT>";
*/
  
  /**
   * Sets message. One is able to set message.
   * 
   * @param s
   */
  public void appendToStatusMessage(final String s) {
    message.append(s);
    message.append("<br />");
  }

  /**
   * Returns message.
   * 
   * @return
   */
  public String getStatusMessage() {
    return message.toString();
  }

  
  public void clearAlarmStatus(){
	  Alarm_Message.setLength(0);
  }
  public String getAlarmStatus(){
	  return Alarm_Message.toString();
  }
  
  public void setAlarmStatus(String msg){
	  Alarm_Message.append(msg);
  }
  
  public String getGlassfishServerStatusMsg() {
    return glassfishServerStatusMsg;
  }

  public void setGlassfishServerStatusMsg(final String glassfishServerStatusMsg) {
    this.glassfishServerStatusMsg = glassfishServerStatusMsg;
  }

  
	public LocalDate getActualExpiryDate() {
		return actualExpiryDate;
	}

	public void setActualExpiryDate(LocalDate actualExpiryDate) {
		this.actualExpiryDate = actualExpiryDate;
	}

  /**
   * Receives url parameter value.
   * 
   * @return url that is used for receiving detailed information.
   */
  public String getDetailUrl() {
    return detailUrl;
  }

  /**
   * Sets detail url value
   * 
   * @param detailUrl
   */
  public void setDetailUrl(final String detailUrl) {
    this.detailUrl = detailUrl;
  }
  	
  
  public String getPassExpiryDate() {
	  return passExpiryDate;
  }

  public void setPassExpiryDate(String passExpiryDate) {
	  this.passExpiryDate = passExpiryDate;
  }
  /**
   * Receives field name for the ui to show.
   * 
   * @return
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Sets fieldname
   * 
   * @param fieldName
   */
  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * Gets status for connection or ping. Status values are <br />
   * BULB_RED = -1; BULB_YELLOW = 0; BULB_GREEN = 1;
   * 
   * @return
   */
  public int getStatus() {
    return status;
  }

  /**
   * Is red status of the connection
   */
  public Boolean isRed() {
    return status == -1;
  }

  /**
   * Is yellow status of the connection
   */
  public Boolean isYellow() {
    return status == 0;
  }

  /**
   * Is green status of the connection
   */
  public Boolean isGreen() {
    return status == 1;
  }
  
  /**
   * Is orange status of the connection
   */
  public Boolean isGray() {
    return status == 2;
  }

  /**
   * Sets status for connection or ping. Status values are <br />
   * BULB_RED = -1; BULB_YELLOW = 0; BULB_GREEN = 1;
   * 
   * @param status
   */
  public void setStatus(final int stat) {
    status = stat;
  }

  /**
   * Sets message. One is able to set appended text for message.
   * 
   * @param s
   */
  public void setMessage(final String s) {
	message.append("- ");
    message.append(s);
    message.append("<br />");
  }

  /**
   * Returns message.
   * 
   * @return
   */
  public String getMessage() {
    return message.toString();
  }

  /**
   * Checks if warning exists
   * 
   * @return
   */
  public boolean isWarning() {
    return warning;
  }

  /**
   * Sets warning value and warning text
   * 
   * @param warning
   * @param text
   */
  public void setWarning(final boolean warning, final String text) {
    this.warning = warning;
    setWarningText(text);
  }

  /**
   * Gets available processors
   * 
   * @return
   */
  public String getAvProcessors() {
    return avProcessors;
  }

  /**
   * Sets available processors
   * 
   * @param avProcessors
   */
  public void setAvProcessors(final String avProcessors) {
    this.avProcessors = avProcessors;
  }

  /**
   * Gets current profile
   * 
   * @return
   */
  public String getCurrProfile() {
    return currProfile;
  }

  /**
   * Sets current profile
   * 
   * @param currProfile
   */
  public void setCurrProfile(final String currProfile) {
    this.currProfile = currProfile;
    setCurrProfileName(currProfile);
  }

  /**
   * Gets current profile name
   * 
   * @return
   */
  public String getCurrProfileName() {
    return currProfileName;
  }

  /**
   * Sets current profile name
   * 
   * @param currProfile
   */
  public void setCurrProfileName(final String currProfile) {
    try {
      final String[] splitted = currProfile.split(":");
      this.currProfileName = splitted[1].trim();
    } catch (final Exception e) {
      this.currProfileName = "";
    }
  }

  /**
   * Gets free memory in megabytes as String value.
   * 
   * @return
   */
  public String getFreeMem() {
    return freeMem;
  }

  /**
   * Sets free memory.
   * 
   * @param freeMem
   */
  public void setFreeMem(final String freeMem) {
    this.freeMem = freeMem;
  }

  /**
   * Gets poll period.
   * 
   * @return
   */
  public String getPollperiod() {
    return pollperiod;
  }

  /**
   * Sets poll period.
   * 
   * @param pollperiod
   */
  public void setPollperiod(final String pollperiod) {
    this.pollperiod = pollperiod;
  }

  /**
   * Gets queue status.
   * 
   * @return
   */
  public String getQueueStatus() {
    return queueStatus;
  }

  /**
   * Sets queue status.
   * 
   * @param queueStatus
   */
  public void setQueueStatus(final String queueStatus) {
    this.queueStatus = queueStatus;
  }

  /**
   * Gets priority queue size.
   * 
   * @return size
   */
  public String getSize() {
    return size;
  }

  /**
   * Sets priority queue size.
   * 
   * @param size
   */
  public void setSize(final String size) {
    this.size = size;
  }

  /**
   * Gets slots.
   * 
   * @return
   */
  public String getSlots() {
    return slots;
  }

  /**
   * Sets slots.
   * 
   * @param slots
   */
  public void setSlots(final String slots) {
    this.slots = slots;
  }

  /**
   * Gets total amount of memory in megabytes.
   * 
   * @return
   */
  public String getTotMem() {
    return totMem;
  }

  /**
   * Sets total memory in megabytes.
   * 
   * @param totMem
   */
  public void setTotMem(final String totMem) {
    this.totMem = totMem;
  }

  /**
   * Gets uptime.
   * 
   * @return
   */
  public String getUptime() {
    return uptime;
  }

  /**
   * Sets uptime.
   * 
   * @param uptime
   */
  public void setUptime(final String uptime) {
    this.uptime = uptime;
  }

  /**
   * Sets poll interval.
   * 
   * @param string
   */
  public void setPollInterval(final String string) {
    pollinterval = string;
  }

  /**
   * Gets poll interval.
   * 
   * @return
   */
  public String getPollInterval() {
    return pollinterval;
  }

	public int getDaysToExpire() {
		return daysToExpire;
	}

	public void setDaysToExpire(int daysToExpire) {
		this.daysToExpire = daysToExpire;
	}
	
  public String getHostname() {
    return hostname;
  }
  public String getUsername() {
	    return userName;
	  }

  /**
   * Sets server host name
   * 
   * @param hostname
   */
  public void setHostname(final String hostname) {
    this.hostname = hostname;
  }

  
  public void setUsername( String userName) {
	    this.userName = userName;
	  }
  /**
   * Gets hardware version.
   * 
   * @return
   */
  public String getHwversion() {
    return hwversion;
  }

  /**
   * Sets hardware version.
   * 
   * @param hwversion
   */
  public void setHwversion(final String hwversion) {
    this.hwversion = hwversion;
  }

  /**
   * Gets host operating system version.
   * 
   * @return
   */
  public String getOsversion() {
    return osversion;
  }

  /**
   * Sets host operating system version.
   * 
   * @param osversion
   */
  public void setOsversion(final String osversion) {
    this.osversion = osversion;
  }
  
  /**
   * Gets service Map values.
   * 
   * @return
   */
  public List<Map<String,String>> getServiceStatusList() {
	  log.debug("getServiceStatusList:: List: " + listOfServiceswithStatus.toString());
	  return listOfServiceswithStatus ;
  }

  /**
   * Sets service Map values.
   * 
   * @param serviceStatusMap
   */
  public void setServiceStatusList(final List<Map<String,String>> list) {
	  for(Map<String,String> p : list){
		  listOfServiceswithStatus.add(p);
	  }
	  log.debug("setServiceStatusListForHost:: List: " + listOfServiceswithStatus.toString());
  }

  /**
   * Sets flag if details exists.
   * 
   * @param b
   */
  public void setIsDetails(final boolean b) {
    details = b;
  }

  /**
   * Checks if details exists.
   * 
   * @return
   */
  public boolean isDetails() {
    return details;
  }

  /**
   * Transforms String that is in format XXXX:12345 to megabytes
   * 
   * @param number
   * @return string
   */
  public static synchronized String transformBytesToMegas(final String number) {
    // Total Memory: 17874944
    final StringTokenizer st = new StringTokenizer(number, ":");
    st.nextToken();

    final float numba = Float.parseFloat(st.nextToken().trim());

    return String.valueOf(((int) ((numba / 1024) / 1024)));
  }

  /**
   * Gets warning text
   * 
   * @return
   */
  public String getWarningText() {
    return warningText;
  }

  /**
   * Sets warning text. After text html breakline is added, so it is possible to add one or more messages bound to db
   * info.
   * 
   * @param warningText
   */
  public void setWarningText(final String warningText) {
    this.warningText += warningText + "<br />";
  }

  public void clearWarnings() {
    this.warningText = "";

  }

  public String getLicManagerStatusMsg() {
    return licManagerStatusMsg;
  }

  public void setLicManagerStatusMsg(final String licManagerStatusMsg) {
    this.licManagerStatusMsg = licManagerStatusMsg;
  }

  public String getLicServerStatusMsg() {
    return licServerStatusMsg;
  }

  public void setLicServerStatusMsg(final String licServerStatusMsg) {
    this.licServerStatusMsg = licServerStatusMsg;
  }

  public List<String> getMediationGatewayStatusMessage() {
    return mediationGatewayStatusMessage;
  }

  public void appendToMediationGatewayStatusMessage(final String message) {
    this.mediationGatewayStatusMessage.add(message);
  }

  public void appendToglassfishStatusMessage(final String message) {
    this.glassfishStatusMessage.add(message);
  }

  public List<String> getglassfishStatusStatusMessage() {
    return glassfishStatusMessage;
  }

  public List<String> getmediatorStatusStatusMessage() {
	    return mediatorStatusMessage;
	  }
	  
  public void appendToMediatorStatusMessage(final String message) {
	    this.mediatorStatusMessage.add(message);
	  }
	  
  private String etlcServerStatus = "";

  public static final String ENGINE_NOT_RUNNING = "<FONT COLOR=\"#FF0000\"><b>Engine is not running</b></FONT>";

  public static final String SCHEDULER_NOT_RUNNING = "<FONT COLOR=\"#FF0000\"><b>Scheduler is not running</b></FONT>";
  
  public static final String FLS_NOT_RUNNING = "<FONT COLOR=\"#FF0000\"><b>FLS is not running</b></FONT>";
  
  /**
   * @return the etlcServerStatus
   */
  public String getEtlcServerStatus() {
    return etlcServerStatus;
  }

  /**
   * @param etlcServerStatus
   *          the etlcServerStatus to set
   */
  public void setEtlcServerStatus(final String etlcServerStatus) {
    this.etlcServerStatus = etlcServerStatus;
  }
  public String getCounterVolume() {
	return counterVolume;
}

public void setCounterVolume(String counterVolume) {
	this.counterVolume = counterVolume;
}
}