/**
 * 
 */
package com.distocraft.dc5000.etl.gui.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.distocraft.dc5000.common.StaticProperties;

/**
 * @author eninkar
 * 
 */
public class DateFormatterTest {

  private static DateFormatter df;

  private static Calendar cal;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {

    File homeDir = new File(System.getProperty("user.dir"));
    /* Creating static property file */
    File sp = new File(homeDir, "static.properties");
    try {
      PrintWriter pw = new PrintWriter(new FileWriter(sp));
      pw.print("firstDayOfTheWeek=4\n");
      pw.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    /* Setting the system property for static property file */
    System.setProperty("CONF_DIR", homeDir.getPath());    
    System.setProperty("dc5000.config.directory", homeDir.getPath());

    /* Initializing Static Properties in order to initialize SessionHandler */
    // StaticProperties.reload();

    // System.setProperty("dc5000.config.directory", "/eniq/sw/conf");
    StaticProperties.reload();
    String date = "yyyy-MM-dd";
    df = new DateFormatter(date);
    String time = "2010-08-25";
    df.setCalendar(time);
    cal = Calendar.getInstance();

  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    File homeDir = new File(System.getProperty("CONF_DIR"));
    final File f = new File(homeDir, "static.properties");
    if (!f.delete()) {
      fail("failed to remove test property file");
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.util.DateFormatter#getFormattedWeekOnly(java.util.Calendar)}.
   */
  @Test
  public void testreverseTimeWeek() {

    List<String> actualWeekValues = df.reverseTimeWeek(8);

    Vector<String> expactedWeekValues = new Vector<String>();

    expactedWeekValues.add("W 35");
    expactedWeekValues.add("W 34");
    expactedWeekValues.add("W 33");
    expactedWeekValues.add("W 32");
    expactedWeekValues.add("W 31");
    expactedWeekValues.add("W 30");
    expactedWeekValues.add("W 29");
    expactedWeekValues.add("W 28");

    assertEquals(expactedWeekValues, actualWeekValues);

  }

  @Test
  public void testgetCurrentDate() {
    DateFormatter df = new DateFormatter();
    String data = df.getCurrentDate("2011-05-27");
    // assertEquals(data, "Year");
  }

  @Test
  public void testreverseTime() {
    DateFormatter df = new DateFormatter();
    df.reverseTime(10);
    // assertThat(data.size(), is(10));
  }

  @Test
  public void testreverseTimeDay() {
    DateFormatter df = new DateFormatter();
    final List<String> data = df.reverseTimeDay(10);
    assertThat(data.size(), is(10));
  }

  @Test
  public void testreverseTimeDateYear() {
    DateFormatter df = new DateFormatter();
    final List<String> data = df.reverseTimeDateYear(10);
    assertThat(data.size(), is(10));
  }

  @Test
  public void testreverseTimeDayString() {
    DateFormatter df = new DateFormatter();
    String data = df.reverseTimeDayString(10);
    // assertEquals(data, "Year");
  }

  @Test
  public void testparseTimeStampToDateMonth() {
    DateFormatter df = new DateFormatter();
    String data = df.parseTimeStampToDateMonth("Year Month Day");
    assertEquals(data, "Year");
  }

  @Test
  public void testparseTimeStampToDateMonth1() {
    DateFormatter df = new DateFormatter();
    String data = df.parseTimeStampToDateMonth("");
    assertEquals(data, null);
  }

  @Test
  public void testparseDateToYear() {
    DateFormatter df = new DateFormatter();
    String data = df.parseDateToYear("Year-Month-Day");
    assertEquals(data, "Year");
  }

  @Test
  public void testparseDateToYear1() {
    DateFormatter df = new DateFormatter();
    String data = df.parseDateToYear("");
    assertEquals(data, null);
  }

  @Test
  public void testparseDateToMonth() {
    DateFormatter df = new DateFormatter();
    String data = df.parseDateToMonth("Year-Month-Day");
    assertEquals(data, "Month");
  }

  @Test
  public void testparseDateToMonth1() {
    DateFormatter df = new DateFormatter();
    String data = df.parseDateToMonth("");
    assertEquals(data, null);
  }

  @Test
  public void testparseDateToDay() {
    DateFormatter df = new DateFormatter();
    String data = df.parseDateToDay("Year-Month-Day");
    assertEquals(data, "Day");
  }

  @Test
  public void testparseDateToDay1() {
    DateFormatter df = new DateFormatter();
    String data = df.parseDateToDay("");
    assertEquals(data, null);
  }

  @Test
  public void testgetActualMaximum() {
    DateFormatter df = new DateFormatter();
    int data = df.getActualMaximum(10);
    assertEquals(data, 11);
  }

  @Test
  public void testreverseTimeMonth() {
    DateFormatter df = new DateFormatter();
    final List<String> data = df.reverseTimeMonth(10);
    assertThat(data.size(), is(10));
  }
}
