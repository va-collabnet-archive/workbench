package org.ihtsdo.batch;

import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 * BatchActionTaskRefsetMoveMember
 * 
 */
public class BatchActionTaskRefsetMoveMember extends BatchActionTask {

    public BatchActionTaskRefsetMoveMember() {
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws Exception {
        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_MOVE_MEMBER, BatchActionEventType.EVENT_NOOP, ":!!!: Add code."));

        return true;
    }
}
