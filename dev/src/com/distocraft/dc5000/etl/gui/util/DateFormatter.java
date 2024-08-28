package com.distocraft.dc5000.etl.gui.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ericsson.eniq.common.CalcFirstDayOfWeek;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Class is wrapper class for Calendar. Extra features are added for timeparsing and certain time views.
 * @author Jani Vesterinen
 */
public class DateFormatter {
  private final SimpleDateFormat sdf;

  private final Log log = LogFactory.getLog(this.getClass());
  
  private final Calendar cal = Calendar.getInstance();
  
  private final int firstDayOfTheWeek = CalcFirstDayOfWeek.calcFirstDayOfWeek();
  private final int endOfWeek = (firstDayOfTheWeek > 1)?firstDayOfTheWeek-1:firstDayOfTheWeek+6;


  /**
   * Formats date in correct format. e.g. HH.mm.ss.mm
   * @param format
   */
  public DateFormatter(final String format) {
    //"HH.mm.ss.mm"
    sdf = new SimpleDateFormat(format);
  }

  /**
   * default constructor
   */
  public DateFormatter() {
    super();
    sdf = new SimpleDateFormat("yyyy-MM-dd");
  }

  /**
   * Returns Calendar object with current time.
   * @return
   */
  public synchronized Calendar getTime() {
    return cal;
  }

  /**
   * Returns current year as integer value
   * @return year
   */
  public int getCurrentYear() {
    return cal.get(Calendar.YEAR);
  }

  /**
   * Returns current day of month
   * @return current day
   */
  public int getCurrentDate() {
    return cal.get(Calendar.DAY_OF_MONTH);
  }

  /**
   * Returns current month of year
   * @return current month
   */
  public int getCurrentMonth() {
    return cal.get(Calendar.MONTH);
  }
  
  /**
   * Returns current hour 
   * @return current hour
   */
  public int getCurrentHour() {
    return cal.get(Calendar.HOUR_OF_DAY);
  }
  
  /**
   * Returns current minute 
   * @return current minute
   */
  public int getCurrentMinute() {
    return cal.get(Calendar.MINUTE);
  }
  

  /**
   * Shows time as wanted with certain delimeter. e.g time is wanted as '-' delimiting time -> 13-23-24 
   * @param delim
   * @return par
   */
  public String getCurrentTime(final String delim) {
    final int seconds = cal.get(Calendar.SECOND);
    final int minutes = cal.get(Calendar.MINUTE);
    final int hours = cal.get(Calendar.HOUR_OF_DAY);
    String secFormat = String.valueOf(seconds);
    String minFormat = String.valueOf(minutes);
    String hourFormat = String.valueOf(hours);
    if (seconds < 10) {
      secFormat = "0" + seconds;
    } 
    if (minutes < 10) {
      minFormat = "0" + minutes;
    }
    if (hours < 10) {
      hourFormat = "0" + hours;
    }
    return (hourFormat + delim + minFormat + delim + secFormat);
  }

  /**
   * Formats month and date with certain delimeter. e.g. 30.12 (mm.dd)
   * @param delim
   * @return
   */
  public String getCurrentDate(final String delim) {
    final int date = cal.get(Calendar.DAY_OF_MONTH);
    String formatDate = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    final int month = cal.get(Calendar.MONTH) + 1;
    String formatMonth = String.valueOf(cal.get(Calendar.MONTH) + 1);

    if (date < 10) {
      formatDate = "0" + date;
    }
    if (month < 10) {
      formatMonth = "0" + month;
    }
    return formatDate + delim + formatMonth;
  }

  /**
   * Formats month and date 
   * @param tmp
   * @return
   */
  public String getFormattedMonthYear(final Calendar tmp) {
    
    final String formatYear = String.valueOf(tmp.get(Calendar.YEAR));
    final int month = tmp.get(Calendar.MONTH) + 1;
    String formatMonth = String.valueOf(tmp.get(Calendar.MONTH) + 1);

    if (month < 10) {
      formatMonth = "0" + month;
    }
    return formatYear + "/" + formatMonth;
  }  

