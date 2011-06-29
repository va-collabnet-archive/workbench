package org.ihtsdo.workflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.I_Work;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.workflow.WorkflowHandlerBI;
import org.ihtsdo.tk.api.workflow.WorkflowHistoryJavaBeanBI;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetSearcher;
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

			List<WorkflowHistoryJavaBean> possibleActions = searchForPossibleActions(modeler, concept);
			
			for (int i = 0; i < possibleActions.size();i++) {
				retSet.add(possibleActions.get(i));
			}
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

	private static List<WorkflowHistoryJavaBean> searchForPossibleActions(I_GetConceptData modeler, I_GetConceptData concept) throws Exception
	{
		EditorCategoryRefsetSearcher categegorySearcher = new EditorCategoryRefsetSearcher();
        ArrayList<WorkflowHistoryJavaBean> retList = new ArrayList<WorkflowHistoryJavaBean>();

		// Get Editor Category by modeler and Concept
        I_GetConceptData category = categegorySearcher.searchForCategoryForConceptByModeler(modeler, concept);
        if (category == null) {
            return new ArrayList<WorkflowHistoryJavaBean>();
        }

        int categoryNid = category.getConceptNid();

		// Get Current WF Status for Concept
        WorkflowHistoryJavaBean latestBean = WorkflowHelper.getLatestWfHxJavaBeanForConcept(concept);

        if ((latestBean != null) && (!WorkflowHelper.getAcceptAction().equals(latestBean.getAction())))
	    {
	        // Get Possible Next Actions to Next State Map from Editor Category and Current WF's useCase and state (which now will mean INITIAL-State)
	        int initialStateNid = Terms.get().uuidToNative(latestBean.getState());
	        StateTransitionRefsetSearcher stateTransitionSearcher = new StateTransitionRefsetSearcher();
	        Map<I_GetConceptData, I_GetConceptData> actionMap = stateTransitionSearcher.searchForPossibleActionsAndFinalStates(categoryNid, initialStateNid);


	        // Create Beans for future update.  Only differences in Beans will be action & state (which now will mean NEXT-State)
	        for (I_GetConceptData key : actionMap.keySet())
	        {
	        	// Such as done via Commit
	    		WorkflowHistoryJavaBean templateBean = new WorkflowHistoryJavaBean();

	            templateBean.setConcept(latestBean.getConcept());
	            templateBean.setWorkflowId(latestBean.getWorkflowId());
	            templateBean.setFSN(latestBean.getFSN());
	            templateBean.setModeler(latestBean.getModeler());
	            templateBean.setPath(latestBean.getPath());
	            templateBean.setAction(key.getUids().get(0));
	            templateBean.setState(actionMap.get(key).getUids().get(0));
	            templateBean.setEffectiveTime(latestBean.getEffectiveTime());
	            templateBean.setWorkflowTime(latestBean.getWorkflowTime());
	            templateBean.setOverridden(latestBean.getOverridden());
	            templateBean.setAutoApproved(latestBean.getAutoApproved());
	            
	            retList.add(templateBean);
	        }
        }

        return retList;
    }

}
