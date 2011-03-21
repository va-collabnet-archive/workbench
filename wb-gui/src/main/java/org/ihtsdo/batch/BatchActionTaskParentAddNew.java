package org.ihtsdo.batch;

import java.util.UUID;
import org.ihtsdo.tk.api.blueprint.RelCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelType;

/**
 * BatchActionTaskParentAddNew
 * 
 */

public class BatchActionTaskParentAddNew extends BatchActionTask {

    UUID selectedRoleTypeUuid; // Ancestory linkage type
    UUID selectedDestUuid; // Parent concept

    public BatchActionTaskParentAddNew(UUID roleTypeUuid, UUID parentToAddUuid) {
        this.selectedRoleTypeUuid = roleTypeUuid;
        this.selectedDestUuid = parentToAddUuid;
    }
    
    @Override
    public boolean execute(ConceptVersionBI c) throws Exception {
        System.out.println("## BatchActionTaskParentAddNew concept: " + c);
               
        RelCAB rc = new RelCAB(c.getPrimUuid(), selectedRoleTypeUuid, selectedDestUuid, 0, TkRelType.STATED_HIERARCHY);
        termConstructor.construct(rc);

        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.PARENT_ADD_NEW));
        return true; 
    }

}
