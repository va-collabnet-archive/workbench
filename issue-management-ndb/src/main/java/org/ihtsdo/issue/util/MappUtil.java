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
package org.ihtsdo.issue.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.IssueAttachmentRef;
import org.ihtsdo.issue.IssueComment;

import com.collabnet.ce.soap50.types.SoapFieldValues;
import com.collabnet.ce.soap50.webservices.cemain.AttachmentSoapRow;
import com.collabnet.ce.soap50.webservices.cemain.CommentSoapList;
import com.collabnet.ce.soap50.webservices.cemain.CommentSoapRow;
import com.collabnet.ce.soap50.webservices.cemain.TrackerFieldSoapDO;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDetailSoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapDO;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapRow;

/**
 * The Class MappUtil.
 */
public class MappUtil {

	//	public static Issue getIssueFromCNArtifactRow_(ArtifactSoapRow artifactSoapRow){
	//		Issue iss=new Issue();
	//		iss.setCategory(artifactSoapRow.getCategory());
	//		iss.setComponent(artifactSoapRow.getCustomer());
	//		iss.setComponentId(artifactSoapRow.g)
	//		return iss;
	//	}
	/** The sdf. */
	private static SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yyyy HH:mm");

	/** The Constant USERNAME_INIMARK. */
	public static final String USERNAME_INIMARK= "-";

	/** The Constant USERNAME_ENDMARK. */
	public static final String USERNAME_ENDMARK= "-:";

	/** The Constant LENINIMARK. */
	public static final int LENINIMARK=1;

	/** The Constant LENENDMARK. */
	public static final int LENENDMARK=2;

	/**
	 * Gets the issue from cn artifact detail row.
	 * 
	 * @param artifactDetailSoapRow the artifact detail soap row
	 * 
	 * @return the issue from cn artifact detail row
	 */
	public static Issue getIssueFromCNArtifactDetailRow(ArtifactDetailSoapRow artifactDetailSoapRow){
		Issue iss=new Issue();
		iss.setCategory(artifactDetailSoapRow.getCategory());
		iss.setComponent(artifactDetailSoapRow.getCustomer());

		SoapFieldValues sfvl=artifactDetailSoapRow.getFlexFields();
		String[] fieldNames=(String[])sfvl.getNames();
		List<String> fieldValues=new ArrayList<String>();
		Object[] objL=sfvl.getValues();
		int i=0;
		for (Object o:objL){
			if (o==null)
			{
				fieldValues.add("");
			}
			else{
				fieldValues.add(o.toString());
			}
			i++;
		}
		for ( i=0;i< fieldValues.size();i++){
			if (fieldNames[i].equalsIgnoreCase("ComponentId")){
				iss.setComponentId(fieldValues.get(i));
			}
			if (fieldNames[i].equalsIgnoreCase("ComponentName")){
				iss.setComponent(fieldValues.get(i));
			}
			if (fieldNames[i].equalsIgnoreCase("ExternalUserName")){
				iss.setExternalUser(fieldValues.get(i));
			}
			if (fieldNames[i].equalsIgnoreCase("QueueName")){
				iss.setQueueName(fieldValues.get(i));
			}
			if (fieldNames[i].equalsIgnoreCase("WorkflowStatus")){
				iss.setWorkflowStatus(fieldValues.get(i));
			}
		}
		iss.setDescription(artifactDetailSoapRow.getDescription());
		iss.setExternalId(artifactDetailSoapRow.getId());
		iss.setPriority(String.valueOf(artifactDetailSoapRow.getPriority()));
		iss.setProjectId(artifactDetailSoapRow.getProjectId());
		iss.setDownloadStatus(artifactDetailSoapRow.getStatus());
		iss.setTitle(artifactDetailSoapRow.getTitle());
		iss.setUser(artifactDetailSoapRow.getAssignedToUsername());
		iss.setRole(artifactDetailSoapRow.getArtifactGroup());
		iss.setLastModifiedDate(artifactDetailSoapRow.getLastModifiedDate().getTime());

		return iss;
	}

