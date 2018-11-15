package com.smart181.jibs.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.smart181.jibs.model.Archive;
import com.smart181.jibs.model.FileSet;
import com.smart181.jibs.service.impl.SftpTransfertManager;
import com.smart181.jibs.service.impl.TransfertManager;

@Component
public class ArchivePlanner {
	// Constants	
	private final static String INHERITED_FILE_TAG = "##Inherited content";
	private final static String UPDATED_FILE_TAG = "##Updated content";
	private final static String ADDED_FILE_TAG = "##Added content";
	
	// RegExp Filters
	private final static String PRESENT_FILE_TAG_TESTER = "^(" + INHERITED_FILE_TAG + "|" + UPDATED_FILE_TAG + "|" + ADDED_FILE_TAG + ")$";
	private final static String SECTION_TAG_TESTER = "^##.*";
	private final static String LOCAL_REPOSITORY_TESTER = "^(/|[A-Z_a-z]:).*";// TODO minuscules
	private final static String SFTP_REPOSITORY_TESTER = "^sftp://[a-z_A-Z_0-9]*:.*@.*";
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private String repository = null;
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	@Lazy
	private TransfertManager transfertManager;
	
	@Value("${jibs.tempFolder:${java.io.tmpdir}/jibs}")
	private String tempFolder;
	
	private StringBuffer buffer = null;

	public void setRepository(String repository) {
		repository = repository.replace("\\", "/");
		this.repository = repository;
		
		if(repository.matches(LOCAL_REPOSITORY_TESTER)) {
			logger.debug("Local repository configuration detected !");
			transfertManager = (TransfertManager) beanFactory.getBean(LocalTransfertManager.class);
		}
		else if(repository.matches(SFTP_REPOSITORY_TESTER)) {
			logger.debug("Remote sftp repository configuration detected !");
			transfertManager = (TransfertManager) beanFactory.getBean(SftpTransfertManager.class);
		}
		// TODO : sftp require password
		// TODO : à compléter et faire une erreur
		//else if()
	}
	
	public List<String> getArchive(String fileSetId, String... archiveFileNames) throws Exception {
		return transfertManager.getArchiveFiles(repository, fileSetId, archiveFileNames);
	}
	
	public String getArchiveReport(String fileSetId, String archiveId) throws Exception {
		return transfertManager.getArchiveReport(repository, fileSetId, archiveId);
	}
	
	public String getLastArchiveReport(String fileSetId) throws Exception {
		return transfertManager.getLastArchiveReport(repository, fileSetId);
	}
	
	public String buildFolderMap(FileSet fileSet, String timestamp, String folderPath, String workingFolderPath) throws IOException {
		//long startTime = System.currentTimeMillis();
		String folderMapPath = workingFolderPath + "/" + timestamp + ".foldermap";

		buffer = new StringBuffer();
		buffer.append("# Associated FileSet : ");
		buffer.append(fileSet.getName());
		buffer.append(" - ");
		buffer.append(fileSet.getId());
		buffer.append("\n");
		extractFolderContent(folderPath, folderPath);
		
		//long endTime = System.currentTimeMillis();
		//long delta = endTime - startTime;
		//System.out.println("Time : " + delta + " ms");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(folderMapPath)));
		
		//write contents of StringBuffer to a file
		writer.write(buffer.toString());
		buffer = null;
		
		//flush and close the stream
		writer.flush();
		writer.close();
		
		return folderMapPath;
	}
	
