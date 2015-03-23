package com.view;

import java.util.HashMap;
import java.util.Map.Entry;

import com.servlet.EnterServlet;
import com.util.AWSSimpleDbUtil;

public class ViewUtils {
	//exchange with simpledb
	private static HashMap<String, ServerStatus> hisView ;
	private static HashMap<String, ServerStatus> myView = EnterServlet.myView;
	
	/*
	 * Init for exchanging view with simpleDB
	 */
	public static void exchangeViewWithSimpleDb() {
		AWSSimpleDbUtil.initSimpleDbInstance();
		hisView = AWSSimpleDbUtil.awsSimpleDBGet();
		//merge view here
		updateLocalView();
		AWSSimpleDbUtil.awsSimpleDBPut(myView);
	}
	
	/*public static void main(String[] args) {
		EnterServlet.myView.put("192.168.0.1", new ServerStatus(ServerStatusCode.UP));
		exchangeViewWithSimpleDb();
	}*/
	
	/*
	 * Exchange the views between local and foreign view
	 */
	private static void updateLocalView() {
		for (Entry<String, ServerStatus> serverEntry : hisView.entrySet()) {
			String serverID = serverEntry.getKey();
			ServerStatus serverStatus = serverEntry.getValue();
			// If the serverID is not present in the local view, then add it to
			// the local view
			if (!myView.containsKey(serverID)) {
				myView.put(serverID, serverStatus);
			} else {
				// Update the server status according to the latest timestamp
				if (serverStatus.getTime() > myView.get(serverID).getTime()) {
					myView.put(serverID, serverStatus);
				}
			}
		}
		hisView.clear();
	}
	
	/*
	 * Init for exchanging view with server.
	 * @param foreign server's view is passed  
	 */
	public static void exchangeViewWithServer(HashMap<String, ServerStatus> foreignServerView) {
		hisView = foreignServerView; 
		//Merge View here
		updateLocalView();
	}
	
}
