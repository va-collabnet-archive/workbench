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
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelType;

/**
 * BatchActionTaskRoleReplaceValue
 * 
 */
public class BatchActionTaskRoleReplaceValue extends BatchActionTask {

    private int roleNid;
    private int valueOldNid;
    private int valueNewNid;

    public BatchActionTaskRoleReplaceValue() {
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
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws IOException, ContraditionException, InvalidCAB {
        int conceptNid = c.getNid();
        boolean changed = false;
        Collection<? extends RelationshipVersionBI> rels = c.getRelsOutgoingActive();
        for (RelationshipVersionBI rvbi : rels) {
            if (rvbi.getTypeNid() == roleNid && rvbi.getDestinationNid() == valueOldNid) {
                rvbi.makeAnalog(RETIRED_NID, ec.getAuthorNid(), rvbi.getPathNid(), Long.MAX_VALUE);

                RelCAB rc = new RelCAB(rvbi.getOriginNid(), rvbi.getTypeNid(), valueNewNid, rvbi.getGroup(),
                        TkRelType.STATED_HIERARCHY);
                tsSnapshot.construct(rc);

                BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.ROLE_REPLACE_VALUE,
                        BatchActionEventType.EVENT_SUCCESS, "retired rel: " + nidToName(conceptNid) + " :: "
                        + nidToName(roleNid) + " :: " + nidToName(valueOldNid)));
                changed = true;
            }
        }

        if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.ROLE_REPLACE_VALUE,
                    BatchActionEventType.EVENT_NOOP, "does have rel: " + nidToName(conceptNid) + " :: "
                    + nidToName(roleNid) + " :: " + nidToName(valueOldNid)));
        }

        return changed;
    }
}
