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
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.panel.TranslationProjectDialog;

/**
 * The Class GetTranslationProject.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/project tasks", type = BeanType.TASK_BEAN)})
public class GetTranslationProject extends AbstractTask {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;
	
	private String projectPropName = ProcessAttachmentKeys.TERMINOLOGY_PROJECT.getAttachmentKey();

	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(projectPropName);
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
			projectPropName = (String)in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}
	
	/**
	 * Instantiates a new list projects to signpost.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public GetTranslationProject() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try {
			//TranslationProject project;
			AceFrameConfig config = (AceFrameConfig) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
			if (config==null){
				config=(AceFrameConfig)Terms.get().getActiveAceFrameConfig();
			}
			I_TermFactory tf = Terms.get();
			ProjectPermissionsAPI permissionApi = new ProjectPermissionsAPI(config);
			
			boolean projectConfigPermission = permissionApi.checkPermissionForProject(
					config.getDbConfig().getUserConcept(), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
					tf.getConcept(ArchitectonicAuxiliary.Concept.CONFIGURATION_MANAGER_ROLE.localize().getNid()));
			if(!projectConfigPermission){
				JOptionPane.showMessageDialog(new JDialog(),"You don't have permission to modify the translation configuration.","Process end", JOptionPane.CLOSED_OPTION);
				return Condition.ITEM_CANCELED;
			}
			
			TranslationProjectDialog dialog = new TranslationProjectDialog();
			TranslationProject project = dialog.showModalDialog();
			if(project != null){
				System.out.println("Project ID: " + project.getId());
				process.setProperty(projectPropName, project);
			}else{
				JOptionPane.showMessageDialog(dialog,"Process terminated, no projects where selected.","Process end", JOptionPane.CLOSED_OPTION);
				return Condition.ITEM_CANCELED;
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
		Collection<Condition> conditions = new ArrayList<Condition>();
		conditions.add(Condition.ITEM_CANCELED);
		conditions.add(Condition.CONTINUE);
		return conditions;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {  };
	}
	
	public String getProjectPropName() {
		return projectPropName;
	}
	
	public void setProjectPropName(String projectPropName) {
		this.projectPropName = projectPropName;
	}

}