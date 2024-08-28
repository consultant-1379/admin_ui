/**
 * 
 */
package com.distocraft.dc5000.etl.gui.afjupgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import com.ericsson.eniq.afj.AFJManager;
import com.ericsson.eniq.afj.common.AFJMeasurementCounter;
import com.ericsson.eniq.afj.common.AFJMeasurementType;
import com.ericsson.eniq.afj.common.AFJMeasurementTag;
import com.ericsson.eniq.afj.common.AFJTechPack;
import com.ericsson.eniq.afj.common.AFJDelta;
import com.ericsson.eniq.exception.AFJConfiguationException;
import com.ericsson.eniq.exception.AFJException;

/**
 * @author eheijun
 * 
 */
public class MockAfjManager implements AFJManager {

  /**
   * 
   */
  private static final String EXPECTED_EXCEPTION_MESSAGE = "This is expected exception.";

  private static final String TEST_NAME_1 = "DC_E_STN";

  private static final String TEST_MTYPE_1 = "DC_E_STN_SYNCHRONIZATION_PLUS";
  
  private static final String TEST_TAG_1 = "Synchronization Plus";

  private static final String TEST_COUNTER_1 = "TS_NoTS_Reselections+";

  private static final String TEST_FILE_NAME_1 = "NEW_STN_COUNTERS.xml";

  private static final String TEST_MTYPE_2 = "DC_E_STN_PPP_EXTRA";

  private static final String TEST_TAG_2 = "PPP extra";

  private static final String TEST_COUNTER_2 = "SubDev";

  private static final String TEST_NAME_2 = "DC_E_BSS";

  private static final String TEST_MTYPE_3 = "GIF";

  private static final String TEST_TAG_3 = "Gif";

  private static final String TEST_COUNTER_3 = "WindowA";

  private static final String TEST_COUNTER_4 = "DoorC";

  private static final String TEST_FILE_NAME_2 = "update_for_bss_counters.xml";

  private static final String TEST_NAME_3 = "DC_E_INVALID";
  
//  private static final String TEST_STATUS_3 = "Never";

  private static final String TEST_FILE_NAME_3 = "dummy.xml";

  private static final String TEST_MTYPE_4 = "XYZ";

  private static final String TEST_TAG_4 = "xyZ";

  private static final String TEST_NAME_4 = "DC_E_NOFILE";
  
//  private static final String TEST_STATUS_4 = "Never";

  private static final String TEST_FILE_NAME_4 = "";

