package com.smart181.jibs.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smart181.jibs.TestFolderManager;
import com.smart181.jibs.config.Config;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest()
public class ConfigTest {

	private static TestFolderManager folderManager = new TestFolderManager();
	private static String configPath = folderManager.getTestRootPath().replace("\\", "/")
																	  .replace("target/test-classes/test", "config");
	
	@Autowired
	private Config config;
	
	/*@BeforeClass
	public static void init() throws IOException {
		// As no config folder has been given as resources, check it and its content have been created
		File configFolder = new File(configPath);
		assertTrue(configFolder.exists());
		
		File repositoryFile = new File(configPath + "/.repository");
		assertTrue(repositoryFile.exists());
		
		File fileSetFile = new File(configPath + "/.fileSet");
		assertTrue(fileSetFile.exists());
	}*/
	
	@AfterClass
	public static void terminate() throws IOException {
		removeConfigFolder();
	}
	
	@Test
	public void testAddAndGetRepository() throws Exception {
		File repositoryFile = new File(configPath + "/.repository");
		List<String> initialListOfRepositories = FileUtils.readLines(repositoryFile, "UTF-8");

		String repositoryName = "local-Repository_" + System.currentTimeMillis();
		config.addRepository(repositoryName, "c:/backup");
		
		List<String> finalListOfRepositories = FileUtils.readLines(repositoryFile, "UTF-8");
		if(initialListOfRepositories.size() == 0)
			assertEquals(finalListOfRepositories.size(), 2);
		else
			assertEquals(initialListOfRepositories.size() + 1, finalListOfRepositories.size());

		assertTrue(config.getRepository(repositoryName).equals("c:/backup"));		
	}

	@Test(expected = Exception.class)
	public void testAddRepositoryTwice() throws Exception {
		String repositoryName = "local-repository_" + System.currentTimeMillis();
		config.addRepository(repositoryName, "c:/backup");
		config.addRepository(repositoryName, "c:/backup");		
	}
	
	@Test(expected = Exception.class)
	public void testAddRepositoryWithANullName() throws Exception {
		config.addRepository(null, "c:/backup");	
	}
	
	@Test(expected = Exception.class)
	public void testAddRepositoryWithAnEmptyName() throws Exception {
		config.addRepository("", "c:/backup");	
	}
	
	@Test(expected = Exception.class)
	public void testAddRepositoryWithANullUrl() throws Exception {
		String repositoryName = "local-repository_" + System.currentTimeMillis();
		config.addRepository(repositoryName, null);	
	}
	
	@Test(expected = Exception.class)
	public void testAddRepositoryWithAnEmptyUrl() throws Exception {
		String repositoryName = "local-repository_" + System.currentTimeMillis();
		config.addRepository(repositoryName, "");	
	}
	
	@Test
	public void testAddAndGetFileSet() throws Exception {
		File fileSetFile = new File(configPath + "/.fileset");
		List<String> initialListOfFileSets = FileUtils.readLines(fileSetFile, "UTF-8");

		String fileSetName = "fileSet_" + System.currentTimeMillis();
		String fileSetPath = folderManager.getTestRootPath().replace("\\", "/") + "/inputs/fileSet1";
		config.addFileSet(fileSetName, fileSetPath);
		
		List<String> finalListOfFileSets = FileUtils.readLines(fileSetFile, "UTF-8");
		if(initialListOfFileSets.size() == 0)
			assertEquals(finalListOfFileSets.size(), 2);
		else
			assertEquals(initialListOfFileSets.size() + 1, finalListOfFileSets.size());

		assertTrue(config.getFileSet(fileSetName).equals(fileSetPath));		
	}
	
	@Test(expected = Exception.class)
	public void testAddFileSetTwice() throws Exception {
		String fileSetName = "fileSet_" + System.currentTimeMillis();
		String fileSetPath = folderManager.getTestRootPath().replace("\\", "/") + "/inputs/fileSet1";
		config.addFileSet(fileSetName, fileSetPath);
		config.addFileSet(fileSetName, fileSetPath);
	}
	
	@Test(expected = Exception.class)
	public void testAddFileSetWithAnInvalidFolder() throws Exception {
		String fileSetName = "fileSet_" + System.currentTimeMillis();
		String fileSetPath = folderManager.getTestRootPath().replace("\\", "/") + "/inputs/fileSet1000000";
		config.addFileSet(fileSetName, fileSetPath);
	}
	
	@Test(expected = Exception.class)
	public void testAddFileSetWithANullName() throws Exception {
		String fileSetPath = folderManager.getTestRootPath().replace("\\", "/") + "/inputs/fileSet1";
		config.addFileSet(null, fileSetPath);
	}
	
	@Test(expected = Exception.class)
	public void testAddFileSetWithAnEmptyName() throws Exception {
		String fileSetPath = folderManager.getTestRootPath().replace("\\", "/") + "/inputs/fileSet1";
		config.addFileSet("", fileSetPath);
	}
	
	@Test(expected = Exception.class)
	public void testAddFileSetWithANullUrl() throws Exception {
		config.addFileSet("fileSet_" + System.currentTimeMillis(), null);
	}
	
	@Test(expected = Exception.class)
	public void testAddFileSetWithAEmptyUrl() throws Exception {
		config.addFileSet("fileSet_" + System.currentTimeMillis(), "");
	}
	
	private static void removeConfigFolder() throws IOException {
		File configFilePath = new File(configPath);
		if(configFilePath.exists())
			FileUtils.deleteDirectory(configFilePath);
	}
}
