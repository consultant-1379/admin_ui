package com.distocraft.dc5000.etl.gui.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;

import ssc.rockfactory.RockException;
import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.util.DateFormatter;
import com.distocraft.dc5000.repository.dwhrep.Measurementkey;
import com.distocraft.dc5000.repository.dwhrep.MeasurementkeyFactory;
import com.distocraft.dc5000.repository.dwhrep.Measurementtype;
import com.distocraft.dc5000.repository.dwhrep.MeasurementtypeFactory;
import com.distocraft.dc5000.repository.dwhrep.Tpactivation;
import com.distocraft.dc5000.repository.dwhrep.TpactivationFactory;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved. <br>
 * This class is for receiving information from dwh repository. <br>
 * This class is used by com.distocraft.dc5000.etl.gui.dataflow package classes. <br>
 * <u>Class cannot be instantieted. </u>
 * */
public class DwhMonitoring {

    private static Log log = LogFactory.getLog("com.distocraft.dc5000.etl.gui.common.DwhMonitoring");

    /**
     * This method will make a commit for connection if needed.
     * 
     * @param con The connection that is commited.
     * @throws SQLException if commit fails
     */

    /**
     * @param type_name
     * @param table_level
     * @param start_time
     * @param con Connection to the DB
     * @return Vector that holds the information * @throws RockException * @throws SQLException
     */

    public static List<List<Object>> getDataRowAndKey(final Context ctx, final String tp_name_ver_type, final String type_name, final String table_level,
                                                      final String start_time, final boolean ascending, final boolean direction,
                                                      final String search_days) throws SQLException, RockException {
        List<List<Object>> return_vector = new Vector<List<Object>>();

        final Connection con = ((RockFactory) ctx.get("rockDwh")).getConnection();
        final Connection repcon = ((RockFactory) ctx.get("rockDwhRep")).getConnection();

        log.debug("Type_name: '" + type_name + "' Table_level: '" + table_level + "' Start_time: '" + start_time + "'");
        String key_column = null;

        final RockFactory rock = (RockFactory) ctx.get("rockDwhRep");

        final Integer i = new Integer(1);
        String tp_name = tp_name_ver_type.split(":")[0];;
        // Get versionId of tech pack

        final Tpactivation whereTP = new Tpactivation(rock);
        whereTP.setTechpack_name(tp_name);
        whereTP.setStatus("ACTIVE");
        final TpactivationFactory ref = new TpactivationFactory(rock, whereTP);

        final List<Tpactivation> version_id = ref.get();

        if (version_id == null || version_id.size() == 0) {
            log.info("Could not find any data for" + tp_name);
        } else {
            final Tpactivation v_id = (Tpactivation) version_id.get(0);
            final String version = v_id.getVersionid();
            // typeId for measurement type

            final Measurementtype whereMea = new Measurementtype(rock);
            whereMea.setTypename(type_name);
            whereMea.setVersionid(version);
            final MeasurementtypeFactory refMea = new MeasurementtypeFactory(rock, whereMea);
            final List<Measurementtype> type_id = refMea.get();
            if (type_id == null || type_id.size() == 0) {
                log.info("Measurementtype " + type_name + " did not have version " + version);
            } else {
                final Measurementtype t_id = (Measurementtype) type_id.get(0);
                final String type = t_id.getTypeid();
                final Measurementkey whereRef = new Measurementkey(rock);
                whereRef.setIselement(i);
                whereRef.setTypeid(type);
                final MeasurementkeyFactory refKey = new MeasurementkeyFactory(rock, whereRef);

                final List<Measurementkey> keys = refKey.get();
                if (keys == null || keys.size() == 0) {
                    log.info("Measurementkey " + type + " did not have typeID " + i);
                } else {
                    final Measurementkey key = (Measurementkey) keys.get(0);
                    key_column = key.getDataname();
                }
            }
        }

        log.debug("key:" + key_column);

        if (key_column == null) {
            // WE dont have a key in the Monitoring rules
            return_vector = getDataRowUseNOkey(type_name, table_level, start_time, con,repcon, ascending);
        } else {
            String dirSql = "";
            String date_id = getDateColumn(type_name,table_level,repcon);
            final DateFormatter df = new DateFormatter("yyyy-MM-dd");
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            df.setCalendar(start_time);
            final String stoptime = df.reverseTimeDayString(Integer.parseInt(search_days));

            if (direction) {
                dirSql = " WHERE " + date_id + " between date('" + start_time + " 00:00:00')-" + search_days + " AND date('" + start_time
                        + " 00:00:00') ";
            } else {
                dirSql = " WHERE " + date_id + " between date('" + start_time + " 00:00:00') AND date('" + stoptime + " 00:00:00') ";
            }

            final String order = (ascending ? "asc" : "desc");
            Statement stmt = null;
            ResultSet rest = null;
            try {
                stmt = con.createStatement();
                String sql = null;

                if (table_level.equalsIgnoreCase("RAW") || table_level.equalsIgnoreCase("COUNT")) {
                    sql = "select datetime_id,count(*),count(distinct " + key_column + ") from " + type_name + "_" + table_level + dirSql
                            + " group by datetime_id " + " order by datetime_id " + order;
                } else {
                    sql = "select " + date_id + " as date_id,count(*),count(distinct " + key_column + ") from " + type_name + "_" + table_level
                            + dirSql + " group by " + date_id + " " + " order by " + date_id + " " + order;
                }

                log.debug(sql);
                rest = stmt.executeQuery(sql);

                while (rest.next()) {
                    final List<Object> temp_vector = new Vector<Object>();
                    temp_vector.add(sdf.format(rest.getTimestamp(1)));
                    temp_vector.add(rest.getObject(2));
                    temp_vector.add(rest.getObject(3));

                    return_vector.add(temp_vector);
                }
                con.commit();

            } finally {
                try {
                    rest.close();
                    stmt.close();
                    con.close();
                } catch (SQLException e) {
                    throw e;
                }
            }
        }

        return return_vector;
    }

