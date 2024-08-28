/**
 *
 */
package com.distocraft.dc5000.etl.gui.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;

import com.distocraft.dc5000.etl.gui.login.LoginForm;



/**
 * @author eheijun
 *
 */
@SuppressWarnings("deprecation")
public abstract class GuiServlet extends VelocityServlet {
	
	
	public static Map<String, HashMap<String, Long>> users_detail = new HashMap<String, HashMap<String, Long>>();
	

  private static final long serialVersionUID = 1L;

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private static final String THE_USER = "theuser";

  private static final String CURRENT_TIME = "currenttime";

  protected static final String ENVIRONMENT = "environment"; 
  private final Log log = LogFactory.getLog(this.getClass());
    
  @Override
  public Template handleRequest(final HttpServletRequest request, final HttpServletResponse response, final Context context) throws Exception {

    final HttpSession session = request.getSession();
    final String userName = getUserName(request);
    
    // Removes a user entry if login is successful
    LoginForm.removeUser(userName);
    
    if (userName != null) {
      context.put(THE_USER, userName);
    }

    context.put(CURRENT_TIME, sdf.format(new Date()));

    if (session.getAttribute(ENVIRONMENT) != null) {
      context.put(ENVIRONMENT, session.getAttribute(ENVIRONMENT));
    } else {
      context.put(ENVIRONMENT, new EnvironmentNone());
    }
	
	// connection limit check code
    	String param_value = getServletContext().getInitParameter("maxSessions");
		int max_sessions = Integer.parseInt(param_value);
		String user = request.getUserPrincipal().getName();
		String environment_stats = request.getSession().getId();//getHeader("user-agent");		
		
		if (users_detail.containsKey(user)) {

			if (users_detail.get(user).containsKey(environment_stats)) {				
				long past_time = users_detail.get(user).put(environment_stats,
						System.currentTimeMillis());
				long time_diff = System.currentTimeMillis() - past_time;
				long session_time = EtlguiServlet.getSessionTimeout() * 1000;
				if (time_diff > session_time) {
					users_detail.get(user).remove(environment_stats);
				}				
			} else {
				// session time out handling
				HashMap<String, Long> user_envs = users_detail.get(user);
				Set<String> users_set = user_envs.keySet();
				Iterator itr = users_set.iterator();

				while (itr.hasNext()) {
					String user_env = itr.next().toString();
					long past_time = user_envs.get(user_env);
					long time_diff = System.currentTimeMillis() - past_time;
					long session_time = EtlguiServlet.getSessionTimeout() * 1000;
					if (time_diff > session_time) {
						itr.remove();
					}

				}
				GuiServlet.users_detail.put(user, user_envs);

				if (users_detail.get(user).size() < max_sessions) {
					HashMap<String, Long> user_map = users_detail.get(user);
					user_map.put(environment_stats, System.currentTimeMillis());
					users_detail.put(user, user_map);
				} else {
					EtlguiServlet.setSession_check(true);
					
				}
			}

		} else {
			HashMap<String, Long> user_map = new HashMap<String, Long>();
			user_map.put(environment_stats, System.currentTimeMillis());
			users_detail.put(user, user_map);
		}
		log.debug("Logged in session for user : "+user +", with session Id : " +environment_stats + ", Number of sessions logged for the user : "+users_detail.get(user).size() + ", Max number of sessions configured : "+max_sessions );
		// connection limit check code ends here

    return null;
  }

private String getUserName(final HttpServletRequest request) {
    String userName = null;
    if (request.getUserPrincipal() != null) {
      userName = request.getUserPrincipal().getName();
    }
    return userName;
  }

}
