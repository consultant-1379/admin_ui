package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import com.distocraft.dc5000.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.enminterworking.EnmInterUtils;
import com.ericsson.eniq.enminterworking.IEnmInterworkingRMI;


/**
 * Copyright &copy; Ericsson ltd. All rights reserved.<br>
 * This server shows details of the engine status.<br>
 * @author Janne Berggren
 */
public class FLSStatusDetails extends EtlguiServlet {

  private Log log = LogFactory.getLog(this.getClass());
  private static IEnmInterworkingRMI multiEs=null;
  
  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) {
    Template page = null;
    final String usePage = "fls_status_details.vm"; 

    ctx.put("flsStatusDetails", getFLSStatusDetails());
    // finally generate page
    try {
      page = getTemplate(usePage);
    } catch (ResourceNotFoundException e) {
      log.error("ResourceNotException",e);
    } catch (ParseErrorException e) {
      log.error("ParseErrorException",e);
    } catch (Exception e) {
      log.error("Exception",e);
    }

    // and return with the template
    return page;
  }

  public MonitorInformation getFLSStatusDetails() {
    MonitorInformation monitorInformation = new MonitorInformation();
    
    try {
    	
    	IEnmInterworkingRMI multiEs =  (IEnmInterworkingRMI) Naming.lookup(RmiUrlFactory.getInstance().getMultiESRmiUrl(EnmInterUtils.getEngineIP()));
    	List<List<String>> flsStatus=multiEs.status(); 
    	log.info(flsStatus.get(0));
    	if(flsStatus.get(1).get(1).contains("OnHold"))
        {
        	monitorInformation.setStatus(MonitorInformation.BULB_YELLOW);
        }
    	else {
            monitorInformation.setStatus(MonitorInformation.BULB_GREEN);
        }
    		
    	monitorInformation.setMessage(flsStatus.get(1).get(0));
        monitorInformation.setMessage("<br />");
        	
        ArrayList<String> splitStatus = new ArrayList<String>(Arrays.asList(flsStatus.get(1).get(1).split("\\|")));
        String msg="ENM ALIAS AND PROFILE DETAILS";
        monitorInformation.setMessage("---------------------------------------------\n");
        monitorInformation.setMessage(msg);
        monitorInformation.setMessage("---------------------------------------------\n");
        msg="";
        for(String stat:splitStatus)
        {
        		
        	if(stat.contains(("eniq_"))||stat.contains("Normal")||stat.contains("OnHold"))
        	{
        		msg+=" "+stat;
        		if(stat.contains("Normal")||stat.contains("OnHold"))
        		{
        			msg+="\n";
        			monitorInformation.setMessage(msg);
        		    monitorInformation.setMessage("---------------------------------------------\n");
        			msg="";
        		}
        	}
        	else
        	{
        		continue;
        	}
        		
        }
        monitorInformation.setMessage("<br />");
        monitorInformation.setMessage(flsStatus.get(1).get(2)+"<br />");
    	
    	
    } catch(Exception e){
    	 monitorInformation.setStatus(MonitorInformation.BULB_RED);
         monitorInformation.setFieldName(MonitorInformation.TITLE_FLS);
         log.error("Exception : "+e.getMessage());
    }
    return monitorInformation;
    
  }

 
}