    /**
     * @param type_name
     * @param table_level
     * @param start_time
     * @param con Connection to the DB dwh
     * @param con Connection to the DB dewhrep
     * @return Vector that holds the information
     */
    public static List<List<Object>> getDataRowUseNOkey(final String type_name, final String table_level, final String start_time,
                                                        final Connection con,final Connection repcon) throws SQLException { // by

        return getDataRowUseNOkey(type_name, table_level, start_time, con,repcon, false);
    }

    public static List<List<Object>> getDataRowUseNOkey(final String type_name, final String table_level, final String start_time,
                                                        final Connection con,final Connection repcon, final boolean ascending) throws SQLException {
        final List<List<Object>> return_vector = new Vector<List<Object>>();

        Statement stmt = null;
        ResultSet rest = null;

        try {
            log.info("type_name: '" + type_name + "'");
            String date_id = getDateColumn(type_name,table_level,repcon);
            log.info("date_id: '" + date_id + "'");
            stmt = con.createStatement();
            String selectSql = null;
            if (table_level.equalsIgnoreCase("RAW") || table_level.equalsIgnoreCase("COUNT")) {
                selectSql = "select datetime_id,count(*) from " + type_name + "_" + table_level + " where " + date_id + " = '" + start_time
                        + "' group by datetime_id order by datetime_id " + "asc";
            } else {
                selectSql = "select" + date_id + ",count(*) from " + type_name + "_" + table_level + " where " + date_id + " = '" + start_time
                        + "' group by " + date_id + "order by " + date_id + "asc";
            }
            log.debug("date_id: '" + date_id + "'");
            log.debug("Selecting: '" + selectSql + "'");
			rest = stmt.executeQuery(selectSql);
           

            while (rest.next()) {
                final List<Object> temp_vector = new Vector<Object>();

                temp_vector.add(rest.getTimestamp(1));
                temp_vector.add(rest.getObject(2));
                temp_vector.add("N/A");

                return_vector.add(temp_vector);
            }
            con.commit();
        } finally {
            rest.close();
            stmt.close();
        }
        return return_vector;
    }