	/**
	 * Gets the cN artifact row from issue.
	 * 
	 * @param issue the issue
	 * 
	 * @return the cN artifact row from issue
	 */
	public static ArtifactSoapRow getCNArtifactRowFromIssue(Issue issue){
		ArtifactSoapRow asr=new ArtifactSoapRow();
		asr.setCategory(issue.getCategory());
		asr.setCustomer("");
		asr.setDescription(issue.getDescription());
		asr.setId(issue.getExternalId());
		asr.setPriority(Integer.parseInt(issue.getPriority()));
		asr.setProjectId(issue.getProjectId());
		asr.setStatus(issue.getDownloadStatus());
		asr.setArtifactGroup(issue.getRole());
		asr.setSubmittedByUsername(issue.getUser());
		asr.setTitle(issue.getTitle());
		return asr;
	}

	/**
	 * Gets the cN artifact do from issue.
	 * 
	 * @param issue the issue
	 * 
	 * @return the cN artifact do from issue
	 */
	public static ArtifactSoapDO getCNArtifactDOFromIssue(Issue issue){
		ArtifactSoapDO asd=new ArtifactSoapDO();

		asd.setCategory(issue.getCategory());
		asd.setCustomer("");
		asd.setDescription(issue.getDescription());
		asd.setId(issue.getExternalId());
		asd.setPriority(Integer.parseInt(issue.getPriority()));
		asd.setStatus(issue.getDownloadStatus());
		asd.setGroup(issue.getRole());
		asd.setLastModifiedBy(issue.getUser());
		asd.setTitle(issue.getTitle());
		SoapFieldValues sfv=new SoapFieldValues();
		sfv.setNames(new String[]{"ComponentId","ExternalUserName","ComponentName","QueueName","WorkflowStatus"});
		sfv.setTypes(new String[]{TrackerFieldSoapDO.FIELD_VALUE_TYPE_STRING,TrackerFieldSoapDO.FIELD_VALUE_TYPE_STRING,TrackerFieldSoapDO.FIELD_VALUE_TYPE_STRING,TrackerFieldSoapDO.FIELD_VALUE_TYPE_STRING,TrackerFieldSoapDO.FIELD_VALUE_TYPE_STRING});
		sfv.setValues(new String[]{issue.getComponentId(),issue.getExternalUser(),issue.getComponent(),issue.getQueueName(),issue.getWorkflowStatus()});

		//		System.out.println("****************** info ****************");
		//		System.out.println(sfv.getNames().toString());
		//		System.out.println(sfv.getTypes().toString());
		//		System.out.println(sfv.getValues().toString());
		//		System.out.println("****************** /info ****************");

		asd.setFlexFields(sfv);

		return asd;
	}

	/**
	 * Gets the issue list from artifact list.
	 * 
	 * @param artifactDetailSoapRowList the artifact detail soap row list
	 * 
	 * @return the issue list from artifact list
	 */
	public static  List<Issue> getIssueListFromArtifactList(ArtifactDetailSoapRow[] artifactDetailSoapRowList){
		List<Issue> issueList=new ArrayList<Issue>();
		for(ArtifactDetailSoapRow asr:artifactDetailSoapRowList ){
			issueList.add(MappUtil.getIssueFromCNArtifactDetailRow(asr));
		}
		return issueList;
	}

	/**
	 * Gets the comments issue list from soap list.
	 * 
	 * @param commentSoapList the comment soap list
	 * 
	 * @return the comments issue list from soap list
	 */
	public static List<IssueComment> getCommentsIssueListFromSoapList(CommentSoapList commentSoapList) {
		List<IssueComment> icl=new ArrayList<IssueComment>();
		CommentSoapRow[] csrl= commentSoapList.getDataRows();
		for (CommentSoapRow csr:csrl ){
			icl.add(MappUtil.getIssueCommentFromCommSoapRow(csr));
		}
		return icl;
	}

	/**
	 * Gets the issue comment from comm soap row.
	 * 
	 * @param csr the csr
	 * 
	 * @return the issue comment from comm soap row
	 */
	public static IssueComment getIssueCommentFromCommSoapRow(CommentSoapRow csr) {
		IssueComment issueComm=new IssueComment();
		issueComm.setCommentDate(sdf.format(csr.getDateCreated()));
		issueComm.setComment(getCommentFromCommentString(csr.getDescription()));
		issueComm.setUser(getUserFromCommentString(csr.getDescription()));
		if (issueComm.getUser()==null || issueComm.getUser().equals(""))
			issueComm.setUser(csr.getCreatedBy());
		return issueComm;
	}

