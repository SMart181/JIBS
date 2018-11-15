package com.smart181.jibs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smart181.jibs.model.FileSet;
import com.smart181.jibs.service.ArchivePlanner;
import com.smart181.jibs.TestFolderManager;
import com.smart181.jibs.config.Config;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class JibsApplicationDefaultConfigTest {

	private final PrintStream originalOut = System.out;
	
    @Autowired
    ApplicationContext ctx;
    
	@Autowired
	ArchivePlanner archivePlanner;
	
	@Autowired
	private Config config;
	
	@Autowired
	private TestFolderManager folderManager;
	
	@Test
	public void testRunRegisterAndListFileset() throws Exception {
		String testName = "rralf";
		String outputFolder = folderManager.getTestOutputPathForTest(testName);
		String inputFolder = outputFolder + "/inputs";
		folderManager.resetOrCreateFolder(inputFolder);
		
		inputFolder += "/data-0";
		System.out.println("#");
		System.out.println("# testRunRegisterAndListFileset test start");
		System.out.println("#");
		System.out.println();
		System.out.println("# Prepare files for test");

		long startTime0 = System.currentTimeMillis();
		
		// Prepare initial data to the inputFolder
		FileUtils.copyDirectory(new File(folderManager.getTestInputPath() + "/data-0"), new File(inputFolder), true);
		
		long endTime0 = System.currentTimeMillis();
		
		System.out.println("T0 test preparation duration : " + (endTime0 - startTime0));	
		System.out.println();
		
		long startTime1 = System.currentTimeMillis();
		
		// Run the register-fileset command
		String fileSetName = "test-register-fileset_" + System.currentTimeMillis();
		JibsApplication runner = ctx.getBean(JibsApplication.class);

		List<String> args = new ArrayList<String>();
		args.add(fileSetName);
		args.add(inputFolder);
		
        runner.runCommand ("register-fileset", args);        
        assertTrue(config.getFileSet(fileSetName).equals(inputFolder));
        
		long endTime1 = System.currentTimeMillis();
		
		System.out.println("T1 state fileset registration duration : " + (endTime1 - startTime1));
		System.out.println();
		
		long startTime2 = System.currentTimeMillis();
		
		// Spy the console display
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outputStream));
		
		// run the list-fileset command
		runner.runCommand ("list-fileset", new ArrayList<String>());
		
		// Check results
		String consoleDisplay = outputStream.toString();
		assertTrue(consoleDisplay.contains("Registered filesets :"));
		assertTrue(consoleDisplay.contains("  " + fileSetName + " -> " + inputFolder));
		
		// Reset the console display
		System.setOut(originalOut);
		
		long endTime2 = System.currentTimeMillis();
		
		System.out.println("T2 state fileset list duration : " + (endTime2 - startTime2));
		System.out.println();

		System.out.println();
		System.out.println("#");
		System.out.println("# testRunRegisterAndListFileset test end");
		System.out.println("#");
		System.out.println();
	}

	@Test
	public void testRunRegisterAndListRepository() throws Exception {
		System.out.println("#");
		System.out.println("# testRunRegisterAndListRepository test start");
		System.out.println("#");
		System.out.println();
		System.out.println("# Prepare files for test");

		
		// Run the register-fileset command
		long startTime0 = System.currentTimeMillis();
		String repositoryName = "test-register-repository_" + System.currentTimeMillis();
		String repositoryPath = "c:\temp";
		JibsApplication runner = ctx.getBean(JibsApplication.class);
		
		List<String> args = new ArrayList<String>();
		args.add(repositoryName);
		args.add(repositoryPath);
		
        runner.runCommand ("register-repository", args); 
        assertTrue(config.getRepository(repositoryName).equals("c:\temp"));
        
		long endTime0 = System.currentTimeMillis();
		System.out.println("T0 state repository registration duration : " + (endTime0 - startTime0));
		System.out.println();

		long startTime1 = System.currentTimeMillis();
		
		// Spy the console display
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outputStream));
		
		// run the list-repository command
		runner.runCommand ("list-repository", new ArrayList<String>());
		
		// Check results
		String consoleDisplay = outputStream.toString();
		assertTrue(consoleDisplay.contains("Registered repositories :"));
		assertTrue(consoleDisplay.contains("  " + repositoryName + " -> " + repositoryPath));
		
		// Reset the console display
		System.setOut(originalOut);
		
		long endTime1 = System.currentTimeMillis();
		
		System.out.println("T1 state repository list duration : " + (endTime1 - startTime1));
		System.out.println();
		
		System.out.println();
		System.out.println("#");
		System.out.println("# testRunRegisterAndListRepository test end");
		System.out.println("#");
		System.out.println();
	}	
	
	@Test
	public void testRunBackupComplexArchive() throws Exception {
		String testName = "rbca";
		String outputFolder = folderManager.getTestOutputPathForTest(testName).replace("\\", "/");
		String inputFolder = outputFolder + "/inputs";
		String repositoryFolder = outputFolder + "/repository";
		folderManager.resetOrCreateFolder(outputFolder);
		folderManager.resetOrCreateFolder(inputFolder);
		folderManager.resetOrCreateFolder(repositoryFolder);
		
		inputFolder += "/data-0";
		System.out.println("#");
		System.out.println("# testRunBackupComplexArchive test start");
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
		
		long startTime1 = System.currentTimeMillis();
		JibsApplication runner = ctx.getBean(JibsApplication.class);
        System.out.println("backup");
        System.out.println(inputFolder);
        System.out.println(repositoryFolder);
        
		List<String> args = new ArrayList<String>();
		args.add("test-folder");
		args.add(inputFolder);
		
		//runner.run ("register-fileset", "test-folder", inputFolder);
        runner.runCommand ("register-fileset", args); 
        
		List<String> args2 = new ArrayList<String>();
		args2.add(inputFolder);
		args2.add(repositoryFolder);
		
        //runner.run ("backup", inputFolder, repositoryFolder);
        runner.runCommand ("backup", args2); 
        		
		long endTime1 = System.currentTimeMillis();
		System.out.println("T0 state report creation duration : " + (endTime1 - startTime1));
		
		// Delete some files, folders...
		FileUtils.deleteDirectory(new File(inputFolder + "/folder_0/folder_7"));
		
		//runner.run ("backup", inputFolder, repositoryFolder);
		runner.runCommand ("backup", args2); 
		 
		// Add some files...
		File addedFolder = new File(inputFolder + "/folder_0/folder_1");
		addedFolder.mkdirs();
		FileUtils.copyDirectory(new File(folderManager.getTestInputPath() + "/data-0/folder_0/folder_1"), addedFolder, true);
		
		//runner.run ("backup", inputFolder, repositoryFolder);
		runner.runCommand ("backup", args2); 
		
		// Update some files...
		String[] listOfFilesToModify = {"/folder_8/folder_9/folder_10/file_118.txt",
										"/folder_8/folder_9/folder_10/file_119.txt",
										"/folder_8/folder_9/folder_10/file_120.txt",
										"/folder_8/folder_9/folder_10/file_121.txt",
										"/folder_8/folder_9/folder_10/file_122.txt",
										"/folder_8/folder_9/folder_10/file_123.txt",
										"/folder_8/folder_9/folder_10/file_124.txt",
										"/folder_8/folder_9/folder_10/file_125.txt",
										"/folder_8/folder_9/folder_10/file_126.txt",
										"/folder_8/folder_9/folder_10/file_127.txt"};
		
		for (String string : listOfFilesToModify)
			Files.write(Paths.get(inputFolder + "/" + string), "\n\nFile updated !!!".getBytes(), StandardOpenOption.APPEND);
		
		long startTime2 = System.currentTimeMillis();
		
		// Make an archive of the initial state
		//runner.run ("backup", inputFolder, repositoryFolder);
		runner.runCommand ("backup", args2); 
		
		long endTime2 = System.currentTimeMillis();
		System.out.println("T1 state report creation duration : " + (endTime2 - startTime2));
		System.out.println();

		List<String> listOfCreatedFolders = Files.walk(Paths.get(repositoryFolder)).filter(Files::isDirectory)
			 	   																   .map(f -> f.toFile().getPath().replace("\\", "/"))
			 	   																   .filter(f -> (!f.equals(repositoryFolder)))
			 	   																   .collect(Collectors.toList());


		// Check that only one fileset folder is created in the repository
		assertEquals(1, listOfCreatedFolders.size());
		
		// Check that 4 zip files were created in the repository
		assertEquals(4, Files.walk(Paths.get(listOfCreatedFolders.get(0))).filter(f -> !(Files.isDirectory(f)))
																		  .map(p -> p.toFile().getPath())
																		  .filter(s -> s.endsWith(".zip"))
																		  .count());
		
		// Check that 5 report files were created in the repository
		assertEquals(5, Files.walk(Paths.get(listOfCreatedFolders.get(0))).filter(f -> !(Files.isDirectory(f)))
																		  .map(p -> p.toFile().getPath())
																		  .filter(s -> s.endsWith(".report"))
																		  .count());

		System.out.println();
		System.out.println("#");
		System.out.println("# testRunBackupComplexArchive test end");
		System.out.println("#");
		System.out.println();
	}

	@Test
	public void testRunBackupComplexMultipleArchive() throws Exception {
		String testName = "rbcma";
		String outputFolder = folderManager.getTestOutputPathForTest(testName);
		String inputFolder = outputFolder + "/inputs";
		String repositoryFolder = outputFolder + "/repository";
		folderManager.resetOrCreateFolder(outputFolder);
		folderManager.resetOrCreateFolder(inputFolder);
		folderManager.resetOrCreateFolder(repositoryFolder);
		
		inputFolder += "/data-0";
		System.out.println("#");
		System.out.println("# testRunBackupComplexMultipleArchive test start");
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
		
		long startTime1 = System.currentTimeMillis();
		JibsApplication runner = ctx.getBean(JibsApplication.class);
        System.out.println("backup");
        System.out.println(inputFolder);
        System.out.println(repositoryFolder);
        
		List<String> args = new ArrayList<String>();
		args.add("test-folder");
		args.add(inputFolder);
		
		//runner.run ("register-fileset", "test-folder", inputFolder);
        runner.runCommand ("register-fileset", args); 
        
		List<String> args2 = new ArrayList<String>();
		args2.add(inputFolder);
		args2.add(repositoryFolder);
		
        //runner.run ("backup", inputFolder, repositoryFolder);
        runner.runCommand ("backup", args2); 
        		
		long endTime1 = System.currentTimeMillis();
		System.out.println("T0 state report creation duration : " + (endTime1 - startTime1));
		
		// Delete some files, folders...
		FileUtils.deleteDirectory(new File(inputFolder + "/folder_0/folder_7"));
		
		//runner.run ("backup", inputFolder, repositoryFolder);
		runner.runCommand ("backup", args2); 
		 
		// Add some files...
		File addedFolder = new File(inputFolder + "/folder_0/folder_1");
		addedFolder.mkdirs();
		FileUtils.copyDirectory(new File(folderManager.getTestInputPath() + "/data-0/folder_0/folder_1"), addedFolder, true);
		
		//runner.run ("backup", inputFolder, repositoryFolder);
		runner.runCommand ("backup", args2); 
		
		// Update some files...
		String[] listOfFilesToModify = {"/folder_8/folder_9/folder_10/file_118.txt",
										"/folder_8/folder_9/folder_10/file_119.txt",
										"/folder_8/folder_9/folder_10/file_120.txt",
										"/folder_8/folder_9/folder_10/file_121.txt",
										"/folder_8/folder_9/folder_10/file_122.txt",
										"/folder_8/folder_9/folder_10/file_123.txt",
										"/folder_8/folder_9/folder_10/file_124.txt",
										"/folder_8/folder_9/folder_10/file_125.txt",
										"/folder_8/folder_9/folder_10/file_126.txt",
										"/folder_8/folder_9/folder_10/file_127.txt"};
		
		for (String string : listOfFilesToModify)
			Files.write(Paths.get(inputFolder + "/" + string), "\n\nFile updated !!!".getBytes(), StandardOpenOption.APPEND);
		
		long startTime2 = System.currentTimeMillis();
		
		// Make an archive of the initial state
		//runner.run ("backup", inputFolder, repositoryFolder);
		runner.runCommand ("backup", args2); 
		
		long endTime2 = System.currentTimeMillis();
		System.out.println("T1 state report creation duration : " + (endTime2 - startTime2));
		System.out.println();

		System.out.println();
		System.out.println("#");
		System.out.println("# testRunBackupComplexMultipleArchive test end");
		System.out.println("#");
		System.out.println();
	}
	
	@Test
	public void testRunRestoreComplexArchive() throws Exception {
		String testName = "rrca";
		String repositoryFolder = folderManager.getTestRootPath() + "/repository";
		String outputFolder = folderManager.getTestOutputPathForTest(testName);
		
		JibsApplication runner = ctx.getBean(JibsApplication.class);
		
		List<String> args = new ArrayList<String>();
		args.add(repositoryFolder);
		args.add("68360a01-2b38-4264-be54-a75212f3db65");
		args.add("lastarchive");
		args.add(outputFolder);
		
		runner.runCommand("restore", args);
		
		assertTrue(folderManager.compareFolders(outputFolder, folderManager.getTestResultPath() + "/rrca"));
	}
	
	
}
