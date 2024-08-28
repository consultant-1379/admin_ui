package com.distocraft.dc5000.etl.gui.dataflow;

import com.distocraft.dc5000.etl.gui.common.CalSelect;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.common.MeasLineCounter;
import com.distocraft.dc5000.etl.gui.common.MeasLines;
import com.distocraft.dc5000.etl.gui.common.MeasResultSet;
import com.distocraft.dc5000.etl.gui.monitor.Util;
import com.distocraft.dc5000.repository.dwhrep.Measurementkey;
import com.distocraft.dc5000.repository.dwhrep.MeasurementkeyFactory;
import com.distocraft.dc5000.repository.dwhrep.Measurementtype;
import com.distocraft.dc5000.repository.dwhrep.MeasurementtypeFactory;
import com.distocraft.dc5000.repository.dwhrep.Tpactivation;
import com.distocraft.dc5000.repository.dwhrep.TpactivationFactory;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

public class DataRowSummary extends EtlguiServlet {
	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(super.getClass());

	

  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
    throws Exception  {
    Template outty = null;
    String page = "datarowsummary.vm";
    ResultSet result = null;
    MeasLineCounter meas = null;
    MeasResultSet measResult = null;
    MeasLines measLines = null;
    Date currentTime = new Date();

    DbCalendar calendar = new DbCalendar();
    String yearStr = null;
    String monthStr = null;
    String dayStr = null;
    String searchDaysStr = null;
    String searchStr = null;
    String searchDirection = null;
    String searchDone = null;
    String level  = null;
    String date = null;
    String meastype = null;
    String requesttype = null;
    String mlevel = null;
    String tp_name = null;
    int yearInt = 0;
    int monthInt = 0;
    int dayInt = 0;
    int daysSearchedInt = 0;
    boolean searchBackward = true;
    try    {
      calendar.setTime(currentTime);

      yearStr = request.getParameter("year_1");
      monthStr = request.getParameter("month_1");
      dayStr = request.getParameter("day_1");
      searchDaysStr = request.getParameter("search_days");
      searchDirection = request.getParameter("search_direction");
      searchDone = request.getParameter("search_done");
      searchStr = request.getParameter("search_string");
      level = request.getParameter("dlevel");
      date = request.getParameter("dayStr");
      meastype = request.getParameter("meastype");
      requesttype = request.getParameter("request_type");  
      tp_name = request.getParameter("search_str");
      HttpSession session = request.getSession(false);
      Connection connDRep = ((RockFactory)ctx.get("rockDwhRep")).getConnection();
      Connection connDwh = ((RockFactory)ctx.get("rockDwh")).getConnection();     
      





      if (requesttype != null) {       
        if (requesttype.equals("counts")) {
          mlevel =level;
          this.log.debug("request type equals counts"+tp_name);
          ctx.put("mlevel", level);          
          List<List<String>> lateRaw = getMeasurementCount(ctx,tp_name,date, meastype, mlevel, connDwh);
          ctx.put("meastype", meastype);          
          ctx.put("datatime", date);
          ctx.put("sourcedata", lateRaw);
          page = "sourcecount.vm";
          this.log.debug("page will be source count");
        }
      }
      if (yearStr != null) {
        session.setAttribute("year", yearStr);
      }
      else if (session.getAttribute("year") != null)
        yearStr = session.getAttribute("year").toString();
      else {
        session.setAttribute("year", calendar.getYearString());
      }
      if (monthStr != null) {
        session.setAttribute("month", monthStr);
      }
      else if (session.getAttribute("month") != null)
        monthStr = session.getAttribute("month").toString();
      else {
        session.setAttribute("month", calendar.getMonthString());
      }

      if (dayStr != null)
        session.setAttribute("day", dayStr);
      else if (session.getAttribute("day") != null)
        dayStr = session.getAttribute("day").toString();
      else {
        session.setAttribute("day", calendar.getDayString());
      }

      if (searchStr != null)
        session.setAttribute("tpName", searchStr);
      else if (session.getAttribute("tpName") != null)
        searchStr = session.getAttribute("tpName").toString();
      else {
        session.setAttribute("tpName", "-");
      }

      ctx.put("backward", ((searchDirection != null) && (searchDirection.equals("backward"))) ? "checked" : " ");
      ctx.put("forward", ((searchDirection != null) && (searchDirection.equals("forward"))) ? "checked" : " ");

      if (searchDirection == null) {
        ctx.put("backward", "checked");
      }

      if (searchStr == null) {
        ctx.put("theGroup", "-");
        searchStr = null;
      } else if (searchStr.equals("-")) {
        ctx.put("theGroup", "-");
        searchStr = null;
      } else {
        ctx.put("theGroup", searchStr);
      }

      List<String> tps = Util.getTechPacks(connDRep);
      final Vector<String> table_levels = new Vector<String>();
      table_levels.add("RAW");
      table_levels.add("DAY");
      table_levels.add("DAYBH");
      table_levels.add("COUNT");
      ctx.put("selectedTechpack", searchStr);
      ctx.put("techpacks", tps);    
      ctx.put("level",level);
      ctx.put("theLevels", table_levels);					
      try      {
        if (yearStr == null) {
          yearStr = calendar.getYearString();
          monthStr = calendar.getMonthString();
          dayStr = calendar.getDayString(); }
        if (searchDone != null){
          if (searchStr == null) {
            searchStr = "";
          }
          if (searchDaysStr == null) {
            searchDaysStr = "31";
          }
          yearInt = Integer.parseInt(yearStr);
          monthInt = Integer.parseInt(monthStr);
          dayInt = Integer.parseInt(dayStr);
          daysSearchedInt = Integer.parseInt(searchDaysStr);

          if (searchDirection.equals("backward"))
            searchBackward = true;
          else if (searchDirection.equals("forward")) {
            searchBackward = false;
          }
          else {
            this.log.error("DataRowSummary :: Critical error as search direction was not specified.");
          }
          meas = new MeasLineCounter(connDwh, connDRep);
          measLines = new MeasLines(meas.getMeasLineCount(), daysSearchedInt, yearInt, monthInt, dayInt, searchBackward);
          result = meas.getMeasTypes(searchStr, level);
          String allMeasTypes = "";
          StringBuilder sb= new StringBuilder();
          List<String> measType = new ArrayList<String>();
          if(result.next()){
          do{
        	  //appending all the measurement types to a single string.
        	  if(level.equalsIgnoreCase("RAW")){
        		  sb.append( "'"+result.getString("typename")+"',");
        		  measType.add(result.getString("typename"));
        	  }else if(level.equalsIgnoreCase("DAYBH")) {
        		  sb.append( "'"+result.getString("typename")+"' ,");
        	  } else {
        		  sb.append( "'"+result.getString("typename")+"_"+level+"' ,");
            	  measType.add(result.getString("typename")+"_"+level);
        	  }
          }while (result.next());
          
          allMeasTypes = sb.toString();
          allMeasTypes = allMeasTypes.substring(0, allMeasTypes.length()-1);
    	  if(level.equalsIgnoreCase("daybh")){
    		  allMeasTypes = handleDayBH(connDwh, sb, allMeasTypes, measType);
          }
          this.log.debug("DataRowSummary: Appending measurement types: Measurement typess :  "+allMeasTypes.replaceAll("[\r\n]",""));
          if (allMeasTypes.length() > 0){
        	  measResult = meas.getMeasLines(yearInt, monthInt, dayInt, daysSearchedInt, allMeasTypes, level, searchBackward, measType);
        	  measLines.addLines(measResult);
          }
         }

          ctx.put("measArray", measLines.getMeasArray());
          ctx.put("daysArray", measLines.getDaysArray());
          ctx.put("lineCountArray", measLines.getLineCountArray());
          ctx.put("countrow", measLines.getRopCountArray());
          ctx.put("search_days", searchDaysStr);
          ctx.put("search_string", searchStr);
          ctx.put("search_done", "true");
        }

        Connection conn = ((RockFactory)ctx.get("rockDwh")).getConnection();
        
        CalSelect calSelect = new CalSelect(conn);
        ctx.put("validYearRange", calSelect.getYearRange());
        ctx.put("year_1", yearStr);
        ctx.put("month_1", monthStr);
        ctx.put("day_1", dayStr); 

      
      }catch (Exception e) {

        this.log.error("DataRowSummary :: Error while getting Measurement Value " + e );

      }
      finally {
    	  if(connDRep != null)
    		  connDRep.close();
    	  if(connDwh != null)
    		  connDwh.close();
      }
      this.log.info("DataRowSummary :: Context ready for Velocity template.");      
      outty = getTemplate(page);
    }
    catch (ParseErrorException pee)   {
      this.log.error("DataRowSummary :: Parse error for template: " + pee);
    }
    catch (ResourceNotFoundException rnfe)    {
      this.log.error("DataRowSummary :: Template not found: " + rnfe);
    }
    catch (SQLException sqle)    {
      this.log.error("DataRowSummary :: Failed to go through the measurement type ResultSet: " + sqle);
    }
    catch (Exception e)    {
      this.log.error("DataRowSummary :: Error: " + e.getMessage());
    }
    return outty;
  }

