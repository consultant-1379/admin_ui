package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class InstallFeatureTest {
	@Test
	public void isAdminExisttest(){
		try {
			InstallFeatures instance = new InstallFeatures();
			Method isAdminExist = InstallFeatures.class.getDeclaredMethod("isAdminExist", null);
			isAdminExist.setAccessible(true);
			isAdminExist.invoke(instance, null);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
