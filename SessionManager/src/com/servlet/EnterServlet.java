package com.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.rpc.SMRPCServer;
import com.scheduler.SessionCleaner;
import com.scheduler.ViewExchangerThread;
import com.util.SessionUtil;
import com.view.ServerStatus;

/**
 * Servlet implementation class EnterServlet
 */
@WebServlet(value="/EnterServlet", loadOnStartup=1)
public class EnterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// Local view with server details
	public static HashMap<String, ServerStatus> myView = new HashMap<String, ServerStatus>();
	// User session details
	public static Hashtable<String, String> sessionTable = new Hashtable<String, String>();

	public static final int COOKIE_MAX_AGE = 3; // 3 minutes
	private static final long SESSION_CLEANER_INTERVAL = 5*60*1000; // 5 minutes
	private static final long EXCHANGE_VIEW_INTERVAL = 1*60*1000; // 1 minute
	public static final int RESILIENCY = 2;

	private static final String COOKIE_NAME = "CS5300PROJ1SESSION";

	@Override
	public void init() throws ServletException {
		super.init();
		Runnable sessionCleaner = new SessionCleaner(sessionTable, SESSION_CLEANER_INTERVAL);
		//spawn the sessionCleaner thread in every 5 mins
		(new Thread(sessionCleaner)).start();
		//spawn the sessionCleaner thread in every 5 mins
		(new Thread(new ViewExchangerThread(EXCHANGE_VIEW_INTERVAL))).start();
		//Start daemon RPC server thread indefinitely
		(new Thread(new SMRPCServer())).start();
	}

	/**
	 * Default constructor.
	 */
	public EnterServlet() {
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		String param = request.getParameter("submit");
		boolean isRefresh = null != param ? param.equals("Refresh") : false;
		boolean isReplace = null != param ? param.equals("Replace") : false;
		boolean isLogout = null != param ? param.equals("Logout") : false;
		boolean noParamSet = false || isReplace || isLogout;
		
		Cookie myCookie = getExistingCookie(request);
		if (myCookie == null && !noParamSet) {
			// New request
			handleNewUser(request, response, "Hello User");
			return;
		}

		if (isReplace) {
			String msgParam = request.getParameter("messagebox");
			String welcomeMessage = (null != msgParam) ? msgParam
					: "Hello User";
			handleReturningUser(request, response, myCookie, welcomeMessage);
			return;
		} else if (isLogout) {
			handleLogout(request, response, myCookie);
			return;
		} else {
			//For refresh and browser reload
			handleReturningUser(request, response, myCookie, null);
			return;
		}
	}

	private void handleReturningUser(HttpServletRequest request,
			HttpServletResponse response, Cookie myCookie, String welcomeMessage)
			throws ServletException, IOException {
		if (myCookie == null) {
			// New request
			handleNewUser(request, response, welcomeMessage);
			return;
		}
		//TODO Can we use cookieValue directly without checking for name?
		String sessionId = SessionUtil.getSessionId(myCookie.getValue());
		String sessionData = null;

		// Check for local table
		if (sessionTable.containsKey(sessionId)) {
			sessionData = sessionTable.get(sessionId);
		} else {
			
			String locationMetadata = myCookie.getValue().split("_")[2];
			
			// Do Session Read to check user's validity
			sessionData = SessionUtil.getSessionDataFromBackupServers(sessionId,locationMetadata);
		}
		// Do random writes
		if (sessionData != null) {

			String[] sessionTokens = sessionData.split("_");

			String localSvrId = SessionUtil.getIpAddress();

			// version number is initialized to 1
			long newVersionNumber = Long.parseLong(sessionTokens[1]) + 1;

			if (welcomeMessage == null) {
				// Non replace message case
				welcomeMessage = sessionTokens[0];
			}

			String cookieExpireTs = SessionUtil.getExpiryTimeStamp(COOKIE_MAX_AGE);

			sessionData = welcomeMessage + "_" + newVersionNumber + "_" + cookieExpireTs;

			// Update the local session table.
			EnterServlet.sessionTable.put(sessionId, sessionData);

			// Choose resilience  backups
			String serverBackups = SessionUtil.getRandomBackupServers(
					localSvrId, sessionId, newVersionNumber, sessionData,
					cookieExpireTs);

			Cookie newCookie = new Cookie(COOKIE_NAME,
					SessionUtil.serializeCookieData(sessionId,
							newVersionNumber, serverBackups));

			displayUserPage(request, response, newCookie, sessionId,
					cookieExpireTs);

		} else {
			//send error page and delete cookie
			myCookie.setMaxAge(0);
			response.addCookie(myCookie);
			RequestDispatcher dispatcher = request.getRequestDispatcher("error.jsp");
			dispatcher.forward(request, response);
		}

	}

	private void displayUserPage(HttpServletRequest request,
			HttpServletResponse response, Cookie newCookie, String sessionId,
			String cookieExpireTs) throws ServletException, IOException {
		newCookie.setMaxAge(COOKIE_MAX_AGE * 60);

		request.setAttribute("currentSessionId", sessionId);
		request.setAttribute("timeStamp", cookieExpireTs);

		response.addCookie(newCookie);
		request.setAttribute("cookieMsg", newCookie.getValue());

		RequestDispatcher dispatcher = request.getRequestDispatcher("home.jsp");
		dispatcher.forward(request, response);
	}

	private void handleNewUser(HttpServletRequest request,
			HttpServletResponse response, String welcomeMsg) throws ServletException, IOException {
		//String msgBoxStr = request.getParameter("messagebox");
		//String welcomeMsg = (null != msgBoxStr)  ? msgBoxStr : "Hello User" ;

		String uniqueCookie = SessionUtil.getUniqueCookie(welcomeMsg);
		Cookie newCookie = new Cookie(COOKIE_NAME, uniqueCookie);
		String sessionId = SessionUtil.getSessionId(newCookie.getValue());
		String cookieExpireTs = SessionUtil.getExpiryTimeStamp(COOKIE_MAX_AGE);

		displayUserPage(request, response, newCookie, sessionId, cookieExpireTs);
	}

	private void handleLogout(HttpServletRequest request,
			HttpServletResponse response, Cookie myCookie)
			throws ServletException, IOException {
		if(myCookie != null) {
			myCookie.setMaxAge(0);
			// TODO Check if stale session Data has to be removed or not
			sessionTable.remove(SessionUtil.getSessionId(myCookie.getValue()));
			response.addCookie(myCookie);
		}
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("home.jsp");
		dispatcher.forward(request, response);
	}

	private Cookie getExistingCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		Cookie myCookie = null;
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(COOKIE_NAME)) {
					myCookie = cookie;
				}
			}
		}
		return myCookie;
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
