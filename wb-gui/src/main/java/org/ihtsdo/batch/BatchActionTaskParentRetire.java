package org.ihtsdo.batch;

import java.util.UUID;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 * BatchActionTaskParentRetire
 * 
 */
public class BatchActionTaskParentRetire extends BatchActionTask {

    private int selectedRoleTypeNid; // Ancestory linkage type
    private int selectedDestNid; // Parent concept

    public void setSelectedDestNid(int selectedDestNid) {
        this.selectedDestNid = selectedDestNid;
    }

    public void setSelectedRoleTypeNid(int selectedRoleTypeNid) {
        this.selectedRoleTypeNid = selectedRoleTypeNid;
    }

    public BatchActionTaskParentRetire() {
        this.selectedRoleTypeNid = Integer.MAX_VALUE;
        this.selectedDestNid = Integer.MAX_VALUE;
    }

    public BatchActionTaskParentRetire(int selectedRoleTypeNid, int selectedDestNid) throws Exception {
        this.selectedRoleTypeNid = selectedRoleTypeNid;
        this.selectedDestNid = selectedDestNid;
    }

    public BatchActionTaskParentRetire(UUID selectedRoleTypeUuid, UUID selectedDestUuid) throws Exception {
        this.selectedRoleTypeNid = Ts.get().getNidForUuids(selectedRoleTypeUuid);
        this.selectedDestNid = Ts.get().getNidForUuids(selectedDestUuid);
    }

    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws Exception {
        boolean changed = false;
        for (RelationshipVersionBI r : c.getRelsOutgoingActive()) {
            if (r.getDestinationNid() == selectedDestNid && r.getTypeNid() == selectedRoleTypeNid) {
                r.makeAnalog(RETIRED_NID, ec.getAuthorNid(), r.getPathNid(), Long.MAX_VALUE);
                changed = true;
                BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.PARENT_RETIRE, BatchActionEventType.EVENT_SUCCESS, "retired: " + nidToName(selectedDestNid)));
            }
        }

        if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.PARENT_RETIRE, BatchActionEventType.EVENT_NOOP, "does not have parent: " + nidToName(selectedDestNid)));
        }

        return changed;
    }
}
