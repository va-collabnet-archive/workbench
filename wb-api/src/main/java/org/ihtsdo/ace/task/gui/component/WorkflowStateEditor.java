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
package org.ihtsdo.ace.task.gui.component;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.tasks.editor.AbstractComboEditor;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WorkflowStateEditor extends AbstractComboEditor {

    @Override
    public EditorComponent setupEditor() {
    	//Sort the list by name
    	LinkedList<I_GetConceptData> states = new LinkedList<I_GetConceptData>();

    	try {
    		for (I_GetConceptData state : Terms.get().getActiveAceFrameConfig().getWorkflowStates())
    		{
				List<? extends I_RelTuple> useCaseRels = WorkflowHelper.getWorkflowRelationship(state, ArchitectonicAuxiliary.Concept.WORKFLOW_USE_CASE);

				boolean foundUseCase = false;
				for (I_RelTuple rel : useCaseRels)
				{
					if (rel != null &&
					    (rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_EXISTING_CONCEPT.getPrimoridalUid()).getConceptNid() ||
						 rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_NEW_CONCEPT.getPrimoridalUid()).getConceptNid()))
						foundUseCase = true;
				}

				if (!foundUseCase)
					states.add(state);
    		}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Couldn't Set up State Editor Constraint", e);
		}
    	
    	EditorComponent ec = new EditorComponent(states.toArray());
    	

    	Dimension d = ec.getPreferredSize();
    	d.width = 275;
    	ec.setPreferredSize(d);
    	Rectangle r = new Rectangle(d);
    	
    	ec.setBounds(r);
    	
    	return ec;
    }

}
