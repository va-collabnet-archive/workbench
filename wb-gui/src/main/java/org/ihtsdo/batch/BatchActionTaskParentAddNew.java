package org.ihtsdo.batch;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
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

    public void setSelectedDestUuid(UUID selectedDestUuid) {
        this.selectedDestUuid = selectedDestUuid;
    }

    public void setSelectedRoleTypeUuid(UUID selectedRoleTypeUuid) {
        this.selectedRoleTypeUuid = selectedRoleTypeUuid;
    }

    public BatchActionTaskParentAddNew() {
        this.selectedRoleTypeUuid = null;
        this.selectedDestUuid = null;
    }

    public BatchActionTaskParentAddNew(UUID roleTypeUuid, UUID parentToAddUuid) {
        this.selectedRoleTypeUuid = roleTypeUuid;
        this.selectedDestUuid = parentToAddUuid;
    }
    
    @Override
    public boolean execute(ConceptVersionBI c)  {
        System.out.println("## BatchActionTaskParentAddNew concept: " + c);
               
        RelCAB rc = null;
        try {
            if (c.getPrimUuid() == null) {
                System.out.println("found bad: " + c.getPrimUuid());
            }
            rc = new RelCAB(c.getPrimUuid(), selectedRoleTypeUuid, selectedDestUuid, 0, TkRelType.STATED_HIERARCHY);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        try {
            termConstructor.construct(rc);
        } catch (IOException ex) {
           throw new RuntimeException(ex);
        } catch (InvalidCAB ex) {
           throw new RuntimeException(ex);
        }

        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.PARENT_ADD_NEW));
        return true; 
    }

}
