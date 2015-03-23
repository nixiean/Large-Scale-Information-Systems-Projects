package com.rpc;

public class RPCRequest {
	private int callId ;
	private int opCode;
	private String payload;
	
	public RPCRequest(int callId, int opCode, String payload) {
		this.callId = callId;
		this.opCode = opCode;
		this.payload = payload;
	}
	
	public int getCallId() {
		return callId;
	}

	public void setCallId(int callId) {
		this.callId = callId;
	}

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
	
	@Override
	public String toString() {
		return "[" + "callId:" + callId + " ," + "opCode:" + opCode + " ," + "payload:" + payload + "]";
	}
	
}
