package com.smart181.jibs.model;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smart181.jibs.TestFolderManager;
import com.smart181.jibs.model.FileSet;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FileSetTest {
	@Autowired
	private TestFolderManager folderManager;
	
	@Test
	public void testReadInFolder() throws Exception {
		String inputPath = folderManager.getTestInputPath() + "/fileSet1";
		FileSet fileSet = FileSet.readInFolder(inputPath);

		assertEquals(fileSet.getId(), "fsid");
		assertEquals(fileSet.getName(), "fsname");
	}
	
	@Test(expected = Exception.class)
	public void testReadInFolderWithNullFolderPath() throws Exception {
		FileSet fileSet = FileSet.readInFolder(null);
	}
	
	@Test(expected = Exception.class)
	public void testReadInFolderWithEmptyFolderPath() throws Exception {
		FileSet fileSet = FileSet.readInFolder("");
	}
	
	@Test(expected = Exception.class)
	public void testReadInFolderWithInvalidFolderPath() throws Exception {
		FileSet fileSet = FileSet.readInFolder("toto");
	}
	
	@Test
	public void testWriteInFolder() throws Exception {
		// Create the output folder
		String outputPath = folderManager.getTestOutputPath() + "/fileSet1";
		folderManager.deleteFolder(outputPath);
		folderManager.createFolder(outputPath);
		
		// Add a new fileSet
		FileSet fileSet = new FileSet();
		fileSet.setId("fsid");
		fileSet.setName("fsname");
		
		// Write to folder
		fileSet.writeInFolder(outputPath);
		
		File outputFile = new File(folderManager.getTestOutputPath() + "/fileSet1/.fileset");
		assertTrue(outputFile.exists());
		String outputContent = FileUtils.readFileToString(outputFile, "UTF-8");
		assertNotEquals(outputContent.indexOf("FileSetId : fsid"), -1);
		assertNotEquals(outputContent.indexOf("FileSetName : fsname"), -1);
	}
	
	@Test(expected = Exception.class)
	public void testWriteInFolderOnNullFolder() throws Exception {
		// Add a new fileSet
		FileSet fileSet = new FileSet();
		fileSet.setId("fsid");
		fileSet.setName("fsname");
		
		// Write to folder
		fileSet.writeInFolder(null);
	}
	
	@Test(expected = Exception.class)
	public void testWriteInFolderOnEmptyFolder() throws Exception {
		// Add a new fileSet
		FileSet fileSet = new FileSet();
		fileSet.setId("fsid");
		fileSet.setName("fsname");
		
		// Write to folder
		fileSet.writeInFolder("");
	}
	
	@Test(expected = Exception.class)
	public void testWriteInFolderOnInvalidFolder() throws Exception {
		// Add a new fileSet
		FileSet fileSet = new FileSet();
		fileSet.setId("fsid");
		fileSet.setName("fsname");
		
		// Write to folder
		fileSet.writeInFolder("toto");
	}
}
