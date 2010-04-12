package org.ihtsdo.db.bdb.computer.refset;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpMemberRefsetsCalculateConflicts;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.ClosestDistanceHashSet;
import org.dwfa.ace.refset.ConceptRefsetInclusionDetails;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;

public class MemberRefsetConflictCalculator extends MemberRefsetCalculator implements I_HelpMemberRefsetsCalculateConflicts {

    public MemberRefsetConflictCalculator(I_ConfigAceFrame config) throws Exception {
		super(config);
	}

	private boolean conflicts = false;
    protected List<String> conflictDetails = new ArrayList<String>();

    public boolean hasConflicts() {
        return conflicts;
    }

    protected void setMembers() throws Exception {

        for (Integer refset : newRefsetMembers.keySet()) {
 
            conflictDetails.add("Conflicts in refset " + Terms.get().getConcept(refset) + " are: ");

            ClosestDistanceHashSet newMembers = newRefsetMembers.get(refset);
            ClosestDistanceHashSet oldMembers = newRefsetExclusion.get(refset);
            if (newMembers != null) {

                for (ConceptRefsetInclusionDetails i : newMembers.values()) {
                    if (oldMembers != null && oldMembers.containsKey(i.getConceptId())) {
                        List<Integer> addedConcepts = new ArrayList<Integer>();
                        for (ConceptRefsetInclusionDetails old : oldMembers.values()) {
                            // Show only first level conflict
                            I_IntSet isARel = Terms.get().newIntSet();
                            isARel.add(Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
                            isARel.add(Terms.get().uuidToNative(SNOMED.Concept.IS_A.getUids()));
                            if (old.equals(i)) {
                                for (I_GetConceptData c : Terms.get().getConcept(i.getConceptId())
                                    .getSourceRelTargets(null, isARel, null,
                                        getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy())) {
                                    int conceptId = c.getConceptId();
                                    if (conceptId == Terms.get().getConcept(i.getInclusionReasonId())
                                        .getConceptId()
                                        || conceptId == Terms.get().getConcept(old.getInclusionReasonId())
                                            .getConceptId()) {

                                        if (!addedConcepts.contains(new Integer(conceptId))) {

                                            StringBuffer sb = new StringBuffer();
                                            sb.append(Terms.get().getConcept(i.getConceptId()).toString());
                                            sb.append(" because of "
                                                + Terms.get().getConcept(i.getInclusionReasonId()).toString());
                                            sb.append(" conflicts with "
                                                + Terms.get().getConcept(old.getInclusionReasonId()).toString());

                                            conflictDetails.add(sb.toString());
                                            addedConcepts.add(new Integer(conceptId));
                                        }
                                    }
                                }
                            }
                            // Show all levels conflicts
                            // if (old.equals(i)) {
                            // StringBuffer sb = new StringBuffer();
                            // sb.append(Terms.get().getConcept(i.getConceptId()).toString());
                            // sb.append(" because of " +
                            // Terms.get().getConcept(i.getInclusionReasonId()).toString());
                            // sb.append(" conflicts with "
                            // +Terms.get().getConcept(old.getInclusionReasonId()).toString());
                            //									
                            // conflictDetails.add(sb.toString());
                            //									
                            // }
                        }
                        conflicts = true;
                    }
                }
            }
        }
    }

	@Override
	public List<String> getConclictDetails() {
		return conflictDetails;
	}
}

