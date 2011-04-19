package org.ihtsdo.batch;

import java.util.Collection;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
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
        int rcNid = c.getNid();
        Collection<? extends RefexVersionBI<?>> currentRefexes = c.getCurrentRefexes(vc);

        // CHECK FROM HAS CONCEPT
        // CHECK TO DOES NOT HAVE REFERENCED CONCEPT
        boolean isInFrom = false;
        boolean isInTo = false;
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getCollectionNid() == collectionFromNid) {
                isInFrom = true;
            }
            if (rvbi.getCollectionNid() == collectionToNid) {
                isInTo = true;
            }
        }
        if (isInFrom == false) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_MOVE_MEMBER,
                    BatchActionEventType.EVENT_NOOP, "member not present in move from: "
                    + nidToName(collectionFromNid) + " to:" + nidToName(collectionFromNid)));
            return false;
        }
        if (isInTo == true) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_MOVE_MEMBER,
                    BatchActionEventType.EVENT_NOOP, "member already present in move to: "
                    + nidToName(collectionFromNid) + " to:" + nidToName(collectionToNid)));
            return false;
        }

        //
        boolean changed = false;
        boolean matched = false;
        for (RefexVersionBI rvbi : currentRefexes) {
            if (rvbi.getCollectionNid() == collectionFromNid) {
                if (matchValue == null) {
                    RefexCAB specFrom = rvbi.getRefexEditSpec();
                    TK_REFSET_TYPE refsetFromType = specFrom.getMemberType();
                    rvbi.makeAnalog(RETIRED_NID, ec.getAuthorNid(), rvbi.getPathNid(), Long.MAX_VALUE);

                    // If not already a member, then a member record is added.
                    RefexCAB specTo = new RefexCAB(refsetFromType, rcNid, collectionToNid);
                    if (refsetFromType == TK_REFSET_TYPE.BOOLEAN) {
                        specTo.with(RefexCAB.RefexProperty.BOOLEAN1, specFrom.getBoolean(RefexCAB.RefexProperty.BOOLEAN1));
                    } else if (refsetFromType == TK_REFSET_TYPE.CID) {
                        specTo.with(RefexCAB.RefexProperty.CNID1, specFrom.getInt(RefexCAB.RefexProperty.CNID1)); // int nid
                    } else if (refsetFromType == TK_REFSET_TYPE.INT) {
                        specTo.with(RefexCAB.RefexProperty.INTEGER1, specFrom.getInt(RefexCAB.RefexProperty.INTEGER1));
                    } else if (refsetFromType == TK_REFSET_TYPE.STR) {
                        specTo.with(RefexCAB.RefexProperty.STRING1, specFrom.getString(RefexCAB.RefexProperty.STRING1));
                    }

                    BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_MOVE_MEMBER,
                            BatchActionEventType.EVENT_SUCCESS, "member moved from: " + nidToName(collectionFromNid)
                            + " to:" + nidToName(collectionToNid)));
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
                        RefexCAB specFrom = rvbi.getRefexEditSpec();
                        TK_REFSET_TYPE refsetFromType = specFrom.getMemberType();
                        rvbi.makeAnalog(RETIRED_NID, ec.getAuthorNid(), rvbi.getPathNid(), Long.MAX_VALUE);

                        // If not already a member, then a member record is added.
                        RefexCAB specTo = new RefexCAB(refsetFromType, rcNid, collectionToNid);
                        if (refsetFromType == TK_REFSET_TYPE.BOOLEAN) {
                            specTo.with(RefexCAB.RefexProperty.BOOLEAN1, specFrom.getBoolean(RefexCAB.RefexProperty.BOOLEAN1));
                        } else if (refsetFromType == TK_REFSET_TYPE.CID) {
                            specTo.with(RefexCAB.RefexProperty.CNID1, specFrom.getInt(RefexCAB.RefexProperty.CNID1)); // int nid
                        } else if (refsetFromType == TK_REFSET_TYPE.INT) {
                            specTo.with(RefexCAB.RefexProperty.INTEGER1, specFrom.getInt(RefexCAB.RefexProperty.INTEGER1));
                        } else if (refsetFromType == TK_REFSET_TYPE.STR) {
                            specTo.with(RefexCAB.RefexProperty.STRING1, specFrom.getString(RefexCAB.RefexProperty.STRING1));
                        }

                        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_MOVE_MEMBER,
                                BatchActionEventType.EVENT_SUCCESS, "(matched) member moved from: "
                                + nidToName(collectionFromNid) + " to:" + nidToName(collectionToNid)));
                        changed = true;
                    }
                    matched = false;
                }
            }
        }

        if (!changed && matchValue == null) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_MOVE_MEMBER,
                    BatchActionEventType.EVENT_NOOP, "was not member of: " + nidToName(collectionFromNid)));
        } else if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_MOVE_MEMBER,
                    BatchActionEventType.EVENT_NOOP, "member not retired (not matched): " + nidToName(collectionFromNid)));
        }

        return changed;
    }
}
