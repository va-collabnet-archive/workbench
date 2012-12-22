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
import java.net.MalformedURLException;
import java.util.Collection;

import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.model.WorkListMember;
/**
 * The Class TranslationWorkFlowStep3Outputs.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class PromoteAttachedWorkListMember extends AbstractTask {

	/** The profile. */
	private String profilePropName;

	/** The worklist item. */
	private String workListItemPropName = ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey();

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
		out.writeObject(workListItemPropName);
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
			workListItemPropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}

	/**
	 * Instantiates a new translation work flow step.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public PromoteAttachedWorkListMember() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try {

			WorkListMember workListMember = (WorkListMember) process.readAttachement(getWorkListItemPropName());
			if (workListMember == null) {
				throw new TaskFailedException("Missing workist member attachment");
			}
		
			return Condition.CONTINUE;
		} catch (Exception e) {
			e.printStackTrace();
			return Condition.STOP;
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do

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
	 * Gets the work list item prop name.
	 *
	 * @return the work list item prop name
	 */
	public String getWorkListItemPropName() {
		return workListItemPropName;
	}

	/**
	 * Sets the work list item prop name.
	 *
	 * @param workListItemPropName the new work list item prop name
	 */
	public void setWorkListItemPropName(String workListItemPropName) {
		this.workListItemPropName = workListItemPropName;
	}
}