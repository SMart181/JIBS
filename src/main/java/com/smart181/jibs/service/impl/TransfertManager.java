package com.smart181.jibs.service.impl;

import java.util.List;

public interface TransfertManager {
	
	public String getArchiveReport(String repository, String fileSetId, String archiveId) throws Exception;
	public String getLastArchiveReport(String repository, String fileSetId) throws Exception;
	
	public List<String> getArchiveFiles(String repository, String fileSetId, String... archiveFileNames) throws Exception;
	public void sendArchiveFiles(String repository, String fileSetId, String... filePaths) throws Exception;
}
