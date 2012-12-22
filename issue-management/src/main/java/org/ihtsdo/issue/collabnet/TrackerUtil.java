/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.issue.collabnet;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.dwfa.ace.log.AceLog;

import com.collabnet.ce.soap50.types.SoapFieldValues;
import com.collabnet.ce.soap50.types.SoapFilter;
import com.collabnet.ce.soap50.types.SoapSortKey;
import com.collabnet.ce.soap50.webservices.ClientSoapStubFactory;
import com.collabnet.ce.soap50.webservices.cemain.AttachmentSoapList;
import com.collabnet.ce.soap50.webservices.cemain.AttachmentSoapRow;
import com.collabnet.ce.soap50.webservices.cemain.ICollabNetSoap;
import com.collabnet.ce.soap50.webservices.filestorage.IFileStorageAppSoap;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDependencySoapList;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDependencySoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDetailSoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapDO;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapList;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ITrackerAppSoap;
import com.collabnet.ce.soap50.webservices.tracker.TrackerSoapDO;


/**
 * This utility class operates on Trackers and the elements within them
 * (artifacts)
 * <p>.
 */
public class TrackerUtil
{

	/* the TeamForge tracker interface */
	/** The m_tracker soap. */
	private ITrackerAppSoap m_trackerSoap;

	/* the session id returned from a previous call to login() */
	/** The m_session id. */
	private String m_sessionId;


	/** The MA p_ filename. */
	private String MAP_FILENAME="FieldMap.bin";

	/** The m_server url. */
	private String m_serverUrl;



	/**
	 * a simple constructor built around the URL of the TeamForge server
	 * <p>.
	 *
	 * @param serverUrl The fully qualified URL of the TeamForge server
	 * instance
	 * @param sessionId A session identifier returned from a prior call
	 * to login()
	 */
	public TrackerUtil(String serverUrl, String sessionId)
	{

		m_sessionId = sessionId;
		m_serverUrl=serverUrl;

		m_trackerSoap = (ITrackerAppSoap) ClientSoapStubFactory.getSoapStub(
				ITrackerAppSoap.class, serverUrl);
	}


	/**
	 * sets the priority data element associated with the soap row passed
	 * in on the command line.
	 * <p>
	 *
	 * @param asd the asd
	 * @param comment the comment
	 * @throws RemoteException if anything goes wrong within the TeamForge
	 * calls, wrap a remote exception around it and
	 * re-throw
	 */
	public void setArtifactData(ArtifactSoapDO asd,String comment)
	throws RemoteException
	{
		try
		{
			ArtifactSoapDO asdtmp=
				m_trackerSoap.getArtifactData(m_sessionId, asd.getId());
			setActiveASD(asdtmp,asd);
			m_trackerSoap.setArtifactData(m_sessionId, asdtmp, comment,
					null, null, null);
		}
		catch (RemoteException e)
		{
			if(AceLog.getAppLog().isLoggable(Level.FINE))
				AceLog.getAppLog().info((new StringBuilder()).append("unable to set artifact " ).append(asd.getId()).append(e.getMessage()).toString());     
			throw new RemoteException("unable to set artifact " + asd.getId(), e);         
		}
	}

	/**
	 * Sets the active asd.
	 *
	 * @param asdAct the asd act
	 * @param asdCust the asd cust
	 */
	private void setActiveASD(ArtifactSoapDO asdAct, ArtifactSoapDO asdCust){

		asdAct.setCategory(asdCust.getCategory());
		asdAct.setDescription(asdCust.getDescription());
		asdAct.setFlexFields(asdCust.getFlexFields());
		asdAct.setPriority(asdCust.getPriority());
		asdAct.setStatus(asdCust.getStatus());
		asdAct.setTitle(asdCust.getTitle());
		asdAct.setGroup(asdCust.getGroup());
	}

