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
import java.util.Collection;

import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The Class AddIssueRepoRegistrationToProfile.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class PutRepoConceptInProperty extends AbstractTask {

	/** The issue repo prop. */
	private TermEntry issueRepoConcept;

	/** The repository concept prop name. */
	private String repositoryConceptPropName = ProcessAttachmentKeys.ISSUE_REPO_CONCEPT.getAttachmentKey();
	
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
		out.writeObject(issueRepoConcept);
		out.writeObject(repositoryConceptPropName);
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
			issueRepoConcept=(TermEntry)in.readObject();
			repositoryConceptPropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
			try{
				I_GetConceptData concept = null;
				if(issueRepoConcept != null){
					concept = Terms.get().getConcept(issueRepoConcept.ids);
				}
				
				if (concept == null) {
					JOptionPane.showMessageDialog(null,
							"Problem getting the Issue Repository.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					throw new TaskFailedException("Problem getting the Issue Repository.");
				}

				process.setProperty(getRepositoryConceptPropName(), concept);
				
	         } catch (Exception e) {
	            throw new TaskFailedException(e);
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
	 * Gets the issue repo concept.
	 *
	 * @return the issue repo concept
	 */
	public TermEntry getIssueRepoConcept() {
		return issueRepoConcept;
	}

	/**
	 * Sets the issue repo concept.
	 *
	 * @param issueRepoConcept the new issue repo concept
	 */
	public void setIssueRepoConcept(TermEntry issueRepoConcept) {
		this.issueRepoConcept = issueRepoConcept;
	}

	/**
	 * Gets the repository concept prop name.
	 *
	 * @return the repository concept prop name
	 */
	public String getRepositoryConceptPropName() {
		return repositoryConceptPropName;
	}

	/**
	 * Sets the repository concept prop name.
	 *
	 * @param repositoryConceptPropName the new repository concept prop name
	 */
	public void setRepositoryConceptPropName(String repositoryConceptPropName) {
		this.repositoryConceptPropName = repositoryConceptPropName;
	}

}