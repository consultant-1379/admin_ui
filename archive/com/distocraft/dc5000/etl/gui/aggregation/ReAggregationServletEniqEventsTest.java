/*------------------------------------------------------------------------
 *
 *
 *      COPYRIGHT (C) ERICSSON RADIO SYSTEMS AB, Sweden
 *
 *      The  copyright  to  the document(s) herein  is  the property of
 *      Ericsson Radio Systems AB, Sweden.
 *
 *      The document(s) may be used  and/or copied only with the written
 *      permission from Ericsson Radio Systems AB  or in accordance with
 *      the terms  and conditions  stipulated in the  agreement/contract
 *      under which the document(s) have been supplied.
 *
 *------------------------------------------------------------------------
 */

package com.distocraft.dc5000.etl.gui.aggregation;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;

public class ReAggregationServletEniqEventsTest extends TestCase {

  private StubbedReAggregationServletEniqEvents testObject = null;

  private final DbCalendar dateCheck = null;

  private DbCalendar dayminus1 = null;

  private DbCalendar dayminus2 = null;

  private DbCalendar dayminus3 = null;

  private DbCalendar day = null;

  private DbCalendar dayplus1 = null;

  private DbCalendar dayplus2 = null;

  private DbCalendar dayplus3 = null;

  private DbCalendar latest15Min = null;

  private DbCalendar latest1Min = null;

  long latest15MinInMilli;

  long latest15Minminus3InMilli;

  long latest15Minminus2InMilli;

  long latest15Minminus1InMilli;

  long latest15Minplus1InMilli;

  long latest15Minplus2InMilli;

  long latest15Minplus3InMilli;

  long latest1MinInMilli;

  long latest1Minminus3InMilli;

  long latest1Minminus2InMilli;

  long latest1Minminus1InMilli;

  long latest1Minplus1InMilli;

  long latest1Minplus2InMilli;

  long latest1Minplus3InMilli;

  private final DbCalendar cal1 = new DbCalendar();

  final Vector<Date> dayWithDurationtion0 = new Vector<Date>();

  final Vector<Date> dayWithDurationtion1 = new Vector<Date>();

  final Vector<Date> dayWithDurationtion2 = new Vector<Date>();

  final Vector<Date> dayWithDurationtionMinus1 = new Vector<Date>();

  final Vector<Date> dayWithDurationtionMinus2 = new Vector<Date>();

  final Vector<Date> dayWithDurationtionMinus3 = new Vector<Date>();

  final Vector<Date> one5WithDurationtion0 = new Vector<Date>();

  final Vector<Date> one5WithDurationtion1 = new Vector<Date>();

  final Vector<Date> one5WithDurationtion2 = new Vector<Date>();

  final Vector<Date> one5WithDurationtionMinus1 = new Vector<Date>();

  final Vector<Date> one5WithDurationtionMinus2 = new Vector<Date>();

  final Vector<Date> one5WithDurationtionMinus3 = new Vector<Date>();

  final Vector<Date> oneMinWithDurationtion0 = new Vector<Date>();

  final Vector<Date> oneMinWithDurationtion1 = new Vector<Date>();

  final Vector<Date> oneMinWithDurationtion2 = new Vector<Date>();

  final Vector<Date> oneMinWithDurationtionMinus1 = new Vector<Date>();

  final Vector<Date> oneMinWithDurationtionMinus2 = new Vector<Date>();

  final Vector<Date> oneMinWithDurationtionMinus3 = new Vector<Date>();

  final String techPackName = "EVENT_E_SGEH_TERM";

  Context ctx = null;

  final DbCalendar calendarToCheckBeginningOfMonthAndYear_Day = new DbCalendar();

  final DbCalendar calendarToCheckFor15Min = new DbCalendar();

  final DbCalendar calendarToCheckFor1Min = new DbCalendar();

