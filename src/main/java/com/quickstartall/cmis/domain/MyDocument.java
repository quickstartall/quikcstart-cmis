package com.quickstartall.cmis.domain;

import java.io.Serializable;

public class MyDocument extends AbstractDocument implements Serializable {
	private static final long serialVersionUID = 1L;
	private String documentId;
	
	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	public String getFolderType() {
		return folderType;
	}
	public void setFolderType(String folderType) {
		this.folderType = folderType;
	}
	private String folderType;
}
