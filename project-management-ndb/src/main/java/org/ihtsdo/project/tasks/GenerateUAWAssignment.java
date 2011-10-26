/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;

import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceItemFilter;
import net.jini.lookup.entry.Name;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.ProcessID;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.PromotionRefset;

/**
 * 
 * @author ALO
 * @version 1.0, June 2010
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN) })
public class GenerateUAWAssignment extends AbstractTask {

	/*
	 * -----------------------
	 * Properties
	 * -----------------------
	 */
	// Serialization Properties
	private static final long serialVersionUID = 1L;
	private static final int dataVersion = 2;

	// Task Attribute Properties
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	private String memberPropName = ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey();
	private String processPropName = ProcessAttachmentKeys.PROCESS_TO_LAUNCH.getAttachmentKey();
	private String translatorInboxPropName = ProcessAttachmentKeys.TRANSLATOR_ROLE_INBOX.getAttachmentKey();
	private String reviewer1InboxPropName = ProcessAttachmentKeys.REVIEWER_1_ROLE_INBOX.getAttachmentKey();
	private String reviewer2InboxPropName = ProcessAttachmentKeys.REVIEWER_2_ROLE_INBOX.getAttachmentKey();
	private String smeInboxPropName = ProcessAttachmentKeys.SME_ROLE_INBOX.getAttachmentKey();
	private String editorialBoardInboxPropName = ProcessAttachmentKeys.EDITORIAL_BOARD_ROLE_INBOX.getAttachmentKey();

	// Other Properties
	private I_TermFactory termFactory;

	/*
	 * -----------------------
	 * Serialization Methods
	 * -----------------------
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
		out.writeObject(memberPropName);
		out.writeObject(processPropName);
		out.writeObject(translatorInboxPropName);
		out.writeObject(reviewer1InboxPropName);
		out.writeObject(reviewer2InboxPropName);
		out.writeObject(smeInboxPropName);
		out.writeObject(editorialBoardInboxPropName);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();

		if (objDataVersion <= dataVersion) {
			if (objDataVersion >= 1) {
				// Read version 1 data fields...
				profilePropName = (String) in.readObject();
				memberPropName = (String) in.readObject();
				processPropName = (String) in.readObject();
				translatorInboxPropName = (String) in.readObject();
				reviewer1InboxPropName = (String) in.readObject();
				reviewer2InboxPropName = (String) in.readObject();
				smeInboxPropName = (String) in.readObject();
				editorialBoardInboxPropName = (String) in.readObject();
			}
			// Initialize transient properties
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	/**
	 * Handles actions required by the task after normal task completion (such as moving a
	 * process to another user's input queue).
	 * 
	 * @return void
	 * @param process The currently executing Workflow process
	 * @param worker The worker currently executing this task
	 * @exception TaskFailedException Thrown if a task fails for any reason.
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do
	}

	/**
	 * Performs the primary action of the task, which in this case is to gather and
	 * validate data that has been entered by the user on the Workflow Details Sheet.
	 * 
	 * @return The exit condition of the task
	 * @param process The currently executing Workflow process
	 * @param worker The worker currently executing this task
	 * @exception TaskFailedException Thrown if a task fails for any reason.
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

		try {

			termFactory = Terms.get();
			// TODO: move to read from profile

			I_GetConceptData enRefset=null;
			try {
				enRefset=Terms.get().getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_EN.getUids());
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			WorkListMember workListMember = (WorkListMember) process.getProperty(memberPropName);
			String destination = (String) process.getProperty(translatorInboxPropName);

			WorkList worklist = TerminologyProjectDAO.getWorkList(
					termFactory.getConcept(workListMember.getWorkListUUID()), config);
			I_TerminologyProject project = TerminologyProjectDAO.getProjectForWorklist(worklist, config);

			List<I_GetConceptData> souLanRefsets = ((TranslationProject)project).getSourceLanguageRefsets();
			Integer langRefset = null;
			for (I_GetConceptData lCon:souLanRefsets){
				if (lCon.getConceptNid()==enRefset.getConceptNid()){
					langRefset=lCon.getConceptNid();
					break;
				}
			}
			if (langRefset==null && souLanRefsets!=null){
				langRefset=souLanRefsets.get(0).getConceptNid();
			}

			int statusId = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_DELIVERED_STATUS.getUids()).getConceptNid();

			PromotionRefset promoRefset = worklist.getPromotionRefset(config);
			ServiceID serviceID = null;
			Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
			String queueName = config.getUsername().trim() + ".outbox";
			Entry[] attrSetTemplates = new Entry[] { new Name(queueName) };
			ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
			ServiceItemFilter filter = null;
			ServiceItem service = null;
			try {
				service = worker.lookup(template, filter);
			} catch (ConfigurationException e1) {
				e1.printStackTrace();
			}
			if (service == null) {
				throw new TaskFailedException("No queue with the specified name could be found: "
						+ "OUTBOX");
			}
			I_QueueProcesses q = (I_QueueProcesses) service.service;
			I_EncodeBusinessProcess wfProcess=(I_EncodeBusinessProcess)worklist.getBusinessProcess();
			wfProcess.setDestination(destination);
			wfProcess.setProperty(translatorInboxPropName, process.getProperty(translatorInboxPropName));
			wfProcess.setProperty(reviewer1InboxPropName, process.getProperty(reviewer1InboxPropName));
			wfProcess.setProperty(reviewer2InboxPropName, process.getProperty(reviewer2InboxPropName));
			wfProcess.setProperty(smeInboxPropName, process.getProperty(smeInboxPropName));
			wfProcess.setProperty(editorialBoardInboxPropName, process.getProperty(editorialBoardInboxPropName));

			workListMember.setActivityStatus(
					ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_DELIVERED_STATUS.getUids().iterator().next());
			TerminologyProjectDAO.updateWorkListMemberMetadata(workListMember, config);
			termFactory.commit();
			wfProcess.writeAttachment(ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey(), workListMember);
			Long statusTime=promoRefset.getLastStatusTime(workListMember.getId(), config);
			
			String subj= TerminologyProjectDAO.getItemSubject(workListMember,worklist,project, promoRefset, langRefset, statusId, statusTime);

			wfProcess.setSubject(subj);	
			wfProcess.setSubject(workListMember.getConcept().toString());
			wfProcess.setProcessID(new ProcessID(UUID.randomUUID()));
			worker.getLogger().info(
					"Moving process " + wfProcess.getProcessID() + " to Queue named: " + queueName);
			q.write(wfProcess, worker.getActiveTransaction());
			worker.commitTransactionIfActive();
			worker.getLogger()
			.info("Moved process " + wfProcess.getProcessID() + " to queue: " + q.getNodeInboxAddress());
			//TerminologyProjectDAO.promoteLanguageContent(workListMember, config);
			JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
					"Assignment delivered!", "", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TaskFailedException(e.getMessage());
		}
		return Condition.CONTINUE;
	}

	/**
	 * This method overrides: getDataContainerIds() in AbstractTask
	 * 
	 * @return The data container identifiers used by this task.
	 */
	public int[] getDataContainerIds() {
		return new int[] {};
	}

	/**
	 * This method implements the interface method specified by: getConditions() in I_DefineTask
	 * 
	 * @return The possible evaluation conditions for this task.
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public String getProfilePropName() {
		return profilePropName;
	}

	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}

	public String getMemberPropName() {
		return memberPropName;
	}

	public void setMemberPropName(String memberPropName) {
		this.memberPropName = memberPropName;
	}

	public String getProcessPropName() {
		return processPropName;
	}

	public void setProcessPropName(String processPropName) {
		this.processPropName = processPropName;
	}

	public String getTranslatorInboxPropName() {
		return translatorInboxPropName;
	}

	public void setTranslatorInboxPropName(String translatorInboxPropName) {
		this.translatorInboxPropName = translatorInboxPropName;
	}

	public String getReviewer1InboxPropName() {
		return reviewer1InboxPropName;
	}

	public void setReviewer1InboxPropName(String reviewer1InboxPropName) {
		this.reviewer1InboxPropName = reviewer1InboxPropName;
	}

	public String getReviewer2InboxPropName() {
		return reviewer2InboxPropName;
	}

	public void setReviewer2InboxPropName(String reviewer2InboxPropName) {
		this.reviewer2InboxPropName = reviewer2InboxPropName;
	}

	public String getSmeInboxPropName() {
		return smeInboxPropName;
	}

	public void setSmeInboxPropName(String smeInboxPropName) {
		this.smeInboxPropName = smeInboxPropName;
	}

	public String getEditorialBoardInboxPropName() {
		return editorialBoardInboxPropName;
	}

	public void setEditorialBoardInboxPropName(String editorialBoardInboxPropName) {
		this.editorialBoardInboxPropName = editorialBoardInboxPropName;
	}
}