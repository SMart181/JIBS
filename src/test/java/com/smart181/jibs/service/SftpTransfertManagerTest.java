package com.smart181.jibs.service;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smart181.jibs.TestFolderManager;
import com.smart181.jibs.service.impl.SftpTransfertManager;

/**
 * To run this test, it is necessary to copy the content of the folder /src/test/resources/test/repository 
 * and update the testRootPath with a valid sftp url (pattern : sftp://user:pwd@host:port/path). 
 *  
 * @author S. Martin
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SftpTransfertManagerTest {
	private final String testRootPath = "";
	
	@Autowired
	SftpTransfertManager transfertManager;
	
	@Autowired
	private TestFolderManager folderManager;
	
	@Test
	public void testGetLastArchiveReportForExistingArchive() throws Exception {
		String lastReport = transfertManager.getLastArchiveReport(testRootPath, "myArchive1");
		lastReport = lastReport.replace("\\", "/");
		System.out.println("lastReport : " + lastReport);
		
		File reportFile = new File(lastReport);
		
		assertNotNull(lastReport);
		assertTrue(lastReport.endsWith("/myArchive1/lastarchive.report"));
		assertTrue(reportFile.exists());
 		assertTrue(FileUtils.readFileToString(reportFile, "UTF-8").equals("lastarchive Report Content !"));
	}

	@Test
	public void testGetLastArchiveReportForNotExistingArchive() throws Exception {
		String lastReport = transfertManager.getLastArchiveReport(testRootPath, "unknownArchive");
		assertNull(lastReport);
	}
	
	@Test(expected = Exception.class)
	public void testGetLastArchiveReportWithNullFileSetId() throws Exception {
		transfertManager.getLastArchiveReport(testRootPath, null);
	}
	
	@Test(expected = Exception.class)
	public void testGetLastArchiveReportWithEmptyFileSetId() throws Exception {
		transfertManager.getLastArchiveReport(testRootPath, "");
	}
	
	@Test (expected = Exception.class)
	public void testGetLastArchiveReportWithNullRepository() throws Exception {
		transfertManager.getLastArchiveReport(null, "myArchive1");
	}
	
	@Test (expected = Exception.class)
	public void testGetLastArchiveReportWithEmptyRepository() throws Exception {
		transfertManager.getLastArchiveReport("", "myArchive1");
	}
	
	@Test
	public void testGetArchiveReportForExistingArchive() throws Exception {
		String lastReport = transfertManager.getArchiveReport(testRootPath, "myArchive1", "001");
		lastReport = lastReport.replace("\\", "/");

		File reportFile = new File(lastReport);
		
		assertNotNull(lastReport);
		assertTrue(lastReport.endsWith("/myArchive1/001.report"));
		assertTrue(reportFile.exists());
		
		assertTrue(FileUtils.readFileToString(reportFile, "UTF-8").equals("001 Report Content !"));
	}
	
	@Test(expected = Exception.class)
	public void testGetArchiveReportWithNullArchiveId() throws Exception {
		transfertManager.getArchiveReport(testRootPath, "myArchive1", null);
	}
	
	@Test(expected = Exception.class)
	public void testGetArchiveReportWithEmptyArchiveId() throws Exception {
		transfertManager.getArchiveReport(testRootPath, "myArchive1", "");
	}
	
	@Test
	public void testGetArchiveForExistingArchive() throws Exception {
		List<String> listOfArchiveFiles = transfertManager.getArchiveFiles(testRootPath, "e62b9ade-cee7-47cf-99d9-8264d92c2e29", "fffc01bb-6d75-4262-b60a-1db835bb8ee8.zip");
		assertEquals(1, listOfArchiveFiles.size());
		
		String archivePath = listOfArchiveFiles.get(0).replace("\\", "/");
		
		File archiveFile = new File(archivePath);
		
		assertTrue(archivePath.endsWith("/e62b9ade-cee7-47cf-99d9-8264d92c2e29/fffc01bb-6d75-4262-b60a-1db835bb8ee8.zip"));
		assertTrue(archiveFile.exists());
	}
	
	@Test(expected = Exception.class)
	public void testGetArchiveWithInvalidFileSetId() throws Exception {
		transfertManager.getArchiveFiles(testRootPath, "aaaa", "fffc01bb-6d75-4262-b60a-1db835bb8ee8");
	}
	
	@Test(expected = Exception.class)
	public void testGetArchiveWithNullFileSetId() throws Exception {
		transfertManager.getArchiveFiles(testRootPath, null, "fffc01bb-6d75-4262-b60a-1db835bb8ee8");
	}
	
	@Test(expected = Exception.class)
	public void testGetArchivetWithEmptyFileSetId() throws Exception {
		transfertManager.getArchiveFiles(testRootPath, "", "fffc01bb-6d75-4262-b60a-1db835bb8ee8");
	}	
	
	@Test(expected = Exception.class)
	public void testGetArchiveWithInvalidArchiveId() throws Exception {
		transfertManager.getArchiveFiles(testRootPath, "aaaa", "aaaa");
	}
	
	@Test(expected = Exception.class)
	public void testGetArchiveWithNullArchiveId() throws Exception {
		transfertManager.getArchiveFiles(testRootPath, "e62b9ade-cee7-47cf-99d9-8264d92c2e29", null);
	}
	
	@Test(expected = Exception.class)
	public void testGetArchivetWithEmptyArchiveId() throws Exception {
		transfertManager.getArchiveFiles(testRootPath, "e62b9ade-cee7-47cf-99d9-8264d92c2e29", "");
	}	
	
	@Test
	public void testSendArchive() throws Exception {
		transfertManager.sendArchiveFiles(testRootPath,
									 	  "fileSet2",
									 	  folderManager.getTestInputPath() + "/fileSet2/archiveId1.zip",
									 	  folderManager.getTestInputPath() + "/fileSet2/archiveId1.report",
									 	  folderManager.getTestInputPath() + "/fileSet2/archiveId2.zip",
									 	  folderManager.getTestInputPath() + "/fileSet2/archiveId2.report");
	}
	
	@Test(expected = Exception.class)
	public void testSendArchivetWithNullRepository() throws Exception {
		transfertManager.sendArchiveFiles(null, "e62b9ade-cee7-47cf-99d9-8264d92c2e29", null);
	}
	
	@Test(expected = Exception.class)
	public void testSendArchivetWithEmptyRepository() throws Exception {
		transfertManager.sendArchiveFiles("", "e62b9ade-cee7-47cf-99d9-8264d92c2e29", null);
	}
	
	@Test(expected = Exception.class)
	public void testSendArchivetWithNullFileSetId() throws Exception {
		transfertManager.sendArchiveFiles(testRootPath, null, null);
	}
	
	@Test(expected = Exception.class)
	public void testSendArchivetWithEmptyFileSetId() throws Exception {
		transfertManager.sendArchiveFiles(testRootPath, "", null);
	}
	
}
