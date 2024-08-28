<%@page import="ssc.rockfactory.RockFactory"%>
<%@page import="org.apache.velocity.VelocityContext"%>
<%@page import="com.distocraft.dc5000.etl.gui.common.Environment.Type"%>
<%@page import="com.distocraft.dc5000.etl.gui.common.EnvironmentNone"%>
<%@page import="com.distocraft.dc5000.etl.gui.common.Environment"%>
<%@page import="com.distocraft.dc5000.etl.engine.main.*"%>
<%@page import="com.distocraft.dc5000.etl.gui.systemmonitor.*"%>
<%@page import="com.distocraft.dc5000.etl.gui.util.*"%>
<%@page import="com.distocraft.dc5000.etl.scheduler.*"%>
<%@page import="com.ericsson.eniq.repository.*"%>
<%@page import="com.distocraft.dc5000.etl.gui.config.*"%>
<%@page import="com.distocraft.dc5000.etl.gui.*"%>
<%@page import="com.distocraft.etl.gui.info.*"%>

<jsp:useBean id="loginForm"
	class="com.distocraft.dc5000.etl.gui.login.LoginForm" scope="session">
</jsp:useBean>




<%@ page isErrorPage="true" %>
<%@ page import="org.apache.commons.httpclient.*"%>
<%@ page import="java.io.*" %>
<%@ page import="java.lang.*" %>
<%@ page import ="javax.naming.Context" %>
<%@ page import ="javax.naming.directory.*" %> 
<%@ page import ="java.util.*" %>
<%@ page import ="java.rmi.*" %> 
<%

String path = request.getContextPath();

%>


<html>

<body>
<%
   String pathInfo =request.getRequestURI();
    String ipAddress = request.getRemoteAddr();
    int statusCode= response.getStatus();
    AdminuiInfo loginErrorInstance= new AdminuiInfo();
    String user = request.getParameter("j_username");
    loginErrorInstance.log(user,ipAddress,statusCode,pathInfo);
    int count = loginForm.getFailureCount(user);
    int lockOutTime = loginForm.getLockOutTime();

