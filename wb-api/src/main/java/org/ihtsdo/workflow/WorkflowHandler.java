package org.ihtsdo.workflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.workflow.WorkflowHandlerBI;
import org.ihtsdo.tk.api.workflow.WorkflowHistoryJavaBeanBI;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WorkflowHandler implements WorkflowHandlerBI {

	@Override
	public Collection<UUID> getAllAvailableWorkflowActionUids() 
	{
		try {
			return Terms.get().getActiveAceFrameConfig().getAllAvailableWorkflowActionUids();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error retrieving ActiveAceFrameConfig: ", e);
		}
		
		return null;
	}

	@Override
	public Collection<? extends WorkflowHistoryJavaBeanBI> getAvailableWorkflowActions(ConceptVersionBI conVer) throws IOException, ContraditionException {
		
		EditorCategoryRefsetSearcher searcher = null;		
		List<WorkflowHistoryJavaBean> retSet = new ArrayList<WorkflowHistoryJavaBean>();
		
		try {
			searcher = new EditorCategoryRefsetSearcher();

            I_GetConceptData modeler = WorkflowHelper.getCurrentModeler();
            I_GetConceptData concept = Terms.get().getConcept(conVer.getConceptNid());

			List<WorkflowHistoryJavaBean> possibleActions = WorkflowHelper.searchForPossibleActions(modeler, concept);
			
			for (int i = 0; i < possibleActions.size();i++) {
				retSet.add(possibleActions.get(i));
			}
			
	        // Get config
	        final I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
	        
	        // Get Worker
	        final I_Work worker;
	        if (config.getWorker().isExecuting()) {
	            worker = config.getWorker().getTransactionIndependentClone();
	        } else {
	            worker = config.getWorker();
	        }
	        
	        // Set bean
	        worker.writeAttachment(ProcessAttachmentKeys.POSSIBLE_WF_ACTIONS_LIST.name(), possibleActions);
		} catch (Exception e) {
			throw new IOException("Unable to search for possible Actions", e);
		}

		return retSet;
	}

	@Override
	public boolean hasAction(Collection<? extends WorkflowHistoryJavaBeanBI> beans,ConceptSpec action) 
		throws IOException, ContraditionException 
	{
		for (WorkflowHistoryJavaBeanBI bean : beans)
		{
			if (bean.getAction().equals(action.getUuids()[0]))
				return true;
		}
		
		return false;
	}

	@Override
	public boolean isActiveAction(
			Collection<? extends WorkflowHistoryJavaBeanBI> possibleActions, UUID action) 
	{
			for (WorkflowHistoryJavaBeanBI bean : possibleActions)
			{
				if (bean.getAction().equals(action))
					return true;
			}

 		return false;
	}

}
