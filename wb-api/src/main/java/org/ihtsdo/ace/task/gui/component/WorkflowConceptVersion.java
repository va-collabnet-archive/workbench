package org.ihtsdo.ace.task.gui.component;

import java.util.UUID;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public  class WorkflowConceptVersion {
	private ConceptVersionBI concept;
	
	public WorkflowConceptVersion(ConceptVersionBI v) {
		concept = v;
	}
	
	@Override
	public String toString() {
		try {
			return concept.getPreferredDescription().getText();
		} catch (Exception e) {
			return "Not Identified";
		}
	}

	public UUID getPrimUuid() {
		return concept.getPrimUuid();
	}
}
