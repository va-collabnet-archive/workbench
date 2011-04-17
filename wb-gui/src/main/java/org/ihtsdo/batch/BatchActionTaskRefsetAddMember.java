package org.ihtsdo.batch;

import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;


/**
 * Sample BatchAction
 * 
 */

public class BatchActionTaskRefsetAddMember extends BatchActionTask {

    // :!!!: Refset Collection
    // :!!!: Refset Value

    public BatchActionTaskRefsetAddMember() {
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws Exception {
        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_ADD_MEMBER, BatchActionEventType.EVENT_NOOP, ":!!!: add code."));

        return true;
    }

}