  @Override
  @Before
  public void setUp() {

    calendarToCheckBeginningOfMonthAndYear_Day.set(Calendar.MILLISECOND, 0);
    calendarToCheckBeginningOfMonthAndYear_Day.set(Calendar.SECOND, 0);
    calendarToCheckBeginningOfMonthAndYear_Day.set(Calendar.MINUTE, 0);
    calendarToCheckBeginningOfMonthAndYear_Day.set(Calendar.HOUR_OF_DAY, 0);
    calendarToCheckBeginningOfMonthAndYear_Day.set(Calendar.DATE, 31);
    calendarToCheckBeginningOfMonthAndYear_Day.set(Calendar.MONTH, Calendar.DECEMBER);
    calendarToCheckBeginningOfMonthAndYear_Day.set(Calendar.YEAR, 2010);

    calendarToCheckFor15Min.set(Calendar.MILLISECOND, 0);
    calendarToCheckFor15Min.set(Calendar.SECOND, 0);
    calendarToCheckFor15Min.set(Calendar.MINUTE, 45);
    calendarToCheckFor15Min.set(Calendar.HOUR_OF_DAY, 23);
    calendarToCheckFor15Min.set(Calendar.DATE, 31);
    calendarToCheckFor15Min.set(Calendar.MONTH, Calendar.DECEMBER);
    calendarToCheckFor15Min.set(Calendar.YEAR, 2010);

    calendarToCheckFor1Min.set(Calendar.MILLISECOND, 0);
    calendarToCheckFor1Min.set(Calendar.SECOND, 0);
    calendarToCheckFor1Min.set(Calendar.MINUTE, 0);
    calendarToCheckFor1Min.set(Calendar.HOUR_OF_DAY, 0);
    calendarToCheckFor1Min.set(Calendar.DATE, 1);
    calendarToCheckFor1Min.set(Calendar.MONTH, Calendar.JANUARY);
    calendarToCheckFor1Min.set(Calendar.YEAR, 2011);

    testObject = new StubbedReAggregationServletEniqEvents();

    dayminus3 = new DbCalendar(cal1.getYear(), cal1.getMonth(), cal1.getDay() - 3);
    dayminus2 = new DbCalendar(cal1.getYear(), cal1.getMonth(), cal1.getDay() - 2);
    dayminus1 = new DbCalendar(cal1.getYear(), cal1.getMonth(), cal1.getDay() - 1);
    day = new DbCalendar(cal1.getYear(), cal1.getMonth(), cal1.getDay());
    dayplus1 = new DbCalendar(cal1.getYear(), cal1.getMonth(), cal1.getDay() + 1);
    dayplus2 = new DbCalendar(cal1.getYear(), cal1.getMonth(), cal1.getDay() + 2);
    dayplus3 = new DbCalendar(cal1.getYear(), cal1.getMonth(), cal1.getDay() + 3);

    latest15Min = new DbCalendar(cal1.getYearString(), cal1.getMonthString(), cal1.getDayString(),
        DbCalendar.HOUR_OF_DAY, (DbCalendar.MINUTE - (DbCalendar.MINUTE % 15)), 0);
    latest1Min = new DbCalendar(cal1.getYearString(), cal1.getMonthString(), cal1.getDayString(),
        DbCalendar.HOUR_OF_DAY, (DbCalendar.MINUTE - 1), 0);

    latest15MinInMilli = latest15Min.getTimeInMillis();
    latest15Minminus3InMilli = latest15MinInMilli - (900000 * 3);
    latest15Minminus2InMilli = latest15MinInMilli - (900000 * 2);
    latest15Minminus1InMilli = latest15MinInMilli - (900000 * 1);
    latest15Minplus1InMilli = latest15MinInMilli + (900000 * 1);
    latest15Minplus2InMilli = latest15MinInMilli + (900000 * 2);
    latest15Minplus3InMilli = latest15MinInMilli + (900000 * 3);

    latest1MinInMilli = latest1Min.getTimeInMillis();
    latest1Minminus3InMilli = latest1MinInMilli - (1000 * 3);
    latest1Minminus2InMilli = latest1MinInMilli - (1000 * 2);
    latest1Minminus1InMilli = latest1MinInMilli - (1000 * 1);
    latest1Minplus1InMilli = latest1MinInMilli + (1000 * 1);
    latest1Minplus2InMilli = latest1MinInMilli + (1000 * 2);
    latest1Minplus3InMilli = latest1MinInMilli + (1000 * 3);

    dayWithDurationtion0.add(new Date(dayminus1.getTimeInMillis()));

    dayWithDurationtion1.add(new Date(dayminus1.getTimeInMillis()));
    dayWithDurationtion1.add(new Date(dayminus2.getTimeInMillis()));

    dayWithDurationtion2.add(new Date(dayminus1.getTimeInMillis()));
    dayWithDurationtion2.add(new Date(dayminus2.getTimeInMillis()));
    dayWithDurationtion2.add(new Date(dayminus3.getTimeInMillis()));

    one5WithDurationtion0.add(new Date(latest15Min.getTimeInMillis()));

    one5WithDurationtion1.add(new Date(latest15Min.getTimeInMillis()));
    one5WithDurationtion1.add(new Date(latest15Min.getTimeInMillis() - (900000)));

    one5WithDurationtion2.add(new Date(latest15Min.getTimeInMillis()));
    one5WithDurationtion2.add(new Date(latest15Min.getTimeInMillis() - (900000)));
    one5WithDurationtion2.add(new Date(latest15Min.getTimeInMillis() - (1800000)));

    oneMinWithDurationtion0.add(new Date(latest1Min.getTimeInMillis()));

    oneMinWithDurationtion1.add(new Date(latest1Min.getTimeInMillis()));
    oneMinWithDurationtion1.add(new Date(latest1Min.getTimeInMillis() - (60000)));

    oneMinWithDurationtion2.add(new Date(latest1Min.getTimeInMillis()));
    oneMinWithDurationtion2.add(new Date(latest1Min.getTimeInMillis() - (60000)));
    oneMinWithDurationtion2.add(new Date(latest1Min.getTimeInMillis() - (120000)));

    ctx = new VelocityContext();

  }

