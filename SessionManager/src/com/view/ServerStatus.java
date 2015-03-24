package com.view;

import java.sql.Timestamp;
import java.util.Date;

public class ServerStatus {

	private ServerStatusCode statusCode;
	private String time;

	public ServerStatus(ServerStatusCode status) {
		this.statusCode = status;
		this.time = new Timestamp((new Date()).getTime()).toString();;
	}

	public ServerStatusCode getStatus() {
		return statusCode;
	}

	public void setStatus(ServerStatusCode status) {
		this.statusCode = status;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
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



