package org.ihtsdo.ace.task.gui.component;

import java.util.LinkedList;
import java.util.List;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public class WorkflowConceptCollection {

	
	List<WorkflowConceptVersion> values = new LinkedList<WorkflowConceptVersion>();

	WorkflowConceptCollection(List<ConceptVersionBI> vals) {
		for (ConceptVersionBI v : vals) {
			values.add(new WorkflowConceptVersion(v));
		}
	}
	
	public WorkflowConceptCollection() {
		// TODO Auto-generated constructor stubsearch
	}

	public int getSize() {
		return values.size();
	}

	public void add(ConceptVersionBI val) {
		values.add(new WorkflowConceptVersion(val));
	}
	
	public WorkflowConceptVersion[] getElements() {
		WorkflowConceptVersion[] retArray = new WorkflowConceptVersion[getSize()];
		int i = 0;
		
		for (WorkflowConceptVersion con : values) {
			retArray[i++] = con;
		}
		
		return retArray;
	}


}
