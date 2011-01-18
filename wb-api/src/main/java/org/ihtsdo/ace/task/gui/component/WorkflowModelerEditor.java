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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.tasks.editor.AbstractComboEditor;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WorkflowModelerEditor extends AbstractComboEditor {

    @Override
    public EditorComponent setupEditor() {
    	
    	TreeSet<I_GetConceptData> activeModelers = new TreeSet<I_GetConceptData>(WorkflowHistoryRefset.createPreferredTermComparer());
    	TreeSet<I_GetConceptData> inactiveModelers = new TreeSet<I_GetConceptData>(WorkflowHistoryRefset.createPreferredTermComparer());
    	List<I_GetConceptData> modelers = new LinkedList<I_GetConceptData>();
		I_GetConceptData defaultModeler = null;

    	try {
    		for (String mod : WorkflowHelper.getModelerKeySet())
    		{
    			I_GetConceptData modeler = WorkflowHelper.lookupModeler(mod);

    			if (isLeadModeler(modeler))
    				modelers.add(modeler);
    			else if (WorkflowHelper.isActiveModeler(modeler))
					activeModelers.add(modeler);
				else
					inactiveModelers.add(modeler);
			}
	    	
			for (I_GetConceptData modeler : activeModelers)
				modelers.add(modeler);
			
			for (I_GetConceptData modeler : inactiveModelers)
			{
				List<? extends I_RelTuple> relList = WorkflowHelper.getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_MODELER_VALUE);

				boolean foundDefaultModeler = false;
				for (I_RelTuple rel : relList)
				{
					if (rel != null &&
						rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_DEFAULT_MODELER.getPrimoridalUid()).getConceptNid()) 
					{
						foundDefaultModeler = true;
						defaultModeler = modeler;
					}
				}
				
				if (!foundDefaultModeler)
					modelers.add(modeler);
			}

			if (defaultModeler != null)
			{
				modelers.add(defaultModeler);
			}
    	
			EditorComponent ec = new EditorComponent(modelers.toArray());

	    	Dimension d = ec.getPreferredSize();
	    	d.width = 275;
	    	ec.setPreferredSize(d);
	    	Rectangle r = new Rectangle(d);
	    	
	    	ec.setBounds(r);
	
	    	return ec;
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
    }

	private boolean isLeadModeler(I_GetConceptData modeler) throws TerminologyException, IOException {
		List<? extends I_RelTuple> relList = WorkflowHelper.getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_MODELER_VALUE);

		for (I_RelTuple rel : relList)
		{
			if (rel != null &&
				rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_LEAD_MODELER.getPrimoridalUid()).getConceptNid())
				return true;
		}
		
		return false;
	}

}
