package org.dwfa.ace.task.data.checks;

import java.util.Collection;

import javax.swing.JList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.RefsetOverlapValidator;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;


@BeanList(specs = { @Spec(directory = "tasks/ace/data checks", type = BeanType.TASK_BEAN) })
public class VerifyRefsetMemberOverlaps extends AbstractTask{
		

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
					.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
			
			I_TermFactory termFactory = LocalVersionedTerminology.get();
						
			RefsetOverlapValidator rov = new RefsetOverlapValidator();
			rov.validate();
						
			if(rov.hasOverlaps()){
				/*
				 * Add members to list
				 */
				JList conceptList = config.getBatchConceptList();
				I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
				model.clear();
				for(Integer i : rov.getOverlapedMemberComopnentIds()){
					model.addElement(termFactory.getConcept(i.intValue()));
				}//End for loop	
			}//End if
			
			config.showListView();

			return Condition.CONTINUE;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		} 		
	}//End method evaluate
	
	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do...
	}//End method complete
	
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}//End methos getConditions

	public int[] getDataContainerIds() {
		return new int[] {};
	}//End method getDataContainerIds
	
}//End class VerifyRefsetMemberOverlaps