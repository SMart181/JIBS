package com.smart181.jibs.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Archive {
	public final static String META_INFO_TAG = "#Archive Infos";
	public final static String ARCHIVE_FILES_TAG = "#Archive Files";
	public final static String INHERITED_FILE_TAG = "##Inherited content";
	public final static String ADDED_FILE_TAG = "##Added content";
	public final static String UPDATED_FILE_TAG = "##Updated content";
	public final static String DELETED_FILE_TAG = "##Deleted content";
	
	private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS");

	private final FileSet fileSet;
	private String archiveId = null;
	private String previousArchiveId = null;
	private String comment = null;
	private List<String> archiveFiles = new ArrayList<String>();
	private List<String> inheritedContent = new ArrayList<String>();
	private List<String> addedContent = new ArrayList<String>();
	private List<String> updatedContent = new ArrayList<String>();
	private List<String> deletedContent = new ArrayList<String>();
	
	public Archive(FileSet fileSet){
		this.fileSet = fileSet;
		
		// Create a new Archive Id
		archiveId = UUID.randomUUID().toString();		
	}
	
	public FileSet getFileSet() {
		return fileSet;
	}
	
	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}
	
	public String getArchiveId() {
		return archiveId;
	}
	
	public String getPreviousArchiveId() {
		return previousArchiveId;
	}

	public void setPreviousArchiveId(String previousArchiveId) {
		this.previousArchiveId = previousArchiveId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	public List<String> getArchiveFiles(){
		return archiveFiles;
	}
	
	public void addArchiveFile(String content){
		archiveFiles.add(content.replace("\\", "/"));
	}
		
	public List<String> getInheritedContent(){
		return inheritedContent;
	}
	
	public void addInheritedContent(String content){
		inheritedContent.add(content);
	}
	
	public List<String> getAddedContent(){
		return addedContent;
	}
	
	public void addAddedContent(String content){
		addedContent.add(content);
	}
	
	public List<String> getUpdatedContent(){
		return updatedContent;
	}
	
	public void addUpdatedContent(String content){
		updatedContent.add(content);
	}
	
	public List<String> getDeletedContent(){
		return deletedContent;
	}
	
	public void addDeletedContent(String content){
		deletedContent.add(content);
	}
	
	public boolean isUpToDate() {
		return ((addedContent.size()==0) && (updatedContent.size() == 0) && (deletedContent.size() == 0));
	}
	
	public static Archive fromReport(String reportPath) throws IOException {		
		// Read its content
		File reportPathFile = new File(reportPath); 
		BufferedReader br = new BufferedReader(new FileReader(reportPathFile)); 
		 
		// Extract the previous archive Id
		Archive newArchive = null;
		String string = null;
		String activeTag = null;
		while ((string = br.readLine()) != null){
			if(!string.isEmpty()){
				if(string.equals(META_INFO_TAG))
					activeTag = META_INFO_TAG;
				else if(string.equals(ARCHIVE_FILES_TAG))
					activeTag = ARCHIVE_FILES_TAG;
				else if(string.equals(INHERITED_FILE_TAG))
					activeTag = INHERITED_FILE_TAG;
				else if(string.equals(ADDED_FILE_TAG))
					activeTag = ADDED_FILE_TAG;
				else if(string.equals(UPDATED_FILE_TAG))
					activeTag = UPDATED_FILE_TAG;
				else if(string.equals(DELETED_FILE_TAG))
					activeTag = DELETED_FILE_TAG;
				else {
					if(activeTag.equals(META_INFO_TAG)) {
						if(string.matches("^FileSet : .*")) {
							FileSet fileSet = new FileSet();
							fileSet.setId(string.substring(10));
							newArchive = new Archive(fileSet);
						}
						else if(string.matches("^ArchiveId : .*"))
							newArchive.setArchiveId(string.substring(12));
						else if(string.matches("^Previous ArchiveId : .*"))
							newArchive.setPreviousArchiveId(string.substring(21));
					}
					else if(activeTag.equals(ARCHIVE_FILES_TAG))
						newArchive.addArchiveFile(string);
					else if(activeTag.equals(INHERITED_FILE_TAG))
						newArchive.addInheritedContent(string);
					else if(activeTag.equals(ADDED_FILE_TAG))
						newArchive.addAddedContent(string);
					else if(activeTag.equals(UPDATED_FILE_TAG))
						newArchive.addUpdatedContent(string);
					else if(activeTag.equals(DELETED_FILE_TAG))
						newArchive.addDeletedContent(string);
				}
			}
		}
		
		br.close();
		
		return newArchive;
	}
	
	public void writeReport(String filePath) throws IOException {
		
		// Insert archive infos.
		StringBuffer buffer = new StringBuffer();
		buffer.append(META_INFO_TAG);
		buffer.append("\nFileSet : ");
		buffer.append(fileSet.getId());
		buffer.append("\nArchiveId : ");
		buffer.append(archiveId);
		buffer.append("\nPrevious ArchiveId : ");
		if(previousArchiveId != null)
			buffer.append(previousArchiveId);
		else
			buffer.append("None");
		buffer.append("\nTimeStamp : ");
		buffer.append(dateFormatter.format(new Date()));
		if(comment != null) {
			buffer.append("\nComment : ");
			buffer.append(comment);
		}
		buffer.append("\n\n");
		
		// If the archive files are presents, include them in the report
		if(archiveFiles.size() != 0) {
			buffer.append(ARCHIVE_FILES_TAG);
			
			for(String archiveFile : archiveFiles) {
				buffer.append("\n");
				buffer.append(archiveFile.substring(archiveFile.lastIndexOf("/")+1));
			}
			buffer.append("\n\n");
		}
		
		// Insert all the reference to file inherited by the previous archives.
		buffer.append(INHERITED_FILE_TAG);
		buffer.append("\n");
		for(String fileRef : inheritedContent) {
			buffer.append(fileRef);
			buffer.append("\n");
		}
		buffer.append("\n");
		
		// Insert all the reference to file added since the previous archives.
		buffer.append(ADDED_FILE_TAG);
		buffer.append("\n");
		for(String fileRef : addedContent) {
			buffer.append(fileRef);
			buffer.append("\n");
		}
		buffer.append("\n");
		
		// Insert all the reference to file updated since the previous archives.
		buffer.append(UPDATED_FILE_TAG);
		buffer.append("\n");
		for(String fileRef : updatedContent) {
			buffer.append(fileRef);
			buffer.append("\n");
		}
		buffer.append("\n");
		
		// Insert all the reference to remaining files (deleted since the previous archives).
		buffer.append(DELETED_FILE_TAG);
		buffer.append("\n");
		for(String fileRef : deletedContent) {
			buffer.append(fileRef);
			buffer.append("\n");
		}
		buffer.append("\n");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath)));
		
		//write contents of StringBuffer to a file
		writer.write(buffer.toString());
		buffer = null;
		
		//flush and close the stream
		writer.flush();
		writer.close();
	}
}