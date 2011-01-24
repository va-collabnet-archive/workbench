/**
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
package org.ihtsdo.project.issue.collabnet;


import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;

import com.collabnet.ce.soap50.types.SoapFieldValues;
import com.collabnet.ce.soap50.types.SoapFilter;
import com.collabnet.ce.soap50.types.SoapSortKey;
import com.collabnet.ce.soap50.webservices.ClientSoapStubFactory;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDetailSoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ITrackerAppSoap;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapList;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapDO;
import com.collabnet.ce.soap50.webservices.tracker.TrackerSoapDO;


/**
 * The Class TrackerUtil.
 */
public class TrackerUtil
{
   
   /* the TeamForge tracker interface */
   /** The m_tracker soap. */
   private ITrackerAppSoap m_trackerSoap;

   /* the session id returned from a previous call to login() */
   /** The m_session id. */
   private String m_sessionId;



   /**
    * Instantiates a new tracker util.
    * 
    * @param serverUrl the server url
    * @param sessionId the session id
    */
   public TrackerUtil(String serverUrl, String sessionId)
   {

      m_sessionId = sessionId;

      m_trackerSoap = (ITrackerAppSoap) ClientSoapStubFactory.getSoapStub(
                                          ITrackerAppSoap.class, serverUrl);
   }


   /**
    * Sets the artifact data.
    * 
    * @param asd the asd
    * @param comment the comment
    * 
    * @throws RemoteException the remote exception
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
    * 
    * @return the artifact detail list
    * 
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
    * 
    * @return the artifact soap row[]
    * 
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
    * Gets the artifact list.
    * 
    * @param trackerId the tracker id
    * 
    * @return the artifact list
    * 
    * @throws RemoteException the remote exception
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
    * 
    * @return the artifact list
    * 
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
    * 
    * @return the tracker soap do
    * 
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
    * 
    * @return the artifact soap do
    * 
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

}