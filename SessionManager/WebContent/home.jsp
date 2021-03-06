<%@page import="com.view.ServerStatus.ServerStatusCode"%>
<%@page import="com.view.ServerStatus"%>
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
		String welcomeMsg = null;
		String versionNumber = null;
		String timeStamp = null;
		String sessionIdParam = (String) request
				.getAttribute("currentSessionId");
		String serializedSessionMsg = null;
		if (sessionIdParam != null) {
			//TODO
			serializedSessionMsg = EnterServlet.sessionTable
					.get(sessionIdParam);
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
	String msg = null;
	if (timeStamp != null) {
		out.println("Current Server ID : " + SessionUtil.getIpAddress() + "	</br>");	
		if (EnterServlet.primaryOrBackupSessionRead.equals("P")) {
			out.println("Existing Session found in: "
					+ EnterServlet.readFromServer + " </br>");
			out.println("Existing Session found in Primary Server"  + " </br>");
		} else if (EnterServlet.primaryOrBackupSessionRead.equals("B")) {
			out.println("Existing Session found in: "
					+ EnterServlet.readFromServer + " </br>");
			out.println("Existing Session found in Backup Server" + " </br>");
		}
		out.println("Primary Server for updated session: "
				+ SessionUtil.getIpAddress() + " </br>");
		msg = (String) request.getAttribute("cookieMsg");
		if (msg != null) {
			String backupServers = msg.split("_")[2];
			out.println("Backup Servers for updated session: " + backupServers + " </br>");
		}

			out.println("Cookie expires at: " + timeStamp + " </br>");
		if (EnterServlet.discardTime == null) {
			out.println("Discard time: " + timeStamp + " </br>");
		}
		else {
			out.println("Discard time: " + EnterServlet.discardTime + " </br>");
		}
		}
			
			

		if (msg != null) {
			out.println("</br>"+ "Cookie Details:");
			String[] sessionDetails = msg.split("_");
	%>
	<table style="width: 40%; border: 2px solid black;">
		<tr bgcolor="#C9C9C9">
			<td><b>SessionID</b></td>
			<td><b>Version Number</b></td>
			<td><b>Location Metadata</b></td>
		</tr>
		<tr>
			<td><b> <%
 	out.print(sessionDetails[0]);
 %>
			</b></td>
			<td><b> <%
 	out.print(sessionDetails[1]);
 %>
			</b></td>
			<td><b> <%
 	out.print(sessionDetails[2]);
 %>
			</b></td>
		</tr>
	</table>
	<%
		}
	%>
	</br>
	</br>
	<h5>
		Click <a href="./print">here</a> to print all active sessions
	</h5>

	<%
		out.println("Server details in local view:");
	%>
	<table style="width: 40%; border: 2px solid black">
		<tr bgcolor="#C9C9C9">
			<td><b>ServerID</b></td>
			<td><b>Status</b></td>
			<td><b>Last Updated on</b></td>
		</tr>
		<%
			for (String key : EnterServlet.myView.keySet()) {
				String statusColor;
				ServerStatus viewSrvStatusObj = EnterServlet.myView.get(key);

				if (viewSrvStatusObj.getStatus() == ServerStatusCode.UP)
					statusColor = "#A5DE43";
				else
					statusColor = "#ED4A4A";
		%>
		<tr bgcolor=<%=statusColor%>>
			<td><%=key%></td>
			<td><%=viewSrvStatusObj.getStatus()%></td>
			<td><%=viewSrvStatusObj.getTime()%></td>
		</tr>
		<%
			}
		%>

	</table>

</body>
</html>