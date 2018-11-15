package com.smart181.jibs.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import com.smart181.jibs.service.impl.TransfertManager;

@Component
public class LocalTransfertManager implements TransfertManager {
	
	public String getArchiveReport(String repository, String fileSetId, String archiveId) throws Exception{
		if((fileSetId == null) || (fileSetId.isEmpty()))
			throw new Exception("Invalid fileSetId !");
		
		if((archiveId == null) || (archiveId.isEmpty()))
			throw new Exception("Invalid archiveId !");
		
		// Build the path of the archive report on the repository
		String archiveReportPath = repository + "/" + fileSetId + "/" + archiveId +".report";
		
		// Check file existence
		File archiveReportPathFile = new File(archiveReportPath);
		if(archiveReportPathFile.exists())
			return archiveReportPath;
		
		return null;
	}
	
	@Override
	public String getLastArchiveReport(String repository, String fileSetId) throws Exception {
		return getArchiveReport(repository, fileSetId, "lastarchive");
	}

	@Override
	public List<String> getArchiveFiles(String repository, String fileSetId, String... archiveFileNames) throws Exception {
		if((fileSetId == null) || (fileSetId.isEmpty()))
			throw new Exception("Invalid fileSetId !");
		
		// Loop on the archive files to get
		List<String> listOfResults = new ArrayList<String>();	
		for(String archiveName : archiveFileNames) {
			String archiveNamePath = repository + "/" + fileSetId + "/" + archiveName;
			
			// Check file existence
			File archivePathFile = new File(archiveNamePath);
			if(!archivePathFile.exists())
				throw new Exception("Unable to retrieve archive file " + archiveName + " in repository " + repository);
			
			listOfResults.add(archiveNamePath);
		}
		
		return listOfResults;
	}

	@Override
	public void sendArchiveFiles(String repository, String fileSetId, String... filePaths) throws Exception {
		// Check repository destination folder exists (or create it)
		File fileSetRepositoryFile = new File(repository + "/" + fileSetId);
		if(!fileSetRepositoryFile.exists())
			fileSetRepositoryFile.mkdirs();
		
		// Loop on the files to copy to the destination folder
		for (String filePath : filePaths)
			FileUtils.copyFileToDirectory(new File(filePath), fileSetRepositoryFile, true);
	}

}
