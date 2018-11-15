package com.smart181.jibs.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

@Component
public class Config {
	
	private String repositoryConfigPath;
	private Properties repositoryProperties = new Properties();
	
	private String fileSetConfigPath;
	private Properties fileSetProperties = new Properties();
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public Config() throws IOException {
		initialize();
	}
	
	private void initialize() throws IOException {
		String logString = "JIBS Initialize config";
		
		//logger.debug("JIBS Initialize config.");
		ApplicationHome home = new ApplicationHome(this.getClass());
		String rootAppPath = home.getDir().getPath().replace("\\", "/");
		
		// Prepare the configuration files if the don't exists
		File configFolder = new File(rootAppPath + "/config/logs");
		if(!configFolder.exists()){
			logString += "\nConfig folder not found => creation of the config folder";
			//logger.debug("JIBS Initialize config -> create the config folder.");
			configFolder.mkdirs();
		}
		
		repositoryConfigPath = rootAppPath + "/config/.repository";
		//System.out.println("repositoryPath : " + repositoryPath);
		File repositoryFile = new File(repositoryConfigPath);
		if(!repositoryFile.exists()) {
			logString += "\nRepository config file not existing => creation of the repository config file " + repositoryConfigPath;
			//logger.debug("Initialize jibs config -> create the .repository file.");
			FileUtils.writeStringToFile(repositoryFile, "", "UTF-8");
		}
		
		fileSetConfigPath = rootAppPath + "/config/.fileset";
		//System.out.println("fileSetPath : " + fileSetPath);
		File fileSetFile = new File(fileSetConfigPath);
		if(!fileSetFile.exists()){
			//logger.debug("Initialize jibs config -> create the .fileset file.");
			logString += "\nFileset config file not existing => creation of the fileset config file " + fileSetConfigPath;
			FileUtils.writeStringToFile(fileSetFile, "", "UTF-8");
		}
		
		logString += "\n - Repository config file : " + repositoryConfigPath;
		logString += "\n - Fileset config file : " + fileSetFile;
		logger.debug(logString);
		
		// Load the content of the properties
		loadProperties(repositoryConfigPath, repositoryProperties);
		loadProperties(fileSetConfigPath, fileSetProperties);
	}
	
	public String getRepository(String name) {
		return repositoryProperties.getProperty(name);
	}
	
	public void addRepository(String name, String url) throws Exception{
		if((name == null) || (name.isEmpty()))
			throw new Exception("A repository name must be set !");
		
		if((url == null) || (url.isEmpty()))
			throw new Exception("A local path or an url must be set to add a new repository!");
		
		// Check if a repository with the same name exists
		String urlValue = repositoryProperties.getProperty(name);
		
		if(urlValue != null)
			throw new Exception("A repository called " + name + " already exists, impossible to use that name twice !");
		
		// Add a new entry to the repository file
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(repositoryConfigPath);
			repositoryProperties.setProperty(name, url);
			repositoryProperties.store(output, null);
		}
		catch(Exception e) {
			throw e;
		}
		finally {
			if(output != null)
				output.close();
		}
	}
	
	public Map<String, String> listRepository() {
		return (Map<String, String>) repositoryProperties.clone();
	}
	
	public String getFileSet(String name) {
		return fileSetProperties.getProperty(name);
	}
	
	public void addFileSet(String name, String path) throws Exception{
		if((name == null) || (name.isEmpty()))
			throw new Exception("A fileSet name must be set !");
		
		if((path == null) || (path.isEmpty()))
			throw new Exception("A path to a valid folder must be set !");
		
		// Check if a fileSet with the same name exists
		String urlValue = fileSetProperties.getProperty(name);
		
		if(urlValue != null)
			throw new Exception("A fileSet called " + name + " already exists, impossible to use that name twice !");
		
		// Check that the root folder of the fileSet exists
		File pathFile = new File(path);
		if(!pathFile.exists())
			throw new Exception("Impossible to use the folder " + path + " to create the fileSet (folder does not exists).");
		
		// Add a new entry to the fileSet file
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(fileSetConfigPath);
			fileSetProperties.setProperty(name, path);
			fileSetProperties.store(output, null);
		}
		catch(Exception e) {
			throw e;
		}
		finally {
			if(output != null)
				output.close();
		}
	}
	
	public Map<String, String> listFileSet() {
		return (Map<String, String>) fileSetProperties.clone();
	}
	
	private void loadProperties(String propertyPath, Properties properties) throws IOException {
		FileInputStream input = null;
		try{
			input = new FileInputStream(propertyPath);
			properties.load(input);
		}
		catch(IOException e) {
			logger.debug("Impossible to load configuration from file " + propertyPath);
			throw e;
		}
		finally {
			if(input != null)
				input.close();
		}
	}
}
