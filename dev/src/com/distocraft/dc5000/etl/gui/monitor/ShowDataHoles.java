package com.distocraft.dc5000.etl.gui.monitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.CalSelect;
import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved. <br>
 * Servlet for showing dataholes. <br>
 * dc5000dwh database is used.
 * 
 * @author Perti Raatikainen
 * @author Antti Laurila
 * @author Mark Stenback
 */
public class ShowDataHoles extends EtlguiServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 229668202519457332L;
  
  private final transient Log log = LogFactory.getLog(this.getClass());
  //private static final SimpleDateFormat sdf_nosecs = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  //private static final SimpleDateFormat sdf_secs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  /**
   * @see org.apache.velocity.servlet.VelocityServlet#handleRequest(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.apache.velocity.context.Context)
   */
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response, final Context ctx)  {
    Template outty = null;
    String page = "holeresult.vm";

    final RockFactory rockDwhRep = (RockFactory) ctx.get("rockDwhRep");
    final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh"); 

    final HttpSession session = request.getSession(false);

    // get the current date (is used at the UI, if none given)
    final DbCalendar calendar = new DbCalendar();

    // check if user has given any parameters
    // day parameters
    String year_1 = request.getParameter("year_1");
    String month_1 = request.getParameter("month_1");
    String day_1 = request.getParameter("day_1");
    String subType = request.getParameter("subtype");
    String type = request.getParameter("type");
    String details = request.getParameter("details");
    String problematic = request.getParameter("problematic");

    final String button = request.getParameter("value");

    final String rtype = request.getParameter("request_type");
    final String source_param = request.getParameter("source");
    final List<Object> sourceCounts = new ArrayList<Object>();

    try {

      // Vector sqlCommands;

      if (year_1 != null) {
        session.setAttribute("year", year_1);
      } else if (session.getAttribute("year") != null) {
        year_1 = session.getAttribute("year").toString();
      } else {
        session.setAttribute("year", calendar.getYearString());
      }
      
      if (month_1 != null) {
        session.setAttribute("month", month_1);
      } else if (session.getAttribute("month") != null) {
        month_1 = session.getAttribute("month").toString();
      } else {
        session.setAttribute("month", calendar.getMonthString());
      }
      
      if (day_1 != null) {
        session.setAttribute("day", day_1);
      } else if (session.getAttribute("day") != null) {
        day_1 = session.getAttribute("day").toString();
      } else {
        session.setAttribute("day", calendar.getDayString());
      }
      
      if (type != null) {
        session.setAttribute("tpName", type);
      } else if (session.getAttribute("tpName") != null) {
        type = session.getAttribute("tpName").toString();
      } else {
        session.setAttribute("tpName", "-");
      }
      
      if (details == null) {
        details = "";
      }

      if (subType == null) {
        subType = "";
      }
      
      ctx.put("subtype", subType);

      ctx.put("problematic", problematic == null ? "" : "checked");

      final String meas_group = type;

      if (meas_group == null) {
        ctx.put("theGroup", "-");
      } else {
        ctx.put("theGroup", meas_group);
      }
      final List<String> tps = Util.getTechPacks(rockDwhRep.getConnection());
      ctx.put("distinctTechPacks", tps);

      // log.debug("Getting measurement types...");
      // Vector meaTypes = Util.getMeasurementTypes(type, connDRep);
      // ctx.put("twoLevelSelectionBuilder", meaTypes);

      // if user has given some parameter's set them to screen
      // if not - set default ones
      ctx.put("type", type);

      // sqlCommands = makeSqlCommands("'" + year_1 + "-" + month_1 + "-" +
      // day_1
      // + "'");

      log.debug("subType: " + subType);
      log.debug("type: " + type);
      log.debug("details: " + details);
      log.debug("problematic: " + problematic);
      log.debug("button: " + button);
      log.debug("rtype: " + source_param);

      if (year_1 == null) {
        year_1 = calendar.getYearString();
        month_1 = calendar.getMonthString();
        day_1 = calendar.getDayString();
      }

      if (rtype != null) {
        if (rtype.equals("rowcounts")) {
          log.debug("Getting source counts.");
          getSourceRowCount(ctx, year_1, month_1, day_1, subType, details, sourceCounts);
          page = "sourcerowcount.vm";
        } else if (rtype.equals("show_loaded_types")) {
          log.debug("Getting loaded measurement types.");
          getLoadedMeasurementTypes(ctx, year_1, month_1, day_1, details, source_param, sourceCounts);
          page = "loadedmeasurementypes.vm";
        }
      }

      if (button != null) {
        log.debug("Finding holes");
        problematic = "true";
        
        final List<?> lateRaw = findHoles(year_1, month_1, day_1, type, problematic, null, rockDwh.getConnection(), rockDwhRep.getConnection());

        String max = "unknown";
        if (lateRaw.size() > 0) {
          max = (String) lateRaw.remove(0);
        }
        ctx.put("maxtime", max);

        ctx.put("lateraw", lateRaw);
        // page = "showloadings.vm";
        page = "holeresult.vm";
      }

      if (details != null && !details.equals("") && rtype == null) {
        log.debug("Getting measurement type details.");

        final List<List<String>> lateRaw = getMeasurementTypeDetails(year_1, month_1, day_1, details, subType, rockDwh.getConnection());

        // Vector lateRaw = findHoles(year_1, month_1, day_1, subType, null,
        // details, details, conn, connDRep);

        final String max = "unknown";
        ctx.put("maxtime", max);
        ctx.put("lateraw", lateRaw);
        ctx.put("timelevel", details);
        page = "holedetails.vm";
      }
      /*
       * else { // getLoadedMeasurementTypes(ctx, year_1, month_1, day_1, "",
       * type, // sourceCounts); Vector lateRaw = findHoles(year_1, month_1,
       * day_1, type, null, "%", subType, conn); ctx.put("maxtime", "unknown");
       * ctx.put("lateraw", lateRaw.get(1)); page = "holedetails.vm"; }
       */

      //This sends a vector of valid years from DIM_DATE Table.
      //This is used by cal_select_1.vm
      final CalSelect calSelect = new CalSelect(rockDwh.getConnection());
      ctx.put("validYearRange", calSelect.getYearRange());
      
      ctx.put("year_1", year_1);
      ctx.put("month_1", month_1);
      ctx.put("day_1", day_1);

      outty = getTemplate(page);
    } catch (ResourceNotFoundException e) {
      log.debug("ResourceNotFoundException (getTemplate): " + e);
    } catch (ParseErrorException e) {
      log.debug("ParseErrorException (getTemplate): " + e);
    } catch (Exception e) {
      log.debug("Exception (getTemplate): " + e);
    }
    return outty;
  }

  /**
   * Get measurement type details
   * 
   * @param year_1
   * @param month_1
   * @param day_1
   * @param subType
   * @param conn
   * @return
   */

  private List<List<String>> getMeasurementTypeDetails(final String year_1, final String month_1, final String day_1, final String details, final String subType,
      final Connection conn) {

    final List<List<String>> mtd = new ArrayList<List<String>>();

    Statement statement = null; // sql statement
    ResultSet result = null; // resultset

    final String sql = "SELECT DATEFORMAT(datatime,'yyyy-mm-dd hh:nn:ss') as datatime, rowcount, sourcecount, status, "
        + "DATEFORMAT(modified,'yyyy-mm-dd hh:nn:ss') as modified, description FROM LOG_LoadStatus "
        + "WHERE typename = '" + subType + "' and timelevel = '" + details + "' and datadate = '" + year_1 + "-"
        + month_1 + "-" + day_1 + "' ORDER BY datatime";

    try {
      statement = conn.createStatement();

      log.debug(sql);

      result = statement.executeQuery(sql);

      // parse search result
      while (result.next()) {
        final Vector<String> ret = new Vector<String>();
        ret.add(result.getString(1));
        //ret.add(sdf_secs.format(result.getTimestamp(1)));

        if (result.getString(2) != null) {
          ret.add(result.getString(2));
        } else {
          ret.add("0");
        }
        if (result.getString(3) != null) {
          ret.add(result.getString(3));
        } else {
          ret.add("0");
        }

        ret.add(result.getString(4));

        if (result.getString(5) != null) {
          ret.add(result.getString(5));
          //ret.add(sdf_secs.format(result.getTimestamp(5)));
        } else {
          ret.add("n/a");
        }

        if (result.getString(6) != null) {
          ret.add(result.getString(6));
        } else {
          ret.add("n/a");
        }

        mtd.add(ret);
      }

      // commit
      conn.commit();

    } catch (Exception e) {
      log.error("Exception: ", e);
    } finally {
      // finally clean up
      try {
        if (result != null) {
          result.close();
        }
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        log.error("SQLException: ", e);
      }
    }

    return mtd;
  }

  /**
   * @param ctx
   * @param year_1
   * @param month_1
   * @param day_1
   * @param timeLevel
   * @param source
   * @param sourceCounts
   */

  private void getLoadedMeasurementTypes(final Context ctx, final String year_1, final String month_1, final String day_1, final String timeLevel,
      final String source, final List<Object> sourceCounts) {
    final String dataTime = year_1 + "-" + month_1 + "-" + day_1;

    ctx.put("details", timeLevel);
    ctx.put("datatime", dataTime);

    final String sqlStr = "select typename, count(*) nrows, count(distinct datatime), sum(rowcount) "
        + "from LOG_SESSION_LOADER where source = '" + source + "' and datadate = '" + dataTime + "' group by typename";

    log.debug("SQL Statement executed: '" + sqlStr + "'");
    final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh");
    //final Connection conn = rockDwh.getConnection();

    Statement statement = null;
    ResultSet result = null;
    try {
      statement = rockDwh.getConnection().createStatement();
      result = statement.executeQuery(sqlStr);

      // parse search result
      while (result.next()) {
        final String date = dataTime;
        final String typename = result.getString(1);
        final int rowcount = result.getInt(2);
        final int datatimes = result.getInt(3);
        final int rowsum = result.getInt(4);

        log.debug("Source '" + source + "' : rowcount=" + rowcount + " : rowsum=" + rowsum);
        sourceCounts.add(new MeasurementType(date, timeLevel, source, typename, rowcount, datatimes, rowsum));
      }

      ctx.put("source_rowcount", sourceCounts);
      ctx.put("source", source);

    } catch (SQLException ex) {
      log.error("SQLException: " + ex);
      log.error("Executed SQL was: '" + sqlStr + "'");
    } catch (Exception ex) {
      log.error("Exception: " + ex);
      log.error("Executed SQL was: '" + sqlStr + "'");
    } finally {
      try {
        rockDwh.getConnection().commit();

        if (statement != null) {
          statement.close();
        }
        if (result != null) {
          result.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
    }
  }

  /**
   * @param ctx
   * @param year_1
   * @param month_1
   * @param day_1
   * @param subType
   * @param timeLevel
   * @param sourceCounts
   */
  private void getSourceRowCount(final Context ctx, final String year_1, final String month_1, final String day_1, final String subType,
      final String timeLevel, final List<Object> sourceCounts) {

    final String dataTime = year_1 + "-" + month_1 + "-" + day_1;
    final String dataTimeBegin = dataTime + " 00:00:00.000";
    final String dataTimeEnd = dataTime + " 23:59:59.999";

    ctx.put("details", timeLevel);
    ctx.put("datatime", dataTime);

    final String sqlStr = "select source, count(*) nrows, count(distinct datatime), sum(rowcount) "
        + "from LOG_SESSION_LOADER where DataTime BETWEEN '" +dataTimeBegin +"' AND '" + dataTimeEnd + "' "
        +"AND typeName like '" + subType + "' and TimeLevel ='" + timeLevel + "' group by source";
    /*
     * + "' group by source union all select distinct source, 0, 0, 0 from
     * LOG_SESSION_LOADER " + "where typeName like '" + subType + "' and
     * TimeLevel ='" + timeLevel + "' and DataTime between date('" + dataTime +
     * "')-30 and date('" + dataTime + "')+30 and " + "source not in (select
     * source from LOG_SESSION_LOADER where typeName like '" + subType + "' and
     * TimeLevel ='" + timeLevel + "' and date(DataTime) ='" + dataTime + "')
     * order by 1";
     */
    // create connection to PM DATA
    final RockFactory rockDwh = (RockFactory) ctx.get("rockDwh");

    Statement statement = null;
    ResultSet result = null;

    try {
      statement = rockDwh.getConnection().createStatement();
      result = statement.executeQuery(sqlStr);

      // ................................................... parse search
      // result
      while (result.next()) {
        final String source = result.getString(1);
        final int rowcount = result.getInt(2);
        final int datatimes = result.getInt(3);
        final int rowsum = result.getInt(4);

        log.debug("Source: '" + source + "', rowcount" + rowcount);
        sourceCounts.add(new SourceRowcount(source, rowcount, datatimes, rowsum));
      }

      ctx.put("source_rowcount", sourceCounts);
    } catch (SQLException e) {
      log.error("SQLException: " + e);
      log.error("When executing statement: '" + sqlStr + "'");
    } finally {
      try {
        rockDwh.getConnection().commit();
        if (statement != null) {
          statement.close();
        }
        if (result != null) {
          result.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
    }
  }

  /**
   * Get active measurement types
   * 
   * @param typeName
   * @param connDRep
   * @return String "AND typename IN ('MEATYPE1','MEATYPE2')"
   */

  /*private String getActiveMeasurementTypes(String typeName, Connection connDRep) {

    String meaTypeList = "AND typename IN (";

    List ll = new ArrayList();

    // if tech pack is selected get all tech packs active meatypes
    // else get all measurement types which

    log.debug("Typename length:" + typeName.length());

    if (typeName != null && !typeName.equals("") && typeName.length() > 1) {
      ll = Util.getMeasurementTypes(typeName, connDRep);
    } else {
      ll = Util.getAllMeasurementTypes(connDRep);
    }

    int listLength = ll.size();

    log.debug("List size: " + ll.size());

    if (listLength > 0) {

      ListIterator li = ll.listIterator();

      for (int i = 0; i < ll.size(); i++) {
        meaTypeList += "'" + li.next().toString() + "'";
        meaTypeList += ",";
      }
      meaTypeList = meaTypeList.substring(0, meaTypeList.length() - 1);
    } else {
      return "";
    }

    meaTypeList += ")";

    return meaTypeList;

  }*/

  /*
  private Vector getLoadStatuses(String date, String techpackName, Connection conn, Connection connDRep) {

    Vector result = new Vector();

    log.debug("Type :" + techpackName);

    String meaTypeList = getActiveMeasurementTypes(techpackName, connDRep);

    // do we search only "problematic" values
    String beginTime = date + " 00:00:00";
    String endTime = date + " 23:59:59";

    String sql = "SELECT typename, timelevel, status, DATEFORMAT(datatime,'yyyy-mm-dd hh:nn:ss') as datatime FROM log_loadstatus "
      + "WHERE datatime BETWEEN '" + beginTime + "' AND '" + endTime + "' " 
      + meaTypeList + " ORDER BY typename, timelevel, datatime";

    Statement stmt = null;
    ResultSet rset = null;

    try {
      // make status query to the monitoring tables
      stmt = conn.createStatement();
      log.debug("Executing query: " + sql);
      rset = stmt.executeQuery(sql);

      while (rset.next()) {
        result.add(rset.getString(1));
        result.add(rset.getString(2));
        result.add(rset.getString(3));
        result.add(rset.getString(4).substring(11, 13));
      }

    } catch (SQLException e) {

    } finally {
      try {
        if (rset != null) {
          rset.close();
        }
        if (stmt != null) {
          stmt.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
    }

    return result;
  }*/

  /**
   * Problematic: Gets all typenamerows, which have some problems, eg.
   * not_loaded or hole
   * 
   * @param year
   * @param month
   * @param day
   * @param typeName
   * @param problematic
   * @param timeLevel
   * @param details
   * @param conn
   * @return
   */
  private List<?> findHoles(final String year, final String month, final String day, final String typeName, final String problematic, final String details,
      final Connection conn, final Connection connDRep) {
    final List<Object> result = new ArrayList<Object>();
    final String maxSql = "SELECT DATEFORMAT(MAX(modified),'yyyy-mm-dd hh:nn:ss') AS modified FROM log_loadstatus";
    String probFilter = ""; // for filtering problematic values

    log.debug("Type :" + typeName);
    String meaTypeList = "AND typename IN (";

    List<String> ll = new ArrayList<String>();

    // if tech pack is selected get all tech packs active meatypes
    // else get all measurement types which

    log.debug("Typename length:" + typeName.length());

    if (typeName != null && !typeName.equals("") && typeName.length() > 1) {
      ll = Util.getMeasurementTypes(typeName, connDRep);
    } else {
      ll = Util.getAllMeasurementTypes(connDRep);
    }

    final int listLength = ll.size();

    log.debug("List size: " + ll.size());

    if (listLength > 0) {

      final ListIterator<String> li = ll.listIterator();

      for (int i = 0; i < ll.size(); i++) {
        meaTypeList += "'" + li.next().toString() + "'";
        meaTypeList += ",";
      }
      meaTypeList = meaTypeList.substring(0, meaTypeList.length() - 1);

      meaTypeList += ")";
    } else {
      meaTypeList = "";
    }

    // do we search only "problematic" values
    if (problematic != null && !problematic.equals("")) {
      probFilter = " AND status <> 'LOADED' AND status <> 'CALC'";
    }
    final String beginTime = year + "-" + month + "-" + day + " 00:00:00";
    final String endTime = year + "-" + month + "-" + day + " 23:59:59";
    
    final String sql = "SELECT typename, timelevel, DATEFORMAT(datatime,'yyyy-mm-dd hh:nn') as datatime, rowcount, sourcecount, status, "
        + " DATEFORMAT(modified,'yyyy-mm-dd hh:nn:ss') as modified, description "
        + " FROM log_loadstatus a " + "WHERE datatime between '" + beginTime + "' AND '" + endTime + "' "+ probFilter
        + " " + meaTypeList + " ORDER BY typename, timelevel, datatime, status";
    /*
     * if (details != null) sql = "SELECT typename, timelevel, datatime,
     * rowcount, sourcecount, status, modified, description " + "FROM
     * log_loadstatus a " + "WHERE DATE(datatime)='" + year + "-" + month + "-" +
     * day + "' AND typename = '" + typeName + "' " + " AND timelevel = '" +
     * timeLevel + "' " + meaTypeList + " ORDER BY typename, timelevel,
     * datatime";
     */
    // this is for new detail features.
    // select distinct typename, count(typename) from log_session_loader where
    // typename='DC_E_BSS_BSC' and datatime='2005-11-17 09:00:00' group by
    // typename
    // and execute statement
    log.debug(sql);
    // select maximum modifed value

    Statement stmtMax = null;
    ResultSet rsetMax = null;
    try {
      stmtMax = conn.createStatement();
      log.debug("Executing query: " + maxSql);
      rsetMax = stmtMax.executeQuery(maxSql);

      // see if we have value at the DB - if not set "last update" as unknown
      if (rsetMax.next()) {
        //Date dd = rsetMax.getTimestamp(1);
        //if (dd != null) {
        //  result.add(sdf_secs.format(dd));
        final String maxModified = rsetMax.getString(1);
        if (maxModified != null) {
          result.add(maxModified);
        } else {
          result.add("unknown");
        }
      } else {
        result.add("unknown");
      }

      stmtMax.close();
      // max query done
    } catch (SQLException e) {
      log.error("SQLException: " + e);
      log.error("Excepted SQL statement was: " + maxSql);
    } finally {
      try {
        if (rsetMax != null) {
          rsetMax.close();
        }
        if (stmtMax != null) {
          stmtMax.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
    }

    Statement stmt = null;
    ResultSet rset = null;
    try {
      // make the actual "status" query to the monitoring tables
      stmt = conn.createStatement();
      log.debug("Executing query: " + sql);
      //String tempOptionSQL = "SET TEMPORARY OPTION RETURN_DATE_TIME_AS_STRING = 'ON';";
      //log.debug("sql: " + tempOptionSQL);
      //stmt.executeQuery(sql);
      //stmt.executeQuery(tempOptionSQL);
      
      log.debug("Executing query: " + sql);
      rset = stmt.executeQuery(sql);

      //tempOptionSQL = "SET TEMPORARY OPTION RETURN_DATE_TIME_AS_STRING = 'OFF';";
      //log.debug("sql: " + tempOptionSQL);
      //stmt.executeQuery(tempOptionSQL);

      Vector<Vector<String>> typeNameVector = null;

      String lastTypeName = "";
      String lastTimeLevel = "";
      String lastDataTime = "";

      int top15 = 0;
      int top10 = 0;
      int top30 = 0;
      String topTLevel = "";
      String topTLevel10 = "";
      String topTLevel30 = "";

      boolean holeFlag = false;
      boolean notLoadedFlag = false;
      boolean checkFailedFlag = false;

      while (rset.next()) {

        // what is the typename - group them into the vectors by type names
        final String tmpTypeName = rset.getString(1);
        final String tmpTimeLevel = rset.getString(2);
        final String tmpDataTime = rset.getString(3).substring(11, 13);

        // if datatime changed
        if (!tmpDataTime.equals(lastDataTime)) {
          holeFlag = false;
          notLoadedFlag = false;
          checkFailedFlag = false;
        }

        // check if we have a new typename
        if (!tmpTypeName.equals(lastTypeName) || !tmpTimeLevel.equals(lastTimeLevel)) {
          if (typeNameVector != null) {
            result.add(typeNameVector);
          }
          typeNameVector = new Vector<Vector<String>>();
        }

        // this is actual data container
        final Vector<String> tmpVect = new Vector<String>();

        tmpVect.add(tmpTypeName);

        lastTypeName = tmpTypeName;

        tmpVect.add(tmpTimeLevel);

        lastTimeLevel = tmpTimeLevel;
        lastDataTime = tmpDataTime;

        // if any measurement type in this hour has hole status
        if (rset.getString(6).trim().startsWith("HOLE")) {
          holeFlag = true;
        }
        if (rset.getString(6).trim().startsWith("NOT_LOADED")) {
          notLoadedFlag = true;
        }
        if (rset.getString(6).trim().startsWith("CHECKFAILED")) {
          checkFailedFlag = true;
        }

        if (tmpTimeLevel.startsWith("15MIN") && details == null) {

          if (top15 <= 3) {
            if ((rset.getString(6).trim().startsWith("HOLE") || rset.getString(6).trim().startsWith("NOT_LOADED"))
                && !topTLevel.equals("") && !topTLevel.equals("HOLE")) {
              topTLevel = rset.getString(6);
            }
          }

          if (top15++ == 3) {
            if (topTLevel.equals("")) {
              topTLevel = rset.getString(6);
            }
            top15 = 0;
            //tmpVect.add(sdf_nosecs.format(rset.getTimestamp(3)));
            tmpVect.add(rset.getString(3));
            tmpVect.add(Integer.toString(rset.getInt(4)));
            tmpVect.add(Integer.toString(rset.getInt(5)));

            if (notLoadedFlag) {
              topTLevel = "NOT_LOADED";
            }
            if (checkFailedFlag) {
              topTLevel = "CHECKFAILED";
            }
            if (holeFlag) {
              topTLevel = "HOLE";
            }

            tmpVect.add(topTLevel);

            //tmpVect.add(sdf_secs.format(rset.getTimestamp(7)));
            tmpVect.add(rset.getString(7));
            topTLevel = "";
          }
        } else if (tmpTimeLevel.startsWith("10MIN") && details == null) {
          if (top10 <= 5) {
            if (rset.getString(6).trim().startsWith("HOLE") || rset.getString(6).trim().startsWith("NOT_LOADED")
                && !topTLevel.equals("") && !topTLevel.equals("HOLE")) {
              topTLevel10 = rset.getString(6);
            }
          }

          if (top10++ == 5) {
            if (topTLevel10.equals("")) {
              topTLevel10 = rset.getString(6);
            }
            top10 = 0;
            //tmpVect.add(sdf_nosecs.format(rset.getTimestamp(3)));
            tmpVect.add(rset.getString(3));
            tmpVect.add(Integer.toString(rset.getInt(4)));
            tmpVect.add(Integer.toString(rset.getInt(5)));

            if (notLoadedFlag) {
              topTLevel10 = "NOT_LOADED";
            }
            if (checkFailedFlag) {
              topTLevel10 = "CHECKFAILED";
            }
            if (holeFlag) {
              topTLevel10 = "HOLE";
            }
            tmpVect.add(topTLevel10);

            //tmpVect.add(sdf_secs.format(rset.getTimestamp(7)));
            tmpVect.add(rset.getString(7));
            topTLevel10 = "";
          }
        } else if (tmpTimeLevel.startsWith("30MIN") && details == null) {
          if (top30 <= 1) {
            if (rset.getString(6).trim().startsWith("HOLE") || rset.getString(6).trim().startsWith("NOT_LOADED")
                && !topTLevel.equals("") && !topTLevel.equals("HOLE")) {
              topTLevel30 = rset.getString(6);
            }
          }

          if (top30++ == 1) {
            if (topTLevel30.equals("")) {
              topTLevel30 = rset.getString(6);
            }
            top30 = 0;
            //tmpVect.add(sdf_nosecs.format(rset.getTimestamp(3)));
            tmpVect.add(rset.getString(3));
            tmpVect.add(Integer.toString(rset.getInt(4)));
            tmpVect.add(Integer.toString(rset.getInt(5)));

            if (notLoadedFlag) {
              topTLevel30 = "NOT_LOADED";
            }
            if (checkFailedFlag) {
              topTLevel30 = "CHECKFAILED";
            }
            if (holeFlag) {
              topTLevel30 = "HOLE";
            }

            tmpVect.add(topTLevel30);

            //tmpVect.add(sdf_secs.format(rset.getTimestamp(7)));
            tmpVect.add(rset.getString(7));
            topTLevel30 = "";
          }
        } else {
          //tmpVect.add(sdf_nosecs.format(rset.getTimestamp(3)));
          tmpVect.add(rset.getString(3));
          tmpVect.add(Integer.toString(rset.getInt(4)));
          tmpVect.add(Integer.toString(rset.getInt(5)));
          tmpVect.add(rset.getString(6));
          //tmpVect.add(sdf_secs.format(rset.getTimestamp(7)));
          tmpVect.add(rset.getString(7));
        }

        if (rset.getString(8) == null || rset.getString(8).length() < 1) {
          tmpVect.add("n/a");
        } else {
          tmpVect.add(rset.getString(8));
        }
        // same type as previous or the first one
        typeNameVector.add(tmpVect);
      }

      log.debug("All sqls processed");

      if (typeNameVector != null) {
        result.add(typeNameVector);
      }
    } catch (SQLException ex) {
      log.error("SQLException: " + ex);
      log.error("Excepted SQL statement was: " + sql);
    } catch (Exception ex) {
      log.error("Exception: " + ex);
    } finally {
      try {
        if (rset != null) {
          rset.close();
        }
        if (stmt != null) {
          stmt.close();
        }
      } catch (SQLException e) {
        log.error("SQLException", e);
      }
    }
    return result;
  }

  /**
   * Make sql commands that are needed for this servlet. format is
   * monitor_aggregation_day(" + dateString + ") etc.
   * 
   * @param dateString
   * @return sqlcommands
   */
  /*
   * public static Vector makeSqlCommands(String dateString) { Vector
   * sqlCommands = new Vector(); sqlCommands.add("monitor_day(" + dateString +
   * ")"); sqlCommands.add("monitor_aggregation_day(" + dateString + ")");
   * sqlCommands.add("monitor_aggregation_week(" + dateString + ")");
   * sqlCommands.add("monitor_aggregation_month(" + dateString + ")"); return
   * sqlCommands; }
   */
}