	private String handleDayBH(Connection connDwh, StringBuilder sb, String allMeasTypes, List<String> measType) {
		String sqlDayBH = "SELECT distinct Aggregation from Log_AggregationRules where target_level='DAYBH' and AggregationScope='DAY' and target_type in ("
				+ allMeasTypes + ")";
		try(PreparedStatement stmtDwhdb = connDwh.prepareStatement(sqlDayBH);ResultSet aggTypeName = stmtDwhdb.executeQuery(); ) { // NOSONAR
			sb = new StringBuilder();
			while (aggTypeName.next()) {
				String typeName = aggTypeName.getString(1);
				sb.append("'" + typeName + "' ,");
				measType.add(typeName);
			}
			allMeasTypes = sb.toString();
			if (allMeasTypes.length() > 0) {
				allMeasTypes = allMeasTypes.substring(0, allMeasTypes.length() - 1);
			}
		} catch (SQLException e) {
			log.warn("SQL Exception in DAYBH");
		}
		return allMeasTypes;
	}
//Added for Drill Down Functionality EQEV-5565
  private List<List<String>> getMeasurementCount(Context ctx,String tp_name,String date, String meastype, String mlevel, Connection conn) throws SQLException, RockException  {
    List<List<String>> drillDownValue = new ArrayList<List<String>>();
    Statement statement = null;
    ResultSet result = null;
    String dataTimeBegin = date + " 00:00:00.000";
    String dataTimeEnd = date + " 23:59:59.999";
    String sql;
    String level = mlevel;
    String key_column =null;
    final RockFactory rock = (RockFactory) ctx.get("rockDwhRep");
    final Integer i = new Integer(1);
    // Get versionId of tech pack
    final Tpactivation whereTP = new Tpactivation(rock);
    whereTP.setTechpack_name(tp_name);
    whereTP.setStatus("ACTIVE");
    final TpactivationFactory ref = new TpactivationFactory(rock, whereTP);
    final List<Tpactivation> version_id = ref.get();
    if (version_id == null || version_id.size() == 0) {
        log.warn("Could not find any data for" + tp_name);
    } else {
        final Tpactivation v_id = (Tpactivation) version_id.get(0);
        String version = v_id.getVersionid();
        // typeId for measurement type
        final Measurementtype whereMea = new Measurementtype(rock);
        //whereMea.setTypename(meastype)
        switch (mlevel)
        {
        case "RAW" : whereMea.setTypename(meastype);
        			 break;
        case "DAY" : whereMea.setTypename(meastype.substring(0,meastype.length() -4));
        			  break;
        case "DAYBH" : break;
        case "COUNT" : whereMea.setTypename(meastype.substring(0,meastype.length() -6));
                                       break;
         default : log.warn("Invalid measurement level" +mlevel);                             
        }	 
        whereMea.setVersionid(version);
        final MeasurementtypeFactory refMea = new MeasurementtypeFactory(rock, whereMea);
        final List<Measurementtype> type_id = refMea.get();
        if (type_id == null || type_id.size() == 0) {
            log.warn("Measurementtype " + meastype + " did not have version " + version);
        } else {
            final Measurementtype t_id = (Measurementtype) type_id.get(0);
            final String type = t_id.getTypeid();            
            final Measurementkey whereRef = new Measurementkey(rock);
            whereRef.setIselement(i);
            whereRef.setTypeid(type);
            final MeasurementkeyFactory refKey = new MeasurementkeyFactory(rock, whereRef);
            final List<Measurementkey> keys = refKey.get();
            if (keys == null || keys.size() == 0) {
                log.warn("Measurementkey " + type + " did not have typeID " + i);
            }
            else 
             {
                final Measurementkey key = (Measurementkey) keys.get(0);
                key_column = key.getDataname();
        }
               
        }
    }
    log.debug("key:" + key_column);
    
    if(level.equalsIgnoreCase("RAW")) {  
    	sql = "SELECT  DATEFORMAT(DATATIME,'yyyy-mm-dd hh:mm:ss') AS DATATIME, SUM(ROWCOUNT),COUNT(DISTINCT SOURCE) AS SOURCECOUNT FROM LOG_SESSION_LOADER " +
    		" WHERE DATATIME BETWEEN '" + dataTimeBegin + "' AND '" + dataTimeEnd + "'AND TYPENAME = '" + meastype + "' GROUP BY DATATIME,ROWCOUNT";
      }else if(level.equalsIgnoreCase("DAYBH")){
    	  String typeName = getTypeNameForMeasType(conn, meastype);
      	sql = "SELECT  DATE_ID, COUNT(DATE_ID) ,COUNT(DISTINCT "+key_column+")  FROM " +typeName +
      	  		" WHERE DATE_ID = '" + date + "' GROUP BY  "+key_column+", DATE_ID";
      }else   {
    	sql = "SELECT  DATE_ID, COUNT(DATE_ID) ,COUNT(DISTINCT "+key_column+")  FROM " +meastype +
    	  		" WHERE DATE_ID = '" + date + "' GROUP BY  "+key_column+", DATE_ID";

    	}
    this.log.debug("DataRowSummary :: Executes the Query" + sql);
    try    {
      statement = conn.createStatement();
      result = statement.executeQuery(sql);      
      while (result.next()) {  
        List<String> ret = new ArrayList<String>();
        ret.add(result.getString(1)); 
        this.log.debug("DataRowSummary :: Adding result date" + result.getString(1));
        if (result.getString(2) != null) {
          ret.add(result.getString(2));
          this.log.debug("DataRowSummary :: Adding result rowcount" + result.getString(2));
        } else {
          ret.add("0");
        }
        if (result.getString(3) != null) {
          ret.add(result.getString(3));
          this.log.info("DataRowSummary :: Adding result element count" + result.getString(3));
        } else {
          ret.add("0");
        }
        drillDownValue.add(ret);
      }
      conn.commit();
    }
    catch (Exception e) {
      this.log.error("Exception: ", e);
    }
    finally {
      try {
        if (result != null) {
          result.close();         
        }
        if (statement != null)
          statement.close();
      }
      catch (SQLException e) {
        this.log.error("SQLException: ", e);
      }
    }
    
    return drillDownValue;
  }

private String getTypeNameForMeasType(Connection conn, String meastype) {
	String sqlDwhdb = "select top 1 target_table from Log_AggregationRules where aggregation = ?";
	ResultSet aggTypeName = null;
	String typeName = "";
	try(PreparedStatement stmtDwhdb = conn.prepareStatement(sqlDwhdb);) {
		stmtDwhdb.setString(1, meastype);
		aggTypeName = stmtDwhdb.executeQuery();
		while(aggTypeName.next())
			typeName = aggTypeName.getString(1);
	} catch (SQLException e) {
		log.warn("SQL Exception in DAYBH");
	} finally{
		if(aggTypeName != null)
			try {
				aggTypeName.close();
			} catch (SQLException e) {
				log.warn("Error while closing connections in data row Summary class");
			}
	}
	return typeName;
}

  

}