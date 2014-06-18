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
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 * BatchActionTaskConceptRefsetRetireMember
 * 
 */
public class BatchActionTaskConceptRefsetRetireMember extends BatchActionTask {

    // REFSET MEMBER
    private int collectionNid;
    // FILTER
    private TK_REFEX_TYPE refsetType;
    private Object matchValue;

    public BatchActionTaskConceptRefsetRetireMember() {
        this.collectionNid = Integer.MAX_VALUE;
        this.matchValue = null;
    }

    public void setCollectionNid(int collectionNid) {
        this.collectionNid = collectionNid;
    }

    public void setRefsetType(TK_REFEX_TYPE refsetType) {
        this.refsetType = refsetType;
    }

    public void setMatchValue(Object matchValue) {
        this.matchValue = matchValue;
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc)
            throws IOException, InvalidCAB, ContradictionException {

        Collection<? extends RefexVersionBI<?>> currentRefexes = c.getRefexesActive(vc);
        boolean changed = false;
        boolean changedReferencedConcept = false;
        ConceptChronicleBI collectionConcept = ts.getConcept(collectionNid);
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getRefexNid() == collectionNid) {
                if (matchValue == null) {
//                        RefexCAB bluePrint = rvbi.makeBlueprint(vc);
//                        bluePrint.setStatusUuid(ts.getConcept(RETIRED_NID).getPrimUuid());
//                        tsSnapshot.constructIfNotCurrent(bluePrint);
//                        changedReferencedConcept = true; //... pass to BatchActionProcessor
                    for (int editPath : ec.getEditPaths()) {
                        rvbi.makeAnalog(RETIRED_NID,
                                Long.MAX_VALUE,
                                ec.getAuthorNid(),
                                ec.getModuleNid(),
                                editPath);
                    }
                    if (collectionConcept.isAnnotationStyleRefex()) {
                        // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                        changedReferencedConcept = true; // pass to BatchActionProcessor
                    } else {
                        changedReferencedConcept = true; //... pass to BatchActionProcessor
                        ts.addUncommitted(collectionConcept);
                    }

                    changed = true;
                    BatchActionEventReporter.add(new BatchActionEvent(c,
                            BatchActionTaskType.CONCEPT_REFSET_RETIRE_MEMBER,
                            BatchActionEventType.EVENT_SUCCESS,
                            "retired member of: " + nidToName(collectionNid)));
                } else {
                    // CHECK FILTER
                    RefexCAB spec = rvbi.makeBlueprint(vc);
                    boolean matched = false;
                    switch (refsetType) {
                        case BOOLEAN:
                            if ((Boolean) matchValue == spec.getBoolean(RefexCAB.RefexProperty.BOOLEAN1)) {
                                matched = true;
                            }
                            break;
                        case CID:
                            if ((Integer) matchValue == spec.getInt(RefexCAB.RefexProperty.CNID1)) {
                                matched = true;
                            }
                            break;
                        case INT:
                            if ((Integer) matchValue == spec.getInt(RefexCAB.RefexProperty.INTEGER1)) {
                                matched = true;
                            }
                            break;
                        case STR:
                            String valStr = spec.getString(RefexCAB.RefexProperty.STRING1);
                            if (valStr != null && valStr.equalsIgnoreCase((String) matchValue)) {
                                matched = true;
                            }
                            break;
                        default:
                    }

                    if (matched) {
                        for (int editPath : ec.getEditPaths()) {
                            rvbi.makeAnalog(RETIRED_NID,
                                    Long.MAX_VALUE,
                                    ec.getAuthorNid(),
                                    ec.getModuleNid(),
                                    editPath);
                        }
                        if (collectionConcept.isAnnotationStyleRefex()) {
                            // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                            changedReferencedConcept = true; // pass to BatchActionProcessor
                        } else {
                            changedReferencedConcept = true; //... pass to BatchActionProcessor
                            ts.addUncommitted(collectionConcept);
                        }

                        changed = true;
                        BatchActionEventReporter.add(new BatchActionEvent(c,
                                BatchActionTaskType.CONCEPT_REFSET_RETIRE_MEMBER,
                                BatchActionEventType.EVENT_SUCCESS,
                                "retired member of (value matched): " + nidToName(collectionNid)));
                    }
                    matched = false;
                }
            }
        }

        if (!changed && matchValue == null) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.CONCEPT_REFSET_RETIRE_MEMBER,
                    BatchActionEventType.EVENT_NOOP,
                    "was not member of: " + nidToName(collectionNid)));
        } else if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.CONCEPT_REFSET_RETIRE_MEMBER,
                    BatchActionEventType.EVENT_NOOP,
                    "member not retired (not matched): " + nidToName(collectionNid)));
        }

        return changedReferencedConcept;
    }
}