    /**
     * @param table_name
     * @param start_time
     * @param con Connection to the DB
     * @return Vector that holds the information
     */
    public static List<List<Object>> getDataRowDate(final String table_name, final String start_time, final Connection con) {
        final List<List<Object>> return_vector = new Vector<List<Object>>();
        Statement stmt = null;
        ResultSet rest = null;
        try {

            stmt = con.createStatement();
            rest = stmt.executeQuery("select date_id,count(*) from " + table_name + " where date_id >= '" + start_time
                    + "' group by date_id order by date_id desc");

            while (rest.next()) {
                final List<Object> temp_vector = new Vector<Object>();
                temp_vector.add(rest.getTimestamp(1));
                temp_vector.add(rest.getObject(2));
                return_vector.add(temp_vector);
            }

        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            try {
                rest.close();
                stmt.close();
            } catch (SQLException e) {
                log.error("Exception", e);
            }
        }
        return return_vector;
    }

    /**
     * Get active installed tech packs
     * 
     * @param Connection to dwhrep
     * @return
     */

    public static List<List<String>> getActiveInstalledTechpacks(final Connection dwhRep) {

        final List<List<String>> tps = new Vector<List<String>>();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Statement stmt = null;
        ResultSet rest = null;
        try {

            stmt = dwhRep.createStatement();
            final String sql = "select versionid, techpack_name, product_number, techpack_version, techpack_type, status, creationdate from  ("
                    + "select v.versionid, v.techpack_name, v.product_number, v.techpack_version, v.techpack_type, t.status, d.creationdate "
                    + "from versioning v, tpactivation t, dwhtechpacks d " + "where v.versionid *= t.versionid and v.versionid *= d.versionid"
                    + " union (" + "SELECT  VERSIONID,TECHPACK_NAME,PRODUCT_NUMBER,TECHPACK_VERSION,TYPE,STATUS,CREATIONDATE FROM MZTechPacks"
                    + ")) as results where status='ACTIVE' order by techpack_name";

            log.debug("Executing sql: " + sql);
            rest = stmt.executeQuery(sql);

            String versionId = null;
            String adminUiVersion = null;
            while (rest.next()) {
                final List<String> tmp = new Vector<String>();
                versionId = rest.getString("versionid"); // versionid of the form DWH_BASE:b68 or DIM_E_GRAN:((4))
                log.debug("versionId: " + versionId);
                tmp.add(rest.getString("techpack_name"));

                if (rest.getString("product_number") == null) {
                    tmp.add("n/a");
                } else {
                    tmp.add(rest.getString("product_number"));
                }

                // Check to see if the techpack_version is from IDE:R1A or SDK:R1A_b5 for example. Convert the IDE version to
                // R1A_((4))
                if (rest.getString("techpack_version").contains("_")) {
                    // From the SDK - Already has the the techpack_version in the right format.
                    tmp.add(rest.getString("techpack_version"));
                } else {
                    adminUiVersion = getVersionForAdminUI(versionId);
                    if (adminUiVersion.startsWith("b")) {
                        tmp.add(rest.getString("techpack_version") + "_" + adminUiVersion); // techpack_version of the sdk.
                    } else {
                        tmp.add(rest.getString("techpack_version") + "_b" + adminUiVersion); // techpack_version of the ide.
                    }

                }

                tmp.add(rest.getString("techpack_type"));
                tmp.add(rest.getString("status"));

                if (rest.getTimestamp("creationdate") == null) {
                    tmp.add("n/a");
                } else {
                    tmp.add(sdf.format(rest.getTimestamp("creationdate")));
                }

                tps.add(tmp);
            }

        } catch (Exception e) {
            log.error("Exception fecthing active installed tech packs: ", e);
        } finally {
            try {
                rest.close();
                stmt.close();
            } catch (SQLException e) {
                log.error("Exception", e);
            }
        }

        return tps;
    }

    private static String getVersionForAdminUI(final String inputVersion) {
        String resultVersion = null;
        resultVersion = inputVersion.substring(inputVersion.indexOf(":") + 1);
        resultVersion = resultVersion.replace("((", "");
        resultVersion = resultVersion.replace("))", "");
        return resultVersion;
    }

    /**
     * Get inactive installed tech packs
     * 
     * @param Connection to dwhrep
     * @return
     */

