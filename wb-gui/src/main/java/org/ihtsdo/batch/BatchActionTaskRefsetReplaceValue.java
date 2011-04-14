package org.ihtsdo.batch;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 * BatchActionTaskRefsetReplaceValue
 * 
 */
public class BatchActionTaskRefsetReplaceValue extends BatchActionTask {

    public BatchActionTaskRefsetReplaceValue() {
    }

    //
    @Override
    public boolean execute(ConceptVersionBI c) {
        System.out.println("## BatchActionTaskRefsetReplaceValue concept: " + c);

        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_REPLACE_VALUE));

        return true;
    }
}
