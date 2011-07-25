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
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.tasks.editor.AbstractComboEditor;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.workflow.refset.utilities.WfComparator;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WorkflowModelerEditor extends AbstractComboEditor {

    @Override
    public EditorComponent setupEditor() {
    	
    	ConceptVersionBI defaultModeler = null;
    	TreeSet<ConceptVersionBI> activeModelers = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createPreferredTermComparer());
    	TreeSet<ConceptVersionBI> inactiveModelers = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createPreferredTermComparer());
    	WorkflowConceptCollection modelers = new WorkflowConceptCollection();

    	try {
    		for (String mod : WorkflowHelper.getModelerKeySet())
    		{
    			ConceptVersionBI modeler = WorkflowHelper.lookupModeler(mod);

    			if (isLeadModeler(modeler))
    				modelers.add(modeler);
    			else if (WorkflowHelper.isActiveModeler(modeler))
					activeModelers.add(modeler);
				else
					inactiveModelers.add(modeler);
			}
	    	
			for (ConceptVersionBI modeler : activeModelers)
				modelers.add(modeler);
			
			for (ConceptVersionBI modeler : inactiveModelers)
			{
				List<RelationshipVersionBI> relList = WorkflowHelper.getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS);

				boolean foundDefaultModeler = false;
				for (RelationshipVersionBI  rel : relList)
				{
					if (rel != null &&
						rel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_DEFAULT_MODELER.getPrimoridalUid()).getConceptNid()) 
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
    	
			EditorComponent ec = new EditorComponent(modelers.getElements());

	    	Dimension d = ec.getPreferredSize();
	    	d.width = 275;
	    	ec.setPreferredSize(d); 
	    	Rectangle r = new Rectangle(d);
	    	
	    	ec.setBounds(r);
	
	    	return ec;
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Couldn't Set up Modeler Editor Constraint", e);
		}
		
		return null;
    }

	private boolean isLeadModeler(ConceptVersionBI modeler) throws TerminologyException, IOException {
		List<RelationshipVersionBI> relList = WorkflowHelper.getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS);

		for (RelationshipVersionBI rel : relList)
		{
			if (rel != null &&
				rel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_LEAD_MODELER.getPrimoridalUid()).getConceptNid())
				return true;
		}
		
		return false;
	}

}
