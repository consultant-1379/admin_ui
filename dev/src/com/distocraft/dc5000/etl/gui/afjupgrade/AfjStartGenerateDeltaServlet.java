/**
 * 
 */
package com.distocraft.dc5000.etl.gui.afjupgrade;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;

/**
 * @author eheijun
 * 
 */
public class AfjStartGenerateDeltaServlet extends EtlguiServlet {

  /**
   * 
   */
  private static final long serialVersionUID = -2776291393914601946L;

  private final transient Log log = LogFactory.getLog(this.getClass());

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
    try {
      final AfjManagerProcess amp = (AfjManagerProcess) session.getAttribute(AFJ_MANAGER_PROCESS);
      if (amp == null) {
        throw new Exception("AfjManagerProcess not initialized.");
      } else {
        if (amp.isRunning()) {
          log.info("AFJManager is reserved for " + amp.getSelectedTechPack().getTechPackName() + ".");
          final String redirectURL = request.getContextPath() + View.AFJ_SHOW_AFJ_TECH_PACKS;
          response.sendRedirect(redirectURL);
        } else {
          log.info("Starting delta generation for " + amp.getSelectedTechPack().getTechPackName() + ".");
          amp.generateAFJTechPackDelta();
          final String redirectURL = request.getContextPath() + View.AFJ_SHOW_PROCESS_STATUS;
          response.sendRedirect(redirectURL);
        }
      }
    } catch (Exception e) {
      log.error("Fatal error. " + e.getMessage());
      throw e;
    }
    return null;
  }
}
