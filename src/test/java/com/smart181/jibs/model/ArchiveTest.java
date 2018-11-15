package com.smart181.jibs.model;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smart181.jibs.TestFolderManager;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ArchiveTest {
	@Autowired
	private TestFolderManager folderManager;
	
	@Test
	public void testFromReport() throws IOException {
		Archive newArchive = Archive.fromReport(folderManager.getTestInputPath() + "/archive/dataReport_t1.txt");
		
		assertTrue(newArchive.getFileSet().getId().equals("id-archive"));
		assertTrue(newArchive.getArchiveId().equals("415dcf54-738f-40da-a50f-4f0b155e5cbf"));
		assertTrue(newArchive.getPreviousArchiveId().equals("3bebb9b9-0b0a-4af5-91b6-dcafbc62a082"));
		
		System.out.println("InheritedContent size : " + newArchive.getInheritedContent().size());
		System.out.println("AddedContent size : " + newArchive.getAddedContent().size());
		System.out.println("UpdatedContent size : " + newArchive.getUpdatedContent().size());
		System.out.println("DeletedContent size : " + newArchive.getDeletedContent().size());
		System.out.println("ArchiveFiles size : " + newArchive.getArchiveFiles().size());
		
		assertTrue(newArchive.getInheritedContent().size() == 71);
		assertTrue(newArchive.getAddedContent().size() == 47);
		assertTrue(newArchive.getUpdatedContent().size() == 196);
		assertTrue(newArchive.getDeletedContent().size() == 19);
		assertTrue(newArchive.getArchiveFiles().size() == 3);
	}

	@Test(expected = FileNotFoundException.class)
	public void testFromReportOnUnavailableReport() throws IOException {
		Archive.fromReport(folderManager.getTestInputPath() + "/archive/unknownDataReport.txt");
	}
	
	// TODO : write
}
