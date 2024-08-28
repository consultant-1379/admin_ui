<html>
<head>
<title>Ericsson Network IQ</title>
<link rel="shortcut icon" href="../img/eric.ico">
</head>
<body bgcolor="#ffffff">
<center><br>
<br>
<img src="eniq_blue_banner.jpg" /><br />

<%
  String dbName = (String)request.getSession().getAttribute("dbName");
  
  if(dbName == null) {
    dbName = "";
  }
  
%>

<br>
<table border="0">
	<tr>
		<td class="basic">
		<font face="Verdana, Helvetica, Arial" size=3>Cannot connect to database or database is full. Please contact Ericsson for support.</td>
		</td>
	</tr>
</table>
</form>
</center>
</html>
