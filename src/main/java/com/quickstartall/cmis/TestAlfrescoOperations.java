package com.quickstartall.cmis;

import java.io.File;
import java.io.IOException;

import com.quickstartall.cmis.util.FileUtil;

public class TestAlfrescoOperations {
	private static final String URL = "http://localhost:2038/alfresco";
	private static final String alfrescoRepoRoot = "/QuickStartAll";
	private static String userName = "admin";
	private static String password = "admin"; // password for my local System

	// private static String filePath1 = "QuickStartAll-Set1/QuickStartAll file 1.pdf";

	public static void main(String[] args) {

		CMISClient client = new CMISClientImpl.CMISClientBuilder(URL, userName,
				password).alfrescoRepoRoot(alfrescoRepoRoot).build();
		
		File[] files = FileUtil.getFilesFromResources();
		saveDocuments(files, client);
		
		printAlfrescoDocuments(client);
	}

	private static void saveDocuments(File[] files, CMISClient client) {
		if (files == null || files.length == 0) {
			System.out.println("There are no valid files to save. Exiting...");
			return;
		}
		try {
			for (File file : files) {
				System.out.println("\n=========================="+ file.getName() + "=================================================");
				System.out.println("********************Deleting any existing version of the file bfore savibg it...");
				client.deleteDocumentByPath(file.getName());
				
				System.out.println("\n*****************Now, going to save the document...");
				client.saveMyDocumentToAlfrescoRoot(FileUtil
						.getMyDocumentFromResourcesFolder(file));
				//System.out.println("******************Done saving document=" + file.getName() +  " to alfresco");
				
				System.out.println("==========================================================================================================");
			}
		} catch (Exception exception) {
			System.out
					.println("There was some error saving document to Alfresco");
			exception.printStackTrace();
		}

	}

	// Following method will be helpful if you want save document from local
	// file System
	/*
	 * private static void saveDocument(CMISClient client) { try {
	 * client.saveMyDocument(FileUtil.getMyDocumentFromLocalSystem(filePath1));
	 * } catch (DocumentException e) {
	 * System.out.println("There was some error saving the document");
	 * e.printStackTrace(); } catch (IOException e) { System.out.println(
	 * "There was an IO Exception while saving the document to Alfresco.");
	 * e.printStackTrace(); } }
	 */

	private static void printAlfrescoDocuments(CMISClient client) {
		try {
			client.printAllMyDocumentsFromAlfresco();
		} catch (DocumentException e) {
			System.out
					.println("There was some exception while fetching document from Alfresco");
			e.printStackTrace();
		} catch (IOException e) {
			System.out
					.println("There was an IO exception while fetching document from Alfresco");
			e.printStackTrace();
		}
	}
}
