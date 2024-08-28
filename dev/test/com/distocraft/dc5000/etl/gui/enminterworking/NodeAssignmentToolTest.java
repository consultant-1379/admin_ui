package com.distocraft.dc5000.etl.gui.enminterworking;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.velocity.context.Context;
import org.junit.Before;
import org.junit.Test;

import com.distocraft.dc5000.etl.gui.etl.INTFPollingDelayConf;
import com.ericsson.eniq.common.testutilities.DatabaseTestUtils;

import ssc.rockfactory.RockFactory;

public class NodeAssignmentToolTest {
	
	Connection conn = null;
	RockFactory rockFactory = null;
	public NodeAssignmentTool testInstance;

	@Before
	public void setUP() {
			try {
				rockFactory = DatabaseTestUtils.getTestDbConnection();
				testInstance = new NodeAssignmentTool();
				conn = rockFactory.getConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}

	}
	
	@Test
	public void closeconnTest() {
		try {
			Method m = NodeAssignmentTool.class.getDeclaredMethod("closeConn",Connection.class);
			m.setAccessible(true);
			m.invoke(testInstance, conn);
					//NodeAssignmentTool.closeConn(conn);
		}catch (Exception e) {
			e.getMessage();
		}
	}
	

	@Test
	public void closeconnTest2() {
		try {
			Method m = NodeAssignmentTool.class.getDeclaredMethod("closeConn",Connection.class);
			m.setAccessible(true);
			conn = null;
			m.invoke(testInstance, conn);
		}catch (Exception e) {
			e.getMessage();
		}
	}
}
