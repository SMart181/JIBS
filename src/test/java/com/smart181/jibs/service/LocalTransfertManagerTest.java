package com.smart181.jibs.service;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smart181.jibs.service.LocalTransfertManager;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LocalTransfertManager.class)
public class LocalTransfertManagerTest {
	private String testRootPath = null;
	
	@Autowired
	LocalTransfertManager transfertManager;
	
	@Before
	public void init() throws IOException {
		testRootPath = new ClassPathResource("").getFile().getPath() + "/test";
		testRootPath = testRootPath.replace("\\", "/");
	}
	
	@Test
	public void testGetLastArchiveReportForExistingArchive() throws Exception {
		String lastReport = transfertManager.getLastArchiveReport(testRootPath + "/repository", "myArchive1");
		lastReport = lastReport.replace("\\", "/");
		
		File reportFile = new File(lastReport);
		
		assertNotNull(lastReport);
		assertTrue(lastReport.equals(testRootPath + "/repository/myArchive1/lastarchive.report"));
		assertTrue(reportFile.exists());
		assertTrue(FileUtils.readFileToString(reportFile, "UTF-8").equals("lastarchive Report Content !"));
	}

	@Test
	public void testGetLastArchiveReportForNotExistingArchive() throws Exception {
		String lastReport = transfertManager.getLastArchiveReport(testRootPath + "/repository", "unknownArchive");
		assertNull(lastReport);
	}
	
	@Test(expected = Exception.class)
	public void testGetLastArchiveReportWithNullFileSetId() throws Exception {
		transfertManager.getLastArchiveReport(testRootPath + "/repository", null);
	}
	
	@Test(expected = Exception.class)
	public void testGetLastArchiveReportWithEmptyFileSetId() throws Exception {
		transfertManager.getLastArchiveReport(testRootPath + "/repository", "");
	}
	
	@Test
	public void testGetLastArchiveReportWithNullRepository() throws Exception {
		String lastReport = transfertManager.getLastArchiveReport(null, "myArchive1");
		assertNull(lastReport);
	}
	
	@Test
	public void testGetLastArchiveReportWithEmptyRepository() throws Exception {
		String lastReport = transfertManager.getLastArchiveReport("", "myArchive1");
		assertNull(lastReport);
	}
	
	@Test
	public void testGetArchiveReportForExistingArchive() throws Exception {
		String lastReport = transfertManager.getArchiveReport(testRootPath + "/repository", "myArchive1", "001");
		lastReport = lastReport.replace("\\", "/");

		File reportFile = new File(lastReport);
		
		assertNotNull(lastReport);
		assertTrue(lastReport.endsWith("/myArchive1/001.report"));
		assertTrue(reportFile.exists());
		
		assertTrue(FileUtils.readFileToString(reportFile, "UTF-8").equals("001 Report Content !"));
	}
	
	@Test(expected = Exception.class)
	public void testGetArchiveReportWithNullInvalidArchiveId() throws Exception {
		transfertManager.getArchiveReport(testRootPath + "/repository", "myArchive1", null);
	}
	
	@Test(expected = Exception.class)
	public void testGetArchiveReportWithEmptyInvalidArchiveId() throws Exception {
		transfertManager.getArchiveReport(testRootPath + "/repository", "myArchive1", "");
	}
}
