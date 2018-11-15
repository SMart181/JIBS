package com.smart181.jibs.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

@Component
public class Archiver {
	@Value("${jibs.archiver.password ?:}")
	private String password;
	
	@Value("${jibs.archiver.maxsize:0}")
	private long maxSize;
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}
	
	public List<String> buildArchive(String rootPath, String outputPath) throws IOException, ZipException {
		List<String> listOfResults = new ArrayList<String>();
		
		// Set the compression and encryption parameters
		ZipParameters parameters = new ZipParameters();
		parameters.setIncludeRootFolder(false);
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

		if((password != null) && (!password.isEmpty())) {
			parameters.setEncryptFiles(true);
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
			parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
			parameters.setPassword(password);
		}
		
		// Run the archive creation
		ZipFile archive = new ZipFile(outputPath);
		archive.createZipFileFromFolder(rootPath, parameters, (maxSize > 65536), maxSize);
		
		String outputParentPath = outputPath.substring(0, outputPath.lastIndexOf("/"));
		String outputName = outputPath.substring(outputPath.lastIndexOf("/") + 1, outputPath.length() - 4);
		
		// List all the created archive files
		File FolderFile = new File(outputParentPath);
		File[] directContent = FolderFile.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				//if(name.matches(".*(\\.zip|\\.z\\d\\d)$"))
				if(name.matches("^" + outputName + "(\\.zip|\\.z\\d\\d)$"))
					return true;
				return false;
			}
		});
		
		if(directContent != null) {
			for(File outputFile : directContent)
				listOfResults.add(outputFile.getPath());
		}
		
		return listOfResults;
	}

	public void extractFiles(String zipPath, String outputFolderPath) throws ZipException {
		ZipFile archive = new ZipFile(zipPath);
		if((password != null) && (!password.isEmpty()))
			archive.setPassword(password);
			
		archive.extractAll(outputFolderPath);
	}
}
