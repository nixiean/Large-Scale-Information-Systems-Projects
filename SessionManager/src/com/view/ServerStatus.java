package com.view;

public class ServerStatus {

	private ServerStatusCode statusCode;
	private long time;

	public ServerStatus(ServerStatusCode status) {
		this.statusCode = status;
		this.time = System.currentTimeMillis();
	}

	public ServerStatusCode getStatus() {
		return statusCode;
	}

	public void setStatus(ServerStatusCode status) {
		this.statusCode = status;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	
	
	@Override
	public String toString() {
		return "ServerStatus [statusCode=" + statusCode + ", time=" + time
				+ "]";
	}



	public enum ServerStatusCode {
		UP,
		DOWN
	}
}



