package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import ssc.rockfactory.RockFactory;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.rock.Meta_system_monitors;
import com.distocraft.dc5000.etl.rock.Meta_system_monitorsFactory;

public class SystemMonitorsDetails extends EtlguiServlet {

  private Log log = LogFactory.getLog(this.getClass());

  private Vector systemMonitors = new Vector();
  
  private Hashtable systemMonitorExecuteTimes = new Hashtable();

  private Context context = null;

  Template template = new Template();

  public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context context)
      throws Exception {
    this.context = context;
    this.systemMonitors = new Vector();
    RockFactory etlRepRockFactory = (RockFactory) this.context.get("rockEtlRep");
    Meta_system_monitors whereMetaSystemMonitors = new Meta_system_monitors(etlRepRockFactory);
    Meta_system_monitorsFactory metaSystemMonitorsFactory = new Meta_system_monitorsFactory(etlRepRockFactory,
        whereMetaSystemMonitors);
    Vector metaSystemMonitors = metaSystemMonitorsFactory.get();
    Iterator metaSystemIterator = metaSystemMonitors.iterator();

    while (metaSystemIterator.hasNext()) {
      Meta_system_monitors currentMetaSystemMonitor = (Meta_system_monitors) metaSystemIterator.next();
      Timestamp executed = currentMetaSystemMonitor.getExecuted();
      Date executedDate = new Date(executed.getTime());
      SimpleDateFormat usedDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String executedDateString = usedDateFormat.format(executedDate);
      
      if(executedDateString.equalsIgnoreCase("1970-01-01 02:00:00")) {
        executedDateString = "Not yet executed";
      }
      
      this.systemMonitors.add(currentMetaSystemMonitor);
      this.systemMonitorExecuteTimes.put(currentMetaSystemMonitor.getHostname() + "::" + currentMetaSystemMonitor.getMonitor(),executedDateString);
    }

    this.context.put("systemMonitors", this.systemMonitors);
    this.context.put("systemMonitorExecuteTimes", this.systemMonitorExecuteTimes);
    this.log.debug("systemMonitors = " + systemMonitors);

    template = getTemplate("system_monitors_details.vm");

    return template;
  }

}
