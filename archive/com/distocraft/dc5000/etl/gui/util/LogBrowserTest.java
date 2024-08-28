/**
 * 
 */
package com.distocraft.dc5000.etl.gui.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.distocraft.dc5000.etl.gui.util.LogBrowser.LogException;

/**
 * @author eninkar
 *
 */
public class LogBrowserTest {
	
	private static File homeDir;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		try {
			  homeDir = new File(System.getProperty("user.dir"));
			
			File f=new File(homeDir,"engine-2010_10_25.log");
			f.deleteOnExit();
		      PrintWriter pw = new PrintWriter(new FileWriter(f));
		      pw.print("25.10 23:50:10 79341 INFO etl.INTF_DC_E_RBS-eniq_oss_1.Adapter.Adapter_INTF_DC_E_RBS_mdc.parser : Found open measurementFile: MeasurementFile /eniq/data/etldata/adapter_tmp/rbs/INTF_DC_E_RBS-eniq_oss_1/DC_E_RBS_ULBASEBANDPOOL_V_2010102523 type DC_E_RBS_ULBASEBANDPOOL_V\n");
		      pw.print("25.10 23:50:10 79331 INFO etl.INTF_DC_E_RBS-eniq_oss_1.Adapter.Adapter_INTF_DC_E_RBS_mdc.parser : Found open measurementFile: MeasurementFile /eniq/data/etldata/adapter_tmp/rbs/INTF_DC_E_RBS-eniq_oss_1/DC_E_RBS_ULBASEBANDPOOL_V_2010102523 type DC_E_RBS_ULBASEBANDPOOL_V\n");
		      pw.print("25.10 23:50:10 79335 INFO etl.INTF_DC_E_RBS-eniq_oss_1.Adapter.Adapter_INTF_DC_E_RBS_mdc.parser : Found open measurementFile: MeasurementFile /eniq/data/etldata/adapter_tmp/rbs/INTF_DC_E_RBS-eniq_oss_1/DC_E_RBS_SCCPCH_V_2010102523 type DC_E_RBS_SCCPCH_V\n");
		      pw.print("25.10 23:50:10 79338 INFO etl.INTF_DC_E_RBS-eniq_oss_1.Adapter.Adapter_INTF_DC_E_RBS_mdc.parser : Found open measurementFile: MeasurementFile /eniq/data/etldata/adapter_tmp/rbs/INTF_DC_E_RBS-eniq_oss_1/DC_E_RBS_ULBASEBANDPOOL_V_2010102523 type DC_E_RBS_ULBASEBANDPOOL_V\n");
		      pw.print("25.10 23:50:10 79335 INFO etl.INTF_DC_E_RBS-eniq_oss_1.Adapter.Adapter_INTF_DC_E_RBS_mdc.parser : Found open measurementFile: MeasurementFile /eniq/data/etldata/adapter_tmp/rbs/INTF_DC_E_RBS-eniq_oss_1/DC_E_RBS_ULBASEBANDPOOL_V_2010102523 type DC_E_RBS_ULBASEBANDPOOL_V\n");
		      pw.print("25.10 23:50:11 79334 INFO etl.INTF_DC_E_RBS-eniq_oss_1.Adapter.Adapter_INTF_DC_E_RBS_mdc.parser : Found open measurementFile: MeasurementFile /eniq/data/etldata/adapter_tmp/rbs/INTF_DC_E_RBS-eniq_oss_1/DC_E_RBS_CARRIER_V_2010102523 type DC_E_RBS_CARRIER_V\n");
		      pw.print("25.10 23:50:11 79334 INFO etl.INTF_DC_E_RBS-eniq_oss_1.Adapter.Adapter_INTF_DC_E_RBS_mdc.parser : Found open measurementFile: MeasurementFile /eniq/data/etldata/adapter_tmp/rbs/INTF_DC_E_RBS-eniq_oss_1/DC_E_RBS_DLBASEBANDPOOL_V_2010102523 type DC_E_RBS_DLBASEBANDPOOL_V\n");
		      pw.print("25.10 23:50:11 79334 INFO etl.INTF_DC_E_RBS-eniq_oss_1.Adapter.Adapter_INTF_DC_E_RBS_mdc.parser : Found open measurementFile: MeasurementFile /eniq/data/etldata/adapter_tmp/rbs/INTF_DC_E_RBS-eniq_oss_1/DC_E_RBS_EDCHRESOURCES_V_2010102523 type DC_E_RBS_EDCHRESOURCES_V\n");
		      pw.print("25.10 23:50:11 79334 INFO etl.INTF_DC_E_RBS-eniq_oss_1.Adapter.Adapter_INTF_DC_E_RBS_mdc.parser : Found open measurementFile: MeasurementFile /eniq/data/etldata/adapter_tmp/rbs/INTF_DC_E_RBS-eniq_oss_1/DC_E_RBS_HSDSCHRES_V_2010102523 type DC_E_RBS_HSDSCHRES_V\n");
		      pw.print("25.10 23:50:11 79334 INFO etl.INTF_DC_E_RBS-eniq_oss_1.Adapter.Adapter_INTF_DC_E_RBS_mdc.parser : Found open measurementFile: MeasurementFile /eniq/data/etldata/adapter_tmp/rbs/INTF_DC_E_RBS-eniq_oss_1/DC_E_RBS_IUBDATASTREAMS_V_2010102523 type DC_E_RBS_IUBDATASTREAMS_V\n");
		      pw.print("25.10 23:50:11 79334 INFO etl.INTF_DC_E_RBS-eniq_oss_1.Adapter.Adapter_INTF_DC_E_RBS_mdc.parser : Found open measurementFile: MeasurementFile /eniq/data/etldata/adapter_tmp/rbs/INTF_DC_E_RBS-eniq_oss_1/DC_E_RBS_NODEBFUNCTION_V_2010102523 type DC_E_RBS_NODEBFUNCTION_V\n");
		      pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.distocraft.dc5000.etl.gui.util.LogBrowser#parseFolder(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testParseFolder() {
		String path = homeDir.getPath();
		   LogBrowser browser = new LogBrowser(path);
		    final String startTime = "2010-10-25 23:50:10.0";
		    final String endTime = "2010-10-25 23:50:11.0";
		    final String type = "Adapter";
		    final String tp = "INTF_DC_E_RBS-eniq_oss_1";
		    final String sn = "Adapter_INTF_DC_E_RBS_mdc";
		    List list = new LinkedList();
		    try {
		      list = browser.parseFolder(startTime,endTime,sn,tp,type);
		      System.out.println("list.size;;; "+ list.size());
		      Assert.assertEquals(11, list.size());
		    } catch (Exception e) {
		      System.out.println(e.getMessage());
		    }
	}
	/**
	 * Test method for parseFolder() which handles exception
	 * @throws Exception
	 */
	@Test 
	public void testParseFolderException() throws Exception
	{
		String path = homeDir.getPath();
		LogBrowser browser = new LogBrowser(path);
		Exception thrownException = null;
		try
	    {
		Class c=browser.getClass();
		Field logDir=c.getDeclaredField("logDir");
		logDir.setAccessible(true);
		System.out.println("value of private variable logDir: " +logDir.get(browser));
		
		// setting a new value to the private variable
		logDir.set(browser,null);
		System.out.println("Valuse of the private variable after set:"+logDir.get(browser));
		final String startTime = "2011-02-14 23:50:10.0";
	    final String endTime = "2011-02-14 23:50:11.0";
	    final String setType = "Adapter";
	    final String techPackName = "INTF_DC_E_RBS-eniq_oss_1";
	    final String setName = "Adapter_INTF_DC_E_RBS_mdc";
	    
	    
	    // The parseFolder() returns exception as the logDir is set to null
	    List testList=browser.parseFolder(startTime, endTime, setName, techPackName, setType);
	    
	    }
	    catch(Exception e)
	    {
	    	thrownException=e;
	    }
		Assert.assertEquals(NullPointerException.class,thrownException.getClass());
	
	}

}