	/**
	 * Gets the user from comment string.
	 * 
	 * @param comment the comment
	 * 
	 * @return the user from comment string
	 */
	public static String getUserFromCommentString(String comment){
		String tmp="";
		int ini=comment.indexOf(USERNAME_INIMARK);
		if (ini==0){
			int end=comment.indexOf(USERNAME_ENDMARK);
			if (end>ini){
				tmp=comment.substring(LENINIMARK,end);
			}
		}
		if (tmp==null) tmp="";
		return tmp;
	}

	/**
	 * Gets the comment from comment string.
	 * 
	 * @param comment the comment
	 * 
	 * @return the comment from comment string
	 */
	public static String getCommentFromCommentString(String comment){
		String tmp=comment;
		int ini=tmp.indexOf(USERNAME_INIMARK);
		if (ini==0){
			int end=tmp.indexOf(USERNAME_ENDMARK);
			if (end>ini){
				tmp=comment.substring(end + LENENDMARK);
			}
		}
		if (tmp==null) tmp="";
		return tmp;
	}

	/**
	 * Gets the issue from artifact soap do.
	 * 
	 * @param asd the asd
	 * 
	 * @return the issue from artifact soap do
	 */
	public static Issue getIssueFromArtifactSoapDO(ArtifactSoapDO asd) {
		Issue issue=new Issue();

		issue.setCategory(asd.getCategory());
		issue.setDescription(asd.getDescription());
		issue.setExternalId(asd.getId());
		issue.setPriority(String.valueOf( asd.getPriority()));
		issue.setDownloadStatus(asd.getStatus());
		issue.setUser(asd.getAssignedTo());
		issue.setTitle(asd.getTitle());
		issue.setRole(asd.getGroup());
		issue.setLastModifiedDate(asd.getLastModifiedDate().getTime());

		SoapFieldValues sfvl=asd.getFlexFields();
		String[] fieldNames=(String[])sfvl.getNames();

		List<String> fieldValues=new ArrayList<String>();
		Object[] objL=sfvl.getValues();
		int i=0;
		for (Object o:objL){
			if (o==null)
			{
				fieldValues.add("");
			}
			else{
				fieldValues.add(o.toString());
			}
			i++;
		}
		for ( i=0;i< fieldValues.size();i++){
			if (fieldNames[i].equalsIgnoreCase("ComponentId")){
				issue.setComponentId(fieldValues.get(i));
			}
			if (fieldNames[i].equalsIgnoreCase("ComponentName")){
				issue.setComponent(fieldValues.get(i));
			}
			if (fieldNames[i].equalsIgnoreCase("ExternalUserName")){
				issue.setExternalUser(fieldValues.get(i));
			}
			if (fieldNames[i].equalsIgnoreCase("QueueName")){
				issue.setQueueName(fieldValues.get(i));
			}
			if (fieldNames[i].equalsIgnoreCase("WorkflowStatus")){
				issue.setWorkflowStatus(fieldValues.get(i));
			}
		}

		return issue;
	}

	public static List<IssueAttachmentRef> getIssueAttachRefListFromAttachRowList(
			AttachmentSoapRow[] asrs) {
		List<IssueAttachmentRef> issueRefList=new ArrayList<IssueAttachmentRef>();
		for(AttachmentSoapRow asr:asrs ){
			issueRefList.add(MappUtil.getIssueAttachRefFromAttachRow(asr));
		}
		return issueRefList;
	}

	private static IssueAttachmentRef getIssueAttachRefFromAttachRow(
			AttachmentSoapRow asr) {
		IssueAttachmentRef iar=new IssueAttachmentRef();
		iar.setAttachmentId(asr.getAttachmentId());
		iar.setFileName(asr.getFileName());
		iar.setFileSize(asr.getFileSize());
		iar.setRawFileId(asr.getRawFileId());
		iar.setMimeType(asr.getMimetype());
		return iar;
	}
}
