/**
 * 
 */
package com.distocraft.dc5000.etl.gui.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.junit.Ignore;

/**
 * 
 * This class has been implemented for EtlguiServletTesting
 * @author eheijun
 *
 */
public class DummyEtlguiServletTest extends EtlguiServletTest {
  
  private class DummyEtlguiServlet extends EtlguiServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
        throws Exception {
      return getTemplate(TEST_VIEW_VM);
    }
    
  }

  @Override
  EtlguiServlet getServlet() {
    return new DummyEtlguiServlet();
  }

}
