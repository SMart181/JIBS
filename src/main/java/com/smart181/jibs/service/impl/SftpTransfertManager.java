package com.smart181.jibs.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.smart181.jibs.model.FileSystemAccount;

@Component
public class SftpTransfertManager implements TransfertManager {
	private final JSch jsch = new JSch();
	
	@Value("${jibs.tempFolder:${java.io.tmpdir}/jibs}")
	private String tempFolder;
	
	private FileSystemAccount accountInfos = null;

	@Override
	public String getArchiveReport(String repository, String fileSetId, String archiveId) throws Exception {
		if((fileSetId == null) || (fileSetId.isEmpty()))
			throw new Exception("Invalid fileSetId !");
		
		if((archiveId == null) || (archiveId.isEmpty()))
			throw new Exception("Invalid archiveId !");
		
		Session session = null;
		ChannelSftp sftpChannel = null;
		try {
			// Open sftp session and channel
			session = connectToRepository(repository);
			session.connect();
			
	        sftpChannel = (ChannelSftp) session.openChannel("sftp");
	        sftpChannel.connect();
	
	        File localFolderFile = new File(tempFolder + "/" + fileSetId);
	        if(!localFolderFile.exists())
	        localFolderFile.mkdirs();
	        
	        //System.out.println(" Remote path : ." + accountInfos.getPath() + "/" + fileSetId + "/" + archiveId + ".report");
	        //System.out.println(" Tmp path : " + localFolderFile.getPath() + "/" + archiveId + ".report");
	        
	        sftpChannel.get("." + accountInfos.getPath() + "/" + fileSetId + "/" + archiveId + ".report", localFolderFile.getPath() + "/" + archiveId + ".report");
	        
			return localFolderFile.getPath() + "/" + archiveId + ".report";
		}
		catch (Exception e) {
			if(!e.getMessage().equals("No such file"))// TODO : just on get
				throw new Exception("Impossible to get the archive " + archiveId + " for the fileSet " + fileSetId + ".");
		}
		finally {
			if(session != null)
				session.disconnect();
			
			if(sftpChannel != null)
				sftpChannel.disconnect();
		}
		
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
		
		String currentArchive = null;
		Session session = null;
		ChannelSftp sftpChannel = null;
		try {
			// Open sftp session and channel
			session = connectToRepository(repository);
			session.connect();
		
	        sftpChannel = (ChannelSftp) session.openChannel("sftp");
	        sftpChannel.connect();

	        File localFolderFile = new File(tempFolder + "/" + fileSetId);
	        if(!localFolderFile.exists())
	        localFolderFile.mkdirs();
	        
			// Loop on the archive files to get
			List<String> listOfResults = new ArrayList<String>();	
	        for(String archiveName : archiveFileNames) {
	        	currentArchive = archiveName;
	        	String localArchiveFolder = localFolderFile.getPath() + "/" + archiveName;
	        	//System.out.println(localArchiveFolder);
	        	//System.out.println("." + accountInfos.getPath() + "/" + fileSetId + "/" + archiveName);
	        
	        	sftpChannel.get("." + accountInfos.getPath() + "/" + fileSetId + "/" + archiveName, localArchiveFolder);
	        	listOfResults.add(localArchiveFolder);
	        }
	        return listOfResults;
		}
		catch (Exception e) {
			throw new Exception("Impossible to get the archive " + currentArchive + " for the fileSet " + fileSetId + ".");
		}
		finally {
			if(session != null)
				session.disconnect();
			
			if(sftpChannel != null)
				sftpChannel.disconnect();
		}
	}

	@Override
	public void sendArchiveFiles(String repository, String fileSetId, String... filePaths) throws Exception {
		if((repository == null) || (repository.isEmpty()))
			throw new Exception("Invalid repository !");
		
		if((fileSetId == null) || (fileSetId.isEmpty()))
			throw new Exception("Invalid fileSetId !");
		
		String currentFilePath = null;
		Session session = null;
		ChannelSftp sftpChannel = null;
		try {
			// Open sftp session
			session = connectToRepository(repository);
			session.connect();
		
	        sftpChannel = (ChannelSftp) session.openChannel("sftp");
	        sftpChannel.connect();
	        
	        try {
	        	 // Check the existence of the fileSet in the repository
	        	sftpChannel.ls("." + accountInfos.getPath() + "/" + fileSetId);
	        }
	        catch(SftpException e) {
	        	sftpChannel.mkdir("." + accountInfos.getPath() + "/" + fileSetId);
	        }
	        catch (Exception e) {
				throw e;
			}
	        
	        // Loop on the file to send to the repository
	        for(String filePath : filePaths) {
	        	filePath = filePath.replace("\\", "/");
	        	currentFilePath = filePath;
	        	sftpChannel.put(filePath, "." + accountInfos.getPath() + "/" + fileSetId + "/" + filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length()));
	        }
		}
		catch (Exception e) {
			throw new Exception("Impossible to send the file " + currentFilePath + " to repository folder of the fileSet " + fileSetId + ".");
		}
		finally {
			if(session != null)
				session.disconnect();
			
			if(sftpChannel != null)
				sftpChannel.disconnect();
		}
	}

	private Session connectToRepository(String respositoy) throws Exception {
		// Extract repository connection infos
	    accountInfos = new FileSystemAccount(respositoy);
	    
	    // Try to connect to the repository
        Session session = jsch.getSession(accountInfos.getUser(), accountInfos.getHost(), accountInfos.getPort());
        session.setPassword(accountInfos.getPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        
        return session;
	}
}
