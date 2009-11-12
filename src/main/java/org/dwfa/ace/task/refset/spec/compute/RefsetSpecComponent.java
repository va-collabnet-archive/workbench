package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public abstract class RefsetSpecComponent {
    private int possibleConceptsCount;

    public int getPossibleConceptsCount() {
        return possibleConceptsCount;
    }

    protected void setPossibleConceptsCount(int possibleConceptsCount) {
        this.possibleConceptsCount = possibleConceptsCount;
    }

    protected Set<Integer> getCurrentStatusIds() {
        Set<Integer> statuses = new HashSet<Integer>();

        try {
            statuses.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.READY_TO_PROMOTE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.PROMOTED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statuses;
    }

    public abstract boolean execute(I_AmTermComponent component) throws IOException, TerminologyException;
}
