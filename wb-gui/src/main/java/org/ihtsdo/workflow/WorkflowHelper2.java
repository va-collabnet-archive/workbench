package org.ihtsdo.workflow;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.api.wf2.implementation.WorkflowStore;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.workflow.api.WfProcessDefinitionBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WorkflowStoreBI;

public class WorkflowHelper2 {

	private static WorkflowChangeListener workflowChangeListener;
	private static WorkflowStoreBI wfStore;

	public static WorkflowChangeListener getWorkflowListener(){

		if (workflowChangeListener!=null){
			return workflowChangeListener; 
		}
		workflowChangeListener=new WorkflowChangeListener();
		return workflowChangeListener;
	}

	public static boolean addComponentToDefaultWorklist(
			ConceptChronicleBI concept) {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();

			I_GetConceptData worklistConcept = config.getDefaultWorkflowForChangedConcept();

			if (worklistConcept==null){
				return false;
			}
			if (wfStore==null){
				wfStore=new WorkflowStore();
			}
			WorkList workList = (WorkList) wfStore.getWorklist(worklistConcept.getPrimUuid());
			WfProcessInstanceBI instance = wfStore.getProcessInstance(workList, concept.getPrimUuid());
			if (instance!=null){
				if (instance.isActive() && !instance.isCompleted()){
					return false;
				}
			}
			workList.createInstanceForComponent(concept.getPrimUuid(), (WfProcessDefinitionBI) workList.getWorkflowDefinition());
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
