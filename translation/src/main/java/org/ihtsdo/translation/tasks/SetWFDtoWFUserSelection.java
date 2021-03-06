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

import java.awt.Component;
import java.awt.GridLayout;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.FileLink;
import org.ihtsdo.project.FileLinkAPI;
import org.ihtsdo.project.model.I_TerminologyProject;

/**
 * The Class.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class SetWFDtoWFUserSelection extends AbstractTask {

	/** The profile prop name. */
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	
	/** The project prop name. */
	private String projectPropName = ProcessAttachmentKeys.TERMINOLOGY_PROJECT.getAttachmentKey();

	/** The worklist name. */
	public Boolean worklistName = true;
	
	/** The business process. */
	public Boolean businessProcess = true;
	
	/** The translator. */
	public Boolean translator = true;

	/** The reviewer1. */
	public Boolean reviewer1 = true;

	/** The reviewer2. */
	public Boolean reviewer2 = true;

	/** The sme. */
	public Boolean sme = true;

	/** The editorial board. */
	public Boolean editorialBoard = true;

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
		out.writeObject(worklistName);
		out.writeObject(businessProcess);
		out.writeObject(translator);
		out.writeObject(reviewer1);
		out.writeObject(reviewer2);
		out.writeObject(sme);
		out.writeObject(editorialBoard);
		out.writeObject(profilePropName);
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
			worklistName=(Boolean)in.readObject();
			businessProcess=(Boolean)in.readObject();
			translator=(Boolean)in.readObject();
			reviewer1=(Boolean)in.readObject();
			reviewer2=(Boolean)in.readObject();
			sme=(Boolean)in.readObject();
			editorialBoard=(Boolean)in.readObject();
			profilePropName = (String) in.readObject();
			projectPropName = (String) in.readObject();
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
			I_TermFactory tf = Terms.get();

			config=(I_ConfigAceFrame)Terms.get().getActiveAceFrameConfig();

			I_TerminologyProject project = (I_TerminologyProject) process.getProperty(getProjectPropName());

			FileLinkAPI flApi = new FileLinkAPI(config);
			FileLink link1 = new FileLink(new File("sampleProcesses/TranslationWorkflow.bp"), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
			flApi.putLinkInConfig(link1);
			FileLink link2 = new FileLink(new File("sampleProcesses/MaintenanceWorkflow.bp"), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
			flApi.putLinkInConfig(link2);
			FileLink link3 = new FileLink(new File("sampleProcesses/IsolatedEdit.bp"), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
			flApi.putLinkInConfig(link3);
			FileLink link4 = new FileLink(new File("sampleProcesses/TranslationWorkflowCa.bp"), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
			flApi.putLinkInConfig(link4);
			FileLink link5 = new FileLink(new File("sampleProcesses/TranslationWorkflowCaFastTrack.bp"), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
			flApi.putLinkInConfig(link5);
			FileLink link6 = new FileLink(new File("sampleProcesses/TranslationWorkflowDk.bp"), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
			flApi.putLinkInConfig(link6);


			JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
			List<Component> componentsToRemove = new ArrayList<Component>();
			
			for (Component component : workflowDetailsSheet.getComponents()) {
				componentsToRemove.add(component);
			}
			for (Component component : componentsToRemove) {
				workflowDetailsSheet.remove(component);
			}
			int width = 475;
			int height = 260;
			workflowDetailsSheet.setSize(width, height);
			workflowDetailsSheet.setLayout(new GridLayout(1, 1));
			
			UsersSelectionForWorkflowPanel newPanel = new UsersSelectionForWorkflowPanel(project, config);

			workflowDetailsSheet.add(newPanel);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IntrospectionException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
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
	 * Gets the translator.
	 *
	 * @return the translator
	 */
	public Boolean getTranslator() {
		return translator;
	}

	/**
	 * Sets the translator.
	 *
	 * @param translator the new translator
	 */
	public void setTranslator(Boolean translator) {
		this.translator = translator;
	}

	/**
	 * Gets the reviewer1.
	 *
	 * @return the reviewer1
	 */
	public Boolean getReviewer1() {
		return reviewer1;
	}

	/**
	 * Sets the reviewer1.
	 *
	 * @param reviewer1 the new reviewer1
	 */
	public void setReviewer1(Boolean reviewer1) {
		this.reviewer1 = reviewer1;
	}

	/**
	 * Gets the reviewer2.
	 *
	 * @return the reviewer2
	 */
	public Boolean getReviewer2() {
		return reviewer2;
	}

	/**
	 * Sets the reviewer2.
	 *
	 * @param reviewer2 the new reviewer2
	 */
	public void setReviewer2(Boolean reviewer2) {
		this.reviewer2 = reviewer2;
	}

	/**
	 * Gets the sme.
	 *
	 * @return the sme
	 */
	public Boolean getSme() {
		return sme;
	}

	/**
	 * Sets the sme.
	 *
	 * @param sme the new sme
	 */
	public void setSme(Boolean sme) {
		this.sme = sme;
	}

	/**
	 * Gets the editorial board.
	 *
	 * @return the editorial board
	 */
	public Boolean getEditorialBoard() {
		return editorialBoard;
	}

	/**
	 * Sets the editorial board.
	 *
	 * @param editorialBoard the new editorial board
	 */
	public void setEditorialBoard(Boolean editorialBoard) {
		this.editorialBoard = editorialBoard;
	}

	/**
	 * Gets the project prop name.
	 *
	 * @return the project prop name
	 */
	public String getProjectPropName() {
		return projectPropName;
	}

	/**
	 * Sets the project prop name.
	 *
	 * @param projectPropName the new project prop name
	 */
	public void setProjectPropName(String projectPropName) {
		this.projectPropName = projectPropName;
	}

	/**
	 * Gets the business process.
	 *
	 * @return the business process
	 */
	public Boolean getBusinessProcess() {
		return businessProcess;
	}

	/**
	 * Sets the business process.
	 *
	 * @param businessProcess the new business process
	 */
	public void setBusinessProcess(Boolean businessProcess) {
		this.businessProcess = businessProcess;
	}

	/**
	 * Gets the worklist name.
	 *
	 * @return the worklist name
	 */
	public Boolean getWorklistName() {
		return worklistName;
	}

	/**
	 * Sets the worklist name.
	 *
	 * @param worklistName the new worklist name
	 */
	public void setWorklistName(Boolean worklistName) {
		this.worklistName = worklistName;
	}

}