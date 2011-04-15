package org.ihtsdo.batch;

import java.util.UUID;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.blueprint.RelCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelType;

/**
 * BatchActionTaskParentReplace
 * 
 */
public class BatchActionTaskParentReplace extends BatchActionTask {

    private int moveFromRoleTypeNid; // Annncestory linkage type
    private int moveFromDestNid; // Parent concept
    private UUID moveToRoleTypeUuid; // Annncestory linkage type
    private UUID moveToDestUuid; // Parent concept

    public void setMoveToDestUuid(UUID moveToDestUuid) {
        this.moveToDestUuid = moveToDestUuid;
    }

    public void setMoveToRoleTypeUuid(UUID moveToRoleTypeUuid) {
        this.moveToRoleTypeUuid = moveToRoleTypeUuid;
    }

    public void setMoveFromDestNid(int moveFromDestNid) {
        this.moveFromDestNid = moveFromDestNid;
    }

    public void setMoveFromRoleTypeNid(int moveFromRoleTypeNid) {
        this.moveFromRoleTypeNid = moveFromRoleTypeNid;
    }

    public BatchActionTaskParentReplace() {
        this.moveFromRoleTypeNid = Integer.MAX_VALUE;
        this.moveFromDestNid = Integer.MAX_VALUE;

        this.moveToRoleTypeUuid = null;
        this.moveToDestUuid = null;
    }

    public BatchActionTaskParentReplace(UUID moveFromRoleTypeUuid, UUID parentMoveFromUuid, UUID moveToRoleTypeUuid, UUID parentMoveToUuid) throws Exception {
        this.moveFromRoleTypeNid = Ts.get().getNidForUuids(moveFromRoleTypeUuid);
        this.moveFromDestNid = Ts.get().getNidForUuids(parentMoveFromUuid);

        this.moveToRoleTypeUuid = moveToRoleTypeUuid;
        this.moveToDestUuid = parentMoveToUuid;
    }

    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws Exception {
        boolean changed = false;
        for (RelationshipVersionBI r : c.getRelsOutgoingActive()) {
            if (r.getDestinationNid() == moveFromDestNid && r.getTypeNid() == moveFromRoleTypeNid) {
                r.makeAnalog(CURRENT_NID, ec.getAuthorNid(), r.getPathNid(), Long.MAX_VALUE);
                changed = true;
            }
        }

        if (changed) {
            RelCAB rc = new RelCAB(c.getPrimUuid(), moveToRoleTypeUuid, moveToDestUuid, 0, TkRelType.STATED_HIERARCHY);
            termConstructor.construct(rc);

            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.PARENT_REPLACE, BatchActionEventType.EVENT_SUCCESS, "from: " + nidToName(moveFromDestNid) + " to: " + uuidToName(moveToDestUuid)));
        } else {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.PARENT_REPLACE, BatchActionEventType.EVENT_NOOP, "does not have parent: " + nidToName(moveFromDestNid)));
        }

        return changed;
    }
}
