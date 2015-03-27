package com.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Random;

import com.servlet.EnterServlet;
import com.util.SessionUtil;
import com.view.ServerStatus;
import com.view.ServerStatus.ServerStatusCode;
import com.view.ViewUtils;

public class ViewExchangerThread implements Runnable {
	private long timeout = 60*1000;
	
	public ViewExchangerThread(long timeout) {
		this.timeout = timeout;
	}
	
	@Override
	public void run() {
		while(true) {
			System.out.println("View Exchange Thread Started at:" + new Date().toString());		
			String localsvrId = SessionUtil.getIpAddress();		
			//Update its own tuple
			EnterServlet.myView.put(localsvrId, new ServerStatus(ServerStatusCode.UP));
			
			String gossipPartnerIp = pickGossipPartner();
			EnterServlet.myView.put(SessionUtil.getIpAddress(), new ServerStatus(ServerStatusCode.UP));
			if(gossipPartnerIp.equals(localsvrId)) {
				//Exchange with SimpleDB
				System.out.println(localsvrId + " is initiating gossip with Simple DB at: "+ new Date().toString());
				ViewUtils.exchangeViewWithSimpleDb();
			} else {
				//Make RPC and exchange with server
				System.out.println(localsvrId + " is initiating gossip with "+ gossipPartnerIp +" at: "+ new Date().toString());
				ViewUtils.exchangeViewWithServer(gossipPartnerIp);
			}
			EnterServlet.myView.put(SessionUtil.getIpAddress(), new ServerStatus(ServerStatusCode.UP));
			try {
				System.out.println("View Exchange Thread Sleeping at:" + new Date().toString());	
				Thread.sleep(timeout); 
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
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