	private void extractFolderContent(String rootPath, String workingFolderPath) throws IOException{
		// TODO : Check Buffer is not null
		
		File FolderFile = new File(workingFolderPath);
		File[] directContent = FolderFile.listFiles();
		
		if(directContent != null) {
			for(File file : directContent) {
				// Extract current file/folder useful information
				Path p = Paths.get(file.getAbsolutePath());
			    BasicFileAttributes view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
			    
				// Add all the file / folder data to extract in the string buffer
				buffer.append(file.getPath().substring(rootPath.length()+1).replace("\\", "/"));
				buffer.append("\t");
				if(file.isDirectory())
				{
					// Append directory infos
					buffer.append("d");
					buffer.append("\n");
					
					// Recursive call to process children
					extractFolderContent(rootPath, file.getPath());
				}
				else
				{
					// Append file infos
					buffer.append("f");
					buffer.append("\t");
					buffer.append(view.size());
					buffer.append("\t");
				    buffer.append(view.creationTime().toInstant());
					buffer.append("\t");
					buffer.append(view.lastModifiedTime().toInstant());
					buffer.append("\n");
				}
			}
		}
	}
	

	
	public Archive computeArchive(FileSet fileSet, String previousArchiveReport, String folderMapPath, String comment) throws IOException{
		// TODO null fileSet => Error
		List<String> listOfModifiedFiles = new ArrayList<String>();
		TreeSet<String> archivePresentFileSet = null;
		String string = null;
		Archive newArchive = new Archive(fileSet);
		newArchive.setComment(comment);
		
		if(previousArchiveReport != null) {
			// If a previous archive has been found
			File lastArchiveDescFile = new File(previousArchiveReport); 
			BufferedReader br2 = new BufferedReader(new FileReader(lastArchiveDescFile)); 
			 
			// Extract the list of the files present in the previous archive
			archivePresentFileSet = new TreeSet<String>();
			TreeSet<String> cursorList = null;
			while ((string = br2.readLine()) != null){
				if(!string.isEmpty()) {
					if(string.matches(PRESENT_FILE_TAG_TESTER))
						// This is a section handling files existing in the archive
						cursorList = archivePresentFileSet;
					else if(string.matches(SECTION_TAG_TESTER))
						// This is a section handling absent files (deleted content) or other information
						cursorList = null;
					else if(cursorList != null)
					//else if((cursorList != null) && (!string.equals(DELETED_FILE_TAG)))
						// Simply add this line in the pointed section
						cursorList.add(string);
					else if((cursorList == null) && (string.matches("^ArchiveId : .*")))
						newArchive.setPreviousArchiveId(string.substring(12));
				}
			}
			
			br2.close();
		}
			
		// Extract folder content file names
		buffer = new StringBuffer();
		File folderMapFile = new File(folderMapPath); 
		BufferedReader br = new BufferedReader(new FileReader(folderMapFile)); 
			
		while ((string = br.readLine()) != null){
			// Skip comments
			if(string.startsWith("#"))
				continue;
			
			// Analysis file archive history
			if(archivePresentFileSet != null){
				if(archivePresentFileSet.contains(string)){
					// If current entry match with previous archive present files => files are identical
					newArchive.addInheritedContent(string);
					archivePresentFileSet.remove(string);
				}
				else
					// The file can be a new file, an updated file or a deleted file
					listOfModifiedFiles.add(string);
			}
			else
				// No Previous archive => All files are added files
				newArchive.addAddedContent(string);
		}
		
		br.close();
		
		if(listOfModifiedFiles != null) {
			// Loop on the remaining items
			while(listOfModifiedFiles.size() != 0) {
				// Extract first item and remove it from the list
				String item = listOfModifiedFiles.get(0);
				listOfModifiedFiles.remove(0);
				//logger.debug("file to process : " + item);
				
				// Split item infos
				String[] itemInfos = item.split("\t");
				//logger.debug("remainingFile itemInfos : " + itemInfos);
				if(itemInfos.length > 0){
					String itemFilePath = itemInfos[0];
					
					// Get the most similar item from the archivePresentFileSet
					String archiveItem = archivePresentFileSet.higher(itemFilePath);
					
					if(archiveItem != null) {
						if(archiveItem.startsWith(itemFilePath + "\t")) {
							// This is the same item => it has been updated
							newArchive.addUpdatedContent(item);
							archivePresentFileSet.remove(archiveItem);
						}
						else {
							// The most similar item of the previous archive does not match => it is a new file 
							newArchive.addAddedContent(item);
						}
					}
					else {
						// The item is not matching => it is a new file
						newArchive.addAddedContent(item);
					}
				}
			}
		}
		
		// Handle file deleted from the previous archive
		if((archivePresentFileSet != null) && (!archivePresentFileSet.isEmpty())) {
			while(!archivePresentFileSet.isEmpty())
				newArchive.addDeletedContent(archivePresentFileSet.pollFirst());
		}
		
		return newArchive;
	}
	
