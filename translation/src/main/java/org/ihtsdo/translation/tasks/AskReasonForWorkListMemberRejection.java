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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.refset.CommentsRefset;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.translation.CommentPopUpDialog;

/**
 * The Class.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class AskReasonForWorkListMemberRejection extends AbstractTask {

	/** The Constant HEADER_SEPARATOR. */
	private static final String HEADER_SEPARATOR = " // ";
	
	/** The Constant COMMENT_HEADER_SEP. */
	private static final String COMMENT_HEADER_SEP = ": -";

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
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		I_ConfigAceFrame config;
		I_TermFactory tf = Terms.get();
		try {
			config=(I_ConfigAceFrame)Terms.get().getActiveAceFrameConfig();
			System.out.println("Config null?" + (config==null));
			WfInstance instance = (WfInstance) process.readAttachement("WfInstance");
			WfRole wfrole = (WfRole) process.readAttachement("WfRole");
			String comment = (String) process.readAttachement("BatchMessage");
			I_GetConceptData concept=Terms.get().getConcept(instance.getComponentId());
			
			WorkList workList = instance.getWorkList();
			
			CommentsRefset commentsRefset = workList.getCommentsRefset(config);
			I_GetConceptData commentType = tf.getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_REJECTION.getPrimoridalUid());
			HashMap<I_GetConceptData,String> reason =  new HashMap<I_GetConceptData,String>();
			if(comment != null && !comment.trim().equals("")){
				reason.put(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.OTHER_REASON_REJECTION.localize().getNid()), comment);
			}else{
				reason = new CommentPopUpDialog("Enter reason for rejection", commentType).showDialog();
			}
			if(reason!= null && !reason.isEmpty()){
				Set<I_GetConceptData> a = reason.keySet();
				I_GetConceptData rejReason = a.iterator().next();
				String fullName= config.getDbConfig().getFullName();
				String commentx = wfrole.getName() + HEADER_SEPARATOR + fullName + COMMENT_HEADER_SEP + reason.get(rejReason);
				commentsRefset.addComment(concept.getNid(), commentType.getNid() , rejReason.getNid(),commentx);
				
			}else{
				return Condition.ITEM_CANCELED;
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
		List<Condition> posibleConditions = new ArrayList<Condition>();
		posibleConditions.add(Condition.CONTINUE);
		posibleConditions.add(Condition.ITEM_CANCELED);
		return posibleConditions;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {  };
	}
	

}