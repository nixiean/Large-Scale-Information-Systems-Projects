package com.rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;


public class SMRPCClient {
	private static final int portPROJ1BRPC = 5300;
	private static final int packetSize = 512;
	
	//comment
	public static void main(String[] args) {
		try {
			// get a datagram socket
		    DatagramSocket socket = new DatagramSocket();
	
		    // send request
		    byte[] buf = new byte[packetSize];
		    InetAddress address = InetAddress.getByName("localhost");
		    
		    int callId = new Random().nextInt();
		    int operationCode = 1;
		    String payLoad = "1_127.0.0.1";
		    RPCRequest rpcRequest = new RPCRequest(callId, operationCode, payLoad);
		    buf = RPCMsgUtil.serializeRPCRequest(rpcRequest).getBytes();
		    
		    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, portPROJ1BRPC);
		    socket.send(packet);
	
		    // get response
		    packet = new DatagramPacket(buf, buf.length);
		    socket.receive(packet);
	
		    // display response
		    String received = new String(packet.getData(), 0, packet.getLength());
		    System.out.println("Response: " + received);
		    socket.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
