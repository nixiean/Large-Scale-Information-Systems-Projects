package com.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.view.ServerStatus;
import com.view.ServerStatus.ServerStatusCode;

public class AWSSimpleDbUtil {
	
	private static AmazonSimpleDBClient amazonSimpleDBClient = null;
	private static String awsSimpleDBDelimiter = ",";
	private static String domain = "servers";
	private static String attributeName = "serverStatus";

	
	public static void initSimpleDbInstance() {
		if(amazonSimpleDBClient == null) {
			BasicAWSCredentials credentials = new BasicAWSCredentials(getAccessKeyFromEnv(), getSecretKeyFromEnv());
			amazonSimpleDBClient = new AmazonSimpleDBClient(credentials);
		} 
	}
	
	private static String getAccessKeyFromEnv() {
		return System.getenv("AWS_ACCESS_KEY");
	}
	
	private static String getSecretKeyFromEnv() {
		return System.getenv("AWS_SECRET_KEY");
	}
	
	public static void awsSimpleDBPut(HashMap<String, ServerStatus> myView) {
		List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
		// Iterate through the items of local view
		for (Entry<String, ServerStatus> serverEntry : myView.entrySet()) {
			String serverID = serverEntry.getKey();
			String attributeValue = serverEntry.getValue().getStatus() + ","
					+ serverEntry.getValue().getTime();

			items.add(new ReplaceableItem().withName(serverID).withAttributes(
					new ReplaceableAttribute().withName(attributeName)
							.withValue(attributeValue).withReplace(true)));
		}
		amazonSimpleDBClient.batchPutAttributes(new BatchPutAttributesRequest(domain, items));
	}
	
	public static HashMap<String, ServerStatus> awsSimpleDBGet() {
		HashMap<String, ServerStatus> hisView = new HashMap<String, ServerStatus>();
		String selectQuery = "select * from " + domain;
		SelectRequest selectRequest = new SelectRequest(selectQuery);
		for (Item item : amazonSimpleDBClient.select(selectRequest).getItems()) {
			String serverStatus[] = item.getAttributes().get(0).getValue()
					.split(awsSimpleDBDelimiter);
			ServerStatus status = new ServerStatus(ServerStatusCode.valueOf(serverStatus[0]));
			status.setTime(Long.parseLong(serverStatus[1]));
			hisView.put(item.getName(), status);
		}
		return hisView;
	}
	
}
