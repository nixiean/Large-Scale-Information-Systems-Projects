package com.rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.servlet.EnterServlet;
import com.view.ServerStatus;
import com.view.ServerStatus.ServerStatusCode;
import com.view.ViewUtils;

public class SMRPCServer implements Runnable {
	private static final int portPROJ1BRPC = 5300;
	private static final int packetSize = 512;
	private static final String NOT_FOUND = "not found";
	private static final String SESSION_WRITE_SUCCESS = "session written successfully";
	private static final String EXCHANGE_VIEW_SUCCESS = "views exchanged succesfully";

	@Override
	public void run() {
		try {
			DatagramSocket rpcSocket = new DatagramSocket(portPROJ1BRPC);
			while (true) {
				byte[] inBuf = new byte[packetSize];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
				// Waits till the packet is received
				rpcSocket.receive(recvPkt);
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				// here inBuf contains the callID and operationCode
				String receivedRPCPktStr = new String(recvPkt.getData(), 0,
						recvPkt.getLength());
				RPCRequest rpcRequest = RPCMsgUtil
						.deserializeRPCRequest(receivedRPCPktStr);
				int operationCode = rpcRequest.getOpCode();
				byte[] outBuf = null;
				String payLoad = "";
				RPCResponse rpcResponse = null;
				switch (operationCode) {
				case 1:
					// rpcRequest will contain sessionID in payload
					payLoad = getPayloadForSessionRead(rpcRequest.getPayload());
					rpcResponse = new RPCResponse(rpcRequest.getCallId(),
							payLoad);
					break;
				case 2:
					// rpcRequest will contain session Data to be written
					payLoad = getPayloadForSessionWrite(rpcRequest.getPayload());
					rpcResponse = new RPCResponse(rpcRequest.getCallId(),
							payLoad);
					break;
				case 3:
					// rpcRequest will contain foreign server's view to be
					// exchanged
					payLoad = getPayloadForExchangeView(rpcRequest.getPayload());
					rpcResponse = new RPCResponse(rpcRequest.getCallId(),
							payLoad);
					break;
				}
				outBuf = RPCMsgUtil.serializeRPCResponse(rpcResponse)
						.getBytes();
				// here outBuf should contain the callID and results of the call
				DatagramPacket sendPkt = new DatagramPacket(outBuf,
						outBuf.length, returnAddr, returnPort);
				rpcSocket.send(sendPkt);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Check if the sessionID is present in the local session table
	 * 
	 * @return <versionNumber,sessionData> OR Not found
	 */
	private static String getPayloadForSessionRead(String incomingPayload) {
		String sessionId = incomingPayload;
		String sessionData = EnterServlet.sessionTable.get(sessionId);
		if (null != sessionData) {
			String[] tokens = sessionData.split("_");
			return tokens[1] + "," + sessionData;
		} else {
			return NOT_FOUND;
		}
	}

	/*
	 * Write the session data to local session table
	 * 
	 * @return Success message
	 */
	private static String getPayloadForSessionWrite(String incomingPayload) {
		String[] tokens = incomingPayload.split(",");
		String sessionId = tokens[0];
		// String versionNumber = tokens[1];
		String data = tokens[2];
		// String timeStamp = tokens[3];

		EnterServlet.sessionTable.put(sessionId, data);
		return SESSION_WRITE_SUCCESS;
	}

	/*
	 * Get and send all the server triplets and exchange the views
	 * 
	 * @return Server triplets built from local view
	 */
	private static String getPayloadForExchangeView(String incomingPayload) {

		//First build the view to be returned
		StringBuilder serverTriplets = new StringBuilder();
		for (Entry<String, ServerStatus> serverEntry : EnterServlet.myView
				.entrySet()) {
			String serverID = serverEntry.getKey();
			ServerStatus serverStatus = serverEntry.getValue();
			serverTriplets.append(serverID).append(",")
					.append(serverStatus.getStatus()).append(",")
					.append(serverStatus.getTime()).append(";");

		}

		//Extract the triplets and put it in hashtable
		if (incomingPayload != null) {
			// Prepare the view with which local view has to be exchanged
			HashMap<String, ServerStatus> hisView = new HashMap<String, ServerStatus>();
			String[] triplets = incomingPayload.split(";");
			for (String serverTriplet : triplets) {
				String serverDetails[] = serverTriplet.split(",");
				String serverID = serverDetails[0];
				ServerStatus status = new ServerStatus(
						ServerStatusCode.valueOf(serverDetails[1]));
				status.setTime(serverDetails[2]);
				hisView.put(serverID, status);
			}
			ViewUtils.updateLocalView(hisView);
		}

		return serverTriplets.toString();
	}

}
