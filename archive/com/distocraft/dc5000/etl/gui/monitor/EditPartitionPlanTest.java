package com.distocraft.dc5000.etl.gui.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;

import javax.naming.Context;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogSystem;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.distocraft.dc5000.etl.gui.util.Helper;

import ssc.rockfactory.RockFactory;
import utils.MemoryDatabaseUtility;

public class EditPartitionPlanTest {

	public static final String TEST_APPLICATION = UtilTest.class.getName();
	
	private static final String TEST_VIEW_VM = "editPartitionPlan.vm";

	  private static RockFactory dwhrepRock;
	  
	  private static MockHttpServletRequest webmockRequest;

	  private static MockHttpSession webmockSession;

	  private MockHttpServletResponse webmockResponse;

	  private VelocityContext velocityContext;
	  
	  private Mockery context = new Mockery() {

	    {
	      setImposteriser(ClassImposteriser.INSTANCE);
	    }
	  };

	  private Context mockContext;

	  private Context mockSubContext;
	  
    @BeforeClass
	public static void setUpBeforeClass() throws java.lang.Exception {
    	
        final FileWriter fwriter = new FileWriter(TEST_VIEW_VM);
        try {
          final PrintWriter out = new PrintWriter(fwriter);
          try {
            out.println("<html></html>");
          } finally {
            out.close();
          }
        } finally {
          fwriter.close();
        }

        /* Creating ETLC property file */
        String folderName = System.getProperty("user.home") + File.separator;
        final String fileName = folderName + "ETLCServer.properties";
        final File ETLCConfFile = new File(fileName);
        ETLCConfFile.deleteOnExit();
        try {
          final PrintWriter pw = new PrintWriter(new FileWriter(ETLCConfFile));
          pw.println("ENGINE_DB_URL=" + MemoryDatabaseUtility.TEST_ETLREP_URL);
          pw.println("ENGINE_DB_USERNAME=" + MemoryDatabaseUtility.TESTDB_USERNAME);
          pw.println("ENGINE_DB_PASSWORD=" + MemoryDatabaseUtility.TESTDB_PASSWORD);
          pw.println("ENGINE_DB_DRIVERNAME=" + MemoryDatabaseUtility.TESTDB_DRIVER);
          pw.println("ENGINE_HOSTNAME=localhost");
          pw.close();
        } catch (Exception e) {
          e.printStackTrace();
        }

        System.setProperty("CONF_DIR", folderName);

        Velocity.setProperty(Velocity.RESOURCE_LOADER, "file");
        Velocity.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogSystem.class.getName());
        Velocity.init();
			    
		dwhrepRock = new RockFactory(MemoryDatabaseUtility.TEST_DWHREP_URL, MemoryDatabaseUtility.TESTDB_USERNAME,
	            MemoryDatabaseUtility.TESTDB_PASSWORD, MemoryDatabaseUtility.TESTDB_DRIVER, TEST_APPLICATION, true);
	    final URL dwhrepsqlurl = ClassLoader.getSystemResource(MemoryDatabaseUtility.TEST_DWHREP_BASIC);
	    if (dwhrepsqlurl == null) {
	        System.out.println("dwhrep script can not be loaded!");
	    } else {
	        MemoryDatabaseUtility.loadSetup(dwhrepRock, dwhrepsqlurl);
	    }
	    
}
    
    @Before
    public final void setUp() throws Exception {

      webmockSession = new MockHttpSession(null);

      webmockRequest = new MockHttpServletRequest();

      webmockRequest.setSession(webmockSession);
        
      webmockResponse = new MockHttpServletResponse();

      velocityContext = new VelocityContext();

      mockContext = context.mock(Context.class, "context");

      mockSubContext = context.mock(Context.class, "subcontext");
      
      Helper.setContext(mockContext);
     
    }
	
	@AfterClass
	public static void tearDownAfterClass() throws java.lang.Exception {
	    
		final File f = new File(TEST_VIEW_VM);
	    if (!f.delete()) {
	      fail("failed to remove test template file");
	    }
		
	    try {
			MemoryDatabaseUtility.shutdown(dwhrepRock);
			}
		
		catch(Exception e){
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testgetPartitionPlan() throws Exception {
		
	  final Method m = EditPartitionPlan.class.getDeclaredMethod("getPartitionPlan", String.class, RockFactory.class);
      m.setAccessible(true);
      m.invoke(new EditPartitionPlan(), "extralarge_plain", dwhrepRock);
      }
	
	@Test
	public void testgetPartitionPlan1() throws Exception {
		
	  final Method m = EditPartitionPlan.class.getDeclaredMethod("getPartitionPlan", String.class, RockFactory.class);
      m.setAccessible(true);
      m.invoke(new EditPartitionPlan(), "large", dwhrepRock);
      }
	
	@Test
	public void testgetMaxStorageTime() throws Exception {
		
	  final Method m = EditPartitionPlan.class.getDeclaredMethod("getMaxStorageTime", String.class, RockFactory.class);
      m.setAccessible(true);
      final String expected = "30";
      final String actual = (m.invoke(new EditPartitionPlan(), "extralarge_plain", dwhrepRock)).toString();
      assertEquals(expected, actual);
      }
	
	@Test
	public void testgetMaxStorageTime1() throws Exception {
		
	  final Method m = EditPartitionPlan.class.getDeclaredMethod("getMaxStorageTime", String.class, RockFactory.class);
      m.setAccessible(true);
      final String expected = "";
      final String actual = (m.invoke(new EditPartitionPlan(), "larrge", dwhrepRock)).toString();
      assertEquals(expected, actual);
      }
	
	 @Test
	  public void testDoHandleRequest() throws Exception {

	    context.checking(new Expectations() {

	      {
	        allowing(mockContext).lookup(with(any(String.class)));
	        will(returnValue(mockSubContext));
	        allowing(mockSubContext).lookup("conffiles");
	        will(returnValue("."));


	      }
	    });
	    
	    webmockRequest = new MockHttpServletRequest("GET", "/servlet/StatusDetails");
	    webmockRequest.setParameter("ds", "rockDwhDba");
	    webmockRequest.setParameter("pp", "extralarge_plain");
	    velocityContext.put("rockDwhRep",dwhrepRock);
	    try {
	       
	      EditPartitionPlan servlet = new EditPartitionPlan();
	      servlet.doHandleRequest(webmockRequest, webmockResponse, velocityContext);
	    }
	    catch (Exception e) {
	    	String actual = null;
	      assertEquals(e.getMessage(), actual);
	    }
	    
	  }

	
}