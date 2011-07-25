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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.tasks.editor.AbstractComboEditor;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class SemanticTagEditor extends AbstractComboEditor {
	
    @Override
    public EditorComponent setupEditor() {
    	try {
        	SortedSet<String> displayTags = generateSemanticTags(Terms.get().getActiveAceFrameConfig().getViewCoordinate());

        	EditorComponent ec = new EditorComponent(displayTags.toArray());
	
	    	Dimension d = ec.getPreferredSize();
	    	d.width = 275;
	    	ec.setPreferredSize(d);
	    	Rectangle r = new Rectangle(d);
	    	
	    	ec.setBounds(r);
	
	    	return ec;
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Failed setting up the Drop Down");
		}
		
		return null;
    }

	private SortedSet<String> generateSemanticTags(ViewCoordinate vc) {
		SortedSet<String> displayTags = new TreeSet<String>();

		try {
			I_GetConceptData parentSemTagConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMTAGS_ROOT.getPrimoridalUid());
	    	Set<ConceptVersionBI> semTagConcepts = WorkflowHelper.getChildren(parentSemTagConcept.getVersion(vc));
	
	    	for (ConceptVersionBI con : semTagConcepts) {
	    		if (!semTagConcepts.equals(con)) {
		        	// Get FSN to display
	    			String semTag = con.getFullySpecifiedDescription().getText();
		
		        	displayTags.add(semTag);
		    	}
			}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to display semantic tags with error msg: " + e.getMessage());
		}
    	return displayTags;
	}
}