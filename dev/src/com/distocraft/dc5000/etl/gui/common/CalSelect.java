package com.distocraft.dc5000.etl.gui.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This Class is used to populate the VM: cal_select_1.vm
 * It should be instantiated from all Classes that use VM Templates
 * which have as a reference the VM Template "cal_select_1.vm"
 * If this Class is not used the default years will be populated 
 * in the cal_select_1.vm, which is the range 2000 - 2010. 
 *   
 * VM: cal_select_1.vm
 * VM: showLoadings.vm
 * 
 * @author eeikbe
 *
 */
public class CalSelect {

	private final Log log = LogFactory.getLog(this.getClass());
	private final Connection conn;

	public CalSelect(final Connection conn) {
		this.conn = conn;
	}

	/**
	 * This method executes an SQL read of the DIM_DATE table. It 
	 * returns a Vector<String> of distinct years.
	 * It's used to populate a SelectBox with valid years.
	 * @return
	 */
	public List<String> getYearRange() {
	  
	  final String sql = "SELECT MIN(YEAR_ID), MAX(YEAR_ID) FROM DIM_DATE";

		Statement stmt = null;
		ResultSet rset = null;
		List<String> years = null; 

		//Make query to get list of years.
		try {
			stmt = conn.createStatement();
			log.debug("Executing query: " + sql);
			rset = stmt.executeQuery(sql);
			years = new ArrayList<>();
			while(rset.next()){
				years.add(rset.getString(1));
				years.add(rset.getString(2));
			}
		} catch (SQLException e) {
			log.error("SQLException", e);
		}finally {
			try {
				if (rset != null) {
					rset.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				log.error("SQLException while trying to close connections", e);
			}
		}
		return years;
	
	}

	/**
	 * This Method returns a valid range, starting from the current year
	 * and ending 5 years in the future.
	 * @param startYear
	 * @return
	 */
	public List<String> getFutureRange(final String startYear) {
	  final int start = Integer.parseInt(startYear);
	  final List<String> result = new Vector<String>(6);
		for(int i = 0; i < 6; i++){
			result.add(Integer.toString(start + i));
		}
		return result;
	}
}