	/**
	 * Gets the artifact detail list.
	 *
	 * @param trackerId the tracker id
	 * @param selectedColumns the selected columns
	 * @param filters the filters
	 * @param sortKeys the sort keys
	 * @param startIndex the start index
	 * @param maxRows the max rows
	 * @param exceptionIfExpiredCache the exception if expired cache
	 * @param forceNewQuery the force new query
	 * @return the artifact detail list
	 * @throws RemoteException the remote exception
	 */
	public ArtifactDetailSoapRow[] getArtifactDetailList(java.lang.String trackerId,
			java.lang.String[] selectedColumns,
			SoapFilter[] filters,
			SoapSortKey[] sortKeys,
			int startIndex,
			int maxRows,
			boolean exceptionIfExpiredCache,
			boolean forceNewQuery)
	throws java.rmi.RemoteException{
		ArtifactDetailSoapRow[] adrl=m_trackerSoap.getArtifactDetailList(m_sessionId,
				trackerId,
				selectedColumns,
				filters,
				sortKeys,
				startIndex,
				maxRows,
				exceptionIfExpiredCache,
				forceNewQuery).getDataRows();

		return adrl;
	}

	/**
	 * Find artifacts.
	 *
	 * @param queryString the query string
	 * @param projectId the project id
	 * @param searchAttachments the search attachments
	 * @return the artifact soap row[]
	 * @throws RemoteException the remote exception
	 */
	public ArtifactSoapRow[] findArtifacts(
			java.lang.String queryString,
			java.lang.String projectId,
			boolean searchAttachments)
	throws java.rmi.RemoteException{

		ArtifactSoapRow[] asl=  m_trackerSoap.findArtifacts( m_sessionId,
				queryString,
				projectId,
				searchAttachments).getDataRows();

		return asl;
	}

	/**
	 * Send field map.
	 *
	 * @param map the map
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String sendFieldMap (HashMap map) throws IOException{
		DataHandler dh = new DataHandler(new org.ihtsdo.issue.util.JODataSource(map));
		IFileStorageAppSoap fileStorage = (IFileStorageAppSoap) ClientSoapStubFactory.getSoapStub(IFileStorageAppSoap.class,m_serverUrl);
		String ObjId=fileStorage.uploadFile(m_sessionId, dh);
		return ObjId; 
	}

	/**
	 * Sets the artifact field map.
	 *
	 * @param artifactId the artifact id
	 * @param mapFileId the map file id
	 * @throws RemoteException the remote exception
	 */
	public void setArtifactFieldMap(String artifactId,String mapFileId )
	throws RemoteException
	{
		try
		{
			/*
             re-retrieve the artifact.  This is good practice to minimize
             errors resulting from artifacts that have been modified while
             we hold them inside this jvm
			 */
			 ArtifactSoapDO ado =
				 m_trackerSoap.getArtifactData(m_sessionId, artifactId);

			m_trackerSoap.setArtifactData( m_sessionId, ado, "field map setting",
					MAP_FILENAME, "application/octet-stream", mapFileId);
		}
		catch (RemoteException e)
		{
			if(AceLog.getAppLog().isLoggable(Level.FINE))
				AceLog.getAppLog().info((new StringBuilder()).append("unable to set artifact field map" ).append(artifactId).append(e.getMessage()).toString());     
			throw new RemoteException("unable to set artifact field map" + artifactId, e); 
		}
	}

