package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.lang.reflect.Field;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class DatabaseInfoTest {

	private Field infoField;
	
	@Test
	public void testParseSQLAnywhereSize(){
		try {
			infoField = DatabaseInfo.class.getDeclaredField("info");
			infoField.setAccessible(true);

			String name = "ENIQ REP";
			DatabaseInfo dbInfo = new DatabaseInfo(name);
			
			String totalSpace   = "2149347328";
      String freeSpace   = "2117021696";
			String version      = "11.0.1.2355";
			String backuptime   = "2010-11-14 08:02:33";
			dbInfo.parseSQLAnywhereSize(version, totalSpace, freeSpace, backuptime);
			
			StringBuffer buff = (StringBuffer)infoField.get(dbInfo);
			String expected  = "SQL Anywhere Version: 11.0.1.2355.<br />Total database size: 2050 MB.<br />Used space: 31 MB (2%). Available 2019 MB.<br />";
			assertEquals(expected, buff.toString());

		} catch (SecurityException e) {
			fail("testParseSize Failed to start: "+e);
		} catch (NoSuchFieldException e) {
			fail("testParseSize Failed to start: "+e);
		} catch (IllegalArgumentException e) {
			fail("testParseSize Failed to start: "+e);
		} catch (IllegalAccessException e) {
			fail("testParseSize Failed to start: "+e);
		}
	}

	@Test
	public void testParseSQLAnywhereSizeBackupTimeEmpty(){
		try {
			infoField = DatabaseInfo.class.getDeclaredField("info");
			infoField.setAccessible(true);

      String name = "ENIQ REP";
			DatabaseInfo dbInfo = new DatabaseInfo(name);
			
			String totalSpace   = "203958272";
      String freeSpace   = "1732608";
			String version      = "11.0.1.2355";
			String backuptime   = "";
			dbInfo.parseSQLAnywhereSize(version, totalSpace, freeSpace, backuptime);
			
			StringBuffer buff = (StringBuffer)infoField.get(dbInfo);
      String expected  = "SQL Anywhere Version: 11.0.1.2355.<br />Total database size: 195 MB.<br />Used space: 193 MB (99%). Available 2 MB.<br />";
			assertEquals(expected, buff.toString());

		} catch (SecurityException e) {
			fail("testParseSize Failed to start: "+e);
		} catch (NoSuchFieldException e) {
			fail("testParseSize Failed to start: "+e);
		} catch (IllegalArgumentException e) {
			fail("testParseSize Failed to start: "+e);
		} catch (IllegalAccessException e) {
			fail("testParseSize Failed to start: "+e);
		}
	}

}