  public ReAggregationServletEniqEventsTest(String functionName) {
    super(functionName);
  }

  @Test
  public void testreadInfoFromFileNormalCase() {

    InitialContext context;
    String value = "";

    Vector vec1 = new Vector();
    vec1.add("DAY");
    vec1.add("15MIN");
    vec1.add("1MIN");
    // ReAggregationServletEniqEvents.readInfoFromFile(RE_AGG_SCOPES);

    assertEquals("Test 1 : Pass", vec1,
        testObject.readInfoFromFile(StubbedReAggregationServletEniqEvents.RE_AGG_SCOPES));

  }

  @Test
  // Test objects has date initialized to Jan 1 2011 00:00:00 . This test case
  // will test
  // Beginning of Month,Year and Normal Case .
  public void testgetNearestStartDate_Day() {
    assertEquals("Test 2 : Pass", calendarToCheckBeginningOfMonthAndYear_Day.getTimeInMillis(),
        testObject.getNearestStartDate(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_DAY));

  }

  @Test
  public void testgetNearestStartDate_15MIN() {
    assertEquals("Test 2 : Pass", calendarToCheckFor15Min.getTimeInMillis(),
        testObject.getNearestStartDate(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_15_MIN));

  }

  @Test
  public void testgetNearestStartDate_1MIN() {
    assertEquals("Test 2 : Pass", calendarToCheckFor1Min.getTimeInMillis(),
        testObject.getNearestStartDate(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_1_MIN));

  }

  @Test
  public void testgetDurationWhenOldestTimeAndLastImmediateTimeAreSame() {

    assertEquals(0, testObject.getDuration(day.getTimeInMillis(), day.getTimeInMillis()));
  }

  @Test
  public void testgetDurationWhenOldestTimeLessThanLastImmediateTime1() {

    assertEquals((day.getTimeInMillis() - dayminus1.getTimeInMillis()) / 1000, testObject.getDuration(
dayminus1.getTimeInMillis(), day.getTimeInMillis()));
  }

  @Test
  public void testgetDurationWhenOldestTimeLessThanLastImmediateTime2() {

    assertEquals((day.getTimeInMillis() - dayminus2.getTimeInMillis()) / 1000, testObject.getDuration(
dayminus2.getTimeInMillis(), day.getTimeInMillis()));
  }

  @Test
  public void testgetDurationWhenOldestTimeGreaterThanLastImmediateTime1() {

    assertEquals(-1, testObject.getDuration(dayplus1.getTimeInMillis(), day.getTimeInMillis()));
  }

  @Test
  public void testgetDurationWhenOldestTimeGreaterThanLastImmediateTime2() {

    assertEquals(-1, testObject.getDuration(dayplus2.getTimeInMillis(), day.getTimeInMillis()));
  }

  // /------> 15 Min TCs begin

  @Test
  public void testgetDurationWhenOldestTimeAndLastImmediateTimeAreSame_15Min() {

    assertEquals(0, testObject.getDuration(latest15MinInMilli, latest15MinInMilli));
  }

  @Test
  public void testgetDurationWhenOldestTimeLessThanLastImmediateTime1_15Min() {

    assertEquals((latest15MinInMilli - latest15Minminus1InMilli) / 1000, testObject.getDuration(
        latest15Minminus1InMilli,
 latest15MinInMilli));
  }

  @Test
  public void testgetDurationWhenOldestTimeLessThanLastImmediateTime2_15Min() {

    assertEquals((latest15MinInMilli - latest15Minminus2InMilli) / 1000, testObject.getDuration(
        latest15Minminus2InMilli,
 latest15MinInMilli));
  }

