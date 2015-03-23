package com.rpc;

public class RPCMsgUtil {
	
	public static String serializeRPCRequest(RPCRequest rpcRequest) {
		return rpcRequest.getCallId() + "#" + rpcRequest.getOpCode() + "#" + rpcRequest.getPayload();
	}
	
	public static RPCRequest deserializeRPCRequest(String rpcRequestStr) {
		String[] tokens = rpcRequestStr.split("#");
		return new RPCRequest(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), tokens[2]);
	}
	
	public static String serializeRPCResponse(RPCResponse rpcResponse) {
		return rpcResponse.getCallId() + "#" +  rpcResponse.getPayload();
	}
	
	public static RPCResponse deserializeRPCResponse(String rpcResponseStr) {
		String[] tokens = rpcResponseStr.split("#");
		return new RPCResponse(Integer.parseInt(tokens[0]),tokens[1]);
	}
	
}