  /**
   * Formats month and date 
   * @param tmp
   * @return
   */
  public String getFormattedDate(final Calendar tmp) {
    
    final int date = tmp.get(Calendar.DAY_OF_MONTH);
    String formatDate = String.valueOf(tmp.get(Calendar.DAY_OF_MONTH));
    final int month = tmp.get(Calendar.MONTH) + 1;
    String formatMonth = String.valueOf(tmp.get(Calendar.MONTH) + 1);

    if (date < 10) {
      formatDate = "0" + date;
    }
    if (month < 10) {
      formatMonth = "0" + month;
    }
    
    return formatDate + "-" + formatMonth;
  }    
  
  /**
   * Formats week in format W45 (7.11 - 13.11)
   * @param tmp
   */
  public synchronized String getFormattedWeek(final Calendar tmp) {
    final Calendar tmpcal = Calendar.getInstance();

    tmpcal.setTime(tmp.getTime());
    tmpcal.setFirstDayOfWeek(firstDayOfTheWeek);
    tmpcal.setMinimalDaysInFirstWeek(4);
    tmpcal.set(Calendar.HOUR_OF_DAY, 11);
    final String week = "W" + (tmpcal.get(Calendar.WEEK_OF_YEAR));
    
    tmpcal.set(Calendar.DAY_OF_WEEK, endOfWeek);
    final String startDate = tmpcal.get(Calendar.DAY_OF_MONTH)  + "." + (tmpcal.get(Calendar.MONTH) + 1);
    tmpcal.add(Calendar.DAY_OF_MONTH, -6);
    final String endDate = tmpcal.get(Calendar.DAY_OF_MONTH) + "." + (tmpcal.get(Calendar.MONTH) + 1);
    return week + "(" + endDate + "-" + startDate + ")";
  }    

  /**
   * Formats week in format Wxx
   * @param tmp
   * @return formatted week
   */
  public synchronized String getFormattedWeekOnly(final Calendar tmp) {
    final Calendar tmpcal = (Calendar)tmp.clone();
  //  tmpcal.setFirstDayOfWeek(firstDayOfTheWeek);
   // tmpcal.setMinimalDaysInFirstWeek(4);
    tmpcal.setTime(tmp.getTime());
    tmpcal.set(Calendar.HOUR_OF_DAY, 11);
    final String week = "W " + (tmpcal.get(Calendar.WEEK_OF_YEAR));
    return week;
  }      
  
  /**
   * Formats week in format 45/2005
   * @param tmp
   * @return formatted month
   */
  public String getFormattedMonth(final Calendar tmp) {
    
    final Calendar c = Calendar.getInstance();
    c.setTime(tmp.getTime());

    c.setTime(tmp.getTime());
    //c.setMinimalDaysInFirstWeek(4);
    final String week = c.get(Calendar.WEEK_OF_YEAR) + "/" + c.get(Calendar.YEAR);

    return week;
  }      
  
  /**
   * Formats year, month and date with certain delimeter. e.g. 2005.30.12 (yyyy.mm.dd)
   * @param delim
   * @return current date and year
   */
  public String getCurrentDateAndYear(final String delim) {
    final int date = cal.get(Calendar.DAY_OF_MONTH);
    String formatDate = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    final int month = cal.get(Calendar.MONTH) + 1;
    String formatMonth = String.valueOf(cal.get(Calendar.MONTH) + 1);

    if (date < 10) {
      formatDate = "0" + date;
    }
    if (month < 10) {
      formatMonth = "0" + month;
    }
    return cal.get(Calendar.YEAR) + delim + formatMonth + delim
        + formatDate;
  }

  /**
   * Formats year, month and date with certain delimeter. e.g. 2005.30.12 (yyyy.mm.dd)
   * @param delim
   * @return date and year
   */
  public String getCurrentDateAndYear(final Calendar tmpcal, final String delim) {
    final int date = tmpcal.get(Calendar.DAY_OF_MONTH);
    String formatDate = String.valueOf(tmpcal.get(Calendar.DAY_OF_MONTH));
    final int month = tmpcal.get(Calendar.MONTH) + 1;
    String formatMonth = String.valueOf(tmpcal.get(Calendar.MONTH) + 1);

    if (date < 10) {
      formatDate = "0" + date;
    }
    if (month < 10) {
      formatMonth = "0" + month;
    }
    return tmpcal.get(Calendar.YEAR) + delim + formatMonth + delim
        + formatDate;
  }  
  
