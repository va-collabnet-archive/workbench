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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

/**
 * The Class AddIssueRepoRegistrationToProfileFromProps.
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN) })
public class AddIssueRepoRegistrationToProfileFromProps extends AbstractTask {

	/**
	 * The Enum WORKER_KIND.
	 */
	private enum WORKER_KIND {
		
		/** The ISSU e_ rep o_ inbox. */
		ISSUE_REPO_INBOX, 
 /** The ISSU e_ rep o_ outbox. */
 ISSUE_REPO_OUTBOX
	};

	/** The profile prop. */
	private String profileProp = ProcessAttachmentKeys.WORKING_PROFILE
			.getAttachmentKey();

	/** The issue repo concept prop. */
	private String issueRepoConceptProp = ProcessAttachmentKeys.ISSUE_REPO_CONCEPT
			.getAttachmentKey();

	/** The username prop. */
	private String usernameProp = ProcessAttachmentKeys.ISSUE_REPO_PROP_NAME
			.getAttachmentKey();

	/** The password prop. */
	private String passwordProp = ProcessAttachmentKeys.ISSUE_REPO_PROP_PASSWORD
			.getAttachmentKey();

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
		out.writeObject(issueRepoConceptProp);
		out.writeObject(usernameProp);
		out.writeObject(passwordProp);
		out.writeObject(profileProp);
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
			issueRepoConceptProp = (String) in.readObject();
			usernameProp = (String) in.readObject();
			passwordProp = (String) in.readObject();
			profileProp = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {
			I_GetConceptData issueRepoConcept = AceTaskUtil
					.getConceptFromProperty(process, issueRepoConceptProp);

			I_ConfigAceFrame profile = (I_ConfigAceFrame) process
					.getProperty(profileProp);

			String username = (String) process.getProperty(usernameProp);
			String password = (String) process.getProperty(passwordProp);
			if (username == null || password == null) {
				throw new TaskFailedException(
						"Username and Password are required.");
			}
			IssueRepoRegistration issueRepoReg = new IssueRepoRegistration(
					issueRepoConcept.getUids().iterator().next(), username,
					password, null);

			IssueRepositoryDAO.addRepositoryToProfile(issueRepoReg, profile
					.getDbConfig());
			String repoUuidStr = issueRepoConcept.getUids().iterator().next().toString();
			profile.getDbConfig().getProperties().put("SMERepositoryUUID", repoUuidStr);

			File inboxConfigFile = new File("profiles/" + profile.getUsername().trim()
					+ "/queues/" + profile.getUsername().trim() + ".inbox/queue.config");
			File outboxConfigFile = new File("profiles/" + profile.getUsername().trim()
					+ "/queues/" + profile.getUsername().trim() + ".outbox/queue.config");

			addWorkerToQueueConfig(inboxConfigFile,
					WORKER_KIND.ISSUE_REPO_INBOX, username, repoUuidStr);
			addWorkerToQueueConfig(outboxConfigFile,
					WORKER_KIND.ISSUE_REPO_OUTBOX, username, repoUuidStr);
			return Condition.CONTINUE;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	/**
	 * Adds the worker to queue config.
	 * 
	 * @param configFile the config file
	 * @param worker the worker
	 * @param username the username
	 * @param repoUuid the repo uuid
	 * 
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	private void addWorkerToQueueConfig(File configFile, WORKER_KIND worker, 
			String username, String repoUuid)
			throws FileNotFoundException, IOException, TerminologyException {
		StringBuilder updatedConfig = new StringBuilder();
		BufferedReader input = new BufferedReader(new FileReader(configFile));
		try {
			String line = null;
				switch (worker) {
				case ISSUE_REPO_INBOX:
					while ((line = input.readLine()) != null) {
						processInboxWorkerConfigLine(updatedConfig, line, username, repoUuid);
					}
					break;
				case ISSUE_REPO_OUTBOX:
					while ((line = input.readLine()) != null) {
						processOutboxWorkerConfigLine(updatedConfig, line);
					}
					break;
				default:
					throw new IOException("Can't handle worker kind: " + worker);
				}
			
		} finally {
			input.close();
		}
		Writer output = new BufferedWriter(new FileWriter(configFile));
		try {
			output.write(updatedConfig.toString());
		} finally {
			output.close();
		}
	}

	/**
	 * Process outbox worker config line.
	 * 
	 * @param config the config
	 * @param line the line
	 */
	private void processOutboxWorkerConfigLine(StringBuilder config,
			String line) {
		if (line.trim().startsWith("org.dwfa.queue.bpa.worker.OutboxQueueWorker")) {
			config.append("org.ihtsdo.issue.integration.workers.IssueRepositoryOutboxQueueWorker {");
			config.append("\n");
			// Gets the repository from the issue
		} else if (line.trim().startsWith("workerSpecs = new QueueWorkerSpec")) {
			config.append("workerSpecs = new QueueWorkerSpec[] { new QueueWorkerSpec(" + 
					"org.ihtsdo.issue.integration.workers.IssueRepositoryOutboxQueueWorker.class, " + 
					"\"Outbox Issue repository worker\", UUID.randomUUID(),");
			config.append("\n");
		} else {
			config.append(line);
			config.append("\n");
		}
	}

	/**
	 * Process inbox worker config line.
	 * 
	 * @param config the config
	 * @param line the line
	 * @param username the username
	 * @param repoUuid the repo uuid
	 * 
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void processInboxWorkerConfigLine(StringBuilder config,
			String line, String username, String repoUuid) throws TerminologyException, IOException {
		if (line.trim().startsWith("org.dwfa.queue.bpa.worker.InboxQueueWorker")) {
			config.append("org.ihtsdo.issue.integration.workers.IssueRepositoryInboxQueueWorker {\n");
			config.append("issueRepoUid = \"" + repoUuid + "\";\n");
		} else if (line.trim().startsWith("username = \"**mailUsername**\";")) {
			config.append("username = \"" + username.trim() + "\";\n");
		} else {
			config.append(line);
			config.append("\n");
		}
		if (line.trim().startsWith("workerSpecs = new QueueWorkerSpec")) {
			config.append("new QueueWorkerSpec(" + 
					"org.ihtsdo.issue.integration.workers.IssueRepositoryInboxQueueWorker.class, " + 
					"\"Inbox Issue repository worker\", UUID.randomUUID(),new SelectAll())\n");
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// nothing to do...
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
		return new int[] {};
	}

	/**
	 * Gets the username prop.
	 * 
	 * @return the username prop
	 */
	public String getUsernameProp() {
		return usernameProp;
	}

	/**
	 * Sets the username prop.
	 * 
	 * @param usernameProp the new username prop
	 */
	public void setUsernameProp(String usernameProp) {
		this.usernameProp = usernameProp;
	}

	/**
	 * Gets the password prop.
	 * 
	 * @return the password prop
	 */
	public String getPasswordProp() {
		return passwordProp;
	}

	/**
	 * Sets the password prop.
	 * 
	 * @param passwordProp the new password prop
	 */
	public void setPasswordProp(String passwordProp) {
		this.passwordProp = passwordProp;
	}

	/**
	 * Gets the issue repo concept prop.
	 * 
	 * @return the issue repo concept prop
	 */
	public String getIssueRepoConceptProp() {
		return issueRepoConceptProp;
	}

	/**
	 * Sets the issue repo concept prop.
	 * 
	 * @param issueRepoConceptProp the new issue repo concept prop
	 */
	public void setIssueRepoConceptProp(String issueRepoConceptProp) {
		this.issueRepoConceptProp = issueRepoConceptProp;
	}

	/**
	 * Gets the profile prop.
	 * 
	 * @return the profile prop
	 */
	public String getProfileProp() {
		return profileProp;
	}

	/**
	 * Sets the profile prop.
	 * 
	 * @param profileProp the new profile prop
	 */
	public void setProfileProp(String profileProp) {
		this.profileProp = profileProp;
	}

}