  private final Mockery context = new JUnit4Mockery() {

    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  private final AFJTechPack mockAFJTechPack1;

  private final AFJTechPack mockAFJTechPack2;

  private final AFJTechPack mockAFJTechPack3;

  private final AFJTechPack mockAFJTechPack4;

  private final AFJTechPack[] afjTechPackTable;

  private final AFJDelta mockAFJDelta1;

  private final AFJDelta mockAFJDelta2;

  private final AFJDelta mockAFJDelta3;

  private final AFJMeasurementType mockAFJMeasurementType1_1;

  private final AFJMeasurementType mockAFJMeasurementType1_2;

  private final AFJMeasurementType mockAFJMeasurementType2_1;

  private final AFJMeasurementType mockAFJMeasurementType2_2;
  
  private final AFJMeasurementType[] afjMeasurementTypeTable1;

  private final AFJMeasurementType[] afjMeasurementTypeTable2;

  private final Map<String, List<AFJMeasurementCounter>> afjTagToCountersMap1_1;

  private final Map<String, List<AFJMeasurementCounter>> afjTagToCountersMap1_2;

  private final Map<String, List<AFJMeasurementCounter>> afjTagToCountersMap2_1;

  private final Map<String, List<AFJMeasurementCounter>> afjTagToCountersMap2_2;

  private final AFJMeasurementTag mockAFJTag1_1;

  private final AFJMeasurementTag mockAFJTag1_2;

  private final AFJMeasurementTag mockAFJTag2_1;

  private final AFJMeasurementTag mockAFJTag2_2;
  
  private final AFJMeasurementTag[] afjTagTable1_1;

  private final AFJMeasurementTag[] afjTagTable1_2;

  private final AFJMeasurementTag[] afjTagTable2_1;

  private final AFJMeasurementTag[] afjTagTable2_2;

  private final AFJMeasurementCounter mockAFJMeasurementCounter1_1_1;

  private final AFJMeasurementCounter mockAFJMeasurementCounter1_1_2;

  private final AFJMeasurementCounter mockAFJMeasurementCounter1_2_1;

  private final AFJMeasurementCounter mockAFJMeasurementCounter1_2_2;

  private final AFJMeasurementCounter mockAFJMeasurementCounter2_1_1;

  private final AFJMeasurementCounter mockAFJMeasurementCounter2_1_2;

  private final AFJMeasurementCounter mockAFJMeasurementCounter2_2_1;
  
  private final AFJMeasurementCounter[] afjMeasurementCounterTable1_1;

  private final AFJMeasurementCounter[] afjMeasurementCounterTable1_2;

  private final AFJMeasurementCounter[] afjMeasurementCounterTable2_1;

  private final AFJMeasurementCounter[] afjMeasurementCounterTable2_2;
  
  private boolean afjTechPackInstalled;

  public static boolean mockFail = false; 

  public MockAfjManager() throws AFJException {
    mockAFJTechPack1 = context.mock(AFJTechPack.class, "T1");
    mockAFJTechPack2 = context.mock(AFJTechPack.class, "T2");
    mockAFJTechPack3 = context.mock(AFJTechPack.class, "T3");
    mockAFJTechPack4 = context.mock(AFJTechPack.class, "T4");

    mockAFJDelta1 = context.mock(AFJDelta.class, "1");
    mockAFJDelta2 = context.mock(AFJDelta.class, "2");
    mockAFJDelta3 = context.mock(AFJDelta.class, "3");

    mockAFJMeasurementType1_1 = context.mock(AFJMeasurementType.class, "MT1.1");
    mockAFJMeasurementType1_2 = context.mock(AFJMeasurementType.class, "MT1.2");
    mockAFJMeasurementType2_1 = context.mock(AFJMeasurementType.class, "MT2.1");
    mockAFJMeasurementType2_2 = context.mock(AFJMeasurementType.class, "MT2.2");
    
    mockAFJTag1_1 = context.mock(AFJMeasurementTag.class, "1.1");
    mockAFJTag1_2 = context.mock(AFJMeasurementTag.class, "1.2");
    mockAFJTag2_1 = context.mock(AFJMeasurementTag.class, "2.1");
    mockAFJTag2_2 = context.mock(AFJMeasurementTag.class, "2.2");

    mockAFJMeasurementCounter1_1_1 = context.mock(AFJMeasurementCounter.class, "1.1.1");
    mockAFJMeasurementCounter1_1_2 = context.mock(AFJMeasurementCounter.class, "1.1.2");
    mockAFJMeasurementCounter1_2_1 = context.mock(AFJMeasurementCounter.class, "1.2.1");
    mockAFJMeasurementCounter1_2_2 = context.mock(AFJMeasurementCounter.class, "1.2.2");
    mockAFJMeasurementCounter2_1_1 = context.mock(AFJMeasurementCounter.class, "2.1.1");
    mockAFJMeasurementCounter2_1_2 = context.mock(AFJMeasurementCounter.class, "2.1.2");
    mockAFJMeasurementCounter2_2_1 = context.mock(AFJMeasurementCounter.class, "2.2.1");

    afjTechPackTable = new AFJTechPack[] { mockAFJTechPack1, mockAFJTechPack2, mockAFJTechPack3, mockAFJTechPack4 };
    
    afjMeasurementTypeTable1 = new AFJMeasurementType[] { mockAFJMeasurementType1_1, mockAFJMeasurementType1_2 };     
    afjMeasurementTypeTable2 = new AFJMeasurementType[] { mockAFJMeasurementType2_1, mockAFJMeasurementType2_2 };
    
    afjTagTable1_1 = new AFJMeasurementTag[] { mockAFJTag1_1 };
    afjTagTable1_2 = new AFJMeasurementTag[] { mockAFJTag1_2 };
    afjTagTable2_1 = new AFJMeasurementTag[] { mockAFJTag2_1 };
    afjTagTable2_2 = new AFJMeasurementTag[] { mockAFJTag2_2 };

    afjMeasurementCounterTable1_1 = new AFJMeasurementCounter[] { mockAFJMeasurementCounter1_1_1, mockAFJMeasurementCounter1_1_2 };
    afjMeasurementCounterTable1_2 = new AFJMeasurementCounter[] { mockAFJMeasurementCounter1_2_1, mockAFJMeasurementCounter1_2_2 };
    afjMeasurementCounterTable2_1 = new AFJMeasurementCounter[] { mockAFJMeasurementCounter2_1_1, mockAFJMeasurementCounter2_1_2 };
    afjMeasurementCounterTable2_2 = new AFJMeasurementCounter[] { mockAFJMeasurementCounter2_2_1 };
    
    afjTagToCountersMap1_1 = new Hashtable<String, List<AFJMeasurementCounter>>();
    afjTagToCountersMap1_1.put(TEST_TAG_1, Arrays.asList(afjMeasurementCounterTable1_1));
    
    afjTagToCountersMap1_2 = new HashMap<String, List<AFJMeasurementCounter>>();
    afjTagToCountersMap1_2.put(TEST_TAG_2, Arrays.asList(afjMeasurementCounterTable1_2));
    
    afjTagToCountersMap2_1 = new Hashtable<String, List<AFJMeasurementCounter>>();
    afjTagToCountersMap2_1.put(TEST_TAG_3, Arrays.asList(afjMeasurementCounterTable2_1));
    
    afjTagToCountersMap2_2 = new Hashtable<String, List<AFJMeasurementCounter>>();
    afjTagToCountersMap2_2.put(TEST_TAG_4, Arrays.asList(afjMeasurementCounterTable2_2));
    
    context.checking(new Expectations() {

      {
        allowing(mockAFJTechPack1).getTechPackName();
        will(returnValue(TEST_NAME_1));
        allowing(mockAFJTechPack1).getFileName();
        will(returnValue(TEST_FILE_NAME_1));
        allowing(mockAFJTechPack1).isMomFilePresent();
        will(returnValue(true));
        allowing(mockAFJTechPack1).getMessage();
        will(returnValue(null));

        allowing(mockAFJTechPack2).getTechPackName();
        will(returnValue(TEST_NAME_2));
        allowing(mockAFJTechPack2).getFileName();
        will(returnValue(TEST_FILE_NAME_2));
        allowing(mockAFJTechPack2).isMomFilePresent();
        will(returnValue(true));
        allowing(mockAFJTechPack2).getMessage();
        will(returnValue(null));

        allowing(mockAFJTechPack3).getTechPackName();
        will(returnValue(TEST_NAME_3));
        allowing(mockAFJTechPack3).getFileName();
        will(returnValue(TEST_FILE_NAME_3));
        allowing(mockAFJTechPack3).isMomFilePresent();
        will(returnValue(true));
        allowing(mockAFJTechPack3).getMessage();
        will(returnValue(null));

        allowing(mockAFJTechPack4).getTechPackName();
        will(returnValue(TEST_NAME_4));
        allowing(mockAFJTechPack4).getFileName();
        will(returnValue(TEST_FILE_NAME_4));
        allowing(mockAFJTechPack4).isMomFilePresent();
        will(returnValue(false));
        allowing(mockAFJTechPack4).getMessage();
        will(returnValue("WARNING:No xml file found."));

        allowing(mockAFJDelta1).getMeasurementTypes();
        will(returnValue(new ArrayList<AFJMeasurementType>(Arrays.asList(afjMeasurementTypeTable1))));

        allowing(mockAFJDelta2).getMeasurementTypes();
        will(returnValue(new ArrayList<AFJMeasurementType>(Arrays.asList(afjMeasurementTypeTable2))));

        allowing(mockAFJDelta3).getMeasurementTypes();
        will(throwException(new AFJException("Invalid MOM")));

        allowing(mockAFJMeasurementType1_1).getTypeName();
        will(returnValue(TEST_MTYPE_1));
        
        allowing(mockAFJMeasurementType1_2).getTypeName();
        will(returnValue(TEST_MTYPE_2));
        
        allowing(mockAFJMeasurementType2_1).getTypeName();
        will(returnValue(TEST_MTYPE_3));
        
        allowing(mockAFJMeasurementType2_2).getTypeName();
        will(returnValue(TEST_MTYPE_4));

        allowing(mockAFJMeasurementType1_1).isTypeNew();
        will(returnValue(false));
        
        allowing(mockAFJMeasurementType1_2).isTypeNew();
        will(returnValue(true));
        
        allowing(mockAFJMeasurementType2_1).isTypeNew();
        will(returnValue(true));

        allowing(mockAFJMeasurementType2_2).isTypeNew();
        will(returnValue(false));
        
        allowing(mockAFJMeasurementType1_1).getTags();
        will(returnValue(Arrays.asList(afjTagTable1_1)));
        
        allowing(mockAFJMeasurementType1_2).getTags();
        will(returnValue(Arrays.asList(afjTagTable1_2)));
        
        allowing(mockAFJMeasurementType2_1).getTags();
        will(returnValue(Arrays.asList(afjTagTable2_1)));
        
        allowing(mockAFJMeasurementType2_2).getTags();
        will(returnValue(Arrays.asList(afjTagTable2_2)));
        
        allowing(mockAFJTag1_1).getTagName();
        will(returnValue(TEST_TAG_1));
        
        allowing(mockAFJTag1_2).getTagName();
        will(returnValue(TEST_TAG_2));
        
        allowing(mockAFJTag2_1).getTagName();
        will(returnValue(TEST_TAG_3));
        
        allowing(mockAFJTag2_2).getTagName();
        will(returnValue(TEST_TAG_4));

        allowing(mockAFJTag1_1).getNewCounters();
        will(returnValue(Arrays.asList(afjMeasurementCounterTable1_1)));
        
        allowing(mockAFJTag1_2).getNewCounters();
        will(returnValue(Arrays.asList(afjMeasurementCounterTable1_2)));
        
        allowing(mockAFJTag2_1).getNewCounters();
        will(returnValue(Arrays.asList(afjMeasurementCounterTable2_1)));
        
        allowing(mockAFJTag2_2).getNewCounters();
        will(returnValue(Arrays.asList(afjMeasurementCounterTable2_2)));
        
        allowing(mockAFJMeasurementCounter1_1_1).getCounterName();
        will(returnValue(TEST_COUNTER_1));
        allowing(mockAFJMeasurementCounter1_1_1).isCounterNew();
        will(returnValue(Boolean.TRUE));

        allowing(mockAFJMeasurementCounter1_1_2).getCounterName();
        will(returnValue(TEST_COUNTER_2));
        allowing(mockAFJMeasurementCounter1_1_2).isCounterNew();
        will(returnValue(Boolean.TRUE));

        allowing(mockAFJMeasurementCounter1_2_1).getCounterName();
        will(returnValue(TEST_COUNTER_3));
        allowing(mockAFJMeasurementCounter1_2_1).isCounterNew();
        will(returnValue(Boolean.TRUE));

        allowing(mockAFJMeasurementCounter1_2_2).getCounterName();
        will(returnValue(TEST_COUNTER_4));
        allowing(mockAFJMeasurementCounter1_2_2).isCounterNew();
        will(returnValue(Boolean.TRUE));

        allowing(mockAFJMeasurementCounter2_1_1).getCounterName();
        will(returnValue(TEST_COUNTER_1));
        allowing(mockAFJMeasurementCounter2_1_1).isCounterNew();
        will(returnValue(Boolean.TRUE));

        allowing(mockAFJMeasurementCounter2_1_2).getCounterName();
        will(returnValue(TEST_COUNTER_2));
        allowing(mockAFJMeasurementCounter2_1_2).isCounterNew();
        will(returnValue(Boolean.TRUE));

        allowing(mockAFJMeasurementCounter2_2_1).getCounterName();
        will(returnValue(TEST_COUNTER_3));
        allowing(mockAFJMeasurementCounter2_2_1).isCounterNew();
        will(returnValue(Boolean.TRUE));

      }
    });
  }

  /*
   * (non-Javadoc)
   * @see com.ericsson.eniq.afj.AFJManager#getAFJTechPacks()
   */
  @Override
  public List<AFJTechPack> getAFJTechPacks() throws AFJException, AFJConfiguationException {
    if (mockFail) {
      throw new AFJException(EXPECTED_EXCEPTION_MESSAGE);
    }
    if (afjTechPackInstalled) {
      final List<AFJTechPack> result = new ArrayList<AFJTechPack>(Arrays.asList(afjTechPackTable));
      return result;
    }
    throw new AFJConfiguationException("AFJ techpack is not properly installed.");
  }

  /*
   * (non-Javadoc)
   * @see com.ericsson.eniq.afj.AFJManager#getAFJTechPack(java.lang.String)
   */
  @Override
  public AFJTechPack getAFJTechPack(String techPackName) throws AFJException, AFJConfiguationException {
    List<AFJTechPack> afjTechPackList = getAFJTechPacks();
    for (AFJTechPack afjTechPack : afjTechPackList) {
      if (afjTechPack.getTechPackName().equals(techPackName)) {
        return afjTechPack;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see com.ericsson.eniq.afj.AFJManager#getAFJDelta(com.ericsson.eniq.common.AFJTechPack)
   */
  @Override
  public AFJDelta getAFJDelta(final AFJTechPack techPack) throws AFJException {
    if (mockFail) {
      throw new AFJException(EXPECTED_EXCEPTION_MESSAGE);
    }
    try {
      if (TEST_NAME_1.equals(techPack.getTechPackName())) {
        final AFJDelta result = mockAFJDelta1;
        /*for (int i = 0; i < 4; i++) {
          Thread.sleep(1000);
        }*/
        return result;
      } else if (TEST_NAME_2.equals(techPack.getTechPackName())) {
        final AFJDelta result = mockAFJDelta2;
        /*for (int i = 0; i < 6; i++) {
          Thread.sleep(1000);
        }*/
        return result;
      } else if (TEST_NAME_3.equals(techPack.getTechPackName())) {
        throw new AFJException("Some error happened when generating delta for " + techPack.getTechPackName() + ".");
      }
    } catch (AFJException e) {
      throw e;
    } catch (Exception e) {
      throw new AFJException(e.getMessage());
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.afjupgrade.AfjManager#upgradeAfjTechPack(com.distocraft.dc5000.etl.gui.afjupgrade.AfjTechPack, com.distocraft.dc5000.etl.gui.afjupgrade.AFJDelta)
   */
  @Override
  public String upgradeAFJTechPack(final AFJDelta delta) throws AFJException {
    if (mockFail) {
      throw new AFJException(EXPECTED_EXCEPTION_MESSAGE);
    }
    try {
      for (int i = 0; i < 10; i++) {
        Thread.sleep(100);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return "Upgrade OK";
  }
  
  public void uninstallAFJTechPack() {
    afjTechPackInstalled = false;
  }

  public void installAFJTechPack() {
    afjTechPackInstalled = true;
  }

  @Override
  public Boolean restoreAFJTechPack(AFJTechPack techPack) throws AFJException {
    // TODO Auto-generated method stub
    return null;
  }

}