  /**
   * Sets time, object that is created is accessed via DateFormatter.
   * @param time
   */
  public void setCalendar(final String time) {
    try {
    	cal.setFirstDayOfWeek(firstDayOfTheWeek);
    	cal.setMinimalDaysInFirstWeek(4);
    	cal.setTime(sdf.parse(time));
    } catch (ParseException e) {
      log.error("ParseException",e);
    }
  }

  /**
   * Reverses time in amount of months. Returns calendar instance representing new time of date.
   * Example of reversing time:
   * DateFormatter df = new DateFormatter("yyyy-MM-dd hh:mm:ss.mm");
     df.setCalendar("2005-06-23 11:55:34.0");
     df.reverseTime(3).getTime();
   * @param amount - amount of months that is subtracted from current time
   * @return Calendar - represents subtracted time.
   */
  public Calendar reverseTime(final int amount) {
    Calendar working;
    working = Calendar.getInstance();
    working.add(Calendar.MONTH, -(amount));
    return working;
  }

  /**
   * Reverses time in amount of days. Returns Vector that includes all the dates that were reversed backwards.
   * @param amount - amount of months that is subtracted from current time
   * @return Calendar - represents subtracted time.
   */
  public List<String> reverseTimeDay(final int amount) {
    final Vector<String> days = new Vector<String>();
    //DateFormatter df = new DateFormatter("yyyy-MM-dd");
    for (int i=0; i < amount; i++){
      days.addElement(getFormattedDate(cal));
      cal.add(Calendar.DAY_OF_MONTH, -(1));
    }
      
    return days;
  }  
  
  /**
   * Reverses time in amount of days. Returns Vector that includes all the dates that were reversed backwards including year information.
   * @param amount - amount of days that is subtracted from current time
   * @return Calendar - represents subtracted time.
   */
  public List<String> reverseTimeDateYear(final int amount) {
    final Vector<String> days = new Vector<String>();
    for (int i=0; i < amount; i++){
      days.addElement(getCurrentDateAndYear("-"));
      cal.add(Calendar.DAY_OF_MONTH, -(1));
    }
    return days;
  }  
//df4.getActualMaximum(Calendar.DAY_OF_MONTH)
  
  /**
   * Reverses time in amount of days. Returns Vector that includes all the dates that were reversed backwards.
   * @param cal - amount of months that is subtracted from current time
   * @return Calendar - represents subtracted time.
   */
  public List<String> reverseTimeMonth(final Calendar cal) {
    final Calendar tmp = Calendar.getInstance();
    tmp.setFirstDayOfWeek(firstDayOfTheWeek);
    tmp.setMinimalDaysInFirstWeek(4);
    tmp.setTime(cal.getTime());
    final Vector<String> days = new Vector<String>();
    //DateFormatter df = new DateFormatter("yyyy-MM-dd");
    for (int i=0; i < 4; i++){
      days.addElement(getFormattedMonthYear(tmp));
      tmp.add(Calendar.MONTH, -(1));
    }
      
    return days;
  }    
  /**
   * Reverses time in amount of days. Returns Vector that includes all the dates that were reversed backwards.
   * @param amount - amount of months that is subtracted from current time
   * 
   */
  public void reverseTimeDayTimeStamp(final int amount) {
    
    //DateFormatter df = new DateFormatter("yyyy-MM-dd hh:mm:ss.mm");
    cal.add(Calendar.DAY_OF_MONTH, -(amount));

  }    
  
  /**
   * Reverses time in amount of days. Returns date with reversed time.
   * @param amount - amount of months that is subtracted from current time
   * @return Calendar - represents subtracted time.
   */
  public String reverseTimeDayString(final int amount) {
    //DateFormatter df = new DateFormatter("yyyy-MM-dd");
    cal.add(Calendar.DAY_OF_MONTH, (amount));

    return getCurrentDateAndYear(cal, "-");
  }    
  
  public static synchronized String parseTimeStampToDateMonth(final String stamp){
    final StringTokenizer st = new StringTokenizer(stamp, " ");
    String ret = "";
    
    if (st.hasMoreTokens()){
      ret = st.nextToken().toString();      
    }else{
      ret = null;
    }
    
    return ret;
  }

