package org.ihtsdo.contradiction;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.refset.utilities.WfComparator;

public class ContradictionIdentificationResults {

    private Set<Integer> conflictingConcepts = new ConcurrentSkipListSet<Integer>();
    private Set<Integer> singleConcepts = new ConcurrentSkipListSet<Integer>();
    private Set<Integer> nonConflictingConcepts = new ConcurrentSkipListSet<Integer>();
    private Set<Integer> duplicateEdit = new ConcurrentSkipListSet<Integer>();
    private Set<Integer> duplicateNew = new ConcurrentSkipListSet<Integer>();
    private ViewCoordinate viewCoord;
    
    public ContradictionIdentificationResults(ViewCoordinate vc) {
    	viewCoord = vc;
    }

	public void addConflict(Integer nid) {
        conflictingConcepts.add(nid);
    }

    public void addConflictingDuplicateEditConcepts(Integer nid) {
    	duplicateEdit.add(nid);
    }
    public void addConflictingDuplicateNewConcepts(Integer nid) {
    	duplicateNew.add(nid);
    }

    public void addSingle(Integer nid) {
        singleConcepts.add(nid);
    }

    public void addNoneConflicting(Integer nid) {
        nonConflictingConcepts.add(nid);
    }

    public TreeSet<ConceptVersionBI> getConflictingConcepts() {
        TreeSet<ConceptVersionBI> sortedConcepts = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createPreferredTermComparer());

        try {
            for (Integer i : conflictingConcepts) {
                ConceptVersionBI con = Terms.get().getConcept(i).getVersion(viewCoord);
                sortedConcepts.add(con);
            }
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Error in getting concept from detection results", e);
        }

        return sortedConcepts;
    }

    public TreeSet<ConceptVersionBI> getDuplicateEditCompId() {
        TreeSet<ConceptVersionBI> sortedConcepts = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createPreferredTermComparer());

        try {
            for (Integer i : duplicateEdit) {
                ConceptVersionBI con = Terms.get().getConcept(i).getVersion(viewCoord);
                sortedConcepts.add(con);
            }
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Error in getting concept from detection results", e);
        }

        return sortedConcepts;
    }

    public TreeSet<ConceptVersionBI> getDuplicateNewCompId() {
        TreeSet<ConceptVersionBI> sortedConcepts = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createPreferredTermComparer());

        try {
            for (Integer i : duplicateNew) {
                ConceptVersionBI con = Terms.get().getConcept(i).getVersion(viewCoord);
                sortedConcepts.add(con);
            }
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Error in getting concept from detection results", e);
        }

        return sortedConcepts;
    }

    public TreeSet<ConceptVersionBI> getSingleConcepts() {
        TreeSet<ConceptVersionBI> sortedConcepts = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createPreferredTermComparer());

        try {
            for (Integer i : singleConcepts) {
                ConceptVersionBI con = Terms.get().getConcept(i).getVersion(viewCoord);
                sortedConcepts.add(con);
            }
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Error in getting concept from detection results", e);
        }

        return sortedConcepts;
    }

    public TreeSet<ConceptVersionBI> getNoneConflictingConcepts() {
        TreeSet<ConceptVersionBI> sortedConcepts = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createPreferredTermComparer());

        try {
            for (Integer i : nonConflictingConcepts) {
                ConceptVersionBI con = Terms.get().getConcept(i).getVersion(viewCoord);
                sortedConcepts.add(con);
            }
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Error in getting concept from detection results", e);
        }

        return sortedConcepts;
    }

    public Set<Integer> getConflictingNids() {
        return conflictingConcepts;
    }

    public Set<Integer> getDuplicateEditNids() {
        return duplicateEdit;
    }

    public Set<Integer> getDuplicateNewNids() {
        return duplicateNew;
    }

    public Set<Integer> getSingleNids() {
        return singleConcepts;
    }

    public Set<Integer> getNoneConflictingNids() {
        return nonConflictingConcepts;
    }
}
