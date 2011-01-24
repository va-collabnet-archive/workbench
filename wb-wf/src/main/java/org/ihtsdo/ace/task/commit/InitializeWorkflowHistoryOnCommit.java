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
package org.ihtsdo.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.commit.AbstractConceptTest;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetSearcher;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetSearcher;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;


/*
* @author Jesse Efron
*
*/
@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class InitializeWorkflowHistoryOnCommit extends AbstractConceptTest {

    private static final long serialVersionUID = 1;
    private static final int DATA_VERSION = 1;
    private static final String ALERT_MESSAGE = "<html>Empty value found:<br><font color='blue'>%1$s</font><br>Please enter a value before commit...";


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(DATA_VERSION);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion != DATA_VERSION) {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public List<AlertToDataConstraintFailure> test(I_GetConceptData concept, boolean forCommit)
            throws TaskFailedException {
        try {
            if (!WorkflowHistoryRefsetWriter.isInUse()) // Not in the middle of an existing commit
        	{
            	WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();

            	I_GetConceptData modeler = WorkflowHelper.getCurrentModeler();

            	if (modeler != null && WorkflowHelper.isActiveModeler(modeler))
            	{
            		I_TermFactory tf = getTermFactory();
            		WorkflowHistoryRefsetWriter writer = new WorkflowHistoryRefsetWriter();

					WorkflowHistoryRefsetWriter.lockMutex();

					// Path
		            writer.setPathUid(Terms.get().nidToUuid(concept.getConceptAttributes().getPathNid()));

		            // Modeler
		            writer.setModelerUid(WorkflowHelper.getCurrentModeler().getPrimUuid());

		            // Concept & FSN
		            writer.setConceptUid(concept.getUids().iterator().next());
		            writer.setFSN(WorkflowHelper.identifyFSN(concept));

		            // Use Case (Deprecated)
	            	writer.setUseCaseUid(ArchitectonicAuxiliary.Concept.WORKFLOW_CONCEPTS.getPrimoridalUid());

	            	// Action
	            	UUID actionUid = identifyAction();
	                writer.setActionUid(actionUid);

	                // State
	                UUID initialState = identifyNextState(writer.getModelerUid(), concept, actionUid);
	                writer.setStateUid(initialState);

	                // Worfklow Id
	                WorkflowHistoryJavaBean latestBean = searcher.getLatestWfHxJavaBeanForConcept(concept);
		            if (!isConceptInCurrentWorkflow(latestBean))
		            	writer.setWorkflowUid(UUID.randomUUID());
		            else
		            	writer.setWorkflowUid(latestBean.getWorkflowId());

		            // Set auto approved based on AceFrameConfig setting
		            if (tf.getActiveAceFrameConfig().isAutoApproveOn()) {
		            	writer.setAutoApproved(true);

		            	// Identify and overwrite Accept Action
		            	UUID acceptActionUid = identifyAcceptAction();
		            	writer.setActionUid(acceptActionUid);

		            	// Identify and overwrite Next State
		            	UUID nextState = identifyNextState(writer.getModelerUid(), concept, acceptActionUid);
						writer.setStateUid(nextState);
		            } else
		            	writer.setAutoApproved(false);

		            // Override
		            writer.setOverride(tf.getActiveAceFrameConfig().isOverrideOn());

		            // TimeStamps
			        java.util.Date today = new java.util.Date();
			        writer.setTimeStamp(today.getTime());
			        writer.setRefsetColumnTimeStamp(today.getTime());

			        // Write Member
					WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
					writer.addMember();
			        Terms.get().addUncommitted(refset.getRefsetConcept());
				}
            }

            // return alerts;
            return new ArrayList<AlertToDataConstraintFailure>();
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

	private UUID identifyAction() {
    	UUID commitActionUid = null;
    	try
    	{
	    	for (I_GetConceptData action : Terms.get().getActiveAceFrameConfig().getWorkflowActions())
	    	{
	    		List<? extends I_RelTuple> relList = WorkflowHelper.getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE);

	    		for (I_RelTuple rel : relList)
	    		{
	    			if (rel != null &&
	        			rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_BEGIN_WF_CONCEPT.getPrimoridalUid()).getConceptNid())
    {
	    				List<? extends I_RelTuple> commitRelList = WorkflowHelper.getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_VALUE);

	    	    		for (I_RelTuple commitRel : commitRelList)
	    	    		{
							if (commitRel != null &&
								commitRel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_SINGLE_COMMIT.getPrimoridalUid()).getConceptNid())
    {
								commitActionUid = action.getPrimUuid();
							}
	    	    		}

	    	    		if (commitActionUid != null)
	    	    			break;
	    			}
				}
	    	}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	return commitActionUid;
	}

	private I_TermFactory getTermFactory() {
        return Terms.get();
    }

    private boolean isConceptInCurrentWorkflow(WorkflowHistoryJavaBean latestBean) throws Exception
    {
    	if (latestBean == null)
    		return false;
    	else
    	{
    		I_GetConceptData action = Terms.get().getConcept(latestBean.getAction());

    		List<? extends I_RelTuple> relList = WorkflowHelper.getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE);

    		for (I_RelTuple rel : relList)
    {
    			if (rel != null &&
    				rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION.getPrimoridalUid()).getConceptNid())
    				return false;
    		}
    	}

    		return true;
    }


	private UUID identifyNextState(UUID modelerUid, I_GetConceptData concept, UUID commitActionUid)
    		{
		I_GetConceptData initialState = null;
        boolean existsInDb = isConceptInDatabase(concept);

		try {
        	WorkflowHistoryRefsetSearcher wfSearcher = new WorkflowHistoryRefsetSearcher();

			if (wfSearcher.getTotalCountByConcept(concept) > 0)
				initialState = Terms.get().getConcept(wfSearcher.getLatestWfHxJavaBeanForConcept(concept).getState());
			else
    	{
				for (I_GetConceptData state : Terms.get().getActiveAceFrameConfig().getWorkflowStates())
    		{
					List<? extends I_RelTuple> relList = WorkflowHelper.getWorkflowRelationship(state, ArchitectonicAuxiliary.Concept.WORKFLOW_USE_CASE);

		    		for (I_RelTuple rel : relList)
    	{
		    			if (rel != null &&
							((existsInDb && (rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_EXISTING_CONCEPT.getPrimoridalUid()).getConceptNid())) ||
							 (!existsInDb && (rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_NEW_CONCEPT.getPrimoridalUid()).getConceptNid()))))
    		{
							initialState = state;
    		}
    	}

		    		if (initialState != null)
		    			break;
    		}
    	}

			EditorCategoryRefsetSearcher categorySearcher = new EditorCategoryRefsetSearcher();
			I_GetConceptData modeler = Terms.get().getConcept(modelerUid);
			I_GetConceptData category = categorySearcher.searchForCategoryForConceptByModeler(modeler, concept);

			StateTransitionRefsetSearcher nextStateSearcher = new StateTransitionRefsetSearcher();
			Map<I_GetConceptData, I_GetConceptData> possibleActions = nextStateSearcher.searchForPossibleActionsAndFinalStates(category.getConceptNid(), initialState.getConceptNid());

			for (I_GetConceptData transitionAction : possibleActions.keySet())
    	{
				if (transitionAction.getPrimUuid().equals(commitActionUid))
					return possibleActions.get(transitionAction).getPrimUuid();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
    	}

	private boolean isConceptInDatabase(I_GetConceptData concept) {
		boolean hasBeenReleased = false;

		try {
			WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();
			int SnomedId = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());

			I_Identify idVersioned = Terms.get().getId(concept.getConceptNid());
	        for (I_IdPart idPart : idVersioned.getMutableIdParts()) {
	            if (idPart.getAuthorityNid() == SnomedId)
	            	hasBeenReleased = true;
	        }

			if (!hasBeenReleased && (searcher.getTotalCountByConcept(concept) == 0))
				return false;
		} catch (Exception e) {
			e.printStackTrace();
    }

    		return true;
    }

	private UUID identifyAcceptAction() {

		try
		{
			for (I_GetConceptData action : Terms.get().getActiveAceFrameConfig().getWorkflowActions())
			{
				List<? extends I_RelTuple> useCaseRel = WorkflowHelper.getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE);

				for (I_RelTuple rel : useCaseRel)
				{
					if (rel != null &&
						rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION.getPrimoridalUid()).getConceptNid())
						return action.getPrimUuid();
				}
			}
		} catch (Exception e ) {
			e.printStackTrace();
		}
		return null;
    }

}
