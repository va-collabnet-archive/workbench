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
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;

/**
 * BatchActionTaskRefsetReplaceValue
 * 
 */
public class BatchActionTaskRefsetReplaceValue extends BatchActionTask {

    // REFSET MEMBER
    private TK_REFSET_TYPE refsetType;
    private int collectionNid;
    private Object refsetValue;
    // FILTER
    private Object matchValue;

    public BatchActionTaskRefsetReplaceValue() {
        this.collectionNid = Integer.MAX_VALUE;
        this.matchValue = null;
    }

    public void setCollectionNid(int collectionNid) {
        this.collectionNid = collectionNid;
    }

    public void setMatchValue(Object matchValue) {
        this.matchValue = matchValue;
    }

    public void setRefsetType(TK_REFSET_TYPE refsetType) {
        this.refsetType = refsetType;
    }

    public void setRefsetValue(Object refsetValue) {
        this.refsetValue = refsetValue;
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws IOException, InvalidCAB {
        int rcNid = c.getNid(); // referenced component
        Collection<? extends RefexVersionBI<?>> currentRefexes = c.getCurrentRefexes(vc);
        boolean changed = false;
        boolean changedReferencedConcept = false;
        ConceptChronicleBI collectionConcept = ts.getConcept(collectionNid);
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getCollectionNid() == collectionNid) {
                if (matchValue == null) {
                    // RETIRE old value
                    rvbi.makeAnalog(RETIRED_NID, ec.getAuthorNid(), rvbi.getPathNid(), Long.MAX_VALUE);
                    if (collectionConcept.isAnnotationStyleRefex()) {
                        // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                        changedReferencedConcept = true; // pass to BatchActionProcessor
                    } else {
                        ts.addUncommitted(collectionConcept);
                    }

                    // ADD new value
                    RefexCAB refexSpec = null;
                    if (refsetType == TK_REFSET_TYPE.BOOLEAN) {
                        refexSpec = new RefexCAB(TK_REFSET_TYPE.BOOLEAN, rcNid, collectionNid);
                        refexSpec.with(RefexCAB.RefexProperty.BOOLEAN1, (Boolean) refsetValue);
                    } else if (refsetType == TK_REFSET_TYPE.CID) {  // int nid
                        refexSpec = new RefexCAB(TK_REFSET_TYPE.CID, rcNid, collectionNid);
                        refexSpec.with(RefexCAB.RefexProperty.CNID1, ((Integer) refsetValue).intValue());
                    } else if (refsetType == TK_REFSET_TYPE.INT) {
                        refexSpec = new RefexCAB(TK_REFSET_TYPE.INT, rcNid, collectionNid);
                        refexSpec.with(RefexCAB.RefexProperty.INTEGER1, (Integer) refsetValue);
                    } else if (refsetType == TK_REFSET_TYPE.STR) {
                        refexSpec = new RefexCAB(TK_REFSET_TYPE.STR, rcNid, collectionNid);
                        refexSpec.with(RefexCAB.RefexProperty.STRING1, (String) refsetValue);
                    }
                    tsSnapshot.constructIfNotCurrent(refexSpec);

                    if (collectionConcept.isAnnotationStyleRefex()) {
                        // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                        changedReferencedConcept = true; // pass to BatchActionProcessor
                    } else {
                        ts.addUncommitted(collectionConcept);
                    }

                    BatchActionEventReporter.add(new BatchActionEvent(c,
                            BatchActionTaskType.REFSET_ADD_MEMBER,
                            BatchActionEventType.EVENT_SUCCESS,
                            "member value changed: " + nidToName(collectionNid)));

                    changed = true;
                } else {
                    // CHECK FILTER
                    RefexCAB spec = rvbi.getRefexEditSpec();
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
                        // RETIRE old value
                        rvbi.makeAnalog(RETIRED_NID, ec.getAuthorNid(), rvbi.getPathNid(), Long.MAX_VALUE);
                        if (collectionConcept.isAnnotationStyleRefex()) {
                            // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                            changedReferencedConcept = true; // pass to BatchActionProcessor
                        } else {
                            ts.addUncommitted(collectionConcept);
                        }

                        // ADD new value
                        RefexCAB refexSpec = null;
                        if (refsetType == TK_REFSET_TYPE.BOOLEAN) {
                            refexSpec = new RefexCAB(TK_REFSET_TYPE.BOOLEAN, rcNid, collectionNid);
                            refexSpec.with(RefexCAB.RefexProperty.BOOLEAN1, (Boolean) refsetValue);
                        } else if (refsetType == TK_REFSET_TYPE.CID) {
                            refexSpec = new RefexCAB(TK_REFSET_TYPE.CID, rcNid, collectionNid);
                            refexSpec.with(RefexCAB.RefexProperty.CNID1, ((Integer) refsetValue).intValue()); // int nid
                        } else if (refsetType == TK_REFSET_TYPE.INT) {
                            refexSpec = new RefexCAB(TK_REFSET_TYPE.INT, rcNid, collectionNid);
                            refexSpec.with(RefexCAB.RefexProperty.INTEGER1, (Integer) refsetValue);
                        } else if (refsetType == TK_REFSET_TYPE.STR) {
                            refexSpec = new RefexCAB(TK_REFSET_TYPE.STR, rcNid, collectionNid);
                            refexSpec.with(RefexCAB.RefexProperty.STRING1, (String) refsetValue);
                        }

                        tsSnapshot.constructIfNotCurrent(refexSpec);

                        if (collectionConcept.isAnnotationStyleRefex()) {
                            // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                            changedReferencedConcept = true; // pass to BatchActionProcessor
                        } else {
                            ts.addUncommitted(collectionConcept);
                        }

                        BatchActionEventReporter.add(new BatchActionEvent(c,
                                BatchActionTaskType.REFSET_ADD_MEMBER,
                                BatchActionEventType.EVENT_SUCCESS,
                                "(matched) member value changed: " + nidToName(collectionNid)));

                        changed = true;
                    }
                    matched = false;
                }
            }
        }

        if (!changed && matchValue == null) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.REFSET_REPLACE_VALUE,
                    BatchActionEventType.EVENT_NOOP,
                    "was not member of: " + nidToName(collectionNid)));
        } else if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.REFSET_REPLACE_VALUE,
                    BatchActionEventType.EVENT_NOOP,
                    "member value not changed (not matched): " + nidToName(collectionNid)));
        }

        return changedReferencedConcept;
    }
}