    public static List<List<String>> getInActiveInstalledTechpacks(final Connection dwhRep) {

        final List<List<String>> tps = new Vector<List<String>>();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Statement stmt = null;
        ResultSet rest = null;
        try {

            stmt = dwhRep.createStatement();
            final String sql = "select versionid, techpack_name, product_number, techpack_version, techpack_type, status, creationdate"
                    + " from  (Select v.versionid, v.techpack_name, v.product_number, v.techpack_version, v.techpack_type, t.status, d.creationdate "
                    + "from versioning v, tpactivation t, dwhtechpacks d " + "where v.versionid *= t.versionid and v.versionid *= d.versionid"
                    + " union ( " + "SELECT  VERSIONID,TECHPACK_NAME,PRODUCT_NUMBER,TECHPACK_VERSION,TYPE,STATUS,CREATIONDATE FROM MZTechPacks "
                    + ")) as results where status<>'ACTIVE' or status = null order by techpack_name ";

            log.debug("Executing sql: " + sql);

            rest = stmt.executeQuery(sql);
            String versionId = null;
            String adminUiVersion = null;
            while (rest.next()) {
                final List<String> tmp = new Vector<String>();
                versionId = rest.getString("versionid"); // versionid of the form DWH_BASE:b68 or DIM_E_GRAN:((4))
                log.debug("versionId: " + versionId);

                tmp.add(rest.getString("techpack_name"));

                if (rest.getString("product_number") == null) {
                    tmp.add("n/a");
                } else {
                    tmp.add(rest.getString("product_number"));
                }

                // Check to see if the techpack_version is from IDE:R1A or SDK:R1A_b5 for example. Convert the IDE version to
                // R1A_((4))
                if (rest.getString("techpack_version").contains("_")) {
                    // From the SDK - Already has the the techpack_version in the right format.
                    tmp.add(rest.getString("techpack_version"));
                } else {
                    adminUiVersion = getVersionForAdminUI(versionId);
                    if (adminUiVersion.startsWith("b")) {
                        tmp.add(rest.getString("techpack_version") + "_" + adminUiVersion); // techpack_version of the sdk
                    } else {
                        tmp.add(rest.getString("techpack_version") + "_b" + adminUiVersion); // techpack_version of the ide
                    }

                }

                tmp.add(rest.getString("techpack_type"));

                if (rest.getString("status") == null) {
                    tmp.add("n/a");
                } else {
                    tmp.add(rest.getString("status"));
                }

                if (rest.getTimestamp("creationdate") == null) {
                    tmp.add("n/a");
                } else {
                    tmp.add(sdf.format(rest.getTimestamp("creationdate")));
                }

                tps.add(tmp);
            }

        } catch (Exception e) {
            log.error("Exception fecthing inactive installed tech packs: ", e);
        } finally {
            try {
                rest.close();
                stmt.close();
            } catch (SQLException e) {
                log.error("Exception", e);
            }
        }

        return tps;
    }

    /**
     * Get typ_id for measurement type
     * 
     * @param meas_type
     * @return
     */

    public static String getTypeIdForMeasType(final Context ctx, final String meas_type) {

        String type_id = null;

        try {

            final RockFactory rock = (RockFactory) ctx.get("rockDwhRep");

            final Measurementtype whereRef = new Measurementtype(rock);
            whereRef.setTypename(meas_type);
            final MeasurementtypeFactory ref = new MeasurementtypeFactory(rock, whereRef);

            final List<Measurementtype> keys = ref.get();

            final Measurementtype key = (Measurementtype) keys.get(0);

            type_id = key.getTypeid();

        } catch (Exception e) {
            log.debug("Exception fecthing type_id: ", e);
        }

        return type_id;
    }

    /**
     * This method was added by Petri Raatikainen in order to introduce the possibility to select data by defining a range of days. startDay-endDay.
     * 
     * @param tableName
     * @param startTime
     * @param con Connection to the DB dwh
     * @param con Connection to the DB dwhrep
     * @return Vector that holds the information
     */
    public static List<List<Object>> getDataRowDateExtendedRange(final String meas_type, final String meas_level, final String startTime,
                                                                 final String endTime, final Connection con,final Connection repcon, final boolean direction,
                                                                 final String search_days) throws SQLException {
        return getDataRowDateExtendedInternal(meas_type, meas_level, startTime, endTime, con,repcon, direction, search_days);
    }

    private static List<List<Object>> getDataRowDateExtendedInternal(final String meas_type, String meas_level, final String startTime,
                                                                     String stopTime, final Connection con,final Connection repcon, final boolean direction,
                                                                     final String search_days) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rset = null;
        final List<List<Object>> finalVector = new Vector<List<Object>>(100);

