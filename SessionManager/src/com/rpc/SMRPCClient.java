package com.rpc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import com.view.ServerStatus;
import com.view.ServerStatus.ServerStatusCode;
import com.view.ViewUtils;

public class SMRPCClient {
	private static final int portPROJ1BRPC = 5300;
	private static final int packetSize = 512;
	public static final String FAILURE = "fail";
	private static final int SOCKET_TIMEOUT = 3 * 1000;

	private static SMRPCClient instance = null;;

	// create a singleton instance
	public static SMRPCClient getInstance() {
		if (instance == null) {
			instance = new SMRPCClient();
		}
		return instance;
	}

	// session id, dest Addresss
	// 1,126.12.13.14 , 23.13.23.45
	// 1234#1#1,126.12.13.14
	public String sendForSessionRead(String sessionId, String destinationAddress) {
		// create RPC request packet
		// send to destination
		// wait for response and reply
		DatagramSocket socket = null;
		try {
			// get a datagram socket
			socket = new DatagramSocket();
			socket.setSoTimeout(SOCKET_TIMEOUT);

			// send request
			byte[] buf = new byte[packetSize];
			InetAddress address = InetAddress.getByName(destinationAddress);

			int callId = Math.abs(new Random().nextInt());
			int operationCode = 1;
			String payLoad = sessionId;
			RPCRequest rpcRequest = new RPCRequest(callId, operationCode,
					payLoad);
			System.out.println("Sending RPC Session Read to "+ destinationAddress + "with payLoad :" + RPCMsgUtil.serializeRPCRequest(rpcRequest));
			buf = RPCMsgUtil.serializeRPCRequest(rpcRequest).getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					address, portPROJ1BRPC);

			socket.send(packet);

			byte[] outBuf = new byte[packetSize];
			// get response
			packet = new DatagramPacket(outBuf, outBuf.length);

			int receivedCallId = 0;
			String received = FAILURE; //will be overwritten if we get a valid packet
			do {
				socket.receive(packet);
			    received = new String(packet.getData(), 0, packet.getLength());
			    receivedCallId = RPCMsgUtil.deserializeRPCResponse(received).getCallId();
			} while(receivedCallId != callId);
			
			socket.close();
			return received;
		} catch (Exception e) {
			if(socket!=null) socket.close();
			ViewUtils.updateSystemStatus(destinationAddress,ServerStatusCode.DOWN);
			e.printStackTrace();
			return FAILURE;
		}

	}

	public String sendForSessionWrite(String sessionId, String versionNumber,
			String sessionData, String timeStamp, String destinationAddress) {
		// create RPC request packet
		// send to destination
		// wait for response and reply
		DatagramSocket socket = null;
		try {
			// get a datagram socket
			socket = new DatagramSocket();
			socket.setSoTimeout(SOCKET_TIMEOUT);

			// send request
			byte[] buf = new byte[packetSize];
			InetAddress address = InetAddress.getByName(destinationAddress);

			int callId = Math.abs(new Random().nextInt());
			int operationCode = 2;
			String payLoad = sessionId + ";" + versionNumber + ";"
					+ sessionData + ";" + timeStamp;
			RPCRequest rpcRequest = new RPCRequest(callId, operationCode,
					payLoad);

			System.out.println("Sending RPC Session Write to "+ destinationAddress + "with payLoad :" + RPCMsgUtil.serializeRPCRequest(rpcRequest));
			buf = RPCMsgUtil.serializeRPCRequest(rpcRequest).getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					address, portPROJ1BRPC);

			socket.send(packet);

			byte[] outBuf = new byte[packetSize];
			// get response
			packet = new DatagramPacket(outBuf, outBuf.length);

			int receivedCallId = 0;
			String received = FAILURE; //will be overwritten if we get a valid packet
			do {
				socket.receive(packet);
			    received = new String(packet.getData(), 0, packet.getLength());
			    receivedCallId = RPCMsgUtil.deserializeRPCResponse(received).getCallId();
			} while(receivedCallId != callId);
			
			socket.close();
			return received;
		} catch (Exception e) {
			if(socket!=null) socket.close();
			ViewUtils.updateSystemStatus(destinationAddress,ServerStatusCode.DOWN);
			e.printStackTrace();
			return FAILURE;
		}

	}

	public String sendForExchangeViews(HashMap<String, ServerStatus> myView,
			String destinationAddress) {
		// create RPC request packet
		// send to destination
		// wait for response and reply
		DatagramSocket socket = null;
		try {
			// get a datagram socket
			socket = new DatagramSocket();
			socket.setSoTimeout(SOCKET_TIMEOUT);

			// send request
			byte[] buf = new byte[packetSize];
			InetAddress address = InetAddress.getByName(destinationAddress);

			int callId = Math.abs(new Random().nextInt());
			int operationCode = 3;
			String payLoad = buildPayLoadForExchangeView(myView);
			RPCRequest rpcRequest = new RPCRequest(callId, operationCode,
					payLoad);

			System.out.println("Sending RPC Exchange View to "+ destinationAddress + "with payLoad :" + RPCMsgUtil.serializeRPCRequest(rpcRequest));
			
			buf = RPCMsgUtil.serializeRPCRequest(rpcRequest).getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					address, portPROJ1BRPC);

			socket.send(packet);

			byte[] outBuf = new byte[packetSize];
			// get response
			packet = new DatagramPacket(outBuf, outBuf.length);

			int receivedCallId = 0;
			String received = FAILURE; //will be overwritten if we get a valid packet
			do {
				socket.receive(packet);
			    received = new String(packet.getData(), 0, packet.getLength());
			    receivedCallId = RPCMsgUtil.deserializeRPCResponse(received).getCallId();
			} while(receivedCallId != callId);
			
			socket.close();
			return received;
		} catch (Exception e) {
			if(socket!=null) socket.close();
			ViewUtils.updateSystemStatus(destinationAddress,ServerStatusCode.DOWN);
			e.printStackTrace();
			return FAILURE;
		}

	}

	/*
	 * Build the payload from the local view
	 */
	private static String buildPayLoadForExchangeView(
			HashMap<String, ServerStatus> myView) {
		StringBuilder payLoad = new StringBuilder();

		for (Entry<String, ServerStatus> serverEntry : myView.entrySet()) {
			String serverID = serverEntry.getKey();
			ServerStatus serverStatus = serverEntry.getValue();

			payLoad.append(serverID).append(",")
					.append(serverStatus.getStatus()).append(",")
					.append(serverStatus.getTime()).append(";");
		}

		return payLoad.toString();
	}

}
