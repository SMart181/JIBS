package com.smart181.jibs.model;

public class FileSystemAccount {
	//private final static String ACCEPTED_URL_PATTERN = "^[a-z_A-Z]+://[a-z_A-Z]+:.*@[a-z_A-Z_0-9]+:\\d+(/.*)?$";
	private final static String ACCEPTED_URL_PATTERN = "^[a-z_A-Z]+://.+:.*@[a-z_A-Z_0-9\\.]+:\\d+(/.*)?$";
	
	private final String protocole;
	private final String user;
	private final String password;
	private final String host;
	private final int port;
	private final String path;
	
	public FileSystemAccount(String url) throws Exception{
		if(!url.matches(ACCEPTED_URL_PATTERN))
			throw new Exception("Invalid repository url !");
		
		// Analyse the url to extract connection infos
		int idx = url.indexOf("://");
		protocole = url.substring(0, idx);
		
		int idx2 = url.indexOf(":", idx+3);
		user = url.substring(idx+3, idx2);
		
		int idx3 = url.lastIndexOf("@");
		password = url.substring(idx2 +1, idx3);
		
		int idx4 = url.indexOf(":", idx3+1);
		host = url.substring(idx3+1, idx4);
		
		int idx5 = url.indexOf("/", idx4+1);
		if(idx5 != -1) {
			port = Integer.parseInt(url.substring(idx4+1, idx5));
			path = url.substring(idx5);
		}
		else {
			port = Integer.parseInt(url.substring(idx4+1, url.length()));
			path = null;
		}
		
	}

	public String getProtocole() {
		return protocole;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getPath() {
		return path;
	}
}
