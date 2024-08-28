/**
 * 
 */
package com.distocraft.dc5000.etl.gui.afjupgrade;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess.State;
import com.ericsson.eniq.afj.AfjManagerFactory;
import com.ericsson.eniq.afj.common.AFJDelta;
import com.ericsson.eniq.afj.common.AFJTechPack;
import com.ericsson.eniq.afj.common.EngineAdminFactory;

/**
 * @author eheijun
 * 
 */
public class AfjManagerProcessTest {

  private MockAfjManager mockAfjManager;

  private MockEngineAdmin mockEngineAdmin;
  
  private static class TestThreadFactory implements ThreadFactory {

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread(final Runnable r) {
      final Thread t = new Thread(r);
      t.setDaemon(true);
      return t;
    }

  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    mockAfjManager = new MockAfjManager();
    MockAfjManager.mockFail = false;
    mockAfjManager.installAFJTechPack();
    AfjManagerFactory.setInstance(mockAfjManager);
    mockEngineAdmin = new MockEngineAdmin();
    EngineAdminFactory.setInstance(mockEngineAdmin);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    AfjManagerFactory.setInstance(null);
    EngineAdminFactory.setInstance(null);
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess#AfjManagerProcess()}.
   */
  @Test
  public void testAfjManagerProcess() {
    final AfjManagerProcess afjmanagerProcess = new AfjManagerProcess(
        Executors.newSingleThreadExecutor(new TestThreadFactory()));
    assertTrue(afjmanagerProcess.getProcessState() == State.NONE);
    assertTrue(afjmanagerProcess.getStatusMessage().equals(""));
    assertFalse(afjmanagerProcess.isRunning());
    assertNull(afjmanagerProcess.getSelectedTechPack());
    assertNull(afjmanagerProcess.getAFJDelta());
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess#getAFJTechPackList()}.
   */
  @Test
  public void testGetAFJTechPackList() {
    final AfjManagerProcess afjmanagerProcess = new AfjManagerProcess(
        Executors.newSingleThreadExecutor(new TestThreadFactory()));
    mockAfjManager.installAFJTechPack();
    final List<AFJTechPack> techPackList = afjmanagerProcess.getAFJTechPackList();
    assertFalse(afjmanagerProcess.isErrorState());
    assertNotNull(techPackList);
    assertTrue(techPackList.size() == 4);
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess#getAFJTechPackList()}.
   */
  @Test
  public void testGetAFJTechPackListConfigurationFail() {
    final AfjManagerProcess afjmanagerProcess = new AfjManagerProcess(
        Executors.newSingleThreadExecutor(new TestThreadFactory()));
    mockAfjManager.uninstallAFJTechPack();
    final List<AFJTechPack> techPackList = afjmanagerProcess.getAFJTechPackList();
    assertTrue(afjmanagerProcess.isErrorState());
    assertNull(techPackList);
    assertTrue(afjmanagerProcess.getProcessState().equals(State.TECHPACK_ERROR));
    assertFalse(afjmanagerProcess.getStatusMessage().equals(""));
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess#getAFJTechPackList()}.
   */
  @Test
  public void testGetAFJTechPackListGeneralFail() {
    MockAfjManager.mockFail = true;
    final AfjManagerProcess afjmanagerProcess = new AfjManagerProcess(
        Executors.newSingleThreadExecutor(new TestThreadFactory()));
    final List<AFJTechPack> techPackList = afjmanagerProcess.getAFJTechPackList();
    assertTrue(afjmanagerProcess.isErrorState());
    assertNull(techPackList);
    assertTrue(afjmanagerProcess.getProcessState().equals(State.GENERAL_ERROR));
    assertFalse(afjmanagerProcess.getStatusMessage().equals(""));
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess#getAFJDelta()}.
   */
  @Test
  public void testGetEmptyAFJDelta() {
    final AfjManagerProcess afjmanagerProcess = new AfjManagerProcess(
        Executors.newSingleThreadExecutor(new TestThreadFactory()));
    AFJDelta delta = afjmanagerProcess.getAFJDelta();
    assertNull(delta);
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess#getAFJTechPackByName(java.lang.String)}.
   */
  @Test
  public void testGetNonExistingAFJTechPackByName() {
    final AfjManagerProcess afjmanagerProcess = new AfjManagerProcess(
        Executors.newSingleThreadExecutor(new TestThreadFactory()));
    AFJTechPack tp = afjmanagerProcess.getAFJTechPackByName("XYZ");
    assertNull(tp);
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess#upgradeAFJTechPackWithDelta()}.
   * 
   * @throws InterruptedException
   */
  @Test
  public void testUpgradeAFJTechPackWithDelta() throws InterruptedException {
    final AfjManagerProcess afjmanagerProcess = new AfjManagerProcess(
        Executors.newSingleThreadExecutor(new TestThreadFactory()));
    final AFJTechPack techPack = afjmanagerProcess.getAFJTechPackByName("DC_E_BSS");
    afjmanagerProcess.setSelectedTechPack(techPack.getTechPackName());
    afjmanagerProcess.generateAFJTechPackDelta();
    while (afjmanagerProcess.isRunning()) {
      assertFalse("".equals(afjmanagerProcess.getStatusMessage()));
      Thread.sleep(500);
    }
    assertFalse(afjmanagerProcess.isErrorState());
    afjmanagerProcess.upgradeAFJTechPackWithDelta();
    while (afjmanagerProcess.isRunning()) {
      assertFalse("".equals(afjmanagerProcess.getStatusMessage()));
      Thread.sleep(500);
    }
    assertFalse(afjmanagerProcess.isErrorState());
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess#upgradeAFJTechPackWithDelta()}.
   * 
   * @throws InterruptedException
   */
  @Test
  public void testUpgradeAFJTechPackWithFailDelta() throws InterruptedException {
    final AfjManagerProcess afjmanagerProcess = new AfjManagerProcess(
        Executors.newSingleThreadExecutor(new TestThreadFactory()));
    final AFJTechPack techPack = afjmanagerProcess.getAFJTechPackByName("DC_E_BSS");
    afjmanagerProcess.setSelectedTechPack(techPack.getTechPackName());
    MockAfjManager.mockFail = true;
    afjmanagerProcess.generateAFJTechPackDelta();
    while (afjmanagerProcess.isRunning()) {
      assertFalse("".equals(afjmanagerProcess.getStatusMessage()));
      Thread.sleep(500);
    }
    assertTrue(afjmanagerProcess.isErrorState());
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess#upgradeAFJTechPackWithDelta()}.
   * 
   * @throws InterruptedException
   */
  @Test
  public void testFailUpgradeAFJTechPackWithDelta() throws InterruptedException {
    final AfjManagerProcess afjmanagerProcess = new AfjManagerProcess(
        Executors.newSingleThreadExecutor(new TestThreadFactory()));
    final AFJTechPack techPack = afjmanagerProcess.getAFJTechPackByName("DC_E_BSS");
    afjmanagerProcess.setSelectedTechPack(techPack.getTechPackName());
    afjmanagerProcess.generateAFJTechPackDelta();
    while (afjmanagerProcess.isRunning()) {
      assertFalse("".equals(afjmanagerProcess.getStatusMessage()));
      Thread.sleep(500);
    }
    assertFalse(afjmanagerProcess.isErrorState());
    MockAfjManager.mockFail = true;
    afjmanagerProcess.upgradeAFJTechPackWithDelta();
    while (afjmanagerProcess.isRunning()) {
      assertFalse("".equals(afjmanagerProcess.getStatusMessage()));
      Thread.sleep(500);
    }
    assertTrue(afjmanagerProcess.isErrorState());
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess#upgradeAFJTechPackWithDelta()}.
   * 
   * @throws InterruptedException
   */
  @Test
  public void testUpgradeAFJTechPackWithTwoDelta() throws InterruptedException {
    final AfjManagerProcess afjmanagerProcess_1 = new AfjManagerProcess(
        Executors.newSingleThreadExecutor(new TestThreadFactory()));
    final AFJTechPack techPack1 = afjmanagerProcess_1.getAFJTechPackByName("DC_E_BSS");
    afjmanagerProcess_1.setSelectedTechPack(techPack1.getTechPackName());
    final AfjManagerProcess afjmanagerProcess_2 = new AfjManagerProcess(
        Executors.newSingleThreadExecutor(new TestThreadFactory()));
    final AFJTechPack techPack2 = afjmanagerProcess_2.getAFJTechPackByName("DC_E_BSS");
    afjmanagerProcess_2.setSelectedTechPack(techPack2.getTechPackName());
    afjmanagerProcess_1.generateAFJTechPackDelta();
    afjmanagerProcess_2.generateAFJTechPackDelta();
    while (afjmanagerProcess_1.isRunning() || afjmanagerProcess_2.isRunning()) {
      assertFalse("".equals(afjmanagerProcess_1.getStatusMessage()));
      assertFalse("".equals(afjmanagerProcess_2.getStatusMessage()));
      Thread.sleep(500);
    }
    afjmanagerProcess_1.upgradeAFJTechPackWithDelta();
    afjmanagerProcess_2.upgradeAFJTechPackWithDelta();
    assertFalse(afjmanagerProcess_1.isErrorState());
    assertFalse(afjmanagerProcess_2.isErrorState());
  }

  /**
   * Test method for {@link com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess#upgradeAFJTechPackWithDelta()}.
   * 
   * @throws InterruptedException
   */
  @Test
  public void testUpgradeAFJTechPackWithInvalidDelta() throws InterruptedException {
    final AfjManagerProcess afjmanagerProcess = new AfjManagerProcess(
        Executors.newSingleThreadExecutor(new TestThreadFactory()));
    final AFJTechPack techPack = afjmanagerProcess.getAFJTechPackByName("DC_E_INVALID");
    afjmanagerProcess.setSelectedTechPack(techPack.getTechPackName());
    afjmanagerProcess.generateAFJTechPackDelta();
    while (afjmanagerProcess.isRunning()) {
      assertFalse("".equals(afjmanagerProcess.getStatusMessage()));
      Thread.sleep(500);
    }
    assertTrue(afjmanagerProcess.getAFJDelta() == null);
    assertTrue(afjmanagerProcess.isErrorState());
    assertEquals(afjmanagerProcess.getProcessState(), State.GENERATE_ERROR);
  }

}
