package com.distocraft.dc5000.etl.gui.monitor;

import static org.junit.Assert.*;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for ShowLoadings.
 * @author eciacah
 *
 */
@RunWith(JMock.class)
public class ShowLoadingsTest {

  private Mockery context;

  private ShowLoadings testInstance;

  @Before
  public void setUp() {
    context = new JUnit4Mockery();
    context.setImposteriser(ClassImposteriser.INSTANCE);

    testInstance = new ShowLoadings();
  }
  
  /** Tests for rows in Log_LOADSTATUS at 15MIN level **/
  
  /**
   * At 15 minute level, one 15 min rop NOT_LOADED followed by LOADED and in the same hour 
   * should return NOT_LOADED.
   * Status should be kept at NOT_LOADED.
   */
  @Test
  public void testUpdateLDStatusInSameHour15MINNotLoadedLoaded() {
        
    // Data from the previous row read in:
    LoadingDetails ld = new LoadingDetails();
    ld.setMeatypeName("DC_E_TESTMTYPE");
    ld.setTimelevel("15MIN");
    ld.addStatus("NOT_LOADED");
    ld.setProblematic(true);
    
    // Data from the database for the current row:
    final String tlevel = "15MIN";
    final String status = "LOADED";
    
    LoadingDetails outputLD = testInstance.updateLDStatusInSameHour(ld, tlevel, status);
    assertTrue("One 15 min rop NOT_LOADED followed by LOADED in the same hour should return NOT_LOADED", 
        outputLD.getStatus().equalsIgnoreCase("NOT_LOADED"));
  }
  
  /**
   * At 15 minute level, one 15 min rop LOADED followed by NOT_LOADED and in the same hour 
   * should return NOT_LOADED.
   * Status should be updated to NOT_LOADED.
   */
  @Test
  public void testUpdateLDStatusInSameHour15MINLoadedNotLoaded() {
        
    // Data from the previous row read in:
    LoadingDetails ld = new LoadingDetails();
    ld.setMeatypeName("DC_E_TESTMTYPE");
    ld.setTimelevel("15MIN");
    ld.addStatus("LOADED");
    ld.setProblematic(false);
    
    // Data from the database for the current row:
    final String tlevel = "15MIN";
    final String status = "NOT_LOADED";
    
    LoadingDetails outputLD = testInstance.updateLDStatusInSameHour(ld, tlevel, status);
    assertTrue("One 15 min rop LOADED followed by NOT_LOADED in the same hour should return NOT_LOADED", 
        outputLD.getStatus().equalsIgnoreCase("NOT_LOADED"));
  }
  
  
  /** Tests for rows in Log_LOADSTATUS at HOUR level **/
  
  /**
   * Previous row is problematic (NOT_LOADED).
   * NOT_LOADED followed by LOADED status for the same hour, should give Loaded status.
   * Should take the status of the new row.
   */
  @Test
  public void testUpdateLDStatusNotLoadedLoaded() {
        
    // Data from the previous row read in:
    LoadingDetails ld = new LoadingDetails();
    ld.setMeatypeName("DC_E_TESTMTYPE");
    ld.setTimelevel("HOUR");
    ld.addStatus("NOT_LOADED");
    ld.setProblematic(true);
    
    // Data from the database for the current row:
    final String tlevel = "HOUR";
    final String status = "LOADED";
    
    LoadingDetails outputLD = testInstance.updateLDStatusInSameHour(ld, tlevel, status);
    assertTrue("Should take the status of the new row at HOUR level", outputLD.getStatus().equalsIgnoreCase("LOADED"));
  }

  /**
   * Previous row is problematic (HOLE).
   * HOLE followed by LOADED status within the same hour, should give LOADED status.
   * Should take the status of the new row.
   */
  @Test
  public void testUpdateLDStatusHoleLoaded() {

    // Data from the previous row read in:
    LoadingDetails ld = new LoadingDetails();
    ld.setMeatypeName("DC_E_TESTMTYPE");
    ld.setTimelevel("HOUR");
    ld.addStatus("HOLE");
    ld.setProblematic(true);
    
    // Data from the database for the current row:
    final String tlevel = "HOUR";
    final String status = "LOADED";

    LoadingDetails outputLD = testInstance.updateLDStatusInSameHour(ld, tlevel, status);
    assertTrue("Should take the status of the new row at HOUR level", outputLD.getStatus().equalsIgnoreCase("LOADED"));
  }
  
