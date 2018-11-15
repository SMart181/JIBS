package com.smart181.jibs;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.smart181.jibs.config.Config;
import com.smart181.jibs.model.Archive;
import com.smart181.jibs.model.FileSet;
import com.smart181.jibs.service.ArchivePlanner;
import com.smart181.jibs.service.Archiver;
import com.smart181.jibs.service.NameValidator;

@SpringBootApplication
public class JibsApplication implements ApplicationRunner {
	
	private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmmssSSS");
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
    @Autowired 
    ConfigurableApplicationContext context;
    
	@Autowired
	private ArchivePlanner archivePlanner;
	
	@Autowired
	private Config config;
	
	@Autowired
	private Archiver archiver;

	@Value("${jibs.keepTempFiles:false}")
	private boolean keepTempFiles;
	
	public static void main(String[] args) {
		SpringApplication.run(JibsApplication.class, args);
	}
	
	public void exit(int errorCode) {
		System.exit(SpringApplication.exit(context, () -> errorCode));
	}

	@Override
	public void run(ApplicationArguments args) {
		List<String> listOfArgs = args.getNonOptionArgs();
		
		//logger.debug("JIBS launched with " + listOfArgs.size() + " arguments");
		//listOfArgs.stream().map(a -> "  - " + a).forEach(logger::debug);
		
		if(listOfArgs.size() == 0)
			printHelp();
		else
			runCommand(listOfArgs.get(0), listOfArgs.subList(1, listOfArgs.size()));
	}
	
	public void runCommand(String command, List<String> listOfArgs) {
		
		if(command.equals("help")) {
			if(listOfArgs.size() !=1)
				printHelp();
			else
				printHelpForCommand(listOfArgs.get(0));
		}
		else if(command.equals("register-fileset")) {
			if(listOfArgs.size() !=2)
				printHelp();
			else
				registerFileSet(listOfArgs.get(0), listOfArgs.get(1));
		}
		else if(command.equals("list-fileset")) {
			if(listOfArgs.size() !=0)
				printHelp();
			else
				listFileSets();
		}
		else if(command.equals("register-repository")) {
			if(listOfArgs.size() !=2)
				printHelp();
			else
				registerRepository(listOfArgs.get(0), listOfArgs.get(1));
		}
		else if(command.equals("list-repository")) {
			if(listOfArgs.size() !=0)
				printHelp();
			else
				listRepositories();
		}
		else if(command.equals("backup")) {
			if(listOfArgs.size() < 2)
				printHelp();
			else {
				String comment = null;
				if(listOfArgs.size() >= 3)
					comment = listOfArgs.get(2);
				backup(listOfArgs.get(0), listOfArgs.get(1), comment);
			}
		}
		else if(command.equals("restore")) {
			if(listOfArgs.size() !=4)
				printHelp();
			else
				restore(listOfArgs.get(0), listOfArgs.get(1), listOfArgs.get(2), listOfArgs.get(3));
		}
		else
			printHelp();
	}
	
	private void printHelp() {
		System.out.println();
		System.out.println("usage jibs [command] [options]");
		System.out.println();
		System.out.println("Commands :");
		System.out.println("  backup\t\tRun a backup operation on the fileset to a repository");
		System.out.println("  help\t\t\tDisplay global help or help on a given command");
		System.out.println("  list-fileset\t\tList all the fileset alias.");
		System.out.println("  list-repository\tList all the repository alias.");
		System.out.println("  register-fileset\tRegister the given folder as a folder to backup.");
		System.out.println("  register-repository\tRegister the given path/url as a repository.");
		System.out.println("  restore\t\tRestore a fileset with a backup store on a given repository");
		System.out.println();
		System.out.println("To get more information about how to use command, please use 'jibs help' and the name of the command.");
		System.out.println();
	}
	