%>
<b>
	<%
		Environment env = null;
		String ENVIRONMENT = "environment";
		if (session.getAttribute(ENVIRONMENT) != null) {
			env = (Environment) session.getAttribute(ENVIRONMENT);
		}else{
			env = new EnvironmentNone();
		}
		out.println("<font color='white'>Env Type: " + env.getType() + "</font>");
		if(env != null && ( env.getType() == Type.EVENTS || env.getType() == Type.MIXED)){
		String ldapServerName = "ldap://ldapserver:9001";
    	String rootdn = "cn=admin, o=jndiTest";
    	String rootpass = "admin";
    	String rootContext = "o=jndiTest";
		Properties envPro = new Properties();
		envPro.put( Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory" );
       	envPro.put( Context.PROVIDER_URL, ldapServerName );
       	envPro.put( Context.SECURITY_PRINCIPAL, rootdn );
       	envPro.put( Context.SECURITY_CREDENTIALS, rootpass );
       	try {
            // obtain initial directory context using the environment
            DirContext ctx = new InitialDirContext( envPro );
    	}catch(final Exception e){
    		//out.println("LDAP Exception: " + e.getMessage());
    		out.println("<font color='white'>Exception Message: " + e.getMessage() + "</font>");
    		if(e.getMessage().contains("error code 49")){
      %>
      	<font face="Verdana, Helvetica, Arial" color="red" size=2>
		<center>
			<br/><br/>
			<img src="eniq_blue_banner.jpg" /><br /><br/>
			<font color='red' size='2px' face='tahoma'>Invalid user details entered or account locked. Please contact the system administrator.</font><br /><br />
		</center>
		</font>
		<font face="Verdana, Helvetica, Arial" size=2>
		<center>	
			Please <a href="<% out.print(path); %>/servlet/LoaderStatusServlet">Login</a> again.</br>
		</center>
		</font>
	  <%
        	}else{
        		boolean isRepDBOnline = true ;
        		boolean isDwhDBOnline = true ;
        		try{
        			RockFactoryType[] rockDBTypes = DbConnectionFactory.getInstance().initialiseConnections(session);
        			if(rockDBTypes != null){
        				for(RockFactoryType type : rockDBTypes){
                			//out.println("DB Type: " + type.getName());
                			if(type.getRockFactory() == null){
                				if(type.getName().equals(MonitorInformation.IQ_NAME_STATUS_DWH)){
                					isDwhDBOnline = false ;
                					continue ;
                				}
                				if(type.getName().equals(MonitorInformation.IQ_NAME_STATUS_REP)){
                					isRepDBOnline = false ;
                					continue ;
                				}
                				//out.println("DB Type: " + type.getName() + " is Online");
                			}
                		}
        			}else{
        				isRepDBOnline = false ;
            			isDwhDBOnline = false ;
        			}
        		}catch(final Exception ex){
        			isRepDBOnline = false ;
        			isDwhDBOnline = false ;
        		}
        		
        		//out.println("REP DB is ONLINE: " + isRepDBOnline);
        		//out.println("Dwh DB is ONLINE: " + isDwhDBOnline);
      %>
      	<font face="Verdana, Helvetica, Arial" color="red" size=2>
		<center>
			<br/><br/>
			<img src="eniq_blue_banner.jpg" /><br /><br/>
			<font color='red' size='2px' face='tahoma'><b>LDAP service is down. Can not login.</b></font><br /><br />
			
			<table border="1" cellpadding="1" cellspacing="1" width="400px">
					<tr bgcolor="#B3B3FF" align="center">
						<td>
							<font face="Verdana,Helvetica,Arial" size="2"><b>ENIQ Service Name</b></font>
						</td>
						<td>
							<font face="Verdana,Helvetica,Arial" size="2"><b>Status</b></font>
						</td>
					</tr>
				
				
				<tr align="center">
					<td>
						<font face="Verdana,Helvetica,Arial" size="2">LDAP</font>
					</td>
					<td>
						<%
							out.println("<font color='red' size='2px' face='verdana'>" + MonitorInformation.SERVICE_NOT_OK + "</font>");
						%>
						
						
					</td>
				</tr>
				
				<tr align="center">
					<td>
						<font face="Verdana,Helvetica,Arial" size="2">ENIQ REP DB</font>
					</td>
					<td>
						<%
							if(isRepDBOnline){
								out.println("<font color='green' size='2px' face='verdana'>" + MonitorInformation.SERVICE_OK + "</font>");
							}else{
								out.println("<font color='red' size='2px' face='verdana'>" + MonitorInformation.SERVICE_NOT_OK + "</font>");
							}
						%>
					</td>
				</tr>
				
				<tr align="center">
					<td>
						<font face="Verdana,Helvetica,Arial" size="2">ENIQ DWH DB</font>
					</td>
					<td>
						<%
							if(isDwhDBOnline){
								out.println("<font color='green' size='2px' face='verdana'>" + MonitorInformation.SERVICE_OK + "</font>");
							}else{
								out.println("<font color='red' size='2px' face='verdana'>" + MonitorInformation.SERVICE_NOT_OK + "</font>");
							}
						%>
					</td>
				</tr>
				
				<tr align="center">
					<td>
						<font face="Verdana,Helvetica,Arial" size="2">Engine</font>
					</td>
					<td>
						<%
							if(ServicesStatusStore.getEngineStatus().equals(ServicesStatusStore.OFFLINE)){
								out.println("<font color='red' size='2px' face='verdana'>" + MonitorInformation.SERVICE_NOT_OK + "</font>");
							}else{
								out.println("<font color='green' size='2px' face='verdana'>" + MonitorInformation.SERVICE_OK + "</font>");
							}
						%>
						
						
					</td>
				</tr>
				
				
				<tr align="center">
					<td>
						<font face="Verdana,Helvetica,Arial" size="2">Scheduler</font>
					</td>
					<td>
						<%
							if(ServicesStatusStore.getSchedulerStatus().equals(ServicesStatusStore.OFFLINE)){
								out.println("<font color='red' size='2px' face='verdana'>" + MonitorInformation.SERVICE_NOT_OK + "</font>");
							}else{
								out.println("<font color='green' size='2px' face='verdana'>" + MonitorInformation.SERVICE_OK + "</font>");
							}
						%>
						
						
					</td>
				</tr>
				
				
				<tr align="center">
					<td>
						<font face="Verdana,Helvetica,Arial" size="2">License Server</font>
					</td>
					<td>
						<%
							if(ServicesStatusStore.getLicSrvStatus().equals(ServicesStatusStore.OFFLINE)){
								out.println("<font color='red' size='2px' face='verdana'>" + MonitorInformation.SERVICE_NOT_OK + "</font>");
							}else{
								out.println("<font color='green' size='2px' face='verdana'>" + MonitorInformation.SERVICE_OK + "</font>");
							}
						%>
						
						
					</td>
				</tr>
				
				
				<tr align="center">
					<td>
						<font face="Verdana,Helvetica,Arial" size="2">License Manager</font>
					</td>
					<td>
						<%
							if(ServicesStatusStore.getLicMgrStatus().equals(ServicesStatusStore.OFFLINE)){
								out.println("<font color='red' size='2px' face='verdana'>" + MonitorInformation.SERVICE_NOT_OK + "</font>");
							}else{
								out.println("<font color='green' size='2px' face='verdana'>" + MonitorInformation.SERVICE_OK + "</font>");
							}
						%>
						
						
					</td>
				</tr>
				
				<tr align="center">
					<td>
						<font face="Verdana,Helvetica,Arial" size="2">Glassfish</font>
					</td>
					<td>
						<%
							if(ServicesStatusStore.getGlassFishStatus().equals(ServicesStatusStore.OFFLINE)){
								out.println("<font color='red' size='2px' face='verdana'>" + MonitorInformation.SERVICE_NOT_OK + "</font>");
							}else{
								out.println("<font color='green' size='2px' face='verdana'>" + MonitorInformation.SERVICE_OK + "</font>");
							}
						%>
						
						
					</td>
				</tr>
				
				<tr align="center">
					<td>
						<font face="Verdana,Helvetica,Arial" size="2">Mediation Gateway</font>
					</td>
					<td>
						<%
							if(ServicesStatusStore.getMGStatus().equals(ServicesStatusStore.OFFLINE)){
								out.println("<font color='red' size='2px' face='verdana'>" + MonitorInformation.SERVICE_NOT_OK + "</font>");
							}else{
								out.println("<font color='green' size='2px' face='verdana'>" + MonitorInformation.SERVICE_OK + "</font>");
							}
						%>	
					</td>
				</tr>
				
			</table>
			<br /><br />
			
		</center>
		</font>
		
		<font face="Verdana, Helvetica, Arial" size=2>
		<center>	
			Please <a href="<% out.print(path); %>/servlet/LoaderStatusServlet">Login</a> again.</br>
		</center>
		</font>
      
      <%
        	}
    	}// end of catch
		}// if(e != null && ( e.getType() == Type.EVENTS || e.getType() == Type.MIXED)){
		else if(env != null && (env.getType() == Type.NONE)){
	   %>
			<font face="Verdana, Helvetica, Arial" color="red" size=2>
			<center>	
			<br/><br/>
				<img src="eniq_blue_banner.jpg" /><br /><br/>
				<font color='red' size='2px' face='tahoma'>Invalid user details entered or account locked. Please contact the system administrator.</font><br /><font color='red' size='1px' face='tahoma'>Could not fetch Environment type (ENIQ REP database might be down)</font><br/><br />
			</center>
			</font>
			<font face="Verdana, Helvetica, Arial" size=2>
			<center>	
			Please <a href="<% out.print(path); %>/servlet/LoaderStatusServlet">Login</a> again.</br>
			</center>
			</font>		
	  <%	
		}else{
	  %>
			<font face="Verdana, Helvetica, Arial" color="red" size=2>
			<center>	
			<br/><br/>
				<img src="eniq_blue_banner.jpg" /><br /><br/>
		<% 
			if(count < 3){
		%>
			<font color='red' size='2px' face='tahoma'>Login failed, please check your username and password.</font><br /><br />
		<%
			}else if(user != ""){
		%>
			<font color='red' size='2px' face='tahoma'>User <%=user%> has been locked for <%=lockOutTime%> seconds due to 3 failed login attempts. Please try with another user.</font><br /><br />	
		<%
			}
		%>
			</center>
			</font>
			<font face="Verdana, Helvetica, Arial" size=2>
			<center>	
			Please <a href="<% out.print(path); %>/servlet/LoaderStatusServlet">Login</a> again.</br>
			</center>
			</font>
	  <%
		}
	  %>
	
</body>
</html>