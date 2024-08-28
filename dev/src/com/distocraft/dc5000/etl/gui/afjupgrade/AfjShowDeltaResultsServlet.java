/**
 * Handles http-requests related to Afj-upgrade. 
 * 
 * Attributes stored in session:
 * 
 */
package com.distocraft.dc5000.etl.gui.afjupgrade;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.afjupgrade.AfjManagerProcess.State;
import com.distocraft.dc5000.etl.gui.afjupgrade.MessageBean.MessageType;
import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.ericsson.eniq.afj.common.AFJDelta;
import com.ericsson.eniq.afj.common.AFJMeasurementTag;
import com.ericsson.eniq.afj.common.AFJMeasurementType;

/**
 * @author eheijun
 * 
 */
public class AfjShowDeltaResultsServlet extends EtlguiServlet {

  private static final long serialVersionUID = 2484915489143220452L;

  private static final String AFJTECHPACK_VERSION = "afjtechpackVersion";

  private static final String VIEW = "afj_show_delta_results_view.vm";

  private final transient Log log = LogFactory.getLog(this.getClass());

  final static String MESSAGES = "messages";

  final static String TOTTYPECOUNT = "tottypecount";

  final static String MODTYPECOUNTERCOUNT = "modtypecountercount";

  final static String MODTYPECOUNT = "modtypecount";

  final static String NEWTYPECOUNTERCOUNT = "newtypecountercount";

  final static String NEWTYPECOUNT = "newtypecount";

  private static final String TOTTYPECOUNTERCOUNT = "tottypecountercount";

  private static final String AFJTECHPACK = "afjtechpack";

  private static final String AFJDELTA = "afjdelta";

  private static final String AFJ_DELTA_EXIST = "afjdeltaexist";

  private static final String AFJUPGRADEALLOWED = "afjupgradeallowed";

  static final String AFJ_MANAGER_PROCESS = "afjManagerProcess";