	// Get the field map of attachments of the artifact
	/**
	 * Gets the field map.
	 *
	 * @param artifactId the artifact id
	 * @return the field map
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public Object[] getFieldMap (String artifactId) throws IOException, ClassNotFoundException{
		Object[] retObj = null;
		HashMap <String,Object> map=new HashMap<String,Object>();
		// Now, retrieve the attachments
//		IFileStorageAppSoap fileStorage = (IFileStorageAppSoap) ClientSoapStubFactory.getSoapStub(IFileStorageAppSoap.class,m_serverUrl);

//		AttachmentSoapRow thisAttachment = getFieldMapAttachment(artifactId);
//		if (thisAttachment!=null){
//			retObj = new Object[2];
//			DataHandler dh = fileStorage.downloadFileDirect(m_sessionId, artifactId, thisAttachment.getRawFileId());
//			// Read the contents
//			ObjectInputStream ois=new ObjectInputStream( dh.getInputStream());
//			map=(HashMap<String,Object>)ois.readObject();
//			retObj[0]=thisAttachment.getAttachmentId();
//			retObj[1]=map;
//		}
		return retObj;
	}


	/**
	 * Gets the field map attachment.
	 *
	 * @param artifactId the artifact id
	 * @return the field map attachment
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public AttachmentSoapRow getFieldMapAttachment (String artifactId) throws IOException, ClassNotFoundException{
		ICollabNetSoap sfSoap = (ICollabNetSoap) ClientSoapStubFactory.getSoapStub(ICollabNetSoap.class, m_serverUrl);
		AttachmentSoapList attachmentSoapList = sfSoap.listAttachments(m_sessionId, artifactId);

		// Now, retrieve the field map attachment 
		AttachmentSoapRow[] attachmentRows = attachmentSoapList.getDataRows();
		AttachmentSoapRow Att=null;
		for(int i=0; i < attachmentRows.length;i++) {
			Att= attachmentRows[i];
			if (Att.getFileName().equals(MAP_FILENAME)){
				break; 
			}
		}
		return Att;
	}

	/**
	 * Del field map.
	 *
	 * @param artifactId the artifact id
	 * @param fieldMapId the field map id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public void delFieldMap (String artifactId,String fieldMapId) throws IOException, ClassNotFoundException{
		ICollabNetSoap sfSoap = (ICollabNetSoap) ClientSoapStubFactory.getSoapStub(ICollabNetSoap.class, m_serverUrl);
		sfSoap.deleteAttachment(m_sessionId, artifactId, fieldMapId);
		return ;
	}

	/**
	 * returns a type-safe list built from the artifacts within the trackerId,
	 * given as an argument
	 * <p>.
	 *
	 * @param trackerId the containing tracker the contents of which we're
	 * going to read and return
	 * <p>
	 * @return a list containing all artifacts within the trackerId given on
	 * the command line
	 * @throws RemoteException if any errors occur in a TeamForge API, we wrap
	 * this in an exception and throw it on up the call
	 * stack
	 * <p>
	 */

	public List<ArtifactSoapRow> getArtifactList(String trackerId)
	throws RemoteException
	{
		return getArtifactList( trackerId,null);
	}

	/**
	 * Gets the artifact list.
	 *
	 * @param trackerId the tracker id
	 * @param soapFilters the soap filters
	 * @return the artifact list
	 * @throws RemoteException the remote exception
	 */
	public List<ArtifactSoapRow> getArtifactList(String trackerId,SoapFilter[] soapFilters)
	throws RemoteException
	{
		ArtifactSoapList asl;
		try
		{
			/* return a list of artifacts from the tracker argument */
			asl = m_trackerSoap.getArtifactList(m_sessionId, trackerId, soapFilters);
		}
		catch (RemoteException e)
		{
			if(AceLog.getAppLog().isLoggable(Level.FINE))
				AceLog.getAppLog().info((new StringBuilder()).append("unable to retrieve artifacts " ).append(e.getMessage()).toString());     

			throw new RemoteException("unable to retrieve artifacts", e);
		}
		if(AceLog.getAppLog().isLoggable(Level.FINE))
			AceLog.getAppLog().info((new StringBuilder()).append("successfully retrieved artifacts " ).toString());     

		ArtifactSoapRow[] artfRows = asl.getDataRows();

		/* put our artifacts in a type-safe list */
		List<ArtifactSoapRow> artifacts = new ArrayList<ArtifactSoapRow>();      
		artifacts.addAll(Arrays.asList(artfRows));

		return (artifacts);
	}
	
