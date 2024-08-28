package com.distocraft.dc5000.etl.gui.enminterworking;


import org.junit.*;

import com.distocraft.dc5000.etl.gui.enminterworking.FlsGranularityConfiguration;
import com.distocraft.dc5000.etl.gui.etl.INTFPollingDelayConf;
import com.ericsson.eniq.common.testutilities.DatabaseTestUtils;

import java.lang.reflect.Method;
import java.sql.*;

import ssc.rockfactory.RockFactory;

public class INTFPollingDelayConfTest {
	public INTFPollingDelayConf testInstance;
	RockFactory rf ;
	Connection con = null;
	
	@Before
	public void setUP() {
			try {
				rf = DatabaseTestUtils.getTestDbConnection();
				testInstance = new INTFPollingDelayConf();
				con = rf.getConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}

	}
	
	@Test
	public void test()  {
		try {
		Method m = INTFPollingDelayConf.class.getDeclaredMethod("closeConn",Connection.class);
		m.setAccessible(true);
		m.invoke(testInstance,con);
		con=null;
		m.invoke(testInstance, con);
		}catch(Exception e) {
			e.getMessage();
		}
	}
}
