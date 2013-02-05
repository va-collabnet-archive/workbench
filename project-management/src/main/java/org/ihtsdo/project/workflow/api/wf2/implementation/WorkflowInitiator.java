package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CancellationException;

import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WorkflowInitiatiorBI;
import org.ihtsdo.tk.workflow.api.WorkflowStoreBI;

public class WorkflowInitiator implements WorkflowInitiatiorBI {
	
	public static NidSet alreadySeen;
	private static ConceptChronicleBI rootConcept;
	private static WorkflowStoreBI wfStore;
	private PropertyChangeEvent myEvt;

	public WorkflowInitiator() {
		alreadySeen = new NidSet();
	}

	@Override
	public boolean evaluateForWorkflowInitiation(
			PropertyChangeEvent event) throws Exception {
		myEvt = event;
		
		SwingUtilities.invokeLater(new Runnable() {
			synchronized
			public void run() {
				ConceptChronicleBI concept=null;
				try {
					I_RepresentIdSet idSet=(I_RepresentIdSet)myEvt.getNewValue();
					if (idSet!=null){
						NidBitSetItrBI possibleItr = idSet.iterator();

						while (possibleItr.next()) {
							if (!alreadySeen.contains(possibleItr.nid())) {
								alreadySeen.add(possibleItr.nid());
								concept=Ts.get().getConcept(possibleItr.nid());
								if (concept!=null){
									System.out.println("Sending to workflow: " + concept.toString());
									addComponentToDefaultWorklist(concept);
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (CancellationException e) {
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return true;
	}
	
	private static boolean addComponentToDefaultWorklist(
			ConceptChronicleBI concept) {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();

			if (rootConcept == null) {
				rootConcept = Ts.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			}

			if (Ts.get().isKindOf(concept.getConceptNid(), rootConcept.getConceptNid(), config.getViewCoordinate())) {

				I_GetConceptData worklistConcept = config.getDefaultWorkflowForChangedConcept();

				//System.out.println("worklistConcept: " + worklistConcept);

				if (worklistConcept==null){
					return false;
				}
				if (wfStore==null){
					wfStore=new WorkflowStore();
				}
				WorkList workList = (WorkList) wfStore.getWorklist(worklistConcept.getPrimUuid());
				WfProcessInstanceBI instance = wfStore.getProcessInstance(workList, concept.getPrimUuid());

				//System.out.println("instance: " + instance);

				if (instance!=null){
					//System.out.println("instance.isActive(): " + instance.isActive());
					//System.out.println("instance.isCompleted(): " + instance.isCompleted());
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
			AceLog.getAppLog().alertAndLogException(e);
		}
		return false;
	}

}
