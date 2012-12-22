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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.Collection;

import javax.swing.JFileChooser;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.Terms;
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
 * The Class RemoveWorkerFromQueueConfigFile.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class RemoveWorkerFromQueueConfigFile extends AbstractTask {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	/** The issue repo prop. */
	private TermEntry issueRepoProp;

	/** The selected worker. */
	private String selectedWorker;

	/** The file name. */
	private String fileName;

	/** The message. */
	private String message = "Please select a file";

	/**
	 * Gets the message.
	 * 
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 * 
	 * @param message the new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(fileName);
		out.writeObject(message);
		out.writeObject(selectedWorker);
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
			fileName = (String) in.readObject();
			message = (String) in.readObject();
			selectedWorker = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {

		JFileChooser fileChooser = new JFileChooser();
		String username = "";
		try {
			username = Terms.get().getActiveAceFrameConfig().getUsername();
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		File inboxConfigFile = new File("profiles/" + username.trim() + "/queues/" + username.trim() + ".inbox/queue.config");
		File outboxConfigFile = new File("profiles/" + username.trim() + "/queues/" + username.trim() + ".outbox/queue.config");

		I_GetConceptData issueRepoConcept = null;
		IssueRepository repository =null;
		if(issueRepoProp== null){
			// get from attachment
			repository = (IssueRepository) process.readAttachement("issueRepositoryKey");
		} else {
			try {
				issueRepoConcept = Terms.get().getConcept(issueRepoProp.ids);
				repository = IssueRepositoryDAO.getIssueRepository(issueRepoConcept);
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		StringBuilder updatedConfig = new StringBuilder();

		try {
			BufferedReader input =  null;
			Writer output = null; 
			if (selectedWorker.equals("IssueRepositoryInboxWorker")) {
				input = new BufferedReader(new FileReader(inboxConfigFile));
			} else if (selectedWorker.equals("IssueRepositoryOutboxWorker")) {
				input = new BufferedReader(new FileReader(outboxConfigFile));
			}
			try {
				String line = null;
				while (( line = input.readLine()) != null){
					if (selectedWorker.equals("IssueRepositoryInboxWorker")) {
						if (line.trim().startsWith("org.ihtsdo.issue.integration.workers.IssueRepositoryInboxQueueWorker")) {
							updatedConfig.append("org.dwfa.queue.bpa.worker.InboxQueueWorker {");
							updatedConfig.append(System.getProperty("line.separator"));
						} else if (line.trim().startsWith("username = \"**mailUsername**\";")){
							updatedConfig.append("username = \"" + LocalVersionedTerminology.get().getActiveAceFrameConfig().getUsername().trim() + "\";");
							updatedConfig.append(System.getProperty("line.separator"));
						} else if (line.trim().startsWith("issueRepoUid =")){
							//skip line to remove issueRepoUid
						} else if (line.trim().startsWith("new QueueWorkerSpec(org.ihtsdo.issue.integration.workers.IssueRepositoryInboxQueueWorker")) {
							//skip line to remove queue worker spec
						} else {
							updatedConfig.append(line);
							updatedConfig.append(System.getProperty("line.separator"));
						}
					} else if (selectedWorker.equals("IssueRepositoryOutboxWorker")) {
						if (line.trim().startsWith("org.ihtsdo.issue.integration.workers.IssueRepositoryOutboxQueueWorker")) {
							updatedConfig.append("org.dwfa.queue.bpa.worker.OutboxQueueWorker {");
							updatedConfig.append(System.getProperty("line.separator"));
							// Gets the repository from the issue
						} else if (line.trim().startsWith("workerSpecs = new QueueWorkerSpec[] { new QueueWorkerSpec(org.ihtsdo.issue.integration.workers.IssueRepositoryOutboxQueueWorker")) {
							updatedConfig.append("workerSpecs = new QueueWorkerSpec[] {new QueueWorkerSpec(AllGroupsOutboxQueueWorker.class, \"Outbox worker\", UUID.randomUUID(),");
							updatedConfig.append(System.getProperty("line.separator"));
						} else {
							updatedConfig.append(line);
							updatedConfig.append(System.getProperty("line.separator"));
						}
					}
				} 
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
			finally {
				input.close();
			}
			
			if (selectedWorker.equals("IssueRepositoryInboxWorker")) {
				output = new BufferedWriter(new FileWriter(inboxConfigFile));
			} else if (selectedWorker.equals("IssueRepositoryOutboxWorker")) {
				output = new BufferedWriter(new FileWriter(outboxConfigFile));
			}
			try {
				output.write(updatedConfig.toString());
			}
			finally {
				output.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
		}

		return Condition.CONTINUE;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		// Nothing to do. 
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

	/**
	 * Gets the file name.
	 * 
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the file name.
	 * 
	 * @param fileName the new file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Gets the selected worker.
	 * 
	 * @return the selected worker
	 */
	public String getSelectedWorker() {
		return selectedWorker;
	}

	/**
	 * Sets the selected worker.
	 * 
	 * @param selectedWorker the new selected worker
	 */
	public void setSelectedWorker(String selectedWorker) {
		this.selectedWorker = selectedWorker;
	}
}
