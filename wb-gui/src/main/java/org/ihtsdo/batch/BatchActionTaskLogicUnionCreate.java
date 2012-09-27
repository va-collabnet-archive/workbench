/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.batch;

import java.util.Collection;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;

/**
 * Sample BatchAction
 *
 */
public class BatchActionTaskLogicUnionCreate extends BatchActionTask {

    private int unionSetNid;

    /**
     * Batch Action adds concept to union set.
     */
    public BatchActionTaskLogicUnionCreate() {
    }

    public void setUnionSetRefexNid(int unionSetNid) {
        this.unionSetNid = unionSetNid;
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc)
            throws Exception {
        int rcNid = c.getNid(); // referenced component nid

        // Check if member already exists
        Collection<? extends RefexVersionBI<?>> currentRefexes = c.getRefexesActive(vc);
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getRefexNid() == unionSetNid) {
                BatchActionEventReporter.add(new BatchActionEvent(c,
                        BatchActionTaskType.LOGIC_UNION_SET_CREATE,
                        BatchActionEventType.EVENT_NOOP,
                        "already member of: " + nidToName(unionSetNid)));
                return false;
            }
        }

        // If not already a member, then a member record is added.
        RefexCAB refexSpec = new RefexCAB(TK_REFEX_TYPE.CID, rcNid, unionSetNid);
        int normalMemberNid = ts.getConcept(
                        RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids()).getConceptNid();
        refexSpec.with(RefexCAB.RefexProperty.CNID1, normalMemberNid);
        refexSpec.setMemberContentUuid();
        tsSnapshot.constructIfNotCurrent(refexSpec);

        BatchActionEventReporter.add(new BatchActionEvent(c,
                BatchActionTaskType.LOGIC_UNION_SET_CREATE,
                BatchActionEventType.EVENT_SUCCESS,
                "member added to: " + nidToName(unionSetNid)));

        ConceptChronicleBI collectionConcept = ts.getConcept(unionSetNid);
        if (collectionConcept.isAnnotationStyleRefex()) {
            // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
            return true; // pass to BatchActionProcessor
        } else {
            ts.addUncommitted(collectionConcept);
            return false;
        }
    }
}
