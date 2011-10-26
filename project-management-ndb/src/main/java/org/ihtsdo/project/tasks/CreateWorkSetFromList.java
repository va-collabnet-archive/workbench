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
package org.ihtsdo.project.tasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.model.WorkSetMember;

/**
 * The Class CreateWorkSetFromList.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/project tasks", type = BeanType.TASK_BEAN)})
public class CreateWorkSetFromList extends AbstractTask {

	/** The project. */
	private String project;
	
	/** The workset name. */
	private String worksetName;
	
	/** The workset description. */
	private String worksetDescription;

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
		out.writeObject(project);
		out.writeObject(worksetName);
		out.writeObject(worksetDescription);
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
			project = (String) in.readObject();
			worksetName = (String) in.readObject();
			worksetDescription = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}
	
	/**
	 * Instantiates a new creates the work set from list.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public CreateWorkSetFromList() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
			.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

			JList conceptList = config.getBatchConceptList();
			I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
			List<TranslationProject> projects = TerminologyProjectDAO.getAllTranslationProjects(config);
			worker.getLogger().log(Level.INFO, "before loop with project string : " + project + " | and model size: " + model.getSize());
			for (I_TerminologyProject loopProject : projects) {
				worker.getLogger().log(Level.INFO, "Inside loop for project: |" + loopProject.getName() + "| - |" + project + "|");
				if (loopProject.getName().equals(project)) {
					worker.getLogger().log(Level.INFO, "Project name matched");
					WorkSet newWorkSet = new WorkSet(worksetName, 0, null, 
							loopProject.getUids().iterator().next());
					WorkSet commitedWorkSet = TerminologyProjectDAO.createNewWorkSet(newWorkSet, config);
					if(commitedWorkSet != null){
						for (int index = 0 ; index < model.getSize() ; index++) {
							//I_GetConceptData conceptToAdd = config.getHierarchySelection();
							I_GetConceptData conceptToAdd = model.getElementAt(index);
							worker.getLogger().log(Level.INFO, "Inside loop for model: " + conceptToAdd.getConceptNid());
							
							WorkSetMember newMember = new WorkSetMember("Member Name", 
									conceptToAdd.getConceptNid(), 
									conceptToAdd.getUids(),
									commitedWorkSet.getUids().iterator().next());
							TerminologyProjectDAO.addConceptAsWorkSetMember(newMember, config);
						}
					}
				}
			}

			return Condition.CONTINUE;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
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
	 * Gets the project.
	 * 
	 * @return the project
	 */
	public String getProject() {
		return project;
	}

	/**
	 * Sets the project.
	 * 
	 * @param project the new project
	 */
	public void setProject(String project) {
		this.project = project;
	}

	/**
	 * Gets the workset name.
	 * 
	 * @return the workset name
	 */
	public String getWorksetName() {
		return worksetName;
	}

	/**
	 * Sets the workset name.
	 * 
	 * @param worksetName the new workset name
	 */
	public void setWorksetName(String worksetName) {
		this.worksetName = worksetName;
	}

	/**
	 * Gets the workset description.
	 * 
	 * @return the workset description
	 */
	public String getWorksetDescription() {
		return worksetDescription;
	}

	/**
	 * Sets the workset description.
	 * 
	 * @param worksetDescription the new workset description
	 */
	public void setWorksetDescription(String worksetDescription) {
		this.worksetDescription = worksetDescription;
	}


}