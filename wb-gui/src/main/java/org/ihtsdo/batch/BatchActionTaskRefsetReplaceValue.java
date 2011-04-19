package org.ihtsdo.batch;

import java.io.IOException;
import java.util.Collection;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
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
        Collection<? extends RefexVersionBI<?>> currentRefexes = c.getCurrentRefexes(vc);
        boolean changed = false;
        boolean matched = false;
        RefexCAB refexSpec;
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getCollectionNid() == collectionNid) {
                if (matchValue == null) {
                    refexSpec = rvbi.getRefexEditSpec();
                    if (refsetType == TK_REFSET_TYPE.BOOLEAN) {
                        refexSpec.with(RefexCAB.RefexProperty.BOOLEAN1, (Boolean) refsetValue);
                    } else if (refsetType == TK_REFSET_TYPE.CID) {
                        refexSpec.with(RefexCAB.RefexProperty.CNID1, ((Integer) refsetValue).intValue()); // int nid
                    } else if (refsetType == TK_REFSET_TYPE.INT) {
                        refexSpec.with(RefexCAB.RefexProperty.INTEGER1, (Integer) refsetValue);
                    } else if (refsetType == TK_REFSET_TYPE.STR) {
                        refexSpec.with(RefexCAB.RefexProperty.STRING1, (String) refsetValue);
                    }

                    BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_ADD_MEMBER,
                            BatchActionEventType.EVENT_SUCCESS, "member value changed: " + nidToName(collectionNid)));

                    changed = true;
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
                        refexSpec = rvbi.getRefexEditSpec();
                        if (refsetType == TK_REFSET_TYPE.BOOLEAN) {
                            refexSpec.with(RefexCAB.RefexProperty.BOOLEAN1, (Boolean) refsetValue);
                        } else if (refsetType == TK_REFSET_TYPE.CID) {
                            refexSpec.with(RefexCAB.RefexProperty.CNID1, ((Integer) refsetValue).intValue()); // int nid
                        } else if (refsetType == TK_REFSET_TYPE.INT) {
                            refexSpec.with(RefexCAB.RefexProperty.INTEGER1, (Integer) refsetValue);
                        } else if (refsetType == TK_REFSET_TYPE.STR) {
                            refexSpec.with(RefexCAB.RefexProperty.STRING1, (String) refsetValue);
                        }

                        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_ADD_MEMBER,
                                BatchActionEventType.EVENT_SUCCESS, "(matched) member value changed: " + nidToName(collectionNid)));

                        changed = true;
                    }
                    matched = false;
                }
            }
        }

        if (!changed && matchValue == null) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_REPLACE_VALUE,
                    BatchActionEventType.EVENT_NOOP, "was not member of: " + nidToName(collectionNid)));
        } else if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_REPLACE_VALUE,
                    BatchActionEventType.EVENT_NOOP, "member value not changed (not matched): " + nidToName(collectionNid)));
        }

        return changed;
    }
}
