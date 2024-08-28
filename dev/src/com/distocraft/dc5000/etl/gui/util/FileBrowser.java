package com.distocraft.dc5000.etl.gui.util;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.distocraft.dc5000.etl.gui.etl.EtlDetailValue;
/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * 
 * File browser for log browsing. Class is used for DC5000 log browsing and reading lines with 
 * certain time and name pairs.
 *  
 * @author Jani Vesterinen
 */
public class FileBrowser {
	private final File file;
	private BufferedReader br;
  private final Log log    = LogFactory.getLog(this.getClass()); // general logger 
	/**
	 * Constructs new instance for specified path.
	 * @param path
	 */
	public FileBrowser(final String path){
		file = new File(path);
	}

	/**
	 * @param file
	 * @throws FileNotFoundException
	 */
	public void openFile(final File file) throws FileNotFoundException{
		br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	}
	

	/**
   * Reads log files in folder. Map is for parameters, that are all needed for succesfull file parsing.<br/>
   * Map should include following parameters form class <code>com.distocraft.dc5000.etl.gui.util.Helper</code><br />
   * <li>Helper.PARAM_START_TIME</li>
   * <li>Helper.PARAM_END_TIME</li>
   * <li>Helper.PARAM_SET_NAME</li>
   * <li>Helper.PARAM_TECHPACKNAME</li>
   * <li>Helper.PARAM_TYPE</li>
   * <br />
   * Start- and end time are in following format: yyyy-mm-dd hh:mm:ss.ms
	 * @param parameters - Map of key - value pairs.
	 * @return list that contains rows from file
	 */
	public List<EtlDetailValue> parseFolder(final Map<String, String[]> parameters){
	  final List<EtlDetailValue> list = new LinkedList<EtlDetailValue>();

    String startTime = "";
    String endTime = "";
    String setName = "";
    String techPack = "";
    String type = "";
    String logStampStart = "";
    String logStampEnd = "";
    for (Entry<String, String[]> entry : parameters.entrySet()) {
      final String key = (String) entry.getKey();

      if (key.equals(Helper.PARAM_START_TIME)) {
        startTime = ((String[]) entry.getValue())[0];
      } else if (key.equals(Helper.PARAM_END_TIME)) {
        endTime = ((String[]) entry.getValue())[0];
      } else if (key.equals(Helper.PARAM_SET_NAME)) {
        setName = ((String[]) entry.getValue())[0];
      } else if (key.equals(Helper.PARAM_TECHPACKNAME)) {
        techPack = ((String[]) entry.getValue())[0];
      } else if (key.equals(Helper.PARAM_TYPE)) {
        type = ((String[]) entry.getValue())[0];
      }
    }
    
    logStampStart = getLogNameTime(startTime);
    logStampEnd = getLogNameTime(endTime);
    String fileName = "";
    try{
      final File[] files = file.listFiles();
      int index = 0;
			for (int i=0; i < files.length; i++){
				log.debug(files[i].getPath());
        openFile(files[i]);
        fileName = files[i].getName();
        boolean isMulti = false;
        log.debug(logStampStart  + " : " + logStampEnd + " : " + fileName);
        if (fileName.indexOf(logStampStart) != -1 || fileName.indexOf(logStampEnd) != -1 ){
  				String line = "";

          line = br.readLine();
  				do{
            isMulti = false;
            //check if line is error line
            if (line.indexOf("(") == -1 && line.toLowerCase().indexOf("exception") == -1){
    				  //check if timestamp is valid, start time and end time are compared to logfile time
              if (isTimeStamp(line, startTime, endTime)){
                 if (line.indexOf(setName) != -1 && line.indexOf(techPack) != -1 && line.indexOf(type) != -1){
                     final EtlDetailValue detail = new EtlDetailValue();
                     detail.setIndex(index);
                     String tmpLine = line;
                     //iterates following error lines.
                     if (line.toLowerCase().indexOf("error") != -1 || line.toLowerCase().indexOf("severe") != -1
                         || line.toLowerCase().indexOf("warning") != -1){  
                       while ((line = br.readLine()) != null && 
                           (line.indexOf("(") != -1 || line.toLowerCase().indexOf("exception") != -1)){
                           tmpLine = tmpLine + line + "\n";
                           isMulti = true;
                       }
                     }
                     detail.setValue(tmpLine);
                     list.add(detail);
                     index++;
                 }//line
              }//timestamp()
            }//exception
           if (!isMulti) {
             line = br.readLine();
           }
          }while (line != null);
          br.close();//file handled
  				
        }//new file
      }
    }catch(Exception e){
      log.info("file not found: "  + file);
      //e.printStackTrace();
		}
		return list;
	}

	/**
   * Compares end/start time to time on certain line from file.
	 * @param line
	 * @param startTime
	 * @param endTime
	 * @return istimespamp
	 */
	private boolean isTimeStamp(final String line, final String startTime, final String endTime) {
	  //time comparing
    //15.06 07:22:34 should be parsed in a way, that comparison is made using only time, not the date..
    //parameters from browser
    //starttime=2005-06-23 11:55:34.0&setname=Loader_DC_E_BSS_LOAS&type=Loader&endtime=2005-06-23 11:55:39.0&techpak=E_BSS
    //time format in log file:
    //15.06 10:42:44 SEVERE etl.E_BSS.Loader.Loader_DC_E_BSS_BSC.Join_DC_E_BSS_BSC.execute : Action execution Failed:
	  final DateFormatter startDF = new DateFormatter(Helper.DATE_URL_FORMAT);
	  final DateFormatter endDF = new DateFormatter(Helper.DATE_URL_FORMAT);
	  final DateFormatter logDF = new DateFormatter(Helper.DATE_LOG_FORMAT);

    startDF.setCalendar(startTime);
    endDF.setCalendar(endTime);
    logDF.setCalendar(getLogLineTime(line, startDF.getCurrentYear()));
    
    return (logDF.getTime().after(startDF.getTime()) && logDF.getTime().before(endDF.getTime())) || 
        (logDF.getTime().equals(endDF.getTime()) || logDF.getTime().equals(startDF.getTime()));
  }

  /**
   * Selects log name from url parameters.
   * @param time
   * @return log name
   */
  private String getLogNameTime(String time) {
    final StringTokenizer st = new StringTokenizer(time, " ");
    time = st.nextToken().toString();
    time = time.replaceAll("-", "_");
    return time;
  }

  /**
   * Parses time from logentry. In time, the current year is added because there is no year in log timestamp.
   * @param line
   * @param year
   * @return log line time
   */
  private String getLogLineTime(final String line, final int year) {
    final StringTokenizer st = new StringTokenizer(line, " ");    
    String time = year + "." + st.nextToken().toString() + " ";
    time += st.nextToken().toString();
    return time;
  }  

}
