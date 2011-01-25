package org.ihtsdo.ace.task.contradiction;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;

public class ContradictionIdentificationResults {
	private Set<Integer> conflictingConcepts = null;
	private Set<Integer> unreachableConcepts = null;
	private Set<Integer> singleConcepts = null;
	private Set<Integer> nonConflictingConcepts = null;

	public void addConflict(Integer nid) {
		conflictingConcepts.add(nid);
	}

	public void addUnreachable(Integer nid) {
		unreachableConcepts.add(nid);
	}

	public void addSingle(Integer nid) {
		singleConcepts.add(nid);
	}

	public void addNoneConflicting(Integer nid) {
		nonConflictingConcepts.add(nid);
	}
	
	public TreeSet<I_GetConceptData> getConflictingConcepts() {
		TreeSet<I_GetConceptData> sortedConcepts = new TreeSet<I_GetConceptData>(WorkflowHistoryRefset.createPreferredTermComparer());

		try {
			for (Integer i : conflictingConcepts)
			{
				I_GetConceptData con = Terms.get().getConcept(i);
				sortedConcepts.add(con);
			}
		} catch (Exception e) {
        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Error in getting concept from detection results", e);
		}

		return sortedConcepts;
	}

	public TreeSet<I_GetConceptData> getUnreachableConcepts() {
		TreeSet<I_GetConceptData> sortedConcepts = new TreeSet<I_GetConceptData>(WorkflowHistoryRefset.createPreferredTermComparer());

		try {
			for (Integer i : unreachableConcepts)
			{
				I_GetConceptData con = Terms.get().getConcept(i);
				sortedConcepts.add(con);
			}
		} catch (Exception e) {
        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Error in getting concept from detection results", e);
		}

		return sortedConcepts;
	}

	public TreeSet<I_GetConceptData> getSingleConcepts() {
		TreeSet<I_GetConceptData> sortedConcepts = new TreeSet<I_GetConceptData>(WorkflowHistoryRefset.createPreferredTermComparer());

		try {
			for (Integer i : singleConcepts)
			{
				I_GetConceptData con = Terms.get().getConcept(i);
				sortedConcepts.add(con);
			}
		} catch (Exception e) {
        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Error in getting concept from detection results", e);
		}

		return sortedConcepts;
	}

	public TreeSet<I_GetConceptData> getNoneConflictingConcepts() {
		TreeSet<I_GetConceptData> sortedConcepts = new TreeSet<I_GetConceptData>(WorkflowHistoryRefset.createPreferredTermComparer());

		try {
			for (Integer i : nonConflictingConcepts)
			{
				I_GetConceptData con = Terms.get().getConcept(i);
				sortedConcepts.add(con);
			}
		} catch (Exception e) {
        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Error in getting concept from detection results", e);
		}

		return sortedConcepts;
	}
	
	public Set<Integer> getConflictingNids() {
		return conflictingConcepts;
	}

	public Set<Integer> getUnreachableNids() {
		return unreachableConcepts;
	}

	public Set<Integer> getSingleNids() {
		return singleConcepts;
	}

	public Set<Integer> getNoneConflictingNids() {
		return nonConflictingConcepts;
	}

}
