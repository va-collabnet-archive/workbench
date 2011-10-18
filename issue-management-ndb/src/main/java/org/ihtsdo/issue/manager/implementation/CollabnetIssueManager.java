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
package org.ihtsdo.issue.manager.implementation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.IssueAttachmentRef;
import org.ihtsdo.issue.IssueComment;
import org.ihtsdo.issue.IssueDependency;
import org.ihtsdo.issue.collabnet.CollabUtil;
import org.ihtsdo.issue.collabnet.TeamForgeConnection;
import org.ihtsdo.issue.collabnet.TrackerUtil;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.util.MappUtil;

import com.collabnet.ce.soap50.types.SoapFieldValues;
import com.collabnet.ce.soap50.types.SoapFilter;
import com.collabnet.ce.soap50.webservices.cemain.AttachmentSoapRow;
import com.collabnet.ce.soap50.webservices.cemain.TrackerFieldSoapDO;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDependencySoapList;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDependencySoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDetailSoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapDO;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapRow;

/**
 * The Class CollabnetIssueManager.
 */
public class CollabnetIssueManager implements I_IssueManager{

	/** The session id. */
	private String sessionId;
	
	/** The url. */
	private String url;
	
	/** The tracker id. */
	private String trackerId;
	
	/** The repository uu id. */
	private UUID repositoryUUId;

