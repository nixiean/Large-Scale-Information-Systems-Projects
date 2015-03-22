package com.scheduler;

import java.util.ArrayList;
import java.util.Random;
import com.servlet.EnterServlet;
import com.util.SessionUtil;
import com.view.ViewUtils;

public class ViewExchangerThread implements Runnable{
	@Override
	public void run() {
		String gossipPartnerIp = pickGossipPartner();
		if(gossipPartnerIp.equals(SessionUtil.getIpAddress())) {
			//exchange with simple db
			ViewUtils.exchangeViewWithSimpleDb();
		} else {
			//make rpc and exchange with server
		}
	}
	
	private static String pickGossipPartner() {
		ArrayList<String> serverIdList = new ArrayList<String>(EnterServlet.myView.keySet());
		Random rand = new Random();
		return serverIdList.get(rand.nextInt(serverIdList.size()));
	}
}
