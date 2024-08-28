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
import java.util.concurrent.ExecutorService;

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
import com.ericsson.eniq.afj.common.AFJTechPack;

/**
 * @author eheijun
 * 
 */
public class AfjShowAfjTechPacksServlet extends EtlguiServlet {

  /**
   * Generated serial version id
   */
  private static final long serialVersionUID = -8280829696115943762L;

  private final transient Log log = LogFactory.getLog(this.getClass());

  private static final String view = "afj_show_afjtechpacks_view.vm";

  static final String MESSAGES = "messages";

  static final String AFJTECHPACKLIST = "afjtechpacklist";

  static final String AFJ_MANAGER_PROCESS = "afjManagerProcess";

  /**
   * Handles the http request (performs operations and forwards to corresponding
   * view)
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

      final List<String> showdeltaIdList = new ArrayList<String>();
      final Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
        final String name = (String) names.nextElement();
        if (name.startsWith("showdelta.")) {
          final String value = name.substring(10, name.length());
          showdeltaIdList.add(value);
        }
      }

      AfjManagerProcess amp = (AfjManagerProcess) session.getAttribute(AFJ_MANAGER_PROCESS);
      if (amp == null) {
        final ExecutorService executorService = (ExecutorService) session.getServletContext().getAttribute("ExecutorService");
        amp = new AfjManagerProcess(executorService);
        session.setAttribute(AFJ_MANAGER_PROCESS, amp);
      }
      
      log.debug(amp.getStatusMessage());
      
      if (amp.isRunning() && (amp.getProcessState() == State.GENERATE_DELTA || amp.getProcessState() == State.UPGRADE_TECHPACK)) {
          final String redirectURL = request.getContextPath() + View.AFJ_SHOW_PROCESS_STATUS;
          response.sendRedirect(redirectURL);
          return null;
      }

      if (!amp.isRunning() && amp.getProcessState() == State.GENERATE_READY) {
        final String redirectURL = request.getContextPath() + View.AFJ_SHOW_DELTA_RESULTS;
        response.sendRedirect(redirectURL);
        return null;
      }

      if (!amp.isRunning() && (showdeltaIdList.size() > 0)) {
        log.debug("Showdelta button pressed for " + showdeltaIdList.get(0) + ".");
        amp.setSelectedTechPack(showdeltaIdList.get(0));
        final String forwardURL = View.AFJ_START_GENERATE_DELTA;
        request.getRequestDispatcher(forwardURL).forward(request, response);
        return null;
      }

      if (!amp.getStatusMessage().isEmpty() && amp.isErrorState()) {
        messages.add(new MessageBean(amp.getStatusMessage(), MessageType.ERROR));
      } else if (!amp.getStatusMessage().isEmpty()) {
        messages.add(new MessageBean(amp.getStatusMessage(), MessageType.INFO));
      }
      
      final List<AFJTechPack> techPackList = amp.getAFJTechPackList();
      
      if (amp.isErrorState()) {
        if (amp.getProcessState().equals(State.TECHPACK_ERROR)) {
          messages.add(new MessageBean(amp.getStatusMessage(), MessageType.INFO));
        } else {
          messages.add(new MessageBean(amp.getStatusMessage(), MessageType.ERROR));
        }
      } else {
        for (AFJTechPack techPack : techPackList) {
          if (techPack.getMessage() != null) {
            if (techPack.getMessage().toUpperCase().startsWith("ERROR")) {
              messages.add(new MessageBean(techPack.getMessage(), MessageType.ERROR));
            } else {
              log.info(techPack.getMessage());
            }
          }
        }
      }

      ctx.put(AFJTECHPACKLIST, techPackList);
      
      if (!amp.isRunning()) {
        amp.initializeStatus();
      }

      log.debug("page ready");

    } catch (Exception e) {
      messages.add(new MessageBean("AFJ manager has technical problems. See logs for more details.", MessageType.ERROR));
      log.error("Fatal error.", e);
    }
    ctx.put(MESSAGES, messages);
    final Template outty = getTemplate(view);
    return outty;
  }
}
