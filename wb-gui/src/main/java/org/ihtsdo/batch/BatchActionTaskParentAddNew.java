/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.batch;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelType;

/**
 * BatchActionTaskParentAddNew
 * 
 */
public class BatchActionTaskParentAddNew extends BatchActionTask {

    private UUID selectedRoleTypeUuid; // Ancestory linkage type
    private int selectedRoleTypeNid; // Ancestory linkage type
    private UUID selectedDestUuid; // Parent concept
    private int selectedDestNid; // Parent concept

    public void setSelectedDestUuid(UUID selectedDestUuid) throws IOException {
        this.selectedDestUuid = selectedDestUuid;
        this.selectedDestNid = Ts.get().getNidForUuids(selectedDestUuid);
    }

    public void setSelectedRoleTypeUuid(UUID selectedRoleTypeUuid) throws IOException {
        this.selectedRoleTypeUuid = selectedRoleTypeUuid;
        this.selectedRoleTypeNid = Ts.get().getNidForUuids(selectedRoleTypeUuid);
    }

    public BatchActionTaskParentAddNew() {
        this.selectedRoleTypeUuid = null;
        this.selectedDestUuid = null;
        this.selectedRoleTypeNid = Integer.MAX_VALUE;
        this.selectedDestNid = Integer.MAX_VALUE;
    }

    public BatchActionTaskParentAddNew(UUID roleTypeUuid, UUID parentToAddUuid) throws IOException {
        this.selectedRoleTypeUuid = roleTypeUuid;
        this.selectedDestUuid = parentToAddUuid;
        this.selectedRoleTypeNid = Ts.get().getNidForUuids(roleTypeUuid);
        this.selectedDestNid = Ts.get().getNidForUuids(parentToAddUuid);
    }

    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws IOException, ContraditionException, InvalidCAB {
        // Check if parent already exists and is active.
        Collection<? extends RelationshipVersionBI> checkParents = c.getRelsOutgoingActive();
        for (RelationshipVersionBI rvbi : checkParents) {
            if (rvbi.getDestinationNid() == selectedDestNid && rvbi.getTypeNid() == selectedRoleTypeNid & rvbi.isStated()) {
                BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.PARENT_ADD_NEW, BatchActionEventType.EVENT_NOOP, "already has parent: " + nidToName(selectedDestNid)));
                return false;
            }
        }

        // If parent does not already exist, than add a new parent record.
        RelCAB rc = new RelCAB(c.getPrimUuid(), selectedRoleTypeUuid, selectedDestUuid, 
                0, TkRelType.STATED_HIERARCHY);
        termConstructor.construct(rc);  

        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.PARENT_ADD_NEW, BatchActionEventType.EVENT_SUCCESS, "added parent: " + nidToName(selectedDestNid)));
        return true;
    }
}