	/**
	 * Creates the tracker.
	 *
	 * @param projectId the project id
	 * @param trackerName the tracker name
	 * @param trackerTitle the tracker title
	 * @param trackerDescription the tracker description
	 * @return the tracker soap do
	 * @throws RemoteException the remote exception
	 */
	public TrackerSoapDO CreateTracker(String projectId,String trackerName, String trackerTitle,String trackerDescription) throws RemoteException{
		TrackerSoapDO tsd;
		try {
			tsd = m_trackerSoap.createTracker(m_sessionId, projectId, trackerName, trackerTitle, trackerDescription);
		} catch (RemoteException e) {
			if(AceLog.getAppLog().isLoggable(Level.FINE))
				AceLog.getAppLog().info((new StringBuilder()).append("unable to create tracker " ).append(e.getMessage()).toString());     
			throw new RemoteException("unable to create tracker", e);
		}
		return tsd;
	}

	/**
	 * Creates the artifact.
	 *
	 * @param trackerId the tracker id
	 * @param artTitle the art title
	 * @param artDescription the art description
	 * @param group the group
	 * @param category the category
	 * @param status the status
	 * @param customer the customer
	 * @param priority the priority
	 * @param estimatedHours the estimated hours
	 * @param assignedUsername the assigned username
	 * @param releasedId the released id
	 * @param flexFields the flex fields
	 * @param attachmentFileName the attachment file name
	 * @param attachmentMimeType the attachment mime type
	 * @param attachmentFileId the attachment file id
	 * @return the artifact soap do
	 * @throws RemoteException the remote exception
	 */
	public ArtifactSoapDO CreateArtifact(String trackerId, String artTitle,String artDescription,String group,String category,String status,
			String customer,int priority,int estimatedHours,String assignedUsername, String releasedId,SoapFieldValues flexFields,
			String attachmentFileName, String attachmentMimeType, String attachmentFileId) throws RemoteException{
		ArtifactSoapDO asd;
		try {
			asd = m_trackerSoap.createArtifact(m_sessionId,trackerId, artTitle, artDescription, group, category, status,
					customer, priority, estimatedHours, assignedUsername,  releasedId, flexFields,
					attachmentFileName,  attachmentMimeType,  attachmentFileId);
		} catch (RemoteException e) {
			if(AceLog.getAppLog().isLoggable(Level.FINE))
				AceLog.getAppLog().info((new StringBuilder()).append("unable to create artifact " ).append(e.getMessage()).toString());     

			throw new RemoteException("unable to create artifact", e);
		}
		return asd;
	}


	/**
	 * Gets the artifact data.
	 *
	 * @param sessionId the session id
	 * @param issueExternalId the issue external id
	 * @return the artifact data
	 * @throws RemoteException the remote exception
	 */
	public ArtifactSoapDO getArtifactData(String sessionId, String issueExternalId) throws RemoteException {

		ArtifactSoapDO asd=m_trackerSoap.getArtifactData(sessionId, issueExternalId);
		return asd;
	}


	/**
	 * Delete artifact.
	 *
	 * @param sessionId the session id
	 * @param issueExternalId the issue external id
	 * @throws RemoteException the remote exception
	 */
	public void deleteArtifact(String sessionId, String issueExternalId) throws RemoteException {

		m_trackerSoap.deleteArtifact(sessionId, issueExternalId);
		return ;
	}

	/**
	 * Gets the attachment list.
	 *
	 * @param artifactId the artifact id
	 * @return the attachment list
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public AttachmentSoapRow[] getAttachmentList(String artifactId) throws IOException, ClassNotFoundException{

		ICollabNetSoap sfSoap = (ICollabNetSoap) ClientSoapStubFactory.getSoapStub(ICollabNetSoap.class, m_serverUrl);
		AttachmentSoapList attachmentSoapList = sfSoap.listAttachments(m_sessionId, artifactId);

		AttachmentSoapRow[] attachmentRows = attachmentSoapList.getDataRows();
		return attachmentRows;
	}

	/**
	 * Sets the artifact attachment.
	 *
	 * @param artifactId the artifact id
	 * @param attachmentId the attachment id
	 * @param fileName the file name
	 * @param mimeType the mime type
	 * @throws RemoteException the remote exception
	 */
	public void setArtifactAttachment(String artifactId,String attachmentId ,String fileName, String mimeType)
	throws RemoteException
	{
		try
		{
			/*
             re-retrieve the artifact.  This is good practice to minimize
             errors resulting from artifacts that have been modified while
             we hold them inside this jvm
			 */
			ArtifactSoapDO ado =
				m_trackerSoap.getArtifactData(m_sessionId, artifactId);

			m_trackerSoap.setArtifactData( m_sessionId, ado, "attachment setting",
					fileName, mimeType, attachmentId);
		}
		catch (RemoteException e)
		{
			if(AceLog.getAppLog().isLoggable(Level.FINE))
				AceLog.getAppLog().info((new StringBuilder()).append("unable to set artifact attachment" ).append(artifactId).append(e.getMessage()).toString());     
			throw new RemoteException("unable to set artifact attachment" + artifactId, e); 
		}
	}

