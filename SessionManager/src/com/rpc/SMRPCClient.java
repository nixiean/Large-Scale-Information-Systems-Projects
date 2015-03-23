package com.rpc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;


public class SMRPCClient {
	private static final int portPROJ1BRPC = 5300;
	private static final int packetSize = 512;
	private static final String FAILURE = "fail";
	private static final int SOCKET_TIMEOUT = 30*1000;
	
	SMRPCClient instance = null;;
	
	//create a singleton instance
	public SMRPCClient getInstance() {
		if(instance != null) {
			instance = new SMRPCClient();
		}
		return instance;
	}
	
	//session id, dest Addresss
	//1,126.12.13.14 , 23.13.23.45
	//1234#1#1,126.12.13.14
	public String sendForSessionRead(String sessionId, String destinationAddress) {
		//create RPC request packet
		//send to destination
		//wait for response and reply
		try {
			// get a datagram socket
		    DatagramSocket socket = new DatagramSocket();
		    socket.setSoTimeout(SOCKET_TIMEOUT);
		    
		    // send request
		    byte[] buf = new byte[packetSize];
		    InetAddress address = InetAddress.getByAddress(destinationAddress.getBytes());
		    
		    int callId = new Random().nextInt();
		    int operationCode = 1;
		    String payLoad = sessionId;
		    RPCRequest rpcRequest = new RPCRequest(callId, operationCode, payLoad);
		    
		    buf = RPCMsgUtil.serializeRPCRequest(rpcRequest).getBytes();
		    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, portPROJ1BRPC);
		    
		    socket.send(packet);
	
		    // get response
		    packet = new DatagramPacket(buf, buf.length);
		    socket.receive(packet);
	
		    // display response
		    String received = new String(packet.getData(), 0, packet.getLength());
		    //System.out.println("Response: " + received);
		    socket.close();
		    return received;
		} catch(Exception e) {
			e.printStackTrace();
			return FAILURE;
		} 
		
	}
	
	public String sendForSessionWrite(String sessionId, String versionNumber, String sessionData, String timeStamp, String destinationAddress) {
		//create RPC request packet
		//send to destination
		//wait for response and reply
		try {
			// get a datagram socket
		    DatagramSocket socket = new DatagramSocket();
		    socket.setSoTimeout(SOCKET_TIMEOUT);
		    
		    // send request
		    byte[] buf = new byte[packetSize];
		    InetAddress address = InetAddress.getByAddress(destinationAddress.getBytes());
		    
		    int callId = new Random().nextInt();
		    int operationCode = 2;
		    String payLoad = sessionId + "," + versionNumber + "," + sessionData + "," + timeStamp;
		    RPCRequest rpcRequest = new RPCRequest(callId, operationCode, payLoad);
		    
		    buf = RPCMsgUtil.serializeRPCRequest(rpcRequest).getBytes();
		    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, portPROJ1BRPC);
		    
		    socket.send(packet);
	
		    // get response
		    packet = new DatagramPacket(buf, buf.length);
		    socket.receive(packet);
	
		    // display response
		    String received = new String(packet.getData(), 0, packet.getLength());
		    //System.out.println("Response: " + received);
		    socket.close();
		    return received;
		} catch(Exception e) {
			e.printStackTrace();
			return FAILURE;
		} 
		
	}
	
}
