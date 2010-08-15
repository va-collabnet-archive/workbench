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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.task.commit.AbstractConceptTest;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.cement.WorkflowAuxiliary;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetSearcher;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetHelper;


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

        	I_TermFactory tf = getTermFactory();
            WorkflowHistoryRefsetWriter writer = new WorkflowHistoryRefsetWriter();
			        	
            if ((isDataChecksPassed()) &&			// Passed Previous Datachecks
        	    (!writer.isInUse() &&				// Not in the middle of an existing commit 
        	     conceptMayBeInitialized(concept)))		// Concept not already in and existing Workflow (if so, don't re-init)
        	{
	            // @ToDo Single Editing path allowed only
	            writer.setPath(tf.getConcept(tf.getActiveAceFrameConfig().getEditingPathSet().iterator().next().getConceptNid()));
	        	
	            String modelerStr = tf.getActiveAceFrameConfig().getUsername();
	            writer.setModeler(WorkflowRefsetHelper.lookupModeler(modelerStr));
	            
	            writer.setConceptId(concept.getUids().iterator().next());
	            writer.setFSN(WorkflowRefsetHelper.identifyFSN(concept));
	            
	            if (isEditUseCase(concept))
	            {
	            	writer.setUseCase(tf.getConcept(WorkflowAuxiliary.Concept.EDIT_USE_CASE.getUids()));
	                writer.setAction(getInitialAction(WorkflowAuxiliary.Concept.EDIT_USE_CASE));
	                writer.setState(getInitialState(WorkflowAuxiliary.Concept.EDIT_USE_CASE));
	                
	            } else {
	            	writer.setUseCase(tf.getConcept(WorkflowAuxiliary.Concept.NEW_USE_CASE.getUids()));
	                writer.setAction(getInitialAction(WorkflowAuxiliary.Concept.NEW_USE_CASE));
	                writer.setState(getInitialState(WorkflowAuxiliary.Concept.NEW_USE_CASE));
	            }
	
				writer.setWorkflowId(UUID.randomUUID());
	            writer.setTimeStamp(getConceptTimeStamp(concept));
				            
				WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
				
				writer.lockMutex();
				writer.addMember();
		        tf.addUncommitted(tf.getConcept(refset.getRefsetConcept()));
				Terms.get().commit();
				writer.unLockMutex();				
			}

            // return alerts;
            return new ArrayList<AlertToDataConstraintFailure>();
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private I_TermFactory getTermFactory() {
        return Terms.get();
    }
    
    private boolean isEditUseCase(I_GetConceptData concept) throws Exception
    {
    	// Other tests go here
    	if (conceptFoundInDatabase(concept))
    		return true;
    	else 
    		return false;
    }
    
    @SuppressWarnings("unchecked")
	private String getConceptTimeStamp(I_GetConceptData concept) throws IOException 
    {

        java.util.Date today = new java.util.Date();

//    	List<I_ConceptAttributePart> parts = (List<I_ConceptAttributePart>) concept.getConceptAttributes().getMutableParts();
//    	 I_ConceptAttributePart latestVersion = parts.get(parts.size() - 1);
    	 
    	 return TimeUtil.getDateFormat().format(today);
    }

    private I_GetConceptData getInitialState(WorkflowAuxiliary.Concept useCase) throws TerminologyException, IOException {
    	if (useCase == WorkflowAuxiliary.Concept.EDIT_USE_CASE)
    		return getTermFactory().getConcept(WorkflowAuxiliary.Concept.CHANGED.getUids());
    	else
    		return getTermFactory().getConcept(WorkflowAuxiliary.Concept.NEW.getUids());
    }

    private I_GetConceptData getInitialAction(WorkflowAuxiliary.Concept useCase) throws TerminologyException, IOException {
    	return getTermFactory().getConcept(WorkflowAuxiliary.Concept.EMPTY.getUids());
    }

    private boolean conceptFoundInDatabase(I_GetConceptData concept) throws Exception 
    {
    	TreeSet<TimePathId> times = (TreeSet<TimePathId>)concept.getConceptAttributes().getTimePathSet();
    	
    	if (times.size() > 1)
    		return true;
    	
    	TimePathId tpId = times.first();
    	
    	for (I_DescriptionVersioned desc : concept.getDescriptions()) 
    	{
    		if (desc.getTimePathSet().size() > 1)
    			return true;
    		else 
    		{
        		TimePathId testTPId = desc.getTimePathSet().iterator().next();
        		
        		if (testTPId.getPathId() != tpId.getPathId() ||
        			testTPId.getTime() != tpId.getTime())
        			return true;
    		}
    	}

    	for (I_RelVersioned srcRels : concept.getSourceRels()) 
    	{
    		if (srcRels.getTimePathSet().size() > 1)
    			return true;
    		else 
    		{
        		TimePathId testTPId = srcRels.getTimePathSet().iterator().next();
        		
        		if (testTPId.getPathId() != tpId.getPathId() ||
        			testTPId.getTime() != tpId.getTime())
        			return true;
    		}
    	}

    	for (I_RelVersioned destRels : concept.getDestRels()) 
    	{
    		if (destRels.getTimePathSet().size() > 1)
    			return true;
    		else 
    		{
        		TimePathId testTPId = destRels.getTimePathSet().iterator().next();
        		
        		if (testTPId.getPathId() != tpId.getPathId() ||
        			testTPId.getTime() != tpId.getTime())
        			return true;
    		}
    	}

    	for (I_ImageVersioned img : concept.getImages()) 
    	{
    		if (img.getTimePathSet().size() > 1)
    			return true;
    		else 
    		{
        		TimePathId testTPId = img.getTimePathSet().iterator().next();
        		
        		if (testTPId.getPathId() != tpId.getPathId() ||
        			testTPId.getTime() != tpId.getTime())
        			return true;
    		}
    	}
    	
    	return false;
    	
    	/*
    	Iterator<I_GetConceptData> itr = Terms.get().getConceptIterator();
    	
    	while (itr.hasNext())
    	{
    		I_GetConceptData con =  (I_GetConceptData)itr.next();
    		
    		if (con.getUids().equals(concept.getUids()))
    			return true;
    	}

    	return false;
    	*/
    	
    	
    //	I_IntSet ids = Terms.get().getConceptNids();
    	
    //	return ids.contains(concept.getConceptNid());
    }
    
    private boolean conceptMayBeInitialized(I_GetConceptData concept) throws Exception
    {
    	WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();
    	
    	SortedSet<WorkflowHistoryJavaBean> beanList = searcher.searchForWFHistory(concept);
    
    	if ((beanList == null) || (beanList.size() == 0))
    		return true;
    	else
	    	return beanList.first().getState().equals(Terms.get().getConcept(WorkflowAuxiliary.Concept.DONE.getUids())); 			
    }

    private boolean isDataChecksPassed() {
/*
 *     	List<AlertToDataConstraintFailure> results = Terms.get().getCommitErrorsAndWarnings();
 
    	
		try {
			for (AlertToDataConstraintFailure dataCheckResult : results) {
				if (dataCheckResult.equals(AlertToDataConstraintFailure.ALERT_TYPE.ERROR))
					return false;
			}
		} catch (Exception e) {
			AceLog.getEditLog().alertAndLogException(e);
		}
*/		
		return true;
    }
}    	

