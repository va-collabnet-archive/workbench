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
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;

/**
 * BatchActionTaskRelationshipRoleReplaceValue
 * 
 */
public class BatchActionTaskRelationshipRoleReplaceValue extends BatchActionTask {

    private int roleNid;
    private int valueOldNid;
    private int valueNewNid;

    public BatchActionTaskRelationshipRoleReplaceValue() {
        this.roleNid = Integer.MAX_VALUE;
        this.valueOldNid = Integer.MAX_VALUE;
        this.valueNewNid = Integer.MAX_VALUE;
    }

    public void setRoleNid(int roleNid) {
        this.roleNid = roleNid;
    }

    public void setValueNewNid(int valueNewNid) {
        this.valueNewNid = valueNewNid;
    }

    public void setValueOldNid(int valueOldNid) {
        this.valueOldNid = valueOldNid;
    }

    //
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB {
        int conceptNid = c.getNid();
        boolean changed = false;
        Collection<? extends RelationshipVersionBI> rels = c.getRelationshipsOutgoingActive();
        for (RelationshipVersionBI rvbi : rels) {
            if (rvbi.getTypeNid() == roleNid && rvbi.getTargetNid() == valueOldNid) {
                for (int editPath : ec.getEditPaths()) {
                    rvbi.makeAnalog(RETIRED_NID,
                            Long.MAX_VALUE,
                            ec.getAuthorNid(),
                            ec.getModuleNid(),
                            editPath);
                }

                TkRelationshipType relChType = TkRelationshipType.STATED_HIERARCHY;
                if (HISTORIC_ROLE_TYPES.contains(rvbi.getTypeNid())) {
                    relChType = TkRelationshipType.HISTORIC;
                }
                RelationshipCAB rc = new RelationshipCAB(rvbi.getSourceNid(), rvbi.getTypeNid(), valueNewNid, rvbi.getGroup(),
                        relChType);
                tsSnapshot.construct(rc);

                BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.RELATIONSHIP_ROLE_REPLACE_VALUE,
                        BatchActionEventType.EVENT_SUCCESS, "retired rel: " + nidToName(conceptNid) + " :: "
                        + nidToName(roleNid) + " :: " + nidToName(valueOldNid)));
                changed = true;
            }
        }

        if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.RELATIONSHIP_ROLE_REPLACE_VALUE,
                    BatchActionEventType.EVENT_NOOP, "does have rel: " + nidToName(conceptNid) + " :: "
                    + nidToName(roleNid) + " :: " + nidToName(valueOldNid)));
        }

        return changed;
    }
}
