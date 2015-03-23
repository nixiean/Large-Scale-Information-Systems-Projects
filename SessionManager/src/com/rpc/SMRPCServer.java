package com.rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.servlet.EnterServlet;


public class SMRPCServer implements Runnable {
	private static final int portPROJ1BRPC = 5300;
	private static final int packetSize = 512;
	private static final String NOT_FOUND = "not found";
	private static final String SESSION_WRITE_SUCCESS = "session written successfully";
	
	@Override
	public void run() {
		try {
			DatagramSocket rpcSocket = new DatagramSocket(portPROJ1BRPC);
			while(true) {
				byte[] inBuf = new byte[packetSize];
			    DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
				rpcSocket.receive(recvPkt);
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				// here inBuf contains the callID and operationCode
				String receivedRPCPktStr = new String(recvPkt.getData(), 0, recvPkt.getLength());
				RPCRequest rpcRequest = RPCMsgUtil.deserializeRPCRequest(receivedRPCPktStr);
				int operationCode = rpcRequest.getOpCode();
				byte[] outBuf = null;
				String payLoad = "";
				RPCResponse rpcResponse = null;
				switch(operationCode) {
					   case 1:
						   payLoad = getPayloadForSessionRead(rpcRequest.getPayload()); //rpcRequest will have session id in payload
						   rpcResponse = new RPCResponse(rpcRequest.getCallId(), payLoad);
						   break;
					   case 2:
						   payLoad = getPayloadForSessionWrite(rpcRequest.getPayload()); //write session id
						   rpcResponse = new RPCResponse(rpcRequest.getCallId(), payLoad);
						   break;
					   case 3:
						   payLoad = "view merged successfully"; //exchange view
						   rpcResponse = new RPCResponse(rpcRequest.getCallId(), payLoad);
						   break;
				}
				outBuf = RPCMsgUtil.serializeRPCResponse(rpcResponse).getBytes();
				//here outBuf should contain the callID and results of the call
				DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length,returnAddr, returnPort);
			    rpcSocket.send(sendPkt);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getPayloadForSessionRead(String incomingPayload) {
	    String sessionId = incomingPayload;
	    String output = EnterServlet.sessionTable.get(sessionId);
	    if(null != output) {
	    	String[] tokens = output.split("_");
	    	return tokens[1] + "," + output;
	    } else {
	    	return NOT_FOUND;
	    }
	}
	
	private static String getPayloadForSessionWrite(String incomingPayload) {
		String[] tokens = incomingPayload.split("_");
		String sessionId = tokens[0];
		String versionNumber = tokens[1];
		String data = tokens[2];
		String timeStamp = tokens[3];
		
		if(EnterServlet.sessionTable.containsKey(sessionId)) {
			EnterServlet.sessionTable.put(sessionId, data);
			return SESSION_WRITE_SUCCESS;
		} else {
			return NOT_FOUND;
		}
		
	}
	
	//TODO
	private static String getPayloadForExchangeView() {
		return "";
	}
	
}


