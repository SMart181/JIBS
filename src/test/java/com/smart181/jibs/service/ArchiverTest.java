package com.smart181.jibs.service;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smart181.jibs.TestFolderManager;
import com.smart181.jibs.model.FileSet;
import com.smart181.jibs.service.Archiver;

import net.lingala.zip4j.exception.ZipException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest()
public class ArchiverTest {
	@Value("${jibs.tempFolder:${java.io.tmpdir}/jibs}")
	private String tempFolder;
	
	@Autowired
	private Archiver archiver;
	
	@Autowired
	private TestFolderManager folderManager;
	
	@Before
	public void cleanTestFolder() throws IOException {
		folderManager.resetOrCreateFolder(folderManager.getTestOutputPathForTest("archiver"));
	}
	
	@Test
	public void testBuildArchiveOnExistingFileSet() throws IOException, ZipException {
		// Prepare fileSet
		FileSet fileSet = new FileSet();
		fileSet.setId("fileSetId");
		fileSet.setName("data-0");
		
		// Configure archiver
		archiver.setMaxSize(0);
		archiver.setPassword(null);
		List<String> listOfArchiveFilePaths = archiver.buildArchive(folderManager.getTestInputPath() + "/data-0", folderManager.getTestOutputPathForTest("archiver") + "/archive.zip" );
		
		System.out.println("listOfArchiveFilePaths.size() : " + listOfArchiveFilePaths.size());
		System.out.println("listOfArchiveFilePaths.get(0) : " + listOfArchiveFilePaths.get(0));
		assertTrue(listOfArchiveFilePaths.size() == 1);
		assertTrue(listOfArchiveFilePaths.get(0).endsWith("archive.zip"));
		
		// Check output backup existence
		File outputArchiveFile = new File(listOfArchiveFilePaths.get(0));
		assertTrue(outputArchiveFile.exists());
		
		// Extract it contents
		String outputPath2 = folderManager.getTestOutputPathForTest("archiver") + "/output-testBuildArchiveOnExistingFileSet";
		folderManager.resetOrCreateFolder(outputPath2);
		archiver.extractFiles(outputArchiveFile.getPath(), outputPath2);
		
		// Check archive content
		File extractedContentFile = new File(outputPath2 + "/folder_0");
		assertTrue(extractedContentFile.exists());
	}

	@Test
	public void testBuildArchiveOnExistingFileSetWithPassword() throws IOException, ZipException {
		// Prepare fileSet
		FileSet fileSet = new FileSet();
		fileSet.setId("fileSetId");
		fileSet.setName("data-0");
		
		// Configure archiver
		archiver.setMaxSize(0);
		archiver.setPassword("password");	
		List<String> listOfArchiveFilePaths = archiver.buildArchive(folderManager.getTestInputPath() + "/data-0", folderManager.getTestOutputPathForTest("archiver") + "/pwd_archive.zip" );
		
		assertTrue(listOfArchiveFilePaths.size() == 1);
		assertTrue(listOfArchiveFilePaths.get(0).endsWith("pwd_archive.zip"));
		
		// Check output backup existence
		File outputArchiveFile = new File(listOfArchiveFilePaths.get(0));
		assertTrue(outputArchiveFile.exists());
		
		// Extract it contents
		String outputPath2 = folderManager.getTestOutputPathForTest("archiver") + "/output-testBuildArchiveOnExistingFileSetWithPassword";
		folderManager.resetOrCreateFolder(outputPath2);
		archiver.extractFiles(outputArchiveFile.getPath(), outputPath2);
		
		// Check archive content
		File extractedContentFile = new File(outputPath2 + "/folder_0");
		assertTrue(extractedContentFile.exists());
	}
	
	@Test
	public void testBuildArchiveOnExistingFileSetWithMaxArchiveSize() throws IOException, ZipException {
		// Prepare fileSet
		FileSet fileSet = new FileSet();
		fileSet.setId("fileSetId");
		fileSet.setName("data-0");
		
		// Configure archiver	
		archiver.setMaxSize(100000);
		archiver.setPassword(null);
		List<String> listOfArchiveFilePaths = archiver.buildArchive(folderManager.getTestInputPath() + "/data-0", folderManager.getTestOutputPathForTest("archiver") + "/multi_archive.zip" );
		
		assertTrue(listOfArchiveFilePaths.size() == 3);
		assertTrue(listOfArchiveFilePaths.get(0).endsWith("multi_archive.z01"));
		assertTrue(listOfArchiveFilePaths.get(1).endsWith("multi_archive.z02"));
		assertTrue(listOfArchiveFilePaths.get(2).endsWith("multi_archive.zip"));
		
		// Check output backup existence (3 files should be generated)
		File outputArchiveFile = new File(listOfArchiveFilePaths.get(0));
		assertTrue(outputArchiveFile.exists());
		
		File outputArchiveFile1 = new File(listOfArchiveFilePaths.get(1));
		assertTrue(outputArchiveFile1.exists());
		
		File outputArchiveFile2 = new File(listOfArchiveFilePaths.get(2));
		assertTrue(outputArchiveFile2.exists());
		
		// Extract it contents
		String outputPath2 = folderManager.getTestOutputPathForTest("archiver") + "/output-testBuildArchiveOnExistingFileSet";
		folderManager.resetOrCreateFolder(outputPath2);
		archiver.extractFiles(listOfArchiveFilePaths.get(2), outputPath2);
		
		// Check archive content
		File extractedContentFile = new File(outputPath2 + "/folder_0");
		assertTrue(extractedContentFile.exists());
	}
	
	// TODO : NoFileToBackup
	
	@Test
	public void testExtractOnExistingZip() throws ZipException, IOException {
		// Extract the content of the zip
		archiver.extractFiles(folderManager.getTestinputPathForTest("archiver") + "/data-0.zip", folderManager.getTestOutputPathForTest("archiver"));
	}

	@Test(expected = ZipException.class)
	public void testExtractOnNonExistingZip() throws ZipException, IOException {
		// Extract the content of the zip
		archiver.extractFiles(folderManager.getTestinputPathForTest("archiver") + "/no-data-0.zip", folderManager.getTestOutputPathForTest("archiver"));
	}
	
	@Test(expected = NullPointerException.class)
	public void testExtractWithNullInputZipPath() throws ZipException, IOException {
		// Extract the content of the zip
		archiver.extractFiles(null, folderManager.getTestOutputPathForTest("archiver"));
	}
	
	@Test(expected = ZipException.class)
	public void testExtractWithEmptyInputZipPath() throws ZipException, IOException {
		// Extract the content of the zip
		archiver.extractFiles("", folderManager.getTestOutputPathForTest("archiver"));
	}
	
	@Test(expected = ZipException.class)
	public void testExtractWithNullOutputFolderPath() throws ZipException, IOException {
		// Extract the content of the zip
		archiver.extractFiles(folderManager.getTestinputPathForTest("archiver") + "/data-0.zip", null);
	}

	@Test(expected = ZipException.class)
	public void testExtractWithEmptyOutputFolderPath() throws ZipException, IOException {
		// Extract the content of the zip
		archiver.extractFiles(folderManager.getTestinputPathForTest("archiver") + "/data-0.zip", "");
	}	
}
