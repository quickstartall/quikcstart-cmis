package com.quickstartall.cmis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.google.common.io.ByteStreams;
import com.quickstartall.cmis.domain.MyDocument;

public class CMISClientImpl implements CMISClient {
	private Session session;
	private static final String PATHSEPAERATOR = "/";
	private final String url;
	private final String userName;
	private final String password;
	private String alfrescoRepoRoot = "/";

	private CMISClientImpl(CMISClientBuilder builder) {
		this.url = builder.url;
		this.userName = builder.userName;
		this.password = builder.password;
		if (builder.alfrescoRepoRoot != null && !builder.alfrescoRepoRoot.isEmpty()) {
			this.alfrescoRepoRoot = builder.alfrescoRepoRoot;
		}
	}

	public static class CMISClientBuilder {
		private final String url;
		private final String userName;
		private final String password;
		private String alfrescoRepoRoot;

		public CMISClientBuilder(String url, String userName, String password) {
			this.url = url;
			this.userName = userName;
			this.password = password;
		}

		public CMISClientBuilder alfrescoRepoRoot(String alfrescoRepoRoot) {
			this.alfrescoRepoRoot = alfrescoRepoRoot;
			return this;
		}

		public CMISClient build() {
			CMISClientImpl client = new CMISClientImpl(this);

			// Create the session here
			client.createSession();
			// If you want validate something, you can always do here...
			return client;
		}
	}
	
