package com.quickstartall.cmis;

import java.io.IOException;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;

import com.quickstartall.cmis.domain.MyDocument;

public interface CMISClient {
	MyDocument getMyDocumentById(String id) throws DocumentException;
	MyDocument getMyDocumentByPath(String path) throws DocumentException;
	List<MyDocument> getAllMyDocuments(String documentId) throws IOException, DocumentException;
	Document getAlfrescoDocumentById(String id) throws DocumentException;
	Document getAlfrescoDocumentByPath(String path) throws DocumentException;
	String saveMyDocument(MyDocument documentDomain) throws DocumentException;
	void updateMyDocument(MyDocument documentDomain);
	void printAllMyDocumentsFromAlfresco() throws IOException, DocumentException;
	void deleteDocumentByPath(String path);
	String saveMyDocumentToAlfrescoRoot(MyDocument documentDomain)
			throws DocumentException;

}
