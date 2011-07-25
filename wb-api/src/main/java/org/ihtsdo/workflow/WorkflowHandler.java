package org.ihtsdo.workflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.workflow.WorkflowHandlerBI;
import org.ihtsdo.tk.api.workflow.WorkflowHistoryJavaBeanBI;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetSearcher;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WorkflowHandler implements WorkflowHandlerBI {

	@Override
	public List<UUID> getAllAvailableWorkflowActionUids() 
	{
		try {
			return Terms.get().getActiveAceFrameConfig().getAllAvailableWorkflowActionUids();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error retrieving ActiveAceFrameConfig: ", e);
		}
		
		return null;
	}

	@Override
	public Collection<? extends WorkflowHistoryJavaBeanBI> getAvailableWorkflowActions(ConceptVersionBI concept, ViewCoordinate vc) throws IOException, ContraditionException {
		
		EditorCategoryRefsetSearcher searcher = null;		
		List<WorkflowHistoryJavaBean> retSet = new ArrayList<WorkflowHistoryJavaBean>();
		
		try {
            ConceptVersionBI modeler = WorkflowHelper.getCurrentModeler();

			List<WorkflowHistoryJavaBean> possibleActions = searchForPossibleActions(modeler, concept, vc);
			
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

	private static List<WorkflowHistoryJavaBean> searchForPossibleActions(ConceptVersionBI modeler, ConceptVersionBI concept, ViewCoordinate vc) throws Exception
	{
        ArrayList<WorkflowHistoryJavaBean> retList = new ArrayList<WorkflowHistoryJavaBean>();

		// Get Editor Category by modeler and Concept
        EditorCategoryRefsetSearcher categegorySearcher = new EditorCategoryRefsetSearcher();

        ConceptVersionBI category = categegorySearcher.searchForCategoryForConceptByModeler(modeler, concept, vc);
        if (category == null) {
            return new ArrayList<WorkflowHistoryJavaBean>();
        }

        int categoryNid = category.getConceptNid();

		// Get Current WF Status for Concept
        WorkflowHistoryJavaBean latestBean = WorkflowHelper.getLatestWfHxJavaBeanForConcept(Terms.get().getConcept(concept.getConceptNid()));

        if ((latestBean != null) && (!WorkflowHelper.getAcceptAction().equals(latestBean.getAction())))
	    {
	        // Get Possible Next Actions to Next State Map from Editor Category and Current WF's useCase and state (which now will mean INITIAL-State)
	        int initialStateNid = Terms.get().uuidToNative(latestBean.getState());
	        StateTransitionRefsetSearcher stateTransitionSearcher = new StateTransitionRefsetSearcher();
	        Map<UUID, UUID> actionMap = stateTransitionSearcher.searchForPossibleActionsAndFinalStates(categoryNid, initialStateNid, vc);


	        // Create Beans for future update.  Only differences in Beans will be action & state (which now will mean NEXT-State)
	        for (UUID key : actionMap.keySet())
	        {
	        	// Such as done via Commit
	    		WorkflowHistoryJavaBean templateBean = new WorkflowHistoryJavaBean();

	            templateBean.setConcept(latestBean.getConcept());
	            templateBean.setWorkflowId(latestBean.getWorkflowId());
	            templateBean.setFSN(latestBean.getFSN());
	            templateBean.setModeler(latestBean.getModeler());
	            templateBean.setPath(latestBean.getPath());
	            templateBean.setAction(key);
	            templateBean.setState(actionMap.get(key));
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
