package com.rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class SMRPCServer {
	public static void main(String[] args) {
		new Thread(new RPCRunnable()).start();
	}
	
}

class RPCRunnable implements Runnable {
	private static final int portPROJ1BRPC = 5300;
	private static final int packetSize = 512;
	
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
				switch( operationCode ) {
					   case 1:
						   payLoad = "1_dummyCookieData"; //read session id
						   rpcResponse = new RPCResponse(rpcRequest.getCallId(), payLoad);
						   break;
					   case 2:
						   payLoad = "session written successfully"; //write session id
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
}
