/**
 * Handles http-requests related to Ebs-upgrade. 
 * 
 * Attributes stored in session:
 *  -String listenerId
 * 
 */
package com.distocraft.dc5000.etl.gui.ebsupgrade;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.engine.system.SetListener;
import com.distocraft.dc5000.etl.engine.system.SetStatusTO;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

/**
 * @author epetrmi
 * 
 */
public class EbsUpgradeServlet extends EtlguiServlet {

  /**
   * Generated serial version id
   */
  private static final long serialVersionUID = -3220996182890709578L;

  private Log log = LogFactory.getLog(this.getClass());
   
  // Http-parameters as constants
  public final static String ACTION = "action";
  public final static String ACTION_RUN_UPGRADE = "action_run_upgrade";
  public final static String ACTION_GET_UPGRADE_STATUS = "action_get_upgrade_status";
  public final static String ACTION_GET_DETAILS = "action_get_details";
  public final static String VIEW_ACTION = "viewAction";

  public final static String IS_EBS_ENABLED = "isEbsEnabled";
  public final static String IS_EBS_AVAILABLE = "isEbsAvailable";
  
  public final static String UPGRADE_STATUS = "upgradeStatus";
  public final static String STATUSLIST = "statuslist";
  public final static String UPGRADELIST = "upgradelist";
  public final static String UPGRADE_ID = "upgradeId";
  public final static String IS_SET_STARTED = "isSetStarted";
//  public final static String NEW_UPGRADE_AVAILABLE = "newUpgradeAvailable";
  public final static String ERRORS = "errors";
  public final static String LISTENER_ID = "listenerId";

  private static final String ONE_SET_IS_RUNNING = "oneSetIsRunning";
  
  /**
   * Handles the http request (performs operations and forwards to corresponding
   * view)
   * 
   * @see com.distocraft.dc5000.etl.gui.common.EtlguiServlet#doHandleRequest(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.apache.velocity.context.Context)
   */
  @Override
  public Template doHandleRequest(HttpServletRequest request,
      HttpServletResponse response, Context ctx) throws Exception {

    
    
    //Messages
    List<String> errors = new ArrayList<String>();
    
    // Set default view
    String view = "ebs_upgrader_mainview.vm";
    Template outty = getTemplate(view);

    try{
    
  //Check if ebs is enabled
    if(EBSPlugin.isEnabled()){
    ctx.put(IS_EBS_ENABLED, true);
      
    // Handle ACTIONs
    String action = request.getParameter(ACTION);
    String viewAction = action != null ? action : "";
    IEbsUpgradeManager ebsUM = EbsUpgradeManagerFactory.getInstance();

      //Check if manager is available
      if(ebsUM!=null){
        //Manager should be available
        ctx.put(IS_EBS_AVAILABLE, true);
         
          if (ACTION_RUN_UPGRADE.equals(action)) {
//            runUpgrade(request, ctx, ebsUM);
            runNewUpgrade(request,ctx,ebsUM);
            
      
          } else if (ACTION_GET_UPGRADE_STATUS.equals(action)) {
            handleUpgradeStatus(request, ctx, ebsUM);
      
          } else if (ACTION_GET_DETAILS.equals(action)) {
            handleDetails(request, ctx, ebsUM);
  
          } else {
            // Default action
            handleUpgradeStatus(request,ctx,ebsUM);
          }
      
          ctx.put(VIEW_ACTION, viewAction);
//          return outty;
  
      }else{
        //Manager is not available
        ctx.put(IS_EBS_AVAILABLE, false);
        log.debug("Manager is not available");
//        return outty;
      }
    
    }else{
      //EBSPlugin is not enabled
      ctx.put(IS_EBS_ENABLED, false);
//      return outty;
    }
    
    }catch(Exception ex){
      errors.add("Ebs manager has technical problems. See logs for more details.");
      ctx.put(ERRORS,errors);
    }
    
    ctx.put(ERRORS, errors);
    return outty;
  }

  private void handleDetails(HttpServletRequest request, Context ctx,
      IEbsUpgradeManager ebsUM) {
    String upgradeId = request.getParameter(UPGRADE_ID); 
    SetStatusTO s = null;
    try {
      s = ebsUM.getStatus(upgradeId , -1, -1);
    } catch (Exception e) {
        e.printStackTrace();
    }
    ctx.put(VIEW_ACTION, ACTION_GET_DETAILS);
    ctx.put(UPGRADE_ID, upgradeId);
    ctx.put("setStatusTO", s);
  }

  private void runNewUpgrade(HttpServletRequest request, Context ctx,
      IEbsUpgradeManager ebsUM) {
    try {
      log.debug("Starting to execute set with listener...");

      String upgradeId = request.getParameter(UPGRADE_ID);
      SetStatusTO statusTO = ebsUM.executeUpgrade(upgradeId);
      getNewStatus(ctx, ebsUM);

    }catch(Exception e){
      log.error("Failed to run upgrade");
    }
  }    
  

  /**
   * Handles getting upgrade status action
   * 
   * @param request
   * @param ctx
   * @param ebsUM
   */
  private void handleUpgradeStatus(HttpServletRequest request, Context ctx,
      IEbsUpgradeManager ebsUM) {
//      getStatus(ctx, ebsUM);
    getNewStatus(ctx, ebsUM);
  }
private void getNewStatus(Context ctx, IEbsUpgradeManager ebsUM) {
  
  boolean oneSetIsRunning = false;
  
  //Get configured upgrades from ebsupgrademanager
  List<Upgrade> upgrades = ebsUM.getUpgrades();
  
  //Traverse through the list and
  //ask the status from engine
  for (Upgrade upgrade : upgrades) {
    SetStatusTO s;
    try {
      s = ebsUM.getStatus(upgrade.getId(), 0, 0);//GEt all now
      log.debug("Retrieved setStatusTO="+s);
      if(s!=null){
        
        //Check if the set is not finished (is running)
        //We dont allow multi execution
        if( SetListener.NOTFINISHED.equals(s.getSetStatus()) ) {
          upgrade.setRunning(true);
          
          //Give GUI information that
          //it knows to deny other upgrades (running sets)
          //oneSetIsRunning = true;
        }else{
          upgrade.setRunning(false);
        }
        
        upgrade.setStatus(s.getSetStatus());
      }
      log.debug("New status: "+upgrade.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  ctx.put(UPGRADELIST, upgrades);
  ctx.put(ONE_SET_IS_RUNNING, oneSetIsRunning);
  }


//private void getNewStatus_OLD(Context ctx, IEbsUpgradeManager ebsUM) {
//  
//  boolean oneSetIsRunning = false;
//  List<Upgrade> upgrades = ebsUM.getUpgrades();
//  
//  for (Upgrade upgrade : upgrades) {
//    SetStatusTO s;
//    try {
//      s = ebsUM.getStatus(upgrade.getId(), -1, -1);//GEt all now
//      log.debug("Retrieved setStatusTO="+s);
//      if(s!=null){
//        
//        //Check if the set is not finished (is running)
//        //We dont allow multi execution
//        if( SetListener.NOTFINISHED.equals(s.getSetStatus()) ) {
//          oneSetIsRunning = true;
//        }
//        upgrade.setStatus(s.getSetStatus());
//      }
//      log.debug("New status: "+upgrade.toString());
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//  ctx.put(UPGRADELIST, upgrades);
//  ctx.put(ONE_SET_IS_RUNNING, oneSetIsRunning);
//  }

}
