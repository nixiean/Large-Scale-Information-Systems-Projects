package com.servlet;

import java.io.IOException;
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

import com.scheduler.SessionCleaner;
import com.scheduler.ViewExchangerThread;
import com.util.SessionUtil;
import com.view.ServerStatus;
import com.view.ServerStatus.ServerStatusCode;

/**
 * Servlet implementation class EnterServlet
 */
@WebServlet("/EnterServlet")
public class EnterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static HashMap<String, ServerStatus> myView = new HashMap<String, ServerStatus>();
	
	
	@Override
	public void init() throws ServletException {
		super.init();
		ServletContext context = getServletContext();
		//initialize the hashtable once the container starts
		Hashtable<String, String> sessionTable = new Hashtable<String, String>();
		context.setAttribute("sessionTable", sessionTable);
		
		//Initialize the session cleaner
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		Runnable sessionCleaner = new SessionCleaner(sessionTable);
		//spawn the sessionCleaner thread in every 5 mins, as cookies get timout by 3 mins
		executor.scheduleAtFixedRate(sessionCleaner, 0, 5, TimeUnit.MINUTES);
		
		myView.put(SessionUtil.getIpAddress(), new ServerStatus(ServerStatusCode.UP));
		//context.setAttribute("myView", myView);
		Runnable viewExchangerThread = new ViewExchangerThread();
		executor.scheduleAtFixedRate(viewExchangerThread, 0, 1, TimeUnit.MINUTES);
	}
	
    /**
     * Default constructor. 
     */
    public EnterServlet() {
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Hashtable<String, String> sessionHashTable = 
				(Hashtable<String, String>) request.getServletContext().getAttribute("sessionTable");
		String param = request.getParameter("submit");
		boolean isRefresh = null != param ? param.equals("Refresh") : false;
		boolean isReplace = null != param ? param.equals("Replace") : false;
		boolean isLogout = null != param ? param.equals("Logout") : false;

		if(isRefresh) {
			handleRefresh(request, response);
		} else if(isReplace) {
			handleReplace(request, response);
		} else if(isLogout) {
			handleLogout(request, response);
		} else {
		
			Cookie myCookie = getExistingCookie(request); 
			if(myCookie == null) {
				String uniqueCookie = SessionUtil.getUniqueCookie();
				myCookie = new Cookie("CS5300PROJ1SESSION", uniqueCookie);
			}
			String sessionId = SessionUtil.getSessionId(myCookie.getValue());
			request.setAttribute("currentSessionId", sessionId);
			String welcomeMessage = (null == sessionHashTable.get(sessionId)) ? "Hello User" : (sessionHashTable.get(sessionId).split("_"))[0];
			myCookie.setMaxAge(3*60);
			String cookieExpireTs = getExpiryTimeStamp(3);
			request.setAttribute("timeStamp", cookieExpireTs);
			incrementVersionNumber(myCookie);
			//TODO implemenet location Metadata
			String serializedSessionMsg = serializeSessionObject(welcomeMessage, SessionUtil.getVersionNumber(myCookie.getValue()), cookieExpireTs, "");
			sessionHashTable.put(sessionId, serializedSessionMsg);
			response.addCookie(myCookie);
			request.setAttribute("cookieMsg", myCookie.getValue());
			RequestDispatcher dispatcher = request.getRequestDispatcher("home.jsp");
			dispatcher.forward(request, response);
		}
	}
	
	private void handleReplace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Hashtable<String, String> sessionHashTable = 
				(Hashtable<String, String>) request.getServletContext().getAttribute("sessionTable");
		Cookie myCookie = getExistingCookie(request);
		String msgParam = request.getParameter("messagebox");
		String welcomeMessage = (null != msgParam) ? msgParam : "Hello User";
		if(myCookie != null) {
			incrementVersionNumber(myCookie);
			myCookie.setMaxAge(3*60);
		} else {
			String uniqueCookie = SessionUtil.getUniqueCookie();
			myCookie = new Cookie("CS5300PROJ1SESSION", uniqueCookie);
		}
		
		String sessionId = SessionUtil.getSessionId(myCookie.getValue());
		request.setAttribute("currentSessionId", sessionId);
		String cookieExpireTs = getExpiryTimeStamp(3);
		//TODO implemenet location Metadata
		String serializedSessionMsg = serializeSessionObject(welcomeMessage, SessionUtil.getVersionNumber(myCookie.getValue()), cookieExpireTs, "");
		sessionHashTable.put(sessionId, serializedSessionMsg);
		request.setAttribute("cookieMsg", myCookie.getValue());
		response.addCookie(myCookie);
		RequestDispatcher dispatcher = request.getRequestDispatcher("home.jsp");
		dispatcher.forward(request, response);
	}
	
	private void handleRefresh(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Hashtable<String, String> sessionHashTable = 
				(Hashtable<String, String>) request.getServletContext().getAttribute("sessionTable");
		Cookie myCookie = getExistingCookie(request);
		if(myCookie != null) {
			incrementVersionNumber(myCookie);
		} else {
			String uniqueCookie = SessionUtil.getUniqueCookie();
			myCookie = new Cookie("CS5300PROJ1SESSION", uniqueCookie);
		}
		
		String sessionId = SessionUtil.getSessionId(myCookie.getValue());
		String welcomeMessage = (null == sessionHashTable.get(sessionId)) ? "Hello User" : sessionHashTable.get(sessionId).split("_")[0];
		request.setAttribute("currentSessionId", sessionId);
		String cookieExpireTs = getExpiryTimeStamp(3);
		//TODO implemenet location Metadata
		String serializedSessionMsg = serializeSessionObject(welcomeMessage, SessionUtil.getVersionNumber(myCookie.getValue()), cookieExpireTs, "");
		sessionHashTable.put(sessionId, serializedSessionMsg);
		myCookie.setMaxAge(3*60);
		request.setAttribute("cookieMsg", myCookie.getValue());
		response.addCookie(myCookie);
		RequestDispatcher dispatcher = request.getRequestDispatcher("home.jsp");
		dispatcher.forward(request, response);
	}
	
	private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Hashtable<String, String> sessionHashTable = 
				(Hashtable<String, String>) request.getServletContext().getAttribute("sessionTable");
		Cookie myCookie = getExistingCookie(request);
		if(myCookie != null) {
			myCookie.setMaxAge(0);
			sessionHashTable.remove(SessionUtil.getSessionId(myCookie.getValue()));
			response.addCookie(myCookie);
		}
		RequestDispatcher dispatcher = request.getRequestDispatcher("home.jsp");
		dispatcher.forward(request, response);
	}
	
	private static String getExpiryTimeStamp(int expiryMin) {
		Date dNow = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dNow);
		cal.add(Calendar.MINUTE, expiryMin);
		dNow = cal.getTime();
		return new Timestamp(dNow.getTime()).toString();
	}
	
	private Cookie getExistingCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		Cookie myCookie = null; 
		if(cookies != null) {
			for(Cookie cookie : cookies) {
				if(cookie.getName().equals("CS5300PROJ1SESSION")) {
					myCookie = cookie;
				}
			}
		}
		return myCookie;
	}
	
	private static synchronized void incrementVersionNumber(Cookie cookie) {
		String oldVal = cookie.getValue();
		int versionNumber = Integer.parseInt(SessionUtil.getVersionNumber(oldVal));
		versionNumber++;
		String newVal = SessionUtil.getUpdatedCookieValue(oldVal, versionNumber);
		cookie.setValue(newVal);
	}
	
	private static String serializeSessionObject(String versionNumber, String message, String timeStamp, String locationMetadata) {
		return versionNumber + "_" + message + "_" + timeStamp + "_" + locationMetadata;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