	// Create Alfresco Session
	private void createSession() {
		try {
			System.out.println("About to create Alfresco session...");
			System.out.println("url=" + url + "|root=" + alfrescoRepoRoot + "|username=" + userName);
			Map<String, String> parameter = new HashMap<String, String>();
			if (userName != null) {
				parameter.put(SessionParameter.USER, userName);
			}
			if (password != null) {
				parameter.put(SessionParameter.PASSWORD, password);
			}
			
			parameter.put(SessionParameter.ATOMPUB_URL, url + "/cmisatom");
			parameter.put(SessionParameter.BINDING_TYPE,
					BindingType.ATOMPUB.value());

			parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE,
					alfrescoServiceURL("ACLService"));
			parameter.put(SessionParameter.WEBSERVICES_POLICY_SERVICE,
					alfrescoServiceURL("PolicyService"));
			parameter.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE,
					alfrescoServiceURL("ObjectService"));
			parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE,
					alfrescoServiceURL("DiscoveryService"));
			parameter.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE,
					alfrescoServiceURL("MultiFilingService"));
			parameter.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE,
					alfrescoServiceURL("NavigationService"));
			parameter.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE,
					alfrescoServiceURL("RelationshipService"));
			parameter.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE,
					alfrescoServiceURL("RelationshipService"));
			parameter.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE,
					alfrescoServiceURL("VersioningService"));

			// Get repository, Alfresco has only one repository
			SessionFactory factory = SessionFactoryImpl.newInstance();
			List<Repository> repositories = factory.getRepositories(parameter);
			session = repositories.get(0).createSession();
			System.out
					.println("Alfresco client session created successfully. Now enjoy your day !");
		} catch (Exception e) {
			System.out
					.println("There was an exception while creating Alfresco session");
			e.printStackTrace();
		}
	}

	private String alfrescoServiceURL(String serviceName) {
		String serviceURLBase = url.endsWith("alfresco") ? "/cmis" : "";
		/*System.out.println("WS URL for " + serviceName + "=" + url + serviceURLBase + "/"
				+ serviceName + "?wsdl");*/
		return url + serviceURLBase + "/" + serviceName + "?wsdl";
	}

	@Override
	public MyDocument getMyDocumentById(String id) throws DocumentException {

		MyDocument documentDomain = null;
		try {
			Document document = (Document) session.getObject(id);
			documentDomain = convertAlfrescoDocumentToMyDocument(document);
		} catch (Exception e) {
			throw new DocumentException(e);
		}
		return documentDomain;
	}

	@Override
	public MyDocument getMyDocumentByPath(String path) throws DocumentException {

		MyDocument documentDomain = null;
		try {
			String alfrescoPath = getAlfrescoPath(path);
			Document document = (Document) session
					.getObjectByPath(alfrescoPath);
			documentDomain = convertAlfrescoDocumentToMyDocument(document);
		} catch (Exception e) {
			throw new DocumentException(e);
		}
		return documentDomain;
	}

	private String getAlfrescoPath(String path) {
		return alfrescoRepoRoot + PATHSEPAERATOR + path;
	}

	@Override
	public String saveMyDocumentToAlfrescoRoot(MyDocument documentDomain)
			throws DocumentException {
		return saveToRootFolder(documentDomain);
	}
	
	@Override
	public String saveMyDocument(MyDocument documentDomain)
			throws DocumentException {
		return save(documentDomain, documentDomain.getFolderType());
	}

	//
	private String saveToRootFolder(MyDocument documentDomain)
			throws DocumentException {
		String filename = documentDomain.getName();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");

		properties.put(PropertyIds.NAME, filename);

		try {
			/*Folder parent = createFolderStructureForDocument(
					documentDomain.getDocumentId(), folderType);*/
			
			Folder parent = getRootFolder();
			ContentStream contentStream = createContentStream(
					documentDomain.getName(), documentDomain.getLength(),
					documentDomain.getContentType(), documentDomain.getData());
			Document document = parent.createDocument(properties,
					contentStream, VersioningState.MAJOR);
			System.out.println("Document created/saved successfully in Alfresco. Alfresco ID="
					+ document.getId());

			return document.getId();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DocumentException(e.getMessage());
		}
	}
	//
	
	
	private String save(MyDocument documentDomain, String folderType)
			throws DocumentException {
		String filename = documentDomain.getName();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");

		properties.put(PropertyIds.NAME, filename);

		try {
			Folder parent = createFolderStructureForDocument(
					documentDomain.getDocumentId(), folderType);
			ContentStream contentStream = createContentStream(
					documentDomain.getName(), documentDomain.getLength(),
					documentDomain.getContentType(), documentDomain.getData());
			Document document = parent.createDocument(properties,
					contentStream, VersioningState.MAJOR);
			System.out.println("Document created/saved successfully in Alfresco. Alfresco ID="
					+ document.getId());

			return document.getId();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DocumentException(e.getMessage());
		}

	}

	@Override
	public void updateMyDocument(MyDocument documentDomain) {
		String filename = documentDomain.getName();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.NAME, filename);

		Document document = (Document) session
				.getObject(documentDomain.getId());
		document.updateProperties(properties);

		ContentStream contentStream = createContentStream(
				documentDomain.getName(), documentDomain.getLength(),
				documentDomain.getContentType(), documentDomain.getData());

		document.setContentStream(contentStream, true);
	}

	public void deleteDocumentById(String id) {
		try {
			Document document = (Document) session.getObject(id);
			if (document != null) {
				System.out.println("There was document for the specified ID <nodeRef>. It will be deleted now.");
				document.deleteAllVersions();
				System.out.println("Document is deleted. It's in heaven now, RIP sweet document.");
			}
		} catch (Exception e) {
			System.out.println("There was some exception while finding the document in Alfresco in the specified path. Document could not be found");
			e.printStackTrace();
		} 
	}

	@Override
	public void deleteDocumentByPath(String path) {
		try {
			String alfrescoPath = getAlfrescoPath(path);
			System.out.println("alfrescoPath, the path to search in Alfresco=" + alfrescoPath);
			Document document = (Document) session.getObjectByPath(alfrescoPath);
			if (document != null) {
				System.out.println("There was document found in the specified path. It will be deleted now.");
				document.deleteAllVersions();
				System.out.println("Document is deleted. It's in heaven now, RIP sweet document.");
			}
		} catch (Exception e) {
			System.out.println("There was some exception while finding the document in Alfresco in the specified path. Document could not be found");
		}
		
		
	}

	/*@Override
	public void printAlfrescoDocumentByPath(String path) {
		System.out.println("About to call the session. Will get the Alfresco document now.");
		Document document = (Document) session.getObjectByPath(path);
		if (document != null) {
			document.deleteAllVersions();
		}
		
		System.out.println("after the call=" + document.getName() + "<>"
				+ document.getPaths().size());
	}*/

	@Override
	public List<MyDocument> getAllMyDocuments(String documentId)
			throws IOException, DocumentException {
		List<MyDocument> documents = new ArrayList<MyDocument>();
		Folder folder = getDocumentFolder(documentId);
		if (folder != null) {
			ItemIterable<CmisObject> children = folder.getChildren();

			for (CmisObject object : children) {
				if (object instanceof Document) {
					MyDocument documentDomain = convertAlfrescoDocumentToMyDocument((Document) object);
					documents.add(documentDomain);
				} else {
					System.out.println(object.getName() + " is not a document, skipping it");
				}
			}
		}
		return documents;
	}

	@Override
	public void printAllMyDocumentsFromAlfresco() throws IOException, DocumentException {
		ItemIterable<CmisObject> children = getRootFolder().getChildren();
		for (CmisObject object : children) {
			System.out.println("Name=" + object.getName() + "|ObjectType="
					+ object.getType().getDisplayName() + "|id="
					+ object.getId());
		}
	}

	private Folder getRootFolder() throws DocumentException {
		Folder folder = null;

		if (session == null) {
			throw new DocumentException("Alfresco Session is null exiting...");
		}

		try {
			folder = (Folder) session.getObjectByPath(alfrescoRepoRoot);
			System.out.println("Alfresco repository root folder=" + folder.getName());
		} catch (Exception e) {
			throw new DocumentException(e);
		}
		return folder;
	}

	private Folder getDocumentFolder(String documentId)
			throws DocumentException {
		Folder folder = null;
		if (session == null) {
			throw new DocumentException("Alfresco Session is null exiting...");
		}

		if (documentId == null || documentId.isEmpty()) {
			System.out.println("Please provide valid documentId");
			return folder;
		}

		try {
			CmisObject object = session.getObjectByPath(alfrescoRepoRoot
					+ PATHSEPAERATOR + documentId);
			if (object instanceof Folder) {
				folder = (Folder) session.getObjectByPath(alfrescoRepoRoot
						+ PATHSEPAERATOR + documentId);
			} else {
				System.out
						.println("Folder could not be found for the documentId="
								+ documentId);
				return folder;
			}
		} catch (Exception e) {
			throw new DocumentException(e);
		}
		return folder;
	}

	private MyDocument convertAlfrescoDocumentToMyDocument(Document document)
			throws IOException {
		MyDocument documentDomain = new MyDocument();
		documentDomain.setId(document.getId());
		documentDomain.setName(document.getName());
		documentDomain.setLength(document.getContentStreamLength());
		documentDomain.setContentType(document.getContentStreamMimeType());
		documentDomain.setData(ByteStreams.toByteArray(document
				.getContentStream().getStream()));
		return documentDomain;
	}

	public MyDocument createMyDocument() {
		MyDocument documentDomain = new MyDocument();
		documentDomain.setId("VERWIJDER-0001609/test.pdf");
		documentDomain.setName("Dummy document");
		// documentDomain.setLength(0);
		documentDomain.setContentType("application/pdf");
		documentDomain.setData(new byte[] {});
		return documentDomain;
	}

	private Folder getFolder(Folder parent, String name) {
		try {
			String path = new StringBuilder().append(parent.getPath())
					.append("/").append(name).toString();
			return (Folder) session.getObjectByPath(path);
		} catch (CmisObjectNotFoundException exception) {
			return null;
		}
	}

	private Folder createFolder(Folder parent, String folderType, String name) {

		Folder folder = getFolder(parent, name);
		if (folder == null) {
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, folderType);
			properties.put(PropertyIds.NAME, name);
			folder = parent.createFolder(properties);
		}
		return folder;
	}

	private Folder createFolderStructureForDocument(String documentId,
			String folderType) throws DocumentException {
		Folder folder = null;
		try {
			folder = createFolder(getRootFolder(), folderType, documentId);
		} catch (DocumentException e) {
			throw e;
		}

		return folder;
	}

	private ContentStream createContentStream(String name, long length,
			String contentType, byte[] documentData) {
		InputStream stream = new ByteArrayInputStream(documentData.clone());
		return session.getObjectFactory().createContentStream(name, length,
				contentType, stream);
	}

	@Override
	public Document getAlfrescoDocumentById(String id) throws DocumentException {
		Document document = null;
		try {
			document = (Document) session.getObject(id);
		} catch (Exception e) {
			throw new DocumentException("alfresco Document could not be found",
					e);
		}
		return document;
	}

	@Override
	public Document getAlfrescoDocumentByPath(String path)
			throws DocumentException {
		Document document = null;
		try {
			String alfrescoPath = getAlfrescoPath(path);
			document = (Document) session.getObjectByPath(alfrescoPath);
		} catch (Exception exception) {
			throw new DocumentException("Exception", exception);
		}
		return document;
	}
	
}
