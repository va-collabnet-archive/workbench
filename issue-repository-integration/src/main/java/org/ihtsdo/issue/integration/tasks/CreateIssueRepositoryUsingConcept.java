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
package org.ihtsdo.issue.integration.tasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

/**
 * The Class CreateIssueRepositoryUsingConcept.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class CreateIssueRepositoryUsingConcept extends AbstractTask {

	/** The issue repo prop. */
	private TermEntry issueRepoProp;

	/** The repository id. */
	private String repositoryId;

	/** The repository url. */
	private String repositoryUrl;

	/** The repository type. */
	private String repositoryType;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(issueRepoProp);
		out.writeObject(repositoryId);
		out.writeObject(repositoryUrl);
		out.writeObject(repositoryType);
	}

	/**
	 * Read object.
	 * 
	 * @param in the in
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			issueRepoProp = (TermEntry) in.readObject();
			repositoryId = (String) in.readObject();
			repositoryUrl = (String) in.readObject();
			repositoryType = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}
	
	/**
	 * Instantiates a new creates the issue repository using concept.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public CreateIssueRepositoryUsingConcept() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		I_ConfigAceFrame config = (I_ConfigAceFrame) worker
		.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
		I_GetConceptData issueRepoConcept = null;

		if(issueRepoProp== null){
			throw new TaskFailedException("Concept is required...");
		} else {
			try {
				issueRepoConcept = Terms.get().getConcept(issueRepoProp.ids);
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (repositoryId == null) {
			// get from attachment
			repositoryId = (String) process.readAttachement("repositoryIdKey");
		}

		if (repositoryUrl == null) {
			// get from attachment
			repositoryUrl = (String) process.readAttachement("repositoryUrlKey");
		}

		if (repositoryType == null) {
			// get from attachment
			repositoryType = (String) process.readAttachement("repositoryTypeKey");
		}

		Integer repositoryTypeInt = IssueRepository.REPOSITORY_TYPE.valueOf(repositoryType).ordinal();

		//System.out.println("****************" + repositoryUrl + "|" + repositoryName + "|" + repositoryTypeInt);
		try {
			IssueRepository newRepository = new IssueRepository(repositoryId, repositoryUrl, issueRepoConcept.toString(), repositoryTypeInt);
			newRepository.setConceptId(issueRepoConcept.getConceptNid());
			newRepository.setUuid(issueRepoConcept.getUids().iterator().next());

			IssueRepositoryDAO.updateRepositoryMetadata(newRepository, config);


			newRepository = IssueRepositoryDAO.getIssueRepository(issueRepoConcept);
			process.writeAttachment(ProcessAttachmentKeys.ISSUE_REPO_CONCEPT.getAttachmentKey(), newRepository);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Condition.CONTINUE;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {  };
	}

	/**
	 * Gets the repository url.
	 * 
	 * @return the repository url
	 */
	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	/**
	 * Sets the repository url.
	 * 
	 * @param repositoryUrl the new repository url
	 */
	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	/**
	 * Gets the repository type.
	 * 
	 * @return the repository type
	 */
	public String getRepositoryType() {
		return repositoryType;
	}

	/**
	 * Sets the repository type.
	 * 
	 * @param repositoryType the new repository type
	 */
	public void setRepositoryType(String repositoryType) {
		this.repositoryType = repositoryType;
	}

	/**
	 * Gets the repository id.
	 * 
	 * @return the repository id
	 */
	public String getRepositoryId() {
		return repositoryId;
	}

	/**
	 * Sets the repository id.
	 * 
	 * @param repositoryId the new repository id
	 */
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	/**
	 * Gets the issue repo prop.
	 * 
	 * @return the issue repo prop
	 */
	public TermEntry getIssueRepoProp() {
		return issueRepoProp;
	}

	/**
	 * Sets the issue repo prop.
	 * 
	 * @param issueRepoProp the new issue repo prop
	 */
	public void setIssueRepoProp(TermEntry issueRepoProp) {
		this.issueRepoProp = issueRepoProp;
	}




}