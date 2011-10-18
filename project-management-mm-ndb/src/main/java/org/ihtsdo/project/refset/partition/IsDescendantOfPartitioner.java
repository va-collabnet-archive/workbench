package org.ihtsdo.project.refset.partition;

import java.io.IOException;
import java.io.Serializable;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class IsDescendantOfPartitioner extends RefsetPartitioner implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	I_GetConceptData parent;
	
	public IsDescendantOfPartitioner() {
		super();
	}
	
	public IsDescendantOfPartitioner(I_GetConceptData parent) {
		super();
		this.parent = parent;
	}

	protected boolean evaluateMember(I_GetConceptData member, I_ConfigAceFrame config) {
		Boolean result = false;
		
		try {
			I_IntSet allowedTypes = Terms.get().newIntSet();
			allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			result = parent.isParentOfOrEqualTo(member); 
//			result = parent.isParentOfOrEqualTo(member, config.getAllowedStatus(), allowedTypes,
//					config.getViewPositionSetReadOnly(), 
//					config.getPrecedence(), config.getConflictResolutionStrategy());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return result;
	}

	public I_GetConceptData getParent() {
		return parent;
	}

	public void setParent(I_GetConceptData parent) {
		this.parent = parent;
	}
	
	public String toString() {
		return "Is descendant of";
	}

}
