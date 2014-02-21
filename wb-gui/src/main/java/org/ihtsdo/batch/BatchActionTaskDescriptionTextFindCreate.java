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
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 * BatchActionTaskDescriptionTextFindCreate
 * 
 */
public class BatchActionTaskDescriptionTextFindCreate extends BatchActionTask {

    // DESCRIPTION CRITERIA
    private int searchByTextConstraint; // NA | Contains | Begins With | Ends With
    private String searchText;
    private boolean isSearchCaseSensitive;
    private int searchByType; // NA | FSN | Synomym | Description
    private int searchByLanguage;

    // REFSET MEMBER
    private TK_REFEX_TYPE refsetType;
    private int collectionNid;
    private Object refsetValue;
    // FILTER
    private Object matchValue;

    public BatchActionTaskDescriptionTextFindCreate() {
        this.searchByTextConstraint = 0; // Does Not Apply
        this.searchText = null;
        this.isSearchCaseSensitive = false;
        this.searchByType = 0; // Does Not Apply
        this.searchByLanguage = 0;
        
        this.collectionNid = Integer.MAX_VALUE;
        this.matchValue = null;
    }

    public void setSearchByTextConstraint(int searchByTextConstraint) {
        this.searchByTextConstraint = searchByTextConstraint;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public void setIsSearchCaseSensitive(boolean isSearchCaseSensitive) {
        this.isSearchCaseSensitive = isSearchCaseSensitive;
    }

    public void setSearchByType(int searchByType) {
        this.searchByType = searchByType;
    }

    public void setSearchByLanguage(int searchByLanguage) {
        this.searchByLanguage = searchByLanguage;
    }

    public void setCollectionNid(int collectionNid) {
        this.collectionNid = collectionNid;
    }

    public void setMatchValue(Object matchValue) {
        this.matchValue = matchValue;
    }

    public void setRefsetType(TK_REFEX_TYPE refsetType) {
        this.refsetType = refsetType;
    }

    public void setRefsetValue(Object refsetValue) {
        this.refsetValue = refsetValue;
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc)
            throws IOException, InvalidCAB, ContradictionException {
        int rcNid = c.getNid(); // referenced component
        Collection<? extends RefexVersionBI<?>> currentRefexes = c.getRefexesActive(vc);
        boolean changed = false;
        boolean changedReferencedConcept = false;
        ConceptChronicleBI collectionConcept = ts.getConcept(collectionNid);
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getRefexNid() == collectionNid) {
                if (matchValue == null) {
                    RefexCAB blueprint = rvbi.makeBlueprint(vc);
                    blueprint.setMemberUuid(rvbi.getPrimUuid());
                    TK_REFEX_TYPE memberType = blueprint.getMemberType();

                    // CHANGE value
                    if (refsetType == TK_REFEX_TYPE.BOOLEAN) {
                        blueprint.with(RefexCAB.RefexProperty.BOOLEAN1, (Boolean) refsetValue);
                    } else if (refsetType == TK_REFEX_TYPE.CID) {  // int nid
                        blueprint.with(RefexCAB.RefexProperty.CNID1, ((Integer) refsetValue).intValue());
                    } else if (refsetType == TK_REFEX_TYPE.INT) {
                        blueprint.with(RefexCAB.RefexProperty.INTEGER1, (Integer) refsetValue);
                    } else if (refsetType == TK_REFEX_TYPE.STR) {
                        blueprint.with(RefexCAB.RefexProperty.STRING1, (String) refsetValue);
                    }
                    RefexChronicleBI<?> annot = tsSnapshot.constructIfNotCurrent(blueprint);

                    if (collectionConcept.isAnnotationStyleRefex()) {
                        // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                        changedReferencedConcept = true; // pass to BatchActionProcessor
                        c.addAnnotation(annot);
                        ts.addUncommitted(c);
                    } else {
                        changedReferencedConcept = true; //... pass to BatchActionProcessor
                        ts.addUncommitted(collectionConcept);
                        ts.addUncommitted(c);
                    }

                    BatchActionEventReporter.add(new BatchActionEvent(c,
                            BatchActionTaskType.DESCRIPTION_TEXT_FIND_CREATE,
                            BatchActionEventType.EVENT_SUCCESS,
                            "member value changed: " + nidToName(collectionNid)));

                    changed = true;
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
                        RefexCAB blueprint = rvbi.makeBlueprint(vc);
                        TK_REFEX_TYPE memberType = blueprint.getMemberType();

                        // CHANGE value
                        if (refsetType == TK_REFEX_TYPE.BOOLEAN) {
                            blueprint.with(RefexCAB.RefexProperty.BOOLEAN1, (Boolean) refsetValue);
                        } else if (refsetType == TK_REFEX_TYPE.CID) {  // int nid
                            blueprint.with(RefexCAB.RefexProperty.CNID1, ((Integer) refsetValue).intValue());
                        } else if (refsetType == TK_REFEX_TYPE.INT) {
                            blueprint.with(RefexCAB.RefexProperty.INTEGER1, (Integer) refsetValue);
                        } else if (refsetType == TK_REFEX_TYPE.STR) {
                            blueprint.with(RefexCAB.RefexProperty.STRING1, (String) refsetValue);
                        }
                        RefexChronicleBI<?> annot = tsSnapshot.constructIfNotCurrent(blueprint);

                        if (collectionConcept.isAnnotationStyleRefex()) {
                            // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                            changedReferencedConcept = true; // pass to BatchActionProcessor
                            c.addAnnotation(annot);
                            ts.addUncommitted(c);
                        } else {
                            changedReferencedConcept = true; //... pass to BatchActionProcessor
                            ts.addUncommitted(collectionConcept);
                            ts.addUncommitted(c);
                        }

                        BatchActionEventReporter.add(new BatchActionEvent(c,
                                BatchActionTaskType.DESCRIPTION_TEXT_FIND_CREATE,
                                BatchActionEventType.EVENT_SUCCESS,
                                "(matched) member value changed: " + nidToName(collectionNid)));

                        changed = true;
                    }
                }
            }
        }

        if (!changed && matchValue == null) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.DESCRIPTION_TEXT_FIND_CREATE,
                    BatchActionEventType.EVENT_NOOP,
                    "was not member of: " + nidToName(collectionNid)));
        } else if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.DESCRIPTION_TEXT_FIND_CREATE,
                    BatchActionEventType.EVENT_NOOP,
                    "member value not changed (not matched): " + nidToName(collectionNid)));
        }

        return changedReferencedConcept;
    }
}
