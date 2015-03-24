package com.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class PrintSessionTableServlet
 */
@WebServlet("/print")
public class PrintSessionTableServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PrintSessionTableServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Hashtable<String, String> sessionHashTable = EnterServlet.sessionTable;
		PrintWriter out = response.getWriter();
		if(sessionHashTable.isEmpty()) {
			out.print("No active sessions at this time");
		} else {
			out.println("<table style='width:60%' border='1'>");
			out.println("<tr><th>Session Id</th>");
			out.println("<th>Message</th>");
			out.println("<th>Version Number</th>");
			out.println("<th>Timestamp</th></tr>");
			for(String sessionId : sessionHashTable.keySet()) {
				String[] valueTokens = sessionHashTable.get(sessionId).split("_");
				out.println("<tr><td>" + sessionId + "</td><td>" + valueTokens[0] + "</td><td>" + valueTokens[1] + "</td><td>" + valueTokens[2] + "</td></tr>");
			}
			out.println("</table>");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
