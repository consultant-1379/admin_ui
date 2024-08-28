<%@ page import="java.io.*" %>
<%@ page import="java.lang.*" %>
<%

String path = request.getContextPath();

%>

<html>
<head>
<title>Ericsson Network IQ</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="content-security-policy" content="default-src 'self'; script-src 'self' 'sha256-jjqG08CsS9MyWicfHPv8IPN3LpH2uPLUTn8EI+GTUXs='">
<title><%=request.getServletContext().getServerInfo() %></title>
<link href="eric.ico" rel="icon" type="image/x-icon" />
<link href="eric.ico" rel="shortcut icon" type="image/x-icon" />
<script>
function redirect(){
	window.location.href = "<% out.print(path); %>/servlet/LoaderStatusServlet";
}
window.addEventListener('load', redirect);
</script>
</head>
 
</html>
 