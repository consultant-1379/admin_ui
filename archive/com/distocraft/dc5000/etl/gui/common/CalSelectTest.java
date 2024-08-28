package com.distocraft.dc5000.etl.gui.common;

import static org.junit.Assert.*;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;



public class CalSelectTest {

	private Connection conn;

	@Before
	public void setup(){

		conn = null;
		try {
			Class.forName("org.hsqldb.jdbcDriver").newInstance();
			conn = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "sa", "");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			final Statement stmt = conn.createStatement();

			stmt.execute("CREATE TABLE DIM_DATE (DATE_ID VARCHAR(31),YEAR_ID VARCHAR(31), "
					+ "MONTH_ID VARCHAR(31), DAY_ID VARCHAR(31))");
		} catch (SQLException e) {
			fail(e.getMessage());
		} 
	}
	
	@After
	public void tearDown(){
		try {
			final Statement stmt = conn.createStatement();

			stmt.execute("DROP TABLE DIM_DATE");
	    stmt.executeUpdate("SHUTDOWN");
      stmt.close();
		} catch (SQLException e) {
			fail(e.getMessage());
		} 
		
	}
	
	@Test
	public void getAllValidYearsFromDIM_DATEMultipleMatch(){

	    try {
		    final Statement stmt = conn.createStatement();
		    stmt.executeUpdate("INSERT INTO DIM_DATE VALUES"
	                + "('"+2000/-03/-04+"', '"+2000+"', '"+3+"', '"+4+"')");
		    stmt.executeUpdate("INSERT INTO DIM_DATE VALUES"
	                + "('"+2008/-03/-04+"', '"+2008+"', '"+3+"', '"+4+"')");
		    stmt.executeUpdate("INSERT INTO DIM_DATE VALUES"
	                + "('"+2001/-03/-04+"', '"+2001+"', '"+3+"', '"+4+"')");
		} catch (SQLException e) {
			fail(e.getMessage());
		}
		CalSelect calSelect = new CalSelect(conn);
		
		List<String> v = calSelect.getYearRange();
		String[] expected = new String[]{"2000", "2001", "2008"};

		  String[] actual = null;
		  actual = new String[v.size()];
		  v.toArray(actual);

		assertArrayEquals(expected, actual);
	}

	@Test
	public void getAllValidYearsFromDIM_DATEOneMatch(){

		try {
			final Statement stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO DIM_DATE VALUES"
					+ "('"+2000/-03/-04+"', '"+2000+"', '"+3+"', '"+4+"')");
		} catch (SQLException e) {
			fail(e.getMessage());
		}
		CalSelect calSelect = new CalSelect(conn);
		
		List<String> v = calSelect.getYearRange();
		assertEquals(1, v.size());
		Vector<String> exp = new Vector<String>();
		exp.add("2000");
		
		assertEquals(exp, v);
	}

	@Test
	public void getFutureRangeFromThisYear(){
		CalSelect calSelect = new CalSelect(conn);
		List<String> actual = calSelect.getFutureRange("2009");
		Vector<String> expected = new Vector<String>(6);
		expected.add("2009");
		expected.add("2010");
		expected.add("2011");
		expected.add("2012");
		expected.add("2013");
		expected.add("2014");
		assertEquals(expected, actual);
	}
}