	public void prepareArchive(Archive archive, String fileSetPath, String archiveRootPath) throws IOException {
		// Get modified files to store in the archive file
		List<String> listOfArchiveContent = new ArrayList<String>();
		listOfArchiveContent.addAll(archive.getAddedContent());
		listOfArchiveContent.addAll(archive.getUpdatedContent());
		
		// Loop on the modified files to copy them in the archive root path
		for(String fileDesc : listOfArchiveContent) {
			// Extract deep path
			String[] fileInfos = fileDesc.split("\\t");
				
			if(fileInfos[1].equals("d")) {
				logger.debug("  - create directory " + archiveRootPath + "/" + fileInfos[0]);
				File newFolderFile = new File(archiveRootPath + "/" + fileInfos[0]);
				newFolderFile.mkdir();
			}
			else {
				logger.debug("  - copy file from " + fileSetPath + "/" + fileInfos[0] + " to " + archiveRootPath + "/" + fileInfos[0]);
				FileUtils.copyFile(new File(fileSetPath + "/" + fileInfos[0]), new File(archiveRootPath + "/" + fileInfos[0]), true);
			}
		}
		
		archive.writeReport(archiveRootPath + "/" + archive.getArchiveId() + ".shortreport");
	}
	
	public List<String> retrieveNecessaryArchiveReports(String fileSetId, String archiveId) throws Exception {
		List<String> listOfResults = new ArrayList<String>();
		
		// Loop on the archiveId
		String previousArchiveId = archiveId;
		while((previousArchiveId != null) && (!previousArchiveId.equals("None"))) {
			// Get the archive report
			String archiveReportPath = getArchiveReport(fileSetId, previousArchiveId);
			Archive extractedArchive = Archive.fromReport(archiveReportPath);
			
			if(previousArchiveId.equals("lastarchive")) {
				// Override data if the "lastarchive" keyword is found
				archiveReportPath = getArchiveReport(fileSetId, extractedArchive.getArchiveId());
				extractedArchive = Archive.fromReport(archiveReportPath);
			}
			
			// Store the archiveReportPath and update the previousArchiveId
			listOfResults.add(archiveReportPath);
			previousArchiveId = extractedArchive.getPreviousArchiveId();
		}
		
		return listOfResults;
	}
	
	public String createTempZoneForFileSet(String fileSetId, String timestamp) throws Exception {
		if((fileSetId == null) || (fileSetId.isEmpty()))
			throw new Exception("Invalid fileSetId !");
		
		if((timestamp == null) || (timestamp.isEmpty()) || (!timestamp.matches("^\\d+$")))
			throw new Exception("Invalid timestamp !");
		
		// Add a new folder in the temp working folder with the id of fileSetId
		String fileSetZone = tempFolder + "/" + fileSetId + "/" + timestamp;
		File fileSetTempFolderFile = new File(fileSetZone);
		
		// Delete the fileSet temp folder if it exists
		if(fileSetTempFolderFile.exists())
			FileUtils.deleteDirectory(fileSetTempFolderFile);
		
		// Create it
		if(!fileSetTempFolderFile.exists()) {
			fileSetTempFolderFile.mkdirs();
			return fileSetZone;
		}
		
		return null;
	}
	
	public void sendArchiveToRepository(String fileSetId, String... filePaths) throws Exception {
		transfertManager.sendArchiveFiles(repository, fileSetId, filePaths);
	}
}
