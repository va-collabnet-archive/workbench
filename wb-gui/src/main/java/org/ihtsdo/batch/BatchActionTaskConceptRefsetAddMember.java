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
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 * Sample BatchAction
 * 
 */
public class BatchActionTaskConceptRefsetAddMember extends BatchActionTask {

    // int rcNid; referenced component provided at execution time
    private TK_REFEX_TYPE refsetType;
    private int collectionNid;
    private Object refsetValue;

    /**
     * Batch Action adds refset member.
     */
    public BatchActionTaskConceptRefsetAddMember() {
        super();
    }

    public void setCollectionNid(int collectionNid) {
        this.collectionNid = collectionNid;
    }

    public void setRefsetType(TK_REFEX_TYPE refsetType) {
        this.refsetType = refsetType;
    }

    public void setRefsetValue(Object refsetValue) {
        this.refsetValue = refsetValue;
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws Exception {
        int rcNid = c.getNid(); // referenced component nid
        // Check if member already exists
        Collection<? extends RefexVersionBI<?>> currentRefexes = c.getRefexesActive(vc);
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getRefexNid() == collectionNid) {
                BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.CONCEPT_REFSET_ADD_MEMBER,
                        BatchActionEventType.EVENT_NOOP, "already member of: " + nidToName(collectionNid)));
                return false;
            }
        }

        // If not already a member, then a member record is added.
        RefexCAB refexSpec = new RefexCAB(refsetType, rcNid, collectionNid);
        if (refsetType == TK_REFEX_TYPE.BOOLEAN) {
            refexSpec.with(RefexCAB.RefexProperty.BOOLEAN1, (Boolean) refsetValue);
        } else if (refsetType == TK_REFEX_TYPE.CID) {
            refexSpec.with(RefexCAB.RefexProperty.CNID1, ((Integer) refsetValue).intValue()); // int nid
        } else if (refsetType == TK_REFEX_TYPE.INT) {
            refexSpec.with(RefexCAB.RefexProperty.INTEGER1, (Integer) refsetValue);
        } else if (refsetType == TK_REFEX_TYPE.STR) {
            refexSpec.with(RefexCAB.RefexProperty.STRING1, (String) refsetValue);
        }

        refexSpec.setMemberContentUuid();
        tsSnapshot.constructIfNotCurrent(refexSpec);

        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.CONCEPT_REFSET_ADD_MEMBER,
                BatchActionEventType.EVENT_SUCCESS, "member added to: " + nidToName(collectionNid)));

        ConceptChronicleBI collectionConcept = ts.getConcept(collectionNid);
        if (collectionConcept.isAnnotationStyleRefex()) {
            // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
            return true; // pass to BatchActionProcessor
        } else {
            ts.addUncommitted(collectionConcept);
            return false;
        }
    }
}
