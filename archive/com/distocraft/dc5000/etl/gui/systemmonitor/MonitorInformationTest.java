package com.distocraft.dc5000.etl.gui.systemmonitor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MonitorInformationTest {

	MonitorInformation MoniInfo = new MonitorInformation();
	
	@Test
	public void testSetAndFetchStatus() throws Exception {
	MoniInfo.setStatus(0);
	final int expected = 0;
	final int actual = MoniInfo.getStatus();
	assertEquals(expected,actual);
	}
	
	@Test
	public void testSetAndFetchDetailUrl() throws Exception {
		MoniInfo.setDetailUrl("http://");
		final String expected = "http://";
		final String actual = MoniInfo.getDetailUrl();
		assertEquals(expected,actual);
	}
	
	@Test
	public void testSetAndGetFieldName() throws Exception {
		MoniInfo.setFieldName("Techpack");
		final String expected = "Techpack";
		final String actual = MoniInfo.getFieldName();
		assertEquals(expected,actual);
	}
	
	@Test
	public void testSetAndGetMessage() throws Exception {
		MoniInfo.setMessage("No of Rows ");
		final String expected = "- No of Rows <br />";
		final String actual = MoniInfo.getMessage();
		assertEquals(expected,actual);
	}
	 
	@Test
	public void testSetAndGetWarningText() throws Exception {
		MoniInfo.setWarning(true,"Max rows exceeded ");
		final Boolean expected1 = true;
		final Boolean actual1 = MoniInfo.isWarning();
		assertEquals(expected1,actual1);
		
		final String expected2 = "Max rows exceeded <br />";
		final String actual2 = MoniInfo.getWarningText();
		assertEquals(expected2,actual2);
	}
	
	@Test
	public void testSetAndGetCurrProfile() throws Exception {
		MoniInfo.setCurrProfile("Profile:NoLoads");
		final String expected1 = "Profile:NoLoads";
		final String actual1 = MoniInfo.getCurrProfile();
		assertEquals(expected1,actual1);
		
		final String expected2 = "NoLoads";
		final String actual2 = MoniInfo.getCurrProfileName();
		assertEquals(expected2,actual2);
	}
	
/*	@Test
	public void testtransformBytesToMegas() throws Exception {
		final String expected = "1";
		final String actual = MoniInfo.transformBytesToMegas("Total Memory: 11288577");
        assertEquals(expected,actual);
	}
*/	
	
	
}