package com.distocraft.dc5000.etl.gui.systemmonitor;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.distocraft.dc5000.common.ServertypesHelper;
import com.distocraft.dc5000.common.ServicenamesHelper;
import com.ericsson.eniq.common.testutilities.ServicenamesTestHelper;

public class LoaderStatusTest {

	private static final File TMPDIR = new File(System.getProperty("java.io.tmpdir"), "ServicenamesHelperTest");
	private static final Class[] populateServiceStatusListClasses = {List.class, MonitorInformation.class, String.class, Context.class};
	private static Method populateServiceStatusListMethod = null;
	private static Map<String, Map<String, String>> serviceToReaderOrWriterMap = new HashMap<String, Map<String, String>>();
	private MockedLoaderStatus testLoaderStatus = null;
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		System.setProperty("ETC_DIR", TMPDIR.getPath());
		System.setProperty("CONF_DIR", TMPDIR.getPath());
		ServicenamesTestHelper.setupEmpty(TMPDIR);
	}

	@Before
	public void setUp() throws SecurityException, NoSuchMethodException {
		populateServiceStatusListMethod = LoaderStatus.class.getDeclaredMethod("populateServiceStatusList", populateServiceStatusListClasses);
		populateServiceStatusListMethod.setAccessible(true);
		// clear server types
		ServertypesHelper.setServerTypeDetails(null);
	}

	@After
	public void tearDown() {
		// clear server types
		ServertypesHelper.setServerTypeDetails(null);
	}

	@Test
	public void testPopulateHostNameToTypeMappingStats() throws IOException {
		statsSetup();
		final Map<String, List<String>> hostToServiceMap = testLoaderStatus.getHostToServiceMap();
		// expected map {atrcxb2335=[dwh_reader_2], atrcxb2334=[engine], atrcxb2332=[dwhdb, licenceservice, repdb, scheduler, webserver], atrcxb2333=[dwh_reader_1]}
		assertEquals("Wrong number of mappings.", 4, hostToServiceMap.size());
		assertEquals("Wrong services for atrcxb2335.", 1, hostToServiceMap.get("atrcxb2335").size());
		assertEquals("Wrong services for atrcxb2334.", 1, hostToServiceMap.get("atrcxb2334").size());
		assertEquals("Wrong services for atrcxb2332.", 5, hostToServiceMap.get("atrcxb2332").size());
		assertEquals("Wrong services for atrcxb2333.", 1, hostToServiceMap.get("atrcxb2333").size());
	}

	@Test
	public void testPopulateHostNameToTypeMappingStatsSingle() throws IOException {
		statsSingleSetup();
		final Map<String, List<String>> hostToServiceMap = testLoaderStatus.getHostToServiceMap();
		// expected map {atrcxb2332=[dwh_reader_1, engine, dwhdb, licenceservice, repdb, scheduler, webserver]}
		assertEquals("Wrong number of mappings.", 1, hostToServiceMap.size());
		assertEquals("Wrong services for atrcxb2332.", 7, hostToServiceMap.get("atrcxb2332").size());
	}

	@Test
	public void testPopulateHostNameToTypeMappingEvents() throws IOException {
		eventsSetup();
		final Map<String, List<String>> hostToServiceMap = testLoaderStatus.getHostToServiceMap();
		/* expected map {atrcxb1336=[dwh_reader_3], atrcxb1731=[ec_2], 
		 *				 atrcxb1361=[controlzone, dwhdb, engine, ldapserver, licenceservice, repdb, webserver], 
		 *				 atrcxb1362=[glassfish], atrcxb1363=[dwh_reader_1], atrcxb1364=[dwh_reader_2], 
		 *				 atrcxb1365=[ec_1, ec_lteefa_1, ec_ltees_1, ec_ltees_2, ec_ltees_3, ec_ltees_4, ec_sgeh_1]}
		 */
		assertEquals("Wrong number of mappings.", 7, hostToServiceMap.size());
		assertEquals("Wrong services for atrcxb1336.", 1, hostToServiceMap.get("atrcxb1336").size());
		assertEquals("Wrong services for atrcxb1731.", 1, hostToServiceMap.get("atrcxb1731").size());
		assertEquals("Wrong services for atrcxb1361.", 7, hostToServiceMap.get("atrcxb1361").size());
		assertEquals("Wrong services for atrcxb1362.", 1, hostToServiceMap.get("atrcxb1362").size());
		assertEquals("Wrong services for atrcxb1365.", 7, hostToServiceMap.get("atrcxb1365").size());
	}
	
	@Test
	public void testPopulateServiceStatusListStats() throws IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// set up server list
		statsSetup();
		final Map<String, List<String>> hostToServiceMap = testLoaderStatus.getHostToServiceMap();

		// set up monitor info
		final MonitorInformation mi = new MonitorInformation();
		
		// call private populateServiceStatusList via reflection 
		Object[] argObjects = new Object[4];

		List<String> serviceMap = hostToServiceMap.get("atrcxb2332");
		argObjects[0] = serviceMap;
		argObjects[1] = mi;
		argObjects[2] = "atrcxb2332";
		argObjects[3] = new VelocityContext();

		populateServiceStatusListMethod.invoke(testLoaderStatus, argObjects);
		
		List serviceStatusList = testLoaderStatus.getServiceStatusList();
		// expected list: [{dwhdb=Online}, {licenceservice=Online}, {repdb=Online}, {scheduler=Online}, {webserver=Online}]
		assertEquals("Wrong size list returned.", 5, serviceStatusList.size());
		assertEquals("Wrong host name text.", "atrcxb2332 (<b>Co-ordinator</b>)", mi.getHostname());
		
		serviceMap = hostToServiceMap.get("atrcxb2333");
		argObjects[0] = serviceMap;
		argObjects[2] = "atrcxb2333";
		populateServiceStatusListMethod.invoke(testLoaderStatus, argObjects);
		serviceStatusList = testLoaderStatus.getServiceStatusList();
		//expected list: {dwh_reader_1=Online}
		assertEquals("Wrong size list returned.", 1, serviceStatusList.size());
		assertEquals("Wrong host name text.", "atrcxb2333 (<b>Reader</b>)", mi.getHostname());
		
		serviceMap = hostToServiceMap.get("atrcxb2334");
		argObjects[0] = serviceMap;
		argObjects[2] = "atrcxb2334";
		populateServiceStatusListMethod.invoke(testLoaderStatus, argObjects);
		serviceStatusList = testLoaderStatus.getServiceStatusList();
		//expected list: {engine=Online}
		assertEquals("Wrong size list returned.", 1, serviceStatusList.size());
		assertEquals("Wrong host name text.", "atrcxb2334 (<b>Engine</b>)", mi.getHostname());
	}

	@Test
	public void testPopulateServiceStatusListStatsSingle() throws IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// set up server list
		statsSingleSetup();
		final Map<String, List<String>> hostToServiceMap = testLoaderStatus.getHostToServiceMap();

		// set up monitor info
		final MonitorInformation mi = new MonitorInformation();
		
		// call private populateServiceStatusList via reflection 
		Object[] argObjects = new Object[4];

		List<String> serviceMap = hostToServiceMap.get("atrcxb2332");
		argObjects[0] = serviceMap;
		argObjects[1] = mi;
		argObjects[2] = "atrcxb2332";
		argObjects[3] = new VelocityContext();

		populateServiceStatusListMethod.invoke(testLoaderStatus, argObjects);
		
		List serviceStatusList = testLoaderStatus.getServiceStatusList();
		// expected list: [{engine=Online}, {dwhdb=Online}, {licenceservice=Online}, {repdb=Online}, {scheduler=Online}, {webserver=Online}]
		assertEquals("Wrong size list returned.", 6, serviceStatusList.size());
		assertEquals("Wrong host name text.", "atrcxb2332 (<b>Eniq Stats</b>)", mi.getHostname());
		
	}

	@Test
	public void testPopulateServiceStatusListEvents() throws IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// set up server list
		eventsSetup();
		final Map<String, List<String>> hostToServiceMap = testLoaderStatus.getHostToServiceMap();

		// set up monitor info
		final MonitorInformation mi = new MonitorInformation();
		
		// set up glassfish monitor info
		final MonitorInformation glassFishMi = new MonitorInformation();
		glassFishMi.setStatus(MonitorInformation.BULB_GREEN);
		final Context context = new VelocityContext();
		context.put(LoaderStatus.GLASSFISH_INFO,glassFishMi);
		
		// call private populateServiceStatusList via reflection 
		Object[] argObjects = new Object[4];

		List<String> serviceMap = hostToServiceMap.get("atrcxb1731");
		argObjects[0] = serviceMap;
		argObjects[1] = mi;
		argObjects[2] = "atrcxb1731";
		argObjects[3] = context;

		populateServiceStatusListMethod.invoke(testLoaderStatus, argObjects);
		
		List serviceStatusList = testLoaderStatus.getServiceStatusList();
		// expected list: {ec2=Offline}
		assertEquals("Wrong size list returned.", 1, serviceStatusList.size());
		assertEquals("Wrong host name text.", "atrcxb1731 (<b>Mediation Server</b>)", mi.getHostname());
		assertEquals("ec_2 should be offline", ServicesStatusStore.OFFLINE, testLoaderStatus.getServiceStatus("ec_2"));
		
		serviceMap = hostToServiceMap.get("atrcxb1361");
		argObjects[0] = serviceMap;
		argObjects[2] = "atrcxb1361";
		populateServiceStatusListMethod.invoke(testLoaderStatus, argObjects);
		serviceStatusList = testLoaderStatus.getServiceStatusList();
		//expected list: [{controlzone=Online}, {dwhdb=Online}, {engine=Online}, {ldapserver=Online}, {licenceservice=Online}, {repdb=Online}, {webserver=Online}]
		assertEquals("Wrong size list returned.", 7, serviceStatusList.size());
		assertEquals("Wrong host name text.", "atrcxb1361 (<b>Co-ordinator</b>)", mi.getHostname());
		assertEquals("controlzone should be online", ServicesStatusStore.ONLINE, testLoaderStatus.getServiceStatus("controlzone"));
		assertEquals("engine should be online", ServicesStatusStore.ONLINE, testLoaderStatus.getServiceStatus("engine"));
		assertEquals("webserver should be online", ServicesStatusStore.ONLINE, testLoaderStatus.getServiceStatus("webserver"));
		assertEquals("ldapserver should be online", ServicesStatusStore.ONLINE, testLoaderStatus.getServiceStatus("ldapserver"));
		
		serviceMap = hostToServiceMap.get("atrcxb1362");
		argObjects[0] = serviceMap;
		argObjects[2] = "atrcxb1362";
		populateServiceStatusListMethod.invoke(testLoaderStatus, argObjects);
		serviceStatusList = testLoaderStatus.getServiceStatusList();
		//expected list: [{glassfish=Online}]
		assertEquals("Wrong size list returned.", 1, serviceStatusList.size());
		assertEquals("Wrong host name text.", "atrcxb1362 (<b>Presentation</b>)", mi.getHostname());
		assertEquals("glassfish should be online", ServicesStatusStore.ONLINE, testLoaderStatus.getServiceStatus("glassfish"));

		serviceMap = hostToServiceMap.get("atrcxb1365");
		argObjects[0] = serviceMap;
		argObjects[2] = "atrcxb1365";
		populateServiceStatusListMethod.invoke(testLoaderStatus, argObjects);
		serviceStatusList = testLoaderStatus.getServiceStatusList();
		//expected list: [{ec_1=Online}, {ec_lteefa_1=Online}, {ec_ltees_1=Online}, {ec_ltees_2=Online}, {ec_ltees_3=Online}, {ec_ltees_4=Online}, {ec_sgeh_1=Online}]
		assertEquals("Wrong size list returned.", 7, serviceStatusList.size());
		assertEquals("Wrong host name text.", "atrcxb1365 (<b>Mediation Server</b>)", mi.getHostname());
		assertEquals("ec_1 should be online", ServicesStatusStore.ONLINE, testLoaderStatus.getServiceStatus("ec_1"));
		assertEquals("ec_ltees_2 should be online", ServicesStatusStore.ONLINE, testLoaderStatus.getServiceStatus("ec_ltees_2"));
	}
	
	private void statsSetup() throws IOException {
		/* create service_names file */
		BufferedWriter writer = new BufferedWriter(new FileWriter(ServicenamesHelper.getServicenamesFile(), false));
		writer.write("#\n");
		writer.write("# ENIQ service list\n");
		writer.write("# Format is:\n");
		writer.write("# <ip_address>::<hostname>::<service>::<service_group>\n");
		writer.write("#\n");
		writer.write("10.45.192.184::atrcxb2332::dwhdb\n");
		writer.write("10.45.192.184::atrcxb2332::licenceservice\n");
		writer.write("10.45.192.184::atrcxb2332::repdb\n");
		writer.write("10.45.192.184::atrcxb2332::scheduler\n");
		writer.write("10.45.192.184::atrcxb2332::webserver\n");
		writer.write("10.45.192.185::atrcxb2333::dwh_reader_1\n");
		writer.write("10.45.192.186::atrcxb2334::engine\n");
		writer.write("10.45.192.187::atrcxb2335::dwh_reader_2\n");
		writer.close();
		
		testLoaderStatus = new MockedLoaderStatus();
		testLoaderStatus.populateHostNameToTypeMapping();
		
		/* create server_types file */
		writer = new BufferedWriter(new FileWriter(ServertypesHelper.getServertypesFile(), false));
		writer.write("10.45.192.184::atrcxb2332::stats_coordinator\n");
		writer.write("10.45.192.185::atrcxb2333::stats_iqr\n");
		writer.write("10.45.192.186::atrcxb2334::stats_engine\n");
		writer.write("10.45.192.187::atrcxb2335::stats_iqr\n");
		writer.close();

		serviceToReaderOrWriterMap.clear();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("coordinator", "N/A");
		serviceToReaderOrWriterMap.put("dwhdb", map);
		map = new HashMap<String, String>();
		map.put("writer", "active");
		serviceToReaderOrWriterMap.put("dwh_reader_1", map);
		serviceToReaderOrWriterMap.put("dwh_reader_2", map);

		testLoaderStatus.setServiceToReaderOrWriterMap(serviceToReaderOrWriterMap);
	}

	private void statsSingleSetup() throws IOException {
		/* create service_names file */
		BufferedWriter writer = new BufferedWriter(new FileWriter(ServicenamesHelper.getServicenamesFile(), false));
		writer.write("#\n");
		writer.write("# ENIQ service list\n");
		writer.write("# Format is:\n");
		writer.write("# <ip_address>::<hostname>::<service>::<service_group>\n");
		writer.write("#\n");
		writer.write("10.45.192.184::atrcxb2332::dwh_reader_1\n");
		writer.write("10.45.192.184::atrcxb2332::dwhdb\n");
		writer.write("10.45.192.184::atrcxb2332::engine\n");
		writer.write("10.45.192.184::atrcxb2332::licenceservice\n");
		writer.write("10.45.192.184::atrcxb2332::repdb\n");
		writer.write("10.45.192.184::atrcxb2332::scheduler\n");
		writer.write("10.45.192.184::atrcxb2332::webserver\n");
		writer.close();
		
		testLoaderStatus = new MockedLoaderStatus();
		testLoaderStatus.populateHostNameToTypeMapping();
		
		/* create server_types file */
		writer = new BufferedWriter(new FileWriter(ServertypesHelper.getServertypesFile(), false));
		writer.write("10.45.192.184::atrcxb2332::eniq_stats\n");
		writer.close();

		serviceToReaderOrWriterMap.clear();

		testLoaderStatus.setServiceToReaderOrWriterMap(serviceToReaderOrWriterMap);
	}

	private void eventsSetup() throws IOException {
		/* create service_names file */
		BufferedWriter writer = new BufferedWriter(new FileWriter(ServicenamesHelper.getServicenamesFile(), false));
		writer.write("#\n");
		writer.write("# ENIQ service list\n");
		writer.write("# Format is:\n");
		writer.write("# <ip_address>::<hostname>::<service>::<service_group>\n");
		writer.write("#\n");
		writer.write("10.44.95.39::atrcxb1361::controlzone\n");
		writer.write("10.44.95.41::atrcxb1363::dwh_reader_1\n");
		writer.write("10.44.95.42::atrcxb1364::dwh_reader_2\n");
		writer.write("10.44.95.45::atrcxb1336::dwh_reader_3\n");
		writer.write("10.44.95.39::atrcxb1361::dwhdb\n");
		writer.write("10.44.95.43::atrcxb1365::ec_1\n");
		writer.write("10.44.95.137::atrcxb1731::ec_2\n");
		writer.write("10.44.95.43::atrcxb1365::ec_lteefa_1\n");
		writer.write("10.44.95.43::atrcxb1365::ec_ltees_1\n");
		writer.write("10.44.95.43::atrcxb1365::ec_ltees_2\n");
		writer.write("10.44.95.43::atrcxb1365::ec_ltees_3\n");
		writer.write("10.44.95.43::atrcxb1365::ec_ltees_4\n");
		writer.write("10.44.95.43::atrcxb1365::ec_sgeh_1\n");
		writer.write("10.44.95.39::atrcxb1361::engine\n");
		writer.write("10.44.95.40::atrcxb1362::glassfish\n");
		writer.write("10.44.95.39::atrcxb1361::ldapserver\n");
		writer.write("10.44.95.39::atrcxb1361::licenceservice\n");
		writer.write("10.44.95.39::atrcxb1361::repdb\n");
		writer.write("10.44.95.39::atrcxb1361::webserver\n");
		writer.close();

		// set up ecStatus 
		ECStatus.add("\nControl Zone is ");
		ECStatus.add("\nEC1 is ");
		ECStatus.add("\nEC2 is not ");
		ECStatus.add("\nec_lteefa_1 is ");
		ECStatus.add("\nec_ltees_1 is ");
		ECStatus.add("\nec_ltees_2 is ");
		ECStatus.add("\nec_ltees_3 is ");
		ECStatus.add("\nec_ltees_4 is ");
		ECStatus.add("\nEC_SGEH_1 is ");
		
		
		testLoaderStatus = new MockedLoaderStatus();
		testLoaderStatus.populateHostNameToTypeMapping();

		/* create server_types file */
		writer = new BufferedWriter(new FileWriter(ServertypesHelper.getServertypesFile(), false));
		writer.write("10.44.95.137::atrcxb1731::eniq_mz\n");
		writer.write("10.44.95.39::atrcxb1361::eniq_coordinator\n");
		writer.write("10.44.95.40::atrcxb1362::eniq_ui\n");
		writer.write("10.44.95.41::atrcxb1363::eniq_iqr\n");
		writer.write("10.44.95.42::atrcxb1364::eniq_iqr\n");
		writer.write("10.44.95.43::atrcxb1365::eniq_mz\n");
		writer.write("10.44.95.45::atrcxb1336::eniq_iqr\n");
		writer.close();

		serviceToReaderOrWriterMap.clear();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("coordinator", "N/A");
		serviceToReaderOrWriterMap.put("dwhdb", map);
		map = new HashMap<String, String>();
		map.put("writer", "active");
		serviceToReaderOrWriterMap.put("dwh_reader_1", map);
		serviceToReaderOrWriterMap.put("dwh_reader_2", map);
		serviceToReaderOrWriterMap.put("dwh_reader_3", map);
		
		testLoaderStatus.setServiceToReaderOrWriterMap(serviceToReaderOrWriterMap);
	}	
	
	class MockedLoaderStatus extends LoaderStatus {
		/**
		 * Getter for hostToServiceMap. Added for testing purposes.
		 * 
		 * @return mapping of hostname to list of services defined for this host
		 * in the service_names file
		 */
		public Map<String, List<String>> getHostToServiceMap() {
			return hostToServiceMap;
		}

		/**
		 * Setter for hostToServiceMap. Added for testing purposes.
		 * 
		 * @param hostToServiceMap mapping of hostname to list of services defined for this host
		 */
		public void setHostToServiceMap(Map<String, List<String>> hostToServiceMap) {
			this.hostToServiceMap = hostToServiceMap;
		}

		/**
		 * Setter for serviceToReaderOrWriterMap. Added for testing purposes
		 * @param serviceToReaderOrWriterMap
		 */
		public void setServiceToReaderOrWriterMap(final Map<String, Map<String, String>> serviceToReaderOrWriterMap) {
			this.serviceToReaderOrWriterMap = serviceToReaderOrWriterMap;
		}

		/** 
		 * Getter for serviceStatusList. Added for testing purposes.
		 * 
		 * @return service status list
		 */
		public List getServiceStatusList() {
			return serviceStatusList;
		}

		public String getServiceStatus(final String service) {
			String status = null;
			
			for (Object serviceStatus : serviceStatusList) {
				final Map<String, String> serviceStatusMap = (Map<String, String>)serviceStatus;
				if (serviceStatusMap.containsKey(service)) {
					status = serviceStatusMap.get(service);
					break;
				}
			}
			
			return status;
		}
	}

}