  @Test
  public void testgetDurationWhenOldestTimeGreaterThanLastImmediateTime1_15Min() {

    assertEquals(-1, testObject.getDuration(latest15Minplus1InMilli, latest15MinInMilli));
  }

  @Test
  public void testgetDurationWhenOldestTimeGreaterThanLastImmediateTime2_15Min() {

    assertEquals(-1, testObject.getDuration(latest15Minplus2InMilli, latest15MinInMilli));
  }



  // ---> 1 Min TCs begin

  @Test
  public void testgetDurationWhenOldestTimeAndLastImmediateTimeAreSame_1Min() {

    assertEquals(0, testObject.getDuration(latest1MinInMilli, latest1MinInMilli));
  }

  @Test
  public void testgetDurationWhenOldestTimeLessThanLastImmediateTime1_1Min() {

    assertEquals((latest1MinInMilli - latest1Minminus1InMilli) / 1000, testObject.getDuration(latest1Minminus1InMilli,
 latest1MinInMilli));
  }

  @Test
  public void testgetDurationWhenOldestTimeLessThanLastImmediateTime2_1Min() {

    assertEquals((latest1MinInMilli - latest1Minminus2InMilli) / 1000, testObject.getDuration(latest1Minminus2InMilli,
 latest1MinInMilli));
  }

  @Test
  public void testgetDurationWhenOldestTimeGreaterThanLastImmediateTime1_1Min() {

    assertEquals(-1, testObject.getDuration(latest1Minplus1InMilli, latest1MinInMilli));
  }

  @Test
  public void testgetDurationWhenOldestTimeGreaterThanLastImmediateTime2_1Min() {

    assertEquals(-1, testObject.getDuration(latest1Minplus2InMilli, latest1MinInMilli));
  }

  // // ---> 1 Min TCs end

  @Test
  public void testgetIntervalsForDay() {
    assertEquals(1, testObject.getIntervals(StubbedReAggregationServletEniqEvents.MILLI_SECONDS_IN_A_DAY / 1000,
        StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_DAY));
  }

  @Test
  public void testgetIntervalsForDay2() {
    assertEquals(0, testObject.getIntervals(0, StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_DAY));
  }

  @Test
  public void testgetIntervalsFor15MIN() {
    assertEquals(96, testObject.getIntervals(StubbedReAggregationServletEniqEvents.MILLI_SECONDS_IN_A_DAY / 1000,
        StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_15_MIN), 1000);
  }

  @Test
  public void testgetIntervalsFor15MIN2() {
    assertEquals(0, testObject.getIntervals(0, StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_15_MIN));
  }

  @Test
  public void testgetIntervalsFor1MIN() {
    assertEquals(1440, testObject.getIntervals(StubbedReAggregationServletEniqEvents.MILLI_SECONDS_IN_A_DAY / 1000,
        StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_1_MIN));
  }

