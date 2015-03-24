package com.rpc;

public class SMRPCServerDryRun {
	public static void main(String[] args) {
		(new Thread(new SMRPCServer())).start();
	}
}
