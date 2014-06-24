package org.ihtsdo.workflow;

import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.api.wf2.implementation.WfProcessDefinition;
import org.ihtsdo.project.workflow.api.wf2.implementation.WorkflowStore;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.workflow.api.WfProcessDefinitionBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WorkflowStoreBI;

public class WorkflowHelper2 {

	private static WorkflowChangeListener workflowChangeListener;
	private static WorkflowStoreBI wfStore;
	private static ConceptChronicleBI rootConcept;

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

			if (rootConcept == null) {
				rootConcept = Ts.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			}

			if (Ts.get().isKindOf(concept.getConceptNid(), rootConcept.getConceptNid(), config.getViewCoordinate())) {

				I_GetConceptData worklistConcept = config.getDefaultWorkflowForChangedConcept();

				System.out.println("worklistConcept: " + worklistConcept);

				if (worklistConcept==null){
					return false;
				}
				if (wfStore==null){
					wfStore=new WorkflowStore();
				}
				WorkList workList = (WorkList) wfStore.getWorklist(worklistConcept.getPrimUuid());
				WfProcessInstanceBI instance = wfStore.getProcessInstance(workList, concept.getPrimUuid());

				System.out.println("instance: " + instance);

				if (instance!=null){
					System.out.println("instance.isActive(): " + instance.isActive());
					System.out.println("instance.isCompleted(): " + instance.isCompleted());
					if (instance.isActive() && !instance.isCompleted()){
						return false;
					}
				}
				workList.createInstanceForComponent(concept.getPrimUuid(), new WfProcessDefinition(workList.getWorkflowDefinition()));
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
