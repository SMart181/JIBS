package com.smart181.jibs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class TestFolderManager {
	private String testRootPath = null;
	private String testInputPath = null;
	private String testOutputPath = null;
	private String testResultPath = null;

	public TestFolderManager() {
		try {
			testRootPath = new ClassPathResource("").getFile().getPath() + "/test";
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		testInputPath = testRootPath + "/inputs";
		testOutputPath = testRootPath + "/outputs";
		testResultPath = testRootPath + "/results";
			
		File testOutputPathFile = new File(testOutputPath);
		if(!testOutputPathFile.exists())
			testOutputPathFile.mkdir();
	}

	public String getTestRootPath() {
		return testRootPath;
	}

	public String getTestInputPath() {
		return testInputPath;
	}

	public String getTestinputPathForTest(String testname) {
		String inputPath = testInputPath + "/" + testname;
		File inputPathFile = new File(inputPath);
		if(inputPathFile.exists())
			return inputPath;
		
		return null;
	}
	
	public String getTestOutputPath() {
		return testOutputPath;
	}
	
	public String getTestOutputPathForTest(String testname) throws IOException {
		String outputPath = testOutputPath + "/" + testname;
		File outputPathFile = new File(outputPath);
		
		if((!outputPathFile.exists()) && (!createFolder(outputPath)))
				throw new IOException("Impossible to create the folder " + outputPath);
		
		return outputPath;
	}
	
	public String getTestResultPath() {
		return testResultPath;
	}
	
	public boolean createFolder(String folderFullPath) {
		File newFolder = new File(folderFullPath);
		return newFolder.mkdirs();
	}
	
	public void deleteFolder(String folderFullPath) throws IOException {
		File folder = new File(folderFullPath);
		FileUtils.deleteDirectory(folder);
	}
	
	public void resetOrCreateFolder(String folderFullPath) throws IOException {
		File folderFullPathFile = new File(folderFullPath);
		if(folderFullPathFile.exists())
			deleteFolder(folderFullPath);
		
		createFolder(folderFullPath);
	}
	
	public List<String> getFolderContent(String folderPath, String extension) throws IOException{
		//List<String> listOfResults = new ArrayList<String>();
		return Files.walk(Paths.get(folderPath)).filter(Files::isDirectory).map(p -> p.toFile().getPath()).collect(Collectors.toList());
		/*Files.walk(Paths.get(folderPath)).filter(p -> p.endsWith(extension))
										 .forEach(p -> listOfResults.add(p.toFile().getPath().replace(folderPath, "")));*/
	
		//return listOfResults;
	}
	
	public boolean compareFolders(String folder1Path, String folder2Path) throws IOException {
		// Compare two folders recursively based on the filenames and file size.
		
		// Get content of first directory
		Path path1 = Paths.get(folder1Path);
	    final TreeSet<String> path1ContentTree = new TreeSet<String>();
	    Files.walkFileTree(path1, new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	            Path relPath = path1.relativize(file);
	            String entry = relPath.toString() + "\t" + attrs.size();
	            path1ContentTree.add(entry);
	            return FileVisitResult.CONTINUE;
	        }
	    });

	    // Get content of second directory
	    Path path2 = Paths.get(folder2Path);
	    final TreeSet<String> path2ContentTree = new TreeSet<String>();
	    Files.walkFileTree(path2, new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	            Path relPath = path2.relativize(file);
	            String entry = relPath.toString() + "\t" + attrs.size();
	            path2ContentTree.add(entry);
	            return FileVisitResult.CONTINUE;
	        }
	    });
	    
	    /*System.out.println("path1ContentTree.size() : " + path1ContentTree.size());
	    System.out.println("path2ContentTree.size() : " + path2ContentTree.size());
	    
	    Iterator<String> iter1 = path1ContentTree.iterator();
	    Iterator<String> iter2 = path2ContentTree.iterator();
	    int i=0;
	    while(iter1.hasNext() || iter2.hasNext()) {
	    	System.out.println( i + " - " + iter1.next());
	    	System.out.println(i +  " - " + iter2.next());
	    	i++;
	    }
	    
	    System.out.println("---------------------------------------------");
	    while(iter2.hasNext()) {
	    	System.out.println(" iter2 only - " + iter2.next());
	    }*/
	    
	    return path1ContentTree.equals(path2ContentTree);
	}
}
