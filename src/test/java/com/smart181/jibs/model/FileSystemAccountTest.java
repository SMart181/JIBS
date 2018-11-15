package com.smart181.jibs.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class FileSystemAccountTest {

	@Test
	public void testSftpUrl() throws Exception {
		FileSystemAccount accountInfos = new FileSystemAccount("sftp://myUser:myPwd@myHost:22/myRootFolder/myFolder");
		
		assertTrue(accountInfos.getProtocole().equals("sftp"));
		assertTrue(accountInfos.getUser().equals("myUser"));
		assertTrue(accountInfos.getPassword().equals("myPwd"));
		assertTrue(accountInfos.getHost().equals("myHost"));
		assertTrue(accountInfos.getPort() == 22);
		assertTrue(accountInfos.getPath().equals("/myRootFolder/myFolder"));
	}

	@Test
	public void testSftpUrl2() throws Exception {
		FileSystemAccount accountInfos = new FileSystemAccount("sftp://myUser:myPwd@myHost:22");
		
		assertTrue(accountInfos.getProtocole().equals("sftp"));
		assertTrue(accountInfos.getUser().equals("myUser"));
		assertTrue(accountInfos.getPassword().equals("myPwd"));
		assertTrue(accountInfos.getHost().equals("myHost"));
		assertTrue(accountInfos.getPort() == 22);
		assertNull(accountInfos.getPath());
	}
	
	@Test
	public void testSftpUrl3() throws Exception {
		FileSystemAccount accountInfos = new FileSystemAccount("sftp://myUser:myPwd@01.01.01.01:22000/root/1");
		
		assertTrue(accountInfos.getProtocole().equals("sftp"));
		assertTrue(accountInfos.getUser().equals("myUser"));
		assertTrue(accountInfos.getPassword().equals("myPwd"));
		assertTrue(accountInfos.getHost().equals("01.01.01.01"));
		assertTrue(accountInfos.getPort() == 22000);
		assertTrue(accountInfos.getPath().equals("/root/1"));
	}
	
	@Test(expected = Exception.class)
	public void testSftpUrlWithInvalidInput() throws Exception {
		FileSystemAccount accountInfos = new FileSystemAccount("someString");
	}
}