  /**
   * Handles the http request (performs operations and forwards to corresponding
   * VIEW)
   * 
   * @see com.distocraft.dc5000.etl.gui.common.EtlguiServlet#doHandleRequest(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.apache.velocity.context.Context)
   */
  @Override
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response,
      final Context ctx) throws Exception {

    final HttpSession session = request.getSession(false);

    final List<MessageBean> messages = new ArrayList<MessageBean>();
    try {
      
      final List<String> runupgradeIdList = new ArrayList<String>();
      final Enumeration<?> names = request.getParameterNames();
      boolean confirmed = false;
      while (names.hasMoreElements()) {
        final String name = (String) names.nextElement();
        if (name.startsWith("runupgrade.")) {
          final String value = name.substring(11, name.length());
          runupgradeIdList.add(value);
        }
        if (name.equals("confirm")) {
          confirmed = true;
        }
      }

      final AfjManagerProcess amp = (AfjManagerProcess) session.getAttribute(AFJ_MANAGER_PROCESS);
      if (amp == null) {
        final String redirectURL = request.getContextPath() + View.AFJ_SHOW_AFJ_TECH_PACKS;
        response.sendRedirect(redirectURL);
        return null;
      } else {
        
        Boolean allowupgrade = true; 
        
        log.debug(amp.getStatusMessage());

        if (amp.isRunning() && (amp.getProcessState() == State.GENERATE_DELTA || amp.getProcessState() == State.UPGRADE_TECHPACK)) {
          final String redirectURL = request.getContextPath() + View.AFJ_SHOW_PROCESS_STATUS;
          response.sendRedirect(redirectURL);
          return null;
        }

        if (!amp.isRunning() && amp.getProcessState() == State.UPGRADE_READY) {
          final String redirectURL = request.getContextPath() + View.AFJ_SHOW_AFJ_TECH_PACKS;
          response.sendRedirect(redirectURL);
          return null;
        }

        if (!amp.isRunning() && (runupgradeIdList.size() > 0)) {
          log.debug("runupgrade button pressed for " + runupgradeIdList.get(0) + ".");
          if (confirmed) {
            amp.setSelectedTechPack(runupgradeIdList.get(0));
            final String forwardURL = View.AFJ_START_UPGRADE_TECH_PACK;
            request.getRequestDispatcher(forwardURL).forward(request, response);
            return null;
          } else {
            log.debug("backup status not confirmed.");
            messages.add(new MessageBean("ENIQ Repository snaphot should be taken before upgrade can be performed.", MessageType.ERROR));
          }
        }

        if (!amp.getStatusMessage().isEmpty() && amp.isErrorState()) {
          messages.add(new MessageBean(amp.getStatusMessage(), MessageType.ERROR));
          allowupgrade = false;
//        } else if (!amp.getStatusMessage().isEmpty()) {
//          messages.add(new MessageBean(amp.getStatusMessage(), MessageType.INFO));
        }
        
        if (amp.getAFJDelta() == null) {
          ctx.put(AFJ_DELTA_EXIST, false);
        } else {
          ctx.put(AFJ_DELTA_EXIST, true);
        }

        int newtypecount = 0;
        int modtypecount = 0;
        int tottypecount = 0;
        int newtypecountercount = 0;
        int modtypecountercount = 0;
        int tottypecountercount = 0;
        
        String afjTpVersion = ""; 
        String afjTpName = amp.getSelectedTechPack().getTechPackName(); 
        
        final AFJDelta afjDelta = amp.getAFJDelta();
        if (afjDelta != null) {
        	afjTpVersion = afjDelta.getTechPackVersion();
        	afjTpName = afjDelta.getTechPackName();
          for (AFJMeasurementType measurementType : afjDelta.getMeasurementTypes()) {
            if (measurementType.isTypeNew()) {
              newtypecount++;
              for (AFJMeasurementTag measurementTag : measurementType.getTags()) {
                newtypecountercount = newtypecountercount + measurementTag.getNewCounters().size();
              }
            } else {
              modtypecount++;
              for (AFJMeasurementTag measurementTag : measurementType.getTags()) {
                modtypecountercount = modtypecountercount + measurementTag.getNewCounters().size();
              }
            }
          }
          tottypecountercount = newtypecountercount + modtypecountercount;
          tottypecount = newtypecount + modtypecount;
          if (afjDelta.getMeasurementTypes().isEmpty()) {
              allowupgrade = false;
            }
        }
        
        if (tottypecountercount > amp.getSelectedTechPack().getMaxCounters()) {
          messages.add(new MessageBean("Only " + amp.getSelectedTechPack().getMaxCounters() + " counters can be created at a time.", MessageType.WARNING));
          allowupgrade = false;
        }
        
        if (tottypecount > amp.getSelectedTechPack().getMaxMeasTypes()) {
          messages.add(new MessageBean("Only " + amp.getSelectedTechPack().getMaxMeasTypes() + " measurement types can be created at a time.", MessageType.WARNING));
          allowupgrade = false;
        }
        
        if (!amp.getStatusMessage().isEmpty() && messages.isEmpty()) {
          messages.add(new MessageBean(amp.getStatusMessage(), MessageType.INFO));
        }        
        
        ctx.put(AFJDELTA, afjDelta);
        ctx.put(AFJTECHPACK_VERSION, afjTpVersion);
        ctx.put(AFJTECHPACK, afjTpName);
        ctx.put(NEWTYPECOUNT, newtypecount);
        ctx.put(NEWTYPECOUNTERCOUNT, newtypecountercount);
        ctx.put(MODTYPECOUNT, modtypecount);
        ctx.put(MODTYPECOUNTERCOUNT, modtypecountercount);
        ctx.put(TOTTYPECOUNT, tottypecount);
        ctx.put(TOTTYPECOUNTERCOUNT, tottypecountercount);
        ctx.put(AFJUPGRADEALLOWED, allowupgrade);

        if (!amp.isRunning()) {
          amp.initializeStatus();
        }

        log.debug("page ready");
      }

    } catch (Exception e) {
      messages.add(new MessageBean("AFJ manager has technical problems. See logs for more details.", MessageType.ERROR));
      log.error("Fatal error: " + e.getMessage());
    }
    ctx.put(MESSAGES, messages);
    final Template outty = getTemplate(VIEW);
    return outty;
  }
}

  