package com.util;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.UUID;

import com.sun.xml.internal.bind.v2.TODO;


public class SessionUtil {
	
	private static int sessionCounter = 0;
	private static String localIpAddress = null;
	//make it synchronized so the unique id will be thread safe
	public static synchronized String getUniqueCookie() {
		int sessionNum = ++sessionCounter;
		String svrId = getIpAddress();
		
		String sessionId = sessionNum + "," + svrId;
		//version number is initialized 1
		long versionNumber = 1L;
		
		//TODO
		//change it later to pass dynamically
		String  serverPrimary = "10.148.9.209";
		String  serverBackup = "10.148.13.190";
		
		return sessionId + "_" + versionNumber + "_" + serverPrimary+ "," + serverBackup;
	}
	
	public static void main(String[] args) {
		System.out.println(getIpAddress());
	}
	
	public static String getIpAddress() {
		try {
			if(localIpAddress == null) {
				Runtime rt = Runtime.getRuntime();
				String command = "/opt/aws/bin/ec2-metadata --public-ipv4";
	
				Process p = rt.exec(command);
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
	
				String s = null;
				while ((s = stdInput.readLine()) != null) {
					localIpAddress = s.split(": ")[1];
				}
			}
			
		} catch(Exception e) {
			try {
				localIpAddress = InetAddress.getLocalHost().getHostAddress();
			} catch(UnknownHostException e1) {
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

	//helper method to get the updated cookie with new version number
	public static String getUpdatedCookieValue(String oldCookie, int newVersionNumber) {
		String[] parsedStr = oldCookie.split("_");
		parsedStr[1] = Integer.toString(newVersionNumber);
		return parsedStr[0] + "_" + parsedStr[1] + "_" + parsedStr[2];
	}
}
