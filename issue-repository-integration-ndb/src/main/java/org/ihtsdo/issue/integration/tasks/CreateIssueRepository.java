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
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

/**
 * The Class CreateIssueRepository.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class CreateIssueRepository extends AbstractTask {

	/** The repository id. */
	private String repositoryId;

	/** The repository url. */
	private String repositoryUrl;

	/** The repository name. */
	private String repositoryName;

	/** The repository type. */
	private String repositoryType;

	private String repositoryConceptPropName = ProcessAttachmentKeys.ISSUE_REPO_CONCEPT.getAttachmentKey();

	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

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
		out.writeObject(repositoryId);
		out.writeObject(repositoryUrl);
		out.writeObject(repositoryName);
		out.writeObject(repositoryType);
		out.writeObject(repositoryConceptPropName);
		out.writeObject(profilePropName);
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
			repositoryId = (String) in.readObject();
			repositoryUrl = (String) in.readObject();
			repositoryName = (String) in.readObject();
			repositoryType = (String) in.readObject();
			repositoryConceptPropName = (String) in.readObject();
			profilePropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}

	/**
	 * Instantiates a new creates the issue repository.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public CreateIssueRepository() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());

			if (repositoryId == null) {
				// get from attachment
				repositoryId = (String) process.readAttachement("repositoryIdKey");
			}

			if (repositoryUrl == null) {
				// get from attachment
				repositoryUrl = (String) process.readAttachement("repositoryUrlKey");
			}

			if (repositoryName == null) {
				// get from attachment
				repositoryName = (String) process.readAttachement("repositoryNameKey");
			}

			if (repositoryType == null) {
				// get from attachment
				repositoryType = (String) process.readAttachement("repositoryTypeKey");
			}

			Integer repositoryTypeInt = IssueRepository.REPOSITORY_TYPE.valueOf(repositoryType).ordinal();

			//System.out.println("****************" + repositoryUrl + "|" + repositoryName + "|" + repositoryTypeInt);

			IssueRepository newRepository = new IssueRepository(repositoryId, repositoryUrl, repositoryName, repositoryTypeInt);

			I_GetConceptData issueRepoConcept = IssueRepositoryDAO.addIssueRepoToMetahier(newRepository, config);


			newRepository = IssueRepositoryDAO.getIssueRepository(issueRepoConcept);
			
			process.setProperty(getRepositoryConceptPropName(), issueRepoConcept);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
	 * Gets the repository name.
	 * 
	 * @return the repository name
	 */
	public String getRepositoryName() {
		return repositoryName;
	}

	/**
	 * Sets the repository name.
	 * 
	 * @param repositoryName the new repository name
	 */
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
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

	public String getRepositoryConceptPropName() {
		return repositoryConceptPropName;
	}

	public void setRepositoryConceptPropName(String repositoryConceptPropName) {
		this.repositoryConceptPropName = repositoryConceptPropName;
	}

	public String getProfilePropName() {
		return profilePropName;
	}

	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}


}