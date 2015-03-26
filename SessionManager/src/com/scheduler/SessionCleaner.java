package com.scheduler;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/*
 * Demon thread which wakes up every five minutes and deletes the timedout session from the session state
 * table. It checks the time stamp of all the session ids in the session state table
 * and deletes the sessions whose time stamps are older than default 3 minutes cookie timeout period 
 */

public class SessionCleaner implements Runnable{

	private static Hashtable<String, String> sessionTable;
	//delete all the sessions older than 3 minutes
	private static final double sessionTimeOutInMins = 3.0;
	private long timeout = 60*1000;
	
	public SessionCleaner(Hashtable<String, String> sessionTable, long timeout) {
		//Initializes the session table
		this.sessionTable = sessionTable;
		this.timeout = timeout;
	}
	
	@Override
	public void run() {
		while(true) {
			System.out.println("Session Cleaner Thread started at:" + new Date().toString());
			List<String> sessionIdsToRemove = new ArrayList<String>();
			for(String sessionId : sessionTable.keySet()) {
				String expiryTs = sessionTable.get(sessionId).split("_")[2];
				Timestamp expiryTimeStamp = Timestamp.valueOf(expiryTs);
				long t1 = expiryTimeStamp.getTime();
				long t2 = System.currentTimeMillis();
				//since timestamp is absolute now just check if current time is greater than expired timestamp
				if(t2 > t1) {
					sessionIdsToRemove.add(sessionId);
				}
			}
			
			//Remove the timeout sessions
			for(String sessionId : sessionIdsToRemove) {
				System.out.println("Session Cleaner removing expired session id:" + sessionId);
				sessionTable.remove(sessionId);
			}
			
			try {
				System.out.println("Session Cleaner Thread sleeping at:" + new Date().toString());
				Thread.sleep(timeout);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
