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

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;

/**
 *
 * @author marc
 */
public class BatchActionTaskRelationshipRoleAdd extends BatchActionTask {

    private int roleNid;
    private int valueNid;

    public BatchActionTaskRelationshipRoleAdd() {
        this.roleNid = Integer.MAX_VALUE;
        this.valueNid = Integer.MAX_VALUE;
    }

    public void setRoleNid(int roleNid) {
        this.roleNid = roleNid;
    }

    public void setValueNid(int valueNid) {
        this.valueNid = valueNid;
    }

    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws Exception {
        // Check if role-value already exists and is active.
        Collection<? extends RelationshipVersionBI> checkParents = c.getRelationshipsOutgoingActive();
        for (RelationshipVersionBI rvbi : checkParents) {
            if (rvbi.getTypeNid() == roleNid
                    && rvbi.getTargetNid() == valueNid
                    && rvbi.isStated()) {
                BatchActionEventReporter.add(new BatchActionEvent(c,
                        BatchActionTaskType.RELATIONSHIP_ROLE_ADD,
                        BatchActionEventType.EVENT_NOOP,
                        "already has role-value: "
                        + nidToName(roleNid) + " :: " + nidToName(valueNid)));
                return false;
            }
        }

        //        RelationshipCAB relSpec = new RelationshipCAB(c.getNid(), roleNid, valueNid, 0, TkRelationshipType.STATED_ROLE);
        List<UUID> roleUuids = ts.getUuidsForNid(roleNid);
        List<UUID> valueUuids = ts.getUuidsForNid(valueNid);
        if (roleUuids.size() > 0 && valueUuids.size() > 0) {
            TkRelationshipType relChType = TkRelationshipType.STATED_ROLE;
            if (HISTORIC_ROLE_TYPES.contains(roleNid)) {
                relChType = TkRelationshipType.HISTORIC;
            }

            RelationshipCAB relSpec = new RelationshipCAB(c.getPrimUuid(), roleUuids.get(0), valueUuids.get(0),
                    0, relChType);
            tsSnapshot.construct(relSpec);

            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.RELATIONSHIP_ROLE_ADD,
                    BatchActionEventType.EVENT_SUCCESS,
                    "added role-value: "
                    + " :: " + nidToName(roleNid) + " :: " + nidToName(valueNid)));
            return true;
        } else {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.RELATIONSHIP_ROLE_ADD,
                    BatchActionEventType.EVENT_ERROR,
                    "could not find role-value uuids: "
                    + nidToName(roleNid) + " :: " + nidToName(valueNid)));
            return false;
        }
    }
}
