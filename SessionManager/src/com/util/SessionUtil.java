package com.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import com.rpc.SMRPCClient;
import com.servlet.EnterServlet;
import com.sun.xml.internal.bind.v2.TODO;
import com.view.ServerStatus;
import com.view.ServerStatus.ServerStatusCode;

public class SessionUtil {

	private static int sessionCounter = 0;
	private static String localIpAddress = null;

	// make it synchronized so the unique id will be thread safe
	public static synchronized String getUniqueCookie() {
		int sessionNum = ++sessionCounter;
		String localSvrId = getIpAddress();
		String sessionId = sessionNum + "," + localSvrId;

		// version number is initialized to 1
		long versionNumber = 1L;
		String cookieExpireTs = getExpiryTimeStamp(EnterServlet.COOKIE_MAX_AGE);

		String sessionData = "Hello User" + "_" + versionNumber + "_" + cookieExpireTs;
		
		//Update the local session table. 
		EnterServlet.sessionTable.put(sessionId,sessionData);
		
		// Choose resilience - 1 backups
		String serverBackups = getRandomBackupServers(localSvrId,
				sessionId, versionNumber, sessionData, cookieExpireTs);

		return serializeCookieData(sessionId, versionNumber, serverBackups);
	}

	public static String serializeCookieData(String sessionId,
			long versionNumber, String serverBackups) {
		StringBuilder cookieData = new StringBuilder();
		cookieData.append(sessionId).append("_").append(versionNumber)
				.append("_").append(serverBackups);
		return cookieData.toString();
	}

	public static String getRandomBackupServers(String localSvrId,
			String sessionId, long versionNumber, String sessionData,
			String cookieExpireTs) {
		int numRandomServers = EnterServlet.RESILIENCY;
		List<String> activeServerId = new ArrayList<String>();

		// List only those servers which are UP
		for (Entry<String, ServerStatus> serverEntry : EnterServlet.myView
				.entrySet()) {
			String serverID = serverEntry.getKey();
			ServerStatus serverStatus = serverEntry.getValue();
			if (serverStatus.getStatus().equals(ServerStatusCode.UP)
					&& !localSvrId.equals(serverID)) {
				activeServerId.add(serverID);
			}
		}

		SMRPCClient rpcClient = SMRPCClient.getInstance();
		StringBuilder retRandomServers = new StringBuilder(localSvrId).append(",");
		Random rand = new Random();
		while (numRandomServers-- > 0) {
			int index = rand.nextInt(activeServerId.size());
			String destinationAddress = activeServerId.get(index);
			if (rpcClient.sendForSessionWrite(sessionId,
					String.valueOf(versionNumber), sessionData, cookieExpireTs,
					destinationAddress) != SMRPCClient.FAILURE) {
				retRandomServers.append(destinationAddress).append(",");
				activeServerId.remove(index);
			} else { 
				retRandomServers.append("NULL").append(",");
			}
		}

		return retRandomServers.toString();
	}

	public static void main(String[] args) {
		System.out.println(getIpAddress());
	}

	public static String getIpAddress() {
		try {
			if (localIpAddress == null) {
				Runtime rt = Runtime.getRuntime();
				String command = "/opt/aws/bin/ec2-metadata --public-ipv4";

				Process p = rt.exec(command);
				BufferedReader stdInput = new BufferedReader(
						new InputStreamReader(p.getInputStream()));

				String s = null;
				while ((s = stdInput.readLine()) != null) {
					localIpAddress = s.split(": ")[1];
				}
			}

		} catch (Exception e) {
			try {
				localIpAddress = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();

			}
		}
		return localIpAddress;
	}

	public static String getSessionId(String cookie) {
		String[] parsedStr = cookie.split("_");
		return parsedStr[0];
	}

	public static String getVersionNumber(String cookie) {
		String[] parsedStr = cookie.split("_");
		return parsedStr[1];
	}

	public static String getLocationMetaData(String cookie) {
		String[] parsedStr = cookie.split("_");
		return parsedStr[2];
	}

	// helper method to get the updated cookie with new version number
	public static String getUpdatedCookieValue(String oldCookie,
			int newVersionNumber) {
		String[] parsedStr = oldCookie.split("_");
		parsedStr[1] = Integer.toString(newVersionNumber);
		return parsedStr[0] + "_" + parsedStr[1] + "_" + parsedStr[2];
	}

	private static String getExpiryTimeStamp(int expiryMin) {
		Date dNow = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dNow);
		cal.add(Calendar.MINUTE, expiryMin);
		dNow = cal.getTime();
		return new Timestamp(dNow.getTime()).toString();
	}

}
