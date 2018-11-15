package com.smart181.jibs.model;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FileSet {
	private final static String FILESET_ID_FILENAME = ".fileset";
	private final static String FILESET_ID_TAG = "FileSetId : ";
	private final static String FILESET_NAME_TAG = "FileSetName : ";
	private final static String FILESET_CREATION_TIMESTAMP = "Created at : ";
	
	private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS");

	private String id = null;
	private String name = null;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public static FileSet readInFolder(String folderPath) throws IOException {
		// Check the given path existence
		File pathFile = new File(folderPath);
		if(!pathFile.exists())
			throw new IOException("The given path does not exist !");
		
		// Search a file set file inside the given path
		File fileSetFullPath = new File(folderPath + "/" + FILESET_ID_FILENAME);
		if(fileSetFullPath.exists()) {
			
			FileSet fileSet = new FileSet();
			// Extract its content
			List<String> listOfFileEntries = FileUtils.readLines(fileSetFullPath, "UTF-8");
			for(String entry :listOfFileEntries) {
				if(entry.startsWith(FILESET_ID_TAG))
					fileSet.setId(entry.replace(FILESET_ID_TAG, ""));
				else if(entry.startsWith(FILESET_NAME_TAG))
					fileSet.setName(entry.replace(FILESET_NAME_TAG, ""));
			}
			
			if((fileSet.getId() != null) && (fileSet.getName() != null))
				return fileSet;
		}
		
		// No Id found
		return null;
	}
	
	public void writeInFolder(String folderPath) throws Exception {
		// Check if the fileSet has already an id
		if(readInFolder(folderPath) == null) {
			File fileSetFullPath = new File(folderPath + "/" + FILESET_ID_FILENAME);
			
			// Generate a new fileSetId and a timestamp 
			List<String> listOfFileSetEntries = new ArrayList<String>();
			listOfFileSetEntries.add(FILESET_ID_TAG + id);
			listOfFileSetEntries.add(FILESET_NAME_TAG + name);
			listOfFileSetEntries.add(FILESET_CREATION_TIMESTAMP + dateFormatter.format(new Date()));
			
			// Store informations in the newly created fileSet file
			FileUtils.writeLines(fileSetFullPath, "UTF-8", listOfFileSetEntries);
		}
		else
			throw new Exception("This folder has already been registered !");
	}
}
