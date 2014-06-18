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
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 *
 * @author marc
 */
public class BatchActionTaskRelationshipRoleRetire extends BatchActionTask {

    private int roleNid;
    private int valueNid;

    public BatchActionTaskRelationshipRoleRetire() {
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
        boolean changed = false;
        Collection<? extends RelationshipVersionBI> rels = c.getRelationshipsOutgoingActive();
        for (RelationshipVersionBI rvbi : rels) {
            if (rvbi.getTypeNid() == roleNid
                    && rvbi.getTargetNid() == valueNid
                    && rvbi.isStated()) {

                for (int editPath : ec.getEditPaths()) {
                    rvbi.makeAnalog(RETIRED_NID,
                            Long.MAX_VALUE,
                            ec.getAuthorNid(),
                            ec.getModuleNid(),
                            editPath);
                }
                changed = true;

                BatchActionEventReporter.add(new BatchActionEvent(c,
                        BatchActionTaskType.RELATIONSHIP_ROLE_RETIRE,
                        BatchActionEventType.EVENT_SUCCESS,
                        "retired rel: " + nidToName(c.getNid())
                        + " :: " + nidToName(roleNid) + " :: " + nidToName(valueNid)));
            }
        }

        if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.RELATIONSHIP_ROLE_RETIRE,
                    BatchActionEventType.EVENT_NOOP,
                    "does not have rel to retire: " + nidToName(c.getNid())
                    + " :: " + nidToName(roleNid) + " :: " + nidToName(valueNid)));
        }

        return changed;
    }
}