        final String date = "yyyy-MM-dd";
        String date_id = getDateColumn(meas_type,meas_level,repcon);
        final DateFormatter df = new DateFormatter(date);
        df.setCalendar(startTime);
        stopTime = df.reverseTimeDayString(Integer.parseInt(search_days));

        try {
            con.commit();

            if (meas_level == null) {
                meas_level = "RAW";
            }

            final String tableName = meas_type + "_" + meas_level;

            String dirSql = "";
            if (direction) {
                dirSql = "WHERE " + date_id + " between date('" + startTime + " 00:00:00')-" + search_days + " AND date('" + startTime
                        + " 00:00:00') ";
            } else {
                dirSql = "WHERE " + date_id + " between date('" + startTime + " 00:00:00') AND date('" + stopTime + " 00:00:00') ";
            }
            //

            // if level is RAW
            if (meas_level.equals("RAW")) {
                // for RAW tables do other kind of search as to others (DAY/DAYBH/COUNT)

                final String rawSearch = "SELECT " + date_id + ", COUNT(*), COUNT(DISTINCT datetime_id) " + "FROM " + tableName + " " + dirSql
                        + "GROUP BY " + date_id + " " + "ORDER BY " + date_id + " " + (direction ? " DESC " : " ASC");

                // do RAW search
                log.debug("Raw search: " + rawSearch);
                statement = con.prepareStatement(rawSearch);
                rset = statement.executeQuery();

                // go thru all results
                while (rset.next()) {
                    final List<Object> tmpVector = new Vector<Object>(3);
                    tmpVector.add(DateFormatter.parseTimeStampToDateMonth(rset.getTimestamp(1).toString())); // date_id
                    tmpVector.add(rset.getObject(2)); // COUNT(*)
                    tmpVector.add(rset.getObject(3)); // COUNT(DISTINCT datetime_id)
                    finalVector.add(tmpVector);
                }
                rset.close();
                rset = null;
            } else {
                // if level is not RAW
                final String nonRawSearch = "SELECT " + date_id + ", COUNT(*) " + "FROM " + tableName + " " + dirSql + "GROUP BY " + date_id + " "
                        + "ORDER BY " + date_id + " " + (direction ? " DESC " : " ASC");
                log.debug(nonRawSearch);
                // do non RAW search
                statement = con.prepareStatement(nonRawSearch);
                rset = statement.executeQuery();

                // save the results to vectors
                while (rset.next()) {
                    final Vector<Object> tmpVector = new Vector<Object>(3);
                    tmpVector.add(DateFormatter.parseTimeStampToDateMonth(rset.getTimestamp(1).toString())); // date_id
                    tmpVector.add(rset.getObject(2)); // COUNT(*)
                    tmpVector.add("N/A"); // COUNT(DISTINCT datetime_id)
                    finalVector.add(tmpVector);
                }
                rset.close();
                rset = null;
            }

        } catch (Exception e) {
            log.debug("Exception ", e);
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
                statement.close();
            } catch (SQLException e) {
                log.error("Exception", e);
            }
        }

        // result now locates at finalVector
        return finalVector;
    }
    /**
     * @param type_name
     * @param table_level
     * @param repcon Connection to the DB with dwhrep
     * @return Column name for Date date_id or datetime_id
     * @throws SQLException
     */
        public static String getDateColumn(final String type_name,final String table_level,final Connection repcon) throws SQLException{
                   Statement stmt = null;
                   ResultSet rs = null;
                   String date_Id = " date_id ";
                   try {
                       stmt = repcon.createStatement();
                       String sql = null;
                   sql = "select col.dataname as Date_Column "+
                                "from dwhpartition part, dwhcolumn col "+
                                "where col.storageid like '%"+type_name+"_"+table_level+"%' "+
                                "and part.storageid = col.storageid "+
                                "and col.dataname like 'date%' group by col.dataname order by col.dataname asc";
      
                       rs = stmt.executeQuery(sql);                     
                       while (rs.next()) {
                         date_Id = rs.getString("Date_Column");//(date_Id);
                         break;
                       }
                       repcon.commit();
                    }catch(Exception e){
                        log.error("Exception", e);
                   }finally{
                           rs.close();
                   stmt.close();
                   }
                   return date_Id;
             }

}
