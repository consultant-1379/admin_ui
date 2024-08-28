package com.distocraft.dc5000.etl.gui.etl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.distocraft.dc5000.etl.gui.common.DbCalendar;
import com.distocraft.dc5000.etl.gui.etl.DataSourceLog.TimeRange;

public class DatasourceLogTest {



    @Test
    public void testTimeRangeUpdateEndOfRange() {

        Date now = new Date();
        DataSourceLog.TimeRange tr = new DataSourceLog.TimeRange(now);
        Date lastUncollectedDataTime;

        lastUncollectedDataTime = addMinutes(now,1);
        assertTrue(tr.updateEndOfRange(lastUncollectedDataTime));
        tr.setEnd(lastUncollectedDataTime);

        lastUncollectedDataTime = addMinutes(lastUncollectedDataTime,2);
        assertFalse(tr.updateEndOfRange(lastUncollectedDataTime));


    }

    @Test
    public void testMapMissingDataSources() throws Exception{
        DataSourceLog log = new DataSourceLog();


        Map<String, List<DataSourceLog.TimeRange>> map;
        List<DataSourceLog.TimeRange> trs;
        long minutes;
        Date lastCollectedTime;

        Date now = new Date();
        log.missingDataMap = new TreeMap<String, List<TimeRange>>();
        map = log.mapMissingDataRange("SGSN1", now);


        lastCollectedTime  =  addMinutes(now, 1);
        map = log.mapMissingDataRange("SGSN1", lastCollectedTime);



        trs  = map.get("SGSN1");
        assertEquals(1,trs.size());
        minutes = trs.get(0).lengthInMinutes();
        assertEquals(2, minutes);

        // add 1 minute
        lastCollectedTime = addMinutes(lastCollectedTime, 1);
        map = log.mapMissingDataRange("SGSN1", lastCollectedTime);

        // should be same time range
        trs  = map.get("SGSN1");
        assertEquals(1,trs.size());
        minutes = trs.get(0).lengthInMinutes();
        assertEquals(3, minutes);

        // add 2 minutes - should result in a new time range
        lastCollectedTime = addMinutes(lastCollectedTime, 2);
        map = log.mapMissingDataRange("SGSN1", lastCollectedTime);


        // should be same time range
        trs  = map.get("SGSN1");
        assertEquals(2,trs.size());
        minutes = trs.get(0).lengthInMinutes();
        assertEquals(3, minutes);

        DataSourceLog.TimeRange tr = trs.get(1);
        assertEquals(lastCollectedTime,tr.getStart());
        lastCollectedTime = addMinutes(tr.getStart(), 1);
        assertEquals(lastCollectedTime, tr.getEnd());

    }
    
    private Date addMinutes(Date dateChange,int min){
        
        GregorianCalendar cal = new DbCalendar();
        cal.setTime(dateChange);
        cal.add(Calendar.MINUTE, min);
        return cal.getTime();

    }


}
