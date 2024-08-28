package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class PrecheckTest {
	@Test
	public void isAdminExisttest(){
		try {
			Precheck instance = new Precheck();
			Field cmd = Precheck.class.getDeclaredField("ADMIN_ROLE_EXIST_CMD");
			cmd.setAccessible(true);
			Method isAdminExist = Precheck.class.getDeclaredMethod("isAdminExist", null);
			isAdminExist.setAccessible(true);
			isAdminExist.invoke(instance, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