	/**
	 * Send attachment.
	 *
	 * @param ds the ds
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String sendAttachment (DataSource ds) throws IOException{
		DataHandler dh = new DataHandler(ds);
		IFileStorageAppSoap fileStorage = (IFileStorageAppSoap) ClientSoapStubFactory.getSoapStub(IFileStorageAppSoap.class,m_serverUrl);
		String ObjId=fileStorage.uploadFile(m_sessionId, dh);
		return ObjId; 
	}
	
	/**
	 * Adds the dependency.
	 *
	 * @param originArtifactId the origin artifact id
	 * @param targetArtifactId the target artifact id
	 * @param description the description
	 * @throws RemoteException the remote exception
	 */
	public void addDependency(String originArtifactId, String targetArtifactId, String description) throws RemoteException{
		
		String desc=(description==null)? "":description;
		m_trackerSoap.createArtifactDependency(m_sessionId, originArtifactId, targetArtifactId, desc);
	}

	/**
	 * Del attachment.
	 *
	 * @param artifactId the artifact id
	 * @param attachmentId the attachment id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public void delAttachment (String artifactId,String attachmentId) throws IOException, ClassNotFoundException{
		ICollabNetSoap sfSoap = (ICollabNetSoap) ClientSoapStubFactory.getSoapStub(ICollabNetSoap.class, m_serverUrl);
		sfSoap.deleteAttachment(m_sessionId, artifactId, attachmentId);
		return ;
	}


	/**
	 * Removes the artifact dependency.
	 *
	 * @param originArtifactId the origin artifact id
	 * @param targetArtifactId the target artifact id
	 * @throws RemoteException the remote exception
	 */
	public void removeArtifactDependency(String originArtifactId, String targetArtifactId) throws RemoteException {
		m_trackerSoap.removeArtifactDependency(m_sessionId, originArtifactId, targetArtifactId);
		
	}


	/**
	 * Gets the attachment file.
	 *
	 * @param artifactId the artifact id
	 * @param rawFileId the raw file id
	 * @return the attachment file
	 * @throws RemoteException the remote exception
	 */
	public DataHandler getAttachmentFile(String artifactId, String rawFileId) throws RemoteException {
		IFileStorageAppSoap fileStorage = (IFileStorageAppSoap) ClientSoapStubFactory.getSoapStub(IFileStorageAppSoap.class,m_serverUrl);

			DataHandler dh = fileStorage.downloadFileDirect(m_sessionId, artifactId, rawFileId);

			return dh;
			
	}


	/**
	 * Gets the parent dependency list.
	 *
	 * @param artifactId the artifact id
	 * @return the parent dependency list
	 * @throws RemoteException the remote exception
	 */
	public ArtifactDependencySoapRow[] getParentDependencyList(String artifactId) throws RemoteException {
		ArtifactDependencySoapList adsl = m_trackerSoap.getParentDependencyList(m_sessionId, artifactId);
		return adsl.getDataRows();
		
	}


	/**
	 * Gets the child dependency list.
	 *
	 * @param artifactId the artifact id
	 * @return the child dependency list
	 * @throws RemoteException the remote exception
	 */
	public ArtifactDependencySoapRow[] getChildDependencyList(String artifactId) throws RemoteException {
		ArtifactDependencySoapList adsl = m_trackerSoap.getChildDependencyList(m_sessionId, artifactId);
		return adsl.getDataRows();
	}
}