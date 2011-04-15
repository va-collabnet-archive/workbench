package org.ihtsdo.batch;

import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 * BatchActionTaskRefsetReplaceValue
 * 
 */
public class BatchActionTaskRefsetReplaceValue extends BatchActionTask {

    public BatchActionTaskRefsetReplaceValue() {
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) {

        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_REPLACE_VALUE, BatchActionEventType.EVENT_NOOP, ":!!!: Add code."));

        return true;
    }
}