  public static synchronized String parseDateToYear(final String stamp){
    final StringTokenizer st = new StringTokenizer(stamp, "-");
    String ret = "";
    
    if (st.hasMoreTokens()){
      ret = st.nextToken().toString();      
    }else{
      ret = null;
    }
    
    return ret;
  }

  public static synchronized String parseDateToMonth(final String stamp){
    final StringTokenizer st = new StringTokenizer(stamp, "-");
    String ret = "";
    
    if (st.hasMoreTokens()){
      st.nextToken();
      ret = st.nextToken().toString();      
    }else{
      ret = null;
    }
    
    return ret;
  } 
  
  public static synchronized String parseDateToDay(final String stamp){
    final StringTokenizer st = new StringTokenizer(stamp, "-");
    String ret = "";
    
    if (st.hasMoreTokens()){
      st.nextToken();
      st.nextToken();
      ret = st.nextToken().toString();      
    }else{
      ret = null;
    }
    
    return ret;
  } 
  
//  //Example for testing and using Dateformatter and comparator.
//  public static void main(final String[] args) {
//    /*DateFormatter df = new DateFormatter("yyyy-MM-dd hh:mm:ss.mm");
//    DateFormatter present = new DateFormatter("yyyy-MM-dd hh:mm:ss.mm");
//    df.setCalendar("2005-06-23 11:55:34.0");
//    df.reverseTimeDayTimeStamp(3);
//    System.out.println(df.getTime().getTime().toString());
//    System.out.println(present.getTime().getTime().toString());
//    System.out.println(present.getTime().after(df.getTime()));
//    Calendar c = Calendar.getInstance();
//    String week = "W" + (c.get(Calendar.WEEK_OF_YEAR));
//    c.set(DbCalendar.DAY_OF_WEEK,firstDayOfTheWeek);
//    String endDate = c.getTime().getDate() + "." + (c.getTime().getMonth() + 1);
//    c.set(DbCalendar.DAY_OF_WEEK,endOfWeek);
//    String startDate = c.getTime().getDate()  + "." + (c.getTime().getMonth() + 1);
//    System.out.println(week + "(" + endDate + "-" + startDate + ")");*/
//    for(int i=0; i < 4; i++){
//      final Calendar c = Calendar.getInstance();
//     //c.setMinimalDaysInFirstWeek(4);
//     //System.out.println(c.get(c.WEEK_OF_YEAR));
//     c.setFirstDayOfWeek(Calendar.MONDAY);
//     c.set(Calendar.WEEK_OF_YEAR, c.get(Calendar.WEEK_OF_YEAR)-i);
//     c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
//     //String start = c.getTime().toString();
//     c.add(Calendar.DAY_OF_MONTH, -6);
//     //String end = c.getTime().toString();
//     //System.out.println(c.get(Calendar.WEEK_OF_YEAR) + " " + start + " - "+ end);
//     c.add(Calendar.DAY_OF_MONTH, -1);
//  }
//  }

  public int getActualMaximum(final int day_of_month) {
    return cal.getActualMaximum(day_of_month);
  }

  public synchronized List<String> reverseTimeWeek(final int amount) {
    final Vector<String> days = new Vector<String>();
    //DateFormatter df = new DateFormatter("yyyy-MM-dd");
    cal.setFirstDayOfWeek(firstDayOfTheWeek);
    cal.setMinimalDaysInFirstWeek(4);
    for (int i=0; i < amount; i++){
      //this is for Solaris platform. Is working fine in windows, but not in sol without this..(Dunno why..)
      //cal.setMinimalDaysInFirstWeek(4);
      days.addElement(getFormattedWeekOnly(cal));
      cal.add(Calendar.WEEK_OF_YEAR, -(1));
    }
      
    return days;
  }

  public List<String> reverseTimeMonth(final int amount) {
    final Vector<String> days = new Vector<String>();
    //DateFormatter df = new DateFormatter("yyyy-MM-dd");
    cal.setFirstDayOfWeek(firstDayOfTheWeek);
    cal.setMinimalDaysInFirstWeek(4);
    for (int i=0; i < amount; i++){
      days.addElement(getFormattedMonth(cal));
      cal.add(Calendar.WEEK_OF_YEAR, -(1));
    }
      
    return days;
  }
  

}
