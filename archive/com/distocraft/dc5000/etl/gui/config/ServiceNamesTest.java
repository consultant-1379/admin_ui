/**
 * 
 */
package com.distocraft.dc5000.etl.gui.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author eheijun
 *
 */
public class ServiceNamesTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    File homeDir = new File(System.getProperty("user.dir"));
    /* Creating static property file */
    File sn = new File(homeDir, "service_names");
    try {
      PrintWriter pw = new PrintWriter(new FileWriter(sn));
      pw.println("159.10.220.3::eniqserver::dwhdb");
      pw.println("159.10.220.3::eniqserver::repdb");
      pw.println("159.10.220.2::ecserver1::ec_1");
      pw.println("159.10.220.1::ecserver2::ec_2");
      pw.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    File homeDir = new File(System.getProperty("user.dir"));
    /* Deleting static property file */
    File sn = new File(homeDir, "service_names");
    if (!sn.delete()) {
      fail("Failed to remove service_names file");
    }
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.config.ServiceNames#getHosts()}.
   */
  @Test
  public void testGetHosts() {
    ServiceNames serviceNames = new ServiceNames("service_names");
    Set<String> hosts = serviceNames.getHosts();
    assertThat(hosts.size(), is(3));
    assertThat(hosts.contains("eniqserver"), is(true));
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.config.ServiceNames#getServices()}.
   */
  @Test
  public void testGetServices() {
    ServiceNames serviceNames = new ServiceNames("service_names");
    Set<String> services = serviceNames.getServices();
    assertThat(services.size(), is(4));
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.config.ServiceNames#getECs()}.
   */
  @Test
  public void testGetECs() {
    ServiceNames serviceNames = new ServiceNames("service_names");
    Set<String> ecs = serviceNames.getECs();
    assertThat(ecs.size(), is(2));
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.config.ServiceNames#getHostToServiceMap()}.
   */
  @Test
  public void testGetHostToServiceMap() {
    ServiceNames serviceNames = new ServiceNames("service_names");
    Map<String, String> serviceMap = serviceNames.getHostToServiceMap();
    assertThat(serviceMap.size(), is(3));
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.config.ServiceNames#getHostToServiceMap()}.
   */
  @Test
  public void testGetFirstServiceName() {
    ServiceNames serviceNames = new ServiceNames("service_names");
    String service = serviceNames.getFirstServiceName("eniqserver");
    assertThat(service, notNullValue());
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.config.ServiceNames#getHostToServiceMap()}.
   */
  @Test
  public void testGetFirstServiceNameFail() {
    ServiceNames serviceNames = new ServiceNames("service_names");
    String service = serviceNames.getFirstServiceName("none");
    assertThat(service, nullValue());
  }

}