	private void printHelpForCommand(String command) {
		
		if(command.equals("backup")) {
			System.out.println();
			System.out.println("backup runs a backup of a given fileset and store it in a given repository.");
			System.out.println();
			System.out.println("Syntax :");
			System.out.println("  jibs backup fileset repository comment");
			System.out.println();
			System.out.println("Options :");
			System.out.println("  fileset\t\tPath or Alias to a registered fileset");
			System.out.println("  repository\t\tPath or Alias to a registered repository");
			System.out.println("  comment\t\t[Optional] Comment added to the backup report");
			System.out.println();
		}
		else if(command.equals("list-fileset")) {
			System.out.println();
			System.out.println("list-fileset displays the list of all the fileset registered with their alias.");
			System.out.println();
			System.out.println("Syntax :");
			System.out.println("  jibs list-fileset");
			System.out.println();
		}
		else if(command.equals("list-repository")) {
			System.out.println();
			System.out.println("list-repository displays the list of all the repository registered with their alias.");
			System.out.println();
			System.out.println("Syntax :");
			System.out.println("  jibs list-repository");
			System.out.println();
		}
		else if(command.equals("register-fileset")) {
			System.out.println();
			System.out.println("register-fileset add a new fileset and give it an alias.");
			System.out.println();
			System.out.println("Syntax :");
			System.out.println("  jibs register-fileset alias filesetPath");
			System.out.println();
			System.out.println("Options :");
			System.out.println("  alias\t\t\tName to associate to the fileset to register");
			System.out.println("  filesetPath\t\tPath to the root folder containing all the file to backup");
			System.out.println();
		}
		else if(command.equals("register-repository")) {
			System.out.println();
			System.out.println("register-repository add a new repository and give it an alias.");
			System.out.println();
			System.out.println("Syntax :");
			System.out.println("  jibs register-repository alias repositoryUrl");
			System.out.println();
			System.out.println("Options :");
			System.out.println("  alias\t\t\tName to associate to the repository to register");
			System.out.println("  repositoryUrl\t\tUrl or path to the repository to register");
			System.out.println();
		}
		else if(command.equals("restore")) {
			System.out.println();
			System.out.println("restore gets the previous fileset state stored on a repository (.");
			System.out.println();
			System.out.println("Syntax :");
			System.out.println("  jibs restore repository filesetId archiveId outputPath");
			System.out.println();
			System.out.println("Options :");
			System.out.println("  repository\t\tUrl, path or alias to a registered repository");
			System.out.println("  filesetId\t\tFilesetId, path to fileset or Alias to the fileset to restore");
			System.out.println("  archiveId\t\tId of the archive to restore");
			System.out.println("  outputPath\t\tFolder to use for the restore operation");
			System.out.println();
		}
		else {
			System.out.println();
			System.out.println(command + " is an unknown command.");
			System.out.println();
			System.out.println("Please type 'jibs help' to list availables commands.");
			System.out.println();
		}
	}
	
	private void registerFileSet(String fileSetName, String fileSetPath){
		logger.debug("JIBS registerFileSet command launched");
		try {
			// If no fileset file is present
			if(FileSet.readInFolder(fileSetPath) == null){				
				// Check the fileSetName
				if(!NameValidator.isValid(fileSetName)) {
					System.out.println("Error : Invalid fileSet Name.\n The filSet name should contains characters a-z, A-Z, 0-9, - and _.");
					exit(-1);
				}
				
				// Generate a new fileSet
				FileSet newFileSet = new FileSet();
				newFileSet.setName(fileSetName);
				newFileSet.setId(UUID.randomUUID().toString());
								
				// Add it in the given folder
				newFileSet.writeInFolder(fileSetPath);
				config.addFileSet(fileSetName, fileSetPath);
				
				System.out.println("This folder has been registered with name " + fileSetName + " and id " + newFileSet.getId());
			}
			else
				System.out.println("This folder has already been registered !");
		} catch (Exception e) {
			System.out.println("Impossible to register the given folder !");
			e.printStackTrace();
		}
	}
	
	private void listFileSets() {
		logger.debug("JIBS listFileSet command launched");
		
		System.out.println("Registered filesets :");
		config.listFileSet().forEach((fileSetName, fileSetPath) -> System.out.println("  " + fileSetName + " -> " +  fileSetPath));
	}
	
