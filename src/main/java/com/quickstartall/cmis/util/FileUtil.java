package com.quickstartall.cmis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;

import com.quickstartall.cmis.domain.MyDocument;

public class FileUtil {
	private static final String rootDirectory = "C:/MyWork/Alfresco";
	private static MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
	private static final String CMIS_ALFRESCO_FOLDER_TYPE = "cmis:folder";
	
	public static MyDocument getMyDocumentFromLocalSystem(String documentPath) throws IOException {
		FileInputStream inputStream = null;
		File file = getFile(documentPath);
		try {
			System.out.println("file Absolute Path:" + file.getAbsolutePath());
			inputStream = new FileInputStream(file);
			int length = (int) file.length();
			byte fileContent[] = new byte[length];
			//String documentPath = getDocumentPath(file);
			inputStream.read(fileContent);

			MyDocument myDocument = createDocument(documentPath);
			myDocument.setData(fileContent);
			myDocument.setLength(length);
			myDocument.setContentType(mimetypesFileTypeMap.getContentType(file));
			myDocument.setFolderType(CMIS_ALFRESCO_FOLDER_TYPE);
			inputStream.close();
			return myDocument;
		} catch (IOException ioException) {
			System.out.println("There was an error fetching document from local File System");
			throw ioException;
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	private static File getFile(String documentPath) throws FileNotFoundException {
		File root = new File(rootDirectory);
		File docFile = new File(root, documentPath);

		if (!docFile.exists()) {
			throw new FileNotFoundException(docFile.getPath());
		}
		return docFile;
	}
	
	
	public static File[] getFilesFromResources() {
		//getClass().getClassLoader().get
		File rootDirectory = new File("src/main/resources");
		return rootDirectory.listFiles();
	}
	
	private static MyDocument createDocument(String docPath) {
		//If there is mix of forward and backward slashes just make it consistent. Here we are changing it to Windows format
		String windowsDocPath = docPath.replaceAll("\\/", "\\\\");
		
		String[] pathNames = windowsDocPath.split("\\\\"); //For Windows
		//String[] pathElements = idString.split("\\/"); use this for Linux
		MyDocument myDocument = new MyDocument();
		//myDocument.setId(idString);
		myDocument.setDocumentId(pathNames[0]);
		myDocument.setName(pathNames[pathNames.length - 1]);
		return myDocument;
	}
	
	/*private String getDocumentPath(File file) {
        String absolutePath = file.getAbsolutePath();
        if (!absolutePath.startsWith(rootDirectory))
            throw new IllegalArgumentException("File not in cache path.");
        String result = absolutePath.substring(rootDirectory.length() + 1);
        System.out.println("getUniquePath:" + result);
        return result;
    }*/
	
	public static MyDocument getMyDocumentFromResourcesFolder(File file) throws IOException {
		FileInputStream inputStream = null;
		//File file = getFile(documentPath);
		try {
			System.out.println("file Absolute Path:" + file.getAbsolutePath());
			inputStream = new FileInputStream(file);
			int length = (int) file.length();
			byte fileContent[] = new byte[length];
			//String documentPath = getDocumentPath(file);
			inputStream.read(fileContent);

			//MyDocument myDocument = createDocument(documentPath);
			MyDocument myDocument = new MyDocument();
			myDocument.setName(file.getName());
			
			myDocument.setData(fileContent);
			myDocument.setLength(length);
			myDocument.setContentType(mimetypesFileTypeMap.getContentType(file));
			myDocument.setFolderType(CMIS_ALFRESCO_FOLDER_TYPE);
			inputStream.close();
			return myDocument;
		} catch (IOException ioException) {
			System.out.println("There was an error fetching document from local File System");
			throw ioException;
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
}
