package com.rpc;

public class SMRPCClientDryRun {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SMRPCClient rpcClient = SMRPCClient.getInstance();
		String rpcResponse = rpcClient.sendForSessionWrite("blah", "1", "blah", "blah", "128.84.216.62");
		System.out.println("Got Response:"+ rpcResponse);
	}

}
