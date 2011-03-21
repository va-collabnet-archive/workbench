package org.ihtsdo.batch;

import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.blueprint.RelCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelType;

/**
 * BatchActionTaskParentReplace
 * 
 */
public class BatchActionTaskParentReplace extends BatchActionTask {

    int moveFromRoleTypeNid; // Annncestory linkage type
    int moveFromDestNid; // Parent concept
    UUID moveToRoleTypeUuid; // Annncestory linkage type
    UUID moveToDestUuid; // Parent concept

    public BatchActionTaskParentReplace(UUID moveFromRoleTypeUuid, UUID parentMoveFromUuid, UUID moveToRoleTypeUuid, UUID parentMoveToUuid) throws Exception {
        this.moveFromRoleTypeNid = Ts.get().getNidForUuids(moveFromRoleTypeUuid);
        this.moveFromDestNid = Ts.get().getNidForUuids(parentMoveFromUuid);

        this.moveToRoleTypeUuid = moveToRoleTypeUuid;
        this.moveToDestUuid = parentMoveToUuid;
    }

    @Override
    public boolean execute(ConceptVersionBI c) throws Exception {
        System.out.println("BatchActionTaskParentReplace concept: " + c);

        boolean changed = false;
        for (RelationshipVersionBI r : c.getRelsOutgoingActive()) {
            if (r.getDestinationNid() == moveFromDestNid && r.getTypeNid() == moveFromRoleTypeNid) {
                r.makeAnalog(CURRENT_NID, r.getAuthorNid(), r.getPathNid(), Long.MAX_VALUE);
                changed = true;
            }
        }

        if (changed) {
            RelCAB rc = new RelCAB(c.getPrimUuid(), moveToRoleTypeUuid, moveToDestUuid, 0, TkRelType.STATED_HIERARCHY);
            termConstructor.construct(rc);
            
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.PARENT_REPLACE));
        }

        return changed;
    }
}
