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
 * BatchActionTaskConceptRefsetMoveMember
 * 
 */
public class BatchActionTaskConceptRefsetMoveMember extends BatchActionTask {

    // REFSET MEMBER
    private int collectionFromNid;
    private int collectionToNid;
    // FILTER
    private TK_REFEX_TYPE refsetType;
    private Object matchValue;

    public BatchActionTaskConceptRefsetMoveMember() {
    }

    public void setCollectionFromNid(int collectionFromNid) {
        this.collectionFromNid = collectionFromNid;
    }

    public void setCollectionToNid(int collectionToNid) {
        this.collectionToNid = collectionToNid;
    }

    public void setRefsetType(TK_REFEX_TYPE refsetType) {
        this.refsetType = refsetType;
    }

    public void setMatchValue(Object matchValue) {
        this.matchValue = matchValue;
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws Exception {
        int rcNid = c.getNid(); // referenced component
        Collection<? extends RefexVersionBI<?>> currentRefexes = c.getRefexesActive(vc);

        // CHECK FROM HAS CONCEPT
        // CHECK TO DOES NOT HAVE REFERENCED CONCEPT
        boolean isInFrom = false;
        boolean isInTo = false;
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getRefexNid() == collectionFromNid) {
                // :!!!:NYI: VALUE FILTER NOT IMPLEMENTED ON TASK VALIDATE CHECK
                isInFrom = true;
            }
            if (rvbi.getRefexNid() == collectionToNid) {
                // :!!!:NYI: VALUE FILTER NOT IMPLEMENTED ON TASK VALIDATE CHECK
                isInTo = true;
            }
        }
        if (isInFrom == false) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.CONCEPT_REFSET_MOVE_MEMBER,
                    BatchActionEventType.EVENT_NOOP, "member not present in move from: "
                    + nidToName(collectionFromNid) + " to:" + nidToName(collectionFromNid)));
            return false;
        }
        if (isInTo == true) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.CONCEPT_REFSET_MOVE_MEMBER,
                    BatchActionEventType.EVENT_NOOP, "member already present in move to: "
                    + nidToName(collectionFromNid) + " to:" + nidToName(collectionToNid)));
            return false;
        }

        boolean changed = false;
        boolean changedReferencedConcept = false;
        ConceptChronicleBI collectionFromConcept = ts.getConcept(collectionFromNid);
        ConceptChronicleBI collectionToConcept = ts.getConcept(collectionToNid);
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getRefexNid() == collectionFromNid) {
                RefexCAB specFrom = rvbi.makeBlueprint(vc);
                TK_REFEX_TYPE refsetFromType = specFrom.getMemberType();
                if (matchValue == null) {

                    // RETIRE MoveFrom
                    for (int editPath : ec.getEditPaths()) {
                        rvbi.makeAnalog(RETIRED_NID,
                            Long.MAX_VALUE,
                            ec.getAuthorNid(),
                            ec.getModuleNid(),
                            editPath);
                    }
                    if (collectionFromConcept.isAnnotationStyleRefex()) {
                        // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                        changedReferencedConcept = true; // pass to BatchActionProcessor
                    } else {
                        ts.addUncommitted(collectionFromConcept);
                    }

                    // ADD MoveTo
                    RefexCAB specTo = new RefexCAB(refsetFromType, rcNid, collectionToNid);
                    if (refsetFromType == TK_REFEX_TYPE.BOOLEAN) {
                        specTo.with(RefexCAB.RefexProperty.BOOLEAN1,
                                specFrom.getBoolean(RefexCAB.RefexProperty.BOOLEAN1));
                    } else if (refsetFromType == TK_REFEX_TYPE.CID) {
                        specTo.with(RefexCAB.RefexProperty.CNID1,
                                specFrom.getInt(RefexCAB.RefexProperty.CNID1)); // int nid
                    } else if (refsetFromType == TK_REFEX_TYPE.INT) {
                        specTo.with(RefexCAB.RefexProperty.INTEGER1,
                                specFrom.getInt(RefexCAB.RefexProperty.INTEGER1));
                    } else if (refsetFromType == TK_REFEX_TYPE.STR) {
                        specTo.with(RefexCAB.RefexProperty.STRING1,
                                specFrom.getString(RefexCAB.RefexProperty.STRING1));
                    }
                    tsSnapshot.constructIfNotCurrent(specTo);

                    if (collectionToConcept.isAnnotationStyleRefex()) {
                        // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                        changedReferencedConcept = true; // pass to BatchActionProcessor
                    } else {
                        ts.addUncommitted(collectionToConcept);
                    }

                    changed = true;
                    BatchActionEventReporter.add(new BatchActionEvent(c,
                            BatchActionTaskType.CONCEPT_REFSET_MOVE_MEMBER,
                            BatchActionEventType.EVENT_SUCCESS,
                            "member moved from: " + nidToName(collectionFromNid)
                            + " to:" + nidToName(collectionToNid)));
                } else {
                    // CHECK FILTER
                    boolean matched = false;
                    switch (refsetType) {
                        case BOOLEAN:
                            if ((Boolean) matchValue == specFrom.getBoolean(RefexCAB.RefexProperty.BOOLEAN1)) {
                                matched = true;
                            }
                            break;
                        case CID:
                            if ((Integer) matchValue == specFrom.getInt(RefexCAB.RefexProperty.CNID1)) {
                                matched = true;
                            }
                            break;
                        case INT:
                            if ((Integer) matchValue == specFrom.getInt(RefexCAB.RefexProperty.INTEGER1)) {
                                matched = true;
                            }
                            break;
                        case STR:
                            String valStr = specFrom.getString(RefexCAB.RefexProperty.STRING1);
                            if (valStr != null && valStr.equalsIgnoreCase((String) matchValue)) {
                                matched = true;
                            }
                            break;
                        default:
                    }

                    if (matched) {
                        // RETIRE MoveFrom
                        for (int editPath : ec.getEditPaths()) {
                            rvbi.makeAnalog(RETIRED_NID,
                                    Long.MAX_VALUE,
                                    ec.getAuthorNid(),
                                    ec.getModuleNid(),
                                    editPath);
                        }
                        if (collectionFromConcept.isAnnotationStyleRefex()) {
                            // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                            changedReferencedConcept = true; // pass to BatchActionProcessor
                        } else {
                            ts.addUncommitted(collectionFromConcept);
                        }

                        // ADD MoveTo
                        RefexCAB specTo = new RefexCAB(refsetFromType, rcNid, collectionToNid);
                        if (refsetFromType == TK_REFEX_TYPE.BOOLEAN) {
                            specTo.with(RefexCAB.RefexProperty.BOOLEAN1,
                                    specFrom.getBoolean(RefexCAB.RefexProperty.BOOLEAN1));
                        } else if (refsetFromType == TK_REFEX_TYPE.CID) {
                            specTo.with(RefexCAB.RefexProperty.CNID1,
                                    specFrom.getInt(RefexCAB.RefexProperty.CNID1)); // int nid
                        } else if (refsetFromType == TK_REFEX_TYPE.INT) {
                            specTo.with(RefexCAB.RefexProperty.INTEGER1,
                                    specFrom.getInt(RefexCAB.RefexProperty.INTEGER1));
                        } else if (refsetFromType == TK_REFEX_TYPE.STR) {
                            specTo.with(RefexCAB.RefexProperty.STRING1,
                                    specFrom.getString(RefexCAB.RefexProperty.STRING1));
                        }
                        tsSnapshot.constructIfNotCurrent(specTo);

                        if (collectionToConcept.isAnnotationStyleRefex()) {
                            // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                            changedReferencedConcept = true; // pass to BatchActionProcessor
                        } else {
                            ts.addUncommitted(collectionToConcept);
                        }

                        changed = true;
                        BatchActionEventReporter.add(new BatchActionEvent(c,
                                BatchActionTaskType.CONCEPT_REFSET_MOVE_MEMBER,
                                BatchActionEventType.EVENT_SUCCESS,
                                "(matched) member moved from: "
                                + nidToName(collectionFromNid) + " to:" + nidToName(collectionToNid)));
                    }
                    matched = false;
                }
            }
        }

        if (!changed && matchValue == null) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.CONCEPT_REFSET_MOVE_MEMBER,
                    BatchActionEventType.EVENT_NOOP,
                    "was not member of: " + nidToName(collectionFromNid)));
        } else if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.CONCEPT_REFSET_MOVE_MEMBER,
                    BatchActionEventType.EVENT_NOOP,
                    "member not retired (not matched): " + nidToName(collectionFromNid)));
        }

        return changedReferencedConcept;
    }
}
