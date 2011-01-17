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
package org.ihtsdo.ace.task.contradiction;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.PositionBI;



@BeanList(specs = { @Spec(directory = "tasks/c/contradiction", type = BeanType.TASK_BEAN) })
public class TestCR extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }
    
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        
    	try {
    		// Build the Iterator Processor
    		PositionBI viewPos = Terms.get().getActiveAceFrameConfig().getViewPositionSet().iterator().next();
    		ContradictionConceptProcessor processor = new ContradictionConceptProcessor (viewPos);
            
    		// Create Resolver (currently used to report results)
    		//ContradictionReporter reporter = new ContradictionReporter("E:\\Workspaces\\conflict-resolution\\output\\test2.txt");
    		ContradictionReporter reporter = new ContradictionReporter();
    		
    		// Iterate over each concept calling processUnfetchedConceptData() on each Concept 
    		Bdb.getConceptDb().iterateConceptDataInParallel(processor);
    		ContradictionIdentificationResults results = processor.getResults();
    		
    		// Report any conflicts
    		reporter.identifyInConceptListPanel(results.getConflictingNids());
    		
    		// Single Change
    		if (results.getUnreachableNids().size() == 0)
    			System.out.println("\n\n\n\n\n\nNo Unreachable Concepts");
    		else
    		{
	    		System.out.println("\n\n\n\n\n\nHere are the list of Unreachable Concepts");
	    		Set<I_GetConceptData> set = results.getUnreachableConcepts();
	    		for (I_GetConceptData con : set)
	    		{
	    			System.out.println(con.getInitialText());
	    		}
    		}
    		
    		if (results.getSingleNids().size() == 0)
    			System.out.println("\n\nNo Single Concepts");
    		else
    		{
    			System.out.println("\n\nHere are the list of Single Concepts");
    			Set<I_GetConceptData> set = results.getSingleConcepts();
	    		for (I_GetConceptData con : set)
	    		{
	    			System.out.println(con.getInitialText());
	    		}
    		}
    		
    		System.out.println(">End of List\n\n\n\n");
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }
    
    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    
}