	private void registerRepository(String repositoryName, String repositoryUrl) {
		String logString = "JIBS registerRepository command launched\nArguments :";
		logString += "\n - repositoryName : " + repositoryName;
		logString += "\n - repositoryUrl : " + repositoryUrl;
		logger.debug(logString);
		
		try {
			// Search the repository in the config
			if(config.getRepository(repositoryName) == null) {
				// Add it in the config
				config.addRepository(repositoryName, repositoryUrl);
				System.out.println("This repository has been successfully registered with name " + repositoryName + " and path " + repositoryUrl + " !");
			}
			else
				System.out.println("This repository has already been registered !");
		} catch (Exception e) {
			System.out.println("Impossible to register the given repository !");
			e.printStackTrace();
		}
	}
	
	private void listRepositories() {
		logger.debug("JIBS listRepositories command launched");
		System.out.println("Registered repositories :");
		config.listRepository().forEach((repositoryName, repositoryPath) -> System.out.println("  " + repositoryName + " -> " +  repositoryPath));
	}
	
	private void backup(String filesetPath, String repository, String comment){
		String logString = "JIBS backup command launched\nArguments :";
		logString += "\n - folder : " + filesetPath;
		logString += "\n - repository : " + repository;
		logString += "\n - comment : " + comment;
		logger.debug(logString);

		// Handle folderPath shortcut name
		filesetPath = filesetPath.replace("\\", "/");
		if(!filesetPath.contains("/")) {
			// This is fileSet registered name
			filesetPath = config.getFileSet(filesetPath);
			if(filesetPath != null)
				filesetPath = filesetPath.replace("\\", "/");
		}
		
		// Handle repository shortcut name
		repository = repository.replace("\\", "/");
		if(!repository.matches(".*(/|@)+.*")) {		// TODO : à verifier
			// This is repository registered name
			repository = config.getRepository(repository);
			if(repository != null)
				repository = repository.replace("\\", "/");
		}
		
		String tmpZone = null;
		try {
			// Get the fileSet infos of the folder to backup
			FileSet fileSet = FileSet.readInFolder(filesetPath);
			if(fileSet != null){
				// Access to the archive zone and get the last archive report
				archivePlanner.setRepository(repository);
				String lastArchiveReportPath = archivePlanner.getLastArchiveReport(fileSet.getId());
				
				// Create the local working zone
				String timestamp = dateFormatter.format(new Date());
				tmpZone = archivePlanner.createTempZoneForFileSet(fileSet.getId(), timestamp);
				String outputArchiveZone = tmpZone.substring(0, tmpZone.lastIndexOf("/"));
				
				// Analyse the folder and prepare the archive
				String folderMapPath = archivePlanner.buildFolderMap(fileSet, timestamp, filesetPath, outputArchiveZone);
				Archive archive = archivePlanner.computeArchive(fileSet, lastArchiveReportPath, folderMapPath, comment);
				
				if(!archive.isUpToDate()) {
					// Gather files and build a package
					archivePlanner.prepareArchive(archive, filesetPath, tmpZone);
					
					List<String> listOfArchivePaths = archiver.buildArchive(tmpZone, outputArchiveZone + "/" + archive.getArchiveId() + ".zip");
					for(String archivePath : listOfArchivePaths)
						archive.addArchiveFile(archivePath);
					
					// Create reports
					String archiveReportPath = outputArchiveZone + "/lastarchive.report";
					archive.writeReport(archiveReportPath);
					
					String lastReportPath = outputArchiveZone + "/" + archive.getArchiveId() + ".report";
					archive.writeReport(lastReportPath);
						
					// Send package to the archiveZone
					for(String archivePath : listOfArchivePaths)
						archivePlanner.sendArchiveToRepository(fileSet.getId(), archivePath, archiveReportPath, lastReportPath);
					
					System.out.println("JIBS Backup operation successfully done !");
				}
				else {
					System.out.println("The fileset is up to date, no modifications detected since last backup !");
				}
			}
		}
		catch (Exception e) {
			System.out.println("OPERATION FAILED : Impossible to run backup for folder " + filesetPath + " to repository " + repository + "!" );
			e.printStackTrace();
		}
		finally{
			try{
				// Clean temporary file unless keepTempFiles option is set when it is possible
				if((tmpZone != null) && (!keepTempFiles))
					FileUtils.forceDelete(new File(tmpZone));
			}
			catch(Exception e){
			}			
		}
	}
	
