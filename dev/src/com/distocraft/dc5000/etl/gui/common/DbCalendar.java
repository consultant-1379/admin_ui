/*
 * Created on 19.11.2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.distocraft.dc5000.etl.gui.common;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * 
 * @author Raatikainen
 * 
 *         DbCalendar extends java.util.GregorianCalendar It has some utility methods for easy handling of database
 *         (ASA, IQ) date datatype
 */
public class DbCalendar extends GregorianCalendar {

  private static final long serialVersionUID = 1L;

  /**
   * create Calendar and set it to current date
   * 
   */
  public DbCalendar() {
    super();
  }

  /**
   * Create calendar by giving year 2004, month 12 and day 31
   * 
   * @param year
   * @param month
   * @param date
   */
  public DbCalendar(final int year, final int month, final int date) {
    super(year, month - 1, date);
  }

  /**
   * Create calendar by giving strings year '2004', month '12' and day '31'
   * 
   * @param year
   * @param month
   * @param date
   */
  public DbCalendar(final String year, final String month, final String date) {
    super(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(date));
  }

  public DbCalendar(final String year, final String month, final String date, final int hour, final int minute, final int seconds) {
    set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(date), hour, minute, seconds);
  }

  /**
   * set calendar by giving strings year '2004', month '12' and day '31'
   * 
   * @param year
   * @param month
   * @param date
   * @param minute
   * @param hour
   * @param second
   */
  public void set(final String year, final String month, final String date, final int hour, final int minute, final int seconds) {
    super.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(date), hour, minute, seconds);
  }

  /**
   * set calendar by giving strings year '2004', month '12' and day '31'
   * 
   * @param year
   * @param month
   * @param date
   * @param minute
   * @param hour
   * @param second
   */
  public void set_(final int year, final int month, final int date, final int hour, final int minute, final int seconds) {
    super.set(year, month - 1, date, hour, minute, seconds);
  }

  /**
   * Create calendar by giving date as '2004-12-31' or '31.12.2004'
   * 
   * @param time
   *          String
   */
  public DbCalendar(final String time) {
    set(time);
  }

  /**
   * Create calendar by giving date as '2004-12-31' or '31.12.2004'
   * 
   * @param time
   *          String
   */
  public DbCalendar(final Calendar date) {
    set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
  }

  /**
   * Get Year as an int
   * 
   * @return Year as an int
   */
  public int getYear() {
    return get(Calendar.YEAR);
  }

  /**
   * return Year as a String "2004"
   * 
   * @return Year as a String
   */
  public String getYearString() {
    return "" + getYear();
  }

  /**
   * get Month as an int
   * 
   * @return Month as an int
   */
  public int getMonth() {
    return get(Calendar.MONTH) + 1;
  }

  /**
   * Get Month as a String ("01".."12")
   * 
   * @return Month as a String
   */
  public String getMonthString() {
    final int month = getMonth();

    if (month < 10) {
      return "0" + month;
    } else {
      return "" + month;
    }
  }

  /**
   * Get Day as an int
   * 
   * @return Day as an int
   */
  public int getDay() {
    return get(Calendar.DAY_OF_MONTH);
  }

  /**
   * Get Day as a String ("01".."31")
   * 
   * @return Day as a String
   */
  public String getDayString() {
    final int day = getDay();

    if (day < 10) {
      return "0" + day;
    } else {
      return "" + day;
    }
  }

  /**
   * Set date by giving String "2004-12-31" or "31.12.2004"
   * 
   * @param time
   */
  public void set(final String time) {
    int year;
    int month;
    int day;

    if (time.indexOf(".") > 0) {
      final java.util.StringTokenizer t = new java.util.StringTokenizer(time, ".");

      day = Integer.parseInt(t.nextToken());
      month = Integer.parseInt(t.nextToken());
      year = Integer.parseInt(t.nextToken());
    } else {
      final java.util.StringTokenizer t = new java.util.StringTokenizer(time, "-");

      year = Integer.parseInt(t.nextToken());
      month = Integer.parseInt(t.nextToken());
      day = Integer.parseInt(t.nextToken());
    }

    set(year, month - 1, day);
  }

  /**
   * Set Year
   * 
   * @param year
   *          int
   */
  public void setYear(final int year) {
    set(year, getMonth() - 1, getDay());
  }

  /**
   * Set Year
   * 
   * @param year
   *          String
   */
  public void setYear(final String year) {
    setYear(Integer.parseInt(year));
  }

  /**
   * Set Month
   * 
   * @param month
   *          int
   */
  public void setMonth(final int month) {
    set(getYear(), month - 1, getDay());
  }

  /**
   * Set Month
   * 
   * @param month
   *          String
   */
  public void setMonth(final String month) {
    setMonth(Integer.parseInt(month));
  }

  /**
   * Set Day
   * 
   * @param day
   *          int
   */
  public void setDay(final int day) {
    set(getYear(), getMonth() - 1, day);
  }

  /**
   * Set Day
   * 
   * @param day
   *          String
   */
  public void setDay(final String day) {
    setDay(Integer.parseInt(day));
  }

  /**
   * Correct date by n days forward or backward
   * 
   * @param days
   *          int
   */
  public void correctByDays(final int n) {
    add(DAY_OF_MONTH, n);
  }

  /**
   * Correct date by n months forward or backward
   * 
   * @param months
   *          int
   */
  public void correctByMonths(final int n) {
    add(MONTH, n);
  }

  /**
   * Get time as database String "2004-12-31"
   * 
   * @return date String in format "2004-12-31"
   */
  public String getDbDate() {
    return getYearString() + "-" + getMonthString() + "-" + getDayString();
  }

  public String getDBDateTime() {
    return getDbDate() + " " + (get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") + get(Calendar.HOUR_OF_DAY) + ":"
        + (get(Calendar.MINUTE) < 10 ? "0" : "") + get(Calendar.MINUTE) + ":" + (get(Calendar.SECOND) < 10 ? "0" : "")
        + get(Calendar.SECOND);
  }

  /**
   * Get time as database String "'2004-12-31'"
   * 
   * @return date String in format "'2004-12-31'"
   */
  public String getDbDateDbFormat() {
    return "'" + getDbDate() + "'";
  }

  public String getDBDateTimeDBFormat() {
    return "'" + getDBDateTime() + "'";
  }

  public String getLocalDisplayDate() {

    final StringBuffer st = new StringBuffer();

    return new SimpleDateFormat().format(new Date(this.time), st, new FieldPosition(0)).toString();

  }

  /**
   * Get time as displayable String "31.12.2004"
   * 
   * @return date String in format "31.12.2004"
   */
  public String getDisplayDate() {
    return getDayString() + "." + getMonthString() + "." + getYearString();
  }
}
