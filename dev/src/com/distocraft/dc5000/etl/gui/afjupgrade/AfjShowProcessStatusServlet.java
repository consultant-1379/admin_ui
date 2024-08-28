/**
 * 
 */
package com.distocraft.dc5000.etl.gui.afjupgrade;

import java.util.ArrayList;
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

/**
 * @author eheijun
 * 
 */
public class AfjShowProcessStatusServlet extends EtlguiServlet {

  /**
   * 
   */
  private static final long serialVersionUID = -98692989060998077L;

  private final transient Log log = LogFactory.getLog(this.getClass());

  private static final String view = "afj_show_process_status_view.vm";

  static final String MESSAGES = "messages";

  private static final String AFJTECHPACK = "afjtechpack";

  static final String AFJ_MANAGER_PROCESS = "afjManagerProcess";

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.distocraft.dc5000.etl.gui.common.EtlguiServlet#doHandleRequest(javax
   * .servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
   * org.apache.velocity.context.Context)
   */
  @Override
  public Template doHandleRequest(final HttpServletRequest request, final HttpServletResponse response,
      final Context ctx) throws Exception {
    final HttpSession session = request.getSession(false);
    final List<MessageBean> messages = new ArrayList<MessageBean>();
    try {
      
      final AfjManagerProcess amp = (AfjManagerProcess) session.getAttribute(AFJ_MANAGER_PROCESS);
      if (amp == null) {
        final String redirectURL = request.getContextPath() + View.AFJ_SHOW_AFJ_TECH_PACKS;
        response.sendRedirect(redirectURL);
        return null;
      } else {
        if (!amp.isRunning()) {
          log.debug(amp.getStatusMessage());
          if (amp.getProcessState() == State.GENERATE_READY || amp.getProcessState() == State.GENERATE_ERROR) {
            final String redirectURL = request.getContextPath() + View.AFJ_SHOW_DELTA_RESULTS;
            response.sendRedirect(redirectURL);
          } else {
            final String redirectURL = request.getContextPath() + View.AFJ_SHOW_AFJ_TECH_PACKS;
            response.sendRedirect(redirectURL);
          }
          return null;
        }
        log.debug(amp.getStatusMessage());
        ctx.put(AFJTECHPACK, amp.getSelectedTechPack().getTechPackName());
      }
      messages.add(new MessageBean(amp.getStatusMessage(), MessageType.RUNNING));
      log.debug("page ready");
      
    } catch (Exception e) {
      messages.add(new MessageBean("AFJ manager has technical problems. See logs for more details.", MessageType.ERROR));
      log.error(e.getMessage());
    }
    ctx.put(MESSAGES, messages);
    ctx.put("servletURL", request.getServletPath());
    final Template outty = getTemplate(view);
    return outty;
  }
}