	private void restore(String repository, String fileSetId, String archiveId, String folderPath){
		String logString = "JIBS restore command launched\nArguments :";
		logString += "\n - repository : " + repository;
		logString += "\n - fileSetId : " + fileSetId;
		logString += "\n - archiveId : " + archiveId;
		logString += "\n - folder : " + folderPath;
		logger.debug(logString);
		
		folderPath = folderPath.replace("\\", "/");

		// Handle repository shortcut name
		repository = repository.replace("\\", "/");
		if(!repository.matches(".*(/|@)+.*")) {		// TODO : à verifier
			// This is repository registered name
			repository = config.getRepository(repository);
			if(repository != null)
				repository = repository.replace("\\", "/");
		}
		
		String tmpZone = null;
		try {
			// Get the archive report knowing the archiveId
			archivePlanner.setRepository(repository);
			
			// Build the list of all the archive reports to get in the repository
			List<String> listOfArchiveReports = archivePlanner.retrieveNecessaryArchiveReports(fileSetId, archiveId);
				
			// Build the list of all the archive file to get in the repostory
			List<String> listOfArchivePaths = new ArrayList<String>();
			for(String archiveReports : listOfArchiveReports) {
				Archive archive = Archive.fromReport(archiveReports);
				listOfArchivePaths.addAll(archivePlanner.getArchive(fileSetId, archive.getArchiveFiles().stream().toArray(String[]::new)));
			}
				
			// 
				
			// Download the necessary archives
			/*List<String> listOfArchivePaths = new ArrayList<String>();
			for(String tmpArchiveId : listOfArchiveIds)
				listOfArchivePaths.add(archivePlanner.getArchive(fileSetId, tmpArchiveId));*/
			
			String timestamp = dateFormatter.format(new Date());
			tmpZone = archivePlanner.createTempZoneForFileSet(fileSetId, timestamp);
			
			// Loop on the archives (from the first to the last)
			for(int i=(listOfArchivePaths.size()-1) ; i>=0 ; i-- ) {
				// Extract content to the output folder (override existing files)
				String archivePath = listOfArchivePaths.get(i);
				
				if(archivePath.endsWith(".zip")) {
					String archiveReportPath = archivePath.replace(".zip", ".report");
					String currentArchiveId = archivePath.substring(archivePath.lastIndexOf("/") + 1, archivePath.length()-4);
					archiver.extractFiles(archivePath, tmpZone);
						
					// Get the file deleted since the last archive
					Archive previousArchive = Archive.fromReport(archiveReportPath);
					List<String> listOfFilesToDelete = previousArchive.getDeletedContent();
						
					// Remove the deleted files
					for(String fileToDelete : listOfFilesToDelete) {
						String[] fileInfos = fileToDelete.split("\t");
						
						// Delete files only
						if(fileInfos[1].equals("f"))
							// Append root temp path and filepath and delete the file
							FileUtils.forceDelete(new File(tmpZone + "/" + fileInfos[0]));
					}
						
					// Here remains only folders => delete them
					for(String folderToDelete : listOfFilesToDelete) {
						String[] folderInfos = folderToDelete.split("\t");
						
						// Delete folders only
						if(folderInfos[1].equals("d"))
							// Append root temp path and filepath and delete the file
							FileUtils.deleteDirectory(new File(tmpZone + "/" + folderInfos[0]));
					}
						
					// At least, delete the report inserted inside the archive
					FileUtils.forceDelete(new File(tmpZone + "/" + currentArchiveId + ".shortreport"));
				}
			}
				
			// Copy the result in the selected directory
			FileUtils.copyDirectory(new File(tmpZone), new File(folderPath), true);
			System.out.println("JIBS Restore operation successfully done !");
		}
		catch (Exception e) {
			System.out.println("OPERATION FAILED : Impossible to run restoration for fileset " + fileSetId + " (archive : " + archiveId + ")" + " from repository " + repository + "!" );
			e.printStackTrace();
		}
		finally{
			try{
				// Clean temporary file unless keepTempFiles option is set when it is possible
				if((tmpZone != null) && (!keepTempFiles))
					FileUtils.forceDelete(new File(tmpZone));
			}
			catch(Exception e){
			}			
		}
	}
}
