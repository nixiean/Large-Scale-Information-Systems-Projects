package com.scheduler;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Random;

import com.servlet.EnterServlet;
import com.util.SessionUtil;
import com.view.ServerStatus;
import com.view.ServerStatus.ServerStatusCode;
import com.view.ViewUtils;

public class ViewExchangerThread implements Runnable {
	@Override
	public void run() {
		String gossipPartnerIp = pickGossipPartner();
		
		String localsvrId = SessionUtil.getIpAddress();
		
		//Update its own tuple
		EnterServlet.myView.put(localsvrId, new ServerStatus(ServerStatusCode.UP));
		
		if(gossipPartnerIp.equals(localsvrId)) {
			//Exchange with SimpleDB
			ViewUtils.exchangeViewWithSimpleDb();
		} else {
			//Make RPC and exchange with server
			ViewUtils.exchangeViewWithServer(gossipPartnerIp);
		}
	}

	private static String pickGossipPartner() {
		// Pick a partner from only the servers which are running
		ArrayList<String> serverIdList = new ArrayList<String>();

		// List only those servers which are running
		for (Entry<String, ServerStatus> serverEntry : EnterServlet.myView
				.entrySet()) {
			String serverID = serverEntry.getKey();
			ServerStatus serverStatus = serverEntry.getValue();
			if (serverStatus.getStatus().equals(ServerStatusCode.UP)) {
				serverIdList.add(serverID);
			}

		}
		Random rand = new Random();
		return serverIdList.get(rand.nextInt(serverIdList.size()));
	}
}
