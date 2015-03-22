package com.view;

import java.util.HashMap;
import java.util.Map.Entry;

import com.servlet.EnterServlet;
import com.util.AWSSimpleDbUtil;

public class ViewUtils {
	//exchange with simpledb
	private static HashMap<String, ServerStatus> hisView ;
	private static HashMap<String, ServerStatus> myView = EnterServlet.myView;
	
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
	
	//exchange with server
	
}
