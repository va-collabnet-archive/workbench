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

import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.blueprint.RelCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelType;

/**
 *
 * @author marc
 */
public class BatchActionTaskRoleAdd extends BatchActionTask {

    private int roleNid;
    private int valueNid;

    public BatchActionTaskRoleAdd() {
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
        int conceptNid = c.getNid();
        RelCAB relSpec = new RelCAB(conceptNid, roleNid, valueNid, 0, TkRelType.STATED_ROLE);
        termConstructor.construct(relSpec);

        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.ROLE_ADD, BatchActionEventType.EVENT_SUCCESS, 
                "added relationship: " + nidToName(conceptNid) + " :: " + nidToName(roleNid) + " :: " + nidToName(valueNid)));
        return true;
    }
}
