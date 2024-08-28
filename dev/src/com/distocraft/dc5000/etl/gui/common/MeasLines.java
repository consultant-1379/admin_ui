package com.distocraft.dc5000.etl.gui.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MeasLines
{
  private final Log log = LogFactory.getLog(super.getClass());
  private final List<String> measArray;
  private final List<String> daysArray;
  private final List<LineCounts> lineCountArray;
  private final List<LineCounts> ropCountArray;
  private final int dayCount;

  public MeasLines(int measCount, int dayCount, int year, int month, int day, boolean searchBackward)
  {
    this.measArray = new ArrayList<String>(measCount);
    this.daysArray = new ArrayList<String>(dayCount);
    this.lineCountArray = new ArrayList<LineCounts>(measCount);
    this.ropCountArray = new ArrayList<LineCounts>(measCount);
    DbCalendar startDate = new DbCalendar(year, month, day);

    this.dayCount = dayCount;

    for (int i = 0; i < dayCount; ++i) {
      this.daysArray.add(parseDateString(startDate));
      if (searchBackward)
        startDate.add(5, -1);
      else
        startDate.add(5, 1);
    }
    if(!searchBackward)
    	Collections.sort(this.daysArray , Collections.reverseOrder());
    	
  }

  public String getDate(int row)
  {
    return ((String)this.daysArray.get(row));
  }

  public int getNumberOfDays()
  {
    return this.daysArray.size();
  }

  public void addLines(MeasResultSet measResult){	 
    //Modified for EQEV-12110
    for(int i = 0 ; i < measResult.getSize(); i++){
    	this.measArray.add(measResult.getMeasType(i));
    	String measType = measResult.getMeasType(i);
    	ArrayList<Integer> lineCountValue= measResult.getMapValue(measType);
    	ArrayList<Integer> ropCountValue= measResult.getRopValue(measType);
    	
    	LineCounts lineCounts = new LineCounts(this.dayCount);
    	LineCounts ropCounts = new LineCounts(this.dayCount);
        for (int k = 0; k < lineCountValue.size(); ++k) {    	
        	Integer tmpValue =lineCountValue.get(k);
            lineCounts.setLine(k, tmpValue.intValue());
        }
        this.lineCountArray.add(lineCounts);
        this.log.debug("MeasLine :: Adding the Value of Row Count");
        for (int j = 0; j < ropCountValue.size(); ++j) {
        	 Integer tmpValue  = ropCountValue.get(j);
        	 ropCounts.setLine(j, tmpValue.intValue());
        }
        this.ropCountArray.add(ropCounts);
    }
  }

  public int measArraySize()
  {
    return this.measArray.size();
  }

  public int daysArraySize()
  {
    return this.daysArray.size();
  }

  public int lineCountArraySize()
  {
    return this.lineCountArray.size();
  }
  public int ropCountArraySize()
  {
    return this.ropCountArray.size();
  }

  public List<String> getMeasArray()
  {
    return this.measArray;
  }

  public List<String> getDaysArray()
  {
    return this.daysArray;
  }

  public List<LineCounts> getLineCountArray()
  {  
    return this.lineCountArray;
  }
  public List<LineCounts> getRopCountArray()
  {
    return this.ropCountArray;
  }

  private String parseDateString(DbCalendar calendar)
  {
    return calendar.getDbDate();
  }
}