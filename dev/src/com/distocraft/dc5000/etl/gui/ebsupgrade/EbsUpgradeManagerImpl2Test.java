package com.distocraft.dc5000.etl.gui.ebsupgrade;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Properties;

import org.junit.Test;

public class EbsUpgradeManagerImpl2Test {

  @Test
  public void testParseUpgrades() {
  String valueList = "PM_E_EBSW=/eniq/data/pmdata/ebs/ebs_ebsw/,PM_E_EBSG=/eniq/data/pmdata/ebs/ebs_ebsg/,PM_E_EBSS=/eniq/data/pmdata/ebs/ebs_ebss/";
  EbsUpgradeManagerImpl2 e = new EbsUpgradeManagerImpl2(new Properties());
  List<Upgrade> list = e.parseUpgrades(valueList);
  assertEquals("PM_E_EBSW",list.get(0).getId());
  assertEquals("PM_E_EBSG",list.get(1).getId());
  assertEquals("PM_E_EBSS",list.get(2).getId());
  
  }

}
