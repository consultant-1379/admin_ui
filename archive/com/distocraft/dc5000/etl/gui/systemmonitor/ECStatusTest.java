package com.distocraft.dc5000.etl.gui.systemmonitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ECStatusTest {

	@Before
	public void setUp() throws Exception {
		ECStatus.clear();
	}

	@After
	public void tearDown() throws Exception {
		ECStatus.clear();
	}

	@Test
	public void testAdd() {
		ECStatus.add("\nEC_SGEH_1 is ");
		ECStatus.add("\nControl Zone is ");
		ECStatus.add("\nEC2 is ");
		
		// ec name is added to map as lower case
		assertTrue("Can't find EC_SGEH_1 in ec status map.", ECStatus.getECStatusMap().containsKey("ec_sgeh_1"));
		assertTrue("Can't find Control Zone in ec status map.", ECStatus.getECStatusMap().containsKey("controlzone"));
		assertTrue("Can't find EC_2 in ec status map.", ECStatus.getECStatusMap().containsKey("ec_2"));
	}

	@Test
	public void testEcIsRunning() {
		ECStatus.add("\nEC_SGEH_1 is ");
		ECStatus.add("\nControl Zone is ");
		ECStatus.add("\nEC2 is not ");
		
		assertTrue("EC_SGEH_1 should be running.", ECStatus.ecIsRunning("EC_SGEH_1"));
		assertTrue("Control Zone should be running.", ECStatus.ecIsRunning("Control Zone"));
		assertTrue("Control Zone should be running.", ECStatus.ecIsRunning("controlzone")); // check standard spelling
		assertFalse("EC_2 should NOT be running.", ECStatus.ecIsRunning("EC_2"));
		assertFalse("EC_2 should NOT be running.", ECStatus.ecIsRunning("ec_2")); // case should be irrelevant
	}

	@Test
	public void testClear() {
		ECStatus.add("\nEC_SGEH_1 is ");
		ECStatus.add("\nControl Zone is ");
		ECStatus.add("\nEC2 is ");
		assertTrue("Nothing in ECStatus to start with.", ECStatus.getECStatusMap().size()>0);
	}
	
	@Test
	public void testECStandardNameTrue(){
		ECStatus.add("ec_1");
		ECStatus.add("ec_2");
		ECStatus.add("ec_3");
		ECStatus.add("ec_4");
		ECStatus.add("ec_5");
		ECStatus.add("ec_6");
		ECStatus.add("ec_7");
		ECStatus.add("ec_8");
		ECStatus.add("ec_9");
		assertEquals(true, ECStatus.isLoggedService("ec1"));
		assertEquals(true, ECStatus.isLoggedService("ec2"));
		assertEquals(true, ECStatus.isLoggedService("ec3"));
		assertEquals(true, ECStatus.isLoggedService("ec4"));
		assertEquals(true, ECStatus.isLoggedService("ec5"));
		assertEquals(true, ECStatus.isLoggedService("ec6"));
		assertEquals(true, ECStatus.isLoggedService("ec7"));
		assertEquals(true, ECStatus.isLoggedService("ec8"));
		assertEquals(true, ECStatus.isLoggedService("ec9"));
		
	}
	
	@Test
	public void testECStandardNameFalse(){
		ECStatus.add("ec_10");
		assertEquals(false, ECStatus.isLoggedService("ec10"));
		ECStatus.clear();
		assertTrue("ECStatus not cleared.", ECStatus.getECStatusMap().size()==0);
	}

}
