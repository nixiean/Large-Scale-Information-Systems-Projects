package com.servlet;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rpc.SMRPCClient;
import com.rpc.SMRPCServer;
import com.scheduler.SessionCleaner;
import com.scheduler.ViewExchangerThread;
import com.util.SessionUtil;
import com.view.ServerStatus;
import com.view.ViewUtils;
import com.view.ServerStatus.ServerStatusCode;

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
	private static final long SESSION_CLEANER_INTERVAL = 5; // 5 minutes
	private static final long EXCHANGE_VIEW_INTERVAL = 1; // 1 minute
	public static final int RESILIENCY = 2;

	private static final String COOKIE_NAME = "CS5300PROJ1SESSION";

	@Override
	public void init() throws ServletException {
		super.init();
		// Initialize the session cleaner
		ScheduledExecutorService executor = Executors
				.newSingleThreadScheduledExecutor();
		Runnable sessionCleaner = new SessionCleaner(sessionTable);
		// spawn the sessionCleaner thread in every 5 mins, as cookies get
		// timeout by 3 mins
		executor.scheduleAtFixedRate(sessionCleaner, 0,
				SESSION_CLEANER_INTERVAL, TimeUnit.MINUTES);

		// TODO Remove this hardcode
		//myView.put("128.84.216.62", new ServerStatus(ServerStatusCode.UP));
		//myView.put("128.84.216.61", new ServerStatus(ServerStatusCode.UP));
		
		// Spawn the exchange view thread
		Runnable viewExchangerThread = new ViewExchangerThread();
		executor.scheduleAtFixedRate(viewExchangerThread, 0,
				EXCHANGE_VIEW_INTERVAL, TimeUnit.MINUTES);

		// Start daemon RPC server thread
		//executor.execute(new SMRPCServer());
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
		
		Cookie myCookie = getExistingCookie(request);
		if (myCookie == null) {
			// New request
			handleNewUser(request, response);
		}

		String param = request.getParameter("submit");
		boolean isRefresh = null != param ? param.equals("Refresh") : false;
		boolean isReplace = null != param ? param.equals("Replace") : false;
		boolean isLogout = null != param ? param.equals("Logout") : false;


		if (isReplace) {
			String msgParam = request.getParameter("messagebox");
			String welcomeMessage = (null != msgParam) ? msgParam
					: "Hello User";
			handleReturningUser(request, response, myCookie, welcomeMessage);
		} else if (isLogout) {
			handleLogout(request, response, myCookie);
		} else {
			//For refresh and browser reload
			handleReturningUser(request, response, myCookie, null);
		}
	}

	private void handleReturningUser(HttpServletRequest request,
			HttpServletResponse response, Cookie myCookie, String welcomeMessage)
			throws ServletException, IOException {
		if (myCookie == null) {
			// New request
			handleNewUser(request, response);
		}
		//TODO Can we use cookieValue directly without checking for name?
		String sessionId = SessionUtil.getSessionId(myCookie.getValue());
		String sessionData = null;

		// Check for local table
		if (sessionTable.containsKey(sessionId)) {
			sessionData = sessionTable.get(sessionId);
		} else {
			
			String locationMetadata = SessionUtil.getSessionId(myCookie.getValue());
			
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

			// Choose resilience - 1 backups
			String serverBackups = SessionUtil.getRandomBackupServers(
					localSvrId, sessionId, newVersionNumber, sessionData,
					cookieExpireTs);

			Cookie newCookie = new Cookie(COOKIE_NAME,
					SessionUtil.serializeCookieData(sessionId,
							newVersionNumber, serverBackups));

			displayUserPage(request, response, newCookie, sessionId,
					cookieExpireTs);

		} else {

			// TODO Return invalid Login if this check fails
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
			HttpServletResponse response) throws ServletException, IOException {
		String uniqueCookie = SessionUtil.getUniqueCookie();
		Cookie newCookie = new Cookie(COOKIE_NAME, uniqueCookie);
		String sessionId = SessionUtil.getSessionId(newCookie.getValue());
		String cookieExpireTs = SessionUtil.getExpiryTimeStamp(COOKIE_MAX_AGE);

		displayUserPage(request, response, newCookie, sessionId, cookieExpireTs);
	}

	private void handleLogout(HttpServletRequest request,
			HttpServletResponse response, Cookie myCookie)
			throws ServletException, IOException {

		myCookie.setMaxAge(0);
		// TODO Check if stale session Data has to be removed or not
		sessionTable.remove(SessionUtil.getSessionId(myCookie.getValue()));
		response.addCookie(myCookie);
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
