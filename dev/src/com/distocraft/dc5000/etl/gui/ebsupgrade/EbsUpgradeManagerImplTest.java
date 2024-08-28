///**
// * 
// */
//package com.distocraft.dc5000.etl.gui.ebsupgrade;
//
//import java.util.Properties;
//
//import org.junit.Test;
//
//
///**
// * @author epetrmi
// *
// */
//public class EbsUpgradeManagerImplTest {
//  IEbsUpgradeManager e = new EbsUpgradeManagerImpl();
//
//  @Test 
//  public void testExecuteSetAndWait() throws Exception{
//    long startTime = System.currentTimeMillis();
//    
//    long b = System.currentTimeMillis();
//    e.initialize(new Properties());//Use classes default stuff
//    System.out.println("init.time="+ (System.currentTimeMillis()-b));
//    
//    b = System.currentTimeMillis();
//    e.executeUpgrade();
//    System.out.println("execute.time="+ (System.currentTimeMillis()-b));
//    
//    //Loop while running (3 success, 4 failed)
//    while(Integer.parseInt(e.getUpgradeStatus())<3){
//      System.out.println("time: "+System.currentTimeMillis()/1000 +" testStatus="+e.getUpgradeStatus());
//    }
//    
//    System.out.println("totaltime="+(System.currentTimeMillis()-startTime));
//    
//  }
//
//}
