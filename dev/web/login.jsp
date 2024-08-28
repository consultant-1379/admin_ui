<%@ page import="com.distocraft.dc5000.etl.gui.cookie.*" %>
<%@ page import="java.net.http.*" %>
<%@ page import="java.util.*"%>
<jsp:useBean id="loginForm"
	class="com.distocraft.dc5000.etl.gui.login.LoginForm" scope="session">
</jsp:useBean>
<jsp:setProperty name="loginForm" property="*" />
<%
	boolean validDetails = false;
	boolean defDetails = false;
	final String contextPath = request.getContextPath();
	if ("true".equals(request.getParameter("process")) && loginForm.process()) {
            validDetails = true;
    }
    if (loginForm.checkDefaults()) {
             defDetails = true;  
    }
    if (request.getMethod().equalsIgnoreCase("get") && (request.getParameter("userName") != null || request.getParameter("userPassword") != null)) {
		loginForm.setUserName("");
		loginForm.setUserPassword("");
    }
    String footerMessage = loginForm.getFooterMessage();
%>
<html>
	<script language="javascript">
        function submitIfPosted() {
            var submit = <%= validDetails %>;
            var defdet = <%= defDetails %>;
            if (submit && defdet) {
                document.getElementById('loginMessage').innerHTML = 'You are using default username and password. Please refer to the SAG document section "Changing AdminUI User Password" and change the password.';
                document.getElementById('username').focus();
            }
            else if(submit && !defdet) {
                     document.forms["login"].username.disabled=true;
                     document.forms["login"].password.disabled=true;
                     document.forms["login"].submit.disabled=true;
                     document.forms["hiddenLogin"].submit();
                     document.getElementById('loginMessage').innerHTML = 'You are now being logged in, please wait...';
            } else {
            	document.getElementById('username').value="";
				document.getElementById('password').value="";
            	document.getElementById('username').focus();
            }
        }
	  </script>
	<head>
		<title>Ericsson Network IQ</title>
		<link rel="shortcut icon" href="<%=contextPath%>/img/eric.ico">
		<script language="javascript" src="<%=contextPath%>/javascript/PreventXSS.js"></script>
	</head>
	<body bgcolor="#ffffff"	onload="submitIfPosted();">
	<div class="container" style="border:15px solid #E9E9E9; border-radius: 10px; margin: auto; width: 500px; min-height: 100px; overflow: hidden;" >
		<center>
			<br/>
			<br/>
			<img src="<%=contextPath%>/eniq_blue_banner.jpg" style="width:90%; height:15%;" />
			<br/>
			<br/>
			<font face="Verdana, Helvetica, Arial" size=3>Management Interface - Login</font>
			<br/>
			<br/>
	<%
	    if (request.getSession().isNew() == false) {
	        out.print("<font face=\"Verdana, Helvetica, Arial\" size=\"2\" id=\"loginMessage\" color=\"red\"><b>You have logged out or your session has expired. Please login again.</b></font></br></br>");
	    }
	%> 
			<font face="Verdana, Helvetica, Arial" size=2>Please, type your username and password.</font>
			<br/>
			<form id="hiddenLogin" action="<%=contextPath%>/j_security_check" method="POST">
                <input type="HIDDEN" name="j_username" value=<%=loginForm.getUserName()%>>
                <input type="HIDDEN" name="j_password" value=<%=loginForm.getUserPassword()%>>
            </form>
	<%
		loginForm.removeUserAfterLockout();
	%>
<%
String token="";
token= UUID.randomUUID().toString();
session.setAttribute("csrfToken",token);

%>		
			<form id="login" autocomplete="off" action='<%=request.getRequestURI()%>' method="POST" onSubmit="return filterFields();">
<%
HttpSession session1 = request.getSession();
if(session1.isNew()){
session.setAttribute("csrfToken",token);
}
%>	

			
				
				<table border=1>
					<tr>
						<td>
							<table border=0 bgcolor="#B3B3FF">
									<tr>
										<td>
											<font face="Verdana, Helvetica, Arial" size=2>Username: </font>
										</td>
										<td>
											<input type="TEXT" name="userName" id="username" style="font-size: 12;" value=<%=loginForm.getUserName()%>>
										</td>
									</tr>
									<tr>
										<td>
											<font face="Verdana, Helvetica, Arial" size=2>Password: </font>
										</td>
										<td>
											<input type="PASSWORD" name="userPassword" style="font-size: 12;" id="password" value=<%=loginForm.getUserPassword()%>>
										</td>
									</tr>
									<%session.setAttribute("username",loginForm.getUserName());%>
									
									<tr>
										<td colspan=2 align="center">
											<br />
											<input type=submit value="Login" name="submit" id="submit" style="font-size: 13f;">
											<input type="HIDDEN" name="process" value="true">
										</td>
									</tr>
							</table>
						</td>
					</tr>
				</table>
			</form>
<%--  Legal notice message EQEV-88506  --%>

<br> </br>
<style> 
.indented { 
padding-left: 20px; 
padding-right: 20pt;
align-content: space-around;
 }
 </style>

<div class="indented">
<b><p align="left" style="font-size:13px; font-family: Arial, Helvitika, sans-serif">Important Legal Notice </p></b>
<p align="left" style="background-color:rgb(255,255,255); font-family: Arial, Helvitika, sans-serif; font-size: 11px; border-color:#000000; padding:0.00065em; ">
<%=footerMessage%>
</p>
</div>
</div>
		</center>
	</body>
</html>