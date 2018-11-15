package com.smart181.jibs.service;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smart181.jibs.TestFolderManager;
import com.smart181.jibs.model.Archive;
import com.smart181.jibs.model.FileSet;
import com.smart181.jibs.service.ArchivePlanner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ArchivePlannerTests {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ArchivePlanner archivePlanner;
	
	@Autowired
	private TestFolderManager folderManager;
	
	@Value("${jibs.tempFolder:${java.io.tmpdir}/jibs}")
	private String tempFolder;

	@Test
	public void testBuildFolderMap1() throws IOException {
		String testName = "bfm1";
		
		FileSet fileSet = new FileSet();
		fileSet.setId("myFileSetId");
		fileSet.setName("myFileSet");
		
		archivePlanner.buildFolderMap(fileSet, "123456789", folderManager.getTestInputPath() + "/data-0", folderManager.getTestOutputPathForTest(testName));
		
        //Path source = Paths.get(testInputPath + "/data-0");
        //Files.walk(source).filter(Files::isRegularFile).forEach(System.out::println);
		
		//assertTrue(compareFiles(folderManager.getTestOutputPathForTest(testName) + "/123456789.foldermap", folderManager.getTestResultPath() + "/bfm1/123456789.foldermap"));
		assertTrue(compareJibsFiles(folderManager.getTestOutputPathForTest(testName) + "/123456789.foldermap", folderManager.getTestResultPath() + "/bfm1/123456789.foldermap"));
	}
	
	/*@Test
	public void testBuildFolderMap2() throws IOException {
		String testName = "bfm2";
		String testFolder = testOutputPath + "/" + testName;
		String folderMap = testFolder + "/dataMap_t0.txt";
		
		// Clear previous test working folder if it exists
		File testFolderFile = new File(testFolder);
		if(testFolderFile.exists())
			FileUtils.forceDelete(testFolderFile);
		
		// Create the test working folder
		testFolderFile.mkdirs();
		
		//ArchivePlanner archivePlanner = new ArchivePlanner();
		archivePlanner.buildFolderMap(testInputPath + "/data-0", folderMap);
		
		//assertTrue(compareFiles(testOutputPath + "/" + outputFileName, testResultPath + "/" + outputFileName));
	}*/

	@Test
	public void testPrepareArchiveInitial() throws IOException {
		String testName = "pai1";
		System.out.println("#");
		System.out.println("# testPrepareArchiveInitial test start");
		System.out.println("#");
		System.out.println();
		
		FileSet fileSet = new FileSet();
		fileSet.setId("id-pai1");
		fileSet.setName("pai1");
		
		// Create the archivePlanner
		String folderMapPath = archivePlanner.buildFolderMap(fileSet, "123456789", folderManager.getTestInputPath() + "/data-0", folderManager.getTestOutputPathForTest(testName));
		Archive archive = archivePlanner.computeArchive(fileSet, null, folderMapPath, null);
		
		System.out.println("InheritedContent size : " + archive.getInheritedContent().size());
		System.out.println("AddedContent size : " + archive.getAddedContent().size());
		System.out.println("UpdatedContent size : " + archive.getUpdatedContent().size());
		System.out.println("DeletedContent size : " + archive.getDeletedContent().size());
		
		/*for(String str: archive.getAddedContent()) {
			String[] strPart = str.split("\t");
			System.out.println("archiver.addFile(testRootPath + \"/data-0/" + strPart[0] + "\");");
		}*/
				
		assertTrue(archive.getInheritedContent().isEmpty());
		assertTrue(archive.getAddedContent().size() == 333);
		assertTrue(archive.getUpdatedContent().isEmpty());
		assertTrue(archive.getDeletedContent().isEmpty());
		
		System.out.println();
		System.out.println("#");
		System.out.println("# testPrepareArchiveInitial test end");
		System.out.println("#");
		System.out.println();	
	}
	
	@Test
	public void testPrepareArchiveAfterFileCreation() throws IOException {
		String testName = "pafc";
		String testFolder = folderManager.getTestOutputPathForTest(testName);
		folderManager.resetOrCreateFolder(testFolder);
		
		String inputFolder = testFolder + "/data-0";
		String archiveReport1 = testFolder + "/dataReport_t0.txt";
		String archiveReport2 = testFolder + "/dataReport_t1.txt";
		
		System.out.println("#");
		System.out.println("# testPrepareArchiveAfterFileCreation test start");
		System.out.println("#");
		System.out.println();
		System.out.println("# Prepare files for test");
		
		FileSet fileSet = new FileSet();
		fileSet.setId("id-" + testName);
		fileSet.setName(testName);
		
		long startTime0 = System.currentTimeMillis();
		
		// Prepare initial data to the inputFolder
		FileUtils.copyDirectory(new File(folderManager.getTestInputPath() + "/data-0"), new File(inputFolder), true);
		FileUtils.deleteDirectory(new File(inputFolder + "/folder_0/folder_1"));
		
		long endTime0 = System.currentTimeMillis();
		System.out.println("T0 test preparation duration : " + (endTime0 - startTime0) + " ms");
		
		System.out.println("");
		System.out.println("# Run test");
				
		long startTime1 = System.currentTimeMillis();
		
		// Make an archive of the initial state
		String folderMapPath1 = archivePlanner.buildFolderMap(fileSet, "1234567890", inputFolder, testFolder);
		Archive archive1 = archivePlanner.computeArchive(fileSet, null, folderMapPath1, null);
		archive1.writeReport(archiveReport1);
		
		long endTime1 = System.currentTimeMillis();
		System.out.println("T0 state report creation duration : " + (endTime1 - startTime1) + " ms");
		
		// Add files
		//FileUtils.copyDirectory(new File(folderManager.getTestInputPath() + "/data-0/folder_0/folder_1"), new File(inputFolder), true);
		
		// Add some files...
		File addedFolder = new File(inputFolder + "/folder_0/folder_1");
		addedFolder.mkdirs();
		FileUtils.copyDirectory(new File(folderManager.getTestInputPath() + "/data-0/folder_0/folder_1"), addedFolder, true);
		
		
		long startTime2 = System.currentTimeMillis();
		
		// Make an archive of the initial state
		String folderMapPath2 = archivePlanner.buildFolderMap(fileSet, "1234567890", inputFolder, testFolder);
		Archive archive2 = archivePlanner.computeArchive(fileSet, archiveReport1, folderMapPath2, null);
		archive2.writeReport(archiveReport2);
		
		long endTime2 = System.currentTimeMillis();
		System.out.println("T1 state report creation duration : " + (endTime2 - startTime2));
		System.out.println();
		
		System.out.println("InheritedContent size : " + archive2.getInheritedContent().size());
		System.out.println("AddedContent size : " + archive2.getAddedContent().size());
		System.out.println("UpdatedContent size : " + archive2.getUpdatedContent().size());
		System.out.println("DeletedContent size : " + archive2.getDeletedContent().size());
		
		assertTrue(archive2.getInheritedContent().size() == 286);
		assertTrue(archive2.getAddedContent().size() == 47);
		assertTrue(archive2.getUpdatedContent().isEmpty());
		assertTrue(archive2.getDeletedContent().isEmpty());

		System.out.println();
		System.out.println("#");
		System.out.println("# testPrepareArchiveAfterFileCreation test end");
		System.out.println("#");
		System.out.println();
		//assertTrue(compareFiles(testOutputPath + "/" + outputFileName, testResultPath + "/" + outputFileName));
	}
	
	@Test
	public void testPrepareArchiveAfterFileModification() throws IOException {
		String testName = "pafm";
		String testFolder = folderManager.getTestOutputPathForTest(testName);
		folderManager.resetOrCreateFolder(testFolder);
		
		String inputFolder = testFolder + "/data-0";
		String archiveReport1 = testFolder + "/dataReport_t0.txt";
		String archiveReport2 = testFolder + "/dataReport_t1.txt";
		
		logger.debug("#");
		logger.debug("# testPrepareArchiveAfterFileModification test start");
		logger.debug("#");
		logger.debug("");
		logger.debug("# Prepare files for test");
		
		FileSet fileSet = new FileSet();
		fileSet.setId("id-" + testName);
		fileSet.setName(testName);
		
		long startTime0 = System.currentTimeMillis();
		
		// Prepare initial data to the inputFolder
		FileUtils.copyDirectory(new File(folderManager.getTestInputPath() + "/data-0"), new File(inputFolder), true);
		
		long endTime0 = System.currentTimeMillis();
		logger.debug("T0 test preparation duration : " + (endTime0 - startTime0));		

		logger.debug("");
		logger.debug("# Run test");
		
		// Create the archivePlanner	
		long startTime1 = System.currentTimeMillis();
		
		// Make an archive of the initial state
		String folderMapPath1 = archivePlanner.buildFolderMap(fileSet, "123456789", inputFolder, testFolder);
		Archive archive1 = archivePlanner.computeArchive(fileSet, null, folderMapPath1, null);
		archive1.writeReport(archiveReport1);
		
		long endTime1 = System.currentTimeMillis();
		logger.debug("T0 state report creation duration : " + (endTime1 - startTime1));
		
		// Update some files...
		File folderMap1File = new File(folderMapPath1); 
		BufferedReader br = new BufferedReader(new FileReader(folderMap1File)); 

		String string = null;
		while ((string = br.readLine()) != null){
			if((!string.isEmpty()) && (string.startsWith("folder_8"))) {
				String[] mapEntryInfos = string.split("\t");
				
				if(mapEntryInfos[1].equals("f"))
					Files.write(Paths.get(inputFolder + "/" + mapEntryInfos[0]), "\n\nFile updated !!!".getBytes(), StandardOpenOption.APPEND);
			}
		}

		long startTime2 = System.currentTimeMillis();
		
		// Make an archive of the initial state
		String folderMapPath2 = archivePlanner.buildFolderMap(fileSet, "123456789_2", inputFolder, testFolder);
		Archive archive2 = archivePlanner.computeArchive(fileSet, archiveReport1, folderMapPath2, null);
		archive2.writeReport(archiveReport2);
		
		long endTime2 = System.currentTimeMillis();
		logger.debug("T1 state report creation duration : " + (endTime2 - startTime2));
		logger.debug("");
		
		logger.debug("InheritedContent size : " + archive2.getInheritedContent().size());
		logger.debug("AddedContent size : " + archive2.getAddedContent().size());
		logger.debug("UpdatedContent size : " + archive2.getUpdatedContent().size());
		logger.debug("DeletedContent size : " + archive2.getDeletedContent().size());
		
		assertTrue(archive2.getInheritedContent().size() == 137);
		assertTrue(archive2.getAddedContent().isEmpty());
		assertTrue(archive2.getUpdatedContent().size() == 196);
		assertTrue(archive2.getDeletedContent().isEmpty());
		
		logger.debug("");
		logger.debug("#");
		logger.debug("# testPrepareArchiveAfterFileModification test end");
		logger.debug("#");
	}
	
	@Test
	public void testPrepareArchiveAfterFileDeletion() throws IOException {
		String testName = "pafd";
		String testFolder = folderManager.getTestOutputPathForTest(testName);
		folderManager.resetOrCreateFolder(testFolder);
		
		String inputFolder = testFolder + "/data-0";
		String archiveReport1 = testFolder + "/dataReport_t0.txt";
		String archiveReport2 = testFolder + "/dataReport_t1.txt";
		
		// Clear previous test working folder if it exists
		/*File testFolderFile = new File(testFolder);
		if(testFolderFile.exists())
			FileUtils.forceDelete(testFolderFile);
		
		// Create the test working folder
		testFolderFile.mkdirs();*/
		
		System.out.println("#");
		System.out.println("# testPrepareArchiveAfterFileDeletion test start");
		System.out.println("#");
		System.out.println();
		System.out.println("# Prepare files for test");
		
		FileSet fileSet = new FileSet();
		fileSet.setId("id-" + testName);
		fileSet.setName(testName);
		
		long startTime0 = System.currentTimeMillis();
		
		// Prepare initial data to the inputFolder
		FileUtils.copyDirectory(new File(folderManager.getTestInputPath() + "/data-0"), new File(inputFolder), true);
		
		long endTime0 = System.currentTimeMillis();
		System.out.println("T0 test preparation duration : " + (endTime0 - startTime0));		
		
		System.out.println("");
		System.out.println("# Run test");
		
		// Create the archivePlanner	
		long startTime1 = System.currentTimeMillis();
		
		// Make an archive of the initial state
		String folderMapPath1 = archivePlanner.buildFolderMap(fileSet, "123456789", inputFolder, testFolder);
		Archive archive1 = archivePlanner.computeArchive(fileSet, null, folderMapPath1, null);
		archive1.writeReport(archiveReport1);
		
		long endTime1 = System.currentTimeMillis();
		System.out.println("T0 state report creation duration : " + (endTime1 - startTime1));
		
		// Delete some files, folders...
		FileUtils.deleteDirectory(new File(inputFolder + "/folder_0"));
		
		long startTime2 = System.currentTimeMillis();
		
		// Make an archive of the second state
		String folderMapPath2 = archivePlanner.buildFolderMap(fileSet, "123456789_2", inputFolder, testFolder);
		Archive archive2 = archivePlanner.computeArchive(fileSet, archiveReport1, folderMapPath2, null);
		archive2.writeReport(archiveReport2);
		
		long endTime2 = System.currentTimeMillis();
		System.out.println("T1 state report creation duration : " + (endTime2 - startTime2));
		System.out.println();
		
		System.out.println("InheritedContent size : " + archive2.getInheritedContent().size());
		System.out.println("AddedContent size : " + archive2.getAddedContent().size());
		System.out.println("UpdatedContent size : " + archive2.getUpdatedContent().size());
		System.out.println("DeletedContent size : " + archive2.getDeletedContent().size());
		
		assertTrue(archive2.getInheritedContent().size() == 211);
		assertTrue(archive2.getAddedContent().isEmpty());
		assertTrue(archive2.getUpdatedContent().isEmpty());
		assertTrue(archive2.getDeletedContent().size() == 122);
		
		System.out.println();
		System.out.println("#");
		System.out.println("# testPrepareArchiveAfterFileDeletion test end");
		System.out.println("#");
		System.out.println();
	}
	
	@Test
	public void testPrepareArchiveAfterAllFileOperation() throws IOException {
		String testName = "paafo";
		String testFolder = folderManager.getTestOutputPathForTest(testName);
		folderManager.resetOrCreateFolder(testFolder);
		
		String inputFolder = testFolder + "/data-0";
		String archiveReport1 = testFolder + "/dataReport_t0.txt";
		String archiveReport2 = testFolder + "/dataReport_t1.txt";
		
		System.out.println("#");
		System.out.println("# testPrepareArchiveAfterAllFileOperation test start");
		System.out.println("#");
		System.out.println();
		System.out.println("# Prepare files for test");

		FileSet fileSet = new FileSet();
		fileSet.setId("id-" + testName);
		fileSet.setName(testName);
		
		long startTime0 = System.currentTimeMillis();
		
		// Prepare initial data to the inputFolder
		FileUtils.copyDirectory(new File(folderManager.getTestInputPath() + "/data-0"), new File(inputFolder), true);
		FileUtils.deleteDirectory(new File(inputFolder + "/folder_0/folder_1"));
		
		long endTime0 = System.currentTimeMillis();
		System.out.println("T0 test preparation duration : " + (endTime0 - startTime0));		

		System.out.println("");
		System.out.println("# Run test");
		
		// Create the archivePlanner	
		long startTime1 = System.currentTimeMillis();
		
		// Make an archive of the initial state
		String folderMapPath1 = archivePlanner.buildFolderMap(fileSet, "123456789", inputFolder, testFolder);
		Archive archive1 = archivePlanner.computeArchive(fileSet, null, folderMapPath1, null);
		archive1.writeReport(archiveReport1);
		
		long endTime1 = System.currentTimeMillis();
		System.out.println("T0 state report creation duration : " + (endTime1 - startTime1));
		
		// Delete some files, folders...
		FileUtils.deleteDirectory(new File(inputFolder + "/folder_0/folder_7"));
		
		// Add some files...
		File addedFolder = new File(inputFolder + "/folder_0/folder_1");
		addedFolder.mkdirs();
		FileUtils.copyDirectory(new File(folderManager.getTestInputPath() + "/data-0/folder_0/folder_1"), addedFolder, true);
		
		// Update some files...
		File folderMap1File = new File(folderMapPath1); 
		BufferedReader br = new BufferedReader(new FileReader(folderMap1File)); 

		String string = null;
		while ((string = br.readLine()) != null){
			if((!string.isEmpty()) && (string.startsWith("folder_8"))) {
				String[] mapEntryInfos = string.split("\t");
				
				if(mapEntryInfos[1].equals("f"))
					Files.write(Paths.get(inputFolder + "/" + mapEntryInfos[0]), "\n\nFile updated !!!".getBytes(), StandardOpenOption.APPEND);
			}
		}
		
		long startTime2 = System.currentTimeMillis();
		
		// Make an archive of the initial state
		String folderMapPath2 = archivePlanner.buildFolderMap(fileSet, "123456789_2", inputFolder, testFolder);
		Archive archive2 = archivePlanner.computeArchive(fileSet, archiveReport1, folderMapPath2, null);
		archive2.writeReport(archiveReport2);
		
		long endTime2 = System.currentTimeMillis();
		System.out.println("T1 state report creation duration : " + (endTime2 - startTime2));
		System.out.println();
		
		System.out.println("InheritedContent size : " + archive2.getInheritedContent().size());
		System.out.println("AddedContent size : " + archive2.getAddedContent().size());
		System.out.println("UpdatedContent size : " + archive2.getUpdatedContent().size());
		System.out.println("DeletedContent size : " + archive2.getDeletedContent().size());
		
		assertTrue(archive2.getInheritedContent().size() == 71);
		assertTrue(archive2.getAddedContent().size() == 47);
		assertTrue(archive2.getUpdatedContent().size() == 196);
		assertTrue(archive2.getDeletedContent().size() == 19);
		
		System.out.println();
		System.out.println("#");
		System.out.println("# testPrepareArchiveAfterAllFileOperation test end");
		System.out.println("#");
		System.out.println();
	}
	
	@Test
	public void testCreateTempZone() throws Exception {
		String fileSetId = "someFileSetId";
		String timestamp = "1234";
		archivePlanner.createTempZoneForFileSet(fileSetId, timestamp);
		
		File checkFolder = new File(tempFolder + "/" + fileSetId + "/" + timestamp);
		assertTrue(checkFolder.exists());
	}
	
	@Test(expected = Exception.class)
	public void testCreateTempZoneWithNullFileSetId() throws Exception {
		archivePlanner.createTempZoneForFileSet(null, "1234");
	}
	
	@Test(expected = Exception.class)
	public void testCreateTempZoneWithEmptyFileSetId() throws Exception {
		archivePlanner.createTempZoneForFileSet("", "1234");
	}
	
	@Test(expected = Exception.class)
	public void testCreateTempZoneWithNullTimestamp() throws Exception {
		archivePlanner.createTempZoneForFileSet("someFileSetId", null);
	}
	
	@Test(expected = Exception.class)
	public void testCreateTempZoneWithEmptyTimestamp() throws Exception {
		archivePlanner.createTempZoneForFileSet("someFileSetId", "");
	}
	
	@Test(expected = Exception.class)
	public void testCreateTempZoneWithInvalidTimestamp() throws Exception {
		archivePlanner.createTempZoneForFileSet("someFileSetId", "1234a");
	}
	
	// TODO => FolderManager
	private boolean compareFiles(String filePath1, String filePath2) throws IOException
	{
        FileChannel channel1 = new RandomAccessFile(filePath1, "r").getChannel();
        FileChannel channel2 = new RandomAccessFile(filePath2, "r").getChannel();

        // Compare file size
        if (channel1.size() != channel2.size())
            return false;

        // Compare file content
        long size = channel1.size();
        ByteBuffer buffer1 = channel1.map(FileChannel.MapMode.READ_ONLY, 0L, size);
        ByteBuffer buffer2 = channel2.map(FileChannel.MapMode.READ_ONLY, 0L, size);
        for (int pos = 0; pos < size; pos++) {
            if (buffer1.get(pos) != buffer2.get(pos))
            	return false;
        }

        return true;
	}
	
	private boolean compareJibsFiles(String filePath1, String filePath2) throws IOException
	{
		File firstFile = new File(filePath1);
		File secondFile = new File(filePath2);
		BufferedReader br1 = new BufferedReader(new FileReader(firstFile));
		BufferedReader br2 = new BufferedReader(new FileReader(secondFile)); 
		// TODO : close
		
		// Compare line by line
		String string1 = br1.readLine();
		String string2 = br2.readLine();
		while((string1 != null) && (string2 != null)){
		//while (((string1 = br1.readLine()) != null) && ((string2 = br2.readLine()) != null)){
			if(!string1.equals(string2)) {
				// Handle file lines
				if(string1.matches("^[^\\t]+\\tf\\t\\d+\\t.*")) {
					// Remove creation & last modification date
					string1 = string1.substring(0, string1.lastIndexOf("\t"));
					string1 = string1.substring(0, string1.lastIndexOf("\t"));
					string2 = string2.substring(0, string2.lastIndexOf("\t"));
					string2 = string2.substring(0, string2.lastIndexOf("\t"));
					
					if(!string1.equals(string2))
						return false;
				}
					
				else
					return false;
			}
			
			// Read next line of the 2 files
			string1 = br1.readLine();
			string2 = br2.readLine();
		}
		
		if((string1 == null) && (string2 == null))
			return true;
		
		return false;
	}
}
