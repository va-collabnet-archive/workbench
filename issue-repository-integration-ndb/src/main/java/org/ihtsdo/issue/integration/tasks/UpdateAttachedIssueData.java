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
import java.util.HashMap;

import org.dwfa.ace.api.I_GetConceptData;
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
import org.ihtsdo.issue.Issue;

/**
 * The Class UpdateAttachedIssueData.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class UpdateAttachedIssueData extends AbstractTask {

	/** The selected prop. */
	private String selectedProp;

	/** The property value. */
	private String propertyValue;

	/** The property map key. */
	private String propertyMapKey;

	/** The property map string value. */
	private String propertyMapStringValue;

	/** The property map concept value. */
	private TermEntry propertyMapConceptValue;

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
		out.writeObject(selectedProp);
		out.writeObject(propertyValue);
		out.writeObject(propertyMapKey);
		out.writeObject(propertyMapStringValue);
		out.writeObject(propertyMapConceptValue);
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
			selectedProp = (String) in.readObject();
			propertyValue = (String) in.readObject();
			propertyMapKey = (String) in.readObject();
			propertyMapStringValue = (String) in.readObject();
			propertyMapConceptValue = (TermEntry) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}
	
	/**
	 * Instantiates a new update attached issue data.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public UpdateAttachedIssueData() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		
		Issue issue = (Issue) process.readAttachement("issueKey");


		if (selectedProp != null) {
			if (selectedProp.equals("WorkflowStatus")) {
				issue.setWorkflowStatus(propertyValue);
			}
			if (selectedProp.equals("DownloadStatus")) {
				issue.setDownloadStatus(propertyValue);
			}
			if (selectedProp.equals("Priority")) {
				issue.setPriority(propertyValue);
			}
			if (selectedProp.equals("QueueName")) {
				issue.setPriority(propertyValue);
			}
			if (selectedProp.equals("Role")) {
				issue.setRole(propertyValue);
			}
			if (selectedProp.equals("User")) {
				issue.setUser(propertyValue);
			}
		}
		if (issue.getFieldMap() == null) {
			issue.setFieldMap(new HashMap<String, Object>());
		}
		if (propertyMapStringValue != null && propertyMapConceptValue != null) {
			// throw error
			throw new TaskFailedException("Change only String or Concept value, can't process both with one key.");
		} else if (propertyMapStringValue != null) {
			issue.getFieldMap().put(propertyMapKey, propertyMapStringValue);
		} else if (propertyMapConceptValue != null) {
			I_GetConceptData droppedConcept = null;
			try {
				droppedConcept = Terms.get().getConcept(propertyMapConceptValue.ids);
				issue.getFieldMap().put(propertyMapKey, droppedConcept);
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		process.writeAttachment("issueKey", issue);

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
	 * Gets the property map key.
	 * 
	 * @return the property map key
	 */
	public String getPropertyMapKey() {
		return propertyMapKey;
	}

	/**
	 * Sets the property map key.
	 * 
	 * @param propertyMapKey the new property map key
	 */
	public void setPropertyMapKey(String propertyMapKey) {
		this.propertyMapKey = propertyMapKey;
	}

	/**
	 * Gets the property map string value.
	 * 
	 * @return the property map string value
	 */
	public String getPropertyMapStringValue() {
		return propertyMapStringValue;
	}

	/**
	 * Sets the property map string value.
	 * 
	 * @param propertyMapStringValue the new property map string value
	 */
	public void setPropertyMapStringValue(String propertyMapStringValue) {
		this.propertyMapStringValue = propertyMapStringValue;
	}

	/**
	 * Gets the property map concept value.
	 * 
	 * @return the property map concept value
	 */
	public TermEntry getPropertyMapConceptValue() {
		return propertyMapConceptValue;
	}

	/**
	 * Sets the property map concept value.
	 * 
	 * @param propertyMapConceptValue the new property map concept value
	 */
	public void setPropertyMapConceptValue(TermEntry propertyMapConceptValue) {
		this.propertyMapConceptValue = propertyMapConceptValue;
	}

	/**
	 * Gets the selected prop.
	 * 
	 * @return the selected prop
	 */
	public String getSelectedProp() {
		return selectedProp;
	}

	/**
	 * Sets the selected prop.
	 * 
	 * @param selectedProp the new selected prop
	 */
	public void setSelectedProp(String selectedProp) {
		this.selectedProp = selectedProp;
	}

	/**
	 * Gets the property value.
	 * 
	 * @return the property value
	 */
	public String getPropertyValue() {
		return propertyValue;
	}

	/**
	 * Sets the property value.
	 * 
	 * @param propertyValue the new property value
	 */
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

}