package com.distocraft.dc5000.etl.gui.dataflow;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.repository.dwhrep.Referencetable;
import com.distocraft.dc5000.repository.dwhrep.ReferencetableFactory;
import com.distocraft.dc5000.repository.dwhrep.Tpactivation;
import com.distocraft.dc5000.repository.dwhrep.TpactivationFactory;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * This servlet shows the information contained by table from the refrencetype
 * table.
 * 
 * @author Antti Laurila
 */
public class ShowRefrenceType extends EtlguiServlet {

  private static Log log = LogFactory.getLog(ShowRefrenceType.class);

  /**
   * 
   * @see org.apache.velocity.servlet.VelocityServlet#handleRequest(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.apache.velocity.context.Context)
   */
  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) {
    Template outty = null;

    try {
      // fetch all refrencetypes
      // Vector refTypes = ReferencetypePeer.getTypesVector();
      Vector<String> refTypes = getTypes(ctx);
      ctx.put("reftypes", refTypes);

      // see if user has given some type already
      String refType =request.getParameter("rtype");
      String rls = request.getParameter("row_limit");
      String filtercolumn = request.getParameter("f_column");
      String filtertype = request.getParameter("f_type");
      String filtervalue = StringEscapeUtils.escapeHtml(request.getParameter("f_value"));
      if(request.getMethod().equalsIgnoreCase("get")){
    	  filtertype = "";
    	  filtervalue = "";
      }
      String pattern =  "^[a-zA-Z0-9-_]*$";
      
      if(refType == null){
    	  refType = "-";
      }
      if(refType.matches(pattern)){
    	  refType = StringEscapeUtils.escapeHtml(refType);
      }else{
    	  refType = "-";
      }
      
      if(filtercolumn == null){
    	  filtercolumn = "-";
      }
      if(filtercolumn.matches(pattern)){
    	  filtercolumn = StringEscapeUtils.escapeHtml(filtercolumn);
      }else{
    	  filtercolumn = null;
      }
      
      if(filtertype == null){
    	  filtertype = "-";
      }
     
      
      String pattern2 = "^[0-9]*$";
      
      if(rls == null){
      	rls = "-";
      }
      
      
      if(rls.matches(pattern2)){
      	rls = StringEscapeUtils.escapeHtml(rls);
      }else{
      	rls = null;
      }
      

      String rt2;
      Object o;
      boolean rtns = true;

      o = request.getSession(false).getAttribute("reftype");
      if (o != null) {
        rt2 = (String) o;
        if (refType != null && rt2.equals(refType)) {
          rtns = false;
        }
      }

      if (null == rls || rls.equals("")) {
        rls = "500";
      }
      int rowLimit = Integer.parseInt(rls);
      if (rowLimit < 0) {
        rowLimit = 0;
      }
      ctx.put("row_limit", String.valueOf(rowLimit));

      if (filtercolumn == null || rtns) {
        filtercolumn = "";
      }
      ctx.put("fcolumn", filtercolumn);

      if (filtertype == null || rtns) {
        filtertype = "";
      }
      ctx.put("ftype", filtertype);

      if (filtervalue == null || rtns) {
        filtervalue = "";
      }
      ctx.put("fvalue", filtervalue);

      //
      // if user has given some parameter's set them to screen
      // and fetch the ingredients of the table
      // if not - set default ones
      if (refType == null) {
        ctx.put("thetype", "-");
        request.getSession(false).setAttribute("reftype", "-");
      } else if (refType.equals("-"))  {
        ctx.put("thetype", "-");
        request.getSession(false).setAttribute("reftype", "-");
      } else {
        try {
          ctx.put("thetable", getReferenceTypeTable(ctx, refType, rowLimit, filtercolumn, filtertype, filtervalue));
        } catch (SQLException e) {
          ctx.put("errormsg", e.getMessage() + "<br>Check your filtering conditions.");
        }
        ctx.put("thetype", refType);
        request.getSession(false).setAttribute("reftype", refType);
     }

      //
      // done - release connection and let Velocity know the template
      outty = getTemplate("reftypeinfo.vm");
    } catch (ParseErrorException pee) {
      log.error("Parse error for template ", pee);
    } catch (ResourceNotFoundException rnfe) {
      log.error("Template not found ", rnfe);
    } catch (Exception e) {
      log.error("Error ", e);
    }
    return outty;
  }


  /**
   * Finds all types from te RefrenceType table.
   * 
   * @return types
   */

  private static Vector<String> getTypes(Context ctx) {
    Vector<String> retVal = new Vector<String>();

    RockFactory rock = (RockFactory) ctx.get("rockDwhRep");

    try {
      Tpactivation whereTPActivation = new Tpactivation(rock);
      whereTPActivation.setStatus("ACTIVE");
      // Get information of the activated techpacks.
      TpactivationFactory TPActivationFact = new TpactivationFactory(rock, whereTPActivation);
      Vector<Tpactivation> activatedTPs = TPActivationFact.get();
      
      Referencetable whereRef = new Referencetable(rock);
       ReferencetableFactory ref = new ReferencetableFactory(rock, whereRef,
       "ORDER BY TYPENAME");
      //ReferencetableFactory ref = new ReferencetableFactory(rock, whereRef);
      Vector<Referencetable> dbVec = ref.get();

      if (dbVec.size() > 0) {
        for (int i = 0; i < dbVec.size(); i++) {
          Referencetable referencetype = (Referencetable) dbVec.elementAt(i);

          if (referencetype.getTypename().endsWith("_CURRENT_DC")) {
            // Don't include the tables that end with "_CURRENT_DC".
          } else {
            
            for(int j = 0; j < activatedTPs.size(); j++) {
              Tpactivation currTPAct = (Tpactivation)activatedTPs.elementAt(j);
              if(currTPAct.getVersionid().equalsIgnoreCase(referencetype.getVersionid())) {
                retVal.add(referencetype.getTypename());
              }
            }
          }
        }
      }

    } catch (Exception e) {
      log.error("Excepted: ", e);
    }
    return retVal;
  }

  /**
   * Selects one reference table from the data DB.
   * 
   * @param type
   * @param rowLimit
   * @param filtercolumn
   * @param filtertype
   * @param filtervalue
   * @return referencetypetable
   */
  private List<List<String>> getReferenceTypeTable(Context ctx, String type, int rowLimit, String filtercolumn, String filtertype,
      String filtervalue) throws SQLException {
    Statement stmt = null;
    ResultSet rset = null;
    List<List<String>> retVal = new ArrayList<>();
    // SQLException errorStr = null;

    // this is connection to the IQ data DB
    Connection conn = ((RockFactory) ctx.get("rockDwh")).getConnection();

    String sqlf;
    String filtertypes[] = { "", "=", "< (string)", "<= (string)", "> (string)", ">= (string)", "begins", "ends",
        "contains" };
    int filtertypeindex = 0;

    for (filtertypeindex = 0; filtertypeindex < filtertypes.length; filtertypeindex++) {
      if (filtertypes[filtertypeindex].equals(filtertype)) {
        break;
      }
    }
    switch (filtertypeindex) {
    case 1: // '=' 
    	sqlf = "string(" + filtercolumn + ") = '" + filtervalue + "' ";
    	break;
    case 2: // '< (string)' flow down
    case 3: // '<= (string)' flow down
    case 4: // '> (string)' flow down
    case 5: // '>= (string)'
      sqlf = " string(" + filtercolumn + ") " + filtertype.substring(0, 2) + " '" + filtervalue + "' ";
      break;
    case 6: // begins
      sqlf = " string(" + filtercolumn + ") like '" + filtervalue + "%' ";
      break;
    case 7: // ends
      sqlf = " string(" + filtercolumn + ") like '%" + filtervalue + "' ";
      break;
    case 8: // contains
      sqlf = " string(" + filtercolumn + ") like '%" + filtervalue + "%' ";
      break;

    // filtering below is for INTEGERS
    case 9: // '< (integer)' flow down
    case 10: // '<= (integer)' flow down
    case 11: // '> (integer)' flow down
    case 12: // '>= (integer)'
      sqlf = " " + filtercolumn + " " + filtertype.substring(0, 2) + " " + filtervalue + " ";
      break;

    // DEFAULT dont filter
    default:
      sqlf = "";
    }

    if (!sqlf.equals("") && !filtercolumn.equals("") && !filtervalue.equals("")) {
      sqlf = " where " + sqlf;
    } else {
      sqlf = "";
    }

    sqlf = "SELECT * FROM " + type + sqlf;
    log.debug("Executing sql: '" + sqlf + "'");

    try {

      stmt = conn.createStatement();
      stmt.setMaxRows(rowLimit); // PRa 17.11.2004
      rset = stmt.executeQuery(sqlf);

      // fecth the column names
      Vector<String> vr = new Vector<String>();
      ResultSetMetaData rsmd = rset.getMetaData();
      int j = rsmd.getColumnCount() + 1;
      
      int latitude=0;
      int longitude=0;
    
      for (int i = 1; i < j; i++){
    	  	  
    	if(rsmd.getColumnName(i).equals("LATITUDE")){
    		latitude=i;
    	}
    	if(rsmd.getColumnName(i).equals("LONGITUDE")){
    		longitude=i;
    	}

        vr.add(rsmd.getColumnName(i));
      }
      retVal.add(vr);

      // fecth all the data

      while (rset.next()) {
  		List<String> v = new ArrayList<>();
		 for (int i = 1; i < j; i++) {
			 Optional<String> value = Optional.ofNullable(rset.getString(i));
			 if (value.isPresent()) {
				   if (i == latitude) {
					   processLatitudeValue(v, value.get());
					   
				   }else if (i == longitude) {
					   processLongitudeValue(v, value.get());
					   
				   }else {
					   v.add(value.get());
				   }
			 }else {
				 v.add("-");
			 }
		 }
			retVal.add(v);
			
		}

      // Commit this statement if needed (method called will
      // determine that)
      // DwhMonitoring.commitConnection(conn);

    } catch (SQLException e) {
      log.error("SQLException", e);
      throw e;
    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      try {
        if (rset != null) {
          rset.close();
        }
      } catch (Throwable e) {
        // catch all - we just want to make sure that we close
        // the DB connection, otherwise we run out of them
        log.error("Exception", e);
      }

      try {
        // commit
        if (conn != null) {
          conn.commit();
        }
        if (stmt != null) {
          stmt.close();
        }
      } catch (Exception e) {
        log.error("Exception", e);
      }
    }

    return retVal;
  }
  
  /**
   * @author xwojohb - TR HN64274 - 01.08.2011
   * Convert the DB value to show latitude/longitude followed by a letter indicating the direction
   * @param value
   * @param s ( Direction N/S/E/W )
   * @return the converted value
   */
  private String conValue(float value,String s){
	String result;
	value=Math.abs(value/3600000); //divided value by Hour(in milliseconds) & get abs value
	result=value+s;
   	return result;
  }
  private void processLatitudeValue(List<String> v, String value) {
		try {
			float floatValue = Float.parseFloat(value);
			if (floatValue<0) {
				v.add(conValue(floatValue, "S"));
				
			}else {
				v.add(conValue(floatValue, "N"));
			}
			}catch(NumberFormatException e) {
				v.add(value);
			}
	}

private void processLongitudeValue(List<String> v, String value) {
		try {
			float floatValue = Float.parseFloat(value);
			if (floatValue<0) {
				v.add(conValue(floatValue, "W"));
			}else {
				v.add(conValue(floatValue, "E"));
			}
			}catch(NumberFormatException e) {
				v.add(value);
			}
	}
}
