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
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.IssueDAO;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.ui.ConfigTranslationModule;

/**
 * The Class
 */
@BeanList(specs = { @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN) })
public class CreateIssueForInternationalEdition extends AbstractTask {

	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE
			.getAttachmentKey();

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	/**
	 * Write object.
	 * 
	 * @param out
	 *            the out
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
	}

	/**
	 * Read object.
	 * 
	 * @param in
	 *            the in
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			profilePropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.
	 * I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process,
			I_Work worker) throws TaskFailedException {
		I_ConfigAceFrame config;
		I_TermFactory tf = Terms.get();
		Date timeStamp = new Date();
		try {
			config = (I_ConfigAceFrame) process
					.getProperty(getProfilePropName());
			if (config == null) {
				config = Terms.get().getActiveAceFrameConfig();
			}

//			ConfigTranslationModule translationConfig = LanguageUtil
//					.getTranslationConfig(config);
//
//			if (translationConfig == null) {
//				JOptionPane.showMessageDialog(null,
//						"Problem getting translation configuration.", "Error",
//						JOptionPane.ERROR_MESSAGE);
//				throw new TaskFailedException(
//						"Problem getting translation configuration.");
//			}
//
//			if (translationConfig.getSourceIssuesRepositoryIds() == null) {
//				JOptionPane
//						.showMessageDialog(
//								null,
//								"Problem getting source language issue repository information from config.",
//								"Error", JOptionPane.ERROR_MESSAGE);
//				throw new TaskFailedException(
//						"Problem getting source language issue repository information from config.");
//			}

			Object[] options = TerminologyProjectDAO.getAllProjects(config).toArray();
			TranslationProject project = (TranslationProject)JOptionPane.showInputDialog(
								null,
			                    "Select a project:",
			                    "Project selection", JOptionPane.QUESTION_MESSAGE,
			                    null,
			                    options,
			                    options[0]);
			
			I_GetConceptData issueRepoConcept =  null;
			
			if (project != null) {
				issueRepoConcept =  project.getSourceIssueRepo();
			}

			if (issueRepoConcept == null) {
				JOptionPane.showMessageDialog(null,
						"Problem getting repository concept.", "Error",
						JOptionPane.ERROR_MESSAGE);
				throw new TaskFailedException(
						"Problem getting repository concept.");
			}

			I_GetConceptData problemConcept = config.getHierarchySelection();

			IssueRepository sourceDefectsRepository = IssueRepositoryDAO
					.getIssueRepository(issueRepoConcept);

			if (issueRepoConcept == null) {
				JOptionPane.showMessageDialog(null,
						"Problem getting repository.", "Error",
						JOptionPane.ERROR_MESSAGE);
				throw new TaskFailedException("Problem getting repository.");
			}

			int n = JOptionPane.showConfirmDialog(null,
					"<html><body>You are about to create a source defect issue for the concept<br>"
							+ problemConcept.toString() + "<br>Are you sure?",
					"Confirmation", JOptionPane.YES_NO_OPTION);

			if (n == JOptionPane.YES_OPTION) {

				String issueTitle = "Source defect on concept: "
						+ problemConcept.toString() + " " + timeStamp;
				Issue newIssue = new Issue();

				newIssue.setCategory("");
				newIssue.setComponent(problemConcept.toString());
				newIssue.setComponentId(problemConcept.getUids().iterator()
						.next().toString());
				newIssue.setExternalUser(config.getUsername());
				newIssue.setDescription("No description.");
				String str= new MultiLineInputDialog().showInputDialog("Enter a description for the new Isuue:");
				if (str != null) {
					newIssue.setDescription(str);
				}
				newIssue.setPriority("0");
				newIssue.setProjectId("");
				newIssue.setWorkflowStatus("");
				newIssue.setTitle(issueTitle);
				newIssue.setUser(config.getUsername());
				newIssue.setFieldMap(new HashMap<String, Object>());
				newIssue.setDownloadStatus("Open");

				IssueDAO jwto = new IssueDAO();
				newIssue = jwto.createIssue(sourceDefectsRepository, newIssue);
				JOptionPane.showMessageDialog(null,
						"Issue succesfully created.", "Success",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null,
						"Issue creation cancelled.", "Cancelled",
						JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Condition.CONTINUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.
	 * I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public String getProfilePropName() {
		return profilePropName;
	}

	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}

}