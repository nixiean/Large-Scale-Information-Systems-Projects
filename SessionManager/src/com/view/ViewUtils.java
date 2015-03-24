package com.view;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map.Entry;

import com.rpc.SMRPCClient;
import com.servlet.EnterServlet;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.util.AWSSimpleDbUtil;
import com.view.ServerStatus.ServerStatusCode;

public class ViewUtils {
	// exchange with simpledb
	private static HashMap<String, ServerStatus> myView = EnterServlet.myView;

	/*
	 * Init for exchanging view with simpleDB
	 */
	public static void exchangeViewWithSimpleDb() {
		AWSSimpleDbUtil.initSimpleDbInstance();
		HashMap<String, ServerStatus> hisView = AWSSimpleDbUtil
				.awsSimpleDBGet();
		// merge view here
		updateLocalView(hisView);
		AWSSimpleDbUtil.awsSimpleDBPut(myView);
	}

	/*
	 * Exchange the views between local and foreign view
	 */
	public static void updateLocalView(HashMap<String, ServerStatus> hisView) {
		for (Entry<String, ServerStatus> serverEntry : hisView.entrySet()) {
			String serverID = serverEntry.getKey();
			ServerStatus serverStatus = serverEntry.getValue();
			// If the serverID is not present in the local view, then add it to
			// the local view
			if (!myView.containsKey(serverID)) {
				myView.put(serverID, serverStatus);
			} else {
				// Update the server status according to the latest timestamp
				//add greater time logic here
				if (Timestamp.valueOf(serverStatus.getTime()).after(Timestamp.valueOf(myView.get(serverID).getTime()))) {
					myView.put(serverID, serverStatus);
				}
			}
		}
		hisView.clear();
	}

	/*
	 * Init for exchanging view with server.
	 * 
	 * @param foreign server's view is passed
	 */
	public static void exchangeViewWithServer(String gossipPartnerIP) {

		// Make the RPC calls with the server and exchange the view
		SMRPCClient rpcClient = SMRPCClient.getInstance();
		String receivedMessage = rpcClient.sendForExchangeViews(myView,
				gossipPartnerIP);
		
		//Get the server details from the foreign view
		if (receivedMessage != SMRPCClient.FAILURE) {
			HashMap<String, ServerStatus> hisView = new HashMap<String, ServerStatus>();
			String []serverTriplets = receivedMessage.split("#")[2].split(";");
			
			for(String serverTriplet : serverTriplets) {
				String serverDetails[] = serverTriplet.split(",");
				String serverID = serverDetails[0];
				ServerStatus status = new ServerStatus(
						ServerStatusCode.valueOf(serverDetails[1]));
				status.setTime(serverDetails[2]);
				hisView.put(serverID, status);
			}
			
			// Merge View here
			updateLocalView(hisView);
		}
	}
	
	/*
	 * Update the status of the server in local view
	 */
	public static void updateSystemStatus(String serverId, ServerStatusCode statusCode) {
		ServerStatus status = new ServerStatus(statusCode);
		myView.put(serverId,status);
		
	}

}