  @Test
  public void testgetIntervalsFor1MIN2() {
    assertEquals(0, testObject.getIntervals(0, StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_DAY));
  }
  @Test
  public void testgetDateObjectsWhenDurationIs0ForDay() {

    assertEquals(
        dayWithDurationtion0,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_DAY,
            dayminus1.getTimeInMillis(), 0));

  }

  @Test
  public void testgetDateObjectsWhenDurationIs1ForDay() {
    assertEquals(
        dayWithDurationtion1,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_DAY,
            dayminus1.getTimeInMillis(), 1));
  }

  @Test
  public void testgetDateObjectsWhenDurationIs2ForDay() {
	  org.junit.Assert.assertEquals(
        dayWithDurationtion2,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_DAY,
            dayminus1.getTimeInMillis(), 2));
  }

  @Test
  public void testgetDateObjectsWhenDurationIsMinus1ForDay() {
    assertEquals(
        dayWithDurationtionMinus1,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_DAY,
            dayminus1.getTimeInMillis(), -1));
  }

  @Test
  public void testgetDateObjectsWhenDurationIsMinus2ForDay() {
    assertEquals(
        dayWithDurationtionMinus2,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_DAY,
            dayminus1.getTimeInMillis(), -2));
  }

  @Test
  public void testgetDateObjectsWhenDurationIsMinus3ForDay() {
    assertEquals(
        dayWithDurationtionMinus3,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_DAY,
            dayminus1.getTimeInMillis(), -3));
  }

  @Test
  public void testgetDateObjectsWhenDurationIs0For15MIN() {
    assertEquals(
        one5WithDurationtion0,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_15_MIN,
            latest15Min.getTimeInMillis(), 0));
  }

  @Test
  public void testgetDateObjectsWhenDurationIs1For15MIN() {
    assertEquals(
        one5WithDurationtion1,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_15_MIN,
            latest15Min.getTimeInMillis(), 1));
  }

  @Test
  public void testgetDateObjectsWhenDurationIs2For15MIN() {
    assertEquals(
        one5WithDurationtion2,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_15_MIN,
            latest15Min.getTimeInMillis(), 2));
  }

  @Test
  public void testgetDateObjectsWhenDurationIsMinus1For15MIN() {
    assertEquals(
        one5WithDurationtionMinus1,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_15_MIN,
            latest15Min.getTimeInMillis(), -1));
  }

  @Test
  public void testgetDateObjectsWhenDurationIsMinus2For15MIN() {
    assertEquals(
        one5WithDurationtionMinus2,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_15_MIN,
            latest15Min.getTimeInMillis(), -2));
  }

  @Test
  public void testgetDateObjectsWhenDurationIsMinus3For15MIN() {
    assertEquals(
        one5WithDurationtionMinus3,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_15_MIN,
            latest15Min.getTimeInMillis(), -3));
  }

  @Test
  public void testgetDateObjectsWhenDurationIs0For1MIN() {
    assertEquals(
        oneMinWithDurationtion0,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_1_MIN,
            latest1Min.getTimeInMillis(), 0));
  }

  @Test
  public void testgetDateObjectsWhenDurationIs1For1MIN() {
    assertEquals(
        oneMinWithDurationtion1,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_1_MIN,
            latest1Min.getTimeInMillis(), 1));
  }

  @Test
  public void testgetDateObjectsWhenDurationIs2For1MIN() {
    assertEquals(
        oneMinWithDurationtion2,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_1_MIN,
            latest1Min.getTimeInMillis(), 2));
  }

  @Test
  public void testgetDateObjectsWhenDurationIsMinus1For1MIN() {
    assertEquals(
        oneMinWithDurationtionMinus1,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_1_MIN,
            latest1Min.getTimeInMillis(), -1));
  }

  @Test
  public void testgetDateObjectsWhenDurationIsMinus2For1MIN() {
    assertEquals(
        oneMinWithDurationtionMinus2,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_1_MIN,
            latest1Min.getTimeInMillis(), -2));
  }

  @Test
  public void testgetDateObjectsWhenDurationIsMinus3For1MIN() {
    assertEquals(
        oneMinWithDurationtionMinus3,
        testObject.getDateObjects(StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_1_MIN,
            latest1Min.getTimeInMillis(), -3));
  }

  /*
   * @Test public void teststartReAggSetsThrowsServerException() throws
   * ParseException, ServerException, RemoteException {
   * 
   * try{ testObject.startReAggSets(techPackName,new
   * Date(day.getTimeInMillis()).toString(),new
   * Date(day.getTimeInMillis()).toString(),
   * StubbedReAggregationServletEniqEvents
   * .RE_AGG_NAME_FOR_DAY,"ReAggregateNow",ctx);
   * 
   * } catch (ServerException Se) { assertTrue("Catched Server Seception",
   * true); } assertEquals( oneMinWithDurationtionMinus3,
   * testObject.getDateObjects
   * (StubbedReAggregationServletEniqEvents.RE_AGG_NAME_FOR_1_MIN,
   * latest1Min.getTimeInMillis(), -3)); }
   */

  @Override
  @After
  public void tearDown() {
    testObject = null;
  }

  public static void main(String args[]) {
    TestCase test = new ReAggregationServletEniqEventsTest("testreadInfoFromFileNormalCase");

    test.run();

  }

  private class StubbedReAggregationServletEniqEvents extends ReAggregationServletEniqEvents {

    @Override
    protected String getEnvEntryFromHelper(final String variableName) {
      return "DAY,15MIN,1MIN";
    }

    @Override
    protected DbCalendar getCalendarWithSecondAndMilliSecondSetToZero() {

      final DbCalendar calendar = new DbCalendar();

      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      calendar.set(Calendar.MINUTE, 1);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.DATE, 1);
      calendar.set(Calendar.MONTH, Calendar.JANUARY);
      calendar.set(Calendar.YEAR, 2011);
      // returning a calendar for Jan 1 2011.We can test three test cases with
      // this object.
      // For Beginning of Month
      // For Beginning of Year
      return calendar;
    }

  }

}