  /**
   * Current and previous rows are LOADED, should keep status.
   */
  @Test
  public void testUpdateLDStatusInSameHourNoChange() {

    // Previous row has a HOLE, but is not set as problematic.
    // Previous and current row are treated as OK.
    LoadingDetails ld = new LoadingDetails();
    ld.setMeatypeName("DC_E_TESTMTYPE");
    ld.setTimelevel("HOUR");
    ld.addStatus("LOADED");
    ld.setProblematic(false);
    
    // Current row is ok (loaded):
    final String tlevel = "HOUR";
    final String status = "LOADED";

    LoadingDetails outputLD = testInstance.updateLDStatusInSameHour(ld, tlevel, status);
    assertTrue("Status should not be changed if both rows are ok", 
        outputLD.getStatus().equalsIgnoreCase("LOADED"));
  }

  /**
   * Previous row is LOADED, but current row status is HOLE.
   * Should return status LOADED if the time level is at HOUR.
   * 
   * Special case for HOUR time level, if the current row is NOT_LOADED or HOLE, 
   * it should take the previous status.
   */
  @Test
  public void testUpdateLDStatusLoadedHole() {

    // Data from the previous row read in:
    LoadingDetails ld = new LoadingDetails();
    ld.setMeatypeName("DC_E_TESTMTYPE");
    ld.setTimelevel("HOUR");
    ld.addStatus("LOADED");
    ld.setProblematic(false);
    
    // Data from the database for the current row:
    final String tlevel = "HOUR";
    final String status = "HOLE";

    LoadingDetails outputLD = testInstance.updateLDStatusInSameHour(ld, tlevel, status);
    assertTrue(outputLD.getStatus().equalsIgnoreCase("LOADED"));
  }
  
  /**
   * Previous row is LOADED, but current row status is NOT_LOADED.
   * Should return status LOADED if the time level is at HOUR.
   * 
   * Special case for HOUR time level, if the current row is NOT_LOADED or HOLE, 
   * it should take the previous status.
   */
  @Test
  public void testUpdateLDStatusLoadedNotLoaded() {
        
    // Data from the previous row read in:
    LoadingDetails ld = new LoadingDetails();
    ld.setMeatypeName("DC_E_TESTMTYPE");
    ld.setTimelevel("HOUR");
    ld.addStatus("LOADED");
    ld.setProblematic(false);
    
    // Data from the database for the current row:
    final String tlevel = "HOUR";
    final String status = "NOT_LOADED";

    LoadingDetails outputLD = testInstance.updateLDStatusInSameHour(ld, tlevel, status);
    assertTrue("Should return status LOADED if the time level is at HOUR", 
        outputLD.getStatus().equalsIgnoreCase("LOADED"));
  }
  
  /**
   * Previous row is LOADED, but current row status is LATE_DATA.
   * Should return currnet status LATE_DATA if the time level is at HOUR.
   * 
   * If the current row is anything other that NOT_LOADED or HOLE at HOUR timelevel,
   * the error status should be kept.
   */
  @Test
  public void testUpdateLDStatusLoadedLateData() {
    
    
    // Data from the previous row read in:
    LoadingDetails ld = new LoadingDetails();
    ld.setMeatypeName("DC_E_TESTMTYPE");
    ld.setTimelevel("HOUR");
    ld.addStatus("LOADED");
    ld.setProblematic(false);
    
    // Data from the database for the current row:
    final String tlevel = "HOUR";
    final String status = "LATE_DATA";

    LoadingDetails outputLD = testInstance.updateLDStatusInSameHour(ld, tlevel, status);
    assertTrue("LoadingDetails status should be set to the status of the current row", 
        outputLD.getStatus().equalsIgnoreCase("LATE_DATA"));
    assertTrue("LoadingDetails should be 'problematic'", outputLD.getProblematic());
  }
  

}