	/* (non-Javadoc)
	 * @see org.ihtsdo.issue.manager.implementation.I_IssueManager#getAllIssues()
	 */
	public List<Issue> getAllIssues() throws Exception {
		TrackerUtil tutil=new TrackerUtil(url, sessionId);

		ArtifactDetailSoapRow[] asrl=tutil.getArtifactDetailList(trackerId, null, null, null, 0, -1, false, true) ;

		List<Issue> issuelist=MappUtil.getIssueListFromArtifactList(asrl);

		Object[] asr=null;
		for (Issue issue:issuelist){
			asr=tutil.getFieldMap(issue.getExternalId());
			if (asr!=null){
				issue.setFieldMapAttId((String)asr[0]);
				issue.setFieldMap((HashMap<String,Object>)asr[1]);
				issue.setRepositoryUUId(repositoryUUId);
			}
		}
		return issuelist;

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.issue.manager.implementation.I_IssueManager#getIssuesForComponentId(java.lang.String)
	 */
	public List<Issue> getIssuesForComponentId(String componentId) throws Exception {

		TrackerUtil tutil=new TrackerUtil(url, sessionId);

		SoapFilter sf=new SoapFilter();
		sf.setName("ComponentId");
		sf.setValue(componentId);
		ArtifactDetailSoapRow[] asrl=tutil.getArtifactDetailList(trackerId, null, new SoapFilter[]{sf}, null, 0, -1, false, true) ;

		List<Issue> issuelist=MappUtil.getIssueListFromArtifactList(asrl);

		Object[] asr=null;
		for (Issue issue:issuelist){
			asr=tutil.getFieldMap(issue.getExternalId());
			if (asr!=null){
				issue.setFieldMapAttId((String)asr[0]);
				issue.setFieldMap((HashMap<String,Object>)asr[1]);
				issue.setRepositoryUUId(repositoryUUId);
			}
		}
		return issuelist;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.issue.manager.implementation.I_IssueManager#getIssuesForCriteria(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.HashMap)
	 */
	public List<Issue> getIssuesForCriteria(String userId,String downloadStatus,String priority,String role,String queueName,String workflowStatus,HashMap<String,Object> map) throws Exception {

		TrackerUtil tutil=new TrackerUtil(url, sessionId);
		int fieldCount=0;
		if (userId != null) {
			if (!userId.equals("")){
				fieldCount++;
			}
		} else userId = "";
		
		if (downloadStatus != null) {
			if (!downloadStatus.equals("")){
				fieldCount++;
			}
		} else downloadStatus = "";
		
		if (priority != null) {
			if (!priority.equals("")){
				fieldCount++;
			}
		} else priority = "";
		
		if (role != null) {
			if (!role.equals("")){
				fieldCount++;
			}
		} else role = "";
		
		if (queueName != null) {
			if (!queueName.equals("")){
				fieldCount++;
			}
		} else queueName = "";
		
		if (workflowStatus != null) {
			if (!workflowStatus.equals("")){
				fieldCount++;
			}
		} else workflowStatus = "";
		
		SoapFilter[] sf=null;

		if (fieldCount>0){
			sf=new SoapFilter[fieldCount];
			fieldCount=0;
		} else {
			throw new Exception("At least one of standard fields are required (userId, status or priority)...");
		}
		
		if (!userId.equals("")){
			sf[fieldCount]=new SoapFilter();
			sf[fieldCount].setName("ExternalUserName");
			sf[fieldCount].setValue(userId);
			fieldCount++;
		}
		if (!downloadStatus.equals("")){
			sf[fieldCount]=new SoapFilter();
			sf[fieldCount].setName("status");
			sf[fieldCount].setValue(downloadStatus);
			fieldCount++;
		}
		if (!priority.equals("")){
			sf[fieldCount]=new SoapFilter();
			sf[fieldCount].setName("priority");
			sf[fieldCount].setValue(priority);
			fieldCount++;
		}
		if (!role.equals("")){
			sf[fieldCount]=new SoapFilter();
			sf[fieldCount].setName("group");
			sf[fieldCount].setValue(role);
			fieldCount++;
		}
		if (!queueName.equals("")){
			sf[fieldCount]=new SoapFilter();
			sf[fieldCount].setName("QueueName");
			sf[fieldCount].setValue(queueName);
			fieldCount++;
		}
		if (!workflowStatus.equals("")){
			sf[fieldCount]=new SoapFilter();
			sf[fieldCount].setName("WorkflowStatus");
			sf[fieldCount].setValue(workflowStatus);
			fieldCount++;
		}

		ArtifactDetailSoapRow[] asrl=tutil.getArtifactDetailList(trackerId, null, sf, null, 0, -1, false, true) ;
		List<Issue> issueList=new ArrayList<Issue>();
		Object[] asr=null;
		if (map==null){
			issueList=MappUtil.getIssueListFromArtifactList(asrl);
			for (Issue issue:issueList){
				asr=tutil.getFieldMap(issue.getExternalId());
				if (asr!=null){
					issue.setFieldMapAttId((String)asr[0]);
					issue.setFieldMap((HashMap<String,Object>)asr[1]);
					issue.setRepositoryUUId(repositoryUUId);
				}
			}
			return issueList;
		}

		HashMap<String,Object> tmpMap;
		Object value;
		boolean pairExists;
		Issue issue;
		for (ArtifactDetailSoapRow detRow:asrl){
			asr=tutil.getFieldMap(detRow.getId());
			if (asr!=null){

				tmpMap=(HashMap<String,Object>)asr[1];

				pairExists=true;
				for (String key: map.keySet()){
					value=map.get(key);
					if (!(tmpMap.containsKey(key) && tmpMap.get(key).toString().equals(value.toString()))){
						pairExists=false;
						break;
					}
				}

				if (pairExists){
					issue=MappUtil.getIssueFromCNArtifactDetailRow(detRow);
					issue.setFieldMapAttId((String)asr[0]);
					issue.setFieldMap((HashMap<String,Object>)asr[1]);
					issue.setRepositoryUUId(repositoryUUId);
					issueList.add(issue);
				}
			}
		}
		return issueList;

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.issue.manager.implementation.I_IssueManager#getIssueData(java.lang.String)
	 */
	public Issue getIssueData(String issueExternalId) throws IOException, ClassNotFoundException{
		Issue issue;

		TrackerUtil tutil=new TrackerUtil(url, sessionId);

		ArtifactSoapDO asd=tutil.getArtifactData(sessionId,issueExternalId);

		issue=MappUtil.getIssueFromArtifactSoapDO(asd);

		issue.setRepositoryUUId(repositoryUUId);
		
		Object[] arrObj=null;
		arrObj=tutil.getFieldMap(issueExternalId);
		if (arrObj!=null){
			issue.setFieldMapAttId((String)arrObj[0]);
			issue.setFieldMap((HashMap<String,Object>)arrObj[1]);
		}
		return issue;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.issue.manager.implementation.I_IssueManager#deleteIssue(java.lang.String)
	 */
	public void deleteIssue(String issueExternalId) throws IOException, ClassNotFoundException{
		
		TrackerUtil tutil=new TrackerUtil(url, sessionId);

		tutil.deleteArtifact(sessionId, issueExternalId);

		return ;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.issue.manager.implementation.I_IssueManager#openRepository(org.ihtsdo.issue.issuerepository.IssueRepository, java.lang.String, java.lang.String)
	 */
	public int openRepository(IssueRepository issueRepository,String connUser,String connPass) throws Exception {

	System.setProperty("javax.net.ssl.trustStore","config/cacerts"); 

		url=issueRepository.getUrl();
		trackerId=issueRepository.getRepositoryId();
		repositoryUUId=issueRepository.getUuid();
		TeamForgeConnection tfconn=new TeamForgeConnection(url);

		sessionId = tfconn.login(connUser,connPass);
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.issue.manager.implementation.I_IssueManager#postNewIssue(org.ihtsdo.issue.Issue)
	 */
	public String postNewIssue(Issue issue) throws Exception {

		TrackerUtil tutil=new TrackerUtil(url, sessionId);

		ArtifactSoapRow asr=MappUtil.getCNArtifactRowFromIssue(issue);

		SoapFieldValues sfv=new SoapFieldValues();
		sfv.setNames(new String[]{"ComponentId","ExternalUserName","ComponentName","QueueName","WorkflowStatus"});
		sfv.setTypes(new String[]{TrackerFieldSoapDO.FIELD_VALUE_TYPE_STRING,TrackerFieldSoapDO.FIELD_VALUE_TYPE_STRING,TrackerFieldSoapDO.FIELD_VALUE_TYPE_STRING,TrackerFieldSoapDO.FIELD_VALUE_TYPE_STRING,TrackerFieldSoapDO.FIELD_VALUE_TYPE_STRING});
		sfv.setValues(new String[]{issue.getComponentId(),issue.getExternalUser(),issue.getComponent(),issue.getQueueName(),issue.getWorkflowStatus()});

//		System.out.println("****************** info ****************");
//		System.out.println(sfv.getNames().toString());
//		System.out.println(sfv.getTypes().toString());
//		System.out.println(sfv.getValues().toString());
//		System.out.println("****************** /info ****************");

		ArtifactSoapDO asd=tutil.CreateArtifact( trackerId,  asr.getTitle() ,asr.getDescription(),
				asr.getArtifactGroup(),asr.getCategory(),asr.getStatus(),
				asr.getCustomer(),asr.getPriority(),asr.getEstimatedHours(),
				asr.getAssignedToUsername(), "" ,sfv,
				"", "", "");

		HashMap<String,Object> map=new HashMap<String,Object>();
		if (issue.getFieldMap()!=null)
			map=issue.getFieldMap();
		else
			map=new HashMap<String,Object>();

		String mapId="";
		// TODO: Commented to prevent "out of heap space"
		//mapId = tutil.sendFieldMap(map);
		//tutil.setArtifactFieldMap(asd.getId(), mapId);

		return asd.getId();

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.issue.manager.implementation.I_IssueManager#setIssueData(org.ihtsdo.issue.Issue)
	 */
	public void setIssueData(Issue issue)throws Exception  {

		TrackerUtil tutil=new TrackerUtil(url, sessionId);


		ArtifactSoapDO asd=MappUtil.getCNArtifactDOFromIssue(issue);

		String comments=issue.getCommentsForUpdate() ;

		if (!comments.trim().equals("") && !issue.getExternalUser().equals("")) {
			comments=MappUtil.USERNAME_INIMARK + issue.getExternalUser() +
					 MappUtil.USERNAME_ENDMARK + comments ;
		}
			

		tutil.setArtifactData( asd,comments);
		
	}

	/**
	 * Open repository.
	 * 
	 * @param repositoryConceptId the repository concept id
	 * @param userName the user name
	 * @param password the password
	 * 
	 * @return the int
	 * 
	 * @throws Exception the exception
	 */
	public int openRepository(int repositoryConceptId, String userName,
			String password) throws Exception {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.issue.manager.implementation.I_IssueManager#getCommentsList(org.ihtsdo.issue.Issue)
	 */
	public List<IssueComment> getCommentsList(Issue issue) throws Exception {
		List<IssueComment> issueCommList=new ArrayList<IssueComment>();
		CollabUtil cout=new CollabUtil(url, sessionId);
		issueCommList=MappUtil.getCommentsIssueListFromSoapList( cout.getCommentsList(issue.getExternalId()));
		return issueCommList;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.issue.manager.implementation.I_IssueManager#setIssueFieldMap(org.ihtsdo.issue.Issue)
	 */
	public void setIssueFieldMap(Issue issue) throws Exception {

		TrackerUtil tutil=new TrackerUtil(url, sessionId);

		if (!issue.getFieldMapAttId().equals(""))
			try {
				tutil.delFieldMap(issue.getExternalId(), issue.getFieldMapAttId());
			} catch (Exception e) {
				if(AceLog.getAppLog().isLoggable(Level.FINE))
					AceLog.getAppLog().info((new StringBuilder()).append("unable to set artifact " ).append(issue.getExternalId()).append(e.getMessage()).toString());  
				e.printStackTrace();
			}

			HashMap<String,Object> map=new HashMap<String,Object>();
			if (issue.getFieldMap()!=null)
				map=issue.getFieldMap();
			else
				map=new HashMap<String,Object>();

			String mapId="";
			// TODO: Commented to prevent "out of heap space"
//			mapId = tutil.sendFieldMap(map);
//
//			tutil.setArtifactFieldMap(issue.getExternalId(), mapId);
//			
//			AttachmentSoapRow asr=tutil.getFieldMapAttachment(issue.getExternalId());
//			issue.setFieldMapAttId(asr.getAttachmentId());

	}

	@Override
	public void addAttachment(Issue issue, File file, String mimeType)
			throws Exception {

		TrackerUtil tutil=new TrackerUtil(url, sessionId);
		
		FileDataSource ds=new FileDataSource(file) ;
		String rawFileId=tutil.sendAttachment(ds);
		

		tutil.setArtifactAttachment(issue.getExternalId(), rawFileId, file.getName(), mimeType);
		
		return;
	}

	@Override
	public void addDependency(Issue originIssue, IssueDependency issueDependency)
			throws Exception {
		TrackerUtil tutil=new TrackerUtil(url, sessionId);
		
		tutil.addDependency(originIssue.getExternalId(), issueDependency.getTargetIssue().getExternalId(),issueDependency.getDescription());
		
	}

	@Override
	public void delAttachment(IssueAttachmentRef issueAttachmentRef)
			throws Exception {

		TrackerUtil tutil=new TrackerUtil(url, sessionId);
		
		String attId=issueAttachmentRef.getAttachmentId();
		if ((attId==null || attId.trim().equals(""))) {
			throw new Exception("The attachment Id is null");
		}

		tutil.delAttachment(issueAttachmentRef.getIssue().getExternalId(), attId);
		
	}

	@Override
	public void delDependency(Issue originIssue, Issue targetIssue)
			throws Exception {
		TrackerUtil tutil=new TrackerUtil(url, sessionId);
		
		tutil.removeArtifactDependency(originIssue.getExternalId(), targetIssue.getExternalId());
		
	}
	
	@Override
	public void delDependency(Issue originIssue,IssueDependency issueDependency)
	throws Exception {
		TrackerUtil tutil=new TrackerUtil(url, sessionId);

		tutil.removeArtifactDependency(originIssue.getExternalId(), issueDependency.getTargetIssue().getExternalId());

	}

	@Override
	public File getAttachmentFile(IssueAttachmentRef issueAttachmentRef,File toFolder)
	throws Exception {
		if (toFolder==null || !toFolder.isDirectory())
			throw new Exception("The folder is not a directory");

		TrackerUtil tutil=new TrackerUtil(url, sessionId);

		DataHandler dataHandler=tutil.getAttachmentFile(issueAttachmentRef.getIssue().getExternalId(), issueAttachmentRef.getRawFileId());


		File file = new File(toFolder.getAbsolutePath() + File.separator + issueAttachmentRef.getFileName());
		FileOutputStream outputStream = new FileOutputStream(file);
		dataHandler.writeTo(outputStream);

		return file;
	}

	@Override
	public List<IssueAttachmentRef> getAttachmentList(Issue issue)
			throws Exception {

		TrackerUtil tutil=new TrackerUtil(url, sessionId);
		AttachmentSoapRow[] asrs=tutil.getAttachmentList(issue.getExternalId());
		List<IssueAttachmentRef> iarList= MappUtil.getIssueAttachRefListFromAttachRowList(asrs);
		for (IssueAttachmentRef iar: iarList){
			iar.setIssue(issue);
		}
		return iarList;

	}

	@Override
	public List<IssueDependency> getParentDependencyList(Issue issue)
			throws Exception {
		
		
		TrackerUtil tutil=new TrackerUtil(url, sessionId);
		
		ArtifactDependencySoapRow[] adsl = tutil.getParentDependencyList(issue.getExternalId());
		
		List<IssueDependency> issueDepList=new ArrayList<IssueDependency>();
		for (ArtifactDependencySoapRow adsr:adsl){
			
			Issue issueTarget=getIssueData(adsr.getTargetId());
			
			IssueDependency issueDep=new IssueDependency();
			issueDep.setTargetIssue(issueTarget);
			issueDep.setDescription(adsr.getDescription());
			
			issueDepList.add(issueDep);
		}
		return issueDepList;
	}

	@Override
	public List<IssueDependency> getChildDependencyList(Issue issue)
			throws Exception {
		TrackerUtil tutil=new TrackerUtil(url, sessionId);
		
		ArtifactDependencySoapRow[] adsl = tutil.getChildDependencyList(issue.getExternalId());
		
		List<IssueDependency> issueDepList=new ArrayList<IssueDependency>();
		for (ArtifactDependencySoapRow adsr:adsl){
			
			Issue issueTarget=getIssueData(adsr.getTargetId());
			
			IssueDependency issueDep=new IssueDependency();
			issueDep.setTargetIssue(issueTarget);
			issueDep.setDescription(adsr.getDescription());
			
			issueDepList.add(issueDep);
		}
		return issueDepList;
	}
}
