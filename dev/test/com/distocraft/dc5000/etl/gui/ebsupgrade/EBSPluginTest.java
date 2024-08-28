///**
// * 
// */
package com.distocraft.dc5000.etl.gui.ebsupgrade;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
//
import java.util.Properties;
//
import org.junit.Test;
//
//
///**
// * @author eanubda
// *
// */
public class EBSPluginTest {

//  /**
//   * If properties exist plugin is enabled
//   * @throws Exception
//   */
  @Test
public void testIsEnabledTrue() throws Exception {
    final EBSPlugin ep = new EBSPlugin();
    ep.setProps(new Properties());
    EBSPlugin.setEBSPlugin(ep);
    assertTrue(EBSPlugin.isEnabled());
    
}

//  /**
//   * If properties dont exist plugin is disabled
//   * @throws Exception
//   */
  @Test
  public void testIsEnabledFalse() throws Exception {
      final EBSPlugin ep = new EBSPlugin();
      ep.setProps(null);
      EBSPlugin.setEBSPlugin(ep);
      assertFalse(EBSPlugin.isEnabled());
  }
  
  
  
}