package com.distocraft.dc5000.etl.gui.etl;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.RmiUrlFactory;
import com.distocraft.dc5000.etl.engine.main.ITransferEngineRMI;
import com.distocraft.dc5000.etl.gui.common.ENIQServiceStatusInfo;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.util.DateFormatter;
import com.distocraft.dc5000.etl.gui.util.Helper;
/**
 *  Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Shows sets that have been run and those status.<br>
 * RMI- connections are used for fetching. Only succeeded and failed sets are received from dc5000rep.
 *
 * @author Antti Laurila
 */
public class ShowSet extends EtlguiServlet {

	private static final Log log    = LogFactory.getLog(ShowSet.class.getClass()); // general logger

  /*
	private static String engineHost = null;
	private static String enginePort = null;
  */
  private static String rmiurl = null;
	private static String engineServiceName = null;

  // private static final SimpleDateFormat sdf_secs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
		final Map ETLSets = new HashMap();			// contains the sets (in the end)

    Template outty = null;
    String page = "etlShowManySets.vm"; 	// show this page if nothing else is stated

    Connection connERep = ((RockFactory)ctx.get("rockEtlRep")).getConnection();

		try {
			// create RMI object
			ITransferEngineRMI trRMI = (ITransferEngineRMI)Naming.lookup(RmiUrlFactory.getInstance().getEngineRmiUrl());

			// fetch all the data needed (running, executed, queued, failed sets)

			ETLSets.put("Queued",processMap(trRMI.getQueuedSets()));
			log.debug("queued fetched");
			ETLSets.put("Running",processMap(trRMI.getRunningSets()));
			log.debug("running fetched");
			ETLSets.put("Executed",getExecutedSets(connERep));
			log.debug("executed fetched");
			ETLSets.put("Failed",getFailedSets(connERep));
			log.debug("failed fetched");

            // aaaaaand we are done....

      if (request.getParameter("colName") != null) {
        page = "etlShowManySets_dummy.vm";
      }

		} catch (MalformedURLException murle) {
			log.error("MalformedURLException error:",murle);
		} catch (RemoteException re) {
			log.error("RemoteException error:",re);
		} catch (NotBoundException nbe) {
			log.error("NotBoundException error:",nbe);
		} catch (Exception e) {
			log.error("Exception caught: ",e);
		}

		if(ENIQServiceStatusInfo.isEngineOffline()){
			ctx.put("errorSet", true);
			ctx.put("errorText", " Engine is offline. Can not show SETs status.");			
			return getTemplate(EtlguiServlet.ADMINUI_NEW_ERROR_PAGE_TEMPLATE_WITHMENU);
		}else{
			ctx.put("isEngineOnline", true);
		}
		
		// put the sets to context
		ctx.put("etlsets", ETLSets);

