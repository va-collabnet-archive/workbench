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
import java.util.HashMap;
import java.util.Set;

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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.CommentsRefset;
import org.ihtsdo.translation.CommentPopUpDialog;

/**
 * The Class
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class AskReasonForWorkListMemberRejection extends AbstractTask {

	private static final String HEADER_SEPARATOR = " // ";
	private static final String COMMENT_HEADER_SEP = ": -";
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	private String worklistMemberPropName = ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey();

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The step role. */
	private TermEntry stepRole;
	
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
		out.writeObject(worklistMemberPropName);
		out.writeObject(stepRole);
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
			worklistMemberPropName = (String) in.readObject();
			stepRole = (TermEntry) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		I_ConfigAceFrame config;
		WorkListMember workListMember;
		I_TermFactory tf = Terms.get();
		try {
			config=(I_ConfigAceFrame)Terms.get().getActiveAceFrameConfig();
			System.out.println("Config null?" + (config==null));
			workListMember = (WorkListMember) process.readAttachement(getWorklistMemberPropName());
			
			WorkList workList = TerminologyProjectDAO.getWorkList(tf.getConcept(workListMember.getWorkListUUID()), config);
			
			CommentsRefset commentsRefset = workList.getCommentsRefset(config);
			I_GetConceptData commentType = tf.getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_REJECTION.getPrimoridalUid());
			HashMap<I_GetConceptData,String> reason = new CommentPopUpDialog("Enter reason for rejection", commentType).showDialog();
			if(reason!= null && !reason.isEmpty()){
				Set<I_GetConceptData> a = reason.keySet();
				I_GetConceptData rejReason = a.iterator().next();
				String fullName= config.getDbConfig().getFullName();
				I_GetConceptData role=Terms.get().getConcept(stepRole.ids);
				String comment = role + HEADER_SEPARATOR + fullName + COMMENT_HEADER_SEP + reason.get(rejReason);
				commentsRefset.addComment(workListMember.getId(), commentType.getNid() , rejReason.getNid(),comment);
				
			}

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
	 * Gets the step role.
	 * 
	 * @return the step role
	 */
	public TermEntry getStepRole() {
		return stepRole;
	}

	/**
	 * Sets the step role.
	 * 
	 * @param stepRole the new step role
	 */
	public void setStepRole(TermEntry stepRole) {
		this.stepRole = stepRole;
	}

	public String getProfilePropName() {
		return profilePropName;
	}

	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}

	public String getWorklistMemberPropName() {
		return worklistMemberPropName;
	}

	public void setWorklistMemberPropName(String worklistMemberPropName) {
		this.worklistMemberPropName = worklistMemberPropName;
	}
}