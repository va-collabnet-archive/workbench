package org.ihtsdo.batch;

import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 * BatchActionTaskParentRetire
 * 
 */
public class BatchActionTaskParentRetire extends BatchActionTask {

    int selectedRoleTypeNid; // Annncestory linkage type
    int selectedDestNid; // Parent concept

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
    public boolean execute(ConceptVersionBI c) throws Exception {
        System.out.println("## ## BatchActionTaskParentRetire concept: " + c);

        boolean changed = false;
        for (RelationshipVersionBI r : c.getRelsOutgoingActive()) {
            if (r.getDestinationNid() == selectedDestNid && r.getTypeNid() == selectedRoleTypeNid) {
                r.makeAnalog(CURRENT_NID, r.getAuthorNid(), r.getPathNid(), Long.MAX_VALUE);
                changed = true;
                BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.PARENT_RETIRE));
            }
        }

        return changed;
    }
}
