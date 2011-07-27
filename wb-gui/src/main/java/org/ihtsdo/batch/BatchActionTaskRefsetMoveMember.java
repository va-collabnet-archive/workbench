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
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;

/**
 * BatchActionTaskRefsetMoveMember
 * 
 */
public class BatchActionTaskRefsetMoveMember extends BatchActionTask {

    // REFSET MEMBER
    private int collectionFromNid;
    private int collectionToNid;
    // FILTER
    private TK_REFSET_TYPE refsetType;
    private Object matchValue;

    public BatchActionTaskRefsetMoveMember() {
    }

    public void setCollectionFromNid(int collectionFromNid) {
        this.collectionFromNid = collectionFromNid;
    }

    public void setCollectionToNid(int collectionToNid) {
        this.collectionToNid = collectionToNid;
    }

    public void setRefsetType(TK_REFSET_TYPE refsetType) {
        this.refsetType = refsetType;
    }

    public void setMatchValue(Object matchValue) {
        this.matchValue = matchValue;
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws Exception {
        int rcNid = c.getNid(); // referenced component
        Collection<? extends RefexVersionBI<?>> currentRefexes = c.getCurrentRefexes(vc);

        // CHECK FROM HAS CONCEPT
        // CHECK TO DOES NOT HAVE REFERENCED CONCEPT
        boolean isInFrom = false;
        boolean isInTo = false;
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getCollectionNid() == collectionFromNid) {
                // :!!!:NYI: VALUE FILTER NOT IMPLEMENTED ON TASK VALIDATE CHECK
                isInFrom = true;
            }
            if (rvbi.getCollectionNid() == collectionToNid) {
                // :!!!:NYI: VALUE FILTER NOT IMPLEMENTED ON TASK VALIDATE CHECK
                isInTo = true;
            }
        }
        if (isInFrom == false) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.REFSET_MOVE_MEMBER,
                    BatchActionEventType.EVENT_NOOP, "member not present in move from: "
                    + nidToName(collectionFromNid) + " to:" + nidToName(collectionFromNid)));
            return false;
        }
        if (isInTo == true) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.REFSET_MOVE_MEMBER,
                    BatchActionEventType.EVENT_NOOP, "member already present in move to: "
                    + nidToName(collectionFromNid) + " to:" + nidToName(collectionToNid)));
            return false;
        }

        boolean changed = false;
        boolean changedReferencedConcept = false;
        ConceptChronicleBI collectionFromConcept = ts.getConcept(collectionFromNid);
        ConceptChronicleBI collectionToConcept = ts.getConcept(collectionToNid);
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getCollectionNid() == collectionFromNid) {
                RefexCAB specFrom = rvbi.getRefexEditSpec();
                TK_REFSET_TYPE refsetFromType = specFrom.getMemberType();
                if (matchValue == null) {

                    // RETIRE MoveFrom
                    rvbi.makeAnalog(RETIRED_NID, ec.getAuthorNid(), rvbi.getPathNid(), Long.MAX_VALUE);
                    if (collectionFromConcept.isAnnotationStyleRefex()) {
                        // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                        changedReferencedConcept = true; // pass to BatchActionProcessor
                    } else {
                        ts.addUncommitted(collectionFromConcept);
                    }

                    // ADD MoveTo
                    RefexCAB specTo = new RefexCAB(refsetFromType, rcNid, collectionToNid);
                    if (refsetFromType == TK_REFSET_TYPE.BOOLEAN) {
                        specTo.with(RefexCAB.RefexProperty.BOOLEAN1,
                                specFrom.getBoolean(RefexCAB.RefexProperty.BOOLEAN1));
                    } else if (refsetFromType == TK_REFSET_TYPE.CID) {
                        specTo.with(RefexCAB.RefexProperty.CNID1,
                                specFrom.getInt(RefexCAB.RefexProperty.CNID1)); // int nid
                    } else if (refsetFromType == TK_REFSET_TYPE.INT) {
                        specTo.with(RefexCAB.RefexProperty.INTEGER1,
                                specFrom.getInt(RefexCAB.RefexProperty.INTEGER1));
                    } else if (refsetFromType == TK_REFSET_TYPE.STR) {
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
                            BatchActionTaskType.REFSET_MOVE_MEMBER,
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
                        rvbi.makeAnalog(RETIRED_NID, ec.getAuthorNid(), rvbi.getPathNid(), Long.MAX_VALUE);
                        if (collectionFromConcept.isAnnotationStyleRefex()) {
                            // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                            changedReferencedConcept = true; // pass to BatchActionProcessor
                        } else {
                            ts.addUncommitted(collectionFromConcept);
                        }

                        // ADD MoveTo
                        RefexCAB specTo = new RefexCAB(refsetFromType, rcNid, collectionToNid);
                        if (refsetFromType == TK_REFSET_TYPE.BOOLEAN) {
                            specTo.with(RefexCAB.RefexProperty.BOOLEAN1,
                                    specFrom.getBoolean(RefexCAB.RefexProperty.BOOLEAN1));
                        } else if (refsetFromType == TK_REFSET_TYPE.CID) {
                            specTo.with(RefexCAB.RefexProperty.CNID1,
                                    specFrom.getInt(RefexCAB.RefexProperty.CNID1)); // int nid
                        } else if (refsetFromType == TK_REFSET_TYPE.INT) {
                            specTo.with(RefexCAB.RefexProperty.INTEGER1,
                                    specFrom.getInt(RefexCAB.RefexProperty.INTEGER1));
                        } else if (refsetFromType == TK_REFSET_TYPE.STR) {
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
                                BatchActionTaskType.REFSET_MOVE_MEMBER,
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
                    BatchActionTaskType.REFSET_MOVE_MEMBER,
                    BatchActionEventType.EVENT_NOOP,
                    "was not member of: " + nidToName(collectionFromNid)));
        } else if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.REFSET_MOVE_MEMBER,
                    BatchActionEventType.EVENT_NOOP,
                    "member not retired (not matched): " + nidToName(collectionFromNid)));
        }

        return changedReferencedConcept;
    }
}
