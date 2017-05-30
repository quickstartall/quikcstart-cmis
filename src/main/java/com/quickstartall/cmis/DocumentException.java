package com.quickstartall.cmis;

public class DocumentException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DocumentException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public DocumentException(String message) {
		super(message);
	}
	
	public DocumentException(Throwable cause) {
		super(cause);
	}
}
