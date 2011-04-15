package org.ihtsdo.batch;

import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 * BatchActionTaskRoleReplaceValue
 * 
 */
public class BatchActionTaskRoleReplaceValue extends BatchActionTask {

    public BatchActionTaskRoleReplaceValue() {
    }

    //
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) {

        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.ROLE_REPLACE_VALUE, BatchActionEventType.EVENT_NOOP, ":!!!: add code"));

        return true;
    }
}
