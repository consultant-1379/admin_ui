package com.distocraft.dc5000.etl.gui.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ssc.rockfactory.RockFactory;
import utils.MemoryDatabaseUtility;

public class MonitoredTypesTest {
	
	public static Connection con1 =null;
	public static Connection con2 =null;
	public static Connection con3 =null;
	public static Connection con4 =null;
	public static Statement stmt1;
	public static Statement stmt2;
	
	private static RockFactory dwhRock;
	private static RockFactory dwhrepRock;
	
@BeforeClass
public static void setUpBeforeClass() throws Exception {
	
	final String TEST_APPLICATION = MonitoredTypes.class.getName();
	
	dwhRock = new RockFactory(MemoryDatabaseUtility.TEST_DWH_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
            MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL dwhsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWH_BASIC);
    if (dwhsqlurl == null) {
        System.out.println("dwh script can not be loaded!");
    } else {
        MemoryDatabaseUtility.loadSetup(dwhRock, dwhsqlurl);
    }
    
	dwhrepRock = new RockFactory(MemoryDatabaseUtility.TEST_DWHREP_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
            MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
    final URL dwhrepsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWHREP_BASIC);
    if (dwhsqlurl == null) {
        System.out.println("dwh script can not be loaded!");
    } else {
        MemoryDatabaseUtility.loadSetup(dwhrepRock, dwhrepsqlurl);
    }
	
}

@AfterClass
public static void tearDownAfterClass() throws Exception {

	try {
		MemoryDatabaseUtility.shutdown(dwhRock);
		MemoryDatabaseUtility.shutdown(dwhrepRock);
		}
	catch(Exception e){
		fail(e.getMessage());
	}
}

@Test
public void testupdateMonitoredType() throws Exception {
	try {
  final Method m = MonitoredTypes.class.getDeclaredMethod("updateMonitoredType", Connection.class, String.class, String.class, String.class, String.class);
  m.setAccessible(true);
  m.invoke(new MonitoredTypes(), dwhRock.getConnection(), "DC_E_BSS_TRC_RAW", "15MIN", "ACTIVE", "2011-04-08 06:00:00.0");
	}
	catch (Exception e) {
		  assertEquals(e.getMessage(), null);
	}  
}

@Test
public void testupdateMonitoredTypeStatus() throws Exception {
	try {
  final Method m = MonitoredTypes.class.getDeclaredMethod("updateMonitoredTypeStatus", Connection.class, String.class, String.class, String.class);
  m.setAccessible(true);
  m.invoke(new MonitoredTypes(), dwhRock.getConnection(), "DC_E_BSS_TRC_RAW", "15MIN", "INACTIVE");
	}
	catch (Exception e) {
		  assertEquals(e.getMessage(), null);
	}  
}

@Test
public void testgetpartionCountForLog_LoadStatus() throws Exception {
  final Method m = MonitoredTypes.class.getDeclaredMethod("getpartionCountForLog_LoadStatus", Connection.class);
  m.setAccessible(true);
  final int data = (Integer) m.invoke(new MonitoredTypes(), dwhrepRock.getConnection());
  assertEquals(data, 5);
}

@Test
public void testdeleteMonitoredType() throws Exception {

  final Method m = MonitoredTypes.class.getDeclaredMethod("deleteMonitoredType", Connection.class, Connection.class, String.class, String.class);
  m.setAccessible(true);
  m.invoke(new MonitoredTypes(), dwhRock.getConnection(), dwhrepRock.getConnection(), "DC_E_BSS_TRC_RAW", "15MIN");

}
}