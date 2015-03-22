<%@page import="com.servlet.EnterServlet"%>
<%@page import="com.util.SessionUtil"%>
<%@page import="java.util.Hashtable"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Welcome</title>
</head>
<body>
	<%
		Hashtable<String, String> sessionHashTable = (Hashtable<String, String>) request
				.getServletContext().getAttribute("sessionTable");
		String welcomeMsg = null;
		String versionNumber = null;
		String timeStamp = null;
		String sessionIdParam = (String) request
				.getAttribute("currentSessionId");
		String serializedSessionMsg = null;
		if (sessionIdParam != null) {
			serializedSessionMsg = sessionHashTable.get(sessionIdParam);
			String[] tokens = serializedSessionMsg.split("_");
			welcomeMsg = tokens[0];
			versionNumber = tokens[1];
			timeStamp = tokens[2];
		}
	%>
	<h3>
		<%
			if (welcomeMsg != null) {
				out.println(welcomeMsg);
			} else {
				out.println("Hello User");
			}
		%>
	</h3>
	<form action="EnterServlet" method="post">
		<table>
			<tr>
				<td><input type="submit" name="submit" value="Replace"></td>
				<td><input type="text" name="messagebox"></td>
			</tr>
			<tr>
				<td><input type="submit" name="submit" value="Refresh"></td>
			</tr>
			<tr>
				<td><input type="submit" name="submit" value="Logout"></td>
			</tr>
		</table>
	</form>
	</br>
	</br>
	<%
		String msg = (String) request.getAttribute("cookieMsg");
		if (msg != null) {
			out.println(msg);
		}
	%>
	</br>
	</br>
	<%
		if (timeStamp != null) {
			out.println("Cookie expires at:" + timeStamp);
		}
	%>
	</br>
	<h5>
		Click <a href="./print">here</a> to print all active sessions
	</h5>

	<%
		for (String key : EnterServlet.myView.keySet()) {
			out.println("myViewEntry :" + key + ","
					+ EnterServlet.myView.get(key).getStatus() + ","
					+ EnterServlet.myView.get(key).getStatus());
	%>
	</br>
	<%
		}
	%>

</body>
</html>