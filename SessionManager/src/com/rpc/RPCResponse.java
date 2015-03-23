package com.rpc;

public class RPCResponse {
	private int callId;
	private String payload ;
	
	public RPCResponse(int callId, String payload) {
		this.callId = callId;
		this.payload = payload;
	}

	public int getCallId() {
		return callId;
	}

	public void setCallId(int callId) {
		this.callId = callId;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "[callId=" + callId + ", payload=" + payload + "]";
	}
	
}
