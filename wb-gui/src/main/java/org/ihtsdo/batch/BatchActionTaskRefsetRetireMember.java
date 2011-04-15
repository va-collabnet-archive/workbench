package org.ihtsdo.batch;

import java.io.IOException;
import java.util.Collection;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

/**
 * BatchActionTaskRefsetRetireMember
 * 
 */
public class BatchActionTaskRefsetRetireMember extends BatchActionTask {

    private int selectedCollectionNid;

    public void setSelectedCollectionNid(int selectedCollectionNid) {
        this.selectedCollectionNid = selectedCollectionNid;
    }

    public BatchActionTaskRefsetRetireMember() {
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws IOException {

        Collection<? extends RefexVersionBI<?>> currentRefexes = c.getCurrentRefexes(vc);
        boolean changed = false;
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getCollectionNid() == selectedCollectionNid) {
                rvbi.makeAnalog(RETIRED_NID, ec.getAuthorNid(), rvbi.getPathNid(), Long.MAX_VALUE);
                changed = true;
                BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_RETIRE_MEMBER, BatchActionEventType.EVENT_SUCCESS, "retired member of: " + nidToName(selectedCollectionNid)));
            }
        }

        if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_RETIRE_MEMBER, BatchActionEventType.EVENT_NOOP, "was not member of: " + nidToName(selectedCollectionNid)));
        }

        return changed;
    }
}