		// return the page that should be shown
        outty = getTemplate(page);
		return outty;
	}

	
	/**
   * @param queuedSets
   * @return queuedSets with schedulingInfo containing only TimeLevel information.
   */
  protected List<Map<String, String>> processMap(List<Map<String, String>> queuedSets) {
    // TODO Auto-generated method stub
    final List<Map<String, String>> result = new ArrayList<Map<String, String>>();
    final Iterator<Map<String, String>> iter = queuedSets.iterator();
    while (iter.hasNext()) {
      final Map<String, String> setMap = iter.next();
      setMap.put("schedulingInfo",giveBackOnlyTimeLevel(setMap.get("schedulingInfo")));
    }
    return queuedSets;
  }

  /**
   * @param existingScheduling : This will be of the form : Timelevel 1440 Date 2011-05-16 00:00:00
   * @return : Only the timelevel part i.e Timelevel 1440.
   */
  protected String giveBackOnlyTimeLevel(String existingScheduling) {
    // TODO Auto-generated method stub
   String returnString = null ;
    if (existingScheduling != null) {
      if (existingScheduling.isEmpty()){
        returnString = "";
    }
      else {
        if(existingScheduling.indexOf("Date") != -1){
          returnString = existingScheduling.substring(0, existingScheduling.indexOf("Date"));
        }
        else{
          returnString = "";
        }
      }
    }
    else {
      returnString ="";
    }
    return returnString;
  }

  /**
	 * @return failed sets
	 */
	private List getFailedSets(Connection connERep) {
		return fetchSets(connERep,"y");
	}

	/**
	 * @return executed sets
	 */
	private List getExecutedSets(Connection connERep) {
		return fetchSets(connERep,"n");
	}

	/**
	 * Retrieves TOP 20 sets from the DC5000 ETL repository. Depending on the fail_flag
	 * they can be either good ones ('n') or failed ones ('y').
	 *
	 * @param fail_flag which kind of records are selected, ok ('n') or failed ('y')
	 * @return list of sets
	 */
	private List fetchSets(Connection conn, String fail_flag) {
		final List retval = new ArrayList();

		// Sql statement as a string
		final String sqlStmt = "SELECT TOP 20 B.ID, B.START_DATE, B.END_DATE, B.FAIL_FLAG, B.STATUS, B.VERSION_NUMBER, B.META_COLLECTION_SET_NAME, B.META_COLLECTION_NAME, B.SETTYPE, B.SCHEDULING_INFO, B.SERVICE_NODE, B.SLOT_ID "+
             "FROM META_TRANSFER_BATCHES B " +
             "WHERE " +
						 "B.FAIL_FLAG = '"+fail_flag+"' "+
						 "ORDER BY 3 DESC,2";


		// make query and build up executed list
		Statement stmt = null;
		ResultSet rset = null;
		//
		try {
			log.debug("Executing query: " + sqlStmt);

			stmt = conn.createStatement();
			rset = stmt.executeQuery(sqlStmt);

			// as long as the collection_set_id remains same we have same
			// collection set (measurementtype), when it changes create a
			// new "set"
			while (rset.next()) {
				Map m = new HashMap();
				//Dateformatter is used to check timeperiod for results that are shown..
        final DateFormatter df = new DateFormatter("yyyy-MM-dd hh:mm:ss.mm");

        df.setCalendar(rset.getString(2));

        final int rev = Helper.getEnvEntryInt("etlShowAge");

        if (df.reverseTime(rev).before(df.getTime())){
          m.put("techpackName", rset.getString(7));
          m.put("setName", rset.getString(8));
          m.put("setType", rset.getString(9));
          m.put("startTime", rset.getString(2));
          m.put("endTime", rset.getString(3));
          m.put("status", rset.getString(5));
          m.put("failureReason", "");
          m.put("priority", "");
          //m.put("runningSlot", "");
          m.put("runningAction", "");
          m.put("version", rset.getString(6));
          
          if (rset.getString(10) != null) {
            if (rset.getString(10).isEmpty()){
          m.put("schedulingInfo","");
          }
            else {
              if(rset.getString(10).indexOf("Date") != -1){
                m.put("schedulingInfo",rset.getString(10).substring(0, rset.getString(10).indexOf("Date")));
              }
              else{
                m.put("schedulingInfo","");
              }
            }
          }
          else {
          m.put("schedulingInfo","");
          }
          
          //20111129 EANGUAN :: To add service node column in adminui :: HSS/SMF IP
          if (rset.getString(11) != null) {
        	  if (rset.getString(11).isEmpty()){
        		  m.put("serviceNode", "");
        	  }else{
        		  m.put("serviceNode", rset.getString(11));
        	  }
          }else{
        	  m.put("serviceNode", "");
          }
          
          
          //20120305 EVIVRAO :: To add slot_id in adminui :: HSS/SMF IP
          if (rset.getString(12) != null) {
        	  if (rset.getString(12).isEmpty()){
        		  m.put("runningSlot", "");
        	  }else{
        		  m.put("runningSlot", rset.getString(12));
        	  }
          }else{
        	  m.put("runningSlot", "");
          }

          retval.add(m);
        }
			}

		} catch (SQLException ex) {
			log.error("SQLException: ",ex);
		} finally {
			try {
				if(rset!=null) {
					rset.close();
				}
			} catch(SQLException e) {
              log.error("SQLException",e);
            }
			try {
				if(stmt!=null) {
					stmt.close();
				}
			} catch(SQLException e) {
              log.error("SQLException",e);
            }
		}

		// return the executed sets
		return retval;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
	 */
	@Override
  public void init(ServletConfig arg0) throws ServletException {
		super.init(arg0);

    ShowSet.rmiurl = Helper.getEnvEntryString("rmiurl");
		ShowSet.engineServiceName="TransferEngine";

	}

}
