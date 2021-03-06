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
package org.ihtsdo.translation.tasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.model.Partition;

/**
 * The Class.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class GenerateWorklistFromAttachments extends AbstractTask {

	/** The profile prop name. */
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	
	/** The process prop name. */
	private String processPropName = ProcessAttachmentKeys.PROCESS_TO_LAUNCH.getAttachmentKey();
	
	/** The worklist name prop name. */
	private String worklistNamePropName = ProcessAttachmentKeys.PROCESS_NAME.getAttachmentKey();
	
	/** The translator inbox prop name. */
	private String translatorInboxPropName = ProcessAttachmentKeys.TRANSLATOR_ROLE_INBOX.getAttachmentKey();
	
	/** The fast track translator inbox prop name. */
	private String fastTrackTranslatorInboxPropName = ProcessAttachmentKeys.FAST_TRACK_TRANSLATOR_ROLE_INBOX.getAttachmentKey();
	
	/** The partition prop name. */
	private String partitionPropName = ProcessAttachmentKeys.PARTITION.getAttachmentKey();

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
		out.writeObject(profilePropName);
		out.writeObject(processPropName);
		out.writeObject(worklistNamePropName);
		out.writeObject(translatorInboxPropName);
		out.writeObject(fastTrackTranslatorInboxPropName);
		out.writeObject(partitionPropName);
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
			profilePropName = (String) in.readObject();
			processPropName = (String) in.readObject();
			worklistNamePropName = (String) in.readObject();
			translatorInboxPropName = (String) in.readObject();
			fastTrackTranslatorInboxPropName = (String) in.readObject();
			partitionPropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		I_ConfigAceFrame config;
		try {
			config=(I_ConfigAceFrame)Terms.get().getActiveAceFrameConfig();

			BusinessProcess selectedWorkFlow = (BusinessProcess) process.getProperty(getProcessPropName());

			Partition partition = (Partition) process.getProperty(getPartitionPropName());

			String trans = (String) selectedWorkFlow.getProperty(getTranslatorInboxPropName());
			String fastTrans = (String) selectedWorkFlow.getProperty(getFastTrackTranslatorInboxPropName());
			String destination = "";
			if (fastTrans != null && !fastTrans.isEmpty()) {
				destination =  fastTrans;
			} else if (trans != null && !trans.isEmpty()){
				destination = trans;
			}
			
			if (destination.isEmpty()) {
				throw new TaskFailedException("No destination");
			}
			

			String name = (String) process.getProperty(getWorklistNamePropName());
			//TerminologyProjectDAO.generateWorkListFromPartition(partition, destination, selectedWorkFlow, name, config);
		} catch (Exception e) {
			throw new TaskFailedException(e.getMessage());
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
	 * Gets the profile prop name.
	 *
	 * @return the profile prop name
	 */
	public String getProfilePropName() {
		return profilePropName;
	}

	/**
	 * Sets the profile prop name.
	 *
	 * @param profilePropName the new profile prop name
	 */
	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}

	/**
	 * Gets the process prop name.
	 *
	 * @return the process prop name
	 */
	public String getProcessPropName() {
		return processPropName;
	}

	/**
	 * Sets the process prop name.
	 *
	 * @param processPropName the new process prop name
	 */
	public void setProcessPropName(String processPropName) {
		this.processPropName = processPropName;
	}

	/**
	 * Gets the worklist name prop name.
	 *
	 * @return the worklist name prop name
	 */
	public String getWorklistNamePropName() {
		return worklistNamePropName;
	}

	/**
	 * Sets the worklist name prop name.
	 *
	 * @param worklistNamePropName the new worklist name prop name
	 */
	public void setWorklistNamePropName(String worklistNamePropName) {
		this.worklistNamePropName = worklistNamePropName;
	}

	/**
	 * Gets the translator inbox prop name.
	 *
	 * @return the translator inbox prop name
	 */
	public String getTranslatorInboxPropName() {
		return translatorInboxPropName;
	}

	/**
	 * Sets the translator inbox prop name.
	 *
	 * @param translatorInboxPropName the new translator inbox prop name
	 */
	public void setTranslatorInboxPropName(String translatorInboxPropName) {
		this.translatorInboxPropName = translatorInboxPropName;
	}

	/**
	 * Gets the partition prop name.
	 *
	 * @return the partition prop name
	 */
	public String getPartitionPropName() {
		return partitionPropName;
	}

	/**
	 * Sets the partition prop name.
	 *
	 * @param partitionPropName the new partition prop name
	 */
	public void setPartitionPropName(String partitionPropName) {
		this.partitionPropName = partitionPropName;
	}

	/**
	 * Gets the fast track translator inbox prop name.
	 *
	 * @return the fast track translator inbox prop name
	 */
	public String getFastTrackTranslatorInboxPropName() {
		return fastTrackTranslatorInboxPropName;
	}

	/**
	 * Sets the fast track translator inbox prop name.
	 *
	 * @param fastTrackTranslatorInboxPropName the new fast track translator inbox prop name
	 */
	public void setFastTrackTranslatorInboxPropName(
			String fastTrackTranslatorInboxPropName) {
		this.fastTrackTranslatorInboxPropName = fastTrackTranslatorInboxPropName;
	}

}