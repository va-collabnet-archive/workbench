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
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;

/**
 * BatchActionTaskRefsetRetireMember
 * 
 */
public class BatchActionTaskRefsetRetireMember extends BatchActionTask {

    // REFSET MEMBER
    private int collectionNid;
    // FILTER
    private TK_REFSET_TYPE refsetType;
    private Object matchValue;
    
    public BatchActionTaskRefsetRetireMember() {
        this.collectionNid = Integer.MAX_VALUE;
        this.matchValue = null;
    }

    public void setCollectionNid(int collectionNid) {
        this.collectionNid = collectionNid;
    }

    public void setRefsetType(TK_REFSET_TYPE refsetType) {
        this.refsetType = refsetType;
    }

    public void setMatchValue(Object matchValue) {
        this.matchValue = matchValue;
    }
    
    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws IOException {

        Collection<? extends RefexVersionBI<?>> currentRefexes = c.getCurrentRefexes(vc);
        boolean changed = false;
        boolean matched = false;
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getCollectionNid() == collectionNid) {
                if (matchValue == null) {
                    rvbi.makeAnalog(RETIRED_NID, ec.getAuthorNid(), rvbi.getPathNid(), Long.MAX_VALUE);
                    changed = true;
                    BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_RETIRE_MEMBER,
                            BatchActionEventType.EVENT_SUCCESS, "retired member of: " + nidToName(collectionNid)));
                } else {
                    // CHECK FILTER
                    RefexCAB spec = rvbi.getRefexEditSpec();
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
                        rvbi.makeAnalog(RETIRED_NID, ec.getAuthorNid(), rvbi.getPathNid(), Long.MAX_VALUE);
                        changed = true;
                        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_RETIRE_MEMBER,
                                BatchActionEventType.EVENT_SUCCESS, "retired member of (value matched): " + nidToName(collectionNid)));
                    }
                    matched = false;
                }
            }
        }

        if (!changed && matchValue == null) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_RETIRE_MEMBER,
                    BatchActionEventType.EVENT_NOOP, "was not member of: " + nidToName(collectionNid)));
        } else if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_RETIRE_MEMBER,
                    BatchActionEventType.EVENT_NOOP, "member not retired (not matched): " + nidToName(collectionNid)));
        }

        return changed;
    }
}
